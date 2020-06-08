/**
 * model: Page
 */
QT.DownloadToQueueDialogView = BaseView.extend({
	Dialog : null,
	mode : "cart", // "cart" or "table" to tell what content to download
	initialized : false,
	saveCSVButton : null,
	saveFlattenedCSVButton: null,
	
	
	dialogConfigs : {
		title: "Download to Queue",
		buttons : [
   		{
   			text: "Save CSV",
   			click: function(e) {
   				saveCSVButton = $(e.target);
   				saveCSVButton.prop('disabled', true);
   				QueryTool.page.get("downloadToQueueDialogView").saveCsv(saveCSVButton);
   			}
   		},
   		{
   			text: "Save Flattened CSV",
   			click : function(e) {
   				saveFlattenedCSVButton = $(e.target);
   				saveFlattenedCSVButton.prop('disabled', true);
   				QueryTool.page.get("downloadToQueueDialogView").saveFlattened(saveFlattenedCSVButton);
   			}
   		},
   		{
   			text: "Cancel",
   			click : function(e) {
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
	
	saveCsv : function(button) {
		this.saveCSVButton = button;
		this.downloadFile(true);
	},
	
	saveFlattened : function(button) {
		this.saveFlattenedCSVButton = button;
		this.downloadFile(false);
	},
	
	downloadFile : function(isNormalCsv) {
		var pkgName = this.$("#downloadDialogName").val();
		var csvBtn = this.saveCSVButton;
		var csvFlattenedBtn = this.saveFlattenedCSVButton;
		if (pkgName == "") {
			this.displayMessage("error", "The package name is required");
			if (csvBtn != null) {
				csvBtn.prop('disabled', false);
			}
			if (csvFlattenedBtn != null) {
				csvFlattenedBtn.prop('disabled', false);
			}
			
		} else if(pkgName.search(/[\\\/:\*\?\|\<\>]/) != -1) {
			this.displayMessage("error", 
					"The package name cannot contain the following special characters: \ / : * ? | < >");
			if (csvBtn != null) {
				csvBtn.prop('disabled', false);
			}
			if (csvFlattenedBtn != null) {
				csvFlattenedBtn.prop('disabled', false);
			}
		} else {
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
					packageName: pkgName,
					isNormalCSV: isNormalCsv  ,
					filterExpression: QueryTool.query.filters.getExpression()
				},
				success : function(data, textStatus, jqXHR) {
					if (typeof data.status !== "undefined") {
			         	if(data.status = "401") {
			         		// redirect
			         		window.location.href = "/query/logout";
			         		return;
			         	}
			         }
					$.ibisMessaging("dialog", "success", 'Your files are processing and will be added to your download queue shortly.' 
							+ '  <a href="' + System.urls.dataRepo + '/repository/downloadQueueAction!view.action">Check the Download Queue</a>'
							+ ' to download your files. These files will stay in your queue for 30 days before being automatically removed.');
					$("#downloadToQueue").addClass("disabled");
					
					if (csvBtn != null) {
						csvBtn.prop('disabled', false);
					}
					if (csvFlattenedBtn != null) {
						csvFlattenedBtn.prop('disabled', false);
					}
					
					EventBus.trigger("close:downloadToQueue");
				},
				error : function(data) {
					if (csvBtn != null) {
						csvBtn.prop('disabled', false);
					}
					if (csvFlattenedBtn != null) {
						csvFlattenedBtn.prop('disabled', false);
					}
					alert("there was an error\n\n" + data);
					// $("body").html("there was an error\n\n" + data);
				}
			});
		}
	}
});