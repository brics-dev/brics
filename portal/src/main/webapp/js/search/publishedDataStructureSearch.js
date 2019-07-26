//Contains functions for a publishedDataStructure search and defines publishedDataStructurePagination Object

// publishedDataStructure Search Object
// This is a global object containing the properties needed for search
var publishedDataStructurePagination = new Object();
publishedDataStructurePagination.page = 1;
publishedDataStructurePagination.pageSize = 10;
publishedDataStructurePagination.sort = "title";
publishedDataStructurePagination.ascending = false;

// Additional properties ownerValue and filterValue are also required for search and are obtained
// through form elements on the page.

// This value submits a search. It takes no arguments, but uses global javascript variables on this page
// as well as reading reading values from the elements on the page
// These variables are set at the top of this block and changed by various javascript calls.
// Several other functions call this function after altering one of these variables to perform a search
function publishedDataStructureSearch() {

	// Ajax call your search
	$.fancybox.showActivity();
	//var filterValue = $(".dataStructureSelectedFilter")[0].id;
	$.ajax("/portal/dictionary/listDataStructureAction!searchPublished.ajax", {
		"type": 	"POST",
		"data": 	{"page" : this.publishedDataStructurePagination.page,
					"pageSize" : this.publishedDataStructurePagination.pageSize,
					"sort" : this.publishedDataStructurePagination.sort,
					"ascending" : this.publishedDataStructurePagination.ascending//,
					//"statusId" : filterValue
					},
		"success": 	function(data) {
			
			$('#publishedDataStructureResultsId').html(data);
					//document.getElementById("publishedDataStructureResultsId").innerHTML = data;
					$('a.lightbox').fancybox();
					tooltip();
		            $.fancybox.hideActivity();
		},
		"error":	function(data) {
						$.fancybox.hideActivity();
					}
	});
}

//Alters the filter links to the appropriate status and then calls the search function
function publishedDataStructureSetFilter(newFilterId) {
	var oldFilter = $(".dataStructureSelectedFilter")[0];
	var newFilter = $("#" + newFilterId, $("#dataStructureFilterOptions"))[0];
	
	var oldFilterId = $(oldFilter).attr('id');
	
	$(oldFilter).removeAttr('class');
	$(oldFilter).attr("href", 'javascript:publishedDataStructureSetFilter(' + oldFilterId + ');');
	$(newFilter).attr("class", "inactiveLink dataStructureSelectedFilter");
	$(newFilter).removeAttr("href");
	
	publishedDataStructureResetPagination();
	publishedDataStructureResetSort();
	publishedDataStructureSearch();
	
}

// This function casues the result to jump to a page given by the page text field
// This text field is defined in elementList.jsp
// This function also checks to make sure the given page input is valid
function publishedDataStructureCheckPageField(maxPage) {
	var desiredPage = document.getElementById("publishedDataStructurePaginationJump").value;
	maxPage = Math.ceil(maxPage);
	if (!isNaN(desiredPage)) {
		if (desiredPage <= maxPage && desiredPage > 0 && (Math.ceil(desiredPage) / desiredPage == 1)) {
			publishedDataStructurePagination.page = desiredPage;
			publishedDataStructureSearch();
		}
		else {
			document.getElementById("publishedDataStructurePaginationJump").value = publishedDataStructurePagination.page;
		}
		
	}
	else {
		document.getElementById("publishedDataStructurePaginationJump").value = publishedDataStructurePagination.page;
	}
}

// Sets the global pagination values back to their default values
function publishedDataStructureResetPagination() {
	publishedDataStructurePagination.page = 1;
	publishedDataStructurePagination.pageSize = 10;
}

function publishedDataStructureResetSort() {
	publishedDataStructurePagination.sort = "title";
	publishedDataStructurePagination.ascending = false;
}

// Function called when user clicks a table head to sort a column.
function publishedDataStructureSetSort(sortIn) {
	publishedDataStructureResetPagination();
	if (sortIn == publishedDataStructurePagination.sort) {
		publishedDataStructurePagination.ascending = !publishedDataStructurePagination.ascending;
	}
	else {
		publishedDataStructurePagination.sort = sortIn;
	}
	publishedDataStructureSearch();
};