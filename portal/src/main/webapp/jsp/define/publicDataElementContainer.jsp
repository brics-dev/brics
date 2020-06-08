<%-- Loaded by an iframe to display public data element search results. --%>
<%@page import="gov.nih.tbi.commons.model.StatusType"%>
<%@include file="/common/taglibs.jsp"%>
<c:set var="hostname" value="${pageContext.request.serverName}" />
<input type="hidden" id="hostStyle" name="hostStyle" value="${modulesConstants.modulesStyleKey}" />
<html style="background: #fff;  margin-top:20px; overflow: hidden;" lang="en">
<head>
<c:choose>
         <c:when test="${fn:contains(hostname, 'cnrm' )}">
        	<title> : CNRM : Center for Neuroscience and Regenerative Medicine</title>
         </c:when>
         <c:when test="${fn:contains(hostname, 'pdbp' )}">
           <title> : PDBP DMR : Parkinson's Disease Biomarkers Project Data Management Resource</title>
         </c:when>
         <c:when test="${fn:contains(hostname, 'fitbir' )}">
         	<title> : FITBIR : Federal Interagency Traumatic Brain Injury Research Informatics System</title>
         </c:when>
         <c:when test="${fn:contains(hostname, 'ninds' )}">
         	<title> : NINDS Common Data Elements</title>
         </c:when>
          <c:when test="${fn:contains(hostname, 'cistar' )}">
         	<title> : CISTAR : Federal Interagency Traumatic Brain Injury Research Informatics System</title>
         </c:when>
         <c:when test="${fn:contains(hostname, 'eyegene' ) || fn:contains(hostname, 'nei' )}">
         	<title> : NEI BRICS : The National Eye Institute</title>
         </c:when>
         <c:when test="${fn:contains(hostname, 'cdrns' )}">
         	<title> : cdRNS : Common Data Repository for Nursing Science</title>
         </c:when>
         <c:when test="${fn:contains(hostname, 'nti' )}">
         	<title> : NTI : National Trauma Institute</title>
         </c:when>
		</c:choose>
</head>
<body>

<jsp:include page="/common/script-includes.jsp" />

      <c:choose>
         <c:when test="${fn:contains(modulesConstants.modulesStyleKey, 'cnrm')}"> 
        	<link href="<s:url value='/config/cnrm/style.css'/>" rel="stylesheet" type="text/css" media="all" />
         </c:when>
         <c:when test="${fn:contains(hostname, 'pdbp')}">          
           	<link href="<s:url value='/config/pdbp/style.css'/>" rel="stylesheet" type="text/css" media="all" />
         </c:when>
         <c:when test="${fn:contains(hostname, 'fitbir')}">
         	<link href="<s:url value='/config/fitbir/style.css'/>" rel="stylesheet" type="text/css" media="all" />
         </c:when>
         <c:when test="${fn:contains(modulesConstants.modulesStyleKey, 'ninds')}">
         	<link href="<s:url value='/config/ninds/style.css'/>" rel="stylesheet" type="text/css" media="all" />
         </c:when>
         <c:when test="${fn:contains(modulesConstants.modulesStyleKey, 'cistar')}">
         	<link href="<s:url value='/config/cistar/style.css'/>" rel="stylesheet" type="text/css" media="all" />
         </c:when>
         <c:when test="${fn:contains(modulesConstants.modulesStyleKey, 'eyegene') || fn:contains(modulesConstants.modulesStyleKey, 'nei' )}"> 
         	<link href="<s:url value='/config/eyegene/style.css'/>" rel="stylesheet" type="text/css" media="all" />
         </c:when>
         <c:when test="${fn:contains(modulesConstants.modulesStyleKey, 'cdrns')}">
         	<link href="<s:url value='/config/cdrns/style.css'/>" rel="stylesheet" type="text/css" media="all" />
         </c:when>
          <c:when test="${fn:contains(modulesConstants.modulesStyleKey, 'nti')}">
         	<link href="<s:url value='/config/nti/style.css'/>" rel="stylesheet" type="text/css" media="all" />
         </c:when>
		</c:choose>
<%-- This styling overrides the padding between the content-wrapper and the body which on a full page will be where the top nav goes --%>

<div id="content-wrapper" style="margin-top: 0px; height: 100%; width: 100%; background:#fff;" >
	<div id="publicDataElementDiv"></div>
</div>
<script type="text/javascript" src="/portal/js/iframe/iframeResizer.contentWindow.min.js"></script>
<script type="text/javascript" src="/portal/js/search/dataElementSearch.js"></script>

<script type="text/javascript">
	$('document').ready(function() {
		loadPublicElementSearch();
	});
	
	function createDELightbox(id) {
		var action = "/publicData/dataElementAction!viewDetails.ajax?dataElementId=" + id;
		$.ajax(action, {
			"type": 	"POST",
			"async": 	false,
			"success": 	function(data) {
							top.postMessage(data, "*");
						}
		});
	}
	
</script>
</body></html>