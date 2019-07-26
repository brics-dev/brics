// Contains common functions used by meta study modules.

/**
 * Create a basic confirmation dialog.
 * 
 * @param dialogType -
 *            warning, info, error ...
 * @param msgText -
 *            message text to be displayed
 * @param yesBtnText -
 *            text for a Yes like button
 * @param noBtnText -
 *            text for a No like button, the default behavior is to close the
 *            dialog
 * @param action -
 *            action to be performed.
 * @param width -
 *            width of the dialog.
 * @param isFormSubmission -
 *            If true we call submitForm with the action, otherwise just treat
 *            it as a link.
 * @param title -
 *            OPTIONAL - the dialog title (appears at the top of the dialog)
 */
function confirmationDialog(dialogType, msgText, yesBtnText, noBtnText, action, isFormSubmission, width, title) {
	var titleText = title || null;
	var dlgId = $.ibisMessaging(
			"dialog", 
			dialogType, 
			msgText,
			{
				buttons: [{
					id: "yesBtnA",
					text: yesBtnText, 
					click: _.debounce(function() {
						$(this).siblings().find("#yesBtnA").prop("disabled", true);
						if (isFormSubmission) {
							submitForm(action);
						} else {
							window.location.href = action;
						}
					}, 1000, true)
				},
				{
					text: noBtnText,
					click: function() {
						//$.ibisMessaging("close", {id: dlgId});
						$(this).dialog( "close" );
					}
				}],
				modal: true,
				width: width,
				title: titleText
			}
	);
}

