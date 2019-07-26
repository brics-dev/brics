/**
 * 
 */
var MultiSelectView  = QuestionView.extend({
	className : "question multi-select formGrid-1",
	
	initialize : function() {
		MultiSelectView.__super__.initialize.call(this);
		this.template = TemplateManager.getTemplate("multiSelectQuestionTemplate");
		this.listenTo(this.model, "change:questionOptionsObjectArray", this.afterChangeOptionsObjectArray);
		//this.listenTo(this.model, "change:includeOther", this.showHideIncludeOther);
	},
	
	render : function($after) {
		MultiSelectView.__super__.render.call(this, $after);
		//this.afterChangeOptions();
		this.afterChangeOptionsObjectArray();
		//this.showHideIncludeOther(this.model);
	},
	
	afterChangeOptionsObjectArray : function() {	
		this.$(".multiSelectedQuestion").empty();
	
		var qOptionsObjectArray = this.model.get("questionOptionsObjectArray");
		var includeOther = this.model.get("includeOther");
		if(qOptionsObjectArray.length > 0) {
			
			//var horizontalDisplay = model.get("horizontalDisplay"); //to do
			var optionHtml = "";
			var optionTemplate;
			var optionsObject;
			var option;
			var submittedValue;
			for(var i=0;i<qOptionsObjectArray.length;i++) {
				optionsObject = qOptionsObjectArray[i];
				option = optionsObject.option;
				submittedValue = optionsObject.submittedValue;
				
				optionTemplate = TemplateManager.getTemplate("multiSelectQuestionInput");

				optionHtml = optionHtml + optionTemplate({
					optionText  : option,
				    optionValue : submittedValue
				});
									
			}
			
			if(includeOther)
			{
				var otherTemplate = TemplateManager.getTemplate("multiSelectQuestionInputOther");
				optionHtml = optionHtml + otherTemplate({
					optionText  : 'Other, Please Specify',
				    optionValue : 'true'
				});
			}
			
			this.$(".multiSelectedQuestion").html(optionHtml);	
		}
	},
	
	afterChangeOptionsObjectArray : function() {	
		this.$(".multiSelectedQuestion").empty();
	
		var qOptionsObjectArray = this.model.get("questionOptionsObjectArray");
		var includeOther = this.model.get("includeOther");
		if(qOptionsObjectArray.length > 0) {
			
			//var horizontalDisplay = model.get("horizontalDisplay"); //to do
			var optionsObject;
			var option;
			var submittedValue;
			var optionHtml = "";
			var optionTemplate;
			for(var i=0;i<qOptionsObjectArray.length;i++) {
				optionsObject = qOptionsObjectArray[i];
				option = optionsObject.option;
				submittedValue = optionsObject.submittedValue;
				optionTemplate = TemplateManager.getTemplate("multiSelectQuestionInput");

				optionHtml = optionHtml + optionTemplate({
					optionText  : option,
				    optionValue : submittedValue
				});
									
			}
			
			if(includeOther)
			{
				var otherTemplate = TemplateManager.getTemplate("multiSelectQuestionInputOther");
				optionHtml = optionHtml + otherTemplate({
					optionText  : 'Other, Please Specify',
				    optionValue : 'true'
				});
			}
			
			this.$(".multiSelectedQuestion").html(optionHtml);	
		}
	},
	
	/*showHideIncludeOther : function(model) {
		var includeOther = this.model.get("includeOther");

		if(includeOther != null ) {
			//determine show/hide
			if(includeOther) {
				var otherTemplate = TemplateManager.getTemplate("multiSelectQuestionInputOther");
				var includeOtherOption = otherTemplate({
					optionText  : 'Other, Please Specify',
				    optionValue : 'true'
				});
				var select = this.$(".multiSelectedQuestion");
				select.append(includeOtherOption);
				
				var includeOtherDiv = this.$(".multiSelectedQuestion").parent().find(".includeOtherDiv");
				$(includeOtherDiv).removeClass("hiddenDiv").addClass("showDiv");
				$(includeOtherDiv).addClass("clearfix");

			}else {
				var option = this.$(".multiSelectedQuestion").parent().find('option.otherspecify');
				option.remove();

				var includeOtherDiv = this.$(".multiSelectedQuestion").parent().find(".includeOtherDiv");
				$(includeOtherDiv).removeClass("showDiv").addClass("hiddenDiv");
			}
		}else {
			var option = this.$(".multiSelectedQuestion").parent().find('option.otherspecify');
			option.remove();
			//hide
			var includeOtherDiv = this.$(".multiSelectedQuestion").parent().find(".includeOtherDiv");
			$(includeOtherDiv).removeClass("showDiv").addClass("hiddenDiv");
		}
	}*/
});