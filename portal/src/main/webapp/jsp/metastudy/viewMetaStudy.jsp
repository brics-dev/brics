<%@include file="/common/taglibs.jsp"%>
<title>View Meta Study</title>

<script type="text/javascript"
	src='/portal/js/metastudy/SavedQueryViewEngine.js'></script>
<script type="text/javascript" src='/portal/js/metastudy/SavedQuery.js'></script>

<script type="text/javascript"
	src='/portal/js/metastudy/SavedQueryDe.js'></script>
<script type="text/javascript"
	src='/portal/js/metastudy/SavedQueryDes.js'></script>

<script type="text/javascript"
	src='/portal/js/metastudy/SavedQueryForm.js'></script>
<script type="text/javascript"
	src='/portal/js/metastudy/SavedQueryForms.js'></script>

<script type="text/javascript"
	src='/portal/js/metastudy/SavedQueryRg.js'></script>
<script type="text/javascript"
	src='/portal/js/metastudy/SavedQueryRgs.js'></script>

<script type="text/javascript"
	src='/portal/js/metastudy/SavedQueryStudy.js'></script>
<script type="text/javascript"
	src='/portal/js/metastudy/SavedQueryStudies.js'></script>

<script type="text/javascript"
	src='/portal/js/metastudy/SavedQueryDeView.js'></script>
<script type="text/javascript"
	src='/portal/js/metastudy/SavedQueryRgView.js'></script>
<script type="text/javascript"
	src='/portal/js/metastudy/SavedQueryFormView.js'></script>
<script type="text/javascript"
	src='/portal/js/metastudy/SavedQueryStudyView.js'></script>
<script type="text/javascript"
	src='/portal/js/metastudy/SavedQueryView.js'></script>

<!-- begin .border-wrapper -->
<s:if test="viewMode == 1">
	<div class="border-wrapper wide">
</s:if>
<s:else>
	<div class="border-wrapper">
</s:else>
<jsp:include page="../navigation/metaStudyNavigation.jsp" />
<h1 class="float-left">Meta Study</h1>
<div style="clear: both"></div>

