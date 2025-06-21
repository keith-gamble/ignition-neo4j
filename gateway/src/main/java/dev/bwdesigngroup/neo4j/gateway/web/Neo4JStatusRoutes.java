package dev.bwdesigngroup.neo4j.gateway.web;

import static com.inductiveautomation.ignition.gateway.dataroutes.RouteGroup.TYPE_JSON;

import java.util.List;

import javax.servlet.http.HttpServletResponse;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import dev.bwdesigngroup.neo4j.gateway.Neo4JDriverGatewayHook;
import dev.bwdesigngroup.neo4j.gateway.components.Neo4JConnectionManager;
import dev.bwdesigngroup.neo4j.gateway.records.DatabaseRecord;
import com.inductiveautomation.ignition.gateway.dataroutes.RequestContext;
import com.inductiveautomation.ignition.gateway.dataroutes.RouteGroup;
import com.inductiveautomation.ignition.gateway.dataroutes.WicketAccessControl;
import com.inductiveautomation.ignition.gateway.model.GatewayContext;

public class Neo4JStatusRoutes {

	private final RouteGroup routes;

	public Neo4JStatusRoutes(GatewayContext context, RouteGroup group) {
		this.routes = group;
	}

	public void mountRoutes() {
		routes.newRoute("/status/connections")
				.handler(this::getConnectionStatus)
				.type(TYPE_JSON)
				.restrict(WicketAccessControl.STATUS_SECTION)
				.mount();
	}

	public JSONObject getConnectionStatus(RequestContext requestContext, HttpServletResponse httpServletResponse)
			throws JSONException {
		JSONObject json = new JSONObject();
		List<DatabaseRecord> connectionList = Neo4JDriverGatewayHook.getDatabaseRecords();

		if (connectionList != null) {
			json.put("count", connectionList.size());
			JSONArray jsonArray = new JSONArray();
			json.put("connections", jsonArray);

			for (DatabaseRecord record : connectionList) {
				if (record != null) {
					JSONObject connectionJson = new JSONObject();
					jsonArray.put(connectionJson);

					// Basic record information
					connectionJson.put("name", record.getName());
					connectionJson.put("type", record.getType());
					connectionJson.put("status", record.getStatus());
					connectionJson.put("maxConnectionPoolSize", record.getMaxConnectionPoolSize());
					connectionJson.put("activeConnections", record.getActiveConnections());
					connectionJson.put("enabled", record.getEnabled());

					// Get detailed connection information from connection manager
					try {
						Neo4JConnectionManager.ConnectionPool connectionStatus = Neo4JDriverGatewayHook
								.getConnectionStatus(record.getName());

						if (connectionStatus != null) {
							// Add connection state information
							connectionJson.put("connectionState", connectionStatus.getState().toString());
							connectionJson.put("retryAttempts", connectionStatus.getRetryAttempts());
							connectionJson.put("lastHealthCheck", connectionStatus.getLastHealthCheck().toString());

							// Add error information if available
							String lastError = connectionStatus.getLastError();
							if (lastError != null) {
								JSONObject exception = new JSONObject();
								exception.put("message", lastError);
								exception.put("type", "Connection Error");

								// For connection errors, we don't have a full stack trace,
								// but we can provide the connection details
								exception.put("details", record.getConnectionDetails());

								connectionJson.put("exception", exception);
							}
						} else {
							// Connection not found in manager
							connectionJson.put("connectionState", "NOT_MANAGED");
							connectionJson.put("retryAttempts", 0);
							connectionJson.put("lastHealthCheck", "Never");
						}

					} catch (Exception e) {
						// Error getting connection status
						JSONObject exception = new JSONObject();
						exception.put("message", "Error getting connection status: " + e.getMessage());
						exception.put("type", "Status Error");
						exception.put("details", e.getClass().getSimpleName());
						connectionJson.put("exception", exception);

						// Set safe defaults
						connectionJson.put("connectionState", "ERROR");
						connectionJson.put("retryAttempts", 0);
						connectionJson.put("lastHealthCheck", "Error");
					}
				}
			}
		} else {
			json.put("count", 0);
			json.put("connections", new JSONArray());
		}

		return json;
	}
}