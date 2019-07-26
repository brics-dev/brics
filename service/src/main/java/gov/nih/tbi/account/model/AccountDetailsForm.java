
package gov.nih.tbi.account.model;

import gov.nih.tbi.account.model.hibernate.Account;
import gov.nih.tbi.commons.model.hibernate.Country;
import gov.nih.tbi.commons.model.hibernate.State;
import gov.nih.tbi.commons.model.hibernate.User;
import gov.nih.tbi.commons.service.AccountManager;
import gov.nih.tbi.commons.service.ServiceConstants;
import gov.nih.tbi.commons.service.StaticReferenceManager;
import gov.nih.tbi.commons.ws.HashMethods;
import gov.nih.tbi.dictionary.model.StaticField;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * This class masks the static fields of Account Details form.
 * 
 * @author Francis Chen
 * 
 */
public class AccountDetailsForm
{

    static Logger logger = Logger.getLogger(AccountDetailsForm.class);

    @Autowired
    StaticReferenceManager staticManager;
    @Autowired
    AccountManager accountManager;

    protected String userName;
    protected String passwordString;
    protected String confirmPassword;
    protected User user;
    protected String affiliatedInstitution;
    protected String eraId;
    protected String address1;
    protected String address2;
    protected String city;
    protected State state;
    protected String postalCode;
    protected Country country;
    protected String phone;
    protected String interestInTbi;
    protected String adminNote;

    protected String firstName;
    protected String middleName;
    protected String lastName;
    protected String email;

    public AccountDetailsForm()
    {

    }

    /**
     * Constructor fetches data for each column in dataElement object
     * 
     * @param account
     */
    public AccountDetailsForm(Account account)
    {

        if (account != null)
        {
            Field[] fields = this.getClass().getDeclaredFields();

            for (int i = 0; i < fields.length; i++)
            {
                Field current = fields[i];
                Object value = null;

                if (!current.getName().equals("logger") && !ServiceConstants.STATIC_MANAGER.equals(current.getName())
                        && !ServiceConstants.ACCOUNT_MANAGER.equals(current.getName()))
                {

                    try
                    {
                        String getMethodName = "get" + current.getName().substring(0, 1).toUpperCase()
                                + current.getName().substring(1);

                        Method setMethod = null;

                        for (Field userField : User.class.getDeclaredFields())
                        {
                            if (userField.getName().equals(current.getName()))
                            {
                                setMethod = user.getClass().getMethod(getMethodName);

                                try
                                {
                                    value = setMethod.invoke(user);
                                    logger.debug(current.getName() + ": " + value);
                                    break;
                                }
                                catch (InvocationTargetException ex)
                                {
                                    if (ex.getCause() instanceof UnsupportedOperationException)
                                    {
                                        logger.error("Could not call method defined by setMethod.");
                                    }
                                    else
                                    {
                                        throw ex;
                                    }
                                }
                            }
                        }

                        for (Field accountField : Account.class.getDeclaredFields())
                        {
                            if (accountField.getName().equals(current.getName()))
                            {
                                setMethod = account.getClass().getMethod(getMethodName);

                                try
                                {
                                    value = setMethod.invoke(account);
                                    logger.debug(current.getName() + ": " + value);
                                    break;
                                }
                                catch (InvocationTargetException ex)
                                {
                                    if (ex.getCause() instanceof UnsupportedOperationException)
                                    {
                                        logger.error("Could not call method defined by setMethod.");
                                    }
                                    else
                                    {
                                        throw ex;
                                    }
                                }
                            }
                        }

                        current.set(this, value);
                    }
                    catch (Exception e)
                    {
                        e.printStackTrace();
                        logger.error("There was an exception in the account details action: " + e.toString());

                    }
                }
            }

            account.setUser(user);
        }
    }

    /*************************************************************/

    public String getUserName()
    {

        return userName;
    }

    public String getAdminNote()
    {

        return adminNote;
    }

    public void setAdminNote(String adminNote)
    {

        this.adminNote = adminNote;
    }

    public void setUserName(String userName)
    {

        this.userName = userName;
    }

    public String getPasswordString()
    {

        return passwordString;
    }

    public void setPasswordString(String password)
    {

        this.passwordString = password;
    }

    public String getConfirmPassword()
    {

        return confirmPassword;
    }

    public void setConfirmPassword(String confirmPassword)
    {

        this.confirmPassword = confirmPassword;
    }

    public User getUser()
    {

        return user;
    }

    public void setUser(User user)
    {

        this.user = user;
    }

    public String getAffiliatedInstitution()
    {

        return affiliatedInstitution;
    }

    public void setAffiliatedInstitution(String affiliatedInstitution)
    {

        this.affiliatedInstitution = affiliatedInstitution;
    }

    public String getEraId()
    {

        return eraId;
    }

    public void setEraId(String eraId)
    {

        this.eraId = eraId;
    }

    public String getAddress1()
    {

        return address1;
    }

