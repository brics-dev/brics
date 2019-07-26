<%@include file="/common/taglibs.jsp"%>

	<div id="studySite">
		<jsp:include page="editStudySite.jsp" />
	</div>
	
	<div class="form-field">
		<label for="studyStartDate" class="required">Start Date <span class="required">* </span>:</label>
		<s:textfield id="studyStartDate" name="studyDetailsForm.studyStartDate" cssClass="textfield required" 
				maxlength="20" escapeHtml="true" escapeJavaScript="true" />
		<s:fielderror fieldName="studyDetailsForm.studyStartDate" />
		<div class="special-instruction">Please enter the date in ISO format, ex: 2013-12-21.</div>
	</div>		

	<div class="form-field">
		<label for="studyEndDate" class="required">End Date <span class="required">* </span>:</label>
		<s:textfield id="studyEndDate" name="studyDetailsForm.studyEndDate" cssClass="textfield required" 
				maxlength="20" escapeHtml="true" escapeJavaScript="true" />
		<s:fielderror fieldName="studyDetailsForm.studyEndDate" />
		<div class="special-instruction">Please enter the date in ISO format, ex: 2013-12-21.</div>
	</div>		

	<div class="form-field">
		<label for="numberOfSubjects">Estimated Number of Subjects :</label>
		<s:textfield id="numberOfSubjects" name="studyDetailsForm.numberOfSubjects" cssClass="textfield" maxlength="10" />
		<s:fielderror fieldName="studyDetailsForm.numberOfSubjects" />
	</div>	
	
	<div class="form-field">
		<label for="fundingSource" class="required">Primary Funding Source <span class="required">* </span>:</label>
		<s:select id="fundingSource" list="fundingSourceList" listKey="id" listValue="name" name="studyDetailsForm.fundingSource" 
			value="studyDetailsForm.fundingSource.id" headerKey="" headerValue="- Select One -" />
		<s:fielderror fieldName="studyDetailsForm.fundingSource" />
	</div>		
		
	<jsp:include page="grantTable.jsp" />

	