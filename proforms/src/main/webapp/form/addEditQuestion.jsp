<%@ page import="gov.nih.nichd.ctdb.common.CtdbConstants,
	gov.nih.nichd.ctdb.question.domain.QuestionType,
	gov.nih.nichd.ctdb.question.domain.Question,gov.nih.nichd.ctdb.common.StrutsConstants,
	java.util.Iterator,gov.nih.nichd.ctdb.form.common.FormConstants,
	gov.nih.nichd.ctdb.question.common.QuestionConstants,
	java.util.ArrayList,
	java.awt.*,
	gov.nih.nichd.ctdb.question.form.AddEditQuestionForm,
	gov.nih.nichd.ctdb.question.form.QuestionSearchForm,
	gov.nih.nichd.ctdb.common.Image,
	gov.nih.nichd.ctdb.question.common.QuestionResultControl,
	java.util.List,
    gov.nih.nichd.ctdb.common.CtdbDomainObject,
    gov.nih.nichd.ctdb.question.manager.QuestionManager"%>
<%@ page import="gov.nih.nichd.ctdb.common.Image"%>
<%@ page import="gov.nih.nichd.ctdb.common.CtdbConstants" %>
<%@ taglib uri="/struts-tags" prefix="s"%>
<%@ taglib uri="/WEB-INF/display.tld" prefix="display" %>
<%@ taglib uri="/WEB-INF/security.tld" prefix="security" %>


<jsp:include page="/common/doctype.jsp" />

<%-- CHECK PRIVILEGES --%>
<security:check privileges="addeditforms"/>
<jsp:useBean id="user" scope="session" class="gov.nih.nichd.ctdb.security.domain.User"/>

<html>

<jsp:include page="/common/templateCss.jsp" />

<script type="text/javascript" src="<s:property value="#webRoot"/>/common/js/timer.js"></script>
<script type="text/javascript" src="<s:property value="#webRoot"/>/common/calculated.js"></script>
<script type="text/javascript" src="<s:property value="#webRoot"/>/common/calendar.js"></script>
<script type="text/javascript" src="<s:property value="#webRoot"/>/common/conversion.js"></script>
<script type="text/javascript" src="<s:property value="#webRoot"/>/common/common.js"></script>

<script type="text/javascript">
var initialType;
var gCountPrevious = 1;
var gLastDivNum = 1;
var workFlow = -1; // to know has user enter step2?
var parsingSymbol = '<%=StrutsConstants.alienSymbol%>';

function loadjscssfile(filename, filetype){
	if (filetype=="js"){ //if filename is a external JavaScript file
		var fileref=document.createElement('script');
		fileref.setAttribute("type","text/javascript");
		fileref.setAttribute("src", filename);
		
	} else if (filetype=="css") { //if filename is an external CSS file
		var fileref=document.createElement("link");
		fileref.setAttribute("rel", "stylesheet");
		fileref.setAttribute("type", "text/css");
		fileref.setAttribute("href", filename);
	}
	if (typeof fileref!="undefined") {
		document.getElementsByTagName("head")[0].appendChild(fileref)
	}
}

$(document).bind("ready", function() {  
    $('#questionText').keyup(function(){  
        var limit = '255';  
        var text = $('#questionText').val();  
        var chars = text.length;  	  
        if (chars > limit) {  
            var new_text = text.substr(0, limit);  	  
            $('#questionText').val(new_text);  
        }  
    });
    
    $('#serchQuestionText').keyup(function(){  
        var limit = '253';  
        var text = $('#serchQuestionText').val();  
        var chars = text.length;  	  
        if (chars > limit) {  
            var new_text = text.substr(0, limit);  	  
            $('#serchQuestionText').val(new_text);  
        }  
    });
    
    $('#addEditQuestionForm :input').change(function() {
		hasChanged=true;
	});
     
 	$("select#questionType").live("change", function(e){
 		var msg="";
		displayQuestionErrorMsg(msg); // clean up previous message
		$('#questionType, #questionType option').attr('disabled',false).show();
		
		var newType = $("select#questionType option:selected").val();
		
		if (questionInfoMode == "add") {
			clearQuestionInf();
    		clearQuestionDetails();
	 	} else {
	 		var q = QuestionHandler.findQuestion(editQuestionDivId);
	 		//alert("Current question count: " + QuestionHandler.getQuestionCount() +": With new question type: "+ newType + ", current Question detail: \n"  + JSON.stringify(q) +"\n\n");
			if (q.hasCalDependent && 
				!((q.questionType == <%=QuestionType.RADIO.getValue()%> && newType == <%=QuestionType.SELECT.getValue()%>) ||
				  (q.questionType == <%=QuestionType.SELECT.getValue()%> && newType == <%=QuestionType.RADIO.getValue()%>))) {
				var formNames="";
				if (q.attachedFormNames != null && q.attachedFormNames.length > 0) {
					formNames = q.attachedFormNames.toString();
				} else {
					formNames = $("#fname").val() + ". Note: You may have to save the form to refresh the question details.";
				}
				
				msg += "<li><font color='red'>The question is being used in the calculation rule in another question(s). You must first remove the calculation rule used in form(s): " 
						+ formNames + "</font></li>";
			}
			
  			if (msg.length > 0) {
	 			msg = "<b><font color='red'>Error: Changing question type is not allowed due to following reason(s):</font></b><br><ul>" + msg + "</ul>";
	 			QuestionHandler.setAllowedQuestionTypeGroups(q);
				$("#questionType option:[value="+q.questionType+"]").attr('selected',"selected");
				displayQuestionErrorMsg(msg);
				return false;
			};
			
			//reset certain fields/values based on new quesetion type, i.e. existing ptions for new textarea type 
	 		q.questionType = newType;
	 		QuestionHandler.populateQuestion(q);
	 	}

    	switchInterfaceByQuestionType(newType);
    	checkAnswerType();
    	checkDataSpring();
    	
    	//existing functionality to allow user to repopulate certain fields/options according to linked DE.
    	checkMatchInCreateEdit(newType);

    	//make sure the dropdown question type set up specifically according to current question type
		if (questionInfoMode != "add") {
	    	QuestionHandler.setAllowedQuestionTypeGroups(q);
		}
    });

 	// Clean up error message display upon user's next action
 	$("div#addEditQuestionFancyBox").find("a, input:radio, input.checkbox, input:text").live("click", function() {
		displayQuestionErrorMsg("");
	});
 	
 	function displayQuestionErrorMsg(errMsg) {
 		if (typeof(errMsg) != "undefined" && errMsg.length > 0) {

 			$.ibisMessaging(
				"primary", 
				"error", 
				errMsg,
				{
					container: "#questionValidationErrorsDiv"
				});
 			$("#questionValidationErrorsDiv").show();
 			$('html,body').animate({scrollTop:$('#questionValidationErrorsDiv').offset().top},800);
 			return false;
 			
 		} else {
			$('#questionValidationErrorsDiv').html('').hide();
		}
	}
});
			
function checkQuestionType(selectedValue){
	clearQuestionInf();
	clearQuestionDetails();
	checkAnswerType();
	switchInterfaceByQuestionType(selectedValue);
	checkDataSpring();
}

function resetQuestionInterface(){ // just the display, not the value due to question type swotching and info retaining
	$("div#step3 h3, div#step3 div[align='left'], #valueDiv, #optionScoreTD, table#questionInfoShow table").hide();
	$("div#step3 h3").find("span.expandCollapseController").text("[+] ");  // reset the expand icon
	$("#defaultH,#graphicH, #formatH").show();
	$('#answerType, #minCharacters,#maxCharacters,#horizontalDisplay,#horizDisplayBreak').attr('disabled', true);
}

function switchInterfaceByQuestionType(selectedValue){
	resetQuestionInterface();
	
	if (selectedValue == <%=Integer.MIN_VALUE%>) { //ONLY SEEN IN CREATING NEW QUESTION
		$('#questionName, #questionText,#descriptionUp,#descriptionDown').attr('disabled',true);			
		$('#createAddBtu').hide();
		resetWorkFlow(2);
	} else {
		$('#questionText, #descriptionUp, #descriptionDown').attr('disabled',false);				
		if (questionInfoMode == 'add') { 
			$('#createAddBtu').show();
			$('#questionName').attr('disabled',false);
		}
	}
	
	if (selectedValue == <%=QuestionType.IMAGE_MAP.getValue()%>) { //image map
		$('#imageMape,#validationH').show();
		if (questionInfoMode !='edit') {
			$('#imageTypeFr').attr("src", '<s:property value="#webRoot"/>/question/addImageType.action');
		}
		
	} else if (selectedValue == <%=QuestionType.VISUAL_SCALE.getValue()%>) { // visual scale
		$('table#visualScale').show();
	
	} else if (selectedValue == <%=QuestionType.File.getValue()%>){ // File
		$("#defaultH").hide();
	
	} else if (selectedValue == <%=QuestionType.CHECKBOX.getValue()%> ||
			  selectedValue == <%=QuestionType.MULTI_SELECT.getValue()%> ||
			  selectedValue == <%=QuestionType.RADIO.getValue()%> ||
			  selectedValue == <%=QuestionType.SELECT.getValue()%>) { 
		//checkbox, multi-select, radio, Select
		$('#selects, #graphicH, #skipRuleH, #validationH, #formatH').show();
		$('#horizontalDisplay,#horizDisplayBreak').attr('disabled', false);
		
		if (selectedValue == <%=QuestionType.SELECT.getValue()%>) {
			$("#prepopulationH").show();
		} 
		
		if ($('#formName option:selected').attr('name') == 'Patient') { 
			// the non-subject form doesn't have e-mail trigger functionality
			$('#emailNotificationsH').show();			
		}
		if (selectedValue == <%=QuestionType.RADIO.getValue()%> || selectedValue == <%=QuestionType.SELECT.getValue()%>) {
			$('#optionScoreTD').show();
		}
		
	} else if (selectedValue == <%=QuestionType.TEXTAREA.getValue()%>) { //textarea
		$('#validationH').show();
		$('#answerType').attr("disabled", false);
		if ($('select#answerType').val() == 1) {
			$('#minCharacters,#maxCharacters').attr("disabled", false);
		}
		
	} else if (selectedValue == <%=QuestionType.TEXTBOX.getValue()%>) { // textbox
		$('#skipRuleH, #validationH, #prepopulationH').show();
		$('#answerType').attr("disabled", false);
		if ($('select#answerType').val() == 1) {
			$('#minCharacters,#maxCharacters').attr("disabled", false);
		}
	}
}


function upCase(e,obj) {
	var evt=e||window.event;
    keyPress =evt.which|| evt.keyCode;
    if (keyPress == 37 || keyPress == 38 || keyPress == 39 || keyPress ==40) {
       return;
    }
    
    var cursorPosition = doGetCaretPosition(obj);
    obj.value=obj.value.toUpperCase();
    setCaretPosition(obj, cursorPosition);
}


function doGetCaretPosition(ctrl) {
	var CaretPos = 0;	// IE Support
	if (document.selection) {
		ctrl.focus ();
		var Sel = document.selection.createRange ();
		Sel.moveStart ('character', -ctrl.value.length);
		CaretPos = Sel.text.length;
	} else if (ctrl.selectionStart || ctrl.selectionStart == '0') { // Firefox support
		CaretPos = ctrl.selectionStart;
	}
	return CaretPos;
}


function setCaretPosition(ctrl, pos){
	if (ctrl.setSelectionRange) {
		ctrl.focus();
		ctrl.setSelectionRange(pos,pos);
		
	} else if (ctrl.createTextRange) {
		var range = ctrl.createTextRange();
		range.collapse(true);
		range.moveEnd('character', pos);
		range.moveStart('character', pos);
		range.select();
	}
}

