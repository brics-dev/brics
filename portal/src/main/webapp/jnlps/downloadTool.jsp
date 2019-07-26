<%@taglib prefix="s" uri="/struts-tags" %>
<%@ page contentType="application/x-java-jnlp-file" %>  
<%@ page session="true" %>  

<%  
response.setDateHeader ("Expires", 0); //prevents caching at the proxy server
response.setHeader("Content-disposition", "inline; filename=\"downloadTool.jnlp\"");
%>  
<?xml version="1.0" encoding="utf-8"?>

<jnlp spec="1.0+" codebase="${modulesAccountURL}jnlps" >

	<information>
    	<title>Download Tool</title>
    	<vendor>National Institutes of Health</vendor>
		<description>Download Tool</description>
    </information>
	<security>
    	<all-permissions/>
    </security>
    
    <resources>
    	<j2se version="1.6+" />
    
    	<jar href='webstarts-fat.jar' main='true' download='eager'/>
    </resources>
    
    <application-desc main-class="gov.nih.tbi.download.view.DownloadPackageView">
		<argument><s:property value="%{downloadToolServerName}" /></argument>
    	<argument><s:property value="%{deploymentVersion}" /></argument>
    	<argument><%= request.getUserPrincipal().getName() %></argument>
    	<argument><s:property value="%{hash1}" /></argument>
    	<argument><s:property value="%{hash2}" /></argument>
    	<argument><s:property value="%{orgEmail}" /></argument>
    </application-desc>
</jnlp>  
