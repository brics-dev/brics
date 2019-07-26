package gov.nih.tbi.guid.portal;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.WebApplicationException;

import org.apache.http.entity.ContentType;
import org.apache.log4j.Logger;
import org.apache.struts2.ServletActionContext;

import com.google.gson.JsonSyntaxException;

import gov.nih.tbi.PortalConstants;
import gov.nih.tbi.commons.portal.BaseAction;
import gov.nih.tbi.guid.exception.InvalidJwtException;
import gov.nih.tbi.guid.model.GuidJwt;

public class GuidAction extends BaseAction {

	private static final long serialVersionUID = -6423771967338479950L;
	private static final Logger logger = Logger.getLogger(GuidAction.class);

	private String selectedUser;
	
	private int noOfpseduGuid;
	
	private List<String> userNameList = new ArrayList<String>();


	public String getSelectedUser() {

		return selectedUser;
	}

	public void setSelectedUser(String selectedUser) {

		this.selectedUser = selectedUser;
	}

	public int getNoOfpseduGuid() {

		return noOfpseduGuid;
	}

	public void setNoOfpseduGuid(int noOfpseduGuid) {

		this.noOfpseduGuid = noOfpseduGuid;
	}

	public List<String> getUserNameList() {

		return userNameList;
	}

	public void setUserNameList(List<String> userNameList) {

		this.userNameList = userNameList;
	}

	/**
	 * Returns true if namespace is 'guidAdmin'
	 */
	public boolean getInAdmin() {

		return PortalConstants.NAMESPACE_GUID_ADMIN.equals(getNameSpace());
	}

	/******************************************************************************************************/

	/**
	 * Action for list of guids
	 * 
	 * @return
	 */
	public String list() {
		return PortalConstants.ACTION_LIST;
	}

	/**
	 * Action for the landing page
	 * 
	 * @return
	 */
	public String landing() {

		return PortalConstants.ACTION_LANDING;
	}

	/**
	 * Action for the landing page
	 * 
	 * @return
	 */
	public String create() {

		try {
			GuidJwt jwt = guidServerAuthUtil.getUserJwt(sessionAccount.getAccount());
			Cookie jwtCookie = new Cookie("sessToken", jwt.toString());
			getResponse().addCookie(jwtCookie);
		} catch (JsonSyntaxException | WebApplicationException | InvalidJwtException e) {
			logger.error(
					"Failed to introduce user " + sessionAccount.getAccount().getUserName() + " to the GUID Server", e);
		}

		return PortalConstants.ACTION_CREATE;
	}

	/**
	 * Action for launching the guid tool
	 */
	public String launch() {

		return PortalConstants.ACTION_LAUNCH;
	}

	public String getJwt() {

		HttpServletResponse response = ServletActionContext.getResponse();
		try {
			response.setContentType(ContentType.TEXT_PLAIN.toString());
			response.getWriter().write(getGuidJwt());
		} catch (Exception e) {
			logger.error("Couldn't get a JWT. Cause: " + e.getMessage());

			try {
				response.sendError(401);
			} catch (IOException f) {
				// can't do anything if we can't write out
				return null;
			}
		}
		return null;
	}

	public String guidButton() {

		return "button";
	}

	public String getGuidWsUrl() {
		return modulesConstants.getModulesGTServiceURL();
	}

}
