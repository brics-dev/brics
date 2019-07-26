<%@include file="/common/taglibs.jsp"%>
<!-- Requires the include to be in a form tag -->

<h3>Request Additional Privileges</h3>
<p>You may request additional privileges by selecting an account role from below. Please note that requesting privileges requires an administrator's
approval and in some cases, will require additional documentation to be uploaded.</p>
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

<br/><br/>

<div class="collapsableContent" id="privileges">	
	<ul class="checkboxgroup-vertical">
		
		<s:iterator var="role" value="roleList">
			<s:if test="(inAccountAdmin || #role.name != 'ROLE_ACCOUNT_REVIEWER')">
				<s:set var="disabled" value="false" />
				<s:set var="checked" value="false" />
				<s:set var="accountRole" value="null"/>
				
				<%-- Find current account role. --%>
				<s:iterator var="ar" value="currentAccount.accountRoleList">
					<s:if test="#accountRole == null && #ar.roleType.id == #role.id">
						<s:set var="accountRole" value="#ar"/>
					</s:if>
				</s:iterator>
				
				<s:if test="#accountRole != null && #accountRole.roleStatus.name != 'Inactive'">
					<s:set var="checked" value="true" />
					<s:set var="disabled" value="true" />
					<input type="hidden" name="accountPrivilegesForm.accountRoleList" value="${role.id}, ${accountRole.roleStatus.id}" />
					<input type="hidden" name="accountPrivilegesForm.accountRoleExpiration" value="${role.id}, ${accountRole.expirationString}" />
				</s:if>
				
				<s:if test="!((#role.name == 'ROLE_ORDER_ADMIN' || #role.name == 'ROLE_ACCOUNT_ADMIN') && instanceType.name == 'FITBIR')"> 
					<s:if test="!#role.isAdmin">    <!-- what is this for??? -->
						<li>
							<s:if test="disabled">
								<input type="checkbox" id="role_${role.id}" disabled checked />
							</s:if>
							<s:else>
								<input type="checkbox" id="role_${role.id}" name="accountPrivilegesForm.accountRoleList" value="${role.id}, 1" />
							</s:else>							
							<label for="role_${role.id}"><strong>${role.title}</strong>&nbsp;-&nbsp;${role.description}</label>
						</li>
					</s:if>
				</s:if>
			</s:if>
		</s:iterator>
	</ul>
	</div>

	<h3>Specific Permission Groups</h3>
	<p>Please mark the data groups for which you are requesting access. Your request will be submitted to the Data 
		Access Committee for review. Please note that requesting data access requires an administrator's approval and 
		in some cases, will require additional documentation to be uploaded.</p>
	
	<ul class="checkboxgroup-vertical">
		<s:if test="permissionGroupList.isEmpty()">
			<li><strong>No permission groups available</strong></li>
		</s:if>
	
		<s:iterator var="permissionGroup" value="permissionGroupList">
			<s:set var="disabled" value="false" />
			<s:set var="checked" value="false" />
			
			<s:iterator var="pgm" value="currentAccount.permissionGroupMemberList">
				<s:if test="#pgm.permissionGroup.id == #permissionGroup.id">
					<s:set var="disabled" value="true" />
					<s:set var="checked" value="true" />
				</s:if>
			</s:iterator>
			<li>
				<s:if test="disabled">
					<input type="checkbox" disabled checked id="permissionGroup${permissionGroup.id}"
							class="permissionGroup" name="accountPrivilegesForm.permissionGroupMemberList" value="${permissionGroup.id}" />
				</s:if>
				<s:else>
					<input type="checkbox" id="permissionGroup${permissionGroup.id}" class="permissionGroup"
							name="accountPrivilegesForm.permissionGroupMemberList" value="${permissionGroup.id}" />
				</s:else>

				<label class="permissionGroup" for="permissionGroup${permissionGroup.id}">
					<strong>${permissionGroup.groupName}</strong>&nbsp;-&nbsp;${permissionGroup.groupDescription}
				</label>
			</li>
		</s:iterator>
	</ul>
	
	<h3>Federated Sites</h3>
	<div>
		<p>Federated site access requires you to apply through an external site. Once access has been granted 
			from their site, your request will be approved here.</p>
	</div>
	<p class="disabled" style="margin-left:40px;">No federated sites available</p>
	
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
	
	
	$('document').ready(function() {
		
		if(accountType.includes("DSR")){
			if(hasProforms == "true"){
				$('#dataSubmitterWithProforms').not(this).prop('checked', true);
			}
			else{
				$('#dataSubmitterWithoutProforms').not(this).prop('checked', true);
			}
		}
		
		if(accountType.includes("DAR")){
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
		$('#privileges :checkbox').prop("disabled", false);
		$('#role_' + userRoleId).prop("disabled", true);	//account should always be selected and disabled
	}
	
	function unselectAllPrivileges() {
		$('#privileges :checkbox').prop('checked', false);
		$('#role_' + userRoleId).prop('checked', true);  	//account should always be selected and disabled
	}
	
	function disableAllPrivileges(){
		$('#privileges :checkbox').prop("disabled", true);
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
		disableAllPrivileges();
		$('#role_' + userRoleId).prop('checked', true);			// Account
    	$('#role_' + guidRoleId).prop('checked', true);			// GUID
    	$('#role_' + dictionaryRoleId).prop('checked', true);   // Data Dictionary
    	$('#role_' + repositoryRoleId).prop('checked', true);	// Data Repository
	    $('#role_' + proFormsRoleId).prop('checked', true);		// ProFoRMS
	    if ($('#dataAccessor').is(':checked')){
	    	$('#role_' + metaStudyId).prop('checked', true);    // Metastudy
	    	$('#role_' + queryRoleId).prop('checked', true);		// Query
	    }
		
	}
	
	function checkDSRWithoutProformsPrivileges(){
		disableAllPrivileges();
		$('#role_' + userRoleId).prop('checked', true);			// Account
    	$('#role_' + guidRoleId).prop('checked', true);			// GUID
    	$('#role_' + dictionaryRoleId).prop('checked', true);   // Data Dictionary
    	$('#role_' + repositoryRoleId).prop('checked', true);	// Data Repository
    	if ($('#dataAccessor').is(':checked')){
	    	$('#role_' + metaStudyId).prop('checked', true);    // Metastudy
	    	$('#role_' + queryRoleId).prop('checked', true);		// Query
	    }
		
	}
	
	function checkDARPrivileges(){
		disableAllPrivileges();
		$('#role_' + userRoleId).prop('checked', true);			// Account
    	$('#role_' + dictionaryRoleId).prop('checked', true);   // Data Dictionary
    	$('#role_' + repositoryRoleId).prop('checked', true);	// Data Repository
    	$('#role_' + queryRoleId).prop('checked', true);		// Query
    	$('#role_' + metaStudyId).prop('checked', true);		// Metastudy
	    $('#role_' + proFormsRoleId).prop('checked', false);		// ProFoRMS
		
	}
	
	function checkOtherPrivileges(){
		enableAllPrivileges();
		unselectAllPrivileges();
		$('#role_' + userRoleId).prop('checked', true);			// Account
	    $('#role_' + userRoleId).prop("disabled", true);
	}

</script>