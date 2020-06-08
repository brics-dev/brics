/**
 * 
 */
$.getScript('/js/sessionHandler.js', function() {});
var EditorView = BaseView.extend({
	dialogTitle : "Edit",
	validationRules : [],
	revertState : null,
	dialogConfig : {},
	buttons : [{
		text: "Save",
		"class":"ui-priority-primary",
		click: _.debounce(function(){
			FormBuilder.page.get("activeEditorView").save();
		}, 1000, true)
	},
	{
		text: "Cancel",
		"class":"ui-priority-secondary",
		click: function(){
          FormBuilder.page.get("activeEditorView").cancel();
		} 
	}],
	
	initialize : function() {
		EditorView.__super__.initialize.call(this);
	},
	
	/**
	 * Renders the editor.  Be sure to set the appropriate template first.
	 * This view does not set a template!
	 * 
	 * usage:
	 * set template first!
	 * ClassName.__super__.render.call(this, model);
	 * 
	 * @param model the model to bind this view to
	 */
	render : function(model) {
		EventBus.trigger("change:activeEditor", this);
		this._openDialog(model);
		EditorView.__super__.render.call(this, model);
		this.revertState = _.clone(model.attributes);
		return this;
	},
	
	/**
	 * Adds a tab to this editor.
	 * 
	 * NOTE: in jqueryUI 1.9, the "add" and "remove" methods were deprecated
	 * in favor of the "refresh" method.  So, we have to add the content
	 * manually then call "refresh".
	 * 
	 * @param id the div ID of the new tab
	 * @param label the text to display on the tab itself
	 * @param content HTML content of the tab
	 */
	addTab : function(linkHtml, contentHtml) {
		this.$el.find("ul").append(linkHtml);
		this.$el.find(".tabcontainer").last().after(contentHtml);
		this.EditorDialog.refreshTabs(this);
	},
	
	disableTab : function(tabId) {
		this.EditorDialog.Tabs.disableTab(tabId);		
	},
	
	enableTab : function(tabId) {
		this.EditorDialog.Tabs.enableTab(tabId);
	},
	
	hideEditorWarning : function() {
		if ($("#editorWarning").length > 0) {
			$.ibisMessaging("close", {id: "editorWarning"});
		}
	},
	
	showEditorWarning : function(message, config) {
		if (typeof config === "undefined") {
			config = {};
		}
		
		var containerClass = Config.identifiers.editorErrorContainer;
		var finalConfig = {};
		var configDefault = {
			container: this.$("."+containerClass),
			id : "editorWarning"
		};
		
		$.extend(finalConfig, configDefault, config);
		$.ibisMessaging("primary", "warning", message, finalConfig);
	},
	
	/**
	 * Validates to make sure the required fields listed in this.requiredFields
	 * are filled in and correct.
	 * 
	 * PLEASE OVERRIDE IN CONCRETE VIEWS
	 * 
	 * @returns {Boolean} true if all validated; otherwise false
	 */
	validate : function() {
		return this.validateEditor(this.validationRules, this.model);
	},
	
	/**
	 * Provides a general method to validate fields in an editor given a set of
	 * rules and a model to check against.
	 * 
	 * This is most often used automatically by the EditorView.validate() function
	 * 
	 * @param rules an array of ValidationRule objects to validate against
	 * @param model the model to pass to each ValidationRule for validation
	 * @returns {Boolean} true if passed validation, otherwise false
	 */
	validateEditor : function(rules, model) {
		this.clearFailedFieldValidation();
		if (Validation.validate(rules, model)) {
			this.hideEditorWarning();
			return true;
		}
		else {
			var containerClass = Config.identifiers.editorErrorContainer;
			this.$("."+containerClass).empty();
			this.showEditorWarning(Validation.errorMessage);
			
			// highlight the fields that failed validation
			this.switchFailedFieldTab(Validation.getFailedFields(),model);
			this.highlightFailedFields(Validation.getFailedFields());
			
			return false;
		}
	},
	
	switchFailedFieldTab : function(fields,model){
		$a = this.$('[name="'+fields[0].name+'"]').parents(".tabcontainer");
		if($a.length == 0){
			this.$('.formcreator_dialog').tabs("option", "active", 0);
		}else if(model instanceof Form){
			this.$('.formcreator_dialog').tabs("option", "active", $a.index()-2);
		}else if(model instanceof Section){
			this.$('#dialog_editSection').tabs("option", "active", $a.index()-2);			
		}else if(model instanceof Question){
			this.$('#dialog_editTextbox').tabs("option", "active", $a.index()-2);
		}else{}
	},
	
	/**
	 * Highlight any fields that failed validation and place the error message
	 * placeholder text inside the field (if visible).
	 * 
	 * @param fields array of fields that failed validation
	 */
	highlightFailedFields : function(fields) {
		for (var i = 0; i < fields.length; i++) {
			this.$('[name="'+fields[i].name+'"]')
					.addClass(Config.styles.errorField)
					.attr("placeholder", fields[i].description);
			
		}
	},
		
	/**
	 * Clear the "failed field" styling on fields that previously failed
	 * validation
	 */
	clearFailedFieldValidation : function() {
		$("."+Config.styles.errorField)
			.removeClass(Config.styles.errorField)
			.attr("placeholder", "");
	},
	
	/**
	 * Callback for a tab's activation.  Does nothing unless overridden by
	 * an implementation in a child class.
	 * 
	 * @param event jQuery UI Event
	 * @param ui jQuery UI element @see http://api.jqueryui.com/tabs/#event-activate
	 */
	onActivateTab : function(event, ui) {
		/*
		 * event : jQuery UI Event
		 * ui :
		 * 		newTab : jQuery the tab that was just activated
		 * 		oldTab : jQuery the tab that was just deactivated
		 * 		newPanel: jQuery the panel that was just activated
		 * 		oldPanel: jQuery the panel that was just deactivated
		 */
	},
	
	close : function() {
		this._closeDialog(this);
		return EditorView.__super__.close.call(this);
	},
	
	save : function() {
		if (this.validate()) {
			EventBus.trigger("close:activeEditor");
			return true;
		}
		else {
			//throw new Error("validation failed");
			return false;
		}
		
		// any needed save processing here, may be needed to be overridden
		
	},
	
	cancel : function() {
		// reverts attributes back to before edit (set in render)
		this.model.set(this.revertState);
		EventBus.trigger("close:activeEditor");
	},
	
	_closeDialog : function(view) {
		this.EditorDialog.close(view);
	},
	
	/**
	 * Opens the dialog for the first time.  Part of the render() process.
	 * 
	 * @param model the model to send to the dialog
	 */
	_openDialog : function(model) {
		this.EditorDialog.init(this, model);
		this.EditorDialog.open(this);
	},
	
	/**
	 * Helper object to manage the memory inside the editor dialog
	 * 
	 */
	EditorDialog : {
		/**
		 * Initialize the editor dialog, including any tabs
		 * This creates a number of events that will need to be
		 * unregistered with destroy()
		 */
		init : function(view, model) {
			this.Tabs.init(view, model);
			this.Dialog.init(view, model);
		},
		
		/**
		 * Completely destroys the dialog and all of its dom elements, callbacks, etc.
		 * This SHOULD completely remove any elements or objects used for this dialog.
		 */
		destroy : function(view) {
			this.Tabs.destroy(view);
			this.Dialog.destroy(view);
		},
		
		open : function(view) {
			this.Dialog.open(view);
		},
		
		close : function(view) {
			this.Dialog.close(view);
		},
		
		refreshTabs : function(view) {
			this.Tabs.refresh(view);
		},
		
		Dialog : {
			init : function(view, model) {
				view.$el.dialog($.extend({}, Config.editorDefaultConfig, view.dialogConfig, {
					title: view.dialogTitle,
					beforeClose : function(event) {
						// removing validation here since it should be run on the save command instead
						//if (!FormBuilder.page.get("activeEditorView").validate()) {
						//	return false;
						//}
					},
					close : function(event) {
						// If the "Esc" key was pressed, cancel the active editor view
						if ( event.which == 27 ) {
							FormBuilder.page.get("activeEditorView").cancel();
						}
					},
					buttons : view.buttons
				}));
			},
			
			open : function(view) {
				view.$el.dialog("open");
			},
			
			close : function(view) {
				view.$el.dialog("close");
			},
			
			destroy : function(view) {
				view.$el.dialog("destroy");
			}
		},
		
		Tabs : {
			view : null,
			init : function(view, model) {
				this.view = view;
				view.$el.children(".formcreator_dialog").eq(0).tabs($.extend({}, Config.editorDefaultTabsConfig, {
					activate : function(event, ui) {
						FormBuilder.page.get("activeEditorView").onActivateTab(event, ui);
					}
				}));
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
		}
	}
});