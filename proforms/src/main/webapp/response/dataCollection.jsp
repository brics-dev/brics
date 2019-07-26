<jsp:include page="/common/doctype.jsp" />
<%@ page import="gov.nih.nichd.ctdb.attachments.manager.AttachmentManager"%>
<%@ page import="gov.nih.nichd.ctdb.protocol.domain.Protocol"%>
<%@ page import="gov.nih.nichd.ctdb.common.CtdbConstants"%>
<%@ page import="gov.nih.nichd.ctdb.common.StrutsConstants"%>
<%@ page import="gov.nih.nichd.ctdb.response.domain.FormInterval"%>
<%@ page import="gov.nih.nichd.ctdb.security.domain.User"%>
<%@ page import="java.util.Iterator"%>
<%@ page import="gov.nih.nichd.ctdb.common.rs"%>
<%@ page import="java.util.List"%>
<%@ taglib uri="/struts-tags" prefix="s"%>
<%@ taglib uri="/WEB-INF/display.tld" prefix="display"%>
<%@ taglib uri="/WEB-INF/security.tld" prefix="security"%>

<security:check privileges="dataentry, dataentryoversight" />

<s:set var="pageTitle" scope="request">
	<s:text name="response.collect.title.display2" />
</s:set>

<jsp:include page="/common/header_struts2.jsp" />

<link rel="stylesheet" type="text/css" href="<s:property value="#systemPreferences.get('app.webroot')"/>/common/c-<s:property value="#systemPreferences.get('template.global.appName')"/>/css/style.css" />
<link rel="stylesheet" type="text/css" href="<s:property value="#webRoot"/>/common/sliderSwing.css">
<link rel="stylesheet" type="text/css" href="<s:property value="#webRoot"/>/common/css/dataCollection.css">

<script type="text/javascript" src="<s:property value="#webRoot"/>/common/js/range.js"></script>
<script type="text/javascript" src="<s:property value="#webRoot"/>/common/js/timer.js"></script>
<script type="text/javascript" src="<s:property value="#webRoot"/>/common/js/slider.js"></script>
<script type="text/javascript" src="<s:property value="#webRoot"/>/common/JsResponse.js"></script>
<script type="text/javascript" src="<s:property value="#webRoot"/>/common/hashmap.js"></script>
<script type="text/javascript" src="<s:property value="#webRoot"/>/common/rangeValidation.js"></script>
<script type="text/javascript" src="<s:property value='#webRoot'/>/common/js/template.js"></script>
<script type="text/javascript" src="<s:property value='#webRoot'/>/common/js/dataCollection.js"></script>
<script type="text/javascript" src="<s:property value='#webRoot'/>/common/js/jquery.form.min.js"></script>
<script type="text/javascript" src='<s:property value="#webRoot"/>/formbuilder/js/models/Processing.js'></script>
<script type="text/javascript" src="<s:property value='#webRoot'/>/formbuilder/js/views/ProcessingView.js"></script>
<script type="text/javascript" src="<s:property value='#webRoot'/>/common/conversionTable.js"></script>

<jsp:include page='/formbuilder/templates/builder/processing.jsp' />

<script type="text/javascript">
 // start up the processingview
 var FormBuilder = {
	page : {
		processingView : null,
		get : function(name) {
			return FormBuilder.page[name];
		}
	} 
 };
 var processingView = null;
 
 $(document).ready(function() {
	FormBuilder.page.processingView = new ProcessingView(); 	
 });
</script>

<%
	String mode = (String) request.getAttribute(CtdbConstants.DATACOLLECTION_MODE); 
	String formType = (String) request.getAttribute(CtdbConstants.FORM_TYPE_REQUEST_ATTR);

	String formId = "";
	if (request.getAttribute(CtdbConstants.FORM_ID_REQUEST_ATTR) != null) {
		formId += request.getAttribute(CtdbConstants.FORM_ID_REQUEST_ATTR);
	}

	String formStatus = (String) request.getAttribute(StrutsConstants.FORMSTATUS);
	String aformId = (String) request.getAttribute(CtdbConstants.AFORM_ID_REQUEST_ATTR);
	String visitDate = (String) request.getAttribute(StrutsConstants.VISITDATE);
	String pVisitDate = (String) request.getAttribute(StrutsConstants.SUBJECT_VISIT_DATE);
	String pIntervName = (String) request.getAttribute(StrutsConstants.SUBJECT_INTERVAL_NAME);

	boolean formFetched = (Boolean) request.getAttribute(StrutsConstants.FORMFETCHED);
	boolean editMode = (Boolean) request.getAttribute(CtdbConstants.DATACOLLECTION_EDITMODE);
	boolean isCat = (Boolean) request.getAttribute(CtdbConstants.IS_CAT);
	String mType = (String) request.getAttribute(CtdbConstants.MEASUREMENT_TYPE);

	boolean attachFiles = false;
	if (request.getAttribute(StrutsConstants.ATTACHFILES) != null) {
		attachFiles = (Boolean) request.getAttribute(StrutsConstants.ATTACHFILES);
	}
	
	boolean deleteOnCancelFlag = false;
	if (session.getAttribute(StrutsConstants.DELETEONCANCELFLAG) != null) {
		deleteOnCancelFlag = (Boolean) session.getAttribute(StrutsConstants.DELETEONCANCELFLAG);
	}
	
	List<FormInterval> formsInInterval = (List) request.getAttribute(StrutsConstants.FORMSININTERVAL);
	
	boolean markAsCompleted = false;
	if (request.getAttribute(StrutsConstants.MARKASCOMPLETESTATUSINACTION) != null) {
		markAsCompleted = (Boolean)request.getAttribute(StrutsConstants.MARKASCOMPLETESTATUSINACTION);
	}
	String action = (String) request.getAttribute(StrutsConstants.ACTION);
	User user = (User) session.getAttribute(CtdbConstants.USER_SESSION_KEY);
	Protocol protocol = (Protocol) session.getAttribute(CtdbConstants.CURRENT_PROTOCOL_SESSION_KEY);
	int subjectDisplayType = protocol.getPatientDisplayType();
	boolean enableEsignature = protocol.isEnableEsignature();
	
	boolean displayVisitDateWarning = false;
	if (request.getAttribute(CtdbConstants.DATACOLLECTION_DISPLAY_VISITDATE_WARNING) != null) {
		displayVisitDateWarning = true;
	}

%>

<script type="text/config" id="formNameToBeSavedOrLocked"><s:property value="#request.formNameToBeSavedOrLocked" /></script>
<script type="text/javascript">
LeftNav.collapsed = true;

<%-- Global flags for saved message and mark as completed checkbox --%>	
var showSavedMessage;
var fileExist;
var assocFileQuestionIds;
var markAsCompletedCheckBoxStatus = false;
var comingFromValidationErrorGoto = false;
var isDocumentLoaded= false;
var globalEditFlag = <%=editMode%>;
var globalDeleteOnCancel = <%=deleteOnCancelFlag%>;
var $disabledInputs;
var globalFormStatus = '<%=formStatus%>';
var passwordErrorId = "";

	<%-- Action on jumpTo link click --%>	
	function jumpTo(jumptoformId) {
		EventBus.trigger("open:processing", "Going to selected eForm...");
		setHiddenFileInputs();

		if(globalFormStatus==="Completed") {	
			addReasonsToForm();
		}
		
		
		

		
		disableActionControls();
		doClickedSectionIds();
		LeavePageWarning.save();
		var formNameElm7 = $('[name="dataEntryForm.formName"]').eq(0);
		var currentFormName = formNameElm7.val();
		var formIdEle = document.getElementsByName("dataEntryForm.formId")[0];
		var formId = formIdEle.value;
		var intervalIdElm7 = $('[name="dataEntryForm.intervalId"]').eq(0);
		var currentIntervalId = intervalIdElm7.val();
		var patientIdElm7 = $('[name="dataEntryForm.patientId"]').eq(0);                     
		var currentSubjectId =  patientIdElm7.val();
		var visitDateElm7 = $('[name="dataEntryForm.visitDate"]').eq(0);
		var currentDateVal= visitDateElm7.val();
		var form = document.getElementById("myForm");
		
		form.action = "<s:property value="#webRoot"/>/response/dataCollection.action?action=jumpToForm&markAsCompletedCheckBoxStatus=" +
				markAsCompletedCheckBoxStatus+"&currentIntervalId="+currentIntervalId+"&jumptoformId="+jumptoformId+"&formId="+formId+"&currentFormName=" +
				currentFormName+"&currentSubjectId="+currentSubjectId+"&currentDateVal="+currentDateVal+"&aformId="+<%=aformId%> +
				"&fileExist="+fileExist+"&assocFileQuestionIds="+assocFileQuestionIds;
		form.submit();
	}

/**
 * Disables all active inputs and links inside the maincontent container to
 * enforce no 
 *
 * This disables all ACTIVE action controls and adds those that are disabled
 * to the $disabledInputs list.  That allows us to re-enable them after
 * finishing the save.
 */
function disableActionControls() {
	$disabledInputs = $();
	$('#mainContent input[type="button"], #mainContent a, .ui-dialog input[type="button"], #markAsCompletedCheckBox').each(function() {
		var $this = $(this);
		if ($this.prop('tagName') == "A") {
			// disables a link
			$this.on( "click", function( event ) {
			  event.preventDefault();
			}); 
			if ($disabledInputs.length < 1) {
				$disabledInputs = $this;
			} else {
				$disabledInputs = $disabledInputs.add($this);
			}
		}
		else {
			if (!$this.prop('disabled')) {
				$this.prop('disabled', true);
				if ($disabledInputs.length < 1) {
					$disabledInputs = $this;
				} else {
					$disabledInputs = $disabledInputs.add($this);
				}
			}
		}
	});
}

/**
 * Re-enables all disabled inputs and links that were disabled via
 * disableActionControls()
 */
function enableActionControls() {
	if($disabledInputs!=null) {
		$disabledInputs.each(function() {
			var $this = $(this);
			if ($this.prop('tagName') == 'A') {
				$this.off( "click");
			} else {
				$(this).prop('disabled', false);
			}
		});
	}
}


//Get attachment links in the form that will direct to actual file location to decide to consider file link as answer for validation
function getAttchmentIds(){
	var fileNames = [];
	assocFileQuestionIds="";
	$(".fileDownloadLink").each(function() {
		var tempCheck = $(this).attr("filename");
		if (tempCheck!='undefined' && tempCheck.length>0) {
			var files = $(this).attr("filename");
			fileNames.push(files);
			var assocQId = $(this).attr("assoc_questionid"); 
			assocFileQuestionIds = assocFileQuestionIds  + assocQId + ",";
		}
	});
	
	if (fileNames.length == 0) {
		fileExist= false;
	} else {
		fileExist= true;
	}
	return fileExist;
}


//this function sets the value for the hidden fileuploadkeys
function setHiddenFileInputs() {
	
	$(".fileInput").each(function() {
		var type = $(this).attr("type");
		if(type === 'file') {
			var value = $(this).val();
			var id =  $(this).attr("id");
			if(value != null && value != 'undefined' && value.length>0) {

				$("#" + id + "_fileKey").val(id);

				
			}
		}
	});
	
}


function commonSaveExitLockTask(){
	disableActionControls();
    doClickedSectionIds();
    
    var imAnswers = document.body.getElementsByTagName('SELECT');	
	if (imAnswers != null) {
		for (i = 0; i < imAnswers.length; i++) {
			if (imAnswers[i].className =='ctdbImageMapAnswers') {
				opts = imAnswers[i].options;
				for (j=0; j < opts.length; j++) {
					opts[j].selected = true;
				}
			}
		}
	}
}


//Toggle(show hide) data element text area

/* function launchCommentText(textAreaId){
	$('#' + textAreaId).toggle();
} */

//Send array of data element comment to action
/* function setDataElementTextAreaCommment(){
	jsonKeySQvalueCommentArray = [];
	$(".deTAcomment").each(function() {
		var value = $(this).val();
		var id_deComment_=  $(this).attr("id");
		var  id  = id_deComment_.replace("_deComment_",' ');
		jsonKeySQvalueComment={};
		jsonKeySQvalueComment["id"] = id;
		jsonKeySQvalueComment["val"] = value;
		
		if(value != null && value != 'undefined' && value.length>0) {
			//jsonKeySQvalueComment.put(id,value);
			jsonKeySQvalueCommentArray.push(jsonKeySQvalueComment);
			
		}
	});
	$('input[name="dataEntryForm.deComments"]').val(JSON.stringify(jsonKeySQvalueCommentArray));
} */



