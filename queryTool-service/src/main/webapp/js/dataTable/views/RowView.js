/**
 * 
 */
QTDT.RowView = BaseView.extend({
	 events: {
	     
	    },
	
	
	initialize : function() {
		 this.$el = $('<tr>', { 
			   id : this.model.cid,
			   "class" : 'table-row'
			});
		 
		 if(this.model.get("frozen")) {
			 EventBus.on("frozenrowview:removeall",this.destroy,this);
		 } else {
			 EventBus.on("rowview:removeall",this.destroy,this);
		 }
		
	},
	render: function(){

		var rowcells = this.model.get("cells");
		var thisView = this;
		var cellViewsArray = [];
	
		
		rowcells.each(function(cellmodel) {	
			var cellView = new QTDT.CellView({model: cellmodel});
			thisView.$el.append(cellView.render().$el);
			cellViewsArray.push(cellView);
			var rowHighlightClass = thisView.$el.hasClass( "highlightRow" );
			if(!rowHighlightClass && cellView.model.get("highlightRow") === true){
				thisView.$el.addClass("highlight-row");
			}
		});
		thisView.model.set("cellViews",cellViewsArray);
		thisView.$el.append('<div class="space-line"></div>');
		return this;
		
	},
	
    destroy : function() {
		
    	EventBus.off("rowview:removeall",this.destroy,this);
		this.close();
		QTDT.RowView.__super__.destroy.call(this);
	}
	
	
});