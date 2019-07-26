<%@include file="/common/taglibs.jsp"%>
	
	<form id="finalApprovalForm" action="finalApprovalAction!finalApproval.action" >
	
	<h1>
		Final Approval
	</h1>
	<br>
	
	<div class="float-left">
		<p>
			Please provide an administrative comment associated with approving this account.
			This reason will not be sent to the user, but notification of their active account will be sent.
		</p>
	
	
	
	<label for="finallApprovalComments" class="required">Comments: <span class="required">* </span></label>
	<s:textarea for="finalApprovalComments" maxlength="1000" cols="50" rows="10" cssClass="textfield required" name="finalApprovalComments" id="finalApprovalComments" escapeHtml="true" escapeJavaScript="true" required='true' />
		<span id="validateFinalApprovalComments" style="display: none">
				<img class="icon-warning" alt="" src="/portal/images/brics/common/icon-warning.gif"> <span class="required"><strong>Comments is a required field</strong></span>
		</span>
	</div>
					 
					 
	<br>

	<div class="float-left">
	<p>
		Set a date for all requested account privileges to expire. The preselected date is a year from date of account approval, but can be edited using the 
		date chooser and applied to all modules. In order to select different dates for individual modules, the account must be approved and then manually 
		edited within an active account: 		
	</p>
	</div>
			
	

	<label for="finalApprovalExpireDate" >Expiration Date:</label>
	<s:textfield id="finalApprovalExpireDate" style="float:left" maxlength="10" name="finalApprovalExpireDate" escapeHtml="true" escapeJavaScript="true" />


	</form>
	

	<script type="text/javascript">
	
	
	$('document').ready(function() 
			{ 
				
				
				$( "#finalApprovalExpireDate").datepicker({
				      showOn: "button",
				      buttonImage: "../images/calendar.gif",
				      buttonImageOnly: true,
				      minDate: 0,
				      changeYear: true,
				      dateFormat: "yy-mm-dd"
				    });

				var oneYr = new Date(new Date().setFullYear(new Date().getFullYear() + 1));
				$( "#finalApprovalExpireDate").datepicker("setDate",oneYr);

			}
		);
	
	</script>