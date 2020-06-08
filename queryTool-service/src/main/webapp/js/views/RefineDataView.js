/**
 * model: Page
 * el: #stepTwoTab
 */
QT.RefineDataView = BaseView.extend({

	events: {
		"click .toggleFilterPane": "toggleFilterPane",
		"click .outputCodeOption": "onSelectOutputCode",
		"click .moveDataCartToQueue": "moveCartToQueue",
		"click #downloadToQueue": "downloadToQueue",
		"click .toggleCartPane" : "toggleCartPane"
	},

	initialize: function () {
		this.template = TemplateManager.getTemplate("refineDataMain");

		// attributes: model (while changing), new value, options
		// @see http://backbonejs.org/#Events-catalog
		this.listenTo(this.model, "change:refineFilterPaneOpen", this.onChangeFilterPaneOpen);
		this.listenTo(this.model.get("query"), "change:tableResults", this.onTableUpdate);
		EventBus.on("window:resize", this.onWindowResize, this);
		EventBus.on("openFilterPane", this.openFilterPane, this);
		EventBus.on("clearDataCart", this.resetOutputCode, this);
		EventBus.on("resetOutputCode", this.resetOutputCode, this);
		EventBus.on("select:stepTabBack", this.toggleCartPane, this);
		EventBus.on("query:reset", this.expandCartPane, this);

	},

	render: function () {
		// should only be called once on page load
		this.$el.html(this.template(this.model.attributes));

		this.assign({
			".resultPaneContent": new QT.RefineDataResultsView({
				model: this.model
			})
		});

		this.resizeMinimizedHandle();
	},

	toggleFilterPane: function () {
		this.model.set("refineFilterPaneOpen", !this.model.get("refineFilterPaneOpen"));
		EventBus.trigger("resultsView:resizeMainContainer");
		EventBus.trigger("select:refineDataTab", this);
	},
	openFilterPane: function () {
		this.model.set("refineFilterPaneOpen", true);
	},
	resizeMinimizedHandle: function () {
		var containerHeight = 0;
		var windowHeight = window.innerHeight;

		var currentTop = this.$(".filterPaneHandle").offset().top;
		var paddingTop = Number(this.$el.css("padding-top").replace("px", ""));
		var paddingBottom = Number(this.$el.css("padding-bottom").replace("px", ""));
		var extraPadding = Config.scrollContainerBottomOffset;
		var borderThickness = 1;
		containerHeight = windowHeight - currentTop - (paddingTop + paddingBottom) - extraPadding;
		this.$(".filterPaneHandle").height(containerHeight);
	},
	
	toggleCartPane : function() {
		if (this.model.get("dataCartPaneOpen")) {
			this.contractCartPane();
		}
		else {
			this.expandCartPane();
		}
	},
	
	contractCartPane : function() {
		if (this.model.get("dataCartPaneOpen") == true) {
			var $cartPaneBody = $(".refineDataDataCartBody");
			var $filterPane = $("#filterPaneContentContainer");
			var $refineDataCart = $("#refineDataCartContainer");
			var $filtersList = $("#filtersList");
			$cartPaneBody.hide();
			$refineDataCart
					.attr("origHeight", $refineDataCart.height())
					.css("height", "");
			$filtersList
					.attr("origHeight", $filtersList.height())
					.height($filtersList.height() + $cartPaneBody.height());
			$filterPane
					.attr("origHeight", $filterPane.height())
					.height($filterPane.height() + $cartPaneBody.height());
			$(".toggleCartPane")
					.addClass("pe-is-i-angle-circle-down")
					.removeClass("pe-is-i-angle-circle-up");
			// update model
			this.model.set("dataCartPaneOpen", false);
			EventBus.trigger("window:resize");
		}
	},
	
	expandCartPane : function() {
		if (this.model.get("dataCartPaneOpen") == false) {
			var $cartPaneBody = $(".refineDataDataCartBody");
			var $filterPane = $("#filterPaneContentContainer");
			var $refineDataCart = $("#refineDataCartContainer");
			var $filtersList = $("#filtersList");
			$(".refineDataDataCartBody").show();
			$refineDataCart.height($refineDataCart.attr("origHeight"));
			$filterPane.height($filterPane.attr("origHeight"));
			$filtersList.height($filtersList.attr("origHeight"));
			$(".toggleCartPane")
					.addClass("pe-is-i-angle-circle-up")
					.removeClass("pe-is-i-angle-circle-down");
			$cartPaneBody.show();
			// update model
			this.model.set("dataCartPaneOpen", true);
			EventBus.trigger("window:resize");
		}
	},

	onWindowResize: function () {
		this.resizeMinimizedHandle();
	},

	onChangeFilterPaneOpen: function () {
		if (this.model.get("refineFilterPaneOpen")) {
			this.contractResultPane();
			this.$(".filterPaneHandle").hide();
			this.$(".filterPane").show();
		} else {
			this.expandResultPane();
			this.$(".filterPane").hide();
			this.$(".filterPaneHandle").show();
			this.resizeMinimizedHandle();
		}
		EventBus.trigger("change:filterPane");
	},

	onTableUpdate: function () {
		this.updateOutputCodeSelectionStyle();
		this.updateDownloadStyle();
		this.updateRboxButtonStyle();
	},

	updateOutputCodeSelectionStyle: function () {
		if (this.isDataAvailable()) {
			this.$("#outputCodesDropdown").removeClass("disabled");
		} else {
			this.$("#outputCodesDropdown").addClass("disabled");
		}
	},

	updateDownloadStyle: function () {
		if (this.isDataAvailable()) {
			this.$("#dataOptions").removeClass("disabled");
		} else {
			this.$("#dataOptions").addClass("disabled");
		}
	},

	updateRboxButtonStyle: function () {
		if (this.isDataAvailable()) {
			$("#rboxButton").removeClass("disabled");
		} else {
			$("#rboxButton").addClass("disabled");
		}
	},

	resetOutputCode: function () {

		var selectedOptionLabel = QueryTool.page.get("query").get("outputSelectionOption");
		//TODO: Note, I feel like hard coding this if statement below is not the best solution, we should look into a better solution when time permits
		this.$(".outputCodeDropdown").text((selectedOptionLabel == "pv") ? "Permissible Value" : selectedOptionLabel);
		this.model.get("query").resetChangeDisplayOption(selectedOptionLabel);
		var selectedOptionCode = QueryTool.page.get("query").get("outputCodeSelection");
		QueryTool.page.get("query").set("outputCodeSelection",selectedOptionLabel);
		EventBus.trigger("dataTableView:changeDisplayOption", selectedOptionCode);
		
	},




	onSelectOutputCode: function (event) {
		if (this.isDataAvailable()) {
			var $target = $(event.target);
			var codeId = $target.attr("id");

			var selectedOptionLabel = $target.text();
			this.$(".outputCodeDropdown").text(selectedOptionLabel);

			$("#downloadToQueue").removeClass("disabled");
			QueryTool.page.get("query").set("outputCodeSelection",selectedOptionLabel);
			EventBus.trigger("dataTableView:changeDisplayOption", codeId);
		}
		
	},

	isDataAvailable: function () {
		return !_.isEmpty(this.model.get("query").get("tableResults"));
	},

	contractResultPane: function () {
		this.$(".resultPane").css("width", "60%");
	},

	expandResultPane: function () {
		this.$(".resultPane").css("width", "95%");
	},

	moveCartToQueue: function () {
		EventBus.trigger("open:downloadToQueue", "cart");
	},

	downloadToQueue: function () {

		//console.log(QueryTool.page.get('dataTableView').model.columns.length);
		//console.log(QueryTool.page.get('dataTableView').model);
		//console.log(QTDT.totalRecords);
		var totalCells = QTDT.totalRecords * QueryTool.page.get('dataTableView').model.columns.length;
		console.log(totalCells);
		if (totalCells > System.downloadThreshold && System.downloadThreshold != 0) {

			$.ibisMessaging("dialog", "warning", "Error: The attempted download package exceeds to maximum file size. Please contact a member of your Operations Team to resolve the issue.");
		} else {
			if (!$("#downloadToQueue").hasClass("disabled")) {
				EventBus.trigger("open:downloadToQueue", "table");
			}
		}
	}
});