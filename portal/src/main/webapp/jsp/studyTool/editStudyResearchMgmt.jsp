<%@include file="/common/taglibs.jsp"%>
<%@page contentType="text/html; charset=UTF-8"%>
<%@page pageEncoding="UTF-8"%>
<style>
	.test {
		color:#dd3434;
	}
.remainder-message {
	font-size: 12px;
	clear: both; 
	margin-left: 10px; 
}
#researchMgmtErrorP .error-message, #researchMgmtErrorP .error-text{
	width: 500px;
	
}
#researchMgmtErrorP .error-text {
	float: none;
	
}
</style>
<div id="resMgmtDialog" title="Study Research Management" style="display: none;">
	<s:form id="resMgmtForm" name="resMgmtForm">
		<p>Enter information about the individuals working on the study.
			For each study in the repository must have at most 1 Primary
			Principal Investigator. Please work with your team to determine who
			will have this title. After you've entered an individuals
			information, click on the "Add to Table" button to add the individual
			to the Research Management table. The individuals added to the table
			will be saved as part of the Study Research.</p>
		<s:if test="hasFieldErrors()">
		
			<input id="hasResMgmtFieldErrors" value="true" type="hidden"/>
			
		</s:if>	
	
		<div class="form-field">
			<label for="roleSelect" class="required">
				Role <span class="required test">* </span>: 
				
			</label>
			<s:select id="roleSelect" style="float:left" list="roleList"
				listKey="id" listValue="name" name="researchMgmtEntry.role"
				value="researchMgmtEntry.role.id" headerKey="-1"
				headerValue="- Select One -" />
		
			<s:fielderror fieldName="researchMgmtEntry.role" />	
		</div>

		<div class="form-field">
			<s:hidden id="researchMgmtId" name="researchMgmtEntry.id"/>
			<label for="firstName" class="required">First Name <span
				class="required test" >* </span>:
			</label>
		
			<s:textfield id="firstName" name="researchMgmtEntry.firstName"
				cssClass="textfield required" maxlength="55" />
			<s:fielderror fieldName="researchMgmtEntry.firstName" />
		</div>

		<div class="form-field">
			<label for="mi">Middle Initial :</label>
			<s:textfield id="mi" name="researchMgmtEntry.mi" theme="simple"
				maxlength="1" size="5" />
		</div>

		<div class="form-field">
			<label for="lastName" class="required">Last Name <span
				class="required test">* </span>:
			</label>
			<s:textfield id="lastName" name="researchMgmtEntry.lastName"
				cssClass="textfield required" maxlength="55" />
			<s:fielderror fieldName="researchMgmtEntry.lastName" />
		</div>

		<div class="form-field">
			<label for="suffix">Suffix :</label>
			<s:textfield id="suffix" name="researchMgmtEntry.suffix"
				theme="simple" maxlength="10" size="10" />
		</div>

		<div class="form-field">
			<label for="email">E-Mail :</label>
			<s:textfield id="email" name="researchMgmtEntry.email"
				cssClass="textfield" maxlength="55" />
			<s:fielderror fieldName="researchMgmtEntry.email" />
		</div>

		<div class="form-field">
			<label for="orgName" class="required">Organization Name <span
				class="required test">* </span>:
			</label>
			<s:textfield id="orgName" name="researchMgmtEntry.orgName"
				cssClass="textfield required" maxlength="255" />
			<s:fielderror fieldName="researchMgmtEntry.orgName" />
		</div>

		<div class="form-field">
			<label for="orcId">ORCID :</label>
			<s:textfield id="orcId" name="researchMgmtEntry.orcId" cssClass="textfield" />
			<s:fielderror fieldName="researchMgmtEntry.orcId" />
		</div>

		<div class="form-field">
			<label for="pictureFile">Picture File :</label>
			<s:file id="pictureFile" name="picture" cssStyle="float:left;" />
			<s:fielderror fieldName="pictureFileName" />
			<s:hidden id="pictureFileName" name="pictureFileName"
				cssStyle="margin: 0 0 0 5px;" />
			<div style="clear: both; margin-left: 165px">
				<b>Only JPEG or PNG File format, with preferred image size 200 x 160.</b>
			</div>
		</div>
	</s:form>
</div>

<div class="form-field inline-right-button">
	<div class="button" style="padding-right: 300px">
		<input type="button" id="openResMgmtDlg" value="Add to Table" onclick="javascript:populateResearchMgmtForm(null, 'add')" />
	</div>
</div>

<div id="resMgmtTblDiv" class="idtTableContainer" 	style="padding-left: 50px; padding-top: 20px; padding-bottom: 10px; width: 800px">
	<br>
	<h4 style="padding-top: 6px;">Research Management Table</h4>
	<table id="resMgmtTblDivTable" class="table table-striped table-bordered"></table>
</div>

