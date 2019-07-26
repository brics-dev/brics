package gov.nih.nichd.ctdb.form.domain;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import gov.nih.nichd.ctdb.common.CtdbDomainObject;
import gov.nih.nichd.ctdb.common.CtdbLookup;
import gov.nih.nichd.ctdb.common.TransformationException;
import gov.nih.nichd.ctdb.form.common.FormHtmlAttributes;
import gov.nih.nichd.ctdb.protocol.domain.Interval;
import gov.nih.nichd.ctdb.protocol.domain.Protocol;
import gov.nih.nichd.ctdb.question.domain.Question;
import gov.nih.nichd.ctdb.response.domain.DataEntryWorkflowType;
/**
 * Form DomainObject for the NICHD CTDB Application
 * 
 * @author Booz Allen Hamilton
 * @version 1.0
 */
public class Form extends CtdbDomainObject {
	private static final long serialVersionUID = -5991846354995397759L;
	
	private String name;
	private String description;
	private CtdbLookup status;
	private FormHtmlAttributes formHtmlAttributes;
	private List<List<Section>> rowList = new ArrayList<List<Section>>();
	private List<Interval> intervalList = new ArrayList<Interval>();
	private int checkOutBy = Integer.MIN_VALUE;
	private Date checkOutDate;
	private int numQuestions;
	private boolean lockFlag = false;
	private int orderValue;
	private boolean administered = false;
	private Date importedDate;
	private String importFileName;
	private String checkedOutByUsername;
	private String formFileUploadPath;
	private int singleDoubleKeyFlag = 1;
	private int accessFlag = 1;
	private boolean displayQids = false;
	private boolean securityAccess = true;

	private String formHeader;
	private String formFooter;
	private int[] formGroups;

	private Map<String, CellFormatting> cellFormatting = new HashMap<String, CellFormatting>();
	private DataEntryWorkflowType dataEntryWorkflow = DataEntryWorkflowType.STANDARD;

	private boolean hasSkipRule = false;
	private boolean hasCalculationRule = false;
	private boolean hasImageMap = false;

	private boolean attachFiles = false;

	private boolean dataSpring = false; // automatically populate adminstered
										// forms from previously
										// administeredofrms

	private boolean tabDisplay = false;

	private Protocol protocol = new Protocol();

	private Document formXml = null;
	private HashMap<Integer, Integer> questionLocator = new HashMap<Integer, Integer>();

	private boolean inCtss = false;
	private int formType;
	private String formTypeName;

	// ibis
	private String formGroupNames;
	private boolean isMandatory = false;
	private boolean isSelfReport = false;
	private Date updateDate;

	private String dataStructureName = "";
	private String dataStructureVersion = "";

	// add for copyright
	private boolean copyRight = false;
	
	//for allowing multiple instances of data collections for same form
	private boolean allowMultipleCollectionInstances = false;
	
	//boolean indicating if this form was created with the new auto-generated form builder or is a legacy form
	private boolean isLegacy = false;
	
	private String shortName;
	
	//map where key = "S_{sectionid}_Q_{questionid} annd value is Question
	private HashMap<String,Question> questionMap;
	
	//map where key is sectionid
	private HashMap<Integer,Section> sectionMap;
	
	//ordered sectionList
	private ArrayList<Section> orderedSectionList;
	
	// added by Ching-Heng
	private boolean isCAT;
	
	private String catOid;
	
	private String measurementType;

	/**
	 * Default Constructor for the Form Domain Object
	 */
	public Form() {
		// default constructor
	}

	public List<Section> getOrderedSectionList() {
		return orderedSectionList;
	}

	public void setOrderedSectionList(ArrayList<Section> orderedSectionList) {
		this.orderedSectionList = orderedSectionList;
	}

	/**
	 * Gets the form name
	 * 
	 * @return The form name
	 */
	public String getName() {
		return name;
	}

	public String getShortName() {
		return shortName;
	}

