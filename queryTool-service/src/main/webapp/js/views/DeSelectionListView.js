/**
 * Extends SelectionListView to handle the special cases for Data Elements
 */

QT.DeSelectionListView = QT.SelectionListView.extend({
	
	events : {
		"click .resetFilter" : "clearSelection",	// selection list clear
		"click .expandCollapseButton" : "expandCollapse",
		"click .selectionSearchButton" : "filterDataTextSearch",
		"click .selectionSearchReset" : "filterDataResetFilter", // reset all
		"keydown .selectionSearchText" : "filterDataTextSearchKey",
		"click .numberSelectedPill .closeButton" : "clearSelection",
		"click .textFilterPill .closeButton" : "clearTextFilter",
		// the only one specific to DE tab
		"click .selectDeButton" : "openSelectDeDialog",
		"click .selectionDeSearchReset" : "resetPane"
	},
	
	render : function() {
		this.tabName = this.model.get("tabName");

		// redefining to local scope for performance and for sub-context handling
		var tabName = this.tabName;
		EventBus.on("selectionChange:" + this.tabName, this.selectionListUpdate, this);
		EventBus.on("dataElements:add", this.updateElementVisiblity, this);
		
		// render the base template for this area
		this.$el.html(this.template(this.model.attributes));
		
		var $selectionItemsContainer = this.$(".selectionList");
		var collection = this.model.collection;
		var endingNumber = collection.length;
		
		this.renderSelectionTabText();
		this.resizeSelectionList($selectionItemsContainer);
		
		// loop over SelectionDataElements
		if (endingNumber > 0) {
			var j = 0;
			
			var renderMethod = function(model, newValue) {
	    		var view = new QT.DeSelectionListItemView({model: model});
		        view.render($selectionItemsContainer, tabName);
	    	};
			
			setTimeout(function deLoopFunction() {
			    try {
			    	for (var i = 0; i < 100 && j < endingNumber; i++) {
			    		collection.at(j).once("change:isVisibleSelectionListDataElementsTab", renderMethod);
				    	j++;
			    	}
			    }
			    catch(e) {
			        // handle any exception
			    	//Log.developer.error("failed to render an item view");
			    	console.error("failed to render an item view");
			    }
			     
			    j++;
			    if (j < endingNumber) {
			        setTimeout(deLoopFunction, 0); // timeout loop
			    }
			    else {
			        // any finalizing code
			    }
			});
		}
		
		this.model.updateVisibleTilesCount(this.tabName);
		
		QT.SelectionListView.__super__.render.call(this);
	},
	
	resizeSelectionList : function($selectionItemsContainer) {
		var windowHeight = window.innerHeight;
		var currentTop = $selectionItemsContainer.offset().top;
		var $parentContainer = $selectionItemsContainer.parents(".selectionListPanel").eq(0);
		var bottomPadding = Number($parentContainer.css("padding-bottom").replace("px", ""));
		var extraPadding = Config.scrollContainerBottomOffset;
		$selectionItemsContainer.height(windowHeight - currentTop - bottomPadding - extraPadding);
	},
	
	openSelectDeDialog : function() {
		EventBus.trigger("open:selectDeDialog");
	},
	
	resetPane : function(){
		EventBus.trigger("reset:selectDeDialog");
		this.render();
	},
	
	/**
	 * Updates the filter which controls the visible list of data elements.
	 * This works very different from the others, has no pill, and no associated
	 * tile filter.
	 */
	updateElementVisiblity : function(visibleList) {
		this.model.filters.setFilter("collection", new QT.Filters.DeHideShowFilter(this.model.collection, visibleList));
		this.updateFilterCount(visibleList.length);
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
		var selectedElementsURI = [];
		for (var i = 0; i < selectedElements.length; i++) {
			selectedElementsURI.push(selectedElements[i].get("uri"));
		}
		
		// "service/query/deForms" - expects a string "textValue" for text search and a list of DE URIs "selectedDeUris"
		// DeCheckboxSelectionFilter
		var view = this;
		$.ajax({
			url: "service/query/deForms",
			cache : false,
			traditional: true,
			data: {
				textValue : "", // text search is handled elsewhere
				selectedDeUris : selectedElementsURI 
			},
			dataType: "json",
			success : function(data) {
				if (typeof data.status !== "undefined") {
		         	if(data.status = "401") {
		         		//redirect
		         		window.location.href = "/query/logout";
		         		return;
		         	}
		         }
				var searchList = view.model.tilesCollection;
				if (searchList.length == data.length) {
					// show all
					view.model.filters.removeFilter({
						collectionName : "tilesCollection",
						filterName : "DeCheckboxSelectionFilter"
					});
				}
				else {
					// get model version of each element and store in new list
					var models = [];
					for (var i = 0; i < data.length; i++) {
						models.push(searchList.get(data[i].uri));
					}
					// use the model list to run the filter
					// on all tabs, the text list is the tiles list
					
					view.model.filters.addFilter("tilesCollection", 
							new QT.Filters.DeCheckboxSelectionFilter(view.model.tilesCollection, models));
				}
			}
		});
		
		
	},
	
	updateFilterCount : function(deCount) {
		this.config = Config.tabConfig[this.model.get("tabName")];
		if (this.config.name == "DataElementsTab" && typeof deCount !== "undefined") {

			var deCountTxt = "("+deCount+" Results)";
			this.$(".filteringCount").text(deCountTxt);
		}
	}
});