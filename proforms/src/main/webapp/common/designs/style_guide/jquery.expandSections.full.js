/**
 * Enables the toggling open/closed of 
 * @author Joshua Park
 */
(function( $ ){ 
	$.fn.expandSection = function(method) {
	
		/* Utility and Object Classes ******************************* */

		/* End Utility and Object Classes ******************************* */
		
		var methods = {
			/**
			 * Toggles the specified section open or closed (whichever is the
			 * reverse of its current state).
			 */
			toggle : function() {
				if (this.expandSection("isOpen")) {
					this.expandSection("close");
				}
				else {
					this.expandSection("open");
				}
			},
				
			/**
			 * Open the section (either level one or level two)
			 */
			open : function() {
				if (this.expandSection("isInit")) {
					var divRef = getSectionRef(this);
					this.removeClass("collapsed").addClass("expanded");
					divRef.show();
					$(this).find("span.expandCollapseController").text("[-] ");
					$(this).trigger("sectionExpand");
				}
			},
			
			/**
			 * Close the section (either level one or level two)
			 * 
			 * @param config
			 */
			close : function() {
				if (this.expandSection("isInit")) {
					var divRef = getSectionRef(this);
					this.removeClass("expanded").addClass("collapsed");
					divRef.hide();
					$(this).find("span.expandCollapseController").text("[+] ");
					$(this).trigger("sectionCollapse");
				}
			},
			
			/**
			 * Checks if the referenced element is open or closed
			 * 
			 * @return boolean true if the section is open, otherwise false
			 */
			isOpen : function() {
				var ret = this.hasClass("expanded");
				return ret;
			},
			
			/**
			 * Checks that the given element has been initialized as an
			 * expandable section.
			 * 
			 * @returns boolean true if it has been initialized, otherwise false
			 */
			isInit : function() {
				return this.hasClass('toggleable') 
					&& (this.hasClass("toggleable-l1") || this.hasClass("toggleable-l2"));
			},
			
			/**
			 * Initialize all headings, as needed.
			 * 
			 * @param options any user-specified options
			 */
			init : function(options) {
				var settings = $.extend({}, $.fn.expandSection.defaults, options);
				this.each(function() {
					var $this = $(this);
					if (!$this.hasClass("toggleable")) {
						$this.addClass("toggleable");
					}
					
					if ($(this).is(settings.levelOneTag)) {
						initLevelOne($this, settings);
					}
					else {
						initLevelTwo($this, settings);
					}
					initClick($this);
				});
				return this;
			}
		};
		
		// ----------------------------------------------------------------
		// private functions go here
		
		/**
		 * If the passed element is the link or the header, get a reference
		 * to the section div
		 */
		function getSectionRef($element) {
			var divRef = $element.parents(".toggleable").next("div, p");
			if (!$element.is("a")) { 
				divRef = $element.next("div, p");
			}
			return divRef;
		}
		
		/**
		 * Initialize level one headers
		 */
		function initLevelOne($element, settings) {
			$element.addClass("toggleable-l1");
			if ($element.hasClass("collapsed")) {
				$element.next().hide();
			}
			else {
				$element.next().show();
				if (!$element.hasClass("expanded")) {
					$element.addClass("expanded");
				}
			}
			
			$element.next().addClass("toggleableOne");
			if ($element.expandSection("isOpen")) {
				// show the "[-]" string
				$element.html($element.html().trim());
				$element.prepend('<span class="expandCollapseController">[-] </span>');
				$element.html('<a class="toggleable" href="javascript:;">' + $element.html() + '</a>');
			}
			else {
				// show the "[+]" string
				$element.html($element.html().trim());
				$element.prepend('<span class="expandCollapseController">[+] </span>');
				$element.html('<a class="toggleable" href="javascript:;">' + $element.html() + '</a>');
				$element.expandSection("close");
			}
			$element.css("cursor","pointer");
		}
		
		/**
		 * Initialize level two headers
		 */
		function initLevelTwo($element, settings) {
			$element.addClass("toggleable-l2");
			if ($element.hasClass("collapsed")) {
				$element.next().hide();
			}
			else {
				$element.next().show();
				if (!$element.hasClass("expanded")) {
					$element.addClass("expanded");
				}
			}
			
			$element.next().addClass("toggleableTwo");
			if ($element.expandSection("isOpen")) {
				// show the "[-]" string
				$element.html($element.html().trim());
				$element.prepend('<span class="expandCollapseController">[-] </span>');
				$element.html('<a class="toggleable" href="javascript:;">' + $element.html() + '</a>');
			}
			else {
				// show the "[+]" string
				$element.html($element.html().trim());
				$element.prepend('<span class="expandCollapseController">[+] </span>');
				$element.html('<a class="toggleable" href="javascript:;">' + $element.html() + '</a>');
			}
			$element.css("cursor","pointer");
		}
		
		function initClick($element) {
			$element.on("click", function(event) {
				event.stopPropagation();
				$element.expandSection("toggle");
			});
		}

		// ------Method calling logic---------------------------------
		if ( methods[method] ) {
			return methods[ method ].apply( this, Array.prototype.slice.call( arguments, 1 ));
		} else if ( typeof method === 'object' || ! method ) {
			return methods.init.apply( this, arguments );
		} else {
			$.error( 'Method ' +  method + ' does not exist on jquery.expandSection' );
		}
	};
	
	// ------defaults and callbacks---------------------------------
	
	$.fn.expandSection.defaults = {
		// configuration options go here
		levelOneTag : "h2",
		levelTwoTag : "h3",
		onOpen : function($header) {},
		onClose : function($header) {}
	};
})( jQuery ); 