package com.bwdesigngroup.neo4j.scripting.client;

import java.util.Map;

import com.bwdesigngroup.neo4j.scripting.AbstractScriptModule;
import com.bwdesigngroup.neo4j.scripting.App;
import com.inductiveautomation.ignition.client.gateway_interface.ModuleRPCFactory;
import com.inductiveautomation.ignition.common.script.builtin.PyArgumentMap;

import org.python.core.PyObject;

public class ClientScriptModule extends AbstractScriptModule {

    private final App rpc;

    public ClientScriptModule() {
        rpc = ModuleRPCFactory.create(
            "com.bwdesigngroup.neo4j.neo4j-driver",
            App.class
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

}

