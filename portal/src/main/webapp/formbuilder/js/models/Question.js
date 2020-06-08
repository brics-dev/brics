var Question = BaseModel.extend({
	defaults : {
		questionId : "-1",
		questionVersionLetter : null,
		questionVersionNumber : 1,
		questionName : null,
		questionText : null,
		descriptionUp : '',
		descriptionDown : '',
		questionType : null,     // needs to be a number
		hasDecimalPrecision : false,
		prepopulation : false,
		defaultValue : "",
		unansweredValue : "",
		associatedGroupIds : new Array(),
		existingQuestion : false,
		forcedNewVersion : false,
		questionOptionsObjectArray :  new Array(),
		imageOption : null,
		imageFileName : null,
		visualScaleInfo : "",
		newQuestionDivId : null,
		sectionId : "",		
		graphicNames : "",
		graphicNamesOrig : "",
		includeOther : false,
		displayPV : false,
		attachedFormIds : null,
		attachedFormNames : new Array(),
		associatedGroupIds : null,
		questionOrder : -1,
		questionOrder_col : -1,
		catOid : "",
		formItemOid :"",
		
		// attribute parameters /////////////////////////////////
		qType : 1,
		required : false,
		answerType : 1,    //1: string, 2: numeric 3: date 4:date-time
		minCharacters : 0,
		maxCharacters : 4000,
		rangeOperator : '',
		rangeValue1 : '',
		rangeValue2 : '',
		
		
		skipRuleOperatorType : -2147483648,
		skipRuleType : -2147483648,
		skipRuleEquals : '',
		questionsToSkip : new Array(),
		skipRuleDependent : false,
		
		
		
		///// HTML attributes
		align : "left",
		vAlign : "top",
		color : "black",
		fontFace : "arial",
		fontSize : '0',
		indent : 0,
		border : false,
		
		horizontalDisplay : false,
		horizDisplayBreak : false,
		dataSpring : false,
		deleteTrigger : false,
		
		///// Email Trigger attributes
		toEmailAddress : '',
		ccEmailAddress : '',
		subject : 'Emailing from Proforms',
		body : '',
		triggerAnswers : new Array(),
		eMailTriggerId : -2147483648,
		eMailTriggerUpdatedBy : -2147483648,
		triggerValues: new Array(),
		
		///// Calculated Form Question Attributes
		calculationType : -2147483648,   //not sure if this is even being used
		conversionFactor : -2147483648,
		questionsToCalculate : new Array(),
		calculation : '',
		calculatedQuestion : false,
		calDependent : false,
		conditionalForCalc : false,
		
		// count information
		countFormula : '',
		questionsInCount : new Array(),
		countFlag : false,
		
		htmlText : '',
		answerTypeDisplay : '',
		
		dataElementName : 'none',
		
		hasBtrisMapping: false,
		isGettingBtrisVal: false,
		//Btris Mapping attributes		
		btrisObservationName: '',
		btrisRedCode: '',
		btrisSpecimenType: '',
		
		prepopulation : false,
		prepopulationValue : '',
		
		decimalPrecision : "-1",
		
		hasUnitConversionFactor : false,
		unitConversionFactor : '',
		// end attribute parameters //////////////////////////////////////////////////
		
		// used by formbuilder only ////////////////////////////////
		active : false,
		validation : false,
		skiprule : false,
		emailTrigger : false,
		rangeOperatorDisplay : "",
		isNew : true,
		renderAfter : null,
		
		// visual scale
		vscaleRangeStart : "-99999",
		vscaleRangeEnd : "-99998",
		vscaleWidth : "100",
		vscaleShowHandle : true,
		vscaleCenterText : "",
		vscaleLeftText : "",
		vscaleRightText : "",
		
		textareaHeight : 60,
		textareaWidth : 100,
		textboxLength : 20,
		
		autoCreated : false,
		hasSavedInDatabase : false,
		disableEditor : false,
		
		
		showText : true,
		tableHeaderType : 0,
		
		//in edit mode we will populated this from DB(Yogi)
		qaId:"-7",
		vsId : "-7",
		etId :-"7",
		sqId:"-7",
		crId:"-7",
		srId:"-7",
		userFileId :"-7",
		etValId:"-7"
		
	},
	
	questionTableAttrs : [
		"questionId",
		"questionVersionLetter",
		"questionVersionNumber",
		"questionName",
		"questionText",
		"descriptionUp",
		"descriptionDown",
		"questionType",
		"hasDecimalPrecision",
		"calDependent",
		"prepopulation",
		"defaultValue",
		"unansweredValue",
		"associatedGroupIds",
		"existingQuestion",
		"forcedNewVersion",
		"questionOptionsObjectArray",
		"imageOption",
		"imageFileName",
		"visualScaleInfo",
		"newQuestionDivId",
		"sectionId",
		"graphicNames",
		//"graphicNamesOrig",
		"includeOther",
		"displayPV",
		"attachedFormIds",
		"questionOrder",
		"questionOrder_col",
		"htmlText",
		//Btris Mapping
		"hasBtrisMapping",
		"isGettingBtrisVal"

	],
	
	/**
	 * If any of these attributes change, other questions on the page with
	 * the same name need to have these updated too.  This is a big deal.
	 * I'm looking at you, RepeatableSectionProcessor
	 */
	questionCoreAttrs : [
		"questionText",
		"descriptionUp",
		"descriptionDown",
		"questionType",
		"questionOptionsObjectArray",
		"defaultValue",
		
		"vscaleRangeStart",
		"vscaleRangeEnd",
		"vscaleWidth",
		"vscaleShowHandle",
		"vscaleCenterText",
		"vscaleLeftText",
		"vscaleRightText",
		"visualScaleInfo"
	],
	
	initialize : function() {
		this.on("change:vscaleRangeStart", this.updateVScaleInfo);
		this.on("change:vscaleRangeEnd", this.updateVScaleInfo);
		this.on("change:vscaleWidth", this.updateVScaleInfo);
		this.on("change:vscaleShowHandle", this.updateVScaleInfo);
		this.on("change:vscaleCenterText", this.updateVScaleInfo);
		this.on("change:vscaleLeftText", this.updateVScaleInfo);
		this.on("change:vscaleRightText", this.updateVScaleInfo);
		this.on("change:visualScaleInfo", this.updateVScale);
		
		this.on("change:answerType", this.changeValidation);
		this.on("change:minCharacters", this.changeValidation);
		this.on("change:maxCharacters", this.changeValidation);
		this.on("change:rangeOperator", this.changeValidation);
		this.on("change:rangeValue1", this.changeValidation);
		this.on("change:rangeValue2", this.changeValidation);
		
		this.on("change:decimalPrecision", this.changeDecimalPrecision);
		
		this.on("change:unitConversionFactor", this.updateUnitConversionFactorFlag);
		
		this.on("change:calculation", this.updateCalcAttributes);
		
		this.on("change:skipRuleOperatorType", this.updateSkipRuleFlag);
		
		this.on("change:questionId", this.checkQuestionIdType);
		
		this.on("change", this.propagateRepeatChange);
		
		// the DE, etc will already be set here if loading for edit so...
		var deName = this.get("dataElementName");
		if (deName != "none") {
			this.associateDE(deName);
		}
		//Called this function to get back the set visual scale value in edit mode
		//this.updateVScale();
		if(this.get("questionType") == Config.questionTypes.visualscale){
			this.updateVScale();
		}
	},
	
	/**
	 * Calculates and sets the div ID of this question.  Can be reset
	 * to a new div ID by passing true to the recalcualte attribute.
	 * 
	 * @param recalculate if set to true, recalculates the div ID
	 */
	calculateDivId : function(recalculate) {
		// need section ID, question ID, and version ID to calculate the divID
		// version number is always 1 if not otherwise assigned
		if (recalculate || this.get("newQuestionDivId") == null) {
			var versionNum, sectionId, questionId;
			
			versionNum = this.get("questionVersionNumber");
			if (versionNum === null) {
				versionNum = 1;
			}
			
			if (this.get("questionId") <= 0) {
				questionId = (FormBuilder.form.countQuestions() + 1) * -1;
			}
			else {
				questionId = this.get("questionId");
			}
			
			// since all questions are added to sections upon creation and
			// this method runs just after creation, this question has a section
			var section = FormBuilder.form.getQuestionParentSection(this);
			sectionId = section.get("id");
			
			this.set("newQuestionDivId", sectionId + "_" + questionId + "_" + versionNum);
		}
	},
	
	getParentSection : function() {
		return FormBuilder.form.section(this.get("sectionId"));
	},
	
	/**
	 * Gets the same question model from the repeatable parent section
	 * 
	 * @return Question Model if found, otherwise undefined
	 */
	getRepeatParentQuestion : function() {
		var thisParentSection = this.getParentSection();
		if (typeof thisParentSection !== "undefined" && thisParentSection.get("isRepeatable")) {
			// get the question inside that section with the same question name as this one
			return thisParentSection.getQuestionByName(this.get("questionName"));
		}
		else {
			return undefined;
		}
	},
	
	isRepeatedChild : function() {
		return this.getParentSection().isRepeatableChild();
	},
	
	isRepeatedParent : function() {
		return this.getParentSection().isRepeatableParent();
	},
	
	isRepeated : function() {
		return this.getParentSection().get("isRepeatable");
	},
	
	propagateRepeatChange : function(model) {
		// only fire the change event if the question has been saved.  Otherwise
		// let the system handle it normally.  This event is for the MAJOR
		// change stuff such as propagating to repeatable children
		if (this.get("questionId") > 0 && this.get("isNew") == false) {
			EventBus.trigger("change:question", model);
		}
	},
	
	/**
	 * Responds to any change of visual scale variables to update the
	 * visualScaleInfo variable appropriately.
	 * 
	 * Performs any model change silently
	 */
	updateVScaleInfo : function() {
		var rangeStart = this.get("vscaleRangeStart");
		var rangeEnd = this.get("vscaleRangeEnd");
		var width = this.get("vscaleWidth");
		var center = this.get("vscaleCenterText");
		var right = this.get("vscaleRightText");
		var left = this.get("vscaleLeftText");
		var alien = Config.alienSymbol;
		
		// the back end uses f and t for this element
		showHandle = (this.get("vscaleShowHandle") === false) ? "f" : "t";
		
		//var vinfo = left + alien + center + alien + right + alien + rangeStart + alien + rangeEnd + alien + width + alien + showHandle;
		//this.set("visualScaleInfo", vinfo, {silent: true});
	},
	
	/**
	 * Responds to a change in the visualScaleInfo variable to update
	 * the visual scale variables appropriately
	 * 
	 * Performs any model change silently
	 */
	updateVScale : function() {
		var vinfo = this.get("visualScaleInfo");
		var json = JSON.parse(vinfo);
		if (vinfo != "") {
			var visualScale = vinfo.split(Config.alienSymbol);
			this.set("vscaleLeftText", json["vscaleLeftText"], {silent: true});
			this.set("vscaleCenterText",json["vscaleCenterText"], {silent: true});
			this.set("vscaleRightText", json["vscaleRightText"], {silent: true});
			this.set("vscaleRangeStart", json["vscaleRangeStart"], {silent: true});
			this.set("vscaleRangeEnd", json["vscaleRangeEnd"], {silent: true});
			this.set("vscaleWidth", json["vscaleWidth"], {silent: true});
			var scaleCursor = true;
			this.set("vscaleShowHandle", json["vscaleShowHandle"], {silent: true});
		}
	},
	
	/**
	 * Sets the data element of this Question to "none"
	 */
	removeDE : function() {
		var deName = this.get("dataElementName");
		if (deName !== "none") {
			this.set("dataElementName", "none");
			FormBuilder.form.dataElements.byName(deName).deassociate(this);
		}
	},
	
	associateDE : function(dataElementName) {
		var dataElementModel = FormBuilder.form.dataElements.byName(dataElementName);
		if (dataElementModel) {
			this.set("dataElementName", dataElementModel.get("dataElementName"));
			dataElementModel.associate(this);
		}
	},
	
	changeValidation : function(model) {
		if (model.get("answerType") == 1) {
			model.set("validation", false);
		}
		else {
			model.set("validation", true);
		}
	},
	
	changeDecimalPrecision : function(model) {
		var decimalPrecision = model.get("decimalPrecision");
		if (decimalPrecision != "actualvalue" && decimalPrecision != "-1") {
			model.set("validation", true);
		}
	},
	
	checkQuestionIdType : function(model) {
		model.set("questionId", Number(model.get("questionId")));
	},
	
	saveToDatabase : function(isNew) {

		var qId = this.get("questionId");
		//var addURL = baseUrl+"/form/addEditQuestion.action?action=addQuestionAjax";
		var addURL = baseUrl+"questionSave!saveQuestionAjax.action";
		var editURL = baseUrl+"/form/addEditQuestion.action?action=editQuestionAjax&qId="+qId+"&qVersion=1";
		
		var questionInfoURL;
		if(isNew) {
			questionInfoURL = addURL;
		}else {
			questionInfoURL = editURL;
		}
		
		
		var params = this.serializeModel();
		var thisModel = this;
		return $.ajax({
			type:"post",
			url:questionInfoURL,
			data:params,
			success: function(response){
				//alert("doAddEditQuestionAjaxPost(): the post Parameters:\n" +params + "\n\nResponse:\n" + response);
				switch(response){
				case 'ERROR_DUPLICATE_QUESTION':
				case 'ImageMap_NOTDONE':
				case 'ERROR_DefaultValue_NEED':
					return response;
					break;
				default:
					var qJSON = JSON.parse(response);
					var qId = qJSON.questionId;				
					thisModel.set('questionId',qJSON.questionId);
					//thisModel.set('questionVersionLetter',qJSON.questionVersionString);
					//thisModel.set('questionVersionNumber',qJSON.questionVersionNumber);
					thisModel.calculateDivId(true);
					return "success";
				}
			},
			error: function(e){
				alert("error" + e );
				return "ERROR_ajaxError";
			}
		});
	},
	
	getQuestionTypeLabel : function(type) {
		if(type == '1') {
			return 'Textbox';
		}else if(type == '2') {
			return 'Textarea';
		}else if(type == '3') {
			return 'Select';
		}else if(type == '4') {
			return 'Radio';
		}else if(type == '5') {
			return 'Multi-Select';
		}else if(type == '6') {
			return 'Checkbox';
		}else if(type == '7') {
			return 'Calculated';
		}else if(type == '8') {
			return 'Patient Calendar';
		}else if(type == '9') {
			return 'Image Map';
		}else if(type == '10') {
			return 'Visual Scale';
		}else if(type == '11') {
			return 'File';
		}else if(type == '12') {
			return 'Textblock';
		}
	},
	
	getQuestionTypeNum : function(type){
		if(type == 'Textbox') {
			return '1';
		}else if(type == 'Textarea') {
			return '2';
		}else if(type == 'Select') {
			return '3';
		}else if(type == 'Radio') {
			return '4';
		}else if(type == 'Multi-Select') {
			return '5';
		}else if(type == 'Checkbox') {
			return '6';
		}else if(type == 'Calculated') {
			return '7';
		}else if(type == 'Patient Calendar') {
			return '8';
		}else if(type == 'Image Map') {
			return '9';
		}else if(type == 'Visual Scale') {
			return '10';
		}else if(type == 'File') {
			return '11';
		}else if(type == 'Textblock') {
			return '12';
		}
	},
	
	/*created by Ching Heng*/
	serializeModel : function(){
		var params = this.toJSON();
		var KEYs = Object.keys(params);
		var serialized='';
		for(key in KEYs){
			//if (KEYs[key] == 'questionText'){
				serialized += this.mappingRules(params,KEYs,key);				
			//}
		}
		//alert(serialized);
		return serialized.substring(0,serialized.length-1);
	},
	
	mappingRules : function(params,KEYs,key){
		switch(KEYs[key]){
		case 'questionId':
			return 'questionForm.id='+params[KEYs[key]]+'&';
			break;
		case 'questionName':
			return 'questionForm.'+KEYs[key]+'='+params[KEYs[key]]+'&';
			break;
		case 'questionText':
			return 'questionForm.text='+ _.fixedEncodeURIComponent(params[KEYs[key]]).replace(/\s/g,'+')+'&';
			break;
		case 'questionType':
			return 'questionForm.type='+params[KEYs[key]]+'&';
			break;
		/*case 'qType':
			return 'questionForm.type='+params[KEYs[key]]+'&';
			break;*/
		case 'skipRuleDependent':
			return 'questionForm.'+KEYs[key]+'='+params[KEYs[key]]+'&';
			break;
		case 'skiprule':
			return 'questionForm.'+KEYs[key]+'='+params[KEYs[key]]+'&';
			break;
		case 'defaultValue':
			return 'questionForm.'+KEYs[key]+'='+params[KEYs[key]]+'&';
			break;
		case 'unansweredValue':
			return 'questionForm.'+KEYs[key]+'='+params[KEYs[key]]+'&';
			break;
		case 'vscaleRangeStart':
			return 'questionForm.rangeStart='+params[KEYs[key]]+'&';
			break;
		case 'vscaleRangeEnd':
			return 'questionForm.rangeEnd='+params[KEYs[key]]+'&';
			break;
		case 'vscaleWidth':
			return 'questionForm.width='+params[KEYs[key]]+'&';
			break;
		case 'vscaleRightText':
			return 'questionForm.rightText='+params[KEYs[key]]+'&';
			break;
		case 'vscaleLeftText':
			return 'questionForm.leftText='+params[KEYs[key]]+'&';
			break;
		case 'vscaleCenterText':
			return 'questionForm.centerText='+params[KEYs[key]]+'&';
			break;
		case 'vscaleShowHandle':
			return 'questionForm.showHandle='+params[KEYs[key]]+'&';
			break;
		case 'questionOptionsObjectArray':
			var result = '';
			if(params[KEYs[key]] != null){
				var optionsArrObjectArray = params[KEYs[key]];
				result = 'questionForm.questionOptionsJSON=' + encodeURIComponent(JSON.stringify(optionsArrObjectArray)) + '&';
			}
			return result;
			break;
		case 'required':
			return 'questionForm.'+KEYs[key]+'='+params[KEYs[key]]+'&';
			break;
		case 'answerType':
			return 'questionForm.'+KEYs[key]+'='+params[KEYs[key]]+'&';
			break;
		case 'minCharacters':
			return 'questionForm.'+KEYs[key]+'='+params[KEYs[key]]+'&';
			break;
		case 'maxCharacters':
			return 'questionForm.'+KEYs[key]+'='+params[KEYs[key]]+'&';
			break;
		case 'align':
			return 'questionForm.'+KEYs[key]+'='+params[KEYs[key]]+'&';
			break;
		case 'vAlign':
			return 'questionForm.'+KEYs[key]+'='+params[KEYs[key]]+'&';
			break;
		case 'color':
			return 'questionForm.'+KEYs[key]+'='+params[KEYs[key]]+'&';
			break;
		case 'fontFace':
			return 'questionForm.'+KEYs[key]+'='+params[KEYs[key]]+'&';
			break;
		case 'fontSize':
			return 'questionForm.'+KEYs[key]+'='+params[KEYs[key]]+'&';
			break;
		case 'indent':
			return 'questionForm.'+KEYs[key]+'='+params[KEYs[key]]+'&';
			break;
		case 'skipRuleType':
			return 'questionForm.'+KEYs[key]+'='+params[KEYs[key]]+'&';
			break;
		/*case 'questionsToSkip':
			return 'questionForm.'+KEYs[key]+'='+params[KEYs[key]]+'&';
			break;*/
		case 'skipRuleEquals':
			return 'questionForm.'+KEYs[key]+'='+params[KEYs[key]]+'&';
			break;	
		case 'skipRuleOperatorType':
			return 'questionForm.'+KEYs[key]+'='+params[KEYs[key]]+'&';
			break;
		case'rangeOperator':
			return 'questionForm.'+KEYs[key]+'='+params[KEYs[key]]+'&';
			break;
		case 'rangeValue1':
			return 'questionForm.'+KEYs[key]+'='+String(params[KEYs[key]])+'&';
			break;
		case 'rangeValue2':
			return 'questionForm.'+KEYs[key]+'='+String(params[KEYs[key]])+'&';
			break;
			
			
		case 'calculation':
			return 'questionForm.calculation='+params[KEYs[key]]+'&';
			break;
		case 'calculationType':
			return 'questionForm.calculationType='+params[KEYs[key]]+'&';
			break;
		case 'questionsToCalculate':
			return 'questionForm.questionsToCalculate='+params[KEYs[key]]+'&';
			break;
		case 'calculatedQuestion':
			return 'questionForm.calculatedQuestion='+params[KEYs[key]]+'&';
			break;
		case 'calDependent':
			return 'questionForm.calDependent='+params[KEYs[key]]+'&';
			break;
		case 'conditionalForCalc':
			return 'questionForm.conditionalForCalc='+params[KEYs[key]]+'&';
			break;	
			
		case 'countFormula':
			return 'questionForm.countFormula='+params[KEYs[key]]+'&';
			break;
		case 'questionsInCount':
			return 'questionForm.questionsInCount='+params[KEYs[key]]+'&';
			break;
		case 'countFlag':
			return 'questionForm.countFlag='+params[KEYs[key]]+'&';
			break;
			
		case 'horizontalDisplay':
			return 'questionForm.'+KEYs[key]+'='+params[KEYs[key]]+'&';
			break;
		case 'horizDisplayBreak':
			return 'questionForm.'+KEYs[key]+'='+params[KEYs[key]]+'&';
			break;
		case 'textareaHeight':
			return 'questionForm.'+KEYs[key]+'='+params[KEYs[key]]+'&';
			break;
		case 'textareaWidth':
			return 'questionForm.'+KEYs[key]+'='+params[KEYs[key]]+'&';
			break;
		case 'textboxLength':
			return 'questionForm.'+KEYs[key]+'='+params[KEYs[key]]+'&';
			break;
		case  'emailTrigger':	
			return 'emailTrigger.'+KEYs[key]+'='+params[KEYs[key]]+'&';
			break;
		case 'toEmailAddress':
			return 'questionForm.emailTrigger.toEmailAddress='+params[KEYs[key]]+'&';
			break;
		case 'ccEmailAddress':
			return 'questionForm.emailTrigger.ccEmailAddress='+params[KEYs[key]]+'&';
			break;
		case 'subject':
			return 'questionForm.emailTrigger.subject='+params[KEYs[key]]+'&';
			break;
		case 'body':
			return 'questionForm.emailTrigger.body='+params[KEYs[key]]+'&';
			break;
		case 'deleteTrigger':
			return 'questionForm.'+KEYs[key]+'='+params[KEYs[key]]+'&';
			break;
		case 'eMailTriggerId':
			return 'questionForm.'+KEYs[key]+'='+params[KEYs[key]]+'&';
			break;
		case 'dataSpring':
			return 'questionForm.'+KEYs[key]+'='+params[KEYs[key]]+'&';
			break;
		case 'htmlText':
			return 'questionForm.'+KEYs[key]+'='+params['htmlText']+'&';
			break;
		case 'descriptionUp':
			return 'questionForm.'+KEYs[key]+'='+params[KEYs[key]]+'&';
			break;
		case 'descriptionDown':
			return 'questionForm.'+KEYs[key]+'='+params[KEYs[key]]+'&';
			break;
		case 'includeOther':
			return 'questionForm.includeOtherOption='+params[KEYs[key]]+'&';
			break;
		case 'prepopulation':
			return 'questionForm.'+KEYs[key]+'='+params[KEYs[key]]+'&';
			break;
		case 'prepopulationValue':
			return 'questionForm.'+KEYs[key]+'='+params[KEYs[key]]+'&';
			break;
		case 'decimalPrecision':
			return 'questionForm.'+KEYs[key]+'='+params[KEYs[key]]+'&';
			break;
		case 'qaId':
			return 'questionForm.'+KEYs[key]+'='+params[KEYs[key]]+'&';
			break;	
		case 'vsId':
			return 'questionForm.'+KEYs[key]+'='+params[KEYs[key]]+'&';
			break;	
		case 'etId':
			return 'questionForm.'+KEYs[key]+'='+params[KEYs[key]]+'&';
			break;	
		case 'etValId':
			return 'questionForm.'+KEYs[key]+'='+params[KEYs[key]]+'&';
			break;	
		case 'sqId':
			return 'questionForm.'+KEYs[key]+'='+params[KEYs[key]]+'&';
			break;	
		case 'crId':
			return 'questionForm.'+KEYs[key]+'='+params[KEYs[key]]+'&';
			break;	
		case 'srId':
			return 'questionForm.'+KEYs[key]+'='+params[KEYs[key]]+'&';
			break;	
		case 'userFileId':
			return 'questionForm.'+KEYs[key]+'='+params[KEYs[key]]+'&';
			break;	
		case 'formItemOid':
			return 'questionForm.'+KEYs[key]+'='+params[KEYs[key]]+'&';
			break;
		case 'catOid':
			return 'questionForm.'+KEYs[key]+'='+params[KEYs[key]]+'&';
			break;
		default:
			return '';
			
		}
	},
	
	toQuestionObj : function(){
		var questionObj = new Object();
		questionObj.attributeObject = this.toAttributeObj();
		questionObj.questionId = this.get("questionId");
		questionObj.prepopulation = this.get("prepopulation");
		questionObj.hasDecimalPrecision = this.get("hasDecimalPrecision");
		questionObj.associatedGroupIds = this.get("associatedGroupIds");
		questionObj.questionOptionsObjectArray = this.get("questionOptionsObjectArray");
		
		questionObj.newQuestionDivId = this.get("newQuestionDivId");
		questionObj.imageOption = this.get("imageOption");
		questionObj.questionVersionLetter = this.get("questionVersionLetter");
		questionObj.attachedFormNames = this.get("attachedFormNames");
		questionObj.descriptionUp = this.get("descriptionUp");
		questionObj.questionText = this.get("questionText");
		questionObj.attachedFormIds = this.get("attachedFormIds");
		questionObj.questionType = this.get("questionType");
		questionObj.questionVersionNumber = this.get("questionVersionNumber");
		questionObj.defaultValue = this.get("defaultValue");
		questionObj.graphicNames = this.get("graphicNames");
		questionObj.unansweredValue = this.get("unansweredValue");
		questionObj.descriptionDown = this.get("descriptionDown");
		questionObj.forcedNewVersion = this.get("forcedNewVersion");
		questionObj.sectionId = this.modifySectionId(this.get("sectionId"));
		questionObj.imageFileName = this.get("imageFileName");
		questionObj.questionOrder = this.get("questionOrder");
		questionObj.questionOrder_col = this.get("questionOrder_col");
		questionObj.questionName = this.get("questionName");
		questionObj.includeOther = this.get("includeOther");
		questionObj.displayPV = this.get("displayPV");
		questionObj.existingQuestion = this.get("existingQuestion");
		questionObj.visualScaleInfo = this.get("visualScaleInfo");
		questionObj.hasCalDependent = this.get("calDependent");
		
		//
		questionObj.qaId = this.get("qaId");
		questionObj.etId = this.get("etId");
		questionObj.etValId = this.get("etValId");
		questionObj.emailTrigger = this.get("emailTrigger");
		questionObj.sqId = this.get("sqId");
		questionObj.crId = this.get("crId");
		questionObj.srId = this.get("srId");
		questionObj.userFileId = this.get("userFileId");
	
		//added by Ching-Heng
		questionObj.catOid = this.get("catOid");
		questionObj.formItemOid = this.get("formItemOid");	
		
		//BTRIS Mapping
		questionObj.hasBtrisMapping = this.get("hasBtrisMapping");
		questionObj.isGettingBtrisVal = this.get("isGettingBtrisVal");
		
		return questionObj;
	},
	
	toAttributeObj : function(){
		var attributeObj = new Object();
		attributeObj.qaId=this.get("qaId");
		attributeObj.vsId=this.get("vsId");
		attributeObj.etId=this.get("etId");
		attributeObj.etValId=this.get("etValId");
		attributeObj.sqId=this.get("sqId");
		attributeObj.crId=this.get("crId");
		attributeObj.srId=this.get("srId");
		attributeObj.userFileId=this.get("userFileId");
		attributeObj.body = this.get("body");
		attributeObj.vAlign = this.get("vAlign");
		attributeObj.subject = this.get("subject");
		attributeObj.hasUnitConversionFactor = this.get("hasUnitConversionFactor");
		attributeObj.maxCharacters = this.get("maxCharacters");
		attributeObj.prepopulation = this.get("prepopulation");
		attributeObj.toEmailAddress = this.get("toEmailAddress");
		attributeObj.answerType = this.get("answerType");
		attributeObj.questionsToCalculate = this.get("questionsToCalculate");
		attributeObj.ccEmailAddress = this.get("ccEmailAddress");
		attributeObj.emailTrigger = this.get("emailTrigger");
		attributeObj.unitConversionFactor = this.get("unitConversionFactor");
		attributeObj.dataElementName = this.get("dataElementName");
		attributeObj.questionsToSkip = this.get("questionsToSkip");
		attributeObj.textareaWidth = this.get("textareaWidth");
		attributeObj.calculation = this.get("calculation");
		attributeObj.horizontalDisplay = this.get("horizontalDisplay");
		attributeObj.htmlText = this.get("htmlText");
		attributeObj.rangeOperator = String(this.get("rangeOperator"));
		attributeObj.eMailTriggerId = this.get("eMailTriggerId");
		attributeObj.deleteTrigger = this.get("deleteTrigger");
		attributeObj.triggerAnswers = this.get("triggerAnswers");
		attributeObj.triggerValues = this.get("triggerValues");
		attributeObj.required = this.get("required");
		attributeObj.answerTypeDisplay = this.get("answerTypeDisplay");
		attributeObj.rangeValue2 = String(this.get("rangeValue2"));
		attributeObj.rangeValue1 = String(this.get("rangeValue1"));
		attributeObj.calDependent = this.get("calDependent");
		attributeObj.conversionFactor = this.get("conversionFactor");
		attributeObj.eMailTriggerUpdatedBy = this.get("eMailTriggerUpdatedBy");
		attributeObj.indent = this.get("indent");
		attributeObj.textboxLength = this.get("textboxLength");
		attributeObj.skipRuleEquals = this.get("skipRuleEquals");
		attributeObj.align = this.get("align");
		attributeObj.fontFace = this.get("fontFace");
		attributeObj.prepopulationValue = this.get("prepopulationValue");
		attributeObj.fontSize = this.get("fontSize");
		attributeObj.skipRuleType = this.get("skipRuleType");
		attributeObj.decimalPrecision = this.get("decimalPrecision");
		attributeObj.qType = this.get("questionType");
		attributeObj.skipRuleOperatorType = this.get("skipRuleOperatorType");
		attributeObj.calculatedQuestion = this.get("calculatedQuestion");
		attributeObj.conditionalForCalc = this.get("conditionalForCalc");
		
		// count
		attributeObj.countFormula = this.get("countFormula");
		attributeObj.questionsInCount = this.get("questionsInCount");
		attributeObj.countFlag = this.get("countFlag");
		
		attributeObj.textareaHeight = this.get("textareaHeight");
		attributeObj.dataSpring = this.get("dataSpring");
		attributeObj.color = this.get("color");
		attributeObj.calculationType = this.get("calculationType");
		attributeObj.horizDisplayBreak = this.get("horizDisplayBreak");
		attributeObj.minCharacters = this.get("minCharacters");
		attributeObj.skipRuleDependent = this.get("skipRuleDependent");
		
		attributeObj.skiprule = this.get("skiprule");
		// visual scale
		attributeObj.vscaleRangeStart = this.get("vscaleRangeStart");
		attributeObj.vscaleRangeEnd = this.get("vscaleRangeEnd");
		attributeObj.vscaleWidth = this.get("vscaleWidth");
		attributeObj.vscaleShowHandle = this.get("vscaleShowHandle");
		attributeObj.vscaleCenterText = this.get("vscaleCenterText");
		attributeObj.vscaleLeftText = this.get("vscaleLeftText");
		attributeObj.vscaleRightText = this.get("vscaleRightText");
		attributeObj.showText = this.get("showText");
		attributeObj.tableHeaderType = this.get("tableHeaderType");
		
		attributeObj.btrisObservationName = this.get("btrisObservationName");
		attributeObj.btrisRedCode = this.get("btrisRedCode");
		attributeObj.btrisSpecimenType = this.get("btrisSpecimenType");

		return attributeObj;
	},
	
	setOptionsOrderVaL:function() {
		var qOptObj = this.get("questionOptionsObjectArray");
		for (var i = 0; i < qOptObj.length; i++) {
			qOptObj[i].orderVal = i + 1;
		}

	},

	
	updateUnitConversionFactorFlag : function() {
		var unitConversionFactor = this.get("unitConversionFactor");
		if(unitConversionFactor.trim() == "") {
			this.set("hasUnitConversionFactor",false);
		}else {
			this.set("hasUnitConversionFactor",true);
		}
		
	},
	
	modifySectionId : function(id){
		var num_id = Number(id.replace("S_",""));
		if(num_id > 0){
			return String(num_id);
		}else{
			return String(id);
		}
	},
	
	
	contains : function(a, obj) {
		 var i = a.length;
		    while (i--) {
		       if (a[i] === obj) {
		           return true;
		       }
		    }
		    return false;
	},
	
	updateSkipRuleFlag : function() {
		var skipRuleOperatorType = this.get("skipRuleOperatorType");
		if(skipRuleOperatorType == -2147483648) {
			this.set("skiprule",false);
		}else {
			this.set("skiprule",true);
		}
	},
	
	//e.g. "[S_32_Q_432] [S_33_Q_455]"
	reformatCalculationDisplay : function(str) {

		var questionsRegEx = /\[S_[-]?\d+_Q_([0-9]+)\]/g;
		var allQuestions = str.match(questionsRegEx);
		var uniqueAllQuestions = [];

		//var activeSection = FormBuilder.page.get("activeSection");
		//var activeSectionId = activeSection.get("id");
		
		var activeSectionId = this.get("sectionId");
		
		if(allQuestions != null) {
			
			//set up unique question list
			for(var i=0;i<allQuestions.length;i++) {
				var qString = allQuestions[i];
				var found = false;
				for(var k=0;k<uniqueAllQuestions.length;k++) {
					var uString = uniqueAllQuestions[k];
					if(qString == uString) {
						found = true;
						break;
					}
				}
				if(!found) {
					uniqueAllQuestions.push(qString);
				}
			}
			
			for(var i=0;i<uniqueAllQuestions.length;i++) {
				var qString = uniqueAllQuestions[i];
				//String questionId= qString.substring(1,qString.indexOf("_Q_"));
				//String sectionId = qString.substring(qString.indexOf("S_")+1,qString.indexOf("_Q_"));
				//qString is in format s_##_q_$$
				//we need div id of this question which is in format s_##_$$_1   
				var qDivId = qString.replace("Q_","");
				qDivId = qDivId.replace("[","");
				qDivId = qDivId.replace("]","");
				qDivId = qDivId + "_1";
				var secId=qString.substring(qString.lastIndexOf("S_")+2,qString.lastIndexOf("_Q_"));
				var qId=qString.substring(qString.lastIndexOf("_Q_")+3,qString.lastIndexOf("]"));
				//var question = FormBuilder.form.getQuestionByDivId(qDivId);
				var question = FormBuilder.form.getQuestionByQuestionIdSectionId(qId,secId);
				var deName = question.get("dataElementName");
				
				
				//find out if this came from repeatable section
				var pos = qString.indexOf("_Q_");
				var sDivId = qString.substring(1,pos);  //use 1 as start index bc it we remove the [
				//var section = FormBuilder.form.getSectionByDivId(sDivId);
				
				
				
				//var underScoreSection = "S_"+secId;
				var section =FormBuilder.form.getSectionBySectionId(secId);
				//var sId = section.id;
				var sId = section.get("id");
				
				if(section.get("isRepeatable") && (!(sId == activeSectionId))) {
					var index = section.getRepeatableIndex();
					deName = deName.replace(".", "(" + index + ").")	
				}
				str = _.replaceAll(str, qString, "[" + deName + "]");

			}

		} //end if(allQuestions!= null)

		return str;

	},
	
	
	updateCalcAttributes : function() {
		var calculation = this.get("calculation");
		if(calculation.trim() == "") {
			this.set("calculatedQuestion",false);
			this.set("questionsToCalculate",new Array());
			this.set("conversionFactor",-2147483648);
			this.set("conditionalForCalc",false);
			
		}else {
			this.set("calculatedQuestion",true);
			var matchRegex = new RegExp(/\[S_[-]?\d+_Q_\d+\]/g); //the question looks like [S_123_Q_123]
			var splitRegex = new RegExp(/[\[S_[-]?\d+_Q_d+\]]/);
			var matchs = calculation.trim().match(matchRegex);
			var questionsToCalculateArr = new Array();
			if (matchs!=null) {
				for (var j=0; j<matchs.length; j++) {
					var dependentQId=matchs[j].split(splitRegex);
					for (var h=0;h<dependentQId.length;h++) {
						if (dependentQId[h]!='') {
							if (!this.contains(questionsToCalculateArr, dependentQId[h])) {
								questionsToCalculateArr.push(dependentQId[h]);
							}
						}
					}
				}
			}
			this.set("questionsToCalculate",questionsToCalculateArr);
			
		}
	}
});



function OptionsObject(configObj) {
	this.option = configObj.option || "";
	this.score = configObj.score || "";
	this.submittedValue = configObj.submittedValue || "";
	this.orderVal = configObj.orderVal || "";
	this.elementOid = configObj.elementOid || "";
	this.itemResponseOid = configObj.itemResponseOid || "";
}