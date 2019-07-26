<%@include file="/common/taglibs.jsp"%>

<table class="formLayoutTable">
	<tr>
		<td></td>
		<td></td>
	</tr>

	<tr>
		<td><s:textfield name="dataStructureForm.title" label="Title" /></td>
	</tr>
	<tr>
		<s:if test="%{currentDataStructure.id == null}">
			<td><s:textfield name="createDataStructureForm.shortName" label="Short Name" /></td>
		</s:if>
		<s:else>
			<td class="tdLabel"><label class="label">Short Name:</label></td>
			<td><s:property value="currentDataStructure.shortName" /></td>
		</s:else>
	</tr>
	<tr>
		<td class="tdLabel"><label class="label">Version:</label></td>
		<td><s:property value="currentDataStructure.version" /></td>
	</tr>
	<tr>
		<td class="tdLabel"><label class="label">Status:</label></td>
		<td><s:property value="currentDataStructure.status.type" /></td>
	</tr>
</table>