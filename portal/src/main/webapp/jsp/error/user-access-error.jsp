<%@ include file="/common/taglibs.jsp"%>
<h2>Access Denied</h2>

<div class="clear-float">
	<h1 class="float-left">Error</h1>
</div>

<!-- begin .border-wrapper -->
<div class="border-wrapper wide">
	<!--begin #center-content -->
	<div id="main-content">
		<div class="clear-float">
			<div id="errorMessage">
				<h2>Access Denied</h2>
				<p>Improper access. Your session has expired, please login again.</p>

				<img src='<s:url value="/images/brics/error/access denied.png"/>' />
			</div>
		</div>
	</div>
</div>

<script type="text/javascript">

	$(document).ready(function() {
		console.log( "session expired" );
		alert("Your session has expired, please login again.");
		doLogout();
	});

	function doLogout() {
		//if(window.location.href.indexOf("local") > -1 || window.location.href.indexOf("127") > -1){
	    //	window.location.replace(environment+"/portal/jsp/login.jsp");       
	    //}else{
	    	window.location.replace(environment+"/portal/logout");   
	    //}
	}

	
	setNavigation({"bodyClass":"primary"});
</script>
