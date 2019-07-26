<%@ taglib uri="/struts-tags" prefix="s" %>
<%@ taglib uri="/WEB-INF/datatables.tld" prefix="idt" %>

<div id="addProtoClinicLocsReminder" style="height: 50px; width: 858px;">
	<span class="reminder-message" style="float: left"> <b>Please click "Add Protocol Clinical Location" button to add to the Clinical Location Table.</b>
	</span>
	<div style="padding-right: 100px; float:right;">
		<input type="button" id="openProtoClinicLocDlg" value="Add Protocol Clinical Location" onclick="javascript:openProtoClinicLocDialog('add', null);" />
	</div>
</div>

<div class="idtTableContainer brics" id="protoClinicLocDTTableDiv" style="width: 858px;">
	<table id="protoClinicLocDTTable" class="table table-striped table-bordered" width="100%">
	</table>   
</div>

<div id="protoClinicLocationDlg" title="Add Protocol Clinical Location" style="display: none;">
	<div id="protoClinicLocErrMsgContainer"></div>
	<s:hidden name="protoClinicLocId" id="protoClinicLocId" />
	<s:hidden name="protoClinicLocAddressId" id="protoClinicLocAddressId" />

	<div>
		<div class="formrow_1">
				<label for="protoClinicLocName" class="requiredInput protocolDlgInputlabel">
				<s:text name="protocol.add.clinicalLocation.name"/>
			</label>
			<div class="protocolDlgInput">
			<s:textfield name="protoClinicLocName" id="protoClinicLocName" maxlength="200" style="width: 600px;"/>
			</div>
		</div>
		<div class="formrow_1">
			<label for="protoClinicLocAddressOne" class="requiredInput protocolDlgInputlabel">
				<s:text name="protocol.add.clinicalLocation.address.addressOne"/>
			</label>
			<div class="protocolDlgInput">
			<s:textfield name="protoClinicLocAddressOne" id="protoClinicLocAddressOne" maxlength="400" style="width: 600px;"/>
			</div>
		</div>
		<div class="formrow_1">
			<label for="protoClinicLocAddressTwo" class="protocolAddress2label">
				<s:text name="protocol.add.clinicalLocation.address.addressTwo"/>
			</label>
			<div class="protocolDlgInput">
			<s:textfield name="protoClinicLocAddressTwo" id="protoClinicLocAddressTwo" maxlength="400" style="width: 600px;"/>
			</div>
		</div>
		<div class="formrow_1">
			<label for="protoClinicLocCity" class="requiredInput protocolDlgInputlabel">
				<s:text name="protocol.add.clinicalLocation.address.city"/>
			</label>
			<div class="protocolDlgInput">
			<s:textfield name="protoClinicLocCity" id="protoClinicLocCity" maxlength="200" style="width: 600px;"/>
			</div>
		</div>
		<div class="formrow_1">
			<label for="protoClinicLocState" class="requiredInput protocolDlgInputlabel">
				<s:text name="protocol.add.clinicalLocation.address.state"/>
			</label>
			<div class="protocolDlgInput">
			<s:select name="protoClinicLocState" id="protoClinicLocState" list="#session.__SiteHome_states" listKey="id" listValue="longName" 
				style="width: 600px;"/>
			</div>
		</div>
		<div class="formrow_1">
			<label for="protoClinicLocCountry" class="requiredInput protocolDlgInputlabel">
				<s:text name="protocol.add.clinicalLocation.address.country"/>
			</label>
			<div class="protocolDlgInput">
			<s:select name="protoClinicLocCountry" id="protoClinicLocCountry" list="#session.__SiteHome_countries" listKey="id" listValue="shortName" 
				style="width: 600px;"/>
			</div>
		</div>
		<div class="formrow_1">
			<label for="protoClinicLocZipCode" class="requiredInput protocolDlgInputlabel">
				<s:text name="protocol.add.clinicalLocation.address.zipCode"/>
			</label>
			<div class="protocolDlgInput">
			<s:textfield name="protoClinicLocZipCode" id="protoClinicLocZipCode" maxlength="200" style="width: 600px;"/>
			</div>
		</div>
	</div>
</div>

