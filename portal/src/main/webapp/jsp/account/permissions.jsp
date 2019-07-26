<%@include file="/common/taglibs.jsp"%>

<div id="innerDivId">
	<jsp:include page="permissions-inner.jsp" />
</div>

<script type="text/javascript">
	function addAuthority()
	{
		// Get value from selected option
		var value = $("#authoritiesDivId select").val();
		
		var type = value.substring(0, value.indexOf(':'));
		var id = value.substring(value.indexOf(':') + 1);
		
		changePermission(type, id, 'READ', 'READ');
	}

	function changePermission(type, id, permission, oldPermission)
	{
		// prevent user from removing their own access.
		var myAccountId = <s:property value="account.id" />
		if(type == "ACCOUNT" && id.split(";")[1] == myAccountId && permission == "NONE")
		{
			alert('If you would like to revoke your own permissions to this entity, please contact support.');
			return;
		}
		
		var update = true;
		if (oldPermission == "OWNER")
		{
			update = false;
			alert("This entity must have exactly one owner. Please add a new owner before you remove this one.");
		}
		if (permission == "OWNER")
		{
			if (type == "ACCOUNT")
			{
				update = confirm("There can only be one Owner.  Are you sure you want to change the owner?");
			}
			else
			{
				update = false;
				alert("Permission Groups can not be assigned the role of 'OWNER'");
			}
			
		}
		if (update == true)
		{
			var params = {selectedAuthorityType:type, selectedAuthorityId:id, selectedPermissionName:permission };
			$.ajax({
				cache : false,
				url : "<s:property value='actionName' />!changePermission.ajax",
				data : params,
				success : function (data) {
					$("#innerDivId").html(data);
				}
			});
		}
		else
		{
			$.ajax({
				cache : false,
				url : "<s:property value='actionName' />!load.ajax",
				success : function (data) {
					console.log("updating the div");
					$("#innerDivId").html(data);
				}
			});
		}
	}
	   function clickAndDisable(link) {
		     // disable subsequent clicks
		     link.onclick = function(event) {
		        event.preventDefault();
		     }
		   }   
</script>