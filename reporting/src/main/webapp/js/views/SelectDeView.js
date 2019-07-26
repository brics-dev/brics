/**
 * 
 */
QT.SelectDeView = BaseView.extend({
	events : {
		"click .selectDe_selectionCheckbox" : "search",
		"click #selectDe_searchButton" : "search",
		"keydown .selectDe_textSearch" : "textSearchKey"
	},
	
	Dialog : null,
	dialogConfig : {
		open : function() {
			$('.ui-dialog-buttonpane').find('button').addClass('buttonPrimary');
		},
		buttons: [
		    {
		    	text: "Add Selected Elements",
		    	click : function() {
		    		QueryTool.page.get("selectDeDialogView").addElements();
		    	}
		    },
		    {
		    	text: "Cancel",
		    	click : function() {
		    		EventBus.trigger("close:selectDeDialog");
		    	}
		}]
	},
	initialized : false,
	itemTemplate : null, 
	
	initialize : function() {
		this.Dialog = new QT.Dialog();
		this.template = TemplateManager.getTemplate("selectDeDialogTemplate");
		this.itemTemplate = TemplateManager.getTemplate("selectDeDialogFilterItem");
		EventBus.on("open:selectDeDialog", this.open, this);
		EventBus.on("close:selectDeDialog", this.close, this);
		
		this.listenTo(this.model, "change:diseaseFiltersOptions", this.populateDiseases);
		this.listenTo(this.model, "change:populationFiltersOptions", this.populatePopulations);
		
		QT.SelectDeView.__super__.initialize.call(this);
	},
	
	render : function() {
		this.setup();
		// super render is called in setup
		this.open();
	},
	
	setup : function() {
		if (!this.initialized) {
			this.setElement($("#selectDeDialog"));
			this.$el.html(this.template(this.model.attributes));
			var localConfig = $.extend({}, this.dialogConfig, {$container: this.$el,height:"auto"});
			this.Dialog.init(this, this.model, localConfig);
			
			this.setUpModel();
			
			QT.SelectDeView.__super__.render.call(this);
			
			this.initialized = true;
		}
	},
	
	/**
	 * Responds to the primary button click.  
	 */
	addElements : function() {
		var dataElements = QueryTool.page.get("dataElements");
		var selectedElements = this.model.get("selectedDataElements");
		var tabName = "DataElementsTab";
		
		if (selectedElements.length == 0) {
			$.ibisMessaging("close", {type: "primary", container : "#selectDe_error"});
			$.ibisMessaging("primary", "error", "You must select a data element to add.  Otherwise, click the Cancel button", {container : "#selectDe_error"});
			return;
		}
		
		// hide all.  list is an array
		var visibleElements = dataElements.allVisible(tabName);
		var selectedModels = visibleElements;
		// I'm doing this manually here because we don't want the filter pills
		// and there are no other filters to apply to this list
		for (var i = 0; i < selectedElements.length; i++) {
			var element = dataElements.get(selectedElements[i]);
			selectedModels.push(element);
		}
		
		EventBus.trigger("dataElements:add", selectedModels);
		
		this.closeDialog();
	},
	
	reset : function() {
		this.model.reset();
		$.ibisMessaging("close", {type: "primary", container : "#selectDe_error"});
		this.$(".selectDe_textSearch").val("");
		this.$(".selectDe_selectionCheckbox").prop("checked", false);
	},
	
	open : function() {
		this.reset();
		this.Dialog.open();
		// re-bind
		this._bindToModel(this.model);
		this.search();
	},
	
	close : function() {
		this.Dialog.close();
		QT.SelectDeView.__super__.close.call(this);
	},
	
	closeDialog : function() {
		this.Dialog.close();
	},
	
	setUpModel : function() {
		this.model.setup();
	},
	
	populateDiseases : function() {
		var diseaseOptions = this.model.get("diseaseFiltersOptions");
		var $diseaseFilters = this.$("#diseaseFilters");
		for (var i = 0; i < diseaseOptions.length; i++) {
			var content = this.itemTemplate({filter: diseaseOptions[i]});
			$diseaseFilters.append(content);
		}
	},
	
	populatePopulations : function() {
		var populationOptions = this.model.get("populationFiltersOptions");
		var $populationFilters = this.$("#populationFilters");
		for (var i = 0; i < populationOptions.length; i++) {
			var content = this.itemTemplate({filter: populationOptions[i]});
			$populationFilters.append(content);
		}
	},
	
	getLocationsParam : function() {
		var output = [];
		this.$(".selectDe_locationCheckbox").each(function() {
			var $this = $(this);
			if ($this.is(":checked")) {
				output.push($this.attr("value"));
			}
		});
		return output;
	},
	
	getParam : function(containerId) {
		var output = [];
		this.$("#" + containerId + " .selectDe_selectionCheckbox").each(function() {
			var $this = $(this);
			if ($this.is(":checked")) {
				output.push($this.val());
			}
		});
		return output;
	},
	
	getTypesParam : function() {
		return this.getParam("elementTypeFilters");
	},
	
	getDiseasesParam : function() {
		return this.getParam("diseaseFilters");
	},
	
	getPopulationsParam : function() {
		return this.getParam("populationFilters");
	},
	
	textSearchKey : function(e) {
		if (e.which == 13) {
			this.search();
		}
	},
	
	search : function() {
		$.ajaxSettings.traditional = true;
		var searchPhrase = this.model.get("textSearch");
		var wholeWord = this.model.get("wholeWord");
		var locations = this.getLocationsParam();
		var types = this.getTypesParam();
		var diseases = this.getDiseasesParam();
		var populations = this.getPopulationsParam();
		var sEcho = this.model.get("sEcho");
		var iDisplayStart = this.model.get("iDisplayStart");
		var iSortCol_0 = this.model.get("iSortCol_0");
		var sSortDir_0 = this.model.get("sSortDir_0");
		var iDisplayLength = this.model.get("iDisplayLength");
		
		var filterSettings = {};
		filterSettings["locations[]"] = locations;
		filterSettings.searchPhrase = searchPhrase;
		filterSettings.wholeWord = wholeWord;
		if (types.length > 0) {
			filterSettings["elementTypes[]"] = types;
		}
		if (diseases.length > 0) {
			filterSettings["diseases[]"] = diseases;
		}
		if (populations.length > 0) {
			filterSettings["populations[]"] = populations;
		}
		
		var view = this;

		var oTable = $('#selectDeTableContainer').dataTable({
			"bProcessing" : false,
			"bJQueryUI" : true,
			"bLengthChange" : false,
			"bServerSide" : true,
			"bFilter" : false,
			"sPaginationType" : "full_numbers",
			"sScrollY" : "450",
			"bScrollCollapse" : true,
			"bAutoWidth" : false,
			"bDestroy" : true,
			"iDisplayLength" : iDisplayLength,
			"sAjaxSource" : "service/deSelect/searchDeSelected",

			"fnServerData" : function(sSource, aoData, fnCallback) {
				var data = filterSettings;

				// transform aoData to something that makes
				// sense
				for (var i = 0; i < aoData.length; i++) {
					var aoDataSingle = aoData[i];
					data[aoDataSingle.name] = aoDataSingle.value;
				}

				$.ajax({
					"dataType" : 'json',
					"type" : "POST",
					"cache" : false,
					"traditional" : true,
					"url" : sSource,
					"data" : data,
					"success" : fnCallback
				});
			},

			"sEmptyTable" : "Loading data from server",
			"aoColumns" : view.model.get("aoColumns"),
			"fnInitComplete" : function() {
				// close status box
				//statusDialog.hide();
			},
			"fnDrawCallback" : function() {
				$('#selectDeTableContainer').find("th").attr("role", "columnheader");

				// update checkboxes to reflect saved check
				// boxes on pagination
				
				var model = QueryTool.page.get("selectDeDialogView").model;
				var listOfSelected = model.get("selectedDataElements");

				$("#selectDeTableContainer").find('input[type="checkbox"]').each(function() {
					if ($.inArray($(this).val(), listOfSelected) !== -1) {
						$(this).attr("checked",true);
					}
				});

				// set onclick for checkboxes to add to deURL
				// array
				$('#selectDeTableContainer input[type="checkbox"]').click(function() {
					if ($(this).attr("checked")) {
						EventBus.trigger("add:filterDataElement", $(this).val());
					} else {
						EventBus.trigger("remove:filterDataElement", $(this).val());
					}
				});
			}
		});
	}
});