<%@taglib prefix="s" uri="/struts-tags"%>

<div id="footer">
	<div class="content">
		<div class="line">
			<div class="unit size1of2">
				<ul class="footer-links" style="width: 100%">
					<li><a href="http://www.commondataelements.ninds.nih.gov/ProjReview.aspx#tab=Introduction">Project Overview</a> |</li>
					<li><a href="http://www.commondataelements.ninds.nih.gov/Contact.aspx">Contact</a> |</li>
					<li><a href="http://www.ninds.nih.gov/privacy.htm"><span>Privacy Statement</span></a> |</li>
					<li><a href="http://ninds.nih.gov"><span>NINDS</span></a> |</li>
					<li><a href="http://www.nih.gov">NIH</a> |</li>
					<li><a href="http://www.hhs.gov">HHS</a> |</li>
					<li><a href="http://www.usa.gov">USA.gov</a></li>
				</ul>
			</div>
			<div class="unit size1of2">
				<ul class="footer-links float-right" style="width:100%">
					<li><a href="http://www.ninds.nih.gov/" target="_blank"><img
							src='<s:url value="/images/global/ninds.png"/>' width="188" height="40" border="0" alt="NINDS"/></a></li>
					<li><a href="http://www.hhs.gov/" target="_blank"><img
							src='<s:url value="/images/global/hhs.gif"/>' width="48" height="55" border="0" alt="HHS" /></a></li>
					<li><a href="http://www.usa.gov/" target="_blank"><img
							src='<s:url value="/images/global/usagov.gif"/>' width="160" height="55" border="0" alt="USA.gov"/></a></li>	
				</ul>
			</div>
		</div>
		<div class="build-notes float-right">
			<p>Build Version:<s:property value="%{deploymentVersion}" /></p>
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

<script type="text/javascript">
function addBid(){
    var bidId = document.getElementById("bidId").value;
    // do whatever with bidId
}
</script>


