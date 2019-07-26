package gov.nih.tbi.dictionary.portal;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

import gov.nih.tbi.PortalConstants;
import gov.nih.tbi.account.model.hibernate.Account;
import gov.nih.tbi.commons.service.DictionaryToolManager;
import gov.nih.tbi.commons.service.ServiceConstants;
import gov.nih.tbi.commons.service.UserPermissionException;
import gov.nih.tbi.dictionary.model.hibernate.eform.BasicEform;
import gov.nih.tbi.idt.ws.IdtInterface;
import gov.nih.tbi.idt.ws.InvalidColumnException;
import gov.nih.tbi.idt.ws.Struts2IdtInterface;
import gov.nih.tbi.portal.PortalUtils;
import gov.nih.tbi.repository.model.hibernate.Study;
import gov.nih.tbi.repository.model.hibernate.SupportingDocumentation;
import gov.nih.tbi.taglib.datatableDecorators.ResearchMgmtIdtListDecorator;
import gov.nih.tbi.taglib.datatableDecorators.StudyListIdtDecorator;

/**
 * Action class to handle eForm serach related user events
 * 
 * @author khanaly
 *
 */
public class EformSearchAction extends BaseEformAction {
	private static final long serialVersionUID = 5604081082402760174L;
	private static final Logger logger = Logger.getLogger(EformSearchAction.class);
	
	List<BasicEform> basicEformList;
	
	public String formStructureName;
	
	private int ownerId;
	
	@Autowired
	DictionaryToolManager dictionaryToolManager;

	
	public String list() {
		
		return PortalConstants.ACTION_LIST;
	}
	
	public String search() {
		boolean onlyOwned = (ownerId == 0);

		Account account = this.getAccount();
		String accountUrl = modulesConstants.getModulesAccountURL(getDiseaseId());
		String proxyTicket = PortalUtils.getProxyTicket(accountUrl);

		if (!onlyOwned && (getNameSpace().toLowerCase()
				.equalsIgnoreCase(PortalConstants.NAMESPACE_DICTIONARYADMIN.toLowerCase()) || getIsDictionaryAdmin())) {
			setBasicEformList(eformManager.searchEformWithFormStructureTitle(null));
		} else {
			setBasicEformList(eformManager.searchEform(account, onlyOwned, proxyTicket));
		}

		return PortalConstants.ACTION_SEARCH;
	}
	
	// http://fitbir-portal-local.cit.nih.gov:8080/portal/dictionary/eFormSearchAction!getSearch.action
	public String getSearch(){
		try {
			IdtInterface idt = new Struts2IdtInterface();
			search();
			ArrayList<BasicEform> outputList =
					new ArrayList<BasicEform>(getBasicEformList());
			idt.setList(outputList);
			idt.setTotalRecordCount(outputList.size());
			idt.setFilteredRecordCount(outputList.size());
			idt.output();
		} catch (InvalidColumnException e) {
			logger.error("invalid column: " + e);
			e.printStackTrace();
		}
		return null;
	}
	
	public String searchAssociatedFormStructures() throws UserPermissionException{
		
		if(dictionaryToolManager.getIsFormStructurePublished(formStructureName)){
			setBasicEformList(eformManager.basicEformSearch(null, null, formStructureName, null, null));
		} else {
			throw new UserPermissionException(ServiceConstants.READ_ACCESS_DENIED);
		}
		return PortalConstants.ACTION_LIST_ASSOCAITED_EFORMS;
	}
	
	// http://fitbir-portal-local.cit.nih.gov:8080/portal/dictionary/eFormSearchAction!getSearchAssociatedForm.action
	public String getSearchAssociatedForm() throws UserPermissionException{
		try {
			IdtInterface idt = new Struts2IdtInterface();
			searchAssociatedFormStructures();
			ArrayList<BasicEform> outputList =
					new ArrayList<BasicEform>(getBasicEformList());
			idt.setList(outputList);
			idt.setTotalRecordCount(outputList.size());
			idt.setFilteredRecordCount(outputList.size());
			idt.output();
		} catch (InvalidColumnException e) {
			logger.error("invalid column: " + e);
			e.printStackTrace();
		}
		return null;
	}
	
	/** 
	 * START SETTERS AND GETTERS
	 */
	
	public void setBasicEformList(List<BasicEform> basicEformList){
		this.basicEformList = basicEformList;
	}
	
	public List<BasicEform> getBasicEformList(){
		return this.basicEformList;
	}
	
	public void addBasicEform(BasicEform basicEform){
		this.basicEformList.add(basicEform);
	}
	
	public void setFormStructureName(String formStructureName){
		this.formStructureName = formStructureName;
	}
	
	public String getFormStructureName(){
		return this.formStructureName;
	}

	public int getOwnerId() {
		return ownerId;
	}

	public void setOwnerId(int ownerId) {
		this.ownerId = ownerId;
	}
	
}
