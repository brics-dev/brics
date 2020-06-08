<%@include file="/common/taglibs.jsp"%>
<title>Account Request</title>

<div class="clear-float">
	<h1 class="float-left">Account Management</h1>
</div>

<!-- begin .border-wrapper -->
<div class="border-wrapper wide">
	<div id="main-content">
<div class="clear-both"></div>
<div id="mainDiv" class="clear-float">

	<s:form id="theForm" name="yForm" cssClass="validate" method="post" validate="true" enctype="multipart/form-data">
		<s:token/>
		<input type="hidden" name="sessionFileNames" value="${sessionFileNames}" />
		<s:hidden id="isNTRRInstance" name="isNTRRInstance" />
		
		
		<s:if test="!alternateWorkflow()">
			<p>Thank you for your interest in the <span id="orgName"><s:property value="orgName" /></span> system. Please complete the following steps to request an account:</p>
			<ol start="1">
		</s:if>
		<s:else>
			<ol start="4">
		</s:else>

		<s:if test="instanceType.name == 'FITBIR'">
			<li>Determine which Account Permissions you require:
				<br><br>
				<h3>Account Types</h3><br>
				<ol>
					<li>Data Submitter with ProFoRMS: Select this option if you will be submitting data to FITBIR and will be 
					using the ProFoRMS module to collect data for your study. This permission will require you to submit the 
					"Data Submission Request" document found in the template table below. </li>
					<li>Data submitter without ProFoRMS: Select this option if you will be submitting data to FITBIR but will 
					NOT be using the ProFoRMS module to collect data for your study. This permission will require you to submit 
					the "Data Submission Request" document found in the template table below. </li>
					<li>Data Accessor: Select this option if you will be accessing data from FITBIR. This permission will require 
					you to submit the "Data Access Request" and "Biosketch" documents found in the template table below. </li>
					<li>Other: Please select Option as Other, if you have an account type need which doesn't fall in the above categories.</li>
				</ol>
				
				<br>
				<p>NOTE: If you would like to have both submitter and accessor privileges, you will be able to select two options 
				from above and will be required to submit the documentation required for both privileges. </p>
			</li>
		</s:if>
		
		<li>Download the appropriate template(s) from the list below.
			<br><br>
			<c:if test="${not empty currentUserFiles}">
			<h4>Existing Files</h4>
			<table id="existingFileTable" class="display-data full-width">
				<thead>
					<tr>
						<th>File Name</th>
						<th>File Type</th>
						<th>Date Submitted</th>
					</tr>
				</thead>
				<tbody>
					<c:forEach var="uploadedFile" items="${currentUserFiles}">
						<tr class="odd">
							<td><a href="fileDownloadAction!download.action?fileId=${uploadedFile.id}">${uploadedFile.name}</a></td>
							<td>${uploadedFile.description}</td>
							<td>${uploadedFile.uploadDateString}</td>
						</tr>
					</c:forEach>
				</tbody>
			</table>
		</c:if>
		
		<jsp:include page="includes/adminFileTemplatesInterface.jsp" />
			
		<ul>
			<li>Fill in the requested fields in the form(s) that require approval, then have it reviewed and approved 
				by your institution.</li>
			<li>Scan required form(s) and save to your computer.</li>
		</ul>
	</li>
	
	<li>Upload your approved file(s) to support your request here.  For each required form, select the appropriate File 
		Type and click Choose File (or Browse).  Find the approved file(s) on your computer and click Upload. <br><br>
		
		<c:if test="${!isAccountRejected}">
			<div><jsp:include page="includes/uploadAdminFileInterface.jsp" /></div>
		</c:if>
		<div class="clear-both"></div><br><br>
		<p style="color:#ff0000">
			<b>NOTE:  Verify that the uploaded file appears here before proceeding to the next step.<br>
				All account requests that do not have the required documents will not be approved.</b>
		</p>
	
		<s:if test="alternateWorkflow()">
			<li>
				<p>When all entries are complete, click SUBMIT REQUEST.</p>
				<p>The Approval Committee will review your request and notify you using the email address in your 
					Contact Information above.<br/> If you have any questions, contact <a href="mailto:<s:property value="%{orgEmail}"/>"">
					<s:property value="%{orgEmail}"/> </a>.</p>
			</li>
		</s:if>
	</li>

	</ol>
		
	<div class="form-field clear-left">
		
		<s:if test="alternateWorkflow()">
			<div class="button">
				<input type="button" id="submitReqBtnPd" value="Submit Request"  onclick="submitPdForm()"/>
			</div>
		</s:if>
		<s:else>
			<div style="display: none;" id="recaptchaErrorDialog"></div>
		
		<div id="account-recaptcha" class="g-recaptcha" data-sitekey="6LfWE3cUAAAAAC_QriM9wYQjzr1nL2irzSmBwxcg"></div>
			<div class="button">
				<input type="button" value="Continue" onClick="javascript:editPrivileges()" />
			</div>
		</s:else>
		
		<a class="form-link" href="javascript:window.location.href='/portal'">Cancel</a>
	</div>
	</s:form>
