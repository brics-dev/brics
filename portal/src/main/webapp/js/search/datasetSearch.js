// Contains functions for a dataset search and defines datasetPagination Object

// dataset Search Object
// This is a global object containing the properties needed for search
var datasetPagination = new Object();
datasetPagination.page = 1;
datasetPagination.pageSize = 10;
datasetPagination.sort = "name";
datasetPagination.ascending = false;
datasetPagination.namespace = "";

// review: translated
var handleCheckbox = function(model) {
	console.log("handleCheckbox()");
	var $table = $("#datasetIdtTable");
	var checkbox = $table.find('input[type="checkbox"]');

	$table.parents(".dataTableContainer").on("change", ".fg-toolbar select", function() {
		$table.idtApi("draw");
		$table.idtApi("deselectAll");
	});

	var statusFilterVal = $("#Status\\:\\ All").val();
	statusFilterVal = (statusFilterVal == "") ? "all" : statusFilterValue;
	if (statusFilterVal == "all") {
		$table.idtApi("disableSelection");
		$table.idtApi("deselectAll");

		$table
						.find('input[type="checkbox"]')
						.parent()
						.qtip(
										{
											content: 'You are not able to perform administrative actions on datasets of different statuses. Please filter by status to perform an action on datasets with the same status.'
										});

		$('#datasetStatusSelect')
						.parent()
						.qtip(
										{
											content: 'You are not able to perform administrative actions on datasets of different statuses. Please filter by status to perform an action on datasets with the same status.'
										});

		$('#datasetStatusSelect').attr("disabled", "disabled");
		$('#accept').attr("disabled", "disabled");

		$("#buttonDiv").addClass("disabled");
	}

	else if (dataTableFilters.filterStatus == "deleted") {
		$table.idtApi("disableSelection");
		$table.idtApi("deselectAll");

		$table
						.find('input[type="checkbox"]')
						.parent()
						.qtip(
										{
											content: 'You are not able to perform administrative actions on datasets of this status. Please filter by a different status.'
										});

		$('#datasetStatusSelect')
						.parent()
						.qtip(
										{
											content: 'You are not able to perform administrative actions on datasets of this status. Please filter by a different status.'
										});

		$('#datasetStatusSelect').attr("disabled", "disabled");
		$('#accept').attr("disabled", "disabled");

		$("#buttonDiv").addClass("disabled");
	}
	else {
		$table.idtApi("enableSelection");

		var selectedDatasetIds = $table.idtApi("getSelected");

		if (selectedDatasetIds.length == 0) {
			$table.find('input[type="checkbox"]').parent().qtip('disable', true);
			$('#datasetStatusSelect').parent().qtip('disable', true);

			$('#datasetStatusSelect').attr("disabled", "disabled");
			$('#accept').attr("disabled", "disabled");
		}

		if (dataTableFilters.filterStatus == "private") {
			$('#datasetStatusSelect')
							.empty()
							.prepend(
											'<option selected value="Share">Share</option><option selected value="Archive">Archive</option><option selected value="Delete">Delete</option>');
			$("#datasetStatusSelect option:first-child").attr("selected", "selected");
		}
		else if (dataTableFilters.filterStatus == "shared") {
			$('#datasetStatusSelect').empty().append('<option selected value="Archive">Archive</option>');
		}
		else if (dataTableFilters.filterStatus == "archived") {
			$('#datasetStatusSelect').empty().append('<option selected value="Delete">Delete</option>');
		}
		else if (dataTableFilters.filterStatus == "requestedDeletion"
						|| dataTableFilters.filterStatus == "requestedSharing"
						|| dataTableFilters.filterStatus == "requestedArchive"
						|| dataTableFilters.filterStatus == "sharedRequestedArchive") {
			$('#datasetStatusSelect')
							.empty()
							.prepend(
											'<option selected value="Approve Request">Approve Request</option><option selected value="Reject Request">Reject Request</option>');
			$("#datasetStatusSelect option:first-child").attr("selected", "selected");
		}
		else if (dataTableFilters.filterStatus == "errors" || dataTableFilters.filterStatus == "uploading"
						|| dataTableFilters.filterStatus == "loadingData") {
			$('#datasetStatusSelect').empty().append('<option selected value="Delete">Delete</option>');
		}
	}
}

