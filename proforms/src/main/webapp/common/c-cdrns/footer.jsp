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
						            <li><a href="<s:property value="#publicSiteUrl"/>/contact" target="_blank">Contact Us</a></li>
						            <li><a href="https://www.ninr.nih.gov/site-structure/policies#privacy" target="_blank">Privacy Statement</a></li>
						            <li><a class="ext-link" target="_blank" href="http://www.nih.gov/about/access.htm">Accessibility Policy</a></li>
						            <li><a class="ext-link" target="_blank" href="http://www.nih.gov/icd/od/foia/index.htm">FOIA</a></li>
						        </ul>
						    </div>
						    <p>NIH ... Turning Discovery Into Health</p>
						    <div class="branding">  
						       <div class="hhs"><a href="http://www.hhs.gov" target="_blank">The United States Department of Health and Human Services</a></div>
						       <div class="nih"><a href="http://www.nih.gov" target="_blank">The National Institutes of Health</a></div>
						       <div class="usa"><a href="http://www.usa.gov" target="_blank">USA.gov : Government Made Easy</a></div>
						    </div>
  						    
  						    <!-- Include release info section -->
  						    <jsp:include page="/common/release-info.jsp" />
						</div>
					</div>
				</div>
			</div>
</body>