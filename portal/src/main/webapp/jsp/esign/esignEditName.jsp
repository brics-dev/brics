<%@include file="/common/taglibs.jsp"%>
<title>Electronic Signature</title>

<div class="clear-float">
	<h1 class="float-left">Legal Name Validation</h1>
</div>

<div class="border-wrapper wide">
	<div style="clear:both"></div>
	
	<div id="main-content">
		<div class="clear-float">
			<p>This use of electronic signatures in this system is a mandatory aspect of electronic data records, and 
			associated processes pertaining to electronic data collection, storage, access and submission within this 
			system. This requirement is governed by Title 21 CFR part 11 of the Code of Federal Regulations.</p>&nbsp;
			<p>The first step of this process is to validate your full legal name. Please review the following 
			information and make appropriate changes if needed. After reviewing this information, please confirm that 
			your full legal name is accurate and correct.</p>
		</div>
		
		<s:form id="theForm" cssClass="validate" method="post" validate="true" action="esignEditValidationAction!saveNameChanges.action">
	
			<div class="form-field">
				<label for="firstName" class="required">First Name <span class="required">* </span>:</label>
				<s:textfield id="firstName" name="firstName" cssClass="textfield required" maxlength="100" 
						escapeHtml="true" escapeJavaScript="true" />
				<s:fielderror fieldName="firstName" />
			</div>
	
			<div class="form-field">
				<label for="middleName">Middle Name :</label>
				<s:textfield id="middleName" name="middleName" cssClass="textfield" maxlength="100" 
						escapeHtml="true" escapeJavaScript="true" />
				<s:fielderror fieldName="middleName" />
			</div>
	
			<div class="form-field">
				<label for="lastName" class="required">Last Name <span class="required">* </span>: </label>
				<s:textfield id="lastName" name="lastName" cssClass="textfield required" maxlength="100" 
						escapeHtml="true" escapeJavaScript="true" />
				<s:fielderror fieldName="lastName" />
			</div>

			<div class="form-field" style="margin-top:10px;">
				<div class="button">
					<input type="submit" id="submitBtn" value="Save & Next" />
				</div>
				<s:a cssClass="form-link" action="esignAction" method="validateName" namespace="/esign">Cancel</s:a>
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

