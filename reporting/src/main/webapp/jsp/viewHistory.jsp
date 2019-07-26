<%@include file="/common/taglibs.jsp"%>

<s:set var="history" value="history" />
<s:set var="userName" value="userName" />

<table class="display-data full-width">
	<tr>
		<th>Modified Date</th>
		<th>Change</th>
		<th>Modified By User Name</th>
	</tr>
	<s:iterator var="historyEntry" value="history" status="rowstatus">
		<s:if test="#rowstatus.odd == true">
		<tr>
		</s:if>
		<s:else>
			<tr class="stripe">
		</s:else>
		<td><ndar:dateTag value='${modifiedDate}' /></td>
		<td><s:property value="auditType.value" /></td>
		<td>
        <s:property value="#userName" />
		</td>
		</tr>
	</s:iterator>
</table>