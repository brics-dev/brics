<%@include file="/common/taglibs.jsp"%>

<div id="main-content">
	<s:form id="theForm" cssClass="validate" method="post" validate="true" enctype="multipart/form-data">
		<s:token />
		<h3>Username Conflict Detected</h3>
		<p>An active account already exists with this username, please enter a new username. Once activated, the user 
			will be notified of this change via email</p>
			
		<div class="form-field">
			<label for="userName" class="required">Username <span class="required">* </span>:</label>
			<s:textfield id="userName" name="userName" cssClass="userName textfield required" maxlength="30" 
					escapeHtml="true" escapeJavaScript="true" />
			<a href="javascript:checkAvailability()">Check Availability</a> <span class="fieldspan" id="availabilityDisplay"></span>
			<s:fielderror fieldName="userName" />
			<div class="special-instruction">
				Must only contain alphanumeric characters (A-Z, a-z, 0-9) and must start with a letter. Max length 30 characters.
			</div>
		</div>
		
		<!-- Different buttons for reactivation and reinstation -->
		<s:if test="currentAccount.accountStatus.name=='Inactive'">
			<div class="button">
				<input type="button" value="Submit" onClick="javascript:reactivateNewUserName('accountReactivationValidationAction!reactivate.ajax')">
			</div>
		</s:if>
		<s:if test="currentAccount.accountStatus.name=='Withdrawn'">
			<div class="button">
				<input type="button" value="Submit" onClick="javascript:reactivateNewUserName('accountReactivationValidationAction!reinstateRequest.ajax')">
			</div>
		</s:if>

	</s:form>
</div>

<script type="text/javascript">
	function reactivateNewUserName(postUrl) {
		$.ajax({
			type: "POST",
			cache: false,
			url: postUrl,
			data: $("form").serializeArray(),
			success: function(data) {
				if(data=="success") {
					window.location="viewUserAccount!viewUserAccount.action?accountId=${currentAccount.id}";
				} else if (data == "landing") {
					window.location.href = "/portal/baseAction!landing.action";
				} else {
					$.fancybox(data);
				}
			}
		});
	}
</script> 