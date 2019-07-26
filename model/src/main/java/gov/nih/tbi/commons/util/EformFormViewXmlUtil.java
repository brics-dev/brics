package gov.nih.tbi.commons.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;

import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;







import gov.nih.tbi.ModelConstants;
import gov.nih.tbi.commons.model.AnswerType;
import gov.nih.tbi.commons.model.QuestionType;
import gov.nih.tbi.commons.model.exceptions.TransformationException;
import gov.nih.tbi.dictionary.model.hibernate.eform.Eform;
import gov.nih.tbi.dictionary.model.hibernate.eform.Question;
import gov.nih.tbi.dictionary.model.hibernate.eform.QuestionAnswerOption;
import gov.nih.tbi.dictionary.model.hibernate.eform.QuestionAttribute;
import gov.nih.tbi.dictionary.model.hibernate.eform.QuestionDocument;
import gov.nih.tbi.dictionary.model.hibernate.eform.Section;
import gov.nih.tbi.dictionary.model.hibernate.eform.SectionQuestion;
import gov.nih.tbi.dictionary.model.hibernate.eform.SkipRuleQuestion;
import gov.nih.tbi.dictionary.model.hibernate.eform.SkipRuleQuestionPk;
import gov.nih.tbi.dictionary.model.hibernate.eform.VisualScale;

public class EformFormViewXmlUtil {
	
	private static Logger logger = Logger.getLogger(EformFormViewXmlUtil.class);

	private Eform eform;

	public EformFormViewXmlUtil(Eform eform) {
		setEform(eform);
	}

