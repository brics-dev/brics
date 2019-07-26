/**
 * 
 */
var FormBuilder = {
	page : null,
	pageView : null,
	form : null,
	
	/**
	 * Sets up the page to be rendered including pre-processing templates and
	 * setting up PageView.
	 * 
	 * usage: FormBuilder.initialize();
	 */
	initialize : function() {
		TemplateManager.initPartials();
		this.page = new Page();
		this.pageView = new PageView({model: this.page});
		this.form = this.page.get("form");
		return this;
	},
	
	/**
	 * Begins the process of rendering the builder's initial elements such
	 * as the "working area".  Will call initialize() if it has not been run.
	 * 
	 * Usage: FormBuilder.render();
	 */
	render : function() {
		if (this.page === null) {
			this.initialize();
		}
		this.pageView.render();
		
		// this is a function in the global list (header)
		if (typeof clearLogoutTimeout !== "undefined") {
			clearLogoutTimeout();
		}
	}
}