<jsp:include page="/common/doctype.jsp" />

<%@ page import="gov.nih.nichd.ctdb.common.rs,java.util.Locale" %>
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
		<s:text name="report.CompletedVisitsReport"/>
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
	$("#completedVisitsReportTable").idtTable({
		idtUrl: "<s:property value='#webRoot'/>/response/getCompletedVisitsReportList.action",
		dom: 'Bfrtip',
        columns: [
        	{
                name: 'studyNumber',
                title: '<%=rs.getValue("report.column.label.studyNumber",l)%>',
                parameter: 'studyNumber',
                data: 'studyNumber'
            },
            {
                name: 'primarySiteName',
                title: '<%=rs.getValue("report.column.label.primarySiteName",l)%>',
                parameter: 'primarySiteName',
                data: 'primarySiteName'
            },
            {
                name: 'totalNumSubjects',
                title: '<%=rs.getValue("report.column.label.totalNumSubjects",l)%>',
                parameter: 'totalNumSubjects',
                data: 'totalNumSubjects'
            },
            {
                name: 'numSubjectsDataCollect',
                title: '<%=rs.getValue("report.column.label.subjectsWithCollection",l)%>',
                parameter: 'numSubjectsDataCollect',
                data: 'numSubjectsDataCollect'
            },
            {
                name: 'baselineVisitTotal',
                title: '<%=rs.getValue("report.column.label.baseline",l)%>',
                parameter: 'baselineVisitTotal',
                data: 'baselineVisitTotal'
            },
            {
                name: 'sixMonthsVisitTotal',
                title: '<%=rs.getValue("report.column.label.sixMonths",l)%>',
                parameter: 'sixMonthsVisitTotal',
                data: 'sixMonthsVisitTotal'
            }, 
            {
                name: 'twelveMonthsVisitTotal',
                title: '<%=rs.getValue("report.column.label.twelveMonths",l)%>',
                parameter: 'twelveMonthsVisitTotal',
                data: 'twelveMonthsVisitTotal'
            }, 
            {
                name: 'eighteenMonthsVisitTotal',
                title: '<%=rs.getValue("report.column.label.eighteenMonths",l)%>',
                parameter: 'eighteenMonthsVisitTotal',
                data: 'eighteenMonthsVisitTotal'
            },
            {
                name: 'twentyFourMonthsVisitTotal',
                title: '<%=rs.getValue("report.column.label.twentyfourMonths",l)%>',
                parameter: 'twentyFourMonthsVisitTotal',
                data: 'twentyFourMonthsVisitTotal'
            }, 
            {
                name: 'thirtyMonthsVisitTotal',
                title: '<%=rs.getValue("report.column.label.thirtyMonths",l)%>',
                parameter: 'thirtyMonthsVisitTotal',
                data: 'thirtyMonthsVisitTotal'
            }, 
            {
                name: 'thirtySixMonthsVisitTotal',
                title: '<%=rs.getValue("report.column.label.thirtysixMonths",l)%>',
                parameter: 'thirtySixMonthsVisitTotal',
                data: 'thirtySixMonthsVisitTotal'
            }, 
            {
                name: 'fortyTwoMonthsVisitTotal',
                title: '<%=rs.getValue("report.column.label.fortytwoMonths",l)%>',
                parameter: 'fortyTwoMonthsVisitTotal',
                data: 'fortyTwoMonthsVisitTotal'
            },
            {
                name: 'fortyEightMonthsVisitTotal',
                title: '<%=rs.getValue("report.column.label.fortyeightMonths",l)%>',
                parameter: 'fortyEightMonthsVisitTotal',
                data: 'fortyEightMonthsVisitTotal'
            },
            {
                name: 'fiftyFourMonthsVisitTotal',
                title: '<%=rs.getValue("report.column.label.fiftyfourMonths",l)%>',
                parameter: 'fiftyFourMonthsVisitTotal',
                data: 'fiftyFourMonthsVisitTotal'
            },
            {
                name: 'sixtyMonthsVisitTotal',
                title: '<%=rs.getValue("report.column.label.sixtyMonths",l)%>',
                parameter: 'sixtyMonthsVisitTotal',
                data: 'sixtyMonthsVisitTotal'
            },

        ],
        buttons: [
			{
				extend: "collection",
				title: 'Completed_Visits_'+now,
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
	
	<p><s:text name="report.completedVisitsReport.instruction"/></p>
	<br>

	<div id="completedVisitsReportContainer" class="idtTableContainer brics">
		<table id="completedVisitsReportTable" class="table table-striped table-bordered" width="100%">
		</table>
	</div>	
	
	<jsp:include page="/common/footer_struts2.jsp" />
</html>