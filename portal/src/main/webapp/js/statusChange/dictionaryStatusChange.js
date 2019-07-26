
//Declare global variables 
var dialogOne;
var currentAction = $("#currentAction").val();
var $statusChangeDiv = $("#statusChange");
var statusChangeInner = $("#statusChangeInner");
var currentId = $("#currentId").val();
var isAdmin = $("#inAdmin").val();

var numberOfAffectedDE = $("#numberOfAffectedDE").val();
var requestedStatusChange = $("#requestedStatusChange").val().trim();

//Declare action variables
var firstFsAction = currentAction+"!formStructureStatusChange.ajax";
var nextFsAction = currentAction +"!attachedDEStatusChange.ajax";
var submitFsAction = currentAction +"!submitFSStatusChange.ajax";

var firstDeAction = "dataElementAction!dataElementStatusChange.ajax";
var submitDeAction = "dataElementAction!submitDEStatusChange.ajax";

function saveHistoryFsChangeStatus(statusId, location, html) {

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

				$.ajax(nextFsAction, {
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
			$("#bricsDialog_0").dialog("destroy");
			$(this).dialog("destroy");
		}
	}
	var backFinishCloseButtons = {
		'Back' : function() {

				$.ajax(firstFsAction, {
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

				$.ajax(submitFsAction, {
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
				$("#bricsDialog_0").dialog("destroy");
				$(this).dialog("destroy");
			}
		},
		Close : function() {
			defaultPublicationInterface(location, html);
			$(this).dialog("destroy");
		}

	};

	var finishOrCloseButtons = {
		'Finish' : function() {

			var statusReason = $("#statusReason").val();
			var reason = $("#reason").val();
			var attachedDEReason = $("#attachedDEReason").val();

			if (validateRequiredFields(statusReason, reason, attachedDEReason)) {

				$.ajax(submitFsAction, {
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
				$(this).dialog("destroy");
			}
		},
		'Close' : function() {
			defaultPublicationInterface(location, html);
			$(this).dialog("close");
		}
	}

	// this is the first ajax call made to get the status change dialog
	$.ajax(firstFsAction, {
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

//DataElement status change function
function saveHistoryDeChangeStatus(publicationId) {

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

				$.ajax(submitDeAction, {
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
							var eventTable = $("#eventTable").idtApi('getTableApi');
							//dt.draw();
							eventTable.ajax.reload();
						}
					}
				});
				$(this).dialog("close");
			}
		},
		'Close' : function() {
			$(this).dialog("destroy");
		}
	}

	//make an ajax call to get the status change dialog
	$.ajax(firstDeAction, {
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

//this function is included to make sure the right supporting documents(FS/DE supporting documents)
//are displayed on the jquery table
function refreshSupportDocTable(){
		var dt = $('#documentationTableTable').idtApi('getTableApi');
		var jsonData ;
		$.ajax({
			type: "POST",
			cache: false,
			url: currentAction +"!refreshSupportDoc.ajax",
			success: function(data) {
				var documentationLimit = $(data).find("#documentationLimit").val();
	            dt.ajax.reload();   
				if(documentationLimit > 0) { //need disable upload document button when document limit has been reached
					var rowCount = dt.rows().count();
					if(rowCount >= documentationLimit) {
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
		statusReason = $.trim(statusReason);
		if (statusReason.length == 0) {
			$("#validateReason").show();
			valid = false;
		} else {
			$("#validateReason").hide();
		}
	}

	//undefined here means a page without an approval/rejection reason is displayed	
	if (typeof reason !== 'undefined') {
		reason = $.trim(reason);
		if (reason.length == 0) {
			$("#validateStatusReason").show();
			valid = false;
		} else {
			$("#validateStatusReason").hide();
		}
	}

	//undefined here means the page displayed is not data element status change page	
	if (typeof attachedDEReason !== 'undefined') {
		attachedDEReason = $.trim(attachedDEReason);
		if (attachedDEReason.length == 0) {
			$("#validateReason").show();
			valid = false;
		} else {
			$("#validateReason").hide();
		}
	}

	return valid;
}

function ConfirmationDialogDelete(dialogType, msgText, yesBtnText, noBtnText, action, isFormSubmission, width, title) {
	var titleText = title || null;
	var documentationLimit = $("#documentationLimit").val();
	var dt = $('#documentationTableTable').idtApi('getTableApi');
	var dlgId = $.ibisMessaging(
		"dialog", 
		dialogType, 
		msgText,
		{
			id: 'deleteDialog',
			container: 'body',
			buttons: [{
				id: "yesBtnA",
				text: yesBtnText, 
				click: _.debounce(function() {
					$(this).siblings().find("#yesBtnA").prop("disabled", true);
					var selectedRows = $('#documentationTableTable').idtApi('getSelectedOptions');
					//var rows = dt.rows('.selected').data();
				    var rowsIDs = [];
				    for (var i = 0; i < selectedRows.length; i++) {
				        rowsIDs.push(selectedRows[i]);
				    }      	   		
					var data = new FormData();
					data.append('supportingDocName', rowsIDs.toString());
						$.ajax({
							type: "POST",
							data: data,	
							processData : false,
							url: action,
							contentType : false,
							success: function(data) {

								//dt.ajax.reload();
								dt.rows('.selected').remove().draw(false);
								selectedRows.length = 0;
								$('#documentationTableTable').idtApi('deselectAll');
								dt.buttons().disable();
									
								if(documentationLimit > 0) { //need disable upload document button when document limit has been reached
									var rowCount = dt.rows().count();
									if(rowCount >= documentationLimit) {
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
							
								$.ibisMessaging("close", {type: 'dialog'});
							},
							error: function(jqXHR, textStatus, errorThrown) {
								console.log(errorThrown)
							}
						});
				}, 1000, true)
			},
			{
				text: noBtnText,
				click: function() {
 				    $.ibisMessaging("close", {id: dlgId});					
				}
			}],
			modal: true,
			width: width,
			title: titleText
		}
	);
}

function viewRelatedDE(){
	
	var $attachedDEDiv = $("#dataElementsDialogue");
	var action = currentAction+"!getDataElementsList.action";
            $("#dataElementListTable").idtTable({
            	idtUrl: action,
            	filterData:{
            		inAdmin: isAdmin
            	},
            	columns: [
            		{
            			title: "Title",
            			data: "title",
            			parameter: "titleViewLink",
            			render: function(data, type, row, full) {
            				$('.lightbox').fancybox();
            				return data;
            			}
            		},
            		{
            			title: "Variable Name",
            			data: "variableName",
            			parameter: "variableName"
            		},
            		{
            			title: "Type",
            			data: "type",
            			parameter: "type"
            		},
            		{
            			title: "Modified Date",
            			data: "modifiedDate",
            			parameter: "modifiedDate"
            		},
            		{
            			title: "Status",
            			data: "status",
            			parameter: "status"
            		}
            	]
            });
            
            // remove all current rows from dataElement table
           //$("#dataElementListTable").Datatable().ajax.reload();
				
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
			

}

