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
				<p>
                   Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat. Duis aute irure dolor in reprehenderit in voluptate velit esse cillum dolore eu fugiat nulla pariatur. Excepteur sint occaecat cupidatat non proident, sunt in culpa qui officia deserunt mollit anim id est laborum.<br></br>
				   1. Lorem ipsum dolor sit amet,<br></br>
				   2. Lorem ipsum dolor sit amet,<br></br>
				   3. Lorem ipsum dolor sit amet,<br></br>
				   4. Lorem ipsum dolor sit amet,<br></br>
				</p>

					<div class="button margin-right" style="float: left;">
						<input type="button" onclick="window.location.href='/reporting/viewreports/reportingListAction!list.action'" value="RUN A REPORT">
					</div>
		</div>
	</div>
</div>

<script type="text/javascript">
	<s:if test="isHowToPage">
	 	setNavigation({"bodyClass":"primary", "navigationLinkID":"reportingModuleLink", "subnavigationLinkID":"studyToolLink", "tertiaryLinkID":"createStudyLink"});
	</s:if>
 	
</script>