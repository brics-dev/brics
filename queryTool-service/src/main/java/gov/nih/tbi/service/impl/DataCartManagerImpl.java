package gov.nih.tbi.service.impl;

import gov.nih.tbi.constants.ApplicationConstants;
import gov.nih.tbi.dictionary.model.hibernate.Schema;
import gov.nih.tbi.dictionary.model.hibernate.ValueRange;
import gov.nih.tbi.exceptions.DataCartException;
import gov.nih.tbi.pojo.CodeMapping;
import gov.nih.tbi.pojo.DataTableColumnWithUri;
import gov.nih.tbi.pojo.FormResult;
import gov.nih.tbi.pojo.InstancedDataTable;
import gov.nih.tbi.pojo.StudyResult;
import gov.nih.tbi.repository.model.DataTableColumn;
import gov.nih.tbi.service.DataCartManager;
import gov.nih.tbi.service.InstancedDataManager;
import gov.nih.tbi.service.QueryAccountManager;
import gov.nih.tbi.service.cache.InstancedDataCache;
import gov.nih.tbi.service.io.SftpClient;
import gov.nih.tbi.service.io.SftpClientManager;
import gov.nih.tbi.service.model.DataCart;
import gov.nih.tbi.util.DataCartUtil;
import gov.nih.tbi.util.InstancedDataUtil;
import gov.nih.tbi.util.QueryRestProviderUtils;
import gov.nih.tbi.ws.provider.DictionaryWebserviceProvider;
import gov.nih.tbi.ws.provider.RestRepositoryProvider;

import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.google.gson.JsonArray;
import com.jcraft.jsch.JSchException;

@Component
@Scope("application")
public class DataCartManagerImpl implements DataCartManager, Serializable {

	private static final long serialVersionUID = 6378361592930914525L;
	private static final Logger log = Logger.getLogger(DataCartManagerImpl.class);

	@Autowired
	InstancedDataManager instancedDataManager;

	@Autowired
	QueryAccountManager queryAccountManager;

	@Autowired
	ApplicationConstants constants;

	public void addToCart(DataCart dataCart, FormResult formToAdd, StudyResult studyToAdd) {

		if (formToAdd == null || studyToAdd == null) {
			throw new DataCartException("Cannot add to cart without a form or a study.");
		}

		// Add to FormResult map
		// if the form is already in the list, add the study to it
		FormResult formInCart = dataCart.getFormsInCart().get(formToAdd.getUri());

		if (formInCart != null) { // form to be added is already in the cart
			if (!formInCart.getStudies().contains(studyToAdd)) {
				formInCart.getStudies().add(studyToAdd);
			}
		} else { // form to be added is not yet in the cart
			FormResult form = new FormResult(formToAdd);
			form.getStudies().clear();
			form.getStudies().add(studyToAdd);
			dataCart.getFormsInCart().put(formToAdd.getUri(), form);
		}
	}

	public void removeFromCart(DataCart dataCart, FormResult formToRemove, StudyResult studyToRemove) {
		if (formToRemove == null || studyToRemove == null) {
			throw new DataCartException("Cannot remove to cart without a form or a study.");
		}

		// check that the form and study are both in the cart on server end
		FormResult formInCart = dataCart.getFormsInCart().get(formToRemove.getUri());
		if (formInCart != null) {
			// if the study isn't in the list, it just doesn't change anything
			formInCart.getStudies().remove(studyToRemove);
			// if the form no longer has studies, remove it completely
			if (formInCart.getStudies().isEmpty()) {
				dataCart.getFormsInCart().remove(formToRemove.getUri());
			}
		}
	}

	/**
	 * Add formResult objects to formsInCart map, seed and sort the repeatable groups for each of them.
	 */
	public List<FormResult> loadSelectedFormDataElements(DataCart dataCart) {

		List<FormResult> selectedForms = new ArrayList<FormResult>();

		Iterator<String> selectedFormsIter = dataCart.getSelectedFormUris().iterator();
		while (selectedFormsIter.hasNext()) {
			String formUri = selectedFormsIter.next();
			FormResult form = dataCart.getFormFromCart(formUri);

			if (form != null) {
				instancedDataManager.seedFormDataElements(form);
				selectedForms.add(form);
			}
		}

		return selectedForms;
	}

