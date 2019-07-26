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
		<h2>Account Renewal</h2>	
		<div id="accountsRenewalListContainer" class="idtTableContainer">
			<div id="dialog"></div>
			<table id="accountsRenewalListTable" class="table table-striped table-bordered"></table>
		</div>	
		<div id="emailSetting" class="float-right" ><!-- style="float: right;font-size: 12px;font-weight: bold;" -->
			<a href="javascript:openEmailSetting('ACCOUNT_RENEWAL')">Manage email settings</a>
		</div>
		<div id="automatedReportDashBoardDialog" style="display: none;">
			<div id="automatedReportDashBoard"></div>
		</div>
	</div>
	<!-- end of #center-content -->
</div>

<script type="text/javascript" src="/portal/js/account.js"></script>
<script type="text/javascript">
	setNavigation({"bodyClass":"primary", "navigationLinkID":"userManagementModuleLink", "subnavigationLinkID":"managingAccountsLink", "tertiaryLinkID":"accountsForRenewalLink"});

	$(document).ready(function(){
		$('#accountsRenewalListTable').idtTable({
			idtUrl: "<s:url value='/accountReviewer/accountReportsAction!getAccountsForRenewal.action' />",
			autoWidth: false,
			order: [[3, "asc" ]],
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
				"data" : "expiringStatus",
				"title" : "Expiring Status",
				"name" : "Expiring Status",
				"parameter": "expiringStatus",
			},{
				"data" : "expirationDate",
				"title" : "Expiration Date",
				"name" : "Expiration Date",
				"parameter": "expirationDate"
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
			},{
				"data" : "isExpired",
				"title" : "",
				"parameter" : "isExpired",
				"visible" : false
			},{
				"data" : "isExpiringSoon",
				"title" : "",
				"parameter" : "isActiveExpringSoon",
				"visible" : false
			}
			
			],
			drawCallback:function(settings) {
                var table = $('#accountsRenewalListTable').idtApi('getTableApi');

                var status = $("[id='Expiring status:Active-Expiring Soon']").val();
                if(status == ""){
                	status = "Active-Expiring Soon";
                }
                
                $(table.column(2).header()).text("Expiring Status");
                table.column(2).nodes().each(function(node, index, dt){
                	table.cell(node).data(status);
        		});
			},
			filters: [
                {
                    type: 'select',
                    name: 'Expiring status:Active-Expiring Soon',
                    options: [
                        {
                            value: 'Active-Expired Privilege(s)',
                            label: 'Expiring status:Active- Expired Privilege(s)'
                        }
                    ],
                    test: function (oSettings, aData, iDataIndex, filterData) {
                    	
                   	 var isExpired = aData[6];
                   	 var isExpringSoon = aData[7];
                   	 
                   	 var filterStatus = filterData['Expiring status:Active-Expiring Soon'];
                   	 
                   	 if((filterStatus == "" || filterStatus == "Active-Expiring Soon") && isExpringSoon == "true") {
                   		 return true;
                   	 }
                   	 else if (filterStatus == "Active-Expired Privilege(s)" && isExpired == "true"){
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