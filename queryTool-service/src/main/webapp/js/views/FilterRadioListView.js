/**
 * 
 */
QT.FilterRadioListView = QT.GenericQueryFilterView.extend({
	events : {
		"click .filterCheckbox" : "updateSelectedPermissibleValues",
		"click .filterToggle" : "toggleFilterBody",
		"click .filterClose" : "closeFilter",
		"change .genericLogicSelect" : "onChangeGenericLogicSelect",
		"change .filterLogicSelect" : "onChangeFilterLogicSelect",
		"change .filterLogic" : "onChangeCombLogic",
		"change .filterLogicBeforeTwo,.filterLogicBeforeOne,.filterLogicAfterOne,.filterLogicAfterTwo" : "onChangeGroupingContainer",
		"click .includeBlanksCheckbox" : "updateBlanks",
	},
	
	initialize : function() {
		this.template = TemplateManager.getTemplate("filterRadioList");
		QT.FilterRadioListView.__super__.initialize.call(this);
	},
	
	render : function($container) {
		
		this.$el.html(this.logicBeforeTemplate() + this.genericTemplate(this.model.attributes) + this.logicAfterTemplate());
		$container.append(this.$el);
		
		this.onRenderHandleSubFilters();
		
		QT.FilterRadioListView.__super__.render.call(this);

		this.populateFormStructureTitleAndElement();
		
		
		
	},
	
	/**
	 * Renders a single sub-filter by model.  This handles all business logic for that
	 * rendering.
	 * 
	 * @param subFilterModel Model describing the sub-filter
	 */
	renderSubFilter : function(subFilterModel) {
		var $filterLogicContainer = this.$(".filterLogicContainer");
		// if this is the first sub-filter, the logic options "and", "or" should be disabled
		if (this.model.get("filters").length == 1) {
			subFilterModel.set('showFilterLogicOr',false);
			subFilterModel.set('showFilterLogicAnd',false);
		}
		var newFilterRow = this.template(subFilterModel.attributes);
		var jNewFilterRow = $(newFilterRow);
		
		
		var itemTemplate = TemplateManager.getTemplate("filterRadioItem");
		var permissibleValues = this.model.get("permissibleValues");
		var elementName = this.model.get("elementName");
		if(permissibleValues.length > 0) {
			for(var i=0;i<permissibleValues.length;i++) {
				var pVal = permissibleValues[i];
				jNewFilterRow.find(".radioList").append(itemTemplate({pVal:pVal,elementName: elementName}));
			}
		}
		

		$filterLogicContainer.find(".genericLogicSelect").before(jNewFilterRow);
	},
	updateSelectedPermissibleValues : function(event) {
		var $targetGrandParent = $(event.target).parent().parent(); //we need to get the grandparent because the grandparent class indicates the filter id better
		var selectedPermissibleValues = [];
		this.$(".filterCheckbox").each(function() {
			if(this.checked) {
				var val = $(this).val();
				selectedPermissibleValues.push(val);
			}	
		});
		this.getThisModel($targetGrandParent).set("selectedPermissibleValues",selectedPermissibleValues);
		EventBus.trigger("populateQueryBox");
	},
	
	/**
	 * Fills the on-page elements with values from this filter model (and sub-filters from
	 * their sub-models).
	 * 
	 * called from RefineDataFiltersView after initialize() and render() on this view
	 */
	fillData : function() {
		var model = this.model;
		QT.FilterRadioListView.__super__.fillData.call(this);
		var filters = model.get("filters");
		var $subFilters = this.$(".filterLogicRow");
		for (var i = 0, filterSize = filters.length; i < filterSize; i++) {
			var filter = filters.at(i);
			var permVals = filter.get("selectedPermissibleValues");
			var $subFilter = $subFilters.eq(i);
			if (permVals != null && permVals.length > 0) {
				for (var j = 0, permValLen = permVals.length; j < permValLen; j++) {
					$subFilter.find('.filterCheckbox[value="' + permVals[j] + '"]').prop("checked", true);
				}
			}
			$subFilter.find(".filterLogicSelect").val(filter.get("logicBefore"));
		}
	}
});