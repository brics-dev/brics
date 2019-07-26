<%@include file="/common/taglibs.jsp"%>

<input id="isApproved" type="hidden" name="isApproved" value="${isApproved}" />
<div>
	<h3>Existing Files</h3>
	<div id="currentUserFilesContainer" class="idtTableContainer">
		<div id="dialog"></div>
		<table id="currentUserFilesTable"
			class="table table-striped table-bordered" width="100%"></table>
	</div>
</div>

<div id="deleteUserFileLightboxDiv" style="display: none">

</div>


<div id="changeFileTypeLightboxDiv" style="display: none">

</div>

<div id="changeFileNameLightboxDiv" style="display: none">

</div>

<div id="addFileDialog" style="display: none">
	<div id="addFileInterface"></div>
</div>


<script type="text/javascript">
	
	function deleteUserFileLightbox(fileId,fileName) {
		var userFileId = fileId;
		
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
						deleteUserFile(userFileId);
						$(this).dialog("close");
				}
			} ]
		});
	}
	
	function addUserFileLightbox() {
		var actionUrl = "documentationAction!addFileLightbox.ajax";
		
		$.ajax({
			type: "GET",
			url:actionUrl,
			cache:false,
			success:function(data) {
				var fileDialog = $("#addFileDialog").dialog({
					title: "Add User File",
					height: 250,
					width: 800,
					autoOpen: true,
                    open: function() {
                    	$(this).html(data);
                    	$.ajax({
        					type : "GET",
        					url : "changeFileTypeAction!existingFileTypesAjax.ajax",
        					success : function(response) {
        						$("#existingFileTypes").val(response);
        					}
        				});
                    },
					buttons : [
						{
							id: "cancelBtn",
							text: "Cancel",
							click: function() {
								$(this).dialog('close');
							}
						},
						{
							id: "uploadBtn",
							text: "Upload",
							click: function() {
								$("#uploadBtn").attr("disabled", true);
								var fileType = $("#uploadDescription option:selected").val();
								console.log("File Type: " + fileType);
								var fileName = $("#fileBrowse").val();
								var fileData = $("#fileBrowse")[0].files[0];
								var userId = $("#userId").val();
								var isApproved = $("#isApproved").val();
								var existingFileTypes = $("#existingFileTypes").val();
								
								if(fileType === "- Select One -") {
									
								}
								
								console.log(fileData);
								
								var postData = new FormData();
								postData.append("uploadFileName", fileName);
								postData.append("uploadDescription", fileType);
								postData.append("upload", fileData);
							 	postData.append("userId", userId);
							 	postData.append("isApproved", isApproved);
							 	postData.append("existingFileTypes", existingFileTypes);
							 	
								$.ajax({
									type: "POST",
									url: "accountDocumentationValidationAction!uploadFile.ajax",
									data: postData,
									cache : false,
									processData : false,
									contentType: false,
									success: function(data) {
										if (data == "success") {
											$('#currentUserFilesTable').DataTable().ajax.reload();
											$('#accountHistoryTable').DataTable().ajax.reload();	
											
											var currentUserTable = $('#currentUserFilesTable').idtApi("getTableApi");
											var idtOptions =  $('#currentUserFilesTable').idtApi("getOptions");
											
											if(currentUserTable.rows().length) {
												var buttons = idtOptions.buttons;
												console.log("buttons", buttons);
												
												for(var i = 0; i < buttons.length; i++){
													if(buttons[i].id === "download-all-btn") {
														
														buttons[i].enabled = true;
														currentUserTable.buttons(i).enable(true)
													}					
												}
											}
											$('#currentUserFilesTable').DataTable().ajax.reload();
											fileDialog.dialog('close');
											
										} else if (data == "landing") {
											window.location.href = "/portal/baseAction!landing.action";
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
	
	function deleteUserFile(fileId) {
		var secondActionAjax = "fileDeleteAction!deleteUserFile.ajax";
		$.ajax({
			type : "post",
			data : {
				"deleteUserFileId" : fileId
			},
			url : secondActionAjax,

			success : function(response) {
				var currentUserTable = $('#currentUserFilesTable').idtApi("getTableApi");
				console.log(currentUserTable,"before");
				$('#currentUserFilesTable').DataTable().ajax.reload();
				$('#accountHistoryTable').DataTable().ajax.reload();
				console.log(currentUserTable,"after");
			},
			error : function(error) {
				alert(error)
			}
		});
		
		
	}
	
	
	
	function changeFileNameLightbox(fileId,currentFileName) {
		var dialogOne;
		var $changeFileNameDiv = $("#changeFileNameLightboxDiv");
		var firstActionAjax = "changeFileNameAction!changeFileNameLightbox.ajax";
		
		dialogOne = $changeFileNameDiv.dialog({

			autoOpen : false,
			modal : true,
			height : 200,
			width : 700,
			draggable : false,
			resizable : false,
			title : "Change File Name",
			buttons : [ {
				text : "Cancel",
				click : function() {
					$(this).dialog("close");
				}
			}, {
				text : "Change File Name",
				click : function() {
						var changedFileName = $("#changeFileName").val().trim();
						if(validateFileName(currentFileName,changedFileName)) {
							changeFileName(fileId,changedFileName);
							$(this).dialog("close");
						}
				}
			} ]
		});
		
		$.ajax({
			type : "post",
			url : firstActionAjax,

			success : function(response) {

				$changeFileNameDiv.html(response);
				
				$changeFileNameDiv.dialog("open");
				
				$("#changeFileName").val(currentFileName);
				
			}
		});
		
		
	}
	
	
	function validateFileName(currentFileName,changedFileName) {

		
		if(changedFileName.trim() == "" || currentFileName == changedFileName || changedFileName.length > 255) {
			$("#validateChangeFileName").show();
			return false;
		}else {
			return true;
		}
		
	}
	
	
	function changeFileName(fileId,changedFileName) {
		var secondActionAjax = "changeFileNameAction!changeFileName.ajax";
		
		
		$.ajax({
			type : "post",
			data : {
				"changedFileName" : changedFileName,
				"userFileId" : fileId
			},
			url : secondActionAjax,

			success : function(response) {
				if (response == "landing") {
					window.location.href = "/portal/baseAction!landing.action";
				} else {
					$('#currentUserFilesTable').DataTable().ajax.reload();
					$('#accountHistoryTable').DataTable().ajax.reload();
				}
			},
			error : function(error) {
				alert("error occurred while renmaing");
			}
		});
		
	}
	
	
	
	
	function changeFileTypeLightbox(fileId,currentFileTypeName) {
		var dialogOne;
		var $changeFileTypeDiv = $("#changeFileTypeLightboxDiv");
		var firstActionAjax = "changeFileTypeAction!changeFileTypeLightbox.ajax";
		
		dialogOne = $changeFileTypeDiv.dialog({

			autoOpen : false,
			modal : true,
			height : 200,
			width : 700,
			draggable : false,
			resizable : false,
			title : "Change File Type",
			buttons : [ {
				text : "Cancel",
				click : function() {
					$(this).dialog("close");
				}
			}, {
				text : "Change File Type",
				click : function() {
						var changedFileType = $("#changeFileType option:selected").val();
						changeFileType(fileId,changedFileType);
						$(this).dialog("close");
				}
			} ]
		});
		
		$.ajax({
			type : "post",
			url : firstActionAjax,

			success : function(response) {
				
				$changeFileTypeDiv.html(response);
				
				$.ajax({
					type : "GET",
					url : "changeFileTypeAction!existingFileTypesAjax.ajax",
					success : function(response) {
						$("#changeFileTypeExistingFileTypes").val(response);
					}
				});
				
				$changeFileTypeDiv.dialog("open");
				
				$("#changeFileType option").each(function () {
					if ($(this).html() == currentFileTypeName) {
			            $(this).attr("selected", "selected");
			            return;
			        }
				});
			}
		});

	}
	
	function validateFileType(currentFileTypeName,changedFileTypeText) {
		//to handle user selecting 'Select One'
		var selectedVal = $("#uploadDescription option:selected").val();
		if(selectedVal == "") {
			$("#validateChangeFileType").show();
			return false;
		}
		
		
		if(changedFileTypeText == "" || currentFileTypeName == changedFileTypeText) {
			$("#validateChangeFileType").show();
			return false;
		}else {
			return true;
		}
		
	}
	
	
	

	
	
	
	
	function changeFileType(fileId,changedFileType) {
		var secondActionAjax = "changeFileTypeValidationAction!changeFileType.ajax";
		var existingFileTypes = $("#existingFileTypes").val();
		
		$.ajax({
			type : "post",
			data : {
				"uploadDescription" : changedFileType,
				"userFileId" : fileId,
				"existingFileTypes" : existingFileTypes
			},
			url : secondActionAjax,

			success : function(response) {
				if(response == "success") {
					$('#currentUserFilesTable').DataTable().ajax.reload();
					$('#accountHistoryTable').DataTable().ajax.reload();
				} else if (response == "landing") {
					window.location.href = "/portal/baseAction!landing.action";
				} else {
					$("#changeFileTypeLightboxDiv").html(response).dialog("open");
				}
			},
			error : function(error) {
				alert(error)
			}
		});
		
		
	}
	
	
   function downloadAll() {
		
		
		var count = $('#currentUserFilesTable').idtApi("getRows").count();
	
		if(count > 0) {
			var downloadUrl = "fileDownloadAction!downloadAll.action";
			window.location.href = downloadUrl;
		}else {
			alert("There are no files to download");
		}
	}
   
   	function hasFiles() {
   		return $('#currentUserFilesTable').idtApi("getRows").count() > 0;
   	}
	
	$(document)
			.ready(
					function() {
						$('#currentUserFilesTable')
								.idtTable(
										{
											idtUrl : "<s:url value='/accounts/viewProfile!getExistingFiles.action' />", 
											dom: "Bfrtip",
											autoWidth: false,
											"columns" : [ {
												"data" : "fileName",
												"title" : "FILE NAME",
												"name" : "FILE NAME",
												"width": "28%",
												"parameter" : "fileName",

											}, {
												"data" : "fileType",
												"title" : "FILE TYPE",
												"name" : "FILE TYPE",
												"width": "39%",
												"parameter" : "fileType"
											}, {
												"data" : "dateUploaded",
												"title" : "DATE SUBMITTED",
												"name" : "DATE SUBMITTED",
												"parameter" : "dateUploaded"
											} ], 
											drawCallback : function (setting) {
												var currentUserTable = $('#currentUserFilesTable').idtApi("getTableApi");
												var idtOptions =  $('#currentUserFilesTable').idtApi("getOptions");
												//console.log("row length: "+currentUserTable.rows().count());
												if( currentUserTable.data().any()) {
													var buttons = idtOptions.buttons;
													console.log("enable button");
													
													for(var i = 0; i < buttons.length; i++){
														if(buttons[i].id === "download-all-btn") {
															
															buttons[i].enabled = false;
															currentUserTable.buttons(i).enable(true);
														}					
													}
												}
											},
											buttons : [<s:if test="!getOnlyReviewer()">{
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
											},</s:if> {
													id: "download-all-btn",
													text: "Download All",
													className: 'idt-createProtocolBtn',
													enabled: false,
													action: function(e, dt, node, config) {
														downloadAll();
									      	   		}
												}
											]
										})
					})
					
					
</script>
