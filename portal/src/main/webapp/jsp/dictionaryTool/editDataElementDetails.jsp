<%@include file="/common/taglibs.jsp"%>
<s:set var="currentPage" value="currentPage" />
<s:set var="currentDataElement" value="currentDataElement" />
<s:set var="formType" value="formType" />

<s:if test="%{formType == 'create'}">
	<title>Create Data Element</title>
</s:if>
<s:elseif test="%{formType == 'edit'}">
	<title>Edit <s:property value="currentDataElement.title" /> Details
	</title>
</s:elseif>



<!-- begin .border-wrapper -->
<div class="border-wrapper">
	<jsp:include page="../navigation/dataDictionaryNavigation.jsp" />
	<div class="">
	
	
	<s:if test="%{formType == 'create'}">
	<h1 class="float-left">Create Data Element</h1>
</s:if>
<s:elseif test="%{formType == 'edit'}">
	<h1 class="float-left">
		<s:if test="inAdmin">(Admin)&nbsp;</s:if>Edit <s:property value="currentDataElement.title" /> Details
	</h1>

</s:elseif>
</div>
	<div style="clear:both;"></div>
	<!--begin #center-content -->
	<div id="main-content" style="margin-top:15px;">
		<s:if test="%{formType == 'edit'}">
			<div id="breadcrumb">
				<s:if test="inAdmin"><s:a action="searchDataElementAction" method="list" namespace="/dictionaryAdmin">Manage Data Elements</s:a></s:if>
				<s:else><s:a action="searchDataElementAction" method="list" namespace="/dictionary">Search Data Elements</s:a></s:else>
				&gt;
				<s:url action="dataElementAction" method="view" var="viewTag">
					<s:param name="dataElementId"><s:property value="currentDataElement.id" /></s:param>
				</s:url>
				<a href="<s:property value="#viewTag" />"><s:property value="currentDataElement.title" /></a> &gt; Edit Data Element
			</div>
		</s:if>

		<s:form id="theForm" cssClass="validate" action="detailsdataElementValidationAction" method="post" validate="true">
				<s:token />
				<s:if test="dataType == 'dataElement'">
					<ndar:dataElementChevron action="detailsdataElementValidationAction" chevron="Step Four" />
				</s:if>
				<s:if test="dataType == 'mapElement'">
					<ndar:dataElementChevron action="detailsmapElementValidationAction" chevron="Step Four" />
				</s:if>

			

			<s:if test="hasActionErrors()">
				<div class="error-message">
					<s:actionerror />
				</div>
			</s:if>

				<p >
					Fields marked with a <span class="required">* </span>are required.
				</p>

			<div class="clear-float">

				<s:if test="!fileErrors.isEmpty()">
					<div class="form-error">
						<s:property value="fileErrors" />
					</div>
				</s:if>

				<h3>Standard Details</h3>

				<!-- <div id="aliasDiv"></div> -->
				<div class="form-field form-field-vert">
					<label for="subon">Submitting Organization Name<span class="required">* </span>:</label>
					<s:textfield label="subon" cssClass="textfield" maxlength="255" name="currentDataElement.submittingOrgName" escapeHtml="true" escapeJavaScript="true" />
					<s:fielderror fieldName="currentDataElement.submittingOrgName" />
					
				</div>
				<div class="form-field form-field-vert">
					<label for="subcn">Submitting Contact Name:</label>
					<s:textfield label="subcn" cssClass="textfield" maxlength="255" name="currentDataElement.submittingContactName" escapeHtml="true" escapeJavaScript="true"  />
					<s:fielderror fieldName="currentDataElement.submittingContactName" />
					
				</div>
								<div class="form-field form-field-vert">
					<label for="subci">Submitting Contact Information:</label>
					<s:textfield label="subci" cssClass="textfield" maxlength="255" name="currentDataElement.submittingContactInfo" escapeHtml="true" escapeJavaScript="true" />
					<s:fielderror fieldName="currentDataElement.submittingContactInfo" />
					
				</div>
				<div class="form-field form-field-vert">
					<label for="son" class="required">Steward Organization Name<span class="required">* </span>:</label>
					<s:textfield label="son" cssClass="textfield" maxlength="255" name="currentDataElement.stewardOrgName" escapeHtml="true" escapeJavaScript="true" />
					<s:fielderror fieldName="currentDataElement.stewardOrgName" />
					
				</div>
				<div class="form-field form-field-vert">
					<label for="scn">Steward Contact Name:</label>
					<s:textfield label="scn" cssClass="textfield" maxlength="255" name="currentDataElement.stewardContactName" escapeHtml="true" escapeJavaScript="true" />
					<s:fielderror fieldName="currentDataElement.stewardContactName" />
					
				</div>
				<div class="form-field form-field-vert">
					<label for="sci">Steward Contact Information:</label>
					<s:textfield label="sci" cssClass="textfield" maxlength="255" name="currentDataElement.stewardContactInfo" escapeHtml="true" escapeJavaScript="true" />
					<s:fielderror fieldName="currentDataElement.stewardContactInfo" />
					
				</div>
				<hr class="underline">
				<div class="form-field form-field-vert">
					<label for="effdate">Effective Date:</label>
					<s:textfield id="effdate" label="effdate" cssClass="textfield" maxlength="255" name="currentDataElement.effectiveDateString" escapeHtml="true" escapeJavaScript="true" />
					<s:fielderror fieldName="currentDataElement.effectiveDateString" />
					
				</div>
							<div class="form-field form-field-vert">
					<label for="untildate">Until Date:</label>
					<s:textfield id="untildate" label="untildate" cssClass="textfield" maxlength="255" name="currentDataElement.untilDateString" escapeHtml="true" escapeJavaScript="true" />
					<s:fielderror fieldName="currentDataElement.untilDateString" />
					
				</div>
				<div class="form-field form-field-vert">
					<label for="seealso">See Also:</label>
					<s:textfield label="seealso" cssClass="textfield" maxlength="1000" name="currentDataElement.seeAlso" escapeHtml="true" escapeJavaScript="true" />
					<s:fielderror fieldName="currentDataElement.seeAlso" />
					
				</div>
	<!--  									SCHEMA PV CODES	 were added to the import functionality. This is an admin only functionality that 
											associates PV with external PVs. Per REQ-491 and REQ-493 this functionality should be removed from the
											edit/create page.
				<hr class="underline">
				
								<h4>External ID</h4>
			
				
				<div class="form-field form-field-vert">
					<label for="loinc">LOINC ID:</label>
					<s:textfield label="loinc" cssClass="textfield" maxlength="100" 
						name="externalIdMap['%{@gov.nih.tbi.commons.model.ExternalType@LOINC}']" escapeHtml="true" escapeJavaScript="true" />
					<s:fielderror fieldName="externalIdMap['%{@gov.nih.tbi.commons.model.ExternalType@LOINC}']" />
					<div class="special-instruction">
						<a href="http://loinc.org/" target="_blank">http://loinc.org/</a>
					</div>
				</div>
				<div class="form-field form-field-vert">
					<label for="cadsr">caDSR ID:</label>
					<s:textfield label="cadsr" cssClass="textfield" maxlength="100" 
						name="externalIdMap['%{@gov.nih.tbi.commons.model.ExternalType@CADSR}']" escapeHtml="true" escapeJavaScript="true" />
					<s:fielderror fieldName="externalIdMap['%{@gov.nih.tbi.commons.model.ExternalType@CADSR}']" />
					<div class="special-instruction">
						<a href="https://cabig.nci.nih.gov/concepts/caDSR/" target="_blank">https://cabig.nci.nih.gov/concepts/caDSR/</a>
					</div>
				</div>
								<div class="form-field form-field-vert">
					<label for="snomed">SNOMED ID:</label>
					<s:textfield label="snomed" cssClass="textfield" maxlength="100" 
						name="externalIdMap['%{@gov.nih.tbi.commons.model.ExternalType@SNOMED}']" escapeHtml="true" escapeJavaScript="true" />
					<s:fielderror fieldName="externalIdMap['%{@gov.nih.tbi.commons.model.ExternalType@SNOMED}']" />
					<div class="special-instruction">
						<a href="http://www.nlm.nih.gov/research/umls/Snomed/snomed_main.html" target="_blank">http://www.nlm.nih.gov/research/umls/Snomed/snomed_main.html</a>
					</div>
				</div>
				<div class="form-field form-field-vert">
					<label for="cdisc">CDISC ID:</label>
					<s:textfield label="cdisc" cssClass="textfield" maxlength="100" 
						name="externalIdMap['%{@gov.nih.tbi.commons.model.ExternalType@CDISC}']" escapeHtml="true" escapeJavaScript="true" />
					<s:fielderror fieldName="externalIdMap['%{@gov.nih.tbi.commons.model.ExternalType@CDISC}']" />
					<div class="special-instruction">
						<a href="http://www.cdisc.org/standards/" target="_blank">http://www.cdisc.org/standards/</a>
					</div>
				</div>
				<div class="form-field form-field-vert">
					<label for="ninds">NINDS ID:</label>
					<s:textfield label="ninds" cssClass="textfield" maxlength="100" 
						name="externalIdMap['%{@gov.nih.tbi.commons.model.ExternalType@NINDS}']" escapeHtml="true" escapeJavaScript="true" />
					<s:fielderror fieldName="externalIdMap['%{@gov.nih.tbi.commons.model.ExternalType@NINDS}']" />
					<div class="special-instruction">
						<a href="http://www.ninds.nih.gov/" target="_blank">http://www.ninds.nih.gov/</a>
					</div>
				</div>  -->
				
				
				
				<div class="form-field clear-left">
					<div class="button">
							<s:if test="#editableDiseases" >
								<input type="button" value="Continue"
									onClick="javascript:submitForm('details${dataType}ValidationAction!review.action')" />
							</s:if>
							<s:else>
								<input type="button" value="Continue"
									onClick="javascript:submitForm('details${dataType}ValidationAction!review.action')" />
							</s:else>
					</div>
					<s:if test="%{formType=='edit'}">
							<s:if test="#editableDiseases" >
								<a class="form-link" href="javascript:submitForm('details${dataType}ValidationAction!review.action')">Review</a>
							</s:if>
							<s:else>
								<a class="form-link" href="javascript:submitForm('multiDiseaseElementValidationAction!review.action')">Review</a>
							</s:else>
					</s:if>
					<a class="form-link" href="javascript:cancel()">Cancel</a>
				</div>
		</s:form>
	</div>
	<!-- end of #main-content -->
