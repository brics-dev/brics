<title>My GUIDs</title>

<%@ include file="/common/taglibs.jsp"%>

<div class="clear-float">
	<h1 class="float-left">GUID (Global Unique Identifier)</h1>
</div>

<div class="border-wrapper">
	<jsp:include page="../navigation/guidNavigation.jsp" />
	<div id="main-content">

		<h2>My GUIDs</h2>
		<p>The table below displays a list of all GUIDs that you have registered from the one-way hashed PII information as well as PseudoGUIDs your organization has registered.</p>

		<s:set var="guidOwner" value="'mine'" />

		<script type="text/javascript" src="/portal/js/search/guidSearch.js"></script>
		<jsp:include page="../guid/guidTable.jsp"></jsp:include>
	</div>
</div>

<script type="text/javascript">
 	setNavigation({"bodyClass":"primary", "navigationLinkID":"guidModuleLink", "subnavigationLinkID":"guidDataLink", "tertiaryLinkID":"myGuidDataLink"});
</script>