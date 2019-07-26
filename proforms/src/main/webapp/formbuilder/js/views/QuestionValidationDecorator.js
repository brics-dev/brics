/**
 * Effectively a snippet class that will be merged into the concrete Question
 * View class.  The properties here will NOT overwrite anything in the concrete
 * class.
 * 
 */
var QuestionValidationDecorator = {
	commonsName : "QuestionValidationDecorator",
	
	validationRules : [       
	                new ValidationRule({
	                	fieldName : "rangeValue1",
	                	description : Config.language.validateRange1Integer,
	                	match : function(model){
	                		var answerType = model.get("answerType");
	                		var rangeValue1 = model.get("rangeValue1");
	                		var rangeValue2 = model.get("rangeValue2");
	                		var rangeOper = model.get("rangeOperator");
	                	
	                		if(answerType==String(Config.answerType.numeric)){
	                			if(rangeOper==Config.rangeOperator.between){
	                				if(!rangeValue1.toString().match(/^[0-9]+$/igm) && rangeValue1){
	                					this.description = Config.language.validateRange1Integer;
	                					this.fieldName = "rangeValue1";
	                					return false;
	                				}
	                				if(!rangeValue2.toString().match(/^[0-9]+$/igm) && rangeValue2){
	                					this.description = Config.language.validateRange2Integer;
	                					this.fieldName = "rangeValue2";
	                					return false;
	                				}
	                				if(rangeValue1 == '' && rangeValue1 != 0){
	                					this.description = Config.language.betweenValue1Empty;
	                					this.fieldName = "rangeValue1";
	                					return false;
	                				}
	                				if(rangeValue2 == '' && rangeValue2 != 0){
	                					this.description = Config.language.betweenValue2Empty;
	                					this.fieldName = "rangeValue2";
	                					return false;
	                				}
	                				if(Number(rangeValue1) > Number(rangeValue2)){
	                					this.description = Config.language.value1LessThan2;
	                					this.fieldName = "rangeValue1";
	                					return false;
	                				}
	                				
	                				if(Number(rangeValue1) == Number(rangeValue2)){
	                					this.description = Config.language.value1LessThan2;
	                					this.fieldName = "rangeValue1";
	                					return false;
	                				}
	                				return true;
	                			}
	                		/*	else if(rangeOper == String(Config.rangeOperator.lessThan)){
	                				if(!rangeValue1.match(/^[0-9]+$/igm) && rangeValue1 != ''){
	                					this.description = Config.language.validateRange1Integer;
	                					this.fieldName = "rangeValue1";
	                					return false;
	                				}
	                				
	                			}*/
	                			
	                			else if(rangeOper == String(Config.rangeOperator.lessThan)){
	                				var result = isNaN(rangeValue1);
	                				if(result){
	                					this.description = Config.language.validateRange1Integer;
	                					this.fieldName = "rangeValue1";
	                					return false;
	                				}else{
	                					return true;
	                				}
	                			}
	                			else if (rangeOper == String(Config.rangeOperator.greaterThan)){
	                				/*if(!rangeValue1.match(/^[0-9]+$/igm) && rangeValue1 != '' ){
	                					this.description = Config.language.validateRange1Integer;
	                					this.fieldName = "rangeValue1";
	                					return false;
	                				}*/
	                				var result = isNaN(rangeValue1);
	                				if(result){
	                					this.description = Config.language.validateRange1Integer;
	                					this.fieldName = "rangeValue1";
	                					return false;
	                				}else{
	                					return true;
	                				}
	                			}
	                			else if (rangeOper == String(Config.rangeOperator.isEqualTo)){
	                				/*if(!rangeValue1.match(/^[0-9]+$/igm) && rangeValue1 != ''){
	                					this.description = Config.language.validateRange1Integer;
	                					this.fieldName = "rangeValue1";
	                					return false;
	                				}*/
	                				var result = isNaN(rangeValue1);
	                				if(result){
	                					this.description = Config.language.validateRange1Integer;
	                					this.fieldName = "rangeValue1";
	                					return false;
	                				}else{
	                					return true;
	                				}
	                				
	                			}else{
	                				//alert("No operator selected");
	                				return true;
	                			}
	                		
	                		}else{
	                			return true;
	                		}
	                	}
	                	
	                }),
					new ValidationRule({  
						fieldName : "minCharacters",
						description: Config.language.minNum,
						match : function(model) {
							var minC = model.get("minCharacters");
							var intRegex = /^[0-9]+$/igm;
							if(!(intRegex.test(minC)) || minC < 0) {
								return false;
							}
							else {
								return true;
							}
						}
					}),
	              	new ValidationRule({  
						fieldName : "maxCharacters",
						description: Config.language.maxNum,
						match : function(model) {
							var maxC = model.get("maxCharacters");
							var intRegex = /^[0-9]+$/igm;
							if(!(intRegex.test(maxC)) || maxC < 0) {
								return false;
							}
							else {
								return true;
							}
						}
					}),
	                new ValidationRule({
	            		fieldName : "minCharacters",
	            		description : Config.language.minChars,
	            		match : function(model) {
	            			if(model.get("minCharacters") < 0){
	            				return false;
	            			}else{
	            				return true;
	            			}
	            		}
	            	}),
	            	new ValidationRule({
	            		fieldName : "maxCharacters",
	            		description : Config.language.minGraterThanMax,
	            		match : function(model) {
	            			if(model.get("minCharacters") > model.get("maxCharacters")){
	            				return false;
	            			}else{
	            				return true;
	            			}
	            		}
	            	}),
	            	new ValidationRule({
	            		fieldName : "maxCharacters",
	            		description : Config.language.maxGreatThanOne,
	            		match : function(model) {
	            			if(model.get("maxCharacters")<1){
	            				return false;
	            			}else{
	            				return true;
	            			}
	            		}
	            	}),
	    
	            	new ValidationRule({
	            		fieldName : "maxCharacters",
	            		description : Config.language.maxChars,
	            		match : function(model) {
	            			if(model.get("maxCharacters") > 4000){
	            				return false;
	            			}else{
	            				return true;
	            			}
	            		}
	            	})
	               
	            	],
	events : {
		
	},
	
	
	/**
	 * Renders this commons to the editor
	 * 
	 * @param model the question model
	 */
	renderValidationCommons : function(model) {
		var type = model.get("questionType");
		if (type === Config.questionTypes.textbox) {
			var linkHtml = TemplateManager.getTemplate("editQuestionValidationTabLabel");
			var contentHtml = TemplateManager.getTemplate("editQuestionValidationTab");
			this.addTab(linkHtml, contentHtml);
			this.model.on("change:answerType", this.onChangeAnswerType, this);
			this.model.on("change:rangeOperator", this.onChangeOperatorType, this);
			this.onChangeAnswerType();
			this.onChangeOperatorType();
			
		}
		else if(type === Config.questionTypes.textarea){
			var linkHtml = TemplateManager.getTemplate("editQuestionValidationTabLabel");
			var contentHtml = TemplateManager.getTemplate("editQuestionValidationTab");
			this.addTab(linkHtml, contentHtml);
			this.model.on("change:answerType", this.onChangeAnswerType, this);
			this.model.on("change:rangeOperator", this.onChangeOperatorType, this);
			this.onChangeAnswerType();
			this.onChangeOperatorType();
			$(".hideThisForTextArea").hide();
		}
		else if (type === Config.questionTypes.select) {
			var linkHtml = TemplateManager.getTemplate("editQuestionValidationTabLabel");
			var contentHtml = TemplateManager.getTemplate("editQuestionValidationTab");
			this.addTab(linkHtml, contentHtml);
			$(".stringSelectionDependent, .numericDependent, .rangeDependent").hide();
			this.$("#answerTypeId").prop('disabled', true);
			
			
		}
		else if(type === Config.questionTypes.radio){
			var linkHtml = TemplateManager.getTemplate("editQuestionValidationTabLabel");
			var contentHtml = TemplateManager.getTemplate("editQuestionValidationTab");
			this.addTab(linkHtml, contentHtml);
			$(".stringSelectionDependent, .numericDependent, .rangeDependent").hide();
			this.$("#answerTypeId").prop('disabled', true);
			
		}
		else if(type === Config.questionTypes.multiSelect){
			var linkHtml = TemplateManager.getTemplate("editQuestionValidationTabLabel");
			var contentHtml = TemplateManager.getTemplate("editQuestionValidationTab");
			this.addTab(linkHtml, contentHtml);
			$(".stringSelectionDependent, .numericDependent, .rangeDependent").hide();
			this.$("#answerTypeId").prop('disabled', true);
			
			
		}
		
		else if(type ===Config.questionTypes.checkbox){
			var linkHtml = TemplateManager.getTemplate("editQuestionValidationTabLabel");
			var contentHtml = TemplateManager.getTemplate("editQuestionValidationTab");
			this.addTab(linkHtml, contentHtml);
			$(".stringSelectionDependent, .numericDependent, .rangeDependent").hide();
			this.$("#answerTypeId").prop('disabled', true);
			
		}
		
		else if(type === Config.questionTypes.imageMap){
			var linkHtml = TemplateManager.getTemplate("editQuestionValidationTabLabel");
			var contentHtml = TemplateManager.getTemplate("editQuestionValidationTab");
			this.addTab(linkHtml, contentHtml);
			$(".stringSelectionDependent, .numericDependent, .rangeDependent, .doNotShowForOnlyRequired").hide();
			
		}
		
		else if(type === Config.questionTypes.visualScale) {
			//There is not validation tab in image map so no code here
		}
		
		else if (type === Config.questionTypes.fileUpload) {
			var linkHtml = TemplateManager.getTemplate("editQuestionValidationTabLabel");
			var contentHtml = TemplateManager.getTemplate("editQuestionValidationTab");
			this.addTab(linkHtml, contentHtml);
			$(".stringSelectionDependent, .numericDependent, .rangeDependent, .doNotShowForOnlyRequired").hide();
		}
	
		else if(type === Config.questionTypes.textblock){
			//Not sure if we need any valiation here this is newly addded functionality?Check with JP
		}	
	
		this.renderFieldRequired();
		this.renderFieldRangeOperator();
	},
	
	config : {
		name : "QuestionValidationDecorator",
		render : "renderValidationCommons"
	},
	
	onChangeOperatorType : function() {
		var operatorType = this.model.get("rangeOperator");
		if(operatorType == String(Config.rangeOperator.isEqualTo)){
			this.$(".rangeDependent").show();
			this.$(".betweenDependent").hide();
			this.$("#rangeOneText").html("Value =");
		}
		else if(operatorType == String(Config.rangeOperator.lessThan)){
			this.$(".rangeDependent").show();
			this.$(".betweenDependent").hide();
			this.$("#rangeOneText").html("Value <= ");
		}
		else if(operatorType == String(Config.rangeOperator.greaterThan)){
			this.$(".rangeDependent").show();
			this.$(".betweenDependent").hide();
			this.$("#rangeOneText").html("Value >= ");
		}
		else if(operatorType == String(Config.rangeOperator.between)){
			this.$(".rangeDependent,.betweenDependent ").show();
			this.$("#rangeOneText").html("Value >= ");
			this.$("#rangeTwoText").html("Value <= ");
		}else{
			this.$(".rangeDependent,.betweenDependent ").hide();
		}
	},
	
	calucationRuleValidation : function(){
		var calculatedQuestionFlag = this.model.get("calculatedQuestion");
		var calDependentFlag = this.model.get("calDependent");
		this.hideEditorWarning();
		if(calculatedQuestionFlag||calDependentFlag){		
			this.showEditorWarning(Config.language.calcRuleValidation);		
			if(this.model.previous("answerType") != $('#answerTypeId').val()){
				var previousValue = this.model.previous("answerType");
				this.model.set("answerType",previousValue,{silent:true});
				$("#answerTypeId option").filter(function() {
				    return $(this).val() == previousValue; 
				}).prop('selected', true);
			}
			
		}
	},
	
	onChangeAnswerType : function() {
		var answerType = this.model.get("answerType");
		var operatorType = this.model.get("rangeOperator");
		this.$("#answerTypeId option").prop("disabled", false);
		if(answerType==String(Config.answerType.numeric)){ // numeric
			this.$(".stringSelectionDependent").hide();
			this.$(".numericDependent").show();
			this.$(".rangeDependent").hide();
			// disable all answer type changes except date/datetime
			this.$("#answerTypeId").prop("disabled", true);
			if(operatorType == String(Config.rangeOperator.isEqualTo)){  // is equal to
				this.$(".rangeDependent").show();
				this.$(".betweenDependent").hide();
			}
			else if(operatorType == String(Config.rangeOperator.lessThan)){ // less than
				this.$(".rangeDependent").show();
				this.$(".betweenDependent").hide();
			}
			else if(operatorType == String(Config.rangeOperator.greaterThan)){ // greater than
				this.$(".rangeDependent").show();
				this.$(".betweenDependent").hide();
			}
			else if(operatorType == String(Config.rangeOperator.between)){ // between
				this.$(".rangeDependent,.betweenDependent ").show();
			}
		}else if(answerType==String(Config.answerType.date)){ // date
			this.$(".stringSelectionDependent, .numericDependent, .rangeDependent").hide();
			this.$('#answerTypeId option[value="1"]').prop("disabled", true);
			this.$('#answerTypeId option[value="2"]').prop("disabled", true);
			this.calucationRuleValidation();
					
		}else if(answerType==String(Config.answerType.datetime)){ // date-time
			this.$(".stringSelectionDependent, .numericDependent, .rangeDependent").hide();
			this.$('#answerTypeId option[value="1"]').prop("disabled", true);
			this.$('#answerTypeId option[value="2"]').prop("disabled", true);
			this.calucationRuleValidation();
		
		}else if(answerType==String(Config.answerType.string)){ // string
			this.$("#answerTypeId").prop("disabled", true);
			this.$(".stringSelectionDependent").show();
			this.$(".numericDependent, .rangeDependent").hide();
			this.calucationRuleValidation();
			
		}else{
			this.$("#answerTypeId").prop("disabled", true);
			this.$(".stringSelectionDependent").hide();
			this.$(".numericDependent").hide();
			this.$(".rangeDependent").hide();
		}
	},
	
	renderFieldRequired : function() {
		if (this.model.get("required")) {
			this.$("#required").html("Yes");
		}
		else {
			this.$("#required").html("No");
		}
	},
	
	renderFieldRangeOperator : function() {
		var rangeOperatorValue = this.model.get("rangeOperator");
		var val1 = this.model.get("rangeValue1");
		var val2 = this.model.get("rangeValue2");
		var outputValue = "None";
		switch(rangeOperatorValue) {
			case "1": // fall through
			case 1:
				outputValue = "Equal To " + val1;
				break;
			case "2": // fall through
			case 2:
				outputValue = "Less Than " + val1;
				break;
			case "3": // fall through
			case 3:
				outputValue = "Greater Than " + val1;
				break;
			case "4": // fall through
			case 4:
				outputValue = "Between " + val1 + " and " + val2;
				break;
		}
		this.$("#rangeOperator").html(outputValue);
	}
};