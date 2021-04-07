package com.bwdesigngroup.neo4j.records;


import com.bwdesigngroup.neo4j.DatabaseRecordType;
import com.bwdesigngroup.neo4j.instances.RemoteDatabaseInstance;
import com.inductiveautomation.ignition.common.BundleUtil;
import com.inductiveautomation.ignition.gateway.localdb.persistence.Category;
import com.inductiveautomation.ignition.gateway.localdb.persistence.IdentityField;
import com.inductiveautomation.ignition.gateway.localdb.persistence.LongField;
import com.inductiveautomation.ignition.gateway.localdb.persistence.PersistentRecord;
import com.inductiveautomation.ignition.gateway.localdb.persistence.RecordMeta;
import com.inductiveautomation.ignition.gateway.localdb.persistence.ReferenceField;
import com.inductiveautomation.ignition.gateway.localdb.persistence.StringField;
import com.inductiveautomation.ignition.gateway.model.GatewayContext;
import com.inductiveautomation.ignition.gateway.web.components.editors.PasswordEditorSource;

import simpleorm.dataset.SFieldFlags;

public class RemoteDatabaseRecord extends PersistentRecord {
    
    public static final RecordMeta<RemoteDatabaseRecord> META = new RecordMeta<>(RemoteDatabaseRecord.class, "neo4j_remoteDBMS");

    public static final StringField CONNECTURL = new StringField(META, "ConnectUri", SFieldFlags.SMANDATORY).setDefault("bolt://localhost:7687");
    public static final StringField USERNAME = new StringField(META, "Username").setDefault("neo4j");
    public static final StringField PASSWORD = new StringField(META, "Password");

    public static final LongField PROFILE_ID = new LongField(META, "ProfileId", SFieldFlags.SPRIMARY_KEY);
    public static final ReferenceField<DatabaseRecord> PROFILE =
        new ReferenceField<>(META, DatabaseRecord.META, "Profile", PROFILE_ID);

    static {
        BundleUtil.get().addBundle("RemoteDatabaseRecord", RemoteDatabaseRecord.class, "RemoteDatabaseRecord");
        PASSWORD.getFormMeta().setEditorSource(PasswordEditorSource.getSharedInstance());
        PROFILE.getFormMeta().setVisible(false);
        CONNECTURL.getFormMeta().setFieldDescriptionKey("RemoteDatabaseRecord.ConnectUri.Description");
    }

    // Create the category for our connection info
    static final Category ConnectionInfo = new Category("RemoteDatabaseRecord.Category.Connection", 2000).include(CONNECTURL, USERNAME, PASSWORD);

    public static class RemoteDatabaseType extends DatabaseRecordType {
        public RemoteDatabaseType() {
            super("Remote", "Remote DBMS", "A remote hosted Neo4J Database");
        }

        @Override
        public RecordMeta<? extends PersistentRecord> getSettingsRecordType() {
            return RemoteDatabaseRecord.META;
        }

        @Override
        public
        RemoteDatabaseInstance createNewInstance(GatewayContext context, DatabaseRecord baseRecord) {
            RemoteDatabaseRecord childRecord = findProfileSettingsRecord(context, baseRecord);
            return new RemoteDatabaseInstance(baseRecord, childRecord);
        }
    }

    // Define the accessors for the database settings
    public void setUri(String path) {
        setString(CONNECTURL, path);
    }

    public String getUri() {
        return getString(CONNECTURL);
    }

    public void setUsername(String username) {
        setString(this.USERNAME, username);
    }

    public String getUsername() {
        return getString(USERNAME);
    }

    public void setPassword(String pass) {
        setString(this.PASSWORD, pass);
    }

    public String getPassword() {
        return getString(PASSWORD);
    }

    @Override
    public RecordMeta<?> getMeta() {
        return META;
    }
}
