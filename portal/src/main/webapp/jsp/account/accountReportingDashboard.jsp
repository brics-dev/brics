<%@include file="/common/taglibs.jsp"%>
<title>Account Reporting Dashboard</title>
<div class="ibisMessaging-flashContainer ibisMessaging-dialogContainer"></div>


<div class="clear-float">
	<div class="clear-float">
		<h1 class="float-left">Account Management</h1>
	</div>
</div>

<!-- begin .border-wrapper -->
<div class="border-wrapper">

	<jsp:include page="../navigation/userManagementNavigation.jsp" />
	
	<!--begin #center-content -->
	<div id="main-content">
		<h2>Account Reporting Dashboard</h2>	
		<form autocomplete="off">
			<input autocomplete="false" name="hidden" type="text" style="display:none;">
			<div id="AccountReportingLogListContainer" class="idtTableContainer">
				<div id="dialog"></div>
				<input type="datetime" id="accountReportingFilterStartDate" name="accountReportingFilterStartDate" placeholder="report start date" class="idtDateFilter" 
				style="vertical-align: top" />
				<input type="datetime" id="accountReportingFilterEndDate" name="accountReportingFilterEndDate" placeholder="report end date" class="idtDateFilter" 
				style="vertical-align: top"/>
				<table id="accountsReportingListTable" class="table table-striped table-bordered" width="100%"></table>
			</div>
		</form>
		
		<form action='accountReportsAction!downloadReport.action' method='POST' id='theForm' autocomplete="off">	
			<input type="hidden" id="filterStartDate" name="filterStartDate" />
			<input type="hidden" id="filterEndDate" name="filterEndDate" />
			<input type="hidden" id="filterReportType" name="filterReportType" />
		</form>
	</div>
	<!-- end of #center-content -->
</div>

