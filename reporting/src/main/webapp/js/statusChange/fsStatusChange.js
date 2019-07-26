
//Declare global variables 
var dialogOne;
var $statusChangeDiv = $("#statusChange");
var statusChangeInner = $("#statusChangeInner");
var currentId = $("#currentId").val();

var numberOfAffectedDE = $("#numberOfAffectedDE").val();
var requestedStatusChange = $("#requestedStatusChange").val().trim();

//Declare action variables
var firstAction = "dataStructureAction!formStructureStatusChange.ajax";
var nextAction = "dataStructureAction!attachedDEStatusChange.ajax";
var submitAction = "dataStructureAction!submitFSStatusChange.ajax";

function saveHistoryChangeStatus(statusId, location, html) {

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
	
	//Defined three buttons nextOrCloseButtons,backFinishCloseButtons,finishOrCloseButtons
	//nextOrCloseButtons and backFinishCloseButtons are used if the status change involve status change to attached data elements
	var nextOrCloseButtons = {
		'Next' : function() {

			var statusReason = $("#statusReason").val();
			var reason = $("#reason").val();
			var attachedDEReason = $("#attachedDEReason").val();

			if (validateRequiredFields(statusReason, reason, attachedDEReason)) {

				$.ajax(nextAction, {
					"type" : "POST",
					"data" : {
						"statusReason" : statusReason,
						"reason" : reason,
						"inDataElementPage" : true,
					},
					"success" : function(data) {
						statusChangeInner.html(data);
						refreshSupportDocTable();					
						dialogOne.dialog('option', 'buttons',backFinishCloseButtons);
					}
				});
			}
		},
		'Close' : function() {
			defaultPublicationInterface(location, html);
			$(this).dialog("close");
		}
	}
	var backFinishCloseButtons = {
		'Back' : function() {

				$.ajax(firstAction, {
					"type" : "POST",
					"data" : {"statusId" : statusId,
						"backNavigation":true,
						"inDataElementPage" : false},

					"success" : function(data) {
						statusChangeInner.html(data);
						dialogOne.dialog('option', 'buttons', nextOrCloseButtons);
						refreshSupportDocTable();
					}
				});

		},
		'Finish' : function() {

			var statusReason = $("#statusReason").val();
			var reason = $("#reason").val();
			var attachedDEReason = $("#attachedDEReason").val();

			if (validateRequiredFields(statusReason, reason, attachedDEReason)) {

				$.ajax(submitAction, {
					"type" : "POST",
					"data" : {
						"attachedDEReason" : attachedDEReason,
						"statusId" : statusId,
						"currentId" : currentId
					},
					"success" : function(data) {
						if(requestedStatusChange == 'true'){
							updatePublicationInterface();
							submitForm('dataStructureAction!approve.action');
						}
						else{
							publicationAction(statusId);
						}
					
					}
				});
				$(this).dialog("close");
			}
		},
		Close : function() {
			defaultPublicationInterface(location, html);
			$(this).dialog("close");
		}

	};

	var finishOrCloseButtons = {
		'Finish' : function() {

			var statusReason = $("#statusReason").val();
			var reason = $("#reason").val();
			var attachedDEReason = $("#attachedDEReason").val();

			if (validateRequiredFields(statusReason, reason, attachedDEReason)) {

				$.ajax(submitAction, {
					"type" : "POST",
					"data" : {
						"statusReason" : statusReason,
						"statusId" : statusId,
						"currentId" : currentId,
						"reason" : reason,
					},
					"success" : function(data) {
						
						if(requestedStatusChange == 'true' && (statusId ==2 || statusId==0 )){
							//approve publish
							if(statusId==2){
								updatePublicationInterface();
								submitForm('dataStructureAction!approve.action');
							}
							else{
								updatePublicationInterface();
								submitForm('dataStructureAction!deny.action');
							}				
						}
						else{
							publicationAction(statusId);
						}
						
					}
				});
				$(this).dialog("close");
			}
		},
		'Close' : function() {
			defaultPublicationInterface(location, html);
			$(this).dialog("close");
		}
	}

	// this is the first ajax call made to get the status change dialog
	$.ajax(firstAction, {
		"type" : "POST",
		"data" : {"statusId" : statusId},
		"success" : function(data) {
			
			statusChangeInner.html(data);
			refreshSupportDocTable();
			
			// display DE page if status change of form structure affects data element status
			//only publication of FS will have the data element status
			if (numberOfAffectedDE>0 && statusId ==2){
				dialogOne.dialog("open");
				dialogOne.dialog('option', 'buttons', nextOrCloseButtons);
			}
			else{
				dialogOne.dialog("open");
				dialogOne.dialog('option', 'buttons', finishOrCloseButtons);
			}

		}
	});
}

//this function will refresh the admin status change table once
//FS status change is made
function refreshEventLogTable() {
	$.post("dataStructureAction!refreshEventLogTable.ajax", function(data) {

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

//this function is included to make sure the right supporting documents(FS/DE supporting documents)
//are displayed on the jquery table
function refreshSupportDocTable(){
	$.post("dataStructureAction!refreshSupportDoc.ajax", function(data) {
		
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
	
	$(".reqPubId").removeAttr('href');
	$('.reqPubId').html('Processing.....');

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

function defaultPublicationInterface(location, html) {

	$("#reqPubId").attr("href", location);
	$('#reqPubId').html(html);

	reEnableLinks();
	

}

function reEnableLinks(){
	
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

	//undefined here means the page displayed is not data element status change page
	if (typeof attachedDEReason !== 'undefined') {
		if (attachedDEReason.length == 0) {
			$("#validateReason").show();
			valid = false;
		}
	}

	return valid;
}

function viewAffectedDE(){
	
	var $attachedDEDiv = $("#dataElementsDialogue");
	
	$.ajax("dataStructureAction!listAttachedDEs.ajax", {
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
				title : "List Of Affected Data Elements",
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

