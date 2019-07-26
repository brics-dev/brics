<%@ include file="/common/taglibs.jsp"%>

<s:set var="exceptionMessages" value="exceptionMessages" />

<script type="text/javascript">
	function checkBoxClick(){
		if(document.getElementById("filterCheckBox").checked)
			document.getElementById("filterCheck").value = "true";
		else
			document.getElementById("filterCheck").value = "false";
	}
</script>

<table class="mainPane">
	<div class="separator"></div>
	<label for="bigTable">Sorted from most recent to least recent:</label>
	<table cellpadding="10" id="bigTable">
		<tr class="headerBackground">
			<th><b>ID</b></th>
			<th><b>User</b></th>
			<th><b>Date</b></th>
			<th><b>Action</b></th>
			<th><b>Action Name (jsp page)</b></th>
			<th><b>Type</b></th>
			<th><b>Stack Trace</b></th>
		</tr>
		<s:iterator var="exception" value="exceptionMessages">
			<tr valign="top">
				<td><s:property value="id" /></td>
				<td><s:property value="userId.fullName" /></td>
				<td><s:property value="#exception.getTimestamp().toString()" /></td>
				<td><s:property value="exceptionAction" /></td>
				<td><s:property value="actionName" /></td>
				<td><s:property value="exceptionType" /></td>
				<td><s:property value="exceptionTrace" /></td>
			</tr>
		</s:iterator>
	</table>
	</td>

	<td class="rightNavBar"><label for="navBox"><b>Filters:</b></label>
		<div id="navBox">
			<s:form action="exceptionAction" method="post">
			<s:token />
				<s:hidden name="filterCheck" id="filterCheck" value="false" escapeHtml="true" escapeJavaScript="true" />
				
				<input type="checkbox" name="ignore" onclick="checkBoxClick()" id="filterCheckBox" value="1" />
				<label for="filterCheckBox">Filter stack trace by gov.nih.tbi packages</label>
				<br />
				<br />
				<label for="dateInput1">Date 1 (mm-dd-yy)(inclusive)</label>
				<input type="text" name="dateInput1" id="dateInput1" value="<s:property value="getTime()" />" />
				<label for="dateInput2">Date 2 (mm-dd-yy)(inclusive)</label>
				<input type="text" name="dateInput2" id="dateInput2" value="<s:property value="getTime()" />" />
				<s:submit align="left" method="list" />
			</s:form>
		</div></td>
</table>









