<%@ taglib uri="/struts-tags" prefix="s" %>
<s:set var="bricsHomeUrl" value="#systemPreferences.get('brics.modules.home.url')" />
					</div>
				</div>
				<div id="leftColumn">
					<jsp:include page="/common/leftNav.jsp" />
				</div>
				<br class="clearfix" />
			</div>
		</div>
	</div>
	
	<div id="footerDiv">
				<!-- FOOTER -->
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
								301-594-3532
								<br /> <a href="<s:property value="#bricsHomeUrl"/>jsp/about/contact-us.jsp"><span>Contact Us</span></a> <br />
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
								<li><a href="<s:property value="#bricsHomeUrl"/>jsp/about/contact-us.jsp"><span>Privacy</span></a>&nbsp;|</li>
								<li><a href="<s:property value="#bricsHomeUrl"/>jsp/general/disclaimer.jsp"><span>Disclaimer</span></a>&nbsp;|</li>
								<li><a href="http://www.nih.gov/about/access.htm" target="_blank" class="ext-link reverse">Accessibility</a>&nbsp;|</li>
								<li><a href="http://www.nih.gov/icd/od/foia/index.htm" target="_blank" class="ext-link reverse">FOIA</a></li>
							</ul>
							<ul class="footer-links footer-logos">
								<li>
									<a target="_blank" href="http://cdmrp.army.mil/dmrdp/default.shtml">
										<img width="101" height="48" alt="DMRDP" src="<s:property value="#bricsHomeUrl"/>portal/images/global/dmrdp_logo-bw.png" />
									</a>
								</li>
								<li>
									<a href="https://ccc.amedd.army.mil/" target="_blank">
										<img src="<s:property value="#bricsHomeUrl"/>portal/images/global/MRDCLOGO_xsm.png" alt="MRDC" />
									</a>
								</li>
								
							</ul>
							<ul class="footer-links footer-logos">
								<li>
									<a href="http://cit.nih.gov" target="_blank">
										<img src="<s:property value="#bricsHomeUrl"/>portal/images/global/cit_logo-bw.png" width="101" height="82" alt="CIT" />
									</a>
								</li>
								<li>
									<a href="http://ninds.nih.gov/" target="_blank">
										<img src="<s:property value="#bricsHomeUrl"/>portal/images/global/ninds_logo-bw.png" width="101" height="82" alt="NINDS" />
									</a>
								</li>	
							</ul>
							<ul class="footer-links footer-logos">
								
								<li>
									<a href="http://www.hhs.gov/" target="_blank">
										<img src="<s:property value="#bricsHomeUrl"/>portal/images/global/hhs_logo-bw.png" alt="The United States Department of Health and Human Services" />
									</a>
								</li>
								<li>
									<a href="http://www.nih.gov/" target="_blank">
										<img src="<s:property value="#bricsHomeUrl"/>portal/images/global/nih_logo-bw.png" alt="The National Institutes of Health" />
									</a>
								</li>
								<li>
									<a href="http://www.usa.gov/" target="_blank">
										<img src="<s:property value="#bricsHomeUrl"/>portal/images/global/usagov_logo-bw.png" alt="USA.gov : Government Made Easy" />
									</a>
								</li>	
							</ul>
						</div>
				
						<div class="contact build-notes" style="margin: -38px 11px -28px 20px;">
								<p class="left">Build Version:<s:property value="%{buildID}" /></br>
								Repository ID: <s:property value="%{deploymentID}" /></br>
								Last Deployed: <s:property value="%{lastDeployed}" /></p>
						</div>
					</div>
				</div>
			</div>
</body>