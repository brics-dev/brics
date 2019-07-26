<jsp:include page="/common/doctype.jsp" />
<%@ page import="gov.nih.nichd.ctdb.common.CtdbConstants" %>
<%@ page import="gov.nih.nichd.ctdb.common.rs, java.util.Locale" %>
<%@ taglib uri="/struts-tags" prefix="s"%>

<%
	Locale l = request.getLocale();
%>

<html>
	<jsp:include page="/selfreporting/header_selfReporting.jsp" />
	<div>
		<s:text name="selfreporting.selfreportingHome.errorMessage" />	
	</div>
<%-- Include Footer --%>
<jsp:include page="/selfreporting/footer_selfReporting.jsp" />
</html>
	