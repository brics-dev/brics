<%@include file="/common/taglibs.jsp"%>
<%@page import="gov.nih.tbi.PortalConstants"%>

<div class="form-output">
	<div class="label">Title : </div>
	<div class="readonly-text"><s:property value="currentStudy.title" /></div>
</div>

<%-- This if statement is for Josh's support of the CNRM dashboard --%>
<%--
<c:if test="${fn:contains(pageContext.request.requestURL, 'cnrm' )}">
	<div class="form-output">
		<div class="label">Study Dashboard:</div>
		<div class="readonly-text">
			<a href="/tbidashboard/dashboard?studyTitle=<s:property value='currentStudy.title' />" target="_blank">View Study Dashboard</a> 
		</div>
	</div>
</c:if>
--%>

<div class="form-output">
	<div class="label">Study ID : </div>
	<div class="readonly-text"><s:property value="currentStudy.prefixedId" /></div>
</div>

<div class="form-output">
	<div class="label">Study ID Source : </div>
	<div class="readonly-text"><%=PortalConstants.BRICS_SYSTEM_GENERATED%></div>
</div>

<s:if test="%{isDoiEnabled}">
	<div class="form-output">
		<div class="label">Study DOI : </div>
		<div id="studyDoiDisplay" class="readonly-text">
			<s:if test="%{currentStudy.doi != null && !currentStudy.doi.isEmpty()}">
				<a href="<s:property value="getDoiResolverUrl()" /><s:property value="currentStudy.doi" />">
					<s:property value="currentStudy.doi" />
				</a>
			</s:if>
		</div>
	</div>
	
	<div class="form-output">
		<div class="label">Study DOI Source : </div>
		<div id="studyDoiSourceDisplay" class="readonly-text">
			<s:if test="%{currentStudy.doi != null && !currentStudy.doi.isEmpty()}">
				DataCite
			</s:if>
		</div>
	</div>
</s:if>

<s:if test="%{isPrivate && !inAdmin}">
	<div class="form-output">
		<div class="label">Visibility : </div>
		<div class="readonly-text">Private</div>
	</div>
</s:if>
<div id="visibility" class="form-output">
	<div class="label">Visibility : </div>
	<div id="publicStatus" class="visibilityDisplay">
		<div class="readonly-text">Public</div>
	</div>
	<div id="privateStatus" class="visibilityDisplay">
		<div class="readonly-text">Private</div>
		<div class="special-instruction">This study has been marked as private. Studies that have been marked as private
			are only visible to the groups listed in on the permission page.</div>
	</div>
	<div id="rejectedStatus" class="visibilityDisplay">
		<div class="readonly-text">Rejected</div>
		<div class="special-instruction">This study has been rejected and pending user updates. It is not recommended to
			edit the study while it has been rejected.</div>
	</div>
	<div id="requestedStatus" class="visibilityDisplay">
		<div class="readonly-text">New Study Request</div>
		<div class="special-instruction">This is a new study request. This study is waiting for an Admin to approve or
			reject this study.</div>
	</div>
</div>

<div class="form-output">
	<div class="label">Recruitment Status : </div>
	<div class="readonly-text">
		<s:property value="currentStudy.recruitmentStatus.name" />
	</div>
</div>

<div class="form-output">
	<div class="label">Study Type : </div>
	<div class="readonly-text">
		<s:property value="currentStudy.studyType.name" />
	</div>
</div>

<div class="form-output">
	<div class="label">Study URL : </div>
	<div class="readonly-text">
		<s:property value="currentStudy.studyUrl" />
	</div>
</div>

<div class="form-output">
	<div class="label" style="white-space:nowrap;">Therapeutic Agents:</div>
	<div class="readonly-text">
		<c:forEach var="therapeuticAgentVal" items="${therapeuticAgentSet}">
			<c:out value="${therapeuticAgentVal.text}"/>  &nbsp; &nbsp;
		</c:forEach>
	</div>
</div>

<div class="form-output">
	<div class="label" style="white-space:nowrap;">Therapy Types:</div>
	<div class="readonly-text">
		<c:forEach var="therapyTypeVal" items="${therapyTypeSet}">
			<c:out value="${therapyTypeVal.text}"/>  &nbsp; &nbsp;
		</c:forEach>
	</div>
</div>


<div class="form-output">
	<div class="label" style="white-space:nowrap;">Therapeutic Targets:</div>
	<div class="readonly-text">
		<c:forEach var="therapeuticTargetVal" items="${therapeuticTargetSet}">
			<c:out value="${therapeuticTargetVal.text}"/> &nbsp; &nbsp;
		</c:forEach>
	</div>
</div>

