<%@page import="gov.nih.tbi.commons.model.StatusType"%>
<%@include file="/common/taglibs.jsp"%>
<style>

.dataTables_scrollHead,.dataTables_scrollHeadInner,.dataTables_scrollBody {
width:100% !important;
}

.dataTables_scrollHeadInner table {
width:100% !important;
}


#resultTable {
width:100% !important;
}
</style>

<!--  This file is loaded (via ajax) in the dictionary namespace so the user can properly load the data element list from the public space -->

<s:set var="mapped" value="mappedList" />
<s:hidden id="publicAreaInterface" name="publicArea" />

<!-- Search Box -->
<form >
	<div style="width:150px; height:50px; float:left;">
		<s:select id="dataElementLocationsSelect" name="dataElementLocationsSelect" multiple="true" list="#{
					'keywords':'Key Words', 
					'description':'Description', 
					'permissibleValue':'Permissible Values', 
					'title':'Title', 
					'labels':'Labels',
					'externalId':'External IDs',
					'shortName':'Variable Name'}" 
				
					
				value="%{{'keywords','description','permissibleValue','title','labels','externalId','shortName'}}" />
	</div>

	<s:textfield cssStyle="width:300px; height:34px; float:left;" name="searchKey" id="searchKey"  aria-label="Search Key for Data Elements"
		onkeydown="javascript: if (event.keyCode == 13){ dataElementResetPagination(); dataElementResetSort(); dataElementSearch(); return false;}" />

	<div class="submit-container">
		<input id="searchKeySubmit" type="submit" value="search submit button" class="submit submit-mag" />
	</div>
	<div style="float:left; width:150px; padding-left:8px; margin-top:10px;">
		<s:checkbox id="exactMatch" name="exactMatch" cssStyle="float:left;" /> 
		<s:label for="exactMatch" cssStyle="width:150px; font-weight:normal; font:12px/1.2 Arial; float:none; display:block;" value="Whole Word or Phrase" />
	</div>
	
	<c:if test="${mapped}">
	<div class="clear-both"></div>
			
				<p>Search existing elements to attach to this form structure</p>
			
			</c:if>
</form>	
<div style="clear:both;"></div>

<div style="width:20%; float:left; padding-top:10px;">
<h3>Narrow your search</h3>
<div id="dataElementFilters">

	<jsp:include page="../dictionaryTool/searchDataElements-dataElementFilters.jsp" />
</div>
</div>
<div style="width:79%; float:left; padding-top:10px;">
<input type="hidden" id="mapped" name="mapped" value="${mappedList}" />
<input type="hidden" id="hostName" name="hostName" value="${pageContext.request.serverName}" />
<input type="hidden" id="public" name="public" value="${publicArea}" />
<input type="hidden" id="inAdmin" name="inAdmin" value="${inAdmin}" />
<input type="hidden" id="grantedAccess" name="grantedAccess" value="${accessIdList}" />
<input type="hidden" id="modulesDDTURL" name="modulesDDTURL" value="${modulesDDTURL}" />
<input type="hidden" id="portalRoot" name="portalRoot" value="${portalRoot}" />

<div id="dataElementResultsId" style="margin-bottom:50px;">
		
	<div id="tableActionDiv" style="float:right; height:0px; min-width:150px; border:1px none #000; position:relative;">
		<s:if test="public">
			<div class="button">
				<input id="resetLink" type="button" value="Reset All" style="display:block; position:relative; top:3px; left:-15px;  z-index:100; cursor:pointer;" />
			</div>
			<div class="button">
				<input id="downloadResultsLink" type="button" value="Download all Results" style="display:block; position:relative; top:3px; left:-8px;  z-index:100; cursor:pointer;" />
			</div>
		</s:if>
		<s:else>
			<div class="button">
				<input id="resetLink" type="button" value="Reset All" style="display:block; position:relative; top:3px; left:-67px;  z-index:100; cursor:pointer;" />
			</div>
			<div class="button">
				<input id="downloadResultsLink" type="button" value="Download all Results" style="display:block; position:relative; top:3px; left:-60px;  z-index:100; cursor:pointer;" />
			</div>
		</s:else>

		<div id="downloadLinks" style="width:250px;  position:absolute; height:auto; z-index:200; background:#fff; color:#000; top:28px; left:-118px; padding:5px; display:none; border:1px solid #cdcdcd">
		<p>
			Data Element Results:&nbsp; 
			<a href="javascript:exportDataElements('XML')">XML</a>&nbsp;
			<a href="javascript:exportDataElements('CSV')">ZIP</a><br/> REDCap Format:
			<a href="javascript:exportDataElements('REDCap')">CSV</a>
		</p></div>
	</div>
	
			<div class="clear-both"></div>
			<div id="dataElementResultsContainter" class="brics">
				<table id="resultTable">
  					<thead>
  					
				
						<tr class="tableRowHeader">
	  						
		  					<th width="145" style="width:120px">Title</th>
	  						<th width="145" style="width:120px">Variable Name</th>
	  						<th width="80">Type</th>
	  						<th width="120">Modified Date</th>
	  						<th width="120">Status</th>
		  				</tr>
					
					</thead>
					<tbody></tbody>
				</table>
				</div>
		<div class="clear-both"></div>
			
			
		</div>
		
		
		<div id="overlay"></div>

