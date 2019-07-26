/**
 * 
 */
QTDT.SendToMetaStudyValidationDialogView = BaseView.extend({
	Dialog : null,
	dialogConfig : {
		title: "Warning",
		height : "auto",
   		width : "40%",		
   		buttons : [
	   	   		{
	   	   			text: "Cancel",
	   	   			click: function() {
	   	   			EventBus.trigger("close:sendToMetaStudyValidationDialog");
	   	   			}
	   	   		},
	   	   		{
	   	   			text: "Continue & Save",
	   	   			click : function() {
	   	   				EventBus.trigger("open:linkSavedQueryData");
	   	   				EventBus.trigger("close:sendToMetaStudyValidationDialog");
	   	   			}
	   	   		}  	   		           
   	   		],
   	   	position: { my: "center", at: "center top+30%", of: window }
	},
	
	initialize : function() {
		this.Dialog = new QT.Dialog();
		this.template = TemplateManager.getTemplate("metaStudyValidationDialogTemplate");
		EventBus.on("open:sendToMetaStudyValidationDialog",this.openDialog, this);
		EventBus.on("close:sendToMetaStudyValidationDialog", this.closeDialog, this);
	},
	
	render : function() {
		this.setup();
		this.openDialog();	
	},
	
	setup : function() {
		if (!this.initialized) {
			this.$el = $("#sendToMetaStudyValidationDialog");
			this.$el.html(this.template({}));
			var localConfig = $.extend({}, this.dialogConfig, {$container: this.$el, width:"auto", height:"auto",resizable: true});
			this.Dialog.init(this, this.model, localConfig);
			this.initialized = true;
		}
	},
	
	openDialog : function(data) {
		
		var queryMessage;
		var fileMessage;
		
		if(data.queryNameUnique==false){
			queryMessage ="Saved Query Name";
		}
		if(data.dataNameUnique==false){
			fileMessage="Data File Name";
		}

		this.$el.html(this.template({queryMessage:queryMessage,fileMessage:fileMessage}));
		this.Dialog.open();
	},
	
	closeDialog : function() {
		this.Dialog.close();
	},
	
});