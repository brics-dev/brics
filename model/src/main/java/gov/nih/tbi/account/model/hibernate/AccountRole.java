
package gov.nih.tbi.account.model.hibernate;

import gov.nih.tbi.ModelConstants;
import gov.nih.tbi.commons.model.BRICSTimeDateUtil;
import gov.nih.tbi.commons.model.RoleStatus;
import gov.nih.tbi.commons.model.RoleType;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlTransient;

@Entity
@Table(name = "ACCOUNT_ROLE")
@XmlAccessorType(XmlAccessType.FIELD)
public class AccountRole implements Serializable
{

    /**
	 * 
	 */
    private static final long serialVersionUID = -9001636980907801431L;

    /**********************************************************************/

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "ACCOUNT_ROLE_SEQ")
    @SequenceGenerator(name = "ACCOUNT_ROLE_SEQ", sequenceName = "ACCOUNT_ROLE_SEQ", allocationSize = 1)
    private Long id;

    @XmlTransient
    @ManyToOne
    @JoinColumn(name = "ACCOUNT_ID")
    private Account account;

    @Enumerated(EnumType.ORDINAL)
    @Column(name = "ROLE_TYPE_ID")
    private RoleType roleType;

    @Enumerated(EnumType.ORDINAL)
    @Column(name = "ROLE_STATUS_ID")
    private RoleStatus roleStatus;

    @Column(name = "EXPIRATION_DATE")
    private Date expirationDate;

    /**********************************************************************/

    public AccountRole()
    {

    }

    public AccountRole(Account account, RoleType roleType, RoleStatus roleStatus, Date expirationDate)
    {

        this.account = account;
        this.roleType = roleType;
        this.roleStatus = roleStatus;
        this.expirationDate = expirationDate;
    }

    public Long getId()
    {

        return id;
    }

    public void setId(Long id)
    {

        this.id = id;
    }

    public Account getAccount()
    {

        return account;
    }

    public void setAccount(Account account)
    {

        this.account = account;
    }

    public RoleType getRoleType()
    {

        return roleType;
    }

    public void setRoleType(RoleType roleType)
    {

        this.roleType = roleType;
    }

    public RoleStatus getRoleStatus()
    {

        return roleStatus;
    }

    public void setRoleStatus(RoleStatus roleStatus)
    {

        this.roleStatus = roleStatus;
    }

    public Date getExpirationDate()
    {

        return expirationDate;
    }

    public void setExpirationDate(Date expirationDate)
    {

        this.expirationDate = expirationDate;
    }

    /**
     * Returns the expirationDate as a string formatted dd-MMM-yyyy
     * 
     * @return
     */
    public String getExpirationString()
    {

        if (this.expirationDate == null)
        {
            return "-";
        }
        SimpleDateFormat df = new SimpleDateFormat(ModelConstants.UNIVERSAL_DATE_FORMAT);

        return df.format(expirationDate);
    }

    public boolean isExpired()
    {

        Date now = new Date();
        if (this.expirationDate != null)
        {
            return now.after(this.expirationDate);
        }
        else
        {
            return false;
        }
    }
    
    public String getExpirationDateISOFormat(){
    	 if (this.expirationDate == null)
         {
             return "No Expiration Date";
         }
    	 
    	 return BRICSTimeDateUtil.formatDate(this.expirationDate);
    }
    
    public boolean getIsActive() {
    	return this.getRoleStatus() == RoleStatus.ACTIVE || this.getRoleStatus() == RoleStatus.EXPIRING_SOON;
    }
    
    @Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((account == null) ? 0 : account.hashCode());
		result = prime * result + ((expirationDate == null) ? 0 : expirationDate.hashCode());
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		result = prime * result + ((roleStatus == null) ? 0 : roleStatus.hashCode());
		result = prime * result + ((roleType == null) ? 0 : roleType.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		AccountRole other = (AccountRole) obj;
		if (account == null) {
			if (other.account != null)
				return false;
		} else if (!account.equals(other.account))
			return false;
		if (expirationDate == null) {
			if (other.expirationDate != null)
				return false;
		} else if (!expirationDate.equals(other.expirationDate))
			return false;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		if (roleStatus != other.roleStatus)
			return false;
		if (roleType != other.roleType)
			return false;
		return true;
	}

	@Override
    public String toString()
    {

        return "[" + getRoleType() + ", " + getRoleStatus() + ", " + getExpirationString() + "]";
    }
}
