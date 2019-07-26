/**
 * 
 */
QT.FilterDateRangeView = QT.GenericQueryFilterView.extend({
	
	events : {
		"click .filterToggle" : "toggleFilterBody",
		"click .filterClose" : "closeFilter"
	},
	
	initialize : function() {
		this.template = TemplateManager.getTemplate("filterDateRange");
		QT.FilterDateRangeView.__super__.initialize.call(this);	
	},
	
	
	render : function($container) {
		
		this.$el.html(this.template(this.model.attributes));
		$container.append(this.$el);
		QT.FilterDateRangeView.__super__.render.call(this);
		this.populateFormStructureTitleAndElement();
		
		this.$("#filterDateMinTextBox").datepicker({
				dateFormat : "m/d/y"
		});
		this.$("#filterDateMaxTextBox").datepicker({
				dateFormat : "m/d/y"
		});
	
		
	},
	
	fillData : function() {
		var dateMin = this.model.get("selectedDateMin");
		var dateMax = this.model.get("selectedDateMax");
		if (dateMin) {
			this.$("#filterDateMinTextBox").val(dateMin);
		}
		if (dateMax) {
			this.$("#filterDateMaxTextBox").val(dateMax);
		}
	}
});