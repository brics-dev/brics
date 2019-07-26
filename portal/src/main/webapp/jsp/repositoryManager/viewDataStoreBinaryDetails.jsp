<%@include file="/common/taglibs.jsp"%>
<title>Data Store <c:out value="${currentDataStoreInfo.id}" /></title>


<!-- begin .border-wrapper -->
<div class="border-wrapper wide">
	<jsp:include page="../navigation/dataRepositoryNavigation.jsp" />
	<h1 class="float-left">Data Repository</h1>
	<div style="clear:both"></div>

	<!--begin #center-content -->
	<div id="main-content">
		<h2>Data Store <c:out value="${currentDataStoreInfo.id}" /></h2>
	
		<div class="form-output">
			<div class="label">Federated:</div>
			<div class="readonly-text">
				<c:out value="${currentDataStoreInfo.federated}" />
			</div>
		</div>
	
		<div class="form-output">
			<div class="label">Server:</div>
			<div class="readonly-text">
				<c:out value="${currentDataStoreInfo.binaryDataStore.datafileEndpointInfo.url}" />
			</div>
		</div>
		<div class="form-output">
			<div class="label">Port:</div>
			<div class="readonly-text">
				<c:out value="${currentDataStoreInfo.binaryDataStore.datafileEndpointInfo.port}" />
			</div>
		</div>
		<div class="form-output">
			<div class="label">Username:</div>
			<div class="readonly-text">
				<c:out value="${currentDataStoreInfo.binaryDataStore.datafileEndpointInfo.userName}" />
			</div>
		</div>
		<div class="form-output">
			<div class="label">Password:</div>
			<div class="readonly-text">
				<c:out value="${currentDataStoreInfo.binaryDataStore.datafileEndpointInfo.password}" />
			</div>
		</div>
		<div class="form-output">
			<div class="label">Path:</div>
			<div class="readonly-text">
				<c:out value="${currentDataStoreInfo.binaryDataStore.path}" />
			</div>
		</div>
	
	
		<h2>Files:</h2>
		<c:choose>
			<c:when test="${empty currentDataStoreInfo.binaryDataStore.files}">
				<p>No files</p>
			</c:when>
			<c:otherwise>
				<table class="display-data">
					<tr>
						<th>Name</th>
						<th>Description</th>
						<th>User ID</th>
					</tr>
					<c:forEach items="${currentDataStoreInfo.binaryDataStore.files}" var="file">
						<tr>
							<td><c:out value="${file.name} " /></td>
							<td><c:out value="${file.description} " /></td>
							<td><c:out value="${file.userId} " /></td>
						</tr>
					</c:forEach>
				</table>
			</c:otherwise>
		</c:choose>
	
		<div class="button">
			<input type="button" value="Close" onClick="backToList()" />
		</div>
	</div>
</div>

<script type="text/javascript">
setNavigation({"bodyClass":"primary", "navigationLinkID":"dataRepositoryModuleLink", "subnavigationLinkID":"contributeDataToolsLink", "tertiaryLinkID":"repositoryList"});
	
	function backToList(){
		window.location = '/portal/repositoryAdmin/dataStoreInfoAction!list.action';
	}
</script>