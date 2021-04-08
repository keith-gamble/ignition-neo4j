package com.bwdesigngroup.neo4j;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.bwdesigngroup.neo4j.components.DatabaseConnector;
import com.bwdesigngroup.neo4j.resources.Neo4JProperties;
import com.bwdesigngroup.neo4j.scripting.ScriptModule;
import com.inductiveautomation.ignition.common.project.ProjectInvalidException;
import com.inductiveautomation.ignition.common.script.builtin.PyArgumentMap;

import org.jetbrains.annotations.Nullable;
import org.python.core.PyObject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class GatewayScriptModule extends ScriptModule {

    private final Logger logger = LoggerFactory.getLogger(getClass());
    private Neo4JProperties projectProperties;

    public void setProjectProperties(Neo4JProperties properties) {
        this.projectProperties = properties;
    }

    private String getDefaultConnector(@Nullable String projectName) {
        
        if ( projectProperties != null ) {
            return projectProperties.getDefaultDatabase();
        } else if ( projectName != null ) {
            try {
                Neo4JProperties properties = GatewayHook.getProjectSettings(projectName);
                return properties.getDefaultDatabase();
            } catch (ProjectInvalidException e) { }
        }

        return null;
    }

    public List<String> getConnectionsList() {
        return new ArrayList<String>(GatewayHook.CONNECTORS.keySet());
    }

    private DatabaseConnector getDatabaseConnector(String datasourceName) {
        if(datasourceName == null) {
            throw new RuntimeException("Datasource is null");
        }
        return GatewayHook.getConnector(datasourceName);
    }

    @Override
    public void updateQueryImpl(PyObject[] pyArgs, String[] keywords) {
        PyArgumentMap args = PyArgumentMap.interpretPyArgs(pyArgs, keywords, ScriptModule.class, "updateQuery"); 

        DatabaseConnector connector = getDatabaseConnector(args.getStringArg("database", getDefaultConnector(args.getStringArg("project", null))));
        connector.updateQuery(args.getStringArg("query"), (Map<String,Object>) args.getArg("params"));
        return;
    }
    
    @Override
    public Object selectQueryImpl(PyObject[] pyArgs, String[] keywords) {
        PyArgumentMap args = PyArgumentMap.interpretPyArgs(pyArgs, keywords, ScriptModule.class, "updateQuery"); 
        
        DatabaseConnector connector = getDatabaseConnector(args.getStringArg("database", getDefaultConnector(args.getStringArg("project", null))));
        return connector.selectQuery(args.getStringArg("query"), (Map<String,Object>) args.getArg("params"));

    }

}
