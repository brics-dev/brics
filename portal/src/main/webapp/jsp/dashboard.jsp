<title>My Dashboard</title>

<%@include file="/common/taglibs.jsp"%>
<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@taglib prefix="decorator" uri="http://www.opensymphony.com/sitemesh/decorator"%>
<%@taglib prefix="page" uri="http://www.opensymphony.com/sitemesh/page"%>
<%@taglib prefix="s" uri="/struts-tags"%>


<div id="top"></div>
<!-- 	Header div for title and help link. (Help link launches a lightbox (function in main.js)) -->
<div class="clear-float">
	<h1 class="float-left">Workspace</h1>
</div>

<div class="border-wrapper">
	
	<!-- 		Left menus -->
	<jsp:include page="navigation/workspaceNavigation.jsp" />

	<!-- 		The pages main content -->
	<div id="main-content">

		<h2>My Dashboard</h2>
		
		<s:if test="modulesSTEnabled">
			<sec:authorize access="hasAnyRole('ROLE_ADMIN', 'ROLE_STUDY_ADMIN', 'ROLE_STUDY')">
				<h3>My Studies</h3>
				<%-- This if statement is for Josh's support of the CNRM dashboard --%>
				<%--
				<c:if test="${fn:contains(pageContext.request.requestURL, 'cnrm' )}">
					<a href="/tbidashboard/dashboard" target="_blank">View Studies Dashboard</a>
				</c:if>
				 --%>
				<div id="studyResultsId"></div>
				<p>
					<a href="<s:url action="studyAction!list.action" namespace="/study" />">Manage Studies</a>
				</p>
				<input type="hidden" class="studySelectedOwner" id="0" />
				<input type="hidden" class="studySelectedFilter" id="-1" />
				<br />
				<br />

				<h3>
					My Datasets
					<div class="back-to-top">
						<a class="back-to-top-inner" href="#top">Jump To Top</a>
					</div>
				</h3>
				<div id="datasetResultsId"></div>
				<input type="hidden" class="datasetSelectedOwner" id="0" />
				<input type="hidden" class="datasetSelectedFilter" id="-1" />
				<br />
			</sec:authorize>
		</s:if>

		<s:if test="modulesGTEnabled">
			<sec:authorize access="hasAnyRole('ROLE_ADMIN', 'ROLE_GUID')">
				<h3>
					My GUIDs
					<div class="back-to-top">
						<a class="back-to-top-inner" href="#top">Jump To Top</a>
					</div>
				</h3>
				<div id="guidResultsId">
					<%-- guidTable.jsp --%>
				</div>
				<br />
			</sec:authorize>
		</s:if>

		<s:if test="modulesDDTEnabled">
			<sec:authorize access="hasAnyRole('ROLE_ADMIN', 'ROLE_DICTIONARY_ADMIN', 'ROLE_DICTIONARY')">
				<h3>
					My Form Structures
					<div class="back-to-top">
						<a class="back-to-top-inner" href="#top">Jump To Top</a>
					</div>
				</h3>
				<div id="dataStructureResultsId"></div>
				<p>
					<a href="<s:url action="listDataStructureAction!list.action" namespace="/dictionary" />">Manage Form Structures</a>
				</p>
				<input type="hidden" class="dataStructureSelectedOwner" id="0" />
				<input type="hidden" class="dataStructureSelectedFilter" id="-1" />
			</sec:authorize>
		</s:if>
	</div>
</div>

<style type="text/css">
	#hideNoDataStudies {display: none;}
	#showAllStudies {display: none;}
</style>

<sec:authorize access="hasAnyRole('ROLE_ADMIN', 'ROLE_DICTIONARY_ADMIN', 'ROLE_DICTIONARY')">
	<s:if test="modulesDDTEnabled">
		<script type="text/javascript" src='<s:url value="/js/search/dataStructureSearch.js"/>'></script>
		<script type="text/javascript">
			$('document').ready(function() {
				dataStructureSearch();
			});
		</script>
	</s:if>
</sec:authorize>

<sec:authorize access="hasAnyRole('ROLE_ADMIN', 'ROLE_STUDY_ADMIN', 'ROLE_STUDY')">
	<s:if test="modulesSTEnabled">
		<script type="text/javascript" src='<s:url value="/js/search/studySearch.js"/>'></script>
		<script type="text/javascript">
			$('document').ready(function() {
				studyPagination.namespace = "/portal/study/";
				studySearch();
			});
		</script>
	</s:if>
</sec:authorize>

<sec:authorize access="hasAnyRole('ROLE_ADMIN', 'ROLE_STUDY_ADMIN', 'ROLE_STUDY')">
	<s:if test="modulesSTEnabled">
		<script type="text/javascript" src='<s:url value="/js/search/datasetSearch.js"/>'></script>
		<script type="text/javascript">
			$('document').ready(function() {
				datasetPagination.namespace = "/portal/study/";
				datasetSearch();
			});
		</script>
	</s:if>
</sec:authorize>

<sec:authorize access="hasAnyRole('ROLE_ADMIN', 'ROLE_GUID')">
	<s:if test="modulesGTEnabled">
		<script type="text/javascript" src='<s:url value="/js/search/guidSearch.js"/>'></script>
		<script type="text/javascript">
			$('document').ready(function() {
				guidSetMineOnly(true);
				guidSearch();
			});
		</script>
		
		<s:form name="downloadForm" id="downloadForm" action="guid/searchGuidAction!downloadGuidResults.action" method="post">
			<input type="hidden" id="downloadJsonData" name="jsonData" />
		</s:form>

	</s:if>
</sec:authorize>

<script type="text/javascript">
	//Sets the navigation menus on the page
	setNavigation({
		"bodyClass" : "primary",
		"navigationLinkID" : "workspaceModuleLink",
		"subnavigationLinkID" : "workspaceLink",
		"tertiaryLinkID" : "userDashboard"
	});
</script>
</body>
</html>



