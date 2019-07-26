/**
 * Data is held in this.forms as a DataCartForms Collection
 * Each DataCartForm in that collection has a string array of study URIs
 */
QT.DataCart = BaseModel.extend({
	defaults : {
		countForms : 0,
		countStudies : 0
	},

	savedQueryLoadUtil : null,

	// collection
	forms : null,

	includedStudies : null,

	initialize : function() {
		QT.Form.__super__.initialize.call(this);
		this.forms = new QT.DataCartForms();
		this.includedStudies = {};
		this.savedQueryLoadUtil = QT.SavedQueryLoadUtil;
		this.savedQueryLoadUtil.init(this);

		EventBus.on("addToDataCart", this.addToDataCart, this);
		EventBus.on("removeFromDataCart", this.removeFromDataCart, this);
		EventBus.on("clearDataCart", this.clearDataCart, this);
		EventBus.on("load:savedQuery", this.loadSingleDefinedQuery, this);
	},

	/**
	 * Pass the batch as an array of objects with format: { form : formUri,
	 * study: studyUri }
	 */
	addBatchToDataCart : function(all) {
		// TODO: fill in
	},

	addToDataCart : function(formUri, studyUri) {
		var dcForms = this.forms;

		// form
		var dcForm = dcForms.get(formUri);
		if (typeof dcForm == "undefined" || dcForm == null) {
			dcForm = this.addForm(formUri);
			// in the case of a uri without version, handle the updated URI here
			// see CRIT-4839 for details
			formUri = dcForm.get("uri");
			//console.log("adding form " + formUri);
		}
		else {
			// TODO: can we log this?
			if ( window.console && console.log ){
			console.log("form " + formUri + " not found on page");
			}
		}

		// study
		if (dcForm != null) {
			var dcStudies = dcForm.get("studies");
	
			if (!this.studyExistsInForm(dcForm, studyUri)) {
				this.addStudy(dcForm, studyUri);
				this.includedStudies[studyUri] = true;
			}
	
			this.addOnServer(formUri, studyUri);
	
			this.updateCounts();
			this.updateCartButtons();
		}
		else {
			if ( window.console && console.log ){
			console.log("data cart form not found: " + formUri);
			}
			EventBus.trigger("close:processing");
		}
		EventBus.trigger("dataCart:update", formUri, studyUri);
	},
	
	removeFromDataCart : function(formUri, studyUri) {
		var form = this.forms.get(formUri);
		if (typeof form !== "undefined") {
			// if we didn't pass a study URI, remove all studies for this form
			if (typeof studyUri === "undefined") {
				// remove all studies in this form then the form itself
				var formStudies = form.get("studies");
				this._removeFormAndSomeStudies(form, formStudies);
			}
			else {
				// remove only the study passed
				this._removeFormAndSomeStudies(form, [studyUri]);
			}
		}
		// else, don't do anything
		
		// check the query itself and see if this form/study is a part of the query
		var queryForms = QueryTool.query.get("formUris");
		var dcForm = this.forms.get(formUri);
		if (dcForm != null) {
			var studyUris = dcForm.get("studies");
			var countStudies = studyUris.length;
			if (queryForms.indexOf(formUri) != -1) {
				// the form is part of the query.  We either need to re-run the query or rest it
				if (countStudies > 1) {
					// the study/form being removed is NOT the last, so rerun
					EventBus.trigger("query:reRun");
				}
				else {
					// the study/form being removed is the last, so reset
					EventBus.trigger("query:reset");
					$.ibisMessaging("dialog", "info", "Because this form was entirely removed, the query has been reset");
				}
			}
		}
		
		// no matter what else happens, update those counts
		this.updateCounts();
		this.updateCartButtons();
		
		if (this.get("countForms") == 0 && this.get("countStudies") == 0) {
			EventBus.trigger("clearDataCart");
		}
	},
	
	/**
	 * Consistent generic function to remove, from one form, some set
	 * of studies.  That set of studies could be one study or all of the
	 * studies for that form.
	 * 
	 * Removes studies that are no longer used from this.includedStudies.
	 * If the form is empty at the end of this process, removes the form as well.
	 * 
	 * @param form - form model to remove studies from
	 * @param arrStudies - array of study URIs to remove from the form
	 */
	_removeFormAndSomeStudies : function(form, arrStudies) {
		var formUri = form.get("uri");
		var len = arrStudies.length;
		
		for (var i = 0; i < len; i++) {
			 // this stays 0 because we're removing them in this loop
			 var studyUri = arrStudies[0];
			 // does not remove from the includedStudies list...
			 this.removeStudy(form, studyUri);
			 // ... so check that
			 if (!this.isStudyInOtherForms(studyUri)) {
				 delete this.includedStudies[studyUri];
			 }
			 
			 this.removeFromServer(formUri, studyUri);
			 EventBus.trigger("dataCart:update", formUri, studyUri);
		}
		// finalize
		if (form.get("studies").length == 0) {
			this.removeForm(formUri);
			EventBus.trigger("dataCart:update");
		}
	},
	
	studyExistsInForm : function(form, studyUri) {
		var studies = form.get("studies");
		for (var i = 0, len = studies.length; i < studies.length; i++) {
			if (studies[i] == studyUri) {
				return true;
			}
		}
		return false;
	},

	clearDataCart : function() {
		var dcForms = this.forms;
		var length = dcForms.length;
		for (var i = 0; i < length; i++) {
			// because the 0th position will be re-worked during each remove
			// we just get the 0th each time
			var dcForm = dcForms.at(0);
			var formUri = dcForm.get("uri");
			this.removeFromDataCart(formUri);
		}
		
		this.updateCounts();
		this.clearServerCart();
		this.updateCartButtons();
	},
	
	clearServerCart : function() {
		$.ajaxSettings.traditional = true;
		$.ajax({
			type : "POST",
			cache: false,
			url : "service/dataCart/clear",
			success : function(data, textStatus, jqXHR) {
				EventBus.trigger("change:stepTab", "stepOneTab");
				EventBus.trigger("dataCart:clear");
			},
			error : function() {
				// TODO: fill in
			}
		});
	},

	/**
	 * Listener for all data cart change events. If the data cart is determined to be empty, 
	 * Clear Data Cart and Save New Query buttons are shown to be disabled, otherwise enabled.
	 */
	updateCartButtons : function() {
		if ( !this.isEmpty() ) {
			$("#saveNewQueryDiv a").removeClass("disabled");
			$("#clearDataCartDiv a").removeClass("disabled");
		}
		else {
			$("#saveNewQueryDiv a").addClass("disabled");
			$("#clearDataCartDiv a").addClass("disabled");
		}
	},

	updateCounts : function() {
		var formsCount = this.forms.length;
		this.set("countForms", formsCount);
		this.set("countStudies", Object.keys(this.includedStudies).length);
		EventBus.trigger("dataCart:countChange", this);
	},

	/**
	 * Checks the existence of a form model and its linked study model.
	 * @Return true if that combination exists, otherwise false
	 */
	isInDataCart : function(formModel, studyModel) {
		var form = this.forms.get(formModel.get("uri"));
		if (form) {
			var studyUri = studyModel.get("uri");
			var studies = form.get('studies');
			for (var i = 0; i < studies.length; i++) {
				if (studies[i] == studyUri) {
					return true;
				}
			}
		}
		return false;
	},

	/**
	 * Utility function which adds the form specified by URI.
	 * This does NO consistency checking so it should be used only by other
	 * processes which do but never by itself.
	 */
	addForm : function(uri) {
		var formModel = QueryTool.page.get("forms").get(uri);
		if (typeof formModel === "undefined") {
			formModel = QueryTool.page.get("forms").findWhere({"uriNoVersion": uri});
		}

		if (typeof formModel != "undefined") {
			var dcForm = new QT.DataCartForm();
			dcForm.fromForm(formModel);
			this.forms.add(dcForm);
			formModel.set("isInDataCart", true);

			return dcForm;
		}
		return null;
	},

	/**
	 * Utility function which removes the form specified.
	 * This does NO consistency checking so it should be used only by other
	 * processes which do but never by itself.
	 */
	removeForm : function(uri) {
		this.forms.remove(uri);
		QueryTool.page.get("forms").get(uri).set("isInDataCart", false);
	},

	/**
	 * Utility function which only adds a study to the form specified.
	 * This does NO consistency checking so it should be used only by other
	 * processes which do but never by itself.
	 */
	addStudy : function(dataCartForm, uri) {
		var studies = dataCartForm.get("studies");
		QueryTool.page.get("studies").get(uri).set("isInDataCart", true);
		studies.push(uri);
		dataCartForm.set("studies", studies);
	},

	/**
	 * Utility function which only removes a study from the form specified.
	 * This does NO consistency checking so it should be used only by other
	 * processes which do but never by itself.
	 */
	removeStudy : function(dataCartForm, uri) {
		var studies = dataCartForm.get("studies");
		var position = studies.indexOf(uri);
		if (~position) {
			studies.splice(position, 1);
			dataCartForm.set("studies", studies);
		}
		var qtStudies = QueryTool.page.get("studies");
		var myStudy = qtStudies.get(uri);
		if (myStudy != null) {
			myStudy.set("isInDataCart", false);
		}
		else {
			if ( window.console && console.log ){
			console.log("study " + uri + " is not in local cache");
			}
		}
	},

	isStudyInOtherForms : function(studyUri) {
		var dcForms = this.forms;
		for (var i = 0; i < dcForms.length; i++) {
			var dcStudies = dcForms.at(i).get("studies");
			for (var k = 0; k < dcStudies.length; k++) {
				if (dcStudies[k] == studyUri) {
					return true;
				}
			}
		}
		return false;
	},

	addOnServer : function(formUri, studyUri) {
		var data = {
			formUri : formUri,
			studyUri : studyUri
		};

		$.ajaxSettings.traditional = true;
		$.ajax({
			type : "POST",
			cache: false,
			url : "service/dataCart/form/add",
			data : data,
			success : function(data, textStatus, jqXHR) {
				EventBus.trigger("added:dataCartForm");
				EventBus.trigger("added:dataCartForm:callback");
			},
			error : function() {
				// TODO: fill in
				if ( window.console && console.log ){
				console.log("error in adding a form");
				}
			}
		});
	},

	removeFromServer : function(formUri, studyUri) {
		var data = {
			formUri : formUri,
			studyUri : studyUri
		};

		$.ajaxSettings.traditional = true;
		$.ajax({
			type : "POST",
			cache: false,
			url : "service/dataCart/form/remove",
			data : data,
			success : function(data, textStatus, jqXHR) {
				// TODO: fill in

			},
			error : function() {
				// TODO: fill in
			}
		});
	},

	/**
	 * Given a Defined Query (Saved Query), overwrite the current contents of
	 * the Data Cart and retrieve the saved query's details, loading them into
	 * the cart.
	 */
	loadSingleDefinedQuery : function(id) {
		this.savedQueryLoadUtil.loadQuery(id);
	},

	isEmpty : function() {
		return (this.get("countForms") === 0)
				&& (this.get("countStudies") === 0);
	}
});