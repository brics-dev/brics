package gov.nih.tbi.constants;

import java.text.DateFormat;
import java.text.SimpleDateFormat;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.NodeFactory;
import com.hp.hpl.jena.sparql.core.Var;

public class ReportingConstants {

	/********************* Errors ****************************/

	/********************* Prefixes ********************************/

	public static final String NINDS_URI = "http://ninds.nih.gov/";
	public static final String DICTIONARY_URI = NINDS_URI + "dictionary/ibis/1.0/";
	public static final String REPOSITORY_URI = NINDS_URI + "repository/fitbir/1.0/";

	/**************************************************************/
	public static final Node FORM_PROPERTY_TITLE =
			NodeFactory.createURI("http://ninds.nih.gov/dictionary/ibis/1.0/FormStructure/title");
	public static final Var RG_VAR = Var.alloc("rg");
	public static final Var ROW_VAR = Var.alloc("row");
	public static final Var GUID_VAR = Var.alloc("guid");
	public static final Var GUID_N_VAR = Var.alloc("guidN");
	public static final Var STUDY_VAR = Var.alloc("study");
	public static final String ROW_URI = "rowUri";
	public static final Var SUBMISSION_VAR = Var.alloc("submission");
	public static final Var SUBMISSION_NODE_VAR = Var.alloc("submissionNode");
	public static final Var DATASET_NAME_VAR = Var.alloc("datasetName");
	public static final Var STUDY_ID_VAR = Var.alloc("studyId");
	public static final Var STUDY_TITLE_VAR = Var.alloc("study");
	public static final Var PREFIXED_ID_VAR = Var.alloc("prefixedId");
	public static final Node DATA_ELEMENT_CLASS = NodeFactory.createURI(DICTIONARY_URI + "Element");
	public static final Node ROW_STUDY =
			NodeFactory.createURI("http://ninds.nih.gov/repository/fitbir/1.0/Dataset/studyTitle");
	public static final Node ROW_DATASET_NAME =
			NodeFactory.createURI("http://ninds.nih.gov/repository/fitbir/1.0/Dataset/datasetName");
	public static final Node ROW_PREFIX =
			NodeFactory.createURI("http://ninds.nih.gov/repository/fitbir/1.0/Dataset/prefixedId");
	public static final Node HAS_INSTANCED_REPEATABLE_GROUP =
			NodeFactory.createURI("http://ninds.nih.gov/dictionary/ibis/1.0/FormStructure/hasRepeatableGroupInstance");
	public static final Node REPEATABLE_GROUP_PROP_NAME_N =
			NodeFactory.createURI("http://ninds.nih.gov/repository/fitbir/1.0/RepeatableGroup/name");
	public static final Node ROW_SUBMISSION =
			NodeFactory.createURI("http://ninds.nih.gov/repository/fitbir/1.0/Dataset/submissionRecordJoinId");
	public static final Node ROW_DATASET =
			NodeFactory.createURI("http://ninds.nih.gov/repository/fitbir/1.0/Dataset/datasetId");
	public static final Var DATASET_ID_VAR = Var.alloc("dsId");
	public static final Var DATASET_IDS_VAR = Var.alloc("dsIds");
	public static final Var STUDY_PREFIXED_ID_VAR = Var.alloc("studyPrefixedId");
	public static final Node ROW_GUID =
			NodeFactory.createURI("http://ninds.nih.gov/dictionary/ibis/1.0/FormStructure/guid");
	public static final Node DATA_ELEMENT_PROP_NAME_N = NodeFactory.createURI(DATA_ELEMENT_CLASS + "/elementName");
	public static final String FORM_STRUCTURE_NAMESPACE = DICTIONARY_URI + "FormStructure/";
	public static final String HAS_REPEATABLE_GROUP_INSTANCE = FORM_STRUCTURE_NAMESPACE + "hasRepeatableGroupInstance";
	public static final Node HAS_REPEATABLE_GROUP_INSTANCE_N = NodeFactory.createURI(HAS_REPEATABLE_GROUP_INSTANCE);
	public static final Var REPEATABLE_GROUP_VARIABLE = Var.alloc("repeatableGroup");
	public static final Var REPEATABLE_GROUP_NAME_VARIABLE = Var.alloc("repeatableGroupName");
	public static final Var INSTANCED_REPEATABLE_GROUP_VARIABLE = Var.alloc("instancedRepeatableGroup");
	public static final Var DATA_ELEMENT_VARIABLE = Var.alloc("dataElement");
	public static final Var DATA_ELEMENT_NAME_VARIABLE = Var.alloc("dataElementName");
	public static final Var COUNT_VARIABLE = Var.alloc("count");
	public static final Var VALUE_VARIABLE = Var.alloc("value");
	public static final Var ROW_VARIABLE = Var.alloc("row");
	public static final Var DATASET_ID_VARIABLE = Var.alloc("datasetId");
	public static final String INSTANCED_ROW_PROPERTY_DATASET =
			"http://ninds.nih.gov/repository/fitbir/1.0/Dataset/datasetId";
	public static final Node INSTANCED_ROW_PROPERTY_DATASET_NODE =
			NodeFactory.createURI(INSTANCED_ROW_PROPERTY_DATASET);
	public static final int DEFAULT_ROWS_TO_DISPLAY = 10;
	public static final String SUBJECT_INFORMATION_PREFIX = "Subject Information";
	public static final String SAMPLE_INFORMATION_PREFIX = "Sample Information";
	public static final int NUM_OF_HARDCODED_COLUMNS = 2;
	public static final String DICTIONARY_CONSTRUCT =
			"construct { ?s ?p ?o } \nwhere {\n?s <http://www.w3.org/2000/01/rdf-schema#subClassOf> ?class .\n?s ?p ?o . \nFILTER(?class = <http://ninds.nih.gov/repository/fitbir/1.0/Study> || ?class = <http://ninds.nih.gov/dictionary/ibis/1.0/FormStructure> || ?class = <http://ninds.nih.gov/dictionary/ibis/1.0/Element>)\n}";
	public static final String[] ADDITIONAL_COLUMNS = {"Study", "Dataset ID"};
	// hash maps of sparql variable names to proper readable names from ADDITIONAL_COLUMNS
	public static final String PREFIX = "PREFIX ";
	public static final String MULTI_SELECT_DELIMIT = ";";
	public static final DateFormat FILTER_DATE_FORMATTER = new SimpleDateFormat("yyyy-MM-dd");
	public static final String NL = System.getProperty("line.separator");
	public static final String WS = " ";
	public final static String EMPTY_STRING = "";
	public final static String FORM_STRUCTURE_URI = DICTIONARY_URI + "FormStructure";
	public final static String STUDY_URI = REPOSITORY_URI + "Study";
	public final static String DATAELEMENT_URI = "<" + DICTIONARY_URI + "Element>";
	// added to fix problems when putting this as a string into an rdf query that post-wraps with brackets
	public final static String DE_URI = DICTIONARY_URI + "Element";
	public final static Node DATA_ELEMENT_NODE = NodeFactory.createURI(DATAELEMENT_URI);
	public final static String FORM_PREFIX = "form_";
	public final static String SUBCLASSOF = "rdfs:subClassOf";
	public final static String MISC_FIELDS[] = {"Study", "Dataset", "Submission"};

