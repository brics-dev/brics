/**
 * A general class for managing the question editor.  This class must be
 * extended for each question type.  It provides the functionality that is
 * common across all question editors.
 */
var QuestionEditView = EditorView.extend({
	dialogTitle : "Edit Question",
	initialize : function() {
	 	$.ibisMessaging("close", {type:"primary"}); 
		QuestionEditView.__super__.initialize.call(this);
	},
	
	render : function(model) {
		this.stopListening();
		
		var qName = model.get("questionName");
		var undeScoreIndex = qName.indexOf("_");
		qName = qName.substring(undeScoreIndex+1,qName.length)
		
		this.dialogTitle = "Edit Question " + qName;
		QuestionEditView.__super__.render.call(this, model);
		this.listenTo(model, "change:questionName", this.uppercaseName);
		
		this.renderCommons(model);
		this.setModel(model);
		this.renderDataElement();
		if(model.get("questionId") > 0){
			this.$('#questionName').prop('disabled', true);
		}
		
        //set visual scale min and max		
		var type = model.get("questionType");
		var questionTypes = Config.questionTypes; 

        if ( (type === questionTypes.textbox) || (type === questionTypes.textarea) ) {
  	      if (typeof(model.get("vscaleRangeStart"))!='undefined' && typeof(model.get("vscaleRangeEnd"))!='undefined') { 
      		  	model.set("vscaleRangeStart", "-99999");
                model.set("vscaleRangeEnd", "-99998");
			    model.set("vscaleWidth", "100");
		        model.set("vscaleShowHandle", true);
			    model.set("vscaleCenterText", "");
		        model.set("vscaleLeftText", "");
			    model.set("vscaleRightText", "");
  	      }
  	    }
        
        this.showFormatDiv();
	},
	
	uppercaseName : function(){
		var upperCase = this.model.get("questionName").toUpperCase();
		this.$("#questionName").val(upperCase);
	},
	
	renderDataElement : function() {

		if (this.model.get("dataElementName") == "none") {

			this.$('label[name="dataElementName"]').html('<span class="' + Config.styles.errorField + '">'+Config.language.noDataElementDefined+Config.language.clickHereFix+'</span>');
			this.$("#removeDEAnchor").hide();
		}else {
			this.$("#removeDEAnchor").show();
		}
	},
	
	initCommons : function() {
		this.commons = {
				registered: []
		};
	},
	
	events : {
		"click #question-changeDataElement":"questionChangeDataElementOnClick",
		"click #removeDEAnchor":"removeDataElement",
		"change .questionType" : "onChangeQuestionType",
		"click #questionText" : "formatQuestionText",
		"click .defaultText" : "defaultQuestionText",
		"click .defaultDescriptionUp" : "defaultDescriptionUp",
		"click .defaultDescriptionDown" : "defaultDescriptionDown",
	},
	
	removeDataElement : function () {
		this.model.set("dataElementName","none");
		this.$('label[name="dataElementName"]').html('<span class="' + Config.styles.errorField + '">'+Config.language.noDataElementDefined+Config.language.clickHereFix+'</span>');
		FormBuilder.page.get("activeEditorView").$("#removeDEAnchor").hide();
	},
	
	/*questionChangeDataElementOnClick : function() {
		var view = FormBuilder.page.get("dataElementsView");
		
		if ( (view !== null) && (typeof view != "undefined") ) {
			// Show the processing dialog box.
	   		EventBus.trigger("open:processing", Config.language.loadDataElem);
	   		
	   		view.render(this.model);
		}
		else {
			Log.user.error(Config.language.openDeTableError);
		}
	},*/
	
	registerCommons : function(target, commonsClass) {
		// check to make sure we have not merged this commons already
		var config = commonsClass.config;
		if (!_.contains(target.commons.registered, config.name)) {
			target.commons.registered.push(config.name);
			
			var finalValidationRules = _.clone(target.validationRules);
			if (typeof commonsClass.validationRules !== "undefined") {
				finalValidationRules = $.merge(_.clone(target.validationRules), _.clone(commonsClass.validationRules));
			}
			$.extend(true, target, commonsClass);
			target.validationRules = finalValidationRules;
			
			for (var fnName in config) {
				if (fnName !== "name") {
					if (typeof target.commons[fnName] === "undefined") {
						target.commons[fnName] = [config[fnName]];
					}
					else {
						target.commons[fnName].push(config[fnName]);
					}
				}
			}
		}
		target.delegateEvents();
	},
	
	/**
	 * Calls each of the commons callbacks for the "render" function.
	 * This happens at the end of the EditView render
	 * @param model the model passed to this.render
	 */
	renderCommons : function(model) {
		var common = this.commons;
		if (typeof common.render !== "undefined") {
			for (var i = 0; i < common.render.length; i++) {
				var eachCommon = common.render[i];
				// just a double-check to make sure the method exists
				if (typeof eachCommon !== "undefined") {
					this[eachCommon].call(this, model);
				}
			}
		}
		this.setModel(model);
	},
	
	cancel : function() {
		QuestionEditView.__super__.cancel.call(this);
		
		var isNew = this.model.get("isNew");
		if (isNew) {
			EventBus.trigger("delete:question",this.model);
		}
	},
	
	close : function() {
		// since we're closing the editor, copy changes to wherever needed
		// NOTE: validation has not passed at this point (passed in EditorView)
		// but that's okay since it would have to change again
		EventBus.trigger("change:question", this.model);
		return QuestionEditView.__super__.close.call(this);
	},	
	
	/**
	 * Performs any GENERAL question save processing.  Anything that can be
	 * abstracted away from the individual type views
	 */
	/*save : function(){
		var saveSuccess = QuestionEditView.__super__.save.call(this);
		if(saveSuccess){
			EventBus.trigger("save:question",this.model);
		}
	},*/
	save : function() {
		var saveSuccess = QuestionEditView.__super__.validate.call(this);
		if (saveSuccess) {
			//added by Ching Heng
			var qId = this.model.get('questionId');
			if( qId < 0 ){ // it is a new question
				//We need to create question in database
				//than get the question id for graphic attachment
				this.createQuestion();
				//
			}else{ // it is a edit question
				//this.editQuestion();
				var graphicFr = this.$('#graphicFr');
				var frExists = graphicFr.length;
				if (frExists > 0) {
					graphicFr.contents().find('#forGarphicQuestionId').val(qId);
//					if( graphicFr.contents().find('#questionImageListContainer_table').length > 0){
//						this.deleteGraphic(qId);
//					} 
					// calls finishQuestionAddEdit
					graphicFr.contents().find('form').submit();
				}
				QuestionDragHandler.resizeAllSections();
				EventBus.trigger("save:question", this.model);
				
				if (frExists == 0) {
					EventBus.trigger("close:activeEditor");
				}
			}
		}
	},
	
	onChangeQuestionType : function() {
		var msg;
		var qModel = this.model;
		var previous = qModel.previous("questionType");
		if(FormBuilder.form.get("isCAT") && FormBuilder.form.get("measurementType") != 'shortForm'){
			msg = "<div class='ibisMessaging-message ibisMessaging-primary ibisMessaging-error'>This instrument is a computer adaptive test (CAT), so its questions are generated dynamically based on answers given. The question type can not be changed in this form.</div>";
			$.ibisMessaging("dialog", "info", msg, {
				width: 500,
				dialogClass : "formBuilder_editor",
				modal : true,
				buttons: [
				{
					text: "Ok",
					click : function() {
						FormBuilder.page.get("activeEditorView").model.set("questionType", previous);
						$(this).dialog("close");
				    }
				}]				               
			});
		}else{
			msg = "Are you sure you want to change the question type?";
			$.ibisMessaging("dialog", "info", msg, {
				width: 500,
				dialogClass : "formBuilder_editor",
				modal : true,
				buttons: [
				{
					text: "Ok",
					click : function() {
						EventBus.trigger("change:questionType", FormBuilder.page.get("activeEditorView").model);
						$(this).dialog("close");
				    }
				},
				{
					text: "Cancel",
					click : function() {
						FormBuilder.page.get("activeEditorView").model.set("questionType", previous);
						//EventBus.trigger("change:questionType", FormBuilder.page.get("activeEditorView").model);
						$(this).dialog("close");
					}
				}]				               
			});
		}
    },
	
	/*added by Ching Heng*/
	createQuestion : function(){
		var qModel = this.model;
		this.hideEditorWarning();
		var isNew = true;
		var response = qModel.saveToDatabase(isNew);
		//alert("doAddEditQuestionAjaxPost(): the post Parameters:\n" +params + "\n\nResponse:\n" + response);
		switch(response){
		case 'ERROR_DUPLICATE_QUESTION':
			this.showEditorWarning(Config.language.validateDuplicateQuestion);
			this.switchFailedFieldTab("questionName",qModel);
			break;
		case 'ImageMap_NOTDONE':
			this.showEditorWarning(Config.language.validateImageHasBeenDone);
			this.switchFailedFieldTab("questionName",qModel);
			break;
		case 'ERROR_DefaultValue_NEED':
			this.showEditorWarning(Config.language.defaultValueFailed);
			this.switchFailedFieldTab("defaultValue",qModel);
			break;
		case 'ERROR_ajaxError':
			break;
		default:
			if(FormBuilder.form.section(qModel.get("sectionId")).isRepeatableParent()){
				//create the repeatable child questions
				EventBus.trigger("create:repeatableQuestions", qModel);
			}
			this.launchSaving(qModel.get("questionId"));
		}
	},
	
	launchSaving : function(qId){
		// check that graphicFr exists.  If it doesn't, this question doesn't
		// have a graphic so no need to submit that form.
		var graphicFr = this.$('#graphicFr');
		if (graphicFr.length > 0) {
			graphicFr.contents().find('#forGarphicQuestionId').val(qId);
			// calls finishQuestionAddEdit
			graphicFr.contents().find('form').submit();
		}
		else {
			// if no graphic, just continue in the JS
			this.saveAndClose();
		}
	},
	
	saveAndClose : function() {
		EventBus.trigger("save:question", this.model);
		EventBus.trigger("close:activeEditor");
	},
	
	isQuestionCoreChanged : function() {
		var isModelChanged = StateManager.watchedModelChanged;
		
		if(isModelChanged) {
			var changeList = StateManager.changeSet;
			if(typeof changeList["questionText"] != "undefined" ) {
				return true;
			}
			if(typeof changeList["descriptionUp"] != "undefined" ) {
				return true;
			}
			if(typeof changeList["descriptionDown"] != "undefined" ) {
				return true;
			}
			if(typeof changeList["questionType"] != "undefined" ) {
				return true;
			}
			if(typeof changeList["questionOptionsObjectArray"] != "undefined" ) {
				return true;
			}
		}
		
		return false;
		
		
	},
	
	editQuestion : function(){
		var thisView = this;
		var qmodel = thisView.model;
		var params = qmodel.serializeModel();
		var editQid = qmodel.get('questionId');
		var editQver = 1;
		var questionInfoURL = '';
		var isQuestionChanged = this.isQuestionCoreChanged();
		if(qmodel.get('hasSavedInDatabase')){//coming from question library
			if(isQuestionChanged){
				if(qmodel.get('attachedFormIds').length > 0){ // has been attached at other form
					thisView.confirmChange(questionInfoURL,params);
				}else{
					questionInfoURL= baseUrl+'/form/addEditQuestion.action?'+
									 'editMode=global&import=1&action=editQuestionAjax&qId='+editQid+
									 '&qVersion='+editQver;
					thisView.doEditAjax(questionInfoURL,params,false);
				}
			}else{ // no changing but still go edit action
				questionInfoURL = baseUrl+'/form/addEditQuestion.action?'+
				                  'editMode=global&import=1&action=editQuestionAjax&qId='+editQid+
				                  '&qVersion='+editQver;
				thisView.doEditAjax(questionInfoURL,params,false);
			}
		}else{ // edit a new question witch has been created
			questionInfoURL = baseUrl+'/form/addEditQuestion.action?'+
							  'editMode=global&import=0&action=editQuestionAjax&qId='+editQid+
							  '&qVersion='+editQver;
			thisView.doEditAjax(questionInfoURL,params,false);
		}
		
	},
	
	doEditAjax : function(questionInfoURL,params,isLocalChange){
		var thisView = this;
		$.ajax({
			type:"post",
			url:questionInfoURL,
			data:params,
			beforeSend: function(){
				thisView.hideEditorWarning();
			},
			success: function(response){
				//alert("doAddEditQuestionAjaxPost(): the post Parameters:\n" +params + "\n\nResponse:\n" + response);
				switch(response){
				case 'ImageMap_NOTDONE':
					thisView.showEditorWarning(Config.language.validateImageHasBeenDone);
					thisView.switchFailedFieldTab("questionName",qModel);
					break;
				case 'scoreMissing':
					thisView.showEditorWarning(Config.language.validateAllOrNoneScore);
					thisView.switchFailedFieldTab("questionOptionsObjectArray", qModel);
					break;
				case 'ERROR_DefaultValue_NEED':
					thisView.showEditorWarning(Config.language.defaultValueFailed);
					thisView.switchFailedFieldTab("defaultValue",qModel);
					break;
				default:
					var qJSON = JSON.parse(response);
					var oldQuestionId = thisView.model.get("questionId");
					
					thisView.model.set("questionId",qJSON.questionId);
					thisView.model.set("questionName",qJSON.questionName);
					thisView.launchSaving(qJSON.questionId);
					
					//if doing local change...we need to now chnange calc rule if the question was used in the calc rule and chnage the questions to skip if it was a skip question
					if(isLocalChange) {
						
						var newQuestionId = qJSON.questionId;
						var newQuestionName = qJSON.questionName;
						thisView.updateSkipRuleUponLocalChange(oldQuestionId,newQuestionId);
						
						thisView.updateCalcRuleUponLocalChange(oldQuestionId,newQuestionId);
						
						
						thisView.updateQuestionIdAndQuestionNameAndCoreAttributesForSameQuestionsInFormDuringLocalChange(thisView.model,oldQuestionId,newQuestionId,newQuestionName);
						


					}
					
					
					
				}				
			},
			error: function(e){
				alert("error" + e );
			}
		});
		//this.launchSaving(this.model.get('questionId')); // once up uncommend, this need to be deleted
	},
	
	
	updateSkipRuleUponLocalChange: function(oldQuestionId,newQuestionId) {
		var sections = FormBuilder.form.sections;
		sections.forEach(function(section) {
				section.questions.forEach(function(question) {
					if (question.get("skiprule")) {
							var questionsToSkip = question.get("questionsToSkip");
							for(var i=0;i<questionsToSkip.length;i++) {
								var oldQuestionIdString = "Q_" + oldQuestionId;
								var newQuestionIdString = "Q_" + newQuestionId;
								questionsToSkip[i] = questionsToSkip[i].replace(oldQuestionIdString, newQuestionIdString);
							}
							question.set("questionsToSkip",questionsToSkip);
					}	
				});

		}); // end for
	},
	
	
	updateCalcRuleUponLocalChange: function(oldQuestionId,newQuestionId) {
		

		var sections = FormBuilder.form.sections;
		sections.forEach(function(section) {
				section.questions.forEach(function(question) {
					if (question.get("calculatedQuestion")) {
							var calculation = question.get("calculation");
							var oldQuestionIdString = "Q_" + oldQuestionId;
							var newQuestionIdString = "Q_" + newQuestionId;
							calculation = calculation.replace(oldQuestionIdString, newQuestionIdString);
							question.set("calculation",calculation);
					}	
				});

		}); // end for
		
		
		
	},
	
	
	//this might be the longest method name ever! but it describes exactly what its doing!
	updateQuestionIdAndQuestionNameAndCoreAttributesForSameQuestionsInFormDuringLocalChange: function(qModel, oldQuestionId, newQuestionId, newQuestionName) {

		var sections = FormBuilder.form.sections;
		sections.forEach(function(section) {
				section.questions.forEach(function(question) {
					if (question.get("questionId") == oldQuestionId) {

							question.set("questionId",qModel.get("questionId"));
							question.set("questionName",qModel.get("questionName"));
							question.set("questionText",qModel.get("questionText"));
							question.set("descriptionUp",qModel.get("descriptionUp"));
							question.set("descriptionDown",qModel.get("descriptionDown"));
							question.set("questionType",qModel.get("questionType"));
							question.set("questionOptionsObjectArray",qModel.get("questionOptionsObjectArray"));
							question.set("defaultValue",qModel.get("defaultValue"));
							
					}	
				});

		}); // end for
		
		
		
	},
	
	
	confirmChange : function(questionInfoURL,params){//callback){
		var qModel = this.model;
		var editQid = qModel.get('questionId');
		var editQver = 1;
		var msg = '';
		var butStatus = '';
		var thisView = this;
		if(Config.isAdmin){
			msg = "<div style='text-align:left;margin-top:14px;'>"+
					"<h2>This question is being used by another form</h2> <p align='justify'>Would you like to make a global change (will affect other forms) or would you like to make a local change(create it as a new question).</p></p><font color='red'>Warning:</font> A global change may result in data loss in any current data collections that use this question.</p>"+
			      "</div>";
		}else{
			msg = "<div style='text-align:left;margin-top:14px;'>"+
					"<h2>This question is being used by another form</h2> <p align='justify'>Would you like to make a local change(create it as a new question).</p>"+
				  "</div>";
			butStatus = 'display: none;';
		}
	    $.ibisMessaging("dialog", "info", msg, {
	    	width: 500,
	    	dialogClass : "formBuilder_dialog_noclose",
	    	buttons: [
	        
	    	
	    	
	    	
	    	
	    	{
	    		text: "Local Change",
	    		click: function() {
					ret = true; 
	                
					if(qModel.get('hasSavedInDatabase')){
						questionInfoURL= baseUrl+"/form/addEditQuestion.action?editMode=local&import=1&action=editQuestionAjax&qId="+editQid+
										 "&qVersion="+editQver;
			    	}else{ // edit
	                	questionInfoURL= baseUrl+"/form/addEditQuestion.action?editMode=local&import=0&action=editQuestionAjax&qId="+editQid+
	                					 "&qVersion="+editQver;
			    	}
					
					$(this).dialog("close");
					thisView.doEditAjax(questionInfoURL,params,true);
	    		}
	    	},
	    	
	    	
	    	
	    	{
	    		text: "Global Change",
	    		click : function() {
					ret = false;
	                
	            	if(qModel.get('hasSavedInDatabase')){
	            		questionInfoURL= baseUrl+"/form/addEditQuestion.action?editMode=global&import=1&action=editQuestionAjax&qId="+editQid+
	            						 "&qVersion="+editQver;
	                }else{ // edit
	                	questionInfoURL= baseUrl+"/form/addEditQuestion.action?editMode=global&import=0&action=editQuestionAjax&qId="+editQid+
	                					 "&qVersion="+editQver;
			    	}
	            	
	            	$(this).dialog("close");
	            	thisView.doEditAjax(questionInfoURL,params,false);
	    		},
	    		style : butStatus
	    	},
	    	
	    	
	    	
	    	{
	    		text: "Cancel",
	    		click: function() {
	    			$(this).dialog("close");
	    			//thisView.doEditAjax(questionInfoURL,params,false);
	    		}
	    	}
	    	
	    	
	    	
	    ]});
	},
	
	finishQuestionAddEdit : function() {
		// attach question to the form
		var graphicJSON= JSON.parse(this.$('#graphicFr').contents().find('input#graphicJSON').val()) ;
		var graphicNames=graphicJSON.graphicNames;
		
		var permissibleTypes = Config.fileUpload.permissibleTypes;
		var permissibleSize = Config.fileUpload.permissibleSize;
		var unsupportedFileExtension = Config.fileUpload.unsupportedFileExtension;
		var unsupportedFileSize = Config.fileUpload.unsupportedFileSize;
		
		if(graphicNames== unsupportedFileExtension+unsupportedFileSize){
			$("#messageContainer").empty();
			$.ibisMessaging("primary", "error", "Please upload file of type "+permissibleTypes+" and size less than "+permissibleSize+" MB",{container: "#questionGraphicsMessageContainer"});
		}else if (graphicNames!=unsupportedFileExtension && (graphicNames==unsupportedFileSize)){
			$("#messageContainer").empty();
			$.ibisMessaging("primary", "error", "Please upload file with size less than "+permissibleSize+" MB",{container: "#questionGraphicsMessageContainer"});
		}else if (graphicNames==unsupportedFileExtension && (graphicNames!=unsupportedFileSize)){
			$("#messageContainer").empty();
			$.ibisMessaging("primary", "error", "Please upload file of type "+permissibleTypes+".",{container: "#questionGraphicsMessageContainer"});
		}
		var userFileId=graphicJSON.userFileId;
		this.model.set("graphicNames",graphicNames);
		FormBuilder.form.setSkipRuleDependent();
		FormBuilder.form.setCalDependent();
		// close the dialog
		this.saveAndClose();
	},
	
	deleteGraphic : function(questionId) {
		var graphicFr = this.$('#graphicFr');
		var deletGraphics = graphicFr.contents().find('input[name="namesToDelete"]');
		var namesToDelete = new Array();
		for (var i=0;i<deletGraphics.length;i++) {
			if (deletGraphics[i].checked) {			
				if(i == deletGraphics.length - 1){
					namesToDelete += deletGraphics[i].value; 
				} else {
					namesToDelete += deletGraphics[i].value + ",";
				}
			}
		}		
		//console.log("deleteGraphic() questionId: "+questionId +" | namesToDelete: "+namesToDelete);
		if(namesToDelete.length > 0){
			var url = Config.baseUrl +"deleteQuestionImage!deleteQuestionImage.action";
			//console.log("deleteGraphic() url: "+url);			
			// Ajax call your search
			$.ajax({
				type: 	"POST",
				url:    url,
				data: 	{	"qId": questionId,
							"namesToDelete": namesToDelete,
						},
				async:  false,
				success: function(data) {}
			});
		}

	},
	
	formatQuestionText : function() {
		var thisView = this;
		var qmodel = thisView.model;
		
		var questionTextEditView = FormBuilder.pageView.model.get("questionTextEditView");
		questionTextEditView.render(qmodel,"questionText");
	},
	
	defaultQuestionText : function() {
		
		if (this.isHTML(this.model.get("questionText"))){
			this.model.set("questionText",$(this.model.get("questionText")).text());
			$(".basicFormatText").hide();
			$(".selectQuestionformat").hide();
		}
		
	},
	
	isHTML : function (str) {
		  var a = document.createElement('div');
		  a.innerHTML = str;

		  for (var c = a.childNodes, i = c.length; i--; ) {
		    if (c[i].nodeType == 1) return true; 
		  }

		  return false;
	},
	
	showFormatDiv : function() {
		
		var type = this.model.get("questionType");
		var questionTypes = Config.questionTypes;
		
		if( type == questionTypes.textbox 
				||type == questionTypes.textarea || type == questionTypes.imageMap){
			
			
			if (this.isHTML(this.model.get("questionText"))){
				$(".basicFormatText").show();
				$(".selectQuestionformat").hide();
				$(".selectDataElement").hide();
			} 
			
			if(this.isHTML(this.model.get("descriptionUp"))){
				$(".descriptionUp").show();
			}
			
			if(this.isHTML(this.model.get("descriptionDown"))){
				$(".descriptionDown").show();
			}
			$(".basicDataElement").show();
		}
		else if(type == questionTypes.radio || type == questionTypes.checkbox 
				||type == questionTypes.select || type == questionTypes.multiSelect || type == questionTypes.visualscale){
			
			if (this.isHTML(this.model.get("questionText"))){
				$(".selectQuestionformat").show();
	        	$(".basicFormatText").hide();
	        	$(".basicDataElement").hide();
			}
			$(".selectDataElement").show();
		}
	},
	
	defaultDescriptionUp : function() {
		
		if (this.isHTML(this.model.get("descriptionUp"))){
			this.model.set("descriptionUp",$(this.model.get("descriptionUp")).text());
			$(".descriptionUp").hide();
			$(".descriptionUpPlaceHolder").show();
		}
	},
	
	defaultDescriptionDown : function() {
		
		if (this.isHTML(this.model.get("descriptionDown"))){
			this.model.set("descriptionDown",$(this.model.get("descriptionDown")).text());
			$(".descriptionDown").hide();
		}
		
	}
	
});