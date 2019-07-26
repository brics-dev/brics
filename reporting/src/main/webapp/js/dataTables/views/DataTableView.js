/**
 * model: QTDT.DataTable
 */
QTDT.DataTableView = BaseView.extend({
	
	 events: {
		 "click #bulkOrderButton" : "addSelectedSamples"
	    
	    },
	   
	
	pager : null,
	lengthMenu : null,
	resultsView : null,
	frozenResultsView : null,
	renderedOnce: false,
	initialize : function() {
		
		 
		 
		 
		this.model.set("rows", new QTDT.Rows());
		//this.model.set("columns", new QTDT.Cols());
		this.model.set("dataTables", new QTDT.DataTables());
		
		this.model.set("ColCells", new QTDT.ColCells());
		this.model.set("RowCells",new QTDT.RowCells());
		
		
		this.template = TemplateManager.getTemplate("dataTableTemplate");
		window.onresize = function(event) {
			EventBus.trigger("window:resize", window.innerHeight);
		};
		
		EventBus.on("window:resize", this.onWindowResize, this);
		
		EventBus.on("select:refineDataTab",this.render,this);
		EventBus.on("column:lock",this.freezeColumns,this);
		EventBus.on("column:unlock",this.freezeColumns,this);
		EventBus.on("clearDataCart", this.destroyTable, this);
		EventBus.on("DataTableView:destroyTable", this.destroyTable, this);
		EventBus.on("renderResults",this.renderResults,this);
		EventBus.on("DataTableView:addQueryListener",this.addQueryListener,this);
		EventBus.on("DataTableView:removeQueryListener",this.removeQueryListener,this);
		EventBus.on("DataTableView:resetHiddenColumns",this.resetHiddenColumns,this);
		
		EventBus.on("DataTableView:setTableWidth",this.setTableWidth,this);
		EventBus.on("DataTableView:setFrozenTableWidth",this.setFrozenTableWidth,this);
		EventBus.on("DataTableView:changeTableWidth",this.changeFrozenTableWidth,this);
		EventBus.on("DataTableView:changeTableWidth",this.changeTableWidth,this);
		EventBus.on("DataTableView:adjustFrozenColumns",this.adjustFrozenColumns,this);
		EventBus.on("DataTableView:renderFrozenResults",this.renderFrozenResults,this);
		
		this.listenTo(QueryTool.query, "change:tableResults", this.render);
		
		
		QTDT.DataTableView.__super__.initialize.call(this);
		
	},
	
	render : function() {
		
		//#resultsDatatableContainer
		//switch to this tab
		EventBus.trigger("openDataTableViewTab");
		
		if(!this.$el.parent().is(":visible")) {
			return;
		}
		
	
		if(this.model.frozenColumnsExist) {
			this.destroyFrozenTable(); //if we are rendering a new table we should destroy a frozen table if any existed
		}
		this.loadData();
		
		this.$el.html(this.template(this.model.attributes)); //TODO: figure out how we can utilize this more
		
		this.resizeMainContainer(Window.innerHeight);
		//If there is data let's build the table
		if(this.model.get("data") !== undefined && !_.isEmpty(this.model.get("data"))) {
			//add length Menu
			
			var perPage = QueryTool.query.get("limit");
			var offset = QueryTool.query.get("offset");
			
			this.lengthMenu = new QTDT.LengthMenu({
				current : perPage
			});
			
			this.pager = new QTDT.Pager({
				currentPage : (offset/perPage) + 1,
				perPage : perPage
			});	
			
			var lengthMenuView = new QTDT.LengthMenuView({model:this.lengthMenu });
			this.$el.prepend(lengthMenuView.render().$el);
			
			//create results and header container
			this.$el.find("#tableContainer").append( $('<div>', { 
			    id: 'innerdiv',
			    "class" : 'base-container base-layer'
			}));
			
			//create table 
			this.createTable();	
			
			//set up pagination
			var rawData = this.model.get("data");
			var vTotalRecords = 0;
			if(rawData.data !== undefined && !_.isEmpty(rawData.data)) {
				vTotalRecords = rawData.data.iTotalRecords
			} else {
				vTotalRecords = rawData.iTotalRecords;
			}
			
			this.pager.set("totalPages",Math.ceil(vTotalRecords / lengthMenuView.model.get("current")));
			var pagerView = new QTDT.PagerView({model: this.pager});
		
			this.$el.append(pagerView.render().$el);
			
			this.resultsView.resizeMainContainer();
			//create bulk order buttong
			this.addBulkOrderButton();
			
			this.renderedOnce = true;
			
		}
		
	},
	/** this is used to refresh rowdata and keep headers intact */
	renderResults : function() {
		
		//if the full table hasn't been previously rendered let's go back and do that.
		if(this.model.columns.length == 0 || !this.renderedOnce) {
			this.render();
			return;
		}
		EventBus.trigger("openDataTableViewTab");
		//only render is table is visible
		if(!this.$el.parent().is(":visible")) {
			return;
		}
		
		this.loadRowData();
		
		//If there is data let's build the table
		if(this.model.get("data") !== undefined && !_.isEmpty(this.model.get("data"))) {
			EventBus.trigger("open:processing", "formatting table...");
			///Create Data Body //new REsults View
			this.resultsView.destroy();
			this.resultsView = new QTDT.ResultsView({model: this.model})
			this.resultsView.render();
			this.resultsView.resizeMainContainer();
			/*if(this.model.frozenColumnsExist) {
				this.renderFrozenResults();
			}*/
			
			
			//reset pagination for filters 
			//set up pagination
			var rawData = this.model.get("data");
			var vTotalRecords = 0;
			if(rawData.data !== undefined && !_.isEmpty(rawData.data)) {
				vTotalRecords = rawData.data.iTotalRecords
			} else {
				vTotalRecords = rawData.iTotalRecords;
			}
			
			this.pager.set("totalPages",Math.ceil(vTotalRecords / this.lengthMenu.get("current")));
			
		}
		
	},
	/** This method will load headers and rowdata **/
	loadData : function() {
		//Deep clone
		
		var resultsData = $.extend(true,{}, QueryTool.query.get("tableResults"));
		
		this.model.set("data",resultsData);
		if(this.model.get("data") !== undefined && !_.isEmpty(this.model.get("data"))) {
			this.model.loadData(resultsData);  //this will set the headers and data for the datatable
			this.model.set("rendered", true);
		}
		
	},
	/** This method will load just row data **/
	loadRowData : function() {
		//Deep clone
		
		var resultsData = $.extend(true,{}, QueryTool.query.get("tableResults"));
		
		this.model.set("data",resultsData);
		if(this.model.get("data") !== undefined && !_.isEmpty(this.model.get("data"))) {
			this.model.loadRowData(resultsData);  //this will set row data for the datatable
		}
		
	},
	
	onWindowResize : function() {
		this.resizeMainContainer(Window.innerHeight);
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
	
	createTable: function(){
		EventBus.trigger("open:processing", "formatting table...");
		
		//updating options in the schema drop down
		EventBus.trigger("updateSchemaOptions");
		
		var thisDataTableView = this;
		
		///4.Create Header Container
		$("#innerdiv").append($('<table>', { 
			   id : "tableHeaders",
				   cellspacing : 0,
				   cellpadding : 0
		}));
		
		//create form headers
		$("#tableHeaders").append($('<tr>', { 
		   id : "formHeader",
			class : 'table-row'
		}))
		
		this.model.formColumns.each(function(header){
			var formCol = new QTDT.ColView({model: header})
				$("#formHeader").append(formCol.render().$el);
				header.set("originalView",formCol);
			
		});
		
		
		//create repeatable group headers
		$("#tableHeaders").append($('<tr>', { 
			   id : "rgHeader",
				class : 'table-row'
		}));
			
		this.model.rgColumns.each(function(header){
				var rgCol = new QTDT.ColView({model: header})
				$("#rgHeader").append(rgCol.render().$el);
				header.set("originalView",rgCol);
		});
		
		
		//create de columns
		$("#tableHeaders").append($('<tr>', { 
		   id : "deHeader",
			class : 'table-row'
		}));
			
		this.model.columns.each(function(header){
				var deCol = new QTDT.ColView({model: header})
				$("#deHeader").append(deCol.render().$el);
				
				//in order to get the accurate width of the col cell, we need to get sum the widths of the content
				var additionalWidth = 70;
				var modelName = header.get("name");
				if (modelName == "OrderableBiosampleID") {
					additionalWidth = 140;
				} else if  (modelName == "Study ID") {
					additionalWidth = 0;
				}
				calculatedColWidth = deCol.$el.find(".colhdr").width() + additionalWidth;
				deCol.model.set("width",-1);
				deCol.model.set("width",calculatedColWidth);
				header.set("originalView",deCol);
				
		});
	
		
		///4.Create Data Body
		this.resultsView = new QTDT.ResultsView({model: this.model})
		//EventBus.trigger("close:processing");
		$("#innerdiv").append(this.resultsView.render().$el);
		
	
		
	},
	createFrozenColumns : function () {
		
		///5. create frozen headers if needed
		if(this.model.frozenColumnsExist) {
			
			///1. Create Table div Container
			$("#innerdiv").before( $('<div>', { 
			    id: 'frozenInnerDiv',
			    class : 'base-container freeze'
			}));

			///4.Create Header Container
			$("#frozenInnerDiv").append($('<table>', { 
				   id : "frozenTableHeaders",
				   cellspacing : 0,
				   cellpadding : 0
			}));
			
			//create frozen form headers
			$("#frozenTableHeaders").append($('<tr>', { 
			   id : "frozenFormHeader",
				class : 'table-row'
			}))
				
			this.model.frozenFormColumns.each(function(header){
					var formCol = new QTDT.ColView({model: header})
					formCol.frozen = true;
					$("#frozenFormHeader").append(formCol.render().$el);
					header.set("frozenParentWidth",formCol.$el.width());
					header.set("frozenView",formCol);
					
			});
			
			//create repeatable group headers
			$("#frozenTableHeaders").append($('<tr>', { 
				   id : "frozenRgHeader",
					class : 'table-row'
			}))
				
			this.model.frozenRgColumns.each(function(header){
					var rgCol = new QTDT.ColView({model: header});
					rgCol.frozen = true;
					$("#frozenRgHeader").append(rgCol.render().$el);
				
					header.set("frozenParentWidth",rgCol.$el.width());
					header.set("frozenView",rgCol);
			});
			
			//create de columns
			$("#frozenTableHeaders").append($('<tr>', { 
				   id : "frozenDeHeader",
					class : 'table-row'
			}))
				
			this.model.frozenColumns.each(function(header){
					var deCol = new QTDT.ColView({model: header});
					$("#frozenDeHeader").append(deCol.render().$el);
					
					calculatedColWidth = deCol.model.get("width");
					deCol.model.set("frozenWidth",-1);
					deCol.model.set("frozenWidth",calculatedColWidth);
					header.set("frozenView",deCol);
					
			});
			
			///4.Create Data Body
			this.frozenResultsView = new QTDT.FrozenResultsView({model: this.model})
			$("#frozenInnerDiv").append(this.frozenResultsView.render().$el);
			this.frozenResultsView.resizeMainContainer();
			
		}//end create frozen
	},
	/** this is used to refresh rowdata and keep headers intact */
	renderFrozenResults : function() {
		//If there is data let's build the table
		this.model.loadFrozenColumns(this.selectedFrozenColumn.model);
		if(this.model.get("data") !== undefined && !_.isEmpty(this.model.get("data"))) {
			
			///4.Create Data Body //new REsults View
			this.frozenResultsView.destroy();
			this.frozenResultsView = new QTDT.FrozenResultsView({model: this.model})
			this.frozenResultsView.render();
			this.frozenResultsView.resizeMainContainer();
		
			
		}
		
	},
	adjustFrozenColumns : function () {
		if(this.model.frozenColumnsExist) {
			var frozenHeaders = this.model.frozenColumns;
			
			frozenHeaders.each(function(header){
				/*** This may be an only child so we may have to adjust the widths **/
				var parentWidth = 0;
				var grandParentWidth = 0;
				var parent = header.get("parent");
				var grandParent = parent.get("parent");
				var siblingAmount = parent.get("children").where({visible:true, frozen:true}).length - 1;
				var singleParentAmount = grandParent.get("children").where({visible:true, frozen:true}).length - 1;
				var maxWidth = header.get("frozenWidth");
				if(siblingAmount == 0 && singleParentAmount == 0) {
					parentWidth = parent.get("frozenParentWidth");
					grandParentWidth = grandParent.get("frozenParentWidth");
					
					var largest = grandParentWidth;
					if(parentWidth > grandParentWidth){
						largest = parentWidth
					}
					
					if(largest > maxWidth) {
						maxWidth = largest;
					}
					header.set("width",-1);
					header.set("width",maxWidth);
					header.set("frozenWidth",-1);
					header.set("frozenWidth",maxWidth);
					var cell = header.get("cells").at(0);
					cell.set("width",-1);
					cell.set("width",maxWidth);
				}
			});
			
			//frozenWidth
			
			frozenHeadersArray = frozenHeaders.where({visible:true});
		    var frozenTotalWidth = 0;
		    for (var i in frozenHeadersArray) {
		    	frozenTotalWidth += frozenHeadersArray[i].get("width");
		    }
			EventBus.trigger("DataTableView:setFrozenTableWidth",frozenTotalWidth);
			//normal Width
		    normalHeadersArray = this.model.columns.where({visible:true});
		    var normalTotalWidth = 0;
		    for (var i in normalHeadersArray) {
		    	normalTotalWidth += normalHeadersArray[i].get("width");
		    }
		    EventBus.trigger("DataTableView:setTableWidth",normalTotalWidth);
		}
		/*******/
		
	},
	/** this method is used to lock and unlock columns **/
	freezeColumns : function (selectCol) {
		this.destroyFrozenTable();
		this.selectedFrozenColumn = selectCol;
		this.model.loadFrozenColumns(selectCol.model);
		this.createFrozenColumns();
		
	},
	addBulkOrderButton : function () {
		if ($("[id^='OrderableBiosampleID']").length) {
			orderButton = '<a id="bulkOrderButton"  class="buttonWithIcon addSelectedSamples" ><span class="icon pe-is-ec-basket-up"></span><span class="ui-button-text ui-c">Add Selected Samples to Cart</span></a><div class="space-line"></div>';
			if (!$('#bulkOrderButton').length) {
						this.$el.append(orderButton);
			}
		}
	},
	addSelectedSamples : function(e) {
		//$(e.target).bind('click', false);
		//$(e.target).attr('disabled', 'disabled');
		//var theButton = $(e.target);
		//$(e.target).preventDefault();
		EventBus.trigger("col:addSelectedSamples",this);
		
	},
	addQueryListener : function () {
		this.listenTo(QueryTool.query, "change:tableResults", this.render);
	},
	removeQueryListener : function () {
		this.stopListening(QueryTool.query, "change:tableResults");
	},
	resetHiddenColumns : function() {
		this.model.columns.each(function(header){
			
			if(!header.get("visible")){
				EventBus.trigger("column:removeVisibleListener",this);
				header.set("visible",true);
				EventBus.trigger("column:addVisibleListener",this);
				header.setCurrentShowHide();
			}
			
		});
		
	},
	setTableWidth : function(width) {
		this.model.set("originalTableHeaderWidth",width);
		$("#tableHeaders").width(width);
	},
	setFrozenTableWidth : function(width) {
		this.model.set("originalFrozenTableHeaderWidth",width);
		$("#frozenTableHeaders").width(width);
	},
	changeTableWidth : function(deltaObj) {
		var width = this.model.get("originalTableHeaderWidth");
		
		var cols = this.model.columns;
		
		var totalWidth = 0;
		cols.each(function(col){
			totalWidth += (col.get("visible")) ? col.get("width") : 0;
		})
		
		$("#tableHeaders").width(totalWidth);
		
		/*if(deltaObj.visible) {
			$("#tableHeaders").width(deltaObj.widthDelta);
		} else {
			width -= deltaObj.widthDelta;
			$("#tableHeaders").width(width);
		}*/
	},
	changeFrozenTableWidth : function(deltaObj) {
		
		if(this.model.frozenColumnsExist) {
			var width = this.model.get("originalFrozenTableHeaderWidth");
			
			var cols = this.model.frozenColumns;
			
			var totalWidth = 0;
			cols.each(function(col){
				totalWidth += (col.get("visible")) ? col.get("width") : 0;
			})
			
			$("#frozenTableHeaders").width(totalWidth);
			/*if(deltaObj.visible) {
				$("#frozenTableHeaders").width(deltaObj.widthDelta);
			} else {
				width -= deltaObj.widthDelta;
				$("#frozenTableHeaders").width(width);
			}*/
		}
	},
	
	destroyTable : function() {
		//if we are creating a new table we should destroy all existing views that are related to the table
		EventBus.trigger("colview:removeall",this);
		EventBus.trigger("rowview:removeall",this);
		//EventBus.trigger("cellview:removeall",this);
		EventBus.trigger("hamburgerview:removeall",this);
		EventBus.trigger("resultsview:removeall",this);
		
		this.renderedOnce = false;
		
		//clear results from table
		this.removeQueryListener();
		QueryTool.query.set("tableResults",{});
		
		this.model.set("data", []);
		//clear form cols
		this.model.formColumns.reset(null);
		//clear rg cols
		this.model.rgColumns.reset(null);
		//clear de cols
		this.model.columns.reset(null);
		//clear rows
		this.model.rows.reset(null);
		
		this.addQueryListener();
		//remove pager
		if(this.pager) {
			this.pager.destroy();
		}
		//remove length menu
		if(this.lengthMenu) {
			this.lengthMenu.destroy();
		}
		
		//remove add samples button
		this.$("#bulkOrderButton").remove();
		//also empty collections for the frozen table
		this.destroyFrozenTable();
		
	},
	destroyFrozenTable : function () {
		//if we are creating a new table we should destroy all existing views that are related to the table
		EventBus.trigger("frozencolview:removeall",this);
		EventBus.trigger("frozenrowview:removeall",this);
		//EventBus.trigger("frozencellview:removeall",this);
		EventBus.trigger("frozenhamburgerview:removeall",this);
		//also empty collections for the frozen table
		$("#frozenInnerDiv").remove();
		this.model.clearFrozenColumns();
		
	},
	
	destroy : function() {
		
		EventBus.off("window:resize", this.onWindowResize, this);
		EventBus.off("select:refineDataTab",this.render,this);
		EventBus.off("column:lock",this.freezeColumns,this);
		EventBus.off("column:unlock",this.freezeColumns,this);
		this.close();
		QTDT.DataTableView.__super__.destroy.call(this);
	}
		
	
	
	
});