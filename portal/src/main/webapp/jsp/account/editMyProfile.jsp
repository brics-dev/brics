<%@include file="/common/taglibs.jsp"%>
<title>Edit My Profile</title>

<div class="clear-float">
	<h1 class="float-left">Account Management</h1>
</div>

<!-- begin .border-wrapper -->
	<div class="border-wrapper">
		<jsp:include page="../navigation/userManagementNavigation.jsp" />
		
		<div id="main-content">
			
			<div id="breadcrumb">
				<a href="viewProfile!view.action">My Profile</a>&nbsp;&gt;&nbsp;Edit My Profile
			</div>
			
			<h2>Edit My Profile</h2>
			<p>Please provide your preferred contact information:</p>
			
			<div id="mainDiv" class="clear-float">
			
				<s:form id="theForm" cssClass="validate" method="post" name="accountDetailsForm" validate="true" enctype="multipart/form-data">
					<s:token />
					<s:hidden name="accountId" escapeHtml="true" escapeJavaScript="true" />
					<s:hidden name="userId" escapeHtml="true" escapeJavaScript="true" />
					<s:hidden name="nameSpace" escapeHtml="true" escapeJavaScript="true" />
					<s:hidden name="isRequest" escapeHtml="true" escapeJavaScript="true" />

				<div class="form-output">
					<label class="required">Username<span class="required"> </span>:</label>
					<div class="readonly-text no-padding">
						<s:property value="accountDetailsForm.userName" />
						<input type="hidden" value="${accountDetailsForm.userName}" name="accountDetailsForm.userName"/>
					</div>
				</div>

				<div class="form-field">
					<label for="accountDetailsForm.eraId">NIH Federal Identity :</label>
					<s:textfield id="accountDetailsForm.eraId" name="accountDetailsForm.eraId" cssClass="textfield" maxlength="50" disabled="true" escapeHtml="true" escapeJavaScript="true" />
					<s:hidden id="accountDetailsForm.eraId" name="accountDetailsForm.eraId" maxlength="50" escapeHtml="true" escapeJavaScript="true" />
					<s:fielderror fieldName="accountDetailsForm.eraId" />
				</div>

				<div class="form-field">
					<label for="accountDetailsForm.firstName" class="required">First Name <span class="required">* </span>:</label>
					<s:textfield id="accountDetailsForm.firstName" name="accountDetailsForm.firstName" cssClass="textfield required" maxlength="100" disabled="isAccountRejected" escapeHtml="true" escapeJavaScript="true" />
					<s:fielderror fieldName="accountDetailsForm.firstName" />
				</div>

				<div class="form-field">
					<label for="accountDetailsForm.middleName">Middle Name :</label>
					<s:textfield id="accountDetailsForm.middleName" name="accountDetailsForm.middleName" cssClass="textfield" maxlength="100" disabled="isAccountRejected" escapeHtml="true" escapeJavaScript="true" />
					<s:fielderror fieldName="accountDetailsForm.middleName" />
				</div>

				<div class="form-field">
					<label for="accountDetailsForm.lastName" class="required">Last Name <span class="required">* </span>:</label>
					<s:textfield id="accountDetailsForm.lastName" name="accountDetailsForm.lastName" cssClass="textfield required" maxlength="100" disabled="isAccountRejected" escapeHtml="true" escapeJavaScript="true" />
					<s:fielderror fieldName="accountDetailsForm.lastName" />
				</div>

				<div class="form-field">
					<label for="accountDetailsForm.email" class="required">E-Mail <span class="required">* </span>:</label>
					<s:textfield id="accountDetailsForm.email" name="accountDetailsForm.email" cssClass="textfield required" maxlength="100" disabled="isAccountRejected" escapeHtml="true" escapeJavaScript="true" />
					<s:fielderror fieldName="accountDetailsForm.email" />
				</div>

				<div class="form-field">
					<label for="affiliatedInstitution" class="required">Affiliated Institution<span class="required">* </span>:</label>
					<s:textfield id="affiliatedInstitution" name="accountDetailsForm.affiliatedInstitution" cssClass="textfield required" maxlength="100" disabled="isAccountRejected" escapeHtml="true" escapeJavaScript="true" />
					<s:fielderror fieldName="accountDetailsForm.affiliatedInstitution" />
					<div class="special-instruction">E.g. NIH, NINDS, DOD, MRMC</div>
				</div>

				<div class="form-field">
					<label for="accountDetailsForm.address1" class="required">Street Line 1 <span class="required">* </span>:</label>
					<s:textfield id="accountDetailsForm.address1" name="accountDetailsForm.address1" cssClass="textfield required" maxlength="100" disabled="isAccountRejected" escapeHtml="true" escapeJavaScript="true" />
					<s:fielderror fieldName="accountDetailsForm.address1" />
				</div>
		
				<div class="form-field">
					<label for="accountDetailsForm.address2">Street Line 2 :</label>
					<s:textfield id="accountDetailsForm.address2" name="accountDetailsForm.address2" cssClass="textfield" maxlength="100" disabled="isAccountRejected" escapeHtml="true" escapeJavaScript="true" />
					<s:fielderror fieldName="accountDetailsForm.address2" />
				</div>

				<div class="form-field">
					<label for="accountDetailsForm.city" class="required">City <span class="required">* </span>:</label>
					<s:textfield id="accountDetailsForm.city" name="accountDetailsForm.city" cssClass="textfield required" maxlength="100" disabled="isAccountRejected" escapeHtml="true" escapeJavaScript="true" />
					<s:fielderror fieldName="accountDetailsForm.city" />
				</div>

				<div class="form-field">
					<label for="accountDetailsForm.country" class="required">Country <span class="required">* </span>:</label>
					<s:select id="accountDetailsForm.country" cssClass="country" list="countryList" listKey="id" listValue="name" name="accountDetailsForm.country" value="accountDetailsForm.country.id" disabled="isAccountRejected" escapeHtml="true" escapeJavaScript="true" />
					<s:fielderror fieldName="accountDetailsForm.country" />
				</div>

				<div class="form-field">
					<label for="accountDetailsForm.postalCode" class="required">Postal Code <span class="required">* </span>:</label>
					<s:textfield id="accountDetailsForm.postalCode" name="accountDetailsForm.postalCode" cssClass="textfield required" maxlength="15" disabled="isAccountRejected" escapeHtml="true" escapeJavaScript="true" />
					<s:fielderror fieldName="accountDetailsForm.postalCode" />
				</div>

				<div id="state" class="form-field">
					<label for="accountDetailsForm.state" class="required">State <span class="required">* </span>:</label>
					<s:select id="accountDetailsForm.state" list="stateList" listKey="id" listValue="name"
						name="accountDetailsForm.state" value="accountDetailsForm.state.id" headerKey="" headerValue="- Select One -" disabled="isAccountRejected" />
					<s:fielderror fieldName="accountDetailsForm.state" />
				</div>
		
				<div class="form-field">
					<label for="accountDetailsForm.phone" class="required">Phone <span class="required">* </span>:</label>
					<s:textfield id="accountDetailsForm.phone" name="accountDetailsForm.phone" cssClass="textfield required" maxlength="30" disabled="isAccountRejected" escapeHtml="true" escapeJavaScript="true" />
					<s:fielderror fieldName="accountDetailsForm.phone" />
					<div class="special-instruction">Only numbers, parentheses, dashes, x, +, or spaces, e.g. +1-(202) 124-1234x567</div>
				</div>

				<div class="form-field">
					<label for="interestInTbi" class="required">
						Briefly describe why you are requesting access to the system. Explain how you intend to use the 
						system and your intentions for data submission and/or data access <span class="required">* </span>:</label>
					<s:textarea id="interestInTbi" cssClass="textfield required" name="accountDetailsForm.interestInTbi" 
							cols="60" rows="10" disabled="isAccountRejected" escapeHtml="true" escapeJavaScript="true" />
					<s:fielderror fieldName="accountDetailsForm.interestInTbi" />
					<div class="special-instruction">
						Please add any information that would be pertinent to administrators approving your account.
					</div>
				</div>

				<div class="form-field clear-left">
					<div class="button">
						<input type="button" value="Update Profile" onClick="javascript:submitEdit()" />
					</div>
					<a class="form-link" href="javascript:window.location.href='/portal/accounts/viewProfile!view.action'">Cancel</a>
				</div>
				
			</s:form>
		</div>
<!-- end of #main-content -->
	</div>
</div>
<!-- end of .border-wrapper -->

<script type="text/javascript" src="/portal/js/account.js"></script>
<script type="text/javascript">

	//Sets the navigation menus on the page
	setNavigation({	"bodyClass" : "primary","navigationLinkID" : "userManagementModuleLink","subnavigationLinkID" : "userManagementToolLink", "tertiaryLinkID" : "viewProfileLink"});

	$('document').ready(function() {
		updateState();
		countryFunction();
		$("#availabilityDisplay").hide();

	});

	//Builds a list of affiliated institutions to be used by the JQUERY auto-complete field.
	$(function() {
		var availableTags = ${affiliatedInstitutionList};
		
		$("#affiliatedInstitution").autocomplete({
			source: availableTags,
			formatResult: function(row) {
		      return $('<div/>').html(row).html();
		    }
		});
	});

	function submitEdit() {
		var theForm = document.getElementById('theForm');
		theForm.action = 'editaccountValidationAction!submit.action';
		theForm.submit();
	}

</script>