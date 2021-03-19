package com.bwdesigngroup.neo4j;

import com.inductiveautomation.ignition.gateway.localdb.persistence.PersistentRecord;
import com.inductiveautomation.ignition.gateway.localdb.persistence.RecordMeta;

public class Neo4JConnectionSettings extends PersistentRecord {

    public static final RecordMeta<Neo4JConnectionSettings> META = new RecordMeta<>(Neo4JConnectionSettings.class, "neo4j_connectionsettings");

    @Override
    public RecordMeta<?> getMeta() {
        return META;
    }
}
