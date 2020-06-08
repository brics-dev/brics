//Contains functions for a dataStructure search and defines dataStructurePagination Object

// dataStructure Search Object
// This is a global object containing the properties needed for search
var dataStructurePagination = new Object();
dataStructurePagination.page = 1;
dataStructurePagination.pageSize = 10;
dataStructurePagination.sort = "title";
dataStructurePagination.ascending = false;
var persistIDisplayLength = 25;

// Additional properties ownerValue and filterValue are also required for search and are obtained
// through form elements on the page.

// This value submits a search. It takes no arguments, but uses global javascript variables on this page
// as well as reading values from the elements on the page
// These variables are set at the top of this block and changed by various javascript calls.
// Several other functions call this function after altering one of these variables to perform a search
function dataStructureSearch() {

	var hostName = $("#hostName").val();
	var hostStyle = "brics-style";
	if(typeof hostName !== 'undefined') {
		if(hostName.indexOf('pdbp') > -1 ){
			hostStyle = "pdbp-style";		
		}else if(hostName.indexOf('fitbir') > -1 ) {
			hostStyle = "fitbir-style";
		}else if(hostName.indexOf('eyegene') > -1 ) {
			hostStyle = "eyegene-style";
		}else if(hostName.indexOf('nei') > -1 ) {
			hostStyle = "eyegene-style";
		}else if(hostName.indexOf('cnrm') > -1 ) {
			hostStyle = "cnrm-style";
		}else if(hostName.indexOf('gsdr') > -1 ) {
			hostStyle = "gsdr-style";
		}else if(hostName.indexOf('ninds') > -1 ) {
			hostStyle = "ninds-style";
		}else if(hostName.indexOf('cistar') > -1 ) {
			hostStyle = "cistar-style";
		}else if(hostName.indexOf('cdrns') > -1 ) {
			hostStyle = "cdrns-style";
		}else if(hostName.indexOf('nti') > -1 ) {
			hostStyle = "nti-style";
		}else if(hostName.indexOf('nia') > -1 ) {
			hostStyle = "nia-style";
		}else if(hostName.indexOf('grdr') > -1 ) {
			hostStyle = "grdr-style";
		}
	}
	
	var searchTerm = $('input[name="searchTerm"]').val().trim();
	var exactMatch = $("#exactMatch").is(':checked');

	if($('#public').val() != '' && $('#public').val() == "true") {
		var ownerValue = $('input[name="owner"]').val();
	} else {
		var ownerValue = $('input[name="owner"]:checked').val();
	}
	
	var statusValue = $('input[name="selectedStatusOptions"]:checked').map(function() {
	    return this.value;
	}).get();
	
	var standardizationValue = $('input[name="selectedStandardizationOptions"]:checked').map(function() {
	    return this.value;
	}).get();
	
	var formLabelValue = $('input[name="selectedFormLabelOptions"]:checked').map(function() {
	    return this.value;
	}).get();
	
	var requiredValue = $('input[name="selectedRequiredOptions"]:checked').map(function() {
	    return this.value;
	}).get();
	
	var formTypeValue = $('input[name="selectedFormTypeOptions"]:checked').map(function() {
	    return this.value;
	}).get();

	var diseaseValue = $('input[name="selectedDiseaseOptions"]:checked').map(function() {
	    return this.value;
	}).get();
	
	var copyRightValue = $('input[name="selectedCopyRightOptions"]:checked').map(function() {
	    return this.value;
	}).get();
	
	var facetValue = $('input[name="selectedFacets[\'Facet1\']"]:checked').map(function() {
	    return this.value;
	}).get();
	
	var action = "listDataStructureAction!search.ajax";
	
	var oTable = $('#resultTable').dataTable( {
		
		"bProcessing": true,
		"bJQueryUI": true,
	    "bServerSide": true,
	    "sAjaxSource": action,
	    
	    "fnServerParams": function (aoData) {
	    	aoData.push( 
	        		{"name":"searchKey", "value": searchTerm},
	    			{"name":"exactMatch" , "value" : exactMatch},
	        		{"name":"ownerId", "value": ownerValue},
	        		{"name":"selectedRequiredOptions", "value": requiredValue.toString()},
	        		{"name":"selectedStatusOptions", "value": statusValue.toString()},
	        		{"name":"selectedStandardizationOptions", "value": standardizationValue.toString()},
	        		{"name":"selectedFormLabelOptions", "value": formLabelValue.toString()},
	        		{"name":"selectedFormTypeOptions", "value": formTypeValue.toString()},
	        		{"name":"selectedDiseaseOptions", "value": diseaseValue.toString()},
	        		{"name":"selectedCopyRightOptions", "value": copyRightValue.toString()},
    				{"name":"selectedFacets['Facet1']", "value": facetValue});

	    	//loop here
	    	$.each($('input[name^="selectedFacets"]:checked'), function( key, value ) {
	    	
	    		var facetValue = $('input[name="'+value.name+'"]:checked').map(function() {
	    		    return this.value;
	    		}).get();
	    		
	    		//aoData.push({"name":value});
	    	});
	    	
	    },
	        
	    "bFilter": false,
	    "sPaginationType": "full_numbers",
	    "sDom": '<"H"ilfr>t<"F"ip>',
	    "scrollY": "1000px",
	    "sScrollX": "100%",
	    "bScrollCollapse": true,
	    "bAutoWidth": false,
	    "bDestroy": true,
	    "sServerMethod": "POST",
	    "sEmptyTable": "Loading data from server",
	    "iDisplayLength": persistIDisplayLength,
	    "fnInitComplete": function () {
	    	if ( window.console && console.log ){
	    		// empty
	    	} 
        },
        
      	"fnHeaderCallback": function( nHead, aData, iStart, iEnd, aiDisplay ) {
      		$('#resultTable_info').css({"float": 'right', "padding": '0 10px'});
      		
      		$(".tableRowHeader th").attr("role", "columnheader");
        },

        "fnInfoCallback": function( oSettings, iStart, iEnd, iMax, iTotal, sPre ) {
    	    
      	  persistIDisplayLength = oSettings._iDisplayLength;
		      	if(iTotal > 0) {
		    	  	return "Showing " + iStart +" to "+ iEnd + " of " + iTotal + " entries";
		    	  } else {
		    		return "";
		    	  }
      	  },
        
	    "aoColumnDefs": [{
            "aTargets": ["_all"],
            "fnCreatedCell": function (nTd, sData, oData, iRow, iCol) {
            	grantedAccess = true; ///currently granted access is always true, i'm not sure why this is needed.
            	cellContent = sData.split("|");
            	newContent = $(nTd).html();
            	if(cellContent.length > 1) {
            		if($('#public').val() != '' && $('#public').val() == "true") {
            			newContent ='<a target="_blank" href="/portal/publicData/dataStructureAction!view.action?dataStructureName='+cellContent[0]+'&publicArea=true&style.key='+hostStyle+'" title="'+cellContent[2]+'">'+cellContent[1]+'</a>';
            		} 
            		else {
	            		if($('#inAdmin').val() == "true") {
	            				newContent ='<a id="viewId'+cellContent[0]+'" class="tip_trigger" href="/portal/dictionaryAdmin/dataStructureAction!view.action?dataStructureName='+cellContent[0]+'" title="'+cellContent[2]+'">'+cellContent[1]+'</a>';
	            		} 
	            		else {
	            				newContent ='<a id="viewId'+cellContent[0]+'" class="tip_trigger" href="/portal/dictionary/dataStructureAction!view.action?dataStructureName='+cellContent[0]+'" title="'+cellContent[2]+'">'+cellContent[1]+'</a>';
            			}
            		}
            	} 
            	else {
            		$(nTd).html(newContent);
            	}
            	
            	//this is for asthetics give us some room to the text isn't so compact
	           $(nTd).html(newContent);
	           if(iCol == 3) {
	        	  // $(nTd).wrapInner( "<div style='max-width:100px'></div>");
	           }
            }
	    }],
          
          
  	    "fnDrawCallback": function( oSettings ) {
  	    	 $("a.lightbox").fancybox();
  	    	 oSettings._iDisplayLength = persistIDisplayLength;
  	     }
	});
}

