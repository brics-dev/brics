/**
 * 
 */
var CheckboxView  = QuestionView.extend({
	className : "question formGrid-1",
	typeClassName : "checkbox",
	
	initialize : function() {
		CheckboxView.__super__.initialize.call(this);
		this.template = TemplateManager.getTemplate("checkboxQuestionTemplate");
		this.listenTo(this.model, "change:questionOptionsObjectArray", this.afterChangeOptionsObjectArray);
		this.listenTo(this.model, "change:horizontalDisplay", this.afterChangeOptions);
		this.listenTo(this.model, "change:displayPV", this.afterChangeOptions);
		this.listenTo(this.model, "change:includeOther", this.showHideIncludeOther);
	},
	
	render : function($after) {
		CheckboxView.__super__.render.call(this, $after);
		this.afterChangeOptionsObjectArray(this.model);
		this.showHideIncludeOther(this.model);
		this.ellipsis(this.$('.checkboxOption label'),100);
	},
	
	
	afterChangeOptions : function(model) {

		this.$(".checkboxOptionsContainer").empty();

		var qOptionsObjectArray = this.model.get("questionOptionsObjectArray");
		
		if(qOptionsObjectArray.length > 0) {
			var horizontalDisplay = model.get("horizontalDisplay");
			var displayPV = model.get("displayPV");
			var optionHtml = "";
			var optionTemplate;
			var optionsObject;
			var option;
			var tooltipText;
			
			for(var i=0;i<qOptionsObjectArray.length;i++) {
				optionsObject = qOptionsObjectArray[i];
				option = optionsObject.option;
				
				if (displayPV) {
					option = optionsObject.submittedValue;
					tooltipText = optionsObject.option;
				} else {
					option = optionsObject.option;
					tooltipText = optionsObject.submittedValue;
				}
				
				if(horizontalDisplay) {
					optionTemplate = TemplateManager.getTemplate("checkboxQuestionInputHorizontalDisplay");
				}else {
					optionTemplate = TemplateManager.getTemplate("checkboxQuestionInputVerticalDisplay");
				}
				
				optionHtml = optionHtml + optionTemplate({
					label : option, tooltip: tooltipText
				});
	
				//fonts????
	
				
				
			}
			
			this.$(".checkboxOptionsContainer").html(optionHtml);
		}
		
		
		
		EventBus.trigger("resize:question", this.model);
	},
	
	
	
	
	afterChangeOptionsObjectArray : function(model) {

		this.$(".checkboxOptionsContainer").empty();

		var qOptionsObjectArray = model.get("questionOptionsObjectArray");
		
		if(qOptionsObjectArray.length > 0) {
			var horizontalDisplay = model.get("horizontalDisplay");
			var displayPV = model.get("displayPV");
			var optionsObject;
			var option;
			var optionHtml = "";
			var optionTemplate;
			var tooltipText;
			
			for(var i=0;i<qOptionsObjectArray.length;i++) {
				
				optionsObject = qOptionsObjectArray[i];
				
				if (displayPV) {
					option = optionsObject.submittedValue;
					tooltipText = optionsObject.option;
				} else {
					option = optionsObject.option;
					tooltipText = optionsObject.submittedValue;
				}
				
				if(horizontalDisplay) {
					optionTemplate = TemplateManager.getTemplate("checkboxQuestionInputHorizontalDisplay");
				}else {
					optionTemplate = TemplateManager.getTemplate("checkboxQuestionInputVerticalDisplay");
				}
				
				optionHtml = optionHtml + optionTemplate({
					label : option,
					tooltip: tooltipText
				});
	
				//fonts????
	
				
				
			}
			
			this.$(".checkboxOptionsContainer").html(optionHtml);
		}
		
		
		
		EventBus.trigger("resize:question", this.model);
	},
	
	
	showHideIncludeOther : function(model) {
		var includeOther = model.get("includeOther");
		var horizontalDisplay = model.get("horizontalDisplay");
		if(includeOther != null ) {
			//determine show/hide
			if(includeOther) {
				var includeOtherDiv = this.$(".checkboxOptionsContainer").parent().find(".includeOtherDiv");
				$(includeOtherDiv).removeClass("hiddenDiv").addClass("showDiv");
				if(horizontalDisplay) {
					$(includeOtherDiv).removeClass("clearfix");
				}else {
					$(includeOtherDiv).addClass("clearfix");
				}
			}else {
				var includeOtherDiv = this.$(".checkboxOptionsContainer").parent().find(".includeOtherDiv");
				$(includeOtherDiv).removeClass("showDiv").addClass("hiddenDiv");
			}
		}else {
			//hide
			var includeOtherDiv = this.$(".checkboxOptionsContainer").parent().find(".includeOtherDiv");
			$(includeOtherDiv).removeClass("showDiv").addClass("hiddenDiv");
		}
	}
})








