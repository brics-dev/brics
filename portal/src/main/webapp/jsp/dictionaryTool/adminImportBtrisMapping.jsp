<%@include file="/common/taglibs.jsp"%>
<title>Import BTRIS Mapping</title>

<body class="lightbox">

	<!-- begin .border-wrapper -->
	<div class="border-wrapper">
		<jsp:include page="../navigation/dataDictionaryNavigation.jsp" />
		
		<h1 class="float-left">Data Dictionary</h1>
		<div class="clear-float"></div>
		<!--begin #center-content -->
		<div id="main-content" style="min-height:300px; margin-top:15px;">
		
			
				<h2>Import BTRIS Mapping</h2>
				
				<s:if test="hasActionErrors()">
					<div class="form-error">
						The BTRIS mappings have not been processed because the uploaded file contains errors.  Please correct the errors below:
						<s:actionerror />
					</div>
				</s:if>
				<s:if test="hasActionMessages()">
					<div class="success-message">
						<s:actionmessage />
					</div>
				</s:if>
				
				<s:form name="uploadForm" class="validate" action="btrisMappingAction!adminUploadBtrisMapping.action" method="post" enctype="multipart/form-data">
					<s:token />
					<p>An administrator can import a BTRIS mapping into the data dictionary. Please select a file to
						import the mapping. Note: The selected file must be in CSV format.</p> 
					<p>For reference purposes, you may download the <a href=<s:url value="/template/importUDE/BtrisMappingTemplate.csv" /> >BTRIS Mapping Import
							Template</a>.
					</p>
					<div class="form-field">
						<label for="new_file_upload">File:</label>
						<s:file name="upload" cssClass="textfield float-left" />
						<div style="width:50%;">
							<s:fielderror fieldName="upload"/><BR>
						</div>
					</div>
					<div class="form-field">
						<div class="button">
							<input type="button" value="Upload" onClick="javascript:  submitTheForm('uploadForm');" />
						</div>
					</div>
				</s:form>

			</div>

		
	</div>

	<script type="text/javascript">
	setNavigation({"bodyClass":"primary", "navigationLinkID":"dataDictionaryModuleLink", "subnavigationLinkID":"defineDataToolsLink", "tertiaryLinkID":"importBtrisMappingLink"});
	
	function submitTheForm(theForm) {
		
		
		var theForm = document.forms['uploadForm'];
		theForm.action='btrisMappingAction!adminUploadBtrisMapping.action';
	    theForm.submit();
	}

</script>