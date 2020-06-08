/**
 * model: QT.Query(singleton loaded  from QT.Page)
 * el: #filterLogicBoxContainer
 * template: filterLogicBox.jsp
 */
QT.FilterLogicBoxView = BaseView.extend({
	
	events : {
		"click #filterLogicCopyQuery" : "onClickCopyQuery",
		"click #filterLogicClearFilters" : "onClickClearFilters",
		"click .toggleLogicBox" : "onToggleLogicBox"
		// run query button is handled in RefineDataFiltersView
	},
	
	initialize : function() {
		this.template = TemplateManager.getTemplate("filterLogicBox");
		QT.FilterLogicBoxView.__super__.initialize.call(this);
		EventBus.on('populateQueryBox',this.populateBox, this);
		EventBus.on('applyFilters', this.populateBox, this);
		EventBus.on('viewFilters', this.populateBox, this);
		EventBus.on('remove:filter', this.onChangeQuery, this);
		EventBus.on('remove:visualFilter', this.onChangeQuery, this);
		EventBus.on('clearDataCart', this.onChangeQuery, this);
		EventBus.on('clearDataCart', this.disableRunQueryButton, this);
		EventBus.on('query:reset', this.onChangeQuery, this);
		EventBus.on('remove:visualFilter', this.onChangeQuery, this);
		EventBus.on('add:queryFilter', this.onChangeQuery, this);
		EventBus.on('runQuery', this.onChangeQuery, this);
		EventBus.on('query:reRun', this.onChangeQuery, this);
		EventBus.on('applyFilters', this.onChangeQuery, this);
		EventBus.on('viewFilters', this.onChangeQuery, this);
		EventBus.on("clearDataCart", this.clearFilters, this);
	},
	
	render : function() {
		this.$el.html(this.template(this.model.attributes));
		QT.RefineDataCartView.__super__.render.call(this);
		
		this.enableDisableButtons();
	},
	
	onClickCopyQuery: function() {
		if (!this.$("#filterLogicCopyQuery").hasClass("disabled")) {
			var copyText = document.getElementById("filterLogicBoxInput");
			copyText.select();
			document.execCommand("copy");
			$.ibisMessaging("flash", "success", "Query copied to your clipboard");
			copyText.blur();
		}
	},
	
	onClickClearFilters: function() {
		if (!this.$("#filterLogicClearFilters").hasClass("disabled")) {
			this.model.removeAllFilters();
		}
	},
	
	onToggleLogicBox : function() {
		var $toggleButton = this.$(".toggleLogicBox");
		var classExpand = "pe-is-i-angle-circle-down";
		var classContract = "pe-is-i-angle-circle-up";
		
		this.$("#filterLogicBoxInput").toggle();
		
		if ($toggleButton.hasClass(classExpand)) {
			$toggleButton.removeClass(classExpand).addClass(classContract);
		}
		else {
			$toggleButton.removeClass(classContract).addClass(classExpand);
		}
		EventBus.trigger("window:resize");
	},
	
	populateBox: function() {
		var filters = QueryTool.query.filters;
		var queryLogic = QueryTool.query.filters.getExpression();
		var numParentFilters = filters.length;
		var filterMap = {};
		var numFiltersInMap = 0;
		for (var i = 0; i < numParentFilters; i++) {
			var filter = filters.at(i);
			var childFilters = filter.get("filters");
			var numChildFilters = childFilters.length;
			for (var j = 0; j < numChildFilters; j++) {
				var childFilter = childFilters.at(j);
				filterMap[childFilter.get("filterName")] = childFilter.toQueryLogicString();
				numFiltersInMap++;
			}
		}
		
		// instead of using replace here, because filter names could have spaces in them
		// we use the split/join method of replacing
		for (var filterName in filterMap) {
			queryLogic = queryLogic.split(filterName).join(filterMap[filterName]);
		}
		queryLogic.replace(/\|\|/g, " OR ");
		queryLogic.replace(/\&\&/g, " AND ");
		queryLogic.replace(/\!/g, " NOT ");

		this.$el.find("#filterLogicBoxInput").val(queryLogic);
		
	},
	
	onChangeQuery : function() {
		this.enableDisableButtons();
	},
	
	enableDisableButtons : function() {
		if (QueryTool.query.filters.length > 0) {
			this.$("#filterLogicCopyQuery").removeClass("disabled");
			this.$("#filterLogicClearFilters").removeClass("disabled");
		}
		else {
			this.$("#filterLogicCopyQuery").addClass("disabled");
			this.$("#filterLogicClearFilters").addClass("disabled");
		}
		
		if (QueryTool.query.get("formUris").length > 0) {
			this.$("#filterLogicRunQuery").removeClass("disabled");
		}
	},
	disableRunQueryButton : function() {
		this.$("#filterLogicRunQuery").addClass("disabled");
	},
	clearFilters : function() {
		this.$el.find("#filterLogicBoxInput").val("");
	}
	
});