package com.bwdesigngroup.neo4j.scripting;

import java.util.Map;

import org.python.core.PyObject;

public interface App {

    public void updateQuery(PyObject[] pyArgs, String[] keywords);

    public Object selectQuery(PyObject[] pyArgs, String[] keywords);
}



