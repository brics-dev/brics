<%@include file="/common/taglibs.jsp"%>
<link
	href="https://cdnjs.cloudflare.com/ajax/libs/select2/4.0.6-rc.0/css/select2.min.css"
	rel="stylesheet" />
<script
	src="https://cdnjs.cloudflare.com/ajax/libs/select2/4.0.6-rc.0/js/select2.min.js"></script>

<title>Create Meta Study</title>


<style>
.remainder-message {
	font-size: 12px;
	clear: both;
	margin-left: 10px;
}

#researchMgmtError .error-message, #researchMgmtError .error-text {
	width: 500px;
}

#researchMgmtError .error-text {
	float: none;
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
</style>
<!-- begin .border-wrapper -->
<div class="border-wrapper wide">
	<jsp:include page="../navigation/metaStudyNavigation.jsp" />
	<h1 class="float-left">Meta Study</h1>
	<div style="clear: both"></div>

	<!--begin #center-content -->
	<div id="main-content">
		<h2>Create Meta Study</h2>

		<s:form id="theForm" cssClass="validate" method="post"
			name="metaStudyDetailsForm" validate="true"
			enctype="multipart/form-data">
			<s:token />

			<ndar:editMetaStudyChevron action="metaStudyValidationAction"
				chevron="Details" />

			<s:if test="hasActionErrors()">
				<div class="error-message">
					<s:actionerror />
				</div>
			</s:if>

			<h3>Details</h3>
			<p class="required">
				Fields marked with a <span class="required">* </span>are required.
			</p>

			<h3>Create Study</h3>
			<p>Please enter your study information below. Once you've
				completed the mandatory fields, click the submit button. Your study
				request will be reviewed by a system administrator. Once the system
				administrator approves your request, you can start submitting data
				to the study.</p>
			<p class="required">
				Fields marked with a <span class="required">* </span>are
				required.You will not be able to submit your study for approval
				until all required fields are answered.
			</p>
			<h3 id="studyOverViewLabel" class="clear-both collapsable">
				<span id="studyOverViewLabelPlusMinus"></span>&nbsp;Study Overview
			</h3>
			<div id="studyOverView">


				<div class="clear-right"></div>
				<input type="hidden" name="metaStudyDetailsForm.id"
					value="${metaStudyDetailsForm.id}" />

				<div class="form-field">
					<label for="metaStudyDetailsForm.title" class="required">Title
						<span class="required">* </span>
					</label>
					<s:textfield id="metaStudyDetailsForm.title"
						name="metaStudyDetailsForm.title" cssClass="textfield required"
						cssStyle="float:left" escapeHtml="true" escapeJavaScript="true" />
					<s:fielderror fieldName="metaStudyDetailsForm.title"
						cssStyle="float:left" />
				</div>

				<div class="form-field">
					<label for="metaStudyDetailsForm.abstractText" class="required">Abstract
						<span class="required">* </span>
					</label>
					<s:textarea label="metaStudyDetailsForm.abstractText" cols="60"
						rows="10" cssClass="textfield required" cssStyle="float:left"
						name="metaStudyDetailsForm.abstractText" escapeHtml="true"
						escapeJavaScript="true" />
					<s:fielderror fieldName="metaStudyDetailsForm.abstractText"
						cssStyle="float:left" />
				</div>
				<div class="form-field">
					<label for="metaStudyDetailsForm.aimsText" class="required">Aims
					</label>
					<s:textarea label="metaStudyDetailsForm.aimsText" cols="60"
						rows="10" cssClass="textfield required" cssStyle="float:left"
						name="metaStudyDetailsForm.aimsText" escapeHtml="true"
						escapeJavaScript="true" />
					<s:fielderror fieldName="metaStudyDetailsForm.aimsText"
						cssStyle="float:left" />

				</div>


				<div class="form-field">
					<label for="metaStudyDetailsForm.recruitmentStatus">Recruitment
						Status :</label>
					<s:select id="metaStudyDetailsForm.recruitmentStatus"
						list="recruitmentStatuses" listKey="id" listValue="name"
						name="metaStudyDetailsForm.recruitmentStatus" value="8" />
					<s:fielderror fieldName="metaStudyDetailsForm.recruitmentStatus" />
				</div>

				<div class="form-field">
					<label for="metaStudyDetailsForm.studyType">Study Type <span
						class="required">* </span>:
					</label>
					<s:select id="metaStudyDetailsForm.studyType" list="metaStudyTypes"
						listKey="id" listValue="name"
						name="metaStudyDetailsForm.studyType" value="4" />
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

				<div class="form-field">
					<label for="metaStudyDetailsForm.studyUrl">Study URL :</label>
					<s:textfield id="metaStudyDetailsForm.studyUrl"
						name="metaStudyDetailsForm.studyUrl" cssClass="textfield"
						cssStyle="float:left" maxlength="255" escapeHtml="true"
						escapeJavaScript="true" />
					<s:fielderror fieldName="metaStudyDetailsForm.studyUrl"
						cssStyle="float:left" />
				</div>

			</div>
			<h3 id="studyResearchMgmtLabel" class="clear-both collapsable">
				<span id="studyResearchMgmtLabelPlusMinus"></span>&nbsp;Study
				Research Management
			</h3>
			<div id="researchMgmt">
				<div id="hideResearchMgmt">
					<span class="remainder-message"> <b>Please Click "ADD TO
							TABLE" button to add attribute to the Research Management Table.</b>
					</span>
				</div>

				<div id="researchMgmtError"
					style="width: 500px; margin-left: 10px; font-size: 12px;">
					<s:fielderror>
						<s:param>sessionMetaStudy.metaStudy.primaryPI</s:param>
					</s:fielderror>
				</div>

				<jsp:include page="editMetaStudyResearchMgmt.jsp" />
			</div>

			<h3 id="studyInformationLabel" class="clear-both collapsable">
				<span id="studyInformationLabelPlusMinus"></span>&nbsp;Study
				Information
			</h3>
			<div id="studyInformation">
				<jsp:include page="editMetaStudyInfo.jsp" />
			</div>

			<div class="form-field inline-right-button">
				<div class="button btn-primary" style="margin-right: 10px;">
					<input type="button" value="Create & Finish"
						onclick="javascript:createMetaStudy('metaStudyValidationAction!submit.action')" />
				</div>
				<div class="button" style="margin-right: 5px;">
					<input type="button" value="Next"
						onclick="javascript:submitForm('metaStudyValidationAction!moveToDocumentation.action')" />
				</div>
				<a class="form-link" href="javascript:void(0)"
					onclick="javascript:cancelCreation()">Cancel</a>
			</div>
		</s:form>

		<div class="ibisMessaging-dialogContainer"></div>
	</div>
