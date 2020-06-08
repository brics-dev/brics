package gov.nih.tbi.repository.model;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.regex.PatternSyntaxException;

import javax.ws.rs.WebApplicationException;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;

import gov.nih.tbi.ModulesConstants;
import gov.nih.tbi.common.util.ProformsWsProvider;
import gov.nih.tbi.commons.model.BRICSTimeDateUtil;
import gov.nih.tbi.commons.service.QueryToolManager;
import gov.nih.tbi.query.model.SummaryResult;
import gov.nih.tbi.query.model.hibernate.SummaryQuery;
import gov.nih.tbi.query.service.QueryToolManagerImpl;
import gov.nih.tbi.repository.model.hibernate.Study;

@Service
public class SummaryDataCache {
	private static final Logger log = Logger.getLogger(SummaryDataCache.class);
	private static SummaryDataCache instance = null;
	private Date updateDate;
	private LinkedHashMap<String, JsonArray> programChartData;
	private LinkedHashMap<String, LinkedHashMap<String, JsonArray>> studyChartData;

	private QueryToolManager queryToolManager;
	
	@Autowired
	private  ProformsWsProvider proformsWsProvider;
	
	@Autowired
	private ModulesConstants modulesConstants;

	/**
	 * Instantiate the class. Protected to set up the singleton nature of this cache. NOTE: singletons here are
	 * system-scoped not thread-scoped so the cache is maintained for ALL users by design.
	 * 
	 * @param queryToolManager connection to the webservice hosting the queries
	 * @param proformsWsProvider connection to the webservice hosting the queries
	 * @param modulesConstants connection to the modulesConstants
	 */
	
	protected SummaryDataCache(QueryToolManager queryToolManager, ProformsWsProvider proformsWsProvider, ModulesConstants modulesConstants) {
		this.queryToolManager = queryToolManager;
		this.proformsWsProvider = proformsWsProvider;
		this.modulesConstants = modulesConstants;
		// <chartName, chart_data>
		programChartData = new LinkedHashMap<String, JsonArray>();
		// <StudyTitle, <chartName, chart_data>>
		studyChartData = new LinkedHashMap<String, LinkedHashMap<String, JsonArray>>();
	}

	/**
	 * Get the instance of the cache from the system.
	 * 
	 * @param queryToolManager connection to the webservice hosting the queries
	 * @return instance of SummaryDataCache that can be used to get summary data
	 */
	public static SummaryDataCache getInstance(QueryToolManager queryToolManager, ProformsWsProvider proformsWsProvider, ModulesConstants modulesConstants) {
		if (instance == null) {
			instance = new SummaryDataCache(queryToolManager, proformsWsProvider, modulesConstants);
		}
		return instance;
	}

	/**
	 * Reset the cache.
	 * @throws IOException 
	 */
	public void resetCache(QueryToolManager queryToolManager, ProformsWsProvider proformsWsProvider, ModulesConstants modulesConstants) throws IOException {
		updateDate = null;
		programChartData.clear();
		studyChartData.clear();
		
		// rebuild the cache
		cacheProgram();
		cacheAllStudies();
	}
	
	
	/**
	 * Empty the cache.
	 * @throws IOException 
	 */
	public void emptyCache(QueryToolManager queryToolManager, ProformsWsProvider proformsWsProvider, ModulesConstants modulesConstants) throws IOException {
		updateDate = null;
		programChartData.clear();
		studyChartData.clear();
	}
	
	/**
	 * Initialize data for a single chart shortName.
	 * 
	 * @param chartName the chart (name) to load
	 */
	protected void initProgramData(String chartName) 
			throws JsonParseException, WebApplicationException, IOException{
		List<String> chartNameList = new ArrayList<String>();
		chartNameList.add(chartName);
		initProgramData(chartNameList);
	}

