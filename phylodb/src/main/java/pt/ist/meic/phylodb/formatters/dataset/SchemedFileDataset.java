package pt.ist.meic.phylodb.formatters.dataset;

import pt.ist.meic.phylodb.typing.profile.model.Profile;
import pt.ist.meic.phylodb.typing.schema.model.Schema;

import java.util.List;

public class SchemedFileDataset extends FileDataset<Profile> {

	private String type;
	private String[] lociIds;

	public SchemedFileDataset(String[] lociIds, List<Profile> entities) {
		super(entities);
		this.lociIds = lociIds;
	}

	public SchemedFileDataset(Schema schema, List<Profile> entities) {
		this(schema.getLociIds(), entities);
		this.type = schema.getType();
	}

	public String getType() {
		return type;
	}

	public String[] getLociIds() {
		return lociIds;
	}

}