</div>

<script type="text/javascript" src="/portal/js/metastudy/metaStudy.js"></script>
<script type="text/javascript">
	setNavigation({"bodyClass":"primary", "navigationLinkID":"metaStudyModuleLink", "subnavigationLinkID":"metaStudyLink", "tertiaryLinkID":"createMetaStudyLink"});
	$('document').ready(function() { 
		
	$("#studyOverViewLabelPlusMinus").text("-");
		
		<s:if test="hasFieldErrors()">
		    $("#hideResearchMgmt").hide();
			$("#studyResearchMgmtLabelPlusMinus").text("-");
			$("#studyInformationLabelPlusMinus").text("-");
			$("#studyKeywordsLabelPlusMinus").text("-");
		</s:if>
		<s:else>
			$("#studyResearchMgmtLabelPlusMinus").text("+");
			$("#studyInformationLabelPlusMinus").text("+");
			$("#studyKeywordsLabelPlusMinus").text("+");	
			$("#researchMgmt").hide();
			$("#studyInformation").hide();
		</s:else>
		
		
		studyOverViewInit();
		studyResearchMgmtInit();
		studyInformationInit();	
		initAutoSelect();
	});
	
	function initAutoSelect() {
		$(".therapeuticAgentSelect").select2();
	    $(".therapyTypeSelect").select2();
	    $(".therapeuticTargetSelect").select2();
	    $(".modelNameSelect").select2();
	    $(".modelTypeSelect").select2();
	}
		
	function submitForm(action) {
		var theForm = document.getElementById('theForm');
		theForm.action = action;
		theForm.submit();
	}
</script>

