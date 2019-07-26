/**
 * Effectively a snippet class that will be merged into the concrete Question
 * View class.  The properties here will NOT overwrite anything in the concrete
 * class.
 * 
 */
var QuestionPrepopulationDecorator = {
	commonsName : "QuestionPrepopulationDecorator",
	events : {
		
	},
	
	validationRules : [
	           	    
	          	     new ValidationRule({  
	              	   fieldName : "prepopulationValue",
	          	       description: Config.language.requiredPreValue,
	              	   match : function(model) {
	              		   var prepopulation = model.get("prepopulation");
	              		   if(prepopulation) {
	              			   if(model.get("prepopulationValue")==''){
	              				 return false;
	              			   }else{
	              				 return true;
	              			   }
	              			   
	              		   }else {
	              			   return true;
	              		   }
	              	   }
	          	      })
	          	     ],
	
	/**
	 * Renders this commons to the editor
	 * 
	 * @param model the question model
	 */
	renderPrepopulationCommons : function(model) {
		var type = model.get("questionType");
		if (type === Config.questionTypes.textbox) {
			var linkHtml = TemplateManager.getTemplate("editQuestionPrepopulationTabLabel");
			var contentHtml = TemplateManager.getTemplate("editQuestionPrepopulationTab");
			this.addTab(linkHtml, contentHtml);
			
			this.model.on("change:prepopulation", this.onChangePrepopulation, this);
			this.model.on("change:answerType", this.onChangeAnswerTypeInPrePop, this);
			this.onChangePrepopulation();
			this.onChangeAnswerTypeInPrePop();
		}
		else if (type === Config.questionTypes.select || type === Config.questionTypes.radio) {
			var linkHtml = TemplateManager.getTemplate("editQuestionPrepopulationTabLabel");
			var contentHtml = TemplateManager.getTemplate("editQuestionPrepopulationTab");
			this.addTab(linkHtml, contentHtml);
			
			this.model.on("change:prepopulation", this.onChangePrepopulation, this);
			this.model.on("change:answerType", this.onChangeAnswerTypeInPrePop, this);
			this.onChangePrepopulation();
			this.onChangeAnswerTypeInPrePop();
		}
		else if (type === Config.questionTypes.fileUpload) {
			
		}
		else if(type === Config.questionTypes.textarea){
			
		}
		else if(type === Config.questionTypes.imageMap){
			
		}
		else if(type === Config.questionTypes.radio){
			
		}
		else if(type ===Config.questionTypes.checkbox){
			
		}	
		else if(type === Config.questionTypes.multiSelect){
			
		}
		else if(type === Config.questionTypes.textblock){
			
		}
		else if(type === Config.questionTypes.visualscale){
			
		}

	},
	
	config : {
		name : "QuestionPrepopulationDecorator",
		render : "renderPrepopulationCommons"
	},
	onChangePrepopulation : function() {
		var prePop = this.model.get("prepopulation");
		
		if(prePop){
			this.$(".prePopDependent").show();
		}else{
			this.$(".prePopDependent").hide();
		}
	},
	onChangeAnswerTypeInPrePop : function() {
		var answerType = this.model.get("answerType");

		if(answerType==2){
		//Need to hide prePop tab on this selection
			this.disableTab('dialog_editQuestion_prepopulation');
		}else if(answerType==3){
			this.enableTab('dialog_editQuestion_prepopulation');
			this.$('[name="prepopulationValue"]').children().prop("disabled", true);
			this.$('[name="prepopulationValue"]').children('[value="visitDate"]').prop("disabled", false);
		}else if(answerType==1){
			this.enableTab('dialog_editQuestion_prepopulation');
			this.$('[name="prepopulationValue"]').children().prop("disabled", false);
			this.$('[name="prepopulationValue"]').children('[value="visitDate"]').prop("disabled", true);
		}else if(answerType==4){
			this.enableTab('dialog_editQuestion_prepopulation');
			this.$('[name="prepopulationValue"]').children().prop("disabled", true);
			this.$('[name="prepopulationValue"]').children('[value="visitDate"]').prop("disabled", false);
		}else{
			this.$(".prePopDependent").hide();
		}
	}
};