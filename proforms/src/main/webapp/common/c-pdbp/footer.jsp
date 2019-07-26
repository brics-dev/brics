<%@ page import="gov.nih.nichd.ctdb.util.common.SysPropUtil" %>
<%@ taglib uri="/struts-tags" prefix="s" %>
<s:set var="publicSiteUrl" value="#systemPreferences.get('template.publicurl')"/>
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
						<div id="footerContentContainer">
						    <div class="footernav clearboth">
						        <ul class="clearboth">
						            <li><a href="<s:property value="#publicSiteUrl"/>/sitemap">Site Map</a></li>
						            <li><a href="<s:property value="#publicSiteUrl"/>/contact-us">Contact Us</a></li>
						            <li><a href="<s:property value="#publicSiteUrl"/>/privacy">Privacy Statement</a></li>
						            <li><a class="ext-link" target="_blank" href="http://www.nih.gov/about/access.htm">Accessibility Policy</a></li>
						            <li><a class="ext-link" target="_blank" href="http://www.nih.gov/icd/od/foia/index.htm">FOIA</a></li>
						        </ul>
						    </div>
						    <p>NIH ... Turning Discovery Into Health</p>
						    <div class="branding">
						       <div class="hhs"><a href="http://www.hhs.gov">The United States Department of Health and Human Services</a></div>
						       <div class="nih"><a href="http://www.nih.gov">The National Institutes of Health</a></div>
						       <div class="usa"><a href="http://www.usa.gov">USA.gov : Government Made Easy</a></div>
						    </div>

							<div class="contact build-notes" style="clear: both;margin: -28px -21px 33px 0;">
								<p class="left">Build Version:<s:property value="%{buildID}" /></br>
								Repository ID: <s:property value="%{deploymentID}" /></br>
								Last Deployed: <s:property value="%{lastDeployed}" /></p>
							</div>
						</div>
					</div>
				</div>
			</div>
</body>