<%@taglib prefix="s" uri="/struts-tags"%>
<%@taglib prefix="ndar" tagdir="/WEB-INF/tags"%>
<%@ taglib prefix="sec" uri="http://www.springframework.org/security/tags"%>

<div id="left-sidebar">
	<ul class="subnavigation">		
		<s:if test="modulesDashboardEnabled">
			<li id='workspaceLink'><s:a action="baseAction" method="dashboard" namespace="/">Workspace</s:a>
				<ul class="tertiary-links" style="display: none">
					<li id="userDashboard"><s:a action="baseAction" method="dashboard" namespace="/">Overview </s:a></li>
					<li id="userDashboard"><s:a action="baseAction" method="dashboard" namespace="/">My Dashboard </s:a></li>
				</ul></li>
		</s:if>
		
		<s:if test='modulesDDTURL != ""'>
			<sec:authorize access="hasAnyRole('ROLE_ADMIN', 'ROLE_DICTIONARY_ADMIN', 'ROLE_DICTIONARY')">
				<li id='dataDictionaryToolLink'><s:a href="%{modulesDDTURL}dictionary/listDataStructureAction!list.action">Data Dictionary Tool</s:a>
					<ul class="tertiary-links" style="display: none">
					 	<li id='listDataStructureLink'><s:a href="%{modulesDDTURL}dictionary/listDataStructureAction!list.action">Search Form Structures</s:a>
					 	<li id='dataStructureLink'><s:a href="%{modulesDDTURL}dictionary/dataStructureAction!create.action">Create Form Structure</s:a></li>
						<li id='searchDataElementLink'><s:a href="%{modulesDDTURL}dictionary/searchDataElementAction!list.action">Search Data Elements</s:a></li>
						<li id='dataElementLink'><s:a href="%{modulesDDTURL}dictionary/dataElementAction!create.action">Create Data Element</s:a></li>
						<!-- Links location is now defined in properties file -->
						<!-- 
						<li id='listDataStructureLink'><s:a action="listDataStructureAction" method="list" namespace="/dictionary">Search Form Structures</s:a></li>
						<li id='dataStructureLink'><s:a action="dataStructureAction" method="create" namespace="/dictionary">Create Form Structure</s:a></li>
						<li id='searchDataElementLink'><s:a action="searchDataElementAction" method="list" namespace="/dictionary">Search Data Elements</s:a></li>
						<li id='dataElementLink'><s:a action="dataElementAction" method="create" namespace="/dictionary">Create Data Element</s:a></li>
						-->
					</ul></li>
			</sec:authorize>
			<sec:authorize access="!hasAnyRole('ROLE_ADMIN', 'ROLE_DICTIONARY_ADMIN', 'ROLE_DICTIONARY')">
				<li id='dataDictionaryToolLink'>
					<div class="missingPermission">Data Dictionary Tool</div>
				</li>
			</sec:authorize>
		</s:if>

		<s:if test='modulesVTURL != ""'>
			<li id='validationToolLink'><s:a href="%{modulesVTURL}viewValidationToolInfo.action">Validation Tool</s:a>
				<ul class="tertiary-links" style="display: none">
					<li id='vtToolPageLink'><s:a href="%{modulesVTURL}viewValidationToolInfo.action">Validation Data</s:a></li>
				</ul></li>
		</s:if>
		
		<s:if test='modulesGTURL != ""'>
			<sec:authorize access="hasAnyRole('ROLE_GUID')">
				<li id='guidDataLink'><s:a href="%{modulesGTURL}guid/guidAction!landing.action">GUID Tool</s:a>
					<ul class="tertiary-links" style="display: none">
						<li id='myGuidDataLink'><s:a href="%{modulesGTURL}guid/guidAction!list.action">My GUIDs</s:a></li>
					</ul></li>
			</sec:authorize>
			<sec:authorize access="!hasAnyRole('ROLE_GUID')">
				<li id='guidDataLink'>
					<div class="missingPermission">GUID Tool</div>
				</li>
			</sec:authorize>
		</s:if>

<%-- 		<s:iaf test='modulesSTURL != ""'> --%>
			<sec:authorize access="hasAnyRole('ROLE_ADMIN', 'ROLE_STUDY_ADMIN', 'ROLE_STUDY')">
				<li id='studyToolLink'><s:a href="%{modulesSTURL}study/studyAction!list.action">Study Tool</s:a>
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
<%-- 		</s:if> --%>

		<s:if test='modulesUDTURL != ""'>
			<sec:authorize access="hasAnyRole('ROLE_ADMIN', 'ROLE_STUDY_ADMIN', 'ROLE_STUDY')">
				<li id='uploadDataLink'><s:a href="%{modulesUDTURL}study/viewUploadToolInfo.action">Upload Data Tool</s:a>
					<ul class="tertiary-links" style="display: none">
						<li id='uploadToolPageLink'><s:a href="%{modulesUDTURL}study/viewUploadToolInfo.action">Upload Data</s:a></li>
					</ul></li>
			</sec:authorize>
			<sec:authorize access="!hasAnyRole('ROLE_ADMIN', 'ROLE_STUDY_ADMIN', 'ROLE_STUDY')">
				<li id='uploadDataLink'>
					<div class="missingPermission">Upload Data Tool</div>
				</li>
			</sec:authorize>
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
						<li id='downloadToolLink'><s:a href="%{modulesDTURL}repository/baseAction!launch.action?webstart=downloadTool">Download Tool</s:a></li>
					</ul>
				</li>
			</sec:authorize>
			<sec:authorize access="!hasAnyRole('ROLE_ADMIN', 'ROLE_STUDY_ADMIN', 'ROLE_STUDY')">
				<li id='downloadToolLink'>
					<div class="missingPermission">Download Tool</div>
				</li>
			</sec:authorize>
		</s:if>
	</ul>
</div>

<script type="text/javascript">
// Create the tooltips only on document load
$(document).ready(function() 
{
   // Match all link elements with href attributes within the content div
   $('div.missingPermission').each(
   function()
   {
	   $(this).qtip(
			   
			   {
				   
			      content: 'You currently do not have access to this tool; if you would like to request access, please visit the <a href="/portal/accounts/accountAction!view.action">Profile</a> page.',
			      hide: {
			          fixed: true // Make it fixed so it can be hovered over
			       },
			      position: {
			    	  my: 'bottom left',
			    	  at: 'bottom right'
			      }
			   });
   }
  );
});
</script>