	/**
	 * Initialize the program data hashmap for the given list of chart names. If the chart entries already exist in
	 * programChartData, they are NOT overwritten and instead just returned.
	 * 
	 * @param chartNames list of chart names for which to load data
	 * @throws IOException 
	 */
	protected void initProgramData(List<String> chartNames) 
			throws JsonParseException, WebApplicationException, IOException{

		String proformsWsUrl = modulesConstants.getModulesPFURL();
		if (programChartData.isEmpty()) {
			updateDate = new Date();
		}

		// @formatter:off
		/*
		 * Data should look like:
		 * programData = {
		 * 		dateUpdated: "date",
		 * 		data : {
		 * 			<chartName> : [
		 * 				{"<legend text>", <value>},
		 * 				... (optionally more entries for this one chart
		 * 			],
		 * 			... (optionally more charts)
		 * 		}
		 * }
		 * 
		 */
		// @formatter:on
		for (String chartName : chartNames) {
			log.info("Cache program chart " + chartName + "..." + " : " + BRICSTimeDateUtil.getCurrentReadableTimeString());
			// only retrieve and store data if we do not have that chart entry already
			// chartData will be cleared daily (at time of writing) by the scheduled job
			if (!programChartData.containsKey(chartName)) {
				SummaryQuery query = queryToolManager.getSummaryByName(chartName);
				SummaryResult singleChartSummaryResult = null;
				if (query.getRequiresProforms()) {
					//if(query.getRequiresSparql()) {
					singleChartSummaryResult = proformsWsProvider.getSummaryData(proformsWsUrl, chartName);
					//} 
				} else {
					singleChartSummaryResult = queryToolManager.getSummaryData(chartName, query);
				}
				// null check in case we gave it a name that doesn't exist or some sort of error in query
				if (singleChartSummaryResult != null) {
					
					
					
					 HashMap<String, String> singleChartData = (LinkedHashMap<String, String>) singleChartSummaryResult.getResults();
					

					JsonArray singleChartJsonArray = new JsonArray();
					
					
					if((query.getRequiresSparql())) {
						if(singleChartData.isEmpty() && !singleChartSummaryResult.getJsonResults().isEmpty()) {
							JsonParser parser = new JsonParser();
					       JsonElement Element = parser.parse(singleChartSummaryResult.getJsonResults());
					       singleChartJsonArray = Element.getAsJsonArray();
						
						} 
					} else if((query.getNeedsObjectMapping())) {
						Multimap<String, String> singleChartDataMultiMap = ArrayListMultimap.create();
						singleChartDataMultiMap =
							singleChartSummaryResult.getResultsMultiMap();
						// loop over all entries for this table
						for (Entry<String, String> entry : singleChartDataMultiMap.entries()) {
							try {
								JsonObject chartEntry = new JsonObject();
								chartEntry.addProperty(getValueFromTypedSemantic(entry.getKey()),
										getValueFromTypedSemantic(entry.getValue()));
								singleChartJsonArray.add(chartEntry);
							} catch (RuntimeException e) {
								log.error("Runtime Exception: dropping chart");
								e.printStackTrace();
								// no need to bother this because it'll just drop the one entry
							}
						}
						
					} else {
						// loop over all entries for this table
						for (Entry<String, String> entry : singleChartData.entrySet()) {
							try {
								JsonObject chartEntry = new JsonObject();
								chartEntry.addProperty(getValueFromTypedSemantic(entry.getKey()),
										getValueFromTypedSemantic(entry.getValue()));
								singleChartJsonArray.add(chartEntry);
							} catch (RuntimeException e) {
								log.error("Runtime Exception: dropping chart");
								e.printStackTrace();
								// no need to bother this because it'll just drop the one entry
							}
						}						
					}


					programChartData.put(chartName, singleChartJsonArray);
				}
				
			}
			else {
				System.out.print("already cached...");
			}
			System.out.println("done" + " : " + BRICSTimeDateUtil.getCurrentReadableTimeString());
		}
	}

	/**
	 * Get JsonObject data for multiple charts for a single program
	 * 
	 * @param chartNames a list of chart names to retrieve
	 * @return JsonObject representation of the program data
	 * @throws IOException 
	 * @throws WebApplicationException 
	 * @throws JsonParseException 
	 */
	public JsonObject getProgramMultiple(List<String> chartNames) throws JsonParseException, WebApplicationException, IOException {
		
		initProgramData(chartNames);
	
		JsonObject dataJson = new JsonObject();

		for (String chartName : chartNames) {
			if (programChartData.containsKey(chartName)) {
				dataJson.add(chartName, programChartData.get(chartName));
			}
		}

		return dataJson;
	}
	
