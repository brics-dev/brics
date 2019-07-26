package gov.nih.nichd.ctdb.form.action;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.imageio.IIOException;
import javax.imageio.ImageIO;

import org.apache.commons.codec.binary.Base64;

import gov.nih.nichd.ctdb.common.CtdbConstants;
import gov.nih.nichd.ctdb.common.CtdbException;
import gov.nih.nichd.ctdb.common.ObjectNotFoundException;
import gov.nih.nichd.ctdb.form.domain.CalculatedFormQuestionAttributes;
import gov.nih.nichd.ctdb.form.domain.Form;
import gov.nih.nichd.ctdb.form.domain.FormInfoExportImport;
import gov.nih.nichd.ctdb.form.domain.FormLayout;
import gov.nih.nichd.ctdb.form.domain.FormQuestionAttributes;
import gov.nih.nichd.ctdb.form.domain.QuestionAttributesExportImport;
import gov.nih.nichd.ctdb.form.domain.QuestionSectionExportImport;
import gov.nih.nichd.ctdb.form.domain.Section;
import gov.nih.nichd.ctdb.form.domain.SectionExportImport;
import gov.nih.nichd.ctdb.form.manager.FormManager;
import gov.nih.nichd.ctdb.question.domain.Answer;
import gov.nih.nichd.ctdb.question.domain.ImageMapExportImport;
import gov.nih.nichd.ctdb.question.domain.ImageMapValuesExportImport;
import gov.nih.nichd.ctdb.question.domain.Question;
import gov.nih.nichd.ctdb.question.domain.QuestionExportImport;
import gov.nih.nichd.ctdb.question.domain.QuestionGraphic;
import gov.nih.nichd.ctdb.question.domain.QuestionOptionsExportImport;
import gov.nih.nichd.ctdb.question.domain.QuestionType;
import gov.nih.nichd.ctdb.question.domain.VisualScale;
import gov.nih.nichd.ctdb.question.domain.VisualScaleExportImport;
import gov.nih.nichd.ctdb.question.manager.QuestionManager;

public class FormExportHelper {
	
