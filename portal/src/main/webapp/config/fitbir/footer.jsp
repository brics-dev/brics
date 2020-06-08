<%@taglib prefix="s" uri="/struts-tags"%>

<div id="footer">
	<div class="content">
		<div class="contact">
			<h4>
				Federal Interagency<br /> Traumatic Brain Injury Research<br /> (FITBIR) Informatics System
			</h4>
			<br />
			<p>
				<strong>National Institutes of Health</strong><br /> <strong>Center for Information Technology</strong><br /> 12
				South Dr RM 2041<br /> Bethesda, MD 20892<br />
			</p>
			<p>
				<strong>Phone: </strong>				
				<s:property value="%{orgPhone}" />
				<br /> <s:a href="%{modulesPublicURL}content/contact-us"><span>Contact Us</span></s:a> <br />
			</p>

		</div>
		<div class="contact">
			<h5>
				U.S. Army Medical Research and Development Command - MRDC<br /> Combat Casualty Care Research Program
			</h5>
			<p>
				Website: <a href="https://ccc.amedd.army.mil" target="_blank" class="ext-link reverse">https://ccc.amedd.army.mil</a>
			</p>

			<h5>
				National Institutes of Health<br /> Center for Information Technology
			</h5>
			<p>
				Website: <a href="http://cit.nih.gov" class="ext-link reverse" title="Center for Information Technology, National Institutes of Health"
					target="_blank">http://cit.nih.gov</a> 
			</p>

			<h5>
				National Institutes of Health<br /> National Institute of Neurological Disorders &amp; Stroke
			</h5>
			<p>
				Website: <a href="http://www.ninds.nih.gov" title="National Institute of Neurological Disorders and Stroke"
					target="_blank" class="ext-link reverse">http://www.ninds.nih.gov</a>
			</p>
		</div>

		<div class="contact">
			<ul class="footer-links">
				<li><a href="https://www.cit.nih.gov/privacy-policy" target="_blank" class="ext-link reverse">Privacy</a></li>
				<li>|</li>
				<li><a href="https://www.cit.nih.gov/disclaimers" target="_blank" class="ext-link reverse">Disclaimer</a></li>
				<li>|</li>
				<li><a href="http://www.nih.gov/about/access.htm" target="_blank" class="ext-link reverse">Accessibility</a></li>
				<li>|</li>
				<li><a href="http://www.nih.gov/icd/od/foia/index.htm" target="_blank" class="ext-link reverse">FOIA</a></li>
			</ul>
			<ul class="footer-links footer-logos">
				<li><a target="_blank" href="http://cdmrp.army.mil/dmrdp/default.shtml"><img width="101" height="48" border="0" alt="DMRDP"
						src='<s:url value="/images/global/dmrdp_logo-bw.png"/>'></a></li>
				<li><a href="https://ccc.amedd.army.mil/" target="_blank"><img
						src='<s:url value="/images/global/MRDCLOGO_xsm.png"/>' border="0" alt="MRDC"/></a></li>
				
			</ul>
			<ul class="footer-links footer-logos">
				<li><a href="http://cit.nih.gov" target="_blank"><img
						src='<s:url value="/images/global/cit_logo-bw.png"/>' width="101" height="82" border="0" alt="CIT"/></a></li>
				<li><a href="http://ninds.nih.gov/" target="_blank"><img
						src='<s:url value="/images/global/ninds_logo-bw.png"/>' width="101" height="82" border="0" alt="NINDS"/></a></li>
					
			</ul>
			<ul class="footer-links footer-logos">
				
				<li><a href="http://www.hhs.gov/" target="_blank"><img
						src='<s:url value="/images/global/hhs_logo-bw.png"/>' width="50" height="50" border="0" alt="HHS"/></a></li>
				<li><a href="http://www.nih.gov/" target="_blank"><img
						src='<s:url value="/images/global/nih_logo-bw.png"/>' width="101" height="82" border="0" alt="NIH" /></a></li>
				<li><a href="http://www.usa.gov/" target="_blank"><img
						src='<s:url value="/images/global/usagov_logo-bw.png"/>' width="100" height="31" border="0" alt="USA.gov"/></a></li>	
			</ul>
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
	});
</script>

<script type="text/javascript">
function addBid(){
    var bidId = document.getElementById("bidId").value;
    // do whatever with bidId
}
</script>


