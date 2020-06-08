<%@include file="/common/taglibs.jsp"%>
<c:set var="hostname" value="${pageContext.request.serverName}" />
<s:if test="%{method == 'viewLightbox'}">
	<div id="main-content">
		<h3>Dataset Information</h3>
</s:if>

<c:choose>
	<c:when test="${fn:contains(hostname, 'pdbp' )}">
		<sec:authorize access="hasAnyRole('ROLE_ADMIN', 'ROLE_REPOSITORY_ADMIN')">
		<div class="addDQButtonShow">
			<div class="right">
				<!-- Francis TODO: remove inline style -->
				<div id="addToQueueButtonWrapper" class="button" style="float: right">
					<input id="addToQueueButton" type="button" onclick="addToDownloadQueue()" value="Add to Download Queue" />
				</div>
			</div>
		</div>
		</sec:authorize>
		<sec:authorize access="!hasAnyRole('ROLE_ADMIN', 'ROLE_REPOSITORY_ADMIN')">
		<div class="addDQButtonShow">
			<div class="right">
				<!-- Francis TODO: remove inline style -->
				<div id="addToQueueButtonWrapper" class="disabled" style="float: right">
					<input id="addToQueueButton" disabled="disabled" type="button" value="Add to Download Queue" />
				</div>
			</div>
		</div>
		</sec:authorize>
	</c:when>
	<c:otherwise>
		<div class="addDQButtonShow">
			<div class="right">
				<!-- Francis TODO: remove inline style -->
				<div id="addToQueueButtonWrapper" class="button" style="float: right">
					<input id="addToQueueButton" type="button" onclick="addToDownloadQueue()" value="Add to Download Queue" />
				</div>
			</div>
		</div>
	</c:otherwise>
</c:choose>


<div class="addDQButtonDisabled">
	<div class="right">
		<div class="disabled" style="float: right">
			<input id="addToQueueButton" disabled="disabled" type="button" value="Add to Download Queue" />
		</div>
	</div>
</div>

<div class="addDQCompleteButtonDisabled">
	<div class="right">
		<div class="disabled" style="float: right">
			<input id="addToQueueButton" disabled="disabled" type="button" value="Added to Download Queue" />
		</div>
	</div>
</div>

<div class="form-output">
	<div class="label">Dataset ID:</div>
	<div class="readonly-text">
		<s:property value="currentDataset.prefixedId" />
	</div>
</div>

<div class="form-output">
	<div class="label">Name:</div>
	<div class="readonly-text">
		<s:property value="currentDataset.name" />
	</div>
</div>

<div class="form-output">

	<s:if test="%{currentDataset.isProformsSubmission}">
		<div class="label">Locked Date:</div>
	</s:if>
	<s:else>
		<div class="label">Submission Date:</div>
	</s:else>

	<div class="readonly-text">
		<ndar:dateTag value="${currentDataset.submitDate}" />
	</div>
</div>

<div class="form-output">
	<s:if test="%{currentDataset.isProformsSubmission}">
		<div class="label">Locked By:</div>
	</s:if>
	<s:else>
		<div class="label">Submitted By:</div>
	</s:else>
	
	<s:if test="%{currentDataset.isSubjectSubmitted}">
		<div class="readonly-text">
			Subject
		</div>
	</s:if>
	<s:else> 
		<div class="readonly-text">
			<s:property value="currentDataset.submitter.fullName" />
		</div>
	</s:else>
</div>

<div class="form-output">
	<div class="label">Submitted Data:</div>
	<div id="submittedDataTableContainer" class="idtTableContainer form-output" style="width: 50%;">
		<table class="display-data" id="submittedDataTable"></table>
	</div>
</div>

<div class="form-output">
	<div class="label">Submitted Form Structures:</div>
	<div id="submittedFSTableContainer" class="idtTableContainer form-output" style="width: 30%;">
		<table class="display-data" id="submittedFSTable"></table>
	</div>
</div>

<div class="form-output">
	<div class="label">Status:</div>
	<div class="readonly-text">
		<s:property value="currentDataset.getDatasetStatusWithRequestStatus" />
	</div>
</div>


<div class="form-output">
	<!-- Status Shared -->
	<div class="label">Shared Date:</div>

	<div class="readonly-text">
		<ndar:dateTag value="${currentDataset.shareDate}" />
	</div>
</div>

