/**
 * 
 */
QT.Page = BaseModel.extend({
	defaults : {
		activeStepsTab: "stepOneTab",				// the ID of the currently selected main tab
		refineFilterPaneOpen : true,				// is the "refine data" filter pane open
		processingView : null,						// view referencing the processing spinner
		forms : null,								// structural forms collection
		studies : null,								// structural studies collection
		dataElements : null,						// structural data elements collection
		definedQueries : null,						// saved queries collection
		dataCart : null,							// data cart model.  holds the data cart
		query : null,								// Query model.  holds the serialized query
		allFormsLoaded : false,
		allStudiesLoaded : false,
		allDataElementsLoaded : false,
		allDefinedQueriesLoaded : false,
		activeFilterTab : Config.tabConfig.StudiesTab.name,
		
		selectDeDialogView : null,
		detailsDialogView : null,
		dataTableView : null,
		downloadToQueueDialogView : null,
		metaStudyDialogView : null,
		imageDialogView : null,
		reloadSessionDialog : null,
		deployentVersion : null,
		schemaList : null,
		fileDownloadDialog :null,
		sendToMetaStudyValidationDialog :null
	},
	
	availableTilesMap: {},
	
	initialize : function() {
		//load page deployment version
		
		var view = this;
		$.ajax({
	        url: "service/deploymentVersion",
	        cache : false,
	        type: 'GET',
	        success: function(data) {
	        	view.set('deploymentVersion',data);
	        },
	        error: function(xhr, status, error) {
	        	$.ibisMessaging("dialog", "error", "There was a problem retrieving the deployment version");
	        },
	        async: false
	    });
	},

	// initialized in PageView
	
	loadStructuralData : function() {
		this.loadFormData();
		this.loadStudiesData();
		this.loadDataElementData();
	},
	
	loadSavedQueries : function() {
		var view = this;
	    jQuery.ajax({
	        url: "service/savedQueries",
	        dataType: 'script',
	        success: function(data) {
	        	view.loadDefinedQueries(JSON.parse(data));
	        },
	        error: function() {
	        	$.ibisMessaging("dialog", "error", "There was a problem retrieving the list of defined queries");
	        },
	        async: true
	    });
	},
	
	loadFormData : function() {
			var view = this;
		    jQuery.ajax({
		        url: "service/query/formResults",
		        dataType: 'script',
		        success: function(data) {
		        	view.loadForms(JSON.parse(data));
		        },
		        error: function() {
		        	// TODO: handle error
		        },
		        async: true
		    });
	},
	
	
	loadStudiesData : function() {
		var view = this;
	    jQuery.ajax({
	        url: "service/query/studyResults",
	        dataType: 'script',
	        success: function(data) {
	        	view.loadStudies(JSON.parse(data));
	        },
	        error: function() {
	        	$.ibisMessaging("dialog", "error", "There was an error loading the studies.  Major functionality of this tool may be impacted.  It is suggested that you re-load the Query Tool");
	        },
	        async: true
	    });
	},
	
	loadDataElementData : function() {
		var view = this;
		jQuery.ajax({
	        url: "service/query/deFacetItems",
	        dataType: 'script',
	        success: function(data) {
	        	view.loadDataElements(JSON.parse(data));
	        },
	        error: function() {
	        	$.ibisMessaging("dialog", "error", "There was an error loading data elements.  Major functionality of this tool may be impacted.  It is suggested that you re-load the Query Tool");
	        },
	        async: true
	    });
	},
	
	loadForms : function(formData) {
		var formCollect = this.get("forms");

		for(var i=0;i<formData.length;i++) {
			var obj = formData[i];
			var f = new QT.Form(obj);
			formCollect.add(f) ;   //testing for now
			//formCollect.add(new QT.Form(JSON.parse(obj)));   when we change to web service
			this.addToAvailableMap("form", obj);
		}

		this.set("allFormsLoaded",true);
		
		if(this.get("allStudiesLoaded") == true) {
			//this will only be called once
			this.associateFormsToStudies();
		}
	},
	
	loadStudies : function(studyData) {
		var studyCollect = this.get("studies");

		for(var i=0;i<studyData.length;i++) {
			var obj = studyData[i];
			studyCollect.add(new QT.Study(obj));
			this.addToAvailableMap("study", obj);
		}
		
		this.set("allStudiesLoaded",true);
		
		if(this.get("allFormsLoaded") == true) {
			//this will only be called once
			this.associateFormsToStudies();
		}			
	},
	
	loadDataElements : function(deData) {
		var deCollection = this.get("dataElements");
		var numDes = deData.length;
		for (var i = 0; i < numDes; i++) {
			var obj = deData[i];
			var de = new QT.DataElement();
			de.fromWebservice(obj)
			deCollection.add(de);
		}
		this.set("allDataElementsLoaded", true);
		// no need to associate at all here - all searches are done server side
		EventBus.trigger("complete:dataElementDataLoaded");
	},
	
	loadDefinedQueries : function(sqData) {
		var queriesCollection = this.get("definedQueries");

		for(var i=0;i<sqData.length;i++) {
			var sq = sqData[i];
			sq.uri = "savedquery";
			queriesCollection.add(sq); 
		}

		this.set("allDefinedQueriesLoaded",true);
		EventBus.trigger("complete:definedQueryDataLoaded");
	},
	
	associateFormsToStudies : _.once(function() {
		var formCollect = this.get("forms");
		var studiesCollect = this.get("studies");
		for(var i=0;i<formCollect.length;i++) {
			var formModel = formCollect.at(i);
			var studies = formModel.get("studiesMapped"); 
			for(var k=0;k<studies.length;k++) {
				var study = studies[k];
				var studyModel = studiesCollect.get(study);
				
				if (typeof studyModel !== "undefined") {
					formModel.studies.add(studyModel);
					studyModel.forms.add(formModel);
				}
			}
			formModel.unset("studiesMapped");
		}
		EventBus.trigger("complete:studiesFormsDataLoad");
	}),
	
	/**
	 * Adds a single element of <type> to the availability map.  The map looks like
	 * {
	 * 		"parent URI": {
	 * 			"child URI" : true | false,
	 * 			...
	 * 		},
	 * 		...
	 * }
	 * 
	 * @param type either "study" or "form" the type of the mapped element
	 * @param data ONE study or form from raw JSON data
	 */
	addToAvailableMap : function(type, data) {
		if (this.availableTilesMap[data.uri] === undefined) {
			this.availableTilesMap[data.uri] = {};
		}
		
		if (type === "form") {
			this.availableTilesMap[data.uri]["default"] = data.isAvailable;
		}
		else {
			// type = "study"
			var children = data.forms;
			var numChildren = children.length;
			this.availableTilesMap[data.uri]["default"] = data.isAvailable;
			for (var i = 0; i < numChildren; i++) {
				var child = children[i];
				if (this.availableTilesMap[child.uri] === undefined) {
					this.availableTilesMap[child.uri] = {};
				}
				
				this.availableTilesMap[data.uri][child.uri] = (child.isAvailable == "true");
				// store the form->study map as well for faster lookup
				this.availableTilesMap[child.uri][data.uri] = (child.isAvailable == "true");
			}
		}
	},
	
	isAvailable : function(parent, child) {
		if (child === undefined) {
			child = "default";
		}
		var parentMap = this.availableTilesMap[parent];
		if (typeof parentMap !== "undefined") {
			return parentMap[child];
		}
		return false;
	}
});