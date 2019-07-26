<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>

<c:set var="portalUrl"><spring:eval expression="@applicationProperties.getProperty('portal.url')" /></c:set>
<jsp:directive.include file="includes/top.jsp" />
	<div id="status" class="errors" >
		<h2>Expired Password</h2>
		<p>Your password has expired. Please click the link below and follow the instructions to change it.</p>
		<p><a href="<c:out value="${portalUrl}" />/portal/publicAccounts/passwordRecoveryAction!input.action">Click here to change your password.</a></p>
	</div>
<jsp:directive.include file="includes/bottom.jsp" />