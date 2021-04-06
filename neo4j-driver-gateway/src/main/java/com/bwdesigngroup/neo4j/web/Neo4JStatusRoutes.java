
package com.bwdesigngroup.neo4j.web;

import com.bwdesigngroup.neo4j.GatewayHook;
import com.bwdesigngroup.neo4j.components.DatabaseConnector;
import com.bwdesigngroup.neo4j.records.BaseRecord;
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

/**
 * Filename: HomeConnectStatusRoutes
 * Created on 9/22/16
 * Author: Kathy Applebaum
 * Copyright: Inductive Automation 2016
 * Project: home-connect-example
 * <p/>
 */
public class Neo4JStatusRoutes {

    private final LoggerEx log = LogUtil.getLogger(getClass().getSimpleName());
    private final RouteGroup routes;
    private GatewayHook INSTANCE;

    public Neo4JStatusRoutes(GatewayContext context, RouteGroup group, GatewayHook INSTANCE) {
        this.routes = group;
        this.INSTANCE = INSTANCE;
    }

    public void mountRoutes() {
        routes.newRoute("/status/connections")
                .handler(this::getConnectionStatus)
                .type(TYPE_JSON)
                .restrict(WicketAccessControl.STATUS_SECTION)
                .mount();

        // Not used in this example. Shown here as an example of using parameters passed in from javascript
        routes.newRoute("/status/connections/:name")
                .handler((req, res) -> getConnectionDetail(req, res, req.getParameter("name")))
                .type(TYPE_JSON)
                .restrict(WicketAccessControl.STATUS_SECTION)
                .mount();
    }

    public JSONObject getConnectionStatus(RequestContext requestContext, HttpServletResponse httpServletResponse) throws JSONException {
        GatewayContext context = requestContext.getGatewayContext();
        JSONObject json = new JSONObject();
        PersistenceSession session = context.getPersistenceInterface().getSession();
        try {
            SQuery<BaseRecord> query = new SQuery<>(BaseRecord.META);
            List<BaseRecord> connectionList = session.query(query);
            if (connectionList != null){
                json.put("count", connectionList.size());
                JSONArray jsonArray = new JSONArray();
                json.put("connections", jsonArray);
                for (BaseRecord record : connectionList){
                    if (record != null) {
                        JSONObject connectionJson = new JSONObject();
                        jsonArray.put(connectionJson);
                        connectionJson.put("ConnectionName", record.getName());
                        connectionJson.put("ConnectionType", record.getType());
                        connectionJson.put("ConnectionStatus", record.getStatus());
                    }
                }
            }
        } finally {
            session.close();
        }
        return json;
    }

    // Not used in this example. Shown here as an example of using parameters passed in from javascript.
    // Any time the parameter is something that could have anything outside of a-zA-Z0-9, be sure to encode/decode the
    // parameter.
    public JSONObject getConnectionDetail(RequestContext requestContext, HttpServletResponse httpServletResponse, String connectionName) throws JSONException, UnsupportedEncodingException {
        String decodedConnectionName = URLDecoder.decode(connectionName, "UTF-8");
        DatabaseConnector connector = INSTANCE.getConnector(decodedConnectionName);
        JSONObject json = new JSONObject();
        
        boolean isValid = connector.verifyConnectivity();
        json.put("connection", isValid);
        return json;
    }
}

