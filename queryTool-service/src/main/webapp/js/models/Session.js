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
			&& session.username !== "undefined"
			&& session.username == System.user.username
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
		var that = this;
		var session = {
			username : "",
			selectedFormURIList: [],
			filterExpression: "",
			forms : [],
			studies : [],
			outputCode : ""
		};
		
		// username
		session.username = System.user.username;
			
		// metadata
		session.selectedFormURIList = QueryTool.query.get("selectedForms");
		
		// filter expression
		session.filterExpression = QueryTool.query.filters.getExpression();
		
		// forms
		var forms = QueryTool.dataCart.forms;
		
		//output code selection
		session.outputCode = QueryTool.page.get("query").get("outputCodeSelection");
		// keeps a running list of groups added to the forms.  
		// It keeps us from adding the same one multiple times AND keeps track of indexes
		var addedGroups = [];
		forms.each(function(form) {
			var sessionForm = {
				uri : form.get("uri"),
				name: form.get("title"),
				groups: []
			};
			var formStudies = [];
			var formFilters = [];
			
			var studies = form.get("studies"); // URIs 
			for (var i = 0, studiesLength = studies.length; i < studiesLength; i++) {
				var study = QueryTool.page.get("studies").get(studies[i]);
				formStudies.push(study.get("studyId"));
			}
			sessionForm["studyIds"] = formStudies;

			// filters
			/**
			 * This is going from page to storage.  Translate the old filter format to new
			 * advanced filters format.  Two options:
			 *  1. Keep a single list of filters and recombine them into the hierarchy in the load
			 *  2. Keep the hierarchy structure through to saved queries.
			 *  
			 *  #2 does not handle old saved queries well and would, effectively, have to have
			 *  its filters re-built during this load process. However, I believe that method would
			 *  be less memory and compute intensive overall.
			 *  
			 *  NOTE: this process must be the equivalent of (java)DataCartUtil.getDataCartToSavedQueryJson()
			 *  The output from this process will be read by SavedQueryLoadUtil.loadSavedQueryData()
			 */
			var filters = QueryTool.query.filters;
			for (var j = 0, filtersLength = filters.length; j < filtersLength; j++) {
				var filter = filters.at(j);
				// only add filters to the correct form
				if (form.get("uri") == filter.get("formUri")) {
					formFilters.push(filter.toJsonObject());
					
					// only add groups that have filters.  We'll fill in the rest later
					if (typeof sessionForm.groups === "undefined") {
						sessionForm.groups = [];
					}
					sessionForm.groups.push({
						uri: filter.get("groupUri"),
						name: filter.get("groupName"),
						elements: []
					});
					addedGroups.push(filter.get("groupUri"));
				}
			}
			sessionForm["filters"] = formFilters;
			
			// add hidden columns and their groups/elements
			that.addHiddenColumns(form, sessionForm, addedGroups, that.getHiddenColForForm(form.get("uri")));
			
			session.forms.push(sessionForm);
		});
		
		// studies
		session.studies = Object.keys(QueryTool.dataCart.includedStudies);
		
		return session;
	},
	
	addHiddenColumns : function(form, sessionForm, addedGroups, hiddenCols) {
		// for any hidden columns, add their element to the form group
		// format is elements: [{uri, shortName, selected}]
		for (var k = 0, hiddenColsLen = hiddenCols.length; k < hiddenColsLen; k++) {
			var col = hiddenCols[k];
			var rgUri = this.getRgUriFromFormUriAndRgName(form.get("uri"), col.rgName);
			
			// add the element for the hidden column
			var uriSplit = col.get("deUri").split("/");
			var shortName = uriSplit[uriSplit.length - 1];
			var element = {
				uri: col.get("deUri"),
				shortName: shortName,
				selected: false
			}
			
			var indexOfGroup = addedGroups.indexOf(rgUri);
			if (indexOfGroup != -1) {
				sessionForm.groups[indexOfGroup].elements.push(element);
			}
			else {
				// group isn't already in the storage object, create an object
				sessionForm.groups.push({
					uri: rgUri,
					name: col.get("rgName"),
					elements: [element]
				});
				addedGroups.push(rgUri);
			}
		}
	},
	
	getHiddenColForForm : function(formUri) {
		var cols = QueryTool.page.get("dataTableView").model.columns;
		return cols.where({
			formUri: formUri,
			visible: false
		});
	},
	
	getRgUriFromFormUriAndRgName : function(formUri, rgName) {
		var formDetails = QueryTool.query.get("formDetails");
		for (var i = 0, formLen = formDetails.length; i < formLen; i++) {
			var formDetail = formDetails[i];
			if (formDetail.uri == formUri) {
				var groups = formDetail.repeatableGroups;
				for (var j = 0, groupLen = groups.length; j < groupLen; j++) {
					var rg = groups[j];
					if (rg.name == rgName) return rg.uri;
				}
			}
		}
		return null;
	}
});