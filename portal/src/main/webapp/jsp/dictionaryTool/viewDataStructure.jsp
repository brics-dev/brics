<%@include file="/common/taglibs.jsp"%>
<title>Form Structure: <s:property value="currentDataStructure.title" /></title>

<style type="text/css">
 .setFontSize{
 	font-size:1.3em;
 }
</style>

<input type="hidden" name="currentId" id="currentId" value="<s:property value="currentDataStructure.id" />" />
<input type="hidden" name="numberOfAffectedDE" id="numberOfAffectedDE" value="<s:property value="numberOfAffectedDE" />" />
<input type="hidden" name="requestedStatusChange" id="requestedStatusChange" value="<s:property value="isRequestedStatusChange" /> "/>
<input type="hidden" name="inAdmin" id="inAdmin" value="<s:property value="inAdmin" /> "/>
<input type="hidden" name="currentAction" id="currentAction" value="dataStructureAction"/>

<div class="clear-float">
	<jsp:include page="dictionaryStatusChange.jsp"></jsp:include>
</div>

<div class="border-wrapper">
	<s:if test="%{nameSpace != 'publicData'}">
		<s:if test="!fromRepository">
			<div id="breadcrumb">
				<s:if test="inAdmin">
					<s:a action="listDataStructureAction" method="list" namespace="/dictionaryAdmin">Manage Form Structures</s:a>
				</s:if>
				<s:else>
					<s:a action="listDataStructureAction" method="list" namespace="/dictionary">Search Form Structures</s:a>
				</s:else>
				&gt;&nbsp;
				<s:property value="currentDataStructure.title" />
			</div>
		</s:if>
	</s:if>
	<div style="clear:both;"></div>
	
	<s:if test="%{nameSpace != 'publicData'}">
		<jsp:include page="../navigation/dataDictionaryNavigation.jsp" />
		<h1 class="float-left"><s:if test="inAdmin">(Admin)&nbsp;</s:if>Data Dictionary</h1>
		<div style="clear:both;"></div>
	</s:if>

	<div id="main-content" style="margin-top:15px;">
	
	<div id="leftContent" style="float:left; width:100%;">
		<div class="publication-notifications" style="border:1px solid #73B9FF; padding:8px; color:#000; display:none">
		</div>

		<h2 class="pageHeader">Form Structure: <s:property value="currentDataStructure.title" /></h2>

		<s:set var="currentDataStructure" value="currentDataStructure" />
		<s:set var="sessionDataStructure" value="sessionDataStructure" />
		<s:set var="createdHistory" value="createdHistory" />
		<s:set var="repeatableGroups" value="currentDataStructure.repeatableGroups" />
		<s:set var="ownerName" value="ownerName" />

		<div class="form-output" style="border:1px solid #73B9FF; padding:8px; margin:5px 0px; color:#000;">
				<s:if test="currentDataStructure.isCopyrighted">
					<div class="readonly-text">This form structure is an organized set of data definitions for a copyrighted form</div>
				</s:if>
				<s:else>
					<div class="readonly-text">This form structure is an organized set of data definitions for a form that has not been copyrighted</div>
				</s:else>
			</div>
	
		<!---- General Details ---->
		
		<h3 id="generalLabel" class="clear-both collapsable">
			<span id="generalLabelPlusMinus"></span>&nbsp;General Details
		</h3>
		<div id="general">
			<div class="clear-right"></div>
			<div class="form-output">
				<div class="label">Title:</div>
				<div class="readonly-text">
					<s:property value="currentDataStructure.title" />
				</div>
			</div>
			<div class="form-output">
				<div class="label">Short Name:</div>
				<div class="readonly-text">
					<s:property value="currentDataStructure.shortName" />
				</div>
			</div>
			<div class="form-output">
				<div class="label">Description:</div>
				<div id="description" class="readonly-text limitLength">
					<s:property value="currentDataStructure.rDFDescription" />
				</div>
			</div>
			<div class="form-output">
				<div class="label">Disease:</div>
				<div class="readonly-text"> 
				<s:iterator var="diseaseStructure" value="currentDataStructure.diseaseList" status="diseaseStatus">
					<c:out value="${diseaseStructure.disease.name}" />
					<s:if test="!#diseaseStatus.last">,</s:if>
				</s:iterator>
				</div>
			</div>
			<s:if test="currentDataStructure.organization!=''">
				<div class="form-output">
					<div class="label">Organization:</div>
					<div class="readonly-text">
						<s:property value="currentDataStructure.organization" />
					</div>
				</div>
			</s:if>
			<div class="form-output">
				<div class="label">Required Program Form:</div>
				<s:if test="%{isRequired}">
					<div class="readonly-text">
						<c:out value="Yes" />
					</div>
				</s:if>
				<s:else> 
					<div class="readonly-text">
						<c:out value="No" />
					</div>
				</s:else>
			</div>
			
			<div class="form-output">
				<div class="label">Standardization:</div>
				<div class="readonly-text">
					<s:property value="currentDataStructure.standardization.display" />
				</div>
			</div>

			<div class="form-output">
				<div class="label">Form Type:</div>
				<div class="readonly-text">
					<s:property value="currentDataStructure.fileType.type" />
				</div>
			</div>
			<s:if test="currentDataStructure.documentationUrl!=null">
				<div class="form-output">
					<div class="label">Documentation:</div>
					<div class="readonly-text">
						<a href="<s:property value="currentDataStructure.documentationUrl" />" target="_blank"><s:property
								value="currentDataStructure.documentationUrl" /></a>
					</div>
				</div>
			</s:if>
			<s:if test="currentDataStructure.publicationDate!=null">
				<div class="form-output">
					<div class="label">Publication Date:</div>
					<div class="readonly-text">
					
					<ndar:dateTag value="${currentDataStructure.publicationDate}" />
					
					</div>
				</div>
			</s:if>
			<div class="form-output">
				<div class="label">Version:</div>
				<div class="readonly-text">
					<s:property value="currentDataStructure.version" />
				</div>
			</div>
			<div class="form-output">
				<div class="label">Date Created:</div>
				<div class="readonly-text">
					<s:property value="currentDataStructure.dateCreatedString" />
				</div>
			</div>
			<s:if test="%{nameSpace != 'publicData'}">
			<div class="form-output">
				<div class="label">Created By:</div>
				<div class="readonly-text">
					<s:property value="currentDataStructure.createdBy" />
				</div>
			</div>
			</s:if>
			<div class="form-output">
				<div class="label">Owner:</div>
				<div class="readonly-text">
					<s:property value="ownerName" />
				</div>
			</div>
			<div class="form-output">
				<div class="label">Number of Data Elements:</div>
				<div class="readonly-text">
					<c:out value="${fn:length(sessionDataElementList.mapElements)}" />
				</div>
			</div>
			<div class="form-output">
				<div class="label">eForms:</div>
				<div class="readonly-text">
					<s:if test="hasAssociatedEforms">
						<c:out value="Y" />
					</s:if>
					<s:else>
						<c:out value="N" />
					</s:else>
				</div>
			</div>
		</div>
		<br/>
		<s:if test="%{nameSpace != 'publicData'}">
			<s:if test="currentDataStructure.status.id == 2">
				<div id="eformShowHideDiv" style="display: none;">
					<h3 id="eformLabel" class="clear-both collapsable">
						<span id="eformLabelPlusMinus"></span>&nbsp;eForms
					</h3>
					<div id="eform">
						<div id="eformsContainer" class="idtTableContainer">
							<table id="eformTable" class="table table-striped table-bordered" width="100%"></table>
						</div>
					</div>
					<br />
				</div>
			</s:if>
		</s:if>
		
		<h3 id="documentationLabel" class="clear-both collapsable">
			<span id="documentationLabelPlusMinus"></span>&nbsp;Documentation
		</h3>
		<div id="documentation">
			<jsp:include page="dictionaryDocumentationList.jsp">
		        <jsp:param name="idtUrl" value="dataStructureAction!getSupportingDocumentationList.action"/>
		    </jsp:include>
		</div>
		<br />
		
		<!---- Data Elements ---->
		
		<h3 id="dataElementsLabel" class="clear-both collapsable">
			<span id="dataElementsLabelPlusMinus"></span>&nbsp;Groups & Attached Data Elements
		</h3>		
		<div id="dataElements">
			<div class="clear-right"></div>
			Logically grouped data elements with defined frequency at which they repeat.<br/><br/>
			<jsp:include page="dataStructure/attachedDataElements.jsp" />
		</div>
		<br/>
		
		<!---- Change History ---->
		
		<h3 id="historyLabel" class="clear-both collapsable">
			<span id="historyLabelPlusMinus"></span>&nbsp;Change History
		</h3>
		<div id="history">
			<div class="clear-right"></div>
			<jsp:include page="historyLogList.jsp">
		        <jsp:param name="idtUrl" value="dataStructureAction!getHistoryLogList.action"/>
		    </jsp:include>
		</div>
		
		
		<!---- Admin Change History ---->
		<h3 id="adminHistoryLabel" class="clear-both collapsable">
			<span id="adminHistoryLabelPlusMinus"></span>&nbsp;Administrative Change History
		</h3>
		<div id="adminHistory">
			<div class="clear-right"></div>
			<jsp:include page="dictionaryEventLogList.jsp">
		        <jsp:param name="idtUrl" value="dataStructureAction!getDictionaryEventLogList.action"/>
		    </jsp:include>
		</div>
		
		
		

		<s:if test="%{nameSpace != 'publicData'}">
			<div class="button">
				<s:if test="fromRepository">
					<input type="button" value="Close"
						onClick="javascript: window.location = '${modulesSTURL}repositoryAdmin/dataStoreInfoAction!list.action'" />
				</s:if>
				<s:else>
					<input type="button" value="Close"
						onClick="javascript: window.location = '/portal/dictionary/listDataStructureAction!list.action'" />
				</s:else>
			</div>
		</s:if>

	</div>
	</div>

	<s:if test="%{nameSpace != 'publicData'}">
		<div id="actionsBar">
			<span id="pubInterface"></span>
			<ul>				
				<div id="draftEdits">
					<s:if test="%{isLatestVersion}">
						<s:if test="%{canEdit}">
							<li id="editForm"><span class="icon"><img alt="Edit" src="<s:url value='/images/icons/edit.png' />" style="padding-right:10px;" height="15px" width="15px" /></span><a href="javascript:void(0);"  onclick="confirmEdit(${currentDataStrucutre.status.id})">Edit</a></li>
						</s:if>
					</s:if>
					<s:if test="%{canAdmin}">
						<li id="deleteForm"><span class="icon"><img alt="Delete" src="<s:url value='/images/icons/delete.png' />" style="padding-right:10px;" height="15px" width="15px" /></span><a href="javascript:void(0);"  onclick="confirmDelete()">Delete</a></li>
					</s:if>
				</div>
				
				<s:if test="%{isLatestVersion}">	
					<li id="publishedEdits"><span class="icon"><img alt="Create Draft Copy" src="<s:url value='/images/icons/draft-copy.png' />" style="padding-right:10px;" height="15px" width="15px" /></span><a  href="javascript:void(0);"  onclick="confirmCreate()">Create Draft Copy</a></li>
				</s:if>
			
				<li id="detailedReport">
					<span class="icon"><img alt="Detailed Report" src="<s:url value='/images/icons/detailed-report.png' />" style="padding-right:10px;" height="15px" width="15px" /></span>
					<a href="dataElementExportAction!exportFSDataElementsDetail.action"> Data Element Report</a>
				</li>
		
				<li id="redcapReport">
					<span class="icon">
						<img alt="REDCap Report" src="<s:url value='/images/icons/redcap.png' />" style="float:left; padding-right:10px;" height="15px" width="15px" />
					</span>
					<a href="javascript:dataElemetReportRedcap()" >Data Element Report - REDCap format</a><br style="clear: both;" />
				</li>
				
				<li id="exportFS">
					Export Form Structure: <br>
					<ndar:actionLink value="XML" action="dataStructureXmlExportAction!export.action" /> |
					<ndar:actionLink value="CSV" 
							action="dataStructureCsvExportAction!export.action?dataStructureName=${currentDataStructure.shortName}" /> | <br>
					<ndar:actionLink value="CSV with Sample Data" 
							action="dataStructureCsvExportAction!exportWithData.action?dataStructureName=${currentDataStructure.shortName}" />
				</li>
			</ul>
		</div>
	</s:if>
	<s:elseif test="%{nameSpace == 'publicData'}">
    	<div id="actionsBar">
   			<ul>
	   			<li id="detailedReport"> 
					<span class="icon">
						<img alt="Detailed Report" src="<s:url value='/images/icons/detailed-report.png' />" style="float: left; padding-right:10px;" height="15px" width="15px" />
					</span>
					<a href="dataElementExportAction!exportFSDataElementsDetail.action"> Data Element Report - Export to CSV</a>
				</li>		
				<li id="redcapReport">
					<span class="icon">
						<img alt="REDCap Report" src="<s:url value='/images/icons/redcap.png' />" style="float:left; padding-right:10px;" height="15px" width="15px" />
					</span>
					<a href="javascript:dataElemetReportRedcap()" >Data Element Report - REDCap format</a><br style="clear: both;" />
				</li>
			</ul>
    	</div>
	</s:elseif>
