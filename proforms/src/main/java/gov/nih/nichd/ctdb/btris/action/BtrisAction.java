package gov.nih.nichd.ctdb.btris.action;

import org.apache.log4j.Logger;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.Iterator;
import java.util.List;


import gov.nih.nichd.ctdb.common.CtdbLookup;
import gov.nih.nichd.ctdb.util.domain.LookupType;
import gov.nih.nichd.ctdb.util.manager.LookupManager;
import gov.nih.nichd.ctdb.btris.domain.BtrisSubject;
import gov.nih.nichd.ctdb.btris.domain.ProformsSubject;
import gov.nih.nichd.ctdb.btris.manager.BtrisManager;
import gov.nih.nichd.ctdb.common.BaseAction;
import gov.nih.nichd.ctdb.common.CtdbException;
import gov.nih.nichd.ctdb.util.manager.LookupManager;

import java.util.Calendar;
import java.util.Date;

public class BtrisAction extends BaseAction {

	private static final long serialVersionUID = 1463193556702349466L;
	private static final Logger logger = Logger.getLogger(BtrisAction.class);

	private String mrn;
	private String firstName;
	private String lastName;
	private String mrnG;
	private String firstNameG;
	private String lastNameG;
	private String jsonString;
	private String errRespMsg;

	public String execute() {
		return SUCCESS;
	}
	public String getSubjectFromBtris() {

		try {
			ProformsSubject ps = new ProformsSubject();
			ps.setMrn(mrn);
			ps.setFirstName(firstName);
			ps.setLastName(lastName);
			BtrisManager bm = new BtrisManager();
			BtrisSubject bs = bm.getSubjectFromBtris(ps);

			JSONObject btrisSubject = new JSONObject();
			btrisSubject.put("birthCity", bs.getBirthCity());
			btrisSubject.put("dob", bs.getDob());
			btrisSubject.put("middleName", bs.getMiddleName());
			btrisSubject.put("sex", bs.getGender());
			btrisSubject.put("homeAddress1", bs.getAddressLine1());
			btrisSubject.put("homeAddress2", bs.getAddressLine2());
			btrisSubject.put("homePhone", bs.getPhoneNumber());
			btrisSubject.put("city", bs.getCity());
			btrisSubject.put("zip", bs.getZip());
			btrisSubject.put("state", "0");
			String btrisState = bs.getCountryDivisionCode();
			if (btrisState != null && !btrisState.trim().equals("")) {
				try {
					Integer stateId = getStateId(btrisState);
					btrisSubject.put("state", ""+stateId+"");			
				}
				catch (CtdbException ce) {
					logger.error("Error in getting state code from btris ", ce);
					errRespMsg = ce.getMessage();
					return ERROR;
				} 
			}
			btrisSubject.put("birthCountry", "0");
			String btrisBirthCountry = bs.getBirthCountry();
			if (btrisBirthCountry != null && !btrisBirthCountry.trim().equals("")) {
				if (btrisBirthCountry.equalsIgnoreCase("USA")) {
					btrisBirthCountry = "United States of America";
				} else if (btrisBirthCountry.equalsIgnoreCase("ENGLAND")) {
					btrisBirthCountry = "United Kingdom";
				}
				try {
					Integer countryId = getCountryId(btrisBirthCountry);
					btrisSubject.put("birthCountry", ""+countryId+"");
				}
				catch (CtdbException ce) {
					logger.error("Error in getting birth country code from btris ", ce);
					errRespMsg = ce.getMessage();
					return ERROR;
				} 
			}			
			btrisSubject.put("country", "0");
			String btrisCountry = bs.getCountryCode();
			if (btrisCountry != null && !btrisCountry.trim().equals("")) {
				if (btrisCountry.equalsIgnoreCase("USA")) {
					btrisCountry = "United States of America";
				}
				try {
					Integer countryId = getCountryId(btrisCountry);
					btrisSubject.put("country", ""+countryId+"");
				}
				catch (CtdbException ce) {
					logger.error("Error in getting country code from btris ", ce);
					errRespMsg = ce.getMessage();
					return ERROR;
				} 
			}
			jsonString = btrisSubject.toString();

		} catch (CtdbException ce) {
			logger.error("Error in getting subject from btris ", ce);
			errRespMsg = ce.getMessage();
			return ERROR;
		} catch (JSONException je) {
			logger.error("Error in constructing json for subject from btris ", je);
			errRespMsg = je.getMessage();
			return ERROR;
		}
		return SUCCESS;
	}

