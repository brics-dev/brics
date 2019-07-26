package gov.nih.tbi.semantic.model;

import org.apache.commons.httpclient.URIException;
import org.apache.commons.httpclient.util.URIUtil;

import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.ResourceFactory;

import gov.nih.tbi.repository.model.AbstractDataset;

/**
 * BRICS vocabulary items
 * 
 * @author Nimesh Patel
 * 
 */
public class DatasetRDF {

	public static final String STRING_DATASET = "Dataset";

	// TODO: Dynamic add Disease?
	public static final String BASE_URI_NS = "http://ninds.nih.gov/repository/fitbir/1.0/";
	public static final String URI_NS = BASE_URI_NS + STRING_DATASET + "/";
	public static final String SJI_NS = URI_NS + "/submissionRecordJoin/";

	// Resources
	public static final Resource RESOURCE_DATASET = resource(STRING_DATASET);

	// Properties
	public static final Property PROPERTY_ID = property("datasetId");
	public static final Property PROPERTY_PREFIXED_ID = property("prefixedId");
	public static final Property PROPERTY_SJI = property("submissionRecordJoinId");
	public static final Property PROPERTY_STUDY = property("studyTitle");
	public static final Property PROPERTY_NAME = property("name");
	public static final Property PROPERTY_PUBLICATION_DATE = property("publicationDate");
	public static final Property PROPERTY_SUBMIT_DATE = property("submitDate");
	public static final Property PROPERTY_SUBMITTER_NAME = property("submitterName");
	public static final Property PROPERTY_STATUS = property("status");
	public static final Property PROPERTY_DATASET_NAME = property("datasetName");
	public static final Property PROPERTY_IS_DERIVED = property("isDerived");

	public static final Resource resource(String local) {

		return ResourceFactory.createResource(BASE_URI_NS + local);
	}

	public static final Property property(String local) {

		return ResourceFactory.createProperty(URI_NS, local);
	}

	public static String generateName(AbstractDataset dataset) {

		return dataset.getId().toString();
	}

	public static Resource createDatasetResource(String datasetId) {
		Resource datasetResource = null;

		try {
			datasetResource = ResourceFactory.createResource(URIUtil.encodeQuery(URI_NS + datasetId));
		} catch (URIException e) {
			e.printStackTrace();
		}
		return datasetResource;
	}

	public static Resource createSubmissionRecordJoinResource(String submissionRecordJoin) {
		Resource submissionRecordJoinResource = null;

		try {
			submissionRecordJoinResource =
					ResourceFactory.createResource(URIUtil.encodeQuery(SJI_NS + submissionRecordJoin));
		} catch (URIException e) {
			e.printStackTrace();
		}

		return submissionRecordJoinResource;
	}

	public static Resource createDatasetResource(AbstractDataset dataset) {
		return createDatasetResource(generateName(dataset));
	}

	// Delete Me
	// public static Property createDatasetProperty(Dataset dataset)
	// {
	//
	// Property datasetProperty = null;
	//
	// try
	// {
	// datasetProperty = ResourceFactory.createProperty(URIUtil.encodeQuery(URI_NS + dataset.getName()));
	// }
	// catch (URIException e)
	// {
	// // TODO Auto-generated catch block
	// e.printStackTrace();
	// }
	// return datasetProperty;
	// }
}