	/**
	 * Initialize the program data hashmap for the given list of chart names. If the chart entries already exist in
	 * programChartData, they are NOT overwritten and instead just returned.
	 * 
	 * @param chartNames list of chart names for which to load data
	 */
	protected void initSparqlProgramData(List<String> chartNames) {
		if (programChartData.isEmpty()) {
			updateDate = new Date();
		}

		// @formatter:off
		/*
		 * Data should look like:
		 * programData = {
		 * 		dateUpdated: "date",
		 * 		data : {
		 * 			<chartName> : [
		 * 				{"<legend text>", <value>},
		 * 				... (optionally more entries for this one chart
		 * 			],
		 * 			... (optionally more charts)
		 * 		}
		 * }
		 * 
		 */
		// @formatter:on
		for (String chartName : chartNames) {
			System.out.print("Cache program chart " + chartName + "...");
			// only retrieve and store data if we do not have that chart entry already
			// chartData will be cleared daily (at time of writing) by the scheduled job
			if (!programChartData.containsKey(chartName)) {
				SummaryResult singleChartSummaryResult = queryToolManager.getSparqlSummaryData(chartName);
				// null check in case we gave it a name that doesn't exist or some sort of error in query
				if (singleChartSummaryResult != null) {
					LinkedHashMap<String, String> singleChartData =
							(LinkedHashMap<String, String>) singleChartSummaryResult.getResults();
					JsonArray singleChartJsonArray = new JsonArray();
					if(singleChartData.isEmpty() && !singleChartSummaryResult.getJsonResults().isEmpty()) {
							JsonParser parser = new JsonParser();
					       JsonElement Element = parser.parse(singleChartSummaryResult.getJsonResults());
					       singleChartJsonArray = Element.getAsJsonArray();
						
					} else {
					

						// loop over all entries for this table
						for (Entry<String, String> entry : singleChartData.entrySet()) {
							try {
								JsonObject chartEntry = new JsonObject();
								chartEntry.addProperty(getValueFromTypedSemantic(entry.getKey()),
										getValueFromTypedSemantic(entry.getValue()));
								singleChartJsonArray.add(chartEntry);
							} catch (RuntimeException e) {
								System.out.println("Runtime Exception: dropping chart");
								e.printStackTrace();
								// no need to bother this because it'll just drop the one entry
							}
						}
					}
					programChartData.put(chartName, singleChartJsonArray);
				}
			}
			else {
				System.out.print("already cached...");
			}
			System.out.println("done");
		}
	}
	
	
	/**
	 * Get Sparql JsonObject data for multiple charts for a single program
	 * 
	 * @param chartNames a list of chart names to retrieve
	 * @return JsonObject representation of the program data
	 */
	public JsonObject getSparqlProgramMultiple(List<String> chartNames) {
		initSparqlProgramData(chartNames);

		JsonObject dataJson = new JsonObject();

		for (String chartName : chartNames) {
			if (programChartData.containsKey(chartName)) {
				dataJson.add(chartName, programChartData.get(chartName));
			}
		}

		return dataJson;
	}

	/**
	 * Get JsonObject data for a single chart for a single program
	 * 
	 * @param chartName a chart name to retrieve
	 * @return JsonObject representation of the program data
	 * @throws IOException 
	 * @throws WebApplicationException 
	 * @throws JsonParseException 
	 */
	public JsonObject getProgramSingle(String chartName) throws JsonParseException, WebApplicationException, IOException {
		
		initProgramData(chartName);
		JsonObject output = new JsonObject();

		// we check here because chartData could be empty
		if (!programChartData.isEmpty()) {
			if (programChartData.containsKey(chartName)) {
				output.add(chartName, programChartData.get(chartName));
			}
		}
		return output;
	}

	/**
	 * Initialize data for a single chart shortName with a single study name. NOTE: this will only ever be used one
	 * study at a time so this should work perfectly
	 * 
	 * @param chartName the chart (name) to load
	 */
	protected void initStudyData(String chartName, String studyName) {
		List<String> chartNameList = new ArrayList<String>();
		chartNameList.add(chartName);
		Study basicStudy = queryToolManager.getBasicStudyByTitle(studyName);
		if (basicStudy == null) {
			basicStudy = new Study();
			basicStudy.setTitle(studyName);
		}
		initStudyData(chartNameList, basicStudy);
	}

	/**
	 * Initialize data for a single chart shortName with a single study id. NOTE: this will only ever be used one study
	 * at a time so this should work perfectly
	 * 
	 * @param chartName the chart (name) to load
	 */
	protected void initStudyData(String chartName, Long studyId) {
		List<String> chartNameList = new ArrayList<String>();
		chartNameList.add(chartName);
		Study basicStudy = queryToolManager.getBasicStudyById(studyId);
		if (basicStudy == null) {
			basicStudy = new Study();
			basicStudy.setId(studyId);
		}
		initStudyData(chartNameList, basicStudy);
	}

