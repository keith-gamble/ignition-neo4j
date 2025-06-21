package dev.bwdesigngroup.neo4j.gateway.web;

import java.util.List;

import dev.bwdesigngroup.neo4j.gateway.Neo4JDriverGatewayHook;
import dev.bwdesigngroup.neo4j.gateway.records.DatabaseRecord;
import com.inductiveautomation.ignition.gateway.localdb.persistence.RecordMeta;
import com.inductiveautomation.ignition.gateway.model.ExtensionPointManager;
import com.inductiveautomation.ignition.gateway.web.components.ExtensionPointPage;
import com.inductiveautomation.ignition.gateway.web.models.ConfigCategory;
import com.inductiveautomation.ignition.gateway.web.models.DefaultConfigTab;
import com.inductiveautomation.ignition.gateway.web.models.IConfigTab;
import com.inductiveautomation.ignition.gateway.web.pages.IConfigPage;

public class Neo4JExtensionPage extends ExtensionPointPage<DatabaseRecord> {

	public static final ConfigCategory CONFIG_CATEGORY = new ConfigCategory("Neo4J", "Neo4J.nav.header", 700);

	public static final IConfigTab CONFIG_ENTRY = DefaultConfigTab.builder()
			.category(CONFIG_CATEGORY)
			.name("neo4j")
			.i18n("Neo4J.nav.connections.title")
			.page(Neo4JExtensionPage.class)
			.terms("Neo4J Connections")
			.build();

	public Neo4JExtensionPage(IConfigPage configPage) {
		super(configPage);
	}

	@Override
	protected ExtensionPointManager getExtensionPointManager() {
		return Neo4JDriverGatewayHook.getInstance();
	}

	@Override
	protected RecordMeta<DatabaseRecord> getRecordMeta() {
		return DatabaseRecord.META;
	}

	@Override
	protected List<ICalculatedField<DatabaseRecord>> getCalculatedFields() {

		List<ICalculatedField<DatabaseRecord>> calcFields = List.of(
				new ICalculatedField<DatabaseRecord>() {
					@Override
					public String getFieldvalue(DatabaseRecord record) {
						return record.getStatus();
					}

					@Override
					public String getHeaderKey() {
						return "DatabaseRecord.Status.Name";
					}
				},

				// Add a new calculated field for connection details
				new ICalculatedField<DatabaseRecord>() {
					@Override
					public String getFieldvalue(DatabaseRecord record) {
						return String.valueOf(record.getActiveConnections()) + "/" +
								String.valueOf(record.getMaxConnectionPoolSize());
					}

					@Override
					public String getHeaderKey() {
						return "DatabaseRecord.Connections.Name";
					}
				});

		return calcFields;
	}
}