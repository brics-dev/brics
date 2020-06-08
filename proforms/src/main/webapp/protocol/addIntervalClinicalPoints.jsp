<%@ taglib uri="/struts-tags" prefix="s" %>
<%@ taglib uri="/WEB-INF/datatables.tld" prefix="idt" %>


<script type="text/javascript">

	function ClinicalLocation (clinicalLocationId, clinicalLocationName){
		this.clinicalLocationId = clinicalLocationId;
		this.clinicalLocationName = clinicalLocationName;
	}
	function Procedure (procedureId, procedureName){
		this.procedureId = procedureId;
		this.procedureName = procedureName;
	}
	function PointOfContact (pocId, pocFullName){
		this.pocId = pocId;
		this.pocFullName = pocFullName;
	}
	function IntervalClinicalPoint(id, clinicalLocation, procedure, pointOfContact, status) {
		this.id = id;
		this.clinicalLocation = clinicalLocation;
		this.procedure = procedure;
		this.pointOfContact = pointOfContact;
		this.status = status;
	}
	/**
	 * Renders the site, procedure and pointOfContact checkboxes for the interval based on the passed in array of these three objects. 
	 */
	
	
	
	function renderIntevalTemplates(){
		
		//console.log("intervalClinicalListStr: "+$("#intervalClinicalListStr").val());
		var intervalClinicalTemplate = Handlebars.compile($("#intervalClinicalTemplate").html());
		var intervalClinicalArray = jQuery.parseJSON($("#intervalClinicalListStr").val()); 
		var clinicalMarkUp = intervalClinicalTemplate({"intervalClinicalArray" : intervalClinicalArray});
		$("#intervalClinicalFields").html(clinicalMarkUp);
		
		var intervalProcedureTemplate = Handlebars.compile($("#intervalProcedureTemplate").html());
		var intervalProcedureArray = jQuery.parseJSON($("#intervalProcedureListStr").val());
		var procedureMarkup = intervalProcedureTemplate({"intervalProcedureArray": intervalProcedureArray});
		$("#intervalProcedureFields").html(procedureMarkup);
		
		var intervalContactTemplate = Handlebars.compile($("#intervalContactTemplate").html());
		var intervalContactArray = jQuery.parseJSON($("#intervalPOCListStr").val());
		var contactMarkup = intervalContactTemplate({"intervalContactArray": intervalContactArray});
		$("#intervalPOCFields").html(contactMarkup);

	 }
	
	var basePath = "<s:property value='#webRoot'/>";
	$(document).ready(function() {
		renderIntevalTemplates();

		$("#intervalCPDTTable").idtTable({
			idtUrl: basePath + "/protocol/intervalClinicalPointAction!getIntervalClinicalPointDTList.action",
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
	                name: 'clinicalLocationId',
	                title: 'Clinical Location Id',
	                parameter: 'clinicalLocationId',
	                data: 'clinicalLocationId',
	                visible: false
	            },
	            {
	                name: 'clinicalLocationName',
	                title: 'Clinical Location',
	                parameter: 'clinicalLocationName',
	                data: 'clinicalLocationName'
	            },
	            {
	                name: 'pointOfContactId',
	                title: 'Point Of Contact Id',
	                parameter: 'pointOfContactId',
	                data: 'pointOfContactId',
	                visible: false
	            },
	            {
	                name: 'pointOfContactName',
	                title: 'Point Of Contact',
	                parameter: 'pointOfContactName',
	                data: 'pointOfContactName'
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
	   				className: "DeletePvBtn",
	   				enabled: false,
	     	    	action: function(e, dt, node, config) {
    	    			
	        	   		var selectedRows = dt.rows('.selected').data().to$();
						var intervalCPArr = [];
						// Convert the array of IDs to a comma delimited string list 
						for (var idx = 0; idx < selectedRows.length; idx++) {
							var intervalCPJson = getSelectedIntCPJson(selectedRows[idx]);
							intervalCPArr.push(intervalCPJson);
						}

	        	   		var $table = $('#intervalCPDTTable');
	        	   		var dlgId = $.ibisMessaging(
	        	   				"dialog", 
	        	   				"warning", 
	        	   				"Are you sure you wish to delete the selected Clinical Point(s)?",
	        	   				{
	        	   					id: 'deleteRows',
	        	   					container: 'body',
	        	   					buttons: [{
	        	   						id: "yesBtnA",
	        	   						text: "Delete", 
	        	   						click: _.debounce(function() {
	        	   							$(this).siblings().find("#yesBtnA").prop("disabled", true);

	        	   							removeIntervalClinicalPnt(JSON.stringify(intervalCPArr), $table);
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
								$.ibisMessaging("dialog", "info", "No clinical points are selected.");
								
								return false;
							}	else {
								//console.log("selectedRow: "+JSON.stringify(selectedRow));
								openIntervalCPDialog('edit', selectedRow);
							}
	   				}
	   			}
	        ]
		});
		
		//enable checkboxes in site section can only be checked once
		$("input.intervalClinicalChkBox").click(function() {
        	$('input[type=checkbox].intervalClinicalChkBox').not(this).prop('checked', false);
    	});
		$("input.intervalProcedureChkBox").click(function() {
	        $('input[type=checkbox].intervalProcedureChkBox').not(this).prop('checked', false);
	    });
		$("input.intervalContactChkBox").click(function() {
        	$('input[type=checkbox].intervalContactChkBox').not(this).prop('checked', false);
    	});
		

		$("#openIntervalCPDlg").click(function(){
			
			if($("#intervalClinicalListStr").val() != '[]' && $("#intervalProcedureListStr").val() != '[]' && $("#intervalPOCListStr").val() != '[]'){
				//if clinical info, procedure and point of contact have been associated to the protocol, showing clinical point dialog
				openIntervalCPDialog('add', null);
			} else {
				//otherwise showing a redirect message to ask redircting
				var dlgId = $.ibisMessaging("dialog", "info", 
						"There are no clinical locations, procedures and point of contacts associated to this protocol. Would you like to go to Protocol Information page to add these information?", {
					modal : true,
					buttons : [
						{
							text : "Yes, redirect",
							click : _.debounce(function() {
								var url= basePath+"/protocol/showStudy.action?studyId="+$("#studyId").val();
								redirectWithReferrer(url);
							}, 1000, true)
						},						
						{
							text : "No, don't add",
							click : function() {
								$.ibisMessaging("close", {id: dlgId});	
							}
						}
					],
					width: "400px",
   					title: "Redirect to Protocol Information"
				});
			}
		});
		
	}); //end document.ready
	function getSelectedIntCPJson(selectedRow) {
		var procedure = new Procedure(selectedRow.procedureId, selectedRow.procedureName);
		var clinicalLocation = new ClinicalLocation(selectedRow.clinicalLocationId, selectedRow.clinicalLocationName);
		var pointOfContact = new PointOfContact(selectedRow.pointOfContactId, selectedRow.pointOfContactName);
		var intervalCP = new IntervalClinicalPoint(selectedRow.id, clinicalLocation, procedure, pointOfContact, selectedRow.status);
		return intervalCP;
	}
	 function populateIntervalClinicLocForm(selectedRow){
		 if(selectedRow != null){
			 var id = selectedRow.id;
			 var clinicalLocationId = selectedRow.clinicalLocationId;
			 var procId = selectedRow.procedureId;
			 var pocId = selectedRow.pointOfContactId;
			 
			 $("#intervalClinicLocId").val(id);
			 $('input#clinical_'+clinicalLocationId).prop("checked", true);
			 $('input#procedure_'+procId).prop("checked", true);
			 $('input#poc_'+pocId).prop("checked", true);
		 }
	 }
	 function openIntervalCPDialog(mode, selectedRow) {
		 if(mode == "edit") {
			 populateIntervalClinicLocForm(selectedRow);
		 }

			$("#IntervalClinicalPointDlg").dialog({
				autoOpen : false,
				height : 300,
				width : 900,
				position : {
					my : "center",
					at : "center",
					of : window
				},
				buttons : [ {
					id : "sbmIntervalCPBtn",
					text : "Submit",
					click : function() {
						if (validateIntervalClinicalPoint()) {
							var intervalClinicalPoint = getIntervalClinicalPointFromDlg(mode);
							addOrEditIntevalClinicalPoint(mode, intervalClinicalPoint);
							$(this).dialog("close");
						}
					}
				}, {
					id : "cancelIntervalCPBtn",
					text : "Clear List",
					click : function() {
						clearIntervalCPChkBx();
						//$("#IntervalClinicalPointDlg").dialog("close");
					}
				} ],

				close : function() {
					clearIntervalCPChkBx();
					$(this).dialog('destroy');
				}
			});
			$("#IntervalClinicalPointDlg").dialog("open");
	}	
	
	function validateIntervalClinicalPoint() {
		var valid = false;
		var errMsg = "";


		if ($(".intervalClinicalChkBox:checkbox:checked").length == 1 && 
				$(".intervalProcedureChkBox:checkbox:checked").length == 1 && 
				$(".intervalContactChkBox:checkbox:checked").length == 1 ){			
			valid = true;	
			errMsg = "";
		} 
				
		if ($(".intervalClinicalChkBox:checkbox:checked").length == 0) {
			errMsg +="<li>Clinical location is required. Please check one of the clinical locations.</li>";			
		} else if ($(".intervalClinicalChkBox:checkbox:checked").length > 1 ) {
			errMsg +="<li>Please check only one clinical location at one time.</li>";
		}
		if ($(".intervalProcedureChkBox:checkbox:checked").length == 0) {
			errMsg +="<li>Procedure is required. Please check one of the procedures.</li>";			
		} else if ($(".intervalProcedureChkBox:checkbox:checked") > 1 ) {
			errMsg +="<li>Please check only one procedure at one time.</li>";
		}
		if ($(".intervalContactChkBox:checkbox:checked").length == 0) {
			errMsg +="<li>Point of contact is required. Please check one of the point of contacts.</li>";			
		} else if ($(".intervalContactChkBox:checkbox:checked").length > 1 ) {
			errMsg +="<li>Please check only one point of contact at one time.</li>";
		}
		
		if(errMsg.length > 0){
			errMsg = "<div style='color:red;'>Error: <ul>"+errMsg+"</ul><hr><br></div>";
		}

		$("#intervalCPMessageContainer").html(errMsg);
		console.log("validate result: "+valid);
		return valid;
	}
	
	function getIntervalClinicalPointFromDlg(mode) {
		var id = $("#intervalClinicLocId").val();
		var clinicalLocationId = $(".intervalClinicalChkBox:checkbox:checked").attr("id").split("_")[1];;
		var clinicalLocationName = $(".intervalClinicalChkBox:checkbox:checked").val();;
		var clinicalLocation = new ClinicalLocation (clinicalLocationId, clinicalLocationName);

		var procedureId = $(".intervalProcedureChkBox:checkbox:checked").attr("id").split("_")[1];
		var procedureName = $(".intervalProcedureChkBox:checkbox:checked").val();
		var procedure = new Procedure(procedureId, procedureName);

		var pocId = $(".intervalContactChkBox:checkbox:checked").attr("id").split("_")[1];
		var pocFullName = $(".intervalContactChkBox:checkbox:checked").val()
		var poc = new PointOfContact(pocId, pocFullName);

		if (mode == "add"){
			status = "added";
		} 
		if (mode == "edit"){
			status = "edited";
		}
		
		var intervalClinicalPoint = new IntervalClinicalPoint(id, clinicalLocation, procedure, poc, status);
		return intervalClinicalPoint;
	}
	
	function addOrEditIntevalClinicalPoint(mode,intervalClinicalPoint) {
		var actionUrl = basePath + "/protocol/intervalClinicalPointAction!addIntervalClinicalPoint.action";
		if(mode == "edit") {
			actionUrl = basePath + "/protocol/intervalClinicalPointAction!editIntervalClinicalPoint.action";
		}
		var intervalClinicalPointStr = JSON.stringify(intervalClinicalPoint); //console.log("Add intervalClinicalPoint: "+intervalClinicalPointStr);
		var postData = new FormData();
		postData.append("intervalClinicalPOCStr", intervalClinicalPointStr);
		
		$.ajax({
			type : "POST",
			url : actionUrl,
			data : postData,
			cache : false,
			processData : false,
			contentType : false,
			success : function(data) {
				$("#intervalCPDTTable").DataTable().ajax.reload();

			}
		});
	}
	
	function removeIntervalClinicalPnt(intClinicalPntJsonStr, $table){
		//console.log("remove intClinicalPntJsonStr: "+intClinicalPntJsonStr);
		var postData = new FormData();
		postData.append("intervalClinicalPOCStr", intClinicalPntJsonStr);
		var actionUrl = basePath + "/protocol/intervalClinicalPointAction!deleteIntervalClinicalPoint.action";
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
				//remove the selected rows in the data table
				var selectedRows = $table.idtApi('getSelectedOptions');
				selectedRows.length = 0;
				$table.idtApi('getTableApi').rows('.selected').remove().draw(false);				
				result = "success";
				console.log("remove result: "+result);
				
			}
		});
		return result;
	}
	
	function clearIntervalCPChkBx() {
		$('#IntervalClinicalPointDlg input:checkbox').prop('checked', false);
		$("#intervalCPMessageContainer").html("");
	}

