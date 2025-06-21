package dev.bwdesigngroup.neo4j.gateway.components;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import org.jetbrains.annotations.Nullable;
import org.neo4j.driver.AuthTokens;
import org.neo4j.driver.Config;
import org.neo4j.driver.ConnectionPoolMetrics;
import org.neo4j.driver.Driver;
import org.neo4j.driver.GraphDatabase;
import org.neo4j.driver.Record;
import org.neo4j.driver.Result;
import org.neo4j.driver.Session;
import org.neo4j.driver.SessionConfig;
import org.neo4j.driver.SessionConfig.Builder;
import org.neo4j.driver.Transaction;
import org.neo4j.driver.summary.Notification;
import org.neo4j.driver.summary.ResultSummary;
import org.neo4j.driver.summary.SummaryCounters;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import dev.bwdesigngroup.neo4j.gateway.records.DatabaseRecord;
import dev.bwdesigngroup.neo4j.gateway.records.RemoteDatabaseRecord;
import com.inductiveautomation.ignition.common.gson.Gson;

/**
 * Improved DatabaseConnector that handles connection lifecycle safely
 * without throwing exceptions from constructor
 */
public class DatabaseConnector implements AutoCloseable {
    private final Logger logger = LoggerFactory.getLogger(getClass());

    // Connection state
    private volatile Driver driver;
    private final AtomicBoolean isInitialized = new AtomicBoolean(false);
    private final AtomicBoolean isClosed = new AtomicBoolean(false);

    // Configuration
    private final DatabaseRecord settingsRecord;
    private final RemoteDatabaseRecord databaseRecord;
    private final SessionConfig sessionConfig;
    private final int slowQueryThreshold;
    private final int maxConnectionPoolSize;
    private final String database;
    private final boolean enabled;

    // Connection metrics
    private volatile String lastError;
    private volatile long connectionAttempts = 0;
    private volatile long successfulQueries = 0;
    private volatile long failedQueries = 0;

    /**
     * Get the settings record (for connection manager access)
     */
    public DatabaseRecord getSettingsRecord() {
        return settingsRecord;
    }

    /**
     * Get the database record (for connection manager access)
     */
    public RemoteDatabaseRecord getDatabaseRecord() {
        return databaseRecord;
    }

    /**
     * Create a DatabaseConnector. Does not establish connection in constructor.
     * Call initialize() to establish the actual connection.
     */
    public DatabaseConnector(DatabaseRecord settingsRecord, RemoteDatabaseRecord databaseRecord) {
        this.settingsRecord = settingsRecord;
        this.databaseRecord = databaseRecord;
        this.database = settingsRecord.getDatabase();
        this.slowQueryThreshold = settingsRecord.getSlowQueryThreshold();
        this.maxConnectionPoolSize = settingsRecord.getMaxConnectionPoolSize();
        this.enabled = settingsRecord.getEnabled();

        // Build session config
        Builder configBuilder = SessionConfig.builder();
        if (this.database != null) {
            configBuilder.withDatabase(this.database);
        }
        this.sessionConfig = configBuilder.build();

        logger.debug("DatabaseConnector created for '{}' (enabled: {})",
                settingsRecord.getName(), enabled);
    }

    /**
     * Initialize the connection. Returns true if successful, false otherwise.
     * Safe to call multiple times - will only initialize once.
     */
    public boolean initialize() {
        if (!enabled) {
            logger.info("Connector '{}' is disabled, skipping initialization", settingsRecord.getName());
            return false;
        }

        if (isClosed.get()) {
            logger.warn("Cannot initialize closed connector '{}'", settingsRecord.getName());
            return false;
        }

        if (isInitialized.get()) {
            return driver != null;
        }

        return performInitialization();
    }

