<%@include file="/common/taglibs.jsp"%>
<title>Delete Data Elements</title>

<s:set var="newDataElementList" value="newDataElementList" />
<s:set var="disabledList" value="disabledList" />
<s:set var="alreadyAttachedDataElementList"
	value="alreadyAttachedDataElementList" />
<s:set var="existingDataElementList" value="existingDataElementList" />

<body class="lightbox">



	<!-- begin .border-wrapper -->
	<div class="border-wrapper">
		<jsp:include page="../navigation/dataDictionaryNavigation.jsp" />

		<h1 class="float-left">Data Dictionary</h1>
		<div class="clear-float"></div>
		<!--begin #center-content -->
		<div id="main-content" style="min-height: 300px; margin-top: 15px;">
			<h2>Delete Data Elements</h2>
			<p>This field takes a list of data element names to delete. Data
				elements must be in Draft status to delete</p>
			<p>Data Elements should be delimited by a semi-colon (;)</p>
			<s:if test="fieldErrors.size() > 0">
					<div id="msg" class="alt-error">
						<h3>There are errors associated with the list of Data Elements.</h3><BR>
						<div style="width:250px;">
								<s:fielderror fieldName="upload"/><BR>
						</div>
					</div>
				</s:if>
					<!-- you need this wrapper div with "content" as the class  -->
					<s:if test="errorsList.size() > 0">
						<div id="msg" class="alt-error">
							<s:iterator value="errorsList">
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
			
			<s:form name="uploadForm" class="validate" action="deleteDataElementAction!adminDelete.action" method="post" enctype="multipart/form-data">
				<div id="text-area">
					<textarea name="dataElements" id="dataElements" rows="5"
						cols="100" style="font-size: 10pt"></textarea>
				</div>
			</s:form>

			<div class="form-field">
				<div class="flex-justify-between">
					<div>
						<div class="button">
							<input type="button"
								onclick="submitDataElements(uploadForm);"
								value="Delete" />
						</div>

						<div class="button margin-left">
							<input id="cancel" type="button"
								onclick="parent.location='deleteDataElementAction!cancel.action'"
								value="Cancel" />
						</div>
					</div>
				</div>
			</div>
		</div>
	</div>
	<script type="text/javascript">
		setNavigation({
			"bodyClass" : "primary",
			"navigationLinkID" : "dataDictionaryModuleLink",
			"subnavigationLinkID" : "dataDictionaryToolLink",
			"tertiaryLinkID" : "adminDeleteDataElementLink"
		});

		function submitDataElements(theForm) {			
			var theForm = document.forms['uploadForm'];
			theForm.action='deleteDataElementAction!adminDelete.action';
		    theForm.submit();
		}

	</script>