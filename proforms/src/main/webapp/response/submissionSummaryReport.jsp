<jsp:include page="/common/doctype.jsp" />

<%@ page import="gov.nih.nichd.ctdb.common.rs, java.util.Locale" %>
<%@ taglib uri="/struts-tags" prefix="s"%>
<%@ taglib uri="/WEB-INF/datatables.tld" prefix="idt" %>
<%@ taglib uri="/WEB-INF/security.tld" prefix="security" %>
<%@ page import="gov.nih.nichd.ctdb.protocol.domain.Protocol"%>
<%@ page import="gov.nih.nichd.ctdb.common.CtdbConstants"%>
<%@ page buffer="100kb"%>

<%
	Locale l = request.getLocale();
Protocol curProtocol = (Protocol) session.getAttribute(CtdbConstants.CURRENT_PROTOCOL_SESSION_KEY);
int SUBJECT_DISPLAY_TYPE = curProtocol.getPatientDisplayType();
request.setAttribute("SUBJECT_DISPLAY_TYPE", SUBJECT_DISPLAY_TYPE);
String SUBJECT_DISPLAY_LABEL = curProtocol.getPatientDisplayLabel();
request.setAttribute("SUBJECT_DISPLAY_LABEL", SUBJECT_DISPLAY_LABEL);
%>

<html>
	<%-- Include Header --%>
	<s:set var="pageTitle" scope="request">
		<s:text name="report.submissionSummary.title.display"/>
	</s:set>
	
	<jsp:include page="/common/header_struts2.jsp" />
<script type="text/javascript">
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
	
$(document).ready(function(){
	var webRoot = "<s:property value='#webRoot'/>";
	$("#submissionSummaryReportTable").idtTable({
		idtUrl: webRoot + "/response/getSubmissionSummaryList.action",
		dom: 'Bfrtip',
		autoWidth: false,
		pageLength: 15,
		columns: [
			<%
				if (SUBJECT_DISPLAY_TYPE == CtdbConstants.PATIENT_DISPLAY_ID) {
			%>
					{
						name: 'nrn',
						title: '<%=rs.getValue("patient.label.SubjectID", l)%>',
						parameter: 'nrn',
						data: 'nrn'
					},
			<%
				}
			%>
			<%
				if (SUBJECT_DISPLAY_TYPE == CtdbConstants.PATIENT_DISPLAY_GUID) {
			%>
					{
						name: 'guid',
						title: '<%=rs.getValue("report.subjectGUID", l) %>',
						parameter: 'guid',
						data: 'guid'
					},
			<%
				}
			%>
			<%
				if (SUBJECT_DISPLAY_TYPE == CtdbConstants.PATIENT_DISPLAY_MRN) {
			%>
					{
						name: 'mrn',
						title: '<%=rs.getValue("patient.mrn.display", l) %>',
						parameter: 'mrn',
						data: 'mrn'
					},
			<%
				}
			%>
			{
				name: 'formName',
				title: '<%= rs.getValue("report.frl.label.formName", l) %>',
				parameter: 'formName',
				data: 'formName'
			},
			{
				name: 'visitTypeName',
				title: '<%= rs.getValue("report.frl.label.visitType", l) %>',
				parameter: 'visitTypeName',
				data: 'visitTypeName'
			},
			{
				name: 'visitDateStr',
				title: '<%= rs.getValue("report.column.label.visitDate", l) %>',
				parameter: 'visitDateStr',
				data: 'visitDateStr'
			},
			{
				name: 'submissionStatus',
				title: '<%= rs.getValue("report.column.label.submissionStatus", l) %>',
				parameter: 'submissionStatus',
				data: 'submissionStatus'
			}
		],	        
		buttons: [
			{
				extend: "collection",
				title: 'Submission_Summary_Report_'+now,
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
	<p><s:text name="report.submissionSummary.instruction"/></p>
	<br/>
	<div class="idtTableContainer brics" id="submissionSummaryTableDiv" >
		<table id="submissionSummaryReportTable" class="table table-striped table-bordered" width="100"></table>
	</div>
	
	<jsp:include page="/common/footer_struts2.jsp" />
</html>