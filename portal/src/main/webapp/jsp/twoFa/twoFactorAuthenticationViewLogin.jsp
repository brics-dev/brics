
<%@taglib prefix="s" uri="/struts-tags"%>
<%@taglib prefix="sec"
	uri="http://www.springframework.org/security/tags"%>
<%@include file="/common/taglibs.jsp"%>
<title>Two Factor Authentication</title>


<div class="border-wrapper wide">
	<div id="main-content">
		
		<div class="clear-float">
		<table border="0" width="40%" align="left">
			<div class="clear-float" style="float: right; width: 70%">
				<p>For security reasons, please Log Out and Exit your web browser when you are done accessing services that require authentication!</p>
				<p><strong>Warning Notice</strong></p>
				<p>This is a U.S. Government computer system, which may be accessed and used only for authorized Government business by authorized personnel. Unauthorized access or use of this computer system may subject violators to criminal, civil, and/or administrative action.</p>
				<P>All information this computer system may be intercepted, recorded, read, copied, and disclosed by and to authorized personnel for official purposes, including criminal investigations. Such information includes sensitive data encrypted to comply with confidentiality and privacy requirements. Access or use of this computer system by any person, whether authorized or unauthorized, constitutes consent to these terms. There is no right of privacy in this system.</P>
			</div>
			<div class="box fl-panel" id="twoFa" style="float: left; width: 27%; border: 1px solid #c6c6c6; border-radius: 5px; background: #eee; padding: 10px;">
					<s:form id="twoFaForm" cssClass="validate" method="post" validate="true" action="/twoFa/twoFaValidationAction!submit.action">
						  <form:errors path="*" cssClass="errors" id="msg" element="div" htmlEscape="false" />
							<h2>Two Factor Authentication</h2>
							</br>
							<p class="float-left">Please check your email and enter the Two Factor Authentication one-time Code. You may need to check your Junk or Spam folder.</p>
							<div class="form-field" style="overflow: visible;">
							<div class="fl-controls-left">
								<s:fielderror fieldName="twoFaCode" />
							</div>
							</br>
							<div class="fl-controls-left">
								<label for="twoFaCode" class="required" style="width: auto;text-align: right">Authentication Code<span class="required"> * </span> : </label>
			                	<s:textfield id="twoFaCode" name="twoFaCode" autocomplete="off" cssClass="textfield required" tabindex="1" size="6" maxlength="6" htmlEscape="true" style="width: 12em; clear:right" />
							</div>
							</div>
							<div class="btn-row">
								<p style="clear:both; padding-top:10px">
									<input class="btn-submit" type="submit" id="submitBtnTwoFa" value="Submit" title="Submit" tabindex="2" style="padding:4px"/>
									<input class="btn-reset"  type="button"  id="clearBtnTwoFa"  value="Clear"  title="Clear"  tabindex="3" style="padding:4px"/>
									<input class="btn-cancel" type="button" id="cancelBtnTwoFa" value="Cancel" title="Cancel" tabindex="3" style="padding:4px"/>
								</p>
							</div>
					</s:form>
			</div>
		</table>	
		</div>	
		
	</div>
</div>

<script type="text/javascript">

	setNavigation({
		"bodyClass" : "primary"
	});
	
	$("input#cancelBtnTwoFa").click(function() {
		window.location.href = "/portal/logout";
	});
</script>

<script type="text/javascript">
	$('document').ready(function() {
		$("#navigation").hide();
		$("input#clearBtnTwoFa").click(function() {
			$(".error-message").html("");
			$("input#twoFaCode").val('');
		});
		var time = 1000;
		setInterval(function() {
			var url = window.location.href;
			while (url.indexOf("/twoFa/twoFaLoginAction") > 0 
					&& document.getElementById("login-button").innerHTML.length <= 0) {
				time += 10;
			} 
			if(url.indexOf("/twoFa/twoFaLoginAction") > 0 
					&& document.getElementById("login-button").innerHTML.length > 0) {
				$('#loginActionBtn')[0].click();
			}
		}, time);
	});
</script>