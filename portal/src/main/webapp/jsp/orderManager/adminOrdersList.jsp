<%@include file="/common/taglibs.jsp"%>
<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@taglib prefix="decorator"
	uri="http://www.opensymphony.com/sitemesh/decorator"%>
<%@taglib prefix="page" uri="http://www.opensymphony.com/sitemesh/page"%>
<%@taglib prefix="s" uri="/struts-tags"%>

<title>Your Orders</title>

<div id="content-wrapper" style="margin-top: 0px; height: 100%" >

	<div class="lightbox" id="orderReportDiv"></div>
</div>
<div class="border-wrapper">
	<p>
		<a href='<s:url action="openQueue" />'>Your Queue (<s:property
				value="queueSize" />)
		</a>&nbsp;&nbsp; <a href='<s:url action="viewOrders" />'>Your Orders (<s:property
				value="orderSize" />)
		</a>
		<s:if test="isOrderManagerAdmin">&nbsp;&nbsp;<a
				href='<s:url action="adminOrders" />'>Admin Orders (<s:property
					value="adminOrderSize" />)
			</a>
		</s:if>
	</p>
	<h1>Orders for Administration</h1>
	<div id="content-wrapper">
		<s:if test="errorMessage neq null">

			<div id="errorMessage" class="errors">
				<h2>
					<s:property value="errorMessage" />
				</h2>
			</div>
		</s:if>
		
		<div class="clear-float">
			<div id="boxBtns" class="button inline-left-button">
				
			</div>
		</div>
		<div id="adminOrdersContainer" class="idtTableContainer" style="width: 98%">
			<table id="adminOrders" class="table table-striped table-bordered" width="100%"></table>
		</div>
	</div>
	<form id="chooseOrder" name="chooseOrder" method="post" action="adminOrder!openAdminOrder.action">
		<input type="hidden" id="orderId" name="orderId" value="0"> 
		<input type="hidden" id="orderOwnerId" name="orderOwnerId" value="0">
 		<s:hidden id="removeOrderList" name="removeOrderList" value="" />
	</form>
</div>


<script type="text/javascript">
jQuery.fn.extend({
	chooseOrder: function (orderId,userId) {
		

		$('#orderId').val(orderId);

		$('#orderOwnerId').val(userId);

		 $('#chooseOrder').submit();
	}
});

//Sets the navigation menus on the page
setNavigation({
	"bodyClass" : "primary",
	"navigationLinkID" : "userManagementModuleLink",
	"subnavigationLinkID" : "userManagementToolLink",
	"tertiaryLinkID" : "changePasswordLink"
});

function buildOrderList() {
	var selectedIds = $("#adminOrders").idtApi("getSelectedOptions");
	var idString = selectedIds.join(",");
	$("input[name=removeOrderList]").attr("value", idString);
	
}

function removeOrders() {
	
	buildOrderList();

	var dlgId = $.ibisMessaging(
			"dialog", 
			"warning", 
			"Are you sure you wish to delete the selected order(s)? All information associated with this order will be lost and cannot be restored.",
			{
				id: 'deleteRows',
				container: 'body',
				buttons: [{
					id: "yesBtnA",
					text: "OK", 
					click: _.debounce(function() {
						$(this).siblings().find("#yesBtnA").prop("disabled", true);
						var selectedRows = $("#adminOrders").idtApi('getSelectedOptions');
		
						$("#adminOrders").idtApi("getTableApi").rows('.selected').remove().draw(false);
						selectedRows.length = 0;
							
						var theForm = document.forms['chooseOrder'];
						theForm.action='adminRemoveOrders!removeBiospeceminOrderAdmin.action';
					    theForm.submit();
					    
						$.ibisMessaging("close", {type: 'dialog'});				
					}, 1000, true)
				},
				{
					text: "Cancel",
					click: function() {
						$.ibisMessaging("close", {id: dlgId});					
					}
				}],
				modal: true,
				width: "400px",
				title: "Confirm Deletion"
			}
		);
	

	
};

function generateReportLightbox() {
	$.post("orderAction!reportLightbox.ajax",
		function(data) {
			/*$.fancybox({
		    	'content':data,
		    	'width':'350',
		    	'autoSize':false
			});*/
			$.bricsDialog(data);
		});
	}
	
	

function generateUnblindReport() {
	var selectedIds = $("#adminOrders").idtApi("getSelectedOptions");
	var status = $("#adminOrders").idtApi("cellContent", "STATUS"); //console.log("cellContent status: "+status);
	if(status == "Shipped") {
		var url = "/portal/ordermanager/biosampleUnblind!generateUnblindReport.action?orderId=" + selectedIds[0];
		window.location.href=url;
	}

}
	
