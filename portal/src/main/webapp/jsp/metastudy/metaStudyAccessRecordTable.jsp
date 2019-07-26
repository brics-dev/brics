<%@include file="/common/taglibs.jsp"%>

<div class="filter-results clear-float">
	<div id="metaStudyAccessReportsWrapper" class="idtTableContainer">
		<table id="metaStudyAccessRecordList" class="table table-striped table-bordered" width="100%"></table>
	</div>
	<br />
	<h3>Quick Stats</h3>
	
	<div class="form-output"><b>Total Number of Data File Downloads:
		<s:property value="dataFileDownloads" /></b>
	</div>
	<div class="form-output"><b>Total Number of Documentation File Downloads:
		<s:property value="documentFileDownloads" /></b>
	</div>
	<div class="form-output"><b>Total Number of Downloads:
			<s:property value="totalDownloads" /></b>
	</div>
	<div class=form-output><b>Total Number of MB:
		<s:property value="totalDownloadSize" /></b>
	</div>	
</div>

<script type="text/javascript">
	//reformat the Date to YYYYMMDD
	 function getValue(value) {
         return (value < 10) ? "0" + value : value;
     };
     function getDate () {
         var newDate = new Date();

         var sMonth = getValue(newDate.getMonth() + 1);
         var sDay = getValue(newDate.getDate());
         var sYear = newDate.getFullYear();

         return sYear + sMonth + sDay;
     }
     var now = getDate();
	$(document).ready(function() {
		$("#metaStudyAccessRecordList").idtTable({
			idtUrl: "<s:url value='/metastudy/metaStudyAccessRecordDownloadAction!searchReportsList.ajax' />",
			idtData: {
				primaryKey: "id"
			},
			dom: 'Bfrtip',
			serverSide: true,
			processing: true,
			pages: 1,
			select: false,
			columns: [
				{
					title: "DOI",
					data: "doi",
					name: "doi",
					parameter: "doi"
				},
				{
					title: "File Name",
					data: "fileName",
					name: "fileName",
					parameter: "fileName"
				},
				{
					title: "User Name",
					data: "userName",
					name: "userName",
					parameter: "account.userName"
				},
				{
					title: "Download Date",
					data: "dateCreated",
					name: "dateCreated",
					parameter: "dateCreated",
					render: IdtActions.formatDate()
				}
			],
			filters: [
				{
					type: "select",
					name: "Time Filter: all",
					columnIndex: 0,
					options: [
						{
							value: "lastWeek",
							label: "Time Filter: Past Week"
						},
						{
							value: "lastMonth",
							label: "Time Filter: Past Month"
						},
						{
							value: "lastYear",
							label: "Time Filter: Past Year"
						}
					]
				}
			],
			buttons: [
				{
					extend: "collection",
					title: '<s:property value="sessionMetaStudy.metaStudy.prefixId" />'+'_Access_Report_'+now,
					buttons: [
						{
							extend: 'csv',
							text: 'csv',
							className: 'btn btn-xs btn-primary p-5 m-0 width-35 assets-export-btn  buttons-csv',
							extension: '.csv',
							name: 'csv',
							exportOptions: {
								orthogonal: 'export'
							},
							enabled: true,
							action: IdtActions.exportAction()
							
						}
					]
				}
			]
		});
	});
</script>