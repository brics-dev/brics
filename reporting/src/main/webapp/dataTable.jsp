<!DOCTYPE html>
<%-- <%@include file="/common/taglibs.jsp"%>  --%> 
<html>
	<head>
		<meta charset="utf-8" />
		<title>Insert title here</title>

		<link type="text/css" href="css/jquery-ui.css" rel="stylesheet" media="all" />
		<link type="text/css" href="css/jquery.ibisMessaging-0.1.css" rel="stylesheet" />
		
		<link type="text/css" href="css/instances/pdbp/jquery-ui.theme.min.css" rel="stylesheet" />
		
		<link type="text/css" href="css/qt-glyphicons.css" rel="stylesheet" />
		<link type="text/css" href="css/qt-structure.css" rel="stylesheet" />
		<link type="text/css" href="css/qt-header.css" rel="stylesheet" />
		<link type="text/css" href="css/qt-general.css" rel="stylesheet" />

		<!-- namespace setup -->
		<script type="text/javascript" src="js/QueryTool.js"></script>
		<script type="text/javascript" src="js/QueryToolDataTable.js"></script>

		<!-- All library imports -->

		<script type="text/javascript" src="https://ajax.googleapis.com/ajax/libs/jquery/1.7.2/jquery.min.js"></script>
		<script type="text/javascript" src="js/lib/jquery-ui.min.js"></script>
		<script type="text/javascript" src="js/lib/jquery.scrollTo.js"></script>
		
		<script type="text/javascript" src="js/lib/jquery.ibisMessaging-0.1.full.js"></script>
		<script type="text/javascript" src="js/lib/core_libs.min.js"></script>
		<script type="text/javascript" src="js/lib/core_classes.min.js"></script>
		
		<!-- development only!!!! -->
		<!-- <script type="text/javascript" src="js/lib/development/BaseView.js"></script> -->
		<!-- end development only -->

		
		
		<!-- All global website imports here -->
		<jsp:include page='/Config.jsp' />
		
		<!-- All application code below here -->
			<!-- Util -->
				<script type="text/javascript" src="js/util/Dialog.js"></script>
				<script type="text/javascript" src="js/util/Filters.js"></script>
		
			<!-- Models -->
				<script type="text/javascript" src="js/dataTable/models/Cell.js"></script>
				<script type="text/javascript" src="js/dataTable/models/Col.js"></script>
				<script type="text/javascript" src="js/dataTable/models/DataTable.js"></script>
				<script type="text/javascript" src="js/dataTable/models/Row.js"></script>
				<script type="text/javascript" src="js/dataTable/models/Hamburger.js"></script>
				
				
		
			<!-- Collections -->
			
				<script type="text/javascript" src="js/dataTable/collections/ColCells.js"></script>
				<script type="text/javascript" src="js/dataTable/collections/Cols.js"></script>
				<script type="text/javascript" src="js/dataTable/collections/DataTables.js"></script>
				<script type="text/javascript" src="js/dataTable/collections/RowCells.js"></script>
				<script type="text/javascript" src="js/dataTable/collections/Rows.js"></script>
				<script type="text/javascript" src="js/dataTable/collections/Hamburgers.js"></script>
				
		
			<!-- Views -->
			<script type="text/javascript" src="js/dataTable/views/HamburgerView.js"></script>
			<script type="text/javascript" src="js/dataTable/views/DataTableView.js"></script>
				
				
				
				
		<!-- Bootstrap -->
		<script type="text/javascript" src="js/QtBootstrap.js"></script>
		<script type="text/javascript" src="js/QtDataTableBootstrap.js"></script>
		
		
		
		<!-- Templates -->
		<jsp:include page='/template/dataTable/dataTableTemplate.jsp' />
		
		
	</head>
	<body>
		<div id="resultsDatatableTwo" class="container-fluid">
		
		</div>
	</body>
	
	<script type="text/javascript">
	
	<!-- bootstrap - startup -->
	$(document).ready(function() {
		QueryTool.render();
		QueryToolDataTable.render();
	});
	</script>
</html>