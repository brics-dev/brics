<%@include file="/common/taglibs.jsp"%>
<title>Validate Data</title>

<!-- begin .border-wrapper -->
<div class="border-wrapper wide">
	<jsp:include page="../navigation/dataRepositoryNavigation.jsp" />
	<h1 class="float-left">Data Repository</h1>
	<div style="clear:both"></div>

	<!-- The pages main content -->
	<div id="main-content" style="margin-top:15px;">

		<h2>Validate Data</h2>

		<p>The Validation Tool assists researchers with the submission of data into
			the repository. The Validation Tool verifies that submitted data conforms to the required format
			and range values defined in the Data Dictionary. The Validation Tool imports the Data Dictionary and
			validates the metadata associated with the files identified by the user for submission against the data
			dictionary. The tool provides a report of any data discrepancies and warnings. If errors are found, a submission
			package cannot be created. The tool, which runs as a Java Web Start application, runs locally on a user's computer,
			requiring the Java runtime environment to be installed.</p>

		<ul>
			<li><ndar:actionLink action="baseAction!launch.action" value="Launch the Validation Tool" paramName="webstart" paramValue="validationTool" /></li>
		</ul>
		<br />
		<h3>Steps to Run the Validation Tool:</h3>
		<ol>
			<li>Click Launch Validation Tool</li>
			<li>Click Browse</li>
			<li>Navigate to the location on your computer of the working directory where the files for submission are located.</li>
			<li>Select the desired files and click OK
				<ul>
					<li><strong>Note:</strong> For instructions on how to format data prior to submission to the Validation Tool, see the Standard Operating Procedures for  Formatting Data for the Validation Tool, Data Element Creation, and Form Structure Creation </li>
				</ul>
			</li>
			<li>Click Load Files
				<ul>
					<li>All files in the directory will be displayed.</li>
				</ul>
			</li>
			<li>Select individual file(s) (click to highlight) that are of TYPE UNKNOWN and those files not needed for the submission. Hold Ctrl while clicking in order to highlight multiple files.</li>
			<li>Click EXCLUDE FILES.</li>
			<li>Select the remaining file(s) (click to highlight) to be validated.</li>  
			<li>Click VALIDATE FILES
				<ul>
					<li><strong>Note:</strong> If there are any errors or warnings associated with the file, it is displayed in the Result Details table. Files with warnings can be validated. However, files with errors must be fixed prior to being able to validate the file. By clicking the Export Result Details, a text file is created and stored in the same directory as your working files.</li>
				</ul>
			</li>
			<li>Select remaining file(s) with no errors in table (click to highlight) and then click Include Files</li>
			<li>Click Validate Files button.
				<ul>
					<li><strong>Note:</strong> If there are no errors, Click OK  All files are valid. If there are errors, make corrections to the file and repeat steps above.</li>
				</ul>
			</li>
			<li>Click Build Submission Package.
				<ul>
					<li><strong>Note:</strong> The submission package will be deposited in the same working directory as the original files submitted to the Validation Tool.</li>
				</ul>
			</li>
		</ol>
<!-- 		Removed until functionality can be fully checked -->
<!-- 		<p>A <a href="/portal/client_apps/CmdLineValTool.zip">Command Line Validation Tool</a> is also available for users without a desktop or users who need to run the tool remotely.</p> -->
	</div>
</div>
<script type="text/javascript">

	//Sets the navigation menus on the page
	setNavigation({"bodyClass":"primary", "navigationLinkID":"dataRepositoryModuleLink", "subnavigationLinkID":"validationToolLink", "tertiaryLinkID":"vtToolPageLink"});

</script>
</body>
</html>