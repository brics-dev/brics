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
		
			
				<h2>Import Data Elements</h2>
				<s:form name="uploadForm" class="validate" action="importDataElementAction!adminUpload.action" method="post" enctype="multipart/form-data">
					<s:token />
					<p>An administrator can import both common and unique data elements into the data dictionary. Please select a file to
						import data elements to this data structure. Note: The selected file must be in CSV format.</p> 
					<p>For reference purposes, you may download the 
					<s:if test="isNTRRInstance">
						<a href=<s:url value="/template/importUDE/Data_Element_Import_Template_NTI_NTRR.csv" />>Import Template</a>.
					</s:if>
					<s:else>
						<a href=<s:url value="/template/importUDE/ImportUDETemplate.csv" />>Import Template</a>.
					</s:else>
					
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

				<s:if test="hasActionErrors()">
					<div class="form-error">
						<s:actionerror />
					</div>
				</s:if>

				<s:if test="newDataElementList!=null">
					<h3>Available Data Elements</h3>
					<p>The following data elements were found in the CSV file. Please review these elements and select which ones
						you wish to import. Data Elements in the system or any elements with errors will not be available for import</p>
						<p>Text highlighted in red will overwrite pre-existing Data Elements </p>
						
					<s:if test="changeHistoryRequired">
						<div class="form-output" style="border:1px solid #d6d6d6; margin:5px 0px; padding:5px;">
								<p><b>Change History</b><span class="required">* </span><br>
									Please enter your reasons for making changes to the Data Elements if you select to overwrite pre-existing Data Elements.
								</p>	
						
										<s:textarea cols="150%" rows="10%" id="auditNote" name="auditNote" escapeHtml="true" escapeJavaScript="true"/>
									
										<span id="validateAuditNote" class="error-message" style="display: none; float:right">
											<img class="icon-warning" alt="" src="/portal/images/brics/common/icon-warning.gif">
											<span class="error-text"><strong>Change History is required.</strong></span>
										</span>
							</div>
					</s:if>
					
					<div id="results">
							<jsp:include page="importList.jsp" />
					</div>
				</s:if>
			</div>

		
	</div>

	<script type="text/javascript">
	setNavigation({"bodyClass":"primary", "navigationLinkID":"dataDictionaryModuleLink", "subnavigationLinkID":"dataDictionaryToolLink", "tertiaryLinkID":"userImportDataElementLink"});
	
	function submitTheForm(theForm) {
		
		
		var theForm = document.forms['uploadForm'];
		theForm.action='importDataElementAction!adminUpload.action';
	    theForm.submit();
	}

</script>