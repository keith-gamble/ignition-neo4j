package com.bwdesigngroup.neo4j;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.bwdesigngroup.neo4j.components.DatabaseConnector;
import com.bwdesigngroup.neo4j.records.BaseRecord;
import com.bwdesigngroup.neo4j.records.RemoteDatabaseRecord;

import com.inductiveautomation.ignition.common.BundleUtil;
import com.inductiveautomation.ignition.common.licensing.LicenseState;
import com.inductiveautomation.ignition.common.script.ScriptManager;
import com.inductiveautomation.ignition.common.script.hints.PropertiesFileDocProvider;
import com.inductiveautomation.ignition.gateway.clientcomm.ClientReqSession;
import com.inductiveautomation.ignition.gateway.localdb.persistence.IRecordListener;
import com.inductiveautomation.ignition.gateway.model.AbstractGatewayModuleHook;
import com.inductiveautomation.ignition.gateway.model.ExtensionPointManager;
import com.inductiveautomation.ignition.gateway.model.ExtensionPointType;
import com.inductiveautomation.ignition.gateway.model.GatewayContext;
import com.inductiveautomation.ignition.gateway.web.models.ConfigCategory;
import com.inductiveautomation.ignition.gateway.web.models.IConfigTab;
import com.inductiveautomation.ignition.gateway.web.models.KeyValue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import simpleorm.dataset.SQuery;

import static com.bwdesigngroup.neo4j.web.Neo4JExtensionPage.CONFIG_CATEGORY;
import static com.bwdesigngroup.neo4j.web.Neo4JExtensionPage.CONFIG_ENTRY;

public class GatewayHook extends AbstractGatewayModuleHook implements ExtensionPointManager  {
    private GatewayContext context;
    public static GatewayHook INSTANCE = null;

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private GatewayScriptModule scriptModule;

    private final Map<String, AbstractExtensionType> extensionPoints = Map.of(
        "remote", new RemoteDatabaseRecord.RemoteDatabaseType()
    );
    
    // private final Set<AbstractExtensionType> instances = new HashSet<>();
    private final Map<String, DatabaseConnector> connectors = new HashMap<String, DatabaseConnector>();


    // 
    //  This sets up the config panel
    //  
    
    @Override
    public List<ConfigCategory> getConfigCategories() {
        return List.of(CONFIG_CATEGORY);
    }

    @Override
    public List<? extends IConfigTab> getConfigPanels() {
        return List.of(CONFIG_ENTRY);
    }

    @Override
    public ExtensionPointType getExtensionPoint(String s) {
        return extensionPoints.get(s);
    }

    @Override
    public List<? extends ExtensionPointType> getExtensionPoints() {
        return new ArrayList<>(extensionPoints.values());
    }



    @Override
    public void setup(GatewayContext gatewayContext) {
        this.context = gatewayContext;
        INSTANCE = this;
        scriptModule = new GatewayScriptModule(INSTANCE);

        logger.debug("Beginning setup of Neo4J Module");

        // Register GatewayHook.properties by registering the GatewayHook.class with BundleUtils
        BundleUtil.get().addBundle("Neo4J", getClass(), "Neo4J");

        //Verify tables for persistent records if necessary
        verifySchema(context);

        // listen for updates to the settings record...
        BaseRecord.META.addRecordListener(new IRecordListener<BaseRecord>() {
            @Override
            public void recordUpdated(BaseRecord SettingsRecord) {
                RemoteDatabaseRecord rdr = getDatabaseRecord(SettingsRecord);
                DatabaseConnector dbConnector = new DatabaseConnector(rdr.getUrl(), rdr.getUsername(), rdr.getPassword());
                connectors.put(SettingsRecord.getName(), dbConnector);
                logger.info("recordUpdated()");
            }

            @Override
            public void recordAdded(BaseRecord jSettingsRecord) {
                RemoteDatabaseRecord rdr = getDatabaseRecord(jSettingsRecord);
                DatabaseConnector dbConnector = new DatabaseConnector(rdr.getUrl(), rdr.getUsername(), rdr.getPassword());
                connectors.put(jSettingsRecord.getName(), dbConnector);
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
            context.getSchemaUpdater().updatePersistentRecords(BaseRecord.META, RemoteDatabaseRecord.META);
        } catch (SQLException e) {
            logger.error("Error verifying persistent record schemas for Neo4J records.", e);
        }
    }

    private RemoteDatabaseRecord getDatabaseRecord(BaseRecord SettingsRecord) {
        return context.getPersistenceInterface().find(RemoteDatabaseRecord.META, SettingsRecord.getLong(BaseRecord.Id));
    }

    @Override
    public void startup(LicenseState licenseState) {
        logger.info("startup()");
        List<BaseRecord> baseRecords = context.getPersistenceInterface().query(new SQuery<>(BaseRecord.META));
        for (BaseRecord record : baseRecords) {
            RemoteDatabaseRecord rdr = getDatabaseRecord(record);
            DatabaseConnector dbConnector = new DatabaseConnector(rdr.getUrl(), rdr.getUsername(), rdr.getPassword());
            connectors.put(record.getName(), dbConnector);
        }
    }

    public DatabaseConnector getConnector(String connectorName) {
        return connectors.getOrDefault(connectorName, null);
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

    @Override
    public boolean isFreeModule() {
        return true;
    }
}
