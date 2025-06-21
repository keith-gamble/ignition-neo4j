package dev.bwdesigngroup.neo4j.gateway.components;

import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import dev.bwdesigngroup.neo4j.gateway.records.DatabaseRecord;
import dev.bwdesigngroup.neo4j.gateway.records.RemoteDatabaseRecord;

/**
 * Manages Neo4J database connections with proper lifecycle management,
 * health monitoring, and retry logic.
 */
public class Neo4JConnectionManager {
    private static final Logger logger = LoggerFactory.getLogger(Neo4JConnectionManager.class);

    // Connection pools by name
    private final ConcurrentHashMap<String, ConnectionPool> connectionPools = new ConcurrentHashMap<>();

    // Health monitoring
    private final ScheduledExecutorService healthCheckExecutor = Executors.newScheduledThreadPool(2);
    private final ScheduledExecutorService retryExecutor = Executors.newScheduledThreadPool(1);

    // Configuration
    private static final Duration HEALTH_CHECK_INTERVAL = Duration.ofSeconds(30);
    private static final Duration RETRY_INITIAL_DELAY = Duration.ofSeconds(5);
    private static final Duration RETRY_MAX_DELAY = Duration.ofMinutes(5);
    private static final int MAX_RETRY_ATTEMPTS = 10;

    private final AtomicBoolean isShutdown = new AtomicBoolean(false);

    /**
     * Get detailed diagnostic information for a connection
     */
    public String getConnectionDiagnostics(String name) {
        ConnectionPool pool = connectionPools.get(name);
        if (pool == null) {
            return "Connection '" + name + "' not found";
        }

        StringBuilder diagnostics = new StringBuilder();
        diagnostics.append("Connection Diagnostics for '").append(name).append("':\n");
        diagnostics.append("  State: ").append(pool.getState()).append("\n");
        diagnostics.append("  URI: ").append(pool.remoteSettings.getUri()).append("\n");
        diagnostics.append("  Username: ")
                .append(pool.remoteSettings.getUsername() != null ? pool.remoteSettings.getUsername() : "None")
                .append("\n");
        diagnostics.append("  Database: ")
                .append(pool.settings.getDatabase() != null ? pool.settings.getDatabase() : "Default").append("\n");
        diagnostics.append("  Enabled: ").append(pool.settings.getEnabled()).append("\n");
        diagnostics.append("  Last Health Check: ").append(pool.getLastHealthCheck()).append("\n");
        diagnostics.append("  Retry Attempts: ").append(pool.getRetryAttempts()).append("\n");

        if (pool.getLastError() != null) {
            diagnostics.append("  Last Error: ").append(pool.getLastError()).append("\n");
        }

        if (pool.connector != null) {
            diagnostics.append("  Connection Stats: ").append(pool.connector.getConnectionStats()).append("\n");
            diagnostics.append("  Active Connections: ").append(pool.connector.getActiveConnections())
                    .append("/").append(pool.connector.getMaxConnectionPoolSize()).append("\n");
        }

        return diagnostics.toString();
    }

    /**
     * Represents a managed connection pool for a single database configuration
     */
    public static class ConnectionPool {
        private final String name;
        private final DatabaseRecord settings;
        public final RemoteDatabaseRecord remoteSettings;
        private volatile DatabaseConnector connector;
        private volatile ConnectionState state = ConnectionState.DISCONNECTED;
        private volatile Instant lastHealthCheck = Instant.now();
        private volatile String lastError;
        private final AtomicInteger retryAttempts = new AtomicInteger(0);
        private volatile Instant nextRetryTime = Instant.now();

        public ConnectionPool(String name, DatabaseRecord settings, RemoteDatabaseRecord remoteSettings) {
            this.name = name;
            this.settings = settings;
            this.remoteSettings = remoteSettings;
        }

        public String getName() {
            return name;
        }

        public ConnectionState getState() {
            return state;
        }

        public String getLastError() {
            return lastError;
        }

        public Instant getLastHealthCheck() {
            return lastHealthCheck;
        }

        public DatabaseConnector getConnector() {
            return connector;
        }

        public DatabaseRecord getSettings() {
            return settings;
        }

        public int getRetryAttempts() {
            return retryAttempts.get();
        }

        void setState(ConnectionState newState) {
            if (this.state != newState) {
                logger.info("Connection '{}' state changed: {} -> {}", name, this.state, newState);
                this.state = newState;
            }
        }

