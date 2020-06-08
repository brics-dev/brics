package gov.nih.tbi.dictionary.portal;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import javax.net.ssl.HttpsURLConnection;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletResponse;

import org.apache.http.impl.cookie.DateParseException;
import org.apache.log4j.Logger;
import org.apache.struts2.ServletActionContext;
import org.json.JSONArray;
import org.json.JSONObject;

import gov.nih.tbi.PortalConstants;
import gov.nih.tbi.commons.model.DataElementStatus;
import gov.nih.tbi.commons.model.DataType;
import gov.nih.tbi.commons.model.InputRestrictions;
import gov.nih.tbi.commons.model.RepeatableType;
import gov.nih.tbi.commons.model.StatusType;
import gov.nih.tbi.commons.service.ServiceConstants;
import gov.nih.tbi.dictionary.model.FormStructureStandardization;
import gov.nih.tbi.dictionary.model.hibernate.Category;
import gov.nih.tbi.dictionary.model.hibernate.Classification;
import gov.nih.tbi.dictionary.model.hibernate.ClassificationElement;
import gov.nih.tbi.dictionary.model.hibernate.DataElement;
import gov.nih.tbi.dictionary.model.hibernate.Disease;
import gov.nih.tbi.dictionary.model.hibernate.DiseaseStructure;
import gov.nih.tbi.dictionary.model.hibernate.Domain;
import gov.nih.tbi.dictionary.model.hibernate.FormStructure;
import gov.nih.tbi.dictionary.model.hibernate.MapElement;
import gov.nih.tbi.dictionary.model.hibernate.Population;
import gov.nih.tbi.dictionary.model.hibernate.RepeatableGroup;
import gov.nih.tbi.dictionary.model.hibernate.StructuralDataElement;
import gov.nih.tbi.dictionary.model.hibernate.SubDomain;
import gov.nih.tbi.dictionary.model.hibernate.SubDomainElement;
import gov.nih.tbi.dictionary.model.hibernate.Subgroup;
import gov.nih.tbi.dictionary.model.hibernate.ValueRange;
import gov.nih.tbi.repository.model.SubmissionType;

/**
 * Action for PROMIS data structure
 * 
 * @author Ching-Heng Lin
 * 
 */
public class PromisDataStructureAction extends BaseDictionaryAction{
	private static final long serialVersionUID = -8338378659412817746L;
	private static Logger logger = Logger.getLogger(PromisDataStructureAction.class);	
	private InputStream inputStream;
	private String contentType;
	private String fileName;
	private static String BATTERY_FLAG = "/Battery";
	
 	public String getContentType() {
		return contentType;
	}

	public String getFileName() {
		return fileName;
	}

	public InputStream getInputStream() {
		return inputStream;
	}
	
	public String adminListNonexistentPromisForms() {
		return PortalConstants.ACTION_LIST;
	}
	