	/**
	 * This method allows the transformation of a Form into an XML Document. If no implementation is available at this
	 * time, an UnsupportedOperationException will be thrown.
	 * 
	 * @return XML Document
	 * @exception TransformationException Is thrown if there is an error during the XML transformation
	 * @exception UnsupportedOperationException Is thrown if this method is currently unsupported and not implemented.
	 */
	public Document convertEformToFormViewXML() throws TransformationException {
		try {
			Document document = newDocument();
			Element root = initXML(document, "form");

			Element nameNode = document.createElement("name");
			nameNode.appendChild(document.createTextNode(eform.getTitle()));
			root.appendChild(nameNode);

			Element formIdNode = document.createElement("formId");
			formIdNode.appendChild(document.createTextNode(String.valueOf(eform.getId())));
			root.appendChild(formIdNode);

			/*
			 * root.setAttribute("displayQids",
			 * --------------------------------------------------------------------------
			 * -------------------------------------------------- (Boolean.toString(this.displayQids)).toLowerCase());
			 */

			Element descriptionNode = document.createElement("description");
			if (eform.getDescription() != null && !eform.getDescription().equals("")) {
				descriptionNode.appendChild(document.createTextNode(eform.getDescription()));
			}

			root.appendChild(descriptionNode);

			Document statusDom = statusToXML("status");
			root.appendChild(document.importNode(statusDom.getDocumentElement(), true));

			Document formHtmlAttDom = questionAttributesToXML();
			root.appendChild(document.importNode(formHtmlAttDom.getDocumentElement(), true));


			createTOC(document, root);
			Element headerNode = document.createElement("formHeader");
			if (eform.getHeader() != null && !eform.getHeader().equals("")) {
				headerNode.appendChild(
						document.createTextNode(eform.getHeader()));
			}
			root.appendChild(headerNode);

			Element footerNode = document.createElement("formFooter");
			if (eform.getFooter() != null && !eform.getFooter().equals("")) {
				footerNode.appendChild(document.createTextNode(eform.getFooter()));
			}

			root.appendChild(footerNode);

			int rowNum = 1;
			int repeatSecCount = 1;
			SortedSet<Section> orderedSections = new TreeSet<Section>();
			orderedSections.addAll(eform.getSectionList());
			
			
			
			///////////////////////nish
			int fRow = 1;
			int fCol = 1;
			ArrayList<ArrayList<Section>> sectionsInForm = new ArrayList<ArrayList<Section>>();
			ArrayList<Section> sectionsInRow = null;
			for (Section sec : orderedSections) {
				int formRow = sec.getFormRow();
				int formCol = sec.getFormCol();
				if(formRow != fRow) {
					sectionsInForm.add(sectionsInRow);
				}
				if(formCol == 1) {
					sectionsInRow = new ArrayList<Section>();
					sectionsInRow.add(sec);
				}else {
					sectionsInRow.add(sec);
				}
				fRow = formRow;
				fCol = formCol;	
			}
			sectionsInForm.add(sectionsInRow);
			
			
			
			
			for ( List<Section> sectionList : sectionsInForm ) {
				Element aRow = document.createElement("row");
				aRow.setAttribute("rowNum", Integer.toString(rowNum));
				aRow.setAttribute("tabLabel", "Empty");
				
				int i = 0;
				for ( Section sec : sectionList ) {
					Element formCell = document.createElement("formcell");
					
						// added by Ching Heng make row width shows correctly
						int colunmLength = sectionList.size();
						
						for ( Section s : sectionList ) {
							if ( s == null ) {
								colunmLength--;
							}
						}
						
						formCell.setAttribute("theStyle","width:" + Integer.toString(100 / colunmLength)+ "%;");
						formCell.setAttribute("theRowSpan", "1");
					
					
					if ( sec != null ) {
						if( sec.getIsRepeatable() && sec.getRepeatedSectionParent() == null ) {
							repeatSecCount=1;
						}
						else if ( sec.getIsRepeatable() && sec.getRepeatedSectionParent() != null ) {
							repeatSecCount++;
						}
						else{
							repeatSecCount=1;
						}
						
						
						if (aRow.getAttribute("tabLabel").equals("Empty")) {
							aRow.setAttribute("tabLabel", sec.getName());
						}
						
						Document sectionDom = sectionToXML(sec, repeatSecCount, rowNum);
						sectionDom.getDocumentElement().appendChild(sectionDom.importNode(formHtmlAttDom.getDocumentElement(), true));
						formCell.appendChild(document.importNode(sectionDom.getDocumentElement(), true));
						formCell.setAttribute("isRepeatable", Boolean.toString(sec.getIsRepeatable()));
						
						int minValue = sec.getInitialRepeatedSections();
						formCell.setAttribute("buttonCount", String.valueOf(rowNum));
						formCell.setAttribute("minimumValue", String.valueOf(sec.getInitialRepeatedSections()));
						formCell.setAttribute("maximumValue", String.valueOf(sec.getMaxRepeatedSections()));
						formCell.setAttribute("parentValue", String.valueOf(sec.getRepeatedSectionParent()));
						formCell.setAttribute("formRow", String.valueOf(sec.getFormRow()));
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
			/////////////////////////////////////////
			

			/*for (Section sec : orderedSections) {
				Element aRow = document.createElement("row");
				aRow.setAttribute("rowNum", Integer.toString(rowNum));
				aRow.setAttribute("tabLabel", "Empty");

				Element formCell = document.createElement("formcell");
				
				 * CellFormatting cf = cellFormatting.get(Integer.toString(rowNum) + "-" + Integer.toString(i + 1));
				 * 
				 * if (cf != null) { formCell.setAttribute("theStyle", cf.getHtmlStyleString());
				 * formCell.setAttribute("theRowSpan", cf.getHtmlRowSpan()); formCell.setAttribute("theColSpan",
				 * cf.getHtmlColSpan());
				 * 
				 * } else {
				 
				int colunmLength = eform.getSectionList().size();

				for (Section s : eform.getSectionList()) {
					if (s == null) {
						colunmLength--;
					}
				}

				formCell.setAttribute("theStyle", "width:" + Integer.toString(100 / colunmLength) + "%;");
				formCell.setAttribute("theRowSpan", "1");
				// }

				if (sec != null) {
					if( !sec.getIsRepeatable() && sec.getRepeatedSectionParent() == null && sec.getSectionQuestion().isEmpty()) {
						repeatSecCount++;
					}
					else if( sec.getIsRepeatable() && sec.getRepeatedSectionParent() == null ) {
						repeatSecCount=1;
					}
					else if ( sec.getIsRepeatable() && sec.getRepeatedSectionParent() != null ) {
						repeatSecCount++;
					}
					else{
						repeatSecCount=1;
					}

					if (aRow.getAttribute("tabLabel").equals("Empty")) {
						aRow.setAttribute("tabLabel", sec.getName());
					}

					Document sectionDom = sectionToXML(sec, repeatSecCount, rowNum);
					sectionDom.getDocumentElement().appendChild(sectionDom.importNode(formHtmlAttDom.getDocumentElement(), true));
					formCell.appendChild(document.importNode(sectionDom.getDocumentElement(), true));
					formCell.setAttribute("isRepeatable", Boolean.toString(sec.getIsRepeatable()));

					int minValue = sec.getInitialRepeatedSections();
					formCell.setAttribute("buttonCount", String.valueOf(rowNum));
					formCell.setAttribute("minimumValue", String.valueOf(sec.getInitialRepeatedSections()));
					formCell.setAttribute("maximumValue", String.valueOf(sec.getMaxRepeatedSections()));
					formCell.setAttribute("parentValue", String.valueOf(sec.getRepeatedSectionParent()));
					formCell.setAttribute("formRow", String.valueOf(sec.getFormRow()));
					formCell.setAttribute("secId", String.valueOf(sec.getId()));

					if (repeatSecCount <= minValue) {
						formCell.setAttribute("visible", "true");
					} else {
						formCell.setAttribute("visible", "false");
					}
				}

				aRow.appendChild(formCell);
				rowNum++;
				root.appendChild(aRow);
			}*/

			/*Element intervalsNode = document.createElement("intervals");

			for (Iterator<Interval> it = intervalList.iterator(); it.hasNext();) {
				Interval interval = it.next();
				Document intervalDom = interval.toXML();
				intervalsNode.appendChild(document.importNode(intervalDom.getDocumentElement(), true));
			}

			root.appendChild(intervalsNode);

			Element numQuestionsNode = document.createElement("numberofquestion");
			numQuestionsNode.appendChild(document.createTextNode(this.numQuestions + ""));
			root.appendChild(numQuestionsNode);

			Element orderValNode = document.createElement("ordervalue");
			orderValNode.appendChild(document.createTextNode(this.orderValue + ""));
			root.appendChild(orderValNode);

			Element protocolIdNode = document.createElement("protocolid");
			protocolIdNode.appendChild(document.createTextNode(this.protocol.getId() + ""));
			root.appendChild(protocolIdNode);*/

			// if status is active, cache xml.
			
			return document;
		} catch (Exception ex) {
			throw new TransformationException("Unable to transform eform object with id = " + eform.getId(), ex);
		}
	}

	public Document statusToXML(String lookupName) throws TransformationException {
		try {
			Document document = newDocument();
			Element root = initXML(document, lookupName);

			Element shortNameNode = document.createElement("shortname");
			shortNameNode.appendChild(document.createTextNode(eform.getStatus().getType()));
			root.appendChild(shortNameNode);

			/*
			 * if (this.longName != null) { Element longNameNode = document.createElement("longname");
			 * ------------------
			 * -------------------------------------------------------------------------------------------
			 * longNameNode.appendChild(document.createTextNode(this.longName)); root.appendChild(longNameNode); }
			 */

			return document;
		} catch (Exception ex) {
			throw new TransformationException("Unable to transform object " + lookupName + " with id = " + eform.getId());
		}
	}

	/**
	 * This method allows the transformation of a User into an XML Document. If no implementation is available at this
	 * time, an UnsupportedOperationException will be thrown.
	 * 
	 * @return XML Document
	 * @exception TransformationException is thrown if there is an error during the XML tranformation
	 * @exception UnsupportedOperationException is thrown if this method is currently unsupported and not implemented.
	 */
	private Document sectionToXML(Section section, int repeatSecCount, int rowNum) throws TransformationException {
		try {
			Document document = newDocument();
			Element root = initXML(document, "section");
			root.setAttribute("isCollapsable", Boolean.toString(section.getCollapsable()));
			root.setAttribute("isGridType", Boolean.toString(Boolean.FALSE)); /*section.isGridtype()*/
			if(section.getIsManuallyAdded() != null){
				root.setAttribute("isManuallyAdded", Boolean.toString(section.getIsManuallyAdded()));
			}
			
			root.setAttribute("sectionId", Long.toString(section.getId()));

			if(section.getIsResponseImage() != null){
				root.setAttribute("isResponseImage", Boolean.toString(section.getIsResponseImage()));
			} else {
				root.setAttribute("isResponseImage", "false");
			}

			/**
			 * if (this.getResponseImage() != null && this.getResponseImage().getFileName() != null) {
			 * root.setAttribute("responseImageUploaded", "true"); Element responseImage =
			 * document.createElement("responseImageFile");
			 * responseImage.appendChild(document.createTextNode(this.getResponseImage().getFileName())); // Document d
			 * = this.getResponseImage().toXML(); root.appendChild(responseImage);
			 * 
			 * } else {
			 **/
			root.setAttribute("responseImageUploaded", "false");
			Element responseImage = document.createElement("responseImageFile");
			responseImage.appendChild(document.createTextNode(""));
			root.appendChild(responseImage);
			// }

			Element nameNode = document.createElement("name");
			nameNode.appendChild(document.createTextNode(section.getName()));
			root.appendChild(nameNode);

			Element secidNode = document.createElement("sectionid");
			secidNode.appendChild(document.createTextNode(Long.toString(section.getId())));
			root.appendChild(secidNode);

			Element descNode = document.createElement("description");
			if (section.getDescription() != null && !section.getDescription().equals("")) {
				descNode.appendChild(document.createTextNode(section.getDescription()));
				root.setAttribute("description",section.getDescription());
			}
			root.appendChild(descNode);
			//root.setAttribute("description",section.getDescription());

			root.setAttribute("textDisplayed",Boolean.toString(true));

			Element textNode = document.createElement("instructionalText");
			textNode.appendChild(document.createTextNode(""));
			root.appendChild(textNode);
			root.setAttribute("textDisplayed", Boolean.toString(true));
			root.setAttribute("inTableOfContents", Boolean.toString(false));          // ---------------------------------------------------
			// this is always set to false

			// determine the section table class name based on tableGroupId and tableHeaderType
			// root.setAttribute("tableGroupId", Integer.toString(this.tableGroupId));
			// root.setAttribute("tableHeaderType", Integer.toString(this.tableHeaderType));
			root.setAttribute("tableHeaderClassName", "noHeader");					// ---------------------------------------------------
			// this is always set to noHeader

			Element altLabelNode = document.createElement("alternativeLabel");
			altLabelNode.appendChild(document.createTextNode(section.getLabel()));
			root.appendChild(altLabelNode);

			// Element questionsNode = document.createElement("questions");

			String maxVal = String.valueOf(section.getMaxRepeatedSections());
			Element maxValNode = document.createElement("maximumValue");
			maxValNode.appendChild(document.createTextNode(maxVal));
			root.appendChild(maxValNode);

			String minVal = String.valueOf(section.getInitialRepeatedSections());
			Element minValNode = document.createElement("minimumValue");
			minValNode.appendChild(document.createTextNode(minVal));
			root.appendChild(minValNode);

			String rowNumCnt = String.valueOf(rowNum);
			Element rowNumCntNode = document.createElement("buttonCount");
			rowNumCntNode.appendChild(document.createTextNode(rowNumCnt));
			root.appendChild(rowNumCntNode);

			String isRepeatable = Boolean.toString((section.getIsRepeatable()));
			Element isRepeatableNode = document.createElement("isSecRepeatable");
			isRepeatableNode.appendChild(document.createTextNode(isRepeatable));
			root.appendChild(isRepeatableNode);

			String repeatSecCountStr = String.valueOf(repeatSecCount);
			Element repeatSecCountNode = document.createElement("repeatSecCount");
			repeatSecCountNode.appendChild(document.createTextNode(repeatSecCountStr));
			root.appendChild(repeatSecCountNode);

			Element parentValueNode = document.createElement("parentValue");

			if (section.getRepeatedSectionParent() != null && section.getRepeatedSectionParent() == -1) {
				parentValueNode.appendChild(document.createTextNode(String.valueOf(section.getId())));
			} else {
				parentValueNode.appendChild(document.createTextNode(String.valueOf(section.getRepeatedSectionParent())));
			}
			root.appendChild(parentValueNode);

			Element orderValueNode = document.createElement("formRow");
			orderValueNode.appendChild(document.createTextNode(String.valueOf(section.getFormRow())));
			root.appendChild(orderValueNode);

			String isCurrentLast = "false";
			if (repeatSecCount == section.getInitialRepeatedSections()) {
				isCurrentLast = "true";
			}
			Element isCurrentLastNode = document.createElement("isCurrentLast");
			isCurrentLastNode.appendChild(document.createTextNode(isCurrentLast));
			root.appendChild(isCurrentLastNode);

			List<SectionQuestion> sectionQuestionListToSort = new ArrayList<SectionQuestion>();
			sectionQuestionListToSort.addAll(section.getSectionQuestion());
			Collections.sort(sectionQuestionListToSort);

			/* Find maximum no of column for each row in the grid
			int maxNoofColumnForEachRowInSection = 0;
			if (section.getSectionQuestion().size() > maxNoofColumnForEachRowInSection) {
				maxNoofColumnForEachRowInSection = section.getSectionQuestion().size();
			}*/

			root.setAttribute("maxNoofColumnForEachRowInSection", String.valueOf(ModelConstants.MAX_COL_Q_IN_SECTION));
			
			//SortedSet<SectionQuestion> orderedSectionQuestions = new TreeSet<SectionQuestion>();
			//orderedSectionQuestions.addAll(section.getSectionQuestion()); 
			
			
			Map<Integer,ArrayList<SectionQuestion>> map = new TreeMap<Integer,ArrayList<SectionQuestion>>();
			
			List<SectionQuestion> questionList = new ArrayList<SectionQuestion>();
			//List<SectionQuestion> questionList = (List<SectionQuestion>) section.getSectionQuestion();
			
			questionList.addAll(section.getSectionQuestion());
			
			class QuestionDisplayOrderComparator implements Comparator<SectionQuestion> {
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
				public int compare(SectionQuestion sqOne, SectionQuestion sqTwo) {
					if (sqOne.getSection().getId() != sqTwo.getSection().getId()) {
						// can't compare these two really, so give a zero
						return 0;
					}
					
					// rows different?
					if (sqOne.getQuestionOrder() > sqTwo.getQuestionOrder()) {
						return 1;
					}
					else if (sqOne.getQuestionOrder() < sqTwo.getQuestionOrder()) {
						return -1;
					}
					
					// otherwise, same row...cols different?
					if (sqOne.getQuestionOrderColumn() > sqTwo.getQuestionOrderColumn()) {
						return 1;
					}
					else if (sqOne.getQuestionOrderColumn() < sqTwo.getQuestionOrderColumn()) {
						return -1;
					}
					
					// fall back just in case
					return 0;
				}
				
			};
			
			
			
			
			
			
			Collections.sort(questionList, new QuestionDisplayOrderComparator());
			
			
			
			
			
			
			
			
			
			
			
			
			
			
			
			
			
			
			
			
			
			
			
			
			
			for (Iterator it = questionList.iterator(); it.hasNext();) {
				
				SectionQuestion sq = (SectionQuestion) it.next();
				Integer questionRowOrder = sq.getQuestionOrder();
				
				if(map.containsKey(questionRowOrder)){
					ArrayList<SectionQuestion> qList = map.get(questionRowOrder);
					qList.add(sq);
				}else{
					ArrayList<SectionQuestion> qList = new ArrayList<SectionQuestion>();
					qList.add(sq);
					map.put(questionRowOrder, qList);
					
				}
			}
			
			
			for (Integer key : map.keySet()) {
				Element sectionRowsNode = document.createElement("sectionRows");
				Element questionsNode = document.createElement("questions");
				ArrayList<SectionQuestion> qList = map.get(key);
				int qListSize = qList.size();


				// calculate colSpan for <td>
				int floorColSpanTD = (int) java.lang.Math.floor(ModelConstants.MAX_COL_Q_IN_SECTION / qListSize);
				int widthTD = 100 / qListSize;
				
				for (SectionQuestion sq:qList) {
					 int questionRowOrder = sq.getQuestionOrder();
					    String bgColor = "#FFFFFF";
					    if (questionRowOrder % 2 == 0) {
					        bgColor = "#E2E4FF";
					    } 
		                                
						Document questionDom =  questionToXML(sq, sq.getQuestion().getId(), bgColor, floorColSpanTD, widthTD);
						questionsNode.appendChild(document.importNode(questionDom.getDocumentElement(), true));
				}
				sectionRowsNode.appendChild(questionsNode);
				root.appendChild(sectionRowsNode);

			}
			
			
			
			
			
			
			
			
			
			
			
			
			
			
			
			// For each row in hash map iterate and create new xml document structure to address grid display
			/*for (SectionQuestion sq : orderedSectionQuestions) {
				Element sectionRowsNode = document.createElement("sectionRows");
				Element questionsNode = document.createElement("questions");
				int qListSize = section.getSectionQuestion().size();

				// calculate colSpan for <td>
				int floorColSpanTD = (int) java.lang.Math.floor(ModelConstants.MAX_COL_Q_IN_SECTION / qListSize);
				int widthTD = 100 / qListSize;

				int questionRowOrder = sq.getQuestionOrder();
				String bgColor = "#FFFFFF";
				if (questionRowOrder % 2 == 0) {
					bgColor = "#E2E4FF";
				}

				Document questionDom = questionToXML(sq, sq.getQuestion().getId(), bgColor, floorColSpanTD, widthTD);
				questionsNode.appendChild(document.importNode(questionDom.getDocumentElement(), true));
				sectionRowsNode.appendChild(questionsNode);
				root.appendChild(sectionRowsNode);
			}*/

			return document;

		} catch (Exception ex) {
			ex.printStackTrace();
			throw new UnsupportedOperationException("Unable to transform object " + this.getClass().getName()
					+ " with id = " + section.getId());
		}
	}

	private void createTOC(Document document, Element root) {
		boolean createTOC = false;
		Element TOC = document.createElement("TOC");
		TOC.setAttribute("display", "false");

		for (Section s : eform.getSectionList()) {
			if (s == null) {
				continue;
			}

//			 if (s.getIntoBoolean() != null && s.getIntoBoolean()) { 
				 Element toAdd = document.createElement("TOCListing"); 
				 toAdd.setAttribute("sectionid", Long.toString(s.getId())); 
				 String label = "";
				 
				 if (s.getAltLabel() != null) { 
					 label = s.getAltLabel(); 
				 } else { 
					 label = s.getName(); 
				}
				 toAdd.setAttribute("displayvalue", label); TOC.appendChild(toAdd); 
				 createTOC = true; 
//			}
			 
		}

		if (createTOC) {
			root.appendChild(TOC);
		}
	}

	private Document questionToXML(SectionQuestion sectionQuestion, Long sectionId, String bgColor, int floorColSpanTD, int widthTD)
			throws TransformationException {
		Document document = newDocument();
		Element root = initXML(document, "question");

		Question question = sectionQuestion.getQuestion();
		QuestionAttribute questionAttribute = question.getQuestionAttribute();
		
		root.setAttribute("questionId", Long.toString(question.getId()));

		if ((questionAttribute != null) && questionAttribute.getCalculatedFlag()) {
			root.setAttribute("type", "Calculated");
			root.setAttribute("calculation", sectionQuestion.getCalculation());
			root.setAttribute("decimalPrecision", String.valueOf(questionAttribute.getDecimalPrecision()));
			root.setAttribute("answertype", questionAttribute.getAnswerType().getName());
		} else {
			if (questionAttribute.getAnswerType().equals(AnswerType.DATE)) {
				root.setAttribute("answertype", "date");
			} else if (questionAttribute.getAnswerType().equals(AnswerType.DATETIME)) {
				root.setAttribute("answertype", "datetime");
			}

			root.setAttribute("type", question.getType().getName());
		}

		root.setAttribute("displayText", Boolean.toString(questionAttribute.getShowText()));

		Element nameNode = document.createElement("name");
		nameNode.appendChild(document.createTextNode(question.getName()));
		root.appendChild(nameNode);

		// bg-color
		Element bgColorNode = document.createElement("bgColor");
		bgColorNode.appendChild(document.createTextNode(bgColor));
		root.appendChild(bgColorNode);
		// colSpan for question td
		root.setAttribute("floorColSpanTD", String.valueOf(floorColSpanTD));
		// width of question td
		root.setAttribute("widthTD", String.valueOf(widthTD));

		// includeOther
		Element includeOtherNode = document.createElement("includeOther");

		if (question.getIncludeOther()) {
			includeOtherNode.appendChild(document.createTextNode("true"));
		} else {
			includeOtherNode.appendChild(document.createTextNode("false"));
		}

		root.appendChild(includeOtherNode);
		
		// displayPV
		Element displayPVNode = document.createElement("displayPV");

		if (question.getDisplayPV()) {
			displayPVNode.appendChild(document.createTextNode("true"));
		} else {
			displayPVNode.appendChild(document.createTextNode("false"));
		}

		root.appendChild(displayPVNode);

		// descriptionUp
		Element descriptionUpNode = document.createElement("descriptionUp");
		String descriptionUpString = question.getDescriptionUp() == null ? "" : question.getDescriptionUp();
		descriptionUpNode.appendChild(document.createTextNode(descriptionUpString));
		root.appendChild(descriptionUpNode);

		if (!descriptionUpString.isEmpty()) {
			root.setAttribute("upDescription", "true");
		} else {
			root.setAttribute("upDescription", "false");
		}

		// descriptionDown
		Element descriptionDownNode = document.createElement("descriptionDown");
		String descriptionDownString = question.getDescriptionDown() == null ? "" : question.getDescriptionDown();
		descriptionDownNode.appendChild(document.createTextNode(descriptionDownString));
		root.appendChild(descriptionDownNode);

		if (!descriptionDownString.isEmpty()) {
			root.setAttribute("downDescription", "true");
		} else {
			root.setAttribute("downDescription", "false");
		}

		// htmltext for textblock
		Element htmltextNode = document.createElement("htmltext");
		String htmltextString = questionAttribute.getXhtmlText() == null ? "" : questionAttribute.getXhtmlText();
		htmltextNode.appendChild(document.createTextNode(htmltextString));
		root.appendChild(htmltextNode);
		Element textNode = document.createElement("text");
		textNode.appendChild(document.createTextNode(question.getText()));

		root.appendChild(textNode);

		if (question.getDefaultValue() != null) {
			Element defaultValueNode = document.createElement("defaultValue");
			defaultValueNode.appendChild(document.createTextNode(question.getDefaultValue()));
			root.appendChild(defaultValueNode);
		} else {
			Element defaultValueNode = document.createElement("defaultValue");
			defaultValueNode.appendChild(document.createTextNode(""));
			root.appendChild(defaultValueNode);
		}

		if (question.getType() == QuestionType.FILE) {
			Element defaultValueNode = document.createElement("attachmentId");
			defaultValueNode.appendChild(document.createTextNode(Integer.toString(Integer.MIN_VALUE)));
			root.appendChild(defaultValueNode);
		} else {
			Element defaultValueNode = document.createElement("attachmentId");
			defaultValueNode.appendChild(document.createTextNode(""));
			root.appendChild(defaultValueNode);
		}

		Element questionSectionNode = document.createElement("questionSectionNode");
		questionSectionNode.appendChild(document.createTextNode(String.valueOf(sectionId)));
		root.appendChild(questionSectionNode);

		// For text box field in other option
		Element otherBoxNode = document.createElement("otherBox");
		otherBoxNode.appendChild(document.createTextNode(""));
		root.appendChild(otherBoxNode);

		// When it's only one option, don't show the blank option
		 Element blankOptionNode = document.createElement("blankOption");
		 
		 if (question.getQuestionAnswerOption().size() < 2) { 
			 blankOptionNode.appendChild(document.createTextNode("false")); 
		 } else {
			 blankOptionNode.appendChild(document.createTextNode("true")); 
		 }
		 
		 root.appendChild(blankOptionNode);
		 
		 Element answersNode = document.createElement("answers"); 
		 String scoreStr = "";
		 
		 SortedSet<QuestionAnswerOption> orderedSectionQuestions = new TreeSet<QuestionAnswerOption>();
		 orderedSectionQuestions.addAll(question.getQuestionAnswerOption()); 
		 
		 for (QuestionAnswerOption answer : orderedSectionQuestions){ 
			 answer.setIncludeOther(question.getIncludeOther()); 
			 answer.setDisplayPV(question.getDisplayPV());
			 scoreStr = scoreStr + answer.getDisplay() + "|" + answer.getScore() + "|"; 
			 Document answerDom = questionAnswerOptionToXML(answer);
			 answersNode.appendChild(document.importNode(answerDom.getDocumentElement(), true)); 
		 }
		 
		 root.appendChild(answersNode); 
		 Element scoreStrNode = document.createElement("scoreStr");
		 scoreStrNode.appendChild(document.createTextNode(scoreStr)); 
		 root.appendChild(scoreStrNode);
		  
		  if ((question.getQuestionDocument() != null) && !question.getQuestionDocument().isEmpty()){ 
			  Element imagesNode = document.createElement("images"); 
			  Element fileNameNode = null;
		  
			  for ( QuestionDocument imageFileName : question.getQuestionDocument() ){ 
				  fileNameNode = document.createElement("filename");
				  fileNameNode.appendChild(document.createTextNode(imageFileName.getQuestionDocumentPk().getFileName())); imagesNode.appendChild(fileNameNode); 
			  }
			  root.appendChild(imagesNode);
		  }
		  
		  
//		  VisualScale vs = question.getVisualScale();
//		  Element centerTextNode = document.createElement("vscaleCenterText");
//		  centerTextNode.appendChild(document.createTextNode((vs.getCenterText())));
//		  root.appendChild(centerTextNode);
//		  
//		  Element rightTextNode = document.createElement("vscaleRightText");
//		  rightTextNode.appendChild(document.createTextNode((vs.getRightText())));
//		  root.appendChild(rightTextNode);
//		  
//		  Element leftTextNode = document.createElement("vscaleLeftText");
//		  leftTextNode.appendChild(document.createTextNode((vs.getLeftText())));
//		  root.appendChild(leftTextNode);
		  
		  
		  root.setAttribute("leftText", question.getVisualScale().getLeftText());
	      root.setAttribute("rightText", question.getVisualScale().getRightText());
	      root.setAttribute("centerText", question.getVisualScale().getCenterText());
	      root.setAttribute("scaleMin", String.valueOf(question.getVisualScale().getStartRange()));
	      root.setAttribute("scaleMax", String.valueOf(question.getVisualScale().getEndRange()));
	      root.setAttribute("width", String.valueOf(question.getVisualScale().getWidthMM()));
	      root.setAttribute("showHandle", Boolean.toString(question.getVisualScale().getShowHandle()).toLowerCase());

	        
		 
		Document qAttrsDom = questionAttributesToXML(sectionQuestion);
		root.appendChild(document.importNode(qAttrsDom.getDocumentElement(), true));

		return document;
	}
	
	private Document questionAnswerOptionToXML(QuestionAnswerOption answer){

		Document document = newDocument();
		Element root = initXML(document, "answer");

		root.setAttribute("type", answer.getQuestionAnswerDataType().getAnswerDataType());

		if (answer.getDisplay() != null) {
			Element displayNode = document.createElement("display");
			if(answer.isDisplayPV()) {
				displayNode.appendChild(document.createTextNode(answer.getSubmittedValue()));
				displayNode.setAttribute("displayToolTip", answer.getDisplay());
			} else {
				displayNode.appendChild(document.createTextNode(answer.getDisplay()));
				displayNode.setAttribute("displayToolTip", answer.getSubmittedValue());
			}
			root.appendChild(displayNode);
		}

		Element includeOtherNode = document.createElement("includeOther");
		
		if (answer.isIncludeOther() && answer.getDisplay().equals(ModelConstants.OTHER_OPTION_DISPLAY)) {
			includeOtherNode.appendChild(document.createTextNode("true"));
		} else {
			includeOtherNode.appendChild(document.createTextNode("false"));
		}
		
		root.appendChild(includeOtherNode);

		if (answer.getCodeValue() != null) {
			Element codeValueNode = document.createElement("codeValue");
			codeValueNode.appendChild(document.createTextNode(answer.getCodeValue()));
			root.appendChild(codeValueNode);
		}
		
		if (answer.getScore() != Integer.MIN_VALUE) {
			Element scoreNode = document.createElement("score");
			scoreNode.appendChild(document.createTextNode(Double.toString(answer.getScore())));
			root.appendChild(scoreNode);
		}

		Element selectedNode = document.createElement("selected");
		selectedNode.appendChild(document.createTextNode(Boolean.toString(answer.isSelected())));
		root.appendChild(selectedNode);

		if (answer.getMinCharacters() != Integer.MIN_VALUE) {
			Element minNode = document.createElement("minCharacters");
			minNode.appendChild(document.createTextNode(Integer.toString(answer.getMinCharacters())));
			root.appendChild(minNode);
		}

		if (answer.getMaxCharacters() != Integer.MIN_VALUE) {
			Element maxNode = document.createElement("maxCharacters");
			maxNode.appendChild(document.createTextNode(Integer.toString(answer.getMaxCharacters())));
			root.appendChild(maxNode);
		}

		return document;
	
	}

	private Document questionAttributesToXML(SectionQuestion sectionQuestion) throws TransformationException {
		Question question = sectionQuestion.getQuestion();
		QuestionAttribute questionAttribute = question.getQuestionAttribute();
		
		Document document = newDocument();
		Element root = initXML(document, "formQuestionAttributes");

		Document htmlAttDom = htmlAttributesToXML(questionAttribute);
		root.appendChild(document.importNode(htmlAttDom.getDocumentElement(), true));

		root.setAttribute("required", (Boolean.toString(questionAttribute.getRequiredFlag())).toLowerCase());

		/*Element labelNode = document.createElement("label");
		labelNode.appendChild(document.createTextNode(this.label));
		root.appendChild(labelNode);*/

		Element answerTypeNode = document.createElement("answerType");
		answerTypeNode.appendChild(document.createTextNode(questionAttribute.getAnswerType().getName()));
		root.appendChild(answerTypeNode);

		Element minNode = document.createElement("minCharacters");
		minNode.appendChild(document.createTextNode(questionAttribute.getMinCharacters() + ""));
		root.appendChild(minNode);

		Element maxNode = document.createElement("maxCharacters");
		maxNode.appendChild(document.createTextNode(questionAttribute.getMaxCharacters() + ""));
		root.appendChild(maxNode);

		Element rangeOperatorNode = document.createElement("rangeOperator");
		rangeOperatorNode.appendChild(document.createTextNode(questionAttribute.getRangeOperator()));
		root.appendChild(rangeOperatorNode);

		Element rangeValue1Node = document.createElement("rangeValue1");
		rangeValue1Node.appendChild(document.createTextNode(questionAttribute.getRangeValue1()));
		root.appendChild(rangeValue1Node);

		Element rangeValue2Node = document.createElement("rangeValue2");
		rangeValue2Node.appendChild(document.createTextNode(questionAttribute.getRangeValue2()));
		root.appendChild(rangeValue2Node);

		Element horizontalDisplayNode = document.createElement("horizontalDisplay");
		horizontalDisplayNode.appendChild(document.createTextNode(Boolean.toString(questionAttribute.getHorizontalDisplay())));
		horizontalDisplayNode.setAttribute("horizDisplayBreak", Boolean.toString(questionAttribute.getHorizontalDisplayBreak()));
		root.appendChild(horizontalDisplayNode);

		Element textareaRows = document.createElement("textareaHeight");
		textareaRows.appendChild(document.createTextNode(Integer.toString(questionAttribute.getTextBoxHeight())));
		root.appendChild(textareaRows);

		Element textareaCols = document.createElement("textareaWidth");
		textareaCols.appendChild(document.createTextNode(Integer.toString(questionAttribute.getTextBoxWidth())));
		root.appendChild(textareaCols);

		// determine the question table class name based on tableGroupId and tableHeaderType
		// root.setAttribute("tableHeaderType", Integer.toString(this.tableHeaderType));
		root.setAttribute("tableHeaderClassName", this.getTableClassName(questionAttribute));
		root.setAttribute("showTextClassName", (questionAttribute.getShowText()) ? "showText" : "hideText");

		// Element textboxSize = document.createElement ("textboxSize");
		// textboxSize.appendChild(document.createTextNode(Integer.toString(this.textboxLength)));
		// root.appendChild(textboxSize);
		root.setAttribute("textboxSize", Integer.toString(questionAttribute.getTextBoxLength()));
		root.setAttribute("hasSkipRule", Boolean.toString(questionAttribute.getSkipRuleFlag()));

		if (questionAttribute.getSkipRuleFlag()) {
			root.setAttribute("skipOperator", questionAttribute.getSkipRuleOperatorType().getName());
			root.setAttribute("skipRule", questionAttribute.getSkipRuleType().getName());
			root.setAttribute("skipEquals", questionAttribute.getSkipRuleEquals());
		}

		if (sectionQuestion.getSkipRuleQuestion() != null && !sectionQuestion.getSkipRuleQuestion().isEmpty()) {
			Element questionToSkipNode = document.createElement("questionsToSkip");
			StringBuffer qIds = new StringBuffer(20);
			
			for (Iterator<SkipRuleQuestion> questionIterator = sectionQuestion.getSkipRuleQuestion().iterator(); questionIterator.hasNext();) {
				SkipRuleQuestion skipQuestion = (SkipRuleQuestion) questionIterator.next();
				// qIds.append("'Q_" + Integer.toString(skipQuestion.getId()) + "'");
				// qIds.append("'Q_" + Integer.toString(skipQuestion.getId()) + "'");
				SkipRuleQuestionPk skipQuestionPk = skipQuestion.getSkipRuleQuestionCompositePk();
				qIds.append("'S_" + Long.toString(skipQuestionPk.getSkipRuleSection().getId()) + "_Q_" 
				+ Long.toString(skipQuestionPk.getSkipRuleQuestion().getId()) + "'");
				if (questionIterator.hasNext()) {
					qIds.append(",");
				}
			}
			questionToSkipNode.appendChild(document.createTextNode(qIds.toString()));
			root.appendChild(questionToSkipNode);
		}
		return document;
	}
	
	private Document htmlAttributesToXML(QuestionAttribute questionAttribute) throws TransformationException {
        Document document = newDocument();
        Element root = initXML(document, "htmlAttributes");

        Element alignNode = document.createElement("align");
        alignNode.appendChild(document.createTextNode(questionAttribute.gethAlign()));
        root.appendChild(alignNode);

        Element vAlignNode = document.createElement("valign");
        vAlignNode.appendChild(document.createTextNode(questionAttribute.getvAlign()));
        root.appendChild(vAlignNode);

        Element colorNode = document.createElement("color");
        colorNode.appendChild(document.createTextNode(questionAttribute.getTextColor()));
        root.appendChild(colorNode);

        Element fontFaceNode = document.createElement("fontFace");
        fontFaceNode.appendChild(document.createTextNode(questionAttribute.getFontFace()));
        root.appendChild(fontFaceNode);

        if (questionAttribute.getFontSize() != null) {
            Element fontSizeNode = document.createElement("fontSize");
            if(questionAttribute.getFontSize().equals("0")){
            	fontSizeNode.appendChild(document.createTextNode("2"));
            }else{
            	fontSizeNode.appendChild(document.createTextNode(questionAttribute.getFontSize()));
            }
            root.appendChild(fontSizeNode);
        }

        Element indentNode = document.createElement("indent");
        indentNode.appendChild(document.createTextNode(Integer.toString(questionAttribute.getIndent())));
        root.appendChild(indentNode);

        /*Element borderNode = document.createElement("border");
        if (questionAttribute.getthis.border) {
            borderNode.appendChild(document.createTextNode("1"));
        } else {
            borderNode.appendChild(document.createTextNode("0"));
        }
        root.appendChild(borderNode);*/

        return document;
    }
	
	private String getTableClassName(QuestionAttribute questionAttribute) {
		String className = "noHeader";
		int tableHeaderType = questionAttribute.getTableHeaderType();
		if (tableHeaderType == 1) {
			className = "rowHeader";
		}
		else if (tableHeaderType == 2) {
			className = "columnHeader";
		}
		else if (tableHeaderType == 3) {
			className = "tableHeader";
		}
		return className;
	}

	private Document questionAttributesToXML() throws TransformationException {
		Document document = newDocument();
		Element root = initXML(document, "htmlAttributes");
		Element formBorderNode = document.createElement("formBorder");

		if (eform.getFormBorder()) {
			formBorderNode.appendChild(document.createTextNode("1"));
		} else {
			formBorderNode.appendChild(document.createTextNode("0"));
		}

		root.appendChild(formBorderNode);
		Element sectionBorderNode = document.createElement("sectionBorder");

		if (eform.getSectionBorder()) {
			sectionBorderNode.appendChild(document.createTextNode("1"));
		} else {
			sectionBorderNode.appendChild(document.createTextNode("0"));
		}

		root.appendChild(sectionBorderNode);

		Element formColorNode = document.createElement("formColor");
		formColorNode.appendChild(document.createTextNode(eform.getFormNameColor())); 
		root.appendChild(formColorNode);

		Element formFontNode = document.createElement("formFont");
		formFontNode.appendChild(document.createTextNode(eform.getFormNameFont()));
		root.appendChild(formFontNode);

		Element sectionColorNode = document.createElement("sectionColor");
		sectionColorNode.appendChild(document.createTextNode(eform.getSectionNameColor()));
		root.appendChild(sectionColorNode);

		Element sectionFontNode = document.createElement("sectionFont");
		sectionFontNode.appendChild(document.createTextNode(eform.getSectionNameFont()));
		root.appendChild(sectionFontNode);

		Element formFontSize = document.createElement("formFontSize");
		formFontSize.appendChild(document.createTextNode(Integer.toString(eform.getFontSize())));
		root.appendChild(formFontSize);

		Element cellpadding = document.createElement("cellpadding");
		cellpadding.appendChild(document.createTextNode(Integer.toString(eform.getCellPadding()))); // this was previously formFont //
		root.appendChild(cellpadding);

		return document;
	}

	/**
	 * Creates a new Document object
	 *
	 * @return A new Document object
	 * @throws TransformationException Thrown if any error occurs while creating the new document.
	 */
	private Document newDocument() throws TransformationException {
		try {
			return DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
		} catch (Exception e) {
			throw new TransformationException("Unable to create new document: " + e.getMessage(), e);
		}
	}

	/**
	 * Initializes the Document object for the eForm Object
	 *
	 * @param document Document object created in sub-class
	 * @param className Domain object class name that this Document will represent
	 * @return The root element of the Document object.
	 * @throws TransformationException Thrown if any errors occur while creating the root element of the Document object
	 */
	private Element initXML(Document document, String className) throws TransformationException {
		try {
			// create root element
			Element root = document.createElement(className);
			root.setAttribute("id", Long.toString(eform.getId()));
			document.appendChild(root);

			return root;
		} catch (Exception e) {
			throw new TransformationException("Unable to transform object " + className + " with id = " + eform.getId()
					+ ": " + e.getMessage(), e);
		}
	}

	private void setEform(Eform eform) {
		this.eform = eform;
	}
}
