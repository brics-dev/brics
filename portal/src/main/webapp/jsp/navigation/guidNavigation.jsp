<%@taglib prefix="s" uri="/struts-tags"%>
<%@taglib prefix="ndar" tagdir="/WEB-INF/tags"%>
<%@ taglib prefix="sec" uri="http://www.springframework.org/security/tags"%>

<div id="left-sidebar">
	<ul class="subnavigation">

		<s:if test='modulesGTURL != ""'>
			<sec:authorize access="hasAnyRole('ROLE_ADMIN', 'ROLE_GUID', 'ROLE_GUID_ADMIN')">
				<li id='guidDataLink'><s:a href="%{modulesGTURL}guid/guidAction!landing.action">GUID Tool</s:a>
					<ul class="tertiary-links" style="display: none">
						<li id='guidOverviewLink'><s:a href="%{modulesGTURL}guid/guidAction!landing.action">GUID Overview</s:a></li>
						<sec:authorize access="hasAnyRole('ROLE_GUID')">
							<li id='createGUIDLink'><s:a href="%{modulesGTURL}guid/guidAction!create.action">Create GUIDs</s:a></li>
						</sec:authorize>
						<li id='myGuidDataLink'><s:a href="%{modulesGTURL}guid/guidAction!list.action">My GUIDs</s:a></li>
					</ul></li>
			</sec:authorize>
			<sec:authorize access="!hasAnyRole('ROLE_ADMIN', 'ROLE_GUID', 'ROLE_GUID_ADMIN')">
				<li id='guidDataLink'>
					<div class="missingPermission">GUID Tool</div>
				</li>
			</sec:authorize>
		</s:if>

		<s:if test='modulesGTURL != ""'>
			<sec:authorize access="hasAnyRole('ROLE_ADMIN', 'ROLE_GUID_ADMIN')">
				<li id="guidToolLink"><s:a href="%{modulesGTURL}guidAdmin/guidAdminAction!list.action">GUID Admin</s:a>
					<ul class="tertiary-links" style="display: none">
						<li id="listGuidsLink"><s:a href="%{modulesGTURL}guidAdmin/guidAdminAction!list.action">View All GUIDs</s:a></li>
					</ul></li>
			</sec:authorize>
		</s:if>
	</ul>
</div>