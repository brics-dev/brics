
package gov.nih.tbi.commons.util;

import gov.nih.tbi.CoreConstants;
import gov.nih.tbi.ModelConstants;
import gov.nih.tbi.query.model.Facet;

import java.util.ArrayList;
import java.util.List;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.NodeFactory;
import com.hp.hpl.jena.sparql.core.Var;

public class RDFConstants
{

    public static final String GRAPH_URI = "<http://ninds.nih.gov:8080/allTriples.ttl>";
    public static final String FILTER = "FILTER";

    /** PREFIX CONSTRUCTION **/
    public static final String BASE_URI = "http://ninds.nih.gov/";
    public static final String DICTIONARY = BASE_URI + "dictionary/ibis/1.0/";
    public static final String REPOSITORY = BASE_URI + "repository/fitbir/1.0/";
    public static final String BRICS = BASE_URI + "dictionary/1.0/brics";
    public static final String FORM_STRUCTURE = DICTIONARY + "FormStructure";
    public static final String DISEASE_ELEMENT = DICTIONARY + "DiseaseElement";
    public static final String CLASSIFICATION_ELEMENT = DICTIONARY + "ClassificationElement";
    public static final String CLASSIFICATION = DICTIONARY + "Classification";
    public static final String STUDY = REPOSITORY + "Study";
    public static final String REPEATABLE_GROUP = REPOSITORY + "RepeatableGroup";
    public static final String ELEMENT = DICTIONARY + "Element";
    public static final String SUBDOMAIN = DICTIONARY + "Subdomain";
    public static final String DATASET = REPOSITORY + "Dataset";
    public static final String DATA_ELEMENT = ELEMENT + "";
    public static final String COMMON_DATA_ELEMENT = ELEMENT + "/CommonDataElement";

    public static final String KEYWORD = DICTIONARY + "Keyword";
    public static final String LABEL = DICTIONARY + "Label";
    public static final String EXTERNAL_ID = DICTIONARY + "ExternalId";
    public static final String TRUE = "1";
    
    public static final String FS_STANDARDIZATION_URI = "http://ninds.nih.gov/dictionary/ibis/1.0/FormStructure/Standardization/";
    public static final String FS_REQUIRED_URI = "http://ninds.nih.gov/dictionary/ibis/1.0/FormStructure/Required/";

    public static final String FS_LABEL = FORM_STRUCTURE + "/label";
    public static final String FS_LABEL_URI = FS_LABEL + "/";

    /** Namespaces **/
    public static final String CLASSIFICATION_NS = CLASSIFICATION + "/";
    public static final String EXTERNAL_ID_NS = EXTERNAL_ID + "/";
    public static final String DISEASE_ELEMENT_NS = DISEASE_ELEMENT + "/";

    public static final String DOMAIN_PAIR = DISEASE_ELEMENT_NS + "DomainPair";

    /**
     * @deprecated Old model node. Use RDFConstants.FORM_STRUCTURE_NS_N instead.
     */
    @Deprecated
    public static final String FORM_STRUCTURE_NS = FORM_STRUCTURE + "/";

    public static final String FORM_STRUCTURE_NS_N = FORM_STRUCTURE + "#";
    public static final String BRICS_NS_N = BRICS + "#";
    public static final String STUDY_NS = STUDY + "/";
    public static final String REPEATABLE_GROUP_NS = REPEATABLE_GROUP + "/";

    public static final String PERMISSIBLE_VALUE = DICTIONARY + "PermissibleValue";
    public static final String PERMISSIBLE_VALUE_NS = PERMISSIBLE_VALUE + "/";
    public static final String ELEMENT_NS = ELEMENT + "/";
    public static final String ELEMENT_NS_N = ELEMENT + "#";
    public static final String SUBDOMAIN_NS = SUBDOMAIN + "/";
    public static final String SUBDOMAIN_NS_N = SUBDOMAIN + "#";
    public static final String DATASET_NS = DATASET + "/";
    public static final String DATA_ELEMENT_NS = DATA_ELEMENT + "/";
    public static final String DOMAIN_PAIR_NS = DOMAIN_PAIR + "/";
    public static final String CLASSIFICATION_ELEMENT_NS = CLASSIFICATION_ELEMENT + "/";
    public static final String CLASSIFICATION_NS_N = CLASSIFICATION + "#";
    /**
     * Used for elements
     */
    public static final String KEYWORD_NS = KEYWORD + "/";
    public static final String LABEL_NS = LABEL + "/";
    /**
     * Used for attributes
     */
    public static final String KEYWORD_NS_N = KEYWORD + "#";

    /** Prefix names */
    public static final String ELEMENT_PREFIX = "element";
    public static final String STUDY_PREIX = "study";
    public static final String REPEATABLE_GROUP_PREFIX = "rg";
    public static final String DATASET_PREFIX = "dataset";
    public static final String FORM_STRUCTURE_PREFIX = "fs";

