/**
 * 
 */
QT.GenericQueryFilterView = BaseView.extend({
	className: 'queryFilterContainer',

	logicBeforeTemplate: null,
	logicAfterTemplate: null,
	genericTemplate: null,
	
	initialize: function() {
		QT.GenericQueryFilterView.__super__.initialize.call(this);
		EventBus.on('clearDataCart', this.onClearDataCart, this);
		EventBus.on('query:reset', this.onQueryReset, this);
		EventBus.on('runQuery', this.onQueryRun, this);
		EventBus.on('remove:filter', this.maybeHideCombFilterLogic, this);
		EventBus.on('remove:visualFilter', this.maybeHideCombFilterLogic, this);
		EventBus.on("dataTableView:changeDisplayOption", this.updateFiltersForPV, this);
		//EventBus.on('runJoinQuery', this.onQueryRun, this);
		this.genericTemplate = TemplateManager.getTemplate('filterGenericContainer');
		this.logicBeforeTemplate = TemplateManager.getTemplate('filterGenericLogicBefore');
		this.logicAfterTemplate = TemplateManager.getTemplate('filterGenericLogicAfter');
		EventBus.on(
			'removeAll:filters',
			function() {
				this.closeFilter(false);
			},
			this
		);
	},

	render: function() {
		// If this filter is the first in the list, hide the filter logic container
		var $el = this.$el;
		$el.attr("order", String(this.model.get("sortOrder")) || "0");
		this.maybeHideCombFilterLogic();
		this.scrollIntoView();
		EventBus.trigger("rendered:queryFilter", this.model);
	},
	
	maybeHideCombFilterLogic : function() {
		var view = this;
		_.defer(function() {
			var $el = view.$el;
			if (typeof $el !== "undefined") {
				var index = $el.parent().find('.queryFilterContainer').index($el);
				if (index == 0) {
					view.$('.filterCombLogicContainer').hide();
					view.model.set("logicBefore", "");
				}
			}
		});
	},
	
	scrollIntoView : function() {
		_.defer(function() {
			var $list = $('#filtersList');
			var listElement = $list[0];
			$list.scrollTop(listElement.scrollHeight - listElement.clientHeight);
		});
	},

	populateFormStructureTitleAndElement: function() {
		var form = QueryTool.page.get('forms').get(this.model.get('formUri'));
		var title = 'Error:Unknown Form';
		if (typeof form != 'undefined') {
			title = form.get('title');
		}
		this.$('.filter_formName').text(title);
		var elementName = this.model.get('elementName');
		this.$('.filter_element').text(elementName);
	},

	/**
	 * Adds a single subfilter to the current list.
	 */
	addOneSubFilter: function(filtersLength) {
		var model = this.model;
		var formUri = model.get('formUri');
		var formUriName = formUri.substring(formUri.lastIndexOf('/') + 1);
		var filterName = formUriName + '_' +  model.get('groupName') + '_' + model.get('elementName') + '_' + (filtersLength ? filtersLength : '0');
		filterName = filterName.replace(/ /g,"$");

		var subModel = new QT.QueryFilter({
			filters: [],
			specialFilters: [],
			formUri: model.get('formUri'),
			elementUri: model.get('elementUri'),
			groupUri: model.get('groupUri'),
			elementName: model.get('elementName'),
			groupName: model.get('groupName'),
			filterType: model.get('filterType'),
			filterJavaType: model.get('filterJavaType'),
			permissibleValues: model.get('permissibleValues'),
			maximum: model.get('maximum'),
			minimum: model.get('minimum'),
			showInclusiveExactToggle: model.get('showInclusiveExactToggle'),
			showAndOption: model.get('showAndOption'),
			showFilterLogicSelect: model.get('showFilterLogicSelect'),
			showFilterLogicOr: model.get('showFilterLogicOr'),
			showFilterLogicAnd: model.get('showFilterLogicAnd'),
			showGenericSelect: model.get('showGenericSelect'),
			inputRestrictions: model.get('inputRestrictions'),
			name: model.get('elementName') + '_' + (filtersLength ? filtersLength : '0'),
			filterName: filterName,
			outputCodeSelection: model.get('outputCodeSelection')
		});
		this.pushNewSubFilter(subModel);
		if(model.get('filterType') == Config.filterTypes.permissibleValuesType){
			this.renderSubFilter(subModel,QueryTool.page.get("query").get("outputCodeSelection"),false);
		} else {
			this.renderSubFilter(subModel);
		}
		return subModel;
	},

	/**
	 * Handles rendering sub-filters, if there are any.
	 * Reminder: the main model for this view will NEVER be a sub-filter
	 */
	onRenderHandleSubFilters: function() {
		var subFilters = this.model.get('filters');

		if (subFilters.length > 0) {
			// there are sub-filters, render them
			for (var i = 0; i < subFilters.length; i++) {
				// don't have to register since it's already there
				this.renderSubFilter(subFilters.at(i));
			}
		} else {
			// There are no sub-filters already. Create the first query row
			// - including an actual query model and sub-render
			this.addOneSubFilter();
		}
	},
	
	updateFiltersForPV: function(displaySelection) {
		var subFilters = this.model.get('filters');
		var outputCodeSelection = QueryTool.page.get("query").get("outputCodeSelection");
		if (subFilters.length > 0) {
			for (var i = 0; i < subFilters.length; i++) {
				if(subFilters.at(i).get('filterType') == Config.filterTypes.permissibleValuesType){
					this.renderSubFilter(subFilters.at(i),outputCodeSelection,true);
				}
			}
		}
	},

	/**
	 * Handles changes to the individual filter's logic selector
	 */
	onChangeFilterLogicSelect: function(event) {
		var $target = $(event.target);
		var $container = $target.parents('.filterLogicRow').first();
		var value = $target.val();
		value = (value == "none") ? "" : value;
		// we can't add here, just remove.  Other options are disabled
		// don't remove the first sub-filter
		var $logicContainer = $container.parents('.filterLogicContainer').first();
		if ($logicContainer.find('.filterLogicRow').length > 1 &&  value == "") {
			$container.remove();
			this.popSubFilter();
			this.enableLastFilterLogicOptions($logicContainer);
		}
		// set the value to the model
		var index = this.$('.filterLogicSelect').index($target);
		this.model.get('filters').at(index).set('logicBefore', value);
		EventBus.trigger("populateQueryBox");
	},

	/**
	 * Handles changes to the LogicBefore/LogicAfter parentheses changing.
	 * This updates the model 
	 */
	onChangeGroupingContainer: function(event, ui) {
		var groupingBefore = 0;
		var groupingAfter = 0;
		var currentGroupingBefore = this.model.get('groupingBefore');
		var currentGroupingAfter = this.model.get('groupingAfter');

		if (this.$('.filterLogicBeforeOne').is(':checked')) {
			groupingBefore += 1;
		}
		if (this.$('.filterLogicBeforeTwo').is(':checked')) {
			groupingBefore += 2;
		}
		if (this.$('.filterLogicAfterOne').is(':checked')) {
			groupingAfter += 1;
		}
		if (this.$('.filterLogicAfterTwo').is(':checked')) {
			groupingAfter += 2;
		}

		if (groupingBefore != currentGroupingBefore) {
			this.model.set('groupingBefore', groupingBefore);
		} else if (groupingAfter != currentGroupingAfter) {
			this.model.set('groupingAfter', groupingAfter);
		}
		EventBus.trigger("populateQueryBox");
	},

	/**
	 * Handles the change event of the filterLogicSelect (the select input containing
	 * none, NOT, OR, or AND
	 */
	onChangeGenericLogicSelect: function(event) {
		/* 
		 * if the selected option is now "AND", "NOT", or "OR", add a new
		 * subfilter to the bottom of the set of subfilters
		 */
		var $target = $(event.target);
		var $container = $target.parents('.filterLogicContainer').first();
		var value = $target.val();
		if (value == '||' || value == '&&' || value == "!") {
			// insert one subfilter and copy the current logic value to it
			this.disableLastFilterLogicOptions($container);
			var subModel = this.addOneSubFilter(this.model.get('filters').length);
			subModel.set('logicBefore', value);
			$container.find('.filterLogicRow').last().find('.filterLogicSelect').val(value);
		}
		// update the generic logic selector to be "none"
		$target.val('none');
		EventBus.trigger("populateQueryBox");
	},

	/**
	 * Registers the new subfilter Model with the current parent filter's "filter"
	 * array.
	 * 
	 * @param newFilterModel - the newly-created filter model
	 */
	pushNewSubFilter: function(newFilterModel) {
		this.model.get('filters').push(newFilterModel);
	},
	
	/**
	 * Removes the subfilter Model with the current parent filter's "filter"
	 * array.
	 * 
	 * @param removeFilterModel - the filter model to be removed
	 */
	removeSubFilter: function(removeFilterModel) {
		this.model.get('filters').remove(removeFilterModel);
		EventBus.trigger("populateQueryBox");
	},

	/**
	 * Registers the new specialfilter Model with the current parent filter's "specialFilter"
	 * array.
	 * 
	 * @param newSpecialFilterModel - the newly-created specialfilter model
	 */
	pushNewSpecialFilter: function(newSpecialFilterModel) {
		this.model.get('specialFilters').push(newSpecialFilterModel);
	},
	
	/**
	 * Removes the specialfilter Model with the current parent filter's "specialFilter"
	 * array.
	 * 
	 * @param removeSpecialFilterModel - the filter model to be removed
	 */
	removeSpecialFilter: function(removeSpecialFilterModel) {
		this.model.get('specialFilters').remove(removeSpecialFilterModel);
	},

	/**
	 * Removes the last subFilter Model from the current parent filter's "filter"
	 * array.
	 */
	popSubFilter: function() {
		return this.model.get('filters').pop();
	},

	/**
	 * Gets the model corresponding to the $target element within the list of filters.
	 * For example, if the $target is an input in the 3rd sub-filter, the model
	 * returned should be the 3rd model.
	 * 
	 * @param $target input for the returned model
	 * @return Model that corresponds to the $input's position in the filter list
	 */
	getThisModel: function($target) {
		var className = $target.attr('class').split(' ')[0];
		var index = this.$('.' + className).index($target);
		return this.getSubFilterAt(index);
	},

	/**
	 * Get a single sub-filter model from within the collection of subfilters
	 * by index
	 * 
	 * @param index the index of the model to get
	 */
	getSubFilterAt: function(index) {
		return this.model.get('filters').at(index);
	},

	enableLastFilterLogicOptions: function($logicContainer) {
		$logicContainer.find('.filterLogicSelect').last().find('.filterLogicSingle').prop('disabled', false);
	},

	disableLastFilterLogicOptions: function($logicContainer) {
		var $logicSelectors = $logicContainer.find('.filterLogicSelect');
		// don't disable the "NOT" value for the first row
		if (!$logicSelectors.first().is($logicSelectors.last())) {
			$logicSelectors.last().find('.filterLogicSingle').prop('disabled', true);
		}
	},

	toggleFilterBody: function() {
		if (this.$('.filterBody').is(':visible')) {
			this.closeFilterBody();
		} else {
			this.openFilterBody();
		}
	},

	openFilterBody: function() {
		this.$('.filterToggle').removeClass('pe-is-i-angle-circle-down').addClass('pe-is-i-angle-circle-up');
		this.$('.filterBodyContainer').removeClass('closed');
	},

	onQueryReset: function() {
		this.onClearDataCart();
		EventBus.trigger("populateQueryBox");
	},

	closeFilterBody: function() {
		this.$('.filterToggle').removeClass('pe-is-i-angle-circle-up').addClass('pe-is-i-angle-circle-down');
		this.$('.filterBodyContainer').addClass('closed');
	},

	closeFilter: function() {
		EventBus.trigger('remove:visualFilter', this.model);
		EventBus.trigger("populateQueryBox");
		this.destroy();
	},

	showError: function(message) {
		$.ibisMessaging('primary', 'error', message, {
			container: '#filterError_' + this.model.get('id')
		});
		EventBus.trigger('filters:showError');
	},

	closeError: function(message) {
		$.ibisMessaging('close', {
			type: 'primary',
			container: '#filterError_' + this.model.get('id')
		});
		EventBus.trigger('filters:removeError');
	},

	/**
	 * Responds to clearing the data cart. All filters should be removed
	 * completely
	 */
	onClearDataCart: function() {
		$.ibisMessaging('close', {
			type: 'primary'
		});
		this.destroy();
	},

	onQueryRun: function(obj) {
		var notOnActivate = obj.notOnActivate;
		this.closeFilter(notOnActivate);
	},

	onChangeCombLogic: function() {
		var value = this.$('.filterLogic').val();
		this.model.set('logicBefore', value);
		EventBus.trigger("populateQueryBox");
	},
	
	updateBlanks: function(event){
		var $target = $(event.target);
		var showBlanksModel = $target.prop("checked") ? this.addShowBlanksFilter() : this.removeShowBlanksFilter();
		this.model.set('selectedBlank',showBlanksModel);
		EventBus.trigger("populateQueryBox");
	},
	
	updateMultiData: function(event){
		var $target = $(event.target);
		var dataBoolean = $target.prop("checked");
		this.model.set('multiData',dataBoolean);
		var childFilters = this.model.get("filters");
		if (childFilters instanceof Backbone.Collection) {
			childFilters.forEach(function(childFilter) {
				childFilter.set('multiData',dataBoolean);
			});
		}
		EventBus.trigger("populateQueryBox");
	},
	
	addShowBlanksFilter: function() {
		var model = this.model;
		var formUri = model.get('formUri');
		var formUriName = formUri.substring(formUri.lastIndexOf('/') + 1);
		var filterName = formUriName + '_' +  model.get('groupName') + '_' + model.get('elementName') + '_show_blanks';
		filterName = filterName.replace(/ /g,"$");

		var subModel = new QT.QueryFilter({
			filters: [],
			formUri: model.get('formUri'),
			elementUri: model.get('elementUri'),
			groupUri: model.get('groupUri'),
			elementName: model.get('elementName'),
			groupName: model.get('groupName'),
			filterType: 'SHOW_BLANKS',
			filterJavaType: 'SHOW_BLANKS',
			name: filterName,
			filterName: filterName
		});
		this.pushNewSpecialFilter(subModel);
		return subModel;
	},
	
	removeShowBlanksFilter: function() {
		var filterModelToRemove = this.model.get('selectedBlank');
		this.removeSpecialFilter(filterModelToRemove);
		EventBus.trigger("populateQueryBox");
		return false;
	},
	
	/**
	 * Fills in the on-screen elements for this filter for the parent
	 * container.
	 * 
	 * Called by the subclassed filters
	 */
	fillData : function() {
		// grouping before
		var model = this.model;
		var $el = this.$el;
		var groupingBefore = model.get("groupingBefore");
		if (groupingBefore == 3) {
			$el.find(".filterLogicBeforeTwo").prop("checked", true);
		}
		else if (groupingBefore == 2) {
			$el.find(".filterLogicBeforeOne").prop("checked", true);
		}
		
		// grouping after
		var groupingAfter = model.get("groupingAfter");
		if (groupingAfter == 3) {
			$el.find(".filterLogicAfterTwo").prop("checked", true);
		}
		else if (groupingAfter == 2) {
			$el.find(".filterLogicAfterOne").prop("checked", true);
		}
		
		// logic before
		$el.find(".filterLogic").val(model.get("logicBefore"));
	},
	/*
	 * Used by FilterEnumerateView, FilterOtherSpecifyView, FilterFreeFormView , FilterFreeFormLargeView
	 * 
	 */
	updateFilterMode: function(event) {
		var $target = $(event.target);
		var modeValue = $target.prop('checked') ? 'exact' : 'inclusive';
		this.getThisModel($target).set('mode', modeValue);
		EventBus.trigger("populateQueryBox");
	},

});
