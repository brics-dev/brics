<%@ include file="/common/taglibs.jsp"%>

<script type="text/javascript" src="/portal/js/search/viewGuid.js"></script>

<title>GUID: <s:property value="%{currentInvalidSubject.guid}"/></title>

<div class="clear-float">
	<h1 class="float-left">GUID (Global Unique Identifier)</h1>
</div>

<div class="border-wrapper">

	<jsp:include page="../navigation/guidNavigation.jsp" />

	<div id="main-content">
		<div id="breadcrumb">
			<s:if test="inAdmin">
				<s:a action="guidAdminAction!list.action">View All GUIDs</s:a>  &gt; <s:property value="%{currentInvalidSubject.guid}" />
			</s:if>
			<s:else>
				<s:a action="guidAction!list.action">My GUIDs</s:a>  &gt; <s:property value="%{currentInvalidSubject.guid}" />
			</s:else>
		</div>

		<h2>GUID:&nbsp;<s:property value="%{currentInvalidSubject.guid}" /></h2>

		<div class="clear-float">

			<div class="form-output">
				<s:label cssClass="label" key="label.subject.guid" />
				<div class="readonly-text">
					<s:property value="currentInvalidSubject.guid" />
				</div>
			</div>

			<div class="form-output">
				<label>Date First Registered</label>
				<div class="readonly-text">
					<ndar:dateTag value="${currentInvalidSubject.dateCreated}" />
				</div>
			</div>
			
			<div class="form-output">
				<label>Linked To</label>
				<div class="readonly-text">
					<c:forEach var="linkedGuid" items="${linkedGuids}">
						<c:choose>
							<c:when test="${linkedGuid=='Not yet converted'}">	
								<td>Not yet converted</td>
							</c:when>
							<c:otherwise>
								<a href= "javascript:redirectWithReferrer('/portal/guid/guidAction!view.action?guid=${linkedGuid}');"> ${linkedGuid}</a>
								<br/>
							</c:otherwise>
						</c:choose>
					</c:forEach>
				</div>
			</div>
		</div>
		
		<h3>Users Registered By:</h3>

		<table class="display-data full-width" cellSpacing="0" cellPadding="0">
			<tr>
				<th>Site</th><th>Date Registered</th>
			</tr>
			<tr>
				<!-- for pseudoguids, there will only ever be one -->
				<td><s:property value="currentInvalidSubject.creator.username" /></td>
				<td><ndar:dateTag value="${currentInvalidSubject.dateCreated}" /></td>
			</tr>
		</table>

		<h3>Datasets where GUID has been used</h3>

		<table class="display-data full-width" cellSpacing="0" cellPadding="0">
			<tr>
				<!-- 	An array of the table headers. The first value in each pair is the name of the header and the second value is the string that will be used to sort the hibernate results -->
				<s:iterator var="name" value="{{'Dataset Id', 'data.dataId'}, {'Dataset Name', 'data.dataName'}, {'Dataset Submission Date', 'data.submissionDate'}, {'Study', 'data.containerName'}}">
					<th><s:property value="#name[0]" /> <s:if test="#name[1] == sort">
							<s:if test="ascending">
								<img src='<s:url value="/images/brics/common/icon-down.png"/>'>
							</s:if>
							<s:else>
								<img src='<s:url value="/images/brics/common/icon-up.png"/>'>
							</s:else>
						</s:if></th>
				</s:iterator>
			</tr>
			<s:iterator var="guidJoinedData" value="subjectSubmittedData" status="rowstatus">
				<s:if test="#rowstatus.odd == true"><tr></s:if>
				<s:else><tr class="stripe"></s:else>
					<td><s:property value="data.dataId" /></td>
					<td><a href="javascript:viewDataset('<s:property value="data.dataId" />', true)"><s:property value="data.dataName" /></a></td>
					<td><ndar:dateTag value="${data.submissionDate}" /></td>
					<td><a href="javascript:viewStudy('<s:property value="data.study.id" />', true)"><s:property value="data.containerName" /></a></td>
				</tr>
			</s:iterator>
		</table>

	</div>
</div>

<script type="text/javascript">
	    <s:if test="%{inAdmin == true}">
	    	setNavigation({"bodyClass":"primary", "navigationLinkID":"guidModuleLink","subnavigationLinkID":"guidToolLink", "tertiaryLinkID":"listGuidsLink"});
		</s:if>
		<s:else>
			setNavigation({"bodyClass":"primary", "navigationLinkID":"guidModuleLink", "subnavigationLinkID":"guidDataLink", "tertiaryLinkID":"myGuidDataLink"});
		</s:else>
</script>