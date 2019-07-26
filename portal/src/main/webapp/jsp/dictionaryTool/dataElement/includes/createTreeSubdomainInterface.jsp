<%@include file="/common/taglibs.jsp"%>
<s:set var="tempTitle" value="" />
<div class="facet-form-field">
	<b>Sub-Domain: <span class="required">*</span></b>
	<br><br>
	<ul id="subdomainTree" class="tree">
	<s:iterator var="domain" value="groupedSubDomains">
		<li><b>
		<s:property value="#domain.key.substring(#domain.key.indexOf('.')+1,#domain.key.length()).replace('.',' ')" /></b>
		</li>
		<s:iterator var="subdomain" value="#domain.value">
		<li>
		
		<s:checkbox id="%{#domain.key + #subdomain}" cssClass="subdomainCheckBox" name="selectedSubDomains" 
						fieldValue="%{#domain.key + #subdomain}" value="%{#domain.key + #subdomain in valueRangeForm.domainList}"  />
		<%-- <input id="<s:property value="#domain.key" /><s:property value="subdomain" />" type="checkbox" name="selectedSubDomains" value="<s:property value="#domain.key" /><s:property value="subdomain" />">
		--%>
				<s:label for="%{#domain.key + #subdomain}" key="subdomain" />
				
				
			</li>
		</s:iterator>
	</s:iterator>
		
	</ul>
</div>
<script type="text/javascript">

	// Load a search at the start
	$('document').ready(function() {
		//set scroll bar styles
		$("#subdomainTree").mCustomScrollbar({
			theme:"inset-dark",
		    scrollButtons:{ enable: true },
		    autoHideScrollbar: true
		});
		
		$('#columnThree input[type=checkbox]').change(function(){
			
			if($(this).prop('checked')) {
			
				
				//add select  class to disease list object
				if($("#columnThree input[type=checkbox]:checked").length > 0) {
					
					if(!$('[name="selected_'+treeSelectedDisease+'"]').hasClass("selected")) {
						$('[name="selected_'+treeSelectedDisease+'"]').addClass("selected");
						
					}
		        	
		        }

				if($.inArray( $(this).val(), subDomainArray ) == -1 && $(this).val() != 'all') {
					subDomainArray.push($(this).val());
				}
				
				//this is for the user role, again probably a way better way to do this. but i need to make sure, classifications are sent for this domain.
				$('.classificationLeaf input[type="radio"]:disabled').each(function(){	
				
					//remove other classifications related to the subgroup from classifications array, there can only be one
					if( classificationsArray.length > 0) {
						subgroup = $(this).val().split('.')[1];
						for (var i = 0; i < classificationsArray.length; i++) {
							
							if(classificationsArray[i].split('.')[1] == subgroup) {
								
								index = $.inArray(classificationsArray[i], classificationsArray ) ;
								classificationsArray.splice(index, 1);
							}
							
						}
					}
		
					if($.inArray( $(this).val(), classificationsArray ) == -1) {
						
						classificationsArray.push($(this).val());
					}
					
					  //after selection assign value to hidden form fields
				      $('[name="valueRangeForm.classificationElementList"]').val(classificationsArray); //domains
			      
				});
				
				
				
				
			} else {
				
				//remove from array
				if($.inArray( $(this).val(), subDomainArray ) > -1) {
					index = $.inArray( $(this).val(), subDomainArray ) ;
					
					subDomainArray.splice(index, 1);
				}
				
				
				
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
			
			/// I added this so this interface works with the create dataElement Crud 
			var domainCheckBoxes = $('#columnTwo input[type="checkbox"]');
			var subDomainCheckBoxes = $('#columnThree input[type="checkbox"]');
					///this will add the selected disease to the chosenDisease array if the user picks this 								
			      // State has changed to checked/unchecked.
			      if($(this).prop('checked')) {
			    	  if($.inArray( treeSelectedDisease , chosenDiseases ) == -1) {
			    		  chosenDiseases.push(treeSelectedDisease);
						}
			    	  
			      } else { ///ok now check to see if any other domains or subdomains are checked, if not remove disase from list in necessary
			    	  
			    	  if(!domainCheckBoxes.is(":checked") && !subDomainCheckBoxes.is(":checked")) {
			    		  //remove disease from array if there are no domains and subdomains selected
			    		  if($.inArray( treeSelectedDisease,chosenDiseases ) > -1) {
					index = $.inArray(treeSelectedDisease, chosenDiseases ) ;
		
					chosenDiseases.splice(index, 1);
				}
			    	  }
			      
			      }
			  	subDomainArrayList = subDomainArray.join(';');
			      //after selection assign value to hidden form fields
			      $('[name="valueRangeForm.subdomainList"]').val(subDomainArrayList); //domains
			     
			
			
		})
	});
</script>