<!--begin #center-content -->
<div id="main-content">
	<s:if test="viewMode == 1">
		<h2>Create Meta Study</h2>
	</s:if>
	<s:else>
		<h2>View Meta Study</h2>
	</s:else>
	<s:if test="viewMode != 1">
		<h3 class="metaStudyHeader">
			<s:property value="currentMetaStudy.title" />
		</h3>
	</s:if>
	<s:form id="theForm" cssClass="validate" method="post" validate="true"
		enctype="multipart/form-data">
		<s:token />
		<s:hidden id="actionName" name="documentsActionName" />


		<s:if test="viewMode == 1">
			<ndar:editMetaStudyChevron action="metaStudyAction" chevron="Preview" />
		</s:if>

		<s:if test="hasActionErrors()">
			<div class="error-message">
				<s:actionerror />
			</div>
		</s:if>

		<h3 id="detailsLabel" class="clear-both collapsable">
			<span id="detailsLabelPlusMinus"></span>&nbsp;Details
		</h3>
		<div id="details">
			<div class="clear-right"></div>
			<s:if test="viewMode != 1">
				<input id="metaStudyId" type="hidden" name="metaStudyDetailsForm.id"
					value="${currentMetaStudy.id}" />
			</s:if>
			<div class="form-output">
				<div class="label">Title</div>
				<div class="readonly-text">
					<s:property value="currentMetaStudy.title" />
				</div>
			</div>

			<s:if test="viewMode != 1">
				<div class="form-output">
					<div class="label">Meta Study ID</div>
					<div class="readonly-text">
						<s:property value="currentMetaStudy.prefixId" />
					</div>
				</div>

				<div class="form-output">
					<div class="label">Meta Study ID Schema</div>
					<div class="readonly-text">BRICS Instance Generated</div>
				</div>

				<div class="form-output">

					<div class="label">Recruitment Status</div>
					<div class="readonly-text">
						<s:property value="currentMetaStudy.recruitmentStatus.name" />
					</div>
				</div>


				<div class="form-output">

					<div class="label">Study Type</div>
					<div class="readonly-text">
						<s:property value="currentMetaStudy.studyType.name" />
					</div>
				</div>
				<div class="form-output">
					<div class="label" style="white-space: nowrap;">Therapeutic
						Agents:</div>
					<div class="readonly-text">
						<c:forEach var="therapeuticAgentVal"
							items="${therapeuticAgentSet}">
							<c:out value="${therapeuticAgentVal.text}" />  &nbsp; &nbsp;
						</c:forEach>
					</div>
				</div>

				<div class="form-output">
					<div class="label" style="white-space: nowrap;">Therapy
						Types:</div>
					<div class="readonly-text">
						<c:forEach var="therapyTypeVal"
							items="${therapyTypeSet}">
							<c:out value="${therapyTypeVal.text}" />  &nbsp; &nbsp;
						</c:forEach>
					</div>
				</div>


				<div class="form-output">
					<div class="label" style="white-space: nowrap;">Therapeutic
						Targets:</div>
					<div class="readonly-text">
						<c:forEach var="therapeuticTargetVal"
							items="${therapeuticTargetSet}">
							<c:out value="${therapeuticTargetVal.text}" /> &nbsp; &nbsp;
						</c:forEach>
					</div>
				</div>

				<div class="form-output">
					<div class="label" style="white-space: nowrap;">Model Names:</div>
					<div class="readonly-text">
						<c:forEach var="modelNameVal"
							items="${modelNameSet}">
							<c:out value="${modelNameVal.text}" /> &nbsp; &nbsp;
						</c:forEach>
					</div>
				</div>

				<div class="form-output">
					<div class="label" style="white-space: nowrap;">Model Types:</div>
					<div class="readonly-text">
						<c:forEach var="modelTypeVal"
							items="${modelTypeSet}">
							<c:out value="${modelTypeVal.text}" /> &nbsp; &nbsp;
						</c:forEach>
					</div>
				</div>

				<div class="form-output">
					<div class="label">Study URL</div>
					<div class="readonly-text">
						<s:property value="currentMetaStudy.studyUrl" />
					</div>
				</div>

				<div class="form-output">
					<div class="label">Duration</div>
					<div class="readonly-text">
						<s:property value="duration" />
						days from
						<ndar:dateTag value="${currentMetaStudy.dateCreated}" />
						to
						<s:if test="isPublished">
							<ndar:dateTag value="${currentMetaStudy.lastUpdatedDate}" />
						</s:if>
						<s:else>
								Now
							</s:else>
					</div>
				</div>

				<div class="form-output">
					<div class="label">Status</div>
					<div class="readonly-text">
						<s:property value="currentMetaStudy.status.name" />
					</div>
				</div>

				<s:if test="isDoiEnabled">
					<div class="form-output">
						<div class="label">DOI</div>
						<div id="metaStudyDoiDisplay" class="readonly-text">
							<s:if
								test="currentMetaStudy.doi != null and !currentMetaStudy.doi.isEmpty()">
								<a
									href="<s:property value="getDoiResolverUrl()" /><s:property value="currentMetaStudy.doi" />">
									<s:property value="currentMetaStudy.doi" />
								</a>
							</s:if>
						</div>
					</div>
				</s:if>
			</s:if>

			<div class="form-output">
				<div class="label">Abstract</div>
				<div class="readonly-text">
					<s:property value="currentMetaStudy.abstractText" />
				</div>
			</div>
			<div class="form-field">
				<label for="metaStudyDetailsForm.aimsText">Aims </label>
				<div class="readonly-text">
					<s:property value="currentMetaStudy.aimsText" />
				</div>
			</div>


			<s:if test="viewMode != 1">
				<div class="form-output">
					<div class="label">Permission</div>
					<div class="readonly-text">
						<s:property value="currentPermissions.permission.name" />
					</div>
				</div>

				<div class="form-output">
					<div class="label">Owner</div>
					<div class="readonly-text">
						<s:property value="currentMetaStudyOwner.displayName" />
					</div>
				</div>

				<div class="form-output">
					<div class="label">Owner Email</div>
					<div class="readonly-text">
						<s:property value="currentMetaStudyOwner.user.email" />
					</div>
				</div>
			</s:if>

		</div>


		<h3 id="studyResearchMgmtLabel" class="clear-both collapsable">
			<span id="studyResearchMgmtLabelPlusMinus"></span>&nbsp;Study Research Management
		</h3>

		<div id="studyResearchMgmt">
			<div id="studyResearchMgmtTable" class="idtTableContainer">
				<table id="studyResearchMgmtTableTable"
					class="table table-striped table-bordered" width="100%"></table>
			</div>
		</div>


		<h3 id="studyInformationLabel" class="clear-both collapsable">
			<span id="studyInformationLabelPlusMinus"></span>&nbsp;Study
			Information
		</h3>
		<div id="studyInformation">
			<div class="form-output">
				<div class="label">Primary Funding Source</div>
				<div class="readonly-text">
					<s:property value="currentMetaStudy.fundingSource.name" />
				</div>
			</div>

			<div id="studyInformationContainer" class="idtTableContainer">
				<table id="studyInformationTable"
					class="table table-striped table-bordered" width="100%"></table>
			</div>
			<script type="text/javascript">
				$(document)
						.ready(
								function() {
									$('#studyInformationTable')
											.idtTable(
													{
														idtUrl : "<s:url value='/metastudy/metaStudyAction!getClinicalTrialMetaSet.action' />",
														"columns" : [ {
															"data" : 'clinicalTrialId',
															"title" : 'Clinical Trial ID',
															"name" : 'Clinical Trial ID',
															"parameter" : 'clinicalTrialId',
															"searchable" : true,
															"orderable" : true
														} ]
													});
								});
			</script>
			<div id="grantTableContainer" class="idtTableContainer">
				<table id="grantTable" class="table table-striped table-bordered"
					width="100%"></table>
			</div>
			<script type="text/javascript">
				$(document)
						.ready(
								function() {
									$('#grantTable')
											.idtTable(
													{
														idtUrl : "<s:url value='/metastudy/metaStudyAction!getGrantMetaSet.action' />",
														"columns" : [ {
															"data" : 'grantId',
															"title" : 'Grant ID',
															"name" : 'Grant ID',
															"parameter" : 'grantId',
															"searchable" : true,
															"orderable" : true
														} ]
													});
								});
			</script>
		</div>

		<h3 id="documentationLabel" class="clear-both collapsable">
			<span id="documentationLabelPlusMinus"></span>&nbsp;Documentation
		</h3>
		<div id="documentation">
			<div id="documentationDetailsDialog" style="display: none"></div>
			<div id="documentationListContainer" class="idtTableContainer">
				<div id="dialog"></div>
				<table id="documentationListTable"
					class="table table-striped table-bordered" width="100%"></table>
			</div>
			<script type="text/javascript">
				$(document)
						.ready(
								function() {
									$('#documentationListTable')
											.idtTable(
													{
														idtUrl : "<s:url value='/metastudy/metaStudyAction!getDocumentionList.action' />",
														idtData : {
															primaryKey : "id"
														},
														autoWidth: false,
														"columns" : [
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
																	"data" : 'typeLink',
																	"title" : 'Type',
																	"name" : 'Type',
																	"parameter" : 'typeLink',
																	"width": '20%',
																	"searchable" : true,
																	"orderable" : true,
																},
																{
																	"data" : 'description',
																	"title" : 'Description',
																	"name" : 'Description',
																	"parameter" : 'description',
																	"width": '23%',
																	"searchable" : true,
																	"orderable" : true,
																	"render" : IdtActions
																			.ellipsis(45)
																},
																{
																	"data" : 'dateCreated',
																	"title" : 'Date Uploaded',
																	"name" : 'Date Uploaded',
																	"parameter" : 'dateCreated',
																	"width": '15%',
																	"searchable" : true,
																	"orderable" : true,
																	"render" : IdtActions
																			.formatDate()
																}

														]
													});
								});
			</script>
		</div>


		<h3 id="dataLabel" class="clear-both collapsable">
			<span id="dataLabelPlusMinus"></span>&nbsp;Data
		</h3>
		<div id="data">
			<div id="dataListContainer" class="idtTableContainer">
				<table id="dataListTable" class="table table-striped table-bordered"
					width="100%"></table>
			</div>
			<script type="text/javascript">
				$(document)
						.ready(
								function() {
									$('#dataListTable')
											.idtTable(
													{
														
														idtUrl : "<s:url value='/metastudy/metaStudyAction!getMetaStudyDataSet.action' />",
														autoWidth: false,
														"columns" : [
																{
																	"data" : 'dataNameLink',
																	"title" : 'Data',
																	"name" : 'Data',
																	"parameter" : 'dataNameLink',
																	"width": '25%',
																	"searchable" : true,
																	"orderable" : true,
																},
																{
																	"data" : 'source',
																	"title" : 'Source',
																	"name" : 'Source',
																	"parameter" : 'source',
																	"width": '17%',
																	"searchable" : true,
																	"orderable" : true,
																},
																{
																	"data" : 'type',
																	"title" : 'Type',
																	"name" : 'Type',
																	"parameter" : 'fileType.name',
																	"width": '20%',
																	"searchable" : true,
																	"orderable" : true,
																},
																{
																	"data" : 'description',
																	"title" : 'Description',
																	"name" : 'Description',
																	"parameter" : 'description',
																	"width": '23%',
																	"searchable" : true,
																	"orderable" : true,
																	"render" : IdtActions
																			.ellipsis(35)
																},
																{
																	"data" : 'dateCreated',
																	"title" : 'Date Uploaded',
																	"name" : 'Date Uploaded',
																	"parameter" : 'dateCreated',
																	"width": '15%',
																	"searchable" : true,
																	"orderable" : true,
																	"render" : IdtActions
																			.formatDate()
																}

														]
													});
								});
			</script>
		</div>


		<h3 id="keywordLabel" class="clear-both collapsable">
			<span id="keywordLabelPlusMinus"></span>&nbsp;Keywords and Labels
		</h3>
		<div id="keyword">
			<div class="form-output">
				<div class="label">Keywords</div>
				<div class="readonly-text">
					<s:iterator var="metaStudyKeyword"
						value="currentMetaStudy.metaStudyKeywords" status="rowstatus">
						<c:out value="${metaStudyKeyword.keyword}" />
						<s:if test="!#rowstatus.last">,</s:if>
					</s:iterator>
				</div>
			</div>

			<div class="form-output">
				<div class="label">Labels</div>
				<div class="readonly-text">
					<s:iterator var="metaStudyLabel"
						value="currentMetaStudy.metaStudyLabels" status="rowstatus">
						<c:out value="${metaStudyLabel.label}" />
						<s:if test="!#rowstatus.last">,</s:if>
					</s:iterator>
				</div>
			</div>
		</div>
		<s:if test="hasAdminPermission && viewMode != 1">
			<h3 id="accessRecordLabel" class="clear-both collapsable">
				<span id="accessRecordLabelPlusMinus"></span>&nbsp;Data Access
				Report
			</h3>
			<div id="accessRecord">
				<jsp:include page="metaStudyAccessReportFrame.jsp" />
			</div>
			<br />
		</s:if>

		<s:if test="viewMode == 1">
			<div class="button" style="margin-right: 10px;">
				<input type="button" value="Back"
					onClick="javascript:submitForm('metaStudyAction!moveToKeyword.action')" />
			</div>

			<div class="form-field inline-right-button">
				<div class="button" style="margin-right: 5px;">
					<input type="button" value="Create & Finish"
						onclick="javascript:createMetaStudy()" />
				</div>
				<a class="form-link" href="javascript:void(0)"
					onclick="javascript:cancelCreation()">Cancel</a>
			</div>
		</s:if>
		<s:else>
			<div class="button">
				<input type="button" value="Close"
					onClick="javascript:window.location='metaStudyListAction!list.action'" />
			</div>
		</s:else>


	</s:form>

	<div class="ibisMessaging-dialogContainer"></div>
