<%@ taglib uri="/struts-tags" prefix="s" %>
<%@ taglib uri="/WEB-INF/datatables.tld" prefix="idt" %>


<jsp:include page="/protocol/templates/protoAllProcedures.jsp" />
<div id="addProtoProceduresReminder" style="height: 50px; width: 858px;">
	<span class="reminder-message" style="float: left"> <b>Please click "Add Protocol Procedure" button to add to the Procedures Table.</b>
	</span>
	<div style="padding-right: 100px; float:right;">
		<input type="button" id="openProtoProcedureDlg" value="Add Protocol Procedure" onclick="javascript:openProtoProcedureDialog('add', null);" />
	</div>
</div>

<div class="idtTableContainer brics" id="protoProcDTTableDiv" style="width: 858px;">
	<table id="protoProcDTTable" class="table table-striped table-bordered" width="100%">
	</table>   
</div>

<div id="protoProcedureDlg" title="Add Protocol Procedure" style="display: none;">
	<div id="protoProcErrMsgContainer"></div>
	<s:hidden name="protoProceduresStr" id="protoProceduresStr" />

	<div id="allProcedureFields"></div>
	
	<hr/>
	<div style="margin: 10px 8px; float:left;">
		<span class="reminder-message" style="float: left"> <b>Not found the procedure? Please click "Create Procedure" button to create a procedure and add to the table.</b>
		</span>
		<input type="button" id="openCreateProtoProcedureDlg" value="Create Procedure"  style="float:left; margin: 10px 0 0 30px;"/>
	</div>	
	<div id="newPprotoProcedureDiv" style="display: none;">	
		<s:hidden name="protoProcedureId" id="protoProcedureId" />	
		<div class="formrow_1">
			<label for="protoProcedureType" class="requiredInput protocolDlgInputlabel">
				<s:text name="protocol.add.procedure.procedureType"/>
			</label>
			<div class="protocolDlgInput">
				<s:select name="protoProcedureType" id="protoProcedureType" list="procedureTypeList" listKey="id" listValue="name"
						headerKey="" headerValue="- Select One -" style="width: 600px;"/>
			</div>
		</div>
		<div class="formrow_1">
			<label for="protoProcedureName" class="requiredInput protocolDlgInputlabel">
				<s:text name="protocol.add.procedure.name"/>
			</label>
			<div class="protocolDlgInput">
				<s:textfield name="protoProcedureName" id="protoProcedureName" maxlength="400" style="width: 600px;"/>
			</div>
		</div>
	</div>
</div>


