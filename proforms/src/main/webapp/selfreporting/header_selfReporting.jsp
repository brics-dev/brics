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

<head>
<title><s:text name="app.title"/></title>

<s:bean name="gov.nih.nichd.ctdb.util.common.SystemPreferences" var="systemPreferences" />
<s:set var="webRoot" value="#systemPreferences.get('app.webroot')"/>
<s:set var="imageRoot" value="#systemPreferences.get('app.imageroot')"/>

<meta charset="utf-8" /> 
<meta http-equiv="X-UA-Compatible" content="IE=edge,chrome=1" />
<meta http-equiv="Pragma" content="no-cache"/>
<meta http-equiv="Cache-Control" content="no-cache, no-store, must-revalidate, private" />
<meta http-equiv="Expires" content="0" />

<script src="<s:property value="#webRoot"/>/common/js/jquery-1.7.2.min.js" type="text/javascript"></script>
<script type="text/javascript">
	var language = "<%= request.getLocale() %>";
	var baseUrl = '<s:property value="#webRoot"/>';
	// added by Ching-Heng for PROMIS api
	var HealthMeasurementApiUrl = "<s:property value="#systemPreferences.get('healthMeasurement.api.url')" />";
	var HealthMeasurementApiToken = "<s:property value="#systemPreferences.get('healthMeasurement.api.token')" />";
</script>
<jsp:include page="/common/templateCss.jsp"/>
<link rel="shortcut icon" type="image/x-icon" 
	href="<s:property value="#systemPreferences.commonImageBaseUrl"/>/favicon.ico" />

<script src="<s:property value="#webRoot"/>/common/js/jquery-ui-1.10.3.custom.min.js" type="text/javascript"></script>
<script src="<s:property value="#webRoot"/>/common/js/jquery.ui.touch-punch.min.js" type="text/javascript"></script>
<script src="<s:property value="#webRoot"/>/common/js/jquery.selectbox-0.2.min.js" type="text/javascript"></script>
<script src="<s:property value="#webRoot"/>/common/js/jquery.scrollTo.min.js" type="text/javascript"></script>
<script src="<s:property value="#webRoot"/>/common/js/HashTable.js" type="text/javascript"></script>
<script src="<s:property value="#webRoot"/>/common/js/jquery.formalize.js" type="text/javascript"></script>
<script src="<s:property value="#webRoot"/>/common/js/jquery.fancybox-1.3.4.pack.js" type="text/javascript"></script>
<script src="<s:property value="#webRoot"/>/common/common.js" type="text/javascript"></script>
<script src="<s:property value="#webRoot"/>/common/js/jquery.ui.accordion.activationExtension.js" type="text/javascript"></script>
<script src="<s:property value="#webRoot"/>/common/js/datatables-1.10.2/js/jquery.dataTables.min.js" type="text/javascript"></script>
<script src="<s:property value="#webRoot"/>/common/js/jquery.cookie.js" type="text/javascript"></script>
<script src="<s:property value="#webRoot"/>/common/js/ibis/core_libs.min.js" type="text/javascript"></script>
<script src="<s:property value="#webRoot"/>/common/js/ibis/core_classes.min.js" type="text/javascript"></script>
<script src="<s:property value="#webRoot"/>/common/js/ibis/jquery.ibisDataTables-2.0.min.js" type="text/javascript"></script>
<script src="<s:property value="#webRoot"/>/common/js/jquery-ui-timepicker-addon.js" type="text/javascript"></script>
<script src="<s:property value="#webRoot"/>/common/js/jquery.ibisMessaging-0.1.full.js" type="text/javascript"></script>
<script src="<s:property value="#webRoot"/>/common/js/ibisCommon.js" type="text/javascript"></script>
<script src="<s:property value="#webRoot"/>/common/js/ibisMessageResource.js" type="text/javascript"></script>
<script src="<s:property value="#webRoot"/>/common/js/templateInteraction.js" type="text/javascript"></script>
<script src="<s:property value="#webRoot"/>/common/js/jquery.sticky.js" type="text/javascript"></script>
</head>


<body>
	<div id="wrap">
	<jsp:include page="/common/envBanner.jsp" />  
		<div id="header">
			<div class="content">
				<s:set var="altTextKey" value="%{'template.' + #systemPreferences.get('template.global.appName') + '.logo.alttext'}" />
				<a id="logo" class="float-left" href="javascript:;">
					<em class="alt"><s:property value="#altTextKey"/></em>
				</a>
				<a href="javascript:;" id="logoAlt" class="">
					<em class="alt"><s:property value="#altTextKey"/></em> 
				</a>
			</div>
		</div>
		<div id="main" class="container960 patientSelfReporting">
			<div id="" class="psrHeader">
				<s:set var="pageTitle" scope="request">
					<s:text name="selfreporting.selfreportingHome.title" />
				</s:set>
			
				<h2><s:text name="selfreporting.selfreportingHome.title" /></h2>
			</div>
			<div id="contentContainer" class="container960">
				<div id="selfReportingMainContent">
				<jsp:include page="/common/messages_struts2.jsp" />
										
										
