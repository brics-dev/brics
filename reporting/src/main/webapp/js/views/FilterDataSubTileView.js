/**
 * 
 */
QT.FilterDataSubTileView = BaseView.extend({
	className: "filterDataSubTile",
	tabName: "",
	parentUri: "", // used to store this particular subtile's parent. Can't use
					// models for this
	config: null,
	events: {
		"click .subTile_cartIcon": "onCartAddRemove",
		"mouseenter .filterDataSubTileText": "showTooltip",
		"mouseout .filterDataSubTileText": "hideTooltip",
		"click .filterDataSubTileText": "showDetails"
	},

	initialize: function() {
		this.template = TemplateManager.getTemplate("filterDataSubTile");
		QT.FilterDataSubTileView.__super__.initialize.call(this);

		EventBus.on("dataCart:update", this.onDataCartChange, this);
	},

	build: function(tabName, parentUri) {
		this.tabName = tabName;
		this.parentUri = parentUri;
		this.config = Config.tabConfig[tabName];
		this.listenTo(this.model, "change:isInDataCart", this.onThisChangeInDataCart);
		this.listenTo(this.getParentModel(), "change:isInDataCart", this.onParentChangeInDataCart);
		this.listenTo(this.model, "change:isVisibleTiles" + tabName, this.updateVisible);

		return this;
	},

	render: function($container) {
		this.$el.html(this.template(this.model.attributes));
		this.$el.attr("id", this.tabName + "_subtile_" + this.model.get("uri"));
		$container.append(this.$el);
		this.formatText(this.$(".filterDataSubTileText"));
		this.ellipsis(this.$(".filterDataSubTileText"),50);
		this.updateSelectedStyle();
	},

	updateVisible: function(model, value, options) {
		// value = visible so true = visible
		if (value) {
			this.$el.show();
		}
		else {
			this.$el.hide();
		}
	},

	setupTooltip: function() {
		this.$(".tooltip").each(function() {
			var link = $(this).prevAll("a").first();
			var linkPosition = link.position();
			$(this).css({
				"top": linkPosition.top + link.height(),
				"left": linkPosition.left + link.height()
			});
		});
	},

	onCartAddRemove: function() {
		if (this.isAvailable()) {
			var thisUri = this.model.get("uri");
			var parentUri = this.parentUri;
			if (this.isSelected()) {
				if (this.model instanceof QT.Form) {
					EventBus.trigger("removeFromDataCart", thisUri, parentUri);
				}
				else {
					EventBus.trigger("removeFromDataCart", parentUri, thisUri);
				}
			}
			else {
				// luckily, forms and studies are the only things that show up
				// in the tiles
				if (this.model instanceof QT.Form) {
					EventBus.trigger("addToDataCart", thisUri, parentUri);
				}
				else {
					EventBus.trigger("addToDataCart", parentUri, thisUri);
				}
			}
		}
	},

	onDataCartChange: function(formUri, studyUri) {
		if (this.model instanceof QT.Form) {
			if (this.model.get("uri") == formUri && this.parentUri == studyUri) {
				this.updateSelectedStyle();
			}
		}
		else {
			if (this.model.get("uri") == studyUri && this.parentUri == formUri) {
				this.updateSelectedStyle();
			}
		}
	},

	showDetails: function() {
		if (this.model instanceof QT.Form) {
			EventBus.trigger("open:details", this.model);
		}
		else {
			// it's a study
			EventBus.trigger("open:details", this.model);
		}
	},

	showTooltip: function() {
		this.$(".filterDataSubTileText").next(".tooltip").show();
	},

	hideTooltip: function() {
		this.$(".filterDataSubTileText").next(".tooltip").hide();
	},

	updateSelectedStyle: function() {
		if (this.isSelected()) {
			this.updateCartIcon("pe-is-ec-cart-minus selected");
		}
		else if (this.isAvailable()) {
			this.updateCartIcon("pe-is-ec-cart-plus");
		}
		else {
			this.updateCartIcon("pe-is-i-ban unavailable");
		}
	},

	isSelected: function() {
		var parentModel = this.getParentModel();
		var dataCart = QueryTool.page.get("dataCart");
		if (this.model instanceof QT.Form) {
			return dataCart.isInDataCart(this.model, parentModel);
		}
		else {
			return dataCart.isInDataCart(parentModel, this.model);
		}
	},

	isAvailable: function() {
		return QueryTool.page.isAvailable(this.model.get("uri"), this.parentUri);
	},

	updateCartIcon: function(newClassName) {
		this.$(".subTile_cartIcon").removeClass().addClass("subTile_cartIcon " + newClassName);
	},

	getParentModel: function() {
		return QueryTool.page.get(this.config.tiles.pageList).get(this.parentUri);
	},
	/*** create ellipses for extended text ***/
	ellipsis: function (minimized_elements, length) {

	    
	    minimized_elements.each(function(){    
	        var t = $(this).text();        
	        if(t.length < length) return;
	        
	        $(this).html(
	            t.slice(0,length)+'<span>... </span>'
	        );
	        
	    }); 
	    
	    

	},
	formatText: function(text){
		 text.each(function(){    
		        var t = $(this).text();        
		        
		        
		        $(this).html(
		        		t.toLowerCase().replace(/\b[a-z]/g, function(letter) {
		        			return letter.toUpperCase();
		        			})
		        );
		        
		    }); 
		
	
	}
});