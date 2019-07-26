/**
 * 
 */
QTDT.LengthMenuView = BaseView.extend({
	events: {
	      "click .lengthMenuOption" : "changeLength"
	},
	initialize : function() {
		this.$el = $('<div>', { 
			   id : "lengthMenuViewContainer"
		});
		this.template = TemplateManager.getTemplate("lengthMenuTemplate"); 
		this.listenTo(this.model, 'destroy', this.destroy);
		EventBus.on("query:formDetailsAvailable", this.updateJoinDescription, this);
		EventBus.on("clearDataCart", this.updateJoinDescription, this);
	},
	
	 render: function() {
	      this.$el.html(this.template(this.model.toJSON()));
	      return this;
	    },
	    
	    destroy : function() {
			this.close();
			QTDT.LengthMenuView.__super__.destroy.call(this);
		},
		
		changeLength :function(e) {
			EventBus.trigger("DataTableView:removeQueryListener")
			var newLength = Number($(e.target).data("value"));
			this.model.set("current", newLength);
			var selected = this.model.get("optionCollection").findWhere({selected: true});
			
			if(selected != undefined){ selected.set("selected", false); }
			
			
			this.model.get("optionCollection").findWhere({length: newLength}).set("selected", true);
			this.model.set("htmlOptions",this.model.get("optionCollection").toJSON());
			perPage = this.model.get("current");/// this is how many rowse per page
			QueryTool.query.set("limit", perPage);
			QueryTool.query.set("offset", 0);
			QueryTool.query.paginate();
		
			$(".lengthMenuDropdown").html(newLength);
			
			
		},
	
	updateJoinDescription : function() {
		var output = " of ";
		var data = QueryTool.query.get("formDetails");
		if (data.length == 0) {
			output = "";
		}
		else {
			for (var i = 0; i < data.length; i++) { 
				if (i > 0) {
					output += " joined with ";
				}
				var formJson = data[i];
				var form = QueryTool.page.get("forms").get(formJson.uri);
				output += "<b>" + form.get("title") + "</b>";
			}
		}
		this.$(".formJoinDescription").html(output);
	}
	
	
	
});