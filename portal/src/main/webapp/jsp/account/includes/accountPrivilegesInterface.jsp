<%@include file="/common/taglibs.jsp"%>

<!-- Do not do this section if its an account request -->
<s:if test="%{!isRequest && (!currentAccount.accountRoleList.isEmpty() || !currentAccount.permissionGroupMemberList.isEmpty())}">
	<table class="display-data full-width">
		<thead>
			<tr>
				<th>Privilege</th>
				<th>Status</th>
				<th>Expiration Date</th>
			</tr>
		</thead>
		<tbody>
			<s:iterator var="ar" value="currentAccount.orderedAccountRoles">
				<tr>
					<td>${ar.roleType.title}</td>
					<td>
						<s:if test="#ar.isExpired()">Expired</s:if>
						<s:else>${ar.roleStatus.name}</s:else>
					</td>
					<td>
						<s:if test="#ar.expirationDate != null">${ar.expirationString}</s:if>
						<s:else>No Expiration Date</s:else>
					</td>
				</tr>
			</s:iterator>
			
			<s:iterator var="pgm" value="currentAccount.permissionGroupMemberList">
				<tr>
					<td>${pgm.permissionGroup.groupName}</td>
					<td>${pgm.permissionGroupStatus.name}</td>
					<td>${pgm.expirationString}</td>
				</tr>
			</s:iterator>
		</tbody>
	</table>
</s:if>

<!-- start permission group checklist -->
<div style="margin-left:3em;">
	<div style="width:50%; float:left">
		<label for="dataSubmitterWithProforms" style="width:auto;font-size: 12px">
			<input type="checkbox" id="dataSubmitterWithProforms" name="roleRadioGroup" class="DSR"/> Data Submitter With ProFoRMS
		</label>
	</div>
	<div style="width:50%; float:left">
		<label for="dataAccessor" style="width:auto;font-size: 12px">
			<input type="checkbox" id="dataAccessor" name="roleRadioGroup" class="DAR"/> Data Accessor
		</label>
	</div>
	<div style="width:50%; float:left">
		<label for="dataSubmitterWithoutProforms" style="width:auto;font-size: 12px">
			<input type="checkbox" id="dataSubmitterWithoutProforms" name="roleRadioGroup" class="DSR"/> Data Submitter Without ProFoRMS
		</label>
	</div>
	<div style="width:50%; float:left">
		<label for="Other" style="width:auto;font-size: 12px">
			<input type="checkbox" id="Other" name="roleRadioGroup" class = "Other"/> Other
		</label>
	</div>
	<div style="clear:both"></div>
	<input type = "hidden" id="accountType" name="accountPrivilegesForm.accountType" />
</div>

<s:if test="%{isRequest}">
	<div>
</s:if>
<s:else>
	<s:if test="%{inAccounts}">
		<p>You may request additional privileges below. Please note that requesting privileges requires an administrator's
			approval and in some cases, will require additional documentation to be uploaded.</p>
	</s:if>
	<s:elseif test="%{inAccountAdmin}">
		<p>You may edit a user's permissions below. Any permissions that are active or awaiting approval cannot be selected
			on this screen. Please use the link in the table to navigate to the pending approval screen.</p>
	</s:elseif>
	
	<div class="account_previleges_collapsable">
		<h3>
			<a href=""><span class="moreIcon">[+]&nbsp;</span><span class="lessIcon">[-]&nbsp;</span>
				<s:if test="%{inAccounts}">Request </s:if>Additional Privileges</a>
		</h3>
</s:else>

<div style="clear: both;"></div><br/>
<p>Based on the selected role, the following privileges will be pre-populated for this account; check or uncheck 
	boxes, as needed:</p>

