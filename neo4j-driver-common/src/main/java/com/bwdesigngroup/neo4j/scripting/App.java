package com.bwdesigngroup.neo4j.scripting;


public interface App {

    public void cypherUpdate(String cypher) throws Exception;

    public Object cypherSelect(String cypher) throws Exception;
}



