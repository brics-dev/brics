package gov.nih.cit.brics.file.mvc.controller;

import java.util.NoSuchElementException;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import gov.nih.cit.brics.file.data.entity.UserToken;
import gov.nih.cit.brics.file.data.repository.meta.AccountRepository;
import gov.nih.cit.brics.file.security.jwt.EurekaTokenProvider;
import gov.nih.cit.brics.file.service.FilePermissionsService;
import gov.nih.tbi.account.model.hibernate.Account;
import gov.nih.tbi.file.model.hibernate.BricsFile;

public class BaseFileRestController {
	private static final Logger logger = LoggerFactory.getLogger(BaseFileRestController.class);

	@Autowired
	protected EurekaTokenProvider tokenProvider;

	@Autowired
	protected AccountRepository accountRepository;

	@Autowired
	protected FilePermissionsService filePermissionsService;

	private UserToken userToken;
	private Account account;

	public BaseFileRestController() {}

	/**
	 * An initialization method for rest controller beans. After needed dependencies are autowired.
	 */
	@PostConstruct
	public void init() {
		this.userToken = generateUserToken();
		this.account = generateAccount();
	}

	/**
	 * Generate a new user's access token, in UserToken form, from the current Authentication object.
	 * 
	 * @return UserToken generated from the Authentication in the Security Context.
	 */
	public UserToken generateUserToken() {
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		String token = (String) auth.getCredentials();
		return tokenProvider.parseToken(token);
	}

	/**
	 * Generate a new user's Account from the requesting JWT.
	 * 
	 * @return The account object referenced by the username from the JWT token, or null if there was not match in the
	 *         system.
	 */
	public Account generateAccount() {
		UserToken token = getUserToken();
		Account account = null;

		try {
			account = accountRepository.findByUserName(token.getUsername()).get();
		} catch (NoSuchElementException e) {
			logger.warn("Couldn't find a user in the database matching the username: {}.", token.getUsername());
		}

		return account;
	}

	/**
	 * Verify that the user as the correct permissions to the given BricsFile object.
	 * 
	 * @param bricsFile - The BricFile that the current user is trying to access.
	 * @return True if and only if the current user can access the given BricsFile object.
	 */
	boolean checkUserPermissions(BricsFile bricsFile) {
		boolean hasAccess = false;

		// Check permissions by the file category.
		switch (bricsFile.getFileCategory()) {
			case DATASET:
				hasAccess = filePermissionsService.validateDataSetAccess(bricsFile, account, userToken);
				break;
			case DOWNLOAD:
				hasAccess = filePermissionsService.validateDownloadAccess(bricsFile, account, userToken);
				break;
			case ELECTRONIC_SIGNATURE:
				hasAccess = filePermissionsService.validateElectronicSignatureAccess(bricsFile, account, userToken);
				break;
			case META_STUDY:
				hasAccess = filePermissionsService.validateMetaStudyAccess(bricsFile, account, userToken);
				break;
			case ORDER_MANAGER:
				hasAccess = filePermissionsService.validateOrderManagerAccess(bricsFile, account, userToken);
				break;
			case QUESTION_DOCUMENT:
				hasAccess = filePermissionsService.validateEformAccess(bricsFile, account, userToken);
				break;
			case RESEARCH_MANAGEMENT:
				hasAccess = filePermissionsService.validateStudyAccess(bricsFile, account, userToken);
				break;
			case STUDY:
				hasAccess = filePermissionsService.validateStudyAccess(bricsFile, account, userToken);
				break;
			case DATA_ELEMENT_SUPPORTING_DOCUMENTATION:
				hasAccess = filePermissionsService.validateDataElementAccess(bricsFile, account, userToken);
				break;
			case FORM_STRUCTURE_SUPPORTING_DOCUMENTATION:
				hasAccess = filePermissionsService.validateFormStructureAccess(bricsFile, account, userToken);
				break;
			case META_STUDY_SUPPORTING_DOCUMENTATION:
				hasAccess = filePermissionsService.validateMetaStudyAccess(bricsFile, account, userToken);
				break;
			case STUDY_SUPPORTING_DOCUMENTATION:
				hasAccess = filePermissionsService.validateStudyAccess(bricsFile, account, userToken);
				break;
			default:
				logger.error("The file category, {}, is not valid.", bricsFile.getFileCategory());
				break;
		}

		return hasAccess;
	}

	public UserToken getUserToken() {
		return userToken;
	}

	public void setUserToken(UserToken userToken) {
		if (userToken != null) {
			this.userToken = userToken;
		} else {
			this.userToken = generateUserToken();
		}
	}

	public Account getAccount() {
		return account;
	}

	public void setAccount(Account account) {
		if (account != null) {
			this.account = account;
		} else {
			this.account = generateAccount();
		}
	}

}
