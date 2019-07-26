var QuestionAdditionalTextDecorator = {
		
		
		commonsName : "QuestionAdditionalTextDecorator",
		
		
		config : {
			name : "QuestionAdditionalTextDecorator",
			render : "renderAdditionalTextCommons"
		},
		
		
		renderAdditionalTextCommons : function(model) {
			
			var linkHtml = TemplateManager.getTemplate("editQuestionAdditionalTextLabel");
			var contentHtml = TemplateManager.getTemplate("editQuestionAdditionalTextTab");
			this.addTab(linkHtml, contentHtml);
			
		}
		
		
		
		
		
		
		
		
		
		
		
		
		
		
		
		
		
		
		
		
		
		
		
};