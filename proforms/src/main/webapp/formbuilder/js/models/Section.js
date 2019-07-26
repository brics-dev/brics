var Section = BaseModel.extend({
	defaults : {
		active : true,
		id : "-1",
		name : "",
		isCollapsable : false,
		ifHasResponseImage : false,
		isRepeatable : false,
		repeatedSectionParent : "-1",
		initRepeatedSecs : 0,
		maxRepeatedSecs : 0,
		repeatableGroupName : "None",
		divId : "",
		row : -1,
		col : -1,
		description: "",
		existingSection : false,
		gridtype : false,
		
		editorCollapsed : false,
		isNew : true,
		textContainer : false,
		
		renderAfter : null,
		disableEditor : false,
		
		tableGroupId : 0,
		tableHeaderType : 0
		
	},
	
	initialize : function(){
		this.questions = new Questions();
		this.calculateDivId();
		
		this.on("change:isRepeatable", this.onChangeRepeatable);
		this.on("change:repeatableGroupName", this.onChangeRepeatableGroupName);
	},
	
	save : function() {
		//console.log("save section");
	},
	
	onChangeRepeatableGroupName : function(model) {
		var preRepeatableGroup = FormBuilder.form.repeatableGroups.byName(model.previous("repeatableGroupName"));
		var repeatableGroup = FormBuilder.form.repeatableGroups.byName(model.get("repeatableGroupName"));
		if(typeof preRepeatableGroup !== "undefined"){
			preRepeatableGroup.set("usedByotherSection",false);
		}
		if(typeof repeatableGroup !== "undefined"){
			repeatableGroup.set("usedByotherSection",true);
		}
		model.clearQuestionsDE();
	},
	
	clearQuestionsDE : function(){
		var allQuestion = this.questions;
		allQuestion.forEach(function(question){
			question.removeDE();
		});
	},
	
	isRepeatableChild : function() {
		return this.get("isRepeatable") && this.get("repeatedSectionParent") !== "-1";
	},
	
	isRepeatableParent : function() {
		return this.get("isRepeatable") && this.get("repeatedSectionParent") === "-1";
	},
	
	isTableSection : function() {
		return this.get("tableGroupId") != 0;
	},
	
	isTableHeaderType : function(typeId) {
		if (this.isTableSection()) {
			return this.get("tableHeaderType") == typeId;
		}
		return false;
	},
	
	isTablePrimary : function() {
		return this.isTableHeaderType(Config.tableHeaderTypes.none);
	},
	
	isTableColumnHeader : function() {
		return this.isTableHeaderType(Config.tableHeaderTypes.columnHeader);
	},
	
	isTableTitle : function() {
		return this.isTableHeaderType(Config.tableHeaderTypes.tableHeader);
	},
	
	/**
	 * Gets the repeatable section parent section of this one
	 * 
	 * @return Section model if found, otherwise undefined
	 */
	getRepeatParentSection : function() {
		if (this.isRepeatableChild()) {
			return FormBuilder.form.section(this.get("repeatedSectionParent"));
		}
		else {
			return undefined;
		}
	},
	
	getRepeatableIndex : function() {
		if(this.isRepeatableChild()) {
			var parent = this.getRepeatParentSection();
			var children = FormBuilder.form.sections.getRepeatableChildren(parent);
			var currCID = this.cid;
			for(var i=0;i<children.length;i++) {
				if(children[i].cid == currCID) {
					return i+2;
				}
			}
			
		}else {
			return 1;
		}
		
		return null;
		
		
	},
	
	calculateDivId : function() {
		if (this.get("divId") === "") {
			this.set("divId", this.get("id"));
		}
	},
	
	onChangeActive : function(model) {
		// any time we're changing active status, we need to check and possibly
		// change the active question
		var activeQuestion = model.questions.findWhere({active: true});
		if (model.get('active')) {
			if (typeof activeQuestion == "undefined" && model.questions.length > 0) {
				// there is no active question in this section, make the first
				// the active one
				model.questions.at(0).set({active: true});
			}
		}
		else {
			// we're going to inactive, inactivate!
			if (typeof activeQuestion != "undefined") {
				activeQuestion.set({active: false});
			}
		}
	},
	
	onChangeRepeatable : function() {
		var isRepeatable = this.get("isRepeatable");
		if (!isRepeatable) {
			// is newly not repeatable
			this.set("repeatableGroupName", "None");
			this.set("initRepeatedSecs", 0);
			this.set("maxRepeatedSecs", 0);
		}else {
			this.set("initRepeatedSecs", 1);
			this.set("maxRepeatedSecs", 1);
		}
	},
	
	addQuestion : function(config) {
		var question = null;
		if (typeof config["cid"] !== "undefined") {
			// config is a model.  That means we just need to add the model
			// instead of creating it as part of the collection
			config.set("sectionId", this.get("id"));
			question = this.questions.add(config);
		}
		else {
			// if we're adding a question that already exists but isn't a model, it might have a qID
			if (typeof config.questionId === "undefined" || config.questionId == "") {
				config.questionId = String((this.questions.length + 1) * -1);
			}
			
			config.sectionId = this.get("id");
			question = this.questions.create(config);
		}
		return question;
	},
	
	/**
	 * Gets the question by reference.  This reference could be id, cid, or the
	 * entire model
	 * 
	 * @param reference id, cid, or model
	 * @returns Question model
	 */
	question : function(reference) {
		return this.questions.get(reference);
	},
	
	/**
	 * Removes a question from the questions collection.  Also calls
	 * this.removeChildSectionQuestions to remove this question from
	 * child sections.
	 * 
	 * @param reference id, cid, or model reference to the question
	 */
	removeQuestion : function(reference) {
		var question = this.question(reference);
		if (question !== null) {
			EventBus.trigger("remove:question", question, this);
			this.questions.remove(question);
		}
		
		this.removeChildSectionQuestions(question);
	},
	
	/**
	 * Removes the question referenced from sections that are child sections
	 * of this section.
	 * 
	 * @param reference id, cid, or model
	 */
	removeChildSectionQuestions : function(reference) {
		if (this.isRepeatableParent()) {
			var children = FormBuilder.form.sections.getRepeatableChildren(this);
			_.each(children, function(section) {
				section.removeQuestion(reference);
			});
		}
	},
	
	/**
	 * A sub-function of Form.getQuestionByDivId() that searches this particular
	 * section.
	 * 
	 * @param divId the div ID to search for
	 * @returns the Question model if it exists, otherwise null
	 */
	getQuestionByDivId : function(divId) {
		return this.questions.findWhere({newQuestionDivId : divId});
	},
	
	/**
	 * Gets the questions array in the order they appear on the page.  Not
	 * guaranteed (because there's no check) but highly likely to get all
	 * questions.
	 * 
	 * @return question model array
	 */
	getQuestionsInPageOrder : function() {
		var section = this;
		var output = [];
		if (section.isRepeatableChild()) {
			// run this same function but on the repeatable parent section then
			// get the associated question inside this model
			var parentSection = FormBuilder.form.section(section.get("repeatedSectionParent"));
			var questionsOrdered = parentSection.getQuestionsInPageOrder(); // array of question models
			for (var i = 0; i < questionsOrdered.length; i++) {
				var parentQuestion = questionsOrdered[i];
				output.push(section.getQuestionByName(parentQuestion.get("questionName")));
			}
		}
		else {
			var $section = $("#" + section.get("divId"));
			var $questions = $section.find("." + Config.identifiers.question);
			
			$questions.each(function() {
				var $question = $(this);
				var question = section.getQuestionByDivId($question.attr("id"));
				output.push(question);
			});
		}
		
		return output;
	},
	
	/**
	 * Finds all questions in this seciton that match the passed question name
	 * 
	 * @param questionName the question name to search for
	 * @returns array of question models
	 */
	getQuestionByName : function(questionName) {
		return this.questions.findWhere({questionName : questionName});
	},
	
	getQuestionById : function(Id) {
		return this.questions.findWhere({questionId : Id});
	},

	// get the question which hasn't been created in the data base
	getNewQuestion : function(){
		var mainThis = this;
		var nweQuestion;
		mainThis.questions.forEach(function(question){
			if(question.get("questionId") < 0){
				nweQuestion = mainThis.getQuestionById(question.get("questionId"));
			}
		});
		return nweQuestion;
	},
	
	/**
	 * Counts the questions in this section
	 * 
	 * @returns integer count of questions
	 */
	countQuestions : function() {
		return this.questions.length;
	},
	
	/*added by Ching Heng*/
	
	toJsonObj : function(){
		var sectionObj = new Object();
		sectionObj.name = this.get("name");
		sectionObj.description = this.get("description");
		sectionObj.isCollapsable = this.get("isCollapsable");
		sectionObj.isRepeatable = this.get("isRepeatable");
		sectionObj.initRepeatedSecs = this.get("initRepeatedSecs");
		sectionObj.maxRepeatedSecs = this.get("maxRepeatedSecs");
		sectionObj.repeatedSectionParent = this.modifyId(this.get("repeatedSectionParent"));
		sectionObj.repeatableGroupName = this.get("repeatableGroupName");
		sectionObj.id = this.modifyId(this.get("id"));
		sectionObj.existingSection = this.get("existingSection");
		sectionObj.row = this.get("row");
		sectionObj.col = this.get("col");
		sectionObj.gridtype = this.get("gridtype");
		sectionObj.tableGroupId = this.get("tableGroupId");
		sectionObj.tableHeaderType = this.get("tableHeaderType");
		return sectionObj;
	},
	
	modifyId : function(id){ 
		var num_id = Number(id.replace("S_",""));
		if(num_id > 0){
			return String(num_id);
		}else{
			return String(id);
		}
	},
	
	releaseRepeatableGroup : function(){
		var repeatableGroup = FormBuilder.form.repeatableGroups.byName(this.get("repeatableGroupName"));		
		if(typeof repeatableGroup !== "undefined"){
			repeatableGroup.set("usedByotherSection",false);
		}
	},
	
	hasCalculationQuestion : function(){
		var flag = false;
		this.questions.forEach(function(question){
			if(question.get("calDependent")){
				flag = true;
			}
		});
		return flag;
	},
	
	hasSkipQuestion : function(){
		var flag = false;
		this.questions.forEach(function(question){
			if(question.get("skipRuleDependent")){
				flag = true;
			}
		});
		return flag;
	}
	
});