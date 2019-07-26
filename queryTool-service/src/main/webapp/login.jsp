<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn"%>
<!DOCTYPE html>
<%-- <%@include file="/common/taglibs.jsp"%>  --%> 
<html>
	<head>
		<meta charset="utf-8" />
		<title>Query Tool</title>

		<!--  change instance folder name for different styles -->
		<c:if test="${fn:contains(applicationConstants.styleKey, 'localhost')}">  
			<link type="text/css" href="css/scss/instances/default/style.css" rel="stylesheet" />
		</c:if>
		<c:if test="${fn:contains(applicationConstants.styleKey, 'pdbp')}">  
			<link type="text/css" href="css/scss/instances/pdbp/style.css" rel="stylesheet" />
		</c:if>
		<c:if test="${fn:contains(applicationConstants.styleKey, 'cistar')}"> 
			<link type="text/css" href="css/scss/instances/cistar/style.css" rel="stylesheet" />
		</c:if>
		<c:if test="${fn:contains(applicationConstants.styleKey, 'cnrm')}">
			<link type="text/css" href="css/scss/instances/cnrm/style.css" rel="stylesheet" />
		</c:if>  
		<c:if test="${fn:contains(applicationConstants.styleKey, 'eyegene') || fn:contains(applicationConstants.styleKey, 'nei')}">  
			<link type="text/css" href="css/scss/instances/eyegene/style.css" rel="stylesheet" />
		</c:if>
		<c:if test="${fn:contains(applicationConstants.styleKey, 'fitbir')}"> 
			<link type="text/css" href="css/scss/instances/fitbir/style.css" rel="stylesheet" />
		</c:if> 
		<c:if test="${fn:contains(applicationConstants.styleKey, 'ninds')}">  
			<link type="text/css" href="css/scss/instances/ninds/style.css" rel="stylesheet" />
		</c:if>
		<c:if test="${fn:contains(applicationConstants.styleKey, 'gsdr')}">  
			<link type="text/css" href="css/scss/instances/gsdr/style.css" rel="stylesheet" />
		</c:if> 
		<!--  <script type="text/javascript" src='<s:url value="/js/browser-check.js"/>'></script>  -->
		<link type="text/css" href="instances/default/qt-footer.css"rel="stylesheet" />

		<script type="text/javascript" src="js/QueryTool.js"></script>
		<!-- All library imports -->

		<script src="https://code.jquery.com/jquery-1.7.2.min.js" 
			integrity="sha256-R7aNzoy2gFrVs+pNJ6+SokH04ppcEqJ0yFLkNGoFALQ=" 
			crossorigin="anonymous"></script>
		<script type="text/javascript" src="js/lib/jquery-ui.min.js"></script>
		<script type="text/javascript" src="js/lib/jquery.scrollTo.js"></script>
		
		<script type="text/javascript" src="js/lib/jquery.ibisMessaging-0.1.full.js"></script>
		<script type="text/javascript" src="js/lib/core_libs.min.js"></script>
		
		<!-- development only!!! -->
		<!-- <script type="text/javascript" src="js/lib/development/backbone.js"></script> -->
		
		<script type="text/javascript" src="js/lib/core_classes.min.js"></script>
		
		<!-- development only!!!! -->
		<script type="text/javascript" src="js/lib/development/BaseView.js"></script>

		<jsp:include page='/Config.jsp' />
	
		
		<!-- All global website imports here -->
		<script type="text/javascript" src="js/models/Login.js"></script>
		<script type="text/javascript" src="js/views/LoginView.js"></script>
		<!-- All application code below here -->
		
		<!-- Templates -->
		<jsp:include page='/templates/main/login.jsp' />
		<jsp:include page='/templates/processing.jsp' />
		
	</head>
	<body>
		<div id="loginContainer" class="container-fluid">
		
		</div>
		<footer id="footer" class="">
		<jsp:include page='/instances/pdbp/footer.jsp' />
		</footer>
	</body>
	
	<script type="text/javascript">
	<!-- bootstrap - startup -->
	$(document).ready(function() {
		var login = new QT.Login();
		var loginView = new QT.LoginView({model: login});
		
		loginView.render();
	});
	</script>
</html>