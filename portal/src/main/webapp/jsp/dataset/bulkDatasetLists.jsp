<%@include file="/common/taglibs.jsp"%>

<!-- begin .border-wrapper -->
<div class="border-wrapper">
	<jsp:include page="../navigation/dataRepositoryNavigation.jsp" />
	<h1>Data Repository</h1>
	<div style="clear:both"></div>

	<!--begin #center-content -->
	<div id="main-content">
			<h2>Manage Datasets</h2>
			<h3>Status:${newStatus.name}</h3>
			<h4>Datasets:</h4>
			
		<jsp:include page="bulkDatasetListTable.jsp"></jsp:include>
		
		<div>
			<s:if test="%{isDatasetRequestDelete}">
							<p>
							<strong>Note:</strong>If you are attempting to delete dataset(s) with access records, 
							the deletion will not be completed for those datasets.
							To delete these datasets, please remove associated access records by contacting the system administrator.
							</p>
			</s:if>
		</div>
		
		<div>
		    <br/>
				<a class="float-right"  href="datasetAction!list.action">Return to full Dataset list</a>
		</div>
	</div>
</div>
<script type="text/javascript">
	setNavigation({
		"bodyClass":"primary", 
		"navigationLinkID":"dataRepositoryModuleLink", 
		"subnavigationLinkID":"contributeDataToolsLink", 
		"tertiaryLinkID":"datasetListLink"
		});
</script>

