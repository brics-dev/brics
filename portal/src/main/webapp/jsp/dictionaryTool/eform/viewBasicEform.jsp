<%@include file="/common/taglibs.jsp"%>

<title>eForm: <s:property escapeHtml="true" value="sessionEform.basicEform.title" /></title>

<div class="border-wrapper">
	<!-- Left menus -->
	<div class="">
		<jsp:include page="../../navigation/dataDictionaryNavigation.jsp" />
		<h1 class="float-left"><s:if test="inAdmin">(Admin)&nbsp;</s:if>Data Dictionary</h1>
		<div style="clear:both;"></div>
	</div>

	<!-- 		The pages main content -->
	<div id="main-content" style="margin-top:15px;">
	<div id="leftContent" style="float:left; width:75%; margin-right:20px;">

		<h2 class="pageHeader">
			eForm:&nbsp; <s:property escapeHtml="true" value="sessionEform.basicEform.title" /> 
		</h2>
		
		<s:if test="sessionEform.basicEform.isCAT"><%-- added by Ching-Heng--%>
			<s:if test="sessionEform.basicEform.measurementType != 'shortForm'">
				<div id="msg" class="alt-error">
					Adaptive Instrument: This instrument is a computer adaptive test (CAT), 
				   so its questions are generated dynamically based on answers given.</br> Because it is dynamic, 
				   it can only be taken in survey form, and all fields below are thus permanently locked and uneditable.
				</div>
			</s:if>
		</s:if>
		
		<hr>
		<s:form id="theForm" cssClass="validate" method="post" validate="true" enctype="multipart/form-data">
			<s:token />
			<s:if test="sessionEform.basicEform.status.id == 1 && hasAdminPermission">
				<div id="adminFun">
					<h3>Publication Request:</h3>
					<p>The user has requested the publication of the following eForm. Please review the form structure details.</p>
						
						<div class="form-field">
							<label for="reason" class="required">Approval/Denial Reason <span class="required">* </span>:
							</label>
							<s:textarea label="reason" cols="60" rows="4" cssClass="textfield required" id="reason" name="reason" escapeHtml="true" escapeJavaScript="true" />
							<s:fielderror fieldName="reason" />
							<div class="special-instruction">
								<p>An email will be sent to the user with the above message included in the body.</p>
							</div>
						</div>
				</div>
			</s:if>
		</s:form>
		
		<h3 id="generalLabel" class="clear-both collapsable">
			<span id="generalLabelPlusMinus"></span>&nbsp;General Details
		</h3>
		<div id="general">
			<div class="clear-right"></div>
			<div class="form-output">
				<s:label cssClass="label" key="Title:" />
				<div class="readonly-text">
					<a href="/portal/dictionary/eformDetailedViewAction!viewFormDetail.ajax?eformId=<s:property value="sessionEform.basicEform.id" />" target="_blank"><s:property value="sessionEform.basicEform.title" /></a>
				</div>
			</div>
			
			<div class="form-output">
				<s:label cssClass="label" key="Short Name:" />
				<div class="readonly-text">
					<s:property escapeHtml="true" escapeJavaScript="true" value="sessionEform.basicEform.shortName" />
				</div>
			</div>
			
			<div class="form-output">
				<s:label cssClass="label" key="Description:" />
				<div class="readonly-text">
					<s:property escapeHtml="true" escapeJavaScript="true" value="sessionEform.basicEform.description" />
					<br/>
					<s:if test="sessionEform.basicEform.isCAT"><%-- added by Ching-Heng--%>
						For information on how to interpret CAT scores, see <a href=" https://www.assessmentcenter.net/manuals.aspx" target="_blank">Scoring Manuals</a>
						</br>
						For information on PROMIS scoring tables, see <a href=" http://www.healthmeasures.net/promis-scoring-manuals" target="_blank">Scoring Tables</a>
					</s:if>
				</div>
			</div>
			
			<div class="form-output">
				<s:label cssClass="label" key="eForm Type:" />
				<div class="readonly-text">
					<s:if test="sessionEform.basicEform.IsShared">
						<div class="readonly-text">Standard</div>
					</s:if>
					<s:else>
						<div class="readonly-text">Non-Standard</div>
					</s:else>
				</div>
			</div>
			
			<div class="form-output">
				<s:label cssClass="label" key="Publication Date:" />
				<div class="readonly-text">
					<s:property value="sessionEform.basicEform.datePublishedString" />
				</div>
			</div>
			
			<div class="form-output">
				<s:label cssClass="label" key="Date Created:" />
				<div class="readonly-text">
					<s:property value="sessionEform.basicEform.dateCreatedString" />
				</div>
			</div>
			
			<div class="form-output">
				<s:label cssClass="label" key="Created By:" />
				<div class="readonly-text">
					<s:property escapeHtml="true" escapeJavaScript="true" value="sessionEform.basicEform.createBy" />
				</div>
			</div>
			
			<div class="form-output">
				<div class="label">Owner:</div>
				<div class="readonly-text">
					<s:property value="ownerName" />
				</div>
			</div>
			
			<s:if test="sessionEform.basicEform.isCAT"><%-- added by Ching-Heng--%>
				<div class="form-output">
					<div class="label">Acknowledgement:</div>
					<div class="readonly-text">
						PROMIS Health Organization and Assessment Center <sup>SM</sup>: <a href="https://assessmentcenter.net/documents/Assessment%20Center%20Terms%20and%20Conditions%20v7.1.pdf" target="_blank">View full acknowledgement</a>
					</div>
				</div>
				<div class="form-output">
					<div class="label">Terms of Use:</div>
					<div class="readonly-text">
						You understand and agree that the PROMIS Health Organization and PROMIS Cooperative Group 
							provides access to PROMIS instruments (e.g., item banks, short forms, profile measures) subject
							 to the PROMIS Terms and Conditions (PTAC). The PROMIS Health Organization/Cooperative Group reserves 
							 the right to update the PTAC at any time. Changes in the 
							 PTAC will apply to new<a href="https://www.assessmentcenter.net/documents/PROMIS%20Terms%20and%20Conditions%20v8.1.pdf" target="_blank">...show more</a></span>
					</div>
				</div>
			</s:if>
			
			<div class="form-output">
				<s:label cssClass="label" key="Associated Form Structure:" />
				<div class="readonly-text">
					<a class="lightbox" href="/portal/dictionary/dataStructureAction!lightboxView.ajax?dataStructureName=<s:property value="sessionEform.basicEform.formStructureShortName" />"><s:property value="sessionEform.basicEform.formStructureShortName" /></a>
				</div>
			</div>
		</div>
		<br><br>
		
		<div class="button">
				<input type="button" value="Close"
					onClick="javascript:window.location='eFormSearchAction!list.action'" />
		</div>		

	</div>
	</div>
		<s:if test="%{nameSpace != 'publicData'}">
			<div id="actionsBar">
				<div id="publicationDiv">
					<jsp:include page="eformActionBar.jsp" />
				</div>
			</div>
		</s:if>		
