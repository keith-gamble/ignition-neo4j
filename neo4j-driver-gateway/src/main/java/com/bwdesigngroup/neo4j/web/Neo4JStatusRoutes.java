
package com.bwdesigngroup.neo4j.web;

import com.bwdesigngroup.neo4j.GatewayHook;
import com.bwdesigngroup.neo4j.components.DatabaseConnector;
import com.bwdesigngroup.neo4j.records.DatabaseRecord;
import com.bwdesigngroup.neo4j.records.RemoteDatabaseRecord;
import com.inductiveautomation.ignition.common.util.LogUtil;
import com.inductiveautomation.ignition.common.util.LoggerEx;
import com.inductiveautomation.ignition.gateway.dataroutes.RequestContext;
import com.inductiveautomation.ignition.gateway.dataroutes.RouteGroup;
import com.inductiveautomation.ignition.gateway.dataroutes.WicketAccessControl;
import com.inductiveautomation.ignition.gateway.localdb.persistence.PersistenceSession;
import com.inductiveautomation.ignition.gateway.model.GatewayContext;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import simpleorm.dataset.SQuery;

import javax.servlet.http.HttpServletResponse;

import java.io.UnsupportedEncodingException;
import java.util.List;
import java.net.URLDecoder;

import static com.inductiveautomation.ignition.gateway.dataroutes.RouteGroup.TYPE_JSON;

public class Neo4JStatusRoutes {

    private final LoggerEx log = LogUtil.getLogger(getClass().getSimpleName());
    private final RouteGroup routes;
    private GatewayHook INSTANCE;

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
        GatewayContext context = requestContext.getGatewayContext();
        JSONObject json = new JSONObject();
        PersistenceSession session = context.getPersistenceInterface().getSession();
        try {
            SQuery<DatabaseRecord> query = new SQuery<>(DatabaseRecord.META);
            List<DatabaseRecord> connectionList = session.query(query);
            if (connectionList != null){
                json.put("count", connectionList.size());
                JSONArray jsonArray = new JSONArray();
                json.put("connections", jsonArray);
                for (DatabaseRecord record : connectionList){
                    if (record != null) {
                        DatabaseConnector dbConnector = GatewayHook.getConnector(record.getName());
                        JSONObject connectionJson = new JSONObject();
                        jsonArray.put(connectionJson);
                        connectionJson.put("ConnectionName", record.getName());
                        connectionJson.put("ConnectionType", record.getType());
                        connectionJson.put("ConnectionStatus", record.getStatus());
                        connectionJson.put("MaxConnectionPoolSize", dbConnector.getMaxConnectionPoolSize());
                        connectionJson.put("ActiveConnections", dbConnector.getActiveConnections());
                    }
                }
            }
        } finally {
            session.close();
        }
        return json;
    }
}

