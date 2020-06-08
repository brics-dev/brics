<%@include file="/common/taglibs.jsp"%>

<form id="renewAccountForm" action="renewAccountAction!renewAccount.action">
	<div class="float-left">
		<p>
			Please provide an administrative comment associated with renewing this account. This reason will not be sent
			to the user, but a notification of their renewed privileges will be. The expiring privileges will be renewed
			for the date selected.
		</p>
		<label for="renewPrivilegesComment" class="required">Comment:<span class="required">* </span> </label>
		<s:textarea for="renewPrivilegesComment" id ="renewPrivilegesComment" name="renewPrivilegesComment"  cols="50" rows="10" cssClass="textfield required" escapeHtml="true" escapeJavaScript="true" required='true'/>
		
		<span id="validateRenewPrivilegesComment" style="display: none">
				<img class="icon-warning" alt="" src="/portal/images/brics/common/icon-warning.gif"> <span class="required"><strong>Comment is a  required field</strong></span>
		</span>
	</div>
	<br><br>
	
	<div class="float-left">
	<p>
		Set a new date for the expiring privileges to expire. The preselected date is a year from date of account
		renewal. In order to select different dates for individual modules, the account must be approved and then
		manually edited within an active account.		
	</p>
	</div>
	
	<div>
		<label for="renewPrivilegesExpireDate" class="required">Expiration Date: <span class="required">* </span></label>
		<input type="text" id="renewPrivilegesExpireDate" name="renewPrivilegesExpireDate" maxlength="10" class ="small float-left" />
	</div> 

	
</form>

	
<script type="text/javascript">
	
	$("document").ready(function() { 
		
		$( "#renewPrivilegesExpireDate").datepicker({
			showOn: "button",
			buttonImage: "../images/calendar.gif",
			buttonImageOnly: true,
			changeYear: true,
			dateFormat: "yy-mm-dd",
			minDate: 0,
		});
		
		var defaultDate = new Date(new Date().setFullYear(new Date().getFullYear() + 1));
		$( "#renewPrivilegesExpireDate").datepicker("setDate",defaultDate);

	});
	
</script>