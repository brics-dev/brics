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



<html>

<jsp:include page="/common/header_struts2.jsp" />

<script type="text/javascript">
	var webRoot = "<s:property value='#webRoot'/>";
	$(document).ready(function() {
		
	
		
		$("#configureEformsTable").idtTable({
			idtUrl: webRoot+"/protocol/getEformConfigureList.action",
			idtData: {
				primaryKey: 'id'
			},
			dom: 'Bfrtip',
			select : "single", 
			columns: [
			{
                name: 'eformName',
                title: 'eForm',
                parameter: 'title',
                data: 'title'
            },
            {
                name: 'isConfigured',
                title: 'configured?',
                parameter: 'isConfigured',
                data: 'isConfigured',
                render: function(value) {
                	if(value == "true") {
                		return "Yes";
                	}else {
                		return "No";
                	}
                }
            }
			],
			buttons : [{
				text: 'Configure eForm',
       		 	className: 'idt-configureEformButton',
       		 	enabled: false,
       		 	enableControl: {
                       count: 1,
                       invert: true
                 },
	  	    	 action: function(e, dt, node, config) {
	  	    		var url = webRoot + "/protocol/configureEform.action?eformId=";
	  	    		var selectedRow = $("#configureEformsTable").idtApi("getSelectedOptions")
	  	    		
	  	    		
	  	    		if (selectedRow.length != 0) {			  	  			
	  	    			var selectedEformId = selectedRow[0];
		  	    		url = url + selectedEformId;
	  	  				//var WindowArgs = "toolbar=no," + "location=no," + "directories=no," + "status=yes,";
	  	  				//openPopup(url, "", WindowArgs+ "width=500,height=400,status=yes,location=yes,scrollbars=yes,resizable=yes");
	  	  				redirectWithReferrer(url);
	  	    		}
	      	   	}
			},{
				text: 'View Audit',
       		 	className: 'idt-configureEformButton',
       		 	enabled: false,
       		 	enableControl: {
                       count: 1,
                       invert: true
                 },
                 action: function(e, dt, node, config) {
 	  	    		var url = webRoot + "/protocol/configureEformAudit.action?eformId=";
 	  	    		var selectedRow = $("#configureEformsTable").idtApi("getSelectedOptions")
 	  	    		
 	  	    		
 	  	    		if (selectedRow.length != 0) {			  	  			
 	  	    			var selectedEformId = selectedRow[0];
 		  	    		url = url + selectedEformId;
 	  	  				var WindowArgs = "toolbar=no," + "location=no," + "directories=no," + "status=yes,";
 	  	  				openPopup(url, "", WindowArgs+ "width=500,height=400,status=yes,location=yes,scrollbars=yes,resizable=yes");
 	  	  				//redirectWithReferrer(url);
 	  	    		}
 	      	   	}
			}
				
			]
	
		});
	
	});
</script>


<h3><s:text name="protocol.psr.eforms.configure.title.display"/></h3>
<p><s:text name="protocol.psr.eforms.configure.instruction"/></p>
<br>
<div class="idtTableContainer brics" id="configureEformsTableDiv">
	<table id="configureEformsTable" class="table table-striped table-bordered" width="100%">
	</table>   
</div>

<%-- Include Footer --%>
<jsp:include page="/common/footer_struts2.jsp" />


</html>