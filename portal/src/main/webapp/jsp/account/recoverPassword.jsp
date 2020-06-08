<%@include file="/common/taglibs.jsp"%>
<title>Reset Password</title>

<div class="clear-float">
	<h1 class="float-left">Account Management</h1>
</div>

<div class="border-wrapper wide">
	<div id="main-content">
		<h2>Reset Password</h2>

		<%-- Message the user if this is a redirect from an expired password login attempt --%>
		<c:if test="${not empty param.login_error}">
			<div class="default-error clear-both">
				<p>Your password has expired, please use the recover password functionality to change it.</p>
			</div>
		</c:if>

		<p>Please provide your <span id="orgName"><s:property value="orgName" /></span> username or email address. A link to reset your 
			password will be sent to the email address on file.</p>

		<s:form id="passwordRecoveryForm" action="promptpasswordRecoveryValidationAction!submit" method="post">
			<s:token />
			<div class="formElements">
				<div class="form-field">
					<label for="userOrEmail" class="required">Username or Email <span class="required">* </span>:</label>
					<s:textfield name="userOrEmail" cssClass="textfield required" escapeHtml="true" escapeJavaScript="true" />
					<s:fielderror fieldName="userOrEmail" />
				</div>
			</div>
			<div class="form-field clear-left">
				<div class="button">
					<s:submit value="Reset Password"></s:submit>
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
		var orgName = $("#orgName").text();
		$("#orgName").text(orgName.replace(/_/g, ' '));
	});
</script>