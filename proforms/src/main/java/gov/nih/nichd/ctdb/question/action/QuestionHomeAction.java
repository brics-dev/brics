package gov.nih.nichd.ctdb.question.action;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.apache.struts2.ServletActionContext;
import org.json.JSONObject;

import gov.nih.nichd.ctdb.common.BaseAction;
import gov.nih.nichd.ctdb.common.CtdbException;
import gov.nih.nichd.ctdb.common.ObjectNotFoundException;
import gov.nih.nichd.ctdb.common.StrutsConstants;
import gov.nih.nichd.ctdb.common.rs;
import gov.nih.nichd.ctdb.common.navigation.LeftNavController;
import gov.nih.nichd.ctdb.form.common.FormConstants;
import gov.nih.nichd.ctdb.question.common.QuestionResultControl;
import gov.nih.nichd.ctdb.question.domain.Answer;
import gov.nih.nichd.ctdb.question.domain.Group;
import gov.nih.nichd.ctdb.question.domain.Question;
import gov.nih.nichd.ctdb.question.domain.QuestionType;
import gov.nih.nichd.ctdb.question.form.QuestionSearchForm;
import gov.nih.nichd.ctdb.question.manager.QuestionManager;
/**
 * QuestionHomeAction will handle all requests to display the home page for Question Library.
 *
 * @author Booz Allen Hamilton
 * @version 1.0
 */
public class QuestionHomeAction extends BaseAction {

	private static final long serialVersionUID = 2639352830992343979L;
	private static final Logger log = Logger.getLogger(QuestionHomeAction.class);
	
    QuestionSearchForm questionSearchForm = new QuestionSearchForm();

