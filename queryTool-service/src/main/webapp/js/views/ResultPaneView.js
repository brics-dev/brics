/**
 * model: SelectionList built for a tab
 * el: Config.tabConfig.[tabName].tiles.container
 */
QT.ResultPaneView = BaseView.extend({
	config : null,
	templateName : "filterDataResultPane",
	notAvailableHidden : false,
	tileSizeSet : false,
	tileViews : [],
	
	events : {
		"click .resultPaneSearchButton" : "resultPaneTextSearch",
		"click .resultPaneSearchReset" : "resultPaneResetFilter",
		"keydown .resultPaneSearchText" : "resultPaneTextSearchKey",
		"click .numberSelectedPill .closeButton" : "clearSelection",
		"click .textFilterPill .closeButton" : "clearTextFilter",
		"click .hideNotAvailable" : "hideNotAvailable",
		"click .expandAll" : "expandAll",
		"click .collapseAll" : "collapseAll"
	},
	
	initialize : function() {
		this.config = Config.tabConfig[this.model.get("tabName")];
		if (this.config.name == "DataElementsTab") {
			this.template = TemplateManager.getTemplate("dataElementFilterDataResultPane");
		}
		else {
			this.template = TemplateManager.getTemplate(this.templateName);
		}

		EventBus.on("clearDataCart", this.collapseAll, this);
		EventBus.on("window:resize", this.onWindowResize, this);
		EventBus.on("select:selectionTab", this.onTabSelect, this);
		
		QT.ResultPaneView.__super__.initialize.call(this);
	},
	
	render : function() {
		this.$el.html(this.template(this.model.attributes));
		
		var container = this.config.tiles.container;
		this.populateTiles();
		this.updateCount(container, this.model);
		this.updateLabels();
		
		this.onWindowResize();
		
		this.listenTo(this.model, "change:visibleTiles", this.updateCount);
		
		QT.ResultPaneView.__super__.render.call(this);
	},
	
	/**
	 * General rendering function for filter data tiles (the items on the right hand side).
	 * It creates a new FilterDataTileView - the view controlling each tile
	 * then renders it within the associated container.
	 * 
	 * @param collection if a collection is passed in here, it is used instead of this.model.tilesCollection
	 */
	populateTiles : function(collection) {
		var tabName = this.model.get("tabName");
		if (typeof collection == "undefined") {
			collection = this.model.tilesCollection;
		}
		var length = collection.length;
		for (var i = 0; i < length; i++) {
			var singleModel = collection.models[i];
			var view = new QT.FilterDataTileView({model : singleModel}).build(tabName);
			view.render(this.$(".resultPaneContent"));
			this.tileViews.push(view);
		}
		this.resizeTiles();
	},
	
	destroyAllTiles : function() {
		var length = this.tileViews.length;
		for(var i = 0; i < length; i++) {
			this.tileViews[i].destroy();
		}
		this.tileViews = [];
	},
	
	onWindowResize : function() {
		this.resizeResultsList(this.$(".resultPaneContent"));
	},
	
	onTabSelect : function() {
		this.onWindowResize();
		this.resizeTiles();
	},
	
	resizeResultsList : function($listContainer) {
		// sometimes the listContainer doesn't exist yet
		if ($listContainer.length > 0) {
			var windowHeight = window.innerHeight;
			var currentTop = $listContainer.offset().top;
			var bottomPadding =  Config.scrollContainerBottomOffset;
			$listContainer.height(windowHeight - currentTop - bottomPadding);
		}
	},
	
	resizeTiles : function() {
		if (!this.tileSizeSet) {
			var height = 0;
			// find common max height of all tiles
			this.$(".resultPaneContent").find(".filterDataTile").each(function() {
			  var thisHeight = $(this).height();
			  height = (height < thisHeight) ? thisHeight : height;
			});
	
			this.$(".resultPaneContent").find(".filterDataTile").css("min-height", height + 1);
			this.tileSizeSet = true;
		}
	},
	
	updateCount : function() {
		var tabName = this.model.get("tabName");
		this.model.updateVisibleTilesCount(tabName);
		var visibleTiles = this.model.get("visibleTiles");
		if (visibleTiles > 0) {
			this.$(".resultPaneListEmpty").hide();
		}
		else {
			this.$(".resultPaneListEmpty").show();
		}
		
		var text = "Results: (" + visibleTiles + " " + this.config.tiles.filterTypeText + ")";
		this.$(".resultPaneCount").text(text);
	},
	
	updateLabels : function() {
		this.$(".resultPaneSearchText").attr("placeholder", "Search " + this.config.tiles.filterTypeText);
		this.$(".resultPaneLi").html(this.config.tiles.filterTypeText);
	},
	
	resultPaneTextSearchKey : function(e) {
		if (e.which == 13) {
			this.resultPaneTextSearch();
		}
	},
	
	resultPaneTextSearch : function() {
		var text = this.$(".resultPaneSearchText").val();
		var tabName = this.model.get("tabName");
		var view = this;
		$.ajax({
			url : Config.tabConfig[tabName].tiles.textSearchUrl,
			cache : false,
			data : {textValue: text},
			dataType : "json",
			success : function(data) {
				var searchList = view.model.tilesCollection;
				// get model version of each element and store in new list
				var models = [];
				for (var i = 0; i < data.length; i++) {
					models.push(searchList.get(data[i].uri));
				}
				// use the model list to run the filter
				// on all tabs, the text list is the tiles list
				view.model.filters.addFilter("tilesCollection", 
						new QT.Filters.TextSelectionFilter(view.model.tilesCollection, models));
				view.showTextPill(text);
			},
			error : function(data) {
				if ( window.console && console.log ){
				console.log("error: " + data);
				}
			}
		});
	},
	
	resultPaneResetFilter : function() {
		this.$(".resultPaneSearchText").val("");
		//this.model.showAllTiles(this.model.get("tabName"));
		this.clearTextFilter();
		if (this.notAvailableHidden) {
			this.showNotAvailable();
		}
		this.updateCount();
	},
	
	/**
	 * Clears all text search filters to reset the list
	 * Called from the filter text pill
	 * 
	 * Unhides all this.model.collection
	 */
	clearTextFilter : function() {
		var tabName = this.model.get("tabName");
		this.model.filters.removeFilter({
			collectionName: "tilesCollection", 
			filterName: "TextSelectionFilter"
		});
		this.$(".resultPaneSearchText").val("");
		this.hideTextPill();
	},
	
	hideNotAvailable : function() {
		// note: unavailable does not interfere with other filters
		var config = this.config;
		var tabName = this.model.get("tabName");
		if (this.notAvailableHidden) {
			this.notAvailableHidden = false;
			// removes the filter
			this.showNotAvailable();
		}
		else {
			this.notAvailableHidden = true;
			this.$(".hideNotAvailable").text("Show All");
			
			// the filter here doesn't need a visible models list - it checks in check()
			this.model.filters.addFilter("tilesCollection", 
					new QT.Filters.UnavailableFilter(this, this.model.tilesCollection, null));
		}
		this.updateCount();
	},
	
	showNotAvailable : function() {
		this.$(".hideNotAvailable").text("Hide Not Available");
		this.model.filters.removeFilter({
			collectionName: "tilesCollection", 
			filterName: "UnavailableFilter"
		});
	},
	
	expandAll : function() {
		EventBus.trigger("tiles:expand", this.model.get("tabName"));
	},
	
	collapseAll : function() {
		EventBus.trigger("tiles:collapse", this.model.get("tabName"));
	},
	
	showTextPill : function(textFilter) {
		if (textFilter.length >= 22) {
			textFilter = textFilter.slice(0, 25) + "...";
		}
		var text = "Text: \"" + textFilter + "\"";
		this.$(".textFilterPill .pillContent").text(text);
		this.$(".textFilterPill").show();
		this.$(".currentlyFiltering").show();
		this.onWindowResize();
	},
	
	hideTextPill : function() {
		this.$(".textFilterPill").hide();
		this.$(".selectionSearchText").val("");
		if (!this.$(".selectionListPill").is(":visible")) {
			this.$(".currentlyFiltering").hide();
			this.onWindowResize();
		}
	}
});