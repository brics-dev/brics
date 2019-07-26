<jsp:directive.include file="default/ui/includes/top.jsp" />
	<c:set var="portalUrl"><spring:eval expression="@applicationProperties.getProperty('portal.url')" /></c:set>
	<div id="status" class="errors" >
		<h2>CAS is unavailable</h2>
		<p>There was an error trying to complete your request.  Please try logging in again.</p>
		<p><a href="<c:out value="${portalUrl}" />/portal">Return to login page.</a></p>
	</div>
<jsp:directive.include file="default/ui/includes/bottom.jsp" />
