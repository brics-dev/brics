 <%@ page import="gov.nih.nichd.ctdb.common.CtdbConstants,gov.nih.nichd.ctdb.security.domain.User,gov.nih.nichd.ctdb.common.CtdbForm,gov.nih.nichd.ctdb.protocol.domain.Protocol,gov.nih.nichd.ctdb.util.common.SysPropUtil" %>
   <%@ taglib uri="/WEB-INF/struts-bean.tld" prefix="bean" %>
   <%@ taglib uri="/WEB-INF/struts-html.tld" prefix="html" %>
   <%@ taglib uri="/WEB-INF/struts-logic.tld" prefix="logic" %>
   <%@ taglib uri="/WEB-INF/security.tld" prefix="security" %>
   <jsp:useBean id="systemPreferences" class="gov.nih.nichd.ctdb.util.common.SystemPreferences"/>
   <s:set var="webRoot" value="%{systemPreferences.get('app.webroot')}"/>
   <s:set var="imageRoot" value="%{systemPreferences.get('app.imageroot')}"/>
   <jsp:useBean id="user" scope="session" class="gov.nih.nichd.ctdb.security.domain.User"/>
   <s:set var="appName" value="%{systemPreferences.get('template.global.appName')}" />
   <%
  	String appName = (String)pageContext.getAttribute("appName");
    // check to see if page uses tabs
    Boolean checkForTabs = (Boolean) request.getAttribute(CtdbConstants.USES_TABS);
    boolean usesTabs = false;
	boolean overviewMode = request.getSession().getAttribute(CtdbConstants.CURRENT_PROTOCOL_SESSION_KEY) == null;

    if(checkForTabs != null)
    {
        if(checkForTabs.booleanValue())
        {
            usesTabs = checkForTabs.booleanValue();
        }
    }
    String accessDenied = (String) session.getAttribute("accessdenied");
    boolean adenied = false;
    if (accessDenied != null && accessDenied.equals("1"))
    {
        adenied = true;
    }
	%>
<head>
<title><bean:message key="app.title"/></title>
<meta charset="utf-8" /> 
<meta http-equiv="X-UA-Compatible" content="IE=edge,chrome=1" />
<meta http-equiv="pragma" content="no-cache"/>
<meta http-equiv="expires" content="Mon, 06 Jan 1990 00:00:01 GMT" />
<meta http-equiv="cache-control" content="no-cache" />
<html:base />

<!-- Load jQuery from their CDN -->
<script src="https://code.jquery.com/jquery-1.7.2.min.js" integrity="sha256-R7aNzoy2gFrVs+pNJ6+SokH04ppcEqJ0yFLkNGoFALQ=" 
		crossorigin="anonymous"></script>

