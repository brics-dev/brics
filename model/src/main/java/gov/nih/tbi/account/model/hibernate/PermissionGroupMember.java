package gov.nih.tbi.account.model.hibernate;

import gov.nih.tbi.ModelConstants;
import gov.nih.tbi.commons.model.PermissionGroupStatus;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
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

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

@Entity
@Table(name = "PERMISSION_GROUP_MEMBER")
@XmlAccessorType(XmlAccessType.FIELD)
public class PermissionGroupMember implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 8035550616038599510L;

	/**********************************************************************/

	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "PERMISSION_GROUP_MEMBER_SEQ")
	@SequenceGenerator(name = "PERMISSION_GROUP_MEMBER_SEQ", sequenceName = "PERMISSION_GROUP_MEMBER_SEQ", allocationSize = 1)
	private Long id;

	@XmlTransient
	@ManyToOne(fetch = FetchType.LAZY, targetEntity = Account.class)
	@JoinColumn(name = "ACCOUNT_ID")
	private Account account;

	@ManyToOne(fetch = FetchType.LAZY, targetEntity = PermissionGroup.class)
	@JoinColumn(name = "PERMISSION_GROUP_ID")
	private PermissionGroup permissionGroup;

	@Enumerated(EnumType.ORDINAL)
	@Column(name = "PERMISSION_GROUP_STATUS_ID")
	private PermissionGroupStatus permissionGroupStatus;

	@Column(name = "EXPIRATION_DATE")
	private Date expirationDate;

	/**********************************************************************/

	public Long getId() {

		return id;
	}

	public void setId(Long id) {

		this.id = id;
	}

	public PermissionGroup getPermissionGroup() {

		return permissionGroup;
	}

	public void setPermissionGroup(PermissionGroup permissionGroup) {

		this.permissionGroup = permissionGroup;
	}

	public Account getAccount() {

		return account;
	}

	public void setAccount(Account account) {

		this.account = account;
	}

	public PermissionGroupStatus getPermissionGroupStatus() {

		return permissionGroupStatus;
	}

	public void setPermissionGroupStatus(PermissionGroupStatus permissionGroupStatus) {

		this.permissionGroupStatus = permissionGroupStatus;
	}

	public Date getExpirationDate() {

		return expirationDate;
	}
	
	
	/**
     * Returns the expirationDate as a string formatted dd-MMM-yyyy
     * 
     * @return
     */
    public String getExpirationString()
    {

        if (expirationDate == null)
        {
            return "No Expiration Date";
        }
        SimpleDateFormat df = new SimpleDateFormat(ModelConstants.UNIVERSAL_DATE_FORMAT);

        return df.format(expirationDate);
    }
    
    

	public void setExpirationDate(Date expirationDate) {

		this.expirationDate = expirationDate;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((account == null) ? 0 : account.hashCode());
		result = prime * result + ((permissionGroup == null) ? 0 : permissionGroup.hashCode());
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
		PermissionGroupMember other = (PermissionGroupMember) obj;
		if (account == null) {
			if (other.account != null)
				return false;
		} else if (!account.equals(other.account))
			return false;
		if (permissionGroup == null) {
			if (other.permissionGroup != null)
				return false;
		} else if (!permissionGroup.equals(other.permissionGroup))
			return false;
		return true;
	}
}
