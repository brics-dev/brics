<%@ taglib uri="/struts-tags" prefix="s" %>
<%@ taglib uri="/WEB-INF/datatables.tld" prefix="idt" %>

<jsp:include page="/patient/templates/dataElementPrePopFields.jsp" />

<div id="addProtoMStoneReminder" style="height: 50px; width: 858px;">
	<span class="reminder-message" style="float: left"> <b>Please click "Add Protocol Milestones" button to add to the Milestones Table.</b>
	</span>
	<div style="padding-right: 100px; float:right;">
		<input type="button" id="openProtoMilesStoneDlg" value="Add Protocol Milestones" onclick="javascript:openProtoMilesStoneDialog('add', null);" />
	</div>
</div>

<div class="idtTableContainer brics" id="protoMilesStoneDTTableDiv" style="width: 858px;">
	<table id="protoMilesStoneDTTable" class="table table-striped table-bordered" width="100%">
	</table>   
</div>

<div id="protoMilesStoneDlg" title="Add Protocol Milestones" style="display: none;">
	<div id="protoMilesStoneErrMsgContainer"></div>
	<s:hidden name="protoMileStoneId" id="protoMileStoneId" />

	<div>
		<div class="formrow_1">
			<label for="protoMilesStoneName" class="requiredInput protocolDlgInputlabel">
				<s:text name="protocol.add.milesStones.name"/>
			</label>
			<div class="protocolDlgInput">
			<s:textfield name="protoMilesStoneName" id="protoMilesStoneName" maxlength="200" style="width: 600px;"/>
			</div>
		</div>
		<div class="formrow_1">
			<label for="protoMilesStoneDate" class="requiredInput protocolDlgInputlabel">
				<s:text name="protocol.add.milesStones.date"/>
			</label>
			<div class="protocolDlgInput">
			<s:textfield name="protoMilesStoneDate" cssClass="dateTimeField validateMe" id="protoMilesStoneDate" size="20"  maxlength="50" style="width: 600px;position: relative; z-index: 100000" />
			</div>
		</div>
	</div>
</div>

