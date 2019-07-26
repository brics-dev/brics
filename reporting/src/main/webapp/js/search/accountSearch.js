//Contains functions for account search and defines accountSearchObject

// Account Search Object
// This is a global object containing the properties needed for search
var accountPagination = new Object();
accountPagination.page = 1;
accountPagination.pageSize = 10;
accountPagination.sort = "userName";
accountPagination.ascending = false;

var accountSearchFilters = {
		filterStatus : "all" // All | Active | Requested | Pending | Denied | Inactive | Withdrawn
		/*
			-1 = All
			0 = Active
			3 = Requested
			4 = Pending
			2 = Denied
			1 = Inactive
			5 = Withdrawn
		 */
	};
if (typeof dataTableFilters === "undefined") {
	var dataTableFilters = accountSearchFilters;
}
else {
	dataTableFilters = $.extend({}, accountFilters, accountSearchFilters);
}

$.fn.dataTableExt.afnFiltering.push(function(oSettings, aData, iDataIndex) {
	if (oSettings.oInstance[0].parentNode.parentNode.id == "accountListContainer") {
		
		var tableStatus = aData[6];
		
		
		if (dataTableFilters.filterStatus != "all") {
			if (dataTableFilters.filterStatus == "active") {
				return tableStatus == "Active";
			}
			else if (dataTableFilters.filterStatus == "requested") {
				return tableStatus == "Requested";
			}
			else if (dataTableFilters.filterStatus == "pending") {
				return tableStatus == "Pending";
			}
			else if (dataTableFilters.filterStatus == "denied") {
				return tableStatus == "Denied";
			}
			else if (dataTableFilters.filterStatus == "inactive") {
				return tableStatus == "Inactive";
			}
			else if (dataTableFilters.filterStatus == "withdrawn") {
				return tableStatus == "Withdrawn";
			}
			else if (dataTableFilters.filterStatus == "locked") {
				return tableStatus == "Locked";
			}
		}
	}
	
	return true;
});

EventBus.on("init:table", function(model) {
	model.get("$el").parents(".dataTableContainer").on("change", ".fg-toolbar select", function() {
		var $this = $(this);
		var $table = $this.parents(".dataTableContainer").find("table");
		var arrChangeVal = $this.val().split(":");
		dataTableFilters[arrChangeVal[0]] = arrChangeVal[1];
		var model = IDT.getTableModel($table);
		model.get("datatable").fnDraw();
	});
});


// Additional properties searchKey and filterValue are also required for search and are obtained
// through form elements on the page.

// This value submits a search. It takes no arguments, but uses global javascript variables on this page
// as well as reading reading values from the elements on the page
// These variables are set at the top of this block and changed by various javascript calls.
// Several other functions call this function after altering one of these variables to perform a search
function accountSearch() {
	
	var searchKey = $("#accountSearchKey").val();
	var filterValue = $(".accountSelectedFilter")[0].id;
	
	// Ajax call your search
	$.fancybox.showActivity();
	$.ajax("accountAction!search.ajax", {
		"type": 	"POST",
		"data": 	{"page" : this.accountPagination.page,
					"pageSize" : this.accountPagination.pageSize,
					"sort" : this.accountPagination.sort,
					"ascending" : this.accountPagination.ascending,
					"key" : searchKey,
					"statusId" : filterValue},
		"success": 	function(data) {
						$("#accountResultsId").html(data);
			            $("#accountResultsId").find("script").each(function(i) {
			                eval($(this).text());
			            });
			            $.fancybox.hideActivity();
					},
		"error":	function(data) {
						$.fancybox.hideActivity();
					}
	});
}

// Alters the filter links to the appropriate status and then calls the search function
function accountSetFilter(newFilterId) {
	var oldFilter = $(".accountSelectedFilter")[0];
	var newFilter = $("#" + newFilterId)[0];
	
	var oldFilterId = $(oldFilter).attr('id');
	
	$(oldFilter).removeAttr('class');
	$(oldFilter).attr("href", 'javascript:accountSetFilter(' + oldFilterId + ');');
	$(newFilter).attr("class", "inactiveLink accountSelectedFilter");
	$(newFilter).removeAttr("href");
	
	accountResetPagination();
	accountResetSort();
	accountSearch();
	
}

// This function casues the result to jump to a page given by the page text field
// This text field is defined in elementList.jsp
// This function also checks to make sure the given page input is valid
function accountCheckPageField(maxPage) {
	var desiredPage = document.getElementById("accountPaginationJump").value;
	maxPage = Math.ceil(maxPage);
	if (!isNaN(desiredPage)) {
		if (desiredPage <= maxPage && desiredPage > 0 && (Math.ceil(desiredPage) / desiredPage == 1)) {
			accountPagination.page = desiredPage;
			accountSearch();
		}
		else {
			document.getElementById("accountPaginationJump").value = accountPagination.page;
		}
		
	}
	else {
		document.getElementById("accountPaginationJump").value = accountPagination.page;
	}
}

// Sets the global pagination values back to their default values
function accountResetPagination() {
	accountPagination.page = 1;
	accountPagination.pageSize = 10;
}

function accountResetSort() {
	accountPagination.sort = "userName";
	accountPagination.ascending = false;
}

// Function called when user clicks a table head to sort a column.
function accountSetSort(sortIn) {
	accountResetPagination();
	if (sortIn == accountPagination.sort) {
		accountPagination.ascending = !accountPagination.ascending;
	}
	else {
		accountPagination.sort = sortIn;
	}
	accountSearch();
};