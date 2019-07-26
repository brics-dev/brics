<jsp:include page="/common/doctype.jsp" />
<%@ page import="gov.nih.nichd.ctdb.common.StrutsConstants"%>
<%@ page import="gov.nih.nichd.ctdb.protocol.common.ProtocolConstants" %>
<%@ page import="gov.nih.nichd.ctdb.attachments.manager.AttachmentManager" %>
<%@ page import="gov.nih.nichd.ctdb.common.rs,java.util.Locale" %>
<%@ taglib uri="/struts-tags" prefix="s" %>
<%@ taglib uri="/WEB-INF/security.tld" prefix="security"%>

<%-- CHECK PRIVILEGES --%>
<security:check privileges="viewstudyebinder" />

<jsp:useBean id="eBinderFormObj" scope="request" class="gov.nih.nichd.ctdb.protocol.form.StudyEbinderForm" />
<% Locale l = request.getLocale(); %>

<html>
	<s:set var="pageTitle" scope="request">
		<s:text name="ebinder.title.display"/>
	</s:set>
    <%-- Include Header --%>
	<jsp:include page="/common/header_struts2.jsp"/>
	
	<link rel="stylesheet" type="text/css" href="<s:property value="#webRoot"/>/common/css/jquery.filefolder-0.1.css" />
	<script src="<s:property value="#webRoot"/>/common/js/jquery.filefolder-0.1.full.js" type="text/javascript"></script>
	
	<s:if test="%{binderValid}">
		<script type="text/javascript">
			var canEdit = false;
		</script>
		<security:hasProtocolPrivilege privilege="editstudyebinder">
			<script type="text/javascript">
				var webRoot = "<s:property value='#webRoot'/>";
				var imr = new IbisMessageResource();
				canEdit = true;
								
				// Save the current tree sturcture in the database.
				function saveTree(jsonTree)
				{
					$("#jsonTree").val(jsonTree);
					
					$.ajax({
						type:	"POST",
						url:	webRoot + "/protocol/saveEbinder.action",
						data: {
							id: 		$("#binderId").val(),
							studyID:	$("#studyId").val(),
							type:		$("#binderType").val(),
							eBinderTree:	jsonTree
						}
					}).done(function(data, textStatus, jqXHR) {
						var response = null;
						
						// Check if there is a JSON object
						if ( data !== "null" )
						{
							response = $.parseJSON(data);
							
							if ( response !== null )
							{
								$("#binderId").val(response.id);
								$("#studyId").val(response.studyID);
								$("#binderType").val(response.type);
								$("#jsonTree").val(response.eBinderTree);
							}
						}
					}).fail(function(jqXHR, textStatus, errorThrown) {
						$.ibisMessaging("dialog", "error", jqXHR.responseText);
					});
				}
				
				// Delete the file from the server and database.
				function deleteFile(file, saveTreeStruct)
				{
					// Make the ajax call to the server
					$.ajax({
						type :	"POST",
						url :	webRoot + "/protocol/deleteFile.action",
						data : {
							attachId : file.id,
							attachName : file.name,
							attachDescription : file.description,
							attachAuthor : file.author,
							attachPubType : file.type,
							attachUrl : file.url,
							attachPubMedId : file.pubMedId,
							deleteType : "file"
						}
					}).done(function(data, textStatus, jqXHR) {
						// Display success message
						$.ibisMessaging("flash", "success", data);
						
						// Save the tree if needed
						if ( saveTreeStruct )
						{
							var tree = $("#eBinderTree").fileFolder("getTreeAsJSON", $("#eBinderTree"));
							saveTree(JSON.stringify(tree));
						}
					}).fail(function(jqXHR, textStatus, errorThrown) {
						alert(jqXHR.responseText + " <s:text name='ebinder.reload.notice'/>");
						redirectWithReferrer(webRoot + "/protocol/studyEbinder.action");
					});
				}
				
				// Delete any files from all sub-folders
				function deleteFilesInSubFolder(subFolders)
				{
					var files = null;
					
					// Delete any files in any sub-folders
					for ( var i = 0; i < subFolders.length; i++ )
					{
						deleteFilesInSubFolder(subFolders[i].folders.values());
						files = subFolders[i].files.values();
						
						// Delete any files in the current sub-folder
						for ( var j = 0; j < files.length; j++ )
						{
							deleteFile(files[j], false);
						}
					}
				}
				
				// Show a dialog box message asking the user to download a file
				function downloadFiles(fileArray)
				{
					if ( fileArray.length > 0 )
					{
						var file = fileArray.shift();
						
						if ( file.fileName !== "" )
						{
							// Create the dynamic download DIV HTML code
							var today = new Date();
							htmlOut = '<div id="fileDownload_' + today.getTime() + '" style="display: none;">' +
								'<form name="attachmentForm" id="downloadForm" method="post" action="' + webRoot + 
								'/attachments/download.action" enctype="multipart/form-data" onsubmit="$(\'#fileDownload_' + today.getTime() + '\').dialog(\'close\');">' +
								'<input type="hidden" name="id" value="' + file.id + '"/>' +
								'<input type="hidden" name="associatedId" value="' + $("#studyId").val() + '"/>' +
								'<input type="hidden" name="typeId" value="<%= Integer.toString(AttachmentManager.FILE_STUDY_EBINDER) %>"/>' +
								'<p>' + imr.messages.getDownloadConfirmMsg(file.name) + '</p><br/>' +
								'<div class="fileFolder-buttonContainer">' +
								'<input type="submit" value="<s:text name="button.yes"/>" title="' + "<s:text name='tooltip.download'/>" + '"/>' +
								'<input type="button" value="<s:text name="button.no"/>" title="' + "<s:text name='tooltip.cancel'/>" + '" ' +
								'onclick="$(\'#fileDownload_' + today.getTime() + '\').dialog(\'close\').dialog(\'destroy\').remove();"/>' +
								'</div></form></div>';
								
							// Add the download DIV to the DOM and open the JQuery Dialog widget
							$("body").append(htmlOut);
							$("#fileDownload_" + today.getTime()).dialog({
								title : "<s:text name='app.window.download.confirm.title'/>",
								close : function(event, ui) {
									downloadFiles(fileArray);
								}
							});
						}
						else
						{
							$.ibisMessaging("dialog", "error", imr.messages.getFileNoDataErrMsg(file.name), {
								title : "<s:text name='app.window.error.title'/>"
							});
							downloadFiles(fileArray);
						}
					}
				}
				
				$(document).ready(function()
				{
					$("#eBinderTree").fileFolder({
						tree : $.parseJSON($("#jsonTree").val()),
						newFolderLabel : "<s:text name='button.newFolder'/>",
						uploadNewLabel : "<s:text name='button.newFile'/>",
						editLabel : "<s:text name='button.Edit'/>",
						deleteItemLabel : "<s:text name='button.Delete'/>",
						downloadFileLabel : "<s:text name='button.download'/>",
						allowClose : true,
						fileUpload : {
							targetUrl :	webRoot + "/protocol/uploadFile.action",
							fieldNames : {
								id :			"attachId",
								name :			"attachName",
								author : 		"attachAuthor",
								description :	"attachDescription",
								type :			"attachPubType",
								file :			"attachFile",
								url :			"attachUrl",
								pubMedId :		"attachPubMedId",
								fileName :		"attachFileFileName"
							}
						},
						onAddFolder : function(folderObj)
						{
							var tree = $("#eBinderTree").fileFolder("getTreeAsJSON", $("#eBinderTree"));
							saveTree(JSON.stringify(tree));
						},
						onEdit : function($originalElement, $element)
						{
							var tree = $("#eBinderTree").fileFolder("getTreeAsJSON", $("#eBinderTree"));
							saveTree(JSON.stringify(tree));
						},
						onMoveFinish : function($moved, $target)
						{
							var tree = $("#eBinderTree").fileFolder("getTreeAsJSON", $("#eBinderTree"));
							saveTree(JSON.stringify(tree));
						},
						onDeleteFolder : function(folderObj)
						{
							var tree = $("#eBinderTree").fileFolder("getTreeAsJSON", $("#eBinderTree"));
							var files = folderObj.files.values();
							
							// Delete files in the folder and any sub-folders
							deleteFilesInSubFolder(folderObj.folders.values());
							
							for ( var i = 0; i < files.length; i++ )
							{
								deleteFile(files[i], false);
							}
							
							$.ibisMessaging("flash", "success", imr.messages.getFolderDeleteSuccessMsg(folderObj.name));
							
							// Save the tree structure
							saveTree(JSON.stringify(tree));
						},
						onAddFile : function($file)
						{
							var tree = $("#eBinderTree").fileFolder("getTreeAsJSON", $("#eBinderTree"));
							saveTree(JSON.stringify(tree));
						},
						onDeleteFile : function(fileObj)
						{
							// Delete the file
							deleteFile(fileObj, true);
						},
						onDownload : function(files)
						{
							if ( files.length > 0 )
							{
								downloadFiles(files);
							}
							else
							{
								$.ibisMessaging("dialog", "warning", "<s:text name='ebinder.download.nofiles.avaliable'/>", {
									title : "<s:text name='app.window.warning.title'/>"
								});
							}
						}
					});
				});
			</script>
		</security:hasProtocolPrivilege>
		<script type="text/javascript">
			$(document).ready(function()
			{
				if ( !canEdit )
				{
					$("#eBinderTree").fileFolder({
						tree : $.parseJSON($("#jsonTree").val())
					});
					
					$("div.fileFolder-buttonBar").remove();
				}
			});
		</script>
		
		<s:form enctype="multipart/form-data" id="eBinderForm" theme="simple">
			<s:hidden name="id" id="binderId"/>
			<s:hidden name="eBinderTree" id="jsonTree"/>
			<s:hidden name="studyID" id="studyId"/>
			<s:hidden name="type" id="binderType"/>
			
			<div id="eBinderTree">
				
			</div>
		</s:form>
	</s:if>
	
	<%-- Include Footer --%>
	<jsp:include page="/common/footer_struts2.jsp" />
</html>