$(document).ready(function() {
	 fileExist = getAttchmentIds();
	 var dataCollectionErrorMsg = "<s:text name="response.errors.duplicate.data"/><br/>"+
		"FormName: "+$('[name="dataEntryForm.formName"]').val()+"<br\>"+
		"Visit Type: "+$('[name="dataEntryForm.intervalName"]').val()+"<br/>"+
		"Patient Id: "+$('[name="dataEntryForm.patientId"]').val();
	/* Save button will call this function 	 */
   $('#saveAndStyBtn').click(function(e) { 
	   $("input[dename='PROMISRawScore']").focus();
    	EventBus.trigger("open:processing", "Saving your eForm...");
    	setHiddenFileInputs();
    	commonSaveExitLockTask();
    	LeavePageWarning.save();
		if(globalFormStatus==="Completed") {	
			addReasonsToForm();
		}
    	var url = "<s:property value="#webRoot"/>/response/collectDataPreviousHome.action";
    	$('#myForm').ajaxSubmit({
			data: {clickedSectionFields:$("#clickedSectionFieldsId").val(), formTypeId:$("#formTypeId").val(), modeId:$("#modeId").val()},
			url: "<s:property value="#webRoot"/>/response/dataCollection.action?action=save&markAsCompletedCheckBoxStatus=" + 
					markAsCompletedCheckBoxStatus +"&aformId=" +<%=aformId%>+ "&editMode=" + globalEditFlag + "&fileExist=" +
					fileExist+"&assocFileQuestionIds="+assocFileQuestionIds,
		  	success: function(response, status, jqxhr) {
		  		EventBus.trigger("close:processing");
		  		
		  		var data = JSON.parse(response);
		  		
		  		var st = data.status;

		  		
		  		
		  		
		  		if (st == "<%=StrutsConstants.ERROR_ANSWER_REQUIRED_IN_SAVE%>") {
		        	$.ibisMessaging("close", {type:"primary"}); 
					$.ibisMessaging("primary", "error", '<s:text name="errors.text"/></b><%=rs.getValue(StrutsConstants.ERROR_ANSWER_REQUIRED_IN_SAVE,request.getLocale())%>', {hideDelay: 3000});	
					enableActionControls();
					if(globalFormStatus!="Completed") {
						$('#markAsCompletedCheckBox').attr('disabled', false);
					}
		  		} else if (st.toLowerCase().indexOf("<li")==0) {
		        	$.ibisMessaging("close", {type:"primary"}); 
					$.ibisMessaging("primary", "error", '<s:text name="errors.text"/></b>'+st, {hideDelay: 3000});	
					enableActionControls();
					if(globalFormStatus!="Completed") {
						$('#markAsCompletedCheckBox').attr('disabled', false);
					}
		  		} else if (st == "saveFormSuccess") {
		        	$.ibisMessaging("close", {type:"primary"}); 
					$.ibisMessaging("flash", "success", "Data Saved Successfully.", {hideDelay: 3000});
					globalEditFlag = true;
					globalDeleteOnCancel = false;
					var returnArray = data.returnArray;
					refreshAttachmentLinks(returnArray);
					if ($('#markAsCompletedCheckBox').prop('checked')) {
						$("#currFormStatusCell").html("&nbsp;&nbsp;Completed");					
						$(".activeDiv .statusCir").addClass('yellowCircle').removeClass('redCircle');
						globalFormStatus="Completed";
						bindReasonForChange();
						enableActionControls();
						$('#markAsCompletedCheckBox').attr('disabled',true);
						//var returnArray = data.returnArray;
						rePopulateJsResponses(returnArray);
						///////////////////////
					}else{
					     $(".activeDiv .statusCir").addClass('redCircle').removeClass('whiteCircle');
					     var newTitle = 'In Progress';
					     var anchorText = $(".activeDiv .statusCir").text();
					     if(anchorText == 'R') {       
					    	 newTitle = 'Required: In Progress';      
					     }
					     $(".activeDiv .statusCir").prop('title', newTitle);
					     enableActionControls();
					    }
		  		}else if(st == 'duplicateData'){
		  			$.ibisMessaging("close", {type:"primary"}); 
		  			$.ibisMessaging("store", "primary", {message: dataCollectionErrorMsg, level: "error"});
		  			redirectWithReferrer(url);
		  		}else if(st == 'dataCollectionException'){
		  			$.ibisMessaging("close", {type:"primary"}); 
		  			$.ibisMessaging("store", "primary", {message: dataCollectionErrorMsg, level: "error"});
		  			redirectWithReferrer(url); 		  		
		  		}else {
		  			$.ibisMessaging("close", {type:"primary"}); 
		  			$.ibisMessaging("store", "primary", {message: dataCollectionErrorMsg, level: "error"});
		  			redirectWithReferrer(url)		  		
		  		}
		  		
	        },
	        error: function (xhr, ajaxOptions, thrownError) {
	        	EventBus.trigger("close:processing");
	  			$.ibisMessaging("close", {type:"primary"}); 
	  			$.ibisMessaging("store", "primary", {message: dataCollectionErrorMsg, level: "error"});
	  			redirectWithReferrer(url);	
	        	enableActionControls();
	        }	 
	    }); 
        return false; 
    }); 
	
	
   /* This is Save and Exit button for Final Lock Edits    */
   $('#saveFLBtn').click(function(e) { 
   	 EventBus.trigger("open:processing", "Saving your eForm...");
   	 setHiddenFileInputs();
   	 commonSaveExitLockTask();
     addReasonsToForm();
     LeavePageWarning.save();

   	 saveFLAjax();

   	 return false;
   });
   
   $('#confirmSignAndSaveFLBtn').click(function(e) {
	   	EventBus.trigger("open:processing", "Saving your eForm...");
	   	setHiddenFileInputs();
	   	commonSaveExitLockTask();
	    addReasonsToForm();
	    LeavePageWarning.save();
 		   	
	   	$(".finalLock").data('aformId', <%=aformId%>).dialog("open");

	   	enableActionControls();
	   	$("#lockAndLoadPrevious").prop("disabled", true);
	   	$("#lockAndLoadNext").prop("disabled", true);
	   	EventBus.trigger("close:processing");
   });
   
  /*  Save and Exit button will call this function */
	$('#saveAndExitBtn').click(function(e) { 
		$("input[dename='PROMISRawScore']").focus();
		EventBus.trigger("open:processing", "Saving your form...");
		setHiddenFileInputs();
		//setDataElementTextAreaCommment();
		if(globalFormStatus==="Completed") {	
			addReasonsToForm();
		}
		commonSaveExitLockTask();
		LeavePageWarning.save();
		var url = "<s:property value="#webRoot"/>/response/collectDataPreviousHome.action";
    	$('#myForm').ajaxSubmit({
    	data: {clickedSectionFields:$("#clickedSectionFieldsId").val(), formTypeId:$("#formTypeId").val(), modeId:$("#modeId").val()},
		url: "<s:property value="#webRoot"/>/response/dataCollection.action?action=saveForm&markAsCompletedCheckBoxStatus="+markAsCompletedCheckBoxStatus+
				"&aformId=" +<%=aformId%>+ "&editMode=" + globalEditFlag+"&fileExist="+fileExist+"&assocFileQuestionIds="+assocFileQuestionIds,	
		success: function(response, status, jqxhr) {
				EventBus.trigger("close:processing");
				var data = JSON.parse(response);
		  		var st = data.status;
				if (st == "<%=StrutsConstants.ERROR_ANSWER_REQUIRED_IN_SAVE%>") {
		        	$.ibisMessaging("close", {type:"primary"}); 
					$.ibisMessaging("primary", "error", '<s:text name="errors.text"/></b><%=rs.getValue(StrutsConstants.ERROR_ANSWER_REQUIRED_IN_SAVE,request.getLocale())%>', {hideDelay: 3000});
					if(globalFormStatus!="Completed") {
						$('#markAsCompletedCheckBox').attr('disabled', false);
					}
		  		} else if (st.toLowerCase().indexOf("<li")==0) {
		        	$.ibisMessaging("close", {type:"primary"}); 
					$.ibisMessaging("primary", "error", '<s:text name="errors.text"/></b>'+st, {hideDelay: 3000});
					if(globalFormStatus=="Completed") {
						var returnArray = data.returnArray;
						rePopulateJsResponses(returnArray);
					}
					if(globalFormStatus!="Completed") {
						$('#markAsCompletedCheckBox').attr('disabled', false);
					}
		  		}else if (st == "saveFormSuccess") {
						$.ibisMessaging("store", "primary", {message: 
							"The administered form "+$('[name="dataEntryForm.formName"]').val()+" has been saved successfully.", level: "success"});
						
						redirectWithReferrer(url);	
		  		}else if(st == 'duplicateData'){
		  			$.ibisMessaging("close", {type:"primary"}); 
		  			$.ibisMessaging("store", "primary", {message: dataCollectionErrorMsg, level: "error"});
		  			redirectWithReferrer(url);
		  		}else if(st == 'dataCollectionException'){
		  			$.ibisMessaging("close", {type:"primary"}); 
		  			$.ibisMessaging("store", "primary", {message: dataCollectionErrorMsg, level: "error"});
		  			redirectWithReferrer(url);	
	
		  		}else {
		  			$.ibisMessaging("close", {type:"primary"}); 
		  			$.ibisMessaging("store", "primary", {message: dataCollectionErrorMsg, level: "error"});
		  			redirectWithReferrer(url);  		
		  		}
		  		enableActionControls();	
	        },
	        error: function (xhr, ajaxOptions, thrownError) {
	        	EventBus.trigger("close:processing");
	  			$.ibisMessaging("close", {type:"primary"}); 
	  			$.ibisMessaging("store", "primary", {message: dataCollectionErrorMsg, level: "error"});
	  			redirectWithReferrer(url);
	        }	
		}); 
    	return false; 
	}); 
		
    /* Lock button will call this  and lockstep1 method in action */
	$('#lockButtonPatient').click(function(e) { 
		$("input[dename='PROMISRawScore']").focus();
		EventBus.trigger("open:processing", "Saving your form...");
		setHiddenFileInputs();

		if(globalFormStatus==="Completed") {	
			addReasonsToForm();
		}
		commonSaveExitLockTask(); 
		LeavePageWarning.save();
		var url = "<s:property value="#webRoot"/>/response/collectDataPreviousHome.action";
	 	$('#myForm').ajaxSubmit({
		data: {clickedSectionFields:$("#clickedSectionFieldsId").val(), formTypeId:$("#formTypeId").val(), modeId:$("#modeId").val()},
		url:"<s:property value="#webRoot"/>/response/dataCollection.action?action=lockForm&markAsCompletedCheckBoxStatus=" +
				markAsCompletedCheckBoxStatus+"&aformId=" +<%=aformId%>+ "&editMode=" + globalEditFlag + 
				"&fileExist="+fileExist+"&assocFileQuestionIds="+assocFileQuestionIds,
		  	success: function(response, status, jqxhr) {
		  		EventBus.trigger("close:processing");
		  		if (response == "<%=StrutsConstants.ERROR_ANSWER_REQUIRED_IN_LOCK%>"){
		        	$.ibisMessaging("close", {type:"primary"}); 
					$.ibisMessaging("primary", "error", '<s:text name="errors.text"/></b><%=rs.getValue(StrutsConstants.ERROR_ANSWER_REQUIRED_IN_SAVE,request.getLocale())%>', {hideDelay: 3000});
		  		} else if (response.toLowerCase().indexOf("<li")==0){
		        	$.ibisMessaging("close", {type:"primary"}); 
					$.ibisMessaging("primary", "error", '<s:text name="errors.text"/></b>'+response, {hideDelay: 3000});
		  		}else if(response == 'duplicateData'){
		  			$.ibisMessaging("close", {type:"primary"}); 
		  			$.ibisMessaging("store", "primary", {message: dataCollectionErrorMsg, level: "error"});
		  			redirectWithReferrer(url);	
		  		}else if(response == 'dataCollectionException'){
		  			$.ibisMessaging("close", {type:"primary"}); 
		  			$.ibisMessaging("store", "primary", {message: dataCollectionErrorMsg, level: "error"});
		  			redirectWithReferrer(url);	
		  		}else if(response == "lockForm1Success"){
			  		$.ibisMessaging("close", {type:"primary"}); 

					<% if (formStatus != null && (formStatus.equals(CtdbConstants.DATACOLLECTION_STATUS_LOCKED)
							|| formStatus.equals(CtdbConstants.DATACOLLECTION_STATUS_FINALLOCKED))) { %>
						$(".finalLock").data('aformId', <%=aformId%>).dialog({title: 'Locked Data Changed - Signature Required'}).dialog("open");							
					<% 	} else { %>
						$(".finalLock").data('aformId', <%=aformId%>).dialog({title: 'Collect Data Lock Confirmation - Signature Required'}).dialog("open");							
					<% 	} %>
					
					//need to update the visit date display in the dialog as it might have changed
			  		var visitDateElem = $("[dataelementname=VisitDate]").find("input");
					if(visitDateElem.length > 0) {
						var visitDate = visitDateElem.val();
						$("#visitdatedisplay").text(visitDate);
		  			}
		  		} else {
		  			$.ibisMessaging("close", {type:"primary"}); 
		  			$.ibisMessaging("store", "primary", {message: dataCollectionErrorMsg, level: "error"});
		  			redirectWithReferrer(url);	
		  		}
		  		enableActionControls();
	        },
	        error: function (xhr, ajaxOptions, thrownError) {
	        	EventBus.trigger("close:processing");
	  			$.ibisMessaging("close", {type:"primary"}); 
	  			$.ibisMessaging("store", "primary", {message: dataCollectionErrorMsg, level: "error"});
	  			redirectWithReferrer(url);	
	        	enableActionControls();
	        }	
		
	 	}); 
	  	return false; 
	}); 
	
	
	var mode = "<%=mode%>";	
	LogoutWarning.init();
	LeavePageWarning.init();
	AutoSaver.init(); 
	<%-- Get the mark as complete checkbox status set in action and apply in JSP --%>
	var markAsCompletedStatusInAction = <%=markAsCompleted%>;
	if (markAsCompletedStatusInAction) {
		$("#markAsCompletedCheckBox").prop('checked', true);
		markAsCompletedCheckBoxStatus = true;
		$('#saveAndStyBtn').prop('disabled', true);
	} else {
		 $('#lockButtonPatient').attr('disabled', true);
		 markAsCompletedCheckBoxStatus = false;
		 $('#saveAndStyBtn').prop('disabled', false);
	}
		
	<%-- Set markAsCompletedCheckBoxStatus as global flag based on whether it's checked or unchecked and 
	Disable Lock button if the mark as checkbox is not checked and enable it when checked --%>
	$('input[name=markChecked]').change(function(){
		if($('input[name=markChecked]').is(':checked')){
			markAsCompletedCheckBoxStatus = true;
			$('#lockButtonPatient').attr('disabled', false);
			$('#saveAndStyBtn').prop('disabled', true);
		 } else {
		    markAsCompletedCheckBoxStatus = false;
		    $('#lockButtonPatient').attr('disabled', true);
		    $('#saveAndStyBtn').prop('disabled', false);
		}
	});
	
	
	<%-- If user tries to edit Locked form then disable the mark as completed checkbox --%> 
	 if(globalEditFlag && (('<%=formStatus%>' =="Final Lock")||('<%=formStatus%>' =="Locked"))){
		$("#markAsCompletedCheckBox").prop('checked', true);
		$('#markAsCompletedCheckBox').attr('disabled', true);
		$('#saveAndStyBtn').prop('disabled', true);
	} 
	
	var nextDisableFlag = <s:property value='dataEntryForm.nextButtonDisable' />;
	var previousDisableFlag = <s:property value='dataEntryForm.previousButtonDisable' />;
	var editHideNextPreviousFlag = <s:property value='dataEntryForm.editHideNextPrevious' />;
	showSavedMessage = <s:property value='dataEntryForm.showSavedMessage' />;
	var formNameToBeSavedOrLocked = $("#formNameToBeSavedOrLocked").text();
	
	if (editHideNextPreviousFlag) {
		$('#nextButton').hide();
		 $('#previousButton').hide();
		 $('#lockAndLoadPrevious').hide();
		 $('#lockAndLoadNext').hide();
	}
	
	var intervalIdElm7 = $('[name="dataEntryForm.intervalId"]').eq(0);
	var currentIntervalId = intervalIdElm7.val();
	<%-- For Other visit type hide next previous and show lock button --%> 
	if (currentIntervalId == -1){
		$('#nextButton').hide();
		 $('#previousButton').hide();
		 $('#lockButtonPatient').show();
		 $('#lockAndLoadPrevious').hide();
		 $('#lockAndLoadNext').hide();
	}
	

 		
	if (nextDisableFlag) {
		$('#nextButton').attr('disabled',true);
		$('#lockAndLoadNext').attr('disabled',true);	 	 
	} 
	
 	$("#sdcID").click(function() {
 		LeavePageWarning.save();
 		fetchForm();
 	});
 		 
	if (previousDisableFlag) {
		$('#previousButton').attr('disabled',true);
		$('#lockAndLoadPrevious').attr('disabled',true);
	} 

	
	<%-- Action on next button click --%>	 
	 $("#nextButton").click(function() {
		EventBus.trigger("open:processing", "Going to next eForm...");
		setHiddenFileInputs();
		if(globalFormStatus==="Completed") {	
			addReasonsToForm();
		}
		disableActionControls();
		doClickedSectionIds();
		LeavePageWarning.save();
		var formNameElm7 = $('[name="dataEntryForm.formName"]').eq(0);
		var currentFormName = formNameElm7.val();
		var formIdEle = document.getElementsByName("dataEntryForm.formId")[0];
		var formId = formIdEle.value;
		var intervalIdElm7 = $('[name="dataEntryForm.intervalId"]').eq(0);
		var currentIntervalId = intervalIdElm7.val();
		var patientIdElm7 = $('[name="dataEntryForm.patientId"]').eq(0);                     
		var currentSubjectId =  patientIdElm7.val();
		var visitDateElm7 = $('[name="dataEntryForm.visitDate"]').eq(0);
		var currentDateVal= visitDateElm7.val();
		
	  	var form = document.getElementById("myForm");
		form.action = "<s:property value="#webRoot"/>/response/dataCollection.action?action=nextForm&markAsCompletedCheckBoxStatus="
			+markAsCompletedCheckBoxStatus+"&currentIntervalId="+currentIntervalId+"&formId="+formId+"&currentFormName="
			+currentFormName+"&currentSubjectId="+currentSubjectId+"&currentDateVal="+currentDateVal+"&aformId="+<%=aformId%>
			+"&fileExist="+fileExist+"&assocFileQuestionIds="+assocFileQuestionIds;
		form.submit();
	});   
	
	 
	 $("#markAsCompletedCheckBox").click(function() {

		 <% if (enableEsignature) { %>
			 if ($('#markAsCompletedCheckBox').prop('checked')) {
				 $(".signatureForComplete").dialog("open");	
				 $("#completeLockChkBx").prop('checked', false);
				 if(passwordErrorId.length > 0){
				 	$.ibisMessaging("close", {id:passwordErrorId});
				 }
				 $("#userPassword").val("");
			 }
		<% } %>
		 
	 });
	 
	 
	 
	
	<%-- Action on previous button click --%>	
	$("#previousButton").click(function() {
		EventBus.trigger("open:processing", "Going to previous eForm...");
		setHiddenFileInputs();
		if(globalFormStatus==="Completed") {	
			addReasonsToForm();
		}
		disableActionControls();
		doClickedSectionIds();
		LeavePageWarning.save();
		var formNameElm7 = $('[name="dataEntryForm.formName"]').eq(0);
		var currentFormName = formNameElm7.val();
		var formIdEle = document.getElementsByName("dataEntryForm.formId")[0];
		var formId = formIdEle.value;
		var intervalIdElm7 = $('[name="dataEntryForm.intervalId"]').eq(0);
		var currentIntervalId = intervalIdElm7.val();
		var patientIdElm7 = $('[name="dataEntryForm.patientId"]').eq(0);                     
		var currentSubjectId =  patientIdElm7.val();
		var visitDateElm7 = $('[name="dataEntryForm.visitDate"]').eq(0);
		var currentDateVal= visitDateElm7.val();
		
	  	var form = document.getElementById("myForm");
		form.action = "<s:property value="#webRoot"/>/response/dataCollection.action?action=previousForm&markAsCompletedCheckBoxStatus="
			+markAsCompletedCheckBoxStatus+"&currentIntervalId="+currentIntervalId+"&formId="+formId+"&currentFormName="
			+currentFormName+"&currentSubjectId="+currentSubjectId+"&currentDateVal="+currentDateVal+"&aformId="+<%=aformId%>
			+"&fileExist="+fileExist+"&assocFileQuestionIds="+assocFileQuestionIds;
		form.submit();
	});
	
	if (showSavedMessage) {
		if (formNameToBeSavedOrLocked != null) {
			$.ibisMessaging("flash", "success", formNameToBeSavedOrLocked , {hideDelay: 3000});
		}
	}
	
	<%--This is the second document ready that was merged in the first one so that it maintains the order of handler that are registered --%>	
	initialize('<%=mode%>', '<%=formId%>', <%=formFetched%>, <%=editMode%>);
	$(".fancyBoxContainer").dialog({
		width: 1055, 
		autoOpen: false,
		modal: true,
		closeOnEscape: false,
		open: function( event, ui ) {
			//close X button while lunching this dialog.
			$(".ui-dialog-titlebar-close").hide();
			 $(".fancyBoxContainer").parent().css("position", "relative");
			 $(".fancyBoxContainer").parent().position({
				 of: window,
				 my: 'center top+50',
				 at: 'center top'
			 });
			 $(".fancyBoxContainer").find("input, textarea").css("width", "auto");
		},
		title: "<s:text name="response.reasonForChange.reasonForChange" />",
		buttons: [{
			text: "OK",
			click: function() {
				addReason();
			}
		}]
	});
		
	$(".finalLock").dialog({
		width: 785, 
		autoOpen: false,
		closeOnEscape: false,
		modal: true,
		open: function( event, ui ) {
			//close X button while lunching this dialog.
			$(".ui-dialog-titlebar-close").hide();
		},
		title: 'Collect Data Lock Confirmation'
	});
	
	
	
	$(".signatureForComplete").dialog({
		width: 785, 
		autoOpen: false,
		closeOnEscape: false,
		modal: true,
		open: function( event, ui ) {
			//close X button while lunching this dialog.
			$(".ui-dialog-titlebar-close").hide();
		},
		title: 'Signature Required'
	});
	
	
	
	//Mark all readOnly grey background this ready will called on inatialize so placing this call here.
	$('[readonly]').css('background-color','#dddddd');
	//maintain state of disable skip rule when it comces back with error
	$('[onchange]').each(function() {
		  if ($(this).attr("onchange").indexOf("applyskiprule") != -1) {
		    $(this).trigger("change");
		  }
		});
		
	//Select image map option value on keypress
	$(".ctdbImageMapAnswers").keypress(function() {
		$(".ctdbImageMapAnswers").val($(".ctdbImageMapAnswers option:first").val());
	});
			 
	 //This is highitlighter for keyboard navigation
	 $(".questionTR").find("input, textarea, select").focus(
			   function() {		 
			    	$(this).parents("tr").first().addClass("focuseddTr");			    	
			    	//Scroll top the question tr containing validation error
			    	 if(comingFromValidationErrorGoto) {
				    	var currentTr = $(this).parents(".questionTR").first();
				    	var  trPosition = currentTr.offset().top;
				    	$(window).scrollTop(trPosition);
				    	comingFromValidationErrorGoto = false;
			    	}
			    }).blur(
			    function(){			     
			    	$(this).parents("tr").first().removeClass("focuseddTr");
			    });
	 
	
	 
	 
	 //here we are calling custom function on keypress
	  $('.hasDatepicker').keydown(function(event) {	
		 $.datepicker.customKeyPress(event);
	  }); 
	
	 //This will extend the capabilty to add short cuts t->today date;(y)ea(r)->y for previous year and r for next year.
	  $.extend($.datepicker, { customKeyPress: function (event) {
		 var inst = $.datepicker._getInst(event.target);
		 var c = String.fromCharCode(event.which).toLowerCase();
		 switch (c) {
		 case "t":
			// Today (same as Ctrl+Home).
			 $.datepicker._gotoToday(event.target);
			 break;
		
		 case "y":
			 //Go to previous year(alternate to Ctrl+Page Up)
			 $.datepicker._adjustDate(event.target, -12, "M");
			 break;
				 
		 case "r":
			 //Go to next year(alternate to Ctrl+Page Down)
			 $.datepicker._adjustDate(event.target, +12, "M");
			 break;
		 case "m":
			 //Go to previous month
			 $.datepicker._adjustDate(event.target, -1, "M");
			 break;
		 case "h":
			 //Go to next month
			$.datepicker._adjustDate(event.target, -1, "M");;
			 break;
		 case "w":
			 //first day of the (w)eek
			 var date = new Date(inst.selectedYear, inst.selectedMonth, inst.selectedDay);
			 var offset = (date.getDay() > 0 ? date.getDay() : 7);
			 $.datepicker._adjustDate(event.target, -offset, "D");
			 break;
		 case "k":
			 //// Last day of the wee(k).
			 var date = new Date(inst.selectedYear, inst.selectedMonth, inst.selectedDay);
			 var offset = (date.getDay() < 6 ? 6 - date.getDay() : 7);
			 $.datepicker._adjustDate(event.target, offset, "D");
			 break;
		 case "p":
			 //previous day
			 $.datepicker._adjustDate(event.target, -1, 'D');
			 break;
		 case "n":
			 //next day
			 $.datepicker._adjustDate(event.target, +1, 'D');
			 break;
		 }
	 	}
	 }); 
	 

	 //This code will check if this is tablet device or not 
	 var clickHandler="click";
	if ("ontouchstart" in document.documentElement) {
		//binding click handler event to touchstart
		clickHandler = "touchstart";
	 	 $(".hasDatepicker").focus(
			  function(){
				  $(this).attr("readonly", "readonly");
		});
	  
	    
		 //To enable support for android devices, just modify the feature detection code with the following code:
		 $.extend($.support, {
       	 touch: "ontouchend" in document
			});
	 
		 $(".hasDatepicker").bind('touchmove',function(e){
	      e.preventDefault();
	      var touch = e.originalEvent.touches[0] || e.originalEvent.changedTouches[0];
	      $(this).attr("readonly", "readonly");
		});
	 
		//plugin form touh punch js to make slider work in touch deviceces(fixes visual scale question type)
		  $('#widget').draggable({
				onDragStart: function(event) {
					event.preventDefault();
				}
		  });
		//disable fileInput in chrome for tablet
			//var isChrome = window.chrome;
			//if (isChrome) {
				
				 //$(".fileInput").focus( 
					//function() {
						 // $(this).attr("disabled", true);
						 // alert("File upload is not supported in Chrome in tablets.Please use firefox if you want this feature.");
					//});
			//}
		}
		
	isDocumentLoaded = true;
	
	// Handle IE issue that causes the back button midigation code to fire
 	// collapsable sections are expanded or collapse.
 	$(".toggleable").click(function(event) {
 		if ( BrowserDetect.browser == "Explorer" ) {
 			LeavePageWarning.ignoreOnce();
 		}
 	});
	
	$(".toggleable").dblclick(function(event) {
		LeavePageWarning.ignoreWarning = false;
	});
	
	/*$(document).tooltip({
		open: function(event, ui) {
			$(ui.tooltip).siblings(".ui-tooltip").remove();
		}
	});*/
	
	// added by Ching-Heng for promis	
	$('.goPROMIS').click(function(){
		$('#myForm').ajaxSubmit({
		    url: "<s:property value="#webRoot"/>/response/dataCollection.action?action=validateMainGroup&isSelfReporting=false&aformId="+<%=aformId%>,
		    cache: false,
		    type: "POST",
		    data: "",
		    dataType: "json",
		    beforeSend: function(xhr) {
		    	
		    },	
		    success: function(data) {
		    	
		    	if(data.status!=""){
		    		$.ibisMessaging("primary", "error", data.status, {container: "#messageContainer"});
		    		$("#messageContainer").show();
		    	}else{
		    		$("#messageContainer").empty();
		    		$('div[name="Main"]').hide();
		    		$('div[name="Form Administration"]').hide();
		    		$('#acknowledgement').hide();
		    		initiateCATs();
		    	}
		    },	
		    error: function(jqXHR, textStatus, errorThrown) {
		        document.write(jqXHR.responseText + ':' + textStatus + ':' + errorThrown);
		    }
		});		
	});
	
	$("#promis_saveLock").click(function(){
		$("input:checkbox[name='markChecked']").prop( "checked", true );
		$("#lockButtonPatient").click();
	});
	
	$("#promis_cancel").click(function(){
		$("#goCancel").click();
	});
	// ====================

    //calling intervalOnChange to initialize disabling/enabling of eforms in the eform list dropdown
	intervalOnChange();
														
});

