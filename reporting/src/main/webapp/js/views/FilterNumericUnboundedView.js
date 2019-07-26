/**
 * 
 */
QT.FilterNumericUnboundedView = QT.GenericQueryFilterView.extend({
	events : {
		"click .filterToggle" : "toggleFilterBody",
		"click .filterClose" : "closeFilter"
	},

	initialize : function() {
		this.template = TemplateManager.getTemplate("filterNumericUnbounded");
		QT.FilterNumericUnboundedView.__super__.initialize.call(this);
		
		this.listenTo(this.model, "change:selectedMinimum", this.validateNumber);
		this.listenTo(this.model, "change:selectedMaximum", this.validateNumber);
	},
	
	render : function($container) {
		this.$el.html(this.template(this.model.attributes));
		$container.append(this.$el);
		QT.FilterNumericUnboundedView.__super__.render.call(this);
		this.populateFormStructureTitleAndElement();
	},
	
	validateMinimum : function(changingModel) {
		var min = Number(changingModel.get("selectedMinimum"));
		if (typeof min != "undefined") {
			if (isNaN(min)) {
				var newMin = changingModel.previous("selectedMinimum");
				this.model.set("selectedMinimum", newMin, {silent: true});
				this.$(".filterNumericRangeMinTextBox").val(newMin);
				return false;
			}
		}
		return true;
	},
	
	validateMaximum : function(changingModel) {
		var max = Number(changingModel.get("selectedMaximum"));
		if (typeof max != "undefined") {
			if (isNaN(max)) {
				var newMax = changingModel.previous("selectedMaximum");
				this.model.set("selectedMaximum", newMax, {silent: true});
				this.$(".filterNumericRangeMaxTextBox").val(newMax);
				return false;
			}
		}
		return true;
	},
	
	validateNumber : function(changingModel) {
		this.closeError();
		var validMax = this.validateMaximum(changingModel);
		var validMin = this.validateMinimum(changingModel);
		if (validMax && validMin) {
			if(Number(changingModel.get("selectedMinimum")) > Number(changingModel.get("selectedMaximum"))) {
				var message = "Minimum cannot be greater than maximum";
				this.showError(message);
			}
		}
	},
	
	fillData : function() {
		var min = this.model.get("selectedMinimum");
		var max = this.model.get("selectedMaximum");
		
		if (min) {
			this.$(".filterNumericRangeMinTextBox").val(min);
		}
		
		if (max) {
			this.$(".filterNumericRangeMaxTextBox").val(max);
		}
	}

});


