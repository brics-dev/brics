function uploadDocument() {
	var validationActionName = $("#validationActionName").val();
	var fileName = $("#fileNameDisplay").text();
	var newVar = $("#uploadFileName").val();

	if (fileName && fileName.length > 0) {
		if (fileName != "No file selected.") {
			$("#uploadFileName").val(fileName);
		} else {
			$("#uploadFileName").val("");
		}

	}

	// this determines if the dataform will work with an ajax call
	// if formdata is undefined you know it's an old browser
	if (typeof FormData != "undefined") {
		var fileAddedToFormByUser = "";
		var data = new FormData();
		var url = $('#url').val();
		var isEdit = $('#isEditingDoc').val();

		if (typeof url === 'undefined') {
			$("#saveFile").prop("disabled", true);
			if (document.addDocForm.uploadSupportDoc.files.length != 0) {
				fileAddedToFormByUser = document.addDocForm.uploadSupportDoc.files[0].name;
				$("#uploadFileName").val(fileAddedToFormByUser);

				data.append('uploadSupportDoc',
						document.addDocForm.uploadSupportDoc.files[0]);
				data.append('uploadSupportDocFileName', fileAddedToFormByUser);
			}
			data.append('uploadFileName', $('#uploadFileName').val());

		} else {
			if (url.trim().length > 0 && !url.match(/^[a-zA-Z]+:\/\//)) {
				url = 'http://' + url;
				$("#url").val(url);
			}
			data.append('url', url);
		}

		var selectedDocNames = $('#documentationTableTable').idtApi('getSelectedOptions');
		
		data.append('addDocSelect', $('#addDocSelect').val());
		data.append('isEditingDoc', isEdit);
		data.append('supportingDocType', $('#supportingDocType').val());
		console.log($('#supportingDocType').val());
		data.append('supportingDocDescription', $('#supportingDocDescription')
				.val());
		data.append('supportingDocName', selectedDocNames);
		
		var type = $("#supportingDocType").find('option:selected').text();
	if(type==='Publication'){
		data.append('pubEntry.firstAuthor.firstName', $('#faFirstName').val());
		data.append('pubEntry.firstAuthor.mi',   $('#faMiddleName').val());
		data.append('pubEntry.firstAuthor.lastName' , $("#faLastName").val());
		data.append('pubEntry.firstAuthor.email' , $("#faEmail").val());
		data.append('pubEntry.firstAuthor.orgName' , $("#faOrg").val());
		data.append('pubEntry.lastAuthor.firstName' , $("#laFirstName").val());
		data.append('pubEntry.lastAuthor.mi' , $("#laMiddleName").val());
		data.append('pubEntry.lastAuthor.lastName' , $("#laLastName").val());
		data.append('pubEntry.lastAuthor.email' ,  $("#laEmail").val());
		data.append('pubEntry.lastAuthor.orgName' ,  $("#laOrg").val());
		data.append('pubEntry.title' , $("#docTitle").val());
		data.append('pubEntry.pubmedId' , $("#pubMedId").val());
		data.append('pubEntry.description', $("#supportingDocDescription").val());
		data.append('pubEntry.publicationDate', $("#pubDate").val());
	}
	
	if(type==='Software'){
		data.append('softwareName', $('#softwareName').val());
		data.append('version', $('#version').val());
	}
	var dt = $('#documentationTableTable').idtApi('getTableApi');
		$.ajax({
					url : validationActionName + "!uploadDocumentation.ajax",
					data : data,
					cache : false,
					processData : false,
					contentType : false,
					type : 'POST',
					success : function(returnData) {
						if (returnData == "success") {
							$("#selectAddDocDiv").toggle();
							dt.ajax.reload();
							$.fancybox.close();
						} else {
							
							$.fancybox(returnData);
							if (isEdit != "true"
									&& fileAddedToFormByUser != ""
									|| (isEdit == "true"
											&& (fileAddedToFormByUser != ""
													&& newVar != "" && fileAddedToFormByUser != newVar) || newVar == "")) {
								$('#uploadFileName').val(null);
								$("#fileNameDisplay").text("No file selected.");
								if (typeof setAddFileDialogMode !== "undefined") {
									// type is the previously-selected document type
							    	setAddFileDialogMode(type);
								}
							}
						}
					}
				});
	} else {
		ieFrameSubmit("addDocForm", "iframeRefreshDocumentTable()");
		$("#selectAddDocDiv").toggle();
		$.fancybox.close();
	}
}

function editDocumentation() {
	var actionName = $("#actionName").val();
	var selectedDocNames = IDT
			.getSelectedOptions($("#documentationTable table"));

	$.ajax({
		type : "POST",
		cache : false,
		url : actionName + "!editDocumentation.ajax?supportingDocName=" + encodeURIComponent(selectedDocNames),
		success : function(data) {
			$.fancybox(data);
		}
	});
}

//function deleteDocumentation() {
//	var actionName = $("#actionName").val();
//	var msgText = "Are you sure you want to delete the item(s)?";
//	var yesBtnText = "Delete";
//	var noBtnText = "Do Not Delete";
//	var $table = $("#documentationTable table");
//	var selectedDocNames = IDT
//			.getSelectedOptions($("#documentationTable table"));
//	var action = actionName + "!removeDocumentations.action?supportingDocName="
//			+ selectedDocNames;
//
//	EditConfirmationDialog("warning", msgText, yesBtnText, noBtnText, action,
//			true, "400px", "Confirm Deletion", $table, selectedDocNames);
//}

function refreshDocumentationTable() {
	var actionName = $("#actionName").val();
	var $table = $('#documentationTable').find("table");
	var action = actionName + "!documentationRefresh.ajax";
	refreshDocumentationIBISTable(action);
}

function iframeRefreshDocumentTable() {
	refreshDocumentationTable();
}

//helper method that refreshes tables based off the response
function refreshDocumentationIBISTable(action){
	var documentationLimit = $("#documentationLimit").val();
	var dt = $('#documentationTableTable').idtApi('getTableApi');
	var jsonData ;
	$.ajax({
		type: "POST",
		cache: false,
		url: action,
		success: function(data) {
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

/**
 * Hide the file input field and replace with a browse button and pre-populated
 * file name. This approach can be used in editing forms with existing file.
 */
function convertFileUpload(input, initialFileName) {
	if (input.css("display") != "none") {
		input.css("display", "none");
		
		var newInput = '<input type="button" id="fileInputBtn" value="Browse..." style="padding: 2px 8px" />' + 
					'&nbsp;<span id="fileNameDisplay"></span>';
		input.after(newInput);
		
		if (!initialFileName || initialFileName.trim().length == 0) {
			$("#fileNameDisplay").text("No file selected.");
		} else {
			$("#fileNameDisplay").text(initialFileName);
		}
		
		$("#fileInputBtn").unbind("click");
		
		$("#fileInputBtn").click(function() {
			input.click();
		});

		input.change(function() {
			var fileName = input.val();
			
			// Stripped off the file path added in the Chrome browser
			if (/chrome/.test(navigator.userAgent.toLowerCase())) {
				fileName = fileName.split('/').pop().split('\\').pop();
			}
			$("#fileNameDisplay").text(fileName);
		});
	}
}

function EditConfirmationDialog(dialogType, msgText, yesBtnText, noBtnText, action, isFormSubmission, width, title, selectedRows) {
	var titleText = title || null;
	var documentationLimit = $("#documentationLimit").val();
	var dt = $('#documentationTableTable').idtApi('getTableApi');
	var selectedRows = $('#documentationTableTable').idtApi('getSelectedOptions');
	var dlgId = $.ibisMessaging(
		"dialog", 
		dialogType, 
		msgText,
		{
			id: 'test',
			container: 'body',
			buttons: [{
				id: "yesBtnA",
				text: yesBtnText, 
				click: _.debounce(function() {
					$(this).siblings().find("#yesBtnA").prop("disabled", true);
						$.ajax({
							type: "POST",
							cache: false,
							url: action,
							data: {isMetaStudyEdit: true},
							success: function(data) {
								 
								dt.rows('.selected').remove().draw(false);
								selectedRows.length = 0;
									
								if(documentationLimit > 0) {
									var rowCount = dt.rows().count();
									if(rowCount < documentationLimit) { //need to enable upload document button when the document limit has not been reached.
										$("#addDocBtnDiv").removeClass("disabled");
										$("#addDocBtnDiv").removeAttr("title");
										$("#addDocBtn").removeAttr("disabled");
										$("#selectAddDocDiv").removeClass("hidden");
									}
								} 
								$.ibisMessaging("close", {type: 'dialog'});
								console.log(dlgId);
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

/**
 * THIS ENDS THE META STUDY DATA EDIT/UPLOAD FUNCTIONALITY.
 */

function ellipsisExpandCollapse(element) {
	var $this = $(element);
	$this.parent().toggle();
	if ($this.text() == "...") {
		$this.parent().next().toggle();
	}
	else {
		$this.parent().prev().toggle();
	}
}


/**
 * When user input exceeds the length limit of the text area, display an alert message and also truncate 
 * the text to the limit.
 */
function checkTextareaMaxLength(textareaId, maxLength) {
	var text = _.escape($("#"+textareaId).val());

	if (text.length > maxLength) { 
		alert("This textarea has a character limit of " + maxLength + 
			".  You have entered more than " + maxLength + " characters and the text has been truncated for you.");
		
		// Trim the field current length over the maxlength.
		$("#"+textareaId).val(_.unescape(text.slice(0, maxLength)));
	}
}

var documentDetailsDialogConfig = {
		buttons : [{
			text: "Close",
			click : function() {
				$(this).dialog( "close" );
				$(this).dialog( "destroy" );
			}
		}],
		width: $(window).width() * 0.5
	};
	
	function showDocumentDetails(publicationName) {
		var actionName = $("#actionName").val();
		$.ajax({
			url : actionName + "!viewDocumentation.ajax",
			type: "post",
			data: {supportingDocName: publicationName},
			success : function(data) {
				$("#documentationDetailsDialog").html(data).dialog(documentDetailsDialogConfig);
			}
		});
	}