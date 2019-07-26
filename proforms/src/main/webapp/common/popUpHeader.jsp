<%@ page import="gov.nih.nichd.ctdb.common.CtdbConstants" %>
<%@ taglib uri="/WEB-INF/struts-bean.tld" prefix="bean" %>

<jsp:useBean id="systemPreferences" class="gov.nih.nichd.ctdb.util.common.SystemPreferences"/>
<s:set var="webRoot" value="%{systemPreferences.get('app.webroot')}"/>
<s:set var="imageRoot" value="%{systemPreferences.get('app.imageroot')}"/>

<!-- Load jQuery from their CDN -->
<script src="https://code.jquery.com/jquery-1.7.2.min.js" integrity="sha256-R7aNzoy2gFrVs+pNJ6+SokH04ppcEqJ0yFLkNGoFALQ=" 
		crossorigin="anonymous"></script>

<script type="text/javascript">
var language = "<%= request.getLocale() %>";
</script>
<jsp:include page="/common/templateCss.jsp" />
<script src="<%= webRoot %>/common/js/HashTable.js" type="text/javascript"></script>
<script src="<%= webRoot %>/common/js/jquery-ui-1.10.3.custom.min.js" type="text/javascript"></script>
<script src="<%= webRoot %>/common/js/jquery.tools.min.js" type="text/javascript"></script>
<script src="<%= webRoot %>/common/js/jquery.formalize.js" type="text/javascript"></script>
<script src="<%= webRoot %>/common/js/jquery.fancybox-1.3.4.pack.js" type="text/javascript"></script>
<script src="<%= webRoot %>/common/common.js" type="text/javascript"></script>
<script src="<%= webRoot %>/common/js/jquery.ui.accordion.activationExtension.js" type="text/javascript"></script>
<script src="<%= webRoot %>/common/js/jquery.dataTables.min.js" type="text/javascript"></script>
<script src="<%= webRoot %>/common/js/jquery.cookie.js" type="text/javascript"></script>
<script src="<%= webRoot %>/common/js/underscore-min.js" type="text/javascript"></script>
<script src="<%= webRoot %>/common/js/jquery.dataTables.ibisExtension.js" type="text/javascript"></script>
<script src="<%= webRoot %>/common/js/jquery-ui-timepicker-addon.js" type="text/javascript"></script>

<script src="<%= webRoot %>/common/js/jquery.ibisMessaging-0.1.full.js" type="text/javascript"></script>
<script src="<%= webRoot %>/common/js/ibisCommon.js" type="text/javascript"></script>
<script src="<%= webRoot %>/common/js/templateInteraction.js" type="text/javascript"></script>

<style type="text/css">
#wrap {background-image: none;}

</style>
<link rel="icon" type="image/x-icon" 
		href="${systemPreferences.commonImageBaseUrl}"/favicon.ico" />