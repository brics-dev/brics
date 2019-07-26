
package gov.nih.tbi.semantic;

import gov.nih.tbi.TestHibernateFactory;
import gov.nih.tbi.account.dao.EntityMapDao;
import gov.nih.tbi.account.model.hibernate.Account;
import gov.nih.tbi.account.model.hibernate.EntityMap;
import gov.nih.tbi.account.model.hibernate.PermissionGroup;
import gov.nih.tbi.account.model.hibernate.PermissionGroupMember;
import gov.nih.tbi.commons.model.DataElementStatus;
import gov.nih.tbi.commons.model.DatasetStatus;
import gov.nih.tbi.commons.model.EntityType;
import gov.nih.tbi.commons.model.InputRestrictions;
import gov.nih.tbi.commons.model.StatusType;
import gov.nih.tbi.commons.model.StudyStatus;
import gov.nih.tbi.dictionary.dao.DataElementDao;
import gov.nih.tbi.dictionary.dao.FormStructureDao;
import gov.nih.tbi.dictionary.dao.RepeatableGroupDao;
import gov.nih.tbi.dictionary.model.hibernate.ClassificationElement;
import gov.nih.tbi.dictionary.model.hibernate.DataElement;
import gov.nih.tbi.dictionary.model.hibernate.DiseaseStructure;
import gov.nih.tbi.dictionary.model.hibernate.FormStructure;
import gov.nih.tbi.dictionary.model.hibernate.MapElement;
import gov.nih.tbi.dictionary.model.hibernate.RepeatableGroup;
import gov.nih.tbi.dictionary.model.hibernate.ValueRange;
import gov.nih.tbi.repository.dao.DataStoreInfoDao;
import gov.nih.tbi.repository.dao.DataStoreTabularInfoDao;
import gov.nih.tbi.repository.dao.DatasetDao;
import gov.nih.tbi.repository.dao.DatasetDataStructureDao;
import gov.nih.tbi.repository.dao.RepositoryDao;
import gov.nih.tbi.repository.dao.StudyDao;
import gov.nih.tbi.repository.model.hibernate.ClinicalTrial;
import gov.nih.tbi.repository.model.hibernate.DataStoreInfo;
import gov.nih.tbi.repository.model.hibernate.DataStoreTabularColumnInfo;
import gov.nih.tbi.repository.model.hibernate.DataStoreTabularInfo;
import gov.nih.tbi.repository.model.hibernate.Dataset;
import gov.nih.tbi.repository.model.hibernate.DatasetDataStructure;
import gov.nih.tbi.repository.model.hibernate.Grant;
import gov.nih.tbi.repository.model.hibernate.Study;
import gov.nih.tbi.semantic.model.DataElementRDF;
import gov.nih.tbi.semantic.model.DatasetRDF;
import gov.nih.tbi.semantic.model.FormStructureRDF;
import gov.nih.tbi.semantic.model.RepeatableGroupRDF;
import gov.nih.tbi.semantic.model.StudyRDF;

import java.io.FileOutputStream;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import junit.framework.TestCase;

import org.apache.log4j.Logger;
import org.hibernate.Criteria;
import org.hibernate.criterion.Restrictions;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

import com.hp.hpl.jena.datatypes.xsd.XSDDatatype;
import com.hp.hpl.jena.datatypes.xsd.XSDDateTime;
import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.ontology.OntModelSpec;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.ResourceFactory;
import com.hp.hpl.jena.vocabulary.RDF;
import com.hp.hpl.jena.vocabulary.RDFS;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath:**/test-context.xml" })
public class RDFJenaTest extends TestCase// extends BaseHibernateTestCase
{

    // Dao's
    @Autowired
    DataElementDao deDao;

    @Autowired
    FormStructureDao formStructureDao;

    @Autowired
    RepeatableGroupDao rgDao;

    @Autowired
    DatasetDataStructureDao dsdsDao;

    @Autowired
    StudyDao studyDao;

    @Autowired
    DatasetDao datasetDao;

    @Autowired
    EntityMapDao emDao;

    @Autowired
    DataStoreInfoDao dsInfoDao;

    @Autowired
    DataStoreTabularInfoDao dsTabInfoDao;

    @Autowired
    RepositoryDao repoDao;

    // private static final String exportLocation = "D:\\RDFExport\\GeoffManley\\";
    private static final String exportLocation = "C:\\RDFExport\\stage\\";
    private static final int sqlLimit = 1000;

    private String deFileName = "deTriples.ttl";
    private String formFileName = "formTriples.ttl";
    private String studyFileName = "studyTriples.ttl";
    private String datasetFileName = "datasetTriples.ttl";
    private String relationshipsFileName = "relationshipsTriples.ttl";
    private String schemaFileName = "schemaTriples.ttl";
    private String repositoryFileName = "repositoryTriples.ttl";
    private String allFileName = "allTriples.ttl";
    // private String securityFileName = "securityTriples.ttl";

    private static final String COLUMN_NAME_DATASETID = "dataset_id";
    // private static final String COLUMN_NAME_SUBMISSION_RECORD_JOIN_ID = "submission_record_join_id";
    // private static final String COLUMN_NAME_GUID = "guid";
    // private static final String TABLE_NAME_SUBMISSION_RECORD_JOIN = "submission_record_join";

    static Logger logger = Logger.getLogger(RDFJenaTest.class);

