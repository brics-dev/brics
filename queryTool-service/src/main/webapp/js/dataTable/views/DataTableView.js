/**
 * model: QTDT.DataTable
 */
QTDT.DataTableView = BaseView.extend({

	events: {
		"click #bulkOrderButton": "addSelectedSamples",
		// events for table cells
		"click .expandData": "expandData",
		"click .collapseData": "collapseData",
		"click .viewTriplanar": "viewTriplanar",
		"click .downloadImage": "downloadImage",
		"click .biosample": "addBiosample",
		"click .bioSampleItem": "addItemToBioSampleList",
		"click .study_details": "viewStudyDetails",
		"click .downloadFile": "downloadFile",
		// events for column headers
		"click .frozenIcon": "frozenIcon",
		"click .sorting": "sortAsc",
		"click .sort_asc": "sortDesc",
		"click .sort_desc": "noSort",
		"click .tableHamburger": "openHamburger",
		"click #sampleCheckAll": "checkAllBioSamples"



	},

	renderedOnce: false,
	inTableData: [],
	hiddenColumns: [],
	hiddenColumnsProperties: [], //this is used in case the table has not been rendered yet
	hideColumn: false,
	numRows: 0,
	// frozen columns info:
	frozenIndex: 0,           // the index of the icon the user just froze/unfroze
	frozenOffsetX: 0,         // the current index of the left-most frozen visible cell
	scrollOffsetX: 0,         // the current index of the left-most visible cell
	scrollPositionX: 0,       // the current scrollX position
	frozenScrollPositionX: 0, // the frozen scrollX position
	frozenScrollOffsetX: 0,   // the frozen scrollX offset.
	updateFrozen: true,
	headerData: [],
	headerColor: "",

	updateRepeatableGroupFunction: function () {},
	sortFunction: function () {},
	applyVirtFilters: function () {},
	renderVirtTable: function () {},
	applyVirtDisplayOption: function () {},
	defaultTableHeight : 800,
	defaultTableWidth : 1040,

	initialize: function () {
		this.inTableData.columns = [];



		this.model.set("rows", new QTDT.Rows());
		this.model.set("dataTables", new QTDT.DataTables());

		this.model.set("ColCells", new QTDT.ColCells());
		this.model.set("RowCells", new QTDT.RowCells());


		this.template = TemplateManager.getTemplate("dataTableTemplate");
		window.onresize = function (event) {
			EventBus.trigger("window:resize", window.innerHeight);
		};

		EventBus.on("window:resize", this.onWindowResize, this);
		EventBus.on("renderTable", this.render, this);
		EventBus.on("rerenderTable", this.rerender, this);
		EventBus.on("column:lock", this.freezeColumns, this);
		EventBus.on("column:unlock", this.freezeColumns, this);
		EventBus.on("query:reset", this.destroyTable, this);
		EventBus.on("clearDataCart", this.destroyTable, this);
		EventBus.on("DataTableView:destroyTable", this.destroyTable, this);
		EventBus.on("renderResults", this.renderResults, this);
		EventBus.on("DataTableView:resetHiddenColumns", this.resetHiddenColumns, this);

		EventBus.on("DataTableView:setTableWidth", this.setTableWidth, this);
		EventBus.on("DataTableView:setFrozenTableWidth", this.setFrozenTableWidth, this);
		EventBus.on("DataTableView:changeTableWidth", this.changeFrozenTableWidth, this);
		EventBus.on("DataTableView:changeTableWidth", this.changeTableWidth, this);
		EventBus.on("DataTableView:adjustFrozenColumns", this.adjustFrozenColumns, this);
		EventBus.on("DataTableView:renderFrozenResults", this.renderFrozenResults, this);
		EventBus.on("DataTableView:reloadRepeatableGroup", this.reloadRepeatableGroup, this);

		EventBus.on("hamburgerview:showHideCol", this.toggleShowHide, this);
		EventBus.on("addTohiddenColumnsProperties",this.addTohiddenColumnsProperties,this);
		EventBus.on("clearDataCart", this.updateJoinDescription, this);
		
		EventBus.on("applyFilters", this.applyFilters, this);
		EventBus.on("dataTableView:changeDisplayOption", this.applyDisplayOption, this);
		EventBus.on("query:formDetailsAvailable", this.updateJoinDescription, this);
		EventBus.on("set:queryState", this.setQueryState, this);
		EventBus.on("resultsView:resizeMainContainer",this.rerender,this);


		QTDT.DataTableView.__super__.initialize.call(this);

	},

	rerender: function () {
		this.hideColumn = true;
		this.renderVirtTable();
	},
	render: function () {
		// #resultsDatatableContainer

		if (!this.$el.parent().is(":visible")) {
			return;
		}

		// switch to this tab
		EventBus.trigger("openDataTableViewTab");
		
//		if (this.model.frozenColumnsExist) {
//			this.destroyFrozenTable(); // if we are rendering a new table we
//										// should destroy a frozen table if any
//										// existed
//		}
		
		//empty biosample arrays
		this.model.set("biosampleArray",[]);
		this.model.set("unselectedBioSampleArray",[]);
		
		this.loadData();
		
		this.$el.html(this.template(this.model.attributes)); // TODO: figure
																// out how we
																// can utilize
																// this more

		this.resizeMainContainer(Window.innerHeight);
		// If there is data let's build the table
		if (this.model.get("data") !== undefined && !_.isEmpty(this.model.get("data"))) {
			// add length Menu

			// create results and header container
			this.$el.find("#tableContainer").append($('<div>', {
				id: 'innerdiv',
				"class": 'base-container base-layer'
			}));
			
			this.updateJoinDescription();
			EventBus.trigger("updateResultsCount");

			// create table
			this.createTable();

			// set up pagination
			var rawData = this.model.get("data");


			if (rawData.hasHighlightedGuid !== "undefined" && rawData.hasHighlightedGuid === true) {
				$.ibisMessaging("dialog", "warning", "This query contains a GUID(s) that has a change in diagnosis. GUIDs with a change in diagnosis are highlighted in yellow. You can filter the returned data to only display GUIDs with a change in diagnosis/highlighted by using the GUID column filter menu.");
			}

			this.renderedOnce = true;

		}

	},
	/** this is used to refresh data on tabs */
	renderResults: function () {
		
		if (this.model.get("queryState")) {
			// switch to this tab
			EventBus.trigger("openDataTableViewTab");
			this.render();
			this.model.set("queryState",0);
			QueryTool.query.set("onReload",false);
			return;
		}
	},
	/** This method will load headers and rowdata * */
	loadData: function () {

		var resultsData = $.extend(true, {}, QueryTool.query.get("tableResults"));

		// once the virtual table headers are styled this can be removed:
		this.model.set("data", resultsData);
		if (this.model.get("data") !== undefined && !_.isEmpty(this.model.get("data"))) {
			this.model.loadData(resultsData); // this will set the headers and
												// data for the datatable
			this.model.set("rendered", true);
		}

		// Puts the data in the format d3 expects for the virtual table:
		if (Object.keys(resultsData).length) {
			// extract virtual table data
			var headers = [];
			var forms = [];
			var repeatableGroups = [];
			var columns = [];
			var groupIndex = 0;
			// header offset

			resultsData.header.forEach(function(top, a) {
				// child count for forms = total grandchildren
				formObj = {
					name: top.name,
					selfIndex: a,
					parentIndex: -1,
					grandParentIndex: -1,
					childCount: 0
				};

				top.repeatableGroupHeaders.forEach(function(group, b) {
					// add grandchildren to count of form
					formObj.childCount += group.dataElementHeaders.length;
					rgObj = {
						name: group.name,
						selfIndex: b,
						parentIndex: a,
						// grandParentId: a,
						grandParentIndex: -1,
						childCount: group.dataElementHeaders.length
					};
					repeatableGroups.push(rgObj);
					group.dataElementHeaders = group.dataElementHeaders.map(function(value, i) {
						return {
							name: value,
							selfIndex: i,
							parentIndex: b,
							parentId: groupIndex,
							grandParentIndex: a,
							childCount: 0,
						};
					})

					columns = columns.concat(group.dataElementHeaders);
					groupIndex += 1;
				})
				forms.push(formObj);
			});
			headers.push(forms);
			headers.push(repeatableGroups);
			headers.push(columns);

			this.inTableData = resultsData.data.aaData.map(function(row) {
				output = {};
				row.forEach(function(val, i) {
					output[i+"_"+columns[i].name] = val;
				});
				return output;
			});

			this.inTableData.columns = columns;
			this.inTableData.headers = headers; // e.g.
												// [["one","two"],["childoneone","childonetwo","childtwoone"],["a","b","c","d"]]
			// might want iTotalRecords instead
			this.numRows = resultsData.data.iTotalDisplayRecords;
		}

	},
	/** This method will load just row data * */
	loadRowData: function (resultsData) {
		// when this function is triggered we already know that the data exists
		// so just
		// pass it through to loadRowData:
		this.model.loadRowData(resultsData); // this will set row data for
												// the datatable
	},

	onWindowResize: function () {
		this.resizeMainContainer(Window.innerHeight);
	},



	/**
	 * The main container is the container around the main tabs, excluding the
	 * header. This function sizes that area to be the entire window view height
	 * minus the header's height. NOTE: this does not work well if there is a
	 * horzontal scrollbar
	 */
	resizeMainContainer: function (innerHeight) {
		var headerHeight = $("#header").height(); // maybe outerHeight()?
		var navHeight = $("#navigation").height();
		$("#mainContent").height(innerHeight - headerHeight - navHeight - Config.windowHeightOffset);
	},

	/**
	 * Virtual table creation and re-rendering. resize=true forces a complete
	 * re-build, like when a column is deleted.
	 */
	makeVirtTable: function (inTableData, config, resize) {
		var that = this;
		// Type: 1. rows change because of scroll, 2. rows changed because
		// repeatable group expansion, or collapse
		var type = 1;
		var activeFilter = false; // this tells us we are filtering
		// Model, when there is a change for repeatable groups we need to send a
		// model to the query object
		var rgModel = {};
		//Object used to send chosen value of display options to Query
		var displayOptionModel = {"displayOption" : "pv"};

		var tableData = inTableData;
		// header container - sized to fit the columns for scrolling in x
		var tableHead = config.container.select(".headerScroll").select(".header");
		if (tableHead.empty()) {
			tableHead = config.container.append("div").classed("headerScroll", true)
				.append("div").classed("header", true);
		}
		// container for 2D array of table entries (not a <table>)
		// sized to fit the entire grid, for scrolling in x,y
		
		var tbody = config.container.select(".centerScroll").select(".tableBody");
		var scroller = config.container.select(".centerScroll").select(".scroller");
		if (tbody.empty()) {
			var cent = config.container.append("div").classed("centerScroll", true)
				.style("position", "relative");
			
			tbody = cent.append("div")
				.style("width", "calc(100% - 18px")
				.style("height", "calc(100% - 26px")
				.style("position", "absolute")
				.style("top", "0")
				.style("overflow", "hidden")
				.append("div").classed("tableBody", true);
			scroller = cent.append("div")
				.classed("scrollerContainer", true)
				.style("width", "100%")
				.style("height", "100%")
				.style("overflow", "auto")
				.append("div").classed("scroller", true);
		}
		config.container.select(".centerScroll").style("overflow", "hidden");

		// create row data, checking for invalid names. rowData will be an array
		// of row objects that have a data property
		var rowOffset = 0;
		var rowData = [];
		tableData.forEach(function(row, i) {
			rowData[rowOffset + i] = {
				data: inTableData.columns.map(function(colObj, i) {
					return {
						name: (row[i+"_"+colObj.name] === undefined || row[i+"_"+colObj.name] === null) ? '-' : row[i+"_"+colObj.name]
					};
				})
			};
		});
		
		var currentDataStart = 0;
		var currentDataEnd = rowData.length;

		// Local sorting here, but sort now happens on the server.
		// save the row index with each row, after sorting.
		rowData.forEach(function(d, i) {
			d.data.forEach(function(cell, j) {
				cell.row = rowOffset + i;
				cell.col = j;
				cell.key = j;
			});
		});

		// this provides an array of header rows
		var headerDataRows = [];
		tableData.headers.forEach(function(row, i) {
			headerDataRows[i] = {
				data: row,
				rowIndex: i // 0:forms, 1:rgs, 2:columns
			};
		});


		// TODO: NOTE: I just duplicated efforts here pretty sure i can just
		// user tableData.headers
		headerDataRows.forEach(function(h, a) {
			var previousKey = 0;
			h.data = h.data.map(function(obj, i) {
				obj.key = previousKey;
				previousKey += (obj.childCount == 0) ? 1 : obj.childCount;
				obj.origIndex = i;
				obj.row = a;
				obj.col = i;
				obj.hidden = false;
				return obj;
			})
		})

		var headerData = headerDataRows[2].data;
		that.headerData = headerData;
		// the header widths are set based on the header name length * 7 + 80
		// The + 80 accounts for the size of the hamburger menu, the lock icon, and sort icons.
		// change the scale and margin for a better fit for the column widths
		// here:
		headerData.forEach(function(d) {
			// initial column widths are set here:
			d.width = Math.min(250, (d.name.length * 7 + 80) + (d.name == "OrderableBiosampleID" ? 20 : 0));
			if ( headerDataRows[1].data[d.parentId].childCount === 1 ) {
				// if the column is the only child make sure the width accommodates the parent name:
				var parentName = headerDataRows[1].data[d.parentId].name;
				d.width = Math.max(d.width, (parentName.length * 7));
			}
		
			if (d.key === 0) {
				d.offset = 0;
			} else {
				d.offset = headerData[d.key - 1].width + headerData[d.key - 1].offset;
			}
		});
		
		var numRows = config.numRows;
		var rowHeight = 19;
		// set the height to match all rows, so scrolling works, even though
		// many are 'virtual'
		var tbodyHeight = numRows * rowHeight;
//		tbody.style("height", "" + (tbodyHeight) + "px")
		tbody.style("height", "100%")
		var maxScrollerHeight = Math.min( 1000000, tbodyHeight );
		scroller.style("height", "" + maxScrollerHeight + "px")
		

		// set the width to match all columns, so scrolling works, even though
		// many are 'virtual'
		var numCols = tableData.columns.length;
		tbody.style("width", "" + (getTableWidth()) + "px")
		scroller.style("width", "" + (getTableWidth()) + "px")

		if (resize) {
			tbody.selectAll("div").remove();
		}

		// Returns a function, that, as long as it continues to be invoked, will
		// not
		// be triggered. The function will be called after it stops being called
		// for
		// N milliseconds. If `immediate` is passed, trigger the function on the
		// leading edge, instead of the trailing.
		function debounce(func, wait, immediate) {
			var timeout;
			return function() {
				var args = arguments;
				if (timeout) return;
				var context = this;
				var later = function() {
					timeout = null;
					if (!immediate) {
						func.apply(context, args);
					}
				};
				var callNow = immediate && !timeout;
				clearTimeout(timeout);
				timeout = setTimeout(later, wait);
				if (callNow) {
					func.apply(context, args);
				}
			};
		}

		// prevent too many requests for data when scrolling rapidly.
		var debounceUpdateRowData = debounce(updateRowData, 200);

		// called on every scroll event
		function render() {
			renderTable(false, true);
		}
		
		
		var scrollX = 0; 
		var scrollY = 0; 
		var lastScrollX = -1;
		var lastScrollY = -1;
		var scrollingNode = undefined;
		var rowCount = 0;
		var offsetY = 0;
		var nonUserScroll = false;
		var lastHeight = -1;
		var preventUpdate = false;
		function renderScroll() {
			if ( scrollingNode === undefined ) {
				scrollingNode = config.container.select(".scrollerContainer").node();
				scrollX = scrollingNode.scrollLeft; 
				scrollY = scrollingNode.scrollTop; 
			} else {
				scrollX = scrollingNode.scrollLeft; 
				scrollY = scrollingNode.scrollTop; 
			}
			
			var scrollerHeight = maxScrollerHeight;
//			var scrollerHeight = scroller.node().clientHeight;
//			if(scrollerHeight === 0) {
//				var cssScrollHeight = scroller.style("height").substr(0, scroller.style("height").length-2);
//				tbodyHeight = parseFloat(cssScrollHeight).toPrecision();
//			}
//			var tbodyHeight = tbody.node().clientHeight;
//			if(tbodyHeight === 0) {
//				var cssTableHeight = tbody.style("height").substr(0, tbody.style("height").length-2);
//				tbodyHeight = parseFloat(cssTableHeight).toPrecision();
//			}
			
			 if ( scrollerHeight !== tbodyHeight ) {
	                var scrollTopMax = scrollingNode.scrollTopMax ? scrollingNode.scrollTopMax : scrollingNode.scrollHeight - scrollingNode.clientHeight;
	                var diff = scrollerHeight - scrollTopMax;
	                scrollY = (tbodyHeight - diff + 10) * (scrollY / scrollTopMax); 
	         }
			
			var isHorizontal = ( scrollX !== undefined && lastScrollX !== undefined) ? (lastScrollX !== -1) ? (scrollX !== lastScrollX ) : false : false;
			var isVertical = ( scrollY !== undefined && lastScrollY !== undefined) ? (lastScrollY !== -1) ? (scrollY !== lastScrollY ) : false : false;
			
			type = 1;
			if ( isHorizontal ) {
				tableHead.style("transform", "translate3d(" + (-scrollX) + "px,0,0)");
				tbody.style("transform", "translate3d(" + (-scrollX) + "px," + "0px,0)");
//				tbody.style("transform", "translate3d(" + (-scrollX) + "px,"+ (-scrollY) + "px,0)");
			}
			else if ( isVertical ) {
				tbody.style("transform", "translate3d(" + (-scrollX) + "px," + "0px,0)");
//				tbody.style("transform", "translate3d(" + (-scrollX) + "px,"+ (-scrollY) + "px,0)");
			}
			if ( isHorizontal || isVertical ) {
				renderTable(isHorizontal, isVertical);
				// create bulk order button
				that.addBulkOrderButton();
			}
			if ( isVertical ) {
				// debounce prevents too many calls to updateRowData when the user is jumping the scrollbar around:
				if(!preventUpdate){
					debounceUpdateRowData();
				} else {
					preventUpdate = false;
				}
//				updateRowData();
			}
			
			lastScrollX = scrollX;
			lastScrollY = scrollY;	
		}

		function redirectEvent(eventType, fromElement, toElement) {
			fromElement.addEventListener(eventType, function (event) {
				toElement.dispatchEvent(new event.constructor(event.type, event));
				event.preventDefault();
				event.stopPropagation();
			});
		}
		
		function resetTableHeight() {
			var newHeight = that.numRows * rowHeight;
			tbody.style("height",  "100%");
			//we need a minimum height of 1px so the user can scroll horizontally
			var scrollerHeight = ((that.numRows * rowHeight) == 0) ? 1 : that.numRows * rowHeight;
			var maxScrollerHeight = Math.min( 1000000, scrollerHeight );
			scroller.style("height", "" + maxScrollerHeight + "px"); 

			// Change the height forces the scroll listener to trigger as a result it re-renders the page without a filter,
			// if the filter returns zero
			// so we need to set nonUserScroll to true

			if((newHeight < lastHeight) && newHeight == 0 && scrollY != 0){
				nonUserScroll = true;
			}
			lastHeight = newHeight;
		}
		
		this.updateRepeatableGroupFunction = function renderRepeatableGroup(expandOrCollapse, e) {
			rgModel = $(e.target).data("model");
			type = expandOrCollapse;
			updateRowData();
		}

		this.sortFunction = function sortTable() {
			type = 4;
			scrollingNode.scrollTop = 0;
			offsetY = 0;
			updateRowData();
		}

		this.applyVirtFilters = function applyTableFilters() {
			type = 5;
			activeFilter = true;
			scrollingNode.scrollTop = 0;
			//prevent renderScroll
			preventUpdate = true;
			offsetY = 0;
			updateRowData();
		}
		
		this.applyVirtDisplayOption = function applyDisplayOption(displayOption) {
			type = 6;
			displayOptionModel = {"displayOption" : displayOption};
			updateRowData();
		}

		function getTableWidth() {
			return headerData[headerData.length - 1].width + headerData[headerData.length - 1].offset;
		}

		function getTableHeaderWidth(d) {
			if (d.childCount === 0)
				return "" + (headerData[d.key].width) + "px";
			var nextKey = d.key + d.childCount;
			if (nextKey >= headerData.length - 1) {
				var width = getTableWidth() - headerData[d.key].offset;
				return "" + (width) + "px";
			}
			var width = headerData[nextKey].offset - headerData[d.key].offset;
			return "" + (width) + "px";
		}

		function getTableHeaderWidth2(d) {
			if (d.childCount === 0)
				return headerData[d.key].width;
			var nextKey = d.key + d.childCount;
			if (nextKey >= headerData.length - 1) {
				var width = getTableWidth() - headerData[d.key].offset;
				return "" + (width) + "px";
			}
			return headerData[nextKey].offset - headerData[d.key].offset;
		}

		function getColumnInfo(scrollX, vizWidth) {
			var offsetX = 0;
			for (var i = headerData.length - 1; i > 0; --i) {
				if (scrollX >= headerData[i].offset) {
					offsetX = i;
					break;
				}
			}
			var columnsPerPage = headerData.length;
			for (var i = offsetX; i < headerData.length; ++i) {
				if (headerData[i].offset >= (scrollX + vizWidth)) {
					columnsPerPage = i - offsetX;
					break;
				}
			}

			return {
				offsetX: offsetX,
				columnsPerPage: columnsPerPage,
			};
		}
		this.renderVirtTable = render;

		var storedHiddenChildren = [];


		var minColWidth = 80;
		var dragStartOffset = 0;
		var dragStartWidth = 0;
		var dragStartX = 0;
		var dragBehavior = d3.drag()
        .on("start", function(d) {
        	dragStartX = d3.event.x;
       		dragStartOffset = headerData[d.key].offset + headerData[d.key].width;
        	dragStartWidth = d.width;
        })
        .on("drag", function(d) {
        	d3.select(this);
        	var dx = d3.event.x - dragStartX;
        	var dragOffset = dragStartOffset + dx;

        	if (dragStartWidth + dx > minColWidth) {
        		var changed = false;
        		if (d.key === headerData.length - 1) {
        			if (headerData[d.key].width !== dragStartWidth + dx) {
	    	        	headerData[d.key].width = dragStartWidth + dx;
	    	        	changed = true;
        			}
        		} else if (headerData[d.key + 1].offset !== dragOffset) {
        			changed = true;
				    lastScrollX = -1;
				    lastScrollY = -1;
		        	headerData[d.key + 1].offset = dragOffset;
		        	headerData[d.key].width = dragStartWidth + dx;
		        	var startKey = d.key + 1;
		        	headerData.forEach(function (next,i) {
		        		if ( i > startKey ) {
		        			next.offset = headerData[next.key - 1].width + headerData[next.key - 1].offset;
		        		}
		        	} );
        		}
        		if (!changed) return;

        		var tbody = config.container.select(".centerScroll").select(".tableBody");
	    		if (!tbody.empty()) {
					tbody.style("width", "" + (getTableWidth()) + "px");
					scroller.style("width", "" + (getTableWidth()) + "px");
	    		}
	        	renderTable(true, false);
        	}
        });
		
		function checkStoredHiddenColumns(){
			if(that.hiddenColumnsProperties.length > 0){
				
				var headerData = (that.inTableData.headers != undefined) ? that.inTableData.headers[2] : [];
				var parentHeader = (that.inTableData.headers != undefined) ? that.inTableData.headers[1] : [];
				var gParentHeader = (that.inTableData.headers != undefined) ? that.inTableData.headers[0] : [];
				
				var columnsNotShown = [];
				that.hiddenColumnsProperties.forEach(function(columnProperties, i) {
					var formName = columnProperties.formName;
					if ( formName === undefined ) {
						formName = columnProperties.formUri.slice(columnProperties.formUri.lastIndexOf("/") + 1);
					}
					var columnHeader = headerData.filter(function (d) {
						// make sure the name and parent name match:
						return ((d.name === columnProperties.deName) && (parentHeader[d.parentId].name === columnProperties.rgName) 
								&& (gParentHeader[d.grandParentIndex].name === formName) )
					});
					
					if ( columnHeader.length !== 0 ) {
						var col = columnHeader[0].col;
						that.hiddenColumns.push(col);
					} else {
						columnsNotShown.push(columnProperties);
					}
				});
				
				if(columnsNotShown.length > 0) {
					that.hiddenColumnsProperties = columnsNotShown;
				} else {
					// remove from hiddenColumnsProperties 
					that.hiddenColumnsProperties = [];
				}
				
			}
		}
		
		// only calculate the new rows on vertical scroll:
		var dataSliceRows;
		function renderTable( isHorizontal, isVertical ) {

			if ((lastScrollX === scrollX) && (lastScrollY === scrollY) && !that.updateFrozen && !that.hideColumn && type === 1) return;
			
			if( nonUserScroll ) {
				nonUserScroll = false;
				return;
			}
			// hide any hidden columns
			if (that.hiddenColumns.length > 0) {
				// re-calculate table body width
				tbody.style("width", "" + (getTableWidth()) + "px");
				scroller.style("width", "" + (getTableWidth()) + "px");
			}
			that.hideColumn = false; // reset hide column flag
			
			
			// calculate visible width and the number of columns to display:
			var vizWidth = scrollingNode.getBoundingClientRect().width || that.defaultTableWidth;
			
			var tbodyHeight = tbody.node().getBoundingClientRect().height;
			if(tbodyHeight <= 0) {
				var cssTableHeight = tbody.style("height").substr(0, tbody.style("height").length-2);
				tbodyHeight = parseFloat(cssTableHeight).toPrecision();
			}
			
			var currentHeight = tbodyHeight || that.defaultTableHeight;
			if(lastHeight == -1){ lastHeight = currentHeight};

			var frozenCount = (that.frozenIndex !== -1) ? (that.frozenIndex - that.frozenOffsetX + 1) : 0;
			// check and respositiong / lock the scroll bar:
			if (that.frozenIndex !== -1) {
				scrollX = Math.max(scrollX, that.frozenScrollPositionX);
				scrollingNode.scrollLeft = scrollX;
			}
			// repositioning has to happen after scrollLeft is reset in case of frozeen columns:
			if ( type === 1 ) {
					tableHead.style("transform", "translate3d(" + (-scrollX) + "px,0,0)");
					tbody.style("transform", "translate3d(" + (-scrollX) + "px," + "0px,0)");
//					tbody.style("transform", "translate3d(" + (-scrollX) + "px,"+ (-scrollY) + "px,0)");
			}
			
			var colInfo = getColumnInfo(scrollX, vizWidth);
			var offsetX = colInfo.offsetX; 
			var columnsPerPage = colInfo.columnsPerPage;
			var colCount = columnsPerPage + 1;
			
			if ( that.frozenIndex !== -1 ) {
				offsetX = Math.max(offsetX, that.frozenOffsetX + frozenCount) ;
			}
			that.scrollOffsetX = offsetX;
			that.scrollPositionX = scrollX;

			// calculate the visible height and the number of rows to display:
			var vizHeight = scrollingNode.getBoundingClientRect().height || that.defaultTableHeight;
			var rowsPerPage = Math.ceil(vizHeight / rowHeight);
			// add some buffer beyond what is visible on the page. Scrolling
			// down is most common.
			rowCount = rowsPerPage + 15;
			offsetY = Math.max(0, Math.floor(scrollY / rowHeight) - 1);
			that.model.set("offsetY", offsetY);
			
			if ( isVertical ) {
				dataSliceRows = rowData.slice(offsetY, offsetY + rowCount);
			}
			if ( isVertical || isHorizontal ) {

				// Reslice Header:
				var headerSliceRows = headerDataRows;
				headerSliceRows.reverse();

				//check for columns, hidden prior to table rendering
				checkStoredHiddenColumns();
				
				// hide any hidden columns
				if (that.hiddenColumns.length > 0) {
					var newColCount = colCount;
					var hiddenChildren = [];
					that.hiddenColumns.forEach(function (index, i) {
						var removeIndex = headerSliceRows[0].data.findIndex(function (a) {
							return (index == a.col && a.row == 2);
						})
						if (removeIndex > -1) {
							var removedCol = headerSliceRows[0].data.slice(removeIndex, removeIndex + 1)[0];
							removedCol.hidden = true;
							removedCol.width = 0;
							++newColCount;
							
							headerSliceRows[0].data.forEach(function (col, i) {
								if (col.key === 0) {
									col.offset = 0;
								} else {
									col.offset = headerData[col.key - 1].width + headerData[col.key - 1].offset;
								}

							});
							hiddenChildren.push(removedCol);

						}
					});
					colCount = newColCount;
				}

				
				var headerSlice = [];
				headerSliceRows.forEach(function(row, i) {
					var headerSliceCols = [];
					if (row.rowIndex == 2) {
						// clip each row in X (columns) and reassemble into 2D
						// array:
						if ( that.frozenIndex !== -1 ) {

							var dataSliceCols = row.data.slice(that.frozenOffsetX, that.frozenOffsetX + frozenCount);

							dataSliceCols.forEach(function(cell, j) {
								if ( j === 0 ) {
									cell.frozen = that.frozenScrollOffsetX;
								}
								else {
									cell.frozen = headerSliceCols[j-1].frozen + getTableHeaderWidth2(headerSliceCols[j-1]);
								}
								headerSliceCols.push(cell);
							});

							dataSliceCols = row.data.slice(offsetX, offsetX + colCount );
							dataSliceCols.forEach(function(cell) {
								cell.frozen = undefined;
								headerSliceCols.push(cell);
							});

						}
						else {
							headerSliceCols = row.data.slice(offsetX, offsetX + colCount);
							headerSliceCols.forEach(function(cell) {
								cell.frozen = undefined;
							});
						}
					} else {
						var rIndex = row.rowIndex;
						row.data.forEach(function(obj, i) {
							// check if child exists in headerSlice
							var child = undefined;
							obj.frozen = undefined;
							if (rIndex == 1) {
								child = headerSlice.find(function(o) {

									return (o.parentIndex === obj.selfIndex && o.grandParentIndex === obj.parentIndex)
								});
							} else if (rIndex == 0) {
								child = headerSlice.find(function(o) {
									return (o.parentIndex === obj.selfIndex && o.grandParentIndex === -1)
								});
							}

							if (child != undefined ) {
								if ( child.frozen  != undefined ) {
									if ( obj.frozen === undefined ) {
										obj.frozen = child.frozen;
									}
									else {
										obj.frozen = Math.min(obj.frozen, child.frozen);
									}
								}
								headerSliceCols.push(obj);
							}

						});
						headerSliceCols.reverse();
					}
					headerSliceCols.forEach(function(column) {
						(row.rowIndex == 2) ? headerSlice.push(column): headerSlice.unshift(column);
					});
				});

				headerSliceRows.reverse(); // TODO: find a better way to manage
											// this
				
				// D3 Code for rendering the Header:
				// request a div for each header cell in headerSlice
				// pass in a key function so each cell is uniquely identified
				// this makes sure that when cells scroll they aren't recreated as
				// long as the content of the cells
				// hasn't changed. Cells should only be remove/recreated when the
				// content scrolls out of view.
				var headcells = tableHead.selectAll("div.headerCell").data(headerSlice, function(d) { return "" + (d.col) + "_" + (d.row); });

				var headCellsExitNodes = headcells.exit();
				// uses exitNodes to find a div that can be reused - avoids creating
				// too many divs which is very very slow.
				var headCellsEnter = headcells.enter().append(function() {
						var reusableNode = 0;
						for (var i = 0; i < headCellsExitNodes._groups[0].length; i++) {
							reusableNode = headCellsExitNodes._groups[0][i];
							if (reusableNode) {
								headCellsExitNodes._groups[0][i] = undefined;
								d3.select(reusableNode).selectAll("*").remove();
								return reusableNode;
							}
						}
						return document.createElement("div");
					})
					.classed("headerCell deHeader", true)
					.style("display", "flex")
					.style("height", "20px");
				headCellsEnter.append("div")
					.html(function(d) { return that.renderColumn(d); })
					.style("text-align", "center")
					.style("flex", "1")
					.on("mouseover", config.hoverTitle);
				headCellsEnter.filter(function(d) { return d.row === 2; })
			 	  .append("div")
					.classed("resizeHandle", true)
					.style("width", "7px")
					.style("height", "20px")
					.style("flex", "0 0 7px")
					.style("cursor", "ew-resize")
					.style("background-color", "#fff")
					.call(dragBehavior);

				headcells.exit().remove();

				// new and existing header cells
				var headCellsUpdate = headCellsEnter.merge(headcells)
					.classed("frozen", function(d) { return d.frozen !== undefined; })
					// put them in the right place
					.style("transform", function(d, i) {
						if ( d.frozen !== undefined ) return "translate3d(" + ( d.frozen + scrollX) + "px,"
						 + (d.row * 20) + "px,0)";
						return "translate3d(" + ( headerData[d.key].offset) + "px,"
						+ (d.row * 20) + "px,0)";
					});
				headCellsUpdate
					.style("width", function(d, i) {
						return getTableHeaderWidth(d, i);
					})
					.attr("title", function(d) { return d.name; });
				// retrieve the current header theme color from background - passed to knowledge graph
				headerColor = headCellsUpdate.select(".hdrcolor").style("background-color");
			}
			
			// reslice table body after header:
			var dataSlice = [];
			var count = 0;

			dataSliceRows.forEach(function(row, i) {
				// clip each row in X (columns) and reassemble into 2D array:

				if ( (that.frozenIndex !== -1) && (that.frozenOffsetX !== offsetX) ) {
					var dataSliceCols = row.data.slice(that.frozenOffsetX, that.frozenOffsetX + frozenCount);

					dataSliceCols.forEach(function(cell, j) {
						if ( j === 0 ) {
							cell.frozen = that.frozenScrollOffsetX;
						}
						else {
							cell.frozen = dataSlice[count-1].frozen + headerData[dataSlice[count-1].col].width;
						}
						dataSlice.push(cell);
						count++;
					});

					dataSliceCols = row.data.slice(offsetX, offsetX + colCount);
					dataSliceCols.forEach(function(cell) {
						cell.frozen = undefined;
						dataSlice.push(cell);
						count++;
					});

				}
				else {
					var dataSliceCols = row.data.slice(offsetX, offsetX + colCount);
					dataSliceCols.forEach(function(cell, j) {
						cell.frozen = undefined;
						dataSlice.push(cell);
						count++;
					});
				}
			});

			// D3 Code for rendering the table Body:
			// pass in a key function so each cell is uniquely identified
			// this makes sure that when cells scroll they aren't recreated as
			// long as the content of the cells
			// hasn't changed. Cells should only be remove/recreated when the
			// content scrolls out of view and new content replaces
			// the old.
			var rowCells = tbody.selectAll("div.tableCell").data(dataSlice, function(d) {
				return  "" + (d.col) + "_" + (d.row);
			});

			// here we re-use exit nodes when they aren't visible.
			var exitNodes = rowCells.exit();
			var cellsEnter = rowCells.enter().append(function() {
				var reusableNode = 0;
				for (var i = 0; i < exitNodes._groups[0].length; i++) {
					reusableNode = exitNodes._groups[0][i];
					if (reusableNode) {
						exitNodes._groups[0][i] = undefined;
						return reusableNode;
					}
				}
				return document.createElement("div");
			})
			.classed("tableCell", true)
			.classed("highlightRow", false)
			.classed("even", function (d) {
				return (d.row % 2 == 0);
			})
			.style("height", "" + (rowHeight) + "px")
			.html(function(d) {

				return that.renderCell(d)
			})
			.classed("disabled", function(d) {  return d.disabled } )
			.classed("highlight-row", function(d) {  return d.highlightRow } )
			.style("text-align", "center")
			.on("mouseover", config.hoverRow)
			.on("mouseleave", config.hoverTitle);
			rowCells.exit().remove();

			var cellsUpdate = cellsEnter.merge(rowCells)
			.html(function(d) {
				return that.renderCell(d)
			})
			.classed("disabled", function(d) { return d.disabled } )
			.classed("frozen", function(d) { return d.frozen !== undefined; })
			.style("width", function (d) {
				return "" + (headerData[d.col].width) + "px"
			})
			.attr("title", function(d){ return d.title })
			.classed("highlight-row", function(d) {  return d.highlightRow } )
			// move only cells that aren't frozen
			// put them in the right place.
			// Variable column width requires a list of each column width
			// and offset.
			.style("transform", function (d) {
				if ( d.frozen !== undefined ) {
					return "translate3d(" + (d.frozen + scrollX) + "px," + ((d.row * rowHeight) - scrollY) + "px,0)";
				}
				return "translate3d(" + (headerData[d.col].offset) + "px," + ((d.row * rowHeight) - scrollY) + "px,0)";
			});					
			
		}

		
		// configure prefetch of data.
		var bufferRows = 30;  // extra rows above/below the rendered rows.
		var fetchCount = 0; // how many fetch requests are in flight.
		var previousOffsetY = -1;
		var prefetchSize = 500;
		var startIndex = 0;
		var fetchCallParam = -1;
		function updateRowData() {
			var direction = 1;
			if ( (previousOffsetY !== -1) && (previousOffsetY !== offsetY) ) {
				direction = offsetY - previousOffsetY;
			}
			previousOffsetY = offsetY;
			
			// rowCount is the number of rendered rows.
			var displayedEnd = offsetY + rowCount;
			var lowerBound = Math.max(0, offsetY - bufferRows);
			var upperBound = Math.min( numRows, displayedEnd + bufferRows );
			
			// Type: 1. rows change because of scroll, i.e. pagination 2. and 3. rows changed
			// because repeatable group expansion, or collapse, 4. sort, 5.
			// filters, 6. Display Options
			if (type == 2 || type == 3 || type == 4 || type == 5 || type == 6) {
				// do nothing
			}
			else if ( !( (lowerBound < currentDataStart) || (upperBound > currentDataEnd) ) ) {
				EventBus.trigger("close:processing");
				return;
			}
			else {
				startIndex = currentDataEnd;
				
				if ( (direction > 0) && (upperBound > currentDataEnd) ) {
					// fetch the next set of rows:
					startIndex = currentDataEnd;
					// jump forward:
					startIndex = Math.max(startIndex, offsetY);
					var prefetchStartIndex = Math.max(0, numRows - prefetchSize);
					// bounds check:
					startIndex = Math.min( prefetchStartIndex, startIndex);
				}
				if ( (direction < 0 ) && (lowerBound < currentDataStart) ) {
					// fetch the previous set of rows:
					startIndex = Math.max(0, currentDataStart - prefetchSize);
					// jump backward:
					startIndex = Math.min(startIndex, offsetY);
				}
			}
			if ( type === 1 ) {
				if ( startIndex === fetchCallParam ) return;
				fetchCallParam = startIndex;
			}
			if ( type === 4 || (type === 5 || activeFilter) ) {
				// scrollTop was reset so reset startIndex:
				startIndex = 0;
			}
			
			// need to fetch
			fetchCount += 1;
			//EventBus.trigger("open:processing", "Retrieving data...");
			$(".containerFontIcon").show();
			var valueObj = (type == 6) ? displayOptionModel : rgModel;
			config.getRows(startIndex, prefetchSize, tableData.columns, type, valueObj)
				.then(function (response) {
					if(type == 2 || type == 3) {
						// expand / collapses repeatable group
						rowData.length = 0;
					}
					fetchCount -= 1;
					fetchCallParam = -1;
					if (!response) return;
					if ( (type === 1) && (startIndex !== response.offsetY) ) return;
					var responseOffsetY = response.offsetY;
					var rowCount = response.rowCount;
					var data = response.data;
					var modelData = response.modelData;
					that.numRows = response.modelData.iTotalDisplayRecords;
					numRows = that.numRows;

					config.dtv.model.set("offsetY", responseOffsetY);
					if(data.length > 0) { 
						if(type == 5 || activeFilter) {
							//if the data has been filtered, let's clear out all the old data
							//and start from new
							rowData = [];
							currentDataEnd = 0;
							tbodyHeight = that.numRows * rowHeight;
							maxScrollerHeight = Math.min( 1000000, tbodyHeight );
							scroller.style("height", "" + maxScrollerHeight + "px");
							scrollY = scrollX = 0;
							
						}
						data.forEach(function(row, i) {
							// add new data row
							rowData[responseOffsetY + i] = {
								// guid: row.GUID || row.ID,
								data: tableData.columns.map(function(col, j) {
									return {
										name: (row[j+"_"+col.name] === undefined || row[j+"_"+col.name] === null) ? '-' : row[j+"_"+col.name],
										row: responseOffsetY + i,
										col: j
									};
								}),
							};
						});
					} else {
						rowData = [];	
					}
					
					// when scrolling (type == 1) clear data:
					if ( (type == 1) && responseOffsetY > currentDataEnd ) {
						// jumped 
						for (var i = currentDataStart; i < currentDataEnd; ++i) {
							rowData[i] = {
								data: []
							};
						}
						currentDataStart = responseOffsetY;
						currentDataEnd = responseOffsetY + data.length;
					}
					else if ( (type == 1) && (responseOffsetY + data.length) < currentDataStart ) {
						// jumped:
						for (var i = currentDataStart; i < currentDataEnd; ++i) {
							rowData[i] = {
								data: []
							};
						}

						currentDataStart = responseOffsetY;
						currentDataEnd = responseOffsetY + data.length;
					}
					else {
						currentDataStart = Math.min( currentDataStart, responseOffsetY );
						currentDataEnd = Math.max( currentDataEnd, responseOffsetY + data.length );
					}
					
					// delete stale data - only if all fetches are complete.
					if (fetchCount === 0) {						
						if ( direction > 0 ) {
							var newCDS = Math.max(currentDataStart, lowerBound - prefetchSize);
							for (var i = currentDataStart; i < newCDS; ++i) {
								rowData[i] = {
									data: []
								};
							}
							currentDataStart = newCDS;
						}
						else if ( direction < 0 ) {
							var newCDE = Math.min( upperBound + prefetchSize, currentDataEnd );
							for (var i = currentDataEnd; i > newCDE; --i) {
								rowData[i] = {
									data: []
								};
							}
							currentDataEnd = newCDE;
						}
					}
					// reset the last scroll positions to ensure the table
					// updates:
					if ( type === 5 || activeFilter ) {
						resetTableHeight();
					}
					// reset type to 1
					type = 1;
					//reset active Filter
					activeFilter = false;
					lastScrollX = lastScrollY = -1;
					renderTable(false, true);
					//EventBus.trigger("close:processing");
					$(".containerFontIcon").hide();

				})
				.catch(function (error) {
					fetchCount -= 1;
					console.log(error);
					//EventBus.trigger("close:processing");
					$(".containerFontIcon").hide();
				});
		}

		var scrollCont = config.container.select(".scrollerContainer");
		scrollingNode = scrollCont.node();
		render();

		// link scrolling callback:
		scrollCont.on("scroll", renderScroll);
		
		tbody.on("wheel", function () {
			scrollingNode.scrollTop += d3.event.wheelDeltaY || d3.event.deltaY || 0;
			d3.event.preventDefault(); 
		});
		
	},
	renderColumn: function (column) {

		newRowNum = column.row;
		/*
		 * if(column.row >= 100) { var rowLength = this.model.rows.length;
		 * newRowNum = (cell.row - (Math.floor(cell.row/100)*100)); }
		 */
		// var cellModel =
		// this.model.rows.at(newRowNum).get("cells").at(cell.col);
		var colModel;
		switch (column.row) {
			case 0:
				colModel = this.model.formColumns.at(column.col);
				break;
			case 1:
				colModel = this.model.rgColumns.at(column.col);
				break;
			case 2:
				colModel = this.model.columns.at(column.col);
				break;
			default:
				colModel = new QTDT.Col();
				break;
		}

		colModel.set('row', newRowNum);
		colModel.set('col', column.col);

		var hType = colModel.get("headerType");
	     var h5Class = (hType == "dataElement") ? "colhdr hdrcolor" : "colhdr ";
	     var modelName = colModel.get("name");
	     var h5Html = (hType == "dataElement") ? "<div class='colValue'>" + modelName + "</div>" : modelName; // ;
	     var checkedAllStatus = (this.model.get('selectAllChecked')) ? 'checked="checked"' : '';

	     if(modelName == "OrderableBiosampleID"){
	    	 this.model.set('bioFormName',colModel.formNameVersion || "");
	     }




	     h5Html += (modelName == "OrderableBiosampleID") ? '<div class="colValue"><input id="sampleCheckAll" type="checkbox" '+checkedAllStatus+' value="0" />&nbsp;Select All</div>' : "";
	     HBHtml = "";
	     (hType == "repeatableGroup" && (colModel.get("name") != "Repeatable Groups:" && colModel.get("name") != "")) ? HBHtml = '<span class="rghbmenu hbblack"></span>': (hType == "dataElement" && !(colModel.get("name") == "GUID" && colModel.get("index") == 1)) ? (hType == "dataElement" && colModel.get("name") != "Study ID" && colModel.get("name") != "Row No.") ? HBHtml = '<span class="hbmenu"></span>' : "" : "";
	     // this is for the repeatable group label column, such that when you
			// freeze the columns the table isn't misaligned
	     HBHtml += (hType == "repeatableGroup" && colModel.get("name") == "Repeatable Groups:") ? '<span style="height: 12px;display: inline-block;"></span>' : "";


	     h5Html = HBHtml + h5Html;
	     var frozenClass = (colModel.get("frozen")) ? "fa fa-lock unlock" : "fa fa-unlock lock";

	     var cellObj = {
					colId : colModel.get("col") || 0
		            };
			var cellObjJson = JSON.stringify(cellObj);

			var sOrder = colModel.get("sortDir");
			var sorting = "sorting";

			if(sOrder == "asc") {
				sorting = "sort_asc";
			} else if(sOrder == "desc") {
				sorting = "sort_desc";
			}

		 h5Html += (hType == "dataElement") ? ' <a href="javascript:void(0);" class="icon frozenIcon ' + frozenClass + '" data-model=\''+cellObjJson+'\'>&nbsp</a>' : "";
	     h5Html += (hType == "dataElement" && !colModel.get("doesRepeat") && colModel.get("name") != "Row No.") ? '<div class="'+sorting+'" data-model=\''+cellObjJson+'\'></div>' : "";
	     htmlOutput = $('<div>');
	     htmlOutput.append($('<h5>', { "class": h5Class, id: (modelName == "OrderableBiosampleID") ? "OrderableBiosampleID" : "" }).html(h5Html));


	     var cellObj = {
					row : colModel.get("row") || 0,
					col : colModel.get("col") || 0
		            };
			var cellObjJson = JSON.stringify(cellObj);
			hamburgerHtml = $('<div>');
			hamburgerHtml.append($('<div class="tableHamburger"><div></div><div></div><div></div></div>'));


			hamburgerHtml.children('.tableHamburger').attr( "data-model", cellObjJson );

	     var hType = colModel.get("headerType");
        if (hType == "dataElement") {
        	htmlOutput.children('h5').children('.hbmenu').append(hamburgerHtml.prop('outerHTML'));
        } else if (hType == "repeatableGroup") {
        	htmlOutput.children('h5').children('.rghbmenu').append(hamburgerHtml.prop('outerHTML'));
        }


        htmlOutput.children('h5').append('<div class="space-line"></div>');

	     // if the adjusted width was previously set use set the width of the
			// column to that.
	    /*
		 * var adjustedWidth = this.model.get("adjustedWidth"); if
		 * (adjustedWidth != -1) { this.$el.width(adjustedWidth);
		 * this.$el.children('h5').width(adjustedWidth); this.model.set("width",
		 * adjustedWidth); }
		 */

	    return htmlOutput.html();

	},
	renderCell: function (cell) {
		cell.title = "";
		return this.parseCellHtml(cell);
	},
	// asynchronous request for more row data. Returns a Promise.
	// Type: 1. rows change because of scroll, 2. rows changed because
	// repeatable group expansion, or collapse
	getRows: function (offsetY, numRows, columns, type, valueObj) {
		if (!columns.length) return Promise.resolve();
		return new Promise(function(resolve, reject) {
			QueryTool.query.updateVirtTable(offsetY, numRows, type, valueObj).then(function(data) {
				// data.aaData is array of arrays of row data.
				var dataSlice = {};
				if ( data.aaData !== undefined ) {
					dataSlice = data.aaData.map(function(row,i) {
						
						var rowCount = offsetY+i+1;
						row.unshift(rowCount.toString());
						// d3 expects an object with key/value pairs for each row.
						output = {};
						row.forEach(function(val, i) {
							output[i+"_"+columns[i].name] = val;
						});
						return output;
					});
				}
				
				// list of column names added as additional property, mirroring
				// d3.csvParse()
				dataSlice.columns = columns;
				var ret = {
					offsetY: offsetY,
					rowCount: numRows,
					data: dataSlice,
					modelData: data
				};
				resolve(ret);
			}).catch(function(err) { reject(err); EventBus.trigger("close:processing"); });
		});
	},
	

	createTable: function () {
		// updating options in the schema drop down
		EventBus.trigger("updateSchemaOptions");
		var container = d3.select("#innerdiv");
		var resize = true;
		this.makeVirtTable(this.inTableData, {
			numRows: this.numRows,
			hoverRow: function() {}, // supply callback that is activated when mouse
								// hovers over a row
			hoverTitle: function() {}, // supply a callback that is activated when
									// the mouse moves off a row
			setClickNode: function() {}, // supply a calback for when the user clicks
									// a row
			container: container,
			getRows: this.getRows,
			dtv: this
		}, resize);



	},
//	createFrozenColumns: function () {
//
//		// /5. create frozen headers if needed
//		if (this.model.frozenColumnsExist) {
//
//			// /1. Create Table div Container
//			$("#innerdiv").before($('<div>', {
//				id: 'frozenInnerDiv',
//				class: 'base-container freeze'
//			}));
//
//			// /4.Create Header Container
//			$("#frozenInnerDiv").append($('<table>', {
//				id: "frozenTableHeaders",
//				cellspacing: 0,
//				cellpadding: 0
//			}));
//
//			// create frozen form headers
//			$("#frozenTableHeaders").append($('<tr>', {
//				id: "frozenFormHeader",
//				class: 'table-row'
//			}))
//
//			this.model.frozenFormColumns.each(function (header) {
//				var formCol = new QTDT.ColView({
//					model: header
//				})
//				formCol.frozen = true;
//				$("#frozenFormHeader").append(formCol.render().$el);
//				header.set("frozenParentWidth", formCol.$el.width());
//				header.set("frozenView", formCol);
//
//			});
//
//			// create repeatable group headers
//			$("#frozenTableHeaders").append($('<tr>', {
//				id: "frozenRgHeader",
//				class: 'table-row'
//			}))
//
//			this.model.frozenRgColumns.each(function (header) {
//				var rgCol = new QTDT.ColView({
//					model: header
//				});
//				rgCol.frozen = true;
//				$("#frozenRgHeader").append(rgCol.render().$el);
//
//				header.set("frozenParentWidth", rgCol.$el.width());
//				header.set("frozenView", rgCol);
//			});
//
//			// create de columns
//			$("#frozenTableHeaders").append($('<tr>', {
//				id: "frozenDeHeader",
//				class: 'table-row'
//			}))
//
//			this.model.frozenColumns.each(function (header) {
//				var deCol = new QTDT.ColView({
//					model: header
//				});
//				$("#frozenDeHeader").append(deCol.render().$el);
//
//				calculatedColWidth = deCol.model.get("width");
//				deCol.model.set("frozenWidth", -1);
//				deCol.model.set("frozenWidth", calculatedColWidth);
//				header.set("frozenView", deCol);
//
//			});
//
//			// /4.Create Data Body
//			this.frozenResultsView = new QTDT.FrozenResultsView({
//				model: this.model
//			})
//			$("#frozenInnerDiv").append(this.frozenResultsView.render().$el);
//			this.frozenResultsView.resizeMainContainer();
//
//		} // end create frozen
//	},
//	/** this is used to refresh rowdata and keep headers intact */
//	renderFrozenResults: function () {
//		// If there is data let's build the table
//		this.model.loadFrozenColumns(this.selectedFrozenColumn.model);
//		if (this.model.get("data") !== undefined && !_.isEmpty(this.model.get("data"))) {
//
//			// /4.Create Data Body //new REsults View
//			this.frozenResultsView.destroy();
//			this.frozenResultsView = new QTDT.FrozenResultsView({
//				model: this.model
//			})
//			this.frozenResultsView.render();
//			this.frozenResultsView.resizeMainContainer();
//
//
//		}
//
//	},
//	adjustFrozenColumns: function () {
//		if (this.model.frozenColumnsExist) {
//			var frozenHeaders = this.model.frozenColumns;
//
//			frozenHeaders.each(function (header) {
//				/**
//				 * * This may be an only child so we may have to adjust the
//				 * widths *
//				 */
//				var parentWidth = 0;
//				var grandParentWidth = 0;
//				var parent = header.get("parent");
//				var grandParent = parent.get("parent");
//				var siblingAmount = parent.get("children").where({
//					visible: true,
//					frozen: true
//				}).length - 1;
//				var singleParentAmount = grandParent.get("children").where({
//					visible: true,
//					frozen: true
//				}).length - 1;
//				var maxWidth = header.get("frozenWidth");
//				if (siblingAmount == 0 && singleParentAmount == 0) {
//					parentWidth = parent.get("frozenParentWidth");
//					grandParentWidth = grandParent.get("frozenParentWidth");
//
//					var largest = grandParentWidth;
//					if (parentWidth > grandParentWidth) {
//						largest = parentWidth
//					}
//
//					if (largest > maxWidth) {
//						maxWidth = largest;
//					}
//					header.set("width", -1);
//					header.set("width", maxWidth);
//					header.set("frozenWidth", -1);
//					header.set("frozenWidth", maxWidth);
//					var cell = header.get("cells").at(0);
//					cell.set("width", -1);
//					cell.set("width", maxWidth);
//				}
//			});
//
//			// frozenWidth
//
//			frozenHeadersArray = frozenHeaders.where({
//				visible: true
//			});
//			var frozenTotalWidth = 0;
//			for (var i in frozenHeadersArray) {
//				frozenTotalWidth += frozenHeadersArray[i].get("width");
//			}
//			EventBus.trigger("DataTableView:setFrozenTableWidth", frozenTotalWidth);
//			// normal Width
//			normalHeadersArray = this.model.columns.where({
//				visible: true
//			});
//			var normalTotalWidth = 0;
//			for (var i in normalHeadersArray) {
//				normalTotalWidth += normalHeadersArray[i].get("width");
//			}
//			EventBus.trigger("DataTableView:setTableWidth", normalTotalWidth);
//		}
//		/** **** */
//
//	},
//	/** this method is used to lock and unlock columns * */
//	freezeColumns: function (selectCol) {
//		this.destroyFrozenTable();
//		this.selectedFrozenColumn = selectCol;
//		this.model.loadFrozenColumns(selectCol.model);
//		this.createFrozenColumns();
//
//	},
	addBulkOrderButton: function () {
		if ($("[id^='OrderableBiosampleID']").length) {
			 	bioSampleArray = this.model.get("biosampleArray");
			 	disabledClass = "disabled";
		        if (bioSampleArray.length > 0) {
		        	disabledClass = "";
		        }
			orderButton = '<a id="bulkOrderButton"  class="buttonWithIcon addSelectedSamples '+disabledClass+'" style="position: relative;margin-bottom: 20px;width: 200px;right: 0px; justify-content: stretch;" ><span class="icon pe-is-ec-basket-up"></span><span class="ui-button-text ui-c">Add Selected Samples to Cart</span></a><div class="space-line"></div>';
			if (!$('#bulkOrderButton').length) {
				$("#innerdiv").append(orderButton);
			}
		}
	},
	addSelectedSamples: function() {
        bioSampleArray = this.model.get("biosampleArray");

        if (bioSampleArray.length > 0 || this.model.get("selectAllChecked")) {

            EventBus.trigger("addBiosamples", this.model);
            //addBulkSampleCommand([{name: 'rowUris', value: bioSampleArray.join()}]);

        } else {
            //TODO: display message saying not selected samples
            $.ibisMessaging("dialog", "error", "You have not selected any samples to send to your cart.");
            //ajaxModule.showOMButtons();
            return false;
        }
    },
	reloadRepeatableGroup: function () {
		var rowLength = this.model.rows.length;
		for (var i = 0; i < rowLength; i++) {
			datarow = this.model.rows.at(i);
			var dataRowView = new QTDT.RowView({
				model: datarow
			});
			var rowcells = datarow.get("cells");

			for (var j = 0; j < rowcells.length; j++) {
				var cellmodel = rowcells.at(j);
				var cellText = cellmodel.html();
				if (cellText.indexOf("collapse") >= 0) {
					EventBus.trigger("cell:refreshRepeatableGroupInCell", this);

				}
			};
		}
	},
	resetHiddenColumns: function () {
		this.model.columns.each(function (header) {

			if (!header.get("visible")) {
				EventBus.trigger("column:removeVisibleListener", this);
				header.set("visible", true);
				EventBus.trigger("column:addVisibleListener", this);
				header.setCurrentShowHide();
			}

		});

	},
	setTableWidth: function (width) {
		this.model.set("originalTableHeaderWidth", width);
		$("#tableHeaders").width(width);
	},
//	setFrozenTableWidth: function (width) {
//		this.model.set("originalFrozenTableHeaderWidth", width);
//		$("#frozenTableHeaders").width(width);
//	},
	changeTableWidth: function (deltaObj) {
		var width = this.model.get("originalTableHeaderWidth");

		var cols = this.model.columns;

		var totalWidth = 0;
		cols.each(function (col) {
			totalWidth += (col.get("visible")) ? col.get("width") : 0;
		})

		$("#tableHeaders").width(totalWidth);

		/*
		 * if(deltaObj.visible) { $("#tableHeaders").width(deltaObj.widthDelta); }
		 * else { width -= deltaObj.widthDelta; $("#tableHeaders").width(width); }
		 */
	},
//	changeFrozenTableWidth: function (deltaObj) {
//
//		if (this.model.frozenColumnsExist) {
//			var width = this.model.get("originalFrozenTableHeaderWidth");
//
//			var cols = this.model.frozenColumns;
//
//			var totalWidth = 0;
//			cols.each(function (col) {
//				totalWidth += (col.get("visible")) ? col.get("width") : 0;
//			})
//
//			$("#frozenTableHeaders").width(totalWidth);
//			/*
//			 * if(deltaObj.visible) {
//			 * $("#frozenTableHeaders").width(deltaObj.widthDelta); } else {
//			 * width -= deltaObj.widthDelta;
//			 * $("#frozenTableHeaders").width(width); }
//			 */
//		}
//	},

	updateJoinDescription: function () {
		var output = "";
		var formShortNames = [];
		var formTitles = [];
		var data = QueryTool.query.get("formUris");
		if (data.length == 0) {
			output = "";
		} else {
			for (var i = 0; i < data.length; i++) {
				if (i > 0) {
					output += " joined with ";
				}
				var formJson = data[i];
				var form = QueryTool.page.get("forms").get(formJson);
				formShortNames.push(form.get("shortName"));
				formTitles.push(form.get("title"));
				output += "<b>" + form.get("title") + "</b>";
			}
		}
		var elem = $(".formJoinDescription");
		elem.html(output);
		
		//update table notification message

		var found = false;
		for (var i = 0; i < formTitles.length; i++) {
		    if ( Config.mdsUpdrsOptions.formNames.indexOf(formShortNames[i]) > -1) {
		        found = true;
		        break;
		    }
		}
		var msgElem = $("#tableNotificationMessage");
		if(found) {
			msgElem.html(Config.mdsUpdrsOptions.message);
			msgElem.show();
		} else {
			msgElem.html("");
			msgElem.hide();
		}
		
		elemHeight = elem.outerHeight(true);
		msgElemHeight = msgElem.outerHeight(true);;
		var height = elemHeight + msgElemHeight;
		
		$("#innerdiv").css("height", "calc(100% - " + height + "px)");
	},

	destroyTable: function () {
		this.renderedOnce = false;
		this.hiddenColumns = [];
		this.hiddenColumnsProperties = [];
		// clear results from table
		QueryTool.query.set("tableResults", {});
		this.model.set("data", []);
		// clear form cols
		this.model.formColumns.reset(null);
		// clear rg cols
		this.model.rgColumns.reset(null);
		// clear de cols
		this.model.columns.reset(null);
		// clear rows
		this.model.rows.reset(null);

		// remove add samples button
		this.$("#bulkOrderButton").remove();
		
		//empty biosample arrays
		this.model.set("biosampleArray",[]);
		this.model.set("unselectedBioSampleArray",[]);
		
		// also empty collections for the frozen table
		//this.destroyFrozenTable();
		this.$el.find("#tableContainer").empty();
		
		//remove title 
		var elem = $(".formJoinDescription");
		elem.html("");
		//remove message
		var msgElem = $("#tableNotificationMessage");
		msgElem.html("");
		msgElem.hide();
		
		
		EventBus.trigger("openSelectCriteriaViewTab");
		

	},
//	destroyFrozenTable: function () {
//		// if we are creating a new table we should destroy all existing views
//		// that are related to the table
//		EventBus.trigger("frozencolview:removeall", this);
//		EventBus.trigger("frozenrowview:removeall", this);
//		// EventBus.trigger("frozencellview:removeall",this);
//		EventBus.trigger("frozenhamburgerview:removeall", this);
//		// also empty collections for the frozen table
//		$("#frozenInnerDiv").remove();
//		this.model.clearFrozenColumns();
//
//	},

	destroy: function () {
		EventBus.off("window:resize", this.onWindowResize, this);
		this.close();
		QTDT.DataTableView.__super__.destroy.call(this);
	},

	// /cell methods
	viewStudyDetails: function (e) {
		// it's a study
		var studyModel = $(e.target).data("model");
		EventBus.trigger("open:details", studyModel);

	},
//	expandText: function (e) {
//		var cellModel = $(e.target).data("model");
//		var model = thisDataTable.rows.findWhere({
//			index: cellModel.row
//		}).get("cells").at(cellModel.col);
//		var htmlStr = linkifyStr(model.get('parsedHtml'));
//
//
//		var dataObjJson = JSON.stringify(cellModel);
//
//		var text = htmlStr;
//		text += '<b><a href="javascript:void(0);" class="collapseText" data-model=\'' + dataObjJson + '\'>(less)</a></b>';
//
//		// we have to calculate new widths for the cells in this column because
//		// we are using divs instead of a table.
//		var h5Container = $(e.target).parent().parent();
//		// set collapse width
//		if (model.get('collapseWidth') == null) {
//			model.set('collapseWidth', h5Container.width());
//		}
//		// remove current width
//		h5Container.css('width', 'auto');
//		h5Container.html(text);
//	},
//
//	collapseText: function (e) {
//		var cellModel = $(e.target).data("model");
//		var model = thisDataTable.rows.findWhere({
//			index: cellModel.row
//		}).get("cells").at(cellModel.col);
//		var dataObjJson = JSON.stringify(cellModel);
//
//		var text = model.get('parsedHtml').slice(0, 38) +
//			'... <b><a href="javascript:void(0);" class="expandText" data-model=\'' + dataObjJson + '\'>(more)</a></b>';
//
//		// we have to calculate new widths for the cells in this column because
//		// we are using divs instead of a table.
//
//		var h5Container = $(e.target).parent().parent();
//		// remove current width
//		h5Container.css('width', 'auto');
//		h5Container.html(text);
//
//		// get collapse width
//		var collapseWidth = model.get('collapseWidth') == null ? 0 : model.get('collapseWidth');
//		// get new width with content
//		var newWidth = (collapseWidth < h5Container.width()) ? collapseWidth : h5Container.width();
//		model.set("width", newWidth);
//		// update other cells with new width
//		model.get('column').get('originalView').adjustColWidth(newWidth, true);
//	},
	// use this to expand repeatable groupd data
	expandData: function (e) {
		this.updateRepeatableGroupFunction(2, e);
	}, // use this to close repeatable group
	collapseData: function (e) {
		this.updateRepeatableGroupFunction(3, e);
	},
	addBiosample: function (e) {
		var colModel = $(e.target).data("model");
		colModel.biosampleRowUri = $(e.target).data("rowuri");
		colModel.biosampleValue = $(e.target).data("value");
		colModel.biosampleFormName = $(e.target).data("formname");
		colModel.columnName = $(e.target).data("columnname");
		EventBus.trigger("addBiosample", colModel);

	},
	addItemToBioSampleList: function (e) {
		var bioSampleArray = this.model.get("biosampleArray");
		var unselectedBioSampleArray = this.model.get("unselectedBioSampleArray");
		if ($(e.target).prop("checked")) {
			bioSampleArray.push($(e.target).val());

			//remove from unselected array
			index = $.inArray($(e.target).val(), unselectedBioSampleArray);
			unselectedBioSampleArray.splice(index, 1);
		} else {
			index = $.inArray($(e.target).val(), bioSampleArray);
			bioSampleArray.splice(index, 1);
			if(this.model.get('selectAllChecked')) {
				 
				unselectedBioSampleArray.push($(e.target).val());
			}
		}
		
		if(unselectedBioSampleArray.length > 0 && this.model.get('selectAllChecked')){
			$("#sampleCheckAll").prop("indeterminate", true);
		} else if(bioSampleArray.length > 0){
			$("#sampleCheckAll").prop("indeterminate", true);
		} else {
			$("#sampleCheckAll").prop("indeterminate", false);
		}
		
		this.disableEnableBulkOrderButton();
	},
	viewTriplanar: function (e) {
		var model = $(e.target).data("model");
		var url = "/query/triplanar.jsp?studyName=" + model.studyName + "&datasetName=" + model.datasetName + "&triplanarName=" + encodeURIComponent(model.triplanarName);
		window.open(url, "_blank");
	},
	downloadImage: function (e) {
		var model = $(e.target).data("model");
		EventBus.trigger("open:image", model);
	},
	downloadFile: function (e) {
		var model = $(e.target).data("model");
		EventBus.trigger("download:file", model);
	},
	frozenIcon: function(e) {

		var model = $(e.target).data("model");
		var colId = model.colId;
		var colModel = thisDataTable.columns.at(colId);
		var isFrozen = colModel.get("frozen");
	    var frozenClassCurrent = (isFrozen) ? "fa-lock unlock" : "fa-unlock lock";
	    var frozenClassNew = (!isFrozen) ? "fa-lock unlock" : "fa-unlock lock";
	    colModel.set("frozen", !isFrozen);

		$(e.target).switchClass(frozenClassCurrent, frozenClassNew, 300, "easeInOutQuad");
		//toggle frozen value:
		isFrozen = !isFrozen;
        var frozenIndex = -1;
        var frozenScrollPositionX = this.frozenScrollPositionX;
        if ( !isFrozen && (frozenScrollPositionX > 0) ) {
			$(".frozenIcon").each(function (s) {
				var model = $(this).data("model");
				var thisColId = model.colId;
				var thisColModel = thisDataTable.columns.at(thisColId);
				var thisFrozen = thisColModel.get("frozen");

				if ( thisFrozen !== isFrozen) {
				    var thisFrozenClassCurrent = (thisFrozen) ? "fa-lock unlock" : "fa-unlock lock";
				    $(this).switchClass(thisFrozenClassCurrent, frozenClassNew, 300, "easeInOutQuad");
					thisColModel.set("frozen", isFrozen);
				}
			});
        }
        else {
			$(".frozenIcon").each(function (s) {
				var model = $(this).data("model");
				var thisColId = model.colId;
				var thisColModel = thisDataTable.columns.at(thisColId);
				var thisFrozen = thisColModel.get("frozen");

				if ( thisFrozen !== isFrozen) {
					if ( (isFrozen && (thisColId < colId)) ||
						 (!isFrozen && (thisColId > colId)) ) {
				    	var thisFrozenClassCurrent = (thisFrozen) ? "fa-lock unlock" : "fa-unlock lock";
				    	$(this).switchClass(thisFrozenClassCurrent, frozenClassNew, 300, "easeInOutQuad");
						thisColModel.set("frozen", isFrozen);
					}
				}

				thisFrozen = thisColModel.get("frozen");
				if ( thisFrozen ) {
					frozenIndex = Math.max(frozenIndex, thisColId);
				}
			});
        }
		this.frozenIndex = frozenIndex;
		if ( frozenIndex === -1 ) {
			this.frozenOffsetX = -1;
			this.frozenScrollPositionX = -1;
			this.frozenScrollOffsetX = -1;
		}
		else {
			if ( this.frozenOffsetX === -1 ) {
				this.frozenOffsetX = this.scrollOffsetX;
			}
			this.frozenScrollPositionX = this.scrollPositionX;
			this.frozenScrollOffsetX = this.headerData[this.frozenOffsetX].offset - this.scrollPositionX;
		}
		this.updateFrozen = true;
		this.renderVirtTable();
	},
	// column header methods
	sortAsc: function (e) {
		var model = $(e.target).data("model");
		var column = thisDataTable.columns.at(model.colId);
		$(".sort_asc").each(function (s) {
			$(this).switchClass("sort_asc", "sorting", 1000, "easeInOutQuad");
		});

		$(".sort_desc").each(function (s) {
			$(this).switchClass("sort_desc", "sorting", 1000, "easeInOutQuad");
		});

		$(e.target).switchClass("sorting", "sort_asc", 1000, "easeInOutQuad");

		QueryTool.query.set("sortColName", column.getFullName());
		QueryTool.query.set("sortOrder", "asc");
		column.set("sortDir", "asc");

		this.sortFunction();
		// EventBus.trigger("runSort", this);

	},
	sortDesc: function (e) {
		var model = $(e.target).data("model");
		var column = thisDataTable.columns.at(model.colId);
		$(".sort_asc").each(function (s) {
			$(this).switchClass("sort_asc", "sorting", 1000, "easeInOutQuad");
		});

		$(".sort_desc").each(function (s) {
			$(this).switchClass("sort_desc", "sorting", 1000, "easeInOutQuad");
		});

		$(e.target).switchClass("sorting", "sort_desc", 1000, "easeInOutQuad");
		// call the event to query
		QueryTool.query.set("sortColName", column.getFullName());
		QueryTool.query.set("sortOrder", "desc");
		column.set("sortDir", "desc");
		this.sortFunction();
		// /EventBus.trigger("runSort", this);

	},
	noSort: function (e) {
		var model = $(e.target).data("model");
		var column = thisDataTable.columns.at(model.colId);
		this.$el.find(".sort_desc").switchClass("sort_desc", "sorting", 1000, "easeInOutQuad");
		// call the event to query
		QueryTool.query.set("sortColName", "");
		QueryTool.query.set("sortOrder", "asc");
		column.set("sortDir", "none");

		this.sortFunction();
		// EventBus.trigger("runSort", this);
	},
	openHamburger: function (e) {
		var $titleHamburger = $(e.currentTarget);
		var model = $titleHamburger.data("model");
		var dtv = this
		var colModel;
		var setClickEvents = function () {};
		switch (model.row) {
			case 0:
				colModel = this.model.formColumns.at(model.col);
				break;
			case 1:
				colModel = this.model.rgColumns.at(model.col);
				setClickEvents = function () {
					$('.rgShow').on('click', {
						dtv: dtv
					},dtv.toggleRgShow);

					$('.rgHide').on('click', {
						dtv: dtv
					},dtv.toggleRgHide);
				};



				break;
			case 2:
				colModel = this.model.columns.at(model.col);
				setClickEvents = function () {
					$('.showHide').on('click', {
						dtv: dtv
					}, dtv.onClickShowHide);

					$('.goToDetails').on('click', dtv.goToDetails);

					$('.applyFilter').on('click', dtv.applyFilter);

					$('.applyDiagnosisFilter').on('click', dtv.applyDiagnosisFilter);
					
					$('.knowledgeGraph').on('click', dtv.knowledgeGraph);

				}
				break;
			default:
				colModel = new QTDT.Col();
				break;
		}

		// ------------
		var DataSetOptions = [
			{
     			title : 'Apply Filter',
     			callback : "applyFilter"
     		} ];

		var DEOptions = [
     		// Commented out in response to CRIT-1794. This is requested
     		// functionality but has a time-consuming bug in it
     		{
     			title : 'Hide Column',
     			callback : "showHide"
     		}, {
     			title : 'Go to Details Page',
     			callback : "goToDetails"
     		}, {
     			title : 'Apply Filter',
     			callback : "applyFilter"
     		}, {
     			title : 'Knowledge Graph',
     			callback : "knowledgeGraph"
     		}  ];

 		var Rgmenuoptions = [
 		// Commented out in response to CRIT-1794. This is requested
 		// functionality but has a time-consuming bug in it
 		{
 			title : 'Show Entire Group',
 			callback : "rgShow"
 		}, {
 			title : 'Hide Entire Group',
 			callback : "rgHide"
 		} ];

 		 var actionContainterCover = $('<div>');
 		 var actionBox = $('<div>', {

				"class" : 'actionContainer'
			});

 		// add a filter specifically for GUID
		var deName =  colModel.get("name");
		if(deName === "GUID" && System.environment === "pdbp") {
			var diagnosisOption = DEOptions.filter(function(option){ return option.title === "Filter on Diagnosis" });

			if(diagnosisOption.length === 0){
				DEOptions.push({
	     			title : 'Filter on Diagnosis Change',
	     			callback : "applyDiagnosisFilter"
	     		});
			}
		}	

		var linkList = '';
		var HBOptions = [];

		if(colModel.get("headerType") == "dataElement"){
			if(deName == "Dataset"){
				HBOptions = DataSetOptions;
			} else {
				HBOptions = DEOptions;
			}			
		} else if (colModel.get("headerType") == "repeatableGroup") {
			HBOptions = Rgmenuoptions;
		} 

		var cellObj = {
				colId : colModel.get("col") || 0
	            };
		var cellObjJson = JSON.stringify(cellObj);

		for (var i = 0; i < HBOptions.length; i++) {
			linkList += '<li><a href="javascript:void(0)" data-model=\''+cellObjJson+'\' class="hblink '
					+ HBOptions[i].callback
					+ '">'
					+ HBOptions[i].title
					+ '</a></li>';
		}
		actionContainer = '<ul>'
				+ linkList
				+ '</ul>';
		actionBox.append(actionContainer);
		actionContainterCover.append(actionBox);
		// --------------------------------


		$("body").append(actionContainterCover.html());

		var offset = $titleHamburger
			.offset();

		$('.actionContainer')
			.offset({
				top: offset.top + 13,
				left: offset.left
			});
		$('.actionContainer')
			.slideDown(300);

		setClickEvents();

	},
	// Hamburger Methods
	onClickShowHide: function (e) {
		e.data.dtv.hideColumn = true;
		var model = $(e.target).data("model");	
		var column = thisDataTable.columns.at(model.colId);
		EventBus.trigger("hamburgerview:showHideCol", {
			formUri:column.formUri,
			rgUri: column.rgUri,
			rgName: column.rgName,
			deUri: column.deUri,
			deName: column.get("name"),
			// inverting this because the state has NOT been changed yet
			// and visible denotes what it should be set TO
			visible: !column.get("visible"),
			col: column.get('col')
		});
		//e.data.dtv.renderVirtTable();  //see todo in toggleShowHide
		$('.actionContainer').slideUp(300);
		$('.actionContainer').remove();
	},
	toggleShowHide: function (columnProperties) {
		this.hideColumn = true;
		
		var formName = columnProperties.formName;
		if ( formName === undefined ) {
			formName = columnProperties.formUri.slice(columnProperties.formUri.lastIndexOf("/") + 1);
		 }	
		var headerData = (this.inTableData.headers != undefined) ? this.inTableData.headers[2] : [];
		var parentHeader = (this.inTableData.headers != undefined) ? this.inTableData.headers[1] : [];
		var gParentHeader = (this.inTableData.headers != undefined) ? this.inTableData.headers[0] : [];
		var columnHeader = headerData.filter(function (d) {
			// make sure the name and parent name match:
			return ((d.name === columnProperties.deName) && (parentHeader[d.parentId].name === columnProperties.rgName) 
					&& (gParentHeader[d.grandParentIndex].name === formName) )
		});
		//Hide columns on server
		var col = this.model.columns.where({
			name: columnProperties.deName, 
			formUri: columnProperties.formUri,
			rgName: columnProperties.rgName});		
		if(col.length > 0) {
			col[0].showHide(); 
		} else {
			if(columnProperties.visible === false){
				
				this.hiddenColumnsProperties.push(columnProperties);
			} else {
				
				// remove from hiddenColumnsProperties 
				this.hiddenColumnsProperties = this.hiddenColumnsProperties.filter(function (value) {
					return (value.deUri != columnProperties.deUri && value.formUri != columnProperties.formUri && value.rgUri != columnProperties.rgUri)
				});
			}
			EventBus.trigger("processed:columnCheckBox");
			return;
		}
		
		
		
			if ( columnHeader.length === 0 ) {
				console.log("toggleShowHide: failed to find", columnProperties.deName );
				if(columnProperties.visible === false){
				
					this.hiddenColumnsProperties.push(columnProperties);
				} else {
					
					// remove from hiddenColumnsProperties 
					this.hiddenColumnsProperties = this.hiddenColumnsProperties.filter(function (value) {
						return (value.deUri != columnProperties.deUri && value.formUri != columnProperties.formUri && value.rgUri != columnProperties.rgUri)
					});
				}
				if(this.model.columns.length > 0) {
					this.renderVirtTable();
				}
				return;
			}
			
			var col = columnHeader[0].col;
		
			// if hidden === -1 then the col is not in the list of hidden columns:
			var hidden = this.hiddenColumns.indexOf(col);
			// sometimes messages come from the same 'hide all blank' button press with the same column name and different
			// visible values = so this cannot be a toggle it's important to check the value of columnProperties.visible
			// to see what the message is setting the visiblity of the column to be.
			if ( (hidden !== -1) && (columnProperties.visible === true) ) { // if hidden and the message says change to visible:
				// remove from hiddenColumns and set width to actual value
				this.hiddenColumns = this.hiddenColumns.filter(function (value) {
					return value != col
				});
				// get column object from virtTable array, at some point we need to
				// consolidate column objects
				this.inTableData.headers[2].forEach(function (column, i) {
					if (column.col === col) {
						column.hidden = false;
						column.width = Math.min(250, column.name.length * 10 + 30);
					}
					if (column.key === 0) {
						column.offset = 0;
					} else {
						column.offset = headerData[column.key - 1].width + headerData[column.key - 1].offset;
					}
				});
			} else if ( (hidden === -1) && (columnProperties.visible === false) ) { // if message says to make hidden:
				this.hiddenColumns.push(col);
			}
			hidden = this.hiddenColumns.indexOf(col);
			if ( ((hidden === -1) && (columnProperties.visible === false)) || ((hidden !== -1) && (columnProperties.visible === true))) {
				console.log( "toggleShowHide: ", columnProperties.deName, col, (hidden === -1), columnProperties.visible );
			}
			
			if(this.model.columns.length > 0) {
				this.renderVirtTable();
			}
		

	},

	toggleRgShow: function (e) {
		var model = $(e.target).data("model");
		var column = thisDataTable.rgColumns.at(model.colId);
		column.get("children").each(function (col) {
			
			//only show if column is hidden
			if(!col.get("visible")) {
				EventBus.trigger("hamburgerview:showHideCol", {
					formUri: col.formUri,
					rgUri: col.rgUri,
					rgName: col.rgName,
					deUri: col.deUri,
					deName: col.get("name"),
					visible: true,
				});
			}
		});

		//e.data.dtv.renderVirtTable();  //see todo in toggleShowHide
		$('.actionContainer').slideUp(300);
		$('.actionContainer').remove();
	},

	toggleRgHide: function (e) {
		var model = $(e.target).data("model");
		var column = thisDataTable.rgColumns.at(model.colId);
		column.get("children").each(function (col) {
			
			//show hide column if visible
			if(col.get("visible")){
				EventBus.trigger("hamburgerview:showHideCol", {
					formUri: col.formUri,
					rgUri: col.rgUri,
					rgName: col.rgName,
					deUri: col.deUri,
					deName: col.get("name"),
					visible: false,
				});
			}
		});

		//e.data.dtv.renderVirtTable(); //see todo in toggleShowHide
		$('.actionContainer').slideUp(300);
		$('.actionContainer').remove();
	},

	applyFilter: function (e) {	
		var model = $(e.target).data("model");
		var column = thisDataTable.columns.at(model.colId);
		var deUri = "";
		var formUri = "";
		var deName = "";
		var rgName = "";
		var formName = "";
		
		if(column.get("name")=="Dataset"){
			
			//The repeatable group that has Dataset on non-joined form is named Forms 
			// and we need to get the formName from the dataTable
			if(column.formName == "Forms:"){
				 formName = thisDataTable.formColumns.models[1].get("name");
			} else {
				 formName = column.formName;
			}
				
			var form = QueryTool.page.get("forms").byShortName(formName);
			if (typeof form === 'object') {
				formUri = form.get("uri");
			}
			
			$('[uri="' + formUri + '"] [dename="Dataset"][filterType="DATASET"] .selectCriteriaDeFilter').click();

		} else{

			deName = column.get("name");
			rgName = column.get("parent").get("name");
			formName = column.get("parent").get("parent").get("name");
		
			// this method will retrieve the DE details from the server and then
			// call the eventbus
			var dataElement = QueryTool.page.get("dataElements").byShortName(column.get("name"));
			if (typeof dataElement === 'object') {
				deUri = dataElement.get("uri");
			}
	
			var form = QueryTool.page.get("forms").byShortName(formName);
			if (typeof form === 'object') {
				formUri = form.get("uri");
			}
	
	
			$('[uri="' + formUri + '"] [groupname="' + rgName + '"] [dename="' + deName + '"] .selectCriteriaDeFilter').click();
		}
	
			$('.actionContainer').slideUp(300);
			$('.actionContainer').remove();

	},
	
	applyDiagnosisFilter: function (e) {
		var model = $(e.target).data("model");
		var column = thisDataTable.columns.at(model.colId);
		var deUri = "";
		var formUri = "";
		var deName = column.get("name");
		var rgName = column.get("parent").get("name");
		var formName = column.get("parent").get("parent").get("name");


		// this method will retrieve the DE details from the server and then
		// call the eventbus
		var dataElement = QueryTool.page.get("dataElements").byShortName(column.get("name"));
		if (typeof dataElement === 'object') {
			deUri = dataElement.get("uri");
		}

		var form = QueryTool.page.get("forms").byShortName(formName);
		if (typeof form === 'object') {
			formUri = form.get("uri");
		}


		$('[uri="' + formUri + '"] [groupname="' + rgName + '"] [dename="highlight_diagnosis"][filterType="CHANGE_IN_DIAGNOSIS"] .selectCriteriaDeFilter').click();



		$('.actionContainer').slideUp(300);
		$('.actionContainer').remove();

	},
	applyFilters: function (e) {
			this.applyVirtFilters();
	},
	applyDisplayOption: function (displayOption) {
		this.applyVirtDisplayOption(displayOption);
	},

	goToDetails: function (e) {
		var model = $(e.target).data("model");
		var column = thisDataTable.columns.at(model.colId);
		var dataElement = QueryTool.page.get("dataElements").byShortName(column.get("name"));
		EventBus.trigger("open:details", dataElement);
	},

	knowledgeGraph: function(e) {
		var model = $(e.target).data("model");
		var column = thisDataTable.columns.at(model.colId);
		var dataElement = QueryTool.page.get("dataElements").byShortName(column.get("name"));
		var deShortName = column.get("name");
		if (typeof dataElement === 'object') {

			var selectedDENames = [];
			selectedDENames.push(deShortName);			
			
			$.ajax({
				url: "service/query/knowledgeGraphInfo",
				cache : false,
				traditional: true,
				data: {
					deNames : selectedDENames 
				},
				success : function(data) {	
					if ( data.length > 0 ) {
						if ( data[0].seeAlso.length > 0 ) {
							var valArray = data[0].seeAlso[0].split(";").map(function(s) { return s.trim(); });
							if ( valArray.length == 1 ) {
								if (valArray[0].length > 0 ) {
									valArray = data[0].seeAlso[0].split(" ").map(function(s) { return s.trim(); });
								}
								else {
									valArray = [];
								}
							}
							if ( valArray.length > 0 ) {
								var url = System.urls.query + "/knowledgeGraph.jsp?dataElementName=" + deShortName + "?color=" + headerColor;
								window.open( url, '_blank');
							}
							else {
								$.ibisMessaging("dialog", "error", "No data elements listed in See Also for " + deShortName );
							}
						}
					}
				},
				error: function(err,x,a) {            		
					$.ibisMessaging("dialog", "error", "No data elements listed in See Also for " + deShortName );
				}
			});
		}
		
		$('.actionContainer').slideUp(300);
		$('.actionContainer').remove();
	},
	
	parseCellHtml : function(d, model) {

		var finalHtml = d.name;
		var finalTitle = finalHtml;


		// should this text be disabled, TODO: add css disabled class
		if (finalHtml.indexOf("%greydis%") >= 0) {

			finalHtml = finalTitle = finalHtml.replace("%greydis%","");
			d.disabled = true;
		}

		d.highlightRow = false;
		// should this text be disabled, TODO: add css disabled class
		if (finalHtml.indexOf("%highlightdis%") >= 0) {
			d.highlightRow = true;
			finalHtml = finalTitle = finalHtml.replace("%highlightdis%","");
		}
		
		d.colorText = "";
		if (finalHtml.indexOf("%colorDis%") >= 0) {
			finalHtml = finalTitle = finalHtml.replace("%colorDis%","");
			var formTitles = [];
			var data = QueryTool.query.get("formDetails");
			for (var i = 0; i < data.length; i++) {
				var formJson = data[i];
				var form = QueryTool.page.get("forms").get(formJson.uri);
				 if ( Config.mdsUpdrsOptions.formNamesForRedText.indexOf(form.get("shortName")) > -1) { 
				        d.colorText = "color-text";
				        break;
				 }
			}
			
		}

		
		var cellColumn = this.model.columns.at(d.col);
		
		if (finalHtml.toLowerCase().indexOf("tbiosample")  >= 0) {

			var tempBioArray = finalHtml.substr(finalHtml.indexOf(":") + 1).split(",");
			if (tempBioArray.length > 0) {
				finalHtml = this.createBioSampleOrderButton(tempBioArray,d); // TODO:
																					// replace
																					// nTd
				finalTitle = "Click on this link to add this Item to your Order Manager Queue";
			} else {
				finalHtml = finalTitle = "";
			}
		} else if (finalHtml.toLowerCase().indexOf("tri-planar:") >= 0) {

			var triPlanarArray = finalHtml.substr(finalHtml.indexOf(":") + 1).split(",");
			
			d.triplanarName = triPlanarArray[2];
			if (d.triplanarName.lastIndexOf("/") >= 0) {
				d.triplanarName = d.triplanarName.substr(d.triplanarName.lastIndexOf("/") + 1);
			}
			
			finalTitle = d.triplanarName;
			finalHtml = this.createTriPlanarLink(triPlanarArray,d);

		} else if (finalHtml.indexOf("Thumbnail:") >= 0) {
			
			var thumbnailArray = finalHtml.substr(finalHtml.indexOf(":") + 1).split(",");

			if (thumbnailArray.length == 3) {
				
				d.imageName = thumbnailArray[2];

				if (d.imageName.lastIndexOf("/") >= 0) {
					d.imageName = d.imageName.substr(d.imageName.lastIndexOf("/") + 1);
				}
				finalTitle = d.imageName;
				finalHtml = this.createThumbnailLink(thumbnailArray,d);
			} else {
				finalHtml = finalTitle = "";
			}
		} else if (finalHtml.indexOf("File:") >= 0){

			var fileArray = finalHtml.substr(finalHtml.indexOf(":") + 1).split(",");

			if (fileArray.length == 3) {
				d.fileName = fileArray[2];
				if (d.fileName.lastIndexOf("/") >= 0) {
					d.fileName = d.fileName.substr(d.fileName.lastIndexOf("/") + 1);
				}
				
				finalTitle = d.fileName;
				finalHtml = this.createFileLink(fileArray,d);
			} else{
				finalHtml = finalTitle = "";
			}

		} else if (finalHtml.toLowerCase().indexOf("-disabledbutton") >= 0) {
			// create expand repeatable group button for empty sets
			finalTitle = 'Empty Group';
			finalHtml = '<a href="javascript:void(0);" class="expandDataEmpty ui-corner-all">Empty Group</a><br>';
			
		} else if (finalHtml.toLowerCase().indexOf("rgbutton") >= 0) {

			// create expand repeatable
			// group button

			var tempArray = finalHtml.substr(finalHtml.indexOf(":") + 1).split(",");
			formName= (cellColumn == null) ? "" : cellColumn.get("parent").get("parent").get("name");
	    	var form = QueryTool.page.get("forms").byShortName(formName);
			formUri = "";
	    	if(typeof form === 'object') {
	    		formUri = form.get("uri");
	    	}
			var cellObj = {
	                rowUri: tempArray[1],
	                rgFormUri:formUri,
	                rgName: tempArray[3].replace('&', '%26')
	            };
			var cellObjJson = JSON.stringify(cellObj);
			finalTitle = "Expand Group";
			finalHtml = '<a href="javascript:void(0);" title="Expand Group" class="expandData ui-corner-all" data-model=\''+cellObjJson+'\'>Expand Group</a><br>';

		} else if (finalHtml.toLowerCase().indexOf("collapsebutton") >= 0) {
			// create close repeatable
			// group button
			var tempArray = finalHtml.substr(finalHtml.indexOf(":") + 1).split(",");
			formName = (cellColumn == null) ? "" : cellColumn.get("parent").get("parent").get("name");
	    	var form = QueryTool.page.get("forms").byShortName(formName);
			formUri = "";
	    	if(typeof form === 'object') {
	    		formUri = form.get("uri");
	    	}

			var cellObj = {
	                rowUri: tempArray[1],
	                rgFormUri: formUri,
	                rgName: tempArray[3].replace('&', '%26')
	            };
			var cellObjJson = JSON.stringify(cellObj);
			finalTitle = "Collapse Group";
			finalHtml = '<a id="' + tempArray[1].replace(/[`~!@#$%^&*()_|+\-=?;:'",.<>\{\}\[\]\\\/]/gi,'')
							+ '" href="javascript:void(0);" title="Collapse Group" class="collapseData ui-corner-all" data-model=\''+cellObjJson+'\'>Collapse Group</a><br>';

		} /* else if (finalHtml.length > 38) {
			// if there is a lot of text
			// we need to allow the user
			// to expand the cell
			var newString = finalHtml;
			var cutText = finalHtml.slice(0, 38);
			if (finalHtml.indexOf("%greydis%") >= 0) {
				newString = finalHtml.replace("%greydis%","");
				cutText = newString.slice(0, 38);
			}

			model.set("html",newString);

			var dataObj = {row : d.row || 0, col : model.get("col") || 0};
			var dataObjJson = JSON.stringify(dataObj);

			finalHtml = cutText + '...<b><a href="javascript:void(0);" class="expandText" data-model=\''+dataObjJson+'\'>(more)</a></b>';
		} */


		else if (cellColumn != null && cellColumn.get("name") == "Study ID") {
			var studyId = parseInt(finalHtml);
			if(!isNaN(studyId) ) {
				var studyArray = QueryTool.page.get("studies").where({"studyId": studyId});
				var study = studyArray[0];
				var studyName = study.get("title");
				// var dataObj = { name: "John", age: 30, city: "New York" };
				var dataObj = { studyId : studyId || 0};
				var dataObjJson = JSON.stringify(dataObj);
				finalTitle = studyName;
				finalHtml = '<a href="javascript:void(0);" class="study_details" title="'+studyName+'" data-model=\''+dataObjJson+'\'>'+finalHtml +'</a><br>';
			}
		} else {
			// is there a link within the content for this cell if so let's make
			// linkify
			var exp = /(\b(((https?|ftp|file|):\/\/)|www[.])[-A-Z0-9+&@#\/%?=~_|!:,.;]*[-A-Z0-9+&@#\/%=~_|])/i;
			if (exp.test(finalHtml)) {
				finalTitle = finalHtml;
				finalHtml = linkifyStr(finalHtml);
			}
		}
		d.title = finalTitle;
		return '<h5 class="colCell '+d.colorText+'" data-row="'+d.row+'">'+finalHtml+'</h5>';
	},
	createBioSampleOrderButton : function (tempBioArray,  d) {
		// tempBioArray[0] = formName
		// tempBioArray[1] = rowUri
		// tempBioArray[2] = value:rowIsDerived
			
		var value = tempBioArray[2];
		var rowIsDerived = tempBioArray[2].substr(tempBioArray[2].indexOf(":")+1);
		var columnName = (this.model.columns.at(d.col) == null) ? "" : this.model.columns.at(d.col).get("name");
		var cellObj = {
				colId : d.col || 0,

	                rowUri: tempBioArray[1],

	                formName: tempBioArray[0],

	                value: value,

	                columnName: columnName
	            };
		
		
		var cellObjJson = JSON.stringify(cellObj);

		var checkBoxValue = tempBioArray[1] + '|' + tempBioArray[0] + '|' + value + '|' + rowIsDerived;

		//check if this has been checked already
		var checkedSamples = this.model.get("biosampleArray");
		var uncheckedSamples = this.model.get("unselectedBioSampleArray");

		var sample = checkedSamples.filter(function(v){ return v === checkBoxValue });
		
		var uncheckedSample = uncheckedSamples.filter(function(v){ return v === checkBoxValue });

		var checked = "";


		if((sample.length > 0 || this.model.get('selectAllChecked')) && (uncheckedSample.length === 0)){
			checked = "checked='checked'";
		}

		var checkBox = '<input name="bioSampleItem" class="bioSampleItem" data-model=\''+cellObjJson+'\' '+checked+' type="checkbox" value="'
				+ tempBioArray[1] + '|' + tempBioArray[0] + '|' + value + '|' + rowIsDerived
				+ '" style="float:left; margin-right:5px;" />';

		// this is use to create the add to biosample queue button


		var output = checkBox
		 + ' <a title="Click on this link to add this Item to your Order Manager Queue" href="javascript:void(0);" class="biosample ui-corner-all" style="float:left; min-width: 90px;" data-model=\''+cellObjJson+'\'  data-rowuri="'+ tempBioArray[1]
		 +'" data-formname="'
		 + tempBioArray[0]
		 +'" data-columnname="'
		 + columnName
		 + '" data-value="'+value+'">'
		 + value + '</a><div style="clear:both"></div>';

		 return output;




	},createTriPlanarLink : function(triPlanarArray, d) {
		d.studyName = triPlanarArray[0].replace("'", "\\'");
		d.datasetName = triPlanarArray[1];
		var cellObj = {
				studyName : d.studyName,
				datasetName : d.datasetName ,
				triplanarName : d.triplanarName
			};
		var cellObjJson = JSON.stringify(cellObj);
		var link = '<a href="javascript:void(0);" class="viewTriplanar" title="' + d.triplanarName + '" data-model=\''+cellObjJson+'\'>' + d.triplanarName + "</a>";

		return link;
	},createThumbnailLink : function(thumbnailArray,d) {
		d.studyName = thumbnailArray[0].replace("'", "\\'");
		d.datasetName = thumbnailArray[1];

		var linkId = d.datasetName.replace(/[^a-zA-Z0-9]/g, '_') + "_link";

		var cellObj = {
				studyName : d.studyName,
				datasetName : d.datasetName ,
				imageName : d.imageName
			};
		var cellObjJson = JSON.stringify(cellObj);
		var link = '<a id="' + linkId
				+ '" href="javascript:void(0);" class="downloadImage" title="'+d.imageName+'" data-model=\''+cellObjJson+'\'>' + d.imageName + "</a>";

		return link;
	},
	createFileLink : function (fileArray,d) {
		d.studyName = fileArray[0].replace("'", "\\'");
		d.datasetName = fileArray[1];

		var linkId = d.datasetName.replace(/[^a-zA-Z0-9]/g, '_') + "_link";
		var cellObj = {
				studyName : d.studyName,
				datasetName : d.datasetName ,
				fileName : d.fileName
			};
		var cellObjJson = JSON.stringify(cellObj);

		var link = '<a id="' + linkId
				+ '" href="javascript:void(0);" class="downloadFile" title="'+d.fileName+'" data-model=\''+cellObjJson+'\'>' + d.fileName + "</a>";

		return link;
	},
	checkAllBioSamples: function(e) {

        bioSampleArray = this.model.get("biosampleArray");
        if ($(e.target).prop("checked")) {
        	this.model.set('selectAllChecked',true);
            $('[name="bioSampleItem"]').each(function() {
                $(this).prop("checked", true);

                if ($.inArray($(this).val(), bioSampleArray) == -1) {
                    bioSampleArray.push($(this).val());
                }
            });
        } else {
        	this.model.set('selectAllChecked',false);
            $('[name="bioSampleItem"]').each(function() {
                $(this).prop("checked", false);
                index = $.inArray($(this).val(), bioSampleArray);
                bioSampleArray.splice(index, 1);

            });

        }
        
        this.disableEnableBulkOrderButton();


    },
    disableEnableBulkOrderButton : function() {
    	if ($('#bulkOrderButton').length) {
			if (bioSampleArray.length > 0) {
				$('#bulkOrderButton').removeClass("disabled");
			} else {
				$('#bulkOrderButton').addClass("disabled");
			}
		}
    },
	checkSelectedBiosamples: function() {
        bioSampleArray = this.model.get("biosampleArray");
        // add item to array when check, remove when unchecked
        if ($('[name="bioSampleItem"]').length > 0) {
            $('[name="bioSampleItem"]').each(function(e) {
                if ($.inArray($(this).val(), bioSampleArray) != -1) {
                    $(this).prop("checked", true);
                }
            });
        }
    },
    deselectSelectAll: function() {
        if ($('#sampleCheckAll').length > 0) {
            $('#sampleCheckAll').prop("checked", false);
        }
    },
    setQueryState: function(state) {
    	this.model.set("queryState",state);
    },
    addTohiddenColumnsProperties : function(columnProperties){
    	this.hiddenColumnsProperties.push(columnProperties);
    }



});
