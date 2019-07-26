/**
 * Builds all questions for this form given a list of data elements.
 * 
 * 
 */
var AutoBuildQuestionUtil = {
	// the index of questions on this form, starting with 0
	questionIndex : 0,
	
	/**
	 * Generates all questions for the form
	 * 
	 * Called once adding Sections is complete
	 * 
	 * General process is to take data element, create a basic question
	 * object from it, decide what type of question it should be, configure
	 * the question object for that particular type, then put it on the page.
	 */
	generate : function() {
		var dataElements = FormBuilder.form.dataElements.toArray();
		var numDataElements = dataElements.length;
		var j = 0;
		var questionIndex = 0;
		var savingFunctions = [];
		EventBus.trigger("close:processing");
		EventBus.trigger("open:processing", "Creating Questions");
		if (numDataElements > 0) {
			setTimeout(function addSingleDE() {
				try {
					var dataElement = dataElements[j];

					var dataElementType = dataElement.get("dataElementType");
					var vrl = dataElement.get("valueRangeList"); 
					if (vrl instanceof Array) {
						//For permissive value, use the sort logic from the back end, instead of sorting the list again in front end
						if(dataElementType != "Numeric Values") {
							vrl.sort(AutoBuildQuestionUtil.permValueComparator);
						} 
						
//						vrl.sort(function(vr1, vr2) {
//							var value1 = vr1.valueRange;
//							var value2 = vr2.valueRange;
//							if (value1 == "Other, specify") {
//								return 1;
//							}
//							else if (value2 == "Other, specify") {
//								return -1;
//							}
//							else {
//								return alphanum(vr1.valueRange, vr2.valueRange);
//							}
//						});
					}

					var question = AutoBuildQuestionUtil.generateSingle(questionIndex--, dataElement);
					
					var sectionName = dataElement.getRGName();
					var section = FormBuilder.form.sections.getBySectionName(sectionName);
					var questionModel = section.addQuestion(question);
					EventBus.trigger("add:question", questionModel);
					
					if(section.isRepeatableParent()){
						//create the repeatable child questions
						EventBus.trigger("create:repeatableQuestions", questionModel);
					}
					
					// scroll to the new question
					var questionDiv = $('#' + questionModel.get("newQuestionDivId"));
					FormBuilder.pageView.scrollPageTo(questionDiv);
					
					var isNew = true;
					savingFunctions.push(questionModel.saveToDatabase(isNew));

					// percent complete.  numQuestions will never be zero here
					pc = (j / numDataElements) * 100;
					FormBuilder.page.get("processingView").setValueTo(pc);
				}
				catch(e) {
					Log.developer.error(e);
					var message = "A problem occurred loading one of your questions.  I will try to load the others but this form will not be correct. ";
					if (Config.devmode) {
						message += e;
					}
					$.ibisMessaging("dialog", "error", message);
				}
				
				j++;
				if (j < numDataElements) {
					setTimeout(addSingleDE, 0); // timeout loop
				}
				else {
					// finish up					
					QuestionDragHandler.resizeAllSections();
					FormBuilder.page.set("loadingData", false);
					
					// this waits until all saves are complete then performs the ID check
					$.when.apply($, savingFunctions).done(function() {
						// handles any non-saved questions - maybe there's a problem somewhere?
						FormBuilder.form.getAllQuestionsInForm().forEach(function(question) {
							if (question.get("questionId") < 0) {
								var commonQuestions = FormBuilder.form.findFilteredQuestionsWhere({questionName : question.get("questionName")}, function(testQuestion) {
									return testQuestion.cid != question.cid;
								});
								if (commonQuestions.length > 0) {
									// all of those should have the same question ID, version number, and version letter
									question.set("questionId", commonQuestions[0].get("questionId"));
									question.set("questionVersionLetter", commonQuestions[0].get("questionVersionLetter"));
									question.set("questionVersionNumber", commonQuestions[0].get("questionVersionNumber"));
									// recalculate div ID so it can be displayed correctly
									question.calculateDivId(true);
								}
							}
						});
					});
				}
			});
		}

	},
	
	/**
	 * Configures a question object - giving it the correct properties for the data element
	 * 
	 * @param questionId the question ID for the upcoming question
	 * @param dataElement the data element to fill in the question
	 * @returns question object after data element properties are added
	 */
	generateSingle : function(questionId, dataElement) {
		var question = AutoBuildQuestionUtil.buildInitialQuestion(questionId, dataElement);
		switch(question.questionType) {
			case "1":
				question = AutoBuildQuestionUtil.buildTextBox(question, dataElement);
				break;
			case "4":
				question = AutoBuildQuestionUtil.buildRadio(question, dataElement);
				break;
			case "6":
				question = AutoBuildQuestionUtil.buildCheckbox(question, dataElement);
				break;
			case "11":
				question = AutoBuildQuestionUtil.buildFileUpload(question, dataElement);
				break;
			default:
		}
		return question;
	},
	
	buildInitialQuestion : function(questionId, dataElement) {
		// find the correct section. Because RG and Section names are
		// the same in the automated version, we can just search RG name
		var sectionName = dataElement.getRGName();
		var section = FormBuilder.form.sections.getBySectionName(sectionName);
		
		// to get "formId_dataElementName"
		var questionName = AutoBuildQuestionUtil.determineQuestionName(dataElement, FormBuilder.form.get("formid"));
		var questionText = dataElement.get("suggestedQuestion") || Config.language.newQuestionText;
		var questionType = AutoBuildQuestionUtil.determineQuestionType(dataElement);
		var answerType   = AutoBuildQuestionUtil.determineAnswerType(dataElement);
		// added by Ching-Heng
		var catOid = dataElement.get("catOid");
		var formItemOid = dataElement.get("formItemOid");
		
		var questionObj = {
			questionId				: questionId,
			questionVersionLetter 	: "A",
			questionVersionNumber 	: "1",
			questionName 			: questionName,
			questionText			: questionText,
			descriptionUp 			: '',
			descriptionDown 		: '',
			questionType 			: questionType,
			catOid					: catOid,
			formItemOid				: formItemOid,
			hasDecimalPrecision 	: false,
			prepopulation 			: false,
			defaultValue 			: "",
			unansweredValue 		: "",
			associatedGroupIds 		: new Array(),
			existingQuestion 		: false,
			forcedNewVersion 		: false,
			// this will be calculated on render
			newQuestionDivId 		: null,
			sectionId				: section.get("id"),
			associatedGroupIds 		: null,
			qType 					: questionType,
			required 				: dataElement.get("requiredType") == "REQUIRED",
			validation 				: answerType!=1 ,
//			visualScaleInfo : "",
//			includeOther : false,
			
			// attribute parameters /////////////////////////////////

			answerType : answerType,    //1: string, 2: numeric 3: date 4:date-time
			dataElementName 		: dataElement.get("dataElementName"),
			isNew					: false,
			active					: true,
			autoCreated				: true
		};
		
		/*
		 * ONLY FOR NUMERIC ANSWERS
		 * logic for range operators and min/max values.  We have a number of different scenarios here including:
		 * 
		 * For any zero or non-zero numbers N and X
		 * 
		 * MIN		MAX		INTERPRETATION		CODED
		 * -------------------------------
		 * 0		X > 0	less than X			x
		 * N < X	X > N	between N and X		x
		 * N > 0	0		greater than N		x
		 * N < 0	0		between N and 0		x
		 * 0		0		none				x
		 * N = X	X = N	exactly N			x
		 * N > X	X < N	error -- none
		 * 
		 * rangeOperator :
		 * isEqualTo  : 1,
		 * lessThan    : 2,
		 * greaterThan : 3,
		 * between      : 4 
		 */ 
		if (answerType == 2 || dataElement.get("dataElementType") == "Numeric Values") {
			// note: min and max are always either string-encoded numbers or actual numerics.  Never undefined
			var min = Number(dataElement.get("min"));
			var max = Number(dataElement.get("max"));
			
			
			if(min == -99999 && max == -99999) {
				questionObj.rangeOperator = 0;
			}else if (min == -99999 && max != -99999) {
				questionObj.rangeOperator = 2;
				questionObj.rangeValue1 = max;
				questionObj.rangeValue2 = 0;
			}else if(min != -99999 && max == -99999) {
				questionObj.rangeOperator = 3;
				questionObj.rangeValue1 = min;
				questionObj.rangeValue2 = 0;	
			}else if(min != -99999 && max != -99999) {
				if(min == max) {
					questionObj.rangeOperator = 1;
					questionObj.rangeValue1 = min;
					questionObj.rangeValue2 = max;
				}else {
					questionObj.rangeOperator = 4;
					questionObj.rangeValue1 = min;
					questionObj.rangeValue2 = max;
				}
			}
			
			
			
			
			/*if (min == 0) {
				if (max == 0) {
					questionObj.rangeOperator = 0;
				}
				else if (max > 0) {
					questionObj.rangeOperator = 2;
					questionObj.rangeValue1 = max;
					questionObj.rangeValue2 = 0;
				}
				else {
					// note, if max < 0, there's some sort of error and we should fall back to accepting anything
					questionObj.rangeOperator = 0;
				}
			}
			else if (min > 0) {
				if (min < max) {
					questionObj.rangeOperator = 4;
					questionObj.rangeValue1 = min;
					questionObj.rangeValue2 = max;
				}
				else if (max == 0) {
					questionObj.rangeOperator = 3;
					questionObj.rangeValue1 = min;
					questionObj.rangeValue2 = 0;
				}else if (max == min) {
					questionObj.rangeOperator = 1;
					questionObj.rangeValue1 = min;
					questionObj.rangeValue2 = max;
				}
			}
			else { // min < 0
				if (min < max) {
					questionObj.rangeOperator = 4;
					questionObj.rangeValue1 = min;
					questionObj.rangeValue2 = max;
				}
				if (max == 0) {
					questionObj.rangeOperator = 4;
					questionObj.rangeValue1 = min;
					questionObj.rangeValue2 = max;
				}
			}*/
			
		}
		
		return questionObj;
	},
	
	/**
	 * Determines the proper default question type given a data element
	 * 
	 *  1/6/2015 mapping (JP):
	 * 	BRICS Type			Defined Values	Single/Multiple		ProFoRMS Question Type
	 * ------------------------------------------------------------------------------------------
	 *	Alphanumeric        No              Free-form           Textbox, Textarea
	 *	                    Yes             Single              Radio, Select
	 *	                    Yes             Multiple            Checkbox, Multi-Select, Image Map
	 *	Numeric Values      No              Free-form           Textbox, Textarea, Visual Scale
	 *	                    Yes             Single              Radio, Select
	 *	                    Yes             Multiple            Checkbox, Multi-Select, Image Map
	 *	Date or Date & Time -               -                   Textbox, Textarea
	 *	GUID                -               -                   Textbox
	 *	File                -               -                   File Upload
	 *	Thumbnail           -               -                   File Upload 
	 *	Biosample           -               -                   Textbox
	 *
	 * @param dataElement (DataElement) the data element source
	 * @returns integer id of the question type @See Config.questionTypes
	 */
	determineQuestionType : function(dataElement) {
		/* Restriction IDs:
		 * 0	Free-form
		 * 1	single select
		 * 2	multi select
		 */
		if (dataElement.get("dataElementType") == "Alphanumeric" || dataElement.get("dataElementType") == "Numeric Values") {
			// if it's alphanumeric or numeric, we should check if it's multi or single select
			if (dataElement.get("restrictionId") == 0 && dataElement.get("valueRangeList").length < 1) {
				return Config.questionTypes.textbox;
			}
			else if (dataElement.get("restrictionId") == 0 && dataElement.get("valueRangeList").length > 0) {
				// this is a fall-back for old "other please specify" options
				return Config.questionTypes.checkbox;
			}
			else if (dataElement.get("restrictionId") == 1 && dataElement.get("valueRangeList").length > 0) {
				return Config.questionTypes.radio;
			}
			else if (dataElement.get("restrictionId") == 2 && dataElement.get("valueRangeList").length > 0) {
				return Config.questionTypes.checkbox;
			}
			else {
				// fall back just in case
				return Config.questionTypes.textbox;
			}
		}
		else if (dataElement.get("dataElementType") == "Date or Date & Time") {
			return Config.questionTypes.textbox;
		}
		else if (dataElement.get("dataElementType") == "GUID") {
			return Config.questionTypes.textbox;
		}
		else if (dataElement.get("dataElementType") == "File") {
			return Config.questionTypes.fileUpload;
		}
		else if (dataElement.get("dataElementType") == "Thumbnail") {
			return Config.questionTypes.fileUpload;
		}
		else if (dataElement.get("dataElementType") == "Biosample") {
			return Config.questionTypes.textbox;
		}
		else {
			// fall back to the most basic question type we have: textbox
			return Config.questionTypes.textbox;
		}
	},
	
	determineQuestionName : function(dataElement, formId) {
		// data element name in dataElementName is in rgName.deName format
		var deNameSplit = dataElement.get("dataElementName").split(".");
		var deName = deNameSplit[1];
		return formId + "_" + deName;
	},
	
	/**
	 * Chooses the question answer type based on the data element type.
	 * Defaults "date or date & time" to "date & time".
	 * 
	 * @param dataElement DataElement model to check
	 * @returns {Number} @see Config.answerType for mapping
	 */
	determineAnswerType : function(dataElement) {
		var elementType = dataElement.get("dataElementType");
		if (elementType == "Alphanumeric") {
			return 1;
		}
		else if (elementType == "Numeric Values") {
			return 2;
		}
		else if (elementType == "Date or Date & Time") {
			return 4;
		}
		else {
			// guid, file, thumbnail, biosample, etc.
			return 1;
		}
	},
	
	
	
	buildOptionsObjectArray : function(dataElement) {
		
		var optionsArray = new Array();
		
		var valueRangeList = dataElement.get("valueRangeList");
		// just a sanity check: is it single or multiple select?
		if (valueRangeList.length > 0) {
			if (valueRangeList != "" && valueRangeList.length > 0) {
				
				//first determine if all the value ranges are numeric...only if all numeric, set the score
				var allValueRangeNumeric = true;
				
				for(var i=0;i<valueRangeList.length;i++) {
					var vr = valueRangeList[i].valueRange;
					if (!jQuery.isNumeric(vr)) {
						allValueRangeNumeric = false;
						break;
					}
				}
				
				
				//test if there is description for all
				var useDescription = true;
				for(var i=0;i<valueRangeList.length;i++) {
					if(typeof valueRangeList[i].description === "undefined" || valueRangeList[i].description.trim() == "") {
						useDescription = false;
						break;
					}	
				}
				
				//test if there are any duplicate descritions...if so...then dont use description
				if(useDescription) {
					var tempDescription1 = "";
					for(var i=0;i<valueRangeList.length;i++) {
						
						var tempDescription2 = valueRangeList[i].description.trim();
						
						if(i==0) {
							tempDescription1 = tempDescription2;
						}else {
							if(tempDescription1 == tempDescription2) {
								useDescription = false;
								break;
							}else {
								tempDescription1 = tempDescription2;
							}	
						}
					}
				}
				
				
				

				for(var i=0;i<valueRangeList.length;i++) {						

					var optionConfig = {};
					var score = "";
					if(allValueRangeNumeric) {
						score = valueRangeList[i].valueRange;
					}

					optionConfig.submittedValue = valueRangeList[i].valueRange;
					optionConfig.score = score;
					
					if(useDescription) {
						optionConfig.option = valueRangeList[i].description;
						
					}else {
						optionConfig.option = valueRangeList[i].valueRange;
					}
					
					//added by Ching-Heng
					optionConfig.elementOid = valueRangeList[i].elementOid;
					optionConfig.itemResponseOid =  valueRangeList[i].itemResponseOid;
					
					optionsArray.push(new OptionsObject(optionConfig));
				}
			}
		}
		return optionsArray;
		
		
		
	},
	
	
	/**
	 * Configures a text-type question (type 1)
	 * 
	 * @param questionObj (Object) the question object to finish configuring
	 * @param dataElement (DataElement) element to create as question
	 * @return Question model after configuration
	 */
	buildTextBox : function(questionObj, dataElement) {
		// modify as needed for this particular question type
		questionObj.minCharacters = 0;
		var size = dataElement.get("size");
		if (size > 0) {
			questionObj.maxCharacters = size;
		}
		return questionObj;
	},
	
	buildRadio : function(questionObj, dataElement) {
		questionObj.questionOptionsObjectArray = AutoBuildQuestionUtil.buildOptionsObjectArray(dataElement);
		return questionObj;
	},
	
	buildCheckbox : function(questionObj, dataElement) {
		questionObj.questionOptionsObjectArray = AutoBuildQuestionUtil.buildOptionsObjectArray(dataElement);
		return questionObj;
	},
	
	buildFileUpload : function(questionObj, dataElement) {
		// no customization needed
		return questionObj;
	},
	
	permValueComparator : function(pv1, pv2) {
		var value1 = pv1.valueRange;
		var value2 = pv2.valueRange;
		
		var ia = 0;
		var ib = 0;
		var nza = 0;
		var nzb = 0; 
		var ca = "";
		var cb = "";
		var result;
		
		if (value1.toLowerCase() == "other, please specify" || value1.toLowerCase() == "other, specify") {
			// other, specify always comes last
			return 1;
		}
		else if (value2.toLowerCase() == "other, please specify" || value2.toLowerCase() == "other, specify") {
			return -1;
		}
		while (1) {
			ca = _.getChar(value1, ia);
			cb = _.getChar(value2, ib);
			// skip over leading spaces or zeros
			while ((ca == " " || ca == "0") && ia < value1.length) {
				if (ca == "0") {
					nza++;
				}
				else {
					// only count consecutive zeroes
					nza = 0;
				}
				ca = _.getChar(value1, ++ia);
			}
			
			while (cb == " " || cb == "0" && ib < value2.length) {
				if (cb == "0") {
					nzb++;
				}
				else {
					nzb = 0;
				}
				cb = _.getChar(value2, ++ib);
			}
			
			if ($.isNumeric(ca) && $.isNumeric(cb)) {
				result = AutoBuildQuestionUtil.permValueCompareRight(value1.substring(ia), value2.substring(ib));
				if (result != 0) {
					return result;
				}
			}
			if (ca == 0 && cb == 0) {
				// The strings compare the same. Perhaps the caller
	            // will want to call strcmp to break the tie.
				return nza - nzb;
			}
			if (ca < cb) {
				return -1;
			}
			else if (ca > cb) {
				return 1;
			}
			++ia;
			++ib;
		}
	},
	
	permValueCompareRight : function(s1, s2) {
		var bias = 0;
		var ia = 0;
		var ib = 0;
		
        // The longest run of digits wins. That aside, the greatest
        // value wins, but we can't know that it will until we've scanned
        // both numbers to know that they have the same magnitude, so we
        // remember it in BIAS.
		while (1) {
			var ca = _.getChar(s1, ia);
			var cb = _.getChar(s2, ib);
			if (!$.isNumeric(ca) && !$.isNumeric(cb)) {
				return bias;
			}
			else {
				if (!$.isNumeric(ca)) {
					return -1;
				}
				else {
					if (!$.isNumeric(cb)) {
						return 1;
					}
					else {
						if (ca < cb && bias == 0) {
							bias = -1;
						}
						else {
							if (ca > cb && bias == 0) {
								bias = 1;
							}
							else if (ca == 0 && cb == 0) {
								return bias;
							}
						}
					}
				}
			}
			
			ia++;
			ib++;
		}
	}
};