package com.bwdesigngroup.neo4j.scripting.client;

import java.util.List;
import java.util.Map;

import com.bwdesigngroup.neo4j.scripting.ScriptModule;
import com.bwdesigngroup.neo4j.scripting.ScriptingFunctions;
import com.inductiveautomation.ignition.client.gateway_interface.ModuleRPCFactory;
import com.inductiveautomation.ignition.common.script.builtin.PyArgumentMap;
import com.inductiveautomation.ignition.common.script.hints.NoHint;

import org.python.core.PyObject;

public class ClientScriptModule extends ScriptModule {

    private final ScriptingFunctions rpc;

    public ClientScriptModule() {
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

