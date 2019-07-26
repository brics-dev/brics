<%@ page autoFlush="true" buffer="1094kb"%>
<jsp:include page="/common/doctype.jsp" />
<%@ page import="gov.nih.nichd.ctdb.common.StrutsConstants"%>
<%@ page import="gov.nih.nichd.ctdb.common.CtdbConstants" %>
<%@ page import="gov.nih.nichd.ctdb.common.rs,java.util.Locale" %>
<%@ page import="gov.nih.nichd.ctdb.protocol.domain.Protocol"%>
<%@ page import="gov.nih.nichd.ctdb.protocol.domain.IntervalScheduleDisplay"%>
<%@ page import="java.lang.Integer"%>
<%@ page import="java.util.Iterator"%>
<%@ page import="java.util.List"%>
<%@ page import="java.text.DateFormat"%>
<%@ page import="java.text.SimpleDateFormat"%>
<%@ taglib uri="/struts-tags" prefix="s"%>
<%@ taglib uri="/WEB-INF/display.tld" prefix="display" %>
<%@ taglib uri="/WEB-INF/security.tld" prefix="security" %>
<%@ taglib uri="/WEB-INF/datatables.tld" prefix="idt" %>

<security:check privileges="addeditschedulevisits" />
<style>
	#divPatientVisit > table {
		width: 88%;
	}
	div.formrow_1 {
		width: 700px !important;
	}
	div.formrow_1 > select {
		width: 444px !important;
	}
	div.formrow_1 > input {
		width: 444px !important;
	}
	div.formrow_1 > textarea {
		width: 444px !important;
	}
</style>

<%-- <jsp:useBean id="patientVisitForm" class="gov.nih.nichd.ctdb.patient.form.PatientVisitForm" scope="request" /> --%>

<% 
	DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm");
	Locale l = request.getLocale();
	Protocol curProtocol = (Protocol) session.getAttribute(CtdbConstants.CURRENT_PROTOCOL_SESSION_KEY);
	String CURPROTOCOL_NUMBER = curProtocol.getProtocolNumber();
	int SUBJECT_DISPLAY_TYPE = curProtocol.getPatientDisplayType();
	String  SUBJECT_DISPLAY_LABEL= curProtocol.getPatientDisplayLabel();
%>
<html>

<s:set var="pageTitle" scope="request">
	<s:text name="patient.scheduleVisit.title" />
</s:set>
<jsp:include page="/common/header_struts2.jsp" />
<link rel="stylesheet" type="text/css" href="<s:property value="#webRoot"/>/common/css/dataCollection.css">

<!-- Page Specific CSS -->
<style>
	.formrow_2 {
		height: 0px;
	}
 	#patientVisitTable_wrapper {
 		overflow: visible;
 	}	
</style>

<s:set var="disallowingPII" value="#systemPreferences.get('guid_with_non_pii')" />
<s:set var="displayClinicalPoint" value="#systemPreferences.get('display.protocol.clinicalPoint')" />
<!-- Include the Template Manager Object from the Form Builder -->
<script src="<s:property value="#webRoot"/>/formbuilder/js/core/util/TemplateManager.js" type="text/javascript"></script>

<!-- Including Handlebar templates -->
<jsp:include page="/patient/templates/dataElementPrePopFields.jsp" />
<jsp:include page="/patient/templates/prePopValWarnMsg.jsp" />

<script type="text/javascript">
var basePath = "<s:property value="#webRoot"/>";
var prePropHashTable = null;

function initTemplates() {
	// Add in the templates.
	TemplateManager.addTemplate("prePopFields", Handlebars.compile($("#dePrePopFields").html()));
	TemplateManager.addTemplate("prePopWarnMsg", Handlebars.compile($("#prePopWarnContent").html()));
}

function renderPrePopFields(prePopArray) {
	var template = TemplateManager.getTemplate("prePopFields");
	var $contentDiv = $("#prePopContent");
	var fieldHtml = template({"prePopArray": prePopArray});
	
	// Insert generated pre-population fields HTML onto the page.
	$contentDiv.find("#prePopFormFields").html(fieldHtml);
	
	// If a date field as inserted, re-initialize all date pickers.
	if ( $contentDiv.find(".dateTimeField").length > 0 ) {
		DatePickerController.initAllPickers();
	}
	
	// Show the pre-population fields.
	$contentDiv.show();
}