function saveFLAjax() {
	   	var url = "<s:property value="#webRoot"/>/response/collectDataPreviousHome.action";
	   	$('#myForm').ajaxSubmit({
	   		data: {clickedSectionFields:$("#clickedSectionFieldsId").val(), formTypeId:$("#formTypeId").val(), modeId:$("#modeId").val()},	
	   		url :  "<s:property value="#webRoot"/>/response/dataCollection.action?action=saveFormFL&markAsCompletedCheckBoxStatus="+
				markAsCompletedCheckBoxStatus+"&aformId=" +<%=aformId%>+ "&editMode=" + globalEditFlag+"&assocFileQuestionIds="+assocFileQuestionIds,
			success: function(response, status, jqxhr) {
				EventBus.trigger("close:processing");
				var data = JSON.parse(response);
		  		var st = data.status;
		  		if (st.toLowerCase().indexOf("<li")==0) {
		        	$.ibisMessaging("close", {type:"primary"}); 
					$.ibisMessaging("primary", "error", '<s:text name="errors.text"/></b>'+st, {hideDelay: 3000});

		  		}else if (st == "saveFormSuccess") {
		  			
		  			var fname = data.fName;	
		  			$.ibisMessaging("store", "primary", {message: 
							"The administered form "+ fname +" has been saved successfully.", level: "success"});
					redirectWithReferrer(url);	
		  		}else if(st == 'duplicateData'){
		  			$.ibisMessaging("close", {type:"primary"}); 
		  			$.ibisMessaging("store", "primary", {message: dataCollectionErrorMsg, level: "error"});
		  			redirectWithReferrer(url);
		  		}else if(st == 'dataCollectionException'){
		  			$.ibisMessaging("close", {type:"primary"}); 
		  			$.ibisMessaging("store", "primary", {message: dataCollectionErrorMsg, level: "error"});
		  			redirectWithReferrer(url);	

		  		}else {
		  			$.ibisMessaging("close", {type:"primary"}); 
		  			$.ibisMessaging("store", "primary", {message: dataCollectionErrorMsg, level: "error"});
		  			redirectWithReferrer(url);  		
		  		}
		  		enableActionControls();	
				
			},
	   		error: function (xhr, ajaxOptions, thrownError) {
	        	EventBus.trigger("close:processing");
	  			$.ibisMessaging("close", {type:"primary"}); 
	  			$.ibisMessaging("store", "primary", {message: dataCollectionErrorMsg, level: "error"});
	  			redirectWithReferrer(url);
	        }
	   }); 
}
</script>

