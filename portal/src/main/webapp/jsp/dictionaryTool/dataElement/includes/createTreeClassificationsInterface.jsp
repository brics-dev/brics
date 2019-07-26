<%@include file="/common/taglibs.jsp"%>

<s:iterator var="var" value="valueRangeForm.classificationElementList">
<s:property value="#var" />
</s:iterator>
<ul class="leaf classificationLeaf">
	<s:iterator var="subgroup" value="classificationOptions">
		<s:if test="classificationOptions.size > 1">
			<li>
			
				<b><s:property value="#subgroup.key" /></b>
				<s:property value="#subgroup.disease" />
				
			
			</li>
		</s:if>
		
		<s:set var = "breakLoop" value = "%{false}" />
		<s:iterator var="classificationItem" value="#subgroup.value" status="status">
		<s:if test="%{(!isAdmin && #classificationItem.classification.name == 'Supplemental' && !#breakLoop) || isAdmin }">
		<li>

		
		<input <s:if test="!isAdmin || #isPublished">disabled = "disabled" checked="checked"</s:if>  id="<s:property value="#classificationItem.disease.name" />.<s:property value="#subgroup.key" />.<s:property value="#classificationItem.classification.name" />" type="radio" name="<s:property value="#classificationItem.disease.name" />.<s:property value="#subgroup.key" />" value="<s:property value="#classificationItem.disease.name" />.<s:property value="#subgroup.key" />.<s:property value="#classificationItem.classification.name" />" class="classificationItem">
				<label for="<s:property value="#classificationItem.disease.name" />.<s:property value="#subgroup.key" />"><s:property value="#classificationItem.classification.name" /></label>
			</li>
				<s:if test="!isAdmin && #classificationItem.classification.name == 'Supplemental'">
					<s:set var = "breakLoop" value = "%{true}"/>
				</s:if>
			</s:if>
			
		</s:iterator>
	</s:iterator>
		
	</ul>


						<script type="text/javascript">

	// Load a search at the start
	$('document').ready(function() {
		
		//set scroll bar styles
		$(".classificationLeaf").mCustomScrollbar({
			theme:"inset-dark",
		    scrollButtons:{ enable: true },
		    autoHideScrollbar: true
		});
		
		//this is used for the user role.
		$('.classificationLeaf input[type="radio"]:disabled').each(function(){	
			
			
			/*if(!$(this).prop('checked')) {
				$(this).prop('checked',true);
			}*/
		});
		
		$('.classificationLeaf input[type="radio"]').click(function(){
			
			var previousValue = "";
			
			previousValue = $(this).attr('previousValue');
			
		
			// this enables radio button to behave like a check box
			  if (previousValue == 'checked')
			  {
				  
			    //$(this).removeAttr('checked');
			    $(this).prop('checked',false)
			    $(this).attr('previousValue', false);
			  }
			  else
			  {
				 
				 $(this).prop('checked',true)
			   $(".classificationItem").attr('previousValue', false);
			    $(this).attr('previousValue', 'checked');
			  }
			 
			
			
			
			  
			if($(this).prop('checked')) {
				
				
				
				///this is specific for TBI, if user has chosen core then all classifications must be core
				
				//I can probably make this block smaller, something to look into
				
				var classificationNameArray =  $(this).val().split('.');
				if(classificationNameArray[0] == 'Traumatic Brain Injury') {
					if(classificationNameArray[2] == "Core") {
						
						$('.classificationLeaf input[type="radio"][id*="Core"]').each(function(){
							
							$(this).prop('checked',true);
							
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
							
							
							
							
							
						
						});
					}else {
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
					}
				} else {
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
				}
				
			} else {
				
				//remove from array
				if($.inArray( $(this).val(), classificationsArray ) > -1) {
					index = $.inArray( $(this).val(), classificationsArray ) ;
					
					classificationsArray.splice(index, 1);
				}
				
			
			}
			
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
		      
		      //after selection assign value to hidden form fields
		      $('[name="valueRangeForm.classificationElementList"]').val(classificationsArray); //domains
		      		
		     
		      
		     
			
		});
		
		
		
	
	});
	
</script>