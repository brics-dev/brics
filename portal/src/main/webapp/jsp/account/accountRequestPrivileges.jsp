<%@include file="/common/taglibs.jsp"%>
<title>Request an Account</title>

<div class="clear-float">
	<h1 class="float-left">Account Management</h1>
</div>

<!-- begin .border-wrapper -->
<div class="border-wrapper wide">
	<!--begin #center-content -->
	<div id="main-content">
		<div class="clear-float">
			<s:form id="theForm" cssClass="validate" method="post" validate="true" enctype="multipart/form-data">
				<s:token/>
				<p class="required">Fields marked with a <span class="required">* </span> are required.</p>
				<div class="clear-right"></div>
				<s:if test="alternateWorkflow()">
					<ol start="3">
				</s:if>
				<s:else>
					<ol start="4">
				</s:else>
					<li>
						Select the following privileges and permissions for this account.<br><br>
						<h3>Account Privileges</h3>
						<p>Choose your role (using the radio buttons): Each role will auto-populate recommended privileges below.</p>
						<jsp:include page="includes/accountPrivilegesInterface.jsp" />
						
						<s:if test="isRequest && (!inAccountAdmin)">
							<jsp:include page="includes/accountESignature.jsp" />
						</s:if>
						
					</li>
					<s:if test="!alternateWorkflow()">
						<li>
							<p>When all entries are complete, click SUBMIT REQUEST.</p>
							<p>The Approval Committee will review your request and notify you using the email address  
								in your Contact Information above.</br> If you have any questions, contact 
								<a href="mailto:<s:property value="%{orgEmail}"/>"><s:property value="%{orgEmail}"/></a>.
							 </p>
						</li>
					</s:if>
				</ol>
				
				<s:if test="alternateWorkflow()">
					<div style="margin-left:3em;">
						<div style="width:auto; font-size: 12px">
							Would you like to access Genomics and/or Neuroimaging data?
						</div>
						<div style="width:10%; float:left; margin-left: 45px">
							<label for="genomicsRadioGroupYes" style="width:auto;font-size: 12px;">
								<input type="radio" id="genomicsRadioGroupYes" name="genomicsRadioGroup" onclick="focusLogicOnYes()" />Yes
							</label>
						</div>
						<div style="width:10%; float:left">
							<label for="genomicsRadioGroupNo" style="width:auto;font-size: 12px">
								<input type="radio" id="genomicsRadioGroupNo" name="genomicsRadioGroup"  onclick="focusLogicOnNo()"/>No
							</label>
						</div>
					</div>

					<div class="form-field clear-left">
						<div class="button disabled" id="submitBtnDiv" style="margin-right: 10px;margin-left: 30px"  >
							<input type="submit" id="submitReqBtn" value="Submit Request" disabled="disabled" />
						</div>
						<div class="button disabled" id="continueBtnDiv" >
							<input type="button" id="continueBtnPd" value="Continue" onClick="javascript:saveAndLoadDUCPage()" disabled="disabled" />
						</div>
						<a class="form-link" href="javascript:window.location.href='/portal'">Cancel</a>	
					</div>				
				</s:if>
				<s:else>
					<div class="form-field clear-left">
						<div class="button">
							<input type="submit" id="submitReqBtn" value="Submit Request" />
						</div>
						<a class="form-link" href="javascript:window.location.href='/portal'">Cancel</a>					
					</div>
				</s:else>
				
			</s:form>
		</div>
		<!-- end of #main-content -->
	</div>
</div>
<!-- end of .border-wrapper -->

<script type="text/javascript" src="/portal/js/account.js"></script>

<script type="text/javascript">
	/** 
	 * Account role expiration date constructer.
	 *
	 * @param roleType - The role type or role ID.
	 * @param exprDate - The expiration date of the given role.
	 */
	function RoleExpiration(roleType, exprDate) {
		this.roleTypeId = roleType;
		this.expirationDate = exprDate;
	}
	
	
	$("document").ready(function() {
		
		// Hide the navigation since we're not logged in yet.
		$("#navigation").hide();
		
		// Removing documentation options that should not be uploaded on the account request screen
		$("#uploadDescription option:contains(Data Submission Request)").remove();
		$("#uploadDescription option:contains(Study Documentation)").remove();
		
		// Define a click handler for the submit button.
		$("#submitReqBtn").click(function() {
			 enableAllPrivileges();
			 
			// Change the action that the form will take when submitted.			
			<s:if test="isRequest && (!inAccountAdmin)">
				$("#theForm").attr("action", "accountSignatureValidationAction!submit.action");
			</s:if>
			<s:else>
				$("#theForm").attr("action", "accountAction!submit.action");
			</s:else>
			
			// Prepare all of the expiration date fields in the included accountPrivilegesInterface.jsp for submission.
			processHiddenExpireDateFields();
			convertExpirationDatesToJSON();
		});
	});
	
	function focusLogicOnYes(){
		if ($("#genomicsRadioGroupYes").is(":checked")) {
			$("#continueBtnPd").prop("disabled",false);
			$("#submitReqBtn").prop("disabled",true);
			$("#submitBtnDiv").addClass("disabled"); 
			$("#continueBtnDiv").removeClass("disabled");
			
			alert("Access to PDBP Genetic, Genomics, and/or Neuroimaging data requires a signed PDBP Genomics & " +
				"Imaging Data Use Certificate (DUC). The PDBP Genomics & Imaging DUC is available for review and " +
				"download in the Administrative File Templates section. A signature from the Requestor and the " + 
				"Requestor’s Authorized Institutional Business Official is required. Once the DUC is signed, upload " +
				"the document to your account for review and approval. Please note that access to Genetics, " +
				"Genomics, and/or Neuroimaging data must be renewed on an annual basis. Failure to renew and provide " +
				"an updated DUC will result in revocation of data access/privileges.");
		}
	}
	function focusLogicOnNo(){
		if ($("#genomicsRadioGroupNo").is(":checked")) {
			$("#submitReqBtn").prop("disabled",false);
			$("#continueBtnPd").prop("disabled",true);
			$("#continueBtnDiv").addClass("disabled"); 
			$("#submitBtnDiv").removeClass("disabled");
		}
	}
	
	function saveAndLoadDUCPage(){
		enableAllPrivileges();
		var formData = $('#theForm').serialize();
		
		<s:if test="isRequest && (!inAccountAdmin)">
			theForm.action="accountSignatureValidationAction!duc.action";
		</s:if>
		<s:else>
			theForm.action="ducAction!duc.action";
		</s:else>

		theForm.submit();	
		// Prepare all of the expiration date fields in the included accountPrivilegesInterface.jsp for submission.
		processHiddenExpireDateFields();
		convertExpirationDatesToJSON();
	}
	
</script>