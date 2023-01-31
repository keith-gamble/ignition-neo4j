package com.bwdesigngroup.neo4j.client;

import java.util.List;

import com.bwdesigngroup.neo4j.common.scripting.ScriptModule;
import com.bwdesigngroup.neo4j.common.scripting.ScriptingFunctions;
import com.inductiveautomation.ignition.client.gateway_interface.ModuleRPCFactory;
import com.inductiveautomation.ignition.common.script.hints.NoHint;

import org.python.core.PyObject;

public class Neo4JClientScriptModule extends ScriptModule {

    private final ScriptingFunctions rpc;

    public Neo4JClientScriptModule() {
        rpc = ModuleRPCFactory.create(
            "com.bwdesigngroup.neo4j.neo4j-driver",
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

