
<div>
	<p>Individual access to the meta study is granted below. Select the intended individual from the drop down to grant
				access. The permissions include read, write, or admin. The default permission is read. Also, individuals can be
				removed from the permission group here.</p>

			<p>
				<strong>Read - </strong>Allows user to view this meta study.<br /> <strong>Write - </strong>Allows user to view this
				meta study, edit study details, manage documentation, manage data, and specify keywords and labels.<br /> <strong>Admin / Owner - </strong>Allows
				user to view this meta study, edit meta study details, manage documentation, manage data, specify keywords and labels, and grant permissions. There can
				only be one Owner.
			</p>
			
			<div id="permissionDivId"></div>
</div>

<script type="text/javascript">
$('document').ready(function(){ 
			getPermissions();
	}
);

function getPermissions(){
	$.ajax({
		type: "GET",
		cache : false,
		url : "eformPermissionAction!load.ajax",
		success : function (data) {
			$("#permissionDivId").html(data);
		}
	});
} 

 function addAuthority(){
		// Get value from selected option
		var value = $("#authoritiesDivId select").val();
		
		var type = value.substring(0, value.indexOf(':'));
		var id = value.substring(value.indexOf(':') + 1);
		
		changePermission(type, id, 'READ', 'READ');
	}

	function changePermission(type, id, permission, oldPermission){
		// prevent user from removing their own access.
		var myAccountId = "<s:property value='account.id' />";
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
				url : "eformPermissionAction!changePermission.ajax",
				data : params,
				success : function (data) {
					$("#innerDivId").html(data);
				}
			});
		}
		else
		{
			$.ajax({
				type: "GET",
				cache : false,
				url : "eformPermissionAction!load.ajax",
				success : function (data) {
					$("#permissionDivId").html(data);
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











