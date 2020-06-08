<%@include file="/common/taglibs.jsp"%>
<link rel="stylesheet" type="text/css" href='<s:url value="/downloadTool/styles/downloadTool.min.css"/>' />
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
			<h2>Download Tool - JavaScript Application</h2>
		</div>
		
		<div class="clear-float">

			<!-- This file must be in src/main/webapp/downloadTool_manual.docx -->
			<p><a href="<s:url value="/downloadTool_manual.docx"/>">Documentation: Download Tool</a></p>
			
			
			<h2>Steps to populate Download Queue</h2>
			
			<ol class="downloadToolList">	
				<li>Select a Package to Download in the Download Packages table.  <strong>Note:</strong> You can send multiple packages to the Download Packages queue.
				Although there may be multiple packages in your queue, you can only download one package at a time.</li>
				<li>Select a Package in the Package Contents table to download
					<ul>
						<li>
							The default download location is defined by the browser you are using and will be downloaded to your Desktop or Downloads (\Users\&lt;username&gt;\Downloads) folder.
							You can choose a location on your computer where downloads should be saved by defining a specific destination in your browser settings.  A separate folder should be
							created for each download package.  Please click one of the following for specific instructions on how to change your browser download settings:
							<ul>
								<li><a href="https://support.google.com/chrome/answer/95759?co=GENIE.Platform%3DDesktop&hl=en" target="_blank" rel="noopener noreferrer">Google Chrome</a></li>
								<li><a href="https://support.mozilla.org/en-US/kb/where-find-and-manage-downloaded-files-firefox" target="_blank" rel="noopener noreferrer">Mozilla Firefox</a></li>
								<li><a href="https://support.apple.com/en-gb/guide/safari/sfri40598/mac" target="_blank" rel="noopener noreferrer">Safari</a></li>
								<li><a href="https://support.microsoft.com/en-us/help/4026331/microsoft-edge-change-the-downloads-folder-location" target="_blank" rel="noopener noreferrer">Microsoft Edge</a></li>
								<li><a href="https://support.microsoft.com/en-us/help/17436/windows-internet-explorer-download-files-from-web" target="_blank" rel="noopener noreferrer">Internet Explorer</a>*
									<br /><strong>Note:</strong> Although compatible with Internet Explorer, it is recommended that you use a different web browser that conforms with current web standards to optimize the download tool experience
								</li>
							</ul>
						</li>
					</ul>
				</li>
				<li>
					After selecting a Package and defining a download location, Click Download
					<ul>
						<li>
							<strong>Note:</strong> The page and table will update as file(s) are being downloaded.  If the Download is successful, the Status will be designated as Completed.
						</li>		
					</ul>
				</li>
				<li>
					If you have more than one Package in your Download Packages queue, repeat steps 1-3
				</li>
			</ol>	
			
			<br>
			<p style="border:3px; border-style:solid; border-color:black; padding: 1em;"><strong>Warning: Files older than 30 days will automatically get deleted for security and performance reason. It is recommended that you save your queries in the Query Tool and ensure data-sets are frequently downloaded.</strong></p>
		</div>
		
		<div class="downloadToolClient"></div>
		
	</div>
	
</div>
	<!-- end of .border-wrapper -->
	<script type="text/javascript" charset="utf-8" src='<s:url value="/downloadTool/js/downloadTool.min.js"/>'></script>
	
	<script type="text/javascript">
	setNavigation({"bodyClass":"primary", "navigationLinkID":"dataRepositoryModuleLink", "subnavigationLinkID":"downloadToolLink", "tertiaryLinkID":"downloadQueueLink"});

	
	// Load a search at the start
	$('document').ready(function() {
		DownloadToolClient.render({
			container: $(".downloadToolClient"),
			apiBaseUrl: "<s:property value="%{microserviceBaseUrl}" />",
			orgEmail: "<s:property value="%{orgEmail}" />"
		});
	});
</script>
</body>
</html>