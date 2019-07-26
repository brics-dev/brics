<%@include file="/common/taglibs.jsp"%>
<title>Username Recovery Confirmation</title>

<div class="clear-float">
	<h1 class="float-left">Account Management</h1>
</div>

<div class="border-wrapper">
	<div id="main-content">
		<h2>Username Recovery Confirmation</h2>
		<p>Thank you for your request. You will receive an email shortly containing your username. If you do not receive an email within the next 5-10 minutes, please contact an administrator.</p> 
		
		<div class="action-button">
			<s:a action="baseAction" method="landing" namespace="/accounts">Log In</s:a>
		</div>
		<div class="button margin-left">
			<input type="button" onClick="javascript:window.location.href='/${portalRoot}/'" value="Return Home" />
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