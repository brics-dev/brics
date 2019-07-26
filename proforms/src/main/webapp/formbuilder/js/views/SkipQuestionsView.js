var SkipQuestionsView = BaseView.extend({
	
	dialogTitle : "Available Questions",
	
	buttons : [{
		id : "skipQuestionAddBtn",
		text : "Add",
		click : function() {
			var $table = $("#skipQuestionsTable");
			var selectedOptions = IDT.getSelectedOptions($table);
			var skipRowsHtml = "";
			var skipRowTemplate;
			var questionsToSkip = [];
			
			if (selectedOptions.length > 0) {
				selectedOptions.forEach(function(id, index) {
					questionsToSkip.push(id);
					skipRowTemplate = TemplateManager.getTemplate("skipQuestionRow");
					
					skipRowsHtml = skipRowsHtml + skipRowTemplate({
						section : IDT.getCellContent($table,"Section")[index],
						questionName : IDT.getCellContent($table,"Question Name")[index],
						questionText : IDT.getCellContent($table,"Question Text")[index],
						questionType : IDT.getCellContent($table,"Question Type")[index]
					});
				});
			}
			
			FormBuilder.page.get("activeEditorView").$(".divQuestionsToSkip").html(skipRowsHtml);
			FormBuilder.page.get("activeEditorView").model.set("questionsToSkip", questionsToSkip);
			
			$(this).dialog("close");
		}
	},
	
	{
		text: "Cancel",
		click: function() {
			$(this).dialog("close");
		}
	}],
	   	
   	initialize : function() {
   		this.template = TemplateManager.getTemplate("skipQuestionsTemplate");
   	},
	   	
   	render : function(model) {
   		// Bind to events
   		EventBus.on("init:table", this.initTableListener, this);
   		EventBus.on("close:skipQuestionDialog", this.close, this);
   		
   		this.model = model;
   		
   		// get template for div
   		this.$el.html(this.template());
   		
   		// open dialog
   		this.SkipQuestionsDialog.initAndOpen(this);
   	},
   	
   	close : function() {
   		// Destroy the table
   		IDT.derenderTable(this.$("#skipQuestionsTable"));
		this.$el.empty();
		
		// Un-bind events
		EventBus.off("init:table", this.initTableListener, this);
   		EventBus.off("close:skipQuestionDialog", this.close, this);
   	},
   	
   	initTableListener : function(dataTable) {
   		var $table = this.$("#skipQuestionsTable");
   		
   		// Check if the init event is for the skip questions table.
		if ( _.isEqual(dataTable, IDT.getTableModel($table)) ) {
	   		// Need to check whichever skip rule questions have been set
	   		var questionsToSkipArray = this.model.get("questionsToSkip");
	   		
	   		if ( questionsToSkipArray.length > 0 ) {
	   			for ( var i = 0; i < questionsToSkipArray.length; i++ ) {
	   				IDT.addSelectedOptionValue($table, questionsToSkipArray[i]);
	   			}
	   		}
		}
   	},
	   	
   	SkipQuestionsDialog : {
		initAndOpen : function(view) {
			view.$el.dialog({
				title : view.dialogTitle,
				modal : true,
				width : 1100,
				maxHeight : Config.getMaxDialogHeight(),
				buttons : view.buttons,
				dialogClass : "formBuilder_dialog_noclose",
				
				close : function(event, ui) {
					$(this).dialog("destroy");
					EventBus.trigger("close:skipQuestionDialog");
				},
				
				open : function(event, ui) {
					// get data and load into datatable
					var activeSectionId = FormBuilder.page.get("activeSection").get("id");
					var activeQuestionId = FormBuilder.page.get("activeQuestion").get("questionId");
					var availableSkipQuestionsArray = [];
					
					FormBuilder.form.getSectionsInPageOrder().forEach(function(section) {
						if ( !(section.get("isRepeatable") && (section.get("repeatedSectionParent") == activeSectionId)) ) {
							var sectionQuestions = section.getQuestionsInPageOrder();

							if (sectionQuestions.length > 0) {
								var sId = section.get("id");
								var sName = section.get("name");
								
								
								if(section.get("isRepeatable") && (!(sId == activeSectionId))) {
								//if(section.get("isRepeatable")) {
									var index = section.getRepeatableIndex();
									sName = sName + "(" + index + ")";
								}
								
								
								sectionQuestions.forEach(function(question) {
									var qId  = question.get("questionId");
									
									// We don't want to include the active question in the list
									if ( !((sId == activeSectionId) && (qId == activeQuestionId)) ) {
										// We don't include visual scale and textblock in list
										var qType = question.get("questionType");
										
										if ( (qType != '10') && (qType != '12') ) {
											var sqId = sId + '_Q_' + qId;
											var qName = question.get("questionName");
											
											var undeScoreIndex = qName.indexOf("_");
											qName = qName.substring(undeScoreIndex+1,qName.length)
											
											
											var qText = question.get("questionText");
											var qType = question.get("questionType");
											var qTypeLabel = question.getQuestionTypeLabel(qType);
											var skipQ = [];
											
											// Now populate the list
											skipQ.push("<input type=\"checkbox\" value=\""+ sqId+ "\"/>");
											skipQ.push(sName);
											skipQ.push(qName);
											skipQ.push(qText);
											skipQ.push(qTypeLabel);
											availableSkipQuestionsArray.push(skipQ);
										}
									}	
								});
							}
						}
					});
					
					var availableSkipQuestionsTable = {
					 	"aaData" : availableSkipQuestionsArray,
					 	"aoColumns" : [
							{"sTitle":""},
							{"sTitle":"Section"},
							{"sTitle":"Question Name"},
							{"sTitle":"Question Text"},
							{"sTitle":"Question Type"}
					 	]
					};

					IDT.buildSingleData(availableSkipQuestionsTable, $("#skipQuestionsTable"));
					
					_.defer(function(view) {
						view.$el.dialog({position: {
							my : "center",
							at : "center",
							of : window
						}});
					}, view);
					
					EventBus.trigger("close:processing");
				}
			});
		},
		
		close : function(view) {
			if ( this.isOpen(view) ) {
				view.$el.dialog("close");
			}
		},
		
		isOpen : function(view) {
			var isDialogOpened = false;
			
			if ( view.$el.hasClass("ui-dialog-content") ) {
				isDialogOpened = view.$el.dialog("isOpen");
			}
			
			return isDialogOpened;
		}
	}
});
