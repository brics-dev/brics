var QuestionLibraryView = BaseView.extend({
	dialogTitle : "Add Question from Question Library",
	
	buttons : [
		{
			id : "librarySaveBtn",
			text : "Add",
			"class":"ui-priority-primary",
			click : _.debounce(function() {
				$(this).siblings().find("#librarySaveBtn").prop("disabled", true);
				EventBus.trigger("fetch:questionLibrary");
			}, 1000, true)
		},
		
		{
			text: "Cancel",
			"class":"ui-priority-secondary",
			click: function() {
				$(this).dialog("close");
				EventBus.trigger("close:questionLibrary");
			}
		}
	],
	
	events : {
		
	},
	
	initialize : function() {
		QuestionLibraryView.__super__.initialize.call(this);
		this.template = TemplateManager.getTemplate("questionLibraryTemplate");
	},
	
	render : function() {
		// Bind to events.
		EventBus.on("fetch:questionLibrary", this.getQuestionDetails, this);
		EventBus.on("init:table", this.initQuestLibTable, this);
		EventBus.on("close:questionLibrary", this.close, this);
		
		// Reset the model
		this.model = new QuestionLibrary();
		
		// Add the template to the page
		this.$el.html(this.template(this.model.attributes));
		this.$el.attr("id", "questionLibTableDisplay");
		
		var view = this;
		var activeQuestions = this.getExistingQuestionIdArray();
		
		// Get the question library table data and init the table and dialog box.
		$.getJSON("../questionjson/getQuestionLibrary.action", {qIds: activeQuestions.toString()}, function(data, textStatus, jqXHR) {
			var tableConfig = {
					aaData : $.parseJSON(data),
					
					aoColumns : [
						{ sTitle : "" },
						{ sTitle : "Question ID", sClass : "center" },
						{ sTitle : "Name (Version)" },
						{ sTitle : "Text" },
						{ sTitle : "Type" }
					]
			};
			
			var $table = view.$("#questionTable");
			
			// Create the question library table
			IDT.buildSingleData(tableConfig, $table);
			
			// Save DataTables object to the model
			view.model.set("oTable", $table);
			
			// Open the dialog box and show the question library
			view.LibraryDialog.initAndOpen(view);
		})
		.fail(function(jqXHR, textStatus, errorThrown) {
			var message = "";
			var messageBtns = [{
				text : "OK",
				
				click : function() {
					$(this).dialog("close");
				}
			}];
			
			// Set the error message and/or the buttons
			switch( jqXHR.status ) {
				case 400 :
					message = Config.language.questLibFilterError;
					break;
				case 500 :
					message = Config.language.getQuestLibError;
					break;
				default :
					message = Config.language.questLibDefaultError;
					break;
			}
			
			$.ibisMessaging("dialog", "error", message, {
				width : "400",
				buttons : messageBtns
			});
		});
		
		QuestionLibraryView.__super__.render.call(this);
	},
	
	initQuestLibTable : function(dataTable) {
		var $table = this.$("#questionTable");
   		
   		// Check if the init event is for the form structures table.
		if ( _.isEqual(dataTable, IDT.getTableModel($table)) ) {
			// Disable the select all feature.
			IDT.disableSelectAll($table);
		}
	},
	
	LibraryDialog : {
		initAndOpen : function(view) {
			view.$el.dialog({
				title : view.dialogTitle,
				modal : true,
				width : 1100,
				maxHeight : Config.getMaxDialogHeight(),
				buttons : view.buttons,
				dialogClass : "formBuilder_dialog_noclose",
				closeOnEscape : false,
				
				open : function(event, ui) {
					EventBus.trigger("close:processing");
				},
				
				close : function(event, ui) {
					$(this).dialog("destroy");
				}
			});
		},
		
		close : function(view) {
			// Check if the dialog can be closed
			if ( view.$el.hasClass("ui-dialog-content") && this.isOpen(view) ) {
				view.$el.dialog("close");
			}
		},
		
		isOpen : function(view) {
			return view.$el.dialog("isOpen");
		}
	},
	
	getQuestionDetails : function() {
		var selectedValue = IDT.getSelectedOptions(this.model.get("oTable"))[0];
		
		if ( (typeof selectedValue != "undefined") && (selectedValue !== null) && (selectedValue.length != 0) ) {
			var strArray = selectedValue.split(",");
			var questionId = strArray[0].trim();
			var questionVersion = strArray[1].trim();
			var view = this;
			
			// Get the question object from the server
			$.getJSON("../questionjson/getQuestionFromLibrary.action", {
				qId : questionId,
				qVersion : questionVersion
			}, function(data, textStatus, jqXHR) {
				// Create a new question model object and save it to the view model
				var q = new Question($.parseJSON(data));
				view.model.set("chosenQuestion", q);
				
				// Close the dialog window
				view.LibraryDialog.close(view);
				
				// Send the add question from library event
				EventBus.trigger("add:questionLibrary", q);
			})
			.fail(function(jqXHR, textStatus, errorThrown) {
				$.ibisMessaging("dialog", "error", "Could not get the question object.", {container: "body", modal: true});
				view.$el.siblings().find("#librarySaveBtn").prop("disabled", false);
			});
		}
		else {
			// Display error message and re-enable the "Add" button
			$.ibisMessaging("dialog", "error", "No question was selected.", {modal: true});
			this.$el.siblings().find("#librarySaveBtn").prop("disabled", false);
		}
	},
	
	getExistingQuestionIdArray : function() {
		var existingQuestionIds = [];
		var questionCollection = FormBuilder.page.get("activeSection").questions;
		
		// Check if a question collection was returned
		if ( (questionCollection != null) && (typeof questionCollection !== "undefined") ) {
			// Loop through the question collection and collect the IDs of questions that are already in the active section.
			questionCollection.forEach(function(question, index, list) {
				var qId = parseInt(question.get("questionId"));
				
				if ( qId > 0 ) {
					existingQuestionIds.push(qId);
				}
				
			}, this);
		}
		
		return existingQuestionIds;
	},
	
	close : function() {
		EventBus.off("fetch:questionLibrary", this.getQuestionDetails, this);
		EventBus.off("init:table", this.initQuestLibTable, this);
		EventBus.off("close:questionLibrary", this.close, this);
		
		// Destroy the question table.
		var $table = this.model.get("oTable");
		
		if ( ($table.length != 0) && IDT.isInit($table) ) {
			IDT.derenderTable($table);
			this.$el.empty();
		}
		
		QuestionLibraryView.__super__.close.call(this);
	},
	
	destroy : function() {
		this.close();
		QuestionLibraryView.__super__.destroy.call(this);
	}
});