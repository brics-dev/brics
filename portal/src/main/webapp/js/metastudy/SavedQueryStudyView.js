/**
 * 
 */
SavedQueryViewEngine.SavedQueryStudyView  = BaseView.extend({
	className : "viewQuery_studyItem",
	events : {
		"click" : "studyClickHandler"
	},
	
	initialize : function() {
		SavedQueryViewEngine.SavedQueryStudyView.__super__.initialize.call(this);
		this.template = TemplateManager.getTemplate("study");
		
		this.listenTo(this.model, "change:active", this.afterChangeActive);
		EventBus.on("close:all", this.destroy, this);
		EventBus.on("destroy:all", this.destroy, this);
	},
	
	render : function($container) {
		this.$el.html(this.template(this.model.attributes));
		$container.append(this.$el);
		SavedQueryViewEngine.SavedQueryStudyView.__super__.render.call(this);
		
		this.determineFiltered();
	},
	
	studyClickHandler : function() {
		if (!this.model.get("active")) {
			EventBus.trigger("change:activeStudy", this.model);
		}
	},
	
	determineFiltered : function() {
		var $filterIcon = this.$(".viewQuery_studyFiltered");
		if (this.model.get("filtered")) {
			$filterIcon.show().addClass("viewQuery_filtered");
		}
		else {
			$filterIcon.hide().addClass("viewQuery_nonFiltered");
		}
	},
	
	/**
	 * Sets the class of this.$el to active or inactive, chosen by the
	 * model's "active" attribute
	 */
	afterChangeActive : function() {
		if (this.isActive()) {
			this.setActive();
			this.updateFormHeader();
			this.renderForms();
		}
		else {
			this.setInactive();
		}
	},
	
	updateFormHeader : function() {
		var $container = $(".viewQuery_formList");
		var $countContainer = $(".viewQuery_formCount");
		var numForms = this.model.get("formCount");
		if (numForms == 0) {
			$container.text("There are no forms in this saved query for this study");
			$countContainer.text("");
		}
		else {
			$container.html("");
			$countContainer.text("(" + numForms + ")");
		}
	},
	
	setActive : function() {
		this.$(".viewQuery_studyItemLink").addClass("viewQuery_active");
	},
	
	setInactive : function() {
		this.$(".viewQuery_studyItemLink").removeClass("viewQuery_active");
	},
	
	renderForms : function() {
		var forms = this.model.forms;
		// note, this container lives outside the study block, so can't use this.$()
		var $container = $(".viewQuery_formList");
		forms.forEach(function(form) {
			var formView = new SavedQueryViewEngine.SavedQueryFormView({model : form});
			formView.render($container);
		});
	},
	
	isActive : function() {
		return this.model.get("active");
	}
	
});