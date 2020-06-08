/**
 * 
 */

SavedQueryViewEngine.SavedQueryStudy = BaseModel.extend({
	forms : null,
	
	defaults : {
		title : "",
		uri : "",
		formCount : 0,
		id : 0,
		active : false
	},
	
	initialize : function() {
		this.forms = new SavedQueryViewEngine.SavedQueryForms();
		
		EventBus.on("change:activeStudy", this.checkActive, this);
	},
	
	loadData : function(dataObj) {
		this.set("title", dataObj.title);
		this.set("uri", dataObj.uri);
		this.set("id", dataObj.id);
	},
	
	addForm : function(form) {
		this.forms.add(form);
	},
	
	load : function() {
		// we already have all forms for this study (see: SavedQuery.js:load)
//		this.forms.forEach(function(form) {
//			form.load();
//		});
	},
	
	/**
	 * Determines if:															DO THIS
	 * 	This model is the one needing activation and it is not already active	activate
	 *  This model is not needing activation and it is active					deactivate
	 */
	checkActive : function(changeModel) {
		var active = this.get("active");
		if (changeModel.cid == this.cid && !active) {
			this.set("active", true);
		}
		else if (changeModel.cid != this.cid && active) {
			this.set("active", false);
		}
	}
});