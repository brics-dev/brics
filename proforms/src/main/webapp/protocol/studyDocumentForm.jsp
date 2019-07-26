<jsp:include page="/common/doctype.jsp" />
<%@ page import="org.apache.struts.upload.MultipartRequestHandler" %>
<%@ page import="gov.nih.nichd.ctdb.common.StrutsConstants"%>
<%@ page import="gov.nih.nichd.ctdb.protocol.common.ProtocolConstants" %>
<%@ page import="gov.nih.nichd.ctdb.common.rs,java.util.Locale" %>
<%@ taglib uri="/struts-tags" prefix="s" %>
<%@ taglib uri="/WEB-INF/display.tld" prefix="display"%>
<%@ taglib uri="/WEB-INF/security.tld" prefix="security"%>

<%-- CHECK PRIVILEGES --%>
<security:check privileges="addeditpublications" />
<% Locale l = request.getLocale(); %>
<html>
	<s:set var="pageTitle" scope="request">
		<s:text name="study.documents.title"/>
	</s:set>
    <%-- Include Header --%>
	<jsp:include page="/common/header_struts2.jsp"/>
	
	<script type="text/javascript">
		var webRoot = "<s:property value='#webRoot'/>";
		
		function downloadFile(attachId, assocObjId, typeId)
		{
			// Set download form data
			$("#attachId").val(attachId);
			$("#attachAssocId").val(assocObjId);
			$("#attachTypeId").val(typeId);
			
			// Submit the download form
			$("#downloadForm").submit();
		}
		
		$(document).ready(function()
		{
			// Check if the page should be in edit mode
			if ($("#docId").val() > 0) {
				$("#docEditSubmBtn").show();
				$("#docAddSubmBtn").hide();
			}
			
			// ++++++ Event Listners ++++++
			
			// A listener for all text fields when a change occurs
			$("input:text").change(function(event) {
				// Remove all leading and trailing white spaces
				$(this).val(jQuery.trim($(this).val()));
			});
			
			// Document form cancel button listener
			$("#docCancelBtn").click(function(event) {
				redirectWithReferrer(webRoot + "/protocol/studyDocument.action");
			});
			
			// Edit document button click listener
			$("#docEditBtn").click(function(event)
			{
				var url = webRoot + "/protocol/studyDocument.action?id=";
				var docId = IbisDataTables.getSelectedOptions(IbisDataTables.getTable($("#docDisplayTable")));
				
				redirectWithReferrer(url + docId[0]);
			});
			
			// Delete document button click listener
			$("#docDeleteBtn").click(function(event)
			{
				var message = "Are you sure you want to delete the selected document(s)?"
				
				$.ibisMessaging("dialog", "info", message, {
					modal : true,
					buttons : [{
						id : "yesBtn",
						text : "Yes",
						
						click : function() {
							// The "Yes" button
							$(this).siblings().find("#yesBtn").prop("disabled", true);
							
							var url = webRoot + "/protocol/deleteDocument.action?idsToDelete=";
							var docIds = IbisDataTables.getSelectedOptions(IbisDataTables.getTable($("#docDisplayTable")));
							var params = "";
							
							// Convert the array of IDs to a comma delimited string list
							for (var idx = 0; idx < docIds.length; idx++) {
								params += docIds[idx];
								
								if ((idx + 1) < docIds.length) {
									params += ",";
								}
							}
							
							redirectWithReferrer(url + params);
						}
					}, {
						text : "No",
						
						click : function() {
							$(this).dialog("close");
						}
					}]
				});
			});
		});
	</script>
	
	<p><s:text name="study.documents.instruction"/></p>
	<h3 class="toggleable"><s:text name="study.documents.addedit.title"/></h3>
	<div id="addEditDocumentSection">
		<s:form enctype="multipart/form-data" id="documentForm" theme="simple" method="post">
			<s:hidden name="id" id="docId" />
			<s:hidden name="studyId" id="studyId"/>
			<s:hidden name="attachmentType" id="attachType"/>
			
			<label class="requiredInput"></label> 
			<i><s:text name="protocol.create.requiredSymbol.display"/></i>
			<br/><br/>
			<div class="formrow_1">
				<label class="requiredInput" for="docTitle"><s:text name="study.documents.title.display"/></label>
				<s:textfield id="docTitle" name="title" maxlength="256" />
			</div>
			<div class="formrow_1">
				<label for="docAuthors"><s:text name="study.documents.authors.display"/></label>
				<s:textfield id="docAuthors" name="authors" maxlength="512" />
			</div>
			<div class="formrow_1">
				<label class="requiredInput" for="docDescription"><s:text name="study.documents.description.display"/></label>
				<s:textfield id="docDescription" name="description" maxlength="4000" />
			</div>
			<div class="formrow_1">
				<label for="docPubType"><s:text name="study.documents.type.display"/></label>
				<s:select id="docPubType" name="publicationType" list="#session._publication_types" listKey="id" listValue="shortName" />
			</div>
			<div class="formrow_1">
				<label for="docFile"><s:text name='study.documents.upload.display'/></label>
				<s:file id="docFile" name="fileUpload" />
			</div>
			<div class="formrow_1">
				<label>&nbsp;</label>
				<span class="formrowinput"><s:text name="study.documents.fileSizeMax.display"/></span>
			</div>
			<s:if test="%{(id != null) && (@java.lang.Integer@valueOf(id) > 0)}">
				<div class="formrow_1">
					<label><s:text name="attachment.uploadedFile.display"/></label>
					<a href="javascript:;" onclick="downloadFile(<s:property value='id'/>, <s:property value='studyId'/>, 
						<s:property value='attachmentType'/>)"><s:property value="fileUploadFileName" /></a>
				</div>
			</s:if>
			<div class="formrow_1">
				<label for="docUrl"><s:text name="study.documents.url.display"/></label>
				<s:textfield id="docUrl" name="url" maxlength="255" />
			</div>
			<div class="formrow_1">
				<label for="docPubMedId"><s:text name="study.documents.pubMedID.display"/></label>
				<s:textfield id="docPubMedId" name="pubmedId" maxlength="255" />
			</div>
			<div class="formrow_1">
				<input type="button" id="docCancelBtn" value="<s:text name='button.Cancel'/>" title="Click to cancel (changes will not be saved)." />
				<input type="reset" value="<s:text name='button.Reset'/>" title="Click to clear fields"/>
				<s:submit action="saveDocument" id="docEditSubmBtn" key="button.studyDoc.UpdateDocument" 
					cssClass="hidden" title="Click to update document" />
				<s:submit action="saveDocument" id="docAddSubmBtn" key="button.studyDoc.AddDocument" title="Click to add document" />
			</div>
		</s:form>
	</div>
	
