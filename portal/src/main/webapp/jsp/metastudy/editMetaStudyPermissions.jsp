<%@include file="/common/taglibs.jsp"%>
<title>Edit Meta Study Permissions</title>

<div class="clear-float">
	<h1 class="float-left">Meta Study</h1>
</div>

<!-- begin .border-wrapper -->
<div class="border-wrapper">
	<jsp:include page="../navigation/metaStudyNavigation.jsp" />
	<!--begin #center-content -->
	<div id="main-content">

		<s:form id="theForm" cssClass="validate" method="post" name="studyDetailsForm" validate="true"
			enctype="multipart/form-data">
			<s:token />
			<h2>Edit Permissions</h2>
			<h3><s:property value="currentMetaStudy.title" /></h3>
			<p>Individual access to the meta study is granted below. Select the intended individual from the drop down to grant
				access. The permissions include read, write, or admin. The default permission is read. Also, individuals can be
				removed from the permission group here.</p>

			<p>
				<strong>Read - </strong>Allows user to view this meta study.<br /> <strong>Write - </strong>Allows user to view this
				meta study, edit study details, manage documentation, manage data, and specify keywords and labels.<br /> <strong>Admin / Owner - </strong>Allows
				user to view this meta study, edit meta study details, manage documentation, manage data, specify keywords and labels, and grant permissions. There can
				only be one Owner.
			</p>

			<br />
			<div id="permissionDivId"></div>

			<div class="form-field clear-left">
				<div class="button">
					<input type="button" value="Save" onClick="javascript:submitForm('metaStudyAction!submitPermissions.action')" />
				</div>
				<a class="form-link" href="metaStudyAction!view.action?metaStudyId=${currentMetaStudy.id}">Cancel</a>
			</div>

				<div>
					<p>
						<span class="inline-special-instruction">Note: Meta Study Administrators have administrative permission for all
							studies regardless of any permissions they have been explicitly granted.</span>
					</p>
				</div>
		</s:form>

	</div>
</div>

<script type="text/javascript">
	setNavigation({"bodyClass":"primary", "navigationLinkID":"metaStudyModuleLink", "subnavigationLinkID":"metaStudyLink", "tertiaryLinkID":"browseMetaStudyLink"});

	
	$('document').ready(function() 
			{ 
				getPermissions();
			}
	);
	
	function submitForm(action) {
		var theForm = document.forms["theForm"];
		
		var disableButtons = document.getElementsByTagName('input');
		var i;
		var length = disableButtons.length;
		for (i = 0; i < length; i++) {
			if (disableButtons[i].type == 'button') {
				disableButtons[i].disabled = true;
			}
		}
		if (action) {
			theForm.action = action;
		}
		theForm.submit();
	}
	
	function getPermissions()
	{
		var params = {entityIdParam: <s:property value='currentMetaStudy.id' />, entityTypeParam:"METASTUDY"};
	
		$.ajax({
			cache : false,
			url : "metaStudyPermissionAction!load.ajax",
			data : params,
			success : function (data) {
				$("#permissionDivId").html(data);
			}
		});
	}
</script>
