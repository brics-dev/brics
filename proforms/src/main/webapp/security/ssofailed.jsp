<jsp:include page="/common/doctype.jsp" />
<%@ taglib uri="/struts-tags" prefix="s"%>


<html>
<%-- Include Header --%>
<jsp:include page="/common/header_struts2.jsp" />

<%-- Presentation Logic Only Below--%>

You were successfully logged in to the Single Sign On Service.  However, your user account
for this module could not be verified.  Please contact your system administrator for help.
<br><br>
<a href="<s:property value="#webRoot"/>/logout">Return to the Log-In Page</a>

<%-- Include Footer --%>
<jsp:include page="/common/footer_struts2.jsp" />
</html>