
package gov.nih.brics.job;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.mail.MessagingException;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import gov.nih.tbi.ModelConstants;
import gov.nih.tbi.ModulesConstants;
import gov.nih.tbi.account.model.hibernate.Account;
import gov.nih.tbi.account.model.hibernate.AccountRole;
import gov.nih.tbi.commons.model.BRICSTimeDateUtil;
import gov.nih.tbi.commons.service.AccountManager;
import gov.nih.tbi.commons.service.HibernateManager;

/**
 * Email job for sending out email alerts when a user's account role is near expiration
 * 
 * @author Francis Chen
 */
@Component
@Scope("singleton")
public class AccountExpirationEmailJob extends EmailJobs
{

    @Autowired
    private AccountManager accountManager;
    
    @Autowired
	private HibernateManager hibernateManager;

    private static Logger log = Logger.getLogger(AccountExpirationEmailJob.class);

    // list of the days away from expiration date the emails should be sent out.
    // Don't try to modify this list on runtime
    private static List<Integer> daysFromExpirationToEmail;

    private static List<Integer> daysFromExpirationToEmailOPs;

    /**
     * Initializes the array that stores the days before expiration we will send emails out
     */
    static
    {
        ArrayList<Integer> modifiableDaysFromExpirationToEmail = new ArrayList<Integer>();

        // set the days before expiration the emails should be sent out here!
        modifiableDaysFromExpirationToEmail.add(30);
        modifiableDaysFromExpirationToEmail.add(14);
        modifiableDaysFromExpirationToEmail.add(7);
        modifiableDaysFromExpirationToEmail.add(6);
        modifiableDaysFromExpirationToEmail.add(5);
        modifiableDaysFromExpirationToEmail.add(4);
        modifiableDaysFromExpirationToEmail.add(3);
        modifiableDaysFromExpirationToEmail.add(2);
        modifiableDaysFromExpirationToEmail.add(1);

        daysFromExpirationToEmail = Collections.unmodifiableList(modifiableDaysFromExpirationToEmail);

        ArrayList<Integer> modifiableDaysFromExpirationToEmailOPs = new ArrayList<Integer>();

        // set the days before expiration the emails should be sent out to OPs here!
        modifiableDaysFromExpirationToEmailOPs.add(14);
        modifiableDaysFromExpirationToEmailOPs.add(7);
        modifiableDaysFromExpirationToEmailOPs.add(1);

        daysFromExpirationToEmailOPs = Collections.unmodifiableList(modifiableDaysFromExpirationToEmailOPs);
    }

