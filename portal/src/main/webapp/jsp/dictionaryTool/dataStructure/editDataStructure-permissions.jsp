<%@include file="/common/taglibs.jsp"%>
<title>Grant Permissions</title>

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
			<ndar:dataStructureChevron action="dataStructureAction" chevron="Grant Permissions" />

			<h2>Grant Permissions: <s:property value="dataStructureForm.title" /></h2>

			<s:if test="%{canAdmin}">
				<p>Individual access to the form structure is granted below. Select the intended individual from the drop down to
				grant access. The permissions include read, write, or admin. The default permission is read. Also, individuals can
				be removed from the permission group here.</p>

				<p><strong>Read - </strong>Allows user to view draft form structure.<br /> 
				<strong>Write - </strong>Allows user to view draft form structure, edit form structure details and attach data elements.<br /> 
				<strong>Admin / Owner - </strong>Allows user to view draft form structure, edit form structure details, attach data elements and grant permissions. There can only be one Owner.</p>

			
				<div id="permissionDivId"></div>
			</s:if>
			<s:else>
				<p>In order to change permissions for individual access, please save the current form structure first and come back to edit it.</p>
			</s:else>

			<%-- <ndar:dataStructureSave action="dataStructureAction" method="moveToPermissions" lastButtonSet="true" />--%>
			
			<ndar:dataStructureSave action="dataStructureAction" method="moveToReview" />

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
	
	$('document').ready(function() 
			{ 
				getPermissions();
			}
	);
	
	function getPermissions()
	{
		<s:if test="%{!sessionDataStructure.newStructure && !sessionDataStructure.draftCopy}">
			var params = {entityIdParam: <s:property value='currentDataStructure.id' />, entityTypeParam:"DATA_STRUCTURE"};
		</s:if>
		<s:else>
			var params = {entityIdParam:null, entityTypeParam:"DATA_STRUCTURE"};
		</s:else>
		
		$.ajax({
			cache : false,
			url : "dataStructurePermissionAction!load.ajax",
			data : params,
			success : function (data) {
				$("#permissionDivId").html(data);
			}
		});
	}

</script>