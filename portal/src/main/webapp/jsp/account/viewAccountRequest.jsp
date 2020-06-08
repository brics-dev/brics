<%@include file="/common/taglibs.jsp"%>
<title>Account Request</title>

<div class="clear-float">
	<h1 class="float-left">Account Management</h1>
</div>

<!-- begin .border-wrapper -->
<div class="border-wrapper">

	<jsp:include page="../navigation/userManagementNavigation.jsp" />

	<!--begin #center-content -->
	<div id="main-content">
		<div id="breadcrumb">
		
		<s:if test="%{inAccountReviewer}">
			<s:a
				href="%{modulesAccountURL}accountReviewer/accountReportsAction!requestList.action">Account Requests</s:a>
		</s:if>
		<s:else>
			<s:a
				href="%{modulesAccountURL}accountAdmin/accountAction!list.action">Account List</s:a>
		</s:else>
			&nbsp;&gt;&nbsp;Account Request:
			<s:property value="accountDetailsForm.userName" />
		</div>
		<h2>
			Account Request:
			<s:property value="currentAccount.userName" />
		</h2>

		<div>
			<s:form id="theForm" cssClass="validate" method="post"
				validate="true" enctype="multipart/form-data">
				<s:token />
				<div>
					<div class="flex-justify-start">
						<div style="width: 33%;">
							<div class="form-output">
								<div class="label">
									<strong>Username:</strong>
								</div>
								<div>
									<strong><s:property value="currentAccount.userName" /></strong>
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

						<div style="width: 33%;">
							<div class="form-output">
								<label class="label"><strong>Account Status:</strong></label> <strong>
									<s:if test="currentAccount.accountStatus.id == 3">New Account Request</s:if>
									<s:else>
										<s:property value="currentAccount.accountStatus.name" />
									</s:else>
								</strong>
							</div>
							<div class="form-output">
								<div class="label">E-Mail:</div>
								<div class="readonly-text">
									<s:property value="currentAccount.user.email" />
								</div>
							</div>
							<div class="form-output">
								<div class="label">Street Line 1:</div>
								<div class="readonly-text">
									<s:property value="currentAccount.address1" />
								</div>
							</div>
							<s:if test="%{currentAccount.address2 != null}">
								<div class="form-output">
									<div class="label">Street Line 2:</div>
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
									href="viewAccountRequest!edit.action?accountId=${currentAccount.id}"><img
										alt="Edit" src="<s:url value='/images/icons/edit.png' />" /></span></a>
							</s:if>
						</div>
					</div>
					
					<s:if test="%{!onlyReviewer && (currentAccount.accountStatus.name == 'Requested' || currentAccount.accountStatus.name == 'Pending')}">
						<div class="margin-top-med margin-bottom-med">
							<div class="button no-float">
								<input type="button"
									onclick="withdrawAccountRequestLightbox();"
									value="Withdraw Account Request" />
							</div>
						</div>
					</s:if>
				</div>

				<jsp:include page="includes/existingAdministrativeFiles.jsp"></jsp:include>
				
				<!-- Display the file templates section -->		
			   <jsp:include page="includes/adminFileTemplatesInterface.jsp" />
				
				<jsp:include page="includes/accountPrivileges.jsp" >
					<jsp:param name="idtUrl" value="viewProfile!getAccountRoleLists.action"/>
				</jsp:include>
				
				<jsp:include page="includes/accountPermissionGroups.jsp">
					<jsp:param name="idtUrl" value="viewProfile!getPermissionGroupMembers.action"/>
				</jsp:include>
				
				<jsp:include page="includes/accountActionHistory.jsp">
					<jsp:param name="idtUrl" value="viewProfile!getAccountHistory.action"/>
				</jsp:include>
				
				<jsp:include page="includes/accountAdministrativeNotes.jsp">
					<jsp:param name="idtUrl" value="viewProfile!getAccountAdministrativeNotes.action"/>
				</jsp:include>
			

			<div class="clear-both">
					<div class="clear-both">
						<div class="form-field">
							<s:if test="%{currentAccount.accountStatus.name == 'Requested' || currentAccount.accountStatus.name == 'Pending' || 
								currentAccount.accountStatus.name == 'Change Requested' || currentAccount.accountStatus.name == 'Renewal Requested'}">
								<div class="flex-justify-between">
									<div>
										<div class="button">
											<input type="button"
												onclick="javascript:partialApprovalLightbox();"
												value="Partial Approval" />
										</div>
										
										<div class="button margin-left">
											<input id="temporary-rejection" type="button" onclick="#" value="Temporary Rejection" />
										</div>
									</div>
									
									<div>
										<div class="button">
											<input type="button" value="Final Approval"
												onclick="javascript:finalApprovalLightbox();" />
										</div>
										
										<s:if test="%{currentAccount.accountStatus.name != 'Change Requested' && currentAccount.accountStatus.name != 'Renewal Requested'}">
											<div class="button margin-left">
												<input id="reject-account" type="button" value="Reject Account" />
											</div>
										</s:if>
									</div>
								</div>
							</s:if>
						</div>
					</div>
				</div>
			</s:form>
		</div>
		<!-- end of #main-content -->
	</div>