<script type="text/javascript">
	$(document)
			.ready(
					function() {
						$("#resMgmtTblDivTable")
								.idtTable(
										{
											idtUrl : "<s:url value='/study/studyAction!getResearchMgmtSet.action' />",
											idtData : {
												primaryKey : "rowId"
											},
											autoWidth : false,
											pages : 1,
											processing : false,
											serverSide : false,
											length : 15,
											dom : 'Bfrtip',
											columns : [

											{
												data : "role",
												title : "ROLE",
												name: "role",
												parameter : "roleTitle"
											}, 
											{
												data : "name",
												title : "FULL NAME",
												name: "name",
												parameter : "fullName"
											}, 
											{
												data : "email",
												title : "E-MAIL",
												name: "email",
												parameter : "email"
											}, 
											{
												data : "organization",
												title : "ORGANIZATION",
												name: "organization",
												parameter : "orgName"
											}, 
											{
												data : "orcId",
												title : "ORCID",
												name: "orcId",
												parameter : "orcId",
												render : function(data, type, row, full) {
													return "<a target='_blank' href=https://orcid.org/" + data + ">"+data+"</a>";
												}

											}, 
											{
												data : "roleId",
												title : "",
												parameter : "role.id"

											},
											{
												data : "firstName",
												title : "",
												parameter : "firstName"

											}, 
											{
												data : "mi",
												title : "",
												parameter : "mi"

											}, 
											{
												data : "lastName",
												title : "",
												parameter : "lastName"

											}, 
											{
												data : "suffix",
												title : "",
												parameter : "suffix"

											},
											{
												data : "id",
												title : "",
												parameter : "id"

											}											

											],
											aoColumnDefs : [ {
												"aTargets" : [ 6, 7, 8, 9, 10, 11 ],
												"visible" : false
											} ],
											select : "multi",
											buttons : [
													{
														text : 'Edit',
														className : "editResMgmtBtn",
														enabled : false,
														enableControl : {
															count : 1,
															invert : true
														},
														action : function(e,dt, node,config) {
															var selectedRow = dt.row({selected : true}).data();
															var researchMgmtJson = getSelectedResMgmtJson(selectedRow);
															populateResearchMgmtForm(researchMgmtJson, "edit");
														}
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
																var researchMgmtJson = getSelectedResMgmtJson(selectedRows[i]);
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

													} ]
										});
						
						if($('#pictureFileName').val() != ""){ console.log("error true: "+$('#pictureFileName').val());
						   convertFileUpload($('#pictureFile'), $('#pictureFileName').val());						
						}

					});
</script>




