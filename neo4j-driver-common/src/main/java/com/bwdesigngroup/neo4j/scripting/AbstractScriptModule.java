package com.bwdesigngroup.neo4j.scripting;


import com.inductiveautomation.ignition.common.BundleUtil;
import com.inductiveautomation.ignition.common.script.hints.ScriptArg;
import com.inductiveautomation.ignition.common.script.hints.ScriptFunction;

import org.jetbrains.annotations.Nullable;

import java.util.Map;



public abstract class AbstractScriptModule implements App {

    static {
        BundleUtil.get().addBundle(
            AbstractScriptModule.class.getSimpleName(),
            AbstractScriptModule.class.getClassLoader(),
            AbstractScriptModule.class.getName().replace('.', '/')
        );
    }

    @Override
    @ScriptFunction(docBundlePrefix = "AbstractScriptModule")
    public void updateQuery(@ScriptArg("connection") String connectionName, @ScriptArg("query") String query, @ScriptArg("params") @Nullable Map<String,Object> params) {
        // DatabaseConnector connector = getDatabaseConnector(connectionName);
        // connector.updateQuery(query, params);
        updateQueryImpl(connectionName, query, params);
        return;
    }

    @ScriptFunction(docBundlePrefix = "AbstractScriptModule")
    public void updateQuery(@ScriptArg("connection") String connectionName, @ScriptArg("query") String query)
    {
        updateQuery(connectionName, query, null);
    }


    @Override
    @ScriptFunction(docBundlePrefix = "AbstractScriptModule")
    public Object selectQuery(@ScriptArg("connection") String connectionName, @ScriptArg("query") String query, @ScriptArg("params") @Nullable Map<String,Object> params) {
        // DatabaseConnector connector = getDatabaseConnector(connectionName);

        // Object response = connector.selectQuery(query, params);
        Object response = selectQueryImpl(connectionName, query, params);
        return response;
    }

    @ScriptFunction(docBundlePrefix = "AbstractScriptModule")
    public Object selectQuery(@ScriptArg("connection") String connectionName, @ScriptArg("query") String query) {
        return selectQuery(connectionName, query, null);
    }

    // public DatabaseConnector getDatabaseConnector(String connectionName) {
    //     return getDatabaseConnectorImpl(connectionName);
    // }

    protected abstract void updateQueryImpl(String connectionName, String query, Map<String,Object> params);
    protected abstract Object selectQueryImpl(String connectionName, String query, Map<String,Object> params);
    // protected abstract DatabaseConnector getDatabaseConnectorImpl(String connectionName);
}
