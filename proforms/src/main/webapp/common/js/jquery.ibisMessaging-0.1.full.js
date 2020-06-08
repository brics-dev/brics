/**
 * Messaging front-end for showing messages to the user. This plug-in combines
 * flash messages (temporary messages), popup messages, and persistent
 * top-of-the page messages. Dependencies: jQuery jQuery UI jquery.cookie.js
 * from https://github.com/carhartl/jquery-cookie for cookie function JSON-2
 * javascript package
 * 
 * @author Joshua Park
 */
(function($) {

	$.ibisMessaging = $.fn.ibisMessaging = function(method) {
		/* Utility and Object Classes ******************************* */

		/* End Utility and Object Classes ******************************* */

		var methods = {
			// public functions go here
			init: function(options) {
				var settings = $.extend({}, $.fn.ibisMessaging.defaults, options);

				// we should be referencing the target for primary messages here
				initPrimaryMessaging($(settings.primaryContainer));
				initFlashMessaging($(settings.flashContainer));
				initDialogMessaging($(settings.dialogContainer));

				// are we set on an actual element?
				if (this !== window.jQuery && this.length > 0) {
					// each child is an html(xml) encoded primary message
					this.children().each(function() {
						initServerMessage($(this));
					});
					// remove this element to get rid of any spacing
					this.remove();
				}

				initStoredMessages();

				return this;
			},

			/**
			 * Displays a new message corresponding to the configuration given
			 * 
			 * @param type
			 *            String type of message to display
			 * @param config
			 *            configuration for that message
			 * @return the ID (HTML/CSS ID) of the opened message
			 * @throws error
			 *             on unknown type
			 */
			show: function(type, config) {
				try {
					var messager = chooseType(type);
					config.id = calculateId(config);
					messager(config);
					return config.id;
				}
				catch (e) {
					throw e;
				}
			},

			/**
			 * Closes the specified popup
			 * 
			 * @param id
			 */
			close: function(config) {
				try {
					// we have two cases here, where we want to close all of
					// a particular type (primary, dialog, flash),
					// a particular message (by ID)
					if (typeof config !== "undefined") {
						if (typeof config.id !== "undefined" && config.id !== null) {
							// close by ID
							var messager = chooseType(getMessageType($("#" + config.id)));
							messager("close", config);
						}
						else if (typeof config.type !== "undefined" && config.type !== null) {
							// close by type
							var messager = chooseType(config.type);
							messager("close", config);
						}
						else {
							throw new Error("cannot close message: cannot determine messages to close");
						}
					}
					else {
						throw new Error("cannot close message: invalid configuration");
					}
				}
				catch (e) {
					throw e;
				}
			},

			/**
			 * Stores the given type of message with the given config into a
			 * semi-persistent storage such that the message will be displayed
			 * next time a page loads messages. This is particularly useful for
			 * messages that would otherwise appear during a redirect - such
			 * that the user wouldn't see it.
			 * 
			 * @param type
			 *            string primary|dialog|flash
			 * @param config
			 *            config object the same as normal configuration of
			 *            messages
			 */
			store: function(type, config) {
				if (typeof $.cookie !== "undefined") {
					var original = $.ibisMessaging("retrieve");
					var newMessage = {
						type: type,
						config: config
					};
					original.push(newMessage);
					$.cookie("ibisMessaging", JSON.stringify(original), {
						expires: 1
					});
				}
			},

			/**
			 * Retrieves all messages from the cookie storage as a javascript
			 * array.
			 * 
			 * @returns array list of messages stored in the cookie
			 */
			retrieve: function() {
				if (typeof $.cookie !== "undefined") {
					var original = $.cookie("ibisMessaging");
					// in older versions of $.cookie, $.removeCookie doesn't
					// exist
					// so, use the older remove in that case
					if (typeof $.removeCookie !== "undefined") {
						$.removeCookie('ibisMessaging');
					}
					else {
						$.cookie("ibisMessaging", null);
					}
					if (original !== null && original !== undefined) {
						original = JSON.parse(original);
					}
					else {
						original = [];
					}
					return original;
				}
				else {
					return [];
				}
			},

			/**
			 * Draws a primary message on the page using default settings and
			 * specified level and message.
			 * 
			 * @param level
			 *            the level of the message
			 * @param message
			 *            the text message
			 * @param config
			 *            any additional configuration settings
			 * @return the ID of the message
			 */
			primary: function(level, message, config) {
				if (typeof config === "undefined") {
					config = {};
				}

				config.level = level;
				config.message = message;

				return $.ibisMessaging("show", "primary", config);
			},

			/**
			 * Draws a flash message on the page using default settings and
			 * specified level and message.
			 * 
			 * @param level
			 *            the level of the message
			 * @param message
			 *            the text message
			 * @param config
			 *            any additional configuration settings
			 * @return the ID of the message
			 */
			flash: function(level, message, config) {
				if (typeof config === "undefined") {
					config = {};
				}

				config.level = level;
				config.message = message;

				return $.ibisMessaging("show", "flash", config);
			},

			/**
			 * Draws a dialog message on the page using default settings and
			 * specified level and message.
			 * 
			 * @param level
			 *            the level of the message
			 * @param message
			 *            the text message
			 * @param config
			 *            any additional configuration settings
			 * @return the ID of the message
			 */
			dialog: function(level, message, config) {
				if (typeof config === "undefined") {
					config = {};
				}
				config.level = level;
				config.message = message;

				return $.ibisMessaging("show", "dialog", config);
			}
		};

		// ----------------------------------------------------------------
		// private functions go here

		/**
		 * Gets the jquery elements containing messages
		 * 
		 * @return jquery list of messages
		 */
		function getMessages() {
			return $(".ibisMessaging-message");
		}

		/**
		 * Calculates the HTML ID of the new message to display
		 * 
		 * @return the new ID
		 */
		function calculateId(config) {
			if (typeof config.id !== "undefined") {
				return config.id;
			}
			else {
				return "message_" + String(getMessages().length + 1);
			}
		}

		/**
		 * Initializes the messages stored in the cookie (if any)
		 */
		function initStoredMessages() {
			var messages = $.ibisMessaging("retrieve");
			for (var i = 0; i < messages.length; i++) {
				$.ibisMessaging("show", messages[i].type, messages[i].config);
			}
		}

		/**
		 * Takes a single server message and creates a primary message out of it
		 * 
		 * @param $element
		 *            jquery reference to the page element containing the
		 *            message and attributes
		 */
		function initServerMessage($element) {
			try {
				$.ibisMessaging("primary", $element.attr("class"), $element.html());
			}
			catch (e) {
				alert("The messaging service failed to display the message:\n" + $element.html()
								+ "\nThis will likely result in future errors");
			}
			$element.remove();
		}

		/**
		 * Initialize the primary messaging subplugin
		 * 
		 * @param $container
		 *            jquery reference to the primary messaging container
		 */
		function initPrimaryMessaging($container) {
			$.primaryMessage("setup", {
				container: $container
			});
		}

		/**
		 * Initialize the flash messaging subplugin
		 * 
		 * @param $container
		 *            jquery reference to the flash messaging container
		 */
		function initFlashMessaging($container) {
			$.flashAlert("setup", {
				container: $container
			});
		}

		/**
		 * Initialize the dialog messaging subplugin
		 * 
		 * @param $container
		 *            jquery reference to the dialog messaging container
		 */
		function initDialogMessaging($container) {
			$.dialogMessage("setup", {
				container: $container
			});
		}

		/**
		 * Chooses which plugin to use based on the "type" parameter.
		 * 
		 * @param type
		 *            String type of the message
		 * @return function the jquery plugin the type refers to
		 * @throws error
		 *             upon bad type
		 */
		function chooseType(type) {
			try {
				if (type === "primary") {
					return $.primaryMessage;
				}
				else if (type === "flash") {
					return $.flashAlert;
				}
				else if (type === "dialog") {
					return $.dialogMessage;
				}
				else {
					throw new Error("The Ibis Messaging type " + type + " does not exist");
				}
			}
			catch (e) {
				throw e;
			}
		}

		/**
		 * Determines the element's messaging type based on its class
		 * 
		 * @param $element
		 *            jquery reference to the element to check
		 * @return string type name
		 * @throws error
		 *             when cannot determine type
		 */
		function getMessageType($element) {
			if ($element.hasClass("ibisMessaging-primary")) {
				return "primary";
			}
			else if ($element.hasClass("ibisMessaging-flash")) {
				return "flash";
			}
			else if ($element.hasClass("ibisMessaging-dialog")) {
				return "dialog";
			}
			else {
				throw new Error("cannot determine message's type");
			}
		}

		// ------Method calling logic---------------------------------
		if (methods[method]) {
			return methods[method].apply(this, Array.prototype.slice.call(arguments, 1));
		}
		else if (typeof method === 'object' || !method) {
			return methods.init.apply(this, arguments);
		}
		else {
			$.error('Method ' + method + ' does not exist on jquery.ibisMessaging');
		}
	};

	// ------defaults and callbacks---------------------------------
	$.fn.ibisMessaging.defaults = {
		// configuration options go here
		primaryContainer: "body",
		flashContainer: "body",
		dialogContainer: "body"
	};

	// -------------------------------------------------------------------

	/**
	 * Messaging front-end for showing messages to the user. This plug-in
	 * combines flash messages (temporary messages), popup messages, and
	 * persistent top-of-the page messages.
	 */
	$.flashAlert = $.fn.flashAlert = function(method) {

		/* Utility and Object Classes ******************************* */

		/* End Utility and Object Classes ******************************* */

		var methods = {
			// public functions go here
			setup: function(config) {
				var settings = $.extend({}, $.fn.flashAlert.defaults, config);
				var $container = $("body");
				if (!settings.container && settings.container.length > 0) {
					$container = settings.container;
				}
				$container.addClass("ibisMessaging-flashContainer");

				initRepositionAction();
			},

			init: function(options) {
				var settings = $.extend({}, $.fn.flashAlert.defaults, options);
				// "this" is the referenced element

				$.flashAlert("open", settings);
			},

			close: function(config) {
				var $element = $();
				if (typeof config !== "undefined" && typeof config.id !== "undefined") {
					// don't close if hideDelay is <0
					if (typeof config.hideDelay === "undefined" || config.hideDelay > 0) {
						$element = $("#" + config.id);
					}
				}
				else {
					$element = $(".ibisMessaging-flash");
				}

				$element.each(function() {
					$(this).fadeOut("fast", function() {
						if (typeof config.onClose === "function") {
							config.onClose($(this));
						}
						$(".ibisMessaging-flashContainer").trigger("flashClosed");
						destroy($(this));
					});
				});
			},

			open: function(config) {
				var $message = render(config);
				setTimeout(function() {
					$.flashAlert("close", config);
				}, config.hideDelay);
				config.onShow($message);
			}
		};

		// ----------------------------------------------------------------
		// private functions go here

		function initRepositionAction() {
			$(".ibisMessaging-flashContainer").on("flashClosed", function() {
				$(this).children(".ibisMessaging-flash").each(function() {
					var $msg = $(this);
					var recalcedRight = $msg.css("right").replace("px", "") - $msg.width() - 10;
					$(this).css("right", String(recalcedRight) + "px");
				});
			});
		}

		/**
		 * Renders the message onto the page.
		 * 
		 * @return jquery element being rendered
		 */
		function render(config) {
			var html = compileHtml(config);
			// we need to figure out a way to handle multiple of these open at
			// once
			var $container = (typeof config.container === "undefined") ? $(".ibisMessaging-flashContainer")
							: $(config.container);
			$container.show();
			var $otherMessages = $container.children(".ibisMessaging-flash");

			// for flash messages, we can go ahead and add the html and worry
			// about siblings later
			$container.prepend(html);
			var $element = $("#" + config.id);
			if ($otherMessages.length > 0) {
				// calculate the offset to the left based on the number of flash
				// messages already
				// visible and the width of each (plus some padding)
				var rightPosition = 10 + (($element.width() + 25) * $otherMessages.length);
				$element.css("right", String(rightPosition) + "px");
			}

			return $element.fadeIn("fast");
		}

		function compileHtml(config) {
			var className = determineClass(config);
			var html = '<div style="display: none" class="ibisMessaging-message ibisMessaging-flash ' + className
							+ '" id="' + config.id + '">';
			html += config.message;
			html += '</div>';
			return html;
		}

		/**
		 * Destroys the message from the page. Should be called after all close
		 * events.
		 */
		function destroy($elem) {
			$elem.remove();
		}

		/**
		 * Decides the css class for the message
		 * 
		 * @return string class name
		 */
		function determineClass(config) {
			if (config.level === "error") {
				return "ibisMessaging-error";
			}
			else if (config.level === "warning") {
				return "ibisMessaging-warning";
			}
			else if (config.level === "success") {
				return "ibisMessaging-success";
			}
			else {
				return "ibisMessaging-info";
			}
		}

		// ------Method calling logic---------------------------------
		if (methods[method]) {
			return methods[method].apply(this, Array.prototype.slice.call(arguments, 1));
		}
		else if (typeof method === 'object' || !method) {
			return methods.init.apply(this, arguments);
		}
		else {
			$.error('Method ' + method + ' does not exist on jquery.flashAlert');
		}

	};

	// ------defaults and callbacks---------------------------------
	$.fn.flashAlert.defaults = {
		// configuration options go here
		id: "",
		container: ".ibisMessaging-flashContainer",
		level: "info",
		message: "",
		hideDelay: 3000,
		buttons: [],
		modal: false,
		title: "",

		// callbacks go here
		// onMoveFinish : function($moved, $target) {},
		onShow: function($message) {
		},
		onClose: function($message) {
		}
	};

	// -------------------------------------------------------------------

	/**
	 * Messaging component for displaying "primary" messages - the standard
	 * message display as used throughout the system currently. This displays a
	 * static message that will live on the page until the page is refreshed or
	 * the user goes to another page.
	 */
	$.primaryMessage = $.fn.primaryMessage = function(method) {
		var settings = null;

		/* Utility and Object Classes ******************************* */

		/* End Utility and Object Classes ******************************* */

		var methods = {
			setup: function(config) {
				var settings = $.extend({}, $.fn.primaryMessage.defaults, config);
				var $container = $("body");
				if (settings.container && settings.container.length > 0) {
					$container = settings.container;
				}
				$container.addClass("ibisMessaging-primaryContainer");
			},

			// public functions go here
			init: function(options) {
				settings = $.extend({}, $.fn.primaryMessage.defaults, options);
				// "this" is the referenced element

				$.primaryMessage("open", settings);
			},

			close: function(config) {
				var $element = $();
				if (typeof config !== "undefined" && typeof config.id !== "undefined") {
					$element = $("#" + config.id + ".ibisMessaging-primary");
				}
				else {
					$element = $(".ibisMessaging-primary");
				}
				$element.each(function() {
					destroy($(this));
				});
			},

			open: function(config) {
				render(config);
				$('div[tabindex="-1"]').removeAttr("tabindex");
				var $element = $("#" + config.id);
				$element.attr("tabindex", "-1");
				$element.focus();
				if (!isScrolledIntoView($element)) {
					$("body").scrollTop($element.offset().top);
				}
			}
		};

		// ----------------------------------------------------------------
		// private functions go here

		/**
		 * Renders the message onto the page.
		 * 
		 * @param config
		 *            the configuration options
		 */
		function render(config) {
			if (typeof config.container === "undefined") { throw new Error(
							"container for primary messages was not set in the configuration"); }

			var html = compileHtml(config);

			// if there are other primary messages within this container,
			// append this one after them
			var $container = $(config.container);
			// just in case
			$container.show();
			var $otherMessages = $container.children(".ibisMessaging-primary");
			if ($otherMessages.length > 0) {
				$otherMessages.last().after(html);
			}
			else {
				$container.prepend(html);
			}
		}

		/**
		 * Check if element is visible within the scrolled window
		 * 
		 * @param $elem
		 *            jquery element to check
		 */
		function isScrolledIntoView($elem) {
			var docViewTop = $(window).scrollTop();
			var docViewBottom = docViewTop + $(window).height();

			var elemTop = $elem.offset().top;
			var elemBottom = elemTop + $elem.height();

			return ((elemBottom >= docViewTop) && (elemTop <= docViewBottom) && (elemBottom <= docViewBottom) && (elemTop >= docViewTop));
		}

		/**
		 * Compiles the HTML for the new message
		 * 
		 * @return html of the new message
		 */
		function compileHtml(config) {
			var className = determineClass(config);
			var html = '<div class="ibisMessaging-message ibisMessaging-primary ' + className + '" id="' + config.id
							+ '">';
			html += config.message;
			html += '</div>';
			return html;
		}

		/**
		 * Destroys the message from the page. Should be called after all close
		 * events.
		 */
		function destroy($elem) {
			$elem.remove();
		}

		/**
		 * Decides the css class for the message
		 * 
		 * @return string class name
		 */
		function determineClass(config) {
			if (config.level === "error") {
				return "ibisMessaging-error";
			}
			else if (config.level === "warning") {
				return "ibisMessaging-warning";
			}
			else if (config.level === "success") {
				return "ibisMessaging-success";
			}
			else {
				return "ibisMessaging-info";
			}
		}

		// ------Method calling logic---------------------------------
		if (methods[method]) {
			return methods[method].apply(this, Array.prototype.slice.call(arguments, 1));
		}
		else if (typeof method === 'object' || !method) {
			return methods.init.apply(this, arguments);
		}
		else {
			$.error('Method ' + method + ' does not exist on jquery.primaryMessage');
		}

	};

	// ------defaults and callbacks---------------------------------
	$.fn.primaryMessage.defaults = {
		// configuration options go here
		container: ".ibisMessaging-primaryContainer",
		id: "",
		level: "info",
		message: "",
		hideDelay: 0,
		buttons: [],
		modal: false,
		title: "",

		// callbacks go here
		// onMoveFinish : function($moved, $target) {},
		onShow: function($message) {
		},
		onClose: function($message) {
		}
	};

	/**
	 * Messaging component for displaying "primary" messages - the standard
	 * message display as used throughout the system currently. This displays a
	 * static message that will live on the page until the page is refreshed or
	 * the user goes to another page.
	 */
	$.dialogMessage = $.fn.dialogMessage = function(method) {
		var settings = null;

		/* Utility and Object Classes ******************************* */

		/* End Utility and Object Classes ******************************* */

		var methods = {
			setup: function(config) {
				var settings = $.extend({}, $.fn.dialogMessage.defaults, config);
				var $container = $("body");
				if (!settings.container && settings.container.length > 0) {
					$container = settings.container;
				}
				$container.addClass("ibisMessaging-dialogContainer");
			},

			// public functions go here
			init: function(options) {
				settings = $.extend({}, $.fn.dialogMessage.defaults, options);
				return $.dialogMessage("open", settings);
			},

			close: function(config) {
				var $element = $();
				if (typeof config !== "undefined" && typeof config.id !== "undefined") {
					$element = $("#" + config.id + ".ibisMessaging-dialog");
				}
				else {
					$element = $(".ibisMessaging-dialog");
				}

				$element.each(function() {
					var $this = $(this);
					if (typeof $this.dialog("instance") !== "undefined") {
						$this.dialog("close");
						destroy($this);
					}
				});
			},

			open: function(config) {
				return render(config);
			}
		};

		// ----------------------------------------------------------------
		// private functions go here

		/**
		 * Renders the message onto the page.
		 * 
		 * @param config
		 *            the configuration options
		 */
		function render(config) {
			var html = compileHtml(config);
			// config.dialogClass = determineClass(config);

			// we don't care if there are other messages open
			var $container = (typeof config.container === "undefined") ? $(".ibisMessaging-dialogContainer")
							: $(config.container);
			$container.show();
			$container.append(html);
			var $element = $("#" + config.id);
			return $element.dialog(config);
		}

		/**
		 * Compiles the HTML for the new message
		 */
		function compileHtml(config) {
			var className = determineClass(config);
			var html = '<div style="display:none" class="ibisMessaging-message ibisMessaging-dialog" id="' + config.id
							+ '">';
			html += '<div class="' + className + '">';
			html += config.message;
			html += '</div>';
			html += '</div>';
			return html;
		}

		/**
		 * Destroys the message from the page. Should be called after all close
		 * events.
		 */
		function destroy($elem) {
			$elem.remove();
		}

		/**
		 * Decides the css class for the message
		 * 
		 * @return string class name
		 */
		function determineClass(config) {
			if (config.level === "error") {
				return "ibisMessaging-error";
			}
			else if (config.level === "warning") {
				return "ibisMessaging-warning";
			}
			else if (config.level === "success") {
				return "ibisMessaging-success";
			}
			else {
				return "ibisMessaging-info";
			}
		}

		// --------------------------------------------------------------
		// Method calling logic
		if (methods[method]) {
			return methods[method].apply(this, Array.prototype.slice.call(arguments, 1));
		}
		else if (typeof method === 'object' || !method) {
			return methods.init.apply(this, arguments);
		}
		else {
			$.error('Method ' + method + ' does not exist on jquery.dialogMessage');
		}

	};

	// -------------------------------------------------------------------
	// defaults and callbacks go here
	$.fn.dialogMessage.defaults = {
		// configuration options go here
		container: ".ibisMessaging-dialogContainer",
		id: "",
		level: "info",
		message: "",
		hideDelay: 0,
		buttons: [{
			text: "OK",
			click: function() {
				$(this).dialog("close");
			}
		}],
		modal: false,
		title: "",

		// callbacks go here
		// onMoveFinish : function($moved, $target) {},
		onShow: function($message) {
		},
		onClose: function($message) {
		}
	};
})(jQuery);