	public String getGuidInfoFromBtris() {

		try {
			ProformsSubject ps = new ProformsSubject();
			ps.setMrn(mrnG);
			ps.setFirstName(firstNameG);
			ps.setLastName(lastNameG);
			BtrisManager bm = new BtrisManager();
			BtrisSubject bs = bm.getSubjectFromBtris(ps);

			JSONObject btrisSubject = new JSONObject();
			btrisSubject.put("guidClientInputFn1", bs.getFirstName());
			btrisSubject.put("guidClientInputFn2", bs.getFirstName());
			btrisSubject.put("guidClientInputSUBJECTHASMIDDLENAME1", "No");
			btrisSubject.put("guidClientInputSUBJECTHASMIDDLENAME2", "No");
			String subjectHasMn = bs.getMiddleName();
			if (subjectHasMn != null && !subjectHasMn.trim().equals("")) {
				btrisSubject.put("guidClientInputSUBJECTHASMIDDLENAME1", "Yes");
				btrisSubject.put("guidClientInputSUBJECTHASMIDDLENAME2", "Yes");
			}
			btrisSubject.put("guidClientInputMn1", bs.getMiddleName());
			btrisSubject.put("guidClientInputMn2", bs.getMiddleName());
			btrisSubject.put("guidClientInputLn1", bs.getLastName());
			btrisSubject.put("guidClientInputLn2", bs.getLastName());
			
			///DOB split into into day, month & year. btrisSubject.put("dob", bs.getDob());
			Integer dayOfBirth = null;
			Integer monthOfBirth = null;
			Integer yearOfBirth = null;
			Date dob = null;
			if (bs.getDob() != null) {
				dob = bs.getDob();
				Calendar calendar = Calendar.getInstance();
				calendar.setTime(dob);
				dayOfBirth = calendar.get(Calendar.DAY_OF_MONTH);
				monthOfBirth = calendar.get(Calendar.MONTH)+1;
				yearOfBirth = calendar.get(Calendar.YEAR);
			}
			btrisSubject.put("guidClientInputDob1", dayOfBirth);
			btrisSubject.put("guidClientInputDob2", dayOfBirth);
			btrisSubject.put("guidClientInputMob1", monthOfBirth);
			btrisSubject.put("guidClientInputMob2", monthOfBirth);
			btrisSubject.put("guidClientInputYob1", yearOfBirth);
			btrisSubject.put("guidClientInputYob2", yearOfBirth);
			
			btrisSubject.put("guidClientInputCob1", bs.getBirthCity());
			btrisSubject.put("guidClientInputCob2", bs.getBirthCity());
			
			btrisSubject.put("guidClientInputCnob1", "");
			btrisSubject.put("guidClientInputCnob2", "");
			String btrisBirthCountry = bs.getBirthCountry();
			if (btrisBirthCountry != null && !btrisBirthCountry.trim().equals("")) {
				if (btrisBirthCountry.equalsIgnoreCase("USA")) {
					btrisBirthCountry = "UNITED STATES";
				}
				btrisSubject.put("guidClientInputCnob1", btrisBirthCountry.toUpperCase());
				btrisSubject.put("guidClientInputCnob2", btrisBirthCountry.toUpperCase());
			}
			String gender = bs.getGender();
			if (gender != null && !gender.trim().equals("")) {
				if(gender.trim().equalsIgnoreCase("Male")) {
					btrisSubject.put("guidClientInputSex1", "M");
					btrisSubject.put("guidClientInputSex2", "M");
				} else if (gender.trim().equalsIgnoreCase("Female")) {
					btrisSubject.put("guidClientInputSex1", "F");
					btrisSubject.put("guidClientInputSex2", "F");
				} else{
					btrisSubject.put("guidClientInputSex1", "");
					btrisSubject.put("guidClientInputSex2", "");
				}
			}

			jsonString = btrisSubject.toString();

		} catch (CtdbException ce) {
			logger.error("Error in getting subject from btris ", ce);
			errRespMsg = ce.getMessage();
			return ERROR;
		} catch (JSONException je) {
			logger.error("Error in constructing json for subject from btris ", je);
			errRespMsg = je.getMessage();
			return ERROR;
		}
		return SUCCESS;
	}
	
	public String getMrn() {
		return mrn;
	}
	public void setMrn(String mrn) {
		this.mrn = mrn;
	}
	public String getFirstName() {
		return firstName;
	}
	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}
	public String getLastName() {
		return lastName;
	}
	public void setLastName(String lastName) {
		this.lastName = lastName;
	}
	public String getMrnG() {
		return mrnG;
	}
	public void setMrnG(String mrnG) {
		this.mrnG = mrnG;
	}
	public String getFirstNameG() {
		return firstNameG;
	}
	public void setFirstNameG(String firstNameG) {
		this.firstNameG = firstNameG;
	}
	public String getLastNameG() {
		return lastNameG;
	}
	public void setLastNameG(String lastNameG) {
		this.lastNameG = lastNameG;
	}	
	public String getJsonString() {
		return jsonString;
	}
	public void setJsonString(String jsonString) {
		this.jsonString = jsonString;
	}
	public String getErrRespMsg() {
		return errRespMsg;
	}
	public void setErrRespMsg(String errRespMsg) {
		this.errRespMsg = errRespMsg;
	}

	private Integer getStateId(String stateName) throws CtdbException {
		Integer stateId = 0;
		LookupManager luMan = new LookupManager();
		List<CtdbLookup> states = luMan.getLookups(LookupType.STATE);
		for ( CtdbLookup lookUp : states )
		{
			if (lookUp.getShortName().equalsIgnoreCase(stateName.trim())) { 
				stateId = lookUp.getId();
			}
		}
		return stateId;
	}
	private Integer getCountryId(String countryName) throws CtdbException {
		Integer countryId = 0;
		LookupManager luMan = new LookupManager();
		List<CtdbLookup> countries = luMan.getLookups(LookupType.COUNTRY);
		for ( CtdbLookup lookUp : countries )
		{
			if (lookUp.getShortName().equalsIgnoreCase(countryName.trim())) { 
				countryId = lookUp.getId();
			}
		}
		return countryId;
	}
}
