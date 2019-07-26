<!-- THIS PAGE IS FOR PASSWORD RECOVERY -->
<!-- FOR CHANGING PASSWORD AFTER LOGGING IN SEE EDITPASSWORD.JSP -->

<%@include file="/common/taglibs.jsp"%>
<title>Change Password</title>

<div class="clear-float">
	<h1 class="float-left">Account Management</h1>
</div>

<div class="border-wrapper wide">
	<div id="main-content">

		<p>Please verify your identity with your username or email address and enter in a new password.</p>

		<s:form id="passwordRecoveryForm" action="submitpasswordRecoveryValidationAction!update" method="post" autocomplete="off">
		<s:token />
			<div class="formElements">
				<div class="form-field">
					<label for="userOrEmail" class="required">Username or Email <span class="required">* </span>:</label>
					<s:textfield name="userOrEmail" cssClass="textfield required" escapeHtml="true" escapeJavaScript="true" />
					<s:fielderror fieldName="userOrEmail" />
				</div>
				<div class="form-field">
					<label for="newPassword" class="required">New Password <span class="required">* </span>:</label>
					<s:password name="newPassword" cssClass="textfield required" autocomplete="off" escapeHtml="true" escapeJavaScript="true" />
					<s:fielderror fieldName="newPassword" />
					<div class="special-instruction">Case sensitive. 8-15 alpha/numeric characters. Must contain at least 3	different kinds of characters: Capital Letter, Lowercase letter, Numbers, and/or Special character.</div>
				</div>
				<div class="form-field">
					<label for="confirmPassword" class="required">Confirm Password <span class="required">* </span>:</label>
					<s:password name="confirmPassword" cssClass="textfield required" escapeHtml="true" escapeJavaScript="true" />
					<s:fielderror fieldName="confirmPassword" />
				</div>
				<div class="form-field">
					<input type="hidden" name="casToken" value="<s:property value="casToken"/>" />
					<s:fielderror fieldName="casToken" />
				</div>
			</div>
			<div class="form-field clear-left">
				<div class="button">
					<s:submit value="Change Password"></s:submit>
				</div>
				<s:a cssClass="form-link" action="baseAction" method="landing" namespace="/accounts">Cancel</s:a>
			</div>
		</s:form>
	</div>
</div>

<script type="text/javascript">
	setNavigation({"bodyClass":"primary"});
</script>

<script type="text/javascript">
	$('document').ready(function() {
		$("#navigation").hide();
	});
</script>