<div class="form-output">
	<div class="label" style="white-space:nowrap;">Model Names:</div>
	<div class="readonly-text">
		<c:forEach var="modelNameVal" items="${modelNameSet}">
			<c:out value="${modelNameVal.text}"/> &nbsp; &nbsp;
		</c:forEach>
	</div>
</div>

<div class="form-output">
	<div class="label" style="white-space:nowrap;">Model Types:</div>
	<div class="readonly-text">
		<c:forEach var="modelTypeVal" items="${modelTypeSet}"> 
			<c:out value="${modelTypeVal.text}"/> &nbsp; &nbsp;
		</c:forEach>
	</div>
</div>



<div class="form-output">
	<div class="label">Abstract : </div>
	<div class="readonly-text">
		<s:property value="currentStudy.abstractText" />
	</div>
</div>

<div class="form-output">
	<div class="label">Study Aims : </div>
	<div class="readonly-text">
		<s:property value="currentStudy.goals" />
	</div>
</div>

<c:if test="${not empty currentStudy.sponsorInfoSet}"> 
	<div class="form-output">
		<div class="label">FDA Clinical Trial Table : </div>
	<div id="sponsorInfoTable" class="readonly-text idtTableContainer" style="width:600px">
		<div id="dialog"></div>
		<table id="sponsorInfoTableTable" class="table table-striped table-bordered" width="100%"></table>
	</div>
	<script type="text/javascript">
		$(document).ready(function() {
			$('#sponsorInfoTableTable').idtTable({
				idtUrl: "<s:url value='/study/studyAction!getSponsorInfoSet.action' />",
				"columns": [
					{
						"data": "fdaInd",
						"title": "FDA IND/IDE",
						"name": "FDA IND/IDE",
						"parameter": "fdaInd"
					}, 
					{
						"data": "sponsor",
						"title": "SPONSOR",
						"name": "SPONSOR",
						"parameter": "sponsor"
					}
				]
			});
		});
	</script>

	</div>
</c:if> 

<c:if test="${not empty currentStudy.clinicalTrialSet}">
	<div class="form-output">
		<div class="label">Clinical Trial ID(s) :</div>
		<div class="readonly-text">
			<c:forEach var="clinicalId" items="${currentStudy.clinicalTrialSet}">
				<a href="javascript:viewClinicalTrial('${clinicalId.clinicalTrialId}')">${clinicalId.clinicalTrialId}</a>&nbsp;&nbsp;
			</c:forEach>
		</div>
	</div>
</c:if>

<c:if test="${not empty currentStudy.grantSet}">
	<div class="form-output">
		<div class="label" style="white-space:nowrap;">Grant/Project ID(s) :</div>
		<div class="readonly-text">
			<c:forEach var="grant" items="${currentStudy.grantSet}">
				<a href="javascript:viewGrantInfo('${grant.grantId}')" ><c:out value="${grant.grantId}" /></a>&nbsp;&nbsp;
			</c:forEach>
		</div>
	</div>
</c:if>

<div class="form-output">
	<div class="label">Start Date : </div>
	<div class="readonly-text"><s:property value="currentStudy.startDate" /></div>
</div>

<div class="form-output">
	<div class="label">End Date : </div>
	<div class="readonly-text"><s:property value="currentStudy.endDate" /></div>
</div>

<div class="form-output">
	<div class="label">Duration : </div>
	<div class="readonly-text"><s:property value="currentStudy.studyDuration" /></div>
</div>

<div class="form-output">
	<div class="label">Primary Funding Source : </div>
	<div class="readonly-text"><s:property value="currentStudy.fundingSource.name" /></div>
</div>

<div class="form-output">
	<div class="label">Estimated Number of Subjects : </div>
	<div class="readonly-text"><s:property value="currentStudy.numberOfSubjects" /></div>
</div>

<div class="form-output">
	<div class="label">Study Research Management : </div>
	<div id="resMgmtTable" class="readonly-text idtTableContainer"  style="width:700px">
		<div id="dialog"></div>
		<table id="resMgmtTableTable" class="table table-striped table-bordered" width="100%"></table>
	</div>

	<script type="text/javascript">
	$(document).ready(function() {
		$('#resMgmtTableTable').idtTable({
			idtUrl: "<s:url value='/study/studyAction!getResearchMgmtSet.action' />",
			"columns": [
				{
					"data": "title",
					"title": "TITLE",
					"name": "TITLE",
					"parameter": "roleTitle"
				},
				{
					"data": "fullName",
					"title": "FULL NAME",
					"name": "FULL NAME",
					"parameter": "fullName"
				}, 
				{
					"data": "email",
					"title": "E-MAIL",
					"name": "E-MAIL",
					"parameter": "email"
				},
				{
					"data": "orgName",
					"title": "ORGANIZATION",
					"name": "ORGANIZATION",
					"parameter": "orgName"
				}
			]
		});
	});
	</script>
