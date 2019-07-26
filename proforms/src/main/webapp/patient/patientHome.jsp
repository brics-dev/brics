<%@ page autoFlush="true" buffer="1094kb"%>
<jsp:include page="/common/doctype.jsp" />
<%@ page import="gov.nih.nichd.ctdb.common.CtdbConstants"%>
<%@ page import="gov.nih.nichd.ctdb.common.Image"%>
<%@ page import="gov.nih.nichd.ctdb.common.rs,java.util.Locale" %>
<%@ page import="gov.nih.nichd.ctdb.patient.common.PatientResultControl"%>
<%@ page import="gov.nih.nichd.ctdb.protocol.domain.Protocol"%>
<%@ page import="gov.nih.nichd.ctdb.security.domain.User"%>
<%@ page import="java.util.List"%>
<%@ taglib uri="/struts-tags" prefix="s"%>
<%@ taglib uri="/WEB-INF/display.tld" prefix="display"%>
<%@ taglib uri="/WEB-INF/datatables.tld" prefix="idt" %>
<%@ taglib uri="/WEB-INF/security.tld" prefix="security"%>


<%
	Protocol curProtocol = (Protocol) session.getAttribute(CtdbConstants.CURRENT_PROTOCOL_SESSION_KEY);
	String CURPROTOCOL_NUMBER = curProtocol.getProtocolNumber();
	int SUBJECT_DISPLAY_TYPE = curProtocol.getPatientDisplayType();
	String SUBJECT_DISPLAY_LABEL = curProtocol.getPatientDisplayLabel();
	Locale l = request.getLocale();
	String selectGuidWithoutCollection = request.getParameter("selectGuidWithoutCollection");

%>

<%-- CHECK PRIVILEGES --%>
<security:check privileges="viewpatients" />
<style>
 	#patientListTable_wrapper {
 		overflow: visible;
 	}
</style>
<html>
<s:set var="pageTitle" scope="request">
	<s:text name="subject.table.title.display"/>
	</s:set>
<jsp:include page="/common/header_struts2.jsp" />

<s:set var="disallowingPII" value="#systemPreferences.get('guid_with_non_pii')" />


<s:form method="post" theme="simple" name="patientSearchForm">
    <div><s:text name="patient.searchform.instruction.display"/> </div>
	<h3 class="toggleable collapsed">
		<s:text name="patient.search.searchform.display" />
	</h3>
	<div id="search_patient" class="searchContainer">
	
     	<s:if test="#disallowingPII == 1">
	 		<div class="formrow_2">
	 			<label for="recordNumberSearch"><s:text name="patient.recordnumber.display" /></label>
				<s:textfield name="recordNumberSearch" size="15" maxlength="128" id="recordNumberSearch" />
			</div>
			<div class="formrow_2">
				<label for="inProtocol"><s:text name="patient.tab.protocol.display" /></label>
	 			<s:select name="enrollmentStatus" id="inProtocol" list="#session.enrollmentStatusList"/>
			</div>
			<div class="formrow_2">
				<label for="subjectNumberSearch"><s:text name="patient.subjectnumber.display" /></label>
				<s:textfield name="subjectNumberSearch" size="15" maxlength="128" id="subjectNumberSearch"  />
			</div>
			<div class="formrow_2">
				<label for="activeInProtocol"><s:text name="patient.status.display" /></label>
				<s:select name="activeInProtocol" id="activeInProtocol" list="#{'':'All', 'yes':'Active', 'no':'Inactive'}" />
				<a title="This status refers to subject's status within a particular study"><img src="../images/icons/info-icon.png"></a> 
	 		</div>
			<div class="formrow_2">
				<label for="guidSearch"><s:text name="patient.guid.display" /></label>
				<s:textfield name="guidSearch" size="15" maxlength="128" id="guidSearch" />
			</div>
		</s:if>
		<s:else>
			<div class="formrow_2">
	 			<label for="mrnSearch"><s:text name="patient.mrn.display" /></label>
				<s:textfield name="mrnSearch" size="15" maxlength="128" id="mrnSearch" />
			</div>

			<div class="formrow_2">
 				<label for="recordNumberSearch"><s:text name="patient.recordnumber.display" /></label>
				<s:textfield name="recordNumberSearch" size="15" maxlength="128"  id="recordNumberSearch"  />
			</div>
			<div class="formrow_2">
				<label for="lastNameS"><s:text name="patient.lastname.display" /></label>
				<s:textfield name="lastNameSearch" size="15" maxlength="128" id="lastNameS" />
			</div>
			<div class="formrow_2">
					<label for="guidSearch"><s:text name="patient.guid.display" /></label>
					<s:textfield name="guidSearch" size="15" maxlength="128" id="guidSearch" />
			</div>
			<div class="formrow_2">
				<label for="firstNameSearch"><s:text name="patient.firstname.display" /></label>
				<s:textfield name="firstNameSearch" size="15" maxlength="128" id="firstNameSearch" />
			</div>
			<div class="formrow_2">
				<label for="inProtocol"><s:text name="patient.tab.protocol.display" /></label>
	 			<s:select name="enrollmentStatus" id = "inProtocol" list="#session.enrollmentStatusList" /> 
			</div>
			<div class="formrow_2">
				<label for="subjectNumberSearch"><s:text name="patient.subjectnumber.display" /></label>
				<s:textfield name="subjectNumberSearch" size="15" maxlength="128" id="subjectNumberSearch"  />
			</div>
			<div class="formrow_2">
				<label for="activeInProtocol"><s:text name="patient.status.display" /></label>
				<s:select name="activeInProtocol" id="activeInProtocol" list="#{'':'All', 'yes':'Active', 'no':'Inactive'}" />
				<a title="This status refers to subject's status within a particular study"><img src="../images/icons/info-icon.png"></a> 
	 		</div>
		</s:else>
		
		<div class="formrow_1">
			<input type="button" value="<s:text name='button.Reset'/>" id="btnResetPatientSearch" title = "Click to clear fields"/>
			<input type="button" value="<s:text name='button.Search'/>" id="btnPatientSearch" title = "Click to search" /> 
		</div>
	</div>
 	<h3> <s:text name="subject.table.title.display"/> </h3>
	<div>
		<s:text name="subject.table.subinstruction.display"/>
	</div>
	
	<div id="patientListContainer" class="idtTableContainer brics">
		<div id="dialog"></div>
		<table id="patientListTable" class="table table-striped table-bordered" width="100%">
		</table>
	</div>	
	</s:form>
