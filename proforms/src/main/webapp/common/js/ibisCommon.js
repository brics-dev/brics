/*
 * Global variables
 */
var dataTables = {};
var LeftNav = {
	collapsed: false,
	selfRef: null,
	mainContentRef: null,
	heightReferenceObj: null,
	handleRef: null,
	onCollapse: function() {
	},
	onExpand: function() {
	},
	onHandleAppear: function() {
	},
	onHandleDisappear: function() {
	},
	init: function(jqueryReference, handleRef) {
		this.selfRef = jqueryReference;
		this.handleRef = handleRef;

		if (LeftNav.handleRef == null) {
			LeftNav.handleRef = $("#leftNavHandle");
		}
		if (LeftNav.selfRef == null) {
			LeftNav.selfRef = $("#leftNav");
		}
		if (LeftNav.mainContentRef == null) {
			LeftNav.mainContentRef = $("#mainContent");
		}
		if (LeftNav.heightReferenceObj == null) {
			LeftNav.heightReferenceObj = $("#leftColumn");
		}

		if (LeftNav.heightReferenceObj.length > 0) {
			LeftNav.handleRef.height(LeftNav.heightReferenceObj.height());
		}
		else {
			LeftNav.handleRef.height(400);
		}

		LeftNav.handleRef.hover(function() {
			LeftNav._unhide();
		}, function() {
			LeftNav._hide();
		});

		LeftNav.handleRef.click(function() {
			if (LeftNav.selfRef.is(":visible")) {
				LeftNav._collapse();
			}
			else {
				// only expand if the left nav is not empty
				if (LeftNav.selfRef.html().trim()) {
					LeftNav._expand();
				}
			}
		});

		if (LeftNav.collapsed) {
			LeftNav.handleRef.removeClass("hide").addClass("close");
		}
		if (LeftNav.handleRef.hasClass("close")) {
			LeftNav.handleRef.click();
		}
	},
	_collapse: function() {
		LeftNav.selfRef.toggle();
		LeftNav.heightReferenceObj.addClass("collapsed");
		LeftNav.mainContentRef.addClass("expanded");
		LeftNav.handleRef.removeClass("close").addClass("open");
		$(window).trigger("resize");
	},
	_expand: function() {
		LeftNav.selfRef.toggle();
		LeftNav.heightReferenceObj.removeClass("collapsed");
		LeftNav.handleRef.removeClass("open").addClass("hide");
		LeftNav.mainContentRef.removeClass("expanded");
		$(window).trigger("resize");
	},
	_unhide: function() {
		if (!LeftNav.handleRef.hasClass("open")) {
			LeftNav.handleRef.removeClass("hide").addClass("close");
		}
	},
	_hide: function() {
		if (!LeftNav.handleRef.hasClass("open")) {
			LeftNav.handleRef.removeClass("close").addClass("hide");
		}
	}
};

/*
 * Standardized functions
 */

/**
 * Handles all activity related to the datepickers and datetimepickers.
 */