    @Override
    protected void setUp()
    {

        try
        {
            super.setUp();
        }
        catch (Exception e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    // Call super set up if you want to use hibernate in test.
    // @Override
    // protected void setUp() throws Exception
    // {
    //
    // // super.setUpDictionary();
    // // super.setUpMeta();
    // super.setUpRepository();
    // }

    @Override
    protected void tearDown()
    {

        try
        {
            TestHibernateFactory.closeSessions();
            super.tearDown();
        }
        catch (Exception e)
        {

            e.printStackTrace();
        }
    }

    @Test
    public void generateAllRDF()
    {

        OntModel ontModel = ModelFactory.createOntologyModel(OntModelSpec.RDFS_MEM);
        Model model = ModelFactory.createDefaultModel();

        ontModel.add(createDERDF(ModelFactory.createOntologyModel(OntModelSpec.RDFS_MEM)));
        ontModel.add(createFormRDF(ModelFactory.createOntologyModel(OntModelSpec.RDFS_MEM)));
        ontModel.add(createStudyRDF(ModelFactory.createOntologyModel(OntModelSpec.RDFS_MEM)));
        ontModel.add(createDatasetRDF(ModelFactory.createOntologyModel(OntModelSpec.RDFS_MEM)));
        ontModel.add(createRelationships(ModelFactory.createOntologyModel(OntModelSpec.RDFS_MEM)));
        // ontModel.add(createSecurityTriples(ModelFactory.createOntologyModel(OntModelSpec.RDFS_MEM)));
        ontModel.add(createSchema(ModelFactory.createOntologyModel(OntModelSpec.RDFS_MEM)));
        createRepositoryRDFDSInfo(model);

        model.add(ontModel);
        writeToFile(exportLocation + allFileName, model, "Turtle", false);

    }

    @Test
    public void generateRepository()
    {

        Model model = ModelFactory.createDefaultModel();
        createRepositoryRDFDSInfo(model);
    }

    @Test
    public void generateDataElements()
    {

        OntModel ontModel = ModelFactory.createOntologyModel(OntModelSpec.RDFS_MEM);
        createDERDF(ontModel);
    }

    public OntModel createDERDF(OntModel model)
    {

        List<DataElement> elementList;

        try
        {
            elementList = deDao.listByStatus(DataElementStatus.PUBLISHED);
            logger.debug(elementList.size());

            // Create Model
            if (model == null)
            {
                model = ModelFactory.createOntologyModel();
            }
            // Iterate through list
            for (DataElement de : elementList)
            {

                model = addRDFForDataElement(model, de);

            }

            writeToFile(exportLocation + deFileName, model, "Turtle", false);
            return model;

        }

        catch (Exception e)
        {
            e.printStackTrace();
            assertTrue(false);
            return null;
        }

    }

    public OntModel addRDFForDataElement(OntModel model, DataElement de)
    {

        try
        {

            // Create Model if it does not exist
            if (model == null)
            {
                model = ModelFactory.createOntologyModel();
            }

            // If Published
            if (de.getStatus().equals(DataElementStatus.PUBLISHED))
            {

                Resource deResource = DataElementRDF.createDEResource(de.getStructuralObject());

                // If CDE
                if (de.isCommonDataElement())
                {
                    // model.add(deResource, RDF.type, DataElementRDF.RESOURCE_CDE);
                    model.add(deResource, RDFS.subClassOf,
                            ResourceFactory.createResource(DataElementRDF.RESOURCE_CDE.getURI()));

                }
                else
                {
                    model.add(deResource, RDFS.subClassOf,
                            ResourceFactory.createResource(DataElementRDF.RESOURCE_DE.getURI()));

                }
                // Added this for when model is not using a reasoner
                model.add(deResource, RDFS.subClassOf,
                        ResourceFactory.createResource(DataElementRDF.RESOURCE_ELEMENT.getURI()));
                model.add(deResource, RDF.type, RDFS.Class);

                model.add(deResource, DataElementRDF.PROPERTY_ID,
                        ResourceFactory.createPlainLiteral(de.getId().toString()));
                model.add(deResource, DataElementRDF.PROPERTY_ELEMENT_NAME,
                        ResourceFactory.createPlainLiteral(de.getName()));
                model.add(deResource, DataElementRDF.PROPERTY_TITLE, ResourceFactory.createPlainLiteral(de.getTitle()));

                model.add(deResource, RDFS.label, ResourceFactory.createPlainLiteral(de.getName()));
                model.add(deResource, RDFS.isDefinedBy, DataElementRDF.createDEResource(de.getStructuralObject()));

                model.add(deResource, DataElementRDF.PROPERTY_ELEMENT_TYPE,
                        ResourceFactory.createPlainLiteral(de.getType().getValue()));

                if (de.getSize() != null)
                {
                    model.add(deResource, DataElementRDF.PROPERTY_ELEMENT_SIZE,
                            ResourceFactory.createTypedLiteral(de.getSize()));
                }

                if (de.getDescription() != null)
                {
                    model.add(deResource, DataElementRDF.PROPERTY_DECRIPTION,
                            ResourceFactory.createPlainLiteral(de.getDescription()));
                }

                if (de.getShortDescription() != null)
                {
                    model.add(deResource, DataElementRDF.PROPERTY_SHORT_DESCRIPTION,
                            ResourceFactory.createPlainLiteral(de.getShortDescription()));
                }

                if (de.getNotes() != null)
                {
                    model.add(deResource, DataElementRDF.PROPERTY_NOTES,
                            ResourceFactory.createPlainLiteral(de.getNotes()));
                }

                if (de.getGuidelines() != null)
                {
                    model.add(deResource, DataElementRDF.PROPERTY_GUIDELINES,
                            ResourceFactory.createPlainLiteral(de.getGuidelines()));
                }

                if (de.getHistoricalNotes() != null)
                {
                    model.add(deResource, DataElementRDF.PROPERTY_HISTORICAL_NOTES,
                            ResourceFactory.createPlainLiteral(de.getHistoricalNotes()));
                }

                if (de.getReferences() != null)
                {
                    model.add(deResource, DataElementRDF.PROPERTY_REFERENCE,
                            ResourceFactory.createPlainLiteral(de.getReferences()));
                }

                if (de.getMaximumValue() != null)
                {
                    model.add(deResource, DataElementRDF.PROPERTY_MAXIMUM_VALUE,
                            ResourceFactory.createPlainLiteral(de.getMaximumValue().toString()));
                }

                if (de.getMinimumValue() != null)
                {
                    model.add(deResource, DataElementRDF.PROPERTY_MINIMUM_VALUE,
                            ResourceFactory.createPlainLiteral(de.getMinimumValue().toString()));
                }

                // if (de.getDomain() != null)
                // {
                // model.add(deResource, DataElementRDF.PROPERTY_DOMAIN,
                // ResourceFactory.createPlainLiteral(de.getDomain().getName()));
                // }
                //
                // if (de.getSubdomain() != null)
                // {
                // model.add(deResource, DataElementRDF.PROPERTY_SUBDOMAIN,
                // ResourceFactory.createPlainLiteral(de.getSubdomain().getName()));
                // }

                if (de.getPopulation() != null)
                {
                    model.add(deResource, DataElementRDF.PROPERTY_POPULATION,
                            ResourceFactory.createPlainLiteral(de.getPopulation().getName()));
                }

                /*                for (KeywordElement kw : de.getKeywordList())
                                {
                                    model.add(deResource, DataElementRDF.PROPERTY_KEYWORD,
                                            ResourceFactory.createPlainLiteral(kw.getKeyword().getKeyword()));
                                }*/

                if (!de.getRestrictions().equals(InputRestrictions.FREE_FORM))
                {
                    for (ValueRange valueRange : de.getValueRangeList())
                    {
                        model.add(deResource, DataElementRDF.PROPERTY_PERMISSIBLE_VALUE,
                                ResourceFactory.createPlainLiteral(valueRange.getValueRange().toString()));
                    }
                }

                for (ClassificationElement ce : de.getClassificationElementList())
                {
                    model.add(deResource, DataElementRDF.PROPERTY_CLASSIFICATION,
                            ResourceFactory.createPlainLiteral(ce.getClassification().getName()));

                }
            }

            return model;

        }

        catch (Exception e)
        {
            e.printStackTrace();
            assertTrue(false);
            return null;
        }

    }

    public OntModel createFormRDF(OntModel model)
    {

        List<FormStructure> formList;

        try
        {

            // Get Forms
            formList = formStructureDao.listDataStructuresByStatus(StatusType.PUBLISHED);

            logger.debug(formList.size());

            // Create Model
            if (model == null)
            {
                model = ModelFactory.createOntologyModel();
            }

            // Iterate through list
            for (FormStructure form : formList)
            {
                model = addRDFForForm(model, form);

            }

            writeToFile(exportLocation + formFileName, model, "Turtle", false);

            return model;
        }

        catch (Exception e)
        {
            e.printStackTrace();
            assertTrue(false);
            return null;
        }

    }

    public OntModel addRDFForForm(OntModel model, FormStructure form)
    {

        try
        {

            // Create Model if it does not exist
            if (model == null)
            {
                model = ModelFactory.createOntologyModel();
            }

            // Only create form triples if they are in a dataset
            if (dsdsDao.isFormInAnyDataset(form.getId()))
            {

                if (form.getStatus().equals(StatusType.PUBLISHED))
                {
                    Resource formResource = FormStructureRDF.createFormResource(form);

                    // model.add(formResource, RDF.type, FormStructureRDF.RESOURCE_FORM_STRUCTURE);
                    model.add(formResource, RDFS.subClassOf,
                            ResourceFactory.createResource(FormStructureRDF.RESOURCE_FORM_STRUCTURE.getURI()));

                    // Added this for when reasoner is off
                    model.add(formResource, RDF.type, RDFS.Class);

                    model.add(formResource, RDFS.label,
                            ResourceFactory.createPlainLiteral(FormStructureRDF.generateName(form)));

                    model.add(formResource, RDFS.isDefinedBy, formResource);

                    model.add(formResource, FormStructureRDF.PROPERTY_ID,
                            ResourceFactory.createPlainLiteral(form.getId().toString()));

                    model.add(formResource, FormStructureRDF.PROPERTY_TITLE,
                            ResourceFactory.createPlainLiteral(form.getTitle()));

                    if (form.getShortName() != null)
                    {
                        model.add(formResource, FormStructureRDF.PROPERTY_SHORT_NAME,
                                ResourceFactory.createPlainLiteral(form.getShortName()));
                    }

                    if (form.getDescription() != null)
                    {
                        model.add(formResource, FormStructureRDF.PROPERTY_DESCRIPTION,
                                ResourceFactory.createPlainLiteral(form.getDescription()));
                    }

                    if (form.getVersion() != null)
                    {
                        model.add(formResource, FormStructureRDF.PROPERTY_VERSION,
                                ResourceFactory.createTypedLiteral(form.getVersion()));
                    }

                    if (form.getOrganization() != null)
                    {
                        model.add(formResource, FormStructureRDF.PROPERTY_ORGANIZATION,
                                ResourceFactory.createPlainLiteral(form.getOrganization()));
                    }
                    if (form.getPublicationDate() != null)
                    {
                        model.add(formResource, FormStructureRDF.PROPERTY_PUBLICATION_DATE,
                                ResourceFactory.createTypedLiteral(form.getPublicationDate()));

                    }
                    if (form.getDiseaseList() != null)
                    {
                        for (DiseaseStructure ds : form.getDiseaseList())
                        {
                            model.add(formResource, FormStructureRDF.PROPERTY_DISEASE,
                                    ResourceFactory.createPlainLiteral(ds.getDisease().getName()));
                        }
                    }

                    // Create RDF for Repeatable Groups
                    for (RepeatableGroup rg : form.getRepeatableGroups())
                    {
                        Resource rgResource = RepeatableGroupRDF.createRGResource(rg);
                        model.add(rgResource, RDFS.subClassOf,
                                ResourceFactory.createResource(RepeatableGroupRDF.RESOURCE_RG.getURI()));

                        // Added this for when reasoner is off
                        model.add(rgResource, RDF.type, RDFS.Class);

                        model.add(rgResource, RDFS.label,
                                ResourceFactory.createPlainLiteral(RepeatableGroupRDF.generateName(rg)));

                        model.add(rgResource, RDFS.isDefinedBy, rgResource);

                        model.add(rgResource, RepeatableGroupRDF.PROPERTY_NAME,
                                ResourceFactory.createPlainLiteral(rg.getName()));

                        // Relationships for repeatable group
                        // Form --> RG
                        model.add(formResource, FormStructureRDF.RELATION_PROPERTY_HAS_REPEATABLE_GROUP, rgResource);

                        // RG --> Form
                        model.add(rgResource, RepeatableGroupRDF.RELATION_PROPERTY_HAS_FORM, formResource);

                        for (MapElement me : rg.getMapElements())
                        {
                            Resource deResource = DataElementRDF.createDEResource(me.getStructuralDataElement());

                            // RG --> DE
                            model.add(rgResource, RepeatableGroupRDF.RELATION_PROPERTY_HAS_DATA_ELEMENT, deResource);

                            // DE --> RG
                            model.add(deResource, DataElementRDF.RELATION_PROPERTY_HAS_REPEATABLE_GROUP, rgResource);
                        }
                    }

                }
            }

            return model;
        }

        catch (Exception e)
        {
            e.printStackTrace();
            assertTrue(false);
            return null;
        }

    }

    public OntModel createStudyRDF(OntModel model)
    {

        List<Study> studyList;

        try
        {

            // Get Forms
            studyList = studyDao.getAllForRDFGen();

            logger.debug(studyList.size());

            // Create Model
            if (model == null)
            {
                model = ModelFactory.createOntologyModel();
            }

            // Iterate through list
            for (Study study : studyList)
            {

                model = addRDFForStudy(model, study);
            }

            writeToFile(exportLocation + studyFileName, model, "TURTLE", false);

            return model;

        }

        catch (Exception e)
        {
            e.printStackTrace();
            assertTrue(false);
            return null;
        }

    }

    public OntModel addRDFForStudy(OntModel model, Study study)
    {

        try
        {

            // Create Model if it does not exist
            if (model == null)
            {
                model = ModelFactory.createOntologyModel();
            }

            // Only create triples if Private / Public study
            if (study.getStudyStatus().equals(StudyStatus.PUBLIC) || study.getStudyStatus().equals(StudyStatus.PRIVATE))
            {
                Resource studyResource = StudyRDF.createStudyResource(study);
                // model.add(studyResource, RDF.type, StudyRDF.RESOURCE_STUDY);
                model.add(studyResource, RDFS.subClassOf,
                        ResourceFactory.createResource(StudyRDF.RESOURCE_STUDY.getURI()));
                model.add(studyResource, StudyRDF.PROPERTY_TITLE, ResourceFactory.createPlainLiteral(study.getTitle()));

                // Added this for when reasoner is off
                model.add(studyResource, RDF.type, RDFS.Class);

                model.add(studyResource, RDFS.label, ResourceFactory.createPlainLiteral(study.getTitle()));

                model.add(studyResource, RDFS.isDefinedBy, StudyRDF.createStudyResource(study));
                model.add(studyResource, StudyRDF.PROPERTY_ID,
                        ResourceFactory.createPlainLiteral(study.getId().toString()));

                if (study.getAbstractText() != null)
                {
                    model.add(studyResource, StudyRDF.PROPERTY_ABSTRACT,
                            ResourceFactory.createPlainLiteral(study.getAbstractText()));
                }

                if (study.getDateCreated() != null)
                {
                    model.add(studyResource, StudyRDF.PROPERTY_DATE_CREATED,
                            ResourceFactory.createTypedLiteral(study.getDateCreated()));
                }

                if (study.getGrantSet() != null)
                {
                    for (Grant grant : study.getGrantSet())
                    {
                        model.add(studyResource, StudyRDF.PROPERTY_GRANT,
                                ResourceFactory.createPlainLiteral(grant.getGrantId()));
                    }
                }

                if (study.getClinicalTrialSet() != null)
                {
                    for (ClinicalTrial ct : study.getClinicalTrialSet())
                    {
                        model.add(studyResource, StudyRDF.PROPERTY_CLINICAL_TRIAL,
                                ResourceFactory.createPlainLiteral(ct.getClinicalTrialId()));
                    }
                }

                if (study.getStudyStatus() != null)
                {
                    model.add(studyResource, StudyRDF.PROPERTY_STATUS,
                            ResourceFactory.createPlainLiteral(study.getStudyStatus().getName()));
                }

            }

            return model;

        }

        catch (Exception e)
        {
            e.printStackTrace();
            assertTrue(false);
            return null;
        }

    }

    public OntModel createDatasetRDF(OntModel model)
    {

        List<Dataset> datasetList;

        try
        {

            // Get Forms
            datasetList = datasetDao.getAll();

            logger.debug(datasetList.size());

            // Create Model
            if (model == null)
            {
                model = ModelFactory.createOntologyModel();
            }

            // Iterate through list
            for (Dataset dataset : datasetList)
            {

                model = addRDFForDataset(model, dataset);
            }

            writeToFile(exportLocation + datasetFileName, model, "TURTLE", false);

            return model;
        }

        catch (Exception e)
        {
            e.printStackTrace();
            assertTrue(false);
            return null;
        }

    }

    public OntModel addRDFForDataset(OntModel model, Dataset dataset)
    {

        try
        {

            // Create Model if it does not exist
            if (model == null)
            {
                model = ModelFactory.createOntologyModel();
            }

            // Only create triples for nonarchived datasets (Private / Shared) for now
            if (dataset.getDatasetStatus().equals(DatasetStatus.PRIVATE)
                    || dataset.getDatasetStatus().equals(DatasetStatus.SHARED))
            {

                Resource datasetResource = DatasetRDF.createDatasetResource(dataset);
                // model.add(datasetResource, RDF.type, DatasetRDF.RESOURCE_DATASET);
                model.add(datasetResource, RDFS.subClassOf,
                        ResourceFactory.createResource(DatasetRDF.RESOURCE_DATASET.getURI()));

                // Added this for when reasoner is off
                model.add(datasetResource, RDF.type, RDFS.Class);

                model.add(datasetResource, RDFS.label, ResourceFactory.createPlainLiteral(dataset.getName()));

                model.add(datasetResource, RDFS.isDefinedBy, DatasetRDF.createDatasetResource(dataset));

                model.add(datasetResource, DatasetRDF.PROPERTY_ID,
                        ResourceFactory.createPlainLiteral(dataset.getId().toString()));

                if (dataset.getPublicationDate() != null)
                {
                    model.add(datasetResource, DatasetRDF.PROPERTY_PUBLICATION_DATE,
                            ResourceFactory.createTypedLiteral(dataset.getPublicationDate()));
                }

                if (dataset.getSubmitDate() != null)
                {

                    model.add(datasetResource, DatasetRDF.PROPERTY_SUBMIT_DATE,
                            ResourceFactory.createTypedLiteral(dataset.getSubmitDate()));
                }

                if (dataset.getSubmitter() != null && dataset.getSubmitter().getFullName() != null)
                {
                    model.add(datasetResource, DatasetRDF.PROPERTY_SUBMITTER_NAME,
                            ResourceFactory.createPlainLiteral(dataset.getSubmitter().getFullName()));
                }

                if (dataset.getStudy() != null && dataset.getStudy().getId() != null)
                {
                    model.add(datasetResource, StudyRDF.PROPERTY_ID,
                            ResourceFactory.createTypedLiteral(dataset.getStudy().getId()));
                }

                if (dataset.getDatasetStatus() != null)
                {
                    model.add(datasetResource, DatasetRDF.PROPERTY_STATUS,
                            ResourceFactory.createPlainLiteral(dataset.getDatasetStatus().getName()));
                }
            }

            return model;
        }

        catch (Exception e)
        {
            e.printStackTrace();
            assertTrue(false);
            return null;
        }

    }

    public OntModel createRelationships(OntModel model)
    {

        List<Study> studyList;

        try
        {

            // Get Studies
            studyList = studyDao.getAllForRDFGen();

            logger.debug("Study Count: " + studyList.size());

            // Create Model
            if (model == null)
            {
                model = ModelFactory.createOntologyModel();
            }

            // Iterate through list
            for (Study study : studyList)
            {

                // Only create triples for Public / Private studies
                if (study.getStudyStatus().equals(StudyStatus.PUBLIC)
                        || study.getStudyStatus().equals(StudyStatus.PRIVATE))
                {

                    Resource studyResource = StudyRDF.createStudyResource(study);
                    // Property studyProperty = StudyRDF.createStudyProperty(study);

                    // Study --> Study
                    model.add(studyResource, StudyRDF.RELATION_PROPERTY_FACETED_STUDY, studyResource);

                    for (Dataset ds : study.getDatasetSet())
                    {

                        // Only create triples for nonarchived datasets (Private / Shared) for now
                        if (ds.getDatasetStatus().equals(DatasetStatus.PRIVATE)
                                || ds.getDatasetStatus().equals(DatasetStatus.SHARED))
                        {
                            Resource datasetResource = DatasetRDF.createDatasetResource(ds);

                            // Dataset --> Study
                            model.add(datasetResource, StudyRDF.RELATION_PROPERTY_FACETED_STUDY, studyResource);

                            // Study --> Dataset
                            model.add(studyResource, StudyRDF.RELATION_PROPERTY_FACETED_DATASET, datasetResource);

                            for (DatasetDataStructure dsds : ds.getDatasetDataStructure())
                            {
                                Long formId = dsds.getDataStructureId();

                                FormStructure form = formStructureDao.get(formId);

                                Resource formResource = FormStructureRDF.createFormResource(form);
                                // Property formProperty = FormStructureRDF.createFormProperty(form);

                                // Study --> Form
                                model.add(studyResource, StudyRDF.RELATION_PROPERTY_FACETED_FORM, formResource);

                                // Form --> Form
                                model.add(formResource, StudyRDF.RELATION_PROPERTY_FACETED_FORM, formResource);

                                // Form --> Study
                                model.add(formResource, StudyRDF.RELATION_PROPERTY_FACETED_STUDY, studyResource);

                                // Form --> Dataset
                                model.add(formResource, StudyRDF.RELATION_PROPERTY_FACETED_DATASET, datasetResource);

                                // Dataset --> Form
                                model.add(datasetResource, StudyRDF.RELATION_PROPERTY_FACETED_FORM, formResource);

                                for (MapElement me : form.getFormStructureSqlObject().getDataElements())
                                {

                                    Resource deResource = DataElementRDF
                                            .createDEResource(me.getStructuralDataElement());
                                    // Property deProperty = DataElementRDF.createDEProperty(me.getDataElement());

                                    // Form --> DE
                                    model.add(formResource, StudyRDF.RELATION_PROPERTY_FACETED_DE, deResource);
                                    // Study --> DE
                                    model.add(studyResource, StudyRDF.RELATION_PROPERTY_FACETED_DE, deResource);
                                    // DE --> DE
                                    model.add(deResource, StudyRDF.RELATION_PROPERTY_FACETED_DE, deResource);

                                    // DE --> Form
                                    model.add(deResource, StudyRDF.RELATION_PROPERTY_FACETED_FORM, formResource);
                                    // DE --> Study
                                    model.add(deResource, StudyRDF.RELATION_PROPERTY_FACETED_STUDY, studyResource);

                                }
                            }
                        }
                    }
                }
            }

            writeToFile(exportLocation + relationshipsFileName, model, "TURTLE", false);

            return model;

        }

        catch (Exception e)
        {
            e.printStackTrace();
            assertTrue(false);
            return null;
        }

    }

    // public OntModel createSecurityTriples(OntModel model)
    // {
    //
    // // Get a list of entity maps for all the private data sets
    // List<Dataset> privateDatasets;
    //
    // try
    // {
    //
    // // Create Model
    // if (model == null)
    // {
    // model = ModelFactory.createOntologyModel();
    // }
    //
    // // Get Private Datasets
    // privateDatasets = datasetDao.getDatasetsByStatus(DatasetStatus.PRIVATE);
    //
    // for (Dataset ds : privateDatasets)
    // {
    // // Get the entity maps for this dataset
    // List<EntityMap> maps = emDao.get((Long) ds.getId(), EntityType.DATASET);
    //
    // Resource datasetResource = DatasetRDF.createDatasetResource(ds);
    //
    // // go through maps and create triples
    // for (EntityMap map : maps)
    // {
    //
    // // Only do this for account permissions not permission groups
    // if (map.getAccount() != null)
    // {
    // model.add(datasetResource, SecurityRDF.PROPERTY_HASATLEASTREADACCESS,
    // ResourceFactory.createPlainLiteral(map.getAccount().getId().toString()));
    // }
    // }
    //
    // }
    //
    // // TODO: Add privelegages to shared datasets
    //
    // // TODO: Add admin priveleages to all (private / shared) datasets
    //
    // writeToFile(exportLocation + securityFileName, model, "TURTLE", false);
    //
    // return model;
    // }
    // catch (Exception e)
    // {
    // e.printStackTrace();
    // assertTrue(false);
    // return null;
    // }
    // }

    // public OntModel createSecurityTriplesOld(OntModel model)
    // {
    //
    // List<EntityMap> entityMaps;
    //
    // try
    // {
    //
    // // Get Entity Maps
    // entityMaps = emDao.getAll();
    //
    // // Create Model
    // if (model == null)
    // {
    // model = ModelFactory.createOntologyModel();
    // }
    //
    // for (EntityMap map : entityMaps)
    // {
    // EntityType entityType = map.getType();
    // if (map.getAccount() != null)
    // {
    //
    // switch (entityType)
    // {
    // case DATA_STRUCTURE:
    // DataStructure form = formDao.get(map.getEntityId());
    //
    // if (form != null)
    // {
    // Resource formResource = FormStructureRDF.createFormResource(form);
    // model.add(formResource, SecurityRDF.getPermissionProperty(map.getPermission()),
    // ResourceFactory.createPlainLiteral(map.getAccount().getId().toString()));
    // }
    // break;
    // case DATA_ELEMENT:
    // DataElement de = deDao.get(map.getEntityId());
    //
    // if (de != null)
    // {
    // Resource deResource = DataElementRDF.createDEResource(de);
    // model.add(deResource, SecurityRDF.getPermissionProperty(map.getPermission()),
    // ResourceFactory.createPlainLiteral(map.getAccount().getId().toString()));
    // }
    // break;
    // case STUDY:
    // Study study = studyDao.get(map.getEntityId());
    //
    // if (study != null)
    // {
    // Resource studyResource = StudyRDF.createStudyResource(study);
    // model.add(studyResource, SecurityRDF.getPermissionProperty(map.getPermission()),
    // ResourceFactory.createPlainLiteral(map.getAccount().getId().toString()));
    // }
    // break;
    // case DATASET:
    // break;
    // default:
    // break;
    //
    // }
    // }
    // }
    //
    // writeToFile(exportLocation + securityFileName, model, "TURTLE", false);
    //
    // return model;
    // }
    // catch (Exception e)
    // {
    // e.printStackTrace();
    // assertTrue(false);
    // return null;
    // }
    // }

    public OntModel createSchema(OntModel model)
    {

        // Create Model
        if (model == null)
        {
            model = ModelFactory.createOntologyModel();
        }

        Resource elementResource = DataElementRDF.RESOURCE_ELEMENT;
        model.add(elementResource, RDF.type, RDFS.Class);
        // model.add(elementResource, RDFS.subClassOf, RDFS.Class);
        model.add(elementResource, RDFS.label, ResourceFactory.createPlainLiteral("Element"));
        model.add(elementResource, RDFS.comment, ResourceFactory.createPlainLiteral("This is a comment for Elements"));

        Resource cdeResource = DataElementRDF.RESOURCE_CDE;
        model.add(cdeResource, RDF.type, RDFS.Class);
        model.add(cdeResource, RDFS.label, ResourceFactory.createPlainLiteral("CommonDataElement"));
        model.add(cdeResource, RDFS.subClassOf, ResourceFactory.createResource(elementResource.getURI()));
        model.add(cdeResource, RDFS.comment,
                ResourceFactory.createPlainLiteral("This is a comment for CommonDataElements"));

        Resource deResource = DataElementRDF.RESOURCE_DE;
        model.add(deResource, RDF.type, RDFS.Class);
        model.add(deResource, RDFS.label, ResourceFactory.createPlainLiteral("DataElement"));
        model.add(deResource, RDFS.subClassOf, ResourceFactory.createResource(elementResource.getURI()));
        model.add(deResource, RDFS.comment, ResourceFactory.createPlainLiteral("This is a comment for DataElements"));

        Resource formResource = FormStructureRDF.RESOURCE_FORM_STRUCTURE;
        model.add(formResource, RDF.type, RDFS.Class);
        // model.add(formResource, RDFS.subClassOf, RDFS.Class);
        model.add(formResource, RDFS.label, ResourceFactory.createPlainLiteral("FormStructure"));
        model.add(formResource, RDFS.comment,
                ResourceFactory.createPlainLiteral("This is a comment for FormStructures"));

        Resource rgResource = RepeatableGroupRDF.RESOURCE_RG;
        model.add(rgResource, RDF.type, RDFS.Class);
        model.add(rgResource, RDFS.label, ResourceFactory.createPlainLiteral("RepeatableGroup"));
        model.add(rgResource, RDFS.comment, ResourceFactory.createPlainLiteral("This is a comment for RepeatableGroup"));

        Resource studyResource = StudyRDF.RESOURCE_STUDY;
        model.add(studyResource, RDF.type, RDFS.Class);
        // model.add(studyResource, RDFS.subClassOf, RDFS.Class);
        model.add(studyResource, RDFS.label, ResourceFactory.createPlainLiteral("Study"));
        model.add(studyResource, RDFS.comment, ResourceFactory.createPlainLiteral("This is a comment for Studies"));

        Resource datasetResoure = DatasetRDF.RESOURCE_DATASET;
        model.add(datasetResoure, RDF.type, RDFS.Class);
        // model.add(datasetResoure, RDFS.subClassOf, RDFS.Class);
        model.add(datasetResoure, RDFS.label, ResourceFactory.createPlainLiteral("Dataset"));
        model.add(datasetResoure, RDFS.comment, ResourceFactory.createPlainLiteral("This is a comment for Datasets"));

        /*        Resource repositoryRow = RepositoryRDF.RepositoryRow;
                // model.add(repositoryRow, RDF.type, RDFS.Class);
                // model.add(repositoryRow, RDFS.subClassOf, RDFS.Class);
                model.add(repositoryRow, RDFS.label, ResourceFactory.createPlainLiteral("RepositoryRow"));
                model.add(repositoryRow, RDFS.comment,
                        ResourceFactory.createPlainLiteral("This is a comment for repository row"));

                Resource repositoryTable = RepositoryRDF.RepositoryTable;
                // model.add(repositoryTable, RDF.type, RDFS.Class);
                // model.add(repositoryTable, RDFS.subClassOf, RDFS.Class);
                model.add(repositoryTable, RDFS.label, ResourceFactory.createPlainLiteral("RepositoryTable"));
                model.add(repositoryTable, RDFS.comment,
                        ResourceFactory.createPlainLiteral("This is a comment for repository table"));*/

        writeToFile(exportLocation + schemaFileName, model, "TURTLE", false);

        return model;

    }

    // // Not finished
    // public Model createRepositoryRDFForm(Model model)
    // {
    //
    // List<DataStructure> formList;
    //
    // try
    // {
    //
    // // Get Forms
    // formList = formDao.listDataStructuresByStatus(StatusType.PUBLISHED);
    //
    //
    // // Create Model
    // if (model == null)
    // {
    // model = ModelFactory.createOntologyModel();
    // }
    //
    // // Iterate through list
    // for (DataStructure form : formList)
    // {
    //
    // // Check to see if this form has repeatable groups
    // if (!form.getRepeatableGroups().isEmpty())
    // {
    // for (RepeatableGroup rg : form.getRepeatableGroups())
    // {
    // // Get Main Group
    // String tableName = getStoreName(rg);
    // List results = repoDao.listRepoTable(tableName);
    //
    // }
    // }
    //
    // // if form has no rg's
    //
    // }
    //
    // writeToFile(exportLocation + repositoryFileName, model, "TURTLE", false);
    //
    // return model;
    // }
    //
    // catch (Exception e)
    // {
    // e.printStackTrace();
    // assertTrue(false);
    // return null;
    // }
    //
    // }

    @SuppressWarnings("rawtypes")
    @Transactional
    public Model createRepositoryRDFDSInfo(Model model)
    {

        try
        {
            // Create Model
            if (model == null)
            {
                model = ModelFactory.createDefaultModel();
            }

            // Get DataStoreInfo
            List<DataStoreInfo> dsInfos = dsInfoDao.getAll();

            findValid: for (DataStoreInfo dsInfo : dsInfos)
            {

                // XXX:REMOVE ME
                // TODO: Stage FITBIR
                // int[] invalidIds = { 416, 417, 418, 419, 420, 421, 425 };

                // TODO: Stage PDBP
                int[] invalidIds = { 401 };

                // XXX:REMOVE ME
                // TODO: Dev
                // int[] invalidIds = {};

                for (int x = 0; x < invalidIds.length; x++)
                {
                    if (invalidIds[x] == dsInfo.getId())
                    {
                    	logger.debug("Skipping RDF(Invalid) FOR dsInfoID: " + dsInfo.getId());
                        continue findValid;
                    }
                }

                try
                {
                    model.add(createRDFForDSInfo(dsInfo));
                }
                catch (Exception e)
                {
                	logger.debug("Error Adding RDF For DSInfo ID: " + dsInfo.getId());
                    e.printStackTrace();
                }

            }

            writeToFile(exportLocation + repositoryFileName, model, "TURTLE", false);

            return model;
        }
        catch (Exception e)
        {
            e.printStackTrace();
            assertTrue(false);
            return null;
        }

    }

    @Transactional
    public Model createRDFForDSInfo(DataStoreInfo dsInfo)
    {

        Model model = ModelFactory.createDefaultModel();

        try
        {

            System.out.println("CREATING RDF FOR dsInfoID: " + dsInfo.getId());

            int rowNumber = 0;

            // if (dsInfo.getId().toString().equals("228"))
            {
                // Get Form
                FormStructure form = formStructureDao.get((Long) dsInfo.getDataStructureId());
                logger.debug("  Form:" + form.getTitle());
                // Join Repeatable Groups into one table
                List results = repoDao.joinDataStoreInfo(dsInfo, sqlLimit);

                for (Object object : results)
                {
                    // Create Row Resource
                    Map row = (Map) object;
                    rowNumber++;

                    // Create RDF for each form
                    Resource formInstance = FormStructureRDF.createFormResourceInstance(form, rowNumber);
                    model.add(formInstance, RDF.type, FormStructureRDF.createFormResource(form));

                    // Add dataset id to row number
                    Object dataset_id = row.get(COLUMN_NAME_DATASETID);

                    if (dataset_id != null)
                    {
                        String columnValue = dataset_id.toString();

                        if (columnValue != null && !columnValue.isEmpty())
                        {

                            model.add(formInstance, DatasetRDF.PROPERTY_ID,
                                    ResourceFactory.createTypedLiteral(columnValue, XSDDatatype.XSDlong));
                        }
                    }

                    // Go through all the repeatable groups of the form
                    // TODO: Might be better to iterate through repeatable groups of form instead
                    for (DataStoreTabularInfo dsTabInfo : dsInfo.getDataStoreTabularInfos())
                    {
                        // Get RG
                        RepeatableGroup rg = rgDao.get(dsTabInfo.getRepeatableGroupId());

                        // Create RG Instance
                        Resource rgInstance = RepeatableGroupRDF.createRGResourceInstance(rg, rowNumber);
                        model.add(rgInstance, RDF.type, RepeatableGroupRDF.createRGResource(rg));

                        // FormInstance --> RGInstance
                        model.add(formInstance, FormStructureRDF.RELATION_PROPERTY_HAS_REPEATABLE_GROUP_INSTANCE,
                                rgInstance);
                        // RGInstnace --> FormInstance
                        model.add(rgInstance, FormStructureRDF.RELATION_PROPERTY_IS_OF_FORM_INSTANCE, formInstance);

                        // Iterate through columns and get instance data
                        // Create RDF for rest of the columns
                        for (DataStoreTabularColumnInfo columnInfo : dsTabInfo.getColumnInfos())
                        {
                            // Table names are created lower cased. Since we are using alias to reference
                            // because of
                            // potential spaces casing must match
                            // Add the table name in the select statement to distinguish between the same de's
                            // in
                            // different rg's
                            String columnName = dsTabInfo.getTableName().toLowerCase() + "."
                                    + columnInfo.getColumnName().toLowerCase();

                            // have to get De for Column (in another db!) so hopefully only do this once
                            if (columnInfo.getDataElement() == null)
                            {

                                Long mapElementId = columnInfo.getMapElementId();
                                DataElement columnDE = deDao.getByMapElementId(mapElementId);
                                columnInfo.setDataElement(columnDE.getStructuralObject());
                            }
                            // Ignore srj and subject columns because those were done earlier (srj wont be in
                            // tabularInfo so
                            // dont need to exclude)
                            // if (columnName != submissionRecordJoinColumnName) && columnName !=
                            // subjectColumnName)
                            if (columnInfo.getDataElement() != null)
                            {
                                Object column = row.get(columnName);

                                if (column != null)
                                {
                                    String columnValue = column.toString();

                                    if (columnValue != null && !columnValue.isEmpty())
                                    {
                                        // determine type of data element to figure out xsd data type
                                        XSDDatatype dType = DataElementRDF.determineDataType(columnInfo
                                                .getDataElement());

                                        // Need to format Date Time
                                        if (dType.equals(XSDDatatype.XSDdateTime))
                                        {

                                            Calendar cal = Calendar.getInstance();
                                            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                                            Date d = sdf.parse(columnValue);
                                            cal.setTime(d);

                                            // This is a hack to set the correct time
                                            // For some reason when convert from cal to xsd it adds 4 hours
                                            // (difference between est and gmt)
                                            // I tried everything with setting the timezone of calendar
                                            // TLDR DateTime in java are dumb
                                            cal.add(Calendar.HOUR_OF_DAY, -4);

                                            XSDDateTime xsdDT = new XSDDateTime(cal);

                                            model.add(rgInstance,
                                                    DataElementRDF.createDEProperty(columnInfo.getDataElement()),
                                                    ResourceFactory.createTypedLiteral(xsdDT));
                                        }
                                        else
                                        {

                                            model.add(rgInstance,
                                                    DataElementRDF.createDEProperty(columnInfo.getDataElement()),
                                                    ResourceFactory.createTypedLiteral(columnValue, dType));
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        catch (Exception e)
        {
        	logger.debug("Skipping dsInfo" + dsInfo.getId());
            e.printStackTrace();
        }

        return model;

    }

    // // This generates RDF for each repeatable group table seperately
    // @SuppressWarnings("rawtypes")
    // public Model createRepositoryRDFOld2(Model model)
    // {
    //
    // try
    // {
    // // Create Model
    // if (model == null)
    // {
    // model = ModelFactory.createDefaultModel();
    // }
    // List<DataStoreTabularInfo> tabularInfos = dsTabInfoDao.getAll();
    //
    //
    // findValid: for (DataStoreTabularInfo tabInfo : tabularInfos)
    // {
    //
    // // XXX:REMOVE ME
    // // TODO: REMOVE ME
    // int[] invalidIds = {};
    //
    // for (int x = 0; x < invalidIds.length; x++)
    // {
    // if (invalidIds[x] == tabInfo.getId())
    // {
    // continue findValid;
    // }
    // }
    //
    //
    // String tableName = tabInfo.getTableName();
    // List results = repoDao.listRepoTable(tableName);
    //
    // // Add repeatable group and form info
    // RepeatableGroup rg = rgDao.get(tabInfo.getRepeatableGroupId());
    // + rg.getDataStructure().getTitle());
    //
    // for (Object object : results)
    // {
    // // Create Row Resource
    // Map row = (Map) object;
    // String id = row.get("id").toString();
    // String rowLabel = rg.getDataStructure().getTitle() + "_" + rg.getName() + id;
    //
    // Resource formInstance = ResourceFactory.createResource(RepositoryRDF.getURI() + rowLabel);
    // model.add(formInstance, RDF.type, FormStructureRDF.createFormResource(rg.getDataStructure()));
    //
    // model.add(formInstance,
    // ResourceFactory.createProperty(FormStructureRDF.RESOURCE_REPEATABLE_GROUP.getURI()),
    // ResourceFactory.createPlainLiteral(rg.getName()));
    //
    // // Add Study and Dataset info to the row
    // Object srjColumn = row.get(COLUMN_NAME_SUBMISSION_RECORD_JOIN_ID);
    // Long submissionRecordJoinId = Long.parseLong(srjColumn.toString());
    //
    // SubmissionRecordJoin submission = repoDao.getSubmissionRecordJoin(submissionRecordJoinId);
    // Dataset dataset = datasetDao.get(submission.getDatasetId());
    //
    // // Create resource and property for dataset / study
    // Resource datasetResource = DatasetRDF.createDatasetResource(dataset);
    // Resource studyResource = StudyRDF.createStudyResource(dataset.getStudy());
    //
    // model.add(formInstance, ResourceFactory.createProperty(DatasetRDF.RESOURCE_DATASET.getURI()),
    // datasetResource);
    // model.add(formInstance, ResourceFactory.createProperty(StudyRDF.RESOURCE_STUDY.getURI()),
    // studyResource);
    //
    // model.add(
    // formInstance,
    // ResourceFactory.createProperty(DataElementRDF.URI_NS
    // + COLUMN_NAME_SUBMISSION_RECORD_JOIN_ID),
    // ResourceFactory.createPlainLiteral(submissionRecordJoinId.toString()));
    //
    // // // Add Row permissions based on Dataset permissions
    // // List<EntityMap> maps = getEntityMapsByEntityId(dataset.getId(), EntityType.DATASET);
    // //
    // // for (EntityMap map : maps)
    // // {
    // // // Exclude permission groups for now
    // // if (map.getAccount() != null)
    // // {
    // // model.add(rowResource, SecurityRDF.getPermissionProperty(map.getPermission()),
    // // ResourceFactory.createPlainLiteral(map.getAccount().getId().toString()));
    // // }
    // // }
    //
    // // Check to see if row has subject associated with it
    // // If it does, create relationships between study and datasets
    // Object subjectColumn = row.get(COLUMN_NAME_GUID);
    // if (subjectColumn != null)
    // {
    // String subjectValue = subjectColumn.toString();
    //
    // if (subjectValue != null && !subjectValue.isEmpty())
    // {
    // Property subjectProperty = ResourceFactory.createProperty(DataElementRDF.URI_NS
    // + COLUMN_NAME_GUID);
    // model.add(studyResource, subjectProperty, ResourceFactory.createPlainLiteral(subjectValue));
    // model.add(datasetResource, subjectProperty,
    // ResourceFactory.createPlainLiteral(subjectValue));
    // }
    // }
    //
    // // Create RDF for rest of the columns
    // for (DataStoreTabularColumnInfo columnInfo : tabInfo.getColumnInfos())
    // {
    // String columnName = columnInfo.getColumnName().toLowerCase();
    //
    // // have to get De for Column (in another db!) so hopefully only do this once
    // if (columnInfo.getDataElement() == null)
    // {
    //
    // Long mapElementId = columnInfo.getMapElementId();
    // DataElement columnDE = deDao.getDataElementByMapElementId(mapElementId);
    // columnInfo.setDataElement(columnDE);
    // }
    // // Ignore srj and subject columns because those were done earlier (srj wont be in tabularInfo so
    // // dont need to exclude)
    // // if (columnName != submissionRecordJoinColumnName) && columnName != subjectColumnName)
    // if (columnInfo.getDataElement() != null)
    // {
    // Object column = row.get(columnName);
    //
    // if (column != null)
    // {
    // String columnValue = column.toString();
    //
    // if (columnValue != null && !columnValue.isEmpty())
    // {
    // model.add(formInstance,
    // DataElementRDF.createDEProperty(columnInfo.getDataElement()),
    // ResourceFactory.createPlainLiteral(columnValue));
    // }
    // }
    // }
    // }
    // }
    // }
    //
    // writeToFile(exportLocation + repositoryFileName, model, "TURTLE", false);
    //
    // return model;
    //
    // }
    // catch (Exception e)
    // {
    // e.printStackTrace();
    // assertTrue(false);
    // return null;
    // }
    // }

    // @SuppressWarnings("rawtypes")
    // public Model createRepositoryRDFOld(Model model)
    // {
    //
    // String submissionRecordJoinColumnName = "submission_record_join_id";
    // String subjectColumnName = "guid";
    //
    // try
    // {
    // // Create Model
    // if (model == null)
    // {
    // model = ModelFactory.createDefaultModel();
    // }
    // List<DataStoreTabularInfo> tabularInfos = dsTabInfoDao.getAll();
    //
    //
    // for (DataStoreTabularInfo tabInfo : tabularInfos)
    // {
    //
    // String tableName = tabInfo.getTableName();
    // List results = repoDao.listRepoTable(tableName);
    //
    //
    // Resource tableResource = ResourceFactory.createResource(RepositoryRDF.getURI() + tableName);
    //
    // model.add(tableResource, RDF.type, RepositoryRDF.RepositoryTable);
    // model.add(tableResource, RepositoryRDF.hasTable, tableResource);
    //
    // model.add(tableResource, RDFS.label, ResourceFactory.createPlainLiteral(tableName));
    //
    // // Add repeatable group and form info
    // RepeatableGroup rg = rgDao.get(tabInfo.getRepeatableGroupId());
    // model.add(tableResource,
    // ResourceFactory.createProperty(FormStructureRDF.RESOURCE_REPEATABLE_GROUP.getURI()),
    // ResourceFactory.createPlainLiteral(rg.getName()));
    // model.add(tableResource,
    // ResourceFactory.createProperty(FormStructureRDF.RESOURCE_FORM_STRUCTURE.getURI()),
    // FormStructureRDF.createFormResource(rg.getDataStructure()));
    //
    // for (Object object : results)
    // {
    // Map row = (Map) object;
    //
    // // Create Row Resource
    // String id = row.get("id").toString();
    // String rowLabel = tableName + "_Row" + id;
    // Resource rowResource = ResourceFactory.createResource(RepositoryRDF.getURI() + rowLabel);
    // // Resource rowProperty = ResourceFactory.createResource(rowResource.getURI());
    //
    //
    // model.add(rowResource, RDF.type, RepositoryRDF.RepositoryRow);
    // model.add(rowResource, RDFS.label, ResourceFactory.createPlainLiteral(rowLabel));
    // model.add(rowResource, RepositoryRDF.hasTable, tableResource);
    // model.add(tableResource, RepositoryRDF.hasRow, rowResource);
    //
    // // Add Study and Dataset info to the row
    // Object srjColumn = row.get(submissionRecordJoinColumnName);
    // Long submissionRecordJoinId = Long.parseLong(srjColumn.toString());
    //
    // SubmissionRecordJoin submission = repoDao.getSubmissionRecordJoin(submissionRecordJoinId);
    // Dataset dataset = datasetDao.get(submission.getDatasetId());
    //
    // // Create resource and property for dataset / study
    // Resource datasetResource = DatasetRDF.createDatasetResource(dataset);
    // Resource studyResource = StudyRDF.createStudyResource(dataset.getStudy());
    //
    // model.add(rowResource, ResourceFactory.createProperty(DatasetRDF.RESOURCE_DATASET.getURI()),
    // datasetResource);
    // model.add(rowResource, ResourceFactory.createProperty(StudyRDF.RESOURCE_STUDY.getURI()),
    // studyResource);
    //
    // // // Add Row permissions based on Dataset permissions
    // // List<EntityMap> maps = getEntityMapsByEntityId(dataset.getId(), EntityType.DATASET);
    // //
    // // for (EntityMap map : maps)
    // // {
    // // // Exclude permission groups for now
    // // if (map.getAccount() != null)
    // // {
    // // model.add(rowResource, SecurityRDF.getPermissionProperty(map.getPermission()),
    // // ResourceFactory.createPlainLiteral(map.getAccount().getId().toString()));
    // // }
    // // }
    //
    // // Check to see if row has subject associated with it
    // // If it does, create relationships between study and datasets
    // Object subjectColumn = row.get(subjectColumnName);
    // if (subjectColumn != null)
    // {
    // String subjectValue = subjectColumn.toString();
    //
    // if (subjectValue != null && !subjectValue.isEmpty())
    // {
    // Property subjectProperty = ResourceFactory.createProperty(DataElementRDF.URI_NS
    // + subjectColumnName);
    // model.add(studyResource, subjectProperty, ResourceFactory.createPlainLiteral(subjectValue));
    // model.add(datasetResource, subjectProperty,
    // ResourceFactory.createPlainLiteral(subjectValue));
    // }
    // }
    //
    // // Create RDF for rest of the columns
    // for (DataStoreTabularColumnInfo columnInfo : tabInfo.getColumnInfos())
    // {
    // String columnName = columnInfo.getColumnName().toLowerCase();
    //
    // // Ignore srj and subject columns because those were done earlier
    // if (columnName != submissionRecordJoinColumnName && columnName != subjectColumnName)
    // {
    // Object column = row.get(columnName);
    //
    // if (column != null)
    // {
    // String columnValue = column.toString();
    //
    // if (columnValue != null && !columnValue.isEmpty())
    // {
    // model.add(rowResource,
    // ResourceFactory.createProperty(DataElementRDF.URI_NS + columnName),
    // ResourceFactory.createPlainLiteral(columnValue));
    // }
    // }
    //
    // }
    // }
    // }
    // }
    //
    // writeToFile(exportLocation + repositoryFileName, model, "TURTLE", false);
    //
    // return model;
    //
    // }
    // catch (Exception e)
    // {
    // e.printStackTrace();
    // assertTrue(false);
    // return null;
    // }
    // }

    /**************************************
     * 
     * Helper Methods
     * 
     *************************************/

    public void writeToFile(String filePath, Model m, String lang, Boolean printTriples)
    {

        if (printTriples)
        {
            m.write(System.out, "TURTLE");
        }

        try
        {
            DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH-mm-ss");
            Calendar cal = Calendar.getInstance();
            filePath = filePath + "(" + dateFormat.format(cal.getTime()) + ")";
            FileOutputStream fout = new FileOutputStream(filePath);
            m.write(fout, lang);
        }
        catch (IOException e)
        {
        	logger.debug("Exception caught" + e.getMessage());
        }

        logger.debug("File Wrriten :" + filePath);

    }

    private String getStoreName(RepeatableGroup repeatableGroup)
    {

        return (repeatableGroup.getDataStructure().getShortName() + "_rg" + repeatableGroup.getId() + "_v" + repeatableGroup
                .getDataStructure().getVersion()).toLowerCase();
    }

    /******************
     * 
     * 
     * Dictionary DAO
     * 
     ******************/

    // @SuppressWarnings("unchecked")
    // public List<DataElement> listPublishedDataElements()
    // {
    //
    // TestHibernateFactory.getSessionFactoryDictionary().getCurrentSession().beginTransaction();
    // Criteria crit = TestHibernateFactory.getSessionFactoryDictionary().getCurrentSession()
    // .createCriteria(DataElement.class);
    // crit.add(Restrictions.eq("status", DataElementStatus.PUBLISHED));
    // crit = crit.setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY);
    //
    // List<DataElement> des = crit.list();
    //
    // return des;
    // }

    // public DataElement getDataElementById(Long id)
    // {
    //
    // TestHibernateFactory.getSessionFactoryDictionary().getCurrentSession().beginTransaction();
    // Criteria crit = TestHibernateFactory.getSessionFactoryDictionary().getCurrentSession()
    // .createCriteria(DataElement.class);
    // crit.add(Restrictions.eq("id", id));
    //
    // DataElement de = (DataElement) crit.uniqueResult();
    //
    // return de;
    // }

    // public DataElement getDataElementByMapElementId(Long id)
    // {
    //
    // TestHibernateFactory.getSessionFactoryDictionary().getCurrentSession().beginTransaction();
    // Criteria crit = TestHibernateFactory.getSessionFactoryDictionary().getCurrentSession()
    // .createCriteria(MapElement.class);
    // crit.add(Restrictions.eq("id", id));
    //
    // MapElement me = (MapElement) crit.uniqueResult();
    //
    // return me.getDataElement();
    // }

    // public DataStructure getFormStructureById(Long id)
    // {
    //
    // TestHibernateFactory.getSessionFactoryDictionary().getCurrentSession().beginTransaction();
    // Criteria crit = TestHibernateFactory.getSessionFactoryDictionary().getCurrentSession()
    // .createCriteria(DataStructure.class);
    // crit.add(Restrictions.eq("id", id));
    //
    // DataStructure form = (DataStructure) crit.uniqueResult();
    //
    // return form;
    // }

    // @SuppressWarnings("unchecked")
    // public List<DataStructure> listPublishedFormStructures()
    // {
    //
    // TestHibernateFactory.getSessionFactoryDictionary().getCurrentSession().beginTransaction();
    // Criteria crit = TestHibernateFactory.getSessionFactoryDictionary().getCurrentSession()
    // .createCriteria(DataStructure.class);
    // crit.add(Restrictions.eq("status", StatusType.PUBLISHED));
    // crit = crit.setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY);
    //
    // List<DataStructure> forms = crit.list();
    //
    // return forms;
    // }

    // public RepeatableGroup getRepeatableGroupById(Long id)
    // {
    //
    // TestHibernateFactory.getSessionFactoryDictionary().getCurrentSession().beginTransaction();
    // Criteria crit = TestHibernateFactory.getSessionFactoryDictionary().getCurrentSession()
    // .createCriteria(RepeatableGroup.class);
    // crit.add(Restrictions.eq("id", id));
    //
    // RepeatableGroup form = (RepeatableGroup) crit.uniqueResult();
    //
    // return form;
    // }

    /******************
     * 
     * 
     * Meta DAO
     * 
     ******************/

    // public Boolean isFormInDataset(Long formId)
    // {
    //
    // TestHibernateFactory.getSessionFactoryMeta().getCurrentSession().beginTransaction();
    // Criteria crit = TestHibernateFactory.getSessionFactoryMeta().getCurrentSession()
    // .createCriteria(DatasetDataStructure.class);
    // crit.add(Restrictions.eq("dataStructureId", formId));
    // crit = crit.setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY);
    //
    // List<DatasetDataStructure> results = crit.list();
    //
    // if (results.isEmpty())
    // {
    // return false;
    // }
    // else
    // {
    // return true;
    // }
    // }

    // @SuppressWarnings("unchecked")
    // // StudyDAOImpl.getAll()
    // public List<Study> listStudies()
    // {
    //
    // TestHibernateFactory.getSessionFactoryMeta().getCurrentSession().beginTransaction();
    // Criteria crit = TestHibernateFactory.getSessionFactoryMeta().getCurrentSession().createCriteria(Study.class);
    // crit = crit.setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY);
    //
    // List<Study> study = crit.list();
    //
    // return study;
    // }

    // StudyDAOImpl.get(id)
    // public Study getStudyById(Long id)
    // {
    //
    // TestHibernateFactory.getSessionFactoryMeta().getCurrentSession().beginTransaction();
    // Criteria crit = TestHibernateFactory.getSessionFactoryMeta().getCurrentSession().createCriteria(Study.class);
    // crit.add(Restrictions.eq("id", id));
    //
    // Study study = (Study) crit.uniqueResult();
    //
    // return study;
    // }

    // DatasetDaoImpl.getAll()
    // @SuppressWarnings("unchecked")
    // public List<Dataset> listDatasets()
    // {
    //
    // TestHibernateFactory.getSessionFactoryMeta().getCurrentSession().beginTransaction();
    // Criteria crit = TestHibernateFactory.getSessionFactoryMeta().getCurrentSession().createCriteria(Dataset.class);
    // crit = crit.setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY);
    //
    // List<Dataset> datasets = crit.list();
    //
    // return datasets;
    // }

    // DatasetDaoImpl.get(id)
    // public Dataset getDatasetById(Long id)
    // {
    //
    // TestHibernateFactory.getSessionFactoryMeta().getCurrentSession().beginTransaction();
    // Criteria crit = TestHibernateFactory.getSessionFactoryMeta().getCurrentSession().createCriteria(Dataset.class);
    // crit.add(Restrictions.eq("id", id));
    //
    // Dataset result = (Dataset) crit.uniqueResult();
    // return result;
    //
    // }

    // @SuppressWarnings("unchecked")
    // public List<Dataset> getDatasetsByStatus(DatasetStatus status)
    // {
    //
    // TestHibernateFactory.getSessionFactoryMeta().getCurrentSession().beginTransaction();
    // Criteria crit = TestHibernateFactory.getSessionFactoryMeta().getCurrentSession().createCriteria(Dataset.class);
    // crit.add(Restrictions.eq("datasetStatus", status));
    // crit = crit.setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY);
    //
    // List<Dataset> datasets = crit.list();
    //
    // return datasets;
    // }

    // @SuppressWarnings("unchecked")
    // public List<Long> getDatasetIdsByStatus(DatasetStatus status)
    // {
    //
    // TestHibernateFactory.getSessionFactoryMeta().getCurrentSession().beginTransaction();
    // Criteria crit = TestHibernateFactory.getSessionFactoryMeta().getCurrentSession().createCriteria(Dataset.class)
    // .setProjection(Projections.property("id"));
    // crit.add(Restrictions.eq("datasetStatus", status));
    // crit = crit.setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY);
    //
    // List<Long> datasetIds = crit.list();
    //
    // return datasetIds;
    // }

    // @SuppressWarnings("unchecked")
    // public List<EntityMap> listEntityMaps()
    // {
    //
    // TestHibernateFactory.getSessionFactoryMeta().getCurrentSession().beginTransaction();
    // Criteria crit = TestHibernateFactory.getSessionFactoryMeta().getCurrentSession()
    // .createCriteria(EntityMap.class);
    // crit = crit.setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY);
    //
    // List<EntityMap> results = crit.list();
    //
    // return results;
    // }
    //
    // @SuppressWarnings("unchecked")
    // public List<EntityMap> getEntityMapsForDatasetByDatasetStatus(DatasetStatus status)
    // {
    //
    // List<Long> datasetIds = getDatasetIdsByStatus(status);
    //
    // TestHibernateFactory.getSessionFactoryMeta().getCurrentSession().beginTransaction();
    // Criteria crit = TestHibernateFactory.getSessionFactoryMeta().getCurrentSession()
    // .createCriteria(EntityMap.class);
    // crit.add(Restrictions.in("entityId", datasetIds));
    // crit.add(Restrictions.eq("type", EntityType.DATASET));
    // crit = crit.setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY);
    //
    // List<EntityMap> results = crit.list();
    //
    // return results;
    // }

    // @SuppressWarnings("unchecked")
    // public List<EntityMap> getEntityMapsByEntityId(Long entityId, EntityType type)
    // {
    //
    // TestHibernateFactory.getSessionFactoryMeta().getCurrentSession().beginTransaction();
    // Criteria crit = TestHibernateFactory.getSessionFactoryMeta().getCurrentSession()
    // .createCriteria(EntityMap.class);
    // crit.add(Restrictions.eq("entityId", entityId));
    // crit.add(Restrictions.eq("type", type));
    // crit = crit.setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY);
    //
    // List<EntityMap> results = crit.list();
    //
    // return results;
    // }

    /**
     * 
     * Not used Currently
     * 
     */
    // public DataStoreTabularInfo getDataStoreTabularInfoByTableName(String tableName)
    // {
    //
    // TestHibernateFactory.getSessionFactoryMeta().getCurrentSession().beginTransaction();
    // Criteria crit = TestHibernateFactory.getSessionFactoryMeta().getCurrentSession()
    // .createCriteria(DataStoreTabularInfo.class);
    //
    // crit = crit.add(Restrictions.eq("tableName", tableName));
    //
    // return (DataStoreTabularInfo) crit.uniqueResult();
    // }

    // @SuppressWarnings("unchecked")
    // public List<DataStoreInfo> listDataStoreInfo()
    // {
    //
    // TestHibernateFactory.getSessionFactoryMeta().getCurrentSession().beginTransaction();
    // Criteria crit = TestHibernateFactory.getSessionFactoryMeta().getCurrentSession()
    // .createCriteria(DataStoreInfo.class);
    // crit = crit.setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY);
    //
    // List<DataStoreInfo> results = crit.list();
    // return results;
    // }

    // @SuppressWarnings("unchecked")
    // public List<DataStoreTabularInfo> listDataStoreTabularInfo()
    // {
    //
    // TestHibernateFactory.getSessionFactoryMeta().getCurrentSession().beginTransaction();
    // Criteria crit = TestHibernateFactory.getSessionFactoryMeta().getCurrentSession()
    // .createCriteria(DataStoreTabularInfo.class);
    // crit = crit.setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY);
    //
    // List<DataStoreTabularInfo> results = crit.list();
    // return results;
    // }

    /******************
     * 
     * 
     * Repo DAO
     * 
     ******************/

    // TODO:REMOVE ME

    // @SuppressWarnings({ "rawtypes", "unchecked" })
    // public List listRepoTable(String tableName)
    // {
    //
    // String queryString = "SELECT * FROM " + tableName;
    //
    // TestHibernateFactory.getSessionFactoryRepo().getCurrentSession().beginTransaction();
    // Query query = TestHibernateFactory.getSessionFactoryRepo().getCurrentSession().createSQLQuery(queryString);
    // query.setResultTransformer(AliasToEntityMapResultTransformer.INSTANCE);
    // List<Map<String, Object>> results = query.list();
    // return results;
    //
    // }

    /**
     * 
     * Not used Currently
     * 
     */
    // @SuppressWarnings({ "unchecked" })
    // public Object getRepoTableRow(String tableName, Long submissionRecordJoinId)
    // {
    //
    // String queryString = "SELECT * FROM " + tableName + "WHERE submission_record_join_id ="
    // + submissionRecordJoinId;
    //
    // TestHibernateFactory.getSessionFactoryRepo().getCurrentSession().beginTransaction();
    // Query query = TestHibernateFactory.getSessionFactoryRepo().getCurrentSession().createSQLQuery(queryString);
    // query.setResultTransformer(AliasToEntityMapResultTransformer.INSTANCE);
    // List<Map<String, Object>> results = query.list();
    // return results;
    //
    // }

    // public SubmissionRecordJoin getSubmissionRecordJoin(Long id)
    // {
    //
    // TestHibernateFactory.getSessionFactoryRepo().getCurrentSession().beginTransaction();
    // Criteria crit = TestHibernateFactory.getSessionFactoryRepo().getCurrentSession()
    // .createCriteria(SubmissionRecordJoin.class);
    // crit.add(Restrictions.eq("id", id));
    //
    // SubmissionRecordJoin result = (SubmissionRecordJoin) crit.uniqueResult();
    // return result;
    //
    // }

    /**
     * 
     * Not used Currently
     * 
     */
    // public List joinForm(DataStructure form)
    // {
    //
    // String queryString = "SELECT * FROM ";
    // List<String> tableNames = new ArrayList<String>();
    //
    // String firstJoinRG = "";
    //
    // for (RepeatableGroup rg : form.getRepeatableGroups())
    // {
    // String tableName = getStoreName(rg);
    //
    // // Check to see if this is the first table join
    // if (tableNames.isEmpty())
    // {
    // firstJoinRG = rg.getName();
    // queryString = queryString + tableName + " AS " + firstJoinRG + " ";
    // }
    // else
    // {
    // String currentJoinRG = rg.getName();
    // queryString = queryString + "LEFT JOIN " + tableName + " AS " + currentJoinRG + " ON " + firstJoinRG
    // + ".submission_record_join_id = " + currentJoinRG + ".submission_record_join_id ";
    // }
    //
    // tableNames.add(tableName);
    //
    // }
    //
    // TestHibernateFactory.getSessionFactoryRepo().getCurrentSession().beginTransaction();
    // Query query = TestHibernateFactory.getSessionFactoryRepo().getCurrentSession().createSQLQuery(queryString);
    // query.setResultTransformer(AliasToEntityMapResultTransformer.INSTANCE);
    // List<Map<String, Object>> results = query.list();
    // return results;
    //
    // }

    // public List joinDataStoreInfo(DataStoreInfo dsInfo)
    // {
    //
    // String queryStringSelect = "";
    // String queryStringFrom = " FROM ";
    // String firstJoinTable = "";
    //
    // // Using " " for table names and column names because apparently they both may contain spaces
    // // Also aliasing with the rg table name so that when we get the result set we know which column belongs to which
    // // rg after the join
    // // After aliasing table / column names casing matters so must lower case them all.
    // for (DataStoreTabularInfo tabInfo : dsInfo.getDataStoreTabularInfos())
    // {
    // String tableName = tabInfo.getTableName();
    // String currentJoinTable = "";
    //
    // // Build query portion from FROM on with joins
    // // Check to see if this is the first table join
    // if (firstJoinTable.isEmpty() || firstJoinTable.equals(""))
    // {
    // firstJoinTable = tabInfo.getTableName().toLowerCase();
    // currentJoinTable = tabInfo.getTableName().toLowerCase();
    // queryStringFrom = queryStringFrom + "\"" + tableName + "\" AS \"" + firstJoinTable + "\"";
    // }
    // else
    // {
    // currentJoinTable = tabInfo.getTableName();
    // queryStringFrom = queryStringFrom + " LEFT JOIN \"" + tableName + "\" AS \"" + currentJoinTable
    // + "\" ON \"" + firstJoinTable + "\".\"" + COLUMN_NAME_SUBMISSION_RECORD_JOIN_ID + "\" = \""
    // + currentJoinTable + "\".\"" + COLUMN_NAME_SUBMISSION_RECORD_JOIN_ID + "\" ";
    // }
    //
    // // Build select portion of query
    // for (DataStoreTabularColumnInfo column : tabInfo.getColumnInfos())
    // {
    // if (queryStringSelect.isEmpty() || queryStringSelect.equals(""))
    // {
    // queryStringSelect = "SELECT \"" + currentJoinTable + "\".\"" + column.getColumnName().toLowerCase()
    // + "\" AS \"" + currentJoinTable + "." + column.getColumnName().toLowerCase() + "\" ";
    // }
    // else
    // {
    // queryStringSelect = queryStringSelect + ", \"" + currentJoinTable + "\".\""
    // + column.getColumnName().toLowerCase() + "\" AS \"" + currentJoinTable + "."
    // + column.getColumnName().toLowerCase() + "\" ";
    // }
    //
    // }
    //
    // }
    //
    // // if there are no de's in the form just make a correct sql statement
    // if (queryStringSelect.isEmpty() || queryStringSelect.equals(""))
    // {
    // queryStringSelect = "SELECT * ";
    // }
    //
    // // Add Dataset Id to query, join to submission_record_join table
    // if (!firstJoinTable.isEmpty() && !firstJoinTable.equals(""))
    // {
    //
    // queryStringSelect = queryStringSelect + ", \"" + TABLE_NAME_SUBMISSION_RECORD_JOIN + "\".\""
    // + COLUMN_NAME_DATASETID + "\" AS \"" + COLUMN_NAME_DATASETID + "\" ";
    //
    // queryStringFrom = queryStringFrom + " LEFT JOIN \"" + TABLE_NAME_SUBMISSION_RECORD_JOIN + "\" ON \""
    // + firstJoinTable + "\".\"" + COLUMN_NAME_SUBMISSION_RECORD_JOIN_ID + "\" = \""
    // + TABLE_NAME_SUBMISSION_RECORD_JOIN + "\".\"" + "id" + "\" ";
    // }
    //
    // String queryString = queryStringSelect + queryStringFrom;
    //
    //
    // TestHibernateFactory.getSessionFactoryRepo().getCurrentSession().beginTransaction();
    // Query query = TestHibernateFactory.getSessionFactoryRepo().getCurrentSession().createSQLQuery(queryString);
    // query.setResultTransformer(Transformers.ALIAS_TO_ENTITY_MAP);
    // // List<Map<String, Object>> results = query.list();
    // List results = query.list();
    //
    //
    // return results;
    //
    // }

    /*****************
     * 
     * RANDOM SECURITY TESTS
     * 
     */

    public Account get(Long id)
    {

        Criteria crit = TestHibernateFactory.getSessionFactoryMeta().getCurrentSession().createCriteria(Account.class);

        crit = crit.add(Restrictions.eq("id", id)).setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY);

        Account account = (Account) crit.uniqueResult();

        if (account != null)
        {
            for (PermissionGroupMember pgm : account.getPermissionGroupMemberList())
            {
                pgm.getPermissionGroup().toString();
            }
        }

        return account;
    }

    public List<EntityMap> listUserAccess(Account account, EntityType type, boolean isAdmin)
    {

        Set<PermissionGroup> currentGroups = new HashSet<PermissionGroup>();
        currentGroups.addAll(getPublicGroups());
        currentGroups.addAll(getGrantedGroups(account));

        List<EntityMap> emList;
        if (isAdmin)
        {
            emList = listUserAccess(account, currentGroups, type, false);
        }
        else
        {
            emList = listUserAccess(account, currentGroups, type, true);
        }
        return emList;
    }

    @SuppressWarnings("unchecked")
    public List<PermissionGroup> getPublicGroups()
    {

        Criteria crit = TestHibernateFactory.getSessionFactoryMeta().getCurrentSession()
                .createCriteria(PermissionGroup.class);

        crit = crit.add(Restrictions.eq(PermissionGroup.PUBLIC_STATUS, true)).setResultTransformer(
                Criteria.DISTINCT_ROOT_ENTITY);

        List<PermissionGroup> list = crit.list();

        for (PermissionGroup pg : list)
        {
            pg.getMemberSet().toString();
        }

        return list;
    }

    @SuppressWarnings("unchecked")
    public List<PermissionGroup> getGrantedGroups(Account account)
    {

        Criteria crit = TestHibernateFactory.getSessionFactoryMeta().getCurrentSession()
                .createCriteria(PermissionGroup.class);

        crit = crit.createAlias("memberSet", "members").add(Restrictions.eq("members.account", account))
                .setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY);

        return crit.list();
    }

    public List<EntityMap> listUserAccess(Account account, Set<PermissionGroup> currentGroups, EntityType type,
            boolean isAdmin)
    {

        Criteria crit = TestHibernateFactory.getSessionFactoryMeta().getCurrentSession()
                .createCriteria(EntityMap.class);

        if (!isAdmin)
        {
            if (!currentGroups.isEmpty())
            {
                crit = crit.add(Restrictions.disjunction()
                        .add(Restrictions.in(EntityMap.PERMISSION_GROUP, currentGroups))
                        .add(Restrictions.eq(EntityMap.ACCOUNT, account)));
            }
            else
            {
                crit.add(Restrictions.eq(EntityMap.ACCOUNT, account));
            }
        }

        crit = crit.add(Restrictions.eq("type", type));

        List<EntityMap> out = crit.list();

        return out;
    }

}
