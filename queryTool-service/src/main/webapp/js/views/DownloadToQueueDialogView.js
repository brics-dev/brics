/**
 * model: Page
 */
QT.DownloadToQueueDialogView = BaseView.extend({
	Dialog : null,
	mode : "cart", // "cart" or "table" to tell what content to download
	initialized : false,
	
	dialogConfigs : {
		title: "Download to Queue",
		buttons : [
   		{
   			text: "Save CSV",
   			click: function() {
   				QueryTool.page.get("downloadToQueueDialogView").saveCsv();
   			}
   		},
   		{
   			text: "Save Flattened CSV",
   			click : function() {
   				QueryTool.page.get("downloadToQueueDialogView").saveFlattened();
   			}
   		},
   		{
   			text: "Cancel",
   			click : function() {
   				QueryTool.page.get("downloadToQueueDialogView").closeDialog();
   			}
   		}
   		           
   		],
   		height : "auto",
   		width : "55%",
   		
   		close : function(event) {
   			EventBus.trigger("close:downloadToQueue");
   		}
	},
	
	events : {

	},
	
	initialize : function() {
		this.Dialog = new QT.Dialog();
		this.template = TemplateManager.getTemplate("downloadToQueueDialog");
		
		EventBus.on("open:downloadToQueue", this.openDialog, this);
		EventBus.on("close:downloadToQueue", this.closeDialog, this);
		
		
		this.listenTo(QueryTool.query, "change:tableResults", this.enableDownloadButton);
		
		QT.SaveQueryDialogView.__super__.initialize.call(this);
	},
	
	enableDownloadButton : function() {
		$("#downloadToQueue").removeClass("disabled");
	},
	
	setup : function() {
		if (!this.initialized) {
			this.setElement($("#downloadDialog"));
			this.$el.html(this.template({}));
			var localConfig = $.extend({}, this.dialogConfigs, {$container: this.$el});
			this.Dialog.init(this, this.model, localConfig);
			
			QT.DownloadToQueueDialogView.__super__.render.call(this);
			
			this.initialized = true;
		}
	},
	
	render : function() {
		this.setup();
		// super render is called in setup
		this.openDialog();
	},
	
	openDialog : function(mode) {
		// open/close state maintenance is handled in Dialog
		this.mode = mode;
		this.$el.html(this.template({}));
		this.Dialog.open();
		this.$("#downloadDialogName").val("");
	},
	
	closeDialog : function() {
		this.Dialog.close();
	},
	
	displayMessage : function(type, messageText) {
		$.ibisMessaging("close", {type: "primary"});
		$.ibisMessaging("primary", type, messageText, {container: "#downloadDialogMsgs"});
	},
	
	saveCsv : function() {
		this.downloadFile(true);
	},
	
	saveFlattened : function() {
		this.downloadFile(false);
	},
	
	downloadFile : function(isNormalCsv) {
		
		if (this.$("#downloadDialogName").val() == "") {
			this.displayMessage("error", "The package name is required");
		}
		else {
			var requestUrl;
			if (this.mode == 'cart') {
				requestUrl = "service/download/dataCart/download";
			} else {
				requestUrl = "service/download/dataTable/download";
			}
			
			$.ajax({
				type: "POST",
				cache : false,
				url: requestUrl,
				data : {
					packageName: this.$("#downloadDialogName").val(),
					isNormalCSV: isNormalCsv  
				},
				success : function(data, textStatus, jqXHR) {
					$.ibisMessaging("dialog", "success", 'Your files are processing and will be added to your download queue shortly.' 
							+ '  <a href="' + System.urls.dataRepo + '/repository/downloadQueueAction!view.action">Check the Download Queue</a>'
							+ ' to download your files. These files will stay in your queue for 30 days before being automatically removed.');
					$("#downloadToQueue").addClass("disabled");
					EventBus.trigger("close:downloadToQueue");
				},
				error : function(data) {
					alert("there was an error\n\n" + data);
					//$("body").html("there was an error\n\n" + data);
				}
			});
		}
	}
});