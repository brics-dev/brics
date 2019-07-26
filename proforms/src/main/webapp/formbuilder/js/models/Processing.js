/**
 * 
 */
var Processing = BaseModel.extend({
	defaults : {
		determinate : false,	// is the value a number? 
		progressBar : null,		// reference to the progress bar on the page
		value		: false 	// the value currently displayed
	},
	
	setTo : function(value) {
		if (value != -1) {
			this.set("value", value);
			this.set("determinate", true);
		}
		else {
			this.set({
				determinate: false,
				value: false
			});
		}
	}
});