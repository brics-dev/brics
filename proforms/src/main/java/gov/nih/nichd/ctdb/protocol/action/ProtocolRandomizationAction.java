package gov.nih.nichd.ctdb.protocol.action;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import au.com.bytecode.opencsv.CSVReader;
import gov.nih.nichd.ctdb.common.BaseAction;
import gov.nih.nichd.ctdb.common.CtdbConstants;
import gov.nih.nichd.ctdb.common.CtdbException;
import gov.nih.nichd.ctdb.common.StrutsConstants;
import gov.nih.nichd.ctdb.common.navigation.LeftNavController;
import gov.nih.nichd.ctdb.protocol.domain.CsvImportUtil;
import gov.nih.nichd.ctdb.protocol.domain.Protocol;
import gov.nih.nichd.ctdb.protocol.domain.ProtocolRandomization;
import gov.nih.nichd.ctdb.protocol.manager.ProtocolManager;
import gov.nih.nichd.ctdb.protocol.tag.ProtoRandomizationIdtDecorator;
import gov.nih.tbi.idt.ws.IdtInterface;
import gov.nih.tbi.idt.ws.InvalidColumnException;
import gov.nih.tbi.idt.ws.Struts2IdtInterface;

public class ProtocolRandomizationAction extends BaseAction{

	private static final long serialVersionUID = 6360470100879672871L;
	private static final Logger logger = Logger.getLogger(ProtocolRandomizationAction.class);
	
	public static final String RANDOMIZATION_GROUP_NAME = "Group Name";
	public static final String RANDOMIZATION_GROUP_DESCRIPTION = "Group Description";

	List<ProtocolRandomization> randomizationList = new ArrayList<ProtocolRandomization>();
	private File upload;
	private String uploadContentType;
	
	
	public String execute() throws CtdbException {
		setupPage();

		Protocol p = (Protocol) session.get(CtdbConstants.CURRENT_PROTOCOL_SESSION_KEY);
		if (p.getHasRandomization()) {
			ProtocolManager pm = new ProtocolManager();
			randomizationList = pm.getProtoRandomizationListByProto(p.getId());
		}
		session.put("protoRandomizationList", randomizationList);
		return StrutsConstants.ACTION_VIEW_RANDOMIZATION;
	}
	
	private void setupPage() {
		buildLeftNav(LeftNavController.LEFTNAV_STUDY_RANDOMIZATION);
	}
	
	public String getProtoRandomizationIdtList() throws CtdbException {
		List<ProtocolRandomization> randomList =  new ArrayList<ProtocolRandomization>();
		randomList = (List<ProtocolRandomization>) session.get("protoRandomizationList");	
		
		try {			
			IdtInterface idt = new Struts2IdtInterface();			
			ArrayList<ProtocolRandomization> outputList = new ArrayList<ProtocolRandomization>(randomList);
			idt.setList(outputList);
			idt.setTotalRecordCount(outputList.size());
			idt.setFilteredRecordCount(outputList.size());
			idt.decorate(new ProtoRandomizationIdtDecorator());
			idt.output();

		} catch (InvalidColumnException e) {
			logger.error("invalid column: " + e);
			e.printStackTrace();
		}
		
		return null;
	}
	
