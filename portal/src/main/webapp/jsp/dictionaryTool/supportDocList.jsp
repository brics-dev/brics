<%@include file="/common/taglibs.jsp"%>
<div id="documentationContainer" class="idtTableContainer">
  	<div id="dialog"></div>
	<s:hidden id="eventLogSupportDocSize" name="eventLogSupportDocSize" />
	<s:hidden id="documentationLimit" name="documentationLimit" />
	<table id="documentationTableTable" class="table table-striped table-bordered" width="100%"></table>
</div>
<script type="text/javascript">
	$(document).ready(function() {
		console.log('currentAction', $('#currentAction').val());
		var currentAction = $('#currentAction').val();
		$('#documentationTableTable').idtTable({
			idtUrl: currentAction+ "!getEventLogDocumentationList.action",
			pageLength: 15,
			dom: 'Bfrtip',
			idtData: {
				primaryKey: 'docNameLink'
			},
			"columns": [
	              {
	                  "data": 'docNameLink',
	                  "title":'NAME',
	                  "name":'NAME',
	                  "parameter" : 'docNameLink',
	                  "searchable": true,
	                  "orderable": true,

	              },
	              {
	                  "data": 'description',
	                  "title": 'DESCRIPTION',
	                  "name": 'DESCRIPTION',
	                  "parameter" : 'description',
	                  "searchable": true,
	                  "orderable": true,
	                  "render": IdtActions.ellipsis(35)
	              }           
	           ],
	           select: 'multi',
	           buttons: [
	        	  {
	        	   	 extend: 'delete',
	        	   	 className: 'idt-DeleteButton',
        	    	 action: function(e, dt, node, config) {
	        	   		var actionName = $("#actionName").val();
	        	   		var msgText = "Are you sure you want to delete the item(s)?";
	        	   		var yesBtnText = "Delete";
	        	   		var noBtnText = "Do Not Delete";
	        	   		var action = actionName + "!removeDocumentations.action"; 

	        	   		ConfirmationDialogDelete("warning", msgText, yesBtnText, noBtnText, action,
	        	   				true, "400px", "Confirm Deletion");
	        	   	} 
	        	  },
	        	  {
	        		 text: 'Edit',
	        		 className: 'idt-EditButton',
	        		 enabled: false,
	        		 enableControl: {
                         count: 1,
                         invert: true
                     },
	        		 action: function(e, dt, node, config) {
	        			var actionName = $("#actionName").val();
	        			var rows = $('#documentationTableTable').idtApi('getSelectedOptions');
	        	        var rowsIDs = [];
	        	        for (var i = 0; i < rows.length; i++) {
	        	            rowsIDs.push(rows[i]);
	        	        }
	        	        var fileData = new FormData();
	        	    	fileData.append('supportingDocName', rowsIDs.toString());
        				$.ajax({
        					type : 'POST',
        					data : fileData,
        					cache : false,
        					processData : false,
        					contentType : false,
        					url : actionName + "!editDocumentation.ajax",
        					success : function(data) {
        						//$.fancybox(data);       						
        						$("#bricsDialog_0").html(data).dialog("open");
        					}
        				});	        			 
	        		 }
	        	  }
	           ]
		})
	})
</script>