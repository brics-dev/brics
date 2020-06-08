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
			<div class="content line">
				<div class="unit size1of2">
					<p>
						<a href="<s:property value="#publicSiteUrl"/>/contact">Contact</a>
					</p>
				</div>

				<!-- Include release info section -->
				<jsp:include page="/common/release-info.jsp" />
			</div>
		</div>
	</div>
</body>