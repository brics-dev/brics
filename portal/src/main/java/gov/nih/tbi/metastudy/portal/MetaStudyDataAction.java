package gov.nih.tbi.metastudy.portal;

import java.io.File;
import java.io.IOException;
import java.io.Writer;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

import com.jcraft.jsch.JSchException;

import gov.nih.tbi.PortalConstants;
import gov.nih.tbi.account.model.hibernate.EntityMap;
import gov.nih.tbi.commons.model.EntityType;
import gov.nih.tbi.commons.model.RoleType;
import gov.nih.tbi.commons.model.hibernate.FileType;
import gov.nih.tbi.commons.service.QueryToolManager;
import gov.nih.tbi.commons.service.ServiceConstants;
import gov.nih.tbi.commons.service.UserPermissionException;
import gov.nih.tbi.commons.util.BRICSFilesUtils;
import gov.nih.tbi.idt.ws.IdtInterface;
import gov.nih.tbi.idt.ws.InvalidColumnException;
import gov.nih.tbi.idt.ws.Struts2IdtInterface;
import gov.nih.tbi.metastudy.model.hibernate.MetaStudy;
import gov.nih.tbi.metastudy.model.hibernate.MetaStudyData;
import gov.nih.tbi.query.model.hibernate.SavedQuery;
import gov.nih.tbi.repository.model.hibernate.UserFile;
import gov.nih.tbi.taglib.datatableDecorators.MetaStudyDataIdtListDecorator;
import gov.nih.tbi.taglib.datatableDecorators.MetaStudySavedQueryIdtListDecorator;

public class MetaStudyDataAction extends BaseMetaStudyAction {

	private static final long serialVersionUID = 4165749296068229995L;

	private static Logger logger = Logger.getLogger(MetaStudyDataAction.class);

	@Autowired
	protected QueryToolManager queryToolManager;

	// Data
	private List<SavedQuery> savedQueryList;
	private boolean isEditingData = false;
	private FileType dataFileType;
	private String version;
	private String dataDescription;
	private String dataSource;
	private Long savedQueryId;
	private String savedQueryName;
	private Long editSavedQueryId;

	private File uploadData;
	private String uploadDataContentType;
	private String uploadDataFileName;

	// Hidden field that maps to the uploaded file name for validation purpose
	private String uploadFileName;


	/**
	 * Returns all Meta Study Data Architect file types.
	 * 
	 * @return all Meta Study Data Architect file types.
	 */
	public List<FileType> getMetaStudyDataTypes() {
		return staticManager.getMetaStudyDataFileTypeList();
	}


	/**
	 * Returns the list of MetaStudyData objects in the Meta Study.
	 * 
	 * @return the list of MetaStudyData objects in the Meta Study.
	 */
	public List<MetaStudyData> getMetaStudyDataList() {

		List<MetaStudyData> dataList = new ArrayList<MetaStudyData>();
		dataList.addAll(getSessionMetaStudy().getMetaStudy().getMetaStudyDataSet());

		return dataList;
	}

	/**
	 * This action retrieves all Saved Query that user has access to and displays them in the Select Saved Query dialog.
	 * 
	 * @return a String that opens the Select Saved Query dialog.
	 */
	public String selectSavedQueryDialog() {

		return PortalConstants.ACTION_SELECT_SAVED_QUERY_DIALOG;
	}
	
	public String getSlectedSavedQueryList() {

		Set<Long> savedQueryIds = new HashSet<Long>();

		if (accountManager.hasRole(getAccount(), RoleType.ROLE_ADMIN)) {
			// Sysadmin has access to all saved queries
			savedQueryList = queryToolManager.searchSavedQuery(null,null,null,null,null,false);

		} else {
			List<EntityMap> entityList = accountManager.listUserAccess(getAccount(), EntityType.SAVED_QUERY, true);
			if (!entityList.isEmpty()) {
				for (EntityMap e : entityList) {
					savedQueryIds.add(e.getEntityId());
				}
				savedQueryList = queryToolManager.searchSavedQuery(savedQueryIds,null,null,null,null,false);
			}
			
		}

		if (savedQueryList == null) {
			savedQueryList = new ArrayList<SavedQuery>();
		}
		return null;
	}

