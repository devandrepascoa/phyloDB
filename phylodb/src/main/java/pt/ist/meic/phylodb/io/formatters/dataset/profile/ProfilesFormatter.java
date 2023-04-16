package pt.ist.meic.phylodb.io.formatters.dataset.profile;

import pt.ist.meic.phylodb.io.formatters.Formatter;
import pt.ist.meic.phylodb.phylogeny.allele.model.Allele;
import pt.ist.meic.phylodb.typing.Method;
import pt.ist.meic.phylodb.typing.profile.model.Profile;
import pt.ist.meic.phylodb.typing.schema.model.Schema;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

/**
 * Base class for profile formatters
 */
public abstract class ProfilesFormatter extends Formatter<Profile> {

	protected String projectId;
	protected String datasetId;
	protected Schema schema;
	protected String missing;
	protected boolean authorized;

	/**
	 * Retrieves the requested ProfilesFormatter
	 *
	 * @param format a String that identifies the formatter
	 * @return the ProfilesFormatter represented by the parameter format
	 */
	public static ProfilesFormatter get(String format) {
		return new HashMap<String, ProfilesFormatter>() {{
			put(Method.MLST.getName(), new MlFormatter());
			put(Method.MLVA.getName(), new MlFormatter());
			put(Method.SNP.getName(), new SnpFormatter());
		}}.get(format);
	}

	/**
	 * Retrieves the alleles formatted to be used in the {@link #format(List, Object...)} operation
	 *
	 * @param alleles list of alleles
	 * @return a {@link List<String>} with each allele formatted
	 */
	protected static List<String> formatAlleles(List<Allele.PrimaryKey> alleles) {
		List<String> output = new ArrayList<>();
		for (Allele.PrimaryKey allele : alleles) {
			if (allele == null)
				output.add(" ");
			else
				output.add(allele.getId());
		}
		return output;
	}

	@Override
	protected boolean init(Iterator<String> it, Object... params) {
		this.projectId = (String) params[0];
		this.datasetId = (String) params[1];
		this.schema = (Schema) params[2];
		this.missing = (String) params[3];
		this.authorized = (boolean) params[4];
		return true;
	}

}
