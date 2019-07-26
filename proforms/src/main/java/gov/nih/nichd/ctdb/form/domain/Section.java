package gov.nih.nichd.ctdb.form.domain;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import gov.nih.nichd.ctdb.common.CtdbConstants;
import gov.nih.nichd.ctdb.common.CtdbDomainObject;
import gov.nih.nichd.ctdb.common.TransformationException;
import gov.nih.nichd.ctdb.question.domain.Question;

/**
 * Section DomainObject for the NICHD CTDB Application
 * 
 * @author Booz Allen Hamilton
 * @version 1.0
 */

public class Section extends CtdbDomainObject implements Comparable<Section> {
	private static final long serialVersionUID = 8084905442167894924L;
	
	private String name;
	private String description;
	private List<Question> questionList = new ArrayList<Question>();
	private int formId = Integer.MIN_VALUE;
	private int orderValue;
	private int row;
	private int col;
	private boolean textDisplayed = true;
	private String instructionalText;
	private boolean intob;
	private String altLabel;
	private boolean collapsable = false;
	private boolean isResponseImage = false;
	private ResponseImage responseImage;
	private boolean isRepeatable = false;
	private int initRepeatedSections = -1;
	private int maxRepeatedSections = -1;
	private int repeatedSectionParent = -1;
	private String repeatableGroupName = "None";
	private boolean isGridtype = false;
	private int tableGroupId = 0;
	private int tableHeaderType = 0;
	private String idText;
	private boolean hasAnyRequiredQuestions = false;
	

	

	/**
	 * Default Constructor for the Section Domain Object
	 */
	public Section() {
		// default constructor
	}
	
	/**
	 * S_$Sid
	 * @return
	 */
	public String getIdText() {
		return idText;
	}

	
	/**
	 * S_$Sid
	 * @param idText
	 */
	public void setIdText(String idText) {
		this.idText = idText;
	}

	
	
	
	

	public boolean isHasAnyRequiredQuestions() {
		return hasAnyRequiredQuestions;
	}

	public void setHasAnyRequiredQuestions(boolean hasAnyRequiredQuestions) {
		this.hasAnyRequiredQuestions = hasAnyRequiredQuestions;
	}

	/**
	 * Gets the section name
	 * 
	 * @return The section name
	 */
	public String getName() {
		return name;
	}

	/**
	 * Sets the section name
	 * 
	 * @param name
	 *            The section name
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * Gets the section's description
	 * 
	 * @return The section's description
	 */
	public String getDescription() {
		return description;
	}

	/**
	 * Sets the section's description
	 * 
	 * @param description
	 *            The section's description
	 */
	public void setDescription(String description) {
		this.description = description;
	}

	/**
	 * Gets the question list for the section
	 * 
	 * @return List of questions for the section
	 */
	public List<Question> getQuestionList() {
		return questionList;
	}

	/**
	 * Sets the question list for the section
	 * 
	 * @param questionList
	 *            question list for the section
	 */
	public void setQuestionList(List<Question> questionList) {
		this.questionList = questionList;
	}

	/**
	 * Gets the form ID.
	 * 
	 * @return The form ID
	 */
	public int getFormId() {
		return formId;
	}

	/**
	 * Sets the form ID.
	 * 
	 * @param formId
	 *            the form ID
	 */
	public void setFormId(int formId) {
		this.formId = formId;
	}

	/**
	 * Gets the section order value.
	 * 
	 * @return The section order value
	 */
	public int getOrderValue() {
		return orderValue;
	}

	/**
	 * Sets the section order value.
	 * 
	 * @param orderValue
	 *            the section order value
	 */
	public void setOrderValue(int orderValue) {
		this.orderValue = orderValue;
	}

	/**
	 * attribute defining display of text during print / online data entry
	 * 
	 * @return
	 */
	public boolean isTextDisplayed() {
		return textDisplayed;
	}

	/**
	 * get Attribute defining display of text during print/ onine data entry
	 * 
	 * @param textDisplayed
	 */

	public void setTextDisplayed(boolean textDisplayed) {
		this.textDisplayed = textDisplayed;
	}

