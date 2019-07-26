/**
 * 
 */
QT.Filters.SqTabModelFilter = QT.Filters.ModelFilter.extend({
	runFilters : function() {
		var tabName = this.model.get("tabName");
		var visibleOutput = {}
		if (this.filters.tilesCollection.SqIncludedTilesFilter) {
			var visibleTiles = this.filters.tilesCollection.SqIncludedTilesFilter.each();
			if (_.keys(visibleOutput).length == 0) {
				visibleOutput = visibleTiles;
			}
			else {
				visibleOutput = this.logicalAndMerge(visibleOutput, visibleTiles);
				//$.extend(visibleOutput, visibleTiles);
			}
		}
		if (this.filters.tilesCollection.TextSelectionFilter) {
			// the list of visible is stored inside the filter from its creation
			var visibleText = this.filters.tilesCollection.TextSelectionFilter.each();
			if (_.keys(visibleOutput).length == 0) {
				visibleOutput = visibleTiles;
			}
			else {
				visibleOutput = this.logicalAndMerge(visibleOutput, visibleText);
				//$.extend(visibleOutput, visibleTiles);
			}
		}
		if (this.filters.tilesCollection.UnavailableFilter) {
			// the unavailable filter doesn't use a visible list but rather the list itself
			var visibleAvailable = this.filters.tilesCollection.UnavailableFilter.each();
			visibleOutput = this.logicalAndMerge(visibleOutput, visibleAvailable);
		}
		this.updateTileModels(visibleOutput, tabName);
	},
	
	hideAllSqTiles : function() {
		QueryTool.page.get("forms").forEach(function(tile) {
			tile.setTileVisible("DefinedQueriesTab", false);
		});
		QueryTool.page.get("studies").forEach(function(tile) {
			tile.setTileVisible("DefinedQueriesTab", false);
		});
	}
});