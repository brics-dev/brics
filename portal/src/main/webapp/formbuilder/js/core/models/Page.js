/**
 * 
 */
var Page = BaseModel.extend({
	defaults : {
		form				: 	null,	// the form
		formView			:	null,	// the form view
		activeEditorView 	: 	null,	// View the active (visible) editor
		sectionEditView		: 	null,	// View section editor view
		questionEditView	:	null,	// View the question editor view
		formEditView		:	null,	// View the form editor view
		activeSection		:	null,	// Section Model the active section
		activeQuestion		:	null,	// Question Model the active question
		actionBarView		:	null,	// ActionBarView the action bar view
		
		textBlockEditView	:	null,	// Textblock editor view
		textboxEditView		:	null,	// textbox question editor view
		textareaEditView	:	null,	// textarea question editor view
		radioEditView		:	null,	// radio quesiton editor view
		checkboxEditView	:	null,	// checkbox question editor view
		selectEditView		:	null,	// select quesiton editor view
		multiSelectEditView	:	null,	// multi select question editor view
		imageMapEditView	:	null,	// image map question editor view
		fileUploadEditView	:	null,	// file upload question editor view
		visualScaleEditView :	null,	// visual scale question editor view
		questionLibraryView	:	null,	// question library view
		formStructureView	:	null,	// form structure select view
		processingView		:	null,	// shows a "processing..." dialog
		imageMapProcessView :	null,	// show image map definition dialog
		RepeatableGropuView	:	null,	// show repeatable grope dialog
		loadingData			:	false,	// are we loading a form from the server?
		dataElementsView	:	null,	//dataelements view
		userHelpView		:	null,	// view to provide helpful tips to users
		layoutEditView		:	null,	// editor for section and question layout
		tableLayoutEditView	:	null,	// Table layout edit view
		dataElementsView2	:	null,	// dataelements view for new auto create form builder
		questionTextEditView : null,  //question edit view for advanced question text formatting
		countDialogView		:	null	// count question dialog view
	},
	
	activeSectionId : function() {
		return this.get("activeSection").get("divId");
	},
	
	activeQuestionId : function() {
		return this.get("activeQuestion").get("newQuestionDivId");
	}
});