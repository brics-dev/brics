<%-- all of the following lines are shown on one line to get rid of newlines in the html.  space them out if you need to
 --%><%@ page import="gov.nih.nichd.ctdb.common.CtdbConstants,
                 gov.nih.nichd.ctdb.security.domain.User,
                 gov.nih.nichd.ctdb.common.CtdbForm,
                 gov.nih.nichd.ctdb.protocol.domain.Protocol"
                 %><%@ 
                 taglib uri="/WEB-INF/struts-bean.tld" prefix="bean" %><%@ 
                 taglib uri="/WEB-INF/struts-html.tld" prefix="html" %><%@ 
                 taglib uri="/WEB-INF/struts-logic.tld" prefix="logic" %><%@ 
                 taglib uri="/WEB-INF/security.tld" prefix="security" %>
                
    <jsp:useBean id="systemPreferences" class="gov.nih.nichd.ctdb.util.common.SystemPreferences"/>
    <s:set var="webRoot" value="%{systemPreferences.get('app.webroot')}"/>
    <s:set var="imageRoot" value="%{systemPreferences.get('app.imageroot')}"/>
    <jsp:useBean id="user" scope="session" class="gov.nih.nichd.ctdb.security.domain.User"/>
    <s:set var="appName" value="%{systemPreferences.get('template.global.appName')}"/><%
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
<meta http-equiv="content-type" content="text/html;charset=utf-8" />
<meta http-equiv="X-UA-Compatible" content="IE=9; IE=8; IE=7; IE=EDGE" />
<meta http-equiv="pragma" content="no-cache"/>
<meta http-equiv="expires" content="Mon, 06 Jan 1990 00:00:01 GMT" />
<meta http-equiv="cache-control" content="no-cache" />
<html:base />

<!-- Load jQuery from their CDN -->
<script src="https://code.jquery.com/jquery-1.7.2.min.js" integrity="sha256-R7aNzoy2gFrVs+pNJ6+SokH04ppcEqJ0yFLkNGoFALQ=" 
		crossorigin="anonymous"></script>

<%@include file="/common/templateCss.jsp" %>
<script src="<%= webRoot %>/common/js/jquery-ui-1.10.3.custom.min.js" type="text/javascript"></script>
<script src="<%= webRoot %>/common/js/jquery.tools.min.js" type="text/javascript"></script>
<script src="<%= webRoot %>/common/js/jquery.formalize.js" type="text/javascript"></script>
<script src="<%= webRoot %>/common/js/jquery.fancybox-1.3.4.pack.js" type="text/javascript"></script>
<script src="<%= webRoot %>/common/common.js" type="text/javascript"></script>
<script src="<%= webRoot %>/common/js/jquery.ui.accordion.activationExtension.js" type="text/javascript"></script>
<script src="<%= webRoot %>/common/js/jquery.dataTables.min.js" type="text/javascript"></script>
<script src="<%= webRoot %>/common/js/jquery.cookie.js" type="text/javascript"></script>
<script src="<%= webRoot %>/common/js/jquery.dataTables.ibisExtension.js" type="text/javascript"></script>
<script src="<%= webRoot %>/common/js/jquery-ui-timepicker-addon.js" type="text/javascript"></script>
<script src="<%= webRoot %>/common/js/ibisCommon.js" type="text/javascript"></script>

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
					//else if (link.indexOf("reportingstudy") >=0 ){
						//("Please select a study to report query.");
						// false;
					//}    
					
					
				}
			});
		});
</script>
<style>
body {background: #FFFFFF;}
</style>