	// Rebuild the permissible value output code and schema value mapping for data
	// elements in all selected forms.
	public void rebuildCodeMapping(DataCart dataCart, List<FormResult> selectedForms) {

		DictionaryWebserviceProvider dictionaryWebserviceProvider = new DictionaryWebserviceProvider(
				constants.getModulesDDTURL(), QueryRestProviderUtils.getProxyTicket(constants.getModulesDDTURL()));

		Map<String, Map<String, ValueRange>> deValueRangeMap = dictionaryWebserviceProvider
				.getDataElementValueRangeMap(selectedForms, constants.getDataElementValueRangeMapURL());
		CodeMapping codeMapping = new CodeMapping(deValueRangeMap);

		dataCart.setCodeMapping(codeMapping);
	}

	public void generateInstancedDataTable(DataCart dataCart, int offset, int limit, String sortColName,
			String sortOrder, String userName, boolean doApplyFilter) {

		if (dataCart.getInstancedDataTable() != null) {
			dataCart.getInstancedDataTable().clear();
		}

		DataTableColumn sortColumn = InstancedDataUtil.getSortColumn(sortColName);

		List<FormResult> selectedForms = dataCart.getSelectedForms();

		if (dataCart.getInstancedDataCache().isEmpty()) {
			InstancedDataCache rowCache = instancedDataManager.buildInstancedRowCache(selectedForms,
					dataCart.getCodeMapping(), InstancedDataUtil.getAccountNode(userName), false);
			dataCart.setInstancedDataCache(rowCache);
		}

		InstancedDataTable instancedDataTable =
				instancedDataManager.buildInstancedDataTable(selectedForms, offset, limit, sortColumn, sortOrder,
						dataCart.getInstancedDataCache(), dataCart.getCodeMapping(), userName, false, doApplyFilter);
		dataCart.setInstancedDataTable(instancedDataTable);
	}

	// This method should be called for pagination or sorting, it assumes the query
	// for InstancedRowCache has been run
	// and the InstancedDataTable in dataCart has the header and rowCount set
	// already, it just reloads the
	// InstancedRecords in the table based on the pagination information passed in.
	public void rebuildDataTableData(DataCart dataCart, int offset, int limit, String sortColName, String sortOrder,
			String userName) {
		InstancedDataTable instancedDataTable = dataCart.getInstancedDataTable();
		if (instancedDataTable != null && instancedDataTable.getInstancedRecords() != null) {
			instancedDataTable.getInstancedRecords().clear();
		}

		DataTableColumn sortColumn = InstancedDataUtil.getSortColumn(sortColName);

		instancedDataTable.setOffset(offset);
		instancedDataTable.setLimit(limit);
		instancedDataTable.setSortColumn(sortColumn);
		instancedDataTable.setSortOrder(sortOrder);

		instancedDataManager.buildDataTableData(dataCart.getSelectedForms(), instancedDataTable,
				dataCart.getInstancedDataCache(), dataCart.getCodeMapping(), InstancedDataUtil.getAccountNode(userName),
				false, false);
		dataCart.setInstancedDataTable(instancedDataTable);
	}

	public String getTableHeaderJson(DataCart dataCart) {

		InstancedDataTable instancedDataTable = dataCart.getInstancedDataTable();

		return DataCartUtil.getColumnHeaderJson(instancedDataTable);
	}

	public String getTableDataJson(DataCart dataCart) {

		InstancedDataTable instancedDataTable = dataCart.getInstancedDataTable();

		return DataCartUtil.getInstancedDataTableJson(instancedDataTable);
	}

