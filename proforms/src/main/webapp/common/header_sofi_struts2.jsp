<%@ page import="gov.nih.nichd.ctdb.common.CtdbConstants,
                 gov.nih.nichd.ctdb.security.domain.User,
                 gov.nih.nichd.ctdb.common.CtdbForm,
                 gov.nih.nichd.ctdb.protocol.domain.Protocol,
                 gov.nih.tbi.account.model.hibernate.Account,
                 gov.nih.tbi.account.model.hibernate.AccountRole,
				 gov.nih.tbi.commons.model.RoleStatus,
                 gov.nih.tbi.commons.model.RoleType,
                 java.util.Iterator,
				 java.util.Set"
                  %>
<%@ taglib uri="/struts-tags" prefix="s"%>
<%@ taglib uri="/WEB-INF/security.tld" prefix="security" %>
<%@ taglib uri="http://java.sun.com/jstl/core" prefix="c" %>

<%
    // check to see if page uses tabs
    Object checkForTabs = request.getAttribute(CtdbConstants.USES_TABS);
    boolean usesTabs = (checkForTabs != null) ? (Boolean)checkForTabs : false;
	boolean overviewMode = request.getSession().getAttribute(CtdbConstants.CURRENT_PROTOCOL_SESSION_KEY) == null;

    String accessDenied = (String) session.getAttribute("accessdenied");
    boolean adenied = false;
    if (accessDenied != null && accessDenied.equals("1")) {
        adenied = true;
    }
    
    //get account obj from session using session_brics_account key
    //accountRoleList   ROLE_ADMIN  
    Account account = (Account)session.getAttribute("session_brics_account");
    Set<AccountRole> accountRoles = account.getAccountRoleList();
    
    Iterator<AccountRole> iter = accountRoles.iterator();
    boolean isROLE_ADMIN = false;
	while(iter.hasNext()) {
		AccountRole role = iter.next();
		if (role.getRoleType().equals(RoleType.ROLE_ADMIN) && role.getIsActive() && !role.isExpired()) {
			isROLE_ADMIN =  true;
			break;
		}
	}
%>

<head>
<title><s:text name="app.title"/></title>

<s:bean name="gov.nih.nichd.ctdb.util.common.SystemPreferences" var="systemPreferences" />
<s:set var="webRoot" value="#systemPreferences.get('app.webroot')"/>
<s:set var="imageRoot" value="#systemPreferences.get('app.imageroot')"/>

<meta charset="utf-8" /> 
<meta http-equiv="X-UA-Compatible" content="IE=edge,chrome=1" />
<meta http-equiv="Pragma" content="no-cache"/>
<meta http-equiv="Cache-Control" content="no-cache, no-store, must-revalidate" />
<meta http-equiv="Expires" content="0" />

<!-- Load jQuery from their CDN -->
<script src="https://code.jquery.com/jquery-1.7.2.min.js" integrity="sha256-R7aNzoy2gFrVs+pNJ6+SokH04ppcEqJ0yFLkNGoFALQ=" 
		crossorigin="anonymous"></script>
<!-- Fallback to jQuery on server if above script is unavailable -->
<script>window.jQuery || document.write('<script src=\'<s:property value="#webRoot"/>/common/js/jquery-1.7.2.min.js\'><\/script>')</script>
<script type="text/javascript">
	var language = "<%= request.getLocale() %>";
	var baseUrl = '<s:property value="#webRoot"/>';
</script>
<jsp:include page="/common/templateCss.jsp"/>
<link rel="shortcut icon" type="image/x-icon" href="<s:property value="#systemPreferences.commonImageBaseUrl"/>/favicon.ico" />
<link rel="stylesheet" type="text/css" href="https://cdn.datatables.net/responsive/2.1.1/css/responsive.dataTables.min.css" />
<link rel="stylesheet" type="text/css" href="https://cdn.datatables.net/v/dt/jszip-3.1.3/pdfmake-0.1.27/dt-1.10.15/b-1.3.1/b-colvis-1.3.1/b-flash-1.3.1/b-html5-1.3.1/b-print-1.3.1/datatables.min.css" />

<link rel="stylesheet" href="https://ajax.googleapis.com/ajax/libs/jqueryui/1.12.1/themes/smoothness/jquery-ui.css">
<script src="https://ajax.googleapis.com/ajax/libs/jquery/1.12.4/jquery.min.js"></script>
<script src="https://ajax.googleapis.com/ajax/libs/jqueryui/1.12.1/jquery-ui.min.js"></script>

