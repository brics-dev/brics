<%@ page contentType="application/x-java-jnlp-file" %>  
<%@ page session="true" %>  
<%@taglib prefix="s" uri="/struts-tags" %>

<%  
response.setDateHeader ("Expires", 0); //prevents caching at the proxy server
response.setHeader("Content-disposition", "inline; filename=\"submissionTool.jnlp\"");
%>  
<?xml version="1.0" encoding="utf-8"?>
<jnlp spec="1.0+" codebase="_JNLPS_FOLDER_" >
	<information>
    	<title>Submission Tool</title>
    	<vendor>National Institutes of Health</vendor>
		<description>Client side data validation and upload.</description>
    </information>
	<security>
    	<all-permissions/>
    </security>
    
    <resources>
    	<j2se version="1.6+" java-vm-args="-Xmx768m" />
    
    	_JAR_DEPENDENCIES_
    </resources>
    
    <application-desc main-class="gov.nih.tbi.dictionary.validation.view.ValidationUploadManager">
		<argument><s:property value="%{validationToolServerName}" /></argument>
    	<argument><s:property value="%{moduleConstants.modulesDDTURL}" /></argument>
    	<argument><s:property value="%{uploadToolServerName}" /></argument>
    	<argument><s:property value="%{deploymentVersion}" /></argument>
    	<argument><%= request.getUserPrincipal().getName() %></argument>
    	<argument><s:property value="%{hash1}" /></argument>
    	<argument><s:property value="%{hash2}" /></argument>
    	<argument><s:property value="%{orgEmail}" /></argument>
    </application-desc>
</jnlp>  
