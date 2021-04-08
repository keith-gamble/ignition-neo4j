package com.bwdesigngroup.neo4j.scripting;


import com.inductiveautomation.ignition.common.BundleUtil;
import com.inductiveautomation.ignition.common.script.builtin.KeywordArgs;
import com.inductiveautomation.ignition.common.script.hints.NoHint;
import com.inductiveautomation.ignition.common.script.hints.ScriptFunction;

import org.python.core.PyObject;

import java.util.List;
import java.util.Map;

public abstract class ScriptModule implements ScriptingFunctions {

    static {
        BundleUtil.get().addBundle(
            ScriptModule.class.getSimpleName(),
            ScriptModule.class.getClassLoader(),
            ScriptModule.class.getName().replace('.', '/')
        );
    }

    @Override
    @ScriptFunction(docBundlePrefix = "ScriptModule")
    @KeywordArgs(names={"query", "params", "database", "project"}, types={String.class, Map.class, String.class, String.class})
    public void updateQuery(PyObject[] pyArgs, String[] keywords) {
        updateQueryImpl(pyArgs, keywords);
        return;
    }


    @Override
    @ScriptFunction(docBundlePrefix = "ScriptModule")
    @KeywordArgs(names={"query", "params", "database", "project"}, types={String.class, Map.class, String.class, String.class})
    public Object selectQuery(PyObject[] pyArgs, String[] keywords) {
        return selectQueryImpl(pyArgs, keywords);
    }

    @NoHint
    public List<String> getConnections() {
        return getConnectionsList();
    }

    protected abstract void updateQueryImpl(PyObject[] pyArgs, String[] keywords);
    protected abstract Object selectQueryImpl(PyObject[] pyArgs, String[] keywords);
    protected abstract List<String> getConnectionsList();
}
