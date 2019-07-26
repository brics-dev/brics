<%@include file="/common/taglibs.jsp"%>
<title>Study: ${currentStudy.title}</title>

<!-- begin .border-wrapper -->
<div class="border-wrapper wide">
	<jsp:include page="../navigation/dataRepositoryNavigation.jsp" />
	<h1 class="float-left">Data Repository</h1>
	<div style="clear:both"></div>
	
	<!--begin #center-content -->
	<div id="main-content">
		<div id="breadcrumb">
			<s:if test="inAdmin">
				<a href="studyAction!list.action">Manage Studies</a>&nbsp;>&nbsp;
				<s:if test="isStudyRequest">Study Request</s:if>
				<s:else>
					<s:property value="currentStudy.title" />
				</s:else>
			</s:if>
			<s:else>
				<a href="studyAction!list.action">View Studies</a>&nbsp;>&nbsp;
				${sessionStudy.study.title}
			</s:else>
		</div>
		<h2>Study: ${currentStudy.title}</h2>

		<div class="clear-both clear-float">
			<s:form id="theForm" cssClass="validate" method="post" validate="true" enctype="multipart/form-data">
				<s:token />
				<s:hidden id="actionName" name="documentsActionName" />
				<s:if test="inAdmin && (currentStudy.isPublic || currentStudy.isPrivate)">
					<h3 id="adminFunLabel" class="clear-both collapsable">
						<span id="adminFunPlusMinus"></span>&nbsp;Administrative Functionality
					</h3>
					<div id="adminFun">
						<p>You may change the visibility of the study below. Please note that marking a study as 
						   private should be the exception to the rule.</p>

						<ul class="checkboxgroup-vertical">
							<p>Visibility:</p>
							<li>
								<label for="public">Public :</label> 
								<s:if test="%{sessionStudy.study.isPublic}">
									<input checked id="public" type="radio" name="visibility" />
								</s:if> 
								<s:else>
									<input id="public" type="radio" name="visibility" />
								</s:else>
							</li>
							<li>
								<label for="private">Private :</label> 
								<s:if test="%{sessionStudy.study.isPrivate}">
									<input checked id="private" type="radio" name="visibility" />
								</s:if> 
								<s:else>
									<input id="private" type="radio" name="visibility" />
								</s:else>
							</li>
						</ul>
						<s:if test="%{getCurrentStudy().getDatasetCount() == 0}">
							<div id="deleteButton">
								<p>You may delete the study by selecting the button below. You may only a delete a 
								   study if it does not have any uploaded data submissions associated with it. 
								   Deleting a study will permanently remove it from the system.</p>
								<div class="button">
									<input type="button" onclick="javascript:confirmDelete()" value="Delete" />
								</div>
							</div>
						</s:if>
						<s:else>
							<p>You cannot delete this study because there are datasets associated with it. 
							   Please delete all associated datasets before deleting this study.</p>
						</s:else>
						<s:if test="%{canShowCreateDoiUi()}">
							<div class="button">
								<input type="button" id="assignDoiBtn" value="Assign DOI" title="Click to assign DOI to this study."/>
							</div>
						</s:if>
						<br />
						<br />
					</div>
					<br />
				</s:if>

				<s:if test="inAdmin && isStudyRequest">
					<h3>Study Request:</h3>
					<p>The user has requested the creation of the following study. Please review the study details as well as 
					   the uploaded documentation.</p>
					<div class="form-output">
						<div class="label">Status:</div>
						<div class="readonly-text">
							<s:property value="currentStudy.studyStatus.name" />
						</div>
					</div>
					<div class="form-field">
						<label for="reason" class="required">Approval/Rejection Reason <span class="required">* </span>:
						</label>
						<s:textarea label="reason" cols="60" rows="4" cssClass="textfield required" name="reason" escapeHtml="true" escapeJavaScript="true" />
						<s:fielderror fieldName="reason" />
						<div class="special-instruction">
							<p>An email will be sent to the user with the above message included in the body.</p>
						</div>
					</div>
					<div class="form-output">
						<div class="label">Date Requested:</div>
						<div class="readonly-text">
							<ndar:dateTag value="${currentStudy.dateCreated}" />
						</div>
					</div>

					<div class="form-output">
						<div class="label">Administrative Document:</div>
						<a
							href="fileDownloadAction!download.action?fileId=<s:property value="currentStudy.dataSubmissionDocument.id" />"><s:property
								value="currentStudy.dataSubmissionDocument.name" /></a>
					</div>

					<div class="form-field">
						<div class="button">
							<input type="button" value="Approve"
								onclick="javascript:submitForm('studyReasonValidationAction!approve.action');" />
						</div>
						<div class="button margin-left">
							<input type="button" value="Reject"
								onclick="javascript:submitForm('studyReasonValidationAction!deny.action');" />
						</div>
					</div>

					<br />
				</s:if>

				<h3>Study Overview:</h3>
				<c:if
					test="${currentStudy.isPrivate && !inAdmin && isAdmin && currentStudy.datasetCount == 0}">
					<div class="button float-right">
						<input type="button" onclick="javascript:confirmDelete()"
							value="Delete" />
					</div>
				</c:if>

				<s:if test="hasWriteAccess">
					<div class="button float-right">
						<input type="button"
							onclick="parent.location.href='studyAction!edit.action?studyId=${currentStudy.prefixedId}'"
							value="Edit" />
					</div>
				</s:if>

				<jsp:include page="viewEagerStudyInterface.jsp" />

				<!-- Supporting Documentation -->
				<c:if test="${not empty currentStudy.supportingDocumentationSet}">
					<div id="documentationDetailsDialog" style="display:none"></div>
					
					<br />
					<h3 id="supportingDocLabel" class="clear-both collapsable">
						<span id="supportingDocPlusMinus"></span>&nbsp;Supporting
						Documentation
					</h3>

					<div id="supportingDocumentation">
						<p>Listed below is pertinent study information including
							methods, findings, relevant presentations, algorithms, etc.
							associated with the study.</p>

						
						<div id="supportingDocTable" class="idtTableContainer">
						   	<div id="dialog"></div>
							<table id="supportingDocTableTable" class="table table-striped table-bordered" width="100%"></table>
						</div>
						<script type="text/javascript">
					$(document).ready(function() {
 						// TODO: INSERT CONFIGS HERE
		 					$('#supportingDocTableTable').idtTable({
								idtUrl: "<s:url value='/study/studyAction!getDocumentation.action' />",
								autoWidth: false,
						         "columns": [
						              {
						                  "data": 'title',
						                  "title":'TITLE',
						                  "name":'TITLE',
						                  "parameter" : 'title',
						                  "width": '25%',
						                  "searchable": true,
						                  "orderable": true,
						                  "render" : IdtActions
											.ellipsis(45)
						              },
						              {
						                  "data": 'docNameLink',
						                  "title":'DOCUMENTATION',
						                  "name":'DOCUMENTATION',
						                  "parameter" : 'docNameLink',
						                  "width": '17%',
						                  "searchable": true,
						                  "orderable": true
						              },
						              {
						                  "data": 'typeLink',
						                  "title": 'TYPE',
						                  "name": 'TYPE',
						                  "parameter" : 'typeLink',
						                  "width": '20%',
						                  "searchable": true,
						                  "orderable": true
						              },
						              {
						                  "data": 'description',
						                  "title": 'DESCRIPTION',
						                  "name": 'DESCRIPTION',
						                  "parameter" : 'description',
						                  "width": '23%',
						                  "searchable": true,
						                  "orderable": true,
						                  "render" : IdtActions
											.ellipsis(45)
						              },
						              {
						                  "data": 'dateCreated',
						                  "title": 'DATE UPLOADED',
						                  "name": 'DATE UPLOADED',
						                  "parameter" : 'dateCreated',
						                  "width": '15%',
						                  "searchable": true,
						                  "orderable": true,
						                  "render": IdtActions.formatDate()
						              }						            
						           ]
							});
						}); 
						</script>
						
						
						

					</div>
				</c:if>
				<!-- End Supporting Documentation -->

				<!-- Admin File -->
				<br />


