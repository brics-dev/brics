package gov.nih.tbi.twoFa.portal;


import java.text.MessageFormat;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.Random;
import java.util.ResourceBundle;

import javax.mail.MessagingException;
import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSource;
import org.springframework.security.cas.authentication.CasAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;

import gov.nih.tbi.PortalConstants;
import gov.nih.tbi.account.model.hibernate.Account;
import gov.nih.tbi.account.model.hibernate.TwoFactorAuthentication;
import gov.nih.tbi.account.service.complex.AccountDetailService;
import gov.nih.tbi.commons.portal.BaseAction;
import gov.nih.tbi.commons.service.AccountManager;
import gov.nih.tbi.commons.service.util.MailEngine;

public class TwoFaAction extends BaseAction {

	private static final long serialVersionUID = 3798547506641413034L;
	private static Logger logger = Logger.getLogger(TwoFaAction.class);

	private String twoFaCode = "";
	private Date twoFaDate;
	private Date twoFaExpirationDate;
	private Account account;
	@Value("#{casSecurityProperties['cas.client.key']}")
	private String[] key;
	@Autowired
	private MailEngine mailEngine;
	@Autowired
	protected MessageSource messageSource;
	@Autowired
	protected AccountManager accountManager;

	private static final String TWO_FA_CODE = "twoFaCode";
	public static final String MODULES_TWO_FA_EMAIL_SUBJECT_PROPERTY = "twoFaEmail.subject";
	public static final String MODULES_TWO_FA_EMAIL_BODY_PROPERTY = "twoFaEmail.body";

	HttpServletRequest request = this.getRequest();

	public String submit() {
		SecurityContext securityContext = SecurityContextHolder.getContext();
		CasAuthenticationToken oldAuth = (CasAuthenticationToken) securityContext.getAuthentication();
		Account account = this.getCurrentAccount();

		String savedUnexpiredTwoFaCode = String.valueOf(account.getTwoFactorAuthentication().getTwoFaCode());
		String twoFaCode = request.getParameter(TWO_FA_CODE);

		logger.debug("savedUnexpiredTwoFaCode*********************    "+savedUnexpiredTwoFaCode);
		logger.debug("twoFaCode***********************************    "+twoFaCode);
		TwoFactorAuthentication twoFa = account.getTwoFactorAuthentication();
		if(twoFaCode != null && twoFaCode.equals(savedUnexpiredTwoFaCode)
				&& Instant.now().isBefore(twoFa.getTwoFaExpirationDate().toInstant())) {
			
			Collection<GrantedAuthority> updatedAuthorities = AccountDetailService.getAuthorities(account);
			if (key == null || key.length < 1) {
				throw new RuntimeException("Property cas.client.key was not set.");
			}
			String caskey = key[0];
			CasAuthenticationToken newAuth = new CasAuthenticationToken(caskey, oldAuth.getPrincipal(),
					oldAuth.getCredentials(), updatedAuthorities, (UserDetails) oldAuth.getPrincipal(),
					oldAuth.getAssertion());
			newAuth.setDetails(oldAuth.getDetails());
			securityContext.setAuthentication(newAuth);

			/*if two factor authentication is successful, expire the twoFa code*/
			Date currentDateTime = new Date();
			twoFa.setTwoFaExpirationDate(currentDateTime);
			HashSet<TwoFactorAuthentication> twoFaSet = new HashSet<TwoFactorAuthentication>();
			twoFaSet.add(twoFa);
			account.setTwoFactorAuthentications(twoFaSet);
			accountManager.saveAccount(account);

			//if account does not have esign, redirect to esign page
			if (account.getBricsESignature() == null) {
				return PortalConstants.ACTION_ESIGN;
			} 	
			
			return PortalConstants.ACTION_SUCCESS;
			
		} else {
			addFieldError(TWO_FA_CODE,"Invalid Authentication Code");

			return PortalConstants.ACTION_INVALID;
		}
	}

	public Account getCurrentAccount() {
		if (account == null) {
			account = this.getAccount();
		}
		return account;
	}

	public void setCurrentAccount(Account currentAccount) {
		this.account = currentAccount;
	}


	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getTwoFaCode() {
		return twoFaCode;
	}

	public void setTwoFaCode(String twoFaCode) {
		this.twoFaCode = twoFaCode;
	}

	public Date getTwoFaDate() {
		return twoFaDate;
	}

	public void setTwoFaDate(Date twoFaDate) {
		this.twoFaDate = twoFaDate;
	}

	public Date getTwoFaExpirationDate() {
		return twoFaExpirationDate;
	}

	public void setTwoFaExpirationDate(Date twoFaExpirationDate) {
		this.twoFaExpirationDate = twoFaExpirationDate;
	}

	public void validate() {
		if (twoFaCode == null || twoFaCode.trim().equals("")) {
			addFieldError(TWO_FA_CODE,"The Two Factor Authentication Code is required");
		}
	}
	public String input() {
		createTwaFaCode(this.getCurrentAccount());
		return PortalConstants.ACTION_INPUT;
	}


