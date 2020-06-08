<%@include file="/common/taglibs.jsp"%>

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
		<h2>Account Requests</h2>	
		<div id="accountsRenewalListContainer" class="idtTableContainer">
			<div id="dialog"></div>
			<table id="accountsRenewalListTable" class="table table-striped table-bordered"></table>
		</div>	
		<div id="emailSetting" class="float-right" ><!-- style="float: right;font-size: 12px;font-weight: bold;" -->
			<a href="javascript:openEmailSetting('ACCOUNT_REQUEST')">Manage email settings</a>
		</div>
		<div id="automatedReportDashBoardDialog" style="display: none;">
			<div id="automatedReportDashBoard"></div>
		</div>
	</div>
	<!-- end of #center-content -->
</div>

<script type="text/javascript" src="/portal/js/account.js"></script>
<script type="text/javascript">
	setNavigation({"bodyClass":"primary", "navigationLinkID":"userManagementModuleLink", "subnavigationLinkID":"managingAccountsLink", "tertiaryLinkID":"accountRequestLink"});

	$(document).ready(function(){
		$('#accountsRenewalListTable').idtTable({
			idtUrl: "<s:url value='/accountReviewer/accountReportsAction!getAccountRequests.action' />",
			order: [[3, "desc"]],
			"columns" : [{
				"data" : "userName",
				"title" : "Username",
				"name" : "Username",
				"parameter": "userName"
			},{
				"data" : "name",
				"title" : "Name",
				"name" : "Name",
				"parameter": "user.fullName"
			},{
				"data" : "status",
				"title" : "Status",
				"name" : "Status",
				"parameter": "status"
			},{
				"data" : "requestSubmitDate",
				"title" : "Submitted Date",
				"name" : "Submitted Date",
				"parameter": "requestSubmitDate"
			},{
				"data" : "lastUpdated",
				"title" : "Last Updated",
				"name" : "Last Updated",
				"parameter": "lastUpdated"
			},{
				"data" : "lastAction",
				"title" : "Last Action",
				"name" : "Last Action",
				"parameter": "lastAction",
				"render": IdtActions.ellipsis(100)
			},
			{
				"data" : "reviewerStatus",
				"title" : "Reviewer Status",
				"name" : "Reviewer Status",
				"parameter": "reviewerStatus",
				"width": "17%",
				"render": function(data, type, row, full) {
					if (!data || data == undefined || data == null || data.length == 0) {
						return "";
					}
					
					var reviewerArr = $.parseJSON(data);
					var result = "<span>";
					
					for (var i = 0; i < reviewerArr.length; i++) {
						if (i > 0) {
							result += "&nbsp;";
						}
						result += "<span title=\"" + reviewerArr[i].fullName + "\">";
						
						if (reviewerArr[i].type == "PARTIAL_APPROVE") {
							result += "<img src=\"/portal/images/icons/checkMark.png\" width=\"20\" height=\"30\" />";
						} else {
							result += "<img src=\"/portal/images/icons/exclamationMark.png\" width=\"10\" height=\"15\" />";
						}
						
						result += reviewerArr[i].initial + "</span>";
					}
					result += "</span>";
					return result;
				}
			}],
			 filters: [
                 {
                     type: 'select',
                     name: 'Status: All',
                     options: [
                         {
                             value: 'Requested',
                             label: 'Status: Requested',
                         },
                         {
                             value: 'Pending',
                             label: 'Status: Pending'
                         },
                         {
                             value: 'Change Requested',
                             label: 'Status: Change Requested'
                         },
                         {
                             value: 'Renewal Requested',
                             label: 'Status: Renewal Requested'
                         }
                     ],
                     test: function (oSettings, aData, iDataIndex, filterData) {
                    	 var currentStatus = aData[2]; 
                    	 var filterStatus = filterData['Status: All']
                    	 
                    	 if(filterStatus == "" || currentStatus == filterStatus) {
                    		 return true;
                    	 } else {
                    		 return false;
                    	 }
                     }
                 }
             ]
		});
	})
	
</script>