	/**
	 * Initialize the study data hashmap for the given list of chart names. If the chart entries already exist in
	 * studyChartData, they are NOT overwritten and instead just returned.
	 * 
	 * @param chartNames list of chart names for which to load data
	 * @param study the study upon which to process
	 */
	protected void initStudyData(List<String> chartNames, Study study) {
		if (studyChartData.isEmpty()) {
			updateDate = new Date();
		}

		// @formatter:off
		/*
		 * Data should look like:
		 * studyData = {
		 * 		dateUpdated: "date",
		 * 		data : {
		 * 			<chartName> : [
		 * 				{"<legend text>", <value>},
		 * 				... (optionally more entries for this one chart
		 * 			],
		 * 			... (optionally more charts)
		 * 		}
		 * }
		 * 
		 */
		// @formatter:on

		// has singleStudyData been changed? It will be written back into studyChartData if this is true at the end
		LinkedHashMap<String, JsonArray> singleStudyData = new LinkedHashMap<String, JsonArray>();

		String studyId = null;
		if (study.getId() != null) {
			studyId = study.getId().toString();
		}

		String studyTitle = study.getTitle();

		if (studyChartData.containsKey(studyTitle)) {
			singleStudyData = studyChartData.get(studyTitle);
		} else if (studyChartData.containsKey(studyId)) {
			singleStudyData = studyChartData.get(studyId);
		} else {
			// store both title and ID
			if (studyTitle != null) {
				singleStudyData.put(studyTitle, new JsonArray());
			}
			if (studyId != null) {
				singleStudyData.put(studyId, new JsonArray());
			}
		}

		for (String chartName : chartNames) {
			System.out.print("Cache study " + studyTitle + " chart " + chartName + "...");
			// only retrieve and store data if we do not have that chart entry already
			// studyChartData will be cleared daily (at time of writing) by the scheduled job
			if (!singleStudyData.containsKey(chartName)) {
				SummaryQuery query = queryToolManager.getSummaryByName(chartName);
				SummaryResult singleChartSummaryResult = queryToolManager.getSummaryData(chartName, query, null, studyId, null, null);
				if (singleChartSummaryResult != null) {
					LinkedHashMap<String, String> singleChartData =
							(LinkedHashMap<String, String>) singleChartSummaryResult.getResults();

					JsonArray singleChartJsonArray = new JsonArray();

					for (Entry<String, String> entry : singleChartData.entrySet()) {
						try {
							JsonObject chartEntry = new JsonObject();
							chartEntry.addProperty(getValueFromTypedSemantic(entry.getKey()),
									getValueFromTypedSemantic(entry.getValue()));
							singleChartJsonArray.add(chartEntry);
						} catch (RuntimeException e) {
							System.out.println("Runtime Exception: dropping chart");
							e.printStackTrace();
							// no need to bother this because it'll just drop the one entry
						}
					}

					// at this point we have a filled jsonArray for this single chart
					singleStudyData.put(chartName, singleChartJsonArray);
				}
			}
			else {
				System.out.print("already cached...");
			}
			System.out.println("done");
		}

		// write back regardless of any changes - just makes this cleaner
		studyChartData.put(studyTitle, singleStudyData);
		if (studyId != null) {
			studyChartData.put(studyId, singleStudyData);
		}
	}
	
