<%@taglib prefix="s" uri="/struts-tags"%>

<div id="footer">
	<div class="content line">
		<div class="unit size1of2">
			<p>
				<s:a href="https://nei.nih.gov/tools/policies"  target="_blank" class="ext-link reverse margin-right">Privacy Statement</s:a>
				<s:a href="http://www.nih.gov/about/access.htm" target="_blank" class="ext-link reverse margin-right">Accessibility Policy</s:a>
				<s:a href="http://www.nih.gov/icd/od/foia/index.htm" target="_blank" class="ext-link reverse margin-right">FOIA</s:a>
			</p>
			<p><em>NIH... Turning Discovery Into Health</em></p>
		</div>

		<div class="unit size1of2 lastUnit">
			<ul class="footer-links" style="float:right">
				<li><a href="http://www.hhs.gov/" target="_blank"><img
						src='<s:url value="/images/global/hhs_logo-bw.png"/>' width="50" height="50" border="0" alt="HHS"/></a></li>
				<li><a href="http://www.nih.gov/" target="_blank"><img
						src='<s:url value="/images/global/nih_logo-bw.png"/>' width="101" height="46" border="0" alt="NIH" style="position: relative" /></a></li>
				<li><a href="http://www.usa.gov/" target="_blank"><img
						src='<s:url value="/images/global/usagov_logo-bw.png"/>' width="100" height="31" border="0" alt="USA.gov"/></a></li>
			</ul>
		</div>	
        <div class="contact build-notes">
            <p>Version: <s:property value="%{buildID}" />
            <br>Repository ID: <s:property value="%{deploymentID}" />
            <br>Last Deployed: <s:property value="%{lastDeployed}" /></p>
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


