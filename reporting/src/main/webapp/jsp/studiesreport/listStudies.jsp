<title>View Reports</title>
<%@include file="/common/taglibs.jsp"%>
<!-- begin .border-wrapper -->
<div class="border-wrapper wide">
	<jsp:include page="../navigation/reportNavigation.jsp" />
	<h1 class="float-left">Reports</h1>
	<div style="clear:both"></div>
	
	<!--begin #center-content -->
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
									label="Report Type" onchange="checkReportType(this)"/>
							
					</td>

					<td width= "50%" colspan="2">
							2. Choose filters to apply

					<td>



				</tr>
				<tr height="30px">
					<td width= "50%">				
					</td>
					<td width= "50%" colspan="2">
							Study Start/End Date
					<td>
				</tr>
				<tr height="10px">
					<td colspan="3">				
					</td>


				</tr>
				<tr height="30px">
					<td width= "50%">				
					</td>

					<td width= "50%" colspan="2">
							Start&nbsp;&nbsp;<s:textfield cssClass="dateField" name="startDate" size="20" editable="false"
						maxlength="50" id="startDate" />
					<td>

				</tr>

				<tr height="30px">
					<td width= "50%">				
					</td>

					<td colspan="2">
							End&nbsp;&nbsp;&nbsp;<s:textfield cssClass="dateField" name="endDate" size="20"
						maxlength="50" id="endDate" />
					<td>

				</tr>
			</table>
		
		</div>


		<div id="someId" class="ClassName">
				<div class="formrow_1">
				
						<input type="button" id="patientFormViewResetBtn"
						value="Clear filters" title="Click to clear fields"
						alt="Reset" onclick="resetSearchTable(this)" /> 
						
						<input type="button"
						id="patientPreviousSearchBtn"
						value="Apply Filters" title="Click to search"
						alt="Search" onclick="submitSearchTable(this)" />
	
				</div>

		</div>			

		<br></br>
		<br></br>

		<h5 style="font-size: 10px;">3. Download report  &nbsp;&nbsp; <a href="/reporting/viewstudiesreports/studiesReportingListAction!exportXLS.action">Download as XLS</a> | <a href="/reporting/viewstudiesreports/studiesReportingListAction!exportCSV.action">Download as CSV</a> | <a href="/reporting/viewstudiesreports/studiesReportingListAction!exportPDF.action">Download as PDF</a> </h5>
		<br></br>
		<br></br>
		<div>
			<div style="width: 50%" class="float-left">
				<h3><b>Principal Investigator Counts</b></h3>
				<ul class="counts-list">
					<li>Total Primary Principal Investigators: ${primaryPrincipalInvestigatorCount}</li>
					<li>Total Principal Investigators: ${principallInvestigatorCount}</li>
					<li>Total Associate Principal Investigators: ${associatePrincipalInvestigatorCount}</li>
					<li>Sum:  ${totalStudyInvestigatorCount}</li>
				</ul>
			</div>
			<div style="width: 50%" class="float-left">
				<h3><b>Study Counts</b></h3>
				<ul class="counts-list">
					<li>Total Private Studies: ${privateStudyCount}</li>
					<li>Total Public Studies: ${publicStudyCount}</li>
					<li>Total Requested Studies: ${requestedStudyCount}</li>
					<li>Total Rejected Studies: ${rejectedStudyCount}</li>
					<li>Total Studies: ${totalStudyCount}</li>
				</ul>
			</div>
		</div>
		<div style="clear: both;"></div>
		<br></br>
		<br></br>
		<div class="float-left"><p>This report shows all studies in the system and select associated study metadata.</p></div>

		<div id="studyListTable" class="idtTableContainer">
	      <div id="dialog"></div>
	      <table id="studyListTableTable" class="table table-striped table-bordered" width="100%"></table>
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
	            	$( "#startDate" ).datepicker({ dateFormat: 'yy-mm-dd' });
	            	$( "#endDate" ).datepicker({ dateFormat: 'yy-mm-dd' });
	            	
	                $('#studyListTableTable').idtTable({
	                    idtUrl: "<s:url value='/viewstudiesreports/studiesReportingListAction!getStudyTableList.action' />",
			          	filterData: {
							startDate: $("#startDate").val(),
							endDate: $("#endDate").val(),
							report: $("#inReport").val()
							
			          	},
	                    pageLength: 15,
	                    //serverSide: true,

	                    //dom: "Bfrtip", // for displaying export formats like csv, pdf etc. and print buttons
	                    "autoWidth": false,
	                    "columns": [
	
	                        {
	                            "data": "studyAdminLink",
	                            "title": "TITLE",
	                            "name": "TITLE",
	                            "parameter": "studyAdminLink",
	                            "searchable": true,
	                            "orderable": true,
	                            "width": '40%'
	                        },

	                        {
	                            "data": "studyStartDate",
	                            "title": "STUDY STARTED",
	                            "name": "STUDY STARTED",
	                            "parameter": "startDate",
	                            "searchable": true,
	                            "orderable": true,
	                            "width": '10%'
	                        },
							{
	                            "data": "studyEndDate",
	                            "title": "STUDY ENDED",
	                            "name": "STUDY ENDED",
	                            "parameter": "endDate",
	                            "searchable": true,
	                            "orderable": true,
	                            "width": '10%'
	                        },

	                        {
	                            "data": "studykeyword",
	                            "title": "STUDY KEYWORD",
	                            "name": "STUDY KEYWORD",
	                            "parameter": "studykeyword",
	                            "searchable": true,
	                            "orderable": true,
	                            "width": '10%'
	                        },

	                        {
	                            "data": "studyTyp",
	                            "title": "STUDY TYPE",
	                            "name": "STUDY TYPE",
	                            "parameter": "studyTyp",
	                            "searchable": true,
	                            "orderable": true,
	                            "width": '10%'
	                        },

	                        {
	                            "data": "fundSrc",
	                            "title": "FUNDING SOURCE",
	                            "name": "FUNDING SOURCE",
	                            "parameter": "fundSrc",
	                            "searchable": true,
	                            "orderable": true,
	                            "width": '7.5%'
	                        },
	                        
	                        {
	                            "data": "recruitmentStatus",
	                            "title": "RECRUITMENT STATUS",
	                            "name": "RECRUITMENT STATUS",
	                            "parameter": "recruitmentStatus",
	                            "searchable": true,
	                            "orderable": true,
	                            "width": '7.5%'
	                        }	                        


	                    ]

	                })
	            })

	var searchData = {};
	function resetSearchTable(resetButtonID) {
		var resetBtnId = resetButtonID.id;
		var oTable = $("#studyListTableTable").idtApi("getTableApi");
		var options = $("#studyListTableTable").idtApi('getOptions');
			$(':text, :password, :file').val('');
			$("#inReport").prop("selectedIndex", 0);

		options.filterData = {};
		oTable.ajax.reload();
	}
	
	function submitSearchTable(submitButtonID) {
		var submitButtonID = submitButtonID.id;
		var oTable = $("#studyListTableTable").idtApi("getTableApi");
		var options = $("#studyListTableTable").idtApi('getOptions');
		if (submitButtonID == "patientPreviousSearchBtn") {
			var startDate = $("#startDate").val(),
				endDate =  $("#endDate").val(),
				report =  $("#inReport").val()

			searchData = {
					startDate: startDate,
					endDate: endDate,
					report: endDate
			}
			
			options.filterData = searchData;
			oTable.ajax.reload();

		}
	}
		            
	</script>
	</div>
</div>
<!-- end of .border-wrapper -->


<script type="text/javascript">

	
	// Load a search at the start
	$('document').ready(function() {

		$("#downloadReportBtn").fancybox({
				'width': 200,
			   'overlayShow': true,
		});
		
	});


</script>
<script type="text/javascript">
 	setNavigation({"bodyClass":"primary", "navigationLinkID":"reportingModuleLink", "subnavigationLinkID":"studyToolLink", "tertiaryLinkID":"browseStudyLink"});
</script>