<%@ page import="gov.nih.nichd.ctdb.util.common.SysPropUtil"%>
<%@ taglib uri="/struts-tags" prefix="s"%>
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
					Global Stroke Data Repository<br />
					(GSDR) System
				</h4>
				<br />
				<p>
					<strong>National Institutes of Health</strong><br />
				</p>
			</div>
			<div class="contact">
				<h5>
					National Institutes of Health<br /> National Institute of
					Neurological Disorders &amp; Stroke
				</h5>
				<p>
					Website: <a href="http://www.ninds.nih.gov"
						title="National Institute of Neurological Disorders and Stroke"
						target="_blank" class="ext-link reverse">http://www.ninds.nih.gov</a>
				</p>
			</div>




			<div class="contact">
				<ul class="footer-links">
				
					<li><a href="http://www.nih.gov/about/access.htm" target="_blank" class="ext-link reverse">Accessibility</a></li> |
					<li><a href="http://www.nih.gov/icd/od/foia/index.htm" target="_blank" class="ext-link reverse">FOIA</a></li>
				</ul>

				<ul class="footer-links footer-logos">
					<li><a href="http://cit.nih.gov" target="_blank" id="footerCitLogo">
						<img src='<s:url value="/images/global/cit_logo-bw.png"/>' width="101" height="82" border="0" alt="CIT" />
					</a></li>
					<li><a href="http://ninds.nih.gov/" target="_blank" id="footerNindsLogo">
						<img src='<s:url value="/images/global/ninds_logo-bw.png"/>' width="101" height="82" border="0" alt="NINDS" />
					</a></li>
					<li><a href="http://www.hhs.gov/" target="_blank" id="footerHhsLogo">
						<img src='<s:url value="/images/global/hhs_logo-bw.png"/>' width="50" height="50" border="0" alt="HHS" />
					</a></li>
					<li><a href="http://www.nih.gov/" target="_blank" id="footerNihLogo">
						<img src='<s:url value="/images/global/NIH_Logo-bw.png"/>' width="101" height="82" border="0" alt="NIH" />
					</a></li>
					<li><a href="http://www.usa.gov/" target="_blank" id="footerUsaLogo">
						<img src='<s:url value="/images/global/usagov_logo-bw.png"/>' width="100" height="31" border="0" alt="USA.gov" />
					</a></li>
				</ul>
			</div>

			<!-- Include release info section -->
			<jsp:include page="/common/release-info.jsp" />
		</div>
		
	</div>
	</body>