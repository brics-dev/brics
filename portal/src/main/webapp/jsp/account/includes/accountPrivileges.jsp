<%@include file="/common/taglibs.jsp"%>

<div class="margin-top-lg">
	<h3>Account Privileges</h3>
	<div id="aaccoutRoleListContainer" class="idtTableContainer">
			<div id="dialog"></div>
			<table id="accountRoleListTable"class="table table-striped table-bordered" width="100%">
			<%-- Hidden input for setting the privilege expiration dates. --%>
			<input type="hidden" id="expireDateJson" name="accountPrivilegesForm.accountRoleExpirationJson" value="[]" />
			</table>
			<s:if test="(inAccountRenewal && isAccountAdmin)">
				<div><button id="commit-changes-button" class="dt-button  float-right" type="submit"><b>Commit Changes</b></button></div>
			</s:if>
	</div> 
	
</div>
<div id="addPermissionsDialog" style="display: none">
	<div id="addPermissionsInterface"></div>
</div>
<script type="text/javascript" src="/portal/js/account.js"></script>
<script>

$(document).ready(function() {
	var accountRoleListTable = $('#accountRoleListTable').idtTable({
		idtUrl: "<c:out value='${param.idtUrl}'/>",
		dom: "Bfrtip",
		autoWidth: false,
		"columns": [
			{
				"data": "privilege",
				"title": "PRIVILEGE",
				"name": "PRIVILEGE",
				"width": "28%",
				"parameter": "decoratedPrivilege",
	
			},
			{
				"data": "roleStatus",
				"title": "STATUS",
				"name": "STATUS",
				"width": "39%",
				"parameter": "decoratedStatusRequest"
			}, 
			{
				"data": "expirationDate",
				"title": "EXPIRATION DATE",
				"name": "EXPIRATION DATE",
				<s:if test="(inAccountRenewal && isAccountAdmin)">
				"parameter": "decoratedExpirationDateRequestWithDatePicker"
				</s:if>
				<s:else>
				"parameter": "decoratedExpirationDateRequest"
				</s:else>
				
				
			}
		],
		
		buttons : [<s:if test="(inAccountRenewal && isAccountAdmin)">{
    		 text: "Add Permissions",
    		 className: 'idt-createProtocolBtn',
    		 enabled: true,
   		 enableControl: {
                   count: 0,
                   invert: false
            },
 	    	 action: function(e, dt, node, config) {
 	    		addPermissionsLightbox();
     	   	}
		}</s:if>],
		
		"drawCallback": function( settings ) {
			var table = $(this);
			//bind cancel links
			
			$(".cancel-request").click(function(e) {
				e.preventDefault();
				var cancelActionAjax = "cancelRequestAction!cancelPrivilegeRequest.ajax";
				
				$.ajax({
			  		type: "post",
			  		data : {
			  			"privilegeId" : $(this).data('id'),
						
					},
			  		url: cancelActionAjax,
	
			  		success: function(response) {
						window.location.href = window.location.href;
						//need to reload the page instead, because the status could get updated
						//var dt = $('#accountRoleListTable').idtApi('getTableApi');
						//dt.ajax.reload();
						//$('#accountHistoryTable').DataTable().ajax.reload();	
			  		},
			  		error : function(error) {
			  			alert(error)
			  		}
				});			
			});
			
			
			$(".request-priv").click(function(e) {
				e.preventDefault();
				var requestPrivAjax = "requestPrivAction!requestPriv.ajax";
				
				$.ajax({
			  		type: "post",
			  		data : {
			  			"privilegeId" : $(this).data('id'),
						
					},
			  		url: requestPrivAjax,
	
			  		success: function(response) {
						// window.location.href = "";
						var dt = $('#accountRoleListTable').idtApi('getTableApi');
			  		 	dt.ajax.reload();
			  		 	$('#accountHistoryTable').DataTable().ajax.reload();	
			  		},
			  		error : function(error) {
			  			alert(error)
			  		}
				});			
			});
			
			
			//set date picker
			
			$(".date-picker").each(function() {
				$(this).datepicker({ 
					buttonImage: "/portal/images/brics/common/icon-cal.gif", 
					buttonImageOnly: true ,
					buttonText: "Select Date", 
					changeMonth: true,
					minDate: 0,
					changeYear: true,
					duration: "fast",
					gotoCurrent: true,
					hideIfNoPrevNext: true,
					showOn: "both",
					showAnim: "blind",
					yearRange: '-120:+5'
				});
			}).attr("readonly", "readonly");
			
			expirationDateInit();
			
		}    
	});
	
	//create commit changes button
	$('#commit-changes-button').click( function(e) {
		e.preventDefault();
		
		convertExpirationDatesToJSONWithoutChkBx();
		var action = 'editPermissionAction!submitPrivilegeRequest.action';
		var dt = $('#accountRoleListTable').idtApi('getTableApi');
        var data = dt.$('input, select').serialize();
        data += '&'+$("#expireDateJson").serialize();
        
 
        $.ajax({
      		type: "post",
      		data : data,
      		url: action,

      		success: function(response) {
      		 	dt.ajax.reload();		
      		 	$('#accountHistoryTable').DataTable().ajax.reload();	
      		},
      		error : function(error) {
      			alert(error);
      			console.log(error);
      		}
    	});	
        return false;
    } );
});

function addPermissionsLightbox() {
	var actionUrl = "permissionAction!addPermissionLightbox.ajax";
	
	$.ajax({
		type: "GET",
		url:actionUrl,
		data: {},
		success:function(data) {
			$("#addPermissionsInterface").html(data);
			
			var fileDialog = $("#addPermissionsDialog").dialog({
				title: "Add Permissions",
				height: 450,
				width: 800,
				buttons : [
					{
						id: "cancelBtn",
						text: "Cancel",
						click: function() {
							$(this).dialog('close');
						}
					},
					{
						id: "commitBtn",
						text: "Commit Changes",
						click: function() {
							submitPermissionsForm('editPermissionAction!submitPrivilegeRequest.action');
							$(this).dialog('close');							
						}
					}
				]
			});
			$("#addPermissionsDialog").dialog('open');			
		}	
	});
}

function submitPermissionsForm(action) {
	// Prepare all of the expiration date fields in the included accountPrivilegesInterface.jsp for submission.
	enableAllPrivileges();
	convertExpirationDatesToJSON();
	// Disable all buttons on the page.
	///$("input:button,button").prop("disabled", true);
	var data = $("#permissionsForm").serialize();
	 data += '&'+$("#expireDateJson").serialize();
	
	
	$.ajax({
  		type: "post",
  		data : data,
  		url: action,

  		success: function(response) {
			var dt = $('#accountRoleListTable').idtApi('getTableApi');
  		 	dt.ajax.reload();
  		 	$('#accountHistoryTable').DataTable().ajax.reload();	
  		},
  		error : function(error) {
  			alert(error)
  			console.log(error);
  		}
	});	
	
}

</script>