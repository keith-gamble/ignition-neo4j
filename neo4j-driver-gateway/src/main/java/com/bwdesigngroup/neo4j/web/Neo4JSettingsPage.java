package com.bwdesigngroup.neo4j.web;

import com.bwdesigngroup.neo4j.GatewayHook;
import com.bwdesigngroup.neo4j.records.Neo4JSettingsRecord;
import com.inductiveautomation.ignition.gateway.model.IgnitionWebApp;
import com.inductiveautomation.ignition.gateway.web.components.RecordEditForm;
import com.inductiveautomation.ignition.gateway.web.models.LenientResourceModel;
import com.inductiveautomation.ignition.gateway.web.pages.IConfigPage;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.wicket.Application;


public class Neo4JSettingsPage extends RecordEditForm {
    public static final Pair<String, String> MENU_LOCATION =
        Pair.of(GatewayHook.CONFIG_CATEGORY.getName(), "neo4j");

    public Neo4JSettingsPage(final IConfigPage configPage) {
        super(configPage, null, new LenientResourceModel("Neo4J.nav.settings.panelTitle"),
            ((IgnitionWebApp) Application.get()).getContext().getPersistenceInterface().find(Neo4JSettingsRecord.META, 0L)
        );
    }


    @Override
    public Pair<String, String> getMenuLocation() {
        return MENU_LOCATION;
    }

}
