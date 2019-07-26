<%@include file="/common/taglibs.jsp"%>
<title>Account Renewal</title>

<div class="clear-float">
	<h1 class="float-left">Account Management</h1>
</div>

<!-- begin .border-wrapper -->
<div class="border-wrapper">

	<jsp:include page="../navigation/userManagementNavigation.jsp" />

	<!--begin #center-content -->
	<div id="main-content">
		<div id="breadcrumb">
				<s:a href="%{modulesAccountURL}accountReviewer/accountReportsAction!renewalList.action">Account Renewal</s:a>
				&nbsp;&gt;&nbsp;Account Renewal: <s:property value="accountDetailsForm.userName" />
		</div>
		
		<h2>
			Account Renewal: User
			<s:property value="currentAccount.userName" />
		</h2>

		<div class="margin-top-sm">
			<div class="flex-justify-start">
				<div style="width: 33%">
					<div class="form-output">
						<div class="label">
							<strong>Username:</strong>
						</div>
						<div class="readonly-text">
							<s:property value="currentAccount.userName" />
						</div>
					</div>
					<div class="form-output">
						<div class="label">First Name:</div>
						<div class="readonly-text">
							<s:property value="currentAccount.user.firstName" />
						</div>
					</div>
					<div class="form-output">
						<div class="label">Middle Name:</div>
						<div class="readonly-text">
							<s:property value="currentAccount.user.middleName" />
						</div>
					</div>
					<div class="form-output">
						<div class="label">Last Name:</div>
						<div class="readonly-text">
							<s:property value="currentAccount.user.lastName" />
						</div>
					</div>
					<div class="form-output">
						<div class="label">E-Mail:</div>
						<div class="readonly-text">
							<s:property value="currentAccount.user.email" />
						</div>
					</div>
					<div class="form-output">
						<div class="label">Affiliated Institution:</div>
						<div class="readonly-text">
							<s:property value="currentAccount.affiliatedInstitution" />
						</div>
					</div>
					<div class="form-output">
						<div class="label">Phone Number:</div>
						<div class="readonly-text">
							<s:property value="currentAccount.phone" />
						</div>
					</div>
					<s:if test="%{accountDetailsForm.eraId != null}">
						<div class="form-output">
							<div class="label">NIH Federal Identity:</div>
							<div class="readonly-text">
								<s:if test="currentAccount.eraId == ''">None Specified</s:if>
								<s:property value="currentAccount.eraId" />
							</div>
						</div>
					</s:if>
					<div class="form-output">
						<div class="label">Account Request Reason:</div>
						<div class="readonly-text limitLength100" id="requestReasonDiv">
							<s:property value="currentAccount.interestInTbi" />
						</div>
					</div>
				</div>

				<div style="width: 33%">
					<div class="form-output">
						<label class="label"><strong>Account Status:</strong></label> <strong>
							<s:property value="currentAccount.accountStatus.name" />
						</strong>
					</div>
					<div class="form-output">
						<div class="label">Street Line 1:</div>
						<div class="readonly-text">
							<s:property value="currentAccount.address1" />
						</div>
					</div>
					<s:if test="%{currentAccount.address2 != null}">
						<div class="form-output">
							<div class="label">Street Line 2</div>
							<div class="readonly-text">
								<s:property value="currentAccount.address2" />
							</div>
						</div>
					</s:if>
					
					
					<div class="form-output">
						<div class="label">City:</div>
						<div class="readonly-text">
							<s:property value="currentAccount.city" />
						</div>
					</div>
					<div class="form-output">
						<div class="label">Country:</div>
						<div class="readonly-text">
							<s:property value="currentAccount.country.name" />
						</div>
					</div>
					
					<div class="form-output">
						<div class="label">Postal Code:</div>
						<div class="readonly-text">
							<s:property value="currentAccount.postalCode" />
						</div>
					</div>
					
					<div class="form-output">
						<div class="label">State:</div>
						<div class="readonly-text">
							<s:property value="currentAccount.state.code" />
						</div>
					</div>
					
					<div class="form-output">
						<div class="label">Phone:</div>
						<div class="readonly-text">
							<s:property value="currentAccount.phone" />
						</div>
					</div>


					<div class="form-output">
						<div class="label">Administrative Notes:</div>
						<div class="readonly-text">
							<s:property value="currentAccount.adminNote" />
						</div>
					</div>
				</div>
				<div style="width: 33%;">
					<s:if test="%{!onlyReviewer}">
						<span class="icon"><a
							href="viewAccountRenewal!edit.action?accountId=${currentAccount.id}"><img
								alt="Edit" src="<s:url value='/images/icons/edit.png' />" /></span></a>
					</s:if>
				</div>
			</div>

			<jsp:include page="includes/existingAdministrativeFiles.jsp"></jsp:include>

			<jsp:include page="includes/accountPrivileges.jsp">
				<jsp:param name="idtUrl"
					value="viewProfile!getAccountRoleLists.action" />
			</jsp:include>
			
			<jsp:include page="includes/accountPermissionGroups.jsp">
				<jsp:param name="idtUrl"
					value="viewProfile!getPermissionGroupMembers.action" />
			</jsp:include>			
			
			<!-- Display the file templates section -->		
			<jsp:include page="includes/adminFileTemplatesInterface.jsp" />
	

			<jsp:include page="includes/accountActionHistory.jsp">
				<jsp:param name="idtUrl"
					value="viewProfile!getAccountHistory.action" />
			</jsp:include>
			
			<jsp:include page="includes/accountAdministrativeNotes.jsp">
				<jsp:param name="idtUrl" value="viewProfile!getAccountAdministrativeNotes.action"/>
			</jsp:include>
			

			<div class="clear-both" id="accountRenewalButtons">
				<div class="button">
					<input type="button" value="Contact User Before Renewal"
						onclick="javascript:openContactUserDialogue();" />
				</div>
				
				<div class="button margin-left">
								<input type="button" value="Renew Expiring Privileges" 
						onclick="javascript:openAccountRenewalDialogue();" />
				</div>
			</div>
		</div>
		<!-- end of #main-content -->
	</div>