<script type="text/javascript">

	function MilesStone (id, name, mileStoneDate){
		this.id = id;
		this.name = name;
		this.milesStoneDate = mileStoneDate;
	}
	
	

	var basePath = "<s:property value='#webRoot'/>";
	$(document).ready(function() {
		
		$("#protoMilesStoneDTTable").idtTable({
			idtUrl: basePath + "/protocol/protocolMilesStoneAction!getProtoMilesStoneDTList.action",
			idtData: {
				primaryKey: 'id'
			},
			dom: 'Bfrtip',
			select: "multi",
			columns: [
	            {
	                name: 'id',
	                parameter: 'id',
	                data: 'id',
	                visible: false
	            },
	            {
	                name: 'name',
	                title: 'Name',
	                parameter: 'name',
	                data: 'name'
	            },
	            {
	                name: 'milesStoneDate',
	                title: 'Date',
	                parameter: 'milesStoneDate',
	                data: 'milesStoneDate',
	               	render: IdtActions.formatDate()
	            }
	        ],
	        buttons: [
	        	{
	        		extend : "delete",
	   				text: "Delete",
	   				className: "DeleteMSBtn",
	   				enabled: false,
	     	    	action: function(e, dt, node, config) {
    	    			
	        	   		var selectedRows = dt.rows('.selected').data().to$();
						var protoMilesStoneArr = [];
						// Convert the array of IDs to a comma delimited string list 
						for (var idx = 0; idx < selectedRows.length; idx++) {
							var protoMilesStoneJson = getSelectedMilesStoneJson(selectedRows[idx]);
							protoMilesStoneArr.push(protoMilesStoneJson);
						}

	        	   		var $table = $('#protoMilesStoneDTTable');
	        	   		var dlgId = $.ibisMessaging(
	        	   				"dialog", 
	        	   				"warning", 
	        	   				"Are you sure you wish to delete the selected Miles Stone(s)?",
	        	   				{
	        	   					id: 'deleteRows',
	        	   					container: 'body',
	        	   					buttons: [{
	        	   						id: "yesBtnA",
	        	   						text: "Delete", 
	        	   						click: _.debounce(function() {
	        	   							$(this).siblings().find("#yesBtnA").prop("disabled", true);

	        	   							removeProtoMilesStone(JSON.stringify(protoMilesStoneArr), $table);
	        	   							$.ibisMessaging("close", {type: 'dialog'});

	        	   					
	        	   						}, 1000, true)
	        	   					},
	        	   					{
	        	   						text: "Do Not Delete",
	        	   						click: function() {
	        	   							$.ibisMessaging("close", {id: dlgId});					
	        	   						}
	        	   					}],
	        	   					modal: true,
	        	   					width: "400px",
	        	   					title: "Confirm Deletion"
	        	   				}
	        	   			);

	   				}
	   			},
	        	{
	   				text: "Edit",
	   				className: "editTPBtn",
	                enableControl: {
	                    count: 1,
	                    invert:true
	                },
	                enabled: false,
	   				action: function(e, dt, node, config) {
							
							var selectedRow = dt.row({selected : true}).data();
							
							if ( selectedRow.length == 0 ) {
								//$("#" + loadingMsgId).dialog("close");
								$.ibisMessaging("dialog", "info", "No time points are selected.");
								
								return false;
							}	else {
								//console.log("selectedRow: "+JSON.stringify(selectedRow));
								openProtoMilesStoneDialog('edit', selectedRow);
							}
	   				}
	   			}
	        ]
		});
		
	}); //end document.ready
	
	function populateProtoMilesStoneForm(selectedRow){
		 if(selectedRow != null){
			 var id = selectedRow.id;
			 var name = selectedRow.name;
			 var msDate = selectedRow.milesStoneDate;

			 $("#protoMileStoneId").val(id);
			 $("#protoMilesStoneName").val(name);
			 $("#protoMilesStoneDate").val(msDate);
		 }
	}
	 function openProtoMilesStoneDialog(mode, selectedRow) {
		 var editOrigName;
		 if(mode == "edit") {
			 editOrigName = selectedRow.name;
			 populateProtoMilesStoneForm(selectedRow);
		 }

			$("#protoMilesStoneDlg").dialog({
				autoOpen : false,
				height : 200,
				width : 900,
				position : {
					my : "center",
					at : "center",
					of : window
				},
				buttons : [ {
					id : "submitProtoMilesStoneBtn",
					text : "Submit",
					click : function() {

						if (validateProtoMilesStone( editOrigName )) {
							var protoMilesStone = getProtoMilesStoneFromDlg(mode);
							addOrEditProtoMilesStone(mode, protoMilesStone, editOrigName);
							$(this).dialog("close");
						}
					}
				}, {
					id : "cancelResMgmtBtn",
					text : "Clear List",
					click : function() {
						clearProtoMilesStoneEntry();
					}
				} ],

				close : function() {
					clearProtoMilesStoneEntry();
					$(this).dialog('destroy');
				}
			});
			$("#protoMilesStoneDlg").dialog("open");
	}	
	
	function validateProtoMilesStone( editOrigName ) {
		var valid = false;
		var errMsg = "";
		
		var inputName = $.trim($("#protoMilesStoneName").val());
		var inputDate = $.trim($("#protoMilesStoneDate").val());


		if (inputName.length > 0 && inputDate.length > 0){			
			valid = true;	
			errMsg = "";
		}
		if (inputName.length == 0) {
			errMsg +="<li>Miles stone name is required. Please input the name of miles stone.</li>";			
		}
		if (inputDate.length == 0) {
			errMsg +="<li>Miles stone date is required. Please input the date of miles stone.</li>";			
		}

		var $table = $("#protoMilesStoneDTTable");
		var allMilestones = $table.idtApi('getTableApi').rows().data();
		
		for (var idx = 0; idx < allMilestones.length; idx++) {
			var milestoneName = allMilestones[idx].name;

			//Skip the current entry if it's being edited.
			if( milestoneName != editOrigName )
			{
				if( milestoneName == inputName )
				{
					errMsg +="<li>Milestone name has to be unique.</li>";			
					valid = false;
				}
				
			}
		}
		
		if(errMsg.length > 0){
			errMsg = "<div style='color:red; text-align: left;'>Error: <ul>"+errMsg+"</ul><hr><br></div>";
		}

		$("#protoMilesStoneErrMsgContainer").html(errMsg);
		console.log("validate result: "+valid);
		return valid;
	}
	
	function getProtoMilesStoneFromDlg(mode) {
		var id = $("#protoMileStoneId").val();
		var name = $.trim($("#protoMilesStoneName").val());
		var mStoneDate = $.trim($("#protoMilesStoneDate").val());
		if (mode == "add"){
			status = "added";
		} 
		if (mode == "edit"){
			status = "edited";
		}
		
		var milesStone = new MilesStone(id, name, mStoneDate);
		
		return milesStone;
	}
	
	function getSelectedMilesStoneJson(selectedRow){
		var milesStone = new MilesStone(selectedRow.id, selectedRow.name, selectedRow.milesStoneDate);
		return milesStone;
	}
	
	function addOrEditProtoMilesStone(mode, protoMilesStone, editOrigName) {
		var protoMilesStoneStr = JSON.stringify(protoMilesStone); 
		var postData = new FormData();
		postData.append("jsonString", protoMilesStoneStr);
		
		var actionUrl = basePath + "/protocol/protocolMilesStoneAction!addProtoMilesStone.action";
		if(mode == "edit") {
			actionUrl = basePath + "/protocol/protocolMilesStoneAction!editProtoMilesStone.action";
			postData.append("editOrigName", editOrigName);
		}
		
		$table = $("#protoMilesStoneDTTable");
		$.ajax({
			type : "POST",
			url : actionUrl,
			data : postData,
			cache : false,
			processData : false,
			contentType : false,
			success : function(data) {
				$table.idtApi('getTableApi').rows().deselect();
				$table.idtApi('getTableApi').ajax.reload();
			}
		});
	}
	
	function removeProtoMilesStone(protoMilesStoneStr, $table){
		console.log("remove protoMilesStoneStr: "+protoMilesStoneStr);
		var postData = new FormData();
		postData.append("jsonString", protoMilesStoneStr);
		var actionUrl = basePath + "/protocol/protocolMilesStoneAction!deleteProtoMilesStone.action";
		var result = "";
		$.ajax({
			type : "POST",
			url : actionUrl,
			data : postData,
			async : false,
			cache : false,
			processData : false,
			contentType : false,
			success : function(data) {
				if(data == "success"){
					//remove the selected rows in the data table
					var selectedRows = $table.idtApi('getSelectedOptions');
					selectedRows.length = 0;
					$table.idtApi('getTableApi').rows('.selected').remove().draw(false);
				}
				result = data;
			}
		});
		return result;
	}
	
	function clearProtoMilesStoneEntry() {
		 $("#protoMileStoneId").val("");
		 $("#protoMilesStoneName").val("");
		 $("#protoMilesStoneDate").val("");
		 $("#protoMilesStoneErrMsgContainer").html();
	}
</script>