function clearPrePopFieldsAndData() {
	var $contentDiv = $("#prePopContent");
	
	$contentDiv.hide();
	$contentDiv.find("#prePopFormFields").empty();
	prePropHashTable.clear();
	$("#pvPrepopValueStrList").val("[]");
}

function getPrePopFormFields() {
	var url = basePath + "/patient/getPrepopDeArray.action";
	var param = {
		"visitTypeId": $("#visitTypeSelect").val()
	};
	
	// Get the pre-population fields from the server for the selected visit type.
	$.getJSON(url, param, function(data) {
		var prePopDeArray = JSON.parse(data);
		
		if ( prePopDeArray.length > 0 ) {
			// Render the pre-population fields.
			renderPrePopFields(prePopDeArray);
			
			// Reinitialize the pre-pop hash table.
			initPrePopHashTable(prePopDeArray);
		}
		else {
			$.ibisMessaging("dialog", "info", "No pre-population fields were found for this visit type.",{modal:true});
			clearPrePopFieldsAndData();
		}
	})
	.fail(function(jqxhr, textStatus, error) {
		clearPrePopFieldsAndData();
		console.error("Error occurred while getting the prepop value JSON array.");
		$.ibisMessaging("primary", "error", "Error occurred while get the pre-population fields. Please contact the system administrator.");
	});
}


function showWarningMsg(prePopArray, prePopsToSave) {
	var template = TemplateManager.getTemplate("prePopWarnMsg");
	var msg = template({"prePopArray": prePopArray});
	
	$.ibisMessaging("dialog", "warning", msg, {
		buttons : [
			{
				text : '<s:text name="button.Cancel" />',
				"class" : "ui-priority-primary",
				click : function() {
					$(this).dialog("close");
				}
			},
			{
				text : '<s:text name="patient.scheduleVisit.title" />',
				"class" : "ui-priority-secondary",
				click : _.debounce(function() {
					$(this).dialog("close");
					
					// Show wait message.
					$.ibisMessaging("dialog", "info", "Saving changes...", {
						modal: true,
						buttons: []
					});
					
					// Apply new pre-pop values to the hidden form reference.
					$("#pvPrepopValueStrList").val(JSON.stringify(prePopsToSave));
					
					var url = basePath + '/patient/savePatientVisit.action';
					$("#patientVisitForm").attr("action", url).submit();
				}, 3000, true)
			}
		]
	});
}

// Show a the self-reporting link dialog when a link is clicked in the table.
function showTokenLink(guid, link) {
	var msg = "To allow the subject to self-report, send this URL to the subject with GUID " + guid + ": <textarea>" + link + "</textarea>";
	var dialogId = $.ibisMessaging("dialog", "info", msg, {modal: true});
	
	$("#" + dialogId).find("textarea").select();
}

function initPrePopHashTable(prePopArray) {
	prePropHashTable = new HashTable();
	
	for (var i = 0; i < prePopArray.length; i++) {
		prePropHashTable.setItem(prePopArray[i].shortName, prePopArray[i]);
	}
}

function isPrePopInputValid(prePopObj, inputVal) {
	var isValid = true;
	
	switch ( prePopObj.valueType ) {
		case "date" :
			var regex = /\d{4}-\d{2}-\d{2}\s\d{2}:\d{2}/;
			
			// Check to see if the input value looks like a date string.
			if ( !regex.test(inputVal) ) {
				isValid = false;
			}
			
			break;
		case "string" :
			// Check if the input value is a non-empty string.
			if ( inputVal.length == 0 ) {
				isValid = false;
			}
			
			break;
		case "number" :
			//Check if its empty first
			if ( inputVal.length == 0 ) {
				isValid = false;
			} else {
				//Do numeric validation
				var num = Number(inputVal);
				
				// Check if the input value is a number.
				if ( num === NaN ) {
					isValid = false;
				}
			}
			
			break;
		default :
			throw "Couldn't determine the value type of the pre-population JS object.";
	}
	
	return isValid;
}

