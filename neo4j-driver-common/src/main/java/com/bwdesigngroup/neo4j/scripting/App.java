package com.bwdesigngroup.neo4j.scripting;

import java.util.Map;

public interface App {

    public void cypherUpdate(String cypher, Map<String, ?> params) throws Exception;

    public Object cypherSelect(String cypher) throws Exception;

    public String getDatabasePath();
    public String getDatabaseUsername();
    public String getDatabasePassword();

    

}



