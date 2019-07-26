<%@include file="/common/taglibs.jsp"%>

	<div class="form-field">
		<label for="metaStudyDetailsForm.fundingSource">Primary Funding Source<span class="required">* </span> :</label>
		<s:select id="fundingSource" list="fundingSourceList" listKey="id" listValue="name"
			name="metaStudyDetailsForm.fundingSource" value="metaStudyDetailsForm.fundingSource.id" headerKey="" headerValue="- Select One -" />
			<s:fielderror fieldName="metaStudyDetailsForm.fundingSource" />
	</div>

	<div id="ctTableDiv" style="padding-top:20px;">
		<jsp:include page="clinicalTrialMetaTable.jsp" />
	</div>
	<jsp:include page="grantMetaTable.jsp" />
	