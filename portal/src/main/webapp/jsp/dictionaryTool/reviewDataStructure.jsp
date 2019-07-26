<%@include file="/common/taglibs.jsp"%>
<%@page contentType="text/html; charset=UTF-8" %>
<title>Form Structure: <s:property value="currentDataStructure.title" /></title>

<s:set var="reviewStructure" value="1" />
<style type="text/css">

 .setFontSize{
 font-size:1.3em;
 }
</style>

<div class="clear-float">
	
</div>


<div class="border-wrapper">
	<div style="clear:both;"></div>
	<s:if test="%{nameSpace != 'publicData'}">
		<jsp:include page="../navigation/dataDictionaryNavigation.jsp" />
		<h1 class="float-left"><s:if test="inAdmin">(Admin)&nbsp;</s:if>Data Dictionary</h1>
		<div style="clear:both;"></div>
	</s:if>
	
	<div id="main-content" style="margin-top:15px;">
		<form name="dataStructureForm">
			<s:hidden name="formStructureChangeSeverity" value="%{formStructureChangeSeverity.severityLevel}"/>
			
			<div id="leftContent" style="float:left; width:100%;">
				<ndar:dataStructureChevron action="dataStructureAction" chevron="Review Structure" />
				<s:if test="%{currentDataStructure.status.type == 'Published'}">
				
					<div class="form-output" style="border:1px solid #d6d6d6; margin:5px 0px; padding:5px;">
							<p><b>Change History</b><font color="red">*</font><s:fielderror fieldName="auditNote" />
							<br>
							Please enter your reasons for making changes to the Form Structure.</p>
							
							<div class="readonly-text">
								<center>
									<s:textarea cols="125%" rows="10%" id="auditNote" name="auditNote"/>
								</center>
							</div>
					</div>
				</s:if>
				<div class="clear-both"></div>
			
				<s:if test="%{!sessionDataStructure.newStructure && !sessionDataStructure.draftCopy}">
					<s:if test="%{shortNameChangeRequired}">
						<div style="min-height: 15px; border: 1px solid #ff0000; padding: 5px; margin-bottom: 5px;">
							<p>
								<span class="icon"><img alt="information"
									src="<s:url value='/images/error-small.gif' />"
									style="padding-right: 10px;" height="15px" width="15px" /></span>You
								must change the Short Name to create a new Form Structure. <a
									href="javascript:void(0);"
									onClick="submitDataStructureForm('dataStructureAction!moveToDetails.action');">Change
									Short Name</a>
							</p>
						</div>
					</s:if>
				</s:if>

				<s:if test="%{changeStrings != null && formStructureChangeSeverity != null}">
				<div id="versioningMessageBox" class="clear-both">
					<s:if test="%{formStructureChangeSeverity.severityLevel == 'new'}">
The following changes will result in a new Form Structure. Please review and click the save button to generate a new Form Structure.
The Form Structure you were editing will remain as is.
					</s:if>
					<s:elseif test="%{formStructureChangeSeverity.severityLevel == 'minor'}">
The following changes will result in a minor change to the Form Structure. Please review and click the save button to generate a new version of the Form Structure.
					</s:elseif>
					<s:elseif test="%{formStructureChangeSeverity.severityLevel == 'major'}">
