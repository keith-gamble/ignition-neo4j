package com.bwdesigngroup.neo4j.scripting;


import com.inductiveautomation.ignition.common.BundleUtil;
import com.inductiveautomation.ignition.common.script.hints.ScriptArg;
import com.inductiveautomation.ignition.common.script.hints.ScriptFunction;

import com.bwdesigngroup.neo4j.driver.*;

public abstract class AbstractScriptModule implements App {


    static {
        BundleUtil.get().addBundle(AbstractScriptModule.class.getSimpleName(),
                AbstractScriptModule.class.getClassLoader(), AbstractScriptModule.class.getName().replace('.', '/'));
    }

    @Override
    @ScriptFunction(docBundlePrefix = "AbstractScriptModule")
    public void cypherUpdate(@ScriptArg("cypher") String cypher) throws Exception {
        DatabaseConnector connector = new DatabaseConnector();
        
        connector.updateTransaction(cypher);
        connector.close();
        return;
    }

    @Override
    @ScriptFunction(docBundlePrefix = "AbstractScriptModule")
    public String cypherSelect(@ScriptArg("cypher") String cypher) throws Exception {
        DatabaseConnector connector = new DatabaseConnector();
        
        String response = connector.selectTransaction(cypher);

        connector.close();
        return response;
    }



}
