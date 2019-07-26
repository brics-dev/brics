<%@include file="/common/taglibs.jsp"%>
<input id="existingFileTypes" type="hidden" name="existingFileTypes" value="${existingFileTypes}" />

<div class="flex-vertical flex-justify-around">
	<h3>Upload Supporting Documentation</h3>
	<p>Please upload your signed administrative documentation to
		support your request here.</p>
		
	<input type="hidden" name="userId" id="userId" value="<s:property value="currentAccount.user.id" />" />
	<div class="form-field flex-justify-between flex-no-wrap">
		<div class="flex-no-wrap">
			<label for="uploadDescription" class="required">File Type<span
				class="required">* </span>:
			</label>
			<s:select id="uploadDescription" cssClass="float-left" width="200"
				list="adminFileTypes" listKey="id" listValue="name"
				name="uploadDescription" value="adminFileTypes.id" headerKey=""
				headerValue="- Select One -" escapeHtml="true"
				escapeJavaScript="true" for="uploadDocumentation" />
		</div>
		<div class="flex-no-wrap">
			<s:fielderror fieldName="uploadDescription" />
		</div>
	</div>

	<div class="form-field flex-justify-between flex-no-wrap">
		<div class="flex-no-wrap">
			<label for="uploadFile">&nbsp;</label>
			<s:file id="fileBrowse" name="upload" for="uploadDocumentation"
				cssClass="file-upload" />
		</div>
		<div class="flex-no-wrap">
			<s:fielderror fieldName="upload" />
			<s:fielderror fieldName="uploadFileName" />
		</div>
	</div>
</div>