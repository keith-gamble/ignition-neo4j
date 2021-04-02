package com.bwdesigngroup.neo4j;

import java.util.List;

import com.bwdesigngroup.neo4j.instances.Extendable;
import com.bwdesigngroup.neo4j.instances.RemoteDatabaseInstance;
import com.bwdesigngroup.neo4j.records.BaseRecord;
import com.bwdesigngroup.neo4j.records.RemoteDatabaseRecord;
import com.bwdesigngroup.neo4j.scripting.AbstractScriptModule;
import com.inductiveautomation.ignition.gateway.model.GatewayContext;

import simpleorm.dataset.SQuery;

public class GatewayScriptModule extends AbstractScriptModule {
    
    private GatewayContext context;

    public GatewayScriptModule( GatewayContext context )
    {
        this.context = context;
    }

    private BaseRecord getSettingsRecord(String connectionName) {
        List<BaseRecord> baseRecords = context.getPersistenceInterface().query(new SQuery<>(BaseRecord.META));
        for (BaseRecord record : baseRecords) {
            if (record.getName() == connectionName) {
                return record;
            }
        }

        return null;
    }

    @Override
    protected String getDBPathImpl() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    protected String getDBUsernameImpl() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    protected String getDBPasswordImpl() {
        // TODO Auto-generated method stub
        return null;
    }

    // @Override
    // protected String getDBPathImpl(String connectionName) {
    //     BaseRecord settingsRecord = getSettingsRecord(connectionName);

    //     if (settingsRecord instanceof RemoteDatabaseRecord) {
    //         return ((RemoteDatabaseRecord) settingsRecord).getUrl();
    //     }

    //     return null;
    // }

    // @Override
    // protected String getDBUsernameImpl(String connectionName) {
    //     BaseRecord settingsRecord = getSettingsRecord(connectionName);
    //     return settingsRecord.getNeo4JUsername();
    // }

    // @Override
    // protected String getDBPasswordImpl(String connectionName) {
    //     BaseRecord settingsRecord = getSettingsRecord(connectionName);
    //     return settingsRecord.getNeo4JPassword();
    // }

}
