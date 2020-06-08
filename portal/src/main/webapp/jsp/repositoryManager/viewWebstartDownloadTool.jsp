<%@include file="/common/taglibs.jsp"%>
<title>Download Queue</title>
<!-- begin .border-wrapper -->
<div class="border-wrapper wide">
	<jsp:include page="../navigation/dataRepositoryNavigation.jsp" />
	<s:if test="!inAdmin">
		<h1 class="float-left">Data Repository</h1>
	</s:if>
	<div style="clear:both"></div>

	<!--begin #center-content -->
	<div id="main-content">
		<div class="clear-float">

			<h2>Download Tool - Java Web Start Application</h2>
			<p>If you do not have Java Runtime Environment (version 7-8) installed, you can use the OpenJDK version.  
			Please reach out to a member of your Operations Support Team for instructions on how to perform this.</p>
		
			<!-- This file must be in src/main/webapp/downloadTool_manual.docx -->
			<p><a href="<s:url value="/downloadTool_manual.docx"/>">Documentation: Download Tool</a></p>
			
			<h2>Steps to Run the Download Tool:</h2>
			<ol>
				<li>Click Launch Download Tool</li>
				<li>Select Download Location
				<ul>
						<li>Click the Browser Button </li>
						<li>Navigate to the location on your computer of the working directory where you want the file to be downloaded.
							Select the folder, and then click Open</li>
					</ul>
				</li>
				<li>Select the files you want to download by clicking the check box next to the file name</li>
				<li>Click Start Download
					<ul>
						<li><strong>Note:</strong> Screen will update as file(s) are being downloaded. If Download is
							successful, the Status will be designated as Completed</li>
					</ul>
				</li>
				<li>This allows immediate access to data you have downloaded</li>
			</ol>
			<br>
			<p style="border:3px; border-style:solid; border-color:black; padding: 1em;"><b>Warning: Files older than 30 days will automatically get deleted for security and performance reason. It is recommended that you save your queries in the Query Tool and ensure data-sets are frequently downloaded.</b></p>
			
			
			<div class="action-button" style="float:none; display:inline-block;"><ndar:actionLink action="baseAction!launch.action" value="Launch Download Tool" paramName="webstart"
						paramValue="downloadTool" /></div>
		</div>
		<div class="downloadToolClient"></div>
	</div>
	
</div>
	<!-- end of .border-wrapper -->
	
	<script type="text/javascript">
	setNavigation({"bodyClass":"primary", "navigationLinkID":"dataRepositoryModuleLink", "subnavigationLinkID":"downloadToolLink", "tertiaryLinkID":"downloadQueueLink"});

	
	//Contains functions for a study search and defines studyPagination Object
	
	//study Search Object
	//This is a global object containing the properties needed for search
	var pagination = new Object();
	pagination.page = 1;
	pagination.pageSize = 10;
	pagination.sort = "dataset";
	pagination.ascending = false;
	pagination.namespace = "";
	pagination.selectedElementIds = new Array();
	
	//Additional properties ownerValue and filterValue are also required for search and are obtained
	//through form elements on the page.
	
	//This value submits a search. It takes no arguments, but uses global javascript variables on this page
	//as well as reading reading values from the elements on the page
	//These variables are set at the top of this block and changed by various javascript calls.
	//Several other functions call this function after altering one of these variables to perform a search
	function search() {
	
		var action = this.pagination.namespace + "downloadQueueAction!search.ajax";
		
		// Ajax call your search
		$.fancybox.showActivity();
		$.ajax(action , {
			"type": 	"POST",
			"data": 	{"page" : this.pagination.page,
						"pageSize" : this.pagination.pageSize,
						"sort" : this.pagination.sort,
						"ascending" : this.pagination.ascending},
			"success": 	function(data) {
							$("#resultsId").html(data);
				            $("#resultsId").find("script").each(function(i) {
				                eval($(this).text());
				            });
				            $.fancybox.hideActivity();
						},
			"error":	function(data) {
							$.fancybox.hideActivity();
						}
		});
	}
	
	//This function casues the result to jump to a page given by the page text field
	//This text field is defined in elementList.jsp
	//This function also checks to make sure the given page input is valid
	function checkPageField(maxPage) {
		var desiredPage = document.getElementById("paginationJump").value;
		maxPage = Math.ceil(maxPage);
		if (!isNaN(desiredPage)) {
			if (desiredPage <= maxPage && desiredPage > 0 && (Math.ceil(desiredPage) / desiredPage == 1)) {
				pagination.page = desiredPage;
				search();
			}
			else {
				document.getElementById("paginationJump").value = pagination.page;
			}
			
		}
		else {
			document.getElementById("paginationJump").value = pagination.page;
		}
	}
	
	//Sets the global pagination values back to their default values
	function resetPagination() {
		pagination.page = 1;
		pagination.pageSize = 10;
	}
	
	function resetSort() {
		pagination.sort = "title";
		pagination.ascending = false;
	}
	
	//Function called when user clicks a table head to sort a column.
	function setSort(sortIn) {
		resetPagination();
		if (sortIn == pagination.sort) {
			pagination.ascending = !pagination.ascending;
		}
		else {
			pagination.sort = sortIn;
		}
		search();
	};
	
	function masterCheckboxClicked(box) {
 		var checkboxes = $("input[name=checkedItem]");
 		if (box.checked)
 		{
 			$.each(checkboxes, function() {
 				if (!this.checked)
 				{
 					this.checked = true;
 					checkedBoxes(this);
 				}
 			});
 		}
 		else
 		{
 			$.each(checkboxes, function() {
 				if (this.checked)
 				{
 					this.checked = false;
 					checkedBoxes(this);
 				}
 			});
 		}
 	}
	
	// Tracks checked elements over multiple pages of results
	// Only relevent when displaying mapped results in the editDS lightbox
 	function checkedBoxes(box) {
	
 		if (box.checked)
 		{
 			pagination.selectedElementIds.push(box.value);
 		}
 		else
 		{
 			// when a box is unchecked, the master checkbox needs to be unchecked
 			$("#masterCheckbox")[0].checked = false;
			
 			for(var i=0; i<pagination.selectedElementIds.length; i++)
 			{
 				if (pagination.selectedElementIds[i] == box.value)
 				{
 					pagination.selectedElementIds.splice(i, 1);
 					i--;
 				}
 			}
 		}
 	}
	
 	function getCheckedElementIds() 
 	{
 	    var returnList = "";
	    
 		for (i in pagination.selectedElementIds) 
 		{
 			returnList += pagination.selectedElementIds[i] + ",";
 		}
		
 		return returnList;
 	}
 	
 	function deleteSelected() 
 	{
		var checkedIds = getCheckedElementIds();
		
 	 	// If there are no Ids to add, then simply return
 	 	// The lightbox will still be closed
 	 	if (checkedIds == "") 
 	 	{
 	 		return;
 	 	}
 		
		var action = this.pagination.namespace + "downloadQueueAction!delete.ajax";
		
		// Ajax call your search
		$.fancybox.showActivity();
		
 	 	$.ajax(action , {
			"type": 	"POST",
			"data": 	{"selectedIds" : this.getCheckedElementIds()},
			"success": 	function(data) {
							search(); 
						},
			"error":	function(data) {
							$.fancybox.hideActivity();
						}
 	 	});
 	}
</script>
</body>
</html>