<!doctype html>
<%@ taglib uri="/struts-tags" prefix="s"%>
<html>
<head>
	<title>Form Management</title>
	<meta charset="utf-8" />
	<s:hidden value="%{formMode}" name="formMode"></s:hidden>
	<s:hidden value="%{modulesDDTURL}" name="modulesDDTURL"></s:hidden>
	<s:hidden value="%{sessionEform}" name="sessionEform"></s:hidden>
	
	
	<!-- All imported styles -->
	<link rel="stylesheet" type="text/css" href='/portal/formbuilder/css/bootstrap/bootstrap.css' />
	
	<link rel="stylesheet" type="text/css" href='/portal/formbuilder/css/formalize.css' />
	
	<link rel="stylesheet" type="text/css" href='/portal/formbuilder/css/formBuilder.css' />
	<link rel="stylesheet" type="text/css" href='/portal/formbuilder/css/actionBar.css' />
	<link rel="stylesheet" type="text/css" href='/portal/formbuilder/css/section.css' />
	<link rel="stylesheet" type="text/css" href='/portal/formbuilder/css/question.css' />
	
	<style type="text/css">
		label {padding-right: 8px; }
		.section {margin-bottom: 10px;}
	</style>
	


	<!-- Development Javascript -->
	<!--JavaScript Libs-->