        void setError(String error) {
            this.lastError = error;
            logger.warn("Connection '{}' error: {}", name, error);
        }

        void clearError() {
            this.lastError = null;
            this.retryAttempts.set(0);
        }

        void updateHealthCheck() {
            this.lastHealthCheck = Instant.now();
        }

        boolean shouldRetry() {
            return retryAttempts.get() < MAX_RETRY_ATTEMPTS &&
                    Instant.now().isAfter(nextRetryTime);
        }

        void scheduleRetry() {
            int attempts = retryAttempts.incrementAndGet();
            long delaySeconds = Math.min(
                    RETRY_INITIAL_DELAY.toSeconds() * (1L << (attempts - 1)),
                    RETRY_MAX_DELAY.toSeconds());
            nextRetryTime = Instant.now().plus(Duration.ofSeconds(delaySeconds));
            logger.info("Connection '{}' retry scheduled in {} seconds (attempt {})",
                    name, delaySeconds, attempts);
        }
    }

    public enum ConnectionState {
        DISCONNECTED,
        CONNECTING,
        CONNECTED,
        FAILED,
        DISABLED
    }

    /**
     * Initialize the connection manager and start background tasks
     */
    public void initialize() {
        if (isShutdown.get()) {
            throw new IllegalStateException("Cannot initialize after shutdown");
        }

        // Start health check monitoring
        healthCheckExecutor.scheduleWithFixedDelay(
                this::performHealthChecks,
                HEALTH_CHECK_INTERVAL.toSeconds(),
                HEALTH_CHECK_INTERVAL.toSeconds(),
                TimeUnit.SECONDS);

        // Start retry processing
        retryExecutor.scheduleWithFixedDelay(
                this::processRetries,
                RETRY_INITIAL_DELAY.toSeconds(),
                RETRY_INITIAL_DELAY.toSeconds(),
                TimeUnit.SECONDS);

        logger.info("Neo4J Connection Manager initialized");
    }

    /**
     * Add or update a connection configuration
     */
    public void addOrUpdateConnection(String name, DatabaseRecord settings, RemoteDatabaseRecord remoteSettings) {
        if (isShutdown.get()) {
            logger.warn("Cannot add connection '{}' - manager is shutdown", name);
            return;
        }

        ConnectionPool existingPool = connectionPools.get(name);

        // Close existing connection if configuration changed
        if (existingPool != null) {
            closeConnectionSafely(existingPool);
        }

        ConnectionPool newPool = new ConnectionPool(name, settings, remoteSettings);
        connectionPools.put(name, newPool);

        // Attempt initial connection if enabled
        if (settings.getEnabled()) {
            attemptConnection(newPool);
        } else {
            newPool.setState(ConnectionState.DISABLED);
        }

        logger.info("Connection '{}' added/updated", name);
    }

    /**
     * Remove a connection configuration
     */
    public void removeConnection(String name) {
        ConnectionPool pool = connectionPools.remove(name);
        if (pool != null) {
            closeConnectionSafely(pool);
            logger.info("Connection '{}' removed", name);
        }
    }

    /**
     * Get a connection by name - returns null if not available
     */
    public DatabaseConnector getConnection(String name) {
        ConnectionPool pool = connectionPools.get(name);
        if (pool == null) {
            return null;
        }

        if (pool.getState() == ConnectionState.CONNECTED && pool.connector != null) {
            return pool.connector;
        }

        return null;
    }

    /**
     * Get connection status information
     */
    public ConnectionPool getConnectionStatus(String name) {
        return connectionPools.get(name);
    }

    /**
     * Get all connection names
     */
    public java.util.Set<String> getConnectionNames() {
        return connectionPools.keySet();
    }

    /**
     * Shutdown the connection manager and close all connections
     */
    public void shutdown() {
        if (isShutdown.compareAndSet(false, true)) {
            logger.info("Shutting down Neo4J Connection Manager");

            // Stop background tasks
            shutdownExecutor(healthCheckExecutor, "Health Check");
            shutdownExecutor(retryExecutor, "Retry");

            // Close all connections
            connectionPools.values().forEach(this::closeConnectionSafely);
            connectionPools.clear();

            logger.info("Neo4J Connection Manager shutdown complete");
        }
    }

