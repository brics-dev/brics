/**
 * Filters provide a means to show/hide only necessary content (either selection list or
 * tiles list).
 * 
 * Input: array or objectMap<uri, X> of elements that should be visible
 * Output: objectMap<uri, Model> of the elements that should be visible 
 * 
 * The input list comes from either the server or the view
 * 
 * Process:
 * 	this.addFilter() - Filter is added to the model with either "tilesCollection" or "collection" as collectionName
 * 		this.runFilters() - filters both lists using the filters stored here
 *  		this.runSelectionFilter() - call each() in the filter and merges the results into the visible list
 *  		this.runTilesFilter() - call each() in the filter and merges the results into the visible list
 *  			filter.each() - merges the results of filter.check() into a single list
 *  				filter.check() - returns false if the item should be invisible, otherwies true
 * (deferred) updateSelectionListModels() - updates the item models to be visible or invisible
 */
QT.Filters = {};


/********************
 * 
 * The below is the object used within models to control filters on that model
 * 
 ********************/

	QT.Filters.ModelFilter = function(model){
		this.model = model;
		this.filters = {
			collection : {},
			tilesCollection : {}
		};
	};
	
	QT.Filters.ModelFilter.prototype.addFilter = function(collectionName, filterObj) {
		// only one of each type is allowed for each collection
		var currentFilter = this.filters[collectionName][filterObj.name];
		// equals checks for undefined
		if (!filterObj.equals(currentFilter)) {
			this.filters[collectionName][filterObj.name] = filterObj;
			this.runFilters();
		}
	};
	
	QT.Filters.ModelFilter.prototype.setFilter = function(collectionName, filterObj) {
		this.filters[collectionName][filterObj.name] = [];
		
		// only one of each type is allowed for each collection
		var currentFilter = this.filters[collectionName][filterObj.name];
		// equals checks for undefined
		if (!filterObj.equals(currentFilter)) {
			this.filters[collectionName][filterObj.name] = filterObj;
			this.runFilters();
		}
	};
		
	/**
	 * Removes one or more filters from the model
	 * 
	 * @param config array of objects (or single object) of form: 
	 * [
	 * 	{
	 * 		collectionName: "tilesCollection",
	 * 		filterName: "CheckboxSelectionFilter"
	 * 	},
	 * 	...
	 * }
	 */
	QT.Filters.ModelFilter.prototype.removeFilter = function(config) {
		if (!_.isArray(config)) {
			 config = [config];
		}
		
		for (var i = 0; i < config.length; i++) {
			var configElement = config[i];
			var collectionName = configElement.collectionName;
			var filterName = configElement.filterName;
			if (this.filters[collectionName][filterName]) {
				delete this.filters[collectionName][filterName];
			}
		}
		this.runFilters();
	};
		
	/**
	 * Applies all the current filters to the two collections
	 */
	QT.Filters.ModelFilter.prototype.runFilters = function() {
		var tabName = this.model.get("tabName");
		
		var collectionKeysLength = Object.keys(this.filters.collection).length;
		var tilesKeysLength = Object.keys(this.filters.tilesCollection).length;
		
		if (collectionKeysLength == 0 && tilesKeysLength == 0) {
			this.model.showAll(tabName);
			// because this doesn't go through the rest of the filter process
			EventBus.trigger("filter:listComplete", tabName);
		}
		else if (collectionKeysLength == 0) {
			// this is an exception for data elements because a view list of 0 actually means 0
			if (tabName == Config.tabConfig.DataElementsTab.name) {
				this.model.hideAllSelectionItems(tabName);
				// because this doesn't go through the rest of the filter process
				EventBus.trigger("filter:listComplete", tabName);
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
	};
	
	/**
	 * Runs filters on the selection list
	 * 
	 * @private
	 * @param tabName the tab name for this model
	 */
	QT.Filters.ModelFilter.prototype.runSelectionFilter = function(tabName) {
		// selection list can contain text filter and selection filter
		// tiles can contain text filter, selection filter, and unavailable filter
		// run selection list filters first
		var visibleOutput = {}
		if (this.filters.collection.TextSelectionFilter) {
			// the list of visible is stored inside the filter from its creation
			var visibleText = this.filters.collection.TextSelectionFilter.each();
			_.extend(visibleOutput, visibleText);
		}
		if (this.filters.collection.CheckboxSelectionFilter) {
			// the only way the selection list gets a checkbox selection filter is from the tiles
			var visibleSelection = this.filters.collection.CheckboxSelectionFilter.each();
			_.extend(visibleOutput, visibleSelection);
		}
		if (this.filters.collection.DeHideShowFilter) {
			// the list of visible is stored inside the filter from its creation
			var visibleText = this.filters.collection.DeHideShowFilter.each();
			_.extend(visibleOutput, visibleText);
		}
		this.updateSelectionListModels(visibleOutput, tabName);
	};
	
	/**
	 * Runs filters on the tiles list
	 * 
	 * @private
	 * @param tabName the tab name for this model
	 */
	QT.Filters.ModelFilter.prototype.runTilesFilter = function(tabName) {
		// tiles can contain text filter, selection filter, and unavailable filter
		var visibleOutput = {};
		var numberOfFilter = 0;
		
		if (this.filters.tilesCollection.TextSelectionFilter) {
			// the list of visible is stored inside the filter from its creation
			var visibleText = this.filters.tilesCollection.TextSelectionFilter.each();
			_.extend(visibleOutput, visibleText);
			
			numberOfFilter++;

		}
		if (this.filters.tilesCollection.CheckboxSelectionFilter) {
			// I don't think I need this because the selected list is already the list of visible
			var selected = this.model.collection.allSelected(tabName);
			var visibleSelection = this.filters.tilesCollection.CheckboxSelectionFilter.each(selected);	
			_.extend(visibleOutput, visibleSelection);
			
			numberOfFilter++;
			
		}
		if (this.filters.tilesCollection.DeCheckboxSelectionFilter) {
			var selected = this.model.collection.allSelected(tabName);
			var visibleSelection = this.filters.tilesCollection.DeCheckboxSelectionFilter.each(selected);		
			_.extend(visibleOutput, visibleSelection);
			
			numberOfFilter++;
			
		}
		if (this.filters.tilesCollection.SqIncludedTilesFilter) {
			var visibleTiles = this.filters.tilesCollection.SqIncludedTilesFilter.each();		
			_.extend(visibleOutput, visibleTiles);
			
			numberOfFilter++;

		}
		if (this.filters.tilesCollection.UnavailableFilter) {
			// the unavailable filter doesn't use a visible list but rather the list itself
			var visibleAvailable = this.filters.tilesCollection.UnavailableFilter.each();
			
			if(numberOfFilter !=0)
				visibleOutput=this.logicalAndMerge(visibleAvailable,visibleOutput);
			else
				visibleOutput = visibleAvailable;
		}
		this.updateTileModels(visibleOutput, tabName);
	};
	
	/**
	 * Updates the models within this selection list to show/hide them
	 * 
	 * @param visibleOutput the list of VISIBLE models
	 * @param tabName the tab name for the parent model
	 */
	QT.Filters.ModelFilter.prototype.updateSelectionListModels = function(visibleOutput, tabName) {
		// hide all selection items, show the ones in visibleOutput
		viewModel = this.model;
		viewModel.hideAllSelectionItems(tabName);
		
		var asArray = _.values(visibleOutput);
		var length = asArray.length;
		var model = {};
		for (var i = 0; i < length; i++) {
			model = asArray[i];
			model.setSelectionListVisible(tabName, true);
		}
		EventBus.trigger("filter:listComplete", tabName);
	};
	
	/**
	 * Updates the models within this tiles list to show/hide them
	 * 
	 * @param visibleOutput the list of VISIBLE models
	 * @param tabName the tab name for the parent model
	 */
	QT.Filters.ModelFilter.prototype.updateTileModels = function(visibleOutput, tabName) {
		// hide all selection items, show the ones in visibleOutput
		_.defer(function(viewModel, visibleOutput, tabName) {
			viewModel.hideAllTiles(tabName);
			
			var asArray = _.values(visibleOutput);
			var length = asArray.length;
			var model = {};
			for (var i = 0; i < length; i++) {
				model = asArray[i];
				model.setTileVisible(tabName, true);
			}
			EventBus.trigger("filter:listComplete", tabName);
		}, this.model, visibleOutput, tabName);
	};
	
	/**
	 * Performs a logical AND merge between two sets of model maps (@see QT.Filter.arrToObj())
	 * to find the keys that are common to both lists.  Useful for performing filters where
	 * the results should be an intersection of the results of two filters instead of a union.
	 * 
	 * For performance sake, the list in listOne should be the smaller of the two
	 * 
	 * @param listOne 
	 * @param listTwo
	 * @returns {uri: model} map of the intersection between the two lists
	 */
	QT.Filters.ModelFilter.prototype.logicalAndMerge = function(listOne, listTwo) {
		var output = {};
		var urisOne = _.keys(listOne);
		var length = urisOne.length;
		for (var i = 0; i < length; i++) {
			var uri = urisOne[i];
			if (typeof listTwo[uri] !== "undefined") {
				output[uri] = listTwo[uri];
			}
		}
		return output;
	}

QT.Filters.ModelFilter.extend = Backbone.History.extend;