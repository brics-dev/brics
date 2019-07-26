<%-- This jsp should be able to be placed on any page and provide the access report table.
It contains the javascript for controlling the table as well as any front end facets. --%>
<%@include file="/common/taglibs.jsp"%>
<div id="accessReportTable">Data Loading. Please Wait.</div>

<script type="text/javascript">
	// The js in this file is based off studySearch.js
	
	// Load a search at the start
	$('document').ready(function() {
		accessReportSearch();
	});

	var accessReportPagination = new Object();
	accessReportPagination.sort = "queueDate";
	accessReportPagination.ascending = false;
	
	// Performs an ajax search and loads the resulting tbale into accessReportTable
	function accessReportSearch()
	{
		//$.fancybox.showActivity();
		
		var action = "accessRecordDownloadAction!searchReportsPage.ajax";
			$.ajax(action, {
				"type":		"POST",
				"async":	true,
				"data":		{
								"sort" : this.accessReportPagination.sort,
								"ascending" : this.accessReportPagination.ascending,
								"key" : $("#accessRecordKey").val(),
								"daysOld" : $("input:radio[name ='accessReportAge']:checked").val()
				},
				"success": 	function(data) {
								$("#accessReportTable").html(data);
								$("#accessReportHeaders").css("width", $("#accessReportContents").css("width"));
		            			$("#accessReportTable").find("script").each(function(i) {
		            				if ($(this).attr("type") != "text/json") {
		                				eval($(this).text());
		            				}
		            			});
		            			//$.fancybox.hideActivity();
		            			buildDataTables();
				},
				"error":	function(data) {
								//$.fancybox.hideActivity();
				}
			});
	}
	
	// Function called when user clicks a table head to sort a column.
	function accessReportSetSort(sortIn) {
		//acessReportResetPagination();			// There is no pagination (yet)
		if (sortIn == accessReportPagination.sort) {
			accessReportPagination.ascending = !accessReportPagination.ascending;
		}
		else {
			accessReportPagination.sort = sortIn;
			accessReportPagination.ascending = false;
		}
		accessReportSearch();
	};
	
	function accessReportResetSort() {
		accessReportPagination.sort = "queueDate";
		accessReportPagination.ascending = false;
	}
</script>