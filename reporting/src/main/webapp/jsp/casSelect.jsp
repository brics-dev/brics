<title>Organization Selection</title>

<%@include file="/common/taglibs.jsp"%>
<%@page import="java.text.SimpleDateFormat"%>
<%@taglib prefix="decorator" uri="http://www.opensymphony.com/sitemesh/decorator"%>
<%@taglib prefix="page" uri="http://www.opensymphony.com/sitemesh/page"%>

<script type="text/javascript" src='<s:url value="/js/browser-check.js"/>'></script>

<div class="clear-float">
	<h1 class="float-left">Please Select an Affiliation</h1>
</div>

<div class="border-wrapper wide">
	<div id="main-content">
		
		<h3>Affiliation</h3>
			<p>In order to user the Data Dictionary, please select a supporting organization.</p>
			
			<div id="browser-msg" class="default-error clear-both" style="display: none">
				<p>We have detected that you are using an unsupported browser, please use one of the recommended browsers listed below.</p> 
			</div>

			<s:if test="hasActionErrors()">
				<div class="default-error clear-both">
					<s:actionerror />
				</div>
			</s:if>
			
			<p><a href="https://fitbir-dev.cit.nih.gov/cas/login?service=http://localhost:8080/portal/tbi_spring_cas_security_check">Federal Interagency Traumatic Brain Injury Research (FITBIR)</a>Informatics System</p>
			<p><a href="https://pdbp-dev.cit.nih.gov/cas/login?service=http://localhost:8080/portal/pd_spring_cas_security_check">Parkinson's Disease Biomarkers Program (PDBP)</a></p>
			
			<div class="text-block">
				<p>				
				<em>We recommend using one of the following supported browsers and versions for accessing the FITBIR website: Internet Explorer 9+, 
				Firefox 9+, Chrome</em>
				</p>
			</div>
			
	</div>
</div>


<script type="text/javascript">
	setNavigation({
		"bodyClass" : "primary"
	});
	
	$(document).ready(function(){
		//Supported browsers include IE9+, Firefox 9+, Chrome
		var supported = true;

		if( BrowserDetect.browser == "Explorer" && BrowserDetect.version < 9)
			{ supported = false; }
		else if( BrowserDetect.browser == "Firefox" && BrowserDetect.version < 9)
			{ supported = false; }
		else if( BrowserDetect.browser == "OmniWeb/" ||  BrowserDetect.browser == "Safari" ||  BrowserDetect.browser == "Opera" 
			||  BrowserDetect.browser == "iCab" ||  BrowserDetect.browser == "KDE" ||  BrowserDetect.browser == "Camino"
			||  BrowserDetect.browser == "Netscape" ||  BrowserDetect.browser == "Mozilla" )
		{ supported = false; }
			
		if( !supported)
			{ $("#browser-msg").show(); }
		
	});
</script>