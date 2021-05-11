package com.bwdesigngroup.neo4j.scripting.client;

import com.inductiveautomation.ignition.client.model.ClientContext;
import com.inductiveautomation.ignition.common.licensing.LicenseState;
import com.inductiveautomation.ignition.common.script.ScriptManager;
import com.inductiveautomation.ignition.common.script.hints.PropertiesFileDocProvider;
import com.inductiveautomation.vision.api.client.AbstractClientModuleHook;

public class ClientHook extends AbstractClientModuleHook {

    @Override
    public void initializeScriptManager(ScriptManager manager) {
        super.initializeScriptManager(manager);

        manager.addScriptModule(
            "system.neo4j",
            new ClientScriptModule(),
            new PropertiesFileDocProvider()
        );
    }

    @Override
    public void startup(ClientContext context, LicenseState activationState) throws Exception {
        super.startup(context, activationState);
        
    }

    @Override
    public void shutdown() {
        super.shutdown();
    }
}