<s:if test="inAdmin || hasOwnerAccess || hasAdminAccess">

				<h3 id="adminFileLabel" class="clear-both collapsable">
					<span id="adminFilePlusMinus"></span>&nbsp;Administrative Files
				</h3>
				<div id="adminFile">
					<p>Listed below are the administrative files that have been
						uploaded for your study.</p>


					<div id="adminFileTable" class="idtTableContainer">
					   	<div id="dialog"></div>
						<table id="adminFileTableTable" class="table table-striped table-bordered" width="100%"></table>
					</div>
					<script type="text/javascript">
					$(document).ready(function() {
						$('#adminFileTableTable').idtTable({
							'serverSide': false,
							'processing': false,
							autoWidth: false,
							'columns': [
								{
									"title":"NAME",
									"data": "name",
									"name":"name",
									"width": '25%',
									"visible":true
								
								},
								{
									"title":"TYPE",
									"data": "type",
									"name":"type",
									"width": '20%',
									"visible":true
								},
								{
									"title":"DESCRIPTION",
									"data": "description",
									"name":"description",
									"width": '37%',
								   "render" : IdtActions
									.ellipsis(85),
									"visible":true
								},
								{
									"title":"DATE UPLOADED",
									"data": "dateUploaded",
									"name":"dateUploaded",
									"width": '15%',
									"visible":true,
									"render": IdtActions.formatDate()
								}
							],
							"data":
								[{
									"name": "<a class=tdLink href=\"fileDownloadAction!download.action?fileId=${currentStudy.dataSubmissionDocument.id}\">${currentStudy.dataSubmissionDocument.name}</a>",
									"type": "Data Submission Document",
									"description": "Data Submission Document",
									"dateUploaded": "${currentStudy.dataSubmissionDocument.isoUploadDateString}"
								}]
								
						});
					});
					</script>
				</div>
	</s:if> 
				<!-- Dataset Include -->
				<s:if test="%{getCurrentStudy().getDatasetCount() >= 0}">
					<br />
					<h3 id="datasetLabel" class="clear-both collapsable">
						<span id="datasetPlusMinus"></span>&nbsp;Dataset Submissions
					</h3>

					<div id="dataset" class="idtTableContainer">
						<table id="datasetTable" class="table table-striped table-bordered" width="100%"></table>
					</div>
					<script type="text/javascript">
						$(document).ready(function() {
							$('#datasetTable').idtTable({
								idtUrl: "<s:url value='/study/studyDatasetAction!datasetDatatable.ajax' />",
								serverSide: true,
								processing: true,
								pages: 3,
								autoWidth: false,
								filters: [
									{
										columnIndex: 3,
										type: 'select',
										name: 'filterStatus',
										label: 'Show all',
										defaultValue: 'hide',
										options: [
											{
												value: "hide",
												label: "Show shared & private"
											}
										]
									}
								],
								order: [[ 2, "desc" ]],
								columns: [
									{
										title:"DATASET ID",
										data: "prefixId",
										name:"prefixId",
										parameter: "prefixedId",
										width: "16%"
									},
									{
										title:"NAME",
										data: "name",
										name:"name",
										parameter: "nameLink",
										width: "35%"
									},
									{
										title:"SUBMISSION DATE",
										data: "submitDate",
										name:"submitDate",
										parameter: "submitDate",
										width: "12%",
										render: IdtActions.formatDate()
									},
									{
										title:"TYPE",
										data: "type",
										name:"type",
										parameter: "fileTypeString",
										width: "12%"
									},
									{
										title:"STATUS",
										data: "status",
										name:"status",
										parameter: "datasetStatus",
										width: "12%"
									},
									{
										title:"# OF REC",
										data: "recordCount",
										name:"recordCount",
										parameter: "numberOfRecords",
										width: "12%"
									}
								]
							});
						});
					</script>
				</s:if>

				<!-- Access Record Include -->
				<s:if test="isPortalAdmin || hasAdminAccess">
					<br />
					<h3 id="accessRecordLabel" class="clear-both collapsable">
						<span id="accessRecordPlusMinus"></span>&nbsp;Data Access Report
					</h3>
					<div id="accessRecord">
						<jsp:include page="accessReportFrame.jsp" />
					</div>
				</s:if>

				<!-- Form Structures Include -->
				<c:if test="${not empty currentStudy.studyForms}">
					<br />
					<div id="formStructureDialog" class="iframeDialog">
						<i id="formStructureDialogSpinner" class="loadingSpinner fa fa-spinner fa-pulse fa-5x fa-fw"></i>
						<iframe></iframe>
					</div>
					<h3 id="formStructuresLabel" class="clear-both collapsable">
						<span id="formStructuresPlusMinus"></span>&nbsp;Study Form Structures
					</h3>
					<div id="formStructures" class="">					
						<div id="formStructuresTable" class="idtTableContainer">
							<div id="dialog"></div>
							<table id="formStructuresTableTable" class="table table-striped table-bordered" width="100%"></table>
						</div>	
						<script type="text/javascript">
							$(document).ready(function() {
								$('#formStructuresTableTable').idtTable({
									idtUrl: "<s:url value='/study/studyAction!getFormStructureList.action' />",
									"columns": [
										{
											"data": "titleLink",
											"title": "FORM STRUCTURE TITLE",
											"name": "FORM STRUCTURE TITLE",
											"parameter": "title",
											"render": function(data, type, row, full) {
												var oTableId = full.settings.nTable.id;
						                        return "<a class=tdLink href=\"javascript:viewFsDetails('" + row.shortName + "')\">" + data + "</a>";
											}
										},
										{
											"data": "shortName",
											"title": "SHORT NAME",
											"name": "SHORT NAME",
											"parameter": "shortName"
										}, 
										{
											"data": "submissionType",
											"title": "FORM TYPE",
											"name": "FORM TYPE",
											"parameter": "submissionType.type"
										}
									]
								})				
							})
						</script>
						
					</div>
				</c:if>
				
				<div class="button">
					<s:if test="source == 'datasetList'">
						<input type="button"
							onclick="javascript:window.location.href='datasetAction!list.action'"
							value="Close" />
					</s:if>
					<s:else>
						<input type="button"
							onclick="javascript:window.location.href='studyAction!list.action'"
							value="Close" />
					</s:else>
				</div>

			</s:form>
		</div>
	</div>
