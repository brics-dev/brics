<jsp:include page="/common/doctype.jsp" />
<%@ taglib uri="/struts-tags" prefix="s"%>
<%@ taglib uri="/WEB-INF/security.tld" prefix="security" %>
<security:check privileges="addeditforms" />
<html>
<head>
	<title>Form Management</title>
	<meta charset="utf-8" />
	
	<s:bean name="gov.nih.nichd.ctdb.util.common.SystemPreferences" var="systemPreferences" />
	<s:set var="webRoot" value="#systemPreferences.get('app.webroot')"/>
	
	<link rel="shortcut icon" type="image/x-icon" 
		href="<s:property value="#systemPreferences.commonImageBaseUrl"/>/favicon.ico" />
	
	<!-- All imported styles -->
	<link rel="stylesheet" type="text/css" href='<s:property value="#webRoot"/>/formbuilder/css/bootstrap/bootstrap.css' />
	<link rel="stylesheet" type="text/css" href='<s:property value="#webRoot"/>/common/c-pdbp/css/style.css' />
	
	<link rel="stylesheet" type="text/css" href='<s:property value="#webRoot"/>/formbuilder/css/formalize.css' />
	
	<link rel="stylesheet" type="text/css" href='<s:property value="#webRoot"/>/formbuilder/css/formBuilder.css' />
	<link rel="stylesheet" type="text/css" href='<s:property value="#webRoot"/>/formbuilder/css/actionBar.css' />
	<link rel="stylesheet" type="text/css" href='<s:property value="#webRoot"/>/formbuilder/css/section.css' />
	<link rel="stylesheet" type="text/css" href='<s:property value="#webRoot"/>/formbuilder/css/question.css' />
	
	<style type="text/css">
		label {padding-right: 8px; }
		.section {margin-bottom: 10px;}
	</style>
	
	<!-- Production Javascript -->
	<!-- <script type="text/javascript" src='<s:property value="#webRoot"/>/formbuilder/js/lib/jquery-1.10.2.js'></script>
	<script type="text/javascript" src='<s:property value="#webRoot"/>/formbuilder/js/lib/jquery-ui-1.10.3.custom.js'></script>
	<script type="text/javascript" src='<s:property value="#webRoot"/>/common/js/jquery.ibisMessaging-0.1.full.js'></script>
	<script src="<s:property value="#webRoot"/>/common/js/ibis/core_libs.min.js" type="text/javascript"></script>
	<script type="text/javascript" src='<s:property value="#webRoot"/>/common/js/tinymce/jquery.tinymce.min.js'></script>
	<script type="text/javascript" src='<s:property value="#webRoot"/>/formbuilder/js/lib/jquery.overlaps.js'></script>
	<script type="text/javascript" src='<s:property value="#webRoot"/>/formbuilder/js/lib/jquery.scrollTo.js'></script>
	<script type="text/javascript" src='<s:property value="#webRoot"/>/common/js/HashTable.js'></script>
	<script type="text/javascript" src='<s:property value="#webRoot"/>/common/js/ibisCommon.js'></script>
	<script src="<s:property value="#webRoot"/>/common/js/ibis/jquery.ibisDataTables-2.0.min.js" type="text/javascript"></script>
	<jsp:include page='/formbuilder/Config.jsp' />
	<script src="<s:property value="#webRoot"/>/common/js/ibis/core_classes.min.js" type="text/javascript"></script> -->

	<!-- Development Javascript -->
	<!--JavaScript Libs-->
	<script type="text/javascript" src='<s:property value="#webRoot"/>/formbuilder/js/lib/jquery-1.10.2.js'></script>
	<script type="text/javascript" src='<s:property value="#webRoot"/>/formbuilder/js/lib/jquery-ui-1.10.3.custom.js'></script>
	<script type="text/javascript" src='<s:property value="#webRoot"/>/common/js/jquery.ibisMessaging-0.1.full.js'></script>
	<script type="text/javascript" src='<s:property value="#webRoot"/>/formbuilder/js/lib/underscore-min.js'></script>
	<script type="text/javascript" src='<s:property value="#webRoot"/>/formbuilder/js/lib/backbone.js'></script>
	<script type="text/javascript" src='<s:property value="#webRoot"/>/formbuilder/js/lib/Backbone.ModelBinder.js'></script>
	<script type="text/javascript" src='<s:property value="#webRoot"/>/formbuilder/js/lib/Backbone.CollectionBinder.js'></script>
	<script type="text/javascript" src='<s:property value="#webRoot"/>/formbuilder/js/lib/handlebars.js'></script>
	<script type="text/javascript" src='<s:property value="#webRoot"/>/common/js/tinymce/jquery.tinymce.min.js'></script>
	<script type="text/javascript" src='<s:property value="#webRoot"/>/formbuilder/js/lib/jquery.overlaps.js'></script>
	<script type="text/javascript" src='<s:property value="#webRoot"/>/formbuilder/js/lib/jquery.scrollTo.js'></script>
	<script type="text/javascript" src='<s:property value="#webRoot"/>/common/js/HashTable.js'></script>
	<script type="text/javascript" src='<s:property value="#webRoot"/>/common/js/ibisCommon.js'></script>
	<script src="<s:property value="#webRoot"/>/common/js/datatables-1.10.2/js/jquery.dataTables.min.js" type="text/javascript"></script>
	<!-- <script type="text/javascript" src='<s:property value="#webRoot"/>/common/js/jquery.dataTables.ibisExtension.js'></script> -->
	
	<!-- All permanent application code -->
	
	<!-- Core -->
		<!-- Util -->
		<jsp:include page='/formbuilder/Config.jsp' />
		<script type="text/javascript" src='<s:property value="#webRoot"/>/formbuilder/js/core/util/underscoreMods.js'></script>
		<script type="text/javascript" src='<s:property value="#webRoot"/>/formbuilder/js/core/util/Log.js'></script>
		<script type="text/javascript" src='<s:property value="#webRoot"/>/formbuilder/js/core/util/EventBus.js'></script>
		<script type="text/javascript" src='<s:property value="#webRoot"/>/formbuilder/js/core/util/TemplateManager.js'></script>
		<script type="text/javascript" src='<s:property value="#webRoot"/>/formbuilder/js/core/util/Validation.js'></script>
		<script type="text/javascript" src='<s:property value="#webRoot"/>/formbuilder/js/core/util/StateManager.js'></script>
		<script type="text/javascript" src='<s:property value="#webRoot"/>/formbuilder/js/util/SectionDragHandler.js'></script>
		<script type="text/javascript" src='<s:property value="#webRoot"/>/formbuilder/js/util/QuestionDragHandler.js'></script>
		<script type="text/javascript" src='<s:property value="#webRoot"/>/formbuilder/js/util/RepeatableSectionProcessor.js'></script>
		<script type="text/javascript" src='<s:property value="#webRoot"/>/formbuilder/js/util/QuestionChangePropagator.js'></script>
		<script type="text/javascript" src='<s:property value="#webRoot"/>/formbuilder/js/util/AutoBuildSectionUtil.js'></script>
		<script type="text/javascript" src='<s:property value="#webRoot"/>/formbuilder/js/util/AutoBuildQuestionUtil.js'></script>



		<!-- Models -->
		<script type="text/javascript" src='<s:property value="#webRoot"/>/formbuilder/js/core/models/BaseModel.js'></script>
		<script type="text/javascript" src='<s:property value="#webRoot"/>/formbuilder/js/core/models/Page.js'></script>
		
		<!-- Views -->
		<script type="text/javascript" src='<s:property value="#webRoot"/>/formbuilder/js/core/views/BaseView.js'></script>
		<script type="text/javascript" src='<s:property value="#webRoot"/>/formbuilder/js/core/views/PageView.js'></script>
		<script type="text/javascript" src='<s:property value="#webRoot"/>/formbuilder/js/core/views/EditorView.js'></script>

	<!-- Collections -->
	<script type="text/javascript" src='<s:property value="#webRoot"/>/formbuilder/js/collections/Questions.js'></script>
	<script type="text/javascript" src='<s:property value="#webRoot"/>/formbuilder/js/collections/Sections.js'></script>
	<script type="text/javascript" src='<s:property value="#webRoot"/>/formbuilder/js/collections/TextElements.js'></script>
	<script type="text/javascript" src='<s:property value="#webRoot"/>/formbuilder/js/collections/DeleteSections.js'></script>
	<script type="text/javascript" src='<s:property value="#webRoot"/>/formbuilder/js/collections/DataElements.js'></script>
	<script type="text/javascript" src='<s:property value="#webRoot"/>/formbuilder/js/collections/RepeatableGroups.js'></script>

		<!-- Bootstrap -->
		<script type="text/javascript" src='<s:property value="#webRoot"/>/formbuilder/js/core/FormBuilder.js'></script>

	<!-- Non-Core -->
	
	
		<!-- Models -->
		<script type="text/javascript" src='<s:property value="#webRoot"/>/formbuilder/js/models/Question.js'></script>
		<script type="text/javascript" src='<s:property value="#webRoot"/>/formbuilder/js/models/Section.js'></script>
		<script type="text/javascript" src='<s:property value="#webRoot"/>/formbuilder/js/models/Form.js'></script>
		<script type="text/javascript" src='<s:property value="#webRoot"/>/formbuilder/js/models/QuestionLibrary.js'></script>
		<script type="text/javascript" src='<s:property value="#webRoot"/>/formbuilder/js/models/DataElement.js'></script>
		<script type="text/javascript" src='<s:property value="#webRoot"/>/formbuilder/js/models/RepeatableGroup.js'></script>
		<script type="text/javascript" src='<s:property value="#webRoot"/>/formbuilder/js/models/Processing.js'></script>
		
		<!-- Views -->
		<script type="text/javascript" src='<s:property value="#webRoot"/>/formbuilder/js/views/ProcessingView.js'></script>
		<script type="text/javascript" src='<s:property value="#webRoot"/>/formbuilder/js/views/SectionView.js'></script>
		<script type="text/javascript" src='<s:property value="#webRoot"/>/formbuilder/js/views/ActionBarView.js'></script>
		<script type="text/javascript" src='<s:property value="#webRoot"/>/formbuilder/js/views/FormEditView.js'></script>
		<script type="text/javascript" src='<s:property value="#webRoot"/>/formbuilder/js/views/FormView.js'></script>
		<script type="text/javascript" src='<s:property value="#webRoot"/>/formbuilder/js/views/SectionEditView.js'></script>
		<script type="text/javascript" src='<s:property value="#webRoot"/>/formbuilder/js/views/FormEditView.js'></script>
		<script type="text/javascript" src='<s:property value="#webRoot"/>/formbuilder/js/views/SectionEditView.js'></script>
		<script type="text/javascript" src='<s:property value="#webRoot"/>/formbuilder/js/views/QuestionEditView.js'></script>
		<script type="text/javascript" src='<s:property value="#webRoot"/>/formbuilder/js/views/QuestionView.js'></script>
		<script type="text/javascript" src='<s:property value="#webRoot"/>/formbuilder/js/views/QuestionLibraryView.js'></script>
		<script type="text/javascript" src='<s:property value="#webRoot"/>/formbuilder/js/views/FormStructureView.js'></script>
		<script type="text/javascript" src='<s:property value="#webRoot"/>/formbuilder/js/views/SkipQuestionsView.js'></script>
		<script type="text/javascript" src='<s:property value="#webRoot"/>/formbuilder/js/views/UserHelpView.js'></script>

		<script type="text/javascript" src='<s:property value="#webRoot"/>/formbuilder/js/views/DataElementsView2.js'></script>
		<script type="text/javascript" src='<s:property value="#webRoot"/>/formbuilder/js/views/ImageMapProcessView.js'></script>
		<script type="text/javascript" src='<s:property value="#webRoot"/>/formbuilder/js/views/RepeatableGroupView.js'></script>
		
		<!-- Question Commons above Questions -->
		<script type="text/javascript" src='<s:property value="#webRoot"/>/formbuilder/js/views/QuestionFormatDecorator.js'></script>
		<script type="text/javascript" src='<s:property value="#webRoot"/>/formbuilder/js/views/QuestionValidationDecorator.js'></script>
		<script type="text/javascript" src='<s:property value="#webRoot"/>/formbuilder/js/views/QuestionPrepopulationDecorator.js'></script>
		<script type="text/javascript" src='<s:property value="#webRoot"/>/formbuilder/js/views/QuestionOptionDecorator.js'></script>
		<script type="text/javascript" src='<s:property value="#webRoot"/>/formbuilder/js/views/QuestionEmailDecorator.js'></script>
		<script type="text/javascript" src='<s:property value="#webRoot"/>/formbuilder/js/views/QuestionGraphicDecorator.js'></script><!-- added by Cing Heng -->
		<script type="text/javascript" src='<s:property value="#webRoot"/>/formbuilder/js/views/QuestionSkipRuleDecorator.js'></script>
		<script type="text/javascript" src='<s:property value="#webRoot"/>/formbuilder/js/views/QuestionCalculationRuleDecorator.js'></script>
		<script type="text/javascript" src='<s:property value="#webRoot"/>/formbuilder/js/views/QuestionConversionFactorDecorator.js'></script>
		<script type="text/javascript" src='<s:property value="#webRoot"/>/formbuilder/js/views/QuestionImageMapDecorator.js'></script>
		<script type="text/javascript" src='<s:property value="#webRoot"/>/formbuilder/js/views/QuestionAdditionalTextDecorator.js'></script>
		<script type="text/javascript" src='<s:property value="#webRoot"/>/formbuilder/js/views/QuestionDefaultValueDecorator.js'></script>
		
		<script type="text/javascript" src='<s:property value="#webRoot"/>/formbuilder/js/views/TextBlockEditView.js'></script>
		<script type="text/javascript" src='<s:property value="#webRoot"/>/formbuilder/js/views/TextBlockView.js'></script>
		<script type="text/javascript" src='<s:property value="#webRoot"/>/formbuilder/js/views/TextboxEditView.js'></script>
		<script type="text/javascript" src='<s:property value="#webRoot"/>/formbuilder/js/views/TextboxView.js'></script>
		<script type="text/javascript" src='<s:property value="#webRoot"/>/formbuilder/js/views/TextareaEditView.js'></script>
		<script type="text/javascript" src='<s:property value="#webRoot"/>/formbuilder/js/views/TextareaView.js'></script>
		
		<script type="text/javascript" src='<s:property value="#webRoot"/>/formbuilder/js/views/CheckboxEditView.js'></script>
		<script type="text/javascript" src='<s:property value="#webRoot"/>/formbuilder/js/views/CheckboxView.js'></script>
		
		
		<script type="text/javascript" src='<s:property value="#webRoot"/>/formbuilder/js/views/VisualScaleView.js'></script>
		<script type="text/javascript" src='<s:property value="#webRoot"/>/formbuilder/js/views/VisualScaleEditView.js'></script>
		
		<script type="text/javascript" src='<s:property value="#webRoot"/>/formbuilder/js/views/SelectEditView.js'></script>
		<script type="text/javascript" src='<s:property value="#webRoot"/>/formbuilder/js/views/SelectView.js'></script>
        <script type="text/javascript" src='<s:property value="#webRoot"/>/formbuilder/js/views/MultiSelectEditView.js'></script>
		<script type="text/javascript" src='<s:property value="#webRoot"/>/formbuilder/js/views/MultiSelectView.js'></script>
		<script type="text/javascript" src='<s:property value="#webRoot"/>/formbuilder/js/views/FileUploadEditView.js'></script>
		<script type="text/javascript" src='<s:property value="#webRoot"/>/formbuilder/js/views/FileUploadView.js'></script>
		
		<script type="text/javascript" src='<s:property value="#webRoot"/>/formbuilder/js/views/RadioEditView.js'></script>
		<script type="text/javascript" src='<s:property value="#webRoot"/>/formbuilder/js/views/RadioView.js'></script>
		
		<script type="text/javascript" src='<s:property value="#webRoot"/>/formbuilder/js/views/ImageMapEditView.js'></script>
		<script type="text/javascript" src='<s:property value="#webRoot"/>/formbuilder/js/views/ImageMapView.js'></script>
		
		<script type="text/javascript" src='<s:property value="#webRoot"/>/formbuilder/js/views/LayoutEditView.js'></script>
		<script type="text/javascript" src='<s:property value="#webRoot"/>/formbuilder/js/views/TableLayoutEditView.js'></script>
		
		<!-- <script src="<s:property value="#webRoot"/>/common/js/ibis/jquery.ibisDataTables-2.0.min.js" type="text/javascript"></script> -->
		<script src="<s:property value="#webRoot"/>/common/designs/datatables2.0/DataTableLanguages.js" type="text/javascript"></script>
		<script src="<s:property value="#webRoot"/>/common/designs/datatables2.0/Row.js" type="text/javascript"></script>
		<script src="<s:property value="#webRoot"/>/common/designs/datatables2.0/TableCol.js" type="text/javascript"></script>
		<script src="<s:property value="#webRoot"/>/common/designs/datatables2.0/Rows.js" type="text/javascript"></script>
		<script src="<s:property value="#webRoot"/>/common/designs/datatables2.0/TableCols.js" type="text/javascript"></script>
		<script src="<s:property value="#webRoot"/>/common/designs/datatables2.0/DataTable.js" type="text/javascript"></script>
		<script src="<s:property value="#webRoot"/>/common/designs/datatables2.0/DataTables.js" type="text/javascript"></script>
		<script src="<s:property value="#webRoot"/>/common/designs/datatables2.0/DataTableView.js" type="text/javascript"></script>
		<script src="<s:property value="#webRoot"/>/common/designs/datatables2.0/OpButtonController.js" type="text/javascript"></script>
		<script src="<s:property value="#webRoot"/>/common/designs/datatables2.0/IBISDataTables.js" type="text/javascript"></script>
		
	<!-- temporary application code -->
	<!-- <script src='<s:property value="#webRoot"/>/formbuilder/js/snippets/sample.js"></script> -->
	
	<script>