    /** RDF Standard Namespaces **/
    public static final String RDFS_PREFIX = "rdfs";
    public static final String RDFS_NS = "http://www.w3.org/2000/01/rdf-schema";

    /** Prefix Builders **/
    public static final String[][] PREFIXES_ARR = { { RDFS_PREFIX, RDFS_NS + "#" },
            { "xsd", "http://www.w3.org/2001/XMLSchema#" }, { FORM_STRUCTURE_PREFIX, FORM_STRUCTURE_NS },
            { ELEMENT_PREFIX, ELEMENT_NS }, { STUDY_PREIX, STUDY_NS },
            { REPEATABLE_GROUP_PREFIX, REPEATABLE_GROUP_NS }, { DATASET_PREFIX, DATASET_NS } };

    public static String PREFIXES = ModelConstants.EMPTY_STRING;

    static
    {
        /** RDF PREFIXES **/
        StringBuffer prefixSb = new StringBuffer();
        for (String[] prefix : PREFIXES_ARR)
        {
            prefixSb.append("PREFIX ").append(prefix[0]).append(":<").append(prefix[1]).append(">")
                    .append(CoreConstants.NL);
        }

        PREFIXES = prefixSb.toString();
    }

    /** Query Syntaxes **/
    public final static String VARIABLE_MARK = "?";
    public final static String FILTER_URI_VARIABLE = "?filterUri";
    public final static String URI_VARIABLE_NAME = "uri";
    public final static String URI_VARIABLE = VARIABLE_MARK + URI_VARIABLE_NAME;
    public final static Var URI_NODE = Var.alloc(URI_VARIABLE_NAME);
    public final static String VALUES = "VALUES ";
    public final static String SELECT_DISTINCT = "SELECT DISTINCT ";

    /** Variable Nodes **/
    public final static Var FORM_STRUCTURE_VARIABLE = Var.alloc("formStructure");
    public final static Var REPEATABLE_GROUP_VARIABLE = Var.alloc("repeatableGroup");
    public final static Var DATA_ELEMENT_VARIABLE = Var.alloc("dataElement");
    public final static Var VALUE_RANGE_VARIABLE = Var.alloc("valueRange");
    public final static Var DISEASE_ELEMENT_VARIABLE = Var.alloc("diseaseElement");
    public final static Var STATUS_VARIABLE = Var.alloc("status");
    /** Class URI Nodes **/
    public final static Node FORM_STRUCTURE_NODE = NodeFactory.createURI(FORM_STRUCTURE);
    public final static Node REPEATABLE_GROUP_NODE = NodeFactory.createURI(REPEATABLE_GROUP);
    public final static Node DATA_ELEMENT_NODE = NodeFactory.createURI(DATA_ELEMENT);

    /*** BEAN FIELDS ***/
    // public final static List<RDFBeanField> FORM_FIELDS = new ArrayList<RDFBeanField>();
    // public final static List<RDFBeanField> STUDY_FIELDS = new ArrayList<RDFBeanField>();
    // public final static List<RDFBeanField> REPEATABLE_GROUP_FIELDS = new ArrayList<RDFBeanField>();
    // public final static List<RDFBeanField> ELEMENT_FIELDS = new ArrayList<RDFBeanField>();

    /** BEAN CONFIGs ***/
    // public final static RDFBeanConfig FORM_STRUCTURE_CONFIG = new RDFBeanConfig(FORM_STRUCTURE, FORM_FIELDS);
    // public final static RDFBeanConfig STUDY_CONFIG = new RDFBeanConfig(STUDY, STUDY_FIELDS);
    // public final static RDFBeanConfig REPEATABLE_GROUP_CONFIG = new RDFBeanConfig(REPEATABLE_GROUP,
    // REPEATABLE_GROUP_FIELDS);
    // public final static RDFBeanConfig DATA_ELEMENT_CONFIG = new RDFBeanConfig(ELEMENT, ELEMENT_FIELDS);
    public final static List<Facet> QT_FACETS = new ArrayList<Facet>();

    /** Property URIs ***************************************************************/
    /** Form Structures **/
    /**
     * @deprecated Old model node. Use RDFConstants.PROPERTY_BRICS_SHORT_NAME_N instead.
     */
    @Deprecated
    public final static String PROPERTY_FS_SHORT_NAME = FORM_STRUCTURE_NS + "shortName";
    /**
     * @deprecated Old model node. Use RDFConstants.PROPERTY_BRICS_TITLE_N instead.
     */
    @Deprecated
    public final static String PROPERTY_FS_TITLE = FORM_STRUCTURE_NS + "title";
    /**
     * @deprecated Old model node. Use RDFConstants.PROPERTY_BRICS_DESCRIPTION_N instead.
     */
    @Deprecated
    public final static String PROPERTY_FS_DESCRIPTION = FORM_STRUCTURE_NS + "description";
    /**
     * @deprecated Old model node. Use RDFConstants.PROPERTY_BRICS_VERSION_N instead.
     */
    @Deprecated
    public final static String PROPERTY_FS_VERSION = FORM_STRUCTURE_NS + "version";
    /**
     * @deprecated Old model node. Use RDFConstants.PROPERTY_FS_DISEASE_N instead.
     */
    @Deprecated
    public final static String PROPERTY_FS_DISEASE = FORM_STRUCTURE_NS + "disease";

