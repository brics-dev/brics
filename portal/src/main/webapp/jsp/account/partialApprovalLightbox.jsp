<%@include file="/common/taglibs.jsp"%>
	
	<form id="theForm">
	<h1>
		Partial Approval
	</h1>
	<br>
	<p>
		Please provide an administrative comment associated with approving this account.
		This reason will not be sent to the user, but stored as a record.
	</p>
	
	
	
	<label for="partialApprovalComments" class="required">Comments: <span class="required">* </span></label>
	<s:textarea for="partialApprovalComments" maxlength="1000" cols="50" rows="10"
		cssClass="textfield required" name="partialApprovalComments" id="partialApprovalComments"
						escapeHtml="true" escapeJavaScript="true" required='true' />
	<span id="validatePartialApprovalComments" style="display: none">
									<img class="icon-warning" alt="" src="/portal/images/brics/common/icon-warning.gif">
									<span class="required"><strong>Comments is a required field</strong></span>
					 </span>
	
	</form>