    /**
     * Perform the actual connection initialization
     */
    private synchronized boolean performInitialization() {
        if (isInitialized.get()) {
            return driver != null;
        }

        connectionAttempts++;

        try {
            Config config = Config.builder()
                    .withMaxConnectionLifetime(30, TimeUnit.MINUTES)
                    .withMaxConnectionPoolSize(maxConnectionPoolSize)
                    .withConnectionAcquisitionTimeout(30, TimeUnit.SECONDS)
                    .withConnectionTimeout(15, TimeUnit.SECONDS)
                    .withDriverMetrics()
                    .build();

            logger.debug("Creating Neo4J driver for '{}' with URI: {}",
                    settingsRecord.getName(), databaseRecord.getUri());

            if (databaseRecord.getUsername() != null) {
                driver = GraphDatabase.driver(
                        databaseRecord.getUri(),
                        AuthTokens.basic(databaseRecord.getUsername(), databaseRecord.getPassword()),
                        config);
                logger.debug("Created driver with authentication for '{}'", settingsRecord.getName());
            } else {
                driver = GraphDatabase.driver(databaseRecord.getUri(), config);
                logger.debug("Created driver without authentication for '{}'", settingsRecord.getName());
            }

            // Test the connection with detailed error reporting
            logger.debug("Testing connectivity for '{}'", settingsRecord.getName());
            driver.verifyConnectivity();
            logger.debug("Connectivity verified successfully for '{}'", settingsRecord.getName());

            isInitialized.set(true);
            lastError = null;
            logger.info("Successfully initialized connector '{}'", settingsRecord.getName());
            return true;

        } catch (org.neo4j.driver.exceptions.ServiceUnavailableException e) {
            lastError = String.format("Service unavailable - Neo4J server may not be running at %s: %s",
                    databaseRecord.getUri(), e.getMessage());
            logger.error("Service unavailable for connector '{}': {}",
                    settingsRecord.getName(), lastError, e);
        } catch (org.neo4j.driver.exceptions.AuthenticationException e) {
            lastError = String.format("Authentication failed for user '%s': %s",
                    databaseRecord.getUsername(), e.getMessage());
            logger.error("Authentication failed for connector '{}': {}",
                    settingsRecord.getName(), lastError, e);
        } catch (org.neo4j.driver.exceptions.SecurityException e) {
            lastError = String.format("Security/TLS error connecting to %s: %s",
                    databaseRecord.getUri(), e.getMessage());
            logger.error("Security error for connector '{}': {}",
                    settingsRecord.getName(), lastError, e);
        } catch (IllegalArgumentException e) {
            lastError = String.format("Invalid connection URI '%s': %s",
                    databaseRecord.getUri(), e.getMessage());
            logger.error("Invalid URI for connector '{}': {}",
                    settingsRecord.getName(), lastError, e);
        } catch (Exception e) {
            lastError = String.format("Unexpected error connecting to %s: %s (%s)",
                    databaseRecord.getUri(), e.getMessage(), e.getClass().getSimpleName());
            logger.error("Unexpected error for connector '{}': {}",
                    settingsRecord.getName(), lastError, e);
        }

        // Clean up failed driver
        if (driver != null) {
            try {
                driver.close();
            } catch (Exception closeEx) {
                logger.warn("Error closing failed driver for '{}': {}",
                        settingsRecord.getName(), closeEx.getMessage());
            }
            driver = null;
        }

        return false;
    }

    /**
     * Execute an update query (INSERT, UPDATE, DELETE, CREATE, etc.)
     */
    public void updateQuery(final String query, @Nullable Map<String, Object> params) {
        if (!ensureConnected()) {
            throw new RuntimeException("Database connector is not available for " + settingsRecord.getName());
        }

        try (Session session = driver.session(sessionConfig)) {
            Transaction tx = session.beginTransaction();
            String[] commands = query.split(";");

            for (String command : commands) {
                if (command.trim().isEmpty()) {
                    continue;
                }

                Result result;
                if (params == null) {
                    result = tx.run(command.trim());
                } else {
                    Map<String, Object> validatedParams = validateParameterMap(params);
                    result = tx.run(command.trim(), validatedParams);
                }

                String summaryString = getResultSummaryString(result.consume(), command);
                logger.debug(summaryString);
            }

            tx.commit();
            successfulQueries++;

        } catch (Exception e) {
            failedQueries++;
            logger.error("Update query failed for '{}': {}", settingsRecord.getName(), e.getMessage());
            throw new RuntimeException("Query execution failed: " + e.getMessage(), e);
        }
    }

