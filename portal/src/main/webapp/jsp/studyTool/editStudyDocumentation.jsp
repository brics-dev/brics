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
			&nbsp;&gt;&nbsp;Manage Documentation
		</div>

		<form id="theForm" action="/portal/study/studyDocumentationAction.action" method="post" enctype="multipart/form-data" class="validate">
			<s:token />
			<ndar:editStudyChevron action="studyAction" chevron="Manage Documentation" />

			<h2>Edit Study: ${sessionStudy.study.title}</h2>

			<!-- Supporting Documentation -->
			<h3>Supporting Documentation</h3>
			<p>Please provide any supporting documentation for this studying
				including methods, findings, relevant presentations, algorithms,
				etc. associated with the study. Note that analyzed images and
				genomics should be provided as a part of a data upload/submission
				process and should not be included here.</p>
		</form>
		
		<s:hidden id="validationActionName" name="validationActionName" />
		<div id="documentationDetailsDialog" style="display:none"></div>
		<!-- start edit study documentation -->
		
		<div id="documentTableAndButtons">
		<%@include file="/common/taglibs.jsp"%>
		<div class="ui-clearfix">
			<s:if test="%{sessionObject.supportingDocumentationSet.size() < documentationLimit || documentationLimit < 0}">
				<div id="addDocBtnDiv" class="button button-right btn-downarrow">
						<input id="addDocBtn" type="button" value="Add Documentation" />
						<div id="selectAddDocDiv" class="btn-downarrow-options">
							<p>
								<a class="lightbox"
									href="${actionName}!addDocDialog.ajax?addDocSelect=url">URL</a>
								<a class="lightbox"
									href="${actionName}!addDocDialog.ajax?addDocSelect=file">File</a>
							</p>
						</div>
				</div>
			</s:if>
			<s:else> 
				<div id="addDocBtnDiv" class="button button-right disabled btn-downarrow " title="The maximum amount of documents allowed to be uploaded is ${documentationLimit}.">
						<input id="addDocBtn" disabled="true" type="button" value="Add Documentation"/>
						<div id="selectAddDocDiv" class="hidden btn-downarrow-options">
							<p>
								<a class="lightbox"
									href="${actionName}!addDocDialog.ajax?addDocSelect=url">URL</a>
								<a class="lightbox"
									href="${actionName}!addDocDialog.ajax?addDocSelect=file">File</a>
							</p>
						</div>
				</div>
			</s:else>
			
			
			
			
			
			
			
			
			
			
			
			
			
			
			
			
			
			
			
			
			
			
			
			
			
			
			
			
			
			
			
			
			
			
			
			
			
			
			
			
			
			
			
			
			
			
			
			
			
			
			
			
			
			
			
			
			
			
			
			
			
			
			
			
			
			
			
			
		</div>
		<br />
		
		<s:hidden id="documentationLimit" name="documentationLimit" />
		<s:hidden id="actionName" name="actionName" />
		
		<div id="documentationDetailsDialog"  style="display:none"></div>
		<div id="documentationTable" class="idtTableContainer">
		  	<div id="dialog" class="ibisMessaging-dialogContainer"></div>
			<table id="documentationTableTable" class="table table-striped table-bordered" width="100%"></table>
		</div>
		<script type="text/javascript">
			$(document).ready(function() {
				var actionName = $("#actionName").val();
				$('#documentationTableTable').idtTable({
					idtUrl: "<s:url value='"+actionName+"!getUploadDocumentation.action' />",
					pageLength: 15,
					dom: 'Bfrtip',
					idtData: {
						primaryKey: 'name'
					},
					"columns": [
			              {
			                  "data": 'title',
			                  "title":'TITLE',
			                  "name":'TITLE',
			                  "parameter" : 'title',
			                  "searchable": true,
			                  "orderable": true
			              },
			              {
			                  "data": 'docNameLink',
			                  "title":'DOCUMENTATION',
			                  "name":'DOCUMENTATION',
			                  "parameter" : 'docNameLink',
			                  "searchable": true,
			                  "orderable": true
			              },
			              {
			                  "data": 'description',
			                  "title": 'DESCRIPTION',
			                  "name": 'DESCRIPTION',
			                  "parameter" : 'description',
			                  "searchable": true,
			                  "orderable": true,
			                  "render": IdtActions.ellipsis(35)
			              },
			              {
			                  "data": 'dateCreated',
			                  "title": 'DATE UPLOADED',
			                  "name": 'DATE UPLOADED',
			                  "parameter" : 'dateCreated',
			                  "searchable": true,
			                  "orderable": true,
			                  "render": IdtActions.formatDate()
			              }	           
			           ],
			           select: 'multi',
			           buttons: [
			        	  {
			        	   	 extend: 'delete',
			        	   	 className: 'idt-DeleteButton',
		        	    	 action: function(e, dt, node, config) {
			        	   		var actionName = $("#actionName").val();
			        	   		var msgText = "Are you sure you want to delete the item(s)?";
			        	   		var yesBtnText = "Delete";
			        	   		var noBtnText = "Do Not Delete";
			        			var rows = dt.rows('.selected').data();
			        	        var rowsIDs = [];
			        	        for (var i = 0; i < rows.length; i++) {
			        	            rowsIDs.push(rows[i].DT_RowId)
			        	        }
			        	   		var action = actionName + "!removeDocumentations.action?supportingDocName="
			        	   				+ rowsIDs;
		
			        	   		EditConfirmationDialog("warning", msgText, yesBtnText, noBtnText, action,
			        	   				true, "400px", "Confirm Deletion", rowsIDs);
			        	   	} 
			        	  },
			        	  {
			        		 text: 'Edit',
			        		 className: 'idt-EditButton',
			        		 enabled: false,
			        		 enableControl: {
		                         count: 1,
		                         invert: true
		                     },
			        		 action: function(e, dt, node, config) {
			        			var actionName = $("#actionName").val();
			        			var rows = dt.rows('.selected').data();
			        	        var rowsIDs = [];
			        	        for (var i = 0; i < rows.length; i++) {
			        	            rowsIDs.push(rows[i].DT_RowId)
			        	        }
			        			 $.ajax({
			        					type : "POST",
			        					cache : false,
			        					url : actionName + "!editDocumentation.ajax?supportingDocName=" + encodeURIComponent(rowsIDs),
			        					success : function(data) {
			        						$.fancybox(data);
			        					}
			        			});
			        		 }
			        	  }
			           ]
				})
			})
		</script>
		<br />
		<script type="text/javascript" src='<s:url value="/js/uploadDocumentations.js"/>'></script>
		<script type="text/javascript">
		
		
			$("#addDocBtn").click( function(e) {
				$("#selectAddDocDiv").toggle();
			});
				
		</script>
		</div>
		
		
		<!--  end edit study documentation -->
		
	
		<div id="documentationDialog" style="display:none"></div>

		<div class="clear-both">
			<br />
			<h3>Administrative Files</h3>
			<p>Listed below are the administrative files that have been uploaded for your study.</p>
			<table class="display-data full-width">
				<thead>
					<tr>
						<th>Name</th>
						<th>Type</th>
						<th>Date Uploaded</th>
					</tr>
				</thead>
				<tbody>
					<tr>
						<td><a
							href="fileDownloadAction!download.action?fileId=${sessionStudy.study.dataSubmissionDocument.id}">${sessionStudy.study.dataSubmissionDocument.name}</a>
						</td>
						<td>Data Submission Agreement</td>
						<td>${sessionStudy.study.dataSubmissionDocument.isoUploadDateString}</td>
					</tr>
				</tbody>
			</table>
			<s:if test="%{inAdmin}">
				<s:form id="adminFileForm" cssClass="validate" method="post" validate="true" enctype="multipart/form-data">
					<s:token />
					<p>The administrator has the ability replace the current Administrative File. Once the file is uploaded 
						and the study is saved, the file will appear in the table above and replace the current file.</p>
					<div class="form-field">
						<label for="adminUpload" class="required">Data Submission Document <span class="required">* </span>:
						</label>
						<s:file id="adminUpload" name="adminUpload" cssClass="textfield float-left" />
						
						<div class="button">
							<input type="button" onclick="javascript:confirmAdminFileChange()" value="Upload" style="float:left"/>
						</div>
					</div>
				</s:form>
			</s:if>
			<s:else>
				<p>The administrative file was submitted when the study was created. If you wish to update this study's 
					administrative file, please contact the system administrator to replace the current file.</p>
				<p>Please include the name of the study and the administrative file in your request.</p>
			</s:else>

			<div class="form-field clear-left">
				<br />
				<div class="button">
					<input type="button" value="Continue"
						onClick="javascript:submitForm('studyAction!moveToDataset.action')" />
				</div>
				<a class="form-link" href="studyAction!submit.action">Save &amp; Finish</a> 
				<a class="form-link" href="viewStudyAction!view.action?studyId=${sessionStudy.study.prefixedId}">Cancel</a>
			</div>
		</div>


	</div>

	<s:form></s:form>
