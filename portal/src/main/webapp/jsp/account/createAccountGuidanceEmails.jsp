<%@include file="/common/taglibs.jsp"%>


<div class="clear-float">
	<div class="clear-float">
		<h1 class="float-left">Account Management</h1>
	</div>
</div>



<div class="border-wrapper">
	<jsp:include page="../navigation/userManagementNavigation.jsp" />
	<div id="main-content">
		<h2>Account Requests: Temporary Rejection Generic Reasons</h2>
		<br><br>
		<p>Administrative users are allowed to add new options and edit existing checkboxes as part of the temporary rejection
		workflow for account renewals. Within the notification email, the selected checkboxes as part of that workflow will appear in the following format:  
		Your account request has been TEMPORARILY REJECTED with the following reason: {Selected Checkbox}. Here is some helpful information
		to help you update your profile appropriately {Text Sent to User}</p>
		<br><br><br>
		<div id="temporaryRejectionListContainer" class="idtTableContainer">
			<h2>Existing Selections</h2>
			<table id="temporaryRejectionListTable" class="table table-striped table-bordered" width="100%"></table>
		</div>	
		<div id="editAccountGuidanceEmailDialog" style="display: none;">
			<div id="editAccountGuidanceEmail"></div>
		</div>
		<div>
			<form id="accountGuidanceEmailsForm" name="accountGuidanceEmailsForm">
				<h2>Add Selection</h2>
				<br>
				<div class="form-field">
					<label for="checkboxTextT" class="required">Checkbox Label<span class="required">* </span>:</label>
					<input type="text" maxlength="100" name="checkboxTextT" id="checkboxTextT" />
					<span id="validateCheckboxMsg" style="display: none">
						<img class="icon-warning" alt="" src="/portal/images/brics/common/icon-warning.gif" style="float: none">
						<span class="required"><strong>Checkbox Label is a  required field</strong></span>
					</span>			
				</div>
				<br>
				<div class="form-field">
					<label for="messageT">Text Sent to User:</label>
					<textarea maxlength="2000" name="messageT" id="messageT" cols="40" rows="5" style='white-space:pre'></textarea>
				</div>
				<br>
				<div class="form-field">
					<div style="width: 425px">
						<label for="defaultCheckedT">Default Checked?</label>
						<input type="radio" name="defaultCheckedT" id="defaultCheckedTY"  />Yes&nbsp;
						<input type="radio"  name="defaultCheckedT" id="defaultCheckedTN" checked="checked" />No
						<div class="button" style="float:right;">
							<input type="button" id="addTempRejBtn" value="Add" onclick="javascript:saveGuidanceEmailTemplate('TEMPORARY_REJECTION')" />
						</div>	
					</div>
				</div>
			</form>
		</div>
		<br><br>
		<h2>Account Renewal: Account Update Generic Guidance</h2>
		<br><br><br>
		<div id="accountRenewalListContainer" class="idtTableContainer">
			<h2>Existing Selections</h2>
			<table id="accountRenewalListTable" class="table table-striped table-bordered" width="100%"></table>
		</div>	
		
		<div>
			<form id="accountGuidanceEmailsFormAR" name="accountGuidanceEmailsFormAR">
				<h2>Add Selection</h2>
				<br>
				<div class="form-field">
					<label for="checkboxTextAR" class="required">Checkbox Label<span class="required">* </span>:</label>
					<input type="text" maxlength="100" name="checkboxTextAR" id="checkboxTextAR" />
					<span id="validateCheckboxMsgAR" style="display: none">
						<img class="icon-warning" alt="" src="/portal/images/brics/common/icon-warning.gif" style="float: none">
						<span class="required"><strong>Checkbox Label is a  required field</strong></span>
					</span>			
				</div>
				<br>
				<div class="form-field">
					<label for="messageAR">Text Sent to User:</label>
					<textarea name="messageAR" maxlength="2000" id="messageAR" cols="40" rows="5" style='white-space:pre'></textarea>
				</div>
				<br>
				<div class="form-field">
					<div style="width: 425px">
						<label for="defaultCheckedAR">Default Checked?</label>
						<input type="radio" name="defaultCheckedAR" id="defaultCheckedARY"  />Yes&nbsp;
						<input type="radio"  name="defaultCheckedAR" id="defaultCheckedARN" checked="checked" />No
						<div class="button" style="float:right;">
							<input type="button" id="addAcctRenBtn" value="Add" onclick="javascript:saveGuidanceEmailTemplate('ACCOUNT_RENEWAL')" />
						</div>	
					</div>
				</div>
			</form>
		</div>
		
	</div>
