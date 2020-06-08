package gov.nih.tbi.util;

import java.util.Collection;
import java.util.List;
import java.util.NoSuchElementException;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import gov.nih.tbi.constants.ApplicationConstants;
import gov.nih.tbi.constants.QueryToolConstants;
import gov.nih.tbi.pojo.DataElement;
import gov.nih.tbi.pojo.FormResult;
import gov.nih.tbi.pojo.InstancedDataTable;
import gov.nih.tbi.pojo.authentication.AccountUserDetails;
import gov.nih.tbi.repository.model.FormHeader;
import gov.nih.tbi.repository.model.RepeatableGroupHeader;
import gov.nih.tbi.service.model.DataCart;
import gov.nih.tbi.ws.cxf.DataCartService;

@Component
public class InstanceSpecificQueryModifier {
	
	private static final Logger logger = Logger.getLogger(InstanceSpecificQueryModifier.class);
	
	private static final String ROLE_ADMIN = "ROLE_ADMIN";
	private static final String ROLE_QUERY_ADMIN = "ROLE_QUERY_ADMIN";
	private static final String MAIN_GROUP = "Main";
	private static final String VISIT_DATE_DE = "VisitDate";
	
	@Autowired
	ApplicationConstants constants;
	
	/**
	 * Performs all instance-specific data manipulation that needs to happen
	 * to the DataCart before the data can go to the user.  Private methods in 
	 * this class can be used to perform individual operations, this method 
	 * calls them.
	 * 
	 * @param dataCart
	 */
	public void modifyDataCart(DataCart dataCart) {
		modifyHeaders(dataCart);
		modifyData(dataCart);
	}
	
	/**
	 * Performs all instance-specific data manipulation that needs to happen
	 * TO THE HEADERS before the data can go to the user.  Private methods in 
	 * this class can be used to perform individual operations, this method 
	 * calls them.
	 * 
	 * @param dataCart
	 */
	private void modifyHeaders(DataCart dataCart) {
		// other instances can be added here
		if (QueryToolConstants.PDBP_ORG_NAME.equalsIgnoreCase(constants.getOrgName())) {
			pdbpRemoveVisitDateHeader(dataCart);
		}
	}
	
	/**
	 * Performs all instance-specific data manipulation that needs to happen
	 * TO THE DATA before the data can go to the user.  Private methods in 
	 * this class can be used to perform individual operations, this method 
	 * calls them.
	 * 
	 * @param dataCart
	 */
	private void modifyData(DataCart dataCart) {
		// other instances can be added here
		if (QueryToolConstants.PDBP_ORG_NAME.equalsIgnoreCase(constants.getOrgName())) {
			pdbpRemoveVisitDateData(dataCart);
		}
	}
	
	/**
	 * Removes the "VisitDate" data element's header from the table for non-admin
	 * (does not have ROLE_ADMIN or ROLE_QUERY_ADMIN roles) users.  This does not
	 * affect admin users.
	 * 
	 * @param dataCart
	 */
	private void pdbpRemoveVisitDateHeader(DataCart dataCart) {
		if (!isUserAdmin()) {
			InstancedDataTable idt = dataCart.getInstancedDataTable();
			List<FormHeader> formHeaders = idt.getHeaders();
			for (FormHeader formHeader : formHeaders) {
				List<RepeatableGroupHeader> rgHeaders = formHeader.getRepeatableGroupHeaders();
				for (RepeatableGroupHeader rgHeader : rgHeaders) {
					List<String> deHeaders = rgHeader.getDataElementHeaders();
					if (deHeaders.contains(VISIT_DATE_DE)) {
						deHeaders.remove(VISIT_DATE_DE);
					}
				}
			}
		}
	}
	
	/**
	 * Removes the "VisitDate" data element's data from the table for non-admin
	 * (does not have ROLE_ADMIN or ROLE_QUERY_ADMIN roles) users.  This does not
	 * affect admin users.
	 * 
	 * @param dataCart
	 */
	private void pdbpRemoveVisitDateData(DataCart dataCart) {
		if (!isUserAdmin()) {
			InstancedDataTable idt = dataCart.getInstancedDataTable();
			List<FormResult> forms = idt.getForms();
			for (FormResult form : forms) {
				try {
					DataElement de = DataCartUtil.getDataElement(dataCart, form.getUri(), VISIT_DATE_DE);
					de.setSelected(false);
				}
				catch(NoSuchElementException e) {
					// just swallow it because that just means the form doesn't have
					// the VisitDate data element.
					// but this is better than doing our own checks because of DRY
				}
			}
		}
	}
	
	/**
	 * Determines if the user is a Query Tool Admin or a global Admin
	 * 
	 * @return true if the user is an admin, otherwise false
	 */
	private boolean isUserAdmin() {
		Authentication auth = (SecurityContextHolder.getContext().getAuthentication());
		Collection<GrantedAuthority> aud = ((AccountUserDetails) auth.getPrincipal()).getAuthorities();
		for (GrantedAuthority authority : aud) {
			if (authority.toString().equals(ROLE_ADMIN) || authority.toString().equals(ROLE_QUERY_ADMIN)) {
				return true;
			}
		}
		return false;
	}
}
