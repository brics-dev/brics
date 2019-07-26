<%@include file="/common/taglibs.jsp"%>
<form id="permissionsForm">
<div class="flex-vertical flex-justify-around">
	<p>Based on the selected role, the following privileges will be pre-populated for this account; check or uncheck 
	boxes, as needed:</p>

<div id="privileges" class="account_previleges_collapsable">
	<ul id="accountRoleListing" class="checkboxgroup-vertical">

	<s:iterator var="role" value="roleList">
		<s:if test="(inAccountAdmin || #role.name != 'ROLE_ACCOUNT_REVIEWER')">
			<s:set var="disabled" value="false" />
			<s:set var="checked" value="false" />
			<s:set var="currentRole" value="null"/>
			
			<%-- Loop through all the roles that account already has, find if the current role is in it. --%>
			<s:iterator var="ar" value="currentAccount.accountRoleList">
				<s:if test="#currentRole == null && #ar.roleType.id == #role.id">
					<s:set var="currentRole" value="#ar"/> 
				</s:if>
			</s:iterator>
			
			<%-- Setting the disabled and checked values --%>
			<s:if test="isAccountRejected">
				<s:set var="disabled" value="true" />
			</s:if>
			
			<s:if test="#currentRole != null">
				<s:if test="inAccountAdmin && isAccountActive">
					<s:if test="#currentRole.roleStatus.name == 'Active'">
						<s:set var="checked" value="true" />
					</s:if>
				</s:if>
				<s:elseif test="#currentRole.roleStatus.name != 'Inactive'">
					<s:set var="checked" value="true" />
				</s:elseif>
			</s:if>
			
			<!-- If NOT Biosample Orders role and FITBIR instance -->
			<s:if test="!(#role.name == 'ROLE_ORDER_ADMIN' && instanceType.name == 'FITBIR')"> 
				<s:if test="!(inAccounts && #role.isAdmin)">
				<li>
					
					<!-- Admin creates a user or edit active user account, no approval needed -->
					
						<s:if test="#role.name == 'ROLE_USER'">
							<input type="checkbox" id="role_0" disabled checked />
							<input type="hidden" name="accountPrivilegesForm.accountRoleList" value="0, 0" />
						</s:if>
						<s:elseif test="disabled">
							<input type="checkbox" disabled />
						</s:elseif>
						<s:else>
							<input type="checkbox" id="role_${role.id}" class="roleCheckbox" <s:if test="checked">checked</s:if> 
									name="accountPrivilegesForm.accountRoleList" value="${role.id}, 0" />
						</s:else>

						<label for="role_${role.id}"><strong>${role.title}</strong>&nbsp;-&nbsp;${role.description}</label>
						
						<s:if test="#role.name != 'ROLE_USER' && !disabled">
							<div id="expirationDate${role.id}" class="expirationDate">
								<label>Expiration Date (Optional):</label>
								
								<s:set var="expirationString" value=""/>
								<s:if test="#currentRole != null && #currentRole.expirationDate != null">
									<s:set var="expirationString" value="#currentRole.expirationString" />
								</s:if>
								<input type="text" id="roleExpiration_${role.id}" maxlength="10" 
										class="date-picker small textfield" value="${expirationString}" />
											
								<a id="clearLink_${role.id}" class="clearTextLink" style="width: 20px;">Clear</a>
							</div>
						</s:if>
				

					</li>
				</s:if>
			</s:if>
		</s:if>
	</s:iterator>
	</ul>
</div>



<%-- Hidden input for setting the privilege expiration dates.
<input type="hidden" id="expireDateJson" name="accountPrivilegesForm.accountRoleExpirationJson" value="[]" />
 --%>


</div>
</form>

<script>
var userRoleId = <s:property value="@gov.nih.tbi.commons.model.RoleType@ROLE_USER.id" />;
function expirationDateInit() {
	
	$(".roleCheckbox").each(function() {
		var $chkBx = $(this);
		var id = $chkBx.attr("id").split("_")[1];
		var $exprDtSection = $("#expirationDate" + id);
		if(!$chkBx.is(":checked")) {
			$exprDtSection.find(".date-picker").val("");
			$exprDtSection.hide();
		} else {
			$exprDtSection.show();
		}
	});
}

$('document').ready(function() {
	$(".date-picker").each(function() {
		$(this).datepicker({ 
			buttonImage: "/portal/images/brics/common/icon-cal.gif", 
			buttonImageOnly: true ,
			buttonText: "Select Date", 
			changeMonth: true,
			changeYear: true,
			duration: "fast",
			gotoCurrent: true,
			hideIfNoPrevNext: true,
			showOn: "both",
			showAnim: "blind",
			yearRange: '-120:+5'
		});
	}).attr("readonly", "readonly");
	
	expirationDateInit();
	
	// Event Handlers
	$(".roleCheckbox").change(function() {
		var $chkBx = $(this);
		var id = $chkBx.attr("id").split("_")[1];
		var $exprDtSection = $("#expirationDate" + id);
		
		if(!$chkBx.is(":checked")) {
			$exprDtSection.find(".date-picker").val("");
			$exprDtSection.hide("slow");
		} else {
			$exprDtSection.show("slow");
		}
	});
	
	
	$(".clearTextLink").click(function(event) {
		event.preventDefault();
		console.log('clear');
		var id = $(this).attr("id").split("_")[1];
		
		$("#roleExpiration_" + id).val("");
	});
});


function enableAllPrivileges(){
	$('#privileges :checkbox').removeAttr("disabled");
	$('#role_' + userRoleId).attr("disabled", true);	//account should always be selected and disabled
}

</script>