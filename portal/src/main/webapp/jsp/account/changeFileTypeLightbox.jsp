<%@include file="/common/taglibs.jsp"%>
<input id="changeFileTypeExistingFileTypes" type="hidden" name="existingFileTypes" />

<label for="uploadDescription" class="required">File Type<span class="required">* </span>:</label>
<s:select id="changeFileType" cssClass="float-left" width="200" list="adminFileTypes" listKey="id" 
				listValue="name" name="uploadDescription" value="adminFileTypes.id" headerKey="" 
				headerValue="- Select One -" escapeHtml="true" escapeJavaScript="true" />
				
				<s:fielderror fieldName="uploadDescription" />
				<span id="validateChangeFileType" style="display: none">
						<img class="icon-warning" alt="" src="/portal/images/brics/common/icon-warning.gif">
						<span class="required"><strong>Please select a new file type to change to</strong></span>
				 </span>
				 
