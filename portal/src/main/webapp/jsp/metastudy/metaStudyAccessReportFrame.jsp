<%-- This jsp should be able to be placed on any page and provide the access report table.
It contains the javascript for controlling the table as well as any front end facets. --%>
<%@include file="/common/taglibs.jsp"%>
<div id="metaStudyAccessReportTable">Data Loading. Please Wait.</div>

<script type="text/javascript">
//The js in this file is based off studySearch.js

// Load a search at the start
$('document').ready(function() {
	accessReportSearch();
});

// Performs an ajax search and loads the resulting tbale into accessReportTable
function accessReportSearch(){
	//$.fancybox.showActivity();
	var metaStudyId = $('#metaStudyId').val();
	var action = "metaStudyAccessRecordDownloadAction!searchReports.ajax";
		$.ajax(action, {
			"type":		"POST",
			"async":	true,
			"data":		{
							"metaStudyId" : metaStudyId
			},
			"success": 	function(data) {
							$("#metaStudyAccessReportTable").html(data);
	            			$("#metaStudyAccessReportTable").find("script").each(function(i) {
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

</script>