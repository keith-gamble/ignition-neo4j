package com.bwdesigngroup.neo4j.scripting;


import com.inductiveautomation.ignition.common.BundleUtil;
import com.inductiveautomation.ignition.common.script.hints.ScriptArg;
import com.inductiveautomation.ignition.common.script.hints.ScriptFunction;

import com.bwdesigngroup.neo4j.driver.*;

public abstract class AbstractScriptModule implements App {

    static {
        BundleUtil.get().addBundle(
            AbstractScriptModule.class.getSimpleName(),
            AbstractScriptModule.class.getClassLoader(),
            AbstractScriptModule.class.getName().replace('.', '/')
        );
    }

    // @Override
    @ScriptFunction(docBundlePrefix = "AbstractScriptModule")
    public void cypherUpdate(@ScriptArg("cypher") String cypher) throws Exception {

        String dbPath = getDBPathImpl();
        String dbUser = getDBUsernameImpl();
        String dbPass = getDBPasswordImpl();

        System.out.println("DB Path is " + dbPath);
        DatabaseConnector connector = new DatabaseConnector(dbPath, dbUser, dbPass);
        
        connector.updateTransaction(cypher);
        connector.close();
        return;
    }

    // @Override
    @ScriptFunction(docBundlePrefix = "AbstractScriptModule")
    public Object cypherSelect(@ScriptArg("cypher") String cypher) throws Exception {
        String dbPath = getDBPathImpl();
        String dbUser = getDBUsernameImpl();
        String dbPass = getDBPasswordImpl();
        DatabaseConnector connector = new DatabaseConnector(dbPath, dbUser, dbPass);
        
        Object response = connector.selectTransaction(cypher);

        connector.close();
        return response;
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
