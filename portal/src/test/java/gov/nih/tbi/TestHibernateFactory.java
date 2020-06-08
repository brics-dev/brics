
package gov.nih.tbi;

import gov.nih.tbi.account.model.hibernate.Account;
import gov.nih.tbi.account.model.hibernate.AccountRole;
import gov.nih.tbi.account.model.hibernate.EntityMap;
import gov.nih.tbi.account.model.hibernate.PermissionGroup;
import gov.nih.tbi.account.model.hibernate.PermissionGroupMember;
import gov.nih.tbi.commons.model.ConditionalOperators;
import gov.nih.tbi.commons.model.DataElementStatus;
import gov.nih.tbi.commons.model.DataType;
import gov.nih.tbi.commons.model.InputRestrictions;
import gov.nih.tbi.commons.model.RepeatableType;
import gov.nih.tbi.commons.model.RequiredType;
import gov.nih.tbi.commons.model.hibernate.Country;
import gov.nih.tbi.commons.model.hibernate.FileType;
import gov.nih.tbi.commons.model.hibernate.State;
import gov.nih.tbi.commons.model.hibernate.User;
import gov.nih.tbi.dictionary.model.hibernate.Alias;
import gov.nih.tbi.dictionary.model.hibernate.Category;
import gov.nih.tbi.dictionary.model.hibernate.Classification;
import gov.nih.tbi.dictionary.model.hibernate.ClassificationElement;
import gov.nih.tbi.dictionary.model.hibernate.Condition;
import gov.nih.tbi.dictionary.model.hibernate.DataElement;
import gov.nih.tbi.dictionary.model.hibernate.Disease;
import gov.nih.tbi.dictionary.model.hibernate.DiseaseElement;
import gov.nih.tbi.dictionary.model.hibernate.DiseaseStructure;
import gov.nih.tbi.dictionary.model.hibernate.Domain;
import gov.nih.tbi.dictionary.model.hibernate.ExternalId;
import gov.nih.tbi.dictionary.model.hibernate.FormStructure;
import gov.nih.tbi.dictionary.model.hibernate.Keyword;
import gov.nih.tbi.dictionary.model.hibernate.MapElement;
import gov.nih.tbi.dictionary.model.hibernate.MeasuringType;
import gov.nih.tbi.dictionary.model.hibernate.MeasuringUnit;
import gov.nih.tbi.dictionary.model.hibernate.Population;
import gov.nih.tbi.dictionary.model.hibernate.RepeatableGroup;
import gov.nih.tbi.dictionary.model.hibernate.SubDomain;
import gov.nih.tbi.dictionary.model.hibernate.Subgroup;
import gov.nih.tbi.dictionary.model.hibernate.ValidationPlugin;
import gov.nih.tbi.dictionary.model.hibernate.ValueRange;
import gov.nih.tbi.repository.model.hibernate.BasicStudy;
import gov.nih.tbi.repository.model.hibernate.ClinicalTrial;
import gov.nih.tbi.repository.model.hibernate.DataStoreBinaryInfo;
import gov.nih.tbi.repository.model.hibernate.DataStoreInfo;
import gov.nih.tbi.repository.model.hibernate.DataStoreTabularColumnInfo;
import gov.nih.tbi.repository.model.hibernate.DataStoreTabularInfo;
import gov.nih.tbi.repository.model.hibernate.DatafileEndpointInfo;
import gov.nih.tbi.repository.model.hibernate.Dataset;
import gov.nih.tbi.repository.model.hibernate.DatasetDataStructure;
import gov.nih.tbi.repository.model.hibernate.DatasetFile;
import gov.nih.tbi.repository.model.hibernate.DatasetSubject;
import gov.nih.tbi.repository.model.hibernate.Grant;
import gov.nih.tbi.repository.model.hibernate.Study;
import gov.nih.tbi.repository.model.hibernate.SubmissionRecordJoin;
import gov.nih.tbi.repository.model.hibernate.SupportingDocumentation;
import gov.nih.tbi.repository.model.hibernate.UserFile;
import junit.framework.TestCase;

