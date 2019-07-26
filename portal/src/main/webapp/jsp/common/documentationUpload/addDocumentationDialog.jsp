<%@taglib prefix="s" uri="/struts-tags"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>

<div class="lightbox-content-wrapper">
	<s:if test="addDocSelect == 'url'">
		<h3>Add URL</h3>
	</s:if>
	<s:else>
		<h3>Add File</h3>
	</s:else>
		
	<s:form id="addDocForm" name="addDocForm" class="validate" method="post" enctype="multipart/form-data" 
			onsubmit="uploadDocument(); return false;">
		<s:token />
		<s:if test="hasActionErrors()">
			<div class="errors">
				<s:actionerror />
			</div>
		</s:if>
		
		<s:if test="hasFieldErrors()">
			<input id="hasFieldErrors" value="true" type="hidden"/>
		</s:if>
		
		<s:hidden id="validationActionName" name="validationActionName" />
		<s:hidden id="actionName" name="actionName" />
		<s:hidden id="isEditingDoc"  name="isEditingDoc" />
		<s:hidden id="addDocSelect"  name="addDocSelect" />
		
		<s:if test="isEditingDoc">
			<input id="isEditingDoc" value="true" type="hidden"/>
		</s:if>
		
		<s:if test="addDocSelect == 'url'">
			<div class="form-field">
				<label for="url" class="required">URL <span class="required">* </span>
				</label>
				<table>
					<tr><td>
						<s:textfield id="url" name="url" cssClass="textfield required" escapeHtml="true" escapeJavaScript="true"/>
						<s:fielderror fieldName="url" />
					</td></tr>
					<tr><td style="padding-top: 3px">
						<a id="testUrlLink" href="javascript:void(0)" onclick="testUrl()">Test URL</a>
					</td></tr>
				</table>
			</div>
		</s:if>
		
		<s:if test="addDocSelect == 'file'">
			<div class="form-field">
				<label for="file" class="required">File<span class="required">* </span>
				</label>
				<s:file id="file" name="uploadSupportDoc" />
				<s:hidden id="uploadFileName" name="uploadFileName" />
				<table align="right">
					<tr><td><s:fielderror fieldName="uploadFileName" /></td></tr>
					<tr><td><s:fielderror fieldName="uploadSupportDoc" /></td></tr>
				</table>
			</div>
		</s:if>
		
		<s:if test="%{supportingDocTypes.size == 1}">
			<div class="form-field hidden">
		</s:if>
		<s:else>
			<div class="form-field">
		</s:else>
			<label for="supportingDocType" class="required">Type<span class="required">* </span>
			</label>
			<s:select id="supportingDocType" list="supportingDocTypes" listKey="id" listValue="name" 
					name="supportingDocType" value="supportingDocType.id" />
			<s:fielderror fieldName="supportingDocType" />
			</div>
		
		<s:fielderror fieldName="supportingDocType" />
		<div class="form-field">
			<label for="description" class="required">Description<span class="required">* </span>
			</label>
			<s:textarea id="supportingDocDescription" name="supportingDocDescription" cols="30" rows="5" cssClass="textfield required"
					escapeHtml="true" escapeJavaScript="true" />
			<s:fielderror fieldName="supportingDocDescription" />
		</div>
		
		<div class="form-field">
			<div class="button">
				<input id="saveFile" type="button" class="submit" value="Save" onclick="uploadDocument()" />
			</div>
			<a class="form-link" href="#" onclick="$.fancybox.close();$('#bricsDialog_0').dialog('close');">Cancel</a>
		</div>
			
	</s:form>
</div>
<script type="text/javascript">

    function testUrl() {
    	var url = $("#url").val();
    	
    	if (!url || url.trim().length == 0) {
    		alert("Warning: URL field is empty!");
    		return;
    	}
    	
    	if (!url.match(/^(f|ht)tps?:\/\//i)) {
            url = 'http://' + url;
        }
    	
    	window.open(url, "_blank");
    	window.focus();
    }

    
	$(document).ready(function() {
		
		var hasFieldErrors = $("input#hasFieldErrors").val();
		var isEditingDoc = $("input#isEditingDoc").val();
		var fileExistInSession = false;
		
		if(hasFieldErrors == "true"){
			var fileFromSession = '${sessionUploadFile.uploadFile}';
			if(fileFromSession != null){ 
				fileExistInSession="true";
				$("uploadSupportDoc").val(fileFromSession);
			}
		}
		if(isEditingDoc == "true" || fileExistInSession == "true"){
			var input = $("input[name=uploadSupportDoc]");
			var fileName = $("#uploadFileName").val();
		
			convertFileUpload(input, fileName);
		}
		
		$("#supportingDocDescription").bind("keyup", function() { 
			checkTextareaMaxLength(this.id, 1000); 
		});
	});
    
</script>
