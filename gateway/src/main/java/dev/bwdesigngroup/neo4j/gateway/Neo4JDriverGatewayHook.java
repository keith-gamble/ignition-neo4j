package dev.bwdesigngroup.neo4j.gateway;

import static dev.bwdesigngroup.neo4j.gateway.web.Neo4JExtensionPage.CONFIG_CATEGORY;
import static dev.bwdesigngroup.neo4j.gateway.web.Neo4JExtensionPage.CONFIG_ENTRY;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.apache.wicket.markup.html.WebMarkupContainer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import dev.bwdesigngroup.neo4j.common.projectresources.Neo4JProperties;
import dev.bwdesigngroup.neo4j.gateway.components.DatabaseConnector;
import dev.bwdesigngroup.neo4j.gateway.components.Neo4JConnectionManager;
import dev.bwdesigngroup.neo4j.gateway.records.DatabaseRecord;
import dev.bwdesigngroup.neo4j.gateway.records.RemoteDatabaseRecord;
import dev.bwdesigngroup.neo4j.gateway.web.Neo4JStatusRoutes;
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
import com.inductiveautomation.ignition.gateway.localdb.persistence.PersistenceInterface;
import com.inductiveautomation.ignition.gateway.model.AbstractGatewayModuleHook;
import com.inductiveautomation.ignition.gateway.model.ExtensionPointManager;
import com.inductiveautomation.ignition.gateway.model.ExtensionPointType;
import com.inductiveautomation.ignition.gateway.model.GatewayContext;
import com.inductiveautomation.ignition.gateway.model.GatewaySystemProperties;
import com.inductiveautomation.ignition.gateway.web.components.AbstractNamedTab;
import com.inductiveautomation.ignition.gateway.web.models.ConfigCategory;
import com.inductiveautomation.ignition.gateway.web.models.IConfigTab;
import com.inductiveautomation.ignition.gateway.web.models.INamedTab;
import com.inductiveautomation.ignition.gateway.web.models.KeyValue;
import com.inductiveautomation.ignition.gateway.web.pages.BasicReactPanel;
import com.inductiveautomation.ignition.gateway.web.pages.status.StatusCategories;

import simpleorm.dataset.SQuery;

/**
 * Updated Gateway Hook that uses the new Neo4JConnectionManager
 * for proper connection lifecycle management
 */
public class Neo4JDriverGatewayHook extends AbstractGatewayModuleHook implements ExtensionPointManager {
	private static final Logger logger = LoggerFactory.getLogger(Neo4JDriverGatewayHook.class);

	// Context and instance references
	private static GatewayContext context;
	private static Neo4JDriverGatewayHook INSTANCE;

	/**
	 * Get the gateway hook instance for access from other classes
	 */
	public static Neo4JDriverGatewayHook getInstance() {
		return INSTANCE;
	}

	// NEW: Connection manager replaces static connection map
	private Neo4JConnectionManager connectionManager;
	private final GatewayScriptModule scriptModule = new GatewayScriptModule();

	/**
	 * Get the connection manager (for diagnostics)
	 */
	public Neo4JConnectionManager getConnectionManager() {
		return connectionManager;
	}

	// Extension points configuration
	private final Map<String, DatabaseRecordType> extensionPoints = Map.of(
			"Remote", new RemoteDatabaseRecord.RemoteDatabaseType());

	/**
	 * Status panel configuration
	 */
	private static final INamedTab NEO_STATUS_PAGE = new AbstractNamedTab(
			"neo4j",
			StatusCategories.CONNECTIONS,
			"Neo4J.nav.status.header") {

		@Override
		public WebMarkupContainer getPanel(String panelId) {
			return new BasicReactPanel(panelId, "/res/neo4j/js/neo4jstatus.js", "neo4jstatus");
		}

		@Override
		public Iterable<String> getSearchTerms() {
			return Arrays.asList("neo4j connections", "neo");
		}
	};

	// Configuration setup
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

	/**
	 * Main setup method - called when module is loaded
	 */
	@Override
	public void setup(GatewayContext gatewayContext) {
		context = gatewayContext;
		INSTANCE = this;

		// Initialize connection manager
		connectionManager = new Neo4JConnectionManager();

		// Register bundles simply like the working example
		BundleUtil.get().addBundle("Neo4J", getClass(), "Neo4JDriverGatewayHook");

		// Verify database schema
		verifySchema(context);

		// Set up database record listeners
		setupRecordListeners();

		logger.info("Neo4J Driver Gateway Hook setup complete");
	}

	/**
	 * Setup listeners for database record changes
	 */
	private void setupRecordListeners() {
		DatabaseRecord.META.addRecordListener(new IRecordListener<DatabaseRecord>() {

			@Override
			public void recordUpdated(DatabaseRecord settingsRecord) {
				updateConnection(settingsRecord);
			}

			@Override
			public void recordAdded(DatabaseRecord settingsRecord) {
				updateConnection(settingsRecord);
			}

			@Override
			public void recordDeleted(KeyValue keyValue) {
				String connectionName = keyValue.toString();
				connectionManager.removeConnection(connectionName);
				logger.info("Removed connection configuration: {}", connectionName);
			}

			/**
			 * Update or add a connection using the connection manager
			 */
			private void updateConnection(DatabaseRecord settingsRecord) {
				try {
					RemoteDatabaseRecord remoteRecord = getDatabaseRecord(settingsRecord);
					if (remoteRecord != null) {
						connectionManager.addOrUpdateConnection(
								settingsRecord.getName(),
								settingsRecord,
								remoteRecord);
						logger.info("Updated connection configuration: {}", settingsRecord.getName());
					} else {
						logger.warn("Could not find remote database record for: {}", settingsRecord.getName());
					}
				} catch (Exception e) {
					logger.error("Error updating connection for record: {}", settingsRecord.getName(), e);
				}
			}
		});
	}

