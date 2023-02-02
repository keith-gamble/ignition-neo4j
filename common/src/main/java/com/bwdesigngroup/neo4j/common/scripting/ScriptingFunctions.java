package com.bwdesigngroup.neo4j.common.scripting;

import java.util.List;

import org.python.core.PyObject;

public interface ScriptingFunctions {

    public void updateQuery(PyObject[] pyArgs, String[] keywords);

    public Object selectQuery(PyObject[] pyArgs, String[] keywords);

    public List<String> getConnections();
}