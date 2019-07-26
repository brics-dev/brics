/**
 * 
 */
var DataTableView = BaseView.extend({
	exportRow : null,
	$container : null,
	$buttonContainer : null,
	baseConfig : {
		jQueryUI: true,
		deferRender : true,
		lengthChange: false,
		iDisplayLength: 15,
		pageLength : 15,
		processing: true,
		//"serverSide" : true,
		//"ajax" : "http://localhost:8080/ibis/sandbox/server_processing.action",
		autoWidth: false,
		pagingType : "full_numbers",
		aaSorting : [] // disables initial sorting
	},
	
	events : {
		'click input[type="checkbox"]' : "selectCheckbox",
		'click input[type="radio"]' : "selectRadio",
		'click tr' : "selectRow",
		'click .selectAll' : "selectAll",
		'click .selectFilter' : "selectFiltered",
		'click .selectNone' : "selectNone"
	},
	
	initialize : function() {
		this.$el = this.model.get("$el");
		
		this.listenTo(this.model, 'add:row', this.onAddRow);
		this.listenTo(this.model, 'remove:row', this.onRemoveRow);
		
		this.listenTo(this.model, 'select:row', this.onAddSelectedRow);
		this.listenTo(this.model, 'deselect:row', this.onRemoveSelectedRow);
		this.listenTo(this.model, 'select:all', this.onSelectAllEvent);
		this.listenTo(this.model, 'deselect:all', this.onDeselectAllEvent);
		this.listenTo(this.model, 'select:set', this.onSelectSetEvent);
		
		this.listenTo(this.model, 'draw:table', this.onDraw);
		this.listenTo(this.model, 'derender:table', this.deRender);
		this.listenTo(this.model, 'disable:row', this.disableSingleRow);
		this.listenTo(this.model, 'enable:row', this.enableSingleRow);
		
		this.listenTo(this.model, 'change:filterData', this.updateFilter);
		
		this.listenTo(this.model, 'init:table', this.afterInit);
	},
	
	render : function() {
		var data = this.model.tempData;
		this.determineInputType(data);
		this.createTransformDom();
		this.startupDatatables(data);
		this.initInfoRow();
		this.setStyles();
		this.drawSelectAll();
		this.drawFilters();
		
		if (!this.isVisible()) {
			this.listenForVisible();
		}
		
		this.model.trigger("init:table", this.model);
		
		this.initSelectedRowsFilter();
	},
	
	/**
	 * Actually runs the datatables init command
	 */
	startupDatatables : function(compiledData) {
		var config = $.extend({}, this.config, compiledData, this.specificConfig());
		this.model.loadData(compiledData);
		var dt = this.$el.DataTable(config);
		var id = this.model.get("id");
		
		this.model.set("datatable", dt);
		this.model.set("rendered", true);
		IDT.dataTables[id] = dt;
	},
	
	/**
	 * Set up config object specific to this table.  Modifies the standard
	 * config.
	 */
	specificConfig : function() {
		let cfg = {};
		// set the language for the controls
		cfg.oLanguage = DataTableLanguage.language();
		
		// does the first column need to be sorted?
		let tableNoSort = [];
		if (this.model.get("inputType") != "none") {
			tableNoSort[0] = 0;
		}
		// outlines whether to sort on the first column or not
		cfg.aoColumnDefs = [
				             { "bSortable": false, 
				            	 "aTargets": tableNoSort, 
				            	 "bSearchable": false
				             },
				             {"sDefaultContent": "none", "aTargets": ["_all"]}
						];
		
		let modelConfig = this.model.get("config") || {};
		
		// the function builders below combine the old-style external event callback functions
		// along with the new style modelConfig event callback
		let oldInitComplete = modelConfig.initComplete;
		let initComplete = function(settings, json) {
			// local needed response to the event
			// moves the process buttons to the top of the data table
			OpButtonController.initTable($(this));
			
			// global function from old-style datatables
			if (typeof fnInitComplete === "function") {
				fnInitComplete(settings, json);
			}
			
			// from JSP/javascript config
			if (typeof oldInitComplete !== "undefined") {
				oldInitComplete.call(this, settings, json);
			}
		};
		
		let oldDrawCallback = modelConfig.drawCallback;
		let drawCallback = function(settings) {
			// local needed response to the event
			let tableRef = $(this).parents(".dataTableContainer");
			$table = tableRef.find("table").eq(0);
			let model = IDT.getTableModel($table);
			
			// propagate this method to any further callback
			if (typeof fnDrawCallback == "function") {
				fnDrawCallback(tableRef);
			}
			
			model.trigger('draw:table', model);
			
			// global function from old-style datatables
			if (typeof fnDrawCallback === "function") {
				fnDrawCallback(settings);
			}
			
			// from JSP/javascript config
			if (typeof oldDrawCallback !== "undefined") {
				oldDrawCallback.call(this, settings);
			}
		};
		
		let oldRowCallback = modelConfig.rowCallback;
		let rowCallback = function(row, data, index) {
			// global function from old-style datatables
			if (typeof fnRowCallback === "function") {
				fnRowCallback(row, data, index);
			}
			
			// from JSP/javascript config
			if (typeof oldRowCallback !== "undefined") {
				oldRowCallback.call(this, row, data, index);
			}
		};
		
		// overwrites the modelConfig with the combined function above
		// TODO: test to make sure this overwriting works.  Otherwise, we'll have to clone
		modelConfig.initComplete = initComplete;
		modelConfig.drawCallback = drawCallback;
		modelConfig.rowCallback = rowCallback;
		// if the model has a filled in config object, merge that too
		
		return $.extend({}, this.baseConfig, cfg, modelConfig);
	},
	
	
	
	/**
	 * What is the selection input for this table?  checkbox, radio, or none
	 */
	determineInputType : function(data) {
		var inputType = "none";
		if (data.data.length > 0 && data.data[0].checkbox) {
			try {
				var $element = $(data.data[0].checkbox);
				// if aaData[0][0] is regular text, element won't exist, so we check that here
				if ($element.length > 0) {
					if ($element.is('[type="radio"]') || $element.find('[type="radio"]').length > 0) {
						inputType = "radio";
					}
					else if ($element.is('input[type="checkbox"]') || $element.find('input[type="checkbox"]').length > 0) {
						inputType = "checkbox";
					}
				}
			}
			catch(e) {
				// in the case of an exception here, we've already set the type correctly
			}
		}
		this.model.set("inputType", inputType);
	},
	
	/**
	 * Gets the header cells from the DOM version of a table.  This is used by
	 * the API to break down the initial DOM table.
	 * 
	 * @param $table jQuery reference to the table to get the cells from
	 * @return Object containing the column headers html in aoColumn format
	 */
	getHeaderCells : function() {
		var $table = this.$el;
		var $tbodyTh = $("> tbody > tr > th" , $table);
		var $th = $("> tr > th" , $table);
		var $thead = $("> thead > tr > td" , $table);
		var $headerElements = $();
		
		if ($tbodyTh.length > 0) {
			$headerElements = $tbodyTh;
		} 
		else if ($th.length > 0) {
			$headerElements = $th;
		}
		else if ($thead.length > 0) {
			$headerElements = $thead;
		}
		else {
			// no head exists, use the first row
			$headerElements = $table.find("tr").eq(0);
		}
		
		var aoColumns = [];
		// using a regular loop here instead of each ensures
		// that we don't do jquery's multithreading and get the headers
		// out of order
		for (var i = 0; i < $headerElements.length; i++) {
			var $this = $headerElements.eq(i);
			if ($this.hasClass("ibisDatatable_expandedData")) {
				aoColumns.push({"bVisible": false});
			}
			else {
				var colspan = $this.attr("colspan");
				if (!colspan) {
					colspan = 1;
				}
				// so if colspan is 2, we give two columns with the same title here
				for (var j = 0; j < colspan; j++) {
					aoColumns.push({title: $this.text()});
				}
			}
		}
		
		this.model.tempData['columns'] = aoColumns;
		return $headerElements;
	},
	
	/**
	 * Interprets the row data for this table by reading the data from the DOM
	 */
	getBodyRows : function($table) {
		var aaData = [];
		var $tbody = $("> tbody > tr", $table);
		var $trs = $("> tr", $table);
		var $rows = $();
		if ($tbody.length > 0) {
			$rows = $tbody;
		}
		else {
			$rows = $trs;
		}
		
		for (var i = 0; i < $rows.length; i++) {
			var $this = $rows.eq(i);
			// each row is one entry in parent array, each cell is entry in
			// sub-array
			var rowArr = [];
			var $tds = $this.children("td");
			for (var j = 0; j < $tds.length; j++) {
				rowArr.push($tds.eq(j).html());
			}
			aaData.push(rowArr);
		}
		this.model.tempData['data'] = aaData;
		return $rows;
	},
	
	setStyles : function() {
		this.$("thead tr").addClass("tableRowHeader").attr("role", "row");
		this.$("th").addClass("tableCellHeader").attr("role", "columnheader");
	},
	
	drawSelectAll : function() {
		var inputType = this.model.get("inputType");
		if (inputType != "none" && inputType != "radio") {
			var $targetCell = this.$el.find("th.tableCellHeader").eq(0);
	//		here's some sample code needed for select all vs filtered:
	//		var table = IDT.models.at(0).get("datatable");
	//		table.fnFilter('test');
	//		table._('tr', {"filter":"applied"});
	//		NOTE: the above will give an error if the data is not filtered
			var html = '<nav class="idtNav"><ul><li class="selectAllIcon"><img src="' + baseUrl + '/formbuilder/images/checkbox_unchecked.png" />';
			html += '<ul><li><a href="javascript:;" class="selectAllMenu selectAll"><img src="' + baseUrl + '/formbuilder/images/checkbox_checked.png" />Select All</a></li>';
			html += '<li><a href="javascript:;" class="selectAllMenu selectFilter"><img src="' + baseUrl + '/formbuilder/images/checkbox_indeterminate.png" />Select Filtered</a></li>';
			html += '<li><a href="javascript:;" class="selectAllMenu selectNone"><img src="' + baseUrl + '/formbuilder/images/checkbox_unchecked.png" />Select None</a></li>';
			html += '</ul></li></nav>';
			$targetCell.html(html);
			this.determineSelectAllIcon();
		}
	},
	
	drawFilters : function() {
		let $table = this.$el;
		IDT.FilterEngine.initFilterEngine($table);
		
		if (typeof this.model.tempData.filters !== "undefined") {
			let filters = this.model.tempData.filters;
			let numFilters = filters.length;
			for (let i = 0; i < numFilters; i++) {
				IDT.FilterEngine.addFilter(filters[i]);
			}
		}
	},
	
	determineSelectAllIcon : function() {
		var selectedRowsLength = this.model.numSelected();
		var allRowsLength = this.model.numRows();
		if (selectedRowsLength == allRowsLength) {
			this.selectAllIconToChecked();
		}
		else if (selectedRowsLength > 0) {
			this.selectAllIconToPartial();
		}
		else {
			this.selectAllIconToUnchecked();
		}
	},
	
	selectAllIconToChecked : function() {
		var inputType = this.model.get("inputType");
		if (inputType != "none" && inputType != "radio") {
			this.$(".selectAllIcon > img").attr("src", baseUrl + "/formbuilder/images/checkbox_checked.png");
		}
	},
	
	selectAllIconToPartial : function() {
		var inputType = this.model.get("inputType");
		if (inputType != "none" && inputType != "radio") {
			this.$(".selectAllIcon > img").attr("src", baseUrl + "/formbuilder/images/checkbox_indeterminate.png")
		}
	},
	
	selectAllIconToUnchecked : function() {
		var inputType = this.model.get("inputType");
		if (inputType != "none" && inputType != "radio") {
			this.$(".selectAllIcon > img").attr("src", baseUrl + "/formbuilder/images/checkbox_unchecked.png")
		}
	},
	
	/**
	 * Create any needed DOM elements (table?) and/or transform
	 * the current DOM elements to what is needed by IDT
	 */
	createTransformDom : function() {
		// determine if the table is wrapped in a datatablecontainer.  If not, wrap it
		if (this.$el.parents(".dataTableContainer").length == 0) {
			this.$el.before('<div id="' + this.$el.attr("id") + '_container" class="dataTableContainer dataTableJSON"></div>');
			
			let $container = $("#" + this.$el.attr("id") + "_container");
			// this is for the buttons
			$container.append('<ul class="actionButtonsContainer></ul>');
			$container.append(this.$el);
		}
		
		this.$container = this.$el.parent();
		this.$buttonContainer = this.$container.find("ul").eq(0);
	},
	
	/**
	 * Answers the question "does this row have a selection input?"
	 * 
	 * @param $row jquery reference to the row
	 * @return jquery reference to input if a selection input exists, otherwise false
	 */
	rowHasInput : function($row) {
		var $input = IDT.getInputForRow($row);
		if ($input.length > 0) {
			return $input;
		}
		else {
			return false;
		}
	},
	
	onDraw : function(tableModel) {
		// gets all visible rows, gets their save value (if applicable) and
		// checks to see if it's selected.  If so, makes sure it's marked as
		// selected.  Otherwise, deselect
		var mainView = this;
		if (_.isEqual(tableModel, this.model)) {
			this.$el.find("tbody tr").each(function() {
				var $row = $(this);
				var $input = IDT.getInputForRow($row);
				if ($input.length > 0) {
					var saveValue = IDT.getSaveValue($input);
					if (mainView.model.isSelectedSaveVal(saveValue)) {
						mainView.setRowSelected($row);
						$input.prop("checked", true);
					}
					else {
						mainView.setRowDeselected($row);
						$input.prop("checked", false);
					}
				}
			});
		}
	},
	
	/**
	 * Re-draws the table (manually).  This also re-filters and re-sorts.
	 * 
	 */
	redraw : function(level) {
		if (typeof level === "undefined") {
			level = "full-reset";
		}
		var dt = this.model.get("datatable");
		dt.draw(level);
	},
	
	/**
	 * Responds to additions in the selectedRows collection
	 * 
	 * @param saveVal {string} the row that was acted upon (added or removed)
	 * @param saveVal {DataTable api Row} the Row that was acted upon
	 * @param model {DataTable} 
	 */
	onAddSelectedRow : function(row, model) {
		if (typeof row !== "object") {
			row = this.model.row(row);
		}
		this.select(row);
		this.redrawAfterSelection(false);
	},
	
	/**
	 * Responds to removals in the selectedRows collection
	 * 
	 * @param saveVal {string} the row that was acted upon (added or removed)
	 */
	onRemoveSelectedRow : function(saveVal) {
		var row = this.model.row(saveVal);
		this.deselect(row);
		this.redrawAfterSelection();
	},
	
	/**
	 * Responds to additions to the rows collection
	 * 
	 * @param model the model that was acted upon (added or removed)
	 * @param collection the collection that was acted upon
	 */
	onAddRow : function(model, collection) {
		this.model.table().draw();
	},
	
	/**
	 * Responds to removals in the rows collection
	 * 
	 * @param model the model that was acted upon (added or removed)
	 * @param collection the collection that was acted upon
	 */
	onRemoveRow : function(model, collection) {
		this.model.table().draw();
	},
	
	/**
	 * Responds to the table finishing rendering but before filters
	 * are added.
	 * 
	 * @param model the table model that was acted upon
	 */
	afterInit : function(model) {
		// currently empty
	},
	
	/**
	 * Selects the row NODE as defined by the DataTable row object passed in.
	 * 
	 * @attr row {DataTable API row} row element which should be selected
	 * @retun {DataTable API row} row that was acted upon
	 */
	select : function(row) {
		var $row = $(row.node());
		if ($row != null) {
			this.findInputOnRow($row).prop("checked", true);
			this.setRowSelected($row);
		}
		return row;
	},
	
	/**
	 * Deselects the row NODE as defined by the DataTable row object passed in.
	 * 
	 * @attr row {DataTable API row} row element which should be deselected
	 * @retun {DataTable API row} row that was acted upon
	 */
	deselect : function(row) {
		var $row = $(row.node());
		if ($row.length > 0) {
			this.findInputOnRow($row).prop("checked", false);
			this.setRowDeselected($row);
		}
	},
	
	/**
	 * Sets the class to highlight a row as selected.  Does NOT
	 * add the row to the set of selected rows.  This is just
	 * a utility function
	 * 
	 * Can accept a set as well as individual
	 * 
	 * @attr $row {jQuery} row list of row TR element(s)
	 */
	setRowSelected : function($row) {
		if (!$row.hasClass("row_selected")) {
			$row.addClass("row_selected");
		}
	},
	
	/**
	 * Sets the class to de-highlight a row as deselected.  Does NOT
	 * remove the row from the set of selected rows.  This is just
	 * a utility function
	 * 
	 * Can accept a set as well as individual
	 * 
	 * @attr $row {jQuery} row list of row TR element(s)
	 */
	setRowDeselected : function($row) {
		$row.removeClass("row_selected");
	},
	
	onSelectAllEvent : function() {
		this.redrawAfterSelection();
	},
	
	onDeselectAllEvent : function() {
		this.redrawAfterSelection();
	},
	
	onSelectSetEvent : function() {
		this.redrawAfterSelection();
	},
	
	redrawAfterSelection : function(tableRedraw) {
		this.updateSelectedItemCount();
		this.determineSelectAllIcon();
		if (typeof tableRedraw === "undefined" || tableRedraw) {
			this.model.trigger("draw:table", this.model);
		}
	},
	
	/*
	 * The below methods are user-interaction only.  They should not be used for
	 * programmatic control of selected options.  Those methods are in the API
	 * class: IbisDataTables.
	 */
	
	/**
	 * Responds to the user clicking on a selection input checkbox
	 */
	selectCheckbox : function(event) {
		event.stopImmediatePropagation();
		// the target is event.target
		// our process here is to determine if the row was selected previously and
		// if it was, deselect it (via the DataTable function, no other way!)
		// if it isn't, select it
		var $checkbox = $(event.target);
		var saveVal = IDT.getSaveValue($checkbox);
		this.model.toggleRow(saveVal);
	},
	
	/**
	 * Responds to the user clicking on a selection input radio button
	 */
	selectRadio : function(event) {
		event.stopImmediatePropagation();
		// luckily, selecting a radio is as easy as a checkbox thanks to our
		// methods in the model!
		var $radio = $(event.target);
		event.stopImmediatePropagation();
		var saveVal = IDT.getSaveValue($radio);
		this.model.toggleRow(saveVal);
	},
	
	/**
	 * Responds to the user clicking on a row somewhere other than a selection
	 * input.
	 */
	selectRow : function(event) {
		event.stopImmediatePropagation();
		// ...but rows are a bit more difficult because we have to find out if
		// there is an input at all.  boooo
		if (!$(event.target).is("a, input, select, label")) {
			var $input = IDT.getInputForRow($(event.currentTarget));
			if ($input.length > 0) {
				this.model.toggleRow(IDT.getSaveValue($input));
			}
		}
	},
	
	selectAll : function(event) {
		event.stopImmediatePropagation();
		
		if ( !this.model.get("disableSelectAll") ) {
			this.model.selectAll();
		}
	},
	
	selectFiltered : function(event) {
		event.stopImmediatePropagation();
		
		if ( !this.model.get("disableSelectAll") ) {
			var datatable = this.model.table();
			try {
				var rows = datatable.rows({search:'applied'});
				this.model.selectSet(rows);
			}
			catch(err) {
				// the data is not filtered, so call selectAll
				this.selectAll(event);
			}
		}
	},
	
	selectNone : function(event) {
		event.stopImmediatePropagation();
		
		if ( !this.model.get("disableSelectAll") ) {
			this.model.deselectAll();
		}
	},
	
	disableSingleRow : function(dataTable, row) {
		if (_.isEqual(dataTable, this.model)) {
			// this appears convoluted but we want to get the actual input
			// on the page, not the input from the row model
			var $row = this.getRowJqueryBySaveValue(row.id());
			this.findInputOnRow($row).prop("disabled", true);
		}
	},
	
	enableSingleRow : function(dataTable, row) {
		if (_.isEqual(dataTable, this.model)) {
			// this appears convoluted but we want to get the actual input
			// on the page, not the input from the row model
			var $row = this.getRowJqueryBySaveValue(row.id());
			this.findInputOnRow($row).prop("disabled", false);
		}
	},
	
	/**
	 * Obtains a jquery reference to the specified row.  Only works for
	 * currently-visible rows.  Used for selecting/deselecting rows
	 * 
	 * @return jquery reference to row or empty jquery reference if not found
	 */
	getRowJqueryBySaveValue : function(saveValue) {
		return $(this.model.row(saveValue).node());
	},
	
	findInputOnRow : function($row) {
		return $row.find("input").eq(0);
	},
	
	
	/* ------ InfoRowManager methods --- */
	initInfoRow : function() {
		this.getExportRow();
		this.placeExportRow();
		this.createSelectedItemsLabel();
	},
	
	getInfoRow : function() {
		return this.$el.parents(".dataTableContainer").find(".dataTables_info");
	},
	
	getSelectedItemsContainer : function() {
		return this.$el.parents(".dataTableContainer").find(".rowCount");
	},
	
	getRowWordContainer : function() {
		return this.$el.parents(".dataTableContainer").find(".rowWord");
	},
	
	placeExportRow : function() {
		var exportRow = this.model.get("exportRow");
		if (exportRow !== null) {
			var $exportDiv = $("<div></div>");
			$exportDiv.css("float", "left").addClass("tableExportAction").html(exportRow);
			var $infoRow = this.getInfoRow();
			if ($infoRow.length > 0) {
				$infoRow.after($exportDiv);
			}
		}
	},
	
	rowCountSelect : function() {
		let filterData = _.clone(tableModel.get("filterData"));
		// toggle the selectedItemFilter
		filterData.selectedItemFilter = !filterData.selectedItemFilter;
		tableModel.set("filterData", filterData);
	},
	
	getExportRow : function() {
		var $exportRow = this.$el.find("tr.tableRowAction");
		if ($exportRow.length > 0) {
			this.model.set("exportRow", this.$el.find("td.tableCellAction").html());
		}
		// the export row exists in a table all by itself inside a blank row. Weird but that's struts for ya
		$exportRow.parents("tr").eq(0).remove();
	},
	
	removeExportRow : function() {
		if (this.model.get("exportRow") !== null) {
			this.$el.find("td.tableCellAction").parents("tr").last().remove();
		}
	},
	
	createSelectedItemsLabel : function() {
		if (this.getSelectedItemsContainer().length < 1 && this.model.get("inputType") != "none") {
			let $infoRow = this.getInfoRow();
			let tableId = this.$el.attr("id");
			let $defaultDiv = $('<div class="rowCountContainer"><a href="javascript:;" class="rowCountSelect">(<span class="rowCount">0</span> <span class="rowWord">Rows</span> Selected)</a></div>');
			$infoRow.after($defaultDiv);
		}
	},
	
	initSelectedRowsFilter : function() {
		let selectedItemFilter = {
				containerId : this.$el.parents(".dataTableContainer").eq(0).attr("id"),
				name: "selectedItemFilter",
				defaultValue : false,
				render : function() {
					// do not render
				},
				test : function(oSettings, aData, iDataIndex, filterData, tableModel) {
					// determines if the row is selected and, if so, allows it
					if (filterData.selectedItemFilter) {
						let row = tableModel.table().row(iDataIndex);
						return tableModel.isSelected(row);
					}
					return true;
				}
		};
		IDT.FilterEngine.addFilter(selectedItemFilter);
	},
	
	/**
	 * Updates the selected item count in the table's info row
	 * @count the new count to place in the info row
	 */
	updateSelectedItemCount : function() {
		this.createSelectedItemsLabel();
		var count = this.model.numSelected();
		var $countContainer = this.getSelectedItemsContainer();
		$countContainer.text(count);
		this.updateRowWord(count);
	},
	
	getCountSelectedItems : function() {
		return this.getSelectedItemsContainer().text();
	},
	
	updateRowWord : function(count) {
		var word = "Rows";
		if (count == 1) {
			word = "Row";
		}
		this.getRowWordContainer().text(word);
	},
	
	updateFilter : function() {
		let settings = this.model.get("datatable").init();
		if (settings.ajax) {
			// TODO: perform ajax filter
		}
		else {
			this.redraw("full-reset");
		}
	},
	
	isVisible : function() {
		return this.$el.is(":visible");
	},
	
	/**
	 * Checks every TIMEOUT milliseconds to see if this table has become
	 * visible.  If not, we wait again.  If so, recalculate the
	 * header/footer widths and stop listening.
	 */
	listenForVisible : function() {
		var timeout = 500;
		var view = this;
		
		if (this.isVisible()) {
			IDT.recalculateHeaderFooterWidths(this.$el);
		}
		else {
			setTimeout(function() {
				view.listenForVisible.call(view);
			}, timeout);
		}
	},
	
	deRender : function(model) {
		if (_.isEqual(model, this.model)) {
			this.close();
		}
	},
	
	close : function() {
		var dt = this.model.get("datatable");
		dt.fnDestroy();
		DataTableView.__super__.close.call(this);
	},
	
	destroy : function() {
		this.close();
		DataTableView.__super__.destroy.call(this);
	}
});