<script type="text/javascript">
	var lockClicked = false;		
	var intervalsArray = new Array();
	
	<s:property value="#request.jsResponseList" escapeHtml="false" />
    
var LogoutWarning = {
	timer : null,
	logoutAfterTimer : null,
	logoutWarningId : null,
	html : 'Your authentication session is about to expire due to inactivity.  <b>All unsaved data will be lost</b>.  Please click the button below to extend your session.',	
	init : function() {
		this.startTimer();
	},
	
	openPopup : function() {
		this.logoutWarningId = $.ibisMessaging(
				"dialog", 
				"warning", 
				this.html, 
				{
					buttons: [{
						text: "Extend My Session", 
						click: function(){LogoutWarning.cancelLogout();}
					}]
				}
		);
		this.startLogoutTimer();
	},
	
	startTimer : function() {
		this.timer = setTimeout(function(){LogoutWarning.openPopup();}, <s:property value="#systemPreferences.get('app.warningTimeout')"/>);
	},
	
	cancelLogout : function() {
		$.ibisMessaging("close", {id: LogoutWarning.logoutWarningId});
		this.cancelLogoutTimer();
		this.startTimer();
	},
	
	logout : function() {
		this.redirectToLogout();
	},
	
	redirectToLogout : function() {
		top.location.href = '<s:property value="#webRoot"/>/logout';
	},
	
	startLogoutTimer : function() {
		this.logoutAfterTimer = setTimeout(function(){LogoutWarning.logout();}, 120000);
	},
	
	cancelLogoutTimer : function() {
		window.clearTimeout(this.logoutAfterTimer);
		this.logoutAfterTimer = null;
	}
};


var AutoSaver = {
	noticeTimer : null,
	saveTimer : null,	
	init : function() {
		if ('<%=formStatus%>' == '<%=CtdbConstants.DATACOLLECTION_STATUS_INPROGRESS%>'){
			//this.startTimer();
		}
	},
	
	startTimer : function() {
		this.saveTimer = setInterval(function(){AutoSaver.start();}, <s:property value="#systemPreferences.get('app.autoSaveTimeout')"/>);
	},
	
	/**
	 * starts the save process for the form.  Collects data from the form
	 * and submits it to the server.
	 * @param callback a callback function to call after completing the save
	 */
	start : function(callback) {
		commonSaveExitLockTask();
			var form = $("#myForm").serialize();			
			// start the ajax!
			$.post(
				"<s:property value="#webRoot"/>/response/dataCollection.action?action=saveForm&comingFromAutoSaver=true&method=ajax&aformId="+
					<%=aformId%>+"&editMode=" + globalEditFlag+"&fileExist="+fileExist+"&assocFileQuestionIds="+assocFileQuestionIds,
				form,
				function(data, textStatus, xhr) {
					if (data.indexOf("badMessage") != -1 || xhr.status == "500") {
						AutoSaver.finished(false, callback);
					} else {
						AutoSaver.finished(true, callback);
					}
				}
			);		
	},
	
	/**
	 * Receives the feedback from the server after a save attempt.
	 */
	finished : function(success, callback) {
		enableActionControls();
		if (success) {
			this.success();
		} else {
			this.error();
		}
		
		if (typeof callback != "undefined") {
			callback();
		}
	},
	
	success : function() {
		this.showNotice(true, "Auto-Save Complete");
	},
	
	error : function() {
		this.showNotice(false, "Auto-Save Not Successful!");
	},
	
	showNotice : function(success, message) {
		var dispClass = "success";
		if (!success) {
			dispClass = "error";
		}
		$.ibisMessaging("flash", dispClass, message, {hideDelay: 3000});
	}
};
    
    
var LeavePageWarning = {
	alertMessage : "This form has been edited since it was last saved.  Are you sure you want to leave this page?",
	noteMessage : "We recommend you exclusively use ProFoRMS navigation buttons and links within the Collect Data module. Leaving the form by any other method (Back/Forward buttons, backspace key, etc.) may result in data loss and unexpected errors.",
	leaveButtonText : "Leave",
	stayButtonText : "Stay",
	serializedForm : "",
	pageToGoTo : "<s:property value="#webRoot"/>" + "/response/collectDataPreviousHome.action",
	formUpdated : false,
	ignoreWarning : false,
	
	init : function() {
		// ensure we're not on page one
		if (!this.onPageOne()) {
			$(window).bind('beforeunload', function(event) {
				return LeavePageWarning.leavePage();
			});
			
			$('input, select').change(function() {
				LeavePageWarning.formUpdated = true;
			});
			$('textarea, input[type="text"]').on("keydown", function() {
				LeavePageWarning.formUpdated = true;
			});
		}
		else {
			this.showNotification();
		}
	},
	
	leavePage : function() {
		if (!this.isFormSaved() && !this.ignoreWarning) {
			return this.displayAlert();
		}
		
		this.ignoreWarning = false;
	},
	
	onPageOne : function() {
		var startButton = $("#sdcID");
		return startButton.length > 0;
	},
	
	isFormSaved : function() {
		return !this.formUpdated;
	},
	
	displayAlert : function() {
		return this.leaveButtonText;
	},
	
	save : function() {
		this.formUpdated = false;
	},
	
	ignoreOnce : function() {
		this.ignoreWarning = true;
	},
	
	showNotification : function() {
		$.ibisMessaging("primary", "warning", this.noteMessage);
	}
};







/**
 * this function is called in body onLoad
 */
