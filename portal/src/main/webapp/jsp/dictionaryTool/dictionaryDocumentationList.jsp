<%@include file="/common/taglibs.jsp"%>

<div id="documentationListContainer" class="idtTableContainer">
	<table id="documentationListTable" class="table table-striped table-bordered" width="100%"></table>			
</div>
<script type="text/javascript">
	$(document).ready(function() {
		$("#documentationListTable").idtTable({
			//since we are using documentation table in different places, we decided to create separate jsp file for it 
			//and just change the param value when we include it, example of creating the param: "<c:out value='${param.idtUrl}'/>"
			idtUrl: "<c:out value='${param.idtUrl}'/>",
			autoWidth: false,
			columns: [
				{
					title: "Name",
					data: "docNameLink",
					name: "Name",
					parameter: "docNameLink",
					width: "20%"
				},
				{
					title: "Type",
					data: "fileType",
					name: "Type",
					parameter: "fileType.name",
					width: "20%"
				},
				{
					title: "Description",
					data: "description",
					name: "Description",
					parameter: "description",
					width: "40%",
					render: IdtActions.ellipsis(75)
				},
				{
					title: "Date Uploaded",
					data: "dateCreated",
					name: "Date Uploaded",
					parameter: "dateCreated",
					width: "20%",
					render: IdtActions.formatDate()
				
				}
			]
		});
	});
</script>