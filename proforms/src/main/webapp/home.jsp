<jsp:include page="/common/doctype.jsp" />
<%@ page import="java.util.List" %>
<%@ page import="javax.servlet.http.Cookie" %>
<%@ page import="gov.nih.nichd.ctdb.common.CtdbConstants"%>
<%@ page import="gov.nih.nichd.ctdb.common.CtdbLookup"%>
<%@ page import="gov.nih.nichd.ctdb.common.StrutsConstants"%>
<%@ page import="gov.nih.nichd.ctdb.common.util.Utils"%>
<%@ page import="gov.nih.nichd.ctdb.security.domain.User"%>
<%@ page import="gov.nih.nichd.ctdb.protocol.domain.Protocol" %>
<%@ page import="gov.nih.nichd.ctdb.common.rs,java.util.Locale" %>
<%@ taglib uri="/WEB-INF/datatables.tld" prefix="idt" %>
<%@ taglib uri="/WEB-INF/display.tld" prefix="display" %>
<%@ taglib uri="/struts-tags" prefix="s"%>

<%
    String focus = "document.loginForm.username.focus();";
    for (Cookie c : request.getCookies()) {
    	if (c.getName().equals("CtdbUserCookie") && !Utils.isBlank(c.getValue())) {
    		focus = "document.loginForm.password.focus();";
    		break;
    	}
    }
    
    User user = (User) session.getAttribute(CtdbConstants.USER_SESSION_KEY);
    Protocol study = (Protocol) session.getAttribute(CtdbConstants.CURRENT_PROTOCOL_SESSION_KEY);
    Locale l = request.getLocale();
%>

<html>
<%-- Include Header --%>
<s:bean name="gov.nih.nichd.ctdb.util.common.SystemPreferences" var="systemPreferences" />
<s:set var="piiSwitch" value="#systemPreferences.get('guid_with_non_pii')" />

<s:set var="pageTitle" scope="request">
	<s:text name="myworkspace.home.title">
		<s:param>
			<s:if test="#piiSwitch == 1">
				<s:text name="application.name.nonPii" />
			</s:if>
			<s:else>
				<s:text name="application.name.pii" />
			</s:else>
		</s:param>
	</s:text>
</s:set>

<jsp:include page="/common/header_struts2.jsp" />
<style>
 #protocolSnapshotTable_wrapper {
 	overflow: visible;
 }
</style>

<script type="text/javascript">

var canEditStudy = <%= Boolean.toString(user.isCreateStudy()) %>;



