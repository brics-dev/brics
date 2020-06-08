<%@include file="/common/taglibs.jsp"%>
<title>Download Queue</title>
<!-- begin .border-wrapper -->
<div class="border-wrapper wide">
	<jsp:include page="../navigation/dataRepositoryNavigation.jsp" />
	<s:if test="!inAdmin">
		<h1 class="float-left">Data Repository</h1>
	</s:if>
	<div style="clear:both"></div>

	<!--begin #center-content -->
	<div id="main-content">
		<div class="clear-float">

			<h2>Download Data</h2>
			<p>The Download Tool provides the capability to export packages (single or multiple datasets) from the system.  Users may download selected packages from the repository to their own system using either 
			(1) the existing WebStart application, which requires Java runtime environment (version 7-8 only) or (2) the beta JavasScript, browser based version of the tool. <b>Note:</b> Any Java version higher than 8
			is not compatible with the Web Start application.</p>
		
			<div style="width: 39%; margin: 10px 5%; float: left">
				<div class="button" style="clear: both; float: none; width: 30%; margin: 20px 35%">
					<input type="button"
						onclick="parent.location.href='downloadQueueAction!viewWebstartDownloadTool.action'"
						value="Web Start Application" />
				</div>
				<p>
					Existing application that requires installation and a user having Java runtime environment (versions 7-8) on their machine. Java Web Start allows Java applications to be transferred over the internet and started without a browser.
				</p>
				<p>
					<h3>Version Overview:</h3>
					<ul>
						<li>Allows for multiple package downloads</li>
						<li>Allows for system defined folder structure(s) for dataset organization</li>
						<li>Requires Java Runtime Environment</li>
						<li>Note: If you do not have Java Runtime Environment (version 8) installed, you can use the OpenJDK version.  Please reach out to a member of your Operations Support Team for instructions on how to perform this.
					</ul>
				</p>
			</div>
			<div style="width: 39%; margin: 10px 5%; float: left">
				
				<div style="clear: both; margin: 20px; text-align: center;">
					<span style="font-weight: bold; color: red; font-size: 1.5em; margin-right: 5px;">Beta Version</span>
					<div class="button" style="clear: both; float: none;">
						<input type="button"
							onclick="parent.location.href='downloadQueueAction!viewJsDownloadTool.action'"
							value="JavaScript Application" />
					</div>
				</div>
				<p>
					Beta version of the application that allows downloads to be performed directly from your web browser. At this time, the beta version is meant for smaller data packages and packages without attachments.
				</p>
				<p>
					<h3>Version Overview:</h3>
					<ul>
						<li>No software installation is required</li>
						<li>Allows downloading a single package at a time</li>
						<li>Does not create a system defined folder structure(s) for dataset organization</li>
						<li>User should define the default download location within the browser settings.  If not defined, depending on your browser, files may be downloaded to a default "Download" folder on your machine or default to your Desktop.</li>
					</ul>
				</p>
			</div>
		</div>
	</div>
	
</div>

	<script type="text/javascript">
	setNavigation({"bodyClass":"primary", "navigationLinkID":"dataRepositoryModuleLink", "subnavigationLinkID":"downloadToolLink", "tertiaryLinkID":"downloadQueueLink"});

	</script>
</body>
</html>