	/**
	 * Startup - initialize connections and start connection manager
	 */
	@Override
	public void startup(LicenseState licenseState) {
		logger.info("Starting up Neo4J Driver Module");

		try {
			// Initialize the connection manager
			connectionManager.initialize();

			// Load existing connections
			List<DatabaseRecord> baseRecords = getPersistenceInterface()
					.query(new SQuery<>(DatabaseRecord.META));

			for (DatabaseRecord settingsRecord : baseRecords) {
				try {
					RemoteDatabaseRecord remoteRecord = getDatabaseRecord(settingsRecord);
					if (remoteRecord != null) {
						connectionManager.addOrUpdateConnection(
								settingsRecord.getName(),
								settingsRecord,
								remoteRecord);
					}
				} catch (Exception e) {
					logger.error("Error loading connection '{}': {}",
							settingsRecord.getName(), e.getMessage());
				}
			}

			logger.info("Neo4J Driver Module startup complete. Loaded {} connections.",
					connectionManager.getConnectionNames().size());

		} catch (Exception e) {
			logger.error("Error during Neo4J Driver Module startup", e);
		}
	}

	/**
	 * Shutdown - properly close all connections
	 */
	@Override
	public void shutdown() {
		logger.info("Shutting down Neo4J Driver Module");

		if (connectionManager != null) {
			connectionManager.shutdown();
		}

		BundleUtil.get().removeBundle("Neo4J");
		logger.info("Neo4J Driver Module shutdown complete");
	}

	/**
	 * Get a database connector by name - now uses connection manager
	 */
	public static DatabaseConnector getConnector(String connectorName) {
		if (INSTANCE != null && INSTANCE.connectionManager != null) {
			return INSTANCE.connectionManager.getConnection(connectorName);
		}
		return null;
	}

	/**
	 * Get connection status - now uses connection manager
	 */
	public static Neo4JConnectionManager.ConnectionPool getConnectionStatus(String connectorName) {
		if (INSTANCE != null && INSTANCE.connectionManager != null) {
			return INSTANCE.connectionManager.getConnectionStatus(connectorName);
		}
		return null;
	}

	/**
	 * Get all database records
	 */
	public static List<DatabaseRecord> getDatabaseRecords() {
		return getPersistenceInterface().query(new SQuery<>(DatabaseRecord.META));
	}

	/**
	 * Get list of connection names - now uses connection manager
	 */
	public static List<String> getConnectionNames() {
		if (INSTANCE != null && INSTANCE.connectionManager != null) {
			return new ArrayList<>(INSTANCE.connectionManager.getConnectionNames());
		}
		return new ArrayList<>();
	}

	// Utility methods
	public static PersistenceInterface getPersistenceInterface() {
		return context.getPersistenceInterface();
	}

	private void verifySchema(GatewayContext context) {
		try {
			context.getSchemaUpdater().updatePersistentRecords(
					DatabaseRecord.META,
					RemoteDatabaseRecord.META);
		} catch (SQLException e) {
			logger.error("Error verifying persistent record schemas for Neo4J records.", e);
		}
	}

	private RemoteDatabaseRecord getDatabaseRecord(DatabaseRecord settingsRecord) {
		return getPersistenceInterface().find(RemoteDatabaseRecord.META, settingsRecord.getId());
	}

	// Script manager setup
	@Override
	public void initializeScriptManager(ScriptManager manager) {
		super.initializeScriptManager(manager);
		manager.addScriptModule(
				"system.neo4j",
				scriptModule,
				new PropertiesFileDocProvider());
	}

	// Project settings methods
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
				logger.error("Error decoding Neo4J properties", e);
			}
		}

		if (properties == null) {
			properties = new Neo4JProperties();
		}
		return properties;
	}

	public static String getDefaultScriptingProject() throws IllegalStateException {
		String value = context.getSystemPropertiesManager()
				.getPropertyValue(GatewaySystemProperties.GatewayScriptingProject);
		if (value == null) {
			throw new IllegalStateException("Default scripting project not set in system properties.");
		}
		return value;
	}

	// RPC handler for client communications
	@Override
	public Object getRPCHandler(ClientReqSession session, String projectName) {
		Neo4JProperties projectProperties;
		try {
			projectProperties = getProjectSettings(projectName);
		} catch (ProjectInvalidException e) {
			projectProperties = null;
		}

		if (projectProperties != null) {
			scriptModule.setProjectProperties(projectProperties);
		}

		return scriptModule;
	}

	@Override
	public boolean isFreeModule() {
		return true;
	}

	// Web interface setup
	@Override
	public Optional<String> getMountPathAlias() {
		return Optional.of("neo4j");
	}

	@Override
	public Optional<String> getMountedResourceFolder() {
		return Optional.of("mounted");
	}

	@Override
	public void mountRouteHandlers(RouteGroup routes) {
		new Neo4JStatusRoutes(context, routes).mountRoutes();
	}

	@Override
	public List<? extends INamedTab> getStatusPanels() {
		return Collections.singletonList(NEO_STATUS_PAGE);
	}
}