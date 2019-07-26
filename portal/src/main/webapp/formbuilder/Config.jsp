<%@ taglib uri="/struts-tags" prefix="s"%>
<script>
/**
 * Defines a configuration object for use in all parts of the application.
 * Can be modified by the page prior to or after initialization.
 * 
 * @author Joshua Park
 */
 var admin = false;


var Config = {
	/**
	 * Whether we are in developer mode or production mode.  This decides
	 * what type of error logging is shown and how it is shown.  Also allows
	 * a variety of testing options.
	 */
	devmode : true,
	
	/**
	 * Should the formbuidler automatically generate sections and questions on
	 * form creation.  This will not affect edit - only create.
	 */
	autoGenerate : true,
	
	userId : 1,
	
	/**
	 * Base URL for everything
	 */
	baseUrl : '<s:property value="modulesDDTURL"/>dictionary/',
	
	/**
	 * What is the primary container for sections, texts, etc.
	 */
	containerDiv : $("body"),
	
	/**
	 * If the user drags a section within sectionDragTolerance, the section
	 * should act like it is a part of that row
	 */
	sectionDragTolerance : 15,
	
	/**
	 * If the user drags a question within questionDragTolerance, the 
	 * question should act like it is a part of that row
	 */
	questionDragTolerance : 10,
	
	/**
	 * The max number of columns used for elements.  For bootstrap, this is 12
	 */
	maxColumns : 12,
	
	/**
	 * The max height for dialog boxes that can contain large table.
	 */
	getMaxDialogHeight : function() {
		var maxHeight;
		
		if ( typeof window.innerHeight != "undefined" ) {
			maxHeight = window.innerHeight - 25;
		}
		else {
			maxHeight = false;
		}
		
		return maxHeight;
	},
	
	/**
	 * Original position of the action bar 
	 */
	actionBarTop : 0,
	
	/** added by Ching Heng
	 *  is the user an admin? 
	 */
	isAdmin : admin,

	/** added by Ching Heng
	 *  get the form mode 
	 */
	formMode : '<s:property value="formMode"/>',
	copyMode : '<s:property value="sessionEform.copyMode"/>',
	
	/** added by Ching Heng
	 *  form information dialog 
	 */
	formInfoMode : 'add',
	
	builderStarted : 'false',
	
	/** added by Ching Heng
	 *  get the editing form information  
	 */
	 formForm : {
		 	name : '<s:property value="sessionEform.eform.title" escapeJavaScript="true" escapeHtml="true"/>',
		    //status : Number($("#formdata-status").html()),
		    status :'<s:property value="sessionEform.eform.status.type" />',
		    description : '<s:property value="sessionEform.eform.description" escapeJavaScript="true" escapeHtml="true"/>',
		    formborder : <s:property value="sessionEform.eform.formBorder" />,
		    //sectionborder : $("#formdata-sectionborder").html(),
		      sectionborder :<s:property value="sessionEform.eform.sectionBorder" />,
		    //formfont : $("#formdata-formfont").html(),
		     formfont : '<s:property value="sessionEform.eform.formNameFont" />',
		    //formcolor : $("#formdata-formcolor").html(),
		      formcolor :  '<s:property value="sessionEform.eform.formNameColor" />',
		    //sectionfont : $("#formdata-sectionfont").html(),
		     sectionfont :'<s:property value="sessionEform.eform.sectionNameFont" />',
		    ///sectioncolor : $("#formdata-sectioncolor").html(),
		     sectioncolor : '<s:property value="sessionEform.eform.sectionNameColor" />',
		    //dataEntryFlag : Number($("#formdata-dataEntryFlag").html()),
		    //accessFlag : Number($("#formdata-accessFlag").html()),
		    //isAdministered : Number($("#formdata-isAdministered").html()),
		    //formHeader : $("#formdata-formHeader").html(),
		     formHeader : '<s:property value="sessionEform.eform.header" />',
		   // formFooter : $("#formdata-formFooter").html(),
		     formFooter :'<s:property value="sessionEform.eform.footer" />',
		    //formGroups : $("#formdata-formGroups").html(),
		   // availiableFormGroups : $("#formdata-availiableFormGroups").html(),
		    //fontSize : Number($("#formdata-fontSize").html()),
		        fontSize :<s:property value="sessionEform.eform.fontSize" />,
		   // dataEntryWorkflowType : Number($("#formdata-dataEntryWorkflowType").html()),
		   // dataEntryFlagNo : Number($("#formdata-dataEntryFlagNo").html()),
		   // cellpadding : Number($("#formdata-cellpadding").html()),
		    cellpadding : <s:property value="sessionEform.eform.cellPadding" />,
		    //attachFiles : $("#formdata-attachFiles").html() == "true",
		    //dataSpring : $("#formdata-dataSpring").html() == "true",
		    //tabdisplay : $("#formdata-tabdisplay").html() == "true",
		   // formtypeid : Number($("#formdata-formtypeid").html()),
		    formid : <s:property value="sessionEform.eform.id" />,
		    //nonpatientformtypeid : Number($("#formdata-nonpatientformtypeid").html()),
		    //questionTypeDisplay : $("#formdata-questionTypeDisplay").html(),
		    //options : $("#formdata-options").html(),
		  
		    dataStructureName : '<s:property value="sessionEform.eform.formStructureShortName" />',
		    //dataStructureVersion : $("#formdata-dataStructureVersion").html(),
			//add for copyright
			//copyRight : $("#formdata-copyRight").html() == "true",
			//for allowing multiple instances of data collections for same form

			allowMultipleCollectionInstances :<s:property value="sessionEform.eform.allowMultipleCollectionInstances" />,
			//descriptionUp : $("#formdata-descriptionUp").html(),
			descriptionUp : '<s:property value="sessionEform.eform.descriptionUp" />',
			//descriptionDown : $("#formdata-descriptionDown").html(),  
			descriptionDown : '<s:property value="sessionEform.eform.descriptionDown" />',
			shortName :'<s:property value="sessionEform.eform.shortName" />'
			//statusHidden : $("#formdata-statusHidden").html()
			
	 },
	 
	 questionDefaults : {
		minCharacters 		:	0,
		maxCharacters		:	4000
	 },
	 
	 sections : {
		defaultRepeat		:	"20",
		maxRepeat			:	45,
		descriptionMaxLength:	4000
	 },
	 
	 questionText : {
			minQuestionText 	:	0,
			maxQuestionText		:	4000,
			maxDescription			:   4000
		 },
	 
	/**
	 * Initial configuration for editor dialogs.  Pre-styles them
	 */
	editorDefaultConfig : {
		autoOpen: false,
		width: "100%",
		modal: true,
		resizable: false,
		dialogClass : "formBuilder_editor",
		//position: {my: "bottom center", at: "bottom+20px", of: $(window)},
		create: function(event, ui) {
			$(event.target).parent().css('position', 'fixed');
			
		},
		open : function(event, ui) {
			$(this).find(".formcreator_dialog").eq(0).tabs("refresh");
			$(this).dialog("widget").css("position", "fixed").css("top", "auto").css("bottom", "0px");
		},
		resizeStop: function(event, ui) {
			$(this).find(".formcreator_dialog").eq(0).tabs("refresh");
		},
		draggable: false
	},
	
	/**
	 * Initial configuration for editor tabs.  Pre-styles them
	 */
	editorDefaultTabsConfig : {
		heightStyle : "content"
	},
	
	/**
	 * class names for elements on the page so that we don't have to hard
	 * code those
	 */
	identifiers : {
		editorErrorContainer:	"editorErrorContainer",
		section				:	"section",
		question			:	"question",
		questionContainer	:	"questionContainer",
		dialogErrorContainer:	"fsErrorContainer",
		formContainer		:	"formContainer"
	},
	
	styles : {
		active 				: 	"is-active",
		inactive 			:	"is-inactive",
		errorField			:	"ui-state-error"
	},
	
	questionTypes : {
		textbox				:	"1",
		textarea			:	"2",
		select				:	"3",
		radio				:	"4",
		multiSelect			:	"5",
		checkbox			:	"6",
		imageMap			:	"9",
		visualscale			:	"10",
		fileUpload			:	"11",
		textblock			:	"12"
	},
	
	questionTypeNames : {
		1					:	"Textbox",
		2					:	"Textarea",
		3					:	"Select",
		4					:	"Radio Button",
		5					:	"Multi-Select",
		6					:	"Checkbox",
		9					:	"Image Map",
		10					:	"Visual Scale",
		11					:	"File Upload",
		12					:	"Text Block"
	},
	
	formStatusNames : {
		1					:	"Inactive",
		2					:	"Checked Out",
		3					:	"Active",
		4					:	"In Progress",
		5					:	"External"
	},
	
	formStatusInverse : {
		"Inactive"			:	1,
		"Checked Out"		:	2,
		"Active"			:	3,
		"In Progress"		:	4,
		"External"			:	5
	},
	//answerType : 1,    //1: string, 2: numeric 3: date 4:date-time
	answerType : {
		"string"			:	1,
		"numeric"		    :	2,
		"date"			    :	3,
		"datetime"		    :	4
	},
	//rangeOperator
	rangeOperator : {
		"isEqualTo"  : 1,
		"lessThan"    : 2,
		"greaterThan" : 3,
		"between"      : 4
	},
	
	tableHeaderTypes : {
		rowHeader			:	1,
		columnHeader		:	2,
		tableHeader			:	3,
		none				:	0
		
	},
	
	tableHeaderClassNames	: {
		rowHeader	: "rowHeader",
		columnHeader	: "columnHeader",
		tableHeader		: "tableHeader",
		tablePrimary     : "tablePrimary"
		
	},
	
	userLocale : "en_US",
	
	language : null,
	
	eng_lang : {
		// Form
		formName				:	"eForm Name is Required",
		shortName				:    "eForm Short Name is Required",
		formNameLong			:	"eForm Name must be fewer than 100 characters long",
		descriptionLong			:	"eForm description must be fewer than 1000 characters long",
		noFormStructureDefined 	: 	"No Form Structure Linked",
		noDataElementDefined 	:	"No Data Element Linked",
		noImageMapDefined       :	"No Image Map Defined",
		clickHereFix			:	"(click here to link)",
		multiInstanceDoubleData	:	"Double Data entry and multiple instances cannot both be turned on",
		formHeaderLong          :   "eFrom  header should be fewer than 500 characters long",
		
		// Form messages
		requriedDeError		:	"There was an error in the web service call for the required data elements of the form structure.",
		dupFormError		:	"Please enter a different form name. The form name must be unique in the study.",
		saveFormError		:	"An error occurred while saving the form. Please try again later or contact the system administrator.",
		fsWsCallError		:	"There was an error in the web service call for the selected form structure.",
		
		fsWsAccessError		:	"There was a network issue while getting the form structures from the server. Please try again later or contact the system administrator.",
		fsContainsDeprecatedOrRetiredDEs    :  "This form structure contains either deprecated and/or retired data element(s).",
		
		// Question Library
		questLibFilterError		:	"An error occured while trying to filter out existing questions from the library.",
		getQuestLibError		:	"An error occured while trying to retrieve the question library from the server.",
		questLibDefaultError	:	"Could not load the question library. Please contact the system administrator",
		
		// Range1 and Range2 validation for Numeric validation
		validateRange1Integer	:   "When entering a value for Range Operator 1, the value must be numeric.",
		validateRange2Integer	:   "When entering a value for Range Operator 2, the value must be numeric.",
		betweenValue1Empty      :   "Value for >= can't be empty if you are selecting between range operator.",
		betweenValue2Empty      :   "Value for <= can't be empty if you are selecting between range operator.",
		value1LessThan2			: 	"Value for >= should be less than value for <=",
		calcRuleValidation	    :   "You can't change the answer type for this question because this is either calulated Question or calculation Dependent Question.",
		
		// Visual Scale
		validateVsWidthMaxLimit 	:	"Scale Width should be less than or equal to 150.",
		validateVsEndMaxLimit 		:	"Scale Range Maximum should be less than or equal to 5000.",
		validateVsEndMimMaxLimit	:	"Scale Range Minimum should be less than or equal to 5000.",
		validateVsWidthMinLimit		:	"Scale Width should be greater than zero.",
		validateVsMinMaxRange		:	"Scale Maximum must be greater than Scale Minimum.",
		validateVsMinInteger		:	"Scale Range Minimum for Visual Scale should be Integer.",
		validateVsMaxInteger		:	"Scale Range Maximum for Visual Scale should be Integer.",
		validateVsWidthInteger		:	"Scale Width for Visual Scale should be Integer. ",
		validateVScenterText		:	"Scale question center text can not contain any special characters.",
		validateVSleftText			:	"Scale question left text can not contain any special characters.",
		validateVSrightText			:	"Scale question right text can not contain any special characters.",
		
		// Options
		validateRequiredOption   :  "Option is required",
		validateNumericScore     :  "Score is not numeric",
		validateOptionsSet       :  "Options have not been set",
		validateOptionsComplete  :  "Option has not been moved to option list",
		validateAllOrNoneScore   :  "Score values must either have values in all option entries or no values in all option entries",
		validateDuplicatedOption :	"An option with same text already exists",
		multipleEditing			 :	"You must complete updating the current option before editing another one",
		
		// Prepopulation
		requiredPreValue		:	"Value to prepopulate is required",
		
		// Create question
		questionNameRequired		:	"Question Name is Required",
		validateDuplicateQuestion	:	"Please enter a different question name. The question name must be unique in the Question Library.",
		validateImageHasBeenDone	:	"Image Map hasn't finished yet.",
		validateAllOrNoneScore		:	"Score values must either have values in all option entries or no values in all option entries",
		validateQuestionNameSpecial	:	"Question name can not contain any special characters.",
		validateQuestionNameWhite	:	"Question name can not contain any white space.",
		newQuestionText				:	"Enter Question Text",
		
		// Image Map
		validateImageMap : "Image Map has not been defined",
		
		// SkipRule
		validateContainsSkipRuleEquals  :    "Please specify a contains value for the skip rule.",
		validateEqualsSkipRuleEquals  :    "Please specify an equals value for the skip rule.",
		validateSkipRuleType  :    "Please specify a skip rule.",
		validateSkipRuleQuestions  :    "Please assign questions to skip for the skip rule.",
		
		// Question text
		questionText 			: "Text is required.",
		questionTextEditorTitle : "Edit Text Block",
		maxQuestionText	:	"Question text length can not be greater than 4000 characters.",
		descriptionAboveText	:	"Description Above text length can not be greater than 4000 characters.",
		descriptionBelowText	:	"Description Below text length can not be greater than 4000 characters.",
		
		// formatting
		validateIndent  		: "Indent value in the Format part must be a Positive Integer.",
		validateIndentValue  	: "Indent value in the Format part must less than 50.",
		
		// default value
		defaultValueFailed 		: "The default value must be one of the defined answer options",
		
		// form Structure
		fsDialogTitle			: "Form Structure",
		validateFS				: "There was a problem reading the form structure.  Please re-select the appropriate structure",
		changedFS				: "You have selected a different form structure to associate this form to. \n All data element associations with questions will be lost. \n You will need to reassociate questions to data elements. \n Do you wish to continue?",
		fsLoadError				: "There was a problem retrieving the form information.  We suggest you return to the \"My Forms\" page and try again",
		
		// data element
		deDialogTitle		: "Data Element",
		openDeTableError	: "The data element table could not be opened.",
		deMatchWarnHtml		: "Some of the question values will be overwritten based on the linked data element!<br/>Do you wish to overwrite?",
		matchWarnBoxTitle	: "Overwrite Values",
		
		//repeat group
		changeRG				:	"Changing the repeatable group will clear all data element associations to questions already attached to this section. Do you wish to continue?",
		
		// processing
		loadFormData			:	"Loading eForm Data...",
		loadDataElem			:	"Loading the Data Element table...",
		loadQuestLib			:	"Loading the Question Library table...",
		loadSkipQuest			:   "Loading the Skip Question table...",
		loadFormStructTable		:	"Retrieving form structures from the server...",
		fsConstructTableMsg		: 	"Building the form structure table...",
		creatingForm			:	"Creating your form and retrieving Form Structure details...",
		cancellingFormCreation  :    "Cancelling form creation process ...",
		cleaningCopiedData      :    "Cleaning copied eForm data...",
	
		// Email
		emailRecipientRequired  :   "When creating an email trigger, email recipient is required.",
		emailAddress			:	"Email Address is not a valid email address; please specify only valid email addresses.",
		triggerAnswer			:	"When creating an email trigger, answers to activate the trigger are required.",
		
		// delete Question
		deleteSkipQuestion		:	"This question can't be deleted since it is part of another question's skip rule",
		deleteCalQuestion		:	"This question can't be deleted since it is part of another question's calculation rule",
		
		// section
		sectionName				:	"The section name is required",
		minMax					:	"Repeatable maximum must be equal or greater than repeatable minimum.",
		initialMin				:	"Repeatable Min must be greater than 0.",
		lessThan45				:	"Repeatable Max cannot be greater than 45.",
		sectionText				:	"Section text must be shorter than 4000 characters in length",
		atLeast					:	"Maximum Number of times viewed is less than the Repeatable Group threshold of ",
		upTo					:	"Maximum Number of times viewed can not be greater than Repeatable Group threshold of ",
		exactly					:	"Maximum Number of times viewed needs to be equal as Repeatable Group threshold of ",
		defaultTextName			:	"Header Row",
		
		// delete section
		hasSkip					:	"This section can't be deleted because it contains a question which is part of another question's skip rule",
		hasCal					:	"This section can't be deleted because it contains a question which is part of another question's calculation rule",
		confirmDelete			:	"Are you sure you want to delete this section?",
		hasSkipOrCalc			:	"This section can't be deleted because it contains a question which is a part of another question's skip or calculation rule",
		updateHasSkipOrCalc		:	"The specified change cannot be completed because one or more questions in this section are referenced in a calculation or skip rule",
		
		// convertor factor
		dateTime				:	"Please select a date-time conversion factor.",
		operrator				:	"The calculation rule cannot start with an operator.",
		divisionZero			:	"The calculation rule cannot include division by 0.",
		dateFormular			:	"Date Questions can only be used in subtraction from each other (ex S_1_Q_1 - S_2_Q_2) all other uses of date or date-time functions are illegal.",
		fialComputation			:	"The entered calculation rule fails computation. Please correct the error.",
		noquestion				:   "The calculation rule has to include at least one question.",
		doNotScoreError         :   "Please provide a valid calculation rule or uncheck the Do Not Calculate checkbox.",
		circularDependency		:   "There is circular logic in the calculation rule for this question with: ",
		correctbeforesave		: 	"Please correct this before saving.",
		
		// validation
		minGraterThanMax		:	"Please enter a default value that is not less than the Minimum Characters specified.",
		maxChars				:	"Maximum Characters can not be greater than 4000.",
		maxGreatThanOne			:	"Maximum Characters should be greater than 1.",
		minChars				:	"Minimum Characters can not be less than 0.",
		maxNum					:	"Maximum Characters must be a Positive Integer.",
		minNum					:	"Minimum Characters must be a Positive Integer.",
		
		// Button Titles
		addBtnTitle			: "Add",
		cancelBtnTitle		: "Cancel",
		yesBtnTitle			: "Yes",
		noBtnTitle			: "No",
		continueBtnTitle	: "Continue"
	},
	
	urls : {
		getFormInfo				:	"eformEditJsonAction!createEformJSONForEdit.action",
		//getFormInfo				:	"/form/saveEditForm.action?action=process_edit_forminfo&formMode=edit",
		//saveFormInfo			:	"/form/saveNewForm.action?action=process_add_forminfo"
		saveFormInfo			:	"eFormAction!getSelectedFS.action"
	}
};

// Choose a language based on the user's locale.
switch (Config.userLocale) {
	case "en_US" :
		Config.language = Config.eng_lang;
		break;
	default :
		Config.language = Config.eng_lang;
		break;
}
</script>