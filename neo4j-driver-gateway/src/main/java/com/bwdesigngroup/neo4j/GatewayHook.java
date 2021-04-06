package com.bwdesigngroup.neo4j;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.Optional;

import com.bwdesigngroup.neo4j.components.DatabaseConnector;
import com.bwdesigngroup.neo4j.components.DatabaseConnectorStatus;
import com.bwdesigngroup.neo4j.records.BaseRecord;
import com.bwdesigngroup.neo4j.records.RemoteDatabaseRecord;
import com.bwdesigngroup.neo4j.web.Neo4JOverviewContributor;
import com.bwdesigngroup.neo4j.web.Neo4JStatusRoutes;
import com.inductiveautomation.ignition.common.BundleUtil;
import com.inductiveautomation.ignition.common.licensing.LicenseState;
import com.inductiveautomation.ignition.common.script.ScriptManager;
import com.inductiveautomation.ignition.common.script.hints.PropertiesFileDocProvider;
import com.inductiveautomation.ignition.gateway.clientcomm.ClientReqSession;
import com.inductiveautomation.ignition.gateway.dataroutes.RouteGroup;
import com.inductiveautomation.ignition.gateway.localdb.persistence.IRecordListener;
import com.inductiveautomation.ignition.gateway.model.AbstractGatewayModuleHook;
import com.inductiveautomation.ignition.gateway.model.ExtensionPointManager;
import com.inductiveautomation.ignition.gateway.model.ExtensionPointType;
import com.inductiveautomation.ignition.gateway.model.GatewayContext;
import com.inductiveautomation.ignition.gateway.web.components.AbstractNamedTab;
import com.inductiveautomation.ignition.gateway.web.models.ConfigCategory;
import com.inductiveautomation.ignition.gateway.web.models.IConfigTab;
import com.inductiveautomation.ignition.gateway.web.models.INamedTab;
import com.inductiveautomation.ignition.gateway.web.models.KeyValue;
import com.inductiveautomation.ignition.gateway.web.pages.BasicReactPanel;
import com.inductiveautomation.ignition.gateway.web.pages.status.StatusCategories;
import com.inductiveautomation.ignition.gateway.web.pages.status.overviewmeta.OverviewContributor;

import org.apache.wicket.markup.html.WebMarkupContainer;
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
        "Remote", new RemoteDatabaseRecord.RemoteDatabaseType()
    );
    
    // private final Set<AbstractExtensionType> instances = new HashSet<>();
    private final Map<String, DatabaseConnector> connectors = new HashMap<String, DatabaseConnector>();


    /**
     * This sets up the status panel which we'll add to the statusPanels list. The controller will be
     * Neo4JStatusRoutes.java, and the model and view will be in our javascript folder. The status panel is optional
     * Only add if your module will provide meaningful info.
     */
    private static final INamedTab NEO_STATUS_PAGE = new AbstractNamedTab(
            "neo4j",
            StatusCategories.CONNECTIONS,
            "Neo4J.nav.status.header") {

        @Override
        public WebMarkupContainer getPanel(String panelId) {
            // We've set  GatewayHook.getMountPathAlias() to return hce, so we need to use that alias here.
            return new BasicReactPanel(panelId, "/main/res/neo4j/js/neo4jstatus.js", "neo4jstatus");
        }

        @Override
        public Iterable<String> getSearchTerms(){
            return Arrays.asList("neo4j connections", "neo");
        }
    };


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


    // /**
    //  * We'll add an overview contributor. This is optional -- only add if your module will provide meaningful info.
    //  */
    // private final OverviewContributor overviewContributor = new Neo4JOverviewContributor();

    // @Override
    // public Optional<OverviewContributor> getStatusOverviewContributor() {
    //     return Optional.of(overviewContributor);
    // }

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
        return context.getPersistenceInterface().find(RemoteDatabaseRecord.META, SettingsRecord.getId());
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

        // Instantiate the executorService that will update the database statuses
        context.getScheduledExecutorService().scheduleAtFixedRate(new DatabaseConnectorStatus(context, INSTANCE), 30, 10, TimeUnit.SECONDS);
        
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

    /**
     * The following methods are used by the status panel. Only add these if you are providing a status panel.
     */


    // getMountPathAlias() allows us to use a shorter mount path. Use caution, because we don't want a conflict with
    // other modules by other authors.
    @Override
    public Optional<String> getMountPathAlias() {
        return Optional.of("neo4j");
    }

    // Use this whenever you have mounted resources
    @Override
    public Optional<String> getMountedResourceFolder() {
        return Optional.of("mounted");
    }

    // Define your route handlers here
    @Override
    public void mountRouteHandlers(RouteGroup routes) {
        new Neo4JStatusRoutes(context, routes, INSTANCE).mountRoutes();
    }

    @Override
    public List<? extends INamedTab> getStatusPanels() {
        return Collections.singletonList(NEO_STATUS_PAGE);
    }
}
