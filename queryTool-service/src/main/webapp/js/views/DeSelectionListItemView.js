/**
 * extends SelectionListItemView in order to render correctly for data elements
 * which are handled differently from other list items
 */
QT.DeSelectionListItemView = QT.SelectionListItemView.extend({
	className : "selectionListItem deSelectionListItem",
	/**
	 * this is used by the render method to say that the element should be displayed.
	 * We want them all displayed here unless the count of forms is 0
	 */
	
	render : function($container, tabName) {
		this.$el.html(this.template(this.model.attributes));
		$container.append(this.$el);
			
		this.updateTitle();
		this.listenTo(this.model, "change:isVisibleSelectionListDataElementsTab", this.updateVisible);
		this.listenTo(this.model, "change:isSelectedSelectionListDataElementsTab", this.updateSelected);
		this.listenTo(this.model, "change:title", this.updateTitle);
	},
	
	hasMappedElements : function() {
		return this.model.get("formsCount") > 0;
	},
	
	updateTitle : function() {
		var output = this.model.get("title");
		var filteredCollectionCount = this.model.get("formsCount");
		output = output + " (" + filteredCollectionCount + ")"; 
		this.$(".selectionListTitleContainer").html(output);
	}
});