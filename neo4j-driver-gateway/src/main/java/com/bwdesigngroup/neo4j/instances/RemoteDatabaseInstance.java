package com.bwdesigngroup.neo4j.instances;

import com.bwdesigngroup.neo4j.records.DatabaseRecord;
import com.bwdesigngroup.neo4j.records.RemoteDatabaseRecord;

public class RemoteDatabaseInstance extends Extendable {
    private final String name;
    private final Boolean enabled;
    private final String url;
    private final String username;
    private final String password;

    public RemoteDatabaseInstance(DatabaseRecord baseRecord, RemoteDatabaseRecord childRecord) {
        this.name = baseRecord.getString(DatabaseRecord.NAME);
        this.enabled = baseRecord.getBoolean(DatabaseRecord.ENABLED);
        this.url = childRecord.getString(RemoteDatabaseRecord.CONNECTURL);
        this.username = childRecord.getString(RemoteDatabaseRecord.USERNAME);
        this.password = childRecord.getString(RemoteDatabaseRecord.PASSWORD);
    }
}