function getIntervalClinicalPntList(visitTypeId){
		var postData = new FormData();
		postData.append("visitTypeId", visitTypeId);
		$.ajax({
			type : "POST",
			url : basePath + "/patient/patientVisitHomeJsonAction!getVTClinicalPntList.action",
			data : postData,
			cache : false,
			processData : false,
			contentType : false,
			success : function(data) {
				//reload select dropdown
				var clinicPntArr = $.parseJSON(data);
			    if (typeof clinicPntArr == 'object') {
			        var $clinialPntSelect = $('#selectedClinicPntId');
			        $clinialPntSelect.find('option').remove();
			        $('<option>').val("-1").text("-----").appendTo($clinialPntSelect);
			        $.each(clinicPntArr, function(i, item) {
			        	$('<option>').val(item.id).text(item.name).appendTo($clinialPntSelect);
			        });
			    }
			}
		});
};

function getIntervalByClinicalPnt(selectedClinicPntId) {
	//console.log("getIntervalByClinicalPnt() selectedClinicPntId: "+selectedClinicPntId);
	var postData = new FormData();
	postData.append("selectedClinicPntId", selectedClinicPntId);
	$.ajax({
		type : "POST",
		url : basePath + "/patient/patientVisitHomeJsonAction!getCorrespondingVT.action",
		data : postData,
		cache : false,
		processData : false,
		contentType : false,
		success : function(data) {
			//select the visit type
			var intervalObj = $.parseJSON(data);
		    if(typeof intervalObj == 'object') {
		        $("#visitTypeSelect option").each(function(i, item) {
		        	if($(this).val() == intervalObj.id){
		        		$(this).prop('selected', true);
		        	}
		        });
		    }
		}
	});
}

function updatePatientVisitPage(patientId, dlgId) {
	$table = $("#patientVisitTable");
	var postData = new FormData();
	postData.append("patientId", patientId);
	$.ajax({
		type : "POST",
		url : basePath + "/patient/getUpdateByPatient.action",
		data : postData,
		cache : false,
		processData : false,
		contentType : false,
		success : function(data) {
			//reload schedule visits datatable
			$table.idtApi('getTableApi').rows().deselect();
			$table.idtApi('getTableApi').ajax.reload();
			//reload interval status chart
			$("#intervalChartContainer").html(data);
			//close the dialog
			if(dlgId.length > 0){
				$.ibisMessaging("close", {id: dlgId});
			}			
		}
	});
}

