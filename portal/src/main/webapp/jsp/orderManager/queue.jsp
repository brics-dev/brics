<%@include file="/common/taglibs.jsp"%>
<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@taglib prefix="decorator" uri="http://www.opensymphony.com/sitemesh/decorator"%>
<%@taglib prefix="page" uri="http://www.opensymphony.com/sitemesh/page"%>
<%@taglib prefix="s" uri="/struts-tags"%>

<style>
	.updateQuantity {
		float: right;
	    margin-right: 10px;
	}
	.updateQuantity #addQuantity {
	    font-weight: bold;
	    color: #000;
	    padding: 5px;
	    background: white;
	    border: 1px solid #b6b2b2;
	    border-radius: 2px;	
	}
</style>

<title>BRICS Biosample Orders - Queue</title>


<div class="border-wrapper">
<p><a href='<s:url action="openQueue" />' >Your Queue (<span id="queueSize"><s:property value="queueSize" /></span>)</a>&nbsp;&nbsp;
<a href='<s:url action="viewOrders" />' >Your Orders (<s:property value="orderSize" />)</a>
<s:if test="isOrderManagerAdmin" >&nbsp;&nbsp;<a href='<s:url action="adminOrders" />'>Admin Orders (<s:property value="adminOrderSize" />)</a></s:if>

</p>

		<h3 id="advancedFilterLabel" class="clear-both collapsable">
			<span id="advancedFilterLabelPlusMinus"></span>&nbsp; Advanced Filter
		</h3>
		<div id="advancedFilter">
			 <div>
			 	<label for="SampleTypes">Sample Type</label>				 
				<select name="sampleTypes" class="filterOptions">
					<option value="sampleType:all" class="allOption" selected="selected">All Sample Types</option>
					<c:forEach items="${sampleTypes}" var="sampleType">
						<option value="sampleType:${sampleType}"> ${sampleType}</option>
					</c:forEach>
				</select>
			</div> 
			<br />
			<div>
				<label for="VisitType">Visit Type</label>
				<select name="visitTypes" class="filterOptions">
					<option value="visitType:all" class="allOption" selected="selected" >All Visit Types</option>
						<c:forEach items="${visitTypes}" var="visitType">
							<option value="visitType:${visitType}"> ${visitType}</option>
						 </c:forEach>
				</select>
			</div>
			<br />
			<br />
			<div id="boxBtns" class="button inline-right-button">
				<button  class="btnBiosampleSearch" >Filter</button>
				<button  class="btnResetBiosampleSearch">Reset</button>	
			</div>
		</div>		
		<br /><br /><br /><br />
		
	<h1>View Your Bio Materials Queue</h1>
		<div id="content-wrapper">
		<s:if test="errorMessage neq null">
			
<div id="errorMessage" class="errors">
<h2><s:property value="errorMessage" /></h2>
</div>
		</s:if>
		
	<div id="queueBox">
	
		<c:forEach items="${queue.items}" var="item">
			<input 
				type="hidden" 
				id="quantity_<c:out value="${item.id}"></c:out>"
				value="1" />
		</c:forEach>
		
	
		<div id="statusBar"></div>

		<div id="bioMaterialsQueueId" style="width: 98%;" class="idtTableContainer">
			<table id="bioMaterialsQueue" class="table table-striped table-bordered"></table>
		</div>
		
		<div id="boxBtns" class="button inline-right-button">
			<button id="Add">Add to order</button>
			<div style="clear:both;"></div>
			<div id="ordersList">
					
			<span><a href="javascript:void(0);" id="newOrder" >New Order</a></span>
						
			<c:forEach items="${userOrders}" var="item">
			<c:choose>
				<c:when test="${item.orderStatus.value == 'Created'}">
				<span><a href="javascript:void(0);" onClick="$().addToOrder(<c:out value="${item.id}"></c:out>)">Order:<c:out value="${item.orderTitle}"></c:out></a></span>
				</c:when>
				<c:otherwise>
					<span><a href="javascript:void(0);" onClick="$().launchReasonForChangeDialog(<c:out value="${item.id}"></c:out>)" >Order:<c:out value="${item.orderTitle}"></c:out></a></span>
				</c:otherwise>
			</c:choose>
			</c:forEach>	
			</div>
		</div>
		
	</div>
</div>
</div>
<form id="queueItemForm" method="POST"  name="queueItemForm" action="orderManager.action">
	<!-- This form intentionally left blank -->
