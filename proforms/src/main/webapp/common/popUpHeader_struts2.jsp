<%@ page import="gov.nih.nichd.ctdb.common.CtdbConstants" %>
<%@ taglib uri="/struts-tags" prefix="s" %>


<head>
<s:bean name="gov.nih.nichd.ctdb.util.common.SystemPreferences" var="systemPreferences" />
<s:set var="webRoot" value="#systemPreferences.get('app.webroot')"/>
<s:set var="imageRoot" value="#systemPreferences.get('app.imageroot')"/>

<s:set var="defaultTitle" scope="request">
	<s:text name="app.title" />
</s:set>

<title>${pageTitle}</title>

<!-- Load jQuery from their CDN -->
<script src="https://cdnjs.cloudflare.com/ajax/libs/jquery/1.12.4/jquery.min.js" crossorigin="anonymous"></script>
<!-- Fallback to jQuery on server if above script is unavailable -->
<script>window.jQuery || document.write('<script src=\'<s:property value="#webRoot"/>/common/js/jquery-1.12.4.min.js\'><\/script>')</script>

<script type="text/javascript">
var language = "<%= request.getLocale() %>";
var baseUrl = '<s:property value="#webRoot"/>';
</script>
<%@include file="/common/templateCss.jsp" %>
<link rel="shortcut icon" type="image/x-icon" href="<s:property value="#systemPreferences.commonImageBaseUrl"/>/favicon.ico" />

<!-- Load jQuery from their CDN -->
<script src="https://cdnjs.cloudflare.com/ajax/libs/jquery-migrate/1.4.1/jquery-migrate.js"></script>
<script src="https://cdnjs.cloudflare.com/ajax/libs/jqueryui/1.12.1/jquery-ui.min.js"></script>

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

<style type="text/css">
#wrap {background-image: none;}

</style>
</head>
