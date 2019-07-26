/**
 * model : DataTable
 */
QTDT.ResultsView = BaseView.extend({
	 events: {
	     
	    },
	
	 
	
	initialize : function() {
		 
		this.$el = $('<div>', { 
				   id : "dataBody",
				   "class" : "dataBody"
				  
			});
		var vel = this.$el;
		viewModel = this.model;
		
		
		
		EventBus.on("window:resize", this.onWindowResize, this);
		
		$("#innerdiv").scroll(function(e) {
			viewModel.set("scrollLeft",$(this).scrollLeft());
			viewModel.set("scrollTop",$(this).scrollTop());
			var IWSLratio = ($(this).scrollLeft() / $(this).width())*100;
			var widthPercentage = 100 + IWSLratio;
			var newWidth = vel.data("origwidth") + $(this).scrollLeft();
			vel.width(widthPercentage+'%');
		});
			 EventBus.on("resultsview:removeall",this.destroy,this);
			 EventBus.on("resultsView:scrollTo",this.scrollTo,this);
			 EventBus.on("DataTableView:setTableWidth",this.setTableWidth,this);
			 EventBus.on("DataTableView:changeTableWidth",this.changeTableWidth,this);
			 EventBus.on("resultsView:resizeMainContainer",this.resizeMainContainer,this);
		
	},
	render: function(){
		//maker sure form label is rendered
		EventBus.trigger("query:formDetailsAvailable");
		//create ending number
		var theNumber = this.model.rows.length;
		//I need to pass, and this model into the timeout
		var dataTableModel = this.model;
		var thisView = this;
		$("#innerdiv").append(this.$el);
		
		var resultsTable = $('<table>', { 
			   id : "dataBodyTable",
			   "class" : "dataBodyTable",
			   cellspacing : 0,
			   cellpadding : 0
		});
		thisView.$el.append(resultsTable);
		if(theNumber == 0) {
			var emptyTableMsg = $('<tr>', { 
				   id : 'emptyTable',
				   "class" : 'table-row'
				}).append($('<td>').text("Your query doesn't have any results."));
			resultsTable.append(emptyTableMsg);
			thisView.distributeDeColumnWidths();
			EventBus.trigger("close:processing");
		} else {
			
			var j = 0;
			var rowViews = [];
			var grouping = 20;
			
			setTimeout(function loopFunction() {
			    try {
			    	//Added this because the model for this is being updated prior to coming here.
			    	theNumber = dataTableModel.rows.length;
			    	if(theNumber == 0) {
			    		//TODO: this should probably be an error message instead. Since this shouldn't happen
						var emptyTableMsg = $('<tr>', { 
							   id : 'emptyTable',
							   "class" : 'table-row'
							}).append($('<td>').text("Your query doesn't have any results."));
						resultsTable.append(emptyTableMsg);
						thisView.distributeDeColumnWidths();
						EventBus.trigger("close:processing");
						return thisView;
					} 
				        // perform your work here, create tables rows here.
				    	var delta = theNumber - j;
				    	var loopLength = Math.floor(delta/grouping) ? grouping + j : theNumber;
				    	for (var i = j; i < loopLength; i++) { 
					    	// rows will create cells within them
					    	datarow = dataTableModel.rows.at(j);
					    	var dataRowView = new QTDT.RowView({model: datarow});
					    	resultsTable.append(dataRowView.render().$el);
					    	if(j == 0) {//we only need one row to get the widths we need to ensure table alignment
						    	var rowCells = datarow.get("cellViews");
						    	rowViews = rowCells; 
					    	}
					    	dataTableModel.rowViews.push(dataRowView);
					    	cellCollection = dataRowView.model.get("cells");
					    	//we need to assign this row to just one cell, we use this later to adjust the rowViewWidth
					    	cellCollection.at(0).set("rowView", dataRowView);
						    j++;
				    	}
					
			  
			    	
			    }
			    catch(e) {
			        // handle any exception
			    	if ( window.console && console.log ){
			    	console.log(e);
			    	}
			    }
			     
			  
			    if (j < theNumber) {
			        setTimeout(loopFunction, 0); // timeout loop
			    }
			    else {
			        // any finalizing code
			    	dataTableModel.rowCellViews = rowViews;
			    	thisView.setRowCellWidths();
			    	
			    	EventBus.trigger("col:deselectSelectAll") //uncheck select all checkbox for biosamples
			    	EventBus.trigger("col:checkSelectedBiosamples") //make sure selected biosample checkboxes are checked, this is for pagination
			    	
			    }
			},0,thisView,rowViews);
		}
		
		return this;
		
	},
	setRowCellWidths: function() {
		
		
		//create ending number
		var cellViewsArray = this.model.rowCellViews;
		var totalCellViews = cellViewsArray.length;
		
		var thisView = this;
		
		var x = 0;
		
		var grouping = 20;
		
		setTimeout(function loopFunction() {
		    try {
		    	
		    	
		    	var delta = totalCellViews - x;
		    	var loopLength = Math.floor(delta/grouping) ? grouping + x : totalCellViews;
		    	for (var i = x; i < loopLength; i++) { 
		    		var cellView = cellViewsArray[i];
			    	cellView.calcWidth();
			    	x++;
		    	}
		    	
		        // perform your work here
		    	
		    }
		    catch(e) {
		        // handle any exception
		    	if ( window.console && console.log ){
		    	console.log(e);
		    	}
		    }
		     
		  
		    if (x < totalCellViews) {
		        setTimeout(loopFunction, 0); // timeout loop
		    }
		    else {
		    	EventBus.trigger("cell:recordCollapseWidth");
		    	thisView.distributeDeColumnWidths();
			
				
		    }
		});
		
		
	},
	distributeDeColumnWidths : function () {
		var thisView = this;
		//get columns for table and loop through them
		var headerColumns = this.model.columns;
		var headerColumnsAmount = this.model.columns.length;
		var headerCounter = 0;
		var dataTableModel = this.model;
		
		var tableWidth = 0;
	
		headerColumns.each(function(headerColumn){
			var cell = headerColumn.get("cells").at(0);
			var colWidth = headerColumn.get("width"); //(this.model.get("visible")) ? this.model.get("width") : 0; //if the column is not visible then the width is 0
			var headerColAdjustedWidth = headerColumn.get("adjustedWidth");///add this in case the column's width was adjusted.
			var maxWidth = 0;	
				
			if(headerColAdjustedWidth != -1){
				
				maxWidth = headerColAdjustedWidth;
			} else {
			
			
				if(colWidth > 200) {
					maxWidth = colWidth; //TODO: Change this to a value is we want to max out the width of the columns
				} else {
					maxWidth = colWidth;
				}
				
				if (cell != undefined && cell.get("width") > maxWidth) {
					maxWidth = cell.get("width");
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
	    	headerColumn.set("width",-1);
	    	headerColumn.set("width",maxWidth);
	    	
	    	
		});
		
		
	   		EventBus.trigger("DataTableView:setTableWidth",tableWidth);
	   		
	   		
	   		thisView.setRowHeights();
	   		if(dataTableModel.frozenColumnsExist) {
	   			 	EventBus.trigger("DataTableView:renderFrozenResults",this);
		 	}
	   		
	   		thisView.distributeRgColumnWidths();
	   		thisView.resizeMainContainer();
		
   	
	},
	distributeRgColumnWidths : function() {
		var thisView = this;
		//get columns for table and loop through them
		var rgHeaderColumns = this.model.rgColumns;
		var rgHeaderColumnsAmount = this.model.rgColumns.length;
		var rgHeaderCounter = 0;
		
		rgHeaderColumns.each(function(rgHeaderColumn){
			rgHeaderColumn.getChildrenWidth();
		});
		
		thisView.distributeFormColumnWidths();
		
		
		
	},
	distributeFormColumnWidths : function () {
		//get columns for table and loop through them
		var formHeaderColumns = this.model.formColumns;
		var formHeaderColumnsAmount = this.model.formColumns.length;
		var formHeaderCounter = 0;
		
		formHeaderColumns.each(function(formHeaderColumn){
			formHeaderColumn.getChildrenWidth();
		});
		EventBus.trigger("DataTableView:resetHiddenColumns",this);
		//open processing in create table
		EventBus.trigger("close:processing");
		//scroll to spot on table
		EventBus.trigger("resultsView:scrollTo",this);
		
		//this is a weird hack but i need to adjust the scroll so the results don't display a blank white
		var scrollPosition = $("#innerdiv").scrollLeft();
		var smallPlusScroll = scrollPosition + 1;
		var smallMinusScroll = scrollPosition - 1;
		$("#innerdiv").scrollLeft(smallPlusScroll);
		$("#innerdiv").scrollLeft(smallMinusScroll);
		
	},
	setTableWidth : function(width) {
		this.model.set("originalDataBodyWidth",width);
		this.$el.find("#dataBodyTable").width(width);
	},
	changeTableWidth : function(deltaObj) {
		var width = this.model.get("originalDataBodyWidth");
		
		var cols = this.model.columns;
		
		var totalWidth = 0;
		cols.each(function(col){
			totalWidth += (col.get("visible")) ? col.get("width") : 0;
		})
		
		$("#dataBodyTable").width(totalWidth);
		
		/*if(deltaObj.visible) {
			this.$el.find("#dataBodyTable").width(deltaObj.widthDelta);
		} else {
			width -= deltaObj.widthDelta;
			this.$el.find("#dataBodyTable").width(width);
		}*/
	},
	setRowHeights : function () {
		
		var rowViews = this.model.rowViews;
		for(var r in rowViews) {
			//set the row height so it's not a decimal
			var rowHeight = rowViews[r].$el.height()+1;
			rowViews[r].$el.height(rowHeight);
			
		}	
		
	},
    destroy : function() {
    	EventBus.off("resultsview:removeall",this.destroy,this);
		this.close();
		QTDT.ResultsView.__super__.destroy.call(this);
	},
	scrollTo : function() {
		var leftPoint = this.model.get("scrollLeft");
		var topPoint = this.model.get("scrollTop");
		$("#innerdiv").scrollLeft(leftPoint);
		$("#innerdiv").scrollTop(topPoint);		
	},/**
	 * The main container is the container around the main tabs, excluding the header.
	 * This function sizes that area to be the entire window view height minus the 
	 * header's height.
	 * NOTE: this does not work well if there is a horzontal scrollbar
	 */
	resizeMainContainer : function() {
		if($("#dataBody").length > 0)
		{
			var containerHeight = $("#resultsDatatable").height();
			var headerHeight = $("#tableHeaders").height(); // maybe outerHeight()?
			var menuHeight = $("#lengthMenuViewContainer").height();
			var paginationHeight = $("#pagerContainer").height();
			var biosampleOrderHeight = 0;
			if($("#bulkOrderButton").length){
				biosampleOrderHeight = $("#bulkOrderButton").height()+10;
			}
		
				
			var scrollHeight = $("#dataBody").get(0).scrollHeight;
		
			var newHeight = containerHeight - headerHeight - menuHeight - paginationHeight - biosampleOrderHeight;
			if(scrollHeight > newHeight)  {
				$("#dataBody").height(newHeight);
			} else if (scrollHeight != 0) {
				$("#dataBody").height(scrollHeight);
				
			}
			
		}
		
	},
	
	onWindowResize : function() {
		this.resizeMainContainer();
	}
	
});