/**
 * model:QT.Query(singleton loaded  from QT.Page)
 * controls:filters inside refine data
 * el:#filterPaneContentContainer
 */
var multiFilterDEs = ["GUID", "OrderableBiosampleID","Dataset"];

QT.RefineDataFiltersView = BaseView.extend({
	applyFiltersEnabled : false,
	
	events : {
		//add the event for Apply Filters here
		"click #filterLogicRunQuery" : "runQuery",
		
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
		EventBus.on("sort:filters", this.sortFilters, this);
	},
	
	
	render : function() {
		this.resizeFilterPaneContainer();
		this.assign("#filterLogicBoxContainer", new QT.FilterLogicBoxView({model: this.model}));
	},
	
	addFilter : function(queryFilterModel) {
		var type = queryFilterModel.get("filterType");
		var elementName = queryFilterModel.get("elementName");
		var view;
		// regardless of all else, if the queryFilterModel has permissible values, it's a permissible value type
		if (queryFilterModel.get("permissibleValues").length > 0) {
			if (queryFilterModel.get("selectOther")) {
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
		else if(type == Config.filterTypes.multiRangeType){
			view = new QT.FilterAgeYrsView({model:queryFilterModel});
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
		this.$('.filterLogicButton').addClass("disabled");
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
			this.$('.filterLogicButton').removeClass("disabled");
			this.model.set("applyFiltersEnabled",true);
		}
		else {
			//this.$('.filterLogicButton').addClass("disabled");
			this.model.set("applyFiltersEnabled",false);
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
		 * 
		 * However, this container can resize height now based on the height
		 * toggle. So, in that case, resize this container to properly
		 * display the "expanded" state.
		 */
		var isOpen = QueryTool.page.get("dataCartPaneOpen");
		var currentTop = $("#refineDataCartContainer").offset().top;
		var paddingTop = Number(this.$el.css("padding-top").replace("px", ""));
		var paddingBottom = Number(this.$el.css("padding-bottom").replace("px", ""));
		var borderThickness = 1;
		var extraPadding = Config.scrollContainerBottomOffset;
		
		if (isOpen) {
			containerHeight = (windowHeight - currentTop - (paddingTop + paddingBottom) - extraPadding - (borderThickness * 2) - 10) / 2;
			containerHeight -= 1; // adjust for border
		}
		else {
			var dataCartHeaderHeight = $(".refineDataDataCartHeader").height();
			var dataCartHeaderTotalHeight = dataCartHeaderHeight + paddingTop + paddingBottom + borderThickness;
			containerHeight = windowHeight - currentTop - dataCartHeaderTotalHeight - (borderThickness * 2) - (paddingTop + paddingBottom);
		}
		this.$el.height(containerHeight);
		
		this.resizeFilterList(containerHeight);
	},
	
	resizeFilterList : function(containerHeight) {
		var headerHeight = this.$("#filterPaneHeader").height();
		var height = containerHeight - headerHeight;
		this.$("#filtersList").height(height);
	},
	
	/**
	 * If the filters have the order attribute, sort them based on that order attribute
	 * 
	 * @see https://stackoverflow.com/questions/13490391/jquery-sort-elements-using-data-id
	 */
	sortFilters : function() {
		var filterList = $('#filtersList'), filters = filterList.children('.queryFilterContainer');		
		if (filterList.length > 0 && filters.eq(0).attr("order") !== "") {
			filters.detach().sort(function(a, b) {
		        var astts = parseInt($(a).attr('order'));
		        var bstts = parseInt($(b).attr('order'));
		        return (astts > bstts) ? (astts > bstts) ? 1 : 0 : -1;
		    });
	
			filterList.append(filters);
		}
	},
	
	//NOTE: Maybe we should move this to RefineDataCartView.js
	/*
	 * This function is designed to run the entire query for gathering data from the backend. It will be responsible for
	 * creating joins and applying filters. If no filters exist it will still produce a join.  If a join doesn't exist it will 
	 * still apply filters.   If a join has already been loaded, it should not reload the join. 
	 */
	runQuery : function() {
		//if we have loaded from a saved query and want to see the data
		if(this.model.get("onReload")){
			this.model.set("onReload",false);
			//render table
			EventBus.trigger("renderTable");
			
			
		}
		//if a user has already joined forms and just wants to apply filters we need to check if the forms are joined
		//if so skip joining forms. 
		else if(!this.model.get("currentlyJoined") && !this.model.get("singleFormRequest")) {
			EventBus.trigger("joinForms");
		} else if (this.model.get("singleFormRequest") || this.model.get("currentlyJoined")) {
			
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
				if (this.model.get("applyFiltersEnabled")) {
					//switch tab
					EventBus.trigger("openDataTableViewTab");
					//apply filter
					EventBus.trigger("applyFilters");
				}
			}
			
		}
		
		
		
	},

});


