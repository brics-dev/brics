/**
 * 
 */
QT.FilterOtherSpecifyView = QT.GenericQueryFilterView.extend({
	events : {
		"click .filterCheckbox" : "updateSelectedPermissibleValues",
		"click .filterToggle" : "toggleFilterBody",
		"click .filterClose" : "closeFilter",
		"change .otherSpecifyCheckbox" : "changeOtherSpecifyOption"
	},
	
	otherSpecify : "Other, specify",
	
	initialize : function() {
		this.template = TemplateManager.getTemplate("filterOtherSpecify");
		QT.FilterEnumeratedListView.__super__.initialize.call(this);
	},
	
	render : function($container) {
		
		this.$el.html(this.template(this.model.attributes));
		$container.append(this.$el);
		
		this.populateFormStructureTitleAndElement();
		var itemTemplate = TemplateManager.getTemplate("filterEnumeratedItem");
		var otherSpecifyTemplate = TemplateManager.getTemplate("filterOtherSpecifyOption");
		var permissibleValues = this.model.get("permissibleValues");
		var otherSpecifyText = false;
		if(permissibleValues.length > 0) {
			for(var i=0;i<permissibleValues.length;i++) {
				var pVal = permissibleValues[i];
				// wait until the end to draw other, specify
				if (pVal === this.otherSpecify) {
					otherSpecifyText = pVal;
				}
				else {
					this.$(".enumeratedList").append(itemTemplate({pVal:pVal}));
				}
			}
			if (otherSpecifyText) {
				this.$(".enumeratedList").append(otherSpecifyTemplate({pVal:otherSpecifyText}));
				
				var freeFormValue = this.model.get('freeFormValue') == undefined ? '' : this.model.get('freeFormValue');
				
				if(freeFormValue.length > 0) {
					this.$('.otherSpecifyCheckbox').prop("checked", true);
					this.$('.filterFreeFormTextBox').removeAttr('disabled');
				}
			}
		}
		
		QT.FilterEnumeratedListView.__super__.render.call(this);
	},
	
	updateSelectedPermissibleValues : function() {
		var selectedPermissibleValues = [];
		var view = this;
		this.$(".filterCheckbox").each(function() {
			if(this.checked) {
				var val = $(this).val();
				// don't include "other, specify" in the permissible values list but
				// the text inside it is passed to back end for mixing
				if (val != view.otherSpecify) {
					selectedPermissibleValues.push(val);
				}
			}	
		});
		this.model.set("selectedPermissibleValues",selectedPermissibleValues);
	},
	
	changeOtherSpecifyOption : function(event) {
		if ($(event.target).is(":checked")) {
			this.$(".filterFreeFormTextBox").prop("disabled", false);
			this.model.set("selectedFreeFormValue", "");
			this.model.set("freeFormValue", "");
		}
		else {
			this.$(".filterFreeFormTextBox").prop("disabled", true).val("");
			this.model.set("selectedFreeFormValue", "");
			this.model.set("freeFormValue", "");
		}
	},
	
	fillData : function() {
		var permVals = this.model.get("selectedPermissibleValues");
		if (permVals != null && permVals.length > 0) {
			for (var i = 0; i < permVals.length; i++) {
				this.$('.filterCheckbox[value="' + permVals[i] + '"]').prop("checked", true);
			}
		}
	}
});