	public String adminListNonexistentPromisFormsTable() {
		//
		Properties laguageDetectionProperties = new Properties();
		Properties hmProperties = new Properties();
		ServletContext aContext = getSession().getServletContext();
		InputStream mTypeS = aContext.getResourceAsStream("measurementType.properties");
		InputStream hmS = aContext.getResourceAsStream("healthMeasurement.properties");
		// web service call		
		byte[] postData = "".getBytes();
		DataOutputStream writer = null;
		DataOutputStream batteryWriter = null;
		HttpURLConnection connection = null;
		String output;
		JSONObject formListObj = null;
		JSONArray formListArr = new JSONArray();
		JSONArray filtedFormListArr = new JSONArray();
		int inx = 0;
		try {
			laguageDetectionProperties.load(mTypeS);
			hmProperties.load(hmS);
			String apiUrl = hmProperties.getProperty("healthMeasurement.api.url");
			URL url = new URL(apiUrl+".json");
			String token = hmProperties.getProperty("healthMeasurement.api.token");
			byte[] encodedBytes = Base64.getEncoder().encode(token.getBytes());
			Charset ascii = Charset.forName("US-ASCII");
			String asciiEncoded = new String(encodedBytes, ascii);
			
			connection = (HttpsURLConnection) url.openConnection();
			connection.setDoOutput(true);
			connection.setRequestMethod("POST");
			connection.addRequestProperty("Authorization", "Basic "+asciiEncoded);
			connection.addRequestProperty("Content-Length", "0");
			connection.connect();			
			writer = new DataOutputStream(connection.getOutputStream());
			writer.write(postData);
			
			BufferedReader br = new BufferedReader(new InputStreamReader((connection.getInputStream())));
						
			while ((output = br.readLine()) != null) {
				formListObj = new JSONObject(output);
			}
			connection.disconnect();
			formListArr = formListObj.getJSONArray("Form");

			String batteryAapiUrl = hmProperties.getProperty("healthMeasurement.api.batteries.url");
			URL batteryUrl = new URL(batteryAapiUrl+".json");
			connection = (HttpsURLConnection) batteryUrl.openConnection();
			connection.setDoOutput(true);
			connection.setRequestMethod("POST");
			connection.addRequestProperty("Authorization", "Basic "+asciiEncoded);
			connection.addRequestProperty("Content-Length", "0");
			connection.connect();
			batteryWriter = new DataOutputStream(connection.getOutputStream());
			batteryWriter.write(postData);
			JSONObject batteryFormObj = null;
			BufferedReader batteryBr = new BufferedReader(new InputStreamReader((connection.getInputStream())));
			while ((output = batteryBr.readLine()) != null) {
				batteryFormObj = new JSONObject(output);
			}
			connection.disconnect();
			JSONArray batteryFormArr = batteryFormObj.getJSONArray("Battery");		
			for(int i=0 ;i<batteryFormArr.length();i++) {
				formListArr.put(batteryFormArr.get(i));
			}

			for(int i=0;i<formListArr.length();i++) {	
				JSONObject ob = (JSONObject) formListArr.get(i);
				String fName = ob.getString("Name");
				String mTypes = laguageDetectionProperties.getProperty(fName.replaceAll("\\s+",".").replace(":",""));				

				if(mTypes != null) { // the properties file only constants English forms
					List<String> mTypeList = Arrays.asList(mTypes.split(","));
					for(String mType : mTypeList) {
						String link = "<a href='promisDataStructureAction!exportFormStructureElement.action?oid="+ob.getString("OID")+"&fname="+URLEncoder.encode(fName,"UTF-8")+"&mtype="+URLEncoder.encode(mType,"UTF-8")+"'>"+fName+"</a>";
						JSONObject nob = new JSONObject("{\"OID\":\""+ob.getString("OID")+"\",\"Name\":\""+link+"\",\"type\":\""+mType+"\"}");
						filtedFormListArr.put(inx, nob);
						inx++;	
					}
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		// build data table object
		JSONObject dataTableJSON = new JSONObject();
		dataTableJSON.put("draw","1");
		dataTableJSON.put("recordsTotal",filtedFormListArr.length());
		dataTableJSON.put("recordsFiltered",filtedFormListArr.length());
		dataTableJSON.put("data",filtedFormListArr);
		
		
		HttpServletResponse response = ServletActionContext.getResponse();
		response.setContentType("application/json");
		try {
			response.getWriter().write(dataTableJSON.toString());
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return null;
	}
	
	public String exportFormStructureElement(){		
		String formOID = getRequest().getParameter("oid");
		String formName = getRequest().getParameter("fname").replaceAll(",", " ");
		String mType = getRequest().getParameter("mtype");
		Properties hmProperties = new Properties();
		ServletContext aContext = getSession().getServletContext();
		InputStream hmS = aContext.getResourceAsStream("healthMeasurement.properties");		
		
		byte[] postData = "".getBytes();
		String output;
		DataOutputStream writer = null;
		HttpURLConnection connection = null;
		JSONObject formObj = null;
		URL url;
		List<DataElement> elementList = null;
		List<DataElement> statisticElementList = null;
		ByteArrayOutputStream baos = null;
		try {
			hmProperties.load(hmS);
			String token = hmProperties.getProperty("healthMeasurement.api.token");
			byte[] encodedBytes = Base64.getEncoder().encode(token.getBytes());
			Charset ascii = Charset.forName("US-ASCII");
			String asciiEncoded = new String(encodedBytes, ascii);
			
			if(formName.contains(BATTERY_FLAG)) { //for the forms in battery list
				String batteryAapiUrl = hmProperties.getProperty("healthMeasurement.api.batteries.url");
				String apiUrl = hmProperties.getProperty("healthMeasurement.api.url");

				JSONArray batteryItems = dictionaryManager.getBatteryItemsJsonArray(batteryAapiUrl, apiUrl, formOID, asciiEncoded);
				
				// Data Elements====
				elementList = createElementList(batteryItems, formOID);
				statisticElementList = creatStatisticElementList();
				// Form Structure===
				FormStructure formStructure = createFormStructure(formOID, formName, mType, elementList);
				
				elementList.addAll(statisticElementList);
				ByteArrayOutputStream batteryBaos = dictionaryManager.exportPromisZippedFsDe(formStructure, elementList);				
				fileName = "PROMIS_dataElementDetailExport.zip";
				contentType = ServiceConstants.APPLICATION_ZIP_FILE;
				inputStream = new ByteArrayInputStream(batteryBaos.toByteArray());

			} else { // form in form list
				String apiUrl = hmProperties.getProperty("healthMeasurement.api.url");
		
				url = new URL(apiUrl+formOID+".json");
				connection = (HttpsURLConnection) url.openConnection();
				connection.setDoOutput(true);
				connection.setRequestMethod("POST");
				connection.addRequestProperty("Authorization", "Basic "+asciiEncoded);
				connection.addRequestProperty("Content-Length", "0");
				writer = new DataOutputStream(connection.getOutputStream());
				writer.write(postData);
				BufferedReader br = new BufferedReader(new InputStreamReader((connection.getInputStream())));
				while ((output = br.readLine()) != null) {
					formObj = new JSONObject(output);
				}	
				
				// Data Elements====
				JSONArray items = formObj.getJSONArray("Items");
				elementList = createElementList(items, formOID);
				statisticElementList = creatStatisticElementList();			
				// Form Structure===
				FormStructure formStructure = createFormStructure(formOID, formName, mType, elementList);
				
				elementList.addAll(statisticElementList);
				baos = dictionaryManager.exportPromisZippedFsDe(formStructure, elementList);
				fileName = "PROMIS_dataElementDetailExport.zip";
				contentType = ServiceConstants.APPLICATION_ZIP_FILE;
				inputStream = new ByteArrayInputStream(baos.toByteArray());
			}
			
		} catch (IOException | DateParseException e) {
			e.printStackTrace();
		}
			
		return PortalConstants.ACTION_DOWNLOAD;
	}
		
 	public List<DataElement> createElementList(JSONArray items, String formOID) {
		List<DataElement> elementList = new ArrayList<DataElement>();
		for(int i = 0; i<items.length(); i++) {
			JSONObject item = items.getJSONObject(i);	
			if(item.getJSONArray("Elements").length() >= 2) {
				elementList.add(itemToDe(item));
			}
		}
		return elementList;
	}
 	
 	public List<DataElement> creatStatisticElementList(){
 		List<DataElement> elementList = new ArrayList<DataElement>();
 		DataElement catTScore = dictionaryManager.getLatestDataElementByName(ServiceConstants.CAT_T_SCORE);
		DataElement catSE = dictionaryManager.getLatestDataElementByName(ServiceConstants.CAT_SE);
		DataElement catFianlTScore = dictionaryManager.getLatestDataElementByName(ServiceConstants.CAT_FINAL_T_SCORE);
		DataElement catFinalSE = dictionaryManager.getLatestDataElementByName(ServiceConstants.CAT_FINAL_SE);
		DataElement catPosition = dictionaryManager.getLatestDataElementByName(ServiceConstants.POSITION);
		if(catTScore == null) {
			elementList.add(createFreeTextDE(ServiceConstants.CAT_T_SCORE));
		}			
		if(catSE == null) {
			elementList.add(createFreeTextDE(ServiceConstants.CAT_SE));
		}
		if(catFianlTScore == null) {
			elementList.add(createFreeTextDE(ServiceConstants.CAT_FINAL_T_SCORE));

		}
		if(catFinalSE == null) {
			elementList.add(createFreeTextDE(ServiceConstants.CAT_FINAL_SE));
		}
		if(catPosition == null) {
			elementList.add(createFreeTextDE(ServiceConstants.POSITION));

		}
		return elementList;
 	}
	
	public DataElement itemToDe(JSONObject item) {
		DataElement de = new DataElement();
		JSONArray elements = item.getJSONArray("Elements");
		String qText = createQuestionText(elements);
		//General Details
		Category category = new Category();
		category.setId((long) 0);
		category.setName("Unique Data Element");
		category.setShortName("UDE");
		de.setCategory(category);
		de.setClassificationElementList(createClassificationElementSet());
		de.setStatus(DataElementStatus.DRAFT);
		de.setShortDescription(qText);
		if(qText.length() > 250) {
			de.setTitle(qText.substring(0, 249));
		}else {
			de.setTitle(qText);
		}		
		de.setName(createDataElementName(item));
		de.setCatOid(item.getString("ID"));
		de.setFormItemId(item.getString("FormItemOID"));
		de.setDescription(qText);
		//Data Restrictions
		de.setRestrictions(InputRestrictions.SINGLE);
		de.setType(DataType.NUMERIC);		
		de.setValueRangeList(createPermissibleValues(elements));
		//Guidelines
		Population p = new Population();
		p.setId((long) 2);p.setName("Adult and Pediatric");
		de.setPopulation(p);		
		de.setSuggestedQuestion(qText);
		//Category Groups and Classification
		de.setSubDomainElementList(createSubDomainElement());
		//
		de.setVersion("1");
		de.setSubmittingOrgName("CIT");
		de.setStewardOrgName("CIT");
		
		return de;
	}
	
	public String createDataElementName(JSONObject item) {
		String itemOID = item.getString("FormItemOID");
		if(itemOID != null) {
			if(itemOID.length() == 36) {
				itemOID = itemOID.substring(0, itemOID.indexOf("-"));				
			}
		}
		String name = PortalConstants.PROMIS_OID_PREFIX +item.getString("ID").replaceAll("[^a-zA-Z0-9]+","")+"_"+itemOID;
		if(name.length() > 30) {
			name = name.substring(0, 30);
		}
		return  name;		
	}
	
	public DataElement createFreeTextDE(String type) {
		DataElement de = new DataElement();
		//General Details
		switch(type) {
		case ServiceConstants.CAT_T_SCORE:
			de.setName(ServiceConstants.CAT_T_SCORE);
			de.setSuggestedQuestion("T-score");
			de.setShortDescription("T-score for adaptive form");
			de.setTitle("T-score for adaptive form");
			de.setDescription("T-score for adaptive form");
			de.setCatOid(ServiceConstants.CAT_T_SCORE);
			break;
		case ServiceConstants.CAT_SE:
			de.setName(ServiceConstants.CAT_SE);
			de.setSuggestedQuestion("Standard Error");
			de.setShortDescription("Standard Error for adaptive form");
			de.setTitle("Standard Error for adaptive form");
			de.setDescription("Standard Error for adaptive form");
			de.setCatOid(ServiceConstants.CAT_SE);
			break;
		case ServiceConstants.CAT_FINAL_T_SCORE:
			de.setName(ServiceConstants.CAT_FINAL_T_SCORE);
			de.setSuggestedQuestion("Total T-score");
			de.setShortDescription("Total T-score for adaptive form");
			de.setTitle("Total T-score for adaptive form");
			de.setDescription("Total T-score for adaptive form");
			de.setCatOid(ServiceConstants.CAT_FINAL_T_SCORE);
			break;
		case ServiceConstants.CAT_FINAL_SE:
			de.setName(ServiceConstants.CAT_FINAL_SE);
			de.setSuggestedQuestion("Total Standard Error");
			de.setShortDescription("Total Standard Error for adaptive form");
			de.setTitle("Total Standard Error for adaptive form");
			de.setDescription("Total Standard Error for adaptive form");
			de.setCatOid(ServiceConstants.CAT_FINAL_SE);
			break;
		case ServiceConstants.POSITION:
			de.setName(ServiceConstants.POSITION);
			de.setSuggestedQuestion("QuestionPosition");
			de.setShortDescription("QuestionPosition for adaptive form");
			de.setTitle("QuestionPosition for adaptive form");
			de.setDescription("QuestionPosition for adaptive form");
			de.setCatOid(ServiceConstants.POSITION);
			break;
		}				
		Category category = new Category();
		category.setId((long) 0);
		category.setName("Unique Data Element");
		category.setShortName("UDE");
		de.setCategory(category);
		de.setClassificationElementList(createClassificationElementSet());
		de.setStatus(DataElementStatus.DRAFT);		
		//Data Restrictions
		de.setRestrictions(InputRestrictions.FREE_FORM);
		de.setType(DataType.NUMERIC);
		//Guidelines
		Population p = new Population();
		p.setId((long) 2);p.setName("Adult and Pediatric");
		de.setPopulation(p);		
		//Category Groups and Classification
		de.setSubDomainElementList(createSubDomainElement());
		//
		de.setVersion("1");
		de.setSubmittingOrgName("CIT");
		de.setStewardOrgName("HealthMeasures");
		
		return de;
	}
	
	public Set<ClassificationElement> createClassificationElementSet(){
		Set<ClassificationElement> classificationElementSet = new LinkedHashSet<ClassificationElement>();
		ClassificationElement ce;
		Disease di;
		Subgroup sg;
		Classification c = new Classification();
		c.setCanCreate(true);c.setIsActive(true);c.setName("Supplemental");
		//
		ce = new ClassificationElement();
		di = new Disease();
		di.setName("General (For all diseases)");di.setIsActive(true);
		sg = new Subgroup();
		sg.setName("General (For all diseases)");
		ce.setClassification(c);
		ce.setDisease(di);
		ce.setSubgroup(sg);
		classificationElementSet.add(ce);
		
		return classificationElementSet;
	}
	
	public String createQuestionText(JSONArray elements) {
		String qText = "";
		for(int i=0; i<elements.length();i++) {
			JSONObject element = (JSONObject) elements.get(i);
			if(!element.has("Map") || !element.get("Description").toString().contains("ContainerFor")) {
				qText += element.get("Description").toString();
			}
		}
		int inx = qText.indexOf("Container:");
		if(inx > 0) {
			qText = qText.substring(0,inx);
		}
		return qText.replace("\n", "").replace("\r", "").replace(",", " ");
	}
	
	public Set<ValueRange> createPermissibleValues(JSONArray elements){		
		Set<ValueRange> vrSet = new LinkedHashSet<ValueRange>();
		for(int i=0; i<elements.length();i++) {
			JSONObject element = (JSONObject) elements.get(i);
			if(element.has("Map")) {
				JSONArray map = element.getJSONArray("Map");
				for(int j=0;j<map.length();j++) {
					ValueRange vr = new ValueRange();
					JSONObject option = map.getJSONObject(j);
					vr.setDescription(option.getString("Description"));
					String value = option.getString("Value");
					if(value.equals("00000000-0000-0000-0000-000000000000")) {
						value = "0";
					}
					try {
						vr.setOutputCode(Integer.valueOf(value));						
					}catch(NumberFormatException e) {
						vr.setOutputCode(Integer.valueOf(i));
					}
					vr.setValueRange(value);					
					vr.setItemResponseOid(option.getString("ItemResponseOID"));
					vr.setElementOid(option.getString("ElementOID"));
					vrSet.add(vr);
				}
			}
		}
		return vrSet;
	}

	public HashSet<SubDomainElement> createSubDomainElement(){
		HashSet<SubDomainElement> sdeSet = new HashSet<SubDomainElement>();
		SubDomainElement sde = new SubDomainElement();
		Disease di = new Disease();
		di.setId((long) 1);
		di.setName("General (For all diseases)");
		di.setIsActive(true);
		di.setIsMajor(true);
		sde.setDisease(di);
		
		Domain dom = new Domain();
		dom.setId((long) 1);
		dom.setName("Assessments and Examinations");
		dom.setIsActive(true);
		sde.setDomain(dom);
		
		SubDomain sdom = new SubDomain();
		sdom.setId((long) 27);
		sdom.setName("Non-Imaging Diagnostics");
		sdom.setIsActive(true);
		sde.setSubDomain(sdom);
		
		sdeSet.add(sde);
		
		return sdeSet;
	}

	public FormStructure createFormStructure(String formOID, String formName, String mType, List<DataElement> elementList) throws IOException {
		FormStructure dataStructure = new FormStructure();		
		Properties p = new Properties();
		ServletContext aContext = getSession().getServletContext();
		InputStream fis = aContext.getResourceAsStream("measurementType.properties");
		p.load(fis);
		String formShortName = "P"+formOID.substring(0, 7);
		if(mType == null) {
			dataStructure.setMeasurementType(ServiceConstants.SHORT_FORM);
			dataStructure.setIsCat(false);
			mType = "";
		}else {
			if(mType.equalsIgnoreCase(ServiceConstants.ADAPTIVE_FULL)) {
				dataStructure.setMeasurementType(ServiceConstants.ADAPTIVE);	
				dataStructure.setIsCat(true);
				mType = ServiceConstants.ADAPTIVE;
				formShortName += "_CAT";
			} else if(mType.equalsIgnoreCase(ServiceConstants.AUTO_SCORING_FULL)) {
				dataStructure.setMeasurementType(ServiceConstants.AUTO_SCORING);	
				dataStructure.setIsCat(true);
				mType = ServiceConstants.AUTO_SCORING;
				formShortName += "_AutoScoring";
			}else { // maybe it's a short form
				dataStructure.setMeasurementType(ServiceConstants.SHORT_FORM);
				dataStructure.setIsCat(true );
				mType = ServiceConstants.SHORT_FORM;
				formShortName += "_ShortForm";
			}
		}		
		dataStructure.setTitle(formName);
		dataStructure.setShortName(formShortName);
		dataStructure.setDescription("Description of Form Structure");
		dataStructure.setDiseaseList(createDiseaseList());
		dataStructure.setOrganization("FITBIR");
		dataStructure.setFileType(SubmissionType.CLINICAL);
		dataStructure.setStandardization(FormStructureStandardization.UNIQUE);
		dataStructure.setIsCopyrighted(true);
		dataStructure.setVersion("1.0");
		dataStructure.setStatus(StatusType.DRAFT);
		dataStructure.setCatOid(formOID);
		dataStructure.setRepeatableGroups(createRepeatableGroupSet(elementList, mType));
		
		return dataStructure;
	}
	
	public Set<DiseaseStructure> createDiseaseList(){
		Set<DiseaseStructure> dsset = new HashSet<DiseaseStructure>();
		DiseaseStructure ds = new DiseaseStructure();
		Disease d = new Disease();
		d.setIsActive(true);
		d.setIsMajor(true);
		d.setName("General (For all diseases)");
		ds.setDisease(d);
		dsset.add(ds);
		return dsset;
	}
	
	public Set<RepeatableGroup> createRepeatableGroupSet(List<DataElement> elementList, String mType){
		Set<RepeatableGroup> repeatableGroupSet = new LinkedHashSet<RepeatableGroup>();
		// main
		RepeatableGroup mainGroup = new RepeatableGroup();
		mainGroup.setName("Main");
		mainGroup.setType(RepeatableType.EXACTLY);
		mainGroup.setThreshold(1);
		mainGroup.setPosition(0);
		mainGroup.setMapElements(createMainMapElementSet());
		repeatableGroupSet.add(mainGroup);
		// final results (not for short form)
		if(!mType.equals(ServiceConstants.SHORT_FORM)) {
			RepeatableGroup frGroup = new RepeatableGroup();
			frGroup.setName("Final Results");
			frGroup.setType(RepeatableType.LESSTHAN);
			frGroup.setThreshold(1);
			frGroup.setPosition(1);
			frGroup.setMapElements(createFinalResultMapElementSet());
			repeatableGroupSet.add(frGroup);
		}
		// PROMIS questions groups
		for(int i=0; i<elementList.size();i++) {
			DataElement de = elementList.get(i);
			RepeatableGroup group = new RepeatableGroup();
			group.setName("Q: "+de.getCatOid());
			group.setType(RepeatableType.LESSTHAN);
			group.setThreshold(1);
			group.setPosition(i+2);
			group.setMapElements(createMapElementSet(de, mType));
			repeatableGroupSet.add(group);
		}
		return repeatableGroupSet;
	}
	
	public LinkedHashSet<MapElement> createMainMapElementSet(){
		LinkedHashSet<MapElement> mes = new LinkedHashSet<MapElement>();
		MapElement me = new MapElement();
		StructuralDataElement sde = new StructuralDataElement();
		//--
		sde.setName("GUID");
		me.setStructuralDataElement(sde);
		me.setPosition(1);
		mes.add(me);
		//--
		sde = new StructuralDataElement();
		sde.setName("SubjectIDNum");
		me = new MapElement();
		me.setPosition(2);
		me.setStructuralDataElement(sde);
		mes.add(me);
		//--
		sde = new StructuralDataElement();
		sde.setName("AgeYrs");
		me = new MapElement();
		me.setPosition(3);
		me.setStructuralDataElement(sde);
		mes.add(me);
		//--
		sde = new StructuralDataElement();
		sde.setName("VitStatus");
		me = new MapElement();
		me.setPosition(4);
		me.setStructuralDataElement(sde);
		mes.add(me);
		//--
		sde = new StructuralDataElement();
		sde.setName("VisitDate");
		me = new MapElement();
		me.setPosition(5);
		me.setStructuralDataElement(sde);
		mes.add(me);
		//--
		sde = new StructuralDataElement();
		sde.setName("SiteName");
		me = new MapElement();
		me.setPosition(6);
		me.setStructuralDataElement(sde);
		mes.add(me);
		//--
		sde = new StructuralDataElement();
		sde.setName("DaysSinceBaseline");
		me = new MapElement();
		me.setPosition(7);
		me.setStructuralDataElement(sde);
		mes.add(me);
		//--
		sde = new StructuralDataElement();
		sde.setName("CaseContrlInd");
		me = new MapElement();
		me.setPosition(8);
		me.setStructuralDataElement(sde);
		mes.add(me);
		//--
		sde = new StructuralDataElement();
		sde.setName("GeneralNotesTxt");
		me = new MapElement();
		me.setPosition(9);
		me.setStructuralDataElement(sde);
		mes.add(me);
		//--
		return mes;
	}
	
	public LinkedHashSet<MapElement> createFinalResultMapElementSet(){
		LinkedHashSet<MapElement> mes = new LinkedHashSet<MapElement>();
		MapElement me;
		StructuralDataElement sde;
		List<DataElement> elementList = new ArrayList<DataElement>();
		elementList.add(createFreeTextDE(ServiceConstants.CAT_FINAL_T_SCORE));
		elementList.add(createFreeTextDE(ServiceConstants.CAT_FINAL_SE));		
		for(int i=0; i<elementList.size();i++) {
			sde = new StructuralDataElement();
			me = new MapElement();
			DataElement de = elementList.get(i);
			sde.setName(de.getName());
			me.setStructuralDataElement(sde);
			me.setPosition(i+1);
			mes.add(me);
		}		
		return mes;
	}

	public LinkedHashSet<MapElement> createMapElementSet(DataElement de, String mType){
		LinkedHashSet<MapElement> mes = new LinkedHashSet<MapElement>();		
		MapElement me = new MapElement();
		StructuralDataElement sde = new StructuralDataElement();
		sde.setName(de.getName());		
		me.setStructuralDataElement(sde);
		me.setPosition(1);
		mes.add(me);
		if(mType.equals(ServiceConstants.ADAPTIVE)) {
			me = new MapElement();
			sde = new StructuralDataElement();
			sde.setName(ServiceConstants.CAT_T_SCORE);		
			me.setStructuralDataElement(sde);
			me.setPosition(2);
			mes.add(me);
			
			me = new MapElement();
			sde = new StructuralDataElement();
			sde.setName(ServiceConstants.CAT_SE);		
			me.setStructuralDataElement(sde);
			me.setPosition(3);
			mes.add(me);
			
			me = new MapElement();
			sde = new StructuralDataElement();
			sde.setName(ServiceConstants.POSITION);		
			me.setStructuralDataElement(sde);
			me.setPosition(4);
			mes.add(me);
		}
		return mes;
	}
	
}
