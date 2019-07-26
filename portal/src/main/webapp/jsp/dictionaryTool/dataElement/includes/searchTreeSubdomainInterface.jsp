<%@include file="/common/taglibs.jsp"%>

<div class="facet-form-field">
	<b>Sub-Domain:</b>
	
	
	<ul id="subdomainTree" class="tree">
	<li>
			<input type="checkbox" id="allSubDomains" name="allSubDomains" value="all" />
			<label for="allSubdomains">All</label>
		</li>
	<s:iterator var="domain" value="groupedSubDomains">
		<li><b>
		<s:property value="#domain.key.substring(#domain.key.indexOf('.')+1,#domain.key.length()).replace('.',' ')" /></b>
		</li>
		<s:iterator var="subdomain" value="#domain.value">
		<li>
		<input id="<s:property value="#domain.key" /><s:property value="subdomain" />" type="checkbox" name="selectedSubDomains" value="<s:property value="#domain.key" /><s:property value="subdomain" />">
				<s:label for="%{#domain.key + #subdomain}" value="%{subdomain}" />
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
		
		$('#columnThree input[type=checkbox]').click(function(){
			
			if($(this).prop('checked')) {
				
				//deselect all checkbutton if needed
				if($(this).attr('name') == 'selectedSubDomains') {
					$("#allSubDomains").prop("checked",false);
				}else if ($(this).attr('name') == 'allSubDomains') {
					//if the user selected all i need to removed array items in this list that were previously checked.
					$('input[name="selectedSubDomains"]:checked').each(function(){
						if($.inArray( $(this).val(), subDomainArray ) > -1) {
							index = $.inArray( $(this).val(), subDomainArray ) ;
							
							subDomainArray.splice(index, 1);
						}
					})
						
					$('input[name="selectedSubDomains"]:checked').prop("checked",false);			
					
				}
				
				//add select  class to disease list object
				if($("#columnThree input[type=checkbox]:checked").length > 0) {
					
					if(!$('[name="selected_'+treeSelectedDisease+'"]').hasClass("selected")) {
						$('[name="selected_'+treeSelectedDisease+'"]').addClass("selected");
						
					}
		        	
		        }

				if($.inArray( $(this).val(), subDomainArray ) == -1 && $(this).val() != 'all') {
					subDomainArray.push($(this).val());
				}
				
			} else {
				
				//remove from array
				if($.inArray( $(this).val(), subDomainArray ) > -1) {
					index = $.inArray( $(this).val(), subDomainArray ) ;
					
					subDomainArray.splice(index, 1);
				}
				
			
			}
			
			
		})
	});
</script>