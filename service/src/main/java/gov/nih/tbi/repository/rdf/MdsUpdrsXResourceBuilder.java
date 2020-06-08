package gov.nih.tbi.repository.rdf;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.ResourceFactory;

import gov.nih.tbi.repository.model.GenericTable;
import gov.nih.tbi.repository.model.GenericTableRow;
import gov.nih.tbi.semantic.model.GuidRDF;

public class MdsUpdrsXResourceBuilder implements RDFGenResourceBuilder {
	private Map<String, GenericTable> repeatableTableResultMap;

	private static final String REQUIRED_RG = "Required Fields";
	private static final String GUID_COLUMN = "guid";


	/**
	 * {@inheritDoc}
	 */
	public void putTableResult(String name, GenericTable tableResult) {
		if (this.repeatableTableResultMap == null) {
			this.repeatableTableResultMap = new HashMap<>();
		}

		this.repeatableTableResultMap.put(name, tableResult);
	}


	/**
	 * For every GUID that appears in the MDS_UPDRS_X form, this method will create a triple like
	 * <http://ninds.nih.gov/repository/fitbir/1.0/Guid/guid1234>
	 * <http://ninds.nih.gov/repository/fitbir/1.0/Guid/mdsUpdrsX> "true" .
	 * 
	 * The triples are all written to an in-memory JENA model and returned
	 */
	public Model buildModel() {
		Model model = ModelFactory.createDefaultModel();

		GenericTable requiredResult = repeatableTableResultMap.get(REQUIRED_RG);
		List<String> guids = new ArrayList<>();

		for (GenericTableRow row : requiredResult.getRows()) {
			String guid = row.getStringByColumnName(GUID_COLUMN);
			if (guid != null && !guid.isEmpty()) {
				guids.add(guid);
			}
		}

		for (String guid : guids) {
			model.add(GuidRDF.createGuidResource(guid), GuidRDF.MDS_UPDRS_X_PROP,
					ResourceFactory.createPlainLiteral("true"));
		}

		return model;
	}
}
