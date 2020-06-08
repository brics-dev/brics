<%@include file="/common/taglibs.jsp"%>
<title>Validate and Upload Data</title>

<div class="border-wrapper wide">
	<!-- 		Left menus -->
	<jsp:include page="../navigation/dataRepositoryNavigation.jsp" />
	<h1 class="float-left">Data Repository</h1>
	<div style="clear:both"></div>

	<!-- 		The pages main content -->
	<div id="main-content">

		<h2>Validate and Upload Data</h2>

		<p>The Submission Tools assist researchers with the validation and upload of data into
			the repository. The validation component verifies that submitted data conforms to the required format
			and range values defined in the Data Dictionary. The tool imports the Data Dictionary and
			validates the metadata associated with the files identified by the user for submission against the data
			dictionary. It provides a report of any data discrepancies and warnings. If errors are found, a submission
			package cannot be created. After successful creation of a submission package, data can be submitted to the 
			system with the upload component.  The tool, which runs as a Java Web Start application, runs locally on a 
			user's computer, requiring the Java runtime environment to be installed.</p>

		<br>
		<h3>Helpful Documentation</h3>
		<ul>
			<li>Getting started and need help? Download the <a href="fileDownloadAction!download.action?fileId=22">Submission Tools User Guide (pdf)</a></li>
		</ul>
		<br>
		<div class="action-button" style="float:none; display:inline-block;"><ndar:actionLink action="baseAction!launch.action" value="Launch Submission Tools" paramName="webstart" paramValue="submissionTool" /></div>
		<br>
		To launch the Submission Tool in 64-Bit mode, click <a href="baseAction!launch.action?webstart=submissionTool64">here</a>
		<br>
		NOTE: Make sure you have a 64-bit machine with ONLY 64-bit Java installed before running this version. You must uninstall the 32-bit Java version if you have both installed.

	</div>
</div>
<script type="text/javascript">

	//Sets the navigation menus on the page
	setNavigation({"bodyClass":"primary", "navigationLinkID":"dataRepositoryModuleLink", "subnavigationLinkID":"submissionToolLink", "tertiaryLinkID":"subToolPageLink"});

</script>
</body>
</html>