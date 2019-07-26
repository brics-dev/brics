<%@ taglib uri="/struts-tags" prefix="s"%>
<html>
<jsp:include page="/common/iframeHeader_struts2.jsp" />

success
<script type="text/javascript">
$(document).ready(function() {
	var startTime = <%= (Long)session.getAttribute("localTimeoutRefreshLast") %>;
	top.startTime = startTime;
});

</script>
<%-- Include Footer --%>
<jsp:include page="/common/footer_struts2.jsp" />
</html>