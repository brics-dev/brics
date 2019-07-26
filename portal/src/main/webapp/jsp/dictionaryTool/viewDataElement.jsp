<%@include file="/common/taglibs.jsp"%>
<%@ page contentType="text/html; charset=UTF-8" %>
<title><s:property escapeHtml="true" value="currentDataElement.title" /></title>

<s:set var="dataType" value="dataType" />
<s:set var="currentMapElement" value="currentMapElement" />
<s:set var="currentDataElement" value="currentDataElement" />
<s:set var="deletable" value="deletable" />
<s:set var="attachedDataStructures" value="attachedDataStructures" />
<s:set var="user" value="user" />

<input type="hidden" name="currentId" id="currentId" value="<s:property value="currentDataElement.id" />" />
<input type="hidden" name="requestedStatusChange" id="requestedStatusChange" value="<s:property value="isRequestedStatusChange" /> "/>
<input type="hidden" name="inAdmin" id="inAdmin" value="<s:property value="inAdmin" /> "/>
<input type="hidden" name="currentAction" id="currentAction" value="dataElementAction"/>

<div class="clear-float">
	<jsp:include page="dictionaryStatusChange.jsp"></jsp:include>
</div>

<div class="border-wrapper">
	<s:if test="%{nameSpace != 'publicData'}">
		
		<sec:authorize access="hasAnyRole('ROLE_ADMIN', 'ROLE_DICTIONARY_ADMIN', 'ROLE_DICTIONARY')">
			<s:if test="inAdmin">
				<div id="breadcrumb">
					<s:a action="searchDataElementAction" method="list">Manage Data Elements</s:a>&nbsp;&gt;&nbsp;<s:property escapeHtml="false" value="currentDataElement.title" />
				</div>
			</s:if>
			<s:else>
				<div id="breadcrumb">
					<s:a action="searchDataElementAction" method="list">Search Data Elements</s:a>&nbsp;&gt;&nbsp;<s:property escapeHtml="false" value="currentDataElement.title" />
				</div>
			</s:else>
		</sec:authorize>
			
		<!-- Left menus -->
		<div class="">
			<jsp:include page="../navigation/dataDictionaryNavigation.jsp" />
			
			<h1 class="float-left"><s:if test="inAdmin">(Admin)&nbsp;</s:if>Data Dictionary</h1>
			<div style="clear:both;"></div>
		</div>
	</s:if>

	<!-- 		The pages main content -->
	<div id="main-content" style="margin-top:15px;">
	
	<div id="leftContent" style="float:left; width:100%;">

		<h2 class="pageHeader">
			<s:property value="currentDataElement.category.name" />:&nbsp;
			<c:if test="${ currentDataElement.title != '' }"><s:property escapeHtml="false" value="currentDataElement.title" /></c:if>
		</h2>
		
		<hr>
		
		<p>Listed below are the details for the data element.</p>
		
		<h3 id="generalLabel" class="clear-both collapsable">
			<span id="generalLabelPlusMinus"></span>&nbsp;General Details
		</h3>
		<div id="general">
			<div class="clear-right"></div>
			<div class="form-output">
				<s:label cssClass="label" key="Version:" />
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
					<s:property escapeHtml="true" value="currentDataElement.description" />
				</div>
			</div>
			
			<div class="form-output">
				<div class="label">Notes:</div>
				<div class="readonly-text limitLength" id="notes">
					<s:property escapeHtml="true" value="currentDataElement.notes" />
				</div>
			</div>
			
			<div class="form-output">
				<div class="label">Creation Date:</div>
				<div class="readonly-text limitLength" id="createdate">
					<fmt:formatDate type="date" dateStyle="MEDIUM" value="${currentDataElement.dateCreated}" pattern="yyyy-MM-dd" />
				</div>
			</div>
			<s:if test="%{nameSpace != 'publicData'}">
				<div class="form-output">
					<div class="label">Created By:</div>
					<div class="readonly-text limitLength" id="createdBy">
						<s:property escapeHtml="false" value="currentDataElement.createdBy" />
					</div>
				</div>
			</s:if>

			<div class="form-output">
				<div class="label">Historical Notes:</div>
				<div class="readonly-text limitLength" id="historical-notes">
					<s:property escapeHtml="true" value="currentDataElement.historicalNotes" />
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
			<div class="clear-right"></div>
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
				<div class="form-output">
					<div class="label">Pre-Defined Values:</div>
					<div class="readonly-text">
						<div id="dataElementPvContainer" class="idtTableContainer">
							<table class="display-data table table-striped table-bordered" id="dataElementPvTable"></table>
						</div>
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
			
			<div class="form-output">
				<div class="readonly-text underLabel"><a href="schemaMappingAction!viewSchemaMappingValues.action?dataElement=<s:property value="currentDataElement.name" />">External Schema Permissible Value Mapping</a></div>
			</div>
			
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
					<s:property escapeHtml="true" value="currentDataElement.suggestedQuestion" />
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
		<br />
			
		<!--  next box  -->
		<h3 id="classificationsLabel" class="clear-both collapsable">
			<span id="classificationsLabelPlusMinus"></span>&nbsp;Category Groups and Classifications
		</h3>
		<div id="classifications">
			<div class="clear-right"></div>
			<div class="form-output">
				<div class="label">Category Groups and Classifications:</div>
				<div class="readonly-text">
			 <table border="1"  class="display-data">
            <tr><th width="25%">Disease</th><th width="25%">Domain</th><th width="25%">Sub-Domain</th><!--  <th width="25%">Classification</th> --> </tr>
            	<s:iterator var="diseaseElement" value="currentDataElement.subDomainElementList" status="diseaseStatus">
            	<tr>
            	<td><strong><c:out value="${diseaseElement.disease.name}" /></strong></td>
            	<td><strong><c:out value="${diseaseElement.domain.name}" /></strong></td>
            	<td><strong><c:out value="${diseaseElement.subDomain.name}" /></strong></td>
            	
			
            	</tr></s:iterator></table> 
            	
			</div>
			</div>
			
			<ndar:dataElementClassification admin="false" readOnly="true" /> 
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
				<div class="label">Labels:</div>
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
			<div class="form-output">
				<div class="label">Effective Date:</div>
				<div class="readonly-text limitLength" id="effdate">
					<fmt:formatDate type="date" dateStyle="MEDIUM" value="${currentDataElement.effectiveDate}" pattern="yyyy-MM-dd" />
				</div>
			</div>
			<div class="form-output">
				<div class="label">Until Date:</div>
				<div class="readonly-text limitLength" id="untildate">
					<fmt:formatDate type="date" dateStyle="MEDIUM" value="${currentDataElement.untilDate}" pattern="yyyy-MM-dd" />
				</div>
			</div>
			<div class="form-output">
				<div class="label">Last Change Date:</div>
				<div class="readonly-text limitLength" id="modifieddate">
					<fmt:formatDate type="date" dateStyle="MEDIUM" value="${currentDataElement.modifiedDate}" pattern="yyyy-MM-dd" />
				</div>
			</div>
			<div class="form-output">
				<div class="label">See Also:</div>
				<div class="readonly-text limitLength" id="seealso">
					<s:property value="currentDataElement.seeAlso" />
				</div>
			</div>
			<div class="form-output">
				<div class="label">Submitting Organization Name:</div>
				<div class="readonly-text limitLength" id="subon">
					<s:property value="currentDataElement.submittingOrgName" />
				</div>
			</div>
			<div class="form-output">
				<div class="label">Submitting Contact Name:</div>
				<div class="readonly-text limitLength" id="subcn">
					<s:property value="currentDataElement.submittingContactName" />
				</div>
			</div>
			<div class="form-output">
				<div class="label">Submitting Contact Information:</div>
				<div class="readonly-text limitLength" id="subci">
					<s:property value="currentDataElement.submittingContactInfo" />
				</div>

			</div>
			<div class="form-output">
				<div class="required label">Steward Organization
					Name:</div>
				<div class="readonly-text limitLength" id="son">
					<s:property value="currentDataElement.stewardOrgName" />
				</div>
			</div>
			<div class="form-output">
				<div class="label">Steward Contact Name:</div>
				<div class="readonly-text limitLength" id="scn">
					<s:property value="currentDataElement.stewardContactName" />
				</div>

			</div>
			<div class="form-output">
				<div class="label">Steward Contact Information:</div>
				<div class="readonly-text limitLength" id="sci">
					<s:property value="currentDataElement.stewardContactInfo" />
				</div>

			</div>
			<%-- <div class="form-output">
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
			</div>--%>
			<div class="form-output">
				<div class="label">NINDS ID:</div>
				<div class="readonly-text limitLength" id="ninds">
					<s:property value="nindsId" />
				</div>
			</div> 
		</div>
		<br>
			
		<!--  next box  -->
		<h3 id="historyLabel" class="clear-both collapsable">
			<span id="historyLabelPlusMinus"></span>&nbsp;Change History
		</h3>
		<div id="history">
			<div class="clear-right"></div>
			<jsp:include page="historyLogList.jsp">
		        <jsp:param name="idtUrl" value="dataElementAction!getHistoryLogList.action"/>
		    </jsp:include>
		</div>
		<br>
		
		<!---- Admin Change History ---->
		<h3 id="adminHistoryLabel" class="clear-both collapsable">
			<span id="adminHistoryLabelPlusMinus"></span>&nbsp;Administrative Change History
		</h3>
		<div id="adminHistory">
			<div class="clear-right"></div>
			<jsp:include page="dictionaryEventLogList.jsp">
		        <jsp:param name="idtUrl" value="dataElementAction!getDictionaryEventLogList.action"/>
		    </jsp:include>
		</div>
		<br>
		
		
		<!--  next box  -->
		<h3 id="linkedFormLabel" class="clear-both collapsable">
			<span id="linkedFormLabelPlusMinus"></span>&nbsp;Linked Form Structures
		</h3>
		<div id="linkedForm">
	
			<!-- Calls the attachedDataStructure.jsp page which draws the table of attached data structures. -->
			<jsp:include page="attachedDataStructure.jsp" />
			<br/>
		</div>
		<br>

	</div>
	</div>

	<s:if test="%{nameSpace != 'publicData'}">
		<div id="actionsBar">
			<div id="publicationDiv"></div>
			
			<ul>

			<!-- Add the edit and delete links. -->
			<!-- First Check public namespace -->
			<s:if test="%{nameSpace != 'publicData'}">
			<!-- Admins only (or owner if status is not published) -->
			<s:if test="(isDataElementAdmin && !isPublished) || isDictionaryAdmin">
				<!-- The form also has to be the latest version -->
				<s:if test="%{isLatestVersion}">
					
						<!-- This if statement is a holdover from when MapElements were DataElements and is no longer required (most likely) -->
						<!-- //TODO: The whole dataType variable can probably be removed with caution -->
						<s:if test="%{dataType == 'dataElement'}">
						<li>
							<span class="icon"><img alt="Edit" src="<s:url value='/images/icons/edit.png' />" style="padding-right:10px;" height="15px" width="15px" /></span><a href="dataElementAction!edit.action?dataElementName=${currentDataElement.name}" >Edit</a>
							</li>
						</s:if>
				
					
					<!-- An additional requirenment for delete is that it not be attached to any form structures. -->
					<c:if test="${(fn:length(allAttachedDataStructures)==0) && (dataType == 'dataElement')}">
					<li>
					<span class="icon"><img alt="Delete" src="<s:url value='/images/icons/delete.png' />" style="padding-right:10px;" height="15px" width="15px" /></span><a href="javascript:void(0);"  onclick="javascript:removeDataElement('${currentDataElement.name}')">Delete</a><br>
						</li>
					</c:if>
				</s:if>
			</s:if>
			</s:if>
			
			
			
			<s:if test="%{nameSpace != 'publicData'}">
			
		<div class="">
			<s:if test="isGuest">
			<li>
			<a href="/${portalRoot}/jsp/define/index.jsp" >Close</a>
				</li>
			</s:if>
			<s:elseif test="dsId == null">
			<li>
			<a href="/portal/dictionary/searchDataElementAction!list.action"> Close </a>
				</li>
			</s:elseif>
			<s:else>
			<li>
			<a href="/portal/dictionary/dataStructureAction!view.action?dataStructureId=${dsId}">Close</a>
			</li>
			</s:else>
		</div>
		
		</s:if>
		
		</ul>	
		</div>
		
	</s:if>
			
