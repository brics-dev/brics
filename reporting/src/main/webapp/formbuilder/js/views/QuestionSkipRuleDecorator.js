var QuestionSkipRuleDecorator = {
		commonsName : "QuestionSkipRuleDecorator",
		
		config : {
			name : "QuestionSkipRuleDecorator",
			render : "renderSkipRuleCommons"
		},
		
		events : {
			"change #skipRuleOperatorType" : "skipRuleOperatorTypeOnChange",	
			"click #addEditSkipQuestionsButton" : "addEditSkipQuestionsButtonOnClick",
			"change #skipRuleType" : "skipRuleTypeOnChange"
		},
		
		validationRules : [
           new ValidationRule({
        	   fieldName : "skipRuleEquals",
        	   description : Config.language.validateContainsSkipRuleEquals,
        	   
        	   match : function(model) {
        		   var skipRuleOperatorType = model.get("skipRuleOperatorType");
        		   
        		   if ( skipRuleOperatorType == 4 ) {
        			   var skipRuleEquals = model.get("skipRuleEquals");
        			   
        			   if ( skipRuleEquals.length != 0 ) {
            			   return true;
            		   }
        			   else {
            			   return false;
            		   }  
        		   }
        		   else {
        			   return true;
        		   } 
        	   } 
           }),
           
           new ValidationRule({
        	   fieldName : "skipRuleEquals",
        	   description : Config.language.validateEqualsSkipRuleEquals,
        	   
        	   match : function(model) {
        		   var skipRuleOperatorType = model.get("skipRuleOperatorType");
        		   
        		   if ( skipRuleOperatorType == 1 ) {
        			   var skipRuleEquals = model.get("skipRuleEquals");
        			   
        			   if ( skipRuleEquals.length != 0 ) {
            			   return true;
            		   }
        			   else {
            			   return false;
            		   }
        		   }
        		   else {
        			   return true;
        		   } 
        	   }   
           }),
           
           new ValidationRule({
        	   fieldName : "skipRuleType",
        	   description : Config.language.validateSkipRuleType,
        	   
        	   match : function(model) {
        		   var skipRuleOperatorType = model.get("skipRuleOperatorType");
        		   
        		   if ( skipRuleOperatorType != -2147483648 ) {
        			   var skipRuleType = model.get("skipRuleType");
        			   
        			   if ( skipRuleType != -2147483648 ) {
            			   return true;
            		   }
        			   else {
            			   return false;
            		   }
        		   }
        		   else {
        			   return true;
        		   } 
        	   }   
           }),
           
           new ValidationRule({
        	   fieldName : "divQuestionsToSkip",
        	   description : Config.language.validateSkipRuleQuestions,
        	   
        	   match : function(model) {
        		   var skipRuleOperatorType = model.get("skipRuleOperatorType");
        		   
        		   if (skipRuleOperatorType !=  -2147483648) {
        			   var questionsToSkip = model.get("questionsToSkip");
        			   
        			   if ( questionsToSkip.length  > 0 ) {
            			   return true;
            		   }
        			   else {
            			   return false;
            		   }
        		   }
        		   else {
        			   return true;
        		   } 
        	   }   
           })

		],
		
		renderSkipRuleCommons : function(model) {
			var type = this.model.get("questionType");

			if ( type === Config.questionTypes.radio || type === Config.questionTypes.select || 
					type === Config.questionTypes.checkbox || type === Config.questionTypes.multiSelect || type === Config.questionTypes.textbox ) {

				var linkHtml = TemplateManager.getTemplate("editQuestionSkipRuleTabLabel");
				var contentHtml = TemplateManager.getTemplate("editQuestionSkipRuleTab");
				this.addTab(linkHtml, contentHtml);
			}

			if ( typeof FormBuilder.page.get("skipQuestionsView") == "undefined" ) {
				FormBuilder.page.set("skipQuestionsView", new SkipQuestionsView());
			}
			
			var questionsToSkip = this.model.get("questionsToSkip");
			var skipRowsHtml = "";
			var skipRowTemplate;
			
			if ( questionsToSkip.length > 0 ) {
				var activeSectionId = FormBuilder.page.get("activeSection").get("id");
				
				
				FormBuilder.form.getSectionsInPageOrder().forEach(function(section) {
					var sectionQuestions = section.getQuestionsInPageOrder();
					var sIdOrdered = section.get("id");
					sectionQuestions.forEach(function(question) {
						var qIdOrdered  = question.get("questionId");
						
						for ( var i = 0; i < questionsToSkip.length; i++ ) {
							var sqId = questionsToSkip[i];
							var qId = Number(sqId.split('_Q_')[1]);
							var qModel = FormBuilder.form.findQuestionsWhere({questionId : qId})[0];
							var qName = qModel.get("questionName");
							var qText = qModel.get("questionText");
							var qType = qModel.get("questionType");
							var qTypeLabel = qModel.getQuestionTypeLabel(qType);
							
							
							
							var sId = sqId.split('_Q_')[0];
							
							
							if(sId==sIdOrdered && qId==qIdOrdered) {
								if(sId.substring(0, 2) == "S_"){
									sId = Number(sId.split('S_')[1]);
								}
								var sModel = FormBuilder.form.getSectionBySectionId(sId);
								
								//var sModel = FormBuilder.form.getQuestionParentSection(qModel);
								//var sId = sModel.get("id");
								var sName = sModel.get("name");
								
								
								if(sModel.get("isRepeatable") && (!(sId == activeSectionId))) {
								//if(sModel.get("isRepeatable")) {
									var index = sModel.getRepeatableIndex();
									sName = sName + "(" + index + ")";
								}
								var undeScoreIndex = qName.indexOf("_");
								qName = qName.substring(undeScoreIndex+1,qName.length)
								
								
								skipRowTemplate = TemplateManager.getTemplate("skipQuestionRow");
								skipRowsHtml = skipRowsHtml + skipRowTemplate({
									section : sName,
									questionName : qName,
									questionText : qText,
									questionType : qTypeLabel
								});
								break;
							}
							
							
						}
						
						
						
						
						
						
						
						
						
						
						
						
						
						
					});
				});
				
				
				
				
				
				
			}
			
			FormBuilder.page.get("activeEditorView").$(".divQuestionsToSkip").html(skipRowsHtml);
			
			var skipRuleOperatorType = this.model.get("skipRuleOperatorType");
			var $skipRuleType = this.$("#skipRuleType");
			var $skipRuleEquals = this.$("#skipRuleEquals");
			
			if ( skipRuleOperatorType != -2147483648 ) {
				// Enable skip rule
				$skipRuleType.prop("disabled", false);
				
				// Enable skip rule equals only if the skip rule operator type is "contains" or "equals"
				if ( skipRuleOperatorType == 1 || skipRuleOperatorType == 4 ) {
					$skipRuleEquals.prop("disabled", false);
				}
				else {
					$skipRuleEquals.prop("disabled", true);
				}
				
				// Enable add skip rule question button
				this. $("#addEditSkipQuestionsButton").prop("disabled", false);
			}
			else {
				//disable everything
				$skipRuleType.prop("disabled", true);
				$skipRuleEquals.prop("disabled", true);
				this.$("#addEditSkipQuestionsButton").prop("disabled", true);
			}
		},
		
		skipRuleOperatorTypeOnChange: function() {
			var $skipRuleOperatorType = this.$("#skipRuleOperatorType");
			var $skipRuleEquals = this.$("#skipRuleEquals");
			
			if ( ($skipRuleOperatorType.val() == 1) || ($skipRuleOperatorType.val() == 4) ) {
				$skipRuleEquals.prop("disabled", false);
		    }
			else {
				$skipRuleEquals.val("").prop("disabled", true);
		    }
			
			if ( $skipRuleOperatorType.val() == -2147483648 ) {
				this.model.set('skipRuleType', -2147483648);
				this.$("#skipRuleType").prop("disabled", true);
				
				// Clear the skipRuleEquals
				this.model.set('skipRuleEquals', "");
				
				// Clear the model questionsToSkip attribute
				this.model.set("questionsToSkip", []);
				
				// Clear the divQuestionToSkip in editor
				this.$(".divQuestionsToSkip").empty();
				
				// Disable the add/edit questions to skip button
				this.$("#addEditSkipQuestionsButton").prop("disabled", true);
			}
			else {
				this.$("#skipRuleType").prop("disabled", false);
			}
		},
		
		skipRuleTypeOnChange: function() {
			if (this.$('#skipRuleType').val() == -2147483648) {
				this.$("#addEditSkipQuestionsButton").prop("disabled", true);
			}
			else {
				this.$("#addEditSkipQuestionsButton").prop("disabled", false);
			}
		},
			
		addEditSkipQuestionsButtonOnClick: function() {
			var view = FormBuilder.page.get("skipQuestionsView");
			EventBus.trigger("open:processing", Config.language.loadSkipQuest);
			view.render(this.model);
		}
};