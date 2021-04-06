package com.bwdesigngroup.neo4j.components;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import com.bwdesigngroup.neo4j.records.BaseRecord;
import com.bwdesigngroup.neo4j.records.RemoteDatabaseRecord;
import com.inductiveautomation.ignition.common.gson.Gson;

import org.jetbrains.annotations.Nullable;
import org.neo4j.driver.AuthTokens;
import org.neo4j.driver.Config;
import org.neo4j.driver.ConnectionPoolMetrics;
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
    private int slowQueryThreshold;
    private int maxConnectionPoolSize;

    private final Logger logger = LoggerFactory.getLogger(getClass());

    public DatabaseConnector(BaseRecord SettingsRecord, RemoteDatabaseRecord DatabaseRecord)
    {
        logger.debug("Creating driver connector");
        this.slowQueryThreshold = SettingsRecord.getSlowQueryThreshold();
        this.maxConnectionPoolSize = SettingsRecord.getMaxConnectionPoolSize();

        Config config = Config.builder()
            .withMaxConnectionLifetime( 30, TimeUnit.MINUTES )
            .withMaxConnectionPoolSize( this.maxConnectionPoolSize  )
            .withConnectionAcquisitionTimeout( 30, TimeUnit.MINUTES )
            .withConnectionTimeout(15, TimeUnit.SECONDS)
            .withDriverMetrics()
            .build();

        if (DatabaseRecord.getUsername() != null) {
            driver = GraphDatabase.driver( DatabaseRecord.getUri(), AuthTokens.basic( DatabaseRecord.getUsername(), DatabaseRecord.getPassword() ), config );
        } else {
            driver = GraphDatabase.driver( DatabaseRecord.getUri(), config );
        }
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
                    Map<String, Object> validatedParams = validateParameterMap(params);
                    result = tx.run(command, validatedParams);
                }
                
                String summaryString = getResultSummaryString(result.consume(), query);
                
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
                            Map<String, Object> validatedParams = validateParameterMap(params);
                            result = tx.run(query, validatedParams);
                        }
                        
                        while ( result.hasNext() ) {
                            Record record = result.next();
                            resultList.add(record.asMap());
                        }
                        
                        String summaryString = getResultSummaryString(result.consume(), query);
                
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

    private String getResultSummaryString( ResultSummary summary, String query) {

        for (Notification message : summary.notifications()) {
            
            Gson gson = new Gson();
            System.out.println(gson.toJson(message));
        }

        SummaryCounters counters = summary.counters();
    
        List<String> resultList = new ArrayList<String>();

        long duration = summary.resultAvailableAfter(TimeUnit.MILLISECONDS);
        if ( duration > this.slowQueryThreshold ) {
            logger.warn("Slow Query took " + ( duration / 1000 ) + " seconds to execute: " + query);
        }

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

    private static Object convertValueType(final Object value) {
        if (value instanceof java.math.BigDecimal) {
            return ((BigDecimal) value).doubleValue();
        } else if (value instanceof java.math.BigInteger) {
            return ((BigInteger) value).longValue();
        } else if (value instanceof java.util.Date) {
            return ((java.util.Date) value).toInstant().atZone(ZoneId.systemDefault());
        } else if (value instanceof Map) {
            return validateParameterMap((Map<String, Object>) value);
        } else if (value instanceof org.python.core.PyList) {
            List<Object> convertedArray = new ArrayList<Object>();
            for (Object item : (org.python.core.PyList) value) {
                convertedArray.add(convertValueType(item));
            }
            return convertedArray;
        } else {
            return value;
        }
    }

    public static Map<String, Object> validateParameterMap(Map<String, Object> params) {

        Map<String, Object> updatedParams = new HashMap<String, Object>();
        try {
            for (Map.Entry<String, Object> entry : params.entrySet()) {
                String key = entry.getKey();
                Object value = entry.getValue();

                if (value == null) {
                    updatedParams.put(key,null);
                    continue;
                }
                if (value instanceof Map) {
                    Map<String, Object> subMap = (Map<String, Object>)value;
                    updatedParams.put(key, validateParameterMap(subMap));
                } else {
                    updatedParams.put(key, convertValueType(value));
                }
            }
            return updatedParams;
        } catch(Exception e) {
            e.printStackTrace();
            System.out.println(e);
        }
        return updatedParams;
    }

    public boolean verifyConnectivity() {
        try {
            driver.verifyConnectivity();
        } catch (Exception e){
            logger.error("Database Status faulted: " + e.getMessage());
            return false;
        }

        return true;
    }

    public int getMaxConnectionPoolSize() {
        return this.maxConnectionPoolSize;
    }

    public int getActiveConnections() {
        Collection<ConnectionPoolMetrics> connectionMetrics = driver.metrics().connectionPoolMetrics();

        int activeConnections = 0;
        for (ConnectionPoolMetrics pool : connectionMetrics ) {
            activeConnections += pool.inUse();
        }

        return activeConnections;
    }
    @Override
    public void close() throws Exception
    {
        driver.close();
    }

}