function initialize(mode, fId, formFetched, eMode) {

	
	
	<%=request.getAttribute("disableStr")%>
	
	
	if(globalFormStatus==="Final Lock" || globalFormStatus=="Completed") {
		$('#markAsCompletedCheckBox').attr('disabled',true);
		bindReasonForChange();
	}
	
	if (eMode) {
		var formId = document.getElementsByName("dataEntryForm.formId")[0];
		if (fId )
		formId.value = fId;
		var modeElm = document.getElementsByName("mode")[0];
		modeElm.value = mode;
		
		$('[name="dataEntryForm.formName"]').attr("readonly", "readonly");
		$('[name="dataEntryForm.visitDate"]').attr("readonly", "readonly");

		if (mode == "formPatient") {
			$('[name="dataEntryForm.subjectId"]').attr("readonly", "readonly");
			$('[name="dataEntryForm.guid"]').attr("readonly", "readonly");
			$('[name="dataEntryForm.mrn"]').attr("readonly", "readonly");
			$('[name="dataEntryForm.intervalName"]').attr("readonly", "readonly");
			
			
		} 
		
	} else {
		if (mode == "formPatient") {
			$('[name="dataEntryForm.formName"]').attr("readonly", "readonly");
			var formId = document.getElementsByName("dataEntryForm.formId")[0];
			formId.value = fId;
			var modeElm = document.getElementsByName("mode")[0];
			modeElm.value = mode;
			
			if (formFetched) {			
				$('[name="dataEntryForm.subjectId"]').attr("readonly", "readonly");
				$('[name="dataEntryForm.mrn"]').attr("readonly", "readonly");
				$('[name="dataEntryForm.guid"]').attr("readonly", "readonly");
				$('[name="dataEntryForm.visitDate"]').attr("readonly", "readonly");
				$('[name="dataEntryForm.intervalName"]').attr("readonly", "readonly");
				
			} else {
				var intervalIdElm = document.getElementsByName("dataEntryForm.intervalId")[0];
				var intervalIdElmVal = intervalIdElm.options[intervalIdElm.selectedIndex].value;
				
				var patientIdElm = document.getElementsByName("dataEntryForm.patientId")[0];
				var patientIdElmVal = patientIdElm.options[patientIdElm.selectedIndex].value;
				
				if (intervalIdElmVal=="noIntervals" || patientIdElmVal=="noPatients") {
					var sdcButton = document.getElementById("sdcID");
					sdcButton.disabled = "true";
				}
			}
		}
		
	
		
		if (mode == "patient") {
			$('[name="dataEntryForm.subjectId"]').attr("readonly", "readonly");
			$('[name="dataEntryForm.mrn"]').attr("readonly", "readonly");
			$('[name="dataEntryForm.guid"]').attr("readonly", "readonly");
			$('[name="dataEntryForm.intervalId"]').attr("readonly", "readonly");
			
			
			var modeElm = document.getElementsByName("mode")[0];
			modeElm.value = mode;
	
			if (formFetched) {
				$('[name="dataEntryForm.formName"]').attr("readonly", "readonly");
				$('[name="dataEntryForm.visitDate"]').attr("readonly", "readonly");
				$('[name="dataEntryForm.intervalName"]').attr("readonly", "readonly");
			} else {
				var formNameElm = document.getElementsByName("dataEntryForm.formName")[0];
				var optionFormId =  formNameElm.options[formNameElm.selectedIndex].value;
				var formId = document.getElementsByName("dataEntryForm.formId")[0];
				formId.value = optionFormId;
				
				var intervalIdElm = document.getElementsByName("dataEntryForm.intervalId")[0];
				for (var i=0; i<intervalIdElm.length; i++) {
					if (intervalIdElm.options[i].text == "<%=pIntervName%>") {
						intervalIdElm.options[i].selected=true;
						break;
					}
				}
				
				//in this mode we need to disable to visit type drop down and set the hidden input
				var val = $('[name="dataEntryForm.intervalId"]').val();
				$('[name="dataEntryForm.intervalId"]').prop("disabled", true);
				$('[name="dataEntryForm.intervalIdHidden"]').val(val);
				
				
				var visitDateElm = document.getElementsByName("dataEntryForm.visitDate")[0];
				if ("<%=pVisitDate%>" != "null" && "<%=pVisitDate%>" != "") {
					alert("get <%=pVisitDate%>");
					visitDateElm.value = "<%=pVisitDate%>";
				}
				
				var formNameElm = document.getElementsByName("dataEntryForm.formName")[0];
				var formNameElmVal = formNameElm.options[formNameElm.selectedIndex].value;
				
				if (formNameElmVal=="noActiveForms") {
					var sdcButton = document.getElementById("sdcID");
					sdcButton.disabled = "true";
				}
			}			
		}
	}
	
	if (formFetched) {
		$("#dcTitle").click();
	}
	
	//visit date de is prepopped with scheduled visit date unless its psr or if user starts collection by selecting eform
	//rather than by selecting scheduled visit. (in those cases, its prepopped with current date).  so....
	//if user starts collection, we need to give warning message on top if the prepopped date is not equal to todays 
	//current date
	if(formFetched && <%=displayVisitDateWarning%>) {
		var visitDateElem = $("[dataelementname=VisitDate]").find("input");
		if(visitDateElem.length > 0) {
			var visitDate = visitDateElem.val();
			var d = new Date(visitDate);
			var dMonth = d.getMonth();
			var today = new Date();
			var todayMonth = today.getMonth();
			var dDate = d.getDate();
			var todayDate = today.getDate();
			var isToday = false;
			if(dMonth == todayMonth && dDate == todayDate) {
				isToday = true;
			}
			if(!isToday) {
				//display warning
				var message = "<s:text name="warning.datacollection.visitdate"/>";
				$.ibisMessaging("primary", "warning", message);
			}

		}
		
	}
}





function checkButtons() {	
}


function goToValidationErrorFlag() {
	comingFromValidationErrorGoto = true;
}

function cancel() {
	
	var url;
	if (globalEditFlag) {
		//In edit mode take to my collections page on cancel click
		url = "<s:property value='#webRoot'/>/response/collectDataPreviousHome.action";
		LeavePageWarning.save();
	} else {
		//In start mode take to data collection page on cancel click
		if (globalDeleteOnCancel) {
			var basePath = "<s:property value='#webRoot'/>";
			<%if (aformId != String.valueOf(Integer.MIN_VALUE)) { %>
				url = basePath + '/response/deleteDataEntry.action?cancelFlag=1&id='+<%=aformId%>;
				<%} else {%>	
				url = "<s:property value='#webRoot'/>/response/collectDataPreviousHome.action";
				<%} %>
				
		} else {
			url = "<s:property value='#webRoot'/>/response/dataCollectingLandingSearch.action";
			LeavePageWarning.save();
		}
	}
	redirectWithReferrer(url); 
}


function fetchForm() {
	var mode = "<%=mode%>";
	if (mode == "patient" || mode == "formPatient") {		
		var intervalIdElm = document.getElementsByName("dataEntryForm.intervalId")[0];
		var optionIntervalId =  intervalIdElm.options[intervalIdElm.selectedIndex].value;
		if (optionIntervalId == "pleaseSelect") {
			$.ibisMessaging("dialog", "info", "Please select a visit type.");
			return;
		}		

		var visitDateElm = document.getElementsByName("dataEntryForm.visitDate")[0];
		if (trim(visitDateElm.value) == "") {
			$.ibisMessaging("dialog","info","Please select a visit date.");
			return;
		}
	}
	
	if (mode == "patient") {		
		var formNameElm = document.getElementsByName("dataEntryForm.formName")[0];
		var optionFormId =  formNameElm.options[formNameElm.selectedIndex].value;
		
		if (optionFormId == "pleaseSelect" || optionFormId == "noForms") {
			$.ibisMessaging("dialog", "info", "Please select a form.  " + 
					"If there are no forms in list, the visit type has no associated forms.");
			return;
		}	
	} else if (mode == "formPatient") {
		var patientIdElm = document.getElementsByName("dataEntryForm.patientId")[0];
		var optionPatientId = patientIdElm.options[patientIdElm.selectedIndex].value;
		
		if (optionPatientId == "pleaseSelect") {
			$.ibisMessaging("dialog","info","Please select a Subject.");
			return;
		}
	}
	
	$('#sdcID').prop('disabled', true);
	var form = document.getElementById("myForm");
	form.action = "<s:property value="#webRoot"/>/response/dataCollection.action?action=fetchForm&startVisitType=true";
	form.submit();
}


function test() {
	var url = "<s:property value="#webRoot"/>/response/dataCollection.action?action=fetchFormPSR&patientId=17&formId=1012&visitTypeId=2";
	redirectWithReferrer(url);
}



function URLDecode(psEncodeString) {
	  // Create a regular expression to search all +s in the string
	  var lsRegExp = /\+/g;
	  return decodeURIComponent(String(psEncodeString).replace(lsRegExp, " "));
}




function intervalOnChange() {
	var intervalId = document.getElementsByName("dataEntryForm.intervalId");
 	
 	if(typeof intervalId != "undefined") {
 		var intervalIdElm = intervalId[0];
 		if(typeof intervalIdElm.selectedIndex != 'undefined') {
 	 		var optionIntervalId = intervalIdElm.options[intervalIdElm.selectedIndex].value;
 	 		var patientId = $('[name="dataEntryForm.patientId"]').val();
 	 		var url = "<s:property value="#webRoot"/>/response/dataCollection.action?action=process_populateFormNamesAJAX&optionIntervalId=" + optionIntervalId + "&patientId=" + patientId;
 	 		 $.ajax({
 	 			type: "POST",
 	 			url: url,
 	 			data: optionIntervalId,
 	 			success: function(response) {
 	 				var selectObject = document.getElementsByName("dataEntryForm.formName")[0];
 	 				for (var i=selectObject.length; --i >= 0;) {
 	 					var optionElement = selectObject.options[i];
 	 					selectObject.removeChild(optionElement);
 	 				}						
 	 				var formsJSONArray=JSON.parse(response);				
 	 				for (var i=0; i<formsJSONArray.length; i++) {				
 	 						var fId = formsJSONArray[i].id;
 	 						var fName = formsJSONArray[i].name;
 	 						var disabled = formsJSONArray[i].disabled;
 	 						var option = document.createElement("option");				
 	 						option.appendChild(document.createTextNode(fName));
 	 						option.setAttribute("value", fId);
 	 						if(disabled) {	
 	 							option.setAttribute("disabled", disabled);
 	 						}
 	 						selectObject.appendChild(option);				
 	 				}			
 	 				var formNameElm = document.getElementsByName("dataEntryForm.formName")[0];
 	 				var optionFormId =  formNameElm.options[formNameElm.selectedIndex].value;
 	 				var formId = document.getElementsByName("dataEntryForm.formId")[0];
 	 				formId.value = optionFormId;
 	 			},
 	 			error: function(e) {
 	 				alert("error" + e );
 	 			}
 	 		});
 		}
 	}
}

function formNameOnChange() {
	var formNameElm = document.getElementsByName("dataEntryForm.formName")[0];
	var optionFormId =  formNameElm.options[formNameElm.selectedIndex].value;
	var formId = document.getElementsByName("dataEntryForm.formId")[0];
	formId.value = optionFormId;	
}

/************** SELECT ALL IMAGE MAP ***************************
* Selects all of the choosen image map answers and
* submits the data entry form
* called by save  progress
*/
function selectAllImageMap() {
    var imAnswers = document.body.getElementsByTagName('SELECT');    
    if(imAnswers != null) {
	    for (i = 0; i < imAnswers.length; i++){
	        if (imAnswers[i].className =='ctdbImageMapAnswers'){
	            opts = imAnswers[i].options;
	            for (j=0; j < opts.length; j++){
	                opts[j].selected = true;
	            }
	        }
	    }
    }
}

function doClickedSectionIds() {
	$('#clickedSectionFieldsId').val(JSON.stringify(clickedSectionIds));
}



function bindReasonForChange() {
    var tehInputs = $("input, select, textarea").not('[type="hidden"]');
    tehInputs.each(function() {
    	if ($(this).readOnly) {
    		return true;
    	}
    	var imageMapIndex = this.id.indexOf("imageMap");
         if (imageMapIndex == -1) {
	    	if (this.id.indexOf("Q_") > -1) {
	    		$(this).change(function() {
	    			launchReasonForChange(this);
	    		});
	    	}
        }

    });
    $(".slider").bind("slidestop",function(event, ui) {
    	launchReasonForChange(this);
    });
    
}

var currQId;
function launchReasonForChange(elem) {
	if (globalFormStatus==="Final Lock" || globalFormStatus==="Completed") {
		if (isDocumentLoaded) {
			var errorContainer = document.getElementById("errorContainer");
			errorContainer.innerHTML="";
			errorContainer.style.display="none";
			$("#rChange").focus();
			
			var qId = elem.id;
			var sliderIndex = elem.id.indexOf("slider");
	    
	    	if (sliderIndex > -1) {
	       		qId = elem.id.substring(0, sliderIndex-1);
	    	} 
	    
			var imageMapIndex = elem.id.indexOf("imageMap");

	    	if (imageMapIndex > -1) {
	       		qId = elem.id.substring(9, elem.id.length);
	    	} 
		    
	    	qId=qId.replace("_otherBox", "");
	    	currQId = qId;
	    
		    document.getElementById('qId').value = qId;
		    document.getElementById('qId').disabled = true;
		    
		    //PS-2697: Replace &#39; htmlspecialchars with their correspondent character
		    var txt = jsResponses.get(qId).getQText();
		    var decodedTxt = txt.replace(/&#(\d+);/g, function(match, decVal) {
		                    return String.fromCharCode(decVal);
		      });
		    
		    document.getElementById('qText').value = decodedTxt;
		    
		    document.getElementById('qText').disabled = true;
		    document.getElementById('dEntry1').value =jsResponses.get(qId).getAnswer1();
		    document.getElementById('dEntry1').disabled = true;
		    //document.getElementById('dEntry2').value =jsResponses.get(qId).getAnswer2();
		   // document.getElementById('dEntry2').disabled = true;
		    
 
		    if (elem.type == "checkbox") {
		    	//making jQuery elem object
		     	var $elem = $(elem);
		    	var checkedAnswers ="";
		    	$('[name="'+$elem.prop("name") + '"]:checked').each(function() {
		    		checkedAnswers = checkedAnswers +":"+ $(this).val();		    		
		   		 }); 
		    	if (elem.disabled) {
		    		 document.getElementById('fAnswer').value = "";
		   		} else {
		    		document.getElementById('fAnswer').value = checkedAnswers.substring(1);
		   		}
		
		    } else {
		    	  if (elem.disabled) {
		    		  document.getElementById('fAnswer').value = "";
				  } else {
		    		//Changed the delimiter to : in case of multi select
		    		if (elem.type=="select-multiple") {
		    			document.getElementById('fAnswer').value =$(elem).val().join(":");
		    		} else {
		    			document.getElementById('fAnswer').value =$(elem).val();
		    		}
				  }
		    }
		    
		    document.getElementById('fAnswer').disabled = true;
		    document.getElementById('rChange').value = "";   
		    document.getElementById('elemId').value = elem.id;
		    $(".fancyBoxContainer").dialog("open");
		}
	}
}

