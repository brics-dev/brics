/**
 * model : one of: Form, Study, DefinedQuery, DataElement
 */
QT.FilterDataTileView = BaseView.extend({
	className : "filterDataTile",
	tabName : "",
	selectionAvailable : false,
	
	config : null,
	events : {
		"click .expandCollapseButton" : "expandCollapseSubTiles",
		"click .tile_cartIcon" : "addRemoveAllSubElements",
		"click .tile_title" : "showDetails"
	},
	
	initialize : function() {
		this.template = TemplateManager.getTemplate("filterDataTile");
		// event listening is now handled in build() because it is dependent on tabName
		
		QT.FilterDataTileView.__super__.initialize.call(this);
	},
	
	render : function($container) {
		this.$el.html(this.template(this.model.attributes));
		this.$el.attr("id", this.tabName + "_tile_" + this.model.get("uri"));
		$container.find(".resultPaneContentClear").before(this.$el);
		
		this.renderSubElements();
		this.updateSubElementText();
		
		this.updateCartSelectionAvailable();
		
		QT.FilterDataTileView.__super__.render.call(this);
	},
	
	renderSubElements : function() {
		var list = this.model[this.config.tiles.subElementList];
		var $container = this.$(".tileSubContainer");
		var that = this;
		list.forEach(function(subElementModel) {
			var view = new QT.FilterDataSubTileView({model : subElementModel}).build(that.tabName, that.model.get("uri"));
			view.render($container);
		});
	},
	
	build : function(tabName) {
		this.tabName = tabName;
		this.config = Config.tabConfig[tabName];
		this.listenTo(this.model, "change:isVisibleTiles" + tabName, this.updateVisible);
		
		EventBus.on("dataCart:update", this.onDataCartChange, this);
		EventBus.on("tiles:expand", this.eventExpandSubTiles, this);
		EventBus.on("tiles:collapse", this.eventCollapseSubTiles, this);
		
		return this;
	},
	
	updateSubElementText : function() {
		var output = "";
		var pi = this.model.get("pi") || "";
		var subList = this.model[this.config.tiles.subElementList];
		if (this.model instanceof QT.Study) {
			output += "PI: " + pi + "; # " + this.config.tiles.subElementListText + ": " + subList.length;
		}
		else {
			output += "# " + this.config.tiles.subElementListText + ": " + subList.length;
		}
		this.$(".tile_subElementText").text(output);
	},
	
	updateVisible : function(model, value, options) {
		// value = visible so true = visible
		if (value) {
			this.$el.show();
		}
		else {
			this.$el.hide();
		}
	},
	
	addRemoveAllSubElements : function() {
		if (this.isAvailable()) {
			var subList = this.model[this.config.tiles.subElementList];
			var thisUri = this.model.get("uri");
			var event = "addToDataCart";
			if (this.isSelected()) {
				//event = "removeFromDataCart";
				if (this.model instanceof QT.Form) {
					//sends a list of studies, and form uri
					EventBus.trigger("removeAllStudiesFromDataCart", thisUri);
					
				}
				else {
					//sends a list of forms, and study uri
					EventBus.trigger("removeAllFormsFromDataCart", thisUri, subList);
				}
				this.collapseSubTiles();
			} else {
				for (var i = 0; i < subList.length; i++) {
					var subItem = subList.at(i);
					var subItemUri = subItem.get("uri");
					if (QueryTool.page.isAvailable(thisUri, subItemUri)) {
						if (this.model instanceof QT.Form) {
							EventBus.trigger(event, thisUri, subItemUri);
						}
						else {
							EventBus.trigger(event, subItemUri, thisUri);
						}
						
					}
				}
				this.expandSubTiles();
			}
		}
	},
	
	updateCartSelectionAvailable : function() {
		if (!this.model.get("isAvailable")) {
			
			this.updateCartIcon("pe-is-i-ban unavailable");
			this.selectionAvailable = false;
			
		} else {

			var subListLength = this.model[this.config.tiles.subElementList].length;
			if (subListLength > 0) {
				if (this.isSelected()) {
					// icon: "selected"
					this.updateCartIcon("pe-is-ec-cart-minus selected");
					this.selectionAvailable = true;
				} else {
					// icon: "unselected"
					this.updateCartIcon("pe-is-ec-cart-plus");
					this.selectionAvailable = true;
				}
			} else {
				// icon: "unavailable"
				this.updateCartIcon("pe-is-i-ban unavailable");
				this.selectionAvailable = false;
			}
		}
	},
	
	showDetails : function() {
		if (this.model instanceof QT.Form) {
			EventBus.trigger("open:details", this.model);
		}
		else {
			// it's a study
			EventBus.trigger("open:details", this.model);
		}
	},
	
	onDataCartChange : function(formUri, studyUri) {
		if (typeof formUri === "undefined" && typeof studyUri === "undefined") {
			// the event was called with no parameters, so just run the update
			this.updateCartSelectionAvailable();
		}
		else if (this.model instanceof QT.Form) {
			var checkThisUri = formUri;
		}
		else {
			var checkThisUri = studyUri;
		}
		
		if (this.model.get("uri") == checkThisUri) {
			this.updateCartSelectionAvailable();
		}
	},
	
	isSelected : function() {
		var subList = this.model[this.config.tiles.subElementList];
		var dataCart = QueryTool.page.get("dataCart");
		var subListLength = subList.length;
		if (this.model instanceof QT.Form) {
			var dataCartForm = dataCart.forms.get(this.model.get("uri"));
			if (!dataCartForm) {
				return false;
			}
			var secondaryLength = dataCartForm.get("studies").length;
			return subListLength <= secondaryLength;
		}
		else {
			// list of forms
			for (var i = 0; i < subListLength; i++) {
				if (!dataCart.isInDataCart(subList.at(i), this.model)) {
					return false;
				}
			}

			// we got through all of the forms, so all's matched
			return true;
		}
	},
	
	isAvailable : function() {
		return QueryTool.page.isAvailable(this.model.get("uri"));
	},
	
	updateCartIcon : function(newClassName) {
		this.$(".tile_cartIcon").removeClass().addClass("tile_cartIcon " + newClassName);
	},
	
	expandCollapseSubTiles : function() {
		if (this.$(".tileSubContainer").is(":visible")) {
			this.collapseSubTiles();
		}
		else {
			this.expandSubTiles();
		}
	},
	
	eventExpandSubTiles : function(tabName) {
		if (tabName == this.tabName) {
			this.expandSubTiles();
		}
	},
	
	eventCollapseSubTiles : function(tabName) {
		if (tabName == this.tabName) {
			this.collapseSubTiles();
		}
	},
	
	expandSubTiles : function() {
		var view = this;
		_.defer(function() {
			if (!view.$(".tileSubContainer").is(":visible")) {
				view.$(".tileSubContainer").show();
				view.$el.addClass("expanded");
				view.$(".expandCollapseButton .glyphicon")
					.removeClass("pe-is-i-plus-circle")
					.addClass("pe-is-i-minus-circle");
			}
		});
	},
	
	collapseSubTiles : function() {
		var view = this;
		_.defer(function() {
			if (view.$(".tileSubContainer").is(":visible")) {
				view.$(".tileSubContainer").hide();
				view.$el.removeClass("expanded");
				view.$(".expandCollapseButton .glyphicon")
					.removeClass("pe-is-i-minus-circle")
					.addClass("pe-is-i-plus-circle");
			}
		});
	}
});