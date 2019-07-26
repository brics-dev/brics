package gov.nih.tbi.guid.portal;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

import com.google.gson.JsonSyntaxException;

import gov.nih.tbi.PortalConstants;
import gov.nih.tbi.commons.portal.BaseAction;
import gov.nih.tbi.commons.service.RepositoryManager;
import gov.nih.tbi.guid.exception.InvalidJwtException;
import gov.nih.tbi.guid.ws.GuidWebserviceProvider;
import gov.nih.tbi.guid.ws.exception.AuthenticationException;
import gov.nih.tbi.idt.ws.IdtInterface;
import gov.nih.tbi.idt.ws.InvalidColumnException;
import gov.nih.tbi.idt.ws.Struts2IdtInterface;
import gov.nih.tbi.repository.model.hibernate.DatasetSubject;
import gov.nih.tbi.taglib.datatableDecorators.GuidDatasetListIdtDecorator;

public class ViewGuidAction extends BaseAction {
	
	private static final long serialVersionUID = 1L;
	static Logger logger = Logger.getLogger(ViewGuidAction.class);
	
	public String guid;
	public String guidDetails;

	@Autowired
	private RepositoryManager repositoryManager;

	@Autowired
	private GuidWebserviceProvider guidWebserviceProvider;

	public String view() {
		try {
			String jwt = getGuidJwt();
			// get guid details, store in guidDetails
			guidDetails = guidWebserviceProvider.getGuidDetails(guid, jwt);
		}
		catch (AuthenticationException e) {
			logger.error("User failed to authenticate to the Centralized GUID server", e);
		} 
		catch (JsonSyntaxException e) {
			// has good enough description in the exception
			logger.error(e);
		} 
		catch (InvalidJwtException e) {
			logger.error("Centralized GUID server responded with an invalid JWT", e);
		}
		// go to the view page regardless. We can process the details
		// and display a general user-facing message there
		return PortalConstants.ACTION_VIEW;
	}

	public String datasetDatatable() {
		try {
			IdtInterface idt = new Struts2IdtInterface();
			ArrayList<DatasetSubject> datasetList = new ArrayList<DatasetSubject>(
					(List<DatasetSubject>) repositoryManager.getGuidJoinedDataList(getAccount(), guid));
			idt.setList(datasetList);
			idt.setTotalRecordCount(datasetList.size());
			idt.setFilteredRecordCount(datasetList.size());
			idt.decorate(new GuidDatasetListIdtDecorator());
			idt.output();
		} catch (InvalidColumnException e) {
			logger.error("invalid column: " + e);
			e.printStackTrace();
		}
		return null;
	}

	public String getGuidDetails() {
		return guidDetails;
	}

	public void setGuidDetails(String guidDetails) {
		this.guidDetails = guidDetails;
	}
	
	/**
	 * Returns true if namespace is 'guidAdmin'
	 */
	public boolean getInAdmin() {
		return PortalConstants.NAMESPACE_GUID_ADMIN.equals(getNameSpace());
	}

}
