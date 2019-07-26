<%@include file="/common/taglibs.jsp"%>
<title>Current Users</title>
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
		<h2>User Log</h2>
		<form autocomplete="off">
			<input autocomplete="false" name="hidden" type="text" style="display:none;">
			<div id="UserLogListContainer" class="idtTableContainer">
				<div id="dialog"></div>
				
				<!-- These filters are moved by js to be inside the table's wrapper after it inits -->
				<select id="filterStatus" name="filterStatus" class="search_init select_filter form-control">
					<option value="">Status: All</option>
					<option value="Active" selected="selected">Status: Active</option>
					<option value="Expired">Status: Expired</option>
				</select>
				<!-- these are disabled because the default status filter is "active" and CRIT-10292 -->
				<input type="datetime" id="filterStartDate" name="filterStartDate" placeholder="login start date" class="idtDateFilter" disabled="disabled" style="cursor: not-allowed" />
				<input type="datetime" id="filterEndDate" name="filterEndDate" placeholder="logout end date" class="idtDateFilter" disabled="disabled" style="cursor: not-allowed" />
				<table id="userLogListTable" class="table table-striped table-bordered" width="100%"></table>
			</div>
		</form>	
	</div>
	<!-- end of #center-content -->
</div>
<!-- end of .border-wrapper -->
<script type="text/javascript">
	$(document).ready(function() {
		$('#userLogListTable').idtTable({
			idtUrl: "<s:url value='/accountAdmin/sessionLogAction!combinedSearchIdt.action' />",
	        serverSide: true,
	        processing: true,
			order: [[ 4, "desc" ]],
			filterData : {
				filter: [{
					name: "Status",
					value: $("#filterStatus").val()
				},
				{
					name: "startDate",
					value: $("#filterStartDate").val()
				},
				{
					name: "endDate",
					value: $("#filterEndDate").val()
				}]
			},
			"columns": [
				{
					data: "account_id",
					title: "",
					name: "account_id",
					parameter: "account.id",
					visible: false,
					searchable: false
				},
				{
					"data": "username",
					"title": "USERNAME",
					"name": "USERNAME",
					"parameter": "username",
					render: function(data, type, row, position) {
						// http://fitbir-portal-local.cit.nih.gov:8080/portal/accountAdmin/
						return '<a href="viewUserAccount!viewUserAccount.action?accountId='+row.account_id+'">' + data + "</a>";
					}
				},
				{
					"data": "fullName",
					"title": "FULL NAME",
					"name": "FULL NAME",
					"parameter": "fullName"
				}, 
				{
					"data": "email",
					"title": "E-MAIL",
					"name": "E-MAIL",
					"parameter": "email",
					render: function(data, type, row, position) {
						return '<a href="mailto:' + data + '">' + data + '</a>';
					}
				},{
					data: "sessionStatus",
					title: "SESSION STATUS",
					name: "SESSION STATUS",
					parameter: "sessionStatus",
					searchable: false
				}, {
					data: "timeIn",
					title: "TIME LOG IN",
					name: "TIME LOG IN",
					parameter: "timeIn",
					searchable: false,
					render: IdtActions.formatDate()
				}, {
					data: "timeOut",
					title: "TIME LOG OUT",
					name: "TIME LOG OUT",
					parameter: "timeOut",
					searchable: false,
					render: IdtActions.formatDate()
				}
			]
		});
		
		$("#filterStartDate, #filterEndDate, #filterStatus")
				.insertAfter($("#userLogListTable_filter"))
				.datepicker({
			dateFormat: "yy-mm-dd", // this corresponds to ex: 2012-4-30
			changeMonth: true,
			changeYear: true,
			yearRange: "1900:+10",
			showButtonPanel: true
		}).change(function() {
			var oTable = $("#userLogListTable").idtApi("getTableApi");
			var options = $("#userLogListTable").idtApi('getOptions');
			
			// disable date pickers if status = "active" and enable otherwise
			if ($("#filterStatus").val() == "Active") {
				$("#filterStartDate, #filterEndDate").prop("disabled", true).css("cursor", "not-allowed");
			}
			else {
				$("#filterStartDate, #filterEndDate").prop("disabled", false).css("cursor", "inherit");
			}
			
			// test for end date before start date
			var filterStartDate = $("#filterStartDate").datepicker("getDate");
			var filterEndDate = $("#filterEndDate").datepicker("getDate");
			if (filterStartDate != null && filterEndDate != null && filterStartDate.getTime() > filterEndDate.getTime()) {
				$.ibisMessaging("dialog", "warning", "The end date cannot be before the start date");
			}
			else {
				options.filterData = {
					filter: [{
						name: "Status",
						value: $("#filterStatus").val()
					},
					{
						name: "startDate",
						value: $("#filterStartDate").val()
					},
					{
						name: "endDate",
						value: $("#filterEndDate").val()
					}]
				};
				oTable.clearPipeline().draw();
			}
		});
	})
</script>
<script type="text/javascript">
	setNavigation({"bodyClass":"primary", "navigationLinkID":"userManagementModuleLink", "subnavigationLinkID":"managingUsersLink", "tertiaryLinkID":"userLog"});
</script>