</div>
<div></div>

<div class="ibisMessaging-dialogContainer"></div>

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
	
	
	function removeDocumentation(documentationToRemove) {
		$.post(	"studyAction!removeDocumentation.ajax", 
			{ documentationToRemove:documentationToRemove }, 
			function (data) {
				DocumentationDialog.init(data);
			}
		);
	}
	
	function confirmAdminFileChange() {
		if (confirm("Updating this study's administrative file cannot be undone. Uploading this file will replace the " +
				"current file. Click OK to continue.")) {
			
			var $form = $("#adminFileForm");
			$form.attr("action", "studyAdminDocValidationAction!updateAdminFile.action");
			$form.submit();
		 } 
	}
		
	var DocumentationDialog = {
		isInit : false,
		$dialog : null,
		
		init : function(html) {
			var $container = $("#supportingDocumentationTable");
			var $dialog = $("#documentationDialog");
			this.$dialog = $dialog;
			this.initDom($container, html);
			this.initDialog($dialog);
		},
		
		initDom : function($container, html) {
			this.destroy();
			
			$container.html(html);
			var $table = $("#documentationTable table");
			$("#addDocBtn").on("click", function(e) {
				$("#selectAddDocDiv").toggle();
			});
			
			IDT.fullBuild($table);
		},
		
		initDialog : function($element) {
			$element.dialog({
				autoOpen : false,
				minWidth: 750,
				width: 750,
				open : function() {
					
				},
				close : function() {
					DocumentationDialog.reset();
				},
				buttons : [
					{
						text: "Save",
						click : function() {
							//alert("TODO: save");
							submitDocumentation();
						}
					},
					{
						text: "Cancel",
						click : function() {
							$(this).dialog("close");
						}
					}
				]
			});
			
			$("#supportingDocType").on("change", function(e) {
				if ($("#addDocSelect").val() == "file") {
					var val = $(this).find(":selected").val();
					if (val == "9") {
						DocumentationDialog.switchInterface("software");
					}
					else if (val == "8") {
						DocumentationDialog.switchInterface("publication");
					}
					else {
						DocumentationDialog.switchInterface("file");
					}
				}
			});
		},
		
		open : function(type) {
			this.reset();
			this.$dialog.dialog("open");
			
			if (type == "url") {
				$("#documentationUrl").show();
				$("#documentationFile").hide();
				$("#documentationDescription").show();
			}
			else {
				// type == file
				$("#documentationUrl").hide();
				$("#documentationFile").show();
				$("#documentationFileGeneral").show();
				$("#documentationSoftware").hide();
				$("#documentationPublication").hide();
				$("#documentationDescription").show();
			}
		},
		
		close : function() {
			this.$dialog.dialog("close");
		},
		
		reset: function() {
			$("#documentationUrl").hide();
			$("#documentationFile").show();
			$("#documentationFileGeneral").show();
			$("#documentationSoftware").hide();
			$("#documentationPublication").hide();
			this.$dialog.find("input, textarea").val("");
			$("#supportingDocType option:eq(0)").prop('selected', true);
		},
		
		switchInterface : function(interfaceToShow) {
			switch(interfaceToShow) {
				case "file":
					$("#documentationFileGeneral").show();
					$("#documentationSoftware").hide();
					$("#documentationPublication").hide();
					break;
				case "software":
					$("#documentationFileGeneral").hide();
					$("#documentationSoftware").show();
					$("#documentationPublication").hide();
					break;
				case "publication":
					$("#documentationFileGeneral").hide();
					$("#documentationSoftware").hide();
					$("#documentationPublication").show();
					break;
			}
			this.reCenter();
		},
		
		reCenter : function() {
			this.$dialog.dialog({
			    position: { 'my': 'center', 'at': 'center' }
			});
		},
		
		destroyTable : function() {
			IDT.derenderTable($("#supportingDocumentationTable table"));
		},
		
		destroy : function() {
			this.$dialog.dialog("destroy");
			$("#addDocBtn").off("click");
		}
	};
	
	function showSoftwareInfo(supportingDocId) {
		// TODO: ajax request details about this supporting doc & display details
		//$.ajax({
			
		//});
	}
	
	function showPublicationInfo(supportingDocId) {
		// TODO: ajax request details about this supporting doc & display details
	}
	
	$('document').on("click", "#pubMedWS", function(event) {
		//alert("https://www.ncbi.nlm.nih.gov/pmc/utils/oa/oa.fcgi?id=PMC13901");
		if ($("#pubMedId").val() != "") {
		
		    $.ajax({
		        type: "get",
		        url: "studyDocumentationAction!getPubMedInfo.ajax",
		        data : {
		              "pubMedId" : $("#pubMedId").val()
		        },
		        success: function(response) {
		        	var obj = JSON.parse(response, function (key, value) {
		        		//alert("key"+key+"val"+value);
		        		if(key == "title"){
		        	    	$("#pubEntryTitle").val(value);
		        	  	}
		        		
		        		if(key=="publicationDate"){
		        			$("#pubEntryDate").val(value);
		        		}
		        		
		        		if(key=="faFirstName"){
		        			$("#pubEntryFirstAuthorFirstName").val(value);
		        		}
		        		
						if(key=="faMiddleName"){
							$("#pubEntryFirstAuthorMiddleName").val(value);
					       }
						
						if(key=="faLastName"){
							$("#pubEntryFirstAuthorLastName").val(value);
						}
						
						if(key=="laFirstName"){
		        			$("#pubEntryLastAuthorFirstName").val(value);
		        		}
		        		
						if(key=="laMiddleName"){
							$("#pubEntryLastAuthorMiddleName").val(value);
					       }
						
						if(key=="laLastName"){
							$("#pubEntryLastAuthorLastName").val(value);
						}
		  
		        	    if (key == "error") {
		        	    	 $.ibisMessaging("close", {type:"primary"}); 
		        	    	$.ibisMessaging("primary", "error", value,{container: ".ibisMessaging-dialogContainer"});
		        	    }
		        	    
		        	    if(key == "format"){
		        	    	 $.ibisMessaging("close", {type:"primary"}); 
		        	    	$.ibisMessaging("primary", "error", value,{container: ".ibisMessaging-dialogContainer"});
		        	    }
		        	 
		        	   });
		                    
		        },
		        error: function(e) {
		              //most likely empty list
		        }
			 });
		}
		else {
			$.ibisMessaging("dialog", "error", "You must enter a PubMed ID to auto-fill the form");
		}
	});
	
    function testUrl() {
    	var url = $("#url").val();
    	
    	if (!url || url.trim().length == 0) {
    		alert("Warning: URL field is empty!");
    		return;
    	}
    	
    	if (!url.match(/^(f|ht)tps?:\/\//i)) {
            url = 'http://' + url;
        }
    	
    	window.open(url, "_blank");
    	window.focus();
    }
    
    function ellipsisExpandCollapse(element) {
    	var $this = $(element);
    	$this.parent().toggle();
    	if ($this.text() == "...") {
    		$this.parent().next().toggle();
    	}
    	else {
    		$this.parent().prev().toggle();
    	}
    }
    
    function submitDocumentation() {
    	var data = new FormData();
    	
    	var url = $("#url").val();
    	
    	var softwareName = $("#supportingDocSoftwareName").val();
    	var softwareVersion = $("#supportingDocVersion").val();
    	
    	var docType = Number($("#supportingDocType option:selected").val());
    	var description = $("#supportingDocDescription").val();
    	
    	var pubMedId = $("#pubMedId").val();
    	var title = $("#pubEntryTitle").val();
    	var pubDate = $("#pubEntryDate").val();
    	var pubAbstract = $("#pubEntryAbstract").val();
    	
    	var firstAuthorFirstName = $("#pubEntryFirstAuthorFirstName").val();
    	var firstAuthorMiddleName = $("#pubEntryFirstAuthorMiddleName").val();
    	var firstAuthorLastName = $("#pubEntryFirstAuthorLastName").val();
    	var firstAuthorEmail = $("#pubEntryFirstAuthorEmail").val();
    	var firstAuthorOrg = $("#pubEntryFirstAuthorOrg").val();
    	
    	var lastAuthorFirstName = $("#pubEntryLastAuthorFirstName").val();
    	var lastAuthorMiddleName = $("#pubEntryLastAuthorMiddleName").val();
    	var lastAuthorLastName = $("#pubEntryLastAuthorLastName").val();
    	var lastAuthorEmail = $("#pubEntryFirstAuthorEmail").val();
    	var lastAuthorOrg = $("#pubEntryLastAuthorOrg").val();

    	var ajaxUrl = "/portal/study/studyDocValidationAction!upload.ajax";
    	
    	// the stuff that's everywhere
    	data.append("supportingDocType", docType);
    	
		if (url != "") {
			// url submit
			data.append("url", url);
			data.append("supportingDocDescription", description);
		}
		else {
			var fileName = document.addDocForm.upload.files[0].name;
			data.append("uploadFileName", fileName);
			data.append('upload', document.addDocForm.upload.files[0]);
			data.append("supportingDocType", docType);

			// file submit
			if (docType == "9") {
				// software
				data.append("softwareName", softwareName);
				data.append("softwareVersion", softwareVersion);
			}
			else if (docType == "8") {
				// publication
				//data.append("pubMedId", pubMedId);
				data.append("pubEntry.title", title);
				data.append("pubEntry.publicationDate", pubDate);
		    	data.append("pubEntry.description", pubAbstract);
		    	data.append("pubEntry.firstAuthor.firstName", firstAuthorFirstName);
		    	data.append("pubEntry.firstAuthor.mi", firstAuthorMiddleName);
		    	data.append("pubEntry.firstAuthor.lastName", firstAuthorLastName);
		    	data.append("pubEntry.firstAuthor.email", firstAuthorEmail);
		    	data.append("pubEntry.firstAuthor.orgName", firstAuthorOrg);
		    	data.append("pubEntry.lastAuthor.firstName", lastAuthorFirstName);
		    	data.append("pubEntry.lastAuthor.mi", lastAuthorMiddleName);
		    	data.append("pubEntry.lastAuthor.lastName", lastAuthorLastName);
		    	data.append("pubEntry.lastAuthor.email", lastAuthorEmail);
		    	data.append("pubEntry.lastAuthor.orgName", lastAuthorOrg);
			}
			else {
				// all other file types
				data.append("supportingDocDescription", description);
			}
		}
		
		$.ajax({
			url : ajaxUrl,
			data: data,
			type: "post",
			cache : false,
			processData : false,
			contentType : false,
			success : function(returnData) {
				DocumentationDialog.close();
				refreshDocuementationTable();
			}
		});
    }
    
    function removeDocumentation() {
    	var $table = $("#documentationTable table");
    	var selectedOptions = IDT.getSelectedOptions($table);
    	var selectedString = selectedOptions.join(",");
    	
    	var data = {
    		"documentationToRemove" : selectedString
    	};
    	
    	$.ajax({
			url : ajaxUrl,
			data: data,
			type: "post",
			cache : false,
			processData : false,
			contentType : false,
			success : function(returnData) {
				refreshDocuementationTable();
			}
    	});
    }
    
    function refreshDocuementationTable() {
    	DocumentationDialog.destroyTable();
    	tableInit();
    }

</script>