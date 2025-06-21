package dev.bwdesigngroup.neo4j.gateway.records;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import dev.bwdesigngroup.neo4j.gateway.Neo4JDriverGatewayHook;
import dev.bwdesigngroup.neo4j.gateway.components.DatabaseConnector;
import dev.bwdesigngroup.neo4j.gateway.components.Neo4JConnectionManager;
import com.inductiveautomation.ignition.common.BundleUtil;
import com.inductiveautomation.ignition.gateway.localdb.persistence.BooleanField;
import com.inductiveautomation.ignition.gateway.localdb.persistence.Category;
import com.inductiveautomation.ignition.gateway.localdb.persistence.IdentityField;
import com.inductiveautomation.ignition.gateway.localdb.persistence.IntField;
import com.inductiveautomation.ignition.gateway.localdb.persistence.PersistentRecord;
import com.inductiveautomation.ignition.gateway.localdb.persistence.RecordMeta;
import com.inductiveautomation.ignition.gateway.localdb.persistence.StringField;

import simpleorm.dataset.SFieldFlags;

/**
 * Updated DatabaseRecord that works with the new connection manager
 * Provides better status reporting and error handling
 */
public class DatabaseRecord extends PersistentRecord {
    private final Logger logger = LoggerFactory.getLogger(getClass());

    public static final RecordMeta<DatabaseRecord> META = new RecordMeta<>(DatabaseRecord.class, "neo4j_databases")
            .setNounKey("DatabaseRecord.Noun")
            .setNounPluralKey("DatabaseRecord.Noun.Plural");

    // Main properties
    public static final IdentityField ID = new IdentityField(META);
    public static final StringField NAME = new StringField(META, "Name", SFieldFlags.SMANDATORY,
            SFieldFlags.SDESCRIPTIVE);
    public static final StringField TYPE = new StringField(META, "Type", SFieldFlags.SMANDATORY,
            SFieldFlags.SDESCRIPTIVE);
    public static final BooleanField ENABLED = new BooleanField(META, "Enabled", SFieldFlags.SMANDATORY)
            .setDefault(true);

    // Advanced properties
    public static final StringField DATABASE = new StringField(META, "Database");
    public static final IntField SLOW_QUERY_THRESHOLD = new IntField(META, "SlowQueryThreshold", SFieldFlags.SMANDATORY)
            .setDefault(60000);
    public static final IntField VALIDATION_TIMEOUT = new IntField(META, "ValidationTimeout", SFieldFlags.SMANDATORY)
            .setDefault(10000);
    public static final IntField MAX_CONNECTION_POOL_SIZE = new IntField(META, "MaxConnectionPoolSize",
            SFieldFlags.SMANDATORY).setDefault(8);

    // Categories for UI organization
    static final Category Main = new Category("DatabaseRecord.Category.Main", 1000)
            .include(ID, NAME, DATABASE, ENABLED);

    static final Category Advanced = new Category("DatabaseRecord.Category.Advanced", 9000, true)
            .include(SLOW_QUERY_THRESHOLD, VALIDATION_TIMEOUT, MAX_CONNECTION_POOL_SIZE);

    static {
        // Register bundle like the working example
        BundleUtil.get().addBundle("DatabaseRecord", DatabaseRecord.class, "DatabaseRecord");

        // Configure form metadata
        TYPE.getFormMeta().setVisible(false);
        ENABLED.getFormMeta().setFieldDescriptionKey("DatabaseRecord.Enabled.Description");
        DATABASE.getFormMeta().setFieldDescriptionKey("DatabaseRecord.Database.Description");
        SLOW_QUERY_THRESHOLD.getFormMeta().setFieldDescriptionKey("DatabaseRecord.SlowQueryThreshold.Description");
        VALIDATION_TIMEOUT.getFormMeta().setFieldDescriptionKey("DatabaseRecord.ValidationTimeout.Description");
        MAX_CONNECTION_POOL_SIZE.getFormMeta()
                .setFieldDescriptionKey("DatabaseRecord.MaxConnectionPoolSize.Description");
    }

    @Override
    public RecordMeta<?> getMeta() {
        return META;
    }

    // Accessor methods
    public Long getId() {
        return getLong(ID);
    }

    public String getType() {
        return getString(TYPE);
    }

    public boolean getEnabled() {
        return getBoolean(ENABLED);
    }

