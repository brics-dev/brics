<%@include file="/common/taglibs.jsp"%>
	
	<form id="rejectionForm">
	<h1>
		Reject Account
	</h1>
	<br>
	<div class="flex-justify-around">
		<img class="icon-warning" alt="" src="/portal/images/brics/common/icon-warning.gif" style="height:100%;">
		<p>
			You are about to reject this user's request in it's entirety, which will change the Account Status to Denied. Rejected account requests cannot be activated and cannot be edited.
			Are you sure you would like to continue? If so, please provide an administrative comment as to why this profile has been permanently denied.  This reason will not be sent to the user, but a notification of their denied request will be.
		</p>
	</div>
	<div id="temporaryRejectionMessages" class="flex-no-wrap">
		<div>
		<b><label for="rejectionReason" class="required">Rejection Reason:</label></b> 
		</div>
		<div class="flex-vertical">
		
			
			 	<s:textarea id="rejectionReason" name="rejectionReason" cols="50" rows="7" cssClass="" escapeHtml="true" escapeJavaScript="true" required='true' />
		
		
		</div>
	</div>
	
	
	</form>
	
