<jsp:include page="/common/doctype.jsp" />
<%@ page import="gov.nih.nichd.ctdb.common.CtdbConstants" %>
<%@ page isErrorPage="true"%>

<%@ taglib uri="/struts-tags" prefix="s"%>

<jsp:useBean id="user" scope="session" class="gov.nih.nichd.ctdb.security.domain.User"/>

<html>
<%-- Include Header --%>
<jsp:include page="/common/header_struts2.jsp" />

<%-- Presentation Logic Only Below--%>

	<h3><s:text name="errors.selectStudy" /></h3> 
	

<%-- Include Footer --%>
<jsp:include page="/common/footer_struts2.jsp" />
</html>