    public String getName() {
        return getString(NAME);
    }

    public String getDatabase() {
        return getString(DATABASE);
    }

    public int getSlowQueryThreshold() {
        return getInt(SLOW_QUERY_THRESHOLD);
    }

    public int getValidationTimeout() {
        return getInt(VALIDATION_TIMEOUT);
    }

    public int getMaxConnectionPoolSize() {
        return getInt(MAX_CONNECTION_POOL_SIZE);
    }

    /**
     * Get the number of active connections - now uses connection manager
     */
    public int getActiveConnections() {
        if (!this.getEnabled()) {
            return 0;
        }

        try {
            DatabaseConnector connector = Neo4JDriverGatewayHook.getConnector(this.getName());
            if (connector != null) {
                return connector.getActiveConnections();
            }
        } catch (Exception e) {
            logger.debug("Error getting active connections for '{}': {}", this.getName(), e.getMessage());
        }

        return 0;
    }

    /**
     * Get connection status - now uses connection manager for better status
     * reporting
     */
    public String getStatus() {
        try {
            if (!this.getEnabled()) {
                return "Disabled";
            }

            // Get connection status from connection manager
            Neo4JConnectionManager.ConnectionPool connectionStatus = Neo4JDriverGatewayHook
                    .getConnectionStatus(this.getName());

            if (connectionStatus == null) {
                return "Not Configured";
            }

            Neo4JConnectionManager.ConnectionState state = connectionStatus.getState();
            switch (state) {
                case CONNECTED:
                    return "Connected";
                case CONNECTING:
                    return "Connecting";
                case FAILED:
                    String error = connectionStatus.getLastError();
                    return "Failed" + (error != null ? ": " + error : "");
                case DISABLED:
                    return "Disabled";
                case DISCONNECTED:
                default:
                    return "Disconnected";
            }

        } catch (Exception e) {
            logger.error("Error getting status for connection '{}'", this.getName(), e);
            return "Error: " + e.getMessage();
        }
    }

    /**
     * Get detailed connection information for debugging
     */
    public String getConnectionDetails() {
        try {
            Neo4JConnectionManager.ConnectionPool connectionStatus = Neo4JDriverGatewayHook
                    .getConnectionStatus(this.getName());

            if (connectionStatus == null) {
                return "No connection information available";
            }

            StringBuilder details = new StringBuilder();
            details.append("State: ").append(connectionStatus.getState());
            details.append(", URI: ").append(connectionStatus.remoteSettings.getUri());
            details.append(", Last Health Check: ").append(connectionStatus.getLastHealthCheck());

            if (connectionStatus.getLastError() != null) {
                details.append(", Last Error: ").append(connectionStatus.getLastError());
            }

            if (connectionStatus.getRetryAttempts() > 0) {
                details.append(", Retry Attempts: ").append(connectionStatus.getRetryAttempts());
            }

            return details.toString();

        } catch (Exception e) {
            logger.error("Error getting connection details for '{}'", this.getName(), e);
            return "Error getting details: " + e.getMessage();
        }
    }

    /**
     * Get comprehensive diagnostics for troubleshooting
     */
    public String getFullDiagnostics() {
        try {
            // Get diagnostics from connection manager
            Neo4JDriverGatewayHook gatewayHook = Neo4JDriverGatewayHook.getInstance();
            if (gatewayHook != null && gatewayHook.getConnectionManager() != null) {
                return gatewayHook.getConnectionManager().getConnectionDiagnostics(this.getName());
            }
            return "Connection manager not available";
        } catch (Exception e) {
            logger.error("Error getting full diagnostics for '{}'", this.getName(), e);
            return "Error getting diagnostics: " + e.getMessage();
        }
    }

    /**
     * Get health status as a simple boolean
     */
    public boolean isHealthy() {
        if (!this.getEnabled()) {
            return false;
        }

        try {
            Neo4JConnectionManager.ConnectionPool connectionStatus = Neo4JDriverGatewayHook
                    .getConnectionStatus(this.getName());

            return connectionStatus != null &&
                    connectionStatus.getState() == Neo4JConnectionManager.ConnectionState.CONNECTED;
        } catch (Exception e) {
            logger.debug("Error checking health for connection '{}'", this.getName(), e);
            return false;
        }
    }
}