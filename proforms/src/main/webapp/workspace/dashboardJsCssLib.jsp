<%@ page import="gov.nih.nichd.ctdb.common.CtdbConstants,
                 gov.nih.nichd.ctdb.security.domain.User,
                 gov.nih.nichd.ctdb.common.CtdbForm"
                 %>
                 
<%@ taglib uri="/struts-tags" prefix="s"%>
<s:bean name="gov.nih.nichd.ctdb.util.common.SystemPreferences" var="systemPreferences" />
<s:set var="webRoot" value="#systemPreferences.get('app.webroot')"/>

<head>
<link rel="stylesheet" type="text/css" href="<s:property value="#webRoot"/>/common/css/hichart/highcharts-ng.css">
<link rel="stylesheet" type="text/css" href="<s:property value="#webRoot"/>/common/css/hichart/select.css">
<link rel="stylesheet" type="text/css" href="<s:property value="#webRoot"/>/common/css/bootstrap/bootstrap-3.2.0.css">
<link rel="stylesheet" type="text/css" href="<s:property value="#webRoot"/>/common/css/hichart/select2.css">
<link rel="stylesheet" type="text/css" href="<s:property value="#webRoot"/>/common/css/hichart/selectize.default-0.8.5.css">

<script src="<s:property value="#webRoot"/>/common/js/angular/angular-1.7.5.js"></script>
<script src="<s:property value="#webRoot"/>/common/js/angular/angular-resource-1.7.5.js"></script>
<script src="<s:property value="#webRoot"/>/common/js/angular/angular-route-1.7.5.js"></script>
<script src="<s:property value="#webRoot"/>/common/js/angular/angular-sanitize-1.7.5.js"></script>

<script src="<s:property value="#webRoot"/>/common/js/hichart/highcharts-7.1.0.js"></script>
<script src="<s:property value="#webRoot"/>/common/js/hichart/drilldown-7.1.0.js"></script>
<script src="<s:property value="#webRoot"/>/common/js/hichart/exporting-7.1.0.js"></script>
<script src="<s:property value="#webRoot"/>/common/js/hichart/export-data-7.1.0.js"></script>
<script src="<s:property value="#webRoot"/>/common/js/hichart/offline-exporting-7.1.0.js"></script>
<script src="<s:property value="#webRoot"/>/common/js/hichart/select.js"></script>
<script src="<s:property value="#webRoot"/>/common/js/hichart/highcharts-ng.js"></script>

<style>
	div.ui-select-container {
		width: 270px;
	}
	span.select2-chosen {
		width: inherit;
	}
	span.filter-name-span {
	    display: inline-block;
		width: 160px;
		font-weight: bold;
		text-align: right;
	}
	span.input-group-btn {
		display: inline-block;
	}
	span.input-group-btn > .btn {
		height: 13px;
	}
	#navigation {
		font-size: 12px;
	}
	* {
		-webkit-box-sixing: unset !important;
		box-sizing: unset !important;
	}

</style>
</head>