</div>
</div>
<!-- end of .border-wrapper -->



<script type="text/javascript">
	<s:if test="!inAdmin">
		<s:if test="%{formType == 'create'}">
			setNavigation({"bodyClass":"primary", "navigationLinkID":"dataDictionaryModuleLink", "subnavigationLinkID":"dataDictionaryToolLink", "tertiaryLinkID":"dataElementLink"});
		</s:if>
		<s:else>
			setNavigation({"bodyClass":"primary", "navigationLinkID":"dataDictionaryModuleLink", "subnavigationLinkID":"dataDictionaryToolLink", "tertiaryLinkID":"searchDataElementLink"});
		</s:else>
	</s:if>
	<s:else>
		setNavigation({"bodyClass":"primary", "navigationLinkID":"dataDictionaryModuleLink", "subnavigationLinkID":"defineDataToolsLink", "tertiaryLinkID":"manageDataElementsLink"});
	</s:else>
	
	
	
	
	
	function submitBeforeDocumentation(action)
	{
		$.ajax({
			url: action,
			data: $("form").serializeArray(),
			success: function() {}
		});
	}
	
	$('document').ready(function() 
		{ 
			displayAlias();
			
			
			$( "#effdate").datepicker({
			      showOn: "button",
			      buttonImage: "../images/calendar.gif",
			      buttonImageOnly: true,
			      dateFormat: "yy-mm-dd",
			    });
			$("#untildate").datepicker({
			      showOn: "button",
			      buttonImage: "../images/calendar.gif",
			      buttonImageOnly: true,
			      dateFormat: "yy-mm-dd",
			      minDate: 0
			    });
		}
	);
	
	function displayAlias() {
		$.post(	"aliasAction!input.ajax", 
			{}, 
				function (data) {
					$("#aliasDiv").html(data);
				}
		);
	}
	
	function createAlias() {
		$.ajax({
			url: "aliasAction!create.ajax",
			data: $("form").serializeArray(),
			success: function(data) {
				$("#aliasDiv").html(data),
				$("#aliasField").val('');
			}
		});
	}
	
	function removeAlias( name ) {
		
		$.post(	"aliasAction!remove.ajax", 
				{ aliasName:name }, 
					function (data) { 
						$("#aliasDiv").html(data);
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