	public void setShortName(String shortName) {
		this.shortName = shortName;
	}

	public HashMap<String, Question> getQuestionMap() {
		return questionMap;
	}

	public HashMap<Integer, Section> getSectionMap() {
		return sectionMap;
	}

	public void setSectionMap(HashMap<Integer, Section> sectionMap) {
		this.sectionMap = sectionMap;
	}

	public void setQuestionMap(HashMap<String, Question> questionMap) {
		this.questionMap = questionMap;
	}

	/**
	 * Sets the form name
	 * 
	 * @param name
	 *            The form name
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * Gets the form's description
	 * 
	 * @return The form's description
	 */
	public String getDescription() {
		return description;
	}

	/**
	 * Sets the form's description
	 * 
	 * @param description
	 *            The form's description
	 */
	public void setDescription(String description) {
		this.description = description;
	}

	/**
	 * Gets the form's status
	 * 
	 * @return The form status
	 */
	public CtdbLookup getStatus() {
		return status;
	}

	/**
	 * Sets the form's status
	 * 
	 * @param status
	 *            The form status
	 */
	public void setStatus(CtdbLookup status) {
		this.status = status;
	}

	/**
	 * Gets the form html attributes.
	 * 
	 * @return The html attributes for the form
	 */
	public FormHtmlAttributes getFormHtmlAttributes() {
		return formHtmlAttributes;
	}

	/**
	 * Sets the form html attributes
	 * 
	 * @param formHtmlAttributes
	 *            FormHtmlAttributes object wraps the html attributes
	 */
	public void setFormHtmlAttributes(FormHtmlAttributes formHtmlAttributes) {
		this.formHtmlAttributes = formHtmlAttributes;
	}

	/**
	 * Gets the section list for the form
	 * 
	 * @return Sections for the form
	 */
	public List<List<Section>> getRowList() {
		return rowList;
	}

	/**
	 * Sets the section list for the form
	 * 
	 * @param rowList
	 *            Section list for the form
	 */
	public void setRowList(List<List<Section>> rowList) {
		this.rowList = rowList;
	}

	/**
	 * Gets the interval list for the form
	 * 
	 * @return List of intervals for the form
	 */
	public List<Interval> getIntervalList() {
		return intervalList;
	}

	/**
	 * Sets the interval list for the form
	 * 
	 * @param intervalList
	 *            the interval list for the form
	 */
	public void setIntervalList(List<Interval> intervalList) {
		this.intervalList = intervalList;
	}

	/**
	 * Gets the user ID who has the form checked out
	 * 
	 * @return The user ID who has the form checked out
	 */
	public int getCheckOutBy() {
		return checkOutBy;
	}

	/**
	 * Sets the user ID who checks out the form
	 * 
	 * @param checkOutBy
	 *            the user ID who checks out the form
	 */
	public void setCheckOutBy(int checkOutBy) {
		this.checkOutBy = checkOutBy;
	}

	/**
	 * Gets form checked out date
	 * 
	 * @return Form checked out date
	 */
	public Date getCheckOutDate() {
		return checkOutDate;
	}

	/**
	 * Sets form checked out date
	 * 
	 * @param checkOutDate
	 *            The form checked out date
	 */
	public void setCheckOutDate(Date checkOutDate) {
		this.checkOutDate = checkOutDate;
	}

	/**
	 * Gets the total number of questions for the form
	 * 
	 * @return Total number of questions for the form
	 */
	public int getNumQuestions() {
		return numQuestions;
	}

	/**
	 * Sets the total number of questions for the form
	 * 
	 * @param numQuestions
	 *            total number of questions for the form
	 */
	public void setNumQuestions(int numQuestions) {
		this.numQuestions = numQuestions;
	}

	/**
	 * Gets the lock flag for the form.
	 * 
	 * @return The form lock flag
	 */
	public boolean getLockFlag() {
		return lockFlag;
	}

