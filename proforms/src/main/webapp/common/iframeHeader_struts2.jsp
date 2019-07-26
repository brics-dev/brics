<%-- all of the following lines are shown on one line to get rid of newlines in the html.  space them out if you need to
 --%><%@ page import="gov.nih.nichd.ctdb.common.CtdbConstants,
                 gov.nih.nichd.ctdb.security.domain.User,
                 gov.nih.nichd.ctdb.common.CtdbForm,
                 gov.nih.nichd.ctdb.protocol.domain.Protocol"
                 %>
                 <%@taglib uri="/struts-tags" prefix="s" %>
                 <%@taglib uri="/WEB-INF/security.tld" prefix="security" %>
                 <jsp:useBean id="user" scope="session" class="gov.nih.nichd.ctdb.security.domain.User"/><%
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
<title><s:text name="app.title"/></title>
<meta http-equiv="content-type" content="text/html;charset=utf-8" />
<meta http-equiv="X-UA-Compatible" content="IE=9; IE=8; IE=7; IE=EDGE" />
<meta http-equiv="pragma" content="no-cache"/>
<meta http-equiv="expires" content="Mon, 06 Jan 1990 00:00:01 GMT" />
<meta http-equiv="cache-control" content="no-cache" />

<s:bean name="gov.nih.nichd.ctdb.util.common.SystemPreferences" var="systemPreferences" />
<s:set var="webRoot" value="#systemPreferences.get('app.webroot')"/>
<s:set var="imageRoot" value="#systemPreferences.get('app.imageroot')"/>


<!-- Load jQuery from their CDN -->
<script src="https://cdnjs.cloudflare.com/ajax/libs/jquery/1.12.4/jquery.min.js" crossorigin="anonymous"></script>
<!-- Fallback to jQuery on server if above script is unavailable -->
<script>window.jQuery || document.write('<script src=\'<s:property value="#webRoot"/>/common/js/jquery-1.12.4.min.js\'><\/script>')</script>

<%@include file="/common/templateCss.jsp" %>

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

<script type="text/javascript">
	var __autosave = false;
		
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
</script>
<style>
	body {background: #FFFFFF;}
</style>