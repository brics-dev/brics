/**
 * Effectively a snippet class that will be merged into the concrete Question
 * View class. The properties here will NOT overwrite anything in the concrete
 * class.
 * 
 * Email Notifications only matter for select type question
 * 
 */
var QuestionEmailDecorator = {
		
	operatorAllowed : false,
	numAllow : true,
	qAllow : true,
	anyOrder : true,
	comparatorAllowed: false,
	logicalOperatorAllowed: false,
	errorString : "",
		
	commonsName : "QuestionEmailDecorator",

	validationRules : [
	// address format
	new ValidationRule({
		fieldName : "toEmailAddress",
		description : Config.language.emailAddress,
		match : function(model) {
			var isValid = true;
			var emailReg = /^([\w-\.]+@([\w-]+\.)+[\w-]{2,4})?$/;
			var toEmailAddresses = model.get("toEmailAddress");
			var toEmailAddressArr = toEmailAddresses.split(/[ ;,]+/);
			for(var i = 0; i < toEmailAddressArr.length; i++){
				if(!emailReg.test(toEmailAddressArr[i]) ){
					isValid = false;
				}
			}
			return isValid;
		}
	}), new ValidationRule({
		fieldName : "ccEmailAddress",
		description : Config.language.emailAddress,
		match : function(model) {
			var isValid = true;
			var emailReg = /^([\w-\.]+@([\w-]+\.)+[\w-]{2,4})?$/;
			var ccEmailAddresses = model.get("ccEmailAddress");
			var ccEmailAddressArr = ccEmailAddresses.split(/[ ;,]+/); //split into array based on space, semicolon or comma
			for(var i = 0; i < ccEmailAddressArr.length; i++){
				if(!emailReg.test(ccEmailAddressArr[i])) {
					isValid = false;
				}
			}
			return isValid;
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
				var type = model.get("questionType");
				var questionTypes = Config.questionTypes;
				//for these four types of question, triggerAnswers field is required
				if ((type == questionTypes.radio || type == questionTypes.checkbox 
						||type == questionTypes.select || type == questionTypes.multiSelect) 
					&& triggerAnswers.length == 0) {
					return false;
				} else {
					return true;
				}
			} else {
				return true;
			}
		}
	}), new ValidationRule({
 	   fieldName : "conditionsDisplay",
 	   description : "",
 	   match : function(model) {
 		  if (model.get("emailTrigger")) {
			   var type = model.get("questionType");
			   var questionTypes = Config.questionTypes;
			   var answerType = model.get("answerType");
	
			   if(type == questionTypes.textbox && answerType == 2){ 
				   //framing the id of question to edit in the format S_secid_Q_qtnId
				   var triggerValues = model.get("triggerValues");
				   if (triggerValues.length == 0 ) {
					   this.description = "Please add a trigger condition"
					   return false;
				   } else {
					    var conditions = triggerValues[0].etCondition;
			 			var eqStr = triggerValues.length == 0 ? "" : triggerValues[0].etCondition;
			 			var findQuestionsAll = /\[S_[-]?\d+_Q_([0-9]+)\]/g;
			 			
			 			var count = 0;
			 		    var operators = /[+-\/%*|&=!><>]/;
			 		    
			 		    if (conditions.length > 0) {
			 		        chr = conditions.charAt(0);
			 		        if (teststr = operators.exec(chr)) {
			 		        	this.description = Config.language.operrator;
			 		        	return false;
			 		        }

			 		        var comparators = /[=><]/;
		 		        	if(!conditions.toString().match(comparators)) {
		 		        		this.description = "Email Trigger is missing a comparator. Please add at least one comparator in email trigger.";
			 		        	return false;
		 		        	}		
			 			    var divbyzero = /\s\/\s0+\s+/g;
			 			    if (teststr = divbyzero.exec(conditions+" ")) {
			 			    	this.description = Config.language.divisionZero;
			 			        return false;
			 			    }
			 		
			 			    var parenStr = this.testParen(eqStr);
			 				if (parenStr != null && parenStr.length > 0) {
			 					this.description = parenStr;
			 					return false;
			 				}
			 		
			 				var noquestion = /\[/;
							if(!conditions.toString().match(noquestion)){
								this.description = Config.language.noquestion;
								return false;
							}
			 		    }
			 		    
			 		   var sId = model.get("sectionId");
					   var qId = "_Q_"+model.get("questionId");
					   var qstnToEdit = sId + qId;
					   		   
					   var conditionArray = this.getConditionInArray(conditions);
					   var allQtnsCondition = {};
					   
					   //get calculation rule of all questions in eform and put it in object
					   FormBuilder.form.getSectionsInPageOrder().forEach(function(section) {
						   var sectionQuestions = section.getQuestionsInPageOrder();
						   sectionQuestions.forEach(function(question) {
							   var conditions = triggerValues.length == 0 ? "" : triggerValues[0].etCondition;
							   if(conditions != null && conditions.trim()!='') {
								   allQtnsCondition[section.get("id")+"_Q_"+question.get("questionId")] = conditions;
							   }		                				   
						   });
						   
					   });
					   
			 		    return true;
				   }

			   } else {
				   return true;
			   }
		   } else {
			   return true;
		   }
 	   },
 	   testParen : function(x) {
 			var str = new String(x);
 		    var left = 0;
 		 
 		    for (i=0; i<str.length; i++) {
 		        chr = str.charAt(i);
 		        if (chr == "(" ) {
 		            left++;
 		        } else if (chr == ")") {
 		            if (left < 1) {
 		                return "Unmatched right parentheses";
 		            } else {
 		                left--;
 		            }
 		        }
 		    }
 		    if (left != 0) {
 		        return "Unmatched left parentheses";
 		    }
 		},
 	  getConditionInArray : function(condition){
		   var conditionArray = new Array();
		   var findAllQstns = /\[S_[-]?\d+_Q_([0-9]+)\]/g;
		   
		   //Get questions used in the calculation rule and put it in array
		   if(!!condition){
   		   while(result = findAllQstns.exec(condition)){
   			   result[0] = result[0].replace("[","");
   			   result[0] = result[0].replace("]","");
   			   if(conditionArray.indexOf(result[0]) == -1){
   				   conditionArray.push(result[0]);
   			   }
   		   }
	   		}
		   return conditionArray;
	   },	   
	   getDataElementName	: function(secqtn){
		   var dename = "";
		   var pattern = /S_[-]?(\d+)_Q_([0-9]+)/g;
		   matches = pattern.exec(secqtn);
		   
		   if(!!matches && matches.length == 3){			   
			   var secId = "S_"+matches[1];
			   var qtnId = matches[2];
			   var section = FormBuilder.form.sections.findWhere({ id: secId});
			   var question = section.questions.findWhere({ questionId: Number(qtnId) });			   
			   dename = question.get("dataElementName");
		   }
		   return dename;
	   }
 	  })  
	// trigger can't be empty
	],

	mainThis : null,

	events : {
		"change #createTrigger"   	: "showHideEmailTrigger",
		
		"click #etBackspace":"etBackspace",
		"click #etClear":"etClear",
		"click #etPercent":"etPercent",
		"click #etFlip":"etFlipsign",
		
		"click #et0" : function(){this.etNumber('0');},
		"click #et1" : function(){this.etNumber('1');},
		"click #et2" : function(){this.etNumber('2');},
		"click #et3" : function(){this.etNumber('3');},
		"click #et4" : function(){this.etNumber('4');},
		"click #et5" : function(){this.etNumber('5');},
		"click #et6" : function(){this.etNumber('6');},
		"click #et7" : function(){this.etNumber('7');},
		"click #et8" : function(){this.etNumber('8');},
		"click #et9" : function(){this.etNumber('9');},
		"click #etDot" : function(){this.etNumber('.');},
		
		"click #etDivide" : function(){this.etButton('/');},
		"click #etMult" : function(){this.etButton('*');},
		"click #etSubt" : function(){this.etButton('-');},
		"click #etAdd" : function(){this.etButton('+');},
		
		"click #etSqrt" : function(){this.etParen(' sqrt(');},
		"click #etLeftP" : function(){this.etParen(' (');},
		"click #etRightP" : function(){this.etParen(') ');},
				
		"click #etEqualEqual": function(){this.etComparator('==');},
		"click #etNotEqual": function(){this.etComparator('!=');},
		"click #etGreater": function(){this.etComparator('>');},
		"click #etGreaterEqual": function(){this.etComparator('>=');},		
		"click #etLess": function(){this.etComparator('<');},
		"click #etLessEqual": function(){this.etComparator('<=');},
		
		"click #etAnd": function(){this.etLogicalOperator('&&');},
		"click #etOr": function(){this.etLogicalOperator('||');},
		
		"click #triggerQuestion":"addTriggerConditionQuestion"

	},
	/**
	 * Renders this commons to the editor
	 * 
	 * @param model
	 *            the question model
	 */
	renderEmailCommons : function(model) {
		mainThis = this;

		var type = this.model.get("questionType");
		var answerType = this.model.get("answerType");
		var questionTypes = Config.questionTypes;
		
		//Only predefined questions or question type is text box and answer type is numeric can have email trigger
		if(type == questionTypes.radio || type == questionTypes.checkbox 
				||type == questionTypes.select || type == questionTypes.multiSelect 
				|| (type == questionTypes.textbox && answerType == 2)) {
			//Answers to trigger selection box will only show if the question type is one of followings.
			if(type == questionTypes.radio || type == questionTypes.checkbox 
					||type == questionTypes.select || type == questionTypes.multiSelect) {
				//populate values from triggerValues to triggerAnswers
				this.getTriggerAnswers(model);
				// the below shows an example of how to listen for changes to the model
				model.on("change:questionOptionsObjectArray", this.showTriggerAnswers, this.model);			
			}
			
			var linkHtml = TemplateManager.getTemplate("editQuestionEmailTabLabel");
			var content = TemplateManager.getTemplate("editQuestionEmailTab");
			var contentHtml = content({"model": this.model}); //passing model to email trigger template - editorEmail.jsp
			this.addTab(linkHtml, contentHtml);

			if(this.model.get("emailTrigger")){
				this.$("#createTrigger").attr('checked', true);
			}
			this.showHideEmailTrigger();
			
			this.showTriggerAnswers(model);
			this.model.set("eMailTriggerUpdatedBy", Config.userId);
			//answerType is numeric and populate questions for trigger condition
			//the list of the questions is the same as the one in calculation rule tab
			if(type == questionTypes.textbox && answerType == 2){ 			
				this.onChangeAnswerTypeInTrigger();
				this.listenTo(this.model, "change:answerType", this.onChangeAnswerTypeInTrigger);
			}
		}
		
	},

	config : {
		name : "QuestionEmailDecorator",
		render : "renderEmailCommons"
	},

	showTriggerAnswers : function(model) {
		var mainThis = FormBuilder.page.get("activeEditorView");
		
		var type = model.get("questionType");
		var answerType = model.get("answerType");
		var questionTypes = Config.questionTypes;
		if((type == questionTypes.radio || type == questionTypes.checkbox 
				|| type == questionTypes.select || type == questionTypes.multiSelect)) {
			var questionOptionsObjectArray = this.model.get("questionOptionsObjectArray"); 
			var triggerAnswers = this.model.get("triggerAnswers");
			
			mainThis.$("#triggerAnswers").html('');
			if (triggerAnswers == null) {
				triggerAnswers = [];
			}
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
				}
				
				for (var i=0; i<triggerAnswers.length; i++) {
					if((typeof triggerAnswers[i]=="object") &&triggerAnswers[i]!=null ){
						var val = $.trim(triggerAnswers[i]);
						var $objectVal = mainThis.$("#triggerAnswers").find("option[value='"+val+"']").css('background-color','grey');
					}
				}
			}
		} else if(type == questionTypes.textbox && answerType == 2) {
			var triggerValues = model.get("triggerValues");
			if(triggerValues.length > 0) {
				//for this type of question, there is only one condition for a trigger
				var str = this.getTriggerCondition(triggerValues);
				this.resetAllows(str);
				var str2;
				if(str != "") {
					str2 = this.model.reformatCalculationDisplay(str);
					//console.log($('#conditionsDisplay'));
			        mainThis.$('#conditionsDisplay').html(str2);
				}
			}
		}

	},

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
		model.set("triggerValues", []);
		this.etClear();
