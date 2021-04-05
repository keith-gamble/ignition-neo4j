package com.bwdesigngroup.neo4j;

import java.util.Map;

import com.bwdesigngroup.neo4j.components.DatabaseConnector;
import com.bwdesigngroup.neo4j.scripting.AbstractScriptModule;

import org.jetbrains.annotations.Nullable;

public class GatewayScriptModule extends AbstractScriptModule {
    private GatewayHook INSTANCE;

    public GatewayScriptModule( GatewayHook INSTANCE )
    {
        this.INSTANCE = INSTANCE;
    }

    private DatabaseConnector getConnector(String connectionName) {
        return INSTANCE.getConnector(connectionName);
    }
    
    @Override
    protected void updateQueryImpl(String connectionName, String query, @Nullable Map<String,Object> params) {
        DatabaseConnector connector = getConnector(connectionName);
        connector.updateQuery(query, params);
        return;
    }

    @Override
    protected Object selectQueryImpl(String connectionName, String query, @Nullable Map<String,Object> params) {
        DatabaseConnector connector = getConnector(connectionName);
        return connector.selectQuery(query, params);
    }
}
