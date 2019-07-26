<%@taglib prefix="s" uri="/struts-tags"%>
<%@taglib prefix="ndar" tagdir="/WEB-INF/tags"%>
<%@ taglib prefix="sec" uri="http://www.springframework.org/security/tags"%>

<div id="left-sidebar">
	<ul class="subnavigation">	
		
		<s:if test="modulesDashboardEnabled">
			<li id='workspaceLink'><s:a href="%{modulesWSURL}baseAction!landing.action">Workspace</s:a>
				<ul class="tertiary-links" style="display: none">
					<sec:authorize access="hasAnyRole('ROLE_ADMIN', 'ROLE_ACCOUNT_ADMIN', 'ROLE_DICTIONARY_ADMIN', 'ROLE_STUDY_ADMIN','ROLE_QUERY_ADMIN','ROLE_GUID_ADMIN')">
						<li id="adminOverview"><a href="baseAction!adminDashboard.action">Admin Dashboard</a></li>
					</sec:authorize>
					<li id="overview"><s:a action="baseAction" method="landing" namespace="/">Module Overview</s:a></li>
				</ul></li>
		</s:if>
	
	</ul>
</div>

