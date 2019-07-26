<%@include file="/common/taglibs.jsp"%>
<title>Import eForm</title>

<body class="lightbox">
	<div class="border-wrapper">
		<jsp:include page="../../navigation/dataDictionaryNavigation.jsp" /><h1 class="float-left">(Admin) Data Dictionary</h1>
		<div class="clear-float"></div>
		<!--begin #center-content -->
		<div id="main-content" style="margin-top:15px;">
			<div>
				<h2>eForm Import</h2>
				<s:form name="uploadForm2" class="validate" action="eformImportAction!importEform.action" method="post" enctype="multipart/form-data">
					<s:token />
					<p>An administrator can import an eForm into the data dictionary. Please select a file to import. Note: The selected file must be in XML format.</p>
					<div class="form-field">
						<label for="new_file_upload">File:</label>
						<s:file name="upload" cssClass="textfield float-left" />
					</div>
					<div class="form-field">
						<div id="buttonDiv" class="button">
							<input id="uploadButton" type="button" value="Upload" onClick="javascript:  submiteForm('eformImportAction!importEform.action');" />
						</div>
					</div>
				<div class="content" style="width:700px;">
				
				<s:if test="fieldErrors.size() > 0">
					<div id="msg" class="alt-error">
						<h3>There are errors associated with the XML file.</h3><BR>
						<div style="width:250px;">
								<s:fielderror fieldName="upload"/><BR>
						</div>
					</div>
				</s:if>
					<!-- you need this wrapper div with "content" as the class  -->
				<s:if test="importEformSuccess.size() > 0">
						<div id="msg" class="alt-success">
							<s:iterator value="importEformSuccess">
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
					<s:if test="importEformErrors.size() > 0">
						<div id="msg" class="alt-error">
							<s:iterator value="importEformErrors">
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
			<s:if test="reenterShortName">
				<h3>Rename eForm Shortname</h3>
				<div class="form-output">
						<div class="form-field form-field-vert">
							<div class="label">Existing eForm Short Name:</div>
							<div class="readonly-text">
								<s:property value="sessionEform.eform.shortName" />
							</div>
							<br>
							<label for=EformShortName>New eForm Short Name:</label>
							<s:textfield id="EformShortName" name="EformShortName" cssClass="textfield required" escapeHtml="true" escapeJavaScript="true" />
							<br><br>
							<div class="form-field inline-right-button">
								<div class="button" style="margin-left: 10px;">
									<input type="button" value="Save" onclick="javascript:submiteForm('eformImportAction!updateEformShortName.action')" />
								</div>
								<div class="button" style="margin-left: 10px;">
									<input type="button" onclick="javascript:window.location.href='eFormSearchAction!list.action'" value="Cancel" />
								</div>
							</div>
						</div>
				</div>
			</s:if>
		 </s:form>
		 				</div>
				<!-- end of wrapper div -->
			</div>
		</div>
	</div>
	
	<s:if test="reenterShortName">
		<script type="text/javascript">
			$("document").ready(function() { 
				$("#uploadButton").prop("disabled", true);
				$("#buttonDiv").addClass("button disabled");
			});
		</script>
	</s:if>

	<script type="text/javascript">
		setNavigation({
			"bodyClass" : "primary",
			"navigationLinkID" : "dataDictionaryModuleLink",
			"subnavigationLinkID" : "defineDataToolsLink",
			"tertiaryLinkID" : "importEformLink"
		});

		function submiteForm(action) {
			$.fancybox.showActivity();

			var theForm = document.forms['uploadForm2'];
			theForm.action = action;
			theForm.submit();

		}
		
	</script>