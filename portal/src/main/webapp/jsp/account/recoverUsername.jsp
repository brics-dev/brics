<%@include file="/common/taglibs.jsp"%>
<title>Username Recovery</title>

<div class="clear-float">
	<h1 class="float-left">Account Management</h1>
</div>

<div class="border-wrapper">
	<div id="main-content">
		<s:form id="theForm" method="post" name="userNameRecoveryValidationAction" validate="true" enctype="multipart/form-data">
			<s:token />
			<h2>Username Recovery</h2>
			<p>Enter the email address associated with your account and your username will be emailed to you.</p>

			<div class="form-field">
				<label for="email" class="required">Email <span class="required">* </span>:</label>
				<s:textfield id="email" name="email" cssClass="textfield required" maxlength="100" escapeHtml="true" escapeJavaScript="true" />
				<s:fielderror fieldName="email" />
			</div>

			<div class="form-field clear-left">
				<div class="button">
					<input type="button" value="Recover Username" onClick="javascript:submitForm('userNameRecoveryValidationAction!submit.action')" />
				</div>
				<s:a cssClass="form-link" action="baseAction" method="landing" namespace="/accounts">Cancel</s:a>
<%-- 			<a class="form-link" href="/${portalRoot}/jsp/login.jsp">Cancel</a> --%>
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