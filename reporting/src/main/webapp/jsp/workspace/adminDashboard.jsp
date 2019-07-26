<title>Admin Dashboard</title>
<%@include file="/common/taglibs.jsp"%>

<div class="clear-float">
	<h1 class="float-left">Workspace</h1>
</div>

<div class="border-wrapper">
	<jsp:include page="navigation/workspaceNavigation.jsp" />
	<div id="main-content">
		<h2>Admin Dashboard</h2>
		<p>The administrative dashboard represents your accessible tools where you have administrative management privileges.</p>
		<br />

		<s:if test="modulesAccountEnabled">
			<sec:authorize access="hasAnyRole('ROLE_ADMIN', 'ROLE_ACCOUNT_ADMIN')">
				<h3>Account Management Module</h3>
				<br />
				<p>
					<a href="<s:url action="accountAction!list.action?filter=3" namespace="/accountAdmin" />"><s:property
							value="newAccountCount" /> New User Requests</a>
				</p>
				<p>
					<a href="<s:url action="accountAction!list.action?filter=5" namespace="/accountAdmin" />"><s:property
							value="withdrawnRequests" /> Withdrawn Account Requests</a>
				</p>
				<p>
					<a href="<s:url action="accountAction!list.action?filter=4" namespace="/accountAdmin" />"><s:property
							value="pendingAccountCount" /> Pending Account Changes</a>
				</p>
				<br />
			</sec:authorize>
		</s:if>

		<s:if test="modulesDDTEnabled">
			<sec:authorize access="hasAnyRole('ROLE_ADMIN', 'ROLE_DICTIONARY_ADMIN')">
				<h3>Data Dictionary Module</h3>
				<br />
				<p>
					<a href="<s:url action="listDataStructureAction!list.action?filter=1" namespace="/dictionaryAdmin" />"><s:property
							value="pendingDSCount" /> Form Structure Approval Requests</a>
				</p>
				<p>
					<a href="<s:url action="searchDataElementAction!list.action" namespace="/dictionaryAdmin" />"><s:property
							value="pendingDECount" /> Data Element Approval Requests</a>
				</p>
				<br />
			</sec:authorize>
		</s:if>

		<s:if test="modulesSTEnabled">
			<sec:authorize access="hasAnyRole('ROLE_ADMIN', 'ROLE_STUDY_ADMIN')">
				<h3>Repository Manager Module</h3>
				<br />
				<p>
					<a href="<s:url action="studyAction!list.action?filter=2" namespace="/studyAdmin" />"><s:property
							value="newStudyCount" /> New Study Requests</a>
				</p>
				<p>
					<a href="<s:url action="datasetAction!list.action?filter=13" namespace="/studyAdmin" />"><s:property
							value="deleteDatasetCount" /> Data Set Deletion Requests</a>
				</p>
				<p>
					<a href="<s:url action="datasetAction!list.action?filter=11" namespace="/studyAdmin" />"><s:property
							value="shareDatasetCount" /> Data Set Sharing Requests</a>
				</p>
				<p>
					<a href="<s:url action="datasetAction!list.action?filter=12" namespace="/studyAdmin" />"><s:property
							value="archiveDatasetCount" /> Data Set Archive Requests</a>
				</p>
				<p>
					<a href="<s:url action="datasetAction!list.action?filter=6" namespace="/studyAdmin" />"><s:property
							value="errorDatasetCount" /> Data Set Errors</a>
				</p>
			</sec:authorize>
		</s:if>

	</div>
</div>
<script type="text/javascript" src='<s:url value="/js/search/dataStructureSearch.js"/>'></script>
<script type="text/javascript">

// This loads the ALL filter to begin and sets results to page 1
	$('document').ready(function() {
		
	});
 					
	setNavigation({"bodyClass":"primary", "navigationLinkID":"workspaceModuleLink", "subnavigationLinkID":"workspaceLink", "tertiaryLinkID":"adminOverview"});
</script>