    public final static String PROPERTY_FS_HAS_REPEATABLE_GROUP = FORM_STRUCTURE_NS + "hasRepeatableGroup";
    /**
     * @deprecated old model node. use RDFConstants.PROPERTY_FS_ORGANIZATION_N instead.
     */
    @Deprecated
    public final static String PROPERTY_FS_ORGANIZATION = FORM_STRUCTURE_NS + "organization";
    public final static String PROPERTY_FS_ORGANIZATION_N = FORM_STRUCTURE_NS_N + "organization";
    public final static String PROPERTY_FS_SUBMISSION_TYPE_N = FORM_STRUCTURE_NS_N + "submissionType";
    public final static String PROPERTY_FS_STANDARDIZATION_TYPE_N = FORM_STRUCTURE_NS_N + "standardization";  //--------------------------------------------------------------------------------------------------------------------------------------
    public final static String PROPERTY_FS_REQUIRED_TYPE_N = FORM_STRUCTURE_NS_N + "required";
    public final static String PROPERTY_FS_LABEL_N = FORM_STRUCTURE_NS_N + "label";
    public final static String PROPERTY_FS_LABEL_ID_N = FS_LABEL + "#id";;
    public final static String PROPERTY_FS_MODIFIED_DATE_N = FORM_STRUCTURE_NS_N + "modifiedDate";
    public final static String PROPERTY_FS_ID = FORM_STRUCTURE_NS + "formStructureId";
    public final static String PROPERTY_FS_DISEASE_N = FORM_STRUCTURE_NS_N + "disease";
    public final static String PROPERTY_FS_DATE_CREATED_N = FORM_STRUCTURE_NS_N + "dateCreated";
    public final static String PROPERTY_FS_CREATED_BY_N = FORM_STRUCTURE_NS_N + "createdBy";
    public final static String PROPERTY_FS_IS_COPYRIGHTED_N = FORM_STRUCTURE_NS_N + "isCopyrighted";

    public final static String PROPERTY_BRICS_SHORT_NAME_N = BRICS_NS_N + "shortName";
    public final static String PROPERTY_BRICS_LATEST_N = BRICS_NS_N + "latest";
    public final static String PROPERTY_BRICS_TYPE_N = BRICS_NS_N + "type";
    public final static String PROPERTY_BRICS_VERSION_N = BRICS_NS_N + "version";
    public final static String PROPERTY_BRICS_DESCRIPTION_N = BRICS_NS_N + "description";
    public final static String PROPERTY_BRICS_STATUS_N = BRICS_NS_N + "status";
    public final static String PROPERTY_BRICS_TITLE_N = BRICS_NS_N + "title";

    public final static Node PROPERTY_FS_SUBMISSION_TYPE_NODE_N = NodeFactory.createURI(PROPERTY_FS_SUBMISSION_TYPE_N);
    public final static Node PROPERTY_FS_ORGANIZATION_NODE_N = NodeFactory.createURI(PROPERTY_FS_ORGANIZATION_N);
    public final static Node PROPERTY_FS_MODIFIED_DATE_NODE_N = NodeFactory.createURI(PROPERTY_FS_MODIFIED_DATE_N);
    public final static Node PROPERTY_FS_CREATED_BY_NODE_N = NodeFactory.createURI(PROPERTY_FS_CREATED_BY_N);
    public final static Node PROPERTY_FS_IS_COPYRIGHTED_NODE_N = NodeFactory.createURI(PROPERTY_FS_IS_COPYRIGHTED_N);
    /**
     * @deprecated Old model node. Use RDFConstants.PROPERTY_BRICS_SHORT_NAME_NODE_N instead.
     */
    @Deprecated
    public final static Node PROPERTY_FS_SHORT_NAME_NODE = NodeFactory.createURI(PROPERTY_FS_SHORT_NAME);
    /**
     * @deprecated Old model node. Use RDFConstants.PROPERTY_BRICS_DESCRIPTION_NODE_N instead.
     */
    @Deprecated
    public final static Node PROPERTY_FS_DESCRIPTION_NODE = NodeFactory.createURI(PROPERTY_FS_DESCRIPTION);
    /**
     * @deprecated Old model node. Use RDFConstants.PROPERTY_BRICS_VERSION_NODE_N instead.
     */
    @Deprecated
    public final static Node PROPERTY_FS_VERSION_NODE = NodeFactory.createURI(PROPERTY_FS_VERSION);
    /**
     * @deprecated Old model node. Use RDFConstants.PROPERTY_BRICS_TITLE_NODE_N instead.
     */
    @Deprecated
    public final static Node PROPERTY_FS_TITLE_NODE = NodeFactory.createURI(PROPERTY_FS_TITLE);

