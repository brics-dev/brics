<%@include file="/common/taglibs.jsp"%>
<div id="accessReportsWrapper" class="idtTableContainer">
	<table id="accessRecordList" class="table table-striped table-bordered" width="100%"></table>
</div>
<script type="text/javascript">
	//Get the url param value
	function getURLParameter(name) {
	       return decodeURIComponent((new RegExp('[?|&]' + name + '=' + '([^&;]+?)(&|#|;|$)').exec(location.search) || [null, ''])[1].replace(/\+/g, '%20')) || null;
	}
	var studyId = getURLParameter('studyId');
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
		$("#accessRecordList").idtTable({
			idtUrl: "<s:url value='/study/accessRecordDownloadAction!searchReports.ajax' />",
			idtData: {
				primaryKey: "id"
			},
			dom : 'Bfrtip',
			serverSide: true,
			processing: true,
			pages:1,
			select: false,
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
			columns: [
				{
					title: "DATASET ID",
					data: "id",
					name: "id",
					parameter: "dataset.id"
				},
				{
					title: "NAME",
					data: "name",
					name: "name",
					parameter: "nameLink"
				},
				{
					title: "STATUS",
					data: "status",
					name: "status",
					parameter: "dataset.datasetStatus.name"
				},
				{
					title: "USER NAME",
					data: "username",
					name: "username",
					parameter: "userLink"
				},
				{
					title: "DATE",
					data: "date",
					name: "date",
					parameter: "queueDate",
					render: IdtActions.formatDate()
				},
				{
					title: "# OF REC",
					data: "recordCount",
					name: "recordCount",
					parameter: "recordCount",
					className: "nowrap"
				},
				{
					title: "DOWNLOAD LOC.",
					data: "source",
					name: "source",
					parameter: "dataSource.name"
				}
			],
			buttons: [
				{
					extend: "collection",
					title: '<s:property value="sessionStudy.study.prefixedId" />' + '_Access_Report_' + now,
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
			],
          	initComplete: function(){
          		var table = $("#accessRecordList");			          		

          		$('#accessRecordList_wrapper').find('.idt_searchContainer').mouseover(function(){

	          		$('.idt_selectColumnCheckbox').unbind().on('click', function(e) {
		  				searchDataTableColumns(table);
			  			
          		 	}); //end click
          		});//end mouseover
          		
          		$('#accessRecordList_wrapper').find(".idt_searchInput").unbind().on("keyup", _.debounce( function(e) {
	  				searchDataTableColumns(table);
		  		}, 100, true));

          	} //end intiComplete
		}); // end datatable
	});
</script>


<script type="text/javascript">	

	function viewAccount(accountId) {
		$.post(	"/portal/accounts/viewProfile!accountLightbox.ajax?accountId=" + accountId, 
			{ }, 
			function (data) {
				$.fancybox(data);
			}
		);
	}
	
	function searchDataTableColumns(table) {
		var oTable = table.idtApi('getTableApi')
		oTable.clearPipeline().draw();		
	}
</script>