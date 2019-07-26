// Contains functions for a study search and defines studyPagination Object

// study Search Object
// This is a global object containing the properties needed for search
var studyPagination = new Object();
studyPagination.page = 1;
studyPagination.pageSize = 10;
studyPagination.sort = "title";
studyPagination.ascending = false;
studyPagination.namespace = "";

var studySearchFilters = {
	showAllOrHidden: "all", // or "hide": all = show all, hide = hide without
							// data
	displayOwnership: "all", // or "mine": all = show all, mine = hide where
								// permission != "Owner"
	dataType: "all"
};
if (typeof dataTableFilters === "undefined") {
	var dataTableFilters = studySearchFilters;
}
else {
	dataTableFilters = $.extend({}, dataTableFilters, studySearchFilters);
}

$.fn.dataTableExt.afnFiltering.push(function(oSettings, aData, iDataIndex) {
	if (oSettings.oInstance[0].parentNode.parentNode.id == "studyListContainer") {
		var currentDataType = aData[3];
		var selectedDataType = dataTableFilters.dataType; // get the selected
															// data type in the
															// drop down filter
		var dataType = selectedDataType == 'all' ? true : currentDataType.indexOf(selectedDataType) >= 0; // check
																											// if
																											// the
																											// row's
																											// data
																											// type
																											// contains
																											// whats
																											// selected
																											// in
																											// the
																											// data
																											// type
																											// filter
		var dataTypeNone = aData[3].indexOf("no data") > -1; // true if there
																// is no data
		var ownershipOwner = aData[4] == "Owner"; // true if permission =
													// "Owner"

		if (dataTableFilters.showAllOrHidden == "all" && dataTableFilters.displayOwnership == "all") {
			return true && dataType; // in this case, show all
		}
		else if (dataTableFilters.showAllOrHidden == "all" && dataTableFilters.displayOwnership == "mine") {
			return ownershipOwner && dataType; // show all where Ownership =
												// "Owner"
		}
		else if (dataTableFilters.showAllOrHidden = "hide" && dataTableFilters.displayOwnership == "all") {
			return !dataTypeNone && dataType; // note "not": show all where
												// data type != none
		}
		else if (dataTableFilters.showAllOrHidden = "hide" && dataTableFilters.displayOwnership == "mine") {
			// return false if the row has no data OR if the ownership is not
			// Owner
			// return true if the row is Owner and has data
			return !dataTypeNone && ownershipOwner && dataType;
		}
	}
	else {
		return true;
	}
});