var DatePickerController = {
	datePickers: [],
	dateTimePickers: [],

	/**
	 * All date fields should have a onclick dateinput to allow for standard JS
	 */
	initDatePickers: function() {
		$('input[type="date"], input[type="text"].dateField, textarea.dateField').each(function() {
			var tmpPicker = $(this).datepicker({
				dateFormat: "yy-mm-dd", // this corresponds to ex: 2012-4-30
				changeYear: true,
				yearRange: "1900:+10"
			});
			DatePickerController.datePickers.push(tmpPicker);
		});
		$('input[type="date"], input[type="text"].dateField').attr("placeholder", "Format: YYYY-MM-DD");
	},

	// TODO: combine these initialization objects into one object and use in
	// both date and datetime

	/**
	 * All datetime fields should be rendered as datetimepickers if the
	 * datetimepicker function exists, otherwise render as datepicker.
	 */
	initDateTimePickers: function() {
		if (!$.fn.datetimepicker) {
			this.translateDatePickers();
		}
		else {
			var thisDate = new Date();
			var tenYearsFuture = new Date(Number(thisDate.getFullYear()) + 10, thisDate.getMonth(), thisDate.getDate());
			$('input[type="datetime"], input[type="text"].dateTimeField, textarea.dateTimeField').each(function() {
				var tmpPicker = $(this).datetimepicker({
					dateFormat: "yy-mm-dd", // this corresponds to ex: 2012-4-30
					changeYear: true,
					yearRange: "1900:+10"
				});
				DatePickerController.dateTimePickers.push(tmpPicker);
			});
		}
		$('input[type="datetime"], input[type="text"].dateTimeField').attr("placeholder", "Format: YYYY-MM-DD HH:MM");

	},

	initAllPickers: function() {
		this.initDateTimePickers();
		this.initDatePickers();
	},

	/**
	 * Change dateTime pickers over to datepickers. This must happen BEFORE the
	 * datepickers are initialized. ONLY called in the case of datetimepicker
	 * function DNE
	 */
	translateDatePickers: function() {
		$('input[type="datetime"]').each(function() {
			$(this).attr("type", "date");
		});
		$('input[type="text"].dateTimeField').each(function() {
			$(this).removeClass("dateTimeField");
			$(this).addClass("dateField");
		});
	},

	getAll: function() {
		return $.merge($.merge([], this.datePickers), this.dateTimePickers);
	},

	/**
	 * called by the window resize function. Handles window resizing and
	 * repositioning.
	 */
	windowResizeHide: function() {
		var pickers = this.getAll();
		for (var i = 0; i < pickers.length; i++) {
			pickers[i].datepicker('hide');
			pickers[i].blur();
		}
	}
};

/**
 * Validation class. Allows javascript to validate input as needed
 */
var Validator = {
	isNumeric: function(n) {
		return !isNaN(parseFloat(n)) && isFinite(n);
	}
};

// add "trim" functionality to String in IE8ish
if (typeof String.prototype.trim !== 'function') {
	String.prototype.trim = function() {
		return this.replace(/^\s+|\s+$/g, '');
	};
}

/**
 * Make the labels look a little nicer This takes all labels inside formrows on
 * the page and standardizes them to be all the same width. It adjusts for
 * required inputs and hidden elements. It will look at the label to see its
 * initial width. If the width is zero or "auto", it will try to calculate the
 * width based on the font size and number of characters in the label.
 */
