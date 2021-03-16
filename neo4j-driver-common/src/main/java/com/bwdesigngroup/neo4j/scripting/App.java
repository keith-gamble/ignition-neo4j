package com.bwdesigngroup.neo4j.scripting;

import org.neo4j.driver.AuthTokens;
import org.neo4j.driver.Driver;
import org.neo4j.driver.GraphDatabase;
import org.neo4j.driver.Result;
import org.neo4j.driver.Session;
import org.neo4j.driver.Transaction;
import org.neo4j.driver.TransactionWork;

import static org.neo4j.driver.Values.parameters;

public interface App {

    public int multiply(int arg0, int arg1);

}



// public class App implements AutoCloseable
// {
//     private final Driver driver;

//     public App( String uri, String user, String password )
//     {
//         driver = GraphDatabase.driver( uri, AuthTokens.basic( user, password ) );
//     }

//     @Override
//     public void close() throws Exception
//     {
//         driver.close();
//     }

//     public void printGreeting( final String message )
//     {
//         try ( Session session = driver.session() )
//         {
//             String greeting = session.writeTransaction( new TransactionWork<String>()
//             {
//                 @Override
//                 public String execute( Transaction tx )
//                 {
//                     Result result = tx.run( "CREATE (a:Greeting) " +
//                                                      "SET a.message = $message " +
//                                                      "RETURN a.message + ', from node ' + id(a)",
//                             parameters( "message", message ) );
//                     return result.single().get( 0 ).asString();
//                 }
//             } );
//             System.out.println( greeting );
//         }
//     }

//     public static void main( String... args ) throws Exception
//     {
//         try ( App greeter = new App( "bolt://localhost:7687", "neo4j", "password" ) )
//         {
//             greeter.printGreeting( "hello, world" );
//         }
//     }
// }