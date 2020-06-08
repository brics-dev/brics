package gov.nih.nichd.ctdb.util;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import gov.nih.tbi.commons.util.BRICSStringUtils;

public class PdfDocGenerator {

	private static Logger logger = Logger.getLogger(PdfDocGenerator.class);
	
	private static final SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd");

	private String firstName;
	private String middleName;
	private String lastName;
	private Date signDate;

	public PdfDocGenerator() {
	}

	public PdfDocGenerator(String firstName, String middleName, String lastName, Date signDate) {
		this.firstName = firstName;
		this.middleName = middleName;
		this.lastName = lastName;
		this.signDate = signDate;
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

	public Date getSignDate() {
		return signDate;
	}

	public void setSignDate(Date signDate) {
		this.signDate = signDate;
	}

	public String getFullName() {
		StringBuilder sb = new StringBuilder();
		sb.append(BRICSStringUtils.capitalizeFirstCharacter(firstName.trim()));

		if (!StringUtils.isEmpty(middleName)) {
			sb.append(" ").append(BRICSStringUtils.capitalizeFirstCharacter(middleName.trim()));
		}

		sb.append(" ").append(BRICSStringUtils.capitalizeFirstCharacter(lastName.trim()));
		return sb.toString();
	}
}
