/**
 * 
 */
QTDT.FrozenResultsView = BaseView.extend({
	 events: {
	     
	    },
	
	
	initialize : function() {
		
			 this.$el = $('<div>', { 
				 id : "frozenDataBody",
				 "class" : "frozenDataBody"
			});
			 
			 $('#dataBody').on('scroll', function () {
				    $('#frozenDataBody').scrollTop($(this).scrollTop());
				});
			 EventBus.on("window:resize", this.onWindowResize, this);
			 EventBus.on("frozenResultsView:removeall",this.destroyFrozen,this);
			 EventBus.on("DataTableView:setFrozenTableWidth",this.setFrozenTableWidth,this);
			 EventBus.on("DataTableView:changeTableWidth",this.changeFrozenTableWidth,this);
			 EventBus.on("resultsView:resizeMainContainer",this.resizeMainContainer,this);
				
	},
	render: function(){
		//create ending number
		endingFrozenNumber = this.model.frozenRows.length;
		
		if(endingFrozenNumber > 0) {
			//I need to pass, and this model into the timeout
			var dataTableModel = this.model;
			var thisView = this;
			$("#frozenInnerDiv").append(this.$el);
			var resultsTable = $('<table>', { 
				 id : "frozenDataBodyTable",
				   "class" : "frozenDataBodyTable",
				   cellspacing : 0,
				   cellpadding : 0
			});
			thisView.$el.append(resultsTable);
			var j = 0;
			var rowCellViews = [];
			setTimeout(function loopFunction() {
			    try {
			    	EventBus.trigger("open:processing", "formatting table..."); 	
			    	datarow = dataTableModel.frozenRows.at(j);
			    	var dataRowView = new QTDT.RowView({model: datarow});
			    	resultsTable.append(dataRowView.render().$el);
			    	var rowHeight = datarow.get("height");
			    	dataRowView.$el.height(rowHeight);
			    	if(j == 0) {//only need one row
			    		rowCellViews = datarow.get("cellViews");
			    	}
			    	
			    }
			    catch(e) {
			        // handle any exception
			    	if ( window.console && console.log ){
			    	console.log(e);
			    	}
			    	
			    }
			     
			    j++;
			    if (j < endingFrozenNumber) {
			        setTimeout(loopFunction, 0); // timeout loop
			    }
			    else {
			        // any finalizing code
			    	// any finalizing code
			    	dataTableModel.frozenRowCellViews = rowCellViews;
			    	thisView.setRowCellWidths();
					
			    }
			});
		}
		
		return this;
		
	},
	setRowCellWidths: function() {
		
		
		//create ending number
		var cellViewsArray = this.model.frozenRowCellViews;
		var totalCellViews = cellViewsArray.length;
		
		var thisView = this;
		
		var x = 0;
		
		setTimeout(function loopFunction() {
		    try {
		    	//EventBus.trigger("open:processing", "formatting table...");
		        // perform your work here
		    	var cellView = cellViewsArray[x];
		    	cellView.calcWidth();
		    }
		    catch(e) {
		        // handle any exception
		    	if ( window.console && console.log ){
		    	console.log(e);
		    	}
		    }
		     
		    x++;
		    if (x < totalCellViews) {
		        setTimeout(loopFunction, 0); // timeout loop
		    }
		    else {
		       
		    	thisView.distributeFrozenDeColumnWidths();
				
				
				
		    }
		});
		
		
	},
	distributeFrozenDeColumnWidths : function () {
		var thisView = this;
		//get columns for table and loop through them
		var headerColumns = this.model.frozenColumns;
		var headerColumnsAmount = this.model.frozenColumns.length;
		var headerCounter = 0;
		var tableWidth = 0;
		
		headerColumns.each(function(headerColumn){
				var cell = headerColumn.get("cells").at(0);
				var colWidth = (headerColumn.get("visible")) ? headerColumn.get("frozenWidth") : 0; //if the column is not visible then the width is 0
				var headerColAdjustedWidth = (headerColumn.get("visible")) ? headerColumn.get("adjustedWidth") : 0;///add this in case the column's width was adjusted.
				var maxWidth = 0;	
					
				if(headerColAdjustedWidth != -1){
					maxWidth = headerColAdjustedWidth;
				} else {
				
					if(colWidth == 0 ){
						maxWidth = 0;
					}else {
						maxWidth = colWidth;
					}
				}
				
				
				tableWidth += maxWidth;
				
				
				//if the column has been adjusted we have to loop through the column's cells and set their widths
				if(headerColAdjustedWidth != -1){
					var cells = headerColumn.get("cells");
					cells.each(function(cell){
						cell.set("width",-1);	
						cell.set("width",maxWidth);	
					});
					
				} else {
					if (cell != undefined) {
						cell.set("width",-1);
						cell.set("width",maxWidth);
					}
				}
				
		    	headerCounter++;
		    	if(headerCounter == headerColumnsAmount) {
		    		 EventBus.trigger("DataTableView:setFrozenTableWidth",tableWidth);
			    	thisView.distributeFrozenRgColumnWidths(); 	
		    	}
				
	    	
		});
		
		
	},distributeFrozenRgColumnWidths : function() {
		var thisView = this;
		//get columns for table and loop through them
		var frozenRgHeaderColumns = this.model.frozenRgColumns;
		var frozenRgHeaderColumnsAmount = this.model.frozenRgColumns.length;
		var frozenRgHeaderCounter = 0;
		frozenRgHeaderColumns.each(function(frozenRgHeaderColumn){
			frozenRgHeaderColumn.getFrozenChildrenWidth();
		});
		
		thisView.distributeFrozenFormColumnWidths();
		
		
		
	},
	distributeFrozenFormColumnWidths : function () {
		//get columns for table and loop through them
		var frozenFormHeaderColumns = this.model.frozenFormColumns;
		var frozenFormHeaderColumnsAmount = this.model.frozenFormColumns.length;
		var frozenFormHeaderCounter = 0;
		frozenFormHeaderColumns.each(function(frozenFormHeaderColumn){
			frozenFormHeaderColumn.getFrozenChildrenWidth();
		});
		
		
		this.resetHiddenColumns();
		this.resizeMainContainer();
    	EventBus.trigger("close:processing");
		
	},
	resetHiddenColumns : function () {
		///create array of col models that are frozen
		var hiddenColumnsArray = this.model.columns.where({visible: false});
		
		for(var i in hiddenColumnsArray) {
			var hiddenCol = hiddenColumnsArray[i];
			var cells = hiddenCol.get("cells");
			var toggleVisibility = hiddenCol.get("visible");
			if(hiddenCol.get("frozenView") != null) {
				hiddenCol.get("frozenView").$el.css("display","none");
			}
			cells.each(function(cell){
				cell.set("visible",true);	
				cell.set("visible",toggleVisibility);	
			});
			
			//do I need to hide my parents
			
			var parent = hiddenCol.get("parent");
			var grandParent = parent.get("parent");
			var siblingAmount = parent.get("children").where({visible:true, frozen:true}).length;
			var singleParentAmount = grandParent.get("children").where({visible:true, frozen:true}).length;
			if(siblingAmount == 0) {
				if(parent.get("frozenView") != null) {
					parent.get("frozenView").$el.css("display","none");
				}
			}
			
			if(singleParentAmount == 0) {
			
				if(grandParent.get("frozenView") != null) {
					grandParent.get("frozenView").$el.css("display","none");
				}
				
			}
		}
	},
	setFrozenTableWidth : function(width) {
		this.model.set("originalFrozenDataBodyWidth",width);
		this.$el.find("#frozenDataBodyTable").width(width);
	},
	changeFrozenTableWidth : function(deltaObj) {
		if(this.model.frozenColumnsExist) {
			var width = this.model.get("originalFrozenDataBodyWidth");
			
			var cols = this.model.frozenColumns;
			
			var totalWidth = 0;
			cols.each(function(col){
				totalWidth += (col.get("visible")) ? col.get("width") : 0;
			})
			
			$("#frozenDataBodyTable").width(totalWidth);
			/*if(deltaObj.visible) {
				this.$el.find("#frozenDataBodyTable").width(deltaObj.widthDelta);
			} else {
				width -= deltaObj.widthDelta;
				this.$el.find("#frozenDataBodyTable").width(width);
			}*/
		}
	},
    destroy : function() {
    	EventBus.off("frozenResultsView:removeall",this.destroy,this);
		this.close();
		QTDT.FrozenResultsView.__super__.destroy.call(this);
	},/**
	 * The main container is the container around the main tabs, excluding the header.
	 * This function sizes that area to be the entire window view height minus the 
	 * header's height.
	 * NOTE: this does not work well if there is a horzontal scrollbar
	 */
	resizeMainContainer : function() {
		if($("#frozenDataBody").length > 0 ) {
			var containerHeight = $("#resultsDatatable").height();
			var headerHeight = $("#frozenTableHeaders").height(); // maybe outerHeight()?
			var menuHeight = $("#lengthMenuViewContainer").height();
			var paginationHeight = $("#pagerContainer").height();
			var scrollHeight = 0;
			var biosampleOrderHeight = 0;
			if($("#bulkOrderButton").length){
				biosampleOrderHeight = $("#bulkOrderButton").height()+10;
			}
			
			
				scrollHeight = $("#frozenDataBody")[0].scrollHeight;
			
			
			var newHeight = containerHeight - headerHeight - menuHeight - paginationHeight - biosampleOrderHeight;
			if( scrollHeight > newHeight) {
				$("#frozenDataBody").height(newHeight);
			} else if (scrollHeight != 0) {
				$("#frozenDataBody").height(scrollHeight);
				
			}
		}
	},
	onWindowResize : function() {
		
		this.resizeMainContainer();
	}
	
	
});