var FormRowCalculator = {
	/**
	 * Initializes the formrow calculator. Processes the elements on the page to
	 * size things correctly. Also adds required field stars.
	 */
	init: function() {
		this.addRequiredStars();
		this.sizeLabels();
		this.sizeInputs();
		$("#mainContent").on("sectionExpand", "h3.toggleable", function() {
			FormRowCalculator.sizeLabels();
			FormRowCalculator.sizeInputs();
		});
	},

	/**
	 * Adds required stars to fields that are marked as required
	 */
	addRequiredStars: function() {
		$(".formrow_1 .requiredStar, .formrow_2 .requiredStar").remove();
		$('.formrow_1 .requiredInput, .formrow_2 .requiredInput').append('<span class="requiredStar">*</span>');
	},

	/**
	 * Sizes the labels
	 */
	sizeLabels: function() {
		this.sizeFormrowOne();
		this.sizeFormrowTwo();
	},

	/**
	 * Sizes formrowone rows. This finds the minimum width that allows all
	 * labels to fit correctly.
	 */
	sizeFormrowOne: function() {
		try {
			var fullMax = 0, fullMin = 0, maxWidth = 0;
			$(".formrow_1 > label").each(function() {
				fullMax = 295;
				fullMin = 147;

				var thisWidth = $(this).width();
				if (thisWidth == 0 || thisWidth == "auto") {
					var fontsize = $(this).css("font-size").replace("px", "").replace("pt", "").replace("em", "");
					thisWidth = ($(this).text().length * fontsize) * 0.75;
				}
				maxWidth = (thisWidth > maxWidth) ? thisWidth : maxWidth;
			});
			if (maxWidth > fullMax) maxWidth = fullMax;
			if (maxWidth < fullMin) maxWidth = fullMin;

			$(".formrow_1 > label").each(function() {
				var newWidth = maxWidth;
				if ($(this).hasClass("requiredInput")) {
					newWidth = newWidth + 7;
				}
				var thisWidth = $(this).width();
				if ((thisWidth == 0 || thisWidth == "auto") || (thisWidth < maxWidth)) {
					$(this).width(newWidth);
				}
			});

			$(".formrow_1").each(function() {
				if (!($(this).find('.clearboth').length > 0)) {
					$(this).append('<div class="clearboth"><!-- clear --></div>');
				}

			});
		}
		catch (e) {
			var maxWidth = 295;
			$(".formrow_1 > label").each(function() {
				var newWidth = maxWidth;
				if ($(this).hasClass("requiredInput")) {
					newWidth = newWidth + 7;
				}
				$(this).width(newWidth);
			});
		}
	},

	/**
	 * Sizes formrowtwo rows. This finds the minimum width that allows all
	 * labels to fit correctly.
	 */
	sizeFormrowTwo: function() {
		try {
			var fullMax = 0, fullMin = 0, maxWidth = 0;
			// calculates the max width of ALL labels in this formrow2
			$(".formrow_2 > label").each(function() {
				fullMax = 170;
				fullMin = 85;

				var thisWidth = $(this).width();
				if (thisWidth == 0 || thisWidth == "auto") {
					var fontsize = $(this).css("font-size").replace("px", "").replace("pt", "").replace("em", "");
					thisWidth = ($(this).text().length * fontsize) * 0.75;
				}
				maxWidth = (thisWidth > maxWidth) ? thisWidth : maxWidth;
			});
			if (maxWidth > fullMax) maxWidth = fullMax;
			if (maxWidth < fullMin) maxWidth = fullMin;

			$(".formrow_2 > label").each(function() {
				var newWidth = maxWidth;
				if ($(this).hasClass("requiredInput")) {
					newWidth = newWidth + 7;
				}
				$(this).width(newWidth);
			});

			$(".formrow_2").each(function() {
				$(this).append('<div class="clearboth"><!-- clear --></div>');
			});

			// standardize the heights in individual rows
			$(".formrow_2").each(function() {
				var thisTop = $(this).position().top;
				var next = $(this).prev(".formrow_2");
				var prev = $(this).next(".formrow_2");
				if (prev.length > 0 && prev.position().top == thisTop) {
					if (!prev.hasClass("allowWrap") && !$(this).hasClass("allowWrap")) {
						var thisHeight = $(this).height();
						var otherHeight = prev.height();
						if (thisHeight > otherHeight) {
							prev.height(thisHeight);
						}
						else if (thisHeight < otherHeight) {
							$(this).height(otherHeight);
						}
					}
				}
				if (next.length > 0 && next.position().top == thisTop) {
					if (!next.hasClass("allowWrap") && !$(this).hasClass("allowWrap")) {
						thisHeight = $(this).height();
						otherHeight = next.height();
						if (thisHeight > otherHeight) {
							next.height(thisHeight);
						}
						else if (thisHeight < otherHeight) {
							$(this).height(otherHeight);
						}
					}
				}
			});
		}
		catch (e) {
			var maxWidth = 170;
			$(".formrow_2 > label").each(function() {
				var newWidth = maxWidth;
				if ($(this).hasClass("requiredInput")) {
					newWidth = newWidth + 7;
				}
				$(this).width(newWidth);
			});
		}
	},

	sizeInputs: function() {
		// for formrow_1, get the width, subtract the label width, and set
		// the next sibling to that width
		var rowWidth = $(".formrow_1:visible").eq(0).innerWidth();
		$(".formrow_1").each(function() {
			FormRowCalculator.setInputSize($(this), rowWidth);
		});

		var rowWidth = $(".formrow_2:visible").eq(0).width();
		$(".formrow_2").each(function() {
			FormRowCalculator.setInputSize($(this), rowWidth);
		});

		// adjust for datatables inside the input block
		IbisDataTables.recalculateHeaderFooterWidths($("table.dataTable"));
	},

	/**
	 * Given a container formrow, set the width of the input half of the row to
	 * "the rest of the row width".
	 * 
	 * @param $row
	 *            the row that contains the input to resize
	 * @param containerSize
	 *            the size of the container (max total width)
	 */
	setInputSize: function($row, containerSize) {
		var $label = $row.children("label").eq(0);
		var $input = $label.next();
		if ($input.attr("type") != "radio" && $input.attr("type") != "checkbox" && $input.attr("type") != "button"
						&& !$input.hasClass("noResize") && !$input.is("button")) {
			var labelWidth = $label.outerWidth();
			if ($label.hasClass("requiredInput")) {
				labelWidth += 14;
			}
			else {
				labelWidth += 21;
			}

			// in some cases, we have elements other than label and input in
			// the row. Subtract the width of those elements from this final.
			var adjustWidth = 0;
			var $otherElements = $row.children().not($label).not($input).not(".clearboth");
			$otherElements.each(function() {
				adjustWidth += $(this).outerWidth();
			});

			$input.outerWidth(containerSize - labelWidth - adjustWidth);
		}
	},

	redraw: function() {
		this.removeSetSizes();
		this.removeRequiredStars();
		this.init();
	},

	removeRequiredStars: function() {
		// TODO: complete
	},

	removeSetSizes: function() {
		// TODO: complete
	}
};

