package com.bwdesigngroup.neo4j.records;

import com.inductiveautomation.ignition.gateway.localdb.persistence.BooleanField;
import com.inductiveautomation.ignition.gateway.localdb.persistence.Category;
import com.inductiveautomation.ignition.gateway.localdb.persistence.IdentityField;
import com.inductiveautomation.ignition.gateway.localdb.persistence.PersistentRecord;
import com.inductiveautomation.ignition.gateway.localdb.persistence.RecordMeta;
import com.inductiveautomation.ignition.gateway.localdb.persistence.StringField;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import simpleorm.dataset.SFieldFlags;

public class BaseRecord extends PersistentRecord {
    private final Logger logger = LoggerFactory.getLogger(getClass());
    
    public static final RecordMeta<BaseRecord> META = new RecordMeta<>(BaseRecord.class, "neo4j_databases").setNounKey("BaseRecord.Noun").setNounPluralKey("BaseRecord.Noun.Plural");

    public static final IdentityField ID = new IdentityField(META);
    public static final StringField NAME = new StringField(META, "Name", SFieldFlags.SMANDATORY, SFieldFlags.SDESCRIPTIVE);
    public static final StringField TYPE = new StringField(META, "Type", SFieldFlags.SMANDATORY, SFieldFlags.SDESCRIPTIVE);
    public static final StringField STATUS = new StringField(META, "Status", SFieldFlags.SMANDATORY, SFieldFlags.SDESCRIPTIVE).setDefault("Created");
    public static final BooleanField ENABLED = new BooleanField(META, "Enabled", SFieldFlags.SMANDATORY).setDefault(true);

    static final Category Main = new Category("BaseRecord.Category.Main", 1000).include(ID, NAME, ENABLED);

    static {
        TYPE.getFormMeta().setVisible(false);
        STATUS.getFormMeta().setVisible(false);
    }

    @Override
    public RecordMeta<?> getMeta() {
        return META;
    } 

    public Long getId() {
        return getLong(ID);
    }

    public String getType() {
        return getString(TYPE);
    }

    public boolean getEnabled() {
        return getBoolean(ENABLED);
    }

    public String getName() {
        return getString(NAME);
    }

    public String getStatus() {
        return getString(STATUS);
    }

    public void setStatus(String status) {
        logger.debug("Setting status for " + this.getName() + " to " + status);
        this.setString(STATUS, status);
    }

}
