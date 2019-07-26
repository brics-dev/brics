<%@include file="/common/taglibs.jsp"%>

<s:if test="!studyList.size==0">
	<div id="studyListTable" class="idtTableContainer">
	   	<div id="dialog"></div>
		<table id="studyListTableTable" class="table table-striped table-bordered" width="100%"></table>
	</div>
	<script type="text/javascript">
		$(document).ready(function() {
			$('#studyListTableTable').idtTable({
				idtUrl: "<s:url value='/study/studyAction!getStudyTableList.action' />",
				pageLength: 15,
				"autoWidth": false,
				"columns": [
					<s:if test="inAdmin">
						{
							 "data": "studyAdminLink",
			                 "title": "TITLE",
			                 "name": "TITLE",
			                 "parameter" : "studyAdminLink",
			                 "searchable": true,
			                 "orderable": true,
			                 "width": '55%'
						},
					</s:if>
					<s:else>
						{
							 "data": 'studyNoAdminLink',
			                 "title": "TITLE",
			                 "name": "TITLE",
			                 "parameter" : 'studyNoAdminLink',
			                 "searchable": true,
			                 "orderable": true,
			                 "width": '55%'
						},
					</s:else>
					{
						 "data": "prefixedId",
		                 "title": "STUDY ID",
		                 "name": "STUDY ID",
		                 "parameter" : "prefixedId",
		                 "class": "nobreak",
		                 "searchable": true,
		                 "orderable": true,
		                 "width": '10%'
					},
					{
						 "data": "principalInvestigator",
		                 "title": "PI",
		                 "name": "PI",
		                 "parameter" : "principalInvestigator",
		                 "searchable": true,
		                 "orderable": true,
		                 "width": '10%'
					},
					<s:if test="!inAdmin">
						{
							 "data": "dataTypes",
			                 "title": "DATA TYPES",
			                 "name": "DATA TYPES",
			                 "parameter" : "dataTypes",
			                 "class": "nobreak",
			                 "searchable": true,
			                 "orderable": true,
			                 "width": '10%'
			                	 
			                 
						},
						{
							 "data": "permission",
			                 "title": "PERMISSION",
			                 "name": "PERMISSION",
			                 "parameter" : "permission",
			                 "searchable": true,
			                 "orderable": true,
			                 "width": '10%'
						},						
					</s:if>
					<s:else>
						{
							 "data": "owner",
			                 "title": "OWNER",
			                 "name": "OWNER",
			                 "parameter" : "owner",
			                 "searchable": true,
			                 "orderable": true,
			                 "width": '10%'
						},
						{
							 "data": "status",
			                 "title": "STATUS",
			                 "name": "STATUS",
			                 "parameter" : "status",
			                 "searchable": true,
			                 "orderable": true,
			                 "width": '10%'
						},
						{
							 "data": "dateCreated",
			                 "title": "REQUEST DATE",
			                 "name": "REQUEST DATE",
			                 "parameter" : "dateCreated",
			                 "searchable": true,
			                 "orderable": true,
			                 "width": '10%',
			                 "render": IdtActions.formatDate()
						}
					</s:else>
				],
				<s:if test="!inAdmin">
				filters: [
					{
						type: 'select',
						name: 'All data types',
						options: [
							{
								value: 'clinical assessment',
								label: 'Clinical Assessment',
							
							}, 
							{
								value: 'imaging',
								label: 'Imaging'
							}, 
							{
								value: 'genomics',
								label: 'Genomics'
							}
						],
						columnIndex: 3
					},
					{
						type: 'select',
						name: 'All studies',
						options: [
							{
								value: 'no data',
								label: 'Only studies with data'
							}
						],
						test: function(oSettings, aData, iDataIndex, filterData) {
							var dataTypeNone = aData[3].indexOf("no data") > -1;
							if(filterData['All studies'] == aData[3]) {
								return !dataTypeNone;
							}else {
								return true;
							}							
						}					
					},
					{
						type: 'select',
						name: 'Ownership: all',
						options: [
							{
								value: 'owner',
								label: 'Ownership: mine'
							}
						],
						columnIndex: 4
					}
				]
				</s:if>
			})
		})
	</script>
</s:if>
<s:else>
	<div class="display-error">
		<br />
		<p>No results were found.</p>
	</div>
</s:else>

