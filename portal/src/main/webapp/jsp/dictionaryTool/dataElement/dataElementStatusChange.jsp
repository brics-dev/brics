<%@include file="/common/taglibs.jsp"%>
	<form id="theForm">
			<h2 class="pageHeader">
				DataElement:
				<s:property value="currentDataElement.title" />
			</h2>
			<h3>
				<strong>Status:</strong>
				<s:property value="currentDataElement.status.name" />
			</h3>

			<br />
			
			<s:if test="%{isRequestedStatusChange}">
				<p>The user has requested the publication of the following data element. Please review the data element
						details.
				</p>
				<div class="form-field">
					<label for="reason" class="required">Approval/Rejection
							Reason: <span class="required">* </span>
					</label>
					<s:textarea label="reason" cols="60" rows="4"
								cssClass="textfield required" name="reason" escapeHtml="true"
								escapeJavaScript="true" id="reason"/>
					 <span id="validateStatusReason" style="display: none">
									<img class="icon-warning" alt="" src="/portal/images/brics/common/icon-warning.gif">
									<span class="required"><strong>Approval/Rejection is a  required field</strong></span>
					 </span>
				</div>				     
				<p>An email will be sent to the user with the above message included in the body.</p>
            </s:if>
				 
				<p>
					This data element is currently in
					<s:property value="currentDataElement.status.name" />
					. This action will change the status to <span class="required">
						${newStatus}</span>. Please provide a detailed reason for making the
					change.You may also upload any relevant documentation (not
					required).
				</p>
				<div class="form-field">
					<label for="reason" class="required">Reason for admin Status
						Change: <span class="required">* </span>
					</label>
					<s:textarea for="reason" cols="60" rows="4"
						cssClass="textfield required" name="statusReason" id="statusReason"
						escapeHtml="true" escapeJavaScript="true"/>
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
