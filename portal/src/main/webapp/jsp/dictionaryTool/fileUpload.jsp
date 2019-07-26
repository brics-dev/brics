<%@taglib prefix="s" uri="/struts-tags"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn"%>

<%@page import="gov.nih.tbi.PortalConstants"%>

		<div class="lightbox-content-wrapper">
			<s:form name="theForm" class="validate" action="%{actionName}!commitChange.ajax" method="post"
				enctype="multipart/form-data">
				<s:token />
				<s:if test="hasActionErrors()">
					<div class="errors">
						<s:actionerror />
					</div>
				</s:if>
				<h3>File Upload</h3>
				<div class="form-field">
					<ul class="checkboxgroup" id="file-upload">
						<li><input type="radio" name="file_upload_type" id="reuse_existing" class="radio"
							value="<%=PortalConstants.DOC_TYPE_OLD_FILE%>"></input><label for="reuse_existing">Reuse Existing
								Uploaded Document</label>
							<c:choose>
         						<c:when test="${empty userFiles}">
									<div id="nofile-warning" class="form-field textfield-other red-text">You have not uploaded any files that are attachable.</div>
									</c:when>
      							<c:otherwise>
        							<div id="file" class="form-field textfield-other">
									<label for="user_files">User files:</label>
									<s:select id="user_file_id" name="user_file_id" list="userFiles" listValue="name" listKey="id"
										value="documentationId" onchange="javascript: ajaxDescription()" />
									
									</div>
									<div class="form-field textfield-other">
									<label for="file_description">Description:</label>
								<div id="description_field" class="readonly-text"></div>
							</div></li>
							</c:otherwise>
    					  </c:choose>
          				
						<li><input type="radio" name="file_upload_type" id="new_file_document" class="radio"
							value="<%=PortalConstants.DOC_TYPE_NEW_FILE%>"></input><label for="new_file_document">New File Document</label>
							<div class="form-field textfield-other">
								<label for="new_file_upload">File:</label>

								<s:file name="upload" cssClass="textfield float-left" cssStyle="width:225px;"/>

							</div>
							<div class="form-field textfield-other">
								<label for="new_file_upload_desc">Description:</label>
								<!--                                         <input type="text" name="" id="new_file_upload_desc" maxlength="50" class="textfield" value="" /> -->
								<s:textfield name="uploadDescription" cssClass="textfield" escapeHtml="true" escapeJavaScript="true" />
							</div></li>
						<li><input type="radio" name="file_upload_type" id="url_document" class="radio"
							value="<%=PortalConstants.DOC_TYPE_URL%>"></input><label for="url_document">URL Document</label>
							<div id="url" class="form-field textfield-other">
								<label for="new_url">URL:</label> <input type="text" name="new_url" id="new_url" maxlength="240"
									class="textfield" value="<s:property value='documentationUrl' />"></input>
							</div></li>
						<li><input type="radio" name="file_upload_type" id="none" class="radio"
							value="<%=PortalConstants.DOC_TYPE_NONE%>"></input><label for="none">None</label></li>
					</ul>
				</div>
				<div class="form-field">
					<s:if test="currentDataStructure.documentationFileId!=null">
										<input type="submit" onclick="return overwriteDocument()" class="submit" value="Submit"></input>
				
					</s:if> <s:elseif test="currentDataStructure.documentationUrl!=null">
													<input type="submit" onclick="return overwriteDocument()" class="submit" value="Submit"></input>

					</s:elseif> <s:else>
					<div class="button">
						<input type="submit" class="submit" value="Submit"></input>
					</div>
					</s:else>
				</div>
			</s:form>
		
		</div>

	<script type="text/javascript">
		$('document').ready(function() {
			openCorrectRadio();
			ajaxDescription();
			//set new token, this is a hijack
			
			$('[name="token"]').val(globalToken);
		});

		setNavigation({
			"bodyClass" : "primary",
			"navigationLinkID" : "",
			"subnavigationLinkID" : ""
		});

		function openCorrectRadio() {
			var radioName = '<s:property value="documentationType" />';

			if (radioName == 'none') {
				$("#none").click();
// 				var selected = document.getElementById('none');
// 				selected.click();
			} else if (radioName == 'file') {
				ajaxDescription();
				$("#reuse_existing").click();
// 				var selected = document.getElementById('reuse_existing');
// 				selected.click();
			} else if (radioName == 'url') {
				$("#url_document").click();
// 				var selected = document.getElementById('url_document');
// 				selected.click();
			}

			var size = '<c:out value="${fn:length(userFiles)}" />';
			if (size > 0) {
				$('#nofile-warning').hide();
			}
		}

		function ajaxDescription() {
			var selectedValue = $('#user_file_id').val();

			var action = '<c:out value="${actionName}" />!getDescription.ajax?fileId='
					+ selectedValue;

			$.post(action, {}, function(data) {
				$('#description_field').html(data);
			});
		}
		
		function overwriteDocument(){
			return confirm("Only one file can be attached to a form structure. By uploading this document, you will overwrite the current file. Would you like to proceed?");
		}
	</script>