</form>

<script type="text/javascript">

$(document).ready(function() {
	
$("#bioMaterialsQueue").idtTable({
	
	idtUrl: "<s:url value='openQueue!getQueueList.action' />",
	idtData: {
         primaryKey: "id"
     },
     length: 15,
     dom: 'Bf<"updateQuantity">rtip',
     select: "multi",
	"columns": [
	{
		"data": "coriellId",
		"title": "SAMPLE ID",
		"name": "coriellId",
		"parameter": "coriellId",

	},
	{
		"data": "bioRepositoryName",
		"title": "REPOSITORY",
		"name": "bioRepositoryName",
		"parameter": "bioRepository.name",

	},
	{
		"data": "sampCollType",
		"title": "SAMPLE TYPE",
		"name": "sampCollType",
		"parameter": "sampCollType",

	},
	{
		"data": "guid",
		"title": "GUID",
		"name": "guid",
		"parameter": "guid",

	},
	{
		"data": "visitTypePDBP",
		"title": "VISIT TYPE",
		"name": "visitTypePDBP",
		"parameter": "visitTypePDBP",

	},
	{
		"data": "inventoryValue",
		"title": "INV",
		"name": "inventoryValue",
		"parameter": "inventoryValue",

	},
	{
		"data": "inventoryDate",
		"title": "INV DATE",
		"name": "inventoryDate",
		"parameter": "inventoryDate",

	},
	{
		"data": "queueQuantityInputField",
		"title": "QTY",
		"name": "queueQuantityInputField",
		"parameter": "queueQuantityInputField",

	},
	{
		"data": "unitNumber",
		"title": "UNIT #",
		"name": "unitNumber",
		"parameter": "unitNumber",

	},
	{
		"data": "unitMeasurement",
		"title": "UOM",
		"name": "PRIVILEGE",
		"parameter": "unitMeasurement",

	}
	],
	initComplete: function(settings) {

	    var oTable = $("#bioMaterialsQueue").idtApi("getTableApi");
							
	},
	buttons : [
		{
     		 text: "Remove From Queue",
     		 className: 'idt-removeQueueBtn',
     		 enabled: false,
  	    	 action: function(e, dt, node, config) {
  	    		removeFromQueue(e);
      	   	}
		}				
	]
	

});

var massUpdate = '<label for="massQuantity" id="quantityLabel" style="width:100px;" >Mass Quantity:</label>'+
					'<input type="text" id="quantity" size = "7" disabled="disabled" '+
					'title="Please select atleast one biosample to input quantity"/> '+
					'<input type="button" id="addQuantity" value="Update" class ="alwaysDisabled"/>'+
					'<input type="hidden" id="addQuantityHidden" />';
$("div.updateQuantity").html(massUpdate);


$(document).mouseup(function (e) {
    var container = $("#ordersList");

    if (!container.is(e.target) // if the target of the click isn't the container...
        && container.has(e.target).length === 0 && container.is(':visible')) { // ... nor a descendant of the container
        container.hide( 'slide', { direction: "down" } , 500);
    }
});




	
	$("#quantity").keyup(function(){
		if ($(this).val() == '') {
	        $('#addQuantity').prop('disabled', true);
	        $('#addQuantity').addClass("alwaysDisabled");
	    } else {
	        $('#addQuantity').prop('disabled', false);
	        $('#addQuantity').removeClass("alwaysDisabled");
	    }
	});

	var bioSampleTableFilters = {
			sampleType: "all",
			visitType: "all",
			diagnosis: "all"
	};


	//for advanced filtering on the biosample table
	if (typeof dataTableFilters === "undefined") {
		var dataTableFilters = bioSampleTableFilters;
	}
	else {
		dataTableFilters = $.extend({}, dataTableFilters, bioSampleTableFilters);
	}

	$("#boxBtns").on("click", ".btnBiosampleSearch", function() {
		var $this = $(".filterOptions");
		$this.each(function(){
	    
			var arrChangeVal = $(this).val().split(":");
			dataTableFilters[arrChangeVal[0]] = arrChangeVal[1];

		});
		
		advancedFilter(dataTableFilters);
		oTable.draw();
	});	
	
	$("#boxBtns").on("click", ".btnResetBiosampleSearch", function() {
		
		$("select").each(function() { 
			this.selectedIndex = 0;
		});
		
		var $this = $(".filterOptions");
		$this.each(function(){
			var arrChangeVal = $(this).val().split(":");
			dataTableFilters[arrChangeVal[0]] = arrChangeVal[1];
		});
		
		advancedFilter(dataTableFilters);
		oTable.draw();
		
	});		
	
	var advancedFilter = function(dataTableFilters) {
		$.fn.dataTableExt.afnFiltering.push(function(oSettings, aData, iDataIndex) {
			var oTable = $("#bioMaterialsQueue").idtApi("getTableApi");
			var oTableId = oTable.settings()[0].nTable.id;
			var sampleType = aData[3];
			var visitType = aData[5];

			if (oTableId != oSettings.sTableId) { return true; }
			else {
				if(dataTableFilters.sampleType != "all" && dataTableFilters.sampleType !=sampleType){
					return false;
				}
				
				if(dataTableFilters.visitType !="all" && dataTableFilters.visitType !=visitType){
					return false;
				}else {
					return true;
				}
			}

	});			
	}
	advancedFilterInit();
	
	
	//enable "Mass Quantity" textbox when any row is selected
	var oTable = $("#bioMaterialsQueue").idtApi("getTableApi");
	oTable.on('select',function(e, dt, type, indexes){
		var selectedOptions = $("#bioMaterialsQueue").idtApi("getSelectedOptions");
		
		 if(selectedOptions.length>0){
			$("#quantity").prop('disabled', false);	
		}
	});
	
	//disable textbox and update button when no row is selected
	oTable.on('deselect', function(e, dt, type, indexes){
		var selectedOptions = $("#bioMaterialsQueue").idtApi("getSelectedOptions");
		if(selectedOptions.length<1){
			$("#quantity").prop('disabled', true);
			$('#quantity').val('');
		}
		
	});
	
	
	// "remove from queue" button click
	$("#remove").click(function( event ) {
		removeFromQueue(event);
	 });
        
	function  removeFromQueue(event){
		var selectedOptions = $("#bioMaterialsQueue").idtApi("getSelectedOptions");
	 	if(selectedOptions.length < 1){
	 		alert('Please select an item from your queue to remove.');
	 	} 
	 	else {
	       	if(confirm("Are you sure you want to remove these items from your Queue?")) {
	 
		 		var sList = "";
	        	var rowArray = [];
	        	var count = 0;
	        	
	        	// Setup the ajax indicator
		  		  $('#statusBar').prepend('<div id="ajaxBusy" style="text-align:center;"><img src="../images/loading.gif"></div>');
			  
		  		var $form = $("#queueItemForm");
		  		$form.empty(); 
		  		var data = compileSubmissionData();
		  		for (var i = 0; i < data.length; i++) {
		  			// append the correct inputs to the form
		  			hiddenInput = "<input type=\"hidden\" name=\"itemsFromQueue("+data[i].id+").numberOfAliquots\" value=\""+data[i].numberOfAliquots+"\" />";
		  			hiddenInput += "<input type=\"hidden\" name=\"itemsFromQueue("+data[i].id+").id\" value=\"" + data[i].id + "\" />";
		  			hiddenInput += "<input type=\"hidden\" id=\"itemCheckList\" name=\"itemCheckList\" value=\"" + data[i].id + "\" />";
		  			
		  			$form.append(hiddenInput);
		  		}
		  		
		  		var selectedRows = $("#bioMaterialsQueue").idtApi("getSelectedOptions");
		  		var countSelected = selectedRows.length;
		  		
	        	$.post("openQueue!removeFromQueue.action", $("#queueItemForm").serialize() ,function(data){ 
		        	$('#ajaxBusy').hide();
		        	selectedRows.length=0;
		        	$("#bioMaterialsQueue").idtApi("getTableApi").rows('.selected').remove().draw(false);
					
		        	
			        //update menu number		
		       	    var curr_val = $('#queueSize').text();
		       	    var new_val = parseInt(curr_val) - countSelected;
		       	    $('#queueSize').text(new_val).fadeIn();
		       	
	        	});
	       	}
	 	}   

	}

    	
    //update queue
	$('#addQuantity').click(function( event ){ 

		   var selectedOptions = $("#bioMaterialsQueue").idtApi("getSelectedOptions");

		   var showDialogue = false;
		   var value = $("#quantity").val();
		   $("#addQuantityHidden").val(value);
		   
		   if(!$.isNumeric(value)){
		   			$.ibisMessaging("dialog", "error", "Quantity must be numeric", {container: "body"});
		   			$("#quantity").val('');
		   }
		   else{
			   
			   if (Math.floor(value) != value) {
				   	$.ibisMessaging("dialog", "error", "Quantity must be integer", {container: "body"});
		   			$("#quantity").val('');
			   } else if (value <= 0 ){				   
				   $.ibisMessaging("dialog", "error", "Quantity must be greater than 0", {container: "body"});
				   	$("#quantity").val('');
			   } else {
					//update quanity in queue across pages
			   	 	var rows = $("#bioMaterialsQueue").idtApi("getRows");
					   rows.each(function(row){
						  if (selectedOptions.indexOf(row["DT_RowId"])>=0){
							  var id = row["DT_RowId"];
							  var textFieldHtml = row["queueQuantityInputField"];
							  var $div = $('<div>').html(textFieldHtml);
							  var quantityVal = $("#quantity").val();
							  $div.find('input').attr('value',quantityVal);
							  $div.find('p').text(quantityVal);
							  row["queueQuantityInputField"] = $div.html();
							  
							  $("#quantity_" + id).val(quantityVal);
							  
							  var inventory = $("#inventory_" +id).text();
					   		   if(parseFloat(quantityVal)>parseFloat(inventory)){
					   				showDialogue = true;
					   		   }
						  } 
					   });
			   		oTable.clear().rows.add(rows).draw();
			   	   var info = "Warning: One or more of your requested order quantities exceeds the available inventory for that Biosample. You may still proceed with processing the Biosample order, but your order may be delayed due to further review. Please cross-reference your Biosample order quantities with the available Biosample inventory quantity and update accordingly.";
			   	   if(showDialogue==true){
			   			$.ibisMessaging("dialog", "warning", info, {container: "body", width: "600px"});
			   	   }
			   	   
			   		$("#quantity").val('');
			   		$('#addQuantity').prop('disabled', true);
			        $('#addQuantity').addClass("alwaysDisabled");
			   }
		   }
	});
			
	// this is the "add to order" button.  Terribly named
	$("#Add").click(function( event ) {
		event.preventDefault();
		//let's check that the user actually picked an item		
		var selectedOptions = $("#bioMaterialsQueue").idtApi("getSelectedOptions");
		if(selectedOptions.length < 1){
			alert('Please select an item from your Queue.');
		} 
		else {
			if($( "#ordersList" ).children().length > 1) {
				var count = ($( "#ordersList" ).children().length <= 6 ? $( "#ordersList" ).children().length - 1 : 5);
				var multiplyBy = 37;
				/*if(count == 2){
					multiplyBy = 31;
				}*/
				var top = -68;
				var newTop = top + (-1 * count * multiplyBy);
				$( "#ordersList" ).css( "top", newTop+"px" );
			} 
				
			$( "#ordersList" ).show( 'slide', { direction: "down" } , 500);
		}
	});
	
	
	// click on "new order" in order menu
	$("#newOrder").click(function( event ) {
		// get the data from the datatable
		var data = compileSubmissionData();
		
		var hiddenInput = "";
		var $form = $("#queueItemForm");
		$form.empty();
		$form.attr("action", "orderManager.action");
		
		for (var i = 0; i < data.length; i++) {
			// append the correct inputs to the form
			hiddenInput = "<input type=\"hidden\" name=\"itemsFromQueue("+data[i].id+").numberOfAliquots\" value=\""+data[i].numberOfAliquots+"\" />";
			hiddenInput += "<input type=\"hidden\" name=\"itemsFromQueue("+data[i].id+").id\" value=\"" + data[i].id + "\" />";
			hiddenInput += "<input type=\"hidden\" id=\"itemCheckList\" name=\"itemCheckList\" value=\"" + data[i].id + "\" />";
			
			$form.append(hiddenInput);
		}
		
		$form.submit();
		
		$('#queueItemForm').submit();
	});

});