</div>
<!-- end of .border-wrapper -->

<div id="contactUserDashBoardDialog" style="display: none">
	<div id="contactUserDashBoard"></div>
</div>

<div id="renewPrivilgesDashBoardDialog" style="display: none">
	<div id="renewPrivilgesDashBoard"></div>
</div>

<div id="validationDialog" style="display: none"></div>

<script type="text/javascript">
	setNavigation({
		"bodyClass" : "primary",
		"navigationLinkID" : "userManagementModuleLink",
		"subnavigationLinkID" : "managingAccountsLink",
		"tertiaryLinkID" : "accountsForRenewalLink"
	});

	function openContactUserDialogue() {

		$
				.ajax({
					type : "GET",
					url : "accountReportsAction!getContactUserDashBoard.ajax",
					success : function(data) {
						$("#contactUserDashBoard").html(data);
						var errorDialog;
						var dialog = $("#contactUserDashBoardDialog")
								.dialog(
										{
											title : "User Action Required",
											height : 350,
											width : 600,
											buttons : [
													{
														id : "cancelBtn",
														text : "Cancel",
														click : function() {
															dialog.dialog('close');
														}
													},
													{
														id : "contactUser",
														text : "Contact User",
														click : function() {
															if (!validateCheckedBox()) {
																errorDialog = $.ibisMessaging(
																				"dialog",
																				"error",
																				"Please select atleast one email template.",
																				{
																					container : "body"
																				});
															} else if (!validateUserText()) {
																errorDialog = $.ibisMessaging(
																				"dialog",
																				"error",
																				"Please provide a text if the email template you selected has a text box associated with it.",
																				{
																					container : "body"
																				});
															} else {
																contactUser();
																dialog.dialog('close');
															}
														}
													} ],
													close: function(event,ui) {
														if(errorDialog) {
															$.ibisMessaging("close", {type: 'dialog'});
														}
													}
										});
						dialog.dialog('open');
					}
				});
	}

		function validateUserText() {

			var valid = true;

			$(".accountEmailsOption")
					.each(
							function() {

								if ($(this)
										.find(
												"input:checkbox[name=accountGuidanceEmail]")
										.is(":checked")
										&& $(this).find('.accountEmailMsg').length > 0) {
									if ($(this).find('.accountEmailMsg').val() == '') {
										valid = false;
									}
								}
							});
			return valid;
		}

		function validateCheckedBox() {

			var valid = true;

			//validate atleast one checkbox is checked 
			if (!$('input:checkbox[name=accountGuidanceEmail]').is(":checked")) {
				valid = false;
			}

			return valid;
		}

		function contactUser() {
			var accountMessages = getAccountMessageTemplateData();

			$.ajax({

				type : "POST",
				url : "accountReportsAction!contactUser.ajax",
				data : {
					"accountMessages" : JSON.stringify(accountMessages)
				},
				success : function(data) {
					//reload datatable
					$('#accountHistoryTable').DataTable().ajax.reload();
				}

			});
		}

		function getAccountMessageTemplateData() {
			var accountMessageTemplates = [];

			$(".accountEmailsOption")
					.each(
							function() {
								if ($(this)
										.find(
												"input:checkbox[name=accountGuidanceEmail]")
										.is(":checked")) {
									var id = $(this).find(".hiddenId").val();
									var text = '';

									if ($(this).find('.accountEmailMsg').length > 0) {
										text = $(this).find('.accountEmailMsg')
												.val();
									}

									var accountMessageTemplate = new AccountMessageTemplate(
											id, text);
									accountMessageTemplates
											.push(accountMessageTemplate);
								}
							});

			return accountMessageTemplates;
		}

		function AccountMessageTemplate(id, userMsg) {
			this.id = id;
			this.userMsg = userMsg;
		}
		
		function openAccountRenewalDialogue() {
			   
			   $.ajax({
					type: "GET",
					url:"accountReportsAction!getRenewPrivilegesDialogue.ajax",
					success:function(data) {
						$("#renewPrivilgesDashBoard").html(data);
						
						var dialog = $("#renewPrivilgesDashBoardDialog").dialog({
							title: "Account Renewal",
							height: 450,
							width: 800,
							buttons : [
								{
									id: "cancelBtn",
									text: "Cancel",
									click: function() {
										dialog.dialog('close');
									}
								},
								{
									id: "renew",
									text: "Renew",
									click: function() {
										if(validateComments()) {
											renewPrivileges();
											$(this).dialog("close");
											window.location.href = "accountReportsAction!renewalList.action"
										}
									}
								}
							]
						});
						dialog.dialog('open');			
					}	
				});
			   
		   }
	   
	   function validateComments() {
				var renewPrivilegesComment =  $("#renewPrivilegesComment").val();
				var valid = true;
				if (typeof renewPrivilegesComment !== 'undefined') {
					if (renewPrivilegesComment.length == 0) {
						$("#validateRenewPrivilegesComment").show();
						valid = false;
					}
				}
				return valid;
		   }
		   
		   function renewPrivileges() {
			   
			   var expirationDate = $("#renewPrivilegesExpireDate").val();
			   var renewPrivilegesComment = $("#renewPrivilegesComment").val();

			   $.ajax({
					 type: "POST",
					 url: "accountReportsAction!renewPrivileges.ajax",
					 data: {"expirationDate" : expirationDate,
						 "renewPrivilegesComment" :renewPrivilegesComment},
					 success: function(data) {
						//reload datatable
						 if (data == "success"){ 
								$('#accountHistoryTable').DataTable().ajax.reload();	
								$('#accountRoleListTable').DataTable().ajax.reload();
						 }else{
							 console.log("error!!!");
						 }		
					 } 
				 }); 
		   }
	
</script>