package gov.nih.nichd.ctdb.util.common;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Hashtable;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import gov.nih.nichd.ctdb.protocol.domain.BricsStudy;
import gov.nih.tbi.commons.model.ResearchManagementRole;

public class BricsStudyXmlHandler extends DefaultHandler {
	private static final Logger logger = Logger.getLogger(BricsStudyXmlHandler.class);
	
	private StringBuffer tagValue; // Used to store the value within the current tag.
	private BricsStudy currStudy;
	private Hashtable<String, BricsStudy> studyTable;
	private String studyPermission;
	private StringBuffer researchMemberName;
	private boolean isInFundingSource;
	private boolean isInStudyType;
	private boolean isInResearchMgt;
	private boolean isInPrimePI;
	private boolean isStudyIdAssigned;

	public BricsStudyXmlHandler()
	{
		super();
		
		// Initialize member variables
		tagValue = new StringBuffer();
		currStudy = null;
		studyTable = new Hashtable<String, BricsStudy>();
		studyPermission = "";
		researchMemberName = new StringBuffer();
		isInFundingSource = false;
		isInStudyType = false;
		isInResearchMgt = false;
		isInPrimePI = false;
		isStudyIdAssigned = false;
	}
	
	public void startElement(String uri, String localName,String qName, Attributes attributes) throws SAXException {
		if ( qName.equalsIgnoreCase("study") ) {
			currStudy = new BricsStudy();
		}
		else if ( qName.equalsIgnoreCase("fundingSource") ) {
			isInFundingSource = true;
		}
		else if ( qName.equalsIgnoreCase("studyType") ) {
			isInStudyType = true;
		}
		else if ( qName.equalsIgnoreCase("researchManagement") ) {
			isInResearchMgt = true;
		}
		
		// Initialize the tag value to consume the text that is in between the start and end elements.
		tagValue = new StringBuffer();
	}
	