jQuery.fn.extend({
	
	
	launchReasonForChangeDialog : function (orderId) {
		$( '#addToExistingOrderReason' ).dialog({
			 open: function(event,ui) {
				 $("#rChange").val("");
			 },
			modal:true,
			buttons: [
			            {
			              text: "Ok",
			              click: function() {
			            	  //validate there is text
			            	  if($("#rChange").val().trim() != "" && $("#rChange").val().length < 1000) {
			            		  $().addToOrder(orderId);
				            	  $( this ).dialog( "close" );
			            	  }  
			              }
			            },
			            {
			            	 text: "Cancel",
				              click: function() {
				                $( this ).dialog( "close" );
				              }
			            }
			          ]
		});
		
		
		
	},
	
	
	addToOrder: function (orderId) {
		// get the data from the datatable
		var data = compileSubmissionData();
		
		var hiddenInput = "";
		var $form = $("#queueItemForm");
		$form.empty();
		$form.attr("action", "orderManager!addToExistingOrder.action");
		$form.append('<input type="hidden" id="orderId" name="orderId" value="'+orderId+'" />');
		var reasonForChangeText = $("#rChange").val();
		$form.append('<input type="hidden" id="addToExistingOrderComments" name="addToExistingOrderComments" value="'+reasonForChangeText+'" />');
		
		for (var i = 0; i < data.length; i++) {
			// append the correct inputs to the form
			hiddenInput = "<input type=\"hidden\" name=\"itemsFromQueue("+data[i].id+").numberOfAliquots\" value=\""+data[i].numberOfAliquots+"\" />";
			hiddenInput += "<input type=\"hidden\" name=\"itemsFromQueue("+data[i].id+").id\" value=\"" + data[i].id + "\" />";
			hiddenInput += "<input type=\"hidden\" id=\"itemCheckList\" name=\"itemCheckList\" value=\"" + data[i].id + "\" />";
			
			$form.append(hiddenInput);
		}
		
		$form.submit();
	}
});

