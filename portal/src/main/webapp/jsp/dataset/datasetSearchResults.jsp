<%@include file="/common/taglibs.jsp"%>

<s:set var="datasetPage" value="page" />
<s:set var="datasetNumPages" value="numPages" />

<%-- 
<div id="datasetTable" class="dataTableContainer dataTableJSON">
	<ul>
		<li>
			<select>
				<option value="filterOwnership:all">Ownership: all</option>
				<option value="filterOwnership:mine">Ownership: mine</option>
			</select>
			<select>
				<option value="filterStatus:all">Status: All</option>
				<option value="filterStatus:private">Status: Private</option>
				<option value="filterStatus:requestedDeletion">Status: Private- Requested Deletion</option>
				<option value="filterStatus:requestedSharing">Status: Private- Requested Share</option>
				<option value="filterStatus:requestedArchive">Status: Private- Requested Archive</option>			
				<option value="filterStatus:shared">Status: Shared</option>
				<option value="filterStatus:sharedRequestedArchive">Status: Shared- Requested Archive</option>
				<option value="filterStatus:archived">Status: Archived</option>
				<option value="filterStatus:deleted">Status: Deleted</option>
				<option value="filterStatus:errors">Status: Errors</option>
				<option value="filterStatus:uploading">Status: Uploading</option>
				<option value="filterStatus:loadingData">Status: Loading Data</option>
			</select>
		</li>
	</ul>
	<idt:jsontable name="datasetList" id="supportingDocumentation" decorator="gov.nih.tbi.taglib.datatableDecorators.DatasetListDecorator">
	    <idt:setProperty name="basic.msg.empty_list"
		value="You have no visible supporting documentation at this time." />
		<idt:column title="" property="dataSetSelectInput" />
		<idt:column title="NAME" property="nameLink" />
		<idt:column title="STUDY" property="studyLink" />
		<idt:column title="SUBMITTER" property="submitter.fullName" />
		<idt:column title="" property="owner" visible="false" />
		<idt:column title="" property="requestStatus" visible="false" />
		<idt:column title="DATE SUBMITTED" property="submitDate" styleClass="nowrap" decorator="gov.nih.tbi.taglib.datatableDecorators.DateColumnDecorator" />
		<idt:column title="STATUS" property="datasetStatus" />
	</idt:jsontable>
</div>
 --%>

