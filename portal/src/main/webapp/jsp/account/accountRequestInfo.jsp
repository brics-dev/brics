<%@include file="/common/taglibs.jsp"%>
<title>Account Request</title>

<div class="clear-float">
	<h1 class="float-left">Account Management</h1>
</div>

<!-- begin .border-wrapper -->
<div class="border-wrapper wide">
	<div id="main-content">

<div id="mainDiv" class="clear-float">

	<s:if test="alternateWorkflow()">
		<p>Thank you for your interest in the PDBP Data Management Resource (DMR) system. 
			Please complete the following steps to request an account.</p>
		<ol start="1">
		<li>Enter the following Account Management information. Please note that a signed Data Use Certificate(DUC) 
				is required before data access can be granted.</li><br>
			NOTE: Each account is for a unique individual. Account holders must not share their login with others.
	</s:if>
	<s:else>
		<ol start="3">
  		<li>Enter the following Account Management information.
		<br><br>NOTE: Each account is for a unique individual. Account holders must not share their login with others.
	</s:else>
	
	<s:form id="theForm" cssClass="validate" method="post" name="accountDetailsForm" validate="true"
		enctype="multipart/form-data">
		<s:token />
		<s:if test="%{(inAccounts || inSSO) && isRequest && (accountDetailsForm.eraId == null || accountDetailsForm.eraId == '')}">
			<h3>Username and Password</h3>
			<div class="form-field">
				New users please fill out the form below. 
				<p class="required">
					Fields marked with a <span class="required">* </span>are required.
				</p>
			</div>
			<div class="clear-right"></div>
		</s:if>

		<s:hidden name="accountId" escapeHtml="true" escapeJavaScript="true" />
		<s:hidden name="userId" escapeHtml="true" escapeJavaScript="true" />
		<s:hidden name="nameSpace" escapeHtml="true" escapeJavaScript="true" />
		<s:hidden name="isRequest" escapeHtml="true" escapeJavaScript="true" />
			
		<!-- If there is not ERA ID linked to the user, use this block of code -->
		<s:if test="%{accountDetailsForm.eraId == null || accountDetailsForm.eraId == ''}">	
			<div class="form-field">
				<label for="userName" class="required">Username <span class="required">* </span>:</label>
				<s:textfield id="userName" name="accountDetailsForm.userName" cssClass="userName textfield required" 
						maxlength="30" escapeHtml="true" escapeJavaScript="true" />
				<a href="javascript:checkAvailability()">Check Availability</a>&nbsp;
				<span class="fieldspan" id="availabilityDisplay"></span>
				<s:fielderror fieldName="accountDetailsForm.userName" />
				<div class="special-instruction">
					Must only contain alphanumeric characters (A-Z, a-z, 0-9), special chars (@,-, _, .) and must start with a letter.
				</div>
			</div>
			
			<div class="form-field">
				<label for="passwordString" class="required">Password <span class="required">* </span>:</label>
				<s:password id="passwordString" name="accountDetailsForm.passwordString" cssClass="textfield required" 
						maxlength="30" escapeHtml="true" escapeJavaScript="true" autocomplete="off" />
				<s:fielderror fieldName="accountDetailsForm.passwordString" />
				<div class="special-instruction">
					Case sensitive. 8-30 alpha/numeric characters. Must contain at least 3 different kinds of characters: 
					Capital Letter, Lowercase letter, Numbers, and/or Special character.
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
			<s:hidden name="accountDetailsForm.userName" escapeHtml="true" escapeJavaScript="true" />
			<s:hidden name="accountDetailsForm.passwordString" escapeHtml="true" escapeJavaScript="true" />
			<s:hidden name="accountDetailsForm.confirmPassword" escapeHtml="true" escapeJavaScript="true" />
		</s:else>

		<br/>
		<h3>Contact Information</h3>
		<p>Please provide your preferred contact information:</p>
		
		<div class="form-field">
			<label for="eraId">NIH Federal Identity :</label>
			<s:textfield id="eraId" name="accountDetailsForm.eraId" cssClass="textfield" maxlength="50" 
					disabled="true" escapeHtml="true" escapeJavaScript="true" />
			<s:hidden name="accountDetailsForm.eraId" maxlength="50" escapeHtml="true" escapeJavaScript="true" />
			<s:fielderror fieldName="accountDetailsForm.eraId" />
		</div>

		<div class="form-field">
			<label for="firstName" class="required">First Name <span class="required">* </span>:</label>
			<s:textfield id="firstName" name="accountDetailsForm.firstName" cssClass="textfield required" 
					maxlength="100" disabled="isAccountRejected" escapeHtml="true" escapeJavaScript="true" />
			<s:fielderror fieldName="accountDetailsForm.firstName" />
		</div>

		<div class="form-field">
			<label for="middleName">Middle Name :</label>
			<s:textfield id="middleName" name="accountDetailsForm.middleName" cssClass="textfield" 
					maxlength="100" disabled="isAccountRejected" escapeHtml="true" escapeJavaScript="true" />
			<s:fielderror fieldName="accountDetailsForm.middleName" />
		</div>

		<div class="form-field">
			<label for="lastName" class="required">Last Name <span class="required">* </span>:</label>
			<s:textfield id="lastName" name="accountDetailsForm.lastName" cssClass="textfield required" maxlength="100" 
					disabled="isAccountRejected" escapeHtml="true" escapeJavaScript="true" />
			<s:fielderror fieldName="accountDetailsForm.lastName" />
		</div>

		<div class="form-field">
			<label for="email" class="required">E-Mail <span class="required">* </span>:	</label>
			<s:textfield id="email" name="accountDetailsForm.email" cssClass="textfield required" maxlength="100" 
					disabled="isAccountRejected" escapeHtml="true" escapeJavaScript="true" />
			<s:fielderror fieldName="accountDetailsForm.email" />
		</div>

		<div class="form-field">
			<label for="affiliatedInstitution" class="required">Affiliated Institution<span class="required">* </span>:</label>
			<s:textfield id="affiliatedInstitution" name="accountDetailsForm.affiliatedInstitution" cssClass="textfield required" 
					maxlength="100" disabled="isAccountRejected" escapeHtml="true" escapeJavaScript="true" />
			<s:fielderror fieldName="accountDetailsForm.affiliatedInstitution" />
			<div class="special-instruction">E.g. NIH, NINDS, DOD, MRMC</div>
		</div>

		<div class="form-field">
			<label for="address1" class="required">Street Line 1 <span class="required">* </span>:</label>
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
			<label for="city" class="required">City <span class="required">* </span>:</label>
			<s:textfield id="city" name="accountDetailsForm.city" cssClass="textfield required" maxlength="100" 
					disabled="isAccountRejected" escapeHtml="true" escapeJavaScript="true" />
			<s:fielderror fieldName="accountDetailsForm.city" />
		</div>

		<div class="form-field">
			<label for="country" class="required">Country <span class="required">* </span>:</label>
			<s:select id="country" cssClass="country" list="countryList" listKey="id" listValue="name" 
					name="accountDetailsForm.country" value="accountDetailsForm.country.id" disabled="isAccountRejected" />
			<s:fielderror fieldName="accountDetailsForm.country" />
		</div>

		<div class="form-field">
			<label for="postalCode" class="required">Postal Code <span class="required">* </span>:</label>
			<s:textfield id="postalCode" name="accountDetailsForm.postalCode" cssClass="textfield required"	
					maxlength="15" disabled="isAccountRejected" escapeHtml="true" escapeJavaScript="true" />
			<s:fielderror fieldName="accountDetailsForm.postalCode" />
		</div>

		<div id="state" class="form-field">
			<label for="stateSelect" class="required">State <span class="required">* </span>:</label>
			<s:select id="stateSelect" list="stateList" listKey="id" listValue="name" name="accountDetailsForm.state" 
					value="accountDetailsForm.state.id" headerKey="" headerValue="- Select One -" disabled="isAccountRejected" />
			<s:fielderror fieldName="accountDetailsForm.state" />
		</div>

		<div class="form-field">
			<label for="phone" class="required">Phone <span class="required">* </span>: </label>
			<s:textfield id="phone" name="accountDetailsForm.phone" cssClass="textfield required" maxlength="30" 
					disabled="isAccountRejected" escapeHtml="true" escapeJavaScript="true" />
			<s:fielderror fieldName="accountDetailsForm.phone" />
			<div class="special-instruction">Only numbers, parentheses, dashes, x, +, or spaces, e.g. +1-(202) 124-1234x567</div>
		</div>

		<div class="form-field">
			<label for="interestInTbi" class="required">
				Briefly describe why you are requesting access to the system. Explain how you intend to use the 
				system and your intentions for data submission and/or data access <span class="required">* </span>:
			</label>
			<s:textarea id="interestInTbi" cssClass="textfield required" name="accountDetailsForm.interestInTbi"
					cols="60" rows="10" disabled="isAccountRejected" escapeHtml="true" escapeJavaScript="true" />
			<s:fielderror fieldName="accountDetailsForm.interestInTbi" />
			<div class="special-instruction">
				Please add any information that would be pertinent to administrators approving your account.
			</div>
		</div>
		
		<br/>
		<s:if test="alternateWorkflow()">
			<li>
				<div>
					<span class="required">*</span> <input type="checkbox" id="ducCheckbox" />&nbsp;
					I agree to the DMR&nbsp;<a href="fileDownloadAction!download.action?fileId=11">Data Use Certificate</a>
				</div>
			</li>
			<p>On the following page, you will be able to request privileges and permissions.</p>
			<div id="account-recaptcha" class="g-recaptcha" data-sitekey="6LfWE3cUAAAAAC_QriM9wYQjzr1nL2irzSmBwxcg"></div>
		</s:if>
		<s:else>
	
			<p>On the following page, you will be able to request privileges and permissions and submit your request.</p>
			<s:if test="isGuidOnly()">
				<div id="account-recaptcha" class="g-recaptcha" data-sitekey="6LfWE3cUAAAAAC_QriM9wYQjzr1nL2irzSmBwxcg"></div>
			</s:if>
		</s:else>
		
		<div class="form-field clear-left">
			<div class="button">
			<s:if test="alternateWorkflow()">
				<input type="button" value="Continue" onClick="javascript:editPrivilegesCaptcha()" />
			</s:if>
			<s:else>
				<s:if test="isGuidOnly()">
					<input type="button" value="Continue" onClick="javascript:editPrivilegesCaptcha()" />
				</s:if>
				<s:else>
					<input type="button" value="Continue" onClick="javascript:editPrivileges()" />
				</s:else>
			</s:else>
			</div>
			<a class="form-link" href="javascript:window.location.href='/portal'">Cancel</a>
		</div>
		<div style="display: none;" id="ducDialog">You must accept the DMR Data Use Certificate before submitting the account request.</div>
		<div style="display: none;" id="recaptchaErrorDialog">There has been an error with ReCaptcha.</div>
		<div style="display: none;" id="recaptchaFailDialog">You need to complete the ReCaptcha form.</div>
	</s:form>
	</li>
 
