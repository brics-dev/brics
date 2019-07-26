package gov.nih.tbi.account.model;

import java.util.Date;

import gov.nih.tbi.account.model.hibernate.Account;
import gov.nih.tbi.account.model.hibernate.ElectronicSignature;

public class AccountSignatureForm {

    private String firstName;
    private String middleName;
    private String lastName;
    private String password;
    
	public AccountSignatureForm() {

	}

	public void adapt(Account account) {
		ElectronicSignature signature = new ElectronicSignature();
		signature.setFirstName(firstName);
		signature.setMiddleName(middleName);
		signature.setLastName(lastName);
		signature.setSignatureDate(new Date());
		signature.setSignatureType(SignatureType.BRICS_ELECTRONIC_SIGNATURE);
		signature.setAccount(account);
		
		account.addElectronicSignature(signature);
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
	
	public String getPassword() {
		return password;
	}
	
	public void setPassword(String password) {
		this.password = password;
	}
    
}
