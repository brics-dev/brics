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
%>

<head>
<title><s:text name="app.title"/></title>

<s:bean name="gov.nih.nichd.ctdb.util.common.SystemPreferences" var="systemPreferences" />
<s:set var="webRoot" value="#systemPreferences.get('app.webroot')"/>
<s:set var="imageRoot" value="#systemPreferences.get('app.imageroot')"/>
<s:set var="appName" value="#systemPreferences.get('template.global.appName')" />

<meta http-equiv="Content-Type" content="text/html; charset=utf-8" /> 
<meta http-equiv="X-UA-Compatible" content="IE=edge,chrome=1" />
<meta http-equiv="Pragma" content="no-cache"/>
<meta http-equiv="Cache-Control" content="no-cache, no-store, must-revalidate" />
<meta http-equiv="Expires" content="0" />

<s:if test="%{#appName == 'nti'}">
	<!-- Google Tag Manager -->
	<script>(function(w,d,s,l,i){w[l]=w[l]||[];w[l].push({'gtm.start':
	new Date().getTime(),event:'gtm.js'});var f=d.getElementsByTagName(s)[0],
	j=d.createElement(s),dl=l!='dataLayer'?'&l='+l:'';j.async=true;j.src=
	'https://www.googletagmanager.com/gtm.js?id='+i+dl;f.parentNode.insertBefore(j,f);
	})(window,document,'script','dataLayer','GTM-K2MLJ55');</script>
	<!-- End Google Tag Manager -->
</s:if>

<jsp:include page="/common/templateCss.jsp"/>
<link rel="shortcut icon" type="image/x-icon" 
	href="<s:property value="#systemPreferences.commonImageBaseUrl"/>/favicon.ico" />

<!-- Set Common Global Variables -->
<script type="text/javascript">
	var language = "<%= request.getLocale() %>";
	var baseUrl = '<s:property value="#webRoot"/>';
	var appTimeOut = '<s:property value="#systemPreferences.get(\'app.timeout\')" />';
	var appWarningThreshold = '<s:property value="#systemPreferences.get(\'app.warningTimeout\')" />';
	var appTimeoutThreshold = '<s:property value="#systemPreferences.get(\'application.integration.cas.localtimeout\')" />';

</script>

<!-- Load jQuery from their CDN -->
<script src="https://cdnjs.cloudflare.com/ajax/libs/jquery/1.12.4/jquery.min.js" crossorigin="anonymous"></script>
<!-- Fallback to jQuery on server if above script is unavailable -->
<script>window.jQuery || document.write('<script src=\'<s:property value="#webRoot"/>/common/js/jquery-1.12.4.min.js\'><\/script>')</script>

<script src="https://cdnjs.cloudflare.com/ajax/libs/jquery-migrate/1.4.1/jquery-migrate.js"></script>
<script src="https://cdnjs.cloudflare.com/ajax/libs/jqueryui/1.12.1/jquery-ui.min.js"></script>

<script src="<s:property value="#webRoot"/>/common/sessionHandlerPF.js" type="text/javascript"></script>
<script src="<s:property value="#webRoot"/>/common/common.js" type="text/javascript"></script>
<script src="<s:property value="#webRoot"/>/common/js/jquery.multiselect.min.js" type="text/javascript"></script>
<script src="<s:property value="#webRoot"/>/common/js/jquery.dataTables.js" type="text/javascript"></script>
<script src="<s:property value="#webRoot"/>/common/designs/dataTables/js/FixedHeader.min.js" type="text/javascript"></script>

<script src="<s:property value="#webRoot"/>/common/js/bootstrap/bootstrap-4.1.1.min.js" type="text/javascript"></script>
<script src="<s:property value="#webRoot"/>/common/js/bootstrap/bootstrap-multiselect.js" type="text/javascript"></script>

<script src="<s:property value="#webRoot"/>/common/js/jquery.ui.touch-punch.min.js" type="text/javascript"></script>
<script src="<s:property value="#webRoot"/>/common/js/jquery.selectbox-0.2.min.js" type="text/javascript"></script>
<script src="<s:property value="#webRoot"/>/common/js/jquery.scrollTo.min.js" type="text/javascript"></script>
<script src="<s:property value="#webRoot"/>/common/js/HashTable.js" type="text/javascript"></script>
<script src="<s:property value="#webRoot"/>/common/js/jquery.formalize.js" type="text/javascript"></script>
<script src="<s:property value="#webRoot"/>/common/js/jquery.fancybox-1.3.4.pack.js" type="text/javascript"></script>
<script src="<s:property value="#webRoot"/>/common/js/jquery.ui.accordion.activationExtension.js" type="text/javascript"></script>
<script src="<s:property value="#webRoot"/>/common/js/jquery.cookie.js" type="text/javascript"></script>
<script src="<s:property value="#webRoot"/>/common/js/ibis/core_libs.min.js" type="text/javascript"></script>
<script src="<s:property value="#webRoot"/>/common/js/ibis/core_classes.min.js" type="text/javascript"></script>
<%--<script src="<s:property value="#webRoot"/>/common/js/ibis/jquery.ibisDataTables-2.0.min.js" type="text/javascript"></script>  --%>

