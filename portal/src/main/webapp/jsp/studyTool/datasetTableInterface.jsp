<%@include file="/common/taglibs.jsp"%>

<div id="datasetTableWrapper" class="idtTableContainer">
	<table id="datasetTable" class="table table-striped table-boardered"></table>
</div>
<script type="text/javascript">
	$(document).ready(function() {
		$('#datasetTable').idtTable({
			idtUrl: "<s:url value='/study/studyDatasetAction!datasetDatatable.ajax' />",
			idtData : {
				primaryKey: "id"
			},
			serverSide: true,
			processing: true,
			pages: 3,
			select: "multi",
			selectParams: {
				selectAllEnabled: false,
				selectNoneEnabled: true,
				selectFilteredEnabled: true,
		  },
			drawCallback : function(settings) {
				var api = new $.fn.dataTable.Api( settings );
				api.on("select", function(e, dt, type, index) {
					var row = api.row(index);
					updateStatusOptions(row, api);
					//var selectedLen = $(this).idtApi("getSelected").length;
					//console.log("external select " + type + " count: " + selectedLen);
				})
				.on("deselect", function(e, dt, type, index) {
					var row = api.row(index);
					updateStatusOptions(row, api);
					//console.log("external deselect");
				});
			},
			rowCallback : function(row, data, index) {
				if (data.status == "Deleted") {
					var $table = $("#datasetTable");
					$table.idtApi("disableRow", $table.idtApi("getApiRow", row));
				}
			},
			filters: [
				{
					columnIndex: 3,
					type: 'select',
					name: 'filterStatus',
					label: 'Show all',
					defaultValue: 'hide',
					options: [
						{
							value: "hide",
							label: "Show shared & private"
						}
					]
				}
			],
			columns: [
				{
					title:"DATASET ID",
					data: "prefixId",
					name:"prefixId",
					parameter: "prefixedId",
					width: "20%"
				},
				{
					title:"NAME",
					data: "name",
					name:"name",
					parameter: "nameLink"
				},
				{
					title:"SUBMISSION DATE",
					data: "submitDate",
					name:"submitDate",
					parameter: "submitDate",
					render: IdtActions.formatDate()
				},
				{
					title:"TYPE",
					data: "type",
					name:"type",
					parameter: "fileTypeString"
				},
				{
					title:"STATUS",
					data: "status",
					name:"status",
					parameter: "statusName"
				},
				{
					title: "",
					data: "requestedStatus",
					name: "requestedStatus",
					parameter: "requestedStatus",
					visible: false
				}
			],
			initComplete: function(){

				$('#datasetTable_wrapper').find(".idt_searchInput").unbind().on("keyup", _.debounce( function(e) {
		    	
					var oTable = $("#datasetTable").idtApi('getTableApi');
					oTable.clearPipeline().draw();	
					
	  			}, 100, true));

				
			}//end intiComplete
		});
	});
</script>

<h3>Dataset Actions</h3>
<div class="form-field">
	<label for="datasetStatusSelect" class="required">Change
		Status <span class="required">* </span>:
	</label> <select id="datasetStatusSelect" name="datasetStatusSelect"
		class="float-left" disabled>
		<option value="">Select a Dataset</option>
	</select>
	<div id="changeStatusButtonDiv" class="button margin-left disabled">
		<input id="changeStatusButton" disabled="" type="button" value="Change Status"
			onclick="javascript:changeStatus()" />
	</div>
	<s:fielderror fieldName="datasetStatusSelect" />
</div>
<script type="text/javascript">	
	var showLightBox = true;

	function viewDataset(prefixedId, isAdmin) {
		//$.fancybox.showActivity();
		console.log(' viewDataset' + showLightBox);

		if(showLightBox){
			showLightBox = false;
			var action = "/portal/study/datasetAction!viewLightbox.ajax";
			if(isAdmin == "true"){
				action = "/portal/studyAdmin/datasetAction!viewLightbox.ajax";
			}
			$.post(action, 
				{ prefixedId:prefixedId }, 
				function (data) {
					$.bricsDialog(data);
					$("#fancybox-wrap").unbind('mousewheel.fb');
					showLightBox = true;
				}
			);
			console.log(' after complete' + showLightBox);

		}
	}
</script>