	/**
	 * Sets the form lock flag.
	 * 
	 * @param lockFlag
	 *            the form lock flag
	 */
	public void setLockFlag(boolean lockFlag) {
		this.lockFlag = lockFlag;
	}



	/**
	 * Gets the protocol ID.
	 * 
	 * @return The protocol ID
	 */
	public int getProtocolId() {
		return protocol.getId();
	}

	/**
	 * Sets the protocol ID.
	 * 
	 * @param protocolId
	 *            the protocol ID
	 */
	public void setProtocolNumber(String protocolNumber) {
		this.protocol.setProtocolNumber(protocolNumber);
	}

	public String getProtocolNumber() {
		return protocol.getProtocolNumber();
	}

	public boolean isAllowMultipleCollectionInstances() {
		return allowMultipleCollectionInstances;
	}

	public void setAllowMultipleCollectionInstances(
			boolean allowMultipleCollectionInstances) {
		this.allowMultipleCollectionInstances = allowMultipleCollectionInstances;
	}
	
	
	

	public boolean isLegacy() {
		return isLegacy;
	}

	public void setLegacy(boolean isLegacy) {
		this.isLegacy = isLegacy;
	}

	/**
	 * Sets the protocol ID.
	 * 
	 * @param protocolId
	 *            the protocol ID
	 */
	public void setProtocolId(int protocolId) {
		this.protocol.setId(protocolId);
	}
	
	
	/**
	 * Gets the form order value.
	 * 
	 * @return The form order value
	 */
	public int getOrderValue() {
		return orderValue;
	}

	/**
	 * Gets the isAdministered flag for the form.
	 * 
	 * @return The form isAdministered flag
	 */
	public boolean isAdministered() {
		return administered;
	}

	/**
	 * Sets the form's administered flag.
	 * 
	 * @param administered
	 *            the form is administered flag
	 */
	public void setIsAdministered(boolean administered) {
		this.administered = administered;
	}

	/**
	 * Sets the form order value.
	 * 
	 * @param orderValue
	 *            the form order value
	 */
	public void setOrderValue(int orderValue) {
		this.orderValue = orderValue;
	}

	/**
	 * Gets form's imported date
	 * 
	 * @return Form imported date
	 */
	public Date getImportedDate() {
		return importedDate;
	}

	/**
	 * Sets form's imported date
	 * 
	 * @param importedDate
	 *            The form's imported date
	 */
	public void setImportedDate(Date importedDate) {
		this.importedDate = importedDate;
	}

	/**
	 * Gets the form's imported file name
	 * 
	 * @return The form's imported file name
	 */
	public String getImportFileName() {
		return importFileName;
	}

	/**
	 * Sets the form's imported file name
	 * 
	 * @param importFileName
	 *            The form's imported file name
	 */
	public void setImportFileName(String importFileName) {
		this.importFileName = importFileName;
	}

	/**
	 * Gets the form exported's username
	 * 
	 * @return The form exported's username
	 */
	public String getCheckedOutByUsername() {
		return checkedOutByUsername;
	}

	/**
	 * Sets the form exported's username
	 * 
	 * @param checkedOutByUsername
	 *            The form exported's username
	 */
	public void setCheckedOutByUsername(String checkedOutByUsername) {
		this.checkedOutByUsername = checkedOutByUsername;
	}

	/**
	 * Gets the form file upload path, which is a file system path on the server
	 * 
	 * @return The form file's upload path.
	 */
	public String getFormFileUploadPath() {
		return formFileUploadPath;
	}

	/**
	 * Sets the form file upload path, which is a file system path on the server
	 * 
	 * @param formFileUploadPath
	 *            The form file upload path
	 */
	public void setFormFileUploadPath(String formFileUploadPath) {
		this.formFileUploadPath = formFileUploadPath;
	}

