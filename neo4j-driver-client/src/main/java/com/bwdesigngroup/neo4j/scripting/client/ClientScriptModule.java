package com.bwdesigngroup.neo4j.scripting.client;

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
    protected String getDBPathImpl(String connectionName) {
        return rpc.getDatabasePath(connectionName);
    }

    @Override
    protected String getDBUsernameImpl(String connectionName) {
        return rpc.getDatabaseUsername(connectionName);
    }

    @Override
    protected String getDBPasswordImpl(String connectionName) {
        return rpc.getDatabasePassword(connectionName);
    }

}

