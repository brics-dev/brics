<%@taglib prefix="s" uri="/struts-tags"%>
<%@taglib prefix="ndar" tagdir="/WEB-INF/tags"%>
<%@ taglib prefix="sec" uri="http://www.springframework.org/security/tags"%>

<div style="float:left; position:relative; margin-left:17px;">
	<div id="hamburger">
       	<div></div>
       	<div></div>
       	<div></div>
       	<b>Menu</b>
	</div>
	<div id="hamburgerMenu" style="display:none; position:absolute; top:46px; left:0px; z-index:100;">
		<div id="left-sidebar">
			<ul class="subnavigation">
				<s:if test='modulesSTURL != ""'>
					<sec:authorize access="hasAnyRole('ROLE_ADMIN', 'ROLE_REPORTING_ADMIN')">
						<li id='studyToolLink'><s:a href="">Reporting</s:a>
							<ul class="tertiary-links" style="display: none">
								<li id='browseStudyLink'><s:a href="/reporting/viewstudiesreports/studiesReportingListAction!list.action">Run a report</s:a></li>
								<li id='createStudyLink'><s:a href="/reporting/viewstudiesreports/studiesReportingListAction!howToReporting.action">How To</s:a></li>
							</ul></li>
					</sec:authorize>
				</s:if>

			</ul>
		</div>
	</div>
</div>
