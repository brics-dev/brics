
var QuestionTextEditView = QuestionEditView.extend({
	dialogTitle : "",
	templateName : "questionTextEditorTemplate",
	$tinymce : null,
	currentAttribute : "",
	
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
	
	initialize : function() {
		QuestionTextEditView.__super__.initialize.call(this);
		this.template = TemplateManager.getTemplate(this.templateName);
		this.initCommons();
	},
	
	render : function(model,attribute) {
		this.model = model;
		this.currentAttribute = attribute;
		
		this.$el.html(this.template(model.attributes));
		QuestionTextEditView.__super__.render.call(this, model);

		this.dialogTitle = "Advanced Question Text Formatter:" + model.get("dataElementName");
		
		// Reset the open dialog's title
		this.$el.dialog("option", "title", this.dialogTitle);
		
		this.findWysiwyg();
		this.initWysiwyg(this.currentAttribute);
		
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
					
					// set the initial content to the model's content if we're editing
					this.activeEditor.setContent(model.get(text));
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
	
	save : function() {
		QuestionTextEditView.__super__.save.call(this);
		
		var attribute = this.model.get("htmlText");
		this.model.set(this.currentAttribute,attribute);
		
		EventBus.trigger("open:questionEditor", this.model);
		
		if(this.currentAttribute=="descriptionUp" || this.currentAttribute=="descriptionDown"){
			$('[href="#dialog_editQuestion_additionalText"]').click();
		}
		

	},
	
	cancel : function() {
		
		EventBus.trigger("open:questionEditor", this.model);
		return QuestionTextEditView.__super__.close.call(this);
	}
})