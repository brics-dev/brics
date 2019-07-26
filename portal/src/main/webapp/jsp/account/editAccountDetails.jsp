<%@include file="/common/taglibs.jsp"%>

<div class="clear-float">
	<h1 class="float-left">Account Management</h1>
</div>

<div class="border-wrapper">

	<jsp:include page="../navigation/userManagementNavigation.jsp" />
	
	<div id="main-content">
		<s:if test="%{isRequest}">
			<h2>Create User</h2>
			<title>Create User</title>
		</s:if>
		<s:else>
			<div id="breadcrumb">
			<s:if test="%{inAccountReviewer}">
				<s:a href="%{modulesAccountURL}accountAdmin/accountAction!list.action">Account List</s:a>&nbsp;&gt;&nbsp;
				Account Details: <s:property value="accountDetailsForm.userName" />
			</s:if>
			<s:else>
				<s:a href="%{modulesAccountURL}accountAdmin/accountAction!list.action">Account List</s:a>&nbsp;&gt;&nbsp;
				Account Details: <s:property value="accountDetailsForm.userName" />
			</s:else>
			</div>
			<h2>Account Details</h2>
			<title>Account Details</title>
		</s:else>

		<div id="mainDiv" class="clear-float">

			<s:form id="theForm" cssClass="validate" method="post" name="accountDetailsForm" validate="true" enctype="multipart/form-data">
				<input type="hidden" name="userOrEmail" value='<s:property value="accountDetailsForm.userName"/>' id="userOrEmail"/>
				<s:token />
		
				<s:if test="%{isRequest}">
					<p>Please fill out the following information to create a new user. Users created by administrators will not need to go 
						through the approval process.</p>
					<h3>Username and Password</h3>
					<p class="required">Fields marked with a <span class="required">* </span>are required.</p>
					<div class="clear-right"></div>
					<p>Select a username and password:</p>
				</s:if>
				<s:else>
					<h3>Account Information</h3>
				</s:else>

				<s:hidden name="accountId" escapeHtml="true" escapeJavaScript="true" />
				<s:hidden name="userId" escapeHtml="true" escapeJavaScript="true" />
				<s:hidden name="nameSpace" escapeHtml="true" escapeJavaScript="true" />
				<s:hidden name="isRequest" escapeHtml="true" escapeJavaScript="true" />

				<s:if test="%{isRequest}">
					<div class="form-field">
						<label for="userName" class="required">Username <span class="required">* </span>:</label>
						<s:textfield id="userName" name="accountDetailsForm.userName" cssClass="userName textfield required" 
								maxlength="30" escapeHtml="true" escapeJavaScript="true" />
						<a href="javascript:checkAvailability()">Check Availability</a>&nbsp;
						<span class="fieldspan" id="availabilityDisplay"></span>
						<s:fielderror fieldName="accountDetailsForm.userName" />
						<div class="special-instruction">
							Must only contain alphanumeric characters (A-Z, a-z, 0-9), special chars (@,-, _, .) and 
							must start with a letter.
						</div>
					</div>
					
					<div class="form-field">
						<label for="passwordString" class="required">Password <span class="required">* </span>:</label>
						<s:password id="passwordString" name="accountDetailsForm.passwordString" cssClass="textfield required" 
								maxlength="30" escapeHtml="true" escapeJavaScript="true" autocomplete="off" />
						<s:fielderror fieldName="accountDetailsForm.passwordString" />
						<div class="special-instruction">
							Case sensitive. 8-30 alpha/numeric characters. Must contain at least 3 different kinds of 
							characters: Capital Letter, Lowercase letter, Numbers, and/or Special character.
						</div>
					</div>
					
					<div class="form-field">
						<label for="confirmPassword" class="required">Retype Password <span class="required">*</span>:</label>
						<s:password id="confirmPassword" name="accountDetailsForm.confirmPassword" cssClass="textfield required" 
								maxlength="30" escapeHtml="true" escapeJavaScript="true" autocomplete="off" />
						<s:fielderror fieldName="accountDetailsForm.confirmPassword" />
					</div>
				</s:if>
				
				<s:else>
					<s:if test="currentAccount.isLocked">
						<div class="big-error-message no-float">
							<img class="icon-warning" src="/portal/images/brics/common/icon-warning.gif" alt="">
							<span class="error-text no-float">
								This user has exceeded the maximum incorrect login attempts allowed. The user will be 
								unable to login until a system administrator resets his or her password at the user's request.
							</span>
						</div>
						<div class="clear-both"></div>
					</s:if>
					<s:elseif test="currentAccount.isTemporarilyLocked">
						<div class="big-error-message no-float">
							<img class="icon-warning" src="/portal/images/brics/common/icon-warning.gif" alt="">
							<span class="error-text no-float">
								This account is temporarily locked after exceeding the threshold for login attempts.
							</span>
						</div>
						<div class="clear-both"></div>
					</s:elseif>
					
					<div class="form-output">
						<label class="required">Username <span class="required">* </span>:</label>
						<div class="readonly-text"><s:property value="accountDetailsForm.userName" /></div>
					</div>
					
					<s:hidden name="accountDetailsForm.userName" escapeHtml="true" escapeJavaScript="true" />
					
					<div class="form-output">
						<label class="required">Password <span class="required">* </span>:</label>
						<div class="readonly-text">
							<s:url action="passwordRecoveryAction!submit.action" var="adminResetLink">
								<s:param name="userOrEmail" value="accountDetailsForm.userName" />
							</s:url>
							<s:a href="%{adminResetLink}">Reset Password</s:a>
		
							<p>An email will be sent to the user's email account on record instructing them on how to 
								change their password.</p>
						</div>
					</div>
		
					<s:if test="currentAccount.accountStatus.name == 'Active'">
						<div class="form-output">
							<label>Account Status:</label>
							<div class="readonly-text">
								<p><strong>ACTIVE</strong></p>
								<p>This account is active in the system.</p>
								
								<c:if test="${isAdmin && hasAdminPrivilege || !hasAdminPrivilege}">
									<p>Deactivating the account will prevent the user from logging into the system. If the account needs to be 
										re-activated, the system will save the user's details and current permissions that are listed below.</p>
									<div class="button">
										<input type="button" value="Deactivate Account" onclick="deactivateAccount()" />
									</div>
								</c:if>
							</div>
						</div>
					</s:if>
					
					<s:elseif test="currentAccount.accountStatus.name == 'Pending'">
						<div class="form-output">
							<label>Account Status:</label>
							<div class="readonly-text">
								<p><strong>ACTIVE WITH CHANGE REQUESTS</strong></p>
								<p>The user has requested approval of additional permissions and/or has uploaded new documentation. Please go to the 
								<a href="viewAccountRequest!viewAccountRequest.action?accountId=${currentAccount.id}">Account Request</a> page for this user.</p>
								
								<c:if test="${isAdmin && hasAdminPrivilege || !hasAdminPrivilege}">
									<p>Deactivating the account will prevent the user from logging into the system. If the account 
										needs to be re-activated, the system will save the user's details and current permissions.</p>
									<div class="button">
										<input type="button" value="Deactivate Account" onclick="deactivateAccount()" />
									</div>
								</c:if>
							</div>
						</div>
					</s:elseif>
					
					<s:elseif test="currentAccount.accountStatus.name =='Inactive'">
						<div class="form-output">
							<label>Account Status:</label>
							<div class="readonly-text">
								<p><strong>INACTIVE</strong></p>
								<p>This account has been deactivated from the system and the user cannot log into the 
									system or perform any actions. Inactive accounts may not be edited.</p>
										
								<c:if test="${isAdmin && hasAdminPrivilege || !hasAdminPrivilege}">
									<p>Re-activating the account will restore the user details and permissions that are listed below.</p>
									<div class="button">
										<input type="button" value="Reactivate Account" onclick="javascript:reactivateAccount()" />
									</div>
								</c:if>
							</div>
						</div>
					</s:elseif>
					
					<s:elseif test="isAccountRejected">
						<div class="form-output">
							<label>Account Status:</label>
							<div class="readonly-text">
								<p><strong>REJECTED</strong></p>
								<p>The user's request for access to the system has been rejected. Rejected account 
									requests cannot be activated and cannot be edited.</p>
							</div>
						</div>
					</s:elseif>
					
					<s:elseif test="currentAccount.accountStatus.name =='Requested'">
						<div class="form-output">
							<label>Account Status:</label>
							<div class="readonly-text">
								<p><strong>NEW ACCOUNT REQUESTED</strong></p>
								<p>The user has requested access to the system. To view this request please visit the 
									<a href="viewAccountRequest!viewAccountRequest.action?accountId=${currentAccount.id}">
									Account Request</a> page for this user.</p>
								
								<c:if test="${isAdmin && hasAdminPrivilege || !hasAdminPrivilege}">
									<p>A withdrawn account will prevent the user from logging into the system. If the 
										user wishes to reapply for an account, the system will save the user's details 
										and current permission requests.</p>
									<div class="button">
										<input type="button" value="Withdraw Request" onclick="withdrawAccount()" />
									</div>
								</c:if>
							</div>
						</div>
					</s:elseif>
					
					<!-- Need to add logic to active an account from withdrawn this will set the user's account back to request. -->
					<s:elseif test="currentAccount.accountStatus.name == 'Withdrawn'">
						<div class="form-output">
							<label>Account Status:</label>
							<div class="readonly-text">
								<p><strong>Withdrawn</strong></p>
								<p>This account is withdrawn in the system.</p>
								
								<c:if test="${(isAdmin && hasAdminPrivilege) || !hasAdminPrivilege}">
									<p>Reinstating the account request will restore the user details and permissions for approval.</p>
									<div class="button">
										<input type="button" value="Reinstate Request" onclick="javascript:reinstateRequest()" />
									</div>
								</c:if>
							</div>
						</div>
					</s:elseif>
				</s:else>

				<br/>
				<h3>User Profile Details</h3>
				<p>Listed below is the contact information for the user.</p>
				<s:if test="%{!isRequest}">
					<div class="form-output">
						<label class="required">Username<span class="required"> </span>: </label>
						<div class="readonly-text no-padding"> <s:property value="accountDetailsForm.userName" /></div>
					</div>
				</s:if>
				<div class="form-field">
					<label for="eraId">NIH Federal Identity :</label>
					<s:textfield id="eraId" name="accountDetailsForm.eraId" cssClass="textfield" maxlength="50" 
							disabled="true" escapeHtml="true" escapeJavaScript="true" />
					<s:hidden id="eraId" name="accountDetailsForm.eraId" maxlength="50" escapeHtml="true" escapeJavaScript="true" />
					<s:fielderror fieldName="accountDetailsForm.eraId" />
				</div>
		
				<div class="form-field">
					<label for="firstName" class="required">First Name <span class="required">* </span>:</label>
					<s:textfield id="firstName" name="accountDetailsForm.firstName" cssClass="textfield required" maxlength="100" 
							disabled="isAccountRejected" escapeHtml="true" escapeJavaScript="true" />
					<s:fielderror fieldName="accountDetailsForm.firstName" />
				</div>
		
				<div class="form-field">
					<label for="middleName">Middle Name :</label>
					<s:textfield id="middleName" name="accountDetailsForm.middleName" cssClass="textfield" 
							maxlength="100" disabled="isAccountRejected" escapeHtml="true" escapeJavaScript="true" />
					<s:fielderror fieldName="accountDetailsForm.middleName" />
				</div>

				<div class="form-field">
					<label for="lastName" class="required">Last Name <span class="required">* </span>: </label>
					<s:textfield id="lastName" name="accountDetailsForm.lastName" cssClass="textfield required" maxlength="100" 
							disabled="isAccountRejected" escapeHtml="true" escapeJavaScript="true" />
					<s:fielderror fieldName="accountDetailsForm.lastName" />
				</div>
		
				<div class="form-field">
					<label for="email" class="required">E-Mail <span class="required">* </span>: </label>
					<s:textfield id="email" name="accountDetailsForm.email" cssClass="textfield required" 
							maxlength="100" disabled="isAccountRejected" escapeHtml="true" escapeJavaScript="true" />
					<s:fielderror fieldName="accountDetailsForm.email" />
				</div>

				<div class="form-field">
					<label for="affiliatedInstitution" class="required">Affiliated Institution<span class="required">* </span>: </label>
					<s:textfield id="affiliatedInstitution" name="accountDetailsForm.affiliatedInstitution" cssClass="textfield required" 
							maxlength="100" disabled="isAccountRejected" escapeHtml="true" escapeJavaScript="true" />
					<s:fielderror fieldName="accountDetailsForm.affiliatedInstitution" />
					<div class="special-instruction">E.g. NIH, NINDS, DOD, MRMC</div>
				</div>
		
				<div class="form-field">
					<label for="address1" class="required">Street Line 1 <span class="required">* </span>: </label>
					<s:textfield id="address1" name="accountDetailsForm.address1" cssClass="textfield required" maxlength="100" 
							disabled="isAccountRejected" escapeHtml="true" escapeJavaScript="true" />
					<s:fielderror fieldName="accountDetailsForm.address1" />
				</div>
		
				<div class="form-field">
					<label for="address2">Street Line 2 :</label>
					<s:textfield id="address2" name="accountDetailsForm.address2" cssClass="textfield" maxlength="100" 
							disabled="isAccountRejected" escapeHtml="true" escapeJavaScript="true" />
					<s:fielderror fieldName="accountDetailsForm.address2" />
				</div>
		
				<div class="form-field">
					<label for="city" class="required">City <span class="required">* </span>: </label>
					<s:textfield id="city" name="accountDetailsForm.city" cssClass="textfield required" maxlength="100" 
							disabled="isAccountRejected" escapeHtml="true" escapeJavaScript="true" />
					<s:fielderror fieldName="accountDetailsForm.city" />
				</div>
		
				<div class="form-field">
					<label for="country" class="required">Country <span class="required">* </span>:</label>
					<s:select id="country" cssClass="country" list="countryList" listKey="id" listValue="name" name="accountDetailsForm.country" 
							value="accountDetailsForm.country.id" disabled="isAccountRejected" />
					<s:fielderror fieldName="accountDetailsForm.country" />
				</div>
		
				<div class="form-field">
					<label for="postalCode" class="required">Postal Code <span class="required">* </span>: </label>
					<s:textfield id="postalCode" name="accountDetailsForm.postalCode" cssClass="textfield required" 
							maxlength="15" disabled="isAccountRejected" escapeHtml="true" escapeJavaScript="true" />
					<s:fielderror fieldName="accountDetailsForm.postalCode" />
				</div>
		
				<div id="state" class="form-field">
					<label for="accountDetailsForm.state" class="required">State <span class="required">* </span>:</label>
					<s:select id="accountDetailsForm.state" list="stateList" listKey="id" listValue="name" 
							name="accountDetailsForm.state" value="accountDetailsForm.state.id" headerKey="" 
							headerValue="- Select One -" disabled="isAccountRejected" />
					<s:fielderror fieldName="accountDetailsForm.state" />
				</div>
		
				<div class="form-field">
					<label for="phone" class="required">Phone <span class="required">* </span>:</label>
					<s:textfield id="phone" name="accountDetailsForm.phone" cssClass="textfield required" maxlength="30" 
							disabled="isAccountRejected" escapeHtml="true" escapeJavaScript="true" />
					<s:fielderror fieldName="accountDetailsForm.phone" />
					<div class="special-instruction">
						Only numbers, parentheses, dashes, x, +, or spaces, e.g. +1-(202) 124-1234x567
					</div>
				</div>
		
				<div class="form-output form-field">
					<label for="interestInTbi" class="required">Account Request Reason <span class="required">* </span>:</label>
					<s:textarea cols="60" rows="10" cssClass="textfield required" name="accountDetailsForm.interestInTbi" 
							disabled="isAccountRejected" escapeHtml="true" escapeJavaScript="true" />
					<s:fielderror fieldName="accountDetailsForm.interestInTbi" />
				</div>

				<s:if test="%{!isRequest}">
					<div class="form-output">
						<label>Application Date :</label>
						<div class="readonly-text no-padding">
							<s:property value="sessionAccountEdit.account.applicationString" />
						</div>
					</div>
		

				</s:if>
		
				<br/>
				<h3>User Account Privileges</h3>
				<jsp:include page="includes/accountPrivilegesInterface.jsp" />
				
				<s:if test="%{!isRequest}">
					<jsp:include page="includes/accountActionHistory.jsp">
						<jsp:param name="idtUrl" value="viewProfile!getAccountHistory.action"/>
					</jsp:include>
				</s:if>
				
				<jsp:include page="includes/accountAdministrativeNotes.jsp">
					<jsp:param name="idtUrl" value="viewProfile!getAccountAdministrativeNotes.action"/>
				</jsp:include>
				
				<div class="form-field clear-left">
					<s:if test="!isAccountRejected">
					
						<%-- If creating a new user--%>
						<s:if test="%{isRequest}"> 
							<div class="button">
								<input type="button" id="" value="Create" onClick="submitDetailsForm('accountValidationAction!adminSubmit.action')" />
							</div>
							<a class="form-link" href="accountAction!list.action">Cancel</a>
						</s:if>
						
						<%-- If a new user request or pending account request --%>
						<s:elseif test="%{currentAccount.accountStatus.name == 'Requested'}">
							<div class="button">
								<input type="button" value="Save" onClick="submitDetailsForm('editaccountValidationAction!moveToRequest.action');" />
							</div>
							<a class="form-link" href="viewAccountRequest!viewAccountRequest.action?accountId=${currentAccount.id}">Cancel</a>
						</s:elseif>
						
						<%-- If a standard edit --%>
						<s:else>
							<div class="button">
								<input type="button" value="Update" onClick="submitDetailsForm('editaccountValidationAction!adminSubmit.action');" />
							</div>
							<a class="form-link" href="accountAction!list.action">Cancel</a>
						</s:else>
					</s:if>
				</div>
		
			</s:form>
		</div>
		<!-- end of #main-content -->
	</div>
