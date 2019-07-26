<!-- THIS PAGE IS FOR CHANGING A PASSWORD WHILE LOGGED IN -->
<!-- FOR PASSWORD RECOVERY SEE CHANGEPASSWORD.JSP -->
<title>Change Password</title>
<%@include file="/common/taglibs.jsp"%>

<div class="clear-float">
	<h1 class="float-left">Account Management</h1>
</div>

<div class="border-wrapper">

	<!-- 		Left menus -->
	<jsp:include page="../navigation/userManagementNavigation.jsp" />

	<div id="main-content">
		<h2>Change Password</h2>
		<p>To change your password, please enter your current password, then select a new password.</p>

		<s:form id="changePasswordForm" action="passwordAccountValidationAction!changePassword" method="post" autocomplete="off">
		<s:token />
			<input type="hidden" name="availability" value="<s:property value="availability" />" />
			<input type="hidden" name="username" value="<s:property value="account.userName" />" />
			<div class="formElements">
				<div class="form-field">
					<label for="password" class="required">Current Password <span class="required">* </span>:</label>
					<s:password name="password" cssClass="textfield required" escapeHtml="true" escapeJavaScript="true" />
					<s:fielderror fieldName="password" />
				</div>
				<div class="form-field">
					<label for="newPassword" class="required">New Password <span class="required">* </span>:</label>
					<s:password name="newPassword" cssClass="textfield required" autocomplete="off" escapeHtml="true" escapeJavaScript="true" />
					<s:fielderror fieldName="newPassword" />
					<div class="special-instruction">
						Case sensitive. 8-15 alpha/numeric characters. Must contain at least 3 different kinds of 
						characters: Capital Letter, Lowercase letter, Numbers, and/or Special character.
					</div>
				</div>
				<div class="form-field">
					<label for="confirmPassword" class="required">Confirm Password <span class="required">* </span>:</label>
					<s:password name="confirmPassword" cssClass="textfield required" escapeHtml="true" escapeJavaScript="true" />
					<s:fielderror fieldName="confirmPassword" />
				</div>
			</div>
			<br />
			<s:if test="availability == null">
				<div class="button">
					<s:submit value="Change Password"></s:submit>
				</div>
				<a class="form-link" href="viewProfile!view.action">Cancel</a>
			</s:if>
			<s:else>
				<div class="button">
					<s:submit value="Change Password"></s:submit>
				</div>
				<div class="form-field">
					<a class="form-link" href="viewProfile!view.action">Cancel</a>
				</div>
			</s:else>
		</s:form>

	</div>
</div>

<script type="text/javascript">
	//Sets the navigation menus on the page
	setNavigation({
		"bodyClass" : "primary",
		"navigationLinkID" : "userManagementModuleLink",
		"subnavigationLinkID" : "userManagementToolLink",
		"tertiaryLinkID" : "changePasswordLink"
	});
</script>