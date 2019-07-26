var VisualScaleEditView = QuestionEditView.extend({
	dialogTitle : "Edit Visual Scale Question",
	templateName : "visualScaleQuestionEditTemplate", // this is tab 1: basic options
	validationRules : [
	   new ValidationRule({fieldName: "questionName", required: true, description: Config.language.questionNameRequired}),
	   new ValidationRule({  
    	   fieldName : "questionName",
	       description: Config.language.validateQuestionNameSpecial,
    	   match : function(model) {
    		   var questionName = model.get("questionName");
    		   var iChars = "!@#$%^&*()+=[]\\\';,./{}|\":<>?";
    		   for(var i = 0; i < questionName.length; i++){
        		   if (iChars.indexOf(questionName.charAt(i)) != -1) {
        			   return false;
        		   }
    		   }
    		   return true;
    	   }
       }),
       new ValidationRule({
	   		fieldName : "questionText",
	   		description : Config.language.maxQuestionText,
	   		match : function(model) {
	   			var questionTextStr = model.get("questionText");
	   			if(questionTextStr!=null && questionTextStr.length > Config.questionText.maxQuestionText){
	   				return false;
	   			}else{
	   				return true;
	   			}
	   		}
	   	}),
       new ValidationRule({  
    	   fieldName : "vscaleCenterText",
	       description: Config.language.validateVScenterText,
    	   match : function(model) {
    		   var vscaleCenterText = model.get("vscaleCenterText");
    		   var iChars = "!@#$%^&*()+=[]\\\';,./{}|\":<>?";
    		   for(var i = 0; i < vscaleCenterText.length; i++){
        		   if (iChars.indexOf(vscaleCenterText.charAt(i)) != -1) {
        			   return false;
        		   }
    		   }
    		   return true;
    	   }
       }),
       new ValidationRule({  
    	   fieldName : "vscaleLeftText",
	       description: Config.language.validateVSleftText,
    	   match : function(model) {
    		   var vscaleLeftText = model.get("vscaleLeftText");
    		   var iChars = "!@#$%^&*()+=[]\\\';,./{}|\":<>?";
    		   for(var i = 0; i < vscaleLeftText.length; i++){
        		   if (iChars.indexOf(vscaleLeftText.charAt(i)) != -1) {
        			   return false;
        		   }
    		   }
    		   return true;
    	   }
       }),
       new ValidationRule({  
    	   fieldName : "vscaleRightText",
	       description: Config.language.validateVSrightText,
    	   match : function(model) {
    		   var vscaleRightText = model.get("vscaleRightText");
    		   var iChars = "!@#$%^&*()+=[]\\\';,./{}|\":<>?";
    		   for(var i = 0; i < vscaleRightText.length; i++){
        		   if (iChars.indexOf(vscaleRightText.charAt(i)) != -1) {
        			   return false;
        		   }
    		   }
    		   return true;
    	   }
       }),
       new ValidationRule({  
    	   fieldName : "questionName",
	       description: Config.language.validateQuestionNameWhite,
    	   match : function(model) {
    		   var questionName = model.get("questionName");
    		   if (/\s/g.test(questionName)) {
    			   return false;
    		   }else {
    			   return true;
    		   }
    	   }
       }),
	   new ValidationRule({
		   fieldName : "vscaleRangeStart",
		   description : Config.language.validateVsMinInteger,
		   match : /^[0-9]+$/igm
	   }),
	   new ValidationRule({
		   fieldName : "vscaleRangeStart",
		   description : Config.language.validateVsEndMimMaxLimit,
		   match : function(model) {
			   var max = Number(model.get("vscaleRangeStart"));
			   if(max<5001){
				   return true;
			   }
			   return false;
		   }
	   }),
	   new ValidationRule({
		   fieldName : "vscaleRangeEnd",
		   description : Config.language.validateVsEndMaxLimit,
		   match : function(model) {
			   var max = Number(model.get("vscaleRangeEnd"));
			   if(max<5001){
				   return true;
			   }
			   return false;
		   }
	   }),
	   new ValidationRule({
		   fieldName : "vscaleRangeEnd",
		   description : Config.language.validateVsMaxInteger,
		   match : /^[0-9]+$/igm
	   }),
	   new ValidationRule({
		   fieldName : "vscaleWidth",
		   description : Config.language.validateVsWidthInteger,
		   match : /^[0-9]+$/igm
	   }),
	   new ValidationRule({
		   fieldName : "vscaleWidth",
		   description : Config.language.validateVsWidthMinLimit,
		   match : function(model) {
			   var max = Number(model.get("vscaleWidth"));
			   if(max>0){
				   return true;
			   }
			   return false;
		   }
	   }),
	   new ValidationRule({
		   fieldName : "vscaleWidth",
		   description : Config.language.validateVsWidthMaxLimit,
		   match : function(model) {
			   var max = Number(model.get("vscaleWidth"));
			   if(max<151){
				   return true;
			   }
			   return false;
		   }
	   }),
	   new ValidationRule({
	 	   fieldName : "vscaleRangeStart",
	 	   description : Config.language.validateVsMinMaxRange,
	 	   match : function(model) {
	 		   var max = Number(model.get("vscaleRangeEnd"));
	 		  var min = Number(model.get("vscaleRangeStart"));
	 		   if (max > min) {
	 			   return true;
	 		   }
	 		   return false;
	 	   }
	   })
	
	
	],
	
	initialize : function() {
		VisualScaleEditView.__super__.initialize.call(this);
		this.template = TemplateManager.getTemplate(this.templateName);
		this.initCommons();
		this.registerCommons(this, QuestionAdditionalTextDecorator);
		this.registerCommons(this, QuestionDefaultValueDecorator);
		this.registerCommons(this, QuestionFormatDecorator);
		this.registerCommons(this, QuestionGraphicDecorator);/*added by Cing Heng*/
		//this.registerCommons(this, QuestionValidationDecorator);
		//this.registerCommons(this, QuestionPrepopulationDecorator);
		
		//for testing
		//this.registerCommons(this, QuestionOptionDecorator);
		
	},
	render : function(model) {
		this.model = model;
		this.listenTo(this.model, "change", this.refreshSize);
		
		this.$el.html(this.template(model.attributes));
		VisualScaleEditView.__super__.render.call(this, model);
		
		if( this.model.get("isNew") )
		{
			this.$('select[name="questionType"] option').prop("disabled", true);
		}
		else
		{ 
			this.$('select[name="questionType"] option').prop("disabled", false);
			this.$('select[name="questionType"] option[value="3"]').prop("disabled", true);
			this.$('select[name="questionType"] option[value="4"]').prop("disabled", true);
			this.$('select[name="questionType"] option[value="5"]').prop("disabled", true);
			this.$('select[name="questionType"] option[value="6"]').prop("disabled", true);
			this.$('select[name="questionType"] option[value="9"]').prop("disabled", true);
			this.$('select[name="questionType"] option[value="11"]').prop("disabled", true);
			this.$('select[name="questionType"] option[value="12"]').prop("disabled", true);
		}
		
		
		
		var operatorType = this.model.get("rangeOperator");		    	
    	if(operatorType == Config.rangeOperator.isEqualTo){
    		this.$("#scaleRangeMinimum").prop('disabled', true);
    		this.$("#scaleRangeMaximum").prop('disabled', true);
    		
    	}
    	else if(operatorType == Config.rangeOperator.lessThan){
    		this.$("#scaleRangeMinimum").prop('disabled', false);
    		this.$("#scaleRangeMaximum").prop('disabled', true);
    	}
    	else if(operatorType == Config.rangeOperator.greaterThan){
    		this.$("#scaleRangeMinimum").prop('disabled', true);
    		this.$("#scaleRangeMaximum").prop('disabled', false);
    	}
    	else if(operatorType == Config.rangeOperator.between){
    		this.$("#scaleRangeMinimum").prop('disabled', true);
    		this.$("#scaleRangeMaximum").prop('disabled', true);
    	}else{
    		this.$("#scaleRangeMinimum").prop('disabled', false);
    		this.$("#scaleRangeMaximum").prop('disabled', false);
  		}
		

		return this;
	},
	
	refreshSize : function() {
		EventBus.trigger("resize:question", this.model);
	}
	
});
