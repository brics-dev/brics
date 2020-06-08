<%@taglib prefix="s" uri="/struts-tags"%>

<div id="footer">
	<div class="content line">
		<div class="unit size1of2">
			<p><b>Biomedical Research<br>Informatics Computing System (BRICS) </b></p>
			<p>The National Institute of Neurological Disorders and Stroke (NINDS)<br>
			Data Repository
			</p>
		</div>

		<div class="unit size1of2 lastUnit">
			<ul class="footer-links" style="float:right; width:auto;">
				<li><a href="http://www.hhs.gov/" target="_blank"><img
						src='<s:url value="/images/global/hhs_logo-bw.png"/>' width="50" height="50" border="0" alt="HHS"/></a></li>
				<li><a href="http://www.nih.gov/" target="_blank"><img
						src='<s:url value="/images/global/nih_logo-bw.png"/>' width="101" height="46" border="0" alt="NIH" style="position: relative" /></a></li>
				
			</ul>
		</div>	
		<div style="float:right; color:#fff; ">
			<ul class="footer-links" style="width:auto;">
				<li><a href="https://pdbp-demo.cit.nih.gov/contact-us" target="_blank" class="ext-link reverse"><span>Contact Us</span></a> |</li>  
				<li><a href="https://pdbp-demo.cit.nih.gov/privacy"  target="_blank" class="ext-link reverse"><span>Privacy Statement</span></a> |</li> 
				<li><a href="https://www.nih.gov/web-policies-notices" target="_blank" class="ext-link reverse"><span>Accessibility Policy</span></a>  |</li>
				<li><a href="https://www.nih.gov/institutes-nih/nih-office-director/office-communications-public-liaison/freedom-information-act-office" target="_blank" class="ext-link reverse">FOIA</a>  |</li>
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