<script type="text/javascript">

	function Procedure (procedureId, procedureTypeName, procedureName, isNew){
		this.id = procedureId;
		this.procedureTypeName = procedureTypeName;
		this.name = procedureName;
		this.isNew = isNew;
	}

	
	function renderProcedureTemplate() {
		
		var allProcedureTemplate = Handlebars.compile($("#allProcedureTemplate").html());		
		var allProcedureArray = jQuery.parseJSON($("#protoProceduresStr").val()); 
		var allProcMarkup = allProcedureTemplate({"allProcedureArray": allProcedureArray});
		$("#allProcedureFields").html(allProcMarkup);
	}
	
	var basePath = "<s:property value='#webRoot'/>";
	$(document).ready(function() {
		renderProcedureTemplate();
		
		$("#protoProcDTTable").idtTable({
			idtUrl: basePath + "/protocol/protocolProcedureAction!getProtoProcedureDTList.action",
			idtData: {
				primaryKey: 'id'
			},
			dom: 'Bfrtip',
			select: "multi",
			columns: [
	            {
	                name: 'procedureId',
	                title: 'Procedure Id',
	                parameter: 'procedureId',
	                data: 'procedureId',
	                visible: false
	            },
	            {
	                name: 'procedureName',
	                title: 'Procedure Name',
	                parameter: 'procedureName',
	                data: 'procedureName'
	            },
	            {
	                name: 'procedureTypeName',
	                title: 'Procedure Type',
	                parameter: 'procedureTypeName',
	                data: 'procedureTypeName'
	            },
	            {
	                name: 'isNew',
	                parameter: 'isNew',
	                data: 'isNew',
	                visible: false
	            }
	        ],
	        buttons: [
	        	{
	        		extend : "delete",
	   				text: "Delete",
	   				className: "DeleteProcBtn",
	   				enabled: false,
	     	    	action: function(e, dt, node, config) {
    	    			
	        	   		var selectedRows = dt.rows('.selected').data().to$();
						var protoProcArr = [];
						// Convert the array of IDs to a comma delimited string list 
						for (var idx = 0; idx < selectedRows.length; idx++) {
							var protoProcJson = getSelectedProcedureJson(selectedRows[idx]);
							protoProcArr.push(protoProcJson);
						}

	        	   		var $table = $('#protoProcDTTable');
	        	   		var dlgId = $.ibisMessaging(
	        	   				"dialog", 
	        	   				"warning", 
	        	   				"Are you sure you wish to delete the selected Procedure(s)?",
	        	   				{
	        	   					id: 'deleteRows',
	        	   					container: 'body',
	        	   					buttons: [{
	        	   						id: "yesBtnA",
	        	   						text: "Delete", 
	        	   						click: _.debounce(function() {
	        	   							$(this).siblings().find("#yesBtnA").prop("disabled", true);

	        	   							removeProtoProcedures(JSON.stringify(protoProcArr), $table);
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
	   				className: "editProcBtn",
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
								openProtoProcedureDialog('edit', selectedRow);
							}
	   				}
	   			}
	        ]
		});
		
	}); //end document.ready
	
	function populateProtoProcForm(selectedRow){
		 if(selectedRow != null){
			 if(!selectedRow.isNew) {
			 	var procedureId = selectedRow.procedureId;
			 	$('input#procedure_'+procedureId).prop("checked", true);
			 } else {
				showCreateProcedureDiv();
				$("#protoProcedureType").find(":selected").text(selectedRow.procedureTypeName);
				$("#protoProcedureName").val(selectedRow.procedureName);
			 }
		 }
	}
	 function openProtoProcedureDialog(mode, selectedRow) {
		//console.log("mode: "+mode);	
		 if(mode == "edit") {
			 populateProtoProcForm(selectedRow);
		 }

			$("#protoProcedureDlg").dialog({
				autoOpen : false,
				height : 400,
				width : 900,
				position : {
					my : "center",
					at : "center",
					of : window
				},
				buttons : [ 
	        	{
					id : "submitProtoProcBtn",
					text : "Add to Table",
					click : function() {
						if (mode == "add") {
							if (validateProtoProcedure()) {
								var protoProcedureArr = getProtoProcedureFromDlg();
								//console.log("dlg protoProcedureArr: "+JSON.stringify(protoProcedureArr));
								addProtoProcedure(protoProcedureArr);
								$(this).dialog("close");
							}
						} else if (mode == "edit") {
							if (validateProtoProcedure()) {
								var addedFromDlg = getProtoProcedureFromDlg();
								editProtoProcedure(selectedRow, addedFromDlg);
								$(this).dialog("close");
							}
						}
						
					}
				}, {
					id : "cancelProtoProcBtn",
					text : "Clear List",
					click : function() {
						clearProtoProcedureEntry();
						//$("#intervalProcedurePointDlg").dialog("close");
					}
				} ],

				close : function() {
					clearProtoProcedureEntry();
					$(this).dialog('destroy');
				}
			});
			$("#protoProcedureDlg").dialog("open");
	}	
	
	function validateProtoProcedure() {
		var valid = false;
		var errMsg = "";

		var inputProcTypeName = $.trim($("#protoProcedureType").find(":selected").val());
		var inputProcTypeText = $.trim($("#protoProcedureType").find(":selected").text());
		var inputProcName = $.trim($("#protoProcedureName").val());
		var isExist = false;
		$("input[type=checkbox]").each(function() {
        	if ($(this).val() == inputProcName) {
        		isExist = true; //console.log("isExist: "+isExist);
        	}
    	});
		var $table = $('#protoProcDTTable');
		var tableRows = $table.idtApi('getRows');
		
		$.each(tableRows, function(index, value) { //console.log("datatable value:"+JSON.stringify(value));
			if(value.procedureName == inputProcName && value.procedureTypeName == inputProcTypeText) {
				isExist = true;
			};
		});
		
		if ($(".protoProcedureChkBox:checkbox:checked").length > 0 ) {
			valid = true;	
			errMsg = "";
		} 
		if ($(".protoProcedureChkBox:checkbox:checked").length == 0 && $("#newPprotoProcedureDiv").is(":hidden")) {
			errMsg +="<li>Procedure is required. Please check one of the procedures.</li>";	
		}	
		if($("#newPprotoProcedureDiv").is(":visible") 
				&& inputProcTypeName.length > 0 && inputProcName.length > 0 && !isExist){			
				valid = true;	
				errMsg = "";
		} else if ($("#newPprotoProcedureDiv").is(":visible")) {
			
			if(inputProcTypeName.length == 0 ){
				errMsg +="<li>Procedure type is required. Please select one of the procedure type.</li>";	
			}
			if(inputProcName.length == 0 ){
				errMsg +="<li>Procedure name is required. Please input the procedure name.</li>";	
			}
			if(isExist){
				errMsg +="<li>Procedure name is exist. Please input another name.</li>";	
			}
		}

		
		if(errMsg.length > 0){
			errMsg = "<div style='color:red; text-align: left;'>Error: <ul>"+errMsg+"</ul><hr><br></div>";
			valid = false;
		}

		$("#protoProcErrMsgContainer").html(errMsg);

		return valid;
	}
	
	function getProtoProcedureFromDlg() {
		var procedureArr = new Array();
		$("#allProcedureFields").find(".protoProcedureChkBox:checkbox:checked").each(function() {
			var procedureId = $(this).attr("id").split("_")[1];
			var procedureName = $(this).val();
			var procedureTypeName = $(this).parent().closest('div').attr("id");
			var isNew = false;
			var procedure = new Procedure(procedureId, procedureTypeName, procedureName, isNew);
			procedureArr.push(procedure);
		});
		if ($("#newPprotoProcedureDiv").is(":visible")) {
			var inputProcId = $.trim($("#protoProcedureId").val());
			var inputProcTypeName = $("#protoProcedureType").find(":selected").text();
			var inputProcName = $.trim($("#protoProcedureName").val());
			var isNew = true;
			var inputProcedure = new Procedure(inputProcId, inputProcTypeName, inputProcName, isNew);
			procedureArr.push(inputProcedure);
		}
		
		return procedureArr;
	}
	
	function getSelectedProcedureJson(selectedRow){
		var procedure = new Procedure(selectedRow.procedureId, selectedRow.procedureTypeName, selectedRow.procedureName, selectedRow.isNew);
		return procedure;
	}
	
	function addProtoProcedure(protoProcedureArr) {
		
		var protoProcArrStr = JSON.stringify(protoProcedureArr); //console.log("Add protoProcArrStr: "+protoProcArrStr);
		var postData = new FormData();
		postData.append("jsonString", protoProcArrStr);
		
		$table = $("#protoProcDTTable");
		$.ajax({
			type : "POST",
			url : basePath + "/protocol/protocolProcedureAction!addProtoProcedure.action",
			data : postData,
			cache : false,
			processData : false,
			contentType : false,
			success : function(data) {
				if(data == "success"){
					$table.idtApi('getTableApi').ajax.reload();
				}
			}
		});
	}
	function editProtoProcedure(selectedRow, addedFromDlg){
		var protoProcJson = getSelectedProcedureJson(selectedRow);//console.log("edit protoProcJson: "+JSON.stringify(protoProcJson));
		var protoProcJsonArr = [];
		protoProcJsonArr.push(protoProcJson);
		var $table = $('#protoProcDTTable');
		var removeResult = removeProtoProcedures(JSON.stringify(protoProcJsonArr), $table);
		//console.log("edit remove result: "+removeResult);
		//console.log("edit addedFromDlg: "+JSON.stringify(addedFromDlg));
		if (removeResult == "success") {
			addProtoProcedure(addedFromDlg);
		}
	}
	function removeProtoProcedures(protoProcStr, $table){
		//console.log("remove protoProcStr: "+protoProcStr);
		var postData = new FormData();
		postData.append("jsonString", protoProcStr);
		var actionUrl = basePath + "/protocol/protocolProcedureAction!deleteProtoProcedure.action";
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
				//console.log("result: "+result);
				
			}
		});
		return result;
	}
	
	function clearProtoProcedureEntry() {
		$('input:checkbox').prop('checked', false);
		$("#protoProcedureId").val("");
		$("#protoProcedureType").val("");
		$("#protoProcedureName").val("");		
		$("#protoProcErrMsgContainer").html("");
		$("#newPprotoProcedureDiv").hide();
	}
	
	$("#openCreateProtoProcedureDlg").click(function(){

		$("#newPprotoProcedureDiv").show();
	});
	
	function clearNewProtoProcedureEntry() {
		$("#protoProcedureId").val("");
		$("#protoProcedureType").val("");
		$("#protoProcedureName").val("");
		$("#newProtoProcErrMsgContainer").html("");
	}
	
	function createNewProtoProcedure(newProtoProcArr) {
		var newProtoProcArrStr = JSON.stringify(newProtoProcArr);
		//console.log("create newProtoProcStr: "+newProtoProcArrStr);
		var postData = new FormData();
		postData.append("jsonString", newProtoProcArrStr);
		var actionUrl = basePath + "/protocol/protocolProcedureAction!createProtoProcedure.action";

		$.ajax({
			type : "POST",
			url : actionUrl,
			data : postData,
			async : false,
			cache : false,
			processData : false,
			contentType : false,
			success : function(data) {
				$("#protoProcedure").html(data);
				addProtoProcedure(newProtoProcArr);			
			}
		});
	}
</script>

