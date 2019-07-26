<jsp:include page="/common/doctype.jsp" />
<%@ page import="java.util.List" %>
<%@ page import="gov.nih.nichd.ctdb.security.domain.SiteLink" %>
<%@ page import="gov.nih.nichd.ctdb.common.rs,java.util.Locale" %>
<%@ taglib uri="/struts-tags" prefix="s" %>
<%@ taglib uri="/WEB-INF/display.tld" prefix="display" %>
<%@ taglib uri="/WEB-INF/security.tld" prefix="security" %>


<%Locale l=request.getLocale(); %>
<%-- CHECK PRIVILEGES --%>
<security:check privileges="sysadmin"/>

<html>

<s:set var="pageTitle" scope="request">
	<s:text name="sitelink.title.display" />
</s:set>

<jsp:include page="/common/header_struts2.jsp" />

<s:set var="webRoot" value="#systemPreferences.get('app.webroot')"/>

<script type="text/javascript" >
$(document).ready(function() {
	var basePath = "<s:property value="#webRoot"/>";
	$("#siteLinksTable").idtTable({
		idtUrl: "<s:property value='#webRoot'/>/admin/getSiteLinkAdmin.action",
		order:[],
		select: "multi",
		dom: "Bfrtip",
		idtData: {
     		primaryKey: "linkId"
  		},	
        columns: [
            {
                name: 'name',
                title: '<%=rs.getValue("report.urlName", l)%>',
                parameter: 'name',
                data: 'name',
            },
            {
                name: 'address',
                title: '<%=rs.getValue("report.urlAddress", l)%>',
                parameter: 'address',
                data: 'address',
            },
            {
                name: 'description',
                title: '<%=rs.getValue("report.urlDescription", l)%>',
                parameter: 'description',
                data: 'description',
            }  
        ],
        buttons: [
        	<%
    			List<SiteLink> links = (List<SiteLink>) request.getAttribute("siteLinks");
    	        if (links.size() >= 2) {
            %>
            	{
            		text: "<s:text name='button.siteURL.changeOrder'/>",
            		className: "changeURLDisplayOrderBtn",
            		 enabled: true,
            		 enableControl: {
                            count: 0,
                            invert: false
                     },
            		action: function(e, dt, node, config) {
            			redirectWithReferrer('<s:property value="#webRoot"/>/admin/siteLinkOrder.action');
            			
            		}
            	},
        	<%
    			}
    		%> 
        	{
        		text: "<s:text name='button.siteURL.AddURL'/>",
        		className: "addURLBtn",
        		 enabled: true,
        		 enableControl: {
                        count: 0,
                        invert: false
                 },
        		action: function(e, dt, node, config) {
        			redirectWithReferrer('<s:property value="#webRoot"/>/admin/addSiteLink.action');
        			
        		}
        	},
        	{
        		text: "<s:text name='button.Edit' />",
        		className: "editBtn",
                enableControl: {
                    count: 1,
                    invert:true
                },
                enabled: false,      		 
        		action: function(e, dt, node, config) {
        			var selectedId = $("#siteLinksTable").idtApi('getSelected');
    				var url = basePath + "/admin/editSiteLink.action?id=" + selectedId;
    				redirectWithReferrer(url);
        			
        		}
        	},
        	{
        		text: "<s:text name='button.Delete' />",
        		className: "deleteBtn",
                enabled: false,      		 
        		action: function(e, dt, node, config) {
        			var selectedIds = $("#siteLinksTable").idtApi('getSelected');
        			var text = "";
    				if(selectedIds.length == 1) {
    					text = "Are you sure you want to remove this URL?";
    				}else {
    					text = "Are you sure you want to remove these URLs?"
    				}
					$.ibisMessaging(
								"dialog", 
								"info", 
								text, {
								buttons: {
									"OK": function() {
										var url = basePath + "/admin/deleteSiteLink.action?deleteId="+selectedIds ;
										console.log('url', url);
										
										$(this).dialog("close");
										redirectWithReferrer(url);
									},
									"Cancel": function() {
										$(this).dialog("close");
									}
								}
							});
        			
        		}
        	},        	
            ]
 
	});	
	
})
</script>

<%-- Presentation Logic Only Below--%>
<div>
	<s:text name="siteLink.instruction"/>
</div>
<br>
<div id="siteLinksContainer" class="idtTableContainer brics">
	<table id="siteLinksTable" class="table table-striped table-bordered" width="100%">
	</table>
</div>
<%-- Include Footer --%>
<jsp:include page="/common/footer_struts2.jsp" />
</html>