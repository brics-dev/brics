<%@include file="/common/taglibs.jsp"%>
	<style>
		#addResearchMgmtDialog span.required {
    		color: #DD3434;
		}
	</style>
	
	<div id="researchMgmt">
	<div id="addResearchMgmtDialog" style="display: none;">
		<s:form id="researchMgmtForm" name="researchMgmtForm">
		<p>Enter information about the individuals working on the study.
			For each study in the repository must have at most 1 Primary
			Principal Investigator. Please work with your team to determine who
			will have this title. After you've entered an individuals
			information, click on the "Add to Table" button to add the individual
			to the Research Management table. The individuals added to the table
			will be saved as part of the Study Research.</p>
		<s:hidden id="selectedResMgmtToEdit" name="selectedResMgmtToEdit" />
		<s:if test="hasFieldErrors()">
			<input id="hasResMgmtFieldErrors" value="true" type="hidden"/>
		</s:if>	
		<div class="form-field">
			<label for="roleSelect" >Role :</label>
			<s:select id="roleSelect" list="roleList" listKey="id" listValue="name" name="researchMgmtMetaEntry.role" 
				value="researchMgmtMetaEntry.role.id" headerKey="" headerValue="- Select One -" />
			<s:fielderror fieldName="researchMgmtMetaEntry.role" />
		</div>
		
		<div id="hideShowBasedOnRole">
		<div class="form-field">
			<s:hidden id="researchMgmtId" name="researchMgmtEntry.id"/>
			<label for="firstName" class="required">First Name <span class="required">* </span>:</label>
			<s:textfield id="firstName" name="researchMgmtMetaEntry.firstName" cssClass="textfield required" maxlength="55" />
			<s:fielderror fieldName="researchMgmtMetaEntry.firstName" />
		</div>
		
		<div class="form-field">
			<label for="mi">Middle Initial :</label>
			<s:textfield id="mi" name="researchMgmtMetaEntry.mi" cssClass="textfield" maxlength="1" />
		</div>
		
		<div class="form-field">
			<label for="lastName" class="required">Last Name <span class="required">* </span>:</label>
			<s:textfield id="lastName" name="researchMgmtMetaEntry.lastName" cssClass="textfield required" maxlength="55" />
			<s:fielderror fieldName="researchMgmtMetaEntry.lastName" />
		</div>
	
		<div class="form-field">
			<label for="suffix">Suffix :</label>
			<s:textfield id="suffix" name="researchMgmtMetaEntry.suffix" theme="simple" maxlength="10" size="10" />
		</div>
		
		<div class="form-field">
			<label for="email">E-Mail :</label>
			<s:textfield id="email" name="researchMgmtMetaEntry.email" cssClass="textfield" maxlength="55" />
			<s:fielderror fieldName="researchMgmtMetaEntry.email" />
		</div>
		
		<div class="form-field">
			<label for="orgName" class="required">Organization Name <span class="required">* </span>:</label>
			<s:textfield id="orgName" name="researchMgmtMetaEntry.orgName" cssClass="textfield required" maxlength="255" />
			<s:fielderror fieldName="researchMgmtMetaEntry.orgName" />
		</div>
		
		<div class="form-field">
				<label for="pictureFile">Picture File :</label>
			<s:file id="pictureFile" name="picture" cssStyle="float:left;" />
			<s:fielderror fieldName="pictureFileName" />
			<s:hidden id="pictureFileName" name="pictureFileName" cssStyle="margin: 0 0 0 5px;"/>		
			<div style="clear:both; margin-left:165px"><b>Only JPEG or PNG File format, with preferred image size 200 x 160.</b></div>
		</div>
		</div>
		</s:form>
	</div>

<!-- 	<div class="button" style="padding-left: 50px;"> -->
<!-- 		<input type="button" id="addResMgmtBtn" value="Add to Table" onclick="javascript:openResearchMgmtEntryDialog('add')" /> -->
<!-- 	</div> -->
	
	<div class="form-field inline-right-button">
		<div id="testopen" class="button" style="padding-right: 300px">
			<input type="button" class="testopen" id="openResMgmtDlg" value="Add to Table" onclick="javascript:openResearchMgmtEntryDialog('add')" />
		</div>
    </div>