    /**
     * Execute an update query without parameters
     */
    public void updateQuery(final String query) {
        updateQuery(query, null);
    }

    /**
     * Execute a select query and return results
     */
    public Object selectQuery(final String query, @Nullable Map<String, Object> params) {
        if (!ensureConnected()) {
            throw new RuntimeException("Database connector is not available for " + settingsRecord.getName());
        }

        List<Map<String, Object>> resultList = new ArrayList<>();

        try (Session session = driver.session(sessionConfig)) {
            session.executeRead(tx -> {
                Result result;

                if (params == null) {
                    result = tx.run(query);
                } else {
                    Map<String, Object> validatedParams = validateParameterMap(params);
                    result = tx.run(query, validatedParams);
                }

                while (result.hasNext()) {
                    Record record = result.next();
                    resultList.add(record.asMap());
                }

                String summaryString = getResultSummaryString(result.consume(), query);
                logger.debug(summaryString);

                return resultList;
            });

            successfulQueries++;
            return resultList;

        } catch (Exception e) {
            failedQueries++;
            logger.error("Select query failed for '{}': {}", settingsRecord.getName(), e.getMessage());
            throw new RuntimeException("Query execution failed: " + e.getMessage(), e);
        }
    }

    /**
     * Execute a select query without parameters
     */
    public Object selectQuery(final String query) {
        return selectQuery(query, null);
    }

    /**
     * Check if the connection is available and healthy
     */
    public boolean isConnected() {
        if (!isInitialized.get() || driver == null || isClosed.get()) {
            return false;
        }

        try {
            driver.verifyConnectivity();
            return true;
        } catch (Exception e) {
            logger.debug("Connection verification failed for '{}': {}",
                    settingsRecord.getName(), e.getMessage());
            return false;
        }
    }

    /**
     * Ensure connection is available, attempting to reconnect if necessary
     */
    private boolean ensureConnected() {
        if (isClosed.get()) {
            return false;
        }

        if (isConnected()) {
            return true;
        }

        // Try to reconnect
        logger.info("Attempting to reconnect connector '{}'", settingsRecord.getName());
        isInitialized.set(false);
        return initialize();
    }

    /**
     * Get connection pool metrics
     */
    public int getActiveConnections() {
        if (driver == null) {
            return 0;
        }

        try {
            Collection<ConnectionPoolMetrics> connectionMetrics = driver.metrics().connectionPoolMetrics();
            return connectionMetrics.stream()
                    .mapToInt(ConnectionPoolMetrics::inUse)
                    .sum();
        } catch (Exception e) {
            logger.debug("Failed to get connection metrics: {}", e.getMessage());
            return 0;
        }
    }

    /**
     * Get maximum connection pool size
     */
    public int getMaxConnectionPoolSize() {
        return maxConnectionPoolSize;
    }

    /**
     * Get connection statistics
     */
    public String getConnectionStats() {
        return String.format(
                "Attempts: %d, Successful Queries: %d, Failed Queries: %d, Last Error: %s",
                connectionAttempts, successfulQueries, failedQueries,
                lastError != null ? lastError : "None");
    }

    /**
     * Get last error message
     */
    public String getLastError() {
        return lastError;
    }

    /**
     * Check if connector is enabled
     */
    public boolean isEnabled() {
        return enabled;
    }