<script type="text/javascript">
	$('document').ready(function() {
		//$("#displayResearchMgmt").show(); 
		
		checkPrimaryPI();
	});

	function checkPrimaryPI() {               
		var hasPrimaryPI = <s:property value="sessionStudy.study.hasPrimaryPI" />;
		if (hasPrimaryPI == true) {
			$("#roleSelect option[value=0]").attr("disabled", "disabled");
		} else {
			$("#roleSelect option[value=0]").removeAttr('disabled');
		}
	}


	
	function addResearchMgmt() {
        
		
		var roleId = $('#roleSelect option:selected').val();
	
		var formData = new FormData();
		formData.append('researchMgmtEntry.role', roleId);
		formData.append('researchMgmtEntry.firstName', $('#firstName').val());
		formData.append('researchMgmtEntry.mi', $('#mi').val());
		formData.append('researchMgmtEntry.lastName', $('#lastName').val());
		formData.append('researchMgmtEntry.suffix', $('#suffix').val());
		formData.append('researchMgmtEntry.email', $('#email').val());
		formData.append('researchMgmtEntry.orgName', $('#orgName').val());
		formData.append('researchMgmtEntry.orcId', $('#orcId').val());

		if (($("#pictureFile"))[0].files.length > 0) {
			formData.append('picture', ($("#pictureFile"))[0].files[0]);
		}

		$.ajax({
			type : "POST",
			url : "researchMgmtValidationAction!addResearchManagement.ajax",
			data : formData,
			cache : false,
			processData : false,
			contentType : false,
			success : function(data) {
				$('#researchMgmt').html(data);
				if($('#researchMgmt .error-text').length > 0){
					//$("#displayResearchMgmt").show();
					openResearchMgmtDialog('add');
				}
				else {
					//$("#displayResearchMgmt").show();
					buildDataTables();
					checkPrimaryPI();
				}
			}
		});
	}

	function getSelectedResMgmtJson(selectedRow) {

		var researchMgmtJson = {
			id 				: selectedRow.id,
			role 			: selectedRow.roleId,
			firstName 		: encodeURI(selectedRow.firstName),
			mi 				: selectedRow.mi,
			lastName 		: encodeURI(selectedRow.lastName),
			suffix 			: selectedRow.suffix,
			email 			: encodeURI(selectedRow.email),
			orgName 		: encodeURI(selectedRow.organization),
			orcId 			: selectedRow.orcId
		};
		
		return researchMgmtJson;
	}

	function populateResearchMgmtForm(researchMgmtJson, mode) {
		var action = "";
		var dataToEdit = null;
		if (mode && mode == 'add'){
			action = "getEmptyResMgmtEntryToAdd.ajax";
		}
		else{
			action = "getResMgmtEntryToEdit.ajax";
			dataToEdit = "researchMgmtEntryJson="
				+ encodeURIComponent(JSON.stringify(researchMgmtJson));
		}
		$.ajax({
					type : "POST",
					url : "studyAction!"+action,
					data : dataToEdit,
					success : function(data) {
						$('#researchMgmt').html(data);
						if (mode && mode == 'add'){
							openResearchMgmtDialog('add');
						}else{
							openResearchMgmtDialog("edit", JSON
								.stringify(researchMgmtJson));
						}
						buildDataTables();
						checkPrimaryPI();
					}
				});
	}
	
	//  because struts does horrible things sometimes
	var StrutsUtils = {
		addEventListener : function() {}
	};

	function editResearchMgmt(selectedRow) {
		var roleId = $('#roleSelect option:selected').val();
	
		var formData = new FormData();
		formData.append('researchMgmtEntry.id',$('#researchMgmtId').val());
		formData.append('researchMgmtEntry.role', roleId);
		formData.append('researchMgmtEntry.firstName', $('#firstName').val());
		formData.append('researchMgmtEntry.mi', $('#mi').val());
		formData.append('researchMgmtEntry.lastName', $('#lastName').val());
		formData.append('researchMgmtEntry.suffix', $('#suffix').val());
		formData.append('researchMgmtEntry.email', $('#email').val());
		formData.append('researchMgmtEntry.orgName', $('#orgName').val());
		formData.append('researchMgmtEntry.orcId', $('#orcId').val());
		formData.append('selectedResMgmtToEdit', selectedRow);

		if (($("#pictureFile"))[0].files.length > 0) {
			formData.append('picture', ($("#pictureFile"))[0].files[0]);
		}
		$.ajax({
			type : "POST",
			url : "researchMgmtValidationAction!editResearchManagement.ajax",
			data : formData,
			cache : false,
			processData : false,
			contentType : false,
			success : function(data) {
				$('#researchMgmt').html(data);
				
				if($('#researchMgmt .error-text').length > 0){
				
					openResearchMgmtDialog("edit", selectedRow);
				}
				else {
					buildDataTables();
					checkPrimaryPI();
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
		form.attr("action",
				"researchMgmtValidationAction!addResearchManagement.action");
		form.attr("method", "post");
		form.attr("enctype", "multipart/form-data");

		var rmDiv = $("#researchMgmt").clone();
		//var showDiv = $("#hideResearchMgmt").show();
		form.append(rmDiv);
		//form.append(showDiv);
		form.submit();
		return;
	}

	function refreshResearchMgmt() {
		$('#picIframe').remove();
		$('#iframeForm').remove();
	}

	function removeResearchMgmt(researchMgmtJson) {

		$.ajax({
			type : "POST",
			url : "studyAction!removeResearchMgmt.ajax",
			data : {
				"researchMgmtJson" : researchMgmtJson
			},
			"async" : true,
			success : function(data) {
				$('#researchMgmt').html(data);
				buildDataTables();
				checkPrimaryPI();
			}
		});
	}

	function openResearchMgmtDialog(mode, selectedRow) {
		
		$("#resMgmtDialog").dialog({
			autoOpen : false,
			height : 520,
			width : 1050,
			position : {
				my : "center",
				at : "center",
				of : window
			},
			buttons : [ {
				id : "sbmtResMgmtBtn",
				text : "Submit",
				click : function() {
					if (mode == "add") {
						addResearchMgmt();
					} else if (mode == "edit") {
						editResearchMgmt(selectedRow);
					}
					$(this).dialog("close");
					
				}
			}, {
				id : "cancelResMgmtBtn",
				text : "Cancel",
				click : function() {
					clearResearchMgmtEntry();
					$("#resMgmtDialog").dialog("close");
				}
			} ],

			close : function() {
				clearResearchMgmtEntry();
				$(this).dialog('destroy');
			}
		});
		$("#resMgmtDialog").dialog("open");
		
	}

	function clearResearchMgmtEntry() {
		$('#roleSelect').val("");
		$('#firstName').val("");
		$('#mi').val("");
		$('#lastName').val("");
		$('#suffix').val("");
		$('#email').val("");
		$('#orgName').val("");
		$('#orcId').val("");
		$('#pictureFile').val("");
		$('#pictureFileName').val("");
	}
</script>
