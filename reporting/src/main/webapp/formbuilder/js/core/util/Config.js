﻿/**
 * Defines a configuration object for use in all parts of the application.
 * Can be modified by the page prior to or after initialization.
 * 
 * @author Joshua Park
 */

var Config = {
	/**
	 * Whether we are in developer mode or production mode.  This decides
	 * what type of error logging is shown and how it is shown.  Also allows
	 * a variety of testing options.
	 */
	devmode : true,
	
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
	 * Original position of the action bar 
	 */
	actionBarTop : 0,
	
	/**
	 * Is this user a administrator 
	 */
	isAdminUser : true,
	
	/**
	 * Initial configuration for editor dialogs.  Pre-styles them
	 */
	editorDefaultConfig : {
		autoOpen: false,
		width: "100%",
		modal: true,
		dialogClass : "formBuilder_editor",
		position: {my: "bottom center", at: "bottom+20px", of: $(window)},
		create: function(event, ui) {
			$(event.target).parent().css('position', 'fixed');
			
		},
		open : function(event, ui) {
			$(this).find(".formcreator_dialog").eq(0).tabs("refresh");
			$(this).dialog("widget").css("position", "fixed").css("top", "auto").css("bottom", "0px");
		},
		resizeStop: function(event, ui) {
			/*var position = [(Math.floor(ui.position.left) - $(window).scrollLeft()),
							 (Math.floor(ui.position.top) - $(window).scrollTop())];
			$(event.target).parent().css('position', 'fixed');
			$(this).dialog('option','position',"fixed");*/
			
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
		dialogErrorContainer:	"fsErrorContainer",
		section				:	"section",
		question			:	"question",
		questionContainer	:	"questionContainer"
	},
	
	styles : {
		active 				: 	"is-active",
		inactive 			:	"is-inactive",
		errorField			:	"ui-state-error"
	},
	
	questionTypes : {
		textbox				:	1,
		textarea			:	2,
		select				:	3,
		radio				:	4,
		multiSelect			:	5,
		checkbox			:	6,
		imageMap			:	9,
		visualscale			:	10,
		fileUpload			:	11,
		textblock			:	12
	},
	
	language : {
		noFormStructureDefined 	: 	"No Form Structure",
		noDataElementDefined 	:	"No Data Element",
		clickHereFix			:	"(click here to link)",
		//Range1 and Range2 validation for Numeric validation
		validateRange1Integer	:   "When entering a value for Range Operator 1, the value must be numeric",
		validateRange2Integer	:   "When entering a value for Range Operator 2, the value must be numeric",
		//Visual Scale
		validateVsWidthMaxLimit :  	"Scale Width should be less than or equal to 150.",
		validateVsEndMaxLimit 	:  	"Scale Range Maximum should be less than or equal to 5000.",
		validateVsEndMimMaxLimit:  	"Scale Range Minimum should be less than or equal to 5000.",
		validateVsWidthMinLimit :	"Scale Width should be greater than zero.",
		validateVsMinMaxRange 	: 	"Scale Maximum must be greater than Scale Minimum.",
		validateVsMinInteger    : 	"Scale Range Minimum for Visual Scale should be Integer.",
		validateVsMaxInteger	:	"Scale Range Maximum for Visual Scale should be Integer.",
		validateVsWidthInteger	:  	"Scale Width for Visual Scale should be Integer. ",
		
		//Options
		validateRequiredOption  :    "Option is required",
		validateNumericScore    :    "Score is not numeric",
		validateOptionsSet      :    "Options have not been set",
		validateOptionsComplete :    "Option has not been moved to option list",
		validateAllOrNoneScore  :    "Score values must either have values in all option entries or no values in all option entries",
		
		// create question
		validateDuplicateQuestion : "Please enter a different question name. The question name must be unique in the Question Library.",
		validateImageHasBeenDone : "Image Map hasn't finished yet."
	},
	
	alienSymbol : ""
};