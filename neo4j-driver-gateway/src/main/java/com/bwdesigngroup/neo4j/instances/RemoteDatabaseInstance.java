package com.bwdesigngroup.neo4j.instances;

import com.bwdesigngroup.neo4j.records.BaseRecord;
import com.bwdesigngroup.neo4j.records.RemoteDatabaseRecord;

public class RemoteDatabaseInstance extends Extendable {
    private final String name;
    private final Boolean enabled;
    private final String url;
    private final String username;
    private final String password;

    public RemoteDatabaseInstance(BaseRecord baseRecord, RemoteDatabaseRecord childRecord) {
        this.name = baseRecord.getString(BaseRecord.NAME);
        this.enabled = baseRecord.getBoolean(BaseRecord.ENABLED);
        this.url = childRecord.getString(RemoteDatabaseRecord.CONNECTURL);
        this.username = childRecord.getString(RemoteDatabaseRecord.USERNAME);
        this.password = childRecord.getString(RemoteDatabaseRecord.PASSWORD);
    }
}
