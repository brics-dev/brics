<%@include file="/common/taglibs.jsp"%>
<title>Import Data Elements</title>

<s:set var="newDataElementList" value="newDataElementList" />
<s:set var="disabledList" value="disabledList" />
<s:set var="alreadyAttachedDataElementList" value="alreadyAttachedDataElementList" />
<s:set var="existingDataElementList" value="existingDataElementList" />

<body class="lightbox">

	

	<!-- begin .border-wrapper -->
	<div class="border-wrapper">
		<jsp:include page="../navigation/dataDictionaryNavigation.jsp" />
		
		<h1 class="float-left">Data Dictionary</h1>
<div class="clear-float"></div>
		<!--begin #center-content -->
		<div id="main-content" style="min-height:300px; margin-top:15px;">
		
			
				<h2>Import Data Elements Mapping</h2>
				
				<s:if test="hasActionErrors()">
					<div class="form-error">
						The schema mappings have not been processed because the uploaded file contains errors.  Please correct the errors below:
						<s:actionerror />
					</div>
				</s:if>
				<s:if test="hasActionMessages()">
					<div class="success-message">
						<s:actionmessage />
					</div>
				</s:if>
				
				<s:form name="uploadForm" class="validate" action="schemaMappingAction!adminUploadSchemaMapping.action" method="post" enctype="multipart/form-data">
					<s:token />
					<p>An administrator can import a data element mapping into the data dictionary. Please select a file to
						import the mapping. Note: The selected file must be in CSV format.</p> 
					<p>For reference purposes, you may download the <a href=<s:url value="/template/importUDE/SchemaTranslationTemplate.csv" />>Schema Import
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

				<s:if test="newDataElementList!=null">
					<h3>Available Data Elements</h3>
					<p>The following data elements were found in the CSV file. Please review these elements and select which ones
						you wish to import. Data Elements in the system or any elements with errors will not be available for import</p>
						<p>Text highlighted in red will overwrite pre-existing Data Elements </p>
					<div id="results">
						<jsp:include page="importList.jsp" />
					</div>
				</s:if>
			</div>

		
	</div>

	<script type="text/javascript">
	setNavigation({"bodyClass":"primary", "navigationLinkID":"dataDictionaryModuleLink", "subnavigationLinkID":"defineDataToolsLink", "tertiaryLinkID":"importDataElementSchemaLink"});
	
	function submitTheForm(theForm) {
		
		
		var theForm = document.forms['uploadForm'];
		theForm.action='schemaMappingAction!adminUploadSchemaMapping.action';
	    theForm.submit();
	}

</script>