/*		try {
			FormBuilder.initialize();
		}
		catch(e) {
			$.ibisMessaging("dialog", "error", "The form builder encountered a fatal error.");
		}
*/
	</script>
	
	<jsp:include page='/formbuilder/templates/builder/actionBar.jsp' />
	<jsp:include page='/formbuilder/templates/builder/addQuestionMenuItem.jsp' />
	<jsp:include page='/formbuilder/templates/builder/addSectionMenuItem.jsp' />
	<jsp:include page='/formbuilder/templates/builder/addTextMenuItem.jsp' />
	<jsp:include page='/formbuilder/templates/builder/cancelMenuItem.jsp' />
	<jsp:include page='/formbuilder/templates/builder/editFormDetailsMenuItem.jsp' />
	<jsp:include page='/formbuilder/templates/builder/layoutMenuItem.jsp' />
	<jsp:include page='/formbuilder/templates/builder/formBuilder.jsp' />
	<jsp:include page='/formbuilder/templates/builder/saveMenuItem.jsp' />
	<jsp:include page='/formbuilder/templates/builder/processing.jsp' />
	<jsp:include page='/formbuilder/templates/form/editor.jsp' />
	<jsp:include page='/formbuilder/templates/section/section.jsp' />
	<jsp:include page='/formbuilder/templates/section/editor.jsp' />
	<jsp:include page='/formbuilder/templates/table/editor.jsp' />
	<jsp:include page='/formbuilder/templates/textbox/editor.jsp' />
	<jsp:include page='/formbuilder/templates/textbox/render.jsp' />
	<jsp:include page='/formbuilder/templates/form/userHelpAddSection.jsp' />
	
	<jsp:include page='/formbuilder/templates/question/textQuestion.jsp' />
	<jsp:include page='/formbuilder/templates/question/textareaQuestion.jsp' />
	<jsp:include page='/formbuilder/templates/question/questionEdit.jsp' />
	<jsp:include page='/formbuilder/templates/question/questionLibrary.jsp' />
	<jsp:include page='/formbuilder/templates/form/formStructure.jsp' />
	<jsp:include page='/formbuilder/templates/question/checkboxQuestion.jsp' />
	<jsp:include page='/formbuilder/templates/question/visualScaleQuestion.jsp' />
	<jsp:include page='/formbuilder/templates/question/visualScaleEditQuestion.jsp' />
	<jsp:include page='/formbuilder/templates/question/selectQuestion.jsp' />
    <jsp:include page='/formbuilder/templates/question/multiSelectQuestion.jsp' />
	<jsp:include page='/formbuilder/templates/question/radioQuestion.jsp' />
	<jsp:include page='/formbuilder/templates/question/fileUploadQuestion.jsp' />
	<jsp:include page='/formbuilder/templates/question/imageMapQuestion.jsp' />
	
	<jsp:include page='/formbuilder/templates/question/editorFormatting.jsp' />
	<jsp:include page='/formbuilder/templates/question/editorValidation.jsp' />
	<jsp:include page='/formbuilder/templates/question/editorPrepopulation.jsp' />
	<jsp:include page='/formbuilder/templates/question/editorOptions.jsp' />
	<jsp:include page='/formbuilder/templates/question/editorOptions_noScore.jsp' />
	<jsp:include page='/formbuilder/templates/question/editorEmail.jsp' />
	<jsp:include page='/formbuilder/templates/question/editorGraphic.jsp' /><!-- added by Cing Heng -->
	<jsp:include page='/formbuilder/templates/question/graphics.jsp' /><!-- added by Cing Heng -->
	<jsp:include page='/formbuilder/templates/question/skipQuestions.jsp' />
	<jsp:include page='/formbuilder/templates/question/editorAdditionalText.jsp' />
	<jsp:include page='/formbuilder/templates/question/editorDefaultValue.jsp' />

	<jsp:include page='/formbuilder/templates/question/imageMapTamplate.jsp' />
	
	<jsp:include page='/formbuilder/templates/question/editorSkipRule.jsp' />
	<jsp:include page='/formbuilder/templates/question/editorCalculationRule.jsp' />
	<jsp:include page='/formbuilder/templates/question/editorConversionFactor.jsp' />
	<jsp:include page='/formbuilder/templates/builder/layoutEditor.jsp' />
	
	<jsp:include page='/formbuilder/templates/form/dataElements2.jsp' />
	
	<script type="text/javascript">
	var baseUrl = "<s:property value='#webRoot'/>";
	
	// monitor and refresh the CAS timeout.  THIS DOES NOT INTERFERE WITH THE INDIVIDUAL PAGE TIMEOUT BELOW
	var currentDate = new Date();
	var startTime = currentDate.getTime();
	// check the timer every minute.
	setInterval(function() {refreshCasTimeout();}, 60000);
	var casClearIframeTimeout = null;
	function refreshCasTimeout() {
		// determine if we need to refresh the timeout at all
		var date = new Date();
		var urlRoot = "<s:property value="#webRoot"/>";
		var timeoutThreshold = <s:property value="#systemPreferences.get('application.integration.cas.localtimeout')"/>;
		var currentTime = date.getTime();
		var elapsedTime = (currentTime - startTime)/60000;
		if (elapsedTime > timeoutThreshold) {
			// create an invisible IFrame
			if ($("#hiddenIframe").length < 1) {
				$("body").append('<iframe id="hiddenIframe" src="'+ urlRoot + '/casLoginRedirect.action?unique='+currentTime+'" style="display: none;"><iframe>');
			}
			else {
				$("#hiddenIframe").attr("src", urlRoot + '/casLoginRedirect.action?unique='+currentTime);
			}
			
			// set a timer to remove the iframe if it doesn't remove itself
			// 1 minute
			casClearIframeTimeout = setTimeout(function() {endIframeProcess();}, 6000);
		}
	}
	function stopRefreshCasTimeout() {
		window.clearTimeout(casClearIframeTimeout);
		//$("#hiddenIframe").remove(); // don't remove iframe because it doesn't hurt anything and it causes a js error in IE9
	}
	function endIframeProcess() {
		if($("#hiddenIframe").length > 0) {
			stopRefreshCasTimeout();
		}
	}
	
	$(document).ready(function() {
		FormBuilder.render();
	});
	</script>
	<%-- <jsp:include page="alltests.html" />  --%>
	
</head>
<body>
	<!-- <form class="form-inline container-fluid" id="app"> -->
	<!-- added by Ching Heng -->
	<s:form method="post" id="app" action="formBuild" cssClass="form-inline container-fluid">
		
	</s:form>

	<!-- </form> -->
	
	<!-- templates -->
	<script id="visualFormTemplate" type="text/x-handlebars-template">
	
	</script>

	<!-- local code -->
</body>
</html>