<%@include file="/common/taglibs.jsp"%>
	
	<form id="theForm">
	<h1>
		Withdraw Account
	</h1>
	<br>
	<p>
		You are about to withdraw your account request, which will take your profile out of the account request queue and prevent you from logging into the system.
		You will have to contact the System Administrator to reinstate your account request. If you would like to continue with withdrawing your account request, please provide a reason why below:
	</p>
	
	
	
	<label for="withdrawReason" class="required">Reason: <span class="required">* </span></label>
	<s:textarea cols="50" rows="10"
		cssClass="textfield required" name="withdrawReason" id="withdrawReason"
						escapeHtml="true" escapeJavaScript="true" required='true' />
	<span id="validateWithdrawReason" style="display: none">
									<img class="icon-warning" alt="" src="/portal/images/brics/common/icon-warning.gif">
									<span class="required"><strong>Reason is a  required field</strong></span>
					 </span>
	
	</form>