	/**
	 * Gets the data entry flag for the form.
	 * 
	 * @return int The form data entry flag, 1 for single key, 2 for double key
	 */
	public int getSingleDoubleKeyFlag() {
		return singleDoubleKeyFlag;
	}

	/**
	 * Sets the form data entry flag.
	 * 
	 * @param singleDoubleKeyFlag
	 *            the form data entry flag
	 */
	public void setSingleDoubleKeyFlag(int singleDoubleKeyFlag) {
		this.singleDoubleKeyFlag = singleDoubleKeyFlag;
	}

	/**
	 * Gets the access flag for the form.
	 * 
	 * @return int The form access flag, 1 for private, 2 for public
	 */
	public int getAccessFlag() {
		return accessFlag;
	}

	/**
	 * Sets the form access flag.
	 * 
	 * @param accessFlag
	 *            the form access flag
	 */
	public void setAccessFlag(int accessFlag) {
		// System.out.println("!!!!!!!!!!!!!!set access flag to " + accessFlag);
		this.accessFlag = accessFlag;
	}

	/**
	 * Gets the security access flag for the form.
	 * 
	 * @return The form security access flag
	 */
	public boolean getSecurityAccess() {
		return securityAccess;
	}

	/**
	 * Sets the form's security access flag.
	 * 
	 * @param securityAccess
	 *            the form security access flag
	 */
	public void setSecurityAccess(boolean securityAccess) {
		this.securityAccess = securityAccess;
	}

	/**
	 * Gets the protocol name
	 * 
	 * @return The protocol name
	 */
	public String getProtocolName() {
		return protocol.getName();
	}

	/**
	 * Sets the protocol name
	 * 
	 * @param name
	 *            The protocol name
	 */
	public void setProtocolName(String name) {
		this.protocol.setName(name);
	}

	public boolean displayQids() {
		return displayQids;
	}

	public void setDisplayQids(boolean displayQids) {
		this.displayQids = displayQids;
	}

	/**
	 * Sets the form's header
	 * 
	 * @param header
	 *            The form header
	 */
	public void setFormHeader(String header) {
		this.formHeader = header;
	}

	/**
	 * Gets the form header
	 * 
	 * @return The form header
	 */
	public String getFormHeader() {
		return formHeader;
	}

	/**
	 * Sets the form footer
	 * 
	 * @param footer
	 *            The form footer
	 */
	public void setFormFooter(String footer) {
		this.formFooter = footer;
	}

	/**
	 * Gets the form footer
	 * 
	 * @return The form footer
	 */
	public String getFormFooter() {
		return formFooter;
	}

	public int getFormType() {
		return formType;
	}

	public void setFormType(int formType) {
		this.formType = formType;
	}

	public int[] getFormGroups() {
		return formGroups;
	}

	public String getFormGroupNames() {
		return formGroupNames;
	}

	public void setFormGroupNames(String formGroupNames) {
		this.formGroupNames = formGroupNames;
	}


	public String getDataStructureName() {
		return dataStructureName;
	}

	public String getDataStructureVersion() {
		return dataStructureVersion;
	}

	public void setDataStructureVersion(String dataStructureVersion) {
		this.dataStructureVersion = dataStructureVersion;
	}

	public void setDataStructureName(String dataStructureName) {
		this.dataStructureName = dataStructureName;
	}

	public void setFormGroups(int[] formGroups) {
		this.formGroups = formGroups;
	}

	public Map<String, CellFormatting> getCellFormatting() {
		return cellFormatting;
	}

	public void setCellFormatting(Map<String, CellFormatting> cellFormatting) {
		this.cellFormatting = cellFormatting;
	}

	public DataEntryWorkflowType getDataEntryWorkflow() {
		return dataEntryWorkflow;
	}

	public void setDataEntryWorkflow(DataEntryWorkflowType dataEntryWorkflow) {
		this.dataEntryWorkflow = dataEntryWorkflow;
	}

	public boolean isHasSkipRule() {
		return hasSkipRule;
	}

