var ActionBarView = BaseView.extend({
	events : {
		"click #saveFormButton" : "saveForm",
		"click #editFormDetailsButton" : "editFormDetails",
		"click #addSectionButton" : "addSection",
		"click #addTextButton" : "addText",
		"click #cancelFormButton" : "cancelForm",
		"click #addQuestionFromLibrary" : "addQuestionFromLibrary",
		"click #addTextboxQuestion" : "addTextboxQuestion",
		"click #addTextareaQuestion" : "addTextareaQuestion",
		"click #addRadioQuestion" : "addRadioQuestion",
		"click #addCheckboxQuestion" : "addCheckboxQuestion",
		"click #addSelectQuestion" : "addSelectQuestion",
		"click #addMultiSelectQuestion" : "addMultiSelectQuestion",
		"click #addImageMapQuestion" : "addImageMapQuestion",
		"click #addFileUploadQuestion" : "addFileUploadQuestion",
		"click #addVisualScaleQuestion" : "addVisualScaleQuestion",
		"click #editLayoutButton" : "openLayoutEditor",
		"click #editTableButton" : "openTableEditor",
		"click #addQuestionButton" : "addDataElementQuestion",
		"click #removeTableButton" : "resetTableSection"
	},
	
	initialize : function() {
		ActionBarView.__super__.initialize.call(this);
		EventBus.on("change:activeSection", this.enableDisableAddQuestionButton, this);
		EventBus.on("change:activeSection", this.enableDisableResetTable, this);
		EventBus.on("change:section", this.enableDisableResetTable, this);
		this.template = TemplateManager.getTemplate("actionBarTemplate");
	},
	
	render : function() {
		this.$el.html(this.template(this.model.attributes));
		ActionBarView.__super__.render.call(this);
		this.formStructureRender();
		
		// sticky this element to the top of the page if the page is scrolled past it
		Config.actionBarTop = this.$el.offset().top;
		
		// formstatus needs to be translated
		this.$('[name="status"]').html(Config.formStatusNames[this.model.get("status")]);
		
		var stickyUnsticky = _.throttle(function() {
			var $actionbar = FormBuilder.page.get("actionBarView").$el;
			if ($(this).scrollTop() > Config.actionBarTop) {
				if (!$actionbar.hasClass("actionbar-sticky")) {
					$actionbar.addClass("actionbar-sticky");
					$actionbar.removeClass("actionbar-nosticky");
				}
			}
			else {
				if ($actionbar.hasClass("actionbar-sticky")) {
					$actionbar.removeClass("actionbar-sticky");
					$actionbar.addClass("actionbar-nosticky");
				}
			}
		}, 66, {leading: false});
		
		$(window).scroll(function() {
			stickyUnsticky();
		});
	},
	
	formStructureRender : function() {
		if (this.model.get("dataStructureName") == "") {
			this.$('span[name="dataStructureName"]').text(Config.language.noFormStructureDefined);
		}
	},
	
	saveForm : _.debounce(function() {
		$("#saveFormButton").addClass("disabled");
		
		// open processing to disable the page
		EventBus.trigger("open:processing", "Saving your form");
		
		RepeatableSectionProcessor.updateCalcRule();
		RepeatableSectionProcessor.updateSkipRule();
		
		/*added by Ching Heng*/
		FormBuilder.pageView.setRowsCols();
		var secArray = new Array();
		var questionArray = new Array();
		var deleteSectionArray = new Array();
		var formModel = this.model;
		formModel.sections.forEach(function(section) {
			secArray.push(section.toJsonObj());
			section.questions.forEach(function(question) {
				question.setOptionsOrderVaL();
				var questionVal = question.toQuestionObj();
				this.emailTriggerAnswers(questionVal);
				questionArray.push(questionVal);
			},this);
		},this);
		
		//ShortName validation for final check for uniqueness in case of concurrent access
		var short = formModel.get('shortName');
		
		$.ajax({
			  type: "POST",
			  url: baseUrl+"validateEformAction!validateEform.action",
			  data: {shortName:short},
			  dataType :"json",
			  async:true,
			  success: function(response, status, jqxhr) {
				  $.ibisMessaging("close", {type:"primary"}); 
				  var data = $.parseJSON(response);
				  for (var i=0; i<data.length; i++){
					  	$.ibisMessaging("primary", "error", "Short Name "+data[i].msgType,{container: "#messageContainer"});
				  }
				  EventBus.trigger("close:processing");
				  if(data.length==0){
						
						if(questionArray.length<1){
							EventBus.trigger("close:processing");
							$.ibisMessaging("dialog", "error", "You need at least a question inside a section to save an eForm",{container: "body"});
							$("#saveFormButton").removeClass("disabled");
							return;
							
						}
						//Removed S_ pattern from section
						for(var i=0;i<secArray.length;i++){
							var s = secArray[i];
							var oldId=s.id;
							var newId=oldId.replace('S_','');
							s.id=newId;
						}
						
						

						formModel.deleteSections.forEach(function(deleteSection){
							deleteSectionArray.push(deleteSection.get("id").replace('S_',''));
						});
						
						$('input[name=formBuildFormId]').val(formModel.get('formid'));
						$('input[name=formMode]').val(Config.formMode);
						$('input[name=sectionsJSON]').val(JSON.stringify(secArray));
						$('input[name=questionsJSON]').val(JSON.stringify(questionArray));
						//for edit for story
						$('input[name=existingSectionIdsToDeleteJSON]').val(JSON.stringify(deleteSectionArray));
						$('input[name=existingQuestionIdsToDeleteJSON]').val('[]');
						//
						EventBus.trigger("save:global");
						$('#app').submit();
				  }
				
			  },
			  error: function (xhr, status, errorThrown ) {
				 alert("errorThrown"+errorThrown);
			  }
		});
	
		//alert("save form button clicked");
	}, 1000, true),
	
	editFormDetails : function() {
		EventBus.trigger('open:formEditor', this.model);
	},
	emailTriggerAnswers : function(questionVal){
		var etObjArray = [];
		var triggerAnswers=questionVal.attributeObject.triggerAnswers;
		for(var i=0;i<triggerAnswers.length;i++){
			if(typeof triggerAnswers[i]=="string"){
				var etObj= {
						etValId : (i+1)*-1,
						etAnswer : triggerAnswers[i]	
				};
				etObjArray.push(etObj);
			}else if((typeof triggerAnswers[i]=="object") &&triggerAnswers[i]!=null ){
				etObjArray.push(triggerAnswers[i]);
			}
		
		}
		questionVal.attributeObject.triggerAnswers = etObjArray;
	},
	
	addSection : function() {
		var sectionConfig = {
			name 			: Config.language.defaultTextName, 
			textContainer 	: true,
			gridtype 		: true,
			isManuallyAdded	: true
		};
		
		if(FormBuilder.form.get('isCAT') && FormBuilder.form.get("measurementType") != 'shortForm'){
			var msg = "<div class='ibisMessaging-message ibisMessaging-primary ibisMessaging-error'>This instrument is a computer adaptive test (CAT), so its questions are generated dynamically based on answers given. Because it is dynamic, a new section can not be added.</div>";
			$.ibisMessaging("dialog", "info", msg, {
				width: 500,
				dialogClass : "formBuilder_editor",
				modal : true,
				buttons: [
				{
					text: "Ok",
					click : function() {
						$(this).dialog("close");
				    }
				}]				               
			});		
		}else{		
			// to adjust for tables, we have to make sure we don't add the new
			// section inside a table
			var activeSection = FormBuilder.page.get("activeSection");
			if (activeSection != null && activeSection.isTableSection()) {
				var $after = this.findLastSectionDivInTable(activeSection);
				sectionConfig.renderAfter = $after;
			}
	
			var newSection = FormBuilder.form.addSection(sectionConfig);
			
			EventBus.trigger("add:section", newSection);
		}
	},
	
	addQuestion : function(type, config) {
		if (typeof config === "undefined") {
			config = {};
		}
		
		var activeSection = FormBuilder.page.get("activeSection");
		if (activeSection != null) {
			var activeSectionDivId = FormBuilder.page.get("activeSection").get("divId");
			var activeSectionModel = FormBuilder.form.getSectionByDivId(activeSectionDivId);
			var defaults = {
					questionName : "",
					questionText : "New Question",
					sectionId	 : activeSection.get("id")
				};
			var question = $.extend({}, defaults, config);
			question.questionType = type;
			var newQuestion = activeSectionModel.addQuestion(question);
			EventBus.trigger("add:question", newQuestion);
		}
	},
	
	addQuestionFromLibrary : function() {
		EventBus.trigger("open:questionLibrary");
	},
	
	addTextboxQuestion : function() {
		this.addQuestion(Config.questionTypes.textbox);
	},
	
	addTextareaQuestion : function() {
		this.addQuestion(Config.questionTypes.textarea);
	},
	
	addRadioQuestion : function() {
		this.addQuestion(Config.questionTypes.radio);
	},
	
	addCheckboxQuestion : function() {
		this.addQuestion(Config.questionTypes.checkbox);
	},
	
	addSelectQuestion : function() {
		this.addQuestion(Config.questionTypes.select);
	},
	
	addMultiSelectQuestion : function() {
		this.addQuestion(Config.questionTypes.multiSelect);
	},
	
	addImageMapQuestion : function() {
		this.addQuestion(Config.questionTypes.imageMap);
	},
	
	addFileUploadQuestion : function() {
		this.addQuestion(Config.questionTypes.fileUpload);
	},
	
	addVisualScaleQuestion : function() {
		this.addQuestion(Config.questionTypes.visualscale);
	},
	
	addText : function() {
		this.addQuestion(Config.questionTypes.textblock, {questionText:"Text Block"});
	},
	
	cancelForm : _.debounce(function() {
		//Modal popup that blocks user interaction with the application once they click the button
		EventBus.trigger("open:processing", Config.language.cancellingFormCreation);
		$("cancelFormButton").addClass("disabled");
		var mode = Config.formMode;
		var formId = FormBuilder.form.get("id");
		//var questionsByFormId(formId);
		var graphicNamesOrigMapping = {};
		var graphicNamesMapping = {};
		var formModel = this.model;
		formModel.sections.forEach(function(section) {
			section.questions.forEach(function(question) {
				var graphicNamesOrig = question.get("graphicNamesOrig");
				if(graphicNamesOrig!='undefined' 
					&& graphicNamesOrig!=null
					&& graphicNamesOrig.length !=0)
				{
					graphicNamesOrigMapping[question.get("questionId")]= question.get("graphicNamesOrig");
				}
				else
				{
					//graphicNamesOrigMapping[''+question.get("questionId")]= "";
					graphicNamesOrigMapping[''+question.get("questionId")]= "None";
				}
				var graphicNames = question.get("graphicNames");
				if(graphicNames!='undefined' 
					&& graphicNames!=null
					&& graphicNames.length !=0)
				{
					graphicNamesMapping[question.get("questionId")]= question.get("graphicNames");
				}
				else
				{
					//graphicNamesMapping[''+question.get("questionId")]= "";
					graphicNamesMapping[''+question.get("questionId")]= "None";
				}
			});
		});
		
		var url;		
		if (mode == "create") {
			//url = baseUrl + '/form/deleteForm.action?mode=create&id=' + formId;
			url = baseUrl + 'eFormDeleteQuestionsAction!deleteEformQuestions.action';

		}
		else {
//			url = baseUrl + "/form/formHome.action?cancelFromBuildForm=true&&formId="+ formId
//			      +"&&graphicNamesOrigMappingJSON="+encodeURIComponent(JSON.stringify(graphicNamesOrigMapping))
//			      +"&&graphicNamesMappingJSON="+encodeURIComponent(JSON.stringify(graphicNamesMapping));
			url = baseUrl + 'eFormSearchAction!list.action';
		}
		
		redirectWithReferrer(url);
	}, 1000, true),
	
	enableDisableAddQuestionButton : function(section) {
		//TO DO
		if (FormBuilder.form.sections.length > 0) {
			this.$el.find(".disabled").removeClass("disabled");
		}
		else {
			// disable
			this.$el.find(".disableable").addClass("disabled");
		}
	},
	
	needEnableResetTable : function(section) {
		if (typeof section === "undefined") {
			section = FormBuilder.page.get("activeSection");
		}
		if (section.get("tableGroupId") != 0) {
			return true;
		}
		return false;
	},
	
	enableDisableResetTable : function(section) {
		var $li = this.$("#removeTableButton").parent("li.disableable").eq(0);
		if ($li.length > 0) {
			if (this.needEnableResetTable(section)) {
				$li.removeClass("disabled");
			}
			else if (!$li.hasClass("disabled")) {
				$li.addClass("disabled");
			}
		}
	},
	
	addDataElementQuestion : function() {
		var view = FormBuilder.page.get("dataElementsView2");
		if(FormBuilder.form.get('isCAT') && FormBuilder.form.get("measurementType") != 'shortForm'){
			var msg = "<div class='ibisMessaging-message ibisMessaging-primary ibisMessaging-error'>This instrument is a computer adaptive test (CAT), so its questions are generated dynamically based on answers given. Because it is dynamic, a new data element can not be added.</div>";
			$.ibisMessaging("dialog", "info", msg, {
				width: 500,
				dialogClass : "formBuilder_editor",
				modal : true,
				buttons: [
				{
					text: "Ok",
					click : function() {
						$(this).dialog("close");
				    }
				}]				               
			});	
		}else{		
			if ( (view !== null) && (typeof view != "undefined") ) {
				// Show the processing dialog box.
		   		EventBus.trigger("open:processing", Config.language.loadDataElem);
		   		
		   		view.render();
			}
			else {
				Log.user.error(Config.language.openDeTableError);
			}
		}
	},
	
	openLayoutEditor : function() {
		if(FormBuilder.form.get('isCAT') && FormBuilder.form.get("measurementType") != 'shortForm'){
			var msg = "<div class='ibisMessaging-message ibisMessaging-primary ibisMessaging-error'>This instrument is a computer adaptive test (CAT), so its questions are generated dynamically based on answers given. Because it is dynamic, the layout can not be edited.</div>";
			$.ibisMessaging("dialog", "info", msg, {
				width: 500,
				dialogClass : "formBuilder_editor",
				modal : true,
				buttons: [
				{
					text: "Ok",
					click : function() {
						$(this).dialog("close");
				    }
				}]				               
			});	
		}else{
			EventBus.trigger("open:layoutEditor", FormBuilder.page.get("activeQuestion"));			
		}
	},
	
	openTableEditor : function() {
		if(FormBuilder.form.get('isCAT') && FormBuilder.form.get("measurementType") != 'shortForm'){
			var msg = "<div class='ibisMessaging-message ibisMessaging-primary ibisMessaging-error'>This instrument is a computer adaptive test (CAT), so its questions are generated dynamically based on answers given. Because it is dynamic, the layout can not be edited.</div>";
			$.ibisMessaging("dialog", "info", msg, {
				width: 500,
				dialogClass : "formBuilder_editor",
				modal : true,
				buttons: [
				{
					text: "Ok",
					click : function() {
						$(this).dialog("close");
				    }
				}]				               
			});	
		}else{
			EventBus.trigger("open:tableEditor", FormBuilder.page.get("activeSection"));
		}
	},
	
	resetTableSection : function() {
		if (this.needEnableResetTable()) {
			EventBus.trigger("reset:tableSection", FormBuilder.page.get("activeSection"));
		}
	},
	
	sectionIsInTable : function($section) {
		if ($section.hasClass(Config.tableHeaderClassNames.columnHeader) 
				|| $section.hasClass(Config.tableHeaderClassNames.tableHeader)
				|| $section.hasClass(Config.tableHeaderClassNames.tablePrimary)) {
			return true;
		}
		else {
			return false;
		}
	},
	
	getNextSection : function($section) {
		return $section.next("." + Config.identifiers.section);
	},
	
	findLastSectionDivInTable : function(sectionModel) {
		var $section = FormBuilder.form.getSectionDiv(sectionModel);
		if (sectionModel.isTableSection()) {
			while (this.sectionIsInTable(this.getNextSection($section))) {
				$section = this.getNextSection($section);
			}
		}
		return $section;
	}
});