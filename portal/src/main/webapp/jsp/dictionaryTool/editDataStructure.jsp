<%@include file="/common/taglibs.jsp"%>
<s:if test="%{sessionDataStructure.newStructure}">
	<title>Create Form Structure</title>
</s:if>
<s:elseif test="%{!sessionDataStructure.newStructure}">
	<title><s:property value="dataStructureForm.title" />&nbsp;Details</title>
</s:elseif>

<div class="clear-float">
	
</div>

<div class="border-wrapper">
<s:if test="%{!sessionDataStructure.newStructure && !sessionDataStructure.draftCopy}">
			<div id="breadcrumb">
				<s:if test="inAdmin">
					<s:a action="listDataStructureAction" method="list" namespace="/dictionaryAdmin">Manage Form Structures</s:a>
				</s:if>
				<s:else>
					<s:a action="listDataStructureAction" method="list" namespace="/dictionary">Search Form Structures</s:a>
				</s:else>
				&gt;
				<s:url action="dataStructureAction" method="view" var="viewTag">
					<s:param name="dataStructureId">
						<s:property value="currentDataStructure.id" />
					</s:param>
				</s:url>
				<a href="<s:property value="#viewTag" />"><s:property value="currentDataStructure.title" /></a> &gt; Edit Form
				Structure
			</div>
		</s:if>
		<div style="clear:both;"></div>
	<jsp:include page="../navigation/dataDictionaryNavigation.jsp" />
	<h1 class="float-left"><s:if test="inAdmin">(Admin)&nbsp;</s:if>Data Dictionary</h1>
	<div style="clear:both;"></div>
	<div id="main-content" style="margin-top:15px;">

		


		<s:form validate="true" name="dataStructureForm">
		<s:token />
			<ndar:dataStructureChevron action="dataStructureValidationAction" chevron="Edit Details" />

			<s:set var="currentDataStructure" value="currentDataStructure" />
			<s:set var="createdHistory" value="createdHistory" />
			<s:set var="ownerName" value="ownerName" />
			<s:bean name="gov.nih.tbi.dictionary.model.DataStructureForm" var="dataStructureForm">

				<s:if test="%{sessionDataStructure.newStructure}">
					<h2>Create Form Structure</h2>
				</s:if>
				<s:elseif test="%{!sessionDataStructure.newStructure}">
					<h2>
						<s:property value="dataStructureForm.title" />
						&nbsp;Details
					</h2>
				</s:elseif>

				<s:if test="hasActionErrors()">
					<div class="error-message">
						<s:actionerror />
					</div>
				</s:if>

				<p>A form structure represents a grouping/collection of various common data elements (CDE) and data elements
					used to gather information for a study. A form structure is analogous to a case report form (CRF) (electronic or
					paper) where data elements are linked together for collection and display.</p>

				<s:if test="%{sessionDataStructure.newStructure}">
					<p>Fill out the details below to create a form structure. On the following pages, you may attach data elements
						and apply permissions.</p>
				</s:if>
				<s:elseif test="%{!sessionDataStructure.newStructure}">
					<p>Edit the details for your form structure below. On the following pages, you may attach data elements and
						apply permissions.</p>
				</s:elseif>

				<div class="formElements">
					<s:if test="hasActionErrors()">
						<div class="form-error">
							<s:actionerror />
						</div>
					</s:if>

					<s:if test="!fileErrors.isEmpty()">
						<div class="form-error">
							<s:property value="fileErrors" />
						</div>
					</s:if>

					<h3>General Details</h3>
					<p class="required">
						Fields marked with a <span class="required">* </span>are required.
					</p>
					<div class="clear-right"></div>
					
					<s:hidden name="dataStructureForm.createdBy" value="%{currentDataStructure.createdBy}" escapeHtml="true" escapeJavaScript="true" />
					<s:hidden name="dataStructureForm.dateCreated" value="%{dataStructureForm.dateCreated}" escapeHtml="true" escapeJavaScript="true" />					
					<div class="form-field">
						<label for="title" class="required">Title <span class="required">* </span>:
						</label>
						<s:textfield name="dataStructureForm.title" cssClass="textfield required" escapeHtml="true" escapeJavaScript="true" />
						<s:fielderror fieldName="dataStructureForm.title" />
					</div>

					<div class="form-field">
						<label for="shortName" class="required">Short Name <span class="required">* </span>:
						</label>
							<s:textfield name="dataStructureForm.shortName" cssClass="textfield required" escapeHtml="true" escapeJavaScript="true" />
						<s:fielderror fieldName="dataStructureForm.shortName" />
						<div class="special-instruction">Must be unique and start with an alphabetic character, contain only
							alphanumeric and underscores, and must be 26 characters or less.</div>
					</div>

					<div class="form-field">
						<label for="title" class="required">Description <span class="required">* </span>:
						</label>
						<s:textarea name="dataStructureForm.description" cssClass="textfield required" escapeHtml="true" escapeJavaScript="true" />
						<s:fielderror fieldName="dataStructureForm.description" />
					</div>

					<div class="form-field">
						<label for="dataStructureForm.selectedDiseases" class="required">Disease <span class="required">* </span>:
						</label> <select name="dataStructureForm.diseaseList" id="dataStructureForm.disease" multiple="multiple">
							<option value="">- Select One or More -</option>
							<c:forEach var="disease" items="${diseaseOptions}">
								<s:set var="selected" value="false" />
								<c:forEach var="selectedDisease" items="${dataStructureForm.diseaseList}">
									<c:if test="${ disease.id == selectedDisease.disease.id }">
										<s:set var="selected" value="true" />
									</c:if>
								</c:forEach>
								<c:choose>
									<c:when test="${selected == true}">
										<option selected="selected" value="${disease.id}">${disease.name}</option>
									</c:when>
									<c:otherwise>
										<option value="${disease.id}">${disease.name}</option>
									</c:otherwise>
								</c:choose>
							</c:forEach>
						</select>
						<s:fielderror fieldName="dataStructureForm.diseaseList" />
						<div class="special-instruction">Hold "Ctrl" to select multiple values</div>
					</div>

					<div class="form-field">
						<label for="title">Organization:</label>
						<s:textfield name="dataStructureForm.organization" cssClass="textfield" escapeHtml="true" escapeJavaScript="true" />
						<s:fielderror fieldName="dataStructureForm.organization" />
					</div>

					<div class="form-field">
						<label for="dataStructureForm.fileType" class="required">Form Type <span class="required">* </span>:
						</label>
						<s:select id="dataStructureForm.fileType" list="fileTypeList" listKey="id" listValue="type"
							name="dataStructureForm.fileType" value="dataStructureForm.fileType.id" headerKey="" headerValue="- Select One -" />
						<s:fielderror fieldName="dataStructureForm.fileType" />
					</div>
					
					<div class="form-field">
						<label for="dataStructureForm.standardization" class="required">Standardization <span class="required">* </span>:
						</label>
						<s:select id="dataStructureForm.standardization" list="mainStandardizationTypes" listKey="name" listValue="display"
							name="dataStructureForm.standardization" value="dataStructureForm.standardization.name" headerKey="" headerValue="- Select One -" />
						<s:fielderror fieldName="dataStructureForm.standardization" />
					</div>
					
					<div class="form-field">
						<label for="dataStructureForm.formLabelList" class="required">Label:</label>
						<select name="dataStructureForm.formLabelList" id="dataStructureForm.formLabel" multiple="multiple">
							<option value="">- Select One or More -</option>
							<c:forEach var="formLabel" items="${formLabelOptions}">
								<s:set var="selected" value="false" />
								<c:forEach var="selectedLabel" items="${dataStructureForm.formLabelList}">
									<c:if test="${formLabel.label == selectedLabel.label}">
										<s:set var="selected" value="true" />
									</c:if>
								</c:forEach>
								<c:choose>
									<c:when test="${selected == true}">
										<option selected="selected" value="${formLabel.id}">${formLabel.label}</option>
									</c:when>
									<c:otherwise>
										<option value="${formLabel.id}">${formLabel.label}</option>
									</c:otherwise>
								</c:choose>
							</c:forEach>
						</select>
						<div class="special-instruction">Hold "Ctrl" to select multiple values</div>
					</div>
					
										<!--  This is for the required field -->
					<s:hidden id="dataStructureForm.currentOrg" name="dataStructureForm.currentOrg" value="%{getOrgName()}" escapeHtml="true" escapeJavaScript="true" />
					
					<div class="form-field">
						<s:if test="%{isDictionaryAdmin || isAdmin}">
							<label class="long-text required">Is this a program Required Form?<span class="required">* </span>:	
							</label>
							<ul class="checkboxgroup-horizontal">
								<li><s:if test="%{dataStructureForm.isInstancesRequiredForValue == getOrgName()}">
										<input checked type="radio" id="isInstancesRequiredForValue" name="dataStructureForm.isInstancesRequiredForValue" value="${orgName}" />
									</s:if> 
									<s:else>
										<input type="radio" id="isInstancesRequiredForValue" name="dataStructureForm.isInstancesRequiredForValue" value="${orgName}"/>
									</s:else> <label for="isInstancesRequiredForValue">Yes</label></li>
								<li><s:if test="%{sessionDataStructure.newStructure || (dataStructureForm.isInstancesRequiredForValue != null && dataStructureForm.isInstancesRequiredForValue != getOrgName())}">
										<input checked type="radio" id="notisInstancesRequiredFor" name="dataStructureForm.isInstancesRequiredForValue" value="" />
									</s:if> 
									<s:else>
										<input type="radio" id="notisInstancesRequiredFor" name="dataStructureForm.isInstancesRequiredForValue" value="" />
									</s:else> <label for="notisInstancesRequiredFor">No</label></li>
							</ul>						
						</s:if>
							<s:else>
								<s:if test="%{sessionDataStructure.newStructure}">
									<s:hidden name="dataStructureForm.isInstancesRequiredForValue" value="" />
								</s:if>
								<s:else>
									<s:hidden name="dataStructureForm.isInstancesRequiredForValue"/>							
								</s:else>
						</s:else> 
						<s:fielderror fieldName="dataStructureForm.isInstancesRequiredForValue" />
					</div>

					<div class="form-field">
						<label class="long-text required">Does this represent a copyrighted form?<span class="required">* </span>:
						</label>
						<ul class="checkboxgroup-horizontal">
							<li><s:if test="dataStructureForm.isCopyrighted">
									<input checked type="radio" id="isCopyrighted" name="dataStructureForm.isCopyrighted" value="true" />
								</s:if> <s:else>
									<input type="radio" id="isCopyrighted" name="dataStructureForm.isCopyrighted" value="true" />
								</s:else> <label for="isCopyrighted">Yes</label></li>
							<li><s:if test="dataStructureForm.isCopyrighted == null || dataStructureForm.isCopyrighted">
									<input type="radio" id="notCopyrighted" name="dataStructureForm.isCopyrighted" value="false" />
								</s:if> <s:elseif test="!dataStructureForm.isCopyrighted">
									<input checked type="radio" id="notCopyrighted" name="dataStructureForm.isCopyrighted" value="false" />
								</s:elseif> <label for="notCopyrighted">No</label></li>
						</ul>
						<s:fielderror fieldName="dataStructureForm.isCopyrighted" />
					</div>

					<div class="form-field">
						<label for="title">Version:</label> <span class="readonly-text"><s:property
								value="currentDataStructure.version" /></span>
					</div>

					<div class="form-field">
						<label for="title">Status:</label> <span class="readonly-text"><s:property
								value="currentDataStructure.status.type" /></span>
					</div>

					<s:if test="%{!sessionDataStructure.newStructure}">
						<div class="form-field">
							<label for="title">Number of Data Elements:</label> <span class="readonly-text"><c:out
									value="${fn:length(currentDataStructure.dataElements)}" /></span>
						</div>

						<div class="form-field">
							<label for="title">Created By:</label> <span class="readonly-text"><s:property
									value="currentDataStructure.createdBy" /></span>
						</div>

						<div class="form-field">
							<label for="title">Created Date:</label> <span class="readonly-text"><s:property
									value="currentDataStructure.dateCreatedString" /></span>
						</div>
					</s:if>

					<s:if test="%{sessionDataStructure.newStructure}">
						<div class="form-field">
							<label for="title">Created By:</label> <span class="readonly-text"><s:property value="currentDataStructure.createdBy" /></span>
						</div>

						<div class="form-field">
							<label for="title">Created Date:</label> <span class="readonly-text"><s:property value="currentDataStructure.dateCreatedString" /></span>
						</div>
					</s:if>

					<ndar:dataStructureSave action="dataStructureValidationAction" method="moveToDocumentations" />

				</div>

			</s:bean>
		</s:form>

	</div>
</div>

<script type="text/javascript">
	var globalToken;
	<s:if test="inAdmin">
		setNavigation({"bodyClass":"primary", "navigationLinkID":"dataDictionaryModuleLink", "subnavigationLinkID":"defineDataToolsLink", "tertiaryLinkID":"manageDataStructuresLink"});
	</s:if>
	<s:else>
		<s:if test="%{!sessionDataStructure.newStructure}">
			setNavigation({"bodyClass":"primary", "navigationLinkID":"dataDictionaryModuleLink", "subnavigationLinkID":"dataDictionaryToolLink", "tertiaryLinkID":"listDataStructureLink"});
		</s:if>
		<s:else>
			setNavigation({"bodyClass":"primary", "navigationLinkID":"dataDictionaryModuleLink", "subnavigationLinkID":"dataDictionaryToolLink", "tertiaryLinkID":"dataStructureLink"});
		</s:else>
	</s:else>
	
	//overrides the default action when 'enter' key is press. 
	//note: it is called the 'enter' key not the 'return' key.
// 	document.onkeypress = function(e){
// 	    if (!e) e = window.event;
// 	    if (e.keyCode == '13'){
// 	      submitSearch();
// 	      return false;
// 	    }
// 	}
	

</script>