<%@include file="/common/taglibs.jsp"%>
<title>Study Confirmation</title>

<div class="border-wrapper">
	<jsp:include page="../navigation/dataRepositoryNavigation.jsp" />
	<h1>Data Repository</h1>
	<div style="clear:both"></div>

	<div id="main-content">
		<h2>Study Confirmation</h2>
		<p>Thank you for your request to create a study. Within the next 24-48 hours, it will be reviewed by an
			administrator. You will receive an approval or rejection email notifying you of the study's status.</p>
	</div>
</div>

<script type="text/javascript">
	setNavigation({"bodyClass":"primary", "navigationLinkID":"dataRepositoryModuleLink", "subnavigationLinkID":"studyToolLink", "tertiaryLinkID":"createStudyLink"});
</script>