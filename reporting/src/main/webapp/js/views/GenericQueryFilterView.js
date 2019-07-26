/**
 * 
 */
QT.GenericQueryFilterView = BaseView.extend({
	className : "queryFilter",

	initialize : function() {
		QT.GenericQueryFilterView.__super__.initialize.call(this);
		EventBus.on("clearDataCart", this.onClearDataCart, this);
		EventBus.on("query:reset", this.onQueryReset, this);
		EventBus.on("runQuery", this.onQueryRun, this);
		EventBus.on("runJoinQuery", this.onQueryRun, this);
	},

	populateFormStructureTitleAndElement : function() {

		var form = QueryTool.page.get("forms").get(this.model.get("formUri"));
		var title = "Error:Unknown Form"
		if (typeof form != "undefined") {
			title = form.get("title");
		}
		this.$(".filter_formName").text(title);
		var elementName = this.model.get("elementName");
		this.$(".filter_element").text(elementName);

	},

	toggleFilterBody : function() {
		if (this.$(".filterBody").is(":visible")) {
			this.closeFilterBody();
		} else {
			this.openFilterBody();
		}
	},

	openFilterBody : function() {
		this.$(".filterToggle").removeClass("pe-is-i-angle-circle-down")
				.addClass("pe-is-i-angle-circle-up");
		this.$(".filterBodyContainer").removeClass("closed");
	},

	onQueryReset : function() {
		this.onClearDataCart();
	},

	closeFilterBody : function() {
		this.$(".filterToggle").removeClass("pe-is-i-angle-circle-up")
				.addClass("pe-is-i-angle-circle-down");
		this.$(".filterBodyContainer").addClass("closed");
	},

	closeFilter : function(notOnActivate) {
		if (notOnActivate) {
			EventBus.trigger("remove:filter", this.model);
		} else {
			EventBus.trigger("remove:visualFilter", this.model);
		}
		this.destroy();

	},

	showError : function(message) {
		$.ibisMessaging("primary", "error", message, {
			container : "#filterError_" + this.model.get("id")
		});
		EventBus.trigger("filters:showError");
	},

	closeError : function(message) {
		$.ibisMessaging("close", {
			type : "primary",
			container : "#filterError_" + this.model.get("id")
		});
		EventBus.trigger("filters:removeError");
	},

	/**
	 * Responds to clearing the data cart. All filters should be removed
	 * completely
	 */
	onClearDataCart : function() {
		$.ibisMessaging("close", {
			type : "primary"
		});
		this.destroy();
	},

	onQueryRun : function(obj) {
		var notOnActivate = obj.notOnActivate;
		this.closeFilter(notOnActivate);
	}

});
