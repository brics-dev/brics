package gov.nih.tbi.account.model.hibernate;

import java.io.Serializable;
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
import javax.persistence.OneToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlTransient;

import gov.nih.tbi.account.model.SignatureType;
import gov.nih.tbi.repository.model.hibernate.UserFile;

@Entity
@Table(name = "ELECTRONIC_SIGNATURE")
@XmlAccessorType(XmlAccessType.FIELD)
public class ElectronicSignature implements Serializable {

	private static final long serialVersionUID = 3827844941921829883L;

	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "ELECTRONIC_SIGNATURE_SEQ")
	@SequenceGenerator(name = "ELECTRONIC_SIGNATURE_SEQ", sequenceName = "ELECTRONIC_SIGNATURE_SEQ", allocationSize = 1)
	private Long id;

	@Column(name = "FIRST_NAME")
	private String firstName;
	
	@Column(name = "MIDDLE_NAME")
	private String middleName;	

	@Column(name = "LAST_NAME")
	private String lastName;
	
	@Column(name = "SIGNATURE_DATE")
	private Date signatureDate;
	
	@OneToOne
	@JoinColumn(name = "SIGNATURE_USER_FILE_ID")
	private UserFile signatureFile;
	
	@Enumerated(EnumType.ORDINAL)
	@Column(name = "SIGNATURE_TYPE_ID")
	private SignatureType signatureType;

	@XmlTransient
	@ManyToOne
	@JoinColumn(name = "ACCOUNT_ID")
	private Account account;

	
	public ElectronicSignature() {
	}

	public ElectronicSignature(Long id, String firstName, String middleName, String lastName, Date signatureDate,
			UserFile signatureFile, SignatureType signatureType, Account account) {
		this.id = id;
		this.firstName = firstName;
		this.middleName = middleName;
		this.lastName = lastName;
		this.signatureDate = signatureDate;
		this.signatureFile = signatureFile;
		this.signatureType = signatureType;
		this.account = account;
	}

	
	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getFirstName() {
		return firstName;
	}

	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}

	public String getMiddleName() {
		return middleName;
	}

	public void setMiddleName(String middleName) {
		this.middleName = middleName;
	}

	public String getLastName() {
		return lastName;
	}

	public void setLastName(String lastName) {
		this.lastName = lastName;
	}

	public Date getSignatureDate() {
		return signatureDate;
	}

	public void setSignatureDate(Date signatureDate) {
		this.signatureDate = signatureDate;
	}

	public UserFile getSignatureFile() {
		return signatureFile;
	}

	public void setSignatureFile(UserFile signatureFile) {
		this.signatureFile = signatureFile;
	}

	public SignatureType getSignatureType() {
		return signatureType;
	}

	public void setSignatureType(SignatureType signatureType) {
		this.signatureType = signatureType;
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
		result = prime * result + ((firstName == null) ? 0 : firstName.hashCode());
		result = prime * result + ((middleName == null) ? 0 : middleName.hashCode());
		result = prime * result + ((lastName == null) ? 0 : lastName.hashCode());
		result = prime * result + ((signatureDate == null) ? 0 : signatureDate.hashCode());
		result = prime * result + ((signatureFile == null) ? 0 : signatureFile.getId().hashCode());
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
		
		ElectronicSignature other = (ElectronicSignature) obj;
		if (firstName == null) {
			if (other.firstName != null)
				return false;
		} else if (!firstName.equals(other.firstName))
			return false;
		if (middleName == null) {
			if (other.middleName != null)
				return false;
		} else if (!middleName.equals(other.middleName))
			return false;
		if (lastName == null) {
			if (other.lastName != null)
				return false;
		} else if (!lastName.equals(other.lastName))
			return false;
		if (signatureDate == null) {
			if (other.signatureDate != null)
				return false;
		} else if (!signatureDate.equals(other))
			return false;
		if (signatureFile == null) {
			if (other.signatureFile != null)
				return false;
		} else if (!signatureFile.getId().equals(other.signatureFile.getId()))
			return false;
		if (signatureType == null) {
			if (other.signatureType != null)
				return false;
		} else if (!signatureType.getId().equals(other.signatureType.getId()))
			return false;
		return true;
	}

}
