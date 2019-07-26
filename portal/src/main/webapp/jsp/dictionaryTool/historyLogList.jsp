<%@include file="/common/taglibs.jsp"%>
<%@taglib prefix="ndar" tagdir="/WEB-INF/tags"%>
					
<div id="historyLogContainer" class="idtTableContainer">
	<table id="historyLogTable" class="table table-striped table-bordered" width="100%"></table>
</div>
<script type="text/javascript">
$(document).ready(function() {
	$("#historyLogTable").idtTable({
		//since we are using documentation table in different places, we decided to create separate jsp file for it 
		//and just change the param value when we include it"
		idtUrl: "<c:out value='${param.idtUrl}'/>",
		autoWidth: false,
		"order": [[ 0, "desc" ]],
		columns: [
			{
				title: "Date",
				data: "date",
				name: "Date",
				parameter: "createTime",
				width: "20%",
				render: IdtActions.formatDate()
			},
			{
				title: "Author",
				data: "user",
				name: "Author",
				parameter: "user",
				width: "20%"
			},
			{
				title: "Change Made",
				data: "changeMade",
				name: "Change Made",
				parameter: "changeMade",
				width: "25%",
				render: IdtActions.ellipsis(55)
			},
			{
				title: "Reason Details",
				data: "comment",
				name: "Reason Details",
				parameter: "comment",
				width: "35%",
				render: IdtActions.ellipsis(55)
			}
		]
	})
});
</script>