<div class="collapsableContent" id="privileges">
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
					<s:if test="#currentRole.roleStatus.name == 'Active' || #currentRole.roleStatus.name == 'Active - Expiring Soon'">
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
					<!-- Request an account or Admin edit pending user account -->
					<s:if test="inAccounts || (inAccountAdmin && !isAccountActive)">
						<s:if test="#role.name == 'ROLE_USER'">
							<input type="checkbox" id="role_0" disabled checked />
							<input type="hidden" name="accountPrivilegesForm.accountRoleList" value="0, 0" />
						</s:if>
						<s:elseif test="disabled">
							<input type="checkbox" disabled <s:if test="checked">checked</s:if> />
						</s:elseif>
						<s:else>
							<s:if test="checked">
								<input type="checkbox" id="role_${role.id}" name="accountPrivilegesForm.accountRoleList" 
										value="${role.id}, ${currentRole.roleStatus.id}" checked />
								<input type="hidden" id="expirationDate_${role.id}" class="hiddenExpireDate"
										name="accountPrivilegesForm.accountRoleExpiration" value="${role.id}, ${currentRole.expirationString}" />
							</s:if>
							<s:else>
								<input type="checkbox" id="role_${role.id}" name="accountPrivilegesForm.accountRoleList" value="${role.id}, 1" />
							</s:else>
						</s:else>
						
						<label for="role_${role.id}"><strong>${role.title}</strong></label>-&nbsp;${role.description}
					</s:if> 
					
					<!-- Admin edit active user account, no approval needed -->
					<s:if test="inAccountAdmin && isAccountActive">
						<s:if test="#role.name == 'ROLE_USER'">
							<input type="checkbox" id="role_0" disabled checked />
							<input type="hidden" name="accountPrivilegesForm.accountRoleList" value="0, 0" />
						</s:if>
						<s:elseif test="disabled">
							<input type="checkbox" disabled />
						</s:elseif>
						<s:else>
							<s:if test="#currentRole.roleStatus.name == 'Active - Expiring Soon'">
								<input type="checkbox" id="role_${role.id}" class="roleCheckbox" <s:if test="checked">checked</s:if> 
										name="accountPrivilegesForm.accountRoleList" value="${role.id}, 5" />
							</s:if>
							<s:else>
							    <input type="checkbox" id="role_${role.id}" class="roleCheckbox" <s:if test="checked">checked</s:if> 
										name="accountPrivilegesForm.accountRoleList" value="${role.id}, 0" />
							</s:else>
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
					</s:if>

					</li>
				</s:if>
			</s:if>
		</s:if>
	</s:iterator>
	</ul>
</div>

<%-- Hidden input for setting the privilege expiration dates. --%>
<input type="hidden" id="expireDateJson" name="accountPrivilegesForm.accountRoleExpirationJson" value="[]" />

<div>
	<s:if test="!inAccountAdmin || !isAccountActive">
		<h3>Data Access Permission Groups</h3>
		<p>
			Please check the data access permission group(s) for which you are requesting access. Please note that 
			requesting data access requires administrator approval and may require Data Access Committee approval and 
			additional uploaded documentation. You will receive notification regarding approval or if further action is required.
		</p>
		<s:if test="permissionGroupList.isEmpty()">
			<strong>No permission groups available</strong>
		</s:if>
		
		<ul class="checkboxgroup-vertical">
			<s:iterator var="permissionGroup" value="permissionGroupList">
				<s:set var="disabled" value="false" />
				<s:set var="checked" value="false" />
				
  				<s:iterator var="pgm" value="currentAccount.permissionGroupMemberList">
					<s:if test="#pgm.permissionGroup.id == #permissionGroup.id">
						<s:set var="checked" value="true" />
						<s:if test="isAccountActive">
							<s:set var="disabled" value="true" />
						</s:if>
					</s:if>
				</s:iterator>
				
				<li>
					<input type="checkbox" id="pg${permissionGroup.id}" class="permissionGroup"
							name="accountPrivilegesForm.permissionGroupMemberList" value="${permissionGroup.id}" 
							<s:if test="disabled">disabled</s:if> <s:if test="checked">checked</s:if> />

					<label class="permissionGroup" for="pg${permissionGroup.id}">
							${permissionGroup.groupName}</label>-&nbsp;${permissionGroup.groupDescription}
				</li>
			</s:iterator>
		</ul>
	</s:if>
</div>
</div>

