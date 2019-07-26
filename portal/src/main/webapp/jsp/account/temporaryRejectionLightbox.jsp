<%@include file="/common/taglibs.jsp"%>
	
	<form id="temporaryRejectionForm">
	<h1>
		Temporary Rejection
	</h1>
	<br>
	<p>
		You must select at least one reason for temporarily rejecting this account request.  This action will send an automated email to the user that will provide the system-designated text associated with the selection reason.
		This text is managed by System Admins. The user will be able to log in their profile and correct any issues specified here.
	</p>
	<div id="temporaryRejectionMessages" class="flex-no-wrap">
		<div>
		<b>Rejection Reason:</b>
		</div>
		<div id="temporaryRejectionList" class="flex-vertical">
		
			<s:iterator var="accountTemporaryRejection" value="accountTemporaryRejectionList">
			<div class="flex-no-wrap flex-checkbox">
			<input type="checkbox" id="accountTemporaryRejection_${accountTemporaryRejection.id}" class="accountTemporaryRejectionCheckBox" style="height: 1.5em" name="accountTemporaryRejectionCheckBox_${accountTemporaryRejection.id}" data-id="${accountTemporaryRejection.id}" value="${accountTemporaryRejection.id}" ${accountTemporaryRejection.defaultChecked ? 'checked' : ''} />
			<label for="${accountTemporaryRejection.checkboxText}" class="no-float ">${accountTemporaryRejection.checkboxText}</label>
			 <br>
			 <s:if test='#accountTemporaryRejection.message == ""'>
			 	<s:textarea for="%{#accountTemporaryRejection.checkboxText}" cols="20" rows="4"
		cssClass="accountTemporaryRejectionMessage" name="CustomMessage_%{#accountTemporaryRejection.id}" id="CustomMessage_%{#accountTemporaryRejection.id}"
						escapeHtml="true" style="float:right" escapeJavaScript="true" required='true' />
			</s:if>
			
			 </div>
			</s:iterator>
		
		</div>
	</div>
	
	</form>