<script type="text/javascript">
var searchSubmitted = "<s:property value='searchSubmitted' />";
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

$("input#btnPatientSearch").click(function() {

	$("form[name='patientSearchForm']").submit();
	
});

$("input#btnResetPatientSearch").click(function() {
	$("div#search_patient").find("select#inProtocol").val("this");
	$("div#search_patient").find("input:text, input:file, select#activeInProtocol").val("");
	
	$("form[name='patientSearchForm']").submit();
});

/**** Tasks to do on page load ****/
$(document).ready(function() {

	if($("select#inProtocol option:selected").val()== "none") { // not enrolled
		$("input#subjectNumberSearch, input#patientGroupNameSearch, select#activeInProtocol").prop("disabled", true);
	}
	else{
		$("input#subjectNumberSearch, input#patientGroupNameSearch, select#activeInProtocol").prop("disabled", false);
	}
	
	$("select#inProtocol").change(function() {
		if($("select#inProtocol option:selected").val() == "none") { // not enrolled
			$("input#subjectNumberSearch, input#patientGroupNameSearch, select#activeInProtocol").prop("disabled", true);
		}
		else{
			$("input#subjectNumberSearch, input#patientGroupNameSearch, select#activeInProtocol").prop("disabled", false);
		}

	});
	
	
	var basePath = "<s:property value='#webRoot'/>";
	$("#patientListTable").idtTable({
		idtUrl: "<s:property value='#webRoot'/>/patient/getPatientList.action",
		dom: 'Bfrtip',
		select: "multi",
        idtData: {
            primaryKey: 'id'
        },
        filterData: {
			recordNumberSearch: $("#recordNumberSearch").val(),
			enrollmentStatus: $("#inProtocol").val(),
			subjectNumberSearch: $("#subjectNumberSearch").val(),
			activeInProtocol: $("#activeInProtocol").val(),
			guidSearch: $("#guidSearch").val()
        },
        columns: [
        	{
                name: 'patientId',
                title: '<%=SUBJECT_DISPLAY_LABEL%>',
                parameter: 'protocolConfiguredDisplay',
                data: 'patientId'
            },			       	
        	<s:if test="#disallowingPII == 0">
	        	{
	                name: 'lastName',
	                title: '<%=rs.getValue("user.lastName",l)%>',
	                parameter: 'lastName',
	                data: 'lastName'
	            },
	        	{
	                name: 'firstName',
	                title: '<%=rs.getValue("user.firstName",l)%>',
	                parameter: 'firstName',
	                data: 'firstName'
	            },	            
			</s:if>      
	            {
	                name: 'patientProtocolStatus',
	                title: '<%=rs.getValue("subject.table.status",l)%>',
	                parameter: 'patientProtocolStatus',
	                data: 'patientProtocolStatus',
	                "render": function(data, type, row, full) {
	                	var options = $("#patientListTable").idtApi('getOptions');
	                	var disabledList = options.selectionDisabled;
	                	if(data == "") {
	                		if (disabledList.indexOf(row.DT_RowId) === -1) {
								disabledList.push(row.DT_RowId);
								$("#"+ row.DT_RowId).addClass("idtSelectionDisabled");
								//data.selectionDisable = true;
							}	                		
	                	}
	                	return data;
	                }
	            },	      
	            {
	                name: 'formatValidated',
	                title: '<%=rs.getValue("patient.validated",l)%>',
	                parameter: 'formatValidated',
	                data: 'formatValidated'
	            },
	            {
	                name: 'protocols',
	                title: '<%=rs.getValue("app.label.lcase.protocol",l)%>',
	                parameter: 'protocols',
	                data: 'protocols'
	            },
	            {
	                name: 'futureStudy',
	                title: '',
	                parameter: 'futureStudy',
	                data: 'futureStudy',
	                visible: false
	            }	            
	            
        ],
        buttons: [
        	<security:hasProtocolPrivilege privilege="addeditpatients">
        	{
   				text: "<s:text name='button.Edit' />",
   				className: "editPatientBtn",
                enableControl: {
                    count: 1,
                    invert:true
                },
                enabled: false,
   				action: function(e, dt, node, config) {
						var selectedSubj = $("#patientListTable").idtApi("getSelected");
						var url = basePath + "/patient/showEditPatient.action?sectionDisplay=default&patientId=" + selectedSubj[0];						redirectWithReferrer(url);
   				}
   			},
   			</security:hasProtocolPrivilege>
   			<security:hasProtocolPrivilege privilege="viewpatients">
        	{
   				text: "<s:text name='button.View' />",
   				className: "viewPatientBtn",
                enableControl: {
                    count: 1,
                    invert:true
                },
                enabled: false,
   				action: function(e, dt, node, config) {
						var selectedSubj = $("#patientListTable").idtApi("getSelected");
						var url = basePath+ "/patient/viewPatient.action?sectionDisplay=default&patientId=" + selectedSubj[0];
						redirectWithReferrer(url);					
   				}
   			},
   			</security:hasProtocolPrivilege> 
   			<security:hasProtocolPrivilege privilege="manageAttachments">
        	{
   				text: "<s:text name='patient.attachment.title' />" ,
   				className: "attachmentsBtn",
                enableControl: {
                    count: 1,
                    invert:true
                },
                enabled: false,
   				action: function(e, dt, node, config) {
						var selectedSubj = $("#patientListTable").idtApi("getSelected");
						var url = basePath+ "/patient/showEditPatient.action?sectionDisplay=attachments&patientId=" + selectedSubj[0];
						redirectWithReferrer(url);

   				}
   			},
   			</security:hasProtocolPrivilege>
   			<security:hasProtocolPrivilege privilege="viewforms">
        	{
   				text: "<s:text name='button.AllCompletedForms' />" ,
   				className: "allCompletedFormsBtn",
                enableControl: {
                    count: 1,
                    invert:true
                },
                enabled: false,
   				action: function(e, dt, node, config) {
						var selectedSubj = $("#patientListTable").idtApi("getSelected");
						var url = basePath+ "/patient/viewPatient.action?sectionDisplay=allCompletedForms&patientId=" + selectedSubj[0];
						redirectWithReferrer(url);						
   				}
   			},
   			</security:hasProtocolPrivilege>
   			<security:hasProtocolPrivilege privilege="viewaudittrails">
        	{
   				text: "<s:text name='button.ViewAudit' />" ,
   				className: "viewAuditBtn",
                enableControl: {
                    count: 1,
                    invert:true
                },
                enabled: false,
   				action: function(e, dt, node, config) {
						var selectedSubj = $("#patientListTable").idtApi("getSelected");
						var url = basePath+ "/patient/patientAudit.action?patientId=" + selectedSubj[0];
						openPopup(url,"Patient_Audit", "width=960,height=600,toolbar=0,location=0,directories=0,,menubar=0,status=1,scrollbars=1,resizable=0");
   				}
   			},
   			</security:hasProtocolPrivilege> 
   			<security:hasProtocolPrivilege privilege="addeditschedulevisits">
        	{
   				text: "<s:text name='patient.scheduleVisit.title' />",
   				className: "scheduleVisitsBtn",
                enableControl: {
                    count: 1,
                    invert:true
                },
                enabled: false,
   				action: function(e, dt, node, config) {
						var selectedSubj = $("#patientListTable").idtApi("getSelected");
						var url = basePath+ '/patient/patientVisitHome.action?patientId=' + selectedSubj[0];
						redirectWithReferrer(url);						
   				}
   			},
   			</security:hasProtocolPrivilege> 
   			<security:hasProtocolPrivilege privilege="addeditpatients">
        	{
   				text: "<s:text name='button.Delete' />",
   				className: "deleteBtn",
                enabled: false,
   				action: function(e, dt, node, config) {
						var selectedIds = $("#patientListTable").idtApi("getSelected");
							$.ibisMessaging(
	 								"dialog", 
	 								"info", 
	 								"<s:text name='subject.alert.remove' />", {
	 								buttons: {
	 									"OK": function() {
	 										var url = basePath+ '/patient/deletePatient.action';
	 										
	 										$(this).dialog("close");
	 										return $.ajax({
	 											type: "POST",
	 											url: url,
	 											data: {selectedIds: selectedIds.join()},
	 											success: function(response){
 													document.open();
 													document.write(response);
 													document.close();
 													return "success"; 
	 											}									
	 										});
	 									},
	 									"Cancel": function() {
	 										$(this).dialog("close");
	 									}
	 								}
	 							});						
   				}
   			}, 
        	{
   				text: "<s:text name='button.AddToCurrentStudy' />",
   				className: "addToStudyBtn",  				
                enabled: false,
   				action: function(e, dt, node, config) {
						var selectedSubj = $("#patientListTable").idtApi("getSelected");
						var url = basePath + "/patient/showEditPatient.action?sectionDisplay=default&patientId=" + selectedSubj[0];
						redirectWithReferrer(url);

   				}
   			},   			
   			</security:hasProtocolPrivilege>
			{
				extend: "collection",
				title: 'Subject_Report_'+now,
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
        ],
        initComplete:function(settings) {
        	if($("select#inProtocol option:selected").val() != 'this'){
        		$(".addToStudyBtn").show();
        		console.log('test');
        	}else {
        		$(".addToStudyBtn").hide();
        	}
        	var oTable = $("#patientListTable").idtApi("getTableApi");
        	//check for rows status
        	oTable.on('select', function(e, dt, type, indexes) {
        		var selectedRowData = dt.rows(indexes).data()[0];
        		var selectedRowsList = $("#patientListTable").idtApi("getSelected");
        		if(selectedRowsList.length == 1) {
        			if(selectedRowData.protocols == "<%=CURPROTOCOL_NUMBER%>" || selectedRowData.futureStudy == " FALSE") {
        				oTable.buttons(['.addToStudyBtn']).disable();
        				
        			}else {
        				oTable.buttons(['.addToStudyBtn']).enable();
        			}			
        		}else {
        			oTable.buttons(['.addToStudyBtn']).disable();
        		}
        	})
        	oTable.on('deselect', function(e, dt, type, indexes) {
        		var selectedRowData = dt.rows(indexes).data()[0];
        		var selectedRowsList = $("#patientListTable").idtApi("getSelected");
        		if(selectedRowsList.length == 1) {
        			var rowId = "#"+ selectedRowsList[0];
        			var selectedRowData = oTable.row(rowId).data();
        			if(selectedRowData.protocols == "<%=CURPROTOCOL_NUMBER%>" || selectedRowData.futureStudy == " FALSE") {
        				oTable.buttons(['.addToStudyBtn']).disable();	
        			}else {
        				oTable.buttons(['.addToStudyBtn']).enable();
        			}			
        		}else {
        			oTable.buttons(['.addToStudyBtn']).disable();
        		}
        	})	      
        }
	})	
});
</script>	
	<jsp:include page="/common/footer_struts2.jsp" />
</html>



