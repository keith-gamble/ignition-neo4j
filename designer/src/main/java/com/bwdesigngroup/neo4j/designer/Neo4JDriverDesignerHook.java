package com.bwdesigngroup.neo4j.designer;

import java.util.List;

import com.bwdesigngroup.neo4j.designer.components.EditorCategory;
import com.bwdesigngroup.neo4j.designer.editors.GeneralPropertyEditor;
import com.bwdesigngroup.neo4j.client.Neo4JClientScriptModule;
import com.bwdesigngroup.neo4j.common.scripting.ScriptingFunctions;

import com.inductiveautomation.ignition.client.gateway_interface.ModuleRPCFactory;
import com.inductiveautomation.ignition.common.BundleUtil;
import com.inductiveautomation.ignition.common.licensing.LicenseState;
import com.inductiveautomation.ignition.common.script.ScriptManager;
import com.inductiveautomation.ignition.common.script.hints.PropertiesFileDocProvider;
import com.inductiveautomation.ignition.designer.model.AbstractDesignerModuleHook;
import com.inductiveautomation.ignition.designer.model.DesignerContext;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This is the Designer-scope module hook.  The minimal implementation contains a startup method.
 */
public class Neo4JDriverDesignerHook extends AbstractDesignerModuleHook {

	private final Logger logger = LoggerFactory.getLogger(getClass());
    private static final Neo4JClientScriptModule scriptModule = new Neo4JClientScriptModule();
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