function generateBiospecimenReport() {
	
	$.ajax({
		type: "POST",
		cache: false,
		url: "biospecimenReportAction!validateShippedForm.ajax",
		data: $("form").serializeArray(),
		success: function(data) {
			if (data == "success")
			{
				//add searchLocations and SearchKey to form
				var theForm = document.forms['biospecimenReportAction'];				
				theForm.action = "/portal/ordermanager/biospecimenReportAction!download.action";
				theForm.submit();
				$.fancybox.close();
			}
			else
			{
				
				$.fancybox({
			    	'content':data,
			    	'width':'350',
			    	'autoSize':false
				});
			}
		}
	});
}


var oTable = $('#orderListTable').dataTable( {
	
	"bJQueryUI": true,  
    "bFilter": false,
    "sPaginationType": "full_numbers",
    "sScrollY": "400",
    "sScrollX": "100%",
    "bScrollCollapse": true,
    "iDisplayLength":25,
    "aaSorting": [[ 3, "desc" ]],
    "aoColumnDefs": [{"aTargets": [ 6 ],
                    	"bSortable":  false
                    	
                     }]
    	
} );

$(document).ready(function() {
	if (typeof dataTablesDisabled == "undefined" || dataTablesDisabled != true) { 
		if (typeof IbisDataTables != "undefined") {
			IbisDataTables.fullBuild();
		}
	}
	
	$("#adminOrders").idtTable({
        idtUrl: "<s:url value='/ordermanager/adminOrders!getAdminOrdersList.ajax' />",
		idtData: {
			primaryKey: 'id'
		},
		dom: 'Bfrtip',
		select: "multi",
        columns: [
            {
                name: 'adminTitle',
                title: 'OID',
                parameter: 'adminTitle',
                data: 'adminTitle'
            },
            {
                name: 'orderTitle',
                title: 'TITLE',
                parameter: 'orderTitle',
                data: 'orderTitle'
            },
            {
                name: 'orderStatus',
                title: 'STATUS',
                parameter: 'orderStatus',
                data: 'orderStatus'
            },	      
            {
                name: 'dateCreated',
                title: 'DATE CREATED',
                parameter: 'dateCreated',
                data: 'dateCreated'
            },
            {
                name: 'dateSubmitted',
                title: 'SUBMITTED ON',
                parameter: 'dateSubmitted',
                data: 'dateSubmitted'
            },
            {
            	name: 'submitterName',
            	title: 'SUBMITTER',
            	parameter: 'submitterName',
            	data: 'submitterName'
            }
        ],
		buttons: [
			{
				text: 'Remove Order',
				className: 'idt-removeButton',
				enabled: false,
		 	 	action: function() {			 		
		 	 		removeOrders();
			   	} 
		 	},
			{
				text: 'Generate Report',
				className: 'idt-generateReportButton',
				enabled: true,
		 	 	action: function() {			 		
		 	 		generateReportLightbox();
			   	} 
		 	},
		 	{
				text: 'Manifest',
				className: 'idt-manifestButton',
				enabled: false,
       		 	enableControl: {
                    count: 1,
                    invert: true
                },
		 	 	action: function() {			 		
		 	 		generateUnblindReport();
			   	} 
		 	}
		]
		
	});
	//disable manifest button when the selected row does not have a "Shipped" status
	var adminOrdersTable = $("#adminOrders").idtApi("getTableApi");
	adminOrdersTable.on('select', function(e, dt, type, indexes){
		var rowData = $("#adminOrders").idtApi('getApiRow', '.selected').data();
		var status = rowData.orderStatus; //console.log("select status: "+status);
		
		if( status != "" && status != "Shipped"){
			$('.idt-manifestButton').addClass("disabled");
		} 
	});
	adminOrdersTable.on('deselect', function(e, dt, type, indexes){
		var selectedRows = $("#adminOrders").idtApi('getSelectedOptions');
		if(selectedRows.length != 1) {
			$('.idt-manifestButton').addClass("disabled"); 
		} else {
			var rowData = $("#adminOrders").idtApi('getApiRow', '.selected').data();
			var status = rowData.orderStatus; //console.log("deselect status: "+status);
			if( status != "" && status != "Shipped"){
				$('.idt-manifestButton').addClass("disabled");
			} 
		}
		
	});

});

</script>