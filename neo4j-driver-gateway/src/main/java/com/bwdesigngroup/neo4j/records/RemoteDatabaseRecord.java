package com.bwdesigngroup.neo4j.records;


import com.bwdesigngroup.neo4j.AbstractExtensionType;
import com.bwdesigngroup.neo4j.instances.RemoteDatabaseInstance;
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
    
    public static final RecordMeta<RemoteDatabaseRecord> META = new RecordMeta<>(RemoteDatabaseRecord.class, "extension_point_remoteDBMS");

    public static final StringField Url = new StringField(META, "Url", SFieldFlags.SMANDATORY).setDefault("bolt://localhost:7687");
    public static final StringField Username = new StringField(META, "Username").setDefault("neo4j");
    public static final StringField Password = new StringField(META, "Password");

    public static final LongField ProfileId = new LongField(META, "ProfileId", SFieldFlags.SPRIMARY_KEY);
    public static final ReferenceField<BaseRecord> Profile =
        new ReferenceField<>(META, BaseRecord.META, "Profile", ProfileId);

    static {
        Password.getFormMeta().setEditorSource(PasswordEditorSource.getSharedInstance());
        Profile.getFormMeta().setVisible(false);
    }

    // Create the category for our connection info
    static final Category ConnectionInfo = new Category("RemoteDatabaseRecord.Category.Connection", 2000).include(Url, Username, Password);

    public static class RemoteDatabaseType extends AbstractExtensionType {
        public RemoteDatabaseType() {
            super("Remote", "Remote DBMS", "A remote hosted Neo4J Database");
        }

        @Override
        public RecordMeta<? extends PersistentRecord> getSettingsRecordType() {
            return RemoteDatabaseRecord.META;
        }

        @Override
        public
        RemoteDatabaseInstance createNewInstance(GatewayContext context, BaseRecord baseRecord) {
            RemoteDatabaseRecord childRecord = findProfileSettingsRecord(context, baseRecord);
            return new RemoteDatabaseInstance(baseRecord, childRecord);
        }
    }

    // Define the accessors for the database settings
    public void setUrl(String path) {
        setString(Url, path);
    }

    public String getUrl() {
        return getString(Url);
    }

    public void setUsername(String username) {
        setString(this.Username, username);
    }

    public String getUsername() {
        return getString(Username);
    }

    public void setPassword(String pass) {
        setString(this.Password, pass);
    }

    public String getPassword() {
        return getString(Password);
    }

    @Override
    public RecordMeta<?> getMeta() {
        return META;
    }
}
