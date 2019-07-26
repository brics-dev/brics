<%@ taglib uri="/struts-tags" prefix="s" %>
<%@ taglib uri="/WEB-INF/datatables.tld" prefix="idt" %>

<div id="addProtoPointOfContactsReminder" style="height: 50px; width: 858px;">
	<span class="reminder-message" style="float: left"> <b>Please click "Add Protocol Point Of Contact" button to add to the Point of Contact Table.</b>
	</span>
	<div style="padding-right: 100px; float:right;">
		<input type="button" id="openProtoPOCDlg" value="Add Protocol Point of Contact" onclick="javascript:openProtoPOCDialog('add', null);" />
	</div>
</div>

<div class="idtTableContainer brics" id="protoPOCDTTableDiv" style="width: 858px;">
	<table id="protoPOCDTTable" class="table table-striped table-bordered" width="100%">
	</table>   
</div>

<div id="protoPointOfContactDlg" title="Add Protocol Point Of Contact" style="display: none;">
	<div id="protoPOCErrMsgContainer"></div>
	<%-- <s:hidden name="protoProceduresStr" id="protoProceduresStr" /> --%>
	<s:hidden name="protoPOCId" id="protoPOCId" />
	<s:hidden name="protoPOCAddressId" id="protoPOCAddressId" />

	<div>
		<div class="formrow_1">
			<label for="protoPOCFirstName" class="requiredInput protocolDlgInputlabel">
				<s:text name="protocol.add.pointofcontact.firstName"/>
			</label>
			<div class="protocolDlgInput">
				<s:textfield name="protoPOCFirstName" id="protoPOCFirstName" maxlength="200" style="width: 600px;"/>
			</div>
		</div>
		<div class="formrow_1">
			<label for="protoPOCMidName" class="protocolDlgInputlabel">
				<s:text name="protocol.add.pointofcontact.middleName"/>
			</label>
			<div class="protocolDlgInput">
				<s:textfield name="protoPOCMiddleName" id="protoPOCMiddleName" maxlength="400" style="width: 600px;"/>
			</div>
		</div>
		<div class="formrow_1">
			<label for="protoPOCLastName" class="requiredInput protocolDlgInputlabel">
				<s:text name="protocol.add.pointofcontact.lastName"/>
			</label>
			<div class="protocolDlgInput">
				<s:textfield name="protoPOCLastName" id="protoPOCLastName" maxlength="200" style="width: 600px;"/>
			</div>
		</div>
		<div class="formrow_1">
			<label for="protoPOCPosition" class="requiredInput protocolDlgInputlabel">
				<s:text name="protocol.add.pointofcontact.position"/>
			</label>
			<div class="protocolDlgInput">
				<s:textfield name="protoPOCPosition" id="protoPOCPosition" maxlength="200" style="width: 600px;"/>
			</div>
		</div>
		<div class="formrow_1">
			<label for="protoPOCPhone" class="requiredInput protocolDlgInputlabel">
				<s:text name="protocol.add.pointofcontact.phone"/>
			</label>
			<div class="protocolDlgInput">
				<s:textfield name="protoPOCPhone" id="protoPOCPhone" maxlength="200" style="width: 600px;"/>
			</div>
		</div>
		<div class="formrow_1">
			<label for="protoPOCEmail" class="requiredInput protocolDlgInputlabel">
				<s:text name="protocol.add.pointofcontact.email"/>
			</label>
			<div class="protocolDlgInput">
				<s:textfield name="protoPOCEmail" id="protoPOCEmail" maxlength="200" style="width: 600px;"/>
			</div>
		</div>
		<div class="formrow_1">
			<label for="protoPOCAddressOne" class="requiredInput protocolDlgInputlabel">
				<s:text name="protocol.add.pointofcontact.address.addressOne"/>
			</label>
			<div class="protocolDlgInput">
			<s:textfield name="protoPOCAddressOne" id="protoPOCAddressOne" maxlength="400" style="width: 600px;"/>
			</div>
		</div>
		<div class="formrow_1">
			<label for="protoPOCAddressTwo" class="protocolAddress2label">
				<s:text name="protocol.add.pointofcontact.address.addressTwo"/>
			</label>
			<div class="protocolDlgInput">
			<s:textfield name="protoPOCAddressTwo" id="protoPOCAddressTwo" maxlength="400" style="width: 600px;"/>
			</div>
		</div>
		<div class="formrow_1">
			<label for="protoPOCCity" class="requiredInput protocolDlgInputlabel">
				<s:text name="protocol.add.pointofcontact.address.city"/>
			</label>
			<div class="protocolDlgInput">
			<s:textfield name="protoPOCCity" id="protoPOCCity" maxlength="200" style="width: 600px;"/>
			</div>
		</div>
		<div class="formrow_1">
			<label for="protoPOCState" class="requiredInput protocolDlgInputlabel">
				<s:text name="protocol.add.pointofcontact.address.state"/>
			</label>
			<div class="protocolDlgInput">
			<s:select name="protoPOCState" id="protoPOCState" list="#session.__SiteHome_states" listKey="id" listValue="longName"
				style="width: 600px;"/>
			</div>
		</div>
		<div class="formrow_1">
			<label for="protoPOCCountry" class="requiredInput protocolDlgInputlabel">
				<s:text name="protocol.add.pointofcontact.address.country"/>
			</label>
			<div class="protocolDlgInput">
			<s:select name="protoPOCCountry" id="protoPOCCountry" list="#session.__SiteHome_countries" listKey="id" listValue="shortName"
				style="width: 600px; "/>
			</div>
		</div>
		<div class="formrow_1">
			<label for="protoPOCZipCode" class="requiredInput protocolDlgInputlabel">
				<s:text name="protocol.add.pointofcontact.address.zipCode"/>
			</label>
			<div class="protocolDlgInput">
			<s:textfield name="protoPOCZipCode" id="protoPOCZipCode" maxlength="200" style="width: 600px;"/>
			</div>
		</div>
		
	</div>