	public static final String TEXT_SEARCH_STUDY_PREDICATE_KEY = "textSearch.study.predicates";
	public static final String TEXT_SEARCH_FORM_PREDICATE_KEY = "textSearch.form.predicates";
	public static final String TEXT_SEARCH_STUDY_PREDICATE_VALUE =
			"http://www.w3.org/2000/01/rdf-schema#label^http://ninds.nih.gov/repository/fitbir/1.0/Study/title^http://ninds.nih.gov/repository/fitbir/1.0/Study/abstract^http://ninds.nih.gov/repository/fitbir/1.0/Study/principalInvestigator^http://ninds.nih.gov/repository/fitbir/1.0/Study/principalInvestigatorEmail^http://ninds.nih.gov/repository/fitbir/1.0/Study/clinicalTrial^http://ninds.nih.gov/repository/fitbir/1.0/Study/grant";
	public static final String TEXT_SEARCH_FORM_PREDICATE_VALUE =
			"http://www.w3.org/2000/01/rdf-schema#label^http://ninds.nih.gov/dictionary/ibis/1.0/FormStructure/title^http://ninds.nih.gov/dictionary/ibis/1.0/FormStructure/shortName^http://ninds.nih.gov/dictionary/ibis/1.0/FormStructure/description";
	public static final String STRING_TYPE = "xsd:string";
	public static final String NUMERIC_TYPE = "xsd:decimal";
	public static final String GUID_TYPE = "GUID";
	public static final String OPTIONAL_START = "OPTIONAL { ";
	public static final String END_BRACKET = " }";
	public static final String NUMERIC_DE_TYPE = "Numeric Values";
	public static final boolean IS_INSTANCED_COLUMN_LIMITED = true;
	public static final String RG_EXACTLY = "EXACTLY";
	public static final String RG_LESSTHAN = "LESSTHAN";
	public static final String RG_MORETHAN = "MORETHAN";
	public static final String SUCCESS = "success";
	public static final String ERROR = "error";
	public static final int INSTANCED_DATA_COLUMN_LIMIT = 30; // controls the maximum number of columns allowed in an
																 // instanced data view
	public static final int INSTANCED_JOINED_DATA_COLUMN_LIMIT = 15;
	public static final int DOWNLOAD_DIRECTORY_FORM_LIMIT = 10;
	public static final String JOIN_SEPARATOR = "_join_";
	public static final String STUDY_COLUMN_VAR = "?study";
	public static final String DOUBLE_QUOTE = "\"";
	public static final String DATASET_COLUMN_VAR = "?prefixedId";
	public static final String BIOSAMPLE_TYPE = "Biosample";
	public static final String BIOSAMPLE_CELL_PREFIX = "tbiosample";
	public static final Node VISIT_TYPE_URI =
			NodeFactory.createURI("http://ninds.nih.gov/dictionary/ibis/1.0/Element/DataElement/VisitTypPDBP");
	public static final Var VISIT_TYPE_VAR = Var.alloc("visitType");

