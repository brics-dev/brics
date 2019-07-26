/**
 * 
 */
var LayoutEditView = EditorView.extend({
	dialogTitle : "Edit Layout",
	templateName : "editLayoutTemplate",
	dialogConfig : {modal : false},
	
	isJoinRowUp : false,
	isJoinRowDown : false,
	isLeaveRowUp : false,
	isLeaveRowDown : false,
	isReorderUp : false,
	isReorderDown : false,
	
	currentTab : "question",
	
	buttons : [{
		text: "Close",
		"class" : "ui-priority-primary",
		click : _.debounce(function() {
			EventBus.trigger("close:activeEditor");
		})
	}],
	validationRules : [],
	
	initialize : function() {
		SectionEditView.__super__.initialize.call(this);
		this.template = TemplateManager.getTemplate(this.templateName);
	},
	
	events : {
		"click .layoutEditorButton.joinRowUp" : "joinRowUp",
		"click .layoutEditorButton.leaveRowDown" : "leaveRowDown",
		"click .layoutEditorButton.joinRowDown" : "joinRowDown",
		"click .layoutEditorButton.leaveRowUp" : "leaveRowUp",
		"click .layoutEditorButton.reorderUp" : "reorderUp",
		"click .layoutEditorButton.reorderDown" : "reorderDown"
	},
	
	render : function(model) {
		this.currentTab = "question";
		this.changeLocalModel(model);
		
		// sections change when question changes but question changes when
		// section does as well.
		EventBus.on("change:activeQuestion", this.changeLocalModel, this);
		EventBus.on("change:activeSection", this.changeLocalModel, this);
		
		this.$el.html(this.template());
		LayoutEditView.__super__.render.call(this, model);
		
		this.initButtons();
		
		return this;
	},
	
	close : function() {
		EventBus.off("change:activeQuestion", this.changeLocalModel, this);
		EventBus.off("change:activeSection", this.changeLocalModel, this);
		LayoutEditView.__super__.close.call(this);
	},
	
	destroy : function() {
		LayoutEditView.__super__.destroy.call(this);
	},
	
	initButtons : function() {
		this.changeObject(this.model);
	},
	
	enableDisableButton : function(buttonName, enable) {
		if (enable) {
			this.$(".layoutEditorButton." + buttonName).removeClass("disabled");
		}
		else {
			this.$(".layoutEditorButton." + buttonName).addClass("disabled");
		}
	},
	
	/**
	 * Responds to changing tabs in the layout editor from question to section
	 * or the other way around.  Switches which model we control.
	 */
	onActivateTab : function(event, ui) {
		if (ui.newPanel.is($("#questionLayout"))) {
			this.currentTab = "question";
		}
		else {
			this.currentTab = "section";
		}
		
		var activeModel = FormBuilder.page.get("activeQuestion");
		// if the active QUESTION is not inside the active SECTION then we have switched
		// to a section that has no questions.  This reference should be the active
		// section, not the active question
		if (activeModel.cid != FormBuilder.form.getQuestionParentSection(activeModel).cid) {
			activeModel = FormBuilder.page.get("activeSection");
		}
		
		this.changeLocalModel(activeModel);
	},
	
	/**
	 * Change the local model to the one passed or, if we are in the section
	 * tab (or changing to it), the section parent of that question model.
	 * 
	 * @param model Question model to change to
	 */
	changeLocalModel : function(model) {
		model = this.getCorrectModelReference(model);
		this.model = model;
		this.changeObject(model);
	},
	
	/**
	 * Change up the layout option buttons where needed
	 * 
	 * called on render, on tab change, and on layout change
	 * 
	 * @param Model Question or Section model to update
	 */
	changeObject : function(model) {
		this.canJoinRowUp(model);
		this.canJoinRowDown(model);
		this.canLeaveRowDown(model);
		this.canLeaveRowUp(model);
		this.canReorderUp(model);
		this.canReorderDown(model);
	},
	
	/**
	 * Decides what model we should actually be using based on tab and active
	 * question.
	 * 
	 * There is a multi-part decision here:
	 * 		If the question tab is active, the model should be a question
	 * 		If the section tab is active, the model should be a section
	 * 		
	 * This is initiated both by a change in tab and a change in active question
	 * 
	 * @param model Question model to get the reference for
	 * @return Model Question Model or Section Model to use for this class
	 */
	getCorrectModelReference : function(model) {
		if (model instanceof Question && this.currentTab == "question") {
			return model;
		} 
		else if (model instanceof Question && this.currentTab == "section") {
			return FormBuilder.form.getQuestionParentSection(model);
			//return FormBuilder.page.get("activeSection");  //TODO
		}
		else if (model instanceof Section && this.currentTab == "question") {
			return FormBuilder.page.get("activeQuestion");
		}
		else {
			return model;
		}
	},
	
	getDragHandler : function(model) {
		return (model instanceof Question) ? QuestionDragHandler : SectionDragHandler;
	},
	
	isLastInRow : function(model) {
		var canMove = false;
		var dragHandler = this.getDragHandler(model);
		var $object = dragHandler.getDiv(model);
		var index = dragHandler.getPositionInRow($object);
		var rowLength = dragHandler.getCountInCurrentRow($object);
		
		if (index == (rowLength - 1)) {
			canMove = true;
		}
		
		return canMove;
	},
	
	isFirstInRow : function(model) {
		var canMove = false;
		var dragHandler = this.getDragHandler(model);
		var index = dragHandler.getPositionInRow(model);
		
		if (index === 0) {
			canMove = true;
		}
		
		return canMove;
	},
	
	hasPreviousRow : function(model) {
		var dragHandler = this.getDragHandler(model);
		var countPrevious = dragHandler.getCountInPreviousRow(model);
		return countPrevious != 0;
	},
	
	hasNextRow : function(model) {
		var dragHandler = this.getDragHandler(model);
		var countNext = dragHandler.getCountInNextRow(model);
		return countNext != 0;
	},
	
	hasPrevious : function(model) {
		var dragHandler = this.getDragHandler(model);
		var $previous = dragHandler.getPreviousNElements(model, 1);
		return $previous != null;
	},
	
	hasNext : function(model) {
		var dragHandler = this.getDragHandler(model);
		var $previous = dragHandler.getNextNElements(model, 1);
		return $previous != null;
	},
	
	isAloneInRow : function(model) {
		var dragHandler = this.getDragHandler(model);
		return dragHandler.getCountInCurrentRow(model) == 1;
	},
	
	isTextbox : function(model) {
		var canMove = false;
		if (model instanceof Question && model.get("questionType") == 12) {
			canMove = true;
		}
		return canMove;
	},
	
	isTableSection : function(model) {
		return model.get("tableGroupId") != 0;
	},
	
	/**
	 * Checks if the current model can join the row up.
	 * Also marks the button in the interface appropriately
	 * 
	 * An object can only join row up if it is the first (index 0) in
	 * its current row.
	 * 
	 * @param model the model (Question or Section) to check
	 */
	canJoinRowUp : function(model) {
		var canMove = false;
		// repeatable sections cannot join row up
		if (model instanceof Section) {
//			if (this.isTableSection(model)) {
//				canMove = false;
//			}
			if (model.get("isRepeatable")) {
				canMove = false;
			}
			else {
				// we also don't want non-repeatable sections to join repeatable ones - that would have the same effect
				// of not having repeatable sections on their own row
				var dragHandler = this.getDragHandler(model);
				var $object = dragHandler.getDiv(model);
				var $previous = dragHandler.getPreviousNElements($object, 1);
				if ($previous != null) { 
					var previousSectionModel = FormBuilder.form.getSectionByDivId($previous.attr("id"));
					if (previousSectionModel != null && previousSectionModel.get("isRepeatable")) {
						canMove = false;
					}
					else {
						canMove = this.isFirstInRow(model) && this.hasPreviousRow(model);
					}
				}
				else {
					canMove = false;
				}
			}
		}
		else if (model != null && this.isAloneInRow(model)) {
			canMove = this.isFirstInRow(model) && this.hasPreviousRow(model);
		}
		this.enableDisableButton("joinRowUp", canMove);
		this.isJoinRowUp = canMove;
		return canMove;
	},
	
	/**
	 * Moves the given model's div to join the row above it
	 * 
	 * @param model the model (Question or Section) to move
	 */
	joinRowUp : function() {
		var model = this.model;
		if (this.canJoinRowUp(model)) {
			var dragHandler = this.getDragHandler(model);
			var $object = dragHandler.getDiv(model);
			var currentRowLength = dragHandler.getCountInCurrentRow($object);
			// note, this object is the first in its row
			var $currentRowNotObject = dragHandler.getNextNElements($object, currentRowLength - 1);
			var previousRowLength = dragHandler.getCountInPreviousRow($object);
			var $newRow = dragHandler.getPreviousNElements($object, previousRowLength);
			
			if ($newRow !== null) {
				$newRow = $newRow.add($object);
				dragHandler.equalWidth($newRow);
				QuestionDragHandler.enforceRowHeight($newRow);
				if ($currentRowNotObject != null) {
					dragHandler.equalWidth($currentRowNotObject);
					QuestionDragHandler.enforceRowHeight($currentRowNotObject);
				}
			}
			this.resizeSection(model);
			this.changeObject(model);
		}
	},
	
	/**
	 * Checks if the current model can leave the row down.
	 * Also marks the button in the interface appropriately
	 * 
	 * An object can only leave row down if it is the last (index 0) in
	 * its current row.
	 * 
	 * @param model the model (Question or Section) to check
	 */
	canLeaveRowDown : function(model) {
		var canMove = false;
//		if (model instanceof Section && this.isTableSection(model)) {
//			canMove = false;
//		}
		if (model != null && !this.isAloneInRow(model)) {
			canMove = this.isLastInRow(model);
		}
		this.enableDisableButton("leaveRowDown", canMove);
		this.isLeaveRowDown = canMove;
		return canMove;
	},
	
	leaveRowDown : function() {
		var model = this.model;
		if (this.canLeaveRowDown(model)) {
			// note: this object is the last in the row
			var dragHandler = this.getDragHandler(model);
			var $object = dragHandler.getDiv(model);
			var currentRowLength = dragHandler.getCountInCurrentRow($object);
			var $oldRow = dragHandler.getPreviousNElements($object, currentRowLength - 1);
			
			dragHandler.fullWidth($object);
			QuestionDragHandler.enforceRowHeight($object);
			
			if ($oldRow != null) {
				dragHandler.equalWidth($oldRow);
				QuestionDragHandler.enforceRowHeight($oldRow);
			}
			this.resizeSection(model);
			this.changeObject(model);
		}
	},

	canJoinRowDown : function(model) {
		var canMove = false;
		// table sections cannot be moved at all
//		if (model instanceof Section && this.isTableSection(model)) {
//			canMove = false;
//		}
		// repeatable sections cannot join row down
		if (model instanceof Section && model.get("isRepeatable")) {
			canMove = false;
		}
		else if (model != null && this.isAloneInRow(model)) {
			canMove = this.isLastInRow(model) && this.hasNextRow(model);
			
			if(canMove) {
				// we also don't want non-repeatable sections to join repeatable ones - that would have the same effect
				// of not having repeatable sections on their own row
				var dragHandler = this.getDragHandler(model);
				var $object = dragHandler.getDiv(model);
				var $next = dragHandler.getNextNElements($object, 1);
				var nextSectionModel = FormBuilder.form.getSectionByDivId($next.attr("id"));
				if (nextSectionModel != null && nextSectionModel.get("isRepeatable")) {
					canMove = false;
				}
				else {
					canMove = true;
				}
			}
			
			
		}

		this.enableDisableButton("joinRowDown", canMove);
		this.isJoinRowDown = canMove;
		return canMove;
	},
	
	joinRowDown : function() {
		var model = this.model;
		if (this.canJoinRowDown(model)) {
			// note: this object is the last in the row
			var dragHandler = this.getDragHandler(model);
			var $object = dragHandler.getDiv(model);
			var currentRowLength = dragHandler.getCountInCurrentRow($object);
			var downRowLength = dragHandler.getCountInNextRow($object);
			var $currentRow = dragHandler.getPreviousNElements($object, currentRowLength - 1);
			var $downRow = dragHandler.getNextNElements($object, downRowLength);
			
			$downRow = $downRow.add($object);
			dragHandler.equalWidth($downRow);
			QuestionDragHandler.enforceRowHeight($downRow);
			
			if ($currentRow != null) {
				dragHandler.equalWidth($currentRow);
				QuestionDragHandler.enforceRowHeight($currentRow);
			}
			this.resizeSection(model);
			this.changeObject(model);
		}
	},
	
	canLeaveRowUp : function(model) {
		var canMove = false;
//		if (model instanceof Section && this.isTableSection(model)) {
//			canMove = false;
//		}
		if (model != null && !this.isAloneInRow(model)) {
			canMove = this.isFirstInRow(model);
		}
		this.enableDisableButton("leaveRowUp", canMove);
		this.isLeaveRowUp = canMove;
		return canMove;
	},
	
	leaveRowUp : function() {
		// note: first in the row
		var model = this.model;
		if (this.canLeaveRowUp(model)) {
			var dragHandler = this.getDragHandler(model);
			var $object = dragHandler.getDiv(model);
			var oldRowLength = dragHandler.getCountInCurrentRow($object);
			var $oldRow = dragHandler.getNextNElements($object, oldRowLength - 1);
			
			dragHandler.fullWidth($object);
			QuestionDragHandler.enforceRowHeight($object);
			if ($oldRow != null) {
				dragHandler.equalWidth($oldRow);
				QuestionDragHandler.enforceRowHeight($oldRow);
			}
			
			this.resizeSection(model);
			this.changeObject(model);
		}
	},
	
	canReorderUp : function(model) {
		var canMove = false;
//		if (model instanceof Section && this.isTableSection(model)) {
//			canMove = false;
//		}
		if (model != null && this.hasPrevious(model)) {
			canMove = this.isTextbox(model) || (model instanceof Section && model.get("textContainer"));
		}
		this.enableDisableButton("reorderUp", canMove);
		this.isReorderUp = canMove;
		return canMove;
	},
	
	reorderUp : function() {
		var model = this.model;
		if (this.canReorderUp(model)) {
			var dragHandler = this.getDragHandler(model);
			var $object = dragHandler.getDiv(model);
			var $previous = $object.prev();
			if (model instanceof Section) {
				// go up until we find the first of any table
				while (this.sectionIsInTable($previous) && !this.isSectionFirstInTable($previous)) {
					$previous = $previous.prev();
				}
			}
			
			$previous.before($object);
			
			dragHandler.refreshRows();
			this.resizeSection(model);
			this.changeObject(model);
		}
	},
	
	canReorderDown : function(model) {
		var canMove = false;
//		if (model instanceof Section && this.isTableSection(model)) {
//			canMove = false;
//		}
		if (model != null && this.hasNext(model)) { 
			canMove = this.isTextbox(model) || (model instanceof Section && model.get("textContainer"));
		}
		this.enableDisableButton("reorderDown", canMove);
		this.isReorderDown = canMove;
		return canMove;
	},
	
	reorderDown : function() {
		var model = this.model;
		if (this.canReorderDown(model)) {
			var dragHandler = this.getDragHandler(model);
			var $object = dragHandler.getDiv(model);
			var $next = $object.next();
			if (model instanceof Section) {
				// go up until we find the last of any table
				while (this.sectionIsInTable($next) && !this.isSectionLastInTable($next)) {
					$next = $next.next();
				}
			}
			$next.after($object);
			
			dragHandler.refreshRows();
			this.resizeSection(model);
			this.changeObject(model);
		}
	},
	
	sectionIsInTable : function($section) {
		if ($section.hasClass(Config.tableHeaderClassNames.tableHeader) 
				|| $section.hasClass(Config.tableHeaderClassNames.columnHeader)
				|| $section.hasClass(Config.tableHeaderClassNames.tablePrimary)) {
			return true;
		}
		return false;
	},
	
	isSectionLastInTable : function($section) {
		var $next = $section.next();
		return $next.length < 1 || !this.sectionIsInTable($next);
	},
	
	isSectionFirstInTable : function($section) {
		var $previous = $section.prev();
		return $previous.length < 1 || !this.sectionIsInTable($previous);
	},
	
	/**
	 * Resizes a section given a child Question model OR the 
	 * Section model itself.
	 * 
	 * @param model Question or Section model
	 */
	resizeSection : function(model) {
		var sectionModel = model;
		if (model instanceof Question) {
			sectionModel = FormBuilder.form.getQuestionParentSection(model);
		}
		EventBus.trigger("resize:section", sectionModel);
	}
});