	/**
	 * Gets the section's instructional text
	 * 
	 * @return The section's instructional text
	 */
	public String getInstructionalText() {
		return instructionalText;
	}

	/**
	 * Sets the section's instructional text
	 * 
	 * @param text
	 *            The section's instructional text
	 */
	public void setInstructionalText(String text) {
		this.instructionalText = text;
	}

	/**
	 * Should the name or alt label of the section be displayed in the table of
	 * contents for online date entry.
	 * 
	 * @return true / false
	 */
	public boolean isIntob() {
		return intob;
	}

	/**
	 * Set attribute defining the section name or alt label displayed in the
	 * table of contents for online data entry.
	 * 
	 * @param intob
	 *            true/false
	 */
	public void setIntob(boolean intob) {
		this.intob = intob;
	}

	/**
	 * Gets the section's alternative label
	 * 
	 * @return The section's alternative label
	 */
	public String getAltLabel() {
		return altLabel;
	}

	/**
	 * Sets the section's alternative label
	 * 
	 * @param altLabel
	 *            The section's alternative label
	 */
	public void setAltLabel(String altLabel) {
		this.altLabel = altLabel;
	}

	public boolean isCollapsable() {
		return collapsable;
	}

	public void setCollapsable(boolean collapsable) {
		this.collapsable = collapsable;
	}

	public boolean isResponseImage() {
		return isResponseImage;
	}

	public void setResponseImage(boolean responseImage) {
		isResponseImage = responseImage;
	}

	public ResponseImage getResponseImage() {
		return responseImage;
	}

	public void setResponseImage(ResponseImage responseImage) {
		this.responseImage = responseImage;
	}

	public boolean isRepeatable() {
		return isRepeatable;
	}

	public void setRepeatable(boolean isRepeatable) {
		this.isRepeatable = isRepeatable;
	}

	public int getInitRepeatedSections() {
		return initRepeatedSections;
	}

	public void setInitRepeatedSections(int initRepeatedSections) {
		this.initRepeatedSections = initRepeatedSections;
	}

	public int getMaxRepeatedSections() {
		return maxRepeatedSections;
	}

	public void setMaxRepeatedSections(int maxRepeatedSections) {
		this.maxRepeatedSections = maxRepeatedSections;
	}

	public int getRepeatedSectionParent() {
		return repeatedSectionParent;
	}

	public void setRepeatedSectionParent(int repeatedSectionParent) {
		this.repeatedSectionParent = repeatedSectionParent;
	}
	
	

	public String getRepeatableGroupName() {
		return repeatableGroupName;
	}

	public void setRepeatableGroupName(String repeatableGroupName) {
		this.repeatableGroupName = repeatableGroupName;
	}

	public boolean isGridtype() {
		return isGridtype;
	}

	public void setGridtype(boolean isGridtype) {
		this.isGridtype = isGridtype;
	}

	
	
	
	public int getTableGroupId() {
		return tableGroupId;
	}

	public void setTableGroupId(int tableGroupId) {
		this.tableGroupId = tableGroupId;
	}
	
	
	public int getTableHeaderType() {
		return tableHeaderType;
	}

	public void setTableHeaderType(int tableHeaderType) {
		this.tableHeaderType = tableHeaderType;
	}