<script type="text/javascript" src="/portal/js/account.js"></script>
<script type="text/javascript">
	setNavigation({"bodyClass":"primary", "navigationLinkID":"userManagementModuleLink", "subnavigationLinkID":"managingAccountsLink", "tertiaryLinkID":"accountReportingLink"});
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
   var columnsWidth = ["20%", "20%", "30%", "10%", "10%", "10%"];
	$(document).ready(function(){
		$('#accountsReportingListTable').idtTable({
			idtUrl: "<s:url value='/accountReviewer/accountReportsAction!getAccountReportingLogs.action' />",
			autoWidth: false,
			dom: "fBtrip",
			order: [[3, "desc" ]],
			"columns" : [{
				"data" : "userName",
				"title" : "Username",
				"name" : "Username",
				"parameter": "userName",
				"width": "15%"
			},{
				"data" : "name",
				"title" : "Name",
				"name" : "Name",
				"parameter": "name",
				"width": "20%"
			},{
				"data" : "email",
				"title" : "Email",
				"name" : "Email",
				"parameter": "email",
				"width": "25%"
			},{
				"data" : "requestSubmitDate",
				"title" : "Submitted Date",
				"name" : "Submitted Date",
				"parameter": "requestSubmitDate",
				"width": "10%"
			},{
				"data" : "accountType",
				"title" : "Account Type",
				"name" : "Account Type",
				"parameter": "accountType",
				"width": "10%"
			},{
				"data" : "requestType",
				"title" : "Request Type",
				"name" : "Request Type",
				"parameter": "requestType",
				"width": "20%"
			}],
			filters: [
                {
                    type: 'select',
                    name: 'All',
                    columnIndex: 5,
                    options: [
                        {
                            value: 'New',
                            label: 'New Account Requests',
                        },
                        {
                            value: 'Approved',
                            label: 'Approved Account Requests'
                        },
                        {
                            value: 'Rejected',
                            label: 'Rejected Account Requests'
                        },
                        {
                            value: 'Awaiting Documentation',
                            label: 'Awaiting Documents Requests'
                        },
                        {
                            value: 'Pending Approval',
                            label: 'Pending Approval Requests'
                        },
               
                    ],
                }],
                buttons: [
    				{
    					extend: "collection",
    					title: 'Account_Reporting_Export_' + now,
    					buttons: [
    						{
    							extend: 'csv',
    							text: 'CSV',
    							className: 'btn btn-xs btn-primary p-5 m-0 width-35 assets-export-btn  buttons-csv',
    							extension: '.csv',
    							name: 'csv',
    							exportOptions: {
    								columns: ':visible',
    								orthogonal: 'export'
    							},
    							enabled: true,
    							action: IdtActions.exportAction()
    							
    						},
    						{
    		                    extend: 'excel',
    		                    text: 'Excel',
    		                    className: 'btn btn-xs btn-primary p-5 m-0 width-35 assets-export-btn  buttons-excel',
    		                    title: 'export_filename',
    		                    extension: '.xlsx',
    		                    exportOptions: {
    		                    	columns: ':visible',
    		                    	orthogonal: 'export'
    		                    },
    		                    enabled: true,
    		                    action: IdtActions.exportAction()
    							
    						},
    						{
    							extend: 'pdf',
    							text: 'PDF',
    							className: 'btn btn-xs btn-primary p-5 m-0 width-35 assets-export-btn buttons-pdf',
    							extension: '.pdf',
    							name: 'pdf',
    							pageSize : 'LEGAL',
    							exportOptions: {
    								columns: ':visible',
    								orthogonal: 'export'
    							},
    							enabled: true,
    							orientation: 'landscape',
    							action: IdtActions.exportAction(),
    				            customize: IdtActions.pdfCustomizer(columnsWidth)						
    						},
    						{
    							text: 'Report',
    							className: 'btn btn-xs btn-primary p-5 m-0 width-35',
    							exportOptions: {
    								columns: ':visible',
    								orthogonal: 'export'
    							},
    							enabled: true,
    							orientation: 'landscape',
    							action: function (e, dt, node, config )	{
    								var theForm = document.forms['theForm'];
    								$("#filterStartDate").val($("#accountReportingFilterStartDate").val());
    								$("#filterEndDate").val($("#accountReportingFilterEndDate").val());
    								$("#filterReportType").val( $("#All_Account_Requests").val());
    								theForm.submit();
    							}			
    						}	
    					]
    				}
    			]
			})
			
 $.fn.dataTable.ext.search.push(
        function (settings, data, dataIndex) {
            var min = $('#accountReportingFilterStartDate').datepicker("getDate");
            var max = $('#accountReportingFilterEndDate').datepicker("getDate");
            var date = new Date(data[3]);
            startDate = new Date(date.getFullYear(), date.getMonth(), date.getDate() + 1, 0, 0, 0);
            if (min == null && max == null) { return true; }
            if (min == null && startDate <= max) { return true;}
            if(max == null && startDate >= min) {return true;}
            if (startDate <= max && startDate >= min) { return true; }
            return false;
        }
        );

$("#accountReportingFilterStartDate, #accountReportingFilterEndDate")
		.prependTo($(".dt-buttons"))
		.datepicker({
	dateFormat: "yy-mm-dd", // this corresponds to ex: 2012-4-30
	changeMonth: true,
	changeYear: true,
	minDate: "2019-06-22",
	showButtonPanel: true
}).change(function() {
	var oTable = $("#accountsReportingListTable").idtApi("getTableApi");
	var options = $("#accountsReportingListTable").idtApi('getOptions');
	
	// test for end date before start date
	var filterStartDate = $("#accountReportingFilterStartDate").datepicker("getDate");
	var filterEndDate = $("#accountReportingFilterEndDate").datepicker("getDate");
	if (filterStartDate != null && filterEndDate != null && filterStartDate.getTime() > filterEndDate.getTime()) {
		$.ibisMessaging("dialog", "warning", "The end date cannot be before the start date");
		$("#accountReportingFilterEndDate").val("");
	}
	else {
		options.filterData = {
			filter: [{
				name: "requestType",
				value: $("#filterRequestType").val()
			},
			{
				name: "startDate",
				value: $("#accountReportingFilterStartDate").val()
			},
			{
				name: "endDate",
				value: $("#accountReportingFilterEndDate").val()
			}]
		};
		oTable.clearPipeline().draw();
	}
});
	});

</script>