<jsp:include page="/common/doctype.jsp" />
<%@ page import="gov.nih.nichd.ctdb.common.CtdbConstants"%>
<%@ page import="gov.nih.nichd.ctdb.security.domain.User"%>
<%@ page import="gov.nih.nichd.ctdb.security.common.UserResultControl"%>
<%@ page import="gov.nih.nichd.ctdb.common.Image"%>
<%@ page import="gov.nih.nichd.ctdb.common.rs"%>
<%@ page import="java.util.Locale" %>
<%@ taglib uri="/struts-tags" prefix="s" %>
<%@ taglib uri="/WEB-INF/display.tld" prefix="display" %>
<%@ taglib uri="/WEB-INF/security.tld" prefix="security" %>


<%-- CHECK PRIVILEGES --%>
<security:check privileges="manageUsers"/>

<%
    Locale l = request.getLocale();
    String userNameTitle = rs.getValue("response.collect.label.username",l);
    String firstNameTitle = rs.getValue("user.firstName",l);
    String lastNameTitle = rs.getValue("user.lastName",l);
    String emailTitle = rs.getValue("user.email",l);
    String sortColumn = UserResultControl.SORT_BY_USERNAME;
%>

<html>
<script>
	function clearSearch() {
		var oTable = $("#usersListTable").idtApi("getTableApi");
		var options = $("#usersListTable").idtApi('getOptions');
		
		$("#firstName").val("");
		$("#email").val("");
		$("#lastName").val("");
		$("#instituteId").prop("selectedIndex", 0);
		$('.searchContainer input[type="radio"]').prop("checked", "");
		$('.searchContainer input[type="radio"][value="all"]').prop("checked", "checked");
		
		$.extend(true, options.filterData, {
			firstName: $("#firstName").val(),
			email: $("#email").val(),
			lastName: $("#lastName").val(),
			instituteId: $("#instituteId").val(),
			staffSearch: $('.searchContainer input[name=staffSearch]:checked').val()			
		})
		oTable.ajax.reload();
		
	}
	
	function submitSearch() {
		var oTable = $("#usersListTable").idtApi("getTableApi");
		var options = $("#usersListTable").idtApi('getOptions');
		$.extend(true, options.filterData, {
			firstName: $("#firstName").val(),
			email: $("#email").val(),
			lastName: $("#lastName").val(),
			instituteId: $("#instituteId").val(),
			staffSearch: $('.searchContainer input[name=staffSearch]:checked').val()			
		})	
		oTable.ajax.reload();

	}	
</script>

<%-- Include Header --%>
<s:set var="pageTitle" scope="request">
	<s:text name="user.title.manageUserAccounts"/>
</s:set>
<jsp:include page="/common/header_struts2.jsp" />

<script type="text/javascript">

$(document).ready(function() {
	var searchSubmitted = "<s:property value='searchSubmitted' />";
	
	$("#usersListTable").idtTable({
		idtUrl: "<s:property value='#webRoot'/>/admin/getUsersList.action",
		filterData: {
			firstName: $("#firstName").val(),
			email: $("#email").val(),
			lastName: $("#lastName").val(),
			instituteId: $("#instituteId").val(),
			staffSearch: $('.searchContainer input[name=staffSearch]:checked').val()
		},
        columns: [
            {
                name: 'username',
                title: "<%= userNameTitle %>",
                parameter: 'username',
                data: 'username'
            },
            {
                name: 'firstName',
                title: "<%= firstNameTitle %>",
                parameter: 'firstName',
                data: 'firstName'
            },
            {
                name: 'lastName',
                title: "<%= lastNameTitle %>",
                parameter: 'lastName',
                data: 'lastName'
            },
            {
                name: 'email',
                title: "<%= emailTitle %>",
                parameter: 'email',
                data: 'email'
            },
            {
                name: 'classification',
                title: '<%=rs.getValue("report.systemSite",l)%>',
                parameter: 'classification',
                data: 'classification'
            }   
        ]
	});	
})
</script>

<div><s:text name="user.instruction.display"/></div>

<h3 class="toggleable collapsed"><s:text name="app.label.lcase.search"/></h3>

<div id="searchContainer" class="searchContainer">
	<s:form theme="simple" method="post" action="searchUser" id="searchForm" name="searchForm">
	<s:hidden name="sortBy"/>
	<s:hidden name="sortedBy"/>
	<s:hidden name="sortOrder"/>  
	
	<div class="formrow_2">
		<label for="firstName"><s:text name="user.firstName"/></label>
		<s:textfield id="firstName" name="firstName" size="20" maxlength="50" />
	</div>
	
	<div class="formrow_2">
		<label for="email"><s:text name="user.email" /></label>
		<s:textfield id="email" name="email" size="20" maxlength="50" />
	</div>
	
	<div class="formrow_2">
		<label for="lastName"><s:text name="user.lastName"/></label>
		<s:textfield id="lastName" name="lastName" size="20" maxlength="50" />
	</div>
	
	<security:hasPrivilege privilege="sysadmin">
	<div class="formrow_2">
		<label for="instituteId"><s:text name="user.institutes"/></label>
		<% if (((User)session.getAttribute(CtdbConstants.USER_SESSION_KEY)).isSysAdmin()) {  %>
			<s:select id="instituteId" list="#session.__userSearch_institutes" listKey="id" listValue="longName" name="instituteId" />
		<% } else { %>
			<s:hidden name="instituteId" id="instituteId" />
		<% } %>
	</div>
	</security:hasPrivilege>
	
	<div class="formrow_2">
		<label for="staffSearch"><s:text name="user.user" /></label>
		<s:radio name="staffSearch" list="#{'all':''}"/><s:text name="user.user.all"/>
		<s:radio name="staffSearch" list="#{'false':''}"/><s:text name="user.user.system"/>
		<s:radio name="staffSearch" list="#{'true':''}"/><s:text name="user.user.site"/>
	</div>
	
	<div class="formrow_1">
		<input type="button" value="<s:text name='button.Reset'/>" onclick="clearSearch()" title="Click to clear fields" />
		<input type="button" value="<s:text name='button.Search'/>"  onclick="submitSearch()" title="Click to search" />
	</div>
	
	</s:form>
</div>
<h3><s:text name="user.subtitle.display"/></h3>
<div id="usersListContainer" class="idtTableContainer brics">
	<p> <s:text name="user.subinstruction.display"/></p>
	<table id="usersListTable" class="table table-striped table-bordered" width="100%">
	</table>
</div>
<%-- Include Footer --%>
<jsp:include page="/common/footer_struts2.jsp" />
</html>