<script type="text/javascript">
var language = "<%= request.getLocale() %>";
</script>
<jsp:include page="/common/templateCss.jsp"/>
<link rel="shortcut icon" type="image/x-icon" 
		href="<s:property value="%{#systemPreferences.commonImageBaseUrl}" />"/>/favicon.ico" />
<script src="<%= webRoot %>/common/js/jquery-ui-1.10.3.custom.js" type="text/javascript"></script>
<script src="<%= webRoot %>/common/js/jquery.ui.touch-punch.min.js" type="text/javascript"></script>

<script src="<%= webRoot %>/common/js/jquery.selectbox-0.2.min.js" type="text/javascript"></script>
<script src="<%= webRoot %>/common/js/jquery.scrollTo.min.js" type="text/javascript"></script>
<script src="<%= webRoot %>/common/js/HashTable.js" type="text/javascript"></script>

<script src="<%= webRoot %>/common/js/jquery.formalize.js" type="text/javascript"></script>
<script src="<%= webRoot %>/common/js/jquery.fancybox-1.3.4.pack.js" type="text/javascript"></script>
<script src="<%= webRoot %>/common/common.js" type="text/javascript"></script>
<script src="<%= webRoot %>/common/js/jquery.ui.accordion.activationExtension.js" type="text/javascript"></script>
<script src="<%= webRoot %>/common/js/jquery.dataTables.min.js" type="text/javascript"></script>
<script src="<%= webRoot %>/common/js/jquery.cookie.js" type="text/javascript"></script>
<script src="<%= webRoot %>/common/js/underscore-min.js" type="text/javascript"></script>
<script src="<%= webRoot %>/common/js/jquery.dataTables.ibisExtension.js" type="text/javascript"></script>
<script src="<%= webRoot %>/common/js/jquery-ui-timepicker-addon.js" type="text/javascript"></script>

<script src="<%= webRoot %>/common/js/jquery.ibisMessaging-0.1.full.js" type="text/javascript"></script>
<script src="<%= webRoot %>/common/js/ibisCommon.js" type="text/javascript"></script>
<script src="<%= webRoot %>/common/js/ibisMessageResource.js" type="text/javascript"></script>
<script src="<%= webRoot %>/common/js/templateInteraction.js" type="text/javascript"></script>
<script type="text/javascript">
//<![CDATA[
	var __autosave = false;
	var logoutTimeout = null;
	function logoutOfSystem() {
	    if (__autosave) {
	        preformAutosave();
	    } else {
	        top.location.href = '<%= webRoot %>/logout';
	    }
	
	}
	
	// monitor and refresh the CAS timeout.  THIS DOES NOT INTERFERE WITH THE INDIVIDUAL PAGE TIMEOUT BELOW
	var currentDate = new Date();
	var startTime = currentDate.getTime();
	// check the timer every minute.
	setInterval(function() {refreshCasTimeout();}, 60000);
	var casClearIframeTimeout = null;
	
	function refreshCasTimeout() {
		// determine if we need to refresh the timeout at all
		var date = new Date();
		var urlRoot = "<%= webRoot %>";
		var timeoutThreshold = <%= SysPropUtil.getProperty("application.integration.cas.localtimeout") %>;
		var currentTime = date.getTime();
		var elapsedTime = (currentTime - startTime)/60000;
		if (elapsedTime > timeoutThreshold) {
			// create an invisible IFrame
			if ($("#hiddenIframe").length < 1) {
				$("body").append('<iframe id="hiddenIframe" src="'+ urlRoot + '/casLoginRedirect.do?unique='+currentTime+'" style="display: none;"><iframe>');
			}
			else {
				$("#hiddenIframe").attr("src", urlRoot + '/casLoginRedirect.do?unique='+currentTime);
			}
			
			// set a timer to remove the iframe if it doesn't remove itself
			// 1 minute
			casClearIframeTimeout = setTimeout(function() {endIframeProcess();}, 6000);
		}
	}
	
	function stopRefreshCasTimeout() {
		window.clearTimeout(casClearIframeTimeout);
		//$("#hiddenIframe").remove(); // don't remove iframe because it doesn't hurt anything and it causes a js error in IE9
	}
	
	function endIframeProcess() {
		if($("#hiddenIframe").length > 0) {
			stopRefreshCasTimeout();
		}
	}

    // TIMEOUT AFTER A PERIOD OF INACTIVITY
    logoutTimeout = setTimeout("logoutOfSystem()", <%= SysPropUtil.getProperty("app.timeout") %>);
    function adjustTimeout () {
        window.clearTimeout (logoutTimeout);
        logoutTimeout = setTimeout("logoutOfSystem()", <%= SysPropUtil.getProperty("app.timeout") %>);
    }
    
    function clearLogoutTimeout() {
    	if (logoutTimeout != null) {
    		window.clearTimeout(logoutTimeout);
    	}
    }
    
    $(document).ready(function(){
     	$("div#topNavContainer ul li a").click(function(){
        	if(<%= overviewMode %>) { 
      			var link = $(this).attr("href").toLowerCase();
        		if(link.indexOf("patienthome") >=0 ){
        			$.ibisMessaging("dialog", "info", "Please select a study to manage subjects.");
        			return false;
        		}
        		else if (link.indexOf("datacollecting") >=0 ){
        			$.ibisMessaging("dialog", "info", "Please select a study to collect data.");
        	  		return false;
        		}    	    	
        		else if (link.indexOf("formhome") >=0 ){
        			$.ibisMessaging("dialog", "info", "Please select a study to manage forms.");
        	  		return false;
        		}
        	}
    	});
    });
 //]]>
</script>
</head>


<body>
	<div id="wrap">
	<jsp:include page="/common/envBanner.jsp" />  
		<div id="header">
				<div class="content">
					<%String altTextKey = "template." + appName + ".logo.alttext"; %>
					<a id="logo" class="float-left" href='${systemPreferences.get('brics.modules.workspace.url')}'>
						<em class="alt"><bean:message key="<%=altTextKey %>"/></em>
					</a>
					<a href="<%= webRoot %>/dashboard.do" id="logoAlt" class="">
						<em class="alt"><bean:message key="<%=altTextKey %>"/></em> 
					</a>
					 
					<div id="login-button" class="float-right">
						<div id="logout-link">
							<p>
								<s:if test="%{#user.username!= null}">
									<bean:message key="app.welcome" /> ${user.firstName }/> | 		
							 		<a href="<%= webRoot %>/logout"><bean:message key="app.logOut" /></a>
								</s:if>
							</p>
						</div>
					</div>	
				</div>
			</div>
				
			<jsp:include page="/common/horizontalNav.jsp" />  
						

		<div id="main" class="container960">
			<div id="mainHeaderArea" class="container960">
				<h1><jsp:include page="/common/cistarproforms.jsp" /></h1>
				<div id="perspectiveCutout">
					<img src="<%= webRoot %>/images/perspectiveCutout.png" id="perspectiveCutoutLeft" alt="" />
					<a href="<%= webRoot %>/dashboard.do?study=all" id="dashboardLink"><bean:message key="myworkspace.title.display" /></a>
					<jsp:include page="/common/perspectiveSelect.jsp" />
				</div>
			</div>
			<div id="contentContainer" class="container960">
			<div id="rightColumn">
					<div id="mainContent">
						<div id="pageHeader"><h1>${pageTitle}</h1></div>
						<jsp:include page="/common/messages.jsp" />
										