	public void createTwaFaCode(Account account)
	{
		Long twoFaCodeExpiresInMinutes = 0L;
		TwoFactorAuthentication twoFa = account.getTwoFactorAuthentication();
		String accountTwoFaCode = twoFa != null ? twoFa.getTwoFaCode() : "";
		String twoFaRandomCode = generateTwoFaRandomCode();
		twoFaCodeExpiresInMinutes = modulesConstants.getTwoFaCodeExpiresInMinutes();
		Date twoFaDate = new Date();
		Date twoFaExpirationDate = Date.from(Instant.now().plus(twoFaCodeExpiresInMinutes, ChronoUnit.MINUTES));
		try {
			if (account != null) {				
				if(twoFa != null && accountTwoFaCode != null) {					
					if(Instant.now().isAfter(twoFa.getTwoFaExpirationDate().toInstant())) {						
						twoFa = updateTwoFactorAuthen(twoFa, twoFaRandomCode, twoFaDate, twoFaExpirationDate, account);
						accountManager.saveTwoFactorAuthentication(twoFa);
					} else {
						twoFaRandomCode = accountTwoFaCode;
					}
				} else {
					twoFa = new TwoFactorAuthentication(twoFaRandomCode, twoFaDate, twoFaExpirationDate, account);
					account.addTwoFactorAuthentication(twoFa);
					accountManager.saveAccount(account);
				}
				this.setCurrentAccount(account);
			} 
			// Update authorities in the session
			SecurityContext securityContext = SecurityContextHolder.getContext();
			CasAuthenticationToken oldAuth = (CasAuthenticationToken) securityContext.getAuthentication();
			Collection<GrantedAuthority> updatedAuthorities = AccountDetailService.getTwoFaAuthorities(account);
			if (key == null || key.length < 1) {
				throw new RuntimeException("Property cas.client.key was not set.");
			}
			String caskey = key[0];
			CasAuthenticationToken newAuth = new CasAuthenticationToken(caskey, oldAuth.getPrincipal(),
					oldAuth.getCredentials(), updatedAuthorities, (UserDetails) oldAuth.getPrincipal(),
					oldAuth.getAssertion());
			newAuth.setDetails(oldAuth.getDetails());
			securityContext.setAuthentication(newAuth);
		}catch (Exception e) {
			e.printStackTrace();
		} 
		/**send 2 FA email*/
		try {
			sendTwoFaCode(account, twoFaRandomCode, twoFaCodeExpiresInMinutes);
		}
		catch (Exception e) {
			logger.error("Exception occurred while trying to send Two FA Code", e);
		}
	}

	/**
	 * Using the specified account, create and send the message to the account's email
	 * 
	 * @param account
	 */
	private void sendTwoFaCode(Account account, String twoFaRandomCode, Long twoFaCodeExpiresInMinutes)
	{
		String orgName = this.getOrgName();
		String orgNameEmaiAddress = this.getOrgEmail();
		/*
		 * Retrieve the email template and send email.
		 */	
		ResourceBundle mailProperties = ResourceBundle.getBundle("mail");
		String subject = MessageFormat.format(mailProperties.getString(TwoFaAction.MODULES_TWO_FA_EMAIL_SUBJECT_PROPERTY), orgName);			
		String mailBody = MessageFormat.format(mailProperties.getString(TwoFaAction.MODULES_TWO_FA_EMAIL_BODY_PROPERTY), 
				twoFaRandomCode, // {0}
				twoFaCodeExpiresInMinutes, // {1}
				orgNameEmaiAddress, // {2}
				orgName // {3}
				);
		/*
		 * send a mail to the user
		 */
		if (logger.isDebugEnabled() == true)
		{
			logger.debug("sending two factor authentication email to the address: " + account.getUser().getEmail() + " of account: " + account);
			logger.debug("mail title: " + subject);
			logger.debug("mail body:\n" + mailBody);
		}
		try
		{
			if (logger.isDebugEnabled() == true)
			{
				logger.debug("sending mail to user: " + account.getUserName());
			}
			this.sendEmail(subject, mailBody, orgNameEmaiAddress, account.getUser().getEmail());
			System.out.println("******" + account.getUser().getEmail());
		}
		catch (MessagingException e)
		{
			logger.error("Exception occurred while trying to send password expiration email in scheduled job", e);
		}
	}

	protected void sendEmail(String subject, String body, String emailFrom, String emailReceiver) throws MessagingException
	{
		try {
			this.mailEngine.sendMail(subject, body, emailFrom, emailReceiver);
		} catch (MessagingException e) {
			logger.error("There was an exception in the BricsAuthenticationSuccessHandler sendEmail++++++++++++++++"
					+ e.getLocalizedMessage());
			e.printStackTrace();
			throw e;
		}
	}

	protected String generateTwoFaRandomCode()
	{
		String twoFaRandomCode = "";
		Random r = new Random();
		twoFaRandomCode = Integer.toString(r.nextInt((999999 - 000000) + 1)) ;
		return twoFaRandomCode;
	}    

	private TwoFactorAuthentication updateTwoFactorAuthen(TwoFactorAuthentication twoFa, String twoFaRandomCode, Date twoFaDate, Date twoFaExpirationDate, Account account)
	{
		twoFa.setTwoFaCode(twoFaRandomCode);
		twoFa.setTwoFaDate(twoFaDate);
		twoFa.setTwoFaExpirationDate(twoFaExpirationDate);
		twoFa.setAccount(account);
		return twoFa;
	} 
	
}
