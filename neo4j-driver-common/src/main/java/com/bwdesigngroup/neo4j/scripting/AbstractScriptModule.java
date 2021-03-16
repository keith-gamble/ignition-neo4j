package com.bwdesigngroup.neo4j.scripting;

import com.inductiveautomation.ignition.common.BundleUtil;
import com.inductiveautomation.ignition.common.script.hints.ScriptArg;
import com.inductiveautomation.ignition.common.script.hints.ScriptFunction;

import org.neo4j.driver.*;

import static org.neo4j.driver.Values.parameters;

public abstract class AbstractScriptModule implements App {

    static {
        BundleUtil.get().addBundle(AbstractScriptModule.class.getSimpleName(),
                AbstractScriptModule.class.getClassLoader(), AbstractScriptModule.class.getName().replace('.', '/'));
    }

    @Override
    @ScriptFunction(docBundlePrefix = "AbstractScriptModule")
    public int multiply(@ScriptArg("arg0") int arg0, @ScriptArg("arg1") int arg1) {

        return multiplyImpl(arg0, arg1);
    }

    public String helloNode() {
        Driver driver = GraphDatabase.driver("bolt://3.239.219.86:7687", AuthTokens.basic("neo4j", "fastener-ponds-tissue"));
        
        String returnStr;
        try ( Session session = driver.session() )
        {
            String greeting = session.writeTransaction( new TransactionWork<String>()
            {
                @Override
                public String execute( Transaction tx )
                {
                    Result result = tx.run( "CREATE (a:Greeting) " +
                                                     "SET a.message = $message " +
                                                     "RETURN a.message + ', from node ' + id(a)",
                            parameters( "message", "hello, world" ) );
                    return result.single().get( 0 ).asString();
                }
            } );
            
            returnStr = greeting;
        }
        
        driver.close();

        return returnStr;
        
    }

    protected abstract int multiplyImpl(int arg0, int arg1);

}
