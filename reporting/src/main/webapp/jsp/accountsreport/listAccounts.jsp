<title>View Reports</title>
<%@include file="/common/taglibs.jsp"%>

<!-- begin .border-wrapper -->
<div class="border-wrapper wide">
	<jsp:include page="../navigation/reportNavigation.jsp" />
	<h1 class="float-left">Reports</h1>
	<div id="main-content">
		<div class="clear-float">
			<table>

				<tr height="30px">
					<td colspan="3"><h2>Run a report</h2>
					</td>
				</tr>
				<tr height="30px" >
					<td colspan="3">The reporting module allows you to view and download metadata associated with studies and user accounts in the system. The table below shows a subset of the available metadata for a report. The downloaded report will provide additional metadata that you can view, sort, and filter on. 
					You can download the report as a XLS, CSV, or PDF.&nbsp;<s:a href="/reporting/viewstudiesreports/studiesReportingListAction!howToReporting.action">Learn how to use the reporting module.</s:a>
					</td>
				</tr>
				
				
				<tr height="20px">
					<td>
					</td>
					<td>
					</td>
				
				
				</tr>
				<tr>
					<td width= "50%">		
						
							1. Choose a report to run

								<s:select name="reportTypes" list="reportTypes" listKey="reportName" listValue="reportType" 
					label="Report Type" onchange="checkReportType(this)" selectedIndex="2"/>
						
					</td>

				</tr>
			</table>

		</div>
		<br></br>
		<br></br>

		<h5 style="font-size: 10px;">2. Download report  &nbsp;&nbsp; <a href="/reporting/viewaccountsreports/accountsReportingListAction!exportXLS.action">Download as XLS</a> | <a href="/reporting/viewaccountsreports/accountsReportingListAction!exportCSV.action">Download as CSV</a> | <a href="/reporting/viewaccountsreports/accountsReportingListAction!exportPDF.action">Download as PDF</a> </h5>
		<br></br>
		<br></br>
		<div><p>This report shows all system users and their associated roles.</p></div>
		
		<div id="accountsListTable" class="idtTableContainer">
	      <div id="dialog"></div>
	      <table id="accountsListTableTable" class="table table-striped table-bordered" width="100%"></table>
		</div>
		
<script type="text/javascript">

	function checkReportType(select) {
		var reportType = select.options[select.selectedIndex].text;
		reportType = reportType.toLowerCase();
		window.location.href='/reporting/view'+reportType+'reports/'+reportType+'ReportingListAction!list.action';
	}


</script>		
	<script type="text/javascript">
	            $(document).ready(function () {
	            	
	            	document.getElementById("reportTypes").selectedIndex = 1;
	            	
	                $('#accountsListTableTable').idtTable({
	                    idtUrl: "<s:url value='/viewaccountsreports/accountsReportingListAction!getAccountsTableList.action' />",
			          	filterData: {

							
			          	},
	                    pageLength: 15,
	                    //dom: "Bfrtip", // for displaying export formats like csv, pdf etc. and print buttons
	                    "autoWidth": false,
	                    "columns": [
	
	                        {
	                            "data": "Firstname",
	                            "title": "FIRST NAME",
	                            "name": "FIRST NAME",
	                            "parameter": "firstname",
	                            "searchable": true,
	                            "orderable": true,
	                            "width": '40%'
	                        },

	                        {
	                            "data": "lastname",
	                            "title": "LAST NAME",
	                            "name": "LAST NAME",
	                            "parameter": "lastname",
	                            "searchable": true,
	                            "orderable": true,
	                            "width": '40%'
	                        },

	                        {
	                            "data": "accountStatus",
	                            "title": "ACCOUNT STATUS",
	                            "name": "ACCOUNT STATUS",
	                            "parameter": "accountStatus",
	                            "searchable": true,
	                            "orderable": true,
	                            "width": '40%'
	                        },

	                        {
	                            "data": "affiliatedInstitution",
	                            "title": "INSTITUTION",
	                            "name": "INSTITUTION",
	                            "parameter": "affiliatedInstitution",
	                            "searchable": true,
	                            "orderable": true,
	                            "width": '40%'
	                        },

							{
	                            "data": "accountRole",
	                            "title": "ACCOUNT ROLE",
	                            "name": "ACCOUNT ROLE",
	                            "parameter": "accountRole",
	                            "searchable": true,
	                            "orderable": true,
	                            "width": '40%'
	                        }
	                    ]

	                })
	            })

	var searchData = {};

	

		            
	</script>
	</div>
</div>
<!-- end of .border-wrapper -->


<script type="text/javascript">

	
	// Load a search at the start
	$('document').ready(function() {

		
	});


</script>
<script type="text/javascript">
 	setNavigation({"bodyClass":"primary", "navigationLinkID":"reportingModuleLink", "subnavigationLinkID":"studyToolLink", "tertiaryLinkID":"browseStudyLink"});
</script>