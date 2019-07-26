<%@include file="/common/taglibs.jsp"%>

<title>Account List</title>

<div class="clear-float">
	<h1 class="float-left">Account Management</h1>
</div>

<!-- begin .border-wrapper -->
<div class="border-wrapper">
	
	<jsp:include page="../navigation/userManagementNavigation.jsp" />
	
	<!--begin #center-content -->
	<div id="main-content">
		<h2>Account List</h2>
		<div class="clear-float">
			<form>
				<div id="accountListContainer" class="idtTableContainer">
					<div id="dialog"></div>
					<table id="accountListTable" class="table table-striped table-bordered" width="100%"></table>
				</div>
			</form>
		</div>
		<!-- end of #main-content -->
	</div>
</div>
<!-- end of .border-wrapper -->

<!-- <script type="text/javascript" src="/portal/js/search/accountSearch.js"></script> -->
<script type="text/javascript">
	setNavigation({"bodyClass":"primary", "navigationLinkID":"userManagementModuleLink", "subnavigationLinkID":"managingUsersLink", "tertiaryLinkID":"accountList"});

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
		$('#accountListTable').idtTable({
			idtUrl: "<s:url value='/accountAdmin/accountAction!getAccountLists.action' />",
			"autoWidth": false,
			"dom": "Bfrtip",
			"columns": [
				{
					"data": "userName",
					"title": "USERNAME",
					"name": "USERNAME",
					"parameter": "userName",
					"width": "17%"

				},
				{
					"data": "userFullName",
					"title": "NAME",
					"name": "NAME",
					"parameter": "userFullName",
					"width": "15%",
					"render": function ( data, type, full) { /*for returning row by searching user full name with inputing following patterns: last, first or last first or first last */
					 	if ( type === 'filter'  ) {
					 		var lastFirstNameWithComma = data;
					 		var nameArr = data.split(',');
					    	var lastName = nameArr[0];
					    	var firstName = nameArr[1];
					  		var lastFirstNameWithSpace = $.trim(lastName) + " " + $.trim(firstName);
					  		var firstLastName = $.trim(firstName) + " " + $.trim(lastName);
					  		
					  		var rtnVal = lastFirstNameWithComma + " " + lastFirstNameWithSpace + " " + firstLastName;
					  		//console.log("rtnVal: "+rtnVal);
					      	return rtnVal;
					 	} else {
					  		return data;
					 	}
					}
				}, 
				{
					"data": "userEmail",
					"title": "EMAIL",
					"name": "EMAIL",
					"parameter": "userEmail",
					"width": "15%"
				},
				{
					"data": "affiliatedInstitution",
					"title": "INSTITUTION",
					"name": "INSTITUTION",
					"parameter": "affiliatedInstitution",
					"width": "20%"
				},
				{
					"data": "applicationDate",
					"title": "SUBMITTED",
					"name": "SUBMITTED",
					"parameter": "applicationDate"
				},
				{
					"data": "lastUpdatedDate",
					"title": "LAST UPDATED",
					"name": "LAST UPDATED",
					"parameter": "lastUpdatedDate"
				},
				{
					"data": "accountStatusName",
					"title": "STATUS",
					"name": "STATUS",
					"parameter": "accountStatusName"
				},
				{
					"data": "adminNote",
					"title": "LAST ADMINISTRATIVE NOTE",
					"name": "adminNote",
					"parameter": "adminNote",
					"visible": false,
					"searchable": true
				}
			],
			filters: [
				{
					type: "select",
					name: 'Status: All',
					columnIndex: 6,
					bRegex: true,
					options: [
						{
							value: "active",
							label: "Status: Active"
						},
						{
							value: "requested",
							label: "Status: Requested"
						},
						{
							value: "change requested",
							label: "Status: Change Requested"
						},
						{
							value: "pending",
							label: "Status: Pending"
						},
						{
							value: "denied",
							label: "Status: Denied"
						},
						{
							value: "inactive",
							label: "Status: Inactive"
						},
						{
							value: "withdrawn",
							label: "Status: Withdrawn"
						},
						{
							value: "locked",
							label: "Status: Locked"
						}
					]
				}
			],
			buttons: [
				{
					extend: "collection",
					title: '_Account_List_' + now,
					buttons: [
						{
							extend: 'csv',
							text: 'csv',
							className: 'btn btn-xs btn-primary p-5 m-0 width-35 assets-export-btn  buttons-csv',
							extension: '.csv',
							name: 'csv',
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

	})
	
</script>