<s:if test="%{!isRequest || inAccountAdmin}">
	<s:if test="!isAccountRejected">
		<div><jsp:include page="uploadAdminFileInterface.jsp" /></div>
	</s:if>

	<div class="clear-both"></div><br/>
	
	<s:if test="!currentUserFiles.isEmpty()">
		<h4>Existing Files</h4>
		<table id="existingFileTable" class="display-data full-width">
			<thead>
				<tr>
					<th>File Name</th>
					<th>File Type</th>
					<th>Date Submitted</th>
				</tr>
			</thead>
			<tbody>
				<s:iterator var="uploadedFile" value="currentUserFiles">
					<tr class="odd">
						<td><a href="fileDownloadAction!download.action?fileId=${uploadedFile.id}">${uploadedFile.name}</a></td>
						<td>${uploadedFile.description}</td>
						<td>${uploadedFile.uploadDateString}</td>
					</tr>
				</s:iterator>
			</tbody>
		</table>
	</s:if>
	
	<jsp:include page="adminFileTemplatesInterface.jsp" />
</s:if>

<script type="text/javascript">
	var userRoleId = <s:property value="@gov.nih.tbi.commons.model.RoleType@ROLE_USER.id" />;
	var guidRoleId = <s:property value="@gov.nih.tbi.commons.model.RoleType@ROLE_GUID.id" />;
	var proFormsRoleId = <s:property value="@gov.nih.tbi.commons.model.RoleType@ROLE_PROFORMS.id" />;
	var dictionaryRoleId = <s:property value="@gov.nih.tbi.commons.model.RoleType@ROLE_DICTIONARY.id" />;
	var repositoryRoleId = <s:property value="@gov.nih.tbi.commons.model.RoleType@ROLE_STUDY.id" />;
	var queryRoleId = <s:property value="@gov.nih.tbi.commons.model.RoleType@ROLE_QUERY.id" />;
	var metaStudyId = <s:property value="@gov.nih.tbi.commons.model.RoleType@ROLE_METASTUDY.id" />;
	var accountType = "<s:property value="currentAccount.accountReportingType" />";
	var hasProforms = "<s:property value="accountPrivilegesForm.hasProformsPrivilege" />";
	var isAdmin = "<s:property value="inAccountAdmin" />";
	
	
	$('document').ready(function() {
		
		if(accountType.indexOf("DSR") > -1){
			if(hasProforms == "true"){
				$('#dataSubmitterWithProforms').not(this).prop('checked', true);
			}
			else{
				$('#dataSubmitterWithoutProforms').not(this).prop('checked', true);
			}
		}
		
		if(accountType.indexOf("DAR") > -1){
			$('#dataAccessor').not(this).prop('checked', true);
		}
		
		if(accountType == "OTHER"){
			$('#Other').not(this).prop('checked', true);
		}
		
		expirationDateInit();
				
		// Disable all privilege checkboxes when Requesting a New Account or Admin Create a User,
		// it enforces user to select a role group first before going further.
		<s:if test="isRequest">
			$('#privileges :checkbox').attr("disabled", true);
		</s:if>
		
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
		
		$('input.DSR').on('change', function() {
		    $('input.DSR').not(this).prop('checked', false);  
		    $('input.Other').not(this).prop('checked', false); 
		});
		
		$('input.Other').on('change', function() {
		    $('input.DSR').not(this).prop('checked', false);  
		    $('input.DAR').not(this).prop('checked', false); 
		});
		
		$('input.DAR').on('change', function() {
		    $('input.Other').not(this).prop('checked', false);
		});
		
		$("input[name=roleRadioGroup]:checkbox").change(function () {

			unselectAllPrivileges();
			if ($('#dataSubmitterWithProforms').is(':checked')) {    
				checkDSRWithProformsPrivileges();
			} else if ($('#dataSubmitterWithoutProforms').is(':checked')) {
				checkDSRWithoutProformsPrivileges();		    	
			} else if ($('#dataAccessor').is(':checked')) {
				checkDARPrivileges();			    
			} else if ($('#Other').is(':checked')) {
				checkOtherPrivileges();
			}
			
			calculateAccountType();
			expirationDateInit();
		});
		
		$(".clearTextLink").click(function(event) {
			event.preventDefault();
			var id = $(this).attr("id").split("_")[1];
			
			$("#roleExpiration_" + id).val("");
		});
	});
	

	function enableAllPrivileges(){
		$('#privileges :checkbox').removeAttr("disabled");
		$('#role_' + userRoleId).attr("disabled", true);	//account should always be selected and disabled
	}
	
	function unselectAllPrivileges() {
		$('#privileges :checkbox').prop('checked', false);
		$('#role_' + userRoleId).attr('checked', true);  	//account should always be selected and disabled
	}
	
	function disableAllPrivileges(){
		$('#privileges :checkbox').attr("disabled", true);
	}
	
	function calculateAccountType(){
		if (($('#dataSubmitterWithProforms').is(':checked') || $('#dataSubmitterWithoutProforms').is(':checked')) && $('#dataAccessor').is(':checked')) { 
			document.getElementById("accountType").value = "DSR + DAR";
		}
		else if (($('#dataSubmitterWithProforms').is(':checked') || $('#dataSubmitterWithoutProforms').is(':checked'))) { 
			document.getElementById("accountType").value = "DSR";
		}
		else if ($('#dataAccessor').is(':checked')) { 
			document.getElementById("accountType").value = "DAR";
		}
		else if ($('#Other').is(':checked')) { 
			document.getElementById("accountType").value = "OTHER";
		}
	}
	
	function checkDSRWithProformsPrivileges(){
		if(isAdmin == "false"){
			disableAllPrivileges();
		}
		$('#role_' + userRoleId).prop('checked', true);			// Account
    	$('#role_' + guidRoleId).prop('checked', true);			// GUID
    	$('#role_' + dictionaryRoleId).prop('checked', true);   // Data Dictionary
    	$('#role_' + repositoryRoleId).prop('checked', true);	// Data Repository
    	$('#role_' + queryRoleId).prop('checked', false);		// Query
	    $('#role_' + proFormsRoleId).prop('checked', true);		// ProFoRMS
	    if ($('#dataAccessor').is(':checked')){
	    	$('#role_' + queryRoleId).prop('checked', true);		// Query
	    	$('#role_' + metaStudyId).prop('checked', true);    // Metastudy
	    }
		
	}
	
	function checkDSRWithoutProformsPrivileges(){
		if(isAdmin == "false"){
			disableAllPrivileges();
		}
		$('#role_' + userRoleId).prop('checked', true);			// Account
    	$('#role_' + guidRoleId).prop('checked', true);			// GUID
    	$('#role_' + dictionaryRoleId).prop('checked', true);   // Data Dictionary
    	$('#role_' + repositoryRoleId).prop('checked', true);	// Data Repository
    	$('#role_' + queryRoleId).prop('checked', false);		// Query
    	if ($('#dataAccessor').is(':checked')){
    		$('#role_' + queryRoleId).prop('checked', true);		// Query
	    	$('#role_' + metaStudyId).prop('checked', true);    // Metastudy
	    }
		
	}
	
	function checkDARPrivileges(){
		if(isAdmin == "false"){
			disableAllPrivileges();
		}
		$('#role_' + userRoleId).prop('checked', true);			// Account
    	$('#role_' + dictionaryRoleId).prop('checked', true);   // Data Dictionary
    	$('#role_' + repositoryRoleId).prop('checked', true);	// Data Repository
    	$('#role_' + queryRoleId).prop('checked', true);		// Query
    	$('#role_' + metaStudyId).prop('checked', true);		// Metastudy
		
	}
	
	function checkOtherPrivileges(){
		enableAllPrivileges();
		unselectAllPrivileges();
		$('#role_' + userRoleId).prop('checked', true);			// Account
	    $('#role_' + userRoleId).prop("disabled", true);
	}



	$("div.account_previleges_collapsable").each(function(c) {
		$(this).find("h3 a").attr("id", c);
		$(this).attr("id", "container_" + c);
		$(this).find(".lessIcon").attr("id", "lessIcon_" + c);
		$(this).find(".moreIcon").attr("id", "moreIcon_" + c);
		$(this).find(".lessIcon").hide();
		$(this).find(".collapsableContent").hide();
		$(this).find("h3 a").click(function() {
			var f = "#container_" + $(this).attr("id");
			var d = "#lessIcon_" + $(this).attr("id");
			var e = "#moreIcon_" + $(this).attr("id");
			$(f).find(d).toggle();
			$(f).find(e).toggle();
			$(f).find(".collapsableContent").toggle("slow");
			return false;
		})
	});

</script>