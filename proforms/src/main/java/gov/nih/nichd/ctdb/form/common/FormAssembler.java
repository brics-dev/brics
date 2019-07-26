package gov.nih.nichd.ctdb.form.common;

import gov.nih.nichd.ctdb.common.AssemblerException;
import gov.nih.nichd.ctdb.common.CtdbAssembler;
import gov.nih.nichd.ctdb.common.CtdbConstants;
import gov.nih.nichd.ctdb.common.CtdbDomainObject;
import gov.nih.nichd.ctdb.common.CtdbException;
import gov.nih.nichd.ctdb.common.CtdbForm;
import gov.nih.nichd.ctdb.common.CtdbLookup;
import gov.nih.nichd.ctdb.common.StrutsConstants;
import gov.nih.nichd.ctdb.form.domain.Form;
import gov.nih.nichd.ctdb.form.domain.Section;
import gov.nih.nichd.ctdb.form.form.FormForm;
import gov.nih.nichd.ctdb.question.domain.Answer;
import gov.nih.nichd.ctdb.question.domain.Question;
import gov.nih.nichd.ctdb.question.domain.QuestionType;
import gov.nih.nichd.ctdb.question.manager.QuestionManager;
import gov.nih.nichd.ctdb.response.domain.DataEntryWorkflowType;

import java.sql.SQLException;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Utilities for use with the protocol form portion of the NICHD CTDB such as
 * conversion between form and domain objects
 * 
 * @author Booz Allen Hamilton
 * @version 1.0
 */

public class FormAssembler extends CtdbAssembler {

	public static String sectionDomainToSectionJson(JSONObject sectionObjJSON,
			Section section) throws AssemblerException, JSONException {

		String id = String.valueOf(section.getId());
		String name = section.getName();
		String description = section.getDescription();
		String row = String.valueOf(section.getRow());
		String col = String.valueOf(section.getCol());

		boolean isCollapsable = section.isCollapsable();
		boolean isRepeatable = section.isRepeatable();
		int initRepeatedSecs = section.getInitRepeatedSections();
		int maxRepeatedSecs = section.getMaxRepeatedSections();
		int repeatedSectionParent = section.getRepeatedSectionParent();
		String repeatedSectionParentString = String.valueOf(repeatedSectionParent);
		String repeatableGroupName = section.getRepeatableGroupName();
		boolean isGridtype = section.isGridtype();
		
		int tableGroupId = section.getTableGroupId();
		int tableHeaderType = section.getTableHeaderType();

		if (description == null) {
			description = "";
		}

		sectionObjJSON.put("id", id);
		sectionObjJSON.put("name", name);
		sectionObjJSON.put("description", description);
		sectionObjJSON.put("row", row);
		sectionObjJSON.put("col", col);
		sectionObjJSON.put("isCollapsable", isCollapsable);
		sectionObjJSON.put("isRepeatable", isRepeatable);
		sectionObjJSON.put("initRepeatedSecs", initRepeatedSecs);
		sectionObjJSON.put("maxRepeatedSecs", maxRepeatedSecs);
		sectionObjJSON.put("repeatedSectionParent", repeatedSectionParentString);
		sectionObjJSON.put("repeatableGroupName", repeatableGroupName);
		sectionObjJSON.put("gridtype", isGridtype);
		sectionObjJSON.put("tableGroupId", tableGroupId);
		sectionObjJSON.put("tableHeaderType", tableHeaderType);

		sectionObjJSON.put("existingSection", true);

		return id;

	}

