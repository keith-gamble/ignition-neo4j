package com.bwdesigngroup.neo4j.gateway.records;

import com.inductiveautomation.ignition.common.BundleUtil;
import com.inductiveautomation.ignition.gateway.localdb.persistence.BooleanField;
import com.inductiveautomation.ignition.gateway.localdb.persistence.Category;
import com.inductiveautomation.ignition.gateway.localdb.persistence.IdentityField;
import com.inductiveautomation.ignition.gateway.localdb.persistence.IntField;
import com.inductiveautomation.ignition.gateway.localdb.persistence.PersistentRecord;
import com.inductiveautomation.ignition.gateway.localdb.persistence.RecordMeta;
import com.inductiveautomation.ignition.gateway.localdb.persistence.StringField;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import simpleorm.dataset.SFieldFlags;

public class DatabaseRecord extends PersistentRecord {
    private final Logger logger = LoggerFactory.getLogger(getClass());
    
    
    public static final RecordMeta<DatabaseRecord> META = new RecordMeta<>(DatabaseRecord.class, "neo4j_databases").setNounKey("DatabaseRecord.Noun").setNounPluralKey("DatabaseRecord.Noun.Plural");


    // Initialize the  main properties
    public static final IdentityField ID = new IdentityField(META);
    public static final StringField NAME = new StringField(META, "Name", SFieldFlags.SMANDATORY, SFieldFlags.SDESCRIPTIVE);
    public static final StringField TYPE = new StringField(META, "Type", SFieldFlags.SMANDATORY, SFieldFlags.SDESCRIPTIVE);
    public static final StringField STATUS = new StringField(META, "Status", SFieldFlags.SMANDATORY, SFieldFlags.SDESCRIPTIVE).setDefault("Created");
    public static final BooleanField ENABLED = new BooleanField(META, "Enabled", SFieldFlags.SMANDATORY).setDefault(true);

    // Initialize the advanced properties
    public static final StringField DATABASE = new StringField(META, "Database");
    public static final IntField SLOW_QUERY_THRESHOLD = new IntField(META, "SlowQueryThreshold", SFieldFlags.SMANDATORY).setDefault(60000);
    public static final IntField VALIDATION_TIMEOUT = new IntField(META, "ValidationTimeout", SFieldFlags.SMANDATORY).setDefault(10000);
    public static final IntField MAX_CONNECTION_POOL_SIZE = new IntField(META, "MaxConnectionPoolSize", SFieldFlags.SMANDATORY).setDefault(8);

    static final Category Main = new Category("DatabaseRecord.Category.Main", 1000).include(ID, NAME, DATABASE, ENABLED);

    static final Category Advanced = new Category("DatabaseRecord.Category.Advanced", 9000, true).include(SLOW_QUERY_THRESHOLD, VALIDATION_TIMEOUT, MAX_CONNECTION_POOL_SIZE);

    static {
        BundleUtil.get().addBundle("DatabaseRecord", DatabaseRecord.class, "DatabaseRecord");
        
        TYPE.getFormMeta().setVisible(false);
        STATUS.getFormMeta().setVisible(false);
        ENABLED.getFormMeta().setFieldDescriptionKey("DatabaseRecord.Enabled.Description");
        DATABASE.getFormMeta().setFieldDescriptionKey("DatabaseRecord.Database.Description");
        SLOW_QUERY_THRESHOLD.getFormMeta().setFieldDescriptionKey("DatabaseRecord.SlowQueryThreshold.Description");
        VALIDATION_TIMEOUT.getFormMeta().setFieldDescriptionKey("DatabaseRecord.ValidationTimeout.Description");
        MAX_CONNECTION_POOL_SIZE.getFormMeta().setFieldDescriptionKey("DatabaseRecord.MaxConnectionPoolSize.Description");
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

    public String getDatabase() {
        return getString(DATABASE);
    }

    public int getSlowQueryThreshold() {
        return getInt(SLOW_QUERY_THRESHOLD);
    }

    public int getValidationTimeout() {
        return getInt(VALIDATION_TIMEOUT);
    }

    public int getMaxConnectionPoolSize() {
        return getInt(MAX_CONNECTION_POOL_SIZE);
    }

    public String getStatus() {
        return getString(STATUS);
    }

    public void setStatus(String status) {
        this.setString(STATUS, status);
    }

}
