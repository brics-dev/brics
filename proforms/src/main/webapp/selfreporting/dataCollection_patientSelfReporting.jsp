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


<jsp:include page="header_selfReporting.jsp" />

<s:set var="usingGUID" value="#systemPreferences.get('guid_with_non_pii')" />

<link rel="stylesheet" type="text/css" href="<s:property value="#systemPreferences.get('app.stylesheet')"/>">
<link rel="stylesheet" type="text/css" href="<s:property value="#webRoot"/>/common/sliderSwing.css">
<link rel="stylesheet" type="text/css" href="<s:property value="#webRoot"/>/common/css/dataCollection.css">
<link rel="stylesheet" type="text/css" href="<s:property value="#webRoot"/>/common/css/dataCollectionSelfReporting.css">

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
<!-- added by Ching-Heng -->
<script src="<s:property value="#webRoot"/>/common/js/crypto.js" type="text/javascript" ></script>
<script type="text/javascript" src="<s:property value='#webRoot'/>/common/conversionTable.js"></script>

<jsp:include page='/formbuilder/templates/builder/processing.jsp' />


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
	
	boolean deleteOnCancelFlag = (Boolean) request.getAttribute(StrutsConstants.DELETEONCANCELFLAG);
	boolean isCat = (Boolean) request.getAttribute(CtdbConstants.IS_CAT);
	String mType = (String) request.getAttribute(CtdbConstants.MEASUREMENT_TYPE);
	
	String action = (String) request.getAttribute(StrutsConstants.ACTION);

	
	
	String token = (String) request.getAttribute(StrutsConstants.TOKEN);

%>

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
	 var appName = "<s:property value="#systemPreferences.get('template.global.appName')" />";
	 if(appName == 'cdrns'){
		 $('.formName').hide();
	 }
	 FormBuilder.page.processingView = new ProcessingView();
	 $("#selfReport_actionButtons").sticky({topSpacing: 0});

	 $(".psrHeader").after("<div id=\"pageText\"></div>");
	 $("#pageText").html($("#thisPageText").text()).css("margin-left","5%").css("margin-right","5%").css("margin-bottom","20px").css("font-size","13px");

	 $("#promis_saveLock").hide();
	 
	 
	 //need to make all questions under Form Administration and Main to be readonly for CAT eforms
	 
	 var isCat = <%= isCat %>;
	 if(isCat){
		 var $main = $("div[name='Main']");
		 var $fa = $("div[name='Form Administration']");
		 $main.find("input,textarea,select").prop("readonly", true);
		 $fa.find("input,textarea,select").prop("readonly", true);
		 $fa.find('input:radio').prop('checked', false).prop("disabled",true);
		 $main.find('input:radio').prop('checked', false).prop("disabled",true);
		 $main.find('input:radio').click(function(){
			    return false;
		 });
		 $main.find('input:checkbox').click(function(){
			    return false;
		 });
		 $fa.find('input:radio').click(function(){
			    return false;
		 });
		 $fa.find('input:checkbox').click(function(){
			    return false;
		 });
		 $main.find('.hasDatepicker').css('pointer-events', 'none');
		 $fa.find('.hasDatepicker').css('pointer-events', 'none');
		 $(".goPROMIS").prop("readonly", false);
	 }
	 
	 /*CISTAR-641:set table cell width to be 0 if no text in it*/
	 $(".questionTextContainerTd").each(function(element) { 
		 var $this = $(this); 
		 var tblCellTxt = $this.find(".questionTextImmediateContainer").text();
		 if (tblCellTxt.length == 0) { 
			 $this.width(0); 
		 } else if ($.trim(tblCellTxt).length == 0) { 
			 //contains white spaces only, then reset width in percentage
			 var widthPercent = tblCellTxt.length / $this.parent().width() * 100 + "%";
			 $this.width(widthPercent);  
		 }   
	 });
	 
 });
</script>

<script type="text" id="thisPageText">
	<s:text name="selfreporting.selfreportingHome.instruction2" />
</script>



<script type="text/config" id="formNameToBeSavedOrLocked"><s:property value="#request.formNameToBeSavedOrLocked" /></script>
<script type="text/javascript">
LeftNav.collapsed = true;

<%-- Global flags for saved message and mark as completed checkbox --%>	
var showSavedMessage;
var fileExist;
var assocFileQuestionIds;

