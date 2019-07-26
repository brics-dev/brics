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
		
		<s:form id="theForm" cssClass="validate" method="post" validate="true" action="esignAction!esign.action">
			<div class="form-output">
				<div class="label">First Name:</div>
				<div class="readonly-text">
					<s:property value="currentAccount.user.firstName" />
				</div>
			</div>
			<div class="form-output">
				<div class="label">Middle Name:</div>
				<div class="readonly-text">
					<s:property value="currentAccount.user.middleName" />
				</div>
			</div>
			<div class="form-output">
				<div class="label">Last Name:</div>
				<div class="readonly-text">
					<s:property value="currentAccount.user.lastName" />
				</div>
			</div>
			
			<div class="form-field" style="margin-top:10px;">
				<div class="button">
					<input type="button" value="Edit Name" onClick="javascript:submitForm('esignAction!editName.action')" />
				</div>
				<div class="button" style="margin-left:10px;">
					<input type="submit" id="submitBtn" value="Next" />
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