	/**
	 * Method to export form to XML
	 * @param formId
	 * @return FormInfoExportImport
	 * @throws ObjectNotFoundException
	 * @throws NumberFormatException
	 * @throws CtdbException
	 */
	public static FormInfoExportImport exportFormToXML(int formId) 
			throws ObjectNotFoundException, NumberFormatException, CtdbException {
		
		FormManager fm = new FormManager();
		Form f = fm.getFormAndSections(formId);

		FormInfoExportImport formInfo = new FormInfoExportImport();
		formInfo.setFromId(f.getId());
		formInfo.setName(f.getName());
		formInfo.setDescription(f.getDescription());
		formInfo.setLockFlag(f.getLockFlag());
		formInfo.setCopyRight(f.isCopyRight());
		formInfo.setAllowMultipleCollectionInstances(f.isAllowMultipleCollectionInstances());
		formInfo.setSingleDoubleKeyFlag(f.getSingleDoubleKeyFlag());
		formInfo.setDataStructureVersion(f.getDataStructureVersion());
		formInfo.setDataStructureName(f.getDataStructureName());
		formInfo.setFormGroupNames(f.getFormGroupNames());
		formInfo.setFormType(f.getFormType());
		formInfo.setFormTypeName(f.getFormTypeName());
		formInfo.setInCtss(f.isInCtss());
		formInfo.setTabDisplay(f.isTabDisplay());
		formInfo.setDataSpring(f.isDataSpring());
		formInfo.setAttachFiles(f.isAttachFiles());
		formInfo.setHasCalculationRule(f.isHasCalculationRule());
		formInfo.setHasImageMap(f.isHasImageMap());
		formInfo.setHasSkipRule(f.isHasSkipRule());
		formInfo.setOrderValue(f.getOrderValue());
		formInfo.setNumQuestions(f.getNumQuestions());
		formInfo.setFormVersion(f.getVersion().getVersionNumber());
		formInfo.setFormBorder(f.getFormHtmlAttributes().getFormBorder());
		formInfo.setSectionBorder(f.getFormHtmlAttributes().getSectionBorder());
		formInfo.setFormColor(f.getFormHtmlAttributes().getFormColor());
		formInfo.setSectionColor(f.getFormHtmlAttributes().getSectionColor());
		formInfo.setFormFont(f.getFormHtmlAttributes().getFormFont());
		formInfo.setSectionFont(f.getFormHtmlAttributes().getSectionFont());
		formInfo.setFormFontSize(f.getFormHtmlAttributes().getFormFontSize());
		formInfo.setCellpadding(f.getFormHtmlAttributes().getCellpadding());
		formInfo.setAccessFlag(f.getAccessFlag());
		formInfo.setFormHeader(f.getFormHeader());
		formInfo.setFormFooter(f.getFormFooter());
		formInfo.setFormDataEntryWorkFlowTypeIntVal(f.getDataEntryWorkflow().getValue());
		formInfo.setLegacy(false);
		
		//Get form Layout information for selected fromId and studyId
		ArrayList<FormLayout> formLayoutList = fm.getFormLayoutRowAndColumn(f.getId(), f.getProtocolId());
		formInfo.setFormLayoutInfo(formLayoutList);
		
		ArrayList<SectionExportImport> sectionExportList = new ArrayList<SectionExportImport>();
		
		// Get a row from list of rows
		for ( List<Section> row : f.getRowList() ) {
			for ( Section section : row ) {
				if ( section != null ) {
					int sectionId = section.getId();
					SectionExportImport sectionExport = new SectionExportImport();
					sectionExport.setSectionId(sectionId);
					sectionExport.setName(section.getName());
					sectionExport.setDescription(section.getDescription());
					sectionExport.setAltLabel(section.getAltLabel());
					sectionExport.setCol(section.getCol());
					sectionExport.setRow(section.getRow());
					sectionExport.setInstructionalText(section.getInstructionalText());
					sectionExport.setInitRepeatedSections(section.getInitRepeatedSections());
					sectionExport.setMaxRepeatedSections(section.getMaxRepeatedSections());
					sectionExport.setCollapsable(section.isCollapsable());
					sectionExport.setRepeatable(section.isRepeatable());
					sectionExport.setRepeatedSectionParent(section.getRepeatedSectionParent());
					sectionExport.setIntob(section.isIntob());
					sectionExport.setTextDisplayed(section.isTextDisplayed());
					sectionExport.setGridtype(section.isGridtype());
					sectionExport.setTableGroupId(section.getTableGroupId());
					sectionExport.setTableHeaderType(section.getTableHeaderType());

					//Marshall sectionQuestion information to XML
					List<Question> questionsList = fm.getSectionQuestions(sectionId);
					ArrayList<QuestionSectionExportImport> questionInSectionList = new ArrayList<QuestionSectionExportImport>();
					for (Question question : questionsList) {
						int questionId = question.getId();
						QuestionSectionExportImport qsei = new QuestionSectionExportImport();
						qsei.setQuestionId(questionId);
						qsei.setQuestionVersion(new Integer(1));
						qsei.setQuestionType(question.getType().getValue());
						qsei.setSectionId(sectionId);
						Question secQuestion = fm.getSectionQuestion(sectionId,questionId);
						qsei.setQuestionOrder(secQuestion.getQuestionOrder());
						qsei.setQuestionOrderCol(secQuestion.getQuestionOrderCol());
						qsei.setSuppressFlag(new Boolean(false));
						qsei.setQuestionAttributeId(secQuestion.getFormQuestionAttributes().getId());
						
						//Get QuestionAttributes to marshall to XML
						FormQuestionAttributes fqa = fm.getFormQuestionAttributes(sectionId, questionId);
						QuestionAttributesExportImport qaei = new QuestionAttributesExportImport();
						qaei.setQaId(fqa.getId());
						qaei.setQuestionVersion(question.getVersion().getVersionNumber());
						qaei.setRequired(fqa.isRequired());
						qaei.setCalculatedQuestion(fqa.isCalculatedQuestion());
						qaei.setSkipRule(fqa.hasSkipRule());
						qaei.setSkipRuleEquals(fqa.getSkipRuleEquals());
						
						if ( fqa.getSkipRuleType() != null ) {
							qaei.setSkipRuleType(fqa.getSkipRuleType().getValue());
						}
						
						if ( fqa.getSkipRuleOperatorType() != null ) {
							qaei.setSkipRuleOperatorType(fqa.getSkipRuleOperatorType().getValue());
						}
						
						qaei.setHalign(fqa.getHtmlAttributes().getAlign());
						qaei.setValign(fqa.getHtmlAttributes().getvAlign());
						qaei.setTextcolor(fqa.getHtmlAttributes().getColor());
						qaei.setFontface(fqa.getHtmlAttributes().getFontFace());
						qaei.setFontsize(fqa.getHtmlAttributes().getFontSize());
						qaei.setIndent(fqa.getHtmlAttributes().getIndent());
						qaei.setRangeOperator(fqa.getRangeOperator());
						qaei.setRangeValue1(fqa.getRangeValue1());
						qaei.setRangeValue2(fqa.getRangeValue2());
						qaei.setLabel(fqa.getLabel());
						qaei.setAnswerType(fqa.getAnswerType().getValue());
						qaei.setMinCharacters(fqa.getMinCharacters());
						qaei.setMaxCharacters(fqa.getMaxCharacters());
						qaei.setHorizontalDisplay(fqa.isHorizontalDisplay());
						qaei.setHorizDisplayBreak(fqa.isHorizDisplayBreak());
						qaei.setTextareaHeight(fqa.getTextareaHeight());
						qaei.setTextareaWidth(fqa.getTextareaWidth());
						qaei.setTextboxLength(fqa.getTextboxLength());
						qaei.setDataSpring(fqa.isDataSpring());
						qaei.setHtmlText(fqa.getHtmlText());
						qaei.setPrepopulation(fqa.isPrepopulation());
						qaei.setPrepopulationValue(fqa.getPrepopulationValue());
						qaei.setDecimalPrecision(fqa.getDecimalPrecision());
						qaei.setHasUnitConversionFactor(fqa.isHasUnitConversionFactor());
						qaei.setUnitConversionFactor(fqa.getUnitConversionFactor());
						qaei.setDataElementName(fqa.getDataElementName());
						qaei.setSectionId(sectionId);
						qaei.setEmailTrigger(fqa.getEmailTrigger());
						qaei.setHtmlText(fqa.getHtmlText());
						qaei.setTableHeaderType(fqa.getTableHeaderType());
						qaei.setShowText(fqa.isShowText()); 
						
						if ( fqa instanceof CalculatedFormQuestionAttributes ) {
							CalculatedFormQuestionAttributes calFormQuestAttrs = (CalculatedFormQuestionAttributes) fqa;
							qaei.setCalculation(calFormQuestAttrs.getCalculation());
							
							if ( calFormQuestAttrs.getCalculationType() != null ) {
								qaei.setCalculationType(calFormQuestAttrs.getCalculationType().getValue());
							}
							
							if (calFormQuestAttrs.getConversionFactor() != null) {
								qaei.setDateConversionFactor(calFormQuestAttrs.getConversionFactor().getValue());
							}
						}
						
						//Set QuestionAttributesExportImport object to get marshalled to XML
						List<String> qSkip = new ArrayList<String>();
						
						if ( !fqa.getQuestionsToSkip().isEmpty() ) {
							for ( Question q : fqa.getQuestionsToSkip() ) {
								String tmp = new String("S_" + q.getSkipSectionId() + "_Q_" + q.getId());
								qSkip.add(tmp);
							}
							
							qsei.setQuestionsToSkip(qSkip);
						}
						
						qsei.setQaei(qaei);
						questionInSectionList.add(qsei);
					}
					
					sectionExport.setSectionQuestion(questionInSectionList);
					sectionExportList.add(sectionExport);
				}
			}
		}
		
		formInfo.setSections(sectionExportList);
		
		return formInfo;
	}

	
	/**
	 * Method to export Question to XML
	 * @param formId
	 * @param protocolId
	 * @param imageFolderPath
	 * @return List of QuestionExportImport
	 * @throws NumberFormatException
	 * @throws CtdbException
	 * @throws SQLException
	 * @throws IOException
	 */
	public static List<QuestionExportImport> exportQuestionToXML(int formId, int protocolId, String imageFolderPath) throws 
			NumberFormatException, SQLException, IOException, CtdbException, IIOException {
		
		QuestionManager qm = new QuestionManager();
		FormManager fm = new FormManager();

		List<Integer> uniqueQList = fm.getUniqueQuestionsForFormInStudy(formId, protocolId);
		Iterator<Integer> it = uniqueQList.iterator();

		// List to store all xml elements for questions exports to marshall in XML
		List<QuestionExportImport> qeList = new ArrayList<QuestionExportImport>();
		// List to store question options
		List<Answer> questionOptionsList = null;
		while (it.hasNext()) {
			Integer uniqueQid = it.next();
			Question q = qm.getQuestion(uniqueQid);
			// Set all the required fields from question to new object QuestionExportImport
			QuestionExportImport qe = new QuestionExportImport();
			qe.setId(q.getId());
			// remove "-QuesVersion" suffix from question name that was added from copy-right forms
			qe.setName(q.getName().replaceAll("(-\\d+)+$", ""));
			qe.setText(q.getText());
			qe.setDescriptionUp(q.getDescriptionUp());
			qe.setDescriptionDown(q.getDescriptionDown());
			qe.setDefaultValue(q.getDefaultValue());
			qe.setUnansweredValue(q.getUnansweredValue());
			qe.setType(QuestionType.getByQT(q.getType()));
			qe.setCopyRight(q.getCopyRight());
			qe.setTextBlockHtmlText(q.getHtmltext());

			// Create VisualScaleExport Object only for Question type Visual Scale
			if (q.getType().toString().equals(CtdbConstants.QUESTION_VISUAL_SCALE_TYPE)) {
				VisualScale vs = qm.getVisualScale(q.getId(), q.getVersion().getVersionNumber());
				// Store in VisualScaleExport object to marshall
				VisualScaleExportImport vse = new VisualScaleExportImport();
				vse.setCenterText(vs.getCenterText());
				vse.setLeftText(vs.getLeftText());
				vse.setRightText(vs.getRightText());
				vse.setRangeStart(vs.getRangeStart());
				vse.setRangeEnd(vs.getRangeEnd());
				vse.setWidth(vs.getWidth());
				vse.setShowHandle(vs.isShowHandle());
				vse.setVersion(q.getVersion().getVersionNumber());
				qe.setVisualScaleParameters(vse);
			}

			// Create ImageMapExportImport Object only for Question type Image Map
			if (q.getType().toString().equals(CtdbConstants.QUESTION_IMAGE_MAP_TYPE)) {
				ImageMapExportImport iMapExport= qm.getImageMapExport(q.getId(), q.getVersion().getVersionNumber());
				List<ImageMapValuesExportImport> iMapOptionsList = qm.getImageMapValuesList(q.getId(), q.getVersion().getVersionNumber());
				iMapExport.setiMapValues(iMapOptionsList);

				String fileName = iMapExport.getImageFileName();
				String filePhysicalLocation = imageFolderPath + File.separator + fileName;
				// ENCODING image from disk to XML to marshal it to XML
				QuestionGraphic qg = new QuestionGraphic();
				BufferedImage img = ImageIO.read(new File(filePhysicalLocation));
				ByteArrayOutputStream baos = new ByteArrayOutputStream();
				ImageIO.write(img, "jpg", baos);
				baos.flush();
				String encodedImage = new String(Base64.encodeBase64(baos.toByteArray()));
				baos.close();
				qg.setFileName(fileName);
				qg.setQuestionGraphic(encodedImage);
				iMapExport.setMapGraphic(qg);
				qe.setImageMap(iMapExport);
			}

			//Generate Question Answers Options list only for SELECT, MULTI-SELECT, RADIO, CHECKBOX
			String questionType = q.getType().toString();
			if (questionType.equals(CtdbConstants.QUESTION_CHECKBOX_TYPE)
					|| questionType.equals(CtdbConstants.QUESTION_RADIO_TYPE)
					|| questionType.equals(CtdbConstants.QUESTION_SELECT_TYPE)
					|| questionType.equals(CtdbConstants.QUESTION_MULTI_SELECT_TYPE)) {
				ArrayList<QuestionOptionsExportImport> qoList = new ArrayList<QuestionOptionsExportImport>();
				questionOptionsList = q.getAnswers();
				for (Answer a : questionOptionsList) {
					QuestionOptionsExportImport qo = new QuestionOptionsExportImport();
					qo.setScore(a.getScore());
					qo.setSubmittedValue(a.getSubmittedValue());
					qo.setDisplay(a.getDisplay());
					qo.setIncludeOtherOption(q.isIncludeOtherOption());
					qoList.add(qo);
				}
				qe.setQuestionOptions(qoList);
			}

			//TODO Location to store images for graphics question
			// Get all images in from Question object and iterate through them to marshal encrypted image to xml
			List imageList = q.getImages();
			ArrayList<QuestionGraphic> qgList = new ArrayList<QuestionGraphic>();
			for (Object o : imageList) {
				String fileName = o.toString();
				String filePhysicalLocation = imageFolderPath + File.separator + fileName;
				// ENCODING image from disk to XML to marshal it to XML
				BufferedImage img = ImageIO.read(new File(filePhysicalLocation));
				ByteArrayOutputStream baos = new ByteArrayOutputStream();
				ImageIO.write(img, "jpg", baos);
				baos.flush();
				String encodedImage = new String(Base64.encodeBase64(baos.toByteArray()));
				QuestionGraphic qg = new QuestionGraphic();
				qg.setQuestionGraphic(encodedImage);
				qg.setFileName(fileName);
				qgList.add(qg);
				baos.close();
			}
			qe.setQuestionsGraphics(qgList);
			qeList.add(qe);
		}
		return qeList;
	}
	
}
