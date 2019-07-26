<%@taglib prefix="s" uri="/struts-tags"%>
<%@ taglib prefix="sec" uri="http://www.springframework.org/security/tags"%>
<%@include file="/common/taglibs.jsp"%>

<c:set var="hostname" value="${pageContext.request.serverName}"/>

<div id="navigation">
	<div class="nav-wrapper">
		<ul id="primary-nav">
			<s:if test='modulesPublicURL != ""'>
				<li id="home-link"><s:a href="%{modulesPublicURL}">
						<span>Home</span>
					</s:a></li>
			</s:if>
			<s:if test="modulesDashboardEnabled">
				<sec:authorize access="hasAnyRole('ROLE_USER')">
					<li id="workspaceModuleLink"><s:a href="%{modulesPublicURL}portal/baseAction!landing.action">Workspace</s:a></li>
				</sec:authorize>
				<sec:authorize access="!hasAnyRole('ROLE_USER')">
					<li id="workspaceModuleLink"><div class="missingPermission">Workspace</div></li>
				</sec:authorize>
			</s:if>
			<s:if test='modulesPFURL != ""'>
				<sec:authorize access="hasAnyRole('ROLE_ADMIN', 'ROLE_PROFORMS_ADMIN', 'ROLE_PROFORMS')">
					<li id="proformsModuleLink">
						<s:a href="%{modulesPFURL}">
							ProFoRMS
						</s:a>
					</li>
				</sec:authorize>
				<sec:authorize access="!hasAnyRole('ROLE_ADMIN', 'ROLE_PROFORMS_ADMIN', 'ROLE_PROFORMS')">
					<li id="proformsModuleLink">
						<div class="missingPermission">
							ProFoRMS
						</div>
					</li>
				</sec:authorize>
			</s:if>
			<s:if test='modulesGTURL != ""'>
				<sec:authorize access="hasAnyRole('ROLE_ADMIN', 'ROLE_GUID', 'ROLE_GUID_ADMIN')">
					<li id="guidModuleLink"><s:a href="%{modulesGTURL}guid/guidAction!landing.action">GUID</s:a></li>
				</sec:authorize>
				<sec:authorize access="!hasAnyRole('ROLE_ADMIN', 'ROLE_GUID', 'ROLE_GUID_ADMIN')">
					<li id='guidDataLink'>
						<div class="missingPermission">GUID</div>
					</li>
				</sec:authorize>
			</s:if>
			<s:if test='modulesDDTURL != ""'>
				<sec:authorize access="hasAnyRole('ROLE_ADMIN', 'ROLE_DICTIONARY_ADMIN', 'ROLE_DICTIONARY')">
					<li id="dataDictionaryModuleLink"><s:a href="%{modulesDDTURL}dictionary/listDataStructureAction!list.action">Data Dictionary</s:a></li>
				</sec:authorize>
				<sec:authorize access="!hasAnyRole('ROLE_ADMIN', 'ROLE_DICTIONARY_ADMIN', 'ROLE_DICTIONARY')">
					<li id='dataDictionaryToolLink'>
						<div class="missingPermission">Data Dictionary</div>
					</li>
				</sec:authorize>
			</s:if>
			<s:if test='modulesSTURL != ""'>
				<sec:authorize access="hasAnyRole('ROLE_ADMIN', 'ROLE_STUDY_ADMIN', 'ROLE_STUDY', 'ROLE_REPOSITORY_ADMIN')">
					<li id="dataRepositoryModuleLink"><s:a href="%{modulesSTURL}study/studyAction!list.action">Data Repository</s:a></li>
				</sec:authorize>
				<sec:authorize access="!hasAnyRole('ROLE_ADMIN', 'ROLE_STUDY_ADMIN', 'ROLE_STUDY', 'ROLE_REPOSITORY_ADMIN')">
					<li id='studyToolLink'>
						<div class="missingPermission">Data Repository</div>
					</li>
				</sec:authorize>
			</s:if>			
			<s:if test='modulesQTURL != ""'>
				<sec:authorize access="hasAnyRole('ROLE_ADMIN', 'ROLE_QUERY_ADMIN', 'ROLE_QUERY')">
					<li id="queryToolModuleLink"><s:a href="%{modulesQTURL}">Query</s:a></li>
				</sec:authorize>
				<sec:authorize access="!hasAnyRole('ROLE_ADMIN', 'ROLE_QUERY_ADMIN', 'ROLE_QUERY')">
					<li id="queryToolModuleLink"><div class="missingPermission">Query</div></li>
				</sec:authorize>
			</s:if>
			<s:if test='modulesMSURL != ""'>
				<sec:authorize access="hasAnyRole('ROLE_ADMIN', 'ROLE_METASTUDY_ADMIN', 'ROLE_METASTUDY')">
					<li id="metaStudyModuleLink"><s:a href="%{modulesMSURL}metastudy/metaStudyListAction!list.action">Meta Study</s:a></li>
				</sec:authorize>
				<sec:authorize access="!hasAnyRole('ROLE_ADMIN', 'ROLE_METASTUDY_ADMIN', 'ROLE_METASTUDY')">
					<li id='metaStudyModuleLink'>
						<div class="missingPermission">Meta Study</div>
					</li>
				</sec:authorize>
			</s:if>
			<s:if test='modulesAccountURL != ""'>
				<sec:authorize access="hasAnyRole('ROLE_USER')">
					<li id="userManagementModuleLink"><s:a href="%{modulesAccountURL}accounts/viewProfile!view.action">Account Management</s:a></li>
				</sec:authorize>
				<sec:authorize access="!hasAnyRole('ROLE_USER')">
					<li id="userManagementModuleLink"><div class="missingPermission">Account Management</div></li>
				</sec:authorize>
			</s:if>
			<!--  Added for reporting -->
			<s:if test='modulesReportingURL != ""'>
				<sec:authorize access="hasAnyRole('ROLE_ADMIN','ROLE_REPORTING_ADMIN')">
					<li id="reportingModuleLink"><s:a href="%{modulesReportingURL}">Reporting</s:a></li>
				</sec:authorize>
				<sec:authorize access="!hasAnyRole('ROLE_ADMIN','ROLE_REPORTING_ADMIN')">
					<li id="reportingModuleLink"><div class="missingPermission">Reporting</div></li>
				</sec:authorize>
			</s:if>
		</ul>
	</div>
</div>
