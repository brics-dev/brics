<title>Create GUIDs</title>

<%@ include file="/common/taglibs.jsp"%>

<link rel="stylesheet" type="text/css" href="${guidWsUrl}../../styles/guidTool/guidTool.min.css" />
<%-- <script src="${guidWsUrl}../../js/guidTool/guidApi.min.js"></script> --%>
<script src="${guidWsUrl}../../js/guidTool/GlobalLibs.min.js"></script>
<script src="${guidWsUrl}../../js/guidTool/guidTool.min.js"></script>

<div class="clear-float">
	<h1 class="float-left">GUID (Global Unique Identifier)</h1>
</div>

<div class="border-wrapper">

	<jsp:include page="../navigation/guidNavigation.jsp" />
	<div id="main-content">

		<h2>Create GUIDs</h2>

		<p>In order to protect the privacy of study participants, the GUID tool is run locally. Upon accepting the terms of the Data Privacy policy, the GUID tool will open and run directly on the page.</p>
		<h3>Helpful Documentation</h3>
		<ul>
			<li>Getting started and need help? Download the <a href="fileDownloadAction!download.action?fileId=3">GUID User Guide (pdf)</a></li>
			<li>Need to generate multiple GUIDs at one time? Download the <a href="fileDownloadAction!download.action?fileId=10">GUID Batch Template (csv)</a></li>
		</ul>
		<div class="guidClient">
		</div>
	</div>
</div>
<script type="text/javascript">

// jwt is in cookie
GuidClient.render({
	container: $(".guidClient"),
	url: "${guidWsUrl}"
});

setNavigation({"bodyClass":"primary", "navigationLinkID":"guidModuleLink", "subnavigationLinkID":"guidDataLink", "tertiaryLinkID":"createGUIDLink"});
</script>