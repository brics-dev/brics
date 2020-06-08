
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
		EventBus.on("sqe:destroy:all", this.destroy, this);
	},
		
	/**
	 * Loads a new saved query from objects into the view
	 * 
	 * @param sqObject
	 */
	loadSavedQuery : function() {
		// sqObject is loaded in the JSP
		this.savedQueryModel = new SavedQueryViewEngine.SavedQuery();
		
		var jsonText = $("#savedQueryData").text().replace(/%20/g, " ").replace(/&quot;/g, "\"").replace(/&amp;/g, "&").trim();
		var sqObject = $.parseJSON(jsonText);
		
		this.savedQueryModel.load(sqObject);
		this.sqView.model = this.savedQueryModel;
		return this.savedQueryModel;
	},
	
	render : function() {
		if (this.savedQueryModel === null) {
			this.initialize();
		}
		
		var sqModel = this.loadSavedQuery();
		this.sqView.render(sqModel);
	},
	
	destroy : function() {
		this.savedQueryModel = null;
		this.sqView = null;
	}
};

/**
 * 
 */
SavedQueryViewEngine.SavedQuery = BaseModel.extend({
	studies : null,
	forms : null,
	
	defaults : {
		name : "",
		description : "",
		lastUpdated : "",
		dateCreated : "",
		formCount : 0,
		linkedUsers : null,
		filtered: false,
		filterExpression: ""
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
		this.set("dateCreated", sqObject.dateCreated);
		this.set("formCount", sqObject.forms.length);
		this.set("linkedUsers", sqObject.linkedUsers);
		this.set("filterExpression", sqObject.filterExpression);
		
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

/**
 * 
 */
SavedQueryViewEngine.SavedQueryDe = BaseModel.extend({
	defaults : {
		uri : "",
		name : "",
		id : 0,
		filtered : false,
		maximum : "",
		minimum : "",
		freeFormValue : "",
		dateMin : "",
		dateMax: "",
		blank: "",
		permissibleValues : []
	},
	
	addRg : function(repeatableGroup) {
		this.savedQueryRgs.add(repeatableGroup);
	},
	
	loadData : function(dataObj) {
		this.set("uri", dataObj.uri);
		this.set("name", dataObj.name);
		this.set("id", dataObj.id);
		// the filter values will be filled later, within SavedQueryForm
	},
	
	getRenderString : function() {
		var output = "";
		var maximum = this.get("maximum");
		var minimum = this.get("minimum");
		var freeFormValue = this.get("freeFormValue");
		var dateMin = this.get("dateMin");
		var dateMax = this.get("dateMax");
		var blank = this.get("blank");
		var permissibleValues = this.get("permissibleValues");
		
		output = "";
		if (this.get("filtered")) {
			if (freeFormValue != "") {
				output += " = \"" + freeFormValue + "\"";
			}
			else if (maximum != "" && minimum != "") {
				if (output != "") {
					output += " and ";
				}
				output += " between " + minimum + " and " + maximum;
			}
			else if (dateMin != "" && dateMax != "") {
				if (output != "") {
					output += " and ";
				}
				output += " between " + dateMin + " and " + dateMax;
			}
			else if (maximum != "") {
				if (output != "") {
					output += " and ";
				}
				output += " < " + maximum;
			}	
			else if (minimum != "") {
				if (output != "") {
					output += " and ";
				}
				output += " > " + minimum;
			}
			else if (dateMin != "") {
				if (output != "") {
					output += " and ";
				}
				output += " after " + dateMin;
			}
			else if (dateMax != "") {
				if (output != "") {
					output += " and ";
				}
				output += " before " + dateMax;
			}
			else if (permissibleValues.length > 0) {
				if (output != "") {
					output += " and ";
				}
				output += " one of " + permissibleValues.join(", ");
			}
			
			if (blank) {
				if (output != "") {
					output += " and ";
				}
				output += " including empty values";
			}
		}
		
		return output;
	}
});

/**
 * 
 */

SavedQueryViewEngine.SavedQueryDes = Backbone.Collection.extend({
	initialize : function() {
		this.model = SavedQueryViewEngine.SavedQueryDe;
	},
	
	getById : function(id) {
		return this.findWhere({id : id});
	},
	
	getByUri : function(uri) {
		return this.findWhere({uri : uri});
	},
	
	getByName : function(name) {
		return this.findWhere({name : name});
	}
});

/**
 * 
 */
SavedQueryViewEngine.SavedQueryForm = BaseModel.extend({
	savedQueryRgs : null,
	
	defaults : {
		uri : "",
		name : "",
		id : 0,
		filtered : false,
		studyIds : [],
		filters: []
	},
	
	addRg : function(repeatableGroup) {
		this.savedQueryRgs.add(repeatableGroup);
	},
	
	loadData : function(dataObj) {
		this.set("uri", dataObj.uri);
		this.set("name", dataObj.name);
		this.set("id", dataObj.id);
		this.set("studyIds", dataObj.studyIds);
		
		this.savedQueryRgs = new SavedQueryViewEngine.SavedQueryRgs();
		var rgs = dataObj.groups;
		if (rgs != null) {
			for (var i = 0; i < rgs.length; i++) {
				var rg = new SavedQueryViewEngine.SavedQueryRg();
				rg.loadData(rgs[i]);
				this.addRg(rg);
			}
		}
		
		var filterConfigDefaults = {
			filtered : false,
			maximum : "",
			minimum : "",
			freeFormValue : "",
			dateMin : "",
			dateMax: "",
			blank: false,
			permissibleValues : [],
			filterName: "",
			logicBefore: "",
			groupingBefore: 0,
			groupingAfter: 0
		};
		
		var filters = dataObj.filters;
		this.set("filters", filters);
		if (filters.length > 0) {
			this.set("filtered", true);
			for (var j = 0; j < filters.length; j++) {
				var filter = filters[j];
				var rg = this.savedQueryRgs.getByUri(filter.groupUri);
				if (rg != null) {
					var de = rg.savedQueryDes.getByUri(filter.elementUri);
					// copies the elements that actually exist in the filter to the
					// config object so it can be set to the DE
					var config = _.clone(filterConfigDefaults);
					for (var key in filter) {
						config[key] = filter[key];
					}
					de.set(config);
				}
			}
		}
	}
});

/**
 * 
 */

SavedQueryViewEngine.SavedQueryForms = Backbone.Collection.extend({
	initialize : function() {
		this.model = SavedQueryViewEngine.SavedQueryForm;
	},
	
	getById : function(id) {
		return this.findWhere({id : id});
	},
	
	getByName : function(name) {
		return this.findWhere({name : name});
	},
	
	getByUri : function(uri) {
		return this.findWhere({uri : uri});
	}
});

/**
 * 
 */
SavedQueryViewEngine.SavedQueryRg = BaseModel.extend({
	savedQueryDes : null,
	
	defaults : {
		uri : "",
		name : "",
		id : 0
	},
	
	addDe : function(repeatableGroup) {
		this.savedQueryDes.add(repeatableGroup);
	},
	
	loadData : function(dataObj) {
		this.set("uri", dataObj.uri);
		this.set("name", dataObj.name);
		this.set("id", dataObj.id);
		
		this.savedQueryDes = new SavedQueryViewEngine.SavedQueryDes();
		var des = dataObj.elements;
		for (var i = 0; i < des.length; i++) {
			var de = new SavedQueryViewEngine.SavedQueryDe();
			de.loadData(des[i]);
			this.addDe(de);
		}
	}
});

/**
 * 
 */

SavedQueryViewEngine.SavedQueryRgs = Backbone.Collection.extend({
	initialize : function() {
		this.model = SavedQueryViewEngine.SavedQueryRg;
	},
	
	getById : function(id) {
		return this.findWhere({id : id});
	},
	
	getByUri : function(uri) {
		return this.findWhere({uri : uri});
	},
	
	getByName : function(name) {
		return this.findWhere({name : name});
	}
});

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
		
		EventBus.on("sqe:change:activeStudy", this.checkActive, this);
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

/**
 * 
 */

SavedQueryViewEngine.SavedQueryStudies = Backbone.Collection.extend({
	initialize : function() {
		this.model = SavedQueryViewEngine.SavedQueryStudy;
	},
	
	getById : function(id) {
		return this.findWhere({id : id});
	}
});

/**
 * 
 */
SavedQueryViewEngine.SavedQueryDeView  = BaseView.extend({
	className : "viewQuery_deItem",
	initialize : function() {
		SavedQueryViewEngine.SavedQueryRgView.__super__.initialize.call(this);
		this.template = TemplateManager.getTemplate("deItem");
		
		EventBus.on("sqe:close:all", this.destroy, this);
		EventBus.on("sqe:change:activeStudy", this.destroy, this);
		EventBus.on("sqe:destroy:all", this.destroy, this);
	},
	
	render : function($container) {
		this.$el.html(this.template(this.model.attributes));
		$container.append(this.$el);
		
		if (this.model.get("filtered")) {
			this.$(".viewQuery_filterIcon").addClass("viewQuery_filtered");
			this.$(".viewQuery_filterValue").text(this.model.getRenderString());
		}
		else {
			this.$(".viewQuery_filterIcon").addClass("viewQuery_nonFilered");
		}
		
		SavedQueryViewEngine.SavedQueryDeView.__super__.render.call(this);
	}
});

/**
 * 
 */
SavedQueryViewEngine.SavedQueryRgView  = BaseView.extend({
	className : "viewQuery_rgContainer",
	events : {

	},
	
	initialize : function() {
		SavedQueryViewEngine.SavedQueryRgView.__super__.initialize.call(this);
		this.template = TemplateManager.getTemplate("rgItem");
		
		EventBus.on("sqe:close:all", this.destroy, this);
		EventBus.on("sqe:change:activeStudy", this.destroy, this);
		EventBus.on("sqe:destroy:all", this.destroy, this);
	},
	
	render : function($container) {
		this.$el.html(this.template(this.model.attributes));
		$container.append(this.$el);
		
		SavedQueryViewEngine.SavedQueryRgView.__super__.render.call(this);
		this.renderDes();
		
	},
	
	renderDes : function() {
		var $container = this.$(".viewQuery_deListContainer");
		var des = this.model.savedQueryDes;
		des.forEach(function(de) {
			var deView = new SavedQueryViewEngine.SavedQueryDeView({model: de});
			deView.render($container);
		});
	}
	
});

/**
 * 
 */
SavedQueryViewEngine.SavedQueryFormView  = BaseView.extend({
	className : "viewQuery_formContainer",
	events : {
		"click .viewQuery_formHeader" : "formClickHandler"
	},
	
	initialize : function() {
		SavedQueryViewEngine.SavedQueryFormView.__super__.initialize.call(this);
		this.template = TemplateManager.getTemplate("formItem");
		
		EventBus.on("sqe:close:all", this.destroy, this);
		EventBus.on("sqe:change:activeStudy", this.destroy, this);
		EventBus.on("sqe:destroy:all", this.destroy, this);
	},
	
	render : function($container) {
		this.$el.html(this.template(this.model.attributes));
		$container.append(this.$el);
		
		SavedQueryViewEngine.SavedQueryFormView.__super__.render.call(this);
		this.determineFiltered();
		this.renderRgs();
	},
	
	formClickHandler : function() {
		var $container = this.$(".viewQuery_formData");
		if ($container.is(":visible")) {
			this.$(".viewQuery_plusMinus")
				.text(" + ")
				.removeClass("viewQuery_plus")
				.addClass("viewQuery_minus");
			$container.hide();
		}
		else {
			this.$(".viewQuery_plusMinus")
				.text(" - ")
				.removeClass("viewQuery_minus")
				.addClass("viewQuery_plus");
			$container.show();
		}
	},
	
	determineFiltered : function() {
		var $filterIcon = this.$(".viewQuery_formFiltered");
		if (this.model.get("filtered")) {
			$filterIcon.show().addClass("viewQuery_filtered");
		}
		else {
			$filterIcon.hide().addClass("viewQuery_nonFiltered");
		}
	},
	
	renderRgs : function() {
		var $container = this.$(".viewQuery_formData");
		var groups = this.model.savedQueryRgs;
		if (groups.length > 0) {
			groups.forEach(function(rg) {
				var rgView = new SavedQueryViewEngine.SavedQueryRgView({model: rg});
				rgView.render($container);
			});
		}
		else {
			$container.text("There are no visible data elements in this form");
		}
	}
	
});

/**
 * 
 */
SavedQueryViewEngine.SavedQueryStudyView  = BaseView.extend({
	className : "viewQuery_studyItem",
	events : {
		"click" : "studyClickHandler"
	},
	
	initialize : function() {
		SavedQueryViewEngine.SavedQueryStudyView.__super__.initialize.call(this);
		this.template = TemplateManager.getTemplate("study");
		
		this.listenTo(this.model, "change:active", this.afterChangeActive);
		EventBus.on("sqe:close:all", this.destroy, this);
		EventBus.on("sqe:destroy:all", this.destroy, this);
	},
	
	render : function($container) {
		this.$el.html(this.template(this.model.attributes));
		$container.append(this.$el);
		SavedQueryViewEngine.SavedQueryStudyView.__super__.render.call(this);
		
		this.determineFiltered();
	},
	
	studyClickHandler : function() {
		if (!this.model.get("active")) {
			EventBus.trigger("sqe:change:activeStudy", this.model);
		}
	},
	
	determineFiltered : function() {
		var $filterIcon = this.$(".viewQuery_studyFiltered");
		if (this.model.get("filtered")) {
			$filterIcon.show().addClass("viewQuery_filtered");
		}
		else {
			$filterIcon.hide().addClass("viewQuery_nonFiltered");
		}
	},
	
	/**
	 * Sets the class of this.$el to active or inactive, chosen by the
	 * model's "active" attribute
	 */
	afterChangeActive : function() {
		if (this.isActive()) {
			this.setActive();
			this.updateFormHeader();
			this.renderForms();
		}
		else {
			this.setInactive();
		}
	},
	
	updateFormHeader : function() {
		var $container = $(".viewQuery_formList");
		var $countContainer = $(".viewQuery_formCount");
		var numForms = this.model.get("formCount");
		if (numForms == 0) {
			$container.text("There are no forms in this saved query for this study");
			$countContainer.text("");
		}
		else {
			$container.html("");
			$countContainer.text("(" + numForms + ")");
		}
	},
	
	setActive : function() {
		this.$(".viewQuery_studyItemLink").addClass("viewQuery_active");
	},
	
	setInactive : function() {
		this.$(".viewQuery_studyItemLink").removeClass("viewQuery_active");
	},
	
	renderForms : function() {
		var forms = this.model.forms;
		// note, this container lives outside the study block, so can't use this.$()
		var $container = $(".viewQuery_formList");
		forms.forEach(function(form) {
			var formView = new SavedQueryViewEngine.SavedQueryFormView({model : form});
			formView.render($container);
		});
	},
	
	isActive : function() {
		return this.model.get("active");
	}
	
});

/**
 * 
 */
SavedQueryViewEngine.SavedQueryView  = BaseView.extend({
	
	events : {
		"click .viewQuery_queryHeader" : "onOpenCloseQueryDetailsClick"
	},
	
	initialize : function() {
		SavedQueryViewEngine.SavedQueryView.__super__.initialize.call(this);
		this.template = TemplateManager.getTemplate("viewSavedQueryTemplate");
		
		EventBus.on("sqe:close:all", this.close, this);
		EventBus.on("sqe:destroy:all", this.destroy, this);
	},
	
	render : function(model) {
		// closes all current views
		EventBus.trigger("sqe:close:all");
		
		//format date
		if(model.attributes.lastUpdated != undefined && model.attributes.lastUpdated != "") {
			var lastUpdatedDate = new Date(model.attributes.lastUpdated);
			model.attributes.lastUpdated = $.datepicker.formatDate('yy-mm-dd', lastUpdatedDate);
		}
		if(model.attributes.dateCreated != undefined && model.attributes.dateCreated != "") {
			var dateCreatedDate = new Date(model.attributes.dateCreated);
			model.attributes.dateCreated = $.datepicker.formatDate('yy-mm-dd', dateCreatedDate);
		}
		
		this.$el.html(this.template(model.attributes));
		
		// start up the magic - by appending to the main container
		$(".viewQueryContainer").append(this.$el);
		
		var studies = model.studies;
		var $container = this.$(".viewQuery_studiesContainer");
		studies.forEach(function(study) {
			var studyView = new SavedQueryViewEngine.SavedQueryStudyView({model: study});
			studyView.render($container);
		});
		
		// generate logic text
		this.$('[name="queryLogic"]').text(this.generateLogicText(model));
	},
	
	onOpenCloseQueryDetailsClick : function() {
		var $container = this.$(".viewQuery_queryDetails");
		if ($container.is(":visible")) {
			this.$(".viewQuery_detailsPlusMinus")
				.text(" + ")
				.removeClass("viewQuery_plus")
				.addClass("viewQuery_minus");
			$container.hide();
		}
		else {
			this.$(".viewQuery_detailsPlusMinus")
				.text(" - ")
				.removeClass("viewQuery_minus")
				.addClass("viewQuery_plus");
			$container.show();
		}
	},
	
	generateLogicText : function(model) {
		var allFilters = [];
		model.forms.each(function(form) {
			allFilters = allFilters.concat(form.get("filters"));
		});
		
		if (allFilters.length == 0) {
			return "none";
		}
		
		var filterExpression = model.get("filterExpression");
		if (!filterExpression) {
			// old filter, doesn't use expression, so just evaluate the filters with &&
			var filterTexts = [];
			for (var j = 0; j < allFilters.length; j++) {
				filterTexts.push(this.singleFilterExpression(allFilters[j]));
			}
			filterExpression = filterTexts.join(" && ");
		}
		else {
			// new filter, uses expression so have to fill it in
			var filterNames = filterExpression.match(/[^\(\)\|\&\s\!]+/g);
			// keeps us from having to re-calculate a filter's innards
			var filterCache = {};
			for (var i = 0; i < filterNames.length; i++) {
				var filterName = filterNames[i];
				var filterText = "";
				if (filterCache[filterName]) {
					filterText = filterCache[filterName];
				}
				else {
					filterText = this.singleFilterExpression(this.findFilterByName(allFilters, filterName));
					filterCache[filterName] = filterText;
				}
				filterExpression = filterExpression.replace(filterName, filterText);
			}
		}
		
		filterExpression = filterExpression.replace(/\&\&/g, "AND");
		filterExpression = filterExpression.replace(/\|\|/g, "OR");
		
		return filterExpression;
	},
	
	findFilterByName : function(filters, name) {
		for (var i = 0; i < filters.length; i++) {
			var filter = filters[i];
			if (filter.name == name) {
				return filter;
			}
		}
		return null;
	},
	
	singleFilterExpression : function(filter) {
		var filterType = filter.filterType;
		// infer filter types for old filters that don't explicitly say it
		if (!filterType) {
			if (filter.freeFormValue) {
				filterType = "FREE_FORM";
			}
			else if (filter.maximum && filter.minimum) {
				filterType = "RANGED_NUMERIC";
			}
			else if (filter.dateMax && filter.dateMin) {
				filterType = "DATE";
			}
			else if (filter.permissibleValues) {
				filterType = "MULTI_SELECT";
			}
		}
		
		var filterName = filter.filterName || filter.name || this.generateFilterName(filter);
		
		var selectedPVs = filter.permissibleValues;
		var combinedValues = "";
		var numSelectedPVs;
		if (filterType == "FREE_FORM") {
			return filterName + " = \"" + filter.freeFormValue + "\"";
		}
		else if (filterType == "DELIMITED_MULTI_SELECT") {
			return filterName + " = \"" + filter.freeFormValue + "\"";
		}
		else if (filterType == "RANGED_NUMERIC") {
			return "(" + filterName + " >= " + filter.minimum + " AND " + filterName + " <= " + filter.maximum + ")";
		}
		else if (filterType == "DATE") {
			return "(" + filterName + " >= " + filter.dateMin + " AND " + filterName + " <= " + filter.dateMax + ")";
		}
		else if (filterType == "MULTI_SELECT") {
			var output = "";
			var mode = filter.mode;
			numSelectedPVs = (selectedPVs == null) ? 0 : selectedPVs.length;
			if (mode == "exact") {
				for (var j = 0; j < numSelectedPVs; j++) {
					if (j != 0) {
						combinedValues += " AND ";
					}
					combinedValues += filterName + " = \"" + selectedPVs[j] + "\"";
				}
				output = "(" + combinedValues + ")";
			}
			else {
				for (var i = 0; i < numSelectedPVs; i++) {
					if (i != 0) {
						combinedValues += ", ";
					}
					combinedValues += "\"" + selectedPVs[i] + "\"";
					if (i == 4) {
						combinedValues += " ...";
						break;
					}
				}
				output = "(" + filterName + " IN (" + combinedValues + "))";
			}
			
			if (filter.multiData) {
				var multiDataOutput = "(" + filterName + ".size > 1)";
				if (output != "") {
					return "(" + output + " AND " + multiDataOutput + ")";
				}
				else {
					return multiDataOutput;
				}
			}
			return output;
		}
		else if (filterType == "SINGLE_SELECT" || filterType == "PERMISSIBLE_VALUE") {
			numSelectedPVs = (selectedPVs == null) ? 0 : selectedPVs.length;
			for (var k = 0; k < numSelectedPVs; k++) {
				if (k != 0) {
					combinedValues += ", ";
				}
				combinedValues += "\"" + selectedPVs[k] + "\"";
				if (k == 4) {
					combinedValues += " ...";
					break;
				}
			}
			return "(" + filterName + " IN (" + combinedValues + "))";
		}
	},
	
	/**
	 * Generates a filter name when the filter doesn't have one explicitly assigned.
	 * ONLY WORKS FOR OLD FILTERS THAT DON'T HAVE SUBFILTERS
	 */
	generateFilterName : function(filter) {
		var formUri = filter.formUri || "form";
		var elementUri = filter.elementUri || "element";
		
		var elementName = elementUri.substring(elementUri.lastIndexOf('/') + 1);
		var formUriName = formUri.substring(formUri.lastIndexOf('/') + 1);
		var name = formUriName + "_" + elementName;
		name = name.replace("/\s/g", "$");
		return name;
	}
	
	
});