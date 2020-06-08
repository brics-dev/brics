package gov.nih.tbi.repository.rdf;

import com.hp.hpl.jena.rdf.model.Model;

import gov.nih.tbi.repository.model.GenericTable;

public interface RDFGenResourceBuilder {
	/**
	 * Inserts the table result for a repeatable group
	 * 
	 * @param rgName - name of the repeatable group
	 * @param tableResult
	 */
	public void putTableResult(String rgName, GenericTable tableResult);

	/**
	 * Returns the resulting model to be written to the triples file.
	 * 
	 * @return
	 */
	public Model buildModel();
}
