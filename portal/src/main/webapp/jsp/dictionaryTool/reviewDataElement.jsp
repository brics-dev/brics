<%@include file="/common/taglibs.jsp"%>
<%@ page contentType="text/html; charset=UTF-8" %>
<title><s:property escapeHtml="true" value="currentDataElement.title" /></title>

<s:set var="dataType" value="dataType" />
<s:set var="currentMapElement" value="currentMapElement" />
<s:set var="currentDataElement" value="currentDataElement" />
<s:set var="deletable" value="deletable" />
<s:set var="attachedDataStructures" value="attachedDataStructures" />
<s:set var="user" value="user" />
<s:set var="readOnly" value="readOnly" />
<s:set var="rulesEngineException" value="rulesEngineException" />


<div class="border-wrapper">
	<s:if test="%{nameSpace != 'publicData'}">

		<s:if test="%{formType == 'edit'}">
			<div id="breadcrumb">
				<s:if test="inAdmin">
					<s:a action="searchDataElementAction" method="list"
						namespace="/dictionaryAdmin">Manage Data Elements</s:a>
				</s:if>
				<s:else>
					<s:a action="searchDataElementAction" method="list"
						namespace="/dictionary">Search Data Elements</s:a>
				</s:else>
				&gt;
				<s:url action="dataElementAction" method="view" var="viewTag">
					<s:param name="dataElementName">
						<s:property value="currentDataElement.name" />
					</s:param>
				</s:url>
				<a href="<s:property value="#viewTag" />"><s:property
						value="currentDataElement.title" /></a> &gt; Edit Data Element
			</div>
		</s:if>

		<!-- 		Left menus -->
		<div class="">
			<jsp:include page="../navigation/dataDictionaryNavigation.jsp" />

			<h1 class="float-left"><s:if test="inAdmin">(Admin)&nbsp;</s:if>Data Dictionary</h1>
			<div style="clear: both;"></div>
		</div>
	</s:if>
	
	<!-- 		The pages main content -->
	<div id="main-content" style="margin-top:15px;">

		<s:form id="theForm" cssClass="validate" action="basicdataElementValidationAction" method="post" validate="true">
			<s:token />
			<s:hidden name="dataElementChangeSeverity"
				value="%{dataElementChangeSeverity.severityLevel}" escapeHtml="true" escapeJavaScript="true" />

			<s:if test="dataType == 'dataElement'">
				<ndar:dataElementChevron action="dataElementAction" chevron="Review" />
			</s:if>
			<s:if test="dataType == 'mapElement'">
				<ndar:dataElementChevron action="dataElementAction" chevron="Review" />
			</s:if>

			<s:if test="hasActionErrors()">
				<div class="errors">
					<s:actionerror />
				</div>
			</s:if>
			<s:if test="%{rulesEngineException}">
				<div class="error-message">
					<p>An error has occurred within the system. Please contact the system administrator.</p>
				</div>
			</s:if>

			<div id="leftContent" style="float: left; width: 100%;">
				<s:if test="%{formType=='edit'}">
					<s:if test="%{shortNameChangeRequired}">
						<div style="min-height: 15px; border: 1px solid #ff0000; padding: 5px; margin-bottom: 5px;">
							<p>
								<span class="icon"><img alt="information"
									src="<s:url value='/images/error-small.gif' />"
									style="padding-right: 10px;" height="15px" width="15px" /></span>You
								must change the Variable Name to create a new Data Element <a
									href="javascript:void(0);"
									onClick="submitForm('${dataType}Action!editDetails.action');">Change
									Variable Name</a>
							</p>
						</div>
					</s:if>
				</s:if>

				<s:if test="%{changeStrings != null && dataElementChangeSeverity != null}">
					<div id="versioningMessageBox" class="clear-both">
						<s:if test="%{dataElementChangeSeverity.severityLevel == 'new'}">
The following changes will result in a new Form Structure. Please review and click the save button to generate a new Form Structure.
The Form Structure you were editing will remain as is.
						</s:if>
						<s:elseif test="%{dataElementChangeSeverity.severityLevel == 'minor'}">
The following changes will result in a minor change to the Form Structure. Please review and click the save button to generate a new version of the Form Structure.
						</s:elseif>
						<s:elseif test="%{dataElementChangeSeverity.severityLevel == 'major'}">
