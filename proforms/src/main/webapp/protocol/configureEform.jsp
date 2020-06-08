<jsp:include page="/common/doctype.jsp" />
<%@ page import="java.util.Locale" %>
<%@ page import="gov.nih.nichd.ctdb.common.StrutsConstants" %>
<%@ page import="gov.nih.nichd.ctdb.common.CtdbConstants"%>
<%@ page import="gov.nih.nichd.ctdb.security.domain.Privilege" %>
<%@ page import="gov.nih.nichd.ctdb.protocol.domain.Protocol" %>
<%@ page import="gov.nih.nichd.ctdb.security.domain.User"%>
<%@ page import="java.util.HashMap"%>
<%@ page import="java.util.List"%>
<%@ page import="java.util.ArrayList"%>
<%@ page import="java.util.Iterator"%>
<%@ page import="java.util.Set"%>
<%@ page import="gov.nih.nichd.ctdb.form.domain.Section"%>
<%@ page import="gov.nih.nichd.ctdb.question.domain.Question"%>
<%@ page import="gov.nih.nichd.ctdb.question.domain.QuestionType"%>
<%@ page import="gov.nih.nichd.ctdb.question.domain.Answer"%>
<%@ taglib uri="/struts-tags" prefix="s" %>
<%@ taglib uri="/WEB-INF/datatables.tld" prefix="idt" %>
<%@ taglib uri="/WEB-INF/security.tld" prefix="security" %>

<jsp:include page="/common/header_struts2.jsp" />
<link rel="stylesheet" type="text/css" href="<s:property value="#webRoot"/>/common/css/configurePSR.css">
<script type="text/javascript" src="<s:property value='#webRoot'/>/common/js/jquery.form.min.js"></script>
<script type="text/javascript">
var calcRuleQuestionsList;
var calcRuleDependentQuestionsJSONList;
var skipRuleQuestionsJSONList;
var skipRuleDependentQuestionsJSONList;
</script>


<html>
<%
	String eformName = (String)request.getAttribute(CtdbConstants.EFORM_NAME);
%>

<h3><s:text name="protocol.eform.configure.title.display"/></h3>
<p><b><%= eformName %></b></p>
<br>

