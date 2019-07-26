<%@include file="/common/taglibs.jsp"%>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"%>
<s:if test="%{isCreate}"><title>Create Account Group</title></s:if>
<s:else><title>Edit Account Group</title></s:else>

<div class="clear-float">
	<div class="clear-float">
		<h1 class="float-left">Account Management</h1>
	</div>
</div>

<div class="border-wrapper">
	<jsp:include page="../navigation/userManagementNavigation.jsp" />
	<div id="main-content">
		<div class="clear-float">
			<s:if test="%{isCreate}">
				<h2>Create Account Group</h2>
			</s:if>
			<s:else>
				<h2>Edit Account Group</h2>
			</s:else>
			<p>Please fill out the following information to create a new account group.</p>
			<p class="required">
				Fields marked with a <span class="red-text">* </span>are required.
			</p>
			<div class="clear-right"></div>
			<h3>Account Group Information</h3>

			<s:form id="theForm" cssClass="validate" method="post" name="permissionGroupForm" validate="true">
				<s:token />

				<s:hidden name="permissionGroupForm.id" escapeHtml="true" escapeJavaScript="true" />

				<div class="form-field">
					<label for="permissionGroupForm.groupName" class="required">Name <span class="required">* </span>:
					</label>
					<s:textfield id="permissionGroupForm.groupName" name="permissionGroupForm.groupName" cssClass="textfield required"
							maxlength="100" escapeHtml="true" escapeJavaScript="true" />
					<s:fielderror fieldName="permissionGroupForm.groupName" />
				</div>

				<div class="form-field">
					<label for="permissionGroupForm.groupDescription" class="required">Description <span class="required">*
					</span>:
					</label>
					<s:textfield id="permissionGroupForm.groupDescription" name="permissionGroupForm.groupDescription"
							cssClass="textfield required" maxlength="100" escapeHtml="true" escapeJavaScript="true" />
					<s:fielderror fieldName="permissionGroupForm.groupDescription" />
				</div>

				<div class="form-field">
					<label for="publicStatus">Public Group :</label>
					<s:checkbox id="publicStatus" name="permissionGroupForm.publicStatus" />
				</div>

				<div id="memberSelect">
					<s:if test="!accessRequests.isEmpty()">
						<h3>Grant Account Group Access</h3>
						<p>The following users have requested access to this account group</p>
						<table id="memberAppovalTable" class="display-data full-width">
							<tr>
								<th>Name</th>
								<th>Admin Function</th>
							</tr>
							<c:forEach var="member" items="${accessRequests}">
								<tr>
									<td>${member.account.user.fullName} (${member.account.user.email})</td>
									<td>
										<ul class="checkboxgroup-horizontal">
											<li>
												<input type="radio" id="approve_${member.id}" class="approveRadio" 
													name="memberApproval_${member.id}" value="${member.id}, 0" />
												<label for="approve_${member.id}">Approve</label>
											</li>
											<li>
												<input type="radio" id="deny_${member.id}" class="notApproveRadio" 
													name="memberApproval_${member.id}" value="${member.id}, 2" />
												<label for="deny_${member.id}">Deny</label>
											</li>
											<li>
												<input type="radio" id="request_${member.id}" class="notApproveRadio noActionRadio" 
													name="memberApproval_${member.id}" value="${member.id}, 1" checked="checked" />
												<label for="request_${member.id}">No Action</label>
											</li>
										</ul>
									</td>
								</tr>
							</c:forEach>
						</table>
						<input type="hidden" id="memberApprovalListJSON" name="permissionGroupForm.memberApprovalJSON" value="[]" />
					</s:if>

					<h3>Account Group Members</h3>
					<p>Add or remove members from the account group</p>
					<div class="form-field">
						<label for="userSearchKey">Filter Users:</label> <input class="textfield" name="userSearchKey" id="userSearchKey"
							type="text" onkeyup="searchKeywords();" />
					</div>
					<select id="hiddenAccounts" style="display: none;">
					</select>

					<div class="center-panel">
						<s:optiontransferselect label="" name="availableAccounts" doubleName="permissionGroupForm.memberSet"
							id="availableAccounts" list="availableAccounts" doubleList="permissionGroupForm.memberSet" listKey="id"
							doubleListKey="account.id" listValue="user.fullName+' ('+user.email+')'"
							doubleListValue="account.user.fullName+' ('+account.user.email+')'" allowAddAllToLeft="false"
							allowAddAllToRight="false" allowSelectAll="false" allowUpDownOnLeft="false" allowUpDownOnRight="false"
							buttonCssClass="leftRightButton margin-left margin-right" cssClass="form-field" doubleCssClass="form-field currentUsers"
							cssStyle="width: 300px; height: 150px; float: left;" doubleCssStyle="width: 300px; height: 150px; float: left;" 
							addToLeftLabel="<<"
							addToRightLabel=">>"
						>
						</s:optiontransferselect>
					</div>

				</div>

				<div class="button">
					<s:if test="isCreate">
						<input type="button" value="Submit"
							onClick="submitAccountGroupForm('permissionGroupValidationAction!submit.action')" />
					</s:if>
					<s:else>
						<input type="button" value="Save" 
							onClick="submitAccountGroupForm('permissionGroupValidationAction!submit.action')"  />
					</s:else>
				</div>
			</s:form>
		</div>
	</div>
