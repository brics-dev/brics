<%@include file="/common/taglibs.jsp"%>
<title>Import Form Structure</title>

<s:set var="newDataElementList" value="newDataElementList" />
<s:set var="disabledList" value="disabledList" />
<s:set var="alreadyAttachedDataElementList"
	value="alreadyAttachedDataElementList" />
<s:set var="existingDataElementList" value="existingDataElementList" />

<body class="lightbox">


	<!-- begin .border-wrapper -->
	<div class="border-wrapper">
		<jsp:include page="../navigation/dataDictionaryNavigation.jsp" /><h1 class="float-left">(Admin) Data Dictionary</h1>
		<div class="clear-float"></div>
		<!--begin #center-content -->
		<div id="main-content" style="margin-top:15px;">

			<div>
				<h2>Form Structure Import</h2>
				<s:form name="uploadForm2" class="validate"
					action="dataStructureAction!importFormStructure.action"
					method="post" enctype="multipart/form-data">
					<s:token />
					<p>An administrator can import a form structure into the data dictionary. 
					Please select a file to import. Note: The selected file must be in XML format.</p>
					<div class="form-field">
						<label for="new_file_upload">File:</label>
						<s:file name="upload" cssClass="textfield float-left" />
					</div>
					<div class="form-field">
						<div class="button">
							<input type="button" value="Upload"
								onClick="javascript:  submitTheFormStructure('uploadForm2');" />
						</div>
					</div>
				</s:form>
				<div class="content" style="width:700px;">
				
				<s:if test="fieldErrors.size > 0">
				<div id="msg" class="alt-error">
					<h3>There are errors associated with the XML file.</h3><BR>
					<div style="width:250px;">
							<s:fielderror fieldName="upload"/><BR>
					</div>
				</div>
				</s:if>
					<!-- you need this wrapper div with "content" as the class  -->
					<s:if test="importStructureSuccess.size()>0">
						<div id="msg" class="alt-success">
							<s:iterator value="importStructureSuccess">
								<s:iterator status="inner">
									<s:if test="#inner.index==0">
										<h3>
											<s:property />
										</h3>
									</s:if>
									<s:elseif test="#inner.index!=0">
										<p>
											<ul>
												<li><s:property /></li>
											</ul>
										</p>
									</s:elseif>
								</s:iterator>
							</s:iterator>
						</div>
					</s:if>
					<s:if test="importStructureErrors.size()>0">
						<div id="msg" class="alt-error">
							<s:iterator value="importStructureErrors">
								<s:iterator status="inner">
									<s:if test="#inner.index==0">
										<h3>
											<s:property />
										</h3>
									</s:if>
									<s:elseif test="!#inner.index==0">
										<p>
											<ul>
												<li><s:property /></li>
											</ul>
										</p>
									</s:elseif>
								</s:iterator>
							</s:iterator>
						</div>
					</s:if>
				</div>
				<!-- end of wrapper div -->
			</div>
		</div>
	</div>

	<script type="text/javascript">
		setNavigation({
			"bodyClass" : "primary",
			"navigationLinkID" : "dataDictionaryModuleLink",
			"subnavigationLinkID" : "defineDataToolsLink",
			"tertiaryLinkID" : "importFormStructureLink"
		});

		function submitTheFormStructure(theForm) {
			$.fancybox.showActivity();

			var theForm = document.forms['uploadForm2'];
			theForm.action = 'importDataStructureAction!importFormStructure.action';
			theForm.submit();

		}
	</script>