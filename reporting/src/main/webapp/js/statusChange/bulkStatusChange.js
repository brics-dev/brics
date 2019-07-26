//Contains functions for dataElement search and defines dataElementSearchObject

// DataElement Search Object
// This is a global object containing the properties needed for search
var dataElementPagination = new Object();
dataElementPagination.page = 1;
dataElementPagination.pageSize = 10;
dataElementPagination.sort = "title";
dataElementPagination.ascending = false;
dataElementPagination.selectedElementIds = new Array();
var persistIDisplayLength = 25;
var save = false;


function exportDataElements(whichExport) {
	if (whichExport == "REDCap") {
		if (!confirm("REDCap CSV format is approximately 80% in alignment with the REDCap standard. \n" + 
				"Please be aware that further refinement will be required to load into the REDCap system. \n" + 
				"Download time will vary based on the number of data elements to be downloaded. \n" +
				"Click OK if you want to continue exporting.")) {
			return;
			
		} else {
			$("#downloadLinks").toggle("blind", 300);
		}
	}
	
	var searchKey = $("#searchKey").val().trim();
	var exactMatch = $("#exactMatch").is(':checked');

	var mapped = $("#mapped").val();
	var publicArea = $("#public").val();
	var general = $("#general").val();
	var ownerId = $('[name="ownerId"]:checked').val();
	var modifiedDate = $("#modifiedDateList").val();
	
	var searchLocations = $('[name="dataElementLocations"]:checked').map(function() {
	    return this.value;
	}).get();
	
	var diseaseSelection = $('[name="selectedDiseases"]:checked').map(function() {
	    return this.value;
	}).get();

	var populationSelection = $('[name="populationSelection"]:checked').map(function() {
	    return this.value;
	}).get();
	
	var classificationSelection = $('[name="selectedClassifications"]:checked').map(function() {
	    return this.value;
	}).get();
		
	
	var statusValue = $('[name="selectedStatuses"]:checked').map(function() {
		return this.value;
	}).get();

	// Since pdbp and fitbir have different sets of status filters, if there is no status option selected, we pass all 
	// status values to the search so that we don't need to handle the host specific logic on the server side. 
	if (publicArea == "true" && statusValue.length == 0) {
		statusValue = $('[name="selectedStatuses"]').map(function() {
			return this.value;
		}).get();
	}
	
	var elementTypes = $('[name="selectedElementTypes"]:checked').map(function() {
	    return this.value;
	}).get();
	
	var domainSelection = "";
	var subDomainSelection = "";
	var classificationSelection = "";
	
	if( save ) {
	domainSelection = domainArray;
	subDomainSelection = subDomainArray;
	classificationSelection = classificationsArray;
	}
	
	// Verify that a valid search location has been selected
	if(searchLocations == "") {
		alert("You must select at least one field to search.");
		return;
	}
	
	
	var action = "";
	if (whichExport === "XML") {
		action = "dataElementExportAction!xmlExport.action";
	} else if (whichExport === "CSV") {
		action = "dataElementExportAction!csvExport.action";
	} else if (whichExport === "XSD") {
		action = "dataElementExportAction!xsdExport.action";
	} else if (whichExport === "REDCap") {
		action = "dataElementExportAction!csvExport.action?format=redcap";
	}
	
	//add searchLocations and SearchKey to form
	var theForm = document.forms['searchForm'];
	$("#filterSearchKey").val($("#searchKey").val());
	$("#filterExactMatch").val(exactMatch);
	$("#filterDataElementLocations").val(searchLocations);
	$("#filterSelectedDomains").val(domainSelection);
	$("#filterSelectedSubdomains").val(subDomainSelection);
	$("#filterSelectedClassifications").val(classificationSelection);
	// added by J. Liu for additional options
	$("#filterSelectedElementTypes").val(elementTypes);
	$("#filterSelectedStatuses").val(statusValue);
	$("#filterSelectedDiseases").val(diseaseSelection);
	$("#filterPopulationSelection").val(populationSelection);
	
	theForm.method = "post";
	theForm.action = action;

	theForm.submit();

}

// Load the public search interface in the publicData namespace
function loadPublicElementSearch() {
	
	$.ajax("/portal/publicData/searchDataElementAction!loadPublicSearch.ajax", {
		"type":		"POST",
		"async":	false,
		"data":		{"publicArea" : true},
		"success":	function(data) {
			
						$("#publicDataElementDiv").html(data);
						tooltip();
						$('a.lightbox').fancybox();
					}
	});
}


//	Function for filtering by data element status
	function dataElementSetFilter(newFilterId) {
		var oldFilter = $(".dataElementSelectedFilter")[0];
		var newFilter = $("#" + newFilterId, $("#dataElementFilterOptions"))[0];

		var oldFilterId = $(oldFilter).attr('id');
	
		$(oldFilter).removeAttr('class');
		$(oldFilter).attr("href", 'javascript:dataElementSetFilter(' + oldFilterId + ');');
		$(newFilter).attr("class", "inactiveLink dataElementSelectedFilter");
		$(newFilter).removeAttr("href");

		//dataElementResetPagination();
		//dataElementResetSort();
		//dataElementSearch();
	
	}

