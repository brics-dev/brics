<title>Electronic Signature</title>

<%@taglib prefix="s" uri="/struts-tags"%>
<%@taglib prefix="sec" uri="http://www.springframework.org/security/tags"%>
<%@include file="/common/taglibs.jsp"%>

<div class="clear-float">
	<h1 class="float-left">Legal Name Validation</h1>
</div>

<div class="border-wrapper">
	<div id="main-content">
		<s:form id="theForm" cssClass="validate" method="post" validate="true" action="esignValidationAction!submit.action">
			<jsp:include page="/jsp/account/includes/accountESignature.jsp" />
			
			<div class="form-field" style="display: inline-block;">
				<div class="button">
					<input type="button" value="Back" onClick="javascript:submitForm('esignAction!validateName.action')" />
				</div>
			</div>
			
			<div class="form-field inline-right-button">
				<div class="button" style="margin-right: 50px;">
					<input type="submit" id="submitBtn" value="Submit" />
				</div>
			</div>
	
		</s:form>
			
	</div>
</div>

<script type="text/javascript">
	setNavigation({"bodyClass":"primary"});
</script>

<script type="text/javascript">
	$('document').ready(function() {
		$("#navigation").hide();
	});
</script>