<%-- 	<script type="text/javascript" src='/portal//formbuilder/js/lib/jquery-1.10.2.js'></script>
	<script type="text/javascript" src='/portal/formbuilder/js/lib/jquery-ui-1.10.3.custom.js'></script> --%>
	<script type="text/javascript" src='/portal/js/ibis/jquery.ibisMessaging-0.1.full.js'></script>
	<script type="text/javascript" src='/portal/formbuilder/js/lib/underscore-min.js'></script>
	<script type="text/javascript" src='/portal/formbuilder/js/lib/backbone.js'></script>
	<script type="text/javascript" src='/portal/formbuilder/js/lib/Backbone.ModelBinder.js'></script>
	<script type="text/javascript" src='/portal/formbuilder/js/lib/Backbone.CollectionBinder.js'></script>
	<script type="text/javascript" src='/portal/formbuilder/js/lib/handlebars.js'></script>
	<script type="text/javascript" src='/portal/formbuilder/js/tinymce/jquery.tinymce.min.js'></script>
	
	<script type="text/javascript" src='/portal/formbuilder/js/lib/jquery.overlaps.js'></script>
	<script type="text/javascript" src='/portal/formbuilder/js/lib/jquery.scrollTo.js'></script>
	<script type="text/javascript" src='/portal/formbuilder/js/lib/HashTable.js'></script>
	<script type="text/javascript" src='/portal/formbuilder/js/lib/ibisCommon.js'></script>
	<script src="/portal/formbuilder/js/lib/jquery.dataTables.js" type="text/javascript"></script>
	<script type="text/javascript" src='/portal/formbuilder/js/lib/y.js'></script>
	<script type="text/javascript" src='/portal/formbuilder/js/lib/qtipNew.js'></script>
	<!-- All permanent application code -->
	
	<!-- Core -->
		<!-- Util -->
		<jsp:include page='/formbuilder/Config.jsp' />
		<script type="text/javascript" src='/portal/formbuilder/js/core/util/underscoreMods.js'></script>
		<script type="text/javascript" src='/portal/formbuilder/js/core/util/Log.js'></script>
		<script type="text/javascript" src='/portal/formbuilder/js/core/util/EventBus.js'></script>
		<script type="text/javascript" src='/portal/formbuilder/js/core/util/TemplateManager.js'></script>
		<script type="text/javascript" src='/portal/formbuilder/js/core/util/Validation.js'></script>
		<script type="text/javascript" src='/portal/formbuilder/js/core/util/StateManager.js'></script>
		<script type="text/javascript" src='/portal/formbuilder/js/util/SectionDragHandler.js'></script>
		<script type="text/javascript" src='/portal/formbuilder/js/util/QuestionDragHandler.js'></script>
		<script type="text/javascript" src='/portal/formbuilder/js/util/RepeatableSectionProcessor.js'></script>
		<script type="text/javascript" src='/portal/formbuilder/js/util/QuestionChangePropagator.js'></script>
		<script type="text/javascript" src='/portal/formbuilder/js/util/AutoBuildSectionUtil.js'></script>
		<script type="text/javascript" src='/portal/formbuilder/js/util/AutoBuildQuestionUtil.js'></script>



		<!-- Models -->
		<script type="text/javascript" src='/portal/formbuilder/js/core/models/BaseModel.js'></script>
		<script type="text/javascript" src='/portal/formbuilder/js/core/models/Page.js'></script>
		
		<!-- Views -->
		<script type="text/javascript" src='/portal/formbuilder/js/core/views/BaseView.js'></script>
		<script type="text/javascript" src='/portal/formbuilder/js/core/views/PageView.js'></script>
		<script type="text/javascript" src='/portal/formbuilder/js/core/views/EditorView.js'></script>

	<!-- Collections -->
	<script type="text/javascript" src='/portal/formbuilder/js/collections/Questions.js'></script>
	<script type="text/javascript" src='/portal/formbuilder/js/collections/Sections.js'></script>
	<script type="text/javascript" src='/portal/formbuilder/js/collections/TextElements.js'></script>
	<script type="text/javascript" src='/portal/formbuilder/js/collections/DeleteSections.js'></script>
	<script type="text/javascript" src='/portal/formbuilder/js/collections/DataElements.js'></script>
	<script type="text/javascript" src='/portal/formbuilder/js/collections/RepeatableGroups.js'></script>

		<!-- Bootstrap -->
		<script type="text/javascript" src='/portal/formbuilder/js/core/FormBuilder.js'></script>

	<!-- Non-Core -->
	
	
		<!-- Models -->
		<script type="text/javascript" src='/portal/formbuilder/js/models/Question.js'></script>
		<script type="text/javascript" src='/portal/formbuilder/js/models/Section.js'></script>
		<script type="text/javascript" src='/portal/formbuilder/js/models/Form.js'></script>
		<script type="text/javascript" src='/portal/formbuilder/js/models/DataElement.js'></script>
		<script type="text/javascript" src='/portal/formbuilder/js/models/RepeatableGroup.js'></script>
		<script type="text/javascript" src='/portal/formbuilder/js/models/Processing.js'></script>
		
		<!-- Views -->
		<script type="text/javascript" src='/portal/formbuilder/js/views/ProcessingView.js'></script>
		<script type="text/javascript" src='/portal/formbuilder/js/views/SectionView.js'></script>
		<script type="text/javascript" src='/portal/formbuilder/js/views/ActionBarView.js'></script>
		<script type="text/javascript" src='/portal/formbuilder/js/views/FormEditView.js'></script>
		<script type="text/javascript" src='/portal/formbuilder/js/views/FormView.js'></script>
		<script type="text/javascript" src='/portal/formbuilder/js/views/SectionEditView.js'></script>
		<script type="text/javascript" src='/portal/formbuilder/js/views/FormEditView.js'></script>
		<script type="text/javascript" src='/portal/formbuilder/js/views/SectionEditView.js'></script>
		<script type="text/javascript" src='/portal/formbuilder/js/views/QuestionEditView.js'></script>
		<script type="text/javascript" src='/portal/formbuilder/js/views/QuestionView.js'></script>
	
		<script type="text/javascript" src='/portal/formbuilder/js/views/FormStructureView.js'></script>
		<script type="text/javascript" src='/portal/formbuilder/js/views/SkipQuestionsView.js'></script>
		<script type="text/javascript" src='/portal/formbuilder/js/views/UserHelpView.js'></script>

		<script type="text/javascript" src='/portal/formbuilder/js/views/DataElementsView2.js'></script>
		<script type="text/javascript" src='/portal/formbuilder/js/views/ImageMapProcessView.js'></script>
		<script type="text/javascript" src='/portal/formbuilder/js/views/RepeatableGroupView.js'></script>
		
		<!-- Question Commons above Questions -->
		<script type="text/javascript" src='/portal/formbuilder/js/views/QuestionFormatDecorator.js'></script>
		<script type="text/javascript" src='/portal/formbuilder/js/views/QuestionValidationDecorator.js'></script>
		<script type="text/javascript" src='/portal/formbuilder/js/views/QuestionPrepopulationDecorator.js'></script>
		<script type="text/javascript" src='/portal/formbuilder/js/views/QuestionOptionDecorator.js'></script>
		<script type="text/javascript" src='/portal/formbuilder/js/views/QuestionEmailDecorator.js'></script>
		<script type="text/javascript" src='/portal/formbuilder/js/views/QuestionGraphicDecorator.js'></script><!-- added by Cing Heng -->
		<script type="text/javascript" src='/portal/formbuilder/js/views/QuestionSkipRuleDecorator.js'></script>
		<script type="text/javascript" src='/portal/formbuilder/js/views/QuestionCalculationRuleDecorator.js'></script>
		<script type="text/javascript" src='/portal/formbuilder/js/views/QuestionConversionFactorDecorator.js'></script>
		<script type="text/javascript" src='/portal/formbuilder/js/views/QuestionImageMapDecorator.js'></script>
		<script type="text/javascript" src='/portal/formbuilder/js/views/QuestionAdditionalTextDecorator.js'></script>
		<script type="text/javascript" src='/portal/formbuilder/js/views/QuestionDefaultValueDecorator.js'></script>
		<script type="text/javascript" src='/portal/formbuilder/js/views/CountDialogView.js'></script>
		<script type="text/javascript" src='/portal/formbuilder/js/views/QuestionCountDecorator.js'></script>
		<script type="text/javascript" src='/portal/formbuilder/js/views/QuestionBtrisMappingDecorator.js'></script>
		
		<script type="text/javascript" src='/portal/formbuilder/js/views/TextBlockEditView.js'></script>
		<script type="text/javascript" src='/portal/formbuilder/js/views/TextBlockView.js'></script>
		<script type="text/javascript" src='/portal/formbuilder/js/views/TextboxEditView.js'></script>
		<script type="text/javascript" src='/portal/formbuilder/js/views/TextboxView.js'></script>
		<script type="text/javascript" src='/portal/formbuilder/js/views/TextareaEditView.js'></script>
		<script type="text/javascript" src='/portal/formbuilder/js/views/TextareaView.js'></script>
		
		<script type="text/javascript" src='/portal/formbuilder/js/views/CheckboxEditView.js'></script>
		<script type="text/javascript" src='/portal/formbuilder/js/views/CheckboxView.js'></script>
		
		
		<script type="text/javascript" src='/portal/formbuilder/js/views/VisualScaleView.js'></script>
		<script type="text/javascript" src='/portal/formbuilder/js/views/VisualScaleEditView.js'></script>
		
		<script type="text/javascript" src='/portal/formbuilder/js/views/SelectEditView.js'></script>
		<script type="text/javascript" src='/portal/formbuilder/js/views/SelectView.js'></script>
        <script type="text/javascript" src='/portal/formbuilder/js/views/MultiSelectEditView.js'></script>
		<script type="text/javascript" src='/portal/formbuilder/js/views/MultiSelectView.js'></script>
		<script type="text/javascript" src='/portal/formbuilder/js/views/FileUploadEditView.js'></script>
		<script type="text/javascript" src='/portal/formbuilder/js/views/FileUploadView.js'></script>
		
		<script type="text/javascript" src='/portal/formbuilder/js/views/RadioEditView.js'></script>
		<script type="text/javascript" src='/portal/formbuilder/js/views/RadioView.js'></script>
		
		<script type="text/javascript" src='/portal/formbuilder/js/views/ImageMapEditView.js'></script>
		<script type="text/javascript" src='/portal/formbuilder/js/views/ImageMapView.js'></script>
		
		<script type="text/javascript" src='/portal/formbuilder/js/views/LayoutEditView.js'></script>
		<script type="text/javascript" src='/portal/formbuilder/js/views/TableLayoutEditView.js'></script>
		<script type="text/javascript" src='/portal/formbuilder/js/views/QuestionTextEditView.js'></script>
	
		<!-- <script src="/portal/js/ibis/jquery.ibisDataTables-2.0.min.js" type="text/javascript"></script> -->
		
		<script src="/portal/js/ibis/DataTableLanguages.js" type="text/javascript"></script>
		<script src="/portal/js/ibis/Row.js" type="text/javascript"></script>
		<script src="/portal/js/ibis/TableCol.js" type="text/javascript"></script>
		<script src="/portal/js/ibis/Rows.js" type="text/javascript"></script>
		<script src="/portal/js/ibis/TableCols.js" type="text/javascript"></script>
		<script src="/portal/js/ibis/DataTable.js" type="text/javascript"></script>
		<script src="/portal/js/ibis/DataTables.js" type="text/javascript"></script>
		<script src="/portal/js/ibis/DataTableView.js" type="text/javascript"></script>
		<script src="/portal/js/ibis/OpButtonController.js" type="text/javascript"></script>
		<script src="/portal/js/ibis/IBISDataTables.js" type="text/javascript"></script>
		
		

	
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
	<jsp:include page='/formbuilder/templates/question/editorGraphic.jsp' />
	<jsp:include page='/formbuilder/templates/question/graphics.jsp' />
	<jsp:include page='/formbuilder/templates/question/skipQuestions.jsp' />
	<jsp:include page='/formbuilder/templates/question/editorAdditionalText.jsp' />
	<jsp:include page='/formbuilder/templates/question/editorDefaultValue.jsp' />
	<jsp:include page='/formbuilder/templates/question/editorQuestionCount.jsp' />
	<jsp:include page='/formbuilder/templates/question/countDialog.jsp' />
	<jsp:include page='/formbuilder/templates/question/editorBtrisMapping.jsp' />

	<jsp:include page='/formbuilder/templates/question/imageMapTamplate.jsp' />
	
	<jsp:include page='/formbuilder/templates/question/editorSkipRule.jsp' />
	<jsp:include page='/formbuilder/templates/question/editorCalculationRule.jsp' />
	<jsp:include page='/formbuilder/templates/question/editorConversionFactor.jsp' />
	<jsp:include page='/formbuilder/templates/builder/layoutEditor.jsp' />
	
	<jsp:include page='/formbuilder/templates/form/dataElements2.jsp' />
	<jsp:include page='/formbuilder/templates/textbox/questionTextEditor.jsp' />
	
	<script type="text/javascript">
	var baseUrl = '<s:property value="modulesDDTURL"/>/dictionary/';
	
	// monitor and refresh the CAS timeout.  THIS DOES NOT INTERFERE WITH THE INDIVIDUAL PAGE TIMEOUT BELOW
	var currentDate = new Date();
	var startTime = currentDate.getTime();
	// check the timer every minute.
	setInterval(function() {refreshCasTimeout();}, 60000);
	var casClearIframeTimeout = null;
	function refreshCasTimeout() {
		// determine if we need to refresh the timeout at all
		var date = new Date();
		var urlRoot = '<s:property value="modulesDDTURL"/>/dictionary/';
		var timeoutThreshold = 132213123213213;
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
		tooltip();
	});
	</script>
	
	
</head>
<body>
	<s:form method="post" id="app" action="eFormSave" cssClass="form-inline container-fluid" >		
	</s:form> 	
	<!-- templates -->
	<script id="visualFormTemplate" type="text/x-handlebars-template">	
	</script>
</body>
</html>
