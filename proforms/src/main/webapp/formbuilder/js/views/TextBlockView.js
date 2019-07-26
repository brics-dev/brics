/**
 * 
 */
var TextBlockView  = QuestionView.extend({
	className : "question textblock formGrid-1",
	
	initialize : function() {
		TextBlockView.__super__.initialize.call(this);
		this.template = TemplateManager.getTemplate("textBlockTemplate");
		
		this.listenTo(this.model, "change:htmlText", this.afterChangeText);
		
		if (this.model.get("questionName") == "" && 
				(!this.model.isRepeated() || !this.model.isRepeatedChild())) {
			this.model.set("questionName", FormBuilder.form.get("formid") + "_" + String(new Date().getTime()));
		}
	},
	
	render : function($after) {
		TextBlockView.__super__.render.call(this, $after);
		this.afterChangeText(this.model);
		this.assignHeaderClass();
	},
	
	
	assignHeaderClass : function() {
		var tableHeaderType = this.model.get("tableHeaderType");
		if(tableHeaderType == Config.tableHeaderTypes.rowHeader) {
			this.$el.addClass(Config.tableHeaderClassNames.rowHeader);
		}else if(tableHeaderType == Config.tableHeaderTypes.columnHeader) {
			this.$el.addClass(Config.tableHeaderClassNames.columnHeader);
		}else if(tableHeaderType == Config.tableHeaderTypes.tableHeader) {
			this.$el.addClass(Config.tableHeaderClassNames.tableHeader);
		}
	},
	
	/**
	 * This responds to changes in the text field in the model.  It is a bit
	 * of a workaround to get past the modelbinding BUT we have to do that
	 * because we don't want to display the TEXT, we want to render the HTML.
	 * 
	 * @param model the model that was just changed
	 */
	afterChangeText : function(model) {
		var target = this.$el.find(".textblockContent");
		var html = model.get("htmlText");
		target.html(html);
	}
});