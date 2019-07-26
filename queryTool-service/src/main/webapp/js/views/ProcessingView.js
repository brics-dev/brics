/**
 * 
 */
QT.ProcessingView = BaseView.extend({
	dialogTitle : "Working...",
	buttons : [],
	
	events : {
		
	},
	
	initialize : function() {
		this.model = new QT.Processing();
		QT.ProcessingView.__super__.initialize.call(this);
		this.template = TemplateManager.getTemplate("processing");
		EventBus.on("open:processing", this.render, this);
		EventBus.on("change:processing", this.change, this);
		EventBus.on("close:processing", this.close, this);
		this.listenTo(this.model, "change:value", this.setValueTo);
	},
	
	render : function(message) {
		if (this.Dialog.isOpen(this)) {
			this.change(message);
		}
		else {
			// break the rendering of this view out of the "normal" thread
			this.$el.html(this.template({message : message}));
			this.Dialog.initAndOpen(this);
			QT.ProcessingView.__super__.render.call(this);
		}
	},
	
	setValueTo : function(value) {
		this.model.set("value", value, {silent: true});
		var $progressBar = this.model.get("progressBar");
		if ($progressBar != null && $progressBar.is(":visible")) {
			$progressBar.progressbar("option", "value", value);
		}
	},
	
	/**
	 * Allows the eventBus to change the text or value of a processing view while it's open.
	 * Message is required but can be an empty string to denote no change
	 * Value is optional but if provided must be numeric [0-100] percent filled
	 */
	change : function(message, value) {
		if ( this.Dialog.isOpen(this) ) {
			if (message != "") {
				this.$("#processDescription").text(message);
			}
			
			if (typeof value != "undefined" && value != "") {
				this.setValueTo(value);
			}
		}
	},
	
	close : function() {
		if (this.Dialog.isOpen(this)) {
			this.Dialog.close(this);
			QT.ProcessingView.__super__.close.call(this);
		}
	},
	
	destroy : function() {
		this.Dialog.destroy(this);
		QT.ProcessingView.__super__.destroy.call(this);
	},
	
	Dialog : {
		initAndOpen : function(view) {
			view.$el.dialog({
				title : view.dialogTitle,
				modal : true,
				width : 300,
				buttons : view.buttons,
				dialogClass : "formBuilder_dialog_noclose",
				closeOnEscape: false,
				position: {my: "center center", at: "center center", of: $(window)},
				create: function(event, ui) {
					$(event.target).parent().css('position', 'fixed');
					
				},
				close : function(event, ui) {
					// close the progressbar
					$(this).dialog("destroy");
				},
				
				open : function() {
					// render the progressbar
					var model = QueryTool.page.get("processingView").model;
					model.set("progressBar", $( "#progressBar" ).progressbar({
					      value: false
					}));
				}
			});
		},
		
		close : function(view) {
			if (this.isOpen(view)) {
				$( "#progressBar" ).progressbar("destroy");
				view.$el.dialog("close");
			}
		},
		
		isOpen : function(view) {
			var isDialogOpened = false;
			
			if ( view.$el.hasClass("ui-dialog-content") ) {
				isDialogOpened = view.$el.dialog("isOpen");
			}
			
			return isDialogOpened;
		},
		
		destroy : function(view) {
			this.close(view);
			view.$el.dialog("destroy");
		}
	}
});