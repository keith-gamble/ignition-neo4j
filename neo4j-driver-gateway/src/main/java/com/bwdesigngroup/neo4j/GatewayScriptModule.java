package com.bwdesigngroup.neo4j;

import com.bwdesigngroup.neo4j.records.BaseRecord;
import com.bwdesigngroup.neo4j.records.RemoteDatabaseRecord;
import com.bwdesigngroup.neo4j.scripting.AbstractScriptModule;
import com.inductiveautomation.ignition.gateway.model.GatewayContext;


import simpleorm.dataset.SQuery;

public class GatewayScriptModule extends AbstractScriptModule {
    private GatewayContext context;

    public GatewayScriptModule( GatewayContext context )
    {
        this.context = context;
    }

    private BaseRecord getSettingsRecord(String connectionName) {
        return context.getPersistenceInterface().queryOne(new SQuery<>(BaseRecord.META).eq(BaseRecord.Name, connectionName));
    }

    private RemoteDatabaseRecord getRemoteRecord(BaseRecord baseRecord) {
        return context.getPersistenceInterface().find(RemoteDatabaseRecord.META, baseRecord.getLong(BaseRecord.Id));
    }

    @Override
    protected String getDBPathImpl(String connectionName) {
        BaseRecord settingsRecord = getSettingsRecord(connectionName);
       
        if ( settingsRecord.getType().equals("remote") ) {
            RemoteDatabaseRecord remoteRecord = getRemoteRecord(settingsRecord);
            if  ( remoteRecord != null ) {
                return remoteRecord.getUrl();
            }
        }

        return null;
    }

    @Override
    protected String getDBUsernameImpl(String connectionName) {
        BaseRecord settingsRecord = getSettingsRecord(connectionName);

        if ( settingsRecord.getType().equals("remote") ) {
            RemoteDatabaseRecord remoteRecord = getRemoteRecord(settingsRecord);
            if  ( remoteRecord != null ) {
                return remoteRecord.getUsername();
            }
        }

        return null;
    }

    @Override
    protected String getDBPasswordImpl(String connectionName) {
        BaseRecord settingsRecord = getSettingsRecord(connectionName);

        if ( settingsRecord.getType().equals("remote") ) {
            RemoteDatabaseRecord remoteRecord = getRemoteRecord(settingsRecord);
            if  ( remoteRecord != null ) {
                return remoteRecord.getPassword();
            }
        }

        return null;
    }

}