    /**
     * @deprecated Old model node. Use RDFConstants.PROPERTY_FS_DISEASE_NODE_N instead.
     */
    @Deprecated
    public final static Node PROPERTY_FS_DISEASE_NODE = NodeFactory.createURI(PROPERTY_FS_DISEASE);

    public final static Node PROPERTY_BRICS_SHORT_NAME_NODE_N = NodeFactory.createURI(PROPERTY_BRICS_SHORT_NAME_N);
    public final static Node PROPERTY_BRICS_LATEST_NODE_N = NodeFactory.createURI(PROPERTY_BRICS_LATEST_N);
    public final static Node PROPERTY_BRICS_TYPE_NODE_N = NodeFactory.createURI(PROPERTY_BRICS_TYPE_N);
    public final static Node PROPERTY_BRICS_DESCRIPTION_NODE_N = NodeFactory.createURI(PROPERTY_BRICS_DESCRIPTION_N);
    public final static Node PROPERTY_BRICS_STATUS_NODE_N = NodeFactory.createURI(PROPERTY_BRICS_STATUS_N);
    public final static Node PROPERTY_BRICS_VERSION_NODE_N = NodeFactory.createURI(PROPERTY_BRICS_VERSION_N);
    public final static Node PROPERTY_BRICS_TITLE_NODE_N = NodeFactory.createURI(PROPERTY_BRICS_TITLE_N);
    public final static Node PROPERTY_FS_DISEASE_NODE_N = NodeFactory.createURI(PROPERTY_FS_DISEASE_N);
    public final static Node PROPERTY_FS_DATE_CREATED_NODE_N = NodeFactory.createURI(PROPERTY_FS_DATE_CREATED_N);
    public final static Node PROPERTY_FS_STANDARDIZATION_NODE_N = NodeFactory.createURI(PROPERTY_FS_STANDARDIZATION_TYPE_N); //-------------------------------------------------------------------------------------------------------------------------
    public final static Node PROPERTY_FS_REQUIRED_NODE_N = NodeFactory.createURI(PROPERTY_FS_REQUIRED_TYPE_N);
    public final static Node PROPERTY_FS_LABEL_NODE_N = NodeFactory.createURI(PROPERTY_FS_LABEL_N);
    public final static Node PROPERTY_FS_LABEL_ID_NODE_N = NodeFactory.createURI(PROPERTY_FS_LABEL_ID_N);
     
    /** Studies **/
    public final static String PROPERTY_STUDY_ABSTRACT = STUDY_NS + "abstract";
    public final static String PROPERTY_STUDY_PI = STUDY_NS + "principalInvestigator";

    /** Repeatable Groups **/
    public final static String PROPERTY_RG_NAME = REPEATABLE_GROUP_NS + "name";
    public final static String PROPERTY_RG_THRESHOLD = REPEATABLE_GROUP_NS + "threshold";
    public final static String PROPERTY_RG_POSITION = REPEATABLE_GROUP_NS + "position";
    public final static String PROPERTY_RG_TYPE = REPEATABLE_GROUP_NS + "type";

    /** External ID **/
    public final static Node PROPERTY_EXTERNAL_ID_VALUE = NodeFactory.createURI(EXTERNAL_ID_NS + "value");
    public final static Node PROPERTY_EXTERNAL_ID_TYPE = NodeFactory.createURI(EXTERNAL_ID_NS + "type");

    /** Subdomain Element **/
    public final static Node PROPERTY_SUBDOMAIN_DISEASE_N = NodeFactory.createURI(SUBDOMAIN_NS_N + "disease");
    public final static Node PROPERTY_SUBDOMAIN_DOMAIN_N = NodeFactory.createURI(SUBDOMAIN_NS_N + "domain");
    public final static Node PROPERTY_SUBDOMAIN_SUBDOMAIN_N = NodeFactory.createURI(SUBDOMAIN_NS_N + "subdomain");

    /** Classification Element **/
    public final static Node PROPERTY_CLASSIFICATION_DISEASE_N = NodeFactory.createURI(CLASSIFICATION_NS_N + "disease");
    public final static Node PROPERTY_CLASSIFICATION_SUBGROUP_N = NodeFactory.createURI(CLASSIFICATION_NS_N
            + "subdisease");
    public final static Node PROPERTY_CLASSIFICATION_VALUE_N = NodeFactory.createURI(CLASSIFICATION_NS_N + "value");
    public final static Node PROPERTY_CLASSIFICATION_ELEMENT_CLASSIFICATION = NodeFactory
            .createURI(CLASSIFICATION_ELEMENT_NS + "value");
    public final static Node PROPERTY_CLASSIFICATION_ELEMENT_SUBGROUP = NodeFactory.createURI(CLASSIFICATION_ELEMENT_NS
            + "subGroup");

    /** Classification **/
    public final static Node PROPERTY_CLASSIFICATION_CAN_CREATE = NodeFactory
            .createURI(CLASSIFICATION_NS + "canCreate");