function cancelFB() {
	$(".fancyBoxContainer").dialog("close");
}


//trim helper function
function trim(str) {
    return str.replace(/^\s+|\s+$/g,"");
}


function refreshAttachmentLinks(responseArray) {
	for(var i=0;i<responseArray.length;i++) {
		var response = responseArray[i];
		var id = response.id;
		var response1 = response.response1;
		var response2 = response.response2;
	 	var isFile = response.isFile;
	 	var attachment1Id = response.attachment1Id;
	 	var attachment2Id = response.attachment2Id;
	
	 	
	 	//TO DO....need to make the following work for second entry whenever we get double data to work again
	 	if(isFile) {
	 		
	 		//now need to update the link on page if we are in edit mode
	 		var $downloadLink = $('.fileDownloadLink[assoc_questionid="'+ id +'"]');
	 		if($downloadLink.length > 0) {
	 			$downloadLink.attr('filename',response1);
	 			$downloadLink.attr('attachmentid',attachment1Id);
	 			var clickHandler = "downloadQuestionFile('"+ attachment1Id +"','<%= formId %>')";
	 			$downloadLink.attr('onclick',clickHandler);
	 			$downloadLink.html(response1);
	 			
	 		}
	 		
	 		
	 		
	 		
	 	}
	}	
}



function rePopulateJsResponses(responseArray) {
	for(var i=0;i<responseArray.length;i++) {
		var response = responseArray[i];
		var id = response.id;
		var response1 = response.response1;
		var response2 = response.response2;
	 	var hashEntry = jsResponses.get(id);

	 	hashEntry.deOneAnswer=response1;
	 	hashEntry.deTwoAnswer=response2;	
	 	

	}	
}

function addReason() {
	var reason = document.getElementById('rChange').value;
	reason = trim(reason);	
	if (reason == "") {		
		$.ibisMessaging("close", {type:"primary"}); 
		$.ibisMessaging("primary", "error", "<s:text name="errors.text"/><b>Reason for Change is required.</b>",{container: "#errorContainer"});
		$("#errorContainer").show();
	} else {
		$(".fancyBoxContainer").dialog("close");
		jsResponses.get(currQId).setChangeReason(document.getElementById('rChange').value);
	}	
}


function addReasonsToForm() {
    var responses = jsResponses.values();
    for (i=0; i < responses.length; i++) {
        var reason = responses[i].getChangeReason();
        if (reason != null && reason != "") {
            try {
            	if ($('input[name=reason_'+responses[i].getQuestionId()+']').length > 0){
            		$('input[name=reason_'+responses[i].getQuestionId()+']').remove();
            	}
            	var element = document.createElement("<INPUT type='hidden' id='reason_"+responses[i].getQuestionId()+"' name='reason_"+responses[i].getQuestionId()+"' value='"+reason+"'/>");
            } catch (err) {
                //stupid firefox
              	if ($('input[name=reason_'+responses[i].getQuestionId()+']').length > 0){
                	$('input[name=reason_'+responses[i].getQuestionId()+']').remove();
              	}
                var element = document.createElement("INPUT");
                element.setAttribute("type", "hidden");
                element.setAttribute("name", "reason_"+responses[i].getQuestionId());
                element.setAttribute("value", reason);
                element.setAttribute("id", "reason_"+responses[i].getQuestionId());
            }
            document.getElementById('myForm').appendChild(element);
        }
    }
}


function popupAttWindow(url) {	
	var WindowArgs = "toolbar=no," + "location=no," + "directories=no," + "status=yes,";
	openPopup(url, "", WindowArgs+"top=10,left=10,scrollbars=yes,resizable=yes,width=800,height=600");
}


/* added by Ching Heng for download the question file */
function downloadQuestionFile(fileID,formID){
	url = "<s:property value="#webRoot"/>/attachments/download.action?id="+fileID+"&associatedId="+formID+"&typeId=<%=AttachmentManager.FILE_COLLECTION%>";
	redirectWithReferrer(url);
}

/* added by Ching Heng, make Reset button can reset the Visual Scale and Image Map */
function resetVSandIM(){
	$(".ctdbImageMapAnswers").html('');
	resetSlider();
}
</script>

