<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn"%>

<script id="pageMain" type="text/x-handlebars-template">
	<div class="ibisMessaging-flashContainer"></div>
	<div class="ibisMessaging-dialogContainer"></div>
	<header id="header" class="header">
		<div id="instanceHeaderOne" class="">
			<a href="javascript:;" class="bannerHome">
		<c:if test="${fn:contains(applicationConstants.styleKey, 'localhost')}">  
		<img name="" src="instances/default/images/header.png"></img>
			
		</c:if>
		<c:if test="${fn:contains(applicationConstants.styleKey, 'pdbp')}">  
		<img name="" src="instances/pdbp/images/header.png"></img>
		
		</c:if>
		<c:if test="${fn:contains(applicationConstants.styleKey, 'cistar')}"> 
			<img name="" src="instances/cistar/images/header.png"></img>
		</c:if>
		<c:if test="${fn:contains(applicationConstants.styleKey, 'cnrm')}">
			<img name="" src="instances/cnrm/images/header.png"></img>
		</c:if>  
		<c:if test="${fn:contains(applicationConstants.styleKey, 'eyegene') || fn:contains(applicationConstants.styleKey, 'nei')}">  
			<img name="" src="instances/eyegene/images/header.png"></img>
		</c:if>
		<c:if test="${fn:contains(applicationConstants.styleKey, 'fitbir')}"> 
			<img name="" src="instances/fitbir/images/header.png" style="height: 39px; padding-right: 10px;background: #ececec; margin-right:10px;"></img>
		</c:if>
		<c:if test="${fn:contains(applicationConstants.styleKey, 'cdrns')}"> 
			<img name="" src="instances/cdrns/images/header.png" style="height: 43px; padding-right: 10px; margin-right:10px;"></img>
		</c:if>
		<c:if test="${fn:contains(applicationConstants.styleKey, 'nti')}">  
			<img name="" src="instances/nti/images/header.png"></img>
		</c:if>
		<c:if test="${fn:contains(applicationConstants.styleKey, 'gsdr')}">  
			<img name="" src="instances/gsdr/images/header.png"></img>
		</c:if>
				
			</a>
		</div>
		<div id="instanceHeaderTwo" class="">


		<c:if test="${fn:contains(applicationConstants.styleKey, 'localhost')}">  
		<span><b>BRICS</b>  Biomedical Research Informatics Computing System</span>
			
		</c:if>
		<c:if test="${fn:contains(applicationConstants.styleKey, 'pdbp')}">  
		<span><b>PDBP DMR</b>  Parkinson's Disease Biomarkers Program Data Management Resources</span>
		
		</c:if>
		<c:if test="${fn:contains(applicationConstants.styleKey, 'cistar')}"> 
			<span></span>
		</c:if>
		<c:if test="${fn:contains(applicationConstants.styleKey, 'cnrm')}">
			<span></span>
		</c:if>
		<c:if test="${fn:contains(applicationConstants.styleKey, 'eyegene') || fn:contains(applicationConstants.styleKey, 'nei')}">  
			<span></span>
		</c:if>  
		<c:if test="${fn:contains(applicationConstants.styleKey, 'fitbir')}"> 
			<span></span>
		</c:if>
		<c:if test="${fn:contains(applicationConstants.styleKey, 'gsdr')}"> 
			<span></span>
		</c:if>
		<c:if test="${fn:contains(applicationConstants.styleKey, 'cdrns')}"> 
			<span></span>
		</c:if>
		<c:if test="${fn:contains(applicationConstants.styleKey, 'nti')}"> 
			<span><b>NTRR</b> National Trauma Research Repository</span>
		</c:if>
			
		</div>
		<div id="userMenu" class="">
			<span>Welcome <span class="username"></span> | <a href="javascript:;" class="logout"> Log Out </a></span>
		</div>
		<div class="clearfix"></div>
	</header>
	<nav id="navigation" class="navigation">
		<div class="navHomeSpan mainNavLink"><a href="javascript:;" class="navHome navLink">Home</a></div>
		<div class="navWorkspaceSpan mainNavLink"><a href="javascript:;" class="navWorkspace navLink">Workspace</a></div>
		<div class="navProformsSpan mainNavLink"><a href="javascript:;" class="navProforms navLink">ProForms</a></div>
		<div class="navGuidSpan mainNavLink"><a href="javascript:;" class="navGuid navLink">GUID</a></div>
		<div class="navDataDictSpan mainNavLink"><a href="javascript:;" class="navDataDict navLink">Data Dictionary</a></div>
		<div class="navDataRepoSpan mainNavLink"><a href="javascript:;" class="navDataRepo navLink">Data Repository</a></div>
		<div class="navQuerySpan mainNavLink"><span class="navQuery navLink">Query</span></div>
		<div class="navMetaSpan mainNavLink"><a href="javascript:;" class="navMeta navLink">Meta Study</a></div>
		<div class="navAcctSpan mainNavLink"><a href="javascript:;" class="navAcct navLink">Account Management</a></div>
		<div class="navReportingSpan mainNavLink"><a href="javascript:;" class="navReporting navLink">Reporting</a></div>
	</nav>
	<div id="mainContent" class="mainContent tabContainer">
		<div class="ibisMessaging-primaryContainer"></div>
			<div id="outerTabsButtonsContainer" style="padding-right: 19px;">
				<div id="saveNewQueryDiv" title="Save the current query to your profile">
					<a href="javascript:;" class="saveQuery buttonWithIcon disabled">
						<span class="icon pe-is-e-zoom-in"></span>
						Save New Query
					</a>
				</div>
				<div id="clearDataCartDiv" title="Clear all currently loaded forms in the data cart">
					<a href="javascript:;" class="clearDataCart buttonWithIcon disabled">
						<span class="icon pe-is-ec-cart-ban"></span>
						Clear Data Cart
					</a>
				</div>
				<div id="dataCartButtonDiv">
				</div>
			</div>
		<div class="adminButtonsContainer" style="display:none" title="Continue to Rbox">
			<div>
				<a href="javascript:;" id="rboxButton" class="buttonWithIcon disabled">
					<span class="icon pe-is-i-inside"></span>
					Rbox
				</a>
			</div>
			<div id="clearMetadata" title="Clear the Query Tool metadata cache. This should ONLY be done if Query Tool is acting slow.">
				<a href="javascript:;" class="clearMetadata buttonWithIcon">
					<span class="icon pe-is-i-close-circle-f"></span>
					Clear Cache
				</a>
			</div>
			<c:if test="${fn:contains(applicationConstants.styleKey, 'pdbp')}">  
				<div id="recordCountReport">
					<a href="service/recordCount/get" class="recordCount buttonWithIcon">
						<span class="icon pe-is-i-inside"></span>
						Download Report
					</a>
				</div>
			</c:if>
			<div class="adminOnlyLabel">Admin Only:</div>
		</div>
		<ul>
			<li class="mainTab">
				<a href="#stepOneTab">Step 1:Filter Data</a>
			</li>
			<li class="mainTab">
				<a href="#stepTwoTab">Step 2:Refine Data</a>
			</li>
			
		</ul>
		<div class="tab-content">
			<div id="stepOneTab" class="tab-pane">
				<p>step One</p>
			</div>
			<div id="stepTwoTab" class="tab-pane">
				<p>step Two</p>
			
			</div>
		</div>
	</div>
	<div id="selectDeDialog"></div>
	<div id="detailsDialog"></div>
	<div id="dataCartRemove"></div>
	<div id="downloadDialog"></div>
	<div id="metaStudyDialog"></div>
	<div id="imageDialog"></div>
	<div id="reloadSessionDialog"></div>
	<div class="viewQueryContainer"></div>
	<div id="fileDownloadDialog"></div>
	<div id="sendToMetaStudyValidationDialog"></div>
</script>

<script type="text/config" id="savedQueryData"></script>