$(document).ready(function() {
	// Initialize Handlebar templates.
	initTemplates();
	
	// Initialize Add/Update button based on hidden Action
	var visitId = Number($("#visitId").val());
	
	// Check if patient visit is to be edited.
	if ( (visitId !== NaN) && (visitId > 0) ) {
		$("#btnSavePatientVisit").val('<s:text name="button.Update" />');
	}
	else {
		$("#btnSavePatientVisit").val('<s:text name="button.Add" />');
	}
	
	// Check if the pre-population fields will need to be rendered.
	var visitTypeId = Number($("#visitTypeSelect").val());
	
	if ( (visitTypeId !== NaN) && (visitTypeId > 0) ) {
		var prePopArray = JSON.parse($("#pvPrepopValueStrList").val());
		
		// Check if there are any pre-population values to display.
		if ( (typeof prePopArray == "object") && (prePopArray != null) && (prePopArray.length > 0) ) {
			// Generate the pre-population fields with values.
			renderPrePopFields(prePopArray);
			
			// Initialize the pre-population hashtable.
			initPrePopHashTable(prePopArray);
		}
	}
	else {
		prePropHashTable = new HashTable();
	}
	
	/*
	 * #############################################################
	 * ################## Input Event Handlers #####################
	 * #############################################################
	 */
	
	$("select#patientId").change(function() {
		var $patSelect = $(this);
		var patientOpt = $patSelect.find("option[value='" + $patSelect.val() + "']").text();
		var patientId = Number($(this).val());
		var dlgId = "";
		
		if (patientId !== NaN && patientId > 0) {
			// Show wait message.
			dlgId = $.ibisMessaging("dialog", "info", "Updating page to show only data for " + patientOpt + "...", {
				modal: true,
				buttons: []
			});
		}
		updatePatientVisitPage(patientId, dlgId);
		
		// Save data from any pre-population fields without doing any validation.
		if ( !prePropHashTable.isEmpty() ) {
			// Save any input data to the prep-pop JS object.
			$("#prePopFormFields input").each(function() {
				var $input = $(this);
				var shortName = $input.attr("id");
				var prePopValObj = prePropHashTable.getItem(shortName);
				
				prePopValObj.prePopValue = $input.val().trim();
			});
			
			// Save the array to the hidden form field.
			$("#pvPrepopValueStrList").val(JSON.stringify(prePropHashTable.values()));
		}
		
		//var url = basePath + "/patient/patientVisitHome.action";
		//$("#patientVisitForm").attr("action", url).submit();
		//var oTable = $("#patientVisitTable").idtApi("getTableApi");
		//oTable.ajax.reload();
	});
	
	$("#visitTypeSelect").change(function() {
		var visitTypeId = Number($(this).val());
		
		if ( (visitTypeId === NaN) || (visitTypeId <= 0) ) {
			// Clear out the pre-pop data.
			clearPrePopFieldsAndData();
		}
		else {
			getPrePopFormFields();
		}
		getIntervalClinicalPntList(visitTypeId);
	});
	
	$("#selectedClinicPntId").change(function() {
		var selectedClinicPntId = Number($(this).val());
		if ( (selectedClinicPntId !== NaN) || (selectedClinicPntId > 0) ) {
			getIntervalByClinicalPnt(selectedClinicPntId);
		}
	});
	
	$("#btnCancelPatientVisit").click(_.debounce(function(event) {
		$(this).prop("disabled", true);
		
		// Show wait message.
		$.ibisMessaging("dialog", "info", "Reloading page with its initial state.. Any unsaved data will be discarded.", {
			modal: true,
			buttons: []
		});
		
		var url = basePath + '/patient/patientVisitHome.action';
		redirectWithReferrer(url);
	}, 3000, true));
	
	$("#btnSavePatientVisit").click(_.debounce(function(e) {
		var $prePopFields = $("#prePopFormFields");
		
		// Check if there are any pre-population fields to save.
		if ( $prePopFields.is(":visible") ) {
			var invalidPrePops = [];
			var prePopsToSave = [];
			
			$prePopFields.find("input").each(function() {
				var $input = $(this);
				var shortName = $input.attr("id");
				var prePopValObj = prePropHashTable.getItem(shortName);
				var inputVal = $input.val().trim();
				
				if ( isPrePopInputValid(prePopValObj, inputVal) ) {
					prePopValObj.prePopValue = inputVal;
					prePopsToSave.push(prePopValObj);
				}
				else {
					invalidPrePops.push(prePopValObj);
				}
			});
			
			// Check if there were any invalid prePop values.
			if ( invalidPrePops.length > 0 ) {
				showWarningMsg(invalidPrePops, prePopsToSave);
				
				return false;
			}
			
			// Save new pre-pop values to the form.
			$("#pvPrepopValueStrList").val(JSON.stringify(prePopsToSave));
		}
		
		// Show wait message.
		$.ibisMessaging("dialog", "info", "Saving changes...", {
			modal: true,
			buttons: []
		});
		
		// Reset the form action to the save action.
		$("#patientVisitForm").attr("action", "savePatientVisit.action");
	}, 2000, true));

}); //end document.ready()
</script>

<div>
	<s:text name="patient.scheduleVisit.instruction.display"/>
	<br/>
	<label class="requiredInput"></label> 
	<i><s:text name="patient.scheduleVisit.requiredSymbol.display"/></i>
	<br/><br/>
</div>