<!-- Search box for Diseases -->
<div id="treeFilterContainer">
	<h2 id="filterFocus">Diseases</h2>
	<p>Use the diseases listed below to refine your search for domains and sub-domains</p>
	<hr>
	<!-- Disease List -->
	<div id="columnOne" class="treeColumn"></div>

	<!-- Domains -->
	<div id="columnTwo" class="treeColumn"></div>


	<!-- Sub-Domains -->
	<div id="columnThree" class="treeColumn"></div>
	<div class="clear-both"></div>
	<div class="button" style="margin:10px;">
			&nbsp;<input type="button" value="Apply" class="float-left" onClick="javascript: saveTree();" />
			&nbsp;<input type="button" value="Cancel" class="float-left" onClick="javascript: closeTree(); " />
	</div>
</div>



</div>
<div class="clear-both"></div>
<script type="text/javascript">

//initialize multi - select
$("#dataElementLocationsSelect").multiselect({
      buttonText:function(options, select) {
          if (options.length == 0) {
              return  'Search Locations <b class="caret"></b>';
            }
            else {
            	return  'Search Locations <b class="caret"></b>';
            }
         },
         checkboxName: 'dataElementLocations'
});

var dataElementsMappingArray = new Array();
var treeSelectedDisease = "";
var domainArray = new Array();
var subDomainArray = new Array();
var classificationsArray = new Array();
	// Load a search at the start
	$('document').ready(function() {
	
		//set scroll bar styles
		$(".diseaseSelections").mCustomScrollbar({
			theme:"inset-dark",
		    scrollButtons:{ enable: true },
		    autoHideScrollbar: true
		});
		
		$("#searchKeySubmit").click(function(e){
			e.preventDefault();
			dataElementSearch();
			
		});
		
		$("#resetLink").click(function(e){
		    $('#dataElementLocationsSelect option').prop('selected', function() {
		        return this.defaultSelected;
		    });
		    
		    $('[name="dataElementLocations"]').attr('checked', true);
		    $('#searchKey').val("");
		    $('#exactMatch').attr('checked', false);
		    
			document.forms['searchForm'].reset();
			closeTree();
			dataElementSearch();
		});
		
		$("#downloadResultsLink").click(function(e){
			
			$("#downloadLinks").toggle( "blind",300 );
			
		});
		
		
		
		dataElementSearch();
	
		
	});
	
	$(".selectedDisease").click(function(e) {
		 e.preventDefault();
		 
		 //scroll to the top of the page
		  $("html, body").animate({ scrollTop: 0 }, "fast");
		 
	        
	        var diseaseSelect = $('input[name="selectedDiseasesBox"]:checked').map(function() {
	    		return this.value;
			}).get();
	        
	       
	        //change styles for the public area
	        if($('#public').val() != '' && $('#public').val() == 'true') {
	        	
	        	$(".treeColumn").addClass('treeColumnPublic');
	        	
	        	//class to treeFilterContainer to fit on public page
	        	$("#treeFilterContainer").addClass('treeFilterContainerPublic');
	        }
	        
	        //.treeColumn
	        
	        $("#overlay").show(300, function() {
	        	$("#treeFilterContainer").show(300,function(){
	        		//populate disease list
	        		
	        		$.ajax("searchDataElementAction!updateDisease.ajax", {
	        			"type": 	"POST",
	        			"async": 	false,
	        			"data": 	{"selectedDiseases" : diseaseSelect.toString()},
	        			"success": 	function(data) {
	        							$("#columnOne").html(data);	
	        							//$('#content-wrapper').focus();
	        							
	        							
	        							$('#columnOne li.selected').each(function () {
	        						          
	        								name = $(this).attr('name');
	        								nameArray = name.split("_");
	        								treeSelectedDisease = nameArray[1].toString();
	        								
	        						        	   $(this).addClass("currentView");
	        						        	 
	        						        	  //open classifications for first disease
													// Ajax call to add classifications
													$.ajax("searchDataElementAction!updateClassifications.ajax", {
														"type": 	"POST",
														"async": 	false,
														"data": 	{"activeDisease" : treeSelectedDisease},
														"success": 	function(data) {
															
															$('.currentView').next('li').html(data);	
																	}
													});
													$('.currentView').next('li').show("slow",function(){});
													
	        						               return false;
	        						          
	        						});
	        							
	        						}
	        		});  
	        		
	        		//populate domain list
	        		$.ajax("searchDataElementAction!updateDomain.ajax", {
	        			"type": 	"POST",
	        			"async": 	false,
	        			"data": 	{"selectedDiseases" : treeSelectedDisease},
	        			"success": 	function(data) {
	        				
	        							$("#columnTwo").html(data);	
	        							
	        							//check columns from previous selection
	        							
	        							if(domainArray.length > 0) {
	    									
	    									$('#columnTwo [name="selectedDomains"]').each(
	    											function(){
	    												
	    												if($.inArray($(this).val(),domainArray) > -1) {
	    													
	    													$(this).prop('checked', true);
	    												} else {
	    													$(this).prop('checked', false);
	    												}
	    											});
	    								
	    								}
	        							
	        							
	        							
	        							
	        							if($("#columnTwo input:checked").length < 1) {
	        								$('#columnTwo [name="selectedAllDomains"]').prop("checked",true);
	        							}
	        						}
	        		});  
	        		
	        		//populate sub-domain list
	        		$.ajax("searchDataElementAction!updateSubDomain.ajax", {
	        			"type": 	"POST",
	        			"async": 	false,
	        			"data": 	{"selectedDiseases" : diseaseSelect.toString(),
	        						 "selectedDomains" : "all"},
	        			"success": 	function(data) {
	        				
	        							$("#columnThree").html(data);	
										//check columns from previous selection
	        							
	        							if(subDomainArray.length > 0) {
	    									
	    									$('#columnThree [name="selectedSubDomains"]').each(
	    											function(){
	    												
	    												if($.inArray($(this).val(),subDomainArray) > -1) {
	    													
	    													$(this).prop('checked', true);
	    												} else {
	    													$(this).prop('checked', false);
	    												}
	    											});
	    								
	    								}
	        							
	        							
	        							
	        							
	        							if($("#columnThree input:checked").length < 1) {
	        								$('#columnThree [name="allSubDomains"]').prop("checked",true);
	        							}
	        						}
	        		});  
	        		
	        		
	        	}); 
	        	
	        });  
	        
	        
	});
	
	
	
	
	//close tree
	function closeTree(){
		var overlayContainer = $("#overlay");
		var treeContainer = $("#treeFilterContainer");
		treeContainer.hide(300, function(){overlayContainer.hide(300);});
		//empty tree session arrays
		treeSelectedDisease = "";
		domainArray = new Array();
		subDomainArray = new Array();
		classificationsArray = new Array();
	}
	
	//close tree
	function saveTree(){
		
		var term = "";
		//update disease checkboxes based on selections
		//Did we choose any sub-domains related to this disease if so, check it.
		$('.diseaseSelections li.selected').each(function(){

			
					//get disease from selected li
				    term = $(this).attr('name').split("_")[1];
					
					//loop through disease checkboxes and compare values to determine if it should be checked.
					$('[name="selectedDiseasesBox"]').each(function(){
						if($(this).val() == term) {
							
							$(this).prop("checked",true);
						}
					});
			
			
		})
		
		
		
		
		var overlayContainer = $("#overlay");
		var treeContainer = $("#treeFilterContainer");
		treeContainer.hide(300, function(){overlayContainer.hide(300);});
		save = true;
		
		
		//submit form values
		dataElementSearch();
		//empty tree session arrays
		treeSelectedDisease = "";
		
		
	}
	
	
	//function for export links
	function submitTheFormStructure(formAction) {
		//set a few variables to submit with the form
		var category = $("#categoryOptions input:checked").val()
		$("#category").val(category);

		
		
		var searchLocations = dataElementGetSearchLocations();
		
		$("#searchLocations").val(searchLocations);
		
		var theForm = document.forms['searchForm'];
		theForm.action = formAction;
		theForm.submit();

	}



	
</script>