<c:if test="${fn:length(eventLogList) != 0}">
	<br>
	<div>
		<h3>Dataset Administrative Status Change History</h3>						
		<div id="eventTableContainer" class="idtTableContainer" style="width: 99%;">
			<table id="eventTable" class="table table-striped table-bordered"></table>
		</div>
	</div>
</c:if>


<s:if test="%{method == 'viewLightbox'}">
	 </div>
</s:if>

<script type="text/javascript">

	var url = "<s:url value='/study/datasetAction!getEventLogListOutput.ajax' />";
	<s:if test="inAdmin">
		var url = "<s:url value='/studyAdmin/datasetAction!getEventLogListOutput.ajax' />";
	</s:if>
	
		$( document ).ready(function() {
			<s:if test="(!inDownloadQueue && isReady && isDownloadable)">
				$(".addDQButtonShow").show();
				$(".addDQButtonDisabled").hide();
				$(".addDQCompleteButtonDisabled").hide();
			</s:if>
			<s:else>
				$(".addDQButtonShow").hide();
				$(".addDQButtonDisabled").show();
				$(".addDQCompleteButtonDisabled").hide();
			</s:else>
			
			$("#submittedDataTable").idtTable({
				autoWidth: false,
				dom : 'frtip',
				pageLength: 10,
				columns: [
					{
						data: "userFileName",
						title: "Data",
						name: "userFileName",
						render: IdtActions.ellipsis(50)
					},
					{
						data: "isQueryable",
						title: "Contains Queryable Data?",
						name: "isQueryable"
					}
				],
			     data: [
			      <s:iterator value="currentDataset.datasetFileSet" var="datasetFile">
		              {
		                "userFileName": "<s:property value='userFile.name' />",
		                "isQueryable": "<s:property value='isQueryable' />"
		              },
	              </s:iterator>
		      ]	
				
			});
			
			$("#submittedFSTable").idtTable({
				autoWidth: false,
				dom : 'frtip',
				pageLength: 5,
				columns: [
					{
						data: "formStrucutueName",
						title: "Form Structure",
						name: "formStrucutueName"
					}
				],
			     data: [
			      <s:iterator value="associatedDataStructure" var="dataStructure">
		              {
		                "formStrucutueName": "<s:property value='dataStructure' />"
		              },
	              </s:iterator>
		      ]	
			});
			
			$("#eventTable").idtTable({
				idtUrl: url,
				autoWidth: false,
				dom : 'frtip',
				pageLength: 10,
				"columns": [
					{
						"data": "createTime",
						"title": "Date",
						"name": "createTime",
						"parameter": "createTime",
						"render": IdtActions.formatDate()
					},
					{
						"data": "user",
						"title": "User",
						"name": "user",
						"parameter": "user.fullName"
					},
					{
						"data": "actionTaken",
						"title": "Action Taken",
						"name": "actionTaken",
						"parameter": "actionTaken"
					},
					{
						"data": "comment",
						"title": "Reason Given",
						"name": "comment",
						"parameter": "comment",
						"render": IdtActions.ellipsis(100)
					},
					{
						"data": "docNameLink",
						"title": "Attachments",
						"name": "docNameLink",
						"parameter": "docNameLink"
					}
				]
			});
			
		});
	
	
	function addToDownloadQueue() {
		var url = window.location.pathname.split("/");
		var namespace = url[2];
		if(namespace=='guid' || namespace=='guidAdmin'){
			completeUrl="portal/study/datasetAction!addToDownloadQueue.ajax";
		}
		if (${currentDataset.datasetStatus.id} == 2)
		{
			if (!confirm("This dataset is archived, which means the data may no longer be valid or relevant. Do you wish to continue?"))
			{
				return;
			}
		}
		if(namespace=='guid' || namespace=='guidAdmin'){
			url="/../../portal/study/datasetAction!addToDownloadQueue.ajax";
		}else{
			url="datasetAction!addToDownloadQueue.ajax";
		}
		
	
		
		$(".addDQButtonDisabled").show();
		$(".addDQButtonShow").hide();
		$(".addDQCompleteButtonDisabled").hide();
		
		$.post(	url, 
			{ }, 
			function (data) {
				$(".addDQButtonDisabled").hide();
				$(".addDQButtonShow").hide();
				$(".addDQCompleteButtonDisabled").show();
				
			}
		);
	}
</script>