package com.bwdesigngroup.neo4j.records;

import com.inductiveautomation.ignition.gateway.localdb.persistence.BooleanField;
import com.inductiveautomation.ignition.gateway.localdb.persistence.Category;
import com.inductiveautomation.ignition.gateway.localdb.persistence.IdentityField;
import com.inductiveautomation.ignition.gateway.localdb.persistence.PersistentRecord;
import com.inductiveautomation.ignition.gateway.localdb.persistence.RecordMeta;
import com.inductiveautomation.ignition.gateway.localdb.persistence.StringField;
import simpleorm.dataset.SFieldFlags;

public class BaseRecord extends PersistentRecord {
    public static final RecordMeta<BaseRecord> META = new RecordMeta<>(BaseRecord.class, "databaseConnection").setNounKey("BaseRecord.Noun").setNounPluralKey("BaseRecord.Noun.Plural");

    public static final IdentityField Id = new IdentityField(META);
    public static final StringField Type = new StringField(META, "Type", SFieldFlags.SMANDATORY, SFieldFlags.SDESCRIPTIVE);
    public static final StringField Name = new StringField(META, "Name", SFieldFlags.SMANDATORY, SFieldFlags.SDESCRIPTIVE);
    public static final StringField Status = new StringField(META, "Status", SFieldFlags.SMANDATORY).setDefault("Created");
    public static final BooleanField Enabled = new BooleanField(META, "Enabled", SFieldFlags.SMANDATORY).setDefault(true);

    static final Category Main = new Category("BaseRecord.Category.Main", 1000).include(Id, Name, Enabled);

    static {
        Type.getFormMeta().setVisible(false);
        Status.getFormMeta().setVisible(false);
    }

    @Override
    public RecordMeta<?> getMeta() {
        return META;
    } 

    public String getType() {
        return getString(Type);
    }

    public String getName() {
        return getString(Name);
    }

}