</div>
yyyyyyyyyyyyyy

<div id="deleteTemplateLightboxDiv" style="display: none">

</div>

<script type="text/javascript">

	function validateCheckboxText(text) {
		var valid = true;
		if (typeof text !== 'undefined') {
			if (text.length == 0) {
				valid = false;
			}
		}
		return valid;
	}
 
	function saveGuidanceEmailTemplate(type) {
		var checkboxText;
		var message;
		var defaultChecked;
		
		if(type=='TEMPORARY_REJECTION') {

			checkboxText = $('#checkboxTextT').val();
			if(!validateCheckboxText(checkboxText)) {
				$("#validateCheckboxMsg").show();
				return;
			}
			message = $('#messageT').val();
			defaultChecked =  ($("#defaultCheckedTY").is(":checked")) ? true : false;
		}else {
			checkboxText = $('#checkboxTextAR').val();
			if(!validateCheckboxText(checkboxText)) {
				$("#validateCheckboxMsgAR").show();
				return;
			}
			message = $('#messageAR').val();
			defaultChecked =  ($("#defaultCheckedARY").is(":checked")) ? true : false;
			
		}
		
		saveGuidanceEmailTemplateAjax(0,checkboxText,message,defaultChecked,type);
	}

	function saveGuidanceEmailTemplateAjax(accountTemplateId,checkboxText,message,defaultChecked,type){
		
		$.ajax({
			type: "GET",
			url: "saveAccountGuidanceEmailTemplateAction!saveAccountGuidanceEmailTemplate.ajax",
			data: {"accountTemplateId":accountTemplateId,"checkboxText": checkboxText, "message": message, "defaultChecked": defaultChecked, "typeid" : type},
			success: function(data) {
				
				if(type=='TEMPORARY_REJECTION') {
					//reload datatables
					$('#temporaryRejectionListTable').DataTable().ajax.reload();
					
					//clear input fields
					$('#checkboxTextT').val("");
					$('#messageT').val("");
					$("#defaultCheckedTY").prop("checked", false);
					$("#defaultCheckedTN").prop("checked", true);
					$("#validateCheckboxMsg").hide();
				}else {
					//reload datatables
					$('#accountRenewalListTable').DataTable().ajax.reload();
					
					//clear input fields
					$('#checkboxTextAR').val("");
					$('#messageAR').val("");
					$("#defaultCheckedARY").prop("checked", false);
					$("#defaultCheckedARN").prop("checked", true);
					$("#validateCheckboxMsgAR").hide();
				}
				
			}
		});

	}
	
	
	
	function deleteAccountMessageTemplate(accountMessageTemplateId,type){
		var $deleteTemplateLightboxDiv = $("#deleteTemplateLightboxDiv");
		var html = "<h1>Delete Record</h1><br><p>Are you sure you want to delete the selected record?</p>";
		$deleteTemplateLightboxDiv.html(html);
		var dialogOne;
		
		dialogOne = $deleteTemplateLightboxDiv.dialog({

			modal : true,
			height : 200,
			width : 600,
			draggable : false,
			resizable : false,
			title : "Delete Record",
			buttons : [ {
				text : "Cancel",
				click : function() {
					$(this).dialog("close");
				}
			}, {
				text : "Delete",
				click : function() {
					doDeleteAccountMessageTemplate(accountMessageTemplateId,type);
					$(this).dialog("close");
				}
			} ]
		});

		}
	
	
	
	
	function doDeleteAccountMessageTemplate(accountMessageTemplateId,type) {
		$.ajax({
			type: "GET",
			url: "deleteAccountGuidanceEmailTemplateAction!deleteAccountMessageTemplate.ajax",
			data: {"accountTemplateId":accountMessageTemplateId},
			success: function(data) {
				
				
				if(type=='TEMPORARY_REJECTION') {
					//reload datatables
					$('#temporaryRejectionListTable').DataTable().ajax.reload();
					
					
					//clear input fields
					$('#checkboxTextT').val("");
					$('#messageT').val("");
					$("#defaultCheckedTY").prop("checked", false);
					$("#defaultCheckedTN").prop("checked", true);
					$("#validateCheckboxMsg").hide();
				}else {
					//reload datatables
					$('#accountRenewalListTable').DataTable().ajax.reload();
					
					//clear input fields
					$('#checkboxTextAR').val("");
					$('#messageAR').val("");
					$("#defaultCheckedARY").prop("checked", false);
					$("#defaultCheckedARN").prop("checked", true);
					$("#validateCheckboxMsgAR").hide();
				}
				
			}
		});
	}
	
	function editAccountMessageTemplate(accountMessageTemplateId,type){
		
		$.ajax({
			type: "GET",
			url: "accountGuidanceEmailsAction!edit.ajax",
			data: {"accountTemplateId":accountMessageTemplateId},
			success: function(data) {
				
				$("#editAccountGuidanceEmail").html(data);
				$("#editAccountGuidanceEmailDialog").dialog({
					title: "Edit Checkbox Text",
					height: 300,
					width: 720,
					buttons : [
						{
							id: "cancelBtn",
							text: "Cancel",
							click : function() {
								$(this).dialog('close');
							}
						},
						{
							id: "saveBtn",
							text: "Save",
							click : function() {
								
								var checkboxText = $('#checkboxTextLightBox').val();
								if(!validateCheckboxText(checkboxText)) {
									$("#validateCheckboxMsgLightBox").show();
									return;
								}
								var message = $('#messageLightBox').val();
								var defaultChecked =  ($("#defaultCheckedLightBoxTY").is(":checked")) ? true : false;
								
								
								saveGuidanceEmailTemplateAjax(accountMessageTemplateId,checkboxText,message,defaultChecked,type);
								
								if(type=='TEMPORARY_REJECTION') {
									//reload datatables
									$('#temporaryRejectionListTable').DataTable().ajax.reload();
									
									
									
								}else {
									//reload datatables
									$('#accountRenewalListTable').DataTable().ajax.reload();
									
								
								}
								$(this).dialog("close");
							}
						}
				],
				});
					
					
						$('#editAccountGuidanceEmailDialog').dialog('open');
					}
			});
	}