    /** Keyword **/
    public final static Node PROPERTY_KEWORD_COUNT = NodeFactory.createURI(KEYWORD_NS + "count");
    public final static Node PROPERTY_LABEL_COUNT = NodeFactory.createURI(LABEL_NS + "count");

    /** Domain Pair **/
    public final static String PROPERTY_DOMAIN_PAIR_DOMAIN = DOMAIN_PAIR_NS + "domain";
    public final static String PROPERTY_DOMAIN_PAIR_SUB_DOMAIN = DOMAIN_PAIR_NS + "subdomain";

    public final static Node PROPERTY_DOMAIN_PAIR_DOMAIN_NODE = NodeFactory.createURI(PROPERTY_DOMAIN_PAIR_DOMAIN);
    public final static Node PROPERTY_DOMAIN_PAIR_SUB_DOMAIN_NODE = NodeFactory
            .createURI(PROPERTY_DOMAIN_PAIR_SUB_DOMAIN);
    /** Disease Element **/
    public final static String PROPERTY_DISEASE_ELEMENT_DISEASE = DISEASE_ELEMENT_NS + "disease";
    public final static String PROPERTY_DISEASE_ELEMENT_DOMAIN_PAIR = DISEASE_ELEMENT_NS + "domainPair";

    public final static Node PROPERTY_DISEASE_ELEMENT_DISEASE_NODE = NodeFactory
            .createURI(PROPERTY_DISEASE_ELEMENT_DISEASE);
    public final static Node PROPERTY_DISEASE_ELEMENT_DOMAIN_PAIR_NODE = NodeFactory
            .createURI(PROPERTY_DISEASE_ELEMENT_DOMAIN_PAIR);

    /** Data Elements **/
    public final static Node PROPERTY_ELEMENT_PERMISSIBLE_VALUES = NodeFactory.createURI(ELEMENT_NS
            + "permissibleValues");
    public final static String PROPERTY_ELEMENT_PERMISSIBLE_VALUE = ELEMENT_NS + "permissibleValue";

    public final static Node PROPERTY_ELEMENT_PERMISSIBLE_VALUE_N = NodeFactory.createURI(ELEMENT_NS_N
            + "permissibleValue");
    @Deprecated
    public final static String PROPERTY_ELEMENT_TYPE = ELEMENT_NS + "elementType";
    public final static String PROPERTY_ELEMENT_MAX_VALUE = ELEMENT_NS + "maximumValue";
    public final static String PROPERTY_ELEMENT_MIN_VALUE = ELEMENT_NS + "minimumValue";

    /**
     * @deprecated old model. use PROPERTY_BRICS_SHORT_NAME and RDFS TYPE=ELEMENT instead.
     */
    @Deprecated
    public final static Node PROPERTY_ELEMENT_NAME = NodeFactory.createURI(ELEMENT_NS + "elementName");
    public final static Node PROPERTY_ELEMENT_TITLE = NodeFactory.createURI(ELEMENT_NS + "title");
    public final static Node PROPERTY_ELEMENT_DESCRIPTION = NodeFactory.createURI(ELEMENT_NS + "description");
    public final static Node PROPERTY_ELEMENT_TYPE_NODE_N = NodeFactory.createURI(ELEMENT_NS_N + "type");
    /**
     * @deprecated old model. use PROPERTY_ELEMENT_SHORT_DESCRIPTION_NODE_N instead.
     */
    @Deprecated
    public final static Node PROPERTY_ELEMENT_SHORT_DESCRIPTION = NodeFactory
            .createURI(ELEMENT_NS + "shortDescription");
    public final static Node PROPERTY_ELEMENT_SHORT_DESCRIPTION_NODE_N = NodeFactory.createURI(ELEMENT_NS_N
            + "shortDescription");
    public final static Node PROPERTY_ELEMENT_CATEGORY_NODE_N = NodeFactory.createURI(ELEMENT_NS_N + "category");
    public final static Node PROPERTY_ELEMENT_CATEGORY_URI_NODE_N = NodeFactory.createURI(ELEMENT_NS_N + "categoryUri");
    public final static Node PROPERTY_ELEMENT_SUBMITTING_CONTACT_INFO_NODE_N = NodeFactory.createURI(ELEMENT_NS_N
            + "submittingContactInfo");
    public final static Node PROPERTY_ELEMENT_SUBMITTING_CONTACT_NAME_NODE_N = NodeFactory.createURI(ELEMENT_NS_N
            + "submittingContactName");
    public final static Node PROPERTY_ELEMENT_SUBMITTING_ORG_NAME_NODE_N = NodeFactory.createURI(ELEMENT_NS_N
            + "submittingOrgName");
    public final static Node PROPERTY_ELEMENT_STEWARD_CONTACT_INFO_NODE_N = NodeFactory.createURI(ELEMENT_NS_N
            + "stewardContactInfo");
    public final static Node PROPERTY_ELEMENT_STEWARD_CONTACT_NAME_NODE_N = NodeFactory.createURI(ELEMENT_NS_N
            + "stewardContactName");
    public final static Node PROPERTY_ELEMENT_STEWARD_ORG_NAME_NODE_N = NodeFactory.createURI(ELEMENT_NS_N
            + "stewardOrgName");
    public final static Node PROPERTY_ELEMENT_EFFECTIVE_DATE_NODE_N = NodeFactory.createURI(ELEMENT_NS_N
            + "effectiveDate");
    public final static Node PROPERTY_ELEMENT_UNTIL_DATE_NODE_N = NodeFactory.createURI(ELEMENT_NS_N + "untilDate");
    public final static Node PROPERTY_ELEMENT_SEE_ALSO_NODE_N = NodeFactory.createURI(ELEMENT_NS_N + "seeAlso");

