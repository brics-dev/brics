/**
 * 
 */
QT.FilterEnumeratedListView = QT.GenericQueryFilterView.extend({
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
		QT.FilterEnumeratedListView.__super__.initialize.call(this);
	},
	
	render : function($container) {
		this.$el.html(this.logicBeforeTemplate() + this.genericTemplate(this.model.attributes) + this.logicAfterTemplate());
		
		//this.$el.html(this.template(this.model.attributes));
		$container.append(this.$el);
		this.onRenderHandleSubFilters();
		
		//ensure the DE needs the multiRace Checkbox
		var index = Config.multiRaceDEs.indexOf(this.model.get("elementName"));
		if(index != -1){
			var multiRaceTemplate = TemplateManager.getTemplate("multiRaceCheckBox");
			this.$el.find(".showBlanks").after(multiRaceTemplate);
		}
		
		QT.FilterEnumeratedListView.__super__.render.call(this);
		
		this.populateFormStructureTitleAndElement();
		
	},
	/**
	 * Renders a single sub-filter by model.  This handles all business logic for that
	 * rendering.
	 * 
	 * @param subFilterModel Model describing the sub-filter
	 */
	renderSubFilter : function(subFilterModel,displaySelection,updateBoolean) {
		var $filterLogicContainer = this.$(".filterLogicContainer");
		// if this is the first sub-filter, the logic options "and", "or" should be disabled
		if (this.model.get("filters").length == 1) {
			subFilterModel.set('showFilterLogicOr',false);
			subFilterModel.set('showFilterLogicAnd',false);
		}
		var newFilterRow = this.template(subFilterModel.attributes);
		var jNewFilterRow = $(newFilterRow);
		if(updateBoolean){
			$filterLogicContainer.find(".filterLogicRow").remove();
		}
		var $filterRowList = jNewFilterRow.find(".enumeratedList");
		
		var itemTemplate = TemplateManager.getTemplate("filterEnumeratedItem");
		var permissibleValues = this.model.get("permissibleValues");
		if(permissibleValues.length > 0) {
			if(displaySelection == "Permissible Value Description"){
				for(var i=0;i<permissibleValues.length;i++) {
					var pv = permissibleValues[i];
					var pVal = pv.pvalueDescription;
						var itemlist = itemTemplate({pVal:pVal});
						$filterRowList.append(itemlist);
						if((subFilterModel.get("selectedPermissibleValues") != null) &&
							(subFilterModel.get("selectedPermissibleValues").indexOf(pv.pvalueLiteral) > -1 || 
							subFilterModel.get("selectedPermissibleValues").indexOf(pv.pvalueDescription) > -1)){
							
							$filterRowList.find(".filterCheckbox").eq(i).prop("checked", true);
						}
				}
			}else {
				for(var i=0;i<permissibleValues.length;i++) {
					var pv = permissibleValues[i];
					var pVal = pv.pvalueLiteral;
						var itemlist = itemTemplate({pVal:pVal});
						$filterRowList.append(itemlist);
						if((subFilterModel.get("selectedPermissibleValues") != null) &&
							(subFilterModel.get("selectedPermissibleValues").indexOf(pv.pvalueLiteral) > -1 || 
							subFilterModel.get("selectedPermissibleValues").indexOf(pv.pvalueDescription) > -1)){
						
							$filterRowList.find(".filterCheckbox").eq(i).prop("checked", true);
					}
				}
			}
		}
		
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
		var model = this.model;
		QT.FilterEnumeratedListView.__super__.fillData.call(this);
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