import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
import org.hibernate.cfg.Environment;

public abstract class TestHibernateFactory extends TestCase
{

    // TODO:
    // Couldn't figure out how to scan for packages automatically for annnoted classes
    // So have to add classes manually right now
    // Also need to move JDBC to use properties file

    private static SessionFactory sessionFactoryMeta;
    private static SessionFactory sessionFactoryRepo;
    private static SessionFactory sessionFactoryDictionary;

    // Dev FITBIR
    // private final static String dbRepoUser = "tbi_reposdev";
    // private final static String dbRepoPass = "EQ>#5tFALTx2=O6KYLJLgxjNy3AGpl";
    // private final static String dbDictionaryUser = "dictionarydev";
    // private final static String dbDictionaryPass = "iSXysxZ=7cJW+Z4wP9tsTy*tWr+G$D";
    // private final static String dbMetaUser = "tbi_metadev";
    // private final static String dbMetaPass = "UJegjWh9DbvgmIPB3BbKzmHHskwbcv";
    // private final static String dbURL = "jdbc:postgresql://fitbir-db-dev.cit.nih.gov/tbi_dev";
    // private final static String dbDictionaryURL = "jdbc:postgresql://ibis-db-dev.cit.nih.gov/dictionary_dev";

    // // Stage FITBIR
    // private final static String dbRepoUser = "tbi_reposstg";
    // private final static String dbRepoPass = "X0CP9_gtt6EmIKCdeJcn!bzE$ebCp";
    // private final static String dbDictionaryUser = "dictionarystg";
    // private final static String dbDictionaryPass = "HN4sgsWyU<i9N8Fr4Y+u1yrNEnZuEO";
    // private final static String dbMetaUser = "tbi_metastg";
    // private final static String dbMetaPass = "bA0L!q6l4UyB=IQCSCBZhCFZ_OclIk";
    // private final static String dbURL = "jdbc:postgresql://fitbir-db-stage.cit.nih.gov/tbi_stage";
    // private final static String dbDictionaryURL = "jdbc:postgresql://ibis-db-stage.cit.nih.gov/dictionary_stage";

    // // Stage PDBP
    private final static String dbRepoUser = "tbi_reposstg";
    private final static String dbRepoPass = "X0CP9_gtt6EmIKCdeJcn!bzE$ebCp";
    private final static String dbDictionaryUser = "dictionarystg";
    private final static String dbDictionaryPass = "HN4sgsWyU<i9N8Fr4Y+u1yrNEnZuEO";
    private final static String dbMetaUser = "tbi_metastg";
    private final static String dbMetaPass = "bA0L!q6l4UyB=IQCSCBZhCFZ_OclIk";
    private final static String dbURL = "jdbc:postgresql://pdbp-db-stage.cit.nih.gov/pd_stg";
    private final static String dbDictionaryURL = "jdbc:postgresql://ibis-db-stage.cit.nih.gov/dictionary_stage";

    // Demo FITBIR
    // private final static String dbRepoUser = "tbi_reposdemo";
    // private final static String dbRepoPass = "yLBZh4PiiWdKmB92Ub72rNvnElfwcL";
    // private final static String dbDictionaryUser = "dictionarydemo";
    // private final static String dbDictionaryPass = "eFasJxPws0INALGA*L6b-HBcWNlYFz";
    // private final static String dbMetaUser = "tbi_metademo";
    // private final static String dbMetaPass = "WICFBVfb3yOeUYD#gXDW5xWSWt_Isb";
    // private final static String dbURL = "jdbc:postgresql://fitbir-db-demo.cit.nih.gov/tbi_demo";
    // private final static String dbDictionaryURL = "jdbc:postgresql://ibis-db-demo.cit.nih.gov/dictionary_demo";