/**
 * Handles click events on dataset checkboxes, changes the status update
 * dropdown and the change status button
 */
function updateStatusOptions(apiRow, api) {
	var id = apiRow.id();
	var rowData = apiRow.data();
	var status = rowData.status;
	var isRequest = rowData.requestStatus !== "";
	var filterStatus = $("#Status\\:\\ All").val();
	// default filterStatus because of the way we build filters is empty string
	filterStatus = (filterStatus == "") ? "all" : filterStatus;
	var $table = $("#datasetIdtTable");
	var $statusSelect = $('#datasetStatusSelect');
	var selectedOptionsLength = $table.idtApi("getSelected").length;

	if (selectedOptionsLength !=0) {
		api.rows().every(function(rowIndex, tableLoopCounter, rowLoopCounter) {
			var data = this.data();
			var currentStatus = data.status;
			var currentIsRequest = data.requestStatus !== "";
			if (!(isRequest && currentIsRequest) && (status != currentStatus || isRequest != currentIsRequest)) {
				$table.idtApi("disableRow", this);
			}
		});

		if (!isRequest) {
			if (status == "Private") {
				$statusSelect
								.removeProp('disabled')
								.removeAttr("disabled")
								.empty()
								.append(
												'<option selected value="">- Select One -</option><option selected value="Share">Share</option><option selected value="Archive">Archive</option><option selected value="Delete">Delete</option>');
				$("#datasetStatusSelect option:first-child").attr("selected", "selected");
				enableStatusButton();
			}
			else if (status == "Shared") {
				$statusSelect.removeProp('disabled').removeAttr("disabled");
				$statusSelect.empty().append('<option selected value="Archive">Archive</option>');
				$("#datasetStatusSelect option:first-child").attr("selected", "selected");
				enableStatusButton();
			}
			else if (status == "Deleted") {
				$statusSelect.prop('disabled', true).addAttr("disabled", "disabled");
				$statusSelect.empty().append('<option selected value="">No Action</option>');
				disableStatusButton();
			}
			else if (status == "Archived") {
				$statusSelect.removeProp('disabled').removeAttr("disabled");
				$statusSelect.empty().append('<option selected value="Delete">Delete</option>');
				enableStatusButton();
			}
			else if (status == "Loading Data") {
				$statusSelect.prop('disabled', true).attr("disabled", "disabled");
				$statusSelect.empty().append('<option selected value="">No Action</option>');
				disableStatusButton();
			}
			else if (status == "Uploading") {
				$statusSelect.removeProp('disabled').removeAttr("disabled");
				$statusSelect.empty().append('<option selected value="Delete">Delete</option>');
				enableStatusButton();
			}
			else if (status == "Error During Load") {
				$statusSelect.removeProp('disabled').removeAttr("disabled");
				$statusSelect.empty().append('<option selected value="Delete">Delete</option>');
				enableStatusButton();
			}
		}
		else {
			$statusSelect.removeProp('disabled').removeAttr("disabled");
			$statusSelect
							.empty()
							.append(
											'<option selected="selected" value="Approve Request">Approve Request</option><option value="Reject Request">Reject Request</option>');
			$statusSelect.prop("value", "Approve Request");
			enableStatusButton();
		}
	}
	else {
		console.log("here");
		$('#accept').prop("disabled", true);
		$("#buttonDiv").addClass("disabled");
		$statusSelect.prop('disabled', true).attr("disabled", "disabled");
	}
}

/**
 * Resets the option dropdown and button to its default status
 */
function resetUI() {
	$('#datasetStatusSelect').prop('disabled', true).attr("disabled", "disabled").empty().append(
					'<option selected value="">Select a Dataset</option>');
	disableStatusButton();
}

function enableStatusButton() {
	$("#accept").removeClass("disabled").prop("disabled", false);
	$("#buttonDiv").removeClass("disabled");
}

function disableStatusButton() {
	$("#accept").addClass("disabled").prop("disabled", true);
	$("#buttonDiv").addClass("disabled");
}

function getSelectedOptions() {
	console.log("getSelectedOptions()");
	var selectedDatasetIds = $("#datasetIdtTable").idtApi("getSelected");
	$('#selectedDatasets').val(selectedDatasetIds.join(","));

};
