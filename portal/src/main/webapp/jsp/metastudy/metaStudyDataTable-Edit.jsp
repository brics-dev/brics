<%@include file="/common/taglibs.jsp"%>
<div class="ui-clearfix">
	<div class="button-right btn-downarrow ">
		<input id="addDataBtn" type="button" value="Add Data" />
		<div id="selectAddDataDiv" class="btn-downarrow-options">
			<p>
				<a class="lightbox"
					href="metaStudyDataAction!addSavedQueryDialog.ajax">Saved Query</a>
				<a class="lightbox"
					href="metaStudyDataAction!addDataFileDialog.ajax">File</a>
			</p>
		</div>
	</div>
</div>
<br />
<div id="metaStudyDataListContainer" class="idtTableContainer">
  	<div id="dialog" class="ibisMessaging-dialogContainer"></div>
	<table id="metaStudyDataListTable" class="table table-striped table-bordered" width="100%"></table>
</div>
<script type="text/javascript">
	$(document).ready(function() {
		$('#metaStudyDataListTable').idtTable({
			idtUrl: "<s:url value='metastudy/metaStudyDataAction!getUploadMetaStudyData.action' />",
			pageLength: 15,
			dom: 'Bfrtip', 
			idtData: {
				primaryKey: 'name'
			},
			"columns": [
	              {
	                  "data": 'dataNameLink',
	                  "title":'Data',
	                  "name":'Data',
	                  "parameter" : 'dataNameLink',
	                  "searchable": true,
	                  "orderable": true,

	              },
	              {
	                  "data": 'source',
	                  "title": 'Source',
	                  "name": 'Source',
	                  "parameter" : 'source',
	                  "searchable": true,
	                  "orderable": true,

	              },
	              {
	                  "data": 'fileType',
	                  "title": 'Type',
	                  "name": 'Type',
	                  "parameter" : 'fileType.name',
	                  "searchable": true,
	                  "orderable": true,

	              },	           
	              {
	                  "data": 'description',
	                  "title": 'Description',
	                  "name": 'Description',
	                  "parameter" : 'description',
	                  "searchable": true,
	                  "orderable": true,
	                  "render": IdtActions.ellipsis(35)
	              }           
	           ],
	           select: 'multi',
	           buttons: [
		        	  {
		        		 text: 'Edit',
		        		 className: 'idt-EditButton',
		        		 enabled: false,
		        		 enableControl: {
	                         count: 1,
	                         invert: true
	                     },
		        		 action: function(e, dt, node, config) {
		        			var rows = dt.rows('.selected').data();
		        	        var rowsIDs = [];
		        	        for (var i = 0; i < rows.length; i++) {
		        	            rowsIDs.push(rows[i].DT_RowId)
		        	        }
		        			 $.ajax({
		        					type : "POST",
		        					cache : false,
		        					url : "metaStudyDataAction!editData.ajax?metaStudyDataName=" + encodeURIComponent(rowsIDs),
		        					success : function(data) {
		        						$.fancybox(data);
		        					}
		        			});
		        		 }
		        	  },
		        	  {
			        	   	 extend: 'delete',
			        	   	 className: 'idt-DeleteButton',
		        	    	 action: function(e, dt, node, config) {
			        	   		var msgText = "Are you sure you want to delete the item(s)?";
			        	   		var yesBtnText = "Delete";
			        	   		var noBtnText = "Do Not Delete";
			        			var rows = dt.rows('.selected').data();
			        	        var rowsIDs = [];
			        	        for (var i = 0; i < rows.length; i++) {
			        	            rowsIDs.push(rows[i].DT_RowId)
			        	        }
			        	   		var action = "metaStudyDataAction!removeData.action?metaStudyDataName=" + rowsIDs;

			        	   		confirmationEditDialog("warning", msgText, yesBtnText, noBtnText, action,
			        	   				true, "400px", "Confirm Deletion", rowsIDs);
			        	   	} 
			        	  },		        	 
		           ]	   
		})
	})
</script>

<script type="text/javascript" src="/portal/js/metastudy/metaStudy.js"></script>