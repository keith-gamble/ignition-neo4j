package com.bwdesigngroup.neo4j.scripting;

public class GatewayScriptModule extends AbstractScriptModule {

    @Override
    protected int multiplyImpl(int arg0, int arg1) {
        return arg0 * arg1;
    }
}