/**
 */
function QueueItem(numberOfAliquots, id) {
	this.numberOfAliquots = numberOfAliquots;
	this.id = id;
} 

/**
 * Copies quantity changes from the transient row element to a more persistent
 * storage area.  I'd almost prefer to use backbone for this :-(
 */
function changeQty(id, input) {
	var $input = $(input);
	var $qtyMaintainer = $("#quantity_" + id);
	$qtyMaintainer.val($input.val());

	var columnIdx = 8;
	updateDTCellContent(columnIdx, $input);
}
 
/*set hidden p element value and update the cell data in datatable*/
function updateDTCellContent(columnIdx, target){	
	var rowData = $("#bioMaterialsQueue").idtApi('getApiRow', target.parent()).data(); 
	var rowIndex = $("#bioMaterialsQueue").idtApi('getApiRow', target.parent()).index();
	
	var inputQty = target.val();
	
	var textFieldHtml = rowData.queueQuantityInputField;
	var $div = $('<div>').html(textFieldHtml);
	$div.find('input').attr('value',inputQty);
	$div.find('p').text(inputQty);
	
	rowData.queueQuantityInputField = $div.html();
	//console.log("roleChanged() after rowData.queueQuantityInputField: "+JSON.stringify(rowData.queueQuantityInputField));
	
	$("#bioMaterialsQueue").dataTable().fnUpdate(rowData.queueQuantityInputField, rowIndex, columnIdx);
}