<script src="<s:property value="#webRoot"/>/common/designs/datatablesWidget/jqfactory-03.js" type="text/javascript"></script>
<script type="text/javascript" src="https://cdn.datatables.net/v/dt/jszip-3.1.3/pdfmake-0.1.27/dt-1.10.15/b-1.3.1/b-colvis-1.3.1/b-flash-1.3.1/b-html5-1.3.1/b-print-1.3.1/datatables.min.js"></script>
<script type="text/javascript" src="https://cdn.datatables.net/select/1.2.2/js/dataTables.select.min.js"></script>


<script type="text/javascript">
//<![CDATA[
	var __autosave = false;
	var logoutTimeout = null;
	
	function logoutOfSystem() {
	    if (__autosave) {
	        preformAutosave();
	    } else {
	        top.location.href = '<s:property value="#webRoot"/>/logout';
	    }
	}

	var appTimeOut = <s:property value="#systemPreferences.get('app.timeout')"/>;
	// monitor and refresh the CAS timeout.  THIS DOES NOT INTERFERE WITH THE INDIVIDUAL PAGE TIMEOUT BELOW
	var currentDate = new Date();
	var startTime = currentDate.getTime();
	// check the timer every minute.
	setInterval(function() {refreshCasTimeout();}, 60000);
	var casClearIframeTimeout = null;
	
	function refreshCasTimeout() {
		// determine if we need to refresh the timeout at all
		var date = new Date();
		var urlRoot = "<s:property value="#webRoot"/>";
		var timeoutThreshold = <s:property value="#systemPreferences.get('application.integration.cas.localtimeout')"/>;
		var currentTime = date.getTime();
		var elapsedTime = (currentTime - startTime) / 60000;
		
		if (elapsedTime > timeoutThreshold) {
			// create an invisible IFrame
			if ($("#hiddenIframe").length < 1) {
				$("body").append('<iframe id="hiddenIframe" src="'+ urlRoot + 
						'/casLoginRedirect.action?unique=' + currentTime + '" style="display: none;"><iframe>');
			}
			else {
				$("#hiddenIframe").attr("src", urlRoot + '/casLoginRedirect.action?unique=' + currentTime);
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
    logoutTimeout = setTimeout("logoutOfSystem()", appTimeOut);
    function adjustTimeout () {
        window.clearTimeout (logoutTimeout);
        logoutTimeout = setTimeout("logoutOfSystem()", appTimeOut);
    }
    
    function clearLogoutTimeout() {
    	if (logoutTimeout != null) {
    		window.clearTimeout(logoutTimeout);
    	}
    }
    
    $(document).ready(function() {
     	$("div#topNavContainer ul li a").click(function() {
        	if(<%= overviewMode %>) { 
      			var link = $(this).attr("href").toLowerCase();
      			
        		if(link.indexOf("patienthome") >= 0) {
        			$.ibisMessaging("dialog", "info", "Please select a study to manage subjects.");
        			return false;
        		}
        		else if (link.indexOf("datacollecting") >= 0) {
        			$.ibisMessaging("dialog", "info", "Please select a study to collect data.");
        	  		return false;
        		}    	    	
        		else if (link.indexOf("formhome") >= 0) {
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
					<s:set var="altTextKey" value="%{'template.' + #systemPreferences.get('template.global.appName') + '.logo.alttext'}" />
					<a id="logo" class="float-left" href="<s:property value="#systemPreferences.get('brics.modules.workspace.url')"/>">
						<em class="alt"><s:property value="#altTextKey"/></em>
					</a>
					<a href="<s:property value="#webRoot"/>/dashboard.action" id="logoAlt" class="">
						<em class="alt"><s:property value="#altTextKey"/></em> 
					</a>
					 
					<div id="login-button" class="float-right">
						<div id="logout-link">
							<p>
								<c:if test="${user.username != null }">
									<s:text name="app.welcome" />
									<% 
										if(isROLE_ADMIN) {
									%>
											<b>Administrator, </b>
									<% 
										}else {
									%>
										<security:hasProtocolPrivilege privilege="sysadmin">
											<b>Administrator, </b>
										</security:hasProtocolPrivilege>
									<%
										}
									%>
									<c:out value="${user.firstName }" /> | 		
							 		<a href="<s:property value="#webRoot"/>/logout"><s:text name="app.logOut" /></a>
								</c:if>
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
					<img src="<s:property value="#webRoot"/>/images/perspectiveCutout.png" id="perspectiveCutoutLeft" alt="" />
					<a href="<s:property value="#webRoot"/>/dashboard.action?study=all" id="dashboardLink">
						<s:text name="myworkspace.title.display" />
					</a>
					<jsp:include page="/common/perspectiveSelect.jsp" />
				</div>
			</div>
			<div id="contentContainer" class="container960">
				<div id="rightColumn">
					<div id="mainContent">
						<div id="pageHeader"><h1><c:out value="${pageTitle}"/> Sofi's Page</h1></div>
						<jsp:include page="/common/messages_struts2.jsp" />
										