    public String execute() throws Exception {
        buildLeftNav(LeftNavController.LEFTNAV_FORM_CREATE);

        session.remove("sectionid");  // make sure user is not sent to section home
        
        String numResultsPerPage = questionSearchForm.getNumResultsPerPage();
        session.put("pagesChoosen", numResultsPerPage != null ? numResultsPerPage : "");

        try {
            QuestionManager qm = new QuestionManager();
            List<Question> questionResults = qm.getQuestions();
            
            session.put("questions", questionResults);
            session.put("types", QuestionType.getDisplayTypes());
            
            List<Group> questionGroups = qm.getGroups();
            Group allGroup = new Group();
            allGroup.setId(Integer.MIN_VALUE);
            allGroup.setName("All Groups");
            questionGroups.add(0, allGroup);
            session.put("groups", questionGroups);

            session.put("numResultsOptions", this.getOptionsFromMessageResources(
            		"app.options.search.numresults"));
            session.put("numResultsPerPageOptions", this.getOptionsFromMessageResources(
            		"app.options.search.numresultsperpage"));
            session.put("medicalCodingStatusOptions", this.getOptionsFromMessageResources(
            		"app.options.search.questionmedicalcodingstatuses", ","));

            if (questionSearchForm.getNumResults() == null) {
                questionSearchForm.setNumResults(getText("app.options.search.numresults.default"));
            }
            if (questionSearchForm.getNumResultsPerPage() == null) {
                questionSearchForm.setNumResultsPerPage(getText("app.options.search.numresultsperpage.default"));
            }

            // Save previous questionSearchForm because when a pagination link is clicked
            // the html form is not submitted and the Action class loses all its data.
            // The session attribute is used in QuestionHomePaginationAction class.
            session.put("previousQuestionSearchForm", questionSearchForm);

            //==========================================Ajax search=================================
        	HttpServletResponse response = ServletActionContext.getResponse();
        	response.setContentType("text/text");
        	PrintWriter out = response.getWriter();
        	
            String action = request.getParameter("action");
            if (action != null && action.equalsIgnoreCase("searchQuestionAjax")) {
            	// in CTDB it only show the latest version questions to show
            	List<Question> finalQuestionResults = getLatestNoDuplicateQuestion(questionResults);
	           	List<Question> nonTextblockQuestionResults = filterTextblocks(finalQuestionResults);
	           	out.print(dataTableBuild(nonTextblockQuestionResults));
            	out.flush();
            	return null;
            }
            
            //====================================Ajax get question Json=============================
            if (action != null && action.equalsIgnoreCase("multiAddQuestionsAjax")) {
            	int qId = Integer.parseInt(request.getParameter("id"));
            	int qVersion = Integer.parseInt(request.getParameter("version"));
            	Question quTemp = qm.getQuestion(qId, qVersion);
            	int versionNumber = quTemp.getVersion().getVersionNumber();
            	
            	// get the select, radio, checkbox, multi-select options
            	quTemp.setAnswers(qm.getAnswers(quTemp.getId(), versionNumber));
            	
            	List<Answer> answers = quTemp.getAnswers();
            	String options = "";
            	for (Answer answer : answers) {
            		if (answer.getDisplay() != null) {
            			String score="", submittedValue="";
            			if (answer.getScore() != Integer.MIN_VALUE) {
            				score = String.valueOf(answer.getScore());
            			}
            			if (answer.getSubmittedValue() != null) {
            				submittedValue = answer.getSubmittedValue();
            			}
            			
	                    options += answer.getDisplay() + " | " + score + " | " + submittedValue + StrutsConstants.alienSymbol;
            		}
            	}
	                  
            	// get image option and image file name
            	String imageOptions = "", imageFileName = "";
            	if (quTemp.getType().getValue() == 9) {
            		imageFileName = qm.getImageMapFileNameByquestionId(quTemp.getId(), versionNumber);
            		List<String> imageMapOptions = qm.getImageMapOptions(quTemp.getId(), versionNumber);
            		
            		for ( String imageOpt : imageMapOptions ) {
            			imageOptions += imageOpt + StrutsConstants.alienSymbol;
            		}
            	}
	                  
            	//get visual scale left, center, right texts
            	String visualScaleInfo = "";
            	if (quTemp.getType().getValue() == 10) {
            		visualScaleInfo = qm.getVisualScaleInfo(quTemp.getId(), versionNumber);
            	}
            	
                // the information question object need in jsp, will be stored in checkbox id
            	JSONObject jsonObj = new JSONObject();
            	jsonObj.put("questionId", quTemp.getId());
            	jsonObj.put("questionVersionString", quTemp.getVersion().getToString());
            	jsonObj.put("questionVersionNumber", versionNumber);
            	jsonObj.put("questionName", quTemp.getName());
            	jsonObj.put("questionText", quTemp.getText());
            	jsonObj.put("descriptionUp", quTemp.getDescriptionUp());
            	jsonObj.put("descriptionDown", quTemp.getDescriptionDown());	              
            	jsonObj.put("questionType", quTemp.getType().getValue());
            	jsonObj.put("options", options);
            	jsonObj.put("imageOption", imageOptions);
            	jsonObj.put("imageFileName", imageFileName);
            	jsonObj.put("visualScaleInfo", visualScaleInfo);
            	jsonObj.put("graphicNames", qm.getQuestionImages(quTemp.getId()));
            	jsonObj.put("defaultValue", quTemp.getDefaultValue());
            	jsonObj.put("unansweredValue", quTemp.getUnansweredValue());
            	jsonObj.put("associatedGroupIds", qm.getAssociatedGroupIds(quTemp.getId()));
            	jsonObj.put("includeOther", quTemp.isIncludeOtherOption());
		              
            	Set<Integer> attachedFormIds = qm.getAttachedFormIds(quTemp.getId(), versionNumber);
            	Integer formId = (Integer) session.get(FormConstants.FORMID);
            	if (formId != null) {
            		attachedFormIds.remove(formId);// don't include the current form Id.
            	}
            	jsonObj.put("attachedFormIds", attachedFormIds);
            	jsonObj.put("hasDecimalPrecision", quTemp.getHasDecimalPrecision());
            	jsonObj.put("hasCalDependent", quTemp.getHasCalDependent());
            	jsonObj.put("prepopulation", quTemp.getPrepopulation());
            	jsonObj.put("attachedFormNames", qm.getCalDependentFormNames(quTemp.getId()));
		              
            	System.out.println(jsonObj);
            	out.print(jsonObj);
            	out.flush();
            	
            	return null;
            }
        }
        catch (CtdbException ce) {
        	log.error("Database error.", ce);
        	return StrutsConstants.FAILURE;
        }
        catch (NumberFormatException nfe) {
        	log.error("Invalid number conversion.", nfe);
        	return StrutsConstants.FAILURE;
        }
        
        return SUCCESS;        	
    }


