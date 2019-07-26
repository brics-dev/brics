<%@ include file="/common/taglibs.jsp"%>

<script type="text/javascript" src="/portal/js/search/viewGuid.js"></script>

<title>GUID: <s:property value="%{currentSubject.guid}"/></title>

<div class="clear-float">
	<h1 class="float-left">GUID (Global Unique Identifier)</h1>
</div>

<s:if test="inAdmin">
<script>var inAdmin = true;</script>
</s:if>
<s:else>
<script>var inAdmin = false;</script>
</s:else>


<div class="border-wrapper">

	<jsp:include page="../navigation/guidNavigation.jsp" />

	<div id="main-content">

		<div id="guidDatasets" class="idtTableContainer">
			<h3>Datasets where GUID has been used</h3>
			<table id="guidDatasetsTable" class="table table-striped table-bordered">
			</table>
		</div>
		<!-- table load script is in viewGuid.js -->
	</div>
</div>

<script id="guidViewMain" type="text/x-handlebars-template">
		<div id="breadcrumb">
			{{#if inAdmin}}
				<s:a action="guidAdminAction!list.action">View All GUIDs</s:a>  &gt; {{guid}}
			{{else}}
				<s:a action="guidAction!list.action">My GUIDs</s:a>  &gt; {{guid}}
			{{/if}}
		</div>

		<h2>GUID:&nbsp;{{guid}}</h2>

		<br>
		<div class="clear-float">
			<div id="linkedContainer" class="form-output" style="display:none">
				<label>Linked To</label>
				<div class="readonly-text" id="linkedList"></div>
			</div>
		</div>

		<br>
		<div id="registrations" class="idtTableContainer">
			<h3>Users Registered By</h3>
			<table class="display-data full-width" id="registrationTable">
			</table>
			<!-- table load script is in viewGuid.js -->
		</div>

		<div id="relatedGuids" class="idtTableContainer">
			<h3>Related GUIDs</h3>
			<table class="display-data full-width" id="relatedGuidsTable">
			</table>
			<!-- table load script is in viewGuid.js -->
		</div>

</script>

<script type="text/javascript">
	    <s:if test="%{inAdmin == true}">
	    	setNavigation({"bodyClass":"primary", "navigationLinkID":"guidModuleLink","subnavigationLinkID":"guidToolLink", "tertiaryLinkID":"listGuidsLink"});
		</s:if>
		<s:else>
			setNavigation({"bodyClass":"primary", "navigationLinkID":"guidModuleLink", "subnavigationLinkID":"guidDataLink", "tertiaryLinkID":"myGuidDataLink"});
		</s:else>

var guidDetailsStr = "<s:property value="%{guidDetails}"/>";
</script>