The following changes will result in a major change to the Form Structure. Please review and click the save button to generate a new version of the Form Structure.
					</s:elseif>
					
					<s:iterator var="change" value="changeStrings">
					<p><span class="icon"><img alt="information" src="<s:url value='/images/icons/info.png' />" style="padding-right:10px;" height="15px" width="15px" /></span>
						<s:property value="#change" /></p>
					</s:iterator>
					</div>
				</s:if>
			
		<h1 style="margin-left:0px;">Review</h1>
		<h2 class="pageHeader">Form Structure: <s:property value="currentDataStructure.title" /></h2>
		
		<s:set var="currentDataStructure" value="currentDataStructure" />
		<s:set var="sessionDataStructure" value="sessionDataStructure" />
		<s:set var="createdHistory" value="createdHistory" />
		<s:set var="repeatableGroups" value="currentDataStructure.repeatableGroups" />
		<s:set var="ownerName" value="ownerName" />
		
		<p>Listed below are the details of the form structure.</p>

		<h3 id="generalLabel" class="clear-both collapsable">
			<span id="generalLabelPlusMinus"></span>&nbsp;General Details
		</h3>
		<div id="general">
		
			<div class="form-output">
				<div class="label">Title:</div>
				<div class="readonly-text">
					<s:property value="currentDataStructure.title" />
				</div>
			</div>
			<div class="form-output">
				<div class="label">Short Name:</div>
				<div class="readonly-text">
					<s:property value="currentDataStructure.shortName" />
				</div>
			</div>
			<div class="form-output">
				<div class="label">Description:</div>
				<div id="description" class="readonly-text limitLength">
					<s:property value="currentDataStructure.rDFDescription" />
				</div>
			</div>
			<div class="form-output">
				<div class="label">Disease:</div>
					<div class="readonly-text">
						<s:iterator var="diseaseStructure" value="currentDataStructure.diseaseList" status="diseaseStatus">
							<c:out value="${diseaseStructure.disease.name}" />
							<s:if test="!#diseaseStatus.last">,</s:if>
						</s:iterator>
					</div>
				</div>
			<s:if test="currentDataStructure.organization!=''">
				<div class="form-output">
					<div class="label">Organization:</div>
					<div class="readonly-text">
						<s:property value="currentDataStructure.organization" />
					</div>
				</div>
			</s:if>

			<div class="form-output">
				<div class="label">Required Program Form:</div>
				<s:if test="%{isRequired}">
					<div class="readonly-text">
						<c:out value="Yes" />
					</div>
				</s:if>
				<s:else> 
					<div class="readonly-text">
						<c:out value="No" />
					</div>
				</s:else>
			</div>
			
			<div class="form-output">
				<div class="label">Standardization:</div>
				<div class="readonly-text">
					<s:property value="currentDataStructure.standardization.display" />
				</div>
			</div>
			
			<div class="form-output">
				<div class="label">Form Type:</div>
				<div class="readonly-text">
					<s:property value="currentDataStructure.fileType.type" />
				</div>
			</div>

			<s:if test="currentDataStructure.documentationUrl!=null">
				<div class="form-output">
					<div class="label">Documentation:</div>
					<div class="readonly-text">
						<a href="<s:property value="currentDataStructure.documentationUrl" />" target="_blank"><s:property
								value="currentDataStructure.documentationUrl" /></a>
					</div>
				</div>
			</s:if>
			<s:if test="currentDataStructure.publicationDate!=null">
				<div class="form-output">
					<div class="label">Publication Date:</div>
					<div class="readonly-text">
					
								<ndar:dateTag value="${currentDataStructure.publicationDate}" />
					
					</div>
				</div>
			</s:if>
			<div class="form-output">
				<div class="label">Version:</div>
				<div class="readonly-text">
					<s:property value="currentDataStructure.version" />
				</div>
			</div>
			<s:if test="%{sessionDataStructure.newStructure}">
			<div class="form-output">
				<div class="label">Date Created:</div>
				<div class="readonly-text">
					<ndar:dateTag value="${currentDate}" />
				</div>
			</div>
			<div class="form-output">
				<div class="label">Created By:</div>
				<div class="readonly-text">
					<s:property value="user.fullName" />
				</div>
			</div>
			</s:if>
			
			<s:if test="%{!sessionDataStructure.newStructure}">
			<div class="form-output">
				<div class="label">Date Created:</div>
				<div class="readonly-text">
					<ndar:dateTag value="${createdHistory.modifiedDate}" />
				</div>
			</div>
			<div class="form-output">
				<div class="label">Created By:</div>
				<div class="readonly-text">
					<s:property value="createdHistory.modifiedUser.fullName" />
				</div>
			</div>
			</s:if>
			<div class="form-output">
				<div class="label">Owner:</div>
				<div class="readonly-text">
					<s:property value="ownerName" />
				</div>
			</div>
			<div class="form-output">
				<div class="label">Number of Data Elements:</div>
				<div class="readonly-text">
					<c:out value="${fn:length(sessionDataElementList.mapElements)}" />
				</div>
			</div>
			
			<div class="form-output">
				<s:if test="currentDataStructure.isCopyrighted">
					<div class="readonly-text">This form structure is copyrighted</div>
				</s:if>
				<s:else>
					<div class="readonly-text">This form structure is not copyrighted</div>
				</s:else>
			</div>
		</div>
		<br/>
		
		<h3 id="documentationLabel" class="clear-both collapsable">
			<span id="documentationLabelPlusMinus"></span>&nbsp;Documentation
		</h3>
		<div id="documentation">
			<jsp:include page="dictionaryDocumentationList.jsp">
		        <jsp:param name="idtUrl" value="dataStructureAction!getSupportingDocumentationList.action"/>
		    </jsp:include>
		</div>
		
		<!-- - next box -->

		<h3 id="dataElementsLabel" class="clear-both collapsable">
			<span id="dataElementsLabelPlusMinus"></span>&nbsp;Groups & Attached Data Elements
		</h3>
		
		<div id="dataElements">
			<div class="clear-right"></div>
			Logically grouped data elements with defined frequency at which they repeat.<br/><br/>
			<div id="elementTable">
				<jsp:include page="dataStructure/reviewAttachedDataElements.jsp" />
			</div>
		</div>
		<br/>

		<!-- next box  -->
		<s:if test="%{currentDataStructure.id != null}">
			<h3 id="historyLabel" class="clear-both collapsable">
				<span id="historyLabelPlusMinus"></span>&nbsp;Change History
			</h3>
			<div id="history">
				<div class="clear-right"></div>
				<jsp:include page="historyLogList.jsp">
			        <jsp:param name="idtUrl" value="dataStructureAction!getHistoryLogList.action"/>
			    </jsp:include>
			</div>
		</s:if>
		<br/>

		<s:if test="!shortNameChangeRequired">
			<s:if test="%{currentDataStructure.status.type == 'Published'}">
				<ndar:dataStructureSave action="reviewDataStructureValidationAction" method="moveToPermissions" lastButtonSet="true" />
			</s:if>
			<s:else>
				<ndar:dataStructureSave action="dataStructureAction" method="moveToPermissions" lastButtonSet="true" />
			</s:else>
		</s:if>
		<s:else>
			<ndar:dataStructureSave action="dataStructureAction" method="moveToDetails" shortNameNeededSet="true"/>
		</s:else>
	

	<!--  end left side -->
	</div>

	</form>

	<!--  end main content -->
	</div>
	
