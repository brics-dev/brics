<jsp:include page="/common/doctype.jsp" />
<%@ taglib uri="/struts-tags" prefix="s" %>
<%@ taglib uri="/WEB-INF/security.tld" prefix="security" %>

<html>
	<jsp:include page="/common/popUpHeader_struts2.jsp" />
	
	<body>
		<jsp:include page="/common/messages_struts2.jsp" />
		<s:form enctype="multipart/form-data" id="filefolder-editfileinnerform" theme="simple" action="studyEbinder" method="post">
			<s:hidden name="attachId" id="fileFolder-fileId"/>
			<s:hidden name="attachFileFileName" id="fileFolder-uploadedFileName"/>
			
			<div class="formrow_1">
				<label class="requiredInput">Title111</label>
				<s:textfield name="attachName" id="fileFolder-fileName"/>
			</div>
			<div class="formrow_1">
				<label>Authors</label>
				<s:textfield name="attachAuthor" id="fileFolder-fileAuthor"/>
			</div>
			<div class="formrow_1">
				<label class="requiredInput">Description</label>
				<s:textarea name="attachDescription" id="fileFolder-fileDesc"/>
			</div>
			<div class="formrow_1">
				<label>Type</label>
				<s:textfield name="attachPubType" id="fileFolder-fileType"/>
			</div>
			<div class="formrow_1">
				<label>Upload File</label>
				<s:file name="attachFile" id="fileFolder-fileFile"/>
			</div>
			<div class="formrow_1">
				<label>URL</label>
				<s:textfield name="attachUrl" id="fileFolder-fileUrl"/>
			</div>
			<div class="formrow_1">
				<label>PubMed ID</label>
				<s:textfield name="attachPubMedId" id="fileFolder-filePubMedId"/>
			</div>
		</s:form>
	</body>
</html>