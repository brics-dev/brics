<%@include file="/common/taglibs.jsp"%>
<title>Create Meta Study</title>

<script type="text/javascript" src='/portal/js/metastudy/SavedQueryViewEngine.js'></script>
<script type="text/javascript" src='/portal/js/metastudy/SavedQuery.js'></script>

<script type="text/javascript" src='/portal/js/metastudy/SavedQueryDe.js'></script>
<script type="text/javascript" src='/portal/js/metastudy/SavedQueryDes.js'></script>

<script type="text/javascript" src='/portal/js/metastudy/SavedQueryForm.js'></script>
<script type="text/javascript" src='/portal/js/metastudy/SavedQueryForms.js'></script>

<script type="text/javascript" src='/portal/js/metastudy/SavedQueryRg.js'></script>
<script type="text/javascript" src='/portal/js/metastudy/SavedQueryRgs.js'></script>

<script type="text/javascript" src='/portal/js/metastudy/SavedQueryStudy.js'></script>
<script type="text/javascript" src='/portal/js/metastudy/SavedQueryStudies.js'></script>

<script type="text/javascript" src='/portal/js/metastudy/SavedQueryDeView.js'></script>
<script type="text/javascript" src='/portal/js/metastudy/SavedQueryRgView.js'></script>
<script type="text/javascript" src='/portal/js/metastudy/SavedQueryFormView.js'></script>
<script type="text/javascript" src='/portal/js/metastudy/SavedQueryStudyView.js'></script>
<script type="text/javascript" src='/portal/js/metastudy/SavedQueryView.js'></script>

<!-- begin .border-wrapper -->
<div class="border-wrapper wide">
	<jsp:include page="../navigation/metaStudyNavigation.jsp" />
	<h1>Create Meta Study</h1>
	<!--begin #center-content -->
	
	<div id="main-content">
		<h2>Create Meta Study</h2>
		<s:form id="theForm" cssClass="validate" method="post" validate="true" enctype="multipart/form-data">
			<s:token />
			<ndar:editMetaStudyChevron action="metaStudyAction" chevron="Data" />
			
			<h3>Data Artifact</h3>
				<jsp:include page="metaStudyDataTable-Edit.jsp" />
			<br/>
			
			<div class="form-field" style="display: inline-block;">
				<div class="button">
					<input type="button" value="Back" onClick="javascript:submitForm('metaStudyAction!moveToDocumentation.action')" />
				</div>
			</div>
			
			<div class="form-field inline-right-button">
				<div class="button btn-primary" style="margin-right: 10px;">
					<input type="button" value="Create & Finish" onclick="javascript:createMetaStudy()" />
				</div>
				<div class="button" style="margin-right: 5px;">
					<input type="button" value="Next" onClick="javascript:submitForm('metaStudyAction!moveToKeyword.action')" />
				</div>
				<a class="form-link" href="javascript:void(0)" onclick="javascript:cancelCreation()">Cancel</a>
			</div>
		</s:form>
		
		<div class="ibisMessaging-dialogContainer"></div>
    </div>
</div>

<script type="text/javascript" src="/portal/js/metastudy/metaStudy.js"></script>
<script type="text/javascript">
	setNavigation({"bodyClass":"primary", "navigationLinkID":"metaStudyModuleLink", "subnavigationLinkID":"metaStudyLink", "tertiaryLinkID":"createMetaStudyLink"});

	$("#addDataBtn").click( function(e) {
		$("#selectAddDataDiv").toggle("blind", 100);
	});
	
</script>