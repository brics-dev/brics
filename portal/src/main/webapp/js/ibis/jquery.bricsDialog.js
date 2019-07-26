/**
 * Provides a replacement for the fancybox link-load feature.
 */

(function($) {
	$.bricsDialog = $.fn.bricsDialog = function(method) {

		/* Utility and Object Classes ******************************* */

		/* End Utility and Object Classes ******************************* */

		var methods = {
			// public functions go here
			init: function(options) {
				var dialogDefaults = {
					width: "auto",
					maxWidth: width(),
					maxHeight: height(),
					autoOpen: false,
					modal: true
				};

				var settings = $.extend({}, $.fn.bricsDialog.defaults, dialogDefaults, options);
				/*
				 * check that we're on an element get href, ensure there is one
				 * crete container for dialog init dialog determine type of load
				 * (ajax, iframe) set up event
				 */
				return this.each(function(index, element) {
					var $link = $(this);
					var href = $link.attr("href");

					if (typeof href !== typeof undefined && href !== false) {

						var id = getId($link);

						$("body").append('<div id="' + id + '" class="bricsDialogContainer"></div>');
						var $dialogContainer = $("#" + id);
						$dialogContainer.dialog(settings);

						$link.attr("href", "javascript:;");

						var clickCallback = null;
						if (needLoadIframe(href)) {
							clickCallback = function() {
								$dialogContainer.html('<iframe src="' + href + '"></iframe>');
								$dialogContainer.dialog("open");
							};
						}
						else {
							clickCallback = function() {
								$dialogContainer.load(href, function() {
									$dialogContainer.dialog("open");
								});
							};
						}

						$link.on("click", function() {
							clickCallback();
						});
					}
				});
			},

			initHtml: function(inputString) {
				var dialogDefaults = {
					width: "auto",
					maxWidth: width(),
					maxHeight: height(),
					modal: true,
					close: function(){
						$(this).dialog("destroy");
						$(this).html("");
					}
				};

				var settings = $.extend({}, $.fn.bricsDialog.defaults, dialogDefaults);
				/*
				 * check that we're on an element get href, ensure there is one
				 * crete container for dialog init dialog determine type of load
				 * (ajax, iframe) set up event
				 */

				// in this format, there is no this, so we need to reassign it
				// and
				// get to working on adding an element
				// options[0] is HTML to insert
				var $body = $("body");
				var id = "bricsDialogContainer_" + Date.now();
				$body.append('<div id="' + id + '" class="bricsDialogContainer"></div>');
				var $dialogContainer = $("#" + id);
				$dialogContainer.html(inputString).dialog(settings);
				return $dialogContainer;
			}


		};

		// ----------------------------------------------------------------
		// private functions go here

		/**
		 * Decides if the URL for this dialog needs to have its content loaded
		 * in an iframe or if it can be inserted directly into the dialog
		 */
		function needLoadIframe(url) {
			return !isLinkRelative(url) && (window.location.hostname != extractHostname(url) || window.location.protocol != extractProtocol(url));
		}

		function width() {
			return $(window).width() * $.fn.bricsDialog.defaults.widthPercent;
		}

		function height() {
			return $(window).height() * $.fn.bricsDialog.defaults.heightPercent;
		}

		function getId($element) {
			var eleId = $element.attr("id");
			var outputId = $("." + $.fn.bricsDialog.defaults.className).length;
			if (typeof eleId !== typeof undefined && eleId !== false) {

				outputId = eleId;
			}
			return $.fn.bricsDialog.defaults.idPrefix + outputId;
		}

		function loadAjax(url, config) {
			// TODO: finish
		}

		function loadIframe(url, config) {
			// TODO: finish
		}


		/***********************************************************************
		 * Extracts the domain name from a URL. For example
		 * https://www.youtube.com/watch?v=PFQnNFe27kU returns www.youtube.com
		 * 
		 * @see https://stackoverflow.com/questions/8498592/extract-hostname-name-from-string
		 */
		function extractHostname(url) {
			var hostname;
			// find & remove protocol (http, ftp, etc.) and get hostname

			if (url.indexOf("://") > -1) {
				hostname = url.split('/')[2];
			}
			else {
				hostname = url.split('/')[0];
			}

			// find & remove port number
			hostname = hostname.split(':')[0];
			// find & remove "?"
			hostname = hostname.split('?')[0];

			return hostname;
		}

		/***********************************************************************
		 * Extracts the protocol from a URL. If there is no protocol defined, we
		 * assume the link is relative and therefore the current page's protocol
		 * 
		 * @param url
		 *            to test
		 */
		function extractProtocol(url) {
			var index = url.indexOf("://");
			var output = window.location.protocol;
			if (index > 0) {
				output = url.substring(0, index);
			}
			return output;
		}

		/***
		 * 
		 */
		function isLinkRelative(url) {
			var index = url.indexOf("://");
			return index < 0;
		}

		// ------Method calling logic---------------------------------
		if (methods[method]) {
			return methods[method].apply(this, Array.prototype.slice.call(arguments, 1));
		}
		else if (typeof method === 'object' || !method) {
			return methods.init.apply(this, arguments);
		}
		else if (typeof method === 'string') {
			return methods.initHtml.apply(this, arguments);
		}
		else {
			$.error('Method ' + method + ' does not exist on jquery.bricsDialog');
		}
	};

	// ------defaults and callbacks---------------------------------

	$.fn.bricsDialog.defaults = {
		// configuration options go here
		widthPercent: 0.9,
		heightPercent: 0.9,
		className: "bricsDialogContainer",
		idPrefix: "bricsDialog_"
	};

})(jQuery);