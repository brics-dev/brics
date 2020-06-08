/**
 * 
 */
QT.SelectionList = BaseModel.extend({
	collection : null,
	tilesCollection : null,
	filters : null,

	defaults : {
		tabId : "studies",
		tabName : "",
		visibleTiles : 0
	},
	
	
	initialize : function() {
		// I use build() to initialize here because it needs the tabId
	},
	
	build : function(tabId) {
		var config = Config.tabConfig[tabId];
		this.collection = QueryTool.page.get(config.selectionList.pageList);
		this.tilesCollection = QueryTool.page.get(config.tiles.pageList);
		this.set("tabName", config.name);
		if (config.name == "DataElementsTab") {
			this.filters = new QT.Filters.DeTabModelFilter(this);
		}
		else if (config.name == "DefinedQueriesTab") {
			this.filters = new QT.Filters.SqTabModelFilter(this);
		}
		else {
			this.filters = new QT.Filters.ModelFilter(this);
		}
		
		EventBus.on("filter:listComplete", this.updateVisibleTilesCount, this);
		
		return this;
	},
	
	showAll : function(tabName) {
		this.showAllTiles(tabName);
		this.showAllSelectionItems(tabName);
	},
	
	hideAll : function(tabName) {
		this.hideAllTiles(tabName);
		this.hideAllSelectionItems(tabName);
	},
	
	showAllTiles : function(tabName) {
		var hiddenTiles = this.tilesCollection.allHiddenTiles(tabName);
		hiddenTiles.forEach(function(item) {
			item.setTileVisible(tabName, true);
		});
	},
	
	showAllSelectionItems : function(tabName) {
		var hiddenSelectable = this.collection.allHidden(tabName);
		hiddenSelectable.forEach(function(item) {
			item.setSelectionListVisible(tabName, true);
		});
	},
	
	hideAllTiles : function(tabName) {
		this.tilesCollection.forEach(function(tile) {
			tile.setTileVisible(tabName, false);
		});
	},
	
	hideAllSelectionItems : function(tabName) {
		var attribute = this.collection.at(0).getSelectionListVisibleVariable(tabName);
		var filterObject = {};
		filterObject[attribute] = true;
		var visible = this.collection.where(filterObject);
		visible.forEach(function(selectionItem) {
			selectionItem.setSelectionListVisible(tabName, false);
		});
	},
	
	updateVisibleTilesCount : function(tabName) {
		var visibleTilesCount = this.tilesCollection.allVisibleTiles(tabName).length;
		this.set("visibleTiles", visibleTilesCount);
	}
});