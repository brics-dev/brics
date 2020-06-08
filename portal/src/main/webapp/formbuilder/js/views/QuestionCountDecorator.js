/**
 * Effectively a snippet class that will be merged into the concrete Question
 * View class.  The properties here will NOT overwrite anything in the concrete
 * class.
 * 
 */
var QuestionCountDecorator = {
	commonsName : "QuestionCountDecorator",
	events : {
		"click #selectCountQuestions" : "onAddEditCountQuestions"
	},
	
	validationRules : [
		new ValidationRule({
			fieldName: "questionsToCountDisplay",
			description: "A question may not have both a count formula and a calculation rule",
			match : function(model) {
				var questionsInCount = model.get("questionsInCount");
				var questionsInCalc = model.get("questionsToCalculate");
				if (questionsInCount.length != 0 && questionsInCalc.length != 0) {
					return false;
				}
				return true;
			}
		})
	],
	
	/**
	 * Renders this commons to the editor
	 * 
	 * @param model the question model
	 */
	renderCountCommons : function(model) {
		var type = this.model.get("questionType");
		if(type === Config.questionTypes.textbox) {
			var linkHtml = TemplateManager.getTemplate("editQuestionCountTabLabel");
			var contentHtml = TemplateManager.getTemplate("editQuestionCountTab");
			this.addTab(linkHtml, contentHtml);
			
			this.onChangeAnswerTypeInCount();
			this.listenTo(this.model, "change:answerType", this.onChangeAnswerTypeInCount);
		}
		
		if ( !FormBuilder.page.get("countDialogView")) {
			FormBuilder.page.set("countDialogView", new CountDialogView());
		}
		
		// display the current value in the selected questions box
		
		this.$("#questionsToCountDisplay").html(this.encodedToHumanReadableFormula());
	},
	
	/**
	 * Turns the S_Q_ encoded question identifier into the sectionName.shortName format
	 * for display in the formula
	 */
	encodedToHumanReadableFormula : function() {
		var questions = this.model.get("questionsInCount");
		var output = "";
		for (var i = 0, qlen = questions.length; i < qlen; i++) {
			if (output != "") {
				output += " + ";
			}
			var qSplit = questions[i].split("_");
			var sectionId = qSplit[1];
			var questionId = qSplit[3];
			var question = FormBuilder.form.getQuestionByQuestionIdSectionId(questionId, sectionId);
			
			output += "[" + question.get("dataElementName") + "]";
		}
		return output;
	},
	
	onAddEditCountQuestions : function() {
		var view = FormBuilder.page.get("countDialogView");
		EventBus.trigger("open:processing", Config.language.loadSkipQuest);
		view.render(this.model);
	},
	
	onChangeAnswerTypeInCount : function() {
		var answerType = this.model.get("answerType");
		if(answerType != 2) {
			//hide tab
			this.disableTab('dialog_editQuestion_count');
			
		}else {
			//show tab
			this.enableTab('dialog_editQuestion_count');
		}
	},
	
	config : {
		name : "QuestionCountDecorator",
		render : "renderCountCommons"
	}
};