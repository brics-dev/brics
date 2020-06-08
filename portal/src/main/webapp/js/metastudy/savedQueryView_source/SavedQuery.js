/**
 * Used in the saved query dialog, NOT the selection list
 */
SavedQueryViewEngine.SavedQuery = BaseModel.extend({
	studies : null,
	forms : null,
	
	defaults : {
		name : "",
		description : "",
		lastUpdated : "",
		formCount : 0,
		filtered: false
	},
	
	/**
	 * Replaces the current savedQuery 
	 */
	load : function(sqObject) {
		this.studies = new SavedQueryViewEngine.SavedQueryStudies();
		this.forms = new SavedQueryViewEngine.SavedQueryForms();
		
		this.set("name", sqObject.name);
		this.set("description", sqObject.description);
		this.set("lastUpdated", sqObject.lastUpdated);
		this.set("formCount", sqObject.forms.length);
		
		var studies = sqObject.studies;
		var forms = sqObject.forms;
		
		for (var i = 0; i < sqObject.studies.length; i++) {
			var study = new SavedQueryViewEngine.SavedQueryStudy();
			study.loadData(studies[i]);
			this.studies.add(study);
		}
		
		for (var j = 0; j < sqObject.forms.length; j++) {
			var form = new SavedQueryViewEngine.SavedQueryForm();
			form.loadData(forms[j]);
			this.forms.add(form);
			
			var studyIds = form.get("studyIds");
			for (var k = 0; k < studyIds.length; k++) {
				var studyId = studyIds[k];
				var study = this.studies.getById(studyId);
				if (study != null) {
					study.addForm(form);
					study.set("formCount", study.get("formCount") + 1);
					if (form.get("filtered") == true) {
						study.set("filtered", true);
					}
				}
			}
			
		}
		
		// now that everything is set up, tell the studies to load
		this.studies.forEach(function(study) {
			study.load();
		});
	}
});