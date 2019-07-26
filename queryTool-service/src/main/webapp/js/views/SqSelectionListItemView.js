/**
 * extends SelectionListItemView in order to render correctly for saved queries
 * which are handled differently from other list items
 */
QT.SqSelectionListItemView = QT.SelectionListItemView.extend({
	className : "selectionListItem sqSelectionListItem",
	
	events : {
		"click .selectionListRadio" : "loadSavedQuery",
		"click .selectionListTitleContainer" : "viewSavedQuery"
	},
	
	/**
	 * this is used by the render method to say that the element should be displayed.
	 * We want them all displayed here unless the count of forms is 0
	 */
	
	initialize : function() {
		this.template = TemplateManager.getTemplate("savedQuerySelectionListItem");
	},
	
	render : function(tabName) {
		var $container = $("#sqSelectionList");
		this.$el.html(this.template(this.model.attributes));
		
		var index = QueryTool.page.get("definedQueries").indexOf(this.model);
		var $itemList = $container.find(".selectionListItem");
		
		if ($itemList.length > 0) {
			if (index == 0) {
				var $first = $itemList.eq(0);
				$first.before(this.$el);
			} else {
				var $before = $itemList.eq(index - 1);
				$before.after(this.$el);
			}
		}
		else {
			$container.append(this.$el);
		}
			
		this.updateTitle();
		this.listenTo(this.model, "change:name", this.updateTitle);
		this.listenTo(this.model, "remove", this.destroy);
	},
	
	hasMappedElements : function() {
		return true;
	},
	
	updateTitle : function() {
		var output = this.model.get("name");
		this.$(".selectionListTitleContainer").html(output);
	},
	
	loadSavedQuery : function(event) {
		var $target = $(event.target);
		// value has the saved query ID to retrieve
		EventBus.trigger("load:savedQuery", $target.val());
	},
	
	viewSavedQuery : function(event) {
		var $target = $(event.target);
		// value has the saved query ID to retrieve
		EventBus.trigger("view:savedQuery", $target.attr("value"));
	}
});