    public final static Node PROPERTY_ELEMENT_FORMAT = NodeFactory.createURI(ELEMENT_NS + "format");
    /**
     * @deprecated old model. use PROPERTY_ELEMENTGUIDELINES_NODE_N instead.
     */
    @Deprecated
    public final static Node PROPERTY_ELEMENT_GUIDELINES = NodeFactory.createURI(ELEMENT_NS + "guidelines");
    public final static Node PROPERTY_ELEMENT_GUIDELINES_NODE_N = NodeFactory.createURI(ELEMENT_NS_N + "guidelines");

    /**
     * @deprecated Old model. Use PROPERTY_DE_HISTORICAL_NOTES_NODE_N
     */
    @Deprecated
    public final static Node PROPERTY_ELEMENT_HISTORICAL_NOTES = NodeFactory.createURI(ELEMENT_NS + "historicalNotes");
    public final static Node PROPERTY_DE_HISTORICAL_NOTES_NODE_N = NodeFactory.createURI(ELEMENT_NS_N
            + "historicalNotes");
    /**
     * @deprecated Old model. Use PROPERTY_DE_POPULATION_NODE_N
     */
    @Deprecated
    public final static Node PROPERTY_ELEMENT_POPULATION = NodeFactory.createURI(ELEMENT_NS + "population");
    public final static Node PROPERTY_DE_POPULATION_NODE_N = NodeFactory.createURI(ELEMENT_NS_N + "population");

    /**
     * @deprecated Old model. See PROPERTY_DE_NOTES_NODE_N
     */
    @Deprecated
    public final static Node PROPERTY_ELEMENT_NOTES = NodeFactory.createURI(ELEMENT_NS + "notes");
    public final static Node PROPERTY_DE_NOTES_NODE_N = NodeFactory.createURI(ELEMENT_NS_N + "notes");

    public final static Node PROPERTY_DE_CLASSIFICATION_NODE_N = NodeFactory.createURI(ELEMENT_NS_N + "classification");
    public final static Node PROPERTY_DE_SUBDOMAIN_NODE_N = NodeFactory.createURI(ELEMENT_NS_N + "subdomain");

    /**
     * @deprecated old model. Use PROPERTY_ELEMENT_DISEASE_ELEMENT_NODE_N instead.
     */
    @Deprecated
    public final static Node PROPERTY_ELEMENT_REFERENCES = NodeFactory.createURI(ELEMENT_NS + "reference");
    public final static Node PROPERTY_ELEMENT_REFERENCES_N = NodeFactory.createURI(ELEMENT_NS_N + "reference");
    /**
     * @deprecated old model. Use PROPERTY_ELEMENT_DISEASE_ELEMENT_NODE_N instead.
     */
    @Deprecated
    public final static Node PROPERTY_ELEMENT_DISEASE_ELEMENT = NodeFactory.createURI(ELEMENT_NS + "diseaseElement");
    public final static Node PROPERTY_ELEMENT_DISEASE_ELEMENT_NODE_N = NodeFactory.createURI(ELEMENT_NS_N
            + "diseaseElement");
    /**
     * @deprecated old model. Use PROPERTY_ELEMENT_CLASSIFICATION_ELEMENT_N instead
     */
    @Deprecated
    public final static Node PROPERTY_ELEMENT_CLASSIFICATION_ELEMENT = NodeFactory.createURI(ELEMENT_NS
            + "classifications");
    public final static Node PROPERTY_ELEMENT_CLASSIFICATION_ELEMENT_N = NodeFactory.createURI(ELEMENT_NS_N
            + "classification");
    @Deprecated
    public final static Node PROPERTY_ELEMENT_KEYWORDS = NodeFactory.createURI(ELEMENT_NS + "keywords");
    public final static Node PROPERTY_ELEMENT_KEYWORDS_N = NodeFactory.createURI(ELEMENT_NS_N + "keyword");
    @Deprecated
    public final static Node PROPERTY_ELEMENT_LABELS = NodeFactory.createURI(ELEMENT_NS + "labels");
    public final static Node PROPERTY_ELEMENT_LABELS_N = NodeFactory.createURI(ELEMENT_NS_N + "label");
    public final static Node PROPERTY_ELEMENT_EXTERNAL_ID = NodeFactory.createURI(ELEMENT_NS + "externalId");

