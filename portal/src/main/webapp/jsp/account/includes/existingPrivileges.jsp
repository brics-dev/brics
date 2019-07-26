<%@include file="/common/taglibs.jsp"%>
<c:if test="${currentAccount.id != null && (not empty currentAccount.accountRoleList || not empty currentAccount.permissionGroupMemberList)}">
	<div class="margin-top-lg">
		<h3>Account Privileges</h3>
		<div id="aaccoutRoleListContainer" class="idtTableContainer">
			<div id="dialog"></div>
			<table id="accountRoleListTable" class="table table-striped table-bordered" width="100%"></table>
		</div>			
	</div>
	<div class="margin-top-lg">
		<h3>Permission Group</h3>
		<div id="permissionGroupMemberListContainer" class="idtTableContainer">
			<div id="dialog"></div>
			<table id="permissionGroupMemberListTable" class="table table-striped table-bordered" width="100%"></table>
		</div>
	</div>
</c:if>
<script type="text/javascript">
$(document).ready(function() {
	$('#accountRoleListTable').idtTable({
		idtUrl: "<s:url value='/accounts/viewProfile!getExistingAccountRoleLists.action' />",
		"columns": [
			{
				"data": "privilege",
				"title": "PRIVILEGE",
				"name": "PRIVILEGE",
				"parameter": "decoratedPrivilege",

			},
			{
				"data": "roleStatus",
				"title": "STATUS",
				"name": "STATUS",
				"parameter": "decoratedStatus"
			}, 
			{
				"data": "expirationDate",
				"title": "EXPIRATION DATE",
				"name": "EXPIRATION DATE",
				"parameter": "decoratedExpirationDate"
			}
		],
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
		}    
	});
	
	$('#permissionGroupMemberListTable').idtTable({
		idtUrl: "<s:url value='/accounts/viewProfile!getPermissionGroupMembers.action' />",
		"columns": [
			{
				"data": "privilege",
				"title": "PRIVILEGE",
				"name": "PRIVILEGE",
				"parameter": "decoratedPermissionGroupName",

			},
			{
				"data": "roleStatus",
				"title": "STATUS",
				"name": "STATUS",
				"parameter": "decoratedPermissionGroupStatus"
			}
		]
	})
	
})
</script>