</div>
<!-- end of .border-wrapper -->


<script type="text/javascript" src="/portal/js/account.js"></script>

<script type="text/javascript">

<s:if test="%{inAccountReviewer}">
	setNavigation({"bodyClass":"primary", "navigationLinkID":"userManagementModuleLink", "subnavigationLinkID":"managingAccountsLink", "tertiaryLinkID":"accountRequestLink"});
</s:if> 

<s:else>
	<s:if test="%{isRequest}">
		setNavigation({	"bodyClass" : "primary", "navigationLinkID" : "userManagementModuleLink","subnavigationLinkID" : "managingUsersLink","tertiaryLinkID" : "createUserLink"});
	</s:if>
	<s:else>
		setNavigation({	"bodyClass" : "primary", "navigationLinkID" : "userManagementModuleLink","subnavigationLinkID" : "managingUsersLink","tertiaryLinkID" : "accountList"});
	</s:else>
</s:else>


	/** 
	 * Account role expiration date constructer.
	 */
	function RoleExpiration(roleType, exprDate) {
		this.roleTypeId = roleType;
		this.expirationDate = exprDate;
	}
	
	
	$('document').ready(function() {
		updateState();
		$("#availabilityDisplay").hide();
		
		// Handler for the "Reset Password" link, which will redirect to the password recovery page.
		$("#adminResetLink").click(function(e) {
		    e.preventDefault();
		    submitForm("passwordRecoveryAction!submit.action");
		});
		
		// Change event handler for the country dropbox.
		$('.country').change(function() {
			var $state = $('#state');
			
			if ($('.country option:selected').text() == "United States of America") {
				$state.fadeIn();
			} else {
				$state.fadeOut();
				$state.find("option:selected").prop("selected", false);
			}
		});
	});
	
	function submitDetailsForm(action) {
		// Prepare all of the expiration date fields in the included accountPrivilegesInterface.jsp for submission.
		enableAllPrivileges();
		processHiddenExpireDateFields();
		convertExpirationDatesToJSON();
		
		// Change the action attribute and submit the form.
		submitForm(action);
	}
	
	// Builds a list of affiliated institutions to be used by the JQUERY auto-complete field.
	$(function() {
		var availableTags = ${affiliatedInstitutionList};
		
		$("#affiliatedInstitution").autocomplete({
			source: availableTags,
			formatResult: function(row) {
				return $('<div/>').html(row).html();
		    }
		});
	});
	

	function checkAvailability() {
		$("#availabilityDisplay").fadeOut(100);

		var userName = $('.userName').val();
		var id = $('#userId').val();

		if (userName != "") {
			$.post("/portal/publicAccounts/accountAction!checkUserName.ajax", {
				id : id,
				userName : userName
			}, 
			function(data) {
				var $availDisplay = $("#availabilityDisplay");
				
				if (data == "landing") {
					window.location.href = "/portal/baseAction!landing.action";
					
				} else if (data == "Username is available") {
					$availDisplay.addClass("green-text");
					$availDisplay.removeClass("red-text");
				} else {
					$availDisplay.addClass("red-text");
					$availDisplay.removeClass("green-text");
					$availDisplay.css("padding-left", 1);
				}

				$availDisplay.text(data);
				$availDisplay.fadeIn();
			});
		}
	}

	//MT: Used to deactivate a user account and reload same screen
	function deactivateAccount() {
		if (confirm("Are you sure you want to deactivate this user?")) {
			window.location = 'accountAction!deactivate.action';
		}
	}
	
	//Used to withdarw a user account and reload same screen
	function withdrawAccount() {
		if (confirm("Are you sure you want to mark this request as withdrawn?")) {
			window.location = 'accountAction!withdraw.action';
		}
	}
	
	//Used to reinstate a user account request and reload the same screen
	function reactivateAccount() {
		// Prepare all of the expiration date fields in the included accountPrivilegesInterface.jsp for submission.
		processHiddenExpireDateFields();
		convertExpirationDatesToJSON();
		
		$.post("accountAction!reactivate.ajax", $('#theForm').serialize(), function(data) {
			
			if (data == "invalid") {
				$.post("accountAction!reactivationLightbox.ajax", {}, function(data) {
					$.fancybox(data);
				});
			} else if (data == "landing") {
				window.location.href = "/portal/baseAction!landing.action";
			} else {
				window.location = 'viewUserAccount!viewUserAccount.action?accountId=${currentAccount.id}';
			}
		});
	}

	//Used to reinstate a user account request and reload the same screen
	function reinstateRequest() {
		// Prepare all of the expiration date fields in the included accountPrivilegesInterface.jsp for submission.
		processHiddenExpireDateFields();
		convertExpirationDatesToJSON();
		
		$.post("accountAction!reinstateRequest.ajax", $('#theForm').serialize(), function(data) {
			if (data == "invalid") {
				$.post("accountAction!reactivationLightbox.ajax", {}, function(data) {
					$.fancybox(data);
				});
			} else if (data == "landing") {
				window.location.href = "/portal/baseAction!landing.action";
			} else {
				window.location = 'viewUserAccount!viewUserAccount.action?accountId=${currentAccount.id}';
			}
		});
	}
</script>
