$(function() {
/*All javascript functionality used in this site is powered by the jQuery JavaScript library. Please see: http://jquery.com/ for full documentation. The UI development bundle [http://jqueryui.com/] has been included with HTML source files. */

/* ---- Initialize the accordion */
	$(".static-accordion").accordion( { icons: false, autoHeight: false } );
	
	$( "#sop-accordion" ).accordion({
			active: false,
			collapsible:true,
			autoHeight: false,
			icons: { 'header': 'ui-icon-plus', 'headerSelected': 'ui-icon-minus' }
	});
	$( "#access-accordion" ).accordion({
			autoHeight: false,
			icons: { 'header': 'ui-icon-plus', 'headerSelected': 'ui-icon-minus' }
	});
	
	$("#details-accordion").accordion({ autoHeight: false, header: "h4", collapsible: true });

/* ---- script for showing/hiding the log in form in the header */
	$('#login-link a').click(function () { 
		$(this).toggleClass("expanded");
		$("#login-panel").slideToggle(); 
		return false;
	});
	
/* ---- script for table stripes / zebra string --*/
$("table.display-data tr:even").addClass("stripe");

/* The script below triggers the lightbox. The <a> element must have the class="lightbox" to trigger the lightbox. Lightbox functionality is using the Fancy box plugin. See http://fancybox.net/ for documentation. */
$('a.lightbox').fancybox();
$('input.lightbox').fancybox();

/* The script below triggers a lightbox. The <a> element must have the class="action-btn" to trigger the lightbox. Markup content of the light box is after the ending div for #body-wrapper. See lightbox.html file for example HTML markup. Lightbox functionality is using the Fancy box plugin. See http://fancybox.net/ for documentation. */
	$('a#content-link, a.open-lightbox').click(function () {
		$('#get-content').dialog({
			modal:true,
			width: 660
		});
		return false;
	});
	

/* ---- The script below validates all forms that have a class of "validate". In order for this to work, the markup needs to be:
<form class="validate" id="" action="" method="post">
<fieldset>
<legend>Section Title goes here</legend>
<div class="form-field">
	<label>Field name goes here</label>
	<input type="text" class="textfield required" value="" />
</div>
</fieldset>

</form> 


If a user submits the form with a blank/space character or the default text in the input field, 
the user recieves an alert box stating the field is required. 
The form must have a class of "validate" in order to use the script below.
Please use the HTML markup for the search or login forms to use this vaildation function.*/
// Finds a form on the page, instantiates a Calendar Popup and hides "other" fields if necessary
	$(this).find("form").each(function() {
		$(".date-picker").each(function() {
			$(this).datepicker({ 
				buttonImage: "/portal/images/brics/common/icon-cal.gif", 
				buttonImageOnly: true ,
				buttonText: "Select Date", 
				changeMonth: true,
				changeYear: true,
				minDate: '0',
				duration: "fast",
				gotoCurrent: true,
				hideIfNoPrevNext: true,
				showOn: "both",
				showAnim: "blind",
				yearRange: '-0:+5'
			});
		}).attr("readonly", "readonly");
		
		// hide all "other" fields
		$("ul.checkboxgroup input.other").filter(".textfield").hide();
		$("ul.checkboxgroup input").click(function() {
			// if the input has a class of other, show it's "other" field
			if ($(this).is(".other")){
				$(this).siblings(".other").show().addClass("required");
				// if it's a checkbox, remove the "other" field when it is unchecked
				if (!this.checked) {
					$(this).siblings(".other").filter(".textfield").hide().removeClass("required");
				}
			}
			// if it's a radio button group, hide "other" fields if not needed
			else if ($(this).is(".radio")) {
				$(this).parent().parent().find(".other").filter(".textfield").hide().removeClass("required");
			}
		});
		// hide all "select-other" divs
		$("div.select-other").hide();
		// if "Other" is selected, then show it's "other" field
		$("select.select-other").change( function () {
			if($(this).val() === "--") {
				$(this).parent().next().show();
				if($(this).is(".required")) {
					$(this).parent().next().find("label").addClass("required");
					$(this).parent().next().find("input").addClass("required");
				}
			} else {
				$(this).parent().next().hide().find("input").val("");
			}
		});
		
		$(".textfield-other").hide();
		$("ul#file-upload li input.radio").click(function() {
				//check if the radio button is checked.
			if ( $(this).is(":checked")) {
				//find the other .textfield-others that are open and hide them
				$(this).parent().parent().find(".textfield-other").hide().find("input").removeClass("required");
				//then show this particular's radio button's .textfield-other fields
				$(this).siblings(".textfield-other").show().find("input").addClass("required");
			} 
		});

	});

	// Validates the form, adding warning icons where needed and alerting user upon completion
	$(".validate").submit(function(event) {
		var errorCount = 0;
		var $first_error = null;

		// find all elements with class name of "required" (minus labels and spans) within all divs with a class of field
		$(this).find("div.form-field .required").not("label, span").each(function() {
			var $elem = $(this);
			
			// remove any previous warnings and validate based on element type
			if ( $elem.is("input") || $elem.is("select") || $elem.is("textarea") ) {
				var valid = true;
				var o = $.trim($elem.val());

				if ( $elem.is(".email") ) {
					// if it's an email address make sure the email is valid using both regular expressions
					var regExp = /^.+\@(\[?)[a-zA-Z0-9\-\.]+\.([a-zA-Z]{2,3}|[0-9]{1,3})(\]?)$/;
					var regExp2 = /(\s+)|(@.*@)|(\.\.)|(@\.)|(\.@)|(^\.)/;
					
					if ( ( o.search( regExp ) ) == -1 || o.search( regExp2 ) != -1 ) {
						valid = false;
					}
				}
				// If not an email address see if it is blank
				else if ( o.length == 0 ) {
					valid = false;
				}
				
				// if the data is not valid show warning icon
				if ( !valid ) {				
					errorCount++;
					
					if ( $first_error == null ) {
						$first_error = $elem;
					}		
				}

			}
			else if ( $elem.is("ul") ) {
				// Verify the checkboxes in this "ul" element(s).
				$elem.each(function() {
					var $ul = $(this);
					var valid = false;
					
					// find all li elements
					$ul.find("li").each(function() {
						var $li = $(this);
						
						// checkboxes in the list
						var $input = $li.children("input");
						
						// Check if this is not the "other" checkbox.
						if ( $input.length == 1 ) {
							if ( $(testdis).is(":checked") ) {
								valid = true;
							}
						}
						// Verify if there were any inputs in the "li" element. If there its then it is the "other" checkbox.
						else if ( $input.length > 1 ) {
							if ( $input.eq(0).is(":checked") && $.trim($input.eq(1).val()).length > 0 ) {
								valid = true;
							}
						}
					});
					
					if ( !valid ) { 						
						errorCount++;
						
						if ( $first_error == null ) {
							$first_error = $ul.find("input:checkbox:first");
						}
					}
				});			
			}
		});
		
		// If there are any errors, report the first error.
		if ( errorCount > 0 ) {
			var labelTxt = $first_error.parent().children("label").text();
			
			// Filter out some special characters
			labelTxt = $.trim(labelTxt.replace(/(:|\*)/g, ""));
			
			// Show error message.
			alert("A valid \"" + labelTxt + "\" is required.");
			$first_error.focus();
			
			return false;
		}		
	});

	// Call the less function on any divs with the limitLength class to truncate text and add a more link
	$('div.limitLength').each( function(){
		less(this.id, 255);
	});
	
	// Truncates text inside of the div indicated by id to the length indicated by charLimit and adds a link to expand the text
	function less(id, charLimit) {
		var $holder = $('#' + id);
		var fullText = $holder.text();
		
			var trunc = fullText;
			if (trunc.length > charLimit) {
				newDiv = "<div id='swap-" + id + "'>" + fullText + " <a class='moreLessSwap' href='#swap-" + id + "'> [Show Less] </a></div>";
				
				$hiddenSwapDiv = $('div.moreLessSwap-content');
				if( $hiddenSwapDiv.length > 0 )
				{
					$hiddenSwapDiv.append(newDiv);
					
				}else{
					hiddenSwapDiv = "<div class='moreLessSwap-content'>" + newDiv + "</div>";
					$('body').append(hiddenSwapDiv);
				}
				
				$newDiv = $('#swap-' + id);
				
				/* Truncate the content of the text, then go back to the end of the
				   previous word to ensure that we don't truncate in the middle of
				   a word */
				trunc = trunc.substring(0, charLimit);
				trunc = trunc.replace(/\w+$/, '');
				
				$holder.text(trunc).append('<a class="moreLessSwap" href="#swap-' + id + '">...</a>');
			}
	}
	
	$('a.moreLessSwap').click( function(event){
		// Pass everything after the # to swapMoreLess so it knows which text to swap
		swapMoreLess( this.href.split('#')[1] );
		return false;
	});
	
	// Swap the text from the hidden div with the text displayed to show more or less text
	function swapMoreLess(clicked) {
		id = clicked.split('swap-')[1];
		
		$swap = $('#' + clicked);
		swapText = $swap.html();
		
		$current = $('#' + id);
		currentText = $current.html();
		
		$current.html(swapText);
		$swap.html(currentText);
		
		// Rebind the click event on the more/less link
		$current.children('a.moreLessSwap').click( function(event){
			// Pass everything after the # to swapMoreLess so it knows which text to swap
			swapMoreLess( this.href.split('#')[1] );
			return false;
		});
	}
	
	$('div.collapsable').each(function(index) {
		$collapsableDiv = $(this);
		$collapsableDiv.find('h3 a').attr("id",index);
		$collapsableDiv.attr("id","container_"+index);
		
		$collapsableDiv.find('.lessIcon').attr("id","lessIcon_" + index);
		$collapsableDiv.find('.moreIcon').attr("id","moreIcon_" + index);
		
		//On load, hide these things
		$collapsableDiv.find('.lessIcon').hide();
		$collapsableDiv.find('.collapsableContent').hide();
		
		$collapsableDiv.find('h3 a').click(function() {
			var id = $(this).attr("id");
			var $ident = $("#container_" + id);
			var less = "#lessIcon_" + id;
			var more = "#moreIcon_" + id;
			
			$ident.find(less).toggle();
			$ident.find(more).toggle();
			$ident.find('.collapsableContent').toggle();
			
			return false;
		});
	});
	
	tooltip();
}); //end of $(function() 

function getURLParameter(name) {
    return decodeURI(
        (RegExp(name + '=' + '(.+)(&|$)?').exec(location.search)||[,])[1]
    );
}

function tooltip() {
	$('a[title]').qtip({
		style: { classes: 'ui-tooltip-green ui-tooltip-shadow' },
		position: {
	        my: 'left top',  // Position my top left...
	        at: 'bottom left', // at the bottom right of...
	        target: $('a[title]') // my target
	    }
	});
}

function submitForm(action) {
	// Disable all buttons on the page.
	$("input:button,button").prop("disabled", true);
	
	// Change the form's action attribute and submit the form.
	var $form = $("#theForm");
	$form.attr("action", action);
	$form.submit();
}