	public static final Node STUDY_PROPERTY_PREFIXED_ID =
			NodeFactory.createURI("http://ninds.nih.gov/repository/fitbir/1.0/Study/prefixedId");

	/********************* Queries *********************************/
	public static final String INSTANCED_DATA_QUERY = "SELECT ?row ?rgName ?deName ?v\n" + "WHERE {\n"
			+ "<%s> fs:hasRepeatableGroup ?rg .\n" + "?rg rg:name ?rgName .\n" + "?rg rg:hasDataElement ?de .\n"
			+ "?de element:elementName ?deName .\n" + "?row ?de ?v .\n";

	public static final String FORM_FILTER_PATTERN = "?row a ?uri .\n" + "?row fs:hasRepeatableGroupInstance ?rg .\n";
	public static final String STUDY_FILTER_PATTERN =
			"?uri study:facetedForm ?form .\n" + "?row a ?form .\n" + "?row fs:hasRepeatableGroupInstance ?rg .\n";

	public static final String ASCENDING = "ASC";
	public static final String DESCENDING = "DESC";
	public static final String GUID_COLUMN_VAR = "?guid";
	public static final String GREYED_FLAG = "%greydis%";
	public static final Var DATASET_VAR = Var.alloc("dataset");
	public static final Var FORM_VAR = Var.alloc("form");

	public final static String HEXES = "0123456789abcdef";
	public final static int EXPIRE_ACCOUNT_AFTER_DAYS = 60;
	public final static String LOGIN_FAILURE_DEFAULT = "/login.jsp?login_error=1";
	public final static String LOGIN_FAILURE_EXPIRED =
			"/publicAccounts/passwordRecoveryAction!input.action?login_error=1";

	public static final String RDF_CONNECTION = "rdfConnection";

	public static final String FACETED_DE_URI = "http://ninds.nih.gov/repository/fitbir/1.0/Study/facetedDE";

	public static final String[] deFacetConfig =
			{"Data Elements", "<http://ninds.nih.gov/repository/fitbir/1.0/Study/facetedDE>",
					"<http://ninds.nih.gov/dictionary/ibis/1.0/FormStructure>"};


	/***** Derived Data ****/

	public static final Var VISIT_TYPE_RG_INSTANCE_VAR = Var.alloc("visitTypeRgInstance");
	public static final Var IS_DERIVED_VAR = Var.alloc("isDerived");
	public static final Node ROW_IS_DERIVED =
			NodeFactory.createURI("http://ninds.nih.gov/repository/fitbir/1.0/Dataset/isDerived");


	/***** Downloading cart, copied from ServiceConstants ****/

	public static final String NAME_READABLE = "variable name";
	public static final String TITLE_READABLE = "title";
	public static final String SHORT_DESCRIPTION_READABLE = "short description";
	public static final String PERMISSIBLE_VALUES_READABLE = "permissible values";
	public static final String PERMISSIBLE_VALUES_DESCRIPTION_READABLE = "permissible value descriptions";
	public static final String PERMISSIBLE_VALUES_OUTPUT_CODES_READABLE = "permissible value output codes";

	public static final String QT_PV_MAPPING_FILE = "pv_mapping";
	public static final String META_STUDY_FILE_PATH = "metaStudy/";

	public static final String SFTP_NAME = "sftp";
	public static final String STRICT_HOST_KEY_CHECK = "StrictHostKeyChecking";
	public static final String NO = "no";
	public static final String FILE_SEPARATER = "/";

}