/**
 * A collection of useful utility functions.
 */
var Utils = {
	/**
	 * Tests whether or not the given object is null or "undefined."
	 * 
	 * @param obj -
	 *            The Javascript object that will be tested for existance.
	 * @returns True if and only if the given object is not null or "undefined."
	 */
	isDefined: function(obj) {
		return (obj !== null) && (typeof obj != "undefined");
	}
};

/**
 * Given a button or set of buttons that are given the class "singleClick",
 * disable the button after being clicked once.
 */
function disableAfterSingleClick() {
	// $('input[type="button"].singleClick, button.singleClick').live("click",
	// function() {
	// $(this).prop('disabled', true);
	// var $button = $(this);
	// setTimeout(function() {reEnableButton($button);}, 2000);
	// });
	$("#mainContent").on("click", 'input[type="button"].singleClick, button.singleClick', function() {
		$(this).prop('disabled', true);
		var $button = $(this);
		setTimeout(function() {
			reEnableButton($button);
		}, 2000);
	});
}
function reEnableButton($button) {
	$button.prop('disabled', false);
}

/**
 * Hide file input fields and replace with a pretty field and icon
 */
function convertFileInputs() {
	// this is just an index for creating IDs. That's why we subtract 1
	var fileInputCount = $(".fileInput").length - 1;
	if (!(BrowserDetect.browser == "Explorer")) {
		$('input[type="file"]:visible').each(
						function() {
							var input = $(this);
							if (input.css("display") != "none") {
								input.css("display", "none");
								var inputId = "fileInput_" + fileInputCount;
								input.after('<input type="text" id="' + inputId + '" ref="' + input.attr("id")
												+ '" class="fileInput" />');
								$("#" + inputId).css("width", $(this).css("width"));
								$("#" + inputId).click(function() {
									input.click();
								}).keydown(function(e) {
									if (e.which != 9) {
										input.click();
									}
								});
								$("#" + inputId).val(input.val());
								input.change(function() {
									input.next().val(input.val());
								});
								fileInputCount++;
							}
						});
	}
}

/**
 * add the required star next to labels that have the requiredInput class
 */
function addRequiredStars() {
	$(".requiredStar").remove();
	$('.requiredInput').append('<span class="requiredStar">*</span>');
}

