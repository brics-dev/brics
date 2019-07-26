
package gov.nih.tbi.account.model.hibernate;

import gov.nih.tbi.commons.model.AccountStatus;
import gov.nih.tbi.commons.model.hibernate.Country;
import gov.nih.tbi.commons.model.hibernate.State;
import gov.nih.tbi.commons.model.hibernate.User;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

/**
 * A Basic representation of an account object (without any OneToMany relationships).
 * 
 * @author mvalei
 */
@Entity
@Table(name = "ACCOUNT")
public class BasicAccount implements Serializable
{

    private static final long serialVersionUID = -288792190452003023L;

    /**********************************************************************/

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "ACCOUNT_SEQ")
    @SequenceGenerator(name = "ACCOUNT_SEQ", sequenceName = "ACCOUNT_SEQ", allocationSize = 1)
    private Long id;

    @Column(name = "USER_NAME")
    private String userName;

    @Column(name = "PASSWORD")
    private byte[] password;

    @OneToOne(cascade = { CascadeType.DETACH, CascadeType.MERGE, CascadeType.PERSIST })
    @JoinColumn(name = "USER_ID")
    private User user;

    @Column(name = "AFFILIATED_INSTITUTION")
    private String affiliatedInstitution;

    @Column(name = "ERA_COMMONS_ID")
    private String eraId;

    @Column(name = "ADDRESS_1")
    private String address1;

    @Column(name = "ADDRESS_2")
    private String address2;

    @Column(name = "CITY")
    private String city;

    @OneToOne(cascade = { CascadeType.DETACH, CascadeType.MERGE, CascadeType.PERSIST })
    @JoinColumn(name = "STATE_ID")
    private State state;

    @Column(name = "POSTAL_CODE")
    private String postalCode;

    @OneToOne(cascade = { CascadeType.DETACH, CascadeType.MERGE, CascadeType.PERSIST })
    @JoinColumn(name = "COUNTRY_ID")
    private Country country;

    @Column(name = "PHONE")
    private String phone;

    @Column(name = "INTEREST_IN_TBI")
    private String interestInTbi;

    @Column(name = "RECOVERY_DATE")
    private Date recoveryDate;

    @Column(name = "IS_ACTIVE")
    private Boolean isActive;

    @Enumerated(EnumType.ORDINAL)
    @Column(name = "ACCOUNT_STATUS_ID")
    private AccountStatus accountStatus;

    @Column(name = "APPLICATION_DATE")
    private Date applicationDate;

    @Column(name = "LAST_UPDATED_DATE")
    private Date lastUpdatedDate;

    /**********************************************************************/

    public BasicAccount()
    {

        user = new User();
    }

    public Long getId()
    {

        return id;
    }

    public void setId(Long id)
    {

        this.id = id;
    }

    public String getUserName()
    {

        return userName;
    }

    public void setUserName(String userName)
    {

        this.userName = userName;
    }

    public byte[] getPassword()
    {

        return password;
    }

    public void setPassword(byte[] password)
    {

        this.password = password;
    }

    public User getUser()
    {

        return user;
    }

    public void setUser(User user)
    {

        this.user = user;
    }

    public Long getUserId()
    {

        if (user != null)
        {
            return user.getId();
        }
        else
        {
            return null;
        }
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

    public void setState(State state)
    {

        this.state = state;
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

    public void setCountry(Country country)
    {

        this.country = country;
    }

    public String getPhone()
    {

        return phone;
    }

    public void setPhone(String phone)
    {

        this.phone = phone;
    }

    public String getInterestInTbi()
    {

        return interestInTbi;
    }

    public void setInterestInTbi(String interestInTbi)
    {

        this.interestInTbi = interestInTbi;
    }

    public AccountStatus getAccountStatus()
    {

        return accountStatus;
    }

    public void setAccountStatus(AccountStatus accountStatus)
    {

        this.accountStatus = accountStatus;
    }

    /**
     * @return the recoveryDate
     */
    public Date getRecoveryDate()
    {

        return recoveryDate;
    }

    /**
     * @param recoveryDate
     *            the recoveryDate to set
     */
    public void setRecoveryDate(Date recoveryDate)
    {

        this.recoveryDate = recoveryDate;
    }

    public Boolean getIsActive()
    {

        return isActive;
    }

    public void setIsActive(Boolean isActive)
    {

        this.isActive = isActive;
    }

    public Date getApplicationDate()
    {

        return applicationDate;
    }

    public void setApplicationDate(Date applicationDate)
    {

        this.applicationDate = applicationDate;
    }

    public String getApplicationString()
    {

        if (applicationDate == null)
        {
            return "-";
        }
        SimpleDateFormat df = new SimpleDateFormat("MM/dd/yyyy");

        return df.format(applicationDate);
    }

	public Date getLastUpdatedDate() {
		return lastUpdatedDate;
	}

	public void setLastUpdatedDate(Date lastUpdatedDate) {
		this.lastUpdatedDate = lastUpdatedDate;
	}
}