</div>

<script type="text/javascript" src="/portal/js/uploadDocumentations.js"></script>
<script type="text/javascript">
<s:if test="!inAdmin" >
	setNavigation({"bodyClass":"primary", "navigationLinkID":"dataRepositoryModuleLink", "subnavigationLinkID":"studyToolLink", "tertiaryLinkID":"browseStudyLink"});
</s:if>
<s:else>
	setNavigation({"bodyClass":"primary", "navigationLinkID":"dataRepositoryModuleLink", "subnavigationLinkID":"contributeDataToolsLink", "tertiaryLinkID":"studyList"});
</s:else>

/*
 * Have to include filters for 
 * Dataset:
 * 		Hide Failed, Archived, and uploading Datasets
 * 		Show Failed, Archived, and uploading Datasets
 *
 * Data Access Report:
 *	 Time: All
 *	 Time: Last Week
 *	 Time: Last Month
 *
 * AND have to decide if we're looking at the right table
 */
 
 var dataTableFilters = {
	filterFailedDatasets: "hide", // or "show": hide = hide failed, show = show all
	timeFilter: "all" // all || lastWeek || lastMonth || lastYear
 };
$.fn.dataTableExt.afnFiltering.push(function(oSettings, aData, iDataIndex) {
	// true = show, false = hide
	if (oSettings.oInstance[0].parentNode.parentNode.id == "datasetTable") {
		if (oSettings.aoColumns[0].sTitle == "DATASET ID") {
			if (dataTableFilters.filterFailedDatasets == "show") {
				return true;
			}
			else {
				var status = aData[4];
				// if status is private or share, show it
				return status == "Private" || status == "Shared";
			}
		}
		else {
			
		}
	}
	return true;
});

