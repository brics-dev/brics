<jsp:include page="/common/doctype.jsp" />
<%@ taglib uri="/struts-tags" prefix="s"%>
<%@ taglib uri="/WEB-INF/display.tld" prefix="display" %>
<%@ taglib uri="/WEB-INF/security.tld" prefix="security" %>

<%-- CHECK PRIVILEGES --%>
<security:check privileges="doublekeyresolution,dataentryoversight,dataentryreassign"/>

<s:bean name="gov.nih.nichd.ctdb.util.common.SystemPreferences" var="systemPreferences" />

<html>
    <head>
        <title><s:text name="app.title" /></title>
        <link rel="stylesheet" charset="UTF-8" type="text/css" href="<s:property value="#systemPreferences.get('app.stylesheet')"/>">
    </head>
    <body>
    <table border=0 width="100%">
        <tr>
            <td class="pageTitle"><s:text name="response.viewassignment.title"/></td>
        </tr>
		<jsp:include page="/response/dataEntryHeader.jsp" />
        <tr>
            <td>
                <display:table border="0" width="100%" cellpadding="4" cellspacing="1" name="dataentryassignmentlist" scope="request"
                    	decorator="gov.nih.nichd.ctdb.response.tag.ViewAssignmentDecorator">
                    <display:setProperty name="basic.msg.empty_list" value="There is no assignment history to display at this time."/>
                    <display:column align="center" property="dataEntryFlag" title="Data Entry Session"/>
                    <display:column property="previousByName" title="Previous User"/>
                    <display:column property="currentByName" title="Current User"/>
                    <display:column property="assignedByName" title="Re-Assigned By"/>
                    <display:column align="center" property="assignedDate" title="Re-Assigned Date" />
                </display:table>
            </td>
        </tr>
    </table>
    </body>
</html>