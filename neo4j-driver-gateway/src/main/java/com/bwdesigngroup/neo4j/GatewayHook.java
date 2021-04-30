package com.bwdesigngroup.neo4j;

import static com.bwdesigngroup.neo4j.web.Neo4JExtensionPage.CONFIG_CATEGORY;
import static com.bwdesigngroup.neo4j.web.Neo4JExtensionPage.CONFIG_ENTRY;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import com.bwdesigngroup.neo4j.components.DatabaseConnector;
import com.bwdesigngroup.neo4j.components.DatabaseConnectorStatus;
import com.bwdesigngroup.neo4j.records.DatabaseRecord;
import com.bwdesigngroup.neo4j.records.RemoteDatabaseRecord;
import com.bwdesigngroup.neo4j.resources.Neo4JProperties;
import com.bwdesigngroup.neo4j.web.Neo4JStatusRoutes;
import com.inductiveautomation.ignition.common.BundleUtil;
import com.inductiveautomation.ignition.common.licensing.LicenseState;
import com.inductiveautomation.ignition.common.project.ProjectInvalidException;
import com.inductiveautomation.ignition.common.project.RuntimeProject;
import com.inductiveautomation.ignition.common.project.resource.ProjectResource;
import com.inductiveautomation.ignition.common.script.ScriptManager;
import com.inductiveautomation.ignition.common.script.hints.PropertiesFileDocProvider;
import com.inductiveautomation.ignition.common.util.ResourceUtil;
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

import org.apache.wicket.markup.html.WebMarkupContainer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import simpleorm.dataset.SQuery;

public class GatewayHook extends AbstractGatewayModuleHook implements ExtensionPointManager  {
    public static GatewayContext context;
    public static GatewayHook INSTANCE;


    private final Logger logger = LoggerFactory.getLogger(getClass());

    private final GatewayScriptModule scriptModule = new GatewayScriptModule();

    private final Map<String, DatabaseRecordType> extensionPoints = Map.of(
        "Remote", new RemoteDatabaseRecord.RemoteDatabaseType()
    );
    
    // private final Set<AbstractExtensionType> instances = new HashSet<>();
    public static final Map<String, DatabaseConnector> CONNECTORS = new HashMap<String, DatabaseConnector>();

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

        // Register GatewayHook.properties by registering the GatewayHook.class with BundleUtils
        BundleUtil.get().addBundle("Neo4J", getClass(), "Neo4J");

        //Verify tables for persistent records if necessary
        verifySchema(context);

        // listen for updates to the settings record...
        DatabaseRecord.META.addRecordListener(new IRecordListener<DatabaseRecord>() {

            private void updateConnector(DatabaseRecord SettingsRecord) {
                RemoteDatabaseRecord remoteRecord = getDatabaseRecord(SettingsRecord);
                DatabaseConnector dbConnector = new DatabaseConnector(SettingsRecord, remoteRecord);
                CONNECTORS.put(SettingsRecord.getName(), dbConnector);
                context.getScheduledExecutorService().scheduleAtFixedRate(new DatabaseConnectorStatus(context, SettingsRecord.getId()), 2, SettingsRecord.getValidationTimeout(), TimeUnit.MILLISECONDS);
            }

            @Override
            public void recordUpdated(DatabaseRecord SettingsRecord) {
                updateConnector(SettingsRecord);
            }

            @Override
            public void recordAdded(DatabaseRecord jSettingsRecord) {
                updateConnector(jSettingsRecord);
            }

            @Override
            public void recordDeleted(KeyValue keyValue) {
                return;
            }
        });

    }

    private void verifySchema(GatewayContext context) {
        try {
            context.getSchemaUpdater().updatePersistentRecords(DatabaseRecord.META, RemoteDatabaseRecord.META);
        } catch (SQLException e) {
            logger.error("Error verifying persistent record schemas for Neo4J records.", e);
        }
    }

    private RemoteDatabaseRecord getDatabaseRecord(DatabaseRecord SettingsRecord) {
        return context.getPersistenceInterface().find(RemoteDatabaseRecord.META, SettingsRecord.getId());
    }

    @Override
    public void startup(LicenseState licenseState) {
        List<DatabaseRecord> baseRecords = context.getPersistenceInterface().query(new SQuery<>(DatabaseRecord.META));
        for (DatabaseRecord SettingsRecord : baseRecords) {
            RemoteDatabaseRecord remoteRecord = getDatabaseRecord(SettingsRecord);
            try {                
                DatabaseConnector dbConnector = new DatabaseConnector(SettingsRecord, remoteRecord);
                CONNECTORS.put(SettingsRecord.getName(), dbConnector);
                // Instantiate the executorService that will update the database statuse
                context.getScheduledExecutorService().scheduleAtFixedRate(new DatabaseConnectorStatus(context, SettingsRecord.getId()), 2, SettingsRecord.getValidationTimeout(), TimeUnit.MILLISECONDS);
            } catch ( Exception e ) { 
                logger.error("Unable to instantiate connection '" + SettingsRecord.getName() + "': " + e.getMessage());
            }
        }       
    }

    public static DatabaseConnector getConnector(String connectorName) {
        return GatewayHook.CONNECTORS.getOrDefault(connectorName, null);
    }

    @Override
    public void shutdown() {
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

    public static Neo4JProperties getProjectSettings(String projectName) throws ProjectInvalidException {
        
        RuntimeProject project = context.getProjectManager()
        .getProject(projectName)
        .orElseThrow() // throws NoSuchElementException if project not found
        .validateOrThrow(); // throws ProjectInvalidException if the project is invalid
    
        Neo4JProperties properties = null;
        Optional<ProjectResource> maybeResource = project.getSingletonResource(Neo4JProperties.RESOURCE_TYPE);
        if (maybeResource.isPresent()) {
            ProjectResource resource = maybeResource.get();
            try {
                properties = ResourceUtil.decodeOrNull(resource, context.createDeserializer(), Neo4JProperties.class);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        
        if (properties == null) {
            properties = new Neo4JProperties();
        }   
        return properties;
    }


    @Override
    public Object getRPCHandler(ClientReqSession session, String projectName) {
        Neo4JProperties projectProperties;
        try {
            projectProperties = getProjectSettings(projectName);
        } catch (ProjectInvalidException e) {
            projectProperties = null;
         }

        if ( projectProperties != null ) {
            scriptModule.setProjectProperties(projectProperties);
        }

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
        new Neo4JStatusRoutes(context, routes).mountRoutes();
    }

    @Override
    public List<? extends INamedTab> getStatusPanels() {
        return Collections.singletonList(NEO_STATUS_PAGE);
    }
}