	public void expandRepeatableGroup(DataCart dataCart, String rowUri, String rgFormUri, String rgName,
			String userName) {

		FormResult currentForm = dataCart.getFormFromCart(rgFormUri);

		if (currentForm == null) {
			String msg = "The given formUri, " + rgFormUri + ", is invalid!";
			log.error(msg);
			throw new DataCartException(msg);
		}

		instancedDataManager.loadRepeatableGroupRows(dataCart.getInstancedDataTable(), currentForm, rowUri, rgName,
				dataCart.getCodeMapping(), userName);
	}

	public void collapseRepeatableGroup(DataCart dataCart, String rowUri, String rgFormUri, String rgName) {

		FormResult currentForm = dataCart.getFormFromCart(rgFormUri);

		if (currentForm == null) {
			String msg = "The given formUri, " + rgFormUri + ", is invalid!";
			log.error(msg);
			throw new DataCartException(msg);
		}

		instancedDataManager.collapseRepeatableGroupRows(dataCart.getInstancedDataTable(), currentForm, rowUri, rgName);
	}

	public void refreshRepeatableGroup(DataCart dataCart, String rowUri, String rgFormUri, String rgName,
			String userName) {
		log.info("refreshRepeatableGroup");
//		FormResult currentForm = dataCart.getFormFromCart(rgFormUri);
//
//		if (currentForm == null) {
//			String msg = "The given formUri, " + rgFormUri + ", is invalid!";
//			log.error(msg);
//			throw new DataCartException(msg);
//		}
//
//		instancedDataManager.refreshRepeatableGroupRows(dataCart.getInstancedDataTable(),
//				dataCart.getInstancedDataCache(), currentForm, rowUri, rgName, dataCart.getCodeMapping(), userName);
	}

	public String getSelectedFormDetailsJson(DataCart dataCart) {

		JsonArray jsonArray = new JsonArray();

		for (String selectedFormUri : dataCart.getSelectedFormUris()) {
			FormResult selectedForm = dataCart.getFormFromCart(selectedFormUri);

			if (selectedForm != null) {
				jsonArray.add(selectedForm.toJsonDetails());
			}
		}

		return jsonArray.toString();
	}