	public String importRandomizationList() throws Exception {
		setupPage();
		List<ProtocolRandomization> importedRandomList = new ArrayList<ProtocolRandomization>();
		
		if ("application/vnd.ms-excel".equalsIgnoreCase(uploadContentType)
				|| "text/csv".equalsIgnoreCase(uploadContentType)
				|| "application/csv".equalsIgnoreCase(uploadContentType)) {
			// file itself is in File upload
			CSVReader reader = null;

			try {
				reader = new CSVReader(new FileReader(upload));				
				List<String[]> lines = reader.readAll();
				
				if (lines.size() > 0) {
					String[] headers = lines.get(0);
					CsvImportUtil csvMappingUtil = new CsvImportUtil(headers);

					List<String> headerErrors = this.validateImportHeaderGetErrors(headers);
					if (!headerErrors.isEmpty()) {
						for (String headerError : headerErrors) {
							addActionError(headerError);
						}
						return StrutsConstants.ACTION_VIEW_RANDOMIZATION;
					}

					// validate all rows
					List<String> validationErrors = this.validateImportingFile(csvMappingUtil, lines);
					for (String error : validationErrors) {
						addActionError(error);
					}

					if (!hasActionErrors()) {
						Protocol p = (Protocol) session.get(CtdbConstants.CURRENT_PROTOCOL_SESSION_KEY);
						int protocolId = p.getId();
						ProtocolManager pm = new ProtocolManager();
						randomizationList = pm.getProtoRandomizationListByProto(protocolId);

						long lastRanSeq = Integer.MIN_VALUE;
						if (randomizationList.size() >0) {
							ProtocolRandomization lastProtoRan = randomizationList.get(randomizationList.size() -1);
							lastRanSeq = lastProtoRan.getSequence();
						} else {
							lastRanSeq = 0;
						}
						
						// load the rows
						for (int i = 1; i < lines.size(); i++) {
							String[] line = lines.get(i);

							String lineGrpName = csvMappingUtil.getValueByHeader(line,
									ProtocolRandomizationAction.RANDOMIZATION_GROUP_NAME);
							String lineGrpDesc = csvMappingUtil.getValueByHeader(line,
									ProtocolRandomizationAction.RANDOMIZATION_GROUP_DESCRIPTION);
							

							// the above fail gracefully to a null, so check here
							// where we can handle it with messaging
							if (lineGrpName == null || lineGrpName.equals("")) {
								addActionError(
										"The Randomlization Group Name header was missing.  Please check your input file and try again.");
							} else if (lineGrpDesc == null || lineGrpDesc.equals("")) {
								addActionError(
										"The Randomlization Group Description header was missing.  Please check your input file and try again.");
							}
							// if schema PV ID or schema PV are null, no problem
							else {
																
								ProtocolRandomization protoRandom = new ProtocolRandomization();
								protoRandom.setProtocolId(protocolId);
								protoRandom.setSequence(lastRanSeq + i);
								protoRandom.setGroupName(lineGrpName);
								protoRandom.setGroupDescription(lineGrpDesc);

								int protoRandomId = pm.createProtoRandomization(protoRandom);	
								
								if(protoRandomId > 0) {
									importedRandomList.add(protoRandom);
								}
							}
						}
					}
				}

			} catch (Exception e) {
				logger.error("Username: " + getUser().getDisplayName()
						+ ".  Exception occurred while performing a randomization mapping");
				e.printStackTrace();
				addActionError(
						"Creating the mapping failed because of an error.  Please check your input file and try again.");
			} finally {
				try {
					if (reader != null)
						reader.close();
				} catch (IOException e) {
					logger.error("Username: " + getUser().getDisplayName() + ".  Failed to close CSVReader");
					throw new IOException(e); // cant handle this, throw up
				}
			}
		}

		else {
			// File uploaded is not a CSV
			addActionError(
					"Invalid file type. If the file is open in another application please close it and try again.");
		}

		// if there are no errors, send up a success
		if (!this.hasActionErrors()) {
			if(importedRandomList.size() > 0) {
				randomizationList.addAll(importedRandomList);
			}
			session.put("protoRandomizationList", randomizationList);
			this.addActionMessage("The Randomization List has been uploaded successfully");
		}
		return StrutsConstants.ACTION_VIEW_RANDOMIZATION;
	}
	
	public List<String> validateImportHeaderGetErrors(String[] headers) {
		List<String> errors = new ArrayList<String>();
		for (String header : headers) {
			String headerFormatted = header.trim();
			if (!headerFormatted.equalsIgnoreCase(ProtocolRandomizationAction.RANDOMIZATION_GROUP_NAME)
					&& !headerFormatted.equalsIgnoreCase(ProtocolRandomizationAction.RANDOMIZATION_GROUP_DESCRIPTION)) {
				errors.add("header " + header + " is not a part of the template");
			}
		}
		return errors;
	}
	
	private List<String> validateImportingFile(CsvImportUtil csvMappingUtil, List<String[]> lines) {
		List<String> validationErrors = new ArrayList<String>();
		for (int i = 1; i < lines.size(); i++) {
			String[] line = lines.get(i);
			validationErrors.addAll(this.validateImportDataRow(csvMappingUtil, line));
			if (validationErrors.size() > 20) {
				validationErrors.add(
						"More than 20 errors exist in your input document.  Please correct the above errors before continuing");
				break;
			}
		}
		return validationErrors;
	}
	
	public List<String> validateImportDataRow(CsvImportUtil csvMappingUtil, String[] rowElements) {
		List<String> errors = new ArrayList<String>();

		String lineGrpName = csvMappingUtil.
				getValueByHeader(rowElements, ProtocolRandomizationAction.RANDOMIZATION_GROUP_NAME).trim();
		String lineGrpDesc = csvMappingUtil.
				getValueByHeader(rowElements, ProtocolRandomizationAction.RANDOMIZATION_GROUP_DESCRIPTION).trim();

			if (StringUtils.isBlank(lineGrpName)) {
				errors.add(String.format("Randomization Group Name is required. A value is missing in this field. Please check your input file and try again."));
			}
			if (StringUtils.isBlank(lineGrpDesc)) {
				errors.add(String.format("Randomization Group Description is required. A value is missing in this field. Please check your input file and try again."));
			}

		return errors;
	}
	
	public List<ProtocolRandomization> getProtocolRandomization() {
		return this.randomizationList;
	}	
	public void setProtocolRandomization(List<ProtocolRandomization> randomizationList) {
		this.randomizationList.clear();
		if(randomizationList != null){
			this.randomizationList.addAll(randomizationList);
		}
	}
	public File getUpload() {
		return upload;
	}

	public void setUpload(File upload) {
		this.upload = upload;
	}

	public String getUploadContentType() {
		return uploadContentType;
	}

	public void setUploadContentType(String uploadContentType) {
		this.uploadContentType = uploadContentType;
	}

}
