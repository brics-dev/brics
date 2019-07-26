<jsp:include page="/common/doctype.jsp" />
<%@ page import="gov.nih.nichd.ctdb.common.CtdbConstants" %>
<%@ page isErrorPage="true"%>

<%@ taglib uri="/struts-tags" prefix="s" %>

<html>
<%-- Include Header --%>
<jsp:include page="/common/header_struts2.jsp" />

<%-- Presentation Logic Only Below--%>


<s:text name="errors.message" />

<%-- Include Footer --%>
<jsp:include page="/common/footer_struts2.jsp" />
</html>