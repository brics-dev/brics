<%@include file="/common/taglibs.jsp"%>

	<h4 style="padding-left:30px">Clinical Trial ID Table</h4>
	<div class="form-field">
		<label for="clinicalTrialId">Clinical Trial ID :</label>
		<s:textfield id="clinicalTrialId" name="clinicalTrialId" cssClass="textfield float-left" maxlength="55" />
		
		<div class="button" style="padding-left:20px;">
			<input type="button" id="ctButton" value="Add to Table" onclick="javascript:addClinicalTrial()" />
		</div>
		<s:fielderror fieldName="clinicalTrialId" cssStyle="float:left" />
		<div class="special-instruction">Example: NCT00483444</div>
	</div>
	
	<div id="clinicalTrialContainer" class="idtTableContainer" style="padding-left:50px; padding-bottom:10px; width:800px">
		<table id="clinicalTrialTable" class="table table-striped table-bordered" width="100%"></table>
	</div>


<script type="text/javascript">
	$(document).ready(function() {
			$('#clinicalTrialTable').idtTable({
			idtUrl: "<s:url value='/metastudy/metaStudyAction!getClinicalTrialMetaSet.action' />",
			idtData: {
				primaryKey: "id"
			},			
	        "columns": [
	              {
	                  "data": 'clinicalTrialId',
	                  "title":'Clinical Trial ID',
	                  "name":'Clinical Trial ID',
	                  "parameter" : 'clinicalTrialId',
	                  "searchable": true,
	                  "orderable": true             
	              },
	              {
	                  "data": 'removeLink',
	                  "title":'Actions',
	                  "name":'Actions',
	                  "parameter" : 'removeLink',
	                  "searchable": true,
	                  "orderable": true
	              }	          
	          ]
		});
	});
	function addClinicalTrial() {
		var ctId = $('#clinicalTrialId').val();
		var oTable = $('#clinicalTrialTable').idtApi('getTableApi');
		
		$.ajax({
			type: "POST",
			url: "clinicalTrialMetaValidationAction!addClinicalTrial.ajax",
			data: "clinicalTrialId=" + ctId,
			"async": true,
			success: function(data) {
				$('#ctTableDiv').html(data)
				oTable.ajax.reload();
			}
		});
	}
	
	function removeClinicalTrial(ctIdToRemove) {
		var oTable = $('#clinicalTrialTable').idtApi('getTableApi');
		$.ajax({
			type: "POST",
			url: "metaStudyAction!removeClinicalTrial.ajax",
			data: "ctIdToRemove=" + ctIdToRemove,
			"async": true,
			success: function(data) {
				$('#ctTableDiv').html(data);
				oTable.ajax.reload();
			}
		});
	}
	
	function viewClinicalTrial(clinicalTrialId) {
		$.post(	"metaStudyAction!viewClinicalTrial.ajax", 
			{ clinicalTrialId:clinicalTrialId }, 
			function (data) {
				$.fancybox(data);
			}
		); 
	}
</script>