	public void setHasSkipRule(boolean hasSkipRule) {
		this.hasSkipRule = hasSkipRule;
	}

	public boolean isHasCalculationRule() {
		return hasCalculationRule;
	}

	public void setHasCalculationRule(boolean hasCalculationRule) {
		this.hasCalculationRule = hasCalculationRule;
	}

	public boolean isHasImageMap() {
		return hasImageMap;
	}

	public void setHasImageMap(boolean hasImageMap) {
		this.hasImageMap = hasImageMap;
	}

	public boolean isAttachFiles() {
		return attachFiles;
	}

	public void setAttachFiles(boolean attachFiles) {
		this.attachFiles = attachFiles;
	}

	public boolean isDataSpring() {
		return dataSpring;
	}

	public void setDataSpring(boolean dataSpring) {
		this.dataSpring = dataSpring;
	}

	public boolean isTabDisplay() {
		return tabDisplay;
	}

	public void setTabDisplay(boolean tabDisplay) {
		this.tabDisplay = tabDisplay;
	}

	public Protocol getProtocol() {
		return protocol;
	}

	public void setProtocol(Protocol protocol) {
		this.protocol = protocol;
	}

	// contains questionid as key and row / cell as bucket
	public HashMap<Integer, Integer> getQuestionLocator() {
		return questionLocator;
	}

	public void setQuestionLocator(HashMap<Integer, Integer> questionLocator) {
		this.questionLocator = questionLocator;
	}

	public boolean isInCtss() {
		return inCtss;
	}

	public void setInCtss(boolean inCtss) {
		this.inCtss = inCtss;
	}
	
	public boolean isCAT() {
		return isCAT;
	}

	public void setCAT(boolean isCAT) {
		this.isCAT = isCAT;
	}	

	public String getCatOid() {
		return catOid;
	}

	public void setCatOid(String catOid) {
		this.catOid = catOid;
	}

	public String getMeasurementType() {
		return measurementType;
	}

	public void setMeasurementType(String measurementType) {
		this.measurementType = measurementType;
	}

	public Question getQuestion(int questionId) {
		Question question = null;
		
		if (questionLocator.isEmpty() || questionLocator.size() < 1) {
			populateQuestionLocator();
		}
		
		int qLocation = questionLocator.get(new Integer(questionId)).intValue();
		question = rowList.get(qLocation >> 18).get((qLocation & 262143) >> 9).getQuestionList().get(qLocation & 511);
		
		return question;
	}

	private void populateQuestionLocator() {
		List<Question> questions;
		int rowNum = 0;
		
		for ( List<Section> sections : rowList ) {
			int i = 0;
			
			for ( Section section : sections ) {
				if (section != null) {
					questions = section.getQuestionList();
					int questionOrder = 0;
					
					for ( Question question : questions ) {
						questionLocator.put(question.getId(), ((rowNum << 18) | (i << 9) | (questionOrder)));
						questionOrder++;
					}
				}
				
				i++;
			}
			
			rowNum++;
		}
	}

	public Document getFormXml() throws TransformationException {
		this.formXml = toXML();
		
		return this.formXml;
	}

	public void setFormXml(Document formXml) {
		this.formXml = formXml;
	}

	public void cloneMetaData(Form f) {

		this.setId(f.getId());
		if (f.getName() != null) {
			this.name = new String(f.getName());
		}
		if (f.getDescription() != null) {
			this.description = new String(f.getDescription());
		}
		if (f.getStatus() != null) {
			this.status = new CtdbLookup();
			this.status.clone(f.getStatus());
		}
		this.protocol.setId(f.getProtocolId());
		if (f.getProtocolName() != null) {
			this.protocol.setName(new String(f.getProtocolName()));
		}

	}

