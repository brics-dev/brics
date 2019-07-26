
package gov.nih.tbi.commons.service;

import gov.nih.tbi.account.model.hibernate.Account;
import gov.nih.tbi.commons.model.RoleType;

import javax.mail.MessagingException;

/**
 * Base Manager
 * 
 * @author Nimesh Patel
 * 
 */
public interface BaseManager
{

    /**
     * Sends an email
     * 
     * @param subject
     * @param htmlMessage
     * @param from
     * @param to
     * @throws MessagingException
     */
    public void sendMail(String subject, String htmlMessage, String from, String... to) throws MessagingException;

    /**
     * Send an email to the user associated with the account with the given subject and body.
     * 
     * @param account
     *            of the recipient
     * @param subject
     *            of the email message
     * @param messageText
     *            of the email message
     * 
     * @throws MessagingException
     */
    public void sendEmail(Account account, String subject, String messageText) throws MessagingException;

    /**
     * Send an email to the user associated with the account with the given subject and body from the given email
     * address.
     * 
     * @param account
     * @param subject
     * @param messageText
     * @param from
     * @throws MessagingException
     */
    public void sendEmail(Account account, String subject, String messageText, String from) throws MessagingException;

    /**
     * Indicates if the account has been granted the role requested (no database hit)
     * 
     * @param account
     * @param role
     * @return
     */
    public Boolean hasRole(Account account, RoleType role);

    public String getHash2(Account account);

    /**
     * Escape the characters '_' '%' and '\' with '\' character for use in a hibernate Restrictions.iLike() statement.
     * 
     * @param input
     *            : The string to be escaped, can be null
     * @return
     */
    public String escapeForILike(String input);

}
