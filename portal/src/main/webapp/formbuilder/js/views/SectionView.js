/**
 * 
 */
var SectionView  = BaseView.extend({
	className : "section formGrid-1",
	questionsViews : [],
	migrateQuestionElements : null,
	
	events : {
		"click" : "selectionClickHandler",
		"click .sectionHeader .editButton" : "edit",
		"click .sectionHeader .deleteButton" : "deleteSection",
		"click .sectionExpandCollapse" : "expandCollapse",
		"mouseenter .sectionRepeatable" : "showRepeatTooltip",
		"mouseout .sectionRepeatable" : "hideRepeatTooltip",
		"mouseenter .sectionCollapsible" : "showCollapsibleTooltip",
		"mouseout .sectionCollapsible" : "hideCollapsibleTooltip",
		"mouseenter .sectionGridType" : "showGridtypeTooltip",
		"mouseout .sectionGridType" : "hideGridtypeTooltip"
	},
	
	initialize : function() {
		this.id = this.model.get("divId");
		SectionView.__super__.initialize.call(this);
		// in initialize, we are creating the new section, so it should be active
		this.template = TemplateManager.getTemplate("sectionTemplate");
		this.listenTo(this.model, "change:active", this.afterChangeActive);
		this.listenTo(this.model, "change:isRepeatable", this.afterChangeRepeatable);
		this.listenTo(this.model, "change:isCollapsable", this.afterChangeCollapsible);
		this.listenTo(this.model, "change:editorCollapsed", this.onExpandCollapse);
		this.listenTo(this.model, "change:gridtype", this.onChangeGridtype);
		
		this.listenTo(this.model.questions, "add", this.createQuestion);
		this.listenTo(this.model, "remove", this.destroy);
		
		EventBus.on("delete:question", this.deleteQuestion, this);
		EventBus.on("reRender:question", this.reRenderQuestion, this);
		EventBus.on("change:section", this.enableDisableResetTable, this);
		// event to tell the rest of the page about this new activesection
		// is in formView when the section is actually added
		return this;
	},
	
	render : function($after) {
		this.$el.attr("id", this.model.get("divId"));
		
		// if this section is repeatable and NOT a parent, don't render it
		if (this.model.get("isRepeatable") && !this.model.isRepeatableParent()) {
			return this;
		}
		
		// in case we're re-rendering because of switching from active to inactive
		// or the reverse, check the template to make sure it's correct
		
		// question migration is only needed if we're re-rendering
		this.startQuestionMigrate();
		this.$el.html(this.template(this.model.attributes));
		this.afterChangeActive();
		if (!this.$el.is(":visible")) {
			// if $after is the form itself, prepend it to the form
			if (typeof $after !== "undefined" && $after.length > 0 && $after.hasClass(Config.identifiers.formContainer)) {
				$after.prepend(this.$el);
			}
			else if (typeof $after !== "undefined" && $after.length > 0) {
				$after.after(this.$el);
			}
			else {
				Config.containerDiv.find("#formContainerClearfix").before(this.$el);
			}
			// wasn't visible so couldn't be column. Set width
			this.$el.addClass("formGrid-1");
//			this.$el.width(Config.containerDiv.width() - 8);
		}
		// in case we have question, etc. already inside, re-add them
		this.finishQuestionMigrate();
		this.setupTooltips();
		SectionView.__super__.render.call(this);
		// if adding a new section,
		// event to tell the rest of the page about this new activesection
		// is in formView when the section is actually added
		this.setupSortable();
		this.needDisableDelete();
		this.assignHeaderClass();
		return this;
	},
	
	
	assignHeaderClass : function() {
		var tableHeaderType = this.model.get("tableHeaderType");
		var tableGroupId = this.model.get("tableGroupId");
		if(tableHeaderType == Config.tableHeaderTypes.columnHeader) {
			this.$el.addClass(Config.tableHeaderClassNames.columnHeader);
		}else if(tableHeaderType == Config.tableHeaderTypes.tableHeader) {
			this.$el.addClass(Config.tableHeaderClassNames.tableHeader);
		}else if(tableHeaderType == 0 && tableGroupId > 0) {
			this.$el.addClass(Config.tableHeaderClassNames.tablePrimary);
			
		}
	},
	
	/**
	 * Determines if this section needs to be deletable.
	 * 
	 * As of right now (3/2015) the requirements for deleting a section are:
	 * * If the section contains NO required questions, the section can be deleted
	 * * otherwise, it cannot
	 * 
	 */
	needDisableDelete : function() {
		var model = this.model;
		var requiredQuestions = model.questions.where({required: true});
		if (typeof requiredQuestions !== "undefined" && requiredQuestions.length > 0) {
			this.disableDelete();
			return true;
		}
		else {
			this.enableDelete();
			return false;
		}
	},
	
	disableDelete : function() {
		var $button = this.$(".deleteButton").first();
		if (!$button.hasClass("disabled")) {
			$button.addClass("disabled");
		}
	},
	
	enableDelete : function() {
		var $button = this.$(".deleteButton").first();
		if ($button.hasClass("disabled")) {
			$button.removeClass("disabled");
		}
	},
	
	setupSortable : function() {
		SectionDragHandler.refresh();
		QuestionDragHandler.refresh();
	},
	
	destroySortable : function() {
		this.$el.sortable("destroy");
	},
	
	isActive : function() {
		return this.model.get("active");
	},
	
	/**
	 * Click section handler
	 * 
	 * Triggers the global change active section activity
	 */
	selectionClickHandler : function() {
		if (!this.isActive()) {
			EventBus.trigger("change:activeSection", this.model);
		}
		EventBus.trigger("resize:section", this.model);
	},
	
	/**
	 * Sets the class of this.$el to active or inactive, chosen by the
	 * model's "active" attribute
	 */
	afterChangeActive : function() {
		if (this.isActive()) {
			this.setActive();
		}
		else {
			this.setInactive();
		}
	},
	
	/**
	 * Sets this section to active - including showing edit
	 * and delete buttons
	 */
	setActive : function() {
		this.$el.removeClass(Config.styles.inactive);
		this.$el.addClass(Config.styles.active);
		// for table and column header types, the section header is hidden
		this.showHideHeader();
		this.$(".sectionHeader .editButton").show();
		this.$(".sectionHeader .deleteButton").show();
	},
	
	/**
	 * Sets this section to inactive - including hiding edit
	 * and delete buttons.
	 */
	setInactive : function() {
		this.$el.removeClass(Config.styles.active);
		this.$el.addClass(Config.styles.inactive);
		// for table and column header types, the section header is hidden
		this.showHideHeader();
		this.$(".sectionHeader .editButton").hide();
		this.$(".sectionHeader .deleteButton").hide();
	},
	
	/**
	 * Responds to a change in the repeatable property.  Toggles the
	 * indicator.
	 */
	afterChangeRepeatable : function(model, changeval) {
		if (changeval) {
			this.$(".sectionRepeatable").removeClass("off");
		}
		else {
			this.$(".sectionRepeatable").addClass("off");
		}
	},
	
	/**
	 * Responds to a change in the collapsible property.  Toggles the
	 * indicator.
	 */
	afterChangeCollapsible : function(model, changeval) {
		if (changeval) {
			this.$(".sectionCollapsible").removeClass("off");
		}
		else {
			this.$(".sectionCollapsible").addClass("off");
		}
	},
	
	/**
	 * Gets a representation of this class as needed for migrating to 
	 * active or inactive.
	 * 
	 * @returns Object containing data necessary to migrate this view
	 */
	startQuestionMigrate : function() {
		this.migrateQuestionElements = this.$(".questionContainer");
	},
	
	/**
	 * Does any post-processing necessary to handle migrating FROM a previous
	 * Section rendering.
	 * 
	 * ONLY NEEDED DURING A RE-RENDER
	 */
	finishQuestionMigrate : function() {
		if (this.migrateQuestionElements.length > 0) {
			if (this.migrateQuestionElements.length > 0) {
				// It SHOULD, but we need to check that it doesn't lose
				// those event bindings
				// a jsfiddle suggests that it will be fine but no laziness here!
				//this.$(".questionContainer").append(this.migrateQuestionElements);
				// it should be noted, as well, that a replacewith is MUCH faster than
				// re-rendering each question because we only add them once
				this.$(".questionContainer").replaceWith(this.migrateQuestionElements);
			}
			this.migrateQuestionElements = null;
		}
	},
	
	/**
	 * Sets up the tooltips for future showing/hiding
	 */
	setupTooltips : function() {
		this.$(".statusTooltip").each(function() {
			var link = $(this).prevAll("a").first();
			var linkPosition = link.position();
			$(this).css({
				"top": linkPosition.top + link.height(),
				"left": linkPosition.left + link.height()
			});
		});
	},
	
	edit : function() {
		EventBus.trigger("open:sectionEditor", this.model);
	},
	
	/**
	 * Starts the delete section process.
	 * 
	 * @param event the click event
	 */
	deleteSection : function(event) {
		// this might not be obvious but, when deleting a section,
		// we move the active section to the previous (or next)
		// section, so we have to make sure a click event doesn't
		// get called on this section after that runs - causing THIS
		// section (now invisible) to become the active one
		event.stopImmediatePropagation();	
	
				
		//do not trigger delete:section if any question in repeatables have skip or calc dependent.   normal sections are handled elsewhere
		if(this.model.get("isRepeatable")) {
			var canDelete = true;
			this.model.questions.forEach(function(question) {
				if(question.get("skipRuleDependent")){
					$.ibisMessaging("dialog", "error", Config.language.hasSkip);
					canDelete = false;
					return;
				}else if(question.get("calDependent")){
					$.ibisMessaging("dialog", "error", Config.language.hasCal);
					canDelete = false;
					return;
				}
			});
			if(!canDelete) {
				return;
			}
			var childSections = RepeatableSectionProcessor.getRepeatableChildren(this.model);
			for (var m = 0; m < childSections.length; m++) {
				var childSection = childSections[m];
				childSection.questions.forEach(function(question) {
					if(question.get("skipRuleDependent")){
						$.ibisMessaging("dialog", "error", Config.language.hasSkip);
						canDelete = false;
						return;
					}else if(question.get("calDependent")){
						$.ibisMessaging("dialog", "error", Config.language.hasCal);
						canDelete = false;
						return;
					}
					
				});
			}
			if(!canDelete) {
				return;
			}
		}

		
		// @see this.needDisableDelete() for these rules
		if (!this.needDisableDelete()) {
			EventBus.trigger("delete:section",this.model);
		}
		//this.destroy();
	},
	
	createQuestion : function(question) {
		// decide question type, initialize that
		if(!this.model.isRepeatableChild()){
			var view = this.questionFactory(question);
			var renderAfter = question.get("renderAfter") || undefined;
			if (this.model.questions.length > 1) { // we already added one
				if (renderAfter != null) {
					view.render(renderAfter);
				}
				else if (question.get("autoCreated")) {
					// if autocreated, ignore any active anything and add it
					// to the end of this section
					view.render(this.$("." + Config.identifiers.question).last());
				}
				else {
					view.render($("." + Config.identifiers.question+"."+Config.styles.active));
				}
			}
			else {
				view.render(renderAfter);
			}
			EventBus.trigger("change:activeQuestion", question);
			this.needDisableDelete();
			
			if (question.get("isNew")) {
				EventBus.trigger("create:question", question);
				if (!question.get("disableEditor")) {
					EventBus.trigger("open:questionEditor", question);
				}
				else {
					question.set("disableEditor", false);
				}
			}
			
			if (!FormBuilder.page.get("loadingData")) {
				EventBus.trigger("resize:section", this.model);
			}
		}
	},
	
	/**
	 * Responds to the delete:question event to actually remove the question
	 * from this section.
	 * 
	 * @param question the question model to remove
	 */
	deleteQuestion : function(question) {
		// does the question exist in this section?
		if (this.model.questions.contains(question)) {
			if(question.get("skipRuleDependent")){
				$.ibisMessaging("dialog", "error", Config.language.deleteSkipQuestion);
			}else if(question.get("calDependent")){
				$.ibisMessaging("dialog", "error", Config.language.deleteCalQuestion);
			}else{
				var newActive = this.chooseNewActiveQuestion(question);
				this.model.removeQuestion(question);
				FormBuilder.form.setSkipRuleDependent();
				FormBuilder.form.setCalDependent();
				EventBus.trigger("change:activeQuestion", newActive);
				EventBus.trigger("resize:section", this.model);
				this.needDisableDelete();
			}
		}
	},
	
	/**
	 * Handles re-rendering the question.  This function should not
	 * remove the question rendering on the page (questionView does that)
	 * but it SHOULD render the new questionView.
	 */
	reRenderQuestion : function(question) {
		// does the question exist in this section?
		if (this.model.questions.contains(question)) {
			// get new question view from questionFactory
			var newQuestionView = this.questionFactory(question);
			// get div ID of question
			var divId = question.get("newQuestionDivId");
			newQuestionView.setElement($("#" + divId));
			// render question in correct location
			newQuestionView.render();
			// just in case this is still floating around - from pageview's type switch
			$("#replacement_placeholder").remove();
		}
	},
	
	/**
	 * Generate the new Question View object based on the given Question
	 * Model.
	 * 
	 * @param question Question Model object
	 * @returns QuestionView subtype for the given model
	 */
	questionFactory : function(question) {
		var typeName = question.get("questionType");
		var questionTypes = Config.questionTypes;
		
		switch(typeName) {
		case questionTypes.textblock:
			return new TextBlockView({model: question});
			break;
		case questionTypes.textbox:
			return new TextboxView({model: question});
			break;
		case questionTypes.textarea:
			return new TextareaView({model: question});
			break;
		case questionTypes.radio:
			return new RadioView({model: question});
			break;
		case questionTypes.checkbox:
			return new CheckboxView({model: question});
			break;
		case questionTypes.select:
			return new SelectView({model: question});
			break;
		case questionTypes.multiSelect:
			return new MultiSelectView({model: question});
			break;
		case questionTypes.imageMap:
			return new ImageMapView({model: question});
			break;
		case questionTypes.fileUpload:
			return new FileUploadView({model: question});
			break;
		case questionTypes.visualscale:
			return new VisualScaleView({model: question});
			break;
		default:
			throw new Error("The question type " + typeName + " has not been created");
		}
	},
	
	/**
	 * Handles the click of the expand/collapse icon in the section header.
	 * Collapses or Expands the question container div so that the section
	 * appears as only a section header.
	 */
	expandCollapse : function() {
		if (this.model.get("editorCollapsed")) {
			this.model.set("editorCollapsed", false);
		}
		else {
			this.model.set("editorCollapsed", true);
		}
	},
	
	/**
	 * Responds to a change in the model's "editorCollapsed" attribute change
	 * to decide whether to expand or collapse the section.
	 * 
	 * I am doing it this way so that, if we collapse one section in a row, we
	 * can tell the other models in that row to collapse as well - and vice
	 * versa.
	 */
	onExpandCollapse : function() {
		if (this.model.get("editorCollapsed")) {
			this.collapse();
		}
		else {
			this.expand();
		}
	},
	
	/**
	 * Responds to a change in the model's "gridtype" attribute.  Toggles the
	 * indicator.
	 */
	onChangeGridtype : function() {
		if (this.model.get("gridtype")) {
			this.$(".sectionGridType").removeClass("off");
		}
		else {
			this.$(".sectionGridType").addClass("off");
		}
		this.showHideHeader();
	},
	
	/**
	 * expands this section, showing all questions
	 */
	expand : function() {
		this.$(".questionContainer").show();
		this.$(".sectionExpandCollapse").removeClass("collapsed");
		EventBus.trigger("expand:section", this.model);
	},
	
	/**
	 * Collapses this section, hiding all questions
	 */
	collapse : function() {
		this.$(".questionContainer").hide();
		this.$(".sectionExpandCollapse").addClass("collapsed");
		EventBus.trigger("collapse:section", this.model);
	},
	
	showRepeatTooltip : function() {
		if (this.model.get("isRepeatable")) {
			this.$(".sectionRepeatable").next(".statusTooltip").show();
		}
	},
	
	hideRepeatTooltip : function() {
		this.$(".sectionRepeatable").next(".statusTooltip").hide();
	},
	
	showCollapsibleTooltip : function() {
		if (this.model.get("isCollapsable")) {
			this.$(".sectionCollapsible").next(".statusTooltip").show();
		}
	},
	
	hideCollapsibleTooltip : function() {
		this.$(".sectionCollapsible").next(".statusTooltip").hide();
	},
	
	showGridtypeTooltip : function() {
		if (this.model.get("gridtype")) {
			this.$(".sectionGridType").next(".statusTooltip").show();
		}
	},
	
	hideGridtypeTooltip : function() {
		this.$(".sectionGridType").next(".statusTooltip").hide();
	},
	
	showHideHeader : function() {
		// only bother if this is a table section
		if (!this.isActive() && this.model.get("gridtype")) {
			this.$(".sectionHeader").hide();
		}
		else {
			this.$(".sectionHeader").show();
		}
		
		if (!this.isActive()) {
			SectionDragHandler.enforceHeightSection(this.$el);
		}
	},
	
	/**
	 * Chooses which question (if any) should be the new active question after
	 * removing the "current" active question.
	 * 
	 * @param question the question being removed
	 * @return Question Model for the previous question (if any)
	 */
	chooseNewActiveQuestion : function(question) {
		var activeQuestion = $(".question." + Config.styles.active);
		// instead of ALL visible questions, let's work only inside this section
		//var allVisibleQuestions = $(".question");
		var allVisibleQuestions = this.$("." + Config.identifiers.question);
		var indexInAll = allVisibleQuestions.index(activeQuestion);
		// not found!  Find the last one and make it active
		if (indexInAll == -1) {
			var newActiveDivId = allVisibleQuestions.not(activeQuestion).last().attr("id");
			// get the question's model
			return FormBuilder.form.getQuestionByDivId(newActiveDivId);
		}
		else if (indexInAll == 0) {
			// the first one.  Are there more?
			if (allVisibleQuestions.length > 1) {
				// there are, choose the NEXT one
				var newActiveDivId = allVisibleQuestions.eq(1).attr("id");
				return FormBuilder.form.getQuestionByDivId(newActiveDivId);
			}
			else {
				return null;
			}
		}
		else {
			// otherwise, there exists a question before this one.
			var newActiveDivId = allVisibleQuestions.eq(indexInAll -1).attr("id");
			return FormBuilder.form.getQuestionByDivId(newActiveDivId);
		}
	}
});