	public void endElement(String uri, String localName, String qName) throws SAXException {
		if ( currStudy != null ) {
 			if ( qName.equalsIgnoreCase("study") ) {
				currStudy.setStudyPermission(studyPermission);
				studyTable.put(currStudy.getPrefixedId(), currStudy);
				currStudy = null;
				isStudyIdAssigned = false;
			}
			else if ( qName.equalsIgnoreCase("id") && !isStudyIdAssigned ) {
				currStudy.setId(Integer.parseInt(tagValue.toString()));
				isStudyIdAssigned = true;
			}
			else if ( qName.equalsIgnoreCase("title") ) {
				currStudy.setTitle(tagValue.toString());
			}
			else if ( qName.equalsIgnoreCase("prefixedId") ) {
				currStudy.setPrefixedId(tagValue.toString());
			}
			else if ( qName.equalsIgnoreCase("abstractText") ) {
				currStudy.setAbstractText(tagValue.toString());
			}
			else if ( qName.equalsIgnoreCase("recruitmentStatus") ) {
				currStudy.setRecruitmentStatus(tagValue.toString());
			}
			else if ( qName.equalsIgnoreCase("principalInvestigatorEmail") ) {
				currStudy.setPrincipalInvestigatorEmail(tagValue.toString());
			}
			else if ( qName.equalsIgnoreCase("dateCreated") ) {
				SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");
				StringBuffer sb = new StringBuffer(tagValue);
				
				try {
					sb.deleteCharAt(sb.lastIndexOf(":"));
					currStudy.setDateCreated(sdf.parse(sb.toString()));
				}
				catch ( Exception e ) {
					logger.warn("Couldn't convert 'dataCreated' to string: " + e.getLocalizedMessage());
				}
			}
			else if ( qName.equalsIgnoreCase("studyStatus") ) {
				currStudy.setStudyStatus(tagValue.toString());
			}
			else if ( qName.equalsIgnoreCase("name") && isInStudyType ) {
				currStudy.setStudyType(tagValue.toString());
			}
			else if ( qName.equalsIgnoreCase("studyType") ) {
				isInStudyType = false;
			}
			else if ( qName.equalsIgnoreCase("fundingSource") ) {
				isInFundingSource = false;
			}
			else if ( qName.equalsIgnoreCase("studyStartDate") ) {
				String val = tagValue.toString();
				
				if ( !StringUtils.isEmpty(val) ) {
					try {
						SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
						Date d = sdf.parse(val);
						currStudy.setStudyStartDate(sdf.format(d));
					}
					catch ( Exception e ) {
						logger.warn("Couldn't convert 'studyStartDate' to string: " + e.getLocalizedMessage());
					}
				}
				else {
					currStudy.setStudyStartDate("");
				}
			}
			else if ( qName.equalsIgnoreCase("studyEndDate") ) {
				String val = tagValue.toString();
				
				if ( !StringUtils.isEmpty(val) ) {
					try {
						SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
						Date d = sdf.parse(val);
						currStudy.setStudyEndDate(sdf.format(d));
					}
					catch ( Exception e ) {
						logger.warn("Couldn't convert 'studyEndDate' to string: " + e.getLocalizedMessage());
					}
				}
				else {
					currStudy.setStudyEndDate("");
				}
			}
			else if ( qName.equalsIgnoreCase("numberOfSubjects") ) {
				currStudy.setStudyNumberSubjects(Integer.parseInt(tagValue.toString()));
			}
			else if ( qName.equalsIgnoreCase("role") && isInResearchMgt ) {
				ResearchManagementRole role = ResearchManagementRole.valueOf(tagValue.toString());
				
				if ( role == ResearchManagementRole.PRIMARY_PRINCIPAL_INVESTIGATOR ) {
					isInPrimePI = true;
				}
			}
			else if ( qName.equalsIgnoreCase("firstName") && isInResearchMgt ) {
				researchMemberName.append(tagValue.toString().trim());
			}
			else if ( qName.equalsIgnoreCase("mi") && isInResearchMgt ) {
				String middleName = tagValue.toString().trim();
				
				if ( !StringUtils.isEmpty(middleName) ) {
					researchMemberName.append(" ").append(middleName);
				}
			}
			else if ( qName.equalsIgnoreCase("lastName") && isInResearchMgt ) {
				researchMemberName.append(" ").append(tagValue.toString().trim());
			}
			else if ( qName.equalsIgnoreCase("suffix") && isInResearchMgt ) {
				String suffix = tagValue.toString().trim();
				
				if ( !StringUtils.isEmpty(suffix) ) {
					researchMemberName.append(", ").append(suffix);
				}
			}
			else if ( qName.equalsIgnoreCase("email") && isInResearchMgt ) {
				if ( isInPrimePI ) {
					currStudy.setPrincipalInvestigatorEmail(tagValue.toString().trim());
				}
			}
			else if ( qName.equalsIgnoreCase("researchManagement") ) {
				isInResearchMgt = false;
				
				if ( isInPrimePI ) {
					currStudy.setPrincipalInvestigator(researchMemberName.toString());
					isInPrimePI = false;
				}
				
				researchMemberName = new StringBuffer();
			}
		}
	}
	
	public void characters(char[] buffer, int start, int length) throws SAXException {
		tagValue.append(buffer, start, length);
	}

	/**
	 * @return the currStudy
	 */
	public BricsStudy getCurrStudy() {
		return currStudy;
	}

	/**
	 * @param currStudy the currStudy to set
	 */
	public void setCurrStudy(BricsStudy currStudy) {
		this.currStudy = currStudy;
	}

	/**
	 * @return the studyTable
	 */
	public Hashtable<String, BricsStudy> getStudyTable() {
		return studyTable;
	}

	/**
	 * @param studyTable the studyTable to set
	 */
	public void setStudyTable(Hashtable<String, BricsStudy> studyTable) {
		this.studyTable = studyTable;
	}

	/**
	 * @return the studyPermission
	 */
	public String getStudyPermission() {
		return studyPermission;
	}

	/**
	 * @param studyPermission the studyPermission to set
	 */
	public void setStudyPermission(String studyPermission) {
		this.studyPermission = studyPermission;
	}

	public boolean isStudyIdAssigned() {
		return isStudyIdAssigned;
	}

	public void setStudyIdAssigned(boolean isStudyIdAssigned) {
		this.isStudyIdAssigned = isStudyIdAssigned;
	}

}
