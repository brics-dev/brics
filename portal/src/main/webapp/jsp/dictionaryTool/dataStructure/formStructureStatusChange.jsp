<%@include file="/common/taglibs.jsp"%>
	<form id="theForm">
			<h2 class="pageHeader">
				Form Structure:
				<s:property value="currentDataStructure.title" />
			</h2>
			<h3>
				<strong>Status:</strong>
				<s:property value="currentDataStructure.status.type" />
			</h3>

			<br />
			
			<s:if test="%{isRequestedStatusChange}">
				<p>The user has requested the publication of the following form structure. Please review the form structure
						details.
				</p>
				<div class="form-field">
					<label for="reason" class="required">Approval/Rejection
							Reason: <span class="required">* </span>
					</label>
					<s:textarea label="reason" cols="60" rows="4"
								cssClass="textfield required" name="reason" escapeHtml="true"
								escapeJavaScript="true" id="reason" required='true' />
					 <span id="validateStatusReason" style="display: none">
									<img class="icon-warning" alt="" src="/portal/images/brics/common/icon-warning.gif">
									<span class="required"><strong>Approval/Rejection is a  required field</strong></span>
					 </span>
				</div>				     
				<p>An email will be sent to the user with the above message included in the body.</p>
            </s:if>
				 
				<p>
					This form structure is currently in
					<s:property value="currentDataStructure.status.type" />
					. This action will change the status to <span class="required">
						${newStatus}</span>. Please provide a detailed reason for making the
					change.You may also upload any relevant documentation (not
					required).
				</p>
				<div class="form-field">
					<label for="reason" class="required">Reason for Admin Status
						Change: <span class="required">* </span>
					</label>
					<s:textarea for="reason" cols="60" rows="4"
						cssClass="textfield required" name="statusReason" id="statusReason"
						escapeHtml="true" escapeJavaScript="true" required='true' />
					  <span id="validateReason" style="display: none">
								<img class="icon-warning" alt="" src="/portal/images/brics/common/icon-warning.gif">
								<span class="required"><strong>Reason is a required field</strong></span>
					  </span>		
				</div>			
	</form>	
		
	<script type="text/javascript">
	$(document).ready(function() {
		
		var reasonFromSession = '${sessionDictionaryStatusChange.statusReason}';
		var approveReasonFromSession = '${sessionDictionaryStatusChange.approveReason}';
		
		$("#statusReason").val(reasonFromSession);
		$("#reason").val(approveReasonFromSession);
	});
	</script>
