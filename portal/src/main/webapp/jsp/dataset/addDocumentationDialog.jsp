<%@taglib prefix="s" uri="/struts-tags"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>

<div class="lightbox-content-wrapper">
		<h3>Add File</h3>
		
	<s:form id="addDocForm" name="addDocForm" class="validate" method="post" enctype="multipart/form-data" 
			onsubmit="uploadDocument(); return false;">
		<s:token />
		
		<s:hidden id="validationActionName" name="validationActionName" />
		<s:hidden id="actionName" name="actionName" />
		<s:hidden id="isEditingDoc"  name="isEditingDoc" />
		<s:hidden id="addDocSelect"  name="addDocSelect" />
			
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
			<a class="form-link" href="#" onclick="$.fancybox.close();">Cancel</a>
		</div>
			
	</s:form>
</div>

<script type="text/javascript">

	$(document).ready(function() {
		<s:if test="isEditingDoc">
			var input = $("input[name=uploadSupportDoc]");
			var fileName = $("#uploadFileName").val();
		
			convertFileUpload(input, fileName);
		</s:if>
		
		$("#supportingDocDescription").bind("keyup", function() { 
			checkTextareaMaxLength(this.id, 1000); 
		});
	});
    
</script>