var weekInMilliseconds = 604800000;
// can't do month because month has different numbers of days.  Date is faster than implementing myself
$.fn.dataTableExt.afnFiltering.push(function(oSettings, aData, iDataIndex) {
	if (oSettings.oInstance[0].parentNode.parentNode.id == "accessReports") {
		if (dataTableFilters.timeFilter != "all") {
			var tableDate = aData[4];
			var dateTimestamp = new Date(tableDate).getTime();
			if (dataTableFilters.timeFilter == "lastWeek") {
				var lastWeekTimestamp = new Date().getTime() - weekInMilliseconds;
				return dateTimestamp > lastWeekTimestamp;
			}
			else if (dataTableFilters.timeFilter == "lastMonth") {
				var lastMonth = new Date();
				// the below is already a timestamp
				var lastMonthTimestamp = lastMonth.setMonth(lastMonth.getMonth() - 1);
				return dateTimestamp > lastMonthTimestamp;
			}
			else if (dataTableFilters.timeFilter == "lastYear") {
				var lastYear = new Date();
				// the below is already a timestamp
				var lastYearTimestamp = lastYear.setYear(lastYear.getYear() - 1);
				return dateTimestamp > lastYearTimestamp;
			}
		}
	}
	return true;
});

	$('document').ready(function() { 
		$("#supportingDocumentation").hide();
		$("#supportingDocPlusMinus").text("+");
		$("#dataset").hide();
		$("#errorDatasetCheckbox").hide();
		$("#datasetPlusMinus").text("+");
		$("#adminFile").hide();
		$("#adminFilePlusMinus").text("+");
		$("#accessRecord").hide();
		$("#accessRecordPlusMinus").text("+");
		$("#formStructures").hide();
		$("#formStructuresPlusMinus").text("+");
		
		supportingDocumentationInit();
		adminFileInit();
		datasetInit();
		accessRecordInit();
		formStructuresInit();
		
		$("#adminFun").hide();
		$("#adminFunPlusMinus").text("+");
		visibilityRadioInit();
		adminFunInit();
		
		EventBus.on("init:table", function(model) {
			model.get("$el").parents(".dataTableContainer").on("change", ".fg-toolbar select", function() {
				var $this = $(this);
				var $table = $this.parents(".dataTableContainer").find("table");
				var arrChangeVal = $this.val().split(":");
				dataTableFilters[arrChangeVal[0]] = arrChangeVal[1];
				var model = IDT.getTableModel($table);
				model.get("datatable").fnDraw();
			});
		});
	});
