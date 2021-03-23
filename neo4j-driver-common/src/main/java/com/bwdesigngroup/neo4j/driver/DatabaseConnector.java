package com.bwdesigngroup.neo4j.driver;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import com.inductiveautomation.ignition.common.gson.Gson;

import org.jetbrains.annotations.Nullable;
import org.neo4j.driver.AuthTokens;
import org.neo4j.driver.Driver;
import org.neo4j.driver.GraphDatabase;
import org.neo4j.driver.Record;
import org.neo4j.driver.Result;
import org.neo4j.driver.Session;
import org.neo4j.driver.Transaction;
import org.neo4j.driver.TransactionWork;
import org.neo4j.driver.summary.Notification;
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
        logger.debug("Creating driver connector");

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
                
                logger.debug(summaryString);
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

    public Object selectQuery( final String query, @Nullable Map<String,Object> params) 
    {
        List<Map<String, Object>> resultList = new ArrayList<Map<String, Object>>();

        try ( Session session = driver.session() )
        {
            session.readTransaction( new TransactionWork<Object>()
                {
                    @Override
                    public Object execute( Transaction tx )
                    {
                        Result result;
                        
                        if (params == null) {
                            result = tx.run(query);
                        } else {
                            result = tx.run(query, params);
                        }
                        
                        while ( result.hasNext() ) {
                            Record record = result.next();
                            resultList.add(record.asMap());
                        }
                        
                        String summaryString = getResultSummaryString(result.consume());
                
                        logger.debug(summaryString);
                        System.out.println(summaryString);

                        return resultList;
                    }
                } 
            );
            return resultList;
        }
    }

    public Object selectQuery( final String query)
    {
        return selectQuery(query, null);
    }

    private String getResultSummaryString( ResultSummary summary) {

        for (Notification message : summary.notifications()) {
            
            Gson gson = new Gson();
            System.out.println(gson.toJson(message));
        }

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

        resultList.add( "Cypher Query Completed in " + duration + "ms" );

        return String.join(", ", resultList);
    }




    @Override
    public void close() throws Exception
    {
        driver.close();
    }

}