	/**
	 * Determines if an object is equal to the current Section Object.
	 * 
	 * @param o
	 *            The object to determine if it is equal to the current section
	 * @return True if the object is equal to the section. False if the object
	 *         is not equal to the section.
	 */
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}

		if (!(o instanceof Section)) {
			return false;
		}

		Section section = (Section) o;

		if (this.getId() != section.getId()) {
			return false;
		}
		if (this.name != section.getName()) {
			return false;
		}
		if (this.description != null && section.getDescription() != null) {
			if (!this.description.equals(section.getDescription())) {
				return false;
			}
		} else if (this.description == null && section.getDescription() != null) {
			return false;
		} else if (this.description != null && section.getDescription() == null) {
			return false;
		}

		return true;
	}
	
	  public Document toXML() throws TransformationException, UnsupportedOperationException {
	    	throw new UnsupportedOperationException("The toXML() is not supported.");
	    }
	    

	/**
	 * This method allows the transformation of a User into an XML Document. If
	 * no implementation is available at this time, an
	 * UnsupportedOperationException will be thrown.
	 * 
	 * @return XML Document
	 * @exception TransformationException
	 *                is thrown if there is an error during the XML
	 *                tranformation
	 * @exception UnsupportedOperationException
	 *                is thrown if this method is currently unsupported and not
	 *                implemented.
	 */
	public Document toXML(int repeatSecCount, int rowNum) throws TransformationException {
		try {
			Document document = super.newDocument();
			Element root = super.initXML(document, "section");
			root.setAttribute("isCollapsable",
					Boolean.toString(this.isCollapsable()));
			root.setAttribute("isGridType",
					Boolean.toString(this.isGridtype()));
			root.setAttribute("isResponseImage",
					Boolean.toString(this.isResponseImage()));
			if (this.getResponseImage() != null
					&& this.getResponseImage().getFileName() != null) {
				root.setAttribute("responseImageUploaded", "true");
				Element responseImage = document
						.createElement("responseImageFile");
				responseImage.appendChild(document.createTextNode(this
						.getResponseImage().getFileName()));
				// Document d = this.getResponseImage().toXML();
				root.appendChild(responseImage);

			} else {
				root.setAttribute("responseImageUploaded", "false");
				Element responseImage = document
						.createElement("responseImageFile");
				responseImage.appendChild(document.createTextNode(""));

				root.appendChild(responseImage);
			}
			
		

			Element nameNode = document.createElement("name");
			nameNode.appendChild(document.createTextNode(this.name));
			root.appendChild(nameNode);

			Element secidNode = document.createElement("sectionid");
			secidNode.appendChild(document.createTextNode(Integer.toString(this
					.getId())));
			root.appendChild(secidNode);

			Element descNode = document.createElement("description");
			if (this.description != null && !this.description.equals("")) {
				descNode.appendChild(document.createTextNode(this.description));
			}
			root.appendChild(descNode);

			Element textNode = document.createElement("instructionalText");
			textNode.appendChild(document
					.createTextNode(this.instructionalText));
			root.appendChild(textNode);

			root.setAttribute("textDisplayed",
					Boolean.toString(this.isTextDisplayed()));
			root.setAttribute("inTableOfContents",
					Boolean.toString(this.isIntob()));
			
			// determine the section table class name based on tableGroupId and tableHeaderType
			//root.setAttribute("tableGroupId", Integer.toString(this.tableGroupId));
			//root.setAttribute("tableHeaderType", Integer.toString(this.tableHeaderType));
			root.setAttribute("tableHeaderClassName", this.getTableClassName());

			Element altLabelNode = document.createElement("alternativeLabel");
			altLabelNode.appendChild(document.createTextNode(this.altLabel));
			root.appendChild(altLabelNode);

			//Element questionsNode = document.createElement("questions");
			
			String maxVal= String.valueOf(this.getMaxRepeatedSections());
			Element maxValNode = document.createElement("maximumValue");
			maxValNode.appendChild(document.createTextNode(maxVal));
			root.appendChild(maxValNode);
			
			String minVal= String.valueOf(this.getInitRepeatedSections());
			Element minValNode = document.createElement("minimumValue");
			minValNode.appendChild(document.createTextNode(minVal));
			root.appendChild(minValNode);
			
			String rowNumCnt= String.valueOf(rowNum);
			Element rowNumCntNode = document.createElement("buttonCount");
			rowNumCntNode.appendChild(document.createTextNode(rowNumCnt));
			root.appendChild(rowNumCntNode);

			String isRepeatable= Boolean.toString((this.isRepeatable()));
			Element isRepeatableNode = document.createElement("isSecRepeatable");
			isRepeatableNode.appendChild(document.createTextNode(isRepeatable));
			root.appendChild(isRepeatableNode);
			
			String repeatSecCountStr= String.valueOf(repeatSecCount);
			Element repeatSecCountNode = document.createElement("repeatSecCount");
			repeatSecCountNode.appendChild(document.createTextNode(repeatSecCountStr));
			root.appendChild(repeatSecCountNode);
			
			
			Element parentValueNode = document.createElement("parentValue");
			if(this.repeatedSectionParent==-1){
			parentValueNode.appendChild(document.createTextNode(String.valueOf(this.getId())));
			}else{
			parentValueNode.appendChild(document.createTextNode(String.valueOf(this.repeatedSectionParent)));
			
			}
			root.appendChild(parentValueNode);
			
			Element orderValueNode = document.createElement("formRow");
			orderValueNode.appendChild(document.createTextNode(String.valueOf(this.row)));
			root.appendChild(orderValueNode);
			
			
			
			
			
			
			String isCurrentLast="false";
			if(repeatSecCount==this.initRepeatedSections){
				isCurrentLast="true";
			}
			Element isCurrentLastNode = document.createElement("isCurrentLast");
			isCurrentLastNode.appendChild(document.createTextNode(isCurrentLast));
			root.appendChild(isCurrentLastNode);
			
			/*int count = 1;
			for (Iterator it = this.questionList.iterator(); it.hasNext();) {
				Question question = (Question) it.next();
				int secId = this.getId();
				String bgColor = "#FFFFFF";
				if (count % 2 == 0) {
					bgColor = "#E2E4FF";
				} // odd or even
				Document questionDom = question.toXML(secId, bgColor);
				questionsNode.appendChild(document.importNode(questionDom.getDocumentElement(), true));
				
				count++;
			}*/
			
			//=====Iterate through each questionList and put them in Hashmap where  key is row no. and values are list of question in each row
			Map<Integer,ArrayList<Question>> map = new TreeMap<Integer,ArrayList<Question>>();
			
			
			class QuestionDisplayOrderComparator implements Comparator<Question> {
				@Override
				/**
				 * Compares question 1 and question 2 to order them properly in
				 * page display order.
				 * 
				 * when comparing two questions in different sections, section
				 * order is the ONLY factor.  If the questions are in the same
				 * section, questionOrder is primary and questionOrderCol is
				 * secondary.
				 * 
				 * @param qOne the first question
				 * @param qTwo the second question
				 * @return a positive integer if the first question comes before the second, 
				 * a negative integer if the first question comes after the second, 
				 * or zero if they are the same (different sections)
				 */
				public int compare(Question qOne, Question qTwo) {
					if (qOne.getSectionId() != qTwo.getSectionId()) {
						// can't compare these two really, so give a zero
						return 0;
					}
					
					// rows different?
					if (qOne.getQuestionOrder() > qTwo.getQuestionOrder()) {
						return 1;
					}
					else if (qOne.getQuestionOrder() < qTwo.getQuestionOrder()) {
						return -1;
					}
					
					// otherwise, same row...cols different?
					if (qOne.getQuestionOrderCol() > qTwo.getQuestionOrderCol()) {
						return 1;
					}
					else if (qOne.getQuestionOrderCol() < qTwo.getQuestionOrderCol()) {
						return -1;
					}
					
					// fall back just in case
					return 0;
				}
				
			};
			
			Collections.sort(questionList, new QuestionDisplayOrderComparator());
			// at this point, we are certain that the questions are in the questionList in display order
			
			
			for (Iterator it = this.questionList.iterator(); it.hasNext();) {
				
				Question question = (Question) it.next();
				Integer questionRowOrder = question.getQuestionOrder();
				
				if(map.containsKey(questionRowOrder)){
					ArrayList<Question> qList = map.get(questionRowOrder);
					qList.add(question);
				}else{
					ArrayList<Question> qList = new ArrayList<Question>();
					qList.add(question);
					map.put(questionRowOrder, qList);
					
				}
			}
			
			//Find maximum no of column for each row in the grid
			int maxNoofColumnForEachRowInSection=0;
			for(ArrayList<Question> aL : map.values()){
				if(aL.size()>maxNoofColumnForEachRowInSection){
					maxNoofColumnForEachRowInSection = aL.size();
				}
			}
			
			root.setAttribute("maxNoofColumnForEachRowInSection", String.valueOf(CtdbConstants.MAX_COL_Q_IN_SECTION));
			
			
			//For each row in hash map iterate and create new xml document structure to address grid display
			for (Integer key : map.keySet()) {
				Element sectionRowsNode = document.createElement("sectionRows");
				Element questionsNode = document.createElement("questions");
				ArrayList<Question> qList = map.get(key);
				int qListSize = qList.size();
									
				//calculate colSpan for <td>
				int floorColSpanTD = (int) java.lang.Math.floor(CtdbConstants.MAX_COL_Q_IN_SECTION/qListSize);
				int widthTD = 100/qListSize;
				
				for (Question q:qList) {
				    
				    int questionRowOrder = q.getQuestionOrder();
				    String bgColor = "#FFFFFF";
				    if (questionRowOrder % 2 == 0) {
				        bgColor = "#E2E4FF";
				    } 
	                                
					Document questionDom = q.toXML(this.getId(), bgColor, floorColSpanTD, widthTD);
					questionsNode.appendChild(document.importNode(questionDom.getDocumentElement(), true));
				}
				sectionRowsNode.appendChild(questionsNode);
				root.appendChild(sectionRowsNode);
			}
														
			return document;

		} catch (Exception ex) {
			ex.printStackTrace();
			throw new UnsupportedOperationException(
					"Unable to transform object " + this.getClass().getName()
							+ " with id = " + this.getId());
		}
	}

	protected String getTableClassName() {
		String className = "noHeader";
		if (this.tableGroupId != 0) {
			int tableHeaderType = this.tableHeaderType;
			if (tableHeaderType == 1) {
				className = "rowHeader";
			}
			else if (tableHeaderType == 2) {
				className = "columnHeader";
			}
			else if (tableHeaderType == 3) {
				className = "tableHeader";
			}
			else if (tableHeaderType == 0) {
				className = "tablePrimary";
			}
		}
		return className;
	}
	
	/**
	 * Getter for property row.
	 * 
	 * @return Value of property row.
	 */
	public int getRow() {
		return row;
	}

	/**
	 * Setter for property row.
	 * 
	 * @param row
	 *            New value of property row.
	 */
	public void setRow(int row) {
		this.row = row;
	}

	/**
	 * Getter for property col.
	 * 
	 * @return Value of property col.
	 */
	public int getCol() {
		return col;
	}

	/**
	 * Setter for property col.
	 * 
	 * @param col
	 *            New value of property col.
	 */
	public void setCol(int col) {
		this.col = col;
	}
	


	public String [] getQustionIdStr(){
		if(this.questionList.size()>0){
			String [] questionIds = new String[this.questionList.size()];
			int i = 0;
			for (Iterator iter = this.questionList.iterator(); iter.hasNext();) {
				Question q = (Question) iter.next();
				questionIds[i++] = String.valueOf(q.getId());
			}
			return questionIds;
		}
		else{
			return new String[0];
		}
	}

	public Integer [] getQustionRows(){
		if(this.questionList.size()>0){
			Integer [] questionRows = new Integer[this.questionList.size()];
			int i = 0;
			for (Iterator iter = this.questionList.iterator(); iter.hasNext();) {
				Question q = (Question) iter.next();
				questionRows[i++] = q.getQuestionOrder();
			}
			return questionRows;
		}
		else{
			return new Integer [0];
		}
	}
	public Integer [] getQustionCols(){
		if(this.questionList.size()>0){
			Integer [] questionCols = new Integer[this.questionList.size()];
			int i = 0;
			for (Iterator iter = this.questionList.iterator(); iter.hasNext();) {
				Question q = (Question) iter.next();
				// ensure the default column number has the meaningful default of 1, if unspecified
				if (q.getQuestionOrderCol() >0){
					questionCols[i++] = q.getQuestionOrderCol();
				}
				else{
					questionCols[i++] = 1;
				}
			}
			return questionCols;
		}
		else{
			return new Integer [0];
		}
	}
	
	public int compareTo(Section o) {
	     return(this.getOrderValue() - o.getOrderValue());
	}

}