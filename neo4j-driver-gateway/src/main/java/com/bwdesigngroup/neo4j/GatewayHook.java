package com.bwdesigngroup.neo4j;

import java.sql.SQLException;
import java.util.Collections;
import java.util.List;

import com.bwdesigngroup.neo4j.records.Neo4JSettingsRecord;
import com.bwdesigngroup.neo4j.web.Neo4JSettingsPage;
import com.inductiveautomation.ignition.common.BundleUtil;
import com.inductiveautomation.ignition.common.licensing.LicenseState;
import com.inductiveautomation.ignition.common.script.ScriptManager;
import com.inductiveautomation.ignition.common.script.hints.PropertiesFileDocProvider;
import com.inductiveautomation.ignition.gateway.clientcomm.ClientReqSession;
import com.inductiveautomation.ignition.gateway.localdb.persistence.IRecordListener;
import com.inductiveautomation.ignition.gateway.model.AbstractGatewayModuleHook;
import com.inductiveautomation.ignition.gateway.model.GatewayContext;
import com.inductiveautomation.ignition.gateway.web.models.ConfigCategory;
import com.inductiveautomation.ignition.gateway.web.models.DefaultConfigTab;
import com.inductiveautomation.ignition.gateway.web.models.IConfigTab;
import com.inductiveautomation.ignition.gateway.web.models.KeyValue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GatewayHook extends AbstractGatewayModuleHook {
    private GatewayContext context;

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private GatewayScriptModule scriptModule;

    
    // 
    //  This sets up the config panel
    //  
    public static final ConfigCategory CONFIG_CATEGORY =
        new ConfigCategory("Neo4J", "Neo4J.nav.header", 700);

    @Override
    public List<ConfigCategory> getConfigCategories() {
        return Collections.singletonList(CONFIG_CATEGORY);
    }


    //
    //  An IConfigTab contains all the info necessary to create a link to your config page on the gateway nav menu.
    //  In order to make sure the breadcrumb and navigation works properly, the 'name' field should line up
    //  with the right-hand value returned from {@link ConfigPanel#getMenuLocation}. In this case name("homeconnect")
    //  lines up with HCSettingsPage#getMenuLocation().getRight()
    //  
    public static final IConfigTab NEO4J_CONFIG_ENTRY = DefaultConfigTab.builder()
            .category(CONFIG_CATEGORY)
            .name("neo4j")
            .i18n("Neo4J.nav.settings.title")
            .page(Neo4JSettingsPage.class)
            .terms("Neo4J settings")
            .build();

    @Override
    public List<? extends IConfigTab> getConfigPanels() {
        return Collections.singletonList(
            NEO4J_CONFIG_ENTRY
        );
    }



    @Override
    public void setup(GatewayContext gatewayContext) {
        this.context = gatewayContext;
        scriptModule = new GatewayScriptModule(this.context);

        logger.debug("Beginning setup of Neo4J Module");

        // Register GatewayHook.properties by registering the GatewayHook.class with BundleUtils
        BundleUtil.get().addBundle("Neo4J", getClass(), "Neo4J");

        //Verify tables for persistent records if necessary
        verifySchema(context);

        // create records if needed
        maybeCreateNeo4JSettings(context);

        // listen for updates to the settings record...
        Neo4JSettingsRecord.META.addRecordListener(new IRecordListener<Neo4JSettingsRecord>() {
            @Override
            public void recordUpdated(Neo4JSettingsRecord neo4jSettingsRecord) {
                logger.info("recordUpdated()");
            }

            @Override
            public void recordAdded(Neo4JSettingsRecord neo4jSettingsRecord) {
                logger.info("recordAdded()");
            }

            @Override
            public void recordDeleted(KeyValue keyValue) {
                logger.info("recordDeleted()");
            }
        });


        logger.debug("Setup Complete.");
    }

    private void verifySchema(GatewayContext context) {
        try {
            context.getSchemaUpdater().updatePersistentRecords(Neo4JSettingsRecord.META);
        } catch (SQLException e) {
            logger.error("Error verifying persistent record schemas for Neo4J records.", e);
        }
    }

    public void maybeCreateNeo4JSettings(GatewayContext context) {
        logger.trace("Attempting to create Neo4J Settings Record");
        try {
            Neo4JSettingsRecord settingsRecord = context.getLocalPersistenceInterface().createNew(Neo4JSettingsRecord.META);
            settingsRecord.setId(0L); 
            settingsRecord.setNeo4JDatabasePath("bolt://localhost:7687");
            settingsRecord.setNeo4JUsername("neo4j");
            settingsRecord.setNeo4JPassword("password");

        /*
			 * This doesn't override existing settings, only replaces it with these if we didn't
			 * exist already.
			 */
            context.getSchemaUpdater().ensureRecordExists(settingsRecord);
        } catch (Exception e) {
            logger.error("Failed to establish Neo4JSettings Record exists", e);
        }

        logger.trace("Neo4J Settings Record Established");
    }

    @Override
    public void startup(LicenseState licenseState) {
        logger.info("startup()");
    }

    @Override
    public void shutdown() {
        logger.info("shutdown()");
        BundleUtil.get().removeBundle("Neo4J");
    }

    @Override
    public void initializeScriptManager(ScriptManager manager) {
        super.initializeScriptManager(manager);
        manager.addScriptModule(
                "system.neo4j",
                scriptModule,
                new PropertiesFileDocProvider());
    }

    @Override
    public Object getRPCHandler(ClientReqSession session, String projectName) {
        return scriptModule;
    }
}
