<jsp:include page="/common/doctype.jsp" />

<%@ page import="gov.nih.nichd.ctdb.common.rs, java.util.Locale" %>
<%@ taglib uri="/struts-tags" prefix="s" %>
<%@ taglib uri="/WEB-INF/datatables.tld" prefix="idt" %>
<%@ taglib uri="/WEB-INF/security.tld" prefix="security" %>

<%@ page buffer="100kb"%>

<%
	Locale l = request.getLocale();
%>

<html>
<s:set var="pageTitle" scope="request">
	<s:text name="report.studyReport" />
</s:set>
	
<jsp:include page="/common/header_struts2.jsp" />
<s:set var="clientName" value="#systemPreferences.get('template.global.appName')" />


<p><s:text name="report.studyReport.instruction" /></p>
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
$(document).ready(function() {
	

	var clientName = '<s:property value="clientName" />';
	
	if(clientName == 'fitbir' || clientName == 'cdrns' || clientName == 'cistar' || clientName == 'cnrm' || clientName == 'eyegene') {
		$("#studyReportsTable").idtTable({
			idtUrl: "<s:property value='#webRoot'/>/response/getStudyReportList.action",
			dom: 'Bfrtip',
			autoWidth:false,
	        columns: [
	        	{
	                name: 'studyName',
	                title: '<%=rs.getValue("study.add.name.display",l)%>',
	                parameter: 'studyName',
	                data: 'studyName',
	                width: '20%',
	                render: IdtActions.ellipsis(40)
	            },
	            {
	                name: 'studyPI',
	                title: '<%=rs.getValue("report.studyReport.pi",l)%>',
	                parameter: 'studyPI',
	                data: 'studyPI',
	                width: '10%',
	                render: IdtActions.ellipsis(20)
	            },

	            {
	                name: 'studyStartDate',
	                title: 'Start Date',
	                parameter: 'studyStartDate',
	                data: 'studyStartDate',
	                width: '9%'
	            },
	            
	            {
	                name: 'studyEndDate',
	                title: 'End Date',
	                parameter: 'studyEndDate',
	                data: 'studyEndDate',
	                width: '9%'
	            },
	            
	            {
	                name: 'studyNumberSubjects',
	                title: '<%=rs.getValue("report.numOfSubjectsEstimated",l)%>',
	                parameter: 'studyNumberSubjects',
	                data: 'studyNumberSubjects'
	            },


	            {
	                name: 'studySubjectCount',
	                title: '<%=rs.getValue("report.numOfSubjectsEnrolled",l)%>',
	                parameter: 'studySubjectCount',
	                data: 'studySubjectCount'
	            },
	            {
	                name: 'studyEformCount',
	                title: '<%=rs.getValue("report.numOfEforms",l)%>',
	                parameter: 'studyEformCount',
	                data: 'studyEformCount',
	                width: '7.4%'
	            },
	            {
	                name: 'studyAdminFormsCount',
	                title: '<%=rs.getValue("report.numOfFormsAdministered",l)%>',
	                parameter: 'studyAdminFormsCount',
	                data: 'studyAdminFormsCount'
	            },
	            {
	                name: 'studyLockedAdminFormsCount',
	                title: '<%=rs.getValue("report.numOfAdministeredFormsLocked",l)%>',
	                parameter: 'studyLockedAdminFormsCount',
	                data: 'studyLockedAdminFormsCount'
	            },
	            {
	                name: 'studyNotLockedAdminFormsCount',
	                title: '<%=rs.getValue("report.numOfAdministeredFormsNotLocked",l)%>',
	                parameter: 'studyNotLockedAdminFormsCount',
	                data: 'studyNotLockedAdminFormsCount',
	                width: '6.8%'
	            }  
            

	        ],
	        buttons: [
				{
					extend: "collection",
					title: 'Protocol_Report_'+now,
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
		})	
	}else {
		$("#studyReportsTable").idtTable({
			idtUrl: "<s:property value='#webRoot'/>/response/getStudyReportList.action",
			dom: 'Bfrtip',
			autoWidth:false,
	        columns: [
	        	{
	                name: 'studyName',
	                title: '<%=rs.getValue("study.add.name.display",l)%>',
	                parameter: 'studyName',
	                data: 'studyName',
	                width: '30%',
	                render: IdtActions.ellipsis(40)
	            },
	            {
	                name: 'studyPI',
	                title: '<%=rs.getValue("report.studyReport.pi",l)%>',
	                parameter: 'studyPI',
	                data: 'studyPI',
	                width: '30%',
	                render: IdtActions.ellipsis(40)
	            },


	            {
	                name: 'studyType',
	                title: '<%=rs.getValue("report.studyType",l)%>',
	                parameter: 'studyType',
	                data: 'studyType'
	            },

	            {
	                name: 'studySubjectCount',
	                title: '<%=rs.getValue("report.numOfSubjectsEnrolled",l)%>',
	                parameter: 'studySubjectCount',
	                data: 'studySubjectCount'
	            },
	            {
	                name: 'studyAdminFormsCount',
	                title: '<%=rs.getValue("report.numOfAdministeredForms",l)%>',
	                parameter: 'studyAdminFormsCount',
	                data: 'studyAdminFormsCount'
	            }            

	        ],
	        buttons: [
				{
					extend: "collection",
					title: 'Protocol_Report_'+now,
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
		})	
	}
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
		
});
</script>

<div id="studyReportsContainer" class="idtTableContainer brics">
	<table id="studyReportsTable" class="table table-striped table-bordered" width="100%">
	</table>
</div>	

<jsp:include page="/common/footer_struts2.jsp" />
</html>