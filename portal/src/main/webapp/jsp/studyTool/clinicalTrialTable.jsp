<%@include file="/common/taglibs.jsp"%>

	<h4 style="padding-left:30px">Clinical Trial ID Table</h4>
	<div class="form-field">
		<label for="clinicalTrialId">Clinical Trial ID :</label>
		<s:textfield id="clinicalTrialId" name="clinicalTrialId" cssClass="textfield float-left" maxlength="55" />
		<s:fielderror fieldName="clinicalTrialId" cssStyle="float:left" />
		
		<div class="inline-right-button button" style="padding-right:300px">
			<input type="button" id="ctButton" value="Add to Table" onclick="javascript:addClinicalTrial()" />
		</div>
		<div class="special-instruction">Example: NCT00483444</div>
	</div>
	
	<div id="clinicalTrialTblDiv" class="idtTableContainer" style="padding-left:50px; padding-bottom:10px; width:800px">
		<table id="clinicalTrialTbl" class="table table-striped table-bordered"></table>
	</div>
	<script type="text/javascript">
		$(document).ready(function(){
			$("#clinicalTrialTbl").idtTable({
				idtUrl: "<s:url value='/study/studyAction!getClinicalTrialSet.action' />",
				idtData: {
					primaryKey: "id"
				},
				processing: false,
				serverSide: false,
				length: 15,
				select: false,
				columns: [
					{
						data: "trialid",
						title: "CLINICAL TRIAL ID",
						name: "trialid",
						parameter: "clinicalTrialId",
						render: function(data, type, row, full) {
							return "<a href='javascript:;' onclick='viewClinicalTrial(\"" + data + "\")'>" + data + "</a>";
						}
					},
					{
						data: "actions",
						title: "ACTIONS",
						name: "actions",
						parameter: "clinicalTrialId",
						render: function(data, type, row, full) {
							return "<a href='javascript:;' onclick='removeClinicalTrial(\"" + data + "\")'>Remove</a>";
						}
					}
				]
			});
		});
	</script>

<script type="text/javascript">
	function addClinicalTrial() {
		var ctId = $('#clinicalTrialId').val();
		
		$.ajax({
			type: "POST",
			url: "clinicalTrialValidationAction!addClinicalTrial.ajax",
			data: "clinicalTrialId=" + ctId,
			"async": true,
			success: function(data) {
				$('#ctTableDiv').html(data);
				buildDataTables();
			}
		});
	}
	
	function removeClinicalTrial(ctIdToRemove) {
		$.ajax({
			type: "POST",
			url: "studyAction!removeClinicalTrial.ajax",
			data: "ctIdToRemove=" + ctIdToRemove,
			"async": true,
			success: function(data) {
				$('#ctTableDiv').html(data);
				buildDataTables();
			}
		});
	}
	
	function viewClinicalTrial(clinicalTrialId) {
		$.post(	"studyAction!viewClinicalTrial.ajax", 
			{ clinicalTrialId:clinicalTrialId }, 
			function (data) {
				$.fancybox(data);
			}
		);
	}
</script>