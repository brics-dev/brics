/**
 * 
 */
QT.FilterOtherSpecifyView = QT.GenericQueryFilterView.extend({
	events: {
		'click .filterCheckbox': 'updateSelectedPermissibleValues',
		'click .filterToggle': 'toggleFilterBody',
		'click .filterClose': 'closeFilter',
		'change .otherSpecifyCheckbox': 'changeOtherSpecifyOption',
		'change .genericLogicSelect': 'onChangeGenericLogicSelect',
		'change .filterLogicSelect': 'onChangeFilterLogicSelect',
		'click .filterMode': 'updateFilterMode',
		'change .filterLogic': 'onChangeCombLogic',
		'change .filterLogicBeforeTwo,.filterLogicBeforeOne,.filterLogicAfterOne,.filterLogicAfterTwo':'onChangeGroupingContainer',
		"click .includeBlanksCheckbox" : "updateBlanks",
		"keyup .filterFreeFormTextBox" : "updateOtherSpecifyFreeForm"
	},

	otherSpecify: 'Other, specify',

	initialize: function() {
		this.template = TemplateManager.getTemplate('filterOtherSpecify');
		QT.FilterOtherSpecifyView.__super__.initialize.call(this);
	},

	render: function($container) {
		this.$el.html(
			this.logicBeforeTemplate() + this.genericTemplate(this.model.attributes) + this.logicAfterTemplate()
		);
		$container.append(this.$el);
		this.onRenderHandleSubFilters();
		QT.FilterOtherSpecifyView.__super__.render.call(this);
		this.populateFormStructureTitleAndElement();
	},

	/**
	 * Renders a single sub-filter by model.  This handles all business logic for that
	 * rendering.
	 * 
	 * @param subFilterModel Model describing the sub-filter
	 */
	renderSubFilter: function(subFilterModel,displaySelection,updateBoolean) {
		var $filterLogicContainer = this.$('.filterLogicContainer');
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

		var itemTemplate = TemplateManager.getTemplate('filterEnumeratedItem');
		var otherSpecifyTemplate = TemplateManager.getTemplate('filterOtherSpecifyOption');
		var permissibleValues = this.model.get('permissibleValues');
		var otherSpecifyText = false;
		if(permissibleValues.length > 0) {
			if(displaySelection == "Permissible Value Description"){
				var allDescriptions = [];
				for(var i=0;i<permissibleValues.length;i++) {
					var pVal = permissibleValues[i].pvalueDescription;
					if(!allDescriptions.includes(pVal)){
						// wait until the end to draw other, specify
						if (pVal === this.otherSpecify) {
							otherSpecifyText = pVal;
						} else {
							jNewFilterRow.find('.enumeratedList').append(itemTemplate({ pVal: pVal }));
						}
						allDescriptions.push(pVal);
					}
				}
			}else {
				for(var i=0;i<permissibleValues.length;i++) {
					var pVal = permissibleValues[i].pvalueLiteral;
					// wait until the end to draw other, specify
					if (pVal === this.otherSpecify) {
						otherSpecifyText = pVal;
					} else {
						jNewFilterRow.find('.enumeratedList').append(itemTemplate({ pVal: pVal }));
					}
				}
			}
			if (otherSpecifyText) {
				jNewFilterRow.find('.enumeratedList').append(otherSpecifyTemplate({ pVal: otherSpecifyText }));

				var freeFormValue = this.model.get('freeFormValue') == undefined ? '' : this.model.get('freeFormValue');

				if (freeFormValue.length > 0) {
					jNewFilterRow.find('.otherSpecifyCheckbox').prop('checked', true);
					jNewFilterRow.find('.filterFreeFormTextBox').removeAttr('disabled');
				}
			}
		}

		$filterLogicContainer.find('.genericLogicSelect').before(jNewFilterRow);
	},

	updateSelectedPermissibleValues: function(event) {
		var $targetGrandParent = $(event.target).parent().parent(); //we need to get the grandparent because the grandparent class indicates the filter id better
		var selectedPermissibleValues = [];
		var view = this;
		this.$('.filterCheckbox').each(function() {
			if (this.checked) {
				var val = $(this).val();
				// don't include "other, specify" in the permissible values list but
				// the text inside it is passed to back end for mixing
				if (val != view.otherSpecify && val != "") {
					selectedPermissibleValues.push(val);
				}
			}
		});
		
		var selectedPermissibleValuesValue = this.setSelectedPermissibleValues();//(selectedPermissibleValues.length > 0) ? selectedPermissibleValues : null;
		this.getThisModel($targetGrandParent).set("selectedPermissibleValues",selectedPermissibleValuesValue);
		this.model.set('selectedPermissibleValues', selectedPermissibleValues);
		EventBus.trigger("populateQueryBox");
	},
	setSelectedPermissibleValues : function(){
		var selectedPermissibleValues = [];
		var view = this;
		this.$('.filterCheckbox').each(function() {
			if (this.checked) {
				var val = $(this).val();
				// don't include "other, specify" in the permissible values list but
				// the text inside it is passed to back end for mixing
				if (val != view.otherSpecify && val != "") {
					selectedPermissibleValues.push(val);
				}
			}
		});
		
		var selectedPermissibleValuesValue = (selectedPermissibleValues.length > 0) ? selectedPermissibleValues : null;
		return selectedPermissibleValuesValue;
	},
	
	changeOtherSpecifyOption: function(event) {
		var $targetGrandParent = $(event.target).parent().parent(); //we need to get the grandparent because the grandparent class indicates the filter id better
		var subModel = this.getThisModel($targetGrandParent);
		if ($(event.target).is(':checked')) {
			this.$('.filterFreeFormTextBox').prop('disabled', false);
			subModel.set('selectedFreeFormValue', '');
			subModel.set('freeFormValue', '');
			this.model.set('selectedFreeFormValue', '');
			this.model.set('freeFormValue', '');
		} else {
			this.$('.filterFreeFormTextBox').prop('disabled', true).val('');
			$(event.target).val('');
			subModel.set('selectedFreeFormValue', '');
			subModel.set('freeFormValue', '');
			this.model.set('selectedFreeFormValue', '');
			this.model.set('freeFormValue', '');
		}
		EventBus.trigger("populateQueryBox");
	},
	
	updateOtherSpecifyFreeForm : function(event) {
		var $target = $(event.target);
		var formValue = $(event.target).val();
		var index = this.$(".filterFreeFormTextBox").index($target);
		var selectedPermissibleValuesValue = this.setSelectedPermissibleValues();

		if(selectedPermissibleValuesValue){
			if(formValue != ""){
				selectedPermissibleValuesValue.push(formValue);
			}
		} else {
			if(formValue != ""){
				selectedPermissibleValuesValue = [formValue];
			}
		}
		this.getSubFilterAt(index).set("selectedPermissibleValues",selectedPermissibleValuesValue);
		this.getSubFilterAt(index).set("selectedFreeFormValue", formValue);
		EventBus.trigger("populateQueryBox");
	},

	/**
	 * Fills the on-page elements with values from this filter model (and sub-filters from
	 * their sub-models).
	 * 
	 * called from RefineDataFiltersView after initialize() and render() on this view
	 */
	fillData: function() {
		var model = this.model;
		QT.FilterOtherSpecifyView.__super__.fillData.call(this);
		var filters = model.get("filters");
		var $subFilters = this.$(".filterLogicRow");
		for (var i = 0, filterSize = filters.length; i < filterSize; i++) {
			var filter = filters.at(i);
			var permVals = filter.get('selectedPermissibleValues');
			var $subFilter = $subFilters.eq(i);
			if (permVals != null && permVals.length > 0) {
				for (var j = 0, permValLen = permVals.length; j < permValLen; j++) {
					$subFilter.find('.filterCheckbox[value="' + permVals[j] + '"]').prop('checked', true);
				}
			}
			$subFilter.find(".filterLogicSelect").val(filter.get("logicBefore"));
		}
	}
});
