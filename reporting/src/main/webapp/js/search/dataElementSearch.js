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

//Alters the filter links to the appropriate status and then calls the search function
function refreshSearch() {
	
	//disable checkbox for mapped dataelements
	if(($('#mapped').val()) ) {
		
		
		if($('#masterCheckbox').length > 0) {
			$('#masterCheckbox').attr("disabled",true);
		}
		
		if($("input[name=dataElementCheckbox]").length > 0) {
			$("input[name=dataElementCheckbox]").attr("disabled",true);
		}
	}
	
	dataElementSearch();
	refreshPublicationBtns();
}


// This value submits a search. It takes no arguments, but uses global javascript variables on this page
// as well as reading reading values from the elements on the page
// These variables are set at the top of this block and changed by various javascript calls.
// Several other functions call this function after altering one of these variables to perform a search
function dataElementSearch() {

	// Grab search variables from form elements
	//var filterValue = $(".dataElementSelectedFilter")[0].id;
	var hostName = $("#hostName").val();
	var hostStyle = "brics-style";
	
	if(hostName.indexOf('pdbp') > -1 ){
		hostStyle = "pdbp-style";		
	}else if(hostName.indexOf('fitbir') > -1 ) {
		hostStyle = "fitbir-style";
	}else if(hostName.indexOf('eyegene') > -1 ) {
		hostStyle = "eyegene-style";
	}else if(hostName.indexOf('cnrm') > -1 ) {
		hostStyle = "cnrm-style";
	}else if(hostName.indexOf('ninds') > -1 ) {
		hostStyle = "ninds-style";
	}else if(hostName.indexOf('cistar') > -1 ) {
		hostStyle = "cistar-style";
	}else if(hostName.indexOf('cdrns') > -1 ) {
		hostStyle = "cdrns-style";
	}else if(hostName.indexOf('nti') > -1 ) {
		hostStyle = "nti-style";
	}
	
	var searchKey = $("#searchKey").val().trim();
	var exactMatch = $("#exactMatch").is(':checked');
	var mapped = $("#mapped").val();
	var publicArea = $("#public").val();
	//var general = $("#general").val();
	if(publicArea != '' && publicArea == "true") {
		var ownerId = $('[name="ownerId"]').val();
	} else {
		var ownerId = $('[name="ownerId"]:checked').val();
		
	}
	
	var modifiedDate = $("#modifiedDateList").val();
	
	var searchLocations = $('[name="dataElementLocations"]:checked').map(function() {
	    return this.value;
	}).get();
	
	var diseaseSelection = $('[name="selectedDiseasesBox"]:checked').map(function() {
	    return this.value;
	}).get();

	var populationSelection = $('[name="selectedPopulations"]:checked').map(function() {
	    return this.value;
	}).get();
	
	var classificationSelection = $('[name="selectedClassifications"]:checked').map(function() {
	    return this.value;
	}).get();
		
	var statusValue = $('[name="selectedStatusOptions"]:checked').map(function() {
		return this.value;
	}).get();
	
	// Since pdbp and fitbir have different sets of status filters, if there is no status option selected, we pass all 
	// status values to the search so that we don't need to handle the logic on the server side. 
	if (publicArea == "true" && statusValue.length == 0) {
		statusValue = $('[name="selectedStatusOptions"]').map(function() {
			return this.value;
		}).get();
	}
		
	var elementTypes = $('[name="selectedElementTypes"]:checked').map(function() {
	    return this.value;
	}).get();
	
	var domainSelection = "";
	var subDomainSelection = "";
	var classificationSelection = "";

	if( save )
	{
	//
	domainSelection = domainArray;
	subDomainSelection = subDomainArray.join(';');
	classificationSelection = classificationsArray;
	
	}
	
	
	// Verify that a valid search location has been selected
	if(searchLocations == "") {
		alert("You must select at least one field to search.");
		return;
	}
	
	var action = "searchDataElementAction!search.ajax";

	var amount = 0;

	var oTable = $('#resultTable').DataTable( {
			
			"bProcessing": true,
			"bJQueryUI": true,
		    "bServerSide": true,
		    "sAjaxSource": action,
		    
		    "fnServerParams": function (aoData) {
		    	
		    	aoData.push( 
		    			{"name":"ownerId", "value" : ownerId},
		    			{"name":"selectedDiseases", "value" : diseaseSelection.toString()},
		    			{"name":"selectedDomains", "value" : domainSelection.toString()},
		    			{"name":"selectedSubdomains" , "value" : subDomainSelection.toString()},
		    			{"name":"populationSelection" , "value" : populationSelection.toString()},
		    			//{"name":"subgroupSelection" , "value" : subgroupSelection},
		    			{"name":"selectedClassifications" , "value" : classificationSelection.toString()},
		    			{"name":"selectedElementTypes" , "value" : elementTypes.toString()},
		    			{"name":"searchKey" , "value" : searchKey},
		    			{"name":"exactMatch" , "value" : exactMatch},
		    			/*{"name":"filterId" , "value" : filterValue},*/
		    			//{"name":"general" , "value" : general},
		    			{"name":"dataElementLocations" , "value" : searchLocations.toString()},
		    			{"name":"mapped" , "value" : mapped},
		    			{"name":"publicArea" , "value" : publicArea},
		    			{"name":"selectedStatuses", "value": statusValue.toString()},
		    			{"name":"modifiedDate", "value": modifiedDate}
		    			
		        		/*{"name":"selectedFacets['Facet1']", "value": facetValue}*/);
		    },
		        
		    "bFilter": false,
		    "sPaginationType": "full_numbers",
		    "sScrollX": "100%",
		    "bScrollCollapse": true,
		    "bAutoWidth":false,
		    "bDestroy": true,
		    "sServerMethod": "POST",
		    "sEmptyTable": "Loading data from server", 
		    "iDisplayLength": persistIDisplayLength,

		    "aoColumnDefs":[ 		                    
		                    {"aTargets": [ 0 ],
		                     "bSortable": ($('#mapped').val() == "true" || $('#inAdmin').val() == "true") ? false: true
		                    	
		                    },
	                        {
	                      	 "aTargets": ["_all"],
				             "fnCreatedCell": function (nTd, sData, oData, iRow, iCol) {
				            	 
				            	//create checkbox
			            		
				            	if(iCol == 0 && ($('#mapped').val() || $('#inAdmin').val()) ) {
				            		
				            		if($.inArray(sData,dataElementsMappingArray) > -1) {
				            			vCheckBox = "inc";
				            		} else {
				            			vCheckBox = '<div style="text-align:center;"><input type="checkbox" name="dataElementCheckbox" value="'+sData+'" onClick="dataElementChecked(this)"  /></div>';
				            		}
				            		
				            		$(nTd).html(vCheckBox);
				            	}
				            	grantedAccess = true; ///currently granted access is always true, i'm not sure why this is needed.
				            	
				            	namespace = 'dictionary';
				            	if($('#inAdmin').val() == "true") { 
				            		namespace = 'dictionaryAdmin';
				            	} else {
				            		namespace = 'dictionary';
				            	}
				    			
				            	cellContent = sData.split("|");
				            	newContent = $(nTd).html();
				            	if(cellContent.length > 1) {
				            		
				            		if($('#public').val() != '' && $('#public').val() == "true") {
					            		newContent = '<a target="_blank" href="/'+$('#portalRoot').val()+'/publicData/dataElementAction!view.action?dataElementName='+cellContent[0]+'&publicArea=true&style.key='+hostStyle+'">'+cellContent[1]+'</a>';
					            		
				            		} else {
				            			
				            			if(grantedAccess) {
				            				
				            				if(!$('#mapped').val()) {
				            					newContent ='<a href="/'+$('#portalRoot').val()+'/'+namespace+'/dataElementAction!view.action?dataElementName='+cellContent[0]+'">'+cellContent[1]+'</a>';
				            				} else {
				            					newContent ='<a class="lightbox" target="_blank" href="/'+$('#portalRoot').val()+'/'+namespace+'/dataElementAction!viewDetails.ajax?dataElementName='+cellContent[0]+'">'+cellContent[1]+'</a>';		
				            				}
				            				
				            			} else {
				            				newContent = cellContent[1];
				            			}
				            			
				            		}
				            		
			
				            		$(nTd).html(newContent);
				            		
				            	}
				            	
					            $(nTd).html(newContent);

					            //add word wrap for title column
					            if(!($('#mapped').val() || $('#inAdmin').val() === 'true')) { //in Data Dictionary Tool and public site
					            	if (iCol == 0){					        	
					        		   $(nTd).wrapInner( "<div style='word-wrap: break-word;'></div>");
					                } else if (iCol == 1) {					        	  
					        	      $(nTd).wrapInner( "<div style='word-wrap: break-word;'></div>");					        	  
					                }
				                } else { // in Data Dictionary Administration
					            	if (iCol == 1){							        	
					            		$(nTd).wrapInner( "<div style='word-wrap: break-word;'></div>");					             
						            } else if (iCol == 2) {						        	  
						        	    $(nTd).wrapInner( "<div style='word-wrap: break-word;'></div>");							        	  
						            }
				                }
			           		
				           		if($('#public').val() != '' && $('#public').val() === "true") { //public site
					           		if(iCol == 1) {
					           			$(nTd).wrapInner( "<div style='max-width:100px; word-wrap: break-word;'></div>");
						           	}
					           	}
   
					          }
				             },		             
				             { "sWidth": !($('#mapped').val() || $('#inAdmin').val() === 'true') ? "45%" : "40px", "aTargets": [0] },
				             { "sWidth": !($('#mapped').val() || $('#inAdmin').val() === 'true') ? "20%" : "45%" , "aTargets": [1] },
				             { "sWidth": !($('#mapped').val() || $('#inAdmin').val() === 'true') ? "8%"  : "12%" , "aTargets": [2] }
				           ],
		       	        
	        "fnInitComplete": function ( oSettings, json ) {          
	        },
	        "fnInfoCallback": function( oSettings, iStart, iEnd, iMax, iTotal, sPre ) {
	        	  
	        	  persistIDisplayLength = oSettings._iDisplayLength;
	        	  
	        	//  amount = iTotal;
	        	  
	        	  if(iTotal > 0) {
	        		  return "Showing " + iStart +" to "+ iEnd + " of " + iTotal + " entries";
	        	  } else {
	        		return "";
	        	  }
	        	 
		        	 
	        		
	         },
	         "fnHeaderCallback": function( nHead, aData, iStart, iEnd, aiDisplay ) {	
	        	 $(".tableRowHeader th").attr("role", "columnheader");
	         },
	         "fnDrawCallback": function( oSettings ) {
    	    	  if ($("#masterCheckbox").length ) {
    	    		  $("#masterCheckbox")[0].checked = false;
    	  			  $('#masterCheckbox').attr("disabled", false);
    	    	  }
    	    	  dataElementCheckSelectedBoxes();
    	    	  tooltip();
    	    	  $("a.lightbox").fancybox();
    	    	  oSettings._iDisplayLength = persistIDisplayLength;
	        	 /* if(amount.toString().toUpperCase() == 'NAN') {
	        		  amount = 0;
	        	  }*/
	        	 // $('#downloadResultsLink').val('Download All ' + amount + ' Results');
    	    	  $('#downloadResultsLink').val('Download All ' + oSettings._iRecordsTotal + ' Results');
	        }
		    	
	});	
}

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
	
	var diseaseSelection = $('[name="selectedDiseasesBox"]:checked').map(function() {
	    return this.value;
	}).get();

	var populationSelection = $('[name="selectedPopulations"]:checked').map(function() {
	    return this.value;
	}).get();
	
	var classificationSelection = $('[name="selectedClassifications"]:checked').map(function() {
	    return this.value;
	}).get();
		
	
	var statusValue = $('[name="selectedStatusOptions"]:checked').map(function() {
		return this.value;
	}).get();

	// Since pdbp and fitbir have different sets of status filters, if there is no status option selected, we pass all 
	// status values to the search so that we don't need to handle the host specific logic on the server side. 
	if (publicArea == "true" && statusValue.length == 0) {
		statusValue = $('[name="selectedStatusOptions"]').map(function() {
			return this.value;
		}).get();
	}
	
	var elementTypes = $('[name="selectedElementTypes"]:checked').map(function() {
	    return this.value;
	}).get();
	
	var domainSelection = "";
	var subDomainSelection = "";
	var classificationSelection = "";
	
	if( save )
	{
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

		dataElementResetPagination();
		dataElementResetSort();
		dataElementSearch();
	
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
			dataElementSearch();
		}
		else {
			document.getElementById("dataElementPaginationJump").value = dataElementPagination.page;
		}
		
	}
	else {
		document.getElementById("dataElementPaginationJump").value = dataElementPagination.page;
	}
}