	public static void questionDomainToQuestionJson(JSONObject questionObjJSON, Question question, String sectionId) 
			throws JSONException, CtdbException, SQLException {
		QuestionManager qm = new QuestionManager();

		String questionId = String.valueOf(question.getId());
		questionObjJSON.put("questionId", questionId);

		questionObjJSON.put("sectionId", sectionId);

		questionObjJSON.put("newQuestionDivId", sectionId + "_" + questionId);

		String questionVersionLetter = question.getVersion().getToString();
		questionObjJSON.put("questionVersionLetter", questionVersionLetter);

		String questionVersionNumber = String.valueOf(question.getVersion()
				.getVersionNumber());
		questionObjJSON.put("questionVersionNumber", questionVersionNumber);

		String questionName = question.getName();
		questionObjJSON.put("questionName", questionName);

		String questionText = question.getText();
		questionObjJSON.put("questionText", questionText);
		
		questionObjJSON.put("htmlText", question.getHtmltext());

		String descriptionUp = question.getDescriptionUp() == null ? ""
				: question.getDescriptionUp();
		questionObjJSON.put("descriptionUp", descriptionUp);

		String descriptionDown = question.getDescriptionDown() == null ? ""
				: question.getDescriptionDown();
		questionObjJSON.put("descriptionDown", descriptionDown);

		String questionType = question.getType().getDispValue();
		questionObjJSON.put("questionType", questionType);

		questionObjJSON.put("existingQuestion", true);

		questionObjJSON.put("forcedNewVersion", false);

		questionObjJSON.put("includeOther", question.isIncludeOtherOption());
		
		//String options = "";
		List optionsList = question.getAnswers();
		JSONArray questionOptionsObjectArray = new JSONArray();
		if (optionsList != null && optionsList.size() > 0) {
			Iterator iter = optionsList.iterator();
			while (iter.hasNext()) {
				Answer ans = (Answer) (iter.next());
				String opt = ans.getDisplay();
				if(question.isIncludeOtherOption() && opt.equals(CtdbConstants.OTHER_OPTION_DISPLAY)) {
					continue;
				}
				
				String score = "";
				if (ans.getScore() != Integer.MIN_VALUE) {
					score = String.valueOf(ans.getScore()); 
				}
				String submittedValue = ans.getSubmittedValue();
				if (submittedValue == null || submittedValue == "null") {
					submittedValue = "";
				}
				
				JSONObject questionOption = new JSONObject();
				questionOption.put("option", opt);
				questionOption.put("score", score);
				questionOption.put("submittedValue", submittedValue);
				
				questionOptionsObjectArray.put(questionOption);
				
				//options += opt + " | " + score + " | " + submittedValue
						//+ StrutsConstants.alienSymbol; // modify by Ching Heng
				// options = options + opt + StrutsConstants.alienSymbol;
			}
		}

		questionObjJSON.put("questionOptionsObjectArray", questionOptionsObjectArray);

		questionObjJSON.put("graphicNames",
				qm.getQuestionImages(question.getId()));
		
		questionObjJSON.put("graphicNamesOrig",
				qm.getQuestionImages(question.getId()));

		String imageOption = "";
		if (question.getType() == QuestionType.IMAGE_MAP) {
			List imageMapOptionList = qm.getImageMapOptions(question.getId(),
					question.getVersion().getVersionNumber());
			Set<String> distinctOptions = new HashSet<String>();
			
			if (imageMapOptionList != null && imageMapOptionList.size() > 0) {
				Iterator iter = imageMapOptionList.iterator();
				while (iter.hasNext()) {
					String op = (String) (iter.next());
					if (!distinctOptions.contains(op)) {
						imageOption += op + StrutsConstants.alienSymbol;
						distinctOptions.add(op);
					} 
				}
			}
		}
		
		if (!imageOption.equals("")) {
			imageOption = imageOption.substring(0,
					imageOption.lastIndexOf(StrutsConstants.alienSymbol));
		}
		questionObjJSON.put("imageOption", imageOption);

		String imageFileName = qm.getImageMapFileNameByquestionId(
				question.getId(), question.getVersion().getVersionNumber());
		if (imageFileName == null) {
			imageFileName = "";
		}
		questionObjJSON.put("imageFileName", imageFileName);

		String visualScaleText = "";
		visualScaleText = qm.getVisualScaleInfo(question.getId(), question
				.getVersion().getVersionNumber());
		if (visualScaleText == null) {
			visualScaleText = "";
		}
		questionObjJSON.put("visualScaleInfo", visualScaleText);

		// added by Ching Heng
		String defaultValue = question.getDefaultValue();
		if (defaultValue == null) {
			defaultValue = "";
		}
		questionObjJSON.put("defaultValue", defaultValue);

		String unansweredValue = question.getUnansweredValue();
		if (unansweredValue == null) {
			unansweredValue = "";
		}
		questionObjJSON.put("unansweredValue", unansweredValue);

		questionObjJSON.put("associatedGroupIds",
				qm.getAssociatedGroupIds(question.getId()));

		JSONObject attributeObjJson = new JSONObject();
		FormQuestionAttributesAssembler.domainToJson(question,
				attributeObjJson, Integer.parseInt(sectionId));
		questionObjJSON.put("attributeObject", attributeObjJson);

	}