    @Override
    public void close() throws Exception {
        if (isClosed.compareAndSet(false, true)) {
            logger.info("Closing DatabaseConnector for '{}'", settingsRecord.getName());

            if (driver != null) {
                try {
                    driver.close();
                } catch (Exception e) {
                    logger.warn("Error closing Neo4J driver: {}", e.getMessage());
                } finally {
                    driver = null;
                }
            }

            isInitialized.set(false);
        }
    }

    // Helper methods remain the same...

    private String getResultSummaryString(ResultSummary summary, String query) {
        for (Notification message : summary.notifications()) {
            Gson gson = new Gson();
            logger.debug("Query notification: {}", gson.toJson(message));
        }

        SummaryCounters counters = summary.counters();
        List<String> resultList = new ArrayList<>();

        long duration = summary.resultAvailableAfter(TimeUnit.MILLISECONDS);
        if (duration > this.slowQueryThreshold) {
            logger.warn("Slow Query took {} seconds to execute: {}", (duration / 1000.0), query);
        }

        if (counters.containsUpdates()) {
            int nodesCreated = counters.nodesCreated();
            if (nodesCreated > 0) {
                resultList.add("Created " + nodesCreated + " nodes");
            }

            int nodesDeleted = counters.nodesDeleted();
            if (nodesDeleted > 0) {
                resultList.add("Deleted " + nodesDeleted + " nodes");
            }

            int propertiesSet = counters.propertiesSet();
            if (propertiesSet > 0) {
                resultList.add("Set " + propertiesSet + " properties");
            }

            int relationshipsCreated = counters.relationshipsCreated();
            if (relationshipsCreated > 0) {
                resultList.add("Created " + relationshipsCreated + " relationships");
            }

            int relationshipsDeleted = counters.relationshipsDeleted();
            if (relationshipsDeleted > 0) {
                resultList.add("Deleted " + relationshipsDeleted + " relationships");
            }
        }

        resultList.add("Cypher Query Completed in " + duration + "ms");
        return String.join(", ", resultList);
    }

    private static Object convertValueType(final Object value) {
        if (value instanceof BigDecimal) {
            return ((BigDecimal) value).doubleValue();
        } else if (value instanceof BigInteger) {
            return ((BigInteger) value).longValue();
        } else if (value instanceof java.util.Date) {
            return ((java.util.Date) value).toInstant().atZone(ZoneId.systemDefault());
        } else if (value instanceof Map<?, ?>) {
            Map<?, ?> rawMap = (Map<?, ?>) value;
            Map<String, Object> stringObjectMap = new HashMap<>();
            for (Map.Entry<?, ?> entry : rawMap.entrySet()) {
                if (entry.getKey() instanceof String) {
                    stringObjectMap.put((String) entry.getKey(), entry.getValue());
                }
            }
            return validateParameterMap(stringObjectMap);
        } else if (value instanceof org.python.core.PyList) {
            List<Object> convertedArray = new ArrayList<>();
            for (Object item : (org.python.core.PyList) value) {
                convertedArray.add(convertValueType(item));
            }
            return convertedArray;
        } else {
            return value;
        }
    }

    public static Map<String, Object> validateParameterMap(Map<String, Object> params) {
        Map<String, Object> updatedParams = new HashMap<>();

        try {
            for (Map.Entry<String, Object> entry : params.entrySet()) {
                String key = entry.getKey();
                Object value = entry.getValue();

                if (value == null) {
                    updatedParams.put(key, null);
                    continue;
                }

                if (value instanceof Map<?, ?>) {
                    @SuppressWarnings("unchecked")
                    Map<String, Object> subMap = (Map<String, Object>) value;
                    updatedParams.put(key, validateParameterMap(subMap));
                } else {
                    updatedParams.put(key, convertValueType(value));
                }
            }
            return updatedParams;
        } catch (Exception e) {
            throw new RuntimeException("Parameter validation failed: " + e.getMessage(), e);
        }
    }
}