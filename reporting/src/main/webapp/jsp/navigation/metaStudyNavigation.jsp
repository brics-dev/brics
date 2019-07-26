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
				<s:if test='modulesMSURL != ""'>
					<sec:authorize access="hasAnyRole('ROLE_ADMIN', 'ROLE_METASTUDY_ADMIN', 'ROLE_METASTUDY')">
						<li id='metaStudyLink'><s:a href="%{modulesMSURL}metastudy/metaStudyListAction!list.action">Manage Meta Studies</s:a>
							<ul class="tertiary-links" style="display: none">
								<li id='browseMetaStudyLink'><s:a href="%{modulesMSURL}metastudy/metaStudyListAction!list.action">View Meta Studies</s:a></li>
								<li id='createMetaStudyLink'><s:a href="%{modulesMSURL}metastudy/metaStudyAction!create.action">Create Meta Study</s:a></li>
							</ul></li>
					</sec:authorize>
					<sec:authorize access="!hasAnyRole('ROLE_ADMIN', 'ROLE_METASTUDY_ADMIN', 'ROLE_METASTUDY')">
						<li id='metaStudyLink'>
							<div class="missingPermission">Meta Study</div>
						</li>
					</sec:authorize>
				</s:if>
		
			</ul>
		</div>
	</div>
</div>


