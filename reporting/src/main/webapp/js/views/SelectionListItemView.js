/**
 * mode: one of Form, DataElement, Study, or DefinedQuery
 */
QT.SelectionListItemView = BaseView.extend({
	className : "selectionListItem",
	tabName : "",
	
	events : {
		"click .selectionListCheckbox" : "onClickCheckbox"
	},
	
	initialize : function() {
		this.template = TemplateManager.getTemplate("selectionListItem");
	},
	
	render : function($container, tabName) {
		//if (this.hasMappedElements()) {
			this.tabName = tabName;
			this.$el.html(this.template(this.model.attributes));
			$container.append(this.$el);
			
			this.updateTitle();
			var eventName = "change:isVisibleSelectionList" + tabName;
			this.listenTo(this.model, eventName, this.updateVisible);
			var checkedEventName = "change:isSelectedSelectionList" + tabName;
			this.listenTo(this.model, checkedEventName, this.updateSelected);
			this.listenTo(this.model, "change:title", this.updateTitle);
		//}
	},
	
	hasMappedElements : function() {
		return this.model.getCollection().length > 0;
	},
	
	updateTitle : function() {
		var output = this.model.get("title");
		var filteredCollectionCount = this.model.getCollection().allVisible(this.tabName).length;
		output = output + " (" + filteredCollectionCount + ")"; 
		this.$(".selectionListTitleContainer").html(output);
	},
	
	updateVisible : function(model, value, options) {
		// value = visible so true = visible
		if (value) {
			this.$el.show();
		}
		else {
			this.$el.hide();
		}
	},
	
	/*
	 * Responds to the model's changing selection value.  Sets the view to be consistent
	 */
	updateSelected : function(model, value, options) {
		// value = true or false.  true = selected
		this.$(".selectionListCheckbox").prop("checked", value);
	},
	
	/**
	 * We're not using modelbinding for selection because it varies from tab
	 * to tab.  So, this handles that functionality.
	 */
	onClickCheckbox : function() {
		var tabName = QueryTool.page.get("activeFilterTab");
		if (this.$(".selectionListCheckbox").is(":checked")) {
			this.model.set("isSelectedSelectionList" + tabName, true);
		}
		else {
			this.model.set("isSelectedSelectionList" + tabName, false);
		}
		
		EventBus.trigger("selectionChange:" + tabName);
	}
});