</div>
<!-- end of .border-wrapper -->

<div id="partialApprovalLightboxDiv" style="display: none"></div>
<div id="temporaryRejectionLightboxDiv" style="display: none"></div>
<div id="rejectionLightboxDiv" style="display: none"></div>
<div id="finalApprovalLightboxDiv" style="display: none"></div>
<div id="withdrawAccountRequestLightboxDiv" style="display: none"></div>

<script type="text/javascript">

	<s:if test="%{inAccountReviewer}">
		setNavigation({"bodyClass":"primary", "navigationLinkID":"userManagementModuleLink", "subnavigationLinkID":"managingAccountsLink", "tertiaryLinkID":"accountRequestLink"});
	</s:if> 
	<s:else>
		setNavigation({"bodyClass" : "primary","navigationLinkID" : "userManagementModuleLink","subnavigationLinkID" : "managingUsersLink","tertiaryLinkID" : "accountList"});
	</s:else> 
	
	function validateReason() {
		var withdrawReason = $("#withdrawReason").val();
		var valid = true;
		if (typeof withdrawReason !== 'undefined') {
			if (withdrawReason.length == 0) {
				$("#validateWithdrawReason").show();
				valid = false;
			}
		}
		return valid;
	}
	

	function withdrawRequest() {

		var secondActionAjax = "withdrawRequestAction!withdrawAccountRequest.ajax";
		var secondAction = "withdrawRequestAction!withdrawAccountRequest.action";
		var withdrawReason = $("#withdrawReason").val();

		$.ajax({
			type : "post",
			data : {
				"withdrawReason" : withdrawReason
			},
			url : secondActionAjax,

			success : function(response) {
	  			if (response == "landing") {
	  				window.location.href = "/portal/baseAction!landing.action";
	  				
	  			} else {
					<s:if test="%{inAccountAdmin}">
						window.location.href = "accountAction!list.action";
					</s:if>
					<s:else>
						window.location.href = "accountReportsAction!requestList.action";
					</s:else>
	  			}
			},
			error : function(error) {
				alert(error)
			}
		});

	}
	
	function withdrawAccountRequestLightbox() {
		var dialogOne;
		var $withdrawDiv = $("#withdrawAccountRequestLightboxDiv");
		var firstActionAjax = "withdrawRequestAction!withdrawAccountRequestLightbox.ajax";

		dialogOne = $withdrawDiv.dialog({

			autoOpen : false,
			modal : true,
			height : 400,
			width : 800,
			draggable : false,
			resizable : false,
			title : "Withdraw Account Request",
			buttons : [ {
				text : "Cancel",
				click : function() {
					$(this).dialog("close");
				}
			}, {
				
				id: "button-withdraw",
				text : "Withdraw Request",
				click : function() {
					if (validateReason()) {
						withdrawRequest();
						$(this).dialog("close");
					}
				}
			} ]
		});

		$.ajax({
			type : "post",
			url : firstActionAjax,

			success : function(response) {
				$withdrawDiv.html(response);
				$withdrawDiv.dialog("open");
				
				var rejectButton = $("#button-withdraw");
	  			rejectButton.button("disable");
	  			
				$('#withdrawReason').on('keyup paste change',function() {
					var withdrawObj = $(this);
					setTimeout(function() {
				        var textarea_value = withdrawObj.val();
				      
					    if(textarea_value != '') {
					    	rejectButton.button("enable");
					    }else{
					    	rejectButton.button("disable");
					    }
				    }, 100);
				});
			}
		});

	}

	// Account role object constructer.
	function AccountRole(roleType, roleStatus) {
		this.roleTypeId = roleType;
		this.roleStatusId = roleStatus;
	}

	$("document").ready(function() {
		$("div.limitLength100").each(function() {
			less($(this), 100);
		});

		expirationDateInit();

		// Event Handlers
		$(".approveRadio").click(function() {
			expirationDateInit();
		});

		$(".noActionRadio, .denyRadio").click(function() {
			expirationDateInit();
		});

		$('a.moreLessSwap').click(function(event) {
			doSwap($(this));
		});
		
		$('#temporary-rejection').click(function(event) {
			event.preventDefault;
			temporaryRejectionLightbox();
		});
		
		
		$('#reject-account').click(function(event) {
			event.preventDefault;
			rejectionLightbox();
		});
		
		
		
	});

	function doSwap($elem) {
		// Pass everything after the # to swapMoreLess so it knows which text to swap
		swapMoreLess($elem);
		return false;
	}

	// Truncates text inside of the div indicated by id to the length indicated by charLimit and adds a link to expand the text
	function less($holder, charLimit) {
		var fullText = $holder.text();
		var trunc = fullText;

		if (trunc.length > charLimit) {
			newDiv = "<div id='swap-" + id + "'>"
					+ fullText
					+ " <a class='moreLessSwap' href='#' id='#swap-" + id + "'> [Show Less] </a></div>";

			$hiddenSwapDiv = $('div.moreLessSwap-content');
			if ($hiddenSwapDiv.length > 0) {
				$hiddenSwapDiv.append(newDiv);
			} else {
				hiddenSwapDiv = "<div class='moreLessSwap-content' style='display: none;'>" + newDiv
						+ "</div>";
				$('body').append(hiddenSwapDiv);
			}

			$newDiv = $('#swap-' + id);

			/* Truncate the content of the text, then go back to the end of the previous 
			   word to ensure that we don't truncate in the middle of a word */
			trunc = trunc.substring(0, charLimit);
			trunc = trunc.replace(/\w+$/, '');

			$holder
					.text(trunc)
					.append(
							'<a class="moreLessSwap" href="#" id="#swap-' + id + '">...</a>');
		}
	}

	// Swap the text from the hidden div with the text displayed to show more or less text
	function swapMoreLess(clicked) {
		clickedId = clicked.attr("id").split('#')[1];
		id = clickedId.split('swap-')[1];

		$swap = $('#' + clickedId);
		swapText = $swap.html();

		$current = clicked.parent();
		currentText = $current.html();

		$current.html(swapText);
		$swap.html(currentText);

		// Rebind the click event on the more/less link
		$current.children('a.moreLessSwap').click(function(event) {
			doSwap($(this));
		});
	}

	function autofill(option) {
		if (option == "acceptance") {
			$("#reason")
					.text(
							"We have reviewed your account and the submitted documentation - your account has been approved.");

		} else if (option == "missing documentation") {
			$("#reason")
					.text(
							"We have reviewed your account and have determined that you are missing required documentation.");

		} else if (option == "bad credentials") {
			$("#reason")
					.text(
							"We have reviewed your request and have determined that you have insufficient credentials for having an active account.");
		}
	}

	function convertPrivRadioBtnsToJSON() {
		var privilegeArray = [];

		// Add the account privilages to the array.
		$("#privilegeTable").find("input:radio:checked").each(function() {
			var valueArray = $(this).val().split(",");
			var roleType = parseInt($.trim(valueArray[0]));
			var roleStatus = parseInt($.trim(valueArray[1]));

			// Add account role to array.
			privilegeArray.push(new AccountRole(roleType, roleStatus));
		});

		// Convert the array to JSON, and apply it to the hidden input.
		$("#roleJson").val(JSON.stringify(privilegeArray));
	}
	
	/* function convertExpirationDatesToJSON() {
		var dateArray = [];

		// Add all of the expiration dates to the date array.
		$(".date-picker").each(function() {
			var $txtBx = $(this);
			var strArray = $txtBx.attr("id").split("_");
			var roleType = parseInt($.trim(strArray[1]));
			var expireDate = $.trim($txtBx.val());
			
			// Add new expiration date to the array.
			dateArray.push(new RoleExpiration(roleType, expireDate));
		});

		// Convert the date array to JSON, and added it to the hidden input.
		$("#expireDateJson").val(JSON.stringify(dateArray));
	} */
	
	//TODO: We should evalaute if this function is still being used, and remove it if necessary
	function submitApproveReject(action) {
		convertPrivRadioBtnsToJSON();
		convertExpirationDatesToJSON();
		submitForm(action);
	}

	// NEW ACCOUNT REQUEST - Approve Account
	function approveAccount() {
		if ($(".noActionRadio:checked").length > 0
				&& $("#reason").val().length > 0) {
			var answer = "You have not approved or denied all of this users requests. If approved, this account "
					+ "will still have pending requests.\n\n Would you like to continue?";

			if (confirm(answer)) {
				submitApproveReject('accountReasonValidationAction!approve.action');
			}
		} else {
			submitApproveReject('accountReasonValidationAction!approve.action');
		}
	}

	

	// CHANGE REQUEST - Submit Privileges
	function approvePrivileges() {
		if ($(".noActionRadio:checked").length > 0
				&& $("#reason").val().length > 0) {
			var answer = "You have not approved or denied all of this users requests. This account's status will "
					+ "remain 'Pending' until all requests have been handled.\n\n Would you like to continue?";

			if (confirm(answer)) {
				submitApproveReject('accountReasonValidationAction!approvePrivileges.action');
			}
		} else {
			submitApproveReject('accountReasonValidationAction!approvePrivileges.action');
		}
	}

	function expirationDateInit() {
		$(".approveRadio").each(
				function() {
					var $radioBtn = $(this);
					var $exprDate = $radioBtn.parents("td:first").siblings()
							.find(".date-picker");

					if ($radioBtn.is(":checked")) {
						$exprDate.prop("disabled", false);
						$exprDate.val(setDefaultDate());
					} else {
						$exprDate.val("mm/dd/yyy");
						$exprDate.prop("disabled", true);
					}
				});
	}

	function setDefaultDate() {
		var d = new Date();
		return (d.getMonth() + 1) + "/" + d.getDate() + "/"
				+ (d.getFullYear() + 1);
	}
	
	
	
	//Temporary Rejection Lightbox
	function temporaryRejectionLightbox() {
		var dialog;
		var temporaryRejectionDiv = $("#temporaryRejectionLightboxDiv");
		var lightBoxAction = "temporaryRejectionAction!temporaryRejectionLightbox.ajax";
		
		var errorDialog;
		dialog = temporaryRejectionDiv.dialog({

			autoOpen : false,
			modal : true,
			height : 520,
			width : 700,
			draggable : true,
			resizable : true ,
			title : "Temporarily Reject Request",
			buttons : [ {
				text : "Cancel",
				click : function() {
					$(this).dialog("close");
				}
			}, {
				text : "Temporary Rejection",
				click : function() {
						
						if (!validateTemporaryRejectionCheckbox()){
							errorDialog = $.ibisMessaging("dialog", "error", "Please select 1 or more reasons for temporarily rejecting this account.", {container: "body"});
			            	//$.ibisMessaging("primary", "error", "test",{container: "#messageContainer"});
			            }
			            else if(!validateTemporaryRejection()){
			            	errorDialog = $.ibisMessaging("dialog", "error", "Please provide a text if the email template you selected has a text box associated with it.", {container: "body"});
						} else {
							submitTemporaryRejection();
							$(this).dialog("close");
							
						};
						
						
						
				}
			} ],
			close: function(event,ui) {
				if(errorDialog) {
					$.ibisMessaging("close", {type: 'dialog'});
				}
			}
		
		});
		
		$.ajax({
	  		type: "post",
	  		url: lightBoxAction,

	  		success: function(response) {
	  			if (response == "landing") {
	  				window.location.href = "/portal/baseAction!landing.action";
	  			} else {
		  			temporaryRejectionDiv.html(response);
		  			temporaryRejectionDiv.dialog("open");
	  			}
	  		}});
		
		
	}
	function validateTemporaryRejectionCheckbox() {
		var valid = true;
			   
	   //validate atleast one checkbox is checked 
	   if(!$('.accountTemporaryRejectionCheckBox').is(":checked")){
		   valid = false;
	    }   
		
		return valid;
		
	}
	function validateTemporaryRejection(){
	 		   var valid = true;
			   
			   $("#temporaryRejectionList div").each(function (){
					
					if($(this).find(".accountTemporaryRejectionCheckBox").is(":checked") && $(this).find('.accountTemporaryRejectionMessage').length > 0){
						if($(this).find('.accountTemporaryRejectionMessage').val()==''){
							valid = false;
						}
					}
				});
			   return valid;
	}
	   
	   function getAccountMessageTemplateData(){
			var accountMessageTemplates = [];
			
			$("#temporaryRejectionList div").each(function (){
				var checkbox = $(this).find(".accountTemporaryRejectionCheckBox");
				if(checkbox.is(":checked")){
					var id = checkbox.data('id');
					var text = '';
					
					if($(this).find('.accountTemporaryRejectionMessage').length > 0){
						 text = $(this).find('.accountTemporaryRejectionMessage').val();
					}
					
					var accountMessageTemplate = new AccountMessageTemplate(id,text);
					accountMessageTemplates.push(accountMessageTemplate);
				}
			});
			
			return accountMessageTemplates;
	   }
	   
	   function AccountMessageTemplate(id, userMsg){
		   this.id = id;
		   this.userMsg = userMsg;
	   }
	
	function submitTemporaryRejection() {
		var accountMessages  = getAccountMessageTemplateData();
		$.ajax({
			 
			 type: "POST",
			 url: "temporaryRejectionAction!temporaryRejection.ajax",
			 data: {"accountMessageTemplates" : JSON.stringify(accountMessages)},
			 success: function(data) {
				 if (data == "landing") {
					 window.location.href = "/portal/baseAction!landing.action";
				 } else {
				 	window.location.href = window.location.href;
				 }
			 }
			 
		 });
		 
	}
	
	//Reject Lightbox
	function rejectionLightbox() {
		var dialog;
		var rejectionDiv = $("#rejectionLightboxDiv");
		var lightBoxAction = "rejectionAction!rejectionLightbox.ajax";
		var errorDialog;
		
		dialog = rejectionDiv.dialog({

			autoOpen : false,
			modal : true,
			height : 320,
			width : 700,
			draggable : true,
			resizable : true ,
			title : "Reject Account Request",
			buttons : [ {
				text : "Cancel",
				click : function() {
					$(this).dialog("close");
				}
			}, {
				id: "button-reject",
				text : "Reject",
				click : function() {
						
						if(!validateRejection()){
							errorDialog = $.ibisMessaging("dialog", "error", "Please provide a text of your reason for rejection.", {container: "body"});
						} else {
							if(rejectAccount()) {
								$(this).dialog("close");
							}
							
						};
						
						
						
				},
				open: function( event, ui ) {
				
				}
			} ],
		close: function(event,ui) {
			if(errorDialog) {
				$.ibisMessaging("close", {id: errorDialog});
			}
		}
		});
		
		$.ajax({
	  		type: "post",
	  		url: lightBoxAction,

	  		success: function(response) {
	  			rejectionDiv.html(response);
	  			rejectionDiv.dialog("open");
	  			
	  			var rejectButton = $("#button-reject");
	  			rejectButton.button("disable");
	  			
				$('#rejectionReason').on('keyup paste change',function() {
					
					var rejectionObj = $(this);
					setTimeout(function() {
				        var textarea_value = rejectionObj.val();
				      
					    if(textarea_value != '') {
					    	rejectButton.button("enable");
					    }else{
					    	rejectButton.button("disable");
					    }
				    }, 100);
				   
				});
	  		}});
		
		
	}
	function validateRejection() {
		var valid = true;

		if($(this).find('#rejectionReason').val()==''){
			valid = false;
		}
	
		return valid;
		
	}
	// NEW ACCOUNT REQUEST - Reject Account
	function rejectAccount() {
				var action = 'rejectionAction!reject.action';
				var $form = $("#rejectionForm");
				$form.attr("action", action);
				$form.submit();
				return true;
	}
	
	function partialApprovalLightbox() {
		var dialogOne;
		var $partialDiv = $("#partialApprovalLightboxDiv");
		var firstActionAjax = "partialApprovalAction!partialApprovalLightbox.ajax";
		
		 dialogOne = $partialDiv.dialog({
				
				autoOpen :false,
				modal : true,
				height : 400,
				width : 800,
				draggable : false,
				resizable : false,
				title : "Partial Approval",
				buttons: [{
					text: "Cancel",
					click: function() {
						$(this).dialog("close");
					}
				},{
					text: "Partial Approval",
					click: function() {
						if(validateComments()) {
							partialApproval();
							$(this).dialog("close");
						}
					}
				}]
			});
		 

		 $.ajax({
		  		type: "post",
		  		url: firstActionAjax,

		  		success: function(response) {
		  			$partialDiv.html(response);
					$partialDiv.dialog("open");
		  		}});
		
	}
	
	
	
	function validateComments() {
		var partialApprovalComments =  $("#partialApprovalComments").val();
		var valid = true;
		if (typeof partialApprovalComments !== 'undefined') {
			if (partialApprovalComments.length == 0) {
				$("#validatePartialApprovalComments").show();
				valid = false;
			}
		}
		return valid;
	}
	
	
	
	function partialApproval() {
		
		var secondActionAjax = "partialApprovalAction!partialApproval.ajax";
		var partialApprovalComments =  $("#partialApprovalComments").val();

		$.ajax({
	  		type: "post",
	  		data : {
				"partialApprovalComments" : partialApprovalComments
			},
	  		url: secondActionAjax,

	  		success: function(response) {
	  			if (response == "landing") {
	  				window.location.href = "/portal/baseAction!landing.action";
	  			} else {
		  			//refresh the page
		  			window.location.href = window.location.href;
		  		}
	  		},
	  		error : function(error) {
	  			alert(error)
	  		}
		});
				
	}
	
	
	
	
	function finalApprovalLightbox() {
		var dialogOne;
		var $finalDiv = $("#finalApprovalLightboxDiv");
		var firstActionAjax = "finalApprovalAction!finalApprovalLightbox.ajax";
		
		 dialogOne = $finalDiv.dialog({
				
				autoOpen :false,
				modal : true,
				height : 550,
				width : 800,
				draggable : false,
				resizable : false,
				title : "Final Approval",
				buttons: [{
					text: "Cancel",
					click: function() {
						$(this).dialog("close");
					}
				},{
					text: "Final Approval",
					click: function() {
						if(validateFinalComments()) {
							$("#finalApprovalForm").submit();
						}
					}
				}]
			});
		 

		 $.ajax({
		  		type: "post",
		  		url: firstActionAjax,

		  		success: function(response) {
		  			$finalDiv.html(response);
					$finalDiv.dialog("open");
		  		}});
		
	}
	
	
	

	function validateFinalComments() {
		var finalApprovalComments =  $("#finalApprovalComments").val();
		var valid = true;
		if (typeof finalApprovalComments !== 'undefined') {
			if (finalApprovalComments.length == 0) {
				$("#validateFinalApprovalComments").show();
				valid = false;
			}
		}
		return valid;
	}
</script>