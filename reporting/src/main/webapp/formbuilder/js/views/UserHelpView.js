/**
 * 
 */
var UserHelpView = BaseView.extend({
	className : "userHelpAddSection",
	rendered : false,
	
	initialize : function() {
// based on CRIT-828 - not showing this for now
//		this.listenTo(this.model.sections, 'add', this.modifySectionsList);
//		this.listenTo(this.model.sections, 'remove', this.modifySectionsList);
//		EventBus.on("close:activeEditor", this.modifySectionsList, this);		
//		UserHelpView.__super__.initialize.call(this);
//		this.template = TemplateManager.getTemplate("userHelpAddSection");
		
		return this;
	},
	
	render : function() {
// based on CRIT-828 - not showing this for now
//		this.rendered = true;
//		this.$el.html(this.template(this.model.attributes));
//		Config.containerDiv.find("#formContainerClearfix").before(this.$el);
//		this.hide();
//		_.defer(function() {
//			var view = FormBuilder.page.get("userHelpView");
//			view.modifySectionsList.call(view);
//		});
	},
	
	modifySectionsList : function() {
		if (this.model.sections.length > 0) {
			this.hide();
		}
		else {
			if (!this.rendered) this.render();
			this.show();
		}
	},
	
	hide : function() {
		this.$el.hide();
	},
	
	show : function() {
		var view = this;
		_.delay(function() {
			view.$el.show();
			view.$el.position({
				my: "right+40px top+20px",
				at: "center bottom",
				of: $("#addSectionButton")
			});
			
		}, 500);
	}
});