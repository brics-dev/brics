<%-- Loaded by an iframe to display public data element search results. --%>
<%@page import="gov.nih.tbi.commons.model.StatusType"%>
<%@include file="/common/taglibs.jsp"%>
<c:set var="hostname" value="${pageContext.request.serverName}" />
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
         	<title> : EYEGENE : The National Ophthalmic Disease Genotyping and Phenotyping Network</title>
         </c:when>
         <c:when test="${fn:contains(hostname, 'cdrns' )}">
         	<title> : cdRNS : Common Data Repository for Nursing Science</title>
		 </c:when>
		  <c:when test="${fn:contains(hostname, 'nti' )}">
         	<title> : National Trauma Institute</title>
         </c:when>
		</c:choose>

</head>

<body>

<jsp:include page="/common/script-includes.jsp" />

      <c:choose>
         <c:when test="${fn:contains(hostname, 'cnrm' )}">
        	<link href="<s:url value='/config/cnrm/style.css'/>" rel="stylesheet" type="text/css" media="all" />
         </c:when>
         <c:when test="${fn:contains(hostname, 'pdbp' )}">
           	<link href="<s:url value='/config/pdbp/style.css'/>" rel="stylesheet" type="text/css" media="all" />
         </c:when>
         <c:when test="${fn:contains(hostname, 'fitbir' )}">
         	<link href="<s:url value='/config/fitbir/style.css'/>" rel="stylesheet" type="text/css" media="all" />
         </c:when>
         <c:when test="${fn:contains(hostname, 'ninds' )}">
         	<link href="<s:url value='/config/ninds/style.css'/>" rel="stylesheet" type="text/css" media="all" />
         </c:when>
          <c:when test="${fn:contains(hostname, 'cistar' )}">
         	<link href="<s:url value='/config/cistar/style.css'/>" rel="stylesheet" type="text/css" media="all" />
         </c:when>
         <c:when test="${fn:contains(hostname, 'eyegene' ) || fn:contains(hostname, 'nei' )}">
         	<link href="<s:url value='/config/eyegene/style.css'/>" rel="stylesheet" type="text/css" media="all" />
         </c:when>
         <c:when test="${fn:contains(hostname, 'cdrns' )}">
         	<link href="<s:url value='/config/cdrns/style.css'/>" rel="stylesheet" type="text/css" media="all" />
		 </c:when>
		<c:when test="${fn:contains(hostname, 'nti' )}">
        <link href="<s:url value='/config/nti/style.css'/>" rel="stylesheet" type="text/css" media="all" />
         </c:when>
		</c:choose>

			
				
<%-- This styling overrides the padding between the content-wrapper and the body which on a full page will be where the top nav goes --%>
<div id="content-wrapper" style="margin-top: 0px; height: 100%; width: 100%; background:#fff;" >
<%-- <div id="dataStructureFilterOptions" class="filter">
	<strong>Status: </strong> <a class="inactiveLink dataStructureSelectedFilter" id="4">All</a> | <a href="javascript:publishedDataStructureSetFilter(5);"
	id="5">Shared Draft</a> | <a href="javascript:publishedDataStructureSetFilter(2);" id="2">Published</a> 
</div> --%>

	<div id="publishedDataStructureResultsId"></div>
</div>
<script type="text/javascript" src="/portal/js/iframe/iframeResizer.contentWindow.min.js"></script>
<script type="text/javascript" src="/portal/js/search/publishedDataStructureSearch.js"></script>

<script type="text/javascript">
	$('document').ready(function() {
		var filterParam = getURLParameter("filter");
		if(filterParam != "undefined")
		{
			publishedDataStructureSetFilter(filterParam);
		}
		else
		{
			publishedDataStructureSearch();
		}	
		});
	
	
	// Perform ajax calls inside the iframe (connected to portal), and sends the result to the topLayer to display in a lightbox.
	function createDSLightbox(id) {
		var action = "/portal/dictionary/dataStructureAction!lightboxView.ajax?dataStructureId=" + id + "&publicArea=true";
		$.ajax(action, {
			"type": 	"POST",
			"async": 	false,
			"success": 	function(data) {
							top.postMessage(data, "*");
						}
		});
	}
	function createDELightbox(deId, dsId) {
		var action = "/portal/publicData/dataElementAction!viewDetails.ajax?dataElementId=" + deId + "&publicArea=true&dataStructureId=" + dsId;
		$.ajax(action, {
			"type": 	"POST",
			"async": 	false,
			"success": 	function(data) {
							top.postMessage(data, "*");
						}
		});
	}
	
	// Listens for messages from the top window to get details on another entity.
	// window.addEventListener("message", receiveMessage, false);
	
	function receiveMessage(event)
	{
		// var deId = event.data["deId"];
		// var dsId = event.data["dsId"];
		var args = event.data.split(";");
		deId = args[0];
		dsId = args[1];
		if(deId != "")
			{
				createDELightbox(deId, dsId);
			}
		else if (dsId != undefined)
			{
				createDSLightbox(dsId);
			}
	}
	
</script>

<!--[if gt IE 8]><!-->
<script type="text/javascript">
//window.addEventListener("message", receiveMessage, false);
</script>
<!--<![endif]-->
<!--[if lte IE 8]><!-->
<script type="text/javascript">
//	window.attachEvent("onmessage", receiveMessage);
</script>
<!--<![endif]-->

</body></html>