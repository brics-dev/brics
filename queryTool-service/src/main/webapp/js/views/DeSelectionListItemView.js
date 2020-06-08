/**
 * extends SelectionListItemView in order to render correctly for data elements
 * which are handled differently from other list items
 */
QT.DeSelectionListItemView = QT.SelectionListItemView.extend({
	className : "selectionListItem deSelectionListItem",
	
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