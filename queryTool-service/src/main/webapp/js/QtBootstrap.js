/**
 * The bootstrap for the query tool.  Kicks off the initialize and render process.
 * Stores the base instances of the core classes
 */
QueryTool = {
	page : null,
	pageView : null,
	dataCart : null,
	query : null,
	dataTable : null,
	
	/**
	 * Sets up the page to be rendered including pre-processing templates and
	 * setting up PageView.
	 * 
	 * usage: QueryTool.initialize();
	 */
	initialize : function() {
		TemplateManager.initPartials();
		this.page = new QT.Page();
		this.pageView = new QT.PageView({model: this.page});
		this.dataCart = this.page.get("dataCart");
		this.query = this.page.get("query");
		
		
		return this;
	},
	
	/**
	 * Begins the process of rendering the tool's initial elements such
	 * as the "working area".  Will call initialize() if it has not been run.
	 * 
	 * Usage: QueryTool.render();
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