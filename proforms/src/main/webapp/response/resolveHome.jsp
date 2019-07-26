<jsp:include page="/common/doctype.jsp" />
<%@ page import="java.util.HashMap,java.util.Iterator,
                 java.util.List,
                 java.util.NoSuchElementException,
                 gov.nih.nichd.ctdb.patient.domain.PatientCategory,
                 gov.nih.nichd.ctdb.form.domain.Form"%>
<%@ taglib uri="/struts-tags" prefix="s"%>
<%@ taglib uri="/WEB-INF/security.tld" prefix="security" %>
<%@ taglib uri="/WEB-INF/display.tld" prefix="display" %>

<%-- CHECK PRIVILEGES --%>
<security:check privileges="doublekeyresolution,dataentry,dataentryoversight,externaldataimport" />

<html>
<s:set var="pageTitle" scope="request">
	<s:text name="response.resolveHome.title"/>
</s:set>

<jsp:include page="/common/header_struts2.jsp" />

<s:set var="disallowingPII" value="#systemPreferences.get('guid_with_non_pii')" />

<script type="text/javascript">

function resolveDies() {
	var elms=document.getElementsByName('administeredformid');
	var adminFormid=null;
	for (var i=0;i<elms.length;i++) {
		if (elms[i].checked) {
			adminFormid=elms[i].value;
		}
	}	

	var url = "viewResolvedForm.action?id=" + adminFormid;
	openPopup(url,"resolve", "width=800,height=300,toolbar=0,location=0,directories=0,,menubar=0,status=1,scrollbars=1,resizable=1");
}

function launchActions(caller) {
	var count = 0;	
	var elms=document.getElementsByName('resolveAdminId');
	var adminFormid=null;
	for (var i=0;i<elms.length;i++) {
		if (elms[i].checked) {
			adminFormid=elms[i].value;
			count++;
		}
	}	
	if (count == 0) {
		 alert("Warning: No form have been selected yet.");
		 return false;
	} else {
		switch (caller.id) {

			case "open":	
				url ="viewResolvedForm.action?id=" + adminFormid + "&viewOnly=yes";
				openPopup(url, "open_dis", "width=800,height=300,toolbar=0,location=0,directories=0,,menubar=0,status=1,scrollbars=1,resizable=1");
				break;			
			
			case "viewAudit":	
				url ="viewEditedAnswer.action?id=" + adminFormid;
				openPopup(url, "view_Audit", "width=800,height=300,toolbar=0,location=0,directories=0,,menubar=0,status=1,scrollbars=1,resizable=1");
				break;					
		}		
	}
}
</script>

<s:form method="post">
	<div><s:text name="response.resolveHome.instruction"/></div><br/>
	
	<div class="dataTableContainer" id="table">
                    
	<ul>
		<li>
			<input type="button" id="resolve" onclick="resolveDies()" value="<s:text name='response.resolveHome.resolveDiscrepancy' />" 
				title ="Click to see discrepancy details"/>
		</li>
	</ul>
	
	<table class="table" width="100%" border="0" cellspacing="1" cellpadding="4">
		<tr class="tableRowHeader">
		
			<th align="center" class="tableCellHeader"></th>
			<s:if test="#disallowingPII == 1">
				<th align="center" class="tableCellHeader"><s:text name="response.resolveHome.tableHeader.subjectGUID"/></th>
			</s:if>
			<s:else>
				<th align="center" class="tableCellHeader"><s:text name="response.resolveHome.tableHeader.subjectName"/></th>
			</s:else>
			
			<th align="center" class="tableCellHeader"><s:text name="response.resolveHome.tableHeader.interval"/></th>
			<th align="center" class="tableCellHeader"><s:text name="response.resolveHome.tableHeader.formName"/></th>
			<th align="center" class="tableCellHeader"><s:text name="response.resolveHome.tableHeader.subjectID"/></th>
			<th align="center" class="tableCellHeader"><s:text name="response.resolveHome.tableHeader.questionName"/></th>
		</tr>
			
		<s:iterator value="#request.formslist">
		<tr>
			<td><input type="checkbox" name="administeredformid" value="<s:property value='administeredformid'/>" /></td>
			<s:if test="#disallowingPII == 1">
				<td><s:property value="guId"/></td>
			</s:if>
			<s:else>
				<td><s:property value="subjectName"/></td>
			</s:else>
			
			<td><s:property value="interval"/></td>
			<td>
				<a href="Javascript:popupWindow('../form/viewFormDetail.action?source=popup&id=<s:property value="formId"/>');">
					<s:property value="formName"/>
				</a>
			</td>
			<td><s:property value="nihRecordNumber"/></td>
			<td>
				<s:iterator value="questionList" status="questionListStatus">
					<s:if test="#questionListStatus.first != true">,</s:if>
					<s:property value="name"/>
				</s:iterator>
			</td>
		</tr>
		</s:iterator>
		
	</table>
	</div>
	
	<h3><s:text name="response.resolveHome.subtitle"/></h3>
	<div><s:text name="response.resolveHome.subinstruction"/></div>
	<div class="dataTableContainer" id="resolvedTable">
		<ul>
			<li>
	     		<input type="button" name="btnAction" id="open" onclick="launchActions(this)" value="<s:text name='response.resolveHome.open' />" title="Click to see the selected discrepancy details"/>
	     	</li>
	     	<li>
	     		<input type="button" name="btnAction" id="viewAudit" onclick="launchActions(this)" value="<s:text name='button.ViewAudit' />" title="Click to view changes made to collections"/>
	     	</li>    
     	</ul>
     			
		<table class="table" width="100%" border="0" cellspacing="1" cellpadding="4">
			<tr class="tableRowHeader">
				<th align="center" class="tableCellHeader"></th>
				<s:if test="#disallowingPII == 1">
					<th align="center" class="tableCellHeader"><s:text name="response.resolveHome.tableHeader.subjectGUID"/></th>
				</s:if>
				<s:else>
					<th align="center" class="tableCellHeader"><s:text name="response.resolveHome.tableHeader.subjectName"/></th>
				</s:else>

				<th align="center" class="tableCellHeader"><s:text name="response.resolveHome.tableHeader.interval"/></th>
				<th align="center" class="tableCellHeader"><s:text name="response.resolveHome.tableHeader.formName"/></th>
				<th align="center" class="tableCellHeader"><s:text name="response.resolveHome.tableHeader.subjectID"/></th>
				<th align="center" class="tableCellHeader"><s:text name="response.resolveHome.tableHeader.resolvedDate"/></th>
				<th align="center" class="tableCellHeader"><s:text name="response.resolveHome.tableHeader.resolvedBy"/></th>			
			</tr>
			
			<s:iterator value="#request.resolvedlist">
			<tr>
				<td><input type="checkbox"  name="resolveAdminId" value="<s:property value='administeredformid'/>" /></td>
				
				<s:if test="#disallowingPII == 1">
					<td><s:property value="guId"/></td>
				</s:if>
				<s:else>
					<td><s:property value="subjectName"/></td>
				</s:else>
				
				<td><s:property value="interval"/></td>
				<td align=center>
					<a href="Javascript:popupWindow ('../form/viewFormDetail.action?source=popup&id=<s:property value="formId"/>');">
						<s:property value="formName"/>
					</a>
				</td>
				<td><s:property value="nihRecordNumber"/></td>
				<td nowrap><s:property value="resolvedDate"/></td>
				<td><s:property value="resolveUserName"/></td>		
			</tr>
			</s:iterator>
		</table>
	</div>		

</s:form>
</html>

<%-- Include Footer --%>
<jsp:include page="/common/footer_struts2.jsp" />
