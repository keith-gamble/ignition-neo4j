package dev.bwdesigngroup.neo4j.gateway.instances;

import dev.bwdesigngroup.neo4j.gateway.Neo4JDriverGatewayHook;
import dev.bwdesigngroup.neo4j.gateway.components.Neo4JConnectionManager;
import dev.bwdesigngroup.neo4j.gateway.records.DatabaseRecord;
import dev.bwdesigngroup.neo4j.gateway.records.RemoteDatabaseRecord;

public class RemoteDatabaseInstance extends Extendable {
    private final String name;
    private final Boolean enabled;
    private final String url;
    private final String username;
    private final String password;

    public String getName() {
        return name;
    }

    public Boolean getEnabled() {
        return enabled;
    }

    public String getUrl() {
        return url;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    /**
     * Get exception information from the connection manager instead of the record
     */
    public String getException() {
        try {
            Neo4JConnectionManager.ConnectionPool connectionStatus = Neo4JDriverGatewayHook
                    .getConnectionStatus(this.name);

            if (connectionStatus != null) {
                return connectionStatus.getLastError();
            }
            return null;
        } catch (Exception e) {
            return "Error getting connection status: " + e.getMessage();
        }
    }

    /**
     * Get connection state for status reporting
     */
    public Neo4JConnectionManager.ConnectionState getConnectionState() {
        try {
            Neo4JConnectionManager.ConnectionPool connectionStatus = Neo4JDriverGatewayHook
                    .getConnectionStatus(this.name);

            if (connectionStatus != null) {
                return connectionStatus.getState();
            }
            return Neo4JConnectionManager.ConnectionState.DISCONNECTED;
        } catch (Exception e) {
            return Neo4JConnectionManager.ConnectionState.FAILED;
        }
    }

    public RemoteDatabaseInstance(DatabaseRecord baseRecord, RemoteDatabaseRecord childRecord) {
        this.name = baseRecord.getString(DatabaseRecord.NAME);
        this.enabled = baseRecord.getBoolean(DatabaseRecord.ENABLED);
        this.url = childRecord.getString(RemoteDatabaseRecord.CONNECTURL);
        this.username = childRecord.getString(RemoteDatabaseRecord.USERNAME);
        this.password = childRecord.getString(RemoteDatabaseRecord.PASSWORD);
    }
}