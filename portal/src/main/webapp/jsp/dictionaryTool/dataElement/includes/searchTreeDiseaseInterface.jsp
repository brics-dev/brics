<%@include file="/common/taglibs.jsp"%>
<div class="facet-form-field">
	<b>Disease:</b>
	<ul id="diseaseSelectionsTree" class="tree diseaseSelections">
		<s:iterator value="diseaseOptions">
			<s:if test="%{name in selectedDiseasesFromBase}">
				<li name="selected_<s:property value="name" />"  class="selected" >
			</s:if>
			<s:else>
				<li name="selected_<s:property value="name" />">
			</s:else>
				
				<s:property value="name" />
				
				
				
			</li>
			<li class="classificationsLeaf closeLeaf">
			
				</li>
		</s:iterator>
	</ul>
</div>
<script type="text/javascript">

treeSelectedDisease = "";
	// Load a search at the start
	$('document').ready(function() {
		
		
		
		
		$('[name^="selected_"]').click(function(){
			
			if(!$(this).hasClass("selected")) {
				$(this).addClass("selected");
			}
			
			 name = $(this).attr('name');
			 nameArray = name.split("_");
			 treeSelectedDisease = nameArray[1].toString();
			 
			 $('#columnOne li').each(function(){$(this).removeClass("currentView");});
			 
			 $('.closeLeaf').hide("slow");
			 $(this).addClass("currentView");
			 $this = $(this);
				//populate domain list
	    		$.ajax("searchDataElementAction!updateDomain.ajax", {
	    			"type": 	"POST",
	    			"async": 	false,
	    			"data": 	{"selectedDiseases" : nameArray[1].toString()},
	    			"success": 	function(data) {
	    				
	    							$("#columnTwo").html(data);	
	    							
	    							//let's check to see if the user has made any selection changes for this disease
	    							
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
	        								$('#columnTwo [name="selectedAllDomains"]').prop("checked",false);
	        							}
	    							
	    							
	    						
	    						}
	    		});
			
    		var domainSelectArray = $('#columnTwo input[name="selectedDomains"]:checked').map(function() {
	    		return this.value;
			}).get();
    		
    		var domainSelect = (domainSelectArray.length > 0) ? domainSelectArray.toString() : "all";
  
	    		//populate sub-domain list
	    		$.ajax("searchDataElementAction!updateSubDomain.ajax", {
	    			"type": 	"POST",
	    			"async": 	false,
	    			"data": 	{"selectedDiseases" : treeSelectedDisease,
	    						 "selectedDomains" : domainSelect},
	    			"success": 	function(data) {
	    				
	    							$("#columnThree").html(data);	
	    							
	    							///has the user made selections in this session.
	    							
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
	        								$('#columnThree [name="allSubDomains"]').prop("checked",false);
	        							}
	    							
	    							
	    							
	    							
	    						}
	    		}); 
	    		
	    		
	    		
	    		////open classifications 
	    		$.ajax("searchDataElementAction!updateClassifications.ajax", {
					"type": 	"POST",
					"async": 	false,
					"data": 	{"activeDisease" : treeSelectedDisease},
					"success": 	function(data) {
						
						$('.currentView').next('li').html(data);	
								}
				});
				$('.currentView').next('li').show("slow",function(){});
				
			
			
		});
	
	});
	
</script>