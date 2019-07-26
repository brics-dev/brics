/**
 * This model is used to represent a form in the data cart.  This is separate from a QT.Form
 * because this is intended to be VERY lightweight.  The studies list here is an array of
 * strings instead of the full reference list to all studies linked to the form.  That's
 * because the form may contain many studies that should NOT be included in the data cart.
 * Also, we don't want to reference the whole study so the list is only strings.
 * 
 * This lets us have a very small form reference for the data cart while keeping the nice
 * main form object able to have whatever properties we want.
 */
QT.DataCartForm = BaseModel.extend({
	idAttribute: "uri",
	defaults : {
		uri 	: "",	// URI of the form
		title 	: "",	// title of the form
		studies : [],	// list of study URIs (string) included in the cart
		position: 0		// if first = 1, if second = 2, if third = 3, fourth = 4, fifth = 5 else 0
						// note: if form is singly-selected: position = 1
	},
	
	initialize : function() {
		this.set("studies", []);
	},
	
	fromForm : function(formModel) {
		this._copyAttribute("uri", formModel);
		this._copyAttribute("title", formModel);
		// TODO: copy over only the studies that are currently selected
	},
	
	/**
	 * Copies an attribute from another model to this.
	 * NOTE: copies by reference (all but primitives) so should be modified if needing
	 * to copy any Object
	 */
	_copyAttribute : function(attribute, copyFromModel, otherModelAttributeName) {
		if (typeof otherModelAttributeName !== "undefined") {
			this.set(attribute, copyFromModel.get(otherModelAttributeName));
		}
		else {
			this.set(attribute, copyFromModel.get(attribute));
		}
	}

	// toJson works fine here but if it needs to be overridden, no problem
});