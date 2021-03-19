package com.bwdesigngroup.neo4j.scripting;

import java.util.Map;

public interface App {

    public void updateQuery(String query, Map<String,Object> params) throws Exception;
    public void updateQuery(String query) throws Exception;

    public Object selectQuery(String query, Map<String,Object> params) throws Exception;
    public Object selectQuery(String query) throws Exception;

    public String getDatabasePath();
    public String getDatabaseUsername();
    public String getDatabasePassword();

    

}