</div>

<script type="text/javascript">

	function PointOfContact (pocId, pocFirstName, pocMidName, pocLastName, position, pocPhone, pocEmail, address, status){
		this.id = pocId;
		this.firstName = pocFirstName;
		this.middleName = pocMidName;
		this.lastName = pocLastName;
		this.position = position
		this.phone = pocPhone;
		this.email = pocEmail;
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

		$("#protoPOCDTTable").idtTable({
			idtUrl: basePath + "/protocol/protocolPointOfContactAction!getProtoPointOfContactDTList.action",
			idtData: {
				primaryKey: 'id'
			},
			dom: 'Bfrtip',
			select: "multi",
			columns: [
	            {
	                name: 'pocId',
	                parameter: 'pocId',
	                data: 'pocId',
	                visible: false
	            },
	            {
	                name: 'pocFirstName',
	                title: 'First Name',
	                parameter: 'pocFirstName',
	                data: 'pocFirstName',
	                visible: false
	            },
	            {
	                name: 'pocMiddleName',
	                title: 'Middle Name',
	                parameter: 'pocMiddleName',
	                data: 'pocMiddleName',
	                visible: false
	            },
	            {
	                name: 'pocLastName',
	                title: 'Last Name',
	                parameter: 'pocLastName',
	                data: 'pocLastName',
	                visible: false
	            },
	            {
	                name: 'pocFullName',
	                title: 'Full Name',
	                parameter: 'pocFullName',
	                data: 'pocFullName'
	            },
	            {
	                name: 'position',
	                title: 'Position',
	                parameter: 'position',
	                data: 'position'
	            },
	            {
	                name: 'pocPhone',
	                title: 'Phone',
	                parameter: 'pocPhone',
	                data: 'pocPhone'
	            },
	            {
	                name: 'pocEmail',
	                title: 'Email',
	                parameter: 'pocEmail',
	                data: 'pocEmail'
	            },
	            {
	                name: 'addressId',
	                parameter: 'addressId',
	                data: 'addressId',
	                visible: false
	            },
	            {
	                name: 'address',
	                title: 'Address',
	                parameter: 'address',
	                data: 'address'
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
	   				className: "DeletePOCBtn",
	   				enabled: false,
	     	    	action: function(e, dt, node, config) {
    	    			
	        	   		var selectedRows = dt.rows('.selected').data().to$();
						var protoPOCArr = [];
						// Convert the array of IDs to a comma delimited string list 
						for (var idx = 0; idx < selectedRows.length; idx++) {
							var protoPOCJson = getSelectedPOCJson(selectedRows[idx]);
							protoPOCArr.push(protoPOCJson);
						}

	        	   		var $table = $('#protoPOCDTTable');
	        	   		var dlgId = $.ibisMessaging(
	        	   				"dialog", 
	        	   				"warning", 
	        	   				"Are you sure you wish to delete the selected Point of Contact(s)?",
	        	   				{
	        	   					id: 'deleteRows',
	        	   					container: 'body',
	        	   					buttons: [{
	        	   						id: "yesBtnA",
	        	   						text: "Delete", 
	        	   						click: _.debounce(function() {
	        	   							$(this).siblings().find("#yesBtnA").prop("disabled", true);
											
	        	   							removeProtoPOCs(JSON.stringify(protoPOCArr), $table);
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
	   				className: "editPOCBtn",
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
								openProtoPOCDialog('edit', selectedRow);
							}
	   				}
	   			}
	        ]
		});
		
	}); //end document.ready
	
	function populateProtoPOCForm(selectedRow){
		 var selectedRowJsonStr = "";
		 if(selectedRow != null){
			 var pocId = selectedRow.pocId;
			 var pocFirstName = selectedRow.pocFirstName;
			 var pocMiddleName = selectedRow.pocMiddleName;
			 var pocLastName = selectedRow.pocLastName;
			 var position = selectedRow.position;
			 var pocPhone = selectedRow.pocPhone;
			 var pocEmail = selectedRow.pocEmail;
			 var addressId = selectedRow.addressId;
			 var addressOne = selectedRow.addressOne; 
			 var addressTwo = selectedRow.addressTwo;
			 var city = selectedRow.city;
			 var state = selectedRow.state;
			 var country = selectedRow.country;
			 var zipCode = selectedRow.zipCode;
			 
			 $("#protoPOCId").val(pocId);
			 $("#protoPOCFirstName").val(pocFirstName);
			 $("#protoPOCMiddleName").val(pocMiddleName);
			 $("#protoPOCLastName").val(pocLastName);
			 $("#protoPOCPosition").val(position);
			 $("#protoPOCPhone").val(pocPhone);
			 $("#protoPOCEmail").val(pocEmail);
			 
			 $("#protoPOCAddressId").val(addressId)
			 $("#protoPOCAddressOne").val(addressOne);
			 $("#protoPOCAddressTwo").val(addressTwo);
			 $("#protoPOCCity").val(city);
			 $("#protoPOCState").val(state);
			 $("#protoPOCCountry").val(country);
			 $("#protoPOCZipCode").val(zipCode);
			 
			 var selectedRowJson = getSelectedPOCJson(selectedRow);
			 selectedRowJsonStr = JSON.stringify(selectedRowJson);
		 }
		 return selectedRowJsonStr;
	}
	 function openProtoPOCDialog(mode, selectedRow) {
		 var editOrigJsonStr = "";
		 if(mode == "edit") {
			 editOrigJsonStr = populateProtoPOCForm(selectedRow);
		 }

			$("#protoPointOfContactDlg").dialog({
				autoOpen : false,
				height : 400,
				width : 900,
				position : {
					my : "center",
					at : "center",
					of : window
				},
				buttons : [ {
					id : "submitProtoPOCBtn",
					text : "Submit",
					click : function() {

						if (validateProtoPOC()) {	
							var protoPOC = getProtoPOCFromDlg(mode);
							addOrEditProtoPOC(mode, protoPOC, editOrigJsonStr);
							
							clearProtoPOCEntry();
							$(this).dialog('destroy');
						}
						
					}
				}, {
					id : "cancelPOCBtn",
					text : "Clear List",
					click : function() {
						clearProtoPOCEntry();
					}
				} ],

				close : function() {
					clearProtoPOCEntry();
					$(this).dialog('destroy');
				}
			});
			$("#protoPointOfContactDlg").dialog("open");
	}	
	
	function validateProtoPOC() {
		var valid = false;
		var errMsg = "";
		
		var inputPOCFirstName = $.trim($("#protoPOCFirstName").val()); 
		var inputPOCLastName = $.trim($("#protoPOCLastName").val()); 
		var inputPOCPhone = $.trim($("#protoPOCPhone").val()); 
		var inputPosition = $.trim($("#protoPOCPosition").val());
		var inputPOCEmail = $.trim($("#protoPOCEmail").val()); 
		var validPhone = validatePhone(inputPOCPhone); 
		var validEmail = validateEmail(inputPOCEmail); 

		var validPOCAddress = validateProtoPOCAddress();
		if (inputPOCFirstName.length > 0 && inputPOCLastName.length > 0 && inputPosition.length > 0
				 && validPhone && validEmail && validPOCAddress.length == 0){					
			valid = true;	
			errMsg = "";
		}
		if (inputPOCFirstName.length == 0) {
			errMsg +="<li>First name is required. Please input the first name.</li>";			
		}
		if (inputPOCLastName.length == 0) {
			errMsg +="<li>Last name is required. Please input the last name.</li>";			
		}
		if (inputPosition.length == 0 ){
			errMsg +="<li>Position is required. Please input the position.</li>";	
		}
		if (inputPOCPhone.length == 0) {
			errMsg +="<li>Phone is required. Please input the phone.</li>";			
		} else if (!validPhone) {
			errMsg +="<li>Phone number is invalid. Please enter the valid phone number.</li>";	
		}
		if (inputPOCEmail.length == 0) {
			errMsg +="<li>Email is required. Please input the email.</li>";			
		} else if (!validEmail) {
			errMsg +="<li>Email is invalid. Please enter the valid email address.</li>";	
		}		
		if(validPOCAddress.length > 0){
			errMsg += validPOCAddress;
		} 
		if(errMsg.length > 0){
			errMsg = "<div style='color:red; text-align: left;'>Error: <ul>"+errMsg+"</ul><hr><br></div>";
		} else {
			valid = true;
		}

		$("#protoPOCErrMsgContainer").html(errMsg);
		return valid;
	}

	function validatePhone(phoneNumber){
		var phoneNumberPattern = /\(?([0-9]{3})\)?([ .-]?)([0-9]{3})\2([0-9]{4})/;  
		return phoneNumberPattern.test(phoneNumber); 
	}
	
	function validateEmail(email) {
	  	var emailPattern = /^([a-zA-Z0-9_.+-])+\@(([a-zA-Z0-9-])+\.)+([a-zA-Z0-9]{2,4})+$/;
	  	return emailPattern.test(email);
	}
	
	function validateProtoPOCAddress() {
		var errMsg = "";
		var inputAddressOne = $.trim($("#protoPOCAddressOne").val());
		var inputCity = $.trim($("#protoPOCCity").val());
		var inputZipCode = $.trim($("#protoPOCZipCode").val());
		var inputState = $.trim($("#protoPOCState").val());
		var inputCountry = $.trim($("#protoPOCCountry").val());
		
		if (inputAddressOne.length > 0 && inputCity.length > 0
				&& inputState.length > 0 && inputCountry.length > 0 && inputZipCode.length > 0){				
			errMsg = "";
		}
		if (inputAddressOne.length == 0) {
			errMsg +="<li>Point of contact address is required. Please input the address one of point of contact.</li>";			
		}
		if (inputCity.length == 0) {
			errMsg +="<li>Point of contact city is required. Please input the city of point of contact.</li>";			
		}
		if (inputState.length == 0) {
			errMsg +="<li>Point of contact state is required. Please select a state.</li>";			
		}
		if (inputCountry.length == 0) {
			errMsg +="<li>Point of contact country is required. Please select a country.</li>";			
		}
		if (inputZipCode.length == 0) {
			errMsg +="<li>Point of contact zip code is required. Please input the zip code of point of contact.</li>";			
		}
		if(errMsg.length > 0){
			errMsg = "<div style='color:red; text-align: left;'>Error: <ul>"+errMsg+"</ul><hr><br></div>";
		}
		return errMsg;
	}
	
	function getProtoPOCFromDlg(mode) {
		var pocId = $("#protoPOCId").val();
		var pocFirstName = $.trim($("#protoPOCFirstName").val());
		var pocMiddleName = $.trim($("#protoPOCMiddleName").val());
		var pocLastName = $.trim($("#protoPOCLastName").val());
		var position = $.trim($("#protoPOCPosition").val());
		var pocPhone = $.trim($("#protoPOCPhone").val());
		var pocEmail = $.trim($("#protoPOCEmail").val());
		
		var pocAddressId = $.trim($("#protoPOCAddressId").val());
		var pocAddressOne = $.trim($("#protoPOCAddressOne").val());
		var pocAddressTwo = $.trim($("#protoPOCAddressTwo").val());
		var pocCity = $.trim($("#protoPOCCity").val());
		var pocState = $.trim($("#protoPOCState").find(":selected").val());
		var pocCountry = $.trim($("#protoPOCCountry").find(":selected").val());
		var pocZipCode = $.trim($("#protoPOCZipCode").val());
		
		var status = "";
		if (mode == "add"){
			status = "added";
		} 
		if (mode == "edit"){
			status = "edited";
		}
		var address = new Address (pocAddressId, pocAddressOne, pocAddressTwo, pocCity, pocState, pocCountry, pocZipCode);
		var poc = new PointOfContact(pocId, pocFirstName, pocMiddleName, pocLastName, position, pocPhone, pocEmail, address, status);
		
		return poc;
	}
	
	function getSelectedPOCJson(selectedRow){
		var address = new Address (selectedRow.addressId, selectedRow.addressOne, selectedRow.addressTwo, selectedRow.city, 
									selectedRow.state, selectedRow.country, selectedRow.zipCode);
		var poc = new PointOfContact(selectedRow.pocId, selectedRow.pocFirstName, selectedRow.pocMiddleName, 
										selectedRow.pocLastName, selectedRow.position, selectedRow.pocPhone, selectedRow.pocEmail, address, selectedRow.status);
		return poc;
	}
	
	function addOrEditProtoPOC(mode, protoPOC, editOrigJsonStr) {
		var actionUrl = basePath + "/protocol/protocolPointOfContactAction!addProtoPointOfContact.action";
		if(mode == "edit") {
			actionUrl = basePath + "/protocol/protocolPointOfContactAction!editProtoPOintOfContact.action";
		}
		
		var protoPOCStr = JSON.stringify(protoPOC); //console.log("Add protoPOCStr: "+protoPOCStr);
		var postData = new FormData();
		postData.append("jsonString", protoPOCStr);
		if(mode == "edit") {
			postData.append("editOrigJsonStr", editOrigJsonStr);
		}
		
		$table = $("#protoPOCDTTable");
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
	
	function removeProtoPOCs(protoPOCStr, $table){
		//console.log("remove protoPOCStr: "+protoPOCStr);
		var postData = new FormData();
		postData.append("jsonString", protoPOCStr);
		var actionUrl = basePath + "/protocol/protocolPointOfContactAction!deleteProtoPointOfContact.action";
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
	
	function clearProtoPOCEntry() {

		$("#protoPOCFirstName").val("");
		$("#protoPOCMiddleName").val("");
		$("#protoPOCLastName").val("");
		$("#protoPOCPosition").val("");
		$("#protoPOCPhone").val("");
		$("#protoPOCEmail").val("");
		$("#protoPOCAddressId").val("")
		$("#protoPOCAddressOne").val("");
		$("#protoPOCAddressTwo").val("");
		$("#protoPOCCity").val("");
		$("#protoPOCState").val("");
		$("#protoPOCCountry").val("");
		$("#protoPOCZipCode").val("");
		
		$("#protoPOCErrMsgContainer").html("");
	}
</script>