// Sets the global pagination values back to their default values
function dataElementResetPagination() {
	dataElementPagination.page = 1;
	dataElementPagination.pageSize = 10;
}

function dataElementResetSort() {
	dataElementPagination.sort = "title";
	dataElementPagination.ascending = false;
}

// Function called when user clicks a table head to sort a column.
function dataElementSetSort(sortIn) {
	dataElementResetPagination();
	if (sortIn == dataElementPagination.sort) {
		dataElementPagination.ascending = !dataElementPagination.ascending;
	}
	else {
		dataElementPagination.sort = sortIn;
	}
	dataElementSearch();
}

function dataElementGetSearchLocations() {
		var returnList =  "";
		

		$('[name="dataElementLocations"]:checked').each(function(e){
			returnList += $(this).val() + " " ;
		});
		
		return returnList;
}

// After a successful search this function checks any boxes that have been selected
function dataElementCheckSelectedBoxes() {
    var checkBoxes = $("input[name=dataElementCheckbox]");
    for (i in dataElementPagination.selectedElementIds) {
	    $.each(checkBoxes, function() {
	    	if (dataElementPagination.selectedElementIds[i] == this.value) {
	    		this.checked=true;
	    	}
	    });
    }
}

function dataElementResetFilters(){

	$('#searchForm').get(0).reset();
}

