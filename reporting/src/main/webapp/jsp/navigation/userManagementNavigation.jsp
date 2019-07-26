<%@taglib prefix="s" uri="/struts-tags"%>
<%@taglib prefix="ndar" tagdir="/WEB-INF/tags"%>
<%@ taglib prefix="sec" uri="http://www.springframework.org/security/tags"%>

<div id="left-sidebar">
	<ul class="subnavigation">	
		<li id='userManagementToolLink'><a href="/portal/accounts/viewProfile!view.action">Account Management</a>
			<ul class="tertiary-links" style="display: none">
				<li id='viewProfileLink'><a href="/portal/accounts/viewProfile!view.action">My Profile</a></li>
				<li id='changePasswordLink'><a href="/portal/accounts/changePassword!view.action">Change Password</a></li>
				<!-- 				<li id='uploadDocumentationLink'><a href="/portal/accounts/documentationAction!view.action">Upload Documentation</a></li> -->
				<li class="long-text" id='requestPrivilegesLink'><a href="/portal/accounts/privilegesAction!view.action">Request Additional Privileges</a></li>
			</ul>
		</li>
	
	<s:if test='modulesAccountURL != ""'>
		<sec:authorize access="hasAnyRole('ROLE_ADMIN', 'ROLE_ACCOUNT_ADMIN')">
			<li id="managingUsersLink"><s:a href="%{modulesAccountURL}accountAdmin/accountAction!list.action">Account Admin</s:a>
				<ul class="tertiary-links" style="display: none">
					<sec:authorize access="hasAnyRole('ROLE_ADMIN', 'ROLE_ACCOUNT_ADMIN')">
						<li id="accountList"><s:a href="%{modulesAccountURL}accountAdmin/accountAction!list.action">Account List</s:a></li>
					</sec:authorize>
					
					<sec:authorize access="hasAnyRole('ROLE_ADMIN', 'ROLE_ACCOUNT_ADMIN')">
						<li id="permissionGroupList" class="long-text"><s:a href="%{modulesAccountURL}accountAdmin/permissionGroupAction!list.action">Account Group List</s:a></li>
					</sec:authorize>
					
					<sec:authorize access="hasAnyRole('ROLE_ADMIN', 'ROLE_ACCOUNT_ADMIN')">
						<li id="currentUsers"><s:a href="%{modulesAccountURL}accountAdmin/accountAction!currentUsers.action">Current Users</s:a></li>
					</sec:authorize>

					<sec:authorize access="hasAnyRole('ROLE_ADMIN', 'ROLE_ACCOUNT_ADMIN')">
						<li id="createUserLink"><s:a href="%{modulesAccountURL}accountAdmin/createAction!create.action">Create User</s:a></li>
					</sec:authorize>

					<sec:authorize access="hasAnyRole('ROLE_ADMIN', 'ROLE_ACCOUNT_ADMIN')">
						<li id="createPermissionGroupLink" class="long-text"><s:a href="%{modulesAccountURL}accountAdmin/permissionGroupAction!create.action">Create Account Group</s:a></li>
					</sec:authorize>
					
					<sec:authorize access="hasAnyRole('ROLE_ADMIN', 'ROLE_ACCOUNT_ADMIN')">
						<li id="createEditAccountGuidanceEmailsLink" class="long-text"><s:a href="%{modulesAccountURL}accountAdmin/accountGuidanceEmailsAction!view.action">Create/Edit Account Guidance Emails</s:a></li>
					</sec:authorize>
				</ul></li>
		</sec:authorize>
	</s:if>
	
	<s:if test='modulesAccountURL != ""'>
		<sec:authorize access="hasAnyRole('ROLE_ADMIN','ROLE_ACCOUNT_ADMIN','ROLE_ACCOUNT_REVIEWER')">
			<li id="managingAccountsLink"><s:a href="%{modulesAccountURL}accountReviewer/accountReportsAction!renewalList.action">Account Reviewer</s:a>
				<ul class="tertiary-links" style="display: none">
					<sec:authorize access="hasAnyRole('ROLE_ADMIN','ROLE_ACCOUNT_ADMIN','ROLE_ACCOUNT_REVIEWER')">
						<li id="accountRequestLink" class="long-text"><s:a href="%{modulesAccountURL}accountReviewer/accountReportsAction!requestList.action">Account Request Dashboard </s:a></li>
					</sec:authorize>
					<sec:authorize access="hasAnyRole('ROLE_ADMIN','ROLE_ACCOUNT_ADMIN','ROLE_ACCOUNT_REVIEWER')">
						<li id="accountsForRenewalLink" class="long-text"><s:a href="%{modulesAccountURL}accountReviewer/accountReportsAction!renewalList.action">Account Renewal Dashboard </s:a></li>
					</sec:authorize>
				</ul>
			</li>
		</sec:authorize>
	</s:if>
	<s:if test='modulesOMURL != ""'>
		<li id='orderManagementToolLink'><a href="/portal/ordermanager/openQueue.action">Biosample Orders</a>
		</li>
	 </s:if> 
	</ul>
</div>