    public final static Node PROPERTY_DE_DATE_CREATED_N = NodeFactory.createURI(ELEMENT_NS_N + "dateCreated");
    public final static Node PROPERTY_DE_CREATED_BY_N = NodeFactory.createURI(ELEMENT_NS_N + "createdBy");
    public static final Node PROPERTY_DE_MODIFIED_DATE_N = NodeFactory.createURI(ELEMENT_NS_N + "modifiedDate");

    /** Variable Names **/
    public final static Var SHORT_NAME_VARIABLE = Var.alloc("shortName");
    public final static Var VERSION_VARIABLE = Var.alloc("version");
    public final static Var DISEASE_VARIABLE = Var.alloc("disease");
    public final static Var BASE_URI_VARIABLE = Var.alloc("baseUri");
    public final static Var ORGANIZATION_VARIABLE = Var.alloc("organization");
    public final static Var MODIFIED_DATE_VARIABLE = Var.alloc("modifiedDate");
    public final static Var DATE_CREATED_VARIABLE = Var.alloc("dateCreated");
    public final static Var SUBMISSION_TYPE_VARIABLE = Var.alloc("submissionType");
    public final static Var IS_COPYRIGHTED_VARIABLE = Var.alloc("isCopyrighted");
    
    public final static Var STANDARDIZATION_VARIABLE = Var.alloc("standardization");  //----------------------------------------------------------------------------------------------------------------------------------------------------
    public final static Var STANDARDIZATION_NODE_VARIABLE = Var.alloc("standardizationNode");
    public final static Var REQUIRED_VARIABLE = Var.alloc("required");
    public final static Var REQUIRED_NODE_VARIABLE = Var.alloc("requiredNode");
    
    public final static Var NAME_VARIABLE = Var.alloc("name");
    public final static Var TITLE_VARIABLE = Var.alloc("title");
    public final static Var DESCRIPTION_VARIABLE = Var.alloc("description");
    public final static Var TYPE_VARIABLE = Var.alloc("type");
    public final static Var SHORT_DESCRIPTION_VARIABLE = Var.alloc("shortDescription");
    public final static Var CATEGORY_URI_VARIABLE = Var.alloc("categoryUri");
    public final static Var CATEGORY_TITLE_VARIABLE = Var.alloc("category");
    public final static Var CATEGORY_SHORT_VARIABLE = Var.alloc("categoryShortName");
    public final static Var FORMAT_VARIABLE = Var.alloc("format");
    public final static Var NOTES_VARIABLE = Var.alloc("notes");
    public final static Var GUIDELINES_VARIABLE = Var.alloc("guidelines");
    public final static Var HISTORICAL_NOTES_VARIABLE = Var.alloc("historicalNotes");
    public final static Var REFERENCES_VARIABLE = Var.alloc("references");
    public final static Var POPULATION_VARIABLE = Var.alloc("population");
    public final static Var DOMAIN_PAIR_VARIABLE = Var.alloc("domainPair");
    public final static Var DOMAIN_VARIABLE = Var.alloc("domain");
    public final static Var ELEMENT_TYPE_VARIABLE = Var.alloc("elementType");
    public final static Var SUB_DOMAIN_VARIABLE = Var.alloc("subdomain");
    public final static Var SUB_DOMAIN_NODE_VARIABLE = Var.alloc("subdomainNode");
    public final static Var CLASSIFICATION_NODE_VARIABLE = Var.alloc("classificationNode");
    public final static Var CLASSIFICATION_ELEMENT_VARIABLE = Var.alloc("classificationElement");
    public final static Var CLASSIFICATION_URI_VARIABLE = Var.alloc("classificationURI");
    public final static Var CLASSIFICATION_VARIABLE = Var.alloc("classification");
    public final static Var IS_ACTIVE_VARIABLE = Var.alloc("isActive");
    public final static Var CAN_CREATE_VARIABLE = Var.alloc("canCreate");
    public final static Var SUBGROUP_VARIABLE = Var.alloc("subgroup");
    public final static Var KEYWORD_NODE_VARIABLE = Var.alloc("keywordNode");
    public final static Var KEYWORD_VARIABLE = Var.alloc("keyword");
    public final static Var LABEL_VARIABLE = Var.alloc("label");
    public final static Var LABEL_ID_VARIABLE = Var.alloc("labelId");
    public final static Var LABEL_NODE_VARIABLE = Var.alloc("labelNode");
    public final static Var VALUE_VARIABLE = Var.alloc("value");
    public final static Var COUNT_VARIABLE = Var.alloc("count");
    public final static Var EXTERNAL_ID_VARIABLE = Var.alloc("externalId");
    public final static Var EXTERNAL_ID_NODE_VARIABLE = Var.alloc("externalIdNode");
    public final static Var PERMISSIBLE_VALUE_VARIABLE = Var.alloc("permissibleValue");
    public final static Var PERMISSIBLE_VALUE_NODE_VARIABLE = Var.alloc("permissibleValueNode");
    public final static Var PERMISSIBLE_VALUE_DESCRIPTION_VARIABLE = Var.alloc("permissibleValueDescription");
    public final static Var SUBMITTING_CONTACT_INFO_VARIABLE = Var.alloc("submittingContactInfo");
    public final static Var SUBMITTING_CONTACT_NAME_VARIABLE = Var.alloc("submittingContactName");
    public final static Var SUBMITTING_ORG_NAME_VARIABLE = Var.alloc("submittingOrgName");
    public final static Var STEWARD_CONTACT_INFO_VARIABLE = Var.alloc("stewardContactInfo");
    public final static Var STEWARD_CONTACT_NAME_VARIABLE = Var.alloc("stewardContactName");
    public final static Var STEWARD_ORG_NAME_VARIABLE = Var.alloc("stewardOrgName");
    public final static Var EFFECTIVE_DATE_VARIABLE = Var.alloc("effectiveDate");
    public final static Var UNTIL_DATE_VARIABLE = Var.alloc("untilDate");
    public final static Var SEE_ALSO_VARIABLE = Var.alloc("seeAlso");
    public final static Var CREATED_BY_VARIABLE = Var.alloc("createdBy");

