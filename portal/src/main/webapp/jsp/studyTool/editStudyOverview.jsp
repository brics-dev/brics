<%@include file="/common/taglibs.jsp"%>
<link href="https://cdnjs.cloudflare.com/ajax/libs/select2/4.0.6-rc.0/css/select2.min.css" rel="stylesheet" />
<script src="https://cdnjs.cloudflare.com/ajax/libs/select2/4.0.6-rc.0/js/select2.min.js"></script>

<s:if test="%{!isCreate}">
	<div class="form-output">
		<div class="label">Study ID:</div>
		<div class="readonly-text">
			<s:property value="currentStudy.prefixedId" />
		</div>
	</div>
</s:if>

<style>
.select2-selection__rendered {
    padding-right: 30px !important;
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

<input type="hidden" name="studyDetailsForm.id"
	value="${currentStudy.id}" />

<div class="form-field">
	<label for="title" class="required">Title <span
		class="required">* </span>:
	</label>
	<s:textfield id="title" name="studyDetailsForm.title"
		cssClass="textfield required" maxlength="255" />
	<s:fielderror fieldName="studyDetailsForm.title" />
</div>

<div class="form-field">
	<label for="abstractText" class="required">Abstract <span
		class="required">* </span>:
	</label>
	<s:textarea id="abstractText" cols="60" rows="4"
		cssClass="textfield required" name="studyDetailsForm.abstractText"
		escapeHtml="true" escapeJavaScript="true" />
	<s:fielderror fieldName="studyDetailsForm.abstractText" />
</div>

<div class="form-field">
	<label for="goals">Aims :</label>
	<s:textarea id="goals" cols="60" rows="4" cssClass="textfield required"
		name="studyDetailsForm.goals" escapeHtml="true"
		escapeJavaScript="true" />
	<s:fielderror fieldName="studyDetailsForm.goals" />
</div>

<div class="form-field">
	<label for="recruitmentStatus">Recruitment Status :</label>
	<s:select id="recruitmentStatus" list="recruitmentStatuses"
		listKey="id" listValue="name"
		name="studyDetailsForm.recruitmentStatus"
		value="studyDetailsForm.recruitmentStatus.id" headerKey=""
		headerValue="- Select One -" />
</div>

<div class="form-field">
	<label for="studyType" class="required">Study Type <span
		class="required">* </span>:
	</label>
	<s:select id="studyType" list="studyTypes" listKey="id"
		listValue="name" name="studyDetailsForm.studyType"
		value="studyDetailsForm.studyType.id" headerKey=""
		headerValue="- Select One -" />
	<s:fielderror fieldName="studyDetailsForm.studyType" />
</div>

<c:set var="selectedTAgentList" value="${studyDetailsForm.therapeuticAgentSet}" />

<div class="form-field">
<label for="therapeuticAgent" class="required">Therapeutic Agent :</label>
<select class="therapeuticAgentSelect" name="studyDetailsForm.therapeuticAgentSelect" multiple="multiple">
	<c:forEach var="tAgent" items="${allTherapeuticAgents}">
		<s:set var="selected" value="false" />
		<c:forEach var="selectedTAgent" items="${selectedTAgentList}">
			<c:if test="${ tAgent.id == selectedTAgent.id || tAgent.text == selectedTAgent.text}">
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

<c:set var="selectedTTypeList" value="${therapyTypeSet}" />

<div class="form-field">
<label for="therapyType" class="required">Therapy Type :</label>
<select class="therapyTypeSelect" name="studyDetailsForm.therapyTypeSelect" multiple="multiple">
	<c:forEach var="tType" items="${allTherapyTypes}">
		<s:set var="selected" value="false" />
		<c:forEach var="selectedTType" items="${selectedTTypeList}">
			<c:if test="${ tType.id == selectedTType.id || tType.text == selectedTType.text }">
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


<c:set var="therapeuticTargetList" value="${therapeuticTargetSet}" />

<div class="form-field">
<label for="therapeuticTarget" class="required">Therapeutic Target :</label>
<select class="therapeuticTargetSelect" name="studyDetailsForm.therapeuticTargetSelect" multiple="multiple">
	<c:forEach var="tTarget" items="${allTherapeuticTargets}">
		<s:set var="selected" value="false" />
		<c:forEach var="selectedTTarget" items="${therapeuticTargetList}">
			<c:if test="${ tTarget.id == selectedTTarget.id || tTarget.text == selectedTTarget.text}">
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

<c:set var="modelNameList" value="${modelNameSet}" />


<div class="form-field">
<label for="modelName" class="required">Model Name :</label>
<select class="modelNameSelect" name="studyDetailsForm.modelNameSelect" multiple="multiple">
	<c:forEach var="mName" items="${allModelNames}">
		<s:set var="selected" value="false" />
		<c:forEach var="selectedMName" items="${modelNameList}">
			<c:if test="${ mName.id == selectedMName.id || mName.text == selectedMName.text}">
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

<c:set var="modelTypeList" value="${modelTypeSet}" />

<div class="form-field">
<label for="modelType" class="required">Model Type :</label>
<select class="modelTypeSelect" name="studyDetailsForm.modelTypeSelect" multiple="multiple">
	<c:forEach var="mType" items="${allModelTypes}">
		<s:set var="selected" value="false" />
		<c:forEach var="selectedMType" items="${modelTypeList}">
			<c:if test="${ mType.id == selectedMType.id || mType.text == selectedMType.text}">
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
	<label for="studyUrl">Study URL :</label>
	<s:textfield id="studyUrl" name="studyDetailsForm.studyUrl"
		cssClass="textfield" maxlength="255" escapeHtml="true" />
	<s:fielderror fieldName="studyDetailsForm.studyUrl"
		cssStyle="float:left" />
</div>

<div class="form-field">
	<label for="upload">Study Picture File :</label>
	<s:file id="graphicFile" name="studyDetailsForm.upload" cssStyle="float:left;" />
	<s:hidden id="graphicFileName" name="studyDetailsForm.uploadFileName" />
		<s:fielderror fieldName="studyDetailsForm.uploadFileName" />
	<div style="clear: both; margin-left: 165px">
		<b>Only JPEG or PNG File format, with preferred image size 200 x 160.</b>
	</div>
</div> 

<div id="sponsorInfoDiv" style="display: none; padding-top: 10px;">
	<jsp:include page="sponsorInfoTable.jsp" />
</div>

<div id="ctTableDiv" style="padding-top: 20px;">
	<jsp:include page="clinicalTrialTable.jsp" />
</div>

<script type="text/javascript">
	var baseUrl = location.origin+"/portal/ws/repository/repository/Study/";
	$('document').ready(function() {
		var studyType = $('#studyType').val();

		if (studyType == 2 || studyType == 3) {
			$("#sponsorInfoDiv").show();
		} else {
			$("#sponsorInfoDiv").hide();
		}
		
		var studyId = ("<s:property value="%{currentStudy.id}"/>") ? "<s:property value="%{currentStudy.id}"/>": "0";
		
		initAutoSelect();
		convertFileUpload($('#graphicFile'), $('#graphicFileName').val());
	});
		
	function initAutoSelect() {
		$(".therapeuticAgentSelect").select2();
	    $(".therapyTypeSelect").select2();
	    $(".therapeuticTargetSelect").select2();
	    $(".modelNameSelect").select2();
	    $(".modelTypeSelect").select2();
	}
	
	$('#studyType').on('change', function() {
		if (this.value == 2 || this.value == 3) {
			$("#sponsorInfoDiv").show();
		} else {
			$("#sponsorInfoDiv").hide();
		}
	});

	function addSponsorInfo() {
		var fdaInd = $('#fdaInd').val();
		var sponsor = $('#sponsor').val();

		$.ajax({
			type : "GET",
			url : "sponsorInfoValidationAction!addSponsorInfo.ajax",
			data : {
				"sponsorInfoEntry.fdaInd" : fdaInd,
				"sponsorInfoEntry.sponsor" : sponsor
			},
			"async" : false,
			success : function(data) {
				$('#sponsorInfoDiv').html(data);
				buildDataTables();
			}
		});
	}

	function removeSponsorInfo(sponsorInfoJson) {
		$.ajax({
			type : "POST",
			url : "studyAction!removeSponsorInfo.ajax",
			data : "sponsorInfoJson=" + JSON.stringify(sponsorInfoJson),
			"async" : true,
			success : function(data) {
				$('#sponsorInfoDiv').html(data);
				buildDataTables();
			}
		});
	}
</script>