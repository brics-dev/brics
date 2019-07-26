<%@taglib prefix="s" uri="/struts-tags"%>

<div class="lightbox-content-wrapper">
	<h3>Add File</h3>
	
	<s:form id="addDataFileForm" class="validate" method="post" enctype="multipart/form-data" onsubmit="uploadDataFile(); return false;">
		<s:if test="hasFieldErrors()">
			<div style="width:980px">&nbsp;<!-- fixes fancybox size because fancybox is terrible --></div>
			<input id="hasFieldErrors" value="true" type="hidden"/>
		</s:if>
		<s:hidden id="isEditingData"  name="isEditingData" />
		
		<s:if test="isEditingDoc">
			<input id="isEditingDoc" value="true" type="hidden"/>
		</s:if>
		
		<div class="form-field">
			<label for="file" class="required" style="width: 100px;">File<span class="required"> * </span>
			</label>
			<s:file id="uploadData" name="uploadData" />
			<s:hidden id="uploadFileName" name="uploadFileName" />
			<table align="right">
				<tr><td><s:fielderror fieldName="uploadFileName" /></td></tr>
				<tr><td><s:fielderror fieldName="uploadData" /></td></tr>
			</table>
		</div>
		
		<div class="form-field">
			<label for="dataFileType" class="required" style="width: 100px;">Type<span class="required"> * </span>
			</label>
			<s:select id="dataFileType" list="metaStudyDataTypes" listKey="id" listValue="name" 
					name="dataFileType" value="dataFileType.id" onchange="checkFileType(this)" />
			<s:fielderror fieldName="dataFileType" />
		</div>
		
		<div id="versionDiv" class="form-field" style="${dataFileType.name != 'Software' ? 'display:none' : ''}">
			<label for="version" class="required" style="width: 100px;">Version<span class="required"> * </span>
			</label>
			<s:textfield type="text" id="version" name="version" cssClass="textfield" maxlength="10" />
			<s:fielderror fieldName="version" />
		</div>
		
		<div class="form-field">
			<label for="dataDescription" class="required" style="width: 100px;"">Description<span class="required"> * </span>
			</label>
			<s:textarea id="dataDescription" name="dataDescription" cssClass="textfield required"
					cols="30" rows="5" escapeHtml="true" escapeJavaScript="true" />
			<s:fielderror fieldName="dataDescription" />
		</div>
		
		<div class="form-field">
			<label for="dataSource" class="required" style="width: 100px;">Source<span class="required"> * </span>
			</label>
			<s:textfield type="text" id="dataSource" name="dataSource" cssClass="textfield"
					maxlength="255" escapeHtml="true" escapeJavaScript="true" />
			<s:fielderror fieldName="dataSource" />
		</div>
		
		<div class="form-field">
			<div class="button">
				<input type="button" id="dataDialogSubmit" class="submit" value="Save" onclick="uploadDataFile()" />
			</div>
			<a class="form-link" href="#" onclick="$.fancybox.close();">Cancel</a>
		</div>
		
	</s:form>
</div>

<script type="text/javascript">

	function checkFileType(select) {
		var fileType = select.options[select.selectedIndex].text;
		
		if (fileType == 'Software') {  // Show version field if user selects Software type.
			$("#versionDiv").css("display", "block");
		} else {
			$("#version").removeAttr('value');
			$("#versionDiv").css("display", "none");
		}
	}

	$(document).ready(function() {
		
		var hasFieldErrors = $("input#hasFieldErrors").val();
		var isEditingDoc = $("input#isEditingDoc").val();
		var fileExistInSession = false;
		
		if(hasFieldErrors == "true"){
			var fileFromSession = '${sessionUploadFile.uploadFile}';
			if(fileFromSession != null){ 
				fileExistInSession="true";
				$("uploadData").val(fileFromSession);
			}
		}
		if(isEditingDoc == "true" || fileExistInSession == "true"){
			var input = $("input[name=uploadData]");
			var fileName = $("#uploadFileName").val();
			
			convertFileUpload(input, fileName);
		}
		
		$("#dataDescription").bind("keyup", function() { 
			checkTextareaMaxLength(this.id, 1000); 
		});
	});

</script>
