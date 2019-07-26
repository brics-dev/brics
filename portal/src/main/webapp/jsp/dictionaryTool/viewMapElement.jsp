<%@include file="/common/taglibs.jsp"%>

<s:set var="currentMapElement" value="currentMapElement" />
<s:set var="user" value="user" />

<jsp:include page="viewDataElement.jsp" />

<h3>Additional Information</h3>
<label class="label">Position: </label>
<s:property value="currentMapElement.position" />
<br />
<label class="label">Section: </label>
<s:property value="currentMapElement.section" />
<br />
<label class="label">Required: </label>
<s:property value="currentMapElement.value" />
<br />
<label class="label">Aliases: </label>
<s:iterator var="alias" value="currentMapElement.aliasList" status="status">
	<c:out value="${alias.name}" />
	<s:if test="!#status.last">;</s:if>
</s:iterator>
<br />
