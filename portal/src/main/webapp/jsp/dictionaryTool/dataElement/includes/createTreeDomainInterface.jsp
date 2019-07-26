<%@include file="/common/taglibs.jsp"%>

<div class="facet-form-field">
	<b>Domain:  <span class="required">*</span></b>
	<br>
	<br>
	<ul class="tree">
		<s:iterator value="domainOptions">
			<li>
				<s:checkbox id="%{value}" cssClass="subdomainCheckBox" name="selectedDomains" 
						fieldValue="%{value}" value="%{value in valueRangeForm.domainList}"  /> 
				<s:label for="%{value}"><s:property value="key" /></s:label>
			</li>
		</s:iterator>
	</ul>
</div>

<script type="text/javascript">

	// Load a search at the start
	$('document').ready(function() {

		$('#columnTwo input[type=checkbox]').click(function(){
			
			
			if($(this).prop('checked') && ($(this).attr('name') == 'selectedAllDomains')) {
				var domainSelect = 'all';
			} else {
				var domainSelect = $('input[name="selectedDomains"]:checked').map(function() {
		    		return this.value;
				}).get();
			}
			 
			
			if($(this).prop('checked')) {
				//deselect all checkbox if if needed

					if($.inArray( $(this).val() , domainArray ) == -1 && $(this).val() != 'all') {
						domainArray.push($(this).val());
					}
					
				
			} else {
				
				
				//remove domain from array, since it has been unchecked
				
						if($.inArray( $(this).val(), domainArray ) > -1) {
							index = $.inArray( $(this).val(), domainArray ) ;
							
							domainArray.splice(index, 1);
						}
					
				//deleselect all related subdomains
						$('#columnThree [id*="'+$(this).val()+'"]').each(function(){
							
							$(this).prop('checked',false);
							//remove from array
							if($.inArray( $(this).val(), subDomainArray ) > -1) {
								index = $.inArray( $(this).val(), subDomainArray ) ;
								
								subDomainArray.splice(index, 1);
							}
						});
				//since we deselected the subdomains make sure the classifications for a user role is deselected as well. 
						//this is for the user role, again probably a way better way to do this. but i need to make sure, classifications are sent for this domain.
						$('.classificationLeaf input[type="radio"]:disabled').each(function(){	
						
							var keepMe = false;
							//remove other classifications related to the subgroup from classifications array, there can only be one
							if( subDomainArray.length > 0) {
								diseaseName = $(this).val().split('.')[0];
								for (var i = 0; i < subDomainArray.length; i++) {
									
									if(subDomainArray[i].split('.')[0] == diseaseName) {
										keepMe = true;
										
									}
									
								}
								if(!keepMe) {
									index = $.inArray($(this).val(), classificationsArray ) ;
									classificationsArray.splice(index, 1);
								}
							}	else {
								//remove classification since subdomain array is empty
								index = $.inArray($(this).val(), classificationsArray ) ;
								classificationsArray.splice(index, 1);
								
							}			
							  //after selection assign value to hidden form fields
						      $('[name="valueRangeForm.classificationElementList"]').val(classificationsArray); //domains
					      
						});
			
			}
			
			
			
			
			//populate sub-domain list
     		$.ajax("createDataElementAction!updateSubDomain.ajax", {
     			"type": 	"POST",
     			"async": 	false,
     			"data": 	{"selectedDiseases" : treeSelectedDisease,
     						 "selectedDomains" : domainSelect.toString()},
     			"success": 	function(data) {
     				
     							$("#columnThree").html(data);	
     							
     							//let's check to see if the user has made any selection changes for this disease
    							
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
    								
    								
    								var domainCheckBoxes = $('#columnTwo input[type="checkbox"]');
    								var subDomainCheckBoxes = $('#columnThree input[type="checkbox"]');
    								
    								
    								
    								$('#columnThree input[type="checkbox"]').change(function(event) {
    									
	    								//add this disease to list of chosen
  								      // State has changed to checked/unchecked.
  								      if($(this).prop('checked')) {
  								    	  if($.inArray( treeSelectedDisease , chosenDiseases ) == -1) {
  								    		  chosenDiseases.push(treeSelectedDisease);
  											}
	  								    	//add select  class to disease list object
	  	    								if($("#columnTwo input[type=checkbox]:checked").length > 0) {
	  	    									
	  	    									if(!$('[name="selected_'+treeSelectedDisease+'"]').hasClass("selected")) {
	  	    										$('[name="selected_'+treeSelectedDisease+'"]').addClass("selected");
	  	    										
	  	    									}
	  	    						        	
	  	    						        }
  								    	  
  								      } else { ///ok now check to see if any other domains or subdomains are checked, if not remove disase from list in necessary
  								    	  
  								    	  if(!domainCheckBoxes.is(":checked") && !subDomainCheckBoxes.is(":checked")) {
  								    		  //remove disease from array if there are no domains and subdomains selected
  								    		  if($.inArray( treeSelectedDisease,chosenDiseases ) > -1) {
														index = $.inArray(treeSelectedDisease, chosenDiseases ) ;
						
														chosenDiseases.splice(index, 1);
													}
  								    	  }
  								      
  								    	//remove select  class to disease list object
  										if($("#columnTwo input[type=checkbox]:checked").length <= 0) {
  											
  											if($('[name="selected_'+treeSelectedDisease+'"]').hasClass("selected")) {
  												$('[name="selected_'+treeSelectedDisease+'"]').removeClass("selected");
  												
  											}
  								        	
  								        }
  								      
  								      }
  								    
  								});
  							
    							
     						}
     		}); 
			
			
     		
		});
		
		
	
	});
	
</script>