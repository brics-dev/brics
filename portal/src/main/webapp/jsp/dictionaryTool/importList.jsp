<%@include file="/common/taglibs.jsp"%>

<s:form id="importDataElementForm" action="importDataElementAction!adminSaveDataElements.action" method="post">
<s:token />
	<table class="display-data full-width">
		<thead>
			<tr>
				<th style="width: 3%; text-align: center">
					<input id="checkAllBox" type="checkbox" onclick="checkAll()"/>
				</th>
				<th style="width: 40%">Title</th>
				<th style="width: 37%">Variable Name</th>
			</tr>
		</thead>
		<tbody>
			<s:iterator var="dataElement" value="newDataElementList" status="status">
			<s:if test="importedDataElementMap[#dataElement]"> 
<!-- 				<tr style="background-color:#FF3030"> -->
				<td class="center"  style= "color:#FF0000 !important;"><input class="importCheckbox" type="checkbox" id="<s:property value="#status.index"/>" <s:if test="#dataElement.category==null" >disabled</s:if> /></td>
				<s:if test="#dataElement.category==null"  >
					<td style= "color:#FF0000 !important;"><s:property escapeHtml="false" value="#dataElement.title" /></td>
				</s:if>
				<s:else>
				<td ><a style= "color:#FF0000 !important;" class="lightbox" target="_blank" href="importDataElementAction!viewDetails.ajax?dataElementName=<s:property value="#dataElement.name" />"><s:property escapeHtml="false" value="#dataElement.title" /></a></td>
				</s:else>
				<td style= "color:#FF0000 !important;"><s:property escapeHtml="false" value="#dataElement.name" /></td>
				</tr>
			</s:if>
			<s:else>
				<tr>
				<td class="center"><input class="importCheckbox" type="checkbox" id="<s:property value="#status.index"/>" <s:if test="#dataElement.category==null" >disabled</s:if> /></td>
				<s:if test="#dataElement.category==null"  >
					<td><s:property escapeHtml="false" value="#dataElement.title" /></td>
				</s:if>
				<s:else>
				<td><a class="lightbox" target="_blank" href="importDataElementAction!viewDetails.ajax?dataElementName=<s:property value="#dataElement.name" />"><s:property escapeHtml="false" value="#dataElement.title" /></a></td>
				</s:else>
				<td><s:property escapeHtml="false" value="#dataElement.name" /></td>
				</tr>
			</s:else>
			</s:iterator>
		</tbody>
	</table>

	<s:hidden id="checkedList" name="checkedList" value="" escapeHtml="true" escapeJavaScript="true" />
	<s:hidden id="isExistingDataElementChecked" name="isExistingDataElementChecked"/>

	<div class="form-field">
		<div class="button">
			<input id="importSelectedDEs" type="button" value="Import Selected Data Elements" onClick="javascript: submitList();" />
		</div>
	</div>
</s:form>
<div id="loadingDiv" style="display: none; background: url(/portal/images/loading_icon.gif) no-repeat center center;">
	<p style="text-align: center; margin-bottom: 10px;">Processing.....</p>
</div>
<script type="text/javascript">

	$(document).ready(function() {
		
		$("#loadingDiv").dialog({
			autoOpen: false,
			height: 180,
			width: 270,
			dialogClass: 'noclose',
			closeOnEscape: false,
			position: { my: "center", at: "center", of: window },
			open: function() {
			    $("#importSelectedDEs").prop("disabled", true);
			    $("#importSelectedDEs").css('color', 'grey');
				$("#content-wrapper").css('opacity', '0.5');
			},
			close: function() {
				$("#content-wrapper").css('opacity', '');
			    $("#importSelectedDEs").prop("disabled", false);
			    $("#importSelectedDEs").css('color', 'white');
			}
		});
		$(".ui-dialog").css({'position': 'fixed'}); //dialog moves with scroll
		$(".ui-dialog-titlebar").hide(); //hide the title bar in dialog

	 });
	
	function buildSubmissionList() {
		var myString = "";
		$("input[type=checkbox]").each(function (index) {
			var id = $(this).attr('id');
			// We don't care if the checkAllBox is checked.
			if(this.checked && id != "checkAllBox")
			{
				// Subtract by 1 so when the list is parsed by java it is 0 indexed
				myString = myString + (index - 1 ) + ",";
			}
		});
		$("input[name=checkedList]").attr("value", myString);
		
	}
	
	function submitList() {
		buildSubmissionList();
		
		checkExistingDataElements();	
	}
	
	function checkExistingDataElements(){
		
		var theForm = document.forms['uploadForm'];
		theForm.action='importDataElementAction!adminSaveDataElements.action';
		var checkedList = $("input[name=checkedList]").val();
		
		$.ajax("importDataElementAction!checkExistingDataElements.ajax", {
			"type" : "POST",
			"data" : {
				"checkedList" : checkedList
			},
			"beforeSend": function(){
				$("#loadingDiv").dialog("open");
			},
			"success" : function(data) {
				var isExistingDataElementChecked = $(data).find("#isExistingDataElementChecked").val();
				console.log("isExistingDataElementChecked: "+isExistingDataElementChecked);
				if(isExistingDataElementChecked=='true'){
                    if(validateRequiredFields()){
                    	submitAdminSaveDataElements(checkedList);
                    } else {
                    	$("#loadingDiv").dialog("close");	
                    }
				}
				else{
					submitAdminSaveDataElements(checkedList);
				}
			}
		});
	}
	
	function submitAdminSaveDataElements(checkedList){
		$.ajax({
			"url"  : "importDataElementAction!adminSaveDataElements.action",
			"type" : "POST",
			"data" : {
				"checkedList" : checkedList
			},
			"success" : function(data) {
				window.location.href = "searchDataElementAction!list.action";
				setTimeout(function () {
					$("#loadingDiv").dialog("close");
				}, 3500);
			}
		})
	}
	
	function validateRequiredFields(){
		
		var valid = true;		
		var auditNote = $("#auditNote").val();
		
		if (!auditNote) {
			$("#validateAuditNote").show();
			valid = false;
		}

		return valid;
	}
	
	function checkAll(checkbox) 
	{
		if($("#checkAllBox").is(":checked"))
		{
			$(".importCheckbox").each(function() {
				if(!$(this).is(":checked")) 
				{
					$(this).click();
				}
			});
		}
		else 
		{
			$(".importCheckbox").each(function() {
				if($(this).is(":checked")) 
				{
					$(this).click();
				}
			});
		}
	}
	
	
</script>