</ol>
</div>
<!-- end of #main-content -->
</div>
</div>
<!-- end of .border-wrapper -->

<!--  recapthcha js -->
<script src='https://www.google.com/recaptcha/api.js'></script>



<!--  end of recapthca js -->
<script type="text/javascript" src="/portal/js/account.js"></script>

<script type="text/javascript">
	setNavigation({"bodyClass":"primary"});
</script>

<script type="text/javascript">
	$('document').ready(function() {
		$("#navigation").hide();
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

	function checkAvailability() {
		$("#availabilityDisplay").fadeOut(100);

		var userName = $('.userName').val();
		var id = $('#userId').val();

		if (userName != "") {
			$
					.post(
							"/portal/publicAccounts/accountAction!checkUserName.ajax",
							{
								id : id,
								userName : userName
							}, function(data) {
								if (data == "landing") {
									window.location.href = "/portal/baseAction!landing.action";
									
								} else if (data == "Username is available") {
									$("#availabilityDisplay").addClass("green-text");
									$("#availabilityDisplay").removeClass("red-text");
								} else {
									$("#availabilityDisplay").addClass("red-text");
									$("#availabilityDisplay").removeClass("green-text");
								}

								$("#availabilityDisplay").text(data);
								$("#availabilityDisplay").fadeIn();
							});
		}
	}

	function editPrivileges() {
		var eraId = $('#accountDetailsForm\\.eraId').val();

		//console.log("ERA ID: <s:property value='accountDetailsForm.eraId' />");
		//only gets called in account creation so can set the password if eraId exists
		//get randomly generated password		
		if (eraId) {
			//console.log("Generating Random Password");

			$.ajax({
					type : "GET",
					url : "/portal/publicAccounts/accountAction!generateRandomPassword.ajax",
					cache : false,
					timeout : 10000,
					async : false,
					error : function() {
						return 'password1!';
					},
					success : function(data) {
						$('#accountDetailsForm\\.passwordString').val(data);
						$('#accountDetailsForm\\.confirmPassword').val(data);
					}
				});
		}
		
		
	
		
		if($('#ducCheckbox').is(':checked') || !$('#ducCheckbox').is(':visible')){
			
			
			var theForm = document.getElementById('theForm');
			theForm.action = '/portal/publicAccounts/accountValidationAction!accountRequestPrivileges.action';

			theForm.submit();
		
		}else{
			$( "#ducDialog" ).dialog({
				title:"Warning",
				modal:true,
				draggable:true,
				  buttons: [
				            {
				              text: "Ok",
				              icons: {
				                primary: "ui-icon-heart"
				              },
				              click: function() {
				                $( this ).dialog( "close" );
				              }
				            }
				          ]
			});
			return false;//Don't sumbit and stay on the same form
		}
	}
	
	
	function editPrivilegesCaptcha() {
		var eraId = $('#accountDetailsForm\\.eraId').val();

		//console.log("ERA ID: <s:property value='accountDetailsForm.eraId' />");
		//only gets called in account creation so can set the password if eraId exists
		//get randomly generated password		
		if (eraId) {
			//console.log("Generating Random Password");

			$.ajax({
					type : "GET",
					url : "/portal/publicAccounts/accountAction!generateRandomPassword.ajax",
					cache : false,
					timeout : 10000,
					async : false,
					error : function() {
						return 'password1!';
					},
					success : function(data) {
						$('#accountDetailsForm\\.passwordString').val(data);
						$('#accountDetailsForm\\.confirmPassword').val(data);
					}
				});
		}
		
		
	
		
		if($('#ducCheckbox').is(':checked') || !$('#ducCheckbox').is(':visible')){
			
			
			//verify recaptcha
			$.ajax({
				type : "POST",
				url : "/portal/ws/recaptcha/verify",
				cache : false,
				timeout : 10000,
				data : {response: grecaptcha.getResponse()},
				async : false,
				error : function(data) {
					
					
					$( "#recaptchaErrorDialog" ).dialog({
						title:"Warning",
						modal:true,
						draggable:true,
						  buttons: [
						            {
						              text: "Ok",
						              icons: {
						                primary: "ui-icon-heart"
						              },
						              click: function() {
						                $( this ).dialog( "close" );
						              }
						            }
						          ]
					});
					return false;//Don't sumbit and stay on the same form
				},
				success : function(data) {
					
					if(!data.success) {
						$( "#recaptchaFailDialog" ).dialog({
							title:"Warning",
							modal:true,
							draggable:true,
							  buttons: [
							            {
							              text: "Ok",
							              icons: {
							                primary: "ui-icon-heart"
							              },
							              click: function() {
							                $( this ).dialog( "close" );
							              }
							            }
							          ]
						});
						return false;//Don't sumbit and stay on the same form
					} else {
						
						//submit the form on recaptcha success
						var theForm = document.getElementById('theForm');
						theForm.action = '/portal/publicAccounts/accountValidationAction!accountRequestPrivileges.action';

						theForm.submit();
						
					}
					
					
				}
			});
			
			
			//end verify captcha
		
		}else{
			$( "#ducDialog" ).dialog({
				title:"Warning",
				modal:true,
				draggable:true,
				  buttons: [
				            {
				              text: "Ok",
				              icons: {
				                primary: "ui-icon-heart"
				              },
				              click: function() {
				                $( this ).dialog( "close" );
				              }
				            }
				          ]
			});
			return false;//Don't sumbit and stay on the same form
		}
	}
</script>