<%
	//write out the sections and questions along for the eform (but dont show required)
	ArrayList<Section> sectionList = (ArrayList)request.getAttribute(CtdbConstants.EFORM_SECTIONLIST);
	Iterator<Section> sectionIter = sectionList.iterator();
	while(sectionIter.hasNext()) {
		Section section = sectionIter.next();
		int sectionId = section.getId();
		String sectionName = section.getName();
		sectionName = sectionName.replaceAll("\\<[^>]*>","");
		String sectionIdText = section.getIdText();
		boolean isRepeatable = section.isRepeatable();
		int parent = section.getRepeatedSectionParent();
		boolean hasAnyRequiredQuestions = section.isHasAnyRequiredQuestions();	
%>
			<div id="<%=sectionIdText%>_div" class="sectionDiv" sid="<%= sectionId %>" isRepeatable="<%= isRepeatable %>" parent="<%= parent %>">
				<table class='configurePsr-table'>
<%
				if(hasAnyRequiredQuestions) {
%>
				<thead>
					<th ><div class='more thWrapper'><%= sectionName %></div></th>
					<th><input type="radio" checked="checked" disabled="true">Show</th>
					<th><input type="radio" value="hide" disabled="true">Hide</th>
				</thead>
				<tbody>

<%
					
				}else {
%>

				<thead>
					<th ><div class='more thWrapper'><%= sectionName %></div></th>
					<th><input type="radio" class="showHideSelector sectionRadio" name="<%= sectionIdText%>" id="<%= sectionIdText%>_show" value="show" onclick="enableSectionQuestionsAndQuestionPVs('<%=sectionIdText%>_div')"  checked="checked">Show</th>
					<th><input type="radio" class="showHideSelector sectionRadio"  name="<%= sectionIdText%>" id="<%= sectionIdText%>_hide" onclick="validateSection('<%=sectionIdText%>')"  value="hide">Hide</th>
				</thead>
				<tbody>

<%
					
				}
%>
				
<%	
				List<Question> questionList = section.getQuestionList();
				Iterator<Question> questionIter = questionList.iterator();
				boolean isConfigStyle1 = true;
				while(questionIter.hasNext()) {
					Question question = questionIter.next();
					String questionText = question.getText();
					String questionIdText = question.getIdText();
					int questionId = question.getId();
					questionText = questionText.replaceAll("\\<[^>]*>","");
					boolean isQuestionRequired = question.getFormQuestionAttributes().isRequired();
					//need to alternate bg color of questions but also keep the pv rows that
					//may come after it in same color
					String configStyle1 = "configStyle1";
					String configStyle2 = "configStyle2";
					String configStyle = "";
					if(isConfigStyle1) {
						configStyle = configStyle1;
					}else {
						configStyle = configStyle2;
					}

					if(!isQuestionRequired) {
%>
						<tr class="formrow_1 sectionquestion <%= configStyle %>">
							<td><div class='more tdWrapper'>Q: <%= questionText %></div></td>
							<td><input type="radio" class="showHideSelector questionRadio " sid="<%=sectionIdText%>" name="<%= questionIdText%>" id="<%= questionIdText%>_show" value="show" onclick="enableQuestionPVs('<%=sectionIdText%>_div','<%=questionIdText%>')" checked="checked">Show</td>
							<td><input type="radio" class="showHideSelector questionRadio"  sid="<%=sectionIdText%>" name="<%= questionIdText%>" id="<%= questionIdText%>_hide" onclick="validateQuestion('<%=sectionIdText%>','<%=questionIdText%>')"  value="hide">Hide</td>
						</tr>
<%			
					}else {
%>
						<tr class="formrow_1 <%= configStyle %>">
							<td><div class='more tdWrapper'>Q: <span style="color:red">*</span><%= questionText %></div></td>
							<td><input type="radio"  value="show" checked="checked" disabled="true">Show</td>
							<td><input type="radio"  value="hide" disabled="true">Hide</td>
						</tr>					
<%
					}
					if(question.getType() == QuestionType.CHECKBOX || question.getType() == QuestionType.RADIO ||
							question.getType() == QuestionType.SELECT || question.getType() == QuestionType.MULTI_SELECT) {
						
						List<Answer> answersList = question.getAnswers();
						Iterator<Answer> answersIter = answersList.iterator();

%>
						<tr class='collapsible <%= configStyle %>'>
							<td class="pvHeading" ><a>Permissible Values</a></td>
							<td></td>
							<td></td>
						</tr>
						<tr class="pvRow">
						<td colspan="3" class="pvList">
						<table class='configurePsr-pvtable'>
<%							
						

						while(answersIter.hasNext()) {
							Answer answer = answersIter.next();
							String display = answer.getDisplay();
							String answerIdText = answer.getIdText();
							int pvid = answer.getId();
%>		
							
							<tr class="formrow_1 <%= configStyle %>" id="<%=answerIdText%>_div" pvid="<%= pvid%>">
								<td><div class='more tdWrapper pvOptionText'>PV: <%= display %></div></td>
								<td><input type="radio" class="showHideSelector pvRadio " pvid="<%= pvid%>" qid="<%= questionIdText%>" sdiv="<%=sectionIdText%>_div" name="<%= answerIdText %>" id="<%= answerIdText%>_show" value="show" checked="checked">Show</td>
								<td><input type="radio" class="showHideSelector pvRadio" pvid="<%= pvid%>" qid="<%= questionIdText%>" sdiv="<%=sectionIdText%>_div" name="<%= answerIdText %>" id="<%= answerIdText%>_hide" onclick="validatePV('<%=questionIdText%>','<%=answerIdText%>')"  value="hide">Hide</td>
							</tr>
										
<%	
						}
%>
						</td>
						</tr>
						</table>

<%						
						
						
						
					}
					if(isConfigStyle1) {
						isConfigStyle1 = false;
					}else {
						isConfigStyle1 = true;
					}
		}	
%>
			</tbody>
			</table>
			</div> 
<%
	}
