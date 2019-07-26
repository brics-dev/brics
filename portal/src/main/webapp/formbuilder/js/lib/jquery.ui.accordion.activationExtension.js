/**
 * Extends the jquery.ui.accordion plugin to facilitate using the accordion
 * as a vertical navigation element.  This is done by assuming the content
 * of the accordions are only links and extending the "navigation" feature
 * of the accordion plugin to assign a class to the link with href
 * equivalent to the current page's url.
 *
 * This extension also adds in the ability to specify the active tab and
 * sublink after loading in the event that the active page does not show up
 * within the navigation structure.
 */
var activationExtension = {
	_findWithinLinks : function(tabName) {
		for (var i = 0; i < this.headers.length; i++) {
			if (this.headers[i].id == tabName) return i;
		}
	},

	/**
	 * Activates a given tab via numeric index or tab ID
	 *
	 * @param tab the numerical Index of the tab to activate or ID of the tab
	 */
	activateTab : function(tab) {
		if (Validator.isNumeric(tab)) {
			// we are working with an index
			this.activate(tab);
		}
		else {
			// activate tab based on name
			var current = this.element.find("#"+tab);
			if (current.length) {
				this.activate(this._findWithinLinks(tab));
			}
		}
	},
	
	/**
	 * Activates a specified sublink
	 * side-effect: deactivates the currently active sublink before activating
	 *
	 * @param link the ID of the sublink to activate
	 */
	activateLink : function(link) {
		// we get an ID of the link to highlight
		this.deactivateCurrentSublink();
		var element = this.element.find("#"+link);
		var className = $(".ui-accordion").accordion("option", "activeSublinkClassName");
		element.addClass(className);
	},
	
	/**
	 * Deactivates the current active sublink (in css) so that another link can be
	 * activated with the correct class name
	 */
	deactivateCurrentSublink : function() {
		var className = $(".ui-accordion").accordion("option", "activeSublinkClassName");
		var current = this.element.find("a."+className).eq(0);
		
		current.removeClass(className);
	},
	
	/**
	 * Finds the PARTICULAR sublink that matches this page (rather than the header that the
	 * built in method finds) and gives it the class defined in this.activeSublinkClassName
	 */
	highlightCurrentSublink : function() {
		var current = this.element.find("a").filter(this.options.navigationFilter).eq(0);
		if (current.length) {
			var className = $(".ui-accordion").accordion("option", "activeSublinkClassName");
			current.addClass(className);
		}
	}
};
$.extend(true, $.ui.accordion.prototype, activationExtension);