</div>

<script type="text/javascript">

<s:if test="!inAdmin">
setNavigation({"bodyClass":"primary", "navigationLinkID":"dataDictionaryModuleLink", "subnavigationLinkID":"dataDictionaryToolLink", "tertiaryLinkID":"searchEformLink"});
</s:if>
<s:else>
setNavigation({"bodyClass":"primary", "navigationLinkID":"dataDictionaryModuleLink", "subnavigationLinkID":"defineDataToolsLink", "tertiaryLinkID":"searchEformLink"});
</s:else>

$('document').ready(function() { 
	
	$("#generalLabelPlusMinus").text("-");
	generalInit();	
});

function publication(publicationId) {
	$.post(	"EformReasonValidationAction!changePublication.ajax", 
		{ publicationId:publicationId, reason: $('#reason').val() }, 
		function (data) {
			$("#publicationDiv").html(data);
			//updatePublicationStatus();
			if(publicationId == 1) {
				$("#adminFun").show();
			} else {
				$("#adminFun").hide();
			}
			
		}
	);
}

function publicationDecision(publicationId){
	$('<input />').attr('type', 'hidden')
    .attr('name', "publicationId")
    .attr('value', publicationId)
    .appendTo('#theForm');
	submitForm('eformReasonValidationAction!publicationDecision.action');
}

function approve(publicationId){
	$('<input />').attr('type', 'hidden')
    .attr('name', "publicationId")
    .attr('value', publicationId)
    .appendTo('#theForm');
	if(publicationId === 2){
		submitForm('eformReasonValidationAction!approvePublication.action');
	} else {
		submitForm('eFormUpdateStatusAction!approvePublication.action');
	}
}

function publish(publicationId){
	$('<input />').attr('type', 'hidden')
    .attr('name', "publicationId")
    .attr('value', publicationId)
    .appendTo('#theForm');
	submitForm('eFormUpdateStatusAction!approvePublication.action');
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

function displayExportLegacyFormError() {
	$.ibisMessaging("dialog", "warning", "This form is a legacy form and cannot be exported.", {container: "body"});
}
</script>
</body>
</html>