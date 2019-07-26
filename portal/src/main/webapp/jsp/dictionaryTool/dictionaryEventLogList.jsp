<%@include file="/common/taglibs.jsp"%>
<%@taglib prefix="ndar" tagdir="/WEB-INF/tags"%>

<div id="eventContainer" class="idtTableContainer">
	<table id="eventTable" class="table table-striped table-bordered" width="100%"></table>
</div>
<script type="text/javascript">
$(document).ready(function() {
	$("#eventTable").idtTable({
		//since we are using documentation table in different places, we decided to create separate jsp file for it 
		//and just change the param value when we include it"
		idtUrl: "<c:out value='${param.idtUrl}'/>",
		autoWidth: false,
		columns: [
			{
				title: "Date",
				data: "date",
				name: "Date",
				parameter: "createTime",
				width: "12%",
				render: IdtActions.formatDate()
			},
			{
				title: "User",
				data: "user",
				name: "User",
				parameter: "user",
				width: "12%"
			},
			{
				title: "Action Taken",
				data: "actionTaken",
				name: "Action Taken",
				parameter: "actionTaken",
				width: "20%"
			},
			{
				title: "Reason Given",
				data: "comment",
				name: "Reason Given",
				parameter: "comment",
				width: "34%",
				render: IdtActions.ellipsis(55)
			},
			{
				title: "Attachments",
				data: "docNameLink",
				name: "Attachments",
				parameter: "docNameLink",
				width: "22%"
			}			
		]
	})
});
</script>