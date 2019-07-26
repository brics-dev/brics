<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn"%>
<!DOCTYPE html>
<%-- <%@include file="/common/taglibs.jsp"%>  --%> 
<html>
	<head>
		Reporting tool
		<meta charset="utf-8" />
		<title>View Reports</title>

		<link type="text/css" href="css/jquery-ui.css" rel="stylesheet" media="all" />
		<!--  <link type="text/css" href="css/jquery.dataTables.css" rel="stylesheet" media="all" /> -->
		<link type="text/css" href="css/jquery.ibisMessaging-0.1.css" rel="stylesheet" />
		<link type="text/css" href="instances/pdbp/jquery-ui.theme.min.css" rel="stylesheet" />
		
		<link type="text/css" href="css/font-awesome-4.6.3/css/font-awesome.min.css" rel="stylesheet" />
		<link type="text/css" href="css/qt-glyphicons.css" rel="stylesheet" />
		<link type="text/css" href="css/brics-qt-glyphicons.css" rel="stylesheet" />
		<!-- <link type="text/css" href="css/qt-structure.css" rel="stylesheet" />
		<link type="text/css" href="css/qt-header.css" rel="stylesheet" />
		<link type="text/css" href="css/qt-general.css" rel="stylesheet" /> -->
		<link type="text/css" href="css/jquery/datatables.css" rel="stylesheet" />
		<!--  change instance folder name for different styles -->
		<c:if test="${fn:contains(pageContext.request.requestURL, 'localhost')}"> 
		<link type="text/css" href="instances/default/qt-footer.css" rel="stylesheet" /> 
			<link type="text/css" href="css/scss/instances/default/style.css" rel="stylesheet" />
			<!-- Favicon -->
			<link href="css/scss/instances/default/images/favicon.ico" rel="icon" />
		</c:if>
		<c:if test="${fn:contains(pageContext.request.requestURL, 'pdbp')}">  
		<link type="text/css" href="instances/pdbp/qt-footer.css" rel="stylesheet" />
			<link type="text/css" href="css/scss/instances/pdbp/style.css" rel="stylesheet" />
			<!-- Favicon -->
			<link href="css/scss/instances/pdbp/images/favicon.ico" rel="icon" />
		</c:if>
		<c:if test="${fn:contains(pageContext.request.requestURL, 'cistar')}"> 
		<link type="text/css" href="instances/cistar/qt-footer.css" rel="stylesheet" />
			<link type="text/css" href="css/scss/instances/cistar/style.css" rel="stylesheet" />
			<!-- Favicon -->
			<link href="css/scss/instances/cistar/images/favicon.ico" rel="icon" />
		</c:if>
		<c:if test="${fn:contains(pageContext.request.requestURL, 'cnrm')}">
		<link type="text/css" href="instances/cnrm/qt-footer.css" rel="stylesheet" />
			<link type="text/css" href="css/scss/instances/cnrm/style.css" rel="stylesheet" />
			<!-- Favicon -->
			<link href="css/scss/instances/cnrm/images/favicon.ico" rel="icon" />
		</c:if>  
		<c:if test="${fn:contains(pageContext.request.requestURL, 'eyegene')}">  
		<link type="text/css" href="instances/eyegene/qt-footer.css" rel="stylesheet" />
			<link type="text/css" href="css/scss/instances/eyegene/style.css" rel="stylesheet" />
			<!-- Favicon -->
			<link href="css/scss/instances/eyegene/images/favicon.ico" rel="icon" />
		</c:if>
		<c:if test="${fn:contains(pageContext.request.requestURL, 'cdrns')}">  
		<link type="text/css" href="instances/cdrns/qt-footer.css"rel="stylesheet" />
			<link type="text/css" href="css/scss/instances/cdrns/style.css" rel="stylesheet" />
			<!-- Favicon -->
			<link href="css/scss/instances/cdrns/images/favicon.ico" rel="icon" />
		</c:if>
		<c:if test="${fn:contains(pageContext.request.requestURL, 'fitbir')}"> 
		<link type="text/css" href="instances/fitbir/qt-footer.css" rel="stylesheet" />
			<link type="text/css" href="css/scss/instances/fitbir/style.css" rel="stylesheet" />
			<!-- Favicon -->
			<link href="css/scss/instances/fitbir/images/favicon.ico" rel="icon" />
		</c:if> 
		<c:if test="${fn:contains(pageContext.request.requestURL, 'brics')}">
		    <link type="text/css" href="instances/cistar/qt-footer.css" rel="stylesheet" />
			<link type="text/css" href="css/scss/instances/cistar/style.css" rel="stylesheet" />
			<!-- Favicon -->
			<link href="css/scss/instances/cistar/images/favicon.ico" rel="icon" />

		</c:if>
		<!-- <link rel="stylesheet" href="http://codemirror.net/lib/codemirror.css" type="text/css" /> -->
		<link rel="stylesheet" href="css/codemirror.css" type="text/css" />
		

		<!-- All library imports -->

		<!-- <script src="https://code.jquery.com/jquery-1.7.2.min.js" 
			integrity="sha256-R7aNzoy2gFrVs+pNJ6+SokH04ppcEqJ0yFLkNGoFALQ=" 
			crossorigin="anonymous"></script> -->
		<script src="https://cdnjs.cloudflare.com/ajax/libs/jquery/1.7.2/jquery.min.js" crossorigin="anonymous"></script>
		<script type="text/javascript" src="js/lib/jquery-ui.min.js"></script>
		<script type="text/javascript" src="js/lib/jquery.scrollTo.js"></script>
		
		<script type="text/javascript" src="js/lib/jquery.ibisMessaging-0.1.full.js"></script>
		<script type="text/javascript" src="js/lib/core_libs.min.js"></script>
		
		<!-- development only!!! -->
		<!-- <script type="text/javascript" src="js/lib/development/backbone.js"></script> -->
		
		<script type="text/javascript" src="js/lib/core_classes.min.js"></script>
		<script type="text/javascript" src="js/lib/jquery.dataTables.js"></script> 
		
		<!--  Linkify  -->
		
		<script type="text/javascript" charset="utf-8" language="javascript" src="js/lib/linkify.min.js"></script>
		<script type="text/javascript" charset="utf-8" language="javascript" src="js/lib/linkify-string.min.js"></script>
		<script type="text/javascript" charset="utf-8" language="javascript" src="js/lib/linkify-jquery.min.js"></script>
		
		<!-- Codemirror libraries for Rbox script input -->
		<script type="text/javascript" src="js/lib/codemirror/codemirror.min.js"></script>
		<!-- <script src="http://codemirror.net/lib/codemirror.js" type="text/javascript"></script>
		<script src="http://codemirror.net/lib/util/runmode.js" type="text/javascript"></script>
		<script src="http://codemirror.net/mode/javascript/javascript.js" type="text/javascript"></script> -->
		

		<jsp:include page='/Config.jsp' />
		
	
		
		
			<!-- Templates -->
		<jsp:include page='/template/main/pageMain.jsp' />
	</head>
	<body>

		<footer id="footer" class="">
		
		
		
		<!--  change instance folder name for different styles -->
		<c:if test="${fn:contains(pageContext.request.requestURL, 'localhost')}"> 
			<jsp:include page='/instances/default/footer.jsp' />
		</c:if>
		<c:if test="${fn:contains(pageContext.request.requestURL, 'pdbp')}">  
			<jsp:include page='/instances/pdbp/footer.jsp' />
		</c:if>
		<c:if test="${fn:contains(pageContext.request.requestURL, 'cistar')}"> 
		
		</c:if>
		<c:if test="${fn:contains(pageContext.request.requestURL, 'cnrm')}">
			<jsp:include page='/instances/cnrm/footer.jsp' />
		</c:if>  
		<c:if test="${fn:contains(pageContext.request.requestURL, 'eyegene')}">  
			<jsp:include page='/instances/eyegene/footer.jsp' />
		</c:if>
		<c:if test="${fn:contains(pageContext.request.requestURL, 'cdrns')}">  
			<jsp:include page='/instances/cdrns/footer.jsp' />
		</c:if>
		<c:if test="${fn:contains(pageContext.request.requestURL, 'fitbir')}"> 
			<jsp:include page='/instances/fitbir/footer.jsp' />
		</c:if> 
		<div class="build-notes clear-both line">
			<p class="right">Build Version:<span id="deploymentVersionContainer"></span></p>
		</div>
		
		</footer>
	</body>
	

</html>