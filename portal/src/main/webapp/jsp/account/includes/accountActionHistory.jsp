<%@include file="/common/taglibs.jsp"%>

<div class="margin-top-lg">
	<h3>Account Action History</h3>
	<br>

	<p>This is a record of any action taken by the user or System Administrator. Please contact <a href="mailto:<s:property value="%{orgEmail}" />">${orgEmail}</a> with any questions.</p>

	
	<div id="accountHistoryContainer" class="idtTableContainer">
		<div id="dialog"></div>
		<table id="accountHistoryTable" class="table table-striped table-bordered"></table>
	</div>
</div>


<script type="text/javascript">
	$(document).ready(function(){
		$('#accountHistoryTable').idtTable({
			idtUrl: "<c:out value='${param.idtUrl}'/>",
			autoWidth: false,
			order: [[ 2, "desc" ]],
			"columns" : [{
				"data" : "actionType",
				"title" : "ACTION TYPE",
				"name" : "Action Type",
				"parameter": "actionType",
				"width": "45%"
			},{
				"data" : "comment",
				"title" : "COMMENT",
				"name" : "Comment",
				"parameter": "comment",
				"render": IdtActions.ellipsis(255),
				"width": "45%"
			},{
				"data" : "actionTime",
				"title" : "",
				"name" : "Date Made",
				"parameter": "actionTime",
				"width": "10%",
				"visible": false
			},{
				"data" : "actionDate",
				"title" : "DATE MADE",
				"name" : "Date Made",
				"parameter": "actionDate",
				"width": "10%"
			}]
		})
	});
</script>