$.fn.dataTableExt.oApi.fnPagingInfo = function(oSettings) {
	return {
		"iTotalPages": oSettings._iDisplayLength === -1 ? 0 : Math.ceil(oSettings.fnRecordsDisplay()
						/ oSettings._iDisplayLength)
	};
};

$.fn.dataTable.pipeline = function(opts) {

	var all = function() {
		return this.fnPagingInfo().iTotalPages;
	};
	// Configuration options
	var conf = $.extend({
		pages: 1, // number of pages to cache
		url: '', // script url
		beforeSend: null,
		data: null, // function or object with parameters to send to the server
		// matching how `ajax.data` works in DataTables
		method: 'GET' // Ajax HTTP method
	}, opts);

	// Private variables for storing the cache
	var cacheLower = -1;
	var cacheUpper = null;
	var cacheLastRequest = null;
	var cacheLastJson = null;
	return function(request, drawCallback, settings) {
		var ajax = false;
		var requestStart = request.start;
		var drawStart = request.start;
		var requestLength = request.length;
		var requestEnd = requestStart + requestLength;
		// request.columns[0].newParm = "test";
		// request.columns.map(function(column) {
		// return column.newParm = false;
		// })

		if (settings.clearCache) {
			// API requested $this the cache be cleared
			ajax = true;
			settings.clearCache = false;
		}
		else if (cacheLower < 0 || requestStart < cacheLower || requestEnd > cacheUpper) {
			// outside cached data - need to make a request
			ajax = true;
		}
		else if (JSON.stringify(request.order) !== JSON.stringify(cacheLastRequest.order)
						|| JSON.stringify(request.columns) !== JSON.stringify(cacheLastRequest.columns)
						|| JSON.stringify(request.search) !== JSON.stringify(cacheLastRequest.search)) {
			// properties changed (ordering, columns, searching)
			ajax = true;
		}
		// Store the request for checking next time around
		cacheLastRequest = $.extend(true, {}, request);

		if (ajax) {
			// Need data from the server
			if (requestStart < cacheLower) {
				requestStart = requestStart - (requestLength * (conf.pages - 1));

				if (requestStart < 0) {
					requestStart = 0;
				}
			}

			cacheLower = requestStart;
			cacheUpper = requestStart + (requestLength * conf.pages);

			request.start = requestStart;
			request.length = requestLength * conf.pages;

			// Provide the same `data` options as DataTables.
			if ($.isFunction(conf.data)) {
				// As a function it is executed with the data object as an arg
				// for manipulation. If an object is returned, it is used as the
				// data object to submit
				var d = conf.data(request);
				if (d) {
					$.extend(request, d);
				}
			}
			else if ($.isPlainObject(conf.data)) {
				// As an object, the data given extends the default
				$.extend(request, conf.data);
			}
			settings.jqXHR = $.ajax({
				"type": conf.method,
				"url": conf.url,
				"data": request,
				"dataType": "json",
				"cache": false,
				"success": function(json) {
					cacheLastJson = $.extend(true, {}, json);

					if (cacheLower != drawStart) {
						json.data.splice(0, drawStart - cacheLower);
					}
					if (requestLength >= -1) {
						settings.recordsTotal = json.recordsTotal;
						json.data.splice(requestLength, json.data.length);
					}
					drawCallback(json);

				}

			});
		}
		else {
			json = $.extend(true, {}, cacheLastJson);
			json.draw = request.draw; // Update the echo for each response
			json.data.splice(0, requestStart - cacheLower);
			json.data.splice(requestLength, json.data.length);

			drawCallback(json);
		}

	}
};

// Register an API method that will empty the pipelined data, forcing an Ajax
// fetch on the next draw (i.e. `table.clearPipeline().draw()`)
$.fn.dataTable.Api.register('clearPipeline()', function() {
	return this.iterator('table', function(settings) {
		settings.clearCache = true;
	});
});

