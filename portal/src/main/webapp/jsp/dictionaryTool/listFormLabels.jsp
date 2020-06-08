<%@include file="/common/taglibs.jsp"%>
<input type="hidden" id="inAdmin" name="inAdmin" value="${inAdmin}" />

<title>Manage Form Labels</title>

<div class="border-wrapper" style="min-height:300px;">
	<jsp:include page="../navigation/dataDictionaryNavigation.jsp" />
	<h1 class="float-left">(Admin) Manage Form Labels</h1>
	<div style="clear:both;"></div>

<div id="main-content">

	<form id="labelForm">
	
		<div id="formLabelDialog" title="Form Label" style="display: none;"><br/>
			<jsp:include page="formLabelDialog.jsp" />
		</div>

		<div class="form-field inline-right-button">
			<div class="button" style="padding-right: 300px">
				<input type="button" id="addLabelDlg" value="Add to Table" onclick="javascript:openFormLabelDialog('add');" />
			</div>
		</div>
		
		<div id="dialog"></div>
		<div id="formLabelTblDiv" class="idtTableContainer" 
				style="padding-left: 50px; padding-top: 20px; padding-bottom: 10px; width: 800px"><br>
			<table id="formLabelTblDivTable" class="table table-striped table-bordered"></table>
		</div>
		
	</form>
</div>
</div>

<script type="text/javascript">

setNavigation({"bodyClass":"primary", "navigationLinkID":"dataDictionaryModuleLink", "subnavigationLinkID":"defineDataToolsLink", "tertiaryLinkID":"manageFormLabelsLink"});

$(document).ready(
	function() {
		$("#formLabelTblDivTable")
			.idtTable({
				idtUrl : "<s:url value='/dictionaryAdmin/formLabelAction!getFormLabelOutput.action' />",
				idtData : {
					primaryKey : "id"
				},
				autoWidth : false,
				pages : 1,
				processing : false,
				serverSide : false,
				length : 15,
				dom : 'Bfrtip',
				columns : [
					{
						data : "label",
						title : "LABEL",
						name: "label",
						parameter : "label"
					}, 
					{
						data : "createdBy",
						title : "CREATED BY",
						name: "createdBy",
						parameter : "createdBy"
					}, 
					{
						data : "id",
						parameter : "id",
						visible : false
					}											
				],
				select : "multi",
				buttons : [
				{
					text : 'Edit',
					className : "idt-EditButton",
					enabled : false,
					enableControl : {
						count : 1,
						invert : true
					},
					action : function(e, dt, node,config) {
						var selectedRow = dt.row({selected : true}).data();
						openFormLabelDialog("edit", selectedRow);
					}
				},
				{
					extend : "delete",
					title : "Delete",
					action : function(e, dt, node,config) {
	
						var labelJsonArr = [];
						var selectedRows = $('#formLabelTblDivTable').idtApi('getSelectedOptions');
						for (var i = 0; i < selectedRows.length; i++) {
							var labelId = selectedRows[i];
							labelJsonArr.push(labelId);
						}
						$("#dialog").dialog({
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
										removeFormLabel(dt, selectedRows, JSON.stringify(labelJsonArr));
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
				}]
			});
	});

	function openFormLabelDialog(mode, selectedRow) {
		$.ajax({
			type : "POST",
			url : "formLabelAction!openFormLabelDialog.ajax",
			cache : false,
			success : function(data) {
				$("#formLabelDialog").html(data);
				
				if (mode == "edit") {
					$('#currentLabel').val(selectedRow.label);
					$('#labelId').val(selectedRow.id);
				}
				displayFormLabelDialog(mode, selectedRow);
			}
		});
	}
	
	function displayFormLabelDialog(mode, selectedRow) {
		$("#formLabelDialog").dialog({
			autoOpen : false,
			height : 200,
			width : 500,
			position : {
				my : "center",
				at : "center",
				of : window
			},
			buttons : [ {
				id : "submitLabelBtn",
				text : "Submit",
				click : function() {
					if (mode == "add") {
						addFormLabel();
					} else if (mode == "edit") {
						editFormLabel(selectedRow);
					}
				}
			}, {
				id : "cancelLabelBtn",
				text : "Cancel",
				click : function() {
					$(this).dialog("close");
				}
			} ],
	
			close : function() {
				$('#currentLabel').val("");
				$('#labelId').val("");
				$(this).dialog('destroy');
			}
		});
		$("#formLabelDialog").dialog("open");
	}
	
	function addFormLabel() {
		$.ajax({
			type : "POST",
			url : "formLabelValidationAction!addFormLabel.ajax",
			data : "currentLabel=" + encodeURI($('#currentLabel').val()),
			cache : false,
			success : function(data) {
				if (data == "success") {
					$("#formLabelDialog").dialog("close");
					$('#formLabelTblDivTable').DataTable().ajax.reload();
				} else {
					$("#formLabelDialog").empty().html(data);
				}
			}
		});
	}
	
	function editFormLabel(selectedRow) {
		var oldLabel = selectedRow.label;
		var newLabel = $("#currentLabel").val();

		if (oldLabel != newLabel) {
			$.ajax({
				type : "POST",
				url : "formLabelValidationAction!editFormLabel.ajax",
				data : {
					"currentLabel" : newLabel,
					"labelIdToEdit" : $("#labelId").val()
				},
				cache : false,
				success : function(data) {
					if (data == "success") {
						$("#formLabelDialog").dialog("close");
						$('#formLabelTblDivTable').DataTable().ajax.reload();
					} else {
						$("#formLabelDialog").empty().html(data);
					}
				}
			});
		} else {
			$("#formLabelDialog").dialog("close");
		}
	}
	
	function removeFormLabel(dt, selectedRows, labelJson) {
		$.ajax({
			type : "POST",
			url : "formLabelAction!deleteFormLabels.ajax",
			data : {
				"labelIdsJson" : labelJson
			},
			cache : false,
			success : function(data) {
				$('#formLabelTblDivTable').idtApi('deselectAll');
                for(var i = 0; i< selectedRows.length; i++) {
                    dt.row("#"+ selectedRows[i]).remove();
                }                               
                selectedRows.length = 0;                     
                $('#formLabelTblDivTable').idtApi('draw');
			}
		});
	}

</script>
