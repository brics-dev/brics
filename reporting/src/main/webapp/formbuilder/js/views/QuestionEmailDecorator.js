/**
 * Effectively a snippet class that will be merged into the concrete Question
 * View class. The properties here will NOT overwrite anything in the concrete
 * class.
 * 
 * Email Notifications only matter for select type question
 * 
 */
var QuestionEmailDecorator = {
	commonsName : "QuestionEmailDecorator",

	validationRules : [
	// address format
	new ValidationRule({
		fieldName : "toEmailAddress",
		description : Config.language.emailAddress,
		match : function(model) {
			var emailReg = /^([\w-\.]+@([\w-]+\.)+[\w-]{2,4})?$/;
			if (emailReg.test(model.get("toEmailAddress"))) {
				return true;
			} else {
				return false;
			}
		}
	}), new ValidationRule({
		fieldName : "ccEmailAddress",
		description : Config.language.emailAddress,
		match : function(model) {
			var emailReg = /^([\w-\.]+@([\w-]+\.)+[\w-]{2,4})?$/;
			if (emailReg.test(model.get("ccEmailAddress"))) {
				return true;
			} else {
				return false;
			}
		}
	}), new ValidationRule({
		fieldName : "toEmailAddress",
		description : Config.language.emailRecipientRequired,
		match : function(model) {
			var toEmailAddress = model.get("toEmailAddress");
			if (toEmailAddress == null) {
				toEmailAddress = "";
			}
			if (model.get("emailTrigger")) {
				if (toEmailAddress.length == 0) {
					return false;
				} else {
					return true;
				}
			} else {
				return true;
			}
		}
	}), new ValidationRule({
		fieldName : "triggerAnswers",
		description : Config.language.triggerAnswer,
		match : function(model) {
			var triggerAnswers = model.get("triggerAnswers");
			if (triggerAnswers == null) {
				triggerAnswers = [];
			}
			if (model.get("emailTrigger")) {
				if (triggerAnswers.length == 0) {
					return false;
				} else {
					return true;
				}
			} else {
				return true;
			}
		}
	})
	// trigger can't be empty
	],

	mainThis : null,

	events : {
		"change #createTrigger" : "showHideEmailTrigger"
	},
	/**
	 * Renders this commons to the editor
	 * 
	 * @param model
	 *            the question model
	 */
	renderEmailCommons : function(model) {
		mainThis = this;
		// the below shows an example of how to listen for changes to the model
		model.on("change:questionOptionsObjectArray", this.showTriggerAnswers);
		// this.model.on("change:triggerAnswers", this.setEmailTriggerFlag,this.model);
		/*this.model.on("change:triggerAnswers", this.showHideTriggerDeletor,this.model);*/
		//this.model.on("change:deleteTrigger", this.showHideTriggerDeletor,this.model);
		
		var linkHtml = TemplateManager.getTemplate("editQuestionEmailTabLabel");
		var contentHtml = TemplateManager.getTemplate("editQuestionEmailTab");
		this.addTab(linkHtml, contentHtml);
		this.showTriggerAnswers(model);
		model.set("eMailTriggerUpdatedBy", Config.userId);
		if(model.get("emailTrigger")){
			this.$("#createTrigger").attr('checked', true);
			this.showHideEmailTrigger();
		}else{}
	},

	config : {
		name : "QuestionEmailDecorator",
		render : "renderEmailCommons"
	},

	showTriggerAnswers : function(model) {
		var questionOptionsObjectArray = model.get("questionOptionsObjectArray");
		var triggerAnswers = model.get("triggerAnswers");
		if (triggerAnswers == null) {
			triggerAnswers = [];
		}
		var mainThis = FormBuilder.page.get("activeEditorView");
		mainThis.$("#triggerAnswers").html('');
		if(questionOptionsObjectArray.length > 0) {
			for(var i=0;i<questionOptionsObjectArray.length;i++) {
				var questionOptionsObject = questionOptionsObjectArray[i];
				var option = questionOptionsObject.option;
				mainThis.$("#triggerAnswers").append($("<option></option>").attr("value", option).text(option));
				//Array
				if (triggerAnswers.length != 0) {
					if (triggerAnswers.indexOf(option) != -1) {
						mainThis.$("#triggerAnswers option[value='"+option+"']").prop("selected", true);
					}
				}
				//Object
			 
			
				
				
				
			}
			
			for (var i=0; i<triggerAnswers.length; i++) {
				if((typeof triggerAnswers[i]=="object") &&triggerAnswers[i]!=null ){
					//var $triggerAns = mainThis.$("#triggerAnswers");
					//var $objectVal = mainThis.$("#triggerAnswers").find("option[value='"+triggerAnswers[i].etAnswer+"']");
					//var $objectVal =$triggerAns.find("option[value='Ocular']");
					var key = triggerAnswers[i].etValId;
					var val = triggerAnswers[i].etAnswer;
					
					//$objectVal.prop("selected", true);
					//mainThis.$("#triggerAnswers option[value='"+val+'>' +key+ '</option>'+"']").prop("selected", true);
					//mainThis.$("#triggerAnswers option[value='"+val+'>' +key+ '</option>'+"']").css('color','red');
					var $objectVal = mainThis.$("#triggerAnswers").find("option[value='"+val+"']").css('background-color','grey');


					//mainThis.$("#triggerAnswers option[value=' + triggerAnswers[i].etValId + '>' + triggerAnswers[i].etAnswer + '</option>').css('color','red');

					//mainThis.$("#triggerAnswers").html('<option value=' + triggerAnswers[i].etValId + '>' + triggerAnswers[i].etAnswer + '</option>').css('color','red');
				 }
			}
		}

	},
	/*
	 * setEmailTriggerFlag : function(model){ },
	 */
	showHideEmailTrigger : function() {
		if(this.$("#createTrigger").is(':checked')){
			this.model.set("deleteTrigger", false);
			this.model.set("emailTrigger", true);
			this.$('.createET').show();
		}else{
			this.model.set("deleteTrigger", true);
			this.model.set("emailTrigger", false);			
			this.$('.createET').hide();
			this.deleteTrigger(this.model);
		}
		 
	},

	deleteTrigger : function(model) {
		model.set("toEmailAddress", "");
		model.set("ccEmailAddress", "");
		model.set("subject", "");
		model.set("body", "");
		model.set("triggerAnswers", []);
	}
};