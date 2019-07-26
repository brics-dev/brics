<!doctype html>
<%@ taglib uri="/struts-tags" prefix="s"%>
<html>
<head>
	<title>ProFoRMS ProToTYPe</title>
	<meta charset="utf-8" />
	
	<s:bean name="gov.nih.nichd.ctdb.util.common.SystemPreferences" var="systemPreferences" />
	<s:set var="webRoot" value="#systemPreferences.get('app.webroot')"/>
	
	<!-- All imported styles -->
	<link rel="stylesheet" type="text/css" href="../../common/c-pdbp/css/style.css" />
	<link rel="stylesheet" type="text/css" href="../css/smoothness/jquery-ui-1.10.3.custom.css" />
	<link rel="stylesheet" type="text/css" href="../css/formalize.css" />
	<link rel="stylesheet" type="text/css" href="../css/bootstrap/bootstrap.css" />
	
	<link rel="stylesheet" type="text/css" href="../css/formBuilder.css" />
	<link rel="stylesheet" type="text/css" href="../css/actionBar.css" />
	<link rel="stylesheet" type="text/css" href="../css/section.css" />
	<link rel="stylesheet" type="text/css" href="../css/question.css" />
	
	<style type="text/css">
		label {padding-right: 8px; }
		.section {margin-bottom: 10px;}
	</style>

	<!--JavaScript Libs-->
	<script src="../js/lib/jquery-1.10.2.js"></script>
	<script src="../js/lib/jquery-ui-1.10.3.custom.js"></script>
	<script src="../../common/js/jquery.ibisMessaging-0.1.full.js"></script>
	<script src="../js/lib/underscore-min.js"></script>
	<script src="../js/lib/backbone.js"></script>
	<script src="../js/lib/Backbone.ModelBinder.js"></script>
	<script src="../js/lib/Backbone.CollectionBinder.js"></script>
	<script src="../js/lib/handlebars.js"></script>
	<script src="../../common/js/tinymce/jquery.tinymce.min.js"></script>
	<script src="../js/lib/jquery.overlaps.js"></script>
	<script src="../js/lib/jquery.scrollTo.js"></script>
	<script src="../../common/js/HashTable.js"></script>
	<script src="../../common/js/ibisCommon.js"></script>
	<script src="../../common/js/jquery.dataTables.js"></script>
	<script src="../../common/js/jquery.dataTables.ibisExtension.js"></script>
	
	<!-- All permanent application code -->
	
	<!-- Core -->
		<!-- Util -->
		<jsp:include page="../Config.jsp" />
		<script src="../js/core/util/underscoreMods.js"></script>
		<script src="../js/core/util/Log.js"></script>
		<script src="../js/core/util/EventBus.js"></script>
		<script src="../js/core/util/TemplateManager.js"></script>
		<script src="../js/core/util/Validation.js"></script>
		<script src="../js/core/util/StateManager.js"></script>
		<script src="../js/util/SectionDragHandler.js"></script>
		<script src="../js/util/QuestionDragHandler.js"></script>
		<script src="../js/util/RepeatableSectionProcessor.js"></script>




		<!-- Models -->
		<script src="../js/core/models/BaseModel.js"></script>
		<script src="../js/core/models/Page.js"></script>
		
		<!-- Views -->
		<script src="../js/core/views/BaseView.js"></script>
		<script src="../js/core/views/PageView.js"></script>
		<script src="../js/core/views/EditorView.js"></script>

	<!-- Collections -->
	<script src="../js/collections/Questions.js"></script>
	<script src="../js/collections/Sections.js"></script>
	<script src="../js/collections/TextElements.js"></script>
	<script src="../js/collections/DeleteSections.js"></script>
		<!-- Bootstrap -->
		<script src="../js/core/FormBuilder.js"></script>

	<!-- Non-Core -->
		<!-- Models -->
		<script src="../js/models/Question.js"></script>
		<script src="../js/models/Section.js"></script>
		<script src="../js/models/Form.js"></script>
		<script src="../js/models/QuestionLibrary.js"></script>
		
		<!-- Views -->
		<script src="../js/views/SectionView.js"></script>
		<script src="../js/views/ActionBarView.js"></script>
		<script src="../js/views/FormEditView.js"></script>
		<script src="../js/views/FormView.js"></script>
		<script src="../js/views/SectionEditView.js"></script>
		<script src="../js/views/FormEditView.js"></script>
		<script src="../js/views/SectionEditView.js"></script>
		<script src="../js/views/QuestionEditView.js"></script>
		<script src="../js/views/QuestionView.js"></script>
		<script src="../js/views/QuestionLibraryView.js"></script>
		<script src="../js/views/SkipQuestionsView.js"></script>

		<script src="../js/views/DataElementsView.js"></script>
		
		<!-- Question Commons above Questions -->
		<script src="../js/views/QuestionFormatDecorator.js"></script>
		<script src="../js/views/QuestionValidationDecorator.js"></script>
		<script src="../js/views/QuestionPrepopulationDecorator.js"></script>
		<script src="../js/views/QuestionOptionDecorator.js"></script>
		<script src="../js/views/QuestionEmailDecorator.js"></script>
		<script src="../js/views/QuestionGraphicDecorator.js"></script><!-- added by Cing Heng -->
		<script src="../js/views/QuestionSkipRuleDecorator.js"></script>
		<script src="../js/views/QuestionCalculationRuleDecorator.js"></script>
		<script src="../js/views/QuestionConversionFactorDecorator.js"></script>
		
		<script src="../js/views/TextBlockEditView.js"></script>
		<script src="../js/views/TextBlockView.js"></script>
		<script src="../js/views/TextboxEditView.js"></script>
		<script src="../js/views/TextboxView.js"></script>
		<script src="../js/views/TextareaEditView.js"></script>
		<script src="../js/views/TextareaView.js"></script>
		
		<script src="../js/views/CheckboxEditView.js"></script>
		<script src="../js/views/CheckboxView.js"></script>
		
		
		<script src="../js/views/VisualScaleView.js"></script>
		<script src="../js/views/VisualScaleEditView.js"></script>
		
		<script src="../js/views/SelectEditView.js"></script>
		<script src="../js/views/SelectView.js"></script>
        <script src="../js/views/MultiSelectEditView.js"></script>
		<script src="../js/views/MultiSelectView.js"></script>
		<script src="../js/views/FileUploadEditView.js"></script>
		<script src="../js/views/FileUploadView.js"></script>
		
		<script src="../js/views/RadioEditView.js"></script>
		<script src="../js/views/RadioView.js"></script>
		
	
	
	
	<jsp:include page="../templates/builder/actionBar.jsp" />
	<jsp:include page="../templates/builder/addQuestionMenuItem.jsp" />
	<jsp:include page="../templates/builder/addSectionMenuItem.jsp" />
	<jsp:include page="../templates/builder/addTextMenuItem.jsp" />
	<jsp:include page="../templates/builder/cancelMenuItem.jsp" />
	<jsp:include page="../templates/builder/editFormDetailsMenuItem.jsp" />
	<jsp:include page="../templates/builder/formBuilder.jsp" />
	<jsp:include page="../templates/builder/saveMenuItem.jsp" />
	<jsp:include page="../templates/form/editor.jsp" />
	<jsp:include page="../templates/section/section.jsp" />
	<jsp:include page="../templates/section/editor.jsp" />
	<jsp:include page="../templates/textbox/editor.jsp" />
	<jsp:include page="../templates/textbox/render.jsp" />
	
	<jsp:include page="../templates/question/textQuestion.jsp" />
	<jsp:include page="../templates/question/textareaQuestion.jsp" />
	<jsp:include page="../templates/question/questionEdit.jsp" />
	<jsp:include page="../templates/question/questionLibrary.jsp" />
	<jsp:include page="../templates/question/checkboxQuestion.jsp" />
	<jsp:include page="../templates/question/visualScaleQuestion.jsp" />
	<jsp:include page="../templates/question/visualScaleEditQuestion.jsp" />
	<jsp:include page="../templates/question/selectQuestion.jsp" />
    <jsp:include page="../templates/question/multiSelectQuestion.jsp" />
	<jsp:include page="../templates/question/radioQuestion.jsp" />
	<jsp:include page="../templates/question/fileUploadQuestion.jsp" />
	
	<jsp:include page="../templates/question/editorFormatting.jsp" />
	<jsp:include page="../templates/question/editorValidation.jsp" />
	<jsp:include page="../templates/question/editorPrepopulation.jsp" />
	<jsp:include page="../templates/question/editorOptions.jsp" />
	<jsp:include page="../templates/question/editorOptions_noScore.jsp" />
	<jsp:include page="../templates/question/editorEmail.jsp" />
	<jsp:include page="../templates/question/editorGraphic.jsp" /><!-- added by Cing Heng -->
	<jsp:include page="../templates/question/graphics.jsp" /><!-- added by Cing Heng -->
	<jsp:include page="../templates/question/skipQuestions.jsp" />

	
	<jsp:include page="../templates/question/editorSkipRule.jsp" />
	<jsp:include page="../templates/question/editorCalculationRule.jsp" />
	<jsp:include page="../templates/question/editorConversionFactor.jsp" />
	
	<jsp:include page="../templates/question/dataElements.jsp" />
	
	
	
	<script type="text/javascript">
	var baseUrl = "<s:property value='#webRoot'/>";
	
	$(document).ready(function() {
		FormBuilder.render(); 
	});
	</script>
	<%-- <jsp:include page="alltests.html" />  --%>
	
</head>
<body>
	<form class="form-inline container-fluid" id="app">

	</form>
	<!-- templates -->
	<script id="visualFormTemplate" type="text/x-handlebars-template">
	
	</script>

	<!-- local code -->
</body>
</html>