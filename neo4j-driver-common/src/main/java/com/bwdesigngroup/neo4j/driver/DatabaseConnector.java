package com.bwdesigngroup.neo4j.driver;

import java.util.ArrayList;
import java.util.Map;

import org.neo4j.driver.AuthTokens;
import org.neo4j.driver.Driver;
import org.neo4j.driver.GraphDatabase;
import org.neo4j.driver.Record;
import org.neo4j.driver.Result;
import org.neo4j.driver.Session;
import org.neo4j.driver.Transaction;
import org.neo4j.driver.TransactionWork;

import static org.neo4j.driver.Values.parameters;


public class DatabaseConnector implements AutoCloseable
{
    private final Driver driver;

    public DatabaseConnector( String dbPath, String dbUser, String dbPass)
    {
        driver = GraphDatabase.driver( dbPath, AuthTokens.basic( dbUser, dbPass ) );
    }

    public void updateQuery( final String cypher, Map<String, ?> params)
    {
        try ( Session session = driver.session() )
        {
            Transaction tx = session.beginTransaction();
            tx.run(cypher,  parameters( params ));
            tx.commit();
            return;
        }
        
    }


    public void updateTransaction( final String cypher) 
    {
        try ( Session session = driver.session() )
        {
            Transaction tx = session.beginTransaction();
            String[] commands = cypher.split(";");
            for (String command : commands)
            {
                System.out.println(command);
                tx.run(command);
            }
            tx.commit();
            return;
        }
    }

    // TODO: Select as JSON
    public Object selectTransaction( final String cypher) 
    {
        try ( Session session = driver.session() )
        {
            Object response = session.readTransaction( new TransactionWork<Object>()
                {
                    @Override
                    public Object execute( Transaction tx )
                    {
                        Result result = tx.run( cypher );
                        ArrayList<Record> queryResults = new ArrayList<Record>();

                        while (result.hasNext())
                        {
                            Record record = result.next();
                            queryResults.add(record);
                        }

                        return queryResults;
                        // return result.single().get( 0 );
                        // return result.list( r -> r.asMap()).stream();
                    }
                } 
            );
            return response;
        }
    }

    @Override
    public void close() throws Exception
    {
        driver.close();
    }

}