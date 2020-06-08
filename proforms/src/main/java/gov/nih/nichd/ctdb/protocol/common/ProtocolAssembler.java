package gov.nih.nichd.ctdb.protocol.common;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import gov.nih.nichd.ctdb.attachments.domain.Attachment;
import gov.nih.nichd.ctdb.common.AssemblerException;
import gov.nih.nichd.ctdb.common.CtdbAssembler;
import gov.nih.nichd.ctdb.common.CtdbLookup;
import gov.nih.nichd.ctdb.common.util.Utils;
import gov.nih.nichd.ctdb.protocol.domain.Procedure;
import gov.nih.nichd.ctdb.protocol.domain.ProcedureType;
import gov.nih.nichd.ctdb.protocol.domain.Protocol;
import gov.nih.nichd.ctdb.protocol.form.ProtocolForm;
import gov.nih.tbi.commons.model.StudyType;

/**
 * Transforms the protocol domain object into the protocolForm object and the protocolForm object into the protocol
 * domain object.
 *
 * @author CIT
 * @version 1.0
 */
public class ProtocolAssembler extends CtdbAssembler {

	/**
	 * Transforms a Protocol Domain Object to a ProtocolForm object
	 *
	 * @param protocol - The Protocol object to transform to the ProtocolForm object
	 * @throws AssemblerException Thrown if any error occurs while transforming the Protocol domain object to the
	 *         ProtocolForm object
	 */
	public static synchronized void domainToForm(Protocol protocol, ProtocolForm protocolForm) throws AssemblerException {
		try {
			Attachment file = protocol.getDataSubmissionFile();

			protocolForm.setProtocolNumber(protocol.getProtocolNumber());
			protocolForm.setName(protocol.getName());
			protocolForm.setImageFileName(file.getFileName());
			protocolForm.setWelcomeUrl(protocol.getWelcomeUrl());
			protocolForm.setDescription(protocol.getDescription());
			protocolForm.setStatus(protocol.getStatus().getId());
			protocolForm.setOrganization(protocol.getOrginization());
			protocolForm.setId(protocol.getId());
			protocolForm.setBricsStudyId(protocol.getBricsStudyId());
			
			// Handle study type conversion.
			StudyType st = protocol.getStudyType();
			
			if ( st != null ) {
				protocolForm.setStudyType(st.getName());
			}
			else {
				protocolForm.setStudyType("");
			}
			
			protocolForm.setIsEvent("no");

			if (protocol.isUsePatientName()) {
				protocolForm.setUsePatientName("yes");
			}
			else {
				protocolForm.setUsePatientName("no");
			}
			
			if (protocol.isLockFormIntervals()) {
				protocolForm.setLockFormIntervals("yes");
			}
			else {
				protocolForm.setLockFormIntervals("no");
			}
			protocolForm.setPatientDisplayType(protocol.getPatientDisplayType());
			protocolForm.setAutoIncrementSubject(protocol.isAutoIncrementSubject());
			protocolForm.setSubjectNumberPrefix(protocol.getSubjectNumberPrefix());
			protocolForm.setSubjectNumberSuffix(protocol.getSubjectNumberSuffix());
			protocolForm.setSubjectNumberStart(protocol.getSubjectNumberStart());
			protocolForm.setAutoAssociatePatientRoles(protocol.isAutoAssociatePatientRoles());
			protocolForm.setEnableEsignature(protocol.isEnableEsignature());
			protocolForm.setReasonForEsignature(protocol.getAudit().getReason()); // audit
			protocolForm.setSelectedDefaults(protocol.getProtocolDefaults().getSelectedDefaults());
			protocolForm.setBtrisAccess(protocol.getBtrisAccess());
			protocolForm.setAccountableInvestigator(protocol.getAccountableInvestigator());
			protocolForm.setUseEbinder(protocol.isUseEbinder());
			protocolForm.setStudyProject(protocol.getStudyProject());
			protocolForm.setPrincipleInvestigatorId(protocol.getPrincipleInvestigatorId());
			protocolForm.setPrincipleInvestigator(protocol.getPrincipleInvestigator());
			protocolForm.setAccountableInvestigatorId(protocol.getAccountableInvestigatorId());
			protocolForm.setSite(protocol.getSite());
			protocolForm.setDrugDeviceList(protocol.getDrugDeviceList());
			protocolForm.setDrugDevice(protocol.getDrugDevice());
			protocolForm.setClinicalTrial(protocol.isClinicalTrial());
			protocolForm.setSiteHashMap(protocol.getSiteHashMap());
			protocolForm.setDrugDeviceHashMap(protocol.getDrugDeviceHashMap());
			protocolForm.setDeleteFlag(protocol.getDeleteFlag());
			protocolForm.setPsrHeader(protocol.getPsrHeader());
		}
		catch (Exception e) {
			throw new AssemblerException("Unable to assemble protocol form object.", e);
		}
	}


