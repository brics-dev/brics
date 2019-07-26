/**
 * 
 */
var FormStructureView = BaseView.extend({
	dialogTitle: Config.language.fsDialogTitle,
	datatable: null,

	buttons: [{
		text: "Select",
		"class": "ui-priority-primary",
		click: _.debounce(function() {
			EventBus.trigger("select:formStructure");
		}, 1000, true)
	}, {
		text: "Cancel",
		"class": "ui-priority-secondary",
		click: function() {
			$(this).dialog("close");
		}
	}],

	buttonsBad: [{
		text: "Select",
		"class": "ui-priority-primary",
		click: _.debounce(function() {
			EventBus.trigger("select:formStructure");
		}, 1000, true)
	},

	{
		text: "Cancel",
		"class": "ui-priority-secondary",
		click: function() {
			var url = baseUrl + '/form/formHome.action';
			redirectWithReferrer(url);
		}
	}],

	events: {

	},

	initialize: function() {
		FormStructureView.__super__.initialize.call(this);
		this.template = TemplateManager.getTemplate("formStructureTemplate");
	},

	render: function() {
		// Init the listeners
		EventBus.on("select:formStructure", this.selectStructure, this);
		EventBus.on("close:formStructure", this.cleanUpView, this);
		this.listenTo(this.model, "change:formStructure", this.onFSChange);

		// since this is a time-heavy operation, let's let the UI update if
		// needed
		// this also gives time for the Processing display to tell the user
		// what's going on
		_.delay(_(function() {
			this.$el.html(this.template());
			var view = this;

			// Get the form structure table data and init the table and dialog
			// box.
			$.getJSON("../dictionary/eFormAction!getPublishedFS.action", function(data, textStatus, jqXHR) {
				EventBus.trigger("change:processing", Config.language.fsConstructTableMsg);
				var tableConfig = $.parseJSON(data);
				var $table = view.$("#formStructureTable");

				// Create the form structure table
				var config = $.extend({}, {
					select: 'single',
				}, tableConfig);

				// Initialize and open the form structure table dialog box.
				view.Dialog.initAndOpen(view);
				$table.idtTable(config);

			}).fail(function(jqXHR, textStatus, errorThrown) {
				EventBus.trigger("close:processing");
				var message = "";
				var messageBtns = [{
					text: "OK",

					click: function() {
						$(this).dialog("close");
					}
				}];

				// Set the error message and/or the buttons
				switch (jqXHR.status) {
				case 500:
					message = Config.language.fsLoadError;
					break;
				case 502:
					message = Config.language.fsWsAccessError;
					break;
				default:
					message = Config.language.fsLoadError;
					break;
				}

				$.ibisMessaging("dialog", "error", message, {
					width: "400",
					buttons: messageBtns
				});
			});
		}).bind(this), 1);

		FormStructureView.__super__.render.call(this);
	},

	close: function() {
		EventBus.off("select:formStructure", this.selectStructure, this);
		EventBus.off("close:formStructure", this.cleanUpView, this);
		FormStructureView.__super__.close.call(this);
	},

	destroy: function() {
		FormStructureView.__super__.destroy.call(this);
	},

	switchButtonsToBad: function() {
		this.Dialog.switchButtonsToBad(this);

	},

	switchButtonsToGood: function() {
		this.Dialog.switchButtonsToGood(this);

	},

	selectStructure: function() {

		var selectedFS = this.getStructureFromTable();
		if (!selectedFS) {
			this.displayError(Config.language.noFormStructureDefined);
		}
		else {
			// if we are changing from one data structure to another, we need to
			// de-associate all
			// questions in the form. Plus, this needs a confirmation!
			this.switchButtonsToGood();
			var oldDataStructure = this.model.get("dataStructureName");
			if (oldDataStructure != "") {
				$.ibisMessaging("dialog", "warning", Config.language.changedFS, {
					modal: true,
					buttons: [{
						text: Config.language.continueBtnTitle,
						click: function() {
							var FSView = FormBuilder.page.get("formStructureView");
							$(this).dialog("close");
							FSView.updateStructureToModel.call(FSView, selectedFS);
						}
					},

					{
						text: Config.language.cancelBtnTitle,
						click: function() {
							$(this).dialog("close");
						}
					}]
				});
			}
			else {
				this.updateStructureToModel(selectedFS);
			}
		}
	},

	updateStructureToModel: function(selectedFS) {
		this.model.set("dataStructureName", selectedFS.shortName);

		// from REQ-609: copy name (shortname), short name & description to
		// other fields
		// but only if there is no value in those fields already
		if (!this.model.get("shortName")) {
			this.model.set("shortName", selectedFS.shortName);
		}
		if (!this.model.get("name")) {
			this.model.set("name", selectedFS.title);
		}
		if (!this.model.get("description")) {
			this.model.set("description", selectedFS.description);
		}

		// actually call the function to make sure there's no race condition
		// issue
		this.onFSChange();
		this.Dialog.close(this);
	},

	getStructureFromTable: function() {
		// there should only be one here since we use radio buttons. But I
		// don't always trust that
		var table = this.$("table");
		if (table.idtApi("getSelected").length > 0) { return {
			shortName: table.idtApi("cellContent", "Short Name")[0],
			version: table.idtApi("cellContent", "Version")[0],
			description: table.idtApi("cellContent", "Description")[0],
			title: table.idtApi("cellContent", "Title")[0],
		}; }
	},

	displayError: function(text) {
		this.$("#fsErrorContainer").empty();
		$.ibisMessaging("primary", "error", text, {
			container: "#fsErrorContainer"
		});
	},

	onFSChange: function() {
		var fs = this.model.get("dataStructureName");
		this.model.set("dataStructureName", fs);
		this.model.set("dataStructureRadio", fs);

		// when changing form structures, remove repeatable groups
		this.model.sections.forEach(function(section) {
			if (section.get("isRepeatable")) {
				section.set("repeatableGroupName", "None");
			}
		});
	},

	hideWarning: function() {
		if ($("#dialogWarning").length > 0) {
			$.ibisMessaging("close", {
				id: "dialogWarning"
			});
		}
	},

	showWarning: function(message, config) {
		if (typeof config === "undefined") {
			config = {};
		}

		var containerClass = Config.identifiers.dialogErrorContainer;
		var finalConfig = {};
		var configDefault = {
			container: this.$("#" + containerClass),
			id: "dialogWarning"
		};

		$.extend(finalConfig, configDefault, config);
		$.ibisMessaging("primary", "warning", message, finalConfig);
	},

	cleanUpView: function() {
		var $table = this.$("table");
		$table.idtApi("getTableApi").search("");
		$table.idtApi("destroy");
		this.$el.empty();
		this.close();
	},

	Dialog: {
		initAndOpen: function(view) {
			view.$el.dialog({
				title: view.dialogTitle,
				modal: true,
				width: 1100,
				maxHeight: Config.getMaxDialogHeight(),
				buttons: view.buttons,
				dialogClass: "formBuilder_dialog_noclose",

				close: function(event, ui) {
					EventBus.trigger("close:formStructure");
					$(this).dialog("destroy");
				},

				closeOnEscape: false,

				open: function(event, ui) {
					_.defer(function(view) {
						view.$el.dialog({
							position: {
								my: "center",
								at: "center",
								of: window
							}
						});
					}, view);

					EventBus.trigger("close:processing");
				}
			});
		},

		close: function(view) {
			if (view.$el.hasClass("ui-dialog-content") && this.isOpen(view)) {
				view.$el.dialog("close");
			}
		},

		isOpen: function(view) {
			var isDialogOpened = false;

			if (view.$el.hasClass("ui-dialog-content")) {
				isDialogOpened = view.$el.dialog("isOpen");
			}

			return isDialogOpened;
		},

		switchButtonsToBad: function(view) {
			view.$el.dialog("option", "buttons", view.buttonsBad);

		},

		switchButtonsToGood: function(view) {
			view.$el.dialog("option", "buttons", view.buttons);

		}
	}
});