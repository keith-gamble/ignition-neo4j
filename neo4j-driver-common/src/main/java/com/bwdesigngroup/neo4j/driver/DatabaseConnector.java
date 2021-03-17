package com.bwdesigngroup.neo4j.driver;

import org.neo4j.driver.AuthTokens;
import org.neo4j.driver.Driver;
import org.neo4j.driver.GraphDatabase;
import org.neo4j.driver.Result;
import org.neo4j.driver.Session;
import org.neo4j.driver.Transaction;
import org.neo4j.driver.TransactionWork;


public class DatabaseConnector implements AutoCloseable
{
    String dbPath = "bolt://3.239.219.86:7687";
    String dbUser = "neo4j";
    String dbPass = "fastener-ponds-tissue";

    private final Driver driver;

    public DatabaseConnector()
    {
        driver = GraphDatabase.driver( dbPath, AuthTokens.basic( dbUser, dbPass ) );
    }

    public void updateTransaction( final String cypher) 
    {
        try ( Session session = driver.session() )
        {
            Transaction tx = session.beginTransaction();
            tx.run(cypher);
            tx.commit();
            return;
        }
    }


    public Object selectTransaction( final String cypher) 
    {
        try ( Session session = driver.session() )
        {
            Object response = session.writeTransaction( new TransactionWork<Object>()
            {
                @Override
                public Object execute( Transaction tx )
                {
                    Result result = tx.run( cypher );

                    return result.single().get( 0 ).asString();
                }
            } );
            return response;
        }
    }

    @Override
    public void close() throws Exception
    {
        driver.close();
    }

}