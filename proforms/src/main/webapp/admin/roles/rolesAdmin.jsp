<jsp:include page="/common/doctype.jsp" />

<%@ page import="gov.nih.nichd.ctdb.common.rs, java.util.Locale" %>
<%@ taglib prefix="s" uri="/struts-tags" %>
<%@ taglib uri="/WEB-INF/display.tld" prefix="display" %>
<%@ taglib uri="/WEB-INF/security.tld" prefix="security" %>


<%-- CHECK PRIVILEGES --%>
<security:check privileges="sysadmin"/>

<jsp:useBean id="user" scope="session" class="gov.nih.nichd.ctdb.security.domain.User"/>
<html>

<s:set var="pageTitle" scope="request">
	<s:text name="role.title.display"/>
</s:set>

<jsp:include page="/common/header_struts2.jsp" />
<script type="text/javascript">
$(document).ready(function() {
	var basePath = "<s:property value="#systemPreferences.get('app.webroot')"/>";
	$("#systemRolesTable").idtTable({
		idtUrl: "<s:property value='#webRoot'/>/admin/getSystemRolesList.action",
		idtData: {
			primaryKey: 'id'
		},
		dom: 'Bfrtip',
		select: "multi",
        columns: [
            {
                name: 'name',
                title: '<%=rs.getValue("report.systemRole", request.getLocale())%>',
                parameter: 'name',
                data: 'name'
            },
            {
                name: 'description',
                title: '<%=rs.getValue("role.description.display", request.getLocale())%>',
                parameter: 'description',
                data: 'description'
            }
        ],
        buttons: [
	      	  {
       		 	 text: "<s:text name='button.AddRole'/>",
       		 	 className: 'idt-addRoleButton',
        		 enabled: true,
        		 enableControl: {
                        count: 0,
                        invert: false
                 },
	  	    	 action: function(e, dt, node, config) {
	  	    		redirectWithReferrer(basePath + "/admin/addRole.action");
	      	   	} 
	      	  },
        	  {
	      		 text: "<s:text name='button.EditRole'/>",
	        	 className: 'idt-DeleteButton',
        		 enabled: false,
        		 enableControl: {
                        count: 1,
                        invert: true
                 },	        	 
     	    	 action: function(e, dt, node, config) {
					var selectedId = $("#systemRolesTable").idtApi('getSelected');
	        	   	redirectWithReferrer(basePath + "/admin/editRole.action?id=" + selectedId);
	        	   		
      	   		
	        	 } 
	       }
      	]
	});	
})
</script>
<div>
	<s:text name="roles.instruction.display"/>
</div>

<%-- Presentation Logic Only Below--%>
<h3><s:text name="roles.subtitle.display"/></h3>
<div id="systemRolesContainer" class="idtTableContainer brics">
	<p><s:text name="roles.subinstruction.display"/></p>
	<table id="systemRolesTable" class="table table-striped table-bordered" width="100%">
	</table>
</div>

<%-- Include Footer --%>
<jsp:include page="/common/footer_struts2.jsp" />
</html>