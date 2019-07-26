<%@include file="/common/taglibs.jsp"%>
<%@taglib prefix="s" uri="/struts-tags"%>

<div class="lightbox-content-wrapper">
	<h3>Select Saved Query</h3>
	
	<s:form id="selectSavedQueryForm" class="validate" method="post">
		<s:hidden name="isEditingData" />
		
		<div id="savedQueryListContainer" class="idtTableContainer">
		  	<div id="dialog" class="ibisMessaging-dialogContainer"></div>
			<table id="savedQueryListTable" class="table table-striped table-bordered" width="100%"></table>
		</div>

		<s:hidden id="savedQueryId" name="savedQueryId" />	
		<s:hidden id="savedQueryName" name="savedQueryName" />
		<s:hidden id="savedQueryDesc" name="dataDescription" />
		<s:hidden name="editSavedQueryId" />
	
		<div class="form-field" style="margin-bottom:30px;">
			<div class="button">
				<input type="button" class="submit" value="Select" onclick="selectSavedQuery()" />
			</div>
			<a class="form-link" href="#" onclick="cleanAndClose()">Cancel</a>
		</div>
			
	</s:form>
</div>
<div class="clearfix" style="clear:both"></div>

<script type="text/javascript">

	$(document).ready(function() {
		$('#savedQueryListTable').idtTable({
			idtUrl: "<s:url value='metastudy/metaStudyDataAction!getSavedQuerySet.action' />",
			pageLength: 10,
			idtData: {
				primaryKey: 'id'
			},
			autoWidth: false,
			"columns": [
	              {
	                  "data": 'dataNameLink',
	                  "title":'Saved Query Title',
	                  "name":'Saved Query Title',
	                  "parameter" : 'dataNameLink',
	                  "width": '30%',
	                  "searchable": true,
	                  "orderable": true,

	              },	           
	              {
	                  "data": 'description',
	                  "title": 'Description',
	                  "name": 'Description',
	                  "parameter" : 'description',
	                  "width": '60%',
	                  "searchable": true,
	                  "orderable": true,
	                  "render": IdtActions.ellipsis(85),
	     
	              }           
	           ],
	           select: 'single'	   
		});	
				
	    $(document).keyup(function(e) { 
	        if (e.keyCode == 27) { // esc keycode
	            cleanAndClose();
	        }
	    });
	});
	
	function selectSavedQuery() {
		
		var rowArray = $('#savedQueryListTable').idtApi('getSelectedOptions'),
			oTable =  $('#savedQueryListTable').idtApi('getTableApi');
		if (!rowArray || rowArray.length == 0) {
			alert("Please select a Saved Query to continue.");
			return;
		}
		
		var getRowData = $('#savedQueryListTable').idtApi('getApiRow', '.selected').data();
		var nameHtml = getRowData.dataNameLink;
		var id = getRowData.DT_RowId;
		var desc = getRowData.description;
		// because name is a link, grab the text
		var name = $(nameHtml).text();
		
		
		$("#savedQueryId").val(getRowData.DT_RowId);
		$("#savedQueryName").val(name);
		$("#savedQueryDesc").val(getRowData.description);
		
		$.ajax({
			type: "POST",
			url: "metaStudyDataAction!addSavedQueryDialog.ajax",
			data: $("#selectSavedQueryForm").serializeArray(),
			success: function(data) {
				$.fancybox(data);
			}
		}); 
		
	}
	
	function cleanAndClose(){
		$.fancybox.close();
	}

</script>
