var QuestionAdditionalTextDecorator = {
		
		
		commonsName : "QuestionAdditionalTextDecorator",
		
		
		config : {
			name : "QuestionAdditionalTextDecorator",
			render : "renderAdditionalTextCommons"
		},
		
		events : {
			"click #descriptionUp" : "formatDescriptionUp",
			"click #descriptionDown" : "formatDescriptionDown"
		},
		
		
		renderAdditionalTextCommons : function(model) {
			
			var linkHtml = TemplateManager.getTemplate("editQuestionAdditionalTextLabel");
			var contentHtml = TemplateManager.getTemplate("editQuestionAdditionalTextTab");
			this.addTab(linkHtml, contentHtml);
			
		},
		
		formatDescriptionUp : function() {
			var thisView = this;
			var qmodel = thisView.model;
			
			var questionTextEditView = FormBuilder.pageView.model.get("questionTextEditView");
			questionTextEditView.render(qmodel,"descriptionUp");
		},
		
		formatDescriptionDown : function() {
			var thisView = this;
			var qmodel = thisView.model;
			
			var questionTextEditView = FormBuilder.pageView.model.get("questionTextEditView");
			questionTextEditView.render(qmodel,"descriptionDown");
		}
		
		
		
		
		
		
		
		
		
		
		
		
		
		
		
		
		
		
		
		
		
		
		
};