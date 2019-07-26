<jsp:include page="/common/doctype.jsp" />
<%@ page import="gov.nih.nichd.ctdb.protocol.domain.Protocol"%>
<%@ page import="gov.nih.nichd.ctdb.common.CtdbConstants"%>
<%@ taglib uri="/struts-tags" prefix="s"%>

<% 
	Protocol protocol = (Protocol) session.getAttribute(CtdbConstants.CURRENT_PROTOCOL_SESSION_KEY);
	int subjectDisplayType = protocol.getPatientDisplayType();
	boolean enableEsignature = protocol.isEnableEsignature();
	Boolean closedout = (Boolean)request.getAttribute("protocolclosedout");
%>

<html>

<%-- Include Header --%>
<s:set var="pageTitle" scope="request">
	<s:text name="protocol.study.closeout" />
</s:set>
<jsp:include page="/common/header_struts2.jsp" />

<style type="text/css">
	.center {
		margin-top: 35px;
		margin-left: 20px;
		width: 60%;
		padding: 10px;
	}

	.lockInstruction {
		text-align: justify;
		margin-bottom: 10px;
		margin-top: 10px;
	}
</style>

<script type="text/javascript" src="<s:property value='#webRoot'/>/common/js/jquery.form.min.js"></script>

<script type="text/javascript">
$(document).ready(function() {

	$(".signatureForComplete").dialog({
		width: 785, 
		autoOpen: false,
		closeOnEscape: false,
		modal: true,
		
		open: function( event, ui ) {
			//close X button while lunching this dialog.
			$(".ui-dialog-titlebar-close").hide();
		},
		
		title: 'Signature Required'
	});

});


function closeProtocol(){
	if ($('#checkAndStudyCloseout').prop('checked')) {
		<% if (enableEsignature) { %>
			$("#completeLockChkBx").prop('checked', true);
			$('#completeLockChkBx').prop('disabled', true);
			$(".signatureForComplete").dialog("open");
		<% }  else { %>
			saveCloseout();
	<% } %>
	}
}

function cancelSignature() {
	$(".signatureForComplete").dialog('close');
	$("#checkAndStudyCloseout").prop('checked', false);
	
}

function confirmSignature() {
	if ( !$("#completeLockChkBx").is(":checked") ) {
		$.ibisMessaging("close", {type:"primary"});
		passwordErrorId = $.ibisMessaging("primary", "error", '<s:text name="errors.response.confirmation.checkbox"/> </b>', {container: ".signatureForComplete"}); 
		return;
	}
	
	var url = "<s:property value="#webRoot"/>/protocol/protocolSignature!digitalSignature.action";	
 	var passwordData = '';
 	
    if ($("#userPassword").length > 0) {
    	passwordData = $("#userPassword").val();
    }
    
    $("#userPasswordId").val(passwordData);
 
    $.ajax({
		type: "POST",
		url: url,
		data: {
			"userPassword"	: passwordData
		},
				
		error: function (xhr, ajaxOptions, thrownError) {
			EventBus.trigger("close:processing");
			if (passwordData.length < 1) {
				$.ibisMessaging("close", {type:"primary"}); 
				passwordErrorId = $.ibisMessaging("primary", "error", '<s:text name="errors.response.password.required"/> </b>', 
						{container: ".signatureForComplete"}); 
				$('#checkAndStudyCloseout').prop('disabled', false);				
			} else {
  				$.ibisMessaging("close", {type:"primary"}); 
  				passwordErrorId = $.ibisMessaging("primary", "error", '<s:text name="errors.response.password.mismatch"/> </b>', 
  						{container: ".signatureForComplete"}); 
  				$('#checkAndStudyCloseout').prop('disabled', false);
  			}
		},
				
		success: function(response, status, jqxhr) { 
			EventBus.trigger("close:processing");
			if (response == "bricsAccountWSnotReacheable") {
				$.ibisMessaging("close", {type:"primary"});
				passwordErrorId = $.ibisMessaging("primary", "error", '<s:text name="errors.brics.webservice"/> </b>', 
						{container: ".signatureForComplete"}); 
				$('#checkAndStudyCloseout').prop('disabled', false);
			} else if (response == "blankPassword") {
				$.ibisMessaging("close", {type:"primary"}); 
				passwordErrorId = $.ibisMessaging("primary", "error", '<s:text name="errors.response.password.required"/> </b>', 
						{container: ".signatureForComplete"}); 
				$('#checkAndStudyCloseout').prop('disabled', false);
			} else if (response == "mismatchPassword") {
				$.ibisMessaging("close", {type:"primary"}); 
				passwordErrorId = $.ibisMessaging("primary", "error", '<s:text name="errors.response.password.mismatch"/> </b>', 
						{container: ".signatureForComplete"}); 
				$('#checkAndStudyCloseout').prop('disabled', false);
			} else if (response == "passwordValidationPassed") {
				$(".signatureForComplete").dialog('close');
				saveCloseout();
			} else {
				$.ibisMessaging("close", {type:"primary"}); 
				passwordErrorId = $.ibisMessaging("primary", "error", "Something went wrong in password validation process. Please try again.</b>", 
						{container: ".signatureForComplete"}); 
				$('#checkAndStudyCloseout').prop('disabled', false);	
			}
		 }
	});
}