<s:form theme="simple" name="patientVisitForm" method="post" id="patientVisitForm" >
    <s:hidden name="id" id="visitId" />
 	<s:hidden name="pvPrepopValues" id="pvPrepopValueStrList" />
 	<div id="divPatientVisit">
 		<table>
 			<tr>
 				<td>
					<div class="formrow_1">      		
						<%if (SUBJECT_DISPLAY_TYPE  == CtdbConstants.PATIENT_DISPLAY_ID) {%>
							<label class="requiredInput"><s:text name="patient.label.SubjectID"/> </label>
							<s:select name="patientId" id="patientId" cssClass="validateMe" list="#session._patientList"  listKey="patientId" listValue="subjectId" 
							headerKey="%{@java.lang.Integer@MIN_VALUE}" headerValue="-----" />
						<%}%>
						<%if (SUBJECT_DISPLAY_TYPE  == CtdbConstants.PATIENT_DISPLAY_GUID) {%>
							<label class="requiredInput"><s:text name="patient.guid.display"/> </label>
							<s:select name="patientId" id="patientId" cssClass="validateMe" list="#session._patientList"  listKey="patientId" listValue="guid" 
								headerKey="%{@java.lang.Integer@MIN_VALUE}" headerValue="-----" />
			        	<%}%>
			        	<%if (SUBJECT_DISPLAY_TYPE  == CtdbConstants.PATIENT_DISPLAY_MRN) {%>
							<label class="requiredInput"><s:text name="patient.scheduleVisit.mrn.display"/> </label>
							<s:select name="patientId" id="patientId" cssClass="validateMe" list="#session._patientList" listKey="patientId" listValue="mrn" 
								headerKey="%{@java.lang.Integer@MIN_VALUE}" headerValue="-----" />
						<%} %>	
					</div>
			
					<div class="formrow_1">
						<label for="visitDateStr" class="requiredInput"><s:text name="patient.visitdate.display"/></label>
						<s:textfield name="visitDateStr" cssClass="dateTimeField validateMe" id="calId2"/>
					</div>
					<div class="formrow_1">
						<label for="visitTypeId" ><s:text name="response.label.interval"/></label>
						<s:select name="visitTypeId" id="visitTypeSelect" list="#session.intervalOptions" listKey="id" listValue="name" 
								headerKey="%{@java.lang.Integer@MIN_VALUE}" headerValue="-----"/>
					</div>
					<s:set var="displayClinicalPoint" value="#systemPreferences.get('display.protocol.clinicalPoint')" />
					<s:if test="#displayClinicalPoint">
						<div class="formrow_1">
							<label for="comments"><s:text name="patient.visitComments.display"/></label>
							<s:textarea name="visitComments" id="visitComments" cols="30" rows="5" cssClass="textfield"/>
						</div>
						<div class="formrow_1" id="intClinicalPnt">
							<label for="vTClinicalPoint" class="requiredInput"><s:text name="patient.vTClinicalPoint.display"/></label>
							<s:select name="selectedClinicPntId" id="selectedClinicPntId" cssClass="validateMe" list="intClinicalPntList" listKey="id" listValue="clinicalPntInfo" 
									headerKey="%{@java.lang.Integer@MIN_VALUE}" headerValue="-----"/>
						</div>
					</s:if>
					<div id="prePopContent" style="display: none;">
						<div class="formrow_1" id="prepopulationDiv" style="width: 50%; padding-left:20px;">
							<p>The following data elements were toggled on for auto-population for this visit type. Please provide values that will be
							populated into the eForms associated with this visit for this particular subject. You will be able to change the values
							during data collection if necessary.</p>
						</div>
						<div id="prePopFormFields"></div>
					</div>
		
					<div class="formrow_2">
						<s:submit id="btnSavePatientVisit" key="button.Add" title="%{getText('tooltip.save')}" />
						<input type="button" id="btnCancelPatientVisit" value="<s:text name='button.Cancel' />" title="<s:text name='tooltip.cancel' />"/>
					</div>
 				</td>
 				<td  valign="top">
 					<s:if test="%{#systemPreferences.get('enable.visittype.scheduler') == @gov.nih.nichd.ctdb.common.CtdbConstants@TRUE}">
						<jsp:include page="/patient/patientVisitIntervalChart.jsp" />
					</s:if>
 				</td>
 			<tr/>
 		</table>
		<h3><s:text name="patient.scheduleVisit.subtitle.display"/></h3>

		<div id="patientVisitContainer" class="idtTableContainer brics">
			<div id="dialog"></div>
			<table id="patientVisitTable" class="table table-striped table-bordered" width="100%">
			</table>
		</div>
	</div>