function confirmationEditDialog(dialogType, msgText, yesBtnText, noBtnText, action, isFormSubmission, width, title, $table, selectedRows) {
	var titleText = title || null;
	var dt = $('#metaStudyDataListTable').idtApi('getTableApi');
	var selectedRows = $('#metaStudyDataListTable').idtApi('getSelectedOptions');
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
									$.ibisMessaging("close", {id: dlgId});
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

function createMetaStudy(action) {
	var msgText = "Are you sure you want to create the meta study?";
	var yesBtnText = "Create";
	var noBtnText = "Continue Editing";
	
	if (!action || action.length == 0) {
		action = 'metaStudyAction!submit.action'
	}
	confirmationDialog("warning", msgText, yesBtnText, noBtnText, action, true, "400px", "Confirm Create");
	
}


function cancelCreation() {
	var msgText = "Are you sure you want to cancel? All changes will be lost.";
	var yesBtnText = "Cancel";
	var noBtnText = "Do Not Cancel";

	var action = "metaStudyAction!cancel.action";
	confirmationDialog("warning", msgText, yesBtnText, noBtnText, action, false, "400px", "Confirm Cancellation");
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

/**
 * THIS BEGINS THE META STUDY DIV COLAPSE AND EXPAND FUNCTIONS USED IN EDIT AND
 * VIEW
 */
function detailsInit() {
	$("#detailsLabel").click(function(){
		$("#details").slideToggle("fast");
		if($("#detailsLabelPlusMinus").text()=="+") {
			$("#detailsLabelPlusMinus").text("- ");
		} else {
			$("#detailsLabelPlusMinus").text("+");
		}
	});
}

function studyOverViewInit(){
	$("#studyOverViewLabel").click(function(){
		$("#studyOverView").slideToggle("fast");
		if($("#studyOverViewLabelPlusMinus").text()=="+") {
			$("#studyOverViewLabelPlusMinus").text("- ");
		} else {
			$("#studyOverViewLabelPlusMinus").text("+");
		}
		
		
	});
	
}



function studyResearchMgmtInit() {
	$("#studyResearchMgmtLabel").click( function() {
		$("#researchMgmt").slideToggle("fast");
		if($("#studyResearchMgmtLabelPlusMinus").text() == "+") {
			$("#studyResearchMgmtLabelPlusMinus").text("-");
		} else {
			$("#studyResearchMgmtLabelPlusMinus").text("+");
		}
	});
}
function studyInformationInit(){
	$("#studyInformationLabel").click(function(){
		$("#studyInformation").slideToggle("fast");
		if($("#studyInformationLabelPlusMinus").text()=="+") {
			$("#studyInformationLabelPlusMinus").text("- ");
		} else {
			$("#studyInformationLabelPlusMinus").text("+");
		}
	});
}



function documentationInit() {
	$("#documentationLabel").click(function(){
		$("#documentation").slideToggle("fast");
		if($("#documentationLabelPlusMinus").text()=="+") {
			$("#documentationLabelPlusMinus").text("- ");
		} else {
			$("#documentationLabelPlusMinus").text("+");
		}
	});
}

function dataInit() {
	$("#dataLabel").click(function(){
		$("#data").slideToggle("fast");
		if($("#dataLabelPlusMinus").text()=="+") {
			$("#dataLabelPlusMinus").text("- ");
		} else {
			$("#dataLabelPlusMinus").text("+");
		}
	});
}

function keywordLabelInit() {
	$("#keywordLabel").click(function(){
		$("#keyword").slideToggle("fast");
		if($("#keywordLabelPlusMinus").text()=="+") {
			$("#keywordLabelPlusMinus").text("- ");
		} else {
			$("#keywordLabelPlusMinus").text("+");
		}
	});
}

/**
 * THIS ENDS THE META STUDY DIV COLAPSE AND EXPAND FUNCTIONS USED IN EDIT AND
 * VIEW
 */


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


/**
 * DATATABLE HELPER FUNCTIONS
 */
function ieFrameSubmit(formName, onloadAction){
	var iframe = $('<iframe name="dociframe" id="dociframe"onload="'+ onloadAction +'" style="display: none"></iframe>');
	
	$("body").append(iframe);
	
	var form = $('#' + formName);
	form.attr("target", "dociframe");
	form.attr("isMetaStudyEdit",true);
	
	form.submit();
	return;
}

// helper method that refreshes tables based off the response
function refreshIBISTable(action){
	var dt = $('#metaStudyDataListTable').idtApi('getTableApi');
	var jsonData ;
	$.ajax({
		type: "POST",
		cache: false,
		url: action,
		success: function(data) {
			dt.ajax.reload();   
			// get json data from the jsp
//            jsonData = JSON.parse($(data).find('script[type="text/json"]').html());
//            aaData = jsonData.aaData;
//            // remove all current rows from documentation table
//			winIDT.removeAllRows($table);
//            
//            // add new rows to table
//            winIDT.addRow($table, aaData);
						
		}
	});
}

/**
 * THIS ENDS THE META STUDY DOCUMENTATION EDIT/UPLOAD FUNCTIONALITY.
 */

/**
 * THIS BEGINS THE META STUDY DATA EDIT/UPLOAD FUNCTIONALITY. THIS UPDATES THE
 * TABLE DYNAMICALLY. THERE ARE 2 METHODS FOR SUBMISSION IE9 AND LOWER/IE10+
 */

function editData() {
	var selectedDataName = IDT.getSelectedOptions($("#dataTable table"));
	
	$.ajax({
		type: "POST",
		cache: false,
		url: "metaStudyDataAction!editData.ajax?metaStudyDataName=" + selectedDataName,
		success: function(data) {
			$.fancybox(data);
		}
	});
}

//function deleteData() {
//	var msgText = "Are you sure you want to delete the item(s)?";
//	var yesBtnText = "Delete";
//	var noBtnText = "Do Not Delete";
//	var $table = $("#dataTable table");
//
//	var selectedDataNames = IDT.getSelectedOptions($("#dataTable table"));
//	var action = "metaStudyDataAction!removeData.action?metaStudyDataName=" + selectedDataNames; 
//	
//	confirmationEditDialog("warning", msgText, yesBtnText, noBtnText, action, true, "400px","Confirm Deletion",$table,selectedDataNames);
//	
//}

function uploadDataFile() {
	var fileName = $("#fileNameDisplay").text();
	var currentName = $("#uploadFileName").val();
	
	if (fileName && fileName.length > 0) {
		if(fileName != "No file selected."){
			$("#uploadFileName").val(fileName);
		} else {
			$("#uploadFileName").val("");
		}
		
	}
  	if(typeof FormData != "undefined"){
  		$("#dataDialogSubmit").prop("disabled",true);
  		var data = new FormData();
  		var isEditingData = $('#isEditingData').val();
  		
  		//var uploadFile = null;
  		if(document.addDataFileForm.uploadData.files.length != 0){
        	data.append('uploadData', document.addDataFileForm.uploadData.files[0]);
       		data.append('uploadDataFileName',document.addDataFileForm.uploadData.files[0].name);
       		$("#uploadFileName").val(document.addDataFileForm.uploadData.files[0].name);
       		
			/*var fileReader = new FileReader();
			fileReader.file = document.addDataFileForm.uploadData.files[0];
		    fileReader.onload = function () {
		      uploadFile = this.file;  
		    };
		    fileReader.readAsDataURL(document.addDataFileForm.uploadData.files[0]); */  
  		}
  		
  		data.append('uploadFileName',$('#uploadFileName').val());
        data.append('isEditingData',isEditingData);
        data.append('dataFileType',$('#dataFileType').val());
        data.append('dataDescription',$('#dataDescription').val());
        data.append('version',$('#version').val());
        data.append('dataSource',$('#dataSource').val());
        var dt = $('#metaStudyDataListTable').idtApi('getTableApi');
        $.ajax({
            url:"metaStudyDataValidationAction!uploadData.ajax",
            data:data,
            cache:false,
            processData:false,
            contentType:false,
            type:'POST',
            success:function (data) {
            if(data == "success"){
            	//refreshDataTable();
            	$("#selectAddDataDiv").toggle();
            	dt.ajax.reload();
            	$.fancybox.close();
			} else {
				$.fancybox(data);
				/*edit data file*/
				var hasFieldErrors = $("input#hasFieldErrors").val();
				
				if(hasFieldErrors !="true"){
	        		if(isEditingData == "true" && (currentName != "" && fileName != "" && currentName != fileName) || currentName == ""){
	        			$('#uploadFileName').val(null);
	        			$("#fileNameDisplay").text("No file selected.");
	        		}
				}
				/*var hasFieldErrors = $("input#hasFieldErrors").val();console.log("hasFieldErrors: "+hasFieldErrors);
				if(hasFieldErrors == "true" && uploadFile != null){ console.log("error true: "+uploadFile.name);				
					$('#uploadFileName').val(uploadFile.name);
					convertFileUpload($('#uploadData'), uploadFile.name);					
				}*/
			}
            }
       	});
	} else {
  		ieFrameSubmit("addDataFileForm","iframeRefreshDataTable()");
    	$("#selectAddDataDiv").toggle();
    	$.fancybox.close();
	}
}

function iframeRefreshDataTable(){
	refreshDataTable();
}

function viewSavedQuery(queryId) {
	$.ajax({
		type: "POST",
		cache: false,
		url: "metaStudyAction!viewSavedQuery.ajax",
		data: {savedQueryId: queryId},
		success: function(data) {
			var $dialogContainer = $(".viewSavedQueryDialog");
			if ($dialogContainer.length < 1) {
				$("body").append('<div class="viewSavedQueryDialog" style="display:none"></div>');
				$dialogContainer = $(".viewSavedQueryDialog");
			}
			
			// 80% of window height
			var height = $(window).height() * 0.9;
			
			$dialogContainer.html(data);
			$dialogContainer.dialog({
				modal: true,
				height: height,
				width: 900,
				close : function() {
					EventBus.trigger("destroy:all");
					$(this).dialog("destroy");
				},
				open : function() {
					var style = $('<style>.ui-dialog { z-index: 1200 !important; }</style>');
					$('html > head').append(style);
					SQViewProcessor.render();
				},
				buttons : [
				    {
				    	text: "OK",
				    	click : function() {
				    		$(this).dialog("close");
				    	}
				    }
				]
			});
		},
		error : function(data) {
			alert("There was a problem retrieving the saved query");
		}
	});
}

function refreshDataTable(){
	var action = "metaStudyAction!dataRefresh.ajax";
	refreshIBISTable(action);
}

function addSavedQueryData() {
	
	var formData = $('#addSavedQueryForm').serializeArray();
	var dt = $('#metaStudyDataListTable').idtApi('getTableApi');
	$.ajax({
		type: "POST",
		cache: false,
		url: "addSavedQueryValidationAction!uploadData.ajax?",
		data: formData,
		success: function(data) {
			if (data == "success") {
				dt.ajax.reload();
		        $("#selectAddDataDiv").toggle();
		        $.fancybox.close();
			} else {
				$.fancybox(data);
			}
		}
	});
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


//searches for keywords
function searchKeywords()
{
	var searchData = $('#keywordSearchKey').val();	//Get the text box where the user is typing in search letters
	
	// The moveOptions function is defined in optiontransferselect.js
	moveOptions($("#availableKeywords")[0], $("#hiddenKeywords")[0], false,
		function(opt) {
			if(opt.value.toUpperCase().indexOf(searchData.toUpperCase()) != -1)
			{
				return false;
			}
			else
			{
				return true;
			}
	});
	moveOptions($("#hiddenKeywords")[0], $("#availableKeywords")[0], false,
		function(opt) {
			if(opt.value.toUpperCase().indexOf(searchData.toUpperCase()) != -1)
			{
				return true;
			}
			else
			{
				return false;
			}		
	});
		sortOptions();
}

//searches for labels
function searchLabels()
{
	var searchData = $('#labelSearchKey').val();	//Get the text box where the user is typing in search letters
	
	// The moveOptions function is defined in optiontransferselect.js
	moveOptions($("#availableLabels")[0], $("#hiddenLabels")[0], false,
		function(opt) {
			if(opt.value.toUpperCase().indexOf(searchData.toUpperCase()) != -1)
			{
				return false;
			}
			else
			{
				return true;
			}
	});
	moveOptions($("#hiddenLabels")[0], $("#availableLabels")[0], false,
		function(opt) {
			if(opt.value.toUpperCase().indexOf(searchData.toUpperCase()) != -1)
			{
				return true;
			}
			else
			{
				return false;
			}		
	});
		sortLabelOptions();
}


function addNewKeyword() {
	
	
	//var keyword = $.trim($('input[name="keywordNew"]').val());
	var keyword = $.trim($("#keywordSearchKey").val());
	
	var options = $('.currentKeywords');
	
	// Uses an ajax call instead of a post and serializes the form data so that the call gets picked up by struts validation
	$.ajax({
		type: "POST",
		cache : false,
		url : "keywordNameValidationAction!createKeyword.ajax",
		data : $("form").serializeArray(),
		success : function (data) {
			if (data.keyword == undefined) {				
				$("#newKeywordDiv").html(data);
			}
			else {				
				options.append($("<option />", { value : data.count + "_" + data.keyword }).html(data.keyword + " (" + data.count + ")"));
				$("#keywordSearchKey")[0].value = "";
				$('.error-message').hide();			
				moveAllOptions(
						$("#hiddenKeywords")[0], 
						$("#availableKeywords")[0], false, '');
			}
			// Sort the select boxes
			sortOptions();
		}
	});
}


function addNewLabel() {
	
	var label = $.trim($("#labelSearchKey").val());
	
	var options = $('.currentLabels');
	
	// Uses an ajax call instead of a post and serializes the form data so that the call gets picked up by struts validation
	$.ajax({
		cache : false,
		url : "labelNameValidationAction!createLabel.ajax",
		data : $("form").serializeArray(),
		success : function (data) {
			if (data.label == undefined) {
				
				$("#newLabelDiv").html(data);
			}
			else {
				
				options.append($("<option />", { value : data.count + "_" + data.label }).html(data.label + " (" + data.count + ")"));
				$("#labelSearchKey")[0].value = "";
				$('.error-message').hide();
				moveAllOptions(
						$("#hiddenLabels")[0], 
						$("#availableLabels")[0], false, '');
			}
			// Sort the select boxes
			sortLabelOptions();
		}
	});
}
	
//Sort options in the lists based on name or frequency
function sortOptions()
{

	
	if ($("#nameRadioButton")[0].checked)
	{
		$("#availableKeywords").html($("#availableKeywords option").sort(function (a, b) {
			var aValue = a.value.split('_', 2)[1].toUpperCase();
			var bValue = b.value.split('_', 2)[1].toUpperCase();
		    return aValue == bValue ? 0 : aValue < bValue ? -1 : 1;
		}));
		$(".currentKeywords").html($(".currentKeywords option").sort(function (a, b) {
			var aValue = a.value.split('_', 2)[1].toUpperCase();
			var bValue = b.value.split('_', 2)[1].toUpperCase();
		    return aValue == bValue ? 0 : aValue < bValue ? -1 : 1;
		}));
	}
	else if ($("#freqRadioButton")[0].checked)
	{
		$("#availableKeywords").html($("#availableKeywords option").sort(function (a, b) {
		    var aValue = parseInt(a.value.split('_', 2)[0]);
		    var bValue = parseInt(b.value.split('_', 2)[0]);
		    return aValue == bValue ? 0 : aValue < bValue ? -1 : 1;
		}));
		$(".currentKeywords").html($(".currentKeywords option").sort(function (a, b) {
		    var aValue = parseInt(a.value.split('_', 2)[0]);
		    var bValue = parseInt(b.value.split('_', 2)[0]);
		    return aValue == bValue ? 0 : aValue < bValue ? -1 : 1;
		}));
	}
}



//Sort label options in the lists based on name or frequency
function sortLabelOptions()
{
	

	if ($("#nameLabelRadioButton")[0].checked)
	{
		$("#availableLabels").html($("#availableLabels option").sort(function (a, b) {
			var aValue = a.value.split('_', 2)[1].toUpperCase();
			var bValue = b.value.split('_', 2)[1].toUpperCase();
		    return aValue == bValue ? 0 : aValue < bValue ? -1 : 1;
		}));
		$(".currentLabels").html($(".currentLabels option").sort(function (a, b) {
			var aValue = a.value.split('_', 2)[1].toUpperCase();
			var bValue = b.value.split('_', 2)[1].toUpperCase();
		    return aValue == bValue ? 0 : aValue < bValue ? -1 : 1;
		}));
	}
	else if ($("#freqLabelRadioButton")[0].checked)
	{
		$("#availableLabels").html($("#availableLabels option").sort(function (a, b) {
		    var aValue = parseInt(a.value.split('_', 2)[0]);
		    var bValue = parseInt(b.value.split('_', 2)[0]);
		    return aValue == bValue ? 0 : aValue < bValue ? -1 : 1;
		}));
		$(".currentLabels").html($(".currentLabels option").sort(function (a, b) {
		    var aValue = parseInt(a.value.split('_', 2)[0]);
		    var bValue = parseInt(b.value.split('_', 2)[0]);
		    return aValue == bValue ? 0 : aValue < bValue ? -1 : 1;
		}));
	}
}

function selectAllCurrentKeywords() {
	 var list = $('.currentKeywords')[0];
	 
	 //If the list contains no options, insert one with the value: 'empty'
	 if (list.length ==0)
	 {
		
		 $(list).append($("<option />", { value : "empty" }));
	 }
	 for (var i = 0; i < list.options.length; i++) 
	   {
		
		 valueSplit = list.options[i].value.split("_");
		 if(valueSplit[1] != 'null' && valueSplit[2] != 'null') {
	   
	    	list.options[i].selected = true;
		 }
	   }
}

function selectAllCurrentLabels() {
	
	 var list = $('.currentLabels')[0];
	 
	 //If the list contains no options, insert one with the value: 'empty'
	 if (list.length ==0)
	 {
		 $(list).append($("<option />", { value : "empty" }));
	 }
	 for (var i = 0; i < list.options.length; i++) 
	   {
		 valueSplit = list.options[i].value.split("_");
		 if(valueSplit[1] != 'null' && valueSplit[2] != 'null') {
	    list.options[i].selected = true;
		 }
	   }
}
