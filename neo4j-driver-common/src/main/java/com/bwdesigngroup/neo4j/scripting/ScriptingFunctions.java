package com.bwdesigngroup.neo4j.scripting;

import java.util.List;

import com.inductiveautomation.ignition.common.script.hints.NoHint;

import org.python.core.PyObject;

public interface ScriptingFunctions {

    public void updateQuery(PyObject[] pyArgs, String[] keywords);

    public Object selectQuery(PyObject[] pyArgs, String[] keywords);

    public List<String> getConnections();
}