<%-- ################################################## Download File Form ###################################################################### --%>
	<div class="hidden">
		<s:form action="download" enctype="multipart/form-data" id="downloadForm" namespace="/attachments">
			<s:hidden name="id" id="attachId"/>
			<s:hidden name="associatedId" id="attachAssocId"/>
			<s:hidden name="typeId" id="attachTypeId"/>
		</s:form>
	</div>
<%-- ################################################## Download File Form ###################################################################### --%>
	
	<h3><s:text name="study.documents.myDocuments.title"/></h3>
	<p><s:text name="study.documents.myDocuments.subinstruction"/></p>
	<br/>
	<div class="dataTableContainer" id="docDisplayTable">
		<ul>
			<li>
				<input type="button" id="docEditBtn" value="<s:text name='button.Edit'/>" title="Click to make changes" />
			</li>
			<li>
				<input type="button" id="docDeleteBtn" value="<s:text name='button.Delete'/>" class="enabledOnMany" title = "Click to delete" />
			</li>
		</ul>
		<display:table name="<%= ProtocolConstants.PUBLICATION_LIST %>" scope="request" decorator="gov.nih.nichd.ctdb.protocol.tag.StudyDocumentDecorator">
			<display:setProperty name="basic.msg.empty_list" value="There are no documents to display at this time."/>
			<display:column property="docCheckbox" title="" />
			<display:column property="downloadTitle" title='<%=rs.getValue("study.documents.title.display", l)%>' />
			<display:column property="description" title='<%=rs.getValue("study.documents.description.display", l)%>'/>
			<display:column property="docType" title='<%=rs.getValue("study.documents.type.display", l)%>'/>
		</display:table>
	</div>
	
	<%-- Include Footer --%>
	<jsp:include page="/common/footer_struts2.jsp" />
</html>