// 	Returns a string of locations to search based on the locations checkboxes
	function getDataElementSearchLocations() {
		var returnList =  "";
	

		with (document.criteria) {
			for ( var i = 0; i < locations.length; i++) {
				if (locations[i].checked) {
					returnList += locations[i].value + " " ;
				}
			}
		}
		return returnList;

	}
	
 	// Tracks checked elements over multiple pages of results
	// Only relevent when displaying mapped results in the editDS lightbox
 	function dataElementChecked(box) {
 		
 		if (box.checked)
 		{
 			dataElementPagination.selectedElementIds.push(box.value);
 		}
 		else
 		{
 			// when a box is unchecked, the master checkbox needs to be unchecked
 			$("#masterCheckbox")[0].checked = false;
			
 			for(var i=0; i<dataElementPagination.selectedElementIds.length; i++)
 			{
 				if (dataElementPagination.selectedElementIds[i] == box.value)
 				{
 					dataElementPagination.selectedElementIds.splice(i, 1);
 					i--;
 				}
 			}
 		}
 	}
	
 	//Javascript function for the select/deselect all checkbox
 	function masterCheckboxClicked(box) {
 		var checkboxes = $("input[name=dataElementCheckbox]");
 		if (box.checked)
 		{
 			$.each(checkboxes, function() {
 				if (!this.checked)
 				{
 					this.checked = true;
 					dataElementChecked(this);
 				}
 			});
 		}
 		else
 		{
 			$.each(checkboxes, function() {
 				if (this.checked)
 				{
 					this.checked = false;
 					dataElementChecked(this);
 				}
 			});
 		}
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
		var dataElementNames = getCheckedElementIds();
 
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
 	function bulkPublish()
 	{
 		var checkedElementIds = getCheckedElementIds();
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
 	function bulkAP()
 	{
 		var checkedElementIds = getCheckedElementIds();
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
 	
 	// Returns a string of the IDs of all the checked Data Elements (comma delimated)
 	function getCheckedElementIds() {
 	    var returnList = "";
	    
 		for (i in dataElementPagination.selectedElementIds) {
 			returnList += dataElementPagination.selectedElementIds[i] + ",";
 		}
		
 		return returnList;
 	}
 	
 	function refreshPublicationBtns() {
 		/*enable both Publish Selected Elements and Awaiting Publication buttons*/
 		//none is checked
 		if((!$("input[name='selectedStatusOptions'][value='Draft']").is(":checked")
 				&& !$("input[name='selectedStatusOptions'][value='Awaiting Publication']").is(":checked")  
 				&& !$("input[name='selectedStatusOptions'][value='Published']").is(":checked")
 				&& !$("input[name='selectedStatusOptions'][value='Deprecated']").is(":checked")
 				&& !$("input[name='selectedStatusOptions'][value='Retired']").is(":checked"))	
 			//Draft is checked
 			|| ($("input[name='selectedStatusOptions'][value='Draft']").is(":checked"))){

 			changePublicationBtns("publish", "enable");
			changePublicationBtns("awaiting", "enable");
 		}
 		
 		/*disable Awaiting Publication button only*/
 		//only Awaiting Publication is checked
 		else if (($("input[name='selectedStatusOptions'][value='Awaiting Publication']").is(":checked") 
				&& $("input[name='selectedStatusOptions'][value !='Awaiting Publication']:checkbox:checked").length == 0)
		//As long as Awaiting Publication is checked and Draft is not checked
				|| (!$("input[name='selectedStatusOptions'][value='Draft']").is(":checked")
				   && $("input[name='selectedStatusOptions'][value='Awaiting Publication']").is(":checked"))) {

			changePublicationBtns("publish", "enable");
			changePublicationBtns("awaiting", "disable");
		} 
		
		/*disable both Publish Selected Elements and Awaiting Publication buttons*/
		//only Published is checked
 		else if (($("input[name='selectedStatusOptions'][value='Published']").is(":checked") 
				&& $("input[name='selectedStatusOptions'][value !='Published']:checkbox:checked").length == 0)
		//only Deprecated is checked
		 || ($("input[name='selectedStatusOptions'][value='Deprecated']").is(":checked") 
				&& $("input[name='selectedStatusOptions'][value !='Deprecated']:checkbox:checked").length == 0)
		//only Retired is checked
		 || ($("input[name='selectedStatusOptions'][value='Retired']").is(":checked") 
				&& $("input[name='selectedStatusOptions'][value !='Retired']:checkbox:checked").length == 0)
		//neither of Draft or Awaiting Publication is checked
		 || (!$("input[name='selectedStatusOptions'][value='Draft']").is(":checked")
					&& !$("input[name='selectedStatusOptions'][value='Awaiting Publication']").is(":checked")
					&& $("input[name='selectedStatusOptions']:checkbox:checked").length > 0)) {

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