<jsp:include page="/common/doctype.jsp"/>
<%@ page import="gov.nih.nichd.ctdb.protocol.domain.Protocol"%>
<%@ page import="gov.nih.nichd.ctdb.common.CtdbConstants"%>
<%@ page import="gov.nih.nichd.ctdb.attachments.manager.AttachmentManager"%>
<%@ page import="gov.nih.nichd.ctdb.security.domain.Privilege" %>
<%@ page import="gov.nih.nichd.ctdb.security.domain.User"%>
<%@ page import="java.util.Date"%>
<%@ page import="java.util.List"%>
<%@ page import="java.util.Locale"%>
<%@ page import="gov.nih.nichd.ctdb.common.rs" %>
<%@ taglib uri="/struts-tags" prefix="s"%>
<%@ taglib uri="/WEB-INF/security.tld" prefix="security"%>
<%@ taglib uri="/WEB-INF/display.tld" prefix="display"%>


<%-- CHECK PRIVILEGES --%>
<security:check privileges="addeditpatients"/>

<%
	Protocol curProtocol = (Protocol) session.getAttribute(CtdbConstants.CURRENT_PROTOCOL_SESSION_KEY);
	request.setAttribute(CtdbConstants.USES_TABS, false);
	int SUBJECT_DISPLAY_TYPE = curProtocol.getPatientDisplayType();
	request.setAttribute("SUBJECT_DISPLAY_TYPE", SUBJECT_DISPLAY_TYPE);
	Locale l = request.getLocale();
	User user = (User) session.getAttribute(CtdbConstants.USER_SESSION_KEY);
	Privilege manageAttachmentsPriv = new Privilege("manageAttachments") ;
	Privilege viewAuditPriv = new Privilege("viewaudittrails") ;
	int protocalId = curProtocol.getId();
%>

<html>
<style>
#guidBtrisDataSearchDiv {
  width: 100%;
  padding: 5px 0;
  text-align: center;
  background-color: lightblue;
  margin-top: 10px;
}
</style>
<s:set var="pageTitle" scope="request">
	<s:if test="%{(patientId != null) && (patientId > 0)}">
		<s:text name="patient.edit.title.display" /> 
		<s:property value="patientForm.firstName"/> <s:property value="patientForm.lastName" />
	</s:if>
	<s:else>
		<s:text name="patient.add.title.display" />
	</s:else>
</s:set>
<s:set var="pageTitle" scope="request">
	<s:text name="subject.table.title.display"/>
</s:set>
<jsp:include page="/common/header_struts2.jsp" />
<s:set var="disallowingPII" value="#systemPreferences.get('guid_with_non_pii')" />
<s:set var="actionName" value="%{#context['struts.actionMapping'].name}"></s:set>
<s:set var="guidWsUrl" value="#systemPreferences.get('webservice.centralizedguid.base.url')" />

<script src="<s:property value="#webRoot"/>/common/js/guidClient.js" type="text/javascript"></script>
<script src="<s:property value="#webRoot"/>/common/js/btris.js" type="text/javascript"></script>

