<%@include file="/common/taglibs.jsp"%>
<%@page contentType="text/html; charset=UTF-8" %>
<title>Attach Elements</title>



<div class="border-wrapper">
	<jsp:include page="../../navigation/dataDictionaryNavigation.jsp" />
		<h1 class="float-left"><s:if test="inAdmin">(Admin)&nbsp;</s:if>Data Dictionary</h1>
	<div style="clear:both;"></div>
	<div id="main-content" style="margin-top:15px;">
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

		<form name="dataStructureForm">
			<ndar:dataStructureChevron action="dataStructureAction" chevron="Attach Elements" />

			<!-- Display Map List Elements if you are in Edit Mode -->
			<h2>
				Attach Data Elements:
				<s:property value="dataStructureForm.title" />
			</h2>
			<p>
				Attach data elements to your form structure below. Use the <strong>Add Data Element</strong> button to open the data
				element interface. In the data element interface you can search for existing data elements or create a new data
				element for this form structure. To group data elements or create a group that repeats data elements, select the <strong>Add Group</strong> button below.
			</p>
			<p>
				Data Elements and Groups can be rearranged by dragging and dropping the group or element into the desired position.
			</p>

			<div id="elementsDiv">
				<jsp:include page="attachedDataElements.jsp" />
			</div>

			<ndar:dataStructureSave action="dataStructureAction" method="moveToPermissions" />

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