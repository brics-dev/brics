<%@include file="/common/taglibs.jsp"%>
<title>Edit Meta Study</title>

<script type="text/javascript"
	src='/portal/js/metastudy/savedQueryViewEngine.js'></script>
<!-- 
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
 -->


<link
	href="https://cdnjs.cloudflare.com/ajax/libs/select2/4.0.6-rc.0/css/select2.min.css"
	rel="stylesheet" />
<script
	src="https://cdnjs.cloudflare.com/ajax/libs/select2/4.0.6-rc.0/js/select2.min.js"></script>

<!-- begin .border-wrapper -->

<style>
.select2-selection__rendered {
    padding-right: 30px !important;
}

.select2-selection__rendered:after {
    content: "";
    position: absolute;
    right: 10px;
    top: 50%;
    transform: translateY(-50%);
    border-top: 5px solid #333;
    border-left: 5px solid transparent;
    border-right: 5px solid transparent;
}

#main-content ul.select2-selection__choice, #main-content li.select2-selection__choice {
    margin: .25em 0;
    margin-top: 0.25em;
    margin-right: 5px;
    margin-bottom: 0.25em;
    margin-left: 0px;
}

#main-content ol.select2-selection__rendered, #main-content ul.select2-selection__rendered {
    margin: 0 0 0 0;
}

