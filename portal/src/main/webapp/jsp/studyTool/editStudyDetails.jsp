<%@include file="/common/taglibs.jsp"%>

<s:if test="%{isCreate}">
	<title>Create Study</title>
</s:if>
<s:else>
	<title>Edit Study: ${sessionStudy.study.title}</title>
</s:else>

<style>
.remainder-message {
	font-size: 12px;
	clear: both;
	margin-left: 10px;
}

#researchMgmtError .error-message, #researchMgmtError .error-text {
	width: 500px;
}

#researchMgmtError .error-text {
	float: none;
}
</style>
<!-- begin .border-wrapper -->
<div class="border-wrapper">
	<jsp:include page="../navigation/dataRepositoryNavigation.jsp" />
	<h1>Data Repository</h1>
	<div style="clear: both"></div>

	<!--begin #center-content -->
	<div id="main-content">

		<s:if test="%{!isCreate}">
			<div id="breadcrumb">
				<a href="studyAction!list.action">View Studies</a> &nbsp;&gt;&nbsp;<a
					href="viewStudyAction!view.action?studyId=${sessionStudy.study.prefixedId}">${sessionStudy.study.title}</a>
				&nbsp;&gt;&nbsp;Edit Details
			</div>
		</s:if>
		<s:if test="%{isCreate}">
			<h2>Create Study</h2>
		</s:if>

		<s:form id="theForm" cssClass="validate" method="post"
			name="studyDetailsForm" validate="true" enctype="multipart/form-data">
			<s:if test="hasFieldErrors()">
				<input id="hasFieldErrors" value="true" type="hidden" />
			</s:if>
			<s:token />
			<s:if test="%{!isCreate}">
				<ndar:editStudyChevron action="studyValidationAction"
					chevron="Edit Details" />
				<h2>Edit Study: ${sessionStudy.study.title}</h2>
			</s:if>

			<s:if test="hasActionErrors()">
				<div class="error-message">
					<s:actionerror />
				</div>
			</s:if>
			<s:if test="%{isCreate}">
				<p>Please enter your study information below. Once you've
					completed the mandatory fields, click the submit button. Your study
					request will be reviewed by a system administrator. Once the system
					administrator approves your request, you can start submitting data
					to the study.</p>
			</s:if>
			<s:else>
				<p>Edit the details of your study below. On the following pages,
					you may manage documentation, datasets, and permissions.</p>
			</s:else>

			<p class="required">
				Fields marked with a <span class="required">* </span>are required.
				You will not be able to submit your study for approval until all
				required fields are answered.
			</p>
			<div class="clear-right"></div>

			<h3 id="overViewLabel" class="clear-both collapsable">
				<span id="overViewLabelPlusMinus"></span>&nbsp;Study Overview
			</h3>
			<div id="overView">
				<jsp:include page="editStudyOverview.jsp" />
			</div>

			<h3 id="researchMgmtLabel" class="clear-both collapsable">
				<span id="researchMgmtLabelPlusMinus"></span>&nbsp;Study Research
				Management
			</h3>
			<div id="researchMgmt">

				<div id="hideResearchMgmt">
					<span class="remainder-message"> <b>Please Click "ADD TO
							TABLE" button to add attribute to the Research Management Table.</b>
					</span>
				</div>

				<div id="researchMgmtError"
					style="width: 500px; margin-left: 10px; font-size: 12px;">
					<s:fielderror>
						<s:param>sessionStudy.study.primaryPI</s:param>
					</s:fielderror>
				</div>
				<jsp:include page="editStudyResearchMgmt.jsp" />
			</div>

			<h3 id="studyInfoLabel" class="clear-both collapsable">
				<span id="studyInfoLabelPlusMinus"></span>&nbsp;Study Information
			</h3>
			<div id="studyInfo">
				<jsp:include page="editStudyInfo.jsp" />
			</div>

			<h3 id="formsLabel" class="clear-both collapsable">
				<span id="formsLabelPlusMinus"></span>&nbsp;Study Form Structure
			</h3>
			<div id="forms">
				<jsp:include page="editStudyForms.jsp" />
			</div>

			<h3 id="keywordsLabel" class="clear-both collapsable">
				<span id="keywordsLabelPlusMinus"></span>&nbsp;Study Keywords
			</h3>
			<div id="keywords" style="padding-left: 50px;">
				<jsp:include page="editStudyKeywords.jsp" />
			</div>

			<s:if test="%{isCreate}">
				<br>
				<br>
				<h3>Approved Data Submission Document</h3>
				<p>Your study cannot be approved until you upload the required
					documentation. Please upload your Approved Submission document.</p>
				<div class="form-field" style="display: inline">
					<label for="uploadField" style="white-space: nowrap;"
						class="required">Data Submission Document <span
						class="required">* </span>:
					</label>&nbsp;&nbsp;
					<s:file name="upload" id="uploadField"
						cssStyle="float:left; padding-left:10px" />
					<s:hidden id="uploadFileName" name="uploadFileName" />
					<s:fielderror fieldName="uploadFileName" />
				</div>

				<br>
				<br>
				<div class="form-field clear-left">
					<div class="button">
						<input type="button" value="Submit Request"
							onClick="javascript:submitForm('studyValidationAction!submit.action')" />
					</div>
					<a class="form-link"
						href="javascript:window.location.href='/portal/study/studyAction!list.action'">Cancel</a>
				</div>
			</s:if>
			<s:else>
				<div class="form-field clear-left">
					<div class="button">
						<input type="button" value="Continue"
							onClick="javascript:submitForm('studyValidationAction!moveToDocumentation.action')" />
					</div>
					<a class="form-link"
						href="javascript:submitForm('studyValidationAction!submit.action')">Save
						&amp; Finish</a> <a class="form-link"
						href="viewStudyAction!view.action?studyId=${sessionStudy.study.prefixedId}">Cancel</a>
				</div>
			</s:else>
		</s:form>
	</div>
