<%@include file="/common/taglibs.jsp"%>
<input type="hidden" id="inAdmin" name="inAdmin" value="${inAdmin}" />

<!-- begin .border-wrapper -->
<div class="border-wrapper wide">
	<jsp:include page="../../navigation/dataDictionaryNavigation.jsp" />
	<!--begin #center-content -->

		<s:if test="inAdmin" ><h1 class="float-left">Search eForms (Admin)</h1></s:if>
		<s:else><h1 class="float-left">Search eForms</h1></s:else>
		
		<div style="clear:both"></div>
		
		<div id="main-content" style="min-height:310px">
		<div style="width:18%; float:left; padding:10px 15px 0 0;">
		<h3 style="margin-left:9px;">Narrow your search</h3>
		
			<form id="searchForm" class="searchEformForm" style="margin: 15px 8px;">

			</form>
		</div>
		
		<!--begin #center-content -->
			<div style="width:79%; float:left;">
				<div id="eFormResultsContainer" class="idtTableContainer brics">
					<table id="eFormResultsTable" class="table table-striped table-bordered" width="100%">
					</table>
				</div>
			</div>	
			
		</div>
	
</div>
<!-- end of .border-wrapper -->

<script type="text/javascript" src="/portal/js/dataTables/2.0/idtCustomSearchPlugin.js"></script>
<script type="text/javascript" src="/portal/js/search/eformSearch.js"></script>
<script type="text/javascript">
<s:if test="!inAdmin">
	setNavigation({"bodyClass":"primary", "navigationLinkID":"dataDictionaryModuleLink", "subnavigationLinkID":"dataDictionaryToolLink", "tertiaryLinkID":"searchEformLink"});
</s:if>
<s:else>
	setNavigation({"bodyClass":"primary", "navigationLinkID":"dataDictionaryModuleLink", "subnavigationLinkID":"defineDataToolsLink", "tertiaryLinkID":"manageEformLink"});
</s:else>
</script>
