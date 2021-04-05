package com.bwdesigngroup.neo4j.scripting;

import java.util.Map;

public interface App {

    public void updateQuery(String connectionName, String query, Map<String,Object> params);
    public void updateQuery(String connectionName, String query);

    public Object selectQuery(String connectionName, String query, Map<String,Object> params);
    public Object selectQuery(String connectionName, String query);

    // public DatabaseConnector getDatabaseConnector(String connectionName);
}