</script>


<jsp:include page="/protocol/templates/intervalClinicalLocationChkBxs.jsp" />
<jsp:include page="/protocol/templates/intervalProcedureCheckBoxes.jsp" />
<jsp:include page="/protocol/templates/intervalContactCheckBoxes.jsp" />

<div id="addIntervalCPReminder" style="height: 50px; width: 858px;">
	<span class="reminder-message" style="float: left"> <b>Please Click "ADD Clinical Points" button to add to the Clinical Points Table.</b>
	</span>
	<div style="padding-right: 100px; float:right;">
		<input type="button" id="openIntervalCPDlg" value="ADD Clinical Points"/>
	</div>
</div>

<div class="idtTableContainer brics" id="intervalSPTableDiv" style="width: 858px;">
	<table id="intervalCPDTTable" class="table table-striped table-bordered" width="100%">
	</table>   
</div>

<div id="IntervalClinicalPointDlg" title="Add Interval Clinical Point" style="display: none;">
	<div id="intervalCPMessageContainer"></div>
	<s:hidden name="intervalClinicLocId" id="intervalClinicLocId" />
	<s:hidden name="intervalClinicals" id="intervalClinicalListStr" />
	<s:hidden name="intervalProcedures" id="intervalProcedureListStr" />
	<s:hidden name="intervalPointOfContacts" id="intervalPOCListStr" />

	<div class="formrow_1">
		<label for="intervalClinicalLoc" class="requiredInput" style="width: 175px; font-size: 11px;">
			<s:text name="protocol.intervalForm.create.clinicalLocation"/>
		</label>
		<div id="intervalClinicalFields"></div>
	</div>

	<div class="formrow_1">
		<label for="intervalProcedure" class="requiredInput" style="width: 175px; font-size: 11px;">
			<s:text name="protocol.intervalForm.create.procedure"/>
		</label>
		<div id="intervalProcedureFields"></div>
	</div>
	<div class="formrow_1">
		<label for="intervalPointOfContact" class="requiredInput" style="width: 175px; font-size: 11px;">
			<s:text name="protocol.intervalForm.create.pointOfContact"/>
		</label>
		<div id="intervalPOCFields"></div>
	</div>

</div>