    public void setAddress1(String address1)
    {

        this.address1 = address1;
    }

    public String getAddress2()
    {

        return address2;
    }

    public void setAddress2(String address2)
    {

        if (address2 != null && !ServiceConstants.EMPTY_STRING.equals(address2))
            this.address2 = address2;
    }

    public String getCity()
    {

        return city;
    }

    public void setCity(String city)
    {

        this.city = city;
    }

    public State getState()
    {

        return state;
    }

    public void setState(String state)
    {

        if (state != null && !ServiceConstants.EMPTY_STRING.equals(state))
        {
            for (State currState : staticManager.getStateList())
            {
                if (Long.valueOf(state).equals(currState.getId()))
                {
                    this.state = currState;
                }
            }
        }
    }

    public String getPostalCode()
    {

        return postalCode;
    }

    public void setPostalCode(String postalCode)
    {

        this.postalCode = postalCode;
    }

    public Country getCountry()
    {

        return country;
    }

    public void setCountry(String country)
    {

        if (country != null && !ServiceConstants.EMPTY_STRING.equals(country))
        {
            for (Country currCountry : staticManager.getCountryList())
            {
                if (Long.valueOf(country).equals(currCountry.getId()))
                {
                    this.country = currCountry;
                }
            }
        }
    }

    public String getPhone()
    {

        return phone;
    }

    public void setPhone(String phone)
    {

        if (phone != null && !ServiceConstants.EMPTY_STRING.equals(phone))
        {
            this.phone = phone;
        }
    }

    public String getInterestInTbi()
    {

        return interestInTbi;
    }

    public void setInterestInTbi(String interestInTbi)
    {

        this.interestInTbi = interestInTbi;
    }

    public String getFirstName()
    {

        return firstName;
    }

    public void setFirstName(String firstName)
    {

        this.firstName = firstName;
    }

    public String getMiddleName() {
		return middleName;
	}

	public void setMiddleName(String middleName) {
		this.middleName = middleName;
	}

	public String getLastName()
    {

        return lastName;
    }

    public void setLastName(String lastName)
    {

        this.lastName = lastName;
    }

    public String getEmail()
    {

        return email;
    }

    public void setEmail(String email)
    {

        this.email = email;
    }

    /******************************************************/

    public void adapt(Account account, Boolean enforceStaticFields, String nameSpace)
    {

        User user = account.getUser();

        Field[] fields = this.getClass().getDeclaredFields();

        for (int i = 0; i < fields.length; i++)
        {
            Field current = fields[i];

            if (!current.getName().equals("logger") && !ServiceConstants.STATIC_MANAGER.equals(current.getName())
                    && !ServiceConstants.ACCOUNT_MANAGER.equals(current.getName()))
            {

                try
                {
                    if (enforceStaticFields == false || current.getAnnotation(StaticField.class) == null)
                    {
                        Object value = current.get(this);

                        String setMethodName = "set" + current.getName().substring(0, 1).toUpperCase()
                                + current.getName().substring(1);

                        Method setMethod = null;

                        for (Field userField : User.class.getDeclaredFields())
                        {
                            if (userField.getName().equals(current.getName()))
                            {
                                setMethod = user.getClass().getMethod(setMethodName, current.getType());

                                try
                                {
                                    setMethod.invoke(user, value);
                                    logger.debug(current.getName() + ": " + value);
                                    break;
                                }
                                catch (InvocationTargetException ex)
                                {
                                    if (ex.getCause() instanceof UnsupportedOperationException)
                                    {
                                        logger.error("Could not call method defined by setMethod.");
                                    }
                                    else
                                    {
                                        throw ex;
                                    }
                                }
                            }
                        }

                        for (Field accountField : Account.class.getDeclaredFields())
                        {
                            if (accountField.getName().equals(current.getName()))
                            {
								// This is a quick and dirty fix that prevents adminAccount from
								// being erased when a user edits their own profile.
								if ("adminNote".equals(accountField.getName()) && !"accountAdmin".equals(nameSpace)) {
									break;
								}
                                setMethod = account.getClass().getMethod(setMethodName, current.getType());

                                try
                                {
                                    setMethod.invoke(account, value);
                                    logger.debug(current.getName() + ": " + value);
                                    break;
                                }
                                catch (InvocationTargetException ex)
                                {
                                    if (ex.getCause() instanceof UnsupportedOperationException)
                                    {
                                        logger.error("Could not call method defined by setMethod.");
                                    }
                                    else
                                    {
                                        throw ex;
                                    }
                                }
                            }
                        }
                    }
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                    logger.error("There was an exception in the account details form method " + e.toString());
                }
            }
        }
        // only set password if something new is entered
        if (passwordString != null && !ServiceConstants.EMPTY_STRING.equals(passwordString))
        {
            String salt = HashMethods.getPasswordSalt();
            account.setSalt(salt);
            account.setPassword(accountManager.hashPassword(salt+passwordString));
        }
        account.setUser(user);
    }
}
