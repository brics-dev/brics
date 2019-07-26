/**
 * Extends SelectionListView to handle the special cases for Data Elements
 * 
 * model: SelectionList with DefinedQuery list
 */

QT.SqSelectionListView = QT.SelectionListView.extend({
	
	events : {
		"click .resetFilter" : "clearSelection",	// selection list clear
		"click .expandCollapseButton" : "expandCollapse",
		"click .selectionSearchButton" : "filterDataTextSearch",
		"click .selectionSearchReset" : "filterDataResetFilter", // reset all
		"keydown .selectionSearchText" : "filterDataTextSearchKey",
		"click .numberSelectedPill .closeButton" : "clearSelection",
		"click .textFilterPill .closeButton" : "clearTextFilter",
		// the only one specific to SQ Tab
		"click .editSqButton" : "openEditQueryDialog"
	},
	
	savedQueryLoadUtil : null,
	loadedSqId : -1,
	
	initialize : function() {
		this.savedQueryLoadUtil = QT.SavedQueryLoadUtil;
		EventBus.on("load:savedQuery", this.registerLoadSavedQuery, this);
		EventBus.on("delete:savedQuery", this.handleDeleteQueryEvent, this);
		EventBus.on("clearDataCart", this.onClearDataCart, this);
		
		QT.SqSelectionListView.__super__.initialize.call(this);
	},
	
	render : function() {
		this.tabName = this.model.get("tabName");

		// redefining to local scope for performance and for sub-context handling
		var tabName = this.tabName;
		
		EventBus.on("selectionChange:" + this.tabName, this.selectionListUpdate, this);
		this.listenTo(this.model.collection, "add", this.renderSingleSq);
		
		// render the base template for this area
		this.$el.html(this.template(this.model.attributes));
		// this is here instead of PageView to prevent a race condition
		QueryTool.page.loadSavedQueries();
		
		this.renderSelectionTabText();
		this.resizeSelectionList(this.$(".selectionList"));
	},
	
	renderSingleSq : function(model) {
		var view = new QT.SqSelectionListItemView({model: model});
        view.render(this.tabName);
        this.model.updateVisibleTilesCount(this.tabName);
	},
	
	resizeSelectionList : function($selectionItemsContainer) {
		var windowHeight = window.innerHeight;
		var currentTop = $selectionItemsContainer.offset().top;
		var $parentContainer = $selectionItemsContainer.parents(".selectionListPanel").eq(0);
		var bottomPadding = Number($parentContainer.css("padding-bottom").replace("px", ""));
		var extraPadding = Config.scrollContainerBottomOffset;
		$selectionItemsContainer.height(windowHeight - currentTop - bottomPadding - extraPadding);

		this.updateFilterCount();
	},
	
	onClearDataCart : function() {
		// uncheck all radio buttons if there are any checked
		this.$(".selectionListRadio:checked").prop("checked", false);
		this.$(".editSqButton").addClass("disabled");
	},
	
	/**
	 * Listener for the "load:savedQuery" event. Records the ID of the loaded saved query, and
	 * enables the edit button. If a negative or non-number is passed in, the edit button will
	 * be disabled instead.
	 * 
	 * @param {String} id  - The ID of the loaded saved query
	 */
	registerLoadSavedQuery : function(id) {
		this.loadedSqId = Number(id);
		
		$.ajaxSettings.traditional = true;
		var view = this;
		$.ajax({
			type: "GET",
			cache : false,
			url : "service/savedQueries/canEdit",
			data : {
				id : id
			},
			success : function(data, textStatus, jqXHR) {
				// Only enable the saved query edit button if the ID is valid 
				//NOTE: Number.isNaN() is not supported by IE
				if (window.navigator.userAgent.indexOf("MSIE") > 0 || window.navigator.userAgent.indexOf('Trident/') > 0 ) {
					if ( typeof(view.loadedSqId) ==='number' && !isNaN(view.loadedSqId) && view.loadedSqId > 0 && data == "true") { 
					view.$(".editSqButton").removeClass("disabled");
					} else {
						view.$(".editSqButton").addClass("disabled");
					}
				} else {					
					if ( !Number.isNaN(view.loadedSqId) && view.loadedSqId > 0 && data == "true") {
						view.$(".editSqButton").removeClass("disabled");
					}
					else {
						view.$(".editSqButton").addClass("disabled");
					}
				}
			},
			error : function() {
				// TODO: fill in
				if ( window.console && console.log ){
				console.log("error getting permission for edit saved query");
				}
			}
		});
	},
	
	/**
	 * Responses to the deletion of a saved query by disabling the edit button.
	 * 
	 * @param {Number} id - The ID of the saved query that was deleted.
	 */
	handleDeleteQueryEvent : function(id) {
		if ( (typeof id == "number") && (id > 0) ) {
			this.$(".editSqButton").addClass("disabled");
			this.model.collection.remove(id);
		}
	},
	
	openEditQueryDialog : _.debounce(function(event) {
		var $editBtn = $(event.currentTarget);
		
		if ( !$editBtn.hasClass("disabled") ) {
			EventBus.trigger("open:processing", "Loading defined query details for editing...");
			
			try {
				var sq = this.savedQueryLoadUtil.getSavedQueryModel(this.loadedSqId);
				
				if ( (sq != null) && (sq.get("name").length > 0) ) {
					EventBus.trigger("open:saveQueryDialog", "edit", sq);
				}
				else {
					$.ibisMessaging("dialog", "error", "Could not retrieve the defined query information for editing.");
				}
			}
			catch (err) {
				console.error("The following error occurred while loading saved query details for edit: " + err);
				$.ibisMessaging("dialog", "error", err);
			}
			finally {
				EventBus.trigger("close:processing");
			}
		}
	}, 3000, true),
	
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
	
	updateFilterCount : function() {
		this.config = Config.tabConfig[this.model.get("tabName")];
		if (this.config.name == "DefinedQueriesTab" ) {
			var queryCount = this.model.collection.length;	

			var queryCountTxt = "("+queryCount+" Results)";
			this.$(".filteringCount").text(queryCountTxt);
		}
	}
});