package com.bwdesigngroup.neo4j;

import java.util.List;
import java.util.Map;

import com.bwdesigngroup.neo4j.components.DatabaseConnector;
import com.bwdesigngroup.neo4j.scripting.ScriptModule;
import com.inductiveautomation.ignition.common.script.builtin.PyArgumentMap;
import com.inductiveautomation.ignition.common.script.hints.NoHint;

import org.python.core.PyObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GatewayScriptModule extends ScriptModule {

    private final Logger logger = LoggerFactory.getLogger(getClass());
    private GatewayHook INSTANCE;
    private String DefaultConnector;

    public GatewayScriptModule( GatewayHook INSTANCE )
    {
        this.INSTANCE = INSTANCE;
    }

    public void setDefaultConnector(String ConnectorName) {
        this.DefaultConnector = ConnectorName;
    }

    private String getDefaultConnector() {
        return DefaultConnector;
    }

    private DatabaseConnector getConnector(String connectionName) {
        return INSTANCE.getConnector(connectionName);
    }
    
    public List<String> getConnectionsList() {
        return INSTANCE.getConnectionList();
    }

    @Override
    public void updateQueryImpl(PyObject[] pyArgs, String[] keywords) {
        PyArgumentMap args = PyArgumentMap.interpretPyArgs(pyArgs, keywords, ScriptModule.class, "updateQuery"); 
        String datasourceName = args.getStringArg("database", getDefaultConnector());
        DatabaseConnector connector = getConnector(datasourceName);
        connector.updateQuery(args.getStringArg("query"), (Map<String,Object>) args.getArg("params"));
        return;
    }
    
    @Override
    public Object selectQueryImpl(PyObject[] pyArgs, String[] keywords) {
        PyArgumentMap args = PyArgumentMap.interpretPyArgs(pyArgs, keywords, ScriptModule.class, "updateQuery"); 
        String datasourceName = args.getStringArg("database", getDefaultConnector());
        DatabaseConnector connector = getConnector(datasourceName);
        return connector.selectQuery(args.getStringArg("query"), (Map<String,Object>) args.getArg("params"));

    }

}
