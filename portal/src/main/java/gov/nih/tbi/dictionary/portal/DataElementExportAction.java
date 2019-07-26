package gov.nih.tbi.dictionary.portal;

import gov.nih.tbi.PortalConstants;
import gov.nih.tbi.account.model.hibernate.Account;
import gov.nih.tbi.commons.model.DataElementStatus;
import gov.nih.tbi.commons.service.ServiceConstants;
import gov.nih.tbi.commons.service.UserPermissionException;
import gov.nih.tbi.commons.service.util.MarshalStreamer;
import gov.nih.tbi.dictionary.model.DictionarySearchFacets;
import gov.nih.tbi.dictionary.model.FacetType;
import gov.nih.tbi.dictionary.model.hibernate.DataElement;
import gov.nih.tbi.dictionary.model.hibernate.FormStructure;
import gov.nih.tbi.dictionary.model.hibernate.RepeatableGroup;
import gov.nih.tbi.dictionary.service.util.RedCapExportHelper;
import gov.nih.tbi.portal.PortalUtils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.http.impl.cookie.DateParseException;
import org.apache.log4j.Logger;

public class DataElementExportAction extends BaseDataElementSearchAction {

	private static final long serialVersionUID = 1L;
	private static Logger logger = Logger.getLogger(DataElementExportAction.class);

	private InputStream inputStream;
	private String contentType;
	private String fileName;
	private List<DataElement> dataElementsList;


	// This is the preparation method for exporting, it builds a list of DataElement objects based on the
	// search criteria user selected.
	private void buildExportList() throws UserPermissionException {

		boolean onlyOwned = false;
		Account account = null;
		Long diseaseId = -1l;

		if (this.getInPublicNamespace()) {
			logger.debug("This is in public search.");

			// User should not be able to see a draft data element
			if (getSelectedStatuses().contains(DataElementStatus.DRAFT.getName())) {
				throw new UserPermissionException(ServiceConstants.READ_ACCESS_DENIED);
			}

			account = this.getAnonymousAccount();

		} else {
			onlyOwned = (getOwnerId() == 1);
			account = this.getAccount();
			diseaseId = getDiseaseId();
		}

		DictionarySearchFacets facets = buildFacets();

		List<String> searchLocations = parseSelectedOptions(getDataElementLocations());
		Map<FacetType, Set<String>> searchKeywords = buildSearchKeywordsMap(searchLocations);

		String accountUrl = modulesConstants.getModulesAccountURL(diseaseId);

		// The function DictionaryService.semanticDataElementSearch makes 2 calls to the account web
		// service
		String[] twoProxyTickets = PortalUtils.getMultipleProxyTickets(accountUrl, 2);

		dataElementsList = dictionaryService.compositeDataElementSearch(account, facets, searchKeywords,
				Boolean.parseBoolean(exactMatch), onlyOwned, null, twoProxyTickets);
	}


	/**
	 * 
	 * @return String "xml" whichs maps to the xml file downloading result defined in struts configuration.
	 * @throws UserPermissionException
	 */
	public String xmlExport() throws UserPermissionException {

		this.buildExportList();

		ByteArrayOutputStream baos = new ByteArrayOutputStream();

		// use our marshal streamer to avoid running out of heapspace while marshalling data elements to xml
		MarshalStreamer<DataElement> marshalStreamer =
				new MarshalStreamer<DataElement>(baos, DataElement.class, "dataElementsExport");

		try {
			for (DataElement currentDataElement : dataElementsList) {
				marshalStreamer.writeElement(currentDataElement);
			}
		} finally {
			marshalStreamer.close();
		}

		inputStream = new ByteArrayInputStream(baos.toByteArray());
		contentType = ServiceConstants.XML_FILE;
		fileName = "dataElementExport.xml";

		return PortalConstants.ACTION_DOWNLOAD;
	}