var HtmlInputHandler = {
	TextInput: {
		read: function($input) {
			return $input.val();
		},
		write: function($input, value) {
			$input.val(value);
		}
	},
	CheckboxInput: {
		/**
		 * returns boolean true if checked, otherwise false
		 */
		read: function($input) {
			if ($input.is(":checked")) { return true; }
			return false;
		},
		write: function($input, value) {
			if (value == false) {
				$input.prop('checked', true);
			}
			else {
				$input.prop('checked', false);
			}
		}
	},
	RadioInput: {
		// the same as checkbox, but abstracted here
		read: function($input) {
			return HtmlInputHandler.CheckboxInput.read($input);
		},
		write: function($input, value) {
			HtmlInputHandler.CheckboxInput.write($input, value);
		}
	},
	PasswordInput: {
		read: function($input) {
			return "";
		},
		write: function($input, value) {
			$input.val(value);
		}
	},
	SelectInput: {
		read: function($input) {
			// TODO: complete
		},
		write: function($input, value) {
			// TODO: complete
		}
	},
	/**
	 * Used for inputs that should NOT be accessed in this method
	 */
	NullInput: {
		read: function($input) {
			return "";
		},
		write: function($input, value) {
		}
	},

	readInput: function($input) {
		var tagname = $input.prop("tagName");
		if (tagname == "INPUT") {
			// TODO: complete
			var type = $input.prop("type");
			if (type == "text") {

			}
		}
		else if (tagname == "SELECT") {
			return HtmlInputHandler.SelectInput.read($input);
		}
		else if (tagname == "TEXTAREA") {
			return HtmlInputHandler.TextInput.read($input);
		}
		else {
			return HtmlInputHandler.NullInput.read($input);
		}
	},

	/**
	 * Sets the Data information for each input to contain its original value.
	 * This registers the input so that it can be reset back to the original
	 * value.
	 * 
	 * @param $form
	 *            limits the set of inputs to a particular form. Leave blank to
	 *            do all
	 */
	registerInputResets: function($form) {
		if (typeof $form == "undefined") {
			$form = $("form");
		}

		$form.each(function() {
			var $inputs = $(this).children("input, select, textarea");
			if ($inputs.length > 0) {

			}
		});
	}
};

$(document).ready(
				function() {
					/*
					 * Styling functionality
					 * ***************************************************************
					 */
					if (typeof $.ibisMessaging !== "undefined") {
						$("#messages").ibisMessaging({
							primaryContainer: "#messageContainer"
						});
					}

					// convert the file inputs
					convertFileInputs();
					disableAfterSingleClick();
					enableDoubleClickOnRadios();

					addRequiredStars();

					/**
					 * Build all datatables when they are finished loading. This
					 * should speed up the loading process for those tables.
					 */
					// only if the variable dataTablesDisabled is not set
					if (typeof dataTablesDisabled == "undefined" || dataTablesDisabled != true) {
						if (typeof IbisDataTables != "undefined") {
							IbisDataTables.fullBuild();
						}
					}

					/**
					 * Add the searchSubmitted field to any search form
					 */
					$(".searchContainer form, form .searchContainer").append(
									'<input type="hidden" name="searchSubmitted" value="" />');

					/**
					 * open advanced search divs if they have search data
					 * 
					 * @return true if search container should stay open,
					 *         otherwise false
					 */
					function checkForSearch() {
						if (typeof window.searchSubmitted == "undefined") {
							var searchSubmitted = "null";
						}
						return window.searchSubmitted != "null" && window.searchSubmitted != "NO";
					}
					if (checkForSearch()) {
						// if the search has been run
						$('.searchContainer').removeClass("expanded").removeClass("collapsed");
						$('.searchContainer').prev().addClass("expanded");
						$('.searchContainer').prev().removeClass("collapsed");
						$('.searchContainer').show();
					}

					// start up datepickers
					DatePickerController.initAllPickers();

					// size the labels correctly
					FormRowCalculator.init();

					/**
					 * initialize toggleable h3's
					 */
					$('h3.toggleable').each(function() {
						var $this = $(this);
						if ($this.hasClass("collapsed")) {
							$this.next().hide();
						}
						else if ($this.hasClass("expanded")) {
							$this.next().show();
						}

						if ($this.next().is(":visible")) {
							// show the "[-]" string
							$this.html($this.html().trim());
							$this.prepend('<span class="expandCollapseController">[-] </span>');
							$this.html('<a class="toggleable" href="javascript:;">' + $this.html() + '</a>');
						}
						else {
							// show the "[+]" string
							$this.html($this.html().trim());
							$this.prepend('<span class="expandCollapseController">[+] </span>');
							$this.html('<a class="toggleable" href="javascript:;">' + $this.html() + '</a>');
						}
						$this.css("cursor", "pointer");
					});

					/**
					 * original reference is to the p or div makes h3+p, h3+div
					 * expandable/collapsible
					 */
					$('h3.toggleable a, h3.toggleable').click(function(event) {
						event.stopPropagation();
						// did we click the link or the h3?
						var divRef = $(this).parents("h3").next("div, p");
						if (!$(this).is("a")) {
							divRef = $(this).next("div, p");
						}
						divRef.toggle();
						if (divRef.is(":visible")) {
							$(this).find("span.expandCollapseController").text("[-] ");
							$(this).trigger("sectionExpand");
						}
						else {
							$(this).find("span.expandCollapseController").text("[+] ");
							$(this).trigger("sectionCollapse");
						}
					});

					LeftNav.init(null, $("#leftNavHandle"));

					$(window).resize();
				});