	/**
	 * Determines if an object is equal to the current Form Object.
	 * 
	 * @param o
	 *            The object to determine if it is equal to the current form
	 * @return True if the object is equal to the form. False if the object is
	 *         not equal to the form.
	 */
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}

		if (!(o instanceof Form)) {
			return false;
		}

		Form form = (Form) o;

		if (this.getId() != form.getId()) {
			return false;
		}
		if (this.name != null && form.getName() != null) {
			if (!this.name.equals(form.getName())) {
				return false;
			}
		} else if (this.name == null && form.getName() != null) {
			return false;
		} else if (this.name != null && form.getName() == null) {
			return false;
		}
		if (!(this.status.getId() == form.status.getId())) {
			return false;
		}
		// description is not required
		if (this.description != null && form.getDescription() != null) {
			if (!this.description.equals(form.getDescription())) {
				return false;
			}
		} else if (this.description == null && form.getDescription() != null) {
			if (!form.getDescription().trim().equals("")) {
				return false;
			}
		} else if (this.description != null && form.getDescription() == null) {
			if (!this.description.trim().equals("")) {
				return false;
			}
		}

		return true;
	}

	/**
	 * This method returns a map from question ID to question in the form
	 * 
	 * @return a Map of question Id(Integer) to question domain object
	 */
	public Map<Integer, Question> getMapQuestionIdQuestion() {
		Map<Integer, Question> map = new HashMap<Integer, Question>();
		
		for ( List<Section> sectionList : rowList ) {
			for ( Section section : sectionList ) {
				if (section != null) {
					for ( Question question : section.getQuestionList() ) {
						map.put(new Integer(question.getId()), question);
					}
				}
			}
		}
		
		return map;
	}

	public void removeDefaultValues() {
		for ( List<Section> sectionList : rowList ) {
			for ( Section section : sectionList ) {
				if (section != null) {
					for ( Question question : section.getQuestionList() ) {
						question.setDefaultValue("");
					}
				}
			}
		}
	}

	/**
	 * This method returns all question IDs in the form
	 * 
	 * @return a Set of all question Ids as Integer in the form
	 */
	public Set<Integer> getQuestionIds() {
		Set<Integer> set = new HashSet<Integer>();
		
		for ( List<Section> sectionList : rowList ) {
			for ( Section section : sectionList ) {
				if (section != null) {
					for ( Question question : section.getQuestionList() ) {
						set.add(new Integer(question.getId()));
					}
				}
			}
		}
		
		return set;
	}

	/**
	 * This method returns all question IDs in the form
	 * 
	 * @return	A set of all question Ids as a string in the form
	 */
	public Set<String> getSectionQuestionIds() {
		Set<String> set = new HashSet<String>();
		
		for ( List<Section> sectionList : rowList ) {
			for ( Section section : sectionList ) {
				if (section != null) {
					for ( Question question : section.getQuestionList() ) {
						set.add("S_" + section.getId() + "_Q_" + question.getId());
					}
				}
			}
		}
		
		return set;
	}

	/**
	 * This method allows the transformation of a Form into an XML Document. If no implementation is available 
	 * at this time, an UnsupportedOperationException will be thrown.
	 * 
	 * @return XML Document
	 * @exception TransformationException	Is thrown if there is an error during the XML transformation
	 * @exception UnsupportedOperationException	Is thrown if this method is currently unsupported and not implemented.
	 */
	public Document toXML() throws TransformationException {
		try {
			Document document = super.newDocument();
			Element root = super.initXML(document, "form");

			Element nameNode = document.createElement("name");
			nameNode.appendChild(document.createTextNode(this.name));
			root.appendChild(nameNode);
			
			// Adding Ching-Heng
			Element catOidNod = document.createElement("catOid");
			catOidNod.appendChild(document.createTextNode(this.getCatOid()));
			root.appendChild(catOidNod);
			
			Element dataStructureNod = document.createElement("dataStructureName");
			dataStructureNod.appendChild(document.createTextNode(this.getDataStructureName()));
			root.appendChild(dataStructureNod);
			
			Element shortNameNod = document.createElement("shortName");
			shortNameNod.appendChild(document.createTextNode(this.getShortName()));
			root.appendChild(shortNameNod);
						
			Element measurementTypeNod = document.createElement("measurementType");
			measurementTypeNod.appendChild(document.createTextNode(this.getMeasurementType()));
			root.appendChild(measurementTypeNod);
						
			Element formIdNode = document.createElement("formId");
			formIdNode.appendChild(document.createTextNode(String.valueOf(this.getId())));
			root.appendChild(formIdNode);
			
			root.setAttribute("displayQids",
					(Boolean.toString(this.displayQids)).toLowerCase());

			Element descriptionNode = document.createElement("description");
			if (this.description != null && !this.description.equals("")) {
				descriptionNode.appendChild(document
						.createTextNode(this.description));
			}
			root.appendChild(descriptionNode);

			Document statusDom = this.status.toXML("status");
			root.appendChild(document.importNode(
					statusDom.getDocumentElement(), true));

			Document formHtmlAttDom = this.formHtmlAttributes.toXML();
			root.appendChild(document.importNode(
					formHtmlAttDom.getDocumentElement(), true));

			Element headerNode = document.createElement("formHeader");
			if (this.formHeader != null && !this.formHeader.equals("")) {
				headerNode
						.appendChild(document.createTextNode(this.formHeader));
			}
			root.appendChild(headerNode);

			this.createTOC(document, root);
			Element footerNode = document.createElement("formFooter");
			if (this.formFooter != null && !this.formFooter.equals("")) {
				footerNode
						.appendChild(document.createTextNode(this.formFooter));
			}
			root.appendChild(footerNode);
			int rowNum = 1;
			int repeatSecCount = 1;
			
			for ( List<Section> sectionList : rowList ) {
				Element aRow = document.createElement("row");
				aRow.setAttribute("rowNum", Integer.toString(rowNum));
				aRow.setAttribute("tabLabel", "Empty");
				
				int i = 0;
				for ( Section sec : sectionList ) {
					Element formCell = document.createElement("formcell");
					CellFormatting cf = cellFormatting.get(Integer.toString(rowNum) + "-" + Integer.toString(i + 1));
					
					if (cf != null) {
						formCell.setAttribute("theStyle", cf.getHtmlStyleString());
						formCell.setAttribute("theRowSpan", cf.getHtmlRowSpan());
						formCell.setAttribute("theColSpan", cf.getHtmlColSpan());

					} else {
						// added by Ching Heng make row width shows correctly
						int colunmLength = sectionList.size();
						
						for ( Section s : sectionList ) {
							if ( s == null ) {
								colunmLength--;
							}
						}
						
						formCell.setAttribute("theStyle","width:" + Integer.toString(100 / colunmLength)+ "%;");
						formCell.setAttribute("theRowSpan", "1");
					}
					
					if ( sec != null ) {
						if( sec.isRepeatable() && sec.getRepeatedSectionParent() == -1 ) {
							repeatSecCount=1;
						}
						else if ( sec.isRepeatable() && sec.getRepeatedSectionParent() != -1 ) {
							repeatSecCount++;
						}
						else{
							repeatSecCount=1;
						}
						
						
						if (aRow.getAttribute("tabLabel").equals("Empty")) {
							aRow.setAttribute("tabLabel", sec.getName());
						}
						
						Document sectionDom = sec.toXML(repeatSecCount,rowNum);
						sectionDom.getDocumentElement().appendChild(sectionDom.importNode(formHtmlAttDom.getDocumentElement(), true));
						formCell.appendChild(document.importNode(sectionDom.getDocumentElement(), true));
						formCell.setAttribute("isRepeatable",Boolean.toString(sec.isRepeatable()));
						
						int minValue = sec.getInitRepeatedSections();
						formCell.setAttribute("buttonCount",String.valueOf(rowNum));
						formCell.setAttribute("minimumValue", String.valueOf(sec.getInitRepeatedSections()));
						formCell.setAttribute("maximumValue", String.valueOf(sec.getMaxRepeatedSections()));
						formCell.setAttribute("parentValue", String.valueOf(sec.getRepeatedSectionParent()));
						formCell.setAttribute("formRow", String.valueOf(sec.getRow()));
						formCell.setAttribute("secId", String.valueOf(sec.getId()));
						
						if (repeatSecCount <= minValue) {
							formCell.setAttribute("visible", "true");
							
						}
						else {
							formCell.setAttribute("visible", "false");
							
						}
					}
					
					aRow.appendChild(formCell);
					i++;
				}
				
				rowNum++;
				root.appendChild(aRow);
			}

			Element intervalsNode = document.createElement("intervals");
			
			for (Iterator<Interval> it = intervalList.iterator(); it.hasNext();) {
				Interval interval = it.next();
				Document intervalDom = interval.toXML();
				intervalsNode.appendChild(document.importNode(
						intervalDom.getDocumentElement(), true));
			}
			
			root.appendChild(intervalsNode);

			Element numQuestionsNode = document
					.createElement("numberofquestion");
			numQuestionsNode.appendChild(document
					.createTextNode(this.numQuestions + ""));
			root.appendChild(numQuestionsNode);

			Element orderValNode = document.createElement("ordervalue");
			orderValNode.appendChild(document.createTextNode(this.orderValue
					+ ""));
			root.appendChild(orderValNode);

			Element protocolIdNode = document.createElement("protocolid");
			protocolIdNode.appendChild(document.createTextNode(this.protocol
					.getId() + ""));
			root.appendChild(protocolIdNode);

			// if status is active, cache xml.

			return document;
		}
		catch (Exception ex) {
			throw new TransformationException("Unable to transform object " + this.getClass().getName() + 
				" with id = " + this.getId());
		}
	}

	private void createTOC (Document document, Element root) {
		boolean createTOC = false;
		Element TOC = document.createElement("TOC");
		TOC.setAttribute("display", "false");
		
		for ( List<Section> sectionList : rowList ) {
			for ( Section s : sectionList ) {
				if ( s == null ) {
					continue;
				}

				if (s.isIntob()) {
					Element toAdd = document.createElement("TOCListing");
					toAdd.setAttribute("sectionid", Integer.toString(s.getId()));
					String label = "";
					
					if (s.getAltLabel() != null) {
						label = s.getAltLabel();
					}
					else {
						label = s.getName();
					}

					toAdd.setAttribute("displayvalue", label);
					TOC.appendChild(toAdd);
					createTOC = true;
				}
			}
		}

		if (createTOC) {
			root.appendChild(TOC);
		}
	}

	public String getFormTypeName() {
		return formTypeName;
	}

	public void setFormTypeName(String formTypeName) {
		this.formTypeName = formTypeName;
	}

	public boolean isMandatory() {
		return isMandatory;
	}

	public void setMandatory(boolean isMandatory) {
		this.isMandatory = isMandatory;
	}

	public boolean isSelfReport() {
		return isSelfReport;
	}

	public void setSelfReport(boolean isSelfReport) {
		this.isSelfReport = isSelfReport;
	}

	public Date getUpdateDate() {
		return updateDate;
	}

	public void setUpdateDate(Date updateDate) {
		this.updateDate = updateDate;
	}

	public boolean isCopyRight() {
		return copyRight;
	}

	public void setCopyRight(boolean copyRight) {
		this.copyRight = copyRight;
	}
	
	public String validateFormInformation(){
		String msg  = "";
		if(this.name == null || this.name.trim().isEmpty()){
			msg += "<br>Form name can not be empty";
		}
		if(this.formType == Integer.MIN_VALUE){
			msg += "<br>Form type is not define";
		}
		return msg;
	}	
}
