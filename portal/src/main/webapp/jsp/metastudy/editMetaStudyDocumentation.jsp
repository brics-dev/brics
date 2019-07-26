<%@include file="/common/taglibs.jsp"%>
<title>Create Meta Study</title>

<!-- begin .border-wrapper -->
<div class="border-wrapper wide">
	<jsp:include page="../navigation/metaStudyNavigation.jsp" />
	<h1>Meta Study</h1>
	<div style="clear:both"></div>
	
	<!--begin #center-content -->
	
	<div id="main-content">
		<h2>Create Meta Study</h2>
		<s:form id="theForm" cssClass="validate" validate="true" enctype="multipart/form-data">
			<s:token />
			<ndar:editMetaStudyChevron action="metaStudyAction" chevron="Documentation" />
			
			<h3>Documentation</h3>
				<!-- edit meta study documentation -->
				
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
						              } ,
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
				
				
				<!-- edit meta study documentation end -->
				
			<br/>
			
			<div class="form-field" style="display: inline-block;">
				<div class="button">
					<input type="button" value="Back" onClick="javascript:submitForm('metaStudyAction!moveToDetails.action')" />
				</div>
			</div>
			
			<div class="form-field inline-right-button">
				<div class="button btn-primary" style="margin-right: 10px;">
					<input type="button" value="Create & Finish" onclick="javascript:createMetaStudy()" />
				</div>
				<div class="button" style="margin-right: 5px;">
					<input type="button" value="Next" onclick="javascript:submitForm('metaStudyAction!moveToData.action')" />
				</div>
				<a class="form-link" href="javascript:void(0)" onclick="javascript:cancelCreation()">Cancel</a>
			</div>
			
			<div class="ibisMessaging-dialogContainer"></div>
		</s:form>

    </div>
</div>
<script type="text/javascript" src="/portal/js/metastudy/metaStudy.js"></script>

<script type="text/javascript">

	setNavigation({"bodyClass":"primary", "navigationLinkID":"metaStudyModuleLink", 
			"subnavigationLinkID":"metaStudyLink", "tertiaryLinkID":"createMetaStudyLink"});
</script>