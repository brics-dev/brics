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

<%@include file="/common/templateCss.jsp" %>

<style>
	body {background: #FFFFFF;}
</style>