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
					<sec:authorize access="hasAnyRole('ROLE_ADMIN', 'ROLE_STUDY_ADMIN', 'ROLE_STUDY')">
						<li id='studyToolLink'><s:a href="%{modulesSTURL}study/studyAction!list.action">Manage Studies</s:a>
							<ul class="tertiary-links" style="display: none">
								<li id='browseStudyLink'><s:a href="%{modulesSTURL}study/studyAction!list.action">View Studies</s:a></li>
								<li id='createStudyLink'><s:a href="%{modulesSTURL}study/studyAction!create.action">Create Study</s:a></li>
							</ul></li>
					</sec:authorize>
					<sec:authorize access="!hasAnyRole('ROLE_ADMIN', 'ROLE_STUDY_ADMIN', 'ROLE_STUDY')">
						<li id='studyToolLink'>
							<div class="missingPermission">Study Tool</div>
						</li>
					</sec:authorize>
				</s:if>
				
				<s:if test='modulesVTURL != "" && modulesUDTURL != ""'>
					<li id='submissionToolLink'><s:a href="%{modulesVTURL}study/viewSubmissionToolInfo.action">Submission Tools</s:a>
						<ul class="tertiary-links" style="display: none">
							<li id='subToolPageLink' class="long-text"><s:a href="%{modulesVTURL}study/viewSubmissionToolInfo.action">Submission Tools - Validation and Upload</s:a></li>
						</ul></li>
				</s:if>
				
				<s:if test='modulesDTURL != ""'>
					<sec:authorize access="hasAnyRole('ROLE_ADMIN', 'ROLE_STUDY_ADMIN', 'ROLE_STUDY')">
				<li id='mipavToolLink'><s:a href="%{modulesUDTURL}study/viewMipavToolInfo.action">MIPAV Tool</s:a>
							<ul class="tertiary-links" style="display: none">
								<li id='mipavToolPageLink'><s:a href="%{modulesUDTURL}study/viewMipavToolInfo.action">MIPAV</s:a></li>
							</ul></li>
							</sec:authorize>
							<sec:authorize access="!hasAnyRole('ROLE_ADMIN', 'ROLE_STUDY_ADMIN', 'ROLE_STUDY')">
						<li id='mipavToolLink'>
							<div class="missingPermission">MIPAV Tool</div>
						</li>
					</sec:authorize>
				</s:if>
				<s:if test='modulesDTURL != ""'>
					<sec:authorize access="hasAnyRole('ROLE_ADMIN', 'ROLE_STUDY_ADMIN', 'ROLE_STUDY')">
						<li id='downloadToolLink'><s:a href="%{modulesDTURL}repository/downloadQueueAction!view.action">Download Tool</s:a>
							<ul class="tertiary-links" style="display: none">
								<li id='downloadQueueLink'><s:a href="%{modulesDTURL}repository/downloadQueueAction!view.action">Download Queue</s:a></li>
								
							</ul></li>
					</sec:authorize>
					<sec:authorize access="!hasAnyRole('ROLE_ADMIN', 'ROLE_STUDY_ADMIN', 'ROLE_STUDY')">
						<li id='downloadToolLink'>
							<div class="missingPermission">Download Tool</div>
						</li>
					</sec:authorize>
				</s:if>
				
				<s:if test='modulesSTURL != ""'>
					<sec:authorize access="hasAnyRole('ROLE_ADMIN', 'ROLE_REPOSITORY_ADMIN', 'ROLE_STUDY_ADMIN')">
						<li id="contributeDataToolsLink" class="long-text"><s:a href="%{modulesSTURL}studyAdmin/studyAction!list.action">Data Repository Administration</s:a>
							<ul class="tertiary-links" style="display: none">						
								<sec:authorize access="hasAnyRole('ROLE_ADMIN', 'ROLE_STUDY_ADMIN')">
									<li id="studyList"><s:a href="%{modulesSTURL}studyAdmin/studyAction!list.action">Manage Studies</s:a></li>
								</sec:authorize>
								
								<sec:authorize access="hasAnyRole('ROLE_ADMIN', 'ROLE_REPOSITORY_ADMIN')">
									<li id="datasetListLink"><s:a href="%{modulesSTURL}studyAdmin/datasetAction!list.action">Manage Datasets</s:a></li>
								</sec:authorize>
							</ul></li>
					</sec:authorize>
				</s:if>
				
				
				
			</ul>
		</div>
	</div>
</div>
