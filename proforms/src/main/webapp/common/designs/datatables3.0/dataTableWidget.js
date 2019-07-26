
    $.fn.dataTableExt.oApi.fnPagingInfo = function(oSettings) {
        return {
            "iTotalPages": oSettings._iDisplayLength === -1 ?
                0 : Math.ceil(oSettings.fnRecordsDisplay() / oSettings._iDisplayLength)
        };
    };
    $.fn.dataTable.pipeline = function(opts) {

        var test = function() {
            return this.fnPagingInfo().iTotalPages;
        };
        // Configuration options
        var conf = $.extend({
            pages: 2, // number of pages to cache
            url: '', // script url
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
            console.log("requestLength", requestLength);
            console.log("requestEnd", requestEnd);
            // request.columns[0].newParm = "test";
            // request.columns.map(function(column) {
            //     return column.newParm = false;
            // })

            if (settings.clearCache) {
                // API requested that the cache be cleared
                ajax = true;
                settings.clearCache = false;
            } else if (cacheLower < 0 || requestStart < cacheLower || requestEnd > cacheUpper) {
                // outside cached data - need to make a request
                ajax = true;
            } else if (JSON.stringify(request.order) !== JSON.stringify(cacheLastRequest.order) ||
                JSON.stringify(request.columns) !== JSON.stringify(cacheLastRequest.columns) ||
                JSON.stringify(request.search) !== JSON.stringify(cacheLastRequest.search)
            ) {
                // properties changed (ordering, columns, searching)
                ajax = true;
            }
            console.log(request.columns);
            console.log(conf.pages);
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
                } else if ($.isPlainObject(conf.data)) {
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
                        console.log(cacheLastJson);
                        if (cacheLower != drawStart) {
                            json.data.splice(0, drawStart - cacheLower);
                        }
                        if (requestLength >= -1) {
                            json.data.splice(requestLength, json.data.length);
                        }
                        json.draw = request.draw;
                        drawCallback(json);
                        console.log(json.draw );

                    }

                });
            } else {
                json = $.extend(true, {}, cacheLastJson);
                console.log(cacheLastJson);
                json.draw = request.draw; // Update the echo for each response
                json.data.splice(0, requestStart - cacheLower);
                json.data.splice(requestLength, json.data.length);

                drawCallback(json);
                console.log(json.draw );
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

        $.jqfactory('person.idtTable', {
            // Your plugin instance properties will go here
            // Default plugin options
            options: {
                "processing": true,
                "serverSide": true,
                "ajax": null,
                urlfactory : null,
                columns : [],
                datatest: {},
                ajaxMethod: 'GET',
                testLength: 0,
                filters: [],
                filterData: {},
                "iniComplete" : null

            },
            buttons: function() {

                $.fn.dataTable.ext.buttons.alert = {
                    className: 'buttons-alert',
                    text: 'My button 1',

                    action: function ( e, dt, node, config ) {
                        alert( this.text() );
                    }
                };
            },

            _create: function() {
                var that = this;
                this.options.ajax = $.fn.dataTable.pipeline({
                                url: that.options.urlfactory,
                                method: that.options.ajaxMethod,
                                data:  function(d) {
                                    var i = 0, arr = [];
                                if(that.options.serverSide === true) {
                                    d.columns.map(function(column) {
                                        column.newParm = that.options.columns[i].newParm;
                                        i++;
                                    });
                                }else {
                                    that.options.columns.map(function(column) {
                                        arr.push({newParm: column.newParm, data: column.data});
                                    });
                                    d.columns = arr;
                                    d.start = 0;
                                    d.length = 20;
                                }

                                    d.obj = that.options.datatest;

                                    console.log("d", d);

                                }


                            });
                this.options.initComplete = function() {
                        that._drawFilter();
                }

            },

            _startupDatatablest: function() {
                // this.ajaxfunc();
                var that = this;
                this.buttons();
                var tableTest = $(this.element).DataTable(this.options);
                console.log("this", tableTest);
                tableTest.on('select', function() {
                    console.log('this', $(this));
                    var selectedRows = tableTest.rows({ selected: true });
                    that.options.selectedRows = selectedRows[0];
                    console.log(that.options);
                    tableTest.buttons().enable(selectedRows.count() > 0);
                });
            },
            _drawFilter: function() {
                var that = this;
                this.options.filters.forEach(function(filter) {
                    that._addFilter(filter);
                });
            console.log(this.options.filterData);
            },
        _addFilter : function(config) {
            var that = this;
            let tableModel = $(this.element);

            // add filter to filterdata
            this.options.filterData[config.name] = config.defaultValue;

            // render filter control
            if (typeof config.render === "undefined"
                    && typeof config.options !== "undefined") {

               this._renderDefaultDropdown(config);
            }
            else if (typeof config.render !== "undefined") {
                let $container = $("#" + config.containerId + " ul").eq(0);
                config.render(tableModel, $container);
            }

            let decoratedFilter = function(oSettings, aData, iDataIndex) {
                let result = true;
                if ($(oSettings.nTable).parents(".tableContainer").attr("id") == config.containerId) {
                    result = config.test(oSettings, aData, iDataIndex, that.options.filterData, tableModel);
                }
                // ensures that we allow any row that was not EXPLICITLY told to not show
                return result !== false;
            };

            // add test function to filter stack
            $.fn.dataTableExt.afnFiltering.push(decoratedFilter);
        },
            _getInputKeyValue : function($table, inputName) {
                    var $input = $table.parents(".tableContainer").find('#' + inputName);
                    var name, value;
                    if ($input.is('[type="checkbox"]')) {
                        name = $input.attr("name");
                        value = false;
                        if ($input.is(":checked")) {
                            value = true;
                        }
                    }
                    else {
                        var arrChangeVal = $input.val().split(":");
                        name = arrChangeVal[0];
                        value = arrChangeVal[1];
                    }
                    // any other input-specific cases go here

                    return [name, value];
                },
                _renderDefaultDropdown : function(config) {
                    var $container = $("#" + config.containerId + " ul").eq(0);
                    $container.append('<li><select id="' + config.name + '" class="filterInput"></select>');
                    var $input = $container.find("#" + config.name);
                    for (var i = 0; i < config.options.length; i++) {
                        var option = config.options[i];
                        var selected = "";
                        if (config.defaultValue == option.value) {
                            selected = 'selected="selected"';
                        }
                        $input.append('<option value="'+ config.name +':'+ option.value +'" '+ selected +'>'+ option.text +'</option>');
                    }
                },

            _render: function() {
                this._startupDatatablest();

            }


        });
    }(jQuery, window, document));
    
    
    // this is how we call the widget with the configs :
    
    
    $(document).ready(function() {

    	var oldExportAction = function (self, e, dt, button, config) {
    	    if (button[0].className.indexOf('buttons-excel') >= 0) {
    	        if ($.fn.dataTable.ext.buttons.excelHtml5.available(dt, config)) {
    	            $.fn.dataTable.ext.buttons.excelHtml5.action.call(self, e, dt, button, config);
    	        }
    	        else {
    	            $.fn.dataTable.ext.buttons.excelFlash.action.call(self, e, dt, button, config);
    	        }
    	    } else if (button[0].className.indexOf('buttons-print') >= 0) {
    	        $.fn.dataTable.ext.buttons.print.action(e, dt, button, config);
    	    } else if (button[0].className.indexOf('buttons-pdf') >= 0) {
    	        if ($.fn.dataTable.ext.buttons.pdfHtml5.available(dt, config)) {
    	            $.fn.dataTable.ext.buttons.pdfHtml5.action.call(self, e, dt, button, config);
    	        }
    	        else {
    	            $.fn.dataTable.ext.buttons.pdfFlash.action.call(self, e, dt, button, config);
    	        }
    	    } else if (button[0].className.indexOf('buttons-csv') >= 0) {
    	        if ($.fn.dataTable.ext.buttons.csvHtml5.available(dt, config)) {
    	            $.fn.dataTable.ext.buttons.csvHtml5.action.call(self, e, dt, button, config);
    	        }
    	        else {
    	            $.fn.dataTable.ext.buttons.csvFlash.action.call(self, e, dt, button, config);
    	        }
    	    }
    	};

    	var newExportAction = function (e, dt, button, config) {
    	    var self = this;
    	    console.log("self", self)
    	    var oldStart = dt.settings()[0]._iDisplayStart;
    	    console.log('oldStart', oldStart);

    	    dt.one('preXhr', function (e, s, data) {
    	        // Just this once, load all data from the server...

    	        data.start = 0;
    	        data.length = 2147483647;


    	        dt.one('preDraw', function (e, settings) {
    	            // Call the original action function
    	            oldExportAction(self, e, dt, button, config);

    	            dt.one('preXhr', function (e, s, data) {
    	                // DataTables thinks the first item displayed is index 0, but we're not drawing that.
    	                // Set the property to what it was before exporting.
    	                settings._iDisplayStart = oldStart;
    	                data.start = oldStart;
    	                console.log("data.start", data.start);

    	            });

    	            // Reload the grid with the original page. Otherwise, API functions like table.cell(this) don't work properly.
    	            setTimeout(dt.ajax.reload, 0);

    	            // Prevent rendering of the full data to the DOM
    	            return false;

    	        })
    	    });

    	    // Requery the server with the new one-time export settings
    	    dt.ajax.reload();
    	};
    	
    	var selected = [];
        var table = $("#tableTest").greg({
                urlfactory: 'http://localhost:8089/app',
                ajaxMethod: 'GET',
                datatest: {
                    name: "name"
                },
                length: 10,
                "columns": [
                    {
                        "data": "name",
                        "title": "name",
                        "name": "name",
                        "newParm" : true,
                        "searchable": true,
                        "orderable": true
                    },
                    {
                        "data": "position",
                        "title": "position",
                        "name": "position",
                        "newParm" : false,
                        "searchable": true,
                        "orderable": true
                    },
                    {
                        "data": "office",
                        "title": "office",
                        "name": "office",
                        "newParm" : true,
                        "searchable": true,
                        "orderable": true
                    },
                    {
                        "data": "extn",
                        "title": "extn",
                        "name": "extn",
                        "newParm" : true,
                        "searchable": true,
                        "orderable": true
                    },
                    {
                        "data": "start_date",
                        "title": "start_date",
                        "name": "start_date",
                        "newParm" : false,
                        "searchable": true,
                        "orderable": true
                    }
                ],
            searchable: false,
            dom: 'Bfrtip',
            fixedHeader: true,
            select: {
                style: 'multi'
            },
        "rowCallback": function( row, data ) {
            if ( $.inArray(data.DT_RowId, selected) !== -1 ) {
                $(row).addClass('selected');
            }
        },
          // initComplete: function () {
          //           this.api().columns().every( function () {
          //               var column = this;
          //               var select = $('<select><option value=""></option></select>')
          //                   .appendTo( $(column.header()).empty() )
          //                   .on( 'change', function () {
          //                       var val = $.fn.dataTable.util.escapeRegex(
          //                           $(this).val()
          //                       );

          //                       column
          //                           .search( val ? '^'+val+'$' : '', true, false )
          //                           .draw();
          //                   } );

          //               column.data().unique().sort().each( function ( d, j ) {
          //                   select.append( '<option value="'+d+'">'+d+'</option>' )
          //               } );
          //           } );
          //       },
                filters: [{
                                      containerId: "data_table_second",
                                      name: "weirded",
                                      defaultValue: "one",
                                      options: [
                                        {
                                          text: "one",
                                          value: "one"
                                        },
                                        {
                                          text: "two",
                                          value: "two"
                                        }
                                      ],
                                      test : function(oSettings, aData, iDataIndex, filterData) {
                                        if (filterData['weirded'] == "one") {
                                          if (Number(aData[1]) > 10) {
                                            return true;
                                          }
                                          else {
                                            return false;
                                          }
                                        }
                                      }
                                    },
                    {
                        containerId: "data_table_second",
                        name: "check",
                        defaultValue: "off",
                        render : function(tableModel, $container) {
                            $container.append('<li><input type="checkbox" id="check" name="check" value="check:on">check</input></li>');
                        },
                        test : function(oSettings, aData, iDataIndex, filterData) {
                            if (filterData.check == true) {
                                if (aData[4] == "Textbox") {
                                    return true;
                                }
                                else {
                                    return false;
                                }
                            }
                            else {
                                return true;
                            }
                        }
                    }
                    ],
            buttons: {
            buttons: [{
                    text: 'Row selected data',
                    action: function(e, dt, node, config) {
                        alert(
                            'Row data: ' +
                            JSON.stringify(dt.row({ selected: true }).data())
                        );
                    },
                    enabled: false,
                    className: 'test'

                },

                'alert',

                'pdf',
                {
                    extend: 'excel',
                    exportOptions: {
                        modifier: {
                            page: 'current'
                        }
                    }
                },
                {
                    extend: 'print',
                    text: 'Print selected',
                    exportOptions: {
                        modifier: {
                            selected: true
                        }
                    }
                }
            ]
            }
        });

        $('#tableTest tbody').on('click', 'tr', function () {
        var id = this.id;
        var index = $.inArray(id, selected);

        if ( index === -1 ) {
            selected.push( id );
        } else {
            selected.splice( index, 1 );
        }

        $(this).toggleClass('selected');
    } );


    });