	/**
	 * This method is called when user selects a Saved Query and returns back to the edit Saved Query dialog. If the
	 * selected saved query has a description, it will be carries over to the edit dialog as "Copy Of " + description.
	 * 
	 * @return a String that opens Add Saved Query dialog.
	 */
	public String addSavedQueryDialog() {

		if (!StringUtils.isBlank(dataDescription)) {
			this.setDataDescription(PortalConstants.COPY_OF + dataDescription);
		}
		return PortalConstants.ACTION_ADD_SAVED_QUERY_DIALOG;
	}


	/**
	 * This method is called when user clicks the Add Data Artifact -> File link.
	 * 
	 * @return a String that opens Add File dialog.
	 */
	public String addDataFileDialog() {
		return PortalConstants.ACTION_ADD_DATA_FILE_DIALOG;
	}

	public void validate(){

		if (hasFieldErrors()) {
			logger.debug("validate() has field errors: "+getFieldErrors().size());
			getSessionUploadFile().clear();
			if (uploadData != null){
				try {
					getSessionUploadFile().setUploadFile(BRICSFilesUtils.copyFile(uploadData));
				} catch (IOException e) {
					logger.error("Failed to read upload file to byte array in validation.");
					e.printStackTrace();
				}

				getSessionUploadFile().setUploadFileFileName(uploadDataFileName);
				getSessionUploadFile().setUploadFileContentType(uploadDataContentType);
				
			}
        } 
	}

	/**
	 * This action is performed when user clicks Save button from Add/Edit Saved Query or File dialog to save a new or
	 * modified MetaStudyData object.
	 * 
	 * @returns a String that redirects to the Add/Edit Data Artifact page.
	 * @throws SocketException
	 * @throws IOException
	 * @throws JSchException
	 */
	public String uploadData() throws SocketException, IOException, JSchException, UserPermissionException {

		MetaStudy currentMetaStudy = getSessionMetaStudy().getMetaStudy();
		MetaStudyData msd = null;

		String selectedDataName = getSessionMetaStudy().getSelectedDataName();
		if (isEditingData && selectedDataName != null) {
			Set<MetaStudyData> currentDataSet = currentMetaStudy.getMetaStudyDataSet();

			for (MetaStudyData data : currentDataSet) {
				if (data.getName().equals(selectedDataName)) {
					msd = data;
					break;
				}
			}
		} else {
			msd = new MetaStudyData();
			msd.setDateCreated(new Date());
			currentMetaStudy.addMetaStudyData(msd);
		}

		msd.setDescription(this.getDataDescription());
		msd.setSource(this.getDataSource());
		getSessionMetaStudy().addNewMetaStudyData(msd);
		
		if(getSessionUploadFile().getUploadFile() != null && uploadData == null){
			uploadData = getSessionUploadFile().getUploadFile();
			uploadDataFileName = getSessionUploadFile().getUploadFileFileName();
			uploadDataContentType = getSessionUploadFile().getUploadFileContentType();
		}

		if (this.getSavedQueryId() == null) {
			// Create new user file only if it's new data or edited data's file name changed
			if (!isEditingData
					|| (isEditingData && uploadDataFileName != null && !uploadDataFileName.equals(msd.getName()))) {
				if (uploadDataFileName == null && uploadFileName !=null){
					uploadDataFileName = uploadFileName;
				}
				UserFile userFile =
						metaStudyManager.uploadFile(getUser().getId(), uploadData, uploadDataFileName, 
								null, ServiceConstants.FILE_TYPE_META_STUDY_DATA, new Date());
				msd.setUserFile(userFile);
			} 
			
			msd.setFileType(this.getDataFileType());

			if (!StringUtils.isBlank(getVersion())) {
				msd.setVersion(this.getVersion());
			}
		} else {
			SavedQuery savedQuery = queryToolManager.getSavedQueryBySavedQueryId(getAccount(), savedQueryId);
			msd.setSavedQuery(savedQuery);
			msd.setFileType(metaStudyManager.getSavedQueryFileType());
		}

		getSessionMetaStudy().setMetaStudy(currentMetaStudy);
		getSessionMetaStudy().setSelectedDataName(null);
		
		// Clear uploaded file.
		getSessionUploadFile().clear();
		uploadData = null;

		Writer rw = getResponse().getWriter();
		
		rw.write("success");
		rw.flush();
		rw.close();

		return null;
	}

