/**
 * 
 */
QT.PageView = BaseView.extend({
	
	events : {
		"click .clearDataCart" : "clearDataCart",
		"click .saveQuery" : "saveNewQuery",
		"click #rboxButton" : "openRboxDialog",
		"click .bannerHome" : "bannerHome",
		"click .logout" : "logout",
		"click .clearMetadata" : "clearMetadata"
	},
	
	initialize : function() {
		Config.containerDiv = $("#queryTool");
		this.$el = Config.containerDiv;
		
		this.model.set("processingView", new QT.ProcessingView());
		this.model.set("forms", new QT.SelectionForms());
		this.model.set("studies", new QT.SelectionStudies());
		this.model.set("dataElements", new QT.SelectionDataElements());
		this.model.set("definedQueries", new QT.SelectionDefinedQueries());
		this.model.set("dataCart",new QT.DataCart());
		this.model.set("query",new QT.Query());
		
		this.template = TemplateManager.getTemplate("pageMain");
		window.onresize = function(event) {
			EventBus.trigger("window:resize", window.innerHeight);
		};
		
		EventBus.on("window:resize", this.onWindowResize, this);
		EventBus.on("select:stepTab", this.onSelectTab, this);
		EventBus.on("dataCart:countChange", this.onDataCartCountChange, this);
		EventBus.on("change:stepTab", this.changeTab, this);
		EventBus.on("view:savedQuery", this.viewSavedQuery, this);
		
		QT.PageView.__super__.initialize.call(this);
	},
	
	render : function() {
		$(window).scrollTo(0);
		
		this.$el.html(this.template(this.model.attributes));
		this.getAccountInfo();
		this.resizeMainContainer(Window.innerHeight);
		this.renderTabs();
		
		this.assign({
			'#stepOneTab'				:	new QT.FilterDataView({model: this.model}),
			'#stepTwoTab'				:	new QT.RefineDataView({model: this.model}),
			'#dataCartButtonDiv' 		:   new QT.DataCartButtonView({model: this.model.get("dataCart")}),
			'#refineDataCartContainer' 	: 	new QT.RefineDataCartView({model: this.model.get("dataCart")}),
			'#filterPaneContentContainer': 	new QT.RefineDataFiltersView({model: this.model.get("query")})
		});
		
		this.setUpDictionarySession();
		
		this.enableDisableStepTwo();
		this.setupDialogs();
		
		this.model.loadStructuralData();
		// loading saved queries is called in SqSelectionListView.render()
		
		// just to make sure we're not carrying over from an earlier page state
		QueryTool.dataCart.clearServerCart();
		
		this.setDeploymentVersion();
		this.setRepositoryId();
		this.setLastDeployed();
	},
	
	setupDialogs : function() {
		var selectDeDialog = new QT.SelectDeView({model: new QT.SelectDe()});
		selectDeDialog.setup();
		this.model.set("selectDeDialogView", selectDeDialog);
		
		var detailsDialog = new QT.DetailsView({});
		detailsDialog.setup();
		this.model.set("detailsDialogView", detailsDialog);
		
		var dataCartRemoveDialog = new QT.DataCartRemoveDialogView({model : this.model.get("dataCart")});
		dataCartRemoveDialog.setup();
		
		var savedQueryDialog = new QT.SaveQueryDialogView({model : new QT.SavedQuery()});
		savedQueryDialog.render();
		
		var rboxDialog = new QT.RboxDialogView();
		rboxDialog.setup();
		
		var downloadToQueueDialogView = new QT.DownloadToQueueDialogView({model : this.model});
		downloadToQueueDialogView.setup();
		this.model.set("downloadToQueueDialogView", downloadToQueueDialogView);
		
		var metaStudyDialog = new QT.SendToMetaStudyDialogView({model: this.model});
		metaStudyDialog.render();
		this.model.set("metaStudyDialogView", metaStudyDialog);
		
		var imageDialog = new QTDT.ImageView({});
		imageDialog.setup();
		this.model.set("imageDialogView", imageDialog);
		
		var reloadSessionDialog = new QT.ReloadSessionDialogView({});
		reloadSessionDialog.setup();
		this.model.set("reloadSessionDialog", reloadSessionDialog);
		
		var fileDownloadDialog = new QTDT.FileDownloadDialogView({});
		fileDownloadDialog.setup();
		this.model.set("fileDownloadDialog", fileDownloadDialog);
		
		var sendToMetaStudyValidationDialog = new QTDT.SendToMetaStudyValidationDialogView({});
		sendToMetaStudyValidationDialog.setup();
		this.model.set("sendToMetaStudyValidationDialog",sendToMetaStudyValidationDialog);
	},
	
	setUpDictionarySession : function() {
		//Initializes the dictionary session so we can grab FS and DE details
		var $footer = $("#footer");
		$footer.append('<iframe id="dictionarySession" style="display:none;height:0px;width:0px;"></iframe>');
		$("#dictionarySession").load(function() {
			//TODO: Right now keeping the IFRAME on the page isn't too much of a problem since it is hidden, removing it causes a problem on IE, will need to investigate a new solution
			//$("#dictionarySession").remove();
		});
		$("#dictionarySession").attr("src", System.urls.dictionary + '/dictionary/listDataStructureAction!list.action');
	},
	
	renderTabs : function() {
		this.Tabs.init(this, this.model, this.$("#mainContent"));
	},
	
	enableStepTwo : function() {
		this.Tabs.enableTab("stepTwoTab");
	},
	
	disableStepTwo : function() {
		this.Tabs.disableTab("stepTwoTab");
	},
	
	onWindowResize : function() {
		this.resizeMainContainer(Window.innerHeight);
	},
	
	changeTab : function(id) {
		var index = this.Tabs.getTabIndex(id);
		this.Tabs.goToTab(this, index);
	},
	
	onSelectTab : function(ui) {
		this.model.set("activeStepsTab", ui.newTab.attr("aria-controls"));
	},
	
	onDataCartCountChange : function(dataCart) {
		this.enableDisableStepTwo(dataCart);
	},
	
	enableDisableStepTwo : function(dataCart) {
		if (typeof dataCart === "undefined") {
			dataCart = QueryTool.page.get("dataCart");
		}
		
		if (dataCart.get("countForms") == 0) {
			this.disableStepTwo();
		}
		else {
			this.enableStepTwo();
		}
	},
	
	/**
	 * The main container is the container around the main tabs, excluding the header.
	 * This function sizes that area to be the entire window view height minus the 
	 * header's height.
	 * NOTE: this does not work well if there is a horzontal scrollbar
	 */
	resizeMainContainer : function(innerHeight) {
		var headerHeight = $("#header").height(); // maybe outerHeight()?
		var navHeight = $("#navigation").height();
		$("#mainContent").height(innerHeight - headerHeight - navHeight - Config.windowHeightOffset);
	},
	
	clearDataCart : function() {
		EventBus.trigger("clearDataCart");
	},
	
	saveNewQuery : function() {
		try {
			EventBus.trigger("open:saveQueryDialog", "create");
		}
		catch(err) {
			console.error("Error during creating a new saved query: \n" + err);
		}
	},
	
	openRboxDialog : function() {
		try {
			EventBus.trigger("open:rboxProto");
		}
		catch(err) {
			console.error("Error during opening rbox dialog: \n" + err);
		}
	},
	
	clearMetadata : function() {
		$.ajaxSettings.traditional = true;
		$.ajax({
			type : "GET",
			cache : false,
			url : "service/clearCache",
			success : function(data, textStatus, jqXHR) {
				$.ibisMessaging("flash", "success", "cache cleared");
			},
			error : function() {
				$.ibisMessaging("flash", "error", "there was a problem clearing the metadata cache");
			}
		});
	},
	
	getAccountInfo : function() {
		var view = this;
	    jQuery.ajax({
	        url: "service/accountInfo",
	        success: function(accountInfoJson) {

	        	System.user.username = accountInfoJson.userName;
	        	
	        	var userFirstName = accountInfoJson.userFirstName;
	        	var isSysAdmin = accountInfoJson.isSysAdmin === "true";
	        	var hasAccessToAccount = accountInfoJson.hasAccessToAccount === "true";
	        	var hasAccessToGUID = accountInfoJson.hasAccessToGUID === "true";
	        	var hasAccessToProforms = accountInfoJson.hasAccessToProforms === "true";
	        	var hasAccessToRepository = accountInfoJson.hasAccessToRepository === "true";
	        	var hasAccessToWorkspace = accountInfoJson.hasAccessToWorkspace === "true";
	        	var hasAccessToDictionary = accountInfoJson.hasAccessToDictionary === "true";
	        	var hasAccessToQuery = accountInfoJson.hasAccessToQuery === "true";
	        	var hasAccessToReporting = accountInfoJson.hasAccessToReporting === "true";
	        	var hasAccessToMetaStudy = accountInfoJson.hasAccessToMetaStudy === "true";
	        	var isQtAdmin = accountInfoJson.isQTAdmin === "true";
	        	
	        	$.extend(System.user, {
	        		hasAccessToAccount : hasAccessToAccount,
	        		hasAccessToDictionary : hasAccessToDictionary,
	        		hasAccessToGUID : hasAccessToGUID,
	        		hasAccessToMetaStudy : hasAccessToMetaStudy,
	        		hasAccessToProforms : hasAccessToProforms,
	        		hasAccessToQuery : hasAccessToQuery,
	        		hasAccessToReporting : hasAccessToReporting,
	        		hasAccessToRepository : hasAccessToRepository,
	        		hasAccessToWorkspace : hasAccessToWorkspace,
	        		isQTAdmin : isQtAdmin,
	        		isSysAdmin : isSysAdmin
	        	});
	        	
	        	var publicUrl= System.urls.publicSite;

	        	$(".navHome").prop("href","/");
	        	
	        	if (hasAccessToWorkspace) {
	        		var navUrl = publicUrl + "/portal/baseAction!landing.action"
	        		$(".navWorkspace").prop("href",navUrl);
	        	} else {
	        		$(".navWorkspaceSpan").html("<div class='missingPermission'>Workspace</div>");
	        	}
	        	
	        	if (hasAccessToProforms) {
	        		var proformsUrl= System.urls.proforms;
	        		var navUrl = proformsUrl;// + "proforms"; I think the proforms URL is complete in props
	        		$(".navProforms").prop("href",navUrl);
	        	} else {
	        		$(".hasAccessToProformsSpan").html("<div class='missingPermission'>ProForms</div>");
	        	}
	        	
	        	// for reporting, hide the link if the URL is empty (non-NTI instances)
	        	if (System.urls.reporting == "") {
	        		$(".navReportingSpan").hide();
	        	}
	        	else {
		        	if (hasAccessToReporting) {
		        		var reportingUrl= System.urls.reporting;
		        		$(".navReporting").prop("href",reportingUrl);
		        	} else {
		        		$(".navReportingSpan").html("<div class='missingPermission'>Reporting</div>");
		        	}
	        	}
	        	
	        	if (hasAccessToGUID) {
	        		var guidUrl= System.urls.guid;
	        		var navUrl = guidUrl + "/guid/guidAction!landing.action"
	        		$(".navGuid").prop("href",navUrl);
	        	} else {
	        		$(".navGuidSpan").html("<div class='missingPermission'>GUID</div>");
	        	}
	        	
	        	if (hasAccessToDictionary) {
	        		var dataDictUrl= System.urls.dataDict;
	        		var navUrl = dataDictUrl + "/dictionary/listDataStructureAction!list.action"
	        		$(".navDataDict").prop("href",navUrl);
	        	} else {
	        		$(".navDataDictSpan").html("<div class='missingPermission'>Data Dictionary</div>");
	        	}
	        	
	        	if (hasAccessToRepository) {
	        		var dataRepoUrl= System.urls.dataRepo;
	        		var navUrl = dataRepoUrl + "/study/studyAction!list.action"
	        		$(".navDataRepo").prop("href",navUrl);
	        	} else {
	        		$(".navDataRepoSpan").html("<div class='missingPermission'>Data Repository</div>");
	        	}
	        	
	        	if (hasAccessToQuery) {
	        		var queryUrl= System.urls.query;
	        		var navUrl = queryUrl;
	        		$(".navQuery").prop("href",navUrl);
	        	}
	        	
	        	if (isQtAdmin) {
	        		$(".adminButtonsContainer").show();
	        	}
	        	
	        	if (hasAccessToMetaStudy){
	        		var metaUrl= System.urls.meta;
	        		var navUrl = metaUrl + "/metastudy/metaStudyListAction!list.action"
	        		$(".navMeta").prop("href",navUrl);
	        	} else {
	        		$(".navMetaSpan").html("<div class='missingPermission'>Meta Study</div>");
	        	}
	        	
	        	if (hasAccessToAccount) {
	        		var accountUrl= System.urls.account;
	        		var navUrl = accountUrl + "/accounts/viewProfile!view.action"
	        		$(".navAcct").prop("href",navUrl);
	        	} else {
	        		$(".navAcctSpan").html("<div class='missingPermission'>Account Management</div>");
	        	}
	        	
	        	
	        	var label = userFirstName;
	        	if (isSysAdmin) {
	        		label = "<b>Administrator,</b> " + userFirstName;
	        		$("#rboxButton").show();
	        	}
	        	$(".username").html(label);
	        	
	      
	    		EventBus.trigger("ready:accountInfo");
	        	
	        },
	        error: function() {

	        },
	        async: true
	    });
		
	},
	
	bannerHome : function() {
		var publicUrl= System.urls.publicSite;

		$(".bannerHome").prop("href",publicUrl);
	},
	
	logout : function() {
		var baseUrl= System.urls.base;

		$(".logout").prop("href", baseUrl + "/logout");
	},
	
	viewSavedQuery : function(queryId) {
		$.ajax({
			type: "GET",
			cache: false,
			url: "service/savedQueries/view",
			data: {id: queryId},
			success: function(data) {
				var $dialogContainer = $(".viewQueryContainer");
				if ($dialogContainer.length < 1) {
					$(".viewQueryContainer").append('<div class="viewSavedQueryDialog" style="display:none"></div>');
					$dialogContainer = $(".viewQueryContainer");
				}
				
				// 80% of window height
				var height = $(window).height() * 0.9;
				
				var template = TemplateManager.getTemplate("viewSavedQueryTemplate");
				//$dialogContainer.html(TemplateManager.getTemplate(template()));
				$("#savedQueryData").html(JSON.stringify(data));
				$dialogContainer.dialog({
					modal: true,
					height: height,
					width: 900,
					close : function() {
						EventBus.trigger("destroy:all");
						$(this).dialog("destroy");
						$(".viewQueryContainer").html("");
					},
					open : function() {
						var style = $('<style>.ui-dialog { z-index: 1200 !important; }</style>');
						$('html > head').append(style);
						SQViewProcessor.render();
					},
					buttons : [
					    {
					    	text: "OK",
					    	click : function() {
					    		$(this).dialog("close");
					    	}
					    }
					]
				});
			},
			error : function(data) {
				alert("There was a problem retrieving the saved query");
			}
		});
	},
	
	Tabs : {
		view : null,
		$container : $("body"),
		init : function(view, model, container) {
			if (typeof container !== "undefined") {
				if (container instanceof jQuery) {
					this.$container = container;
				}
				else {
					this.$container = view.$(container);
				}
			}
			else {
			
				this.$container = view.$el;
			}
			this.view = view;
			this.$container.tabs({
				activate : function(event, ui) {
					/*
					 * event : jQuery UI Event
					 * ui :
					 * 		newTab : jQuery the tab that was just activated
					 * 		oldTab : jQuery the tab that was just deactivated
					 * 		newPanel: jQuery the panel that was just activated
					 * 		oldPanel: jQuery the panel that was just deactivated
					 */
					EventBus.trigger("select:stepTab", ui);
					
					if (ui.newTab.index() == 1) {
						
						EventBus.trigger("renderResults", ui);
					}
				}
			});
		},
		
		/**
		 * Disable and hide the specified tab by ID
		 * 
		 * @param tabId the tab ID (link href minus hash mark)
		 */
		disableTab : function(tabId) {
			// tabs in the tab api is a cumulative list, so we have to append to the total list
			var $tab = this.getTab(tabId);
			var disabledTabs = this.getDisabledTabIndices();
			var tabIndex = this.getTabIndex($tab);
			
			// check if the tab is already disabled
			if (disabledTabs.indexOf(tabIndex) == -1) {
				disabledTabs.push(tabIndex);
	
				$tab.hide();
				this.$container.tabs("option", "disabled", disabledTabs);
			}
		},
		
		/**
		 * Enable and show the specified tab by ID
		 * 
		 * @param tabId the tab ID (link href minus hash mark)
		 */
		enableTab : function(tabId) {
			// tabs in the tab api is a cumulative list, so we have to remove from the total list
			var $tab = this.getTab(tabId);
			var disabledTabs = this.getDisabledTabIndices();
			var tabIndex = this.getTabIndex($tab);
			
			disabledTabs = _.without(disabledTabs, tabIndex);
			
			this.$container.tabs("option", "disabled", disabledTabs);
			$tab.show();
		},
		
		/**
		 * Gets all LI elements inside this view that are tab toppers (links)
		 * 
		 * @returns jquery list of tab top li elements
		 */
		getTabTops : function() {
			return this.$container.find("> .ui-tabs-nav > li");
		},
		
		/**
		 * Gets a reference to the LI elements containing the tab link
		 * 
		 * @param tabId tab reference
		 * @returns jquery reference to the tab top LI
		 */
		getTab : function(tabId) {
			return this.$container.find('.ui-tabs-nav').eq(0).find('.ui-tabs-anchor[href="\\#' + tabId+'"]').parent();
		},
		
		/**
		 * Gets the tab index of a specified tab.
		 * 
		 * @param tab either a jquery reference to the tab or a tabId
		 * @returns jquery reference to the tab (if it exists)
		 */
		getTabIndex : function(tab) {
			if (typeof tab == "string") {
				tab = this.getTab(tab);
			}
			return this.getTabTops().index(tab);
		},
		
		/**
		 * Gets jquery list of all disabled tab tops
		 * 
		 * @returns jquery reference list of all disabled tab tops
		 */
		getDisabledTabs : function() {
			return this.$container.find(" > .ui-tabs-nav > .ui-state-disabled");
		},
		
		/**
		 * Gets an array of indices of all currently disabled tabs
		 * 
		 * @returns integer {Array}
		 */
		getDisabledTabIndices : function() {
			var indices = [];
			var $tabs = this.getDisabledTabs();
			var $liList = this.getTabTops();
			$tabs.each(function() {
				indices.push($liList.index($(this)));
			});
			return indices;
		},
		
		goToTab : function(view, index) {
			this.$container.tabs({
				active: index
			});
		},
		
		destroy : function(view) {
			this.$container.tabs("destroy");
		},
		
		refresh : function(view) {
			this.$container.tabs("refresh");
		}
	},
	setDeploymentVersion : function() {
		$("#deploymentVersionContainer").html(this.model.get("deploymentVersion"));
	},
	setRepositoryId : function() {
		$("#repositoryIDContainer").html(this.model.get("repositoryId"));
	},
	setLastDeployed : function() {
		$("#lastDeployedContainer").html(this.model.get("lastDeployed"));
	}
});