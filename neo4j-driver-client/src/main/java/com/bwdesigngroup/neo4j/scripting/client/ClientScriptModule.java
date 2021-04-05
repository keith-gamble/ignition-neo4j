package com.bwdesigngroup.neo4j.scripting.client;

import java.util.Map;

import com.bwdesigngroup.neo4j.scripting.AbstractScriptModule;
import com.bwdesigngroup.neo4j.scripting.App;
import com.inductiveautomation.ignition.client.gateway_interface.ModuleRPCFactory;

public class ClientScriptModule extends AbstractScriptModule {

    private final App rpc;


    public ClientScriptModule() {
        rpc = ModuleRPCFactory.create(
            "com.bwdesigngroup.neo4j.neo4j-driver",
            App.class
        );
    }

    @Override 
    protected void updateQueryImpl(String connectionName, String query, Map<String, Object> params) {
        rpc.updateQuery(connectionName, query, params);
        return;
    }

    @Override 
    protected Object selectQueryImpl(String connectionName, String query, Map<String, Object> params) {
        return rpc.selectQuery(connectionName, query, params);
    }

    // @Override
    // protected DatabaseConnector getDatabaseConnectorImpl(String connectionName) {
    //     return rpc.getDatabaseConnector(connectionName);
    // }

}