	/**
	 * Transforms a Form Domain Object to a formForm object
	 * 
	 * @param domain
	 *            The Form object to transform to the FormForm object
	 * @throws AssemblerException
	 *             Thrown if any error occurs while transforming the Form domain
	 *             object to the FormForm object
	 */
	public static void domainToForm(CtdbDomainObject domain, FormForm formForm) throws AssemblerException {

		try {
			Form form = (Form) domain;
			formForm.setName(form.getName());
			formForm.setDescription(form.getDescription());
			formForm.setDataEntryFlag(form.getSingleDoubleKeyFlag());
			formForm.setDataEntryFlagNo(form.getSingleDoubleKeyFlag());
			formForm.setAccessFlag(form.getAccessFlag());
			formForm.setStatus(Integer.toString(form.getStatus().getId()));

			if (form.getFormHtmlAttributes().getFormBorder()) {
				formForm.setFormborder("yes");
			} else {
				formForm.setFormborder("no");
			}

			if (form.getFormHtmlAttributes().getSectionBorder()) {
				formForm.setSectionborder("yes");
			} else {
				formForm.setSectionborder("no");
			}

			formForm.setFormfont(form.getFormHtmlAttributes().getFormFont());
			formForm.setFormcolor(form.getFormHtmlAttributes().getFormColor());
			formForm.setSectionfont(form.getFormHtmlAttributes().getSectionFont());
			formForm.setSectioncolor(form.getFormHtmlAttributes().getSectionColor());
			formForm.setFontSize(form.getFormHtmlAttributes().getFormFontSize());
			formForm.setCellpadding(form.getFormHtmlAttributes().getCellpadding());
			formForm.setFormHeader(notNull(form.getFormHeader()));
			formForm.setFormFooter(notNull(form.getFormFooter()));
			formForm.setDataEntryWorkflowType(form.getDataEntryWorkflow().getValue());
			formForm.setAttachFiles(form.isAttachFiles());
			formForm.setDataSpring(form.isDataSpring());
			formForm.setTabdisplay(form.isTabDisplay());
			formForm.setFormtypeid(form.getFormType());
			formForm.setDataStructureName(form.getDataStructureName());
			formForm.setCopyRight(form.isCopyRight());
			formForm.setAllowMultipleCollectionInstances(form.isAllowMultipleCollectionInstances());

		}
		catch (Exception e) {
			throw new AssemblerException("Unable to assemble protocol form object: " + e.getMessage(), e);
		}
	}

	/**
	 * Transforms a FormForm object to a Form Domain Object
	 * 
	 * @param inform
	 *            The FormForm object to transform to the Form Domain Object
	 * @return The Form Domain Object
	 * @throws AssemblerException
	 *             Thrown if any error occurs while transforming the FormForm
	 *             object to the Form Domain Object
	 */
	public static CtdbDomainObject formToDomain(CtdbForm inform)
			throws AssemblerException {

		try {
			FormForm formForm = (FormForm) inform;
			Form form = new Form();

			form.setName(formForm.getName().trim());
			form.setDescription(formForm.getDescription().trim());
			form.setSingleDoubleKeyFlag(formForm.getDataEntryFlag());
			form.setAccessFlag(formForm.getAccessFlag());
			form.setDataStructureName(formForm.getDataStructureName());

			CtdbLookup status = new CtdbLookup();
			if(formForm.getStatus() != null) {
				status.setId(Integer.parseInt(formForm.getStatus()));
			}else {
				status.setId(Integer.parseInt(formForm.getStatusHidden()));
			}
			form.setStatus(status);

			FormHtmlAttributes attributes = new FormHtmlAttributes();
			// System.out.println(formForm.getFormborder());
			// System.out.println(formForm.getSectionborder());
			if (formForm.getFormborder().equals("yes")) {
				attributes.setFormBorder(true);
			} else {
				attributes.setFormBorder(false);
			}
			if (formForm.getSectionborder().equals("yes")) {
				attributes.setSectionBorder(true);
			} else {
				// attributes.setFormBorder(false);
				attributes.setSectionBorder(false); // fixed by Ching Heng
			}
			attributes.setFormColor(formForm.getFormcolor());
			attributes.setFormFont(formForm.getFormfont());
			attributes.setSectionColor(formForm.getSectioncolor());
			attributes.setSectionFont(formForm.getSectionfont());
			attributes.setFormFontSize(formForm.getFontSize());
			attributes.setCellpadding(formForm.getCellpadding());
			form.setFormHtmlAttributes(attributes);

			form.setFormHeader(formForm.getFormHeader());
			form.setFormFooter(formForm.getFormFooter());

			// form.setFormGroups( formForm.getFormGroups());
			form.setDataEntryWorkflow(DataEntryWorkflowType.getByValue(formForm
					.getDataEntryWorkflowType()));
			form.setAttachFiles(formForm.isAttachFiles());
			form.setDataSpring(formForm.isDataSpring());
			form.setTabDisplay(formForm.isTabdisplay());
			form.setFormType(formForm.getFormtypeid());

			// add for copyright
			form.setCopyRight(formForm.isCopyRight());
			form.setAllowMultipleCollectionInstances(formForm.isAllowMultipleCollectionInstances());
			return form;
		} catch (Exception e) {
			e.printStackTrace();
			throw new AssemblerException(
					"Exception assembling form domain object: "
							+ e.getMessage(), e);
		}
	}

	private static String notNull(String s) {
		if (s == null) {
			return "";
		} else {
			return s;
		}

	}

}
