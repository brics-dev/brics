<%@include file="/common/taglibs.jsp"%>
<s:if test="inAdmin"><title>Manage Form Structures</title></s:if>
<s:else><title>Search Form Structures</title></s:else>


<div class="clear-float">
	<h1 class="float-left">Data Dictionary</h1>
</div>

<div class="border-wrapper">
	<jsp:include page="../navigation/dataDictionaryNavigation.jsp" />
	<div id="main-content" style="margin-top:15px;">

		<s:if test="inAdmin"><h2>Manage Form Structures</h2></s:if>
		<s:else><h2>Search Form Structures</h2></s:else>
		
		<p>Use the table below to view form structures. Selecting a specific Status or Ownership type will filter the form
			structures accordingly.</p>
		<form>

			<s:set var="dataStructureList" value="dataStructureList" />
			<s:set var="statusArray" value="statusArray" />

			<div id="dataStructureOwnerOptions" class="filter">
				<strong>Ownership: </strong> <a class="inactiveLink dataStructureSelectedOwner" id=1>All</a> | <a
					href="javascript:dataStructureSetOwner(0);" id="0">Mine</a>
			</div>
			<br />
			<div id="dataStructureFilterOptions" class="filter">
				<strong>Status: </strong> <a class="inactiveLink dataStructureSelectedFilter" id="-1">All</a> | <a
					href="javascript:dataStructureSetFilter(0);" id="0">Draft</a> | <a href="javascript:dataStructureSetFilter(1);"
					id="1">Awaiting Publication</a> | <a href="javascript:dataStructureSetFilter(5);"
	id="5">Shared Draft</a> | <a href="javascript:dataStructureSetFilter(2);" id="2">Published</a> | <a
					href="javascript:dataStructureSetFilter(3);" id="3">Archived</a>
			</div>

			<div id="dataStructureResultsId"></div>
		</form>
	</div>
</div>

<script type="text/javascript" src="/portal/js/search/dataStructureSearch.js"></script>
<script type="text/javascript">

// This loads the ALL filter to begin and sets results to page 1
	$('document').ready(function() {
		var filterParam = getURLParameter("filter");
		if(filterParam != "undefined")
		{
			dataStructureSetFilter(filterParam);
		}
		else
		{
			dataStructureSearch();
		}
	});
	
	<s:if test="!inAdmin">					
 		setNavigation({"bodyClass":"primary", "navigationLinkID":"dataDictionaryModuleLink", "subnavigationLinkID":"dataDictionaryToolLink", "tertiaryLinkID":"listDataStructureLink"});
	</s:if>
	<s:else>
		setNavigation({"bodyClass":"primary", "navigationLinkID":"dataDictionaryModuleLink", "subnavigationLinkID":"defineDataToolsLink", "tertiaryLinkID":"manageDataStructuresLink"});
	</s:else>
</script>