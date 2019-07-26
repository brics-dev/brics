<%@include file="/common/taglibs.jsp"%>
	<form id="theForm">
			<h2 class="pageHeader">
				Bulk DataElement Status Change
			</h2>
			<h3>
				<strong>Status:</strong>
				${currentStatus}
			</h3>

			<br />			 
				<p>
					The selected data element(s) is(are) currently in ${currentStatus}. 
					This action will change the status to <span class="required">
						${newStatus}</span>. Please provide a detailed reason for making the
					change.You may also upload any relevant documentation (not
					required). &nbsp; <a href="javascript:viewRelatedDE()">click to view full list of selected data elements</a>
				</p>
				<div class="form-field">
					<label for="reason" class="required">Reason for admin Status
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
