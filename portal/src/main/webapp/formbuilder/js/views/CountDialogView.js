/**
 * 
 */
var CountDialogView = BaseView.extend({
	dialogTitle: "Select Questions",
	
	buttons: [{
		id : "countQuestionsSave",
		text : "Save",
		click : function() {
			var $table = $("#countQuestionsTable");
			var selectedOptions = IDT.getSelectedOptions($table);
			var outputHtml = "";
			var questionsToCount = [];
			
			if (selectedOptions.length > 0) {
				selectedOptions.forEach(function(id, index) {
					// add to the model
					questionsToCount.push(id);
					
					if (outputHtml != "") {
						outputHtml += " + ";
					}
					outputHtml = outputHtml + "[" + IDT.getCellContent($table, "dataElementName")[index] + "]";
				});
			}
			
			var activeEditorView = FormBuilder.page.get("activeEditorView");
			activeEditorView.$("#questionsToCountDisplay").html(outputHtml);
			activeEditorView.model.set({
				questionsInCount: questionsToCount,
				countFormula: outputHtml,
				countFlag: questionsToCount.length > 0
			});
			
			$(this).dialog("close");
		}
	},
	{
		id : "countQuestionsCancel",
		text : "Cancel",
		click : function() {
			$(this).dialog("close");
		}
	}],
	
	initialize : function() {
		this.template = TemplateManager.getTemplate("countDialog");
	},
	
	render : function(model) {
		EventBus.on("init:table", this.initTableListener, this);
		EventBus.on("close:countQuestionDialog", this.close, this);
		this.model = model;
		this.$el.html(this.template());
		this.Dialog.initAndOpen(this);
	},
	
	close : function() {
   		// Destroy the table
   		IDT.derenderTable(this.$("#countQuestionsTable"));
		this.$el.empty();
		
		// Un-bind events
		EventBus.off("init:table", this.initTableListener, this);
   		EventBus.off("close:countQuestionDialog", this.close, this);
   	},
   	
   	initTableListener : function(dataTable) {
   		var $table = this.$("#countQuestionsTable");
   		
   		// Check if the init event is for the count questions table.
		if ( _.isEqual(dataTable, IDT.getTableModel($table)) ) {
	   		// select already-chosen questions
	   		var questionsToCount = this.model.get("questionsInCount");
	   		
	   		if ( questionsToCount.length > 0 ) {
	   			for ( var i = 0; i < questionsToCount.length; i++ ) {
	   				IDT.addSelectedOptionValue($table, questionsToCount[i]);
	   			}
	   		}
		}
   	},
   	
   	Dialog : {
   		initAndOpen : function(view) {
   			view.$el.dialog({
   				title : view.dialogTitle,
   				modal: true,
   				width: 1100,
   				maxHeight: Config.getMaxDialogHeight(),
   				buttons: view.buttons,
   				dialogClass: "formBuilder_dialog_noclose",
   				
   				close : function(event, ui) {
   					$(this).dialog("destroy");
   					EventBus.trigger("close:countQuestionDialog");
   				},
   				
   				open : function(event, ui) {
   				// get data and load into datatable
					var activeSectionId = FormBuilder.page.get("activeSection").get("id");
					var activeQuestionId = FormBuilder.page.get("activeQuestion").get("questionId");
					var availableCountQuestionsArray = [];
					
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
										
										if (qType != '12') {
											var sqId = sId + '_Q_' + qId;
											var qName = question.get("questionName");
											
											var undeScoreIndex = qName.indexOf("_");
											qName = qName.substring(undeScoreIndex+1,qName.length)
											
											
											var qText = question.get("questionText");
											var qType = question.get("questionType");
											var qTypeLabel = question.getQuestionTypeLabel(qType);
											var countQ = [];
											
											// Now populate the list
											countQ.push("<input type=\"checkbox\" value=\""+ sqId+ "\"/>");
											countQ.push(sName);
											countQ.push(qName);
											countQ.push(qText);
											countQ.push(qTypeLabel);
											countQ.push(question.get("dataElementName"));
											availableCountQuestionsArray.push(countQ);
										}
									}	
								});
							}
						}
					});
					
					var availableCountQuestionsTable = {
					 	"aaData" : availableCountQuestionsArray,
					 	"aoColumns" : [
							{"sTitle":""},
							{"sTitle":"Section"},
							{"sTitle":"Question Name"},
							{"sTitle":"Question Text"},
							{"sTitle":"Question Type"},
							{"sTitle":"dataElementName", "bVisible":false}
					 	]
					};

					IDT.buildSingleData(availableCountQuestionsTable, $("#countQuestionsTable"));
					
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