<script type="text/javascript">

	function ClinicalLocation (id, name, address, status){
		this.id = id;
		this.name = name;
		this.address = address;
		this.status = status;
	}
	
	function Address (id, addressOne, addressTwo, city, state, country, zipCode) {
		this.id = id;
		this.addressOne = addressOne;
		this.addressTwo = addressTwo;
		this.city = city;
		this.state = state;
		this.country = country;
		this.zipCode = zipCode;
	}

	var basePath = "<s:property value='#webRoot'/>";
	$(document).ready(function() {

		$("#protoClinicLocDTTable").idtTable({
			idtUrl: basePath + "/protocol/protocolClinicalLocationAction!getProtoClinicalLocationDTList.action",
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
	                name: 'address',
	                title: 'Address',
	                parameter: 'address',
	                data: 'address'
	            },
	            {
	                name: 'addressId',
	                parameter: 'addressId',
	                data: 'addressId',
	                visible: false
	            },
	            {
	                name: 'addressOne',
	                title: 'Address One',
	                parameter: 'addressOne',
	                data: 'addressOne',
	                visible: false
	            },
	            {
	                name: 'addressTwo',
	                title: 'Address Two',
	                parameter: 'addressTwo',
	                data: 'addressTwo',
	                visible: false
	            },
	            {
	                name: 'city',
	                title: 'City',
	                parameter: 'city',
	                data: 'city',
	                visible: false
	            },
	            {
	                name: 'state',
	                title: 'State',
	                parameter: 'state',
	                data: 'state',
	                visible: false
	            },
	            {
	                name: 'country',
	                title: 'Country',
	                parameter: 'country',
	                data: 'country',
	                visible: false
	            },
	            {
	                name: 'zipCode',
	                title: 'Zip Code',
	                parameter: 'zipCode',
	                data: 'zipCode',
	                visible: false
	            },
	            {
	                name: 'status',
	                parameter: 'status',
	                data: 'status',
	                visible: false
	            }
	        ],
	        buttons: [
	        	{
	        		extend : "delete",
	   				text: "Delete",
	   				className: "DeleteLocBtn",
	   				enabled: false,
	     	    	action: function(e, dt, node, config) {
    	    			
	        	   		var selectedRows = dt.rows('.selected').data().to$();
						var protoClincLocArr = [];
						// Convert the array of IDs to a comma delimited string list 
						for (var idx = 0; idx < selectedRows.length; idx++) {
							var protoClinicLocJson = getSelectedClinicLocJson(selectedRows[idx]);
							protoClincLocArr.push(protoClinicLocJson);
						}

	        	   		var $table = $('#protoClinicLocDTTable');
	        	   		var dlgId = $.ibisMessaging(
	        	   				"dialog", 
	        	   				"warning", 
	        	   				"Are you sure you wish to delete the selected Location(s)?",
	        	   				{
	        	   					id: 'deleteRows',
	        	   					container: 'body',
	        	   					buttons: [{
	        	   						id: "yesBtnA",
	        	   						text: "Delete", 
	        	   						click: _.debounce(function() {
	        	   							$(this).siblings().find("#yesBtnA").prop("disabled", true);

	        	   							removeProtoClinicLocs(JSON.stringify(protoClincLocArr), $table);
	        	   							$.ibisMessaging("close", {id: dlgId});

	        	   					
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
								openProtoClinicLocDialog('edit', selectedRow);
							}
	   				}
	   			}
	        ]
		});
		
	}); //end document.ready
	
	function populateProtoClinicLocForm(selectedRow){//console.log("populateProtoClinicLocForm selectedRow: "+JSON.stringify(selectedRow));
		 var selectedRowJsonStr = "";
		 if(selectedRow != null){
			 var id = selectedRow.id;
			 var name = selectedRow.name;
			 var addressId = selectedRow.addressId;
			 var addressOne = selectedRow.addressOne; 
			 var addressTwo = selectedRow.addressTwo;
			 var city = selectedRow.city;
			 var state = selectedRow.state;
			 var country = selectedRow.country;
			 var zipCode = selectedRow.zipCode;

			 $("#protoClinicLocId").val(id);
			 $("#protoClinicLocName").val(name);
			 $("#protoClinicLocAddressId").val(addressId);
			 $("#protoClinicLocAddressOne").val(addressOne);
			 $("#protoClinicLocAddressTwo").val(addressTwo);
			 $("#protoClinicLocCity").val(city);
			 $('#protoClinicLocState').val(state);
			 $('#protoClinicLocCountry').val(country);
			 $("#protoClinicLocZipCode").val(zipCode);
			 
			 var selectedRowJson = getSelectedClinicLocJson(selectedRow);
			 selectedRowJsonStr = JSON.stringify(selectedRowJson);
		 }
		 return selectedRowJsonStr;
	}
	 function openProtoClinicLocDialog(mode, selectedRow) {
		 var editOrigJsonStr = "";	
		 if(mode == "edit") {
			 editOrigJsonStr = populateProtoClinicLocForm(selectedRow);
		 }

			$("#protoClinicLocationDlg").dialog({
				autoOpen : false,
				height : 400,
				width : 900,
				position : {
					my : "center",
					at : "center",
					of : window
				},
				buttons : [ {
					id : "submitProtoClinicLocBtn",
					text : "Submit",
					click : function() {

						if (validateProtoClinicLoc()) {
							var protoClinicLoc = getProtoClinicLocFromDlg(mode);
							addOrEditProtoClinicLoc(mode, protoClinicLoc, editOrigJsonStr);
							$(this).dialog("close");
						}
					}
				}, {
					id : "cancelResMgmtBtn",
					text : "Clear List",
					click : function() {
						clearProtoClinicLocEntry();
						//$("#intervalLocationPointDlg").dialog("close");
					}
				} ],

				close : function() {
					clearProtoClinicLocEntry();
					$(this).dialog('destroy');
				}
			});
			$("#protoClinicLocationDlg").dialog("open");
	}	
	
	function validateProtoClinicLoc() {
		var valid = false;
		var errMsg = "";
		
		var inputLocName = $.trim($("#protoClinicLocName").val());
		var inputLocAddressOne = $.trim($("#protoClinicLocAddressOne").val());
		var inputLocAddressTwo = $.trim($("#protoClinicLocAddressTwo").val());
		var inputLocCity = $.trim($("#protoClinicLocCity").val());
		var inputLocState = $.trim($("#protoClinicLocState").val());
		var inputLocCountry = $.trim($("#protoClinicLocCountry").val());
		var inputLocZipCode = $.trim($("#protoClinicLocZipCode").val());


		if (inputLocName.length > 0 && inputLocAddressOne.length > 0 && inputLocCity.length > 0
				&& inputLocState.length > 0 && inputLocCountry.length > 0 && inputLocZipCode.length > 0){			
			valid = true;	
			errMsg = "";
		}
		if (inputLocName.length == 0) {
			errMsg +="<li>Clinical location name is required. Please input the name of clinical location.</li>";			
		}
		if (inputLocAddressOne.length == 0) {
			errMsg +="<li>Clinical location address is required. Please input the address of clinical location.</li>";			
		}
		if (inputLocCity.length == 0) {
			errMsg +="<li>Clinical location city is required. Please input the city of clinical location.</li>";			
		}
		if (inputLocState.length == 0) {
			errMsg +="<li>Clinical location state is required. Please select a state.</li>";			
		}
		if (inputLocCountry.length == 0) {
			errMsg +="<li>Clinical location country is required. Please select a country.</li>";			
		}
		if (inputLocZipCode.length == 0) {
			errMsg +="<li>Clinical location zip code is required. Please input the zip code of clinical location.</li>";			
		}
		
		if(errMsg.length > 0){
			errMsg = "<div style='color:red; text-align: left;'>Error: <ul>"+errMsg+"</ul><hr><br></div>";
		}

		$("#protoClinicLocErrMsgContainer").html(errMsg);
		//console.log("validate result: "+valid);
		return valid;
	}
	
	function getProtoClinicLocFromDlg(mode) {
		var id = $("#protoClinicLocId").val();
		var name = $.trim($("#protoClinicLocName").val());
		var addressId = $.trim($("#protoClinicLocAddressId").val());
		var addressOne = $.trim($("#protoClinicLocAddressOne").val());
		var addressTwo = $.trim($("#protoClinicLocAddressTwo").val());
		var city = $.trim($("#protoClinicLocCity").val());
		var state = $.trim($('#protoClinicLocState').find(":selected").val());
		var country = $.trim($('#protoClinicLocCountry').find(":selected").val());
		var zipCode = $.trim($("#protoClinicLocZipCode").val());
		var status = "";
		if (mode == "add"){
			status = "added";
		} 
		if (mode == "edit"){
			status = "edited";
		}
		
		var address = new Address(addressId, addressOne, addressTwo, city, state, country, zipCode);
		var clinicalLocation = new ClinicalLocation(id, name, address, status);
		
		return clinicalLocation;
	}
	
	function getSelectedClinicLocJson(selectedRow){
		var address = new Address(selectedRow.addressId, selectedRow.addressOne, selectedRow.addressTwo, 
							selectedRow.city, selectedRow.state, selectedRow.country, selectedRow.zipCode)
		var clinicalLocation = new ClinicalLocation(selectedRow.id, selectedRow.name, address, selectedRow.status);
		return clinicalLocation;
	}
	
	function addOrEditProtoClinicLoc(mode, protoClinicLoc, editOrigJsonStr) {
		var actionUrl = basePath + "/protocol/protocolClinicalLocationAction!addProtoClinicalLocation.action";
		if(mode == "edit") {
			actionUrl = basePath + "/protocol/protocolClinicalLocationAction!editProtoClinicalLocation.action";
		}
		
		var protoClinicLocStr = JSON.stringify(protoClinicLoc); //console.log("Add protoClinicLocStr: "+protoClinicLocStr);
		var postData = new FormData();
		postData.append("jsonString", protoClinicLocStr);
		if(mode == "edit") {
			postData.append("editOrigJsonStr", editOrigJsonStr);
		}		
		
		$table = $("#protoClinicLocDTTable");
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
	
	function removeProtoClinicLocs(protoClinicLocStr, $table){
		//console.log("remove protoClinicLocStr: "+protoClinicLocStr);
		var postData = new FormData();
		postData.append("jsonString", protoClinicLocStr);
		var actionUrl = basePath + "/protocol/protocolClinicalLocationAction!deleteProtoClinicalLocation.action";
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
	
	function clearProtoClinicLocEntry() {
		 $("#protoClinicLocId").val("");
		 $("#protoClinicLocName").val("");
		 $("#protoClinicLocAddressId").val("");
		 $("#protoClinicLocAddressOne").val("");
		 $("#protoClinicLocAddressTwo").val("");
		 $("#protoClinicLocCity").val("");
		 $('#protoClinicLocState').val("1");
		 $('#protoClinicLocCountry').val("1");
		 $("#protoClinicLocZipCode").val("");
		 $("#protoClinicLocErrMsgContainer").html();
	}
</script>