</div>

<s:if test="viewMode != 1">
	<jsp:include page="metaStudyActionBar.jsp" />
</s:if>
</div>

<script type="text/javascript" src="/portal/js/uploadDocumentations.js"></script>
<script type="text/javascript" src="/portal/js/metastudy/metaStudy.js"></script>
<script type="text/javascript">
	function viewGrantInfo(grantId) {
		var f = $(
				"<form target='_blank' method='POST' style='display:none;'></form>")
				.attr(
						{
							action : "<s:property value='@gov.nih.tbi.PortalConstants@FEDERAL_REPORTER_SEARCH_URL' />"
						}).appendTo(document.body);
		$('<input type="hidden" />').attr({
			name : "projectNumbers",
			value : grantId
		}).appendTo(f);

		$('<input type="hidden" />').attr({
			name : "projectNumbersRaw",
			value : grantId
		}).appendTo(f);

		$('<input type="hidden" />').attr({
			name : "searchMode",
			value : "Smart"
		}).appendTo(f);
		f.submit();
		f.remove();

	}

	var viewMode = "<c:out value="${viewMode}" />";

	var dataTableFilters = {
		timeFilter : "all" //|| "lastWeek" || "lastMonth"
	};

	if (viewMode == "1") {
		setNavigation({
			"bodyClass" : "primary",
			"navigationLinkID" : "metaStudyModuleLink",
			"subnavigationLinkID" : "metaStudyLink",
			"tertiaryLinkID" : "createMetaStudyLink"
		});
	} else {
		setNavigation({
			"bodyClass" : "primary",
			"navigationLinkID" : "metaStudyModuleLink",
			"subnavigationLinkID" : "metaStudyLink",
			"tertiaryLinkID" : "none"
		});
	}

	EventBus.on("init:table", function(model) {
		model.get("$el").parents(".dataTableContainer").on(
				"change",
				".fg-toolbar select",
				function() {
					var $this = $(this);
					var $table = $this.parents(".dataTableContainer").find(
							"table");
					var arrChangeVal = $this.val().split(":");
					dataTableFilters[arrChangeVal[0]] = arrChangeVal[1];
					var model = IDT.getTableModel($table);
					model.get("datatable").fnDraw();
				});
	});

	$(document)
			.ready(
					function() {
						$("#studyResearchMgmtTableTable")
								.idtTable(
										{
											idtUrl : "<s:url value='/metastudy/metaStudyAction!getResearchMgmtMetaSet.action' />",
											idtData : {
												primaryKey : "id"
											},
											columns : [ {
												data : "Title",
												title : "Title",
												name : "Title",
												parameter : "roleTitle"
											}, {
												data : "fullName",
												title : "Full Name",
												name : "fullName",
												parameter : "fullName"
											}, {
												data : "email",
												title : "E-Mail",
												name : "email",
												parameter : "email"
											}, {
												data : "orgName",
												title : "Organization",
												name : "orgName",
												parameter : "orgName"
											} ]
										});

						$("#detailsLabelPlusMinus").text("-");
						$("#studyResearchMgmtLabelPlusMinus").text("+");
						$("#studyResearchMgmt").hide();
						$("#studyInformationLabelPlusMinus").text("+");
						$("#studyInformation").hide();
						$("#documentationLabelPlusMinus").text("+");
						$("#documentation").hide();
						$("#dataLabelPlusMinus").text("+");
						$("#data").hide();
						$("#keywordLabelPlusMinus").text("+");
						$("#keyword").hide();
						$("#accessRecordLabelPlusMinus").text("+");
						$("#accessRecord").hide();

						detailsInit();
						documentationInit();
						dataInit();
						keywordLabelInit();
						accessRecordLabelInit();
						studyResearchMgmtInit();
						studyInformationInit();

						var weekInMilliseconds = 604800000;
						// can't do month because month has different numbers of days.  Date is faster than implementing myself
						$.fn.dataTableExt.afnFiltering
								.push(function(oSettings, aData, iDataIndex) {
									if (oSettings.oInstance[0].parentNode.parentNode.id == "accessRecordLabelContainer") {
										if (dataTableFilters.timeFilter != "all") {
											var tableDate = aData[3];
											var dateTimestamp = new Date(
													tableDate).getTime();
											if (dataTableFilters.timeFilter == "lastWeek") {
												var lastWeekTimestamp = new Date()
														.getTime()
														- weekInMilliseconds;
												return dateTimestamp > lastWeekTimestamp;
											} else if (dataTableFilters.timeFilter == "lastMonth") {
												var lastMonth = new Date();
												// the below is already a timestamp
												var lastMonthTimestamp = lastMonth
														.setMonth(lastMonth
																.getMonth() - 1);
												return dateTimestamp > lastMonthTimestamp;
											} else if (dataTableFilters.timeFilter == "lastYear") {
												var lastYear = new Date();
												// the below is already a timestamp
												var lastYearTimestamp = lastYear
														.setYear(lastYear
																.getYear() - 1);
												return dateTimestamp > lastYearTimestamp;
											}
										}
									}
									return true;
								});
					});
	

	function detailsInit() {
		$("#detailsLabel").click(function() {
			$("#details").slideToggle("fast");
			if ($("#detailsLabelPlusMinus").text() == "+") {
				$("#detailsLabelPlusMinus").text("- ");
			} else {
				$("#detailsLabelPlusMinus").text("+");
			}
		});
	}

	function studyResearchMgmtInit() {
		$("#studyResearchMgmtLabel").click(function() {
			$("#studyResearchMgmt").slideToggle("fast");
			if ($("#studyResearchMgmtLabelPlusMinus").text() == "+") {
				$("#studyResearchMgmtLabelPlusMinus").text("- ");
			} else {
				$("#studyResearchMgmtLabelPlusMinus").text("+");
			}
		});
	}

	function studyInformationInit() {

		$("#studyInformationLabel").click(function() {
			$("#studyInformation").slideToggle("fast");
			if ($("#studyInformationLabelPlusMinus").text() == "+") {
				$("#studyInformationLabelPlusMinus").text("- ");
			} else {
				$("#studyInformationLabelPlusMinus").text("+");
			}
		});
	}

	function documentationInit() {
		$("#documentationLabel").click(function() {
			$("#documentation").slideToggle("fast");
			if ($("#documentationLabelPlusMinus").text() == "+") {
				$("#documentationLabelPlusMinus").text("- ");
			} else {
				$("#documentationLabelPlusMinus").text("+");
			}
		});
	}

	function dataInit() {
		$("#dataLabel").click(function() {
			$("#data").slideToggle("fast");
			if ($("#dataLabelPlusMinus").text() == "+") {
				$("#dataLabelPlusMinus").text("- ");
			} else {
				$("#dataLabelPlusMinus").text("+");
			}
		});
	}

	function keywordLabelInit() {
		$("#keywordLabel").click(function() {
			$("#keyword").slideToggle("fast");
			if ($("#keywordLabelPlusMinus").text() == "+") {
				$("#keywordLabelPlusMinus").text("- ");
			} else {
				$("#keywordLabelPlusMinus").text("+");
			}
		});
	}

	function accessRecordLabelInit() {
		$("#accessRecordLabel").click(function() {
			$("#accessRecord").slideToggle("fast");
			if ($("#accessRecordLabelPlusMinus").text() == "+") {
				$("#accessRecordLabelPlusMinus").text("- ");
			} else {
				$("#accessRecordLabelPlusMinus").text("+");
			}
		});
	}

	function viewClinicalTrial(clinicalTrialId) {
		$.post("metaStudyAction!viewClinicalTrial.ajax", {
			clinicalTrialId : clinicalTrialId
		}, function(data) {
			$.fancybox(data);
		});
	}