</div>

<script type="text/javascript">
	/**
	 * Constructor for the PermissionGroup JavaScript object.
	 *
	 * @param memeberId - The ID of the group member.
	 * @param status - The status ID of the member.
	 */
	function PermissionGroup(memberId, status) {
		this.memberId = memberId;
		this.status = status;
	}
	
	/**
	 * Coverts the values of the all selected approval radio buttons into a JSON string. The
	 * string is then stored in the "memberApprovalListJSON" hidden input field.
	 */
	function convertApprovalsToJSON() {
		var memberArray = [];
		
		// Loop through all selected radio buttons in the table.
		$("#memberAppovalTable").find("input:radio:checked").each(function() {
			var vals = $(this).val().split(",");
			var memberId = parseInt($.trim(vals[0]));
			var status = $.trim(vals[1]);
			
			// Add new member to the array.
			memberArray.push(new PermissionGroup(memberId, status));
		});
		
		// Store the array as a JSON string in the "memberApprovalListJSON" hidden input field.
		$("#memberApprovalListJSON").val(JSON.stringify(memberArray));
	}
	
	$('document').ready(function() {
		<s:if test="isCreate">
			setNavigation({"bodyClass":"primary", "navigationLinkID":"userManagementModuleLink", "subnavigationLinkID":"managingUsersLink", "tertiaryLinkID":"createPermissionGroupLink"});
		</s:if>
		<s:else>
			setNavigation({"bodyClass":"primary", "navigationLinkID":"userManagementModuleLink", "subnavigationLinkID":"managingUsersLink", "tertiaryLinkID":"permissionGroupList"});
		</s:else>
		
		publicCheckInit();
		<c:forEach var="member" items="${accessRequests}">
			disableCurrentMember(${member.account.id});
		</c:forEach>
		
		// Wrap the left and right buttons in divs
		$(".leftRightButton").wrap("<div class=button />");
	});
	
	function submitAccountGroupForm(action) {
		
		selectAllCurrentUsers();
		convertApprovalsToJSON();
		submitForm(action);
	}
	
	function disableCurrentMember(accountId) {
		$('.currentUsers option[value=' + accountId + ']').prop('disabled', true);
	}
	
	//searches for keywords
	function searchKeywords()
	{
		var searchData = $('#userSearchKey').val();	//Get the text box where the user is typing in search letters
		
		// The moveOptions function is defined in optiontransferselect.js
		moveOptions($("#availableAccounts")[0], $("#hiddenAccounts")[0], false,
			function(opt) {
				if (opt.text.toUpperCase().indexOf(searchData.toUpperCase()) != -1) {
					return false;
				}
				else {
					return true;
				}
		});
		
		moveOptions($("#hiddenAccounts")[0], $("#availableAccounts")[0], false,
			function(opt) {
				if (opt.text.toUpperCase().indexOf(searchData.toUpperCase()) != -1) {
					return true;
				}
				else {
					return false;
				}		
		});
	}
	
	function selectAllCurrentUsers() {
		 var $members = $(".currentUsers");
		 
		 // If the list contains no options, insert one with the value: 'empty'
		 if ($members.is(":empty")) {
			 $members.append("<option value='empty'></option>");
		 }
		 
		 // Select all members
		 $members.children("option").prop("selected", true);
	}
	
	function publicCheckInit() {
		if($("#publicStatus").is(":checked")) {
			$("#memberSelect").hide();
		}
		 
		$('#publicStatus').change(function() {
			if ($(this).is(":checked")) {
				$("#memberSelect").hide("slow");
			}
			else {
				$("#memberSelect").show("slow");
			}
		});
	}
</script>