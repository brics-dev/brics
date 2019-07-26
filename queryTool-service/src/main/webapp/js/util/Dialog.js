/**
 * 
 */
QT.Dialog = function() {
	this.config = {
		// the below must be edited for any non-standard dialog
		$container : $(window),
		title: "",
		buttons : [{
			text : "Cancel",
			click : function() {
				$(this).dialog("close");
			}
		}],
		// generally, the below can be left alone but is available
		// as are all other config options for jQueryUI Dialog
		
		autoOpen: false,
		width: "90%",
		height: Config.getMaxDialogHeight(),
		modal: true,
		position: {my: "center", at: "center", of: window},
		draggable: true,
		resizable: false,
		dialogClass : "systemDialog",
		create: function(event, ui) {
			
		},
		open : function(event, ui) {
			
		},
		close : function(event) {
			// If the "Esc" key was pressed, cancel the active editor view
			//if ( event.which == 27 ) {
			//	$(this).dialog("close");
			//}
		}
	};
};

QT.Dialog.prototype.init = function(view, model, config) {
	this.config = $.extend({}, this.config, config);
	this.config.$container.dialog(this.config);
};
	
QT.Dialog.prototype.open = function(view) {
	//if (!this.isOpen()) {
		this.config.$container.dialog("open");
	//}
};
	
QT.Dialog.prototype.close = function(view) {
	var isDialogOpen = this.isOpen();
	//if (this.isOpen()) {
		this.config.$container.dialog("close");
	//}
};
	
QT.Dialog.prototype.destroy = function(view) {
	this.config.$container.dialog("destroy");
};

QT.Dialog.prototype.isOpen = function() {
	this.config.$container.dialog("isOpen");
}