The following changes will result in a major change to the Form Structure. Please review and click the save button to generate a new version of the Form Structure.
						</s:elseif>

						<s:iterator var="change" value="changeStrings">
							<p>
								<span class="icon"><img alt="information" src="<s:url value='/images/icons/info.png' />"
									style="padding-right: 10px;" height="15px" width="15px" /></span>
								<s:property value="#change" />
							</p>
						</s:iterator>
					</div>
				</s:if>
				
				<s:if test="%{currentDataElement.status.name != 'Draft' && currentDataElement.status.name != 'Awaiting Publication'}">
				
					<div class="form-output" style="border:1px solid #d6d6d6; margin:5px 0px; padding:5px;">
						<p><b>Change History</b><font color="red">*</font><s:fielderror fieldName="auditNote" /><br>
							Please enter your reasons for making changes to the Data Element.
						</p>
							
						<div class="readonly-text">
							<center>
								<s:textarea cols="125%" rows="10%" id="auditNote" name="auditNote" escapeHtml="true" escapeJavaScript="true"/>
							</center>
						</div>
					</div>
				</s:if>

				<h2>Review</h2>
				<h2 class="pageHeader">
					<s:property value="currentDataElement.category.name" />:&nbsp;
					<c:if test="${ currentDataElement.title != '' }">
						<s:property escapeHtml="true" value="currentDataElement.title" />
					</c:if>
				</h2>
				<hr>
				
				<h3 id="generalLabel" class="clear-both collapsable">
					<span id="generalLabelPlusMinus"></span>&nbsp;General Details
				</h3>
				<div id="general">
					<div class="clear-right"></div>

					<div class="form-output">
						<s:label cssClass="label" key="Version" />
						<div class="readonly-text">
							<s:property escapeHtml="false" value="currentDataElement.version" />
						</div>
					</div>

					<div class="form-output">
						<div class="label">Element Type:</div>
						<div class="readonly-text">
							<s:property value="currentDataElement.category.name" />
						</div>
					</div>

					<div class="form-output">
						<s:label cssClass="label" key="label.dataElement.title" />
						<div class="readonly-text">
							<s:property escapeHtml="true" value="currentDataElement.title" />
						</div>
					</div>

					<div class="form-output">
						<s:label cssClass="label" key="label.dataElement.name" />
						<div class="readonly-text">
							<s:property escapeHtml="true" value="currentDataElement.name" />
						</div>
					</div>

					<div class="form-output">
						<div class="label">Short Description:</div>
						<div class="readonly-text">
							<s:property escapeHtml="true" value="currentDataElement.shortDescription" />
						</div>
					</div>

					<div class="form-output">
						<div class="label">Definition:</div>
						<div class="readonly-text limitLength" id="description">
							<s:property escapeHtml="true"
								value="currentDataElement.description" />
						</div>
					</div>

					<div class="form-output">
						<div class="label">Notes:</div>
						<div class="readonly-text limitLength" id="notes">
							<s:property escapeHtml="true" value="currentDataElement.notes" />
						</div>
					</div>

					<div class="form-output">
						<label for="createdate">Creation Date:</label>
						<div class="readonly-text limitLength" id="createdate">
							<ndar:dateTag value="${currentDataElement.dateCreated}" />
						</div>
					</div>
					
					<div class="form-output">
						<div class="label">Created By:</div>
							<div class="readonly-text limitLength" id="createdBy">
							<s:property escapeHtml="false" value="currentDataElement.createdBy" />
						</div>
					</div>

					<div class="form-output">
						<div class="label">Historical Notes:</div>
						<div class="readonly-text limitLength" id="historical-notes">
							<s:property escapeHtml="true"
								value="currentDataElement.historicalNotes" />
						</div>
					</div>
					<div class="form-output">
						<div class="label">References:</div>
						<div class="readonly-text limitLength" id="references">
							<s:property escapeHtml="true" value="currentDataElement.references" />
						</div>
					</div>
				</div>
				<br>

				<!--  next box  -->
				<h3 id="basicLabel" class="clear-both collapsable">
					<span id="basicLabelPlusMinus"></span>&nbsp;Basic Attributes
				</h3>
				<div id="basic">
					<div class="form-output">
						<div class="label">Data Type:</div>
						<div class="readonly-text">
							<s:property value="currentDataElement.type.value" />
						</div>
					</div>

					<div class="form-output">
						<div class="label">Input Restrictions:</div>
						<div class="readonly-text">
							<s:property value="currentDataElement.restrictions.value" />
						</div>
					</div>

					<s:if test="currentDataElement.size!=null">
						<div class="form-output">
							<div class="label">Maximum Character Quantity:</div>
							<div class="readonly-text">
								<s:property value="currentDataElement.size" />
							</div>
						</div>
					</s:if>

					<s:if test="currentDataElement.measuringUnit!=null">
						<div class="form-output">
							<div class="label">Unit of Measure:</div>
							<div class="readonly-text">
								<s:property value="currentDataElement.measuringUnit" />
							</div>
						</div>
					</s:if>

					<c:if test="${fn:length(currentDataElement.valueRangeList) != 0}">
						<!-- A table that displays the Acceptable Value ranges -->
						<div class="form-output">
							<div class="label">Pre-Defined Values</div>
							<div class="readonly-text">
								<table class="display-data">
									<thead>
										<tr>
											<th>Permissible Value</th>
											<th class="alphanumericRange">Description</th>
											<th>Output Code</th>
										</tr>
									</thead>
									<tbody>
										<s:iterator value="currentDataElement.valueRangeList" var="valueRange">
											<tr>
												<td><s:property escapeHtml="false" value="valueRange" /></td>
												<td><s:property escapeHtml="false" value="description" /></td>
												<td><s:property escapeHtml="false" value="outputCode" /></td>
											</tr>
										</s:iterator>
									</tbody>
								</table>
							</div>
						</div>
					</c:if>

					<s:if test="currentDataElement.minimumValue!=null">
						<div class="form-output">
							<div class="label">Minimum Value:</div>
							<div class="readonly-text">
								<s:property value="currentDataElement.minimumValue" />
							</div>
						</div>
					</s:if>
					<s:if test="currentDataElement.maximumValue!=null">
						<div class="form-output">
							<div class="label">Maximum Value:</div>
							<div class="readonly-text">
								<s:property value="currentDataElement.maximumValue" />
							</div>
						</div>
					</s:if>
					
					<s:if test="currentDataElement.id != null">
					<div class="form-output">
						<div class="readonly-text underLabel"><a href="schemaMappingAction!viewSchemaMappingValues.action?dataElement=<s:property value="currentDataElement.name" />">External Schema Permissible Value Mapping</a></div>
					</div>
					</s:if>

					<div class="form-output">
						<div class="label">Population:</div>
						<div class="readonly-text">
							<s:property value="currentDataElement.population.name" />
						</div>
					</div>

					<div class="form-output">
						<div class="label">Guidelines/Instructions:</div>
						<div class="readonly-text limitLength" id="guidelines">
							<s:property escapeHtml="true" value="currentDataElement.guidelines" />
						</div>
					</div>

					<div class="form-output">
						<div class="label">Preferred Question Text:</div>
						<div class="readonly-text limitLength" id="suggestedQuestion">
							<s:property escapeHtml="true"
								value="currentDataElement.suggestedQuestion" />
						</div>
					</div>
				</div>
				<br>
				
				<h3 id="documentationLabel" class="clear-both collapsable">
				<span id="documentationLabelPlusMinus"></span>&nbsp;Documentation
				</h3>
				<div id="documentation">
					<jsp:include page="dictionaryDocumentationList.jsp">
				        <jsp:param name="idtUrl" value="dataElementAction!getSupportingDocumentationList.action"/>
				    </jsp:include>
				</div>
				<!--  next box  -->
				<h3 id="classificationsLabel" class="clear-both collapsable">
					<span id="classificationsLabelPlusMinus"></span>&nbsp;Category Groups and Classifications
				</h3>
				<div id="classifications">
					<div class="clear-right"></div>
					<div id="categoryGroups" class="form-output">
						<div class="label">Category Groups and Classifications:</div>
						<div class="readonly-text" >
							<table border="1" class="display-data">
								<tr>
									<th width="25%">Disease</th>
									<th width="25%">Domain</th>
									<th width="25%">Sub-Domain</th>
								</tr>
								<s:iterator value="currentDataElement.subDomainElementList">
									<tr>
										<td><strong><s:property value="disease.name" /></strong></td>
										<td><strong><s:property value="domain.name" /></strong></td>
										<td><strong><s:property value="subDomain.name" /></strong></td>
									</tr>
								</s:iterator>
							</table>
						</div>
					</div>

					<div id="dataElementClassifications">
						<ndar:dataElementClassification admin="false" readOnly="true" />
					</div>
				</div>
				<br>

				<!--  next box  -->
				<h3 id="keywordsLabel" class="clear-both collapsable">
					<span id="keywordsLabelPlusMinus"></span>&nbsp;Keywords and Labels
				</h3>
				<div id="keywords">
					<div class="clear-right"></div>
					<div class="form-output">
						<div class="label">Keywords:</div>
						<div class="readonly-text">
							<s:iterator var="keywordFromList" value="currentDataElement.keywords" status="keywordStatus">
								<c:out value="${keyword}" />
								<s:if test="!#keywordStatus.last">,</s:if>
							</s:iterator>
						</div>
					</div>

					<div class="form-output">
						<div class="label">Label(s):</div>
						<div class="readonly-text">
							<s:iterator var="label" value="currentDataElement.labels" status="labelStatus">
								<c:out value="${keyword}" />
								<s:if test="!#labelStatus.last">,</s:if>
							</s:iterator>
						</div>
					</div>
				</div>
				<br>

				<!--  next box  -->
				<h3 id="specificLabel" class="clear-both collapsable">
					<span id="specificLabelPlusMinus"></span>&nbsp;Specific Details
				</h3>
				<div id="specific">
					<div class="clear-right"></div>
					<div class="form-field">
						<label for="effdate">Effective Date:</label>
						<div class="readonly-text limitLength" id="effdate">
							<ndar:dateTag value="${currentDataElement.effectiveDate}" />
						</div>
					</div>
					<div class="form-field">
						<label for="untildate">Until Date:</label>
						<div class="readonly-text limitLength" id="untildate">
							<ndar:dateTag value="${currentDataElement.untilDate}" />
						</div>
					</div>

					<div class="form-field">
						<label for="modifieddate">Last Change Date:</label>
						<div class="readonly-text limitLength" id="modifieddate">
							<%--	<ndar:dateTag value="${currentDataElement.modifiedDate}" /> --%>
						</div>
					</div>
					<div class="form-field">
						<label for="seealso">See Also:</label>
						<div class="readonly-text limitLength" id="seealso">
							<s:property value="currentDataElement.seeAlso" />
						</div>
					</div>

					<div class="form-field">
						<label for="subon">Submitting Organization Name:</label>
						<div class="readonly-text limitLength" id="subon">
							<s:property value="currentDataElement.submittingOrgName" />
						</div>

					</div>
					<div class="form-field">
						<label for="subcn">Submitting Contact Name:</label>
						<div class="readonly-text limitLength" id="subcn">
							<s:property value="currentDataElement.submittingContactName" />
						</div>

					</div>
					<div class="form-field">
						<label for="subci">Submitting Contact Information:</label>
						<div class="readonly-text limitLength" id="subci">
							<s:property value="currentDataElement.submittingContactInfo" />
						</div>

					</div>
					<div class="form-field">
						<label for="son" class="required">Steward Organization Name:</label>
						<div class="readonly-text limitLength" id="son">
							<s:property value="currentDataElement.stewardOrgName" />
						</div>
					</div>

					<div class="form-field">
						<label for="scn">Steward Contact Name:</label>
						<div class="readonly-text limitLength" id="scn">
							<s:property value="currentDataElement.stewardContactName" />
						</div>

					</div>
					<div class="form-field">
						<label for="sci">Steward Contact Information:</label>
						<div class="readonly-text limitLength" id="sci">
							<s:property value="currentDataElement.stewardContactInfo" />
						</div>
					</div>

