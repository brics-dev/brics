/**
 * 
 */
var SelectView  = QuestionView.extend({
	className : "question formGrid-1",
	typeClassName : "select",
	
	initialize : function() {
		SelectView.__super__.initialize.call(this);
		this.template = TemplateManager.getTemplate("selectQuestionTemplate");
		this.listenTo(this.model, "change:questionOptionsObjectArray", this.afterChangeOptionsObjectArray);
		this.listenTo(this.model, "change:displayPV", this.afterChangeOptionsObjectArray);
		this.listenTo(this.model, "change:includeOther", this.showHideIncludeOther);
		this.listenTo(this.model, "change:defaultValue", this.onChangeDefaultValue);
	},
	
	render : function($after) {
		SelectView.__super__.render.call(this, $after);
		//this.afterChangeOptions(this.model);
		this.afterChangeOptionsObjectArray();
		this.showHideIncludeOther(this.model);
		this.onChangeDefaultValue();

	},
	
	
	afterChangeOptionsObjectArray : function() {	
		this.$(".selectedQuestion").empty();
	
		var qOptionsObjectArray = this.model.get("questionOptionsObjectArray");
		var includeOther = this.model.get("includeOther");

		if(qOptionsObjectArray.length > 0) {
			
			//var horizontalDisplay = model.get("horizontalDisplay"); //to do
			var displayPV = this.model.get("displayPV");
			var optionsObject;
			var option;
			var submittedValue;
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
				
				submittedValue = optionsObject.submittedValue;
				optionTemplate = TemplateManager.getTemplate("selectQuestionInput");

				optionHtml = optionHtml + optionTemplate({
					optionText  : option,
				    optionValue : submittedValue,
					tooltip     : tooltipText
				});					
			}
			if(includeOther)
			{
				var otherTemplate = TemplateManager.getTemplate("selectQuestionInputOther");
				optionHtml = optionHtml + otherTemplate({
					optionText  : 'Other, Please Specify',
				    optionValue : 'true'
				});
			}
			
			this.$(".selectedQuestion").html(optionHtml);	
		}
	},
	
	
	
	showHideIncludeOther : function() {
		var includeOther = this.model.get("includeOther");
		if(includeOther != null ) {
			//determine show/hide
			if(includeOther) {
				var otherTemplate = TemplateManager.getTemplate("selectQuestionInputOther");
				var includeOtherOption = otherTemplate({
					optionText  : 'Other, Please Specify',
				    optionValue : 'true'
				});
				var select = this.$(".selectedQuestion");
				select.append(includeOtherOption);
				
				var includeOtherDiv = this.$(".selectedQuestion").parent().find(".includeOtherDiv");
				$(includeOtherDiv).removeClass("hiddenDiv").addClass("showDiv");
			    $(includeOtherDiv).addClass("clearfix");
			}else {
				var option = this.$(".selectedQuestion").parent().find('option.otherspecify');
				option.remove();

				var includeOtherDiv = this.$(".selectedQuestion").parent().find(".includeOtherDiv");
				$(includeOtherDiv).removeClass("showDiv").addClass("hiddenDiv");
			}
		}else {
			var option = this.$(".selectedQuestion").parent().find('option.otherspecify');
			option.remove();
			//hide
			var includeOtherDiv = this.$(".selectedQuestion").parent().find(".includeOtherDiv");
			$(includeOtherDiv).removeClass("showDiv").addClass("hiddenDiv");
		}
	},
	
	onChangeDefaultValue:function(){
		var defaultVal = this.model.get("defaultValue");
		if(defaultVal != "") {
			var $input = this.$("select");
			$input.val(this.model.get("defaultValue"));
		}
	}
});