/**
 * Provides the Select All, Select None, and Select Filtered widget addon to the
 * Ibis DataTable (IDT) table. Renders the control and enables its actions. Only
 * draws the control if the table is multi-select and there are no
 * configurations saying it shouldn't be rendered.
 */
/* global $, TemplateManager */
(function($) {
	var thisFileName = "idtSearchColumnPlugin.js";

	// main function here
	$.fn.IdtSearchColumn = function(table, opts) {
		// private methods here

		function getColumns() {
			var columns = {
				columns: []
			};
			// @formatter:off
			api.columns().every(
							function(index) {
								var $header = $(this.header());
								var text = $header.text();
								var columnsName = api.settings().init().columns;
								
								if ($header.find(".idtNav").length === 0 && $header.text() !== ""
												&& $header.find(".idt_selectAllSelector").length === 0) {
									columns.columns.push({
										index: this.index(),
										text: $header.text(),
										name: columnsName[index].name
									});
								}
							});
			return columns;
			// @formatter:on
		}

		/**
		 * Renders the control and registers the event click handlers
		 */
		function render(oTable) {
			var $wrapper = $(api.table().node()).parent();
			var baseUrl = getWidgetBaseDirectory();
			if (baseUrl !== "") {
				var templateUrl = baseUrl + "templates/searchColumns.hbs";
				var $searchInput = $wrapper.find('.dataTables_filter');
				$searchInput.hide();

				TemplateManager.loadNewAsync("columnSearch", templateUrl, function(template) {
					$searchInput.after(template(getColumns()));
					$widget = $wrapper.find(".idt_searchContainer");
					registerEvents();
				});
				var searchRows = getSearchRowIndices;
				if (oTable.fnSettings().oFeatures.bServerSide == true) {
					var settings = oTable.fnSettings();
					var elem = $(oTable[0]);
					$(oTable[0]).on('preXhr.dt', function(e, settings, data) {
						$widget = $wrapper.find(".idt_searchContainer");
						var searchRowIndices = searchRows($widget);
						var sear = $wrapper.find(".idt_searchInput").val().toLowerCase();
						settings.clearCache = true;
						$.extend(data.search, {
							value : sear
						});
						for (var i = 0, len = data.columns.length; i < len; i++) {
							data.columns[i].searchable = (searchRowIndices.indexOf(data.columns[i].name) >= 0);
						}
					});
				}
			}
			// else, don't try to render because you'll fail
		}

		/**
		 * Registers the event handlers including open/close the menu and all
		 * sub-menu item click handlers.
		 */
		function registerEvents() {
			$widget.find(".idt_searchColumnLabel").on("click", openCloseDropdownCallback);

			var $table = $(api.table().node());
			var callSearch = function(event) {
				search($table.parent().find(".idt_searchInput"), event);
			};

			$table.parent().find(".idt_searchInput").on('keyup', _.debounce(callSearch, 200));
			$(api.table().node()).on("idt:filter", callSearch);
			$(api.table().node()).on("destroy.dt", function(e, settings) {
				removeOldSearch($table.parent().find(".idt_searchInput"));
			});
			$widget.find('.idt_selectColumnCheckbox').on("click", callSearch);
		}

		function openCloseDropdownCallback(event) {
			event.stopPropagation();
			// I don't use toggle here because we still have the hover
			// controlled
			// in CSS
			var $menu = $widget.find(".idt_selectColumnDrop");
			// native JS because jquery doesn't have this
			if ($menu[0].hasAttribute("style")) {
				$menu.removeAttr("style");
			}
			else {
				$menu.css("display", "block");
			}
		}

		function search($element, event) {
			table.dataTableExt.search.push(function(settings, data, dataIndex) {
                if (table.attr("id") != settings.nTable.getAttribute('id')) {
                    return true;
                }
                // get all selected checkboxes
				for (var i = 0, len = data.length; i < len; i++) {
					var sear = $element.val().toLowerCase();
					if ($widget.find('.idt_selectColumnCheckbox[value="' + settings.aoColumns[i].name + '"]:checked').length > 0) {
						if (~data[i].toLowerCase().indexOf(sear)) return true;
					}
				}
				return false;
			});
			
			table.fnDraw();
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

		function getSearchRowIndices($widget) {
			var output = [];
			$widget.find('.idt_selectColumnCheckbox').each(function(index, value) {
				if ($(this).is(":checked")) {
					output.push($(this).val());
				}
			});
			return output;
		}

		// private properties here
		// jquery reference to this widget after rendering
		var $widget = null;
		// API instance of this table
		var api = new $.fn.dataTable.Api(table);
		// is this table fed by server data or local data?
		var isServerData = false;

		// default configuration
		var defaults = {

		};

		// code to run immediately here, this is the body of the main function

		var tmpOpts = opts || {};
		var settings = $.extend(defaults, tmpOpts);

		// it doesn't make sense to show select all if the table isn't
		// multiselect
		render(this);
		return this;
	}
})(jQuery, window, document);
