<%@ page contentType="application/x-java-jnlp-file" %>  
<%@ page session="true" %>  
<%@taglib prefix="s" uri="/struts-tags" %>
<%@ page import="com.opensymphony.xwork2.ActionContext" %>
<%!
    private String getContextValue(ActionContext context, String param) {
        Object valObj = context.getValueStack().findValue(param);
        String result = (String)valObj;
        return result;
    }

    private void printActionVar(ActionContext context, String varName) {

        String valStr = getContextValue(context, varName);

        System.out.println(varName + " = " + valStr);
    }

%>
<%


response.setDateHeader ("Expires", 0); //prevents caching at the proxy server
response.setHeader("Content-disposition", "inline; filename=\"submissionTool64.jnlp\"");



%>  
<?xml version="1.0" encoding="utf-8"?>
<jnlp spec="1.0+" codebase="${modulesAccountURL}jnlps" >
	<information>
    	<title>Submission Tool</title>
    	<vendor>National Institutes of Health</vendor>
		<description>Client side data validation and upload.</description>
    </information>
	<security>
    	<all-permissions/>
    </security>
    
    <application-desc main-class="gov.nih.tbi.dictionary.validation.view.ValidationUploadManager">
		<argument><s:property value="%{validationToolServerName}" /></argument>
    	<argument><s:property value="%{modulesDdtUrl}" /></argument>
    	<argument><s:property value="%{uploadToolServerName}" /></argument>
    	<argument><s:property value="%{deploymentVersion}" /></argument>
    	<argument><s:property value="%{usernameFromRequest}" /></argument>
    	<%-- <argument><%= username %></argument> --%>
    	<%-- Comment --%>
    	<argument><s:property value="%{hash1}" /></argument>
    	<argument><s:property value="%{hash2}" /></argument>
    	<argument><s:property value="%{orgEmail}" /></argument>
    	<argument><s:property value="%{webstartPortalRoot}" /></argument>
    	<argument><s:property value="%{webstartPerformExtraValidation}" /></argument>
    </application-desc>
    
    <resources>
    	<j2se version="1.6+" java-vm-args="${webstart64BitJVMArgs}" />
    
    	<jar href='webstarts-fat.jar' main='true' download='eager'/>

	
	
    </resources>


    <%
    	ActionContext context = ActionContext.getContext();

    	System.out.println("Welcome to Submission Tool !");

        printActionVar(context, "validationToolServerName");
        printActionVar(context, "modulesDdtUrl");
        printActionVar(context, "uploadToolServerName");
        printActionVar(context, "usernameFromRequest");
        printActionVar(context, "modulesJavaVMArgs");
        printActionVar(context, "deploymentVersion");
        printActionVar(context, "hash1");
        printActionVar(context, "hash2");
        printActionVar(context, "orgEmail");
        printActionVar(context, "webstartPortalRoot");
        printActionVar(context, "webstartPerformExtraValidation");


    %>

    
</jnlp>  
