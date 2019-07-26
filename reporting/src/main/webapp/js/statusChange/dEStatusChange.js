
//Declare global variables 
var dialogOne;
var $statusChangeDiv = $("#statusChange");
var statusChangeInner = $("#statusChangeInner");
var currentId = $("#currentId").val();

var requestedStatusChange = $("#requestedStatusChange").val().trim();

//Delare action variables
var firstAction = "dataElementAction!dataElementStatusChange.ajax";
var submitAction = "dataElementAction!submitDEStatusChange.ajax";

function saveHistoryChangeStatus(publicationId) {

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

				$.ajax(submitAction, {
					"type" : "POST",
					"data" : {
						"statusReason" : statusReason,
						"publicationId" : publicationId,
						"reason" : reason,
					},
					"success" : function(data) {
						
						if(requestedStatusChange == 'true'){
							//approve publish
							if(publicationId==2){
								submitForm('dataElementAction!approve.action');
							}
							else{
								submitForm('dataElementAction!deny.action');
							}
						}
						else{
							publicationAction(publicationId);
							refreshEventLogTable();
						}
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
	$.ajax(firstAction, {
		"type" : "POST",
		"data" : {"publicationId" : publicationId},
		"success" : function(data) {
			statusChangeInner.html(data);
			
			refreshSupportDocTable();
			
			dialogOne.dialog("open");
			dialogOne.dialog('option', 'buttons', buttons);

		}
	});
}

function refreshEventLogTable() {
	$.post("dataElementAction!refreshEventLogTable.ajax", function(data) {

		var winIDT = IDT;
		var jsonData;
		var $table = $('#eventTable').find("table");

		// get json data from the jsp
		jsonData = JSON.parse($(data).find('script[type="text/json"]').html());
		aaData = jsonData.aaData;
		aoColumns = jsonData.aoColumns;

		// remove all current rows from documentation table
		winIDT.removeAllRows($table);
		// add new rows to table
		winIDT.addRow($table, aaData);

	});
}

function refreshSupportDocTable(){
	$.post("dataElementAction!refreshSupportDoc.ajax", function(data) {
		
		var supportDocSize = $(data).find("#eventLogSupportDocSize").val();
		var documentationLimit =$(data).find("#documentationLimit").val();

		var winIDT = IDT;
		var jsonData;
		var $table = $('#documentationTable').find("table");

		// get json data from the jsp
		jsonData = JSON.parse($(data).find('script[type="text/json"]').html());
		aaData = jsonData.aaData;
		aoColumns = jsonData.aoColumns;

		// remove all current rows from documentation table
		winIDT.removeAllRows($table);
		// add new rows to table
		winIDT.addRow($table, aaData);
		
		//refresh the add document field	
		if(parseInt(documentationLimit) > 0) { //need disable upload document button when document limit has been reached
			if(parseInt(supportDocSize) >= parseInt(documentationLimit)) {
				$("#addDocBtnDiv").addClass("disabled");
				$("#addDocBtnDiv").prop("title", "The maximum amount of documents allowed to be uploaded is " + documentationLimit + ".");
				$("#addDocBtn").prop("disabled", "true");
				$("#selectAddDocDiv").addClass("hidden");
			} else { //need to enable upload document button when the document limit has not been reached.
				$("#addDocBtnDiv").removeClass("disabled");
				$("#addDocBtnDiv").removeAttr("title");
				$("#addDocBtn").removeAttr("disabled");
				$("#selectAddDocDiv").removeClass("hidden");
			}
		}
	
	});
}

function updatePublicationInterface() {

	$("#reqPubId").removeAttr('href');
	$('#reqPubId').html('Processing.....');

	//disable links
	$('a').attr("temp-html", function() {
		return $(this).attr("href");
	});
	$('a').removeAttr('href');
	$('a').attr("temp-onClick", function() {
		return $(this).attr("onclick");
	});
	$('a').removeAttr('onclick');
}

function defaultPublicationInterface() {

	// re-enable links
	$('a').attr("href", function() {

		return $(this).attr("temp-html");
	});
	$('a').attr("onclick", function() {
		return $(this).attr("temp-Onclick");
	});

	$("a").removeAttr('temp-html');
	$("a").removeAttr('temp-onClick');

}
function validateRequiredFields(statusReason, reason, attachedDEReason) {

	var valid = true;
	if (typeof statusReason !== 'undefined') {
		if (statusReason.length == 0) {
			$("#validateReason").show();
			valid = false;
		}
	}

	//undefined here means a page without an approval/rejection reason is displayed
	if (typeof reason !== 'undefined') {
		if (reason.length == 0) {
			$("#validateStatusReason").show();
			valid = false;
		}
	}

	return valid;
}

function viewSelectedDEs(){
	
	var $attachedDEDiv = $("#dataElementsDialogue");
	
	$.ajax("dataElementAction!listBulkDEs.ajax", {
		"type" : "POST",
		"success" : function(data) {
			
			var $table = $("#dataElementListContainer table"); 
			var winIDT = IDT;
			// get json data from the jsp
            jsonData = JSON.parse($(data).find('script[type="text/json"]').html());
            aaData = jsonData.aaData;
            
            // remove all current rows from dataElement table
			winIDT.removeAllRows($table);
            // add new rows to table
            winIDT.addRow($table, aaData);
				
			var dialogOne = $attachedDEDiv.dialog({
				
				autoOpen :false,
				modal : true,
				height : 600,
				width : 1000,
				draggable : false,
				resizable : false,
				title : "List Of Selected Data Elements",
				buttons: {
				       	Cancel: function() {
				       		dialogOne.dialog( "close" );
				        }
				      }
			});
			
			dialogOne.dialog("open");			
			$('.lightbox').fancybox();
		}
	});
}