	/**
	 * Initialize the study data hashmap for the given list of chart names. If the chart entries already exist in
	 * studyChartData, they are NOT overwritten and instead just returned.
	 * 
	 * @param chartNames list of chart names for which to load data
	 * @param study the study upon which to process
	 */
	protected void initSparqlStudyData(List<String> chartNames, Study study) {
		if (studyChartData.isEmpty()) {
			updateDate = new Date();
		}

		// @formatter:off
		/*
		 * Data should look like:
		 * studyData = {
		 * 		dateUpdated: "date",
		 * 		data : {
		 * 			<chartName> : [
		 * 				{"<legend text>", <value>},
		 * 				... (optionally more entries for this one chart
		 * 			],
		 * 			... (optionally more charts)
		 * 		}
		 * }
		 * 
		 */
		// @formatter:on

		// has singleStudyData been changed? It will be written back into studyChartData if this is true at the end
		LinkedHashMap<String, JsonArray> singleStudyData = new LinkedHashMap<String, JsonArray>();

		String studyId = null;
		if (study.getId() != null) {
			studyId = study.getId().toString();
		}

		String studyTitle = study.getTitle();

		if (studyChartData.containsKey(studyTitle)) {
			singleStudyData = studyChartData.get(studyTitle);
		} else if (studyChartData.containsKey(studyId)) {
			singleStudyData = studyChartData.get(studyId);
		} else {
			// store both title and ID
			if (studyTitle != null) {
				singleStudyData.put(studyTitle, new JsonArray());
			}
			if (studyId != null) {
				singleStudyData.put(studyId, new JsonArray());
			}
		}

		for (String chartName : chartNames) {
			System.out.print("Cache study " + studyTitle + " chart " + chartName + "...");
			// only retrieve and store data if we do not have that chart entry already
			// studyChartData will be cleared daily (at time of writing) by the scheduled job
			if (!singleStudyData.containsKey(chartName)) {
				SummaryResult singleChartSummaryResult = queryToolManager.getSparqlSummaryData(chartName, null, studyTitle, null, null);
				if (singleChartSummaryResult != null) {
					
					
					LinkedHashMap<String, String> singleChartData =
							(LinkedHashMap<String, String>) singleChartSummaryResult.getResults();

					JsonArray singleChartJsonArray = new JsonArray();
					if(singleChartData.isEmpty() && !singleChartSummaryResult.getJsonResults().isEmpty()) {
						JsonParser parser = new JsonParser();
				       JsonElement Element = parser.parse(singleChartSummaryResult.getJsonResults());
				       singleChartJsonArray = Element.getAsJsonArray();
					
				} else {

					for (Entry<String, String> entry : singleChartData.entrySet()) {
						try {
							JsonObject chartEntry = new JsonObject();
							chartEntry.addProperty(getValueFromTypedSemantic(entry.getKey()),
									getValueFromTypedSemantic(entry.getValue()));
							singleChartJsonArray.add(chartEntry);
						} catch (RuntimeException e) {
							System.out.println("Runtime Exception: dropping chart");
							e.printStackTrace();
							// no need to bother this because it'll just drop the one entry
						}
					}
				}

					// at this point we have a filled jsonArray for this single chart
					singleStudyData.put(chartName, singleChartJsonArray);
				}
			}
			else {
				System.out.print("already cached...");
			}
			System.out.println("done");
		}

		// write back regardless of any changes - just makes this cleaner
		studyChartData.put(studyTitle, singleStudyData);
		if (studyId != null) {
			studyChartData.put(studyId, singleStudyData);
		}
	}

	/**
	 * Get JsonObject data for multiple charts for a single study
	 * 
	 * @param chartNames a list of chart names to retrieve
	 * @return JsonObject representation of the study data
	 */
	public JsonObject getStudyMultiple(List<String> chartNames, Long studyId) {
		Study basicStudy = queryToolManager.getBasicStudyById(studyId);
		if (basicStudy == null) {
			basicStudy = new Study();
			basicStudy.setId(studyId);
		}
		initStudyData(chartNames, basicStudy);

		JsonObject dataJson = new JsonObject();
		LinkedHashMap<String, JsonArray> studyData = studyChartData.get(studyId.toString());
		if (!studyData.isEmpty()) {
			for (String chartName : chartNames) {
				if (studyData.containsKey(chartName)) {
					dataJson.add(chartName, studyData.get(chartName));
				}
			}
		}

		return dataJson;
	}
	
	/**
	 * Get JsonObject data for multiple charts for a single study
	 * 
	 * @param chartNames a list of chart names to retrieve
	 * @return JsonObject representation of the study data
	 */
	public JsonObject getSparqlStudyMultiple(List<String> chartNames, Long studyId) {
		Study basicStudy = queryToolManager.getBasicStudyById(studyId);
		if (basicStudy == null) {
			basicStudy = new Study();
			basicStudy.setId(studyId);
		}
		initSparqlStudyData(chartNames, basicStudy);

		JsonObject dataJson = new JsonObject();
		LinkedHashMap<String, JsonArray> studyData = studyChartData.get(studyId.toString());
		if (!studyData.isEmpty()) {
			for (String chartName : chartNames) {
				if (studyData.containsKey(chartName)) {
					dataJson.add(chartName, studyData.get(chartName));
				}
			}
		}

		return dataJson;
	}
	
	public JsonObject getStudyMultiple(List<String> chartNames, String studyTitle) {
		Study basicStudy = queryToolManager.getBasicStudyByTitle(studyTitle);
		if (basicStudy == null) {
			basicStudy = new Study();
			basicStudy.setTitle(studyTitle);
		}
		initStudyData(chartNames, basicStudy);
		JsonObject dataJson = new JsonObject();
		LinkedHashMap<String, JsonArray> studyData = studyChartData.get(studyTitle);
		if (studyData != null && !studyData.isEmpty()) {
			for (String chartName : chartNames) {
				if (studyData.containsKey(chartName)) {
					dataJson.add(chartName, studyData.get(chartName));
				}
			}
		}

		return dataJson;
	}
	
