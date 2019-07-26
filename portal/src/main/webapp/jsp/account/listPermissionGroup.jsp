<%@include file="/common/taglibs.jsp"%>
<title>Account Group List</title>

<div class="clear-float">
	<div class="clear-float">
		<h1 class="float-left">Account Management</h1>
	</div>
</div>

<div class="border-wrapper">
	<jsp:include page="../navigation/userManagementNavigation.jsp" />
	
	<div id="main-content">
		<h2>Account Group List</h2>
		<p>Account groups are listed below. Click on one to edit an account group.</p>
		<div class="clear-float">
			<ndar:permissionGroupList elementList="${permissionGroupList}" />
		</div>
	</div>
</div>

<script type="text/javascript">
	setNavigation({"bodyClass":"primary", "navigationLinkID":"userManagementModuleLink", "subnavigationLinkID":"managingUsersLink", "tertiaryLinkID":"permissionGroupList"});
</script>