    public static SessionFactory getSessionFactoryMeta()
    {

        if (sessionFactoryMeta == null)
        {
            buildSessionFactoryMeta();
        }
        return sessionFactoryMeta;
    }

    public static SessionFactory getSessionFactoryRepo()
    {

        if (sessionFactoryRepo == null)
        {
            buildSessionFactoryRepository();
        }
        return sessionFactoryRepo;
    }

    public static SessionFactory getSessionFactoryDictionary()
    {

        if (sessionFactoryDictionary == null)
        {
            buildSessionFactoryDictionary();
        }
        return sessionFactoryDictionary;
    }

    public static SessionFactory buildSessionFactoryDictionary()
    {

        try
        {
            Configuration configuration = new Configuration();
            configuration.setProperty(Environment.DRIVER, "org.postgresql.Driver");
            configuration.setProperty(Environment.URL, dbDictionaryURL);
            configuration.setProperty(Environment.USER, dbDictionaryUser);
            configuration.setProperty(Environment.PASS, dbDictionaryPass);

            configuration.setProperty(Environment.SHOW_SQL, "false");
            configuration.setProperty(Environment.CURRENT_SESSION_CONTEXT_CLASS, "thread");

            // configuration.setProperty("packagesToScan", "gov.nih.tbi");

            // Common Classes
            configuration.addAnnotatedClass(User.class);

            // Element Classes
            configuration.addAnnotatedClass(DataElement.class);
            configuration.addAnnotatedClass(DataType.class);
            configuration.addAnnotatedClass(InputRestrictions.class);
            configuration.addAnnotatedClass(ValidationPlugin.class);
            configuration.addAnnotatedClass(DataElementStatus.class);
            configuration.addAnnotatedClass(ValueRange.class);
            configuration.addAnnotatedClass(ClassificationElement.class);
            configuration.addAnnotatedClass(Population.class);
            configuration.addAnnotatedClass(DiseaseElement.class);
            configuration.addAnnotatedClass(Domain.class);
            configuration.addAnnotatedClass(SubDomain.class);
            configuration.addAnnotatedClass(MeasuringUnit.class);
            configuration.addAnnotatedClass(ExternalId.class);
            configuration.addAnnotatedClass(Alias.class);
            configuration.addAnnotatedClass(Classification.class);
            configuration.addAnnotatedClass(Subgroup.class);
            configuration.addAnnotatedClass(Disease.class);
            configuration.addAnnotatedClass(MeasuringType.class);
            configuration.addAnnotatedClass(Keyword.class);
            configuration.addAnnotatedClass(MapElement.class);
            configuration.addAnnotatedClass(RepeatableGroup.class);
            configuration.addAnnotatedClass(RepeatableType.class);
            configuration.addAnnotatedClass(FormStructure.class);
            configuration.addAnnotatedClass(RequiredType.class);
            configuration.addAnnotatedClass(Condition.class);
            configuration.addAnnotatedClass(ConditionalOperators.class);
            configuration.addAnnotatedClass(Category.class);

            // DataStructure Classes
            configuration.addAnnotatedClass(DiseaseStructure.class);

            sessionFactoryDictionary = configuration.buildSessionFactory();
            sessionFactoryDictionary.openSession();

            return sessionFactoryDictionary;
        }
        catch (Throwable ex)
        {
            // Make sure you log the exception, as it might be swallowed
            System.err.println("Initial SessionFactory creation failed." + ex);
            throw new ExceptionInInitializerError(ex);
        }
    }

