package com.bwdesigngroup.neo4j.records;

import com.inductiveautomation.ignition.gateway.localdb.persistence.Category;
import com.inductiveautomation.ignition.gateway.localdb.persistence.IdentityField;
import com.inductiveautomation.ignition.gateway.localdb.persistence.PersistentRecord;
import com.inductiveautomation.ignition.gateway.localdb.persistence.RecordMeta;
import com.inductiveautomation.ignition.gateway.localdb.persistence.StringField;
import com.inductiveautomation.ignition.gateway.web.components.editors.PasswordEditorSource;

import simpleorm.dataset.SFieldFlags;


public class Neo4JSettingsRecord extends PersistentRecord {

    public static final RecordMeta<Neo4JSettingsRecord> META = new RecordMeta<Neo4JSettingsRecord>(
            Neo4JSettingsRecord.class, "Neo4JSettingsRecord").setNounKey("Neo4JSettingsRecord.Noun").setNounPluralKey(
            "Neo4JSettingsRecord.Noun.Plural");

    public static final IdentityField Id = new IdentityField(META);


    // Connection settings - For the database
    public static final StringField Neo4JDatabasePath = new StringField(META, "Neo4JDatabasePath", SFieldFlags.SMANDATORY).setDefault("bolt://localhost:7687");
    public static final StringField Neo4JUsername = new StringField(META, "Neo4JUsername").setDefault("neo4j");
    public static final StringField Neo4JPassword = new StringField(META, "Neo4JPassword");

    static {
        Neo4JPassword.getFormMeta().setEditorSource(PasswordEditorSource.getSharedInstance());
    }

    // Create the category for our connection info
    static final Category ConnectionInfo = new Category("Neo4JSettingsRecord.Category.Connection", 1000).include(Neo4JDatabasePath, Neo4JUsername, Neo4JPassword);


    // Define the accessors for the database settings
    public void setId(Long id) {
        setLong(Id, id);
    }

    public Long getId() {
        return getLong(Id);
    }

    public void setNeo4JDatabasePath(String path) {
        setString(Neo4JDatabasePath, path);
    }

    public String getNeo4JDatabasePath() {
        return getString(Neo4JDatabasePath);
    }

    public void setNeo4JUsername(String username) {
        setString(Neo4JUsername, username);
    }

    public String getNeo4JUsername() {
        return getString(Neo4JUsername);
    }

    public void setNeo4JPassword(String pass) {
        setString(Neo4JPassword, pass);
    }

    public String getNeo4JPassword() {
        return getString(Neo4JPassword);
    }

    @Override
    public RecordMeta<?> getMeta() {
        return META;
    }
}
