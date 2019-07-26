/**
 * 
 */
QT.FilterRadioListView = QT.GenericQueryFilterView.extend({
	events : {
		"click .filterCheckbox" : "updateSelectedPermissibleValues",
		"click .filterToggle" : "toggleFilterBody",
		"click .filterClose" : "closeFilter"
	},
	
	initialize : function() {
		this.template = TemplateManager.getTemplate("filterRadioList");
		QT.FilterRadioListView.__super__.initialize.call(this);
	},
	
	render : function($container) {
		
		this.$el.html(this.template(this.model.attributes));
		$container.append(this.$el);
		QT.FilterRadioListView.__super__.render.call(this);
		
		this.populateFormStructureTitleAndElement();
		var itemTemplate = TemplateManager.getTemplate("filterRadioItem");
		var permissibleValues = this.model.get("permissibleValues");
		var elementName = this.model.get("elementName");
		if(permissibleValues.length > 0) {
			for(var i=0;i<permissibleValues.length;i++) {
				var pVal = permissibleValues[i];
				this.$(".radioList").append(itemTemplate({pVal:pVal,elementName: elementName}));
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