<jsp:include page="/common/doctype.jsp" />

<%@ page import="gov.nih.nichd.ctdb.common.rs, java.util.Locale" %>
<%@ taglib uri="/struts-tags" prefix="s"%>
<%@ taglib uri="/WEB-INF/datatables.tld" prefix="idt" %>
<%@ page import="gov.nih.nichd.ctdb.protocol.domain.Protocol"%>
<%@ page import="gov.nih.nichd.ctdb.common.CtdbConstants"%>
<%@ page buffer="100kb"%>

<% Locale l=request.getLocale(); 

Protocol curProtocol = (Protocol) session.getAttribute(CtdbConstants.CURRENT_PROTOCOL_SESSION_KEY);
int SUBJECT_DISPLAY_TYPE = curProtocol.getPatientDisplayType();
request.setAttribute("SUBJECT_DISPLAY_TYPE", SUBJECT_DISPLAY_TYPE);
String SUBJECT_DISPLAY_LABEL = curProtocol.getPatientDisplayLabel();
request.setAttribute("SUBJECT_DISPLAY_LABEL", SUBJECT_DISPLAY_LABEL);
%>


<html>


<s:set var="pageTitle" scope="request">
	<s:text name="report.CompletedFormsReport" />
</s:set>
<jsp:include page="/common/header_struts2.jsp" />

<p><s:text name="report.completedFormsReport.instruction" /></p>
<br>

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
	
$(document).ready(function(){
	var webRoot = "<s:property value='#webRoot'/>";
	$("#lockedFormsReportTable").idtTable({
		idtUrl: "<s:property value='#webRoot'/>/response/getCompletedFormsList.action",
		dom: 'Bfrtip',
		columns: [
			<%
			if (SUBJECT_DISPLAY_TYPE  == CtdbConstants.PATIENT_DISPLAY_ID) {
			%>
		 		{
		 			name: 'cfvSubjectNrn',
					title: '<%=rs.getValue("patient.label.SubjectID", l)%>',
					parameter: 'cfvSubjectNrn',
					data: 'cfvSubjectNrn'
		 		},
			<%
			}
			%>
			<%
			if (SUBJECT_DISPLAY_TYPE  == CtdbConstants.PATIENT_DISPLAY_GUID) {
			%>
				{
					name: 'cfvSubjectGuid',
					title: '<%=rs.getValue("report.subjectGUID",l)%>',
					parameter: 'cfvSubjectGuid',
					data: 'cfvSubjectGuid'
				},
			<%
			}
			%>
			<%
			if (SUBJECT_DISPLAY_TYPE  == CtdbConstants.PATIENT_DISPLAY_MRN) {
			%>
				{
					name: 'cfvSubjectMrn',
					title: '<%=rs.getValue("patient.mrn.display", l)%>',
					parameter: 'cfvSubjectMrn',
					data: 'cfvSubjectMrn'
				},
			<%
			}
			%>
			{
				name: 'cfvFilledFormsName',
				title: '<%=rs.getValue("report.filledForm",l)%>',
				parameter: 'cfvFilledFormsName',
				data: 'cfvFilledFormsName'
			},
			{
				name: 'cfvVisitDate',
				title: '<%=rs.getValue("visitdate.display",l)%>',
				parameter: 'cfvVisitDate',
				data: 'cfvVisitDate'
			},
			{
				name: 'answeredQuesCount',
				title: '<%=rs.getValue("report.numOfQuestionsAnswered",l)%>',
				parameter: 'answeredQuesCount',
				data: 'answeredQuesCount'
			},
			{
				name: 'ccvIntervalName',
				title: '<%=rs.getValue("protocol.visitType.title.display",l)%>',
				parameter: 'ccvIntervalName',
				data: 'ccvIntervalName'
			}
	        ],
	        buttons: [
				{
					extend: "collection",
					title: 'Locked_Forms_'+now,
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

<div id="lockedFormsReportContainer" class="idtTableContainer brics">
	<table id="lockedFormsReportTable" class="table table-striped table-bordered" width="100%">
	</table>
</div>	

<jsp:include page="/common/footer_struts2.jsp" />
</html>