</div>

<div class="form-output">
	<div class="label">Study Site : </div>
	<div id="studySiteTable" class="readonly-text idtTableContainer"  style="width:700px">
		<div id="dialog"></div>
		<table id="studySiteTableTable" class="table table-striped table-bordered" width="100%"></table>
	</div>
	<script type="text/javascript">
		$(document).ready(function() {
			$('#studySiteTableTable').idtTable({
				idtUrl: "<s:url value='/study/studyAction!getStudySiteSet.action' />",
				"columns": [
					{
						"data": "siteName",
						"title": "SITE NAME",
						"name": "SITE NAME",
						"parameter": "siteName"
					},
					{
						"data": "addressLine",
						"title": "ADDRESS",
						"name": "ADDRESS",
						"parameter": "addressLine"
					}, 
					{
						"data": "city",
						"title": "CITY",
						"name": "CITY",
						"parameter": "address.city"
					},
					{
						"data": "state",
						"title": "STATE",
						"name": "STATE",
						"parameter": "state"
					}, 
					{
						"data": "country",
						"title": "COUNTRY",
						"name": "COUNTRY",
						"parameter": "country"
					}, 
					{
						"data": "phoneNumber",
						"title": "PHONE NUMBER",
						"name": "PHONE NUMBER",
						"parameter": "phone"
					}
				]
			});
		});
	</script>
</div>
<s:if test="%{assoPfProtoVisible}">
<div class="form-output" id="studyPfProtocolDiv">
	<div class="label">Study Associated Proforms Protocols : </div>
	<div id="studyPfProtocolTableContainer" class="readonly-text idtTableContainer"  style="width:700px">
		<div id="dialog"></div>
		<table id="studyPfProtocolTable" class="table table-striped table-bordered" width="100%"></table>
	</div>
	<script type="text/javascript">
		$(document).ready(function() {
			$('#studyPfProtocolTable').idtTable({
				idtUrl: "<s:url value='/study/viewStudyAction!getStudyPfProtocolList.action' />",
				"columns": [
					{
						"data": "protocolName",
						"title": "Protocol Name",
						"name": "Protocol Name",
						"parameter": "protocolName"
					},
					{
						"data": "protocolNumber",
						"title": "Protocol Number",
						"name": "Protocol Number",
						"parameter": "protocolNumber"
					}, 
					{
						"data": "protoESignature",
						"title": "eSignature",
						"name": "eSignature",
						"parameter": "protoESignature"
					},
					{
						"data": "closedByFullName",
						"title": "Closed By",
						"name": "Closed By",
						"parameter": "closedByFullName"
					},
					{
						"data": "closingOutDate",
						"title": "Close Date",
						"name": "Close Date",
						"parameter": "closingOutDate"
					}
				]
			});
		});
	</script>
</div>
</s:if>
<script type="text/javascript">
	$('document').ready(function() { 
		$(".visibilityDisplay").hide();
		displayVisibility('${currentStudy.studyStatus.name}');
	});
	
	function changeVisibility(visibility) {
		
		if (visibility != null) {
			$.post(	"viewStudyAction!changeVisibility.ajax", 
					{ visibility:visibility }, 
					function (data) {
						visibility = data;
					}
				);
			
			displayVisibility(visibility);
		}
	}
	
	function displayVisibility(visibility) {
		
		$(".visibilityDisplay").fadeOut("fast");
		if(visibility == "Private") {
			$("#deleteButton").show();
			$("#privateStatus").fadeIn("fast");
		} else if(visibility == "Public") {
			$("#deleteButton").hide();
			$("#publicStatus").fadeIn("fast");
		} else if(visibility == "Requested") {
			$("#requestedStatus").fadeIn("fast");
		} else if(visibility == "Rejected") {
			$("#rejectedStatus").fadeIn("fast");
		}
	}
	
	function viewGrantInfo(grantId) {
		
	    var f = $("<form target='_blank' method='POST' style='display:none;'></form>").attr({
	        action: "<s:property value='@gov.nih.tbi.PortalConstants@FEDERAL_REPORTER_SEARCH_URL' />"
	    }).appendTo(document.body);

	    $('<input type="hidden" />').attr({
            name: "projectNumbers", value: grantId
        }).appendTo(f);
	    
	    $('<input type="hidden" />').attr({
            name: "projectNumbersRaw", value: grantId
        }).appendTo(f);
	    
	    $('<input type="hidden" />').attr({
            name: "searchMode", value: "Smart"
        }).appendTo(f);
	    
	    f.submit();
	    f.remove();
	}
	
</script>