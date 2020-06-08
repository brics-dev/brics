/**
 * Handles user leave page session saving as well as enter page session loading (if exists).
 * Model: Session model
 * el: #reloadSessionDialog
 */
QT.ReloadSessionDialogView = BaseView.extend({
	Dialog : null,
	dialogConfigs : {
		buttons: [{
			text: "Reload",
			click: function() {
				var onClickReloadSession = function() {
					var data = this.model.getSession();
					QT.SavedQueryLoadUtil.sqData.query = data;
					QT.SavedQueryLoadUtil.loadSavedQueryData(data);
					this.closeDialog();
				}
				onClickReloadSession.call(QueryTool.page.get("reloadSessionDialog"));
			}
		},
		{
			text: "Do Not Reload",
			click : function() {
				var onClickDontReloadSession = function() {
					this.closeDialog();
				}
				onClickDontReloadSession.call(QueryTool.page.get("reloadSessionDialog"));
			}
		}],
		width: "75%",
		height: "auto",
		position : {my: "center top+10%", at: "center top+10%", of: window}
	},
	
	events : {
		"click .reloadSession" : "onClickReloadSession",
		"click .dontReloadSession" : "onClickDontReloadSession"
	},
	
	initialized : false,
	
	initialize : function() {
		this.model = new QT.Session();
		this.Dialog = new QT.Dialog();
		this.template = TemplateManager.getTemplate("reloadSessionDialogTemplate");
		
		//EventBus.on("complete:studiesFormsDataLoad", this.open, this);
		EventBus.on("view:reloadSessionDialog", this.open, this);
		
		QT.ReloadSessionDialogView.__super__.initialize.call(this);
	},
	
	render : function() {
		this.setup();
		// super render is called in setup
		this.open();
	},
	
	setup : function() {
		if (!this.initialized) {
			this.setElement($("#reloadSessionDialog"));
			this.$el.html(this.template({}));
			var localConfig = $.extend({}, this.dialogConfigs, {$container: this.$el});
			this.Dialog.init(this, this.model, localConfig);
			
			QT.ReloadSessionDialogView.__super__.render.call(this);
			
			window.addEventListener("beforeunload", function() {
				var view = QueryTool.page.get("reloadSessionDialog");
				view.onUnload.call(view);
			});
			
			this.initialized = true;
		}
	},
	
	open: function() {
		if (this.model.isSessionAvailable()) {
			this.$el.html(this.template({}));
			this.Dialog.open();
		}
	},
	
	closeDialog : function() {
		this.Dialog.close();
	},
	
	onUnload : function() {
		this.model.setSession();
		// do not return anything - that would cause a "are you sure you want to leave" dialog
	}
});