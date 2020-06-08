<%@taglib prefix="s" uri="/struts-tags"%>


<div id="footer">
	<div class="content">
		<div class="contact">
			<h4>
				Center for Neuroscience and Regenerative Medicine (CNRM)<br/>
				Data Repository
			</h4>
		</div>
		<div class="contact">
			<h5>
				Center for Neuroscience and Regenerative Medicine
			</h5>
			<p>Uniformed Services University of the Health Sciences
			4301 Jones Bridge Rd.<br/>
			Bethesda, MD 20814-4799<br/>
			Website: <a href="http://www.usuhs.mil/cnrm/index.html" target="_blank" class="ext-link reverse">http://www.usuhs.mil/cnrm/index.html</a></p>
		</div>
		<div class="contact">
			<ul class="footer-links">
				<li><s:a href="%{modulesPublicURL}jsp/about/contact-us.jsp"><span>Privacy</span></s:a> |
				<li><s:a href="%{modulesPublicURL}jsp/general/disclaimer.jsp"><span>Disclaimer</span></s:a> |
				<li><a href="http://www.nih.gov/about/access.htm" target="_blank" class="ext-link reverse">Accessibility</a></li> |
				<li><a href="http://www.nih.gov/icd/od/foia/index.htm" class="ext-link reverse" target="_blank">FOIA</a></li>
			</ul>
			<ul class="footer-links footer-logos">
				<li><a target="_blank" href="http://cdmrp.army.mil/dmrdp/default.shtml"><img width="101" height="48" border="0" alt="DMRDP"
						src='<s:url value="/images/global/dmrdp_logo-bw.png"/>'></a></li>
				<li><a href="http://mrmc.amedd.army.mil/" target="_blank"><img
						src='<s:url value="/images/global/mrmc_logo-bw.png"/>' width="101" height="40" border="0" alt="MRMC"/></a></li>
				
			</ul>
			<ul class="footer-links footer-logos">
				
				<li><a href="http://cit.nih.gov" target="_blank"><img
						src='<s:url value="/images/global/cit_logo-bw.png"/>' width="101" height="35" border="0" alt="CIT"/></a></li>
				<li><a href="http://ninds.nih.gov/" target="_blank"><img
						src='<s:url value="/images/global/ninds_logo-bw.png"/>' width="101" height="47" border="0" alt="NINDS"/></a></li>		
			</ul>
			<ul class="footer-links footer-logos">
				<li><a href="http://www.hhs.gov/" target="_blank"><img
						src='<s:url value="/images/global/hhs_logo-bw.png"/>' width="50" height="50" border="0" alt="HHS"/></a></li>
				<li><a href="http://www.nih.gov/" target="_blank"><img
						src='<s:url value="/images/global/nih_logo-bw.png"/>' width="101" height="46" border="0" alt="NIH" /></a></li>
				<li><a href="http://www.usa.gov/" target="_blank"><img
						src='<s:url value="/images/global/usagov_logo-bw.png"/>' width="100" height="31" border="0" alt="USA.gov"/></a></li>		
			</ul>
		</div>

        <!-- Include the release info section -->
		<jsp:include page="/common/release-info.jsp" />
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

