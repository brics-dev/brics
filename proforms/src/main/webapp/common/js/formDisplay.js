//array to hold all the javascript Section objects
var sectionsArray = new Array();

//array to hold all the javascript Question objects // Ching Heng
var questionsArray = new Array();

		$('document').ready(function() {
			$('.ctdbSectionContainer').each(function() {
				sectionsArray.push(new SectionObj($(this).attr("id")));
			});
			
    		$('.ctdbSectionContainer').each(function() {
    			$(this).css("height", "auto");
    			DynamicElements.enforceSectionRows(null, {item: $(this)});
    		});
    	});
        	
        	var DynamicElements = {
        			enforceSectionRows : function(event, ui) {
					var target = $(ui.item);
					var rowArray = new Array();
					var maxHeight = 0;
					var minHeight = 1000000;
					var allSame = true;
					// get all sections in row
					for (var i = 0; i < sectionsArray.length; i++) {
						var otherColElement = $("#"+sectionsArray[i].id);
						if (otherColElement.offset().top == target.offset().top) {
							rowArray[rowArray.length] = otherColElement;
						}
					}
					
					// clear the heights of the sections in the row
					for (var i = 0; i < rowArray.length; i++) {
						rowArray[i].css("height", "auto");
					}
					
					// get the max height of sections in this row
					var maxHeight = 0;
					for (var i = 0; i < rowArray.length; i++) {
						if (rowArray[i].height() > maxHeight) {
							maxHeight = rowArray[i].height();
						}
					}
					
					// set the sections to all the same height
					
					
					// we now have all of the row elements, resize all to match either the largest or smallest
					// why do we care about the smallest? 
					// is the target the smallest?
					// if the target is the largest
					for (var i = 0; i < rowArray.length; i++) {
						rowArray[i].height(maxHeight);
					}
				}
        	}; 
        	
        	function SectionObj(id,name,description,isCollapsable,ifHasResponseImage) {
        		this.name = name;
        		this.description = description;
        		this.isCollapsable = (isCollapsable == "true") ? true : false;
        		this.ifHasResponseImage = (ifHasResponseImage == "true") ? true : false;	
        		this.id = id;
        		this.existingSection = false;
        		this.row = -1;
        		this.col = -1;
        	}