<jsp:include page="/common/doctype.jsp" />
<%@ page import="gov.nih.nichd.ctdb.common.Image"%>
<%@ page import="gov.nih.nichd.ctdb.common.CtdbConstants,
		gov.nih.nichd.ctdb.question.manager.QuestionManager,
		gov.nih.nichd.ctdb.question.domain.Question,
		gov.nih.nichd.ctdb.question.domain.QuestionType,
		gov.nih.nichd.ctdb.common.StrutsConstants"%>
<%@ taglib uri="/struts-tags" prefix="s"%>
<%@ taglib uri="/WEB-INF/display.tld" prefix="display"%>
<%@ taglib uri="/WEB-INF/security.tld" prefix="security"%>
<%@ page import="gov.nih.nichd.ctdb.security.domain.User"%>

<%-- This is the create / edit form page for IBIS --%>

<%
	String formMode = (String) request.getAttribute("formMode");
	String comingFromSaveAs = "";
	if (request.getAttribute("comingFromSaveAs") != null) {
		comingFromSaveAs = (String) request.getAttribute("comingFromSaveAs");
	}
	User user = (User) request.getSession().getAttribute(CtdbConstants.USER_SESSION_KEY);
%>

<%-- CHECK PRIVILEGES --%>
<security:check privileges="addeditforms" />
<s:set var="pageTitle" scope="request">
	<s:text name="form.forms.buildFormDisplay" />
</s:set>

<s:set var="pageTitle" scope="request">
	<s:if test="%{#request.formMode == 'edit'}">
		<s:text name="form.forms.editFormDisplay" />
	</s:if>
	<s:else>
		<s:text name="form.forms.createFormDisplay" />
	</s:else>
</s:set>

<jsp:include page="/common/header_struts2.jsp" />

<script type="text/javascript"  src="<s:property value="#webRoot"/>/common/js/tinymce/jquery.tinymce.min.js"></script>
<script type="text/javascript" src="<s:property value="#webRoot"/>/common/js/range.js"></script>
<script type="text/javascript" src="<s:property value="#webRoot"/>/common/js/timer.js"></script>
<script type="text/javascript" src="<s:property value="#webRoot"/>/common/js/slider.js"></script>
<script type="text/javascript" src="<s:property value="#webRoot"/>/common/js/deepCopy.js"></script>

<p> <s:text name="form.forms.createFormDisplay.text" /> </p>

<script type="text/javascript">

LeftNav.collapsed = true;

var hasChanged=false;

var parsingSymbol='<%=StrutsConstants.alienSymbol%>';
var questionId;
var questionVersionLetter;
var questionVersionNumber;
var questionName;
var questionText;
var descriptionUp; 
var descriptionDown;
var questionType;
var questionOptions;
var imageOption;
var imageFileName;
var defaultValue;
var unansweredValue;
var associatedGroupIds;
var includeOther;
var attachedFormIds;
var hasDecimalPrecision;
var hasCalDependent;
var prepopulation;

var editDataElementName;
var deAssociateFlag=false;

var editQuestionId;
var editQuestionDivId;
var editQuestionType;
var editQuestionVersionNumber;
var editAttachedFormIds=attachedFormIds;

var formInfoMode = "add";
var isDataSpring = false;
var questionInfoMode = "add";

var text = "";

var editFormInfoPressed = false;
var currentSectionID;

//array to hold all the javascript Section objects
var sectionsArray = new Array();

//array to hold all the javascript Question objects // Ching Heng
var questionsArray = new Array();

var existingSectionIdsToDeleteArray = new Array();
var existingQuestionIdsToDeleteArray = new Array();

var proceedToBuildForm = false;
var bogusSectionId = -1;

var containerWidth = $("#rightColumn").width() - $("#leftColumn").width();

//section object
function SectionObj(id, name, description, isCollapsable, ifHasResponseImage, isRepeatable,
		initRepeatedSecs, maxRepeatedSecs, repeatedSectionParent, repeatableGroupName) {
	this.name = trim(name);
	this.description = description;
	this.isCollapsable = isCollapsable == "true" || isCollapsable == true;
	this.ifHasResponseImage = ifHasResponseImage == "true" || ifHasResponseImage == true;
	this.isRepeatable = isRepeatable;
    
	this.initRepeatedSecs = initRepeatedSecs;
	this.maxRepeatedSecs = maxRepeatedSecs;
	
	this.repeatedSectionParent = repeatedSectionParent;
	this.repeatableGroupName = repeatableGroupName;
	
	this.id = id;
	this.existingSection = false;
	this.row = -1;
	this.col = -1;
}

SectionObj.prototype.getQuestions = function() {
	var questions = new Array();
	for (var i = 0; i < questionsArray.length; i++) {
		if (SectionHandler.convertToHTMLId(questionsArray[i].sectionId) == SectionHandler.convertToHTMLId(this.id)) {
			questions.push(questionsArray[i]);
		}
	}
	return questions;
}

SectionObj.prototype.getDiv = function() {
		var addQuestionFancyBoxBtnID = "addQuestionFancyBoxBtn_" + this.id;
		var addQuestionFancyBoxAID = "addQuestionFancyBoxA_" + this.id;
		//create the new section div
		var deleteSectionIconPath = "../images/icons/delete.png";
		var editSectionIconPath =  "../images/icons/cog.png";
		
		// if this section is a repeated child (IE. not the parent of a repeated group or a regular section)
		// then it should not be drawn
		if (this.isRepeatable && this.repeatedSectionParent != -1) {
			return "";
		}
		
		var newSectionDiv;
		if(this.ifHasResponseImage) {
			newSectionDiv = '<div id="' + this.id + '" class="section activeSection" style="width: '+containerWidth+'px">' + 
			"<table width='100%'>" + 
				"<tr>" + 
					"<td class=\"sectionHeader1\">" +
						"<table valign='top'>" + 
							"<tr>" + 
								"<td align='left' valign='top'>" + 
									"<b><s:text name='form.addsection.sectionname' />: </b>" + 
								"</td>" + 
							 	"<td align='left' valign='top' class='sectionNameContainer'>" +
									trim(this.name) +
							 	"</td>" +
							 "</tr>" +
						 "</table>" +
					 "</td>";

					 if (this.isRepeatable) {
						newSectionDiv += "<td id=\"repeatedHeader_"+this.id+"\" align=\"center\">" +
							 	"<b>Repeatable Group:</b> " + this.repeatableGroupName + "&nbsp;&nbsp;&nbsp;<b>Initial:</b> " + this.initRepeatedSecs + "&nbsp;&nbsp;&nbsp;<b>Max:</b> " + this.maxRepeatedSecs +
							 "</td>";
					}
				 	newSectionDiv += 
					"<td align='right' valign='top'>" + 
						"<input type='button' class='addSection'  disabled='true'  title='This is a response image section and questions can not be added' value='<s:text name="form.forms.formInfo.addQuestionDisplay" />' />" +
						"<a href='javascript:void(0)' title='Edit Section' onclick='SectionHandler.editSection(\""+this.id+"\")'><img src='" + editSectionIconPath + "' /></a>" + 
						"<a href='javascript:void(0)' title='Remove Section' onclick='removeSection(\""+this.id+"\",\"<%=formMode%>\")'><img src='" + deleteSectionIconPath + "' /></a>"+
					"</td>" + 
				 "</tr>" + 
				 "<tr>" + 
					"<td align='left' valign='top' colspan='2' class='sectionDescriptionContainer'><em>" + 
						trim(this.description) + 
					"</em></td>" + 
				 "</tr>" +
			"</table>" + 
			'<div class="clearboth"></div>'+
		"</div>";
		} else {
			newSectionDiv = '<div id="' + this.id + '" class="section activeSection" style="width: '+containerWidth+'px;">' + 
			"<table width='100%'>" + 
				"<tr>" + 
					"<td class=\"sectionHeader1\">" +
						"<table valign='top'>" + 
							"<tr>" + 
								"<td align='left' valign='top'>" + 
									"<b><s:text name='form.addsection.sectionname' />: </b>" + 
								"</td>" + 
							 	"<td align='left' valign='top' class='sectionNameContainer'>" +
									trim(this.name) +
							 	"</td>" +
							 "</tr>" +
						 "</table>" +
					 "</td>";

					 if (this.isRepeatable) {
						newSectionDiv += "<td id=\"repeatedHeader_"+this.id+"\" align=\"center\">" +
						 	"<b>Repeatable Group:</b> " + this.repeatableGroupName + "&nbsp;&nbsp;&nbsp;<b>Initial:</b> " + 
						 	this.initRepeatedSecs + "&nbsp;&nbsp;&nbsp;<b>Max:</b> " + this.maxRepeatedSecs +
						 "</td>";
					 }
					newSectionDiv += "<td align='right' valign='top'>" + 
						'<input type="button" class="addText" onclick="triggerAddText(\'' + this.id + '\')" value="<s:text name="form.forms.formInfo.addTextDisplay" />" />'+
						'<input type="button" id="' + addQuestionFancyBoxBtnID + '" class="addSection" onclick="triggerAddQuestionAnchor(\''+ 
								this.id +'\');showSectionQuestion();" value="<s:text name="form.forms.formInfo.addQuestionDisplay" />" />' +
						"<a href='javascript:void(0)' title='Edit Section' onclick='SectionHandler.editSection(\""+this.id+"\")'><img src='" + editSectionIconPath + "' /></a>" + 
						"<a href='javascript:void(0)' title='Remove Section' onclick='removeSection(\""+this.id+"\",\"<%=formMode%>\")'><img src='" + deleteSectionIconPath + "' /></a>"+
					"</td>" + 
				"</tr>" + 
				 "<tr>" + 
					"<td align='left' valign='top' colspan='2' class='sectionDescriptionContainer'><em>" + 
						trim(this.description) + 
					"</em></td>" + 
				 "</tr>" +
			"</table>" + 
			'<div class="clearboth"></div>'+
		"</div>";
		}
		
	return newSectionDiv;
}

// Data Structure Object--Ching Heng
function DataStructureObject(dataStructureName,dataStructureVersion, dataElementsArr,repeatableGroupNamesArr,repeatableGroupsArr){
	this.dataStructureName=dataStructureName;
	this.dataStructureVersion=dataStructureVersion
	this.dataElementsArr=dataElementsArr;
	this.repeatableGroupNamesArr=repeatableGroupNamesArr;
	this.repeatableGroupsArr = repeatableGroupsArr;
}

var DSObj=new DataStructureObject(<%=Integer.MIN_VALUE%>,[]);

function RepeatableGroupObject(repeatableGroupName,repeatableGroupThreshold,repeatableGroupType) {
	this.repeatableGroupName = repeatableGroupName;
	this.repeatableGroupThreshold = repeatableGroupThreshold;
	this.repeatableGroupType = repeatableGroupType;
}

function DataElementObject(dataElementName, dataElementType, requiredType, description, suggestedQuestion, 
		restrictionId, restrictionName, valueRangeList, size, max, min, associated, isGroupRepeatable, order, title) {
	// Info
	this.dataElementName=dataElementName;
	this.dataElementType=dataElementType;
	this.requiredType=requiredType;
	this.description=description;
	this.suggestedQuestion=suggestedQuestion;
	// value
	this.restrictionId=restrictionId; //0=free entry 1=single, 2=multiple
	this.restrictionName=restrictionName;
	this.valueRangeList=valueRangeList;
	this.size=size;
	this.max=max;
	this.min=min;
	this.associated=associated; // it's boolean
	this.isGroupRepeatable=isGroupRepeatable;
	this.order=order;
	this.title=title;
}

var dataStructureHandler = {
		findDataElement : function(dataElementName) {

			var DEarr=DSObj.dataElementsArr;
			for (var i=0; i<DEarr.length; i++) {
				if (DEarr[i].dataElementName == dataElementName) {
					return DEarr[i];
				}
			}
		},
		setDataElementAssociate : function(questionDivId,dataElementName) {
			var QuestioObj=QuestionHandler.findQuestion(questionDivId);
			if (typeof dataElementName == "undefined" || dataElementName == "none") {
				QuestioObj.attributeObject.dataElementName = "none";
				return;
			}
			else {
				// find data element by user selects
				var DEobj=dataStructureHandler.findDataElement(dataElementName);
				var QuestioObj=QuestionHandler.findQuestion(questionDivId);
				
				// set the question attribute
				QuestioObj.attributeObject.dataElementName=DEobj.dataElementName;
			}
		},
		deAssociateDataElement : function(questionDivId) {

			var QuestioObj=QuestionHandler.findQuestion(questionDivId);
			// reset the question attribute to defualt
			QuestioObj.attributeObject.dataElementName='none';
		},
		initiateDataElement : function(questionsArray) {
			for (var i=0; i<questionsArray.length; i++) {
				var qObj=questionsArray[i];
				if (qObj.attributeObject.dataElementName != 'none') {
					var deObj=dataStructureHandler.findDataElement(qObj.attributeObject.dataElementName);
					deObj.associated=true;
				}
			}
		}
}

//question attribute object--Chinge Heng
function AttributeObject() {
	this.qType=1;  //int
	this.required = false;  //boolean
	this.answerType = 1; //int
	this.minCharacters=0; //int
	this.maxCharacters=4000; //int
	this.rangeOperator='';  //string
	this.rangeValue1='';  //string
	this.rangeValue2='';  //string
	this.skipRuleOperatorType=<%=Integer.MIN_VALUE%>;  //int
	this.skipRuleEquals='';  //string
	this.skipRuleType=<%=Integer.MIN_VALUE%>;  //int
	this.questionsToSkip=new Array();  //string array
	this.align = "left";  //string
    this.vAlign = "top";  //string
    this.color = "black";  //string
    this.fontFace = "arial";  //string
    this.fontSize='';  //string
	this.indent = 0;  //int
	this.horizontalDisplay=false;  //boolean
	this.horizDisplayBreak=false;  //boolean
	// data spring
	this.dataSpring=false;
	// Email
	this.deleteTrigger=false;  //boolean
	this.toEmailAddress='';//string
	this.ccEmailAddress=''; //string
	this.subject = 'Emailing from the IBIS'; //string
	this.body=''; //string
	this.triggerAnswers=new Array();  //string array
	this.eMailTriggerId=<%=Integer.MIN_VALUE%>;  //int
	this.eMailTriggerUpdatedBy=<%=Integer.MIN_VALUE%>;  //int
	
	// calculation
	this.calculationType=<%=Integer.MIN_VALUE%>; //int
	this.conversionFactor=<%=Integer.MIN_VALUE%>; //int
	this.questionsToCalculate = new Array(); //string array
	this.calculation=''; //string
    this.calculatedQuestion = false; //boolean
	this.calDependent = false; //boolean
	this.skipRuleDependent = false; //boolean
	
	this.htmlText=''; // it is question text
	this.answerTypeDisplay='';

	this.dataElementName='none';
	
	//added for prepopulation
	this.prepopulation = false;
	this.prepopulationValue = '';
	
	//added for demimal precision
	this.decimalPrecision="-1";
	
	//added for conversion factor
	this.hasUnitConversionFactor = false;
	this.unitConversionFactor='';
}

//function for setting the Question Atrribute object properties____Ching Heng
function setAttributeObject(attribute){
	
	if ($('#required').val() != null) {
		attribute.required = $('#required').val();
	} else {
		attribute.required = false;
	}
	attribute.answerType = $('#answerType').val();
	attribute.minCharacters=parseInt($('#minCharacters').val(),10);
	attribute.maxCharacters=parseInt($('#maxCharacters').val(),10);
	attribute.rangeOperator=$('#rangeOperator').val();
	attribute.rangeValue1=$('#rangeValue1').val();
	attribute.rangeValue2=$('#rangeValue2').val();
	attribute.skipRuleOperatorType=parseInt($('#skipRuleOperatorType').val(),10);
	attribute.skipRuleEquals=$('#skipRuleEquals').val();
	attribute.skipRuleType=parseInt($('#skipRuleType').val(),10);
	
	if($('#dataSpring').is(':checked')){
		attribute.dataSpring=true;
	}
	
	// prepopulation
	if ($('#prepopulation').is(':checked')) {
		attribute.prepopulation=true;
		attribute.prepopulationValue=$('#prepopulationValue').val();
	} else {
		attribute.prepopulation=false;
		attribute.prepopulationValue='';
	}
	
	//decimalPresion
	attribute.decimalPrecision=$('#decimalPrecision').val();
	
	//conversion factor
	if (trim($("#windowConv").text()) != "") {
		attribute.hasUnitConversionFactor=true;
		attribute.unitConversionFactor=trim($("#windowConv").html());
	} else {
		attribute.hasUnitConversionFactor=false;
		attribute.unitConversionFactor="";
	}
	
	// Skip rule
	var skipQuestions=[];
	$('input[name=questionToSkip]:checked').each(function(i) {
		skipQuestions[i] = $(this).val();
	});
	attribute.questionsToSkip=skipQuestions;
	
	// E-mail
	attribute.eMailTriggerUpdatedBy=<%=Integer.toString(user.getId())%>;
	attribute.toEmailAddress=$('#_et_toAddr').val();
	attribute.ccEmailAddress=$('#_et_ccAddr').val();
	attribute.subject = $('#_et_subject').val();
	attribute.body=$('#_et_body').val();
	var theTriggerAnswer = new Array();
	
	$('#_et_answers option:selected').each(function(i) {
		var options=$(this).text().split("|");
		theTriggerAnswer.push(options[0]);
	});
	attribute.triggerAnswers=theTriggerAnswer;

	attribute.align = $('#align').val();
	attribute.vAlign = $('#vAlign').val();
	attribute.color = $('#color').val();
	attribute.fontFace = $('#fontFace').val();
	attribute.fontSize = $('#fontSize').val();
	
	if ($('#indent').val() == '') {
		attribute.indent = 0;
	} else {
		attribute.indent = parseInt($('#indent').val(),10);
	}
	
	if ($('#horizontalDisplay').is(':checked')) {
		attribute.horizontalDisplay=true;	
	} else {
		attribute.horizontalDisplay=false;
	}
	if ($('#horizDisplayBreak').is(':checked')) {
		attribute.horizDisplayBreak=true;	
	} else{
		attribute.horizDisplayBreak=false;
	}
	
	// calculation
	attribute.calculationType=<%=Integer.MIN_VALUE%>;  // ???
	attribute.conversionFactor=$('#conversionFactor').val();
	attribute.calculation=trim($("#window").text());
	// set questionsToCalculate
	var matchRegex = new RegExp(/\[S_[-]?\d+_Q_\d+\]/g); //the question looks like [S_123_Q_123]
	var splitRegex = new RegExp(/[\[S_[-]?\d+_Q_d+\]]/);
	var matchs = trim($("#window").text()).match(matchRegex);
	var questionsToCalculateArr = new Array();
	if (matchs!=null) {
		for (var j=0; j<matchs.length; j++) {
			var dependentQId=matchs[j].split(splitRegex);
			for (var h=0;h<dependentQId.length;h++) {
				if (dependentQId[h]!='') {
					if (!contains(questionsToCalculateArr, dependentQId[h])) {
						questionsToCalculateArr.push(dependentQId[h]);
					}
				}
			}
		}
	}
	attribute.questionsToCalculate=questionsToCalculateArr;
	
    if (attribute.calculation!="") {
    	attribute.calculatedQuestion = true;
    }
    return attribute;
}

function contains(a, obj) {
    var i = a.length;
    while (i--) {
       if (a[i] === obj) {
           return true;
       }
    }
    return false;
}

// set the question's calculation depedent status_Ching Heng
function setCalDependent(){
	var matchRegex = new RegExp(/\[S_[-]?\d+_Q_\d+\]/g); //the question looks like [S_123_Q_123]
	var splitRegex = new RegExp(/[\[S_[-]?\d+_Q_d+\]]/);
	var dependentQIdArray= new Array();
	for (var i=0;i<questionsArray.length;i++) {
		questionsArray[i].attributeObject.calDependent= false; // make all the question calculation dependent flag tp be default
		var calStr=questionsArray[i].attributeObject.calculation;
		var matchs = calStr.match(matchRegex);
		if (matchs!=null){
			for (var j=0;j<matchs.length;j++) {
				var dependentQId=matchs[j].split(splitRegex); 
				for (var h=0;h<dependentQId.length;h++) {
					if (dependentQId[h]!=''){ // dependentQId[h]=[S_123_Q_123]
						var temp=dependentQId[h].split("\_");
						var sId=temp[1];
						var qId=temp[3].substring(0,temp[3].indexOf("\]"));
						var Q=QuestionHandler.findQuestionById(qId);
						var qV=Q.questionVersionNumber;// we need the question version!!
						var divId="S_"+sId+"_"+qId+"_"+qV;
						dependentQIdArray.push(divId);
					}
				}
			}
		}
	}
	for (var u=0;u<dependentQIdArray.length;u++) { // recheck all question's calculate dependence
		for (var x=0;x<questionsArray.length;x++) {			
			if (dependentQIdArray[u]==questionsArray[x].newQuestionDivId) {
				questionsArray[x].attributeObject.calDependent=true;
			}
		}
	}
}

// add by sunny 
function setSkipRuleDependent(){
	var dependentArray= new Array();
	for (var i=0; i<questionsArray.length;i++) {

		var skipArray=questionsArray[i].attributeObject.questionsToSkip;
		questionsArray[i].attributeObject.skipRuleDependent=false;
		if (skipArray.length!=0) {
			questionsArray[i].attributeObject.skipRuleDependent=false;// i think this should be false....the question with the skip rule is not dependent....only the
																	 //questions that it refers.....nish 6/11/2013
			for (var j=0;j<skipArray.length;j++) {
				//alert(skipArray[j]);
				dependentArray.push(skipArray[j]);
			}
		}
	}
	for (var i=0;i<questionsArray.length;i++){		
		var Q=questionsArray[i];
		var secId=SectionHandler.convertToHTMLId(Q.sectionId);
		var Qid=secId+'_Q_'+Q.questionId;
		//alert('S_'+Q.sectionId+'_Q_'+Q.questionId);
		for (var k=0;k<dependentArray.length;k++){ 
			if (Qid==dependentArray[k] && questionsArray[i].attributeObject.skipRuleDependent==false){
				questionsArray[i].attributeObject.skipRuleDependent=true;
			}
		}
	}
}
	
//update question formula-- Ching Heng
function updateFormula(oldSectionId,newSectionId,elementQuestionId){
	for (var i=0;i<questionsArray.length;i++){
		var oldFormula=questionsArray[i].attributeObject.calculation;
		if (oldFormula!=''){
			var oldId="S_"+oldSectionId+"_Q_"+elementQuestionId;
			var newId="S_"+newSectionId+"_Q_"+elementQuestionId;
			var newFormula=replaceAll(oldFormula,oldId,newId);
			questionsArray[i].attributeObject.calculation=newFormula;
		}
	}
}

//update questions to skip--Ching Heng
function updateQuestionToSkip(oldSectionId,newSectionId,questionId){
	for(var i=0;i<questionsArray.length;i++){
		var questionSkipArray=questionsArray[i].attributeObject.questionsToSkip;
		if(questionSkipArray.length>0){
			for(var j=0;j<questionSkipArray.length;j++){
				if(questionSkipArray[j]=="S_"+oldSectionId+"_Q_"+questionId){
					questionSkipArray[j]="S_"+newSectionId+"_Q_"+questionId
				}
			}
			questionsArray[i].attributeObject.questionsToSkip=questionSkipArray;
		}
	}
}

function replaceAll(strOrg,strFind,strReplace){
	 var index = 0;
	 while (strOrg.indexOf(strFind,index) != -1){
	  strOrg = strOrg.replace(strFind,strReplace);
	  index++;
	}
	 return strOrg;
}

