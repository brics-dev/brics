<%@include file="/common/taglibs.jsp"%>
<title>Upload Data</title>

<div class="border-wrapper wide">
	<!-- 		Left menus -->
	<jsp:include page="../navigation/dataRepositoryNavigation.jsp" />
	<h1 class="float-left">Data Repository</h1>
	<div style="clear:both"></div>

	<!-- 		The pages main content -->
	<div id="main-content">

		<h2>MIPAV Imaging Tool</h2>
		<p>The MIPAV imaging tool allows users to submit unprocessed brain images in DICOM format and processed images in a variety of formats including DICOM, BIDS, MINC 1.0 and 2.0, Analyze, NIfTI-1, AFNI and SPM. If you are using a different file format, please contact your program administrator to have it added to the list of supported standards.
		
		</p><p>
To submit imaging data to the repository, researchers are required to run a component of the MIPAV (Medical Image Processing, Analysis, and Visualization) application. Using the MIPAV component, you can prepare your image data for submission by following the steps below.
</p>

<br>
<h3>Helpful Documentation</h3>
<ul>
<li>Getting started and need help? Download the <a href="fileDownloadAction!download.action?fileId=23">MIPAV Imaging Tool User Guide (pdf)</a></li>
</ul>
<br>

		<h3>Steps to Run the MIPAV Tool:</h3>
<ol>
<li>Place each of your images into a common directory accessible by your computer.</li>
<li>Select the link: <a href="${mIPAVClientURL}" target="_blank">MIPAV Imaging Tool</a>. This link will launch a Java web start application. A recent version of the 64-bit Java Runtime Environment (JRE) is needed to run the application.</li>
<li>Add Files to the program.</li>
<li>Enter appropriate metadata. Date of interview, image dimensions, GUID, dimensions, and site subject ID are all required. You may need to scroll down to access all required fields.</li>
<li>Add or Remove files, select the output directory, and select finish when meta-data items for all files have been completed.</li>
<li>Selecting Finish will generate all the files necessary for validation: 
<ul>
<li>The compressed image</li>
<li>A JPG that can be used as a thumbnail to preview the image in the Query Tool</li>
<li>A CSV of the meta-data from the image. The data matches a form structure that can be queried using the Query Tool.</li>
</ul>
</li>
<li>Validate the files using the Submission Tools.</li>
</ol>
<br/>
<div class="action-button" style="float:none; display:inline-block;"><a href="${mIPAVClientURL}" target="_blank">Launch MIPAV Imaging Tool</a></div>




	</div>
</div>
<script type="text/javascript">

	//Sets the navigation menus on the page
	setNavigation({"bodyClass":"primary", "navigationLinkID":"dataRepositoryModuleLink", "subnavigationLinkID":"mipavToolLink", "tertiaryLinkID":"mipavToolPageLink"});

</script>
</body>
</html>