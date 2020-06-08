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
						        	<li><a class="ext-link" target="_blank" href="http://www.nih.gov/about/access.htm">Privacy Statement</a></li>
						            <li><a class="ext-link" target="_blank" href="http://www.nih.gov/about/access.htm">Accessibility Policy</a></li>
						            <li><a class="ext-link" target="_blank" href="http://www.nih.gov/icd/od/foia/index.htm">FOIA</a></li>
						        </ul>
						    </div>
						    <p>NIH ... Turning Discovery Into Health</p>
						    <div class="branding">
						       <a href="http://www.hhs.gov" target="_blank"><div class="hhs">The United States Department of Health and Human Services</div></a>
						       <a href="http://www.nih.gov" target="_blank"><div class="nih">The National Institutes of Health</div></a>
						       <a href="http://www.usa.gov" target="_blank"><div class="usa">USA.gov : Government Made Easy</div></a>
						    </div>

  						    <!-- Include release info section -->
							<jsp:include page="/common/release-info.jsp" />
						</div>
					</div>
				</div>
			</div>
</body>