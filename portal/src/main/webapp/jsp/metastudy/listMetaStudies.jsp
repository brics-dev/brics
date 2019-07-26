<%@include file="/common/taglibs.jsp"%>

<title>View Meta Studies</title>

<!-- begin .border-wrapper -->
<div class="border-wrapper wide">
	<jsp:include page="../navigation/metaStudyNavigation.jsp" />
	<h1 class="float-left">Meta Study</h1>
	<div style="clear:both"></div>
	
	<!--begin #center-content -->
	<div id="main-content">
		<h2>Meta Study Overview</h2>
		<p>
		A Meta Study contains findings from other studies that could be used by researchers to conduct additional analysis.  The information within the Meta Study can be used in publications and is accessible by clicking on the name of the Meta Study from the list below.
		</p>
		
		<h2>View Meta Studies</h2>
		<div id="metaStudyListContainer" class="idtTableContainer">
			<table id="metaStudyListTable" class="table table-striped table-bordered" width="100%"></table>
		</div>	
	
		<div class="clear-float">
		</div>
		<!-- end of #main-content -->
	</div>
</div>
<!-- end of .border-wrapper -->

<script type="text/javascript">
$(document).ready(function() {
	$("#metaStudyListTable").idtTable({
		idtUrl: "<s:url value='/metastudy/metaStudyListAction!getMetaStudyTableList.action' />",
		autoWidth: false,
		columns: [
			{
                "data": 'title',
                "title":'Title',
                "name":'Title',
                "parameter" : 'titleLink',
                "width": '55%',
                "searchable": true,
                "orderable": true
			},
			{
                "data": 'prefixId',
                "title":'Meta Study Id',
                "name":'Meta Study Id',
                "parameter" : 'prefixId',
                "width": '15%',
                "searchable": true,
                "orderable": true
			},
			{
                "data": 'permissionName',
                "title":'Permissions',
                "name":'Permissions',
                "parameter" : 'permissionName',
                "width": '15%',
                "searchable": true,
                "orderable": true
			},
			{
                "data": 'statusText',
                "title":'Status',
                "name":'Status',
                "parameter" : 'statusText',
                "width": '15%',
                "searchable": true,
                "orderable": true
			}
		]
	});
});
	setNavigation({"bodyClass":"primary", "navigationLinkID":"metaStudyModuleLink", "subnavigationLinkID":"metaStudyLink", "tertiaryLinkID":"browseMetaStudyLink"});
</script>
