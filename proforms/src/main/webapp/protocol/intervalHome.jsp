<jsp:include page="/common/doctype.jsp" />
<%@ page import="java.util.Locale" %>
<%@ page import="gov.nih.nichd.ctdb.common.StrutsConstants" %>
<%@ page import="gov.nih.nichd.ctdb.common.CtdbConstants"%>
<%@ page import="gov.nih.nichd.ctdb.security.domain.Privilege" %>
<%@ page import="gov.nih.nichd.ctdb.protocol.domain.Protocol" %>
<%@ page import="gov.nih.nichd.ctdb.security.domain.User"%>
<%@ taglib uri="/struts-tags" prefix="s" %>
<%@ taglib uri="/WEB-INF/datatables.tld" prefix="idt" %>
<%@ taglib uri="/WEB-INF/security.tld" prefix="security" %>

<security:check privileges="viewvisittypes"/>

<html>

<% Locale l = request.getLocale(); 
   User user = (User) session.getAttribute(CtdbConstants.USER_SESSION_KEY);
   Privilege editDeletePriv = new Privilege("addeditvisittypes") ;
   Privilege viewAuditPriv = new Privilege("viewaudittrails") ;
   Protocol protocol = (Protocol) session.getAttribute(CtdbConstants.CURRENT_PROTOCOL_SESSION_KEY);
   int protocalId = protocol.getId();
%>

<jsp:include page="/common/header_struts2.jsp" />

<script type="text/javascript">
	var webRoot = "<s:property value='#webRoot'/>";
	
	$(document).ready(function() {
		
		$("#visitTypeDisplayTable").idtTable({
			idtUrl: webRoot+"/protocol/getVisitTypeList.action",
			idtData: {
				primaryKey: 'id'
			},
			dom: 'Bfrtip',
			select: "multi",
	        columns: [
	            {
	                name: 'name',
	                title: 'Visit Type Name',
	                parameter: 'name',
	                data: 'name'
	            },
	            {
	                name: 'intervalTypeName',
	                title: 'Type',
	                parameter: 'intervalTypeName',
	                data: 'intervalTypeName'
	            },
	            {
	                name: 'category',
	                title: 'Category',
	                parameter: 'category',
	                data: 'category'
	            },	      
	            {
	                name: 'description',
	                title: 'Description',
	                parameter: 'description',
	                data: 'description'
	            },
	            {
	                name: 'intervalFormNames',
	                title: 'eForms Included',
	                parameter: 'intervalFormNames',
	                data: 'intervalFormNames',
	                width: '50%'
	            }
	        ],
	        buttons: [
        	<%
        		if (user.hasPrivilege(editDeletePriv, protocalId)) {
        	%>
		      	  {
	       		 	text: 'Edit',
	       		 	className: 'idt-EditButton',
	       		 	enabled: false,
	       		 	enableControl: {
	                       count: 1,
	                       invert: true
	                 },
		  	    	 action: function(e, dt, node, config) {
		  	    		var url = webRoot + "/protocol/editVisitType.action?id=";
		  	    		var selectedRow = $("#visitTypeDisplayTable").idtApi("getSelectedOptions")
		  	    		var selectedVisitTypeId = selectedRow[0];
		  	    		
		  	    		if (selectedRow.length != 0) {			  	  			
		  	  				redirectWithReferrer(url + selectedVisitTypeId);
		  	    		}
		      	   	} 
			      },
			<%
    			}
    		%>
        	<%
    			if (user.hasPrivilege(viewAuditPriv, protocalId)) {
    		%>
		      	  {
	       		 	text: 'View Audit',
	       		 	className: 'idt-viewAuditButton',
	       		 	enabled: false,
	       		 	enableControl: {
	                       count: 1,
	                       invert: true
	                 },
		  	    	 action: function(e, dt, node, config) {
		  	    		var url = webRoot + "/protocol/intervalAudit.action?id=";
		  	    		var selectedRow = $("#visitTypeDisplayTable").idtApi("getSelectedOptions")
		  	    		var selectedVisitTypeId = selectedRow[0];
		  	    		
		  	    		if (selectedRow.length != 0) {			  	  			
		  	  				Javascript:popupWindow(url + selectedVisitTypeId);
		  	    		}
		      	   	} 
		      	  },
			<%
    			}
    		%>
        	<%
				if (user.hasPrivilege(editDeletePriv, protocalId)) {
			%>
	        	  {
		        	 extend: 'delete',
		        	 className: 'idt-DeleteButton',
	       		 	 enabled: false,        	 
	     	    	 action: function(e, dt, node, config) {
		  	    		var table = $("#visitTypeDisplayTable");
		  	    		var selectedRows = $("#visitTypeDisplayTable").idtApi("getSelectedOptions")
						var params = "";
						
						// Convert the array of IDs to a comma delimited string list 
						for (var idx = 0; idx < selectedRows.length; idx++) {
							params += selectedRows[idx];
							if ((idx + 1) < selectedRows.length) {
								params += ",";
							}
						}
						
						var msgText = "<s:text name="visitType.alert.delete" />";
	        	   		var yesBtnText = "OK";
	        	   		var noBtnText = "Cancel";
	        	   		var action = webRoot + "/protocol/deleteVisitType.action?idsToDelete=" + params;
		  	    		if (selectedRows.length != 0) {	
		  	    			DeleteDialog(table, "warning", msgText, yesBtnText, noBtnText, action,
		        	   				true, "400px", "Confirm Deletion");
		  	    		}
		        	 } 
		       	  }
			<%
    			}
    		%>
	      	]
		})	
	});
</script>

<h3><s:text name="protocol.intervalForm.intervalTable.title"/></h3>
<p><s:text name="protocol.intervalForm.intervalTable.instruction"/></p>
<br>
<div class="idtTableContainer brics" id="intervalDisplayTable">
	<table id="visitTypeDisplayTable" class="table table-striped table-bordered" width="100%">
	</table>   
</div>

<%-- Include Footer --%>
<jsp:include page="/common/footer_struts2.jsp" />

</html>