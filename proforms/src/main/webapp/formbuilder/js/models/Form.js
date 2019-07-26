var Form = BaseModel.extend({ 
	defaults : {
		name : "",
		formid : 0,
		access : 1,
		dataEntryFlag : 1,
		copyrightedForm : false,
		status : 1,
		submitData : {
			existingsectionIdsToDeleteArray : [],
			existingQuestionIdsToDeleteArray : []
		},
		/* added by Ching Heng */
		formtypeid : 10,
		id : -2147483648, /* is it same as formid?*/
		dataStructureName : "",
		dataStructureVersion : "",
		dataStructureRadio : "",
		tabdisplay : false,
		description : "",
		allowMultipleCollectionInstances : false,
		statusHidden : null,
		nonpatientformtypeid : 0,
		formHeader : "",
		formFooter : "",
		formborder : false,
		formfont : "arial",
		fontSize: 10,
		formcolor : "black",
		cellpadding : 2,
		sectionborder : "yes",
		sectionfont : "arial",
		sectioncolor : "black",
		
		// used only by the formbuilder
		isDataSpring : false,
		saved : false
	},
	
	sections : null,
	dataElements : null,
	repeatableGroups : null,
	allGroups : null,
	
	initialize: function(){
		this.sections = new Sections();
		this.dataElements = new DataElements();
		this.repeatableGroups = new RepeatableGroups();
		this.allGroups = new RepeatableGroups();
		this.deleteSections = new DeleteSections();
	},
	
	section : function(reference) {
		return this.sections.get(reference);
	},
	
	/**
	 * Adds a section to the form and propagates the event to render the section
	 * and open the editor.
	 * 
	 * @param config basic JS object version of the model
	 * @param calcId (optional, default true) calculate a new ID?
	 * @return section model
	 */
	addSection : function(config, calcId) {
		if (typeof calcId === "undefined" || calcId) {
			config.id = "S_" + (this.sections.length + 1) * -1;
		}
		var newSection = this.sections.create(config);
		return newSection;
	},
	
	/**
	 * Removes a section from the sections collection
	 * 
	 * @param reference id, cid, or model reference to the section
	 */
	removeSection : function(reference) {
		if (this.section(reference) !== null) {
			this.sections.remove(reference);
			reference.releaseRepeatableGroup();
			if(reference.get("existingSection")){
				this.deleteSections.create(reference);
			}
		}
	},
	
	
	/**
	 * Finds a section model by Div ID.
	 * 
	 * @param divId the div ID of the model to find
	 * @returns Model section model if found; otherwise null
	 */
	getSectionByDivId : function(divId) {
		return this.sections.findWhere({divId : divId});
	},
	
	/**
	 * Finds a section div on the page from its model
	 * 
	 * @param sectionModel the Section to find the div of
	 * @return jQuery div containing the section or empty jQuery if not found
	 */
	getSectionDiv : function(sectionModel) {
		return $("#" + sectionModel.get("divId"));
	},
	
	/**
	 * Finds a section model by section name.
	 * 
	 * @param name the name of the model to find
	 * @returns Model section model if found; otherwise null
	 */
	getSectionByName : function(name) {
		return this.sections.findWhere({name : name});
	},
	
	
	/**
	 * Gets the last section in the sections collection and 
	 * returns it.
	 */
	getLastSection : function() {
		return this.sections.at(this.sections.length - 1);
	},
	
	/**
	 * Gets the first question in the form.
	 * 
	 * @return Question model or null if doesn't exist
	 */
	getFirstQuestion : function() {
		if (this.sections.length > 0) {
			var firstSection = this.sections.at(0);
			if (firstSection.questions.length > 0) {
				return firstSection.questions.at(0);
			}
		}
		
		return null;
	},
	
	
	/**
	*
	* gets array of visible sections
	*
	*/

	getVisibleSections : function() {
		var visibleSections = [];
		this.sections.each(function(section) {
			if(!section.isRepeatableChild()) {
				visibleSections.push(section);
			}
		});
		return visibleSections;
	},
	
	/**
	 * Gets the sections array in the order they appear on the page.  Not
	 * guaranteed (because there's no check) but highly likely to get all 
	 * sections.
	 * 
	 * @return section model array
	 */
	getSectionsInPageOrder : function() {
		var output = [];
		var form = FormBuilder.form;
		$(".section").each(function() {
			var $sectionDiv = $(this);
			var sectionModel = form.getSectionByDivId($sectionDiv.attr("id"));
			output.push(sectionModel);
			
			if (sectionModel.isRepeatableParent()) {
				var repeatChildren = form.sections.getRepeatableChildren(sectionModel);
				Array.prototype.push.apply(output, repeatChildren);
			}
		});
		
		return output;
	},
	
	/**
	 * Finds a Question model by div ID.
	 * 
	 * @param divId the div ID of the model to find
	 * @return Model Question model if found, otherwise null
	 */
	getQuestionByDivId : function(divId) {
		var output = null;
		this.sections.forEach(function(section) {
			var question = section.getQuestionByDivId(divId);
			if (typeof question !== "undefined") {
				output = question;
				return;
			}
		});
		return output;
	},
	
	
	
	/**
	 * Finds a Question model by  ID.
	 * 
	 * @param Id the  ID of the model to find
	 * @return Model Question model if found, otherwise null
	 */
	getQuestionById : function(id) {
		var output = null;
		this.sections.forEach(function(section) {
			var question = section.getQuestionById(id);
			if (typeof question !== "undefined") {
				output = question;
				return;
			}
		});
		return output;
	},
	
	/**
	 * Finds the parent section of a given question
	 * 
	 * @param question Question Model to search for
	 * @returns Section model if found, otherwise null
	 */
	getQuestionParentSection : function(question) {
		var resultSection = null;
		this.sections.forEach(function(section) {
			if (section.question(question)) {
				resultSection = section;
			}
		});
		return resultSection;
	},
	
	/**
	 * Performs an underscore findWhere on every section to find all
	 * questions in all sections where the passed properties match.
	 * 
	 * @see underscorejs.org#findWhere
	 * @param properties the findWhere properties to search
	 * @returns array of question models
	 */
	findQuestionsWhere : function(properties) {
		var output = [];
		this.sections.forEach(function(sect) {
			var question = sect.questions.findWhere(properties);
			if (typeof question !== "undefined") {
				output.push(question);
			}
		}, FormBuilder.form);
		return output;
	},
	
	/**
	 * Performs an underscore where() on every section to find all
	 * sections where the passed properties match.
	 * 
	 * @see underscorejs.org#where
	 * @param properties the where properties to search
	 * @returns array of section models or undefined if not found
	 */
	findSectionWhere : function(properties) {
		return this.sections.where(properties);
	},
	
	/**
	 * Performs an underscore findWhere on every section to find all
	 * questions in all sections where the passed properties match
	 * and the filter function returns true
	 * 
	 * @see underscore.js.org#findWhere
	 * @param properties the findWhere properties to search
	 * @param filter a function which returns boolean true if the question is accepted
	 * @returns array of question models
	 */
	findFilteredQuestionsWhere : function(properties, filter) {
		var output = [];
		this.sections.forEach(function(sect) {
			var question = sect.questions.findWhere(properties);
			if (typeof question !== "undefined" && filter(question)) {
				output.push(question);
			}
		}, FormBuilder.form);
		return output;
	},
	
	countQuestions : function() {
		var count = 0;
		this.sections.forEach(function(section) {
			count += section.countQuestions();
		});
		return count;
	},
	
	getAllQuestionsInForm : function(){
		var questions = new Questions();
		this.sections.forEach(function(section){
			questions.add(section.questions.toArray());
		});
		return questions;
	},
	
	loadDataElements : function(dataElementsJSON) {
		if (this.dataElements.length > 0) {
			this.dataElements.reset();
		}
		
		for (var t=0; t<dataElementsJSON.length; t++) {
			var de = {
				dataElementName 	: 	dataElementsJSON[t].name.replace("\\", ""),
				dataElementType 	: 	dataElementsJSON[t].type.value,
				requiredType		:	dataElementsJSON[t].requiredType,
				description			:	dataElementsJSON[t].shortDescription,
				suggestedQuestion	:	dataElementsJSON[t].suggestedQuestion,
				restrictionId		:	dataElementsJSON[t].restrictions.id,
				restrictionName		:	dataElementsJSON[t].restrictions.value,
				valueRangeList		:	dataElementsJSON[t].valueRangeList,
				size				:	dataElementsJSON[t].size,
				max					:	dataElementsJSON[t].maximumValue,
				min					:	dataElementsJSON[t].minimumValue,
				associated			:	false,
				isGroupRepeatable	:	dataElementsJSON[t].groupRepeatable,
				order				:	dataElementsJSON[t].order,
				title				:	dataElementsJSON[t].title
			};
			this.dataElements.create(de);
		}
	},
	
	loadRepeatableGroups : function(repeatableGroupsJSON) {
		// first empty the collection, just in case
		this.repeatableGroups.reset();
		for (var t=0; t<repeatableGroupsJSON.length; t++) {
			var rg = {
				repeatableGroupName 		: repeatableGroupsJSON[t].name,
				repeatableGroupThreshold 	: repeatableGroupsJSON[t].threshold,
				repeatableGroupType			: repeatableGroupsJSON[t].type
			};
			// create is effectively collection.add(new collection.model(config))
			this.repeatableGroups.create(rg);
		}
	},
	
	loadAllGroups : function(allGroupsJSON) {
		// first empty the collection, just in case
		this.allGroups.reset();
		for (var t=0; t<allGroupsJSON.length; t++) {
			var rg = {
				repeatableGroupName 		: allGroupsJSON[t].name,
				repeatableGroupThreshold 	: allGroupsJSON[t].threshold,
				repeatableGroupType			: allGroupsJSON[t].type
			};
			// create is effectively collection.add(new collection.model(config))
			this.allGroups.create(rg);
		}
	},
	
	/**
	 * Finds all member sections of the table group and returns them as an array.
	 * 
	 * @param groupId the group ID to find the members of
	 * @return array of Section models or undefined if not found
	 */
	getTableGroupSectionsById : function(groupId) {
		return this.findSectionWhere({tableGroupId : groupId});
	},
	
	/**
	 * Gets the largest table group ID in the form.  This runs in O(n) time
	 * right now.
	 * 
	 * @return integer largest table group ID on the page
	 */
	getLargestTableGroupId : function() {
		var largestTableGroupId = 0;
		this.sections.forEach(function(section){
			var groupId = section.get("tableGroupId");
			if (groupId > largestTableGroupId) {
				largestTableGroupId = groupId;
			}
		});
		return largestTableGroupId;
	},
	
	save : function() {
		//console.log("save form");
	},
	
	/**
	 * Gets the (one) column header section for the given table group ID.
	 * 
	 * @param groupId the group ID to search over
	 * @return Section model matching the group ID and column header type or undefined if not found
	 */
	getColHeaderForTableGroup : function(groupId) {
		return this.sections.findWhere({
			tableGroupId : groupId,
			tableHeaderType : Config.tableHeaderTypes.columnHeader
		});
	},
	
	/**
	 * Gets the (one) table header section for the given table group ID.
	 * 
	 * @param groupId the group ID to search over
	 * @return Section model matching the group ID and table header type or undefined if not found
	 */
	getTableHeaderForTableGroup : function(groupId) {
		return this.sections.findWhere({
			tableGroupId : groupId,
			tableHeaderType : Config.tableHeaderTypes.tableHeader
		});
	},
	
	/**
	 * Gets the (one) primary section for the given table group ID.
	 * 
	 * @param groupId the group ID to search over
	 * @return Section model matching the group ID with no headers or undefined if not found
	 */
	getPrimarySectionForTableGroup : function(groupId) {
		var primarySections = this.findSectionWhere({
			tableGroupId : groupId,
			tableHeaderType : Config.tableHeaderTypes.none
		});
		
		// primary sections can be repeatable, so there can be multiple.  Check that and, if needed, find the right one
		// it is also probably the first one of this set
		for (var i = 0; i < primarySections.length; i++) {
			if (!primarySections[i].get("isRepeatable") || primarySections[i].isRepeatableParent()) {
				return primarySections[i];
			}
		}
		// couldn't find it, return null
		return null;
	},
	
	//added by Ching Heng
	serializeModel : function(){
		var params = this.toJSON();
		var KEYs = Object.keys(params);
		var serialized='';
		for(key in KEYs){
			serialized += this.mappingRules(params,KEYs,key);
		}
		return serialized.substring(0,serialized.length-1);
	},
	
	mappingRules : function(params,KEYs,key){
		switch(KEYs[key]){
		case 'formtypeid':
			return 'formForm.'+KEYs[key]+'='+params[KEYs[key]]+'&';
			break;
		case 'formid':
			return 'formForm.'+KEYs[key]+'='+params[KEYs[key]]+'&';
			break;
		case 'id':
			return 'formForm.'+KEYs[key]+'='+params['formid']+'&';
			break;
		case 'dataStructureName':
			return 'formForm.'+KEYs[key]+'='+params[KEYs[key]]+'&';
			break;
		case 'dataStructureVersion':
			return 'formForm.'+KEYs[key]+'='+params[KEYs[key]]+'&';
			break;
		case 'dataStructureRadio':
			return KEYs[key]+'='+params[KEYs[key]]+'&';
			break;
		case 'tabdisplay':
			return 'formForm.'+KEYs[key]+'='+params[KEYs[key]]+'&';
			break;
		case 'name':
			return 'formForm.'+KEYs[key]+'='+params[KEYs[key]]+'&';
			break;
		case 'access':
			return 'formForm.accessFlag='+params[KEYs[key]]+'&';
			break;
		case 'description':
			return 'formForm.'+KEYs[key]+'='+params[KEYs[key]]+'&';
			break;
		case 'dataEntryFlag':
			return 'formForm.'+KEYs[key]+'='+params[KEYs[key]]+'&';
			break;
		case 'copyrightedForm':
			return 'formForm.copyRight='+params[KEYs[key]]+'&';
			break;
		case 'allowMultipleCollectionInstances':
			return 'formForm.'+KEYs[key]+'='+params[KEYs[key]]+'&';
			break;
		case 'status':
			return 'formForm.'+KEYs[key]+'='+params[KEYs[key]]+'&';
			break;
		case 'statusHidden':
			return 'formForm.'+KEYs[key]+'='+params[KEYs[key]]+'&';
			break;
		case 'nonpatientformtypeid':
			return 'formForm.'+KEYs[key]+'='+params[KEYs[key]]+'&';
			break;
		case 'formHeader':
			return 'formForm.'+KEYs[key]+'='+params[KEYs[key]]+'&';
			break;
		case 'formFooter':
			return 'formForm.'+KEYs[key]+'='+params[KEYs[key]]+'&';
			break;
		case 'formborder':
			return 'formForm.'+KEYs[key]+'='+params[KEYs[key]]+'&';
			break;
		case 'formfont':
			return 'formForm.'+KEYs[key]+'='+params[KEYs[key]]+'&';
			break;
		case 'fontSize':
			return 'formForm.'+KEYs[key]+'='+params[KEYs[key]]+'&';
			break;
		case 'formcolor':
			return 'formForm.'+KEYs[key]+'='+params[KEYs[key]]+'&';
			break;
		case 'cellpadding':
			return 'formForm.'+KEYs[key]+'='+params[KEYs[key]]+'&';
			break;
		case 'sectionborder':
			return 'formForm.'+KEYs[key]+'='+params[KEYs[key]]+'&';
			break;
		case 'sectionfont':
			return 'formForm.'+KEYs[key]+'='+params[KEYs[key]]+'&';
			break;
		case 'sectioncolor':
			return 'formForm.'+KEYs[key]+'='+params[KEYs[key]]+'&';
			break;
		default:
			return '';
		}
	},
	
	repopulate : function(formForm){
		this.set({
			name:formForm.name,
			status:formForm.status,
			description:formForm.description,
			formborder:formForm.formborder,
			sectionborder:formForm.sectionborder,
			formfont:formForm.formfont,
			formcolor:formForm.formcolor,
			sectionfont:formForm.sectionfont,
			sectioncolor:formForm.sectioncolor,
			dataEntryFlag:formForm.dataEntryFlag,
			access:formForm.accessFlag,
			formHeader:formForm.formHeader,
			formFooter:formForm.formFooter,
			fontSize:formForm.fontSize,
			cellpadding:formForm.cellpadding,
			tabdisplay:formForm.tabdisplay,
			formtypeid:formForm.formtypeid,
			formid:formForm.formid,
			id:formForm.formid,
			nonpatientformtypeid:formForm.nonpatientformtypeid,
			dataStructureName:formForm.dataStructureName,
			dataStructureVersion:formForm.dataStructureVersion,
			dataStructureRadio: formForm.dataStructureName,
			copyrightedForm:formForm.copyRight,
			allowMultipleCollectionInstances:formForm.allowMultipleCollectionInstances,
			statusHidden:formForm.statusHidden
		});

	},
	
	setCalDependent : function() {
		var matchRegex = new RegExp(/\[S_[-]?\d+_Q_\d+\]/g); //the question looks like [S_123_Q_123]
		var splitRegex = new RegExp(/[\[S_[-]?\d+_Q_d+\]]/);
		var dependentQIdArray= new Array();
		
		var questions = this.getAllQuestionsInForm();
		questions.forEach(function(question) {
			question.set("calDependent",false); // make all the question calculation dependent flag tp be default
			var calStr = question.get("calculation");
			var matchs = calStr.match(matchRegex);
			if (matchs!=null){
				for (var j=0;j<matchs.length;j++) {
					var dependentQId=matchs[j].split(splitRegex); 
					for (var h=0;h<dependentQId.length;h++) {
						if (dependentQId[h]!=''){ // dependentQId[h]=[S_123_Q_123]
							var temp=dependentQId[h].split("\_");
							var sId=temp[1];
							var qId=temp[3].substring(0,temp[3].indexOf("\]"));
							var q = FormBuilder.form.getQuestionById(qId);
							var qV = question.get("questionVersionNumber");// we need the question version!!
							var divId="S_"+sId+"_"+qId+"_"+qV;
							dependentQIdArray.push(divId);
						}
					}
				}	
			}	
		});
		
		for (var u=0;u<dependentQIdArray.length;u++) { // recheck all question's calculate dependence
			questions.forEach(function(question) {
				question.calculateDivId(true);
				if (dependentQIdArray[u]==question.get("newQuestionDivId")) {
					question.set("calDependent",true);
				}
			});	
		}
		
		
	},
	
	
	
	setSkipRuleDependent : function(){
		
		var dependentArray= new Array();
		if((this.sections !== null) || (typeof this.sections !== "undefined")){
			this.sections.forEach(function(section){
				section.questions.forEach(function(question){
				question.set("skipRuleDependent",false);
				var questionSkipArray = question.get("questionsToSkip");
				if(typeof questionSkipArray !== "undefined" && questionSkipArray.length>0){
					for(var i=0;i<questionSkipArray.length;i++){
						dependentArray.push(questionSkipArray[i]);
					}
				}
				});
			});
			
			this.sections.forEach(function(section){
				section.questions.forEach(function(question){
					var secId = question.get("sectionId");
					var checkId = secId+"_Q_"+question.get("questionId");
					for(var j = 0; j < dependentArray.length; j++){
						if(question.get("skipRuleDependent")== false && checkId==dependentArray[j])
							question.set("skipRuleDependent",true);
					}
				});
			});
		}
	}
});