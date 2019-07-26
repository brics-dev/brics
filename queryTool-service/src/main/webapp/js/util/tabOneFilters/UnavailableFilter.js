/**
 * Used by the result pane view to determine whether to show or hide tiles
 */
QT.Filters.UnavailableFilter = QT.Filter.extend({
	name : "UnavailableFilter",
	
	subElementListName : null,
	
	init : function(view, collection, listOfVisible) {
		this.subElementListName = view.config.tiles.subElementList;
		QT.Filters.UnavailableFilter.__super__.init.call(this, collection, listOfVisible);
	},
	
	/**
	 * Show only tiles with sub elements.
	 * IE: return true if sub element list length > 0, else false
	 */
	check : function(model, fullObjMap, visibleMap) {
		var isAvailable = QueryTool.page.isAvailable(model.get("uri"));
		return isAvailable && model[this.subElementListName].length > 0;
	}
});