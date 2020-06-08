<%@include file="/common/taglibs.jsp"%>
<title>Request Account Renewal</title>

<div class="clear-float">
	<h1 class="float-left">Account Management</h1>
</div>

<div class="border-wrapper wide">
	<jsp:include page="../navigation/userManagementNavigation.jsp" />

	<div id="main-content">
		<h2>Request Account Renewal</h2>
		
		<s:form id="theForm" cssClass="validate" method="post" validate="true" enctype="multipart/form-data">
				
			<div style="margin-top: 10px; margin-bottom: 40px;">
				<h3>Your privileges</h3>
				<div id="privilegeListContainer" class="idtTableContainer">
					<table id="privilegeListTable" class="table table-striped table-bordered" width="100%"></table>
				</div>
			</div>
				
			<h3>Upload Documents</h3>
			<s:if test="needTemplates">
				<div style="margin-top: 20px; margin-bottom: 20px;">
					<p>Your account type: <s:property value="accountType" /><br><br>
						Based on your account type, please upload appropriate signed documents using the "ADD" button.</p>
					<div id="fileTemplatesContainer" class="idtTableContainer">
						<table id="fileTemplatesTable" class="table table-striped table-bordered" width="100%"></table>
					</div>
				</div>
			</s:if>
			
			<div style="margin-top: 20px; margin-bottom: 10px;">
				<div id="userFilesContainer" class="idtTableContainer">
					<table id=currentUserFilesTable class="table table-striped table-bordered" width="100%"></table>
				</div>
			</div>
		
			<div>
				<p>Note: Please make sure you have uploaded the appropriate documents supporting your privilege renewal request. 
				Lacking in submitting appropriate documents will delay your account privilege renewal request. </p>
				<div class="form-field clear-left">
					<div id="buttonDiv" class="button <s:if test="!needRenewal">disabled</s:if>">
						<input type="button" id="submitRenewalBtn" <s:if test="!needRenewal">disabled</s:if> value="Renew Privileges" />
					</div>
				</div>
			</div>
			
			<div id="addFileDialog" style="display: none">
				<div id="addFileInterface"></div>
			</div>
			
			<div id="deleteUserFileLightboxDiv" style="display: none">
			</div>
		</s:form>
		
	</div>
</div>
<!-- end of .border-wrapper -->

