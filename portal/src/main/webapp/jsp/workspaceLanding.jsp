<title>Module Overview</title>

<%@include file="/common/taglibs.jsp"%>
<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@taglib prefix="decorator" uri="http://www.opensymphony.com/sitemesh/decorator"%>
<%@taglib prefix="page" uri="http://www.opensymphony.com/sitemesh/page"%>
<%@taglib prefix="s" uri="/struts-tags"%>

<div id="top"></div>
<div class="clear-float">
	<h1 class="float-left">Workspace</h1>
</div>

<div class="border-wrapper">

	<!-- 		Left menus -->
	<jsp:include page="navigation/workspaceNavigation.jsp" />

	<!-- 		The pages main content -->
	<div id="main-content" class="brics-modules">

		<h2>Modules: Building Blocks for the Research Life Cycle</h2>
		<p>There are a variety of key software modules comprising this system. These modules support the vision of
				interconnectivity and collaboration among the research communities, as well as, provide a combination of web-based
				functionality and downloadable tools that support data definition, data contribution, and data access throughout the
				research life cycle.</p>
		
		<div class="list-grid thirds" style="margin: 0 0 30px 0;">
			<s:if test='modulesPFURL != ""'>
				<div class="list-grid-module">
					<sec:authorize access="hasAnyRole('ROLE_ADMIN', 'ROLE_PROFORMS_ADMIN', 'ROLE_PROFORMS')">
						<div class="brics-tool proforms wider-margin">
							<a href="${modulesPFURL}" title="
								<ul>
									<li>Create &amp; manage protocols</li>
									<li>Enroll &amp; schedule subjects</li>
									<li>Create or Reuse eCRFs</li>
									<li>Collect subject data</li>
								</ul>
							">ProFoRMS</a>
							ProFoRMS
						</div>
					</sec:authorize>
					<sec:authorize access="!hasAnyRole('ROLE_ADMIN', 'ROLE_PROFORMS_ADMIN', 'ROLE_PROFORMS')">
						<div class="brics-tool proforms missingPermission wider-margin">
							<a href="#" title="
								<ul>
									<li>Create &amp; manage protocols</li>
									<li>Enroll &amp; schedule subjects</li>
									<li>Create or Reuse eCRFs</li>
									<li>Collect subject data</li>
								</ul>
							"></a>
							ProFoRMS
						</div>
					</sec:authorize>
				</div>
			</s:if>
			<s:if test='modulesGTURL != ""'>
				<div class="list-grid-module">
					<sec:authorize access="hasAnyRole('ROLE_ADMIN', 'ROLE_GUID', 'ROLE_GUID_ADMIN')">
						<div class="brics-tool guid margin-right wider-margin">
							<s:a href="%{modulesGTURL}guid/guidAction!landing.action" title="
								<ul>
									<li>Generate unique IDs for subjects (required for submission)</li>
									<li>View your existing GUIDs and where else they might have been used</li>
								</ul>
							">GUID</s:a>
							GUID
						</div>
					</sec:authorize>
					<sec:authorize access="!hasAnyRole('ROLE_ADMIN', 'ROLE_GUID', 'ROLE_GUID_ADMIN')">
						<div class="brics-tool guid missingPermission wider-margin">
							<a href="#" title="
								<ul>
									<li>Generate unique IDs for subjects (required for submission)</li>
									<li>View your existing GUIDs and where else they might have been used</li>
								</ul>
							"></a>
							GUID
						</div>
					</sec:authorize>
				</div>
			</s:if>
			<s:if test='modulesDDTURL != ""'>
				<div class="list-grid-module">
					<sec:authorize access="hasAnyRole('ROLE_ADMIN', 'ROLE_DICTIONARY_ADMIN', 'ROLE_DICTIONARY')">
						<div class="brics-tool data-dictionary wider-margin">
							<s:a href="%{modulesDDTURL}dictionary/listDataStructureAction!list.action" title="
								<ul>
									<li>Browse existing data elements, form structures, and eForms</li>
									<li>Create new unique data elements, form structures, and eForms</li>
								</ul>
							">Data Dictionary</s:a>
							Data Dictionary
						</div>
					</sec:authorize>
					<sec:authorize access="!hasAnyRole('ROLE_ADMIN', 'ROLE_DICTIONARY_ADMIN', 'ROLE_DICTIONARY')">
						<div class="brics-tool data-dictionary missingPermission wider-margin">
							<a href="#" title="
								<ul>
									<li>Browse existing data elements, form structures, and eForms</li>
									<li>Create new unique data elements, form structures, and eForms</li>
								</ul>
							"></a>
							Data Dictionary
						</div>
					</sec:authorize>
				</div>
			</s:if>
			<s:if test='modulesSTURL != ""'>
				<div class="list-grid-module">
					<sec:authorize access="hasAnyRole('ROLE_ADMIN', 'ROLE_STUDY_ADMIN', 'ROLE_STUDY')">
						<div class="brics-tool data-repo wider-margin">
							<s:a href="%{modulesSTURL}study/studyAction!list.action" title="
								<ul>
									<li>Contribute, upload, and store research data that is associated to each study</li>
									<li>Define and manage study information</li>
								</ul>
							">Data Repository</s:a>
							Data Repository
						</div>
					</sec:authorize>
					<sec:authorize access="!hasAnyRole('ROLE_ADMIN', 'ROLE_STUDY_ADMIN', 'ROLE_STUDY')">
						<div class="brics-tool data-repo missingPermission wider-margin">
							<a href="#" title="
								<ul>
									<li>Contribute, upload, and store research data that is associated to each study</li>
									<li>Define and manage study information</li>
								</ul>
							"></a>
							Data Repository
						</div>
					</sec:authorize>
				</div>
			</s:if>
			<div class="list-grid-module">
				<s:if test='modulesQTURL != ""'>
					<sec:authorize access="hasAnyRole('ROLE_ADMIN', 'ROLE_QUERY_ADMIN', 'ROLE_QUERY')">
						<div class="brics-tool query wider-margin">
							<s:a href="%{modulesQTURL}" title="
								<ul>
									<li>Search for studies and/or forms using filters and free text search</li>
									<li>Filter data by any data element and/or value</li>
									<li>Quickly and efficiently gain an understanding of data that is in the system</li>
									<li>Download data</li>
								</ul>
							">Query</s:a>
							Query
						</div>
					</sec:authorize>
					<sec:authorize access="!hasAnyRole('ROLE_ADMIN', 'ROLE_QUERY_ADMIN', 'ROLE_QUERY')">
						<div class="brics-tool query missingPermission wider-margin">
							<a href="#" title="
								<ul>
									<li>Search for studies and/or forms using filters and free text search</li>
									<li>Filter data by any data element and/or value</li>
									<li>Quickly and efficiently gain an understanding of data that is in the system</li>
									<li>Download data</li>
								</ul>
							"></a>
							Query
						</div>
					</sec:authorize>
				</s:if>
			</div>
			<s:if test='modulesMSURL != ""'>
				<div class="list-grid-module">
					<sec:authorize access="hasAnyRole('ROLE_ADMIN', 'ROLE_METASTUDY_ADMIN', 'ROLE_METASTUDY')">
						<div class="brics-tool metastudy wider-margin">
							<s:a href="%{modulesMSURL}metastudy/metaStudyListAction!list.action" title="
								<ul>
									<li>Upload and store meta study data artifacts and documentation</li>
									<li>Define and manage meta study information</li>
								</ul>
							">Meta Study</s:a>
							Meta Study
						</div>
					</sec:authorize>
					<sec:authorize access="!hasAnyRole('ROLE_ADMIN', 'ROLE_METASTUDY_ADMIN', 'ROLE_METASTUDY')">
						<div class="brics-tool metastudy missingPermission wider-margin">
							<a href="#" title="
								<ul>
									<li>Upload and store meta study data artifacts and documentation</li>
									<li>Define and manage meta study information</li>
								</ul>
							"></a>
							Meta Study
						</div>
					</sec:authorize>
				</div>
			</s:if>
			<div class="list-grid-module">
				<s:if test='modulesAccountURL != ""'>
					<div class="brics-tool user-mgmt margin-right wider-margin">
						<s:a href="%{modulesAccountURL}accounts/viewProfile!view.action" title="
							<ul>
								<li>Update your profile</li>
								<li>Change your password</li>
								<li>Upload documentation</li>
								<li>Request privileges</li>
							</ul>
						">Account Management</s:a>
						Account Management
					</div>
				</s:if>
			</div>
			<s:if test='modulesReportingURL != ""'>
				<div class="list-grid-module">
					<sec:authorize access="hasAnyRole('ROLE_ADMIN','ROLE_REPORTING_ADMIN')">
						<div class="brics-tool reporting wider-margin">
							<s:a href="%{modulesReportingURL}" title=" 
								<ul>
									<li>View Reports</li>
								</ul>
							">Reporting</s:a>
							Reporting
						</div>
					</sec:authorize>
					<sec:authorize access="!hasAnyRole('ROLE_ADMIN','ROLE_REPORTING_ADMIN')">
						<div class="brics-tool reporting missingPermission wider-margin">
							<a href="#" title="
								<ul>
									<li>View Reports</li>
								</ul>
							"></a>
							Reporting
						</div>
					</sec:authorize>
				</div>
			</s:if>
			
		</div>
	</div>
</div>

<script type="text/javascript">
	
	//Sets the navigation menus on the page
	setNavigation({
		"bodyClass" : "primary",
		"navigationLinkID" : "workspaceModuleLink",
		"subnavigationLinkID" : "workspaceLink",
		"tertiaryLinkID" : "overview"
	});
	
	$(document).ready(function(){
			$(".list-grid.thirds .list-grid-module:nth-child(4n+1)").css("clear","left");
	});

	
</script>