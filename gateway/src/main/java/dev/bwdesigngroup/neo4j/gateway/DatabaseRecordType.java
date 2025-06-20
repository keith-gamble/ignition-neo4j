package dev.bwdesigngroup.neo4j.gateway;

import dev.bwdesigngroup.neo4j.gateway.instances.Extendable;
import dev.bwdesigngroup.neo4j.gateway.records.DatabaseRecord;

import com.inductiveautomation.ignition.gateway.model.BaseExtensionPointType;
import com.inductiveautomation.ignition.gateway.model.GatewayContext;

public abstract class DatabaseRecordType extends BaseExtensionPointType {
    public DatabaseRecordType(String typeId, String typeName, String description) {
        super(typeId, typeName , description);
    }

public abstract Extendable createNewInstance(GatewayContext context, DatabaseRecord baseRecord);
}