/**
 * Resizes the main container to be the width of the window - 20 pixels for
 * padding
 */
function resizeMainContainer() {
	var newWidth = $(window).width() - 20;
	if (newWidth > 1400) {
		newWidth = 1400;
	}
	else if (newWidth < 960) {
		newWidth = 960;
	}

	var $main = $("#main");

	var newHeight = $(window).height() - $("#header").height() - $("#footerDiv").height()
					- parseInt($main.css('padding-bottom'));

	$("#main, #footer .content").width(newWidth);
	$("#navigation, #header .content").width(newWidth + 2); // modified for the
	// borders
	if ($main.height() < newHeight) {
		$main.height(newHeight);
	}

}

function enableDoubleClickOnRadios() {
	// $('input[type="radio"]').live("dblclick", function() {
	// $(this).attr("checked", false);
	// });
	$('body').on('dblclick', 'input[type="radio"]', function() {
		uncheckRadio($(this));
	});
}

function uncheckRadio($radio) {
	if (!$radio.hasClass("disableUncheck")) {
		$radio.prop("checked", false);
		$radio.trigger("change");
	}
}

/**
 * fires after the window resizes. This is a general hook and can be used for
 * anything. It waits for the resize to complete (waits 100ms) before actually
 * firing the event so that it does not fire windowResize() every millisecond.
 */
var resizeTimer;
$(window).resize(function() {
	clearTimeout(resizeTimer);
	resizeTimer = setTimeout("windowResize()", 5);
});
function windowResize() {
	// set up the main container to resize (to provide padding) when the window
	// resizes. I hate doing this in javascirpt but there's no good way in CSS
	// and still maintain the size parameters
	//resizeMainContainer();

	DatePickerController.windowResizeHide();
	IbisDataTables.recalculateHeaderFooterWidths($("table.dataTable"));

	FormRowCalculator.redraw();
};

function openSection(sectionText) {
	var headList = $("h3#" + sectionText);
	if (headList.length > 0) {
		if (headList.get(0).next("p, div").not(":visible")) {
			headList.click();
		}
	}
	else {
		$("h3").each(function() {
			if ($(this).text() == "[+] " + sectionText && $(this).next("p, div").not(":visible")) {
				$(this).click();
				return false;
			}
		});
	}
}

function closeSection(sectionText) {
	var headList = $("h3#" + sectionText);
	if (headList.length > 0) {
		if (headList.get(0).next("p, div").is(":visible")) {
			headList.click();
		}
	}
	else {
		$("h3").each(function() {
			if ($(this).text() == "[-] " + sectionText && $(this).next("p, div").is(":visible")) {
				$(this).click();
				return false;
			}
		});
	}
}

