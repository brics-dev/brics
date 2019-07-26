<jsp:directive.include file="default/ui/includes/top.jsp" />
  <c:set var="opsEmail"><spring:eval expression="@applicationProperties.getProperty('ops.email')" /></c:set>
  <div id="msg" class="errors">
    <h2><spring:message code="screen.accountlocked.heading" /></h2>
    <p><spring:message code="brics.accountlocked.message" arguments="${opsEmail}"/></p>
  </div>
<jsp:directive.include file="default/ui/includes/bottom.jsp" />