    /**
     * Executes the email job
     */
    public void doJob()
    {
    	hibernateManager.clearMetaHibernateCache();
        List<Account> accounts = accountManager.getActiveAccounts();

        if (accounts == null)
        {
            log.error("Account list should not be null!");
            throw new NullPointerException("Account list should not be null!");
        }

        // see if we need to send out the email for each account
        for (Account account : accounts)
        {
            Map<AccountRole, Integer> roleToDaysFromExpirationMap = getRoleToDaysFromExpirationMap(account);
            Map<Integer, List<AccountRole>> expiringRoles = getExpiringRolesByDay(account);

            if (!expiringRoles.isEmpty()) // if there is a role expiring for this account
            {
                String orgName = modulesConstants.getModulesOrgName();

                // subject line of the email
                String mailTitle = messageSource.getMessage(ModulesConstants.RENEW_ACCOUNT_ROLE_SUBJECT_PROPERTY,
                        new Object[] { orgName }, "Application Name", null);

                // body of the email           
                String name = account.getUser().getFullName();
                String rolelist = getRoleList(roleToDaysFromExpirationMap, expiringRoles);
                String contentForFitbir = "";  //This is email content for Fitbir only      
                String supportPhoneNumber = modulesConstants.getModulesOrgPhone();
                String supportEmailAddress = modulesConstants.getModulesOrgEmail();
                
                if (orgName.indexOf("FITBIR") >= 0) {
                	contentForFitbir = messageSource.getMessage(ModulesConstants.RENEW_ACCOUNT_ROLE_CONTENT_FOR_FITBIR_PROPERTY, 
                    		null, null);
                }
                
                String mailBody = messageSource.getMessage(ModulesConstants.RENEW_ACCOUNT_ROLE_MESSAGE_BODY_PROPERTY, 
                		new Object[] { orgName, rolelist, contentForFitbir, supportEmailAddress, supportPhoneNumber, name }, null);

                List<String> emailAddresses = new ArrayList<String>();

                // add the user into the list of recipients
                emailAddresses.add(account.getUser().getEmail());

                boolean sendEmailToOps = false;

                // if a role expires on a certain day, email the OPs as well
                for (int dayToEmailOps : daysFromExpirationToEmailOPs)
                {
                    if (expiringRoles.keySet().contains(dayToEmailOps))
                    {
                        sendEmailToOps = true;
                    }
                }

                // add OPs email to the list
                if (sendEmailToOps)
                {
                    emailAddresses.addAll(modulesConstants.getModulesOrgEmailList());
                }

                // for each recipient, send out the email
                for (String emailAddress : emailAddresses)
                {
                    try
                    {
                        this.sendEmail(mailTitle, mailBody, emailAddress);
                    }
                    catch (MessagingException e)
                    {
                        log.error("Exception occurred while trying to send account expiration email in scheduled job",
                                e);
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    /**
     * Returns a hashmap of the days from expiration to a list of account role that are expiring
     * 
     * @param account
     * @return
     */
    private Map<Integer, List<AccountRole>> getExpiringRolesByDay(Account account)
    {

        Map<Integer, List<AccountRole>> expiringRoles = new HashMap<Integer, List<AccountRole>>();

        for (AccountRole role : account.getAccountRoleList())
        {
            int daysFromExpiration = getDaysAwayFromExpiration(role);

            if (daysFromExpirationToEmail.contains(daysFromExpiration))
            {
                List<AccountRole> rolesExpiring = expiringRoles.get(daysFromExpiration);

                if (rolesExpiring == null)
                {
                    rolesExpiring = new ArrayList<AccountRole>();
                    expiringRoles.put(daysFromExpiration, rolesExpiring);
                }

                rolesExpiring.add(role);
            }
        }

        return expiringRoles;
    }

    /**
     * Returns a hashmap of account roles and their days from expiration for the given account.
     * 
     * @param account
     * @return
     */
    private Map<AccountRole, Integer> getRoleToDaysFromExpirationMap(Account account)
    {

        Map<AccountRole, Integer> roleToDaysFromExpirationMap = new HashMap<AccountRole, Integer>();

        for (AccountRole role : account.getAccountRoleList())
        {
            int daysFromExpiration = getDaysAwayFromExpiration(role);
            roleToDaysFromExpirationMap.put(role, daysFromExpiration);
        }

        return roleToDaysFromExpirationMap;
    }

    /**
     * Returns the role list in the mail body  
     * @param roleToDaysFromExpirationMap
     * @param expiringRoles
     * @return
     */
    private String getRoleList(Map<AccountRole, Integer> roleToDaysFromExpirationMap,
          Map<Integer, List<AccountRole>> expiringRoles)
    {

        String orgName = modulesConstants.getModulesOrgName();

        StringBuffer roleListBuffer = new StringBuffer();

        for (int daysFromExpiration : daysFromExpirationToEmail)
        {
            List<AccountRole> rolesExpiringThisDay = expiringRoles.get(daysFromExpiration);

            if (rolesExpiringThisDay != null && !rolesExpiringThisDay.isEmpty())
            {
                int numberOfRolesExpiringThisDay = rolesExpiringThisDay.size();

                for (int i = 0; i < numberOfRolesExpiringThisDay; i++)
                {
                    String currentRole = rolesExpiringThisDay.get(i).getRoleType().getTitle();

                    if (i == 0) // first role expiring this day
                    {
                    	roleListBuffer.append("     Your ").append(orgName.replaceAll("_", " ")).append(" ").append(currentRole);
                    }

                    if (i > 0 && i < numberOfRolesExpiringThisDay - 1) // between the first and last role, not inclusive
                    {
                    	roleListBuffer.append(", ").append(currentRole);
                    }

                    if (i > 0 && i == numberOfRolesExpiringThisDay - 1) // last role, with more than one role expiring
                                                                        // today
                    {
                        if (numberOfRolesExpiringThisDay > 2)
                        {
                        	roleListBuffer.append(",");
                        }

                        roleListBuffer.append(" and ").append(currentRole)
                                .append(" account roles are about to expire in ").append(daysFromExpiration)
                                .append(" day");
                    }

                    if (numberOfRolesExpiringThisDay == 1) // only one role expiring today
                    {
                    	roleListBuffer.append(" account role is about to expire in ").append(daysFromExpiration)
                                .append(" day");
                    }

                    // days or day
                    if (i == numberOfRolesExpiringThisDay - 1)
                    {
                        if (daysFromExpiration > 1) // if more than one day from expiration, we need to add the 's' to
                                                    // make days plural.
                        {
                        	roleListBuffer.append("s");
                        }

                        roleListBuffer.append("."); // add period at the end
                    }
                }
            }
        }

        roleListBuffer
                .append("<br /><br />The following are all the account roles you have and their expiration dates: <br /><br /><table><tr><td>Role</td><td>Expiration Date</td><td>Days From Expiration</td></tr>");

        for (Entry<AccountRole, Integer> roleToDaysFromExpirationEntry : roleToDaysFromExpirationMap.entrySet())
        {
            AccountRole currentRole = roleToDaysFromExpirationEntry.getKey();
            String roleName = currentRole.getRoleType().getTitle();
            String expirationDate = null;

            if (currentRole.getExpirationDate() != null)
            {
                    expirationDate = BRICSTimeDateUtil.formatDate(currentRole.getExpirationDate());
            }
            else
            {
                expirationDate = ModelConstants.EMPTY_STRING;
            }

            Integer daysFromExpiration = roleToDaysFromExpirationEntry.getValue();

            roleListBuffer.append("<tr><td>").append(roleName).append("</td><td>").append(expirationDate)
                    .append("</td><td>")
                    .append(daysFromExpiration < Integer.MAX_VALUE ? daysFromExpiration : "No Expiration Date")
                    .append("</td></tr>");
        }

        roleListBuffer.append("</table>");
	
        return roleListBuffer.toString();
    }

    /**
     * Returns the number of days from expiration the account role is
     * 
     * @param account
     * @return
     */
    private int getDaysAwayFromExpiration(AccountRole role)
    {

        Date roleExpirationDate = role.getExpirationDate();

        // return a high number if the field is null, so we never send out the expiration email for this role
        if (roleExpirationDate == null)
        {
            return Integer.MAX_VALUE;
        }

        return getDaysAwayFromToday(roleExpirationDate.getTime());
    }
}