    private void updateResultControl(QuestionResultControl qrc) {
    	
    	String clicked = questionSearchForm.getClicked();
        if (clicked != null && clicked.equalsIgnoreCase("sort")) {
            // swap sort order only if a sort link was clicked
            if (questionSearchForm.getSortedBy() != null && 
            	questionSearchForm.getSortedBy().equalsIgnoreCase(questionSearchForm.getSortBy())) {
                if (questionSearchForm.getSortOrder().equals(QuestionResultControl.SORT_ASC)) {
                    qrc.setSortOrder(QuestionResultControl.SORT_DESC);
                } else {
                    qrc.setSortOrder(QuestionResultControl.SORT_ASC);
                }
            } else {
                qrc.setSortOrder(QuestionResultControl.SORT_ASC);
            }
            
        } else { // keep current sort order
            qrc.setSortOrder(questionSearchForm.getSortOrder());
        }
        
        if (questionSearchForm.getQuestionId() != null && !questionSearchForm.getQuestionId().equals("")) {
            qrc.setQuestionId(Integer.parseInt(questionSearchForm.getQuestionId()));
        }
        
        qrc.setSortBy(questionSearchForm.getSortBy());
        qrc.setName(questionSearchForm.getQuestionName());
        qrc.setText(questionSearchForm.getQuestionText());
        qrc.setGroupId(questionSearchForm.getQuestionGroup());
        qrc.setType(questionSearchForm.getQuestionType());
        qrc.setCreatedBy(questionSearchForm.getCreatedBy());
        qrc.setNameSearchModifier(questionSearchForm.getNameSearchModifier());
        qrc.setTextSearchModifier(questionSearchForm.getTextSearchModifier());
        
        if (questionSearchForm.getNumResults() != null && !questionSearchForm.getNumResults().equalsIgnoreCase("all")) {
            qrc.setRowNumMax(Integer.parseInt(questionSearchForm.getNumResults()));
        }
        
        if (questionSearchForm.getMedicalCodingStatus().equalsIgnoreCase("all")) {
            qrc.setMedicalCodingStatus(null);
        } else {
            qrc.setMedicalCodingStatus(questionSearchForm.getMedicalCodingStatus());
        }
        qrc.setInCdes(questionSearchForm.isInCdes());
    }
    

