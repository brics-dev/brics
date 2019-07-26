<%@include file="/common/taglibs.jsp"%>
<s:set var="currentPage" value="currentPage" />
<s:set var="currentDataElement" value="currentDataElement" />
<s:set var="formType" value="formType" />
<s:bean name="gov.nih.tbi.dictionary.model.DataElementForm"
	var="dataElementForm" />

<s:if test="%{formType == 'create'}">
	<title>Add Documentations</title>
</s:if>
<s:elseif test="%{formType == 'edit'}">
	<title>Add <s:property value="currentDataElement.title" /> Documentations</title>
</s:elseif>

<!-- begin .border-wrapper -->
<div class="border-wrapper">
	<jsp:include page="../navigation/dataDictionaryNavigation.jsp" />
	<div class="">
		<s:if test="%{formType == 'create'}">
			<h1 class="float-left">Add Documentations</h1>
		</s:if>
		<s:elseif test="%{formType == 'edit'}">
			<h1 class="float-left">
				<s:if test="inAdmin">(Admin)&nbsp;</s:if>Edit
				<s:property value="currentDataElement.title" />
				Documentations
			</h1>
		</s:elseif>
	</div>
	<div style="clear: both;"></div>
	<!--begin #center-content -->
	<div id="main-content"  style="width:100%; margin-top:15px;">
		<s:if test="%{formType == 'edit'}">
			<div id="breadcrumb">
				<s:if test="inAdmin">
					<s:a action="searchDataElementAction" method="list"
						namespace="/dictionaryAdmin">Manage Data Elements</s:a>
				</s:if>
				<s:else>
					<s:a action="searchDataElementAction" method="list"
						namespace="/dictionary">Search Data Elements</s:a>
				</s:else>
				&gt;
				<s:url action="dataElementAction" method="view" var="viewTag">
					<s:param name="dataElementId">
						<s:property value="currentDataElement.id" />
					</s:param>
				</s:url>
				<a href="<s:property value="#viewTag" />"><s:property
						value="currentDataElement.title" /></a> &gt; Edit Data Element
			</div>
		</s:if>

		<s:form id="theForm" cssClass="validate"
			action="dataElementAction" method="post"
			validate="true">
				
				<s:if test="dataType == 'dataElement'">
					<ndar:dataElementChevron
						action="dataElementAction" chevron="Documentations" />
				</s:if>
				<s:if test="dataType == 'mapElement'">
					<ndar:dataElementChevron action="dataElementAction"
						chevron="Documentations" />
				</s:if>
				
				<ndar:documentationUpload />
			
				<!--  new page or move to page -->
				<div class="form-field clear-left">
					<div class="button">
						<input type="button" value="Continue"
								onClick="javascript:submitForm('dataElementAction!editValueRange.action')" />
					</div>
					<s:if test="%{formType=='edit'}">				
						<a class="form-link" href="javascript:submitForm('dataElementAction!review.action')">Review
									</a>
					</s:if>
					<a class="form-link" href="javascript:cancel()">Cancel</a>
				</div>
		</s:form>
	</div>
	<!-- end of #main-content -->
</div>
</div>
<!-- end of .border-wrapper -->
<script type="text/javascript">
	var globalToken;
	<s:if test="!inAdmin">
		<s:if test="%{formType == 'create'}">
			setNavigation({"bodyClass":"primary", "navigationLinkID":"dataDictionaryModuleLink", "subnavigationLinkID":"dataDictionaryToolLink", "tertiaryLinkID":"dataElementLink"});
		</s:if>
		<s:else>
			setNavigation({"bodyClass":"primary", "navigationLinkID":"dataDictionaryModuleLink", "subnavigationLinkID":"dataDictionaryToolLink", "tertiaryLinkID":"searchDataElementLink"});
		</s:else>
	</s:if>
	<s:else>
		setNavigation({"bodyClass":"primary", "navigationLinkID":"dataDictionaryModuleLink", "subnavigationLinkID":"defineDataToolsLink", "tertiaryLinkID":"manageDataElementsLink"});
	</s:else>
	
	//calls clear session to clear the data in session upon cancel
	function cancel() {
		var dataType = '<s:property value="dataType"/>';
		if(dataType=="mapElement") { 
			window.location = "dataStructureElementAction!moveToElements.action";
		} else if(dataType=="dataElement") {
			<s:if test="%{formType == 'create'}">
				window.location = "searchDataElementAction!list.action";
			</s:if>
			<s:else>
				window.location = "dataElementAction!view.action?dataElementId=<s:property value='currentDataElement.id' />";
			</s:else>
		}		
	}
	
</script>