	public byte[] downloadThumbnailBytes(String studyPrefixedId, String datasetName, String imageName) {

		byte[] fileBytes = null;

		try {
			SftpClient client = SftpClientManager.getClient(constants.getDataDropEndpointInfo());

			RestRepositoryProvider restRepositoryProvider = new RestRepositoryProvider(constants.getModulesSTURL(),
					QueryRestProviderUtils.getProxyTicket(constants.getModulesSTURL()));
			String imagePath = restRepositoryProvider.getDatasetFilePath(constants.getDatasetFilePathURL(),
					studyPrefixedId, datasetName, imageName);

			log.info("QT Image Path: " + imagePath + imageName);
			fileBytes = client.downloadBytes(imageName, imagePath);
		} catch (JSchException e) {
			log.error(e.getMessage(), e);
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
		return fileBytes;
	}

	public boolean hasMatchingGuid(DataCart dataCart) {
		return dataCart.getInstancedDataCache().hasMatchingGuid();
	}

	public boolean hasHighlightedGuid(List<FormResult> selectedForms, String userName) {

		if (selectedForms == null || selectedForms.isEmpty()) {
			return false;
		}

		return instancedDataManager.hasHighlightedGuid(selectedForms.get(0),
				InstancedDataUtil.getAccountNode(userName));
	}

	/**
	 * Calls the web service provider to get a html string for DE Details page.
	 * 
	 * @return html string.
	 */
	public String getDeDetailPage(String deName) {

		String deDetailsHtml = null;

		DictionaryWebserviceProvider dictionaryWebserviceProvider = new DictionaryWebserviceProvider(
				constants.getModulesDDTURL(), QueryRestProviderUtils.getProxyTicket(constants.getModulesDDTURL()));

		try {
			deDetailsHtml = dictionaryWebserviceProvider.getDeDetailsPageHtml(deName);
		} catch (UnsupportedEncodingException e) {
			log.error("DE Details is blank " + e.getMessage());
			e.printStackTrace();
		}

		return deDetailsHtml;
	}

	/**
	 * Calls the web service provider to get a list of Schema objects in the dictionary database.
	 * 
	 * @return a list of schema objects.
	 */
	public List<Schema> getSchemaOptions() {

		List<Schema> schemaList = null;

		DictionaryWebserviceProvider dictionaryWebserviceProvider = new DictionaryWebserviceProvider(
				constants.getModulesDDTURL(), QueryRestProviderUtils.getProxyTicket(constants.getModulesDDTURL()));

		try {
			schemaList = dictionaryWebserviceProvider.getSchemaList(constants.getSchemaListURL());
		} catch (UnsupportedEncodingException e) {
			log.error("Schema options is empty " + e.getMessage());
			e.printStackTrace();
		}

		return schemaList;
	}

	/**
	 * Calls the web service provider to get a list of Schema objects in the dictionary database.
	 * 
	 * @return a list of schema objects.
	 */
	public List<Schema> getSchemaOptionsByFormNames(List<String> formStuctureNames) {

		List<Schema> schemaList = null;

		DictionaryWebserviceProvider dictionaryWebserviceProvider = new DictionaryWebserviceProvider(
				constants.getModulesDDTURL(), QueryRestProviderUtils.getProxyTicket(constants.getModulesDDTURL()));

		try {
			// build the webservice URL and add the form structure names as parameters
			StringBuilder webserviceURLBuilder = new StringBuilder(constants.getSchemaListByFormURL());

			webserviceURLBuilder.append("?");

			for (String formStructureName : formStuctureNames) {
				webserviceURLBuilder.append("formName=").append(URLEncoder.encode(formStructureName, "UTF-8"))
						.append("&");
			}

			schemaList = dictionaryWebserviceProvider.getSchemaList(webserviceURLBuilder.toString());
		} catch (UnsupportedEncodingException e) {
			log.error("Schema options is empty " + e.getMessage());
			e.printStackTrace();
		}

		return schemaList;
	}

	/**
	 * {@inheritDoc}
	 */
	public Set<DataTableColumnWithUri> getColumnsWithNoData(DataCart dataCart, String userName) {
		return instancedDataManager.getColumnsWithNoData(dataCart.getSelectedForms(),
				InstancedDataUtil.getAccountNode(userName));
	}

	@Override
	public Long getFileSize(String studyName, String datasetName, String fileName) {

		Long size = 0L;

		try {
			SftpClient client = SftpClientManager.getClient(constants.getDataDropEndpointInfo());

			RestRepositoryProvider restRepositoryProvider = new RestRepositoryProvider(constants.getModulesSTURL(),
					QueryRestProviderUtils.getProxyTicket(constants.getModulesSTURL()));
			String filePath = restRepositoryProvider.getDatasetFilePath(constants.getDatasetFilePathURL(), studyName,
					datasetName, fileName);
			log.info("QT file size: " + filePath + fileName);
			size = client.getFileSize(filePath, fileName);

		} catch (JSchException e) {
			log.error(e.getMessage(), e);
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}

		return size;
	}

	@Override
	public byte[] downloadFileBytes(String studyPrefixedId, String datasetName, String fileName) {

		byte[] fileBytes = null;

		try {
			SftpClient client = SftpClientManager.getClient(constants.getDataDropEndpointInfo());

			RestRepositoryProvider restRepositoryProvider = new RestRepositoryProvider(constants.getModulesSTURL(),
					QueryRestProviderUtils.getProxyTicket(constants.getModulesSTURL()));
			String imagePath = restRepositoryProvider.getDatasetFilePath(constants.getDatasetFilePathURL(),
					studyPrefixedId, datasetName, fileName);

			log.info("QT File Path: " + imagePath + fileName);
			fileBytes = client.downloadBytes(fileName, imagePath);
		} catch (JSchException e) {
			log.error(e.getMessage(), e);
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
		return fileBytes;
	}
}
