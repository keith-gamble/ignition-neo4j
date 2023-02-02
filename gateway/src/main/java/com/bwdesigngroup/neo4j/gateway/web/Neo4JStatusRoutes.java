
package com.bwdesigngroup.neo4j.gateway.web;

import static com.inductiveautomation.ignition.gateway.dataroutes.RouteGroup.TYPE_JSON;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.List;

import javax.servlet.http.HttpServletResponse;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.bwdesigngroup.neo4j.gateway.Neo4JDriverGatewayHook;
import com.bwdesigngroup.neo4j.gateway.records.DatabaseRecord;
import com.inductiveautomation.ignition.common.util.LogUtil;
import com.inductiveautomation.ignition.common.util.LoggerEx;
import com.inductiveautomation.ignition.gateway.dataroutes.RequestContext;
import com.inductiveautomation.ignition.gateway.dataroutes.RouteGroup;
import com.inductiveautomation.ignition.gateway.dataroutes.WicketAccessControl;
import com.inductiveautomation.ignition.gateway.model.GatewayContext;

public class Neo4JStatusRoutes {

    private final LoggerEx log = LogUtil.getLogger(getClass().getSimpleName());
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

    public JSONObject getConnectionStatus(RequestContext requestContext, HttpServletResponse httpServletResponse) throws JSONException {
        JSONObject json = new JSONObject();
		List<DatabaseRecord> connectionList = Neo4JDriverGatewayHook.getDatabaseRecords();
		if (connectionList != null){
			json.put("count", connectionList.size());
			JSONArray jsonArray = new JSONArray();
			json.put("connections", jsonArray);
			for (DatabaseRecord record : connectionList){
				if (record != null) {
					JSONObject connectionJson = new JSONObject();
					jsonArray.put(connectionJson);
					connectionJson.put("name", record.getName());
					connectionJson.put("type", record.getType());
					connectionJson.put("status", record.getStatus());
					connectionJson.put("maxConnectionPoolSize", record.getMaxConnectionPoolSize());
					connectionJson.put("activeConnections", record.getActiveConnections());
					
					if (record.exception != null) {

						JSONObject exception = new JSONObject();
						exception.put("message", record.exception.getMessage());

						// Convert the stacktrace to a newline separated string
						StringWriter stringWriter = new StringWriter();
						PrintWriter printWriter = new PrintWriter(stringWriter);
						record.exception.printStackTrace(printWriter);
						String sStackTrace = stringWriter.toString();
						exception.put("stacktrace", sStackTrace);

						connectionJson.put("exception", exception);
					}
				}
			}
		}
        return json;
    }
}