    /*** static stuff ***/
    static
    {
        QT_FACETS.add(new Facet("Studies", FORM_STRUCTURE, STUDY + "/facetedStudy"));
        QT_FACETS.add(new Facet("Forms", STUDY, STUDY + "/facetedForm"));

        // ok, bean fields store configurations for the SPARQL dao to be able to generically do lazy get's.
        // this is not unlike hibernate annotations.
        // The query bean fields needs the name of the field, the uri of the field in RDF, and the type of the field.
        // FORM_FIELDS.add(new RDFBeanField("shortName", PROPERTY_FS_SHORT_NAME, String.class));
        // FORM_FIELDS.add(new RDFBeanField("id", PROPERTY_FS_ID, Long.class));
        // FORM_FIELDS.add(new RDFBeanField("title", PROPERTY_FS_TITLE, String.class));
        // FORM_FIELDS.add(new RDFBeanField("description", PROPERTY_FS_DESCRIPTION, String.class));
        // FORM_FIELDS.add(new RDFBeanField("organization", PROPERTY_FS_ORGANIZATION, String.class));
        // FORM_FIELDS.add(new RDFBeanField("version", PROPERTY_FS_VERSION, Integer.class));
        // FORM_FIELDS.add(new QueryBeanField("repeatableGroups", "fs:hasRepeatableGroup", Set.class));
        // FORM_FIELDS.add(new QueryBeanField("studies", "study:facetedStudy", String.class));

        /** Study Fields **/
        // STUDY_FIELDS.add(new RDFBeanField("title", RDFS.Nodes.label.toString(), String.class));
        // STUDY_FIELDS.add(new RDFBeanField("abstractText", PROPERTY_STUDY_ABSTRACT, String.class));
        // STUDY_FIELDS.add(new RDFBeanField("principalInvestigator", PROPERTY_STUDY_PI, String.class));
        // STUDY_FIELDS.add(new BeanField("forms", "study:facetedForm", FormResult.class));

        // REPEATABLE_GROUP_FIELDS.add(new RDFBeanField("name", PROPERTY_RG_NAME, String.class));
        // REPEATABLE_GROUP_FIELDS.add(new RDFBeanField("threshold", PROPERTY_RG_THRESHOLD, Integer.class));
        // REPEATABLE_GROUP_FIELDS.add(new RDFBeanField("position", PROPERTY_RG_POSITION, Integer.class));
        // REPEATABLE_GROUP_FIELDS.add(new RDFBeanField("type", PROPERTY_RG_TYPE, RepeatableType.class));
        // REPEATABLE_GROUP_FIELDS.add(new QueryBeanField("mapElements", "rg:hasDataElement", Set.class));

        /** Element Fields **/
        // ELEMENT_FIELDS.add(new RDFBeanField("name", PROPERTY_ELEMENT_NAME, String.class));
        // ELEMENT_FIELDS.add(new RDFBeanField("title", PROPERTY_ELEMENT_TITLE, String.class));
        // ELEMENT_FIELDS.add(new RDFBeanField("type", PROPERTY_ELEMENT_TYPE, DataType.class));
        // ELEMENT_FIELDS.add(new RDFBeanField("maximumValue", PROPERTY_ELEMENT_MAX_VALUE, BigDecimal.class));
        // ELEMENT_FIELDS.add(new RDFBeanField("minimumValue", PROPERTY_ELEMENT_MIN_VALUE, BigDecimal.class));
        // ELEMENT_FIELDS.add(new RDFBeanField("position", PROPERTY_ELEMENT_POSITION, Integer.class));
        // ELEMENT_FIELDS.add(new RDFBeanField("description", PROPERTY_ELEMENT_DESCRIPTION, String.class));
    }
}
