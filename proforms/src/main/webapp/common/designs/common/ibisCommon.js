/**
 * Validation class.  Allows javascript to validate input as needed
 */
var Validator = {
		isNumeric : function(n) {
			return !isNaN(parseFloat(n)) && isFinite(n);
		}
	};