<body>
	<h3 class="toggleable expanded" id="dcTitle">
		<s:text name="response.collect.title.display" />
	</h3>
	
	<div id="dataCollectionEntry">
		<p><s:text name="response.collect.instruction2" /></p><br> 
		<label class="requiredInput"></label> 
		<i><s:text name="app.requiredSymbol" /></i> <br><br>

		<s:form id="myForm" method="post" enctype="multipart/form-data">
			<s:hidden name="mode" id="modeId" />
			<s:hidden name="dataEntryForm.formId" />
			<s:hidden name="dataEntryForm.userPassword" id="userPasswordId" />
			<s:hidden name="dataEntryForm.clickedSectionFields" id="clickedSectionFieldsId" />
			<s:hidden name="dataEntryForm.formType" id="formTypeId" />
			<s:hidden name="dataEntryForm.scheduledVisitDate" />
			<s:hidden name="dataEntryForm.visitDate" />
			<%-- <s:hidden name="dataEntryForm.deComments" id="deCommentsId"/> --%>
			
	<%
		if (mode.equals("formPatient")) {
			if (!editMode && !formFetched) {
	%>
			
			<%if (subjectDisplayType == CtdbConstants.PATIENT_DISPLAY_GUID) { %>
				<div class="formrow_1">
					<label for="patientId" class="requiredInput"> <s:text name="response.collect.label.inhrecord" /></label>
					<s:select name="dataEntryForm.patientId" list="#request.patientList" listKey="id" listValue="guid" 
						headerKey="pleaseSelect" headerValue="- Please Select" />
				</div>
			<%} else if (subjectDisplayType == CtdbConstants.PATIENT_DISPLAY_ID) {%>

				<div class="formrow_1">
					<label for="patientId" class="requiredInput"> <s:text name="response.collect.label.inhrecord" /></label>
					<s:select name="dataEntryForm.patientId" list="#request.patientList" listKey="id" listValue="displayLabel" 
						headerKey="pleaseSelect" headerValue="- Please Select" />
				</div>
			<%} else if (subjectDisplayType == CtdbConstants.PATIENT_DISPLAY_MRN) {%>
				<div class="formrow_1">
					<label for="patientId" class="requiredInput"> <s:text name="response.collect.label.inhrecord" /></label>
					<s:select name="dataEntryForm.patientId" list="#request.patientList" listKey="id" listValue="mrn" 
						headerKey="pleaseSelect" headerValue="- Please Select" />
				</div>
			
			<%} %>
			
			<div class="formrow_1">
				<label for="intervalId" class="requiredInput"><s:text name="response.label.interval" /></label>
				<s:select name="dataEntryForm.intervalId" list="#request.intervalOptions" listKey="key" listValue="value" 
						headerKey="pleaseSelect" headerValue="- Please Select" />
			</div>

			<div class="formrow_1">
				<label for="formName"> <s:text name="response.collect.label.formname" /></label>
				<s:textfield name="dataEntryForm.formName" size="20" maxlength="50" />
			</div>

		
			
		<% } else { %>
		
			<div class="formrow_1">
			
				<%if (subjectDisplayType == CtdbConstants.PATIENT_DISPLAY_GUID) { %>
								<label for="subjectId"> <s:text name="response.resolveHome.tableHeader.subjectGUID" /></label>
							<s:textfield name="dataEntryForm.guid" size="20" maxlength="50" />
						<%} %>
						
						<%if (subjectDisplayType == CtdbConstants.PATIENT_DISPLAY_ID) { %>
							<label for="subjectId"> <s:text name="subject.table.subjectID"/></label>
						<s:textfield name="dataEntryForm.subjectId" size="20" maxlength="50" />
						<%} %>
					<%if (subjectDisplayType == CtdbConstants.PATIENT_DISPLAY_MRN) { %>
							<label for="subjectId"> <s:text name="patient.scheduleVisit.mrn.display"/></label>
						<s:textfield name="dataEntryForm.mrn" size="20" maxlength="50" />
					<%} %>
				
				
			</div>

			<s:hidden name="dataEntryForm.patientId" />
			<s:hidden name="dataEntryForm.intervalId" />
			
			<div class="formrow_1">
				<label for="intervalName"><s:text name="response.label.interval" /></label>
				<s:textfield name="dataEntryForm.intervalName" size="20" maxlength="50" />
			</div>

			<div class="formrow_1">
				<label for="formName"> <s:text name="response.collect.label.formname" /></label>
				<s:textfield name="dataEntryForm.formName" size="20" maxlength="50" />
			</div>

		

	<% 	}} else if (mode.equals("patient")) {
			if (!editMode && !formFetched) { %>
			
			<div class="formrow_1">
			
					<%if (subjectDisplayType == CtdbConstants.PATIENT_DISPLAY_GUID) { %>
						<label for="subjectId"> 
						<s:text name="response.resolveHome.tableHeader.subjectGUID" />
						</label>
						<s:textfield name="dataEntryForm.guid" size="20" maxlength="50" />
						<%} %>
						<%if (subjectDisplayType == CtdbConstants.PATIENT_DISPLAY_ID) { %>
						<label for="subjectId"> 
						<s:text name="subject.table.subjectID"/>
						</label>
						<s:textfield name="dataEntryForm.subjectId" size="20" maxlength="50" />
						<%} %>
					<%if (subjectDisplayType == CtdbConstants.PATIENT_DISPLAY_MRN) { %>
						<label for="subjectId"> 
						<s:text name="patient.scheduleVisit.mrn.display"/>
						</label>
						<s:textfield name="dataEntryForm.mrn" size="20" maxlength="50" />
					<%} %>
							

			</div>

			<s:hidden name="dataEntryForm.patientId" />
			<s:hidden name="dataEntryForm.intervalIdHidden" />

			<div class="formrow_1">
				<label for="intervalId" class="requiredInput"><s:text name="response.label.interval" /></label>
				<s:select name="dataEntryForm.intervalId" onchange="intervalOnChange()" list="#request.intervalOptions" 
					listKey="key" listValue="value" headerKey="pleaseSelect" headerValue="- Please Select" />
			</div>

			<div id="formNameDiv" class="formrow_1">
				<label for="formName" class="requiredInput"> <s:text name="response.collect.label.formname" /></label>
				<s:select name="dataEntryForm.formName" onchange="formNameOnChange()" list="#session.activeforms" 
					listKey="key" listValue="value" headerKey="pleaseSelect" headerValue="- Please Select" />
			</div>

			
			
		<%  } else { %>

			<div class="formrow_1">
				<%if (subjectDisplayType == CtdbConstants.PATIENT_DISPLAY_GUID) { %>
						<label for="subjectId"> 
						<s:text name="response.resolveHome.tableHeader.subjectGUID" />
						</label>
						<s:textfield name="dataEntryForm.guid" size="20" maxlength="50" />
						<%} %>
						<%if (subjectDisplayType == CtdbConstants.PATIENT_DISPLAY_ID) { %>
						<label for="subjectId"> 
						<s:text name="subject.table.subjectID"/>
						</label>
						<s:textfield name="dataEntryForm.subjectId" size="20" maxlength="50" />
						<%} %>
					<%if (subjectDisplayType == CtdbConstants.PATIENT_DISPLAY_MRN) { %>
						<label for="subjectId"> 
						<s:text name="patient.scheduleVisit.mrn.display"/>
						</label>
						<s:textfield name="dataEntryForm.mrn" size="20" maxlength="50" />
					<%} %>
			</div>
			
			<s:hidden name="dataEntryForm.patientId" />
			<s:hidden name="dataEntryForm.intervalId" />
			
			<div class="formrow_1">
				<label for="intervalName"><s:text name="response.label.interval" /></label>
				<s:textfield name="dataEntryForm.intervalName" size="20" maxlength="50" />
			</div>
			
			<div class="formrow_1">
				<label for="formName"> <s:text name="response.collect.label.formname" /></label>
				<s:textfield name="dataEntryForm.formName" size="20" maxlength="50" />
			</div>
			

			
	<%} } if (!formFetched) { %>
			
			<div>
				<div style="float: right;margin-left: 5px">
					<input type="button" value="<s:text name='button.Cancel' />" title="Click to cancel (changes will not be saved)." 
							alt="Cancel" onclick="cancel()" />
				</div>
				<div style="float: right;margin-right: 5px">
					<input type="button" id="sdcID" value="<s:text name="repoonse.collectDataLandingHome.startDataCollection" />" 
							title="Click to start Data Collection" alt="Start Data Collection" />
				</div>
			</div>
	<%	} %>	
			
	</div>
	<br><br>
	
	<% 	if (formFetched) {
			if (attachFiles) { 
	%>
		<a href="Javascript:void(0);" onClick="popupAttWindow('<s:property value="#webRoot"/>/attachments/formAttachment.do?hideNav=true&typeId=3&associatedId=<%=aformId%>')">Manage Attachments</a>
		<br>
	<% 		} %>
	
	<div class="floatRight" >
		 <div class="greenCircle floatLeft statusCir"></div><div class="floatLeft height30px" >Locked</div>
		 <div class="redCircle floatLeft statusCir"></div><div class="floatLeft height30px" >In Progress</div>
		 <div class="yellowCircle floatLeft statusCir"></div><div class="floatLeft height30px" >Completed</div> 
		 <div class="whiteCircle floatLeft statusCir"></div><div class="floatLeft height30px" >Not Started</div>
		 <div class="marginBottom10px" ><span class="paintRed" >*</span> Letter R inside circle means required form for that visit type </div>
 	</div>
	<div style="clear: both;"></div>

	<div style="display: table; width: 100%"> 
		<div id="colorFormIntervalNavigator" style="display: table-cell; width: 229px;height: 700px;border: 1px solid black;background: #F0F0F0; vertical-align: top;" >
			
				<div id="leftSideHeader">
					<h3 id="formsInThisVisitTypeH">
						<s:text name="response.collect.formsInThisVisitType.display" />
					</h3>
				</div>
				
				
				<div>
				<% for (FormInterval fi : formsInInterval) { %>	
							<% if (Integer.valueOf(formId) == fi.getFormId()) { %>
								<div class="statusRow activeDiv">
							<%}else{ %>
								<div class="statusRow">
							 <%} %>
						<% if(fi.getDataCollectionStatus().equals("Locked")){ %>	
								<% if(fi.getRequired().equals("Required")){%>					
									<a href="javascript:;" class="greenCircle floatLeft statusCir" title="Required: Locked">R</a>	
								<%}else{ %>		
									<a href="javascript:;" class="greenCircle floatLeft statusCir" title="Locked"></a>	
								<%} %>								
						<% } else if(fi.getDataCollectionStatus().equals("In Progress")){%>		
								<% if(fi.getRequired().equals("Required")){%>			
									<a href="javascript:;" class="redCircle floatLeft statusCir" title="Required: In Progress">R</a>	
								<%}else{ %>	
									<a href="javascript:;" class="redCircle floatLeft statusCir" title="In Progress"></a>	
								<%} %>	
								
									
						<% } else if(fi.getDataCollectionStatus().equals("Completed")) { %>	
								<% if(fi.getRequired().equals("Required")){%>
									<a href="javascript:;" class="yellowCircle floatLeft statusCir" title="Required: Completed">R</a>	
								<%}else{ %>	
									<a href="javascript:;" class="yellowCircle floatLeft statusCir" title="Completed"></a>
								<%} %>									
						<% } else{ %>
								<% if(fi.getRequired().equals("Required")){%>							
									<a href="javascript:;" class="whiteCircle floatLeft statusCir" title="Required: Not Started">R</a>	
								<%}else{ %>	
									<a href="javascript:;" class="whiteCircle floatLeft statusCir" title="Not Started"></a>	
								<%} %>	
														
						<%} %>	
						
						
						<% if(editMode && formStatus.equalsIgnoreCase("Final Lock")){ %>
							<% if (Integer.parseInt(formId) == fi.getFormId()) { %>
								<div class="blueBackGroud"><%=fi.getFormName()%></div>
							<%}else{ %>
								<div><%=fi.getFormName()%></div>
							<%} %>			
						<% }else{ %>
						<%if (fi.getDataCollectionStatus().equalsIgnoreCase("locked") || 
							(fi.getDataCollectionStatus().equalsIgnoreCase("In Progress") && !user.isSysAdmin() && fi.getUserId() != user.getId()) || 
							(fi.getDataCollectionStatus().equalsIgnoreCase("Completed") && !user.isSysAdmin() && fi.getUserId() != user.getId())) { %>
								<div><%=fi.getFormName()%></div>
							<% }else{ %>	
									<% if (Integer.parseInt(formId) == fi.getFormId()) { %>
										<div><%=fi.getFormName()%></div>
								<%}else{ %>
									<div><%=fi.getFormNameLink()%></div>
								<%} %>
							<%} %>	
						<% } %>		
						</div>
								
				<%} %>
			
				</div>
			
			
		
		</div> 
		<div id="divdataentryform"> 
			<s:property value="#request.formdetail" escapeHtml="false" />
		</div>
	 </div> 
	<br>
	

	<div id="formButtons" style="clear: left;">
	
	<%
		if (formStatus != null && formStatus.equals("Final Lock")) {
	%>
		<div class="floatRight">
			<% if (enableEsignature) { %>
				<div class="saveFinalLock">
					<input type="button" value="<s:text name='button.Finish' />" title="Save & Exit Final Lock"
						alt="Save" id="confirmSignAndSaveFLBtn" />
				</div>
			<% }  else { %>
				<div class="saveFinalLock">
					<input type="button" value="<s:text name='button.Finish' />" title="Save & Exit Final Lock"
						alt="Save" id="saveFLBtn" />
				</div>
			<% } %>
			<div class="floatLeft">
				<input type="button" value="<s:text name='button.Cancel' />"
					title="Click to cancel (changes will not be saved)." alt="Cancel" onclick="cancel()" />
			</div>
		</div>
	<%
				} else {
	%>
					<%
						if(!isCat || "shortForm".equals(mType)){
					%>
		<div  class="markAsCompletedLayout floatLeft">
					<%
						}else{
					%>
		<div  class="markAsCompletedLayout floatLeft" style="display: none;">
					<%
						}
					%>
			<div id="chechbox"  class="floatLeft">
				<input type="checkbox" name="markChecked" value="<s:text name="proforms.label.markascompletecheckbox" />" id="markAsCompletedCheckBox" title="<s:text name="proforms.label.markascompletecheckbox" />" />
				<label id="markAsCompletedCheckBoxLabel" for="markAsCompletedCheckBox">
					<s:text name="proforms.label.markascompletecheckbox" />
				</label>
			</div>
		</div>
					
		<div class="floatLeft">
			<div class="floatLeft marginRight10px">
				<input type="button" class="floatLeft" id="previousButton" value="<s:text name='button.dataCollection.Previous' />"
						title="<s:text name='tooltip.previousFormNavigation.dataCollection' />" />
			</div>
			<div class="floatLeft">
				<input type="button" class="floatLeft" id="nextButton" value="<s:text name='button.dataCollection.Next' />"
						title="<s:text name='tooltip.nextFormNavigation.dataCollection' />" />
			</div>
		</div>	
		<%
					if(!isCat || mType.equals("shortForm")){
		%>
		<div class="floatRight">	
			<div class="saveAndStay">
				<input id="saveAndStyBtn" type="button" value="<s:text name='button.Save' />"
						title="<s:text name='button.Save' />" alt="Save & Stay" />
			</div>
			<div class="saveAndExit">
				<input id="saveAndExitBtn" type="button" value="<s:text name='button.Finish' />"
						title="<s:text name='tooltip.saveAndFinish.dataCollection' />" alt="Save"  />
			</div>
			<div class="lockForm">
				<input type="button" id="lockButtonPatient" value="<s:text name='button.Lock' />"
						title="Click to lock the answer" alt="Save & Lock" />
			</div>
			<div class="floatLeft">
				<input type="button" value="<s:text name='button.Cancel' />"
						title="Click to cancel (changes will not be saved)." alt="Cancel" onclick="cancel()" />
			</div>		
		</div>
		
	<%
					}else{
	%>
		<div class="floatRight" >
			<div class="lockForm" style="display: none;">
				<input type="button" id="lockButtonPatient" value="<s:text name='button.Lock' />"
						title="Click to lock the answer" alt="Save & Lock" />
			</div>
			<div class="floatLeft">
				<input type="button" id='goCancel' value="<s:text name='button.Cancel' />"
						title="Click to cancel (changes will not be saved)." alt="Cancel" onclick="cancel()" />
			</div>		
		</div>
	<%	
					}
				} 
			
		}
	%>
	</div>
	<%-- <s:token id="token" /> --%>
	</s:form>

	<a href="#changeResponseReason" id="changeResponseReasonFancyBoxA" style="display: none">clicky!</a>
	<div class="fancyBoxContainer">
		<div id="changeResponseReason">
			<div id="errorContainer" style="display: none"></div>
			<div style="display: none">
				<input type="text" maxlength="50" size="35" id="elemId" /> 
				<input type="text" maxlength="50" size="35" id="elemType" /> 
				<input type="text" maxlength="50" size="35" id="elemValue" />
			</div>
			<div class="formrow_1" style="display: none">
				<label for="qId"><s:text name="app.label.lcase.questionid" /></label> 
				<input type="text" maxlength="50" size="35" id="qId" />
			</div>
			<div class="formrow_1">
				<label for="qText"><s:text name="app.label.lcase.questionText" /></label> 
				<input type="text" maxlength="50" size="35" id="qText" />
			</div>
			<div class="formrow_1">
				<label for="dEntry1"><s:text name="response.viewaudit.dataentry1" /></label> 
				<input type="text" maxlength="50" size="35" id="dEntry1" />
			</div>

			<%-- <div class="formrow_1">
				<label for="dEntry2"><s:text name="response.viewaudit.dataentry2" /></label> 
				<input type="text" maxlength="50" size="35" id="dEntry2" />
			</div> --%>

			<div class="formrow_1">
				<label for="fAnswer"><s:text name="response.reasonForChange.finalAnswer" /></label> 
				<input type="text" maxlength="50" size="35" id="fAnswer" />
			</div>

			<div class="formrow_1">
				<label for="rChange" class="requiredInput"><s:text name="response.reasonForChange.reasonForChange" /></label>
				<textarea rows="2" cols="35" id="rChange"></textarea>
			</div>
		</div>
		<div class="clearboth">
			<!-- IE fix -->
		</div>
	</div>
</body>

<script type="text/javascript">

function viewCompletedForm(){
	LeavePageWarning.save();
	var aformId = $(".finalLock").data('aformId');
	var url = "<s:property value="#webRoot"/>/response/viewForm.action?id=" + aformId;
	var WindowArgs = "toolbar=no," + "location=no," + "directories=no," + "status=yes,";
	openPopup(url, "", WindowArgs+"top=10,left=10,scrollbars=yes,resizable=yes,width=600,height=400");
}


function lockAndLoadPrevious(){
	if($("#finalLockChkBx").is(':checked') == true) { 
		EventBus.trigger("open:processing", "Locking your form...");
   		disableActionControls();		
        var imAnswers = document.body.getElementsByTagName('SELECT');
        if(imAnswers != null) {
	        for (i = 0; i < imAnswers.length; i++){
	            if (imAnswers[i].className =='ctdbImageMapAnswers'){
	                opts = imAnswers[i].options;
	                for (j=0; j < opts.length; j++){
	                    opts[j].selected = true;
	                }
	            }
	        }
        }
		LeavePageWarning.save();
	
	   	<% if (enableEsignature) { %>
			var container = ".finalLock";
		    var callingFrom = 'lockAndLoadPrevious';
		    passwordValidationAjax(callingFrom, container);
	    <% } else {%>
			var aformId = $(".finalLock").data('aformId');
			
			var form = document.getElementById("myForm");
		    form.action = "<s:property value="#webRoot"/>/response/dataCollection.action?action=lockAndLoadPreviousForm&markAsCompletedCheckBoxStatus="+markAsCompletedCheckBoxStatus+"&aformId=" + aformId + "&editMode=" + globalEditFlag;
			form.submit();
	   	<% } %>

	}else{
		$.ibisMessaging("close", {type:"primary"}); 
		$.ibisMessaging("primary", "error", '<s:text name="errors.response.confirmation.checkbox"/> </b>', {container: ".finalLock"});
	}
} 



function lockAndLoadNext(){
	if($("#finalLockChkBx").is(':checked') == true) {
		EventBus.trigger("open:processing", "Locking your form...");
		disableActionControls();
        var imAnswers = document.body.getElementsByTagName('SELECT');
        if(imAnswers != null) {
	        for (i = 0; i < imAnswers.length; i++){
	            if (imAnswers[i].className =='ctdbImageMapAnswers'){
	                opts = imAnswers[i].options;
	                for (j=0; j < opts.length; j++){
	                    opts[j].selected = true;
	                }
	            }
	        }
        }
		LeavePageWarning.save();
	
		<% if (enableEsignature) { %>
			var container = ".finalLock";
		    var callingFrom = 'lockAndLoadNext';
		    passwordValidationAjax(callingFrom, container);
	    <% } else {%>
			var aformId = $(".finalLock").data('aformId');
	
			var form = document.getElementById("myForm");
			form.action = "<s:property value="#webRoot"/>/response/dataCollection.action?action=lockAndLoadNextForm&markAsCompletedCheckBoxStatus="+markAsCompletedCheckBoxStatus+"&aformId=" + aformId + "&editMode=" + globalEditFlag;
			form.submit(); 
	    <% } %>
		
	}else{
		$.ibisMessaging("close", {type:"primary"}); 
		$.ibisMessaging("primary", "error", '<s:text name="errors.response.confirmation.checkbox"/></b>', {container: ".finalLock"});
	}
} 