<script type="text/javascript">
	var DISALLOWING_PII = '<s:property value="disallowingPII" />';
 	var ATT_ADDING_MODE = true;
 	var SUBJECT_DISPLAY_TYPE = <%=SUBJECT_DISPLAY_TYPE%>;
 	var guidWsBaseUrl = '<s:property value="guidWsUrl" />';
 	var attachMap = new HashTable();
 	
 	function resetAttachmentForm ($form) {
	    $form.find("input:text, input:file, textarea").val("");
	    $form.find("input:radio:checked, input:checkbox:checked").prop("checked", false);
	    $form.find("#attachmentId").val(<%= Integer.MIN_VALUE %>);
	    $form.find("#associateId").val("");
	    $form.find("#fileName").val("");
	    $("#attachmenCategory option:first").prop("selected", true);
	}
 	
 	function validatePatient() {
 		var errmsg = "";
		
		// Clear any primary messages
		$("#messageContainer").empty();
		
		
		if ($.trim($("input#guid").val()).length == 0) { 
			errmsg += "<s:text name='errors.required'><s:param><s:text name='patient.guid.display'/></s:param>" +
			"<s:param>is</s:param></s:text><br>";
		}
		
		if ( $.trim($("input#subjectId").val()).length == 0) { 
			errmsg += "<s:text name='errors.required'><s:param><s:text name='patient.label.SubjectID'/></s:param>" +
				"<s:param>is</s:param></s:text><br>";
		}
		
		if ($.trim($("input#mrn").val()).length == 0 && SUBJECT_DISPLAY_TYPE == 5 ) { 
			errmsg += "<s:text name='errors.required'><s:param><s:text name='patient.mrn.display'/></s:param>" +
				"<s:param>is</s:param></s:text><br>";
		}
		
		if (DISALLOWING_PII == 0) {
			if ($.trim($("input#mrn").val()).length == 0) { 
				errmsg += "<s:text name='errors.required'><s:param><s:text name='patient.mrn.display'/></s:param>" +
					"<s:param>is</s:param></s:text><br>";
			}
			if ($.trim($("input#firstName").val()).length == 0) { 
				errmsg += "<s:text name='errors.required'><s:param><s:text name='patient.firstname.display'/></s:param>" +
					"<s:param>is</s:param></s:text><br>";
			}
			if ($.trim($("input#lastName").val()).length == 0) { 
				errmsg += "<s:text name='errors.required'><s:param><s:text name='patient.lastname.display'/></s:param>" +
					"<s:param>is</s:param></s:text><br>";
			}
		}

		// skip unnecessary GUID validation if any missing required fields(s)
		if (errmsg.length > 0) {
			$.ibisMessaging("primary", "error", errmsg);
			return false;
		}
		
		if ($.trim($("input#guid").val()).length > 0 && !IS_GUID_VALIDATED) {
			$("input#btnValidateGUID").trigger("click");
			if (!IS_GUID_VALIDATED) { // the invalid icon will be displayed, instead of error message
				return false;
			}
		}
				
		$("input#guid").prop("disabled", false);
 		
 		return true;
 	}
 	
 	
	$(document).ready(function() {

		initGuid(DISALLOWING_PII, '<s:property value="actionName" />');
		
		// Populate the attachment map
		var attachArray = $.parseJSON($("#attachListStr").val());
		
		for ( var i = 0; i < attachArray.length; i++ ) {
			attachMap.setItem(attachArray[i].id, attachArray[i]);
		}
		
		$("#currProtocolCheckbox").click(function(event) {
			// If the assoicate to study checkbox is not checked, disable all study related input fields.
			if( !$(this).is(':checked') ) {
				$("#studySection").find('input, select').not("#currProtocolCheckbox").prop("disabled", true);
				$("#associated2Study").val("false");
			}
			// Enable all study releated input fields if the assoicate to study checkbox is checked.
			else {
				$("#studySection").find("input, select").prop("disabled", false);
				$("#associated2Study").val("true");
			}
		});
		

		 $("#btnAddAttachment,#btnEditAttachment").click(function(event) {
			 var $attachSection = $("#subjectAttachmentSection");
			 
			// Disable submit button to prevent extra clicking
			$(this).prop("disabled", true);
			
			$("input[name='patientForm.sectionDisplay']").val("attachments");
			
		    if ( !ATT_ADDING_MODE ) {
		    	$("#editForm").attr("action", "editAttachment.action"); // Set the form to use the "editAttachment" struts action.
				ATT_ADDING_MODE = true;
		    }
		    else {
		    	$attachSection.find("#attachmentId").val(<%=Integer.MIN_VALUE%>);
		    	$("#editForm").attr("action", "addAttachment.action"); // Set the form to use the "addAttachment" struts action.
		    }
		    
			$("input#guid").prop("disabled", false);
			$("#editForm").submit(); // without this explict submit(), no action class is invoked with Chrome browser.
 		});
			 
		$("#btnCancelAttachment").click(function() {
			var $attachSection = $("#subjectAttachmentSection");
			var $table = $attachSection.find("table");
			var $downloadLink = $attachSection.find("#downloadLink");
			
			// Reset the download link if it was hidden
			if ( !$downloadLink.is(":visible") ) {
				$attachSection.find("#fileNameDisplay").text("").hide();
				$downloadLink.show();
				$downloadLink.text("");
			}
			
			resetAttachmentForm($attachSection.find("#divPatientAttachmentForms"));
			$attachSection.find("#btnAddAttachment").show();
			$attachSection.find("#btnEditAttachment").hide();
			$attachSection.find("#divPatientAttachments input").not("input:button, input:submit").prop("disabled", false);
			ATT_ADDING_MODE = true;
			IDT.clearSelected($table);
			$table.find("tr").removeClass("row_selected");
			$attachSection.find(".attConditional").hide();
	        $attachSection.find("#attachmenCategory option:first").prop("selected", true);
		});
		
	
		$("#attaCategories").click(function() {
			$("#categoryAdd").html('<iframe src="<s:property value="#webRoot"/>/attachments/attachmentCategory.action?_typeId=2" width="990" height="600"/ seamless >');
			$("#categoryAdd").dialog({
				title: "<s:text name='attachmentcategory.editTitle.display'/>",
				width: 1010,
				modal: true,
				
				buttons: [
					{
						text: "<s:text name='button.Close'/>",
						click: function() {
							$(this).dialog("close");
						}
					}
				],
				
				close: function() {
					var cateid = $("select[name='attachment.category.id']").val();
					
					$.ajax({
						url: "<s:property value='#webRoot'/>/attachments/attachmentCategoryList.action",
						type: 'GET',
						data: {'categoryid': cateid},
						
					    success: function(resp) {
					    	var $select = $("#attachmenCategory");
					    	
					    	// Reset the options in the attachment category select box.
					    	$select.empty();
					    	$select.append($(resp).find("option"));
					    	$select.find("option:first").prop("selected", true);
					    	
					    	// If in edit mode, figure out which option should be selected
					    	if ( !ATT_ADDING_MODE ) {
					    		var attach = attachMap.getItem($("#attachmentId").val());
					    		
					    		$select.children().each(function(index) {
					    			var $option = $(this);
					    			
					    			if ( $option.val() == attach.categoryId ) {
					    				$option.prop("selected", true);
					    				
					    				return false;
					    			}
					    		});
					    	}
					    	
					    	// Remove the attachment category iframe and its content from the DOM.
	             			$("#categoryAdd").empty();
	                	}
					}); //end of ajax
					
					$(this).dialog("destroy");
				}
			});
		});

		
		$("#downloadLink").click(function(event) {
			var id = $("#attachmentId").val();
			var associateId = $("#patientId").val();
			
			url = "<s:property value="#webRoot"/>/attachments/download.action?id=" + id + "&associatedId=" + associateId + 
					"&typeId=<%=AttachmentManager.FILE_PATIENT%>";
			redirectWithReferrer(url);
		});
		
		$("#btnAddPatient").click(function(event) {
			var $saveBtn = $(this);
	 		
			// Disable the submit button to prevent double click
			$saveBtn.prop("disabled", true);
			
			var canContinue = validatePatient();
			
			// if the validation was successful set the action attribute of the form to point to the edit action
			if ( canContinue ) {
				// Set the form's action attribute to use the correct struts action.
				$("#editForm").attr("action", "addPatient.action");
				$("#editForm").submit(); // without this explict submit(), no action class is invoked with Chrome browser.
			}
			else {
				// Re-enable the submit button since there was an error.
				$saveBtn.prop("disabled", false);
			}
			return canContinue;
		});
		
		$("#btnEditPatient").click(function(event) {
			var $saveBtn = $(this);
	 		
			// Disable the submit button to prevent double click
			$saveBtn.prop("disabled", true);
			
			var canContinue = validatePatient();
			
			// if the validation was successful set the action attribute of the form to point to the edit action
			if ( canContinue ) {
				// Set the form's action attribute to use the correct struts action.
				$("#editForm").attr("action", "updatePatient.action");
				$("#editForm").submit(); // without this explict submit(), no action class is invoked with Chrome browser.
			}
			else {
				// Re-enable the submit button since there was an error.
				$saveBtn.prop("disabled", false);
			}
			return canContinue;
		});
		
		$("input#btnResetPatient").click(function() {
			$("#btnResetPatient").prop("disabled", true);
			
			var url = '<s:property value="#webRoot"/>';		
			if ($("#patientId").length > 0) {	
				url += "/patient/showEditPatient.action?sectionDisplay=default&patientId=" + $("#patientId").val();
			}
			else{
				url += "/patient/showAddPatient.action";
				//alert(url);
			}
			redirectWithReferrer(url);
		});
		
		var webRoot = "<s:property value='#webRoot'/>";
		$("#addSubjectAttachmentsTable").idtTable({
			idtUrl: webRoot+"/patient/getAttachmentsListForDT.action",
			idtData: {
				primaryKey: 'id'
			},
			dom: 'Bfrtip',
			select: "multi",
	        columns: [
	            {
	                name: 'patientAttachmentName',
	                title: 'Name',
	                parameter: 'patientAttachmentName',
	                data: 'patientAttachmentName'
	            },
	            {
	                name: 'description',
	                title: 'Description',
	                parameter: 'description',
	                data: 'description'
	            },
	            {
	                name: 'categoryName',
	                title: 'Category',
	                parameter: 'categoryName',
	                data: 'categoryName'
	            }
	        ],
	        buttons: [
        	<%
        		if (user.hasPrivilege(manageAttachmentsPriv, protocalId)) {
        	%>
		      	  {
	       		 	text: 'Edit',
	       		 	className: 'idt-EditButton',
	       		 	enabled: false,
	       		 	enableControl: {
	                       count: 1,
	                       invert: true
	                 },
		  	    	 action: function(e, dt, node, config) {
		  				var $attachSection = $("#subjectAttachmentSection");
		  				
		  				// Display only the edit attachment button
		  				$attachSection.find("#btnAddAttachment").hide();
		  				$attachSection.find("#btnEditAttachment").show();
		  				
		  				//disable the buttons attached to the data table
		  				$attachSection.find(".idt-EditButton").addClass("disabled");
		  				
		  				// Get selected attachment
		  				var selected = $("#addSubjectAttachmentsTable").idtApi("getSelectedOptions");
		  				if ( selected.length > 1 ) {
		  					$.ibisMessaging("dialog", "warning","<s:text name='warning.attachment.multiSelect'/>");
		  					return false;
		  				}
		  				
		  				// Set the attachment input elements
		  				var attach = attachMap.getItem(selected[0]);
		  				
		  				if ( attach !== undefined ) {
		  					$attachSection.find("#attachmentId").val(attach.id);
		  					$attachSection.find("#attachmentName").val(attach.name);
		  					$attachSection.find("#fileName").val(attach.fileName);
		  					$attachSection.find("#associatedId").val(attach.associatedId);
		  					$attachSection.find("#attachmentDesc").val(attach.description);
		  					$attachSection.find("#attachmenCategory").val(attach.categoryId);
		  					$attachSection.find("#changeReason").val("");
		  				}
		  				else {
		  					var msg = '<s:text name="errors.notfound"><s:param><s:text name="patientForm.attachment.name"/></s:param></s:text>';
		  					$.ibisMessaging("dialog", "error", msg);
		  					return false;
		  				}
		  				
		  				ATT_ADDING_MODE = false;
		  				$attachSection.find(".attConditional").show();
		  				//$attachSection.find("#divPatientAttachments input").prop("disabled", true);
		  				
		  				// Setup the display of the existing file
		  				if ( attach.id <= 0 ) {
		  					$attachSection.find("#downloadLink").hide();
		  					$attachSection.find("#fileNameDisplay").show().text(attach.fileName);
		  				}
		  				else {
		  					$attachSection.find("#downloadLink").text(attach.fileName);
		  				}
		      	   	} 
			      },
			<%
    			}
    		%>
        	<%
    			if (user.hasPrivilege(viewAuditPriv, protocalId)) {
    		%>
		      	  {
	       		 	text: 'View Audit',
	       		 	className: 'idt-viewAuditButton',
	       		 	enabled: false,
	       		 	enableControl: {
	                       count: 1,
	                       invert: true
	                 },
		  	    	 action: function(e, dt, node, config) {
		  				var $table = $("#addSubjectAttachmentsTable");
		  				var selected  = $table.idtApi("getSelectedOptions");
		  				if (selected.length != 1) {
		  						$.ibisMessaging("dialog", "warning", "<s:text name='warning.attachment.multiAudit'/>");
		  						return false;
		  				}
		  				
		  				var url = webRoot + "/attachments/attachmentAudit.action?id=" + selected;
		  				openPopup(url, "Attachment_Audit", "width=800,height=300,toolbar=0,location=0,directories=0,,menubar=0,status=1,scrollbars=1,resizable=1");
		      	   	} 
		      	  },
			<%
    			}
    		%>
        	<%
				if (user.hasPrivilege(manageAttachmentsPriv, protocalId)) {
			%>
	        	  {
		        	 extend: 'delete',
		        	 className: 'idt-DeleteButton',
	       		 	 enabled: false,        	 
	     	    	 action: function(e, dt, node, config) {

	     	            $("input[name='patientForm.sectionDisplay']").val("attachments");
	     				$("input#guid").prop("disabled", false);
	     	    	 
		  	    		var table = $("#addSubjectAttachmentsTable");
		  	    		var selectedRows = $("#addSubjectAttachmentsTable").idtApi("getSelectedOptions");
						var params = "";
						
						// Convert the array of IDs to a comma delimited string list 
						for (var idx = 0; idx < selectedRows.length; idx++) {
							params += selectedRows[idx];
							if ((idx + 1) < selectedRows.length) {
								params += ",";
							}
						}
						
						var msgText = "<s:text name='attachment.alert.remove' />";
	        	   		var yesBtnText = "OK";
	        	   		var noBtnText = "Cancel";
	        	   		var action = webRoot + "/patient/deleteAttachment.action?selectedAttachmentIds=" + params;
		  	    		if (selectedRows.length != 0) {	
		  	    			EditConfirmationDialog(table,"warning", msgText, yesBtnText, noBtnText, action,
		        	   				true, "400px", "Confirm Deletion", params);
		  	    		}
		        	 } 
		       	  }
			<%
    			}
    		%>
	      	]
		});	
		
		$('input[type=file]').on('change', function(){
			
			var filePath = "";
			var fileFullPath = $(this).val(); 
			if (fileFullPath.lastIndexOf("\\") >= 0 ){
				filePath = fileFullPath.substring(0, fileFullPath.lastIndexOf("\\") + 1);
			} else if (fileFullPath.lastIndexOf("/") >= 0 ){
				filePath = fileFullPath.substring(0, fileFullPath.lastIndexOf("/") + 1);
			}
			//console.log("file change filePath: "+filePath);
			$("#attchFileStoragePath").val(filePath);
		})
		
		//retain the file name and path if there is a validation error
		if ( !$("#messageContainer").is(':empty') ) {
			var attchFileFilename = $("#attchFileFileName").val();
			var attchFilePath = $("#attchFileStoragePath").val();
			//console.log("attchFileFile: " + attchFilePath + attchFileFilename);
			$("input[ref=editForm_patientForm_attachment_attachFile]").val(attchFilePath + attchFileFilename);
		}
		
	});