function saveCloseout() {
	var url = "<s:property value="#webRoot"/>/protocol/saveProtocolCloseout!saveProtocolCloseout.action";
	
	 $.ajax({
		type: "POST",
		url: url,
	
		error: function (xhr, ajaxOptions, thrownError) {
			EventBus.trigger("close:processing");
			if (passwordData.length < 1) {
				$.ibisMessaging("close", {type:"primary"}); 
				passwordErrorId = $.ibisMessaging("primary", "error", '<s:text name="errors.response.password.required"/> </b>', 
						{container: ".signatureForComplete"}); 
				$('#checkAndStudyCloseout').prop('disabled', false);	
			} else {  					
				$.ibisMessaging("close", {type:"primary"}); 
				passwordErrorId = $.ibisMessaging("primary", "error", '<s:text name="errors.response.password.mismatch"/> </b>', 
						{container: ".signatureForComplete"}); 
				$('#checkAndStudyCloseout').prop('disabled', false);		
			}	
		},
		
		success: function(response, status, jqxhr) { 
			if (response == "bricsAccountWSnotReacheable") {
				$.ibisMessaging("close", {type:"primary"});
				passwordErrorId = $.ibisMessaging("primary", "error", '<s:text name="errors.brics.webservice"/> </b>', 
						{container: ".signatureForComplete"}); 
				$('#checkAndStudyCloseout').prop('disabled', false);
				$('#closeProtocol').prop('disabled', false);
			} else {
				var protocolName = "<%=protocol.getName()%>";
				// Display success message
				$.ibisMessaging("primary", "success", "Protocol " + protocolName + " has been closed out successfully");
				$('#checkAndStudyCloseout').prop('disabled', true);
				$('#closeProtocol').prop('disabled', true);
			}
		}
	});
}

</script>

<%if (closedout.booleanValue()){ %>
	<div style="margin-top: 50px;margin-left: 80px;">
	<h3>Protocol <%=protocol.getName()%> is already closed out</h3>
</div> 
<%} else { %>
<div id="chechbox"  class="center">
	<div>
		<input type="checkbox" value="<s:text name="protocol.study.closeout.certify" />" id="checkAndStudyCloseout" title="<s:text name="protocol.study.closeout.certify" />" />
		<label id="checkAndStudyCloseoutLabel" for="checkAndStudyCloseout">
			<s:text name="protocol.study.closeout.certify" />
		</label><br><br>
		<div style="float:right">
			<button id="closeProtocol" onClick="closeProtocol()">Close Protocol</button>
		</div>
	</div>
</div>
<%} %>

<div class="signatureForComplete">
	<div class="lockInstruction">
		<input type="checkbox" id="completeLockChkBx" />&nbsp;&nbsp;
		<s:text name="protocol.study.closeout.certify" />
	</div>
	<br/>
	<br/>
	
	<div>
		<span style="float:left; "><b>Name:</b> <s:property value='userFullName'/></span><br/><br/>
	</div>
	
	<div style="float: left;">
		<s:text name="response.complete.password" />
	</div>

		<br/>
		<br/>
		<label style="float: left;font-weight: bold;" for="userPassword">Password:<span class="requiredStar">*</span></label>
		<input type="password" style="float: left;margin-left: 20px" id="userPassword" name="userPassword" />

		<br/>
		<br/>
		<br/>
		<div style="float: left;margin-right: 10px;">
			<input type="button" id="signatureOK" value="OK" onclick="confirmSignature()" />
		</div>
		<div style="float: left;">
			<input type="button" id="signatureCancel" value="Cancel" onclick="cancelSignature()" />
		</div>
</div>

<%-- Include Footer --%>
	<jsp:include page="/common/footer_struts2.jsp" />
</html>