</div>

<script type="text/javascript">

var versionedFields = new Array(); 
<s:if test="%{changeStrings != null && formStructureChangeSeverity != null}">

	<s:iterator var="change" value="changeStrings">
		var str = '<s:property value="#change" />';
		strArray = str.split(":");
		versionedFields.push(strArray[0]);
	</s:iterator>
	
	$(".label").each(function() {
		
		if ($.inArray( $(this).html().split(":")[0], versionedFields ) != -1){
			$(this).next().css( "border", "1px solid #7AD2EE" );
			$(this).next().css( "padding", "0px 0px 0px 3px" );
		}
		
		if($.inArray('Disease Removal', versionedFields ) != -1 || $.inArray('Disease Addition', versionedFields ) != -1){
			if($(this).html().split(":")[0] == "Disease"){
				$(this).next().css( "border", "1px solid #7AD2EE" );
				$(this).next().css( "padding", "0px 0px 0px 3px" );
				
			}
		}
	});
		
	if ($.inArray('Element Groups | List Item', versionedFields ) != -1) {
			
			$('#elementTable').css( "border", "1px solid #7AD2EE" );
			$('#elementTable').css( "padding", "3px 0px 0px 3px" );
	}
	
</s:if>


	var currentState;
	
	$('document').ready( function() {
		$("#documentationLabelPlusMinus").text("-");
		$("#generalLabelPlusMinus").text("-");
		$("#dataElementsLabelPlusMinus").text("-");
		$("#historyLabelPlusMinus").text("-");

		generalInit();	
		dataElementsInit();
		historyInit();
		documentationInit();
	});
	
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

	function dataElementsInit() {
		$("#dataElementsLabel").click(function(){
			$("#dataElements").slideToggle("fast");
			if($("#dataElementsLabelPlusMinus").text()=="+") {
				$("#dataElementsLabelPlusMinus").text("- ");
			} else {
				$("#dataElementsLabelPlusMinus").text("+");
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
	
	<s:if test="fromRepository">
		setNavigation({"bodyClass":"primary", "navigationLinkID":"dataRepositoryModuleLink", "subnavigationLinkID":"contributeDataToolsLink", "tertiaryLinkID":"repositoryList"});
	</s:if>
	<s:elseif test="inAdmin">
		setNavigation({"bodyClass":"primary", "navigationLinkID":"dataDictionaryModuleLink", "subnavigationLinkID":"defineDataToolsLink", "tertiaryLinkID":"manageDataStructuresLink"});
	</s:elseif>
	<s:else>
		setNavigation({"bodyClass":"primary", "navigationLinkID":"dataDictionaryModuleLink", "subnavigationLinkID":"dataDictionaryToolLink", "tertiaryLinkID":"listDataStructureLink"});
	</s:else>
	

</script>