</script>
<link rel="stylesheet" type="text/css" href="<s:property value="guidWsUrl" />ws/v1/../../styles/guidTool/guidTool.min.css" />
<script src="<s:property value="guidWsUrl" />ws/v1/../../js/guidTool/GlobalLibs.min.js"></script>
<script src="<s:property value="guidWsUrl" />ws/v1/../../js/guidTool/guidTool.min.js"></script>
	
 	<div class="clearboth"><!-- fix for IE --></div>
 	<br/>
 
 <s:form method="post" id="editForm" theme="simple" enctype="multipart/form-data">
	<s:hidden name="patientId" id="patientId" />
	<s:hidden name="patientForm.patientId" id="patientId" />
 	<s:hidden name="patientForm.id" />
 	<s:hidden name="patientForm.sectionDisplay" />
 	<s:hidden name="patientForm.addressId" />
	<s:hidden name="patientForm.associated2Protocol" value="true" id="associated2Study" />
	<s:hidden name="attachmentListJson" id="attachListStr" />
		
    <div>
    	<s:text name="patient.add.instruction.display"/>
    </div>
		
	<div id="accordion">
	
		<!-- Subject Information -->
 		<h3 id="subjectInfoH" class="toggleable <s:if test="%{patientForm.sectionDisplay != 'default'}">collapsed</s:if>">
 			<s:text name="patient.tab.info.display" />
		</h3>
		
		<!-- Subject Information -->
		<div id="subjectPiiInfo" class="DivPatientInfo">
		
			<!-- Validated checkbox (only for Editing Patient) -->
			<s:if test="%{(patientId != null) && (patientId > 0)}">
				<div class="formrow_2">
					<label for="patientForm.validated" style="white-space: nowrap;"><s:text name="patient.MarkValidated" /></label>
					<s:checkbox name="patientForm.validated" id="validated" cssStyle="margin-left: 3px;"/>
				</div>
	 			<div class="formrow_2"></div>
			</s:if>
			<s:else>
				<s:hidden name="patientForm.validated" value="false" />
			</s:else>
			
        	<s:if test="#disallowingPII == 1">
				<div>
					<div class="formrow_1">
						<s:text name="patient.add.subject.instruction.display"/>&nbsp;<b><s:text name="patient.add.subject.reminder.display"/></b> <br/>
						<div id="guidClient" class="guidClient"></div>	
					</div>
		    		<div> 
		   	 			<label class="requiredInput"></label> 
		    			<span style="font-style: italic;"><s:text name="patient.requiredSynbol.display"/></span>
	   				</div>
				</div>
				<div id="guidDisplay">
					<h4></h4>
					<div class="formrow_2">
						<label for="patientForm.guid" class="requiredInput"><s:text name="patient.guid.display"/></label>
						<s:textfield name="patientForm.guid" id="guid" maxlength="100" />
					</div>
					<div class="formrow_2">
						<input type="button" name="btnValidateGUID" value="<s:text name='button.ValidateGUID'/>" class="hidden" id="btnValidateGUID" 
								title="Validate GUID for the current subject" disabled />
	        				<img class="hidden" id ="runningValidate" width="25" height="25" src='<s:property value="#webRoot"/>/images/javafx-loading-100x100.gif' />
	        				<img id="validGuid" width="70" height="20" src='<s:property value="#webRoot"/>/images/GuidValidGreen.png' />
	        				<img id="invalidGuid"  width="70" height="20" src='<s:property value="#webRoot"/>/images/GuidInvalidRed.png' />
			 		</div>
				</div>
					
				<!-- Additional Information Associated with Subject -->
				<h4><s:text name="patient.AdditionalInfAssociatedSub" /></h4> 
 				<div class="formrow_2">
					<label for="patientForm.subjectId" class="requiredInput">
					<s:text name="patient.subjectId.display" /></label>
					<s:textfield name="patientForm.subjectId" size="25" id="subjectId" maxlength="50" cssClass="validateMe"/>
				</div>
				<div class="formrow_2">
					<label for="patientForm.recruited"><s:text name="patient.Recruit"/></label>
					<s:checkbox name="patientForm.recruited" fieldValue="true" />
				</div>
			
			</s:if>
			<s:else>
				<!-- For CNRM and CiStar users, PII information need to be manually entered and saved for display -->
   				<div class="formrow_2">
					<label for="patientForm.subjectId" class="requiredInput"> <s:text name="patient.mrn.display" /></label>
					<s:textfield name="patientForm.mrn" maxlength="50" size="25" id="mrn" cssClass="validateMe" />
				</div>
				<div class="formrow_2">
					<label for="patientForm.recruited"><s:text name="patient.Recruit"/></label>
					<s:checkbox name="patientForm.recruited" fieldValue="true" id="recruited" />
				</div>
			
				<div class="formrow_2">
					<label for="patientForm.lastName" class="requiredInput"><s:text name="patient.lastname.display" /></label>
					<s:textfield name="patientForm.lastName" maxlength="128" size="25" id="lastName" cssClass="validateMe" /> 
				</div>
				<div class="formrow_2">
					<label for="patientForm.dateOfBirth"><s:text name="patient.dateofbirth.display" /></label>
					<s:textfield name="patientForm.dateOfBirth" size="25" id="dob" cssClass="dateField validateMe" /> 
				</div>
				<div class="formrow_2">
					<label for="patientForm.firstname" class="requiredInput"><s:text name="patient.firstname.display" /></label>
					<s:textfield name="patientForm.firstName" maxlength="128" size="25" id="firstName" cssClass="validateMe" />	
				</div>
				<div class="formrow_2">
					<input type="button" style="margin-left: 180px;" name="btngetFromBTRIS" value="<s:text name='button.getFromBTRIS'/>" id="btngetFromBTRIS" 
								title="Get Subject from BTRIS" disabled />
				</div>
				<div class="formrow_2">
					<label for="patientForm.birthCity"><s:text name="patient.birthCity.display" /></label>
					<s:textfield name="patientForm.birthCity" maxlength="150" size="25" id="birthCity"/>
				</div>
 				<div class="formrow_2">
					<label for="patientForm.middleName"><s:text name="patient.middlename.display" /></label>
					<s:textfield name="patientForm.middleName" maxlength="128" size="25" id="middleName" cssClass="validateMe" />
				</div>
				<div class="formrow_2">
					<label for="patientForm.birthCountryId"><s:text name="patient.birthCountry.display" /></label>
					<s:select name="patientForm.birthCountryId" id="birthCountry" list="#session.countryOptions" listKey="id" listValue="shortName" />
				</div>
				<div class="formrow_2">
					<label for="patientForm.sex"> <s:text name="patient.sex.display" /></label>
					<s:select name="patientForm.sex" cssClass="validateMe" id="sex" list="#session.sexOptions" listKey="id" listValue="shortName" />
				</div>
				<div class="formrow_2">
					<label for="patientForm.address1"><s:text name="patient.address1.display" /></label>
					<s:textfield name="patientForm.address1" maxlength="150" size="25" cssClass="validateMe" />
				</div>
 				<div class="formrow_2">
					<label for="patientForm.email"><s:text name="patient.email.display" /></label>
					<s:textfield name="patientForm.email" maxlength="50" size="25" cssClass="validateMe" title="Format: name@email.com"/>
				</div>
 				<div class="formrow_2">
					<label for="patientForm.address2"><s:text name="patient.address2.display" /></label>
					<s:textfield name="patientForm.address2" maxlength="150" size="25" cssClass="validateMe" />
				</div>
				<div class="formrow_2">
					<label for="patientForm.homePhone"><s:text name="patient.homephone.display" /></label>
					<s:textfield name="patientForm.homePhone" maxlength="16" size="25" cssClass="validateMe" title="Format: 123-456-7890" />
				</div>
				<div class="formrow_2">
					<label for="patientForm.city"><s:text name="patient.city.display" /></label>
					<s:textfield name="patientForm.city" maxlength="150" size="25" cssClass="validateMe" />
				</div>
				<div class="formrow_2">
					<label for="patientForm.workPhone"><s:text name="patient.workphone.display" /></label>
					<s:textfield name="patientForm.workPhone" maxlength="16" size="25" cssClass="validateMe" title="Format: 123-456-7890" />
				</div>
 				<div class="formrow_2">
					<label for="patientForm.state"><s:text name="patient.state.display" /></label>
					<s:select name="patientForm.state" cssClass="validateMe" list="#session.xstates" listKey="id" listValue="longName" />
				</div>
 				<div class="formrow_2">
					<label for="patientForm.mobilePhone"><s:text name="patient.mobilephone.display" /></label>
					<s:textfield name="patientForm.mobilePhone" maxlength="16" size="25" cssClass="validateMe" title="Format: 123-456-7890" />
				</div>
 				<div class="formrow_2">
					<label for="patientForm.zip"><s:text name="patient.zip.display" /></label>
					<s:textfield name="patientForm.zip" maxlength="50" size="25" cssClass="validateMe" />
				</div>
				<div class="formrow_2"></div>
 				<div class="formrow_2">
					<label for="patientForm.country"><s:text name="patient.country.display" /></label>
					<s:select name="patientForm.country" cssClass="validateMe" list="#session.countryOptions" listKey="id" listValue="shortName" />
				</div>
				
				<div class="formrow_1">
					<s:text name="patient.add.subject.instruction.display"/>&nbsp;<b><s:text name="patient.add.subject.reminder.display"/></b> <br/>					
				</div>
				<div class="hidden" id="guidBtrisDataSearchDiv">
					<div class="formrow_4">
						<label for="patientForm.subjectId"> <s:text name="patient.mrn.display" /></label>
						<s:textfield name="patientForm.mrnG" maxlength="128" size="25" id="mrnG" cssClass="validateMe" />&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
						
						<label for="patientForm.firstname"><s:text name="patient.firstname.display" /></label>
						<s:textfield name="patientForm.firstNameG" maxlength="128" size="25" id="firstNameG" cssClass="validateMe" />&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
				
						<label for="patientForm.lastName"><s:text name="patient.lastname.display" /></label>
						<s:textfield name="patientForm.lastNameG" maxlength="128" size="25" id="lastNameG" cssClass="validateMe" />&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
					
						<input type="button" style="margin-left: 10px;" name="btngetFromBTRIS" value="<s:text name='button.getFromBTRIS'/>" id="btngetFromBTRISG" 
									title="Get Subject from BTRIS" disabled /><br/>
					</div>
				</div>				
				<div id="guidClient" class="guidClient"></div>	
					
				<div id="guidDisplay">
					<div class="formrow_2">
						<label for="patientForm.guid"  class="requiredInput">
							<s:text name="patient.guid.display"/></label>
						<s:textfield name="patientForm.guid" id="guid" maxlength="100" />
					</div>
					<div class="formrow_2">
						<input type="button" name="btnValidateGUID" value="<s:text name='button.ValidateGUID'/>" class="hidden" id="btnValidateGUID" 
								title="Validate GUID for the current subject" disabled />
	        				<img class="hidden" id ="runningValidate" width="25" height="25" src='<s:property value="#webRoot"/>/images/javafx-loading-100x100.gif' />
	        				<img id="validGuid" width="70" height="20" src='<s:property value="#webRoot"/>/images/GuidValidGreen.png' />
	        				<img id="invalidGuid"  width="70" height="20" src='<s:property value="#webRoot"/>/images/GuidInvalidRed.png' />
			 		</div>
				</div>
					
			</s:else>
		</div>

		<!-- Study -->
		<h3 class="toggleable <s:if test="%{patientForm.sectionDisplay != 'default'}">collapsed</s:if>"> 
			<s:text name="patient.study.title"/>
		</h3>
		<div id="studySection" class="divPatientInfo">
			<div><s:text name="patient.study.instruction" /></div> <br>
 			
 			<div class="formrow_2" >
				<label style="white-space: nowrap;"><s:text name="patient.protocolname.display" /></label> 
				<s:checkbox name="patientForm.currentProtocolId" value="%{associatedWithStudy}" 
						fieldValue="%{patientForm.currentProtocolId}" id="currProtocolCheckbox" cssStyle="margin-left: 26px;" />
			</div>
			<div class="formrow_2">
				<label> <s:text name="patient.futurestudy.display" /> </label> 
				<s:checkbox name="patientForm.futureStudy" id="futureStudy" />
			</div>
 			<div class="formrow_2">
       			<!--  <label for="patientForm.subjectNumber" <s:if test="%{#SUBJECT_DISPLAY_TYPE == 3}"> class="requiredInput" </s:if> >
			  		<s:text name="patient.subjectnumber.display" />
				</label>
				<s:textfield name="patientForm.subjectNumber" size="25" id="subjectNumber" maxlength="40" cssClass="validateMe"/>-->
			</div>

            <s:if test="#disallowingPII == 1">
	 			<div class="formrow_2">
					<label for="patientForm.enrollmentDate"><s:text name="patient.enrollmentdate.display" /></label>
					<s:textfield name="patientForm.enrollmentDate" size="25" maxlength="40" cssClass="dateField validateMe" />
				</div>
				<div class="formrow_2">
					<label for="patientForm.siteId"><s:text name="patient.subjectSite"/></label>
					<s:select name="patientForm.siteId" cssClass="validateMe" list="#session.__patientAction_sites" 
							listKey="id" listValue="name" headerKey="%{@java.lang.Integer@MIN_VALUE}" headerValue="-----"/>
				</div>
	 			<div class="formrow_2">
					<label for="patientForm.completionDate"> <s:text name="patient.completionDate.display" /></label>
					<s:textfield name="patientForm.completionDate" size="25" maxlength="40" cssClass="dateField validateMe"/>
				</div>
				<div class="formrow_2">
					<label for="patientForm.active"><s:text name="patient.statusInCurrentStudy"/></label>
					<s:radio name="patientForm.active" list="#{'true':''}"/><s:text name="patient.active.display"/>
					<s:radio name="patientForm.active" list="#{'false':''}"/><s:text name="patient.inactive.display"/>
				</div>
			</s:if>
			<s:else>
	   			<div class="formrow_2">
					<label for="patientForm.subjectId" class="requiredInput"> 
						<s:text name="patient.subjectId.display" />
					</label>
					<s:textfield name="patientForm.subjectId" maxlength="50" size="25" id="subjectId" cssClass="validateMe" />
				</div>

	 			<div class="formrow_2">
					<label for="patientForm.enrollmentDate"> <s:text name="patient.enrollmentdate.display" /></label>
					<s:textfield name="patientForm.enrollmentDate" size="25" maxlength="40" cssClass="dateField validateMe" />
				</div>
				<div class="formrow_2">
					<label for="patientForm.siteId"><s:text name="patient.subjectSite"/></label>
					<s:select name="patientForm.siteId" cssClass="validateMe" list="#session.__patientAction_sites" 
							listKey="id" listValue="name" headerKey="%{@java.lang.Integer@MIN_VALUE}" headerValue="-----" />
				</div>
	 			<div class="formrow_2">
					<label for="patientForm.completionDate"> <s:text name="patient.completionDate.display" /></label>
					<s:textfield name="patientForm.completionDate" size="25" maxlength="40" cssClass="dateField validateMe"/>
				</div>
				<div class="formrow_2">
					<label for="patientForm.active"> <s:text name="patient.statusInCurrentStudy"/></label>
					<s:radio name="patientForm.active" list="#{'true':''}"/><s:text name="patient.active.display"/>
					<s:radio name="patientForm.active" list="#{'false':''}"/><s:text name="patient.inactive.display"/>
				</div>
			</s:else>
		</div>

		<!-- Attachments -->
 		<h3 class="toggleable collapsed" >
 		 	<s:text name="patient.attachment.title" />
		</h3>
		<div id="subjectAttachmentSection" class="divPatientInfo">
		<security:hasProtocolPrivilege privilege="manageAttachments">
			<div><s:text name="patient.attachment.warn"/></div><br>
			<div id="divPatientAttachmentForms">
				<s:hidden name="patientForm.attachment.id" id="attachmentId" />
				<s:hidden name="patientForm.attachment.associatedId" id="associateId"/>
				<s:hidden name="patientForm.attachment.fileName" id="fileName"/>
				<s:hidden name="patientForm.attachment.attachFileFileName" id="attchFileFileName"/>
				<s:hidden name="patientForm.attachment.storagePath" id="attchFileStoragePath"/>

 				<div class="formrow_2">
					<label for="patientForm.attachment.name" class="requiredInput"> <s:text name="patient.AttchName" /></label>
					<s:textfield name="patientForm.attachment.name" size="25" maxlength="256" id="attachmentName" />
				</div>
				<div class="formrow_2">
					<label for="patientForm.attachment.file" class="requiredInput"><s:text name="patient.Attachment" /></label>
			 		<a title="<s:text name='tooltip.warning.fileSize'/>"><img src="../images/icons/info-icon.png"></a> 
					<s:file name="patientForm.attachment.attachFile" size="20" cssStyle="width:164px;" />
				</div>
				<div class="formrow_2 attConditional" style="display: none;" >
					<label>&nbsp;</label>
					&nbsp;
				</div>
				<div class="formrow_2 attConditional" style="display: none;" >
					<label><s:text name="attachment.uploadedFile.display"/></label>
					<a href="javascript:;" id="downloadLink" ></a>
					<span id="fileNameDisplay" style="display: none;"></span>
				</div>
 				<div class="formrow_2">
					<label for="patientForm.attachment.description"> <s:text name="patient.AttchDescription" /></label>
					<s:textfield name="patientForm.attachment.description" size="20" maxlength="4000" id="attachmentDesc" />
				</div>
				<div class="formrow_2" id="attaCategoryList">
					<label for="patientForm.attachment.category.id"><s:text name="patient.AttchCategory" /></label>
					<a id="attaCategories" title="<s:text name='tooltip.attachment.category.addEdit'/>">
						<img src="../images/icons/add.png">
					</a>
					<s:select name="patientForm.attachment.category.id" id="attachmenCategory" list="#session._attachments_categories" 
						listKey="id" listValue="name" cssStyle="width:164px;"/>
					
				</div>

				<div class="formrow_2 attConditional" style="display: none;" >
					<label for="patientForm.attachment.changeReason"> <s:text name="patient.AttchReasonForChange" /></label>
					<s:textfield name="patientForm.attachment.changeReason" size="25" maxlength="4000" id="changeReason" />
				</div>
				<div class="formrow_2 attConditional" style="display: none;" ></div>
 				<div class="formrow_1">
 					<input type="button" value="<s:text name='button.Cancel'/>" id="btnCancelAttachment" title="<s:text name='tooltip.cancel'/>"/>
 					<input type="submit" id="btnAddAttachment" value="<s:text name='button.Add'/>" title="<s:text name='tooltip.attachment.add'/>" />
 					<input type="submit" id="btnEditAttachment" value="<s:text name='button.Edit'/>" 
 						title="<s:text name='tooltip.update'/>" style="display: none;" />
				</div>	
 				<div class="formrow_1"></div>
			</div>	
		</security:hasProtocolPrivilege>	
					
 		<div class="idtTableContainer brics" id="divPatientAttachments"> 
          	<table id="addSubjectAttachmentsTable" class="table table-striped table-bordered" width="100%">
			</table> 
		</div>
	  </div> 
	  
	  <div class="formrow_1">
	  		<s:submit action="cancelPatient" key="button.Cancel" title="%{getText('tooltip.cancel')}"/>
		    <input type="button" value="<s:text name='button.Reset' />" id="btnResetPatient" title="<s:text name='tooltip.reset'/>"/>
			<s:if test="%{patientId > 0}">
				<input type="button" id="btnEditPatient" value="<s:text name='button.Save' />" title="<s:text name='tooltip.save'/>" />
		    </s:if>
		    <s:else>
		    	<input type="button" id="btnAddPatient" value="<s:text name='button.Save' />" title="<s:text name='tooltip.save'/>" />
		    </s:else>
	  </div>
	</div>
 	<div id="categoryAdd" style="display: none;"></div>
 </s:form>
 
 <script type="text/javascript">
 // jwt is in the cookie
 GuidClient.render({
	container: $("#guidClient"),
	url: "<s:property value="guidWsUrl" />ws/v1/"
});
 
 </script>
 
<jsp:include page="/common/footer_struts2.jsp" />
</html>
