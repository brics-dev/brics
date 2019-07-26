/**
 * 
 */
QT.RboxDialogView = BaseView.extend({
	dialogTitle : "Rbox Prototype",
	Dialog : null,
	model : null,
	terminal : null,
	$flash : null,
	dialogConfig : {
		buttons : [],

		height : "auto",
		width : "50%",
		position : {my: "center top+10%", at: "center top+10%", of: window},
		
		close : function(event) {
			EventBus.trigger("close:rboxProto");
		}
		
	},
	
	events : {
		"click #runRButton" : "executeRScript",
		"click #runRboxBtn" : "executeRScript",
		"click #cancelRboxBtn" : "close"
	},
	
	initialized : false,
	
	initialize : function() {
		this.Dialog = new QT.Dialog();
		this.template = TemplateManager.getTemplate("rboxProto");
		
		EventBus.on("open:rboxProto",this.open,this);
		EventBus.on("close:rboxProto", this.close, this);
		
		QT.RboxDialogView.__super__.initialize.call(this);
	},
	
	setup : function() {
		
		if (!this.initialized) {
			
			var localConfig = $.extend({}, this.dialogConfigs, {$container: this.$el});
			this.Dialog.init(this, this.model, localConfig);
			this.initialized = true;
		}
	},
	
	open : function() {
		this.$el.html(this.template({}));
		this.Dialog.config.$container.dialog("option", "title", "Rbox Prototype");
		this.Dialog.open();

		terminal = CodeMirror.fromTextArea(document.getElementById("term"), {
			mode: 'r',
			lineNumbers: true
		});
		
		terminal.setValue("#BEGIN NOTE#\n" +
				"#data.str='{query tool input}' \n" +
				"#df <- read.csv(text=data.str, header=TRUE, sep=',')\n" +
				"#END NOTE#\n");
		
		terminal.markText({line: 0, ch: 0}, {line: 4, ch: 0}, {
			readOnly: true
		});
		
		$flash = this.$('#form_flash');
	},
	
	executeRScript : function() {
		if ( terminal.getValue().length > 0 ) {
			$flash.hide();
			this.$('#rboxConsole').html('<img src="images/loading.gif">');
			var view = this;

			$.get("service/download/executeRScript", function(csvData) {
				var ind = terminal.getValue().indexOf("#END NOTE#") + 11;
				var script = terminal.getValue().substring(ind);
				//This will set the working directory to /tmp by default, so the needed temp files can be written, read from, and deleted.
				script = script.concat("\nsetwd('/tmp') \n");
				
				var requestData = {
					"script" : script,
					"dataBytes" : csvData
				};
				
				$.ajax({
					url : System.urls.rBoxProcess,
					type : "POST",
					contentType : "application/json; charset=utf-8",
					dataType : "json",
					data : JSON.stringify(requestData),
					success : function(data) {
						view.$("#rboxConsole").text(data.consoleOutput);

						// Display graph, if able.
						if ( (typeof data.graphImage != "undefined") && (data.graphImage !== null) ) {
							view.$('#rboxGraph').html("<img src='" + data.graphImage + "'>");
						}
					}
				})
				.fail(function(jqXHR, textStatus, errorThrown) {
					view.$("#rboxConsole").text('FAILURE');
					
					// Log error to the console.
					console.error("Error while running R script. " + jqXHR.status + " : " + errorThrown);
				});
			})
			.fail(function(jqXHR, textStatus, errorThrown) {
				view.$("#rboxConsole").text('FAILURE');
				
				// Log error to the console.
				console.error("Error while running R script. " + jqXHR.status + " : " + errorThrown);
			});
		}
		else {
			$flash.text('Please enter a command that I can run!').show();
		}

	}
});