</style>
<div class="border-wrapper">
	<jsp:include page="../navigation/metaStudyNavigation.jsp" />
	<h1 class="float-left">Meta Study</h1>
	<div style="clear: both"></div>

	<!--begin #center-content -->
	<div id="main-content">
		<s:form id="metaStudyDetailsForm" cssClass="validate" method="post"
			name="metaStudyDetailsForm" validate="true"
			enctype="multipart/form-data">
			<h2>Edit Meta Study</h2>
			<h3 class="metaStudyHeader">
				<s:property value="currentMetaStudy.title" />
			</h3>
			<!--  This ends the details form. Other form data will be submitted in separate processes -->

			<h3 id="detailsLabel" class="clear-both collapsable">
				<span id="detailsLabelPlusMinus"></span>&nbsp;Details
			</h3>
			<div id="details">
				<div id="metaStudyDetails">
					<s:token />

					<s:if test="hasActionErrors()">
						<div class="error-message">
							<s:actionerror />
						</div>
					</s:if>
					<div class="clear-right"></div>
					<input type="hidden" name="metaStudyDetailsForm.id"
						value="${metaStudyDetailsForm.id}" /> 
						
					<!-- <input type="hidden"
						name="metaStudyDetailsForm.studyType.id"
						value="${metaStudyDetailsForm.studyType.id}" /> -->

					<div class="form-field">
						<label for="metaStudyDetailsForm.title" class="required">Title
							<span class="required">* </span>
						</label>
						<s:textfield id="metaStudyDetailsForm.title"
							name="metaStudyDetailsForm.title" cssClass="textfield required"
							escapeHtml="true" escapeJavaScript="true" />
						<s:fielderror fieldName="metaStudyDetailsForm.title" />
					</div>

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

					<div class="form-field">
						<label for="metaStudyDetailsForm.recruitmentStatus">Recruitment
							Status :</label>
						<s:select id="metaStudyDetailsForm.recruitmentStatus"
							list="recruitmentStatuses" listKey="id" listValue="name"
							name="metaStudyDetailsForm.recruitmentStatus"
							value="metaStudyDetailsForm.recruitmentStatus.id" />
						<s:fielderror fieldName="metaStudyDetailsForm.recruitmentStatus" />
					</div>

					<div class="form-field">
						<label for="metaStudyDetailsForm.studyType">Study Type <span
							class="required">* </span>:
						</label>
						<s:select id="metaStudyDetailsForm.studyType"
							list="metaStudyTypes" listKey="id" listValue="name"
							name="metaStudyDetailsForm.studyType"
							value="metaStudyDetailsForm.studyType.id" />
						<s:fielderror fieldName="metaStudyDetailsForm.studyType" />
					</div>
					
					<div class="form-field">
					<label for="therapeuticAgent" class="required">Therapeutic
						Agent :</label> <select class="therapeuticAgentSelect"
						name="metaStudyDetailsForm.therapeuticAgentSelect" multiple="multiple">
					
						<c:forEach var="tAgent" items="${allTherapeuticAgents}">
							<s:set var="selected" value="false" />
							<c:forEach var="selectedTAgent" items="${metaStudyDetailsForm.therapeuticAgentSet}">
								<c:if test="${ tAgent.id == selectedTAgent.therapeuticAgentId }">
									<s:set var="selected" value="true" />
								</c:if>
							</c:forEach>
							<c:choose>
								<c:when test="${selected == true}">
									<option selected="selected" value="${tAgent.id}">${tAgent.text}</option>
								</c:when>
								<c:otherwise>
									<option value="${tAgent.id}">${tAgent.text}</option>
								</c:otherwise>
							</c:choose>
						</c:forEach>
					</select>
				</div>

				<div class="form-field">
					<label for="therapyType" class="required">Therapy Type :</label> <select
						class="therapyTypeSelect" name="metaStudyDetailsForm.therapyTypeSelect"
						multiple="multiple">
						
						<c:forEach var="tType" items="${allTherapyTypes}">
							<s:set var="selected" value="false" />
							<c:forEach var="selectedTType" items="${metaStudyDetailsForm.therapyTypeSet}">
								<c:if test="${ tType.id == selectedTType.therapyTypeId }">
									<s:set var="selected" value="true" />
								</c:if>
							</c:forEach>
							<c:choose>
								<c:when test="${selected == true}">
									<option selected="selected" value="${tType.id}">${tType.text}</option>
								</c:when>
								<c:otherwise>
									<option value="${tType.id}">${tType.text}</option>
								</c:otherwise>
							</c:choose>
						</c:forEach>
					</select>
				</div>

				<div class="form-field">
					<label for="therapeuticTarget" class="required">Therapeutic
						Target :</label> <select class="therapeuticTargetSelect"
						name="metaStudyDetailsForm.therapeuticTargetSelect" multiple="multiple">
						
						<c:forEach var="tTarget" items="${allTherapeuticTargets}">
							<s:set var="selected" value="false" />
							<c:forEach var="selectedTTarget" items="${metaStudyDetailsForm.therapeuticTargetSet}">
								<c:if test="${ tTarget.id == selectedTTarget.therapeuticTargetId }">
									<s:set var="selected" value="true" />
								</c:if>
							</c:forEach>
							<c:choose>
								<c:when test="${selected == true}">
									<option selected="selected" value="${tTarget.id}">${tTarget.text}</option>
								</c:when>
								<c:otherwise>
									<option value="${tTarget.id}">${tTarget.text}</option>
								</c:otherwise>
							</c:choose>
						</c:forEach>
					</select>
				</div>

				<div class="form-field">
					<label for="modelName" class="required">Model Name :</label> <select
						class="modelNameSelect" name="metaStudyDetailsForm.modelNameSelect" multiple="multiple">
						
						<c:forEach var="mName" items="${allModelNames}">
							<s:set var="selected" value="false" />
							<c:forEach var="selectedMName" items="${metaStudyDetailsForm.modelNameSet}">
								<c:if test="${ mName.id == selectedMName.modelNameId }">
									<s:set var="selected" value="true" />
								</c:if>
							</c:forEach>
							<c:choose>
								<c:when test="${selected == true}">
									<option selected="selected" value="${mName.id}">${mName.text}</option>
								</c:when>
								<c:otherwise>
									<option value="${mName.id}">${mName.text}</option>
								</c:otherwise>
							</c:choose>
						</c:forEach>
					</select>
				</div>

				<div class="form-field">
					<label for="modelType" class="required">Model Type :</label> <select
						class="modelTypeSelect" name="metaStudyDetailsForm.modelTypeSelect" multiple="multiple">
						
						<c:forEach var="mType" items="${allModelTypes}">
							<s:set var="selected" value="false" />
							<c:forEach var="selectedMType" items="${metaStudyDetailsForm.modelTypeSet}">
								<c:if test="${ mType.id == selectedMType.modelTypeId }">
									<s:set var="selected" value="true" />
								</c:if>
							</c:forEach>
							<c:choose>
								<c:when test="${selected == true}">
									<option selected="selected" value="${mType.id}">${mType.text}</option>
								</c:when>
								<c:otherwise>
									<option value="${mType.id}">${mType.text}</option>
								</c:otherwise>
							</c:choose>
						</c:forEach>
					</select>
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

					<s:if test="getIsPublished()">
						<div class="form-output">
							<div class="label">DOI</div>
							<div class="readonly-text">
								<s:property value="currentMetaStudy.doi" />
							</div>
						</div>

						<div class="form-output">
							<div class="label">DOI Scheme</div>
							<div class="readonly-text">FILL IN</div>
						</div>
					</s:if>

					<div class="form-field">
						<label for="metaStudyDetailsForm.abstractText" class="required">Abstract
							<span class="required">* </span>
						</label>
						<s:textarea label="metaStudyDetailsForm.abstractText" cols="60"
							rows="10" cssClass="textfield required"
							name="metaStudyDetailsForm.abstractText" escapeHtml="true"
							escapeJavaScript="true" />
						<s:fielderror fieldName="metaStudyDetailsForm.abstractText" />
					</div>
					<div class="form-field">
						<label for="metaStudyDetailsForm.aimsText">Aims </label>
						<s:textarea label="metaStudyDetailsForm.aimsText" cols="60"
							rows="10" cssClass="textfield"
							name="metaStudyDetailsForm.aimsText" escapeHtml="true"
							escapeJavaScript="true" />
					</div>


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

					<div class="form-field">
						<label for="metaStudyDetailsForm.aimsText">Study URL </label>
						<s:textfield id="metaStudyDetailsForm.studyUrl"
							name="metaStudyDetailsForm.studyUrl" cssClass="textfield"
							escapeHtml="true" escapeJavaScript="true" />
						<s:fielderror fieldName="metaStudyDetailsForm.studyUrl" />


					</div>
					<br />
				</div>
				<br />
			</div>
			<!--  This ends the details form. Other form data will be submitted in separate processes -->

			<h3 id="studyResearchMgmtLabel" class="clear-both collapsable">
				<span id="studyResearchMgmtLabelPlusMinus"></span>&nbsp;Study
				Research Management
			</h3>
			<div id="researchMgmt">
				<jsp:include page="editMetaStudyResearchMgmt.jsp" />
			</div>
			<br />
			<h3 id="studyInformationLabel" class="clear-both collapsable">
				<span id="studyInformationLabelPlusMinus"></span>&nbsp;Study
				Information
			</h3>
			<div id="studyInformation">
				<jsp:include page="editMetaStudyInfo.jsp" />
			</div>
			<br />
			<h3 id="documentationLabel" class="clear-both collapsable">
				<span id="documentationLabelPlusMinus"></span>&nbsp;Documentation
			</h3>
			<div id="documentation">
				<jsp:include page="metaStudyDocumentationTable-Edit.jsp" />
			</div>
			<br />

			<h3 id="dataLabel" class="clear-both collapsable">
				<span id="dataLabelPlusMinus"></span>&nbsp;Data
			</h3>
			<div id="data">
				<jsp:include page="metaStudyDataTable-Edit.jsp" />
			</div>
			<br />

			<h3 id="keywordLabel" class="clear-both collapsable">
				<span id="keywordLabelPlusMinus"></span>&nbsp;Keywords and Labels
			</h3>
			<div id="keyword"></div>
			<div class="form-field inline-left-button">
				<div class="button" style="margin-left: 10px;">
					<input type="button" value="Save" onclick="javascript:save()" />
				</div>
				<a class="form-link"
					href="/portal/metastudy/metaStudyAction!view.action?metaStudyId=${currentMetaStudy.id}">Cancel</a>
			</div>
			<div class="ibisMessaging-dialogContainer"></div>
		</s:form>
	</div>
