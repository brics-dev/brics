/**
 * 
 */
QT.FilterDateRangeView = QT.GenericQueryFilterView.extend({
	
	events : {
		"click .filterToggle" : "toggleFilterBody",
		"click .filterClose" : "closeFilter",
		"change .genericLogicSelect" : "onChangeGenericLogicSelect",
		"change .filterLogicSelect" : "onChangeFilterLogicSelect",
		"change .filterDateMinTextBox" : "onChangeMinDate",
		"change .filterDateMaxTextBox" : "onChangeMaxDate",
		"change .filterLogic" : "onChangeCombLogic",
		"change .filterLogicBeforeTwo,.filterLogicBeforeOne,.filterLogicAfterOne,.filterLogicAfterTwo" : "onChangeGroupingContainer"
	},
	
	initialize : function() {
		this.template = TemplateManager.getTemplate("filterDateRange");
		QT.FilterDateRangeView.__super__.initialize.call(this);	
		this.listenTo(this.model,"change:selectedMinimum",this.onModelChange);
		this.listenTo(this.model,"change:selectedMaximum",this.onModelChange);
	},
	
	
	render : function($container) {
		this.$el.html(this.logicBeforeTemplate() + this.genericTemplate(this.model.attributes) + this.logicAfterTemplate());
		$container.append(this.$el);
		
		this.onRenderHandleSubFilters();
	
		QT.FilterDateRangeView.__super__.render.call(this);
		this.populateFormStructureTitleAndElement();
	},
	
	renderSubFilter : function(subFilterModel) {
		var $filterLogicContainer = this.$(".filterLogicContainer");
		
		// if this is the first sub-filter, the logic options "and", "or" should be disabled
		if (this.model.get("filters").length == 1) {
			subFilterModel.set('showFilterLogicOr',false);
			subFilterModel.set('showFilterLogicAnd',false);
		}
		
		$filterLogicContainer.find(".genericLogicSelect").before(this.template(subFilterModel.attributes));
		
		// we just added the last instance of .filterLogicRow to this.$, so we can operate on it
		var $container = this.$(".filterLogicRow").last();
		$container.find(".filterDateMinTextBox").datepicker({
			dateFormat : "m/d/y"
		});
		$container.find(".filterDateMaxTextBox").datepicker({
				dateFormat : "m/d/y"
		});
		
		
	},
	
	/**
	 * Fills the on-page elements with values from this filter model (and sub-filters from
	 * their sub-models).
	 * 
	 * called from RefineDataFiltersView after initialize() and render() on this view
	 */
	fillData : function() {
		var model = this.model;
		QT.FilterDateRangeView.__super__.fillData.call(this);
		var filters = model.get("filters");
		var $subFilters = this.$(".filterLogicRow");
		for (var i = 0, filterSize = filters.length; i < filterSize; i++) {
			var filter = filters.at(i);
			var $subFilter = $subFilters.eq(i);
			var dateMin = filter.get("selectedDateMin");
			var dateMax = filter.get("selectedDateMax");
			if (dateMin) {
				$subFilter.find(".filterDateMinTextBox").val(dateMin);
			}
			if (dateMax) {
				$subFilter.find(".filterDateMaxTextBox").val(dateMax);
			}
			$subFilter.find(".filterLogicSelect").val(filter.get("logicBefore"));
		}
	},
	
	/**
	 * Respond to UI events that change the min date value.  NOT model changes
	 */
	onChangeMinDate : function(event, ui) {
		var $target = $(event.target);
		this.onChangeDateGeneric($target, "selectedDateMin");
	},
	
	/**
	 * Responds to UI events that change the max date value.  NOT model changes
	 */
	onChangeMaxDate : function(event, ui) {
		var $target = $(event.target);
		this.onChangeDateGeneric($target, "selectedDateMax");
	},
	
	onChangeDateGeneric : function($target, modelField) {
		this.closeError();
		var value = this.validateDate($target.val());
		if (value !== false) {
			this.getThisModel($target).set(modelField, value);
		}
		EventBus.trigger("populateQueryBox");
	},
	
	onModelChange : function(changingModel) {
		var changingValue;
		var changingField;
		var revert = false;
		
		// determine which value is changing and validate it
		if (typeof changingModel.changed.selectedDateMin !== "undefined") {
			changingValue = changingModel.changed.selectedDateMin;
			changingField = "selectedDateMin";
		}
		else if (typeof changingModel.changed.selectedDateMax !== "undefined") {
			changingValue = changingModel.changed.selectedDateMax;
			changingField = "selectedDateMax";
		}
		
		// validate that the value is a date and make sure max is greater than min
		if (!this.validateDate(changingValue) || !this.validateMaxMinDifference(changingModel)) {
			// revert
			changingModel.set(changingField, changingModel.previous(changingField), {silent: true});
		}
		EventBus.trigger("populateQueryBox");
	},
	
	/**
	 * Checks that the incoming value is a date. Shows an error if this check fails.
	 * 
	 * @param value the value to check
	 * @return boolean false if the check fails, otherwise the date in integer format
	 */
	validateDate : function(value) {
		if (isNaN(Date.parse(value))) {
			this.showError("The input must be a date");
			return false;
		}
		else {
			return value;
		}
	},
	
	/**
	 * Checks that the start date is before the end date.  If that check fails,
	 * show an error and return false
	 * 
	 * @param changingModel the model that is changing
	 * @return boolean true if input is valid, otherwise false
	 */
	validateMaxMinDifference : function(changingModel) {
		var min = changingModel.get("selectedDateMin");
		var max = changingModel.get("selectedDateMax");
		if (typeof min !== "undefined" && typeof max !== "undefined") {
			if (min > max) {
				this.showError("The start date cannot be after the end date");
				return false;
			}
		}
		return true;
	}
	
});