<!-- 	<div id="resMgmtTblDiv" class="idtTableContainer" 	style="padding-left: 50px; padding-top: 20px; padding-bottom: 10px; width: 800px"> -->
<!-- 		<br> -->
<!-- 		<h4 style="padding-top: 6px;">Research Management Table</h4> -->
<!-- 		<table id="resMgmtTblDivTable" 	class="table table-striped table-bordered"></table> -->
<!-- 	</div> -->

	<div id="resMgmtTblDiv" class="idtTableContainer" style="padding-left:50px; padding-top:20px;  padding-bottom: 10px; width:800px">
	    <br>
		<h4 style="padding-top: 6px;">Research Management Table</h4>
		<div id="dialog"></div>
		<table id="researchMgmtMetaTable" class="table table-striped table-bordered"></table>
	</div>
	</div>
<script type="text/javascript">
function ResearchMgmt(id, roleId, firstName, mi, lastName, suffix, email, orgName) {
	this.id = id;
	this.role = roleId
	this.firstName = firstName;
	this.mi = mi;
	this.lastName = lastName;
	this.suffix = suffix;
	this.email = email;
	this.orgName = orgName;
}

function getSelectedResMgmtMetaJson(selectedRow){
	var researchMgmtJson = new ResearchMgmt();
	researchMgmtJson.id = selectedRow.id;
    researchMgmtJson.role = selectedRow.roleId;
    researchMgmtJson.firstName = selectedRow.firstName;
    researchMgmtJson.mi = selectedRow.mi;
    researchMgmtJson.lastName = selectedRow.lastName;
    researchMgmtJson.suffix = selectedRow.suffix;
    researchMgmtJson.email = selectedRow.email;
    researchMgmtJson.orgName = selectedRow.orgName;	    
    return researchMgmtJson;
}
	

$(document).ready(function() {
	$("#researchMgmtMetaTable").idtTable({
		idtUrl: "<s:url value='/metastudy/metaStudyAction!getResearchMgmtMetaSet.action' />",
		idtData: {
			primaryKey: "rowId"
		},
		autoWidth: false,
		dom : 'Bfrtip',
		columns: [
			{
				data: "Role",
				title: "Role",
				name: "Role",
				parameter: "roleTitle"
			},
			{
				data: "fullName",
				title: "Full Name",
				name: "fullName",
				parameter: "fullName"
			},
			{
				data: "email",
				title: "E-Mail",
				name: "email",
				parameter: "email"
			},
			{
				data: "orgName",
				title: "Organization",
				name: "orgName",
				parameter: "orgName"
			},
			{
				data: "roleId",
				title: "roleId",
				name: "roleId",
				parameter: "role.id"
				
			},
			{
				data : "id",
				title : "",
				name: "id",
				parameter : "id"

			},	
			{
				data: "firstName",
				title: "",
				name: "firstName",
				parameter: "firstName"
				
			},
			{
				data: "mi",
				title: "",
				name: "mi",
				parameter: "mi"
				
			},
			{
				data: "lastName",
				title: "",
				name: "lastName",
				parameter: "lastName"
				
			},
			{
				data: "suffix",
				title: "Suffix",
				name: "suffix",
				parameter: "suffix"
				
			}
		],
		aoColumnDefs:[ {"aTargets": [ 5, 6, 7, 8, 9],
    					"visible":  false
    	
     	}],
		select : 'multi',
		initComplete: function(){
			var table = $("#researchMgmtMetaTable");
			reorderStudyReasearchMgmtTable(table);
		},
		buttons: [
		        	{
			        	text: 'Edit',
			            action: function(e, dt, node, config) {
			            	
			            	var dt = dt;
			            	var selectedRow = dt.row({ selected: true }).data();		            	
			            	var researchMgmtJson = getSelectedResMgmtMetaJson(selectedRow);
		                    
		                    //console.log("researchMgmtJson: "+JSON.stringify(researchMgmtJson));
			            	populateResearchMgmtForm(researchMgmtJson)

			            },
			            enableControl: {
			            	count: 1,
			                invert:true
			            },
			            enabled: false
			
			        },
			        {
						extend : "delete",
						title : "Delete",
						action : function(e, dt, node,config) {

							var researchMgmtJsonArr = [];
							var dt = dt;
							var rows = dt.rows().data().to$();
							var selectedRows = dt.rows('.selected').data().to$();
							for (var i = 0; i < selectedRows.length; i++) {
								var researchMgmtJson = getSelectedResMgmtMetaJson(selectedRows[i]);
								researchMgmtJsonArr.push(researchMgmtJson);
							}
							$("#dialog").dialog(
											{
												modal : true,
												title : 'Confirm Deletion',
												type : 'warning',
												zIndex : 10000,
												autoOpen : true,
												width : '400px',
												resizable : false,
												dialogClass : 'dialog-wrapper',
												open : function() {
													var markup = 'Are you sure you want to delete the item(s)?';
													$(this).html(markup);
												},
												buttons : {
													'deleteRow' : {
														text : 'Delete',
														className : 'deleteRow dialog-btn-row',
														click : function() {
															removeResearchMgmt(JSON.stringify(researchMgmtJsonArr));
															$(this).dialog("close");
														}
													},
													'cancelDelete' : {
														text : 'Cancel',
														className : 'cancelDelete dialog-btn-cancel',
														click : function() {
															$(this).dialog("close");
														}
													}
												}
											});
										}
			       				 }

							]
				});

	checkPrimaryPI();

})