<%-- 					<div class="form-output">
						<div class="label">LOINC ID:</div>
						<div class="readonly-text limitLength" id="loinc">
							<s:property value="currentDataElement.loinc.value" />
						</div>
					</div>

					<div class="form-output">
						<div class="label">caDSR ID:</div>
						<div class="readonly-text limitLength" id="cadsr">
							<s:property value="currentDataElement.cadsr.value" />
						</div>
					</div>

					<div class="form-output">
						<div class="label">SNOMED ID:</div>
						<div class="readonly-text limitLength" id="snomed">
							<s:property value="currentDataElement.snomed.value" />
						</div>
					</div>

					<div class="form-output">
						<div class="label">CDISC ID:</div>
						<div class="readonly-text limitLength" id="cdisc">
							<s:property value="currentDataElement.cdisc.value" />
						</div>
					</div>

					<div class="form-output">
						<div class="label">NINDS ID:</div>
						<div class="readonly-text limitLength" id="ninds">
							<s:property value="currentDataElement.ninds.value" />
						</div>
					</div> --%>
				</div>
				<br>

			</div>
		</s:form>
		
		<div class="form-field clear-left">
		<div class="requiredValidationErrorContainer" id="requiredValidationErrorContainer"></div>
			<s:if test="%{!rulesEngineException}">
				<s:if test="%{!shortNameChangeRequired}">
					<s:if test="%{currentDataElement.status == null || currentDataElement.status.name == 'Draft' || currentDataElement.status.name == 'Awaiting Publication'}">
						<div class="button">
							<input type="button" value="Save & Finish"
								onClick="validateAllRequiredOnFinalSubmit();" />
						</div>
					</s:if>
					<s:else>
						<div class="button">
							<input type="button" value="Save & Finish"
								onClick="submitForm('reviewDataElementValidationAction!submit.action');" />
						</div>
					</s:else>

				</s:if>
				<s:else>
					<div class="button">
						<input type="button" value="Edit Variable Name"
							onClick="submitForm('dataElementAction!editDetails.action');" />
					</div>
				</s:else>
			</s:if>


			<a class="form-link" href="javascript:cancel()">Cancel</a>
		</div>
	</div>
	
	<!--  put below in a new place -->
