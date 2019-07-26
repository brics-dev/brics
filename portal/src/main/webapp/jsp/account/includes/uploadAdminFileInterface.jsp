<%@include file="/common/taglibs.jsp"%>

<input type="hidden" name="isApproved" value="${isApproved}" />
<input type="hidden" name="existingFileTypes" />

<div id="errorContainer" style="display: none"></div>
<div id="uploadedFilesDiv">
	<c:if test="${not empty uploadedFiles}">
		<s:if test="%{inAccounts}"><h4>My Uploaded Files</h4></s:if>
		<s:elseif test="%{inAccountAdmin}"><h4>User's Uploaded Documents</h4></s:elseif>
	</c:if>

	<jsp:include page="../../common/uploadDocuments.jsp" />
	</div>

	<h3>Upload Supporting Documentation</h3>
	<p>Please upload your signed administrative documentation to support your request here. Selected templates are available below. </p>
	<div class="form-field">
		<label for="uploadDescription" class="required">File Type<span class="required">* </span>:</label>
		<s:select id="uploadDescription" cssClass="float-left" width="200" list="adminFileTypes" listKey="id" 
				listValue="name" name="uploadDescription" value="adminFileTypes.id" headerKey="" 
				headerValue="- Select One -" escapeHtml="true" escapeJavaScript="true" />
		<s:fielderror fieldName="uploadFileName" />		
		<s:fielderror fieldName="uploadDescription" />
	</div>
	<div class="form-field">
		<label for="uploadFile">&nbsp;</label>
		<s:file id="fileBrowse" name="upload" cssClass="file-upload" />
	</div>
	<div class="button margin-left">
		<input type="button" value="Upload" onClick="javascript:uploadDocument()" />
	</div>
	
<s:hidden id="removeDuplicate" name="removeDuplicate" value="false" />
<script type="text/javascript" src="/portal/js/account.js"></script>
<script type="text/javascript">

	function uploadDocument() {
		// Prepare all of the expiration date fields in the included requestAdditionalPrivileges.jsp for submission.
		processHiddenExpireDateFields();
		convertExpirationDatesToJSON();
		
		var fileName = document.getElementById("fileBrowse").value;
		var filesList = new Array();
		if(fileName != ""){
			$('#filesList>tbody>tr>td:nth-child(1)').each(function() {   	
				filesList.push($(this).text());
			 });
			
			if(filesList.length > 0){
				for(var i = 0; i<filesList.length; i++){
					if(fileName == filesList[i]){
						var r=confirm("Would you like to replace the existing file?");
						if (r==true){
							$("#removeDuplicate").val("true");
						  }
						else{
						  return;
						  } 
					}
				}
			}

			var theForm = document.getElementById('theForm');
			$(theForm).find('input:checked').prop('disabled', false);
			theForm.action = 'accountDetailsUploadValidationAction!upload.action';
			theForm.submit();
		}else{
			$.ibisMessaging("close", {type:"primary"}); 
			$.ibisMessaging("primary", "error", 'File Type is a required field',{container: "#errorContainer"});
			$.ibisMessaging("primary", "error", 'File Type must be selected for upload',{container: "#errorContainer"});	
		}
	}
	
	function removeFile(fileName){
		
		$.post("accountDetailsRemoveValidationAction!removeFile.ajax", {
			uploadFileName : fileName
		}, function(data) {
			newDiv = $(data).filter("#uploadedFilesDiv").html();
			$('#uploadedFilesDiv').html(newDiv);
		});
		
	}
	
</script>