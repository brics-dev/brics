<%@taglib prefix="s" uri="/struts-tags" %>
<%@ page contentType="application/x-java-jnlp-file" %>  
<%@ page session="true" %>  

<%  
response.setDateHeader ("Expires", 0); //prevents caching at the proxy server
response.setHeader("Content-disposition", "inline; filename=\"guidTool.jnlp\"");
%>  
<?xml version="1.0" encoding="utf-8"?>
<jnlp spec="1.0+" codebase="${modulesAccountURL}jnlps-guid/" >
	<information>
    	<title>GUID Tool</title>
    	<vendor>National Institutes of Health</vendor>
		<description>GUID Tool</description>
    </information>
	<security>
    	<all-permissions/>
    </security>
    
    <resources>
    	<j2se version="1.6+" />
    
    	<jar href='guid-client-fat.jar' main='true' download='eager'/>
	
    	<property name="jnlp.serviceUrl" value="<s:property value='%{guidToolServerName}' />" />
    	<property name="jnlp.jwt" value="<s:property value='%{guidJwt}' />" />
    	<property name="jnlp.orgEmail" value="<s:property value='%{orgEmail}' />" />
    </resources>
    
    <application-desc main-class="gov.nih.guid.ws.guid.client.GuidClientFrameWebstart">
    </application-desc>
</jnlp>  
