<%@include file="/common/taglibs.jsp"%>

<div id="addFileDialog" style="display: none">
	<div id="addFileInterface"></div>
</div>
<div class="margin-top-lg">
	<h3>Account Administrative Notes</h3>
	<br>
	<div id="accountAdministrativeNotesTableContainer" class="idtTableContainer">
		<div id="dialog"></div>
		<table id="accountAdministrativeNotesTable" class="table table-striped table-bordered"></table>
	</div>
</div>


<script type="text/javascript">
	$(document).ready(function(){
		$('#accountAdministrativeNotesTable').idtTable({
			idtUrl: "<c:out value='${param.idtUrl}'/>",
			autoWidth: false,
			order: [[ 2, "desc" ]],
			dom: "Bftrip",
			"columns" : [{
				"data" : "getAccountName",
				"title" : "ACCOUNT NAME",
				"name" : "Name",
				"parameter": "accountName",
				"width": "45%"
			},{
				"data" : "note",
				"title" : "NOTE",
				"name" : "Note",
				"parameter": "note",
				"render": IdtActions.ellipsis(255),
				"width": "45%"
			},{
				"data" : "date",
				"title" : "DATE",
				"name" : "Date",
				"parameter": "date",
				"width": "10%",
			}],
			buttons : [
				{
	     		 text: "Add Note",
	     		 className: 'idt-createProtocolBtn',
	     		 enabled: true,
	    		 enableControl: {
	                    count: 0,
	                    invert: false
	             },
	  	    	 action: function(e, dt, node, config) {
	  	    		addAdminNote();
	      	   		}
				}
			]
		})
	});
</script>
<script type="text/javascript">

function addAdminNote() {
	var actionUrl = "accountAdministrativeNotesValidationAction!input.ajax";
	
	$.ajax({
		type: "GET",
		url:actionUrl,
		data: {},
		success:function(data) {
			$("#addFileInterface").html(data);
			
			var fileDialog = $("#addFileDialog").dialog({
				title: "Add Administrative Note",
				height: 400,
				width: 500,
				buttons : [
					{
						id: "cancelBtn",
						text: "Cancel",
						click: function() {
							$(this).dialog('close');
						}
					},
					{
						id: "addBtn",
						text: "Add Note",
						click: function() {
							var accountAdministrativeNoteText = $("#administrativeNote").val();
							
							var postData = new FormData();
							postData.append("accountAdministrativeNoteText", accountAdministrativeNoteText);
							
							$.ajax({
								type: "POST",
								url: "accountAdministrativeNotesValidationAction!addAccountAdmininistrativeNote.ajax",
								data: postData,
								cache : false,
								processData : false,
								contentType: false,
								success: function(data) {
									if (data == "landing") {
										window.location.href = "/portal/baseAction!landing.action";
									} else {
										$('#accountAdministrativeNotesTable').DataTable().ajax.reload();
									}
								}
							});
							
							$(this).dialog('close');
							$(this).dialog('destroy');
						}
					}
					]
			});
		}
	
	});
}
</script>