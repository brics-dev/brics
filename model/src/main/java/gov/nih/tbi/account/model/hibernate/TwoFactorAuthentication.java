package gov.nih.tbi.account.model.hibernate;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
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
@Table(name = "TWO_FACTOR_AUTHENTICATION")
@XmlAccessorType(XmlAccessType.FIELD)
public class TwoFactorAuthentication implements Serializable {

	private static final long serialVersionUID = 3827844941921829883L;

	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "TWO_FACTOR_AUTHENTICATION_SEQ")
	@SequenceGenerator(name = "TWO_FACTOR_AUTHENTICATION_SEQ", sequenceName = "TWO_FACTOR_AUTHENTICATION_SEQ", allocationSize = 1)
	private Long id;

	@Column(name = "TWO_FA_CODE")
	private String twoFaCode;

	@Column(name = "TWO_FA_DATE")
	private Date twoFaDate;

	@Column(name = "TWO_FA_EXPIRATION_DATE")
	private Date twoFaExpirationDate;

	@XmlTransient
	@ManyToOne
	@JoinColumn(name = "ACCOUNT_ID")
	private Account account;

	public TwoFactorAuthentication() {
	}

	public TwoFactorAuthentication(String twoFaCode, Date twoFaDate, Date twoFaExpirationDate, Account account) {
		this.twoFaCode = twoFaCode;
		this.twoFaDate = twoFaDate;
		this.twoFaExpirationDate = twoFaExpirationDate;
		this.account = account;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
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


	public Account getAccount() {
		return account;
	}
	public void setAccount(Account account) {
		this.account = account;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((twoFaCode == null) ? 0 : twoFaCode.hashCode());
		result = prime * result + ((twoFaDate == null) ? 0 : twoFaDate.hashCode());
		result = prime * result + ((twoFaExpirationDate == null) ? 0 : twoFaExpirationDate.hashCode());
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

		TwoFactorAuthentication other = (TwoFactorAuthentication) obj;
		if (twoFaCode == null) {
			if (other.twoFaCode != null)
				return false;
		} else if (!twoFaCode.equals(other.twoFaCode))
			return false;
		if (twoFaDate == null) {
			if (other.twoFaDate != null)
				return false;
		} else if (!twoFaDate.equals(other))
			return false;
		if (twoFaExpirationDate == null) {
			if (other.twoFaExpirationDate != null)
				return false;
		} else if (!twoFaExpirationDate.equals(other))
			return false;

		return true;
	}
}
