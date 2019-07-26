<%@include file="/common/taglibs.jsp"%>
<title>My Profile</title>

<div class="clear-float">
	<h1>Account Management</h1>
</div>

<!-- begin .border-wrapper -->
<div class="border-wrapper">

	<jsp:include page="../navigation/userManagementNavigation.jsp" />

	<!--begin #center-content -->
	<div id="main-content">

		<h2>My Profile</h2>

		<s:form id="theForm" cssClass="validate" method="post" validate="true"
			enctype="multipart/form-data">
			<s:token />
			<div class="flex-justify-start">
				<div style="width: 33%">
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
					<s:if test="%{accountDetailsForm.eraId!=null}">
						<div class="form-output">
							<div class="label">NIH Federal Identity:</div>
							<div class="readonly-text">
								<c:if test="${currentAccount.eraId == ''}">None Specified</c:if>
								<s:property value="currentAccount.eraId" />
							</div>
						</div>
					</s:if>
				</div>

				<div style="width: 33%">
					<div class="form-output">
						<label class="label"><strong>Account Status:</strong></label> <strong>
							<s:if test="currentAccount.accountStatus.id == 3">New Account Request</s:if>
							<s:elseif test="currentAccount.accountStatus.id == 4">Change Requested</s:elseif>
							<s:else>
								<s:property value="currentAccount.accountStatus.name" />
							</s:else>
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
			
				</div>
				<div style="width: 33%">
<!-- 						<span class="icon"><a -->
<%-- 							href="viewUserAccount!viewUserAccount.action?accountId=${currentAccount.id}"><img --%>
<%-- 								alt="Edit" src="<s:url value='/images/icons/edit.png' />" /></a></span> --%>
					<span class="icon"><a
						href="editProfile!view.action"><img
							alt="Edit" src="<s:url value='/images/icons/edit.png' />" /></a></span>
				</div>
			</div>
			
			<s:if test="currentAccount.accountStatus.name == 'Requested' || currentAccount.accountStatus.name == 'Pending'">
				<div class="margin-top-med margin-bottom-med">
					<div class="button no-float">
						<input type="button"
							onclick="withdrawAccountRequestLightbox();"
							value="Withdraw Account Request" />
					</div>
				</div>
			</s:if>


			<s:if test="%{currentAccount.accountStatus.name == 'Requested' || currentAccount.accountStatus.name == 'Pending' || currentAccount.accountStatus.name == 'Change Requested'}">
				<jsp:include page="includes/accountPrivileges.jsp" >
					<jsp:param name="idtUrl" value="viewProfile!getAccountRoleLists.action"/>
				</jsp:include>
				
				<jsp:include page="includes/accountPermissionGroups.jsp">
					<jsp:param name="idtUrl" value="viewProfile!getPermissionGroupMembers.action"/>
				</jsp:include>
			</s:if>
			<s:else>
				<!-- Output Existing Admin Privileges -->
				<jsp:include page="includes/existingPrivileges.jsp" />
			</s:else>

			<!-- Output Existing Administrative Files -->
			<jsp:include page="includes/existingAdministrativeFiles.jsp" />
			
				<!-- Display the file templates section -->		
		   	<jsp:include page="includes/adminFileTemplatesInterface.jsp" />
				

			<jsp:include page="includes/accountActionHistory.jsp">
				<jsp:param name="idtUrl" value="viewProfile!getAccountHistory.action"/>
			</jsp:include>

<!-- 			<div class="button "> -->
<!-- 				<input type="button" -->
<!-- 					onclick="parent.location.href='editProfile!view.action'" -->
<!-- 					value="Edit My Profile" /> -->
<!-- 			</div> -->
		</s:form>
		<!-- end of #main-content -->
	</div>
</div>
<!-- end of .border-wrapper -->


<div id="withdrawAccountRequestLightboxDiv" style="display: none">

</div>

<script type="text/javascript">

<s:if test="%{inAccountReviewer}">
	setNavigation({"bodyClass":"primary", "navigationLinkID":"userManagementModuleLink", "subnavigationLinkID":"managingAccountsLink", "tertiaryLinkID":"accountsForRenewalLink"});
</s:if> 
<s:else>
	setNavigation({	"bodyClass" : "primary", "navigationLinkID" : "userManagementModuleLink","subnavigationLinkID" : "userManagementToolLink","tertiaryLinkID" : "viewProfileLink"});
</s:else>

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
				window.location.href = "/portal/logout";

			},
			error : function(error) {
				alert(error)
			}
		});

	}
</script>

