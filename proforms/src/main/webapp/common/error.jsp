<jsp:include page="/common/doctype.jsp" />
<%@ page import="gov.nih.nichd.ctdb.common.CtdbConstants" %>
<%@page isErrorPage="true"%>

<%@ taglib uri="/WEB-INF/struts-bean.tld" prefix="bean" %>
<%@ taglib uri="/WEB-INF/struts-html.tld" prefix="html" %>

<jsp:useBean id="user" scope="session" class="gov.nih.nichd.ctdb.security.domain.User"/>
   <%
       if (request.getHeader(CtdbConstants.HTTP_HEADER_REFERER) != null &&
            request.getHeader(CtdbConstants.HTTP_HEADER_REFERER).indexOf("vbr") > 0) { %>
<jsp:forward page="../vbr/common/vbrError.jsp"/>
<%
       }
       %>
<html:html>
<%-- Include Header --%>
<jsp:include page="/common/header.jsp" />

<%-- Presentation Logic Only Below--%>


<bean:message key="errors.message" />

<%-- Include Footer --%>
<jsp:include page="/common/footer.jsp" />
</html:html>