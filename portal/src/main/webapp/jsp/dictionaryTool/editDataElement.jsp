<%@include file="/common/taglibs.jsp"%>
<s:set var="currentPage" value="currentPage" />
<s:set var="currentDataElement" value="currentDataElement" />
<s:set var="formType" value="formType" />
<s:bean name="gov.nih.tbi.dictionary.model.DataElementForm"
	var="dataElementForm" />

<s:if test="%{formType == 'create'}">
	<title>Create Data Element</title>
</s:if>
<s:elseif test="%{formType == 'edit'}">
	<title>Edit <s:property value="currentDataElement.title" /> Details</title>
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
				<s:if test="inAdmin">(Admin)&nbsp;</s:if>Edit
				<s:property value="currentDataElement.title" />
				Details
			</h1>

		</s:elseif>
	</div>
	<div style="clear: both;"></div>
	<!--begin #center-content -->
	<div id="main-content"  style="margin-top:15px;">
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
					<s:param name="dataElementId">
						<s:property value="currentDataElement.id" />
					</s:param>
				</s:url>
				<a href="<s:property value="#viewTag" />"><s:property
						value="currentDataElement.title" /></a> &gt; Edit Data Element
			</div>
		</s:if>

		<s:form id="theForm" cssClass="validate" accept-charset="UTF-8" action="basicdataElementValidationAction" method="post" validate="true">
			<s:token />
				<s:if test="dataType == 'dataElement'">
					<ndar:dataElementChevron
						action="basicdataElementValidationAction" chevron="Edit Details" />
				</s:if>
				<s:if test="dataType == 'mapElement'">
					<ndar:dataElementChevron action="detailsmapElementValidationAction"
						chevron="Edit Details" />
				</s:if>


			<s:if test="hasActionErrors()">
				<div class="error-message">
					<s:actionerror />
				</div>
			</s:if>

			<p>A data element is a logical unit of data, pertaining to
				information of one kind. A data element has a name, precise
				definition, and clear enumerated values (codes) if applicable. A
				data element is not necessarily the smallest unit of data; it can be
				a unique combination of one or more smaller units. A data element
				occupies the space provided by field(s) on a paper/electronic case
				report form (CRF) or field(s) in a database record.</p>

			<s:if test="%{formType == 'create'}">
				<p>Fill out the details below to create a data element. On the
					following pages, you may define the type of data captured and
					associate keywords.</p>
			</s:if>
			<s:elseif test="%{formType == 'edit'}">
				<p>Edit the details for your data element below. On the
					following pages, you may define the type of data captured and
					associate keywords.</p>
			</s:elseif>
			<p>
				Fields marked with a <span class="required">* </span>are required.
			</p>

			<div class="clear-float">

				<s:if test="!fileErrors.isEmpty()">
					<div class="form-error">
						<s:property value="fileErrors" />
					</div>
				</s:if>

				<h3>General Details</h3>


				<div class="clear-right"></div>
				<s:hidden name="dataElementForm.id" label="ID" escapeHtml="true" escapeJavaScript="true" />

				<div class="form-field form-field-vert">
					<label class="required">Element Type<span class="required">*
					</span>:
					</label> 

					<sec:authorize access="!hasAnyRole('ROLE_DICTIONARY_ADMIN','ROLE_ADMIN')">

						<s:if test="%{currentDataElement.category==null}">
							Unique Data Element
							<input type="hidden" id="uncommon" name="dataElementForm.category" value="UDE" />
						</s:if>
						<s:else>
							<s:property value="currentDataElement.category.name" />
							<s:if test="%{#currentDataElement.category.shortName == 'UDE'}">
								<input type="hidden" name="dataElementForm.category" value="UDE" />
							</s:if>
							<s:elseif test="%{#currentDataElement.category.shortName == 'CDE'}">
								<input type="hidden" name="dataElementForm.category" value="CDE" />
							</s:elseif>
							<s:else>
								<input type="hidden" name="dataElementForm.category" value="UDE" />
							</s:else>
						</s:else>

					</sec:authorize>

					<sec:authorize access="hasAnyRole('ROLE_DICTIONARY_ADMIN','ROLE_ADMIN')">

						<ul class="checkboxgroup required">
							<li><s:if test="%{currentDataElement.category.shortName=='CDE'}">
									<input type="radio" name="dataElementForm.category" id="uncommon" class="radio" value="UDE" />
									<label for="uncommon">Unique Data Element</label>
									<input checked="yes" type="radio" name="dataElementForm.category" id="common" class="radio" value="CDE" />
									<label for="common">Common Data Element</label>
								</s:if></li>
							<li><s:else>
									<input checked="yes" type="radio"
										name="dataElementForm.category" id="uncommon" class="radio"
										value="UDE" />
									<label for="uncommon">Unique Data Element</label>									
									<input type="radio" name="dataElementForm.category" id="common"
										class="radio" value="CDE" />
									<label for="common">Common Data Element</label>
								</s:else></li>
						</ul>

					</sec:authorize>


					<s:fielderror fieldName="dataElementForm.category" />
					<div style="clear: both;"></div>
				</div>

				<div class="form-field form-field-vert">
					<label for="dataElementForm.title" class="required">Title<span
						class="required">*</span>:
					</label> 
					<s:textarea id="dataElementForm.title"
						name="dataElementForm.title" cssClass="textfield required" escapeHtml="true" escapeJavaScript="true" />
					<s:fielderror fieldName="dataElementForm.title" />
					<div class="special-instruction">Readable Name</div>
				</div>

				<div class="form-field form-field-vert">
					<label for="dataElementForm.name" class="required">Variable
						Name <span class="required">* </span>:
					</label> 
					<s:textfield id="dataElementForm.name" name="dataElementForm.name"
						cssClass="textfield required" maxlength="100" escapeHtml="true" escapeJavaScript="true" />
					<s:fielderror fieldName="dataElementForm.name" />
					<div class="special-instruction">Variable Name & Aliases are
						valid and unique names. It must start with an alphabet character,
						can only contains Alphanumeric and Underscores, and must be 30
						characters or less.</div>
				</div>

				<div class="form-field form-field-vert">
					<label for="dataElementForm.shortDescription" class="required">Short
						Description <span class="required">* </span>:
					</label> 
					<s:textarea id="dataElementForm.shortDescription"
						cssClass="textfield required" cols="60" rows="4"
						name="dataElementForm.shortDescription" escapeHtml="true" escapeJavaScript="true" />
					<s:fielderror fieldName="dataElementForm.shortDescription" />
				</div>

				<div class="form-field form-field-vert">
					<label for="dataElementForm.description">Definition:</label>
					<s:textarea id="dataElementForm.description"
						name="dataElementForm.description" cols="60" rows="4"
						cssClass="textfield" escapeHtml="true" escapeJavaScript="true" />
					<s:fielderror fieldName="dataElementForm.description" />
				</div>

				<div class="form-field form-field-vert">
					<label for="dataElementForm.notes">Notes:</label>
					<s:textarea label="notes" cols="60" rows="4" cssClass="textfield"
						name="dataElementForm.notes" escapeHtml="true" escapeJavaScript="true" />
					<s:fielderror fieldName="dataElementForm.notes" />
				</div>
				<div class="form-field form-field-vert">
					<label for="dataElementForm.historicalNotes">Historical
						Notes:</label>
					<s:textarea label="historical-notes" cols="60" rows="4"
						cssClass="textfield" name="dataElementForm.historicalNotes" escapeHtml="true" escapeJavaScript="true" />
					<s:fielderror fieldName="dataElementForm.historicalNotes" />
				</div>

				<div class="form-field form-field-vert">
					<label for="dataElementForm.references">References:</label>
					<s:textarea label="references" cols="60" rows="4"
						cssClass="textfield" name="dataElementForm.references" escapeHtml="true" escapeJavaScript="true" />
					<s:fielderror fieldName="dataElementForm.references" />
				</div>

				<!--  new page or move to page -->
				<div class="form-field clear-left">
					<div class="button">
						<input type="button" value="Continue"
								onClick="javascript:submitForm('basic${dataType}ValidationAction!editDocumentation.action')" />
					</div>
					<s:if test="%{formType=='edit'}">				
						<a class="form-link" href="javascript:submitForm('basic${dataType}ValidationAction!review.action')">Review
									</a>
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
	var globalToken;
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
	
	
	
	$("#domainSelect").change(function()
	{
		$("#dataElementForm\\.subdomain").val("");
		<s:if test="currentMapElement != null">
			submitForm("mapElementAction!domainUpdate.action#classificationHeader");
		</s:if>
		<s:else>
			submitForm("dataElementAction!domainUpdate.action#classificationHeader");
		</s:else>
	});
	
	function submitBeforeDocumentation(action)
	{
		globalToken = $('[name="token"]').val();
		$.ajax({
			url: action,
			data: $("form").serializeArray(),
			success: function() {}
		});
	}
	
	$('document').ready(function() 
		{ 
			displayAlias();
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
