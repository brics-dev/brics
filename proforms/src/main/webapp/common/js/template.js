
var clickedSectionIds = new Array();
$(document)
		.ready(function() {
			$(".ctdbSectionContainer:visible").each(function() {
				var $this = $(this);
				if ($(this).parents(".rowTable").find('input[type="button"].repeatButton').length > 0) {
					var id = $(this).attr("id").split("_");
					storeSectionIds(id[1]);
				}
			});

			$(".repeatButton").hide();
			var $visibleGroup, $parentElement, searchParentId;
			var $repeatedDivs = $('div[parent]');
			var repeatedDivsLength = $repeatedDivs.length;
//			$repeatedDivs.each(function() {
//			  var $this = $(this);
//			  var maxRepeat = $this.attr("maxdisplay");
//			  if ($this.attr("parent") == "-1") {
//			    searchParentId = $this.attr("id");
//			    $parentElement = $this;
//			  }
//			  else {
//			    searchParentId = $this.attr("parent");
//			    $parentElement = $("#" + searchParentId);
//			  }
//			  $visibleGroup = $parentElement.add($('[parent="' + searchParentId + '"]:visible'));
//			  
//			  if (maxRepeat == $visibleGroup.length) {
//				  $this.parents(".rowTable").find(".repeatButton").hide();
//			  }
//			  else if ($this.is($visibleGroup.last())){
//				  $this.parents(".rowTable").find(".repeatButton").show();
//			  }
//			  else {
//				  $this.parents(".rowTable").find(".repeatButton").hide();
//			  }
//			});
			
			
			// the set repeat button needs to happen asynchronously
			var j = 0;
			setTimeout(function loopFunction() {
			    try {
			    	// perform your work here
			    	// note: we are only ever working on the FIRST element of the list
			    	// because we are removing elements from the list after processing
			    	var $this = $repeatedDivs.eq(0);
			    	var maxRepeat = $this.attr("maxdisplay");
			    	var parentId = $this.attr("parent");
			    	if (parentId == "-1") {
			    		searchParentId = $this.attr("id");
			    		$parentElement = $this;
			    	}
			    	else {
			    		searchParentId = parentId;
			    		$parentElement = $("#" + searchParentId);
			    	}
			    	$visibleGroup = $parentElement.add($('[parent="' + searchParentId + '"]:visible'));
				  
			    	var $removeSet = $this;
			    	if (maxRepeat == $visibleGroup.length) {
			    		$this.parents(".rowTable").find(".repeatButton").hide();
			    	}
			    	else if ($this.is($visibleGroup.last())){
			    		$this.parents(".rowTable").find(".repeatButton").show();
			    		$removeSet = $visibleGroup;
			    	}
			    	else {
			    		$this.parents(".rowTable").find(".repeatButton").hide();
			    	}
			    	
			    	// we need to remove either this element or a group.  This set is decided
			    	// in the if statement above.  This keeps the loop from processing the same
			    	// element more than once AND from processing sections in the same set
			    	// which are not visible
			    	$repeatedDivs = $repeatedDivs.not($removeSet);
			    }
			    catch(e) {
			        // handle any exception
			    }
			     
			    if ($repeatedDivs.length > 0) {
			        setTimeout(loopFunction, 0); // timeout loop
			    }
			    else {
			        // any finalizing code
			    }
			});
			
			// set up question options widths
			var $inputTds = $(".questionInputTD");
			var inputTdsLength = $inputTds.length;
			var k = 0;
			setTimeout(function questionWidthsLoop() {
			    try {
			        // perform your work here
			    	var $this = $inputTds.eq(k);
					var $labels = $this.find("label")
					if ($this.find(".horizontalDisplay").length > 0) {
						var width = Math.floor(100 / $labels.length);
						$labels.css("width", width + "%");
					}
					else {
						$labels.css("width", "100%");
					}
			    }
			    catch(e) {
			        // handle any exception
			    }
			     
			    k++;
			    if (k < inputTdsLength) {
			        setTimeout(questionWidthsLoop, 0); // timeout loop
			    }
			    else {
			        // any finalizing code
			    }
			});
		});
	

	function storeSectionIds(secId) {
	clickedSectionIds.push(secId);
	}
	

function showSection(sectionid, minVal, maxVal, buttID, repeatSecCount,parentVal, formRow) {
	var preprocessSectionId = null;
	var newFormRowVal = formRow + 1;
	var possibleSection = $('div[parent="' + parentVal + '"][formrow="'+ newFormRowVal + '"]');
	storeSectionIds(possibleSection.eq(0).attr("id"));
	if (possibleSection.length > 0) {
		preprocessSectionId = possibleSection.eq(0).attr("id");
		var nextSectionId = preprocessSectionId;
		var nextButtonId = preprocessSectionId + "_" + newFormRowVal;
		$("#" + sectionid + "_" + formRow).hide();
		$("#" + nextSectionId).show();
		$("#" + nextSectionId + " img").css("left", "");
		if (repeatSecCount == maxVal - 1) {
			$("#" + nextButtonId).hide();
		} else {
			$("#" + nextButtonId).show();
		}
	}
	
	// remove the spacing after the primary if this is a table
	var $containerDiv = $("#sectionContainer_" + sectionid);
	if ($containerDiv.hasClass("tablePrimary")) {
		$containerDiv.addClass("tablePrimary_repeat");
	}
	convertFileInputs();
}

