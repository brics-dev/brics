<%@include file="/common/taglibs.jsp"%>
<div class="margin-top-lg">
<h3>Permission Group</h3>
	<div id="permissionGroupMemberListContainer" class="idtTableContainer">
		<div id="dialog"></div>
		<table id="permissionGroupMemberListTable" class="table table-striped table-bordered" width="100%"></table>
	</div> 
</div>

<script>

$(document).ready(function() {
	$('#permissionGroupMemberListTable').idtTable({
		idtUrl: "<c:out value='${param.idtUrl}'/>",
		autoWidth: false,
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
	});
});

</script>