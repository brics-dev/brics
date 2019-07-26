/**
 * 
 */
var RadioView  = QuestionView.extend({
	className : "question radio formGrid-1",
	
	initialize : function() {
		RadioView.__super__.initialize.call(this);
		this.template = TemplateManager.getTemplate("radioQuestionTemplate");
		this.listenTo(this.model, "change:questionOptionsObjectArray", this.afterChangeOptionsObjectArray);
		this.listenTo(this.model, "change:horizontalDisplay", this.afterChangeOptions);
		this.listenTo(this.model, "change:includeOther", this.showHideIncludeOther);
		this.listenTo(this.model, "change:defaultValue", this.onChangeDefaultValue);
	},
	
	render : function($after) {
		RadioView.__super__.render.call(this, $after);
		this.afterChangeOptions(this.model);
		this.afterChangeOptionsObjectArray(this.model);
		this.showHideIncludeOther(this.model);
		this.onChangeDefaultValue();
		this.ellipsis(this.$('.radioOption label'),100);
	},
	
	
	afterChangeOptions : function(model) {

		this.$(".radioOptionsContainer").empty();

		var qOptionsObjectArray = this.model.get("questionOptionsObjectArray");
		
		if(qOptionsObjectArray.length > 0) {
			
			var horizontalDisplay = model.get("horizontalDisplay"); //to do
			var optionHtml = "";
			var optionTemplate;
			var optionsObject;
			var option;
			for(var i=0;i<qOptionsObjectArray.length;i++) {
				optionsObject = qOptionsObjectArray[i];
				option = optionsObject.option;
				
				if(horizontalDisplay) {
					optionTemplate = TemplateManager.getTemplate("radioQuestionInputHorizontalDisplay");
				}else {
					optionTemplate = TemplateManager.getTemplate("radioQuestionInputVerticalDisplay");
				}
				
				optionHtml = optionHtml + optionTemplate({
					label : option
				});

				//fonts????

				
				
			}
			
			this.$(".radioOptionsContainer").html(optionHtml);
		}
		
		
		
		
		
		EventBus.trigger("resize:question", this.model);
	},
	
	
	
	afterChangeOptionsObjectArray : function(model) {

		this.$(".radioOptionsContainer").empty();

		var qOptionsObjectArray = this.model.get("questionOptionsObjectArray");
		
		if(qOptionsObjectArray.length > 0) {
			var horizontalDisplay = model.get("horizontalDisplay"); //to do
			var optionsObject;
			var option;
			var optionHtml = "";
			var optionTemplate;
			for(var i=0;i<qOptionsObjectArray.length;i++) {
				optionsObject = qOptionsObjectArray[i];
				option = optionsObject.option;
				
				if(horizontalDisplay) {
					optionTemplate = TemplateManager.getTemplate("radioQuestionInputHorizontalDisplay");
				}else {
					optionTemplate = TemplateManager.getTemplate("radioQuestionInputVerticalDisplay");
				}
				
				optionHtml = optionHtml + optionTemplate({
					label : option
				});

				//fonts????

				
				
			}
			
			this.$(".radioOptionsContainer").html(optionHtml);
		}
		
		
		
		
		
		EventBus.trigger("resize:question", this.model);
	},
	
	showHideIncludeOther : function(model) {
		var includeOther = model.get("includeOther");
		var horizontalDisplay = model.get("horizontalDisplay");
		if(includeOther != null ) {
			//determine show/hide
			if(includeOther) {
				var includeOtherDiv = this.$(".radioOptionsContainer").parent().find(".includeOtherDiv");
				$(includeOtherDiv).removeClass("hiddenDiv").addClass("showDiv");
				if(horizontalDisplay) {
					$(includeOtherDiv).removeClass("clearfix");
				}else {
					$(includeOtherDiv).addClass("clearfix");
				}
			}else {
				var includeOtherDiv = this.$(".radioOptionsContainer").parent().find(".includeOtherDiv");
				$(includeOtherDiv).removeClass("showDiv").addClass("hiddenDiv");
			}
		}else {
			//hide
			var includeOtherDiv = this.$(".radioOptionsContainer").parent().find(".includeOtherDiv");
			$(includeOtherDiv).removeClass("showDiv").addClass("hiddenDiv");
		}
	},
	
	onChangeDefaultValue:function(){
		var defaultVal = this.model.get("defaultValue");
		if(defaultVal != "") {
			this.$('.radioOptionsContainer input[type="radio"]').prop("checked", false);
			this.$(".radioOptionsContainer label").filter(function() {
				return $(this).text() == defaultVal;
			}).prev("input").prop("checked", true);
		}else {
			this.$('.radioOptionsContainer input[type="radio"]').prop("checked", false);
		}
	}
	
})








