package com.bwdesigngroup.neo4j;

import com.bwdesigngroup.neo4j.records.Neo4JSettingsRecord;
import com.bwdesigngroup.neo4j.scripting.AbstractScriptModule;
import com.inductiveautomation.ignition.gateway.model.GatewayContext;

public class GatewayScriptModule extends AbstractScriptModule {
    
    private GatewayContext context;

    public GatewayScriptModule( GatewayContext context )
    {
        this.context = context;
    }

    private Neo4JSettingsRecord getSettingsRecord()
    {
        Neo4JSettingsRecord settingsRecord = context.getLocalPersistenceInterface().find(Neo4JSettingsRecord.META, 0L);
        return settingsRecord;
    }

    @Override
    protected String getDBPathImpl() {
        Neo4JSettingsRecord settingsRecord = getSettingsRecord();
        return settingsRecord.getNeo4JDatabasePath();
    }

    @Override
    protected String getDBUsernameImpl() {
        Neo4JSettingsRecord settingsRecord = getSettingsRecord();
        return settingsRecord.getNeo4JUsername();
    }

    @Override
    protected String getDBPasswordImpl() {
        Neo4JSettingsRecord settingsRecord = getSettingsRecord();
        return settingsRecord.getNeo4JPassword();
    }

}
