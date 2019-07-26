/**
 * 
 */
// TODO: complete!
var TextQuestionView  = QuestionView.extend({
	templateName : "textQuestionTemplate",
	
	events : {
		
	},
	
	initialize : function() {
		this.id = this.model.get("newQuestionDivId");
		TextQuestionView.__super__.initialize.call(this);
		this.template = TemplateManager.getTemplate(this.templateName);
		this.listenTo(this.model, "change:active", this.afterChangeActive);
		return this;
	},
	
	render : function($section, $after) {
		this.$el.attr("id", this.model.get("divId"));
		this.$el.html(this.template(this.model.attributes));
		this.afterChangeActive();
		if (!this.$el.is(":visible")) {
			if (typeof $after !== "undefined" && $after.length > 0) {
				$after.after(this.$el);
			}
			else {
				$section.append(this.$el);
			}
		}
		this.setupTooltips();
		TextQuestionView.__super__.render.call(this);
		// if adding a new question, event to tell the rest of the page about
		// this new activequestion is in sectionView when the question
		// is actually added
		return this;
	},
	
	setupTooltips : function() {
		
	},
	
	isActive : function() {
		return this.model.get("active");
	},
	
	/**
	 * Sets the class of this.$el to active or inactive, chosen by the
	 * model's "active" attribute
	 */
	afterChangeActive : function() {
		if (this.isActive()) {
			this.setActive();
		}
		else {
			this.setInactive();
		}
	},
	
	/**
	 * Sets this section to active - including showing edit
	 * and delete buttons
	 */
	setActive : function() {
		this.$el.removeClass(Config.styles.inactive);
		this.$el.addClass(Config.styles.active);
		this.$(".editButton").show();
		this.$(".deleteButton").show();
	},
	
	/**
	 * Sets this section to inactive - including hiding edit
	 * and delete buttons.
	 */
	setInactive : function() {
		this.$el.removeClass(Config.styles.active);
		this.$el.addClass(Config.styles.inactive);
		this.$(".editButton").hide();
		this.$(".deleteButton").hide();
	}
	
	
});