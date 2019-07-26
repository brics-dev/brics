<title>Request Additional Privileges</title>
<%@include file="/common/taglibs.jsp"%>

<div class="clear-float">
	<h1 class="float-left">Account Management</h1>
</div>

<div class="border-wrapper">

	<jsp:include page="../navigation/userManagementNavigation.jsp" />

	<div id="main-content">
		<h2>Request Additional Privileges</h2>
		
		<!-- Output Existing Privileges -->
		<jsp:include page="includes/existingPrivileges.jsp" />
		
		<s:form id="theForm" action="accountAction" cssClass="validate" method="post" validate="true" enctype="multipart/form-data">
			<s:token />
			<jsp:include page="includes/requestAdditionalPrivileges.jsp" />
			
			<div class="form-field clear-left">
				<div class="button">
					<input type="submit" id="submitPrivBtn" value="Request Privileges" />
				</div>
				<a class="form-link" href="javascript:window.location.href='/portal/accounts/viewProfile!view.action'">Cancel</a>
			</div>
		</s:form>
		
	</div>
</div>
<!-- end of .border-wrapper -->

<script type="text/javascript" src="/portal/js/account.js"></script>

<script type="text/javascript">
	/** 
	 * Account role expiration date constructer.
	 *
	 * @param roleType - The role type or role ID.
	 * @param exprDate - The expiration date of the given role.
	 */
	function RoleExpiration(roleType, exprDate) {
		this.roleTypeId = roleType;
		this.expirationDate = exprDate;
	}
	
	$("document").ready(function() {
		// Sets the navigation menus on the page
		setNavigation({
			"bodyClass" : "primary", 
			"navigationLinkID" : "userManagementModuleLink", 
			"subnavigationLinkID" : "userManagementToolLink", 
			"tertiaryLinkID" : "requestPrivilegesLink"
		});
		
		// Handler for submission button's click event.
		$("#submitPrivBtn").click(function() {
			// Change the action that the form will take when submitted.
			$("#theForm").attr("action", "accountPrivilegesUploadValidationAction!submitPrivilegeRequest.action");
			enableAllPrivileges();
			// Prepare all of the expiration date fields in the included requestAdditionalPrivileges.jsp for submission.
			processHiddenExpireDateFields();
			convertExpirationDatesToJSON();
		});
	});
</script>