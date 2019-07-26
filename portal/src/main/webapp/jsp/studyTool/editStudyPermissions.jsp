<%@include file="/common/taglibs.jsp"%>
<title>Edit Study: ${sessionStudy.study.title}</title>

<!-- begin .border-wrapper -->
<div class="border-wrapper">
	<jsp:include page="../navigation/dataRepositoryNavigation.jsp" />
	<h1>Data Repository</h1>
	<div style="clear:both"></div>

	<!--begin #center-content -->
	<div id="main-content">
		<div id="breadcrumb">
			<a href="studyAction!list.action">View Studies</a> &nbsp;&gt;&nbsp;<a
				href="viewStudyAction!view.action?studyId=${sessionStudy.study.prefixedId}">${sessionStudy.study.title}</a>
			&nbsp;&gt;&nbsp;Grant Permissions
		</div>

		<s:form id="theForm" cssClass="validate" method="post" name="studyDetailsForm" validate="true"
			enctype="multipart/form-data">
			<s:token />
			<ndar:editStudyChevron action="studyAction" chevron="Grant Permissions" />
			<h2>Edit Study: ${sessionStudy.study.title}</h2>
			<h3>Grant Permissions</h3>
			<p>Individual access to the study is granted below. Select the intended individual from the drop down to grant
				access. The permissions include read, write, or admin. The default permission is read. Also, individuals can be
				removed from the permission group here.</p>

			<p>
				<strong>Read - </strong>Allows user to view this study.<br /> <strong>Write - </strong>Allows user to view this
				study, edit study details, manage documentation, manage datasets.<br /> <strong>Admin / Owner - </strong>Allows
				user to view this study, edit study details, manage documentation, manage datasets and grant permissions. There can
				only be one Owner.
			</p>

			<br />
			<div id="permissionDivId"></div>

			<div class="form-field clear-left">
				<div class="button">
					<input type="button" value="Save & Finish" onClick="javascript:submitForm('studyAction!submit.action')" />
				</div>
				<a class="form-link" href="viewStudyAction!view.action?studyId=${sessionStudy.study.prefixedId}">Cancel</a>
			</div>

			<s:if test="isAdmin">
				<div>
					<p>
						<span class="inline-special-instruction">Note: Study Administrators have administrative permission for all
							studies regardless of any permissions they have been granted.</span>
					</p>
				</div>
			</s:if>
		</s:form>

	</div>
</div>

<script type="text/javascript">
	<s:if test="!inAdmin" >
		<s:if test="isCreate">
			setNavigation({"bodyClass":"primary", "navigationLinkID":"dataRepositoryModuleLink", "subnavigationLinkID":"studyToolLink", "tertiaryLinkID":"createStudyLink"});
		</s:if>
		<s:else>
			setNavigation({"bodyClass":"primary", "navigationLinkID":"dataRepositoryModuleLink", "subnavigationLinkID":"studyToolLink", "tertiaryLinkID":"browseStudyLink"});
		</s:else>
	</s:if>
	<s:else>
		setNavigation({"bodyClass":"primary", "navigationLinkID":"dataRepositoryModuleLink", "subnavigationLinkID":"contributeDataToolsLink", "tertiaryLinkID":"studyList"});
	</s:else>

	
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
		<s:if test="isCreate">
			var params = {entityIdParam: <s:property value='currentStudy.id' />, entityTypeParam:"STUDY"};
		</s:if>
		<s:else>
			var params = {entityIdParam:null, entityTypeParam:"STUDY"};
		</s:else>
	
		$.ajax({
			cache : false,
			url : "studyPermissionAction!load.ajax",
			data : params,
			success : function (data) {
				$("#permissionDivId").html(data);
			}
		});
	}
</script>
