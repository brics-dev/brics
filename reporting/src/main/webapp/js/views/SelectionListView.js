/**
 * 
 */
QT.SelectionListView = BaseView.extend({
	className : "selectionList",
	config : null,
	tabName : "",
	
	events : {
		"click .resetFilter" : "clearSelection",	// selection list clear
		"click .expandCollapseButton" : "expandCollapse",
		"click .selectionSearchButton" : "filterDataTextSearch",
		"click .selectionSearchReset" : "filterDataResetFilter", // reset all
		"keydown .selectionSearchText" : "filterDataTextSearchKey",
		"click .numberSelectedPill .closeButton" : "clearSelection",
		"click .textFilterPill .closeButton" : "clearTextFilter"
	},
	
	initialize : function() {
		this.config = Config.tabConfig[this.model.get("tabName")];
		if (this.config.name == "DataElementsTab") {
			this.template = TemplateManager.getTemplate("dataElementSelectionList");
		}
		else if (this.config.name == "DefinedQueriesTab") {
			this.template = TemplateManager.getTemplate("savedQuerySelectionList");
		}
		else {
			this.template = TemplateManager.getTemplate("selectionList");
		}
		EventBus.on("window:resize", this.onWindowResize, this);
		EventBus.on("select:selectionTab", this.onWindowResize, this);
		
		QT.SelectionListView.__super__.initialize.call(this);
	},
	
	render : function() {
		this.tabName = this.model.get("tabName");

		// redefining to local scope for performance and for sub-context handling
		var tabName = this.tabName;
		EventBus.on("selectionChange:" + this.tabName, this.selectionListUpdate, this);
		
		// render the base template for this area
		this.$el.html(this.template(this.model.attributes));
		
		var $selectionItemsContainer = this.$(".selectionList");
		var collection = this.model.collection;
		var endingNumber = collection.length;
		
		this.renderSelectionTabText();
		this.resizeSelectionList($selectionItemsContainer);
		
		// loop over this.model.collection which is one of SelectionDataElements, SelectionForms, etc
		//if (endingNumber > 0) {
			var j = 0;
			setTimeout(function loopFunction() {
			    try {
			    	var model = collection.at(j);
			    	var view = null;
			    	// special case, unfortunately
			    	if (model instanceof QT.DataElement) {
			    		view = new QT.DeSelectionListItemView({model: model});
			    	}
			    	else if (model instanceof QT.DefinedQuery) {
			    		view = new QT.SqSelectionListItemView({model: model});
			    	}
			    	else {
			    		view = new QT.SelectionListItemView({model: model});
			    	}
			        view.render($selectionItemsContainer, tabName);
			    }
			    catch(e) {
			        // handle any exception
			    	//Log.developer.error("failed to render an item view");
			    	console.error("failed to render an item view");
			    }
			     
			    j++;
			    if (j < endingNumber) {
			        setTimeout(loopFunction, 0); // timeout loop
			    }
			    else {
			        // any finalizing code
			    }
			});
		//}
		
		this.model.updateVisibleTilesCount(this.tabName);
		
		QT.SelectionListView.__super__.render.call(this);
	},
	
	onWindowResize : function() {
		this.resizeSelectionList(this.$(".selectionList"));
	},
	
	renderSelectionTabText : function() {
		var config = Config.tabConfig[this.tabName];
		this.$(".currentlyFilteringText").text(config.selectionList.currentlyFilteringText);
		this.$(".filterText").text(config.selectionList.filterText);
		this.$(".filteringType").text(config.selectionList.filterTypeText);
		this.$(".selectionSearchText").attr("placeholder", "Search " + config.selectionList.filterTypeText);
	},
	
	resizeSelectionList : function($selectionItemsContainer) {
		var windowHeight = window.innerHeight;
		var currentTop = $selectionItemsContainer.offset().top;
		var $parentContainer = $selectionItemsContainer.parents(".selectionListPanel").eq(0);
		var bottomPadding = Number($parentContainer.css("padding-bottom").replace("px", ""));
		var extraPadding = Config.scrollContainerBottomOffset;
		$selectionItemsContainer.height(windowHeight - currentTop - bottomPadding - extraPadding);
	},
	
	filterDataTextSearchKey : function(e) {
		if (e.which == 13) {
			this.filterDataTextSearch();
		}
	},
	
	filterDataTextSearch : function() {
		var text = this.$(".selectionSearchText").val();
		var view = this;
		$.ajax({
			url : Config.tabConfig[this.tabName].selectionList.textSearchUrl,
			cache : false,
			data : {textValue: text},
			dataType : "json",
			success : function(data) {
				var searchList = view.model.collection;
				// get model version of each element and store in new list
				var models = [];
				for (var i = 0; i < data.length; i++) {
					models.push(searchList.get(data[i].uri));
				}
				// use the model list to run the filter
				// on all tabs, the text list is the tiles list
				view.model.filters.addFilter("collection", 
						new QT.Filters.TextSelectionFilter(view.model.collection, models));
				view.showTextPill(text);
			},
			error : function() {
				if ( window.console && console.log ){
				console.log("error: " + data);
				}
			}
		});
	},
	
	/**
	 * 
	 */
	filterDataResetFilter : function() {
		this.clearAllFilters();
		
		this.$(".selectionSearchText").val("");
		// can't show all tiles any more since there are two reset buttons
		this.model.showAllSelectionItems(this.tabName);
	},
	
	
	
	showTextPill : function(textFilter) {
		var text = "Text: \"" + textFilter + "\"";
		this.$(".textFilterPill .pillContent").text(text);
		this.$(".textFilterPill").show();
	},
	
	hideTextPill : function() {
		this.$(".textFilterPill").hide();
		this.$(".selectionSearchText").val("");
	},
	
	showSelectedPill : function(numberSelected) {
		var selectionListItemsName = Config.tabConfig[this.tabName].selectionList.filterTypeText;
		this.$(".numberSelectedPill .pillContent").text(numberSelected + " " + selectionListItemsName + " Selected");
		this.$(".numberSelectedPill").show();
	},
	
	hideSelectedPill : function() {
		this.$(".numberSelectedPill").hide();
	},
	
	/**
	 * Clears all selected to reset filters
	 * Called from the selection pill
	 */
	clearSelection : function() {
		var tabName = this.tabName;
		var selected = this.model.collection.allSelected(tabName);
		for (var i = 0; i < selected.length; i++) {
			selected[i].setSelectionListSelected(tabName, false);
		}
		this.hideSelectedPill();
		
		this.model.filters.removeFilter([{
			collectionName: "collection", 
			filterName : "CheckboxSelectionFilter"
		},
		{
			collectionName: "tilesCollection", 
			filterName: "CheckboxSelectionFilter"
		}
		]);
	},
	
	/**
	 * Clears ALL filters that affect the selection list
	 */
	clearAllFilters : function() {
		this.clearSelection();
		this.clearTextFilter();
		
		this.model.updateVisibleTilesCount(this.tabName);
	},
	
	/**
	 * Clears all text search filters to reset the list
	 * Called from the filter text pill
	 * 
	 * Unhides all this.model.collection
	 */
	clearTextFilter : function() {
		var tabName = this.tabName;
		this.model.filters.removeFilter({
			collectionName: "collection", 
			filterName: "TextSelectionFilter"
		});
		this.$(".selectionSearchText").text("");
		this.hideTextPill();
	},
	
	/**
	 * This tab's selection has been updated so the tiles (and the list) need
	 * to be updated as well.
	 * 
	 * If there are no selected elements in the selection list, show all tiles and
	 * selection list elements
	 */
	selectionListUpdate : function() {
		var selectedElements = this.model.collection.allSelected(this.tabName);
		var selectedElementsLength = selectedElements.length;
		if (selectedElementsLength > 0) {
			this.showSelectedPill(selectedElementsLength);
			this.model.filters.addFilter("tilesCollection", 
					new QT.Filters.CheckboxSelectionFilter(this.model.tilesCollection, selectedElements));
		}
		else {
			this.clearSelection();
			//this.model.updateVisibleTilesCount(this.tabName);
		}
		
	}
});