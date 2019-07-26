<%@ page contentType="application/x-java-jnlp-file" %>  
<%@ page session="true" %>  
<%@taglib prefix="s" uri="/struts-tags" %>

<%  
response.setDateHeader ("Expires", 0); //prevents caching at the proxy server
response.setHeader("Content-disposition", "inline; filename=\"validationTool.jnlp\"");
%>  
<?xml version="1.0" encoding="utf-8"?>
<jnlp spec="1.0+" codebase="_JNLPS_FOLDER_" >
	<information>
    	<title>Validation Tool</title>
    	<vendor>National Institutes of Health</vendor>
		<description>Validation Tool</description>
    </information>
	<security>
    	<all-permissions/>
    </security>
    
    <resources>
    	<j2se version="1.6+" />
    
    	_JAR_DEPENDENCIES_
    </resources>
    
    <application-desc main-class="gov.nih.tbi.dictionary.validation.view.ValidationClient">
    	<argument><s:property value="%{validationToolServerName}" /></argument>
    	<argument><s:property value="%{moduleConstants.modulesDDTURL}" /></argument>
    	<argument><s:property value="%{deploymentVersion}" /></argument>
    	<argument><%= request.getUserPrincipal()==null? "anonymous":request.getUserPrincipal().getName() %></argument>
    	<argument><s:property value="%{hash1}" /></argument>
    	<s:if test="%{hash2 != ''}">
			<argument><s:property value="%{hash2}" /></argument>
		</s:if>
    </application-desc>
</jnlp>  
