//Contains functions for a dataStructure search and defines dataStructurePagination Object

// dataStructure Search Object
// This is a global object containing the properties needed for search
var guidPagination = new Object();
guidPagination.page = 1;
guidPagination.pageSize = 10;
guidPagination.sort = "guid";
guidPagination.ascending = false;
guidPagination.mineOnly = null;

//encode(decode) html text into html entity
var decodeHtmlEntity = function(str) {
  return str.replace(/&#(\d+);/g, function(match, dec) {
    return String.fromCharCode(dec);
  });
};	

var guidTableFilters = {
		guids: "all", // or "guids", "pseudoguids", "converted_pseudoguids", "unconverted_pseudoguids"
		duplicates : "all" // all for "do not filter" or hide for "filter"
	};
if (typeof dataTableFilters === "undefined") {
	var dataTableFilters = guidTableFilters;
}
else {
	dataTableFilters = $.extend({}, dataTableFilters, guidTableFilters);
}


var currentUserId = $("#userId").val();
$.fn.dataTableExt.afnFiltering.push(function(oSettings, aData, iDataIndex) {
	if (oSettings.oInstance[0].parentNode.parentNode.id == "guidListContainer") {
		// true = show, false = hide
		// guid = 0
		var type = aData[1];
		// registered org = 2
		// registered user = aData[3];
		// date registered = aData[4];
		var linkedTo = aData[5];
		var duplicate = aData[6];
		
		if (dataTableFilters.duplicates != "all" && duplicate != "none") {
			return false;
		}
		
		if (dataTableFilters.guids != "all") {
			if (dataTableFilters.guids == "guids" && type != "GUID") {
				return false;
			}
			else if (dataTableFilters.guids == "pseudoguids" && type != "PseudoGUID") {
				return false;
			}
			else if (dataTableFilters.guids == "convertedPseudoguids") {
				if (type == "PseudoGUID" && linkedTo != "Not yet converted") {
					return true;
				}
				else {
					return false;
				}
			}
			// pseudoguid with a value in "linked" should be excluded
			else if (dataTableFilters.guids == "unconvertedPseudoguids") {
				if (type == "PseudoGUID" && linkedTo == "Not yet converted") {
					return true;
				}
				else {
					return false;
				}
			}
		}
		return true;
	}
	
	return true;
});


EventBus.on("init:table", function(model) {
	if (!model.get("filtersInitialized")) {
		model.get("$el").parents(".dataTableContainer").on("change", ".fg-toolbar select, .fg-toolbar input", function() {
			var $this = $(this);
			var $table = $this.parents(".dataTableContainer").find("table");
			var arrChangeVal = $this.val().split(":");
			dataTableFilters[arrChangeVal[0]] = arrChangeVal[1];
			var model = IDT.getTableModel($table);
			model.get("datatable").fnDraw();
		});
		model.set("filtersInitialized", true);
	}
});



// Additional properties ownerValue and filterValue are also required for search and are obtained
// through form elements on the page.

// This value submits a search. It takes no arguments, but uses global javascript variables on this page
// as well as reading reading values from the elements on the page
// These variables are set at the top of this block and changed by various javascript calls.
// Several other functions call this function after altering one of these variables to perform a search
function guidSearch() {

	$.fancybox.showActivity();
	
	var action = "/portal/guidAdmin/searchGuidAction!combinedSearch.ajax";
	
	if (guidPagination.mineOnly == true)
	{
		action = "/portal/guid/searchGuidAction!combinedSearch.ajax";
	}
	
	// Ajax call your search
	$.ajax(action, {
		"type": 	"POST",
		"contentType": "text/plain",
		"data": 	{"page" : this.guidPagination.page,
					"pageSize" : this.guidPagination.pageSize,
					"sort" : this.guidPagination.sort,
					"ascending" : this.guidPagination.ascending,
					"mineOnly" : this.guidPagination.mineOnly},
		"success": 	function(data) {
						$("#guidResultsId").html(data);
			            $("#guidResultsId").find("script").each(function(i) {
			            	if ($(this).attr("type") != "text/json") {
			            		eval($(this).text());
			            	}
			            });
			            $.fancybox.hideActivity();
			            buildDataTables();
					},
		"error":	function(data) {
						$.fancybox.hideActivity();
					}
	});
}

