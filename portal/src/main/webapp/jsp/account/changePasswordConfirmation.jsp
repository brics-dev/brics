<%@include file="/common/taglibs.jsp"%>
<title>Change Password Confirmation</title>

<div class="clear-float">
	<h1 class="float-left">Account Management</h1>
</div>

<div class="border-wrapper">
	<div id="main-content">
		<h2>Change Password Confirmation</h2>
		<p>Your password has been successfully changed. You may now log into the system with the new password. </p>

		<div class="action-button">
			<s:a action="baseAction" method="landing" namespace="/accounts">Log In</s:a>
		</div>
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