/**
 * 
 */
QT.FilterNumericRangeView = QT.GenericQueryFilterView.extend({
	
	events : {
		"click .filterToggle" : "toggleFilterBody",
		"click .filterClose" : "closeFilter"
	},
	
	
	initialize : function() {
		
		this.template = TemplateManager.getTemplate("filterNumericRange");
		QT.FilterNumericRangeView.__super__.initialize.call(this);
		this.listenTo(this.model,"change:selectedMinimum",this.onMinChange);
		this.listenTo(this.model,"change:selectedMaximum",this.onMaxChange);
	},
	
	
	render : function($container) {
		this.$el.html(this.template(this.model.attributes));
		$container.append(this.$el);
		QT.FilterNumericRangeView.__super__.render.call(this);
		this.populateFormStructureTitleAndElement();
		
		var minimum = this.model.get("minimum");
		var maximum = this.model.get("maximum");
		var selectedMin = this.model.get("selectedMinimum");
		var selectedMax = this.model.get("selectedMaximum");
		if(selectedMin === undefined) {
			selectedMin = minimum;
		}
		if(selectedMax === undefined) {
			selectedMax = maximum;
		}
		var validationText = "Between " + selectedMin + " and " + selectedMax;
		this.$(".filterNumericRangeValidation").html(validationText);
		
		
		var view = this;
		this.$(".filterNumericRangeSlider").slider({
			range : true,
			min : minimum,
			max : maximum,
			values : [selectedMin,selectedMax],
			
		   slide: function( event, ui ) {
		      var sliderMin = ui.values[ 0 ];
		      var sliderMax = ui.values[ 1 ];
			  view.model.set("selectedMinimum",sliderMin);
			  view.model.set("selectedMaximum",sliderMax);
			  
		    }

		});

		  this.model.set("selectedMinimum",selectedMin);
		  this.model.set("selectedMaximum",selectedMax);
		
		
	},
	
	onMinChange : function(changingModel) {
		this.closeError();
		if (this.validateMinimum(changingModel) && this.validateMaxMinDifference(changingModel)) {
			this.updateSlider();
		}
	},
	
	onMaxChange : function(changingModel) {
		this.closeError();
		if (this.validateMaximum(changingModel) && this.validateMaxMinDifference(changingModel)) {
			this.updateSlider();
		}
	},
	
	validateMinimum : function(changingModel) {
		var min = Number(changingModel.get("selectedMinimum"));
		var modelMin = changingModel.get("minimum");
		var validationMinimum = (typeof modelMin == "string" || typeof modelMin == "number") ? Number(modelMin) : null;
		if (typeof min != "undefined") {
			if (isNaN(min)) {
				this.model.set("selectedMinimum", '', {silent: true});
				this.$("input[name='selectedMinimum']").val('');
				return false;
			}
			
			if (validationMinimum != null && validationMinimum != undefined && min < validationMinimum) {
				var newMin = changingModel.previous("selectedMinimum");
				this.showError("The minimum cannot be set less than the filter's minimum value");
				this.model.set("selectedMinimum", newMin, {silent: true});
				this.$("input[name='selectedMinimum']").val(newMin);
				return false;
			}
		}
		// convert to a number
		this.model.set("selectedMinimum", min, {silent: true});
		return true;
	},
	
	validateMaximum : function(changingModel) {
		var max = Number(changingModel.get("selectedMaximum"));
		var modelMax = changingModel.get("maximum");
		var validationMax = (typeof modelMax == "string" || typeof modelMax == "number") ? Number(modelMax) : null;
		if (typeof max != "undefined") {
			if (isNaN(max)) {
				this.model.set("selectedMaximum", '', {silent: true});
				this.$("input[name='selectedMaximum']").val('');
				return false;
			}
			
			if (validationMax != null && validationMax != undefined && max > validationMax) {
				var newMax = this.model.get("maximum");
				this.showError("The maximum cannot be set greater than the filter's maximum value");
				this.model.set("selectedMaximum", newMax, {silent: true});
				this.$("input[name='selectedMaximum']").val(newMax);
				return false;
			}
		}
		this.model.set("selectedMaximum", max, {silent: true});
		return true;
	},
	
	validateMaxMinDifference : function(changingModel) {
		var min = changingModel.get("selectedMinimum");
		var max = changingModel.get("selectedMaximum");
		if (typeof min != "undefined" && max != "undefined") {
			if (min > max) {
				// revert whichever was changing
				if (typeof changingModel.changed.selectedMinimum != "undefined") {
					this.model.set("selectedMinimum", changingModel.previous("selectedMinimum"), {silent: true});
				}
				if (typeof changingModel.changed.selectedMaximum != "undefined") {
					this.model.set("selectedMaximum", changingModel.previous("selectedMaximum"), {silent: true});
				}

				// show a message
				this.showError("The minimum cannot be greater than the maximum");
				return false;
			}
		}
		return true;
	},
	
	updateSlider : function(model) {
		var min = this.model.get("selectedMinimum");
		if (typeof min == "undefined") {
			min = this.model.get("minimum");
		}
		var max = this.model.get("selectedMaximum");
		if (typeof max == "undefined") {
			max = this.model.get("maximum");
		}
		this.$(".filterNumericRangeSlider").slider("values",[min,max]);
		this.closeError();
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
		this.updateSlider();
	}
	
	
	
	
	
	
	
	
});