</div>
<!-- end of #main-content -->
</div>
</div>
<!-- end of .border-wrapper -->
<script type="text/javascript" src="/portal/js/account.js"></script>
<!--  recapthcha js -->
<script src='https://www.google.com/recaptcha/api.js'></script>
<!--  end of recapthca js -->
<script type="text/javascript">
	setNavigation({"bodyClass":"primary"});

	$('document').ready(function() {
		$("#navigation").hide();		
		$("#availabilityDisplay").hide();
		var orgName = $("#orgName").text();
		$("#orgName").text(orgName.replace(/_/g, ' '));
	});

	function submitPdForm(){
		var $table = $('#filesList');
		var rowCount = $('#filesList >tbody >tr').length;
		
		if(($table.length>0 && rowCount < 1 ) ||$table.length==0 ) {
			var r = confirm("We noticed that no document was uploaded with this request. If you do not require " +
					"access to restricted data, please click \"Ok\". If you would like access to restricted data, " + 
					"please click \"Cancel\" and complete the required documentation. Account requests that do not " + 
					"have the appropriate documentation will not be approved.");
			if (r == true) {
				theForm.action='accountAction!submit.action';
				theForm.submit();	
			}else{
				 return false;
			}
		}else{
			theForm.action='accountAction!submit.action';
			theForm.submit();	
		}
	}


	function editPrivileges() {
		
		var isNTRRInstance = $("#isNTRRInstance").val();
		var rowCount = $('#filesList >tbody >tr').length;
		
		if (rowCount < 1 && isNTRRInstance=== "true") {
			$.ibisMessaging("close", {type:"primary"}); 
			$.ibisMessaging("primary", "error", 'Supporting Documentation is required',{container: "#errorContainer"});
		} else if(rowCount<1){
			var r = confirm("We noticed that no document was uploaded with this request. " + "Account requests that do not have the required documents will not be approved.");
			if (r == true) {
				
				//verify recaptcha
				$.ajax({
					type : "POST",
					url : "/portal/ws/recaptcha/verify",
					cache : false,
					timeout : 10000,
					data : {response: grecaptcha.getResponse()},
					async : false,
					error : function(data) {
						
						$.ibisMessaging("close", {type:"primary"}); 
						$.ibisMessaging("primary", "error", 'There has been an error with ReCaptcha.',{container: "#recaptchaErrorDialog"});
						
						return false;//Don't sumbit and stay on the same form
					},
					success : function(data) {
						
						if(!data.success) {
							
							$.ibisMessaging("close", {type:"primary"}); 
							$.ibisMessaging("primary", "error", 'You need to complete the ReCaptcha form.',{container: "#recaptchaErrorDialog"});
							
							return false;//Don't sumbit and stay on the same form
						} else {
							
							//submit the form on recaptcha success
							window.location.href='/portal/publicAccounts/accountInfoAction!createDetails.action';
							
						}
						
						
					}
				});
				
				
				//end verify captcha
					
			} else {
	    		return false;
			}
		}
		else{
			
			
			//verify recaptcha
			$.ajax({
				type : "POST",
				url : "/portal/ws/recaptcha/verify",
				cache : false,
				timeout : 10000,
				data : {response: grecaptcha.getResponse()},
				async : false,
				error : function(data) {
					$.ibisMessaging("close", {type:"primary"}); 
					$.ibisMessaging("primary", "error", 'There has been an error with ReCaptcha.',{container: "#recaptchaErrorDialog"});
					
					return false;//Don't sumbit and stay on the same form
				},
				success : function(data) {
					
					if(!data.success) {
						
						$.ibisMessaging("close", {type:"primary"}); 
						$.ibisMessaging("primary", "error", 'You need to complete the ReCaptcha form.',{container: "#recaptchaErrorDialog"});
						return false;//Don't sumbit and stay on the same form
					} else {
						
						//submit the form on recaptcha success
						window.location.href='/portal/publicAccounts/accountInfoAction!createDetails.action';
			
						
					}
					
					
				}
			});
			
			
			//end verify captcha
			
		}	
	}

</script>