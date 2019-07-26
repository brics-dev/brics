<title>Request Confirmation</title>
<%@include file="/common/taglibs.jsp"%>

<div class="clear-float">
	<h1 class="float-left">Account Management</h1>
</div>

<div class="border-wrapper">

	<!-- Left menus -->
	<jsp:include page="../navigation/userManagementNavigation.jsp" />

	<div id="main-content">
		<h3>Request Confirmation</h3>
		<p>Thank you for submitting a request to update your privileges. An administrator will review the request 
			and an email will be sent to notify you of the decision.</p>
		
		<p><strong>Please note that some permissions may require additional documentation to be uploaded.</strong> 
			Failure to supply proper documentation may result in a delay or rejection of requested privileges.</p> 
		
		<p>Please view the <a href="/portal/accounts/documentationAction!view.action">Upload Documentation</a> 
			page to verify that all documentation has been properly submitted.</p>
		
	</div>
</div>

<script type="text/javascript">
	//Sets the navigation menus on the page
	setNavigation({	"bodyClass" : "primary","navigationLinkID" : "userManagementModuleLink","subnavigationLinkID" : "userManagementToolLink","tertiaryLinkID" : "requestPrivilegesLink"});
</script>