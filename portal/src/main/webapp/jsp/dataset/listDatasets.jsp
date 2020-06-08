<%@include file="/common/taglibs.jsp"%>
<title>Manage Datasets</title>

<!-- begin .border-wrapper -->
<div class="border-wrapper wide">
	<jsp:include page="../navigation/dataRepositoryNavigation.jsp" />
	<h1 class="float-left">Data Repository</h1>
	<div style="clear:both"></div>
	
	<!--begin #center-content -->
	<div id="main-content" class="dataReporsitory-content">
		<div class="clear-float">
			<input type="hidden" name="userId" id="userId" value="<s:property value="user.id" />" />
			<h2>Manage Datasets</h2>
			<p>All Datasets associated with the studies within are listed in the Manage Datasets list. 
			Administrator (Admins) can search for a Dataset by keyword or either (1) all Datasets in the system  or 
			(2) only Datasets of your own. Datasets can also be searched by the Dataset Status state, which are 
			Private, Requested Deletion, Requested Sharing, Requested Archive, Shared, Archived, or All. 
			Results are shown in a tabular format to include the following:</p>
			<ul>
				<li><strong>Name</strong> is a short description of the Dataset. It is a hyperlink allowing for
					access into the Dataset
				</li>
				<li><strong>Study</strong> refers to the name of the study the Dataset will be associated with
				</li>
				<li>The <strong>Submitter</strong> is the investigator/researcher contributing the Dataset to the Study
				</li>
				<li>The <strong>Date Submitted</strong> is the date the Dataset is requesting a Status change
				</li>
				<li>The <strong>Status</strong> is the state of the Dataset:
					<ul>
						<li><strong>Private</strong> - All Datasets are private by default until requested to share</li>
						<li><strong>Private - Requested Deletion</strong> - Dataset is in Private status and it has been requested by a user to Delete. Note: Deletions are not recommended and data sets should to be archived first. Deletions are accepted if the data has been resubmitted.</li>
						<li><strong>Private - Requested Share</strong> - Dataset is in Private Status and has been requested/permitted by the user to share with site users. Dataset is accessible to users and cannot be modified.</li>
						<li><strong>Private - Requested Archive</strong> - Dataset is in Private status and has been requested by the user for archiving.</li>
						<li><strong>Shared</strong> - Dataset is now accessible to site users. Dataset cannot by modified.</li>
						<li><strong>Shared - Requested Archive</strong> - Data is in Shared status and has been requested by user for archiving.</li>
						<li><strong>Archived</strong> - Dataset is stored in a catalog location. Data is not visible or accessible to users. Dataset cannot be reverted to Private.</li>
						<li><strong>Deleted</strong> - Dataset has been removed from the repository and is no longer accessible. Please confirm that updated datasets have been resubmitted.</li>
						<li><strong>Error During Load</strong> - Dataset is unable to load into the repository due to errors in the data file. Request assistance from Admin.</li>
						<li><strong>Uploading</strong> - Datafile has been created for the dataset and is currently being loaded into the repository. Request assistance from Admin if dataset is not uploaded by end of day.</li>
						<li><strong>Loading Data</strong> - Dataset has been submitted via Submission Tool. The data is in process of entering into the repository.</li>
					</ul>
				</li>
			</ul>

			<form action="datasetAction!manageBulkDatasets.action" onsubmit="getSelectedOptions()">
				<div id="datasetResultsId"></div>
				
				<div id="data_table_second" class="idtTableContainer">
					<table id="datasetIdtTable" class="" cellspacing="0" width="100%"></table>
				</div>
				<script type="text/javascript">
				$(document).ready(function() {
					$("#datasetIdtTable").idtTable({
						idtUrl: "<s:url value='/studyAdmin/datasetAction!searchIdt.ajax' />",
						idtData: {
							primaryKey : "id"
						},
						pages: 1,
						processing: true,
						serverSide: true,
						length: 15,
						columns : [
							{
								data: "nameLink",
								title: "NAME",
								name: "nameLink",
								parameter: "nameLink"
							},
							{
								data: "studyLink",
								title: "STUDY",
								name: "studyLink",
								parameter: "studyLink"
							},
							{
								data: "submitterFullName",
								title: "SUBMITTER",
								name: "submitterFullName",
								parameter: "submitter"
							},
							{
								data: "owner",
								title: "",
								name: "owner",
								parameter: "owner",
								visible: false
							},
							{
								data: "requestStatus",
								title:"",
								name: "requestStatus",
								parameter: "requestStatus",
								visible: false
							},
							{
								data: "submitDate",
								title: "DATE SUBMITTED",
								name: "submitDate",
								parameter: "submitDate",
								render: IdtActions.formatDate()
							},
							{
								data: "status",
								title: "STATUS",
								name: "status",
								parameter: "datasetStatus"
							}
						],
						fixedHeader : true,
						select : 'multi',
						bFilter: true,
						filters: [
							{
								type: "select",
								columnIndex: 0,
								name: "Status: All",
								defaultValue: 'all',
								options: [
									{
										value: "private",
										label: "Status: Private"
									},
									{
										value: "requestedDeletion",
										label: "Status: Private- Requested Deletion"
									},
									{
										value: "requestedSharing",
										label: "Status: Private- Requested Share"
									},
									{
										value: "requestedArchive",
										label: "Status: Private- Requested Archive"
									},
									{
										value: "shared",
										label: "Status: Shared"
									},
									{
										value: "sharedRequestedArchive",
										label: "Status: Shared- Requested Archive"
									},
									{
										value: "archived",
										label: "Status: Archived"
									},
									{
										value: "deleted",
										label: "Status: Deleted"
									},
									{
										value: "errors",
										label: "Status: Error During Load"
									},
									{
										value: "uploading",
										label: "Status: Uploading"
									},
									{
										value: "loadingData",
										label: "Status: Loading Data"
									}
								]
							},
							{
								type: "select",
								columnIndex: 0,
								name: "Ownership: all",
								defaultValue: 'all',
								options: [
									{
										value: "mine",
										label: "Ownership: mine"
									}
								]
							}
						],
						initComplete : function(settings, json) {
							// disable all rows at beginning 
							$("#datasetIdtTable").idtApi("disableSelection");
						},
						rowCallback : function(row, data, index) {
							var filter = document.getElementById('Status:_All');
							if (filter.value == "" || data.status == "Deleted" || filter.value == "all") {
								var $table = $("#datasetIdtTable");
								var apiRow = $table.idtApi("getApiRow", row);
								$table.idtApi("disableRow",apiRow);
								
							}
							else {
								var $table = $("#datasetIdtTable");
								$table.idtApi("enableRow", $table.idtApi("getApiRow", row));
							}
						},
						"drawCallback" : function(settings) {
							var api = new $.fn.dataTable.Api( settings );
							api.on("select", function(e, dt, type, index) {
								var row = api.row(index);
								updateStatusOptions(row, api);
							})
							.on("deselect", function(e, dt, type, index) {
								var row = api.row(index);
								updateStatusOptions(row, api);
							});
							
							var filter = document.getElementById('Status:_All');
							if (filter.value == "" || filter.value == "all") {
								api.rows().deselect();
							}
						}
					});
				});
				</script>

				<div class="float-left" id="toolTip">
					<select id="datasetStatusSelect" name="datasetStatusSelect" class="large" disabled>
						<option value="">Archive</option>
					</select> 
					
					<div class="button disabled" id="buttonDiv" >
						<input id="accept" type="submit" value="Continue" class="button margin-left" disabled/> 
					</div>
					<input id="selectedDatasets" type="hidden" name="selectedDatasets" value="" />
				</div>
							
			</form>
	</div>
	<!-- end of #main-content -->
</div>
</div>
<!-- end of .border-wrapper -->

<script type="text/javascript" src="/portal/js/search/datasetSearch.js"></script>
<script type="text/javascript">
		setNavigation({"bodyClass":"primary", "navigationLinkID":"dataRepositoryModuleLink", "subnavigationLinkID":"contributeDataToolsLink", "tertiaryLinkID":"datasetListLink"});
/*	
	// Load a search at the start
	$('document').ready(function() {
		var filterParam = getURLParameter("filter");
		if(filterParam != "undefined")
		{
			datasetSetFilter(filterParam);
		}
		else
		{
			datasetSearch();
		}
	}); 
*/	
</script>
