var QuestionBtrisMappingDecorator = {
	commonsName : "QuestionBtrisMappingDecorator",
	events : {
		"change #getBtrisValue" : "setIsGettingBtrisVale"
	},
	
	/**
	 * Renders this commons to the editor
	 * 
	 * @param model the question model
	 */
	renderBtrisMappingCommons : function(model) {
		
		var questionId = model.get("questionId");
		var questionInfoURL = baseUrl+"showQuestionBtrisMapping!showQuestionBtrisMapping.ajax?questionId=" + questionId;
		$.ajax({
			type:"get",
			url:questionInfoURL,
			async: false,
			success: function(response){
//				console.log("QuestionBtrisMappingDecorator.js ajax response: "+response);
				var btrisMapping = jQuery.parseJSON(response);

				model.set("hasBtrisMapping", btrisMapping.hasBtrisMapping);
				model.set("isGettingBtrisVal", btrisMapping.isGettingBtrisVal);
				model.set("btrisObservationName", btrisMapping.btrisObservationName);
				model.set("btrisRedCode", btrisMapping.btrisRedCode);
				model.set("btrisSpecimenType", btrisMapping.btrisSpecimenType);
			}
		});

		var hasBtrisMapping = model.get("hasBtrisMapping");
		if(hasBtrisMapping) {
			var linkHtml = TemplateManager.getTemplate("editQuestionBtrisMappingTabLabel");
			var contentHtml = TemplateManager.getTemplate("editQuestionBtrisMappingTab");
			this.addTab(linkHtml, contentHtml);
			this.showBtrisMappingInfo(model);
		}
	},
	
	config : {
		name : "QuestionBtrisMappingDecorator",
		render : "renderBtrisMappingCommons"
	},
	
	showBtrisMappingInfo : function(model) {
		var btrisObservationName = model.get("btrisObservationName");
		var btrisRedCode = model.get("btrisRedCode");
		var btrisSpecimenType = model.get("btrisSpecimenType");

		
//		var mainThis = FormBuilder.page.get("activeEditorView");
		if(model.get("isGettingBtrisVal")){
			this.$("#getBtrisValue").attr('checked', true);
		}
		this.$("#btrisObservationName").text(btrisObservationName);
		this.$("#btrisRedCode").text(btrisRedCode);
		this.$("#btrisSpecimenType").text(btrisSpecimenType);

	},
	
	setIsGettingBtrisVale : function() {
		var mainThis = FormBuilder.page.get("activeEditorView");
		if (mainThis.$("#getBtrisValue").is(':checked')) {
			this.model.set("isGettingBtrisVal", true);
		} else {
			this.model.set("isGettingBtrisVal", false);
		}
		
		
	}
};