function exportGuids() {
	// two dimensional array, first is rows, second is columns
	// discussed in https://datatables.net/forums/discussion/20409/getting-filtered-unfiltered-rows-when-using-deferred-rendering
	var table = $("#guidListContainer table").dataTable();
	var visibleRows = table.fnSettings().aiDisplay;
	var allRows = table.fnSettings().aoData;
	var numVisible = visibleRows.length;
	var visibleData = [];
	for (var i = 0; i < numVisible; i++) {
		visibleData.push({
			"id" : $(allRows[visibleRows[i]]._aData[0]).text(),
			"type" : allRows[visibleRows[i]]._aData[1]
		});
	}
	
	var temparray,chunk = 10000;
	var chunkedPart = 1;
	var j=0;
	
	function createChunks(chunkedPart){
			
		temparray = visibleData.slice(j * chunk, (j + 1) * chunk);
			
			   $.ajax("/portal/guid/searchGuidAction!getChunckedData.ajax", {
					"type": 	"POST",
					"data": 	{"filteredData" : JSON.stringify(temparray),
								 "chunkedPart" : chunkedPart},
					"traditional": true,
					"success": 	function(returnData) {
						
								if (returnData == "success"){
										
									console.log("chunkedPart"+chunkedPart);
										if(chunkedPart<visibleData.length){
											console.log("j"+j);
											j++;
											createChunks(chunkedPart+=chunk);
										}
										else {
											console.log("submit called");
											$("#downloadForm").submit();
										}																			
								}
							},
				});	
		
	} 
	createChunks(chunkedPart);
	
}

// This function casues the result to jump to a page given by the page text field
// This text field is defined in elementList.jsp
// This function also checks to make sure the given page input is valid
function guidCheckPageField(maxPage) {
	var desiredPage = document.getElementById("guidPaginationJump").value;
	maxPage = Math.ceil(maxPage);
	if (!isNaN(desiredPage)) {
		if (desiredPage <= maxPage && desiredPage > 0 && (Math.ceil(desiredPage) / desiredPage == 1)) {
			guidPagination.page = desiredPage;
			guidSearch();
		}
		else {
			document.getElementById("guidPaginationJump").value = guidPagination.page;
		}
		
	}
	else {
		document.getElementById("guidPaginationJump").value = guidPagination.page;
	}
}

// Sets the global pagination values back to their default values
function guidResetPagination() {
	guidPagination.page = 1;
	guidPagination.pageSize = 10;
}

function guidResetSort() {
	guidPagination.sort = "guid";
	guidPagination.ascending = false;
}

// Function called when user clicks a table head to sort a column.
function guidSetSort(sortIn) {
	guidResetPagination();
	if (sortIn == guidPagination.sort) {
		guidPagination.ascending = !guidPagination.ascending;
	}
	else {
		guidPagination.sort = sortIn;
	}
	guidSearch();
};

function guidSetMineOnly(mineOnly) {
	if (mineOnly == true)
	{
		this.guidPagination.mineOnly = mineOnly;
	}
	else
	{
		this.guidPagination.mineOnly = false;
	}
	
}

if (typeof buildDataTables === "undefined") {
	function buildDataTables() {
		/**
		 * Build all datatables when they are finished loading.  This should speed up
		 * the loading process for those tables.
		 */
		// only if the variable dataTablesDisabled is not set
		if (typeof dataTablesDisabled == "undefined" || dataTablesDisabled != true) { 
			if (typeof IbisDataTables != "undefined") {
				IbisDataTables.fullBuild();
			}
		}
	}
}