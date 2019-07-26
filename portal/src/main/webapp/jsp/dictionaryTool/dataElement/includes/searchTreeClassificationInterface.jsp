<%@include file="/common/taglibs.jsp"%>
		<ul class="leaf">
	<s:iterator var="subgroup" value="classificationOptions">
		<s:if test="classificationOptions.size > 1">
			<li>
			
				<b><s:property value="#subgroup.key" /></b>
				<s:property value="#subgroup.disease" />
				
			
			</li>
		</s:if>
		<s:iterator var="classificationItem" value="#subgroup.value" status="status">
		<li>
		
		
		
		<input type="checkbox" name="<s:property value="#classificationItem.disease.name" />.<s:property value="#subgroup.key" />" value="<s:property value="#classificationItem.disease.name" />.<s:property value="#subgroup.key" />.<s:property value="#classificationItem.classification.name" />" class="classificationCheckBox classificationItem">
				<label for="<s:property value="#classificationItem.disease.name" />.<s:property value="#subgroup.key" />"><s:property value="#classificationItem.classification.name" /></label>
			</li>
		</s:iterator>
	</s:iterator>
		
	</ul>
		
<script type="text/javascript">

	// Load a search at the start
	$('document').ready(function() {
		
		//set scroll bar styles
		$(".leaf").mCustomScrollbar({
			theme:"inset-dark",
		    scrollButtons:{ enable: true },
		    autoHideScrollbar: true
		});
		//let's check to see if the user has made any selection changes for this disease
		
		if(classificationsArray.length > 0) {
			
			$('.classificationItem').each(
					function(){
						
						if($.inArray($(this).val(),classificationsArray) > -1) {
							
							$(this).prop('checked', true);
						} else {
							$(this).prop('checked', false);
						}
					});
		
		}
		
		
		$('.classificationItem').click(function(){
			
			if($(this).prop('checked')) {

				if($.inArray( $(this).val(), classificationsArray ) == -1) {
					classificationsArray.push($(this).val());
				}
				
			} else {
				
				//remove from array
				if($.inArray( $(this).val(), classificationsArray ) > -1) {
					index = $.inArray( $(this).val(), classificationsArray ) ;
					
					classificationsArray.splice(index, 1);
				}
				
			
			}
			
			
			
			
				
			
			
		});
	
	});
	
</script>
		
		
		