    private List<Question> filterTextblocks(List<Question> questionResults) {
    	List<Question> output = new ArrayList<Question>();
    	for (Question question : questionResults) {
    		if (!question.getType().equals(QuestionType.TEXT_BLOCK)) {
    			output.add(question);
    		}
    	}
    	return output;
    }

    
    private List<Question> getLatestNoDuplicateQuestion(List<Question> questionResults) {
    	List<Question> finalQuestionResults= new ArrayList<Question>();
   		for (Question baseQ : questionResults) {
       		boolean flag = true;
       		for (Question rollQ : questionResults) {
       			if (baseQ.getId() == rollQ.getId()) {
       				if (baseQ.getVersion().getVersionNumber() < rollQ.getVersion().getVersionNumber()) {
       					flag = false;
       				}
       			}
       		}
       		
       		if (flag) {
       			boolean flag2 = true;
       			if (finalQuestionResults.isEmpty()) {
       				finalQuestionResults.add(baseQ);		           				
       			} else {
       				for (Question infinalQ : finalQuestionResults) {
       					if (baseQ.getId() == infinalQ.getId()) {
       						flag2 = false;
       					}
       				}
       				if (flag2) {
       					finalQuestionResults.add(baseQ);
       				}
       			}
       		}
       	}
       	
       	// remove the question which has been attached on form
       	String duplicateQIdstr = questionSearchForm.getDuplicateQuestions();
       	if (duplicateQIdstr != null && duplicateQIdstr.length() > 0) {
       		String[] duplicateQidArray = duplicateQIdstr.split(";");
       		
       		for (int i=finalQuestionResults.size()-1; i>=0; i--) {
       			Question checkQ = (Question) finalQuestionResults.get(i);
       			for (int j=0; j<duplicateQidArray.length; j++) {
       				String checkName=duplicateQidArray[j];
       				if (checkQ.getName().equalsIgnoreCase(checkName)) {
       					finalQuestionResults.remove(i);
       				}
       			}
       		}
       	}
       	return finalQuestionResults;
    }
    
    
    // For build out the data table
    private String dataTableBuild(List<Question> finalQuestionResults) throws ObjectNotFoundException, CtdbException {
    	
    	String tableContentHTML = "";
       	for (Question quTemp : finalQuestionResults) {
            // get question groups
            String groupStr = "";
            QuestionManager qm = new QuestionManager();
            List<String> qGroups = qm.getAssociatedGroupIds(quTemp.getId());
            
            if (!qGroups.isEmpty()) {
            	for (String qGroupId : qGroups) {
            		Group theGroup = qm.getGroup(Integer.parseInt(qGroupId));
            		groupStr += theGroup.getName() + ", ";
            	}
            	groupStr = groupStr.substring(0, groupStr.lastIndexOf(", "));
            }
            
            tableContentHTML += 
       					 "<tr>" +
   							"<td>" +
   								"<input id=\""+quTemp.getId()+"_"+quTemp.getVersion().getVersionNumber()+"\" value=\""+quTemp.getType().getValue()+
   										"\" type=\"radio\"  name=\"selectQuestionId\" onclick=\"qSelect();\">"+
   							"</td>" +
   							"<td>"+quTemp.getId()+"</td>" +
   							"<td align=\"left\">"+quTemp.getName()+" ("+quTemp.getVersion().getToString()+")"+"</td>"+
   							"<td align=\"left\">"+quTemp.getText()+"</td>"+
   							"<td align=\"left\">"+quTemp.getType()+"</td>"+
   						  "</tr>";
       	}

       	Locale l = request.getLocale();
       	String tableHTML = 
       			"<table id=\"DataTables_Table_1\">" +
       				"<thead>" +
       					"<tr class=\"tableRowHeader\" role=\"row\">"+
       						"<th>" + "" + "</th>" +
       						"<th class=\"tableCellHeader\">" +
       							rs.getValue("form.section.questionattributes.questionid", l) +   /*ID*/
       						"</th>" +
       						"<th class=\"tableCellHeader\">" +
       							rs.getValue("form.quesitonNameVesrsion", l) +  /*Name*/
       						"</th>" +
       						"<th class=\"tableCellHeader\">" +
       							rs.getValue("form.quesitonText",l) + /*text*/
       						"</th>" +
       						"<th class=\"tableCellHeader\">" +
       							rs.getValue("form.quesitonType",l) +  /*Type*/
       						"</th>" +
       					"</tr>" +
	           		"</thead>" +
	           		"<tbody>" + tableContentHTML + "</tbody>"+
	           	"</table>";
       	
       	return tableHTML;
    }

	public QuestionSearchForm getQuestionSearchForm() {
		return questionSearchForm;
	}

	public void setQuestionSearchForm(QuestionSearchForm questionSearchForm) {
		this.questionSearchForm = questionSearchForm;
	}
}