</div>

<script type="text/javascript"
	src='<s:url value="/js/uploadDocumentations.js"/>'></script>
<script type="text/javascript">
	<s:if test="!inAdmin" >
		<s:if test="isCreate">
			setNavigation({"bodyClass":"primary", "navigationLinkID":"dataRepositoryModuleLink", "subnavigationLinkID":"studyToolLink", "tertiaryLinkID":"createStudyLink"});
		</s:if>
		<s:else>
			setNavigation({"bodyClass":"primary", "navigationLinkID":"dataRepositoryModuleLink", "subnavigationLinkID":"studyToolLink", "tertiaryLinkID":"browseStudyLink"});
		</s:else>
	</s:if>
	<s:else>
		setNavigation({"bodyClass":"primary", "navigationLinkID":"dataRepositoryModuleLink", "subnavigationLinkID":"contributeDataToolsLink", "tertiaryLinkID":"studyList"});
	</s:else>


	$('document').ready( function() { 
		
		$("#overViewLabelPlusMinus").text("-");
		
		<s:if test="hasFieldErrors()">
		   
  		    $("#hideResearchMgmt").hide();
		    $("#researchMgmtLabelPlusMinus").text("-");
			$("#studyInfoLabelPlusMinus").text("-");
			$("#formsLabelPlusMinus").text("-");
			$("#keywordsLabelPlusMinus").text("-");
			
		</s:if>
		<s:else>
	
			$("#researchMgmtLabelPlusMinus").text("+");
			$("#studyInfoLabelPlusMinus").text("+");
			$("#formsLabelPlusMinus").text("+");
			$("#keywordsLabelPlusMinus").text("+");
			
			$("#researchMgmt").hide();
			$("#studyInfo").hide();
			$("#forms").hide();
			$("#keywords").hide();
		</s:else>
		
		overviewInit();
		researchMgmtInit();
		studyInfoInit();
		formsInit();
		keywordsInit();
 
		    
		var hasFieldErrors = $("input#hasFieldErrors").val();//console.log("hasFieldErrors: "+hasFieldErrors);
		if(hasFieldErrors == "true" && $('#uploadFileName').val() != ""){ //console.log("error true: "+$('#uploadFileName').val());
			convertFileUpload($('#uploadField'), $('#uploadFileName').val());						
		}
	});
 
	function overviewInit() {
		$("#overViewLabel").click( function() {
			$("#overView").slideToggle("fast");
			if($("#overViewLabelPlusMinus").text() == "+") {
				$("#overViewLabelPlusMinus").text("-");
			} else {
				$("#overViewLabelPlusMinus").text("+");
			}
		});
	}
	
	function researchMgmtInit() {
		$("#researchMgmtLabel").click( function() {
			$("#researchMgmt").slideToggle("fast");
			if($("#researchMgmtLabelPlusMinus").text() == "+") {
				$("#researchMgmtLabelPlusMinus").text("-");
			} else {
				$("#researchMgmtLabelPlusMinus").text("+");
			}
		});
	}
	
// 	function researchMgmtMessageInit() {
		
// 	}

	function studyInfoInit() {
		$("#studyInfoLabel").click( function() {
			$("#studyInfo").slideToggle("fast");
			if($("#studyInfoLabelPlusMinus").text() == "+") {
				$("#studyInfoLabelPlusMinus").text("-");
			} else {
				$("#studyInfoLabelPlusMinus").text("+");
			}
		});
	}

	function formsInit() {
		$("#formsLabel").click( function() {
			$("#forms").slideToggle("fast");
			if($("#formsLabelPlusMinus").text() == "+") {
				$("#formsLabelPlusMinus").text("-");
			} else {
				$("#formsLabelPlusMinus").text("+");
			}
		});
	}

	function keywordsInit() {
		$("#keywordsLabel").click( function() {
			$("#keywords").slideToggle("fast");
			if($("#keywordsLabelPlusMinus").text() == "+") {
				$("#keywordsLabelPlusMinus").text("-");
			} else {
				$("#keywordsLabelPlusMinus").text("+");
			}
		});
	}
	
	function submitForm(action) {
		selectAllCurrentKeywords();
		var theForm = document.getElementById('theForm');
		theForm.action = action;
		theForm.submit();
	}

	
</script>