EventBus.on("init:table", function(model) {
	if (!model.get("filtersInitialized")) {
		model.get("$el").parents(".dataTableContainer").on("change", ".fg-toolbar select, .fg-toolbar input",
						function() {
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

// Additional properties ownerValue and filterValue are also required for search
// and are obtained
// through form elements on the page.

// This value submits a search. It takes no arguments, but uses global
// javascript variables on this page
// as well as reading reading values from the elements on the page
// These variables are set at the top of this block and changed by various
// javascript calls.
// Several other functions call this function after altering one of these
// variables to perform a search
function studySearch() {
	var ownerValue = $("#studyViewSelect").val();
	var filterValue = "-1";
	var searchKey = $("#studySearchKey").val();
	var recruitmentStatus = $("#studyRecruitmentStatusSelection").val();
	var grantNumber = $("#studyGrantNumber").val();
	var trialNumber = $("#studyTrialNumber").val();
	var action = this.studyPagination.namespace + "studyAction!search.ajax";

	// Ajax call your search
	// $.fancybox.showActivity();

	// reset the data filter back to default
	var $studyDataFilter = $("#showAllStudies");
	if (!$studyDataFilter.is(":disabled")) {
		$studyDataFilter.click();
	}

	$.ajax(action, {
		"type": "POST",
		"data": {
			"page": this.studyPagination.page,
			"pageSize": this.studyPagination.pageSize,
			"sort": this.studyPagination.sort,
			"ascending": this.studyPagination.ascending,
			"ownerId": ownerValue,
			"filterId": filterValue,
			// "key" : searchKey,
			"recruitmentStatusSelection": recruitmentStatus,
		// "grantId": grantNumber,
		// "clinicalTrialId": trialNumber
		},
		"success": function(data) {
			$("#studyResultsId").html(data);
			$("#studyResultsId").find('script').each(function(i) {
				if ($(this).attr("type") != "text/json") {
					eval($(this).text());
				}
			});
			// $.fancybox.hideActivity();
			buildDataTables();
		},
		"error": function(data) {
			// $.fancybox.hideActivity();
		}
	});
}

// Alters the filter links to the appropriate status and then calls the search
// function
function studySetFilter(newFilterId) {
	var oldFilter = $(".studySelectedFilter")[0];
	var newFilter = $("#" + newFilterId, $("#studyFilterOptions"))[0];

	var oldFilterId = $(oldFilter).attr('id');

	$(oldFilter).removeAttr('class');
	$(oldFilter).attr("href", 'javascript:studySetFilter(' + oldFilterId + ');');
	$(newFilter).attr("class", "inactiveLink studySelectedFilter");
	$(newFilter).removeAttr("href");

	studyResetPagination();
	studyResetSort();
}

function studySetOwner(newOwnerId) {
	var oldOwner = $(".studySelectedOwner")[0];
	var newOwner = $("#" + newOwnerId, $("#studyOwnerOptions"))[0];

	var oldOwnerId = $(oldOwner).attr('id');

	$(oldOwner).removeAttr('class');
	$(oldOwner).attr("href", 'javascript:studySetOwner(' + oldOwnerId + ');');
	$(newOwner).attr("class", "inactiveLink studySelectedOwner");
	$(newOwner).removeAttr("href");

	studyResetPagination();
	studyResetSort();
}

// This function casues the result to jump to a page given by the page text
// field
// This text field is defined in elementList.jsp
// This function also checks to make sure the given page input is valid
function studyCheckPageField(maxPage) {
	var desiredPage = document.getElementById("studyPaginationJump").value;
	maxPage = Math.ceil(maxPage);
	if (!isNaN(desiredPage)) {
		if (desiredPage <= maxPage && desiredPage > 0 && (Math.ceil(desiredPage) / desiredPage == 1)) {
			studyPagination.page = desiredPage;
			studySearch();
		}
		else {
			document.getElementById("studyPaginationJump").value = studyPagination.page;
		}

	}
	else {
		document.getElementById("studyPaginationJump").value = studyPagination.page;
	}
}

// Sets the global pagination values back to their default values
function studyResetPagination() {
	studyPagination.page = 1;
	studyPagination.pageSize = 10;
}

function studyResetSort() {
	studyPagination.sort = "title";
	studyPagination.ascending = false;
}

function studyResetFilters() {
	$("#studySearchKey").val("");
	$("#studyRecruitmentStatusSelection").val("");
	$("#studyGrantNumber").val("");
	$("#studyTrialNumber").val("");
}

// Function called when user clicks a table head to sort a column.
function studySetSort(sortIn) {
	studyResetPagination();
	if (sortIn == studyPagination.sort) {
		studyPagination.ascending = !studyPagination.ascending;
	}
	else {
		studyPagination.sort = sortIn;
	}
	studySearch();
};

if (typeof buildDataTables === "undefined") {
	function buildDataTables() {
		/**
		 * Build all datatables when they are finished loading. This should
		 * speed up the loading process for those tables.
		 */
		// only if the variable dataTablesDisabled is not set
		if (typeof dataTablesDisabled == "undefined" || dataTablesDisabled != true) {
			if (typeof IbisDataTables != "undefined") {
				IbisDataTables.fullBuild();
			}
		}
	}
}

function updateSelectStudies() {
	var $table = $("#studyListTableTable");
	var selectedOptionsLength = $table.idtApi("getSelected").length;
	/*enable DOWNLOAD REPORT button*/
	if(selectedOptionsLength > 0){
		$("#downloadReportBtn").prop("disabled", false);
		$("#downloadReportBtn").parent().closest('div').removeClass("disabled");
	} else {
		$("#downloadReportBtn").prop("disabled", true);
		$("#downloadReportBtn").parent().closest('div').addClass("disabled");
	}
}