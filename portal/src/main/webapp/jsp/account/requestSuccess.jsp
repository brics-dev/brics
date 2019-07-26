<%@include file="/common/taglibs.jsp"%>
<title>Request an Account Confirmation</title>

<div class="clear-float">
	<h1 class="float-left">Account Management</h1>
</div>

<!-- begin .border-wrapper -->
<div class="border-wrapper">
	<!--begin #center-content -->
	<div id="main-content">
		<div class="clear-float">
			<h2>Request Account Confirmation</h2>
			<p>You have successfully requested an account. 
			You have temporary access to your profile in the system that will allow you to log in and see the status of your account request and any necessary actions called for by the system administrator</p>

			<div class="button">
				<input type="button" onClick="javascript:window.location.href='/${portalRoot}/'" value="Login" />
			</div>
		</div>
		<!-- end of #main-content -->
	</div>
</div>
<!-- end of .border-wrapper -->

<script type="text/javascript">
	setNavigation({
		"bodyClass" : "primary"
	});
</script>