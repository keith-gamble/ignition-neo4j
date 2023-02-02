package com.bwdesigngroup.neo4j.gateway;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.jetbrains.annotations.Nullable;
import org.python.core.PyObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.bwdesigngroup.neo4j.common.projectresources.Neo4JProperties;
import com.bwdesigngroup.neo4j.common.scripting.ScriptModule;
import com.bwdesigngroup.neo4j.gateway.components.DatabaseConnector;
import com.inductiveautomation.ignition.common.project.ProjectInvalidException;
import com.inductiveautomation.ignition.common.script.builtin.PyArgumentMap;


public class GatewayScriptModule extends ScriptModule {

    private final Logger logger = LoggerFactory.getLogger(getClass());
    private Neo4JProperties projectProperties;

    public void setProjectProperties(Neo4JProperties properties) {
        this.projectProperties = properties;
    }

    private String getDefaultConnector(@Nullable String projectName) {
        
        if ( projectProperties != null ) {
            return projectProperties.getDefaultDatabase();
        } else {
            if ( projectName == null ) {
                projectName = Neo4JDriverGatewayHook.getDefaultScriptingProject();
            }

            try {
                Neo4JProperties properties = Neo4JDriverGatewayHook.getProjectSettings(projectName);
                return properties.getDefaultDatabase();
            } catch (ProjectInvalidException e) { }
        }

        return null;
    }

    public List<String> getConnectionsList() {
        return new ArrayList<String>(Neo4JDriverGatewayHook.CONNECTORS.keySet());
    }

    private DatabaseConnector getDatabaseConnector(String datasourceName) {
        if(datasourceName == null) {
            
            throw new RuntimeException("Datasource is null");
        }
        return Neo4JDriverGatewayHook.getConnector(datasourceName);
    }

    @Override
    public void updateQueryImpl(PyObject[] pyArgs, String[] keywords) {
        PyArgumentMap args = PyArgumentMap.interpretPyArgs(pyArgs, keywords, ScriptModule.class, "updateQuery"); 
		String connectorName = args.getStringArg("database", getDefaultConnector(args.getStringArg("project", null)));
				
        DatabaseConnector connector = getDatabaseConnector(connectorName);
		
		if (connector.Enabled == false) {
			logger.error("Database connector is disabled");
			throw new RuntimeException("Database connector '" + connectorName + "' is disabled");
		}
		
        connector.updateQuery(args.getStringArg("query"), (Map<String,Object>) args.getArg("params"));
        return;
    }
    
    @Override
    public Object selectQueryImpl(PyObject[] pyArgs, String[] keywords) {
        PyArgumentMap args = PyArgumentMap.interpretPyArgs(pyArgs, keywords, ScriptModule.class, "updateQuery"); 
		String connectorName = args.getStringArg("database", getDefaultConnector(args.getStringArg("project", null)));
				
        DatabaseConnector connector = getDatabaseConnector(connectorName);
		
		if (connector.Enabled == false) {
			logger.error("Database connector is disabled");
			throw new RuntimeException("Database connector '" + connectorName + "' is disabled");
		}

        return connector.selectQuery(args.getStringArg("query"), (Map<String,Object>) args.getArg("params"));

    }

}
