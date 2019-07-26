var QuestionConversionFactorDecorator = {
		
		operatorAllowedC : false,
		numAllowC : true,
		qAllowC : true,
		anyOrderC : true,
		errorString : "",
		
		commonsName : "QuestionConversionFactorDecorator",


		config : {
			name : "QuestionConversionFactorDecorator",
			render : "renderConversionFactorCommons"
		},
		
		events : { 
			"click #convBackspace":"backspaceC",
			"click #convClear":"clearC",
			"click #convPercent":"percentC",
			"click #convFlip":"flipsignC",
			"click #questionConv":"addQuestionC",
			
			"click #conv0" : function(){this.numberC('0');},
			"click #conv1" : function(){this.numberC('1');},
			"click #conv2" : function(){this.numberC('2');},
			"click #conv3" : function(){this.numberC('3');},
			"click #conv4" : function(){this.numberC('4');},
			"click #conv5" : function(){this.numberC('5');},
			"click #conv6" : function(){this.numberC('6');},
			"click #conv7" : function(){this.numberC('7');},
			"click #conv8" : function(){this.numberC('8');},
			"click #conv9" : function(){this.numberC('9');},
			"click #convDot" : function(){this.numberC('.');},
			
			"click #convDivide" : function(){this.buttonC('/');},
			"click #convMult" : function(){this.buttonC('*');},
			"click #convSubt" : function(){this.buttonC('-');},
			"click #convAdd" : function(){this.buttonC('+');},
			
			"click #convSqrt" : function(){this.parenC(' sqrt(');},
			"click #convLeftP" : function(){this.parenC(' (');},
			"click #convRightP" : function(){this.parenC(') ');},
			

		},
		
		
		validationRules : [
		      
	  new ValidationRule({
	   fieldName : "unitConversionFactor",
	   description : "",
	   match : function(model) {

			var eqC = model.get("unitConversionFactor");
			var eqStrC = model.get("unitConversionFactor");
			var eqStrC2 = model.get("unitConversionFactor");
			
			var count = 0;
		    var operators = /[+-\/%*]/;
		    
		    if (eqC.length > 0) {
		        chr = eqC.charAt(0);
		        if (teststr = operators.exec(chr)) {
		        	this.description = Config.language.operrator;
		        	 return false;
		        }

			    var divbyzero = /\s\/\s0+\s+/g;
			    if (teststr = divbyzero.exec(eqC+" ")) {
			    	this.description = Config.language.divisionZero;
			        return false;
			    }
		
			    var parenStr = this.testParenC(eqStrC);
				if (parenStr != null && parenStr.length > 0) {
					this.description = parenStr;
					return false;
				}
				
 				var noquestion = /\[/;
				if(!eqC.toString().match(noquestion)){
					this.description = Config.language.noquestion;
					return false;
				}
		
				//test eqn by substituting 2 for [this]
			    eqStrC = eqStrC.replace("[this]", "2");
			    eqStrC = eqStrC.replace(/sqrt\(\s*([\d\.]+)\s*\)/g, "Math.sqrt($1)");
			    eqStrC = eqStrC.replace(/%/g, "*\(1/100\)*");
			    
			    //test eqn again by substituting 4 for [this]
			    eqStrC2 = eqStrC2.replace("[this]", "4");
			    eqStrC2 = eqStrC2.replace(/sqrt\(\s*([\d\.]+)\s*\)/g, "Math.sqrt($1)");
			    eqStrC2 = eqStrC2.replace(/%/g, "*\(1/100\)*");
			    
			    try {
			    	evalResult = eval(eqStrC);
			    	evalResult = eval(eqStrC2);
			    } catch (exception) {
			    	this.description = Config.language.fialComputation;
			    	return false;
			    }
		    }
		    return true;
	   },
	   testParenC : function(x) {
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
		
		
		
		renderConversionFactorCommons: function(model) {
			var type = this.model.get("questionType");

			if(type === Config.questionTypes.textbox) {
				var linkHtml = TemplateManager.getTemplate("editQuestionConversionFactorTabLabel");
				
				var contentHtml;
				
				if(FormBuilder.form.get("isCAT") && FormBuilder.form.get("measurementType") != 'shortForm'){
					contentHtml = TemplateManager.getTemplate("editQuestionConversionFactorTabForCAT");
				}else{
					contentHtml = TemplateManager.getTemplate("editQuestionConversionFactorTab");
				}
				
				this.addTab(linkHtml, contentHtml);
				
				this.onChangeAnswerTypeInConv();
				this.listenTo(this.model, "change:answerType", this.onChangeAnswerTypeInConv);
				
			}
			
		},
		
		
		
		
		onChangeAnswerTypeInConv : function() {
			var answerType = this.model.get("answerType");
			if(answerType == 2) {
				//hide tab
				this.enableTab('dialog_editQuestion_conversionFactor');
				
			}else {
				//show tab
				this.disableTab('dialog_editQuestion_conversionFactor');
			}
			
		},
		
		
		
		
		resetC : function() {
			this.numAllowC = true;
			this.qAllowC = true;
			this.operatorAllowedC = false;
		},
		
		
		
		
		backspaceC : function() {
			var str = this.model.get("unitConversionFactor");
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

			this.model.set('unitConversionFactor',str);
			
		},
		
		
		
		
		clearC : function() {

			this.model.set('unitConversionFactor',"");
			this.resetC();
		},
		
		
		
		numberC : function(x) {
			if (this.anyOrderC || this.numAllowC) {
				
				var str =  this.model.get("unitConversionFactor") + x;
				this.model.set('unitConversionFactor',str);
			}
			this.qAllowC = false;
			this.operatorAllowedC = true;
		},
		
		
		
		
		buttonC : function(x) {
			if (this.anyOrderC || this.operatorAllowedC) {
				

				var str = this.model.get("unitConversionFactor").trim()  + ' ' + x + ' ';
				
				this.model.set('unitConversionFactor',str);
			}

			this.operatorAllowedC = false;
			this.qAllowC = true;
			this.numAllowC = true;
		},
		
		
		
		
		percentC : function() {
			
			var str =  this.model.get("unitConversionFactor") + '%';
			this.model.set('unitConversionFactor',str);
		},
		
		
		
		
		parenC : function(x) {

			var str = this.model.get("unitConversionFactor").trim() + x;
			this.model.set('unitConversionFactor',str);
		},

		addQuestionC : function() {

			
			str = this.model.get("unitConversionFactor").trim()


			var q = document.getElementById("questionConv").value;
			q_str = q.trim();
			
			str = str + "[" + q_str + "]";

			
			this.model.set('unitConversionFactor',str);
			
			this.operatorAllowedC = true;
			this.qAllowC = false;
			this.numAllowC = false;
		},
		
		

		flipsignC : function(s) {
			isNum = new RegExp("[\.0-9]");

			str = this.model.get("unitConversionFactor").trim();

			if (str.charAt(str.length-1) == "]") {
				pos = str.lastIndexOf("[");
				if (str.charAt(pos-1) != "-") {
					str = str.substring(0, pos) + "-" + str.substring(pos, str.length);
				} else {
					str = str.substring(0, pos-1) + str.substring(pos, str.length);
				}
			
			this.model.set('unitConversionFactor',str);
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

			
			this.model.set('unitConversionFactor',str);
			}
		}
		
		
		
		
		
		
		
		
};