// This function casues the result to jump to a page given by the page text field
// This text field is defined in elementList.jsp
// This function also checks to make sure the given page input is valid
function dataElementCheckPageField(maxPage) {
	var desiredPage = document.getElementById("dataElementPaginationJump").value;
	maxPage = Math.ceil(maxPage);
	if (!isNaN(desiredPage)) {
		if (desiredPage <= maxPage && desiredPage > 0 && (Math.ceil(desiredPage) / desiredPage == 1)) {
			dataElementPagination.page = desiredPage;
			//dataElementSearch();
		}
		else {
			document.getElementById("dataElementPaginationJump").value = dataElementPagination.page;
		}
		
	}
	else {
		document.getElementById("dataElementPaginationJump").value = dataElementPagination.page;
	}
}


function dataElementGetSearchLocations() {
		var returnList =  "";
		

		$('[name="dataElementLocations"]:checked').each(function(e){
			returnList += $(this).val() + " " ;
		});
		
		return returnList;
}

 	// Adds the given elements to the current data structure in
 	// session and the given reapeatbale group (assuming the page
 	// is called from the edit dataStructure page and is displayed
 	// in a lightbox). Displays the current attached repeatable
 	// groups and elements in the 'elementsDiv' section.
 	function addElements(repeatableGroupId) {
 	// If there are no Ids to add, then simply return
 	// The lightbox will still be closed
 		$("#addElementsButton").prop("disabled",true);
 		var getSelected = $('#dataElementResultsTable').idtApi('getSelected');
	    var dataElementNames = getSelected.toString();
 
 		if (dataElementNames == "") {
 			$("#addElementsButton").prop("disabled",false);
 			return;
 		}

 		$.ajax({
			url: "dataStructureElementAction!checkRetiredDataElements.ajax",
			data: {"dataElementNames" : dataElementNames},
			success: function(data) {
				var deprecatedRetiredDECount = data.split("|");
				var deprecatedDECount, retiredDECount;
				
				if (deprecatedRetiredDECount.length == 2) {
					deprecatedDECount = parseInt(deprecatedRetiredDECount[0]);
					retiredDECount = parseInt(deprecatedRetiredDECount[1]);
				}
				
				if (retiredDECount > 0) {
					alert("You have selected " + retiredDECount + " retired data element(s). Please remove them from your selection to continue.");
					$("#addElementsButton").prop("disabled",false);
					return;
				} 
				else {
					if (deprecatedDECount > 0) {
						if (!confirm("You have selected " + deprecatedDECount + " deprectated data element(s).  Would you like to proceed?")) {
							$("#addElementsButton").prop("disabled",false);
							return;
						}
					}
					
					// Find and store the open accordion
				 	var open =$("#details-accordion").accordion("option", "active");

				 	var address="dataStructureElementAction!addDataElements.action"
				 				+ "?dataElementNames=" + dataElementNames
				 				+ "&groupElementId=" + repeatableGroupId;
				 		
				 	window.location.href = address;
				 	// just in case
				 	$("#addElementsButton").prop("disabled",false);
				}
			}
 		});
 	}
 	
 	
 	// Publishes the selected data elements. Security is handled in the action. When done user is taken to search result page.
 	function bulkPublish() {
 		var getSelected = $('#dataElementResultsTable').idtApi('getSelected');
	    var checkedElementIds = getSelected.toString();
 		if (checkedElementIds == "") {
 			alert("You must select a data element to perform this action.");
 			return;
 		}
 		if (confirm("Are you sure you want to publish all selected elements?"))
 		{
 			
 			saveBulkStatusChange(2,checkedElementIds);
 			
 		}
 	}
 	
 	// Sets the status of all selected elements to AP. Security is handled in the action. When done user is taken to search result page.
 	function bulkAP() {
 		var getSelected = $('#dataElementResultsTable').idtApi('getSelected');
 		var checkedElementIds = getSelected.toString();
 		if (checkedElementIds == "") {
 			alert("You must select a data element to perform this action.");
 			return;
 		}
 		if (confirm("Are you sure you want to change the status of all selected elements to Awaiting Publication?"))
 		{
 			
 			$("#bulkAP").prop("value", "Working... Please Wait");
 			$("#bulkAP").prop("disabled",true);
 			
 			var address="dataElementAction!bulkAP.action";
 			
 			$.ajax({
 				type: "POST",
 				url: address,
 				data: {dataElementNames: checkedElementIds},
 				success: function(response){
 					if (window.navigator.userAgent.indexOf('MSIE')>0){
 						$("html").html(response);
 					} else {
 						document.open();
 						document.write(response);
 						document.close();
 					}
 				}									
 			});
 		}
 	}
 	
 	function refreshPublicationBtns() {
 		/*enable both Publish Selected Elements and Awaiting Publication buttons*/
 		//none is checked
 		if((!$("input[name='selectedStatuses'][value='Draft']").is(":checked")
 				&& !$("input[name='selectedStatuses'][value='Awaiting Publication']").is(":checked")  
 				&& !$("input[name='selectedStatuses'][value='Published']").is(":checked")
 				&& !$("input[name='selectedStatuses'][value='Deprecated']").is(":checked")
 				&& !$("input[name='selectedStatuses'][value='Retired']").is(":checked"))	
 			//Draft is checked
 			|| ($("input[name='selectedStatuses'][value='Draft']").is(":checked"))){

 			changePublicationBtns("publish", "enable");
			changePublicationBtns("awaiting", "enable");
 		}
 		
 		/*disable Awaiting Publication button only*/
 		//only Awaiting Publication is checked
 		else if (($("input[name='selectedStatuses'][value='Awaiting Publication']").is(":checked") 
				&& $("input[name='selectedStatuses'][value !='Awaiting Publication']:checkbox:checked").length == 0)
		//As long as Awaiting Publication is checked and Draft is not checked
				|| (!$("input[name='selectedStatuses'][value='Draft']").is(":checked")
				   && $("input[name='selectedStatuses'][value='Awaiting Publication']").is(":checked"))) {

			changePublicationBtns("publish", "enable");
			changePublicationBtns("awaiting", "disable");
		} 
		
		/*disable both Publish Selected Elements and Awaiting Publication buttons*/
		//only Published is checked
 		else if (($("input[name='selectedStatuses'][value='Published']").is(":checked") 
				&& $("input[name='selectedStatuses'][value !='Published']:checkbox:checked").length == 0)
		//only Deprecated is checked
		 || ($("input[name='selectedStatuses'][value='Deprecated']").is(":checked") 
				&& $("input[name='selectedStatuses'][value !='Deprecated']:checkbox:checked").length == 0)
		//only Retired is checked
		 || ($("input[name='selectedStatuses'][value='Retired']").is(":checked") 
				&& $("input[name='selectedStatuses'][value !='Retired']:checkbox:checked").length == 0)
		//neither of Draft or Awaiting Publication is checked
		 || (!$("input[name='selectedStatuses'][value='Draft']").is(":checked")
					&& !$("input[name='selectedStatuses'][value='Awaiting Publication']").is(":checked")
					&& $("input[name='selectedStatuses']:checkbox:checked").length > 0)) {

			changePublicationBtns("publish", "disable");
			changePublicationBtns("awaiting", "disable");
		} 
		
 	}
 	
 	function changePublicationBtns(btnName,status){
 		var $thisBtn = null;
 		if (btnName == "publish"){
 			$thisBtn = $('#bulkPublish');
 		} else if (btnName == "awaiting"){
 			$thisBtn = $('#bulkAP');
 		} 
 		if ($thisBtn != null){
	 		if (status == "enable"){
	 			$thisBtn.prop("disabled", false);
	 			$thisBtn.css('color', 'white');
	 		} else if (status == "disable"){
	 			$thisBtn.prop("disabled", true);	
	 			$thisBtn.css('color', 'grey');
	 		}
 		}		
 	}
 	
 	function bulkPublishAfterDialog(checkedElementIds){
 		
 		$("#bulkPublish").prop("value", "Publishing... Please Wait");
		$("#bulkPublish").prop("disabled",true);
		
		var address="dataElementAction!bulkPublish.action" + "?dataElementNames=" + checkedElementIds;
		
		window.location.href = address;
 	}
 	
 	function saveBulkStatusChange(publicationId,checkedElementIds){
 		
 		var firstBulkAction = "dataElementAction!dataElementStatusChange.ajax";
 		var submitBulkAction = "dataElementAction!submitDEStatusChange.ajax";
 		
 		//Declare dialogs	
 		 dialogOne = $statusChangeDiv.dialog({
 			
 			autoOpen :false,
 			modal : true,
 			height : 600,
 			width : 1000,
 			draggable : false,
 			resizable : false,
 			title : "Admin Status Change"
 		});

 		var buttons = {
 			'Finish' : function() {

 				var statusReason = $("#statusReason").val();
 				var reason = $("#reason").val();

 				if (validateRequiredFields(statusReason, reason)) {

 					$.ajax(submitBulkAction, {
 						"type" : "POST",
 						"data" : {
 							"statusReason" : statusReason,
 							"publicationId" : publicationId,
 							"reason" : reason,
 						},
 						"success" : function(data) {
 							
 								bulkPublishAfterDialog(checkedElementIds);							
 						}
 					});
 					$(this).dialog("close");
 				}
 			},
 			'Close' : function() {
 				$(this).dialog("close");
 			}
 		}

 		//make an ajax call to get the status change dialog
 		$.ajax(firstBulkAction, {
 			"type" : "POST",
 			"data" : {"publicationId" : publicationId,
 					  "bulkStatusChange":true,
 					  "currentId":currentId,
 					  "checkedElementIds":checkedElementIds},
 			"success" : function(data) {
 				statusChangeInner.html(data);
 				
 				refreshSupportDocTable();
 				
 				dialogOne.dialog("open");
 				dialogOne.dialog('option', 'buttons', buttons);

 			}
 		});
 	}