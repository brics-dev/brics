package gov.nih.tbi.repository.service.hibernate;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

import gov.nih.tbi.common.util.ProformsWsProvider;
import gov.nih.tbi.commons.model.BRICSTimeDateUtil;
import gov.nih.tbi.commons.service.AdvancedVisualizationManager;
import gov.nih.tbi.commons.service.QueryToolManager;
import gov.nih.tbi.commons.service.RepositoryManager;
import gov.nih.tbi.repository.dao.DatasetDao;
import gov.nih.tbi.repository.model.SubmissionType;
import gov.nih.tbi.repository.model.SummaryDataCache;
import gov.nih.tbi.repository.model.hibernate.Grant;
import gov.nih.tbi.repository.model.hibernate.ResearchManagement;
import gov.nih.tbi.repository.model.hibernate.Study;
import gov.nih.tbi.repository.model.hibernate.StudyForm;
import gov.nih.tbi.repository.model.hibernate.StudySite;
import gov.nih.tbi.repository.model.hibernate.VisualizationDataset;


public class AdvancedVisualizationManagerTest {


	private String domain;

	@Mock
	protected RepositoryManager repositoryManager;

	@Mock
	private QueryToolManager queryToolManager;
	
	@Mock
	private  ProformsWsProvider proformsWsProvider;
	
	@Mock
	DatasetDao datasetDao;
	
	@InjectMocks
	AdvancedVisualizationManager avManager = new AdvancedVisualizationManagerImpl();
	
	@BeforeMethod
    public void setUp() {
        MockitoAnnotations.initMocks(this);
    }
	
	
	
	@Test
	public void getStudyDataTypes() {
		
		Study study = new Study();
		study.setId(1l);
		Map<Long, Set<SubmissionType>> studySubmissionTypes = new HashMap<Long, Set<SubmissionType>>();
		Set<SubmissionType> typeSet = new HashSet<SubmissionType>();
		typeSet.add(SubmissionType.CLINICAL);
		studySubmissionTypes.put(1l,typeSet);
		JsonArray studyDataTypes = avManager.getStudyDataTypes(study,studySubmissionTypes);
		
		JsonArray expectedStudyDataTypes = new JsonArray();
		expectedStudyDataTypes.add(new JsonPrimitive("clinical"));
		
		Assert.assertEquals(studyDataTypes,expectedStudyDataTypes);
	
	}
	
	@Test
	public void getStudyJsonForms() {
		Study study = mock(Study.class);
		Set<StudyForm> mockStudyForms = new HashSet<StudyForm>();
		StudyForm studyForm = new StudyForm();
		studyForm.setTitle("test form");
		mockStudyForms.add(studyForm);
		when(study.getStudyForms()).thenReturn(mockStudyForms);
		JsonArray forms = avManager.getStudyJsonForms(study);
		
		JsonArray expectedForms = new JsonArray();
		expectedForms.add(avManager.valueOrEmpty("test form"));
		Assert.assertEquals(forms,expectedForms);
		
	}
	
	@Test
	public void getStudySummaryGraphData() {
		
		List<String> studyChartNames  = new ArrayList<String>();
		studyChartNames.add("Sample Chart");
		Study study = mock(Study.class);
		study.setId(1l);
		JsonObject studyData =  avManager.getStudySummaryGraphData(studyChartNames, study);
		SummaryDataCache sdCache = SummaryDataCache.getInstance(queryToolManager, proformsWsProvider, null);
		JsonObject expectedStudyData = new JsonObject();
		expectedStudyData.addProperty("dateUpdated", BRICSTimeDateUtil.formatDate(sdCache.getUpdateDate()));
		Assert.assertEquals(studyData,expectedStudyData);
	}
	
	@Test
	public void getStudyPis() {
		
		Set<ResearchManagement> mockRm  = new HashSet<ResearchManagement>();
		ResearchManagement rm = new ResearchManagement();
		rm.setFirstName("Test");
		rm.setLastName("User");
		rm.setRole(1l);
		rm.setOrgName("TestOrg");
		mockRm.add(rm);
		Study study = mock(Study.class);
		when(study.getResearchMgmtSet()).thenReturn(mockRm);
		JsonArray piListJson =  avManager.getStudyPis(study);
		
		
		
		JsonArray expectedPiListJson = new JsonArray();
		JsonObject pi = new JsonObject();
		pi.add("name", avManager.valueOrEmpty("Test User"));
		pi.add("role", avManager.valueOrEmpty("Principal Investigator"));
		pi.add("image", new JsonPrimitive(domain + "sites/default/files/genericImage.jpg"));
		pi.add("institute", avManager.valueOrEmpty("TestOrg"));
		expectedPiListJson.add(pi);
		
		
		Assert.assertEquals(piListJson, expectedPiListJson);
	}
	
	@Test
	public void getStudySites() {
		
		Set<StudySite> mockStudySiteSet  = new HashSet<StudySite>();
		StudySite ss = new StudySite();
		ss.setSiteName("Test Site");
		ss.setIsPrimary("True");
		mockStudySiteSet.add(ss);
		Study study = mock(Study.class);
		when(study.getStudySiteSet()).thenReturn(mockStudySiteSet);
		JsonArray studySitesJson =  avManager.getStudySites(study);
		
		
		
		JsonObject locationObject = new JsonObject();
		locationObject.addProperty("lat", 41.2281187);
		locationObject.addProperty("lng", -73.99549689999999);
		locationObject.addProperty("place", "Test Site");
		JsonArray expectedStudySitesJson = new JsonArray();
		JsonObject studySiteJson = new JsonObject();
		studySiteJson.add("institute", avManager.valueOrEmpty("Test Site"));
		studySiteJson.add("location", locationObject);
		studySiteJson.add("primary", new JsonPrimitive(true));
		expectedStudySitesJson.add(studySiteJson);
		Assert.assertEquals(studySitesJson,expectedStudySitesJson);
	}
	
	@Test
	public void getStudyGrantIdList() {
		
		
		Set<Grant> mockGrantSet  = new HashSet<Grant>();
		Grant grant = new Grant();
		grant.setGrantId("12345");
		grant.setGrantName("Test Grant");
		grant.setId(1l);
		
		mockGrantSet.add(grant);
		Study study = mock(Study.class);
		when(study.getGrantSet()).thenReturn(mockGrantSet);
		String grantId =  avManager.getStudyGrantIdList(study);
		
		
		
		
		String expectedGrantId = "12345";
		Assert.assertEquals(grantId,expectedGrantId);
		
	}
	
	@Test
	public void getStudySharingStatus() {
		
		List<VisualizationDataset> mockDataSet  = new ArrayList<VisualizationDataset>();
		VisualizationDataset vds = new VisualizationDataset();
		vds.setDatasetStatusId(1);
		vds.setId(1);
		vds.setStudyId(1);
		mockDataSet.add(vds);
		Study study = mock(Study.class);
		//DatasetDao datasetDao = mock(DatasetDao.class);
		when(datasetDao.getVisualizationStudyDatasetByStudy(study)).thenReturn(mockDataSet);
		String dataSharing =  avManager.getStudySharingStatus(study);
		

		String expectedDataSharing = "shared";
		Assert.assertEquals(dataSharing,expectedDataSharing);

	}

}
