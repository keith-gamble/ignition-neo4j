package com.bwdesigngroup.neo4j.scripting;

import java.util.Map;

public interface App {

    public void updateQuery(String connectionName, String query, Map<String,Object> params) throws Exception;
    public void updateQuery(String connectionName, String query) throws Exception;

    public Object selectQuery(String connectionName, String query, Map<String,Object> params) throws Exception;
    public Object selectQuery(String connectionName, String query) throws Exception;

    public String getDatabasePath(String connectionName);
    public String getDatabaseUsername(String connectionName);
    public String getDatabasePassword(String connectionName);

    

}



