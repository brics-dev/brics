/**
 * Provides the Select All, Select None, and Select Filtered widget addon to the
 * Ibis DataTable (IDT) table. Renders the control and enables its actions. Only
 * draws the control if the table is multi-select and there are no
 * configurations saying it shouldn't be rendered. NOTE: variable thisFileName
 * should be changed if this file is renamed
 */
(function($, window, document, undefined) {
	var thisFileName = "idtSelectSetPlugin.js";

	// main function here
	$.fn.dataTable.SelectSet = function(table, opts) {
		// private methods here

		/**
		 * Determines if the table is selectable in any way.
		 * 
		 * @return boolean true if the table is selectable, otherwise false
		 */
		function isSelectable() {
			// options for settings.select: "multi", "single", false, or not set
			// confused? @see
			// https://stackoverflow.com/questions/784929/what-is-the-not-not-operator-in-javascript
			return !!settings.select;
		}

		/**
		 * Determines if the table is multi-selectable
		 * 
		 * @return boolean true if the table is multi-selectable, otherwise
		 *         false
		 */
		function isMultiSelect() {
			return isSelectable() && settings.selectParams.mode == "multi";
		}

		/**
		 * Determines if Select All should respond to clicks
		 */
		function shouldAllowSelectAll() {
			return isWidgetVisible && settings.selectParams.selectAllEnabled;
		}

		/**
		 * Determines if Select None should respond to clicks
		 */
		function shouldAllowSelectNone() {
			return isWidgetVisible && settings.selectParams.selectNoneEnabled;
		}

		/**
		 * Determines if Select Filtered should respond to clicks
		 */
		function shouldAllowSelectFiltered() {
			return isWidgetVisible && settings.selectParams.selectFilteredEnabled;
		}

		/**
		 * Renders the control and registers the event click handlers
		 */
		function render() {
			var $wrapper = $(api.table().node()).parent();
			var $columnHeader = $wrapper.find("th:first-child");
			var baseUrl = getWidgetBaseDirectory();
			if (baseUrl !== "") {
				var templateUrl = baseUrl + "templates/selectSet.hbs";
				TemplateManager.loadNewAsync("selectSet", templateUrl, function(template) {
					$columnHeader.prepend(template({}));
					isWidgetVisible = true;
					$widget = $columnHeader.find(".idt_selectAllSelector");
					if (!shouldAllowSelectAll()) {
						$columnHeader.find(".idt_selectAll").hide();
					}
					if (!shouldAllowSelectNone()) {
						$columnHeader.find(".idt_selectNone").hide();
					}
					if (!shouldAllowSelectFiltered()) {
						$columnHeader.find(".idt_selectFilter").hide();
					}
					registerEvents();
				});
			}
			// else, don't try to render because you'll fail
		}

		/**
		 * Registers the event handlers including open/close the menu and all
		 * sub-menu item click handlers.
		 */
		function registerEvents() {
			$widget.find(".idt_selectAllHeader").on("click", openCloseDropdownCallback);
			if (settings.serverSide) {
				
				$widget.find(".idt_selectFilter").on("click", selectFilterServerCallback);
			}
			else {
				$widget.find(".idt_selectFilter").on("click", selectFilteredCallback);
			}
			$widget.find(".idt_selectAll").on("click", confirmSelectAllCallBack);
			$widget.find(".idt_selectNone").on("click", selectNoneCallback);

			$(api.table().node()).parent();
		}

		this.onSelect = function(selectedRowsLength) {
			if ($widget) {
				var fullLength = api.settings()[0].fnRecordsTotal();
				if (selectedRowsLength === 0) {
					$widget.removeClass("idt_selectAll").removeClass("idt_selectFilter").addClass("idt_selectNone");
				}
				else if (selectedRowsLength == fullLength) {
					$widget.removeClass("idt_selectFilter").removeClass("idt_selectNone").addClass("idt_selectAll");
				}
				else {
					$widget.removeClass("idt_selectAll").removeClass("idt_selectNone").addClass("idt_selectFilter");
				}
			}
		};

		function openCloseDropdownCallback(event) {
			event.stopPropagation();
			// I don't use toggle here because we still have the hover
			// controlled
			// in CSS
			var $menu = $widget.find(".idt_selectAllMenu");
			// native JS because jquery doesn't have this
			if ($menu[0].hasAttribute("style")) {
				$menu.removeAttr("style");
			}
			else {
				$menu.css("display", "block");
			}
		}
		
		function confirmSelectAllCallBack(event) {
			event.stopPropagation();
			var totalRecordCount = api.settings()[0].fnRecordsTotal();
			var msgText = 'Are you sure you want to select ' + totalRecordCount + ' item(s) including the filtered Row(s)?';

			 var dlgId = $.ibisMessaging(
					   "dialog", 
					   "warning", 
						msgText,
						{
							buttons: [{
								id: "selectAllRows",
								text: 'Select All', 
								click: _.debounce(function(event) {
		                			 if (settings.serverSide) {
		                					selectAllServerCallback(event);		    
		                				}
		                				else {
		                					 selectAllCallback(event);;
		                				}
			                     
		                			 $.ibisMessaging("close", {id: dlgId});
			                     	
								}, 1000, true)
							},
							{
								text: 'Cancel',
								click: function() {
									$.ibisMessaging("close", {id: dlgId});
								}
							}],
							modal: true,
							width: '400px',
							title: 'Confirm Select All'
						}
				);			 
			 
		}
		
		function selectAllCallback(event) {
			event.stopPropagation();
			if (shouldAllowSelectAll()) {
				api.rows(function(idx, data, node) {
					return !data.selectionDisable;
				}).select();
				
				/*check the checkbox*/
				$widget
				.removeClass("idt_selectFilter")
				.removeClass("idt_selectNone")
				.addClass("idt_selectAll");
			}
		}

		function selectAllServerCallback(event) {
			event.stopPropagation();
			if (shouldAllowSelectAll()) {
				var url = settings.idtUrl,
				idtSettings = api.settings()[0],
				totalRecordCount = api.settings()[0].fnRecordsTotal(),	
				//I had to add the order to the data, some of the serverSide tables
				//were throwing error since we were ordering on different columnIndex 
				//and in this case we are only sending the Id.
				data = {
					start: 0,
					length: totalRecordCount,
					columns: [{
						parameter: settings.idtData.primaryKey,
						data: "id"
					}],
					order:[{
						column: 0,
						dir: "asc"
					}]		
				};
				
				$widget
					.removeClass("idt_selectFilter")
					.removeClass("idt_selectNone")
					.addClass("idt_selectAll");
				
				//I'm using the ajax instead because i dont want datatable to draw all data.
				$.ajax({
					type: "GET",
					url: url,
					dataType: "json",
					data: data,
				    beforeSend: function(){
				    	$(idtSettings.nTableWrapper).find('div.dataTables_processing').show();
				    },
				    complete: function(){
				    	api.draw(false);
				    },
					success: function(json) {
						//on Success there is no need to show the processing message anymore
						$(idtSettings.nTableWrapper).find('div.dataTables_processing').hide();
						// I really wish I didn't have to modify this here
						opts.selected = json.data.reduce(function(ids, obj) {

							if ($.inArray(obj.id, opts.selectionDisabled) == -1) {
								ids.push(obj.id);
								api.row("#" + obj.id).select();
							}
							return ids;
														
						},[]);	
					}
				});

			}
		}
		
		function selectFilterServerCallback(event) {
		      event.stopPropagation();
		      if (shouldAllowSelectFiltered()) {
		        var url = settings.idtUrl;
		        var idtSettings = api.settings()[0];
		        var totalRecordCount = idtSettings.fnRecordsDisplay();
		        var columns = idtSettings.aoColumns.slice(1);
		        var data =  api.ajax.params();
		        var filterData = idtSettings.oInit.filterData;
		        var i = 0;
		        
		        var arr = mapNonNull(data.columns, function(column) {
		          return column.data === "checkbox" ? null : column;
		        });
		        
		        arr.map(function(column) {
		          column.parameter = columns[i].parameter;
		          i++;
		        });
		        
		        //In this case we need to send all the columns params
		        //in order for the search to work.
		        data.start = 0;
		        data.length = totalRecordCount;
		        data.obj = {
		            primaryKey: settings.idtData.primaryKey,
		        };
		        data.columns = arr;
		        
		        if(filterData) {
		        	$.extend(data,filterData);
		        }
		        
		        $widget
					.removeClass("idt_selectAll")
					.removeClass("idt_selectNone")
					.addClass("idt_selectFilter");
		       
		        $.ajax({
		          type: "GET",
		          url: url,
		          dataType: "json",
		          data: data,
		            beforeSend: function(){
		              $(idtSettings.nTableWrapper).find('div.dataTables_processing').show();
		              console.log(idtSettings.nTable.id)
		            },
		            complete: function(){
		              api.draw(false);
		            },
		          success: function(json) {
		            
		            $(idtSettings.nTableWrapper).find('div.dataTables_processing').hide();
		            opts.selected = json.data.reduce(function(ids, obj) {

		              if ($.inArray(obj.DT_RowId, opts.selectionDisabled) == -1) {
		                ids.push(obj.DT_RowId);
		                api.row("#" + obj.DT_RowId).select();
		              }
		              return ids;
		                            
		            },[]);            
		            
		          }
		        });
		      } 			
		}
		
		function mapNonNull(arr, cb) {
			return arr.reduce(function(accumulator, value, index, arr) {
				var result = cb.call(null, value, index, arr);
				if (result != null) {
					accumulator.push(result);
				}

				return accumulator;
			}, []);
		}

		function selectNoneCallback(event) {
			event.stopPropagation();
			if (shouldAllowSelectNone()) {
				// ugh
				opts.selected = [];
				//i have to redraw the table otherwise it wont update
				//the number of the selected Row(s) in the _infoCallBack
				api.rows().deselect().draw(false);
				
				/*check the checkbox*/
				$widget
					.removeClass("idt_selectAll")
					.removeClass("idt_selectFilter")
					.addClass("idt_selectNone");
			}
			
		}

		function selectFilteredCallback(event) {
			event.stopPropagation();
			if (shouldAllowSelectFiltered()) {
				api.rows(":not(.idtSelectionDisabled)", {
					search: 'applied'
				}).select();
				
				$widget
					.removeClass("idt_selectAll")
					.removeClass("idt_selectNone")
					.addClass("idt_selectFilter");
			}
		}

		/**
		 * Gets the base directory of this plugin. This is used by other places
		 * in this plugin to reference other needed files. This is done by
		 * finding the script tag for THIS FILE on the containing page and
		 * getting the source attribute for that tag and removing this file
		 * name.
		 * 
		 * @return base directory (.../datatablesWidget) or empty string if not
		 *         found
		 */
		function getWidgetBaseDirectory() {
			var $thisScriptTag = $('script[src*="' + thisFileName + '"]');
			if ($thisScriptTag.length) {
				// will output something like //domain.tld/datatablesWidget/
				return $thisScriptTag.attr("src").replace(thisFileName, "");
			}
			else {
				return "";
			}
		}

		// private properties here
		// jquery reference to this widget after rendering
		var $widget = null;
		// API instance of this table
		var api = new $.fn.dataTable.Api(table);
		// is the widget visible?
		var isWidgetVisible = false;
		// has the widget been initialized?
		var isInitialized = false;
		// is this table fed by server data or local data?
		var isServerData = false;

		// default configuration
		var defaults = {
			selectParams: {
				selectAllEnabled: true,
				selectNoneEnabled: true,
				selectFilteredEnabled: true,
				mode: "multi"
			}
		};

		// code to run immediately here, this is the body of the main function

		var tmpOpts = opts || {};
		var settings = $.extend({}, defaults, tmpOpts);
		settings.selectParams.mode = tmpOpts.select.style;

		// it doesn't make sense to show select all if the table isn't
		// multiselect
		if (isMultiSelect()) {
			render();
		}
		isInitialized = true;
		return this;
	};
})(jQuery, window, document);

// register to both the capital-D DataTable and little-d dataTable namespaces
$.fn.DataTable.SelectSet = $.fn.dataTable.SelectSet;
