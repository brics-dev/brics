<%@include file="/common/taglibs.jsp"%>

<div class="facet-form-field">
	<b>Domain: </b>
	<ul class="tree">
	<s:if test="%{!createDataElement}">
		<li>
			<input type="checkbox" id="allDomains" name="selectedAllDomains" value="all"  class="subdomainCheckBox" />
			<label for="allDomains">All</label>
		</li>
	</s:if>
		<s:iterator value="domainOptions">
			<li>
				<s:checkbox id="domain%{key}" cssClass="subdomainCheckBox" name="selectedDomains" 
						fieldValue="%{value}" value="%{value in sessionCriteria.domains}"  /> 
				
				<s:label for="domain%{key}" value="%{key}" />
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
				//deselect all checkbox if needed
				if($(this).attr('name') == 'selectedDomains') {
					
					$('input[name="selectedAllDomains"]:checked').prop("checked",false);
				} else if ($(this).attr('name') == 'selectedAllDomains') {
					//if the user selected all i need to removed array items in this list that were previously checked.
					$('input[name="selectedDomains"]:checked').each(function(){
						if($.inArray( $(this).val(), domainArray ) > -1) {
							index = $.inArray( $(this).val(), domainArray ) ;
							
							domainArray.splice(index, 1);
						}
					})
						
					$('input[name="selectedDomains"]:checked').prop("checked",false);			
					
				}
			
				//add select  class to disease list object
				if($("#columnTwo input[type=checkbox]:checked").length > 0) {
					
					if(!$('[name="selected_'+treeSelectedDisease+'"]').hasClass("selected")) {
						$('[name="selected_'+treeSelectedDisease+'"]').addClass("selected");
						
					}
		        	
		        }
				
				
					if($.inArray( $(this).val() , domainArray ) == -1 && $(this).val() != 'all') {
						domainArray.push($(this).val());
					}
					
				
			} else {
				
				//remove select  class to disease list object
				if($("#columnTwo input[type=checkbox]:checked").length <= 0) {
					
					if($('[name="selected_'+treeSelectedDisease+'"]').hasClass("selected")) {
						$('[name="selected_'+treeSelectedDisease+'"]').removeClass("selected");
						
					}
		        	
		        }
				//remove domain from array, since it has been unchecked
				
				
						if($.inArray( $(this).val(), domainArray ) > -1) {
							index = $.inArray( $(this).val(), domainArray ) ;
							
							domainArray.splice(index, 1);
						}
					
			
			}
			
			
			
			
			//populate sub-domain list
     		$.ajax("searchDataElementAction!updateSubDomain.ajax", {
     			"type": 	"POST",
     			"async": 	false,
     			"data": 	{"selectedDiseases" : treeSelectedDisease,
     						 "selectedDomains" : domainSelect.toString()
     						},
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
    							
     						}
     		}); 
			
			
     		
		})
	
	})
	
</script>