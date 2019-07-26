/**
 * 
 */
Tabs = {
	defaults : {
		view : null,
		$container : null,
		tabActivateEventName : "select:tab"
	},
	
	
	configure : function(config) {
		
	},
	
	init : function(view, model) {
		this.view = view;
		$("#mainContent").tabs({
			heightStyle : "fill",
			activate : function(event, ui) {
				/*
				 * event : jQuery UI Event
				 * ui :
				 * 		newTab : jQuery the tab that was just activated
				 * 		oldTab : jQuery the tab that was just deactivated
				 * 		newPanel: jQuery the panel that was just activated
				 * 		oldPanel: jQuery the panel that was just deactivated
				 */
				EventBus.trigger(this.tabActivateEventName, ui);
				if (typeof this.view.onTabActivate() === "function") {
					this.view.onTabActivate(ui);
				}
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
		this.view.$el.children(".formcreator_dialog").eq(0).tabs("option", "disabled", disabledTabs);
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
		
		this.view.$el.children(".formcreator_dialog").eq(0).tabs("option", "disabled", disabledTabs);
		$tab.show();
	},
	
	/**
	 * Gets all LI elements inside this view that are tab toppers (links)
	 * 
	 * @returns jquery list of tab top li elements
	 */
	getTabTops : function() {
		return this.view.$(".ui-tabs-nav > li");
	},
	
	/**
	 * Gets a reference to the LI elements containing the tab link
	 * 
	 * @param tabId tab reference
	 * @returns jquery reference to the tab top LI
	 */
	getTab : function(tabId) {
		return this.view.$('.ui-tabs-nav .ui-tabs-anchor[href="\\#' + tabId+'"]').parent();
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
		return this.view.$(".ui-tabs-nav > .ui-state-disabled");
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
	
	destroy : function(view) {
		view.$el.children(".formcreator_dialog").eq(0).tabs("destroy");
	},
	
	refresh : function(view) {
		view.$el.children(".formcreator_dialog").eq(0).tabs("refresh");
	}
};