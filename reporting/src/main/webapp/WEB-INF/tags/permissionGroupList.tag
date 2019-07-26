<%@ tag body-content="scriptless"%>
<%@ attribute name="elementList" type="java.util.Collection" required="true"%>

<%@taglib prefix="s" uri="/struts-tags"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn"%>
<%@taglib prefix="ndar" tagdir="/WEB-INF/tags"%>

<div id="permissionsGroupContainer" class="idtTableContainer">
	<div id="dialog"></div>
	<table id="permissionsGroupTable" class="table table-striped table-bordered" width="100%"></table>
</div>

<script type="text/javascript">
$(document).ready(function() {
	$('#permissionsGroupTable').idtTable({
		"columns": [
			{
				"data": "name",
				"title": "NAME",
				"name": "NAME"
			},
			{
				"data": "description",
				"title": "DESCRIPTION",
				"name": "DESCRIPTION",
				"parameter": "description"
			}
		],
		"data": [
			<c:forEach var="permissionGroup" items="${elementList}">
			<c:if
				test="${permissionGroup.groupName != 'Public Study' && permissionGroup.groupName != 'Public Data Elements'
				 	&& permissionGroup.groupName != 'Published Form Structures'}">
				 {
					"name": "<a href='permissionGroupAction!edit.action?permissionGroupId=${permissionGroup.id}'>${permissionGroup.groupName}</a>",
					"description": "${permissionGroup.groupDescription}"
				 },
			</c:if>
			</c:forEach>
		]
	})
})
</script>
