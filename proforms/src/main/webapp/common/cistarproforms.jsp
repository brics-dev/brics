<jsp:include page="/common/doctype.jsp" />
<%@ taglib uri="/struts-tags" prefix="s"%>

<%-- 
This file is to choose what label to draw for all instances of what was once the word "proforms" across the site.
This file allows us to choose which word to put on the page.  It displays ONLY the textural word "ProFoRMS" or 
"CiSTAR" based on the value of the guid_with_non_pii value in proforms.properties.
 --%>

<s:set var="piiSwitch" value="#systemPreferences.get('guid_with_non_pii')" />

<s:if test="#piiSwitch == 1">
	<s:text name="application.name.nonPii" />
</s:if>
<s:else>
	<s:text name="application.name.pii" />
</s:else>
