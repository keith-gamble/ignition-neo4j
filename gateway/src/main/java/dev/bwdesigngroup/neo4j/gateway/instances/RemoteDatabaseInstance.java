package dev.bwdesigngroup.neo4j.gateway.instances;

import dev.bwdesigngroup.neo4j.gateway.records.DatabaseRecord;
import dev.bwdesigngroup.neo4j.gateway.records.RemoteDatabaseRecord;

public class RemoteDatabaseInstance extends Extendable {
    private final String name;
    private final Boolean enabled;
    private final String url;
    private final String username;
    private final String password;
    private final Exception exception;

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

    public Exception getException() {
        return exception;
    }

    public RemoteDatabaseInstance(DatabaseRecord baseRecord, RemoteDatabaseRecord childRecord) {
        this.name = baseRecord.getString(DatabaseRecord.NAME);
        this.enabled = baseRecord.getBoolean(DatabaseRecord.ENABLED);
        this.url = childRecord.getString(RemoteDatabaseRecord.CONNECTURL);
        this.username = childRecord.getString(RemoteDatabaseRecord.USERNAME);
        this.password = childRecord.getString(RemoteDatabaseRecord.PASSWORD);
		this.exception = baseRecord.exception;
    }
}
