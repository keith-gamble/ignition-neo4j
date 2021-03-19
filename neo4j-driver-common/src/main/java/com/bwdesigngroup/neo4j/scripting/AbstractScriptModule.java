package com.bwdesigngroup.neo4j.scripting;


import com.inductiveautomation.ignition.common.BundleUtil;
import com.inductiveautomation.ignition.common.script.hints.ScriptArg;
import com.inductiveautomation.ignition.common.script.hints.ScriptFunction;

import org.jetbrains.annotations.Nullable;

import java.util.Map;

import com.bwdesigngroup.neo4j.driver.*;

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
    public void updateQuery(@ScriptArg("query") String query, @ScriptArg("params") @Nullable Map<String,Object> params) throws Exception {

        String dbPath = getDBPathImpl();
        String dbUser = getDBUsernameImpl();
        String dbPass = getDBPasswordImpl();

        DatabaseConnector connector = new DatabaseConnector(dbPath, dbUser, dbPass);
        
        connector.updateQuery(query, params);
        connector.close();
        return;
    }

    @ScriptFunction(docBundlePrefix = "AbstractScriptModule")
    public void updateQuery(@ScriptArg("query") String query) throws Exception
    {
        updateQuery(query, null);
    }


    @Override
    @ScriptFunction(docBundlePrefix = "AbstractScriptModule")
    public Object selectQuery(@ScriptArg("query") String query, @ScriptArg("params") @Nullable Map<String,Object> params) throws Exception {
        String dbPath = getDBPathImpl();
        String dbUser = getDBUsernameImpl();
        String dbPass = getDBPasswordImpl();
        DatabaseConnector connector = new DatabaseConnector(dbPath, dbUser, dbPass);
        
        Object response = connector.selectQuery(query, params);

        connector.close();
        return response;
    }

    @ScriptFunction(docBundlePrefix = "AbstractScriptModule")
    public Object selectQuery(@ScriptArg("query") String query) throws Exception {
        return selectQuery(query, null);
    }

    public String getDatabasePath()
    {
        return getDBPathImpl();
    }

    public String getDatabaseUsername()
    {
        return getDBUsernameImpl();
    }
    public String getDatabasePassword()
    {
        return getDBPasswordImpl();
    }

    protected abstract String getDBPathImpl();
    protected abstract String getDBUsernameImpl();
    protected abstract String getDBPasswordImpl();

}
