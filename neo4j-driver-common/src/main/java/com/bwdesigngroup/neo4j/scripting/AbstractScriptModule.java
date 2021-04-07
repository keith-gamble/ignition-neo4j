package com.bwdesigngroup.neo4j.scripting;


import com.inductiveautomation.ignition.common.BundleUtil;
import com.inductiveautomation.ignition.common.script.builtin.KeywordArgs;
import com.inductiveautomation.ignition.common.script.builtin.PyArgumentMap;
import com.inductiveautomation.ignition.common.script.hints.ScriptArg;
import com.inductiveautomation.ignition.common.script.hints.ScriptFunction;

import org.jetbrains.annotations.Nullable;
import org.python.core.PyObject;

import java.util.Map;



public abstract class AbstractScriptModule implements App {

    static {
        BundleUtil.get().addBundle(
            AbstractScriptModule.class.getSimpleName(),
            AbstractScriptModule.class.getClassLoader(),
            AbstractScriptModule.class.getName().replace('.', '/')
        );
    }

    @Override
    @ScriptFunction(docBundlePrefix = "AbstractScriptModule")
    @KeywordArgs(names={"database", "query", "params"}, types={String.class, String.class, Map.class})
    public void updateQuery(PyObject[] pyArgs, String[] keywords) {
        updateQueryImpl(pyArgs, keywords);
        return;
    }


    @Override
    @ScriptFunction(docBundlePrefix = "AbstractScriptModule")
    @KeywordArgs(names={"database", "query", "params"}, types={String.class, String.class, Map.class})
    public Object selectQuery(PyObject[] pyArgs, String[] keywords) {
        return selectQueryImpl(pyArgs, keywords);
    }

    protected abstract void updateQueryImpl(PyObject[] pyArgs, String[] keywords);
    protected abstract Object selectQueryImpl(PyObject[] pyArgs, String[] keywords);
}
