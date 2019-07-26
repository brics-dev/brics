<%@include file="/common/taglibs.jsp"%>
<title>Create PROMIS Form Structure</title>


	<div class="border-wrapper">
		<jsp:include page="../navigation/dataDictionaryNavigation.jsp" />
		<h1 class="float-left">(Admin) Export Form Structure and Data Elements of HealthMeasures Forms</h1>
		<div style="clear:both"></div>
		<div id="main-content">
			<h2>HealthMeasures Form List</h2>
			<div class="idtTableContainer">
				<table id="tableTest"  class="table table-striped table-bordered" width="100%"></table>
			</div>
			<div class="clear-float"></div>	
		</div>
	</div>

<script type="text/javascript">
	var baseUrl = '<s:property value="modulesDDTURL"/>';
	
	setNavigation({
		"bodyClass" : "primary",
		"navigationLinkID" : "dataDictionaryModuleLink",
		"subnavigationLinkID" : "defineDataToolsLink",
		"tertiaryLinkID" : "createPromisFormStructureLink"
	});
	
	$(document).ready(function() {
		  $("#tableTest").idtTable({
	          idtUrl: baseUrl+'dictionaryAdmin/promisDataStructureAction!adminListNonexistentPromisFormsTable.action',
	          idtData: {
	              primaryKey: "OID"
	          },
	          pages: 1,
	          "processing": false,
	          "serverSide": false,
	          length: 10,
	          "columns": [
        	  	{
	                data: 'Name',
	                title: 'Form Name',
	                parameter : '',
	                searchable: true,
	                orderable: true,
	                name: "name"
	            },  
	            {
	                data: 'type',
	                title:'Type',
	                parameter : '',
	                name: "type"
	            },
	        	{
	                data: 'OID',
	                title:'OID',
	                parameter : '',
	                name: "OID"
	            }
	          ],
				bFilter: true
	  });
	});
</script>