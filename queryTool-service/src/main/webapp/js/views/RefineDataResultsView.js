/**
 * model: Page
 * el: (#stepTwoTab .resultPaneContent)
 * 
 * renders the two tabs for refine data results pane
 * and handles the top buttons
 */

QT.RefineDataResultsView = BaseView.extend({
	templateName : "resultsPanelMain",
	metaStudyLinkEnabled : true,
	
	events : {
		"click #sendToMetaStudy" : "onSendToMetaStudy",
	},
	
	initialize : function() {
		this.template = TemplateManager.getTemplate(this.templateName);
		QT.RefineDataResultsView.__super__.initialize.call(this);
		EventBus.on("window:resize", this.onWindowResize, this);
		// changing tabs is effectively the same as window resizing
		EventBus.on("select:stepTab", this.onWindowResize, this);
		EventBus.on("DataTableView:destroyTable", this.clearResultsCount, this);
		EventBus.on("openDataTableViewTab",this.openDataTableViewTab, this);
		EventBus.on("openSelectCriteriaViewTab",this.openSelectCriteriaViewTab, this);
		EventBus.on("change:dataTab", this.changeTab, this);
		EventBus.on("ready:accountInfo", this.updatePermissions, this);
		EventBus.on("updateSchemaOptions", this.updateSchemaOptions, this);
		EventBus.on("updateResultsCount", this.onQueryResultsChanged, this);
		
		
		this.listenTo(this.model.get("query"), "change:tableResults", this.onQueryResultsChanged);
	},
	
	render : function() {
		this.$el.html(this.template(this.model.attributes));
		this.resizeContainer();
		this.renderTabs();
		// assign the contents of the two tabs
		// TODO: fill in the tabs here
		QueryTool.page.set("dataTableView",new QTDT.DataTableView({model: new QTDT.DataTable()}));
		this.assign({
			"#resultsSelectCriteria" : new QT.SelectCriteriaView({model: QueryTool.page.get("query")}),
			"#resultsDatatableContainer" : QueryTool.page.get("dataTableView")
		});

		this.$("#viewDownloadQueue").attr("href", System.urls.dataRepo + '/repository/downloadQueueAction!view.action');
	},
	
	updateSchemaOptions : function() {
		var dat = this;
		var schemaListTemplate = TemplateManager.getTemplate("schemaList");
		
		$.ajax({
			type : "GET",
			url : "service/dataCart/schemas",
			cache : false,
			success : function(data, textStatus, jqXHR) {
				EventBus.trigger("resetOutputCode");
				dat.model.set("schemaList", data);
				dat.$(".schemaOutputCodes").remove();
				var schemaTemplate = schemaListTemplate(dat.model.attributes);
				dat.$("#outputCodesDropdownList").append(schemaTemplate);
			},
			error : function() {
				// TODO: fill in
			}
		});
	},
	
	updatePermissions : function() {
		if (!System.user.hasAccessToMetaStudy) {
			this.metaStudyLinkEnabled = false;
			this.$("#sendToMetaStudy").addClass("disabled");
		}
	},
	
	onWindowResize : function() {
		this.resizeContainer();
	},
	
	changeTab : function(id) {
		var index = this.Tabs.getTabIndex(id);
		this.Tabs.goToTab(this, index);
		//added listener to re-render table, specifically when users unhide columns

		
	},
	
	rerenderTable : function () {
		EventBus.trigger("select:refineDataTab", this);
	},
	
	onQueryResultsChanged : function() {
		var recordCount = 0;
		try {
			var tableResults = this.model.get("query").get("tableResults");
			var data = tableResults.data;
			// filters don't reset the header so there is no distinction between data and header there
			// however, we still need to get the count here
			// this is the only place that matters so far
			if (typeof data == "undefined") {
				recordCount = tableResults.iTotalRecords;
			}
			else {
				recordCount = data.iTotalRecords;
				
			}
			
			QTDT.totalRecords = recordCount;
		}
		catch(e) {
			// already handled above with a fall-back number
			if ( window.console && console.log ){
			console.log(e);
			}
		}
		var text = "";
		if (typeof recordCount != "undefined") {
			text = "(" + recordCount + " Rows of Data)";
		}
		
		if (this.$el.parent().parent().is(":visible")) {
			this.$("#queryResultsCount").text(text);
		}
		
	},
	
	clearResultsCount : function() {
		this.$("#queryResultsCount").text("");
	},
	
	
	onSendToMetaStudy : function() {
		if (this.metaStudyLinkEnabled) {
			EventBus.trigger("open:sendToMetaStudy");
		}
	},
	
	resizeContainer : function() {
		var $element = this.$el;
		var windowHeight = window.innerHeight;
		var currentTop = $element.offset().top;
		var $parentContainer = $element.parents(".resultPane").eq(0);
		var bottomPadding = Number($parentContainer.css("padding-bottom").replace("px", ""));
		var extraPadding = Config.scrollContainerBottomOffset;
		var finalHeight = windowHeight - currentTop - bottomPadding - extraPadding;
		$element.height(finalHeight);
		this.resizeTabs(finalHeight);
	},
	
	resizeTabs : function(finalHeight) {
		var $elementOne = this.$("#resultsSelectCriteria");
		var $elementTwo = this.$("#resultsDatatable");
		// the only thing above our element here is the tabs UL and our own padding
		var paddingTop = Number($elementOne.css("padding-top").replace("px", ""));
		var paddingBottom = Number($elementOne.css("padding-bottom").replace("px", ""));
		var tabHeight = this.$("ul.ui-tabs-nav").height();
		var height = finalHeight - tabHeight - paddingTop - paddingBottom;
		
		$elementOne.height(height);
		$elementTwo.height(height);
	},
	
	renderTabs : function() {
		this.Tabs.init(this, this.model, this.$el);
	},
	
	Tabs : {
		view : null,
		$container : $("body"),
		init : function(view, model, container) {
			if (typeof container !== "undefined") {
				if (container instanceof jQuery) {
					this.$container = container;
				}
				else {
					this.$container = view.$(container);
				}
			}
			else {
				this.$container = view.$el;
			}
			this.view = view;
			this.$container.tabs({
				active: 0,
				activate : function(event, ui) {
					/*
					 * event : jQuery UI Event
					 * ui :
					 * 		newTab : jQuery the tab that was just activated
					 * 		oldTab : jQuery the tab that was just deactivated
					 * 		newPanel: jQuery the panel that was just activated
					 * 		oldPanel: jQuery the panel that was just deactivated
					 */
				}
			});
		},
		
		/**
		 * Disable and hide the specified tab by ID
		 * 
		 * @param tabId the tab ID (link href minus hash mark)
		 */
		disableTab : function(tabId) {
			// tabs in the tab api is a cumulative list, so we have to append to the total list
			var $tab = this.getTab(tabId);
			var disabledTabs = this.getDisabledTabIndices();
			var tabIndex = this.getTabIndex($tab);
			
			disabledTabs.push(tabIndex);

			$tab.hide();
			this.$container.tabs("option", "disabled", disabledTabs);
		},
		
		/**
		 * Enable and show the specified tab by ID
		 * 
		 * @param tabId the tab ID (link href minus hash mark)
		 */
		enableTab : function(tabId) {
			// tabs in the tab api is a cumulative list, so we have to remove from the total list
			var $tab = this.getTab(tabId);
			var disabledTabs = this.getDisabledTabIndices();
			var tabIndex = this.getTabIndex($tab);
			
			disabledTabs = _.without(disabledTabs, tabIndex);
			
			this.$container.tabs("option", "disabled", disabledTabs);
			$tab.show();
		},
		
		/**
		 * Gets all LI elements inside this view that are tab toppers (links)
		 * 
		 * @returns jquery list of tab top li elements
		 */
		getTabTops : function() {
			return this.$container.find("> .ui-tabs-nav > li");
		},
		
		/**
		 * Gets a reference to the LI elements containing the tab link
		 * 
		 * @param tabId tab reference
		 * @returns jquery reference to the tab top LI
		 */
		getTab : function(tabId) {
			return this.$container.find('.ui-tabs-nav').eq(0).find('.ui-tabs-anchor[href="\\#' + tabId+'"]').parent();
		},
		
		/**
		 * Gets the tab index of a specified tab.
		 * 
		 * @param tab either a jquery reference to the tab or a tabId
		 * @returns jquery reference to the tab (if it exists)
		 */
		getTabIndex : function(tab) {
			if (typeof tab == "string") {
				tab = this.getTab(tab);
			}
			return this.getTabTops().index(tab);
		},
		
		/**
		 * Gets jquery list of all disabled tab tops
		 * 
		 * @returns jquery reference list of all disabled tab tops
		 */
		getDisabledTabs : function() {
			return this.$container.find(" > .ui-tabs-nav > .ui-state-disabled");
		},
		
		/**
		 * Gets an array of indices of all currently disabled tabs
		 * 
		 * @returns integer {Array}
		 */
		getDisabledTabIndices : function() {
			var indices = [];
			var $tabs = this.getDisabledTabs();
			var $liList = this.getTabTops();
			$tabs.each(function() {
				indices.push($liList.index($(this)));
			});
			return indices;
		},
		
		goToTab : function(view, index) {
			this.$container.tabs({
				active: index
			});
		},
		
		destroy : function(view) {
			this.$container.tabs("destroy");
		},
		
		refresh : function(view) {
			this.$container.tabs("refresh");
		}
	},
	openDataTableViewTab : function() {
		var index = this.Tabs.getTabIndex("resultsDatatable");
		this.Tabs.goToTab(this, index);
	},
	openSelectCriteriaViewTab : function() {
		var index = this.Tabs.getTabIndex("resultsSelectCriteria");
		this.Tabs.goToTab(this, index);
	}
});