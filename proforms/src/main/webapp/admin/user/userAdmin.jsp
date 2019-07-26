<jsp:include page="/common/doctype.jsp" />
<%@ taglib uri="/WEB-INF/struts-bean.tld" prefix="bean" %>
<%@ taglib uri="/WEB-INF/struts-html.tld" prefix="html" %>
<%@ taglib uri="/WEB-INF/struts-logic.tld" prefix="logic" %>
<%@ taglib uri="/WEB-INF/display.tld" prefix="display" %>
<%@ taglib uri="/WEB-INF/security.tld" prefix="security" %>

<%-- CHECK PRIVILEGES --%>
<security:check privileges="sysadmin"/>

<jsp:useBean id="user" scope="session" class="gov.nih.nichd.ctdb.security.domain.User"/>

<html:html>

<%-- Include Header --%>
<jsp:include page="/common/header.jsp" />

<%-- Presentation Logic Only Below--%>

<table width="100%" border="0" cellpadding="0" cellspacing="0">
    <tr>
        <td class="pageTitle"><bean:message key="user.title.display"/></td>
    </tr>
	<logic:messagesPresent message="true">
        <tr><td><img src="<%= imageRoot %>/spacer.gif" width="1" height="10" alt="" border="0"/></td></tr>
        <tr>
            <td class="confirmationText">
                <html:messages id="confirmation" message="true">
                    <bean:write name="confirmation"/>
                </html:messages>
            </td>
        </tr>
    </logic:messagesPresent>
	<jsp:include page="/common/validationErrors.jsp" />
	<tr>
		<td><img src="<%= imageRoot %>/spacer.gif" width="1" height="10" alt="" border="0"/></td>
	</tr>	
    <tr><td height="10"><img src="<%= imageRoot %>/spacer.gif" width="1" height="10" alt="" border="0"/></td></tr>
    <tr>
        <td align="center">
            <display:table border="0" width="100%" cellpadding="4" cellspacing="1"
                           name="users" scope="request"
                           decorator="gov.nih.nichd.ctdb.security.tag.UserDecorator">
                <display:setProperty name="basic.msg.empty_list" value="There are no Users in the system at this time."/>
                <display:column property="lastName" title="Last Name"/>
				<display:column property="firstName" title="First Name"/>
				<display:column property="username" title="Username"/>
                <display:column property="actions" title="System Administrator" align="center" width="5%" nowrap="true"/>
            </display:table>
        </td>
    </tr>
</table>

<%-- Include Footer --%>
<jsp:include page="/common/footer.jsp" />
</html:html>