function compileSubmissionData() {
	var itemsFromQueue = [];
	
	var selectedOptions = $("#bioMaterialsQueue").idtApi("getSelectedOptions");
 	
	for (var i = 0; i < selectedOptions.length; i++) {
		var id = selectedOptions[i];
		var qtyInput = $("#quantity_" + id).val();
		var item = new QueueItem(qtyInput, id);
		itemsFromQueue.push(item);
	}
	
	return itemsFromQueue;
}

function advancedFilterInit() {
	
	$("#advancedFilterLabelPlusMinus").text("+");
	$("#advancedFilter").hide();
	
	$("#advancedFilterLabel").click(function(){			
		$("#advancedFilter").slideToggle("fast");
		if($("#advancedFilterLabelPlusMinus").text()=="+") {
			$("#advancedFilterLabelPlusMinus").text("- ");
		} else {
			$("#advancedFilterLabelPlusMinus").text("+");
		}
		
	});
	
}



    
//Sets the navigation menus on the page
setNavigation({
	"bodyClass" : "primary",
	"navigationLinkID" : "userManagementModuleLink",
	"subnavigationLinkID" : "userManagementToolLink",
	"tertiaryLinkID" : "changePasswordLink"
}); 



</script>

<div style="display:none" id="addToExistingOrderReason">
	<div>
		<label>Reason for Change</label>
		<textarea rows="2" cols="35" id="rChange"></textarea>
	</div>
</div>