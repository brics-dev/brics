<title>View Reports</title>
<%@include file="/common/taglibs.jsp"%>

<!-- begin .border-wrapper -->
<div class="border-wrapper wide">
	<jsp:include page="../navigation/reportNavigation.jsp" />
	<h1 class="float-left">Reporting</h1>
	<div style="clear:both"></div>
	
	<!--begin #center-content -->
	<div id="main-content">
		<div class="clear-float">
				<h2>How to use the reporting module</h2>	
				<br/>
				<p>The reporting module allows you to view and download metadata associated with studies and user accounts in the system. You will see a subset of the available metadata for each report in a tabular format, but the downloaded report will provide additional metadata that you can view, sort, and filter on. You can download the report as a XLS, CSV, or PDF.<p>
				
				<p>To use the reporting module: </p>
				
				<p>
					<ol>
					  <li>Choose a report to run.</li>
					  <li>If available, choose filters to apply and click the "Apply Filters" button.</li>
					  <li>Further filtering and sorting options within the table  include the following:
						  	<ul style="list-style-type:circle">
					  			<li>Typing text next to the "Search" box in the top right corner </br>of the table and selecting to search within one or more of </br>the column headings.</li>
					  			<li>Clicking the sort order icon next to column titles in the table.</li>
					  		</ul>
					  </li>
					  <li>Click through the pages of the table by using the buttons at the bottom of the table.</li>
					  <li>Click to download the report as an XLS, CSV, and/or a PDF. Note that the downloaded report currently contains all metadata for the report (not just the filtered/sorted results).</li>
					</ol>
				</p>
				</br>
					<div class="button margin-right" style="float: left;">
						<input type="button" onclick="window.location.href='/reporting/viewstudiesreports/studiesReportingListAction!list.action'" value="RUN A REPORT">
					</div>
		</div>
	</div>
</div>

<script type="text/javascript">
	<s:if test="isHowToPage">
	 	setNavigation({"bodyClass":"primary", "navigationLinkID":"reportingModuleLink", "subnavigationLinkID":"studyToolLink", "tertiaryLinkID":"createStudyLink"});
	</s:if>
 	
</script>