<%@ taglib uri="/WEB-INF/struts-bean.tld" prefix="bean" %>
<%@ taglib uri="/WEB-INF/struts-html.tld" prefix="html" %>
<%@ taglib uri="/WEB-INF/struts-logic.tld" prefix="logic" %>

<logic:messagesPresent>
<!-- added by yogi to separate common msg from being repeated to address IBIS-1257 -->
	<div class="error">
	 <bean:message key="errors.header"/>
    <html:messages id="error">
        <li style="margin-left: 20px;">
        <%-- <bean:message key="errors.header"/> --%>
        <bean:write name="error" filter="false"/>
        </li>
    </html:messages>
    </div>
</logic:messagesPresent>