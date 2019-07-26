/**
 * model:QT.Query(singleton loaded  from QT.Page)
 * controls:filters inside refine data
 * el:#filterPaneContentContainer
 */
var multiFilterDEs = ["GUID", "OrderableBiosampleID"];

QT.RefineDataFiltersView = BaseView.extend({
	applyFiltersEnabled : false,
	
	events : {
		//add the event for Apply Filters here
		"click .applyFilterButton" : "applyFilters"
		
	},
	
	initialize : function() {
		EventBus.on("add:queryFilter",this.addFilter,this);
		EventBus.on("window:resize", this.onWindowResize, this);
		// changing tabs is effectively the same as window resizing
		EventBus.on("select:stepTab", this.onWindowResize, this);
		EventBus.on("remove:filter", this.onRemoveFilter, this);
		EventBus.on("query:reset", this.onQueryReset, this);
		EventBus.on("filters:showError", this.showFilterError, this);
		EventBus.on("filters:removeError", this.removeFilterError, this);
	},
	
	
	render : function() {
		
		
		this.resizeFilterPaneContainer();
	},
	
	addFilter : function(queryFilterModel) {
		var type = queryFilterModel.get("filterType");
		var elementName = queryFilterModel.get("elementName");
		var view;
		// regardless of all else, if the queryFilterModel has permissible values, it's a permissible value type
		if (queryFilterModel.get("permissibleValues").length > 0) {
			if (type == Config.filterTypes.freeFormType) {
				view = new QT.FilterOtherSpecifyView({model:queryFilterModel});
			} else if (type == Config.filterTypes.radioFormType) {
				view = new QT.FilterRadioListView({model:queryFilterModel});
			}
			else {
				view = new QT.FilterEnumeratedListView({model:queryFilterModel});
			}
		}
		else if(type == Config.filterTypes.freeFormType) {
			if(multiFilterDEs.indexOf(elementName) > -1){
				view = new QT.FilterFreeFormLargeView({model: queryFilterModel});
			}
			else {
                view = new QT.FilterFreeFormView({model: queryFilterModel});
            }
		}
		else if(type == Config.filterTypes.numericRangeType){
			view = new QT.FilterNumericRangeView({model:queryFilterModel});	
		}
		else if(type == Config.filterTypes.numericUnbounded) {
			view = new QT.FilterNumericUnboundedView({model:queryFilterModel});	
		}
		else if(type == Config.filterTypes.dateRangeType) {
			view = new QT.FilterDateRangeView({model:queryFilterModel});	
		}
		else if(type == Config.filterTypes.permissibleValuesType) {
			view = new QT.FilterEnumeratedListView({model:queryFilterModel});
		}

		this.model.filters.add(queryFilterModel);
		view.render(this.$("#filtersList"));
		view.fillData();
		
		this.enableDisableApplyFilter();
	},
	
	showFilterError : function() {
		this.$('.applyFilterButton').addClass("disabled");
	},
	
	removeFilterError : function() {
		this.enableDisableApplyFilter();
	},
	
	onQueryReset : function() {
		this.enableDisableApplyFilter();
	},
	
	onRemoveFilter : function() {
		this.enableDisableApplyFilter();
	},
	
	enableDisableApplyFilter : function() {
		if (this.model.filters.length > 0) {
			this.$('.applyFilterButton').removeClass("disabled");
			this.applyFiltersEnabled = true;
		}
		else {
			this.$('.applyFilterButton').addClass("disabled");
			this.applyFiltersEnabled = false;
		}
	},
	
	onWindowResize : function() {
		this.resizeFilterPaneContainer();
	},
	
	resizeFilterPaneContainer : function() {
		var containerHeight = 0;
		var windowHeight = window.innerHeight;
		/*
		 * the height of this container should be the same as the height
		 * of the container above it, $("#refineDataCartContainer").
		 * However, we can't guarantee that container is already resized,
		 * so, we have to base our knowledge on what we can know: its
		 * top left corner position.
		 */
		var currentTop = $("#refineDataCartContainer").offset().top;
		var paddingTop = Number(this.$el.css("padding-top").replace("px", ""));
		var paddingBottom = Number(this.$el.css("padding-bottom").replace("px", ""));
		var borderThickness = 1;
		var extraPadding = Config.scrollContainerBottomOffset;
		containerHeight = (windowHeight - currentTop - (paddingTop + paddingBottom) - extraPadding - (borderThickness * 2) - 10) / 2;
		containerHeight -= 1; // adjust for border
		this.$el.height(containerHeight);
		
		this.resizeFilterList(containerHeight);
	},
	
	resizeFilterList : function(containerHeight) {
		var headerHeight = this.$("#filterPaneHeader").height();
		var height = containerHeight - headerHeight;
		this.$("#filtersList").height(height);
	},
	
	applyFilters : function() {
		/* params:
		 * 
		 * filters: json array of all filters (see QueryFilter::toObjectJson())
		 * offset: int page offset (from table data)
		 * limit: page display limit (from table data)
		 * sortColName: sort column in formNameAndVersion,rgName,deName format (from table data)
		 * sortOrder : "asc" or "desc" (from table data) 
		 */
		if ($(".filterErrorContainer").children().length > 0) {
			$.ibisMessaging("dialog", "error", "You cannot apply filters while there is still an error in their configuration");
		}
		else {
			if (this.applyFiltersEnabled) {
				//switch tab
				EventBus.trigger("openDataTableViewTab");
				//apply filter
				EventBus.trigger("applyFilters");
			}
		}
	}

});


