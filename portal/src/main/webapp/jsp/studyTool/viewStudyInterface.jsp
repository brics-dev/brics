<%@include file="/common/taglibs.jsp"%>
<%@page import="gov.nih.tbi.PortalConstants"%>

<div class="form-output">
	<div class="label">Title : </div>
	<div class="readonly-text"><s:property value="currentLazyStudy.title" /></div>
</div>

<%-- This if statement is for Josh's support of the CNRM dashboard --%>
<%--
<c:if test="${fn:contains(pageContext.request.requestURL, 'cnrm' )}">
	<div class="form-output">
		<div class="label">Study Dashboard:</div>
		<div class="readonly-text">
			<a href="/tbidashboard/dashboard?studyTitle=<s:property value='currentLazyStudy.title' />" target="_blank">View Study Dashboard</a> 
		</div>
	</div>
</c:if>
--%>

<div class="form-output">
	<div class="label">Study ID : </div>
	<div class="readonly-text"><s:property value="currentLazyStudy.prefixedId" /></div>
</div>

<div class="form-output">
	<div class="label">Study ID Source : </div>
	<div class="readonly-text"><%=PortalConstants.BRICS_SYSTEM_GENERATED%></div>
</div>

<s:if test="%{isDoiEnabled}">
	<div class="form-output">
		<div class="label">Study DOI : </div>
		<div id="studyDoiDisplay" class="readonly-text">
			<s:if test="%{currentLazyStudy.doi != null && !currentLazyStudy.doi.isEmpty()}">
				<a href="<s:property value="getDoiResolverUrl()" /><s:property value="currentLazyStudy.doi" />">
					<s:property value="currentLazyStudy.doi" />
				</a>
			</s:if>
		</div>
	</div>
	
	<div class="form-output">
		<div class="label">Study DOI Source : </div>
		<div id="studyDoiSourceDisplay" class="readonly-text">
			<s:if test="%{currentLazyStudy.doi != null && !currentLazyStudy.doi.isEmpty()}">
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
		<s:property value="currentLazyStudy.recruitmentStatus.name" />
	</div>
</div>

<div class="form-output">
	<div class="label">Study Type : </div>
	<div class="readonly-text">
		<s:property value="currentLazyStudy.studyType.name" />
	</div>
</div>

<div class="form-output">
	<div class="label">Study URL : </div>
	<div class="readonly-text">
		<s:property value="currentLazyStudy.studyUrl" />
	</div>
</div>

<div class="form-output">
	<div class="label">Abstract : </div>
	<div class="readonly-text">
		<s:property value="currentLazyStudy.abstractText" />
	</div>
</div>

<div class="form-output">
	<div class="label">Study Aims : </div>
	<div class="readonly-text">
		<s:property value="currentLazyStudy.goals" />
	</div>
</div>

<c:if test="${not empty currentLazyStudy.sponsorInfoSet}"> 
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

<c:if test="${not empty currentLazyStudy.clinicalTrialSet}">
	<div class="form-output">
		<div class="label">Clinical Trial ID(s) :</div>
		<div class="readonly-text">
			<c:forEach var="clinicalId" items="${currentLazyStudy.clinicalTrialSet}">
				<a href="javascript:viewClinicalTrial('${clinicalId.clinicalTrialId}')">${clinicalId.clinicalTrialId}</a>&nbsp;&nbsp;
			</c:forEach>
		</div>
	</div>
</c:if>

<c:if test="${not empty currentLazyStudy.grantSet}">
	<div class="form-output">
		<div class="label" style="white-space:nowrap;">Grant/Project ID(s) :</div>
		<div class="readonly-text">
			<c:forEach var="grant" items="${currentLazyStudy.grantSet}">
				<a href="javascript:viewGrantInfo('${grant.grantId}')" ><c:out value="${grant.grantId}" /></a>&nbsp;&nbsp;
			</c:forEach>
		</div>
	</div>
</c:if>

<div class="form-output">
	<div class="label">Start Date : </div>
	<div class="readonly-text"><s:property value="currentLazyStudy.startDate" /></div>
</div>

<div class="form-output">
	<div class="label">End Date : </div>
	<div class="readonly-text"><s:property value="currentLazyStudy.endDate" /></div>
</div>

<div class="form-output">
	<div class="label">Duration : </div>
	<div class="readonly-text"><s:property value="currentLazyStudy.studyDuration" /></div>
</div>

<div class="form-output">
	<div class="label">Primary Funding Source : </div>
	<div class="readonly-text"><s:property value="currentLazyStudy.fundingSource.name" /></div>
</div>

<div class="form-output">
	<div class="label">Estimated Number of Subjects : </div>
	<div class="readonly-text"><s:property value="currentLazyStudy.numberOfSubjects" /></div>
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
					"parameter": "role.name"
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
				},
				{
                    "data": "orcId",
                    "title": "ORCID",
                    "name": "ORCID",
                    "parameter": "orcId"

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

<script type="text/javascript">
	$('document').ready(function() { 
		$(".visibilityDisplay").hide();
		displayVisibility('${currentLazyStudy.studyStatus.name}');
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