var BrowserDetect = {
	init: function() {
		this.browser = this.searchString(this.dataBrowser) || "An unknown browser";
		this.version = this.searchVersion(navigator.userAgent) || this.searchVersion(navigator.appVersion)
						|| "an unknown version";
		this.OS = this.searchString(this.dataOS) || "an unknown OS";
	},
	searchString: function(data) {
		for (var i = 0; i < data.length; i++) {
			var dataString = data[i].string;
			var dataProp = data[i].prop;
			this.versionSearchString = data[i].versionSearch || data[i].identity;
			if (dataString) {
				if (dataString.indexOf(data[i].subString) != -1) return data[i].identity;
			}
			else if (dataProp) return data[i].identity;
		}
	},
	searchVersion: function(dataString) {
		var index = dataString.indexOf(this.versionSearchString);
		if (index == -1) return;
		return parseFloat(dataString.substring(index + this.versionSearchString.length + 1));
	},
	dataBrowser: [{
		string: navigator.userAgent,
		subString: "Chrome",
		identity: "Chrome"
	}, {
		string: navigator.userAgent,
		subString: "OmniWeb",
		versionSearch: "OmniWeb/",
		identity: "OmniWeb"
	}, {
		string: navigator.vendor,
		subString: "Apple",
		identity: "Safari",
		versionSearch: "Version"
	}, {
		prop: window.opera,
		identity: "Opera",
		versionSearch: "Version"
	}, {
		string: navigator.vendor,
		subString: "iCab",
		identity: "iCab"
	}, {
		string: navigator.vendor,
		subString: "KDE",
		identity: "Konqueror"
	}, {
		string: navigator.userAgent,
		subString: "Firefox",
		identity: "Firefox"
	}, {
		string: navigator.vendor,
		subString: "Camino",
		identity: "Camino"
	}, { // for newer Netscapes (6+)
		string: navigator.userAgent,
		subString: "Netscape",
		identity: "Netscape"
	}, {
		string: navigator.userAgent,
		subString: "MSIE",
		identity: "Explorer",
		versionSearch: "MSIE"
	}, {
		string: navigator.userAgent,
		subString: "Gecko",
		identity: "Mozilla",
		versionSearch: "rv"
	}, { // for older Netscapes (4-)
		string: navigator.userAgent,
		subString: "Mozilla",
		identity: "Netscape",
		versionSearch: "Mozilla"
	}],
	dataOS: [{
		string: navigator.platform,
		subString: "Win",
		identity: "Windows"
	}, {
		string: navigator.platform,
		subString: "Mac",
		identity: "Mac"
	}, {
		string: navigator.userAgent,
		subString: "iPhone",
		identity: "iPhone/iPod"
	}, {
		string: navigator.platform,
		subString: "Linux",
		identity: "Linux"
	}]

};
BrowserDetect.init();

/**
 * Redirects to the specified URL. If IE, it sets the referrer. Added to resolve
 * the access denied error due to a missing 'referrer' header when using IE.
 */
function redirectWithReferrer(url, target) {
	// re-define for IE
	var targetStr = "";
	if (typeof target == "string") {
		targetStr = target;
	}
	// if(navigator.userAgent.indexOf('MSIE') > 0){ //must be IE
	var referLink = document.createElement('a');
	referLink.href = url;
	referLink.target = targetStr;
	document.body.appendChild(referLink);
	referLink.click();
	/*
	 * }else{ //location.replace(url); location.assign(url); }
	 */
}

function openPopup(url, name, specs) {
	var wnd = null;

	if (typeof name == "undefined" || name == "") {
		name = "newwindow";
	}

	if ((BrowserDetect.browser == "Explorer")) {
		if ($("#redirectLink").length < 1) {
			// if we don't currently have a redirect link on the page, create
			// one
			$("body")
							.append(
											'<a href="javascript:void(0)" id="redirectLink" target="_self" style="display:none">click</a>');
		}
		// change the properties of the link and click it
		// wnd = window.open("", name, specs);
		var link = document.getElementById("redirectLink");
		link.target = name;
		link.href = url;
		link.click();
	}
	else {
		wnd = window.open(url, name, specs);
	}
	return wnd;
}



function showHideEformConfig() {
	
	var $hideElements = $(".eformConfigureHidden");

		$hideElements.each(function() {
			var $hideElement = $(this);
			if(areElementsHidden) {
				$hideElement.addClass("showHiddenElementBG");
				//need to handle select/multi select dropdowns bc these options are not visible to user
				if($hideElement.is("option")) {
					$hideElement.parent().addClass("selectShowHideBorder");
				}
				$hideElement.show();	
			}else {
				//need to handle select/multi select dropdowns bc these options are not visible to user
				if($hideElement.is("option")) {
					$hideElement.parent().removeClass("selectShowHideBorder");
				}
				$hideElement.hide();	
			}
		});
		
		if(areElementsHidden) {
			areElementsHidden = false;
			$("#showHideConfig").prop('value', 'Hide hidden eForm elements');
		}else {
			areElementsHidden = true;
			$("#showHideConfig").prop('value', 'Show hidden eForm elements');
		}
	
	}