	public JsonObject getSparqlStudyMultiple(List<String> chartNames, String studyTitle) {
		Study basicStudy = queryToolManager.getBasicStudyByTitle(studyTitle);
		if (basicStudy == null) {
			basicStudy = new Study();
			basicStudy.setTitle(studyTitle);
		}
		initSparqlStudyData(chartNames, basicStudy);
		JsonObject dataJson = new JsonObject();
		LinkedHashMap<String, JsonArray> studyData = studyChartData.get(studyTitle);
		if (studyData != null && !studyData.isEmpty()) {
			for (String chartName : chartNames) {
				if (studyData.containsKey(chartName)) {
					dataJson.add(chartName, studyData.get(chartName));
				}
			}
		}

		return dataJson;
	}

	public void cacheAllStudies() {
		// List<String> studyTitles = queryToolManager.getAllStudyTitles();
		List<Study> studies = queryToolManager.getAllStudies();
		int studyLength = studies.size();
		int studyNumber = 1;
		
		List<String> chartNames = new ArrayList<String>();
		List<String> sparqlChartNames = new ArrayList<String>();
		String orgName = modulesConstants.getModulesOrgName();
		List<SummaryQuery> queries = queryToolManager.getAllStudySummaryQueriesShortnames(orgName);
		for (SummaryQuery query : queries) {
			if (query != null) {
				String shortname = query.getShortname();
				if(query.getRequiresSparql()) {
					sparqlChartNames.add(shortname);
				} else {
					chartNames.add(shortname);
				}
			}
		}
		
		for (Study study : studies) {
			// don't need to actually get the data out but just set it to the cache
			System.out.println("Caching summary data for study: " + study.getTitle() + " (" + studyNumber + " of "
					+ studyLength + ")");
			initStudyData(chartNames, study);
			initSparqlStudyData(sparqlChartNames, study);
			studyNumber++;
		}
	}
	
	public void cacheProgram() {
		List<String> chartNames = new ArrayList<String>();
		List<String> sparqlChartNames = new ArrayList<String>();
		String orgName = modulesConstants.getModulesOrgName();
		List<SummaryQuery> queries = queryToolManager.getAllProgramSummaryQueriesShortnames(orgName);
		for (SummaryQuery query : queries) {
			if (query != null) {
				String shortname = query.getShortname();
				if(query.getRequiresSparql()) {
					sparqlChartNames.add(shortname);
				} else {
					chartNames.add(shortname);
				}
			}
		}
		
		
		// don't need to actually get the data out but just set it to the cache
		System.out.println("Caching summary data for program.  " + chartNames.size() + " charts found");
		try {
			initProgramData(chartNames);
		} catch (JsonParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (WebApplicationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		initSparqlProgramData(sparqlChartNames);
	}

	/**
	 * Get JsonObject data for a single chart for a single study
	 * 
	 * @param chartName a chart name to retrieve
	 * @return JsonObject representation of the study data
	 */
	public JsonObject getStudySingle(String chartName, String studyTitle) {

		initStudyData(chartName, studyTitle);

		JsonObject dataJson = new JsonObject();

		LinkedHashMap<String, JsonArray> studyData = studyChartData.get(studyTitle);
		// we check here because studyChartData could be empty
		if (!studyChartData.isEmpty()) {
			if (studyData.containsKey(chartName)) {
				dataJson.add(chartName, studyData.get(chartName));
			}
		}
		return dataJson;
	}

	/**
	 * Get the "last updated date" for entry into the JSON
	 * 
	 * @return Date last updated date
	 */
	public Date getUpdateDate() {
		return updateDate;
	}

	/**
	 * Translates a value from the typed semantic (of the form 2090^^http:\/\/www.w3.org\/2001\/XMLSchema#integer), into
	 * JUST the integer value in string format.
	 * 
	 * @param value the typed string
	 * @return String representation of the integer
	 */
	protected String getValueFromTypedSemantic(String value) throws PatternSyntaxException {
		if (value != null) {
			String[] parts = value.split("\\^\\^");
			if (parts.length > 1) {
				return parts[0];
			}
		}
		// in the case of length < 2 or == null
		return value;
	}
}