    public static SessionFactory buildSessionFactoryMeta()
    {

        try
        {
            Configuration configuration = new Configuration();
            configuration.setProperty(Environment.DRIVER, "org.postgresql.Driver");
            configuration.setProperty(Environment.URL, dbURL);
            configuration.setProperty(Environment.USER, dbMetaUser);
            configuration.setProperty(Environment.PASS, dbMetaPass);

            configuration.setProperty(Environment.SHOW_SQL, "false");
            configuration.setProperty(Environment.CURRENT_SESSION_CONTEXT_CLASS, "thread");

            // configuration.setProperty("packagesToScan", "gov.nih.tbi");

            // Common Classes
            configuration.addAnnotatedClass(User.class);

            // Study Classes
            configuration.addAnnotatedClass(Study.class);
            configuration.addAnnotatedClass(BasicStudy.class);
            configuration.addAnnotatedClass(ClinicalTrial.class);
            configuration.addAnnotatedClass(Grant.class);
            configuration.addAnnotatedClass(SupportingDocumentation.class);

            // Dataset
            configuration.addAnnotatedClass(Dataset.class);
            configuration.addAnnotatedClass(UserFile.class);
            configuration.addAnnotatedClass(DatasetFile.class);
            configuration.addAnnotatedClass(DatasetDataStructure.class);
            configuration.addAnnotatedClass(DatasetSubject.class);
            configuration.addAnnotatedClass(FileType.class);
            configuration.addAnnotatedClass(DataStoreBinaryInfo.class);
            configuration.addAnnotatedClass(DatafileEndpointInfo.class);

            // Data Store
            configuration.addAnnotatedClass(DataStoreTabularInfo.class);
            configuration.addAnnotatedClass(DataStoreInfo.class);
            configuration.addAnnotatedClass(DataStoreTabularColumnInfo.class);

            // Entity Map
            configuration.addAnnotatedClass(Account.class);
            configuration.addAnnotatedClass(EntityMap.class);
            configuration.addAnnotatedClass(PermissionGroup.class);
            configuration.addAnnotatedClass(PermissionGroupMember.class);
            configuration.addAnnotatedClass(AccountRole.class);
            configuration.addAnnotatedClass(Country.class);
            configuration.addAnnotatedClass(State.class);

            sessionFactoryMeta = configuration.buildSessionFactory();
            sessionFactoryMeta.openSession();

            return sessionFactoryMeta;
        }
        catch (Throwable ex)
        {
            // Make sure you log the exception, as it might be swallowed
            System.err.println("Initial SessionFactory creation failed." + ex);
            throw new ExceptionInInitializerError(ex);
        }
    }

    public static SessionFactory buildSessionFactoryRepository()
    {

        try
        {
            Configuration configuration = new Configuration();
            configuration.setProperty(Environment.DRIVER, "org.postgresql.Driver");
            configuration.setProperty(Environment.URL, dbURL);
            configuration.setProperty(Environment.USER, dbRepoUser);
            configuration.setProperty(Environment.PASS, dbRepoPass);

            configuration.setProperty(Environment.SHOW_SQL, "false");
            configuration.setProperty(Environment.CURRENT_SESSION_CONTEXT_CLASS, "thread");

            configuration.addAnnotatedClass(SubmissionRecordJoin.class);

            sessionFactoryRepo = configuration.buildSessionFactory();
            sessionFactoryRepo.openSession();

            return sessionFactoryRepo;
        }
        catch (Throwable ex)
        {
            // Make sure you log the exception, as it might be swallowed
            System.err.println("Initial SessionFactory creation failed." + ex);
            throw new ExceptionInInitializerError(ex);
        }
    }

    public static boolean isSessionFactoryMetaInitialized()
    {

        return (TestHibernateFactory.sessionFactoryMeta != null);
    }

    public static boolean isSessionFactoryRepoInitialized()
    {

        return (TestHibernateFactory.sessionFactoryRepo != null);
    }

    public static boolean isSessionFactoryDictionaryInitialized()
    {

        return (TestHibernateFactory.sessionFactoryDictionary != null);
    }

    public static void closeSessions()
    {

        if (isSessionFactoryDictionaryInitialized())
        {
            sessionFactoryDictionary.close();
        }

        if (isSessionFactoryMetaInitialized())
        {
            sessionFactoryMeta.close();
        }

        if (isSessionFactoryRepoInitialized())
        {
            sessionFactoryRepo.close();
        }
    }
}