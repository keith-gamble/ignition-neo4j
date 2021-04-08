package com.bwdesigngroup.neo4j;

import java.util.List;

import com.bwdesigngroup.neo4j.components.EditorCategory;
import com.bwdesigngroup.neo4j.editors.GeneralPropertyEditor;
import com.bwdesigngroup.neo4j.scripting.ScriptingFunctions;
import com.bwdesigngroup.neo4j.scripting.client.ClientScriptModule;
import com.inductiveautomation.ignition.client.gateway_interface.ModuleRPCFactory;
import com.inductiveautomation.ignition.common.BundleUtil;
import com.inductiveautomation.ignition.common.licensing.LicenseState;
import com.inductiveautomation.ignition.common.script.ScriptManager;
import com.inductiveautomation.ignition.common.script.hints.PropertiesFileDocProvider;
import com.inductiveautomation.ignition.designer.model.AbstractDesignerModuleHook;
import com.inductiveautomation.ignition.designer.model.DesignerContext;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DesignerHook extends AbstractDesignerModuleHook {

    private final Logger logger = LoggerFactory.getLogger(getClass());
    private static final ClientScriptModule scriptModule = new ClientScriptModule();
    private static final ScriptingFunctions rpc = ModuleRPCFactory.create("com.bwdesigngroup.neo4j.neo4j-driver", ScriptingFunctions.class);
    private DesignerContext context;

    @Override
    public void startup(DesignerContext context, LicenseState activationState) {
        this.context = context;
        BundleUtil.get().addBundle("GeneralPropertyEditor", GeneralPropertyEditor.class, "GeneralPropertyEditor");
        init();
    }

    public void init() {
        context.addPropertyEditor(EditorCategory.class);
        context.addPropertyEditor(GeneralPropertyEditor.class);
    }

   

    @Override
    public void shutdown() {

    }

    public static List<String> getConnectionsList() {
        return rpc.getConnections();
    }

    @Override
    public void initializeScriptManager(ScriptManager manager) {
        super.initializeScriptManager(manager);

        manager.addScriptModule(
            "system.neo4j",
            scriptModule,
            new PropertiesFileDocProvider()
        );
    }

}