$('document').ready(function() {
	setNavigation({"bodyClass":"primary", "navigationLinkID":"userManagementModuleLink", "subnavigationLinkID":"managingUsersLink", "tertiaryLinkID":"createEditAccountGuidanceEmailsLink"});

	
	$('#temporaryRejectionListTable').idtTable({
		idtUrl: "<s:url value='/accountAdmin/accountGuidanceEmailsAction!getTemporaryRejectionList.action' />",
		"columns": [
			{
				"data": "checkboxText",
				"title": "Checkbox Text",
				"name": "Checkbox Text",
				"parameter": "decoratedCheckboxText",

			},
			{
				"data": "message",
				"title": "Text Sent to User",
				"name": "Text Sent to User",
				"parameter": "decoratedMessage",
				"render": IdtActions.ellipsis(255)	 
			},
			{
				"data": "defaultChecked",
				"title":"Default Checked?",
				"name":"Default Checked?",
				"parameter":"decoratedDefaultChecked"
			},
			{
				"data": "actions",
				"title":"Actions",
				"name":"Actions",
				"parameter":"decoratedActions"
			}
		]
	});
	
	
	
	$('#accountRenewalListTable').idtTable({
		idtUrl: "<s:url value='/accountAdmin/accountGuidanceEmailsAction!getAccountRenewalList.action' />",
		"columns": [
			{
				"data": "checkboxText",
				"title": "Checkbox Text",
				"name": "Checkbox Text",
				"parameter": "decoratedCheckboxText",

			},
			{
				"data": "message",
				"title": "Text Sent to User",
				"name": "Text Sent to User",
				"parameter": "decoratedMessage",
				"render": IdtActions.ellipsis(255)
				
			},
			{
				"data": "defaultChecked",
				"title":"Default Checked?",
				"name":"Default Checked?",
				"parameter":"decoratedDefaultChecked"
			},
			{
				"data": "actions",
				"title":"Actions",
				"name":"Actions",
				"parameter":"decoratedActions"
			}
		]
	});
	
	
});

</script>
	