function checkPrimaryPI() {
	var hasPrimaryPI = <s:property value="sessionMetaStudy.metaStudy.hasPrimaryPI" />;
	if (hasPrimaryPI == true) {
		$("#roleSelect option[value=0]").attr("disabled","disabled");
	}else {
		$("#roleSelect option[value=0]").removeAttr('disabled');
	}
}

function openResearchMgmtEntryDialog(mode, selectedRowData) {
	$("#addResearchMgmtDialog").dialog({
		title: "Study Research Management",
		autoOpen: false,
		height: 450,
		width: 1050,
		position: { my: "center", at: "center", of: window },
		buttons : [
				{
					id: "saveResMgmtBtn",
					text: "Submit",
					click : function() {
						if (mode == "add"){//console.log("add mode");
							addResearchMgmt();
						} else if (mode == "edit") {//console.log("edit mode");
							editResearchMgmtAjax(selectedRowData);
						}
						
						$(this).dialog("destroy");
						
					}
				},
				{
					id: "cancelResMgmtBtn",
					text: "Cancel",
					click : function() {
						clearResearchMgmtEntry();
						$(this).dialog('destroy');
					}
				}
		],
		open: function(event,ui) {
			
			var type = $("#roleSelect").val();	
			if(type!=''){
				$("#firstName").prop('disabled', false);
				$("#mi").prop('disabled', false);
				$("#lastName").prop('disabled', false);
				$("#suffix").prop('disabled', false);
				$("#email").prop('disabled', false);
				$("#orgName").prop('disabled', false);
				$("#pictureFile").prop('disabled', false);
				$("#saveResMgmtBtn").prop('disabled', false).removeClass("ui-state-disabled");
				//reload the file name for edit
				if($('#pictureFileName').val() != ""){ 
					convertFileUpload($('#pictureFile'), $('#pictureFileName').val());
				}
			} else {
				$("#firstName").prop('disabled', true);
				$("#mi").prop('disabled', true);
				$("#lastName").prop('disabled', true);
				$("#suffix").prop('disabled', true);
				$("#email").prop('disabled', true);
				$("#orgName").prop('disabled', true);
				$("#pictureFile").prop('disabled', true);
				$("#saveResMgmtBtn").prop('disabled', true).addClass("ui-state-disabled");
				clearResearchMgmtEntry();
			}
				
			$("#roleSelect").on('change', function(){					
				var type = $(this).find('option:selected').text();	 
	
				if(type!='- Select One -' && type!=''){
					$("#firstName").prop('disabled', false);
					$("#mi").prop('disabled', false);
					$("#lastName").prop('disabled', false);
					$("#suffix").prop('disabled', false);
					$("#email").prop('disabled', false);
					$("#orgName").prop('disabled', false);
					$("#pictureFile").prop('disabled', false);
					$("#saveResMgmtBtn").prop('disabled', false).removeClass("ui-state-disabled");
				} else {
					$("#firstName").prop('disabled', true);
					$("#mi").prop('disabled', true);
					$("#lastName").prop('disabled', true);
					$("#suffix").prop('disabled', true);
					$("#email").prop('disabled', true);
					$("#orgName").prop('disabled', true);
					$("#pictureFile").prop('disabled', true);
					$("#saveResMgmtBtn").prop('disabled', true).addClass("ui-state-disabled");	
				}

			});

		},
		close: function() {
			clearResearchMgmtEntry();
			$(this).dialog('destroy');
		}
	});

	
$("#addResearchMgmtDialog").dialog('open');
}

function getFormData() {
	var formData = new FormData();
	var roleId = $('#roleSelect option:selected').val();
	
	formData.append('researchMgmtEntry.id',$('#researchMgmtId').val());
	formData.append('researchMgmtMetaEntry.role', roleId);
	formData.append('researchMgmtMetaEntry.firstName', $('#firstName').val());
	formData.append('researchMgmtMetaEntry.mi', $('#mi').val());
	formData.append('researchMgmtMetaEntry.lastName', $('#lastName').val());
	formData.append('researchMgmtMetaEntry.suffix', $('#suffix').val());
	formData.append('researchMgmtMetaEntry.email', $('#email').val());
	formData.append('researchMgmtMetaEntry.orgName', $('#orgName').val());
	//console.log("getFormData fileName: "+($("#pictureFile"))[0].files[0]);
	if (($("#pictureFile"))[0].files.length > 0) {
		formData.append('picture', ($("#pictureFile"))[0].files[0]);
	}
	return formData;
}

