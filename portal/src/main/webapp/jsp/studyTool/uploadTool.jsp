<%@include file="/common/taglibs.jsp"%>
<title>Upload Data</title>

<div class="border-wrapper wide">
	<!-- 		Left menus -->
	<jsp:include page="../navigation/dataRepositoryNavigation.jsp" />
	<h1 class="float-left">Data Repository</h1>
	<div style="clear:both"></div>

	<!-- 		The pages main content -->
	<div id="main-content">

		<h2>Upload Data</h2>
		<p>After successful creation of a Submission Package using the Validation tool, data can now be submitted to the
			system. The Upload Tool facilitates this process. The tool, which runs as a Java Web Start
			application, runs locally on a user's computer, requiring the Java runtime environment to be installed.</p>

		<ul>
			<li><ndar:actionLink action="baseAction!launch.action" value="Launch the Upload Tool" paramName="webstart"
					paramValue="uploadTool" /></li>
		</ul>

		<h3>Steps to Run the Upload Tool:</h3>

		<ol>
			<li>Launch Upload Tool</li>
			<li>Using the dropdown menu, select the Study to which you wish to upload data.</li>
			<li>Click Browse next to Submission Ticket (XML)</li>
			<li>Navigate to location on your computer of the working directory where the file for submission is located. Select the file, and then click Open</li>
			<li>Enter a Dataset Name
				<ul>
					<li><strong>Note:</strong> The name must be unique to the selected Study.</li>
				</ul>
			</li>
			<li>Select the Upload button
				<ul>
					<li><strong>Note:</strong> Screen will update as file(s) are being uploaded to the system. If Upload is
						successful, the Status will be designated as Completed</li>
				</ul>
			</li>
		</ol>



	</div>
</div>
<script type="text/javascript">

	//Sets the navigation menus on the page
	setNavigation({"bodyClass":"primary", "navigationLinkID":"dataRepositoryModuleLink", "subnavigationLinkID":"uploadDataLink", "tertiaryLinkID":"uploadToolPageLink"});

</script>
</body>
</html>