function cancelSignature() {
	$(".signatureForComplete").dialog('close');
	$("#markAsCompletedCheckBox").prop('checked', false);
	$('#lockButtonPatient').attr('disabled', true);
	markAsCompletedCheckBoxStatus = false;
	
}



function confirmSignature() {
	if($("#completeLockChkBx").prop('checked')==false) {
		$.ibisMessaging("close", {type:"primary"});
		passwordErrorId = $.ibisMessaging("primary", "error", '<s:text name="errors.response.confirmation.checkbox"/> </b>', {container: ".signatureForComplete"}); 
		return;
	}
    
    var callingFrom = 'confirmSignature';
    var container = ".signatureForComplete";
    passwordValidationAjax(callingFrom, container);
	
}

function lockFormStep2(){
	if($("#finalLockChkBx").is(':checked') == true) { 
		EventBus.trigger("open:processing", "Locking your form...");
		LeavePageWarning.save();
		disableActionControls();
	    var aformId = $(".finalLock").data('aformId');
	    $.ibisMessaging("close", {type:"primary"}); 
		var form = document.getElementById("myForm");
		form.action = "<s:property value="#webRoot"/>/response/dataCollection.action?action=lockAndExit&markAsCompletedCheckBoxStatus="+markAsCompletedCheckBoxStatus+"&aformId=" + aformId + "&editMode=" + globalEditFlag;
		form.submit(); 

	        
	}else {
		$.ibisMessaging("close", {type:"primary"}); 
		$.ibisMessaging("primary", "error", '<s:text name="errors.response.confirmation.checkbox"/> </b>', {container: ".finalLock"});
	}
} 
	
function passwordValidationAjax(callingFrom, container){
	var aformId = $(".finalLock").data('aformId');
	var url = "<s:property value="#webRoot"/>/response/dataCollection.action?action=digitalSignature&aformId=" + aformId;
 	var passwordData ='';
 	if(callingFrom == "confirmSignature") { //.signatureForComplete dialog
	    if ($("#userPassword").length>0) {
	    	passwordData = $("#userPassword").val();
	    }
 	} else { //.finalLock dialog
	    if ($("#userPasswordLock").length>0) {
	    	passwordData = $("#userPasswordLock").val();
	    }
 	}

    $("#userPasswordId").val(passwordData);

    $.ajax({
			 type: "POST",
				url: url,
				data: {
					"dataEntryForm.userPassword"	: passwordData
				},
				error: function (xhr, ajaxOptions, thrownError) {
					EventBus.trigger("close:processing");

					if(passwordData.length<1){
						enableActionControls();
						$.ibisMessaging("close", {type:"primary"}); 
						passwordErrorId = $.ibisMessaging("primary", "error", '<s:text name="errors.response.password.required"/> </b>', {container: container}); 
	  		  			$('#lockButtonPatient').attr('disabled', true);
	  		  			markAsCompletedCheckBoxStatus = false;
					}else{
						enableActionControls();
  						$.ibisMessaging("close", {type:"primary"}); 
  						passwordErrorId = $.ibisMessaging("primary", "error", '<s:text name="errors.response.password.mismatch"/> </b>', {container: container}); 
  		  				$('#lockButtonPatient').attr('disabled', true);
  		  				markAsCompletedCheckBoxStatus = false;
  					}
				},
				success: function(response, status, jqxhr) { 
					EventBus.trigger("close:processing");
					if(response == "bricsAccountWSnotReacheable"){
						enableActionControls();
						$.ibisMessaging("close", {type:"primary"});
						passwordErrorId = $.ibisMessaging("primary", "error", '<s:text name="errors.brics.webservice"/> </b>', {container: container}); 
						$('#lockButtonPatient').attr('disabled', true);
						markAsCompletedCheckBoxStatus = false;
					}else if(response == "blankPassword"){
						enableActionControls();
						$.ibisMessaging("close", {type:"primary"}); 
						passwordErrorId = $.ibisMessaging("primary", "error", '<s:text name="errors.response.password.required"/> </b>', {container: container}); 
	  		  			$('#lockButtonPatient').attr('disabled', true);
	  		  			markAsCompletedCheckBoxStatus = false;
					}else if(response == "mismatchPassword"){
						enableActionControls();
						$.ibisMessaging("close", {type:"primary"}); 
						passwordErrorId = $.ibisMessaging("primary", "error", '<s:text name="errors.response.password.mismatch"/> </b>', {container: container}); 
		  				$('#lockButtonPatient').attr('disabled', true);
		  				markAsCompletedCheckBoxStatus = false;
					}else if(response == "passwordValidationPassed"){
						
						if(callingFrom == "confirmSignature") {
							//to do
							enableActionControls();
							$(".signatureForComplete").dialog('close');
							$("#markAsCompletedCheckBox").prop('checked', true);
							$('#markAsCompletedCheckBox').attr('disabled', true);
							$('#lockButtonPatient').attr('disabled', false);
							markAsCompletedCheckBoxStatus = true;
						} else if(callingFrom == "confirmSignAndLockFormStep2") {

						    $.ibisMessaging("close", {type:"primary"}); 
							var form = document.getElementById("myForm");
							form.action = "<s:property value="#webRoot"/>/response/dataCollection.action?action=lockAndExit&markAsCompletedCheckBoxStatus="+markAsCompletedCheckBoxStatus+"&aformId=" + aformId + "&editMode=" + globalEditFlag;
							form.submit();
							
							//to do
							enableActionControls();
							$(container).dialog('close');
						} else if (callingFrom == "lockAndLoadPrevious") {
							
							var form = document.getElementById("myForm");
						    form.action = "<s:property value="#webRoot"/>/response/dataCollection.action?action=lockAndLoadPreviousForm&markAsCompletedCheckBoxStatus="+markAsCompletedCheckBoxStatus+"&aformId=" + aformId + "&editMode=" + globalEditFlag;
							form.submit();
						
						} else if (callingFrom == "lockAndLoadNext") {

							var form = document.getElementById("myForm");
							form.action = "<s:property value="#webRoot"/>/response/dataCollection.action?action=lockAndLoadNextForm&markAsCompletedCheckBoxStatus="+markAsCompletedCheckBoxStatus+"&aformId=" + aformId + "&editMode=" + globalEditFlag;
							form.submit(); 
						} else if (callingFrom == "confirmSignAndSaveFL") {
							$.ibisMessaging("close", {type:"primary"}); 
							saveFLAjax();
							enableActionControls();
							$(container).dialog('close');
						}

					 }else{
						enableActionControls();
						$.ibisMessaging("close", {type:"primary"}); 
						passwordErrorId = $.ibisMessaging("primary", "error", "Something went wrong in password validation process.Please try again.</b>", {container: container}); 
	  		  			$('#lockButtonPatient').attr('disabled', true);
	  		  			markAsCompletedCheckBoxStatus = false;
					 }
		 	} //end success				
		 });//end ajax
}

function confirmSignAndLockFormStep2() {
	if($("#finalLockChkBx").is(':checked') == true) { 
		EventBus.trigger("open:processing", "Locking your form...");
		LeavePageWarning.save();
		disableActionControls();
		
		var container = ".finalLock";
	    var callingFrom = 'confirmSignAndLockFormStep2';
	    passwordValidationAjax(callingFrom, container);
	        
	}else {
		$.ibisMessaging("close", {type:"primary"}); 
		$.ibisMessaging("primary", "error", '<s:text name="errors.response.confirmation.checkbox"/> </b>', {container: ".finalLock"});
	}
}

function cancelInFinalLockConfirmationDialog(){
	enableActionControls();
	$(".finalLock").dialog('close');
}

function confirmSignAndSaveFL() {
	if($("#finalLockChkBx").is(':checked') == true) {
		LeavePageWarning.save();
		var container = ".finalLock";
	    var callingFrom = 'confirmSignAndSaveFL';
	    passwordValidationAjax(callingFrom, container);
	} else {
		$.ibisMessaging("close", {type:"primary"}); 
		$.ibisMessaging("primary", "error", '<s:text name="errors.response.confirmation.checkbox"/></b>', {container: ".finalLock"});
	}
}





</script>



<div class="signatureForComplete">
	<div class="lockInstruction">
		<input type="checkbox" id="completeLockChkBx" />&nbsp;&nbsp;
		<s:text name="response.lock.statement" />
	</div>
	<br/>
	<br/>
	
	<div>
		<span style="float:left; "><b>Name:</b> <s:property value='dataEntryForm.userFullName'/></span><br/><br/>
	</div>		

	<div class="floatLeft">
		<s:text name="response.complete.password" />
	</div>

		<br/>
		<br/>
		<label style="float: left;font-weight: bold;" for="userPassword">Password:<span class="requiredStar">*</span></label>
		<input type="password" style="float: left;margin-left: 20px" id="userPassword" name="userPassword" />

		<br/>
		<br/>
		<br/>
		<div class="floatLeft marginRight10px">
			<input type="button" id="signatureOK" value="OK" onclick="confirmSignature()" />
		</div>
		<div class="floatLeft">
			<input type="button" id="signatureCancel" value="Cancel" onclick="cancelSignature()" />
		</div>
</div>

<div class="finalLock">
	<% if (formStatus != null && (formStatus.equals(CtdbConstants.DATACOLLECTION_STATUS_INPROGRESS)
			|| formStatus.equals(CtdbConstants.DATACOLLECTION_STATUS_STARTED)
			|| formStatus.equals(CtdbConstants.DATACOLLECTION_STATUS_COMPLETED) 
			|| formStatus.equals(CtdbConstants.DATACOLLECTION_STATUS_LOCKED)
			|| formStatus.equals(CtdbConstants.DATACOLLECTION_STATUS_FINALLOCKED))) {%>
	<div>
		<jsp:include page="dataEntryHeader.jsp" />
	</div>

	<br>
	<div class="lockInstruction">
		<input type="checkbox" id="finalLockChkBx" />&nbsp;&nbsp;
		<s:text name="response.lock.statement" />
	</div>
	<br/>

	<% if (enableEsignature) { %>
		<div id="signatureRequired">
			<span style="float:left; "><b>Name:</b> <s:property value='dataEntryForm.userFullName'/></span><br/><br/>
			<span style="float:left;"><s:text name="response.complete.password" /></span><br/><br/>

			<label style="float: left;font-weight: bold;" for="userPasswordLock">Password:<span class="requiredStar">*</span></label>
			<input type="password" style="float: left;margin-left: 20px" id="userPasswordLock" name="userPasswordLock" />
		</div>
		<br/><br/><br/>
	<% } %>

	<div class="floatLeft marginRight10px">
		<input type="button" id="viewCompletedForm" value="View Completed Form" onclick="viewCompletedForm()" />
	</div>
	<div class="floatLeft marginRight10px">
		<input type="button" id="lockAndLoadPrevious" 
			value="<s:text name='button.lockAndLoadPreviousForm' />" onclick="lockAndLoadPrevious()" />
	</div>
	<div class="floatLeft marginRight10px">
		<input type="button" id="lockAndLoadNext" value="<s:text name='button.lockAndLoadNextForm' />" onclick="lockAndLoadNext()" />
	</div>

	<% if (enableEsignature) { %>
		<% if (formStatus != null && (formStatus.equals(CtdbConstants.DATACOLLECTION_STATUS_LOCKED)
									|| formStatus.equals(CtdbConstants.DATACOLLECTION_STATUS_FINALLOCKED))) {%>
			<div class="floatLeft marginRight10px">
				<input type="button" id="lockAndExit" value="Apply Changes & Exit" onclick="confirmSignAndSaveFL()" />
			</div>
		<% } else { %>
			<div class="floatLeft marginRight10px">
				<input type="button" id="lockAndExit" value="Lock & Exit" onclick="confirmSignAndLockFormStep2()" />
			</div>
		<% } %>
	<% } else { %>
		<div class="floatLeft marginRight10px">
			<input type="button" id="lockAndExit" value="Lock & Exit" onclick="lockFormStep2()" />
		</div>
	<% } %>

	<div class="floatLeft">
		<input type="button" id="cancelFromLockConfirm" value="Cancel" onclick="cancelInFinalLockConfirmationDialog()" />
	</div>
	
	<br/><br/>
	<% } %>
</div>
<jsp:include page="/common/footer_struts2.jsp" />