//question object--Ching Heng
function QuestionObj(questionId,questionVersionLetter,questionVersionNumber,questionName,questionText,descriptionUp,descriptionDown,questionType,questionOptions,imageOption,imageFileName,visualScaleInfo,newQuestionDivId,
			sectionId,AttributeObject,graphicNames,defaultValue,unansweredValue,associatedGroupIds,includeOther,attachedFormIds,hasDecimalPrecision, hasCalDependent, prepopulation, text) {
	this.questionId = questionId;
	this.questionVersionLetter=questionVersionLetter;
	this.questionVersionNumber=questionVersionNumber;
	this.questionName = questionName;
	this.questionText =questionText.replace(/'/g, "&#39;");
	this.descriptionUp =  descriptionUp.replace(/'/g, "&#39;");
	this.descriptionDown =descriptionDown.replace(/'/g, "&#39;");
	this.questionType = questionType;
	this.hasDecimalPrecision = hasDecimalPrecision;
	this.hasCalDependent = hasCalDependent;
	this.prepopulation = prepopulation;
	this.defaultValue=defaultValue;
	this.unansweredValue=unansweredValue;
	this.associatedGroupIds=associatedGroupIds;
	this.existingQuestion = false;
	this.forcedNewVersion = false;
	
	this.questionOptions=questionOptions;
	this.imageOption=imageOption;
	this.imageFileName=imageFileName;
	this.visualScaleInfo=visualScaleInfo;
	
	this.newQuestionDivId=newQuestionDivId;
	this.sectionId=sectionId;
	this.attributeObject=AttributeObject;
	this.graphicNames=graphicNames;	
	this.includeOther=includeOther;
	this.attachedFormIds=attachedFormIds;
	this.questionOrder = -1;
	
	// used for textblocks
	this.text = text;
}


var QuestionHandler = {
		questionListRef : null,
		findQuestion : function(newQuestionDivId) {
			for (var i = 0; i < questionsArray.length; i++) {
				if (questionsArray[i].newQuestionDivId==newQuestionDivId) {	
					return questionsArray[i];
				}
			}
			return null;
		},
		
		findQuestionById : function(questionId) {
			for (var i = 0; i < questionsArray.length; i++) {
				if (questionsArray[i].questionId == questionId) {
					return questionsArray[i];
				}
			}
			return null;
		},
		
		/**
		 * Finds the question referenced by the ID in newQuestionDivId within the
		 * question object array.  Uses the section ID, question ID, and question version
		 * to do its mighty work.
		 */
		findQuestionIndex : function(newQuestionDivId) {
			for (var i = 0; i < questionsArray.length; i++) {
				if(questionsArray[i].newQuestionDivId==newQuestionDivId){
					return i;
				}
			}
			return null;
		},
		
		/**
		 * this is the point where all methods of adding a question converge.
		 * Handles refreshing the form view in order to make the form look right after adding the quesion
		 */
		addQuestion : function(questionObject) {
			$("#" + questionObject.newQuestionDivId).ready(function() {
				DynamicElements.enforceSectionRows(null, {item: $("#"+questionObject.newQuestionDivId).parent()});
			});
		},
		
		/**
		 * removes a single question from the page completely
		 * @param sectionId the html ID of the question to remove
		 * @param confirm true or undefined to confirm removal
		 */
		removeQuestion : function(removeId, confirmation) { // it's question div Id
			// do this method for all repeatable children if there are any
			var q = QuestionHandler.findQuestion(removeId);
			
			var repeatedQuestions = QuestionHandler.findAllRepeatedQuestions(q);
			if (repeatedQuestions.length > 0) {
				for (var i = 0; i < repeatedQuestions.length; i++) {
					// disable the verification check
					QuestionHandler.removeQuestion(repeatedQuestions[i].newQuestionDivId, true);
				}
			}
			
			var submitForm = true;
			if (typeof confirmation == "undefined" || confirmation == false) {
				submitForm = confirm("Are you sure you want to REMOVE this question from Form? \n Click Ok to continue or Cancel to cancel");
			}
			else {
				submitForm = confirmation;
			}
			if (submitForm) {
				var fflag,msg;
				if (q.attributeObject.calDependent) {
					fflag=false;
					msg='you can not delete this question because it is being used in another question\'s calculation rule';
				} else if (q.attributeObject.skipRuleDependent){
					fflag=false;
					msg='you can not delete this question because it is being used in another question\'s skip rule';
				} else {
					fflag=true;
				}
				if (fflag) {
					// deassociate the like between question and data element
					if(q.attributeObject.dataElementName!='none'){ // if this question has data element

						dataStructureHandler.deAssociateDataElement(q.newQuestionDivId,q.attributeObject.dataElementName);
						if (q.existingQuestion) {
							var sectionID = q.sectionId;
							var splitId = removeId.split("_");
							if (!splitId.length > 1) {
								//need to add this ID to the list of existingQuestionsToRemove
								var eq_string = removeId + "#" + sectionID;
							}
							existingQuestionIdsToDeleteArray.push(eq_string);
						}
					}
					// remove question from question array
					var index = QuestionHandler.findQuestionIndex(removeId);
					questionsArray.splice(index,1); 
					var removedObjectReference = null;
					if ($("#"+removeId).hasClass("question")) {
						removedObjectReference = $("#"+removeId).parent();
					} else if ($("#"+removeId).hasClass("section")) {
						removedObjectReference = $("#"+removeId);
					}
					
					// activate the question before this one if it exists, or the next one if it doesn't
					if ($("#"+removeId).length > 0 && $("#"+removeId).prev(".question").length > 0) {
						changeActiveQuestion($("#"+removeId).prev(".question"));
					} else {
						changeActiveQuestion($("#"+removeId).next(".question"));
					}
					
					// this is likely different from removedObjectReference because
					// this is normally the question
					var $removeElem = $("#"+removeId);
					$removeElem.remove();
					// change the section's resize min height
					if ($removeElem.length > 0) {
						assignMinHeight(removedObjectReference);
						DynamicElements.enforceSectionRows(null, {item: removedObjectReference});
					}
				} else {
					alert(msg);
				}
				setCalDependent(); // reset the calculation dependnce of question 
				setSkipRuleDependent();
			}
		
		},
		
		getQuestionCount : function(question){
			return questionsArray.length;
		},
		populateQuestion : function(question){
			// data element-------------------------------------------------------
			if (questionInfoMode=='edit') {
				if (question.attributeObject.dataElementName!='none') { // question has a data element
					$('#linkDEcheckBox').attr('checked',true);
					var $table = $("#dataElementTable");
					var $inputs = IDT.getAllInputs($table);
					if ($inputs.filter($('#'+ question.attributeObject.dataElementName))) {
						IDT.addSelectedOptionValue($table, question.attributeObject.dataElementName);
						IDT.dataTables["dataElementTable"].fnDraw("false");
					}
				} else {
					$('#linkDEcheckBox').attr('checked',false);
					$('input[name="dataElementRadio"]').each(function(){
						$(this).attr('disabled',true);
						$(this).attr('checked',false);
					});
				}
			}
			// question info------------------------------------------------------
			$('#questionType').val(question.questionType);
			$('#questionName').val(question.questionName);
			$('#questionText').val(String(question.questionText).replace(/&#39;/g, "'"));
			$('#descriptionUp').val(String(question.descriptionUp).replace(/&#39;/g, "'"));
			$('#descriptionDown').val(String(question.descriptionDown).replace(/&#39;/g, "'"));
			$('#includeOther').attr("checked",question.includeOther);
			// question detail-----------------------------------------------------
			$('#defaultValue').val(question.defaultValue);
			$('#unansweredValue').val(question.unansweredValue);
			$('#graphicFr').attr("src","<s:property value="#webRoot"/>/question/showQuestionImage.action?questionId="+editQuestionId);
			$('input[name=questionGroupIds]').each(function(){
				for (var h=0;h<question.associatedGroupIds.length;h++){
					if(question.associatedGroupIds[h]==$(this).val()){
						$(this).attr('checked',true);
					}
				}
			});
			// checkbox,select,multi-select,radio
			if (question.questionOptions!=null && question.questionOptions!='undefined'&& question.questionOptions!='') {
				OptionsCodes=question.questionOptions.split(parsingSymbol);
				var optionHtml;
				for (var i=0; i<OptionsCodes.length; i++){
					if (OptionsCodes[i] != null && typeof(OptionsCodes[i])!='undefined' && OptionsCodes[i]!='') {
						// replace the '
						OptionsCodes[i]=replaceAll(OptionsCodes[i],"\'","&#39;");
						
						var stringTest='<%=CtdbConstants.OTHER_OPTION_DISPLAY%>';
						if(OptionsCodes[i].indexOf(stringTest) == -1){	
							optionHtml += '<option value="'+OptionsCodes[i]+'">' + OptionsCodes[i] + "</option>";
						}			
					}
				}
				$('#options').html(optionHtml);
			}
			//image map
			if (question.questionType==<%=QuestionType.IMAGE_MAP.getValue()%>){
				$('#imageTypeFr').attr("src",'<s:property value="#webRoot"/>/question/editImageType.action?id='+editQuestionId);
			}
			//visual scale
			if (question.questionType==<%=QuestionType.VISUAL_SCALE.getValue()%>){
				var visualScale=question.visualScaleInfo.split(parsingSymbol); //get info from JSON object
				var left='';center='';right='';rangeStart='';rangeEnd='';scaleWidth='';scaleCursor='';
				for (var i=0;i<visualScale.length;i++) {
					left=visualScale[0];
					center=visualScale[1];
					right=visualScale[2];
					rangeStart=visualScale[3];
					rangeEnd=visualScale[4];
					scaleWidth=visualScale[5];
					scaleCursor=visualScale[6];
				}
				$('#rangeStart').attr("value",rangeStart);
				$('#rangeEnd').attr("value",rangeEnd);
				$('#scaleRangeMinimum').attr("value",rangeStart);
 				$('#scaleRangeMaximum').attr("value",rangeEnd);

				$('#scaleWidth').attr("value",scaleWidth);
				$('#sliderEx').attr("width", scaleWidth+"mm");
				$('#scaleCursor').attr("value",scaleCursor);
				$('#centerText').attr("value",center);
				$('#leftText').attr("value",left);
				$('#rightText').attr("value",right);
				$('#rightT').html(right);
				$('#leftT').html(left);
				$('#centerT').html(center);
			}
			// fill in the data: Attribute
			
			var attObj=question.attributeObject;
			//re-set all ribute's attCalDependent
			setCalDependent();
			setSkipRuleDependent();
			$('#dataSpring').attr("checked",attObj.dataSpring);
			
			//prepopulation
			$('#prepopulation').attr("checked",attObj.prepopulation);
			if (attObj.prepopulation==true) {
				$('#prepopulationValue').attr('disabled',false);
				$('#prepopulationValue').val(attObj.prepopulationValue);
			} else {
				$('#prepopulationValue').val('none');
				$('#prepopulationValue').attr('disabled',true);
			}
			
			//decimal precision
			$('#decimalPrecision').val(attObj.decimalPrecision);
			
			//unit conversion factor
			if (attObj.hasUnitConversionFactor==true) {
				$('#windowConv').html(attObj.unitConversionFactor);
			}
			
			if (attObj.required === 'true'|| attObj.required === true ) {
				$('#required').val("true");
			} else {
				$('#required').val("false");
			}
			$('#answerType').val(attObj.answerType);

			checkAnswerType();
			if (question.questionType==<%=QuestionType.RADIO.getValue()%> || question.questionType==<%=QuestionType.MULTI_SELECT.getValue()%> ||
			    question.questionType==<%=QuestionType.SELECT.getValue()%> || question.questionType==<%=QuestionType.CHECKBOX.getValue()%>) {
				$('#minCharacters, #maxCharacters').attr("disabled", true);
			}

			$('#conversionFactor').val(attObj.conversionFactor);

			$('#window').html(attObj.calculation);
			if(attObj.calDependent){
				$('#answerType').attr('disabled',true);
				$('#calculationEditButton').hide();
				$('#calculateMassege').show();
			} else {
				$('#calculateMassege').hide();	
			}
			$('#minCharacters').val(attObj.minCharacters);
			$('#maxCharacters').val(attObj.maxCharacters);	
			$('#rangeOperator').prop("selectedIndex", attObj.rangeOperator);
			$('#rangeValue1').val(attObj.rangeValue1);
			$('#rangeValue2').val(attObj.rangeValue2);	
			checkRangeOperator();
	
			if (attObj.skipRuleOperatorType==<%=Integer.MIN_VALUE%>) {
				$('#skipRuleOperatorType option:first').attr("selected", "selected");
			} else {
				$('#skipRuleOperatorType').prop("selectedIndex", 5-attObj.skipRuleOperatorType);
			}
			$('#skipRuleEquals').val(attObj.skipRuleEquals);
			showEquals();
			
			if (attObj.skipRuleType==<%=Integer.MIN_VALUE%>) {
				$('#skipRuleType option:first').attr("selected", "selected");
			} else {
				$('#skipRuleType').prop("selectedIndex", 3-attObj.skipRuleType);
			}
			
			showSectionQuestion();
			$('input[name=questionToSkip]').each(function(i){
				for(var i=0;i<attObj.questionsToSkip.length;i++){
					if(attObj.questionsToSkip[i]==$(this).val()){
						$(this).attr("checked", true);
					}
				}
			});
				
			$('#_et_toAddr').val(attObj.toEmailAddress);
			$('#_et_ccAddr').val(attObj.ccEmailAddress);
			$('#_et_subject').val(attObj.subject);
			$('#_et_body').val(attObj.body);
			showTriggerAnswers();
			$('#_et_answers option').each(function(){
				if(typeof(attObj.triggerAnswers)!='undefined'){
					for(var j=0;j<attObj.triggerAnswers.length;j++){
						var options=$(this).text().split("|");
						if(trim(options[0])==trim(attObj.triggerAnswers[j])){
							$(this).attr('selected',true);
						}
					}
				}
			});
		
			//Remove previous each loops for following SELECT elements			
			$('#align').val(attObj.align);
			$('#vAlign').val(attObj.vAlign);
			$('#color').val(attObj.color);
			$('#fontFace').val(attObj.fontFace);
			$('#fontSize').val(attObj.fontSize);
			$('#indent').val(attObj.indent);
			$('#horizontalDisplay').attr("checked", attObj.horizontalDisplay);
			$('#horizDisplayBreak').attr("checked", attObj.horizDisplayBreak);
		},
		
		editQuestion : function(questionDivId){
			var question = QuestionHandler.findQuestion(questionDivId);
			deAssociateFlag=false;
			editQuestionId=question.questionId;
			editQuestionType=question.questionType;
			editQuestionVersionNumber=question.questionVersionNumber;
			editQuestionDivId=questionDivId;
			editAttachedFormIds=question.attachedFormIds;
			//Data element
			editDataElementName=question.attributeObject.dataElementName;
			
			if ($('#activeSectionId').length < 1) {
				$("body").append('<input type="hidden" id="activeSectionId" />');
			}
			$('#activeSectionId').val(question.sectionId);
			

			// opens the form
			//$("#addQuestionFancyBoxA").trigger("click");
			$("#addEditQuestionFancyBox").dialog("open");
			
			Interface.switchQuestionToEdit();
			checkQuestionType(question.questionType);
			// fill in the data: Question
			QuestionHandler.populateQuestion(question);
			// finally, we still lock the question name field
			$('#questionName').attr('disabled',true);
			
			$('#activeQuestion').val(questionDivId);
			QuestionHandler.setAllowedQuestionTypeGroups(question); 
		},
		editTextblock : function(questionDivId) {
			var question = QuestionHandler.findQuestion(questionDivId);
			//questionInfoMode="edit";
			deAssociateFlag=false;
			editQuestionId=question.questionId;
			editQuestionType=question.questionType;
			editQuestionVersionNumber=question.questionVersionNumber;
			editQuestionDivId=questionDivId;
			
			// open the edit textblock dialog
			$("#addEditTextDialog").dialog("open");
			// text is changed in the editor using the oninit method of tinymce
		},
		// After a new question is added, now need to make sure its new attr is consistent with global flags set in very eary beginning for each of questions
		// e.g. hasDecimalPrecision, hasCalDependent, hasPrepopulation
		updateGlobalQuestionFlags: function(sourceQ){
			if(typeof(sourceQ) == "undefined" || sourceQ == null ||sourceQ.attributeObject == null){
				return; 
			}
			//alert("About to update sourceQ: \n" + JSON.stringify(sourceQ) + "\n\n");
			for(var i =0; i<questionsArray.length; i++){
				if(questionsArray[i].questionId == sourceQ.questionId){ // found matched question 
					if(!questionsArray[i].hasDecimalPrecision && sourceQ.attributeObject.decimalPrecision != -1){
						questionsArray[i].hasDecimalPrecision =true;
						//alert("finish updating DP Global flag for question ID " + sourceQ.questionId);
					}
					if(!questionsArray[i].prepopulation && sourceQ.attributeObject.prepopulation){
						questionsArray[i].prepopulation =true;
						//alert("finish updating PP Global flag for question ID " + sourceQ.questionId);
					}
				}
			}
			// update calculation questions if it is just being included in the last new or updated question
			if(typeof(sourceQ.attributeObject.questionsToCalculate) != "undefined" && sourceQ.attributeObject.questionsToCalculate.length >0){
				var calDependentQuestionIds= $.map(sourceQ.attributeObject.questionsToCalculate, function(secQuesId){ 
					//alert("now secquesid=" + secQuesId +"\n\n");
					return secQuesId.match(/Q_(\d+)\]/)[1];
					});
				//alert("QIds: length=" + calDependentQuestionIds.length + "   " + JSON.stringify(calDependentQuestionIds) +"\n\n" );
				for(var i =0; i<questionsArray.length; i++){
					if(!questionsArray[i].hasCalDependent && calDependentQuestionIds.length>0 
							&& $.inArray(questionsArray[i].questionId, calDependentQuestionIds) != -1){
						questionsArray[i].hasCalDependent =true;
						//alert("finish updating CD Global flag for question ID " + questionsArray[i].questionId);
					}
				}
			}
		},
		
		setAllowedQuestionTypeGroups: function (question){ 
			// allow all options as initialization
			$('#questionType, #questionType option').attr('disabled',false).show();
			if(
				question.questionType ==  <%=QuestionType.TEXTBOX.getValue()%> || 
				question.questionType ==  <%=QuestionType.TEXTAREA.getValue()%> || 
				question.questionType ==  <%=QuestionType.VISUAL_SCALE.getValue()%> || 
				question.questionType ==  <%=QuestionType.File.getValue()%> ||
				question.questionType ==  <%=QuestionType.IMAGE_MAP.getValue()%> ){
				$('#questionType').attr('disabled',true);
			} 
			else{ // disable a subset of options to limit question type interchange
				// STORY to disable question type switch between texbox and textarea
				if(question.questionType == <%=QuestionType.RADIO.getValue()%> ||
					question.questionType == <%=QuestionType.SELECT.getValue()%> ||
					question.questionType == <%=QuestionType.MULTI_SELECT.getValue()%> ||
					question.questionType == <%=QuestionType.CHECKBOX.getValue()%> ){
					 $("select#questionType").find("option").filter(function(index) {
						    return $(this).val() != <%=QuestionType.RADIO.getValue()%> && $(this).val() != <%=QuestionType.SELECT.getValue()%> &&
						       		$(this).val() != <%=QuestionType.MULTI_SELECT.getValue()%> && $(this).val() != <%=QuestionType.CHECKBOX.getValue()%>;
					}).attr("disabled", true).hide();
				}
			}
		},
		/**
		 * sets the section ID and order for the questions at save time
		 */
		setQuestionSectionId : function(jqueryQuestionSet) {
			var lastSectionId = null;
			var currentPosition = 1;
			jqueryQuestionSet.each(function() {
				var index = QuestionHandler.findQuestionIndex($(this).attr("id"));
				var sectionId = $(this).parents(".section").attr("id");
				
				// in the event of edit, we need to remove "S_" from EXISTING sections
				var sectionIndex = SectionHandler.findSectionIndex(sectionId);
				
				if (lastSectionId != sectionId) {
					currentPosition = 1;
					lastSectionId = sectionId;
				} else {
					currentPosition++;
				}
				var repeatedQuestions = QuestionHandler.findAllRepeatedQuestions(questionsArray[index]);
				questionsArray[index].questionOrder = currentPosition;
				questionsArray[index].sectionId = (sectionsArray[sectionIndex].existingSection) ? String(questionsArray[index].sectionId).replace("S_", "") : sectionId;
				questionsArray[index].questionId = (sectionsArray[sectionIndex].existingSection) ? String(questionsArray[index].questionId).replace("S_", "") : String(questionsArray[index].questionId);
				
				// in the case of repeatable sections, propagate the questionOrder to the other sections
				for (var i=0; i < repeatedQuestions.length; i++) {
					repeatedQuestions[i].questionOrder = currentPosition;
					repeatedQuestions[i].sectionId = (SectionHandler.findSection(repeatedQuestions[i].sectionId).existingSection) ? String(repeatedQuestions[i].sectionId).replace("S_", "") : repeatedQuestions[i].sectionId;
					repeatedQuestions[i].questionId = questionsArray[index].questionId;
				}
			});
		},
		
		findAllRepeatedQuestions : function(questionObj) {
			// this is the parent (repeatable?) section
			var section = sectionsArray[SectionHandler.findSectionIndex(SectionHandler.convertToHTMLId(questionObj.sectionId))];
			if (!section.isRepeatable) {
				// not repeatable, therefore no children
				return new Array();
			}
			
			var repeatedSections = SectionHandler.findAllRepeatableChildren(section.id);
			// now look through all questions and find those with one of those section IDs and this question ID
			var repeatedQuestions = new Array();
			for (var i = 0; i < questionsArray.length; i++) {
				for (var j = 0; j < repeatedSections.length; j++) {
					if (questionsArray[i].questionId == questionObj.questionId && 
							SectionHandler.convertToHTMLId(questionsArray[i].sectionId) == SectionHandler.convertToHTMLId(repeatedSections[j].id)) {
						repeatedQuestions.push(questionsArray[i]);
					}
				}
			}
			return repeatedQuestions;
		}
};


var SectionHandler = {
	sectionListRef : null,
	findSection : function(sectionId) {
		for (var i = 0; i < sectionsArray.length; i++) {
			if (sectionsArray[i].id == sectionId || SectionHandler.convertToHTMLId(sectionId) == sectionsArray[i].id) {
				return sectionsArray[i];
			}
		}
		return null;
	},
	
	/**
	 * Finds the index of a section, given its id, within the section array
	 * @param sectionId the HTML id of the section
	 * @return int index or null if not found
	 */
	findSectionIndex : function(sectionId) {
		for (var i = 0; i < sectionsArray.length; i++) {
			if (sectionsArray[i].id == sectionId) {
				return i;
			}
		}
		return null;
	},
	
	convertToHTMLId : function(sectionId) {
		if (String(sectionId).indexOf("S_") > -1) {
			return sectionId;
		}
		return "S_" + sectionId;
	},
	
	addSection : function(sectionObject) {
		sectionsArray.push(sectionObject);
	},
	
	/**
	 * removes a single section from the page completely
	 * @param sectionId the html ID of the section to remove
	 */
	removeSection : function(sectionId) {
		// NOTE: why in the world does this method not include all of the
		// operations involved in removing a section such as adding the section
		// to the existingsectionIdsToDeleteArray or removing questions...?
		sectionId = SectionHandler.convertToHTMLId(sectionId);
		var index = this.findSectionIndex(sectionId);
		
		// activate either the previous section (default) or next section
		// and the last or first question in that section (respectively)
		if ($("#"+sectionId).length > 0) {
			var otherSection = $("#"+sectionId).prev(".section");
			if (otherSection.length > 0) {
				changeActiveSection(otherSection);
				if (otherSection.children(".question").length > 0) {
					changeActiveQuestion(otherSection.children(".question").last());
				}
			} 
			else {
				otherSection = $("#"+sectionId).next(".section");
				changeActiveSection(otherSection);
				if (otherSection.children(".question").length > 0) {
					changeActiveQuestion(otherSection.children(".question").first());
				}
			}
		}
		
		// remove the section
		if (index != null) {
			DynamicElements.remove(sectionId);
		}
		sectionsArray.splice(index,1);
		DynamicElements.enforceColumnEqualWidth(null, {item : $("#"+sectionId)});
	},
	
	/**
	 * Determines if the section being passed needs to have its question's
	 * data elements changed to "none" (IE, association removed)
	 */
	needDeleteDEs : function(section, newlyRepeatable, newSelectGroupName) {
		if (section.isRepeatable && newlyRepeatable) {
			// is still repeatable, must check if rgroup changed
			if (section.repeatableGroupName != newSelectGroupName) {
				return true;
			}
		} else if (section.isRepeatable && !newlyRepeatable) {
			return true;
		} else if (!section.isRepeatable && newlyRepeatable) {
			return true;
		}
		
		// in all other cases, don't delete de's
		return false;
	},
	
	
	editSection : function(sectionId) {
		// find the section in the list
		var section = SectionHandler.findSection(sectionId);
		if (section == null) {
			alert("The section you have specified could not be found");
		} else {
			// opens the form
			$("#addEditSectionFancyBox").dialog("open");
			
			// register a click event on the repeatable checkbox to empty the min and max fields if cleared
			$("#sectionRepeatbale").unbind("click");
			$("#sectionRepeatbale").click(function() {
				if (!$(this).is(":checked")) {
					$('#initialRepeated, #maximumRepeated').val("");
				}
			});
			
			// fill in the data
			$('#sectionName').val(section.name);
			$('#sectionDescription').val(section.description);
			if (section.isCollapsable){
				$('#sectionCollapsible').attr("checked", true);
			}
			
			if (section.ifHasResponseImage) {
				$('#sectionImage').attr("checked", true);
			}
			
			// set the ID
			if ($('#activeSectionId').length < 1) {
				$("body").append('<input type="hidden" id="activeSectionId" />');
			}
			$('#activeSectionId').val(section.id);
			
			if (section.isRepeatable) {
				$('#sectionRepeatbale').attr("checked", true);
				$('#initialRepeated').val(section.initRepeatedSecs);
				$('#maximumRepeated').val(section.maxRepeatedSecs);
				populateRepeatableGroupNameSelect();
				$('#repeatableGroupSelect').val(section.repeatableGroupName)
				$('#initRepeatedTR, #maxRepeatedTR, #repeatableGroupTR').show();
				populateRepeatableGroupInfoDialog();
			}else {
				$('#sectionRepeatbale').attr("checked", false);
				$('#initialRepeated, #maximumRepeated').val("");
				$('#repeatableGroupSelect').html("");
				$('#initRepeatedTR, #maxRepeatedTR, #repeatableGroupTR').hide();
			}
			
			// set the form to "edit" rather than "create"
			Interface.switchSectionFormToEdit();
		}
	},
	
	reorderSectionAt : function(oldIndex, newIndex) {
	},
	
	editSectionCallback : function() {
	$("#errorContainer2").html("");
		// get data
		var name = $('#sectionName').val();
		if (name == "") {
			var errorString = "<s:text name="errors.text"/><b>Section Name is required.</b>";
			$.ibisMessaging("primary", "error", errorString, {container: "#errorContainer2"});
    		$("#errorContainer2").show();
			return false;
		}
		
		var description = $('#sectionDescription').val();
		var isCollapsable = $('#sectionCollapsible').attr("checked");
		var hasResponseImage = $('#sectionImage').attr("checked");
		var id = $('.activeSection').attr("id");
		var isRepeatable;
		var initRepeatedSecs = -1;
		var maxRepeatedSecs = -1;
		var repeatableGroupName = "None";

		if (isCollapsable == "checked") {
			isCollapsable = true;
		}
		
		if (hasResponseImage == "checked") {
			hasResponseImage = true;
		}
		
		if ($("#sectionRepeatbale").is(":checked") ) {
			isRepeatable = true;

			initRepeatedSecs = trim($('#initialRepeated').val());
			maxRepeatedSecs = trim($('#maximumRepeated').val());
			repeatableGroupName = $('#repeatableGroupSelect').val();

			if(isInt(initRepeatedSecs) && isInt(maxRepeatedSecs)) {
				initRepeatedSecs = Number(initRepeatedSecs);
				maxRepeatedSecs = Number(maxRepeatedSecs);
				if(initRepeatedSecs < 0 || maxRepeatedSecs < 0) {
					$.ibisMessaging(
							"primary", 
							"error", 
							"<s:text name="errors.text"/><b>Initial Number or Maximum Number can not be less than 0</b>",
							{
								container: "#errorContainer2"
							});
					$("#errorContainer2").show();
					return false;
				}
				if(initRepeatedSecs > maxRepeatedSecs) {
					$.ibisMessaging(
							"primary", 
							"error", 
							"<s:text name="errors.text"/><b>Maximum Number of times viewed can not be less than the Initial Number of times viewed.</b>",
							{
								container: "#errorContainer2"
							});
					$("#errorContainer2").show();
					return false;
				}
				if(initRepeatedSecs == 0) {
					$.ibisMessaging(
							"primary", 
							"error", 
							"<s:text name="errors.text"/><b>Initial Number of times viewed must be greater than 0.</b>",
							{
								container: "#errorContainer2"
							});
					$("#errorContainer2").show();
					return false;
				}
				if(maxRepeatedSecs > 30) {
					$.ibisMessaging(
							"primary", 
							"error", 
							"<s:text name="errors.text"/><b>Maximum Number of times viewed must not be greater than 30.</b>",
							{
								container: "#errorContainer2"
							});
					$("#errorContainer2").show();
					return false;
				}
				
				//now check to see if repeatable group is associated and that threhold is correct
				if(repeatableGroupName != "None") {
					var rgThreshold;
					for(var i=0;i<DSObj.repeatableGroupsArr.length;i++){
						var rg = DSObj.repeatableGroupsArr[i];
						var rgName = rg.repeatableGroupName;
						
						if(repeatableGroupName == rgName) {
							rgThreshold = rg.repeatableGroupThreshold;
							break;
						}
					}

					//threshold of 0 means infinity....so check for that
					//also...threshold can be one for these truly repeatable secs (repeats more than once) if it is set for MORETHAN.  (in other words...MORE THAN 1)
					if(rgThreshold != 0 && rgThreshold != 1) {
						if(maxRepeatedSecs > rgThreshold) {
							$.ibisMessaging(
									"primary", 
									"error", 
									"<s:text name="errors.text"/><b>Maximum Number of times viewed can not be greater than Repeatable Group threshold of " + rgThreshold + "</b>",
									{
										container: "#errorContainer2"
									});
							$("#errorContainer2").show();
							return false;
						}else if(maxRepeatedSecs < rgThreshold) {
							//show warning
							alert("Warning: Maximum Number of times viewed is less than the Repeatable Group threshold of " + rgThreshold);
						}
					}
				}
			} else {
				$.ibisMessaging(
						"primary", 
						"error", 
						"<s:text name="errors.text"/><b>Valid numbers are required for Initial and Maximum Number of times viewed.</b>",
						{
							container: "#errorContainer2"
						});
				$("#errorContainer2").show();
				return false;
			}
			
			
			
		}else {
			isRepeatable = false;
		}
		
		var editSection = sectionsArray[SectionHandler.findSectionIndex(id)];
		// dealing with data elements...do we have to clear them?
		if (SectionHandler.needDeleteDEs(editSection, $("#sectionRepeatbale").is(":checked"), repeatableGroupName)) {
			// for all questions in this section and children sections, change dataElementName
			var firstSectionQuestions = editSection.getQuestions();
			var questions = [];
			$.merge(questions, firstSectionQuestions);
			for (var i = 0; i < firstSectionQuestions.length; i++) {
				// clear the data element label for each visible question
				$("#"+firstSectionQuestions[i].newQuestionDivId).find(".questionDataElementName").text("none");
				// merge the newly found repeated questions into the larger list
				$.merge(questions, QuestionHandler.findAllRepeatedQuestions(firstSectionQuestions[i]));
			}
			for (var i = 0; i < questions.length; i++) {
				questions[i].attributeObject.dataElementName = "none";
			}
		}
		
		Interface.switchSectionFormToAdd();
		
		// update object on page
		$("#"+ id + " .sectionNameContainer").text(name);
		$("#"+ id + " .sectionDescriptionContainer").text(description);

		var index = SectionHandler.findSectionIndex(id);
		// did we change the repeatable max?  If so, we need to update the page, ahh!
		if (maxRepeatedSecs != sectionsArray[index].maxRepeatedSecs ||initRepeatedSecs != sectionsArray[index].initRepeatedSecs || repeatableGroupName != sectionsArray[index].repeatableGroupName) {
			// first, look at whether we switched from repeatable to not or vice-versa
			// the new max is 1 or repeatable is turned off from on
			if (isRepeatable == false && sectionsArray[index].isRepeatable == true) {
				// this is no longer a repeated section, remove the extras and change up the properties
				var repeatedChildren = SectionHandler.findAllRepeatableChildren(sectionsArray[index].id);
				for (var i = 0; i < repeatedChildren.length; i++) {
					var removeThisSection = repeatedChildren[i];
					var questions = removeThisSection.getQuestions();
					for (var j = 0; j < questions.length; j++) {
						QuestionHandler.removeQuestion(questions[j].newQuestionDivId, true);
					}
					
					if (removeThisSection.existingSection) {
						existingSectionIdsToDeleteArray.push(removeThisSection.id);
					}
					SectionHandler.removeSection(removeThisSection.id);
				}
				
				// change the properties of the section itself, namely: remove the header
				$("#repeatedHeader_"+sectionsArray[index].id).remove();
				isRepeatable = false;
				maxRepeatedSecs = -1;
				initRepeatedSecs = -1;
				repeatableGroupName = "None";
				
			}
			else if (isRepeatable && !sectionsArray[index].isRepeatable) {
				// newly repeatable
				// create this many new children
				var difference = Number(maxRepeatedSecs) - 1;
				var newChildren = new Array();
				var rSection = sectionsArray[index];
				for(var i=0;i<difference;i++) {
					var childRepeatableSection = null;
					var childRepeatableSectionId = SectionHandler.convertToHTMLId(bogusSectionId);
					bogusSectionId--;
					childRepeatableSection = new SectionObj(childRepeatableSectionId,rSection.name,rSection.description,rSection.isCollapsable,rSection.ifHasResponseImage,rSection.isRepeatable,rSection.initRepeatedSecs,maxRepeatedSecs,rSection.id,rSection.repeatableGroupName);
					SectionHandler.addSection(childRepeatableSection);
					newChildren.push(childRepeatableSection);
				}
				
				var rQuestions = rSection.getQuestions();
				for (var i = 0; i < newChildren.length; i++) {
					// add all child questions to the sections...this will be ugly
					var repeatedSectionId = SectionHandler.convertToHTMLId(newChildren[i].id);
					for (var j = 0; j < rQuestions.length; j++) {
						// replicate the question for this section
						var rQues = owl.deepCopy(rQuestions[j]);
						var divId = repeatedSectionId + "_" + rQues.questionId + "_" + rQues.questionVersionNumber;
						var ques = new QuestionObj(rQues.questionId,rQues.questionVersionLetter,rQues.questionVersionNumber,rQues.questionName,rQues.questionText,rQues.descriptionUp,rQues.descriptionDown,rQues.questionType,rQues.questionOptions,rQues.imageOption,rQues.imageFileName,rQues.visualScaleInfo,divId,repeatedSectionId,rQues.attributeObject,rQues.graphicNames,rQues.defaultValue,rQues.unansweredValue,rQues.associatedGroupIds,rQues.includeOther,rQues.attachedFormIds, "");
						questionsArray.push(ques);
					}
				}
				setCalDependent();  // for edit attribute, when the question was choosed to be calculation element for other question, it can't change the answer type anymore.
				setSkipRuleDependent();

				// update the section itself: add the new header information
				var newSectionDiv = "<td id=\"repeatedHeader_"+rSection.id+"\" align=\"center\">" +
						"<b>Repeatable Group:</b> " + rSection.repeatableGroupName + "&nbsp;&nbsp;&nbsp;<b>Initial:</b> " + rSection.initRepeatedSecs + "&nbsp;&nbsp;&nbsp;<b>Max:</b> " + rSection.maxRepeatedSecs +
						"</td>";
				$("#"+rSection.id+" .sectionHeader1").after(newSectionDiv);
			}
			else {
				// less than?  please?  this is much easier than greater
				if (maxRepeatedSecs < sectionsArray[index].maxRepeatedSecs) {
					// remove this many repeated sections
					var difference = Number(sectionsArray[index].maxRepeatedSecs) - Number(maxRepeatedSecs);
					var repeatedChildren = SectionHandler.findAllRepeatableChildren(sectionsArray[index].id);
					// remove difference number of sections from the end of that list.  do NOT remove the parent, ever
					for (var i = 0; i < difference; i++) {
						// remove the questions
						var removeThisSection = repeatedChildren.pop();
						var questions = removeThisSection.getQuestions();
						for (var j = 0; j < questions.length; j++) {
							QuestionHandler.removeQuestion(questions[j].newQuestionDivId, true);
						}
						
						if (removeThisSection.existingSection) {
							existingSectionIdsToDeleteArray.push(removeThisSection.id);
						}
						SectionHandler.removeSection(removeThisSection.id);
					}
				}
				else if (maxRepeatedSecs > sectionsArray[index].maxRepeatedSecs) {
					// create this many new children
					var difference = Number(maxRepeatedSecs) - Number(sectionsArray[index].maxRepeatedSecs);
					var newChildren = new Array();
					var childRepeatableSection;
					var rSection = sectionsArray[index];
					for(var i=0;i<difference;i++) {
						childRepeatableSection = null;
						var childRepeatableSectionId = SectionHandler.convertToHTMLId(bogusSectionId);
						bogusSectionId--;
						childRepeatableSection = new SectionObj(childRepeatableSectionId,rSection.name,rSection.description,rSection.isCollapsable,rSection.ifHasResponseImage,rSection.isRepeatable,rSection.initRepeatedSecs,maxRepeatedSecs,rSection.id,rSection.repeatableGroupName);
						SectionHandler.addSection(childRepeatableSection);
						newChildren.push(childRepeatableSection);
					}
					
					var rQuestions = rSection.getQuestions();
					for (var i = 0; i < newChildren.length; i++) {
						// add all child questions to the sections...this will be ugly
						var repeatedSectionId = SectionHandler.convertToHTMLId(newChildren[i].id);
						for (var j = 0; j < rQuestions.length; j++) {
							// replicate the question for this section
							var rQues = owl.deepCopy(rQuestions[j]);
							var divId = repeatedSectionId + "_" + rQues.questionId + "_" + rQues.questionVersionNumber;
							var ques = new QuestionObj(rQues.questionId,rQues.questionVersionLetter,rQues.questionVersionNumber,rQues.questionName,rQues.questionText,rQues.descriptionUp,rQues.descriptionDown,rQues.questionType,rQues.questionOptions,rQues.imageOption,rQues.imageFileName,rQues.visualScaleInfo,divId,repeatedSectionId,rQues.attributeObject,rQues.graphicNames,rQues.defaultValue,rQues.unansweredValue,rQues.associatedGroupIds,rQues.includeOther,rQues.attachedFormIds, "");
							questionsArray.push(ques);
						}
					}
					setCalDependent();  // for edit attribute, when the question was choosed to be calculation element for other question, it can't change the answer type anymore.
					setSkipRuleDependent();
				}
			}

			$("#repeatedHeader_"+sectionsArray[index].id).html("<b>Repeatable Group:</b> " + repeatableGroupName + "&nbsp;&nbsp;&nbsp;<b>Initial:</b> " + initRepeatedSecs + "&nbsp;&nbsp;&nbsp;<b>Max:</b>" + maxRepeatedSecs);
		}
		
		
		// update js object(s)
		var allSectionsInRepeat = SectionHandler.findAllRepeatableChildren(sectionsArray[index].id);
		allSectionsInRepeat.push(sectionsArray[index]);
		
		for(var i = 0; i < allSectionsInRepeat.length; i++) {
			allSectionsInRepeat[i].name = name;
			allSectionsInRepeat[i].description = description;
			allSectionsInRepeat[i].isCollapsable = (typeof isCollapsable == "undefined") ? false : isCollapsable;
			allSectionsInRepeat[i].ifHasResponseImage = (typeof hasResponseImage == "undefined") ? false : hasResponseImage;
			allSectionsInRepeat[i].isRepeatable = isRepeatable;
			
			allSectionsInRepeat[i].initRepeatedSecs = initRepeatedSecs;
			allSectionsInRepeat[i].maxRepeatedSecs = maxRepeatedSecs;
			allSectionsInRepeat[i].repeatableGroupName = repeatableGroupName;
		}
		$("#addEditSectionFancyBox").dialog("close");
		
		DynamicElements.enforceSectionRows(null, {item: $("div.activeSection")});
	},
	
	/**
	 * Sets up the row/column definitions for each section.  This is called
	 * on save and processes through each section in the list.
	 * 
	 * I don't like the organization of this method.  Let's slim it down
	 */
	setRowsCols : function() {
		var rowNum = 1;
		var colNum = 1;
		var matchedSections = $(".section");
		// don't use each here because that runs concurrently(sorta...)
		for (var j = 0; j < matchedSections.length; j++) {
			var that = matchedSections.eq(j); // as opposed to $(this)...get it?
			var previous = that.prev(".section");
			var thisId = that.attr("id");
			var thisSect = sectionsArray[SectionHandler.findSectionIndex(thisId)];

			// if this section is a repeatable parent, calculate all of this section's children too
			// note, repeatable children would not be in the matchedSections list because they're not on the page
			if (thisSect.isRepeatable) {
				// take care of this section first: repeatable sections are always on their own row
				if (previous.length < 1) {
					SectionHandler.setRowCol(thisSect, 1, 1);
				}
				else {
					rowNum++;
					colNum = 1;
					SectionHandler.setRowCol(thisSect, rowNum, colNum);
				}
				// now the kids
				var repeatableChildren = SectionHandler.findAllRepeatableChildren(thisSect.id);
				for (var i = 0; i < repeatableChildren.length; i++) {
					rowNum++;
					SectionHandler.setRowCol(repeatableChildren[i], rowNum, colNum);
				}
			}
			else {
				if (previous.length < 1) {
					SectionHandler.setRowCol(thisSect, 1, 1);
				} else {
					// if this is not a repeatable section, process like normal
					if (previous.offset().top == that.offset().top) {
						// this elemen is on the same row as the previous element
						colNum++;
						// don't change row number
					}
					else {
						// this element is on a different row from the previous element
						rowNum++;
						colNum = 1;
					}
					SectionHandler.setRowCol(thisSect, rowNum, colNum);
				}
			}
		}
		// the above SHOULD catch all sections
	},
	
	/**
	 * Sets the row and col numbers for the given section Object
	 */
	setRowCol : function(sectionObj, row, col) {
		sectionObj.row = row;
		sectionObj.col = col;
	},
	
	/**
	 * Finds all sections with the repeatableParent property equal to the passed ID
	 * @return SectionObj[] array of section Objects children of repeatableParentId section
	 */
	findAllRepeatableChildren : function(repeatableParentId) {
		var sections = new Array();
		for (var i = 0; i < sectionsArray.length; i++) {
			// do not add S_ here because that could match S_-1 to nonrepeated -1 parent IDs
			if (sectionsArray[i].repeatedSectionParent == repeatableParentId) {
				sections.push(sectionsArray[i]);
			}
		}
		return sections;
	}
};

var Interface = {
		switchSectionFormToAdd : function() {
			$("#addSectionEditButton").hide();
			$("#addSectionAddButton").show();
			$("#sectionImage").attr("disabled", false);
			$("#addEditSectionFancyBox").dialog("option", "title", "<s:text name="form.forms.formInfo.addSectionDisplay" />");
		},
		
		switchSectionFormToEdit : function() {
			$("#addSectionEditButton").show();
			$("#addSectionAddButton").hide();
			$("#sectionImage").attr("disabled", true);
			$("#addEditSectionFancyBox").dialog("option", "title", "<s:text name="form.forms.formInfo.editSectionDisplay" />");
		},
		
		switchQuestionToAdd:function(){
			$("#addEditQuestionFancyBox").dialog("option", "title", "<s:text name="form.forms.formInfo.addQuestionDisplay" />");
			$("#qSwitchTable").show();
			$("#sQ").attr('checked', true);
			$("#cQ").attr('checked', false);
			checkSwitch(); // to make the questionInfoMode='search'
			$("#forceVersion").hide();
			$("#calculationEditButton").attr('value',"Add Calculation Rule");
			$('#answerType').attr('disabled',false);

			// show the data element table
			showDataElementTable();
			if($("#dataElementH").next("div").css('display')=='none'){
				$("#dataElementH").click();
				$("#dataElementH").children('span').html('[-]');
			} else {
				$("#dataElementH").children('span').html('[-]');
			}
			controlWorkFlow(1);
			// Bottuns
			$('#searchAddBtu').hide(); 
			$('#createAddBtu').hide();
			$('#questionEditButton').hide();
			$('#searchAddBtuStep3').hide();
			$('#createAddBtuStep3').hide();
			$('#questionEditButtonStep3').hide();
			$('#questionEditButtonInSearch').hide();
			
		},
		
		switchQuestionToEdit:function(){
			$("#addEditQuestionFancyBox").dialog("option", "title", "<s:text name="form.forms.formInfo.editQuestionDisplay" />");
			$("#qSwitchTable").hide();
			$("#sQ").attr('checked', false);
			$("#cQ").attr('checked', true);
			checkSwitch();// to make the questionInfoMode='add'
			questionInfoMode='edit';//but it's edit, so do this...
			
			$("#calculationEditButton").attr('value',"Edit Calculation Rule");
			if(editQuestionType!='9'){
				$("#forceVersion").hide(); // we don't need this now 
			}else{
				$("#forceVersion").hide();
			}	
			// show data element table
			showDataElementTable();
			if($("#dataElementH").next("div").css('display')=='none'){
				$("#dataElementH").click();
				$("#dataElementH").children('span').html('[-]');
			}else{
				$("#dataElementH").children('span').html('[-]');
			}
			
			// make all work flow enable
			$('#step2Q').removeClass();
			$('#step2Q').addClass('focus-link');
			$('#step2Q').attr('onclick','controlWorkFlow(2)');
			
			$('#step3QA').removeClass();
			$('#step3QA').addClass('focus-link');
			$('#step3QA').attr('onclick','controlWorkFlow(3)');
			
			controlWorkFlow(1);
			
			// Bottuns
			$('#searchAddBtu').hide();
			$('#createAddBtu').hide();
			$('#questionEditButton').show();
			$('#searchAddBtuStep3').hide();
			$('#createAddBtuStep3').hide();
			$('#questionEditButtonStep3').show();
			$('#questionEditButtonInSearch').show();
		},
		
		showQuestionInfoInSearch:function(){
			$('#questionType').attr('disabled',true);
			$('#questionName').attr('disabled',true);
			$('#searchAddBtu').show();
			$('#createAddBtu').hide();
			// show question Info
			$('#addEditQuestion').show();
		}
};

var overlaps = (function () {
    function getPositions( elem ) {
        var pos, width, height;
        pos = $( elem ).position();
        width = $( elem ).width();
        height = $( elem ).height();
        return [ [ pos.left, pos.left + width ], [ pos.top, pos.top + height ] ];
    }

    function comparePositions( p1, p2 ) {
        var r1, r2;
        r1 = p1[0] < p2[0] ? p1 : p2;
        r2 = p1[0] < p2[0] ? p2 : p1;
        return r1[1] > r2[0] || r1[0] === r2[0];
    }

    return function ( a, b ) {
        var pos1 = getPositions( a ),
            pos2 = getPositions( b );
        return comparePositions( pos1[0], pos2[0] ) && comparePositions( pos1[1], pos2[1] );
    };
})();

var DynamicElements = {
	watchSectionTimer : null,
	intersects : null,
		
	remove : function(elementId) {
		$('#'+elementId).remove();
	},
	
	/**
	 *
	 * @return all sections in the row.  If includeSelected is FALSE, this will NOT include the current section
	 */
	getAllSectionsInRow : function(sectionJqueryObject, tolerance, includeSelected) {
		// if this is a repeatable section, just return it by itself and move on
		var sectObj = SectionHandler.findSection(sectionJqueryObject.attr("id"));
		if (sectObj != null && sectObj.isRepeatable) {
			// we're returning jquery objects here, not arrays
			return sectionJqueryObject;
		}
		
		if (typeof tolerance == "undefined") {
			tolerance = 0;
		}
		if (typeof includeSelected == "undefined") {
			includeSelected = false;
		}
		var rowArray = $();
		for (var i = 0; i < sectionsArray.length; i++) {
			var otherColElement = $("#"+sectionsArray[i].id);
			// if otherColElement is a repeatable child, it would not appear on the page
			if (otherColElement.length > 0) {
				// don't even check repeatable sections.  They should NOT be on the same row as another
				sectObj = SectionHandler.findSection(otherColElement.attr("id"));
				if (!sectObj.isRepeatable) {
					try {
						if (sectionsArray[i].id != sectionJqueryObject.attr("id") || includeSelected) {
							var currentTop = sectionJqueryObject.position().top;
							var otherTop = otherColElement.position().top;
							
							if (otherTop <= (currentTop + tolerance) && otherTop >= (currentTop - tolerance)) {
								// because of some oddities in jquery, combine the results in a manner consistent with
								// http://stackoverflow.com/questions/323955/how-to-combine-two-jquery-results
								if (rowArray.length < 1) {
									rowArray = otherColElement;
								} else {
									rowArray = rowArray.add(otherColElement.get(0));
								}
							}
						}
					} catch(e) {
						if (typeof console != "undefined") {
							console.error("retrieving sections returned error");
						}
					}
				}
			}
		}
		return rowArray;
	},
	
	seededGetAllSectionsInRow : function(sectionJqueryObject) {
		// if this is a repeatable section, just return it.  Repeatables are forever alone
		var sectObj = SectionHandler.findSection(sectionJqueryObject.attr("id"));
		if (sectObj != null && sectObj.isRepeatable == true) {
			// we're returning jquery objects here, not arrays
			return sectionJqueryObject;
		}
		
		var rowArray = $();
		for (var i = 0; i < sectionsArray.length; i++) {
			var otherColElement = $("#"+sectionsArray[i].id);
			// if otherColElement is a repeatable child, it won't be on the page
			if (otherColElement.length > 0) {
				try {
					if (sectionJqueryObject.position().top == otherColElement.position().top) {
						// because of some oddities in jquery, combine the results in a manner consistent with
						// http://stackoverflow.com/questions/323955/how-to-combine-two-jquery-results
						if (rowArray.length < 1) {
							rowArray = otherColElement;
						} else {
							rowArray = rowArray.add(otherColElement.get(0));
						}
					}
				} catch(e) {
					if (typeof console != "undefined") {
						console.error("retrieving sections returned error");
					}
				}
			}
		}
		return rowArray;
	},
	
	enforceColumnEqualWidth : function(event, ui) {
		// re-calculate widths for ALL sections (this is process intensive but unavoidable from what I can see right now)
		for (var i = 0; i < sectionsArray.length; i++) {
			// if the section being examined is a repeatable child, it won't be on the page
			if ($("#"+sectionsArray[i].id).length > 0) {
				var rowArray = DynamicElements.getAllSectionsInRow($("#"+sectionsArray[i].id), 20, true);
				if (rowArray.length == 1 ) {
					eachWidth = containerWidth;
				} else {
					// 5*2 px for margins, 1*2px for borders, divide
					var totalWidth = containerWidth;
					var eachWidth = ((totalWidth - 2 - (12 * rowArray.length)) / rowArray.length);
				}
				rowArray.each(function() {
					$(this).css("width", eachWidth+"px");
				});
			}
		}
		// with the placeholder the correct width, they appear beside each other
		$(ui.placeholder).width($(ui.item).width());
	},
	
	startWatchSectionMove : function(event, ui) {
		DynamicElements.continueTimeout(event, ui);
		
	},
	
	continueTimeout: function(event, ui) {
		DynamicElements.watchSectionTimer = setTimeout(function() {DynamicElements.watchSectionMove(event, ui);}, 20);
	},
	
	// fired off every 200ms
	// jp note: this whole process needs to be documented.  I am trying to remember how in the world it works and just can't get it yet
	watchSectionMove : function(event, ui) {
		var foundHover = false;
		$(".section").each(function() {
			if (overlaps($(ui.item), $(this))) {
				foundHover = true;
				if (!$(ui.item).is(DynamicElements.intersects)) {
					// this is equivalent to a hover start
					// jp: I forget what this does.  Test when I get time
					DynamicElements.intersects = $(this);
					// what is rowArray used for here?  I don't remember it being global
					var rowArray = DynamicElements.seededGetAllSectionsInRow($(this));
					rowArray.add($(ui.item));
					
				}
			}
		});
		if (!foundHover && DynamicElements.intersects != null) {
			// this is equivalent to a hover off
			DynamicElements.intersects = null;
			// resize ALL elements to correct sizing
			enforceColumnEqualWidth(event, ui);
			$(".section").each(function() {
				DynamicElements.enforceSectionRows(null, {item: $(this)});
			});
		}
		
		DynamicElements.enforceColumnEqualWidth(event, ui);
		if (DynamicElements.watchSectionTimer != null) {
			DynamicElements.continueTimeout(event, ui);
		}
	},
	
	stopWatchSectionMove : function(event, ui) {
		clearTimeout(DynamicElements.watchSectionTimer);
		DynamicElements.watchSectionTimer = null;
		DynamicElements.enforceSectionRows(event, ui);
	},
	
	enforceSectionRows : function(event, ui) {
		var target = $(ui.item);
		var rowArray = new Array();
		// get all sections in row
		for (var i = 0; i < sectionsArray.length; i++) {
			// only do the below for repeatable parents and regular sections
			if (sectionsArray[i].repeatedSectionParent == -1) {
				var otherColElement = $("#"+sectionsArray[i].id);
				if (otherColElement.offset().top == target.offset().top) {
					rowArray[rowArray.length] = otherColElement;
				}
			}
		}
		
		// clear the heights of the sections in the row
		for (var i = 0; i < rowArray.length; i++) {
			rowArray[i].css("height", "auto");
		}
		
		// get the max height of sections in this row
		var maxHeight = 0;
		for (var i = 0; i < rowArray.length; i++) {
			if (rowArray[i].height() > maxHeight) {
				maxHeight = rowArray[i].height();
			}
		}
		
		// set the sections to all the same height if the target is the largest
		for (var i = 0; i < rowArray.length; i++) {
			rowArray[i].height(maxHeight);
		}
	}
};


//set up the fancy boxes and trigger the add form info fancy box on page load
$(document).ready(function() {
	$("#addEditFormInfoFancyBox").dialog({
		autoOpen: false,
		width: 850,
		modal: true,
		title: "Form Information",
		close : function() {
			if (!proceedToBuildForm) {
				var url = "<s:property value="#webRoot"/>/form/formHome.action";
				redirectWithReferrer(url);
			}
		},
		open : function() {
			var fm = "<%= formMode %>";
			if (fm == "edit") {
				$("h3#fidH3").click();
				$("h3#fidH3").click();
			} else {
				$("h3#fdsH3").click();
				$("h3#fdsH3").click();
			}
		}
	});
	
	$("#addEditSectionFancyBox").dialog({
		autoOpen: false,
		width: 790,
		modal: true,
		title: "Add Section",
		close : function() {
			$('#sectionName').val("");
			$('#sectionDescription').val("");
			$('#sectionCollapsible').attr("checked", false);
			$('#sectionImage').attr("checked", false);
			$('#sectionRepeatbale').attr("checked",false);
			$('#initRepeatedTR').attr("style","display:none");
			$('#maxRepeatedTR').attr("style","display:none");
			$('#repeatableGroupSelect').html("");
			$('#repeatableGroupTR').attr("style","display:none");
			$('#initialRepeated').val("");
			$('#maximumRepeated').val("");
			$('#errorContainer2').attr("style","display:none");    
		}, 
		open : function() {
			$("#sectionName").focus();
			FormRowCalculator.redraw();   
		}  
	});  
	
	$("#addEditQuestionFancyBox").dialog({
		autoOpen: false,
		width: 950,
		modal: true,
		title: "Add Question",
		close : function() {
			$('#questionId, #qName, #qText').val('');
			$('#qGroup option:first').attr("selected", "selected");
			$('#qType option:first').attr("selected", "selected");
			$('#medicalCode option:first').attr("selected", "selected");
			$('#checkCDE').attr("checked", false);
			$('#myQuestions').html('');
			
			$('#sQ').attr("checked", true);
			$('#addEditQuestion').hide();
			$('#searchQuestion').show();
			
			clearQuestionInf();
			clearQuestionDetails();	
			resetWorkFlow(1);
			$('#linkDEcheckBox').attr('checked',true);
			workFlow=-1; // clear the workFlow
		}
	});	
	
	$("#addEditTextDialog").dialog({
		autoOpen: false,
		width: 950,
		modal: true,
		title: "Add text",
		buttons : [{
				text: "Save", click : function() {
					// get the html from the tinymce
					var html = $(".tinymce").tinymce().getContent();
					// are we creating or editing?
					if (editQuestionId == "") {
						addTextBlock(html);   // creating
					} else {
						editTextBlock(editQuestionId, editQuestionDivId, html);  // editing
					}
					$(this).dialog("close");
		  		}
			},{
		  		text : "Cancel", click : function() {
		  			$(this).dialog("close");
		  		}
			}
		],
		open : function() {
			$(".tinymce").tinymce({
				script_url	: "<s:property value="#webRoot"/>/common/js/tinymce/tinymce.min.js",
				menuvar 	: false,
				statusbar	: false,
				plugins		: ["textcolor"],
				toolbar1	: "undo redo | styleselect | bold italic | alignleft aligncenter alignright alignjustify | bullist numlist outdent indent | link | forecolor backcolor",
				oninit		: function() {
					if (editQuestionDivId != null) {
						var question = QuestionHandler.findQuestion(editQuestionDivId);
						$(".tinymce").tinymce().setContent(question.text);
					}
				}
			});
			
		},
		close : function() {
			$field = $(".tinymce");
			$("#textName").attr('disabled',false);
			$("#textName").val("");
			$field.tinymce().setContent("");
			$field.tinymce().remove($field.attr("id"));
		}
		
	});	
		
	setupDynamicObjects();
	
	var comingFromSaveAs = "<%=comingFromSaveAs%>";
	if (comingFromSaveAs == "comingFromSaveAs") {
		$("#createFormSubmitEdit").trigger("click");
	} else {
		$("#addEditFormInfoFancyBox").dialog("open");
	}
});


//---hehe  
function addEditQuestion_fancyConfirm() {
	var msg="";
	<%if (user.isSysAdmin()) {%>
		msg="<div style='text-align:left;margin-top:14px;'>"+
					"<h1>This question is being used by another form</h1> <p align='justify'>Would you like to make a global change (will affect other forms) or would you like to make a local change(create it as a new question).</p></p><font color='red'>Warning:</font> A global change may result in data loss in any current data collections that use this question.</p>"+
			"</div>";
	<%} else {%>
		msg="<div style='text-align:left;margin-top:14px;'>"+
				"<h1>This question is being used by another form</h1> <p align='justify'>Would you like to make a local change(create it as a new question).</p>"+
			"</div>";
	<%}%>
	var questionInfoURL;
	//alert("current mode in addEditQuestion_fancyConfirm():" +  questionInfoMode  +" with hasChanged flag=" + hasChanged);
	if (questionInfoMode=='add') {
		questionInfoURL= "<s:property value="#webRoot"/>/form/addEditQuestion.action?action=addQuestionAjax";
		doAddEditQuestionAjaxPost(questionInfoURL);
	} else if (questionInfoMode=='search') {
		if (hasChanged) {    // has changed
			if (editAttachedFormIds.length!=0) { // has been attached at other form
				fancyConfirm(msg, function(ret) { })
			} else { // not been attached 
				questionInfoURL= "<s:property value="#webRoot"/>/form/addEditQuestion.action?editMode=global&import=1&action=editQuestionAjax&qId="+editQuestionId+"&qVersion="+editQuestionVersionNumber;
				doAddEditQuestionAjaxPost(questionInfoURL);
			}
		}else{ // do nothing change
			questionInfoURL= "<s:property value="#webRoot"/>/form/addEditQuestion.action?editMode=global&import=1&action=editQuestionAjax&qId="+editQuestionId+"&qVersion="+editQuestionVersionNumber;
			doAddEditQuestionAjaxPost(questionInfoURL);
		}
	}else{ // edit question
		if (hasChanged) {    // has changed
			if (editAttachedFormIds.length!=0){ // has been attached at other form
				fancyConfirm(msg, function(ret) { });
			} else { // not been attached 
				questionInfoURL= "<s:property value="#webRoot"/>/form/addEditQuestion.action?editMode=global&import=0&action=editQuestionAjax&qId="+editQuestionId+"&qVersion="+editQuestionVersionNumber;			
				doAddEditQuestionAjaxPost(questionInfoURL);
			}
		} else { // do nothing change
			questionInfoURL= "<s:property value="#webRoot"/>/form/addEditQuestion.action?editMode=global&import=0&action=editQuestionAjax&qId="+editQuestionId+"&qVersion="+editQuestionVersionNumber;			
			doAddEditQuestionAjaxPost(questionInfoURL);
		}
	}
}

function fancyConfirm(msg,callback) {
    var ret;
    $.ibisMessaging("dialog", "info", msg, {
    	width: 500,
    	buttons: [
		<%if(user.isSysAdmin()){%>
        {
    		text: "Global Change",
    		click : function() {
				ret = false;
                
            	if(questionInfoMode=='search'){
            		questionInfoURL= "<s:property value="#webRoot"/>/form/addEditQuestion.action?editMode=global&import=1&action=editQuestionAjax&qId="+editQuestionId+"&qVersion="+editQuestionVersionNumber;
		    		doAddEditQuestionAjaxPost(questionInfoURL);
                }else{ // edit
                	questionInfoURL= "<s:property value="#webRoot"/>/form/addEditQuestion.action?editMode=global&import=0&action=editQuestionAjax&qId="+editQuestionId+"&qVersion="+editQuestionVersionNumber;
		    		doAddEditQuestionAjaxPost(questionInfoURL);
                }
            	
            	$(this).dialog("close");
				callback.call(this,ret);
    		},
    		id : 'heeeeeeee'
    	},
    	<%}%>
    	{
    		text: "Local Change",
    		click: function() {
				ret = true; 
                
				if(questionInfoMode=='search'){
					questionInfoURL= "<s:property value="#webRoot"/>/form/addEditQuestion.action?editMode=local&import=1&action=editQuestionAjax&qId="+editQuestionId+"&qVersion="+editQuestionVersionNumber;
		    		doAddEditQuestionAjaxPost(questionInfoURL);
                }else{ // edit
                	questionInfoURL= "<s:property value="#webRoot"/>/form/addEditQuestion.action?editMode=local&import=0&action=editQuestionAjax&qId="+editQuestionId+"&qVersion="+editQuestionVersionNumber;
		    		doAddEditQuestionAjaxPost(questionInfoURL);
                }
				
				$(this).dialog("close");
				callback.call(this,ret);
    		}
    	},{
    		text: "Cancel",
    		click: function() {
    			$(this).dialog("close");
    			callback.call(this,ret);
    		}
    	}
    ]});
}

function resetWorkFlow(toStep){
	if(toStep==1){
		$('#step2Q').removeClass();
		$('#step2Q').addClass('disable-link');
		$('#step2Q').attr('onclick','');
		
		$('#step3QA').removeClass();
		$('#step3QA').addClass('disable-link');
		$('#step3QA').attr('onclick','');
	}else if(toStep==2){
		$('#step3QA').removeClass();
		$('#step3QA').addClass('disable-link');
		$('#step3QA').attr('onclick','');
	}else{}
}

function resetSearchQuestionInf(){
	$('#addEditQuestion').hide();
	resetWorkFlow(2);
	clearQuestionInf();
	$('#searchAddBtu').hide();
	
}

function clearQuestionInf() {
	unlockQuestion(1); // to make questionName remain as disabled  
	$('#questionValidationErrorsDiv').html('');
	$('#questionValidationErrorsDiv').attr("style","display:none");
	
	if(questionInfoMode=='add'){
		//search form
		document.getElementById('searchQuestionForm').reset();
		$('#questionName').val("").attr("disabled", false);
	}

	//add form
	$('#questionText,#descriptionUp,#descriptionDown,#optionChoice, #optionScore, #optionSubmittedValue').val("");
	$('#versionUpdate, #includeOther').attr("checked",false);
}

function clearQuestionDetails(){
	if($("#defaultH").next("div").is(":visible")){
		$("#defaultH").click();
	}
		$('#defaultValue,#unansweredValue').val("");
	
	if($("#graphicH").next("div").is(":visible")){
		$("#graphicH").click();
	}
	
	if($("#groupH").next("div").is(":visible")){
		$("#groupH").click();
	}
	$('.dataTableContainer input[type="checkbox"]').attr("checked", false);
		
	$('#imageMape, #selects, #visualScale').hide();
	$('#imageTypeFr').attr("src","");
	$('#rangeStart').attr("value","1");
	$('#rangeEnd').attr("value","100");
	$('#scaleRangeMinimum').attr("value","1");
	$('#scaleRangeMaximum').attr("value","100");
	$('#scaleWidth').attr("value","100");
	$('#sliderEx').attr("width","100mm");
	$('#scaleCursor').attr("checked", true);
	$('#centerText, #leftText, #rightText').attr("value","");
	$('#rightT,#leftT,#centerT,#options').html("");	
	
	if($("#dataSpringH").next("div").is(":visible")){
		$("#dataSpringH").click();
		$("#dataSpringDiv").hide();
	}
		$('#dataSpring').attr("checked",false);
	
	
	if($("#validationH").next("div").is(":visible")){
		$("#validationH").click();
		$("#validationDiv").hide();
	}
		$('#required').prop("selectedIndex", 0);
		$('#answerType').prop("selectedIndex", 3);
		$('#calculationEditButton, #editCalculationTable').hide();
		$('#conversionFactor option:first').attr("selected", "selected");
		$('#window').html('');
		$('#minCharacters').val("0");
		$('#maxCharacters').val("4000");
		$('#rangeOperator option:first').attr("selected", "selected");
		$('#rangeValue1').val("");
		$('#rangeValue2').val("");
	
	if($("#skipRuleH").next("div").is(":visible")){
		$("#skipRuleH").click();
		$("#skipRuleDiv").hide();
	}
		$('#skipRuleOperatorType').prop("selectedIndex", 0);
		$('#skipRuleEquals').val("");
		$('#skipRuleEquals').attr("disabled", true);
		$('#skipRuleType option:first').attr("selected", "selected");
		
	$('#emailNotificationsH').attr("style","display: none");<%--the default type is textbox and the textbox doesn't need email attribute so hided --%>
	if($("#emailNotificationsH").next("div").is(":visible")){
		$("#emailNotificationsH").click();
		$("#emailNotificationsDiv").hide();
	}
		$('#_et_toAddr').val("");
		$('#_et_ccAddr').val("");
		$('#_et_subject').val("Emailing from the IBIS");
		$('#_et_body').val("");
		$('#triggerAnswerDiv').html('<select  id="_et_answers" multiple="true"></select>');
	
	
	if($("#formatH").next("div").is(":visible")){
		$("#formatH").click();
		$("#formatDiv").hide();
	}
		$('#align option:first').attr("selected", "selected");
		$('#vAlign option:first').attr("selected", "selected");
		$('#color option:first').attr("selected", "selected");
		$('#fontFace option:first').attr("selected", "selected");
		$('#fontSize option:first').attr("selected", "selected");
		$('#indent').val("0");
		$('#horizontalDisplay, #horizDisplayBreak').attr("checked", false).attr("disabled",false);
		
		//prepopulation
        $('#prepopulation').attr('checked',false);
        $('#prepopulationValue').val('none').attr('disabled',true);
		if($("#prepopulationH").next("div").css('display')!='none'){
			$("#prepopulationH").click();
			$("#prepopulationDiv").hide();
		}
		
		//decimal precision
		$('#decimalPrecision').val('-1');
		$("#decimalPrecisionDiv").hide(); 
}

function editFormInfo() {
	formInfoMode = "edit";
	editFormInfoPressed = true;
	var eDiv1Elm = document.getElementById("eDiv1");
	var eDiv2Elm = document.getElementById("eDiv2");

	if(eDiv1Elm != null) {
		eDiv1Elm.style.display="block";
		eDiv2Elm.style.display="none";
	}
	
	var fdsDiv = document.getElementById("fdsDiv");
	if(fdsDiv != null) {
		fdsDiv.style.display="none";
	}

	$("#addEditFormInfoFancyBox").dialog("open");
}

function setupDynamicObjects() {
	setupSortable();
	setupClick();
}

var uiSender = null;
function setupSortable() {
	QuestionHandler.questionRef = $(".section").sortable({
		connectWith: ".section",
		items: ".question",
		placeholder: "sectionPlaceholder",
		forcePlaceholderSize: true,
		start : function(event, ui) {
			uiSender = ui.item.parent();
		},
		stop : function(event, ui) {
			// check that the target does not have this question already
			var oldQuestionDivId=ui.item.attr("id");
			
			var qindex = QuestionHandler.findQuestionIndex(oldQuestionDivId);
			if (qindex != null && !ui.item.parent(".section").is(uiSender)) {
				
				var quest = questionsArray[qindex];
				var qName = quest.questionName;
				
				var oldSectionId = uiSender.attr("id");
				var oldSection = SectionHandler.findSection(oldSectionId);
				var $oldSection = $("#"+oldSection.id);
				
				var newSecId = ui.item.parent(".section").attr("id").split("_")[1];
				var newSection = SectionHandler.findSection(newSecId);
				
				var newQuestionDivId="S_"+newSecId+"_"+quest.questionId+"_"+quest.questionVersionNumber;
				var repeatedQuestions = QuestionHandler.findAllRepeatedQuestions(quest);
				var newRepeatedSects = SectionHandler.findAllRepeatableChildren(newSection.id);
				
				var duplicateFound = false;
				
				// check for duplicate
				for (var i=0; i < questionsArray.length; i++) {
					var tmpQuestion = questionsArray[i];
					if (tmpQuestion.questionName == qName && SectionHandler.convertToHTMLId(tmpQuestion.sectionId) == SectionHandler.convertToHTMLId(newSecId)) {
						duplicateFound = true;
						break;
					}
				}

				// if a duplicate is found, exit out.  Otherwise, continue
				if (duplicateFound) {
					$(this).sortable('cancel');
					alert("This section already contains that question.  A section can not contain two of the same question.");
				}
				else {
					// no duplicate
					DynamicElements.enforceSectionRows(event, {item: ui.item.parent(".section")});
					DynamicElements.enforceSectionRows(event, {item: event.target});
					// change the section that this question object resides in

					// was this question a part of a repeated section?
					if (oldSection.isRepeatable) {
						// yes, yes it was.  Gots'ta remove da children
						// for all of those, remove them
						for (var i = 0; i < repeatedQuestions.length; i++) {
							var tmpQuestion = repeatedQuestions[i];
							existingQuestionIdsToDeleteArray.push(tmpQuestion.questionId+"#"+tmpQuestion.sectionId);
							var qind = QuestionHandler.findQuestionIndex(tmpQuestion.newQuestionDivId);
							questionsArray.splice(qind,1);
							assignMinHeight($oldSection);
							DynamicElements.enforceSectionRows(null, {item: $oldSection});
						}
						setCalDependent(); // reset the calculation dependnce of question 
						setSkipRuleDependent();
					}
					
					// is the question being moved into a repeated section?
					if (newSection.isRepeatable) {
						// propagate this change throughout the child sections
						for(var i=0; i < newRepeatedSects.length; i++) {
							// nothing changes here except the section ID and div ID
							// the repeated section IDs are found above in repeatedSects
							var divId = SectionHandler.convertToHTMLId(newRepeatedSects[i].id) + "_" + quest.questionId + "_" + quest.questionVersionNumber;
							var ques = $.extend(true, {}, quest);
							ques.newQuestionDivId = divId;
							ques.sectionId = SectionHandler.convertToHTMLId(newRepeatedSects[i].id);
							questionsArray.push(ques);
							if(typeof quest.attributeObject.dataElementName != "undefined" && quest.attributeObject.dataElementName != 'none'){ // user want the data element
								dataStructureHandler.setDataElementAssociate(ques.newQuestionDivId,quest.attributeObject.dataElementName);
							}
						}
					}
					
					//update formula in attribute object of question object
					updateFormula(quest.sectionId,newSecId,quest.questionId);
					// update questionToSkip array in attribute object of question object
					updateQuestionToSkip(quest.sectionId,newSecId,quest.questionId);
					
					// update parent question object
					quest.sectionId = SectionHandler.convertToHTMLId(newSecId);
					quest.newQuestionDivId=newQuestionDivId;
					//-----------------------------------------------------------------------------------------------------------
					var originalHtml=$('#'+ui.item.attr("id")).html();
					//first repalce all oldDivIds			
					var newHtml=originalHtml.replace(/S_[-]?\d+_\d+_\d+/g,newQuestionDivId);
					$('#'+ui.item.attr("id")).html('');
					$('#'+ui.item.attr("id")).html(newHtml);
					// then update the Div id
					$('#'+ui.item.attr("id")).attr("id",newQuestionDivId);
					//---------------------------------------------------------------------------------------------------------------------
				
					// clear the data element?
					if (SectionHandler.needDeleteDEs(oldSection, newSection.isRepeatable, newSection.repeatableGroupName)) {
						// get all questions in (potentially) repeatable
						var questions = [quest];
						// make sure THIS question is in the array to be processed
						$.merge(questions, QuestionHandler.findAllRepeatedQuestions(quest));
						
						for (var i = 0; i < questions.length; i++) {
							questions[i].attributeObject.dataElementName = "none";
							$("#"+questions[i].newQuestionDivId).find(".questionDataElementName").text("none");
						}
					}

					ui.item.click();
				}
			}
		},
		
		remove : function(event, ui) {
			// fires when a question is removed from a section
			assignMinHeight(ui.item);
			DynamicElements.enforceSectionRows(event, {item: ui.item});
		},
		
		receive : function(event, ui) {
			
		}
	}).disableSelection();
	SectionHandler.sectionRef = $("#formSectionsDiv").sortable({
		items: ".section",
		start : function(event, ui) {
			// minimize the placeholder
			$(".ui-sortable-placeholder").height(2);
			$(".ui-sortable-placeholder").css("visibility","visible");
			$(ui.item).click();
			changeActiveSection($(ui.item));
			DynamicElements.startWatchSectionMove(event, ui);
		},
		stop : function(event, ui) {
			$(ui.item).width($(ui.placeholder).width());
			DynamicElements.enforceSectionRows(event, ui);
			DynamicElements.stopWatchSectionMove(event, ui);
		}
	}).disableSelection();
	
}

/**
 * Sets the given jquery element to be the "active section" on the page.
 * Gives this element the class "activeSection" and removes that class from all other sections on the page
 *
 * @param newActive jquery Elment
 */
function changeActiveSection(newActive) {
	clearActiveSection();
	newActive.addClass("activeSection");
}

/**
 * Sets the given jquery element to be the "active question" on the page.
 * Gives this element the class "activeQuestion" and removes that class from all other questions on the page
 *
 * @param newActive jquery Elment
 */
function changeActiveQuestion(newActive) {
	clearActiveQuestion();
	newActive.addClass("activeQuestion");
}

/**
 * Sets up the onclick action for sections and questions
 */
function setupClick() {
	$(".section").click(function() {
		changeActiveSection($(this));
		var $questionsInSection = $(this).children(".question");
		if ($questionsInSection.length > 0) {
			changeActiveQuestion($questionsInSection.first());
		}
		else {
			clearActiveQuestion();
		}
	});
	$(".question").click(function(event) {
		event.stopPropagation();
		changeActiveQuestion($(this));
		// if we click on a question, also click the section
		changeActiveSection($(this).parent(".section"));
	});
}

function setupHover() {
	$(".question").hover(function() {
		SectionHandler.sectionRef.sortable("disable");
	},function() {
		SectionHandler.sectionRef.sortable("enable");
	});
}

function refreshDynamicObjects() {
	$(".section").each(function() {
		if ($(this).hasClass("ui-sortable")) {
			$(this).sortable("destroy");
		}
	});
	$("#formSectionsDiv").sortable("destroy");
	$(".section").unbind("click");
	$(".question").unbind("click");
	$('.question').unbind("mouseenter").unbind("mouseleave");
	setupSortable();
	setupClick();
	setupHover();
}

function makeSectionDynamic(sectionId) {
	refreshDynamicObjects();
}

function makeQuestionDynamic(QuestionDivId) {
	$(".section").each(function() {
		// clear the old height
		$(this).css("height", "auto");
		// size the rows correctly
		DynamicElements.enforceSectionRows(null, {item: $(this)});
	});
	refreshDynamicObjects();
}

//trigger the section fancy box
function triggerAddSectionAnchor() {
	Interface.switchSectionFormToAdd();
	
	if ($('#activeSectionId').length < 1) {
		$("body").append('<input type="hidden" id="activeSectionId" />');
	} else {
		$('#activeSectionId').val("");
	}

	$("#addEditSectionFancyBox").dialog("open");
}


//trigger the question fancy box
function triggerAddQuestionAnchor(sectionID) {
	currentSectionID=sectionID;
	if ($('#activeSectionId').length < 1) {
		$("body").append('<input type="hidden" id="activeSectionId" />');
	}
	$("#activeSectionId").val(sectionID);
	editQuestionId="";
	//commented for now till its time to uncomment
	Interface.switchQuestionToAdd();
	$('#linkDEcheckBox').attr('checked',true); //when adding question, have the linkde checkbox checked
	$("#addEditQuestionFancyBox").dialog("open");
}

// trigger the text dialog
function triggerAddText(sectionID) {
	editQuestionDivId = null;
	currentSectionID=sectionID;
	if ($('#activeSectionId').length < 1) {
		$("body").append('<input type="hidden" id="activeSectionId" />');
	}
	$("#activeSectionId").val(sectionID);
	editQuestionId="";
	$("#addEditTextDialog").dialog("open");
}

function triggerEditText(textId) {
	editQuestionDivId = textId;
	var textIdSplit = textId.split("_");
	editQuestionId = textIdSplit[2]; 
	var editThisQuestion = QuestionHandler.findQuestionById(editQuestionId);
	$("#textName").attr('disabled',true);
	$("#textName").val(editThisQuestion.questionName);
	// open the dialog, THEN set the content.  There's no tinymce until then
	$("#addEditTextDialog").dialog("open");
	$("").tinymce().setContent(contentHTML);
}
/**
 * Adds a text block question element to the given sectionId with the
 * given HTML
 */
function addTextBlock(html) {
	// create the question
	var url = "<s:property value="#webRoot"/>/form/addEditQuestion.action";
	var data = "action=addQuestionAjax&" 
		+ "id=-2147483648&"
		+ "questionForm.type=12&" // 12 = textblock
		// I'm getting a unique name here by using current milliseconds
		// it's not ideal but we don't actually NEED the name for textblocks
		+ "questionForm.questionName=" + String(new Date().getTime()) + "&"
		+ "questionForm.descriptionUp=&"
		+ "questionForm.descriptionDown=&"
		// fulfills the required value here
		+ "questionForm.text=none&"
		// NOTE: new name here for the action
		+ "versionUpdate=false&"
		+ "questionForm.htmlText=" + encodeURIComponent(html) + "&"
		+ "questionForm.maxCharacters=4000&"
		+ "questionForm.minCharacters=0&"
		+ "questionForm.showHandle=true";
	
	$.ajax({
		type:"post",
		url: url,
		data: data,
		success: function(response) {
			var qJSON=JSON.parse(response);
			//alert("Response from question save/add: " + response);
			questionId=qJSON.questionId;
			questionVersionLetter=qJSON.questionVersionString;
			questionVersionNumber=qJSON.questionVersionNumber;
			questionName=qJSON.questionName;
			questionText=qJSON.questionText;
			descriptionUp=qJSON.descriptionUp;
			descriptionDown=qJSON.descriptionDown;				
			questionType=qJSON.questionType;
			questionOptions=qJSON.options;
			imageOption=qJSON.imageOption;
			imageFileName=qJSON.imageFileName;
			defaultValue=qJSON.defaultValue;
			unansweredValue=qJSON.unansweredValue;
			associatedGroupIds=qJSON.associatedGroupIds;
			includeOther=qJSON.includeOther;
			attachedFormIds=qJSON.attachedFormIds;
			hasDecimalPrecision = qJSON.hasDecimalPrecision;
			hasCalDependent = qJSON.hasCalDependent;
			prepopulation = qJSON.prepopulation;
			text = qJSON.text;
			existingQuestion = false;
			forcedNewVersion = false;
			addTextBlockAjaxCallback(text);
		}
	});
}

function addTextBlockAjaxCallback(html) {
	// so here we have a created section with question ID stored in questionId
	// and the other values as shown in the function above ^
	var sectionId=$('div.activeSection').attr("id");
	var newQuestionDivId=sectionId+"_"+questionId+"_"+questionVersionNumber;
	var attribute=new AttributeObject();
	var visualScaleInfo=""+parsingSymbol+""+parsingSymbol+""+parsingSymbol+""+parsingSymbol+""+parsingSymbol+""+parsingSymbol+"";
	var graphicNames = "";
	attribute.qType=questionType;
	attribute=setAttributeObject(attribute);
	var question = new QuestionObj(questionId,questionVersionLetter,questionVersionNumber,questionName,questionText,
			descriptionUp,descriptionDown,questionType,questionOptions,imageOption,imageFileName,visualScaleInfo,
			newQuestionDivId,sectionId,attribute, graphicNames,defaultValue,unansweredValue,associatedGroupIds,
			includeOther,attachedFormIds, hasDecimalPrecision,hasCalDependent, prepopulation, text);
	questionsArray.push(question);
	
	// this is add, not edit, so we can just loop through this section of code to add questions for repeatable sections
	// get the section and find out if it's repeatable
	var sect = sectionsArray[SectionHandler.findSectionIndex(sectionId)];
	if (sect.isRepeatable && sect.repeatedSectionParent == -1) {
		// since we are in a repeatable parent, add the children!
		var repeatedSects = SectionHandler.findAllRepeatableChildren(sect.id);
		for (var i=0; i < repeatedSects.length; i++) {
			// nothing changes here except the section ID and div ID
			// the repeated section IDs are found above in repeatedSects
			var divId = SectionHandler.convertToHTMLId(repeatedSects[i].id) + "_" + question.questionId + "_" + question.questionVersionNumber;
			var rAttribute = owl.deepCopy(question.attributeObject);
			var ques = new QuestionObj(questionId,questionVersionLetter,questionVersionNumber,questionName,questionText,
					descriptionUp,descriptionDown,questionType,questionOptions,imageOption,imageFileName,visualScaleInfo,
					divId,repeatedSects[i].id,rAttribute,graphicNames,defaultValue,unansweredValue,associatedGroupIds,
					includeOther,attachedFormIds, "");
			questionsArray.push(ques);
		}
		// there's no data element linking for text blocks
	}
	
	//create the new question div
	var newQuestionDiv = questionShow(question,attribute);    //  <--------------show different question
	//add the newQuestionDiv to the Section Div
	clearActiveQuestion();
	if ($('div.activeSection .clearboth').length > 0) {
		$('div.activeSection .clearboth').before(newQuestionDiv);
	} else {
		$('div.activeSection').append(newQuestionDiv);
	}
	// make the question dynamic
	makeQuestionDynamic(newQuestionDivId);
	// don't format text block questions
	// change the section's resize min height
	assignMinHeight($('div.activeSection'));
	QuestionHandler.addQuestion(question);
}

/**
 * Edits the given question and div with the given HTML
 */
function editTextBlock(qId, divId, html) {
	var question = QuestionHandler.findQuestion(divId);
	editQuestionDivId = divId;
	editQuestionId = qId;
	question.text = html;

	// perform an update
	var url = "<s:property value="#webRoot"/>/form/addEditQuestion.action?editMode=global&import=0&action=editQuestionAjax&qId=" + question.questionId+ "&qVersion="+question.questionVersionNumber;
	// we can only change the text of a question, so...
	var data = "id=-2147483648&"
		+ "questionForm.type=12&" // 12 = textblock
		+ "questionForm.questionName=" + question.questionName + "&"
		+ "questionForm.descriptionUp=&"
		+ "questionForm.descriptionDown=&"
		+ "questionForm.text=none&"
		+ "versionUpdate=false&"
		+ "questionForm.htmlText=" + encodeURIComponent(html) + "&"
		+ "questionForm.maxCharacters=4000&"
		+ "questionForm.minCharacters=0&"
		+ "questionForm.showHandle=true";
	
	$.ajax({
		type:"post",
		url: url,
		data: data,
		success: function(response) {
			var qJSON=JSON.parse(response);
			//alert("Response from question save/add: " + response);
			questionId=qJSON.questionId;
			text = qJSON.text;
			existingQuestion = false;
			forcedNewVersion = false;
			editTextBlockAjaxCallback(editQuestionId, editQuestionDivId, text);
		}
	});
}

//callback after update
function editTextBlockAjaxCallback(qId, divId, html) {
	console.log("edit complete");
	// just update the html display here
	$("#" + divId + " .textblockContainer").html(html);
}



//cancel add form info
function cancelAddEditFormInfo() {
	$("#addEditFormInfoFancyBox").dialog("close");
		if(!proceedToBuildForm) {
			var url = "<s:property value="#webRoot"/>/form/formHome.action";
			redirectWithReferrer(url);
		}
}

function initSlider(questionName, questionId, min, max) {
	$("#"+questionName).slider({
		min : min,
		max : max,
		change : function(event,ui){
			var input = $("#"+questionId);
			input.attr("value", ui.value);
		}
	});
}

function cancel() {
	var mode = "<%=formMode%>";
	var formId = $('#formBuildFormId').val();
	var url;

	if (mode == "create") {
		url = "<s:property value="#webRoot"/>/form/deleteForm.action?mode=create&id=" + formId;
	} else {
		url = "<s:property value="#webRoot"/>/form/formHome.action?cancelFromBuildForm=true&&formId="+ formId;
	}

	redirectWithReferrer(url);
}


//cancel add/edit section
function cancelAddEditSection() {
	$("#addEditSectionFancyBox").dialog("close");
}
//cancel add/edit question
function cancelAddEditQuestion() {
	$("#addEditQuestionFancyBox").dialog("close");
}

//trim helper function
function trim(str) {
    return str.replace(/^\s+|\s+$/g,"");
}

function isInt(n) {
	return !isNaN(n) && n % 1 == 0;
}

//function for adding a section
function addSection() {
	$("#errorContainer2").html("");
	
	var sectionName = document.getElementById("sectionName").value;
	var sectionNameTrim = trim(sectionName);
	if (sectionNameTrim == "") {
		$.ibisMessaging(
				"primary", 
				"error", 
				"<s:text name="errors.text"/><b>Section Name is required.</b>",
				{
					container: "#errorContainer2"
				});
		$("#errorContainer2").show();
		return false;
	} else {
		var sectionDescription = document.getElementById("sectionDescription").value;
		var sectionCollapsible;
		var sectionImage;
		var isRepeatable;
		var initRepeatedSecs = -1;
		var maxRepeatedSecs = -1;
		var repeatedSectionParent = "-1";
		var repeatableGroupName="None";
		
		if (document.getElementById("sectionCollapsible").checked == 1) {
			sectionCollapsible = "true";
		} else {
			sectionCollapsible = "false";
		}
		
		var sectionImageCheckbox = document.getElementById("sectionImage");
		if (sectionImageCheckbox.checked) {
			sectionImage = "true";
		} else {
			sectionImage = "false";
		}
		
		if ($("#sectionRepeatbale").is(":checked")) {
			isRepeatable = true;

			initRepeatedSecs = trim($('#initialRepeated').val());
			maxRepeatedSecs = trim($('#maximumRepeated').val());
			repeatableGroupName = $('#repeatableGroupSelect').val();

			if (isInt(initRepeatedSecs) && isInt(maxRepeatedSecs)) {
				initRepeatedSecs = Number(initRepeatedSecs);
				maxRepeatedSecs = Number(maxRepeatedSecs);
				var errorString = "";
				if(initRepeatedSecs < 0 || maxRepeatedSecs < 0) {
					errorString = "<s:text name="errors.text"/><b>Initial Number or Maximum Number can not be less than 0</b>";
				}
				if(initRepeatedSecs > maxRepeatedSecs) {
					errorString = "<s:text name="errors.text"/><b>Maximum Number of times viewed can not be less than the Initial Number of times viewed.</b>";
				}
				if(initRepeatedSecs == 0) {
					errorString = "<font color='red'><s:text name="errors.text"/><b>Initial Number of times viewed must be greater than 0.</b>";
				}
				if(maxRepeatedSecs > 30) {
					var errorString = "<s:text name="errors.text"/><b>Maximum Number of times viewed must not be greater than 30.</b>";
				}
				if (errorString != "") {
					$.ibisMessaging("primary", "error", errorString,{container: "#errorContainer2"});
					$("#errorContainer2").show();
					return false;
				}
				
				//now check to see if repeatable group is associated and that threhold is correct
				if (repeatableGroupName != "None") {
					var rgThreshold;
					for (var i=0;i<DSObj.repeatableGroupsArr.length;i++){
						var rg = DSObj.repeatableGroupsArr[i];
						var rgName = rg.repeatableGroupName;
						
						if (repeatableGroupName == rgName) {
							rgThreshold = rg.repeatableGroupThreshold;
							break;
						}
					}
					//threshold of 0 means infinity....so check for that
					//also...threshold can be one for these truly repeatable secs (repeats more than once) if it is set for MORETHAN.  (in other words...MORE THAN 1)
					if (rgThreshold != 0 && rgThreshold != 1) {
						if (maxRepeatedSecs > rgThreshold) {
							var errorString = "<s:text name="errors.text"/><b>Maximum Number of times viewed can not be greater than Repeatable Group threshold of " + rgThreshold + "</b>";
							$.ibisMessaging("primary", "error", errorString, {container: "#errorContainer2"});
							$("#errorContainer2").show();
							return false;
						} else if (maxRepeatedSecs < rgThreshold) {
							alert("Warning: Maximum Number of times viewed is less than the Repeatable Group threshold of " + rgThreshold);
						}
					}
				}
			} else {
				var errorString = "<s:text name="errors.text"/><b>Valid numbers are required for Initial and Maximum Number of times viewed.</b>";
				$.ibisMessaging("primary", "error", errorString, {container: "#errorContainer2"});
				$("#errorContainer2").show();
				return false;
			}
		} else {
			isRepeatable = false;
		}
		
		var sectionID = "S_" + bogusSectionId;
		bogusSectionId = bogusSectionId - 1;

		//create Section javascript object and push onto array
		var section = new SectionObj(sectionID,sectionNameTrim,sectionDescription,sectionCollapsible,sectionImage,isRepeatable,initRepeatedSecs,maxRepeatedSecs,repeatedSectionParent,repeatableGroupName);
		SectionHandler.addSection(section);
		//create the new section div
		var newSectionDiv = section.getDiv();

		//add the newQuestionDiv to the formSections Div 
		if ($('div.activeSection').length < 1) {
			clearActiveSection();
			$('#formSectionsDiv > .clearboth').before(newSectionDiv);
		} else {
			var activeSection = $('.activeSection');
			clearActiveSection();
			// added last() here just in case
			activeSection.last().after(newSectionDiv);
		}
		// adds this section to the resizable and drag/droppable
		makeSectionDynamic(sectionID);
		
		if (isRepeatable) {
			var childRepeatableSection;
			for (var i=1;i<maxRepeatedSecs;i++) {
				childRepeatableSection = null;
				var childRepeatableSectionId = SectionHandler.convertToHTMLId(bogusSectionId);
				bogusSectionId--;
				childRepeatableSection = new SectionObj(childRepeatableSectionId,sectionNameTrim,sectionDescription,sectionCollapsible,sectionImage,isRepeatable,initRepeatedSecs,maxRepeatedSecs,sectionID,repeatableGroupName);
				SectionHandler.addSection(childRepeatableSection);
			}
		}
		$("#addEditSectionFancyBox").dialog("close");
	}
}

// Show the question by different question type // Ching Heng
function questionShow(question,attribute){ // welcom to the show
	var changeLineFlag=true;
	var textLength=question.questionText.length;
	var deleteQuestionIconPath = "../images/icons/delete.png";
	var editQuestionIconPath =  "../images/icons/cog.png";
	
	// check to see if this is a question in a duplicated section.  If so, don't draw
	var sect = sectionsArray[SectionHandler.findSectionIndex(SectionHandler.convertToHTMLId(question.sectionId))];
	if (sect.isRepeatable && sect.repeatedSectionParent != -1) {
		// this question is inside a duplicated (child) section, not a regular section nor repeated parent
		return "";
	}
	var QuestionHTMLup=		'<div id="' +question.newQuestionDivId+ '" class="question activeQuestion">' + 
								'<div id="'+question.newQuestionDivId+'_header" class="questionHeader">';
	if (question.questionType != 12) {							
				QuestionHTMLup += '<div id="QDE_'+question.questionId+'" align="left" style="float:left;"> <b><s:text name="questionlibrary.DataElement" />:&nbsp;</b>'+
										"<span class=\"questionDataElementName\">"+ attribute.dataElementName + "</span>"+
									"</div>";
				QuestionHTMLup += "<a href='javascript:void(0)' title='Edit Question' onclick=\"QuestionHandler.editQuestion('"+question.newQuestionDivId+"');\"><img src='" + editQuestionIconPath + "' /></a>";
	} else {
				QuestionHTMLup += "<a href='javascript:void(0)' title='Edit Text' onclick=\"QuestionHandler.editTextblock('"+question.newQuestionDivId+"');\"><img src='" + editQuestionIconPath + "' /></a>";
	}
																		
	QuestionHTMLup += 			"<a href='javascript:void(0)' title='Remove Question' onclick=\"QuestionHandler.removeQuestion('"+question.newQuestionDivId+"');\"><img src='" + deleteQuestionIconPath + "' /></a>" +
								'</div>' +
								'<hr>'+						 
								'<div id="'+question.newQuestionDivId+'_body">';
						
	if (question.questionType != 12) {
				QuestionHTMLup +='<div style="float:left;">'+
										'<pre id="pre_'+question.newQuestionDivId+'" style="display:inline;"></pre>'+
											'<font id="font_'+question.newQuestionDivId+'">';
		if (question.descriptionUp.length!=0) {
				QuestionHTMLup += question.descriptionUp+'&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<br/>';
		}
	
		QuestionHTMLup += question.questionText+'&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;';

		if (question.descriptionDown.length!=0) {
			QuestionHTMLup += 	'<br/>'+question.descriptionDown+'&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;';			 									
		}
		QuestionHTMLup += 		'&nbsp;</font>'+
							'</div>';
	}
								 
	if (!attribute.horizDisplayBreak) {
		QuestionHTMLup += 		 '<div style="float:left;">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</div>';
		changeLineFlag=false;
	}
	
	if (question.descriptionUp.length!=0){
		QuestionHTMLup+='<br/>';
	}			 						
			 						
	var QuestionHTMLbottom ='';
	if (question.descriptionDown.length!=0){			 						
		QuestionHTMLbottom+='<br/>';
	}
	
	QuestionHTMLbottom +='<div style="clear:both"></div></div></div>';

	if(question.questionType==<%=QuestionType.TEXTBOX.getValue()%>){ //text box		
		var textBoxHTML='<input type="text" disabled="true" id="'+question.questionId+'">';
						 	
		var questionDiv=QuestionHTMLup+textBoxHTML+QuestionHTMLbottom;						 
		return questionDiv;
	}
	else if(question.questionType==<%=QuestionType.TEXT_BLOCK.getValue()%>){ //text box		
		var textBoxHTML='<div class="textblockContainer">'+question.text+'</div>';	 	
		var questionDiv=QuestionHTMLup+textBoxHTML+QuestionHTMLbottom;						 
		return questionDiv;
	}
	else if(question.questionType==<%=QuestionType.TEXTAREA.getValue()%>){ //text area
		var textAreaHTML='<TEXTAREA disabled="true" id="'+question.questionId+'" style="width:auto"></TEXTAREA>';
		var questionDiv=QuestionHTMLup+textAreaHTML+QuestionHTMLbottom;
		return questionDiv;
	}
	else if(question.questionType==<%=QuestionType.SELECT.getValue()%>){ // select
		var OptionsCodes=new Array();
		var optionsTemp=new Array();
		var selectHTML='<select  id="'+question.questionId+'">';
		if(question.questionOptions!=null && question.questionOptions!='undefined'&& question.questionOptions!=''){
			OptionsCodes=question.questionOptions.split(parsingSymbol);
			for (var i=0;i<OptionsCodes.length;i++){
				optionsTemp=OptionsCodes[i].split("|");
				if(optionsTemp[0]!=null && typeof(optionsTemp[0])!='undefined' && optionsTemp[0]!=''){
					selectHTML+=
								'<option value="'+optionsTemp[1]+'">'+
									optionsTemp[0]+
								"</option>";
				}
			}
		}
		selectHTML+="</select>";
		if(question.includeOther){
			selectHTML+="<input type='text' disabled='true' value='Other, please specify'>";	
		}
							 
		var questionDiv=QuestionHTMLup+selectHTML+QuestionHTMLbottom;
		return questionDiv;
	}
	else if(question.questionType==<%=QuestionType.RADIO.getValue()%>){   //Radio
		var OptionsCodes=new Array();
		var optionsTemp=new Array();
		var radioHTML="";
		if(question.questionOptions!=null && question.questionOptions!='undefined' && question.questionOptions!=''){
			OptionsCodes=question.questionOptions.split(parsingSymbol);
			for (var i=0;i<OptionsCodes.length;i++){
				optionsTemp=OptionsCodes[i].split("|");
				if(optionsTemp[0]!=null && typeof(optionsTemp[0])!='undefined' && optionsTemp[0]!=''){
					if(attribute.horizontalDisplay){
						radioHTML+=
							'<input type="radio" name="'+question.questionId+'_'+optionsTemp[0]+'" id="'+optionsTemp[0]+'"/>'+
								'&nbsp;<font class="font_'+question.newQuestionDivId+'_option">'+
									optionsTemp[0]+
								'</font>';
						if(question.includeOther && OptionsCodes[i]=='<%=CtdbConstants.OTHER_OPTION%>'){
							radioHTML+="<input type='text' disabled='true'>";	
						}		
						radioHTML+="&nbsp;";
					} else {
						if (textLength>0 && radioHTML.length>0 && changeLineFlag){
							for(var k=0; k<textLength+attribute.indent;k++)
							{radioHTML+=" ";}
						}
						radioHTML+=
							'<input type="radio" name="'+question.questionId+'_'+optionsTemp[0]+'" id="'+optionsTemp[0]+'"/>'+
								'&nbsp;<font class="font_'+question.newQuestionDivId+'_option">'+
									optionsTemp[0]+
								'</font>';
						if (question.includeOther && OptionsCodes[i]=='<%=CtdbConstants.OTHER_OPTION%>'){
							radioHTML+="<input type='text' disabled='true'>";	
						}	
						radioHTML+="<br>";	
					}
				}
			}
		}
							 
		var questionDiv=QuestionHTMLup+"<div style='float:left; word-wrap: break-word; width: 100%'>"+radioHTML+"</div>"+QuestionHTMLbottom;
		return questionDiv;
	}
	else if (question.questionType==<%=QuestionType.MULTI_SELECT.getValue()%>){ //multi-select
		var OptionsCodes=new Array();
		var optionsTemp=new Array();
		var multiSelectHTML='<select MULTIPLE name="'+question.questionText+'" id="'+question.questionId+'">';
		if (question.questionOptions!=null && question.questionOptions!='undefined' && question.questionOptions!='') {
			OptionsCodes=question.questionOptions.split(parsingSymbol);
			for (var i=0;i<OptionsCodes.length;i++) {
				optionsTemp=OptionsCodes[i].split("|");
				if (optionsTemp[0]!=null && typeof(optionsTemp[0])!='undefined' && optionsTemp[0]!='') {
					multiSelectHTML+=
								'<option value="'+optionsTemp[1]+'">'+
									optionsTemp[0]+
								"</option>";
				}
			}
		}
		multiSelectHTML+="</select>";
		if(question.includeOther){
			multiSelectHTML+="<input type='text' disabled='true' value='Other, please specify'>";	
		}							 
		var questionDiv=QuestionHTMLup+multiSelectHTML+QuestionHTMLbottom;
		return questionDiv;
	}
	else if (question.questionType==<%=QuestionType.CHECKBOX.getValue()%>) { //checkbox
		var OptionsCodes=new Array();
		var optionsTemp=new Array();
		var checkboxHTML="";
		if(question.questionOptions!=null && question.questionOptions!='undefined' && question.questionOptions!=''){
			OptionsCodes=question.questionOptions.split(parsingSymbol);
			for (var i=0;i<OptionsCodes.length;i++){
				optionsTemp=OptionsCodes[i].split("|");
				if(optionsTemp[0]!=null && typeof(optionsTemp[0])!='undefined' && optionsTemp[0]!=''){
					if(attribute.horizontalDisplay){
						checkboxHTML+=
							'<input type="checkbox" value="'+optionsTemp[1]+'" id="'+question.questionId+'_'+optionsTemp[1]+'"/>'+
								'&nbsp;<font class="font_'+question.newQuestionDivId+'_option">'+	
									optionsTemp[0]+
								'</font>';
						if(question.includeOther && OptionsCodes[i]=='<%=CtdbConstants.OTHER_OPTION%>'){
							checkboxHTML+="<input type='text' disabled='true'>";	
						}
						checkboxHTML+="&nbsp;";
					} else {
						if (textLength>0 && checkboxHTML.length>0 && changeLineFlag) {
							for (var k=0; k<textLength+attribute.indent;k++) { 
								checkboxHTML+=' '; 
							}
						}
						checkboxHTML+=
							'&nbsp;<input type="checkbox" value="'+optionsTemp[1]+'" id="'+question.questionId+'_'+optionsTemp[1]+'"/>'+
								'<font class="font_'+question.newQuestionDivId+'_option">'+
									optionsTemp[0]+
								'</font>';
						if(question.includeOther && OptionsCodes[i]=='<%=CtdbConstants.OTHER_OPTION%>'){
							checkboxHTML+="<input type='text' disabled='true'>";	
						}
						checkboxHTML+="<br>";	
					}
				}
			}
		}
		
		var questionDiv=QuestionHTMLup+"<div style='float:left;'>"+checkboxHTML+"</div>"+QuestionHTMLbottom;
		return questionDiv;
	}
	else if(question.questionType==<%=QuestionType.IMAGE_MAP.getValue()%>){ //image map
		var imageOptionsCodes=new Array();
		var optionsTemp=new Array();
		var imageOptionHTML='<select MULTIPLE name="'+question.questionText+'" id="'+question.questionId+'">';
		if(question.imageOption!=null && question.imageOption!='undefined' && question.imageOption!=''){
			imageOptionsCodes=question.imageOption.split(parsingSymbol);
			for (var i=0;i<imageOptionsCodes.length;i++){
				if(imageOptionsCodes[i]!=null && imageOptionsCodes[i]!='undefined' && imageOptionsCodes[i]!='')
				imageOptionHTML+=
								'<option value="'+imageOptionsCodes[i]+'">'+
									imageOptionsCodes[i]+
								"</option>";
			}
		}
		imageOptionHTML+="</select>";
		var imageMapHTML="";
		if(question.imageFileName!=null && question.imageFileName!='undefined' && question.imageFileName!=''){
			imageMapHTML='<img width="250" height="250" border="0" src="<s:property value="#imageRoot"/>/questionimages/';
			imageMapHTML+=question.imageFileName+'" id="'+question.questionId+'">';
		}
		
		var imageOptionHTMLall=imageMapHTML+imageOptionHTML;
		var questionDiv=QuestionHTMLup+imageOptionHTMLall+QuestionHTMLbottom;
		return questionDiv;
	}
	else if (question.questionType==<%=QuestionType.VISUAL_SCALE.getValue()%>) { // visual scale form add question
		var visualScale=question.visualScaleInfo.split(parsingSymbol); //get info from JSON object
		var visualScaleHTML=
							'<div id="'+question.questionName+question.sectionId+'" class="slider" style="width:'+visualScale[5]+'mm;">'+
								'<input id="'+question.questionId+'" class="slider-input" value="" >'+     //<==============================input
								'<script language="javascript">'+
									// this needs to be filled in more for actual view and input
									"initSlider('"+question.questionName+question.sectionId+"', '"+question.questionId+"', '"+visualScale[3]+"', '"+visualScale[4]+"');"+
								"</"+
								"script>"+
							"</div>";
							
			var visualScale=question.visualScaleInfo.split(parsingSymbol); //get info from JSON object
			var left='';center='';right='';
			for (var i=0;i<visualScale.length;i++){
				left=visualScale[0];
				center=visualScale[1];
				right=visualScale[2];
			}
			var rightPosition=visualScale[5];
			var visualScaleHTMLall=
									"<table>"+
										 "<tr>"+
										 	"<td align='right'>"+
										 		'<font class="font_'+question.newQuestionDivId+'_option">'+
											 		left+
											 	'</font>'+
										 	"</td>"+
										 	"<td align='center'>"+
										 		visualScaleHTML+
										 		"</br>"+
										 		'<font class="font_'+question.newQuestionDivId+'_option">'+
										 			center+
										 		'</font>'+
										 	"</td>"+
										 	"<td align='left'>"+
										 		'<font class="font_'+question.newQuestionDivId+'_option">'+
										 			right+
										 		'</font>'+	
										 	"</td>"+
										 "</tr>"+
									 "</table>";	
		var questionDiv=QuestionHTMLup+visualScaleHTMLall+QuestionHTMLbottom;
		return questionDiv;
	}
	else if(question.questionType==<%=QuestionType.File.getValue()%>){//  File
		var fileHTML='<input type="file" disabled="true" id="'+question.questionId+'">';
		var questionDiv=QuestionHTMLup+fileHTML+QuestionHTMLbottom;						 
		return questionDiv;
	}
	else{ // unknow question type
		var otherHTMLall=
							"<table>"+
								"<tr>" + 
									"<td align='left' valign='top'>" + 
										"<b>Question Name: </b>" + 
									"</td>" + 
								 	"<td align='left' valign='top'>" +
								 		question.questionName+
								 	"</td>" +
								 	"<td align='left' valign='top'>"+
								 		"<b>Question Type: </b>"+
								 	"</td>"+
								 	"<td align='left' valign='top'>"+
								 		question.questionType+
								 	"</td>"+
								 	"<td>"+
								 		"<b>Question Texet: </b>"+
								 	"</td>"+
								 	"<td align='left' valign='top'>"+
								 		question.questionText+
								 	"</td>"+
								 "</tr>"+
							"</table>";
		var questionDiv=QuestionHTMLup+otherHTMLall+QuestionHTMLbottom;
		return questionDiv;
	}
		
}

//function for adding a question___Ching Heng
function addQuestionToForm(questionName, questionId, questionVersionLetter, questionVersionNumber, 
		questionOptions, imageOption, imageFileName, graphicNames, defaultValue, unansweredValue,
		associatedGroupIds, attachedFormIds, hasDecimalPrecision, hasCalDependent, prepopulation) {

	var orgQuestionName = $("#questionName").val();
	var questionText=$("#questionText").val();
	var descriptionUp=$("#descriptionUp").val();
	var descriptionDown=$("#descriptionDown").val();
	var questionType=$("#questionType").val();
	
	var visualScaleInfo = $('#leftText').val() + parsingSymbol + $('#centerText').val() + parsingSymbol + 
			$('#rightText').val() + parsingSymbol + $('#scaleRangeMinimum').val() + parsingSymbol + $('#scaleRangeMaximum').val() + 
			parsingSymbol + $('#scaleWidth').val() + parsingSymbol + $('#scaleCursor').val();
	var includeOther=$('#includeOther').is(':checked');	
	
	var sectionId=$('div.activeSection').attr("id");
	var newQuestionDivId=$('div.activeSection').attr('id')+"_"+questionId+"_"+questionVersionNumber;
	
	//create AttributeObject
	var attribute=new AttributeObject();
	attribute.qType=questionType;
	attribute=setAttributeObject(attribute);
	//create Question javascript object and push onto array
	var question = new QuestionObj(questionId, questionVersionLetter, questionVersionNumber, questionName, questionText,
			descriptionUp, descriptionDown, questionType, questionOptions, imageOption, imageFileName, visualScaleInfo,
			newQuestionDivId, sectionId, attribute, graphicNames, defaultValue, unansweredValue, associatedGroupIds,
			includeOther, attachedFormIds, hasDecimalPrecision, hasCalDependent, prepopulation, text);
	
	if (questionInfoMode=='search') {
		// also need to change the other same question
		for (var i = 0; i < questionsArray.length; i++) {
			if (questionsArray[i].questionName==orgQuestionName) {
				updateSameQuestionInForm(question,questionsArray[i],i);
			} 
		}	
	}
	questionsArray.push(question);
	//alert("The new quesiton obj to be used to update question whole array: \n" + JSON.stringify(question) +"\n\n");
	
	// this is add, not edit, so we can just loop through this section of code to add questions for repeatable sections
	// get the section and find out if it's repeatable
	var sect = sectionsArray[SectionHandler.findSectionIndex(sectionId)];
	if (sect.isRepeatable && sect.repeatedSectionParent == -1) {
		// since we are in a repeatable parent, add the children!
		var repeatedSects = SectionHandler.findAllRepeatableChildren(sect.id);
		for(var i=0; i < repeatedSects.length; i++) {
			// nothing changes here except the section ID and div ID
			// the repeated section IDs are found above in repeatedSects
			var divId = SectionHandler.convertToHTMLId(repeatedSects[i].id) + "_" + question.questionId + "_" + question.questionVersionNumber;
			var rAttribute = owl.deepCopy(question.attributeObject);
			var ques = new QuestionObj(questionId,questionVersionLetter,questionVersionNumber,questionName,questionText,descriptionUp,descriptionDown,questionType,questionOptions,imageOption,imageFileName,visualScaleInfo,divId,repeatedSects[i].id,rAttribute,graphicNames,defaultValue,unansweredValue,associatedGroupIds,includeOther,attachedFormIds, "");
			questionsArray.push(ques);
			if ($('#linkDEcheckBox').is(':checked')) { // user want the data element
				dataStructureHandler.setDataElementAssociate(ques.newQuestionDivId,editDataElementName);
			}
		}
	}
	
	
	setCalDependent();  // for edit attribute, when the question was choosed to be calculation element for other question, it can't change the answer type anymore.
	setSkipRuleDependent();
	// set the associate between data element and question
	if($('#linkDEcheckBox').is(':checked')){ // user want the data element
		dataStructureHandler.setDataElementAssociate(question.newQuestionDivId,editDataElementName);
	}
	//create the new question div
	var newQuestionDiv = questionShow(question,attribute);    //  <--------------show different question
	//add the newQuestionDiv to the Section Div
	clearActiveQuestion();
	if ($('div.activeSection .clearboth').length > 0) {
		$('div.activeSection .clearboth').before(newQuestionDiv);
	}
	else {
		$('div.activeSection').append(newQuestionDiv);
	}
	// make the question dynamic
	makeQuestionDynamic(newQuestionDivId);
	//change the format based on what user want
	changeFormate(question,attribute);
	// attach the graphics
	attachGraphics(question);
	// change the section's resize min height
	assignMinHeight($('div.activeSection'));
	QuestionHandler.addQuestion(question);
}

function updateCalculateFormula(oldQId,newId){
	for (var i=0;i<questionsArray.length;i++) {
		var calStr=questionsArray[i].attributeObject.calculation;
		if(calStr != ''){
			var oldQId="Q_"+oldQId;
			calStr=replaceAll(calStr,oldQId,"Q_"+newId);
			questionsArray[i].attributeObject.calculation=calStr;
		}
	}
}


//function for updating a question___Ching Heng
function updateQuestionToForm(questionName,questionId,questionVersionLetter,questionVersionNumber,questionOptions,imageOption,imageFileName,graphicNames,defaultValue,unansweredValue,
		associatedGroupIds,attachedFormIds,hasDecimalPrecision, hasCalDependent, prepopulation, text){
	//alert("calling function updateQuestionToForm() for question id "  + questionId);
	var orgQuestionName = $("#questionName").val();
	var questionName =questionName;
	var questionNameTrim = trim(questionName);
	var questionText=$("#questionText").val();
	var descriptionUp=$("#descriptionUp").val();
	var descriptionDown=$("#descriptionDown").val();
	var questionType=$("#questionType").val();
	var defaultValue=$('#defaultValue').val();
	var unansweredValue=$('#unansweredValue').val();
	var includeOther=$('#includeOther').is(':checked');
	var visualScaleInfo=$('#leftText').val()+parsingSymbol+$('#centerText').val()+parsingSymbol+$('#rightText').val()+parsingSymbol+$('#scaleRangeMinimum').val()+parsingSymbol+$('#scaleRangeMaximum').val()+parsingSymbol+$('#scaleWidth').val()+parsingSymbol+$('#scaleCursor').val();
	
	var oldQId;
	var oldQuestionDivId;
	for (var i = 0; i < questionsArray.length; i++) {
		if (questionsArray[i].newQuestionDivId == editQuestionDivId) {
			var oldQ=questionsArray[i];
			oldQuestionDivId=oldQ.newQuestionDivId;
			oldQId=oldQ.questionId;
			updateCalculateFormula(oldQId,questionId);
			questionsArray.splice(i,1);  // remove old question from question array
		}
	}
	var afterEditQuestionDivId=$('div.activeSection').attr('id')+"_"+questionId+"_"+questionVersionNumber;
	
	var attribute=oldQ.attributeObject;  // get old attribute object
	// attribute.qType=questionType; //question type won't be changed in Edit question
	attribute=setAttributeObject(attribute); // re-set the attribute object
	var q=updateQuestionObj(oldQ,questionId,afterEditQuestionDivId,questionName,questionText,descriptionUp,descriptionDown,questionType,questionVersionLetter,questionVersionNumber,questionOptions,imageOption,imageFileName,graphicNames,visualScaleInfo,defaultValue,unansweredValue,associatedGroupIds,attribute,
			includeOther,attachedFormIds,hasDecimalPrecision, hasCalDependent, prepopulation, "");
	
	// also need to change the other same question
	for (var i = 0; i < questionsArray.length; i++) {
		if (questionsArray[i].questionName==orgQuestionName){
			updateSameQuestionInForm(q,questionsArray[i],i);
		} 
	}
	
	// put the edited question into question array
	questionsArray.push(q);
	
	//alert("The new quesiton obj to be used to update question whole array: \n" + JSON.stringify(q) +"\n\n");
	//re-set all atribute's attCalDependent
	setCalDependent();
	setSkipRuleDependent();
	// set the associate between data element and question
	var repeatedQuestions = QuestionHandler.findAllRepeatedQuestions(q);
	if ($('#linkDEcheckBox').is(':checked')) { // user want the data element
		dataStructureHandler.setDataElementAssociate(q.newQuestionDivId,editDataElementName);
		if (repeatedQuestions.length > 0) {
			for (var i = 0; i < repeatedQuestions.length; i++) {
				var repeatedQ = repeatedQuestions[i];
				dataStructureHandler.setDataElementAssociate(repeatedQ.newQuestionDivId,editDataElementName);
			}
		}
	} else { // user deassociate the dataelement and question
		dataStructureHandler.deAssociateDataElement(q.newQuestionDivId);
		if (repeatedQuestions.length > 0) {
			for (var i = 0; i < repeatedQuestions.length; i++) {
				var repeatedQ = repeatedQuestions[i];
				dataStructureHandler.setDataElementAssociate(repeatedQ.newQuestionDivId);
			}
		}
	}
	
	//create the new question div
	var editQuestionDiv = questionShow(q,attribute);    //  <--------------show different question
	//update the newQuestionDiv to the editQuestionDiv
	changeActiveQuestion($(".activeQuestion").prev(".question"));
	$("#"+oldQuestionDivId).remove();
	if ($(".question").length > 0 && $(".activeQuestion").length < 1) {
		if ($('div.activeSection .question').length>0) {	
			$('div.activeSection .question').eq(0).before(editQuestionDiv);
		} else{
			$('div.activeSection .clearboth').before(editQuestionDiv);
		}
	}
	else if ($(".activeQuestion").length > 0) {
		$(".activeQuestion").after(editQuestionDiv);
	}
	else if ($('div.activeSection .clearboth').length > 0) {
		$('div.activeSection .clearboth').before(editQuestionDiv);
	}
	else {
		$('div.activeSection').append(editQuestionDiv);
	}
	
	changeActiveQuestion($("#"+afterEditQuestionDivId));
	// re-attach graphics
	attachGraphics(q);
	//change the format based on what user want
	changeFormate(q,q.attributeObject);
	
	makeQuestionDynamic(afterEditQuestionDivId);
	
}

function updateSameQuestionInForm(sourceQuestionObj,targetQuestionObject,targetIndex){
	var targetDiv=targetQuestionObject.newQuestionDivId;
	var replaceQdivId=SectionHandler.convertToHTMLId(targetQuestionObject.sectionId)+"_"+sourceQuestionObj.questionId+"_"+sourceQuestionObj.questionVersionNumber;
	// the attribute object should not be update
	// EXCEPT in the case of a repeated child
	// make sure the calculation string is updated for repeated children
	var attrObject = targetQuestionObject.attributeObject;
	var repeatedQuestions = QuestionHandler.findAllRepeatedQuestions(sourceQuestionObj);
	if (repeatedQuestions.length > 0) {
		// this is a repeated parent
		for (var i = 0; i < repeatedQuestions.length; i++) {
			if (repeatedQuestions[i].sectionId == targetQuestionObject.sectionId 
					&& repeatedQuestions[i].questionId == targetQuestionObject.questionId) {
				// the target question is part of a repeated section and we're changing its parent
				attrObject = owl.deepCopy(sourceQuestionObj.attributeObject);
			}
		}
	}
	
	var updatedTargetQuestion=updateQuestionObj(targetQuestionObject,
			                                    sourceQuestionObj.questionId,
			                                    replaceQdivId,
			                                    sourceQuestionObj.questionName,
			                                    sourceQuestionObj.questionText.replace(/'/g, "&#39;"),
			                                    sourceQuestionObj.descriptionUp.replace(/'/g, "&#39;"),
			                                    sourceQuestionObj.descriptionDown.replace(/'/g, "&#39;"),
			                                    sourceQuestionObj.questionType,
			                                    sourceQuestionObj.questionVersionLetter,
			                                    sourceQuestionObj.questionVersionNumber,
			                                    sourceQuestionObj.questionOptions,
			                                    sourceQuestionObj.imageOption,
			                                    sourceQuestionObj.imageFileName,
			                                    sourceQuestionObj.graphicNames,
			                                    sourceQuestionObj.visualScaleInfo,
			                                    sourceQuestionObj.defaultValue,
			                                    sourceQuestionObj.unansweredValue,
			                                    sourceQuestionObj.associatedGroupIds,
			                                    attrObject,
			                                    sourceQuestionObj.includeOther,
			                                    sourceQuestionObj.attachedFormIds,
			                                    sourceQuestionObj.hasDecimalPrecision,
			                                    sourceQuestionObj.hasCalDependent,
			                                    sourceQuestionObj.prepopulation,
			                                    sourceQuestionObj.text
			                                    );
	questionsArray[targetIndex]=updatedTargetQuestion;
	
	// after updating the same questions' attribute, we need to update the calculation in the repeatable question based on its section ID.
	var repeatedQuestions = QuestionHandler.findAllRepeatedQuestions(sourceQuestionObj);
	if (repeatedQuestions.length > 0) {
		var sourceSectionId = SectionHandler.convertToHTMLId(sourceQuestionObj.sectionId);
		for (var i = 0; i < repeatedQuestions.length; i++) {
			var changeInx=QuestionHandler.findQuestionIndex(repeatedQuestions[i].newQuestionDivId);
			var orgCalculation=questionsArray[changeInx].attributeObject.calculation;
			var repeatSectionId = SectionHandler.convertToHTMLId(repeatedQuestions[i].sectionId);
			questionsArray[changeInx].attributeObject.calculation=replaceAll(orgCalculation,sourceSectionId,repeatSectionId);
		}
	}

	// re-create the question div, but no need header...
	var editQuestionDiv = questionShow(updatedTargetQuestion,updatedTargetQuestion.attributeObject);
	$("#"+targetDiv).replaceWith(editQuestionDiv);
	$("#"+replaceQdivId).removeClass('activeQuestion');
	// re-attach graphics
	attachGraphics(updatedTargetQuestion);
	//change the format based on what user want
	changeFormate(updatedTargetQuestion,updatedTargetQuestion.attributeObject);
}


function updateQuestionObj(question,editQId,editQDivId,questionName,questionText,descriptionUp,descriptionDown,questionType,questionVersionLetter,questionVersionNumber, questionOptions,imageOption,imageFileName,graphicNames,visualScaleInfo,defaultValue,unansweredValue,
		associatedGroupIds,attribute,includeOther,attachedFormIds, hasDecimalPrecison, hasCalDependent, prepopulation, text){
	question.questionId=editQId;
	question.newQuestionDivId=editQDivId;
	question.questionName=questionName;
	question.questionVersionLetter=questionVersionLetter;
	question.questionVersionNumber=questionVersionNumber;
	question.questionText =questionText;
	question.descriptionUp  =descriptionUp;
	question.descriptionDown=descriptionDown;
	question.questionType = questionType;
	question.defaultValue=defaultValue;
	question.unansweredValue=unansweredValue;
	question.questionOptions=questionOptions;
	question.imageOption=imageOption;
	question.imageFileName=imageFileName;
	question.graphicNames=graphicNames;
	question.visualScaleInfo=visualScaleInfo;
	question.associatedGroupIds=associatedGroupIds;
	question.attributeObject=attribute;
	question.includeOther=includeOther;
	question.attachedFormIds=attachedFormIds;
	question.hasDecimalPrecision = hasDecimalPrecision;
	question.hasCalDependent = hasCalDependent;
	question.prepopulation = prepopulation;
	question.text = text;
	return question;
}

function assignMinHeight(sect) {
	if (!sect.hasClass("section")) {
		sect = sect.parents(".section");
	}

	sect.css("height", "auto");
}

function attachGraphics(question){
	var graphicNames=question.graphicNames;
	var graphicHTML='';
	if (graphicNames != 'undefined' && graphicNames != null && graphicNames!=''){
		for (var i=0;i<graphicNames.length;i++){
			graphicHTML+="&nbsp;<img width='100' height='100' border='0' src='<s:property value='#imageRoot'/>/questionimages/"+graphicNames[i]+"' onclick=goImgWin('<s:property value='#imageRoot'/>/questionimages/"+graphicNames[i]+"',240,302,100,50)>";
		}
		var graphicShow =
						"<table frame='box' >"+
							"<tr aling='left'>"+
								"<td>"+
									"Thumbnail (click to see original)"+
								"<td>"+
							"<tr>"+
							"<tr aling='left'>"+
								"<td>"+
									graphicHTML+
								"</td>"+
							"</tr>"+
						"</table>";
		$('#'+question.newQuestionDivId).append(graphicShow);
	}
}

function goImgWin(myImage, myWidth, myHeight, origLeft, origTop) {
	   myHeight += 24;
	   myWidth += 24;
	   TheImgWin = openPopup(myImage,'image','height=' +
	                                myHeight + ',width=' + myWidth +
	                                ',toolbar=no,directories=no,status=no,' +
	                                'menubar=no,scrollbars=no,resizable=yes');
	   TheImgWin.moveTo(origLeft,origTop);
	   TheImgWin.focus();
}

function calculateSectionMinHeight() {
	var height = 0;
	$("div.activeSection .question").each(function() {
		// for each question, get its actual height, add 2 (for borders)
		// and add to the total height of the section
		height += $(this).height() + 2;
	});
	// the height of the section header is just under 30
	// TODO: calculate the section header dynamically because we're adding a description

	// calculate the section header height and add a padding
	return height + $("div.activeSection table").height() + 10;
}

//function for changing the question formate based on what user want__Ching Heng
function changeFormate(question,attribute){
	$('#font_'+question.newQuestionDivId).attr('face',attribute.fontFace);
	if(attribute.fontSize!=0){
		$('#font_'+question.newQuestionDivId).attr('size',attribute.fontSize);
	}
	$('#font_'+question.newQuestionDivId).attr('color',attribute.color);
	
	$('.font_'+question.newQuestionDivId+'_option').attr('face',attribute.fontFace);
	if(attribute.fontSize!=0){
		$('.font_'+question.newQuestionDivId+'_option').attr('size',attribute.fontSize);
	}
	$('.font_'+question.newQuestionDivId+'_option').attr('color',attribute.color);
	
	$('#'+question.newQuestionDivId+"_body").attr('align',attribute.align);

	$('#'+question.questionId).css({
		'vertical-align':attribute.vAlign
	});
	if (attribute.indent!=null && attribute.indent!='undefined' && attribute.indent!=''){
		var preRef = $('#pre_'+question.newQuestionDivId);
		for (var i=0;i<attribute.indent;i++){
			preRef.html(preRef.html()+'&nbsp;');	
		}
	}

	if(attribute.required=='true' || attribute.required==true){
		$('#pre_'+question.newQuestionDivId).prepend('<font color="red">*</font>');
	}
}

function removeSection(sectionID,fMode) {
	if (confirm("Are you sure you want to delete this section? Any attached questions will be removed also.")) {
		//if we are in create mode, then all we need to do is remove the section and its associated questions from their arrays
		if (!checkSectionCanBeDeleted(sectionID)){
			var s = SectionHandler.findSection(sectionID);
			var sections = SectionHandler.findAllRepeatableChildren(sectionID);
			sections.push(s);
			for (var j = 0; j < sections.length; j++) {
				//first remove questions attached to this section
				var questions = sections[j].getQuestions();
				for (var k = 0; k < questions.length; k++) {
					QuestionHandler.removeQuestion(questions[k].newQuestionDivId, true);
				}
				//now remove section
				if(s.existingSection) {
					existingSectionIdsToDeleteArray.push(sections[j].id);
				}
				SectionHandler.removeSection(sections[j].id);
				//need to remove the questions...for later
			}
		} else {
			alert('This section contains calculate dependent question, please delete those questions first');
		}
	}
}

function checkSectionCanBeDeleted(sectionID){
	var stopFlag=false;
	for (var i = 0; i < questionsArray.length; i++) {
		//alert(questionsArray[i].attributeObject.calDependent+','+questionsArray[i].sectionId+','+sectionID)
		var qSid = questionsArray[i].sectionId;
		if(qSid.indexOf("S_") < 0 ) {
			qSid = 'S_'+questionsArray[i].sectionId;
		}

		if (questionsArray[i].attributeObject.calDependent==true && qSid==sectionID){
			stopFlag=true;
			break;
		}
		if (questionsArray[i].attributeObject.skipRuleDependent==true && qSid==sectionID){
			stopFlag=true;
			break;
		}
	}
	
	return stopFlag;
}

function clearActiveSection() {
	$('.activeSection').removeClass("activeSection");
}

function clearActiveQuestion() {
	$(".activeQuestion").removeClass("activeQuestion");
}

//ajax call and response for adding form info
function doAddEditFormInfoAjaxPost(fMode, action) {
	// the user has started form creation, disable session timeout
	clearLogoutTimeout();
	
	if (fMode == "edit") {
		formInfoMode = "edit";

		var dsName;
		var dsVersion;
		if( $('#dataStructureName').val()!="") { //datastructurename can be empty if it was imported
			dsName = $('#dataStructureName').val() + "(" + $('#dataStructureVersion').val()+ ")";
			var $inputs = IDT.getAllInputs($table);
			if ($inputs.filter($('[value="'+dsName+'"]')).length > 0) {
				IDT.addSelectedOptionValue($table, dsName);
				
			}
			IDT.dataTables[$table.attr("id")].fnDraw();
		}
	}

	var params = $('#createEditForm form').serialize();
	var formInfoURL;
	if (formInfoMode == "add") {
		formInfoURL = "<s:property value="#webRoot"/>/form/saveNewForm.action?action=process_add_forminfo";
	} else {
		formInfoURL = "<s:property value="#webRoot"/>/form/saveEditForm.action?action=process_edit_forminfo&formMode=" + fMode;
	}
	
	$.ajax({
		type: "POST",
		url: formInfoURL,
		data: params,
        beforeSend: function() {
        	
         	var errorContainer = document.getElementById("errorContainer");
        	errorContainer.innerHTML="";
        	var dataStructName = $('#dataStructureName').val();
        	var selected_fs_arr = IbisDataTables.getSelectedOptions($("#dsList")); 
	
        	if (selected_fs_arr.length == 0) {	
        		var errorString = "<s:text name="errors.text"/><b>Form Structure is required.</b>";
        		$.ibisMessaging("primary", "error", errorString, {container: "#errorContainer"});
        		$("#errorContainer").show();
        		return false;
        	} else {
        		errorContainer.style.display="none";
        		errorContainer.innerHTML="";
        	}   
        	
        	var formName = document.getElementById("fname");
        	var formNameTrim = trim(formName.value);
        	if (formNameTrim=="") {
        		var errorString = "<s:text name="errors.text"/><b>Form Name is required.</b>";
        		$.ibisMessaging("primary", "error", errorString, {container: "#errorContainer"});
        		$("#errorContainer").show();
        		return false;
        	} else {
        		if (!proceedToBuildForm) {
	        		var selectedDataStructureName = $('#dataStructureName').val() + "(" + $('#dataStructureVersion').val() + ")";
	        		if(originalDataStructureName != "" && selectedDataStructureName != originalDataStructureName) {
	        			if (confirm("You have selected a different form structure to associate this form to. \n All data element associations with questions will be lost. \n You will need to reassociate questions to data elements. \n Do you wish to continue?")) {
	        				return true;
	        			} else {
	        				return false;
	        			}
	        		}
        		} else {
        			errorContainer.style.display="none";
            		errorContainer.innerHTML="";
            		return true;
        		}
        	}
        },
        
		success: function(response) {
			//alert("doAddEditFormInfoAjaxPost(): the post Parameters:\n" +params + "\n\nResponse:\n" + response);
			if (response != "errors.duplicate.form" && response != "errors.required.data.elements" && response != "errors.web.service" && response != "webserviceException") {
				if (action == "continue") {
					proceedToBuildForm = true;
					
					$("#formInfoDiv").show();
					containerWidth = $("#formInfoDiv").width();
					
					var dataStructureName = document.getElementById("dataStructureName");
					var dStructureName = document.getElementById("dStructureName");
					dStructureName.innerHTML =  $('#dataStructureName').val() + "(" + $('#dataStructureVersion').val()+ ")";
					
					var dataStructureNameTR = document.getElementById("dataStructureNameTR");
					dataStructureNameTR.style.display="block";
 
					$("#addEditFormInfoFancyBox").dialog("close");
						var splits = response.split("::");
						var formName = splits[0];
						var formStatus = splits[1];
						var formID = splits[2];
						var isDataSpringString = splits[3];
						var dataStructureName = splits[4];
						
						/* added by Ching Heng for data structure and data element */
						var dataStrucutrJSON=JSON.parse(splits[5]);
						//alert(splits[5]);
						var dataStructureName=dataStrucutrJSON.dataStructName;
						var dataStructureVersion=dataStrucutrJSON.dataStructVersion;
						var dataElementJsonArray = dataStrucutrJSON["dataElements"];
						var dataElementsArray = new Array();
						
						var repeatableGroupNamesJSONArr = dataStrucutrJSON["repeatableGroupNames"];
						var repeatableGroupNamesArr = new Array();
						
						var repeatableGroupsJsonArray = dataStrucutrJSON["repeatableGroupList"];
						var repeatableGroupsArray = new Array();
						
						for (var t=0; t<repeatableGroupsJsonArray.length; t++) {
							var rgName = dataStrucutrJSON["repeatableGroupList"][t].name;
							var rgThreshold =  dataStrucutrJSON["repeatableGroupList"][t].threshold;
							var rgType = dataStrucutrJSON["repeatableGroupList"][t].type;
							var RGObj = new RepeatableGroupObject(rgName,rgThreshold,rgType);
							
							repeatableGroupsArray.push(RGObj);
						}
						
						for (var t=0; t<dataElementJsonArray.length; t++) { // set the data element object what we want
							var dataElementName=dataStrucutrJSON["dataElements"][t].name;
							var dataElementType=dataStrucutrJSON["dataElements"][t].type.value;
							var requiredType=dataStrucutrJSON["dataElements"][t].requiredType;
							var description=dataStrucutrJSON["dataElements"][t].shortDescription;
							var suggestedQuestion=dataStrucutrJSON["dataElements"][t].suggestedQuestion;
							var restrictionId=dataStrucutrJSON["dataElements"][t].restrictions.id;
							var restrictionName=dataStrucutrJSON["dataElements"][t].restrictions.value;
							var valueRangeList=dataStrucutrJSON["dataElements"][t].valueRangeList;
							var size=dataStrucutrJSON["dataElements"][t].size;
							var max=dataStrucutrJSON["dataElements"][t].maximumValue;
							var min=dataStrucutrJSON["dataElements"][t].minimumValue;
							var isGroupRepeatable=dataStrucutrJSON["dataElements"][t].groupRepeatable;
							var order=dataStrucutrJSON["dataElements"][t].order;
							var title=dataStrucutrJSON["dataElements"][t].title;
							var DEObj=new DataElementObject(dataElementName,
									                        dataElementType,
									                        requiredType,
									                        description,
									                        suggestedQuestion,
									                        restrictionId,
									                        restrictionName,
									                        valueRangeList,
									                        size,
									                        max,
									                        min,
									                        false,
									                        isGroupRepeatable,
									                        order,
									                        title);
							dataElementsArray.push(DEObj);
						}
						
						for (var t=0;t<repeatableGroupNamesJSONArr.length;t++) { 
							repeatableGroupNamesArr.push(repeatableGroupNamesJSONArr[t]);
						}

						var dsobj=new DataStructureObject(dataStructureName,dataStructureVersion,dataElementsArray,repeatableGroupNamesArr,repeatableGroupsArray);
						DSObj=dsobj;
						//========================================================
						
						if (isDataSpringString == "true") {
							isDataSpring = true;
						} else {
							isDataSpring = false;
						}
						if (fMode == "edit") {
							if ($(".section").length < 1) {
								var sectionsJSONArrJSONString = splits[6];
								if(sectionsJSONArrJSONString != "") {
									sectionsArray = JSON.parse(sectionsJSONArrJSONString);
								}
								var questionsJSONArrJSONString = splits[7];
								if(questionsJSONArrJSONString != "") {
									questionsArray = JSON.parse(questionsJSONArrJSONString); // reget all questions
									// set all the data elements associate flag
									dataStructureHandler.initiateDataElement(questionsArray);
								}
								
								// add the sections and questions only if the form isn't already drawn
								addAllSectionsAndQuestions();
								
								setCalDependent();
								setSkipRuleDependent();
							}
						}
						
						$('#formid').val(formID);
						$('#formBuildFormId').val(formID);
						var addSectionFancyBoxBtn = document.getElementById("addSectionFancyBoxBtn");
						addSectionFancyBoxBtn.disabled=false;
						var formNameTD = document.getElementById("formNameTD");
						var formStatusTD = document.getElementById("formStatusTD");
						var formStructureTD = document.getElementById("formStructureTD");
						formNameTD.innerHTML=formName;
						formStatusTD.innerHTML=formStatus;
						formStructureTD.innerHTML=dataStructureName+"("+dataStructureVersion+")";	

				} else {					
					$("#addEditFormInfoFancyBox").dialog("close");
					var f = document.getElementById("formName");
					var selected = f.options[f.selectedIndex].value;

					var formTypeToDisplay="nonSubject";
					if (selected == "Subject") {
						formTypeToDisplay = "Subject";
					}

					var splits = response.split("::");
					var nowCopyright = splits[8];
					var orgCopyright = splits[9];
				
					var url = '<s:property value="#webRoot"/>/form/formHome.action?message=formSaved&formTypeToDisplay=' + formTypeToDisplay +"&formId="+document.formForm.formid.value +"&nowCopyright="+nowCopyright +"&orgCopyright="+orgCopyright;

					redirectWithReferrer(url);
				}
			} else {
				
				if (response == "errors.duplicate.form") {
					$.ibisMessaging(
							"primary", 
							"error", 
							"<s:text name="errors.text"/><b>Please enter a different form name. The form name must be unique in the study.</b>",
							{
								container: "#errorContainer"
							});
					$("#errorContainer").show();	
				}else if(response == "errors.required.data.elements") {
					$.ibisMessaging(
							"primary", 
							"error", 
							"<s:text name="errors.text"/><b>The form can not be activated because not all required data elements are associated to questions on the form.</b>",
							{
								container: "#errorContainer"
							});
					$("#errorContainer").show();
				}else if(response == "errors.web.service") {
					$.ibisMessaging(
							"primary", 
							"error", 
							"<s:text name='errors.text'/><b><s:text name='form.forms.formActivation.webserviceException'><s:param>" + fName + "</s:param></s:text></b>",
							{
								container: "#errorContainer"
							});
					$("#errorContainer").show();
				}else if(response == "webserviceException") {
					$.ibisMessaging(
							"primary", 
							"info", 
							"There was a problem with retrieving Form Structures from the web service.",
							{
								container: "#errorContainer"
							});
					$("#errorContainer").show();
				}
			}
		},
		error: function(e) {
			alert("error" + e );
		}
	});
} 


/**
 * This function uses the sectionsarray and questionsarray to build the entire form.
 * It should ONLY be called on edit when the form is empty
 */
function addAllSectionsAndQuestions() {
	addAllSections();
	addAllQuestions();
	// enforce equal width of all sections on the same row
	editActionEnforceWidth();
	// enforce equal height of all secitons on the same row
	$(".section").each(function() {
		DynamicElements.enforceSectionRows(null, {item: $(this)});
	});

	setupDynamicObjects();
}

/**
 * Adds all sections in the section array to the page.
 * The last section will be the active section
 */
function addAllSections() {
	// sort the sections array in order of row (primary), column (primary) ascending
	// this could take some time
	sectionsArray.sort(function(a,b){
		if (a.row != b.row) {
			return a.row - b.row;
		}
		return a.col - b.col;
	});
	
	var currentRow = 1;
	var currentCol = 1;
	
	for (var i = 0; i < sectionsArray.length; i++) {
		var newSection = new SectionObj(
				"S_"+sectionsArray[i].id, 
				sectionsArray[i].name,
				sectionsArray[i].description,
				sectionsArray[i].isCollapsable,
				sectionsArray[i].ifHasResponseImage,
				sectionsArray[i].isRepeatable,
				sectionsArray[i].initRepeatedSecs,
				sectionsArray[i].maxRepeatedSecs,
				sectionsArray[i].repeatedSectionParent,
				sectionsArray[i].repeatableGroupName);
		
		newSection.row = sectionsArray[i].row;
		newSection.col = sectionsArray[i].col;
		newSection.existingSection = sectionsArray[i].existingSection;
		newSection.isCollapsable =  (typeof sectionsArray[i].isCollapsable == "undefined") ? false : sectionsArray[i].isCollapsable;
		newSection.ifHasResponseImage = sectionsArray[i].ifHasResponseImage;
		
		newSection.repeatedSectionParent = 
			(String(newSection.repeatedSectionParent).indexOf("S_") < 0 && String(newSection.repeatedSectionParent) != "-1") ? 
					"S_" + newSection.repeatedSectionParent : 
					newSection.repeatedSectionParent;
		
		// copy the new seciton object into the array
		sectionsArray[i] = newSection;
		var sectionDiv = sectionsArray[i].getDiv();
		clearActiveSection();
		var endOfContainer = $('#formSectionsDiv > .clearboth');
		endOfContainer.before(sectionDiv);
	}	
}

function editActionEnforceWidth() {
	// we know the sections are in order, so work BACKWARD
	for (var i = sectionsArray.length-1; i >= 0; i--) {
		// we only need to do anything if this col is > 1
		if (sectionsArray[i].col > 1) {
			// get the previous sectionsArray[i].col-1 sections and size them all
			var arrRow = new Array();
			do {
				arrRow.push(sectionsArray[i]);
				i--;
			} while (sectionsArray[i+1].col > 1);
			
			i++; // correct for the last i--
			
			var eachWidth = ((containerWidth - 2 - (12 * arrRow.length)) / arrRow.length);
			for (var j = 0; j < arrRow.length; j++) {
				$("#"+arrRow[j].id).css("width", eachWidth+"px");
			}
		}
	}
}

function editActionEnforceHeight() {
	var sections = $(".section");
	var rowArray = new Array();
	var maxHeight = 0;
	
	sections.each(function() {
	});
}

/**
 * Add all questions to the form for editing
 */
function addAllQuestions() {
	// sort the questions array in order of section (primary), order (primary) ascending
	// this could take some time
	questionsArray.sort(function(a,b){
		if (a.sectionId != b.sectionId) {
			return a.sectionId - b.sectionId;
		}
		return a.questionOrder - b.questionOrder;
	});
	 
	for (var i = 0; i < questionsArray.length; i++) {
		if (questionsArray[i].questionType == "Textbox") {questionsArray[i].questionType = <%=QuestionType.TEXTBOX.getValue()%>;}
		else if (questionsArray[i].questionType == "Textarea") {questionsArray[i].questionType = <%=QuestionType.TEXTAREA.getValue()%>;}
		else if (questionsArray[i].questionType == "Select") {questionsArray[i].questionType = <%=QuestionType.SELECT.getValue()%>;}
		else if (questionsArray[i].questionType == "Radio") {questionsArray[i].questionType = <%=QuestionType.RADIO.getValue()%>;}
		else if (questionsArray[i].questionType == "Multi-Select") {questionsArray[i].questionType = <%=QuestionType.MULTI_SELECT.getValue()%>;}
		else if (questionsArray[i].questionType == "Checkbox") {questionsArray[i].questionType = <%=QuestionType.CHECKBOX.getValue()%>;}
		else if (questionsArray[i].questionType == "Image Map") {questionsArray[i].questionType = <%=QuestionType.IMAGE_MAP.getValue()%>;}
		else if (questionsArray[i].questionType == "Visual Scale") {questionsArray[i].questionType = <%=QuestionType.VISUAL_SCALE.getValue()%>;}
		else if (questionsArray[i].questionType == "File") {questionsArray[i].questionType = <%=QuestionType.File.getValue()%>;}
		else if (questionsArray[i].questionType == "Textblock") {questionsArray[i].questionType = <%=QuestionType.TEXT_BLOCK.getValue()%>;}
		questionsArray[i].newQuestionDivId = "S_"+questionsArray[i].newQuestionDivId + "_" + questionsArray[i].questionVersionNumber;		
		
		var questionDiv = questionShow(questionsArray[i], questionsArray[i].attributeObject);
		clearActiveQuestion();
		$('#S_'+ questionsArray[i].sectionId +' > .clearboth').before(questionDiv);
		changeFormate(questionsArray[i],questionsArray[i].attributeObject);
		attachGraphics(questionsArray[i]);
	}
}

function stillLock(){ //if there is any error, the feilds were locked must still be locked...
	if(questionInfoMode!='edit'){ // add or search
		if(questionInfoMode=='search'){
			$('#questionName').attr('disabled',true);
			$('#questionType').attr('disabled',true);
		}
		var dataElementName=$('input:radio:checked[name="dataElementRadio"]:checked').attr('id');
		var questionType=$('input[name="selectQuestionId"]:checked').val();
	}
	else{// edit
		$('#questionName').attr('disabled',true);
		switchInterfaceByQuestionType(editQuestionType);
		checkRangeOperator();
	}
}

//ajax call and response for adding question __ Ching Heng
var stopFalg=false;
var errorString='';
function doAddEditQuestionAjaxPost(questionInfoURL) {
	
	var text = $('#questionText').val().replace(/'/g, "&#39;").replace(/\n/g, "\r\n");
	if (text.length > 255){
		// Since we truncate question text for the end user when it's over the limit, it doesn't make 
		// sense to show the error message again. We need to re-truncate the text to the right size 
		// since we may have replaced certain special characters. 
		text = text.substr(0, 255);  
	}
	$('#questionText').val(text);

	document.getElementById("descriptionUp").value = document.getElementById("descriptionUp").value.replace(/'/g, "&#39;");
	document.getElementById("descriptionDown").value = document.getElementById("descriptionDown").value.replace(/'/g, "&#39;");
	var params = $('#addEditQuestion form').serialize();
	
	$.ajax({
		type:"post",
		url:questionInfoURL,
		data:params,
		beforeSend: function(){
			$('#questionValidationErrorsDiv').html('');
			var questionName = document.getElementById("questionName");
			var questionNameTrim = trim(questionName.value);
        	var questionText = document.getElementById("questionText");
        	var questionTextTrim = trim(questionText.value.replace(/'/g, "&#39;"));
        	var descriptionUp  = document.getElementById("descriptionUp");
        	var descriptionUpTrim = trim(descriptionUp.value.replace(/'/g, "&#39;"));
        	var descriptionDown= document.getElementById("descriptionDown");
        	var descriptionDownTrim = trim(descriptionDown.value.replace(/'/g, "&#39;"));
       	
        	var emailReg = /^([\w-\.]+@([\w-]+\.)+[\w-]{2,4})?$/;
        	var intRegex = /^\d+$/;
        	var minChar=parseInt($('#minCharacters').val());
        	var maxChar=parseInt($('#maxCharacters').val());
        	errorString='';
        	stopFalg=false;

        	if (questionNameTrim==""||questionTextTrim=="") {
        		errorString += "<font color='red'>Question Name and Question Text is required.</font><br>";        		
        		stopFalg=true;
        	}
        	
        	if (questionNameTrim.length>0) {
    			var iChars
    			if (questionInfoMode=='add') {
    				iChars = "!@#$%^&*()+=-[]\\\';,./{}|\":<>?";
    			} else {
    				iChars = "!@#$%^&*()+=[]\\\';,./{}|\":<>?"; // no "-" character
    			}
    			var specialFlag=false;
    			for (var i = 0; i < $('#questionName').val().length; i++) {
    			    if (iChars.indexOf($('#questionName').val().charAt(i)) != -1) {
    			    	specialFlag=true;
    			    }
    			}
    			if (specialFlag) {
			    	errorString += "<font color='red'>Question name can not contain any special characters.</font><br>"; 
			    	stopFalg=true;
    			}
    			
    			if (/\s/g.test($('#questionName').val())) {
    	                errorString += "<font color='red'>Question name can not contain any white space.</font><br>";
    	                stopFalg=true;
    	        }
    		}

        	if($('#questionType').val()==<%=QuestionType.RADIO.getValue()%> || $('#questionType').val()==<%=QuestionType.MULTI_SELECT.getValue()%>
		   		|| $('#questionType').val()==<%=QuestionType.SELECT.getValue()%> || $('#questionType').val()==<%=QuestionType.CHECKBOX.getValue()%> ) {
			
				var optionChoiceTrim = trim($('#optionChoice').val());
				if (optionChoiceTrim != "") {
	        		errorString += "<font color='red'><s:text name="questionLibrary.optionChoice.error" /></font><br>";        		
	        		stopFalg=true;
	        	}
			}
		        	
        	if ($('#_et_toAddr').val()!='' || $('#_et_ccAddr').val()!=''){  // Email validation
        		if (!emailReg.test($('#_et_toAddr').val()) || !emailReg.test($('#_et_ccAddr').val())) {
    				errorString += "<font color='red'>Email Address is not a valid email address; please specify only valid email addresses.</font><br>";
    				stopFalg=true;
    			}
        		if($('#_et_answers').val()==null){
        			errorString += "<font color='red'>When creating an email trigger, answers to activate the trigger are required.</font><br>";
    				stopFalg=true;
        		}
        	}
        	
        	if(!intRegex.test($('#scaleRangeMinimum').val())){
        		errorString += "<font color='red'>Scale Range Minimum for Visual Scale should be Integer.</font><br>";
				stopFalg=true;
        	}
        	
        	if(!intRegex.test($('#scaleRangeMaximum').val())){
        		errorString += "<font color='red'>Scale Range Maximum for Visual Scale should be Integer.</font><br>";
				stopFalg=true;
        	}
        	
        	if(!intRegex.test($('#scaleWidth').val())){
        		errorString += "<font color='red'>Scale Width for Visual Scale should be Integer.</font><br>";
				stopFalg=true;
        	}
        	
        	if($('#defaultValue').val()!=''){
        		if($('#maxCharacters').val()!=''){
	        		if($('#defaultValue').val().length > maxChar){
	        			errorString += "<font color='red'>Please enter a default value that is not greater than the Maximum Characters specified.</font><br>";
	        			stopFalg=true;
	        		}
        		}
        		if ($('#minCharacters').val()!=''){
	        		if (minChar > $('#defaultValue').val().length){
	        			errorString += "<font color='red'>Please enter a default value that is not less than the Minimum Characters specified.</font><br>";
	        			stopFalg=true;
	        		}
        		}
        		
        		
        		if($('#questionType').val()==<%=QuestionType.VISUAL_SCALE.getValue()%>){
        			/* the default value needs to be numeric when question type is visual scale */
        			var dV=parseInt($('#defaultValue').val());
        			if (isNaN(dV)|| $('#defaultValue').val().indexOf('.')>0){
        				errorString += "<font color='red'>The default value needs to be numeric when the question type is visual scale.</font><br>";
            			stopFalg=true;
        			}
        			if(dV<$('#scaleRangeMinimum').val()){
        				errorString += "<font color='red'>The default value can not less than Scale Range Minimum.</font><br>";
            			stopFalg=true;
        			}
        			if(dV>$('#scaleRangeMaximum').val()){
        				errorString += "<font color='red'>The default value can not greater than Scale Range Maximum.</font><br>";
            			stopFalg=true;
        			}
        		}
        	}

        	if($('#required option:selected').val()=='true'){
        		if($('#questionType').val()==<%=QuestionType.TEXTAREA.getValue()%> || $('#questionType').val()==<%=QuestionType.TEXTBOX.getValue()%>){
        			if($('#answerType option:selected').val()=='1'){
        				if($('#minCharacters').val()<1){
        					errorString += "<font color='red'>Once the Answer Required set to Yes, Minimum Characters need to set greater than 0.</font><br>";
                			stopFalg=true;
        				}else if($('#maxCharacters').val()<1){
        					errorString += "<font color='red'>Once the Answer Required set to Yes, Maximum Characters need to set greater than 0.</font><br>";
                			stopFalg=true;
        				}else{}
        			}
        		}
        	}
        	
        	if($('#minCharacters').val()!='' || $('#maxCharacters').val()!=''){// min MAX character validation
        		if($('#minCharacters').val()<0 || !intRegex.test($('#minCharacters').val()) || $('#maxCharacters').val()<0 || !intRegex.test($('#maxCharacters').val())){
        			errorString += "<font color='red'>Minimum/Maximum Characters in the Validation part must be an Positive Integer.</font><br>";
        			stopFalg=true;
        		}				
        	}
        	if(minChar>maxChar){
				errorString += "<font color='red'>Please enter a value for the Maximum Characters greater or equal to the value of Minimum Characters.</font><br>";
				stopFalg=true;
			}
        	if(maxChar>4000){
				errorString += "<font color='red'>Maximum Characters can not be greater than 4000.</font><br>";
				stopFalg=true;
			}
        	
        	
        	//preopopulation precision
        	if($('#prepopulation').is(':checked')){
        		if($('#prepopulationValue').val() == 'none') {
        			errorString += "<font color='red'><s:text name="form.forms.prepopulationError" /></font><br>";
        			stopFalg=true;	
        		}else {
        			//now check to see if field is set to proper type is set:
        		    // answer type:string   prepopulation value: primary site, visit type or guid
        		    // answer type:date     preopoulatio value: visit date
        		    // answer type:datetime prepopulation value: visit date
        		    if($('#questionType').val() == <%=QuestionType.TEXTBOX.getValue()%>) {
	        		    if($('#answerType').val() == '1' ) { //string
	        		    	if($('#prepopulationValue').val() == '<%=CtdbConstants.PREPOPULATION_VISITDATE%>') { 
	        		    		errorString += "<font color='red'><s:text name="form.forms.textboxPrepopulationError1" /></font><br>";
	                			stopFalg=true;
	        		    	}
	        		    }else if($('#answerType').val() == '3'  || $('#answerType').val() == '4') { //date, datetime
	        		    	if($('#prepopulationValue').val() != '<%=CtdbConstants.PREPOPULATION_VISITDATE%>') {
	        		    		errorString += "<font color='red'><s:text name="form.forms.textboxPrepopulationError2" /></font><br>";
	                			stopFalg=true;
	        		    	}
	        		    }
        			}else if($('#questionType').val() == <%=QuestionType.SELECT.getValue()%>) { // select
        				if($('#prepopulationValue').val() == '<%=CtdbConstants.PREPOPULATION_VISITDATE%>' || $('#prepopulationValue').val() == '<%=CtdbConstants.PREPOPULATION_GUID%>'  ) {
        		    		errorString += "<font color='red'><s:text name="form.forms.selectPrepopulationError" /></font><br>";
                			stopFalg=true;
        		    	}
        			}
        		}
        	}

        	
        	if($('#skipRuleOperatorType').val() < 0){
        		if($('#skipRuleType').val()<0){
        			
        			var count=0;
            		$('input[name=questionToSkip]:checked').each(function(i){
            			var sectionQuestionId=$(this).val();
            			var questionId=sectionQuestionId.split("_Q_")[1];
            			var tempQuestioObj=QuestionHandler.findQuestionById(questionId);
            			var sectionId=SectionHandler.convertToHTMLId(sectionQuestionId.split("_Q_")[0]);
            			// we can have duplicate question in same form, that means the question id is not unique, so we need to rebuild the question div id.
            			var QuestioObj=QuestionHandler.findQuestion(sectionId+'_'+questionId+'_'+tempQuestioObj.questionVersionNumber);
            			if(QuestioObj.attributeObject.required=='true' && $('#skipRuleType').val()==2){
            				errorString += "<font color='red'>The question[name = "+QuestioObj.questionName+"] is answer required in validation, it can't set to disabled in skip rule</font><br>";
            				stopFalg=true;
            			}
            			count++;
            		});
            		
            		if(count>0){
            			errorString += "<font color='red'>Please unselect questions to skip or set up a crrect skip rule.</font><br>";
            			stopFalg=true;
            		}
         		} else {
        			errorString += "<font color='red'>Please select a skip rule operator type.</font><br>";
        			stopFalg=true;
        		}
        	}
        	
        	
        	if($('#skipRuleOperatorType').val() >0){
        		if($('#skipRuleOperatorType').val() == 1){ // it is equal
            		if(trim($('#skipRuleEquals').val())==''){
            			errorString += "<font color='red'><s:text name="questionlibrary.skipRule.equalsValueError" /></font><br>";
            			stopFalg=true;	
            		}else {
            			var srEquals = trim($('#skipRuleEquals').val());
            			if(srEquals.indexOf("|") == 0) {
            				errorString += "<font color='red'><s:text name="questionlibrary.skipRule.equalsBeginPipeError" /></font><br>";
                			stopFalg=true;	
            			}
            			if(srEquals.indexOf("|") == srEquals.length - 1) {
            				errorString += "<font color='red'><s:text name="questionlibrary.skipRule.equalsEndsPipeError" /></font><br>";
                			stopFalg=true;	
            			}
            		}
            	}
        		
        		if($('#skipRuleOperatorType').val() == 4){ // it is contains
            		if(trim($('#skipRuleEquals').val())==''){
            			errorString += "<font color='red'><s:text name="questionlibrary.skipRule.containsValueError" /></font><br>";
            			stopFalg=true;	
            		}else {
            			var srEquals = trim($('#skipRuleEquals').val());
            			if(srEquals.indexOf("|") == 0) {
            				errorString += "<font color='red'><s:text name="questionlibrary.skipRule.containsBeginPipeError" /></font><br>";
                			stopFalg=true;	
            			}
            			if(srEquals.indexOf("|") == srEquals.length - 1) {
            				errorString += "<font color='red'><s:text name="questionlibrary.skipRule.containsEndsPipeError" /><</font><br>";
                			stopFalg=true;	
            			}
            			
            		}
        		
            		
            	}
        		if($('#skipRuleType').val()<0){
        			errorString += "<font color='red'>Please specify a skip rule to associate with the skip rule operator in the skip rules tab.</font><br>";
        			stopFalg=true;
        		}
        		var count=0;
        		$('input[name=questionToSkip]:checked').each(function(i){
        			var sectionQuestionId=$(this).val();
        			var questionId=sectionQuestionId.split("_Q_")[1];
        			var tempQuestioObj=QuestionHandler.findQuestionById(questionId);
        			var sectionId=SectionHandler.convertToHTMLId(sectionQuestionId.split("_Q_")[0]);
        			// we can have duplicate question in same form, that means the question id is not unique, so we need to rebuild the question div id.
        			var QuestioObj=QuestionHandler.findQuestion(sectionId+'_'+questionId+'_'+tempQuestioObj.questionVersionNumber);
        			if(QuestioObj.attributeObject.required=='true' && $('#skipRuleType').val()==2){
        				errorString += "<font color='red'>The question[name = "+QuestioObj.questionName+"] is answer required in validation, it can't set to disabled in skip rule</font><br>";
        				stopFalg=true;
        			}
        			count++;
        		});
        		if(count==0){
        			errorString += "<font color='red'>Please assign questions to skip in the skip rules tab.</font><br>";
        			stopFalg=true;
        		}
        	}
        	
        	if($('#indent').val()!=''){
        		if($('#indent').val()<0 || !(intRegex.test($('#indent').val()))){
        			errorString += "<font color='red'>Indent value in the Format part must be a Positive Integer.</font><br>";
        			stopFalg=true;
        		}
        		if($('#indent').val()>50){
        			errorString += "<font color='red'>Indent value in the Format part must less than 50.</font><br>";
        			stopFalg=true;
        		}
        	}

        	if($('#rangeOperator').val()>0){
        		
        		var msg=checkRange();
        		if(typeof(msg)!='undefined'){
        			errorString += "<font color='red'>"+msg+"</font><br>";
        			stopFalg=true;
        		}
        	}
        
        	var fileCheckFlag=false;
        	var ext = $('#graphicFr').contents().find('.fileInput').each(function(index){
        		var extension=$(this).val().split('.').pop().toLowerCase();
        		if(extension!=''){
            		if($.inArray(extension, ['gif','jpg','jpeg']) == -1) {
            			if(index!=0){fileCheckFlag=true;}
                	}
                	//alert(index+":"+fileCheckFlag);	
        		}
        	});
        	if(fileCheckFlag){
        		errorString += "<font color='red'> One of a image file is not of type .jpg or .gif</font><br>";
            	stopFalg=true;
        	}
        	        	
        	testFormular();   //test for calulcation formula
        	testFormularC();  //test for unit conversion formula
        	
        	if($('#answerType').val()>"2"){
        		if( trim($("#window").text()).length>0 && $('#conversionFactor').val()==<%=Integer.MIN_VALUE%>){
        			errorString += "<font color='red'> Please choose a conversion factor.</font><br>";
                	stopFalg=true;
        		}
        	}
        	
        	if($('#linkDEcheckBox').is(':checked')){ // user want the data element
        		if(!$('input:radio:checked[name="dataElementRadio"]:checked').val()){
        			errorString += "<font color='red'> Please select a data element when link data element.</font><br>";
                	stopFalg=true;
        		}
        	}

        	if(stopFalg){
        		$('#questionValidationErrorsDiv').html('');
        		$.ibisMessaging("primary", "error", errorString, {container: '#questionValidationErrorsDiv'});
        		$('#questionValidationErrorsDiv').show();
        		stillLock();
        		$('html,body').animate({scrollTop:$('#questionValidationErrorsDiv').offset().top},800);
        		return false;
        	}else {
        		$('#questionValidationErrorsDiv').html('');
        		$('#questionValidationErrorsDiv').hide();
        		return true;
        	}
		},
		success: function(response){
			//alert("doAddEditQuestionAjaxPost(): the post Parameters:\n" +params + "\n\nResponse:\n" + response);
			$('html,body').animate({scrollTop:$('#questionValidationErrorsDiv').offset().top},800);
			if(response=="ERROR_DUPLICATE_QUESTION"){
				var errorString = "Please enter a different question name. The question name must be unique in the Question Library.";
				$('#questionValidationErrorsDiv').html('');
        		$.ibisMessaging("primary", "error", errorString, {container: '#questionValidationErrorsDiv'});
        		$('#questionValidationErrorsDiv').show();
				stillLock();
			}
			else if(response=="OneOptionError"){
				var errorString = "<font color='red'>Answer options can not be empty.</font>";
				$('#questionValidationErrorsDiv').html('');
        		$.ibisMessaging("primary", "error", errorString, {container: '#questionValidationErrorsDiv'});
        		$('#questionValidationErrorsDiv').show();
				stillLock();
			}
			else if(response=="CheckBoxError"){
				var errorString = "<font color='red'>You must enter at least one option for check box to continue.</font>";
				$('#questionValidationErrorsDiv').html('');
        		$.ibisMessaging("primary", "error", errorString, {container: '#questionValidationErrorsDiv'});
        		$('#questionValidationErrorsDiv').show();
				stillLock();
			}
			else if(response=="TwoOptions"){
				var errorString = "<font color='red'>You must enter at least two multiple options to continue.</font>";
				$('#questionValidationErrorsDiv').html('');
        		$.ibisMessaging("primary", "error", errorString, {container: '#questionValidationErrorsDiv'});
        		$('#questionValidationErrorsDiv').show();
				stillLock();
			}
			else if(response=="ERROR_SCALE_MAXMIN"){
				var errorString = "<font color='red'>Scale Maximum must be greater than Scale Minimum.</font>";
				$('#questionValidationErrorsDiv').html('');
        		$.ibisMessaging("primary", "error", errorString, {container: '#questionValidationErrorsDiv'});
        		$('#questionValidationErrorsDiv').show();
				stillLock();
			}
			else if(response=="ImageMap_NOTDONE"){
				var errorString = "<font color='red'>Image Map hasn't finished yet.</font>";
				$('#questionValidationErrorsDiv').html('');
        		$.ibisMessaging("primary", "error", errorString, {container: '#questionValidationErrorsDiv'});
        		$('#questionValidationErrorsDiv').show();
				stillLock();
			}
			else if(response=="ERROR_DefaultValue_NEED"){
				var errorString = "<font color='red'>The default value entered must match an option entered.</font>";
				$('#questionValidationErrorsDiv').html('');
        		$.ibisMessaging("primary", "error", errorString, {container: '#questionValidationErrorsDiv'});
        		$('#questionValidationErrorsDiv').show();
				stillLock();
			}
			else if(response=="scoreMissing"){
				var errorString = "<font color='red'>Please enter the score for all of answers, or none of them.</font>";
				$('#questionValidationErrorsDiv').html('');
        		$.ibisMessaging("primary", "error", errorString, {container: '#questionValidationErrorsDiv'});
        		$('#questionValidationErrorsDiv').show();
				stillLock();
			}
			else{
				var qJSON=JSON.parse(response);
				//alert("Response from question save/add: " + response);
				questionId=qJSON.questionId;
				questionVersionLetter=qJSON.questionVersionString;
				questionVersionNumber=qJSON.questionVersionNumber;
				questionName=qJSON.questionName;
				questionText=qJSON.questionText;
				descriptionUp=qJSON.descriptionUp;
				descriptionDown=qJSON.descriptionDown;				
				questionType=qJSON.questionType;
				questionOptions=qJSON.options;
				imageOption=qJSON.imageOption;
				imageFileName=qJSON.imageFileName;
				defaultValue=qJSON.defaultValue;
				unansweredValue=qJSON.unansweredValue;
				associatedGroupIds=qJSON.associatedGroupIds;
				includeOther=qJSON.includeOther;
				attachedFormIds=qJSON.attachedFormIds;
				hasDecimalPrecision = qJSON.hasDecimalPrecision;
				hasCalDependent = qJSON.hasCalDependent;
				prepopulation = qJSON.prepopulation;
				text = qJSON.text;
				<%--do the add graphic--%>
				$('#graphicFr').contents().find('#forGarphicQuestionId').val(questionId);
				$("#graphicFr").contents().find('form').submit();
				<%--the iframe page will go to addGraphicDone.jsp, that means the graphic upload is done--%> 
			}
		},
		error: function(e){
			alert("error" + e );
			stillLock();
		}
	});
}

function finishQuestionAddEdit(){ // when add question, no matter whether the user upload the grahpic, the iframe must go to addGraphicDone.jsp 
	var graphicJSON= JSON.parse($('#graphicFr').contents().find('input#graphicJSON').val()) ;
	var graphicNames=graphicJSON.graphicNames;
	if(questionInfoMode=='edit'){
		updateQuestionToForm(questionName,questionId,questionVersionLetter,questionVersionNumber,questionOptions,imageOption,imageFileName,graphicNames,defaultValue,unansweredValue,
				associatedGroupIds,attachedFormIds,hasDecimalPrecision, hasCalDependent, prepopulation, "");   // update question!!
	}else{  // for add or search question(not anymore)
		addQuestionToForm(questionName,questionId,questionVersionLetter,questionVersionNumber,questionOptions,imageOption,imageFileName,graphicNames,defaultValue,unansweredValue,
					associatedGroupIds,attachedFormIds,hasDecimalPrecision, hasCalDependent, prepopulation, "");   // atached question!!
	}
	//$.fancybox.close();
	$("#addEditQuestionFancyBox").dialog("close");
	setCalDependent(); // reset the calculation dependnce of question
	setSkipRuleDependent();
	hasChanged=false;
}

/**
 * Looks for repeated sections and, if any are found, looks for questions
 * within them that have calculation rules based on the same section.  Changes
 * the references to always point to the current section instead of the
 * repeated parent.
 */
function refreshRepeatedCalcRules() {
	// look at all sections in the page
	for (var i = 0; i < sectionsArray.length; i++) {
		var section = sectionsArray[i];
		
		// only look at repeatable parents, we don't care about others
		if (section.isRepeatable && SectionHandler.convertToHTMLId(section.repeatedSectionParent) == "S_-1") {
			var secId = SectionHandler.convertToHTMLId(section.id);
			
			// look at each question in the repeatable parent section
			var $questions = $("#"+secId+" .question");
			$questions.each(function() {
				var question = QuestionHandler.findQuestion($(this).attr("id"));
				if (question.attributeObject.calculatedQuestion && question.attributeObject.calculation.indexOf(secId) != -1) {
					// this question has a calculation rule and it contains a reference to this section
					
					var repeatedQuestions = QuestionHandler.findAllRepeatedQuestions(question);
					for (var j = 0; j < repeatedQuestions.length; j++) {
						var repQuestion = repeatedQuestions[j];
						var repSectId = SectionHandler.convertToHTMLId(repQuestion.sectionId);
						var calculation = repQuestion.attributeObject.calculation;
						
						// replace the parent section ID with the question's immediate section ID
						repQuestion.attributeObject.calculation = calculation.replace(secId, repSectId);
					}
				}
			});
		}
	}
}

function refreshRepeatedSkipRules() {
	// look at all sections in the page
	for (var i = 0; i < sectionsArray.length; i++) {
		var section = sectionsArray[i];
		
		// only look at repeatable parents, we don't care about others
		if (section.isRepeatable && SectionHandler.convertToHTMLId(section.repeatedSectionParent) == "S_-1") {
			var secId = SectionHandler.convertToHTMLId(section.id);
			
			// look at each question in the repeatable parent section
			var $questions = $("#"+secId+" .question");
			$questions.each(function() {
				var question = QuestionHandler.findQuestion($(this).attr("id"));
				
				if (question.attributeObject.skipRuleType != <%=Integer.MIN_VALUE%>) {
					// this question has a skip rule, so go ahead and get the repeated questions in case we need to process
					// would be more expensive to do this inside the next loop
					var repeatedQuestions = QuestionHandler.findAllRepeatedQuestions(question);
					for (var j = 0; j < question.attributeObject.questionsToSkip.length; j++) {
						if (question.attributeObject.questionsToSkip[j].indexOf(secId) != -1) {
							// this question has a calculation rule and it contains a reference to this section
							
							for (var k = 0; k < repeatedQuestions.length; k++) {
								var repQuestion = repeatedQuestions[k];
								var repSectId = SectionHandler.convertToHTMLId(repQuestion.sectionId);
								
								// replace the parent section ID with the question's immediate section ID
								repQuestion.attributeObject.questionsToSkip[j] = repQuestion.attributeObject.questionsToSkip[j].replace(secId, repSectId);
							}
						}
					}
				}
			});
		}
	}
}

function populateForm() {
	if($('input[name=tabdisplay]:checked').val()=='true'){
		// check to see if there are ANY repeatable sections here
		var allsections = $(".section");
		for (var i = 0; i < allsections.length; i++) {
			var thesect = SectionHandler.findSection(allsections.eq(i).attr("id"));
			if (thesect.isRepeatable) {
				alert("A section on this form is repeatable.  The form cannot be in tab display and have repeatable sections.  Please turn off tab display or remove the repeatable section");
				return false;
			}
		}
	}
	
	SectionHandler.setRowsCols();
	// refreshes the section ID for each question
	QuestionHandler.setQuestionSectionId($(".question"));
	
	// refresh the calculation rules within repeated sections
	try {
		refreshRepeatedCalcRules();
	} catch(e) {
		alert("There was a problem propagating the calculation rules across repeated sections.  We will save anyway");
	}
	
	// refresh the skip rules within repeated sections
	try {
		refreshRepeatedSkipRules();
	} catch (e) {
		alert("There was a problem propagating the skip rules across repeated sections.  We will save anyway");
	}
	
	var string = "";
	for(var i=0; i < sectionsArray.length; i++) {
		string +="["+i+",{rows: "+sectionsArray[i].row+", cols: "+sectionsArray[i].col+"} ]\n";
	}
	
	// remove the "S_" from the section id if the section exists
	for(var i=0; i < sectionsArray.length; i++) {
		if (sectionsArray[i].existingSection) {
				sectionsArray[i].id = sectionsArray[i].id.replace("S_", "");
		}
	}
	
	for(var i=0; i < existingSectionIdsToDeleteArray.length; i++) {
		existingSectionIdsToDeleteArray[i] = existingSectionIdsToDeleteArray[i].replace("S_","");
	}
	
	for(var i=0; i < existingQuestionIdsToDeleteArray.length; i++) {
		existingQuestionIdsToDeleteArray[i] = existingQuestionIdsToDeleteArray[i].replace("S_","");
	}
	
	questionsArray.sort(function(a,b) {
		if (a.sectionId != b.sectionId) {
			return a.sectionId - b.sectionId;
		}
		return a.questionOrder - b.questionOrder;
	});

	//JSON stuff
	var addSecJSONString = encodeURI(JSON.stringify(sectionsArray));
	var addQuesJSONString = JSON.stringify(questionsArray);
	var existingSectionIdsToDeleteJSONString = JSON.stringify(existingSectionIdsToDeleteArray);
	var existingQuestionIdsToDeleteJSONString = JSON.stringify(existingQuestionIdsToDeleteArray);
	
	var sectionsJSON = document.getElementsByName("sectionsJSON")[0];
	var questionsJSON = document.getElementsByName("questionsJSON")[0];
	var existingSectionIdsToDeleteJSON = document.getElementsByName("existingSectionIdsToDeleteJSON")[0];
	var existingQuestionIdsToDeleteJSON = document.getElementsByName("existingQuestionIdsToDeleteJSON")[0];
	
	sectionsJSON.value=addSecJSONString;
	questionsJSON.value=addQuesJSONString;
	existingSectionIdsToDeleteJSON.value=existingSectionIdsToDeleteJSONString;
	existingQuestionIdsToDeleteJSON.value=existingQuestionIdsToDeleteJSONString;

	$("#formBuildForm").submit();
}

function checkDataSpring(){
	if (isDataSpring) {
		$('#dataSpringH').show();
	} else {
		$('#dataSpringH').hide();
		$('#dataSpringDiv').hide();
	}
}


function setDataStructure(name,version) {

	$('#dataStructureName').val(name);
	$('#dataStructureVersion').val(version);
	var fullName = name + "(" + version + ")";

	var formDataStructuresDiv = document.getElementById("formDataStructures");
	var table = formDataStructuresDiv.getElementsByTagName("table")[0];
	var $table = $(table);
	var $list = IbisDataTables.getAllInputs($table);
	var elms = $list.toArray();
	
	for (var i=0;i<elms.length;i++) {
		var elm = elms[i];

		var radioId = elms[i].value;
		if (radioId != fullName) {
			elm.checked = 0;
		}
	}
}


function collapseFormStructureDiv() {
	if (!($("#fidH3").next().is(":visible"))) {
		if ($("#fdsDiv").length>0) {
			
			if ($("#fdsH3").next().is(":visible")) {
				$("#fdsDiv h3").click();
			}
			
		}
	}
}


function setCopyright(isCopyright){
	if (isCopyright) {
		document.getElementsByName("formForm.copyRight")[0].checked=true;
		document.getElementsByName("formForm.copyRight")[1].checked=false;
	} else {
		document.getElementsByName("formForm.copyRight")[0].checked=false;
		document.getElementsByName("formForm.copyRight")[1].checked=true;			
	}
}


function enabledisableRepeatable(flag) {
	if (flag == "true") {
		$('#sectionRepeatbale').attr('disabled',true);
	} else {
		$('#sectionRepeatbale').attr('disabled',false);
	}
}

</script>


<div id="sectionQuestionButtonsDiv" align="center">
	<table>
		<tr>
			<td>
				<button id="addSectionFancyBoxBtn" class="addSection" onclick="triggerAddSectionAnchor()" disabled title="Click to add section">
					<s:text name="form.forms.formInfo.addSectionDisplay" />
				</button> 
				<a href="#addEditSectionFancyBox" id="addSectionFancyBoxA" style="display: none">
					<s:text name="form.forms.formInfo.addSectionDisplay" />
				</a>
			</td>
			<td></td>
		</tr>
	</table>
</div><br>


<!-- this is the form info panel --------------------------------------------------------------------------------------------------------->
<div id="formInfoDiv" style="display: none" >
	<table width="100%">
		<tr>
			<td valign='top'>
				<table>
					<tr>
						<td align="left" valign='top'><b><s:text name="form.forms.formInformation.formName" />:&nbsp;</b></td>
						<td id="formNameTD" align="left" valign='top'></td>
					</tr>
					<tr>
						<td align="left" valign='top'><b><s:text name="form.forms.formInformation.formDataStructure" />:&nbsp;</b></td>
						<td id="formStructureTD" align="left" valign='top'></td>
					</tr>
					<tr>
						<td align="left" valign='top'><b><s:text name="form.forms.formInformation.formStatus" />:&nbsp;</b></td>
						<td id="formStatusTD" align="left" valign='top'></td>
					</tr>
				</table>
			</td>
			<td align="right" valign="top">
				<a href='javascript:void(0)' title='Edit Form Information' onclick="editFormInfo()"><img src="../images/icons/cog.png" /></a>
			</td>
		</tr>
	</table>
</div><br>
<!--  ---------------------------------------------------------------------------------------------------------------------------------------->


<!--  this is the div for all the sections --------------------------------------------------------------------------------------------------->
<div id="formSectionsDiv">

	<div class="clearboth">
		<!-- clear -->
	</div>
</div>
<div class="clearboth">
	<!-- clear -->
</div>
<!--  ----------------------------------------------------------------------------------------------------------------------------------------->


<!--  below are the fancy box pop-up divs for the add/edit form info, add/edit section, and add/edit question ------------------------------------>

<div class="fancyBoxContainer">
	<div id="addEditFormInfoFancyBox">
		<jsp:include page="addEditFormInfo.jsp"/>
	</div>
</div>

<div class="fancyBoxContainer">
	<div id="addEditSectionFancyBox">
	 	<jsp:include page="addEditSection.jsp"/>    
	</div>
</div>

<div class="fancyBoxContainer">
	<div id="addEditQuestionFancyBox">
		<jsp:include page="addEditQuestion.jsp"/>
	</div>
</div>

<div class="fancyBoxContainer">
	<div id="addEditTextDialog">
		<div class="formrow_1">
			<label for="textText">Text</label>
			<textarea class="tinymce" id="textText"></textarea>
		</div>
	</div>
</div>

<!--  ------------------------------------------------------------------------------------------------------------------------------------------------>


<!--  below is the form that will be sent to server for adding sections and questions ----------------------------------------------------------------->
<div id="formBuildDiv" align="right">

	<div class="clearfix">
		<!-- stuff -->
	</div>
	
	<s:form method="post" id="formBuildForm" theme="simple">
		<s:hidden name="formMode" value="%{#request.formMode}" />
		<s:hidden name="formBuildFormId" id="formBuildFormId" />
		<s:hidden name="sectionsJSON" />
		<s:hidden name="questionsJSON" />
		<s:hidden name="existingSectionIdsToDeleteJSON" />
		<s:hidden name="existingQuestionIdsToDeleteJSON" />

		<s:submit action="formBuild" key="button.Save" title="Click to save" onclick="return populateForm()" />
		<input type="button" value="<s:text name='button.Cancel' />" title="Click to cancel(changes will not be saved)." onclick="cancel()" />
	</s:form>

</div>

<!--  -------------------------------------------------------------------------------------------------------------------------------------------------->
<jsp:include page="/common/footer_struts2.jsp" />