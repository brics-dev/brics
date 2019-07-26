<%@include file="/common/taglibs.jsp"%>

<s:set var="sessionDataStructure" value="sessionDataStructure" />
<s:set var="user" value="user" />

<s:if test="%{sessionDataStructure != null && sessionDataStructure.dataStructure != null}">

	<div id="sessionDataStructureDiv">
		<label class="label">Title</label>
		<s:property value="sessionDataStructure.dataStructure.title" />
		<label class="label">Short Name</label>
		<s:property value="sessionDataStructure.dataStructure.shortName" />
		<br /> <label class="label">Created By</label>
		<s:property value="user.lastName" />
		,
		<s:property value="user.firstName" />

		<ndar:actionLink value="Edit Data Structure" action="dataStructureAction!edit.action" />
		<ndar:actionLink value="Remove" action="baseDictionaryAction!removeSessionDataStructure.action" />
		<a href="javascript: addDataElements();">Add Selected Data Elements</a>
	</div>


</s:if>

<script type="text/javascript">

function addDataElements() {
	var dataElementCheckbox = document.getElementsByName("dataElementCheckbox");
	
	first = true;
	addValuesJson = "";
	for (var i = 0; i < dataElementCheckbox.length; i++)
	{
		var checkBox = dataElementCheckbox[i];
		if (checkBox.checked)
			{
				if (first == false) 
				{
					addValuesJson += ",";
				}
				else
				{
					first = false;
				}
				
				addValuesJson += checkBox.value;
			}
	}

	$.post( "dataStructureAction!addDataElements.ajax",
			{ "dataElementIds": addValuesJson },
			function (data)
				{
					alert("Data Elements were added.");
// 					document.getElementById("lowerFilterId").innerHTML = data;
				}
			);
}

</script>