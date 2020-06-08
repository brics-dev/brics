<%@taglib prefix="s" uri="/struts-tags"%>

<div id="footer">
	<div class="content line">
		<div class="unit size1of2">
			<p>
				<s:a href="%{modulesPublicURL}contact" class="margin-right">Contact</s:a>
			</p>
		</div>
		
		<!-- Include the release info section -->
		<jsp:include page="/common/release-info.jsp" />
	</div>
</div>
<!-- end #footer -->

<script type="text/javascript">

$(document).ready(function(){ 
 	// Match all link elements with href attributes within the content div
  	$('div.missingPermission').each(function(){
	   $(this).qtip({
		      content: 'You currently do not have access to this tool; if you would like to request access, please visit the Privileges page in the Account Module.',
		      hide: {
		          fixed: true // Make it fixed so it can be hovered over
		       },
		      position: {
		    	  my: 'bottom left',
		    	  at: 'bottom right'
		     	}
		   	});
	   	});
 	
 	
	$('div.reporting.missingPermission').each(function(){
		   $(this).qtip({
			      content: 'You currently do not have access to this tool; if you would like to request access, please visit the Privileges page in the Account Module.',
			      hide: {
			          fixed: true // Make it fixed so it can be hovered over
			       },
			      position: {
			    	  my: 'bottom right',
			    	  at: 'bottom right'
			     	}
			   	});
	 });
});
</script>


