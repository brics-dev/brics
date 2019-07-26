<%@taglib prefix="s" uri="/struts-tags"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<link rel="stylesheet" href="https://ajax.googleapis.com/ajax/libs/jqueryui/1.12.1/themes/smoothness/jquery-ui.css">

<link rel="stylesheet" type="text/css" href="<s:url value='/styles/jquery/jquery.dataTables.css'/>"/>
<link rel="stylesheet" type="text/css" href="<s:url value='/styles/jquery/datatables.css'/>"/>
<link rel="stylesheet" href="<s:url value='/styles/bootstrap/bootstrap-3.1.1.min.css'/>" type="text/css">
<link rel="stylesheet" href="<s:url value='/styles/bootstrap/bootstrap-multiselect.css'/>" type="text/css">
<link rel="stylesheet" href="<s:url value='/styles/chosen/chosen.css'/>" type="text/css">
<link rel="stylesheet" href="<s:url value='/styles/jquery.mCustomScrollbar.css'/>" type="text/css">
<link href="<s:url value='/styles/jquery/jquery.qtip.css'/>" rel="stylesheet" type="text/css" media="all" />
<link type="text/css" href="https://maxcdn.bootstrapcdn.com/font-awesome/4.7.0/css/font-awesome.min.css" rel="stylesheet" />

<!-- new datatables styles -->
<link rel="stylesheet" type="text/css" href='<s:url value="/js/dataTables/2.0/css/datatables-pdfmake.min.css"/>' />
<!-- <link rel="stylesheet" type="text/css" href="https://cdn.datatables.net/responsive/2.1.1/css/responsive.dataTables.min.css" />  -->
<link rel="stylesheet" type="text/css" href='<s:url value="/js/dataTables/2.0/css/responsive.dataTables.min.css"/>' />
<link rel="stylesheet" type="text/css" href='<s:url value="/js/dataTables/2.0/css/datatablesWidget.css"/>' />
<link rel="stylesheet" type="text/css" href='<s:url value="/js/dataTables/2.0/css/idtSearchColumnPlugin.css"/>' />
<link rel="stylesheet" type="text/css" href='<s:url value="/js/dataTables/2.0/css/idtSelectSetPlugin.css"/>' />

<!-- Messaging style -->
<link rel="stylesheet" href="<s:url value='/styles/messaging/jquery.ibisMessaging-0.1.css'/>" type="text/css">

<script type="text/javascript">
	var baseUrl = '<s:url value="/" />';
	var portalHost = "<c:out value="${portalHostURL}" />";
</script>

<!-- Load jQuery from their CDN -->
<%-- <script src="https://code.jquery.com/jquery-1.12.4.min.js" integrity="sha256-ZosEbRLbNQzLpnKIkEdrPv7lOy9C27hHQ+Xp8a4MxAQ=" crossorigin="anonymous"></script> --%>
<script src="https://cdnjs.cloudflare.com/ajax/libs/jquery/1.12.4/jquery.min.js" crossorigin="anonymous"></script>
<!-- Fallback to jQuery on BRICS if above script is unavailable -->
<script>window.jQuery || document.write('<script src=\'<s:url value="/js/jquery-1.12.4.min.js"/>\'><\/script>')</script>
<%-- <script src="https://code.jquery.com/jquery-migrate-1.4.1.min.js"></script> --%>
<script src="https://cdnjs.cloudflare.com/ajax/libs/jquery-migrate/1.4.1/jquery-migrate.js"></script>
<script src="https://ajax.googleapis.com/ajax/libs/jqueryui/1.12.1/jquery-ui.min.js"></script>
<script src="https://cdn.polyfill.io/v2/polyfill.min.js?features=default,Array.prototype.includes"></script>


<%-- min file contains all the files located in /js/common-source-files/ --%>
<script type="text/javascript" src='<s:url value="/js/common.min.js"/>'></script>
<script type="text/javascript" charset="utf-8" language="javascript" src='<s:url value="/js/jquery.multiselect.min.js"/>'></script>
<script type="text/javascript" charset="utf-8" language="javascript" src='<s:url value="/formbuilder/js/lib/jquery.dataTables.js"/>'></script>
<script type="text/javascript" charset="utf-8" language="javascript" src='<s:url value="/js/dataTables/js/FixedHeader.min.js"/>'></script>