</div>

<script type="text/javascript" src="/portal/js/metastudy/metaStudy.js"></script>
<script type="text/javascript">

	setNavigation({"bodyClass":"primary", "navigationLinkID":"metaStudyModuleLink", "subnavigationLinkID":"metaStudyLink", "tertiaryLinkID":"none"});
		
	$('document').ready(function() { 
		console.log("load dis");
		$.ajax({
			type: "GET",
			cache: false,
			url: "metaStudyAction!keywords.ajax",
			success: function(data) {
				$("#keyword").html(data);
			}
		});
		
		$("#detailsLabelPlusMinus").text("-");
		$("#studyResearchMgmtLabelPlusMinus").text("-");
		$("#studyInformationLabelPlusMinus").text("-");
		$("#documentationLabelPlusMinus").text("-");
		$("#dataLabelPlusMinus").text("-");
		$("#keywordLabelPlusMinus").text("-");

		detailsInit();	
		studyResearchMgmtInit();
		studyInformationInit();
		documentationInit();
		dataInit();
		keywordLabelInit();
		initAutoSelect();
		
	});
	
	$("#addDocBtn").click( function(e) {
		$("#selectAddDocDiv").toggle();
	});
	
	$("#addDataBtn").click( function(e) {
		$("#selectAddDataDiv").toggle();
	});

	function initAutoSelect() {
		$(".therapeuticAgentSelect").select2();
	    $(".therapyTypeSelect").select2();
	    $(".therapeuticTargetSelect").select2();
	    $(".modelNameSelect").select2();
	    $(".modelTypeSelect").select2();
	}
	
	function save(){
		$.ajax({
			type: "POST",
			cache: false,
			url: "metaStudyEditValidationAction!editDetailsSave.ajax",
			data: $('#metaStudyDetailsForm').serialize(),
			success: function(data) {
				if (data == "success")
				{
					selectAllCurrentFields();			
					var theForm = document.getElementById('metaStudyKeywordForm');
					//need to submit the keyword form and redirect the window to the submit function
					theForm.action = 'metaStudyAction!submit.action';
					theForm.submit();
				}
				else
				{
					object = $('<div/>').html(data).contents();
		            var details = object.find('#metaStudyDetails').html();
		            $('#metaStudyDetails').html(details);
		  		    $("html, body").animate({ scrollTop: 0 }, "fast");
		  		  	initAutoSelect();
				}
			}
		});
	}
	
	function saveDetailsForm(){
		$.ajax({
			type: "POST",
			cache: false,
			url: "metaStudyEditValidationAction!editDetailsSave.ajax",
			data: $('#metaStudyDetailsForm').serialize(),
			success: function(data) {
					object = $('<div/>').html(data).contents();
		            var details = object.find('#metaStudyDetails').html();
		            $('#metaStudyDetails').html(details);
		  		    $("html, body").animate({ scrollTop: 0 }, "fast");
			}
		});
	}
	
	function selectAllCurrentFields() {
		selectAllCurrentKeywords(); 
		
		<s:if test="isMetaStudyAdmin">
			selectAllCurrentLabels();
		</s:if>
	}
	
	function saveKeywordForm(){
		selectAllCurrentKeywords(); 
		
		<s:if test="isAdmin">
			selectAllCurrentLabels();
		</s:if>
		
		$.ajax({
			type: "POST",
			cache: false,
			url: "metaStudyAction!editKeywordSave.ajax",
			data: $('#metaStudyKeywordForm').serialize(),
			success: function(data) {
				$("#keyword").html(data);
			}
		});
	}

	function submitForm(action) {
		var theForm = document.getElementById('theForm');
		theForm.action = action;
		theForm.submit();
	}
</script>