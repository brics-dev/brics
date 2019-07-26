<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn"%>
<!DOCTYPE html>
<%-- <%@include file="/common/taglibs.jsp"%>  --%> 
<html>
	<head>
		<meta charset="utf-8" />
		<title>Query Tool</title>

		<link type="text/css" href="css/jquery-ui.css" rel="stylesheet" media="all" />
		<!--  <link type="text/css" href="css/jquery.dataTables.css" rel="stylesheet" media="all" /> -->
		<link type="text/css" href="css/jquery.ibisMessaging-0.1.css" rel="stylesheet" />
		<link type="text/css" href="instances/pdbp/jquery-ui.theme.min.css" rel="stylesheet" />
		
		<link type="text/css" href="css/font-awesome-4.6.3/css/font-awesome.min.css" rel="stylesheet" />
		<link type="text/css" href="css/qt-glyphicons.css" rel="stylesheet" />
		<link type="text/css" href="css/brics-qt-glyphicons.css" rel="stylesheet" />
		<!-- <link type="text/css" href="css/qt-structure.css" rel="stylesheet" />
		<link type="text/css" href="css/qt-header.css" rel="stylesheet" />
		<link type="text/css" href="css/qt-general.css" rel="stylesheet" /> -->
		<link type="text/css" href="css/jquery/datatables.css" rel="stylesheet" />
		<!--  change instance folder name for different styles -->
		
		<c:if test="${fn:contains(applicationConstants.styleKey, 'localhost')}"> 
			<link type="text/css" href="instances/default/qt-footer.css" rel="stylesheet" /> 
			<link type="text/css" href="css/scss/instances/default/style.css" rel="stylesheet" />
			<!-- Favicon -->
			<link href="css/scss/instances/default/images/favicon.ico" rel="icon" />
		</c:if>	
		<c:if test="${fn:contains(applicationConstants.styleKey, 'nti')}"> 
			<link type="text/css" href="instances/nti/qt-footer.css" rel="stylesheet" /> 
			<link type="text/css" href="css/scss/instances/nti/style.css" rel="stylesheet" />
			<!-- Favicon -->
			<link href="css/scss/instances/nti/images/favicon.ico" rel="icon" />
		</c:if>
		<c:if test="${fn:contains(applicationConstants.styleKey, 'gsdr')}"> 
			<link type="text/css" href="instances/gsdr/qt-footer.css" rel="stylesheet" /> 
			<link type="text/css" href="css/scss/instances/gsdr/style.css" rel="stylesheet" />
			<!-- Favicon -->
			<link href="css/scss/instances/gsdr/images/favicon.ico" rel="icon" />
		</c:if>
		<c:if test="${fn:contains(applicationConstants.styleKey, 'pdbp')}">  
		<link type="text/css" href="instances/pdbp/qt-footer.css" rel="stylesheet" />
			<link type="text/css" href="css/scss/instances/pdbp/style.css" rel="stylesheet" />
			<!-- Favicon -->
			<link href="css/scss/instances/pdbp/images/favicon.ico" rel="icon" />
		</c:if>
		<c:if test="${fn:contains(applicationConstants.styleKey, 'cistar')}"> 
		<link type="text/css" href="instances/cistar/qt-footer.css" rel="stylesheet" />
			<link type="text/css" href="css/scss/instances/cistar/style.css" rel="stylesheet" />
			<!-- Favicon -->
			<link href="css/scss/instances/cistar/images/favicon.ico" rel="icon" />
		</c:if>
		<c:if test="${fn:contains(applicationConstants.styleKey, 'cnrm')}">
		<link type="text/css" href="instances/cnrm/qt-footer.css" rel="stylesheet" />
			<link type="text/css" href="css/scss/instances/cnrm/style.css" rel="stylesheet" />
			<!-- Favicon -->
			<link href="css/scss/instances/cnrm/images/favicon.ico" rel="icon" />
		</c:if>  
		<c:if test="${fn:contains(applicationConstants.styleKey, 'eyegene') || fn:contains(applicationConstants.styleKey, 'nei')}">  
		<link type="text/css" href="instances/eyegene/qt-footer.css" rel="stylesheet" />
			<link type="text/css" href="css/scss/instances/eyegene/style.css" rel="stylesheet" />
			<!-- Favicon -->
			<link href="css/scss/instances/eyegene/images/favicon.ico" rel="icon" />
		</c:if>
		<c:if test="${fn:contains(applicationConstants.styleKey, 'cdrns')}">  
		<link type="text/css" href="instances/cdrns/qt-footer.css"rel="stylesheet" />
			<link type="text/css" href="css/scss/instances/cdrns/style.css" rel="stylesheet" />
			<!-- Favicon -->
			<link href="css/scss/instances/cdrns/images/favicon.ico" rel="icon" />
		</c:if>
		<c:if test="${fn:contains(applicationConstants.styleKey, 'fitbir')}"> 
		<link type="text/css" href="instances/fitbir/qt-footer.css" rel="stylesheet" />
			<link type="text/css" href="css/scss/instances/fitbir/style.css" rel="stylesheet" />
			<!-- Favicon -->
			<link href="css/scss/instances/fitbir/images/favicon.ico" rel="icon" />
		</c:if> 
		<c:if test="${fn:contains(applicationConstants.styleKey, 'brics')}">
		    <link type="text/css" href="instances/cistar/qt-footer.css" rel="stylesheet" />
			<link type="text/css" href="css/scss/instances/cistar/style.css" rel="stylesheet" />
			<!-- Favicon -->
			<link href="css/scss/instances/cistar/images/favicon.ico" rel="icon" />

		</c:if>
		<!-- <link rel="stylesheet" href="http://codemirror.net/lib/codemirror.css" type="text/css" /> -->
		<link rel="stylesheet" href="css/codemirror.css" type="text/css" />
		
		<c:if test="${fn:contains(applicationConstants.styleKey, 'nti')}"> 
			<!-- Google Tag Manager -->
			<script>(function(w,d,s,l,i){w[l]=w[l]||[];w[l].push({'gtm.start':
			new Date().getTime(),event:'gtm.js'});var f=d.getElementsByTagName(s)[0],
			j=d.createElement(s),dl=l!='dataLayer'?'&l='+l:'';j.async=true;j.src=
			'https://www.googletagmanager.com/gtm.js?id='+i+dl;f.parentNode.insertBefore(j,f);
			})(window,document,'script','dataLayer','GTM-K2MLJ55');</script>
			<!-- End Google Tag Manager -->
		</c:if>

		<!-- namespace setup -->
		<script type="text/javascript" src="js/QueryTool.js"></script>
		<script type="text/javascript" src="js/QueryToolDataTable.js"></script>

		<!-- All library imports -->

		<script src="https://cdnjs.cloudflare.com/ajax/libs/jquery/1.7.2/jquery.min.js" crossorigin="anonymous"></script>
		<script type="text/javascript" src="js/lib/jquery-ui.min.js"></script>
		<script type="text/javascript" src="js/lib/jquery.scrollTo.js"></script>
		
		<script type="text/javascript" src="js/lib/jquery.ibisMessaging-0.1.full.js"></script>
		<script type="text/javascript" src="js/lib/core_libs.min.js"></script>
		
		<!-- development only!!! -->
		<!-- <script type="text/javascript" src="js/lib/development/backbone.js"></script> -->

		<script type="text/javascript" src="js/lib/array_find_polyfill.js"></script>
        <script src="https://cdn.jsdelivr.net/npm/promise-polyfill@8/dist/polyfill.min.js"></script>
		<script type="text/javascript" src="js/lib/d3.v4.js"></script>
		<script type="text/javascript" src="js/lib/core_classes.min.js"></script>
		<script type="text/javascript" src="js/lib/jquery.dataTables.js"></script> 
		
		<!--  Linkify  -->
		
		<script type="text/javascript" charset="utf-8" language="javascript" src="js/lib/linkify.min.js"></script>
		<script type="text/javascript" charset="utf-8" language="javascript" src="js/lib/linkify-string.min.js"></script>
		<script type="text/javascript" charset="utf-8" language="javascript" src="js/lib/linkify-jquery.min.js"></script>
		
		<!-- Codemirror libraries for Rbox script input -->
		<script type="text/javascript" src="js/lib/codemirror/codemirror.min.js"></script>
		<!-- <script src="http://codemirror.net/lib/codemirror.js" type="text/javascript"></script>
		<script src="http://codemirror.net/lib/util/runmode.js" type="text/javascript"></script>
		<script src="http://codemirror.net/mode/javascript/javascript.js" type="text/javascript"></script> -->
		

		<jsp:include page='/Config.jsp' />
		
		<script type="text/javascript" src="js/lib/savedQueryViewEngine.js"></script>
		
		<!-- All global website imports here -->
		
		<!-- All application code below here -->
			<!-- Util -->
				<script type="text/javascript" src="js/util/Dialog.js"></script>
				<script type="text/javascript" src="js/util/tabOneFilters/Filter.js"></script>
				<script type="text/javascript" src="js/util/tabOneFilters/Filters.js"></script>
				<script type="text/javascript" src="js/util/tabOneFilters/DeTabFilters.js"></script>
				<script type="text/javascript" src="js/util/tabOneFilters/SqTabFilters.js"></script>
				
				<script type="text/javascript" src="js/util/tabOneFilters/TextSelectionFilter.js"></script>
				<script type="text/javascript" src="js/util/tabOneFilters/UnavailableFilter.js"></script>
				<script type="text/javascript" src="js/util/tabOneFilters/DeHideShowFilter.js"></script>
				<script type="text/javascript" src="js/util/tabOneFilters/DeCheckboxSelectionFilter.js"></script>
				<script type="text/javascript" src="js/util/tabOneFilters/CheckboxSelectionFilter.js"></script>
				<script type="text/javascript" src="js/util/tabOneFilters/SqIncludedTilesFilter.js"></script>
				<script type="text/javascript" src="js/util/sessionHandler.js"></script>
				<script type="text/javascript" src="js/util/Util.js"></script>
				<script type="text/javascript" src="js/util/SavedQueryLoadUtil.js"></script>
				<script type="text/javascript" src="js/util/r.js"></script>
				
			<!-- Models -->
				<script type="text/javascript" src="js/models/QueryDe.js"></script>
				<script type="text/javascript" src="js/models/QueryRg.js"></script>
				<script type="text/javascript" src="js/models/QueryForm.js"></script>
				<script type="text/javascript" src="js/models/Query.js"></script>
				<script type="text/javascript" src="js/models/DataElement.js"></script>
				<script type="text/javascript" src="js/models/Form.js"></script>
				<script type="text/javascript" src="js/models/DataCartForm.js"></script>
				<script type="text/javascript" src="js/models/DataCart.js"></script>
				<script type="text/javascript" src="js/models/Study.js"></script>
				<script type="text/javascript" src="js/models/DefinedQuery.js"></script>
				<script type="text/javascript" src="js/models/SelectionList.js"></script>
				
				<script type="text/javascript" src="js/models/Processing.js"></script>
				<script type="text/javascript" src="js/models/Page.js"></script>
				<script type="text/javascript" src="js/models/SelectDe.js"></script>				
				<script type="text/javascript" src="js/models/QueryFilter.js"></script>
				<script type="text/javascript" src="js/models/SavedQuery.js"></script>
				<script type="text/javascript" src="js/models/User.js"></script>
				<script type="text/javascript" src="js/models/UserPermission.js"></script>
				
				<script type="text/javascript" src="js/dataTable/models/Cell.js"></script>
				<script type="text/javascript" src="js/dataTable/models/Col.js"></script>
				<script type="text/javascript" src="js/dataTable/models/DataTable.js"></script>
				<script type="text/javascript" src="js/dataTable/models/Row.js"></script>
				<script type="text/javascript" src="js/dataTable/models/Pager.js"></script>
				<script type="text/javascript" src="js/dataTable/models/LengthMenu.js"></script>
				<script type="text/javascript" src="js/dataTable/models/LengthMenuOption.js"></script>
				
				<script type="text/javascript" src="js/models/Session.js"></script>
		
			<!-- Collections -->
				<script type="text/javascript" src="js/collections/QueryForms.js"></script>
				<script type="text/javascript" src="js/collections/QueryRgs.js"></script>
				<script type="text/javascript" src="js/collections/QueryDes.js"></script>
				<script type="text/javascript" src="js/collections/SelectionDataElements.js"></script>
				<script type="text/javascript" src="js/collections/SelectionForms.js"></script>
				<script type="text/javascript" src="js/collections/SelectionStudies.js"></script>
				<script type="text/javascript" src="js/collections/SelectionDefinedQueries.js"></script>
				<script type="text/javascript" src="js/collections/DataCartForms.js"></script>
				
				<script type="text/javascript" src="js/collections/QueryFilters.js"></script>
				<script type="text/javascript" src="js/collections/Permissions.js"></script>
				<script type="text/javascript" src="js/collections/Users.js"></script>
				
				<script type="text/javascript" src="js/dataTable/collections/ColCells.js"></script>
				<script type="text/javascript" src="js/dataTable/collections/Cols.js"></script>
				<script type="text/javascript" src="js/dataTable/collections/DataTables.js"></script>
				<script type="text/javascript" src="js/dataTable/collections/RowCells.js"></script>
				<script type="text/javascript" src="js/dataTable/collections/Rows.js"></script>
				<script type="text/javascript" src="js/dataTable/collections/Hamburgers.js"></script>
				<script type="text/javascript" src="js/dataTable/collections/LengthMenuOptions.js"></script>
				
		
			<!-- Views -->
			
			
				<script type="text/javascript" src="js/views/DataCartRemoveDialogView.js"></script>
				<script type="text/javascript" src="js/views/DownloadToQueueDialogView.js"></script>
				<script type="text/javascript" src="js/views/SelectDeView.js"></script>
				<script type="text/javascript" src="js/views/FilterDataSubTileView.js"></script>
				<script type="text/javascript" src="js/views/FilterDataTileView.js"></script>
				<script type="text/javascript" src="js/views/SelectionListItemView.js"></script>
				<script type="text/javascript" src="js/views/DeSelectionListItemView.js"></script>
				<script type="text/javascript" src="js/views/SqSelectionListItemView.js"></script>
				<script type="text/javascript" src="js/views/ResultPaneView.js"></script>
				<script type="text/javascript" src="js/views/SqResultPaneView.js"></script>
				<script type="text/javascript" src="js/views/SelectionListView.js"></script>
				<script type="text/javascript" src="js/views/DeSelectionListView.js"></script>
				<script type="text/javascript" src="js/views/SqSelectionListView.js"></script>
				<script type="text/javascript" src="js/views/ProcessingView.js"></script>
				
				<script type="text/javascript" src="js/views/FilterDataView.js"></script>
				<script type="text/javascript" src="js/views/RefineDataResultsView.js"></script>
				<script type="text/javascript" src="js/views/SelectCriteriaView.js"></script>
				<script type="text/javascript" src="js/views/RefineDataView.js"></script>
				<script type="text/javascript" src="js/views/DataCartButtonView.js"></script>
				<script type="text/javascript" src="js/views/PageView.js"></script>
				<script type="text/javascript" src="js/views/RefineDataFormView.js"></script>
				<script type="text/javascript" src="js/views/RefineDataCartView.js"></script>
				
				<script type="text/javascript" src="js/views/GenericQueryFilterView.js"></script>
				<script type="text/javascript" src="js/views/FilterFreeFormView.js"></script>
				<script type="text/javascript" src="js/views/FilterFreeFormLargeView.js"></script>
				<script type="text/javascript" src="js/views/FilterNumericRangeView.js"></script>
				<script type="text/javascript" src="js/views/FilterNumericUnboundedView.js"></script>
				<script type="text/javascript" src="js/views/FilterEnumeratedListView.js"></script>
				<script type="text/javascript" src="js/views/FilterOtherSpecifyView.js"></script>
				<script type="text/javascript" src="js/views/FilterRadioListView.js"></script>
				<script type="text/javascript" src="js/views/FilterDateRangeView.js"></script>
				<script type="text/javascript" src="js/views/RefineDataFiltersView.js"></script>
				
				<script type="text/javascript" src="js/views/RboxDialogView.js"></script>
				
				<script type="text/javascript" src="js/views/DetailsView.js"></script>
				<script type="text/javascript" src="js/views/SaveQueryDialogView.js"></script>
				<script type="text/javascript" src="js/views/SendToMetaStudyDialogView.js"></script>
				
				<script type="text/javascript" src="js/dataTable/views/HamburgerView.js"></script>
				<script type="text/javascript" src="js/dataTable/views/HamburgerActionContainerView.js"></script>
				<script type="text/javascript" src="js/dataTable/views/PagerView.js"></script>
				<script type="text/javascript" src="js/dataTable/views/LengthMenuView.js"></script>
				<script type="text/javascript" src="js/dataTable/views/DataTableView.js"></script>
				<script type="text/javascript" src="js/dataTable/views/ResultsView.js"></script>
				<script type="text/javascript" src="js/dataTable/views/FrozenResultsView.js"></script>
				<script type="text/javascript" src="js/dataTable/views/ColView.js"></script>
				<script type="text/javascript" src="js/dataTable/views/RowView.js"></script>
				<script type="text/javascript" src="js/dataTable/views/CellView.js"></script>
				<script type="text/javascript" src="js/dataTable/views/ImageView.js"></script>
				<script type="text/javascript" src="js/views/ReloadSessionDialogView.js"></script>
				<script type="text/javascript" src="js/dataTable/views/FileDownloadDialogView.js"></script>
				<script type="text/javascript" src="js/views/SendToMetaStudyValidationDialogView.js"></script>
				
		<!-- Bootstrap -->
		<script type="text/javascript" src="js/QtBootstrap.js"></script>
		
		
		
		<!-- Templates -->
		<jsp:include page='/templates/main/pageMain.jsp' />
		<jsp:include page='/templates/tab1/filterDataMain.jsp' />
		<jsp:include page='/templates/processing.jsp' />
		<jsp:include page='/templates/tab2/refineDataMain.jsp' />
		<jsp:include page='/templates/tab1/selectionPane/selectionList.jsp' />
		<jsp:include page='/templates/tab1/selectionPane/selectionListItem.jsp' />
		<jsp:include page='/templates/tab1/tiles/filterDataTile.jsp' />
		<jsp:include page='/templates/tab1/tiles/sub-tiles/filterDataSubTile.jsp' />
		<jsp:include page='/templates/main/dataCartButton.jsp' />
		<jsp:include page='/templates/main/saveQueryDialog.jsp' />
		<jsp:include page='/templates/tab2/criteriaPane/refineDataForm.jsp' />
		<jsp:include page='/templates/tab2/criteriaPane/refineDataCart.jsp' />
		<jsp:include page='/templates/tab1/selectionFilterPill.jsp' />
		<jsp:include page='/templates/tab1/filterDataResultPane.jsp' />
		<jsp:include page='/templates/tab2/criteriaPane/filterFreeForm.jsp' />
		<jsp:include page='/templates/tab2/criteriaPane/filterFreeFormLarge.jsp' />
		<jsp:include page='/templates/tab2/resultsPane/resultsPanelMain.jsp' />
		<jsp:include page='/templates/tab2/criteriaPane/filterNumericRange.jsp' />
		<jsp:include page='/templates/tab2/criteriaPane/filterNumericUnbounded.jsp' />
		<jsp:include page='/templates/tab2/criteriaPane/filterEnumeratedList.jsp' />
		<jsp:include page='/templates/tab2/criteriaPane/filterOtherSpecify.jsp' />
		<jsp:include page='/templates/tab2/criteriaPane/filterOtherSpecifyOption.jsp' />
		<jsp:include page='/templates/tab2/criteriaPane/filterEnumeratedItem.jsp' />
		<jsp:include page='/templates/tab2/criteriaPane/filterDateRange.jsp' />
		<jsp:include page='/templates/tab2/criteriaPane/filterRadioList.jsp' />
		<jsp:include page='/templates/tab2/criteriaPane/filterRadioItem.jsp' />
		<jsp:include page='/templates/tab1/dataElementFilterDataResultPane.jsp' />
		<jsp:include page='/templates/tab1/selectionPane/dataElementSelectionList.jsp' />
		<jsp:include page='/templates/tab1/selectDeDialogTemplate.jsp' />
		<jsp:include page='/templates/tab1/details.jsp' />
		<jsp:include page='/templates/tab2/criteriaPane/dataCartRemoveDialog.jsp' />
		<jsp:include page='/templates/tab2/resultsPane/selectCriteria.jsp' />
		<jsp:include page='/templates/tab1/selectionPane/savedQuerySelectionList.jsp' />
		<jsp:include page='/templates/tab1/selectionPane/savedQuerySelectionListItem.jsp' />
		<jsp:include page='/templates/tab2/criteriaPane/filterNumericUnbounded.jsp' />
		<jsp:include page='/templates/tab2/downloadToQueueDialog.jsp' />
		<jsp:include page='/templates/tab2/resultsPane/metaStudyDialog.jsp' />
		<jsp:include page='/templates/main/viewSavedQuery.jsp' />
		<jsp:include page='/templates/tab2/resultsPane/rboxProto.jsp' />
		<jsp:include page='/templates/main/reloadSessionDialog.jsp' />	
		<jsp:include page='/templates/dataTable/dataTableTemplate.jsp' />
		<jsp:include page='/templates/dataTable/lengthMenuTemplate.jsp' />
		<jsp:include page='/templates/dataTable/image.jsp' />
		<jsp:include page='/templates/dataTable/fileDownloadTemplate.jsp' />
		<jsp:include page='/templates/tab2/resultsPane/metaStudyValidationDialog.jsp' />
	</head>
	<body>

		<c:if test="${fn:contains(applicationConstants.styleKey, 'nti')}"> 
			<!-- Google Tag Manager (noscript) -->
			<noscript><iframe src="https://www.googletagmanager.com/ns.html?id=GTM-K2MLJ55"
			height="0" width="0" style="display:none;visibility:hidden"></iframe></noscript>
			<!-- End Google Tag Manager (noscript) -->
		</c:if>
		
		<div id="queryTool" class="container-fluid">
		
		</div>
		<footer id="footer" class="">
		
		
		
		<!--  change instance folder name for different styles -->
		<c:if test="${fn:contains(applicationConstants.styleKey, 'localhost')}"> 
			<jsp:include page='/instances/default/footer.jsp' />
		</c:if>
		<c:if test="${fn:contains(applicationConstants.styleKey, 'nti')}"> 
			<jsp:include page='/instances/nti/footer.jsp' />
		</c:if>
		<c:if test="${fn:contains(applicationConstants.styleKey, 'pdbp')}">  
			<jsp:include page='/instances/pdbp/footer.jsp' />
		</c:if>
		<c:if test="${fn:contains(applicationConstants.styleKey, 'cistar')}"> 
		
		</c:if>
		<c:if test="${fn:contains(applicationConstants.styleKey, 'gsdr')}"> 
			<jsp:include page='/instances/gsdr/footer.jsp' />
		</c:if>
		<c:if test="${fn:contains(applicationConstants.styleKey, 'cnrm')}">
			<jsp:include page='/instances/cnrm/footer.jsp' />
		</c:if>  
		<c:if test="${fn:contains(applicationConstants.styleKey, 'eyegene') || fn:contains(applicationConstants.styleKey, 'nei')}">  
			<jsp:include page='/instances/eyegene/footer.jsp' />
		</c:if>
		<c:if test="${fn:contains(applicationConstants.styleKey, 'cdrns')}">  
			<jsp:include page='/instances/cdrns/footer.jsp' />
		</c:if>
		<c:if test="${fn:contains(applicationConstants.styleKey, 'fitbir')}"> 
			<jsp:include page='/instances/fitbir/footer.jsp' />
		</c:if> 
		
		<div style="margin-left: 132px">
			<p>Version:<span id="deploymentVersionContainer"></span></br>
			Repository ID:<span id="repositoryIDContainer"></span></br>
			Last Deployed:<span id="lastDeployedContainer"></span></p></br>
		</div>
		</footer>
	</body>
	
	<script type="text/javascript">
	<!-- bootstrap - startup -->
	$(document).ready(function() {
		QueryTool.render();
	});
	</script>
</html>