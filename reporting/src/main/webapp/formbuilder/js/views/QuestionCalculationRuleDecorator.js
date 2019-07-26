
var QuestionCalculationRuleDecorator = {


		operatorAllowed : false,
		numAllow : true,
		qAllow : true,
		anyOrder : true,
		errorString : "",
		
		commonsName : "QuestionCalculationRuleDecorator",


		config : {
			name : "QuestionCalculationRuleDecorator",
			render : "renderCalculationRuleCommons"
		},
		
		events : {	
			"click #calcBackspace":"backspace",
			"click #calcClear":"clear",
			"click #calcPercent":"percent",
			"click #calcFlip":"flipsign",
			//"click .calcOpt":"addQuestion",
			
			"click #questionCalc":"addQuestion",
			
			"click #calc0" : function(){this.number('0');},
			"click #calc1" : function(){this.number('1');},
			"click #calc2" : function(){this.number('2');},
			"click #calc3" : function(){this.number('3');},
			"click #calc4" : function(){this.number('4');},
			"click #calc5" : function(){this.number('5');},
			"click #calc6" : function(){this.number('6');},
			"click #calc7" : function(){this.number('7');},
			"click #calc8" : function(){this.number('8');},
			"click #calc9" : function(){this.number('9');},
			"click #calcDot" : function(){this.number('.');},
			
			"click #calcDivide" : function(){this.button('/');},
			"click #calcMult" : function(){this.button('*');},
			"click #calcSubt" : function(){this.button('-');},
			"click #calcAdd" : function(){this.button('+');},
			
			"click #calcSqrt" : function(){this.paren(' sqrt(');},
			"click #calcLeftP" : function(){this.paren(' (');},
			"click #calcRightP" : function(){this.paren(') ');},
		},
		
		
		
		
		
		validationRules : [
		                   new ValidationRule({
		                	   fieldName : "conversionFactor",
			             	   description : Config.language.dateTime,
			             	  match : function(model) {
			             		 if(model.get("answerType") == 3 || model.get("answerType") == 4) {
			             			 if(model.get("calculation") != "") {
			             				if(model.get("conversionFactor")== -2147483648) {
			             			        return false;	
		             					}
			             			 }
			             		 }
			             		 return true;
			             	  }  
		                   }),
		                   
		                   
		                   
		                   new ValidationRule({
		                	   fieldName : "conditionalForCalc",
			             	   description : "",
			             	  match : function(model) {
			             	
			             	if(model.get("calculation") == "") {
			             			if(model.get("conditionalForCalc")== true) {
			             				this.description=Config.language.doNotScoreError;
			             				return false;	
		             				}
			             	}
			             		 
			             		 return true;
			             	  }  
		                   }),

		                   
		             	  new ValidationRule({
		             	   fieldName : "calculation",
		             	   description : "",
		             	   match : function(model) {

		             			var eq = model.get("calculation");
		             			var eqStr = model.get("calculation");
		             			var findQuestionsAll = /\[S_[-]?\d+_Q_([0-9]+)\]/g;
		             			
		             			var count = 0;
		             		    var operators = /[+-\/%*]/;
		             		    
		             		    if (eq.length > 0) {
		             		        chr = eq.charAt(0);
		             		        if (teststr = operators.exec(chr)) {
		             		        	this.description = Config.language.operrator;
		             		        	 return false;
		             		        }

		             			    var divbyzero = /\s\/\s0+\s+/g;
		             			    if (teststr = divbyzero.exec(eq+" ")) {
		             			    	this.description = Config.language.divisionZero;
		             			        return false;
		             			    }
		             		
		             			    var parenStr = this.testParen(eqStr);
		             				if (parenStr != null && parenStr.length > 0) {
		             					this.description = parenStr;
		             					return false;
		             				}
		             		
		             				var noquestion = /\[/;
	                				if(!eq.toString().match(noquestion)){
	                					this.description = Config.language.noquestion;
	                					return false;
	                				}


		             				//check following for date type questions
		             				if(model.get("answerType") == 3 || model.get("answerType") == 4) {
		             					
		             					//if(model.get("conversionFactor")== -2147483648) {
		             						//this.description = " Please select a date conversion factor.";
			             			       // return false;	
		             					//}
		             					 //--------------------------------------------------------------------------
		             			        // check date
		             			        //--------------------------------------------------------------------------
		             			        //var findQuestionsWithMinus = /\[Q_[0-9]+\]\s*-\s*\[Q_[0-9]+\]/g;
		             			        var findQuestionsWithMinus = /\[S_[-]?\d+_Q_[0-9]+\]\s*-\s*\[S_[-]?\d+_Q_[0-9]+\]/g;

		             			        eqStr = eqStr.replace(findQuestionsWithMinus, "5");
		             			        while (qs2 = findQuestionsAll.exec(eqStr)) {
		             			        	this.description = Config.language.dateFormular;
		             			            return false;
		             			        }
		             				}else {
		             					//answer type is numeric
		             					eqStr = eqStr.replace(findQuestionsAll, "2");
		             				}
		             				
		             				//test eqn by substituting 2 for [this]
		             				eqStr = eqStr.replace(/sqrt\(\s*([\d\.]+)\s*\)/g, "Math.sqrt($1)");
		             				eqStr = eqStr.replace(/%/g, "*\(1/100\)*");
		             				
		             			    try {
		             			    	evalResult = eval(eqStr);

		             			    } catch (exception) {
		             			    	this.description = Config.language.fialComputation;
		             			    	return false;
		             			    }
		             		    }
		             		    return true;
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
		             		}
		             	  })            
		             		                   
		             		                   
		             		                   
		             		],
		
		
		             		
		             		
		             		
		             		
		renderCalculationRuleCommons: function(model) {

			var type = this.model.get("questionType");

			if(type === Config.questionTypes.textbox) {
				

				var linkHtml = TemplateManager.getTemplate("editQuestionCalculationRuleTabLabel");
				var contentHtml = TemplateManager.getTemplate("editQuestionCalculationRuleTab");
				this.addTab(linkHtml, contentHtml);
					
				this.onChangeAnswerTypeInCalc();
				this.listenTo(this.model, "change:answerType", this.onChangeAnswerTypeInCalc);
				
				//populate available questions to calculate is done in onChangeAnswerTypeInCalc()



				
				
			}
			
			
			
			
		},
		
		
		
		
		onActivateTab : function(event, ui) {
			if(ui.newPanel.is(this.$("#dialog_editQuestion_calculationRule"))) {
				var str = this.model.get("calculation");
				var str2;
				if(str != "") {
					str2 = this.model.reformatCalculationDisplay(str);
					//var cd='<div id="calculationDisplay" name="calculationDisplay" style="border:1px solid; height: 140px">';
			         // cd += str2;
			         // cd += '</div>';
					//console.log($('#calculationDisplay'));
			        $('#calculationDisplay').html(str2);
				}
			}
			
			
		},
		
		
		reset : function() {
			this.numAllow = true;
			this.qAllow = true;
			this.operatorAllowed = false;
		},
		
		backspace : function() {
			var str = this.model.get("calculation");
			if (str.length == 0) { return; }

			str = str.trim();
			sqrt = new String("sqrt(");

			// Need to erase the entire question
			if (str.charAt(str.length - 1) == "]") {

				str = str.substr(0, str.lastIndexOf("["));
				if (str.charAt(str.length-1) == "-") {
					str = str.substr(0, str.length-2);
				}
			} else if ((str.length >= sqrt.length) && ((str.substring(str.length - sqrt.length, str.length)) == sqrt)) {
					str = str.substr(0, str.length - sqrt.length - 1);
			} else {
					str = str.substr(0, str.length - 1);
			}


			str = str.trim();

			this.model.set('calculation',str);
			
			//For "Calculation Rule" area displaying
			str = this.model.get("calculation");
			var str2 = "";
			if(str != "") {
				str2 = this.model.reformatCalculationDisplay(str);
			}
			//var cd='<div id="calculationDisplay" name="calculationDisplay" style="border:1px solid; height: 140px">';
	          //cd += str2;
	         // cd += '</div>';
	        $('#calculationDisplay').html(str2);

			
		},
		
		clear : function() {

			this.model.set('calculation',"");
			this.reset();
			
			//For "Calculation Rule" area displaying
			//var cd='<div id="calculationDisplay" name="calculationDisplay" style="border:1px solid; height: 140px">';
	            //cd += '</div>';
	        $('#calculationDisplay').html("");
		},
		
		number : function(x) {
			if (this.anyOrder || this.numAllow) {
				
				var str =  this.model.get("calculation") + x;
				this.model.set('calculation',str);
				
				//For "Calculation Rule" area displaying
				//str = this.model.get("calculation");
				var str2 = "";
				if(""!=str) {
					str2 = this.model.reformatCalculationDisplay(str);
				}
				//var cd='<div id="calculationDisplay" name="calculationDisplay" style="border:1px solid; height: 140px">';
		         // cd += str2;
		         // cd += '</div>';
		        $('#calculationDisplay').html(str2);
			}
			this.qAllow = false;
			this.operatorAllowed = true;
		},
		
		button : function(x) {
			if (this.anyOrder || this.operatorAllowed) {
				

				var str = this.model.get("calculation").trim()  + ' ' + x + ' ';
				
				this.model.set('calculation',str);
				
				//For "Calculation Rule" area displaying
				//str = this.model.get("calculation");
				var str2 = "";
				if(str != "") {
					str2 = this.model.reformatCalculationDisplay(str);
				}
				//var cd='<div id="calculationDisplay" name="calculationDisplay" style="border:1px solid; height: 140px">';
		         // cd += str2;
		         // cd += '</div>';
		        $('#calculationDisplay').html(str2);
			}

			this.operatorAllowed = false;
			this.qAllow = true;
			this.numAllow = true;
		},
		
		
		percent : function() {
			
			var str =  this.model.get("calculation") + '%';
			this.model.set('calculation',str);
			
			//For "Calculation Rule" area displaying
			//str = this.model.get("calculation");
			var str2 = "";
			if(str != "") {
				str2 = this.model.reformatCalculationDisplay(str);
			}
			//var cd='<div id="calculationDisplay" name="calculationDisplay" style="border:1px solid; height: 140px">';
	        //  cd += str2;
	         // cd += '</div>';
	        $('#calculationDisplay').html(str2);
		},
		
		paren : function(x) {

			var str = this.model.get("calculation").trim() + x;
			this.model.set('calculation',str);
			//For "Calculation Rule" area displaying
			//str = this.model.get("calculation");
			var str2 = "";
			if(str != "") {
				str2 = this.model.reformatCalculationDisplay(str);
			}
			//var cd='<div id="calculationDisplay" name="calculationDisplay" style="border:1px solid; height: 140px">';
	        //  cd += str2;
	         // cd += '</div>';
	        $('#calculationDisplay').html(str2);
			
		},
		
		addQuestion : function() {

			
			str = this.model.get("calculation").trim();

			var q = document.getElementById("questionCalc").value;
			q_str = q.trim();
			var sectioQuestionArr=q_str.split(":");
			
				if (q_str.length>0) {
			        if ((this.anyOrder || this.qAllow) && !(str.length>1 && ((str.charAt(str.length-1) == "(") || (str.charAt(str.length-1) == "%")))) {
			            str = str + ' ['+sectioQuestionArr[0]+'_Q_' + sectioQuestionArr[1] + '] ';
			        } else if (str.length>1 && (str.charAt(str.length-1) == "(" || (str.charAt(str.length-1) == "%"))) {
			            str = str + '['+sectioQuestionArr[0]+'_Q_' + sectioQuestionArr[1] + '] ';
			        }
			        
			    }

			
			this.model.set('calculation',str);
			//document.getElementById("calculationDisplay").value = str2;
			//For "Calculation Rule" area displaying
			//str = this.model.get("calculation");
			var str2 = "";
			if(str != "") {
				str2 = this.model.reformatCalculationDisplay(str);
			}
			//var cd='<div id="calculationDisplay" name="calculationDisplay" style="border:1px solid; height: 140px">';
	         // cd += str2;
	         // cd += '</div>';
	        $('#calculationDisplay').html(str2);

			
			this.operatorAllowed = true;
			this.qAllow = false;
			this.numAllow = false;
		},
		
		
		
		flipsign : function(s) {
			isNum = new RegExp("[\.0-9]");

			str = this.model.get("calculation").trim();

			if (str.charAt(str.length-1) == "]") {
				pos = str.lastIndexOf("[");
				if (str.charAt(pos-1) != "-") {
					str = str.substring(0, pos) + "-" + str.substring(pos, str.length);
				} else {
					str = str.substring(0, pos-1) + str.substring(pos, str.length);
				}
			
			this.model.set('calculation',str);
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

			
			this.model.set('calculation',str);
			}
			
			//For "Calculation Rule" area displaying
			//str = this.model.get("calculation");
			var str2 = "";
			if(""!=str) {
				str2 = this.model.reformatCalculationDisplay(str);
			}
			//var cd='<div id="calculationDisplay" name="calculationDisplay" style="border:1px solid; height: 140px">';
	        //  cd += str2;
	         // cd += '</div>';
	        $('#calculationDisplay').html(str2);
		},
		
		
		
		
		populateQuestionsToCalculate : function() {
			
			var activeSection = FormBuilder.page.get("activeSection");
			var activeSectionId = activeSection.get("id");
			var activeQuestionId = FormBuilder.page.get("activeQuestion").get("questionId");
			var activeQuestionAnswerType = FormBuilder.page.get("activeQuestion").get("answerType");

			//var availableCalculateQuestionsArray = new Array();
			
			var availbleQ='<select id="questionCalc" multiple  style="height: 140px; overflow: scroll;border: solid 1px">';
			FormBuilder.form.getSectionsInPageOrder().forEach(function(section) {
				
				var optGroup='';

				if(activeSection.get("isRepeatable") &&  section.get("isRepeatable") && section.get("repeatedSectionParent") == activeSectionId) {
					// if active section is repeatable, dont show any of its own child repeatable questions.   
				}else {
					var sectionQuestions = section.getQuestionsInPageOrder();
					
					if(sectionQuestions.length > 0) {
						var sId = section.get("id");
						var sName = section.get("name");
						if(section.get("isRepeatable") && (!(sId == activeSectionId))) {
						//if(section.get("isRepeatable")) {
							var index = section.getRepeatableIndex();
							//availbleQ+='<optgroup label="'  +  sName  +  '(' +   index + ')' +   '">';
							optGroup = '<optgroup label="'  +  sName  +  '(' +   index + ')' +   '">';
							
						}else {
							//availbleQ+='<optgroup label="'+sName+'">';
							optGroup = '<optgroup label="'+sName+'">';
							
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
									//now populate the list
									//var calculateQ = new Array();
									//calculateQ.push("<input type=\"checkbox\" value=\""+ sqId+ "\"/>");
									//calculateQ.push(sName);
									//calculateQ.push(qName);
									//calculateQ.push(qText);
									//calculateQ.push(qTypeLabel);
									//availableCalculateQuestionsArray.push(calculateQ);
									
									//availbleQ+='<option value="'+sId+':'+qId+':'+sName+':'+qName +'">&nbsp;&nbsp;* '+qName_sht+'</option>';
									options+='<option class="calcOpt" value="'+sId+':'+qId+':'+sName+':'+qName +'">&nbsp;&nbsp;* '+qName_sht+'</option>';
									
								}
							}
							//availbleQ+='</optgroup>';
						});	
						if(options != '') {
							optGroup = optGroup + options + '</optgroup>';
							availbleQ+=optGroup;
						}
						//availbleQ+='</optgroup>';
					}
				}
			});
			
			availbleQ += '</select>';
			$('#availableQuestionsToCalculateDiv').html(availbleQ);
		},
		
		
		
		
		
		
		
		
		
		
		
		onChangeAnswerTypeInCalc : function() {
			var answerType = this.model.get("answerType");
			if(answerType == 1) {
				//hide tab
				this.disableTab('dialog_editQuestion_calculationRule');
				
			}else {
				//if answer type is numeric, hide the date-time conversion factor
				//else show it
				if(answerType == 2) {
					this.model.set("conversionFactor",-2147483648);
					$("#dateTimeConversionFactorDiv").hide();
				}else {
					
					$("#dateTimeConversionFactorDiv").show();
				}
				
				
				
				//show tab
				this.enableTab('dialog_editQuestion_calculationRule');
				this.populateQuestionsToCalculate();
			}
			
		}
		
		


};