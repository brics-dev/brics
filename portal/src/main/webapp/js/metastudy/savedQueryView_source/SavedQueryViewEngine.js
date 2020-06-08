
/**
 * 
 */
var SavedQueryViewEngine = {};


var SQViewProcessor = {
	savedQueryModel : null,
	sqView : null,
	
	initialize : function() {
		TemplateManager.initPartials();
		this.sqView = new SavedQueryViewEngine.SavedQueryView();
		EventBus.on("destroy:all", this.destroy, this);
	},
		
	/**
	 * Loads a new saved query from objects into the view
	 * 
	 * @param sqObject
	 */
	loadSavedQuery : function() {
		// sqObject is loaded in the JSP
		this.savedQueryModel = new SavedQueryViewEngine.SavedQuery();
		
		var jsonText = $("#savedQueryData").text().replace(/%20/g, " ").replace(/&quot;/g, "\"").trim();
		var sqObject = $.parseJSON(jsonText);
		
		this.savedQueryModel.load(sqObject);
		this.sqView.model = this.savedQueryModel;
	},
	
	render : function() {
		if (this.savedQueryModel === null) {
			this.initialize();
			this.loadSavedQuery();
		}
		
		this.sqView.render();
	},
	
	destroy : function() {
		this.savedQueryModel = null;
		this.sqView = null;
	}
};