</script>

<%-- ##### Begin DOI Dialog UI ##### --%>
<s:if test="%{canAssignDoiForMetaStudy()}">
	<script type="text/javascript"
		src="/portal/formbuilder/js/lib/underscore-min.js"></script>
	<jsp:include page="../common/createDoiDialogUI.jsp" />
	<script type="text/javascript">
		$("document")
				.ready(
						function() {
							// Start listening for DOI events.
							EventBus
									.on(
											"create:doi",
											function(data, $dialogDiv) {
												// Hide the "Assign DOI" link.
												$("#assignDoiLinkDisplay")
														.hide();

												// Display the new DOI on the page.
												var link = '<a href="<s:property value="getDoiResolverUrl()" />'
														+ data.doi
														+ '">'
														+ data.doi + '</a>';
												$("#metaStudyDoiDisplay").html(
														link);
											});

							EventBus.on("error:doi",
									function(cause, $dialogDiv) {
									});

							// Initialize the create DOI dialog UI.
							initCreateDoiDialog("createDoiForMetaStudy.action",
									"meta study", false);

							// Bind click handler to the "Assign DOI" link on the action bar.
							$("#metaStudyOps_assignDoi").click(
									_.debounce(function() {
										$("#doiDialogUi").dialog("open");
									}, 1000, true));
						});
	</script>
</s:if>
<%-- ##### Begin DOI Dialog UI ##### --%>
