/**
 * This View represents the page itself and allows the code to 
 */
$.getScript('/js/sessionHandler.js', function() {});
var PageView = BaseView.extend({
	actionBarSubview : null,
	loadFormStrucutre : 0,	
	
	initialize : function() {
		EventBus.on("change:activeSection", this.changeActiveSection, this);
		EventBus.on("change:activeEditor", this.setActiveEditor, this);
		EventBus.on("open:sectionEditor", this.openSectionEditor, this);
		EventBus.on("open:formEditor", this.openFormEditor, this);
		EventBus.on("close:activeEditor", this.destroyActiveEditor, this);
		EventBus.on("open:questionEditor", this.openQuestionEditor, this);
		EventBus.on("change:activeQuestion", this.changeActiveQuestion, this);
		EventBus.on("open:questionLibrary", this.openQuestionLibrary, this);
		EventBus.on("open:formStructure", this.openFormStructure, this);
		EventBus.on("add:questionLibrary", this.addQuestionFromLibrary, this);
		EventBus.on("change:questionType", this.changeQuestionType, this);
		EventBus.on("open:layoutEditor", this.openLayoutEditor, this);
		EventBus.on("open:tableEditor", this.openTableEditor, this);
		EventBus.on("scrollto:element", this.scrollPageTo, this);
		// moved by Ching Heng
		//EventBus.on("change:question", this.updateQuestions, this);
		
		var form = new Form();
		this.model.set("form", form);
		this.model.set("formView", new FormView({model: form}));
		this.model.set("formEditView", new FormEditView({model: form}));
		this.model.set("sectionEditView", new SectionEditView());
		this.model.set("textBlockEditView", new TextBlockEditView());
		
		this.model.set("textboxEditView", new TextboxEditView());
		this.model.set("textareaEditView", new TextareaEditView());
	
		this.model.set("formStructureView", new FormStructureView({model: form}));
		this.model.set("checkboxEditView", new CheckboxEditView());
		this.model.set("selectEditView", new SelectEditView());
        this.model.set("multiSelectEditView", new MultiSelectEditView());
        this.model.set("radioEditView", new RadioEditView());
        this.model.set("fileUploadEditView", new FileUploadEditView());
        this.model.set("processingView", new ProcessingView());
        this.model.set("imageMapProcessView", new ImageMapProcessView());
        this.model.set("repeatableGroupView", new RepeatableGroupView());
        
		this.model.set("visualScaleEditView", new VisualScaleEditView());
		this.model.set("imageMapEditView", new ImageMapEditView());
		//this.model.set("dataElementsView", new DataElementsView());
		this.model.set("userHelpView", new UserHelpView({model: form}));
		this.model.set("layoutEditView", new LayoutEditView());
		this.model.set("tableLayoutEditView", new TableLayoutEditView());
		
		this.model.set("dataElementsView2", new DataElementsView2({model:form}));
		
		this.$el = $("#app");
		
		this.template = TemplateManager.getTemplate("formBuilder");
		
		this.model.set("actionBarView", new ActionBarView({model: this.model.get("form")}));
		this.model.set("questionTextEditView", new QuestionTextEditView());
	},
	
	render : function() {
		$(window).scrollTo(0);
		this.$el.html(this.template(this.model.attributes));
		//$("body").append(this.$el);
		this.assign({
			'#actionBar'		:	this.model.get("actionBarView")
		});
		
		// Reassign containerDiv here because it didn't exist until now
		Config.containerDiv = $("#formContainer");
		
		this.model.get("formView").render();
		SectionDragHandler.init();
		QuestionDragHandler.init();
		StateManager.init();
		QuestionChangePropagator.init();
		
		$("#spacer").height($(window).height());
		
		window.onresize = function(event) {
			$("#spacer").height($(window).height());
		};
		
		//added by Ching Heng
		if(Config.formMode == 'create'){
			
			EventBus.trigger('open:formEditor', FormBuilder.form);
		}else{
			FormBuilder.form.repopulate(Config.formForm);
			this.loadFormData();
			if(Config.copyMode=='true'){
				EventBus.trigger('open:formEditor', FormBuilder.form);
			}
		}
	},
	
	loadFormData : function() {
		thisView = this;
		$.ajax({
			type: "POST",
			url: Config.baseUrl + Config.urls.getFormInfo,
			data: FormBuilder.form.serializeModel(),
	        beforeSend: function() {
	        	EventBus.trigger("open:processing", Config.language.loadFormData);
	        },
	        
			success: function(data) {
				FormBuilder.page.set("loadingData", true);
				var response = $.parseJSON(data);
				//var splits = response.split("::");
				
//				if (splits.length < 7) {
//					$.ibisMessaging("dialog", "error", Config.language.fsLoadError);
//					EventBus.trigger("close:processing");
//				}
//				else {
					/* 
					 * The first few are provided in Config
					 * 0 = form Name
					 * 1 = form Status
					 * 2 = form ID
					 * 3 = isDataSpring
					 * 4 = Data Structure Name
					 */
					var formData = {
//						formStructure : splits[5],
//						sectionsJSONArrJSONString : splits[6],
//						questionsJSONArrJSONString : splits[7]
							formStructure : response['dataStructObj'],
							sectionsJSONArrJSONString : response['sectionsJSONArrJSONString'],
							questionsJSONArrJSONString : response['questionsJSONArrJSONString']
					};
					
					thisView.loadFormStructure = thisView.loadFormStructure(formData);
					
					/*
					 * The following processes are asynchronous BUT need to run
					 * in order relative to each other.  So, they each call the
					 * next step.  Here's the order.
					 * * load form structure
					 * * add all sections
					 * * add all questions
					 * * finalize (finalizeLoadingForm())
					 */
					thisView.addAllSections(formData);
				//}
			},
			error: function(e) {
				$.ibisMessaging("dialog", "error", Config.language.fsLoadError + ".  Error: " + e);
			}
		});
	},
	
	/**
	 * Because adding questions is asynchronous, we need to call this once it
	 * is finished processing to finish up the form
	 */
	finalizeLoadingForm : function() {
		Config.formInfoMode = 'edit';
		FormBuilder.form.setCalDependent();//Call call dependent function to load calc Dependent flag on form edit
		FormBuilder.form.setSkipRuleDependent();//Call skip rule dependent function to load skip Dependent flag on form edit
		FormBuilder.page.set("loadingData", false);
		EventBus.trigger("close:processing");
		QuestionDragHandler.resizeAllQuestionsSections();
		
		//need to do the following only if form structure cant be loaded!
		if(FormBuilder.pageView.loadFormStructure == -1) {
			EventBus.trigger('open:formEditor', FormBuilder.form);
			EventBus.trigger("open:formStructure");
			
			//need to display error message on top of 
			_.defer(function() {
				var view = FormBuilder.page.get("formStructureView");
				view.showWarning("The form structure currently associated to this form does not exist in the data dictionary.  Please select a new form structure.");
				view.switchButtonsToBad();
			});
			//when user selects new form structure...need to de-associate all old elements and group names?
		}
	},
	
	loadFormStructure : function(formData) {
		var dataStructureString = formData.formStructure;
		if (this.formStructureString != "") {
			var dataStructureJSON = formData.formStructure;
			var dataElementJsonArray;
			var repeatableGroupsJsonArray;
			var allGroupsJsonArray;
			if(dataStructureJSON != null) {
				FormBuilder.form.set("isCAT", formData.formStructure.isCAT);	// added by Ching-Heng
				dataElementJsonArray = dataStructureJSON['dataElements'];
				repeatableGroupsJsonArray = dataStructureJSON["repeatableGroupList"];
				allGroupsJsonArray = dataStructureJSON["allGroupsList"];
				FormBuilder.form.set("copyrightedForm",dataStructureJSON["copyrightedForm"]);
				FormBuilder.form.loadDataElements(dataElementJsonArray);
				FormBuilder.form.loadRepeatableGroups(repeatableGroupsJsonArray);
				FormBuilder.form.loadAllGroups(allGroupsJsonArray);				
				return 1;
			}else {
				return -1;
			}
		}
	},
	
	sectionSort : function(a, b) {
		if (a.row != b.row) {
			return a.row - b.row;
		}
		return a.col - b.col;
	},
	
	addAllSections : function(formData){
		var pageView = this;
		var sectionsJSONArrJSONString = formData.sectionsJSONArrJSONString;
		
		if(sectionsJSONArrJSONString != "") {
			EventBus.trigger("close:processing");
			EventBus.trigger("open:processing", "Drawing Sections");
			var sectionsArray = JSON.parse(sectionsJSONArrJSONString);
			sectionsArray.sort(this.sectionSort);
			
			var numSections = sectionsArray.length;
			var j = 0;
			setTimeout(function addSingleSection() {
				try {
					var section = sectionsArray[j];
					var id = section.id;
					var orgRepeatedSectionParent = section.repeatedSectionParent;
					section.isNew = false;
					section.id = "S_"+id;
					section.divId = "S_"+id;
					if(orgRepeatedSectionParent != "-1"){
						section.repeatedSectionParent = "S_"+orgRepeatedSectionParent;				
					}
					FormBuilder.form.addSection(section, false);
					pageView.setRepeatGroupUsedBy(section);
					
					// percent complete.  numQuestions will never be zero here
					pc = (j / numSections) * 100;
					FormBuilder.page.get("processingView").setValueTo(pc);
				}
				catch(e) {
					Log.developer.error(e);
					var message = "A problem occurred loading one of your sections.  I will try to load the others but this form will not be correct ";
					if (Config.devmode) {
						message += e;
					}
					$.ibisMessaging("dialog", "error", message);
				}
				
				j++;
				if (j < numSections) {
					setTimeout(addSingleSection, 0); // timeout loop
				}
				else {
					SectionDragHandler.assignPositions();
					// finished, continue to questions
					pageView.addAllQuestions(formData);
				}
			});
		}
		else {
			// if we didn't load any sections, finalize anyway
			pageView.finalizeLoadingForm();
		}
	},
	
	setRepeatGroupUsedBy : function(section){
		var repeatableGroup = FormBuilder.form.repeatableGroups.byName(section.repeatableGroupName);
		if(typeof repeatableGroup !== "undefined"){
			repeatableGroup.set("usedByotherSection",true);
		}
	},
	
	questionSort : function(a, b) {
		// to make sure sorting is stable here, use the position as defined earlier
		var output = a.position - b.position;
		if (a.sectionId == b.sectionId) {
			if (a.questionOrder != b.questionOrder) {
				// in same section but not same row, order by row
				output = a.questionOrder - b.questionOrder;
			}
			else {
				// same section, same row: order by column
				output = a.questionOrder_col - b.questionOrder_col;
			}
		}
		
		return output;
	},
	
	addAllQuestions : function(formData){
		var pageView = FormBuilder.pageView;
		var questionsJSONArrJSONString = formData.questionsJSONArrJSONString;
		
		if(questionsJSONArrJSONString != "") {
			EventBus.trigger("close:processing");
			EventBus.trigger("open:processing", "Drawing Questions");
			var questionsArray = JSON.parse(questionsJSONArrJSONString); // get all questions -- standard array
			
			// set position for each element to make the sort function stable
			var numQuestions = questionsArray.length;
			for (var i =0; i < numQuestions; i++) {
				questionsArray[i].position = i;
			}
			
			questionsArray.sort(this.questionSort);
			var dataElements = FormBuilder.form.dataElements;
	
			var numQuestions = questionsArray.length;
			
			
			// start add question loop
			var i = 0;
			setTimeout(function addSingleQuestion() {
				try {
					var question = questionsArray[i];
					var newSecId = "S_"+question.sectionId;
					question.sectionId = newSecId;
					var secModle = FormBuilder.form.section(newSecId);
					var a = question.questionType;
					question.questionType = pageView.getQuestionTypeNum(a);
					question.questionId = Number(question.questionId);
					question.hasSavedInDatabase = true;
					question.active = false;
					question.isNew  = false;
					var questionModel = new Question(question);
		
					//need to check whether de that is currently assigned to question is still a valid one from the fs we get from brics...
					//becasue user can remove de from a draft fs
//					var validDE = false;
//					var currDeName = questionModel.get("attributeObject").dataElementName;
//					if(currDeName != 'none') {
//						dataElements.forEach(function(dataElement){
//							var deName = dataElement.get("dataElementName");
//							if(currDeName == deName) {
//								validDE = true;
//								return false;
//							}
//						});
//						//clear the de association
//						if(!validDE) {
//							questionModel.get("attributeObject").dataElementName = "none";
//						}
//					}
		
					pageView.addAttributeIntoQuestion(questionModel);
					questionModel.unset("attributeObject", { silent: true });
					
					questionModel.changeValidation(questionModel);
					questionModel.checkQuestionIdType(questionModel);
					questionModel.changeDecimalPrecision(questionModel);
					questionModel.updateSkipRuleFlag();
					questionModel.updateCalcAttributes();
					questionModel.updateUnitConversionFactorFlag();
					
					secModle.addQuestion(questionModel);
					
					// percent complete.  numQuestions will never be zero here
					pc = (i / numQuestions) * 100;
					FormBuilder.page.get("processingView").setValueTo(pc);
				}
				catch(e) {
					// an error happened during this iteration, eek!
					Log.developer.error(e);
					var message = "A problem occurred loading one of your sections.  I will try to load the others but this form will not be correct";
					if (Config.devmode) {
						message += e;
					}
					$.ibisMessaging("dialog", "error", e + "aaaa" + message);
				}
				i++;
				if (i < numQuestions) {
					setTimeout(addSingleQuestion, 0); // timeout loop
				}
				else {
					// end the loop and finalize
					QuestionDragHandler.assignPositions();
					pageView.finalizeLoadingForm();
				}
			}, 0);
		}
		else {
			// if we didn't load any questions, finalize anyway
			pageView.finalizeLoadingForm();
		}
	},
	
	addAttributeIntoQuestion : function(questionModel){
		var attributeObj = questionModel.get("attributeObject");
		var loadThis = {
			body : attributeObj.body,
			vAlign :attributeObj.vAlign,
			subject : attributeObj.subject,
			hasUnitConversionFactor : attributeObj.hasUnitConversionFactor,
			maxCharacters : attributeObj.maxCharacters,
			prepopulation : attributeObj.prepopulation,
			toEmailAddress : attributeObj.toEmailAddress,
			answerType : attributeObj.answerType,
			questionsToCalculate : attributeObj.questionsToCalculate,
			ccEmailAddress : attributeObj.ccEmailAddress,
			unitConversionFactor : attributeObj.unitConversionFactor,
			dataElementName : attributeObj.dataElementName,
			questionsToSkip : attributeObj.questionsToSkip,
			textareaWidth : attributeObj.textareaWidth,
			calculation : attributeObj.calculation,
			horizontalDisplay : attributeObj.horizontalDisplay,
			//htmlText : attributeObj.htmlText,
			rangeOperator : attributeObj.rangeOperator,
			etId	: attributeObj.etId,
			eMailTriggerId : attributeObj.eMailTriggerId,
			deleteTrigger : attributeObj.deleteTrigger,
			triggerAnswers : attributeObj.triggerAnswers,
			triggerValues: attributeObj.triggerValues,
			required : attributeObj.required,
			answerTypeDisplay : attributeObj.answerTypeDisplay,
			rangeValue2 : attributeObj.rangeValue2,
			rangeValue1 : attributeObj.rangeValue1,
			calDependent : attributeObj.calDependent,
			conversionFactor : attributeObj.conversionFactor,
			eMailTriggerUpdatedBy : attributeObj.eMailTriggerUpdatedBy,
			indent : attributeObj.indent,
			textboxLength : attributeObj.textboxLength,
			skipRuleEquals : attributeObj.skipRuleEquals,
			align : attributeObj.align,
			fontFace : attributeObj.fontFace,
			prepopulationValue : attributeObj.prepopulationValue,
			fontSize : attributeObj.fontSize,
			skipRuleType : attributeObj.skipRuleType,
			decimalPrecision : attributeObj.decimalPrecision,
			qType : attributeObj.questionType,
			skipRuleOperatorType : attributeObj.skipRuleOperatorType,
			calculatedQuestion : attributeObj.calculatedQuestion,
			conditionalForCalc : attributeObj.conditionalForCalc,
			textareaHeight : attributeObj.textareaHeight,
			dataSpring : attributeObj.dataSpring,
			color : attributeObj.color,
			calculationType : attributeObj.calculationType,
			horizDisplayBreak : attributeObj.horizDisplayBreak,
			minCharacters : attributeObj.minCharacters,
			skipRuleDependent : attributeObj.skipRuleDependent,
			skiprule : attributeObj.skiprule,
			tableHeaderType : attributeObj.tableHeaderType,
			showText : attributeObj.showText,
			countFormula : attributeObj.countFormula,
			questionsInCount: attributeObj.questionsInCount,
			countFlag : attributeObj.countFlag
		};
		
//		if(typeof attributeObj.triggerAnswers != 'undefined' && attributeObj.triggerAnswers.length > 0){
		if(typeof attributeObj.triggerValues != 'undefined' && attributeObj.triggerValues.length > 0){
			loadThis.emailTrigger = true;
			questionModel.deleteTrigger = false;
		}
		
		questionModel.set(loadThis);
	},
		
	getQuestionTypeNum : function(type){
		if(type == 'Textbox') {
			return '1';
		}else if(type == 'Textarea') {
			return '2';
		}else if(type == 'Select') {
			return '3';
		}else if(type == 'Radio') {
			return '4';
		}else if(type == 'Multi-Select') {
			return '5';
		}else if(type == 'Checkbox') {
			return '6';
		}else if(type == 'Calculated') {
			return '7';
		}else if(type == 'Patient Calendar') {
			return '8';
		}else if(type == 'Image Map') {
			return '9';
		}else if(type == 'Visual Scale') {
			return '10';
		}else if(type == 'File') {
			return '11';
		}else if(type == 'Textblock') {
			return '12';
		}
	},
	
	//=====
	/**
	 * Changes the active question to the given one.  Also switches currently
	 * active question (if there is one) to inactive.
	 * 
	 * @param question Question Model to make active
	 */
	changeActiveQuestion: function(question) {
		var oldActiveQuestion = this.model.get("activeQuestion");
		
		if (oldActiveQuestion !== null) {
			this.switchQuestionToInactive(oldActiveQuestion);
		}

		if (question !== null) {
			this.switchQuestionToActive(question);
			this.model.set("activeQuestion", question);
			
			var section = FormBuilder.form.getQuestionParentSection(question);
			// just in case that section isn't active, set it active
			if (section !== null && !section.get("active")) {
				EventBus.trigger("change:activeSection", section);
			}
		}
	},
	
	/**
	 * Changes the active section to the given one.  Also switches currently
	 * active section (if there is one) to inactive.
	 * 
	 * @param section Section Model to make active
	 */
	changeActiveSection: function(section) {
		var oldActiveSection = this.model.get("activeSection");
		
		if (section !== null) {
			var oldCid = null;
			var newCid = section.cid;
			
			if (oldActiveSection !== null) {
				oldCid = oldActiveSection.cid;
			}
			
			// if the two sections are the same, don't bother
			if (newCid !== oldCid || oldCid == null) {
				if (oldCid !== null) {
					this.switchSectionToInactive(oldActiveSection);
				}
				
				this.switchSectionToActive(section);
				this.model.set("activeSection", section);
				
				// is there a question in this section already active?
				// if not, make the first question (if it exists) active
				if (section.questions.length > 0) {
					var activeQuestion = section.questions.findWhere({active: true});
					if (typeof activeQuestion == "undefined") {
						var newActiveQuestion = section.questions.at(0);
						newActiveQuestion.set({active: true});
						this.changeActiveQuestion(newActiveQuestion);
					}
				}
			}
		}
	},
	
	/**
	 * Switches the given Section to active.
	 * 
	 * @param section Section to set to active
	 * @returns {Section} the newly activated Section
	 */
	switchSectionToActive : function(section) {
		var active = section.get("active");
		if (!active) {
			section.set({active : true});
		}
		return section;
	},
	
	/**
	 * Switches the given section to inactive.
	 * 
	 * @param section Section to set to inactive
	 * @returns {section} the newly deactivated section
	 */
	switchSectionToInactive : function(section) {
		var active = section.get("active");
		if (active) {
			section.set({active : false});
			var activeQuestion = section.questions.findWhere({active: true});
			if (typeof activeQuestion !== "undefined") {
				activeQuestion.set({active: false});
			}
		}
		return section;
	},
	
	/**
	 * Switches the given Question to active.
	 * 
	 * @param question Question to set to active
	 * @returns {Question} the newly activated Question
	 */
	switchQuestionToActive : function(question) {
		var active = question.get("active");
		if (!active) {
			question.set({active : true});
		}
		return question;
	},
	
	/**
	 * Switches the given question to inactive.
	 * 
	 * @param question Question to set to inactive
	 * @returns {question} the newly deactivated question
	 */
	switchQuestionToInactive : function(question) {
		var active = question.get("active");
		if (active) {
			question.set({active : false});
		}
		return question;
	},
	
	openSectionEditor : function(section) {
		if (!this.model.get("loadingData")) {
			var sectionDiv = $('#' + FormBuilder.page.activeSectionId());
			
			var sectionEditView = this.model.get("sectionEditView");
			sectionEditView.render(section);
			this.setActiveEditor(sectionEditView);
			this.scrollPageTo(sectionDiv);
		}
	},
	
	/**
	 * Sets the active editor to the given EditorView.
	 * This destroys the previous active editor.
	 * 
	 * @param view the view to make the active editor
	 */
	setActiveEditor : function(view) {
		var activeEditor = this.model.get("activeEditorView");
		if (activeEditor !== null && activeEditor != view) {
			this.destroyActiveEditor();
		}
		this.model.set("activeEditorView", view, {silent: true});
	},
	
	/**
	 * Destroys the current active editor both on the page and in the Page
	 * model.
	 */
	destroyActiveEditor : function() {
		var view = this.model.get("activeEditorView");
		if (view !== null) {
			try {
				view.close();
				this.model.set("activeEditorView", null);
			}
			catch(e) {
				Log.developer.error(e);
				// validation failed or other error occurred.
				// make no changes
			}
		}
	},
	
	openFormEditor : function() {
		if (!this.model.get("loadingData")) {
			var view = this.model.get("formEditView");
			if (view !== null) {
				view.render();
				this.setActiveEditor(view);
			}
			else {
				Log.user.error("The Form Editor could not be opened");
			}
		}
	},
	
	openQuestionEditor : function(question) {
		if (!this.model.get("loadingData")) {
			var questionDiv = $('#' + FormBuilder.page.activeQuestionId());
			
			var questionEditView = this.chooseQuestionEditor(question);
			questionEditView.render(question);
			this.setActiveEditor(questionEditView);
			this.scrollPageTo(questionDiv);
		}
	},
	
	scrollPageTo : function($div) {
		var o = $div.offset().top;
		var h = $div.outerHeight();
		var e = $(".formBuilder_editor:visible").outerHeight();
		var w = $(window).height();
		var b = $("#actionBar").height();
		/*$(window).scrollTo($div.offset().top - $("#actionBar").height());*/
		var offsetTo = (o+h+e)-w;
		if(offsetTo < 0 ){
			offsetTo = 0;
		}
		
		$(window).scrollTo(offsetTo);
	},
	
	/**
	 * Decides which editor to open for the given question.  Does NOT
	 * actually open that editor, it just chooses which one to open.
	 * 
	 * @param question Question Model to check for
	 */
	chooseQuestionEditor : function(question) {
		var type = question.get("questionType");
		var questionTypes = Config.questionTypes;
		
		switch(type) {
			case questionTypes.textblock:
				return this.model.get("textBlockEditView");
				break;
			case questionTypes.textbox:
				return this.model.get("textboxEditView");
				break;
			case questionTypes.textarea:
				return this.model.get("textareaEditView");
				break;
			case questionTypes.radio:
				return this.model.get("radioEditView");
				break;
			case questionTypes.checkbox:
				return this.model.get("checkboxEditView");
				break;
			case questionTypes.select:
				return this.model.get("selectEditView");
				break;
			case questionTypes.multiSelect:
				return this.model.get("multiSelectEditView");
				break;
			case questionTypes.imageMap:
				return this.model.get("imageMapEditView");
				break;
			case questionTypes.fileUpload:
				return this.model.get("fileUploadEditView");
				break;
			case questionTypes.visualscale:
				return this.model.get("visualScaleEditView");
				break;
			default:
				throw new Error("The question type " + type + " has not been created");
		}
	},
	
	openQuestionLibrary : function() {
		var view = this.model.get("questionLibraryView");
		
		if ( (view !== null) && (typeof view != "undefined") ) {
			// Show the processing dialog box.
			EventBus.trigger("open:processing", Config.language.loadQuestLib);
			
			view.render();
			this.setActiveEditor(view);
		}
		else {
			Log.user.error("The question library could not be opened.");
		}
	},
	
	addQuestionFromLibrary : function(question) {
		// Validate the question param
		if ( (question === null) || (typeof question === "undefined") ) {
			question = this.model.get("questionLibraryView").model.get("chosenQuestion");
		}
		
		// Add question to the active section
		try {
			var activeSection = this.model.get("activeSection");
			
			if ( activeSection != null ) {
				var activeSectionModel = FormBuilder.form.getSectionByDivId(activeSection.get("divId"));
				var newQuestion = activeSectionModel.addQuestion(question);
				EventBus.trigger("add:question", newQuestion);
				
				if(FormBuilder.form.section(newQuestion.get("sectionId")).isRepeatableParent()){
					//create the repeatable child questions
					EventBus.trigger("create:repeatableQuestions", newQuestion);
					
				}
				
			}
			else {
				$.ibisMessaging("dialog", "error", "The active section could not be found.", {container: "body"});
			}
		}
		catch ( err ) {
			var errMessage = "Could not add a question to the active section because:<br/><br/>" + err.message;
			$.ibisMessaging("dialog", "error", errMessage, {container: "body"});
		}
	},
	
	
	openFormStructure : function() {
		var view = this.model.get("formStructureView");
		
		if ( view !== null ) {
			view.render();
		}
		else {
			Log.user.error("The question library could not be opened.");
		}
	},
	
	openLayoutEditor : function(question) {
		var view = this.model.get("layoutEditView");
		view.render(question);
		this.setActiveEditor(view);
	},
	
	openTableEditor : function(section) {
		var view = this.model.get("tableLayoutEditView");
		view.render(section);
		this.setActiveEditor(view);
	},
	
	/**
	 * Changes the given question's type.  This only exists here because it
	 * can't happen from inside the question model (well maybe model) or view
	 * because it is destroyed during processing. 
	 */
	changeQuestionType : function(question) {
		//get previous view
		//remove the previous view
		//EventBus.trigger("close:activeEditor");
		var currentSectionModel = FormBuilder.form.getQuestionParentSection(question);
		
		question.calculateDivId(true);
		// set placeholder
		var $question = $("#" + question.get("newQuestionDivId"));
		$question.after('<div id="replacement_placeholder" style="display:none"></div>');
		var $placeholder = $("#replacement_placeholder");		
		question.set("renderAfter", $placeholder);
		
        //set visual scale min and max		
		var type = question.get("questionType");
		var questionTypes = Config.questionTypes; 

        if (type === questionTypes.visualscale) {
	      if (typeof(question.get("rangeValue1"))!='undefined' && typeof(question.get("rangeValue2"))!='undefined') {
	    	var operatorType = question.get("rangeOperator");		    	
	    	if(operatorType == Config.rangeOperator.isEqualTo){
	    		question.set("vscaleRangeStart", String(question.get("rangeValue1")));
	    		question.set("vscaleRangeEnd",   String(question.get("rangeValue1")));
	    	}
	    	else if(operatorType == Config.rangeOperator.lessThan){
	    		if(question.get("vscaleRangeStart") == "-99999") {
	    			question.set("vscaleRangeStart", "0");
	    		}
	    		question.set("vscaleRangeEnd",   String(question.get("rangeValue1")));
	    	}
	    	else if(operatorType == Config.rangeOperator.greaterThan){
	    		question.set("vscaleRangeStart", String(question.get("rangeValue1")));
	    		if(question.get("vscaleRangeEnd") == "-99998") {
	    			question.set("vscaleRangeEnd", "5000");
	    		}
	    	}
	    	else if(operatorType == Config.rangeOperator.between){
	    		question.set("vscaleRangeStart", String(question.get("rangeValue1")));
	    		question.set("vscaleRangeEnd",   String(question.get("rangeValue2")));
	    	}else{
	    		if(question.get("vscaleRangeStart") == "-99999" && question.get("vscaleRangeEnd") == "-99998") {
	    			question.set("vscaleRangeStart", "1");
	    			question.set("vscaleRangeEnd",   "5000");
	    		}
	  		}                 
	      }
	    }
		
        if (this.model.get("activeEditorView") != null) {
        
	        var changeSet = StateManager.changeSet;
	        this.model.get("activeEditorView").close();
	        StateManager.onEditorClose();
	
			// because we can have multiple questions with the same name on the form and we don't
	        // want to open the editor for each of them, we have to check here.
	        if (question.cid == FormBuilder.page.get("activeQuestion").cid) {
		        EventBus.trigger("open:questionEditor", question);
		        StateManager.onEditorOpen(question, false);
				
				// copies the "carryover" changes from the first editor to the second
				StateManager.changeSet = changeSet;
	        }
        }
			
		//call function to re-render the view
		this.reRenderQuestion(question);
        
	},
	
	/**
	 * Re-renders the given question.  This will likely remove the current
	 * question view and replace it with a new one.
	 */
	reRenderQuestion : function(question) {
		EventBus.trigger("reRender:question", question);
	},
	
	/**
	 * Loops over all sections/questions and assigns them a row and column value
	 * based on their position on the page (or derived position in the case of
	 * repeatable children).
	 * 
	 * Note: for questions, the row/col is always relative to the parent section
	 */
	setRowsCols : function() {
		var sectionRow = 1;
		var sectionCol = 1;
		var sections = $("." + Config.identifiers.section);
		for (var i = 0; i < sections.length; i++) {
			var $thisSect = sections.eq(i);
			var $previousSection = $thisSect.prev(".section");
			var sectionModel = FormBuilder.form.getSectionByDivId($thisSect.attr("id"));
			
			if (sectionModel.get("isRepeatable") === true) {
				// take care of this section first: repeatable sections are always on their own row
				if ($previousSection.length < 1) {
					sectionModel.set({row : 1, col : 1});
				}
				else {
					sectionRow++;
					sectionCol = 1;
					sectionModel.set({row : sectionRow, col : sectionCol});
				}
				
				// now the kids (models)
				var repeatableChildren = FormBuilder.form.sections.getRepeatableChildren(sectionModel);
				for (var k = 0; k < repeatableChildren.length; k++) {
					sectionRow++;
					repeatableChildren[k].set({row : sectionRow, col : sectionCol});
				}
			}
			else {
				if ($previousSection.length < 1) {
					sectionModel.set({row : 1, col : 1});
				} else {
					// if this is not a repeatable section, process like normal
					if (SectionDragHandler.areSectionsOnSameRow($previousSection, $thisSect)) {
						// this element is on the same row as the previous element
						sectionCol++;
						// don't change row number
					}
					else {
						// this element is on a different row from the previous element
						sectionRow++;
						sectionCol = 1;
					}
					
					sectionModel.set({row : sectionRow, col : sectionCol});
				}
			}
			
			// now for the questions.  The great thing about questions is that any
			// change to a repeatable parent section's questions can be propagated
			// to its children.  In this case, the row/col settings are copied by
			// RepeatableSectionProcessor.editQuestion(questionModel);
			var questionRow = 1;
			var questionCol = 1;
			var sectionQuestions = sectionModel.getQuestionsInPageOrder();
			for (var j = 0; j < sectionQuestions.length; j++) {
				var question = sectionQuestions[j];
				var $question = $("#" + question.get("newQuestionDivId"));
				if (j == 0) {
					question.set({questionOrder : 1, questionOrder_col : 1});
				}
				else {
					var $previousQuestion = $("#" + sectionQuestions[j-1].get("newQuestionDivId"));
					if (QuestionDragHandler.areQuestionsOnSameRow($question, $previousQuestion)) {
						questionCol++;
					}
					else {
						questionRow++;
						questionCol = 1;
					}
					question.set({questionOrder : questionRow, questionOrder_col : questionCol});
				}
			}
			
			
		}
	}
});