<script type="text/javascript">
	setNavigation({
		"bodyClass" : "primary",
		"navigationLinkID" : "userManagementModuleLink",
		"subnavigationLinkID" : "userManagementToolLink",
		"tertiaryLinkID" : "renewalRequestLink"
	});
	
	$(document).ready(function() {
		$('#privilegeListTable').idtTable({
			idtUrl: "<s:url value='/accounts/renewalRequestAction!getExistingPrivileges.action' />",
			autoWidth: false,
			dom: "rt",
			"columns": [
				{
					"data": "privilege",
					"title": "PRIVILEGE",
					"name": "PRIVILEGE",
					"parameter": "decoratedPrivilege",
				},
				{
					"data": "roleStatus",
					"title": "STATUS",
					"name": "STATUS",
					"parameter": "decoratedStatus"
				}, 
				{
					"data": "expirationDate",
					"title": "EXPIRATION DATE",
					"name": "EXPIRATION DATE",
					"parameter": "decoratedExpirationDate"
				}
			]
		});
		
	<s:if test="needTemplates">
		var dsrTemplateLink = 
			<s:if test="userFileExists(6) eq true">"<a href='fileDownloadAction!download.action?fileId=6'>Data Submission Request</a>"</s:if>
			<s:else>"Data Submission Request"</s:else>;
			
		var darTemplateLink = 
			<s:if test="userFileExists(4) eq true">"<a href='fileDownloadAction!download.action?fileId=4'>Data Access Request</a>"</s:if>
			<s:else>"Data Access Request"</s:else>;

		var bioTemplateLink = 
			<s:if test="userFileExists(9) eq true">"<a href='fileDownloadAction!download.action?fileId=9'>Biographical Sketch</a>"</s:if>
			<s:else>"Biographical Sketch"</s:else>;

		$('#fileTemplatesTable').idtTable({
			"columns": [
				{
					"data": "accountType",
					"title": "ACCOUNT TYPE",
					"name": "Account Type",
					"width": "17%"
				},
				{
					"data": "fileTemplate",
					"title": "FILE TEMPLATE",
					"name": "File Template",
					"width": "36%"
				},
				{
					"data": "privilege",
					"title": "PRIVILEGE",
					"name": "Privilege",
					"width": "42%"
				}
			],
			dom: "rt",
			autoWidth: false,
			data: [
				{
					"accountType": "Data Submitter",
					"fileTemplate": dsrTemplateLink,
					"privilege": "Required for Data Submitter Users (Data Dictionary, Data Repository, GUID)"							
				},
				{
					"accountType": "Data Accessor",
					"fileTemplate": darTemplateLink + "&nbsp;&nbsp;" + bioTemplateLink,
					"privilege": "Required for Data Access Users (Data Dictionary, Data Repository, Query Tool, Meta Study)"							
				},
				{
					"accountType": "Data Submitter + Data Accessor",
					"fileTemplate": dsrTemplateLink + "&nbsp;&nbsp;" + darTemplateLink + "&nbsp;&nbsp;" + bioTemplateLink,
					"privilege": "Required for Data Submitter + Data Accessor (Data Dictionary, Data Repository, GUID, Query Tool, Meta Study)"							
				},
				{
					"accountType": "Other",
					"fileTemplate": "Please contact your Account Admin for next steps",
					"privilege": ""							
				}
			]
		});
		
	</s:if>
		
		$('#currentUserFilesTable').idtTable({
			idtUrl : "<s:url value='/accounts/renewalRequestAction!getExistingFiles.action' />", 
			dom: "Bfrt",
			autoWidth: false,
			"columns" : [{
				"data" : "fileName",
				"title" : "FILE NAME",
				"name" : "FILE NAME",
				"width": "28%",
				"parameter" : "fileName",
			}, {
				"data" : "description",
				"title" : "FILE DESCRIPTION",
				"name" : "FILE DESCRIPTION",
				"width": "39%",
				"parameter" : "description"
			}, {
				"data" : "dateUploaded",
				"title" : "DATE SUBMITTED",
				"name" : "DATE SUBMITTED",
				"parameter" : "dateUploaded"
			}], 
			buttons : [{
	     		 text: "Add",
	     		 className: 'idt-createProtocolBtn',
	     		 enabled: true,
	    		 enableControl: {
	                    count: 0,
	                    invert: false
	             },
	  	    	 action: function(e, dt, node, config) {
	  	    		addUserFileLightbox();
	      	   	}
			}]
		});
		
		
		$("#submitRenewalBtn").click(function() {
			$.ajax({
			    type: "GET",
			    url: "renewalRequestAction!submitRenewal.ajax",
			    success: function(data) {
			    	if (data == "success") {
			    		alert("Your renewal request has been submitted.");
			    		$('#privilegeListTable').DataTable().ajax.reload();
			    		$("#buttonDiv").addClass("disabled"); 
			    		$("#submitRenewalBtn").prop("disabled", true);
			    		
			    	} else if (data && data != null) {
			    		alert("Please Upload the latest signed " + data + " form(s).")
			    	}
			    }
			});
		});
		
	})

	
	function addUserFileLightbox() {
		$.ajax({
			type: "GET",
			url: "renewalDocumentationAction!addFileLightbox.ajax",
			cache: false,
			success: function(data) {
				var fileDialog = $("#addFileDialog").dialog({
					title: "Add User File",
					height: 250,
					width: 800,
					autoOpen: true,
                    open: function() {
                    	$(this).html(data);
                    },
					buttons: [
						{
							id: "cancelBtn",
							text: "Cancel",
							click: function() {
								$(this).dialog('close');
							}
						}, {
							id: "uploadBtn",
							text: "Upload",
							click: function() {
								$("#uploadBtn").attr("disabled", true);
								var fileType = $("#uploadDescription option:selected").val();
								var fileName = $("#fileBrowse").val();
								var fileData = $("#fileBrowse")[0].files[0];
								var userId = $("#userId").val();
								
								var postData = new FormData();
								postData.append("uploadFileName", fileName);
								postData.append("uploadDescription", fileType);
								postData.append("upload", fileData);
							 	postData.append("userId", userId);
							 	
								$.ajax({
									type: "POST",
									url: "documentationValidationAction!uploadFile.ajax",
									data: postData,
									cache : false,
									processData : false,
									contentType: false,
									success: function(data) {
										if (data == "success") {
											$('#currentUserFilesTable').DataTable().ajax.reload();
											fileDialog.dialog('close');
										} else {
											fileDialog.empty().html(data).dialog("open");
											$("#uploadBtn").attr("disabled", false);
										}
									}
								});
								
								fileDialog.html(data).dialog("open");
							}
						}
					]
				});	
			}	
		});
	}

	
	function deleteUserFileLightbox(fileId, fileName) {
		var dialogOne;
		var $deleteUserFileDiv = $("#deleteUserFileLightboxDiv");
		var firstActionAjax = "fileDeleteAction!deleteUserFileLightbox.ajax";
		var html = "<h1>Delete File</h1><br><p>Are you sure you want to delete " + fileName + "?</p>";
		$deleteUserFileDiv.html(html);
		
		dialogOne = $deleteUserFileDiv.dialog({
			modal : true,
			height : 200,
			width : 600,
			draggable : false,
			resizable : false,
			title : "Delete User File",
			buttons : [ {
				text : "Cancel",
				click : function() {
					$(this).dialog("close");
				}
			}, {
				text : "Delete",
				click : function() {
						$.ajax({
							type : "post",
							url : "fileDeleteAction!deleteUserFile.ajax",
							data : {
								"deleteUserFileId" : fileId
							},
							success : function(response) {
								var currentUserTable = $('#currentUserFilesTable').idtApi("getTableApi");
								$('#currentUserFilesTable').DataTable().ajax.reload();
							},
							error : function(error) {
								alert(error);
							}
						});
						$(this).dialog("close");
				}
			}]
		});
	}
	
</script>
