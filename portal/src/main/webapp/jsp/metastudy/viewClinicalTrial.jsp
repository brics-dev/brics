<%@include file="/common/taglibs.jsp"%>

<div id="main-content">
	<h3>Clinical Trial Information (from ClinicalTrials.gov)</h3>
	<div class="form-output">
		<div class="label">Clinical Trial ID :</div>
		<div class="readonly-text">
			<a href="${currentClinicalStudy.requiredHeader.url}" target="_blank"><s:property value="clinicalTrialId" /></a>
		</div>
	</div>
	<div class="form-output">
		<div class="label">Title :</div>
		<div class="readonly-text">
			<s:property value="currentClinicalStudy.briefTitle" />
		</div>
	</div>
	<div class="form-output">
		<div class="label">Overview :</div>
		<div class="readonly-text">
			<s:property value="currentClinicalStudy.briefSummary.textblock" />
		</div>
	</div>
	<div class="form-output">
		<div class="label">Status :</div>
		<div class="readonly-text">
			<s:property value="currentClinicalStudy.overallStatus" />
		</div>
	</div>
	<div class="form-output">
		<div class="label">Study ID :</div>
		<div class="readonly-text">
			<s:property value="currentClinicalStudy.idInfo.orgStudyId" />
		</div>
	</div>
	<s:if test="%{currentClinicalStudy.principalInvestigator!=null}">
		<div class="form-output">
			<div class="label">Principal Investigator :</div>
			<div class="readonly-text">
				<s:property value="currentClinicalStudy.principalInvestigator.fullTitle" />
			</div>
		</div>
	</s:if>
	<s:if test="%{currentClinicalStudy.startDate!=null}">
		<div class="form-output">
			<div class="label">Start Date :</div>
			<div class="readonly-text">
				<s:property value="currentClinicalStudy.startDate" />
			</div>
		</div>
	</s:if>
	<s:if test="%{currentClinicalStudy.completionDate!=null}">
		<div class="form-output">
			<div class="label">End Date :</div>
			<div class="readonly-text">
				<s:property value="currentClinicalStudy.completionDate.value" />
			</div>
		</div>
	</s:if>
</div>