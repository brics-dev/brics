/**
 * 
 */
var TextBlockEditView = QuestionEditView.extend({
	dialogTitle : Config.language.questionTextEditorTitle,
	templateName : "editTextblockTemplate",
	$tinymce : null,
	
	validationRules : [
   	    new ValidationRule({
   	    	fieldName : "htmlText",
   	    	description : Config.language.questionText,
   	    	
   	    	match : function(model) {
   	    		var questionText = model.get("htmlText");
   	    		
   	    		// Check for the existence of question text.
   	    		if ( questionText.length == 0 ) {
   	    			return false;
   	    		}
   	    		
   	    		return true;
   	    	}
   	    }) 
   	],
	
    events : {
   		"change .questionType" : "onChangeQuestionType"
   	},
   	
	initialize : function() {
		TextBlockEditView.__super__.initialize.call(this);
		this.template = TemplateManager.getTemplate(this.templateName);
		this.initCommons();
		
	},
	
	render : function(model) {
		this.model = model;
		
		this.$el.html(this.template(model.attributes));
		TextBlockEditView.__super__.render.call(this, model);
		
		// Reset the open dialog's title
		this.$el.dialog("option", "title", Config.language.questionTextEditorTitle);
		
		this.findWysiwyg();
		this.initWysiwyg(this.model.get("htmlText"));
		
		this.$('select[name="questionType"] option').prop("disabled", true);
		
		return this;
	},
	
	isWysiwygInit : function() {
		return this.$tinymce.is(":tinymce");
	},
	
	findWysiwyg : function() {
		this.$tinymce = this.$el.find(".tinymce");
		return this.$tinymce;
	},
	
	initWysiwyg : function(text) {
		if (!this.isWysiwygInit()) {
			var $ele = this.$tinymce;
			var eleHeight = $ele.height();
			var $target = $("#dialog_editTextblock_editorField");
			var model = this.model;
			
			// a function to throttle updating the value field when user types
			// or makes changes.  Bound in tinymce.setup
			var copyContents = _.throttle(function(ed) {
				$target.val(ed.getContent());
				$target.trigger("change");
			}, 500);
			
			this.$tinymce.tinymce({
				script_url	: "/portal/formbuilder/js/tinymce/tinymce.min.js",
				menubar 	: false,
				statusbar	: false,
				plugins		: ["textcolor"],
				toolbar1	: "undo redo | styleselect fontsizeselect | bold italic | alignleft aligncenter alignright alignjustify | bullist numlist | link | forecolor backcolor | removeformat",
				oninit		: function() {
					// resize the container to fit this editor.  I'm not sure why it doesn't work by itself but it doesn't
//					var $row = $ele.parents(".ui-tabs-panel");
//					var fullEditorHeight = 170;
//					$row.height($row.height() - eleHeight + fullEditorHeight);
					
					// set the initial content to the model's content if we're editing
					this.activeEditor.setContent(model.get("htmlText"));
				},
				setup : function(ed) {
					ed.on('change keyup', function(e) {
						copyContents(ed);
					});
				}
			});
		}
		else {
			this.$tinymce.html("");
		}
	},
	
	setWywiwygContent : function(contentHTML) {
		this.$tinymce.tinymce().setContent(contentHTML);
	},
	
	getWywiwygHTML : function() {
		return this.$tinymce.tinymce().getContent();
	},
	
	getWywiwygText : function() {
		return this.$tinymce.tinymce().getContent({format: "text"});
	},
	
	removeWysiwyg : function() {
		this.$tinymce.tinymce().remove("#" + this.$tinymce.attr("id"));
	},
	
	close : function() {
		this.removeWysiwyg();
		return TextBlockEditView.__super__.close.call(this);
		EventBus.trigger("resize:question", this.model);
	},
	
	save : function() {
		EventBus.trigger("resize:question", this.model);
		TextBlockEditView.__super__.save.call(this);
	},
	
	onChangeQuestionType : function() {
		var msg = "Are you sure you want to change the question type?";
		var qModel = this.model;
		var previous = qModel.previous("questionType");
		
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
					EventBus.trigger("change:questionType", FormBuilder.page.get("activeEditorView").model);
					$(this).dialog("close");
				}
			}]
			               
		});
	 }
	
});