var comingFromValidationErrorGoto = false;
var isDocumentLoaded= false;
var globalEditFlag = <%=editMode%>;
var globalDeleteOnCancel = <%=deleteOnCancelFlag%>;
var $disabledInputs;
var globalFormStatus = '<%=formStatus%>';
var passwordErrorId;




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
	$('#mainContent input[type="button"], #mainContent a, .ui-dialog input[type="button"]').each(function() {
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

function checkButtons() {	
}


$(document).ready(function() {	

	fileExist = getAttchmentIds();
	var dataCollectionErrorMsg = "<s:text name="response.errors.duplicate.data"/><br/>";



  /*  Save and Exit button will call this function */
	$('#saveAndExitBtn').click(function(e) { 
		$("input[dename='PROMISRawScore']").focus();
		EventBus.trigger("open:processing", "Saving your form...");
		setHiddenFileInputs();

		commonSaveExitLockTask();
		LeavePageWarning.save();
		var url = "<s:property value="#webRoot"/>/selfreporting/list?token=<%= token %>";
    	$('#myForm').ajaxSubmit({
    	data: {clickedSectionFields:$("#clickedSectionFieldsId").val(), formTypeId:$("#formTypeId").val(), modeId:$("#modeId").val()},
		url: "<s:property value="#webRoot"/>/selfreporting/dataCollection?action=saveFormPSR&complete=false&aformId=" +<%=aformId%>+"&fileExist="+fileExist+"&assocFileQuestionIds="+assocFileQuestionIds,	
		success: function(response, status, jqxhr) {
				EventBus.trigger("close:processing");

				var data = JSON.parse(response);
		  		
		  		var st = data.status;
		  		
				
				if (st == "<%=StrutsConstants.ERROR_ANSWER_REQUIRED_IN_SAVE%>") {
		        	$.ibisMessaging("close", {type:"primary"}); 
					$.ibisMessaging("primary", "error", '<s:text name="errors.text"/></b><%=rs.getValue(StrutsConstants.ERROR_ANSWER_REQUIRED_IN_SAVE,request.getLocale())%>', {hideDelay: 3000});

		  		} else if (st.toLowerCase().indexOf("<li")==0) {
		        	$.ibisMessaging("close", {type:"primary"}); 
					$.ibisMessaging("primary", "error", '<s:text name="errors.text"/></b>'+st, {hideDelay: 3000});

		  		}else if (st == "saveFormSuccess") {
		  			var formName = data.formName;	
		  			$.ibisMessaging("store", "primary", {message: 
							"The administered form "+ formName +" has been saved successfully.", level: "success"});
						
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
	

  	/*
  	* function that determines if there are any unamswered optional questions
  	*/
	function areThereAnyUnansweredOptionalQuestions() {
		var areThereAnyUnansweredOptional = false;

		$(".questionInputTD").each(function() {
			var $parent = $(this).parents(".eformConfigureHidden");
			if($parent.length > 0) {
				return;
			}
			
			
		  var $this = $(this);
		  var $input = $this.find("input, textarea, select").eq(0);
		  var labelText = $this.prev().text().trim();
		  var value = $input.val();
		  var required = false;
		  var emptyValue = false;
		  
		  // get value
		  if ($input.is('[type="radio"]')) {
		    var inputGroupName = $input.attr("name");
		    value = $("input:radio[name='" + inputGroupName + "']:checked").val();
		  }
		  if ($input.is('[type="checkbox"]')) {
		    var inputGroupName = $input.attr("name");
		    value = $("input:radio[name='" + inputGroupName + "']:checked").val();
		  }
		  
		  if (labelText.indexOf("*") > -1) {
		    // required
		    required = true;
		  }

		  if (value == null || typeof value === "undefined" || value == "") {
		    emptyValue = true;
		  }
		  
		  if(required == false && emptyValue == true) {
			  areThereAnyUnansweredOptional = true;
			  return;
		  }

		});
		
		return areThereAnyUnansweredOptional;
	}
  
		
	var unamsweredOptionalDialogId = -1;

	  /*  Save and Exit button will call this function */
	$('#completeAndExitBtn').click(function(e) { 				
		$("input[dename='PROMISRawScore']").focus();
		var areThereAnyUnansweredOptional = areThereAnyUnansweredOptionalQuestions();
		if(areThereAnyUnansweredOptional) {
			
			unamsweredOptionalDialogId = $.ibisMessaging(
					"dialog", 
					"warning", 
					"There are some optional questions (which may be in unopened sections) that are unanswered. Do you wish to complete as is?", 
					{
						buttons: [{
							text: "Complete", 
							click: function(){completeForm();}
						},
							{text: "Return to form",
							click: function(){
								$.ibisMessaging("close", {id: unamsweredOptionalDialogId});							
							}}],
						modal: true,
						draggable:false
					}
			);
			
		}else {
			completeForm();
		}

    	//return false; 
	}); 
	  
	  
	 /*
	 * function that calls complete on the form
	 */
	function completeForm() {
		if(unamsweredOptionalDialogId != -1) {
			$.ibisMessaging("close", {id: unamsweredOptionalDialogId});
			unamsweredOptionalDialogId = -1;
		}
		
		EventBus.trigger("open:processing", "Completing your form...");
		setHiddenFileInputs();

		commonSaveExitLockTask();
		LeavePageWarning.save();
		var url = "<s:property value="#webRoot"/>/selfreporting/list?token=<%= token %>";
    	$('#myForm').ajaxSubmit({
    	data: {clickedSectionFields:$("#clickedSectionFieldsId").val(), formTypeId:$("#formTypeId").val(), modeId:$("#modeId").val()},
		url: "<s:property value="#webRoot"/>/selfreporting/dataCollection?action=saveFormPSR&complete=true&aformId=" +<%=aformId%>+"&fileExist="+fileExist+"&assocFileQuestionIds="+assocFileQuestionIds,	
		success: function(response, status, jqxhr) {
				EventBus.trigger("close:processing");

				var data = JSON.parse(response);
		  		
		  		var st = data.status;
				
				
				if (st == "<%=StrutsConstants.ERROR_ANSWER_REQUIRED_IN_SAVE%>") {
		        	$.ibisMessaging("close", {type:"primary"}); 
					$.ibisMessaging("primary", "error", '<s:text name="errors.text"/></b><%=rs.getValue(StrutsConstants.ERROR_ANSWER_REQUIRED_IN_SAVE,request.getLocale())%>', {hideDelay: 3000});

		  		} else if (st.toLowerCase().indexOf("<li")==0) {
		        	$.ibisMessaging("close", {type:"primary"}); 
					$.ibisMessaging("primary", "error", '<s:text name="errors.text"/></b>'+st, {hideDelay: 3000});

		  		}else if (st == "saveFormSuccess") {
		  			var formName = data.formName;	
		  			$.ibisMessaging("store", "primary", {message: 
							"The administered form "+ formName +" has been saved successfully.", level: "success"});
						
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
	
  
	$('#exitBtn').click(function(e) { 
		
		var selfReportingLandingPageURL = "<s:property value='#webRoot'/>/selfreporting/list?token=<%= token %>";

		if (globalDeleteOnCancel) {
			//first delete collection
			var deleteCollectionURL = "<s:property value='#webRoot'/>"  + "/selfreporting/deleteDataEntryPSR?cancelFlag=1&aformId=<%=aformId%>&token=<%= token %>";

			$.ajax({
				type:"post",
				url: deleteCollectionURL,
				success: function(response){

					redirectWithReferrer(selfReportingLandingPageURL);
	
				},
				error: function(e){
					alert("error" + e );
				}
			});
	
		} else {
			url = "<s:property value='#webRoot'/>/selfreporting/list?token=<%= token %>";
			LeavePageWarning.save();
			redirectWithReferrer(selfReportingLandingPageURL);
		}
	}); 
	  

	var mode = "<%=mode%>";	
	LogoutWarning.init();
	LeavePageWarning.init();

	
	showSavedMessage = <s:property value='dataEntryForm.showSavedMessage' />;
	var formNameToBeSavedOrLocked = $("#formNameToBeSavedOrLocked").text();
	
	
	if (showSavedMessage) {
		if (formNameToBeSavedOrLocked != null) {
			$.ibisMessaging("flash", "success", formNameToBeSavedOrLocked , {hideDelay: 3000});
		}
	}
	
	<%--This is the second document ready that was merged in the first one so that it maintains the order of handler that are registered --%>	
	initialize('<%=mode%>', '<%=formId%>', <%=formFetched%>, <%=editMode%>);

	
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
			var isChrome = window.chrome;
			if (isChrome) {
				
				 $(".fileInput").focus( 
					function() {
						  $(this).attr("disabled", true);
						  alert("File upload is not supported in Chrome in tablets.Please use firefox if you want this feature.");
					});
			}
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
	
	// added by Ching-Heng
	$('#lockButtonPatient').click(function(e) { 
		EventBus.trigger("open:processing", "Saving your form...");
		setHiddenFileInputs();
		commonSaveExitLockTask(); 
		LeavePageWarning.save();
		var url = "<s:property value="#webRoot"/>/selfreporting/list?token=<%= token %>";
	 	$('#myForm').ajaxSubmit({
		data: {clickedSectionFields:$("#clickedSectionFieldsId").val(), formTypeId:$("#formTypeId").val(), modeId:$("#modeId").val()},
		url:"<s:property value="#webRoot"/>/selfreporting/dataCollection?action=lockAndExitCat&markAsCompletedCheckBoxStatus=" +
				true+"&aformId=" +<%=aformId%>+ "&editMode=" + globalEditFlag + 
				"&fileExist="+fileExist+"&assocFileQuestionIds="+assocFileQuestionIds,
		  	success: function(response, status, jqxhr) {
		  		EventBus.trigger("close:processing");
				var data = JSON.parse(response);
		  		var st = data.status;
		  		if(st == 'duplicateData'){
		  			$.ibisMessaging("close", {type:"primary"}); 
		  			$.ibisMessaging("store", "primary", {message: dataCollectionErrorMsg, level: "error"});
		  			redirectWithReferrer(url);	
		  		} else if(st == 'ctdbException'){
		  			$.ibisMessaging("close", {type:"primary"}); 
		  			$.ibisMessaging("store", "primary", {message: dataCollectionErrorMsg, level: "error"});
		  			redirectWithReferrer(url);	
		  		} else if(st == 'runtimeException'){
		  			$.ibisMessaging("close", {type:"primary"}); 
		  			$.ibisMessaging("store", "primary", {message: dataCollectionErrorMsg, level: "error"});
		  			redirectWithReferrer(url);	
		  		} else if(st == 'exception'){
		  			$.ibisMessaging("close", {type:"primary"}); 
		  			$.ibisMessaging("store", "primary", {message: dataCollectionErrorMsg, level: "error"});
		  			redirectWithReferrer(url);	
		  		} else if(st == "lockFormSuccess"){
		  			var formName = data.formName;	
		  			$.ibisMessaging("store", "primary", {message: 
							"The administered form "+ formName +" has been saved successfully.", level: "success"});					
					redirectWithReferrer(url);			  	
		  		}else{
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
	
	//added by Ching-Heng for promis	
	$('.goPROMIS').click(function(e){
		$('#myForm').ajaxSubmit({
			url: "<s:property value="#webRoot"/>/selfreporting/dataCollection?action=validateMainGroup&isSelfReporting=true&aformId="+<%=aformId%>,
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
	
	$("#promis_saveComplete").click(function(){
		$("input:checkbox[name='markChecked']").prop( "checked", true );
		completeForm();
	});
	
	$("#promis_cancel").click(function(){
		$("#exitBtn").click();
	});
	// ====================
		

	//PSR forms can be configured to hide certain sections (along with its questions) and/or certain questions within sections
	//so lets hide them here
	
	var idlist = JSON.parse($("#hiddenSectionsQuestionsPVsElementIdsJSON").val());
	for (var i = 0; i < idlist.length; i++) {
		var elementId = idlist[i];
		var $hideElement = $("[hideid='" + elementId + "']");
		$hideElement.hide();
		$hideElement.addClass("eformConfigureHidden");
		
		//handle repeatables
		if (elementId.indexOf("questionContainer") === -1) {
			//this is a section
			//no need to hide the child sections since we are hiding the repeat button:
			//hide repeat button
			var $repeatButton;
			if(globalEditFlag == true) {
				$repeatButton = $hideElement.parents("tr").first().next("tr").find(".repeatButton");
			}else {
				$repeatButton = $hideElement.next(".repeatButton");
			}
			$repeatButton.removeClass("repeatButton");
			$repeatButton.addClass("eformConfigureHidden");
			$repeatButton.hide();
			
		} else {
			//this is a question or pv
			var isQuestion = true;
			var sectionId;
			var questionId;
			var pvId;
			
			if (elementId.split("_").length - 1 == 3) { //handles pvs
				isQuestion = false;
				var s_q_p = elementId.substring(elementId.indexOf("questionContainer")+18, elementId.length);
				sectionId = s_q_p.substring(0,s_q_p.indexOf("_"));
				var q_p = s_q_p.substring(s_q_p.indexOf("_") + 1 ,s_q_p.length);
				questionId = q_p.substring(0,q_p.indexOf("_"));
				pvId = q_p.substring(q_p.indexOf("_") + 1, q_p.length);
			} else { //handles questions
				sectionId = elementId.substring(elementId.indexOf("questionContainer")+18, elementId.lastIndexOf("_"));
				questionId = elementId.substring(elementId.lastIndexOf("_")+1, elementId.length);
			}
			
			//hide children corresponding questions and add eformConfigureHidden class 
			var $childRepeatables = $('[parent="' + sectionId + '"]');
			$childRepeatables.each(function(index) {
				$this = $(this);
				var childId = $this.attr("id");
				var childElementId;
				
				if (isQuestion) {
					childElementId = "questionContainer_" + childId + "_" + questionId;
				} else {
					childElementId = "questionContainer_" + childId + "_" + questionId + "_" + pvId;
				}
				
				var $childElement = $this.find(("[hideid='" + childElementId + "']"));
				$childElement.hide();
				$childElement.addClass("eformConfigureHidden");
			});
		}
	}
														
});
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
		this.timer = setTimeout(function(){LogoutWarning.openPopup();}, <s:property value="#systemPreferences.get('app.warningTimeout')"/> * 60 * 1000);
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
		top.location.href = '<s:property value="#webRoot"/>/selfreporting/list?token=sessionExpired';
	},
	
	startLogoutTimer : function() {
		this.logoutAfterTimer = setTimeout(function(){LogoutWarning.logout();}, 120000);
	},
	
	cancelLogoutTimer : function() {
		window.clearTimeout(this.logoutAfterTimer);
		this.logoutAfterTimer = null;
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

			$(window).bind('beforeunload', function(event) {
				return LeavePageWarning.leavePage();
			});
			
			$('input, select').change(function() {
				LeavePageWarning.formUpdated = true;
			});
			$('textarea, input[type="text"]').on("keydown", function() {
				LeavePageWarning.formUpdated = true;
			});
		
	},
	
	leavePage : function() {
		if (!this.isFormSaved() && !this.ignoreWarning) {
			return this.displayAlert();
		}
		
		this.ignoreWarning = false;
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
	

	
	if (formFetched) {
		$("#dcTitle").click();
	}
}


function goToValidationErrorFlag() {
	comingFromValidationErrorGoto = true;
}






function URLDecode(psEncodeString) {
	  // Create a regular expression to search all +s in the string
	  var lsRegExp = /\+/g;
	  return decodeURIComponent(String(psEncodeString).replace(lsRegExp, " "));
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





var currQId;




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






function popupAttWindow(url) {	
	var WindowArgs = "toolbar=no," + "location=no," + "directories=no," + "status=yes,";
	openPopup(url, "", WindowArgs+"top=10,left=10,scrollbars=yes,resizable=yes,width=800,height=600");
}


/* added by Ching Heng for download the question file */
function downloadQuestionFile(fileID,formID){
	url = "<s:property value="#webRoot"/>/selfreporting/download.action?id="+fileID+"&associatedId="+formID+"&typeId=<%=AttachmentManager.FILE_COLLECTION%>";
	redirectWithReferrer(url);
}

/* added by Ching Heng, make Reset button can reset the Visual Scale and Image Map */
function resetVSandIM(){
	$(".ctdbImageMapAnswers").html('');
	resetSlider();
}
</script>

<body>
	
	
	<div id="dataCollectionEntry">


		<s:form id="myForm" method="post" enctype="multipart/form-data">
			<s:hidden name="mode" id="modeId" />
			<s:hidden name="dataEntryForm.formId" />
			<s:hidden name="dataEntryForm.userPassword" id="userPasswordId" />
			<s:hidden name="dataEntryForm.clickedSectionFields" id="clickedSectionFieldsId" />
			<s:hidden name="dataEntryForm.formType" id="formTypeId" />
			<s:hidden name="hiddenSectionsQuestionsPVsElementIdsJSON" id="hiddenSectionsQuestionsPVsElementIdsJSON" />
			<s:hidden name="aformCacheKey" />
			<%-- <s:param name="struts.token.name" value="'token'"/> --%>
			

			
	</div>

	

		
	
	<div style="display: table; width: 100%; position: relative;"> 
		<div id="divdataentryform"> 
			<s:property value="#request.formdetail" escapeHtml="false" />
		</div>
		<div id="selfReport_actionButtons" style="right: 4%;">
			<% if(!isCat || "shortForm".equals(mType)){ %>
				<div>
					<input id="saveAndExitBtn" type="button" class="boldText" value="<s:text name='button.Save' />"  />
				</div>
				<div>
					<input id="completeAndExitBtn" type="button" class="boldText" value="<s:text name='button.Complete' />"  />
				</div>
			<% } else { %>	
				<div style="display: none;">
					<input id="lockButtonPatient" type="button" value="<s:text name='button.Lock' />"  />
				</div>
			<% } %>	
				<div>
					<input id="exitBtn" type="button" value="<s:text name='button.Exit' />"  />
				</div>
		</div>
	 </div> 
	<br>
	


	<%-- <s:token id="token" /> --%>
	</s:form>
</body>







<jsp:include page="/selfreporting/footer_selfReporting.jsp" />
