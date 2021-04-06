/*
 * Copyright 2021 Keith Gamble
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
*/
package com.bwdesigngroup.neo4j.web;

import java.util.Collections;
import java.util.List;

import com.bwdesigngroup.neo4j.records.BaseRecord;
import com.inductiveautomation.ignition.gateway.localdb.persistence.PersistenceSession;
import com.inductiveautomation.ignition.gateway.model.GatewayContext;
import com.inductiveautomation.ignition.gateway.web.pages.status.overviewmeta.InfoLine;
import com.inductiveautomation.ignition.gateway.web.pages.status.overviewmeta.OverviewContributor;
import com.inductiveautomation.ignition.gateway.web.pages.status.overviewmeta.SystemsEntry;
import simpleorm.dataset.SQuery;

/**
 *
 * @author Keith Gamble
 */
public class Neo4JOverviewContributor implements OverviewContributor {

    @Override
    public Iterable<SystemsEntry> getSystemsEntries(GatewayContext context) {

        int connectionCount = 0;
        PersistenceSession session = context.getPersistenceInterface().getSession();
        try {
            SQuery<BaseRecord> query = new SQuery<>(BaseRecord.META);
            List<BaseRecord> connectionList = session.query(query);
            if (connectionList != null){
                connectionCount = connectionList.size();
            }
        } finally {
            session.close();
        }

        SystemsEntry connections = new SystemsEntry(
                "Graphs",
                new InfoLine(String.format("%d configured", connectionCount), false),
                "/main/web/status/sys.neo4j"
        );
        return Collections.singletonList(connections);
    }
}