//		console.log("deleteTrigger model: "+JSON.stringify(model));
	},
	
	populateQuestionsToEmailTrigger : function() {
	
		var activeSection = FormBuilder.page.get("activeSection");
		var activeSectionId = activeSection.get("id");
		var activeQuestionId = FormBuilder.page.get("activeQuestion").get("questionId");
		var activeQuestionAnswerType = FormBuilder.page.get("activeQuestion").get("answerType");
		
		var availbleQ='<select id="triggerQuestion" multiple  style="height: 140px; overflow: scroll;border: solid 1px">';
		availbleQ += '<option value="thisQuestion_' + activeSectionId + "_Q_" + activeQuestionId + '">This Question</option>';
		FormBuilder.form.getSectionsInPageOrder().forEach(function(section) {
			
			var optGroup='';
	
			if(!activeSection.get("isRepeatable") ||  !section.get("isRepeatable") || section.get("repeatedSectionParent") != activeSectionId) {
				// if active section is repeatable, dont show any of its own child repeatable questions.   
				var sectionQuestions = section.getQuestionsInPageOrder();
				
				if(sectionQuestions.length > 0) {
					var sId = section.get("id");
					var sName = section.get("name");
					if(section.get("isRepeatable") && (!(sId == activeSectionId))) {
						var index = section.getRepeatableIndex();
						optGroup = '<optgroup label="'  +  sName  +  '(' +   index + ')' +   '" style="font-weight: bold;">';
						
					} else {
						optGroup = '<optgroup label="'+sName+'" style="font-weight: bold;">';
						
					}
					var options = '';
					sectionQuestions.forEach(function(question) {
						var qId  = question.get("questionId");
						//we dont want to include the active question in the list
						if(!(sId == activeSectionId && qId == activeQuestionId)) {
	
							var qType = question.get("questionType");
							var aType = question.get("answerType");
							var questionOptionsObjectArray = question.get("questionOptionsObjectArray");
															
							var scoreAvailable="false";
								
							if(questionOptionsObjectArray.length > 0) {
								for(var i=0;i<questionOptionsObjectArray.length;i++) {
									var questionOptionsObject = questionOptionsObjectArray[i];
									var score = questionOptionsObject.score;
									if(score != null && score.trim()!='') {
										scoreAvailable="true";
										break;
									}
								}
							}
							
							if((qType == '1' && aType == activeQuestionAnswerType) || 
									(qType == '3' &&  scoreAvailable=='true') ||
										(qType == '4' &&  scoreAvailable=='true')) {
								var sqId = sId + '_Q_' + qId;
								var qName = question.get("questionName");
								var qName_short = "";
								if(""!=qName) {
									qName_sht = qName.replace(/^[0-9]+_/, "");
							    }
								var qText = question.get("questionText");
								var qType = question.get("questionType");
								var qTypeLabel = question.getQuestionTypeLabel(qType);

								options+='<option class="calcOpt" value="'+sId+':'+qId+':'+sName+':'+qName +'">&nbsp;&nbsp;* '+qName_sht+'</option>';
								
							}
						}
						
					});	
					if(options != '') {
						optGroup = optGroup + options + '</optgroup>';
						availbleQ+=optGroup;
					}
					
				}
			}
		});
		
		availbleQ += '</select>'; //console.log("populateQuestionsToEmailTrigger availbleQ: "+availbleQ);
		$('#availableQuestionsToEmailDiv').html(availbleQ);
	},
	
	getTriggerAnswers: function(model) {
		var triggerAnswers = [];
		var triggerValues = model.get("triggerValues");
		for(var i = 0; i < triggerValues.length; i++){
			triggerAnswers.push($.trim(triggerValues[i].etAnswer));
		}
		if(triggerAnswers.length == 0){
			triggerAnswers = this.model.get("triggerAnswers");
		}
		model.set("triggerAnswers", triggerAnswers);
		//console.log("getTriggerAnswers triggerAnswers: "+JSON.stringify(triggerAnswers));
	},

	TriggerValue : function (etValId, etAnswer, etCondition) {	
		this.etValId = etValId;
		this.etAnswer = etAnswer;
		this.etCondition = etCondition;
	},
	
	onChangeAnswerTypeInTrigger : function() {
		var answerType = this.model.get("answerType");
		if(answerType == 2) {
			//if answer type is numeric, show tab
			this.enableTab('dialog_editQuestion_email');
			this.populateQuestionsToEmailTrigger();			
		} else {
			//otherwise hide it
			//hide tab
			this.disableTab('dialog_editQuestion_email');
		}
	},
		
	etReset : function() {
		this.numAllow = true;
		this.qAllow = true;
		this.anyOrder = true;
		this.operatorAllowed = false;
		this.comparatorAllowed = false;
		this.logicalOperatorAllowed = false;
	},
	
	etBackspace : function() {
		var mainThis = FormBuilder.page.get("activeEditorView");
		var triggerValues = this.model.get("triggerValues");
		var str = this.getTriggerCondition(triggerValues);
		if (str.length == 0) { return; }

		str = $.trim(str);
		sqrt = new String("sqrt(");

		// Need to erase the entire question
		if (str.charAt(str.length - 1) == "]") {

			str = str.substr(0, str.lastIndexOf("["));
			if (str.charAt(str.length-1) == "-") {
				str = str.substr(0, str.length-2);
			}
		} else if ((str.length >= sqrt.length) && ((str.substring(str.length - sqrt.length, str.length)) == sqrt)) { //sqrt
				str = str.substr(0, str.length - sqrt.length - 1);
		} else if (str.lastIndexOf("||") == str.length -2 || str.lastIndexOf("&&") == str.length -2 //logical operator
					|| str.lastIndexOf("==") == str.length -2 || str.lastIndexOf("!=") == str.length -2  
					|| str.lastIndexOf(">=") == str.length -2 || str.lastIndexOf("<=") == str.length -2 //some comparators
				) { 
			str = str.substr(0, str.length - 2);
		} else {
				str = str.substr(0, str.length - 1);
		}


		str = $.trim(str) + " ";
		this.setTriggerValues(triggerValues, str);
		
		//For "Condition" area displaying
		str = this.getTriggerCondition(triggerValues);
		var str2 = "";
		if(str != "") {
			str2 = this.model.reformatCalculationDisplay(str);
		}
        mainThis.$('#conditionsDisplay').html(str2);

        this.resetAllows(str);
	},
	
	etClear : function() {
		var mainThis = FormBuilder.page.get("activeEditorView");
		var triggerValues = [];
		this.model.set('triggerValues', triggerValues);
		this.etReset();
        mainThis.$('#conditionsDisplay').html("");
	},
	
	etNumber : function(x) {
		var mainThis = FormBuilder.page.get("activeEditorView");
		if (this.anyOrder || this.numAllow) {
			var triggerValues = this.model.get("triggerValues");
			var str =  this.getTriggerCondition(triggerValues) + x ;
			
			this.setTriggerValues(triggerValues, str);

			var str2 = "";
			if(""!=str) {
				str2 = this.model.reformatCalculationDisplay(str);
			}
	        mainThis.$('#conditionsDisplay').html(str2);
		} else if (!this.numAllow){
	    	var msg = "Please add an operator / comparator / logical operator before adding a number.";
	    	this.showWarningMsg(msg);
	    } 
		
		this.qAllow = false;
		this.anyOrder = true;
		this.operatorAllowed = true;
		this.comparatorAllowed = true;
		this.logicalOperatorAllowed = true;
	},
	
	etButton : function(x) {
		var mainThis = FormBuilder.page.get("activeEditorView");
		if (this.anyOrder || this.operatorAllowed) {
			var triggerValues = this.model.get("triggerValues");
			var str =  this.getTriggerCondition(triggerValues) + ' ' + x + ' ';

			this.setTriggerValues(triggerValues, str);
			
			var str2 = "";
			if(str != "") {
				str2 = this.model.reformatCalculationDisplay(str);
			}

	        mainThis.$('#conditionsDisplay').html(str2);
		}

		this.operatorAllowed = false;
		this.comparatorAllowed = false;
		this.logicalOperatorAllowed = false;
		this.qAllow = true;
		this.numAllow = true;
	},
	
	
	etPercent : function() {
		var mainThis = FormBuilder.page.get("activeEditorView");
		
		var triggerValues = this.model.get("triggerValues");
		var str =  this.getTriggerCondition(triggerValues) + '%';

		this.setTriggerValues(triggerValues, str);
		
		var str2 = "";
		if(str != "") {
			str2 = this.model.reformatCalculationDisplay(str);
		}

        mainThis.$('#conditionsDisplay').html(str2);
	},
	
	etParen : function(x) {
		var mainThis = FormBuilder.page.get("activeEditorView");
		
		var triggerValues = this.model.get("triggerValues");
		var str =  this.getTriggerCondition(triggerValues) + x;

		this.setTriggerValues(triggerValues, str);

		var str2 = "";
		if(str != "") {
			str2 = this.model.reformatCalculationDisplay(str);
		}

        mainThis.$('#conditionsDisplay').html(str2);		
	},
	
	etFlipsign : function(s) {
		isNum = new RegExp("[\.0-9]");

		var triggerValues = this.model.get("triggerValues");
		var str = this.getTriggerCondition(triggerValues);

		if (str.charAt(str.length-1) == "]") {
			pos = str.lastIndexOf("[");
			if (str.charAt(pos-1) != "-") {
				str = str.substring(0, pos) + "-" + str.substring(pos, str.length);
			} else {
				str = str.substring(0, pos-1) + str.substring(pos, str.length);
			}
		
			this.setTriggerValues(triggerValues, str);
		} else if (isNum.exec((str.charAt(str.length-1)))) {
			pos = -2;
			track = str.length-1;
			do {
				if (track < 0) {
					pos = -1;
				} else if (isNum.exec(str.charAt(track))) {
					track--;
				} else {
					pos = track;
				}
			} while(pos == -2);

			if (str.charAt(pos) != "-") {
				str = str.substring(0, pos+1) + "-" + str.substring(pos+1, str.length);
			} else {
				str = str.substring(0, pos) + str.substring(pos+1, str.length);

			}
			this.setTriggerValues(triggerValues, str);
		}
		
		var str2 = "";
		if(""!=str) {
			str2 = this.model.reformatCalculationDisplay(str);
		}

        mainThis.$('#conditionsDisplay').html(str2);
	},
	
	addTriggerConditionQuestion : function() {
		var mainThis = FormBuilder.page.get("activeEditorView");
		var triggerValues = this.model.get("triggerValues");
		var str = $.trim(this.getTriggerCondition(triggerValues));

		var q = $.trim(mainThis.$("#triggerQuestion").val());
		var sectioQuestionArr = q.split(":");
		
		if (this.qAllow && q.length>0 ) {
	        if ((this.anyOrder || this.qAllow) && !(str.length>1 && ((str.charAt(str.length-1) == "(") || (str.charAt(str.length-1) == "%")))) {
	        	if(q.indexOf('thisQuestion') >= 0) {
	        		str = str + ' [' + q + '] ';
	        	} else {
	        		str = str + ' ['+sectioQuestionArr[0]+'_Q_' + sectioQuestionArr[1] + '] ';
	        	}	            
	        } else if (str.length>1 && (str.charAt(str.length-1) == "(" || (str.charAt(str.length-1) == "%"))) {
	        	if(q.indexOf('thisQuestion') >= 0) {
	        		str = str + ' [' + q + '] ';
	        	} else {
	        		str = str + ' ['+sectioQuestionArr[0]+'_Q_' + sectioQuestionArr[1] + '] ';
	        	}
	        }	        
	    } else if (!this.qAllow) {
	    	var msg = "Please add an operator / comparator / logical operator.";
	    	this.showWarningMsg(msg);
	    } else if (q.length == 0){
	    	var msg = "Please select a question.";
	    	this.showWarningMsg(msg);
	    }
		
		this.setTriggerValues(triggerValues, str);
		var str2 = "";
		if(str != "") {
			str2 = this.model.reformatCalculationDisplay(str);
		}
		mainThis.$('#conditionsDisplay').html(str2);
		//populate this value in tooltip on the question
		var type = this.model.get("questionType");
		var answerType = this.model.get("answerType");
		var questionTypes = Config.questionTypes;
		if (type == questionTypes.textbox && answerType == 2) {
			this.model.set("triggerAnswers", str2);
		}

		this.operatorAllowed = true;
		this.qAllow = false;
		this.anyOrder = false;
		this.numAllow = false;
		this.comparatorAllowed = true;
		this.logicalOperatorAllowed = true;
	},
	
	etComparator : function(comparator) {
		var triggerValues = this.model.get("triggerValues");
		var str = this.getTriggerCondition(triggerValues);
		
		if (this.comparatorAllowed && comparator.length > 0) {
			str = str + ' ' + comparator + ' ';	        
	    } else if (!this.comparatorAllowed){
	    	var msg = "Please add a question or a number before adding a comparator.";
	    	this.showWarningMsg(msg);
	    } else if (comparator.length == 0) {
	    	var msg = "Please select a comparator.";
	    	this.showWarningMsg(msg);
	    }
				
		this.setTriggerValues(triggerValues, str);
		
		var str2 = "";
		if(str != "") {
			str2 = this.model.reformatCalculationDisplay(str);
		}
		mainThis.$('#conditionsDisplay').html(str2);

		
		this.operatorAllowed = false;
		this.comparatorAllowed = false;
		this.logicalOperatorAllowed = true;
		this.qAllow = true;
		this.numAllow = true;
	},
	
	etLogicalOperator : function(logicalOp) {
		var triggerValues = this.model.get("triggerValues");
		var str = this.getTriggerCondition(triggerValues);

		if (this.logicalOperatorAllowed && logicalOp.length>0) {
			str = str + ' ' + logicalOp + ' ';		        
	    } else if (!this.logicalOperatorAllowed){
	    	var msg = "Please add a condition before adding a logical operator.";
	    	this.showWarningMsg(msg);
	    } else if (logicalOp.length == 0) {
	    	var msg = "Please select a logical operator.";
	    	this.showWarningMsg(msg);
	    }
		
		this.setTriggerValues(triggerValues, str);
		
		var str2 = "";
		if(str != "") {
			str2 = this.model.reformatCalculationDisplay(str);
		}
		mainThis.$('#conditionsDisplay').html(str2);
		
		this.operatorAllowed = false;
		this.comparatorAllowed = false;
		this.logicalOperatorAllowed = false;
		this.qAllow = true;
		this.numAllow = true;
	},
	
	showWarningMsg : function(msg){
		$.ibisMessaging("dialog", "info", msg, {modal: true});
	},
	
	getTriggerCondition : function(triggerValues) {		
		var str = triggerValues.length == 0 ? "" : triggerValues[0].etCondition;
		return str;
	},
	
	setTriggerValues : function(triggerValues, str) {
		var etValId = triggerValues.length == 0 ? -1 : triggerValues.length * -1;		 
		var etAnswer = "";
		var etCondition = str;
		if(triggerValues.length == 0){
			var thisTriggerVal = new this.TriggerValue(etValId, etAnswer, etCondition);
			triggerValues.push(thisTriggerVal);
		} else {
			triggerValues[0].etCondition = str;
		}
		this.model.set('triggerValues',triggerValues);
	},
	
	resetAllows : function(condStr) {
		condStr = $.trim(condStr);
		var lastChar = condStr.substr(condStr.length - 1);
		var comparators = ["=", ">", "<"];
		var logicalOps = ["&", "|"];
		var operators = ["+", "-", "*", "/", "%", "(", ")"];
		var numbers = ["0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "]"];
		
		if(comparators.indexOf(lastChar) >= 0) {
			this.operatorAllowed = false;
			this.comparatorAllowed = false;
			this.logicalOperatorAllowed = false;
			this.qAllow = true;
			this.numAllow = true;
		}
		if(logicalOps.indexOf(lastChar) >= 0) {
			this.operatorAllowed = false;
			this.comparatorAllowed = false;
			this.logicalOperatorAllowed = false;
			this.qAllow = true;
			this.numAllow = true;
		}
		if(operators.indexOf(lastChar) >= 0) {
			this.operatorAllowed = false;
			this.comparatorAllowed = false;
			this.logicalOperatorAllowed = false;
			this.qAllow = true;
			this.numAllow = true;
		}
		if(numbers.indexOf(lastChar) >= 0) {
			this.operatorAllowed = true;
			this.comparatorAllowed = true;
			this.logicalOperatorAllowed = true;
			this.qAllow = false;
			this.numAllow = true;
		}
		if(condStr.length == 0) {
			this.operatorAllowed = false;
			this.numAllow = true;
			this.qAllow = true;
			this.comparatorAllowed = false;
			this.logicalOperatorAllowed = false;
		}
	}
	

};