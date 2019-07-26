package gov.nih.tbi.metastudy.portal;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.Date;
import java.util.List;

import gov.nih.tbi.PortalConstants;
import gov.nih.tbi.commons.model.BRICSTimeDateUtil;
import gov.nih.tbi.commons.service.ServiceConstants;
import gov.nih.tbi.metastudy.model.hibernate.MetaStudy;
import gov.nih.tbi.metastudy.model.hibernate.MetaStudyAccessRecord;
import gov.nih.tbi.metastudy.model.hibernate.MetaStudyData;
import gov.nih.tbi.metastudy.model.hibernate.MetaStudyDocumentation;
import gov.nih.tbi.repository.service.util.AccessReportExportUtil;

import org.apache.log4j.Logger;


public class MetaStudyExportAction extends BaseMetaStudyAction {

	private static final long serialVersionUID = 4636928399164824419L;

	private static Logger logger = Logger.getLogger(MetaStudyExportAction.class);

	
	//these two parameters are used to download data and set access records
	private long  metaStudyId;
	private long metaStudyDataId;
	private long supportingDocId;
	private long fileId;
	
	private InputStream inputStream;
	private String exportFileName;
	
	public String download(){
		MetaStudy metaStudy = metaStudyManager.getMetaStudyById(metaStudyId);
		
		//we are only keeping track of published meta studies per requirement
			if(metaStudyDataId != 0){
				for(MetaStudyData dataRecord : metaStudy.getMetaStudyDataSet()){
					if(dataRecord.getId() == metaStudyDataId){
						if(metaStudy.isPublished()){
							metaStudyManager.addAccessRecord(metaStudy, getAccount(), null, dataRecord);
						}
						setFileId(dataRecord.getUserFile().getId());
						break;
					}
				}
			} else {
				for(MetaStudyDocumentation supportingDoc : metaStudy.getSupportingDocumentationSet()){
					if(supportingDoc.getId() == supportingDocId){
						if(metaStudy.isPublished()){
							metaStudyManager.addAccessRecord(metaStudy, getAccount(), supportingDoc, null);
						}
						setFileId(supportingDoc.getUserFile().getId());
						break;
					}
				}
			}
		return "redirectToFileDownload";
	}
	
	public String exportAccessRecords(){
		List<MetaStudyAccessRecord> accessRecords = metaStudyManager.getAccessRecordByMetaStudyId(metaStudyId);
		
		AccessReportExportUtil util = new AccessReportExportUtil(ServiceConstants.EXPORT_META_STUDY_ACCESS_RECORD_CSV_HEADERS);
		util.writeMetaStudyAccessData(accessRecords);
		
		ByteArrayOutputStream baos = util.getOutputStream();
		// NEW PAGINATED EXPORT ENDS HERE

		inputStream = new ByteArrayInputStream(baos.toByteArray());
		exportFileName = "Meta_Study_Access_Report_" + BRICSTimeDateUtil.formatDate(new Date()) + ".csv";

		return PortalConstants.ACTION_EXPORT;
	}
	
	public void setMetaStudyId(long metaStudyId){
		this.metaStudyId = metaStudyId;
	}
	public long getMetaStudyId(){
		return this.metaStudyId;
	}
	public void setMetaStudyDataId(long metaStudyDataId){
		this.metaStudyDataId = metaStudyDataId;
	}
	public long getmetaStudyDataId(){
		return this.metaStudyDataId;
	}
	public void setSupportingDocId(long supportingDocId){
		this.supportingDocId = supportingDocId;
	}
	public long getSupportingDocId(){
		return this.supportingDocId;
	}
	public void setFileId(long fileId){
		this.fileId = fileId;
	}
	public long getFileId(){
		return this.fileId;
	}
	public void setInputStream(InputStream inputStream){
		this.inputStream = inputStream;
	}
	public InputStream getInputStream(){
		return inputStream;
	}
	public void setExportFileName(String exportFileName){
		this.exportFileName = exportFileName;
	}
	public String getExportFileName(){
		return exportFileName;
	}
}
