package gov.nih.nichd.ctdb.protocol.action;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.NoRouteToHostException;
import java.net.UnknownHostException;
import java.util.Date;

import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.apache.struts2.ServletActionContext;

import gov.nih.nichd.ctdb.common.BaseAction;
import gov.nih.nichd.ctdb.common.CtdbConstants;
import gov.nih.nichd.ctdb.common.CtdbException;
import gov.nih.nichd.ctdb.common.StrutsConstants;
import gov.nih.nichd.ctdb.common.navigation.LeftNavController;
import gov.nih.nichd.ctdb.common.util.Utils;
import gov.nih.nichd.ctdb.protocol.domain.Protocol;
import gov.nih.nichd.ctdb.protocol.domain.ProtocolClosingOut;
import gov.nih.nichd.ctdb.protocol.manager.ProtocolManager;
import gov.nih.nichd.ctdb.response.util.DataCollectionUtils;
import gov.nih.nichd.ctdb.security.domain.User;
import gov.nih.nichd.ctdb.security.util.SecuritySessionUtil;
import gov.nih.tbi.account.model.hibernate.Account;
import gov.nih.tbi.commons.ws.HashMethods;

public class ProtocolCloseoutAction extends BaseAction {

	private static final long serialVersionUID = 3647792394987863396L;
	private static final Logger logger = Logger.getLogger(ProtocolCloseoutAction.class);

	private String userFullName;
	private String userPassword;

	public String execute() {
		buildLeftNav(LeftNavController.LEFTNAV_STUDY_CLOSE_OUT);
		User user = getUser();
		String userFullName = user.getFirstName() + " " + user.getLastName();
		setUserFullName(userFullName);
		checkProtocolClosed();
		return SUCCESS;
	}

	public void checkProtocolClosed() {
		boolean closedout = false;
		Protocol protocol = null;
		protocol = (Protocol) session.get(CtdbConstants.CURRENT_PROTOCOL_SESSION_KEY);

		try {
			if (protocol != null) {
				ProtocolManager pm = new ProtocolManager();
				closedout = pm.checkProtocolClosed(protocol.getId());
				request.setAttribute("protocolclosedout", closedout);
			}
		} catch (CtdbException e) {
			logger.error("Error in getting protocol close out detail from database " + e);
			e.printStackTrace();
		}

	}

	public void digitalSignature() throws IOException, CtdbException {
		HttpServletResponse response = ServletActionContext.getResponse();
		PrintWriter out = response.getWriter();
		User user = getUser();
		boolean webServiceReachAble = DataCollectionUtils.isWebServiceUp(request, user);

		if (!webServiceReachAble) {
			out.print("bricsAccountWSnotReacheable");
			out.flush();
			return;
		}

		String bricsPassword;
		try {
			Account bricsAccount = SecuritySessionUtil.getBricsAccountInformation(request, user);
			// String saltedPassword = dataEntryForm.getUserPassword();
			String saltedPassword = SecuritySessionUtil.getBricsAccountSalt(bricsAccount) + getUserPassword();

			String signedPassword = HashMethods.convertFromByte(SecuritySessionUtil.sha256(saltedPassword));
			bricsPassword = SecuritySessionUtil.getBricsAccountPassword(bricsAccount);

			logger.info(DataCollectionUtils.getUserIdSessionIdString(request) + "Digital Signature Validation"
					+ user.getId());

			if (signedPassword.equals(bricsPassword)) {
				logger.info("Digital Signature Validation passed valiadtion.");
				// response.setStatus(200);//send success code
				out.print("passwordValidationPassed");
				out.flush();
				return;
			} else if (Utils.isBlank(getUserPassword())) {
				logger.info(DataCollectionUtils.getUserIdSessionIdString(request)
						+ "Digital Signature Validation failed valiadtion.Password is blank");
				out.print("blankPassword");
				out.flush();
				return;
			} else {
				logger.info(DataCollectionUtils.getUserIdSessionIdString(request)
						+ "Digital Signature Validation failed valiadtion.Password mismatch error");
				out.print("mismatchPassword");
				out.flush();
				return;
			}
		} catch (UnknownHostException ue) {
			logger.error(DataCollectionUtils.getUserIdSessionIdString(request)
					+ "An error occurred in getting Brics account info.", ue);
			out.print("bricsAccountWSnotReacheable");
			out.flush();
			return;
		} catch (CtdbException ce) {
			logger.error(DataCollectionUtils.getUserIdSessionIdString(request)
					+ "An error occurred in getting Brics account info.", ce);
			out.print("bricsAccountWSnotReacheable");
			out.flush();
			return;
		} catch (NoRouteToHostException ne) {
			logger.error(DataCollectionUtils.getUserIdSessionIdString(request)
					+ "An error occurred in getting Brics account info.", ne);
			out.print("bricsAccountWSnotReacheable");
			out.flush();
			return;
		}
	}

	public String saveProtocolCloseout() {

		Protocol protocol = null;
		try {
			protocol = (Protocol) session.get(CtdbConstants.CURRENT_PROTOCOL_SESSION_KEY);

			if (protocol == null) {
				return StrutsConstants.SELECTPROTOCOL;
			}

			ProtocolClosingOut pco = new ProtocolClosingOut();
			User user = getUser();

			if (user != null) {
				pco.setClosingUserId(user.getId());
				pco.setClosingBricsUserId(user.getBricsUserId());
				pco.setProtocolId(protocol.getId());
				pco.setBricsStudyId(protocol.getBricsStudyId());
				pco.setClosingOutDate(new Date());

				ProtocolManager pm = new ProtocolManager();
				pm.saveProtocolClosingout(pco);
			}

		} catch (Exception e) {
			logger.error("Failed to save the protocol close out.", e);
		}

		return SUCCESS;
	}

	public String getUserPassword() {
		return userPassword;
	}

	public void setUserPassword(String userPassword) {
		this.userPassword = userPassword;
	}

	public String getUserFullName() {
		return userFullName;
	}

	public void setUserFullName(String userFullName) {
		this.userFullName = userFullName;
	}
}