    /**
     * Attempt to establish a connection
     */
    private void attemptConnection(ConnectionPool pool) {
        if (isShutdown.get()) {
            return;
        }

        pool.setState(ConnectionState.CONNECTING);

        // Close existing connection if any
        if (pool.connector != null) {
            closeConnectionSafely(pool);
        }

        DatabaseConnector newConnector = null;
        boolean connectionSuccessful = false;

        try {
            logger.debug("Attempting to create connector for '{}'", pool.getName());

            // Create new connection
            newConnector = new DatabaseConnector(pool.settings, pool.remoteSettings);

            // Initialize the connection
            if (!newConnector.initialize()) {
                String initError = newConnector.getLastError();
                if (initError != null) {
                    throw new RuntimeException("Connection initialization failed: " + initError);
                } else {
                    throw new RuntimeException("Connection initialization failed for unknown reason");
                }
            }

            // Additional verification
            if (!newConnector.isConnected()) {
                String connError = newConnector.getLastError();
                if (connError != null) {
                    throw new RuntimeException("Connection verification failed: " + connError);
                } else {
                    throw new RuntimeException("Connection verification failed - driver reports not connected");
                }
            }

            // Connection successful - assign to pool
            pool.connector = newConnector;
            connectionSuccessful = true; // Mark as successful to prevent cleanup
            pool.setState(ConnectionState.CONNECTED);
            pool.clearError();
            logger.info("Successfully connected to '{}' at {}",
                    pool.getName(), pool.remoteSettings.getUri());

        } catch (Exception e) {
            pool.setState(ConnectionState.FAILED);

            // Get more detailed error information
            String detailedError;
            if (newConnector != null && newConnector.getLastError() != null) {
                detailedError = newConnector.getLastError();
            } else {
                detailedError = e.getMessage();
            }

            pool.setError(detailedError);
            pool.scheduleRetry();

            logger.error("Failed to connect to '{}' at {}: {}",
                    pool.getName(), pool.remoteSettings.getUri(), detailedError);

            // Log the full exception for debugging
            if (logger.isDebugEnabled()) {
                logger.debug("Full connection failure details for '{}':", pool.getName(), e);
            }

        } finally {
            // Only close if connection was not successful
            if (!connectionSuccessful && newConnector != null) {
                try {
                    newConnector.close();
                } catch (Exception closeEx) {
                    logger.warn("Error closing failed connector for '{}': {}",
                            pool.getName(), closeEx.getMessage());
                }
            }
        }
    }

    /**
     * Remove the transferConnectionOwnership method as it's no longer needed
     */

    /**
     * Perform health checks on all connections
     */
    private void performHealthChecks() {
        if (isShutdown.get()) {
            return;
        }

        connectionPools.values().parallelStream().forEach(pool -> {
            if (pool.getState() == ConnectionState.CONNECTED && pool.connector != null) {
                try {
                    if (pool.connector.isConnected()) {
                        pool.updateHealthCheck();
                    } else {
                        logger.warn("Health check failed for connection '{}'", pool.getName());
                        pool.setState(ConnectionState.FAILED);
                        pool.setError("Health check failed");
                        pool.scheduleRetry();
                    }
                } catch (Exception e) {
                    logger.error("Health check error for connection '{}': {}",
                            pool.getName(), e.getMessage());
                    pool.setState(ConnectionState.FAILED);
                    pool.setError("Health check error: " + e.getMessage());
                    pool.scheduleRetry();
                }
            }
        });
    }

    /**
     * Process connection retries
     */
    private void processRetries() {
        if (isShutdown.get()) {
            return;
        }

        connectionPools.values().stream()
                .filter(pool -> pool.getState() == ConnectionState.FAILED)
                .filter(pool -> pool.getSettings().getEnabled())
                .filter(ConnectionPool::shouldRetry)
                .forEach(this::attemptConnection);
    }

    /**
     * Safely close a connection pool
     */
    private void closeConnectionSafely(ConnectionPool pool) {
        if (pool.connector != null) {
            try {
                pool.connector.close();
            } catch (Exception e) {
                logger.warn("Error closing connection '{}': {}", pool.getName(), e.getMessage());
            } finally {
                pool.connector = null;
                pool.setState(ConnectionState.DISCONNECTED);
            }
        }
    }

    /**
     * Safely shutdown an executor service
     */
    private void shutdownExecutor(ScheduledExecutorService executor, String name) {
        try {
            executor.shutdown();
            if (!executor.awaitTermination(10, TimeUnit.SECONDS)) {
                logger.warn("{} executor did not terminate gracefully, forcing shutdown", name);
                executor.shutdownNow();
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            executor.shutdownNow();
        }
    }
}