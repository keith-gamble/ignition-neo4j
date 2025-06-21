package dev.bwdesigngroup.neo4j.gateway;

import java.util.List;
import java.util.Map;

import org.jetbrains.annotations.Nullable;
import org.python.core.PyObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import dev.bwdesigngroup.neo4j.common.projectresources.Neo4JProperties;
import dev.bwdesigngroup.neo4j.common.scripting.ScriptModule;
import dev.bwdesigngroup.neo4j.gateway.components.DatabaseConnector;
import com.inductiveautomation.ignition.common.project.ProjectInvalidException;
import com.inductiveautomation.ignition.common.script.builtin.PyArgumentMap;

/**
 * Updated Gateway Script Module that works with the new connection manager
 * Provides improved error handling and connection reliability
 */
public class GatewayScriptModule extends ScriptModule {

    private final Logger logger = LoggerFactory.getLogger(getClass());
    private Neo4JProperties projectProperties;

    public void setProjectProperties(Neo4JProperties properties) {
        this.projectProperties = properties;
    }

    /**
     * Get the default connector name for a project
     */
    private String getDefaultConnector(@Nullable String projectName) {
        // Try project-specific properties first
        if (projectProperties != null) {
            String defaultDb = projectProperties.getDefaultDatabase();
            if (defaultDb != null) {
                return defaultDb;
            }
        }

        // Fallback to system default project
        if (projectName == null) {
            try {
                projectName = Neo4JDriverGatewayHook.getDefaultScriptingProject();
            } catch (IllegalStateException e) {
                logger.warn("No default scripting project configured");
                return null;
            }
        }

        // Try to get project-specific settings
        try {
            Neo4JProperties properties = Neo4JDriverGatewayHook.getProjectSettings(projectName);
            return properties.getDefaultDatabase();
        } catch (ProjectInvalidException e) {
            logger.warn("Invalid project '{}' specified for Neo4J query", projectName);
            return null;
        }
    }

    /**
     * Get list of available connections
     */
    @Override
    public List<String> getConnectionsList() {
        return Neo4JDriverGatewayHook.getConnectionNames();
    }

    /**
     * Get a database connector with improved error handling
     */
    private DatabaseConnector getDatabaseConnector(String datasourceName) {
        if (datasourceName == null) {
            throw new IllegalArgumentException("Datasource name cannot be null. Available connections: " +
                    String.join(", ", getConnectionsList()));
        }

        DatabaseConnector connector = Neo4JDriverGatewayHook.getConnector(datasourceName);
        if (connector == null) {
            throw new IllegalArgumentException(
                    String.format("Datasource '%s' not found. Available connections: %s",
                            datasourceName, String.join(", ", getConnectionsList())));
        }

        return connector;
    }

    /**
     * Execute an update query with improved error handling
     */
    @Override
    public void updateQueryImpl(PyObject[] pyArgs, String[] keywords) {
        PyArgumentMap args = PyArgumentMap.interpretPyArgs(pyArgs, keywords, ScriptModule.class, "updateQuery");

        // Get connector name with fallback logic
        String connectorName = args.getStringArg("database", getDefaultConnector(args.getStringArg("project", null)));

        if (connectorName == null) {
            throw new IllegalArgumentException(
                    "No database specified and no default database configured. " +
                            "Please specify a 'database' parameter or configure a default database in project properties.");
        }

        // Get the connector
        DatabaseConnector connector;
        try {
            connector = getDatabaseConnector(connectorName);
        } catch (IllegalArgumentException e) {
            logger.error("Failed to get database connector: {}", e.getMessage());
            throw new RuntimeException(e.getMessage());
        }

        // Check if connector is enabled
        if (!connector.isEnabled()) {
            String errorMsg = String.format("Database connector '%s' is disabled", connectorName);
            logger.error(errorMsg);
            throw new RuntimeException(errorMsg);
        }

        // Validate and execute query
        String query = args.getStringArg("query");
        if (query == null || query.trim().isEmpty()) {
            throw new IllegalArgumentException("Query parameter is required and cannot be empty");
        }

        // Extract parameters
        Map<String, Object> params = extractParams(args);

        try {
            connector.updateQuery(query, params);
            logger.debug("Successfully executed update query on connector '{}'", connectorName);
        } catch (Exception e) {
            String errorMsg = String.format("Update query failed on connector '%s': %s", connectorName, e.getMessage());
            logger.error(errorMsg, e);
            throw new RuntimeException(errorMsg, e);
        }
    }

    /**
     * Execute a select query with improved error handling
     */
    @Override
    public Object selectQueryImpl(PyObject[] pyArgs, String[] keywords) {
        PyArgumentMap args = PyArgumentMap.interpretPyArgs(pyArgs, keywords, ScriptModule.class, "selectQuery");

        // Get connector name with fallback logic
        String connectorName = args.getStringArg("database", getDefaultConnector(args.getStringArg("project", null)));

        if (connectorName == null) {
            throw new IllegalArgumentException(
                    "No database specified and no default database configured. " +
                            "Please specify a 'database' parameter or configure a default database in project properties.");
        }

        // Get the connector
        DatabaseConnector connector;
        try {
            connector = getDatabaseConnector(connectorName);
        } catch (IllegalArgumentException e) {
            logger.error("Failed to get database connector: {}", e.getMessage());
            throw new RuntimeException(e.getMessage());
        }

        // Check if connector is enabled
        if (!connector.isEnabled()) {
            String errorMsg = String.format("Database connector '%s' is disabled", connectorName);
            logger.error(errorMsg);
            throw new RuntimeException(errorMsg);
        }

        // Validate query
        String query = args.getStringArg("query");
        if (query == null || query.trim().isEmpty()) {
            throw new IllegalArgumentException("Query parameter is required and cannot be empty");
        }

        // Extract parameters
        Map<String, Object> params = extractParams(args);

        try {
            Object result = connector.selectQuery(query, params);
            logger.debug("Successfully executed select query on connector '{}'", connectorName);
            return result;
        } catch (Exception e) {
            String errorMsg = String.format("Select query failed on connector '%s': %s", connectorName, e.getMessage());
            logger.error(errorMsg, e);
            throw new RuntimeException(errorMsg, e);
        }
    }

    /**
     * Extract and validate parameters from arguments
     */
    @SuppressWarnings("unchecked")
    private Map<String, Object> extractParams(PyArgumentMap args) {
        Object paramsObj = args.getArg("params");

        if (paramsObj == null) {
            return null;
        }

        if (paramsObj instanceof Map) {
            try {
                return (Map<String, Object>) paramsObj;
            } catch (ClassCastException e) {
                throw new IllegalArgumentException("Parameters must be a Map<String, Object>");
            }
        }

        throw new IllegalArgumentException("Parameters must be a Map<String, Object>, got: " +
                paramsObj.getClass().getSimpleName());
    }
}