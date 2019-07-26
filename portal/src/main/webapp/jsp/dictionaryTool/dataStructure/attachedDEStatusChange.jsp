<%@include file="/common/taglibs.jsp"%>

	<form id="theForm">
					<h2 class="pageHeader">Form Structure: <s:property value="currentDataStructure.title" /></h2>
					<h3><strong>Status:</strong> <s:property value ="currentDataStructure.status" /></h3>
					
					<br />
					<p>This form structure contains additional data elements that will have their status changed in conjunction with this action. Please provide a detailed reason
					for changing the status of these data elements.You may also upload documentation, but each document will be associated with every data element that has its status
					changed as part of this action.</p>
					
					<p>Number of data elements affected: ${numberOfAffectedDE} &nbsp; &nbsp;&nbsp;&nbsp;&nbsp;  <a href="javascript:viewRelatedDE()" >click to view full list of affected data elements</a></p>
					
					<div class="form-field">
					<label for="attachedDEReason" class="required">Admin Status Change Reason: <span class="required">* </span>
								</label>
								<s:textarea for="attachedDEReason" cols="60" rows="4"
									cssClass="textfield required" name="attachedDEReason" id="attachedDEReason"
									escapeHtml="true" escapeJavaScript="true"   />
								 <span id="validateReason" style="display: none">
									<img class="icon-warning" alt="" src="/portal/images/brics/common/icon-warning.gif">
									<span class="required"><strong>Reason is a required field</strong> </span>
					 			 </span>
					 </div>	
	</form>	
	
	<script type="text/javascript">
	$(document).ready(function() {
		
		var reasonFromSession = '${sessionDictionaryStatusChange.statusReason}';
		$("#attachedDEReason").val(reasonFromSession);
	});
	</script>
