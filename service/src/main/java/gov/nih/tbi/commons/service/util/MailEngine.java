package gov.nih.tbi.commons.service.util;

import java.io.Serializable;
import java.util.Arrays;

import javax.activation.DataSource;
import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.MailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;

import gov.nih.tbi.CoreConstants;
import gov.nih.tbi.ModulesConstants;

/**
 * Mail Engine class creates a simple way for the service layer to send an email.
 * 
 * @author Andrew Johnson
 * 
 */
@Component
public class MailEngine implements Serializable {
	static Logger logger = Logger.getLogger(MailEngine.class);

	private static final long serialVersionUID = 7533221379126705380L;

	@Autowired
	protected ModulesConstants modulesConstants;

	private MailSender mailSender;
	private String defaultFrom;

	public MailEngine(MailSender mailSender) {

		this.mailSender = mailSender;
	}

	/**
	 * Send a basic email, explicitly call out the from email address. Content type of the message will be html
	 * 
	 * @param subject
	 * @param message
	 * @param from (null implies default from address, most common case)
	 * @param to
	 * @throws MessagingException
	 */
	public void sendMail(String subject, String message, String from, String... to) throws MessagingException {
		
		subject += modulesConstants.getEnvironmentFlag();
		
		logger.debug("----- Sending Email -----");
		logger.debug("To: " + (to == null ? "<To field is null>" : Arrays.toString(to)));
		logger.debug("From: " + (from == null ? "<From field is null>" : from));
		logger.debug("Subject: " + (subject == null ? "<Subject field is null>" : subject));
		logger.debug("Message: " + (message == null ? "<Message field is null>" : message));
		logger.debug("-------------------------");
		MimeMessage mimeMessage = ((JavaMailSenderImpl) mailSender).createMimeMessage();

		MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true);

		if (to == null || to.length == 0) {
			throw new IllegalArgumentException("TO must have at least one address");
		}
		
		for(String toAddress:to) {
			if(toAddress == null || toAddress.isEmpty()) {
				throw new IllegalArgumentException("Cannot send email to null or empty address(s).");
			}
		}

		helper.setTo(to);
		if (from == null || CoreConstants.EMPTY_STRING.equals(from.trim())) {
			this.defaultFrom = modulesConstants.getModulesOrgEmail();
			helper.setFrom(defaultFrom);
		} else {
			helper.setFrom(from);
		}
		helper.setText(message, true);
		helper.setSubject(subject);
		

		((JavaMailSenderImpl) mailSender).send(mimeMessage);
	}
	
	public void sendMailWithAttachment(String subject, String message, DataSource source, String fileName, String from, String... to) throws MessagingException {
		
		subject += modulesConstants.getEnvironmentFlag();
		
		logger.debug("----- Sending Email -----");
		logger.debug("To: " + (to == null ? "<To field is null>" : Arrays.toString(to)));
		logger.debug("From: " + (from == null ? "<From field is null>" : from));
		logger.debug("Subject: " + (subject == null ? "<Subject field is null>" : subject));
		logger.debug("Message: " + (message == null ? "<Message field is null>" : message));
		logger.debug("-------------------------");
		MimeMessage mimeMessage = ((JavaMailSenderImpl) mailSender).createMimeMessage();

		MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true);

		if (to == null || to.length == 0) {
			throw new IllegalArgumentException("TO must have at least one address");
		}
		
		for(String toAddress:to) {
			if(toAddress == null || toAddress.isEmpty()) {
				throw new IllegalArgumentException("Cannot send email to null or empty address(s).");
			}
		}

		helper.setTo(to);
		if (from == null || CoreConstants.EMPTY_STRING.equals(from.trim())) {
			this.defaultFrom = modulesConstants.getModulesOrgEmail();
			helper.setFrom(defaultFrom);
		} else {
			helper.setFrom(from);
		}
		helper.setText(message, true);
		helper.setSubject(subject);
		helper.addAttachment(fileName, source);
		

		((JavaMailSenderImpl) mailSender).send(mimeMessage);
	}
	
	/**
	 * Send a basic email with support for cc email address. Content type of the message will be html
	 * 
	 * @param subject
	 * @param message
	 * @param from (null implies default from address, most common case)
	 * @param to - an array of to email addresses
	 * @param cc - an array of cc email addresses
	 * @throws MessagingException
	 */
	public void sendMail(String subject, String message, String from, String[] to, String[] cc) throws MessagingException {
		
		subject += modulesConstants.getEnvironmentFlag();
		
		logger.debug("----- Sending Email -----");
		logger.debug("To: " + (to == null ? "<To field is null>" : Arrays.toString(to)));
		logger.debug("From: " + (from == null ? "<From field is null>" : from));
		logger.debug("Subject: " + (subject == null ? "<Subject field is null>" : subject));
		logger.debug("Message: " + (message == null ? "<Message field is null>" : message));
		logger.debug("-------------------------");
		MimeMessage mimeMessage = ((JavaMailSenderImpl) mailSender).createMimeMessage();

		MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true);

		if (to == null || to.length == 0) {
			throw new IllegalArgumentException("TO must have at least one address");
		}
		
		for(String toAddress:to) {
			if(toAddress == null || toAddress.isEmpty()) {
				throw new IllegalArgumentException("Cannot send email to null or empty address(s).");
			}
		}

		helper.setTo(to);
		
		if (cc != null && cc.length > 0) {
			helper.setCc(cc);
		}
		
		if (from == null || CoreConstants.EMPTY_STRING.equals(from.trim())) {
			this.defaultFrom = modulesConstants.getModulesOrgEmail();
			helper.setFrom(defaultFrom);
		} else {
			helper.setFrom(from);
		}
		helper.setText(message, true);
		helper.setSubject(subject);
		

		((JavaMailSenderImpl) mailSender).send(mimeMessage);
	}


}
