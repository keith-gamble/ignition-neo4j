/*
 * Copyright 2021 Keith Gamble
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
*/
package com.bwdesigngroup.neo4j.components;

import java.util.List;

import com.bwdesigngroup.neo4j.GatewayHook;
import com.bwdesigngroup.neo4j.records.BaseRecord;
import com.inductiveautomation.ignition.gateway.model.GatewayContext;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import simpleorm.dataset.SQuery;

/**
 *
 * @author Keith Gamble
 */
public class DatabaseConnectorStatus implements Runnable{

    private GatewayContext context;
    private GatewayHook gateway;
    private final Logger logger = LoggerFactory.getLogger(getClass());

    public DatabaseConnectorStatus(GatewayContext context, GatewayHook gateway) {
        this.context = context;
        this.gateway = gateway;
    }

    @Override
    public void run() {
        List<BaseRecord> baseRecords = context.getPersistenceInterface().query(new SQuery<>(BaseRecord.META));
        for (BaseRecord SettingsRecord : baseRecords) {
            DatabaseConnector connector = gateway.getConnector(SettingsRecord.getName());
            boolean isConnected = connector.verifyConnectivity();
            boolean enabled = SettingsRecord.getBoolean(BaseRecord.Enabled);

            String status = (isConnected) ? "Valid" : "Faulted";
            status = (enabled) ? status : "Disabled";
            SettingsRecord.setStatus(status);
            context.getPersistenceInterface().save(SettingsRecord);
        }
        return;
    }
}
