
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
			var $allGroup, $visibleGroup, $parentElement, searchParentId;
			$('div[parent]').each(function() {
			  var $this = $(this);
			  if ($this.attr("parent") == "-1") {
			    searchParentId = $this.attr("id");
			    $parentElement = $this;
			  }
			  else {
			    searchParentId = $this.attr("parent");
			    $parentElement = $("#" + searchParentId);
			  }
			  $allGroup = $parentElement.add($('[parent="' + searchParentId + '"]'));
			  $visibleGroup = $parentElement.add($('[parent="' + searchParentId + '"]:visible'));
			  
			  if ($allGroup.length == $visibleGroup.length) {
				  $this.parents(".rowTable").find(".repeatButton").hide();
			  }
			  else if ($this.is($visibleGroup.last())){
				  $this.parents(".rowTable").find(".repeatButton").show();
			  }
			  else {
				  $this.parents(".rowTable").find(".repeatButton").hide();
			  }
			});
			
			// set up question options widths
			$(".questionInputTD").each(function() {
				var $this = $(this);
				var $labels = $this.find("label")
				if ($this.find(".horizontalDisplay").length > 0) {
					var width = Math.floor(100 / $labels.length);
					$labels.css("width", width + "%");
				}
				else {
					$labels.css("width", "100%");
				}
			});
			
			//PS-3536: As a user, I should be able to print a properly formatted eForm
			$("table:has(.ctdbSectionContainer:visible)").css("break-after","left");
			$("table:has(.ctdbSectionContainer:visible):last-child").css("break-after","avoid");
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