/*	
	$('document').ready(function() {
		init("studyDatasetAction!includeErroredDatasets.ajax");
	}); 

	
	function init(action) {
		$.post(action, 
			{}, 
			function (data) {
				$("#dataset").html(data);
				buildDataTables();
			}
		);
	}
	 
	$('#errorDatasetCheckbox :checkbox').change(function() {
		if (document.getElementById("check1").checked ==true) { 
			// the checkbox was checked 
			var action ="studyDatasetAction!includeErroredDatasets.ajax";
			init(action);
		} else if(document.getElementById("check1").checked ==false){
			var action = "studyDatasetAction!filterErroredDatasets.ajax";
			init(action);
		} 
	});
*/	 
	var showLightBox = true;

	function viewDataset(prefixedId, isAdmin) {
		/* $.fancybox.showActivity(); */
		//$.fancybox.showActivity();
		console.log(' viewDataset' + showLightBox);
		if(showLightBox){
			showLightBox = false;
			var action = "/portal/study/datasetAction!viewLightbox.ajax";
			if(isAdmin == "true"){
				action = "/portal/studyAdmin/datasetAction!viewLightbox.ajax";
			}
			
			console.log("prefixedId: " + prefixedId);
			console.log($.fancybox);
			
			$.post(action, 
				{prefixedId:prefixedId}, 
				function (data) {
					$.bricsDialog(data);
					//$("#fancybox-wrap").unbind('mousewheel.fb');
					showLightBox = true;
				}
			);
			console.log(' viewDataset' + showLightBox);
		}	
	}
	 
	//Submit a form
	function submitForm(action)
	{
		var theForm = document.getElementById('theForm');
		theForm.action = action;
		theForm.submit();
	}
	
	function supportingDocumentationInit() {
		$("#supportingDocLabel").click(function(){
			$("#supportingDocumentation").slideToggle("fast");
			if($("#supportingDocPlusMinus").text()=="+") {
				$("#supportingDocPlusMinus").text("- ");
			} else {
				$("#supportingDocPlusMinus").text("+");
			}
		});
	}
	
	function adminFileInit() {
		$("#adminFileLabel").click(function(){
			$("#adminFile").slideToggle("fast");
			if($("#adminFilePlusMinus").text()=="+") {
				$("#adminFilePlusMinus").text("- ");
			} else {
				$("#adminFilePlusMinus").text("+");
			}
		});
	}
	
	function datasetInit() {
		$("#datasetLabel").click(function(){
			$("#dataset").toggle("blind",500);
			$("#errorDatasetCheckbox").toggle("blind",500);
			if($("#datasetPlusMinus").text()=="+") {
				$("#datasetPlusMinus").text("- ");
			} else {
				$("#datasetPlusMinus").text("+");
			}
		});
	}
	
	function adminFunInit() {
		$("#adminFunLabel").click(function(){
			$("#adminFun").slideToggle("fast");
			if($("#adminFunPlusMinus").text()=="+") {
				$("#adminFunPlusMinus").text("- ");
			} else {
				$("#adminFunPlusMinus").text("+");
			}
		});

	}
	
	function accessRecordInit() {
		$("#accessRecordLabel").click(function(){
			$("#accessRecord").slideToggle("fast");
			if($("#accessRecordPlusMinus").text()=="+") {
				$("#accessRecordPlusMinus").text("- ");
			} else {
				$("#accessRecordPlusMinus").text("+");
			}
		});
	}
	
	function formStructuresInit() {
		$("#formStructuresLabel").click(function(){
			$("#formStructures").slideToggle("fast");
			if($("#formStructuresPlusMinus").text()=="+") {
				$("#formStructuresPlusMinus").text("- ");
			} else {
				$("#formStructuresPlusMinus").text("+");
			}
		});
	}
	
	function confirmDelete() {
		var where_to = confirm("Are you sure you want to delete this study?  Once deleted, a study can not be recovered.");
		if (where_to == true) {
			window.location = "viewStudyAction!delete.action?studyId=${currentStudy.prefixedId}";
		}
	}
	
	function visibilityRadioInit() {
		$("#public").click(function() {
			changeVisibility("Public");
		});
		
		$("#private").click(function() {
			changeVisibility("Private");
		});
		<s:if test="currentStudy.studyStatus.id == 1">
			$("#deleteButton").show();
		</s:if>
		<s:else>
			$("#deleteButton").hide();
		</s:else>
	}
	
	function viewClinicalTrial(clinicalTrialId) {
		$.post(	"studyAction!viewClinicalTrial.ajax", 
			{ clinicalTrialId:clinicalTrialId }, 
			function (data) {
				$.fancybox(data);
			}
		);
	}
	
	
	var ddtUrl = "<s:property value="modulesDDTURL" />";
	$(document).ready(function() {
		// pre-tag a dictionary page to make sure CAS has loaded
		var url = ddtUrl + "dictionary/listDataStructureAction!list.action";
		$("#formStructureDialog").find("iframe").attr("src", url);
	});
	
	
	function viewFsDetails(shortName) {
		var url = ddtUrl + "dictionary/dataStructureAction!lightboxView.ajax?dataStructureName=" + shortName + "&queryArea=true&publicArea=true";
		var $container = $("#formStructureDialog");
		var $iframe = $container.find("iframe");
		$("#formStructureDialogSpinner").show();
		if ($iframe.attr("src") != "") {
			$iframe.attr("src", url);
			
			$iframe.on("load", function() {
				$("#formStructureDialogSpinner").hide();
			});
			
			$container.dialog({
				autoOpen: false,
				height: window.innerHeight - 25,
				width: window.innerWidth * 0.85,
				position: { my: "center", at: "center", of: window },
				close: function() {
					$(this).dialog("destroy");
				}
			});
		}
		else {
			$("#formStructureDialog iframe").attr("src", url);
		}
		$container.dialog("open");
	}
