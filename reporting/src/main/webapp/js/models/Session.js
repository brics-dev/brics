/**
 * 
 */
QT.Session = BaseModel.extend({
	defaults : {
		// don't put the stored session in here.  We want it to be garbage collected
	},
	
	key : "session",
	
	initialize : function() {
		
	},
	
	isSessionAvailable : function() {
		var session = this.getSession();
		return session != null 
			&& typeof session.forms !== "undefined" 
			&& session.forms.length > 0;
	},
	
	/**
	 * Get session from the sessionStorage
	 * 
	 * @return session data in saved query format
	 */
	getSession : function() {
		return JSON.parse(sessionStorage.getItem(this.key));
	},
	
	/**
	 * Gets the page configuration and stores it to the session storage
	 */
	setSession : function() {
		var data = this.pageToSessionData();
		data = JSON.stringify(data);
		sessionStorage.setItem(this.key, data);
	},
	
	emptyStorage : function() {
		sessionStorage.removeItem(this.key);
	},
	
	emptyAllStorage : function() {
		sessionStorage.clear();
	},
	
	/**
	 * Converts the current page configuration into the session/saved query format.
	 * 
	 * @return session storage format of the current page state
	 */
	pageToSessionData : function() {
		var session = {
			selectedFormURIList: [],
			forms : [],
			studies : []
		};
		
		// metadata
		session.selectedFormURIList = QueryTool.query.get("selectedForms");
		
		// forms
		var forms = QueryTool.dataCart.forms;
		forms.each(function(form) {
			var sessionForm = {
				uri : form.get("uri")
			};
			var formStudies = [];
			var formFilters = [];
			
			var studies = form.get("studies"); // URIs 
			for (var i = 0, studiesLength = studies.length; i < studiesLength; i++) {
				var study = QueryTool.page.get("studies").get(studies[i]);
				formStudies.push(study.get("studyId"));
			}
			sessionForm["studyIds"] = formStudies;
			
			var filters = QueryTool.query.filters;
			for (var j = 0, filtersLength = filters.length; j < filtersLength; j++) {
				var filter = filters.at(j);
				// only add filters to the correct form
				if (form.get("uri") == filter.get("formUri")) {
					formFilters.push({
						formUri : filter.get("formUri"),
						elementUri : filter.get("elementUri"),
						groupUri : filter.get("groupUri"),
						freeFormValue : filter.get("selectedFreeFormValue") || "",
						blank : filter.get("selectedBlank"),
						minimum : filter.get("selectedMinimum"),
						maximum : filter.get("selectedMaximum"),
						dateMin : filter.get("selectedDateMin"),
						dateMax : filter.get("selectedDateMax"),
						selectedPermissibleValues : filter.get("selectedPermissibleValues")
					});
					
					// only add groups that have filters.  We'll fill in the rest later
					if (typeof sessionForm.groups === "undefined") {
						sessionForm.groups = [];
					}
					sessionForm.groups.push({
						uri: filter.get("groupUri"),
						name: filter.get("groupName")
					});
				}
			}
			sessionForm["filters"] = formFilters;
			
			session.forms.push(sessionForm);
		});
		
		// studies
		session.studies = Object.keys(QueryTool.dataCart.includedStudies);
		
		return session;
	}
});