// Alters the filter links to the appropriate status and then calls the search function
function refreshSearch() {
	dataStructureResetPagination();
	dataStructureResetSort();
	dataStructureSearch();
}

function dataStructureSetOwner(newOwnerId) {
	var oldOwner = $(".dataStructureSelectedOwner")[0];
	var newOwner = $("#" + newOwnerId, $("#dataStructureOwnerOptions"))[0];
	
	var oldOwnerId = $(oldOwner).attr('id');
	
	$(oldOwner).removeAttr('class');
	$(oldOwner).attr("href", 'javascript:dataStructureSetOwner(' + oldOwnerId + ');');
	$(newOwner).attr("class", "inactiveLink dataStructureSelectedOwner");
	$(newOwner).removeAttr("href");
	
	dataStructureResetPagination();
	dataStructureResetSort();
	dataStructureSearch();
}

// This function casues the result to jump to a page given by the page text field
// This text field is defined in elementList.jsp
// This function also checks to make sure the given page input is valid
function dataStructureCheckPageField(maxPage) {
	var desiredPage = document.getElementById("dataStructurePaginationJump").value;
	maxPage = Math.ceil(maxPage);
	if (!isNaN(desiredPage)) {
		if (desiredPage <= maxPage && desiredPage > 0 && (Math.ceil(desiredPage) / desiredPage == 1)) {
			dataStructurePagination.page = desiredPage;
			dataStructureSearch();
		}
		else {
			document.getElementById("dataStructurePaginationJump").value = dataStructurePagination.page;
		}
		
	}
	else {
		document.getElementById("dataStructurePaginationJump").value = dataStructurePagination.page;
	}
}

// Sets the global pagination values back to their default values
function dataStructureResetPagination() {
	dataStructurePagination.page = 1;
	dataStructurePagination.pageSize = 10;
}

function dataStructureResetSort() {
	dataStructurePagination.sort = "title";
	dataStructurePagination.ascending = false;
}

// Function called when user clicks a table head to sort a column.
function dataStructureSetSort(sortIn) {
	dataStructureResetPagination();
	if (sortIn == dataStructurePagination.sort) {
		dataStructurePagination.ascending = !dataStructurePagination.ascending;
	}
	else {
		dataStructurePagination.sort = sortIn;
	}
	dataStructureSearch();
}

function showStandardizationTooltips() {
	$.each($('.dynamicTitle'), function() {
		if($(this).attr('title')=='Standard NINDS'){
			$(this).attr('title'," Standard forms/measures recommended by the NINDS CDE project");
		}
		else if($(this).attr('title')=='Standard not NINDS'){
			$(this).attr('title',"Community used standard forms/measures");
		}
		else if($(this).attr('title')=='Modified'){
			$(this).attr('title',"Significant study specific modifications to an original standard");
		}
		else if($(this).attr('title')=='Appendix'){
			$(this).attr('title',"Additional study specific questions not found in standard forms/measures");
		}
		else if($(this).attr('title')=='Unique'){
			$(this).attr('title',"Study specific nonstandard forms/measures");
		}
	});

}
