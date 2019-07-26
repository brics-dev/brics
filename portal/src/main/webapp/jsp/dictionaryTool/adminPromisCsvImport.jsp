<%@include file="/common/taglibs.jsp"%>
<title>Import eForm</title>

<body class="lightbox">
	<div class="border-wrapper">
		<jsp:include page="../navigation/dataDictionaryNavigation.jsp" />
		<h1 class="float-left">(Admin) Data Dictionary</h1>
		<div class="clear-float"></div>
		<div id="main-content" style="margin-top:15px;">
			<div>
				<h2>PROMIS CSV Import</h2>
				<s:form name="uploadForm2" class="validate" action="promisCsvToXMLAction!exportXML.action" method="post" enctype="multipart/form-data">
					<s:token />
					<p>An administrator can import a PROMIS CSV to generate import PROMIS Form Structure file in XML format. Please select a file to import. Note: The selected file must be in CSV format.</p>
					<div class="form-field">
						<label for="new_file_upload">File:</label>
						<s:file name="upload" cssClass="textfield float-left" />
					</div>
					<div class="form-field">
						<div id="buttonDiv" class="button">
							<input id="uploadButton" type="button" value="Upload" onClick="javascript:  submiteForm('promisCsvToXMLAction!exportXML.action');" />
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
						<s:if test="importStructureSuccess.size() > 0">
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
							<s:if test="importStructureErrors.size() > 0">
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
		 			</s:form>
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
			"tertiaryLinkID" : "promisCsvToXMLLink"
		});

		function submiteForm(action) {
			//$.fancybox.showActivity();
			$("#msg").empty().hide();
			var theForm = document.forms['uploadForm2'];
			theForm.action = action;
			theForm.submit();
		}
		
	</script>