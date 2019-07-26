<%@ tag body-content="scriptless"%>
<%@ attribute name="dataStoreInfos" type="java.util.Collection" required="true"%>
<%@ attribute name="modulesDDTURL" type="java.lang.String" required="false"%>

<%@taglib prefix="s" uri="/struts-tags"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn"%>
<%@taglib prefix="ndar" tagdir="/WEB-INF/tags"%>

<table class="display-data full-width">
	<tr>
		<!-- 	An array of the table headers. The first value in each pair is the name of the header and the second value is the string that will be used to sort the hibernate results -->
		<s:set var="columns"
			value="{{'Datastore ID', 'id'}, {'Form Structure', 'dataStructureId'}, {'Type', 'tabular'}, {'Federated', 'federated'}, {'Archived', 'archived'}}" />

		<s:iterator var="name" value="#columns">
			<th>
				<div class="no-wrap">
					<s:if test="#name[1] != ''">
						<a href="javascript:repositorySetSort('<s:property value='#name[1]' />')"> <s:property value="#name[0]" />
						</a>
					</s:if>
					<s:else>
						<s:property value="#name[0]" />
					</s:else>
					<s:if test="#name[1] == sort">
						<s:if test="ascending">
							<img src='<s:url value="/images/brics/common/icon-down.png"/>'>
						</s:if>
						<s:else>
							<img src='<s:url value="/images/brics/common/icon-up.png"/>'>
						</s:else>
					</s:if>
				</div>
			</th>
		</s:iterator>
	</tr>
	<s:iterator var="repository" value="dataStoreInfos" status="status">
		<c:choose>
			<c:when test="${status.count%2 == 0}">
				<tr class="stripe">
			</c:when>
			<c:otherwise>
				<tr>
			</c:otherwise>
		</c:choose>
		<td>
		${id}
		
		</td>
		<td>
		<s:if test="isDictionaryAdmin">
		<a
			href="${modulesDDTURL}dictionary/dataStructureAction!view.action?dataStructureId=<s:property value="#repository.dataStructureId" />&repository=1">
				<s:property value="#repository.dataStructureId" />
		</a>
		</s:if> <s:else>
				<s:property value="#repository.dataStructureId" />
			</s:else>
		
		</td>
		<td><s:if test="#repository.tabular">Tabular</s:if> <s:else>Binary</s:else></td>
		<td><s:if test="#repository.federated">Yes</s:if> <s:else>No</s:else></td>
		<td><s:if test="#repository.archived">Yes</s:if> <s:else>No</s:else></td>
		</tr>
		<!-- Warning due to <tr> tag being created inside a choose tag -->
	</s:iterator>
</table>
<script type="text/javascript">

</script>