</div>

<script type="text/javascript">
 	function   validateAllRequiredOnFinalSubmit(){
 		
 		//Details page required field
 		var submittingOrgName = "<s:property value="currentDataElement.submittingOrgName" />";
 		var stewardOrgName = "<s:property value="currentDataElement.stewardOrgName" />";
 		var population= "<s:property value="currentDataElement.population.name" />";
 		
 		if(submittingOrgName===null || submittingOrgName===''||stewardOrgName===null || stewardOrgName==='') {
				$("html, body").animate({ scrollTop: 0 }, "fast");
 			return false;
 		}
 		
 		
 		//Attributes required field.
 		if(population===null || population==='') {
				$("html, body").animate({ scrollTop: 0 }, "fast");
 			return false;
 		}
 	
 		
 		submitForm('dataElementAction!submit.action'); 
 	}

	//Sets the navigation menus on the page
	<s:if test="!inAdmin">
		setNavigation({"bodyClass":"primary", "navigationLinkID":"dataDictionaryModuleLink", "subnavigationLinkID":"dataDictionaryToolLink", "tertiaryLinkID":"searchDataElementLink"});
	</s:if>
	<s:else>
		setNavigation({"bodyClass":"primary", "navigationLinkID":"dataDictionaryModuleLink", "subnavigationLinkID":"defineDataToolsLink", "tertiaryLinkID":"manageDataElementsLink"});
	</s:else>
	
	
	var versionedFields = new Array(); 
	<s:if test="%{changeStrings != null && dataElementChangeSeverity != null}">

		<s:iterator var="change" value="changeStrings">
			var str = '<s:property value="#change" />';
			strArray = str.split(":");
			versionedFields.push(strArray[0]);
		</s:iterator>

		$(".label").each(function() {
			
			
			if ($.inArray( $(this).html().split(":")[0], versionedFields ) != -1) {
				$(this).next().css( "border", "1px solid #7AD2EE" );
				$(this).next().css( "padding", "0px 0px 0px 3px" );
			} else if ($(this).html().split(":")[0] == "Keywords" ) {
				if ($.inArray( "Keyword Addition", versionedFields ) != -1) {
					$(this).next().css( "border", "1px solid #7AD2EE" );
					$(this).next().css( "padding", "0px 0px 0px 3px" );
				}
				
			} else if ($(this).html().split(":")[0] == "Label(s)" ) {
				if ($.inArray( "Label Addition", versionedFields ) != -1) {
					$(this).next().css( "border", "1px solid #7AD2EE" );
					$(this).next().css( "padding", "0px 0px 0px 3px" );
				}
			}
		});
		
		if ($.inArray('Category Groups Addition', versionedFields ) != -1) {
			
			$('#categoryGroups').css( "border", "1px solid #7AD2EE" );
			$('#categoryGroups').css( "padding", "3px 0px 0px 3px" );
			$('#categoryGroups').css( "margin", "3px 0px 0px 3px" );
		};
		
		if ($.inArray('Classifications Addition', versionedFields ) != -1) {
			$('#dataElementClassifications').css( "border", "1px solid #7AD2EE" );
			$('#dataElementClassifications').css( "padding", "3px 0px 0px 3px" );
			$('#dataElementClassifications').css( "margin", "3px 0px 0px 3px" );
		}
		
	</s:if>
	
	$('document').ready(function() { 
		initPublication();
			
		$("#generalLabelPlusMinus").text("-");
		$("#basicLabelPlusMinus").text("-");
		$("#classificationsLabelPlusMinus").text("-");
		$("#keywordsLabelPlusMinus").text("-");
		$("#specificLabelPlusMinus").text("-");
		$("#documentationLabelPlusMinus").text("-");

		generalInit();	
		basicInit();
		classificationsInit();
		keywordsInit();
		specificInit();
		documentationInit();
	});
	
	function generalInit() {
		$("#generalLabel").click(function(){
			$("#general").slideToggle("fast");
			if($("#generalLabelPlusMinus").text()=="+") {
				$("#generalLabelPlusMinus").text("- ");
			} else {
				$("#generalLabelPlusMinus").text("+");
			}
		});
	}
	
	function basicInit() {
		$("#basicLabel").click(function(){
			$("#basic").slideToggle("fast");
			if($("#basicLabelPlusMinus").text()=="+") {
				$("#basicLabelPlusMinus").text("- ");
			} else {
				$("#basicLabelPlusMinus").text("+");
			}
		});
	}
	
	function classificationsInit() {
		$("#classificationsLabel").click(function(){
			$("#classifications").slideToggle("fast");
			if($("#classificationsLabelPlusMinus").text()=="+") {
				$("#classificationsLabelPlusMinus").text("- ");
			} else {
				$("#classificationsLabelPlusMinus").text("+");
			}
		});
	}
	
	function keywordsInit() {
		$("#keywordsLabel").click(function(){
			$("#keywords").slideToggle("fast");
			if($("#keywordsLabelPlusMinus").text()=="+") {
				$("#keywordsLabelPlusMinus").text("- ");
			} else {
				$("#keywordsLabelPlusMinus").text("+");
			}
		});
	}
	
	function specificInit() {
		$("#specificLabel").click(function(){
			$("#specific").slideToggle("fast");
			if($("#specificLabelPlusMinus").text()=="+") {
				$("#specificLabelPlusMinus").text("- ");
			} else {
				$("#specificLabelPlusMinus").text("+");
			}
		});
	}
	
	// Called by the delete link.
	// Return to the search page after the element has been deleted.
	function removeDataElement(dataElementName) {
		
		var where_to = confirm("Are you sure you want to delete this data element?");
		if (where_to == true) {
			$.post("dataElementAction!removeDataElement.ajax", {
				dataElementName : dataElementName
			}, function(data) {
				window.location = "searchDataElementAction!list.action";
			});
		}
	}
	
	function documentationInit() {
		$("#documentationLabel").click(function(){
			$("#documentation").slideToggle("fast");
			if($("#documentationLabelPlusMinus").text()=="+") {
				$("#documentationLabelPlusMinus").text("- ");
			} else {
				$("#documentationLabelPlusMinus").text("+");
			}
		});
	}
	
	function initPublication() {
		$.post(	"dataElementAction!viewPublication.ajax", 
			{ }, 
			function (data) {
				$("#publicationDiv").html(data);
			}
		);
	}
	
	//calls clear session to clear the data in session upon cancel
	function cancel() {
		var dataType = '<s:property value="dataType"/>';
		if(dataType=="mapElement") { 
			window.location = "dataStructureElementAction!moveToElements.action";
		} else if(dataType=="dataElement") {
			<s:if test="%{formType == 'create'}">
				window.location = "searchDataElementAction!list.action";
			</s:if>
			<s:else>
				window.location = "dataElementAction!view.action?dataElementId=<s:property value='currentDataElement.id' />";
			</s:else>
		}		
	}
</script>
</body>
</html>