	/**
	 * Transforms a ProtocolForm object to a Protocol Domain Object
	 *
	 * @param protoForm - The ProtocolForm object to transform to the Protocol Domain Object
	 * @return The Protocol Domain Object
	 * @throws AssemblerException Thrown if any error occurs while transforming the ProtocolForm object to the Protocol
	 *         Domain Object
	 */
	public static synchronized Protocol formToDomain(ProtocolForm protoForm) throws AssemblerException {
		Protocol protocol = new Protocol();

		try {
			protocol.setProtocolNumber(protoForm.getProtocolNumber());
			protocol.setName(protoForm.getName());
			protocol.setWelcomeUrl(protoForm.getWelcomeUrl());
			protocol.setDescription(protoForm.getDescription());
			CtdbLookup status = new CtdbLookup();
			status.setId(protoForm.getStatus());
			protocol.setStatus(status);
			protocol.setOrginization(protoForm.getOrganization());
			protocol.setId(protoForm.getId());
			
			// Handle study type conversion.
			String typeName = protoForm.getStudyType();
			
			if ( !Utils.isBlank(typeName) ) {
				protocol.setStudyType(StudyType.getStudyTypeFromName(typeName));
			}
			
			protocol.setBricsStudyId(protoForm.getBricsStudyId());
			protocol.setIsEvent(false);
			protocol.setPrincipleInvestigator(protoForm.getPrincipleInvestigator());

			if (protoForm.getUsePatientName() != null && protoForm.getUsePatientName().equals("yes")) {
				protocol.setUsePatientName(true);
			}
			else {
				protocol.setUsePatientName(false);
			}

			if (protoForm.getLockFormIntervals() != null && protoForm.getLockFormIntervals().equals("yes")) {
				protocol.setLockFormIntervals(true);
			}
			else {
				protocol.setLockFormIntervals(false);
			}

			// Processes the submission file
           File formFile = protoForm.getUploadedFile();
           Attachment fileObj = new Attachment();
            
           if ( formFile != null )
           {
        	   if( protoForm.getRemove() && (formFile.exists()) )
	           {
        		   fileObj.setFileName(null);
        		   fileObj.setFile(null);
	           }
	           else
	           {
	        	   fileObj.setFileName(formFile.getAbsoluteFile().getName());
	        	   fileObj.setFile(formFile);	        	 
	           }
           }

			protocol.setDataSubmissionFile(fileObj);
			protocol.setPatientDisplayType(protoForm.getPatientDisplayType());
			protocol.setAutoAssociatePatientRoles(protoForm.isAutoAssociatePatientRoles());
			protocol.setEnableEsignature(protoForm.isEnableEsignature());
			protocol.getAudit().setReason(protoForm.getReasonForEsignature()); // audit
			protocol.setAutoIncrementSubject(protoForm.isAutoIncrementSubject());
			protocol.setSubjectNumberPrefix(protoForm.getSubjectNumberPrefix());
			protocol.setSubjectNumberSuffix(protoForm.getSubjectNumberSuffix());
			protocol.setSubjectNumberStart(protoForm.getSubjectNumberStart());
			protocol.getProtocolDefaults().setSelectedDefaults(protoForm.getSelectedDefaults());
			protocol.setBtrisAccess(protoForm.getBtrisAccess());
			protocol.setPrincipleInvestigator(protoForm.getPrincipleInvestigator());
			protocol.setAccountableInvestigator(protoForm.getAccountableInvestigator());
			protocol.setUseEbinder(protoForm.isUseEbinder());
			protocol.setStudyProject(protoForm.getStudyProject());
			protocol.setPrincipleInvestigatorId(protoForm.getPrincipleInvestigatorId());
			protocol.setAccountableInvestigatorId(protoForm.getAccountableInvestigatorId());
			// protocol.setStudySites(protoForm.getStudySites());
			protocol.setSite(protoForm.getSite());
			protocol.setDrugDeviceList(protoForm.getDrugDeviceList());
			protocol.setDrugDevice(protoForm.getDrugDevice());
			protocol.setClinicalTrial(protoForm.isClinicalTrial());
			protocol.setSiteHashMap(protoForm.getSiteHashMap());
			protocol.setDrugDeviceHashMap(protoForm.getDrugDeviceHashMap());
			protocol.setDeleteFlag(protoForm.getDeleteFlag());
			protocol.setPsrHeader(protoForm.getPsrHeader());
			
			protocol.setClinicalLocationList(protoForm.getProtoClinicLocList());
			protocol.setProcedureList(protoForm.getProtoProcedureList());
			protocol.setPointOfContact(protoForm.getProtoPOCList());
			protocol.setMilesStoneList(protoForm.getProtoMilesStoneList());
		}
		catch (Exception e) {
			throw new AssemblerException("Exception assembling protocol domain object.", e);
		}

		return protocol;
	}

	public static JSONArray procedureListTOJSONArray(List<Procedure> procedureList) throws JSONException {
		JSONArray jsonArray = new JSONArray();
		List<ProcedureType> procTypeList = new ArrayList<ProcedureType>();

		for (Procedure procedure : procedureList) {
			if (!procTypeList.contains(procedure.getProcedureType())) {
				procTypeList.add(procedure.getProcedureType());		
			}
		}

		Collections.sort(procTypeList, new Comparator<ProcedureType>() {
			@Override
			public int compare(ProcedureType procT1, ProcedureType procT2) {
				return procT1.getName().compareTo(procT2.getName());
			}
		});
		
		for(ProcedureType procType : procTypeList){
			JSONObject jsonObj = new JSONObject();
			jsonObj.put("proceduretype", procType.getName());
			
			JSONArray innerJsonArr = new JSONArray();
			for (Procedure procedure : procedureList) {
				if (procedure.getProcedureType() == procType) {
					JSONObject innerJsonObj = new JSONObject();

					innerJsonObj.put("id", procedure.getId());
					innerJsonObj.put("name", procedure.getName());
					innerJsonArr.put(innerJsonObj);
				}
			}
			
			jsonObj.put("procedureList", innerJsonArr);
			jsonArray.put(jsonObj);
		}
		
		return jsonArray;
	}
}