function addResearchMgmt() {
	var formData = getFormData();
	var roleId = $('#roleSelect').val();
	//console.log("add() formData.roleid: "+formData.get('researchMgmtMetaEntry.role') );
	$.ajax({
		type: "POST",
		url: "researchMgmtMetaValidationAction!addResearchManagement.ajax",
		data: formData,
		cache : false,
		processData : false,
		contentType : false,
		success: function(data) {
			$('#researchMgmt').html(data);			
			/* buildDataTables(); */
			//console.log(data);
			var dt = $('#researchMgmtMetaTable').idtApi('getTableApi');
			 //dt.ajax.reload();
			if (roleId == 0) {    // Just added a Primary PI
				$("#roleSelect option[value=0]").attr("disabled","disabled");
			} else {
				checkPrimaryPI();
			}
			if($('#researchMgmt .error-text').length > 0){
				openResearchMgmtEntryDialog('add');						
			}
		
		}
	});  
}

function iframeSubmit() {
	var iframe = $('<iframe name="picIframe" id="picIframe" onload="refreshResearchMgmt()" style="display: none"></iframe>');
	$("body").append(iframe);
	
	var form = $('<form name="iframeForm" id="iframeForm"></form>');
	$("body").append(form);
	
	form.attr("target", "picIframe");
	form.attr("action", "researchMgmtMetaValidationAction!addResearchManagement.action"); 
	form.attr("method", "post");
	form.attr("enctype", "multipart/form-data");
	
	var rmDiv = $("#researchMgmt").clone();
	form.append(rmDiv);
	form.submit();
	return;
}

function refreshResearchMgmt() {
	$('#picIframe').remove();
	$('#iframeForm').remove();
}

function removeResearchMgmt(researchMgmtJson) {
	$.ajax({
		type: "POST",
		url: "metaStudyAction!removeResearchMgmt.ajax",
		data: "researchMgmtJson=" + JSON.stringify(researchMgmtJson),
		"async": true,
		success: function(data) { //console.log("remove data: "+data);
 			$('#researchMgmt').html(data);
 			buildDataTables(); 
 			var dt = $('#researchMgmtMetaTable').idtApi('getTableApi');
			 dt.ajax.reload();
 			if (researchMgmtJson.role == 0) {  // Just removed the primary PI
				$("#roleSelect option[value=0]").removeAttr('disabled');
			} else { 
				checkPrimaryPI();
 			} 
		
		}
	});
}

function populateResearchMgmtForm(selectedRowData) {
	//console.log("populateResearchMgmtForm");
	$.ajax({
		type: "POST",
		url: "metaStudyAction!getResMgmtMetaEntryToEdit.ajax",
		data: "researchMgmtEntryJson=" + JSON.stringify(selectedRowData),

		success: function(data) { //console.log("populateResearchMgmtForm data: "+data);
 			$('#researchMgmt').html(data);
 			openResearchMgmtEntryDialog("edit",JSON.stringify(selectedRowData));
			
		}
	});
}

function editResearchMgmtAjax(selectedRowData) {
	
	var formData = getFormData();
	formData.append('selectedResMgmtToEdit', selectedRowData);
	$('#selectedResMgmtToEdit').val(selectedRowData);

	$.ajax({
		type: "POST",
		url: "metaStudyAction!editResearchMgmtAjax.ajax",
		data: formData,
		cache : false,
		processData : false,
		contentType : false,
		success: function(data) { //console.log("edit data: "+data);
 			$('#researchMgmt').html(data);
 			//buildDataTables(); 
 			var dt = $('#researchMgmtMetaTable').idtApi('getTableApi');
			 //dt.ajax.reload();
 			checkPrimaryPI(); 
		}
	});
}

function clearResearchMgmtEntry() {
	$('#roleSelect').val("");
	$('#firstName').val("");
	$('#mi').val("");
	$('#lastName').val("");
	$('#suffix').val("");
	$('#email').val("");
	$('#orgName').val("");
	$('#pictureFile').val("");
	$('#pictureFileName').val("");
	//remove the file name retained for edit
	if ( $("#fileNameDisplay").length ) {
		$("#fileNameDisplay").remove();
	}
}




</script>