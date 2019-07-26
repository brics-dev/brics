/**
 * 
 */
var RepeatableSectionProcessor = {
	sections : null,
	
		
	init : function() {
		this.sections = FormBuilder.form.sections;
		
		// do not add add:question, that's for adding it to the questions
		// array, which this class will initiate at some points
		EventBus.on("create:repeatableQuestions", this.createQuestionInRepeatableSectoin, this);
		EventBus.on("delete:question", this.deleteQuestionInRepeatableSectoin, this);
		EventBus.on("change:question", this.editQuestion, this);
		EventBus.on("change:section", this.editSection, this);
		EventBus.on("delete:section", this.deleteSection, this);
	},
	
	/**
	 * Answers the question "is this section a repeatable parent?"
	 * In general: does this class need to work on it
	 * 
	 * @param sectionModel the section model to check
	 */
	isRepeatableParent : function(sectionModel) {
		return sectionModel.isRepeatableParent();
	},
	
	getRepeatableChildren : function(sectionModel) {
		return this.sections.getRepeatableChildren(sectionModel);
	},
	
	getRepeatableChildrenAlternative : function(sectionModel){
		return this.sections.where(
                {repeatedSectionParent : sectionModel.get("divId")}
				  );
	},
	
	/**
	 * Gets this same question in all repeated children of sectionModel
	 * 
	 * @param questionModel the question to find in repeated sections
	 * @param sectionModel the repeated parent section
	 * @returns Array of repeated questions (if any)
	 */
	getQuestionRepeatedCopies : function(questionModel, sectionModel) {
		var output = [];
		var childSections = this.getRepeatableChildren(sectionModel);
		childSections.forEach(function(sect) {
			var question = sect.getQuestionByName(questionModel.get("questionName"));
			if (typeof question !== "undefined") {
				output.push(question);
			}
		});
		return output;
	},
	
	/**
	 * Produces a cloned question, with repeatable changes in place, for
	 * the given section
	 * @param questionModel
	 * @returns
	 */
	makeClonedQuestion : function(questionModel, sectionModel) {
		var clonedQuestion = new Question(_.clone(questionModel.attributes));
		var oldDiveId = questionModel.get("newQuestionDivId");
		clonedQuestion.set("sectionId", sectionModel.get("id"));
		clonedQuestion.set("newQuestionDivId", sectionModel.get("id")+oldDiveId.substr(oldDiveId.indexOf("_"),oldDiveId.length));
		clonedQuestion.set("active", false);
		clonedQuestion.set("qaId", "-7");
		clonedQuestion.set("vsId", "-7");
		clonedQuestion.set("etId", "-7");
		clonedQuestion.set("sqId", "-7");
		clonedQuestion.set("crId", "-7");
		clonedQuestion.set("srId", "-7");
		clonedQuestion.set("userFileId", "-7");
		clonedQuestion.set("etValId", "-7");

		
		
		this.updateSkipRule(clonedQuestion, sectionModel);
		this.updateCalcRule(questionModel, sectionModel);
		
		return clonedQuestion;
	},
	
	makeClonedQuestions : function(questions, sectionModel){
		 var result = new Questions();
		 var mainThis = this;
		 questions.forEach(function(eachQuestion){
			var q = mainThis.makeClonedQuestion(eachQuestion,sectionModel);
			result.add(q);
		});
		
		return result;
	},
	
	/**
	 * A sections (possibly repeatable parent) has added a question.
	 * Processes that add to potentially add that question to child
	 * repeatable sections
	 * 
	 * @param questionModel the new question model
	 * @param sectionModel (optional) the section model
	 */
	createQuestionInRepeatableSectoin : function(questionModel, sectionModel) {
		if (typeof sectionModel !== "object") {
			sectionModel = FormBuilder.form.getQuestionParentSection(questionModel);
		}
		
		if (this.isRepeatableParent(sectionModel)) {
			var children = this.getRepeatableChildren(sectionModel);
			mainThis = this;
			_.each(children, function(childSection) {
				var clonedQuestion = mainThis.makeClonedQuestion(questionModel, childSection);
				childSection.addQuestion(clonedQuestion);
			});
		}
	},
	
	/**
	 * A section (possibly repeatable parent) has removed a question.
	 * Process that removal to potentially remove that question from
	 * child repeatable sections
	 * 
	 * @param questionModel the question being removed
	 * @param sectionModel the section from which it is being removed
	 */
	deleteQuestionInRepeatableSectoin : function(questionModel, sectionModel) {
		if (sectionModel == null || typeof sectionModel !== "object") {
			sectionModel = FormBuilder.form.getQuestionParentSection(questionModel);
		}
		
		if (this.isRepeatableParent(sectionModel)) {
			var children = this.getRepeatableChildren(sectionModel);
			_.each(children, function(childSection) {
				var sectionsQuestion = childSection.getQuestionByName(questionModel.get("questionName"));
				childSection.removeQuestion(sectionsQuestion);
			});
		}
	},
	
	updateQuestionAttribute : function(questionModel){
		var parentSection = FormBuilder.form.section(questionModel.get("sectionId"));
		if(parentSection.isRepeatableParent){
			var children = this.getRepeatableChildren(parentSection);
			_.each(children, function(child) {
				var t = questionModel.get("questionId");
				var qModel = child.getQuestionById(questionModel.get("questionId"));
				qModel.set("body", questionModel.get("body"));
				qModel.set("vAlign", questionModel.get("vAlign"));
				qModel.set("subject", questionModel.get("subject"));
				qModel.set("hasUnitConversionFactor", questionModel.get("hasUnitConversionFactor"));
				qModel.set("maxCharacters", questionModel.get("maxCharacters"));
				qModel.set("prepopulation", questionModel.get("prepopulation"));
				qModel.set("toEmailAddress", questionModel.get("toEmailAddress"));
				qModel.set("answerType", questionModel.get("answerType"));
				qModel.set("questionsToCalculate", questionModel.get("questionsToCalculate"));
				qModel.set("ccEmailAddress", questionModel.get("ccEmailAddress"));
				qModel.set("unitConversionFactor", questionModel.get("unitConversionFactor"));
				qModel.set("dataElementName", questionModel.get("dataElementName"));
				qModel.set("questionsToSkip", questionModel.get("questionsToSkip"));
				qModel.set("textareaWidth", questionModel.get("textareaWidth"));
				qModel.set("calculation", questionModel.get("calculation"));
				qModel.set("horizontalDisplay", questionModel.get("horizontalDisplay"));
				qModel.set("htmlText", questionModel.get("htmlText"));
				qModel.set("rangeOperator", questionModel.get("rangeOperator"));
				qModel.set("eMailTriggerId", questionModel.get("eMailTriggerId"));
				qModel.set("deleteTrigger", questionModel.get("deleteTrigger"));
				qModel.set("required", questionModel.get("required"));
				qModel.set("answerTypeDisplay", questionModel.get("answerTypeDisplay"));
				qModel.set("rangeValue2", questionModel.get("rangeValue2"));
				qModel.set("rangeValue1", questionModel.get("rangeValue1"));
				qModel.set("calDependent", questionModel.get("calDependent"));
				qModel.set("conversionFactor", questionModel.get("conversionFactor"));
				qModel.set("eMailTriggerUpdatedBy", questionModel.get("eMailTriggerUpdatedBy"));
				qModel.set("indent", questionModel.get("indent"));
				qModel.set("textboxLength", questionModel.get("textboxLength"));
				qModel.set("skipRuleEquals", questionModel.get("skipRuleEquals"));
				qModel.set("align", questionModel.get("align"));
				qModel.set("fontFace", questionModel.get("fontFace"));
				qModel.set("prepopulationValue", questionModel.get("prepopulationValue"));
				qModel.set("fontSize", questionModel.get("fontSize"));
				qModel.set("skipRuleType", questionModel.get("skipRuleType"));
				qModel.set("decimalPrecision", questionModel.get("decimalPrecision"));
				qModel.set("qType", questionModel.get("questionType"));
				qModel.set("skipRuleOperatorType", questionModel.get("skipRuleOperatorType"));
				qModel.set("calculatedQuestion", questionModel.get("calculatedQuestion"));
				qModel.set("conditionalForCalc", questionModel.get("conditionalForCalc"));
				qModel.set("textareaHeight", questionModel.get("textareaHeight"));
				qModel.set("dataSpring", questionModel.get("dataSpring"));
				qModel.set("color", questionModel.get("color"));
				qModel.set("calculationType", questionModel.get("calculationType"));
				qModel.set("horizDisplayBreak", questionModel.get("horizDisplayBreak"));
				qModel.set("minCharacters", questionModel.get("minCharacters"));
				qModel.set("skipRuleDependent", questionModel.get("skipRuleDependent"));
				qModel.set("skiprule", questionModel.get("skiprule"));
			});
		}
	},
	
	makeClonedSection : function(sectionModel, i) {
		var clonedSection = new Section(_.clone(sectionModel.attributes));
		var paretSectionId = sectionModel.get("id");
		var childSectionId = FormBuilder.form.sections.getNextSectionId();
		clonedSection.set("id", childSectionId);
		clonedSection.set("divId", childSectionId);
		clonedSection.set("isRepeatable", true);
		clonedSection.set("existingSection", false);
		clonedSection.set("repeatedSectionParent", paretSectionId);
		clonedSection.questions = this.makeClonedQuestions(sectionModel.questions,clonedSection);
		
		return clonedSection;
	},
	
	deleteSection : function(sectionModel) {
		if(sectionModel.get("isRepeatable")){
			var children = this.getRepeatableChildren(sectionModel);
			_.each(children, function(child) {
				FormBuilder.form.sections.remove(child);
				if(sectionModel.get("existingSection")){
					FormBuilder.form.deleteSections.create(child);
				}
			});
		}
	},
	
	checkCanDelete : function(sectionModel) {
		var children = this.getRepeatableChildrenAlternative(sectionModel);
		if (children.length < 1) {
			return true;
		}
		return !this.childHasCalcRule(sectionModel, children) && !this.childHasSkipRule(sectionModel, children);
	},
	
	editSection : function(sectionModel) {
		if(sectionModel.get("isRepeatable")){
			this.toRepeatable(sectionModel);
		}else{
			this.notToRepeatable(sectionModel);
		}
	},
	
	toRepeatable : function(sectionModel) {		
		var children = this.getRepeatableChildren(sectionModel);
		
		if (children.length == 0) {
			var max = sectionModel.get("maxRepeatedSecs");
			
			for (var i = 1; i < max; i++) {
				FormBuilder.form.addSection(this.makeClonedSection(sectionModel, i));
			}
			// regenerate the children list since we just added them
			children = this.getRepeatableChildren(sectionModel);
		}

		_.each(children, function(child) {
			child.set("name", sectionModel.get("name"));
			child.set("description", sectionModel.get("description"));
			child.set("isCollapsable", sectionModel.get("isCollapsable"));
			child.set("ifHasResponseImage", sectionModel.get("ifHasResponseImage"));
			child.set("initRepeatedSecs", sectionModel.get("initRepeatedSecs"));
			child.set("maxRepeatedSecs", sectionModel.get("maxRepeatedSecs"));
			child.set("repeatableGroupName", sectionModel.get("repeatableGroupName"));
			child.set("gridtype", sectionModel.get("gridtype"));
			child.set("tableGroupId", sectionModel.get("tableGroupId"));
			child.set("tableHeaderType", sectionModel.get("tableHeaderType"));
			child.set("isManuallyAdded", sectionModel.get("isManuallyAdded"));
		});
		
		var changedSet = StateManager.changeSet["maxRepeatedSecs"];
		
		if (typeof changedSet !== "undefined") {
			this.editSectionRepeatMax(sectionModel, children, changedSet.previousValue);
		}
	},
	
	notToRepeatable : function(sectionModel) {
		// the repeatable children alternative is used in the case where
		// children can exist BUT the parent section may not YET be marked
		// as repeatable.
		var children = this.getRepeatableChildrenAlternative(sectionModel);
		_.each(children, function(child) {
			FormBuilder.form.sections.remove(child);
			
			if (sectionModel.get("existingSection")) {
				FormBuilder.form.deleteSections.create(child);
			}
		});
	},
		
	editSectionRepeatMax : function(sectionModel, repeatChildren, previous) {
		if(Number(sectionModel.get("maxRepeatedSecs")) > Number(previous)) {
			this.maxRepeatIncrease(sectionModel,Number(sectionModel.get("maxRepeatedSecs"))-Number(previous));			
		}
		else {
			this.maxRepeatDecrease(sectionModel, repeatChildren, Number(previous)-Number(sectionModel.get("maxRepeatedSecs")));			
		}
	},
	
	maxRepeatIncrease : function(sectionModel, difference) {
		for (var i = 1; i < difference+1; i++) {
			FormBuilder.form.addSection(this.makeClonedSection(sectionModel, i));
		}
	},
	
	/**
	 * Removes difference number of repeatable children sections from the
	 * list of children of the parent section
	 * 
	 * @param sectionModel parent section model
	 * @param repeatChildren repeated child sections
	 * @param difference change in max number
	 */
	maxRepeatDecrease : function(sectionModel, repeatChildren, difference) {
		for (var i = 0; i < difference; i++) {
			FormBuilder.form.sections.remove(repeatChildren[i]);
			
			if (sectionModel.get("existingSection")) {
				FormBuilder.form.deleteSections.create(repeatChildren[i]);
			}
		}
	},
	
	updateChildeQuestionId : function(parentQmodel){
		var parentSection = FormBuilder.form.section(parentQmodel.get("sectionId"));
		if(parentSection.isRepeatableParent){
			var children = this.getRepeatableChildren(parentSection);
			var childQ;
			_.each(children, function(child) {
				childQ = child.getNewQuestion();
				if(typeof childQ !== 'undefined'){
					childQ.set("questionId",parentQmodel.get("questionId"));
				}
			});
		}
	},
	
	/**
	 * Responds to a changing question Model.  Will propagate any needed changes
	 * to all repeatable child questions.
	 * 
	 * NOTE: the questionModel here is normally, though not always, the change
	 * event model (with changing = true).  So, first check there instead of
	 * the stateManager.
	 * 
	 * @param questionModel the model being changed
	 */
	editQuestion : function(questionModel) {
		// are there repeatable questions?
		var parentSection = FormBuilder.form.getQuestionParentSection(questionModel);
		var parentQuestionType = questionModel.get("questionType");
		var questionTypes = Config.questionTypes;
		if (parentSection != null && parentSection.get("isRepeatable") == true) {
			var otherRepeatedQuestions = this.getQuestionRepeatedCopies(questionModel, parentSection);

			var changes = StateManager.changeSet;
			if (questionModel._changing) {
				changes = questionModel.changedAttributes();
			}
			
			var repeatChanges = {};
			for (var attr in changes) {
				if (questionModel.defaults.hasOwnProperty(attr) != -1 
						&& attr != "sectionId"
						&& attr != "newQuestionDivId"
						&& attr != "active") {
					repeatChanges[attr] = questionModel.get(attr);
				}
			}
			
			// most values are copied to repeated questions
			for (var i = 0; i < otherRepeatedQuestions.length; i++) {
				var quest = otherRepeatedQuestions[i];
				quest.set(repeatChanges);
				
				if (repeatChanges.hasOwnProperty("questionType")) {
					//FormBuilder.pageView.changeQuestionType(quest);
					
					 quest.set("questionType",parentQuestionType);
					 if(parentQuestionType === questionTypes.visualscale) {
						 if (typeof(quest.get("rangeValue1"))!='undefined' && typeof(quest.get("rangeValue2"))!='undefined') {
						    	var operatorType = quest.get("rangeOperator");		    	
						    	if(operatorType == Config.rangeOperator.isEqualTo){
						    		quest.set("vscaleRangeStart", String(quest.get("rangeValue1")));
						    		quest.set("vscaleRangeEnd",   String(quest.get("rangeValue1")));
						    	}
						    	else if(operatorType == Config.rangeOperator.lessThan){
						    		if(quest.get("vscaleRangeStart") == "-99999") {
						    			quest.set("vscaleRangeStart", "0");
						    		}
						    		quest.set("vscaleRangeEnd",   String(quest.get("rangeValue1")));
						    	}
						    	else if(operatorType == Config.rangeOperator.greaterThan){
						    		quest.set("vscaleRangeStart", String(quest.get("rangeValue1")));
						    		if(quest.get("vscaleRangeEnd") == "-99998") {
						    			quest.set("vscaleRangeEnd", "5000");
						    		}
						    	}
						    	else if(operatorType == Config.rangeOperator.between){
						    		quest.set("vscaleRangeStart", String(quest.get("rangeValue1")));
						    		quest.set("vscaleRangeEnd",   String(quest.get("rangeValue2")));
						    	}else{
						    		if(quest.get("vscaleRangeStart") == "-99999" && quest.get("vscaleRangeEnd") == "-99998") {
						    			quest.set("vscaleRangeStart", "1");
						    			quest.set("vscaleRangeEnd",   "100");
						    		}
						  		}                 
						      }
					 }else if(parentQuestionType === questionTypes.textbox || parentQuestionType === questionTypes.textare) {
						 //set visual scale back to defaults
						 quest.set("vscaleRangeStart", "1");
						 quest.set("vscaleRangeEnd", "100");
						 quest.set("vscaleWidth", "100");
						 quest.set("vscaleShowHandle", true);
						 quest.set("vscaleCenterText", "");
						 quest.set("vscaleLeftText", "");
						 quest.set("vscaleRightText", "");
					 }
					
				}
			}
		}
	},
	
	childHasCalcRule : function(sectionModel, children) {
		var foundMatch = false;
		// because children doesn't have a push method???
		children[children.length] = sectionModel;
		for (var i = 0; i < children.length; i++) {
			if (children[i].hasCalculationQuestion()) {
				foundMatch = true;
				break;
			}
		};
		return foundMatch;
	},
	
	childHasSkipRule : function(sectionModel, children) {
		var foundMatch = false;
		// because children doesn't have a push method???
		children[children.length] = sectionModel;
		for (var i = 0; i < children.length; i++) {
			if (children[i].hasSkipQuestion()) {
				foundMatch = true;
				break;
			}
		};
		return foundMatch;
	},
	
	/**
	 * Updates skip rule references in the case of repeatable groups
	 * 
	 * @param questionModel the question to update
	 * @param targetSectionModel the question's new section model
	 */
	updateSkipRule : function(questionModel, targetSectionModel) {
		// look at all sections in the page
		var sections = FormBuilder.form.sections;
		sections.forEach(function(section) {
			
			// only look at repeatable parents, we don't care about others
			if (section.get("isRepeatable") && section.get("repeatedSectionParent") == "-1") {
				var secId = section.get("id");
				//var questions = section.get("questions").models;
				section.questions.forEach(function(question) {

					if (question.get("skipRuleType") != -2147483648) {
						// this question has a skip rule	
						var repeatedQuestions = RepeatableSectionProcessor.getQuestionRepeatedCopies(question, section);
						var questionsToSkip = question.get("questionsToSkip");
						for (var j = 0; j < questionsToSkip.length; j++) {
							if (questionsToSkip[j].indexOf(secId) != -1) {
								for (var m = 0; m < repeatedQuestions.length; m++) {
									var repQuestion = repeatedQuestions[m];
									var repSectId = repQuestion.get("sectionId");
									// replace the parent section ID with the question's immediate section ID
									var repQuestionsToSkip = repQuestion.get("questionsToSkip").slice(0);
									
									
									
									
									for(var p = 0; p < repQuestionsToSkip.length ; p++) {
										
										repQuestionsToSkip[p] = repQuestionsToSkip[p].replace(secId, repSectId);
										
									}
									

									repQuestion.set("questionsToSkip",repQuestionsToSkip)
								}
							}
						}
					}
				});
			} // end if
		}); // end for
	},
	
	/**
	 * Looks for repeated sections and, if any are found, looks for questions
	 * within them that have calculation rules based on the same section.  Changes
     * the references to always point to the current section instead of the
     * repeated parent.
	 * 
	 * @param questionModel the question to update
	 * @param targetSectionModel the question's new section model
	 */
	updateCalcRule : function(questionModel, targetSectionModel) {
		// look at all sections in the page
		var sections = FormBuilder.form.sections;
		sections.forEach(function(section) {
			
			// only look at repeatable parents, we don't care about others
			if (section.get("isRepeatable") && section.get("repeatedSectionParent") == "-1") {
				var secId = section.get("id");
				//var questions = section.get("questions").models;
				section.questions.forEach(function(question) {
					
					if (question.get("calculatedQuestion") && question.get("calculation").indexOf(secId) != -1) {
						// this question has a calculation rule and it contains a reference to this section
						
						var repeatedQuestions = RepeatableSectionProcessor.getQuestionRepeatedCopies(question, section);
						for (var j = 0; j < repeatedQuestions.length; j++) {
							var repQuestion = repeatedQuestions[j];
							var repSectId = repQuestion.get("sectionId");
							var calculation = repQuestion.get("calculation");
							
							// replace the parent section ID with the question's immediate section ID
							//does the replaceall version of: calculation = calculation.replace(secId, repSectId);
							calculation = _.replaceAll(calculation, secId, repSectId);
							
							repQuestion.set("calculation",calculation);
						}
					}	
				});
			} // end if
		}); // end for
	} // end function
};