/**
 * 
 */
QT.FilterEnumeratedListView = QT.GenericQueryFilterView.extend({
	events : {
		"click .filterCheckbox" : "updateSelectedPermissibleValues",
		"click .filterToggle" : "toggleFilterBody",
		"click .filterClose" : "closeFilter"
	},
	
	initialize : function() {
		this.template = TemplateManager.getTemplate("filterEnumeratedList");
		QT.FilterEnumeratedListView.__super__.initialize.call(this);
	},
	
	render : function($container) {
		
		this.$el.html(this.template(this.model.attributes));
		$container.append(this.$el);
		QT.FilterEnumeratedListView.__super__.render.call(this);
		
		this.populateFormStructureTitleAndElement();
		var itemTemplate = TemplateManager.getTemplate("filterEnumeratedItem");
		var permissibleValues = this.model.get("permissibleValues");
		if(permissibleValues.length > 0) {
			for(var i=0;i<permissibleValues.length;i++) {
				var pVal = permissibleValues[i];
				this.$(".enumeratedList").append(itemTemplate({pVal:pVal}));
			}
		}
	},
	
	updateSelectedPermissibleValues : function() {
		var selectedPermissibleValues = [];
		this.$(".filterCheckbox").each(function() {
			if(this.checked) {
				var val = $(this).val();
				selectedPermissibleValues.push(val);
			}	
		});
		this.model.set("selectedPermissibleValues",selectedPermissibleValues);
		
		
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