/**
 * model: DataCart
 */
QT.DataCartRemoveDialogView = BaseView.extend({
	Dialog : null,
	dialogConfigs : {
		buttons: [],
		width: "75%",
		height: "auto",
		position : {my: "center top+10%", at: "center top+10%", of: window}
	},
	
	events : {
		"click .dataCartRemoveItemLink" : "removeItem"
	},
	
	initialized : false,
	itemTemplate : null,
	
	initialize : function() {
		this.Dialog = new QT.Dialog();
		this.template = TemplateManager.getTemplate("dataCartRemoveDialog");
		this.itemTemplate = TemplateManager.getTemplate("dataCartRemoveDialogItem");
		EventBus.on("open:dataCartRemove", this.open, this);
		EventBus.on("close:dataCartRemove", this.close, this);
		
		QT.DataCartRemoveDialogView.__super__.initialize.call(this);
	},
	
	render : function() {
		this.setup();
		// super render is called in setup
		this.open();
	},
	
	setup : function() {
		if (!this.initialized) {
			this.setElement($("#dataCartRemove"));
			this.$el.html(this.template({}));
			var localConfig = $.extend({}, this.dialogConfigs, {$container: this.$el});
			this.Dialog.init(this, this.model, localConfig);
			
			QT.DataCartRemoveDialogView.__super__.render.call(this);
			
			this.initialized = true;
		}
	},
	
	open : function(model) {
		this.$el.html(this.template({}));
		this.Dialog.open();
		this.$("#removeFormUri").val(model.get("uri"));
		this.populateStudies(model);
	},
	
	closeDialog : function() {
		this.Dialog.close();
	},
	
	/**
	 * Populates the studies into the dialog.
	 * @param model the form model that was clicked
	 */
	populateStudies : function(model) {
		var dcForm = this.model.forms.get(model.get("uri"));
		var studyUris = dcForm.get("studies");
		var mainStudies = QueryTool.page.get("studies");
		var $container = this.$(".dataCartItemContainer");
		for (var i = 0; i < studyUris.length; i++) {
			var studyModel = mainStudies.get(studyUris[i]);
			var templateConfig = {
				name : studyModel.get("title"),
				uri : studyUris[i]
			};
			$container.append(this.itemTemplate(templateConfig));
		}
	},
	
	removeItem : function(event) {
		var formUri = this.$("#removeFormUri").val();
		var $target = $(event.target);
		if (!$target.is(".dataCartRemoveItemLink")) {
			$target = $target.parent(); // the link itself
		}
		var $parentContainer = $target.parents(".dataCartRemoveItem");
		var studyUri = $target.attr("id").replace("removeStudy_","");
		
		
		/* Perform the rules as described in CRIT-5089:
		 * If form/study pair is NOT a part of a query (not included in the query)
		 * 		if the removing study is not the last, just update config and send to server
		 * 		if the removing study is the last, remove the form from cart entirely
		 * If form/study pair is a part of a query:
		 * 		if the removing study is not the last, update its configuration, send to server, and re-run query
		 * 		if the removing study is the last, remove the form from the cart entirely and reset the query
		 */
		var queryForms = QueryTool.query.get("formUris");
		var dcForm = this.model.forms.get(formUri);
		if (dcForm != null) {
			var studyUris = dcForm.get("studies");
			var countStudies = studyUris.length;
			if (queryForms.indexOf(formUri) != -1) {
				// the form is part of the query
				if (countStudies > 1) {
					// the study is NOT the last
					EventBus.trigger("removeFromDataCart", formUri, studyUri);
					$parentContainer.remove();
					// re-run the query as is
					EventBus.trigger("query:reRun");
				}
				else {
					// the study is the last
					EventBus.trigger("removeFromDataCart", formUri, undefined);
					$parentContainer.remove();
					// the below handles rebuilding the list
					EventBus.trigger("query:reset");
					$.ibisMessaging("dialog", "info", "Because this form was entirely removed, the query has been reset");
					this.closeDialog();
				}
			}
			else {
				// the form is NOT part of the query
				if (countStudies > 1) {
					// the study is NOT the last
					EventBus.trigger("removeFromDataCart", formUri, studyUri);
					$parentContainer.remove();
				}
				else {
					// the study is the last
					EventBus.trigger("removeFromDataCart", formUri, undefined);
					$parentContainer.remove();
					this.closeDialog();
				}
			}
		}
	}
});