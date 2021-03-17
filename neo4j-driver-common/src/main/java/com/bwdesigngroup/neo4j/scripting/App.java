package com.bwdesigngroup.neo4j.scripting;


public interface App {
    
    public void cypherUpdate(String cypher) throws Exception;

    public String cypherSelect(String cypher) throws Exception;
}



