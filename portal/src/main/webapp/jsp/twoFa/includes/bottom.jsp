<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
               	</div>
               </div> 
               <div id="footerDiv">
                <div id="footer" class="fl-panel fl-note fl-bevel-white fl-font-size-80">
                	<div class="content">
<c:set var="styleKey"><spring:eval expression="@applicationProperties.getProperty('modules.style.key')" /></c:set>
     <c:choose>
        <c:when test="${fn:contains(styleKey, 'cnrm' )}">
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
						<div class="unit size1of2 lastUnit">
							<ul class="footer-links">
							<li><a href="http://www.hhs.gov/" target="_blank"><img
							src="/cas/images/global/hhs_logo-bw.png" width="50" height="50" border="0" alt="HHS"/></a></li>
							<li><a href="http://www.nih.gov/" target="_blank"><img
							src="/cas/images/global/nih_logo-bw.png" width="101" height="46" border="0" alt="NIH" style="position: relative; top: 35px;" /></a></li>
							<li><a href="http://www.usa.gov/" target="_blank"><img
							src="/cas/images/global/usagov_logo-bw.png" width="100" height="31" border="0" alt="USA.gov"/></a></li>
							</ul>
						</div>	
						<a id="jasig" href="http://www.jasig.org" title="go to Jasig home page"></a>
	                    <div id="copyright">
	                        <p>Copyright &copy; 2005 - 2010 Jasig, Inc. All rights reserved.</p>
	                        <p>Powered by <a href="http://www.jasig.org/cas">Jasig Central Authentication Service <%=org.jasig.cas.CasVersion.getVersion()%></a></p>
	                    </div>
        </c:when>
		 <c:when test="${fn:contains(styleKey, 'pdbp' )}">
	        <div class="section">
       			<div class="unit size2of3">
					<p>
						<a class="footerLink" href="${portalUrl}/sitemap">Site Map</a>
						<a class="footerLink" href="${portalUrl}/contact-us">Contact Us</a>
						<a class="footerLink" href="${portalUrl}/privacy">Privacy Statement</a>
						<a class="ext-link reverse" target="_blank" href="http://www.nih.gov/about/access.htm">Accessibility Policy</a>
						<a class="ext-link reverse" target="_blank" href="http://www.nih.gov/icd/od/foia/index.htm">FOIA</a>
					</p>
					<p><em>NIH... Turning Discovery Into Health</em></p>
				</div>
				<div class="unit size1of3 lastUnit">
					<ul class="footer-links">
						<li><a href="http://www.hhs.gov/" target="_blank"><img
								src="/cas/images/global/hhs_logo-bw.png" width="50" height="50" border="0" alt="HHS"/></a></li>
						<li><a href="http://www.nih.gov/" target="_blank"><img
								src="/cas/images/global/nih_logo-bw.png" width="101" height="46" border="0" alt="NIH" style="position: relative; top: 35px;" /></a></li>
						<li><a href="http://www.usa.gov/" target="_blank"><img
								src="/cas/images/global/usagov_logo-bw.png" width="100" height="31" border="0" alt="USA.gov"/></a></li>
					</ul>
				</div>	
			<div class="clear-both"></div>
					</div>
					
					<div class="clear-both">
						<a id="jasig" href="http://www.jasig.org" title="go to Jasig home page"></a>
	                    <div id="copyright">
	                        <p>Copyright &copy; 2005 - 2010 Jasig, Inc. All rights reserved.</p>
	                        <p>Powered by <a href="http://www.jasig.org/cas">Jasig Central Authentication Service <%=org.jasig.cas.CasVersion.getVersion()%></a></p>
	                    </div>
                    </div> 
          
        </c:when>
        <c:when test="${fn:contains(styleKey, 'fitbir' )}">
        	
        				<div class="contact">
							<div class="section">
								<h4>
									Federal Interagency<br />
									Traumatic Brain Injury Research
									<br/>(FITBIR) Informatics System						
								</h4>
							</div>
							<div class="section">	
								<h5>National Institutes of Health <br />Center for Information Technology</h5>
								<p>12 South Dr RM 2041 <br />Bethesda, MD 20892</p>
								<p>
									<br /> <a href="${portalUrl}/content/contact-us"><span>Contact Us</span></a> <br />
								</p>
							</div>
						</div>
						<div class="contact">
							<div class="section">
								<h5>U.S. Army Medical Research and Development Command - MRDC<br />Combat Casualty Care Research Program</h5>
								<p>Website: <a class="ext-link reverse" href="https://ccc.amedd.army.mil" target="_blank">https://ccc.amedd.army.mil</a></p>
							</div>
							<div class="section">
								<h5>National Institutes of Health <br />Center for Information Technology</h5>
								<p>Website: <a href="http://cit.nih.gov" target="_blank" class="ext-link reverse">http://cit.nih.gov</a>
							</div>
							<div class="section">
								<h5>National Institutes of Health <br />National Institute of Neurological Disorders & Stroke</h5>
								<p>Website: <a href="http://www.ninds.nih.gov" target="_blank" class="ext-link reverse">http://www.ninds.nih.gov </a>
							</div>							
						</div>
						<div class="contact">
							<ul class="footer-links">
								<li><a href="https://www.cit.nih.gov/privacy-policy" target="_blank" class="ext-link reverse">Privacy</a></li>
								<li>|</li>
								<li><a href="https://www.cit.nih.gov/disclaimers" target="_blank" class="ext-link reverse">Disclaimer</a></li>
								<li>|</li>
								<li><a href="http://www.nih.gov/about/access.htm" target="_blank" class="ext-link reverse">Accessibility</a></li>
								<li>|</li>
								<li><a href="http://www.nih.gov/icd/od/foia/index.htm" class="ext-link reverse" target="_blank">FOIA</a></li>
							</ul>
							<ul class="footer-links">
								<li><a target="_blank" href="http://cdmrp.army.mil/dmrdp/default.shtml"><img width="101" height="48" border="0" alt="DMRDP"
										src="/cas/images/global/dmrdp_logo-bw.png"></a></li>
								<li><a href="https://ccc.amedd.army.mil/" target="_blank"><img
										src='/cas/images/global/MRDCLOGO_xsm.png' border="0" alt="MRDC"/></a></li>
							</ul>
							<ul class="footer-links">
								<li><a href="http://cit.nih.gov" target="_blank"><img
										src='/cas/images/global/cit_logo-bw.png' width="101" height="35" border="0" alt="CIT"/></a></li>
								<li><a href="http://ninds.nih.gov/" target="_blank"><img
										src='/cas/images/global/ninds_logo-bw.png' width="101" height="47" border="0" alt="NINDS"/></a></li>		
							</ul>
							<ul class="footer-links">
								<li><a href="http://www.hhs.gov/" target="_blank"><img
										src='/cas/images/global/hhs_logo-bw.png' width="50" height="50" border="0" alt="HHS"/></a></li>
								<li><a href="http://www.nih.gov/" target="_blank"><img
										src='/cas/images/global/nih_logo-bw.png' width="101" height="46" border="0" alt="NIH"/></a></li>
								<li><a href="http://www.usa.gov/" target="_blank"><img
										src='/cas/images/global/usagov_logo-bw.png' width="100" height="31" border="0" alt="USA.gov"/></a></li>		
							</ul>
						</div>
						<a id="jasig" href="http://www.jasig.org" title="go to Jasig home page"></a>
	                    <div id="copyright">
	                        <p>Copyright &copy; 2005 - 2010 Jasig, Inc. All rights reserved.</p>
	                        <p>Powered by <a href="http://www.jasig.org/cas">Jasig Central Authentication Service <%=org.jasig.cas.CasVersion.getVersion()%></a></p>
	                    </div> 
        </c:when>
         <c:when test="${fn:contains(styleKey, 'cistar' )}">
        
		<div class="contact">
			<h4>
				Clinical Informatics System for Trials and Research <br /> (CISTAR)  System
			</h4>
		<!--  <br />
			<p>
				<strong>National Institutes of Health</strong><br /> <strong>Center for Information Technology</strong><br /> 12
				South Dr RM 2041<br /> Bethesda, MD 20892<br />
			</p>
			-->	

		</div>
		<div class="contact">
			
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
			
		
			<ul class="footer-links footer-logos">
				<li><a href="http://cit.nih.gov" target="_blank"><img
						src='/cas/images/global/cit_logo-bw.png' width="101" height="82" border="0" alt="CIT"/></a></li>
				<li><a href="http://ninds.nih.gov/" target="_blank"><img
						src='/cas/images/global/ninds_logo-bw.png' width="101" height="82" border="0" alt="NINDS"/></a></li>
					
			</ul>
			<ul class="footer-links footer-logos">
				
				<li><a href="http://www.hhs.gov/" target="_blank"><img
						src='/cas/images/global/hhs_logo-bw.png' width="50" height="50" border="0" alt="HHS"/></a></li>
				<li><a href="http://www.nih.gov/" target="_blank"><img
						src='/cas/images/global/nih_logo-bw.png' width="101" height="82" border="0" alt="NIH" /></a></li>
				<li><a href="http://www.usa.gov/" target="_blank"><img
						src='/cas/images/global/usagov_logo-bw.png' width="100" height="31" border="0" alt="USA.gov"/></a></li>	
			</ul>
			
			
		</div>
		

		<div class="contact build-notes">
			<p>Build Version:<s:property value="%{deploymentVersion}" /></p>
		</div>
		
						
        </c:when>
         <c:when test="${fn:contains(styleKey, 'gsdr' )}">
        
		<div class="contact">
			<h4>
				Global Stroke Data Repository <br /> (GSDR)  System
			</h4>
		</div>
		<div class="contact">
			
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
			
		
			<ul class="footer-links footer-logos">
				<li><a href="http://cit.nih.gov" target="_blank"><img
						src='/cas/images/global/cit_logo-bw.png' width="101" height="82" border="0" alt="CIT"/></a></li>
				<li><a href="http://ninds.nih.gov/" target="_blank"><img
						src='/cas/images/global/ninds_logo-bw.png' width="101" height="82" border="0" alt="NINDS"/></a></li>
					
			</ul>
			<ul class="footer-links footer-logos">
				
				<li><a href="http://www.hhs.gov/" target="_blank"><img
						src='/cas/images/global/hhs_logo-bw.png' width="50" height="50" border="0" alt="HHS"/></a></li>
				<li><a href="http://www.nih.gov/" target="_blank"><img
						src='/cas/images/global/nih_logo-bw.png' width="101" height="82" border="0" alt="NIH" /></a></li>
				<li><a href="http://www.usa.gov/" target="_blank"><img
						src='/cas/images/global/usagov_logo-bw.png' width="100" height="31" border="0" alt="USA.gov"/></a></li>	
			</ul>
			
			
		</div>
		

		<div class="contact build-notes">
			<p>Build Version:<s:property value="%{deploymentVersion}" /></p>
		</div>
		
						
        </c:when>
        <c:when test="${fn:contains(styleKey, 'ninds' )}">
        	<div class="clearfix">
				<div style="width: 55%; float: left;">
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
				<div style="width: 45%; float: right;">
					<ul class="footer-links float-right" style="width:100%">
						<li><a href="http://www.ninds.nih.gov/" target="_blank"><img
								src='images/global/ninds.png' width="188" height="40" border="0" alt="NINDS"/></a></li>
						<li><a href="http://www.hhs.gov/" target="_blank"><img
								src='images/global/hhs.gif' width="48" height="55" border="0" alt="HHS" /></a></li>
						<li><a href="http://www.usa.gov/" target="_blank"><img
								src='images/global/usagov.gif' width="160" height="55" border="0" alt="USA.gov"/></a></li>	
					</ul>
				</div>
			</div>
			<div class="build-notes">
				<p>Build Version:<s:property value="%{deploymentVersion}" /></p>
			</div>
        </c:when>
        <c:when test="${fn:contains(styleKey, 'eyegene' ) || fn:contains(styleKey, 'nei' )}">
        	  <div class="section">
       			<div class="contact" style="width:327px">
					<p>
						<a href="https://nei.nih.gov/tools/policies"  target="_blank" class="ext-link reverse margin-right">Privacy Statement</a>
						<a href="http://www.nih.gov/about/access.htm" target="_blank" class="ext-link reverse margin-right">Accessibility Policy</a>
						<a href="http://www.nih.gov/icd/od/foia/index.htm" target="_blank" class="ext-link reverse margin-right">FOIA</a>
					</p>
					<p><em>NIH... Turning Discovery Into Health</em></p>
				</div>
				<div class="unit size1of2 lastUnit">
					<ul class="footer-links">
						<li><a href="http://www.hhs.gov/" target="_blank"><img
								src="/cas/images/global/hhs_logo-bw.png" width="50" height="50" border="0" alt="HHS"/></a></li>
						<li><a href="http://www.nih.gov/" target="_blank"><img
								src="/cas/images/global/nih_logo-bw.png" width="101" height="46" border="0" alt="NIH" /></a></li>
						<li><a href="http://www.usa.gov/" target="_blank"><img
								src="/cas/images/global/usagov_logo-bw.png" width="100" height="31" border="0" alt="USA.gov"/></a></li>
					</ul>
				</div>	
				<div class="clear-both"></div>
			</div>
        </c:when>
        <c:when test="${fn:contains(styleKey, 'cdrns' )}">
        	  <div class="section">
      			<div class="unit size1of2">
					<p>
						<a href="${portalUrl}/contact" class="margin-right">Contact Us</a>
						<a href="https://www.ninr.nih.gov/site-structure/policies#privacy" target="_blank" class="margin-right ext-link reverse">Privacy Statement</a>
						<a href="http://www.nih.gov/about/access.htm" target="_blank" class="margin-right ext-link reverse">Accessibility Policy</a>
						<a href="http://www.nih.gov/icd/od/foia/index.htm" target="_blank" class="margin-right ext-link reverse">FOIA</a>
					</p>
					<p><em>NIH... Turning Discovery Into Health</em></p>
				</div>
				<div class="unit size1of2 lastUnit">
					<ul class="footer-links" style="float: right;">
						<li><a href="http://www.hhs.gov/" target="_blank"><img
								src="/cas/images/global/hhs_logo-bw.png" width="50" height="50" border="0" alt="HHS"/></a></li>
						<li><a href="http://www.nih.gov/" target="_blank"><img
								src="/cas/images/global/nih_logo-bw.png" width="101" height="46" border="0" alt="NIH" style="position: relative;" /></a></li>
						<li><a href="http://www.usa.gov/" target="_blank"><img
								src="/cas/images/global/usagov_logo-bw.png" width="100" height="31" border="0" alt="USA.gov"/></a></li>
					</ul>
				</div>	
				<div class="clear-both"></div>
			</div>
        </c:when>
        <c:when test="${fn:contains(styleKey, 'nti' )}">
			<div class="section">
				<div class="contact">
					<h4>
						National Trauma Research Repository (NTRR)<br/>
					</h4>
				</div>
			<div class="clear-both"></div>
			</div>
			<div class="clear-both">
				<div class="build-notes">
					<p>Build Version:<s:property value="%{deploymentVersion}" /></p>
				</div>
			</div> 
        </c:when>
        <c:otherwise>
        <!--  same as build-brics -->
	        <div class="section">
	          			<div class="contact">
								<h4>
									 Biomedical Research Informatics Computing System
								</h4>
							</div>
									<div class="unit size1of2 lastUnit">
				<ul class="footer-links">
					<li><a href="http://www.hhs.gov/" target="_blank"><img
							src="/cas/images/global/hhs_logo-bw.png" width="50" height="50" border="0" alt="HHS"/></a></li>
					<li><a href="http://www.nih.gov/" target="_blank"><img
							src="/cas/images/global/nih_logo-bw.png" width="101" height="46" border="0" alt="NIH" style="position: relative; top: 35px;" /></a></li>
					<li><a href="http://www.usa.gov/" target="_blank"><img
							src="/cas/images/global/usagov_logo-bw.png" width="100" height="31" border="0" alt="USA.gov"/></a></li>
				</ul>
			</div>	
			<div class="clear-both"></div>
					</div>
					
					<div class="clear-both">
						<a id="jasig" href="http://www.jasig.org" title="go to Jasig home page"></a>
	                    <div id="copyright">
	                        <p>Copyright &copy; 2005 - 2010 Jasig, Inc. All rights reserved.</p>
	                        <p>Powered by <a href="http://www.jasig.org/cas">Jasig Central Authentication Service <%=org.jasig.cas.CasVersion.getVersion()%></a></p>
	                    </div>
                    </div> 
          
        </c:otherwise>
       
	</c:choose>
	
	
	 
					</div>
                </div>
            </div>
        </div>	        
        <script type="text/javascript" src="https://ajax.googleapis.com/ajax/libs/jquery/1.4.2/jquery.min.js"></script>
        <script type="text/javascript" src="https://ajax.googleapis.com/ajax/libs/jqueryui/1.8.5/jquery-ui.min.js"></script>
        <script type="text/javascript" src="<c:url value="/js/cas.js" />"></script>
        <script type="text/javascript" src="<c:url value="/js/browser-check.js" />"></script>
    </body>
</html>

<script type="text/javascript">
			$("#loginUrl").click(function (){
				var loginURL ="${portalUrl}/cas/login?service=";
				var serviceUrl = '${portalUrl}/portal/j_spring_cas_security_check';
				var finalURL = loginURL+ encodeURIComponent(serviceUrl);
				$(location).attr('href',finalURL);
				return false;
			});
		</script>