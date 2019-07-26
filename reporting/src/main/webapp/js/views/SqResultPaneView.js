/**
 * 
 */
QT.SqResultPaneView = QT.ResultPaneView.extend({
	
	render : function() {
		EventBus.on("loaded:savedQuery", this.applySqFilter, this);
		EventBus.on("clearDataCart", this.onClearDataCart, this);
		QT.SqResultPaneView.__super__.render.call(this);
	},
	
	populateTiles : function() {
		QT.SqResultPaneView.__super__.populateTiles.call(this);
		this.applySqFilter(undefined);
	},
	
	onClearDataCart : function() {
		this.hideAll();
	},
	
	/**
	 * Applies the visibility filter to the tiles.
	 * @param visibleTree - if Undefined, the filter hides all tiles, otherwise it contains saved query data
	 *  which is used to determine which tiles to display
	 */
	applySqFilter : function(visibleTree) {
		if (typeof visibleTree == "undefined") {
			// apply the filter that hides all tiles in this tab
			this.hideAll();
		}
		else {
			// translate the SQ format into filter format for the visible elements
			// query data is stored in visibleTree.  It has arrays "forms" and "studies"
			/*
			 * Forms has: {
			 * 	uri: "",
			 *  studyIds : [#, #, ...]
			 * }
			 */
			var forms = visibleTree.query.forms;
			/*
			 * Studies has: {
			 * 	uri: "",
			 *  id: #
			 * }
			 */
			var studies = visibleTree.query.studies;
			
			// loop over all studies and forms and get the models for each and combine into one array
			var models = [];
			for (var i = 0; i < forms.length; i++) {
				var uri = forms[i].uri;
				var model = QueryTool.page.get("forms").get(uri);
				if (model) {
					models.push(model);
				}
			}
			
			for (var j = 0; j < studies.length; j++) {
				var uri = studies[j].uri;
				var model = QueryTool.page.get("studies").get(uri);
				if (model) {
					models.push(model);
				}
			}
			
			this.showThese(models);
		}
	},
	
	hideAll : function() {
		this.model.filters.addFilter("tilesCollection",
				new QT.Filters.SqIncludedTilesFilter(this.model.tilesCollection, []));
	},
	
	showThese : function(models) {
		this.model.filters.addFilter("tilesCollection",
				new QT.Filters.SqIncludedTilesFilter(this.model.tilesCollection, models));
	}
});