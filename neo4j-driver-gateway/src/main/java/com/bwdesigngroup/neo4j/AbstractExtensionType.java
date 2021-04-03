package com.bwdesigngroup.neo4j;

import com.bwdesigngroup.neo4j.instances.Extendable;
import com.bwdesigngroup.neo4j.records.BaseRecord;
import com.inductiveautomation.ignition.gateway.model.BaseExtensionPointType;
import com.inductiveautomation.ignition.gateway.model.GatewayContext;

public abstract class AbstractExtensionType extends BaseExtensionPointType {
    public AbstractExtensionType(String typeId, String typeName, String description) {
        super(typeId, typeName , description);
    }

public abstract Extendable createNewInstance(GatewayContext context, BaseRecord baseRecord);
}
