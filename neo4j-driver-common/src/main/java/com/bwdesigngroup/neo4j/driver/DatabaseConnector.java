package com.bwdesigngroup.neo4j.driver;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.jetbrains.annotations.Nullable;
import org.neo4j.driver.AuthTokens;
import org.neo4j.driver.Driver;
import org.neo4j.driver.GraphDatabase;
import org.neo4j.driver.Result;
import org.neo4j.driver.Session;
import org.neo4j.driver.Transaction;
import org.neo4j.driver.TransactionWork;
import org.neo4j.driver.summary.ResultSummary;
import org.neo4j.driver.summary.SummaryCounters;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DatabaseConnector implements AutoCloseable
{
    private final Driver driver;

    private final Logger logger = LoggerFactory.getLogger(getClass());

    public DatabaseConnector(String dbPath, String dbUser, String dbPass)
    {
        driver = GraphDatabase.driver( dbPath, AuthTokens.basic( dbUser, dbPass ) );
    }

    public void updateQuery( final String query, @Nullable Map<String,Object> params)
    {
        try ( Session session = driver.session() )
        {
            Transaction tx = session.beginTransaction();
            String[] commands = query.split(";");
            for (String command : commands)
            {
                Result result;

                if (params == null) {
                    result = tx.run(command);
                } else {
                    result = tx.run(command, params);
                }
                
                String summaryString = getResultSummaryString(result.consume());
                
                System.out.println(summaryString);

            }
            tx.commit();
            return;
        }   
    }

    public void updateQuery( final String query)
    {
        updateQuery(query, null);
    }


    // TODO: Select as JSON
    public Object selectQuery( final String query, @Nullable Map<String,Object> params) 
    {
        try ( Session session = driver.session() )
        {
            Object response = session.readTransaction( new TransactionWork<Object>()
                {
                    @Override
                    public Object execute( Transaction tx )
                    {
                        Result results;
                        
                        if (params == null) {
                            results = tx.run(query);
                        } else {
                            results = tx.run(query, params);
                        }

                        return results;
                    }
                } 
            );
            return response;
        }
    }

    public Object selectQuery( final String query)
    {
        return selectQuery(query, null);
    }

    private String getResultSummaryString( ResultSummary summary) {

        SummaryCounters counters = summary.counters();
                
        List<String> resultList = new ArrayList<String>();

        long duration = summary.resultAvailableAfter(TimeUnit.MILLISECONDS);

        if ( counters.containsUpdates() ) {
            
            int nodesCreated = counters.nodesCreated();     
            if ( nodesCreated > 0 ) {
                resultList.add( "Created " + nodesCreated + " nodes" );
            }
            
            int nodesDeleted = counters.nodesDeleted();
            if ( nodesDeleted > 0 ) {
                resultList.add( "Deleted " + nodesDeleted + " nodes" );
            }

            int propertiesSet = counters.propertiesSet();

            if ( propertiesSet > 0 ) {
                resultList.add( "Set " + propertiesSet + " properties" );
            }

            int relationshipsCreated = counters.relationshipsCreated();
            if ( relationshipsCreated > 0 ) {
                resultList.add( "Created " + relationshipsCreated + " relationships" );
            }

            int relationshipsDeleted = counters.relationshipsDeleted();
            if ( relationshipsDeleted > 0 ) {
                resultList.add( "Deleted " + relationshipsDeleted + " relationships" );
            }
        } 

        resultList.add( "Completed in " + duration + "ms" );

        return String.join(", ", resultList);
    }




    @Override
    public void close() throws Exception
    {
        driver.close();
    }

}