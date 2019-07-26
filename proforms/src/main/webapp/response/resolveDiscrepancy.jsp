<jsp:include page="/common/doctype.jsp" />
<%@ page import="gov.nih.nichd.ctdb.response.common.ResponseConstants"%>
<%@ taglib uri="/struts-tags" prefix="s"%>
<%@ taglib uri="/WEB-INF/display.tld" prefix="display"%>
<%@ taglib uri="/WEB-INF/security.tld" prefix="security"%>

<%-- CHECK PRIVILEGES --%>
<security:check privileges="doublekeyresolution" />
<html>
<s:set var="pageTitle" scope="request">
	<s:text name="response.dataentry.resolution" />
</s:set>
<jsp:include page="/common/popUpHeader_struts2.jsp" />

<script type="text/javascript">

	function resolveDiscrepancy() {

		var allInput = document.getElementsByTagName("input");
		var flag = 0;

		for (var i = 0; i < allInput.length; i++) {
			if (allInput[i].type == 'text') {
				if (allInput[i].value == '') {
					flag = 1;
				}
			} else if (allInput[i].type == 'checkbox' || allInput[i].type == 'radio') {
				var tagName = allInput[i].name;
				var elmChk = eval("document.getElementsByName('" + tagName + "')");
				var flagChk = 0;
				
				for (var j = 0; j < elmChk.length; j++) {
					if (elmChk[j].checked) {
						flagChk = 1;
						break;
					}
				}
				if (flagChk == 0) {
					flag = 1;
					break;
				}
			}
		}

		var allTextArea = document.getElementsByTagName("textarea");
		for (var i = 0; i < allTextArea.length; i++) {
			if (allTextArea[i].value == '') {
				flag = 1;
				break;
			}
		}

		if (flag == 1) {
			alert('Please provide your comments before clicking Resolve button. ');
			return false;
		} 
		
		return true;
	}
	
	$(document).ready(function() {
		if ( $("#canCloseWindow").val() == "true" )
		{
			window.opener.location.reload(true);
			self.close();
		}
	});
	
</script>
<body>
	<!-- Add section for Action or IBIS Messaging messages -->
	<jsp:include page="/common/messages_struts2.jsp" />
	
	<s:form action="resolveDiscrepancy" id="dataDiscrepancyForm" method="post" onsubmit="return resolveDiscrepancy();">
		<s:hidden name="closePopUp" id="canCloseWindow" />
		
		<display:table name="responseList" scope="request" decorator="gov.nih.nichd.ctdb.response.tag.resolveDiscrepancyDecorator" >
			<display:setProperty name="basic.msg.empty_list" value="There are no responses for this form in the system at this time." />
			<display:column property="twoAnwsers" title="Discrepancies to Resolve" />
		</display:table>
		
		<div class="formbutton">
			<input type="button" value="<s:text name='button.Close'/>" id="bntCloseAudit" onClick="window.close()" title= "Click to close"/>
			
			<s:set var="viewOnly" value="%{#parameters.viewOnly[0]}" />
			<s:if test="%{#viewOnly == null || #viewOnly != 'yes'}">
				<s:submit id="resolve" value="Resolve" />
			</s:if>
		</div>
	</s:form>

</body>
</html>
