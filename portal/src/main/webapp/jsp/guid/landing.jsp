<%@ include file="/common/taglibs.jsp"%>
<title>GUID Overview</title>
<div class="clear-float">
	<h1 class="float-left">GUID (Global Unique Identifier)</h1>
</div>

<div class="border-wrapper">
	<jsp:include page="../navigation/guidNavigation.jsp" />
	
	<div id="main-content">

		<h2>GUID Overview</h2>

		<p>The GUID Tool is a customized software application that generates a Global Unique Identifier for each study participant. A GUID is a subject ID that allows researchers to share data specific to a study participant without exposing personally identifiable information (PII). A GUID is made up of random alpha-numeric characters and is NOT generated from PII/PHI. By using GUIDs in your research data, the system can associate a single research participant's genetic, imaging, clinical assessment data even if the data was collected at different locations or through different studies.</p>
		<p>In order to submit data to the system, the system expects all prospective studies to include a GUID in the data submission. For retrospective studies, the team understands that the participant data needed to generate a GUID may not be available. To account for this, the capability to generate pseudo-GUIDs is provided. However submitting data with pseudo-GUIDs, silos the associated research data from the other data associated with valid GUIDs.</p> 

		<h3>Fields required to generate a GUID</h3>
		<p>In order to generate a GUID, the following PII is required:</p>
		<ul>
			<li>Complete legal given (first)name of subject at birth</li>
			<li>If the subject has a middle name</li>
			<li>Complete legal family (last) name of subject at birth</li>
			<li>Day of birth</li>
			<li>Month of birth</li>
			<li>Year of birth</li>
			<li>Name of city/municipality in which subject was born</li>
			<li>Country of birth</li>
		</ul>
	</div>
</div>

<script type="text/javascript">
    //setNavigation({"bodyClass":"primary", "navigationLinkID":"", "subnavigationLinkID":"guidDataLink", "tertiaryLinkID":"listDataStructureLink"});
 	setNavigation({"bodyClass":"primary", "navigationLinkID":"guidModuleLink", "subnavigationLinkID":"guidDataLink", "tertiaryLinkID":"guidOverviewLink"});
</script>