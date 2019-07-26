<%@include file="/common/taglibs.jsp"%>
<title>Data Store Details</title>

<!-- begin .border-wrapper -->
<div class="border-wrapper wide">
	<jsp:include page="../navigation/dataRepositoryNavigation.jsp" />
	<h1 class="float-left">Data Repository</h1>
	<div style="clear:both"></div>
	
	<!--begin #center-content -->
	<div id="main-content">

		<h2>Data Store Details</h2>
		<div class="form-output">
			<div class="label">Form Structure ID:</div>
			<div class="readonly-text">
				<s:property value="currentDataStructure.id" />
			</div>
		</div>
		<div class="form-output">
			<div class="label">Form Structure Name:</div>
			<div class="readonly-text">
				<s:property value="currentDataStructure.title" />
			</div>
		</div>
		<div class="form-output">
			<div class="label">Version:</div>
			<div class="readonly-text">
				<s:property value="currentDataStructure.version" />
			</div>
		</div>
		<div class="form-output">
			<div class="label">Federated:</div>
			<div class="readonly-text">
				<s:property value="currentDataStoreInfo.federated" />
			</div>
		</div>
		<div class="form-output">
			<div class="label">Archived:</div>
			<div class="readonly-text">
				<s:property value="currentDataStoreInfo.archived" />
			</div>
		</div>

		<c:forEach items="${currentDataStoreInfo.dataStoreTabularInfos}" var="table">
			<h2>
				Table:
				<c:out value="${table.tableName}" />
			</h2>


			<table class="display-data">
				<tr>
					<th>Data Element Id</th>
					<th>Column Name</th>
					<th>Column Type</th>
				</tr>
				<c:forEach items="${table.columnInfos}" var="column">
					<tr>
						<td><c:out value="${column.dataElement.dataElementId}" /></td>
						<td><c:out value="${column.columnName}" /></td>
						<td><c:out value="${column.columnType}" /></td>
					</tr>
				</c:forEach>
			</table>
		</c:forEach>

		<div class="button">
			<input type="button" value="Close"
				onClick="javascript: window.location = '/portal/repositoryAdmin/dataStoreInfoAction!list.action'" />
		</div>

	</div>
</div>
<script type="text/javascript">
setNavigation({"bodyClass":"primary", "navigationLinkID":"dataRepositoryModuleLink", "subnavigationLinkID":"contributeDataToolsLink", "tertiaryLinkID":"repositoryList"});
</script>