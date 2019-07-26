<%@include file="/common/taglibs.jsp"%>
<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@taglib prefix="decorator"
	uri="http://www.opensymphony.com/sitemesh/decorator"%>
<%@taglib prefix="page" uri="http://www.opensymphony.com/sitemesh/page"%>
<%@taglib prefix="s" uri="/struts-tags"%>

<title>Your Orders</title>
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
	<h1>Your Created Orders</h1>
	<div id="content-wrapper">
		<s:if test="errorMessage neq null">

			<div id="errorMessage" class="errors">
				<h2>
					<s:property value="errorMessage" />
				</h2>
			</div>
		</s:if>
		
		<div id="ordersBoxIDTContainer" style="width: 95%; margin-left: 10px;" class="idtTableContainer">
			<table class="table table-striped table-bordered" id="ordersBoxIDT" width="100%"></table>
		</div>
		
		
		
	</div>
	<form id="chooseOrder" name="chooseOrder" method="post" action="openOrder!openOrder.action">
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
	var selectedArr = $("#ordersBoxIDT").idtApi('getSelectedOptions');
	var selectedList = selectedArr.join(",");
	
	$("input[name=removeOrderList]").attr("value",selectedList);
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
    				click: _.debounce(function(){
    					$(this).siblings().find("#yesBtnA").prop("disabled",true);
    					var selectedRows = $("#ordersBoxIDT").idtApi('getSelectedOptions');
    					
    					$("#ordersBoxIDT").idtApi("getTableApi").rows('.selected').remove().draw(false);
    					selectedRows.length = 0;
    					
    					var theForm = document.forms['chooseOrder'];
    					theForm.action='removeOrders!removeBiospeceminOrder.action';
    				    theForm.submit();
    				    
    					$.ibisMessaging("close", {type: 'dialog'});				
					}, 1000, true)
    			},
    			{
    				text: "Cancel",
    				click: function() {
    					$.ibisMessaging("close",{type: 'dialog'});
    				}
				}],
				modal: true,
				width: "400px",
				title: "Confirm Deletion"
			}
		);
};

$(document).ready(function() {
	
	$("#ordersBoxIDT").idtTable({
		idtUrl: "<s:url value='/ordermanager/viewOrders!getBiosampleOrdersList.ajax' />",
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
		 	}
		]
        
	});
});

</script>