<%--moved by Ching-Heng from questionWizardVisulaScale--%>
	function changeSliderWidth () {
        var tehWidth = Math.min(150, $('#scaleWidth').val());
        $('#scaleWidth').val(tehWidth);
        $('#sliderEx').attr("width", tehWidth +"mm");
        //s.recalculate();
	}

	function updateTxt(obj, whichOne) {
        if (whichOne == 'center') {
           $('#centerT').html(obj.value);
        } else if (whichOne == 'right') {
            $('#rightT').html( obj.value);
        } else if (whichOne == 'left') {
            $('#leftT').html(obj.value);
        }
	}
	
	function updateCursor (obj) {
        if (obj.checked) {
            s.handle.style.display = 'block'; 
        } else {
            s.handle.style.display='none';
        }
    }
	
	function editAnswerOption () {
	<%if (session.getAttribute("isAdministeredOnForm") != null && session.getAttribute("isAdministeredOnForm").equals("true")) {%>
  		 return;
	<%} else { %>
		 var codeVal = document.getElementById('options').value;
		 if (codeVal!='<%=CtdbConstants.OTHER_OPTION%>') {
			 separated = codeVal.split("|");
			 document.getElementById('optionChoice').value = separated[0];
			 document.getElementById('optionScore').value = separated[1];
			 document.getElementById('optionSubmittedValue').value = separated[2];
			 removeItem(document.getElementById('options'), 'NO');
	 	 }
	<%}%>
	}
	
	function checkSwitch(){
		resetWorkFlow(2);
		if($('#cQ').is(':checked')){
			questionInfoMode='add';
			$('#questionType, #questionType option').attr('disabled',false).show();
			$('#questionName').attr('disabled',false);
			$('#addEditQuestion').show();
			$('#searchQuestion').hide();
			$('#myQuestions, #questionValidationErrorsDiv').html('');
			$("#sQ").attr('checked', false);
			$('#graphicFr').attr("src","<s:property value="#webRoot"/>/question/showQuestionImage.action");
			// make the defualt question type is "Please select a question type"
			$('#questionType').val(<%=Integer.MIN_VALUE%>);
			checkQuestionType('<%=Integer.MIN_VALUE%>');
			
			$('#searchAddBtu').hide();
		 	$('#createAddBtu').hide();
		}
		else if($('#sQ').is(':checked')){
			questionInfoMode='search';
			$('#addEditQuestion').attr("style","display: none");
			$('#searchQuestion').attr("style","");
			$('#questionValidationErrorsDiv').html('');
			doSearchQuestionAjaxPost();
			$("#cQ").attr('checked', false);
			$('#searchAddBtu').hide(); // still hide, untill user select a question
			$('#createAddBtu').hide();
		}
	}
	
	function doSearchQuestionAjaxPost() {
		
		// here make the question won't show when this question already attached into same "section"
		var activeSectionID = currentSectionID;
		
		// Change to use question name
		var duplicatNames='';
		for (var i=0;i<questionsArray.length;i++) {
			if (questionsArray[i].sectionId==activeSectionID || 'S_'+questionsArray[i].sectionId==activeSectionID) {
				var questionText=questionsArray[i].questionName;
				var questionName;

				if (document.getElementsByName("formForm.copyRight")[0].checked) {
					var arr=questionText.split("-"); //filter the copyright question number ex.AGE-1 
					if (arr.length > 1) {
						questionName=";"+arr[0];	
						for (var j=0;j<arr[1];j++) {
							var no=j+1;
							questionName+=';'+arr[0]+'-'+no;
						}
					} else {
						questionName=questionText;
					}
				} else {
					questionName=questionText;
				}
				duplicatNames+=questionName+';';
			}
		}
		$('#duplicateQuestions').val(duplicatNames);
		
		var params = $('#searchQuestion form').serialize();
		var questionInfoURL= "<s:property value="#webRoot"/>/form/searchQuestions.action?action=searchQuestionAjax";
		$.ajax({
			type:"post",
			url:questionInfoURL,
			data:params,
			beforeSend: function() {
				var qId=$('#questionId').val();
				if (isNaN(qId) || qId.indexOf('.') > -1) {
					var errorString = "<font color='red'>The question ID must be numeric.</font>";
					$('#questionValidationErrorsDiv').html(errorString).show();
					return false;
				} else{
					$('#questionValidationErrorsDiv').hide();
					return true;
				}
			},
			success: function(response) {
				//alert("Response from doSearchQuestionAjaxPost function:\n" + response);
				$('#myQuestions').html('');
				$('#myQuestions').html(response);
				resetSearchQuestionInf();
			},
			complete: function() {
				IbisDataTables.fullBuild();
			},
			error: function(e) {
				alert("error" + e);
			}
		});
	}
		
	function checkAnswerType() {
        var aType = $('select#answerType').val();
        var qType = $('select#questionType').val();

        $('#editCalculationTable, #calculationEditButton, #decimalPrecisionDiv, #prepopulationH,#prepopulationDiv,#conversionFactorH ,#conversionFactorDiv').hide();
		$('#window').html('');
	    $("select#conversionFactor").val("none");
		
	    if (aType == 1) {
	     	if ($('select#answerType').is(":enabled")) { // String
				$('#minCharacters, #maxCharacters').attr("disabled", false);
	     	}
	    } else {  // non-String: numberic, date, or date-time
			$('#minCharacters').val(0).attr("disabled", true);
			$('#maxCharacters').val(4000).attr("disabled", true);
			if (qType ==1) { // TextBbox 
			 	$('#calculationEditButton').show();
			}
	     }
		
	     // range operator
	     if (aType == 2) { //numeric 
		    $("select#rangeOperator").removeAttr("disabled");
	     } else {
	    	$("#rangeValue1, #rangeValue2").val("").attr("disabled", true); 
		    $("select#rangeOperator").val('0').attr("disabled", true);
	     }
		 checkRangeOperator();

	     // date conversion dropdown: This should be moved to Edit Cal_Rule Button Event
		if (aType == 3 || aType == 4) {
	    	$('#conversionFactor').attr("disabled", false);
		} else {
	    	$('#conversionFactor').attr("disabled",true);
		}
		
	     //prepopulation & precision only applies to textbox  
	     // alert("current question type=" + qType +  "; \tand answer type=" + aType );
	     if (qType == 1) { 
	     	if (aType == 2) { // numeric answer type
   		    	 $("#prepopulation").attr('checked',false);
				 $("#prepopulationH,  #prepopulationDiv").hide(); 
	    		 $("#decimalPrecisionDiv").show();
	    		 $("#conversionFactorH").show();
	    		 
		    } else { //prepopulation section is visible only for textbox && non-numeric answer type
	    		$('#decimalPrecision').val('-1').parent("#decimalPrecisionDiv").hide();
	     	    $("#conversionFactorH").hide();  
		    	$('#prepopulationH').show();
		    	$('#windowConv').html('');
		      }
	     } else {
	    	 $('#windowConv').html('');
	     }
	     
	     if (qType == <%=QuestionType.SELECT.getValue()%>) {
	    	 $('#prepopulationH').show();
	     }
	     // refreshing #prepopulationValue options when user changes answer type
	 	 activateDeactivatePrepopulationValueSelect();
	}
	
	function checkRangeOperator (){
		
		var rangeFlag = $('#rangeOperator').val();
		$('#rangeValue1, #rangeValue2').attr("disabled",true);
		
	    if (typeof(rangeFlag) == "undefined" || rangeFlag == "" || rangeFlag  == 0 || $('#rangeOperator').is(":disabled")) {
			$('#rangeValue1, #rangeValue2').val("");
	    } else if (rangeFlag == 4) {
			$('#rangeValue1, #rangeValue2').attr("disabled",false);
	    } else{
            $('#rangeValue1').attr("disabled", false);
            $('#rangeValue2').val("");
	    }
	}

	function showEditCalculation(){
		if(!($('#answerType').val()=='3' || $('#answerType').val()=='4')){
			$('#conversionFactor').val(<%=Integer.MIN_VALUE%>);
		}
		$('#editCalculationTable').show();
	}
	
	function cancelCalculationEdit(){
		var q=QuestionHandler.findQuestion(editQuestionDivId);
		if(q!=null){
			q.attributeObject.calculatedQuestion=false;
		}
		$('#window').html('');
		$('#editCalculationTable').hide();
		$('#conversionFactor').val(<%=Integer.MIN_VALUE%>);
	}
	
	function cancelConversionEdit() {
		var q=QuestionHandler.findQuestion(editQuestionDivId);
		if (q!=null) {
			q.attributeObject.hasUnitConversionFactor=false;
			q.attributeObject.unitConversionFactor="";
		}
		$('#windowConv').html('');
	}
		
	function showEquals() { 
	    if ($("#skipRuleOperatorType").val() == 1 || $("#skipRuleOperatorType").val() == 4) {
	        $("#skipRuleEquals").attr("disabled",false);
	    } else {
	        $("#skipRuleEquals").val("").attr("disabled", true);
	    }
	}
	
	function resetSkipRule() {
		if($('#skipRuleOperatorType').val()==<%=Integer.MIN_VALUE%>){
			$('#skipRuleType option:first').attr("selected", "selected");
			showSectionQuestion();
		}
	}
	
	function showSectionQuestion() {

		$('#divquestionsToSkip').html('');
		var sectionQuestionHTML='';
		var qQuestionId=0;
		var sectionCurrent = currentSectionID;
		if (questionInfoMode=='edit') {
			q = QuestionHandler.findQuestion(editQuestionDivId);
			qQuestionId=q.questionId;
			sectionCurrent=q.sectionId;
			if (sectionCurrent.indexOf("S_") != 0) {
				sectionCurrent = "S_" + sectionCurrent;
			}
		}
		
		//alert(qQuestionId);
		//alert("currentSecId:" + currentSectionID);
		for (var j=0; j<sectionsArray.length; j++) {
			if (!(sectionsArray[j].isRepeatable && sectionsArray[j].repeatedSectionParent==sectionCurrent)) {
				sectionQuestionHTML+='<span><b>'+sectionsArray[j].name+'</b><br></span>';
				
				for (var i = 0; i < questionsArray.length; i++) {
					var qSectionId=SectionHandler.convertToHTMLId(questionsArray[i].sectionId);
					var sectionId=SectionHandler.convertToHTMLId(sectionsArray[j].id);
					if(qSectionId==sectionId && editQuestionDivId !=questionsArray[i].newQuestionDivId && questionsArray[i].questionType!='10' && questionsArray[i].questionType!='12'){ // visual scale can't be skip question, text block can't be skip question
						var sectionQuestionId=sectionId+'_Q_'+questionsArray[i].questionId;
						var fatherQid=skipFatherQid(sectionQuestionId);

						sectionQuestionHTML+='&nbsp;&nbsp;<input type="checkbox" name="questionToSkip" id="'+sectionQuestionId+'"  value="'+sectionQuestionId+'">&nbsp;';
						sectionQuestionHTML+='<span>'+sectionQuestionId+':'+questionsArray[i].questionText+'<br></sapn>';
					}
				}
			}
		}
		$('#divquestionsToSkip').html(sectionQuestionHTML);
	}
	
	
	function skipFatherQid(sectionQuestionId) {
		var qId=0;
		for (var i = 0; i < questionsArray.length; i++) {
			var skipArray=questionsArray[i].attributeObject.questionsToSkip;
			for (var j=0; j<skipArray.length; j++) {
				if (sectionQuestionId==skipArray[j]) {
					qId=questionsArray[i].questionId;
					break ;
				}
			}
		}
		return 	qId;
	}
		
	function showTriggerAnswers(){
		var answersHTML = '<select  id="_et_answers" multiple width="30%">';
		$('#options option').each(function() {
			answersHTML+='<option value="' + $(this).text().split("|")[1] + '">' +
							$(this).text() +
						 "</option>";
		});
		
		var includeOther = $('#includeOther').is(':checked');	
		if (includeOther) {
			answersHTML+='<option value="Other, please specify">Other, please specify</option>';
		}
		answersHTML += '</select>';
		$('#triggerAnswerDiv').html('');
		$('#triggerAnswerDiv').html(answersHTML);
	}
	
	function checkRange() {
		var msg = '';
		if (document.getElementById('rangeOperator').disabled == true) {
			msg = "Range operator is disabled but is something other than none";
	    	return msg;
	    }
	   
	    var rangeOp = '';
	    var range1 = '';
	    var range2 = '';
	    if (document.getElementById ('rangeOperator')) {
	        rangeOp = document.getElementById('rangeOperator').value;
	    }
	    if (document.getElementById ('rangeValue1')) {
	        range1 = document.getElementById ('rangeValue1').value;
	    }
	    if (document.getElementById ('rangeValue2')) {
	        range2 = document.getElementById ('rangeValue2').value;
	    }

	    // is equal to, must have range value 1
	    if (rangeOp == 1) {
	        if (range1 == '') {
	            msg = "When choosing Range Operator 'Is Equal To', you must enter a value for Range Value 1";
	        } else {
	            if (! parseInt (range1) && (parseInt (range1) != 0)) {
	                msg = "When entering a value for Range Operator 1, the value must be numeric";
	            }
	        }
	    }

	    // less than, must have range value 1
	    if (rangeOp == 2) {
	        if (range1 == '') {
	            msg = "When choosing Range Operator 'Less Than', you must enter a value for Range Value 1";
	        } else {
	            if (! parseInt (range1) && (parseInt (range1) != 0))  {
	                msg = "When entering a value for Range Operator 1, the value must be numeric";
	            }
	        }
	    }

		// greater than, must have range value 1
	    if (rangeOp == 3) {
	        if (range1 == '') {
	            msg = "When choosing Range Operator 'Greater Than', you must enter a value for Range Value 1";
	        } else {
	            if (!parseInt (range1) && (parseInt (range1) != 0)) {
	                msg = "When entering a value for Range Operator 1, the value must be numeric";
	            }
	        }
	    }

	    if (rangeOp == 4) {
	        if (range1 == '' || range2 == '') {
	            msg = "When choosing Range Operator 'Between', you must enter a value for Range Value 1 and Range Value 2";
	        } else {
	            if (! parseFloat (range1) && range1 != 0) {
	                msg = "When entering a value for Range Operator 1, the value must be numeric <br>";
	            }
	            if (! parseFloat (range2) && range2 !=0) {
	                msg += "When entering a value for Range Operator 2, the value must be numeric";
	            }
	        }
	        // if no errors, check to see if range is acceptable
	        if (msg == '') {
	            if (parseFloat(range1) >= parseFloat(range2)) {
	                msg += "When choosing between, please ensure range value 1 is less than range value 2.";
	            }
	        }
	    }

	    if (msg != '') {
	        return msg;
	    }
	}	
	
	function getAvailableCalculateQuesions(){
		var currentSectionId = SectionHandler.convertToHTMLId($('div.activeSection').attr("id"));
		$('#availableQuestionsToCalculateDiv').html('');
		
		// get the edit Section Object
		var editSectionObj = SectionHandler.findSection(currentSectionId);
		var availbleQ='<select id="questions" multiple onclick="javascript:addQuestion()" style="width: 250px; height: 200px; overflow: scroll;border: solid 1px #B6B77B;">';<%-- <option value="'+<%=Integer.MIN_VALUE%>+' selected="true">Please select One</option> --%>

		for (var j=0; j<sectionsArray.length; j++) { // each section
			var sectionId=SectionHandler.convertToHTMLId(sectionsArray[j].id);
			var currentSectionObj=SectionHandler.findSection(sectionId);
			if (editSectionObj.isRepeatable && currentSectionObj.isRepeatable &&
				currentSectionObj.repeatedSectionParent != -1) { // Do not show the repeat section when edit section is repeatable
			} else {
				availbleQ+='<optgroup label="'+sectionsArray[j].id+':'+sectionsArray[j].name+'">';
				for (var i=0;i<questionsArray.length;i++){ // each question
					var qSectionId=SectionHandler.convertToHTMLId(questionsArray[i].sectionId);					
					var ansType=questionsArray[i].attributeObject.answerType;
					
					// IBIS-856 checkbox,select,multi-select,radio
					var scoreAvailable="false";
					if (questionsArray[i].questionOptions != null && questionsArray[i].questionOptions != 'undefined' && 
						questionsArray[i].questionOptions!='') {														
						OptionsCodes=questionsArray[i].questionOptions.split(parsingSymbol);

						for (var m=0;m<OptionsCodes.length;m++) {
							if (OptionsCodes[m]!=null && typeof(OptionsCodes[m])!='undefined' && OptionsCodes[m]!='') {									
								var displays = OptionsCodes[m].split('|');									
								if (displays.length>=2 && displays[1]!=null && displays[1].trim()!='') {										
									scoreAvailable="true";
									break;
								}	
							}
						}
					}
					
					//IBIS-857: Available Questions
					if (sectionId==qSectionId && editQuestionDivId!=questionsArray[i].newQuestionDivId &&
						((questionsArray[i].questionType==<%=QuestionType.TEXTBOX.getValue()%> && $('#answerType').val()==ansType ) || 
						 (questionsArray[i].questionType==<%=QuestionType.SELECT.getValue()%>  && scoreAvailable=='true') ||
						 (questionsArray[i].questionType==<%=QuestionType.RADIO.getValue()%>   && scoreAvailable=='true')) ) {
						
						availbleQ+='<option value="'+qSectionId+":"+questionsArray[i].questionId+'">&nbsp;&nbsp;'+questionsArray[i].questionId+': '+questionsArray[i].questionName+'</option>';
					}
				}
				availbleQ+='</optgroup>';
			}
		}
		
		availbleQ += '</select>';
		$('#availableQuestionsToCalculateDiv').html(availbleQ);
	}

	
	function testFormular() {
		var eq = trim(new String($("#window").html()));
		var eqStr = trim(new String($("#window").html()));
		var eqStr2 = trim(new String($("#window").html()));
		var findQuestionsAll = /\[S_[-]?\d+_Q_([0-9]+)\]/g;
		var count=0;

	    var operators = /[+-\/%*]/;
	    if (eq.length > 0) {
	        chr = eq.charAt(0);
	        if (teststr = operators.exec(chr)) {
	        	errorString+="<DIV align='left'><font color='red'>The equation cannot start with an operator.</font></DIV><br>";
	        	stopFalg=true;
	        }

		    var divbyzero = /\s\/\s0+\s+/g;
		    if (teststr = divbyzero.exec(eq+" ")) {
		            errorString+="<DIV align='left'><font color='red'>The equation cannot include division by 0.</font></DIV><br>";
		            stopFalg=true;
		    }
	
		    var parenStr = testParen(eqStr);
			if (parenStr != null && parenStr.length > 0) {
				errorString+="<DIV align='left'><font color='red'>"+ parenStr + "</font></DIV><br>";
				stopFalg=true;
			}
	
		    if (isDateQuestion()) {
		    	ers = "";
		        var findQuestionsWithMinus = /\[S_[-]?\d+_Q_[0-9]+\]\s*-\s*\[S_[-]?\d+_Q_[0-9]+\]/g;
	
		        eqStr = eqStr.replace(findQuestionsWithMinus, "5");
				var dateTimeWrong=false;
		        while (qs2 = findQuestionsAll.exec(eqStr)) {
		            stopFalg=true;
		            dateTimeWrong=true;
		        }
		        if (dateTimeWrong){
		        	ers="<DIV align='left'><font color='red'>"+ ers + "Date Questions can only be used in subtraction from each other (ex S_1_Q_1 - S_2_Q_2) all other uses of date or date-time functions are illegal.</font></DIV><br>";
		        }
	
		        if (ers != null && ers.length > 0) {
		            errorString+="<DIV align='left'><font color='red'>"+ ers + "</font></DIV><br>";
		            stopFalg=true;
		        }
		    } else {
		    	eqStr = eqStr.replace(findQuestionsAll, "2");
		    }
	
		    eqStr = eqStr.replace(/sqrt\(\s*([\d\.]+)\s*\)/g, "Math.sqrt($1)");
		    eqStr = eqStr.replace(/%/g, "*\(1/100\)*");

		    try {
		    	evalResult = eval(eqStr);
		    } catch(exception) {
		    	errorString+="<DIV align='left'><font color='red'>The entered calculation fails computation. Please correct the error.</font></DIV><br>";
		    	stopFalg=true;
		    }
	    }
	}

	
	function testFormularC() {
		var eqC = trim(new String($("#windowConv").html()));
		var eqStrC = trim(new String($("#windowConv").html()));
		var eqStrC2 = trim(new String($("#windowConv").html()));
		
		var count = 0;
	    var operators = /[+-\/%*]/;
	    
	    if (eqC.length > 0) {
	        chr = eqC.charAt(0);
	        if (teststr = operators.exec(chr)) {
	        	errorString+="<DIV align='left'><font color='red'>The conversion equation cannot start with an operator.</font></DIV><br>";
	        	stopFalg=true;
	        }

		    var divbyzero = /\s\/\s0+\s+/g;
		    if (teststr = divbyzero.exec(eqC+" ")) {
		            errorString+="<DIV align='left'><font color='red'>The conversion equation cannot include division by 0.</font></DIV><br>";
		            stopFalg=true;
		    }
	
		    var parenStr = testParen(eqStrC);
			if (parenStr != null && parenStr.length > 0) {
				errorString+="<DIV align='left'><font color='red'>"+ parenStr + "</font></DIV><br>";
				stopFalg=true;
			}
	
			//test eqn by substituting 2 for [this]
		    eqStrC = eqStrC.replace("[this]", "2");
		    eqStrC = eqStrC.replace(/sqrt\(\s*([\d\.]+)\s*\)/g, "Math.sqrt($1)");
		    eqStrC = eqStrC.replace(/%/g, "*\(1/100\)*");
		    
		    //test eqn again by substituting 4 for [this]
		    eqStrC2 = eqStrC2.replace("[this]", "4");
		    eqStrC2 = eqStrC2.replace(/sqrt\(\s*([\d\.]+)\s*\)/g, "Math.sqrt($1)");
		    eqStrC2 = eqStrC2.replace(/%/g, "*\(1/100\)*");
		    
		    try {
		    	evalResult = eval(eqStrC);
		    	evalResult = eval(eqStrC2);
		    } catch (exception) {
		    	errorString+="<DIV align='left'><font color='red'>The entered conversion calculation fails computation. Please correct the error.</font></DIV><br>";
		    	stopFalg=true;
		    }
	    }
	}
	
		
	function unlockQuestion(LOCKQNAME){
		$('#questionType, #questionText, #descriptionUp,#descriptionDown,#questionName').attr('disabled',false);
	}
	
	function deleteTrigger(){
		$('#_et_toAddr,#_et_ccAddr,#_et_body,#_et_subject').val('');
		$('#_et_answers option').each(function(){
			$(this).attr('selected',false);
		});
		
		if (formInfoMode=='edit' && questionInfoMode=='edit'){
			var QuestioIndex=QuestionHandler.findQuestionIndex(editQuestionDivId);

			questionsArray[QuestioIndex].attributeObject.deleteTrigger=true;
			questionsArray[QuestioIndex].attributeObject.toEmailAddress='';
			questionsArray[QuestioIndex].attributeObject.ccEmailAddress='';
			questionsArray[QuestioIndex].attributeObject.body='';
		}
	}

	
	function showDataElementTable(){

		var activeSId = $('#activeSectionId').val();
		var s = SectionHandler.findSection(activeSId);
		var sectionIsRepeatable = s.isRepeatable;
		var repeatableGroupName = s.repeatableGroupName;
		
		// attach the data element table
		var colunm='';
		for (var i=0;i<DSObj.dataElementsArr.length;i++){
			var DEObj=DSObj.dataElementsArr[i];
			var deName = DEObj.dataElementName;
			var index=deName.indexOf("."); 
			var rGroup = deName.substring(0,index);
			var deIsRepeatable = DEObj.isGroupRepeatable;

			var showRow = false;
			if (sectionIsRepeatable && repeatableGroupName=="None") {
			} else if (sectionIsRepeatable && repeatableGroupName!="None") {
				if (rGroup == repeatableGroupName) {
					showRow = true;
				}
			} else if(!sectionIsRepeatable) {
				if(!deIsRepeatable) {
					showRow = true;
				}
			}

			//if data element is already associated, then dont display it unless its the activeEditQ
			if (showRow == true) {
				for (var k=0;k<questionsArray.length;k++){
					var q = questionsArray[k];
					var sId = q.sectionId;
					
					var s2 = SectionHandler.findSection(sId);
					var s2IsRepeatable = s2.isRepeatable;
					var s2Parent = s2.repeatedSectionParent;

					var qId = q.questionId;
					var attrObj = q.attributeObject;
					var assocDE = attrObj.dataElementName;
					
					if (!s2IsRepeatable || (s2IsRepeatable && s2Parent == "-1")) {
						if (assocDE == deName) {
							if (editQuestionId == qId && activeSId == sId) {	
								showRow = true;
							} else {
								showRow = false;
							}
							break;
						}
					}
				}
			}
			
			if (showRow) {
				var DEFullName = DEObj.dataElementName;
				var index = DEFullName.indexOf(".");
				var group = DEFullName.substring(0,index);
				var deName = DEFullName.substring(index+1);
				var select = '';
				select = '<input type="radio" name="dataElementRadio" calss="deUnique" value="'+DEObj.dataElementType+'" id="'+DEObj.dataElementName+'" onclick="dataElementSelect();">';
				colunm += '<tr>'+
							'<td>' + select + '</td>'+
							'<td align="left">' + deName + '</td>'+
							'<td align="left">' + group + '</td>'+
							'<td align="left">' + DEObj.dataElementType + '</td>'+
							'<td align="left">' + DEObj.restrictionName + '</td>'+
							'<td align="left">';
							
				if (typeof(DEObj.description) != 'undefined') {
					colunm += DEObj.description;
				}
				colunm += '</td>'+
					'<td align="left">' + DEObj.requiredType + '</td>'+
					'<td align="left">';
					
				if (typeof(DEObj.suggestedQuestion) != 'undefined') {						
					colunm+=  DEObj.suggestedQuestion;
				}
	            colunm += '</td>' + '</tr>';
			}
		}

		var DEtable = '<table id="dataElementTable">'+
						'<thead>'+
							'<tr class="tableRowHeader" role="row">'+
								'<th class="tableCellHeader">' + '' + '</th>'+
								'<th class="tableCellHeader">'+
									'<s:text name="questionlibrary.DataElement" />'+
								'</th>'+
								'<th class="tableCellHeader">'+
									'<s:text name="questionlibrary.RepeatableGroup" />'+
								'</th>'+
								'<th class="tableCellHeader">'+
									'<s:text name="questionlibrary.dataType" />'+
								'</th>'+
								'<th class="tableCellHeader">'+
									'<s:text name="questionlibrary.restrictionType" />'+
								'</th>'+
								'<th class="tableCellHeader">'+
									'<s:text name="questionlibrary.shortDescription" />'+	
								'</th>'+
								'<th class="tableCellHeader">'+
									'<s:text name="questionlibrary.requiredType" />'+
								'</th>'+
								'<th class="tableCellHeader">'+
									'<s:text name="questionlibrary.suggestedQuestion" />'+
								'</th>'+
							 '</tr>'+
						 '</thead>'+
						 '<tbody>'+ colunm+ '</tbody>'+
					  '</table>';
		
		$('#dataElementTableDiv').html(DEtable);
		IbisDataTables.fullBuild();
	}
	
	
	function populateDataElement(questionType,dataElementName,msgDialogId){
		$.ibisMessaging("close", {id: msgDialogId});
		
		//restrictions: 0=free entry 1=single, 2=multiple
		if (dataElementName=='null') {
			dataElementName=$('input:radio:checked[name="dataElementRadio"]:checked').attr('id');
		}
		var DEobj = dataStructureHandler.findDataElement(dataElementName);

		// replace the question options
		if (DEobj.restrictionId=='1'||DEobj.restrictionId=='2') { // Data element is a single or multiple select
			var optionHtml='';
			for (var i=0;i<DEobj.valueRangeList.length;i++) {
				var optionValue=DEobj.valueRangeList[i].valueRange+'||';
				optionHtml += '<option value="' + optionValue + '">' + optionValue + "</option>";	
			}
			$('#options').html(optionHtml);
		}
		
		// set the Visual  Scale Range Minimum/Maximum 
		if (DEobj.restrictionId=='0' && DEobj.dataElementType=='Numeric Values' && 
			questionType==<%=QuestionType.VISUAL_SCALE.getValue()%>) { // data element is a numeric one, not for visual
			if (typeof(DEobj.min)!='undefined' && typeof(DEobj.max)!='undefined') {
				$('#rangeStart').val(DEobj.min);
				$('#rangeEnd').val(DEobj.max);
			}
		}
		
		// make the question is required
		if (DEobj.requiredType=='REQUIRED') {
			$('#required').val("true");
		}
		
		// set the Maximum Characters
		if (DEobj.restrictionId=='0' && DEobj.dataElementType=='Alphanumeric') { // data element is a String(number or alpha)
			$('#maxCharacters').val(DEobj.size);
		}
		
		//set the Range Operator, Range Value 1 & 2
		if (DEobj.dataElementType=='Numeric Values' && questionType!=<%=QuestionType.VISUAL_SCALE.getValue()%>) { 
			// data element is a numeric one, not for visual
			$('#answerType').val(2); // Numeric
			$("#decimalPrecisionDiv").show(); //show decimal precision div
			$('#minCharacters').attr('disabled',true);
			$('#maxCharacters').attr('disabled',true);
			
			if (typeof(DEobj.min)!='undefined' && typeof(DEobj.max)!='undefined') {
				// enable
				$('#rangeOperator').attr('disabled',false);
				$('#rangeValue1').attr('disabled',false);
				$('#rangeValue2').attr('disabled',false);
				
				$('#rangeOperator').val(4);
				$('#rangeValue1').val(DEobj.min);
				$('#rangeValue2').val(DEobj.max);
			}
		}
				
		// set the answer type
		if (DEobj.dataElementType=='Date or Date & Time') { // data element is a date or dateTime one
			$('#answerType').val(1); // make default is date
		}
	}
	
	function checkDEandQuestionType(questionType,dataElementName){
		// find data element by user selects
		var DEobj=dataStructureHandler.findDataElement(dataElementName);
		var dataElementType=DEobj.dataElementType;
		
		//restrictions: 0=free entry 1=single, 2=multiple
		if (dataElementType == 'Alphanumeric') {
			if (questionType == <%=QuestionType.VISUAL_SCALE.getValue()%> || questionType==<%=QuestionType.File.getValue()%>) {
				return false; // visual scale
			}
			if (DEobj.restrictionId == '0') {
				if (questionType == <%=QuestionType.IMAGE_MAP.getValue()%> || questionType == <%=QuestionType.File.getValue()%> ||
					questionType == <%=QuestionType.CHECKBOX.getValue()%> || questionType==<%=QuestionType.MULTI_SELECT.getValue()%> ||
					questionType==<%=QuestionType.RADIO.getValue()%> || questionType==<%=QuestionType.SELECT.getValue()%>) {
					return false;
				}
			}
			if(DEobj.restrictionId == '1') {
				if (questionType == <%=QuestionType.IMAGE_MAP.getValue()%> || questionType == <%=QuestionType.File.getValue()%> ||
					questionType == <%=QuestionType.CHECKBOX.getValue()%> || questionType == <%=QuestionType.MULTI_SELECT.getValue()%> ||
					questionType == <%=QuestionType.TEXTBOX.getValue()%> || questionType == <%=QuestionType.TEXTAREA.getValue()%>) {
					return false;
				}
			}
			if (DEobj.restrictionId=='2') {
				if (questionType == <%=QuestionType.IMAGE_MAP.getValue()%> || questionType == <%=QuestionType.File.getValue()%> ||
					questionType=='4' || questionType=='3' || questionType=='1' || questionType=='2') {
					return false;
				}
			}
			return true;
		}
		
		if (dataElementType == 'Date or Date & Time') {
			if (questionType == <%=QuestionType.IMAGE_MAP.getValue()%>  || questionType==<%=QuestionType.File.getValue()%> ||
				questionType==<%=QuestionType.VISUAL_SCALE.getValue()%> || questionType==<%=QuestionType.CHECKBOX.getValue()%> ||
				questionType==<%=QuestionType.MULTI_SELECT.getValue()%> || questionType==<%=QuestionType.RADIO.getValue()%> ||
				questionType==<%=QuestionType.SELECT.getValue()%>) {
				return false; //NOT text box or text area 
			}
			return true;
			
		} else if (dataElementType=='GUID'){
			if (questionType != <%=QuestionType.TEXTBOX.getValue()%>) {
				return false;
			} //NOT text box
			return true;
			
		} else if (dataElementType=='Numeric Values'){
			if (questionType == <%=QuestionType.File.getValue()%>) {
				return false;
			}
			if (DEobj.restrictionId=='0'){
				if (questionType==<%=QuestionType.IMAGE_MAP.getValue()%> || questionType==<%=QuestionType.CHECKBOX.getValue()%> || 
					questionType==<%=QuestionType.MULTI_SELECT.getValue()%> || questionType==<%=QuestionType.RADIO.getValue()%> || 
					questionType==<%=QuestionType.SELECT.getValue()%>) {
					return false;
				}
			}
			if(DEobj.restrictionId=='1'){
				if (questionType==<%=QuestionType.VISUAL_SCALE.getValue()%> || questionType==<%=QuestionType.IMAGE_MAP.getValue()%> || 
					questionType==<%=QuestionType.CHECKBOX.getValue()%> || questionType==<%=QuestionType.MULTI_SELECT.getValue()%> ||
					questionType==<%=QuestionType.TEXTBOX.getValue()%> || questionType==<%=QuestionType.TEXTAREA.getValue()%>) {
					return false;
				}
			}
			if (DEobj.restrictionId=='2'){
				if (questionType==<%=QuestionType.VISUAL_SCALE.getValue()%> || 
					questionType==<%=QuestionType.IMAGE_MAP.getValue()%> || questionType==<%=QuestionType.RADIO.getValue()%> || 
					questionType==<%=QuestionType.SELECT.getValue()%> || questionType==<%=QuestionType.TEXTBOX.getValue()%> || 
					questionType==<%=QuestionType.TEXTAREA.getValue()%>) {
					return false;
				}
			}
			return true;
			
		} else if (dataElementType == 'File'){
			if (questionType == <%=QuestionType.File.getValue()%>) {
				return true;
			} else {
				return false;
			}
			
		}
	}
	
	function linkHilight(step) {
		$('#step1DE, #step2Q, #step3QA').css('background-color','');
		if (step==1){
			$('#step1DE').css('background-color','#C8FE2E');
		} else if(step==2) {
			$('#step2Q').css('background-color','#C8FE2E');
		} else {
			$('#step3QA').css('background-color','#C8FE2E');
		}
		
	}
	
	function controlWorkFlow(step) {
		if (step==1) {
			$('#step1').show();
			$('#step2,#step3').hide();
			linkHilight(step);
			
		} else if (step==2) {
			if($('#linkDEcheckBox').is(':checked')) { // user want data element
				if (!$('input:radio:checked[name="dataElementRadio"]:checked').val()) { // but they doesn't select a data element
					alert('Please select a Data Element to continue');
				} else{
					$('#step1, #step3').hide();
					$('#step2').show();
					workFlow=2; // user has enter step 2
					linkHilight(step);
				}
			} else {
				$('#step1, #step3').hide();
				$('#step2').show();
				workFlow=2; // user has enter step 2
				linkHilight(step);
			}
		} else { // step3
			if ($('#linkDEcheckBox').is(':checked')) {
				if (!$('input:radio:checked[name="dataElementRadio"]:checked').val()) {
					alert('Please select a DataElement to continue');
				} else {
					$('#step1,#step2').hide();
					$('#step3').show();
					linkHilight(step);
					if ($('#cQ').is(':checked')) {
						$('#searchAddBtuStep3').hide();
						if(questionInfoMode=='add'){ // if the mode is edit, don't show
							$('#createAddBtuStep3').show();
						}
					} else {
						$('#searchAddBtuStep3').show();
						$('#createAddBtuStep3').hide();
					}	
				}
			} else {
				$('#step1,#step2').hide();
				$('#step3').show();
				linkHilight(step);
				if ($('#cQ').is(':checked')) {
					$('#searchAddBtuStep3').hide();
					if (questionInfoMode=='add') { // if the mode is edit, don't show
						$('#createAddBtuStep3').show();
					}
				} else {
					$('#searchAddBtuStep3').show();
					$('#createAddBtuStep3').hide();
				}	
			}
		}
	}
	
	function linkDataElement(){
		if ($('#linkDEcheckBox').is(':checked')) { // user want the data element
			if (questionInfoMode == 'add') { // if the mode is edit, every workflow is available
				resetWorkFlow(1);
			}
			$('input[name="dataElementRadio"]').attr('disabled',false).attr('checked',false);

			if ($("#dataElementH").next("div").css('display')=='none') {
				$("#dataElementH").click();
			}
		} else { // user DON'T want the data element
			if (questionInfoMode!='edit') { // if the mode is edit, every workflow is available
				$('#step2Q').removeClass();
				$('#step2Q').addClass('focus-link');
				$('#step2Q').attr('onclick','controlWorkFlow(2)');
			}
			$('input[name="dataElementRadio"]').attr('disabled',true).attr('checked',false);
			editDataElementName=''; // clear the data element
		}
	}
	
	function dataElementSelect() {
		$('#step2Q').removeClass().addClass('focus-link').attr('onclick','controlWorkFlow(2)');
		editDataElementName=$('input[name="dataElementRadio"]:checked').attr('id');   // set the data element id user selected
		var selectQtype=$('input[name="selectQuestionId"]:checked').val();
		
		if (questionInfoMode=='edit') {
			checkMatchInCreateEdit(editQuestionType);
		} else {
			if (workFlow==2) { // user has entered into step
				if ($('#sQ').is(':checked')) {
					var questionType=$('input[name="selectQuestionId"]:checked').val();
					if (typeof(questionType) != 'undefined') {// user has selected a question
						checkMatchInSearch();
					}
				} else if ($('#cQ').is(':checked')) {// user has selected a question type
					var questionType=$('#questionType').val();
					if (questionType!=<%=Integer.MIN_VALUE%>) {
						checkMatchInCreateEdit(questionType);
					}
				}
			}
		}
	}
	
	function qSelect() {
		$('#step3QA').removeClass().addClass('focus-link').attr('onclick','controlWorkFlow(3)');
		//we did showQuestionInfoInSearch in  doSearchSigleQuestionsAjax
		doSearchSigleQuestionsAjax($('input:radio:checked[name="selectQuestionId"]:checked').attr('id'));		
		if ($('#linkDEcheckBox').is(':checked')) { // user want the data element
			checkMatchInSearch();
		}
	}
	
	function checkMatchInSearch() {
		var questionType=$('input[name="selectQuestionId"]:checked').val();
		var dataElementName=$('input:radio:checked[name="dataElementRadio"]:checked').attr('id');
		if (checkDEandQuestionType(questionType,dataElementName)) {
			var html = "Some of the question values will be overwritten based on the linked data element!\nDo you wish to overwrite?";
			var dlgId = $.ibisMessaging(
						"dialog", 
						"info",
						html,	
						{
							title: "Overwrite Values",
							buttons: [{
								text: "Yes", 
								click: function() { populateDataElement(questionType,dataElementName,dlgId); }
							},
							{
								text: "No",
								click: function() { closeDialog(dlgId); }
							}],
							modal : true
						}
				);
		}
	}
	
	function checkMatchInCreateEdit(questionType) {
		if ($('#linkDEcheckBox').is(':checked')) { // user want the data element
			var dataElementName=$('input:radio:checked[name="dataElementRadio"]:checked').attr('id');
			if (questionType!=<%=Integer.MIN_VALUE%>) {
				if (checkDEandQuestionType(questionType,dataElementName)) {
					var html = "Some of the question values will be overwritten based on the linked data element!\nDo you wish to overwrite?"
					var dlgId = $.ibisMessaging(
									"dialog", 
									"info", 
									html, 
									{
										title: "Overwrite Values",
										buttons: [{
											text: "Yes", 
											click: function(){populateDataElement(questionType,dataElementName,dlgId);}
										},
										{
											text: "No",
											click: function(){closeDialog(dlgId);}
										}],
										modal : true
									}
							);
				}
			}
		}
	}
	
	
	function closeDialog(msgDlgId) {
		$.ibisMessaging("close", {id: msgDlgId});
	}
	
	function doSearchSigleQuestionsAjax(idAndVer){
		var id=idAndVer.split("\_")[0];
		var version=idAndVer.split("\_")[1];
		var J;
		$.ajax({
			type:"post",
			url: "<s:property value="#webRoot"/>/form/searchQuestions.action?action=multiAddQuestionsAjax&id="+id+"&version="+version,
			beforeSend: function(){},
			success: function(response){
				//alert("Response from doSearchSigleQuestionAjax function:\n" + response);
				if (response.indexOf("{") != -1) {
					//create AttributeObject
					var qJSON=JSON.parse(response);
					var attribute=new AttributeObject(); // the attribute is default
					attribute.qType=qJSON.questionType;
					attribute=setAttributeObject(attribute);
					var sectionId=$('div.activeSection').attr("id");
					var newQuestionDivId=$('div.activeSection').attr('id')+"_"+qJSON.questionId+"_"+qJSON.questionVersionNumber;	
					var question = new QuestionObj( qJSON.questionId,
													qJSON.questionVersionString,
													qJSON.questionVersionNumber,
													qJSON.questionName,
													qJSON.questionText,
													qJSON.descriptionUp,
													qJSON.descriptionDown,
													qJSON.questionType,
													qJSON.options,
													qJSON.imageOption,
													qJSON.imageFileName,
													qJSON.visualScaleInfo,
													newQuestionDivId,
													sectionId,
													attribute,
													qJSON.graphicNames,
													qJSON.defaultValue,
													qJSON.unansweredValue,
													qJSON.associatedGroupIds,
													qJSON.includeOther,
													qJSON.attachedFormIds);
					
					// it is like EDIT mode, very semilar...
					editQuestionId = question.questionId;
					editQuestionVersionNumber = question.questionVersionNumber;
					editQuestionDivId = newQuestionDivId;
					editAttachedFormIds = question.attachedFormIds;
					checkQuestionType(question.questionType);
					// keep the search table chebox, the clearQuestionInf() in checkQuestionType() will clean the checkbox
					$("#"+idAndVer).attr('checked', true);
					// populate question Inf	
					QuestionHandler.populateQuestion(question);
					// interface switch
					Interface.showQuestionInfoInSearch();
				}
			},
			error:function(e) {
				alert("Single attached fail "+e);
			}
		});
	}
	
	function enableStep3() {
		if ($('#questionName').val().length!=0 &&  $('#questionText').val().length!=0) {
			$('#step3QA').removeClass().addClass('focus-link').attr('onclick','controlWorkFlow(3)');
		} else {
			$('#step3QA').removeClass().addClass('disable-link').attr('onclick','');
		}
	}
	
	function activateDeactivatePrepopulationValueSelect() {
		if ($('#prepopulation').is(':checked')) {
			$('#prepopulationValue, #prepopulationValue option').attr('disabled',false);
			if ($("#questionType").val() ==  <%=QuestionType.SELECT.getValue()%>) { // SELECT question type
		    	$("#prepopulationValue").find("option").filter(function() {
			    	return $(this).val() =='visitDate' || $(this).val() == 'guid';
				}).attr("disabled", true);
			}
			else if ($("#questionType").val() ==  <%=QuestionType.TEXTBOX.getValue()%>) {
				if ($("#answerType").val() == 1) { //string type
			    	$("#prepopulationValue option[value ='visitDate']").attr("disabled", true);
				} else if($("#answerType").val() == 3 || $("#answerType").val() == 4) { // date or date time
			    	$("#prepopulationValue option[value !='visitDate']").attr("disabled", true);
				}
			}
		} else {// checkbox is not checked
			$('#prepopulationValue').val('none').attr('disabled',true);
		}
	}
	
	function selectDeselectAllSkip() {
		if ($('#selectAllSkip').is(':checked')) {
			$("#divquestionsToSkip input[type = 'checkbox']").attr("checked", true);
		} else {
			$("#divquestionsToSkip input[type = 'checkbox']").attr("checked", false);
		}
	}
	
</script>

<br>
<!-- =============================================================================================== -->
<div id="workFlow" align="left">
	<a href="javascript:" id="step1DE" class="focus-link" onclick="controlWorkFlow(1);">1.<s:text name="question.dataElement.title"/></a>
	=&gt;
	<a href="javascript:" id="step2Q" class="disable-link" onclick="">2.<s:text name="question.question.title"/></a>
	=&gt;
	<a href="javascript:" id="step3QA" class="disable-link" onclick="">3.<s:text name="question.questionDetail.title"/></a>
</div>
<br>

<div id="questionValidationErrorsDiv" align='left' style="display:none" class="errorContainer"></div>

<!-- =============================================================================================== -->
<div id="step1">
	<div align="left">
		<H3><input id="linkDEcheckBox" type="checkbox" checked="true" onchange="linkDataElement()"><s:text name="questionlibrary.linkDataElement"/></H3>
	</div>
	<div align="left">
		<s:text name="question.dataElement.instruction"/>
	</div>
	<h3 align="left" class="toggleable" id="dataElementH"> 
		<s:text name="question.dataElement.title"/>
	</h3>
	<div id="dataElementTableDiv" class="dataTableContainer"></div>
	
	<div align="right">
		<input type="button" id="questionEditButtonInSearch" value="<s:text name='button.Save'/>" alt="Save Change" 
			onclick="unlockQuestion(1); selectAllOptions(document.getElementById('options')); addEditQuestion_fancyConfirm();" title="Click to save changes"/>
	</div>
</div>
<!-- =============================================================================================== -->
<div id="step2">
	<table id='qSwitchTable'>
		<tr align='center'>
			<td>
				<input type='radio' id='sQ' name='switch' value='s' checked='checked' onclick='checkSwitch()'> 
				<label for="searchQuestion"><s:text name="questionlibrary.question.wizard.searchLabel"/></label> 
				&nbsp;&nbsp;&nbsp;&nbsp;
				<input type='radio' id='cQ' name='switch' value='c' onclick='checkSwitch()'> 
				<label for="createQuestion"><s:text name="questionlibrary.question.wizard.createLabel"/></label>
			</td>
		</tr>
	</table>

	<div id="searchQuestion">
		<h3 align='left'>
	   		<s:text name="questionlibrary.subtitle.display"/>
	    </h3>
		
		<h3 align="left" class="toggleable collapsed">
			<s:text name="questionlibrary.search.title.display"/>
		</h3>
		
		<div id='advencedSearchQuestion' style="display: none;">
			<s:form theme="simple" action="searchQuestions" id="searchQuestionForm" onsubmit="return false">
				<s:hidden name="questionSearchForm.clicked" value="submit"/>
				<s:hidden name="questionSearchForm.duplicateQuestions" id="duplicateQuestions"/>   
				  
				<div id="search_question" class="searchContainer" style="width:759px">
					<div class="formrow_2">
			   			<label for="questioIdSearch"><s:text name="questionlibrary.search.questionid.display"/></label>
			   			<s:textfield name="questionSearchForm.questionId" id="questionId"/>
			    	</div>
			    	<div class="formrow_2">
			    		<label for="questionTypeSearch"><s:text name="questionlibrary.search.questiontype.display"/></label>
						<s:select name="questionSearchForm.questionType" id="qType" tabindex="5" list="#session.types" listKey="value" listValue="dispValue" 
								headerKey="%{@java.lang.Integer@MIN_VALUE}" headerValue="All Types" />
			    	</div>
			    	<div class="formrow_2">
			    		<label for="questionNameSearch"><s:text name="questionlibrary.search.questionname.display"/></label>
			    		<s:textfield name="questionSearchForm.questionName" />
			    		<br>
			    		<label for="none">&nbsp;</label>
			    		<s:radio name="questionSearchForm.nameSearchModifier" list="#{'Contains':''}" /><%=QuestionResultControl.SEARCH_MODIFIER_CONTAINS%>
		                <s:radio name="questionSearchForm.nameSearchModifier" list="#{'Begins With':''}" /><%=QuestionResultControl.SEARCH_MODIFIER_BEGINS_WITH%>
		                <s:radio name="questionSearchForm.nameSearchModifier" list="#{'Not':''}" /><%=QuestionResultControl.SEARCH_MODIFIER_NOT%>
			    	</div>
			    	<div class="formrow_2">
			    		<label for="questionTextSearch"><s:text name="questionlibrary.search.questiontext.display"/></label>
			    		<s:textarea name="questionSearchForm.questionText" id="serchQuestionText"/>
			    		<br>
			    		<label for="none">&nbsp;</label>
			    		<s:radio name="questionSearchForm.textSearchModifier" list="#{'Contains':''}" /> <%=QuestionResultControl.SEARCH_MODIFIER_CONTAINS%>
		                <s:radio name="questionSearchForm.textSearchModifier" list="#{'Begins With':''}" /> <%=QuestionResultControl.SEARCH_MODIFIER_BEGINS_WITH%>
		                <s:radio name="questionSearchForm.textSearchModifier" list="#{'Not':''}" /> <%=QuestionResultControl.SEARCH_MODIFIER_NOT%>
			    	</div>
			    	<!-- ---following field is hiden function, Groups is good, medical coding is broken--- -->
			    	<div class="formrow_2" style="display:none">
			    		<label for="medicalCodingSearch"><s:text name="app.label.search.questionstatus"/></label>
			    		<s:select name="questionSearchForm.medicalCodingStatus" tabindex="6" id="medicalCode" list="#session.medicalCodingStatusOptions" />
			    	</div>
			    	<div class="formrow_2" style="display:none">
			    		<label for="questionGroupSearch"><s:text name="questionlibrary.search.questiongroup.display"/></label>
			    		<s:select name="questionSearchForm.questionGroup" tabindex="4" id="qGroup" list="#session.groups" listValue="name" listKey="id" />
			    	</div>
			    	
			    	<!-- --------------- -->
			    	<div class="formrow_1">
			    		<input type="button" title="Click to clear fields" value="<s:text name='button.Reset'/>" alt="Reset" 
			    			onclick="javascript:document.getElementById('searchQuestionForm').reset(); doSearchQuestionAjaxPost(); resetSearchQuestionInf();">
			    		<input id="searchQuestionSubmit" type="submit" value="<s:text name='button.Search'/>"  
			    			onclick="doSearchQuestionAjaxPost();" title="Click to search" />
			    	</div> 
				</div>
			</s:form>
		</div>
	   
		<div class="dataTableContainer" id="myQuestions"></div>
	</div><!--  search question end -->

	<div id="addEditQuestion" style="display: none;">
		<s:form theme="simple" action="addEditQuestion" id="addEditQuestionForm" enctype="multipart/form-data" onsubmit="return false">
			<s:hidden name="id" />
			<s:hidden name="questionForm.calculations" id="calculations"/>
			
			<input type="hidden" name=finish id="finish" value="false" />
			<input type=hidden name="forceNewVersion" id="forceNewVersion" value="false">

			<h3 align="left">
				<s:text name="questionlibrary.question.wizard.subtitle.question.inf" />
			</h3>
			<div class="leftAlign">
				<label class="requiredInput"></label> 
				<i><s:text name="form.forms.formInfo.enterFormInfo.requiredSymbol"/></i>
			</div>
		
			<table id="questioInfo" width="100%">
				<tr>
					<td valign="top" width="45%">
						<%--left table --%>
						<table id="questionInfoLeft" width="100%">
							<tr>
								<td class="formItemLabel"  width="30%" nowrap>
								 	<label for="formName" class="requiredInput"><b><s:text name="questionlibrary.search.questiontype.display"/></b></label>
								 	&nbsp;
								</td>
								<td align="left" valign="middle" class="formItemLabel">
 		                            <s:select name="questionForm.type" id="questionType" tabindex='3' cssStyle="width:98%" list="#session.questionTypes" 
 		                            	listKey="value" listValue="dispValue" headerKey="%{@java.lang.Integer@MIN_VALUE}" 
 		                            	headerValue="%{getText('questionlibrary.search.selectQuestionType')}" />
		                        </td>
							</tr>
							<tr>
								<td>
									<label for="formName" class="requiredInput"><b><s:text name="questionlibrary.search.questionname.display"/></b></label>
								</td>
								<td align="left" valign="middle" class="formItemLabel">
									<s:textfield name="questionForm.questionName" size="30" maxlength="40" tabindex="1" cssStyle="width:98%" 
										id="questionName" onkeyup="upCase(event,this);enableStep3();" />
								</td>
							</tr>
							<tr>
								<td valign="top"> 
									<label for="formName"><b><s:text name="questionlibrary.search.textAboveQuestion"/></b></label>&nbsp;
								</td>
								<td><s:textarea name="questionForm.descriptionUp" cols="20" rows="3" tabindex="2" id="descriptionUp"/></td>
							</tr>								
							<tr>
								<td valign="top"> 
									<label for="formName" class="requiredInput"><b><s:text name="questionlibrary.search.questiontext.display"/></b></label>&nbsp;
								</td>
								<td>
									<s:textarea name="questionForm.text" cols="20" rows="10" tabindex="2" id="questionText" onkeyup="enableStep3();"/>
								</td>
							</tr>				
							<tr>
								<td valign="top"> 
									<label for="formName"><b><s:text name="questionlibrary.search.textBelowQuestion"/></b></label>&nbsp;
								</td>
								<td>
									<s:textarea name="questionForm.descriptionDown" cols="20" rows="3" tabindex="2" id="descriptionDown"/>
								</td>
							</tr>		
						</table>
						
						<table id="forceVersion" style='dispaly:none'>
							<tr>
								<td>
									<h3>
										<s:text name="question.version.force.new" /><s:text name="app.formitem.separator" />
										<input type="checkbox" value="1" id="versionUpdate" name="versionUpdate" tabindex="4" >
									</h3>
								</td>
							</tr>
						</table>
					</td>
					<td>&nbsp;&nbsp;&nbsp;&nbsp;</td>
					<td>
						<%--right table--%>
						<table id="questionTypeShow">
							<tr>
								<td>
									<table id="visualScale" style="display: none;" frame="box"	width="50%" align="left">
										<tr>
											<td>
												<h4 align="left"><s:text name="form.addEditQuestion.visualScale.instruction"/></h4>
											</td>
										</tr>
										<tr>
											<td>
												<img src="<s:property value="#imageRoot"/>/spacer.gif" width="1" height="15" alt="" border="0" />
											</td>
										</tr>
										<tr>
											<td>
												<table>
													<tr>
														<td class="formItemLabel"><div id="leftT"></div></td>
														<td align="center" class="formItemLabel">
															<div class="slider" id="sliderEx" style="width: 100mm;"></div>
															<div id="centerT"></div>
														</td>
														<td class="formItemLabel"><div id="rightT"></div></td>
													</tr>
												</table>
											</td>
										</tr>
										<tr>
											<td valign="top" align="left">
												<s:text name="form.addEditQuestion.visualScale.SRMin"/>
												<s:textfield name="questionForm.rangeStart" size="5" id="rangeStart" />
											</td>
										</tr>
										<tr>
											<td valign="top" align="left">
												<s:text name="form.addEditQuestion.visualScale.SRMax"/> 
												<s:textfield name="questionForm.rangeEnd" size="5" id="rangeEnd" />
											</td>
										</tr>
										<tr>
											<td valign="top" align="left">
												<s:text name="form.addEditQuestion.visualScale.SWdith"/> 
												<s:textfield name="questionForm.width" size="5" id="scaleWidth" onkeyup="changeSliderWidth();" onchange="changeSliderWidth();" />
		
											</td>
										</tr>
										<tr>
											<td valign="top" align="left">
												<s:text name="form.addEditQuestion.visualScale.ShowCursor"/>
												<s:checkbox name="questionForm.showHandle" fieldValue="true" id="scaleCursor" onclick="updateCursor(this);" />
											</td>
										</tr>
										<tr>
											<td valign="top" align="left">
												<s:text name="form.addEditQuestion.visualScale.centerText"/>
												<s:textfield name="questionForm.centerText" id="centerText" onkeyup="updateTxt(this, 'center');" />
											</td>
										</tr>
										<tr>
											<td valign="top" align="left">
												<s:text name="form.addEditQuestion.visualScale.leftText"/> 
												<s:textfield name="questionForm.leftText" id="leftText" onkeyup="updateTxt(this, 'left');" />
											</td>
										</tr>
											<tr>
												<td valign="top" align="left">
													<s:text name="form.addEditQuestion.visualScale.rightText"/> 
													<s:textfield name="questionForm.rightText" id="rightText" onkeyup="updateTxt(this, 'right');" />
												</td>
											</tr>
									</table>  <%--Visual end --%>
									
									<table id="imageMape" style="display:none;" frame="box">
										<tr>
											<td>
												<%--the src given by javascript 'checkQuestionType'--%> 
												<iframe id="imageTypeFr" src="" frameborder="0" scrolling="auto" width=640 height=830></iframe>
											</td>
										</tr>
									</table> <%--imageMap end --%>
									
									<table id="selects" style="display:none;" frame="box" width="75%">
										<tr align="left">
											<td colspan=3>
												<h4><s:text name="form.addEditQuestion.select.instruction"/></h4>
											</td>
										</tr>
										<tr>
											<td>
												<table>
													<tr>
														<td valign="top" align='left' style="padding-top: 7px" nowrap width="120">
															<s:hidden  name="questionForm.includeOtherOption" id="includeOther" onclick="showTriggerAnswers()" /> <!--<s:text name="questionlibrary.wizardSpecify"/>--><br>
														</td>													
													</tr>
													<tr>
														<td align='left' valign="top" class="formItemLabel" style="padding-top: 7px" nowrap width="120">
															<s:text name="form.addEditQuestion.select.multipleChoiceOptions"/><s:text name="app.formitem.separator" />
															<label for="formName" class="requiredInput"></label>
															<input type="text" name="optionChoice" size="15" id="optionChoice">
														</td>
													</tr>
													<tr>
														<td id="optionScoreTD" align='left' class="formItemLabel" style="display: none; padding-top: 7px" nowrap width="120">
															<s:text name="form.addEditQuestion.select.score"/><s:text name="app.formitem.separator" />
															<input type="text" name="optionScore" id="optionScore" size="15">
														</td>
													</tr>
													
													<tr>
														<td id="optionSubmittedValueTD" align='left' class="formItemLabel" padding-top: 7px" nowrap width="120">
															<s:text name="form.addEditQuestion.select.submittedValue"/><s:text name="app.formitem.separator" />
															<input type="text" name="optionSubmittedValue" id="optionSubmittedValue" size="15">
														</td>
													</tr>
												</table>
											</td>
											<td>
												&nbsp;<img src="<s:property value="#imageRoot"/>/buttonArrowRight.png"
														alt="To Right" title="To Right" border="0" height="23" width="23"
														onClick="transferTextItemToSelectX(document.getElementById('optionChoice'), document.getElementById('optionScore'),document.getElementById('optionSubmittedValue'), document.getElementById('options'), 'NO');showTriggerAnswers()">&nbsp;
											</td>
											<td>
												<table>
													<tr>
														<td>
															<div id="divoptions" class="resizeMe" style="width: 175px; border: 2px outset white; height: 110px;">
																<s:select multiple="true" size="7" name="questionForm.options" id="options" list="#request.answers"
																	cssStyle="width:100%;height:100%;" cssClass="ctdbSelect" ondblclick="editAnswerOption();" />
															</div>
														</td>
														<td align="left" valign="middle">
															<img src="<s:property value="#imageRoot"/>/buttonArrowUp.png"
																 alt="Up" title="Up" border="0" width="23" height="34"
																 onclick="swapItem(document.getElementById('options'), 'UP'); showTriggerAnswers()" /><br>
															<%
																if (session.getAttribute("isAdministeredOnForm") == null || 
																    !session.getAttribute("isAdministeredOnForm").equals("true")) {
															%>
																	<img src="<s:property value="#imageRoot"/>/buttonRemove.png" alt="Delete" title="Click to delete" border="0" 
																 		 width="23" height="23" onclick="removeItem(document.getElementById('options'), 'NO'); showTriggerAnswers();" />
															<%	} %>
															<br>
															<img src="<s:property value="#imageRoot"/>/buttonArrowDown.png"
																 alt="Down" title="Down" border="0" width="23" height="32"
																 onclick="swapItem(document.getElementById('options'), 'DOWN');showTriggerAnswers()" />
														 </td>
													</tr>
												</table>
											</td>
										</tr>
									</table> <%--selects end --%>
								</td>
							</tr>
						</table>
					</td>		
				</tr>
				<tr>
					<td>
						<h3 align="left" class="toggleable collapsed" id='defaultH'>
							<s:text name="questionlibrary.question.wizard.subtitle.value" />
						</h3>
						<div id="valueDiv" style="display: none;">
							<table>
								<tr>
									<td valign="middle" align="right"> 
										<b><s:text name="questionlibrary.question.wizard.subtitle.value" /></b>&nbsp;
									</td>
									<td>
										<s:textfield name="questionForm.defaultValue" size="30" id="defaultValue" />
									</td>
								</tr>
								<tr>
									<td valign="middle" align="right">
										<b><s:text name="questionlibrary.question.wizard.subtitle.unanswervalue"/></b>&nbsp;
									</td>
									<td>
										<s:textfield name="questionForm.unansweredValue" size="30" maxlength="3999" id="unansweredValue" />
									</td>
								</tr>
							</table>
						</div>
					</td>
				</tr>
			</table> <%--questionInfo end --%>
		</div>
		
		<div align="right">	
			<input type="button" id="searchAddBtu" value="<s:text name='button.Save'/>" alt="Save Change" style="display: none;" onclick="unlockQuestion(1);javascript:selectAllOptions(document.getElementById('options'));addEditQuestion_fancyConfirm();" title ="Click to save changes"/>
			<input type="button" id="createAddBtu" value="<s:text name='button.Add'/>" alt="Add Question" style="display: none;" onclick="unlockQuestion();javascript:selectAllOptions(document.getElementById('options'));addEditQuestion_fancyConfirm();" title ="Click to add question"/>
			<input type="button" id="questionEditButton" value="<s:text name='button.Save'/>" alt="Save Change" style="display: none;" onclick="unlockQuestion(1);javascript:selectAllOptions(document.getElementById('options'));addEditQuestion_fancyConfirm();" title ="Click to save changes"/>
		</div>
	</div>
	
<!-- =============================================================================================== -->	
	<div id="step3">			
		<%--add Graphics --%>
		<h3 align="left" class="toggleable collapsed" id='graphicH'>
			<s:text name="question.image.graphic.display" />
		</h3>
		<div id="graphicDiv" style="display:none;">
			<div align="left"><s:text name="question.image.graphicInstrustion.display"/></div>
			<iframe id="graphicFr" src="" frameborder="0" scrolling="auto" width=1461 height=228></iframe>
		</div>
		
		<%--Groups--%>  
		<h3 align="left" class="toggleable collapsed" id="groupH"> 
			<s:text name="questionlibrary.tab.groups.display" />
		</h3>
		<div id="GroupDiv" class="dataTableContainer" style="display: none;">
			<p align="left">
				<s:text name="question.group.select.intro.display" />
			</p>
			<display:table border="0" width="100%" cellpadding="4" cellspacing="1" name="availableQuestionGroups" 
					scope="request" decorator="gov.nih.nichd.ctdb.question.tag.GroupHomeDecorator">
				<display:setProperty name="basic.msg.empty_list" value="There are no groups to display at this time." />
				<display:column nowrap="true" property="groupIdCheckbox" title="" align="center" width="10%"/>
				<display:column property="groupName" title="Group Name" nowrap="true" width="40%"/>
				<display:column property="descr" title="Description" nowrap="true" width="60%"/>
			</display:table>
		</div>

		<%--data spring --%>
		<h3 align="left" class="toggleable collapsed" id='dataSpringH'>
			<s:text name="form.addEditQuestion.dataSpring.title"/>
		</h3>
		<div id="dataSpringDiv" style="display: none;" align="left">
			<div><s:text name="form.addEditQuestion.dataSpring.instruction"/></div>
			<table>
				<tr align="left">
					<td><b><s:text name="form.addEditQuestion.dataSpring.subinstruction"/></b></td>
					<td><s:checkbox name="questionForm.dataSpring" id="dataSpring"/></td>
				</tr>
			</table>
		</div>
			
		<%--validation --%>	
		<h3 align="left" class="toggleable collapsed" id='validationH'>
			<s:text name="form.addEditQuestion.validation.title"/>
		</h3>
		<div id="validationDiv" style="display: none;" align="left">
			<div>
				<s:text name="form.addEditQuestion.validation.instruction"/>	</br>
				<label class="requiredInput"></label> 
                <i> <s:text name="form.addEditQuestion.requiredSymbol.display"/></i>		
			</div>
			<br>
			<table>
				<tr>
					<td>
						<table  id='leftValidTable'>
					         <tr>
						         <td valign="middle" align="right" class="formItemLabel" nowrap >
						            <label for="formName" class="requiredInput" ><b><s:text name="form.addEditQuestion.validation.answerRequired"/></b></label>&nbsp;
						         </td>
						         <td>
						            <s:select name="questionForm.required" id="required" cssStyle="width:145px;" value="%{questionForm.required}"
						            	list="#{'false':getText('form.addEditQuestion.validation.no'), 'true':getText('form.addEditQuestion.validation.yes')}" />
						          </td>			          
						       </tr>
						       <tr>
							       <td valign="middle" align="right" class="formItemLabel" nowrap>
							       	<label for="formName" class="requiredInput"><b><s:text name="form.addEditQuestion.validation.answerType"/></b></label>&nbsp;
							       </td>
							       <td>
							       		<s:select name="questionForm.answerType" id="answerType" onchange="checkAnswerType()" cssStyle="width:145px;"
			                        		list="#request.answerTypes" listKey="value" listValue="dispValue" />&nbsp;
			                        	<input type="button" id="calculationEditButton" value="<s:text name='button.form.addCalculationRule'/>" style="display:none;" 
			                        		onclick="showEditCalculation();getAvailableCalculateQuesions();" title ="Click to add calculation rule">
							       		<img src="../images/icons/info-icon.png" id="calculateMassege" title="A question involved in another question's calculation cannot be turned into a calculated question">
							       </td>
						       </tr>
						       <tr>
					               <td valign="middle" align="right" class="formItemLabel">
					                   <b><s:text name="questionlibrary.validation.minimumcharacters.display" /></b>&nbsp;
					               </td>
					               <td>
					                   <s:textfield name="questionForm.minCharacters" size="10" maxlength="50" id="minCharacters" value="0" cssStyle="width:145px;"/>
					                   <img src="../images/icons/info-icon.png" title="Only active when the answer type is string.">
					               </td>
					           </tr>
					           <tr>
					           		<td valign="middle" align="right" class="formItemLabel" nowrap>
					                    <b><s:text name="questionlibrary.validation.maximumcharacters.display" /></b>&nbsp;
					               </td>
					               <td>
					                	<s:textfield name="questionForm.maxCharacters" size="10" maxlength="50" id="maxCharacters" value="4000" cssStyle="width:145px;"/>	
					                	<img src="../images/icons/info-icon.png" title="Only active when the answer type is string.">	
					               </td>
					           </tr>
					           <tr>
					                <td valign="middle" align="right" class="formItemLabel" nowrap >
					                    <b><s:text name="questionlibrary.validation.rangeOperator"/></b>&nbsp;
					                </td>
					                <td>
					                    <s:select name="questionForm.rangeOperator" id="rangeOperator" onchange="checkRangeOperator();" cssStyle="width:145px;" 
					                        list="#request.rangeOptions" listKey="id" listValue="shortName" />
					                    <img src="../images/icons/info-icon.png" title="Only active when the answer type is numeric.">	
					                </td>
					           </tr>
					           <tr>
					               <td valign="middle" align="right" class="formItemLabel" nowrap>
					                   <b><s:text name="questionlibrary.validation.rangeValue1"/></b>&nbsp;
					               </td>
					               <td>
					                   <s:textfield name="questionForm.rangeValue1" size="10" maxlength="30" id="rangeValue1" cssStyle="width:145px;"/>
					                   <img src="../images/icons/info-icon.png" title="Maximum value for data element (optional).">	
					               </td>
					           </tr>
					           <tr>
					               <td valign="middle" align="right" class="formItemLabel" nowrap>
					                   <b><s:text name="questionlibrary.validation.rangeValue2"/></b>&nbsp;
					               </td>
					               <td>
					                   <s:textfield name="questionForm.rangeValue2" size="10" maxlength="30"  id="rangeValue2" cssStyle="width:145px;"/>
					                   <img src="../images/icons/info-icon.png" title="Minimum value for data element (optional), and is only applicable when &quot Between &quot from the range operator is selected.">	
					               </td>
					           </tr>
					            
								<tr id="decimalPrecisionDiv" style="display: none;">
						       		<td valign="middle" align="right" class="formItemLabel" nowrap>
						       			<b><s:text name="questionlibrary.decimalPrecision.select"/></b>&nbsp;
						       		</td>
						       		<td>
						       			<s:select name="questionForm.decimalPrecision" id="decimalPrecision" 
						       				list="#{'-1':'Actual Value', '0':'0', '1':'1', '2':'2', '3':'3', 
						       						'4':'4', '5':'5', '6':'6', '7':'7', '8':'8'}" />
						       		</td>
								</tr>
					            <tr>
					                <td colspan=2 align=center>
					                	<div id="infoDiv"></div>
					                </td>
					            </tr>		            
						</table>
					</td>
					
					<td>&nbsp;&nbsp;&nbsp;</td>
					<td valign="top">
				       	<table id="editCalculationTable" style="display: none;" frame="border">
				       		<tr>
				       			<td valign="top" colspan="5">
				       				<b><s:text name="questionlibrary.calculatorFormula"/>:</b><br>
							    	<span>
							    		<div id="window" style="height:55px;border-bottom:medium dotted;"></div>
							    		<%--show calculate formular there --%>
							    	</span>
						    	</td>
						    	<td valign="top">
						    		<br>
						    		<b><s:text name='questionlibrary.validation.type.title'/></b>&nbsp;
						    		<select id="conversionFactor">
        								<option value="<%=Integer.MIN_VALUE%>"><s:text name='questionlibrary.validation.time.none'/></option>
        								<option value="7"><s:text name='questionlibrary.validation.time.seconds'/></option>
										<option value="6"><s:text name='questionlibrary.validation.time.minutes'/></option>
										<option value="5"><s:text name='questionlibrary.validation.time.hours'/></option>
										<option value="4"><s:text name='questionlibrary.validation.time.days'/></option>
										<option value="3"><s:text name='questionlibrary.validation.time.weeks'/></option>
										<option value="2"><s:text name='questionlibrary.validation.time.months'/></option>
										<option value="1"><s:text name='questionlibrary.validation.time.years'/></option>
        							</select><br>
						    		<b><s:text name='questionlibrary.validation.availableQuestions'/></b><br>
						    		<div id="availableQuestionsToCalculateDiv" >
								        <select  id="questions" multiple onclick="javascript:addQuestion()">
								        	<%--show available questions there --%>
								        </select>
							        </div>;l
						    	</td>
						    	<td>&nbsp;&nbsp;&nbsp;</td>
				       			<td>
				       				<table>
						                <tr width="100%" algin="right">
						                    <td colspan="5" width="100%" align="right">
						                        <a href="javascript:backspace()"><img border="0" src="<s:property value="#webRoot"/>/images/calc_backspace.png"></a>
						                        <a href="javascript:clear()"><img border="0" src="<s:property value="#webRoot"/>/images/calc_clear.png"></a>
						                    </td>
						                </tr>
						                <tr>
						                    <td><a href="javascript:number('7')"><img border="0" src="<s:property value="#webRoot"/>/images/calc_seven.png"></a></td>
						                    <td><a href="javascript:number('8')"><img border="0" src="<s:property value="#webRoot"/>/images/calc_eight.png"></a></td>
						                    <td><a href="javascript:number('9')"><img border="0" src="<s:property value="#webRoot"/>/images/calc_nine.png"></a></td>
						                    <td><a href="javascript:button('/')"><img border="0" src="<s:property value="#webRoot"/>/images/calc_divide.png"></a></td>
						                    <td><a href="javascript:paren(' sqrt(')"><img border="0" src="<s:property value="#webRoot"/>/images/calc_sqrt.png"></a></td>
						                </tr>
						                <tr>
						                    <td><a href="javascript:number('4')"><img border="0" src="<s:property value="#webRoot"/>/images/calc_four.png"></a></td>
						                    <td><a href="javascript:number('5')"><img border="0" src="<s:property value="#webRoot"/>/images/calc_five.png"></a></td>
						                    <td><a href="javascript:number('6')"><img border="0" src="<s:property value="#webRoot"/>/images/calc_six.png"></a></td>
						                    <td><a href="javascript:button('*')"><img border="0" src="<s:property value="#webRoot"/>/images/calc_multiply_star.png"></a></td>
						                    <td><a href="javascript:percent()"><img border="0" src="<s:property value="#webRoot"/>/images/calc_percent.png"></a></td>
						                </tr>
						                <tr>
						                    <td><a href="javascript:number('1')"><img border="0" src="<s:property value="#webRoot"/>/images/calc_one.png"></a></td>
						                    <td><a href="javascript:number('2')"><img border="0" src="<s:property value="#webRoot"/>/images/calc_two.png"></a></td>
						                    <td><a href="javascript:number('3')"><img border="0" src="<s:property value="#webRoot"/>/images/calc_three.png"></a></td>
						                    <td><a href="javascript:button('-')"><img border="0" src="<s:property value="#webRoot"/>/images/calc_minus.png"></a></td>
						                    <td><a href="javascript:button('+')"><img border="0" src="<s:property value="#webRoot"/>/images/calc_plus.png"></a></td>
						                </tr>
						                <tr>
						                    <td><a href="javascript:flipsign()"><img border="0" src="<s:property value="#webRoot"/>/images/calc_plus_minus.png"></a></td>
						                    <td><a href="javascript:number('0')"><img border="0" src="<s:property value="#webRoot"/>/images/calc_zero.png"></a></td>
						                    <td><a href="javascript:number('.')"><img border="0" src="<s:property value="#webRoot"/>/images/calc_decimal.png"></a></td>
						                    <td><a href="javascript:paren(' (')"><img border="0" src="<s:property value="#webRoot"/>/images/calc_paren_left.png"></a></td>
						                    <td><a href="javascript:paren(') ')"><img border="0" src="<s:property value="#webRoot"/>/images/calc_paren_right.png"></a></td>
						                </tr>
						            </table>				       		
				       			</td>
				       			<td>&nbsp;&nbsp;&nbsp;</td>
						    	<td valign="bottom">
									<input type="button" value="<s:text name='button.Cancel'/>" onclick="cancelCalculationEdit()" title = "Click to cancel (changes will not be saved).">
								</td>
				       		</tr>
				       	</table>
				     </td>
				 </tr>
			</table>	       
		</div>
		
		<%-- SI unit conversion --%>
		<h3 align="left" class="toggleable collapsed" id='conversionFactorH'>
			<s:text name="questionlibrary.conversionfactor.title"/>
		</h3>
		<div id="conversionFactorDiv" style="display: none;" align="left">
			<table id="editConversionTable"  frame="border">
				<tr>
					<td valign="top" colspan="5">
						<b><s:text name="questionlibrary.conversionfactor.formula"/>:</b><br>
							<span>
								<div id="windowConv" style="height:55px;border-bottom:medium dotted;"></div>
							</span>
					</td>
					<td valign="top">
						<b><s:text name='questionlibrary.conversionfactor.availableQuestion'/></b><br>
						<div id="questionToConvertDiv" >
							<select id="questionConv" multiple style="width: 250px; height: 200px" onclick="javascript:addQuestionC()">
								<option value="this">This Question</option>
							</select>
						</div>
					</td>
					<td>&nbsp;&nbsp;&nbsp;</td>
					<td>
						<table>
							<tr width="100%" algin="right">
								<td colspan="5" width="100%" align="right">
									<a href="javascript:backspaceC()"><img border="0" src="<s:property value="#webRoot"/>/images/calc_backspace.png"></a>
						            <a href="javascript:clearC()"><img border="0" src="<s:property value="#webRoot"/>/images/calc_clear.png"></a>
								</td>
							</tr>
						    <tr>
								<td><a href="javascript:numberC('7')"><img border="0" src="<s:property value="#webRoot"/>/images/calc_seven.png"></a></td>
								<td><a href="javascript:numberC('8')"><img border="0" src="<s:property value="#webRoot"/>/images/calc_eight.png"></a></td>
								<td><a href="javascript:numberC('9')"><img border="0" src="<s:property value="#webRoot"/>/images/calc_nine.png"></a></td>
								<td><a href="javascript:buttonC('/')"><img border="0" src="<s:property value="#webRoot"/>/images/calc_divide.png"></a></td>
								<td><a href="javascript:parenC(' sqrt(')"><img border="0" src="<s:property value="#webRoot"/>/images/calc_sqrt.png"></a></td>
							</tr>
							<tr>
								<td><a href="javascript:numberC('4')"><img border="0" src="<s:property value="#webRoot"/>/images/calc_four.png"></a></td>
								<td><a href="javascript:numberC('5')"><img border="0" src="<s:property value="#webRoot"/>/images/calc_five.png"></a></td>
								<td><a href="javascript:numberC('6')"><img border="0" src="<s:property value="#webRoot"/>/images/calc_six.png"></a></td>
								<td><a href="javascript:buttonC('*')"><img border="0" src="<s:property value="#webRoot"/>/images/calc_multiply_star.png"></a></td>
								<td><a href="javascript:percentC()"><img border="0" src="<s:property value="#webRoot"/>/images/calc_percent.png"></a></td>
							</tr>
							<tr>
								<td><a href="javascript:numberC('1')"><img border="0" src="<s:property value="#webRoot"/>/images/calc_one.png"></a></td>
								<td><a href="javascript:numberC('2')"><img border="0" src="<s:property value="#webRoot"/>/images/calc_two.png"></a></td>
								<td><a href="javascript:numberC('3')"><img border="0" src="<s:property value="#webRoot"/>/images/calc_three.png"></a></td>
								<td><a href="javascript:buttonC('-')"><img border="0" src="<s:property value="#webRoot"/>/images/calc_minus.png"></a></td>
								<td><a href="javascript:buttonC('+')"><img border="0" src="<s:property value="#webRoot"/>/images/calc_plus.png"></a></td>
							</tr>
							<tr>
								<td><a href="javascript:flipsignC()"><img border="0" src="<s:property value="#webRoot"/>/images/calc_plus_minus.png"></a></td>
								<td><a href="javascript:numberC('0')"><img border="0" src="<s:property value="#webRoot"/>/images/calc_zero.png"></a></td>
								<td><a href="javascript:numberC('.')"><img border="0" src="<s:property value="#webRoot"/>/images/calc_decimal.png"></a></td>
								<td><a href="javascript:parenC(' (')"><img border="0" src="<s:property value="#webRoot"/>/images/calc_paren_left.png"></a></td>
								<td><a href="javascript:parenC(') ')"><img border="0" src="<s:property value="#webRoot"/>/images/calc_paren_right.png"></a></td>
							</tr>
						</table>				       		
					</td>
					<td>&nbsp;&nbsp;&nbsp;</td>
					<td valign="bottom">
						<input type="button" value="<s:text name='button.Cancel'/>" onclick="cancelConversionEdit()" title = "Click to cancel (changes will not be saved).">
					</td>
				</tr>
			</table>
		</div>

		
		<%--Prepopulation --%>
		<h3 align="left" class="toggleable collapsed" id='prepopulationH'>
			<s:text name="questionlibrary.prepopulation.title"/>
		</h3>
		<div id="prepopulationDiv" style="display: none;" align="left">
			<table>
				<tr>
					<td>
						<b><s:text name="questionlibrary.prepopulation.checkbox"/></b>&nbsp;
            			<s:checkbox name="questionForm.prepopulation" id="prepopulation" onclick="activateDeactivatePrepopulationValueSelect()"/> 
					</td>
				</tr>
				<tr>
					<td>&nbsp;</td>
				</tr>
				<tr>
					<td>
						<b><s:text name="questionlibrary.prepopulation.select"/></b>&nbsp;
						<s:select name="questionForm.prepopulationValue" id="prepopulationValue" disabled="true" 
							list="#{'none':'- Please Select', 'primarySiteName':'Primary Site Name', 
									'visitType':'Visit Type', 'visitDate':'Visit Date', 'guid':'GUID'}" />
					</td>
				</tr>
			</table>
		</div>

		
		<%--Skip Rule --%>	
		<h3 align="left" class="toggleable collapsed" id='skipRuleH'>
			<s:text name="questionlibrary.skipRule.title"/>
		</h3>
		<div id="skipRuleDiv" style="display: none;" align="left">
			<table>
				<tr>
					<td>
						<div><s:text name="questionlibrary.skipRule.instruction"/></div><br>
					</td>
				</tr>
				<tr>
					<td>
				        <b><s:text name="questionlibrary.skipRuleOperator.title"/></b>&nbsp;
				        <s:select onchange="showEquals();resetSkipRule()" name="questionForm.skipRuleOperatorType" id="skipRuleOperatorType" 
				        	cssStyle="width:145px;" list="#request.skipRuleOperatorTypes" listKey="value" listValue="dispValue"
					   		headerKey="%{@java.lang.Integer@MIN_VALUE}" headerValue="None" />
						<s:textfield name="questionForm.skipRuleEquals" disabled="true" size="20" maxlength="50" id="skipRuleEquals"/>
				        <a title="<s:text name="questionlibrary.skipRul.tooltip" />"><img src="../images/icons/info-icon.png"></a> 
				     </td>
				  </tr>
				  <tr>
				      <td>
				      	&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
					   	<b><s:text name="questionlibrary.skipRule.title"/></b>&nbsp;
					   	<s:select name="questionForm.skipRuleType" id="skipRuleType" cssStyle="width:145px;" 
					   		list="#request.skipRuleTypes" listKey="value" listValue="dispValue"
					   		headerKey="%{@java.lang.Integer@MIN_VALUE}" headerValue="None" />
					   </td>
				   </tr>
				   <tr>
					   <td>
							<br>
							<b><s:text name="questionlibrary.skipRule.availableQ"/></b><br>
							<input type="checkbox" id='selectAllSkip' onClick="selectDeselectAllSkip()"/>&nbsp;Select All
							<div id="divquestionsToSkip" style="width: 400px; height: 200px; overflow: scroll;border: solid 1px #B6B77B;"></div>
						</td>
					</tr>				
			</table>
		</div>		
		
		<%--Email Notifications --%>	
		<h3 align="left" class="toggleable collapsed" id='emailNotificationsH' style="display: none;"><%--the default type is textbox and the textbox doesn't need email attribute so hided --%>
			<s:text name="questionlibrary.email.title"/>
		</h3>
		<div id="emailNotificationsDiv" style="display: none;" align="left">
			<table>
				<tr>
					<td>
					 	<div>
					 		<s:text name="questionlibrary.email.instruction"/><br>
    	                	<em><s:text name="questionlibrary.email.subinstruction"/></em>
    	                </div><br>
					</td>
				</tr>
				 <tr>
                    <td>
                    	&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
                    	<b><s:text name="questionlibrary.email.recipient"/></b>&nbsp;&nbsp;&nbsp;<s:textfield name="questionForm.emailTrigger.toEmailAddress" id="_et_toAddr"/>
                    </td>
                </tr>
                <tr>
                    <td>
                    	<b><s:text name="questionlibrary.email.CCrecipient"/></b>&nbsp;&nbsp;<s:textfield name="questionForm.emailTrigger.ccEmailAddress" id="_et_ccAddr" />
                    </td>
                </tr>
                <tr>
                    <td>
                       	&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
                    	<b><s:text name="questionlibrary.email.subject"/></b>&nbsp; <s:textfield name="questionForm.emailTrigger.subject" id="_et_subject"/>
                    </td>
                </tr>
                <tr>
                    <td>
                    	<b><s:text name="questionlibrary.email.body"/></b>&nbsp; <s:textarea name="questionForm.emailTrigger.body" cols="15" rows="5" tabindex="2" id="_et_body" />
                    	<script type="text/javascript">
    						document.getElementById('_et_body').setAttribute('maxlength','4000');
						</script>
                    </td>
                </tr>
                <tr>
                    <td>&nbsp;<i><s:text name="questionlibrary.email.note"/></i></td>
                </tr>
				<tr>
                    <td>
                    	<b><s:text name="questionlibrary.email.answersToTrigger"/></b> 
                		<div id='triggerAnswerDiv'>
                			<select id="_et_answers" multiple></select>
                		</div>
                		<br>
                		<input type="button" value='<s:text name="button.form.deleteEmailTrigger"/>' onclick='deleteTrigger()'title="Click to delete email trigger">
                	</td>
                </tr> 
			</table>
		</div>	

		<%--Format --%>	
		<h3 align="left" class="toggleable collapsed" id='formatH'>
			<s:text name="questionlibrary.format.title"/>
		</h3>
		<div id='formatDiv' style="display: none;" align="left">
			<table>
				<tr>
					<td><s:text name="questionlibrary.format.instruction"/></td>
				</tr>
				<tr><td><img src="<s:property value="#imageRoot"/>/spacer.gif" width="1" height="10" alt="" border="0" /></td></tr>
				<tr>
	                <td>
	                    <b><s:text name="questionlibrary.format.horizontalAlign"/></b>&nbsp;
						<s:select name="questionForm.align" id="align" cssStyle="width:145px;" 
							list="#{'left':getText('questionlibrary.format.horizontalAlign.left'),
									'right':getText('questionlibrary.format.horizontalAlign.right'),
									'center':getText('questionlibrary.format.horizontalAlign.center')}" />
	                </td>
            	</tr>
            	<tr>
	                <td>
	                	&nbsp;&nbsp;&nbsp;&nbsp;
	                    <b><s:text name="questionlibrary.format.verticalAlign"/></b>
						<s:select name="questionForm.vAlign" id="vAlign" cssStyle="width:145px;"
							list="#{'top':getText('questionlibrary.format.verticalAlign.top'),
									'baseline':getText('questionlibrary.format.horizontalAlign.baseline'),
									'bottom':getText('questionlibrary.format.horizontalAlign.bottom'),
									'middle':getText('questionlibrary.format.horizontalAlign.middle')}" />
	                </td>
	            </tr>
            	<tr>
	                <td>
	                	&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
	                    <b><s:text name="questionlibrary.format.fontColor"/></b>&nbsp;&nbsp;
						<s:select name="questionForm.color" id="color" cssStyle="width:145px;"
							list="#{'black':getText('form.formatting.color.Black'),
									'aqua':getText('form.formatting.color.Aqua'),
									'blue':getText('form.formatting.color.Blue'),
									'fuchsia':getText('form.formatting.color.Fuchsia'),
									'gray':getText('form.formatting.color.Gray'),
									'green':getText('form.formatting.color.Green'),
									'lime':getText('form.formatting.color.Lime'),
									'maroon':getText('form.formatting.color.Maroon'),
									'navy':getText('form.formatting.color.Navy'),
									'olive':getText('form.formatting.color.Olive'),
									'purple':getText('form.formatting.color.Purple'),
									'red':getText('form.formatting.color.Red'),
									'silver':getText('form.formatting.color.Silver'),
									'teal':getText('form.formatting.color.Teal'),
									'yellow':getText('form.formatting.color.Yellow')}" />
	                </td>
	            </tr>
	            <tr>
	                <td>
	                	&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
	                    <b><s:text name="questionlibrary.format.fontFace"/></b>&nbsp;&nbsp;
						<s:select name="questionForm.fontFace" id="fontFace" cssStyle="width:145px;"
							list="#{'arial':'Arial', 'courier new':'Courier New', 'fixedsys':'Fixedsys',
									'sans-serif':'MS Sans Serif', 'times new roman':'Times New Roman'}" />
	                </td>
	            </tr>
	            <tr>
	                <td>
	                	&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
	                    <b><s:text name="questionlibrary.format.fontSize"/></b>&nbsp;&nbsp;
						<s:select name="questionForm.fontSize" id="fontSize" cssStyle="width:145px;" 
							list="#{'0':getText('questionlibrary.format.fontSize.default')+'(10)', 
									'2':'7.5', '+1':'13.5', '+2':'18', '+3':'24', '+4':'36'}" />
	                </td>
	            </tr>
	            <tr>
	                <td>
	                	&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
	                    <b><s:text name="questionlibrary.format.indent"/></b>&nbsp;
	                    <s:textfield name="questionForm.indent" id="indent" size="10" maxlength="50" cssStyle="width:145px;"/>
	                    <font style='vertical-align:bottom' size="1pt"><s:text name="questionlibrary.format.lessThan50"/></font>
	                </td>
	            </tr>
            	<tr>
            		<td>
            			<b><s:text name="questionlibrary.format.displayOptionsHorizontally"/></b>&nbsp;
            			<input type="checkbox" id='horizontalDisplay'/>
            		</td>
            	</tr>
            	<tr>
            		<td>
            			<b><s:text name="questionlibrary.format.qTonPreLine"/></b>&nbsp;
            			<input type="checkbox" id='horizDisplayBreak'/>
            		</td>
            	</tr>
			</table>
		</div>	
			
		<div id="submitDiv" align="right">
			<table>
				<tr>
					<td>
						<input type="button" id="searchAddBtuStep3" value="Add" alt="Save Change" onclick="unlockQuestion();javascript:selectAllOptions(document.getElementById('options'));addEditQuestion_fancyConfirm();" title ="Click to save changes"/>
					</td>
					<td>
						<input type="button" id="createAddBtuStep3" value="<s:text name='button.Add'/>" alt="Add Question" onclick="unlockQuestion();javascript:selectAllOptions(document.getElementById('options'));addEditQuestion_fancyConfirm();" title ="Click to add question"/>
				    </td>
				    <td>
				    	<input type="button" id="questionEditButtonStep3" value="<s:text name='button.Save'/>" alt="Save Change" onclick="unlockQuestion(1);javascript:selectAllOptions(document.getElementById('options'));addEditQuestion_fancyConfirm();" title ="Click to save changes"/>
				    </td>
				</tr>
			</table>
		</div>	
	</s:form>
</div> <%--addEditQuestion Div end --%>


<script language="Javascript">
	document.body.focus();
	try {
        initialType = $('#questionType').val();
    } catch (err) {
        // oh well
    }
	$("#sliderEx").slider();
	updateTxt($('#centerText')[0], 'center');
	updateTxt($('#rightText')[0], 'right');
	updateTxt($('#leftText')[0], 'left');
	checkAnswerType();
    checkRangeOperator();
</script>

</html>	