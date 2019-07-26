<jsp:include page="/common/doctype.jsp" />

<%@ page import="gov.nih.nichd.ctdb.common.rs, java.util.Locale" %>
<%@ taglib uri="/struts-tags" prefix="s" %>
<%@ taglib uri="/WEB-INF/datatables.tld" prefix="idt" %>
<%@ taglib uri="/WEB-INF/security.tld" prefix="security" %>
<%@ page buffer="100kb" %>

<%
	Locale l = request.getLocale();
%>

<html>
	<%-- Include Header --%>
	<s:set var="pageTitle" scope="request">
		<s:text name="report.PerformanceOverviewReport"/>
	</s:set>
	<jsp:include page="/common/header_struts2.jsp" />
	
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
	$("#performanceOverviewReportTable").idtTable({
		idtUrl: "<s:property value='#webRoot'/>/response/getPerformanceOverviewReportList.action",
		dom: 'Bfrtip',
        columns: [
        	{
                name: 'primarySiteName',
                title: '<%=rs.getValue("report.column.label.primarySiteName",l)%>',
                parameter: 'primarySiteName',
                data: 'primarySiteName'
            },
            {
                name: 'studyNumber',
                title: '<%=rs.getValue("report.column.label.studyNumber",l)%>',
                parameter: 'studyNumber',
                data: 'studyNumber'
            },
            {
                name: 'informedConsentEnrollmentCount',
                title: '<%=rs.getValue("report.column.label.informedConsentEnrollment",l)%>',
                parameter: 'informedConsentEnrollmentCount',
                data: 'informedConsentEnrollmentCount'
            },
            {
                name: 'csfCollectionDataCount',
                title: '<%=rs.getValue("report.column.label.csfCollectionData",l)%>',
                parameter: 'csfCollectionDataCount',
                data: 'csfCollectionDataCount'
            },
            {
                name: 'protocolDeviationsCount',
                title: '<%=rs.getValue("report.column.label.protocolDeviations",l)%>' ,
                parameter: 'protocolDeviationsCount',
                data: 'protocolDeviationsCount'
            }, 
            {
                name: 'adverseEventsCount',
                title: '<%=rs.getValue("report.column.label.adverseEvents",l)%>',
                parameter: 'adverseEventsCount',
                data: 'adverseEventsCount'
            }, 
            {
                name: 'earlyTermination',
                title: '<%=rs.getValue("report.column.label.earlyTermination",l)%>',
                parameter: 'earlyTermination',
                data: 'earlyTermination'
            }

        ],
        buttons: [
			{
				extend: "collection",
				title: 'Performance_Overview_'+now,
				buttons: [
					{
		                extend: 'excel',
		                className: 'btn btn-xs btn-primary p-5 m-0 width-35 assets-export-btn  buttons-excel',
		                extension: '.xlsx',
						exportOptions: {
							orthogonal: 'export'
						},
						enabled: true,
						action: IdtActions.exportAction()
						
					},
		            {
		                extend: 'csv',
		                text: 'csv',
		                className: 'btn btn-xs btn-primary p-5 m-0 width-35 assets-export-btn  buttons-csv',
		                extension: '.csv',
		                exportOptions: {
		                  orthogonal: 'export'
		                },
		                enabled: true,
		                action: IdtActions.exportAction()
		            },					
				]
			}			
		]
	})		
});
</script>
	
	<p><s:text name="report.performanceOverviewReport.instruction"/></p>
	<br>

	<div id="performanceOverviewReportContainer" class="idtTableContainer brics">
		<table id="performanceOverviewReportTable" class="table table-striped table-bordered" width="100%">
		</table>
	</div>		
	<%-- Include Footer --%>
	<jsp:include page="/common/footer_struts2.jsp" />
</html>