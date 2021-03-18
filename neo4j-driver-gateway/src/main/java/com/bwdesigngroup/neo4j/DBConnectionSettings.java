package com.bwdesigngroup.neo4j;

import com.inductiveautomation.ignition.gateway.localdb.persistence.PersistentRecord;
import com.inductiveautomation.ignition.gateway.localdb.persistence.RecordMeta;

public class DBConnectionSettings extends PersistentRecord {

    public static final RecordMeta<DBConnectionSettings> META = new RecordMeta<>(DBConnectionSettings.class, "db_connectionsettings");

    @Override
    public RecordMeta<?> getMeta() {
        return META;
    }
}