</div>

<script type="text/javascript" src="/portal/js/statusChange/dictionaryStatusChange.js"></script>
<script type="text/javascript">

	//Sets the navigation menus on the page
	<s:if test="!inAdmin">
		setNavigation({"bodyClass":"primary", "navigationLinkID":"dataDictionaryModuleLink", "subnavigationLinkID":"dataDictionaryToolLink", "tertiaryLinkID":"searchDataElementLink"});
	</s:if>
	<s:else>
		setNavigation({"bodyClass":"primary", "navigationLinkID":"dataDictionaryModuleLink", "subnavigationLinkID":"defineDataToolsLink", "tertiaryLinkID":"manageDataElementsLink"});
	</s:else>

	
	$('document').ready(function() { 
		<s:if test="%{nameSpace != 'publicData'}">
			initPublication();
		</s:if>
		
		$("#generalLabelPlusMinus").text("-");
		$("#basicLabelPlusMinus").text("-");
		$("#documentationLabelPlusMinus").text("+");
		$("#documentation").hide();
		$("#classificationsLabelPlusMinus").text("+");
		$("#classifications").hide();
		$("#keywordsLabelPlusMinus").text("+");
		$("#keywords").hide();
		$("#specificLabelPlusMinus").text("+");
		$("#specific").hide();
		$("#historyLabelPlusMinus").text("+");
		$("#history").hide();
		$("#linkedFormLabelPlusMinus").text("+");
		$("#linkedForm").hide();
		$("#adminHistoryLabelPlusMinus").text("+");
		$("#adminHistory").hide();

		generalInit();	
		basicInit();
		classificationsInit();
		keywordsInit();
		specificInit();
		historyInit();
		linkedFormInit();
		documentationInit();
		adminHistoryInit();
	
		$("#dataElementPvTable").idtTable({
			autoWidth: false,
			dom : 'frtip',
			pageLength: 5,
			columns: [
				{
					data: "valueRange",
					title: "Permissible Value",
					name: "valueRange",
					width: "20%",
					render: IdtActions.ellipsis(45)
				},
				{
					data: "description",
					title: "Description",
					name: "description",
					width: "20%",
					render: IdtActions.ellipsis(45)
				},
				{
					data: "outputCode",
					title: "Output Code",
					name: "outputCode",
					width: "20%"
				}
			],
		     data: [
		      <s:iterator value="currentDataElement.valueRangeList" var="valueRange">
	              {
	                "valueRange": "<s:property value='valueRange' />",
	                "description": "<s:property value='description' />",
	                "outputCode": "<s:property value='outputCode' />"
	              },
              </s:iterator>
	      ]			
		});
	});
	

	function ellipsisExpandCollapse(element) {
		var $this = $(element);
		$this.parent().toggle();
		if ($this.text() == "...") {
			$this.parent().next().toggle();
		}
		else {
			$this.parent().prev().toggle();
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
	
	function linkedFormInit() {
		$("#linkedFormLabel").click(function(){
			$("#linkedForm").slideToggle("fast");
			if($("#linkedFormLabelPlusMinus").text()=="+") {
				$("#linkedFormLabelPlusMinus").text("- ");
			} else {
				$("#linkedFormLabelPlusMinus").text("+");
			}
		});
	}
	
	function historyInit() {
		
		$("#historyLabel").click(function(){			
			$("#history").slideToggle("fast");
			if($("#historyLabelPlusMinus").text()=="+") {
				$("#historyLabelPlusMinus").text("- ");
			} else {
				$("#historyLabelPlusMinus").text("+");
			}
			
		});
		
	}
	function adminHistoryInit() {
		
		$("#adminHistoryLabel").click(function(){
			
			var dictionaryAdmin = '<s:property value="isDictionaryAdmin" />';
			
			if(dictionaryAdmin!=='true'){
				alert("You do not have the correct permissions to view this section.");
				return;
			}
				
			$("#adminHistory").slideToggle("fast");
			if($("#adminHistoryLabelPlusMinus").text()=="+") {
				$("#adminHistoryLabelPlusMinus").text("- ");
			} else {
				$("#adminHistoryLabelPlusMinus").text("+");
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
	
	function initPublication() {
		$.post(	"dataElementAction!viewPublication.ajax", 
			{ }, 
			function (data) {
				
				$("#publicationDiv").html(data);
			}
		);
	}
	
	<sec:authorize access="hasAnyRole('ROLE_ADMIN', 'ROLE_DICTIONARY_ADMIN', 'ROLE_DICTIONARY')">
		function publication(publicationId) {
			
			var inAdmin = $('#inAdmin').val().trim();
			
			if(inAdmin == 'false'){
				publicationAction(publicationId);
			}
			else{
				saveHistoryDeChangeStatus(publicationId);
			}
			
		}
		
		function approvePublication(){
			saveHistoryDeChangeStatus(2);
		}
		
		function denyPublication(){
			saveHistoryDeChangeStatus(0);
		}
		
		function publicationAction(publicationId){
			updatePublicationInterface();
			$.post(	"dataElementAction!changePublication.ajax", 
					{ publicationId:publicationId }, 
					function (data) {
					
						$("#publicationDiv").html(data);
						//refreshEventLogTable();
						var dt = $("#eventTable").idtApi('getTableApi');
						//dt.draw();
						$("#eventTable").DataTable().ajax.reload();						
						reEnableLinks();
						
					}
				);
			
		
			
		}
	</sec:authorize>
	
	
</script>
</body>
</html>