(function($, window, document, undefined) {
	// var DataTable = $.fn.dataTable;
	// Your plugin will go here
	// namespace - person
	// name - greg

	$.jqfactory('table.idtTable', {
		// Your plugin instance properties will go here
		// Default plugin options
		options: {
			"processing": false,
			"serverSide": false,
			"ajax": null,
			idtUrl: null,
			beforeSend: null,
			pageLength: 15,
			filtering: true,
			columns: [],
			idtData: {},
			ajaxMethod: 'GET',
			filterData: {},
			selected: [],
			rowsData: [],
			selectionDisabled: [],
			select: false,
			"iniComplete": null,
			dom: 'frtip',
			test: null,
			pagingType: "full_numbers",
			"order": [[0, "asc"]]
		// "deferLoading": 0
		},

		buttons: function() {
			var that = this;
			if (this.options.dom !== 'frtip') {
				$.extend(true, $.fn.DataTable.ext.buttons, {
					'delete': {
						className: 'buttons-delete',
						text: 'Delete',
						enabled: false,
						action: IdtActions.runDeleteAction(that.options)
					},
					'addRow': {
						className: 'buttons-add',
						text: 'AddRow',
						enabled: false,
						action: IdtActions.addRowAction()
					},
					'collection': {
						text: 'Download',
						enabled: true,
						buttons: [{
							extend: 'print',
							text: 'print',
							className: 'btn btn-xs btn-primary p-5 m-0 width-35 assets-export-btn  buttons-print',
							title: 'export_filename',
							extension: '.xlsx',
							exportOptions: {
								columns: ':visible',
								orthogonal: 'export'
							},
							enabled: true,
							action: IdtActions.exportAction()

						}, {
							extend: 'csv',
							text: 'csv',
							className: 'btn btn-xs btn-primary p-5 m-0 width-35 assets-export-btn  buttons-csv',
							title: 'export_filename',
							extension: '.csv',
							name: 'csv',
							exportOptions: {
								columns: ':visible',
								orthogonal: 'export'
							},
							enabled: true,
							action: IdtActions.exportAction()
						},

						{
							extend: 'pdf',
							text: 'pdf',
							className: 'btn btn-xs btn-primary p-5 m-0 width-35 assets-export-btn buttons-pdf',
							title: 'export_filename',
							extension: '.pdf',
							name: 'pdf',
							exportOptions: {
								columns: ':visible',
								orthogonal: 'export'
							},
							enabled: true,
							//orientation: 'landscape',
							action: IdtActions.exportAction(),
				            customize: IdtActions.pdfCustomizer()						
						}, {
							extend: 'excel',
							text: 'excel',
							className: 'btn btn-xs btn-primary p-5 m-0 width-35 assets-export-btn  buttons-excel',
							title: 'export_filename',
							extension: '.xlsx',
							exportOptions: {
								columns: ':visible',
								orthogonal: 'export'
							},
							enabled: true,
							action: IdtActions.exportAction()
						}]
					}
				});
			}
		},

		_create: function() {
			var that = this;
			if (this.options.idtUrl !== null) {
				if (this.options.serverSide === true) {
					this.options.ajax = $.fn.dataTable.pipeline({
						url: that.options.idtUrl,
						beforeSend: that.options.beforeSend,
						method: that.options.ajaxMethod,
						pages: that.options.pages,
						data: function(d) {
							var i = 0;
							var columns = that._mapNonNull(that.options.columns, function(column) {
								return column.data === "checkbox" ? null : column;
							});

							var arr = that._mapNonNull(d.columns, function(column) {
								return column.data === "checkbox" ? null : column;
							});
							arr.map(function(column) {
								column.parameter = columns[i].parameter;
								i++;
							});
							d.columns = arr;
							columnIndex = d.order[0].column;
							if(that.options.select.style == 'multi' || that.options.select.style == 'single') {
								d.order[0].column = columnIndex - 1;
							}
							// Object.keys(that.options.filterData).forEach(function(key,index)
							// {
							// filter.push({name: key, value:
							// that.options.filterData[key]})

							// });
							// d.filter = filter;

							d.obj = that.options.idtData;
							d.pageLength = that.options.pageLength;
							if(that.options.filterData) {
								$.extend(d,that.options.filterData);
							}

						}
					});

				}
				else {
					this.options.ajax = {
						url: this.options.idtUrl,
						method: this.options.ajaxMethod,
						beforeSend: this.options.beforeSend,
						data: function(d) {
							var arr = that._mapNonNull(that.options.columns, function(column) {
								return column.data === "checkbox" ? null : {
									parameter: column.parameter,
									data: column.data
								};
							});
							d.columns = arr;
							d.obj = that.options.idtData;
							if(that.options.filterData) {
								$.extend(d,that.options.filterData);
							}
						}
					};
				}
			}
			var optionRowCallback = this.options.rowCallback || undefined;
			this.options.rowCallback = function(row, data, index) {
				var $row = $(row);

				// test code. can we use datatables storage for storing the list
				// of selected elements?
				// console.log("selected rows count: " +
				// that.options.selected.length);
				// the answer: no, use our list

				if ($.inArray(data.DT_RowId, that.options.selected) !== -1) {
					$row.addClass('selected');
				}

				if ($.inArray(data.DT_RowId, that.options.selectionDisabled) !== -1) {
					$row.addClass("idtSelectionDisabled");
					data.selectionDisable = true;
				}
				else {
					data.selectionDisable = false;
				}

				// allows the developer to specify a rowCallback as well and
				// have it run
				if (optionRowCallback) {
					optionRowCallback(row, data, index);
				}
			};

			var optionDrawCallback = this.options.drawCallback || undefined;
			this.options.drawCallback = function(settings) {
				that._createTdLinkClass();
				that.options.totalRowCount = 0;
				var api = new $.fn.dataTable.Api(settings);
				if(that.options.serverSide === true) {
					that.options.totalRowCount = settings._iRecordsTotal
				}else {
					that.options.totalRowCount = api.rows().count();
				}
				that._idtInfoCallback(that.options.totalRowCount);
				// allows the developer to specify a drawCallback as well and
				// have it run
				if (optionDrawCallback) {
					optionDrawCallback(settings);
				}
			}

			// sets up selection
			var selectDefaults = {
				style: "api",
				selector: "tr:not(.idtSelectionDisabled) td",
				items: "row",
				info:false
			};
			var currentSelect = this.options.select;
			if (currentSelect && typeof currentSelect !== "object") {
				if (typeof currentSelect === "boolean") {
					currentSelect = "os";
				}
				selectDefaults.style = currentSelect;
			}
			this.options.select = selectDefaults;

		},
		_idtInfoCallback: function(totalRowCount) {
			var $elId = $(this.element)[0].id;
			var selectedRow = this.options.selected;
			var currentSelect = this.options.select;
			if(typeof currentSelect === "object") {
				if(currentSelect.style !== "api") {
					if(selectedRow.length <= 1) {
				     	$("#" + $elId + "_info").append('<span class=listSelectedRows> (' + selectedRow.length + ' row selected of '+totalRowCount+')</span>');
					}else {
				     	$("#" + $elId + "_info").append('<span class=listSelectedRows> (' + selectedRow.length + ' rows selected of '+totalRowCount+')</span>');
					}
				}
			}
		},
		_mapNonNull: function(arr, cb) {
			return arr.reduce(function(accumulator, value, index, arr) {
				var result = cb.call(null, value, index, arr);
				if (result != null) {
					accumulator.push(result);
				}

				return accumulator;
			}, []);
		},
		_createButtonSetting: function(buttons) {
			if (this.options.buttons) {
				buttons.map(function(button) {
					if (!button.enableControl && button.extend !== 'collection') {
						button.enableControl = {
							count: 1,
							invert: false
						}
					}
				});
			}
		},
		_createTdLinkClass: function() {
			var $el = $(this.element);
			$el.find('td a').addClass('tdLink');
			$el.find('a.morelink').removeClass('tdLink');
			$el.find('a.less').removeClass('tdLink');
		},
		_collectionUnion: function(arr1, arr2, equalityFunc) {
		    var union = arr1.concat(arr2);

		    for (var i = 0; i < arr1.length; i++) {
		        for (var j = arr1.length; j < union.length; j++) {
		            if (equalityFunc(union[i], union[j])) {
		                union.splice(j, 1);
		                j--;
		            }
		        }
		    }

		    return union;		
		},
		_areRowsIdsEqual: function(g1, g2) {
		    return g1.DT_RowId === g2.DT_RowId;
		},
		_startupDatatable: function() {
			var that = this, buttonsConf = this.options.buttons, selectedRows;
			this.buttons();
			this._createCheckBox();

			var oTable = $(this.element).DataTable(this.options);

			this._createButtonSetting(buttonsConf);
			var tableSetting = oTable.settings()[0].oInstance;
			if(that.options.filtering == true) {
				tableSetting.fnDtFilter({
					aoColumns: that.options.filters
				});
			}

			var selectSet = new $.fn.dataTable.SelectSet(tableSetting, that.options);
			if(that.options.searching != false) {
				var searchColumn = tableSetting.IdtSearchColumn(tableSetting, that.options);
			}

			this._createButtonSetting(buttonsConf);
			
			oTable.on('select', function(e, dt, type, indexes) {
				var selectedRows = dt.rows(indexes);
				// we reference the rows in "selected" by ID
				var justSelectedIds = selectedRows.ids().toArray();
				
				// we assume, if a select event is called, those should be added
				// to the list ignoring single/multi
				// underscore's union performs a merge-and-unique operation
				that.options.selected = _.union(that.options.selected, justSelectedIds);
				var masterSelectionList = that.options.selected;
				
				//if the table is server side, we assume, if the select event is called, 
				//rows data should be added to the list
				if (that.options.serverSide === true) {
					var selectedRowsData = selectedRows.data().toArray();
					that.options.rowsData = that._collectionUnion(that.options.rowsData, selectedRowsData, that._areRowsIdsEqual);
				}
				//we override the footer on select row
				if(masterSelectionList.length <= 1) {
					$("#" + (this.id) +"_info .listSelectedRows").html(' (' + masterSelectionList.length + ' row selected of '+that.options.totalRowCount+')');
				}else {
					$("#" + (this.id) +"_info .listSelectedRows").html(' (' + masterSelectionList.length + ' rows selected of '+that.options.totalRowCount+')');;
				}
				
				if (that.options.buttons) {
					(buttonsConf).map(function(button, index) {
						if (button.enableControl == null) { return; }
						switch (button.enableControl.invert) {
						case false:
							oTable.buttons(index).enable(masterSelectionList.length >= button.enableControl.count);
							break;
						case true:
							if (button.enabled == false) {
								if (masterSelectionList.length > button.enableControl.count) {
									oTable.buttons(index).disable();
								}
								else {
									oTable.buttons(index).enable(
													masterSelectionList.length <= button.enableControl.count);
								}
							}
							else {
								if (masterSelectionList.length > button.enableControl.count) {
									oTable.buttons(index).disable();
								}
							}
							break;
						}
					});
				}

				selectSet.onSelect(masterSelectionList.length);

			});

			oTable.on("deselect", function(e, dt, type, indexes) {
				var selectedRows = dt.rows(indexes);
				// we reference the rows in "selected" by ID
				var justDeselectedIds = selectedRows.ids().toArray();
				// @see http://underscorejs.org/#difference
				that.options.selected = _.difference(that.options.selected, justDeselectedIds);
				var masterSelectionList = that.options.selected;
				
				if (that.options.serverSide === true) {
					var deselectedRowsData = selectedRows.data().toArray();
					that.options.rowsData = _.filter(that.options.rowsData, function(obj){ return !_.findWhere(deselectedRowsData, obj); });					
				}
				
				if(masterSelectionList.length <= 1) {
					$("#" + (this.id) +"_info .listSelectedRows").html(' (' + masterSelectionList.length + ' row selected of '+that.options.totalRowCount+')');
				}else {
					$("#" + (this.id) +"_info .listSelectedRows").html(' (' + masterSelectionList.length + ' rows selected of '+that.options.totalRowCount+')');
				}

				if (that.options.buttons) {
					(buttonsConf).map(function(button, index) {
						if (button.enableControl == null) { return; }
						switch (button.enableControl.invert) {
						case false:
							if (masterSelectionList.length < button.enableControl.count) {
								oTable.buttons(index).disable();
							}
							break;
						case true:
							if (button.enabled == false) {
								if (masterSelectionList.length === 0 || masterSelectionList.length > button.enableControl.count) {
									oTable.buttons(index).disable();
								}
								else {
									oTable.buttons(index).enable(masterSelectionList.length <= button.enableControl.count);
								}
							}
							else {
								oTable.buttons(index).enable(masterSelectionList.length <= button.enableControl.count);
							}
							break;
						}
					});
				}
				selectSet.onSelect(masterSelectionList.length);
			});
			
		},
		_createCheckBox: function() {
			if (this.options.select.style == 'multi') {
				this.options.columns.unshift({
					'searchable': false,
					'orderable': false,
					'name': 'checkbox',
					'data': 'checkbox',
					width: "20px",
					render: function(data, type, full, meta) {
						return '<span class="fa fa-square-o" aria-hidden="true"></span>';
					}
				});
				this.options.order = [[1, "asc"]];
			}
			else if (this.options.select.style == 'single') {
				this.options.columns.unshift({
					'searchable': false,
					'orderable': false,
					'name': 'checkbox',
					'data': 'checkbox',
					width: "20px",
					render: function(data, type, full, meta) {
						return '<span class="fa fa-circle-o" aria-hidden="true"></span>';
					}
				});
				this.options.order = [[1, "asc"]];
			}
		},

		_render: function() {
			this._startupDatatable();

		},

		_events: {
			'tr click': function(e) {
				e.preventDefault();
			},
			'.tdLink click': function(e) {
				e.stopImmediatePropagation();
			}
		}

	});

}(jQuery, window, document));