/**** Tasks to do on page load ****/
$(document).ready(function()
{
	var basePath = "<s:property value="#webRoot"/>";
	var dictionaryUrl = "<s:property value="#systemPreferences.get('brics.modules.ddt.url')"/>";
	
	var $footer = $("#footer");
	$footer.append('<iframe id="dictionarySession" style="display:none;height:0px;width:0px;"></iframe>');
	$("#dictionarySession").load(function() {
		//TODO: Right now keeping the IFRAME on the page isn't too much of a problem since it is hidden, removing it causes a problem on IE, will need to investigate a new solution
		//$("#dictionarySession").remove();
	});
	$("#dictionarySession").attr("src", dictionaryUrl);
	
	
	$("#protocolSnapshotTable").idtTable({
		idtUrl: "<s:property value='#webRoot'/>/getProtocolList.action",
		idtData: {
			primaryKey: 'id'
		},
		dom: 'Bfrtip',
		select: "multi",
        columns: [
            {
                name: 'switchStudyLink',
                title: '<%=rs.getValue("study.add.number.display",l)%>',
                parameter: 'switchStudyLink',
                data: 'switchStudyLink'
            },
            {
                name: 'name',
                title: '<%=rs.getValue("protocol.add.name.display",l)%>',
                parameter: 'name',
                data: 'name'
            },
            {
                name: 'studyTypeName',
                title: '<%=rs.getValue("study.add.type.display",l)%>',
                parameter: 'studyTypeName',
                data: 'studyTypeName'
            },	      
            {
                name: 'status',
                title: '',
                parameter: 'status.shortName',
                data: 'status',
                visible: false
            }
        ],
        buttons: [
	      	  {
       		 	text: 'View Audit',
       		 	className: 'idt-viewAuditButton',
       		 	enabled: false,
       		 	enableControl: {
                       count: 1,
                       invert: true
                 },
	  	    	 action: function(e, dt, node, config) {
	  	    		var url = "";
	  	    		var basePath = "<s:property value="#webRoot"/>";
	  	    		var selectedRow = $("#protocolSnapshotTable").idtApi("getSelectedOptions")
	  	    		var selectedProtocolId = selectedRow[0];
	  	    		
	  	    		if (selectedRow.length != 0) {
	  	    			url = basePath + "/protocol/protocolAudit.action?id=" + selectedProtocolId;
	  	    			Javascript:popupWindow(url);
	  	    		}
	      	   	} 
	      	  },
        	  {
	        	 extend: 'delete',
	        	 className: 'idt-DeleteButton',
       		 	enabled: false,
       		 	enableControl: {
                       count: 1,
                       invert: true
                 },	        	 
     	    	 action: function(e, dt, node, config) {
     	    			var basePath = "<s:property value="#webRoot"/>";
     	    			var table = $('#protocolSnapshotTable');
	        	   		var msgText = "<s:text name="study.alert.remove" />";
	        	   		var selectedRows = $('#protocolSnapshotTable').idtApi('getSelectedOptions');
	        	   		var yesBtnText = "Delete";
	        	   		var noBtnText = "Do Not Delete";
	        	   		var action = basePath + "/protocol/protocolDelete.action?id=" + selectedRows; 

	        	   		DeleteDialog(table, "warning", msgText, yesBtnText, noBtnText, action,
	        	   				true, "400px", "Confirm Deletion");
	        	   		
      	   		
	        	   	} 
	       },
			<%
			if (user.isSysAdmin()) {
			%>
	      {
     		 text: "<s:text name='myworkspace.pickstudy.CreateStudy' />",
     		 className: 'idt-createProtocolBtn',
     		 enabled: true,
    		 enableControl: {
                    count: 0,
                    invert: false
             },
  	    	 action: function(e, dt, node, config) {
  	    		var basePath = "<s:property value="#webRoot"/>";
  	  			var url = basePath + "/protocol/createProtocol.action";
  				redirectWithReferrer(url);

      	   	} 
	      }	
		<%
			}
		%>
      	]
	})	
});
</script>



<div>
<%
	if ( (study == null) || user.isSysAdmin() ) {
%>
	<s:text name="myworkspace.welcome.content">
		<s:param>
			<s:if test="#piiSwitch == 1">
				<s:text name="application.name.nonPii" />
			</s:if>
			<s:else>
				<s:text name="application.name.pii" />
			</s:else>
		</s:param>
	</s:text>
<%
	} else {
%>
	<s:text name="myworkspace.welcome.content.noInstructions">
		<s:param>
			<s:if test="#piiSwitch == 1">
				<s:text name="application.name.nonPii" />
			</s:if>
			<s:else>
				<s:text name="application.name.pii" />
			</s:else>
		</s:param>
	</s:text>
<%
	}
%>
</div>

<br>



<h3><s:text name="myworkspace.pickstudy.title.display" /></h3>

<div id="protocolSnapshotContainer" class="idtTableContainer brics">
	<div>
		<p><s:text name="myworkspace.pickstudy.subinstruction.display"/></p>
	</div>
	
	<table id="protocolSnapshotTable" class="table table-striped table-bordered" width="100%">
	</table>
</div>

<%-- Include Footer --%>
<jsp:include page="/common/footer_struts2.jsp" />
</html>