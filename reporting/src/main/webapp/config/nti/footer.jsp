<%@taglib prefix="s" uri="/struts-tags"%>

<div id="footer">
	<div class="content line">
		<div class="unit size1of2">
			<p>
				<s:a href="%{modulesPublicURL}contact" class="margin-right">Contact</s:a>
			</p>
		</div>
		<div class="build-notes clear-both line">
			<p class="right">Build Version:<s:property value="%{deploymentVersion}" /></p>
		</div>
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
});
</script>