%>
	
	<security:hasProtocolPrivilege privilege="editConfigureEform">
		<div class="formrow_1">	
			<input type="button" id="save" value="Save" onclick="save()" />
			<input type="button" id="cancel" value="Cancel" onclick="cancel()" />
		</div>
	</security:hasProtocolPrivilege>
	
	
	<s:form id="myForm" method="post" enctype="multipart/form-data">
		<s:hidden name="eformId" id="eformId" />
		<s:hidden name="hiddenSectionsQuestionsPVsIdsJSON" id="hiddenSectionsQuestionsPVsIdsJSON" />
		<s:hidden name="hasEditPriv" id="hasEditPriv" />
		
		<s:hidden name="calcRuleQuestionsJSON" id="calcRuleQuestionsJSON" />
		<s:hidden name="calcRuleDependentQuestionsJSON" id="calcRuleDependentQuestionsJSON" />
		<s:hidden name="skipRuleQuestionsJSON" id="skipRuleQuestionsJSON" />
		<s:hidden name="skipRuleDependentQuestionsJSON" id="skipRuleDependentQuestionsJSON" />
	</s:form>
	
	
		
		
		
	<script type="text/javascript">


	 function enableSectionQuestionsAndQuestionPVs(sectionIdDiv) {
		 $("#" + sectionIdDiv).find($('.showHideSelector.questionRadio[value="show"]')).attr("checked", true);
	     $("#" + sectionIdDiv).find($('.showHideSelector.questionRadio[value="hide"]')).attr("disabled", false);
	     $("#" + sectionIdDiv).find($('.showHideSelector.questionRadio[value="show"]')).attr("disabled", false);
	     $("#" + sectionIdDiv).find($('.showHideSelector.pvRadio[value="show"][sdiv="' + sectionIdDiv + '"]')).attr("checked", true);
	     $("#" + sectionIdDiv).find($('.showHideSelector.pvRadio[value="hide"][sdiv="' + sectionIdDiv + '"]')).attr("disabled", false);
	     $("#" + sectionIdDiv).find($('.showHideSelector.pvRadio[value="show"][sdiv="' + sectionIdDiv + '"]')).attr("disabled", false);
	 }
	    
	 function disableSectionQuestionsAndQuestionPVs(sectionIdDiv) {
		 $("#" + sectionIdDiv).find($('.showHideSelector.questionRadio[value="hide"]')).attr("checked", true);
	     $("#" + sectionIdDiv).find($('.showHideSelector.questionRadio[value="hide"]')).attr("disabled", true);
	     $("#" + sectionIdDiv).find($('.showHideSelector.questionRadio[value="show"]')).attr("disabled", true);
	     $("#" + sectionIdDiv).find($('.showHideSelector.pvRadio[value="hide"][sdiv="' + sectionIdDiv + '"]')).attr("checked", true);
	     $("#" + sectionIdDiv).find($('.showHideSelector.pvRadio[value="hide"][sdiv="' + sectionIdDiv + '"]')).attr("disabled", true);
	     $("#" + sectionIdDiv).find($('.showHideSelector.pvRadio[value="show"][sdiv="' + sectionIdDiv + '"]')).attr("disabled", true);
		 
	 }

    
    function enableSectionQuestions(sectionIdDiv) {
    	$("#" + sectionIdDiv).find($('.showHideSelector.questionRadio[value="show"]')).attr("checked", true);
    	$("#" + sectionIdDiv).find($('.showHideSelector.questionRadio[value="hide"]')).attr("disabled", false);
    	$("#" + sectionIdDiv).find($('.showHideSelector.questionRadio[value="show"]')).attr("disabled", false);
    }
    
    function disableSectionQuestions(sectionIdDiv) {
    	$("#" + sectionIdDiv).find($('.showHideSelector.questionRadio[value="hide"]')).attr("checked", true);
    	$("#" + sectionIdDiv).find($('.showHideSelector.questionRadio[value="hide"]')).attr("disabled", true);
    	$("#" + sectionIdDiv).find($('.showHideSelector.questionRadio[value="show"]')).attr("disabled", true);
    }
    
    
    
    function enableQuestionPVs(sectionIdDiv,questionIdText) {
    	$("#" + sectionIdDiv).find($('.showHideSelector.pvRadio[value="show"][qid="' + questionIdText + '"]')).attr("checked", true);
    	$("#" + sectionIdDiv).find($('.showHideSelector.pvRadio[value="hide"][qid="' + questionIdText + '"]')).attr("disabled", false);
    	$("#" + sectionIdDiv).find($('.showHideSelector.pvRadio[value="show"][qid="' + questionIdText + '"]')).attr("disabled", false);
    }
    
    function disableQuestionPVs(sectionIdDiv,questionIdText) {
    	$("#" + sectionIdDiv).find($('.showHideSelector.pvRadio[value="hide"][qid="' + questionIdText + '"]')).attr("checked", true);
    	$("#" + sectionIdDiv).find($('.showHideSelector.pvRadio[value="hide"][qid="' + questionIdText + '"]')).attr("disabled", true);
    	$("#" + sectionIdDiv).find($('.showHideSelector.pvRadio[value="show"][qid="' + questionIdText + '"]')).attr("disabled", true);
    }
    
    
    
    
    function validateQuestion(sectionIdText,questionIdText) {
    		var warningMessage = ""
        	var areThereValidationErrors = false;
        	
        	//validate calc
        	var calcRuleWarnings = validateElements(questionIdText,false,calcRuleQuestionsList);
        	var calcRuleWarning = "<p align='left'>-This question contains a calculation rule.</p>";
        	if(calcRuleWarnings) {
        		areThereValidationErrors = true;
        		warningMessage = warningMessage + calcRuleWarning;
        	}
        	//validate calc dependent
        	var calcRuleDependentWarnings = validateElements(questionIdText,false,calcRuleDependentQuestionsList);
        	var calcRuleDependentWarning = "<p align='left'>-This question is used in another question's calculation rule.</p>";
        	if(calcRuleDependentWarnings) {
        		areThereValidationErrors = true;
        		warningMessage = warningMessage + calcRuleDependentWarning;
        	}
        	//validate skip
        	var skipRuleWarnings = validateElements(questionIdText,false,skipRuleQuestionsList);
        	var skipRuleWarning = "<p align='left'>-This question contains a skip rule.</p>";
        	if(skipRuleWarnings) {
        		areThereValidationErrors = true;
        		warningMessage = warningMessage + skipRuleWarning;
        	}
        	//validate skip dependent
        	var skipRuleDependentWarnings = validateElements(questionIdText,false,skipRuleDependentQuestionsList);
        	var skipRuleDependentWarning = "<p align='left'>-This question is  used in another question's skip rule.</p>";
        	if(skipRuleDependentWarnings) {
        		areThereValidationErrors = true;
        		warningMessage = warningMessage + skipRuleDependentWarning;
        	}
        	
 	
        	if(areThereValidationErrors) {
        		warningMessage = warningMessage + "<p align='left'>Do you want to still hide this question?</p>";
        		var warningDialogId = $.ibisMessaging(
        				"dialog", 
        				"warning", 
        				warningMessage, 
        				{
        					buttons: [{
        						text: "Yes", 
        						click: function(){	
        							disableQuestionPVs(sectionIdText + '_div',questionIdText);	
        							$.ibisMessaging("close", {id: warningDialogId});
        						}
        					},
        						{text: "No",
        						click: function(){
        							$("#" + questionIdText + "_show").attr("checked",true);
        							$("#" + questionIdText + "_hide").attr("checked",false);
        							$.ibisMessaging("close", {id: warningDialogId});							
        						}}],
        					modal: true,
        					draggable:false,
        					width: 500
        				}
        		);
        	}else {
        		disableQuestionPVs(sectionIdText + '_div',questionIdText);	
        	}
    }
    
    
    
    
    function validatePV(questionIdText,answerIdText) {
    		var warningMessage = ""
        	var areThereValidationErrors = false;
        	
        	//validate calc
        	var calcRuleWarnings = validateElements(questionIdText,false,calcRuleQuestionsList);
        	var calcRuleWarning = "<p align='left'>-This permissible value is part of a question that contains a calculation rule.</p>";
        	if(calcRuleWarnings) {
        		areThereValidationErrors = true;
        		warningMessage = warningMessage + calcRuleWarning;
        	}
        	//validate calc dependent
        	var calcRuleDependentWarnings = validateElements(questionIdText,false,calcRuleDependentQuestionsList);
        	var calcRuleDependentWarning = "<p align='left'>-This permissible value is part of a question that is used in another question's calculation rule.</p>";
        	if(calcRuleDependentWarnings) {
        		areThereValidationErrors = true;
        		warningMessage = warningMessage + calcRuleDependentWarning;
        	}
        	//validate skip
        	var skipRuleWarnings = validateElements(questionIdText,false,skipRuleQuestionsList);
        	var skipRuleWarning = "<p align='left'>-This permissible value is part of a question that contains a skip rule.</p>";
        	if(skipRuleWarnings) {
        		areThereValidationErrors = true;
        		warningMessage = warningMessage + skipRuleWarning;
        	}
        	//validate skip dependent
        	var skipRuleDependentWarnings = validateElements(questionIdText,false,skipRuleDependentQuestionsList);
        	var skipRuleDependentWarning = "<p align='left'>-This permissible value is part of a question that is  used in another question's skip rule.</p>";
        	if(skipRuleDependentWarnings) {
        		areThereValidationErrors = true;
        		warningMessage = warningMessage + skipRuleDependentWarning;
        	}
        	

        	if(areThereValidationErrors) {
        		warningMessage = warningMessage + "<p align='left'>Do you want to still hide this permissible value?</p>";
        		var warningDialogId = $.ibisMessaging(
        				"dialog", 
        				"warning", 
        				warningMessage, 
        				{
        					buttons: [{
        						text: "Yes", 
        						click: function(){		
        							$.ibisMessaging("close", {id: warningDialogId});
        						}
        					},
        						{text: "No",
        						click: function(){
        							$("#" + answerIdText + "_show").attr("checked",true);
        							$("#" + answerIdText + "_hide").attr("checked",false);
        							$.ibisMessaging("close", {id: warningDialogId});							
        						}}],
        					modal: true,
        					draggable:false,
        					width: 500
        				}
        		);
        	}
    	

    }
    
    

    function validateSection(sectionIdText) {
    	var warningMessage = ""
    	var areThereValidationErrors = false;
    	
    	//validate calc
    	var calcRuleWarnings = validateElements(sectionIdText,true,calcRuleQuestionsList);
    	var calcRuleWarning = "<p align='left'>-This section contains one or more questions with a calculation rule.</p>";
    	if(calcRuleWarnings) {
    		areThereValidationErrors = true;
    		warningMessage = warningMessage + calcRuleWarning;
    	}
    	//validate calc dependent
    	var calcRuleDependentWarnings = validateElements(sectionIdText,true,calcRuleDependentQuestionsList);
    	var calcRuleDependentWarning = "<p align='left'>-This section contains one or more questions that are used in another question's calculation rule.</p>";
    	if(calcRuleDependentWarnings) {
    		areThereValidationErrors = true;
    		warningMessage = warningMessage + calcRuleDependentWarning;
    	}
    	//validate skip
    	var skipRuleWarnings = validateElements(sectionIdText,true,skipRuleQuestionsList);
    	var skipRuleWarning = "<p align='left'>-This section contains one or more questions with a skip rule.</p>";
    	if(skipRuleWarnings) {
    		areThereValidationErrors = true;
    		warningMessage = warningMessage + skipRuleWarning;
    	}
    	//validate skip dependent
    	var skipRuleDependentWarnings = validateElements(sectionIdText,true,skipRuleDependentQuestionsList);
    	var skipRuleDependentWarning = "<p align='left'>-This section contains one or more questions that are used in another question's skip rule.</p>";
    	if(skipRuleDependentWarnings) {
    		areThereValidationErrors = true;
    		warningMessage = warningMessage + skipRuleDependentWarning;
    	}
    	
    	
    	
    	if(areThereValidationErrors) {
    		warningMessage = warningMessage + "<p align='left'>Do you want to still hide this section?</p>";
    		var warningDialogId = $.ibisMessaging(
    				"dialog", 
    				"warning", 
    				warningMessage, 
    				{
    					buttons: [{
    						text: "Yes", 
    						click: function(){	
    							disableSectionQuestionsAndQuestionPVs(sectionIdText + '_div');	
    							$.ibisMessaging("close", {id: warningDialogId});
    						}
    					},
    						{text: "No",
    						click: function(){
    							$("#" + sectionIdText + "_show").attr("checked",true);
    							$("#" + sectionIdText + "_hide").attr("checked",false);
    							$.ibisMessaging("close", {id: warningDialogId});							
    						}}],
    					modal: true,
    					draggable:false,
    					width: 500
    				}
    		);
    	}else {
    		disableSectionQuestionsAndQuestionPVs(sectionIdText + '_div');	
    	}
	
    }
    
    
    
    
 
    
    function validateElements(hideElementIdText,isSection,elementList) {
    	var validationErr = false;
    	for(var i=0;i<elementList.length;i++) {
			var elementIdText = elementList[i];
			if(isSection) {
				elementIdText = elementIdText.substring(0,elementIdText.indexOf("_Q_"));
			}
			if(elementIdText == hideElementIdText) {
				validationErr = true;
				break;
			}
		}
    	return validationErr;
    }
    
    

	
	function save() {
		var webRoot = "<s:property value='#webRoot'/>";
		var configurePSRHomeUrl = webRoot + "/protocol/configurePSReFormsHome.action";
		var sectionsQuestionsPVsIdsToHideArray = new Array();
		var selectedHideInputs = $('.showHideSelector[value="hide"]:checked:enabled');
		selectedHideInputs.each(function(index) {
			var sectionQuestionPVId =  $(this).attr("name");
			sectionsQuestionsPVsIdsToHideArray.push(sectionQuestionPVId);
		});
		
		

		var sectionsQuestionsPVsIdsToHideArrayString = JSON.stringify(sectionsQuestionsPVsIdsToHideArray);
		$("#hiddenSectionsQuestionsPVsIdsJSON").val(sectionsQuestionsPVsIdsToHideArrayString);
		$('#myForm').ajaxSubmit({
		    url: "<s:property value="#webRoot"/>/protocol/configureEformAjax.action?action=saveHiddenElements",
		    data: "",
		    success: function(data) {
		    	if(data == ""){
		    		redirectWithReferrer(configurePSRHomeUrl);
		    	}else{
		    		//errors
		    	}
		    },	
		    error: function(jqXHR, textStatus, errorThrown) {
		        document.write(jqXHR.responseText + ':' + textStatus + ':' + errorThrown);
		    }
		});	
	}
	
	function cancel() {
		var webRoot = "<s:property value='#webRoot'/>";
		var configurePSRHomeUrl = webRoot + "/protocol/configurePSReFormsHome.action";
		redirectWithReferrer(configurePSRHomeUrl);
	}
	
	
	
	$(document).ready(function() {	

	    $(".collapsible").click(function() {
	       this.classList.toggle("active");
	       var content = this.nextElementSibling;
	       if (content.style.display === "table-row") {
	         content.style.display = "none";
	       } else {
	         content.style.display = "table-row";
	       }
	    });

	    
		//check appropriate hide radio options in edit mode
		var idList = JSON.parse($("#hiddenSectionsQuestionsPVsIdsJSON").val());
		for(var i=0;i<idList.length;i++) {
			var elementId = idList[i];
			$("#" + elementId + "_hide").attr("checked",true);
			//if elementid is a pv, we need to call the click function to expand the list in edit mode
			var num_underscores = elementId.match(/_/gi).length;
			if(num_underscores == 4) {//it is pv
				var pvElement = elementId+"_div";
				var $pvHeading = $("#"+ pvElement).parents(".pvRow").eq(0).prev();
				if(!$pvHeading.hasClass("active")) {
					$pvHeading.click();
				}
				
			}
			
		}
		
		calcRuleQuestionsList = JSON.parse($("#calcRuleQuestionsJSON").val());
		calcRuleDependentQuestionsList = JSON.parse($("#calcRuleDependentQuestionsJSON").val());
		skipRuleQuestionsList = JSON.parse($("#skipRuleQuestionsJSON").val());
		skipRuleDependentQuestionsList = JSON.parse($("#skipRuleDependentQuestionsJSON").val());
		
		//if in edit mode, the section is set to hide, we need to call disableSectionQuestion function to disable the questions in it
		var selectedHideSectionInputs = $('.showHideSelector.sectionRadio[value="hide"]:checked');
		selectedHideSectionInputs.each(function(index) {
			var sectionIdName =  $(this).attr("name");
			disableSectionQuestions(sectionIdName + "_div");
		});
		//if in edit mode, the question is set to hide, we need to call disableQuestionPVs function to disable the pvs in it
		var selectedHideQuestionInputs = $('.showHideSelector.questionRadio[value="hide"]:checked');
		selectedHideQuestionInputs.each(function(index) {
			var questionIdName =  $(this).attr("name");
			var sectionIdName = $(this).attr("sid");
			disableQuestionPVs(sectionIdName + "_div", questionIdName);
		});
		

		//disable all inputs if user does not have edit priv (if user only has viewpsr priv)
		if($("#hasEditPriv").val() == "false") {
			$('.showHideSelector').attr("disabled",true);
		}

		//code to handle the shore more/show less for text
	    var showChar = 130;  // How many characters are shown by default
	    var ellipsestext = "...";
	    var moretext = "Show more >";
	    var lesstext = "Show less";
	    $('.more').each(function() {
	        var content = $(this).html();
	      	if(content.length > showChar) {
	            var c = content.substr(0, showChar);
	            var h = content.substr(showChar, content.length - showChar);
	            var html = c + '<span class="moreellipses">' + ellipsestext+ '&nbsp;</span><span class="morecontent"><span>' + h + '</span>&nbsp;&nbsp;<a href="" class="morelink">' + moretext + '</a></span>';
	            $(this).html(html);
	        }
	    });
	 
	    $(".morelink").click(function(){
	        if($(this).hasClass("less")) {
	            $(this).removeClass("less");
	            $(this).html(moretext);
	        } else {
	            $(this).addClass("less");
	            $(this).html(lesstext);
	        }
	        $(this).parent().prev().toggle();
	        $(this).prev().toggle();
	        return false;
	    });	
	    
	    
	    
	    
	    
	});
	
	
	
	</script>


<%-- Include Footer --%>
<jsp:include page="/common/footer_struts2.jsp" />


</html>
