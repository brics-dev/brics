/**
 * Handles all activity related to the datepickers and datetimepickers.
 */
var DatePickerController = {
	datePickers : [],
	dateTimePickers : [],
	
	/**
	 * All date fields should have a onclick dateinput to allow for standard JS
	 */
	initDatePickers : function() {
		$('input[type="date"], input[type="text"].dateField, textarea.dateField').each(function() {
			var tmpPicker = $(this).datepicker({
				dateFormat: "yy-mm-dd", // this corresponds to ex: 2012-4-30
				changeYear: true,
				yearRange: "1900:+10"
			});
			DatePickerController.datePickers.push(tmpPicker);
		});
		$('input[type="date"], input[type="text"].dateField').attr("placeholder","Format: YYYY-MM-DD");
	},
	
	// TODO: combine these initialization objects into one object and use in both date and datetime
	
	/**
	 * All datetime fields should be rendered as datetimepickers
	 * if the datetimepicker function exists, otherwise render 
	 * as datepicker.
	 * 
	 */
	initDateTimePickers : function() {
		if (!$.fn.datetimepicker) {
			this.translateDatePickers();
		}
		else {
			$('input[type="datetime"], input[type="text"].dateTimeField, textarea.dateTimeField').each(function() {
				var tmpPicker = $(this).datetimepicker({
					dateFormat: "yy-mm-dd", // this corresponds to ex: 2012-4-30
					timeFormat: "HH:mm",
					yearRange: "1900:+10",
					showMillisec: false
				});
				DatePickerController.dateTimePickers.push(tmpPicker);
			});
		}
		$('input[type="datetime"], input[type="text"].dateTimeField').attr("placeholder","Format: YYYY-MM-DD HH:MM");
		
	},
	
	initAllPickers : function() {
		this.initDateTimePickers();
		this.initDatePickers();
	},
	
	/**
	 * Change dateTime pickers over to datepickers.  This must happen
	 * BEFORE the datepickers are initialized.
	 * 
	 * ONLY called in the case of datetimepicker function DNE
	 */
	translateDatePickers : function() {
		$('input[type="datetime"]').each(function() {
			$(this).attr("type", "date");
		});
		$('input[type="text"].dateTimeField').each(function() {
			$(this).removeClass("dateTimeField");
			$(this).addClass("dateField");
		});
	},
	
	getAll : function() {
		return $.merge($.merge([], this.datePickers), this.dateTimePickers);
	},
	
	/**
	 * called by the window resize function.  Handles window resizing and
	 * repositioning.
	 */
	windowResizeHide : function() {
		var pickers = this.getAll();
		for (var i = 0; i < pickers.length; i++) {
			pickers[i].datepicker('hide');
			pickers[i].blur();
		}
	}
};