	// url: http://fitbir-portal-local.cit.nih.gov:8081/portal/metastudy/metaStudyDataAction!getUploadMetaStudyData.action
	public String getUploadMetaStudyData() {
		try {
			IdtInterface idt = new Struts2IdtInterface();
			ArrayList<MetaStudyData> sdList =
					new ArrayList<MetaStudyData>(getSessionMetaStudy().getMetaStudy().getMetaStudyDataSet());
			idt.setList(sdList);
			idt.decorate(new MetaStudyDataIdtListDecorator());
			idt.setTotalRecordCount(sdList.size());
			idt.setFilteredRecordCount(sdList.size());
			idt.output();
		} catch (InvalidColumnException e) {
			logger.error("invalid column: " + e);
			e.printStackTrace();
		}
		return null;
	}
	
	// url: http://fitbir-portal-local.cit.nih.gov:8081/portal/metastudy/metaStudyDataAction!getSavedQuerySet.action
	public String getSavedQuerySet() {
		try {
			IdtInterface idt = new Struts2IdtInterface();
			getSlectedSavedQueryList();
			ArrayList<SavedQuery> sdList =
					new ArrayList<SavedQuery>(this.savedQueryList);
			idt.setList(sdList);
			idt.decorate(new MetaStudySavedQueryIdtListDecorator());
			idt.setTotalRecordCount(sdList.size());
			idt.setFilteredRecordCount(sdList.size());
			idt.output();
		} catch (InvalidColumnException e) {
			logger.error("invalid column: " + e);
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * This action is called when user selects a data artifact from the table and clicks Edit button to make changes. It
	 * sets the selected data name in the session to track which data artifact we are editing.
	 * 
	 * @return a String that opens the Edit Saved Query or Edit File dialog.
	 */
	public String editData() {

		String result = null;

		String dataNameTemp = getRequest().getParameter(PortalConstants.META_STUDY_DATA_NAME);
		if (!StringUtils.isBlank(dataNameTemp)) {
			dataNameTemp = dataNameTemp.replace("\\", "");
			
			// No need to do this anymore with the new Datatable
			
			//TODO: this should really use IDs since names are not forced to be unique
			//update below such that metastudy values were unique, the beginning of the value should always be [id]_[sq_name]
//			String[] dataNameSplit = dataNameTemp.split("_",2); //remove [id]_ since we retrieve data based on name
//			String dataName = dataNameSplit[1];
			
			Set<MetaStudyData> currentDataSet = getSessionMetaStudy().getMetaStudy().getMetaStudyDataSet();

			for (MetaStudyData msd : currentDataSet) {
				if (msd.getName().equals(dataNameTemp)) {
					this.setIsEditingData(true);
					this.setDataDescription(msd.getDescription());
					this.setDataFileType(msd.getFileType().getId());
					this.setVersion(msd.getVersion());
					this.setDataSource(msd.getSource());

					if (msd.getSavedQuery() != null) {
						this.setSavedQueryId(msd.getSavedQuery().getId());
						this.setSavedQueryName(msd.getSavedQuery().getName());
						this.setEditSavedQueryId(msd.getSavedQuery().getId());
						result = PortalConstants.ACTION_ADD_SAVED_QUERY_DIALOG;
					} else {
						this.setUploadFileName(msd.getUserFile().getName());
						this.setUploadDataFileName(msd.getUserFile().getName());
						result = PortalConstants.ACTION_ADD_DATA_FILE_DIALOG;
					}

					getSessionMetaStudy().setSelectedDataName(dataNameTemp);
					return result;
				}
			}
		}

		return result;
	}

	
	/**
	 * This action is called when user selects one or many data artifacts and clicks Delete button to remove them from
	 * the Meta Study.
	 * 
	 * @return a String that refreshes the Data Artifacts page.
	 */
	public String removeData() {

		String dataNames = getRequest().getParameter(PortalConstants.META_STUDY_DATA_NAME);

		Set<MetaStudyData> currentDataSet = getSessionMetaStudy().getMetaStudy().getMetaStudyDataSet();

		if (!StringUtils.isBlank(dataNames)) {
			String[] dataNameArr = dataNames.split(",");

			for (int i = 0; i < dataNameArr.length; i++) {
				String dataNameTemp = dataNameArr[i].replace("\\", "");  // Remove backslash that may be added in the request
																	// call
				
				//update below such that metastudy values were unique, the beginning of the value should always be [id]_[sq_name]
//				String[] dataNameSplit = dataNameTemp.split("_",2); //remove [id]_ since we retrieve data based on name
//				String dataName = dataNameSplit[1];

				for (Iterator<MetaStudyData> it = currentDataSet.iterator(); it.hasNext();) {
					MetaStudyData msd = it.next();

					if (msd.getName().equals(dataNameTemp)) {
						// if it's a new meta study data, we also delete the user files associated with it.
						if (msd.getId() == null && msd.getUserFile() != null) {
							metaStudyManager.removeUserFile(msd.getUserFile());
						}

						it.remove();
						logger.debug("Removed Meta Study data " + dataNameTemp);
						break;
					}
				}
			}
		}

		return PortalConstants.ACTION_DATASET;
	}

	public List<SavedQuery> getSavedQueryList() {
		return savedQueryList;
	}

	public boolean getIsEditingData() {
		return isEditingData;
	}

	public void setIsEditingData(boolean isEditingData) {
		this.isEditingData = isEditingData;
	}

	public FileType getDataFileType() {
		return dataFileType;
	}

	public void setDataFileType(Long dataFileType) {
		if (dataFileType != null) {
			for (FileType type : this.getMetaStudyDataTypes()) {
				if (type.getId().equals(dataFileType)) {
					this.dataFileType = type;
					break;
				}
			}
		}
	}
	
	public String getVersion() {
		return version;
	}

	public void setVersion(String version) {
		this.version = version;
	}

	public String getDataDescription() {
		return dataDescription;
	}

	public void setDataDescription(String dataDescription) {
		this.dataDescription = dataDescription;
	}

	public String getDataSource() {
		return dataSource;
	}

	public void setDataSource(String dataSource) {
		this.dataSource = dataSource;
	}

	public Long getSavedQueryId() {
		return savedQueryId;
	}

	public void setSavedQueryId(Long savedQueryId) {
		this.savedQueryId = savedQueryId;
	}
	
	public String getSavedQueryName() {
		return savedQueryName;
	}

	public void setSavedQueryName(String savedQueryName) {
		this.savedQueryName = savedQueryName;
	}

	public File getUploadData() {
		return uploadData;
	}

	public void setUploadData(File uploadData) {
		this.uploadData = uploadData;
	}

	public String getUploadDataContentType() {
		return uploadDataContentType;
	}

	public void setUploadDataContentType(String uploadDataContentType) {
		this.uploadDataContentType = uploadDataContentType;
	}

	public String getUploadDataFileName() {
		return uploadDataFileName;
	}

	public void setUploadDataFileName(String uploadDataFileName) {
		this.uploadDataFileName = uploadDataFileName;
	}

	public String getUploadFileName() {
		return uploadFileName;
	}

	public void setUploadFileName(String uploadFileName) {
		this.uploadFileName = uploadFileName;
	}
	public Long getEditSavedQueryId(){
		return this.editSavedQueryId;
	}
	public void setEditSavedQueryId(Long editSavedQueryId){
		this.editSavedQueryId = editSavedQueryId;
	}

}
