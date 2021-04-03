package com.bwdesigngroup.neo4j.scripting;


import com.inductiveautomation.ignition.common.BundleUtil;
import com.inductiveautomation.ignition.common.script.hints.ScriptArg;
import com.inductiveautomation.ignition.common.script.hints.ScriptFunction;

import org.jetbrains.annotations.Nullable;

import java.util.Map;

import com.bwdesigngroup.neo4j.driver.*;

public abstract class AbstractScriptModule implements App {

    private DatabaseConnector connector;

    static {
        BundleUtil.get().addBundle(
            AbstractScriptModule.class.getSimpleName(),
            AbstractScriptModule.class.getClassLoader(),
            AbstractScriptModule.class.getName().replace('.', '/')
        );
    }

    private void verifyConnector(String connectionName) {
        if (connector != null) {
            return;
        } else {
            String dbPath = getDBPathImpl(connectionName);
            String dbUser = getDBUsernameImpl(connectionName);
            String dbPass = getDBPasswordImpl(connectionName);
            connector = new DatabaseConnector(dbPath, dbUser, dbPass);
        }
    }

    @Override
    @ScriptFunction(docBundlePrefix = "AbstractScriptModule")
    public void updateQuery(@ScriptArg("connection") String connectionName, @ScriptArg("query") String query, @ScriptArg("params") @Nullable Map<String,Object> params) throws Exception {
        verifyConnector(connectionName);
        connector.updateQuery(query, params);
        return;
    }

    @ScriptFunction(docBundlePrefix = "AbstractScriptModule")
    public void updateQuery(@ScriptArg("connection") String connectionName, @ScriptArg("query") String query) throws Exception
    {
        updateQuery(connectionName, query, null);
    }


    @Override
    @ScriptFunction(docBundlePrefix = "AbstractScriptModule")
    public Object selectQuery(@ScriptArg("connection") String connectionName, @ScriptArg("query") String query, @ScriptArg("params") @Nullable Map<String,Object> params) throws Exception {
        verifyConnector(connectionName);

        Object response = connector.selectQuery(query, params);

        return response;
    }

    @ScriptFunction(docBundlePrefix = "AbstractScriptModule")
    public Object selectQuery(@ScriptArg("connection") String connectionName, @ScriptArg("query") String query) throws Exception {
        return selectQuery(connectionName, query, null);
    }

    public String getDatabasePath(String connectionName)
    {
        return getDBPathImpl(connectionName);
    }

    public String getDatabaseUsername(String connectionName)
    {
        return getDBUsernameImpl(connectionName);
    }
    public String getDatabasePassword(String connectionName)
    {
        return getDBPasswordImpl(connectionName);
    }

    protected abstract String getDBPathImpl(String connectionName);
    protected abstract String getDBUsernameImpl(String connectionName);
    protected abstract String getDBPasswordImpl(String connectionName);

}
