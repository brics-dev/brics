package gov.nih.tbi.esign.portal;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Collection;

import org.apache.log4j.Logger;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import com.jcraft.jsch.JSchException;

import gov.nih.tbi.PortalConstants;
import gov.nih.tbi.account.model.AccountSignatureForm;
import gov.nih.tbi.account.model.hibernate.Account;
import gov.nih.tbi.account.model.hibernate.ElectronicSignature;
import gov.nih.tbi.account.service.complex.AccountDetailService;
import gov.nih.tbi.account.util.ESignDocGenerator;
import gov.nih.tbi.commons.model.BRICSTimeDateUtil;
import gov.nih.tbi.commons.model.hibernate.User;
import gov.nih.tbi.commons.portal.BaseAction;
import gov.nih.tbi.commons.service.ServiceConstants;
import gov.nih.tbi.repository.model.hibernate.UserFile;

public class ESignAction extends BaseAction {

	private static final long serialVersionUID = 3798547506641413034L;
	private static Logger logger = Logger.getLogger(ESignAction.class);
	
	private String firstName;
	private String middleName;
	private String lastName;
	
	private AccountSignatureForm accountSignatureForm;
	private Account currentAccount;
	
	public String validateName() {
		return PortalConstants.ACTION_VIEW;
	}
	
	public String editName() {
		User user = getCurrentAccount().getUser();
		this.setFirstName(user.getFirstName());
		this.setMiddleName(user.getMiddleName());
		this.setLastName(user.getLastName());
		return PortalConstants.ACTION_EDIT;
	}
	
	public String esign() {
		return PortalConstants.ACTION_SUCCESS;
	}
	
	public String saveNameChanges() {
		User user = getCurrentAccount().getUser();
		user.setFirstName(firstName);
		user.setMiddleName(middleName);
		user.setLastName(lastName);
		
		user = accountManager.saveUserChanges(user);
		getCurrentAccount().setUser(user);
		return PortalConstants.ACTION_SUCCESS;
	}
	
	
	public String submit() {
		if (!currentAccount.hasBricsESignature()) {
			if (accountSignatureForm != null) {
				accountSignatureForm.adapt(currentAccount);
			}

			currentAccount = accountManager.saveAccount(currentAccount);
			this.generateESignatureDoc();
			logger.info("E-Signature saved for user " + currentAccount.getUserName());
		}

		// Update authorities in the session
		SecurityContext securityContext = SecurityContextHolder.getContext();
		Authentication auth = securityContext.getAuthentication();
		Collection<GrantedAuthority> updatedAuthorities = AccountDetailService.getAuthorities(currentAccount);
		
		Authentication newAuth = new UsernamePasswordAuthenticationToken(
				auth.getPrincipal(), auth.getCredentials(), updatedAuthorities);
        securityContext.setAuthentication(newAuth);
        
		return PortalConstants.ACTION_SUCCESS;
	}

	
	private void generateESignatureDoc() {
		ElectronicSignature es = currentAccount.getBricsESignature();
		ESignDocGenerator generator = new ESignDocGenerator(es);
		UserFile esigUserFile = null;
		
		try {
			byte[] fileBytes = generator.generateESignature(this.getOrgName().toLowerCase());

			if (fileBytes != null) {
				esigUserFile = new UserFile();
				String dateStr = BRICSTimeDateUtil.formatDate(es.getSignatureDate());
				String fileName = currentAccount.getUserName() + "_esig_" + dateStr + ".pdf";
				
				esigUserFile = repositoryManager.uploadFile(currentAccount.getUser().getId(), fileBytes, fileName, "",
						ServiceConstants.FILE_TYPE_ELECTRONIC_SIGNATURE, es.getSignatureDate());
			}
		} catch (IOException ioe) {
			ioe.printStackTrace();
		} catch (JSchException je) {
			je.printStackTrace();
		}
		
		if (esigUserFile != null) {
			es.setSignatureFile(esigUserFile);
			es = accountManager.saveElectronicSignature(es);
		}
	} 


	public Account getCurrentAccount() {
		if (currentAccount == null) {
			currentAccount = this.getAccount();
		}
		return currentAccount;
	}

	public void setCurrentAccount(Account currentAccount) {
		this.currentAccount = currentAccount;
	}
	

	public String getFirstName() {
		return firstName;
	}

	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}

	public String getMiddleName() {
		return middleName;
	}

	public void setMiddleName(String middleName) {
		this.middleName = middleName;
	}

	public String getLastName() {
		return lastName;
	}

	public void setLastName(String lastName) {
		this.lastName = lastName;
	}

	public AccountSignatureForm getAccountSignatureForm() {
		return accountSignatureForm;
	}

	public void setAccountSignatureForm(AccountSignatureForm accountSignatureForm) {
		this.accountSignatureForm = accountSignatureForm;
	}

}
