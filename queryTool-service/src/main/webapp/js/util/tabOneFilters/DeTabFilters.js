/**
 * 
 */
QT.Filters.DeTabModelFilter = QT.Filters.ModelFilter.extend({
	runFilters : function() {
		var tabName = this.model.get("tabName");
		
		var collectionKeysLength = Object.keys(this.filters.collection).length;
		var tilesKeysLength = Object.keys(this.filters.tilesCollection).length;
		
		if (collectionKeysLength == 0 && tilesKeysLength == 0) {
			this.model.showAllTiles(tabName);
		}
		else if (collectionKeysLength == 0) {
			// this is an exception for data elements because a view list of 0 actually means 0
			if (tabName == Config.tabConfig.DataElementsTab.name) {
				this.model.hideAllSelectionItems(tabName);
				this.runTilesFilter(tabName);
			}
			else {
				this.model.showAllSelectionItems(tabName);
				this.runTilesFilter(tabName);
			}
		}
		else if (tilesKeysLength == 0) {
			this.model.showAllTiles(tabName);
			this.runSelectionFilter(tabName);
		}
		else {
			this.runSelectionFilter(tabName);
			this.runTilesFilter(tabName);
		}
	}
});