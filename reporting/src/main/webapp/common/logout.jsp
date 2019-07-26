<%@include file="/common/taglibs.jsp"%>

<div id="logout-link">
	<p>
		Welcome
 		<sec:authorize access="hasRole('ROLE_ADMIN')"> 
 			<b>Administrator, </b>
 		</sec:authorize>
		<s:property value="user.firstName" /> | 
<!-- 		<a href="/portal/baseAction!dashboard.action">Workspace</a> | -->
		
<%-- 		<sec:authorize access="hasAnyRole('ROLE_ADMIN', 'ROLE_ACCOUNT_ADMIN', 'ROLE_REPOSITORY_ADMIN', 'ROLE_DICTIONARY_ADMIN', 'ROLE_GUID_ADMIN', 'ROLE_STUDY_ADMIN', 'ROLE_QUERY_ADMIN')"> --%>
<!-- 			<a href="/portal/baseAction!adminDashboard.action">Admin Ops</a> | -->
<%-- 		</sec:authorize> --%>
		
<!-- 		<a href="/portal/accounts/accountAction!view.action">Profile</a> |  -->
		<a href="<s:url value="/logout"/>" class="logout">Log Out</a>
	</p>
</div>