</script>

<%-- ##### Begin DOI Dialog UI ##### --%>
<s:if test="%{inAdmin && canShowCreateDoiUi()}">
	<script type="text/javascript" src="/portal/formbuilder/js/lib/underscore-min.js"></script>
	<jsp:include page="../common/createDoiDialogUI.jsp" />
	<script type="text/javascript">
		$("document").ready(function() {
			// Start listening for DOI events.
			EventBus.on("create:doi", function(data, $dialogDiv) {
				// Hide the "Assign DOI" button.
				$("#assignDoiBtn").hide();
				
				// Display the new DOI on the page.
				var link = '<a href="<s:property value="getDoiResolverUrl()" />' + data.doi + '">' +
						data.doi + '</a>';
				$("#studyDoiDisplay").html(link);
				$("#studyDoiSourceDisplay").text("DataCite");
			});
			
			EventBus.on("error:doi", function(cause, $dialogDiv){});
			
			// Initialize the create DOI dialog UI.
			initCreateDoiDialog("createStudyDoiAction.action", "study", true);
			
			// Bind click handler to the "Assign DOI" button on the main page.
			$("#assignDoiBtn").click(_.debounce(function() {
				$("#doiDialogUi").dialog("open");
			}, 1000, true));
		});
	</script>
</s:if>
<%-- ##### End DOI Dialog UI ##### --%>
