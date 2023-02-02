package com.bwdesigngroup.neo4j.client;

import java.util.List;

import org.python.core.PyObject;

import com.bwdesigngroup.neo4j.common.Neo4JDriverModule;
import com.bwdesigngroup.neo4j.common.scripting.ScriptModule;
import com.bwdesigngroup.neo4j.common.scripting.ScriptingFunctions;
import com.inductiveautomation.ignition.client.gateway_interface.ModuleRPCFactory;
import com.inductiveautomation.ignition.common.script.hints.NoHint;

public class Neo4JClientScriptModule extends ScriptModule {

    private final ScriptingFunctions rpc;

    public Neo4JClientScriptModule() {
        rpc = ModuleRPCFactory.create(
            Neo4JDriverModule.MODULE_ID,
            ScriptingFunctions.class
        );
    }



    @Override 
    protected void updateQueryImpl(PyObject[] pyArgs, String[] keywords) {
        rpc.updateQuery(pyArgs, keywords);
        return;
    }

    @Override 
    protected Object selectQueryImpl(PyObject[] pyArgs, String[] keywords) {
        return rpc.selectQuery(pyArgs, keywords);
    }

    @Override
    @NoHint
    public List<String> getConnectionsList() {
        return rpc.getConnections();
    }

}

