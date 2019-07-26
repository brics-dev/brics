<title>Upload Documentation</title>
<%@include file="/common/taglibs.jsp"%>

<div class="clear-float">
	<h1 class="float-left">Account Management</h1>
</div>

<!-- begin .border-wrapper -->
<div class="border-wrapper">

	<!-- 		Left menus -->
	<jsp:include page="../navigation/userManagementNavigation.jsp" />

	<!--begin #center-content -->
	<div id="main-content">
		<h2>My Uploaded Documents</h2>
		
		<s:form id="theForm" action="accountAction" cssClass="validate" method="post" validate="true" enctype="multipart/form-data">
			<s:token />
			<!-- Display the upload module -->
			<jsp:include page="includes/uploadAdminFileInterface.jsp" />
			
			<div class="clear-both"></div>
			
			<!-- Output Existing Privileges -->
			<jsp:include page="includes/existingAdministrativeFiles.jsp" />
			
			<!-- Display the file templates section -->		
			<jsp:include page="includes/adminFileTemplatesInterface.jsp" />
		</s:form>
		
	</div>
</div>
<!-- end of .border-wrapper -->

<script type="text/javascript">
	//Sets the navigation menus on the page
	setNavigation({	"bodyClass" : "primary", "navigationLinkID" : "userManagementModuleLink", "subnavigationLinkID" : "userManagementToolLink",	"tertiaryLinkID" : "uploadDocumentationLink"	});
</script>