<script type="text/javascript" src='<s:url value="/js/bootstrap/bootstrap-3.1.1.min.js"/>'></script>
<script type="text/javascript" src='<s:url value="/js/bootstrap/bootstrap-multiselect.js"/>'></script>

<script type="text/javascript" src='<s:url value="/js/chosen/chosen.jquery.js"/>'></script>
<script type="text/javascript" src='<s:url value="/js/jquery.mCustomScrollbar.concat.min.js"/>'></script>

<s:if test="%{nameSpace !='publicData'}">
	<script type="text/javascript" src='<s:url value="/js/sessionHandler.js"/>'></script>
</s:if>

<script type="text/javascript" src='<s:url value="/js/jquery.form.min.js"/>'></script>

<!-- Messaging JS -->
<script type="text/javascript" src='<s:url value="/js/ibis/jquery.ibisMessaging-0.1.full.js"/>'></script>
<script type="text/javascript" src='<s:url value="/js/ibis/jquery.bricsDialog.js"/>'></script>

<!-- Proforms DataTables -->
<script type="text/javascript" src='<s:url value="/js/ibis/core_libs.min.js"/>'></script>
<script type="text/javascript" src='<s:url value="/js/ibis/core_classes.min.js"/>'></script>
<%-- <script type="text/javascript" src='<s:url value="/js/ibis/jquery.ibisDataTables-2.0.min.js"/>'></script> --%>

<script type="text/javascript" src='<s:url value="/js/ibis/OpButtonController.js"/>'></script>
<script type="text/javascript" src='<s:url value="/js/ibis/DataTableLanguages.js"/>'></script>
<script type="text/javascript" src='<s:url value="/js/ibis/Row.js"/>'></script>
<script type="text/javascript" src='<s:url value="/js/ibis/TableCol.js"/>'></script>
<script type="text/javascript" src='<s:url value="/js/ibis/Rows.js"/>'></script>
<script type="text/javascript" src='<s:url value="/js/ibis/TableCols.js"/>'></script>
<script type="text/javascript" src='<s:url value="/js/ibis/DataTable.js"/>'></script>
<script type="text/javascript" src='<s:url value="/js/ibis/DataTables.js"/>'></script>
<script type="text/javascript" src='<s:url value="/js/ibis/DataTableView.js"/>'></script>
<script type="text/javascript" src='<s:url value="/js/ibis/DataTableViews.js"/>'></script>
<script type="text/javascript" src='<s:url value="/js/ibis/IBISDataTables.js"/>'></script>

<!-- New Proforms DataTables -->
<!-- <script type="text/javascript" src="https://cdn.datatables.net/v/dt/jszip-3.1.3/pdfmake-0.1.27/dt-1.10.15/b-1.3.1/b-colvis-1.3.1/b-flash-1.3.1/b-html5-1.3.1/b-print-1.3.1/datatables.min.js"></script> -->
<script type="text/javascript" src="<s:url value="/js/dataTables/2.0/lib/datatables.min.js"/>"></script>
<!-- <script type="text/javascript" src="https://cdn.datatables.net/select/1.2.2/js/dataTables.select.min.js"></script> -->
<script type="text/javascript" src='<s:url value="/js/dataTables/2.0/lib/dataTables.select.min.js"/>'></script>
<script type="text/javascript" src='<s:url value="/js/dataTables/2.0/jqfactory-03.js"/>'></script>
<script type="text/javascript" src='<s:url value="/js/dataTables/2.0/idtTableWidget.js"/>'></script>
<script type="text/javascript" src='<s:url value="/js/dataTables/2.0/fnDtFilterPlugin.js"/>'></script>
<script type="text/javascript" src='<s:url value="/js/dataTables/2.0/idtSelectSetPlugin.js"/>'></script>
<script type="text/javascript" src='<s:url value="/js/dataTables/2.0/idtSearchColumnPlugin.js"/>'></script>
<script type="text/javascript" src='<s:url value="/js/dataTables/2.0/idtTableActions.js"/>'></script>
<script type="text/javascript" src='<s:url value="/js/dataTables/2.0/idtApi.js"/>'></script>
<script type="text/javascript">$.fn.bootstrapBtn = $.fn.button.noConflict()</script>
