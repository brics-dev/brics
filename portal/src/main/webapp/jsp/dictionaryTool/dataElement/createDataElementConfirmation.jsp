<%@include file="/common/taglibs.jsp"%>
<title>Data Element Confirmation</title>

<div class="clear-float">
	
</div>

<div class="border-wrapper">
	<jsp:include page="../../navigation/dataDictionaryNavigation.jsp" />
	<h1 class="float-left">Data Element Confirmation</h1>
	<div id="main-content" style="margin-top:25px; min-height:400px;">
		<h2></h2>
		<p>Your data element has been created. The data element will be in a DRAFT state until you request for it to be
			published. Publication of data elements are subject to administrative review and approval.</p>
		<p>You may publish a data element through two different methods:
		<ol>
			<li>Requesting publication from the data element screen itself, or</li>
			<li>Published automatically when requesting the publication of a form structure with the draft data element
				attached</li>
		</ol>
	</div>
</div>

<script type="text/javascript">
	setNavigation({"bodyClass":"primary", "navigationLinkID":"", "subnavigationLinkID":"dataDictionaryToolLink", "tertiaryLinkID":"dataElementLink"});
</script>