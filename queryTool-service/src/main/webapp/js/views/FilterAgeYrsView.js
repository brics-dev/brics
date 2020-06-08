/**
 * 
 */
QT.FilterAgeYrsView = QT.GenericQueryFilterView.extend({
	events : {
		"click .filterCheckbox" : "updateSelectedPermissibleValues",
		"click .filterToggle" : "toggleFilterBody",
		"click .filterClose" : "closeFilter",
		"change .filterLogic" : "onChangeCombLogic",
		"change .genericLogicSelect" : "onChangeGenericLogicSelect",
		"change .filterLogicSelect" : "onChangeFilterLogicSelect",
		"click .filterMode" : "updateFilterMode",
		"change .filterLogicBeforeTwo,.filterLogicBeforeOne,.filterLogicAfterOne,.filterLogicAfterTwo" : "onChangeGroupingContainer",
		"click .includeBlanksCheckbox" : "updateBlanks",
		"click .multiRaceCheckBox" : "updateMultiData",

	},
	
	initialize : function() {
		this.template = TemplateManager.getTemplate("filterEnumeratedList");
		QT.FilterAgeYrsView.__super__.initialize.call(this);
	},
	
	render : function($container) {
		this.$el.html(this.logicBeforeTemplate() + this.genericTemplate(this.model.attributes) + this.logicAfterTemplate());
		
		$container.append(this.$el);
		this.onRenderHandleSubFilters();
		
		QT.FilterAgeYrsView.__super__.render.call(this);
		
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
		
		
		var itemTemplate = TemplateManager.getTemplate("filterEnumeratedItem");
		var permissibleValues = ["0 - 9","10 - 19","20 - 29","30 - 39","40 - 49","50 - 59","60 - 69","70 - 79","80 - 89","90 - 150"];
		if(permissibleValues.length > 0) {
			for(var i=0;i<permissibleValues.length;i++) {
				var pVal = permissibleValues[i];
				var itemlist = itemTemplate({pVal:pVal});
				jNewFilterRow.find(".enumeratedList").append(itemlist);
			}
		}
		
		jNewFilterRow.find('label:contains("90 - 150")').text("90+");
		
		$filterLogicContainer.find(".genericLogicSelect").before(jNewFilterRow);
		
	},
	updateFilterMode : function(event) {
		var $target = $(event.target);
		var modeValue = $target.prop("checked") ? "exact" : "inclusive";
		this.getThisModel($target).set("mode", modeValue);
		EventBus.trigger("populateQueryBox");
	},
	
	updateSelectedPermissibleValues : function(event) {
		var $targetGrandParent = $(event.target).parent().parent(); //we need to get the grandparent because the grandparent class indicates the filter id better
		var selectedPermissibleValues = [];
		$targetGrandParent.find(".filterCheckbox").each(function() {
			if(this.checked) {
				var val = $(this).val();
				selectedPermissibleValues.push(val);
			}	
		});
		
		var selectedPermissibleValuesValue = (selectedPermissibleValues.length > 0) ? selectedPermissibleValues : null;
		
		this.getThisModel($targetGrandParent).set("selectedPermissibleValues",selectedPermissibleValuesValue);
		EventBus.trigger("populateQueryBox");
	},
	
	/**
	 * Fills the on-page elements with values from this filter model (and sub-filters from
	 * their sub-models).
	 * 
	 * called from RefineDataFiltersView after initialize() and render() on this view
	 */
	fillData : function() {
		// TODO: translate selected range back into checkbox values
		var model = this.model;
		QT.FilterAgeYrsView.__super__.fillData.call(this);
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