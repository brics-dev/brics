<%@include file="/common/taglibs.jsp"%>

<table class="display-data full-width">
	<tr>
		<th id="rowid" class="sort">Data Store ID</th>
		<th id="dataStructureId" class="sort">Form Structure</th>
		<th id="tabular" class="sort">Type</th>
		<th id="federated" class="sort">Federated</th>
		<th id="archived" class="sort">Archived</th>
	</tr>
	<s:iterator var="dataStoreInfo" value="dataStoreInfos" status="rowstatus">
		<s:if test="#rowstatus.odd == true">
			<tr>
		</s:if>
		<s:else>
			<tr class="stripe">
		</s:else>
		
			<td>${id}</td>
		

		<td><s:property value="dataStructureId" /></td>

		<s:if test="tabular">
			<td>Tabular</td>
		</s:if>
		<s:else>
			<td>Binary</td>
		</s:else>

		<s:if test="federated">
			<td>Yes</td>
		</s:if>
		<s:else>
			<td>No</td>
		</s:else>

		<s:if test="archived">
			<td>Yes</td>
		</s:if>
		<s:else>
			<td>No</td>
		</s:else>
		</tr>
	</s:iterator>
</table>