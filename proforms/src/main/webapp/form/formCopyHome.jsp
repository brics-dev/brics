<jsp:include page="/common/doctype.jsp" />
<%@ page import="gov.nih.nichd.ctdb.form.common.FormResultControl, gov.nih.nichd.ctdb.common.Image"%>
<%@ page import="gov.nih.nichd.ctdb.common.rs,java.util.Locale" %>
<%@ taglib uri="/struts-tags" prefix="s"%>
<%@ taglib uri="/WEB-INF/security.tld" prefix="security" %>
<%@ taglib uri="/WEB-INF/display.tld" prefix="display" %>

<%-- CHECK PRIVILEGES --%>
<security:check privileges="viewforms"/>

<html>

<s:set var="pageTitle" scope="request">
	<s:text name="form.forms.copyExistingForms.title"/>
</s:set>

<jsp:include page="/common/header_struts2.jsp" />

<script type="text/javascript">
	var searchSubmitted = "<s:property value='searchSubmitted' />";

function swichSubjectType(type) {
	document.copyExistForm.searchFormType.value = type;
	document.copyExistForm.submit();	
}

function swichExistType(type) {
	document.copyExistForm.existType.value = type;
	document.copyExistForm.submit();
}

function search() {
	document.copyExistForm.searchSubmitted.value = 'YES';
	document.copyExistForm.submit();
}

function performViewForm() {
	var publicContent = document.getElementById("publicFormsContent");
	var elms = publicContent.getElementsByTagName("*");
	var checked = false;
	for (var i=0; i<elms.length; i++) {
		var elm = elms[i];
		if(elm.type == "checkbox") {
			if(elm.checked == 1) {
				checked = true;
				var WindowArgs = "toolbar=no," + "location=no," + "directories=no," + "status=yes,";
				var url = "<s:property value="#webRoot"/>/form/viewFormDetail.action?source=popup&id=" + elm.id;
				openPopup(url, "", WindowArgs+"top=10,left=10,scrollbars=yes,resizable=yes,width=600,height=400");
				break;
			}
		}
	}
	
	if (checked == false) {
		window.alert("You must select a form to perform this action");
	}
}

function performCopy() {
	var publicContent = document.getElementById("publicFormsContent");
	var elms = publicContent.getElementsByTagName("*");
	
	var checked = false;
	for (var i=0;i<elms.length;i++) {
		var elm = elms[i];
		if (elm.type == "checkbox") {
			if (elm.checked == 1) {
				checked = true;
				var url = "<s:property value="#webRoot"/>/form/showCopyForm.action?id=" + elm.id;
			    redirectWithReferrer(url);
				break;
			}
		}
	}
	
	if (checked == false) {
		window.alert("You must select a form to perform this action");
	}
}
</script>

<% Locale l=request.getLocale(); %>

<s:form theme="simple" action="formCopyHome" id="copyExistForm" method="post">
	<div><s:text name="form.copyformHome.instruction"/></div>
	<h3 class="toggleable collapsed"><s:text name="form.forms.manageFormsDisplay.advancedSearchForms.text"/></h3>
	<div id="searchPublicFormsDiv" class="searchContainer">
		<s:hidden name="clicked" value="submit"/>
		<s:hidden name="sortBy"/>
	    <s:hidden name="sortedBy"/>
		<s:hidden name="sortOrder"/>
		<s:hidden name="existType" />
		
		<div class="formrow_2">
			<label for="name"><s:text name="form.name.display"/></label>
			<s:textfield name="name" size="20" maxlength="50"/>
		</div>
		
		<div class="formrow_2">
			<label for="updatedDate"><s:text name="app.label.lcase.lastupdated"/></label>
			<s:textfield name="updatedDate" cssClass="dateField" id="calId2" size="20"  maxlength="50"/>
		</div>
	             
		<div class="formrow_2">
			<label for="status"><s:text name="form.status.display"/></label>
			<s:select name="status" list="#{'0':'All', '3':'Active', '4':getText('app.status.inprogress'), '1':'Inactive'}" />
		</div>
		
		<div class="formrow_2">
			<label for="protocolName"><s:text name="protocol.add.name.display"/></label>
			<s:textfield name="protocolName" size="20" maxlength="50"/>
		</div>
	             
		<div class="formrow_1">
			<input type="Reset" value="<s:text name='button.Reset'/>" title="Click to clear fields" alt="Reset" 
				onclick="redirectWithReferrer('formCopyHome.action?existType=Mine')"/>
			<input type="button"   value="<s:text name='button.Search'/>" title="Click to search" alt="Search" onclick="search()"/> 
		</div>
	</div>

	<h3><s:text name="form.forms.copyExistingForms.title"/></h3>
	<p><s:text name="form.forms.manageForms.myFormsDisplay.text"/></p>

	<table width="100%">
		<tr>
			<td>
			<s:if test="%{existType != 'Mine'}">
				<a href="javascript:swichExistType('Mine')" class="underline">Mine</a>
			</s:if>
			<s:else>
				Mine
			</s:else>
			&nbsp;|
			
			<s:if test="%{existType != 'Public'}">
				<a href="javascript:swichExistType('Public')" class="underline">Public</a>
			</s:if>
			<s:else>
				Public
			</s:else>
			&nbsp;|
	
			<s:if test="%{existType != 'All'}">
				<a href="javascript:swichExistType('All')" class="underline">All</a>
			</s:if>
			<s:else>
				All
			</s:else>
			</td>
			
			<td align="right">
        		<s:select name="searchFormType" id="formType" onchange="swichSubjectType(this.value);" 
        			list="#{'0':getText('form.forms.subjectForms'), '1':getText('form.forms.nonsubjectForms'), '2':getText('form.forms.allForms')}" />
			</td>
		</tr>
	</table>

	<br>
	<div class="dataTableContainer" id="publicFormsContent" >
		<ul>
			<li> 
				<security:hasProtocolPrivilege privilege="addeditforms">
					<input type="button" id= "btnViewPu" onclick="performViewForm()" title="Click to view" value="<s:text name='button.View'/>"/>
				</security:hasProtocolPrivilege>
			</li>
			<li> 
				<security:hasProtocolPrivilege privilege="addeditforms">
					<input type="button" id= "btnCopyPu" onclick="performCopy()" value="<s:text name='button.copyToCurrentStudy'/>"/>
				</security:hasProtocolPrivilege>
			</li>
		</ul>

	 	<display:table  name="formsList"  scope="request" decorator="gov.nih.nichd.ctdb.form.tag.PublicFormSearchDecorator">
    		<display:setProperty name="basic.msg.empty_list" value="There are no Public Forms found at this time."/>
    		<display:column property="formIdCheckbox" title="" />
        	<display:column property="protocolNumber" title='<%=rs.getValue("study.add.number.display",l)%>' align="left"/>
        	<display:column property="name" title='<%=rs.getValue("form.name.display",l)%>' align="left"/>
			<display:column property="numQuestions" title='<%=rs.getValue("response.collect.label.numberofquestions",l)%>' align="center" />
        	<display:column property="updateddatetime" title='<%=rs.getValue("app.label.lcase.lastupdated",l)%>' align="center" />
    	</display:table>
	</div>
</s:form>

<jsp:include page="/common/footer_struts2.jsp" />

</html>

