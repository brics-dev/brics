/**
 * This class handles the business rule that says multiple questions of the
 * same name MAY exist on a single form (but not the same section) and that
 * there are some properties of a question that, if changed for one of those
 * instances, must be changed for others.
 * 
 * When changing a question, this class makes the needed change to all other
 * instances of that question on the page.
 * 
 * One concern is that models are changed immediately when the user edits the
 * fields in the editors.  This class doesn't want to respond to THAT; it wants
 * to respond to the save:question event.  That DOES, however, mean that this
 * class (or StateManager?) needs to watch what is changed so it is able to
 * decide what action to take.
 */
var QuestionChangePropagator = {
	init : function() {
		EventBus.on("save:question", this.onSaveQuestion, this);
	},
	
	onSaveQuestion : function(model) {
		var otherQuestions = this.getOtherChildQuestions(model.get("questionName"),model.get("sectionId"));
		var numOtherQuestions = otherQuestions.length;
		if (numOtherQuestions > 1) {
			// reminder! model is the one that was changed
			var changes = this.getChanges(model);
			// we DO need to propagate these
			for (var i = 0; i < numOtherQuestions; i++) {
				// should this change be silent?  I'm not sure
				var otherModel = otherQuestions[i];
				otherModel.set(changes);
				// note: we have already run pageView.changeQuestionType on the primary
				if (model.cid != otherModel.cid && changes.hasOwnProperty("questionType") && !otherModel.isRepeatedChild()) {
					// tell pageView to change the type
					FormBuilder.pageView.changeQuestionType(otherModel);
				}
				
			}
		}
	},
	
	othersExist : function(questionName) {
		var otherQuestions = FormBuilder.form.findQuestionsWhere({questionName: questionName});
		// because this one has that name, we can't do > 0
		return otherQuestions.length > 1;
	},
	
	getOtherQuestions : function(questionName) {
		return FormBuilder.form.findQuestionsWhere({questionName: questionName});
	},
	getOtherChildQuestions : function(questionName,sectionId) {
		return FormBuilder.form.findChildQuestions({questionName: questionName,sectionId:sectionId});
	},
	
	isAttributeCore : function(attribute, questionModel) {
		var coreAttrs = questionModel.questionCoreAttrs;
		return coreAttrs.indexOf(attribute) >= 0;
	},
	
	/**
	 * Gets an object representing changes made to the model.
	 * 
	 * This is a difficult method because it abstracts away what is really the
	 * core of this class.  This is where the decision is made about which
	 * attributes to change and knowing what exactly was changed in the first
	 * place.
	 * 
	 * @param model the changed model
	 */
	getChanges : function(model) {
		var changes = StateManager.changeSet;
		var coreChanges = {};
		for (var attr in changes) {
			if (this.isAttributeCore(attr, model)) {
				coreChanges[attr] = model.get(attr);
			}
		}
		return coreChanges;
	}
};