<div id="documentTableAndButtons">
<%@include file="/common/taglibs.jsp"%>
<div class="ui-clearfix">
	<s:if test="%{sessionObject.supportingDocumentationSet.size() < documentationLimit || documentationLimit < 0}">
		<div id="addDocBtnDiv" class="button button-right btn-downarrow">
				<input id="addDocBtn" type="button" value="Add Documentation" />
				<div id="selectAddDocDiv" class="btn-downarrow-options">
					<p>
						<a class="lightbox"
							href="${actionName}!addDocDialog.ajax?addDocSelect=url">URL</a>
						<a class="lightbox"
							href="${actionName}!addDocDialog.ajax?addDocSelect=file">File</a>
					</p>
				</div>
		</div>
	</s:if>
	<s:else> 
		<div id="addDocBtnDiv" class="button button-right disabled btn-downarrow " title="The maximum amount of documents allowed to be uploaded is ${documentationLimit}.">
				<input id="addDocBtn" disabled="true" type="button" value="Add Documentation"/>
				<div id="selectAddDocDiv" class="hidden btn-downarrow-options">
					<p>
						<a class="lightbox"
							href="${actionName}!addDocDialog.ajax?addDocSelect=url">URL</a>
						<a class="lightbox"
							href="${actionName}!addDocDialog.ajax?addDocSelect=file">File</a>
					</p>
				</div>
		</div>
	</s:else>
</div>
<br />

<s:hidden id="documentationLimit" name="documentationLimit" />
<s:hidden id="actionName" name="actionName" />

<div id="documentationDetailsDialog"  style="display:none"></div>
<div id="documentationTable" class="idtTableContainer">
  	<div id="dialog" class="ibisMessaging-dialogContainer"></div>
	<table id="documentationTableTable" class="table table-striped table-bordered" width="100%"></table>
</div>
<script type="text/javascript">
	$(document).ready(function() {
		var actionName = $("#actionName").val();
		$('#documentationTableTable').idtTable({
			idtUrl: "<s:url value='"+actionName+"!getUploadDocumentation.action' />",
			pageLength: 15,
			dom: 'Bfrtip',
			idtData: {
				primaryKey: 'name'
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
	                  "data": 'typeLink',
	                  "title": 'TYPE',
	                  "name": 'TYPE',
	                  "parameter" : 'typeLink',
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
	        			var rows = dt.rows('.selected').data();
	        	        var rowsIDs = [];
	        	        for (var i = 0; i < rows.length; i++) {
	        	            rowsIDs.push(rows[i].DT_RowId)
	        	        }
	        	   		var action = actionName + "!removeDocumentations.action?supportingDocName="
	        	   				+ rowsIDs;

	        	   		EditConfirmationDialog("warning", msgText, yesBtnText, noBtnText, action,
	        	   				true, "400px", "Confirm Deletion", rowsIDs);
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
	        			var rows = dt.rows('.selected').data();
	        	        var rowsIDs = [];
	        	        for (var i = 0; i < rows.length; i++) {
	        	            rowsIDs.push(rows[i].DT_RowId)
	        	        }
	        			 $.ajax({
	        					type : "POST",
	        					cache : false,
	        					url : actionName + "!editDocumentation.ajax?supportingDocName=" + encodeURIComponent(rowsIDs),
	        					success : function(data) {
	        						$.fancybox(data);
	        					}
	        			});
	        		 }
	        	  }
	           ]
		})
	})
</script>
<br />
<script type="text/javascript" src='<s:url value="/js/uploadDocumentations.js"/>'></script>
<script type="text/javascript">


	$("#addDocBtn").click( function(e) {
		$("#selectAddDocDiv").toggle();
	});
		
</script>
</div>