<script src="<s:property value="#webRoot"/>/common/js/jquery-ui-sliderAccess_v1.4.6.js" type="text/javascript"></script>
<script src="<s:property value="#webRoot"/>/common/js/jquery-ui-timepicker-addon_v1.4.6.js" type="text/javascript"></script>
<script src="<s:property value="#webRoot"/>/common/js/jquery.ibisMessaging-0.1.full.js" type="text/javascript"></script>
<script src="<s:property value="#webRoot"/>/common/js/ibisCommon.js" type="text/javascript"></script>
<script src="<s:property value="#webRoot"/>/common/js/ibisMessageResource.js" type="text/javascript"></script>
<script src="<s:property value="#webRoot"/>/common/js/templateInteraction.js" type="text/javascript"></script>

<script src="<s:property value="#webRoot"/>/common/designs/datatables2.0/OpButtonController.js" type="text/javascript"></script>
<script src="<s:property value="#webRoot"/>/common/designs/datatables2.0/DataTableLanguages.js" type="text/javascript"></script>
<script src="<s:property value="#webRoot"/>/common/designs/datatables2.0/Row.js" type="text/javascript"></script>
<script src="<s:property value="#webRoot"/>/common/designs/datatables2.0/TableCol.js" type="text/javascript"></script>
<script src="<s:property value="#webRoot"/>/common/designs/datatables2.0/Rows.js" type="text/javascript"></script>
<script src="<s:property value="#webRoot"/>/common/designs/datatables2.0/TableCols.js" type="text/javascript"></script>
<script src="<s:property value="#webRoot"/>/common/designs/datatables2.0/DataTable.js" type="text/javascript"></script>
<script src="<s:property value="#webRoot"/>/common/designs/datatables2.0/DataTables.js" type="text/javascript"></script>
<script src="<s:property value="#webRoot"/>/common/designs/datatables2.0/DataTableView.js" type="text/javascript"></script>
<script src="<s:property value="#webRoot"/>/common/designs/datatables2.0/IBISDataTables.js" type="text/javascript"></script>

<!-- New Proforms DataTables -->
<script src="<s:property value="#webRoot"/>/common/designs/dataTables/2.0/lib/datatables.min.js" type="text/javascript"></script>
<script src="<s:property value="#webRoot"/>/common/designs/dataTables/2.0/lib/dataTables.select.min.js" type="text/javascript"></script>
<script src="<s:property value="#webRoot"/>/common/designs/dataTables/2.0/jqfactory-03.js" type="text/javascript"></script>
<script src="<s:property value="#webRoot"/>/common/designs/dataTables/2.0/idtTableWidget.js" type="text/javascript"></script>
<script src="<s:property value="#webRoot"/>/common/designs/dataTables/2.0/fnDtFilterPlugin.js" type="text/javascript"></script>
<script src="<s:property value="#webRoot"/>/common/designs/dataTables/2.0/idtSelectSetPlugin.js" type="text/javascript"></script>
<script src="<s:property value="#webRoot"/>/common/designs/dataTables/2.0/idtSearchColumnPlugin.js" type="text/javascript"></script>
<script src="<s:property value="#webRoot"/>/common/designs/dataTables/2.0/idtTableActions.js" type="text/javascript"></script>
<script src="<s:property value="#webRoot"/>/common/designs/dataTables/2.0/idtApi.js" type="text/javascript"></script>
<script type="text/javascript">$.fn.bootstrapBtn = $.fn.button.noConflict()</script>

<!-- added by Ching-Heng -->
<script src="<s:property value="#webRoot"/>/common/js/crypto.js" type="text/javascript" ></script>
<script type="text/javascript">
	//added by Ching-Heng for PROMIS api
	var HealthMeasurementApiUrl = "<s:property value="#systemPreferences.get('healthMeasurement.api.url')" />";
	var HealthMeasurementApiToken = "<s:property value="#systemPreferences.get('healthMeasurement.api.token')" />";
	var casServiceUrl = "<s:property value="#systemPreferences.get('webservice.cas.service.url')" />";

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
</script>
</head>


<body>
	<s:if test="%{#appName == 'nti'}">
		<!-- Google Tag Manager (noscript) -->
		<noscript><iframe src="https://www.googletagmanager.com/ns.html?id=GTM-K2MLJ55"
		height="0" width="0" style="display:none;visibility:hidden"></iframe></noscript>
		<!-- End Google Tag Manager (noscript) -->
	</s:if>
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
					<s:if test="%{#appName == 'nti'}">
						<div id="headerTitle" class="float-left"><b>NTRR</b> National Trauma Research Repository</div>
					</s:if>
					<div id="login-button" class="float-right">
						<div id="logout-link">
							<p>							
								<s:if test="{user!= null}">
									<s:text name="app.welcome" />
									<security:hasPrivilege privileges="sysadmin"> 
						 				<b>Administrator, </b>
						 			</security:hasPrivilege>
									<s:property value="user.firstName" /> | 		
							 		<a href="<s:property value="#webRoot"/>/logout"><s:text name="app.logOut" /></a>
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
						<div id="pageHeader"><h1><s:property value="%{#pageTitle}" /></h1></div>
						<jsp:include page="/common/messages_struts2.jsp" />									