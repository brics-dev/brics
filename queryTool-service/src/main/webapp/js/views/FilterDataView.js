/**
 * 
 */
QT.FilterDataView = BaseView.extend({

	initialize : function() {
		this.template = TemplateManager.getTemplate("filterDataMain");
		EventBus.on("complete:studiesFormsDataLoad", this.renderAll, this);
		EventBus.on("complete:dataElementDataLoaded", this.renderDataElements, this);
		// saved query can be rendered immediately - it listens for add instead of waiting
		// NOTE: shouldn't do this for the others, they rely on each other so should wait
		
		EventBus.on("select:selectionTab", this.onSelectTab, this);
		
		QT.FilterDataView.__super__.initialize.call(this);
	},
	
	render : function() {
		// should only be called once on page load
		this.$el.html(this.template(this.model.attributes));
		QT.FilterDataView.__super__.render.call(this);
		this.renderTabs();
	},
	
	renderTabs : function() {
		this.Tabs.init(this, this.model);
		this.renderResultPanels();
		// we used to wait on rendering defined queries but it needs forms and studies too now
	},
	
	renderAll : function() {
		this.renderStudies();
		this.renderForms();
		this.renderDefinedQueries();
	},
	
	renderTab : function(tabId) {
		var tabConfig = Config.tabConfig[tabId];
		var list = new QT.SelectionList().build(tabId);
		
		var selectionListContainer = "#" + tabConfig.name;
		var resultPaneContainer = tabConfig.tiles.container;
		if (tabId == "DataElementsTab") {
			this.assign(selectionListContainer, new QT.DeSelectionListView({model: list}));
		}
		else if (tabId == "DefinedQueriesTab") {
			this.assign(selectionListContainer, new QT.SqSelectionListView({model: list}));
		}
		else {
			this.assign(selectionListContainer, new QT.SelectionListView({model: list}));
		}
		
		if (tabId == "DefinedQueriesTab") {
			this.assign(resultPaneContainer, new QT.SqResultPaneView({model: list}));
		}
		else {
			this.assign(resultPaneContainer, new QT.ResultPaneView({model: list}));
		}
	},
	
	renderForms : function() {
		this.renderTab("FormsTab");
	},
	
	renderStudies : function() {
		this.renderTab("StudiesTab");
	},
	
	renderDataElements : function() {
		this.renderTab("DataElementsTab");
	},
	
	renderDefinedQueries : function() {
		this.renderTab("DefinedQueriesTab");
	},
	
	renderResultPanels : function() {
		this.$(".resultPaneSelectForms").hide();
		this.$(".resultPaneSelectStudies").hide();
		this.$(".resultPaneSelectDataElements").hide();
		this.$(".resultPaneSelectDefinedQueries").hide();
		
		var activeTab = this.model.get("activeFilterTab");
		if (activeTab == Config.tabConfig.StudiesTab.name) {
			this.$(".resultPaneSelectStudies").show();
		}
		else if (activeTab == Config.tabConfig.FormsTab.name) {
			this.$(".resultPaneSelectForms").show();
		}
		else if (activeTab == Config.tabConfig.DataElementsTab.name) {
			this.$(".resultPaneSelectDataElements").show();
		}
		else {
			this.$(".resultPaneSelectDefinedQueries").show();
		}
	},
	
	onSelectTab : function(ui) {
		this.model.set("activeFilterTab", ui.newTab.attr("aria-controls"));
		this.renderResultPanels();
	},
	
	Tabs : {
		view : null,
		init : function(view, model) {
			this.view = view;
			view.$(".filterPane").tabs({
				activate : function(event, ui) {
					/*
					 * event : jQuery UI Event
					 * ui :
					 * 		newTab : jQuery the tab that was just activated
					 * 		oldTab : jQuery the tab that was just deactivated
					 * 		newPanel: jQuery the panel that was just activated
					 * 		oldPanel: jQuery the panel that was just deactivated
					 */
					EventBus.trigger("select:selectionTab", ui);
				}
			});
		},
		
		destroy : function(view) {
			view.$el.children(".filterPane").eq(0).tabs("destroy");
		},
		
		refresh : function(view) {
			view.$el.children(".filterPane").eq(0).tabs("refresh");
		}
	}
});