</div>
<script type="text/javascript" src="/portal/js/statusChange/dictionaryStatusChange.js"></script>
<script type="text/javascript">
	<s:if test="fromRepository">
		setNavigation({"bodyClass":"primary", "navigationLinkID":"dataRepositoryModuleLink", "subnavigationLinkID":"contributeDataToolsLink", "tertiaryLinkID":"repositoryList"});
	</s:if>
	<s:elseif test="inAdmin">
		setNavigation({"bodyClass":"primary", "navigationLinkID":"dataDictionaryModuleLink", "subnavigationLinkID":"defineDataToolsLink", "tertiaryLinkID":"manageDataStructuresLink"});
	</s:elseif>
	<s:else>
		setNavigation({"bodyClass":"primary", "navigationLinkID":"dataDictionaryModuleLink", "subnavigationLinkID":"dataDictionaryToolLink", "tertiaryLinkID":"listDataStructureLink"});
	</s:else>

	var currentState;
	
	$('document').ready( function() {
		$("#detailsLabelPlusMinus").text("-");
		$("#documentationLabelPlusMinus").text("+");
		$("#eformLabelPlusMinus").text("-");
		$("#documentation").hide();
		$("#dataLabelPlusMinus").text("-");
		$("#keywordLabelPlusMinus").text("-");
		
		<s:if test="%{nameSpace != 'publicData'}">
			publicationFields('<s:property value="currentDataStructure.status.id" escapeJavaScript="true"/>');
		</s:if>
		
		$("#generalLabelPlusMinus").text("-");
		$("#dataElementsLabelPlusMinus").text("-");
		$("#historyLabelPlusMinus").text("+");
		$("#history").hide();
		
		$("#adminHistoryLabelPlusMinus").text("+");
		$("#adminHistory").hide();

		generalInit();	
		dataElementsInit();
		<s:if test="currentDataStructure.status.id == 2">
			eformInit();
			eformLoadTableData();
		</s:if> 
		historyInit();
		documentationInit();
		adminHistoryInit();

	});
	
	function eformLoadTableData(){
		var formStructureShortName = '<s:property value="currentDataStructure.shortName" />';
		$("#eformShowHideDiv").show();
		$("#eformTable").idtTable({
			idtUrl: "eFormSearchAction!getSearchAssociatedForm.action",
			filterData: {
				formStructureName : formStructureShortName
			},
			autoWidth: false,
			columns: [
				{
					title: "Title",
					data: "title",
					name: "Title",
					parameter: "title",
					width: "11%"
				},
				{
					title: "Description",
					data: "description",
					name: "Description",
					parameter: "description",
					width: "41%",
					render: IdtActions.ellipsis(200)
				},
				{
					title: "Last Update",
					data: "updatedDate",
					name: "Last Update",
					parameter: "updatedDate",
					width: "25%",
					render: IdtActions.formatDate()
				}
			]
	});
	}
	
	function eformInit() {
		$("#eformLabel").click(function(){
			$("#eform").slideToggle("fast");
			if($("#eformLabelPlusMinus").text()=="+") {
				$("#eformLabelPlusMinus").text("- ");
			} else {
				$("#eformLabelPlusMinus").text("+");
			}
		});
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
	
	function documentationInit() {
		$("#documentationLabel").click(function(){
			$("#documentation").slideToggle("fast");
			if($("#documentationLabelPlusMinus").text()=="+") {
				$("#documentationLabelPlusMinus").text("- ");
			} else {
				$("#documentationLabelPlusMinus").text("+");
			}
		});
	}
	
	function generalInit() {
		$("#generalLabel").click(function(){
			$("#general").slideToggle("fast");
			if($("#generalLabelPlusMinus").text()=="+") {
				$("#generalLabelPlusMinus").text("- ");
			} else {
				$("#generalLabelPlusMinus").text("+");
			}
		});
	}

	function dataElementsInit() {
		$("#dataElementsLabel").click(function(){
			$("#dataElements").slideToggle("fast");
			if($("#dataElementsLabelPlusMinus").text()=="+") {
				$("#dataElementsLabelPlusMinus").text("- ");
			} else {
				$("#dataElementsLabelPlusMinus").text("+");
			}
		});
	}
	
	function historyInit() {
		$("#historyLabel").click(function(){
			$("#history").slideToggle("fast");
			if($("#historyLabelPlusMinus").text()=="+") {
				$("#historyLabelPlusMinus").text("- ");
			} else {
				$("#historyLabelPlusMinus").text("+");
			}
		});
	}
	
	function adminHistoryInit() {
		
		$("#adminHistoryLabel").click(function(){
			
			var dictionaryAdmin = '<s:property value="isDictionaryAdmin" />';
			
			if(dictionaryAdmin!=='true'){
				alert("You do not have the correct permissions to view this section.");
				return;
			}
				
			$("#adminHistory").slideToggle("fast");
			if($("#adminHistoryLabelPlusMinus").text()=="+") {
				$("#adminHistoryLabelPlusMinus").text("- ");
			} else {
				$("#adminHistoryLabelPlusMinus").text("+");
			}
		});
	}
	
	function confirmCreate() {
		var where_to = confirm("Are you sure you want to create a new draft copy of this form?");
		if (where_to == true) {
			window.location = "dataStructureAction!changeCurrentStructure.action?dataStructureName=${currentDataStructure.shortName}";
		}
	}
	
	function viewDataElementsPage(){
		
	}
	
	function confirmEdit(statusId) {
		
		var statusId = ('${currentDataStructure.status.id}' == '') ? '${currentDataStructure.status.id}' : 1;
		
		if (currentState==1||statusId == 1) {
			var where_to = confirm("Are you sure you want to edit this form?");
			if (where_to == true) {
				if ('<s:property value="sessionDataStructure" escapeJavaScript="true"/>' != null
						&& '<s:property value="sessionDataStructure.dataStructure" escapeJavaScript="true"/>' != null) {
					window.location = "dataStructureAction!edit.action";
				} else {
					window.location = "dataStructureAction!changeCurrentStructure.action";
				}
			}
		} else {
			window.location = "dataStructureAction!edit.action";
		}
	}

	function confirmDelete() {
		var where_to = confirm("Are you sure you want to delete this form?");
		if (where_to == true) {
			window.location = "dataStructureAction!delete.action?dataStructureId=${currentDataStructure.id}";
		}
	}
	var confirmPublished = false;
	var statusId ;
	
	function publication(statusId) {
		
		statusId=statusId;
		// Request Publication or Publish (admin)
		if (statusId == 1 || statusId == 2) {
			var retiredDECount = '<s:property value="retiredDataElementCount" />';
			
			if (retiredDECount > 0) {
				var plural = (retiredDECount > 1) ? "s" : "";
				alert("This form structure has " + retiredDECount + " data element" + plural + " in Retired status and cannot be published.");
				return;
				  
			} else {
				var deprecatedDECount = '<s:property value="deprecatedDataElementCount" />';
				
				if (deprecatedDECount > 0) {
					var plural = (deprecatedDECount > 1) ? "s" : "";
					if (!confirm("Warning: This form structure contains " + deprecatedDECount + " deprecated data element" + 
							plural + ". Do you want to continue?")) {
						return;
					}
				}
			}
		}
		
		var location= $('#reqPubId').attr("href");
		var html =  $('#reqPubId').html();
		 	
		var isAllPublished = '<s:property value="isAllPublished" />';
		var currentId = ${currentDataStructure.id};
		
		var inAdmin = $('#inAdmin').val().trim();
		
		if (statusId == 2 && isAllPublished != 'true') {
			var where_to = confirm("Publishing this form structure will also publish all of its attached data elements, continue?");
			if (where_to == true) {
				
				//look into fsStatusChange.js for method implementation
				saveHistoryFsChangeStatus(statusId,location,html);
			} 
		} 
		//if status change is request publication or cancel request for publication, no need to  save the status change
		else if(inAdmin == 'false') {
			 publicationAction(statusId);
		}	
		else{
			saveHistoryFsChangeStatus(statusId,location,html);	   
		}	
	}
	
	function approvePublication() {
		
		var retiredDECount = '<s:property value="retiredDataElementCount" />';	
		
		if (retiredDECount > 0) {
			var plural = (retiredDECount > 1) ? "s" : "";
			alert("This form structure has " + retiredDECount + " data element" + plural + " in Retired status and cannot be published.");
			return;
			  
		} else {
			var deprecatedDECount = '<s:property value="deprecatedDataElementCount" />';
			
			if (deprecatedDECount > 0) {
				var plural = (deprecatedDECount > 1) ? "s" : "";
				if (!confirm("Warning: This form structure contains " + deprecatedDECount + " deprecated data element" + 
						plural + ". Do you want to continue?")) {
					return;
				}
			}	
			
			var location= $('#reqPubId').attr("href");
			var html =  $('#reqPubId').html();
			
			saveHistoryFsChangeStatus(2,location,html);				 
		}
	}
	
	function denyPublication() {
		
		var location= $('#reqPubId').attr("href");
		var html =  $('#reqPubId').html();
		
		saveHistoryFsChangeStatus(0,location,html);	
	}


	function publicationFields(statusId) {
		
		var isAllPublished = '<s:property value="isAllPublished" />';
		publicationActionFields(statusId);
	}

	function publicationAction(statusId) {
		var currentId = ${currentDataStructure.id};
		currentState = statusId;
		
		if(statusId == 2 || statusId == 3 ||statusId == 5) {
		
			$("#editForm").hide();
			$("#deleteForm").hide();
			$("#publishedEdits").show();
			$("#groupDraftEdits").hide();

		} else {
		
			$("#draftEdits").show();
			$("#editForm").show();
			$("#deleteForm").show();
			$("#publishedEdits").hide();
			$("#groupDraftEdits").hide();
		}
		

		if(statusId == 2) {
			$("#editForm").show();
		}
		
		//look into statusChange for method implementation
		updatePublicationInterface();
		
		$.post("dataStructureAction!publication.ajax", {
			statusId : statusId,
			dataStructureId : currentId,
		}, function(data) {
			
			document.getElementById("pubInterface").innerHTML = data;
			
			
			// If we are publishing this, then hide all the drafts
			if (statusId == 2) {
				$(".draft-class").hide();
			}
			
			if(statusId == 1) {
				confirmPublished = true;
			} else {
				confirmPublished = false;
			}
			if(confirmPublished == true){
				$(".publication-notifications").html("Your Form Structure has been submitted for publication.");
				$(".publication-notifications").fadeIn("fast");
			} else {
				$(".publication-notifications").fadeOut("fast");
			}
			
			
			reEnableLinks();
			
			var inAdmin = $('#inAdmin').val().trim();
			if(inAdmin == 'true'){
				//refreshEventLogTable();
				var dt = $("#eventTable").idtApi('getTableApi');
				//dt.draw();
				$("#eventTable").DataTable().ajax.reload();
				console.log(dt);
				//$("#eventTable").ajax.reload();
			}
			
			
		}
		);
		
	}
	
	function publicationActionFields(statusId) {
		var currentId = ${currentDataStructure.id};
		currentState = statusId;
		
		if(statusId == 2 || statusId == 3 ||statusId == 5) {
			$("#editForm").hide();
			$("#deleteForm").hide();
			$("#publishedEdits").show();
			$("#groupDraftEdits").hide();

		} else {
		
			$("#draftEdits").show();
			$("#editForm").show();
			$("#deleteForm").show();
			$("#publishedEdits").hide();
			$("#groupDraftEdits").hide();
		}
		
		if(statusId == 2) {
			$("#editForm").show();
		}
		
		//show edit button for published forms
		if (statusId == 2 ) {
			
		}
		
		$.post("dataStructureAction!publication.ajax", {
			statusId : statusId,
			dataStructureId : currentId,
		}, function(data) {
			
			document.getElementById("pubInterface").innerHTML = data;
			
			
			// If we are publishing this, then hide all the drafts
			if (statusId == 2) {
				$(".draft-class").hide();
			}
		});
	}
	
	function dataElemetReportRedcap() {
		if (confirm("REDCap CSV format is approximately 80% in alignment with the REDCap standard. \n" + 
				"Please be aware that further refinement will be required to load into the REDCap system. \n" + 
				"Click OK if you want to continue exporting.")) {
			window.location = "dataElementExportAction!exportFSDataElementsDetail.action?format=redcap";
		}
	}
</script>