</s:form>
<script type="text/javascript">
	//reformat the Date to YYYYMMDD
	function getValue(value) {
	   return (value < 10) ? "0" + value : value;
	};

	function getDate () {
	   var newDate = new Date();
	
	   var sMonth = getValue(newDate.getMonth() + 1);
	   var sDay = getValue(newDate.getDate());
	   var sYear = newDate.getFullYear();
	
	   return sYear + sMonth + sDay;
	}
	var now = getDate();


	/**** Tasks to do on page load ****/
	$(document).ready(function() {
		var idtFilterData =  {
			id: $("input[name='id']").val(),
			pvPrepopValues:$("input[name='pvPrepopValues']").val(),
			patientId: $("select[name='patientId']").val(),
			visitDateStr: $("input[name='visitDateStr']").val(),
			visitTypeId: $("select[name='visitTypeId']").val(),
			AgeYrs: $("input[name='AgeYrs']").val(),
			AgeRemaindrMonths: $("input[name='AgeRemaindrMonths']").val()
		}
		$("#patientVisitTable").idtTable({
			idtUrl: "<s:property value='#webRoot'/>/patient/getPatientVisitList.action",
			dom: 'Bfrtip',
			select: "multi",
	        filterData: idtFilterData,
	        idtData: {
	            primaryKey: 'id'
	        },
	        columns: [
	        	<s:if test="#disallowingPII == 1">
					<%if (SUBJECT_DISPLAY_TYPE  == CtdbConstants.PATIENT_DISPLAY_ID) {%>
			        	{
			                name: 'patientId',
			                title: '<%=rs.getValue("response.resolveHome.tableHeader.subjectID",l)%>',
			                parameter: 'idDisplay',
			                data: 'patientId'
			            },
					<%}%>
					<%if (SUBJECT_DISPLAY_TYPE  == CtdbConstants.PATIENT_DISPLAY_GUID) {%>
			        	{
			                name: 'patientId',
			                title: '<%=rs.getValue("subject.table.GUID",l) %>',
			                parameter: 'guidDisplay',
			                data: 'patientId'
			            },
					<%}%>
				</s:if>
				<s:else>
					<%if (SUBJECT_DISPLAY_TYPE  == CtdbConstants.PATIENT_DISPLAY_ID) {%>
			        	{
			                name: 'patientId',
			                title: '<%=rs.getValue("response.resolveHome.tableHeader.subjectID",l)%>',
			                parameter: 'idDisplay',
			                data: 'patientId'
			            },
					<%}%>
					<%if (SUBJECT_DISPLAY_TYPE  == CtdbConstants.PATIENT_DISPLAY_GUID) {%>
			        	{
			                name: 'patientId',
			                title: '<%=rs.getValue("subject.table.GUID",l) %>',
			                parameter: 'guidDisplay',
			                data: 'patientId'
			            },
					<%}%>
					<%if (SUBJECT_DISPLAY_TYPE  == CtdbConstants.PATIENT_DISPLAY_MRN) {%>
			        	{
			                name: 'patientId',
			                title: 'MRN',
			                parameter: 'mrn',
			                data: 'patientId'
			            },				
					<%}%>
		        	{
		                name: 'patientLastName',
		                title: '<%=rs.getValue("user.lastName",l)%>',
		                parameter: 'patientLastName',
		                data: 'patientLastName'
		            },
		        	{
		                name: 'patientFirstName',
		                title: '<%=rs.getValue("user.firstName",l)%>',
		                parameter: 'patientFirstName',
		                data: 'patientFirstName'
		            },				
		        </s:else>
		        	{
		                name: 'intervalName',
		                title: '<%=rs.getValue("protocol.visitType.title.display",l)%>',
		                parameter: 'intervalName',
		                data: 'intervalName'
		            },
		        	{
		                name: 'visitDate',
		                title: '<%=rs.getValue("patient.visitdate.display",l)%>' ,
		                parameter: 'visitDate',
		                data: 'visitDate',
		                render: IdtActions.formatDate()
		            },
		            <security:hasProtocolPrivilege privilege="patientselfreporting">
			        	{
			                name: 'tokenLink',
			                title: '<%=rs.getValue("patient.visitToken.title.display",l)%>',
			                parameter: 'tokenLink',
			                data: 'tokenLink'
			            },
		            </security:hasProtocolPrivilege>
			        <s:if test="#displayClinicalPoint">
		        	{
		                name: 'comments	',
		                title: '<%=rs.getValue("patient.visitComments.display",l)%>' ,
		                parameter: 'comments',
		                data: 'comments'
		            }   
		        	</s:if>
	        ],
	        buttons: [
	        	{
	   				text: "Edit",
	   				className: "editPvBtn",
	                enableControl: {
	                    count: 1,
	                    invert:true
	                },
	                enabled: false,
	   				action: function(e, dt, node, config) {
							
							// Show wait message.
							var loadingMsgId = $.ibisMessaging("dialog", "info", "Loading data for the selected visit...", {
								modal: true,
								buttons: []
							});
							
							var selectedPV = $("#patientVisitTable").idtApi("getSelected");
							
							if ( selectedPV.length == 0 ) {
								$("#" + loadingMsgId).dialog("close");
								$.ibisMessaging("dialog", "info", "No visits are selected.");
								
								return false;
							}
							else {
								$("#visitId").val(selectedPV[0]);
								$("#patientVisitForm").attr("action", "getPatientVisit.action").submit();
							}
	
	   				}
	   			},
	        	{
	   				text: "Delete",
	   				className: "DeletePvBtn",
	                enabled: false,
	   				action: function(e, dt, node, config) {
							
	   					var confirmMsg = '<s:text name="patient.scheduleVisit.alert.remove" />'
	   						
	   						// Show confirmation message.
	   						var msgRef = $.ibisMessaging("dialog", "info", confirmMsg, {
	   							modal : true,
	   							buttons : [
	   								{
	   									text : '<s:text name="button.yes" />',
	   									click : _.debounce(function() {
	   										// Hide the dialog buttons, and update the message.
	   										var $dialogDiv = $(this);
	   										
	   										$dialogDiv.siblings().find(".ui-dialog-buttonset").hide();
	   										$dialogDiv.find(".ibisMessaging-info").text("Deleting the selected visits from the system...");
	   										
	   										// Call the delete action.
	   										var selectedPVs = $("#patientVisitTable").idtApi("getSelected");
	   										var url = basePath 
	   											+ '/patient/deletePatientVisit.action?selectedIds=' + encodeURIComponent(JSON.stringify(selectedPVs));
	   										
	   										redirectWithReferrer(url);
	   									}, 3000, true)
	   								},
	   								{
	   									text : '<s:text name="button.no" />',
	   									click : function() {
	   										$(this).dialog("close");
	   									}
	   								}
	   							]
	   						});
	
	   				}
	   			},   			        	
	        	{
					extend: "collection",
					title: 'Patient_Visit_'+now,
					buttons: [
			            {
			                extend: 'csv',
			                text: 'csv',
			                className: 'btn btn-xs btn-primary p-5 m-0 width-35 assets-export-btn  buttons-csv',
			                title: 'export_filename',
			                extension: '.csv',
			                name: 'csv',
			                exportOptions: {
			                  orthogonal: 'export'
			                },
			                enabled: true,
			                action: IdtActions.exportAction()
			            },					
						{
			                extend: 'excel',
			                className: 'btn btn-xs btn-primary p-5 m-0 width-35 assets-export-btn  buttons-excel',
			                extension: '.xlsx',
							exportOptions: {
								orthogonal: 'export'
							},
							enabled: true,
							action: IdtActions.exportAction()
							
						}				
					]
				}  			
	        ]
		});
	});
</script>	
<jsp:include page="/common/footer_struts2.jsp" />
</html>
