<%@ page import="gov.nih.nichd.ctdb.util.common.SysPropUtil" %>
<%@ taglib uri="/struts-tags" prefix="s" %>

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
								<li><a href="/cnrm-public/jsp/general/privacy.jsp">Privacy</a></li> |
								<li><a href="/cnrm-public/jsp/general/disclaimer.jsp">Disclaimer</a></li> |
								<li><a href="http://www.nih.gov/about/access.htm" target="_blank"class="ext-link reverse">Accessibility</a></li> |
								<li><a href="http://www.nih.gov/icd/od/foia/index.htm" target="_blank"class="ext-link reverse">FOIA</a></li>
							</ul>
						</div>
						<div class="clear-both"></div>
						<div id="footer-logo-box">
							<div class="branding">
						       <div class="hhs"><a href="http://www.hhs.gov">The United States Department of Health and Human Services</a></div>
						       <div class="nih"><a href="http://www.nih.gov">The National Institutes of Health</a></div>
						       <div class="usa"><a href="http://www.usa.gov">USA.gov : Government Made Easy</a></div>
						    </div>
						</div>	
					</div>
				</div>

				<div class="contact build-notes" style="margin: 20px;">
					<p class="left">Build Version:<s:property value="%{buildID}" /></br>
					Repository ID: <s:property value="%{deploymentID}" /></br>
					Last Deployed: <s:property value="%{lastDeployed}" /></p>
				</div>
			</div>
</body>