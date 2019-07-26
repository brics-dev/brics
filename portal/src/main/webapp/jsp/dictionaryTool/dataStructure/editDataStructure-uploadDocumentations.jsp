<%@include file="/common/taglibs.jsp"%>
<title>Upload Documentations</title>

<div class="border-wrapper">
<s:if test="!sessionDataStructure.newStructure && !sessionDataStructure.draftCopy">
			<div id="breadcrumb">
				<s:if test="inAdmin"><s:a action="listDataStructureAction" method="list" namespace="/dictionaryAdmin">Manage Form Structures</s:a></s:if>
				<s:else><s:a action="listDataStructureAction" method="list" namespace="/dictionary">Search Form Structures</s:a></s:else>
				&gt;
				<s:url action="dataStructureAction" method="view" var="viewTag">
					<s:param name="dataStructureId">
						<s:property value="currentDataStructure.id" />
					</s:param>
				</s:url>
				<a href="<s:property value="#viewTag" />"><s:property value="currentDataStructure.title" /></a> &gt; Edit Form
				Structure
			</div>
		</s:if>
		<div style="clear:both"></div>
	<jsp:include page="../../navigation/dataDictionaryNavigation.jsp" />
	<h1 class="float-left"><s:if test="inAdmin">(Admin)&nbsp;</s:if>Data Dictionary</h1>
	<div style="clear:both"></div>
	<div id="main-content" style="margin-top:15px;">
		

		<form name="dataStructureForm">
			<ndar:dataStructureChevron action="dataStructureAction" chevron="Upload Documentations" />

			<h2>Upload Documentations: <s:property value="currentDataStructure.title" /></h2>

			<ndar:documentationUpload />
			
			<ndar:dataStructureSave action="dataStructureAction" method="moveToElements" />

		</form>
	</div>
</div>

<script type="text/javascript">
	
	<s:if test="inAdmin">
		setNavigation({"bodyClass":"primary", "navigationLinkID":"dataDictionaryModuleLink", "subnavigationLinkID":"defineDataToolsLink", "tertiaryLinkID":"manageDataStructuresLink"});
	</s:if>
	<s:else>
		<s:if test="%{!sessionDataStructure.newStructure}">
			setNavigation({"bodyClass":"primary", "navigationLinkID":"dataDictionaryModuleLink", "subnavigationLinkID":"dataDictionaryToolLink", "tertiaryLinkID":"listDataStructureLink"});
		</s:if>
		<s:else>
			setNavigation({"bodyClass":"primary", "navigationLinkID":"dataDictionaryModuleLink", "subnavigationLinkID":"dataDictionaryToolLink", "tertiaryLinkID":"dataStructureLink"});
		</s:else>
	</s:else>
	
</script>