	/**
	 * 
	 * @return String "csv" whichs maps to the excel file downloading result defined in struts configuration.
	 * @throws UserPermissionException
	 */
	public String csvExport() throws UserPermissionException {

		this.buildExportList();

		ByteArrayOutputStream baos = null;
		try {
			if (isRedCap()) {
				baos = RedCapExportHelper.exportDEListToRedCapCsv(dataElementsList);
				fileName = "dataElementExport_REDCap.csv";
				contentType = ServiceConstants.CSV_FILE;
			} else {
				baos = dictionaryManager.exportToZippedCsvDetailed(dataElementsList);
				fileName = "dataElementDetailExport.zip";
				contentType = ServiceConstants.APPLICATION_ZIP_FILE;
			}
		} catch (IOException | DateParseException e) {
			e.printStackTrace();
			logger.error("Error occurred while exporting search results");
		}
		inputStream = new ByteArrayInputStream(baos.toByteArray());

		return PortalConstants.ACTION_DOWNLOAD;
	}


	/**
	 * 
	 * @return String "schema" whichs maps to the xsd file downloading result defined in struts configuration.
	 * @throws UserPermissionException
	 */
	public String xsdExport() throws FileNotFoundException {

		URL xsd = DataElementExportAction.class.getResource("/TbiDictionary.xsd");

		inputStream = new FileInputStream(xsd.getFile());
		contentType = ServiceConstants.XSD_FILE;
		fileName = "dictionarySchema.xsd";

		return PortalConstants.ACTION_DOWNLOAD;
	}


	/**
	 * This method will export a detailed report of data elements in the session form structure
	 * 
	 * @return
	 * @throws IOException
	 */
	public String exportFSDataElementsDetail() {

		FormStructure fs = getSessionDataStructure().getDataStructure();

		// default
		contentType = ServiceConstants.CSV_FILE;

		ByteArrayOutputStream baos = null;
		try {
			// REDCap export
			if (isRedCap()) {

				List<String> groupDEStringList = new ArrayList<String>();
				List<List<DataElement>> groupDEDataElementList = new ArrayList<List<DataElement>>();
				List<List<Boolean>> groupDEDataRequiredList = new ArrayList<List<Boolean>>();
				List<Integer> groupDEThresholdList = new ArrayList<Integer>();

				List<RepeatableGroup> repeatableGroups = fs.getSortedRepeatableGroups();
				for (RepeatableGroup rg : repeatableGroups) {
					groupDEStringList.add(rg.getName());
					groupDEDataElementList.add(fs.getSortedDataElementList(rg));
					groupDEDataRequiredList.add(fs.getSortedDataElementRequiredList(rg));
					groupDEThresholdList.add(rg.getThreshold());
				}

				baos = RedCapExportHelper.exportDEDetailsToRedCapCsv(fs.getShortName(), groupDEStringList,
						groupDEDataElementList, groupDEDataRequiredList, groupDEThresholdList);
				fileName = fs.getShortName() + "_dataElementExport_REDCap.csv";

			} else { // normal export
				List<DataElement> dataElements = fs.getSortedDataElementList();

				Set<String> deNames = new HashSet<String>();

				for (DataElement de : dataElements) {
					deNames.add(de.getName());
				}

				List<DataElement> latestDes = new ArrayList<DataElement>();
				if(deNames != null && !deNames.isEmpty()) {
					latestDes = dictionaryManager.getLatestDataElementByNameList(deNames);
				}

				baos = dictionaryManager.exportToZippedCsvDetailed(latestDes);
				fileName = fs.getShortName() + "_dataElementDetailExport.zip";
				contentType = ServiceConstants.APPLICATION_ZIP_FILE;
			}

		} catch (IOException | DateParseException e) {
			e.printStackTrace();
			logger.error("Error occured occured while exporting data element detailed report");
		}

		inputStream = new ByteArrayInputStream(baos.toByteArray());

		return PortalConstants.ACTION_DOWNLOAD;
	}


	private boolean isRedCap() {
		String format = getRequest().getParameter(PortalConstants.FORMAT);
		return (format != null && format.equalsIgnoreCase(PortalConstants.REDCAP));
	}


	public InputStream getInputStream() {
		return inputStream;
	}

	public String getContentType() {
		return contentType;
	}

	public String getFileName() {
		return fileName;
	}

}
