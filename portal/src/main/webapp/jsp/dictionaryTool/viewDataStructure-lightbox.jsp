<%@include file="/common/taglibs.jsp"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn"%>

<s:set var="mapElement" value="mapElement" />
<div class="lightbox-content-wrapper">
	<h3>
		Form Structure:
		<s:property value="currentDataStructure.title" />
	</h3>
	<div class="clear-float">


		<!-- First column of of data from the items.
					 Items with long descriptions (and to get cropped on the page)
					 See guidelines as an example.
					 The less function must also be called for each cropped item in the js at
					 the bottom of the page. -->
		<div class="form-output">
			<div class="label">Short Name:</div>
			<div class="readonly-text">
				<s:property value="currentDataStructure.shortName" />
			</div>
		</div>
		<div class="form-output">
			<div class="label">Description:</div>
			<div id="description" class="readonly-text limitLength">
				<s:property value="currentDataStructure.description" />
			</div>
		</div>
		<div class="form-output">
			<div class="label">Disease:</div>
			
				<div class="readonly-text">
				<s:iterator var="diseaseStructure" value="currentDataStructure.diseaseList" status="diseaseStatus">
					<c:out value="${diseaseStructure.disease.name}" />
					<s:if test="!#diseaseStatus.last">,</s:if>
					</s:iterator>
				</div>
			
		</div>
		<s:if test="currentDataStructure.documentationFileId!=null">
			<div class="form-output">
				<div class="label">Documentation:</div>
				<div class="readonly-text">
					<s:a action="fileDownloadAction!downloadDDT">
						<s:param name="fileId" value="%{currentDataStructure.documentationFileId}" />
						<s:property value="documentationDDTFileName" />
					</s:a>
				</div>
			</div>
		</s:if> 
 		<s:else>

		</s:else>
		<s:if test="currentDataStructure.documentationUrl!=null">
			<div class="form-output">
				<div class="label">Documentation:</div>
				<div class="readonly-text">
					<a href="<s:property value="currentDataStructure.documentationUrl" />" target="_blank"><s:property
							value="currentDataStructure.documentationUrl" /></a>
				</div>
			</div>
		</s:if> 
		<s:if test="currentDataStructure.organization!=''">
			<div class="form-output">
				<div class="label">Organization:</div>
				<div class="readonly-text">
					<s:property value="currentDataStructure.organization" />
				</div>
			</div>
		</s:if>
		<div class="form-output">
				<div class="label">Required Program Form:</div>
				<s:if test="%{isRequired}">
					<div class="readonly-text">
						<c:out value="Yes" />
					</div>
				</s:if>
				<s:else> 
					<div class="readonly-text">
						<c:out value="No" />
					</div>
				</s:else>
			</div>
			
			<div class="form-output">
				<div class="label">Standardization:</div>
				<div class="readonly-text">
					<s:property value="currentDataStructure.standardization.display" />
				</div>
			</div>
		<s:if test="currentDataStructure.publicationDate!=null">
			<div class="form-output">
				<div class="label">Publication Date:</div>
				<div class="readonly-text">
					<%-- <s:property value="currentDataStructure.publicationDate" /> --%>
					<ndar:dateTag value="${currentDataStructure.publicationDate}" />
				</div>
			</div>
		</s:if>
		<!-- 	</div> -->
		<!-- 	<div class="column"> -->
		<div class="form-output">
			<div class="label">Version:</div>
			<div class="readonly-text">
				<s:property value="currentDataStructure.version" />
			</div>
		</div>
		<div class="form-output">
			<div class="label">Date Created:</div>
			<div class="readonly-text">
				<ndar:dateTag value="${currentDataStructure.modifiedDate}" />
			</div>
		</div>
		<div class="form-output">
			<div class="label">Created By:</div>
			<div class="readonly-text">
				<s:property value="currentDataStructure.createdBy" />
			</div>
		</div>
		<div class="form-output">
			<div class="label">Number of Data Elements:</div>
			<div class="readonly-text">
				<c:out value="${fn:length(sessionDataElementList.mapElements)}" />
			</div>
		</div>
		<div class="form-output">
				<div class="label">eForms:</div>
				<div class="readonly-text">
					<s:if test="hasAssociatedEforms">
						<c:out value="Y" />
					</s:if>
					<s:else>
						<c:out value="N" />
					</s:else>
				</div>
			</div>

		<h3>Attached Data Elements</h3>
		<div>
			<jsp:include page="dataStructure/attachedDataElements.jsp" />
		</div>
	</div>

</div>
<c:if test="${fn:contains(pageContext.request.requestURL, 'pdbp' )}">
<link rel='stylesheet' type='text/css' href='/portal/config/pdbp/style.css'>
</c:if>
<c:if test="${fn:contains(pageContext.request.requestURL, 'fitbir' )}">
<link rel='stylesheet' type='text/css' href='/portal/config/fitbir/style.css'>
</c:if>
<c:if test="${fn:contains(pageContext.request.requestURL, 'cistar' )}">
<link rel='stylesheet' type='text/css' href='/portal/config/cistar/style.css'>
</c:if>
<c:if test="${fn:contains(pageContext.request.requestURL, 'eyegene' ) || fn:contains(hostname, 'nei' )}">
<link rel='stylesheet' type='text/css' href='/portal/config/eyegene/style.css'>
</c:if>
<c:if test="${fn:contains(pageContext.request.requestURL, 'cdrns' )}">
<link rel='stylesheet' type='text/css' href='/portal/config/cdrns/style.css'>
</c:if>
<c:if test="${fn:contains(pageContext.request.requestURL, 'nti' )}">
<link rel='stylesheet' type='text/css' href='/portal/config/nti/style.css'>
</c:if>
<link href="<s:url value='/styles/jquery/jquery-ui.css'/>" rel="stylesheet" type="text/css" media="all" />
<script>
$("#details-accordion").accordion({ autoHeight: false,heightStyle: "content" , header: "h4", collapsible: true });

</script>

