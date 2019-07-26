<%@ page import="gov.nih.nichd.ctdb.common.CtdbConstants,
                 gov.nih.nichd.ctdb.security.domain.User,
                 gov.nih.nichd.ctdb.common.CtdbForm"
                 %><%@ 
                 taglib uri="/struts-tags" prefix="s"%>
<s:bean name="gov.nih.nichd.ctdb.util.common.SystemPreferences" var="systemPreferences" />
<link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/jqueryui/1.12.1/themes/smoothness/jquery-ui.min.css">
<s:set var="webRoot" value="#systemPreferences.get('app.webroot')"/>
<s:set var="imageRoot" value="#systemPreferences.get('app.imageroot')"/>

<link rel="stylesheet" type="text/css" href="<s:property value="#webRoot"/>/common/css/jquery.dataTables.css" />
<link rel="stylesheet" type="text/css" href="<s:property value="#webRoot"/>/common/css/datatables.css" />
<link rel="stylesheet" type="text/css" href="<s:property value="#webRoot"/>/common/css/bootstrap/bootstrap-3.1.1.min.css" />
<link rel="stylesheet" type="text/css" href="<s:property value="#webRoot"/>/common/css/bootstrap/bootstrap-multiselect.css" />

<!-- new datatables styles -->
<link rel="stylesheet" type="text/css" href="<s:property value="#webRoot"/>/common/designs/dataTables/2.0/css/datatables-pdfmake.min.css" />
<link rel="stylesheet" type="text/css" href="<s:property value="#webRoot"/>/common/designs/dataTables/2.0/css/responsive.dataTables.min.css" />
<link rel="stylesheet" type="text/css" href="<s:property value="#webRoot"/>/common/designs/dataTables/2.0/css/datatablesWidget.css" />
<link rel="stylesheet" type="text/css" href="<s:property value="#webRoot"/>/common/designs/dataTables/2.0/css/idtSearchColumnPlugin.css" />
<link rel="stylesheet" type="text/css" href="<s:property value="#webRoot"/>/common/designs/dataTables/2.0/css/idtSelectSetPlugin.css" />

<link rel="stylesheet" type="text/css" href="<s:property value="#webRoot"/>/common/jquery.selectbox.css" />
<link rel="stylesheet" type="text/css" href="<s:property value="#webRoot"/>/common/c-<s:property value="#systemPreferences.get('template.global.appName')"/>/css/style.css" />
<link type="text/css" href="https://maxcdn.bootstrapcdn.com/font-awesome/4.7.0/css/font-awesome.min.css" rel="stylesheet" />


